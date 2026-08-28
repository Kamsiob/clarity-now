package com.kamsiob.claritynow.domain.engine.realize

import com.kamsiob.claritynow.domain.engine.ActiveItem
import com.kamsiob.claritynow.domain.engine.AnsweredPulse
import com.kamsiob.claritynow.domain.engine.AreaFacts
import com.kamsiob.claritynow.domain.engine.AreaId
import com.kamsiob.claritynow.domain.engine.CompletedItem
import com.kamsiob.claritynow.domain.engine.CueFacts
import com.kamsiob.claritynow.domain.engine.EstimateTendency
import com.kamsiob.claritynow.domain.engine.FactSet
import com.kamsiob.claritynow.domain.engine.FirstEver
import com.kamsiob.claritynow.domain.engine.HistoryFacts
import com.kamsiob.claritynow.domain.engine.ItemFacts
import com.kamsiob.claritynow.domain.engine.PartOfDay
import com.kamsiob.claritynow.domain.engine.Precedent
import com.kamsiob.claritynow.domain.engine.PulseFacts
import com.kamsiob.claritynow.domain.engine.RollupFacts
import com.kamsiob.claritynow.domain.engine.Trend
import com.kamsiob.claritynow.domain.engine.WindowFacts
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

/**
 * Fact sets built by hand, for the two layers that never touch a log.
 *
 * Layers 3 and 4 take a `FactSet` and nothing else, so their tests are better written
 * against constructed facts than against a replayed log: a test that has to build eleven
 * events to reach a share of seventy percent is a test about the extractor. Layer one has
 * its own suite for that, and `FactExtractorTest` is where a fact set built from a log is
 * checked against the log.
 *
 * Every default here is the empty case, so a test names only what it is about.
 */
internal object EngineFacts {

    /** A fixed zone, so a test never depends on where it runs. */
    val ZONE: ZoneId = ZoneOffset.UTC

    /** Day zero. A Sunday, chosen so a week boundary is easy to reason about. */
    val EPOCH: LocalDate = LocalDate.of(2026, 3, 1)

    /** Local midnight opening day [offset]. */
    fun startOfDay(offset: Int): Long =
        EPOCH.plusDays(offset.toLong()).atStartOfDay(ZONE).toInstant().toEpochMilli()

    /** An instant inside day [offset]. */
    fun at(offset: Int, hour: Int): Long = startOfDay(offset) + hour * MILLIS_PER_HOUR

    /** The `yyyy-MM-dd` key of day [offset]. */
    fun dateKey(offset: Int): String = EPOCH.plusDays(offset.toLong()).toString()

    private const val MILLIS_PER_HOUR = 3_600_000L

    fun window(
        startDay: Int = 0,
        endDay: Int = 1,
        endHour: Int = 0,
        totalEvents: Int = 0,
        completions: Int = 0,
        additions: Int = 0,
        promotions: Int = 0,
        swaps: Int = 0,
        deletions: Int = 0,
        focusStarted: Int = 0,
        focusCompleted: Int = 0,
        focusEndedEarly: Int = 0,
        focusMinutes: Int = 0,
        // A session a day until the window runs out of days, which is the shape a fixture
        // naming a session count usually means. Pass it where the point is several
        // sessions on one afternoon.
        focusDays: Int = minOf(focusStarted, endDay - startDay),
        activeDays: Int = 0,
        busiestDayKey: String? = null,
        busiestDayCount: Int = 0,
        eventsByPartOfDay: Map<PartOfDay, Int> = PartOfDay.entries.associateWith { 0 },
    ) = WindowFacts(
        startInstant = startOfDay(startDay),
        endInstant = at(endDay, endHour),
        dayCount = endDay - startDay,
        totalEvents = totalEvents,
        completions = completions,
        additions = additions,
        promotions = promotions,
        swaps = swaps,
        deletions = deletions,
        focusStarted = focusStarted,
        focusCompleted = focusCompleted,
        focusEndedEarly = focusEndedEarly,
        focusSecondsTotal = focusMinutes * SECONDS_PER_MINUTE,
        focusMinutesTotal = focusMinutes,
        focusDays = focusDays,
        activeDays = activeDays,
        busiestDayKey = busiestDayKey,
        busiestDayCount = busiestDayCount,
        eventsByPartOfDay = eventsByPartOfDay,
        netFlow = completions - additions,
    )

