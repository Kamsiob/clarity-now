package com.kamsiob.claritynow.domain.engine.catalog

import com.kamsiob.claritynow.domain.engine.AnsweredPulse
import com.kamsiob.claritynow.domain.engine.CompletedItem
import com.kamsiob.claritynow.domain.engine.FactSet
import com.kamsiob.claritynow.domain.engine.HistoryFacts
import com.kamsiob.claritynow.domain.engine.realize.EngineFacts
import com.kamsiob.claritynow.domain.engine.realize.Measure
import com.kamsiob.claritynow.domain.engine.realize.MeasureKind
import com.kamsiob.claritynow.domain.engine.realize.MeasureScope
import com.kamsiob.claritynow.domain.engine.realize.MeasureValue
import com.kamsiob.claritynow.domain.engine.realize.Measures
import java.lang.reflect.Modifier
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.lang.reflect.WildcardType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The streak exception, audited over the real catalog. [StreakExceptionAudit] holds the
 * reasoning; this file supplies the fact sets and asserts.
 *
 * **Every check here has its failure branch exercised.** Four of the tests below build a
 * rule that does the forbidden thing and assert the audit finds it, and a fifth builds a
 * measure that reads a run and asserts the differential can see it. An audit that has only
 * ever passed is an audit nobody has verified, and this one exists precisely for a change
 * nobody has made yet.
 */
class StreakExceptionAuditTest {

    // --------------------------------------------------------------- the four checks

    @Test
    fun `the real catalog passes every check the exception is scoped by`() {
        val findings = StreakExceptionAudit.checkAll(CorpusFixture.catalog.rules, RunProbe)
        assertTrue(
            findings.joinToString("\n\n") { "${it.check}: ${it.detail}" },
            findings.isEmpty(),
        )
    }

    @Test
    fun `exactly the declared criteria read a run, and they are found by probing rather than by name`() {
        val quiet = StreakExceptionAudit
            .readersOf(StreakExceptionAudit.RunFact.QUIET, CorpusFixture.catalog.rules, RunProbe)
            .map { it.id }
        val single = StreakExceptionAudit
            .readersOf(StreakExceptionAudit.RunFact.SINGLE_AREA, CorpusFixture.catalog.rules, RunProbe)
            .map { it.id }
        assertEquals(
            "the two stages of quietDay that the corpus writes over consecutive quiet days",
            listOf("quietDay.run.2to3", "quietDay.run.4plus"),
            quiet.sorted(),
        )
        assertEquals(
            "the days branch of concentration stage 3, and nothing else. The criterion that " +
                "pairs the run to its own area does not appear here because it reads the area " +
                "and not the length, which is what makes it a pairing rather than a second " +
                "reading of the run",
            listOf("concentration.run.4plus"),
            single.sorted(),
        )
        assertEquals(
            "PERMITTED is the register of that decision and has to hold every one of them",
            (quiet + single).sorted(),
            StreakExceptionAudit.PERMITTED.map { it.criterionId }.sorted(),
        )
    }

    @Test
    fun `a criterion reading a run in the positive direction is caught`() {
        // The inversion, written the way somebody reasonable would write it: nothing has
        // been quiet, so something has been kept up. It is false at every length but zero,
        // which is exactly the shape the direction rule exists to name.
        val inverted = ruleOver("streakTest.inverted") { it.history.currentQuietRunDays == 0 }
        val findings = StreakExceptionAudit.checkAll(CorpusFixture.catalog.rules + inverted, RunProbe)
        assertTrue(
            "the audit did not notice a criterion firing on a quiet run of zero",
            findings.any { it.check == "a criterion fires on a run of zero" },
        )
        assertTrue(
            "and it is also a reader nothing declared",
            findings.any { it.check == "a criterion reads a run that nothing declared it may read" },
        )
    }

    @Test
    fun `a criterion reading a run nobody declared is caught even when its direction is right`() {
        val undeclared = ruleOver("streakTest.undeclared") { it.history.currentQuietRunDays >= 2 }
        val findings = StreakExceptionAudit.undeclaredReaders(CorpusFixture.catalog.rules + undeclared, RunProbe)
        assertEquals(
            "a rule may read a run only where somebody wrote down which absence it claims",
            listOf("streakTest.undeclared"),
            findings.map { it.detail.substringBefore(" reads") },
        )
    }

