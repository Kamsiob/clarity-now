package com.kamsiob.claritynow.domain.replay

import com.kamsiob.claritynow.data.event.ClarityEvent
import com.kamsiob.claritynow.data.event.FocusCompleted
import com.kamsiob.claritynow.data.event.FocusEndedEarly
import com.kamsiob.claritynow.data.event.FocusExtended
import com.kamsiob.claritynow.data.event.FocusStarted
import com.kamsiob.claritynow.domain.query.TrailTestLog
import com.kamsiob.claritynow.domain.query.area
import com.kamsiob.claritynow.domain.query.at
import com.kamsiob.claritynow.domain.query.item
import com.kamsiob.claritynow.domain.query.promote
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Random

/**
 * How the log folds into a focus session. MASTER_BUILD_PROMPT 14b.5.
 *
 * The sentence this file exists for is the one in 14b.5: "a session's planned
 * duration is the newest `newPlannedSeconds` rather than the value in
 * `FOCUS_STARTED`, and every later reader, the completion path, the Trail, the
 * engine and the widget, reads the folded value." Getting it wrong gives back the
 * added minutes at the next cold start, and it does so silently.
 */
class FocusFoldTest {

    private val json = Json { prettyPrint = false; encodeDefaults = true }

    private val areaId = "area-1"
    private val itemId = "item-1"
    private val sessionId = "session-1"
    private val startedAt = at(0, 9, 30)

    private fun serialize(state: ClarityState): String =
        json.encodeToString(ClarityState.serializer(), state.canonical())

    private fun startedLog(plannedSeconds: Int = 1_500): TrailTestLog {
        val log = TrailTestLog()
        log.area(at(0, 9, 0), areaId, "Work")
        log.item(at(0, 9, 0), itemId, areaId, "Draft the letter")
        log.promote(at(0, 9, 1), itemId, areaId, "Draft the letter")
        log.add(startedAt, FocusStarted(sessionId, areaId, itemId, plannedSeconds))
        return log
    }

    private fun fold(log: TrailTestLog): ClarityState = ClarityReplay.replay(log.events())

    private fun session(log: TrailTestLog): FocusSessionState =
        fold(log).focusSessions.getValue(sessionId)

    // Extensions --------------------------------------------------------------

    @Test
    fun `two extensions accumulate and the newest planned duration wins`() {
        val log = startedLog()
        log.add(at(0, 9, 40), FocusExtended(sessionId, 600, 2_100))
        log.add(at(0, 9, 50), FocusExtended(sessionId, 600, 2_700))

        val folded = session(log)

        assertEquals(2_700, folded.plannedSeconds)
        assertEquals("an extension is not a terminal event", FocusOutcome.RUNNING, folded.outcome)
        assertNull("an extension does not end the session", folded.endedAt)
        assertEquals("and it does not restart it", startedAt, folded.startedAt)
    }

    @Test
    fun `delivering the same extension twice does not add the time twice`() {
        val log = startedLog()
        val extension = log.add(at(0, 9, 40), FocusExtended(sessionId, 600, 2_100))

        val once = ClarityReplay.replay(log.events())
        val twice = ClarityReplay.replay(log.events() + extension)

        assertEquals(2_100, once.focusSessions.getValue(sessionId).plannedSeconds)
        assertEquals(
            "the payload carries the absolute figure so a duplicate delivery after a " +
                "merge folds to the same number a person was shown",
            serialize(once),
            serialize(twice),
        )
    }

    @Test
    fun `an extension that arrives after the session ended is recorded rather than applied`() {
        val log = startedLog()
        log.add(at(0, 9, 40), FocusCompleted(sessionId, 1_500))
        log.add(at(0, 9, 41), FocusExtended(sessionId, 600, 2_100))

        val folded = session(log)

        assertEquals(
            "a finished session never runs under a plan it did not have",
            1_500,
            folded.plannedSeconds,
        )
        assertEquals(FocusOutcome.COMPLETED, folded.outcome)
        assertTrue(
            "the refusal is visible rather than silent",
            fold(log).diagnostics.any { it.reason.contains(sessionId) },
        )
    }

    // Ending ------------------------------------------------------------------

