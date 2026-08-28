package com.kamsiob.claritynow.domain.momentum

/**
 * Everything the Momentum screen draws, decided once and handed over whole.
 * `MASTER_BUILD_PROMPT.md` 12.2 and design-v3.md section 11.
 *
 * ## What is deliberately not in here
 *
 * **There is no streak and no field that could become one.** No run length, no
 * consecutive count, no "days since", no longest anything.
 * `CLARITY_LOGIC_ENGINE.md` 3.1 keeps streak facts out of `FactSet` by construction and
 * `TrailQueries` says in its own header that `activeDayKeys` makes a streak three lines of
 * work and that those three lines must never be written. This type is where they would be
 * written, so it holds fourteen independent days and nothing that relates them.
 * [ActivityWindow.activeCount] is a count of a set, not of a run, and a missed day changes
 * it by one and resets nothing.
 *
 * **There is no sentence here except [headline], and that one came out of the engine.**
 * Every other field is a number or a name. `CLAUDE.md` rule 8: fixed labels and direct
 * readouts of a queried number live in `strings.xml`, observations never do, and the one
 * observation on this screen is the headline.
 *
 * **Every number below came from a `TrailQueries` function**, most of them through
 * `FactSet`, which states the same rule on itself and is layer one of the engine over the
 * same facade. Nothing on this screen is counted a second way.
 */
data class MomentumView(

    /**
     * The engine's headline, `Purpose.MOMENTUM_HEADLINE`, under twelve words, set in
     * `readSerif`.
     *
     * **Null is a real state and it is silence.** The engine returns `Silent` when nothing
     * qualifies, when everything qualified was filtered, and when layer 5 vetoed
     * everything it was offered. The screen draws no headline and no stand in: a fixed
     * sentence in `strings.xml` standing where an observation would go is exactly the
     * second path `MASTER_BUILD_PROMPT.md` 11.1 forbids, and it would be a claim about a
     * person's fortnight that no corpus line was written for.
     */
    val headline: String?,

    val activity: ActivityWindow,

    /** One per non archived area, in the order the Areas screen shows them. */
    val tiles: List<AreaTile>,

    val week: WeekStats,

    val insights: MomentumInsights,
) {

    /**
     * True when the log holds nothing at all inside the fortnight.
     *
     * The empty state in 12.2 is this screen with nothing in it rather than a screen of
     * its own: empty dots, outlined tiles, dimmed stats, and whatever the engine says
     * above them, which for a person with areas and no events is the `cleanSlate` family.
     * There is no separate layout to keep in step and no second place for a welcome to
     * live.
     */
    val isEmpty: Boolean get() = activity.activeCount == 0 && week.isEmpty
}

/** One day in the fourteen dot row. */
data class ActivityDay(
    val dateKey: String,
    /**
     * At least one user activity event on this local day.
     *
     * `TrailQueries.activeDayKeys`, which counts `isUserActivity` events only, so opening
     * the app and doing nothing never fills a dot. `DECISIONS.md` C7 argues that at
     * length: had `APP_OPENED` counted, a fortnight of opening and closing would draw a
     * full row.
     */
    val active: Boolean,
    val isToday: Boolean,
)

/**
 * The rolling window behind `Active X of last 14 days`.
 *
 * **Rolling by design, and that is the whole of the promise.** A missed day drops out of
 * the count when it falls off the back of the window and never resets anything, because
 * there is nothing here to reset. `MASTER_BUILD_PROMPT.md` 12.2 states it and issue #5
 * lists the accidental streak as the phase's first risk.
 */
data class ActivityWindow(val days: List<ActivityDay>, val activeCount: Int) {

    /** Fourteen. Carried rather than assumed so the row and the label cannot disagree. */
    val length: Int get() = days.size
}

/**
 * One area tile. design-v3.md section 11 and 3.4.
 *
 * **This is the one place in the app where an area color gets real presence.** 3.4 permits
 * the accent in exactly four forms: a 7dp dot, a 5 to 14 percent wash, a 60 percent tile
 * in Momentum, and the area label text. The third of those is this. It is still never a
 * stripe, a bar, an edge or a border.
 */
data class AreaTile(
    val id: String,
    val name: String,
    val colorHex: String,
    /** Drawn at 60 percent of the accent when true, and as a faint outline when false. */
    val hasActiveItem: Boolean,
)

/** Which feature a stat is about, and therefore which discovery line it can carry. */
enum class WeekStatKind {
    COMPLETED,
    FOCUS_MINUTES,
    ADDED,
}

/**
 * One of the three This Week figures, Monday to now.
 *
 * [discovered] is false only when the feature behind the figure has **never** been used,
 * in the whole log, which is not the same as a zero this week. 12.2 asks for an unused
 * feature to render dimmed with a soft discovery line rather than be hidden, and a zero
 * this week is a zero rather than a missing feature: telling somebody who finished nine
 * things last week what completing is would be worse than saying nothing.
 */
