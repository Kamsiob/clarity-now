package com.kamsiob.claritynow.domain.engine.realize

import com.kamsiob.claritynow.domain.engine.AnsweredPulse
import com.kamsiob.claritynow.domain.engine.catalog.CorpusFixture
import com.kamsiob.claritynow.domain.engine.catalog.Purpose
import com.kamsiob.claritynow.domain.engine.catalog.SlotProduction
import com.kamsiob.claritynow.domain.engine.catalog.Subject
import com.kamsiob.claritynow.domain.engine.catalog.SubjectKind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The binding table against the corpus it was written from. CLARITY_LOGIC_ENGINE.md 7.2.
 *
 * A binding table is data, and data rots quietly. A key with a typo in it binds nothing and
 * looks exactly like a key that was never needed; an exclusion for a line that has since
 * been renamed protects nothing. Everything here exists to make that visible.
 */
class SlotBindingsTest {

    private val catalog = CorpusFixture.catalog

    private val variantKeys = catalog.allVariants.map { it.key }.toSet()

    @Test
    fun `every override and every exclusion names a line that exists`() {
        val unknown = (SlotBindings.OVERRIDES.keys + SlotBindings.EXCLUDED.keys) - variantKeys
        assertTrue("a binding for a line that is not in any corpus file: $unknown", unknown.isEmpty())
    }

    @Test
    fun `no line is both overridden and excluded`() {
        val both = SlotBindings.OVERRIDES.keys intersect SlotBindings.EXCLUDED.keys
        assertTrue("$both is bound and taken away at once", both.isEmpty())
    }

    @Test
    fun `every exclusion carries the reason it was excluded`() {
        for ((key, reason) in SlotBindings.EXCLUDED) {
            assertTrue("$key is excluded with no reason anyone could act on", reason.length > 30)
        }
    }

    @Test
    fun `every binding names a measure the engine has`() {
        for ((purpose, family) in SlotBindings.DECLARED_FAMILIES) {
            for (variant in catalog.families.filter { it.purpose == purpose && it.key == family }.flatMap { it.allVariants }) {
                val bindings = SlotBindings.bindingsFor(purpose, family, variant.stage, variant.key)
                for ((slot, binding) in bindings) {
                    assertNotNull(
                        "$purpose $family binds {$slot} to ${binding.measure}, which is not a measure",
                        Measures.byId(binding.measure),
                    )
                }
            }
        }
    }

    @Test
    fun `every bound slot is one the corpus and the production table both declare`() {
        for ((purpose, family) in SlotBindings.DECLARED_FAMILIES) {
            for (variant in catalog.families.filter { it.purpose == purpose && it.key == family }.flatMap { it.allVariants }) {
                for (slot in SlotBindings.bindingsFor(purpose, family, variant.stage, variant.key).keys) {
                    assertTrue(
                        "{$slot} is bound for $purpose $family and SlotProduction does not declare it",
                        slot in SlotProduction.DECLARED,
                    )
                }
            }
        }
    }

    @Test
    fun `a binding that reads the subject asks for the kind of subject its rules yield`() {
        val facts = EngineFacts.factSet(
            areas = listOf(
                EngineFacts.area(
                    "work", "Work", events = 4, share = 1.0,
                    activeItemId = "item-1", activeItemTitle = "Rewrite the proposal intro", activeItemAgeDays = 9,
                ),
            ),
            dominantAreaId = "work",
        )
        for ((purpose, family) in SlotBindings.DECLARED_FAMILIES) {
            val kinds = catalog.rulesOf(purpose, family)
                .flatMap { rule -> rule.subject.select(facts) }
                .mapNotNull { it?.kind }
                .toSet()
            if (kinds.isEmpty()) continue
            for (variant in catalog.families.filter { it.purpose == purpose && it.key == family }.flatMap { it.allVariants }) {
                val bindings = SlotBindings.bindingsFor(purpose, family, variant.stage, variant.key)
                for ((slot, binding) in bindings) {
                    if (binding.entity != SlotBindings.EntitySource.SUBJECT) continue
                    val needed = SlotBindings.subjectKindFor(binding) ?: continue
                    assertTrue(
                        "$purpose $family binds {$slot} to a $needed measure and its rules yield $kinds",
                        needed in kinds,
                    )
                }
            }
        }
    }

    @Test
    fun `the families that carry no binding are the ones with nothing to fill`() {
        // A family with markers in its lines and no binding at all would be silent for a
        // reason nobody could see. A family with no markers needs no binding, and there are
        // several: the Report headlines are mostly bare, and hardStretch is bare by design,
        // because 6.4 makes the pattern the grammatical subject and a number would make it
        // the person.
        val unbound = catalog.families
            .filter { family -> family.allVariants.any { it.statement.slots.isNotEmpty() } }
            .filter { (it.purpose to it.key) !in SlotBindings.DECLARED_FAMILIES }
            .filter { catalog.rulesOf(it.purpose, it.key).isNotEmpty() }
            .map { "${it.purpose} ${it.key}" }
            .toSet()
        assertEquals(
            "a family with a rule, markers in its lines and no binding can only ever speak " +
                "through the lines that happen to have no marker in them",
            KNOWN_UNBOUND,
            unbound,
        )
    }

