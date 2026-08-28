package com.kamsiob.claritynow.domain.engine.select

import com.kamsiob.claritynow.domain.engine.AnsweredPulse
import com.kamsiob.claritynow.domain.engine.FactSet
import com.kamsiob.claritynow.domain.engine.FiringHistory
import com.kamsiob.claritynow.domain.engine.PartOfDay
import com.kamsiob.claritynow.domain.engine.SilenceReason
import com.kamsiob.claritynow.domain.engine.catalog.CorpusFixture
import com.kamsiob.claritynow.domain.engine.catalog.Purpose
import com.kamsiob.claritynow.domain.engine.realize.EngineFacts
import com.kamsiob.claritynow.domain.engine.realize.EngineMoment
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Layer 3, in the order section 5 states. CLARITY_LOGIC_ENGINE.md 5.
 *
 * Each filter is tested by building the one fact set it acts on and asserting what the
 * ranked list holds, rather than by asserting which sentence came out. A test that reads
 * the sentence is a test of four layers, and when it fails nobody knows which one moved.
 */
class SelectorTest {

    private val catalog = CorpusFixture.catalog

    private val selector = Selector(catalog)

    private val moment = EngineMoment(EngineFacts.dateKey(10), PartOfDay.MORNING)

    private fun ranked(
        purpose: Purpose,
        facts: FactSet,
        history: FiringHistory = FiringHistory.EMPTY,
        at: EngineMoment = moment,
    ): List<Selection> =
        (selector.select(purpose, facts, history, at) as? SelectionOutcome.Ranked)?.selections.orEmpty()

    private fun silence(
        purpose: Purpose,
        facts: FactSet,
        history: FiringHistory = FiringHistory.EMPTY,
        at: EngineMoment = moment,
    ): SilenceReason? =
        (selector.select(purpose, facts, history, at) as? SelectionOutcome.Silent)?.reason

    // ------------------------------------------------------------------ fixtures

    /** One nine day old item in Work, and a second area, so nothing else qualifies. */
    private fun persistenceOnly(): FactSet {
        val work = EngineFacts.area(
            areaId = "work", name = "Work", events = 3, share = 0.6,
            activeItemId = "item-1", activeItemTitle = "Rewrite the proposal intro", activeItemAgeDays = 9,
        )
        val health = EngineFacts.area(areaId = "health", name = "Health", events = 2, share = 0.4)
        return EngineFacts.factSet(
            window = EngineFacts.window(totalEvents = 5, activeDays = 1),
            areas = listOf(work, health),
            dominantAreaId = "work",
        )
    }

    // ------------------------------------------------------------------ the steps

    @Test
    fun `step 1 evaluates every rule against every subject its selector yields`() {
        val selections = ranked(Purpose.PULSE, persistenceOnly())
        assertTrue(selections.any { it.rule.family == "persistence" && it.subjectId == "item-1" })
        assertTrue("a rule only ever qualifies for a subject it was evaluated against", selections.all {
            it.subject == null || it.subjectId != null
        })
    }

    @Test
    fun `step 6 ranks by specificity, then priority, then key`() {
        val selections = ranked(Purpose.REPORT_OBSERVATION, busyWeek())
        assertTrue("this fixture needs more than one observation to rank", selections.size > 1)
        assertEquals(selections.sortedWith(Selector.RANKING), selections)
        for ((first, second) in selections.zipWithNext()) {
            assertTrue(
                "${first.rule.key} ranked above ${second.rule.key} out of order",
                first.specificity > second.specificity ||
                    (first.specificity == second.specificity && first.rule.priority > second.rule.priority) ||
                    (
                        first.specificity == second.specificity &&
                            first.rule.priority == second.rule.priority &&
                            first.rule.key <= second.rule.key
                        ),
            )
        }
    }

    @Test
    fun `step 4 drops yesterday's family, and only for the Pulse`() {
        val facts = persistenceOnly()
        assertTrue(ranked(Purpose.PULSE, facts).any { it.rule.family == "persistence" })

        val afterPersistence = EngineFacts.factSet(
            window = facts.window,
            areas = facts.areas.values.toList(),
            dominantAreaId = "work",
            pulse = EngineFacts.pulse(lastGeneratedFamily = "persistence", lastGeneratedDateKey = EngineFacts.dateKey(9)),
        )
        assertFalse(ranked(Purpose.PULSE, afterPersistence).any { it.rule.family == "persistence" })
    }