data class WeekStat(val kind: WeekStatKind, val value: Int, val discovered: Boolean)

/**
 * This Week, Monday to now. `MASTER_BUILD_PROMPT.md` 12.2.
 *
 * Three figures and no fourth. The order is output, attention, intake, and it is
 * deliberate: the obvious order is the order a thing moves through the app, added and
 * then completed, which opens the screen on a number that goes up every time somebody has
 * an idea. See `MomentumComposer` for the recorded reasoning.
 */
data class WeekStats(val completed: WeekStat, val focused: WeekStat, val added: WeekStat) {

    val all: List<WeekStat> get() = listOf(completed, focused, added)

    val isEmpty: Boolean get() = all.all { it.value == 0 }
}

/**
 * The four insight modules. **Each is null when it has no data**, and a null module is
 * absent from the screen rather than drawn empty.
 *
 * `MASTER_BUILD_PROMPT.md` 11.4: never pad a section to reach a minimum. A module with
 * one week of history in it is not a smaller version of the module, it is a shape a person
 * could read something into that is not there.
 */
data class MomentumInsights(
    val areaBalance: AreaBalance?,
    val completionPace: CompletionPace?,
    val focusPattern: FocusPattern?,
    val idleAreas: List<IdleArea>?,
) {
    val any: Boolean
        get() = areaBalance != null || completionPace != null || focusPattern != null || idleAreas != null
}

/**
 * Every area's share of the fortnight, and the total those shares are shares of.
 *
 * **The total is carried because the shares do not sum to a hundred and a reader will
 * add them.** `AreaFacts.shareOfEvents` divides an area's events by every user activity
 * event in the window, and some of those belong to no area at all: answering a Pulse,
 * changing a setting, writing something into the unfiled inbox before it is filed. On a
 * fortnight holding thirteen events with two of them area-less, two areas read 64 and 21
 * percent, and the fifteen points between that and a hundred are real and unexplained.
 *
 * **The denominator is not changed to make the column sum**, which is the obvious fix and
 * the wrong one. The Momentum headline can say the same percentage about the same area
 * through the engine's `areaShare` measure, and `MASTER_BUILD_PROMPT.md` 11.4 allows one
 * fact exactly one number. A module that quietly divided by a different total would
 * disagree with the sentence above it, which is a worse defect than the one it fixed and
 * a much harder one to notice.
 *
 * So the number stays and the screen says what it is a share of. [total] is
 * `HistoryFacts.totalEvents`, the same count the Report's week ribbon reads out.
 */
data class AreaBalance(val shares: List<AreaShare>, val total: Int)

/**
 * One area's share of the fortnight, as a whole percentage.
 *
 * The share is `AreaFacts.shareOfEvents`, which layer one computed from two counts that
 * both came off the facade, and it is rounded to a whole number here and nowhere else.
 */
data class AreaShare(
    val id: String,
    val name: String,
    val colorHex: String,
    val events: Int,
    val percent: Int,
)

/**
 * Eight weekly completion counts, oldest first, for the sparkline.
 *
 * `HistoryFacts.weekCompletionsSeries`, which is seven day buckets anchored at the window
 * end rather than calendar weeks, so the newest bucket ends today and the comparison a
 * reader makes across the line is one they could reproduce.
 */
data class CompletionPace(val weeks: List<Int>, val total: Int) {

    val busiestWeek: Int get() = weeks.maxOrNull() ?: 0
}

/** One day of the focus heat strip. */
data class FocusDay(val dateKey: String, val minutes: Int)

/** Seven days of focus, oldest first, and the minutes across them. */
data class FocusPattern(val days: List<FocusDay>, val minutes: Int) {

    val busiestDay: Int get() = days.maxOfOrNull { it.minutes } ?: 0
}

/**
 * An area with nothing in it for seven days or more. 12.2: gentle, and no red.
 *
 * [daysIdle] is `AreaFacts.daysSinceLastEvent`, and an area that has never had an event
 * never becomes one of these: 3.1 gives that case `Int.MAX_VALUE`, which is a sentinel and
 * not a number, and `MomentumComposer` excludes it rather than printing it.
 */
data class IdleArea(val id: String, val name: String, val colorHex: String, val daysIdle: Int)

/**
 * The Areas banner. design-v3.md 10.2 and `CORPUS_3_MOMENTUM.md`.
 *
 * Two parts, always rendered together: [sentence] in bodyStrong and [caption] beneath it.
 * Both are authored corpus lines. The sentence came through the engine with
 * `Purpose.AREAS_BANNER`; the caption came off the shared caption bench, which the corpus
 * says is "selected independently of the sentence and combined with it".
 *
 * [caption] is null when no caption line could be filled truthfully from the week in hand,
 * which is a real state rather than an error: the corpus's own rule is that zero never
 * renders through a count slot, and a week that cannot fill a caption gets no caption
 * rather than a nought.
 */
data class AreasBannerView(val sentence: String, val caption: String?)
