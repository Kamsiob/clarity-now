package com.kamsiob.claritynow.domain.momentum

import com.kamsiob.claritynow.domain.engine.AreaFacts
import com.kamsiob.claritynow.domain.engine.ClarityEngine
import com.kamsiob.claritynow.domain.engine.EngineResult
import com.kamsiob.claritynow.domain.engine.FactExtractor
import com.kamsiob.claritynow.domain.engine.FactSet
import com.kamsiob.claritynow.domain.engine.FiringHistory
import com.kamsiob.claritynow.domain.engine.catalog.ClarityCatalog
import com.kamsiob.claritynow.domain.engine.catalog.Purpose
import com.kamsiob.claritynow.domain.engine.validate.ClarityValidator
import com.kamsiob.claritynow.domain.query.TrailQueries
import com.kamsiob.claritynow.domain.query.TrailWindow
import java.time.ZoneId
import kotlin.math.roundToInt

/**
 * Momentum and the Areas banner, composed. `MASTER_BUILD_PROMPT.md` 11.3 and 12.2.
 *
 * 11.3 gives Momentum four steps and one sentence: "on screen entry: extract, select one
 * `MOMENTUM_HEADLINE`, realize, validate. The banner is the same with `AREAS_BANNER` and a
 * one hour throttle held in the ViewModel, not the engine." Those steps are numbered in
 * the code below, the way `PulseGenerator` numbers its eight, so that a later edit has to
 * notice which one it is changing.
 *
 * ## What this class is, and what it deliberately is not
 *
 * It is a pure function of a log, a zone and one instant. It has no clock, no repository
 * and **no way to write**: Momentum is a mirror, it records nothing, and there is no
 * `MOMENTUM_GENERATED` event in the catalog for it to append. That is also why every rule
 * about engine state living in the log rather than in DataStore is satisfied here by there
 * being no state at all.
 *
 * **It holds no throttle.** The banner's once an hour rule is 6.5's and it says where it
 * lives: in the ViewModel. A throttle here would be engine state, it would be per process,
 * and two devices would disagree about it silently.
 *
 * ## The numbers
 *
 * **Every number that reaches the screen came from a `TrailQueries` function.** Most reach
 * it through `FactSet`, which is layer one over the same facade and states the same rule
 * on itself, and the handful that do not are direct calls named at their call site. There
 * is no second path and nothing on this screen is counted twice.
 *
 * **There is no streak here and no helper that could produce one.** `activeDayKeys` is
 * read as a set and asked one question per day, "was this day active", and the answers are
 * never related to each other. The count is the size of a set. A missed day changes it by
 * one and resets nothing.
 */