    private const val SECONDS_PER_MINUTE = 60L

    fun area(
        areaId: AreaId,
        name: String = areaId,
        events: Int = 0,
        completions: Int = 0,
        additions: Int = 0,
        share: Double = 0.0,
        activeItemId: String? = null,
        activeItemTitle: String? = null,
        activeItemAgeDays: Int? = null,
        queueLength: Int = 0,
        queueLengthAtWindowStart: Int = 0,
        // A fixture that opens the window holding a queue and closes it empty has drained,
        // so the default derives rather than being a third number a caller has to remember
        // to keep consistent with the other two. Pass it explicitly for the shape the
        // boundary pair cannot express: a queue built and emptied inside the window.
        queueDrainedFrom: Int? = queueLengthAtWindowStart.takeIf { queueLength == 0 && it > 0 },
        daysSinceLastEvent: Int = 0,
        lifetimeEvents: Int = events,
        lifetimeCompletions: Int = completions,
        ageDays: Int = 100,
        focusSessions: Int = 0,
        focusMinutes: Int = 0,
        swapsInWindow: Int = 0,
        dormantDaysBeforeReturn: Int? = null,
        // The day the gap started, derived from the gap so a fixture cannot hold a length
        // and a date that disagree. Day zero is the window opening in every fixture here,
        // so a nine day gap started on day minus nine.
        dormancyStartKey: String? = dormantDaysBeforeReturn?.let { dateKey(-it) },
        completionsSinceActiveItemStarted: Int = 0,
        weekEventsSeries: List<Int> = emptyList(),
        dipPrecedent: Precedent = Precedent.INSUFFICIENT,
    ) = AreaFacts(
        areaId = areaId,
        nameSnapshot = name,
        colorHex = "#4C6B8A",
        eventsInWindow = events,
        completionsInWindow = completions,
        additionsInWindow = additions,
        swapsInWindow = swapsInWindow,
        shareOfEvents = share,
        hasActiveItem = activeItemId != null,
        activeItemId = activeItemId,
        activeItemTitleSnapshot = activeItemTitle,
        activeItemAgeDays = activeItemAgeDays,
        queueLength = queueLength,
        queueLengthAtWindowStart = queueLengthAtWindowStart,
        queueDelta = queueLength - queueLengthAtWindowStart,
        queueDrainedFrom = queueDrainedFrom,
        daysSinceLastEvent = daysSinceLastEvent,
        dormantDaysBeforeReturn = dormantDaysBeforeReturn,
        dormancyStartKey = dormancyStartKey,
        completionsSinceActiveItemStarted = completionsSinceActiveItemStarted,
        lifetimeEvents = lifetimeEvents,
        lifetimeCompletions = lifetimeCompletions,
        ageDays = ageDays,
        isNew = ageDays < NEW_AREA_DAYS,
        focusSecondsInWindow = focusMinutes * SECONDS_PER_MINUTE,
        focusSessionsInWindow = focusSessions,
        weekEventsSeries = weekEventsSeries,
        dipPrecedent = dipPrecedent,
    )

    private const val NEW_AREA_DAYS = 14

    fun rollup(
        areas: Map<AreaId, AreaFacts>,
        dominantAreaId: AreaId? = null,
        neglected: List<AreaId> = emptyList(),
        dormantReturned: List<AreaId> = emptyList(),
        queueDrained: List<AreaId> = emptyList(),
        queueGrowing: List<AreaId> = emptyList(),
        freshStart: List<AreaId> = emptyList(),
    ) = RollupFacts(
        areasWithEvents = areas.values.count { it.eventsInWindow > 0 },
        areasTotal = areas.size,
        areasIdle = areas.values.count { it.eventsInWindow == 0 },
        dominantAreaId = dominantAreaId,
        dominantShare = dominantAreaId?.let { areas[it]?.shareOfEvents } ?: 0.0,
        neglectedAreaIds = neglected,
        dormantReturnedAreaIds = dormantReturned,
        queueDrainedAreaIds = queueDrained,
        queueGrowingAreaIds = queueGrowing,
        freshStartAreaIds = freshStart,
    )

