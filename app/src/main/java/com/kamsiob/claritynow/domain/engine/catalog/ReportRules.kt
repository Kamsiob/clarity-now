package com.kamsiob.claritynow.domain.engine.catalog

import com.kamsiob.claritynow.domain.engine.FamilyKey

/**
 * The Report rules: the headline, the observations and the pattern line.
 * CLARITY_LOGIC_ENGINE.md 6.3, 6.4, 7.3 and 7.4.
 *
 * **The headline is selected first and constrains everything after it**, per 9.2, which
 * is a composition rule rather than a catalog one; the catalog's part is that the
 * headline is its own purpose with its own rules, so the composer can select it alone.
 *
 * **Where a threshold is stated in a corpus stage header, the header is the source.**
 * `eighty to eighty nine percent`, `one to three sessions`, `seven to thirteen days` and
 * the rest are read from `CORPUS_2_REPORT.md` and asserted against by [StageRangesTest].
 *
 * **Where a trigger is stated in prose without a number, this file chooses one and says
 * so.** Four triggers in section 1 of the corpus read `below the quiet threshold`,
 * `clearly exceed`, `within a narrow band` and `a clear margin`, and neither the corpus
 * nor CLARITY_LOGIC_ENGINE.md gives a figure. Every such choice is a named constant below
 * with the reasoning beside it, so the next person to disagree with one can find it and
 * change it in one place rather than hunting through predicates.
 */
internal object ReportRules {

    /**
     * A week is quiet when it holds fewer events than it has days.
     *
     * Derived rather than picked: fewer than one event a day is the shape the word quiet
     * describes, it scales with the window rather than assuming seven days, and it does
     * not need a magic number. `CORPUS_2_REPORT.md` 1.1 says only `below the quiet
     * threshold`, so this is the threshold until the corpus states one.
     */
    private fun isQuietWeek(events: Int, days: Int): Boolean = events < days

    /**
     * `clearly exceed` for the two flow headlines.
     *
     * Three, and **the reasoning that first chose it does not hold**, so it is restated
     * here rather than left standing. The original was that two is where the Pulse
     * `accumulation` and `throughput` ladders begin and a headline should not fire on the
     * margin a daily note uses. That compares a week to a day: a weekly margin set one
     * above a daily one is a weaker claim per day, not a stronger one, so the number was
     * being justified by an argument for a different number.
     *
     * The number stays where it is on its own weekly terms. A headline is the largest text
     * in the app and it is a pull quote, so it wants a gap somebody would recognize
     * without being shown the arithmetic; and it is the floor for both directions, so it
     * has to be a margin `netInflow` can also carry without becoming the default headline
     * of every ordinary week. Three is the smallest gap that reads as a direction rather
     * than as noise across seven days, and `netInflow` at this margin took 34 of 451
     * report windows, which is a headline that means something rather than one that is
     * always there.
     *
     * **`netOutflow` never qualified once and lowering this would not change that.**
     * `PulseRules.throughput` carries the account: no simulated day finishes more than it
     * captures, so no simulated week does either, and weekly net flow is at or below zero
     * throughout the run. The margin is not what is holding this headline back and moving
     * it would only weaken `netInflow`, which works.
     */
    private const val CLEAR_FLOW_MARGIN = 3

    /**
     * The magnitude at which `intakeVsOutput` leaves stage 1.
     *
     * The corpus header for stage 1 is `mild imbalance, gap of two to four`, and the
     * headers for stages 2 and 3 state a direction with no magnitude. Five is one above
     * stage 1's stated upper bound, so it comes from the corpus rather than from
     * judgment: it is the smallest gap stage 1 does not already cover.
     */
    private const val CLEAR_IMBALANCE = 5

    /** `within a narrow band`, three weeks. Absolute, per [withinBand]. */
    private const val STEADY_BAND = 2

    /** `a narrow band for four weeks`, for `consistentRhythm`. */
    private const val RHYTHM_BAND = 2

    /** `neither grown nor shrunk`, for `queueEquilibrium`. */
    private const val EQUILIBRIUM_BAND = 1

    /** The window itself. */
    private const val WEEK_HORIZON = 7

    /** A comparison across the last few weeks. */
    private const val PATTERN_HORIZON = 90

    /**
     * The fewest weeks `comebackPattern` can see two returns across.
     *
     * Active, quiet, active, quiet, active. Derived rather than chosen: it is the length
     * of the shortest series the family's own claim can occupy, so a shorter requirement
     * would be a criterion that never separates anything.
     */
    private const val COMEBACK_WEEKS = 5

    /** A comparison against every week since install. */
    private const val RECORD_HORIZON = 180

    /** Patterns need three weeks of snapshots before any of them may fire. 6.3. */
    private val threeWeeksOfData = window(
        "report.weeksOfData.3",
        "there are at least three weeks of snapshots, without which no pattern may fire",
    ) { it.history.weeksOfData >= 3 }

    val ALL: List<ClarityRule> = headlines() + observations() + patterns()

    /**
     * Corpus families the Report renders directly, with no rule and no engine selection.
     *
     * **One entry, and it is an empty state rather than an observation.**
     * `CORPUS_2_REPORT.md` 3.16 is written as a pattern family and it is not one: its four
     * lines say that there is not enough history to see a pattern yet. Nothing in them is a
     * claim about the person, there is no subject, there is no escalation and there is
     * nothing for layer 5 to compare against a fact.
     *
     * As a rule it was also unreachable. `ReportComposer` asks for a pattern only when
     * `weeksOfData >= 3`, and the rule required `weeksOfData < 3`, so the two conditions
     * could never both hold and the family had authored language that could not be spoken.
     * Making the composer ask for a pattern it did not want, so that a rule could tell it
     * there was none, is machinery in the shape of a section header.
     *
     * So the composer renders the line itself, through `ReportLanguage.insufficientData`,
     * exactly as it renders the other three benches that are not families. That is a
     * deliberate and owner authorized exception to the rule that every sentence about a
     * person's own data comes through the engine, and it is narrow: an empty state makes no
     * claim about anybody's data. `ReportComposer` carries the full note at the point the
     * condition is read.
     *
     * [CatalogIntegrity.everyFamilyHasARule] reads this, so a family cannot lose its rule
     * without somebody deciding it should. **Adding an entry here is a family leaving the
     * engine.** Add one only for a bench that makes no claim about a person, and never to
     * park a rule that is inconvenient to write.
     *
     * **`insufficientData` is the ninth of the nine families that never qualified once,
     * and it is the only one of them already closed.** The rules pass re-checked it rather
     * than assuming: it is the entry above, the composer renders it, and the catalog
     * defect the measurement recorded, a rule requiring fewer than three weeks behind a
     * gate that only asks for a pattern at three or more, no longer exists. Nothing about
     * it is a threshold or a window and it needs nothing from phase 9.
     */
    val RENDERED_DIRECTLY: Set<FamilyKey> = setOf("insufficientData")

