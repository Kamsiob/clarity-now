package com.kamsiob.claritynow.domain.engine.select

import com.kamsiob.claritynow.domain.engine.AnsweredPulse
import com.kamsiob.claritynow.domain.engine.FactSet
import com.kamsiob.claritynow.domain.engine.FamilyKey
import com.kamsiob.claritynow.domain.engine.FiringHistory
import com.kamsiob.claritynow.domain.engine.PartOfDay
import com.kamsiob.claritynow.domain.engine.SilenceReason
import com.kamsiob.claritynow.domain.engine.catalog.CorpusFixture
import com.kamsiob.claritynow.domain.engine.catalog.EngineFamilies
import com.kamsiob.claritynow.domain.engine.catalog.Purpose
import com.kamsiob.claritynow.domain.engine.realize.EngineFacts
import com.kamsiob.claritynow.domain.engine.realize.EngineMoment
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The three week pattern family cooldown, measured over a stretch of weekly reports rather
 * than asserted one call at a time. CLARITY_LOGIC_ENGINE.md 7.3.
 *
 * ## What is being measured, and why a single week could not measure it
 *
 * The facts phase counted 419 pattern slots across a simulated year, 416 of them filled,
 * and **three families holding 402**. Nothing about one week is wrong in that run: each of
 * those weeks the highest ranked pattern rule qualified and spoke, which is what layer 3 is
 * for. The defect is only visible across weeks, so the test has to be too. Every case below
 * runs [WEEKS] consecutive weekly reports over a person whose facts do not change, which
 * makes the cooldown the only thing that can decide a difference between one week and the
 * next.
 *
 * ## The arithmetic that made the old cooldown a no-op, in one paragraph
 *
 * A report covers the seven completed days before today, is **recorded** against its week
 * start key, and is **selected** against its week end. `ReportSchedule.weekAt` and
 * `ClaritySimulator.writeReport` agree on that and `FiringHistory.from` stores the week
 * start. So the gap between one report's recorded firing and the next report's selecting
 * moment is fourteen days, not seven, and a cooldown of `C` days blocks the next
 * `max(0, ceil(C / 7) - 2)` reports. Fourteen blocks none. Twenty one blocks one.
 *
 * ## What the cooldown guarantees, and what it does not
 *
 * Guaranteed, and asserted below: no `(family, subjectId)` pair appears in two consecutive
 * reports, so no pair takes more than half of a stretch.
 *
 * Not guaranteed, and also asserted below, because a property nobody wrote down is a
 * property somebody later assumes:
 *
 * - the head rotates among two pairs and no more, whatever else qualifies, because
 *   selection is deterministic and a three week cooldown moves the winner aside for exactly
 *   one report
 * - one family can still hold every week when it has two subjects to speak about
 * - a section that used to speak can fall silent, because a cooldown only ever removes
 *
 * The last two are costs rather than defects and both are stated in
 * [Selector.PATTERN_COOLDOWN_DAYS]. They are here so that a later change to the number
 * moves a measurement rather than a belief.
 */
class PatternCooldownTest {

    private val selector = Selector(CorpusFixture.catalog)

    // ------------------------------------------------------------------ the instrument

    /** One weekly report: which pair took the pattern section, or why nothing did. */
    private data class Slot(val pair: Pair<FamilyKey, String?>?, val silence: SilenceReason?)

    /**
     * [weeks] consecutive weekly reports over an unchanging [facts], history carried across.
     *
     * The head of the ranking is what is recorded, which is what the composer does with it:
     * layers 4 and 5 can veto the head and fall through to the next, and that is a test of
     * the realizer and the validator rather than of this filter.
     *
     * The firing is recorded against the report's **week start**, seven days behind the
     * moment that selected it, because that is what `FiringHistory.from` reads off a
     * `REPORT_GENERATED` payload. Recording it against the moment instead would make every
     * assertion here pass for a reason the app does not have.
     */
    private fun stretch(facts: FactSet, weeks: Int = WEEKS): List<Slot> {
        var history = FiringHistory.EMPTY
        val slots = mutableListOf<Slot>()
        for (week in 0 until weeks) {
            val today = FIRST_REPORT_DAY + week * DAYS_PER_WEEK
            val moment = EngineMoment(EngineFacts.dateKey(today), PartOfDay.MORNING)
            when (val outcome = selector.select(Purpose.REPORT_PATTERN, facts, history, moment)) {
                is SelectionOutcome.Silent -> slots += Slot(pair = null, silence = outcome.reason)
                is SelectionOutcome.Ranked -> {
                    val head = outcome.selections.first()
                    val pair = head.rule.family to head.subjectId
                    slots += Slot(pair = pair, silence = null)
                    history = history.copy(
                        lastFiredBySubject = history.lastFiredBySubject +
                            (pair to EngineFacts.dateKey(today - DAYS_PER_WEEK)),
                    )
                }
            }
        }
        return slots
    }