    @Test
    fun `every family that binds something can fill at least one of its lines`() {
        val facts = richWeek()
        val unfillable = mutableListOf<String>()
        for ((purpose, family) in SlotBindings.DECLARED_FAMILIES) {
            val variants = catalog.families
                .filter { it.purpose == purpose && it.key == family }
                .flatMap { it.allVariants }
                .filter { it.statement.slots.isNotEmpty() }
            if (variants.isEmpty()) continue
            val fillable = variants.any { variant ->
                val bindings = SlotBindings.bindingsFor(purpose, family, variant.stage, variant.key)
                variant.statement.slots.all { slot ->
                    val binding = bindings[slot] ?: return@all false
                    val measure = Measures.byId(binding.measure) ?: return@all false
                    val entity = SlotBindings.resolveEntity(binding, facts, subjectFor(purpose, family), null)
                    measure.read(facts, entity, EngineFacts.ZONE) != null
                }
            }
            if (!fillable) unfillable += "$purpose $family"
        }
        assertTrue(
            "these families bind slots and could not fill a single line from a week with " +
                "something in every corner: $unfillable",
            unfillable.isEmpty(),
        )
    }

    private fun subjectFor(purpose: Purpose, family: String) = catalog.rulesOf(purpose, family)
        .firstNotNullOfOrNull { rule -> rule.subject.select(richWeek()).firstOrNull { it != null } }
        ?: when {
            family == "persistentItem" || family == "selfReportVsData" -> Subject("item-1", SubjectKind.ITEM)
            else -> Subject("work", SubjectKind.AREA)
        }

    private fun richWeek() = EngineFacts.factSet(
        window = EngineFacts.window(
            startDay = 0, endDay = 7, totalEvents = 20, completions = 8, additions = 12,
            swaps = 2, focusStarted = 4, focusCompleted = 2, focusEndedEarly = 2, focusMinutes = 50,
            activeDays = 6, busiestDayKey = EngineFacts.dateKey(3), busiestDayCount = 8,
        ),
        areas = RICH_AREAS,
        rollup = EngineFacts.rollup(
            RICH_AREAS.associateBy { it.areaId },
            dominantAreaId = "work",
            dormantReturned = listOf("home"),
            queueDrained = listOf("home"),
            queueGrowing = listOf("work"),
        ),
        history = EngineFacts.history(
            daysSinceInstall = 200,
            lastWeekCompletions = 6,
            weekCompletions = listOf(4, 5, 6, 8),
            weekQueueSizes = listOf(3, 4, 5, 6),
            weekTotalEvents = listOf(12, 15, 18, 20),
            dominantAreaLastThreeWeeks = listOf("health", "work", "work"),
            personalBestWeekCompletions = 7,
            personalBestWeekKey = "2026-01-04",
            weeksSincePersonalBest = 8,
            mostRecentBetterWeekKey = "2026-01-11",
            personalBestFocusMinutesWeek = 90,
        ),
        pulse = EngineFacts.pulse(
            answeredLifetime = 9,
            answeredInWindow = 3,
            positiveInWindow = 2,
            flaggedInWindow = 1,
            recentAnswers = listOf(
                AnsweredPulse(
                    EngineFacts.dateKey(2), "persistence", "item-1", "persistence.s2.r01.1", "Deep work", true,
                ),
            ),
        ),
    )

    private companion object {

        /** Three areas: one busy, one steady, one that emptied its queue this week. */
        val RICH_AREAS = listOf(
            EngineFacts.area(
                "work", "Work", events = 12, completions = 5, additions = 7, share = 0.6,
                activeItemId = "item-1", activeItemTitle = "Rewrite the proposal intro",
                activeItemAgeDays = 16, queueLength = 4, queueLengthAtWindowStart = 2,
                daysSinceLastEvent = 1, focusSessions = 2, focusMinutes = 50,
            ),
            EngineFacts.area(
                "health", "Health", events = 8, completions = 3, additions = 5, share = 0.3,
                queueLength = 2, queueLengthAtWindowStart = 1, daysSinceLastEvent = 2,
                activeItemId = "item-2", activeItemTitle = "Book the eye test", activeItemAgeDays = 4,
            ),
            EngineFacts.area(
                "home", "Home", events = 3, completions = 3, share = 0.1,
                queueLength = 0, queueLengthAtWindowStart = 3, daysSinceLastEvent = 3,
            ),
        )

        /**
         * Families with a rule, markers in their lines, and no binding.
         *
         * Recorded rather than left to be discovered. Each one speaks through its bare
         * lines and stays quiet on the rest, which is the safe half of the tradeoff in
         * [SlotBindings]: a marker nothing fills costs a line, and a marker filled from the
         * wrong fact costs the credibility of everything else the app says.
         */
        val KNOWN_UNBOUND: Set<String> = setOf(
            // Stages 2 and 3 count consecutive quiet days, which 3.1 does not declare and
            // RulesAwaitingFacts already records. Stage 1, the one with a rule, is authored
            // without a marker in it and speaks.
            "PULSE quietDay",
            // The first focus session's own length, and the days from adding an item to
            // finishing it. WindowFacts carries a total and ItemFacts a median.
            "REPORT_OBSERVATION firstMilestone",
            // `{pct} of your activity was after 5pm` needs a share of the day that stops at
            // midnight, and PartOfDay.NIGHT runs to five in the morning.
            "REPORT_OBSERVATION timeOfDay",
            // `since {sinceRef}` here means the week the queues stopped moving, which no
            // fact records.
            "REPORT_PATTERN queueEquilibrium",
            // Counts an answer given in each of three separate weeks. PulseFacts carries
            // the answers and not the weeks they fall in.
            "REPORT_PATTERN reportedVsActual",
        )
    }
}
