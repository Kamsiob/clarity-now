package com.kamsiob.claritynow.domain.engine.realize

import com.kamsiob.claritynow.domain.engine.AreaFacts
import com.kamsiob.claritynow.domain.engine.FactRef
import com.kamsiob.claritynow.domain.engine.FactSet
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Every fact a rendered slot may come from, as a table something can walk.
 * CLARITY_LOGIC_ENGINE.md 7.2 and 8 check 3, and `MASTER_BUILD_PROMPT.md` 9 and 11.4.
 *
 * **This is what makes number provenance checkable rather than asserted.** Check 3 of
 * section 8 says every `Count`, `Percent` and `Days` slot carries a [FactRef], and that
 * re-reading that fact yields the same value. Re-reading is only possible if a [FactRef]
 * names something a function can look up, so a ref here is not a label: it is an address.
 * [FactLookup] resolves one back to a number, and the validator compares.
 *
 * The address is `category` plus `path`, and the path is a measure id with the entity it
 * was read for appended after a colon. `("area", "areaEvents:7f3c...")` is the events in
 * the window for that one area. Nothing else in the app parses these, so the format is
 * free to be exactly what re-reading needs.
 *
 * **Zero and never both resolve to null rather than to a number.** 7.2 says zero never
 * reaches a template and 3.1 says `daysSinceLastEvent` is `Int.MAX_VALUE` for an area that
 * has never had an event. Both would render as a sentence about nothing, so both make the
 * slot unfillable, the variant unusable, and another line from the same bench is chosen.
 * Validator check 4 stays as the backstop for a number that arrives some other way.
 *
 * **A measure is not a rule.** Nothing here decides whether a sentence should be said. It
 * decides what a number is once something else has decided to say it.
 */
internal enum class MeasureKind { COUNT, DAYS, PERCENT, DATE, TEXT }

/** What a measure has to be given before it can be read. */
internal enum class MeasureScope {
    /** Nothing. The whole window, the whole rollup, the whole history. */
    WINDOW,

    /** An area id. Absent areas answer null, which is how archived areas stay unnameable. */
    AREA,

    /** An item id, which must be the active item of some area in the fact set. */
    ITEM,

    /** A weekly series offset, `0` being the newest bucket. */
    OFFSET,

    /** A stored `responseLabel`, quoted back exactly as the person saw it. */
    LABEL,
}

/** What a measure read. */
internal sealed interface MeasureValue {

    data class Number(val value: Int) : MeasureValue

    /**
     * A snapshot name, and the entity it named.
     *
     * [namedArea] and [namedItem] are what fill `Candidate.namedAreaIds` and
     * `namedItemIds`, which validator checks 1 and 2 read. They are recorded by the
     * measure rather than derived by the realizer from the entity it passed in, because the
     * two are not always the same thing: `itemAreaName` is read for an item id and names an
     * area, and `areaActiveItemTitle` is read for an area id and names an item. Deriving it
     * outside would get both backwards.
     */
    data class Text(val value: String, val namedArea: String? = null, val namedItem: String? = null) : MeasureValue

    data class Date(val weekKey: String, val display: String) : MeasureValue
}

/**
 * One readable fact, with the noun it counts.
 *
 * [singular] and [plural] are carried on the measure rather than on the line because the
 * measure is what knows whether it is counting things, areas or sessions. A template
 * author writes `{n} things` and never writes a plural rule.
 */
internal class Measure(
    val id: String,
    val category: String,
    val kind: MeasureKind,
    val scope: MeasureScope,
    val describe: String,
    val singular: String = "",
    val plural: String = "",
    val read: (FactSet, String?, ZoneId) -> MeasureValue?,
) {
    /** The address of this measure read for [entityId]. */
    fun refFor(entityId: String?): FactRef =
        FactRef(category, if (entityId == null) id else "$id:$entityId")
}

/**
 * The measure table, and the resolution that makes a [FactRef] re-readable.
 *
 * Every entry was read out of the three corpus files: a measure exists here because a line
 * somewhere needs the number it produces, and the binding table in [SlotBindings] names
 * which line needs which.
 */
internal object Measures {

