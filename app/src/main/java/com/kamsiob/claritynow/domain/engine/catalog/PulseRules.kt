package com.kamsiob.claritynow.domain.engine.catalog

import com.kamsiob.claritynow.domain.engine.FamilyKey
import com.kamsiob.claritynow.domain.engine.HistoryFacts

/**
 * The Pulse rules. CLARITY_LOGIC_ENGINE.md 6.1, and every threshold from the stage
 * headers in `CORPUS_1_PULSE.md`.
 *
 * **Every number in this file is a corpus number.** `3..5`, `6..13`, `14..29` and `30` on
 * `persistence` are the four stage headers of that family, read in that file and repeated
 * here only because a criterion has to test something. Where this file and the corpus
 * ever disagree, the corpus wins, and [StageRangesTest] is what makes that true rather
 * than aspirational: it parses the headers and asserts the ranges below match.
 *
 * **A compound stage header becomes two rules pointing at the same stage.** 7.3 forbids
 * encoding a disjunction inside a range, so `accumulation` stage 3 and `throughput` stage
 * 3 each have two rules here, one over the magnitude and one over the multi week shape,
 * both carrying `stage = 3`.
 *
 * **Every family in 6.1 now has a rule, and every stage of every ladder has one.** The
 * five that did not are the ones whose escalation fact 3.1 did not declare: `switching`
 * wanted an area's own swap count, `rebalance` wanted the dormancy an area returned from,
 * and `quietDay` stages 2 and 3 and `concentration` stage 3's second branch wanted a run
 * of consecutive days. The facts phase declared all four and `RulesAwaitingFacts` is
 * empty. Nothing below approximates anything.
 *
 * **Two of those facts are the scoped exception to the streak ban**, and the rules reading
 * them are audited by [StreakExceptionAudit] rather than trusted. Read that file before
 * writing a rule over `currentQuietRunDays` or `currentSingleAreaRunDays`: the exception
 * is a shape, not a permission, and the audit is what keeps it from widening.
 *
 * **`unflattering` is false throughout.** 7.4 enumerates the rules that carry it and every
 * one of them is a Report family. Pulse asks a question and offers two answers that are
 * equally valid read out of context, which is a different mechanism for the same problem
 * and does not need the neutral agent register. `CORPUS_1_PULSE.md` authors no `[N]`
 * lines at all, so there would be nothing to select even if one were marked.
 */
internal object PulseRules {

    /** Current window facts only. */
    private const val WINDOW_HORIZON = 7

    /**
     * A rule reading one of the two capped runs.
     *
     * `HistoryFacts.MAX_RUN_DAYS` is the oldest day either run can reach, so a rule over
     * one of them references facts up to that many days old and declares a horizon that
     * covers it. Section 4 defines a horizon as the age of the oldest fact referenced, and
     * a shorter one here would be a rule quietly describing days outside what it declared.
     */
    private const val RUN_HORIZON = HistoryFacts.MAX_RUN_DAYS

    /** The second branch of `concentration` stage 3, `four or more consecutive days`. */
    private const val CONCENTRATION_RUN_DAYS = 4

    val ALL: List<ClarityRule> = buildList {
        addAll(persistence())
        addAll(concentration())
        addAll(accumulation())
        addAll(throughput())
        addAll(quietDay())
        addAll(spread())
        addAll(switching())
        addAll(burst())
        addAll(queueDrain())
        addAll(rebalance())
        addAll(freshStart())
    }

    private fun pulse(
        key: RuleKey,
        family: FamilyKey,
        stage: Int,
        subject: SubjectSelector,
        horizonDays: Int,
        priority: Int = 0,
        criteria: List<Criterion>,
    ) = ClarityRule(
        key = key,
        purpose = setOf(Purpose.PULSE),
        family = family,
        subject = subject,
        criteria = criteria,
        priority = priority,
        horizonDays = horizonDays,
        unflattering = false,
        stage = stage,
    )