    private fun report(
        key: RuleKey,
        purpose: Purpose,
        family: FamilyKey,
        stage: Int,
        horizonDays: Int,
        subject: SubjectSelector = Subjects.NONE,
        priority: Int = 0,
        unflattering: Boolean = UnflatteringRules.isUnflattering(family, stage),
        absenceSubject: Boolean = false,
        callback: CallbackRequirement? = null,
        criteria: List<Criterion>,
    ) = ClarityRule(
        key = key,
        purpose = setOf(purpose),
        family = family,
        subject = subject,
        criteria = criteria,
        priority = priority,
        horizonDays = horizonDays,
        unflattering = unflattering,
        absenceSubject = absenceSubject,
        requiresCallback = callback,
        stage = stage,
    )

    // ------------------------------------------------------------- headlines

    private fun headlines(): List<ClarityRule> = listOf(
        report("report.headline.quietWeek", Purpose.REPORT_HEADLINE, "quietWeek", 1, WEEK_HORIZON, criteria = listOf(
            window("headline.quiet", "the week holds fewer events than it has days") {
                isQuietWeek(it.window.totalEvents, it.window.dayCount)
            },
            window("headline.quiet.notEmpty", "something happened, so the week is quiet rather than absent") {
                it.window.totalEvents >= 1
            },
        )),
        report("report.headline.netOutflow", Purpose.REPORT_HEADLINE, "netOutflow", 1, WEEK_HORIZON, criteria = listOf(
            window("headline.outflow", "completions exceed additions by a clear margin") {
                it.window.netFlow >= CLEAR_FLOW_MARGIN
            },
            window("headline.outflow.completions", "there are completions to speak of") { it.window.completions >= 2 },
        )),
        report("report.headline.netInflow", Purpose.REPORT_HEADLINE, "netInflow", 1, WEEK_HORIZON, criteria = listOf(
            window("headline.inflow", "additions exceed completions by a clear margin") {
                it.window.netFlow <= -CLEAR_FLOW_MARGIN
            },
            window("headline.inflow.additions", "there are additions to speak of") { it.window.additions >= 2 },
        )),
        report("report.headline.singleFocus", Purpose.REPORT_HEADLINE, "singleFocus", 1, WEEK_HORIZON, criteria = listOf(
            window("$SHARE_READING_PREFIX.headline.single", "one area holds eighty percent of the week or more") {
                it.rollup.dominantAreaId != null && it.rollup.dominantShare >= 0.80
            },
            shareFloor(5),
        )),
        report("report.headline.balanced", Purpose.REPORT_HEADLINE, "balanced", 1, WEEK_HORIZON, criteria = listOf(
            window("headline.balanced.areas", "three or more areas had activity") { it.rollup.areasWithEvents >= 3 },
            window("$SHARE_READING_PREFIX.headline.balanced", "no area dominates") { it.rollup.dominantShare < 0.50 },
            shareFloor(5),
        )),
        report("report.headline.focusProtected", Purpose.REPORT_HEADLINE, "focusProtected", 1, WEEK_HORIZON, criteria = listOf(
            window("headline.focus.five", "five or more focus sessions finished") { it.window.focusCompleted >= 5 },
            window("headline.focus.minutes", "there are minutes to speak of") { it.window.focusMinutesTotal >= 1 },
        )),
        report("report.headline.personalBest", Purpose.REPORT_HEADLINE, "personalBest", 1, RECORD_HORIZON, criteria = listOf(
            window("headline.best.noBetterWeek", "no earlier week strictly beats this one") {
                it.history.mostRecentBetterWeekKey == null
            },
            window("headline.best.beatsTheRecord", "this week reaches the personal best") {
                it.window.completions >= it.history.personalBestWeekCompletions
            },
            window("headline.best.hasHistory", "there is more than one week to be best of") {
                it.history.weeksOfData >= 2
            },
            window("headline.best.completions", "there are completions, so no count renders as zero") {
                it.window.completions >= 1
            },
        )),
        report("report.headline.mostActiveSince", Purpose.REPORT_HEADLINE, "mostActiveSince", 1, RECORD_HORIZON, criteria = listOf(
            window("headline.since.hasNamedPoint", "there is an earlier week that strictly beats this one to name") {
                it.history.mostRecentBetterWeekKey != null
            },
            window("headline.since.beatsRecent", "this week beats the one before it") {
                (it.history.weekOverWeekDelta ?: 0) > 0
            },
            window("headline.since.hasHistory", "there are three weeks to reach back across") {
                it.history.weeksOfData >= 3
            },
        )),
        report("report.headline.decliningActivity", Purpose.REPORT_HEADLINE, "decliningActivity", 1, PATTERN_HORIZON, criteria = listOf(
            window("headline.declining", "total activity has fallen three weeks running") {
                strictlyFalling(it.history.weekTotalEventsSeries, 3)
            },
            threeWeeksOfData,
        )),
        report("report.headline.risingActivity", Purpose.REPORT_HEADLINE, "risingActivity", 1, PATTERN_HORIZON, criteria = listOf(
            window("headline.rising", "total activity has risen three weeks running") {
                strictlyRising(it.history.weekTotalEventsSeries, 3)
            },
            threeWeeksOfData,
        )),
        report("report.headline.comeback", Purpose.REPORT_HEADLINE, "comeback", 1, PATTERN_HORIZON, Subjects.AREA, criteria = listOf(
            criterion("headline.comeback.area", "this area returned after a long dormancy") { facts, subject ->
                subject != null && subject.id in facts.rollup.dormantReturnedAreaIds
            },
            areaHasEvents(),
        )),
        report("report.headline.queuePressure", Purpose.REPORT_HEADLINE, "queuePressure", 1, WEEK_HORIZON, criteria = listOf(
            window("headline.queue.growing", "two or more areas grew their queues") {
                it.rollup.queueGrowingAreaIds.size >= 2
            },
            window("headline.queue.holds", "the queues hold something to count") { it.totalQueueLength() >= 3 },
        )),
        /**
         * `clearing`, dark for 451 report windows, and the reason was not in this rule.
         * **It fires twice now, and the drain fact is measurably the whole of why.**
         *
         * It reads `RollupFacts.queueDrainedAreaIds`, which was anchored to the two window
         * boundaries, so a queue built and finished inside the week was invisible to it.
         * That list is now `AreaFacts.queueDrainedFrom` with the corpus floor applied, and
         * this rule reads it unchanged: 1.13's trigger is `one or more areas fully drained`
         * and a fall to nothing on Saturday is one. `PulseRules.queueDrain` carries the full
         * account of the fact and why a criterion could not approximate it. Nothing about a
         * headline is weekly in the wrong way here: 1.13's eight lines are a week's pull
         * quote and the window they are read over is the week.
         *
         * **The attribution is measured rather than argued.** The drain fact and the
         * persona fix landed together, so the fifth measurement ran the year three times to
         * separate them: with the fact and the old personas this headline reads 0, with the
         * fixed personas and the boundary anchored fact it still reads 0, and with both it
         * reads 2. It is the only family in the run for which the fact alone is responsible,
         * and the two headlines it bought displaced exactly two others, one `comeback` and
         * the last surviving `risingActivity`. DECISIONS.md, the persona defect and the
         * fifth measurement.
         *
         * **Two numbers in this rule come from outside 1.13 and both are deliberate.**
         * The corpus trigger is `one or more areas fully drained` and names no figure at
         * all. The three inside `queueDrainedAreaIds` is the Pulse family's stage 1 header,
         * `queue of three to four drained`, borrowed across volumes; a clean sweep of one
         * queued item is not what `A clean sweep` and `All the way through` claim, so the
         * floor stays. The completions floor is this file's own and is the guard
         * [drainedByFinishing] makes exact for the two rules whose subject is the area.
         * **It is window scoped and the sentence is area scoped**: `hd.clear.04` is
         * `{areaName} finished everything`, and two completions anywhere in the week do
         * not establish that the named area produced any of them. Tightening it needs the
         * criterion to agree with `SlotBindings`, which picks the named area itself, and
         * that coupling is worth writing only once the boundary fact is fixed and this
         * family can reach a person at all.
         */
        report("report.headline.clearing", Purpose.REPORT_HEADLINE, "clearing", 1, WEEK_HORIZON, criteria = listOf(
            window("headline.clearing.drained", "an area's queue emptied this week") {
                it.rollup.queueDrainedAreaIds.isNotEmpty()
            },
            window("headline.clearing.completions", "there are completions behind it") { it.window.completions >= 2 },
        )),
        report("report.headline.steadyPace", Purpose.REPORT_HEADLINE, "steadyPace", 1, PATTERN_HORIZON, criteria = listOf(
            window("headline.steady", "the last three weeks sit inside a narrow band") {
                withinBand(it.history.weekTotalEventsSeries, 3, STEADY_BAND)
            },
            threeWeeksOfData,
            window("headline.steady.notQuiet", "the band is a pace rather than a run of empty weeks") {
                !isQuietWeek(it.window.totalEvents, it.window.dayCount)
            },
        )),
        /**
         * `fragmented`, dark across 451 report windows, and the rule is a faithful reading
         * of 1.15.
         *
         * The corpus trigger is `many events, few completions, high switching` and the
         * three criteria are those three things in that order, over the week the headline
         * describes. There is no window error to find: every one of the eight lines is
         * about a week and the window is the week.
         *
         * **What is missing is a persona, not a threshold.** The switching criterion held
         * ten times in the run and never beside the other two, because no simulated life
         * both hoards and switches. `QueueHoarder` clears the first two criteria most
         * weeks, roughly twelve events against one completion, and never swaps once all
         * year; `HeavySingleArea` is the only persona that swaps at all and it finishes
         * three or four things a week, which fails `fewCompletions`. The shape this
         * headline is for, a week of starting things and putting them down again, is
         * exactly the week nobody in section 12 lives.
         *
         * The two swaps are the same count `switchingBehavior` calls high, read at the same
         * grain, and the ids are kept apart on purpose: `RuleCatalogTest` requires one
         * criterion id to mean one condition, and the Pulse `switching` family counts an
         * area's own swaps rather than the window's.
         */
        report("report.headline.fragmented", Purpose.REPORT_HEADLINE, "fragmented", 1, WEEK_HORIZON, criteria = listOf(
            window("headline.frag.manyEvents", "the week was busy") { it.window.totalEvents >= 10 },
            window("headline.frag.fewCompletions", "very little of it closed") { it.window.completions <= 2 },
            window("headline.frag.switching", "the active item changed more than once") { it.window.swaps >= 2 },
        )),
        report("report.headline.firstWeek", Purpose.REPORT_HEADLINE, "firstWeek", 1, WEEK_HORIZON, priority = 5, criteria = listOf(
            window("headline.first.isFirst", "this is the first week of data there has ever been") {
                it.history.isFirstWeekEver
            },
            window("headline.first.notEmpty", "something happened in it") { it.window.totalEvents >= 1 },
        )),
        /**
         * The fallback, and the one rule in the catalog whose criteria are meant to pass
         * most of the time. 1.17 says of it, in the corpus, `Never absent. Used when
         * nothing else qualifies.` Its priority is the lowest in the catalog so anything
         * with something to say outranks it, and [CatalogIntegrity] exempts it by name
         * from the criterion discrimination expectation rather than letting it quietly
         * pass a test it was never going to pass.
         */
        report("report.headline.datedFallback", Purpose.REPORT_HEADLINE, "datedFallback", 1, WEEK_HORIZON, priority = -100, criteria = listOf(
            window("headline.fallback.hasWeek", "there is a week to name") { it.history.weeksOfData >= 1 },
            window("headline.fallback.notEmpty", "something happened in it") { it.window.totalEvents >= 1 },
        )),
    )