    // ------------------------------------------------------------------ helpers

    private fun count(value: Int?): MeasureValue? =
        if (value == null || value <= 0) null else MeasureValue.Number(value)

    private fun days(value: Int?): MeasureValue? =
        if (value == null || value <= 0 || value == Int.MAX_VALUE) null else MeasureValue.Number(value)

    private fun percent(share: Double): MeasureValue? {
        val whole = (share * PERCENT_SCALE).roundToInt()
        return if (whole <= 0) null else MeasureValue.Number(whole)
    }

    private fun text(value: String?): MeasureValue? =
        if (value.isNullOrBlank()) null else MeasureValue.Text(value)

    private const val PERCENT_SCALE = 100.0

    private const val DAYS_PER_WEEK = 7

    /** The month a date key names, `March`. 7.2: a month name, never a numeric date. */
    private fun monthOf(dateKey: String?): MeasureValue? {
        val date = dateKey?.let { runCatching { LocalDate.parse(it) }.getOrNull() } ?: return null
        return MeasureValue.Date(dateKey, date.month.getDisplayName(TextStyle.FULL, Locale.US))
    }

    /** A month and a day, `March 3`, for the one family that names the week it covers. */
    private fun monthDayOf(date: LocalDate): MeasureValue =
        MeasureValue.Date(
            date.toString(),
            "${date.month.getDisplayName(TextStyle.FULL, Locale.US)} ${date.dayOfMonth}",
        )

    private fun localDate(atMillis: Long, zone: ZoneId): LocalDate =
        Instant.ofEpochMilli(atMillis).atZone(zone).toLocalDate()

    /** The areas of a fact set, busiest first, ties by id so two devices agree. */
    private fun byActivity(facts: FactSet): List<AreaFacts> =
        facts.areas.values.sortedWith(compareByDescending<AreaFacts> { it.eventsInWindow }.thenBy { it.areaId })

    private fun area(facts: FactSet, id: String?): AreaFacts? = id?.let { facts.areas[it] }

    private fun activeItem(facts: FactSet, id: String?): Pair<AreaFacts, String>? {
        if (id == null) return null
        val holder = facts.areas.values.firstOrNull { it.activeItemId == id } ?: return null
        val title = holder.activeItemTitleSnapshot ?: return null
        return holder to title
    }

    private fun seriesValue(series: List<Int>, offset: String?): Int? {
        val back = offset?.toIntOrNull() ?: return null
        val index = series.size - 1 - back
        return series.getOrNull(index)
    }

    // ------------------------------------------------------------------ the table