    /**
     * `persistence`, over `activeItemAgeDays`. The most frequently firing family in the
     * app and the one where escalation matters most.
     *
     * Stage 4 carries a third criterion that is not about the ladder at all: the item has
     * to genuinely hold the lifetime record. 7.3 requires it, because stage 4's bench
     * reaches into `HistoryFacts.longestEverActiveDays` and every line there would be a
     * lie the moment a longer running item existed.
     */
    private fun persistence(): List<ClarityRule> = listOf(
        pulse("pulse.persistence.s1", "persistence", 1, Subjects.ACTIVE_ITEM, WINDOW_HORIZON, criteria = listOf(
            activeItemAge("persistence.age.3to5", "the active item is three to five days old") { it in 3..5 },
            holdingAreaHasEvents(),
        )),
        pulse("pulse.persistence.s2", "persistence", 2, Subjects.ACTIVE_ITEM, horizonDays = 14, criteria = listOf(
            activeItemAge("persistence.age.6to13", "the active item is six to thirteen days old") { it in 6..13 },
            holdingAreaHasEvents(),
        )),
        pulse("pulse.persistence.s3", "persistence", 3, Subjects.ACTIVE_ITEM, horizonDays = 30, criteria = listOf(
            activeItemAge("persistence.age.14to29", "the active item is fourteen to twenty nine days old") { it in 14..29 },
            holdingAreaHasEvents(),
            window("persistence.someCompletions", "something else was completed, so the comparison has a number") {
                it.window.completions >= 1
            },
        )),
        pulse("pulse.persistence.s4", "persistence", 4, Subjects.ACTIVE_ITEM, horizonDays = 180, criteria = listOf(
            activeItemAge("persistence.age.30plus", "the active item is thirty days old or more") { it >= 30 },
            holdingAreaHasEvents(),
            criterion(
                "persistence.holdsTheRecord",
                "this item genuinely holds the longest ever active record, without which " +
                    "stage 4's historical language would be false",
            ) { facts, subject -> subject != null && facts.history.longestEverActiveItemId == subject.id },
        )),
    )

    /**
     * `concentration`, over `shareOfEvents` combined with `eventsInWindow`.
     *
     * Every rule here carries an event floor, per 3.1 and failure mode `Off-by-one on
     * shares`. One event in a one event day is a hundred percent concentration and
     * nothing at all.
     *
     * Stage 3's header is compound: `ninety five percent and above, or four or more
     * consecutive days`, and both branches now have a rule pointing at stage 3, per 7.3.
     *
     * The days branch reads `HistoryFacts.currentSingleAreaRunDays`, which is one of the
     * two facts the streak ban is scoped around. It carries **two** criteria over that
     * run and not one, because the length alone is a claim with no subject: every stage 3
     * statement names the area, and pairing a run with whichever area happened to lead
     * the window would eventually print `{areaName} has held everything for four days`
     * about an area that led the window without holding the run. `currentSingleAreaRunAreaId`
     * is the run's own subject and the rule may claim it only for that area.
     * [StreakExceptionAudit] enforces that pairing rather than leaving it to review.
     */
    private fun concentration(): List<ClarityRule> = listOf(
        pulse("pulse.concentration.s1", "concentration", 1, Subjects.AREA, WINDOW_HORIZON, criteria = listOf(
            area("$SHARE_READING_PREFIX.concentration.70to84", "the area holds seventy to eighty four percent of the window") {
                it.shareOfEvents >= 0.70 && it.shareOfEvents < 0.85
            },
            areaShareFloor(4),
            shareFloor(4),
        )),
        pulse("pulse.concentration.s2", "concentration", 2, Subjects.AREA, WINDOW_HORIZON, criteria = listOf(
            area("$SHARE_READING_PREFIX.concentration.85to94", "the area holds eighty five to ninety four percent of the window") {
                it.shareOfEvents >= 0.85 && it.shareOfEvents < 0.95
            },
            areaShareFloor(4),
            shareFloor(4),
        )),
        pulse("pulse.concentration.s3.share", "concentration", 3, Subjects.AREA, WINDOW_HORIZON, criteria = listOf(
            area("$SHARE_READING_PREFIX.concentration.95plus", "the area holds ninety five percent of the window or more") {
                it.shareOfEvents >= 0.95
            },
            areaShareFloor(4),
            shareFloor(4),
        )),
        pulse("pulse.concentration.s3.days", "concentration", 3, Subjects.AREA, horizonDays = RUN_HORIZON, criteria = listOf(
            criterion(
                "concentration.run.4plus",
                "one area has held every event of every day for four days or more, which is the " +
                    "second branch of the stage 3 header",
            ) { facts, _ -> facts.history.currentSingleAreaRunDays >= CONCENTRATION_RUN_DAYS },
            criterion(
                "concentration.run.isThisArea",
                "the run belongs to this area, without which the days would be a length with no " +
                    "subject and the sentence would name whichever area happened to lead",
            ) { facts, subject -> subject != null && facts.history.currentSingleAreaRunAreaId == subject.id },
            areaHasEvents(),
        )),
    )