    @Test
    fun `an ending under sixty seconds folds to a real short duration`() {
        val log = startedLog()
        log.add(startedAt + 42_000L, FocusEndedEarly(sessionId, 42))

        val folded = session(log)

        assertEquals(FocusOutcome.ENDED_EARLY, folded.outcome)
        assertEquals(42, folded.actualSeconds)
        assertEquals(startedAt + 42_000L, folded.endedAt)
        assertEquals("the plan it ran under is untouched by how it ended", 1_500, folded.plannedSeconds)
    }

    @Test
    fun `an ending past sixty seconds folds the same way and records the real duration`() {
        val log = startedLog()
        log.add(startedAt + 840_000L, FocusEndedEarly(sessionId, 840))

        val folded = session(log)

        assertEquals(
            "fourteen minutes is fourteen minutes, and the outcome carries no other " +
                "grade of ending than the one at forty two seconds",
            FocusOutcome.ENDED_EARLY,
            folded.outcome,
        )
        assertEquals(840, folded.actualSeconds)
    }

    @Test
    fun `an ending after an extension records what actually ran, not what was planned`() {
        val log = startedLog()
        log.add(at(0, 9, 40), FocusExtended(sessionId, 600, 2_100))
        log.add(startedAt + 1_800_000L, FocusEndedEarly(sessionId, 1_800))

        val folded = session(log)

        assertEquals(2_100, folded.plannedSeconds)
        assertEquals(1_800, folded.actualSeconds)
    }

    @Test
    fun `a natural completion after two extensions records the folded duration`() {
        val log = startedLog()
        log.add(at(0, 9, 40), FocusExtended(sessionId, 600, 2_100))
        log.add(at(0, 9, 50), FocusExtended(sessionId, 600, 2_700))
        log.add(startedAt + 2_700_000L, FocusCompleted(sessionId, 2_700))

        val folded = session(log)

        assertEquals(FocusOutcome.COMPLETED, folded.outcome)
        assertEquals(
            "the completion path reads the folded value, which is what 14b.5 means " +
                "by every later reader",
            folded.plannedSeconds,
            folded.actualSeconds,
        )
    }

    @Test
    fun `a session left running by a killed process stays running`() {
        val folded = session(startedLog())

        assertEquals(FocusOutcome.RUNNING, folded.outcome)
        assertNull(folded.endedAt)
        assertNull(
            "nothing may infer an ending from the absence of one",
            folded.actualSeconds,
        )
    }

    // Replay, with focus sessions in the stream -------------------------------

    @Test
    fun `a focus stream replays the same way in any delivery order`() {
        val log = startedLog()
        log.add(at(0, 9, 40), FocusExtended(sessionId, 600, 2_100))
        log.add(at(0, 9, 50), FocusExtended(sessionId, 600, 2_700))
        log.add(startedAt + 2_700_000L, FocusCompleted(sessionId, 2_700))
        val events: List<ClarityEvent> = log.events()

        val straight = ClarityReplay.replay(events)
        for (seed in 1L..25L) {
            val shuffled = ClarityReplay.replay(events.shuffled(Random(seed)))
            assertEquals("seed $seed", serialize(straight), serialize(shuffled))
        }
    }

    /**
     * The replay harness is only a check on focus folding if focus events reach it,
     * and nothing else in the suite asserts that they do. A generator that quietly
     * stopped emitting them would leave every property in `ReplayHarnessTest` true
     * and this phase unproved.
     */
    @Test
    fun `the generated streams the harness replays carry focus sessions and extensions`() {
        val streams = (1L..8L).map { EventStreamGenerator(it, "device-a").generate(400) }

        assertTrue(
            "no focus session in any generated stream",
            streams.any { ClarityReplay.replay(it).focusSessions.isNotEmpty() },
        )
        assertTrue(
            "no extension in any generated stream, so the fold is untested there",
            streams.any { events -> events.any { it.payload is FocusExtended } },
        )
        // And each of them still folds to one answer with focus in the mix.
        streams.forEachIndexed { index, events ->
            assertEquals(
                "stream $index",
                serialize(ClarityReplay.replay(events)),
                serialize(ClarityReplay.replay(events.shuffled(Random(index.toLong() + 1L)))),
            )
        }
    }
}