    val ALL: List<Measure> = listOf(
        // The window, counted. Everything here came from TrailQueries through layer one.
        Measure("completions", "window", MeasureKind.COUNT, MeasureScope.WINDOW,
            "things completed in the window", "thing", "things") read@{ facts, _, _ -> count(facts.window.completions) },
        Measure("additions", "window", MeasureKind.COUNT, MeasureScope.WINDOW,
            "things added in the window", "thing", "things") read@{ facts, _, _ -> count(facts.window.additions) },
        Measure("totalEvents", "window", MeasureKind.COUNT, MeasureScope.WINDOW,
            "everything that happened in the window", "move", "moves") read@{ facts, _, _ -> count(facts.window.totalEvents) },
        Measure("activeDays", "window", MeasureKind.COUNT, MeasureScope.WINDOW,
            "days in the window with something in them", "day", "days") read@{ facts, _, _ -> count(facts.window.activeDays) },
        Measure("quietDays", "window", MeasureKind.COUNT, MeasureScope.WINDOW,
            "days in the window with nothing in them", "day", "days") read@{ facts, _, _ ->
            count(facts.window.dayCount - facts.window.activeDays)
        },
        Measure("dayCount", "window", MeasureKind.COUNT, MeasureScope.WINDOW,
            "how many days the window covers", "day", "days") read@{ facts, _, _ -> count(facts.window.dayCount) },
        Measure("focusSessions", "window", MeasureKind.COUNT, MeasureScope.WINDOW,
            "focus sessions that finished", "session", "sessions") read@{ facts, _, _ -> count(facts.window.focusCompleted) },
        Measure("focusStarted", "window", MeasureKind.COUNT, MeasureScope.WINDOW,
            "focus sessions that were started", "session", "sessions") read@{ facts, _, _ -> count(facts.window.focusStarted) },
        Measure("focusEndedEarly", "window", MeasureKind.COUNT, MeasureScope.WINDOW,
            "focus sessions ended before the timer", "session", "sessions") read@{ facts, _, _ -> count(facts.window.focusEndedEarly) },
        Measure("focusMinutes", "window", MeasureKind.COUNT, MeasureScope.WINDOW,
            "minutes of focused time", "minute", "minutes") read@{ facts, _, _ -> count(facts.window.focusMinutesTotal) },
        Measure("swaps", "window", MeasureKind.COUNT, MeasureScope.WINDOW,
            "active item changes in the window", "swap", "swaps") read@{ facts, _, _ -> count(facts.window.swaps) },
        Measure("intakeGap", "window", MeasureKind.COUNT, MeasureScope.WINDOW,
            "how far additions ran ahead of completions", "thing", "things") read@{ facts, _, _ ->
            count(facts.window.additions - facts.window.completions)
        },
        Measure("outflowGap", "window", MeasureKind.COUNT, MeasureScope.WINDOW,
            "how far completions ran ahead of additions", "thing", "things") read@{ facts, _, _ -> count(facts.window.netFlow) },
        Measure("busiestDayCount", "window", MeasureKind.COUNT, MeasureScope.WINDOW,
            "events on the busiest day", "event", "events") read@{ facts, _, _ -> count(facts.window.busiestDayCount) },
        Measure("busiestDayName", "window", MeasureKind.TEXT, MeasureScope.WINDOW,
            "the busiest day of the window, by name") read@{ facts, _, _ ->
            val key = facts.window.busiestDayKey ?: return@read null
            val date = runCatching { LocalDate.parse(key) }.getOrNull() ?: return@read null
            text(date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.US))
        },
        Measure("weekRef", "window", MeasureKind.DATE, MeasureScope.WINDOW,
            "the day the window opens, as a month and a day") read@{ facts, _, zone ->
            monthDayOf(localDate(facts.window.startInstant, zone))
        },
        Measure("eventsOutsideArea", "window", MeasureKind.COUNT, MeasureScope.AREA,
            "everything in the window that was not this area", "thing", "things") read@{ facts, id, _ ->
            val subject = area(facts, id) ?: return@read null
            count(facts.window.totalEvents - subject.eventsInWindow)
        },

        // The shape across areas.
        Measure("areasWithEvents", "rollup", MeasureKind.COUNT, MeasureScope.WINDOW,
            "areas that moved", "area", "areas") read@{ facts, _, _ -> count(facts.rollup.areasWithEvents) },
        Measure("areasTotal", "rollup", MeasureKind.COUNT, MeasureScope.WINDOW,
            "areas that exist", "area", "areas") read@{ facts, _, _ -> count(facts.rollup.areasTotal) },
        Measure("areasIdle", "rollup", MeasureKind.COUNT, MeasureScope.WINDOW,
            "areas that did not move", "area", "areas") read@{ facts, _, _ -> count(facts.rollup.areasIdle) },
        Measure("dominantPercent", "rollup", MeasureKind.PERCENT, MeasureScope.WINDOW,
            "the busiest area's share of the window") read@{ facts, _, _ -> percent(facts.rollup.dominantShare) },
        Measure("queueTotal", "rollup", MeasureKind.COUNT, MeasureScope.WINDOW,
            "everything waiting, across every area", "thing", "things") read@{ facts, _, _ ->
            count(facts.areas.values.sumOf { it.queueLength })
        },
        Measure("queueGrowth", "rollup", MeasureKind.COUNT, MeasureScope.WINDOW,
            "how much longer the queues are than at the window start", "thing", "things") read@{ facts, _, _ ->
            count(facts.areas.values.sumOf { it.queueDelta })
        },
        Measure("queueShrink", "rollup", MeasureKind.COUNT, MeasureScope.WINDOW,
            "how much shorter the queues are than at the window start", "thing", "things") read@{ facts, _, _ ->
            count(-facts.areas.values.sumOf { it.queueDelta })
        },
        Measure("queueTotalAtStart", "rollup", MeasureKind.COUNT, MeasureScope.WINDOW,
            "everything that was waiting when the window opened", "thing", "things") read@{ facts, _, _ ->
            count(facts.areas.values.sumOf { it.queueLengthAtWindowStart })
        },
        Measure("areasWithQueue", "rollup", MeasureKind.COUNT, MeasureScope.WINDOW,
            "areas holding something behind their active item", "area", "areas") read@{ facts, _, _ ->
            count(facts.areas.values.count { it.queueLength > 0 })
        },
        Measure("areaSpread", "rollup", MeasureKind.COUNT, MeasureScope.WINDOW,
            "the gap between the busiest and the quietest area", "event", "events") read@{ facts, _, _ ->
            val ranked = byActivity(facts)
            if (ranked.size < 2) return@read null
            count(ranked.first().eventsInWindow - ranked.last().eventsInWindow)
        },