    fun items(
        areas: Map<AreaId, AreaFacts> = emptyMap(),
        completed: List<CompletedItem> = emptyList(),
        medianDaysToComplete: Int? = null,
    ): ItemFacts {
        val active = areas.values.mapNotNull { area ->
            val id = area.activeItemId ?: return@mapNotNull null
            area.areaId to ActiveItem(
                itemId = id,
                titleSnapshot = area.activeItemTitleSnapshot.orEmpty(),
                ageDays = area.activeItemAgeDays ?: 0,
                areaNameSnapshot = area.nameSnapshot,
            )
        }.toMap()
        val longest = active.values.maxByOrNull { it.ageDays }
        return ItemFacts(
            activeByArea = active,
            longestActiveItemId = longest?.itemId,
            longestActiveDays = longest?.ageDays ?: 0,
            completedInWindow = completed,
            medianDaysToComplete = medianDaysToComplete,
        )
    }

    fun history(
        daysSinceInstall: Int = 90,
        lifetimeCompletions: Int = 20,
        lastWeekCompletions: Int? = null,
        weekCompletions: List<Int> = emptyList(),
        weekQueueSizes: List<Int> = emptyList(),
        weekTotalEvents: List<Int> = emptyList(),
        dominantAreaLastThreeWeeks: List<AreaId?> = emptyList(),
        personalBestWeekCompletions: Int = 0,
        personalBestWeekKey: String? = null,
        weeksSincePersonalBest: Int? = null,
        mostRecentBetterWeekKey: String? = null,
        longestEverActiveDays: Int = 0,
        longestEverActiveItemId: String? = null,
        personalBestFocusMinutesWeek: Int = 0,
        firstEverFlags: Set<FirstEver> = emptySet(),
        weekAreaCounts: List<Int> = emptyList(),
        weekFocusStarted: List<Int> = emptyList(),
        weekFocusCompleted: List<Int> = emptyList(),
        weekFocusEndedEarly: List<Int> = emptyList(),
        weekWeekendEvents: List<Int> = emptyList(),
        currentQuietRunDays: Int = 0,
        currentSingleAreaRunDays: Int = 0,
        currentSingleAreaRunAreaId: AreaId? = null,
        estimatedCompletions: Int = 0,
        activeToEstimateRatio: Double? = null,
        activityDipPrecedent: Precedent = Precedent.INSUFFICIENT,
        focusDipPrecedent: Precedent = Precedent.INSUFFICIENT,
        isJustBackFromAbsence: Boolean = false,
        // The dates behind the weekly series, one entry per bucket, oldest first, spaced
        // seven days and ending on day zero. Every fixture here opens its window on day
        // zero, which is where the extractor puts the newest bucket's first day, so the
        // default lines the keys up with the numbers rather than leaving a caller to.
        weekStartKeys: List<String> = weekStartKeysFor(
            maxOf(
                weekCompletions.size, weekQueueSizes.size, weekTotalEvents.size,
                weekAreaCounts.size, weekFocusStarted.size,
            ),
        ),
        // Derived from the key, so a fixture naming a better week gets its length for free
        // and the two can never disagree. Null when the named week is outside the series,
        // which is what a real history does when the record is older than twelve buckets.
        weeksSinceBetterWeek: Int? =
            weekStartKeys.indexOf(mostRecentBetterWeekKey).takeIf { it >= 0 }?.let { weekStartKeys.size - 1 - it },
    ) = HistoryFacts(
        daysSinceInstall = daysSinceInstall,
        weeksOfData = daysSinceInstall / DAYS_PER_WEEK,
        isFirstWeekEver = daysSinceInstall < DAYS_PER_WEEK,
        lifetimeCompletions = lifetimeCompletions,
        lastWeekCompletions = lastWeekCompletions,
        weekStartKeySeries = weekStartKeys,
        weekCompletionsSeries = weekCompletions,
        weekQueueSizeSeries = weekQueueSizes,
        weekTotalEventsSeries = weekTotalEvents,
        weekAreaCountSeries = weekAreaCounts,
        weekFocusStartedSeries = weekFocusStarted,
        weekFocusCompletedSeries = weekFocusCompleted,
        weekFocusEndedEarlySeries = weekFocusEndedEarly,
        weekWeekendEventsSeries = weekWeekendEvents,
        weekOverWeekDelta = null,
        completionsTrend = Trend.of(weekCompletions),
        queueSizeTrend = Trend.of(weekQueueSizes),
        activityTrend = Trend.of(weekTotalEvents),
        dominantAreaLastThreeWeeks = dominantAreaLastThreeWeeks,
        personalBestWeekCompletions = personalBestWeekCompletions,
        personalBestWeekKey = personalBestWeekKey,
        weeksSincePersonalBest = weeksSincePersonalBest,
        mostRecentBetterWeekKey = mostRecentBetterWeekKey,
        weeksSinceBetterWeek = weeksSinceBetterWeek,
        longestEverActiveDays = longestEverActiveDays,
        longestEverActiveItemId = longestEverActiveItemId,
        personalBestFocusMinutesWeek = personalBestFocusMinutesWeek,
        firstEverFlags = firstEverFlags,
        currentQuietRunDays = currentQuietRunDays,
        currentSingleAreaRunDays = currentSingleAreaRunDays,
        currentSingleAreaRunAreaId = currentSingleAreaRunAreaId,
        estimatedCompletions = estimatedCompletions,
        activeToEstimateRatio = activeToEstimateRatio,
        estimateTendency = EstimateTendency.of(activeToEstimateRatio),
        activityDipPrecedent = activityDipPrecedent,
        focusDipPrecedent = focusDipPrecedent,
        isJustBackFromAbsence = isJustBackFromAbsence,
    )