    @Test
    fun `a criterion with a threshold up at the cap is caught`() {
        val nearTheCap = ruleOver("streakTest.atTheCap") {
            it.history.currentQuietRunDays >= HistoryFacts.MAX_RUN_DAYS
        }
        val findings = StreakExceptionAudit.distinguishesTheCap(CorpusFixture.catalog.rules + nearTheCap, RunProbe)
        assertTrue(
            "a threshold at the cap reads `at least thirty` as `exactly thirty`, and gives a " +
                "run of absence a ceiling somebody could aim at",
            findings.any { it.detail.startsWith("streakTest.atTheCap") },
        )
    }

    @Test
    fun `a single area run rule that does not name its own area is caught`() {
        val unpaired = ClarityRule(
            key = "streakTest.unpaired",
            purpose = setOf(Purpose.PULSE),
            family = "concentration",
            subject = Subjects.AREA,
            criteria = listOf(
                criterion("streakTest.singleAreaRun", "the run is four days or more") { facts, _ ->
                    facts.history.currentSingleAreaRunDays >= 4
                },
                areaHasEvents(),
            ),
            priority = 0,
            horizonDays = HistoryFacts.MAX_RUN_DAYS,
            unflattering = false,
            stage = 3,
        )
        val findings = StreakExceptionAudit.runsWithoutASubject(CorpusFixture.catalog.rules + unpaired, RunProbe)
        assertEquals(
            "a run with no subject is `four days running` about nobody, which is a streak " +
                "sentence with the area filed off",
            listOf("a run rule that would name the wrong area"),
            findings.map { it.check },
        )
    }

    // --------------------------------------------------------------- never rendered

    /**
     * No slot can render either run, proved by reading every measure twice.
     *
     * The chain this rests on: a corpus marker is filled from a `SlotBindings` entry, an
     * entry names a `Measures` id, and the realizer fills a slot from that measure and from
     * nothing else, dropping any line whose marker has no binding. So a run reaches a
     * sentence as a number if and only if some measure's value moves when a run moves, and
     * that is what this reads. It is stronger than checking the binding table, because it
     * would catch a measure bound under a name that says nothing about runs.
     */
    @Test
    fun `no measure's value moves when only a run moves`() {
        val still = RunProbe.factsWith(0, 0, null)
        val running = RunProbe.factsWith(
            HistoryFacts.MAX_RUN_DAYS,
            HistoryFacts.MAX_RUN_DAYS,
            RunProbe.areaIds.first(),
        )
        assertNotEquals("the two probes are identical, so this test would pass on nothing", still, running)
        val moved = Measures.ALL.filter { measure -> reads(measure, still) != reads(measure, running) }
        assertTrue(
            StreakExceptionAudit.NEVER_RENDERED + ":\n" + moved.joinToString("\n") { "${it.category}.${it.id}" },
            moved.isEmpty(),
        )
    }

    @Test
    fun `the rendering check would catch a measure that read a run`() {
        val offender = Measure(
            id = "streakTestQuietRun",
            category = "history",
            kind = MeasureKind.DAYS,
            scope = MeasureScope.WINDOW,
            describe = "the current quiet run, which no measure may read",
        ) { facts, _, _ -> MeasureValue.Number(facts.history.currentQuietRunDays) }
        val still = RunProbe.factsWith(0, 0, null)
        val running = RunProbe.factsWith(HistoryFacts.MAX_RUN_DAYS, HistoryFacts.MAX_RUN_DAYS, RunProbe.areaIds.first())
        assertNotEquals(
            "the differential is the whole mechanism, and it has to be able to see one",
            reads(offender, still),
            reads(offender, running),
        )
    }

    /** A measure read for every entity its scope admits, so nothing is skipped for want of one. */
    private fun reads(measure: Measure, facts: FactSet): List<MeasureValue?> {
        val entities = when (measure.scope) {
            MeasureScope.WINDOW -> listOf(null)
            MeasureScope.AREA -> RunProbe.areaIds
            MeasureScope.ITEM -> listOf(RunProbe.ITEM_ID)
            MeasureScope.OFFSET -> listOf("0", "1", "2")
            MeasureScope.LABEL -> listOf(RunProbe.LABEL)
        }
        return entities.map { measure.read(facts, it, EngineFacts.ZONE) }
    }

