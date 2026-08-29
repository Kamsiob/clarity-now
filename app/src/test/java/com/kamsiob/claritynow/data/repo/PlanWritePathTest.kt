package com.kamsiob.claritynow.data.repo

import com.kamsiob.claritynow.data.event.PlanAccepted
import com.kamsiob.claritynow.data.event.PlanOffered
import com.kamsiob.claritynow.domain.engine.FactRef
import com.kamsiob.claritynow.domain.guidance.PlanHistory
import com.kamsiob.claritynow.domain.query.TrailTestLog
import com.kamsiob.claritynow.domain.query.at
import com.kamsiob.claritynow.domain.replay.ClarityReplay
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The plan write path, and the two events that are the whole of it.
 * CLARITY_LOGIC_ENGINE.md 10.3, 10.5 and 10.6.
 *
 * `PLAN_OFFERED` and `PLAN_ACCEPTED` were in the catalog, the reducer folded them, the
 * Trail rendered nothing for them and `FiringHistory` read the first for 7.6's variant
 * exclusion. What was missing until phase 9b was a method on the only writer in the app,
 * for `SettingChangedTest`'s reason: `commit` is private and the phases before this one
 * correctly refused to reach around it.
 *
 * **There is no third method and there must not be.** 10.5 says declining writes nothing,
 * costs nothing, is never counted and is never referenced, and ignoring both options is
 * identical to declining. A `recordPlanDeclined` would need a `PLAN_DECLINED` to write, and
 * the absence of that event type is what makes the promise structural rather than a rule
 * somebody remembers.
 */
class PlanWritePathTest {

    // ------------------------------------------------------------------ the seam

    @Test
    fun `the only writer in the app has one method per plan event and no third`() {
        val source = repository()
        assertTrue(
            "layer 6 cannot record the plan it composed without this, and without the " +
                "record 7.6's ninety day exclusion never sees a frame, a cue or an action",
            source.contains("suspend fun recordPlanOffered("),
        )
        assertTrue(
            "accepting is one event and this is the method that writes it",
            source.contains("suspend fun acceptPlan("),
        )
        assertFalse(
            "10.5 says declining writes nothing. A method to write it would need an event " +
                "type that does not exist, and adding either is the thing this asserts against",
            source.contains("PlanDeclined") || source.contains("declinePlan("),
        )
    }

    @Test
    fun `both writes go through the one write path`() {
        val offered = body("suspend fun recordPlanOffered(")
        val accepted = body("suspend fun acceptPlan(")
        assertTrue("recordPlanOffered writes through commitLocked", "commitLocked(payload)" in offered)
        assertTrue("acceptPlan writes through commitLocked", "commitLocked(PlanAccepted(planId))" in accepted)
    }

    /**
     * Both writes refuse a second one, and both refuse it before they reach the log.
     *
     * A plan's id is derived from the week it belongs to, so a report regenerating would
     * otherwise file the same plan again, and a double tap on the pill would file a second
     * acceptance. The reducer ignores both too, which is the backstop; the guard here is
     * what keeps the log from carrying an event that changes nothing.
     */
    @Test
    fun `a second offer and a second acceptance are both refused before the log`() {
        assertTrue(
            "a regenerated report must not file the same plan twice",
            "if (existing != null) return@withLock existing" in body("suspend fun recordPlanOffered("),
        )
        assertTrue(
            "a double tap on the accept pill must not file a second acceptance",
            "if (plan.isAccepted) return@withLock plan" in body("suspend fun acceptPlan("),
        )
    }

    // ------------------------------------------------------------------ the fold

    /** An offer and an acceptance, folded, are one accepted plan. */
    @Test
    fun `an offered and accepted plan folds to an accepted plan`() {
        val log = TrailTestLog()
        log.add(at(1), offer(PLAN))
        log.add(at(1, hour = 10), PlanAccepted(PLAN))
        val state = ClarityReplay.replay(log.events())
        val plan = state.plans.getValue(PLAN)
        assertTrue("the acceptance did not reach the projection", plan.isAccepted)
        assertEquals("neglectedArea", plan.familyKey)
    }

    /**
     * An offer with no acceptance beside it is what a decline looks like in the log, and
     * `PlanHistory` must not be able to see it.
     *
     * This is 10.5 and 10.6 asserted at the layer that decides it. The plan is in the
     * projection, because the app really did offer it and 7.6 needs the keys; it is **not**
     * in the history, because the history is what the follow through reads and an unaccepted
     * plan vanishes without trace.
     */
    @Test
    fun `an unaccepted plan is in the projection and not in the history`() {
        val log = TrailTestLog()
        log.add(at(1), offer(PLAN))
        val state = ClarityReplay.replay(log.events())
        assertFalse("the offer should still be in the projection", state.plans.getValue(PLAN).isAccepted)
        assertEquals(
            "an offer nobody accepted must leave no entry for the follow through to find",
            PlanHistory.EMPTY,
            PlanHistory.from(log.queries(), Long.MAX_VALUE),
        )
    }

    /** Two offers, one accepted, and only the accepted one reaches the follow through. */
    @Test
    fun `the history holds the accepted plan and only its three keys`() {
        val log = TrailTestLog()
        log.add(at(1), offer(PLAN))
        log.add(at(1, hour = 10), PlanAccepted(PLAN))
        log.add(at(8), offer(SECOND_PLAN, week = "2026-03-15"))
        val history = PlanHistory.from(log.queries(), Long.MAX_VALUE)
        assertEquals(
            listOf(PlanHistory.Accepted("2026-03-08", "neglectedArea", "area-reading")),
            history.accepted,
        )
    }

    /** An acceptance for a plan nobody offered changes nothing and is recorded as a diagnostic. */
    @Test
    fun `an acceptance with no offer behind it is refused by the reducer`() {
        val log = TrailTestLog()
        log.add(at(1), PlanAccepted("plan-nobody-offered"))
        val state = ClarityReplay.replay(log.events())
        assertTrue("an unknown plan must not be invented by an acceptance", state.plans.isEmpty())
        assertNull(PlanHistory.from(log.queries(), Long.MAX_VALUE).accepted.firstOrNull())
    }

    // ------------------------------------------------------------------ helpers

    private fun offer(id: String, week: String = "2026-03-08") = PlanOffered(
        planId = id,
        weekStartKey = week,
        frameKey = "frm.04",
        cueKey = "cue.bound.04",
        actionKey = "act.neg.05",
        familyKey = "neglectedArea",
        subjectId = "area-reading",
        offeredLine = "Something to consider before Friday: deciding whether Reading stays or goes to the archive.",
        committedLine = "My one thing before Friday: deciding whether Reading stays or goes to the archive.",
        resolutionFactRef = FactRef("area", "areaEvents:area-reading"),
    )

    private fun repository(): String =
        File("src/main/java/com/kamsiob/claritynow/data/repo/ClarityRepository.kt").readText()

    private fun body(signature: String): String =
        repository().substringAfter(signature).substringBefore("\n    /**")

    private companion object {
        const val PLAN = "plan-2026-03-08"
        const val SECOND_PLAN = "plan-2026-03-15"
    }
}
