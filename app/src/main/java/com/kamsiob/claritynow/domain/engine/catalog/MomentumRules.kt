package com.kamsiob.claritynow.domain.engine.catalog

import com.kamsiob.claritynow.domain.engine.FamilyKey

/**
 * The Momentum headline and the Areas banner. CLARITY_LOGIC_ENGINE.md 6.5, and the
 * triggers stated beside each family in `CORPUS_3_MOMENTUM.md`.
 *
 * **Momentum observes and never interprets.** That rule governs the language rather than
 * the rules, and the corpus enforces it with a test over the benches. What it means here
 * is that these rules are the plainest in the catalog: each one reads the shape of the
 * last fourteen days and nothing else. There is no comparison against a plan, no
 * inference about why, and no family whose trigger is a judgment.
 *
 * **Every rule here is `unflattering = false`, and one of them is arguably wrong.** 7.4
 * enumerates the rules that carry the flag and every one is a Report family, so
 * everything here is false by the rule that everything not enumerated is. But
 * `CORPUS_3_MOMENTUM.md`'s own authoring rule 5 says every quiet or low activity line in
 * that file is `[N]`, and twenty six such lines are authored. Under 7.4 as it stands
 * those lines cannot be reached, because the neutral agent register is only ever selected
 * for a rule marked unflattering. Addendum 01 7c widens the enumeration to cover exactly
 * this case and `MASTER_BUILD_PROMPT.md` 14b.10 marks that widening pending in phase 9.
 * This file follows 7.4 as written and the conflict is recorded rather than pre empted.
 */
internal object MomentumRules {

    /** Momentum reads the last fourteen days. */
    private const val FORTNIGHT_HORIZON = 14

    /** The banner reads the week so far. */
    private const val WEEK_HORIZON = 7

    /** The average this week is set against, for `strongPace` and `weekStrong`. */
    private const val AVERAGE_WEEKS = 8

    /**
     * The horizon of a rule that compares against that average.
     *
     * A horizon is the maximum age of the oldest fact a rule references, and eight weeks
     * of weekly completions is eight weeks old. Declaring the surface's own window instead
     * would put the rule one step from being filtered out at step 3 of selection every
     * time it qualified.
     */
    private const val AVERAGE_HORIZON = AVERAGE_WEEKS * 7

    /** `a clear margin` above the recent average. Half again, so a good week is not a record. */
    private const val CLEAR_MARGIN_NUMERATOR = 3

    private const val CLEAR_MARGIN_DENOMINATOR = 2

    val ALL: List<ClarityRule> = momentum() + banner()

    private fun surface(
        key: RuleKey,
        purpose: Purpose,
        family: FamilyKey,
        horizonDays: Int,
        subject: SubjectSelector = Subjects.NONE,
        priority: Int = 0,
        criteria: List<Criterion>,
    ) = ClarityRule(
        key = key,
        purpose = setOf(purpose),
        family = family,
        subject = subject,
        criteria = criteria,
        priority = priority,
        horizonDays = horizonDays,
        unflattering = false,
        stage = 1,
    )

    /** Completions this week set against the average of the weeks before it. */
    private fun aboveRecentAverage(series: List<Int>, thisWeek: Int): Boolean {
        val recent = series.takeLast(AVERAGE_WEEKS)
        if (recent.size < 2) return false
        val average = recent.sum().toDouble() / recent.size
        return thisWeek * CLEAR_MARGIN_DENOMINATOR > average * CLEAR_MARGIN_NUMERATOR
    }