    /** [count] bucket start days, oldest first, the newest of them day zero. */
    fun weekStartKeysFor(count: Int): List<String> =
        (count - 1 downTo 0).map { dateKey(-DAYS_PER_WEEK * it) }

    private const val DAYS_PER_WEEK = 7

    fun pulse(
        answeredLifetime: Int = 0,
        answeredInWindow: Int = 0,
        positiveInWindow: Int = 0,
        flaggedInWindow: Int = 0,
        lastGeneratedFamily: String? = null,
        lastGeneratedDateKey: String? = null,
        recentAnswers: List<AnsweredPulse> = emptyList(),
    ) = PulseFacts(
        answeredLifetime = answeredLifetime,
        answeredInWindow = answeredInWindow,
        positiveInWindow = positiveInWindow,
        flaggedInWindow = flaggedInWindow,
        lastGeneratedFamily = lastGeneratedFamily,
        lastGeneratedDateKey = lastGeneratedDateKey,
        recentAnswers = recentAnswers,
        answersByFamily = recentAnswers.groupBy { it.family },
    )

    /** A fact set with everything empty, which every test then narrows. */
    fun factSet(
        window: WindowFacts = window(),
        areas: List<AreaFacts> = emptyList(),
        dominantAreaId: AreaId? = null,
        rollup: RollupFacts? = null,
        items: ItemFacts? = null,
        history: HistoryFacts = history(),
        pulse: PulseFacts = pulse(),
        cues: CueFacts = CueFacts.NONE,
    ): FactSet {
        val byId = areas.associateBy { it.areaId }
        return FactSet(
            window = window,
            areas = byId,
            rollup = rollup ?: EngineFacts.rollup(byId, dominantAreaId),
            items = items ?: EngineFacts.items(byId),
            history = history,
            pulse = pulse,
            cues = cues,
        )
    }
}