    /** Every pair that would qualify on an empty history, which is the bench the cooldown draws from. */
    private fun qualifyingPairs(facts: FactSet): List<Pair<FamilyKey, String?>> {
        val moment = EngineMoment(EngineFacts.dateKey(FIRST_REPORT_DAY), PartOfDay.MORNING)
        val outcome = selector.select(Purpose.REPORT_PATTERN, facts, FiringHistory.EMPTY, moment)
        return (outcome as? SelectionOutcome.Ranked)?.selections.orEmpty().map { it.rule.family to it.subjectId }
    }

    // ------------------------------------------------------------------ the defect

    @Test
    fun `the flat Report cooldown cannot hold a family out of the next weekly report`() {
        val spokeLastWeek = FiringHistory.EMPTY.copy(
            lastFiredBySubject = mapOf<Pair<FamilyKey, String?>, String>(
                ("consistentRhythm" to null) to EngineFacts.dateKey(0),
            ),
        )
        val nextReport = EngineFacts.dateKey(DAYS_PER_WEEK * 2)

        assertTrue(
            "a report keyed to day 0 and the next report selecting on day 14 are two weeks " +
                "apart, because a report is recorded at its week start and selected at its week end",
            spokeLastWeek.daysSinceFiring("consistentRhythm", null, nextReport) == DAYS_PER_WEEK * 2,
        )
        assertFalse(
            "the flat fourteen day Report cooldown is a no-op at the weekly cadence, which is " +
                "the whole of the starvation the facts phase measured",
            spokeLastWeek.inCooldown(
                "consistentRhythm", null, nextReport, EngineFamilies.REPORT_DEFAULT_COOLDOWN_DAYS,
            ),
        )
        assertTrue(
            "three weeks is the shortest cooldown that reaches the next weekly report at all",
            spokeLastWeek.inCooldown(
                "consistentRhythm", null, nextReport, Selector.PATTERN_COOLDOWN_DAYS,
            ),
        )
    }

    // ------------------------------------------------------------------ what it guarantees

    @Test
    fun `no pattern pair speaks in two consecutive reports`() {
        val slots = stretch(steadyWeek())
        assertTrue(
            "this fixture has to fill every slot or the property below is vacuous",
            slots.all { it.pair != null },
        )
        for ((first, second) in slots.zipWithNext()) {
            assertFalse(
                "${first.pair} held two reports running, which is the shape the cooldown exists to break",
                first.pair == second.pair,
            )
        }
    }

    @Test
    fun `no pattern pair takes more than half of a stretch`() {
        val counts = stretch(steadyWeek()).mapNotNull { it.pair }.groupingBy { it }.eachCount()
        val cap = (WEEKS + 1) / 2
        val over = counts.filterValues { it > cap }
        assertTrue(
            "a pair blocked for the next report can take every other one and no more: $over",
            over.isEmpty(),
        )
        assertTrue("the stretch produced nothing to count", counts.isNotEmpty())
    }

    // ------------------------------------------------------------------ what it does not

    /**
     * The measurement the owner's ruling is worth on its own, stated as a number.
     *
     * Six pairs qualify in this fixture every single week and two of them speak. That is
     * the honest ceiling of a cooldown against a deterministic ranking: three weeks moves
     * the winner aside for exactly one report, so the second ranked pair fills that report
     * and the two of them alternate forever. It is a real improvement on one pair holding
     * every week and it is not, by itself, seven families getting a turn.
     */
    @Test
    fun `three weeks rotates two pairs, however many qualify`() {
        val facts = steadyWeek()
        assertTrue(
            "the fixture needs more than two qualifying pairs or the ceiling is not being measured",
            qualifyingPairs(facts).size > 2,
        )
        assertEquals(
            "the head rotates among ceil(cooldown / 7) - 1 pairs, and the rest lose every week",
            Selector.PATTERN_COOLDOWN_DAYS / DAYS_PER_WEEK - 1,
            stretch(facts).mapNotNull { it.pair }.distinct().size,
        )
    }

    @Test
    fun `a family with two subjects still holds every week, because the key is per subject`() {
        val slots = stretch(twoQuietAreas())
        assertTrue(
            "areaGoneQuiet is the only family qualifying here, so it should hold every week",
            slots.all { it.pair?.first == "areaGoneQuiet" },
        )
        assertEquals(
            "it holds them by alternating its two subjects, exactly as 7.3 keys the Pulse",
            2,
            slots.mapNotNull { it.pair?.second }.distinct().size,
        )
    }

