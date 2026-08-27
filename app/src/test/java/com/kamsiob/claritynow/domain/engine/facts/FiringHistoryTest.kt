package com.kamsiob.claritynow.domain.engine.facts

import com.kamsiob.claritynow.domain.engine.FiringHistory
import com.kamsiob.claritynow.domain.query.TEST_ZONE
import com.kamsiob.claritynow.domain.query.TrailQueries
import com.kamsiob.claritynow.domain.query.TrailTestLog
import com.kamsiob.claritynow.domain.query.dateKey
import com.kamsiob.claritynow.domain.query.startOfDay
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * `FiringHistory`, derived from the log and from nothing else.
 * CLARITY_LOGIC_ENGINE.md 2.1 and 7.6.
 *
 * The cross device requirement in 14 is the one that matters most here: two objects
 * rebuilt independently from the same merged log must produce identical selections
 * for the same `dateKey`. A history that came from DataStore could not, because
 * DataStore does not merge, and the disagreement would be invisible until two
 * devices printed different sentences from one log.
 */
class FiringHistoryTest {

    private fun log(): TrailTestLog {
        val log = TrailTestLog()
        log.pulse(1, "p1", "persistence", "persistence.s1.03", stage = 1, subjectId = "i1")
        log.pulse(4, "p2", "persistence", "persistence.s2.11", stage = 2, subjectId = "i1")
        log.pulse(5, "p3", "persistence", "persistence.s1.03", stage = 1, subjectId = "i2")
        log.pulse(6, "p4", "spread", "spread.s1.02", stage = 1)
        log.report(
            day = 7,
            reportId = "r1",
            weekStartKey = dateKey(7),
            headlineKey = "netOutflow",
            headlineVariantKey = "hd.out.04",
            sections = listOf(
                section("neglectedArea", "ob.neg.s1.02", stage = 1, subjectId = "health"),
                section("intakeVsOutput", "ob.flow.s2.09", stage = 2),
            ),
        )
        log.planOffered(
            day = 7,
            planId = "pl1",
            weekStartKey = dateKey(7),
            frameKey = "frm.01",
            cueKey = "cue.band.01",
            actionKey = "act.neg.01",
            familyKey = "neglectedArea",
            subjectId = "health",
        )
        return log
    }

    private fun history(log: TrailTestLog): FiringHistory =
        FiringHistory.from(log.queries(), startOfDay(30))

    @Test
    fun `every variant maps to the most recent day it appeared on`() {
        val history = history(log())
        assertEquals(dateKey(5), history.variantsUsed["persistence.s1.03"])
        assertEquals(dateKey(4), history.variantsUsed["persistence.s2.11"])
        assertEquals(dateKey(6), history.variantsUsed["spread.s1.02"])
        assertEquals(dateKey(7), history.variantsUsed["hd.out.04"])
        assertEquals(dateKey(7), history.variantsUsed["ob.neg.s1.02"])
    }

    /** Escalation is tracked per subject, so two items climb independent ladders. */
    @Test
    fun `a stage belongs to a family and a subject together`() {
        val history = history(log())
        assertEquals(2, history.lastStage("persistence", "i1"))
        assertEquals(1, history.lastStage("persistence", "i2"))
        assertNull(history.lastStage("persistence", "i3"))
        assertEquals(2, history.lastStage("intakeVsOutput", null))
        assertEquals(1, history.lastStage("neglectedArea", "health"))
    }

    @Test
    fun `the last pulse family is the newest one, and a report is not a pulse`() {
        assertEquals("spread", history(log()).lastPulseFamily)
    }

    /**
     * A plan records the three keys it was built from and nothing else.
     *
     * Recording a firing against its family too would put that family into a
     * cooldown the observation did not earn, and 10.6 is emphatic that guidance
     * never gets its own hold over what an observation may say.
     */
    @Test
    fun `a plan records its frame, cue and action and no firing of its own`() {
        val history = history(log())
        assertEquals(dateKey(7), history.variantsUsed["frm.01"])
        assertEquals(dateKey(7), history.variantsUsed["cue.band.01"])
        assertEquals(dateKey(7), history.variantsUsed["act.neg.01"])
        // The observation that motivated it recorded the firing, on its own week key.
        assertEquals(dateKey(7), history.lastFiredBySubject["neglectedArea" to "health"])
    }

    @Test
    fun `the ninety day window and the family cooldown read the same history`() {
        val history = history(log())
        assertTrue(history.variantUsedWithin("persistence.s1.03", dateKey(20), days = 90))
        assertFalse(history.variantUsedWithin("persistence.s1.03", dateKey(120), days = 90))
        assertFalse(
            "a variant never used is never excluded",
            history.variantUsedWithin("spread.s9.99", dateKey(20), days = 90),
        )

        assertTrue(history.inCooldown("persistence", "i1", dateKey(6), cooldownDays = 3))
        assertFalse(history.inCooldown("persistence", "i1", dateKey(8), cooldownDays = 3))
        assertFalse(history.inCooldown("burst", "work", dateKey(8), cooldownDays = 10))
    }

    /**
     * Two histories rebuilt independently from the same merged log are identical.
     *
     * The log is handed over in reverse, which is what a merge looks like from the
     * receiving device. `TrailQueries` sorts into total order at construction and
     * every "most recent" resolves by `dateKey` first, so nothing here depends on
     * arrival order.
     */
    @Test
    fun `two histories rebuilt from the same log agree`() {
        val log = log()
        val forward = FiringHistory.from(log.queries(), startOfDay(30))
        val reversed = TrailQueries(log.events().reversed(), TEST_ZONE)
        val merged = FiringHistory.from(reversed, startOfDay(30))
        assertEquals(forward, merged)
    }

    @Test
    fun `an empty log rebuilds to nothing having fired`() {
        val rebuilt = FiringHistory.from(TrailTestLog().queries(), startOfDay(30))
        assertEquals(FiringHistory.EMPTY, rebuilt)
    }

    private fun isComment(text: String): Boolean {
        val trimmed = text.trimStart()
        return trimmed.startsWith("//") || trimmed.startsWith("*") || trimmed.startsWith("/*")
    }

    /**
     * No preference store read can reach the engine, checked by reading the source.
     *
     * 7.6 forbids it outright and the acceptance criteria for this phase ask for a
     * test. `DomainPurityTest` already bans every `androidx` import in this package,
     * which covers the platform store itself; this adds the app's own by name,
     * because that is the shape the mistake would actually take.
     */
    @Test
    fun `no engine source mentions a preference store`() {
        val engine = File("src/main/java/com/kamsiob/claritynow/domain/engine")
        assertTrue(
            "unit tests are expected to run from the app module directory, and this run " +
                "is in ${File("").absolutePath}",
            engine.isDirectory,
        )
        val banned = Regex("DataStore|ClarityPreferences|SharedPreferences")
        // Comment lines are stripped first, for the reason `DomainPurityTest` gives
        // about `StableHash`: the file that states the prohibition names the thing
        // it prohibits, and a scan that read comments would fail on the explanation.
        val offenders = engine.walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .flatMap { file ->
                file.readLines().withIndex()
                    .filterNot { isComment(it.value) }
                    .filter { banned.containsMatchIn(it.value) }
                    .map { "${file.path}:${it.index + 1}: ${it.value.trim()}" }
            }
            .toList()
        assertTrue(
            "engine state must derive from the log, never from a preference store:\n" +
                offenders.joinToString("\n"),
            offenders.isEmpty(),
        )
    }
}