        // History, and the records the window is set against.
        Measure("lastWeekCompletions", "history", MeasureKind.COUNT, MeasureScope.WINDOW,
            "completions in the week before this one", "thing", "things") read@{ facts, _, _ ->
            count(facts.history.lastWeekCompletions)
        },
        Measure("personalBestCompletions", "history", MeasureKind.COUNT, MeasureScope.WINDOW,
            "the best earlier week's completions", "thing", "things") read@{ facts, _, _ ->
            count(facts.history.personalBestWeekCompletions)
        },
        Measure("personalBestFocusMinutes", "history", MeasureKind.COUNT, MeasureScope.WINDOW,
            "the best earlier week's focused minutes", "minute", "minutes") read@{ facts, _, _ ->
            count(facts.history.personalBestFocusMinutesWeek)
        },
        Measure("weeksSincePersonalBest", "history", MeasureKind.COUNT, MeasureScope.WINDOW,
            "weeks since the record week", "week", "weeks") read@{ facts, _, _ ->
            count(facts.history.weeksSincePersonalBest)
        },
        Measure("lifetimeCompletions", "history", MeasureKind.COUNT, MeasureScope.WINDOW,
            "everything ever completed", "thing", "things") read@{ facts, _, _ -> count(facts.history.lifetimeCompletions) },
        Measure("daysSinceInstall", "history", MeasureKind.COUNT, MeasureScope.WINDOW,
            "days since the first event in the log", "day", "days") read@{ facts, _, _ ->
            count(facts.history.daysSinceInstall)
        },
        Measure("longestEverActiveDays", "history", MeasureKind.DAYS, MeasureScope.WINDOW,
            "the longest anything has ever stayed active") read@{ facts, _, _ -> days(facts.history.longestEverActiveDays) },
        Measure("activityBandWidth", "history", MeasureKind.COUNT, MeasureScope.WINDOW,
            "the spread between the busiest and quietest of the last four weeks", "event", "events") read@{ facts, _, _ ->
            val recent = facts.history.weekTotalEventsSeries.takeLast(RHYTHM_WEEKS)
            if (recent.size < RHYTHM_WEEKS) return@read null
            count(recent.max() - recent.min())
        },
        Measure("averageWeekCompletions", "history", MeasureKind.COUNT, MeasureScope.WINDOW,
            "the mean completions of the weeks before this one", "thing", "things") read@{ facts, _, _ ->
            val earlier = facts.history.weekCompletionsSeries.dropLast(1).takeLast(AVERAGE_WEEKS)
            if (earlier.isEmpty()) return@read null
            count((earlier.sum().toDouble() / earlier.size).roundToInt())
        },
        Measure("personalBestWeekRef", "history", MeasureKind.DATE, MeasureScope.WINDOW,
            "the month the record week fell in") read@{ facts, _, _ -> monthOf(facts.history.personalBestWeekKey) },
        Measure("mostRecentBetterWeekRef", "history", MeasureKind.DATE, MeasureScope.WINDOW,
            "the month of the newest week that beat this one") read@{ facts, _, _ ->
            monthOf(facts.history.mostRecentBetterWeekKey)
        },
        Measure("weekCompletionsAgo", "history", MeasureKind.COUNT, MeasureScope.OFFSET,
            "completions in a numbered week back from this one", "thing", "things") read@{ facts, offset, _ ->
            count(seriesValue(facts.history.weekCompletionsSeries, offset))
        },
        Measure("weekEventsAgo", "history", MeasureKind.COUNT, MeasureScope.OFFSET,
            "total events in a numbered week back from this one", "event", "events") read@{ facts, offset, _ ->
            count(seriesValue(facts.history.weekTotalEventsSeries, offset))
        },
        Measure("weekQueueSizeAgo", "history", MeasureKind.COUNT, MeasureScope.OFFSET,
            "everything waiting at the end of a numbered week back", "thing", "things") read@{ facts, offset, _ ->
            count(seriesValue(facts.history.weekQueueSizeSeries, offset))
        },
        Measure("dominantAreaAgo", "history", MeasureKind.TEXT, MeasureScope.OFFSET,
            "the name of the area that led a numbered week back") read@{ facts, offset, _ ->
            val back = offset?.toIntOrNull() ?: return@read null
            val series = facts.history.dominantAreaLastThreeWeeks
            val id = series.getOrNull(series.size - 1 - back) ?: return@read null
            val subject = facts.areas[id] ?: return@read null
            // An area that led an earlier week and has been still through this one is a
            // true thing to say and a candidate validator check 1 vetoes, because the check
            // reads the window in front of it. Answering null instead drops this one line
            // and leaves the rest of the bench, which says the same thing without names.
            if (subject.eventsInWindow <= 0) return@read null
            MeasureValue.Text(subject.nameSnapshot, namedArea = subject.areaId)
        },

