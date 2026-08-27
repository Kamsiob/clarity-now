package com.kamsiob.claritynow.domain.replay

import com.kamsiob.claritynow.data.event.ClarityEventJson
import com.kamsiob.claritynow.data.event.ClarityEventType
import com.kamsiob.claritynow.data.event.ItemStatus
import com.kamsiob.claritynow.data.event.SubjectKind
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * MASTER_BUILD_PROMPT 6.6.
 *
 * `testdata/golden-log.json` and `testdata/golden-state.json` are committed as plain,
 * readable JSON. This test regenerates them on demand and otherwise checks that the
 * committed files still describe what this build actually does.
 *
 * Regenerate deliberately, never casually:
 *
 *     ./gradlew :app:testDebugUnitTest -PregenerateGolden=true
 *
 * A change to either file is a change to the contract with the desktop app, so it
 * should be visible and argued for in a diff rather than appearing quietly.
 */
class GoldenFixtureTest {

    private val json = Json {
        prettyPrint = true
        prettyPrintIndent = "  "
        encodeDefaults = true
        explicitNulls = true
    }

    private val regenerate: Boolean =
        System.getProperty("clarity.regenerateGolden")?.toBoolean() == true

    private fun stateText(state: ClarityState): String =
        json.encodeToString(ClarityState.serializer(), state.canonical())

    @Test
    fun `the committed fixture matches this build`() {
        val logFile = File(GoldenFixture.LOG_PATH)
        val stateFile = File(GoldenFixture.STATE_PATH)

        val log = GoldenFixture.log()
        val logText = ClarityEventJson.encodeLog(log)
        val expectedState = stateText(ClarityReplay.replay(log))

        if (regenerate) {
            logFile.absoluteFile.parentFile?.mkdirs()
            logFile.writeText(logText + "\n")
            stateFile.writeText(expectedState + "\n")
        }

        assertTrue("missing ${logFile.absolutePath}", logFile.isFile)
        assertTrue("missing ${stateFile.absolutePath}", stateFile.isFile)

        // The committed log replays to the committed state, on this build, today.
        val committedLog = ClarityEventJson.decodeLog(logFile.readText())
        assertTrue("unknown types: ${committedLog.skippedTypes}", committedLog.skippedTypes.isEmpty())
        assertEquals(
            "the committed log no longer replays to the committed state",
            stateFile.readText().trim(),
            stateText(ClarityReplay.replay(committedLog.events)).trim(),
        )
        assertEquals(
            "the committed log no longer matches GoldenFixture",
            logFile.readText().trim(),
            logText.trim(),
        )
    }

    @Test
    fun `the fixture covers every event type`() {
        val used = GoldenFixture.log().map { it.type }.toSet()
        val missing = ClarityEventType.entries - used
        assertTrue("the golden log never exercises: ${missing.joinToString()}", missing.isEmpty())
    }

    /**
     * The five transitions the Addendum 01 vocabulary added, each one asserted on
     * the state rather than on the presence of an event type.
     *
     * `the fixture covers every event type` above is satisfied by an event of the
     * right type sitting anywhere in the log, including one the reducer refused. The
     * contract with the desktop app is not that these types appear; it is that
     * replaying them produces this state. `docs/EVENT_FORMAT.md` 8 names these five
     * as the transitions nothing else in the fixture would reach.
     */
    @Test
    fun `the fixture exercises the inbox, an estimate and an extended session`() {
        val state = GoldenFixture.state()
        val idea = state.items.getValue("item-idea")

        // Captured with no area on day 3, filed into Personal on day 12, and never
        // active at any point along the way. It could not be while it was unfiled,
        // and Personal already had an active item when the filing landed.
        assertEquals("area-personal", idea.areaId)
        assertEquals(ItemStatus.QUEUED, idea.status)
        assertNull(idea.activeSince)
        assertNull(idea.completedAt)
        assertEquals("Find last winter heating bill", idea.firstStep)
        // Ninety minutes on capture, revised to forty five three days later. The
        // event states the value after it and the reducer applies that.
        assertEquals(45, idea.estimateMinutes)
        // Nothing is left in the inbox, because the one thing that went in came out.
        assertTrue(state.unfiledItems.isEmpty())

        // Fifteen minutes planned, ten added, finished at the extended length. The
        // session neither restarted nor ended when it was extended.
        val extended = state.focusSessions.getValue("focus-3")
        assertEquals(1500, extended.plannedSeconds)
        assertEquals(1500, extended.actualSeconds)
        assertEquals(FocusOutcome.COMPLETED, extended.outcome)

        // The renamed type, folded as a neutral ending rather than as anything else.
        assertEquals(FocusOutcome.ENDED_EARLY, state.focusSessions.getValue("focus-2").outcome)

        // APP_OPENED is in the log and changes nothing in the projection. That is
        // the point of it: a presence marker with no reader but gap detection.
        val withoutOpens = ClarityReplay.replay(
            GoldenFixture.log().filterNot { it.type == ClarityEventType.APP_OPENED },
        )
        assertEquals(
            "APP_OPENED must fold into nothing at all. A last opened date on the " +
                "projection would be a tally of presence in the object every screen " +
                "reads, which is what Addendum 01 4d exists to prevent.",
            stateText(withoutOpens.copy(eventsApplied = 0)),
            stateText(GoldenFixture.state().copy(eventsApplied = 0)),
        )
    }