    // --------------------------------------------------------------- no per day series

    /**
     * Nothing layer one hands out is one entry per day.
     *
     * **What this proves.** Every type reachable from a `FactSet` is enumerated by walking
     * field types, every collection valued member on those types is classified below, and
     * none of them is at day grain. A member that arrived without being classified fails
     * here, which is the case that matters: the two runs become a streak the moment
     * something beside them says which days had activity in them, and a new per day series
     * is how that would arrive. Every series that does exist is weekly and at most
     * `FactExtractor.SERIES_LENGTH` entries long, so seven days collapse into one number
     * and no day can be resolved out of it.
     *
     * **What it does not prove.** Two things, and both are worth stating rather than
     * leaving to be discovered.
     *
     * A `FactSet` still names single days: `WindowFacts.busiestDayKey` is one, and every
     * `AnsweredPulse` carries the day it was given. Neither is a series. One day named out
     * of a week cannot be extended into a run, and the answers name days a Pulse was
     * answered rather than days anything happened.
     *
     * And running the extractor once per day and differencing the results recovers a per
     * day series from any of these facts. That is true, it is what the simulator does, and
     * it is not something the two runs introduced: the same sequence recovers the same
     * series from `WindowFacts.totalEvents`, which has been on the fact set since phase 1.
     * The ban has never been about what the log can answer, and nothing in the shape of a
     * type could make it so. It is about what one fact set hands to one rule.
     */
    @Test
    fun `no member of a fact set is a per day series`() {
        val members = reachableFactTypes()
            .flatMap { cls ->
                cls.declaredFields
                    .filterNot { Modifier.isStatic(it.modifiers) }
                    .filter { isCollection(it.type) }
                    .map { "${cls.simpleName}.${it.name}" }
            }
            .sorted()
        assertEquals(
            StreakExceptionAudit.NO_PER_DAY_SERIES + ". Every collection on the fact set is " +
                "classified in this test with the grain of its entries. A member here that is " +
                "not in that table is a series nobody has decided about",
            GRAINS.keys.sorted(),
            members,
        )
        val perDay = GRAINS.filterValues { it == Grain.DAY }.keys.sorted()
        assertTrue(
            StreakExceptionAudit.NO_PER_DAY_SERIES + ": " + perDay,
            perDay.isEmpty(),
        )
    }

    @Test
    fun `the fact type walk reaches the types it is supposed to`() {
        val names = reachableFactTypes().map { it.simpleName }.sorted()
        assertEquals(
            "a fact type that stopped being reachable is a walk that stopped looking, and " +
                "every check above it would then pass on a smaller surface than it names",
            listOf(
                "ActiveItem", "AnsweredPulse", "AreaFacts", "CompletedItem", "CueFacts", "FactSet",
                "HistoryFacts", "ItemFacts", "PulseFacts", "RollupFacts", "WindowFacts",
            ),
            names,
        )
    }

    /**
     * A rule carrying one synthetic criterion, for the tests that prove a check can fail.
     *
     * The second criterion is there because every rule in the catalog requires at least two
     * things and a one criterion rule would be a different shape from the ones audited.
     */
    private fun ruleOver(id: String, test: (FactSet) -> Boolean): ClarityRule = ClarityRule(
        key = id,
        purpose = setOf(Purpose.PULSE),
        family = "quietDay",
        subject = Subjects.NONE,
        criteria = listOf(
            window(id, "a synthetic criterion, written to be caught by this audit", test),
            window("streakTest.hasHistory", "there is at least a day of history behind it") {
                it.history.daysSinceInstall >= 1
            },
        ),
        priority = 0,
        horizonDays = HistoryFacts.MAX_RUN_DAYS,
        unflattering = false,
        stage = 1,
    )

    /** What a collection's entries are one of. [Grain.DAY] is the one nothing may be. */
    private enum class Grain { DAY, WEEK, AREA, ITEM, ANSWER, FAMILY, PART_OF_DAY, FLAG }