        // Items.
        Measure("medianDaysToComplete", "items", MeasureKind.DAYS, MeasureScope.WINDOW,
            "how long things usually take, null under three completions") read@{ facts, _, _ ->
            days(facts.items.medianDaysToComplete)
        },
        Measure("longestActiveDays", "items", MeasureKind.DAYS, MeasureScope.WINDOW,
            "how long the oldest active item has been active") read@{ facts, _, _ -> days(facts.items.longestActiveDays) },
        Measure("completedInWindow", "items", MeasureKind.COUNT, MeasureScope.WINDOW,
            "items finished in the window that can still be named", "thing", "things") read@{ facts, _, _ ->
            count(facts.items.completedInWindow.size)
        },

        // What the person said back.
        Measure("answeredInWindow", "pulse", MeasureKind.COUNT, MeasureScope.WINDOW,
            "pulses answered in the window", "answer", "answers") read@{ facts, _, _ -> count(facts.pulse.answeredInWindow) },
        Measure("positiveInWindow", "pulse", MeasureKind.COUNT, MeasureScope.WINDOW,
            "answers in the window that were the positive option", "answer", "answers") read@{ facts, _, _ ->
            count(facts.pulse.positiveInWindow)
        },
        Measure("flaggedInWindow", "pulse", MeasureKind.COUNT, MeasureScope.WINDOW,
            "answers in the window that were not the positive option", "answer", "answers") read@{ facts, _, _ ->
            count(facts.pulse.flaggedInWindow)
        },
        Measure("mostGivenLabel", "pulse", MeasureKind.TEXT, MeasureScope.WINDOW,
            "the stored label the person has given most often, ties by alphabet") read@{ facts, _, _ ->
            val counted = facts.pulse.recentAnswers.groupingBy { it.responseLabel }.eachCount()
            val best = counted.entries.sortedWith(
                compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key },
            ).firstOrNull() ?: return@read null
            text(best.key)
        },
        Measure("labelText", "pulse", MeasureKind.TEXT, MeasureScope.LABEL,
            "a stored responseLabel, quoted exactly as the person saw it") read@{ _, label, _ -> text(label) },
        Measure("labelCount", "pulse", MeasureKind.COUNT, MeasureScope.LABEL,
            "how many times that stored label was given, across the answers on hand", "time", "times") read@{ facts, label, _ ->
            if (label == null) null else count(facts.pulse.recentAnswers.count { it.responseLabel == label })
        },
        Measure("labelCountInWindow", "pulse", MeasureKind.COUNT, MeasureScope.LABEL,
            "how many times that stored label was given inside the window", "time", "times") read@{ facts, label, zone ->
            if (label == null) return@read null
            val from = localDate(facts.window.startInstant, zone)
            val to = localDate(facts.window.endInstant, zone)
            count(
                facts.pulse.recentAnswers.count { answer ->
                    val day = runCatching { LocalDate.parse(answer.dateKey) }.getOrNull()
                    answer.responseLabel == label && day != null && !day.isBefore(from) && day.isBefore(to)
                },
            )
        },

        // One area.
        Measure("areaName", "area", MeasureKind.TEXT, MeasureScope.AREA,
            "the area name snapshot") read@{ facts, id, _ ->
            val subject = area(facts, id) ?: return@read null
            MeasureValue.Text(subject.nameSnapshot, namedArea = subject.areaId)
        },
        Measure("areaEvents", "area", MeasureKind.COUNT, MeasureScope.AREA,
            "this area's events in the window", "move", "moves") read@{ facts, id, _ -> count(area(facts, id)?.eventsInWindow) },
        Measure("areaCompletions", "area", MeasureKind.COUNT, MeasureScope.AREA,
            "this area's completions in the window", "thing", "things") read@{ facts, id, _ ->
            count(area(facts, id)?.completionsInWindow)
        },
        Measure("areaAdditions", "area", MeasureKind.COUNT, MeasureScope.AREA,
            "what this area took on in the window", "thing", "things") read@{ facts, id, _ ->
            count(area(facts, id)?.additionsInWindow)
        },
        Measure("areaShare", "area", MeasureKind.PERCENT, MeasureScope.AREA,
            "this area's share of the window") read@{ facts, id, _ ->
            val subject = area(facts, id) ?: return@read null
            percent(subject.shareOfEvents)
        },
        Measure("areaQueue", "area", MeasureKind.COUNT, MeasureScope.AREA,
            "what is waiting behind this area's active item", "thing", "things") read@{ facts, id, _ ->
            count(area(facts, id)?.queueLength)
        },
        Measure("areaQueueAtStart", "area", MeasureKind.COUNT, MeasureScope.AREA,
            "what was waiting in this area when the window opened", "thing", "things") read@{ facts, id, _ ->
            count(area(facts, id)?.queueLengthAtWindowStart)
        },
        Measure("areaDaysSinceLastEvent", "area", MeasureKind.DAYS, MeasureScope.AREA,
            "how long this area has been still") read@{ facts, id, _ -> days(area(facts, id)?.daysSinceLastEvent) },
        Measure("areaFocusSessions", "area", MeasureKind.COUNT, MeasureScope.AREA,
            "focus sessions in this area", "session", "sessions") read@{ facts, id, _ ->
            count(area(facts, id)?.focusSessionsInWindow)
        },
        Measure("areaLastEventRef", "area", MeasureKind.DATE, MeasureScope.AREA,
            "the month this area last moved in") read@{ facts, id, zone ->
            val subject = area(facts, id) ?: return@read null
            if (subject.daysSinceLastEvent <= 0 || subject.daysSinceLastEvent == Int.MAX_VALUE) return@read null
            val last = localDate(facts.window.endInstant, zone).minusDays(subject.daysSinceLastEvent.toLong())
            monthOf(last.toString())
        },
        Measure("areaActiveItemTitle", "area", MeasureKind.TEXT, MeasureScope.AREA,
            "the title snapshot of this area's active item") read@{ facts, id, _ ->
            val subject = area(facts, id) ?: return@read null
            val title = subject.activeItemTitleSnapshot ?: return@read null
            val item = subject.activeItemId ?: return@read null
            MeasureValue.Text(title, namedItem = item)
        },
        Measure("areaActiveItemAge", "area", MeasureKind.DAYS, MeasureScope.AREA,
            "how long this area's active item has been active") read@{ facts, id, _ ->
            days(area(facts, id)?.activeItemAgeDays)
        },

        // One item, always the active item of some area, because that is the only item
        // any family speaks about.
        Measure("itemTitle", "item", MeasureKind.TEXT, MeasureScope.ITEM,
            "the item title snapshot") read@{ facts, id, _ ->
            val held = activeItem(facts, id) ?: return@read null
            MeasureValue.Text(held.second, namedItem = id)
        },
        Measure("itemAreaName", "item", MeasureKind.TEXT, MeasureScope.ITEM,
            "the name snapshot of the area holding this item") read@{ facts, id, _ ->
            val held = activeItem(facts, id) ?: return@read null
            MeasureValue.Text(held.first.nameSnapshot, namedArea = held.first.areaId)
        },
        Measure("itemAgeDays", "item", MeasureKind.DAYS, MeasureScope.ITEM,
            "how long this item has been active") read@{ facts, id, _ -> days(activeItem(facts, id)?.first?.activeItemAgeDays) },
        Measure("itemActiveWeeks", "item", MeasureKind.COUNT, MeasureScope.ITEM,
            "how many whole weeks this item has been active", "week", "weeks") read@{ facts, id, _ ->
            val age = activeItem(facts, id)?.first?.activeItemAgeDays ?: return@read null
            count(age / DAYS_PER_WEEK)
        },
        Measure("itemQueueBehind", "item", MeasureKind.COUNT, MeasureScope.ITEM,
            "what is waiting behind this item", "thing", "things") read@{ facts, id, _ ->
            count(activeItem(facts, id)?.first?.queueLength)
        },
    )

    /** `consistentRhythm` reads four weeks, per `CORPUS_2_REPORT.md` 3.6. */
    private const val RHYTHM_WEEKS = 4

    /** How many earlier weeks an average is taken over, matching `MomentumRules`. */
    private const val AVERAGE_WEEKS = 8

    private val measuresById: Map<String, Measure> = ALL.associateBy { it.id }

    init {
        require(measuresById.size == ALL.size) { "two measures share an id, so one would resolve to the other" }
    }

    /** The measure with [id], or null. */
    fun byId(id: String): Measure? = measuresById[id]

    /** Every measure that produces a number the validator must be able to re-read. */
    val NUMERIC: List<Measure> = ALL.filter { it.kind != MeasureKind.TEXT && it.kind != MeasureKind.DATE }
}