    /**
     * `accumulation`, over the intake gap. Stage 3's header is compound and both branches
     * have a rule: the magnitude, and queues growing three weeks running, which
     * `HistoryFacts.weekQueueSizeSeries` supports directly.
     */
    private fun accumulation(): List<ClarityRule> = listOf(
        pulse("pulse.accumulation.s1", "accumulation", 1, Subjects.NONE, WINDOW_HORIZON, criteria = listOf(
            window("accumulation.gap.2to3", "additions exceed completions by two or three") {
                (it.window.additions - it.window.completions) in 2..3
            },
            window("accumulation.additions", "at least two things were added, so no count renders as zero") {
                it.window.additions >= 2
            },
        )),
        pulse("pulse.accumulation.s2", "accumulation", 2, Subjects.NONE, WINDOW_HORIZON, criteria = listOf(
            window("accumulation.gap.4to7", "additions exceed completions by four to seven") {
                (it.window.additions - it.window.completions) in 4..7
            },
            window("accumulation.additions", "at least two things were added, so no count renders as zero") {
                it.window.additions >= 2
            },
        )),
        pulse("pulse.accumulation.s3.gap", "accumulation", 3, Subjects.NONE, WINDOW_HORIZON, criteria = listOf(
            window("accumulation.gap.8plus", "additions exceed completions by eight or more") {
                (it.window.additions - it.window.completions) >= 8
            },
            window("accumulation.additions", "at least two things were added, so no count renders as zero") {
                it.window.additions >= 2
            },
        )),
        pulse("pulse.accumulation.s3.weeks", "accumulation", 3, Subjects.NONE, horizonDays = 30, criteria = listOf(
            window("accumulation.queuesRising", "the queues have grown three weeks running") {
                strictlyRising(it.history.weekQueueSizeSeries, 3)
            },
            window("accumulation.gap.2plus", "additions still exceed completions by two or more this window") {
                (it.window.additions - it.window.completions) >= 2
            },
            window("accumulation.additions", "at least two things were added, so no count renders as zero") {
                it.window.additions >= 2
            },
        )),
    )

    /** `throughput`, over net flow. Stage 3's second branch reads the completions series. */
    private fun throughput(): List<ClarityRule> = listOf(
        pulse("pulse.throughput.s1", "throughput", 1, Subjects.NONE, WINDOW_HORIZON, criteria = listOf(
            window("throughput.net.1to2", "completions exceed additions by one or two") { it.window.netFlow in 1..2 },
            window("throughput.completions.2", "at least two completions, per the family trigger") {
                it.window.completions >= 2
            },
        )),
        pulse("pulse.throughput.s2", "throughput", 2, Subjects.NONE, WINDOW_HORIZON, criteria = listOf(
            window("throughput.net.3to5", "completions exceed additions by three to five") { it.window.netFlow in 3..5 },
            window("throughput.completions.2", "at least two completions, per the family trigger") {
                it.window.completions >= 2
            },
        )),
        pulse("pulse.throughput.s3.net", "throughput", 3, Subjects.NONE, WINDOW_HORIZON, criteria = listOf(
            window("throughput.net.6plus", "completions exceed additions by six or more") { it.window.netFlow >= 6 },
            window("throughput.completions.2", "at least two completions, per the family trigger") {
                it.window.completions >= 2
            },
        )),
        pulse("pulse.throughput.s3.weeks", "throughput", 3, Subjects.NONE, horizonDays = 30, criteria = listOf(
            window("throughput.rising", "completions have risen three weeks running") {
                strictlyRising(it.history.weekCompletionsSeries, 3)
            },
            window("throughput.net.1plus", "completions still exceed additions this window") { it.window.netFlow >= 1 },
            window("throughput.completions.2", "at least two completions, per the family trigger") {
                it.window.completions >= 2
            },
        )),
    )