    /**
     * Every collection reachable from a `FactSet`, and what one entry of it is.
     *
     * `PulseFacts.recentAnswers` is at [Grain.ANSWER] and not [Grain.DAY] deliberately. Each
     * answer carries the day it was given, so the list does name days, and the days it names
     * are the days a Pulse was answered rather than the days anything moved. A Pulse is
     * offered at most once a day and answering is optional, so the list is a subset of a
     * subset and cannot enumerate activity.
     */
    private val GRAINS: Map<String, Grain> = mapOf(
        "FactSet.areas" to Grain.AREA,
        "WindowFacts.eventsByPartOfDay" to Grain.PART_OF_DAY,
        "AreaFacts.weekEventsSeries" to Grain.WEEK,
        "RollupFacts.neglectedAreaIds" to Grain.AREA,
        "RollupFacts.dormantReturnedAreaIds" to Grain.AREA,
        "RollupFacts.queueDrainedAreaIds" to Grain.AREA,
        "RollupFacts.queueGrowingAreaIds" to Grain.AREA,
        "RollupFacts.freshStartAreaIds" to Grain.AREA,
        "ItemFacts.activeByArea" to Grain.AREA,
        "ItemFacts.completedInWindow" to Grain.ITEM,
        // The dates behind every other weekly series, one entry per bucket. It names days,
        // and the days it names are one every seven, each of them the first of a bucket, so
        // it resolves no day inside a week and no run of days can be counted from it. It is
        // the one member here whose entries are day keys and whose grain is still a week.
        "HistoryFacts.weekStartKeySeries" to Grain.WEEK,
        "HistoryFacts.weekCompletionsSeries" to Grain.WEEK,
        "HistoryFacts.weekQueueSizeSeries" to Grain.WEEK,
        "HistoryFacts.weekTotalEventsSeries" to Grain.WEEK,
        "HistoryFacts.weekAreaCountSeries" to Grain.WEEK,
        "HistoryFacts.weekFocusStartedSeries" to Grain.WEEK,
        "HistoryFacts.weekFocusCompletedSeries" to Grain.WEEK,
        "HistoryFacts.weekFocusEndedEarlySeries" to Grain.WEEK,
        "HistoryFacts.weekWeekendEventsSeries" to Grain.WEEK,
        "HistoryFacts.dominantAreaLastThreeWeeks" to Grain.WEEK,
        "HistoryFacts.firstEverFlags" to Grain.FLAG,
        "PulseFacts.recentAnswers" to Grain.ANSWER,
        "PulseFacts.answersByFamily" to Grain.FAMILY,
    )

    // --------------------------------------------------------------- reflection

    private companion object {

        /** The fact package itself, never one of its subpackages. */
        const val FACT_PACKAGE = "com.kamsiob.claritynow.domain.engine."

        fun isCollection(type: Class<*>): Boolean =
            Collection::class.java.isAssignableFrom(type) || Map::class.java.isAssignableFrom(type)

        fun isFactType(type: Class<*>): Boolean =
            type.name.startsWith(FACT_PACKAGE) &&
                !type.name.removePrefix(FACT_PACKAGE).contains('.') &&
                !type.isEnum &&
                !type.isInterface &&
                !type.isPrimitive

        /** Every class named by [type], including the arguments of a generic one. */
        fun typesIn(type: Type): List<Class<*>> = when (type) {
            is Class<*> -> listOf(type)
            is ParameterizedType ->
                listOfNotNull(type.rawType as? Class<*>) + type.actualTypeArguments.flatMap { typesIn(it) }
            is WildcardType -> type.upperBounds.flatMap { typesIn(it) }
            else -> emptyList()
        }
    }

    /** Every fact type a `FactSet` reaches, by field type rather than by any instance. */
    private fun reachableFactTypes(): Set<Class<*>> {
        val found = LinkedHashSet<Class<*>>()
        val pending = ArrayDeque<Class<*>>()
        pending += FactSet::class.java
        while (pending.isNotEmpty()) {
            val cls = pending.removeFirst()
            if (!isFactType(cls) || !found.add(cls)) continue
            for (field in cls.declaredFields) {
                if (Modifier.isStatic(field.modifiers)) continue
                pending += typesIn(field.genericType)
            }
        }
        return found
    }
}

/**
 * The fact set the audit varies, and everything else on it held still.
 *
 * Two areas, both with events in the window, because the pairing check has to be able to
 * set one against the other and an area subject rule has to have a subject to reach. The
 * series are twelve entries so that every rule reading a tail of three or four has one.
 */