    @Test
    fun `step 5 drops a family and subject pair that fired inside its cooldown`() {
        val facts = persistenceOnly()
        val history = FiringHistory(
            variantsUsed = emptyMap(),
            lastStageBySubject = emptyMap(),
            lastFiredBySubject = mapOf(("persistence" to "item-1") to EngineFacts.dateKey(9)),
            lastPulseFamily = "persistence",
        )
        assertFalse(
            "persistence declares a cooldown of three days",
            ranked(Purpose.PULSE, facts, history).any { it.rule.family == "persistence" },
        )
    }

    @Test
    fun `a cooldown is per subject, so another item keeps its own ladder`() {
        val work = EngineFacts.area(
            areaId = "work", name = "Work", events = 3, share = 0.5,
            activeItemId = "item-1", activeItemTitle = "Rewrite the proposal intro", activeItemAgeDays = 9,
        )
        val health = EngineFacts.area(
            areaId = "health", name = "Health", events = 3, share = 0.5,
            activeItemId = "item-2", activeItemTitle = "Book the eye test", activeItemAgeDays = 7,
        )
        val facts = EngineFacts.factSet(
            window = EngineFacts.window(totalEvents = 6, activeDays = 1),
            areas = listOf(work, health),
        )
        val history = FiringHistory(
            variantsUsed = emptyMap(),
            lastStageBySubject = emptyMap(),
            lastFiredBySubject = mapOf(("persistence" to "item-1") to EngineFacts.dateKey(9)),
            lastPulseFamily = null,
        )
        val subjects = ranked(Purpose.PULSE, facts, history).filter { it.rule.family == "persistence" }.map { it.subjectId }
        assertFalse(subjects.contains("item-1"))
        assertTrue(subjects.contains("item-2"))
    }

    @Test
    fun `step 3 drops a pair whose oldest fact is older than the rule declares`() {
        // neglectedArea declares ninety days. An area still for four hundred is exactly the
        // callback section 4 calls uncanny.
        val stale = EngineFacts.area(
            areaId = "personal", name = "Personal", ageDays = 800, lifetimeEvents = 40,
            queueLength = 2, daysSinceLastEvent = 400,
        )
        val work = EngineFacts.area(areaId = "work", name = "Work", events = 9, share = 1.0)
        val facts = EngineFacts.factSet(
            window = EngineFacts.window(startDay = 0, endDay = 7, totalEvents = 9, completions = 4),
            areas = listOf(stale, work),
            dominantAreaId = "work",
            rollup = EngineFacts.rollup(
                mapOf("personal" to stale, "work" to work),
                dominantAreaId = "work",
                neglected = listOf("personal"),
            ),
        )
        assertFalse(
            "an area still for four hundred days is outside neglectedArea's ninety day horizon",
            ranked(Purpose.REPORT_OBSERVATION, facts).any { it.rule.family == "neglectedArea" },
        )

        // The same shape inside the horizon still speaks, or the filter above would be
        // passing for the wrong reason.
        val recent = EngineFacts.area(
            areaId = "personal", name = "Personal", ageDays = 60, lifetimeEvents = 40,
            queueLength = 2, daysSinceLastEvent = 20,
        )
        val inside = EngineFacts.factSet(
            window = EngineFacts.window(startDay = 0, endDay = 7, totalEvents = 9, completions = 4),
            areas = listOf(recent, work),
            rollup = EngineFacts.rollup(
                mapOf("personal" to recent, "work" to work),
                dominantAreaId = "work",
                neglected = listOf("personal"),
            ),
        )
        assertTrue(ranked(Purpose.REPORT_OBSERVATION, inside).any { it.rule.family == "neglectedArea" })
    }

    @Test
    fun `step 2 drops a rule whose callback cannot be resolved, and attaches it when it can`() {
        val facts = selfReport(answered = false)
        assertFalse(ranked(Purpose.REPORT_OBSERVATION, facts).any { it.rule.family == "selfReportVsData" })

        val answered = ranked(Purpose.REPORT_OBSERVATION, selfReport(answered = true))
            .firstOrNull { it.rule.family == "selfReportVsData" }
        assertNotNull("a stored answer about the same item should resolve the callback", answered)
        assertEquals("Deep work", answered?.callbackLabel)
        assertEquals("item-1", answered?.callback?.answer?.subjectId)
    }

    @Test
    fun `a callback quotes the label as it was stored, not as the app would word it today`() {
        val answered = ranked(Purpose.REPORT_OBSERVATION, selfReport(answered = true))
            .first { it.rule.family == "selfReportVsData" }
        assertEquals(
            "the label travels from the event, verbatim, or validator check 6 has nothing to compare",
            "Deep work",
            answered.callback?.answer?.responseLabel,
        )
    }

    // ------------------------------------------------------------------ silence