    // ---------------------------------------------------------- observations

    private fun observations(): List<ClarityRule> = listOf(
        report("report.observation.singleFocus.s1", Purpose.REPORT_OBSERVATION, "singleFocus", 1, WEEK_HORIZON, criteria = listOf(
            window("$SHARE_READING_PREFIX.single.80to89", "one area holds eighty to eighty nine percent of the week") {
                it.rollup.dominantAreaId != null && it.rollup.dominantShare >= 0.80 && it.rollup.dominantShare < 0.90
            },
            shareFloor(5),
        )),
        report("report.observation.singleFocus.s2", Purpose.REPORT_OBSERVATION, "singleFocus", 2, WEEK_HORIZON, criteria = listOf(
            window("$SHARE_READING_PREFIX.single.90plus", "one area holds ninety percent of the week or more") {
                it.rollup.dominantAreaId != null && it.rollup.dominantShare >= 0.90
            },
            shareFloor(5),
        )),
        report("report.observation.intakeVsOutput.s1", Purpose.REPORT_OBSERVATION, "intakeVsOutput", 1, WEEK_HORIZON, criteria = listOf(
            window("flow.gap.2to4", "the gap between intake and output is two to four either way") {
                val gap = it.window.additions - it.window.completions
                gap in 2..4 || gap in -4..-2
            },
            window("flow.hasBoth", "both sides have a number, so neither renders as zero") {
                it.window.additions >= 1 && it.window.completions >= 1
            },
        )),
        report("report.observation.intakeVsOutput.s2", Purpose.REPORT_OBSERVATION, "intakeVsOutput", 2, WEEK_HORIZON, criteria = listOf(
            window("flow.towardIntake", "intake exceeds output by five or more") {
                (it.window.additions - it.window.completions) >= CLEAR_IMBALANCE
            },
            window("flow.additions", "there are additions to count") { it.window.additions >= CLEAR_IMBALANCE },
        )),
        report("report.observation.intakeVsOutput.s3", Purpose.REPORT_OBSERVATION, "intakeVsOutput", 3, WEEK_HORIZON, criteria = listOf(
            window("flow.towardOutput", "output exceeds intake by five or more") {
                it.window.netFlow >= CLEAR_IMBALANCE
            },
            window("flow.completions", "there are completions to count") { it.window.completions >= CLEAR_IMBALANCE },
        )),
        report("report.observation.focusInvestment.s1", Purpose.REPORT_OBSERVATION, "focusInvestment", 1, WEEK_HORIZON, criteria = listOf(
            window("focus.sessions.1to3", "one to three focus sessions finished") { it.window.focusCompleted in 1..3 },
            window("focus.minutes", "there are minutes to count") { it.window.focusMinutesTotal >= 1 },
        )),
        report("report.observation.focusInvestment.s2", Purpose.REPORT_OBSERVATION, "focusInvestment", 2, WEEK_HORIZON, criteria = listOf(
            window("focus.sessions.4to7", "four to seven focus sessions finished") { it.window.focusCompleted in 4..7 },
            window("focus.minutes", "there are minutes to count") { it.window.focusMinutesTotal >= 1 },
        )),
        report("report.observation.focusInvestment.s3", Purpose.REPORT_OBSERVATION, "focusInvestment", 3, WEEK_HORIZON, criteria = listOf(
            window("focus.sessions.8plus", "eight or more focus sessions finished") { it.window.focusCompleted >= 8 },
            window("focus.minutes", "there are minutes to count") { it.window.focusMinutesTotal >= 1 },
        )),
        /**
         * `neglectedArea` names an area with no events in the week, which is what put it
         * against validator check 1 in section 8. The conflict was real and it was a
         * specification error rather than a rule defect: prohibition 1 forbids naming an
         * area with zero events **in the window under consideration**, and for this family
         * the window under consideration is the neglect window rather than the report week.
         *
         * **The check is now narrowed rather than the rule loosened.** [ClarityRule.absenceSubject]
         * says the subject of this family is the absence itself, and check 1 answers that
         * by requiring the area to have a real history rather than by requiring events in
         * the week. The two criteria below carry that same history requirement, so the rule
         * cannot select an area the check would then refuse.
         */
        report("report.observation.neglectedArea.s1", Purpose.REPORT_OBSERVATION, "neglectedArea", 1, PATTERN_HORIZON, Subjects.AREA, absenceSubject = true, criteria = listOf(
            area("neglect.days.7to13", "the area has been silent seven to thirteen days") {
                it.daysSinceLastEvent in 7..13
            },
            area("neglect.hasHistory", "the area has real history, so this is a silence and not a new area") {
                it.lifetimeEvents >= 5 && !it.isNew
            },
        )),
        report("report.observation.neglectedArea.s2", Purpose.REPORT_OBSERVATION, "neglectedArea", 2, PATTERN_HORIZON, Subjects.AREA, absenceSubject = true, criteria = listOf(
            area("neglect.days.14plus", "the area has been silent fourteen days or more") {
                it.daysSinceLastEvent >= 14 && it.daysSinceLastEvent != Int.MAX_VALUE
            },
            area("neglect.hasHistory", "the area has real history, so this is a silence and not a new area") {
                it.lifetimeEvents >= 5 && !it.isNew
            },
        )),
        report("report.observation.completionSplit", Purpose.REPORT_OBSERVATION, "completionSplit", 1, WEEK_HORIZON, criteria = listOf(
            window("split.answers.3", "three or more pulses were answered in the window") {
                it.pulse.answeredInWindow >= 3
            },
            window("split.completions", "there are completions for the answers to be about") {
                it.window.completions >= 1
            },
        )),
        /**
         * The flagship. It quotes what the user said and sets it against what happened,
         * so it carries a real [CallbackRequirement] rather than a criterion: 5 resolves
         * the callback before the rule can qualify, and a rule with an unresolvable
         * callback does not fire and never degrades into a version without the quote.
         */
        report("report.observation.selfReportVsData", Purpose.REPORT_OBSERVATION, "selfReportVsData", 1, PATTERN_HORIZON, Subjects.ACTIVE_ITEM,
            callback = CallbackRequirement(family = "persistence", withinDays = 30, responseKey = null, subjectMustMatch = true),
            criteria = listOf(
                activeItemAge("srvd.stillActive", "the item the user answered about is still active") { it >= 3 },
                holdingAreaHasEvents(),
                window("srvd.hasAnswers", "there is a stored answer to quote") { it.pulse.answeredLifetime >= 1 },
            )),
        report("report.observation.quietWeek", Purpose.REPORT_OBSERVATION, "quietWeek", 1, WEEK_HORIZON, criteria = listOf(
            window("obs.quiet", "the week holds fewer events than it has days") {
                isQuietWeek(it.window.totalEvents, it.window.dayCount)
            },
            window("obs.quiet.notEmpty", "something happened, so the week is quiet rather than absent") {
                it.window.totalEvents >= 1
            },
        )),
        report("report.observation.queuePressure", Purpose.REPORT_OBSERVATION, "queuePressure", 1, WEEK_HORIZON, criteria = listOf(
            window("obs.queue.growing", "at least one area's queue grew") { it.rollup.queueGrowingAreaIds.isNotEmpty() },
            window("obs.queue.holds", "the queues hold three or more things to count") { it.totalQueueLength() >= 3 },
        )),
        /**
         * The dormancy lead. Its subject is the gap the area came back from, so it is
         * flagged like the two silence families, and unlike them it also carries
         * [areaHasEvents]: an area that returned moved inside the window by definition.
         *
         * **The flag is therefore inert as this rule stands**, and it is here anyway,
         * because it records what the family is about. Check 1 only consults it on an area
         * with no events in the window and this rule cannot select one, so the flag costs
         * nothing and stops the next author who widens the return window from rediscovering
         * the same 107 vetoes.
         */
        report("report.observation.areaRevival", Purpose.REPORT_OBSERVATION, "areaRevival", 1, PATTERN_HORIZON, Subjects.AREA, absenceSubject = true, criteria = listOf(
            criterion("revival.returned", "this area returned after a dormancy") { facts, subject ->
                subject != null && subject.id in facts.rollup.dormantReturnedAreaIds
            },
            areaHasEvents(),
        )),
        /**
         * `persistentItem`, split into a low and a high rule so 7.4's qualification
         * survives a corpus family that has one stage. See [UnflatteringRules]. The split
         * point is fourteen days, which is where `persistence` stage 3 begins in
         * `CORPUS_1_PULSE.md`.
         */
        report("report.observation.persistentItem.low", Purpose.REPORT_OBSERVATION, "persistentItem", 1, PATTERN_HORIZON, Subjects.ACTIVE_ITEM,
            unflattering = false,
            criteria = listOf(
                activeItemAge("persistentItem.age.7to13", "the item has been active seven to thirteen days") { it in 7..13 },
                holdingAreaHasEvents(),
            )),
        report("report.observation.persistentItem.high", Purpose.REPORT_OBSERVATION, "persistentItem", 1, PATTERN_HORIZON, Subjects.ACTIVE_ITEM,
            priority = 1,
            unflattering = true,
            criteria = listOf(
                activeItemAge("persistentItem.age.14plus", "the item has been active fourteen days or more") { it >= 14 },
                holdingAreaHasEvents(),
                window("persistentItem.hasMedian", "there is a median completion time to compare against") {
                    it.items.medianDaysToComplete != null
                },
            )),
        report("report.observation.personalBest", Purpose.REPORT_OBSERVATION, "personalBest", 1, RECORD_HORIZON, criteria = listOf(
            window("obs.best.noBetterWeek", "no earlier week strictly beats this one") {
                it.history.mostRecentBetterWeekKey == null
            },
            window("obs.best.beatsTheRecord", "this week reaches the personal best") {
                it.window.completions >= it.history.personalBestWeekCompletions
            },
            window("obs.best.hasHistory", "there is more than one week to be best of") { it.history.weeksOfData >= 2 },
            window("obs.best.completions", "there are completions, so no count renders as zero") {
                it.window.completions >= 1
            },
        )),
        report("report.observation.mostActiveSince", Purpose.REPORT_OBSERVATION, "mostActiveSince", 1, RECORD_HORIZON, criteria = listOf(
            window("obs.since.hasNamedPoint", "there is an earlier week that strictly beats this one to name") {
                it.history.mostRecentBetterWeekKey != null
            },
            window("obs.since.beatsRecent", "this week beats the one before it") {
                (it.history.weekOverWeekDelta ?: 0) > 0
            },
            window("obs.since.hasHistory", "there are three weeks to reach back across") { it.history.weeksOfData >= 3 },
        )),
        report("report.observation.dayShape", Purpose.REPORT_OBSERVATION, "dayShape", 1, WEEK_HORIZON, criteria = listOf(
            window("dayShape.hasBusiestDay", "one day of the week stands out and can be named") {
                it.window.busiestDayKey != null && it.window.busiestDayCount >= 3
            },
            window("dayShape.concentrated", "that day holds a third of the week or more") {
                it.window.totalEvents >= 6 && it.window.busiestDayCount * 3 >= it.window.totalEvents
            },
        )),
        report("report.observation.timeOfDay", Purpose.REPORT_OBSERVATION, "timeOfDay", 1, WEEK_HORIZON, criteria = listOf(
            window("timeOfDay.hasBand", "one part of the day holds most of the week") {
                val counts = it.window.eventsByPartOfDay
                val top = counts.values.maxOrNull() ?: 0
                it.window.totalEvents >= 6 && top * 2 >= it.window.totalEvents
            },
            window("timeOfDay.spread", "the week was not confined to a single part of one day") {
                it.window.eventsByPartOfDay.count { entry -> entry.value > 0 } >= 2
            },
        )),
        /**
         * `switchingBehavior`, split low and high for the same reason as `persistentItem`.
         * The split point is two swaps, which is where the Pulse `switching` family's
         * stage 2 begins.
         *
         * Subject is `NONE` rather than the area, and it stays that way now that
         * `AreaFacts.swapsInWindow` exists. The fact was the reason it could not be an
         * area rule; it is not on its own a reason to make it one. The Pulse `switching`
         * family is the per area reading of the same behavior and it now has both stages,
         * so an area subject here would put two families on one fact at two grains, which
         * 9.1 exists to prevent. What is left for this family is a binding: six of its
         * nine leads name an area and `SlotBindings` declares nothing for `{areaName}`
         * here, so those six stay out of the bench either way.
         */
        report("report.observation.switchingBehavior.low", Purpose.REPORT_OBSERVATION, "switchingBehavior", 1, WEEK_HORIZON,
            unflattering = false,
            criteria = listOf(
                window("switching.swaps.1", "the active item changed once") { it.window.swaps == 1 },
                window("switching.hasAreas", "there is an area with activity behind it") { it.rollup.areasWithEvents >= 1 },
            )),
        report("report.observation.switchingBehavior.high", Purpose.REPORT_OBSERVATION, "switchingBehavior", 1, WEEK_HORIZON,
            priority = 1,
            unflattering = true,
            criteria = listOf(
                window("switching.swaps.2plus", "the active item changed twice or more") { it.window.swaps >= 2 },
                window("switching.hasAreas", "there is an area with activity behind it") { it.rollup.areasWithEvents >= 1 },
            )),
        report("report.observation.focusAbandonment", Purpose.REPORT_OBSERVATION, "focusAbandonment", 1, WEEK_HORIZON, criteria = listOf(
            window("abandon.some", "at least two sessions ended early") { it.window.focusEndedEarly >= 2 },
            window("abandon.started", "there are started sessions to set them against") { it.window.focusStarted >= 3 },
        )),
        /**
         * `queueDrained`, and the two things that were wrong with it.
         *
         * **`drained.hadAQueue` was padding and is gone.** `RollupFacts.queueDrainedAreaIds`
         * is defined as a queue of three or more at the window start that is zero at the
         * window end, so membership in that list already carries the starting queue.
         * Restating it as a second criterion could never separate one fact set from
         * another and bought a free point of specificity, which is the one number
         * `ClarityRule` says must never be authored. Section 14's discrimination report
         * cannot see this shape, because it measures each criterion's pass rate across the
         * whole corpus rather than conditionally on the rest of its own rule.
         *
         * **The completions guard is new and is not a threshold.** Nothing in the drain
         * facts asks how the items left, and a queue also empties when its items are
         * deleted. `ob.drain.l02` says `{areaName} cleared its entire queue this week`,
         * which is false of somebody who threw five things away. [drainedByFinishing]
         * carries the reasoning and the Pulse family reads the same criterion, so one id
         * means one condition across both grains.
         *
         * **Why it was dark is the anchoring error `PulseRules.queueDrain` carried**, which
         * survived the move to a week. The queue was read at the two window boundaries, so
         * an area that built a queue on Tuesday and finished it on Saturday read as no
         * drain at all. `CORPUS_2_REPORT.md` 1.13 states the trigger for `clearing` as `one
         * or more areas fully drained` and states no boundary, and 2.17 says `cleared its
         * entire queue this week` rather than `had a queue on Monday`. Both now read
         * `AreaFacts.queueDrainedFrom` through the rollup, and `PulseRules.queueDrain`
         * carries the account of the fact.
         *
         * **One lead of this bench does date itself and is handled where a line is handled.**
         * `ob.drain.l01`, *It held {n} things on Sunday*, is the only sentence in either
         * volume that names the boundary, and it is false whenever the fall began after it.
         * `SlotBindings` overrides `{n}` there to a measure that answers only when the fall
         * began at or before the window opened, so the line drops off the bench rather than
         * renders a number nobody could fault about a Sunday that held nothing. A criterion
         * could not do this: it would take the whole family down with the one line.
         */
        report("report.observation.queueDrained", Purpose.REPORT_OBSERVATION, "queueDrained", 1, WEEK_HORIZON, Subjects.AREA, criteria = listOf(
            criterion("drained.area", "this area's queue emptied this week") { facts, subject ->
                subject != null && subject.id in facts.rollup.queueDrainedAreaIds
            },
            drainedByFinishing(),
            areaHasEvents(),
        )),
        report("report.observation.steadyPace", Purpose.REPORT_OBSERVATION, "steadyPace", 1, PATTERN_HORIZON, criteria = listOf(
            window("obs.steady", "the last three weeks sit inside a narrow band") {
                withinBand(it.history.weekTotalEventsSeries, 3, STEADY_BAND)
            },
            threeWeeksOfData,
            window("obs.steady.notQuiet", "the band is a pace rather than a run of empty weeks") {
                !isQuietWeek(it.window.totalEvents, it.window.dayCount)
            },
        )),
        report("report.observation.firstMilestone", Purpose.REPORT_OBSERVATION, "firstMilestone", 1, WEEK_HORIZON, priority = 3, criteria = listOf(
            window("milestone.firstEver", "something happened this window for the first time ever") {
                it.history.firstEverFlags.isNotEmpty()
            },
            window("milestone.notEmpty", "the window has activity behind the milestone") { it.window.totalEvents >= 1 },
        )),
        report("report.observation.areaBalance", Purpose.REPORT_OBSERVATION, "areaBalance", 1, WEEK_HORIZON, criteria = listOf(
            window("balance.areas", "three or more areas had activity") { it.rollup.areasWithEvents >= 3 },
            window("$SHARE_READING_PREFIX.balance.even", "no area holds half the week") { it.rollup.dominantShare < 0.50 },
            shareFloor(6),
        )),
        /**
         * The difficulty register, 6.4. Three or more consecutive quiet weeks combined
         * with growing queues, or a sustained decline across four weeks.
         *
         * Its remaining constraints are not criteria and cannot be: firing at most once
         * every six weeks is the cooldown in 7.3, never appearing beside a plan or beside
         * `selfReportVsData` is the incompatibility matrix in 9.1, and never naming or
         * inferring an emotional state is a property of the eight authored lines.
         */
        report("report.observation.hardStretch.quiet", Purpose.REPORT_OBSERVATION, "hardStretch", 1, PATTERN_HORIZON, criteria = listOf(
            window("hard.quietWeeks", "three or more weeks running below one event a day") {
                val weeks = tail(it.history.weekTotalEventsSeries, 3)
                weeks != null && weeks.all { events -> isQuietWeek(events, it.window.dayCount) }
            },
            window("hard.queuesGrowing", "and the queues grew across them") {
                strictlyRising(it.history.weekQueueSizeSeries, 3)
            },
            threeWeeksOfData,
        )),
        report("report.observation.hardStretch.decline", Purpose.REPORT_OBSERVATION, "hardStretch", 1, PATTERN_HORIZON, criteria = listOf(
            window("hard.fourWeekDecline", "a sustained decline across four weeks") {
                strictlyFalling(it.history.weekTotalEventsSeries, 4)
            },
            window("hard.fourWeeksOfData", "there are four weeks to see it across") { it.history.weeksOfData >= 4 },
        )),
    )