private object RunProbe : StreakExceptionAudit.Probe {

    const val ITEM_ID = "item-one"

    const val LABEL = "Reprioritizing"

    override val areaIds: List<String> = listOf("area-one", "area-two")

    override fun factsWith(quietRunDays: Int, singleAreaRunDays: Int, runAreaId: String?): FactSet {
        val areas = listOf(
            EngineFacts.area(
                areaId = areaIds[0],
                name = "Work",
                events = 8,
                completions = 3,
                additions = 2,
                share = 0.6,
                activeItemId = ITEM_ID,
                activeItemTitle = "Rewrite the proposal intro",
                activeItemAgeDays = 9,
                queueLength = 2,
                queueLengthAtWindowStart = 4,
                daysSinceLastEvent = 0,
                focusSessions = 2,
                focusMinutes = 50,
                swapsInWindow = 2,
                dormantDaysBeforeReturn = 7,
                weekEventsSeries = listOf(3, 0, 4, 0, 5, 2, 3, 4, 2, 3, 5, 8),
            ),
            EngineFacts.area(
                areaId = areaIds[1],
                name = "Health",
                events = 5,
                completions = 2,
                additions = 1,
                share = 0.4,
                activeItemId = "item-two",
                activeItemTitle = "Book the appointment",
                activeItemAgeDays = 4,
                queueLength = 1,
                queueLengthAtWindowStart = 1,
                daysSinceLastEvent = 0,
                weekEventsSeries = listOf(2, 2, 1, 3, 2, 2, 1, 2, 3, 2, 1, 5),
            ),
        )
        return EngineFacts.factSet(
            window = EngineFacts.window(
                startDay = 0,
                endDay = 7,
                totalEvents = 13,
                completions = 5,
                additions = 3,
                promotions = 2,
                swaps = 3,
                focusStarted = 4,
                focusCompleted = 2,
                focusEndedEarly = 2,
                focusMinutes = 50,
                activeDays = 4,
                busiestDayKey = EngineFacts.dateKey(0),
                busiestDayCount = 5,
            ),
            areas = areas,
            dominantAreaId = areaIds[0],
            items = EngineFacts.items(
                areas.associateBy { it.areaId },
                completed = listOf(
                    CompletedItem(
                        itemId = "item-done",
                        titleSnapshot = "Send the invoice",
                        areaId = areaIds[0],
                        areaNameSnapshot = "Work",
                        daysActive = 3,
                    ),
                ),
                medianDaysToComplete = 4,
            ),
            history = EngineFacts.history(
                weekCompletions = SERIES,
                weekQueueSizes = SERIES,
                weekTotalEvents = SERIES,
                weekAreaCounts = SERIES,
                weekFocusStarted = SERIES,
                weekFocusCompleted = SERIES,
                weekFocusEndedEarly = SERIES,
                weekWeekendEvents = SERIES,
                dominantAreaLastThreeWeeks = listOf(areaIds[0], areaIds[1], areaIds[0]),
                personalBestWeekCompletions = 9,
                personalBestWeekKey = EngineFacts.dateKey(0),
                weeksSincePersonalBest = 2,
                mostRecentBetterWeekKey = EngineFacts.dateKey(0),
                longestEverActiveDays = 20,
                longestEverActiveItemId = ITEM_ID,
                personalBestFocusMinutesWeek = 120,
                currentQuietRunDays = quietRunDays,
                currentSingleAreaRunDays = singleAreaRunDays,
                currentSingleAreaRunAreaId = runAreaId,
            ),
            pulse = EngineFacts.pulse(
                answeredLifetime = 6,
                answeredInWindow = 2,
                positiveInWindow = 1,
                flaggedInWindow = 1,
                recentAnswers = listOf(
                    AnsweredPulse(
                        dateKey = EngineFacts.dateKey(0),
                        family = "persistence",
                        subjectId = ITEM_ID,
                        responseKey = "deepWork",
                        responseLabel = LABEL,
                        isPositive = true,
                    ),
                ),
            ),
        )
    }

    /** Twelve weeks, none of them equal to its neighbor, so no rule qualifies by accident. */
    private val SERIES: List<Int> = listOf(4, 6, 3, 7, 5, 8, 4, 9, 6, 10, 7, 11)
}
