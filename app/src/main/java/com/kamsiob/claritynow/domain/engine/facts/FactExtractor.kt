package com.kamsiob.claritynow.domain.engine

import com.kamsiob.claritynow.domain.query.TrailQueries
import com.kamsiob.claritynow.domain.query.TrailWindow
import java.time.LocalDate
import java.time.ZoneId

/**
 * Layer one. CLARITY_LOGIC_ENGINE.md 3.
 *
 * The only layer that touches data. It runs once per invocation and returns a fully
 * populated, immutable [FactSet]. Nothing below it is lazy, and nothing below it may
 * become lazy: a fact computed at validation time was computed against a different
 * log than the fact beside it, and the sentence on the screen would carry a number
 * nothing could reproduce.
 *
 * **Every number here came out of [TrailQueries].** Where this class does arithmetic
 * it is over numbers the facade produced: a share is a division of two of its
 * counts, a weekly series is its per day counts added up, a trend is a comparison of
 * those sums. There is no second path to a count, per MASTER_BUILD_PROMPT 9, and a
 * quantity the facade could not answer was added to the facade rather than computed
 * here.
 *
 * **The [FactSet] is a pure function of the log, the window and the zone.** No
 * instant is read that the window did not supply, so the same log and the same
 * window produce the same facts on a phone and on a desktop, today and next year.
 * That is what the determinism test in 14 rests on, and it is why there is no
 * separate "now" parameter: everything is measured as of the window's own end.
 *
 * @param queries the facade over the event log. Its zone is the zone used here.
 */
class FactExtractor(private val queries: TrailQueries) {

    /** Extract every fact for [window]. */
    fun extract(window: TrailWindow): FactSet =
        Extraction(queries, queries.zone(), window).extract()

    companion object {

        /** Buckets carried in a weekly series. CLARITY_LOGIC_ENGINE.md 3.1, "up to 12". */
        const val SERIES_LENGTH = 12

        /** An area is new for its first two weeks. CLARITY_LOGIC_ENGINE.md 3.1. */
        const val NEW_AREA_DAYS = 14

        /** `RollupFacts.neglectedAreaIds`: lifetime events an area needs before it counts. */
        const val NEGLECT_LIFETIME_EVENTS = 5

        /** `RollupFacts.neglectedAreaIds`: days of silence that make an area neglected. */
        const val NEGLECT_QUIET_DAYS = 7

        /** `CORPUS_1_PULSE.md` rebalance: "activity returned to an area dormant 5 or more days". */
        const val DORMANT_DAYS = 5

        /** `CORPUS_1_PULSE.md` queueDrain: "an area went from 3 or more queued to 0". */
        const val DRAIN_FROM_QUEUE = 3

        /** Completions needed before a median time to complete means anything. */
        const val MEDIAN_MIN_COMPLETIONS = 3

        /** Answers carried on `PulseFacts.recentAnswers`. CLARITY_LOGIC_ENGINE.md 3.1. */
        const val RECENT_ANSWERS = 30

        /**
         * A guard on the bucket walk, not a retention limit.
         *
         * Twenty years of weekly buckets. Events are kept forever, per
         * MASTER_BUILD_PROMPT 9, so the loop that walks back to install has no
         * natural bound and a corrupt wall clock in an imported log would otherwise
         * be able to ask for millions of iterations.
         */
        const val MAX_BUCKETS = 1040
    }
}

/**
 * One extraction, holding the derived values every fact class is built from.
 *
 * A class rather than a long function so that the shared quantities, the window
 * bounds, the day maps and the live area set, are computed once and named once.
 * Several of them are folds of the whole log, and the declaration order below is
 * deliberate: the reads at the window end sit together so that `TrailQueries`'s
 * single fold memo serves all of them, and the two reads at the window start sit
 * together for the same reason.
 */