    /**
     * `quietDay`, over consecutive quiet days.
     *
     * **Stage 1 is written against the family trigger and stages 2 and 3 against the
     * ladder fact, and that is not an inconsistency.** 6.1 triggers the family on fewer
     * than two events in the window, and stage 1's bench says so: `One thing happened
     * yesterday` is a stage 1 line about a day that had an event in it. A day holding one
     * event is not a quiet day to `currentQuietRunDays`, which counts days with nothing,
     * so the run is zero there and stage 1 still has to fire. Every line at stages 2 and 3
     * claims nothing moved, so those two read the run.
     *
     * **The consequence is that the three rungs do not exclude each other, and the
     * escalation is carried by [priority] rather than by disjoint ranges.** On the second
     * day of a quiet run the window is empty, so stage 1's criteria hold as well as stage
     * 2's, and specificity cannot separate them: both require two things. Every other
     * Pulse ladder in this file separates its rungs by writing disjoint ranges over one
     * fact, and this one cannot, because the two facts disagree about what a quiet day is.
     *
     * The obvious repair is to bound stage 1 with `currentQuietRunDays <= 1`, and it is
     * exactly what [StreakExceptionAudit] forbids. A criterion that fires when a run of
     * absence is short is reading the fact as evidence that something has been kept up,
     * which is the streak reading the fact was shaped to prevent. Priority is the honest
     * instrument here: it says stage 3 outranks stage 2 outranks stage 1 when all three
     * describe the same day, and it says nothing about anybody's week.
     */
    private fun quietDay(): List<ClarityRule> = listOf(
        pulse("pulse.quietDay.s1", "quietDay", 1, Subjects.NONE, WINDOW_HORIZON, criteria = listOf(
            window("quietDay.fewerThanTwo", "fewer than two events in the window, per the family trigger") {
                it.window.totalEvents < 2
            },
            window("quietDay.hasHistory", "there is at least a day of history, so this is a quiet day and not an empty install") {
                it.history.daysSinceInstall >= 1
            },
        )),
        pulse("pulse.quietDay.s2", "quietDay", 2, Subjects.NONE, WINDOW_HORIZON, priority = 1, criteria = listOf(
            window("quietDay.run.2to3", "two or three days running with nothing in them at all") {
                it.history.currentQuietRunDays in 2..3
            },
            window("quietDay.hasHistory", "there is at least a day of history, so this is a quiet day and not an empty install") {
                it.history.daysSinceInstall >= 1
            },
        )),
        pulse("pulse.quietDay.s3", "quietDay", 3, Subjects.NONE, horizonDays = RUN_HORIZON, priority = 2, criteria = listOf(
            window("quietDay.run.4plus", "four or more days running with nothing in them at all") {
                it.history.currentQuietRunDays >= 4
            },
            window("quietDay.hasHistory", "there is at least a day of history, so this is a quiet day and not an empty install") {
                it.history.daysSinceInstall >= 1
            },
        )),
    )

    /**
     * `switching`, over the area's own swap count. 6.1 gives it the area as its subject.
     *
     * `WindowFacts.swaps` counts the whole window across every area and cannot serve: all
     * eighteen of this family's statements name an area, so a rule reading the window
     * total would say `You changed what is active in Work twice` about a week holding one
     * swap in Work and one in Health. `AreaFacts.swapsInWindow` is the per area count, and
     * the ids here are distinct from the `switching.swaps.*` pair in [ReportRules], which
     * read the window total for `switchingBehavior` and are a different measurement.
     *
     * **All eighteen lines name the area and `SlotBindings` binds nothing for this
     * family**, so the rule qualifies and the bench is empty until that entry exists. A
     * line whose marker has no binding is dropped by the realizer, per 7.2's slot
     * completeness rule, so the family is silent in exactly the way it was silent before,
     * and the fact and the rule are the two thirds of it that belong here. The same is
     * true of `rebalance` below.
     */
    private fun switching(): List<ClarityRule> = listOf(
        pulse("pulse.switching.s1", "switching", 1, Subjects.AREA, WINDOW_HORIZON, criteria = listOf(
            area("switching.area.swaps.1", "the area changed its active item exactly once") {
                it.swapsInWindow == 1
            },
            areaHasEvents(),
        )),
        pulse("pulse.switching.s2", "switching", 2, Subjects.AREA, WINDOW_HORIZON, criteria = listOf(
            area("switching.area.swaps.2plus", "the area changed its active item twice or more") {
                it.swapsInWindow >= 2
            },
            areaHasEvents(2),
        )),
    )