    @Test
    fun `where one pair qualifies the cooldown buys variety with silence`() {
        val slots = stretch(oneQualifier())
        assertEquals(
            "a cooldown removes candidates and never adds one, so every other week the " +
                "pattern section says nothing where it used to speak",
            WEEKS / 2,
            slots.count { it.silence != null },
        )
        assertTrue(
            "the silence is a filtered candidate and not a week with nothing to describe, " +
                "which is the distinction section 5 keeps",
            slots.all { it.silence == null || it.silence == SilenceReason.ALL_QUALIFIED_RULES_FILTERED },
        )
    }

    // ------------------------------------------------------------------ fixtures

    /**
     * A person whose week keeps its shape: six pattern pairs qualify, every week, forever.
     *
     * Built to hold six of the specificity three pattern families at once, so the ranking
     * has somewhere to go and any rotation observed is the cooldown's doing rather than a
     * thin bench. `areaGoneQuiet` supplies the one subject bearing pair.
     */
    private fun steadyWeek(): FactSet {
        val work = EngineFacts.area(
            areaId = "work", name = "Work", events = 8, completions = 4, additions = 4, share = 0.67,
            queueLength = 5, queueLengthAtWindowStart = 5,
        )
        val admin = EngineFacts.area(
            areaId = "admin", name = "Admin", events = 4, completions = 1, additions = 3, share = 0.33,
        )
        val reading = EngineFacts.area(
            areaId = "reading", name = "Reading", queueLength = 2, queueLengthAtWindowStart = 2,
            daysSinceLastEvent = 25, lifetimeEvents = 30, ageDays = 300,
        )
        return EngineFacts.factSet(
            window = EngineFacts.window(
                startDay = 0, endDay = 7, totalEvents = 12, completions = 5, additions = 7, activeDays = 5,
            ),
            areas = listOf(work, admin, reading),
            dominantAreaId = "work",
            history = EngineFacts.history(
                daysSinceInstall = 400,
                // consistentRhythm, and weekendShift's second criterion.
                weekTotalEvents = listOf(13, 12, 13, 12),
                // queueEquilibrium, over the seven queued items the three areas hold.
                weekQueueSizes = listOf(7, 7, 6, 7),
                // narrowingFocus, over the three areas this person has.
                weekAreaCounts = listOf(3, 2, 1),
                // weekendShift.
                weekWeekendEvents = listOf(0, 0, 0, 0),
            ),
            // reportedVsActual.
            pulse = EngineFacts.pulse(answeredLifetime = 9, recentAnswers = threeAnswers()),
        )
    }

    /** Two areas that have been still for three weeks, and no other pattern in sight. */
    private fun twoQuietAreas(): FactSet {
        val work = EngineFacts.area(
            areaId = "work", name = "Work", events = 12, completions = 5, additions = 7, share = 1.0,
        )
        fun quiet(areaId: String, name: String) = EngineFacts.area(
            areaId = areaId, name = name, daysSinceLastEvent = 25, lifetimeEvents = 30, ageDays = 300,
        )
        return EngineFacts.factSet(
            window = EngineFacts.window(
                startDay = 0, endDay = 7, totalEvents = 12, completions = 5, additions = 7, activeDays = 5,
            ),
            areas = listOf(work, quiet("garden", "Garden"), quiet("reading", "Reading")),
            dominantAreaId = "work",
            history = EngineFacts.history(daysSinceInstall = 400),
        )
    }

    /** Stored answers and nothing else, so `reportedVsActual` is the only pair on the bench. */
    private fun oneQualifier(): FactSet = EngineFacts.factSet(
        window = EngineFacts.window(
            startDay = 0, endDay = 7, totalEvents = 12, completions = 5, additions = 7, activeDays = 5,
        ),
        areas = listOf(
            EngineFacts.area(
                areaId = "work", name = "Work", events = 12, completions = 5, additions = 7, share = 1.0,
            ),
        ),
        dominantAreaId = "work",
        history = EngineFacts.history(daysSinceInstall = 400),
        pulse = EngineFacts.pulse(answeredLifetime = 9, recentAnswers = threeAnswers()),
    )

    /** Three answers about one family, which is what `reportedVsActual` compares against. */
    private fun threeAnswers(): List<AnsweredPulse> = (1..3).map { index ->
        AnsweredPulse(
            dateKey = EngineFacts.dateKey(index),
            family = "persistence",
            subjectId = "item-1",
            responseKey = "persistence.s2.r0$index.1",
            responseLabel = "Deep work",
            isPositive = true,
        )
    }

    private companion object {
        /** Half a year of weekly reports, which is long enough for any rotation to settle. */
        const val WEEKS = 26

        /** The day the first report is composed on. Its window is the seven days before it. */
        const val FIRST_REPORT_DAY = 7

        const val DAYS_PER_WEEK = 7
    }
}