    @Test
    fun `an app with nothing in it says so, and it is not the same as nothing qualifying`() {
        val empty = EngineFacts.factSet(history = EngineFacts.history(daysSinceInstall = 0, lifetimeCompletions = 0))
        assertEquals(SilenceReason.INSUFFICIENT_DATA, silence(Purpose.PULSE, empty))
    }

    @Test
    fun `a week where nothing qualified is told apart from one where everything was filtered`() {
        val nothing = EngineFacts.factSet(
            window = EngineFacts.window(totalEvents = 3, promotions = 3, activeDays = 1),
            areas = listOf(EngineFacts.area("work", "Work", events = 3, share = 1.0)),
            dominantAreaId = "work",
        )
        assertEquals(SilenceReason.NO_RULE_QUALIFIED, silence(Purpose.PULSE, nothing))

        val filtered = EngineFacts.factSet(
            window = persistenceOnly().window,
            areas = persistenceOnly().areas.values.toList(),
            dominantAreaId = "work",
            pulse = EngineFacts.pulse(lastGeneratedFamily = "persistence"),
        )
        assertEquals(SilenceReason.ALL_QUALIFIED_RULES_FILTERED, silence(Purpose.PULSE, filtered))
    }

    @Test
    fun `the pattern section is silent for want of weeks rather than for want of a rule`() {
        fun weeksOld(days: Int) = EngineFacts.factSet(
            window = EngineFacts.window(startDay = 0, endDay = 7, totalEvents = 9, completions = 4),
            areas = listOf(EngineFacts.area("work", "Work", events = 9, share = 1.0)),
            dominantAreaId = "work",
            history = EngineFacts.history(daysSinceInstall = days),
        )
        assertEquals(SilenceReason.INSUFFICIENT_DATA, silence(Purpose.REPORT_PATTERN, weeksOld(3)))

        // What used to be asserted here is that `insufficientData` was ranked once there was
        // a week to look at. That rule is gone, and its absence is the point. It could never
        // have fired: `ReportComposer` asks the engine for a pattern only when there are
        // three weeks of snapshots and the rule required fewer, so the two conditions were
        // complements. The owner's ruling is that it was never a pattern, it is the pattern
        // section's empty state, and `ReportComposer.patternNote` renders its line directly.
        // A rule left behind beside that render would be a second source for one line.
        assertFalse(
            "insufficientData is the section's empty state, rendered by ReportComposer, and " +
                "selection must not carry a rule for it",
            catalog.rules.any { it.family == "insufficientData" },
        )
    }

    /**
     * 5.1, and the reason it currently never fires.
     *
     * Deliberate silence is reached only when the best thing available is a single bare
     * condition. **Every Pulse rule in the catalog carries at least two criteria**, because
     * every one of them carries a floor that keeps a share or a count honest, so the
     * highest available specificity is never 1 and this branch is unreachable today. That
     * is a finding rather than a defect in either place, and it is recorded here so that
     * adding a one criterion rule, or amending 5.1, changes a test rather than changing
     * behavior quietly.
     */
    @Test
    fun `deliberate silence cannot suppress anything more specific than a bare condition`() {
        assertTrue(
            "a Pulse rule with one criterion would make 5.1 reachable, and would also be a " +
                "rule with no floor under whatever it reads",
            catalog.rulesFor(Purpose.PULSE).all { it.specificity >= 2 },
        )
        for (day in 0 until 60) {
            val at = EngineMoment(EngineFacts.dateKey(day), PartOfDay.MORNING)
            val reason = silence(Purpose.PULSE, persistenceOnly(), at = at)
            assertNull(
                "deliberate silence must never suppress a specificity 2 or higher observation",
                reason?.takeIf { it == SilenceReason.DELIBERATE_SILENCE },
            )
        }
    }

    // ------------------------------------------------------------------ observations

    @Test
    fun `observations never repeat a family and never pad to reach a minimum`() {
        val chosen = selector.selectObservations(busyWeek(), FiringHistory.EMPTY, moment)
        assertTrue("at most four, per section 5", chosen.size <= 4)
        assertEquals("one family speaks once", chosen.map { it.rule.family }.distinct().size, chosen.size)

        val thin = EngineFacts.factSet(
            window = EngineFacts.window(startDay = 0, endDay = 7, totalEvents = 3, completions = 3, activeDays = 2),
            areas = listOf(EngineFacts.area("work", "Work", events = 3, completions = 3, share = 1.0)),
            dominantAreaId = "work",
        )
        assertTrue(
            "a thin week is allowed to produce fewer than two observations rather than reach for one",
            selector.selectObservations(thin, FiringHistory.EMPTY, moment).size <= 4,
        )
    }

