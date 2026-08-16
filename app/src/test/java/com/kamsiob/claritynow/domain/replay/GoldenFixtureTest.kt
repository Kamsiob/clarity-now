package com.kamsiob.claritynow.domain.replay

import com.kamsiob.claritynow.data.event.ClarityEventJson
import com.kamsiob.claritynow.data.event.ClarityEventType
import com.kamsiob.claritynow.data.event.ItemStatus
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
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
        assertEquals("item-dentist", state.activeItemIn("area-personal")?.id)
        assertEquals(
            setOf("item-letter", "item-tap"),
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