internal class MomentumComposer(
    /**
     * Null when the corpus could not be read or parsed, which is a packaging fault rather
     * than anything about the person's data.
     *
     * **Losing the language costs the headline and the banner and nothing else.** Every
     * number on this screen comes from the log through `TrailQueries` and none of it needs
     * a corpus, so the dots, the tiles, the stats and the insights all still render. That
     * is the right failure and it is the same one `PulseCoordinator` chose: a person can
     * still read what the app counted. A nullable field rather than a second class,
     * because the two would share every line below except one.
     */
    private val catalog: ClarityCatalog?,
    private val zone: ZoneId,
) {

    /**
     * Step 4 for both surfaces, and the constructor call is quoted from 11.3 rather than
     * parameterized.
     *
     * There is no seam for the validator, for the reason `PulseGenerator` gives: this is a
     * caller, and a caller that accepted a validator would be a way to hand the engine one
     * that vetoes nothing. 11.4 forbids bypassing layer 5 for any reason at all.
     */
    private val engine = catalog?.let { ClarityEngine(it, ClarityValidator(zone), zone) }

    /**
     * The whole Momentum surface for the instant [nowMillis].
     *
     * [queries] must be built over the **whole** log, not over the fortnight. Layer one
     * reads lifetime maps for its history facts, `FiringHistory` reads every engine
     * authored event ever written, and the This Week stats ask whether a feature has ever
     * been used at all. A windowed facade here would dim the focus stat for somebody who
     * ran sessions every day last month.
     *
     * [areaOrder] is the order the person arranged their areas in, as ids. It decides the
     * order of the tiles and nothing else. It is a parameter rather than something derived
     * here because area order lives on `AreaState.orderKey` in the projection and
     * `TrailQueries` deliberately hands out no ordering: a tile grid in a different order
     * from the Areas screen is two apps, and the alternative, sorting tiles by how busy
     * each area was, would turn an identity mosaic into a leaderboard.
     */
    fun compose(queries: TrailQueries, nowMillis: Long, areaOrder: List<String>): MomentumView {
        requireSameZone(queries)

        // 1. The window: the trailing fourteen local days, ending now.
        val window = MomentumWindows.fortnight(nowMillis, zone)

        // 2. Layer one.
        val facts = FactExtractor(queries).extract(window)

        // 3. Rebuilt from the log every time and never cached, per 11.7.
        val history = FiringHistory.from(queries, nowMillis)

        // 4. Select, realize, validate, in one call. Silence is an answer.
        val headline = speak(facts, history, Purpose.MOMENTUM_HEADLINE)

        return MomentumView(
            headline = headline,
            activity = activityOf(queries, window, nowMillis),
            tiles = tilesOf(facts, areaOrder),
            week = weekOf(queries, nowMillis),
            insights = insightsOf(queries, facts, nowMillis),
        )
    }

    /**
     * The banner sentence and its caption, or null when the engine had nothing to say.
     *
     * Null is silence and the Areas screen draws nothing in its place. design-v3.md 10.2
     * gives the banner a sentence and a caption and no third state, and a fixed sentence
     * standing in for an absent one would be the second path 11.1 forbids on the screen a
     * person opens most often.
     *
     * **This is not throttled and must not become throttled.** It is called by a ViewModel
     * that holds the once an hour rule, per 6.5, and a second throttle here would make the
     * rate a product of two numbers nobody stated.
     */
    fun banner(queries: TrailQueries, nowMillis: Long): AreasBannerView? {
        requireSameZone(queries)
        val language = catalog ?: return null
        val running = engine ?: return null
        val window = MomentumWindows.weekToDate(nowMillis, zone)
        val facts = FactExtractor(queries).extract(window)
        val history = FiringHistory.from(queries, nowMillis)
        val sentence = speak(facts, history, Purpose.AREAS_BANNER) ?: return null
        return AreasBannerView(
            sentence = sentence,
            // The caption is selected independently of the sentence, per the corpus, and
            // from the same fact set, so the two describe one week by construction.
            caption = BannerCaptions.render(language, facts, running.momentOf(facts).dateKey, history),
        )
    }

    private fun requireSameZone(queries: TrailQueries) {
        require(queries.zone() == zone) {
            "the facade counted in ${queries.zone()} and this composer is reading windows in " +
                "$zone, so the dots and the numbers would be about different days"
        }
    }

    /** One sentence for [purpose], or null when the engine chose or was forced into silence. */
    private fun speak(facts: FactSet, history: FiringHistory, purpose: Purpose): String? =
        when (val result = engine?.observe(facts, history, purpose)) {
            is EngineResult.Spoke -> result.output.text
            is EngineResult.Silent -> null
            null -> null
        }

    // ------------------------------------------------------------- the dot row

    /**
     * `Active X of last 14 days`, and the fourteen dots behind it.
     *
     * The keys are walked as calendar dates and the active set comes from
     * `TrailQueries.activeDayKeys`, which counts `isUserActivity` events only. Two things
     * follow that are worth stating because both are load bearing.
     *
     * **Opening the app is not activity.** `APP_OPENED` is excluded from `isUserActivity`,
     * so a fortnight of opening the app and doing nothing draws fourteen empty dots.
     * `DECISIONS.md` C7 has the argument.
     *
     * **The count is the size of a set.** Nothing here asks whether two active days were
     * adjacent, and there is no field on the way out that could answer it.
     */
    private fun activityOf(queries: TrailQueries, window: TrailWindow, nowMillis: Long): ActivityWindow {
        val keys = MomentumWindows.dayKeys(nowMillis, zone, MomentumWindows.FORTNIGHT_DAYS)
        val activeKeys = queries.activeDayKeys(window.fromMillis, window.toMillis)
        val todayKey = keys.last()
        val days = keys.map { key ->
            ActivityDay(dateKey = key, active = key in activeKeys, isToday = key == todayKey)
        }
        return ActivityWindow(days = days, activeCount = days.count { it.active })
    }

    // --------------------------------------------------------------- the tiles

    /**
     * One tile per non archived area. design-v3.md section 11 and 3.4.
     *
     * `FactSet.areas` holds only the areas live at the window end, so an archived or
     * tombstoned area cannot become a tile and there is no filter here that somebody could
     * later relax. Areas the order does not mention follow the ones it does, by name, so a
     * projection and a fact set that disagree for one frame still draw every area exactly
     * once.
     */
    private fun tilesOf(facts: FactSet, areaOrder: List<String>): List<AreaTile> {
        val byId = facts.areas
        val ordered = areaOrder.mapNotNull(byId::get)
        val rest = byId.values.filterNot { it.areaId in areaOrder }.sortedBy { it.nameSnapshot }
        return (ordered + rest).map { area ->
            AreaTile(
                id = area.areaId,
                name = area.nameSnapshot,
                colorHex = area.colorHex,
                hasActiveItem = area.hasActiveItem,
            )
        }
    }

    // ----------------------------------------------------------- This Week

    /**
     * The three figures, Monday to now. `MASTER_BUILD_PROMPT.md` 12.2.
     *
     * **Which three, and the order, were left open and are recorded here.** 12.2 asks for
     * three typographic stats and does not name them. The obvious three are completed,
     * added and areas active, and two of those are rejected: the tiles above already say
     * which areas moved, so a figure repeating it would be the same fact twice on one
     * screenful, and the lifecycle order, added then completed, opens the screen on a
     * number that goes up every time somebody has an idea. What is left is output,
     * attention and intake, in that order, which are the three axes the engine's own
     * families read and none of which is drawn anywhere else on the page.
     *
     * **[WeekStat.discovered] is a lifetime question, never a weekly one.** 12.2 asks for
     * an unused feature to render dimmed with a soft discovery line rather than be hidden,
     * and a zero this week is a zero rather than a missing feature. Both flags are asked of
     * the whole log for that reason, and intake has no flag at all because capture is the
     * first thing anybody does and there is no feature behind it to discover.
     */
    private fun weekOf(queries: TrailQueries, nowMillis: Long): WeekStats {
        val week = MomentumWindows.weekToDate(nowMillis, zone)
        val everCompleted = queries.completionsBetween(Long.MIN_VALUE, Long.MAX_VALUE) > 0
        val everFocused = queries.focusSessionCounts(Long.MIN_VALUE, Long.MAX_VALUE).started > 0
        return WeekStats(
            completed = WeekStat(
                kind = WeekStatKind.COMPLETED,
                value = queries.completionsBetween(week.fromMillis, week.toMillis),
                discovered = everCompleted,
            ),
            focused = WeekStat(
                kind = WeekStatKind.FOCUS_MINUTES,
                value = queries.focusMinutes(week.fromMillis, week.toMillis),
                discovered = everFocused,
            ),
            added = WeekStat(
                kind = WeekStatKind.ADDED,
                value = queries.additionsBetween(week.fromMillis, week.toMillis),
                discovered = true,
            ),
        )
    }

    // ------------------------------------------------------------- the modules

    /**
     * The four insight modules, each null when it has no data. 12.2.
     *
     * Every gate below is a floor under a claim rather than a taste. 11.4: never pad a
     * section to reach a minimum, and a module drawn from too little is not a smaller
     * version of itself, it is a shape a person can read something into that is not there.
     */
    private fun insightsOf(queries: TrailQueries, facts: FactSet, nowMillis: Long) = MomentumInsights(
        areaBalance = areaBalanceOf(facts),
        completionPace = completionPaceOf(facts),
        focusPattern = focusPatternOf(queries, nowMillis),
        idleAreas = idleAreasOf(facts),
    )

    /**
     * Each area's share of the fortnight, busiest first.
     *
     * **Two areas with something in them, or nothing.** One area holding a hundred percent
     * of a fortnight is a balance in the arithmetic sense and says nothing a person could
     * act on, and `AreaFacts.shareOfEvents` calls itself the most misused fact in the
     * system for exactly this reason. The engine's own share rules all carry an event
     * floor; this is the same floor expressed as a count of areas.
     */
    private fun areaBalanceOf(facts: FactSet): List<AreaShare>? {
        val withEvents = facts.areas.values.filter { it.eventsInWindow > 0 }
        if (withEvents.size < MIN_AREAS_FOR_BALANCE) return null
        return withEvents
            .sortedWith(compareByDescending<AreaFacts> { it.eventsInWindow }.thenBy { it.nameSnapshot })
            .map { area ->
                AreaShare(
                    id = area.areaId,
                    name = area.nameSnapshot,
                    colorHex = area.colorHex,
                    events = area.eventsInWindow,
                    // The one division on this screen, rounded once, here. Both terms came
                    // off the facade and layer one did the division; this only makes it a
                    // whole number, which is the form 7.2 renders a percentage in.
                    percent = (area.shareOfEvents * 100).roundToInt(),
                )
            }
    }

    /**
     * Up to eight weekly completion counts, oldest first.
     *
     * `HistoryFacts.weekCompletionsSeries` carries twelve seven day buckets anchored at the
     * window end, and the newest of them ends today. Three points and two of them carrying
     * something is the floor: two points is a comparison rather than a pace, and a line
     * across a single non zero bucket is a spike drawn as a trend.
     */
    private fun completionPaceOf(facts: FactSet): CompletionPace? {
        val weeks = facts.history.weekCompletionsSeries.takeLast(MomentumWindows.PACE_WEEKS)
        if (weeks.size < MIN_PACE_WEEKS) return null
        if (weeks.count { it > 0 } < MIN_PACE_WEEKS_WITH_COMPLETIONS) return null
        return CompletionPace(weeks = weeks, total = weeks.sum())
    }

    /**
     * Seven days of focus minutes, oldest first, and the minutes across them.
     *
     * The strip is drawn from `focusSecondsPerDay`, which attributes a whole session to the
     * day it started on, so a session across midnight lands on one day rather than being
     * split into two halves that disagree. The total is `focusMinutes` over the same
     * window rather than a sum of the strip, because rounding to whole minutes happens once
     * per figure in the facade and summing seven rounded figures is a second arithmetic.
     */
    private fun focusPatternOf(queries: TrailQueries, nowMillis: Long): FocusPattern? {
        val days = MomentumWindows.FOCUS_STRIP_DAYS
        val strip = MomentumWindows.trailingDays(nowMillis, zone, days)
        val perDay = queries.focusSecondsPerDay(strip.fromMillis, strip.toMillis)
        val marks = MomentumWindows.dayKeys(nowMillis, zone, days).map { key ->
            FocusDay(dateKey = key, minutes = ((perDay[key] ?: 0L) / SECONDS_PER_MINUTE).toInt())
        }
        if (marks.none { it.minutes > 0 }) return null
        return FocusPattern(days = marks, minutes = queries.focusMinutes(strip.fromMillis, strip.toMillis))
    }

    /**
     * Areas with nothing in them for seven days or more, longest first. 12.2: only at seven
     * or more days, gentle, and no red.
     *
     * An area that has never had an event is excluded rather than reported as idle forever:
     * 3.1 gives that case `Int.MAX_VALUE`, which is a sentinel and never a number a screen
     * may render.
     *
     * **The module needs somewhere that is not quiet, and this was found by a test rather
     * than reasoned out in advance.** Without the second gate, a person who created two
     * areas and then had a hard fortnight opens the calmest screen in the app and is handed
     * a list of everything they have not touched, under a heading, with a number of days
     * beside each one. That is the guilt 12.2 rules out of the empty state in as many
     * words, and it is also a measurement of an absence, which 14b.4 spends its whole
     * length forbidding on a screen a returning person sees. Quiet is a comparison: it
     * means quiet next to somewhere that moved, and when nothing moved the headline has
     * already said so in one sentence from the corpus. `MomentumComposerTest` holds the
     * line.
     */
    private fun idleAreasOf(facts: FactSet): List<IdleArea>? {
        val anywhereMoving = facts.areas.values.any { it.daysSinceLastEvent < IDLE_DAYS }
        if (!anywhereMoving) return null
        val idle = facts.areas.values
            .filter { it.daysSinceLastEvent >= IDLE_DAYS && it.daysSinceLastEvent < Int.MAX_VALUE }
            .sortedWith(compareByDescending<AreaFacts> { it.daysSinceLastEvent }.thenBy { it.nameSnapshot })
            .map { area ->
                IdleArea(
                    id = area.areaId,
                    name = area.nameSnapshot,
                    colorHex = area.colorHex,
                    daysIdle = area.daysSinceLastEvent,
                )
            }
        return idle.ifEmpty { null }
    }

    companion object {

        /** `MASTER_BUILD_PROMPT.md` 12.2. Seven or more days inactive, and not a day sooner. */
        const val IDLE_DAYS = 7

        /** A share of a fortnight is only a balance when there is something to balance against. */
        const val MIN_AREAS_FOR_BALANCE = 2

        /** Three points, because two is a comparison and one is a dot. */
        const val MIN_PACE_WEEKS = 3

        /** Two of them carrying something, so a line is not drawn across a single spike. */
        const val MIN_PACE_WEEKS_WITH_COMPLETIONS = 2

        private const val SECONDS_PER_MINUTE = 60L
    }
}