    /**
     * `rebalance`, over the dormancy an area returned from. 6.1 names dormancy length as
     * its escalation fact.
     *
     * `AreaFacts.dormantDaysBeforeReturn` is null in the two cases where nothing came
     * back, and null is read as zero here so that neither stage can fire on an absence.
     * `daysSinceLastEvent` cannot serve and the difference is the whole family: it is zero
     * the moment the area moves, so it answers how long the area has been quiet **since**
     * the return rather than before it.
     *
     * The horizons are long because the fact is. A stage 2 return names a gap of a
     * fortnight or more and `CORPUS_1_PULSE.md` authors `Three weeks of stillness` at that
     * stage, so the rule declares a horizon that reaches the oldest fact it references
     * rather than the width of the window it fired in.
     *
     * Every line of this family carries a marker too, and `{ageDays}` here is the dormancy
     * rather than an item's age, so the binding this family waits for is its own and not a
     * copy of another family's. `AreaFacts.dormantDaysBeforeReturn` is the fact behind it
     * and it now exists, which is what that entry was waiting for.
     */
    private fun rebalance(): List<ClarityRule> = listOf(
        pulse("pulse.rebalance.s1", "rebalance", 1, Subjects.AREA, horizonDays = 14, criteria = listOf(
            area("rebalance.dormant.5to13", "the area had been still five to thirteen days before it moved") {
                (it.dormantDaysBeforeReturn ?: 0) in 5..13
            },
            areaHasEvents(),
        )),
        pulse("pulse.rebalance.s2", "rebalance", 2, Subjects.AREA, horizonDays = 180, criteria = listOf(
            area("rebalance.dormant.14plus", "the area had been still fourteen days or more before it moved") {
                (it.dormantDaysBeforeReturn ?: 0) >= 14
            },
            areaHasEvents(),
        )),
    )

    /** `spread`, over the count of areas with events. */
    private fun spread(): List<ClarityRule> = listOf(
        pulse("pulse.spread.s1", "spread", 1, Subjects.NONE, WINDOW_HORIZON, criteria = listOf(
            window("spread.threeAreas", "exactly three areas had events") { it.rollup.areasWithEvents == 3 },
            window("$SHARE_READING_PREFIX.spread.noneDominant", "no area is above half the window") {
                it.rollup.dominantShare < 0.50
            },
            shareFloor(5),
        )),
        pulse("pulse.spread.s2", "spread", 2, Subjects.NONE, WINDOW_HORIZON, criteria = listOf(
            window("spread.fourOrMoreAreas", "four or more areas had events") { it.rollup.areasWithEvents >= 4 },
            window("$SHARE_READING_PREFIX.spread.noneDominant", "no area is above half the window") {
                it.rollup.dominantShare < 0.50
            },
            shareFloor(5),
        )),
    )

    /**
     * `burst`, over completions in one area.
     *
     * The trigger in 6.1 is three or more completions in one area **in one day**, and the
     * Pulse window is one reflection period, so `completionsInWindow` is that day's count
     * rather than a week's. A Report rule reading the same field would be reading a week
     * and would need a different fact.
     */
    private fun burst(): List<ClarityRule> = listOf(
        pulse("pulse.burst.s1", "burst", 1, Subjects.AREA, WINDOW_HORIZON, criteria = listOf(
            area("burst.completions.3to4", "the area completed three or four things in the window") {
                it.completionsInWindow in 3..4
            },
            areaHasEvents(3),
        )),
        pulse("pulse.burst.s2", "burst", 2, Subjects.AREA, WINDOW_HORIZON, criteria = listOf(
            area("burst.completions.5plus", "the area completed five or more things in the window") {
                it.completionsInWindow >= 5
            },
            areaHasEvents(5),
        )),
    )

    /** `queueDrain`, over the queue size the area started the window with. */
    private fun queueDrain(): List<ClarityRule> = listOf(
        pulse("pulse.queueDrain.s1", "queueDrain", 1, Subjects.AREA, WINDOW_HORIZON, criteria = listOf(
            area("queueDrain.from.3to4", "the area started the window with a queue of three or four") {
                it.queueLengthAtWindowStart in 3..4
            },
            area("queueDrain.toZero", "the area's queue is now empty") { it.queueLength == 0 },
            areaHasEvents(),
        )),
        pulse("pulse.queueDrain.s2", "queueDrain", 2, Subjects.AREA, WINDOW_HORIZON, criteria = listOf(
            area("queueDrain.from.5plus", "the area started the window with a queue of five or more") {
                it.queueLengthAtWindowStart >= 5
            },
            area("queueDrain.toZero", "the area's queue is now empty") { it.queueLength == 0 },
            areaHasEvents(),
        )),
    )

    /** `freshStart`, a single stage. A new area, or a first item in an empty one. */
    private fun freshStart(): List<ClarityRule> = listOf(
        pulse("pulse.freshStart.s1", "freshStart", 1, Subjects.AREA, WINDOW_HORIZON, criteria = listOf(
            criterion("freshStart.isFresh", "the area is one this window began or reopened") { facts, subject ->
                subject != null && subject.id in facts.rollup.freshStartAreaIds
            },
            areaHasEvents(),
        )),
    )
}