/**
 * Reads a [FactRef] back. CLARITY_LOGIC_ENGINE.md 8 check 3.
 *
 * The validator holds a candidate, a fact set and a map of slot to [FactRef], and its job
 * is to prove that the number on the screen is the number in the facts. That is one call
 * to [readNumber] per numeric slot and a comparison, and it works because the ref was
 * produced by the same table that resolves it.
 *
 * A ref that does not resolve is **not** the same as one that resolves to something else,
 * and the caller must treat it as a veto rather than as a pass. That is why this returns
 * null rather than throwing: the validator decides what an unreadable number means, and
 * the answer is that it never reaches a person.
 */
internal object FactLookup {

    /** The measure a ref addresses, or null when nothing declares it. */
    fun measureOf(ref: FactRef): Measure? {
        val measure = Measures.byId(ref.path.substringBefore(SEPARATOR)) ?: return null
        return if (measure.category == ref.category) measure else null
    }

    /** The entity the ref was read for, or null for a whole window measure. */
    fun entityOf(ref: FactRef): String? =
        if (SEPARATOR in ref.path) ref.path.substringAfter(SEPARATOR) else null

    /** Re-reads [ref] against [facts]. */
    fun read(facts: FactSet, ref: FactRef, zone: ZoneId): MeasureValue? {
        val measure = measureOf(ref) ?: return null
        return measure.read(facts, entityOf(ref), zone)
    }

    /** Re-reads [ref] as the number it produced, or null when it is not a number now. */
    fun readNumber(facts: FactSet, ref: FactRef, zone: ZoneId): Int? =
        (read(facts, ref, zone) as? MeasureValue.Number)?.value

    private const val SEPARATOR = ':'
}