    private fun momentum(): List<ClarityRule> = listOf(
        // **A family whose lines claim a fortnight may not fire before a fortnight
        // exists.** Applied to every FORTNIGHT_HORIZON headline except `firstDays` and
        // `cleanSlate`, which are the two that exist precisely for a young install and
        // an empty one.
        //
        // The window is always fourteen days wide, so `window.dayCount` cannot tell a
        // fortnight of data from a fortnight of window and every one of these families
        // qualified on day two. On the device a two day old install read
        // "A narrow fortnight." from `singleAreaWeek` and, before the first fix,
        // "A still fortnight." from `quietStretch`: sentences about eleven days that
        // had not happened, on the first screen that ever spoke to the person.
        //
        // Addendum 01 7d asks every reflective surface to be honest about what it does
        // not have yet, and CLARITY_LOGIC_ENGINE 1 is blunt about the cost of one wrong
        // number. This is that rule, expressed once.

        surface("momentum.steadyStretch", Purpose.MOMENTUM_HEADLINE, "steadyStretch", FORTNIGHT_HORIZON, criteria = listOf(
            window("momentum.steady.nineDays", "active on nine or more of the last fourteen days") {
                it.window.activeDays >= 9
            },
            window("momentum.steady.fullWindow", "there are fourteen days to have been active across") {
                it.window.dayCount >= FORTNIGHT_HORIZON
            },
            // The window is always fourteen days long. The install is not, and a
            // fortnight family that cannot tell those apart describes days that did
            // not happen. See momentum.quiet.installed for the whole argument.
            fortnightOfHistory(),
        )),
        surface("momentum.quietStretch", Purpose.MOMENTUM_HEADLINE, "quietStretch", FORTNIGHT_HORIZON, criteria = listOf(
            window("momentum.quiet.fourDays", "active on four or fewer of the last fourteen days") {
                it.window.activeDays <= 4
            },
            window("momentum.quiet.notEmpty", "active on at least one, so no day count renders as zero") {
                it.window.activeDays >= 1
            },
            // **A fortnight family may not speak before a fortnight exists.**
            //
            // Without this, a three day old install reads "A still fortnight",
            // which describes eleven days that had not happened. The window is
            // always fourteen days wide, so `window.dayCount` cannot catch it; only
            // the install age can. Found by the phase 7 builder on a fresh install
            // probe rather than by a test, because every fixture in the suite had
            // history behind it.
            //
            // This is the same requirement Addendum 01 7d states for every
            // reflective surface, and CLARITY_LOGIC_ENGINE 1's line about one
            // off number destroying the credibility of everything else applies
            // exactly: a person on day three would have been told something plainly
            // untrue about their own fortnight, on the first screen that ever spoke
            // to them.
            fortnightOfHistory(),
            window("momentum.quiet.fullWindow", "there are fourteen days to have been quiet across") {
                it.window.dayCount >= FORTNIGHT_HORIZON
            },
        )),
        surface("momentum.comeback", Purpose.MOMENTUM_HEADLINE, "comeback", FORTNIGHT_HORIZON, Subjects.AREA, criteria = listOf(
            criterion("momentum.comeback.area", "this area resumed after a gap of five days or more") { facts, subject ->
                subject != null && subject.id in facts.rollup.dormantReturnedAreaIds
            },
            areaHasEvents(),
            fortnightOfHistory(),
        )),
        surface("momentum.balancedWeek", Purpose.MOMENTUM_HEADLINE, "balancedWeek", FORTNIGHT_HORIZON, criteria = listOf(
            window("momentum.balanced.areas", "three or more areas were active") { it.rollup.areasWithEvents >= 3 },
            window("$SHARE_READING_PREFIX.momentum.balanced", "none above half the window") {
                it.rollup.dominantShare < 0.50
            },
            shareFloor(5),
            fortnightOfHistory(),
        )),
        surface("momentum.singleAreaWeek", Purpose.MOMENTUM_HEADLINE, "singleAreaWeek", FORTNIGHT_HORIZON, Subjects.AREA, criteria = listOf(
            area("$SHARE_READING_PREFIX.momentum.single", "this area holds seventy percent of the window or more") {
                it.shareOfEvents >= 0.70
            },
            areaShareFloor(4),
            shareFloor(4),
            fortnightOfHistory(),
        )),
        surface("momentum.strongPace", Purpose.MOMENTUM_HEADLINE, "strongPace", AVERAGE_HORIZON, criteria = listOf(
            window("momentum.pace.aboveAverage", "completions are clearly above the recent weekly average") {
                aboveRecentAverage(it.history.weekCompletionsSeries, it.window.completions)
            },
            window("momentum.pace.completions", "there are completions to count") { it.window.completions >= 3 },
            window("momentum.pace.history", "there are earlier weeks to be above") { it.history.weeksOfData >= 2 },
        )),
        surface("momentum.firstDays", Purpose.MOMENTUM_HEADLINE, "firstDays", FORTNIGHT_HORIZON, priority = 5, criteria = listOf(
            window("momentum.first.new", "fewer than fourteen days since install") {
                it.history.daysSinceInstall < FORTNIGHT_HORIZON
            },
            window("momentum.first.hasSomething", "at least one event, so this is a beginning rather than an empty app") {
                it.window.totalEvents >= 1
            },
        )),
        surface("momentum.cleanSlate", Purpose.MOMENTUM_HEADLINE, "cleanSlate", FORTNIGHT_HORIZON, priority = -10, criteria = listOf(
            window("momentum.clean.noEvents", "no events at all in the window") { it.window.totalEvents == 0 },
            window("momentum.clean.hasAreas", "there are areas, so there is an app to have a clean slate in") {
                it.rollup.areasTotal >= 1
            },
        )),
    )