    @Test
    fun `a headline excludes the observations that argue with it`() {
        val facts = busyWeek()
        val unconstrained = selector.selectObservations(facts, FiringHistory.EMPTY, moment)
        assertTrue("this fixture needs observations for the headline to exclude", unconstrained.isNotEmpty())

        val underQuiet = selector.selectObservations(facts, FiringHistory.EMPTY, moment, headlineFamily = "quietWeek")
        assertTrue(
            "a quietWeek headline sets the frame and leaves room for four families only",
            underQuiet.all { it.rule.family in setOf("quietWeek", "neglectedArea", "persistentItem", "hardStretch") },
        )
        assertTrue(
            "the headline is a constraint, not a preference: a conflicting observation is " +
                "excluded entirely rather than deprioritized",
            underQuiet.size < unconstrained.size,
        )
    }

    @Test
    fun `the incompatibility matrix is symmetric and never conflicts a family with itself`() {
        val families = catalog.familiesFor(Purpose.REPORT_OBSERVATION).map { it.key }
        val rules = catalog.rulesFor(Purpose.REPORT_OBSERVATION)
        for (first in families) {
            for (second in families) {
                val a = rules.firstOrNull { it.family == first } ?: continue
                val b = rules.firstOrNull { it.family == second } ?: continue
                val left = Selection(a, Purpose.REPORT_OBSERVATION, null, null, 7)
                val right = Selection(b, Purpose.REPORT_OBSERVATION, null, null, 7)
                assertEquals(
                    IncompatibilityMatrix.conflicts(left, right),
                    IncompatibilityMatrix.conflicts(right, left),
                )
                if (first == second) assertFalse(IncompatibilityMatrix.conflicts(left, right))
            }
        }
    }

    // ------------------------------------------------------------------ shared fixtures

    private fun busyWeek(): FactSet {
        val work = EngineFacts.area(
            areaId = "work", name = "Work", events = 12, completions = 5, additions = 7, share = 0.6,
            activeItemId = "item-1", activeItemTitle = "Rewrite the proposal intro", activeItemAgeDays = 16,
            queueLength = 4, queueLengthAtWindowStart = 2, focusSessions = 2, focusMinutes = 50,
        )
        val health = EngineFacts.area(
            areaId = "health", name = "Health", events = 8, completions = 3, additions = 5, share = 0.4,
            queueLength = 2, queueLengthAtWindowStart = 2,
        )
        return EngineFacts.factSet(
            window = EngineFacts.window(
                startDay = 0, endDay = 7, totalEvents = 20, completions = 8, additions = 12,
                swaps = 2, focusStarted = 4, focusCompleted = 2, focusEndedEarly = 2, focusMinutes = 50,
                activeDays = 6, busiestDayKey = EngineFacts.dateKey(3), busiestDayCount = 8,
            ),
            areas = listOf(work, health),
            dominantAreaId = "work",
            history = EngineFacts.history(
                daysSinceInstall = 120,
                lastWeekCompletions = 6,
                weekCompletions = listOf(4, 5, 6, 8),
                weekQueueSizes = listOf(3, 4, 5, 6),
                weekTotalEvents = listOf(12, 15, 18, 20),
                dominantAreaLastThreeWeeks = listOf("health", "work", "work"),
                personalBestWeekCompletions = 7,
                personalBestWeekKey = "2026-01-04",
                weeksSincePersonalBest = 8,
            ),
        )
    }

    private fun selfReport(answered: Boolean): FactSet {
        val work = EngineFacts.area(
            areaId = "work", name = "Work", events = 9, completions = 3, share = 1.0,
            activeItemId = "item-1", activeItemTitle = "Rewrite the proposal intro", activeItemAgeDays = 12,
        )
        val answers = if (!answered) {
            emptyList()
        } else {
            listOf(
                AnsweredPulse(
                    dateKey = EngineFacts.dateKey(4),
                    family = "persistence",
                    subjectId = "item-1",
                    responseKey = "persistence.s2.r01.1",
                    responseLabel = "Deep work",
                    isPositive = true,
                ),
            )
        }
        return EngineFacts.factSet(
            window = EngineFacts.window(startDay = 0, endDay = 7, totalEvents = 9, completions = 3, activeDays = 5),
            areas = listOf(work),
            dominantAreaId = "work",
            pulse = EngineFacts.pulse(
                // Lifetime answers either way, so the only thing that changes between the
                // two fact sets is whether the callback can be resolved.
                answeredLifetime = 6,
                answeredInWindow = if (answered) 1 else 0,
                positiveInWindow = if (answered) 1 else 0,
                recentAnswers = answers,
            ),
        )
    }
}