private class Extraction(
    private val queries: TrailQueries,
    private val zone: ZoneId,
    private val window: TrailWindow,
) {

    private val start = window.fromMillis
    private val end = window.toMillis

    /**
     * The last instant the window describes.
     *
     * The three `AsOf` snapshot resolvers on the facade are inclusive of the instant
     * they are handed, unlike every other bound there, so they are handed this rather
     * than [end]. An area renamed at the exact millisecond the window closed is
     * outside the window and must not reach a sentence about it.
     */
    private val lastInstant = if (end > start) end - 1 else start

    private val startDate: LocalDate = FactDates.dateOf(start, zone)
    private val endDate: LocalDate = FactDates.dateOf(lastInstant, zone)

    private val installAt: Long? = queries.firstEventAt()
    private val installDate: LocalDate? = installAt?.let { FactDates.dateOf(it, zone) }

    // Everything read as the window ended, together, so one fold serves them all.
    private val liveAreas: Set<String> = queries.liveAreaIdsAt(end)
    private val liveItems: Set<String> = queries.liveItemIdsAt(end)
    private val activeItemNow: Map<String, String> = queries.activeItemPerAreaAt(end)
    private val queueNow: Map<String, Int> = queries.queueSizeByAreaAt(end)

    // Everything read as the window began.
    private val activeItemAtStart: Map<String, String> = queries.activeItemPerAreaAt(start)
    private val queueAtStart: Map<String, Int> = queries.queueSizeByAreaAt(start)

    private val totalEvents: Int = queries.totalEvents(start, end)
    private val eventsPerArea: Map<String, Int> = queries.eventsPerArea(start, end)
    private val completionsPerArea: Map<String, Int> = queries.completionsPerArea(start, end)
    private val additionsPerArea: Map<String, Int> = queries.additionsPerArea(start, end)
    private val lifetimeEventsPerArea: Map<String, Int> = queries.eventsPerArea(Long.MIN_VALUE, end)
    private val lifetimeCompletionsPerArea: Map<String, Int> =
        queries.completionsPerArea(Long.MIN_VALUE, end)
    private val focusSecondsPerArea: Map<String, Long> = queries.focusSecondsPerArea(start, end)
    private val focusSessionsPerArea: Map<String, Int> = queries.focusSessionsPerArea(start, end)

    // Lifetime day maps, read once and bucketed here rather than one windowed call
    // per week. A year of history is one pass instead of fifty two.
    private val completionsByDay: Map<String, Int> =
        queries.completionsPerDay(Long.MIN_VALUE, end)
    private val eventsByDay: Map<String, Int> = queries.eventsPerDay(Long.MIN_VALUE, end)
    private val focusSecondsByDay: Map<String, Long> =
        queries.focusSecondsPerDay(Long.MIN_VALUE, end)

    /**
     * Seven day buckets ending with the window, index 0 newest.
     *
     * See `HistoryFacts` for why these are anchored at the window end rather than
     * Sunday aligned. The count reaches back to the oldest event in the log, so a
     * personal best is a lifetime record rather than a record over the twelve weeks
     * the series happens to carry.
     */
    private val bucketCount: Int = run {
        val install = installDate ?: return@run 0
        val span = FactDates.daysBetween(install, endDate)
        if (span < 0) 0 else ((span / 7) + 1).coerceAtMost(FactExtractor.MAX_BUCKETS)
    }

    fun extract(): FactSet {
        val windowFacts = windowFacts()
        val areas = areaFacts()
        val rollup = rollupFacts(areas)
        return FactSet(
            window = windowFacts,
            areas = areas,
            rollup = rollup,
            items = itemFacts(areas),
            history = historyFacts(windowFacts, rollup),
            pulse = pulseFacts(),
            cues = cueFacts(),
        )
    }

    // Window ------------------------------------------------------------------

    private fun windowFacts(): WindowFacts {
        val completions = queries.completionsBetween(start, end)
        val additions = queries.additionsBetween(start, end)
        val focus = queries.focusSessionCounts(start, end)
        val perDay = queries.eventsPerDay(start, end)
        val busiest = perDay.entries
            .sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key })
            .firstOrNull()
        val bands = PartOfDay.entries.associateWith { 0 }.toMutableMap()
        for ((hour, count) in queries.eventsPerHourOfDay(start, end)) {
            val band = PartOfDay.of(hour)
            bands[band] = bands.getValue(band) + count
        }
        return WindowFacts(
            startInstant = start,
            endInstant = end,
            dayCount = if (end <= start) 0 else FactDates.daysBetween(startDate, endDate) + 1,
            totalEvents = totalEvents,
            completions = completions,
            additions = additions,
            promotions = queries.promotionsBetween(start, end),
            swaps = queries.swapsBetween(start, end),
            deletions = queries.deletionsBetween(start, end),
            focusStarted = focus.started,
            focusCompleted = focus.completed,
            focusEndedEarly = focus.endedEarly,
            focusSecondsTotal = queries.focusSecondsTotal(start, end),
            focusMinutesTotal = queries.focusMinutes(start, end),
            activeDays = queries.activeDays(start, end),
            busiestDayKey = busiest?.key,
            busiestDayCount = busiest?.value ?: 0,
            eventsByPartOfDay = bands.toMap(),
            netFlow = completions - additions,
        )
    }

    // Areas -------------------------------------------------------------------

    /**
     * One entry per live area, keyed by id, sorted so two devices iterate alike.
     *
     * An area whose name or color cannot be resolved at the window end is **dropped
     * rather than defaulted**. A placeholder would eventually be printed, and
     * CLARITY_LOGIC_ENGINE.md 1 is blunt about what one fabricated area name costs.
     * In practice this cannot happen: an area is live only if AREA_CREATED is in the
     * log, and that payload carries both. It is written this way so that a merged or
     * imported log missing the creation event costs one area rather than one lie.
     */
    private fun areaFacts(): Map<AreaId, AreaFacts> =
        liveAreas.sorted().mapNotNull { areaFactsFor(it) }.associateBy { it.areaId }

    private fun areaFactsFor(areaId: String): AreaFacts? {
        val name = queries.areaNameAsOf(areaId, lastInstant) ?: return null
        val color = queries.areaColorHexAsOf(areaId, lastInstant) ?: return null
        val events = eventsPerArea[areaId] ?: 0
        val activeItemId = activeItemNow[areaId]
        val queueLength = queueNow[areaId] ?: 0
        val queueStart = queueAtStart[areaId] ?: 0
        val lastEventAt = queries.lastEventForArea(areaId, end)
        val createdAt = queries.areaCreatedAt(areaId)
        val ageDays = if (createdAt == null) 0 else daysTo(createdAt)
        return AreaFacts(
            areaId = areaId,
            nameSnapshot = name,
            colorHex = color,
            eventsInWindow = events,
            completionsInWindow = completionsPerArea[areaId] ?: 0,
            additionsInWindow = additionsPerArea[areaId] ?: 0,
            shareOfEvents = if (totalEvents == 0) 0.0 else events.toDouble() / totalEvents,
            hasActiveItem = activeItemId != null,
            activeItemId = activeItemId,
            activeItemTitleSnapshot = activeItemId?.let { queries.itemTitleAsOf(it, lastInstant) },
            activeItemAgeDays = activeItemId?.let { activeAgeDays(it) },
            queueLength = queueLength,
            queueLengthAtWindowStart = queueStart,
            queueDelta = queueLength - queueStart,
            daysSinceLastEvent = if (lastEventAt == null) Int.MAX_VALUE else daysTo(lastEventAt),
            lifetimeEvents = lifetimeEventsPerArea[areaId] ?: 0,
            lifetimeCompletions = lifetimeCompletionsPerArea[areaId] ?: 0,
            ageDays = ageDays,
            isNew = ageDays < FactExtractor.NEW_AREA_DAYS,
            focusSecondsInWindow = focusSecondsPerArea[areaId] ?: 0L,
            focusSessionsInWindow = focusSessionsPerArea[areaId] ?: 0,
        )
    }

    /**
     * How long the item has been active, in whole local days, as the window ended.
     *
     * Built from the facade's `activeSinceForItem` rather than from its
     * `daysActiveForItem`, which counts to the calendar day of the instant it is
     * handed. The window end is exclusive, so a Pulse window covering yesterday ends
     * at midnight tonight and that function would count today as a day of the item's
     * age. One day too many, on the fact the persistence ladder escalates on.
     */
    private fun activeAgeDays(itemId: String): Int? =
        queries.activeSinceForItem(itemId, end)?.let { daysTo(it) }

    private fun rollupFacts(areas: Map<AreaId, AreaFacts>): RollupFacts {
        val withEvents = areas.values.filter { it.eventsInWindow > 0 }
        val dominant = strictMaxBy(areas.values.toList()) { it.eventsInWindow }
            ?.takeIf { it.eventsInWindow > 0 }
        return RollupFacts(
            areasWithEvents = withEvents.size,
            areasTotal = areas.size,
            areasIdle = areas.size - withEvents.size,
            dominantAreaId = dominant?.areaId,
            dominantShare = dominant?.shareOfEvents ?: 0.0,
            neglectedAreaIds = areas.values.filter {
                it.lifetimeEvents >= FactExtractor.NEGLECT_LIFETIME_EVENTS &&
                    it.daysSinceLastEvent >= FactExtractor.NEGLECT_QUIET_DAYS &&
                    !it.isNew
            }.map { it.areaId },
            dormantReturnedAreaIds = areas.values.filter { returnedFromDormancy(it.areaId) }
                .map { it.areaId },
            queueDrainedAreaIds = areas.values.filter {
                it.queueLengthAtWindowStart >= FactExtractor.DRAIN_FROM_QUEUE && it.queueLength == 0
            }.map { it.areaId },
            queueGrowingAreaIds = areas.values.filter { it.queueDelta > 0 }.map { it.areaId },
            freshStartAreaIds = areas.values.filter { freshStart(it) }.map { it.areaId },
        )
    }

    /**
     * True when this area's activity resumed inside the window after a real gap.
     *
     * The gap is measured from the area's own previous event to its first event in
     * the window, never to the window start, so a revival is a revival rather than an
     * artifact of where the boundary fell. An area with no event before its first one
     * here has not returned from anything, and is a fresh start instead.
     */
    private fun returnedFromDormancy(areaId: String): Boolean {
        val firstIn = queries.firstEventForArea(areaId, start, end) ?: return false
        val previous = queries.lastEventForArea(areaId, firstIn) ?: return false
        val gap = FactDates.daysBetween(
            FactDates.dateOf(previous, zone),
            FactDates.dateOf(firstIn, zone),
        )
        return gap >= FactExtractor.DORMANT_DAYS
    }

    /** Both shapes of `CORPUS_1_PULSE.md` family 11: a new area, or a first item in an empty one. */
    private fun freshStart(area: AreaFacts): Boolean {
        if (area.isNew && area.eventsInWindow > 0) return true
        val wasEmpty = (queueAtStart[area.areaId] ?: 0) == 0 &&
            activeItemAtStart[area.areaId] == null
        return wasEmpty && area.hasActiveItem
    }

    // Items -------------------------------------------------------------------

    private fun itemFacts(areas: Map<AreaId, AreaFacts>): ItemFacts {
        val active = areas.values.mapNotNull { area ->
            val itemId = area.activeItemId ?: return@mapNotNull null
            val title = area.activeItemTitleSnapshot ?: return@mapNotNull null
            val age = area.activeItemAgeDays ?: return@mapNotNull null
            area.areaId to ActiveItem(
                itemId = itemId,
                titleSnapshot = title,
                ageDays = age,
                areaNameSnapshot = area.nameSnapshot,
            )
        }.toMap()
        val longest = active.values.sortedBy { it.itemId }.maxByOrNull { it.ageDays }
        val completed = queries.completedItemsBetween(start, end)
            .filter { it.areaId in liveAreas && it.itemId in liveItems }
            .map {
                CompletedItem(
                    itemId = it.itemId,
                    titleSnapshot = it.titleSnapshot,
                    areaId = it.areaId,
                    areaNameSnapshot = it.areaNameSnapshot,
                    daysActive = it.activeDurationDays,
                )
            }
        return ItemFacts(
            activeByArea = active,
            longestActiveItemId = longest?.itemId,
            longestActiveDays = longest?.ageDays ?: 0,
            completedInWindow = completed,
            medianDaysToComplete = median(completed.map { it.daysActive }),
        )
    }

    /**
     * The median of [values], or null under [FactExtractor.MEDIAN_MIN_COMPLETIONS].
     *
     * An even count takes the mean of the two central values rounded down, because
     * the number renders as whole days and rounding down never overstates how long
     * things take.
     */
    private fun median(values: List<Int>): Int? {
        if (values.size < FactExtractor.MEDIAN_MIN_COMPLETIONS) return null
        val sorted = values.sorted()
        val middle = sorted.size / 2
        return if (sorted.size % 2 == 1) {
            sorted[middle]
        } else {
            (sorted[middle - 1] + sorted[middle]) / 2
        }
    }

    // History -----------------------------------------------------------------

    private fun historyFacts(windowFacts: WindowFacts, rollup: RollupFacts): HistoryFacts {
        val daysSinceInstall = installAt?.let { daysTo(it) } ?: 0
        val seriesIndices = (0 until minOf(bucketCount, FactExtractor.SERIES_LENGTH))
            .reversed()
            .toList()
        val completionsSeries = seriesIndices.map { bucketSum(it, completionsByDay) }
        val eventsSeries = seriesIndices.map { bucketSum(it, eventsByDay) }
        val queueSeries = seriesIndices.map { queries.queueSizeAt(bucketEndMillis(it)) }

        val thisBucket = if (bucketCount > 0) bucketSum(0, completionsByDay) else 0
        val lastBucket = if (bucketCount > 1) bucketSum(1, completionsByDay) else null

        var bestIndex = -1
        var bestCompletions = 0
        var bestFocusMinutes = 0
        var betterIndex = -1
        for (index in 1 until bucketCount) {
            val completions = bucketSum(index, completionsByDay)
            // Ascending index is newest first among the earlier buckets, so a strict
            // comparison keeps the most recent bucket holding a tied record.
            if (bestIndex == -1 || completions > bestCompletions) {
                bestIndex = index
                bestCompletions = completions
            }
            if (betterIndex == -1 && completions > thisBucket) betterIndex = index
            val minutes = (bucketSeconds(index, focusSecondsByDay) / 60L).toInt()
            if (minutes > bestFocusMinutes) bestFocusMinutes = minutes
        }

        val longestEver = longestEverActive()
        return HistoryFacts(
            daysSinceInstall = daysSinceInstall,
            weeksOfData = daysSinceInstall / 7,
            isFirstWeekEver = daysSinceInstall < 7,
            lifetimeCompletions = queries.completionsBetween(Long.MIN_VALUE, end),
            lastWeekCompletions = lastBucket,
            weekCompletionsSeries = completionsSeries,
            weekQueueSizeSeries = queueSeries,
            weekTotalEventsSeries = eventsSeries,
            weekOverWeekDelta = lastBucket?.let { thisBucket - it },
            completionsTrend = Trend.of(completionsSeries),
            queueSizeTrend = Trend.of(queueSeries),
            activityTrend = Trend.of(eventsSeries),
            dominantAreaLastThreeWeeks = listOf(2, 1, 0).map { dominantAreaInBucket(it) },
            personalBestWeekCompletions = if (bestIndex == -1) 0 else bestCompletions,
            personalBestWeekKey = if (bestIndex == -1) {
                null
            } else {
                FactDates.keyOf(bucketFirstDate(bestIndex))
            },
            weeksSincePersonalBest = if (bestIndex == -1) null else bestIndex,
            mostRecentBetterWeekKey = if (betterIndex == -1) {
                null
            } else {
                FactDates.keyOf(bucketFirstDate(betterIndex))
            },
            longestEverActiveDays = longestEver?.second ?: 0,
            longestEverActiveItemId = longestEver?.first,
            personalBestFocusMinutesWeek = bestFocusMinutes,
            firstEverFlags = firstEverFlags(windowFacts, rollup),
        )
    }

    /**
     * The item that has been active longest, ever, and for how many days.
     *
     * Read over both sides of the question: the items active right now, whose age is
     * measured to the window end, and every item ever completed, whose age is the
     * `activeDurationDays` its own completion payload recorded. Recomputing a
     * completed item's duration here would contradict the figure the device that
     * finished it had already shown the person.
     *
     * Tombstoned items and items in areas the person can no longer see are excluded,
     * so the record cannot be held by something unnameable. Candidates are sorted by
     * id and compared strictly, so a tie resolves to the lower id on every device.
     */
    private fun longestEverActive(): Pair<ItemId, Int>? {
        val candidates = ArrayList<Pair<ItemId, Int>>()
        for (areaId in liveAreas) {
            val itemId = activeItemNow[areaId] ?: continue
            val age = activeAgeDays(itemId) ?: continue
            candidates += itemId to age
        }
        for (record in queries.completedItemsBetween(Long.MIN_VALUE, end)) {
            if (record.areaId !in liveAreas || record.itemId !in liveItems) continue
            candidates += record.itemId to record.activeDurationDays
        }
        return candidates.sortedBy { it.first }.maxByOrNull { it.second }
    }

    /**
     * The firsts that happened inside this window.
     *
     * Each is a pair of questions: did it happen here, and had it never happened
     * before. The two that cannot be answered by a count, a queue drain and a week
     * with every area active, are answered by walking the earlier buckets. That walk
     * only runs when the first half is true, so the cost is paid on the rare window
     * where there is something to check and never on an ordinary one, and it stops at
     * the oldest earlier occurrence rather than walking to install.
     */
    private fun firstEverFlags(windowFacts: WindowFacts, rollup: RollupFacts): Set<FirstEver> {
        val flags = LinkedHashSet<FirstEver>()
        if (windowFacts.completions > 0 && queries.completionsBetween(Long.MIN_VALUE, start) == 0) {
            flags += FirstEver.FIRST_COMPLETION
        }
        if (windowFacts.focusStarted > 0 &&
            queries.focusSessionCounts(Long.MIN_VALUE, start).started == 0
        ) {
            flags += FirstEver.FIRST_FOCUS_SESSION
        }
        if (windowFacts.swaps > 0 && queries.swapsBetween(Long.MIN_VALUE, start) == 0) {
            flags += FirstEver.FIRST_SWAP
        }
        if (queries.areaArchivesBetween(start, end) > 0 &&
            queries.areaArchivesBetween(Long.MIN_VALUE, start) == 0
        ) {
            flags += FirstEver.FIRST_AREA_ARCHIVED
        }
        if (rollup.queueDrainedAreaIds.isNotEmpty() && (1 until bucketCount).none(::bucketDrained)) {
            flags += FirstEver.FIRST_QUEUE_DRAIN
        }
        if (rollup.areasTotal > 0 &&
            rollup.areasIdle == 0 &&
            (1 until bucketCount).none(::bucketHadEveryAreaActive)
        ) {
            flags += FirstEver.FIRST_WEEK_WITH_ALL_AREAS_ACTIVE
        }
        return flags
    }

    private fun bucketDrained(index: Int): Boolean {
        val before = queries.queueSizeByAreaAt(bucketStartMillis(index))
        val after = queries.queueSizeByAreaAt(bucketEndMillis(index))
        return before.any { (areaId, size) ->
            size >= FactExtractor.DRAIN_FROM_QUEUE && (after[areaId] ?: 0) == 0
        }
    }

    private fun bucketHadEveryAreaActive(index: Int): Boolean {
        val live = queries.liveAreaIdsAt(bucketEndMillis(index))
        if (live.isEmpty()) return false
        val events = queries.eventsPerArea(bucketStartMillis(index), bucketEndMillis(index))
        return live.all { (events[it] ?: 0) > 0 }
    }

    /** The dominant area of one bucket, or null on a tie, on no events, or on no bucket. */
    private fun dominantAreaInBucket(index: Int): AreaId? {
        if (index >= bucketCount) return null
        val events = queries.eventsPerArea(bucketStartMillis(index), bucketEndMillis(index))
            .filterKeys { it in liveAreas }
            .filterValues { it > 0 }
        return strictMaxBy(events.entries.sortedBy { it.key }) { it.value }?.key
    }

    // Pulse -------------------------------------------------------------------

    private fun pulseFacts(): PulseFacts {
        val generated = queries.pulsesGeneratedBetween(Long.MIN_VALUE, end)
        val byId = generated.associateBy { it.pulseId }
        val answers = queries.pulseAnswersBetween(Long.MIN_VALUE, end)
        val inWindow = queries.pulseAnswersBetween(start, end)
        val positive = inWindow.count { it.responseIsPositive }

        // Newest first is the reverse of the log's total order, which is causal and
        // identical on both devices after a merge. Ordering by an answer's wall clock
        // would reorder two answers written in the same second on two devices.
        val recent = answers.asReversed().mapNotNull { answer ->
            val pulse = byId[answer.pulseId] ?: return@mapNotNull null
            AnsweredPulse(
                dateKey = pulse.dateKey,
                family = pulse.family,
                subjectId = answer.subjectId ?: pulse.subjectId,
                responseKey = answer.responseKey,
                responseLabel = answer.responseLabel,
                isPositive = answer.responseIsPositive,
            )
        }.take(FactExtractor.RECENT_ANSWERS)

        var newestKey: String? = null
        var newestFamily: FamilyKey? = null
        for (pulse in generated) {
            val seen = newestKey
            if (seen == null || pulse.dateKey >= seen) {
                newestKey = pulse.dateKey
                newestFamily = pulse.family
            }
        }
        return PulseFacts(
            answeredLifetime = answers.size,
            answeredInWindow = inWindow.size,
            positiveInWindow = positive,
            flaggedInWindow = inWindow.size - positive,
            lastGeneratedFamily = newestFamily,
            lastGeneratedDateKey = newestKey,
            recentAnswers = recent,
            answersByFamily = recent.groupBy { it.family },
        )
    }

    // Cues --------------------------------------------------------------------

    /**
     * The twelve week rhythms, gated on all three thresholds in 3.7.
     *
     * The buckets are the same seven day buckets the weekly series uses, so a "week"
     * means one thing across the whole `FactSet`. The newest bucket is included even
     * when the window ends part way through it, which very slightly dilutes a
     * confidence: the alternative, dropping it, would make a cue that has held for
     * six weeks unusable in the seventh, and the thresholds are already the
     * conservative side of the argument.
     */
    private fun cueFacts(): CueFacts {
        val weeks = minOf(bucketCount, CueFacts.Thresholds.WINDOW_WEEKS)
        if (weeks < CueFacts.Thresholds.MIN_WEEKS) return CueFacts.NONE
        val indices = (0 until weeks).toList()
        val cueStart = bucketStartMillis(weeks - 1)

        val weekdayTotals = weekdayCounts(eventsByDay, cueStart, end)
        val weekdayPerWeek = indices.map { weekdayCountsInBucket(it, eventsByDay) }
        val cueEvents = weekdayTotals.values.sum()
        val strongest = cue(weekdayTotals, weekdayPerWeek)

        // The count floor sits on the whole cue window for the quietest day rather
        // than on that day's own count, which would make the quietest day
        // undetectable by construction: the floor would be asking a day to be busy.
        val quietest = cue(weekdayTotals, weekdayPerWeek, highest = false, floorOnWinner = false)

        val productive = cue(
            bandCounts(queries.eventsPerHourOfDay(cueStart, end)),
            indices.map {
                bandCounts(queries.eventsPerHourOfDay(bucketStartMillis(it), bucketEndMillis(it)))
            },
        )

        val focusByDay = queries.focusStartsPerDay(Long.MIN_VALUE, end)
        val focusWeekday = cue(
            weekdayCounts(focusByDay, cueStart, end),
            indices.map { weekdayCountsInBucket(it, focusByDay) },
        )
        val focusBand = cue(
            bandCounts(queries.focusStartsPerHourOfDay(cueStart, end)),
            indices.map {
                bandCounts(
                    queries.focusStartsPerHourOfDay(bucketStartMillis(it), bucketEndMillis(it)),
                )
            },
        )
        val adding = cue(
            bandCounts(queries.additionsPerHourOfDay(cueStart, end)),
            indices.map {
                bandCounts(
                    queries.additionsPerHourOfDay(bucketStartMillis(it), bucketEndMillis(it)),
                )
            },
        )

        val weekendEvents = weekdayTotals.filterKeys { it.isWeekend }.values.sum()
        val cleared = listOfNotNull(strongest, quietest, productive, focusWeekday, focusBand, adding)
        return CueFacts(
            strongestWeekday = strongest?.key,
            strongestWeekdayConfidence = strongest?.confidence ?: 0.0,
            quietestWeekday = quietest?.key,
            productiveBand = productive?.key,
            productiveBandShare = if (productive != null && cueEvents > 0) {
                productive.count.toDouble() / cueEvents
            } else {
                0.0
            },
            focusTypicalWeekday = focusWeekday?.key,
            focusTypicalBand = focusBand?.key,
            addingBand = adding?.key,
            weekdayOnly = cueEvents >= CueFacts.Thresholds.MIN_EVENTS &&
                weekendEvents.toDouble() / cueEvents < CueFacts.Thresholds.WEEKEND_SHARE_CEILING,
            hasStableRhythm = cleared.isNotEmpty(),
        )
    }

    /**
     * One cue, or null when it did not clear all three thresholds in 3.7.
     *
     * The week count is checked by the caller, because it is the same for every cue.
     * This checks the other two: the underlying count, and the share of weeks in
     * which the pattern held.
     *
     * [floorOnWinner] chooses what the count floor is asked of. For a busiest day or
     * a productive band it is the winner's own count, which is the quantity the cue
     * is a claim about. For a quietest day it is the whole cue window, because a
     * floor on the quiet day itself would be a contradiction.
     */
    private fun <T : Comparable<T>> cue(
        totals: Map<T, Int>,
        perWeek: List<Map<T, Int>>,
        highest: Boolean = true,
        floorOnWinner: Boolean = true,
    ): CueReading<T>? {
        val entries = totals.entries.sortedBy { it.key }
        val winner = if (highest) {
            strictMaxBy(entries) { it.value }
        } else {
            strictMinBy(entries) { it.value }
        } ?: return null
        val underlying = if (floorOnWinner) winner.value else totals.values.sum()
        if (underlying < CueFacts.Thresholds.MIN_EVENTS) return null
        val held = confidence(perWeek, winner.key, highest)
        if (held < CueFacts.Thresholds.MIN_CONFIDENCE) return null
        return CueReading(winner.key, winner.value, held)
    }

    /** Day keyed counts folded onto weekdays, with every weekday present including zeros. */
    private fun weekdayCounts(
        byDay: Map<String, Int>,
        fromMillis: Long,
        toMillis: Long,
    ): Map<Weekday, Int> {
        val from = FactDates.dateOf(fromMillis, zone)
        val to = FactDates.dateOf(if (toMillis > fromMillis) toMillis - 1 else fromMillis, zone)
        val counts = Weekday.entries.associateWith { 0 }.toMutableMap()
        for ((key, count) in byDay) {
            val date = FactDates.parse(key) ?: continue
            if (date < from || date > to) continue
            val day = Weekday.of(date.dayOfWeek)
            counts[day] = counts.getValue(day) + count
        }
        return counts.toMap()
    }

    private fun weekdayCountsInBucket(index: Int, byDay: Map<String, Int>): Map<Weekday, Int> =
        weekdayCounts(byDay, bucketStartMillis(index), bucketEndMillis(index))

    /** Hour keyed counts folded onto bands, with every band present including zeros. */
    private fun bandCounts(byHour: Map<Int, Int>): Map<PartOfDay, Int> {
        val counts = PartOfDay.entries.associateWith { 0 }.toMutableMap()
        for ((hour, count) in byHour) {
            val band = PartOfDay.of(hour)
            counts[band] = counts.getValue(band) + count
        }
        return counts.toMap()
    }

    /**
     * The share of weeks in which [key] was that week's own extreme.
     *
     * A tied week does not count as holding, which is what makes this a measure of a
     * pattern rather than of a coincidence.
     */
    private fun <T : Comparable<T>> confidence(
        perWeek: List<Map<T, Int>>,
        key: T,
        highest: Boolean,
    ): Double {
        if (perWeek.isEmpty()) return 0.0
        val held = perWeek.count { week ->
            val entries = week.entries.sortedBy { it.key }
            val winner = if (highest) {
                strictMaxBy(entries) { it.value }
            } else {
                strictMinBy(entries) { it.value }
            }
            winner?.key == key
        }
        return held.toDouble() / perWeek.size
    }

    // Buckets and dates -------------------------------------------------------

    private fun bucketFirstDate(index: Int): LocalDate = endDate.minusDays(7L * index + 6L)

    private fun bucketStartMillis(index: Int): Long =
        FactDates.startOfDayMillis(bucketFirstDate(index), zone)

    /**
     * The exclusive upper bound of a bucket, never past the window end.
     *
     * The newest bucket runs to the end of its last calendar day, which is in the
     * future when the window closes at midday. Clamping keeps every fact a function
     * of the window alone: without it, a merged log holding an event stamped later
     * today would change the answer depending on when extraction ran.
     */
    private fun bucketEndMillis(index: Int): Long = minOf(
        FactDates.startOfDayMillis(bucketFirstDate(index).plusDays(7L), zone),
        end,
    )

    private fun bucketDayKeys(index: Int): List<String> {
        val first = bucketFirstDate(index)
        return (0L until 7L).map { FactDates.keyOf(first.plusDays(it)) }
    }

    private fun bucketSum(index: Int, byDay: Map<String, Int>): Int =
        bucketDayKeys(index).sumOf { byDay[it] ?: 0 }

    /** The seconds variant. Named apart because the two erase to one JVM signature. */
    private fun bucketSeconds(index: Int, byDay: Map<String, Long>): Long =
        bucketDayKeys(index).sumOf { byDay[it] ?: 0L }

    /** Whole local days from an instant to the last day the window describes. */
    private fun daysTo(atMillis: Long): Int =
        FactDates.daysBetween(FactDates.dateOf(atMillis, zone), endDate).coerceAtLeast(0)
}

/** A cue that cleared its thresholds: what it is, how big, and how often it held. */
private data class CueReading<T>(val key: T, val count: Int, val confidence: Double)

/**
 * The single greatest element, or null when nothing is greatest on its own.
 *
 * Null on a tie, deliberately, and every caller wants that. "Work carried the week"
 * is a false sentence when Work and Health both had four events, and a deterministic
 * tie break would produce a winner that is still the wrong claim.
 */
private fun <T> strictMaxBy(items: List<T>, value: (T) -> Int): T? {
    if (items.isEmpty()) return null
    val best = items.maxOf(value)
    val winners = items.filter { value(it) == best }
    return winners.singleOrNull()
}

/** The single least element, or null on a tie. The mirror of [strictMaxBy]. */
private fun <T> strictMinBy(items: List<T>, value: (T) -> Int): T? {
    if (items.isEmpty()) return null
    val least = items.minOf(value)
    val winners = items.filter { value(it) == least }
    return winners.singleOrNull()
}