    /**
     * The keys the engine's own rules are stated in survive into the state.
     *
     * A rendered sentence is not enough to derive `FiringHistory` from: two variants
     * of one family read as two different sentences, and a stage 3 line and a stage
     * 1 line of the same family read as unrelated. CLARITY_LOGIC_ENGINE.md 7.6 step
     * 1 excludes a variant for 90 days, 7.3 cools a family for 14, and 6.4 caps
     * `hardStretch` at 42. All three are keyed on values that only exist because
     * they are carried on the event. Issue 19.
     */
    @Test
    fun `the fixture carries the family and variant keys the engine needs`() {
        val report = GoldenFixture.state().reports.getValue("2026-01-11")
        val sections = report.sections.associateBy { it.sectionKey }

        assertEquals("intakeVsOutput", sections.getValue("observations").familyKey)
        assertEquals("ob.flow.s1.l08", sections.getValue("observations").variantKey)
        assertEquals("area-work", sections.getValue("observations").subjectId)
        assertEquals(SubjectKind.AREA, sections.getValue("observations").subjectKind)
        // A family with no subject is the other legal shape, not a missing value.
        assertNull(sections.getValue("focus").subjectId)
        assertNull(sections.getValue("focus").subjectKind)
        assertTrue(report.sections.all { it.escalationStage >= 1 })
        assertEquals(
            "two sections of one report rendering the same variant would make the " +
                "90 day exclusion in 7.6 step 1 untestable against this fixture",
            report.sections.size,
            report.sections.map { it.variantKey }.distinct().size,
        )
    }

    @Test
    fun `the fixture ends in a state worth asserting about`() {
        val state = GoldenFixture.state()

        assertTrue("the fixture should leave the invariants intact", ClarityInvariants.check(state).isEmpty())
        assertTrue("the fixture should not produce diagnostics", state.diagnostics.isEmpty())

        // Three live areas: the fourth was archived and then tombstoned.
        assertEquals(listOf("Work", "Personal", "Health"), state.liveAreas.map { it.name })
        assertNotNull(state.areas.getValue("area-scratch").deletedAt)
        assertTrue(state.archivedAreas.isEmpty())

        // The contested area resolved to the laptop's promotion, and nothing vanished.
        // item-idea is in that queue because it was filed there in the second week,
        // and filing left it queued rather than promoting it.
        assertEquals("item-dentist", state.activeItemIn("area-personal")?.id)
        assertEquals(
            setOf("item-letter", "item-tap", "item-idea"),
            state.queueIn("area-personal").map { it.id }.toSet(),
        )

        // Health was deliberately left idle by requeueing its active item.
        assertEquals(null, state.activeItemIn("area-health"))
        assertEquals(listOf("item-swim"), state.queueIn("area-health").map { it.id })

        // Both divergences were recorded rather than swallowed.
        assertEquals(
            setOf(ConflictKind.TWO_ACTIVE_IN_AREA, ConflictKind.DUPLICATE_DATE_KEY),
            state.conflicts.map { it.kind }.toSet(),
        )
        // Two days carry a Pulse, and the contested day holds exactly one entry.
        assertEquals(setOf("2026-01-06", "2026-01-18"), state.pulses.keys)
        assertEquals("pulse-2-laptop", state.pulses.getValue("2026-01-18").id)

        // The accepted plan is stored in the first person, never as an imperative.
        val plan = state.plans.getValue("plan-1")
        assertTrue(plan.isAccepted)
        assertTrue(plan.committedLine.startsWith("If it is"))
        assertTrue(plan.offeredLine.startsWith("One option"))

        // A deleted item stays as a tombstone so its Trail entry still renders.
        assertNotNull(state.items.getValue("item-notes").deletedAt)
        assertEquals(ItemStatus.COMPLETED, state.items.getValue("item-walk").status)
    }

    @Test
    fun `the fixture replays identically from a checkpoint`() {
        val log = GoldenFixture.log()
        val head = log.take(log.size / 2)
        val checkpoint = ClarityReplay.checkpoint(head)
        assertEquals(
            stateText(ClarityReplay.replay(log)),
            stateText(ClarityReplay.replayFrom(checkpoint, log)),
        )
    }

    @Test
    fun `the fixture is readable json rather than escaped strings`() {
        val text = File(GoldenFixture.LOG_PATH).readText()
        assertTrue("payloads should be nested objects", text.contains("\"payload\": {"))
        assertTrue("ids should be readable", text.contains("\"evt-001\""))
        assertTrue(text.all { it.code < 128 })
    }
}