    /**
     * The five banner families. 6.5 puts the once an hour throttle in the ViewModel rather
     * than in the engine, so none of these carries a cooldown.
     *
     * Three of them are keyed to the point in the week, which the fact set expresses as
     * `window.dayCount` for a week to date window: one or two days in is Monday or
     * Tuesday. That is a derivation from the window rather than a weekday fact, which
     * matters because 3.7 restricts the weekday facts in `CueFacts` to layer 6.
     */
    private fun banner(): List<ClarityRule> = listOf(
        surface("banner.weekStarting", Purpose.AREAS_BANNER, "weekStarting", WEEK_HORIZON, criteria = listOf(
            window("banner.start.earlyInWeek", "one or two days into the week") { it.window.dayCount <= 2 },
            window("banner.start.fewCompletions", "fewer than three completions so far") { it.window.completions < 3 },
        )),
        surface("banner.weekBuilding", Purpose.AREAS_BANNER, "weekBuilding", WEEK_HORIZON, criteria = listOf(
            window("banner.build.midweek", "three to five days into the week") { it.window.dayCount in 3..5 },
            window("banner.build.accumulating", "completions are accumulating") { it.window.completions >= 2 },
        )),
        surface("banner.weekStrong", Purpose.AREAS_BANNER, "weekStrong", AVERAGE_HORIZON, criteria = listOf(
            window("banner.strong.aboveAverage", "completions are clearly above the recent weekly average") {
                aboveRecentAverage(it.history.weekCompletionsSeries, it.window.completions)
            },
            window("banner.strong.completions", "there are completions to count") { it.window.completions >= 3 },
            window("banner.strong.history", "there are earlier weeks to be above") { it.history.weeksOfData >= 2 },
        )),
        surface("banner.weekQuiet", Purpose.AREAS_BANNER, "weekQuiet", WEEK_HORIZON, criteria = listOf(
            window("banner.quiet.low", "fewer events than days so far this week") {
                it.window.totalEvents < it.window.dayCount
            },
            window("banner.quiet.someWeek", "the week is under way, so this is quiet rather than not yet begun") {
                it.window.dayCount >= 2
            },
        )),
        surface("banner.weekMixed", Purpose.AREAS_BANNER, "weekMixed", WEEK_HORIZON, priority = -5, criteria = listOf(
            window("banner.mixed.areas", "two or more areas were active") { it.rollup.areasWithEvents >= 2 },
            window("$SHARE_READING_PREFIX.banner.mixed", "one of them holds half the week or more") {
                it.rollup.dominantShare >= 0.50
            },
            shareFloor(4),
        )),
    )
}
