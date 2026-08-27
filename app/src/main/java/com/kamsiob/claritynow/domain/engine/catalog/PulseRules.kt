package com.kamsiob.claritynow.domain.engine.catalog

import com.kamsiob.claritynow.domain.engine.FamilyKey

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
 * **`unflattering` is false throughout.** 7.4 enumerates the rules that carry it and every
 * one of them is a Report family. Pulse asks a question and offers two answers that are
 * equally valid read out of context, which is a different mechanism for the same problem
 * and does not need the neutral agent register. `CORPUS_1_PULSE.md` authors no `[N]`
 * lines at all, so there would be nothing to select even if one were marked.
 */
internal object PulseRules {

    /** Current window facts only. */
    private const val WINDOW_HORIZON = 7

    val ALL: List<ClarityRule> = buildList {
        addAll(persistence())
        addAll(concentration())
        addAll(accumulation())
        addAll(throughput())
        addAll(quietDay())
        addAll(spread())
        addAll(burst())
        addAll(queueDrain())
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
     * consecutive days`. Only the first branch has a rule. The second needs a count of
     * consecutive days on which one area held the window, and 3.1 declares no such fact.
     * It is recorded in [RulesAwaitingFacts] rather than approximated with `activeDays`,
     * which counts days with any activity at all and would fire the stage on a shape it
     * does not describe.
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
     * Only stage 1 has a rule. Stages 2 and 3 are `two to three consecutive quiet days`
     * and `four or more consecutive quiet days`, and 3.1 declares no consecutive quiet
     * day count. Recorded in [RulesAwaitingFacts]. Nothing here approximates it: a stage 3
     * fired on the wrong shape would tell someone they had been still for four days on
     * the strength of one quiet afternoon.
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