    // -------------------------------------------------------------- patterns

    private fun patterns(): List<ClarityRule> = listOf(
        report("report.pattern.shiftingFocus", Purpose.REPORT_PATTERN, "shiftingFocus", 1, PATTERN_HORIZON, criteria = listOf(
            window("pattern.shifting", "a different area led each of the last three weeks") {
                val leaders = it.history.dominantAreaLastThreeWeeks
                leaders.size == 3 && leaders.none { id -> id == null } && leaders.toSet().size == 3
            },
            threeWeeksOfData,
        )),
        report("report.pattern.growingQueues", Purpose.REPORT_PATTERN, "growingQueues", 1, PATTERN_HORIZON, criteria = listOf(
            window("pattern.growing", "the queues have grown three weeks running") {
                strictlyRising(it.history.weekQueueSizeSeries, 3)
            },
            threeWeeksOfData,
        )),
        report("report.pattern.improvingThroughput", Purpose.REPORT_PATTERN, "improvingThroughput", 1, PATTERN_HORIZON, criteria = listOf(
            window("pattern.improving", "completions have risen three weeks running") {
                strictlyRising(it.history.weekCompletionsSeries, 3)
            },
            threeWeeksOfData,
        )),
        report("report.pattern.decliningActivity", Purpose.REPORT_PATTERN, "decliningActivity", 1, PATTERN_HORIZON, criteria = listOf(
            window("pattern.declining", "total activity has fallen three weeks running") {
                strictlyFalling(it.history.weekTotalEventsSeries, 3)
            },
            threeWeeksOfData,
        )),
        report("report.pattern.areaGoneQuiet", Purpose.REPORT_PATTERN, "areaGoneQuiet", 1, PATTERN_HORIZON, Subjects.AREA, absenceSubject = true, criteria = listOf(
            area("pattern.gone.threeWeeks", "the area has had nothing in it for three weeks") {
                it.daysSinceLastEvent in 21..(PATTERN_HORIZON * 2)
            },
            area("pattern.gone.hadHistory", "it used to move, so this is a stop rather than an empty heading") {
                it.lifetimeEvents >= 5 && !it.isNew
            },
            threeWeeksOfData,
        )),
        report("report.pattern.consistentRhythm", Purpose.REPORT_PATTERN, "consistentRhythm", 1, PATTERN_HORIZON, criteria = listOf(
            window("pattern.rhythm", "four weeks inside a narrow band") {
                withinBand(it.history.weekTotalEventsSeries, 4, RHYTHM_BAND)
            },
            window("pattern.rhythm.weeks", "there are four weeks to see it across") { it.history.weeksOfData >= 4 },
            window("pattern.rhythm.notQuiet", "the band is a rhythm rather than a run of empty weeks") {
                !isQuietWeek(it.window.totalEvents, it.window.dayCount)
            },
        )),
        report("report.pattern.reportedVsActual", Purpose.REPORT_PATTERN, "reportedVsActual", 1, PATTERN_HORIZON, criteria = listOf(
            window("pattern.rva.answers", "there are several stored answers to compare against") {
                it.pulse.answeredLifetime >= 5
            },
            window("pattern.rva.repeated", "one family has been answered about more than twice") {
                it.pulse.answersByFamily.values.any { answers -> answers.size >= 3 }
            },
            threeWeeksOfData,
        )),
        report("report.pattern.queueEquilibrium", Purpose.REPORT_PATTERN, "queueEquilibrium", 1, PATTERN_HORIZON, criteria = listOf(
            window("pattern.equilibrium", "the queues have held the same length for four weeks") {
                withinBand(it.history.weekQueueSizeSeries, 4, EQUILIBRIUM_BAND)
            },
            window("pattern.equilibrium.weeks", "there are four weeks to see it across") {
                it.history.weeksOfData >= 4
            },
            window("pattern.equilibrium.notEmpty", "there is a queue to be in balance") { it.totalQueueLength() >= 2 },
        )),
        /**
         * `narrowingFocus`. The area count has fallen every week for three weeks.
         *
         * **Line 01 of a pattern family states its trigger, and that convention decides
         * every rule below.** `pt.narrow.01` reads `Your attention has concentrated
         * further each week for three weeks`, `pt.broad.01` says the same in the other
         * direction, `pt.hab.01` names appearing every week and `pt.ab.01` names three
         * weeks running. A bench is chosen from freely, so the rule has to make every
         * line in it true, and line 01 is the line that says which claim that is.
         *
         * The second criterion is what keeps a narrowing apart from a stop. A series of
         * three, two, zero is strictly falling and the week behind it had nothing in it,
         * and `The spread keeps shrinking` about an empty week is a `quietWeek` sentence
         * wearing the wrong family's clothes.
         *
         * **Four of the seven pattern families this phase gave a rule hold one line the
         * rule cannot make true**, and each is recorded on its own family below. They are
         * lines that name a quantity no fact carries rather than lines the rule is too
         * weak for, so a stronger criterion would not reach them. The mechanism for
         * holding a line out of a bench is `SlotBindings.EXCLUDED`, which belongs to the
         * realizer and not here, so each is named where a reader of the rule will find it
         * and none is silently tolerated.
         */
        report("report.pattern.narrowingFocus", Purpose.REPORT_PATTERN, "narrowingFocus", 1, PATTERN_HORIZON, criteria = listOf(
            window("pattern.narrowing", "fewer areas have moved in each of the last three weeks") {
                strictlyFalling(it.history.weekAreaCountSeries, 3)
            },
            window("pattern.narrowing.stillMoving", "an area still moved this week, so this is a narrowing and not a stop") {
                (it.history.weekAreaCountSeries.lastOrNull() ?: 0) >= 1
            },
            threeWeeksOfData,
        )),
        /**
         * `broadeningFocus`, the mirror of the one above, over the same series.
         *
         * **`pt.broad.03`, `No area has held a majority for three weeks`, is not made true
         * by this rule and no criterion could make it so.** A rising area count says
         * nothing about shares: two areas becoming four is compatible with one of them
         * holding sixty percent throughout. It needs a dominant share per week, and
         * `HistoryFacts` carries the leader of each of the last three weeks by id and not
         * its share. The line waits for that fact, exactly as this family waited for the
         * area count.
         */
        report("report.pattern.broadeningFocus", Purpose.REPORT_PATTERN, "broadeningFocus", 1, PATTERN_HORIZON, criteria = listOf(
            window("pattern.broadening", "more areas have moved in each of the last three weeks") {
                strictlyRising(it.history.weekAreaCountSeries, 3)
            },
            threeWeeksOfData,
        )),
        /**
         * `focusHabitForming`, over sessions **started** in each week.
         *
         * `HistoryFacts.weekFocusStartedSeries` is deliberately not the finished count the
         * register asked for, and its own documentation says why: a person who starts five
         * every week and finishes fewer each time has a habit and a finishing problem, and
         * `abandonmentPattern` is the family that speaks about the second one.
         *
         * Four weeks rather than three, because `pt.hab.04`, `pt.hab.06` and `pt.hab.07`
         * all claim a month and the bench is chosen from freely. Four consecutive weeks
         * makes the three week lines true as well; three would leave three lines in the
         * bench claiming a month that had not happened.
         *
         * **`pt.hab.03`, `Protected time has increased every week for three weeks`, is not
         * made true by this rule.** Protected time is minutes and this series is sessions,
         * and more sessions is not more minutes: five ten minute sessions are fewer
         * minutes than three forty minute ones. It needs a weekly focus minutes series,
         * which 3.1 does not declare, and requiring a rising session count here would put
         * a number behind the line without making it true.
         */
        report("report.pattern.focusHabitForming", Purpose.REPORT_PATTERN, "focusHabitForming", 1, PATTERN_HORIZON, criteria = listOf(
            window("pattern.habit.everyWeek", "a focus session was started in each of the last four weeks") {
                tail(it.history.weekFocusStartedSeries, 4)?.all { started -> started >= 1 } == true
            },
            window("pattern.habit.weeks", "there are four weeks to see it across") { it.history.weeksOfData >= 4 },
        )),
        /**
         * `focusHabitFading`, over the same started count falling.
         *
         * **This family holds two claims that cannot both be true, and the rule implements
         * the one line 01 states.** `pt.fade.01` says sessions have fallen every week for
         * three weeks; `pt.fade.04` says there has been no focus time in two weeks. A
         * series can satisfy either and never both: falling strictly across three weeks
         * requires the last two to differ, and two weeks of nothing requires them to be
         * equal. So `pt.fade.04` is out of reach of any rule for this family, and it is a
         * corpus split rather than a missing fact: the shape it describes is real and
         * belongs to a family of its own.
         *
         * The second criterion is what makes a fall a fading. A series of two, one, zero
         * is strictly falling from a person who started twice, and `Focus sessions have
         * fallen every week for three weeks` about that is technically true and reads as
         * an accusation about a habit that never existed.
         */
        report("report.pattern.focusHabitFading", Purpose.REPORT_PATTERN, "focusHabitFading", 1, PATTERN_HORIZON, criteria = listOf(
            window("pattern.fading", "fewer focus sessions were started in each of the last three weeks") {
                strictlyFalling(it.history.weekFocusStartedSeries, 3)
            },
            window("pattern.fading.hadAHabit", "the oldest of those weeks held sessions, so something is fading rather than never having started") {
                (tail(it.history.weekFocusStartedSeries, 3)?.firstOrNull() ?: 0) >= 2
            },
            threeWeeksOfData,
        )),
        /**
         * `weekendShift`. Nothing on a Saturday or a Sunday for four weeks.
         *
         * **A report pattern reads `HistoryFacts` here and not `CueFacts`, and the
         * decision was taken in the facts phase rather than here.** 3.7 restricts the cue
         * facts to layer 6, and `weekdayOnly` there is a twelve week weekend share under a
         * ceiling: it cannot substantiate a claim of nothing, and it answers over twelve
         * weeks a family that speaks about four. `weekWeekendEventsSeries` is the fact
         * that was declared instead, and its own documentation carries the reasoning.
         *
         * The second criterion is the one that stops this from firing on an empty month.
         * Four weekends with nothing in them is only a shape if the weekdays had
         * something, and `Your weeks end on Friday, consistently` about somebody who did
         * nothing at all for four weeks is a false claim with correct arithmetic behind
         * it.
         *
         * **It qualified once in eleven simulated years and the window is right.** The
         * family speaks about four weeks, the fact is four weekly buckets and the horizon
         * reaches them. The reason it is silent is that no persona in section 12 knows
         * what day of the week it is: every one of them acts off a day index, so weekend
         * days carry the same load as weekdays and four consecutive empty weekends can
         * only happen by coincidence, which is what the single occurrence was.
         *
         * **The bench is wider than this rule and phase 9 owns the difference.** Three of
         * the five lines claim absolute silence, `Nothing has happened on a weekend in
         * four weeks`, and two claim a share, `{pct} of your activity has been on weekdays
         * for a month` and `Your weeks end on Friday, consistently`. The rule encodes the
         * strict branch, which is the safe direction: under it all five lines are true,
         * and `{pct}` renders as a hundred. A second rule for the share branch would be
         * the 7.3 device for a compound condition and it is **not** available here,
         * because both rules would share one bench and the looser one could select
         * `Nothing has happened on a weekend in four weeks` for a month that had a
         * weekend in it. Splitting the bench is authoring work and comes after this pass.
         */
        report("report.pattern.weekendShift", Purpose.REPORT_PATTERN, "weekendShift", 1, PATTERN_HORIZON, criteria = listOf(
            window("pattern.weekend.silent", "no event fell on a Saturday or a Sunday in any of the last four weeks") {
                tail(it.history.weekWeekendEventsSeries, 4)?.all { weekend -> weekend == 0 } == true
            },
            window("pattern.weekend.weekdaysMoved", "each of those four weeks held activity, so the weekends are a shape and not an empty month") {
                tail(it.history.weekTotalEventsSeries, 4)?.all { events -> events >= 1 } == true
            },
            window("pattern.weekend.weeks", "there are four weeks to see it across") { it.history.weeksOfData >= 4 },
        )),
        /**
         * `abandonmentPattern`. More sessions ended early than finished, three weeks
         * running, which is `pt.ab.01` word for word.
         *
         * **The two series are compared and never subtracted.** A killed process leaves a
         * session with no terminal event, which is a legal state and in neither count, and
         * inferring an early ending from started minus finished would attribute it to an
         * ending the person did not choose. `HistoryFacts.weekFocusEndedEarlySeries` says
         * the same thing about itself.
         *
         * **`pt.ab.02`, `Sessions have been getting shorter each week`, is not made true by
         * this rule.** It is about the length of a session and every fact here is a count
         * of them. A weekly focus minutes series and a weekly session count would together
         * give a mean length, and neither the series nor the division exists.
         */
        report("report.pattern.abandonmentPattern", Purpose.REPORT_PATTERN, "abandonmentPattern", 1, PATTERN_HORIZON, criteria = listOf(
            window("pattern.abandon.threeWeeks", "more sessions ended early than finished in each of the last three weeks") {
                val early = tail(it.history.weekFocusEndedEarlySeries, 3)
                val finished = tail(it.history.weekFocusCompletedSeries, 3)
                early != null && finished != null && early.zip(finished).all { (e, f) -> e > f }
            },
            window("pattern.abandon.enoughSessions", "each of those weeks started at least two sessions, so the comparison is over something") {
                tail(it.history.weekFocusStartedSeries, 3)?.all { started -> started >= 2 } == true
            },
            threeWeeksOfData,
        )),
        /**
         * `comebackPattern`. This area has gone quiet and come back twice.
         *
         * Counted over the area's own weekly series, which is the only fact that can see a
         * second return: `RollupFacts.dormantReturnedAreaIds` describes one window and
         * therefore sees at most one. [returnsAfterSilence] carries the counting and the
         * reason its leading zeros are skipped.
         *
         * Five weeks rather than the section's three, because two returns need at least
         * five buckets to sit in: active, quiet, active, quiet, active. Three weeks of
         * data cannot hold the pattern this family names, so requiring three would be a
         * criterion that never decides anything.
         *
         * **`pt.come.04`, `{areaName} has never been active two weeks in a row`, is not
         * made true by this rule.** Two returns say the area went quiet twice; they say
         * nothing about whether it ever ran two weeks together, and a series of three,
         * four, zero, two, zero, five satisfies the rule and contradicts the line.
         *
         * **Every line of this family names its area and `SlotBindings` binds nothing for
         * it**, so the family qualifies and produces no sentence until that entry exists.
         * That is the same shape the family was in before this rule, and the rule is the
         * half of it that belongs here.
         */
        report("report.pattern.comebackPattern", Purpose.REPORT_PATTERN, "comebackPattern", 1, PATTERN_HORIZON, Subjects.AREA, criteria = listOf(
            area("pattern.comeback.twice", "this area has gone quiet and come back twice in its weekly series") {
                returnsAfterSilence(it.weekEventsSeries) >= 2
            },
            window("pattern.comeback.weeks", "there are five weeks of history, which is the fewest two returns can sit in") {
                it.history.weeksOfData >= COMEBACK_WEEKS
            },
            areaHasEvents(),
        )),
    )
}
