package com.kamsiob.claritynow.domain.engine.validate

import com.kamsiob.claritynow.domain.engine.ActiveItem
import com.kamsiob.claritynow.domain.engine.AnsweredPulse
import com.kamsiob.claritynow.domain.engine.AreaFacts
import com.kamsiob.claritynow.domain.engine.AreaId
import com.kamsiob.claritynow.domain.engine.CompletedItem
import com.kamsiob.claritynow.domain.engine.CueFacts
import com.kamsiob.claritynow.domain.engine.FactRef
import com.kamsiob.claritynow.domain.engine.FactSet
import com.kamsiob.claritynow.domain.engine.FirstEver
import com.kamsiob.claritynow.domain.engine.HistoryFacts
import com.kamsiob.claritynow.domain.engine.ItemFacts
import com.kamsiob.claritynow.domain.engine.ItemId
import com.kamsiob.claritynow.domain.engine.PartOfDay
import com.kamsiob.claritynow.domain.engine.PulseFacts
import com.kamsiob.claritynow.domain.engine.RollupFacts
import com.kamsiob.claritynow.domain.engine.Trend
import com.kamsiob.claritynow.domain.engine.WindowFacts
import com.kamsiob.claritynow.domain.engine.catalog.LengthBand
import com.kamsiob.claritynow.domain.engine.catalog.Purpose
import com.kamsiob.claritynow.domain.engine.catalog.Register
import com.kamsiob.claritynow.domain.engine.catalog.SlotKey
import com.kamsiob.claritynow.domain.engine.realize.Candidate
import com.kamsiob.claritynow.domain.engine.realize.Slot

/**
 * One consistent week, built by hand, and the candidates that describe it.
 *
 * **Built rather than extracted.** The fact tests run `FactExtractor` over a synthetic
 * log, which is right for them: they are checking that the facts are correct. These tests
 * check the opposite thing, that a candidate disagreeing with the facts is stopped, and an
 * extractor cannot produce a candidate that disagrees with the facts it just produced. So
 * the week is written out here, every field named, and the violating candidates are
 * constructed against it deliberately.
 *
 * The week is a real shape rather than a minimum: three areas of which one is idle, an
 * item active nine days, a queue that shrank, two answered Pulses, and a personal best
 * some weeks back. Half the checks in section 8 can only be exercised against a fact set
 * with something in it.
 */
internal object ValidateFixture {

    const val WORK: AreaId = "area-work"
    const val HEALTH: AreaId = "area-health"
    const val READING: AreaId = "area-reading"

    /** An area the person archived. Absent from the fact set, by construction, per 3.1. */
    const val ARCHIVED: AreaId = "area-archived"

    const val ACTIVE_ITEM: ItemId = "item-proposal"
    const val COMPLETED_ITEM: ItemId = "item-invoice"

    /** Holds the longest ever active record. Named by persistence stage 4. */
    const val RECORD_ITEM: ItemId = "item-legacy"

    /** Deleted since the window. Nothing in the fact set resolves it. */
    const val DELETED_ITEM: ItemId = "item-deleted"

    const val ITEM_TITLE = "Rewrite the proposal intro"
    const val WORK_NAME = "Work"

    fun window(
        startInstant: Long = 1_772_000_000_000L,
        endInstant: Long = 1_772_604_800_000L,
        dayCount: Int = 7,
        totalEvents: Int = 12,
        completions: Int = 5,
        additions: Int = 3,
        promotions: Int = 2,
        swaps: Int = 1,
        deletions: Int = 1,
        focusStarted: Int = 3,
        focusCompleted: Int = 2,
        focusEndedEarly: Int = 1,
        focusSecondsTotal: Long = 5_400L,
        focusMinutesTotal: Int = 90,
        activeDays: Int = 4,
        busiestDayKey: String? = "2026-03-11",
        busiestDayCount: Int = 5,
        eventsByPartOfDay: Map<PartOfDay, Int> = mapOf(
            PartOfDay.MORNING to 4,
            PartOfDay.AFTERNOON to 5,
            PartOfDay.EVENING to 3,
            PartOfDay.NIGHT to 0,
        ),
        netFlow: Int = 2,
    ): WindowFacts = WindowFacts(
        startInstant = startInstant,
        endInstant = endInstant,
        dayCount = dayCount,
        totalEvents = totalEvents,
        completions = completions,
        additions = additions,
        promotions = promotions,
        swaps = swaps,
        deletions = deletions,
        focusStarted = focusStarted,
        focusCompleted = focusCompleted,
        focusEndedEarly = focusEndedEarly,
        focusSecondsTotal = focusSecondsTotal,
        focusMinutesTotal = focusMinutesTotal,
        activeDays = activeDays,
        busiestDayKey = busiestDayKey,
        busiestDayCount = busiestDayCount,
        eventsByPartOfDay = eventsByPartOfDay,
        netFlow = netFlow,
    )

    fun area(
        areaId: AreaId,
        nameSnapshot: String,
        eventsInWindow: Int,
        completionsInWindow: Int = 0,
        additionsInWindow: Int = 0,
        shareOfEvents: Double = 0.0,
        activeItemId: ItemId? = null,
        activeItemTitleSnapshot: String? = null,
        activeItemAgeDays: Int? = null,
        queueLength: Int = 0,
        queueLengthAtWindowStart: Int = 0,
        daysSinceLastEvent: Int = 0,
        lifetimeEvents: Int = 20,
        lifetimeCompletions: Int = 8,
        ageDays: Int = 200,
        focusSecondsInWindow: Long = 0L,
        focusSessionsInWindow: Int = 0,
        swapsInWindow: Int = 0,
        dormantDaysBeforeReturn: Int? = null,
        weekEventsSeries: List<Int> = emptyList(),
    ): AreaFacts = AreaFacts(
        areaId = areaId,
        nameSnapshot = nameSnapshot,
        colorHex = "#4A6FA5",
        eventsInWindow = eventsInWindow,
        completionsInWindow = completionsInWindow,
        additionsInWindow = additionsInWindow,
        swapsInWindow = swapsInWindow,
        shareOfEvents = shareOfEvents,
        hasActiveItem = activeItemId != null,
        activeItemId = activeItemId,
        activeItemTitleSnapshot = activeItemTitleSnapshot,
        activeItemAgeDays = activeItemAgeDays,
        queueLength = queueLength,
        queueLengthAtWindowStart = queueLengthAtWindowStart,
        queueDelta = queueLength - queueLengthAtWindowStart,
        daysSinceLastEvent = daysSinceLastEvent,
        dormantDaysBeforeReturn = dormantDaysBeforeReturn,
        lifetimeEvents = lifetimeEvents,
        lifetimeCompletions = lifetimeCompletions,
        ageDays = ageDays,
        isNew = ageDays < 14,
        focusSecondsInWindow = focusSecondsInWindow,
        focusSessionsInWindow = focusSessionsInWindow,
        weekEventsSeries = weekEventsSeries,
    )

    /** Nine of the twelve events, an item active nine days, a queue that halved. */
    fun work(): AreaFacts = area(
        areaId = WORK,
        nameSnapshot = WORK_NAME,
        eventsInWindow = 9,
        completionsInWindow = 4,
        additionsInWindow = 2,
        shareOfEvents = 0.75,
        activeItemId = ACTIVE_ITEM,
        activeItemTitleSnapshot = ITEM_TITLE,
        activeItemAgeDays = 9,
        queueLength = 2,
        queueLengthAtWindowStart = 4,
        lifetimeEvents = 140,
        lifetimeCompletions = 51,
        ageDays = 300,
        focusSecondsInWindow = 3_600L,
        focusSessionsInWindow = 2,
    )

    fun health(): AreaFacts = area(
        areaId = HEALTH,
        nameSnapshot = "Health",
        eventsInWindow = 3,
        completionsInWindow = 1,
        additionsInWindow = 1,
        shareOfEvents = 0.25,
        queueLength = 1,
        queueLengthAtWindowStart = 1,
        daysSinceLastEvent = 1,
    )

    /**
     * Live, visible, and completely absent from this week.
     *
     * This is the area check 1 exists for. It is not archived and not deleted, so nothing
     * upstream removes it, and a rule that reached for it would produce a sentence about
     * a week the area had nothing to do with.
     */
    fun reading(): AreaFacts = area(
        areaId = READING,
        nameSnapshot = "Reading",
        eventsInWindow = 0,
        queueLength = 3,
        queueLengthAtWindowStart = 3,
        daysSinceLastEvent = 21,
        lifetimeEvents = 12,
        lifetimeCompletions = 5,
        ageDays = 400,
    )

    fun rollup(
        areasWithEvents: Int = 2,
        areasTotal: Int = 3,
        areasIdle: Int = 1,
        dominantAreaId: AreaId? = WORK,
        dominantShare: Double = 0.75,
        neglectedAreaIds: List<AreaId> = listOf(READING),
        dormantReturnedAreaIds: List<AreaId> = emptyList(),
        queueDrainedAreaIds: List<AreaId> = emptyList(),
        queueGrowingAreaIds: List<AreaId> = emptyList(),
        freshStartAreaIds: List<AreaId> = emptyList(),
    ): RollupFacts = RollupFacts(
        areasWithEvents = areasWithEvents,
        areasTotal = areasTotal,
        areasIdle = areasIdle,
        dominantAreaId = dominantAreaId,
        dominantShare = dominantShare,
        neglectedAreaIds = neglectedAreaIds,
        dormantReturnedAreaIds = dormantReturnedAreaIds,
        queueDrainedAreaIds = queueDrainedAreaIds,
        queueGrowingAreaIds = queueGrowingAreaIds,
        freshStartAreaIds = freshStartAreaIds,
    )

    fun items(
        activeByArea: Map<AreaId, ActiveItem> = mapOf(
            WORK to ActiveItem(ACTIVE_ITEM, ITEM_TITLE, ageDays = 9, areaNameSnapshot = WORK_NAME),
        ),
        longestActiveItemId: ItemId? = ACTIVE_ITEM,
        longestActiveDays: Int = 9,
        completedInWindow: List<CompletedItem> = listOf(
            CompletedItem(COMPLETED_ITEM, "Send the invoice", HEALTH, "Health", daysActive = 3),
        ),
        medianDaysToComplete: Int? = null,
    ): ItemFacts = ItemFacts(
        activeByArea = activeByArea,
        longestActiveItemId = longestActiveItemId,
        longestActiveDays = longestActiveDays,
        completedInWindow = completedInWindow,
        medianDaysToComplete = medianDaysToComplete,
    )

    fun history(
        daysSinceInstall: Int = 120,
        weeksOfData: Int = 17,
        lifetimeCompletions: Int = 60,
        lastWeekCompletions: Int? = 4,
        weekCompletionsSeries: List<Int> = listOf(3, 4, 5),
        weekQueueSizeSeries: List<Int> = listOf(6, 5, 3),
        weekTotalEventsSeries: List<Int> = listOf(10, 11, 12),
        weekOverWeekDelta: Int? = 1,
        dominantAreaLastThreeWeeks: List<AreaId?> = listOf(WORK, HEALTH, WORK),
        personalBestWeekCompletions: Int = 7,
        personalBestWeekKey: String? = "2026-01-05",
        weeksSincePersonalBest: Int? = 9,
        mostRecentBetterWeekKey: String? = "2026-02-16",
        longestEverActiveDays: Int = 21,
        longestEverActiveItemId: ItemId? = RECORD_ITEM,
        personalBestFocusMinutesWeek: Int = 140,
        firstEverFlags: Set<FirstEver> = emptySet(),
        weekAreaCountSeries: List<Int> = emptyList(),
        weekFocusStartedSeries: List<Int> = emptyList(),
        weekFocusCompletedSeries: List<Int> = emptyList(),
        weekFocusEndedEarlySeries: List<Int> = emptyList(),
        weekWeekendEventsSeries: List<Int> = emptyList(),
        currentQuietRunDays: Int = 0,
        currentSingleAreaRunDays: Int = 0,
        currentSingleAreaRunAreaId: AreaId? = null,
    ): HistoryFacts = HistoryFacts(
        daysSinceInstall = daysSinceInstall,
        weeksOfData = weeksOfData,
        isFirstWeekEver = daysSinceInstall < 7,
        lifetimeCompletions = lifetimeCompletions,
        lastWeekCompletions = lastWeekCompletions,
        weekCompletionsSeries = weekCompletionsSeries,
        weekQueueSizeSeries = weekQueueSizeSeries,
        weekTotalEventsSeries = weekTotalEventsSeries,
        weekAreaCountSeries = weekAreaCountSeries,
        weekFocusStartedSeries = weekFocusStartedSeries,
        weekFocusCompletedSeries = weekFocusCompletedSeries,
        weekFocusEndedEarlySeries = weekFocusEndedEarlySeries,
        weekWeekendEventsSeries = weekWeekendEventsSeries,
        weekOverWeekDelta = weekOverWeekDelta,
        completionsTrend = Trend.of(weekCompletionsSeries),
        queueSizeTrend = Trend.of(weekQueueSizeSeries),
        activityTrend = Trend.of(weekTotalEventsSeries),
        dominantAreaLastThreeWeeks = dominantAreaLastThreeWeeks,
        personalBestWeekCompletions = personalBestWeekCompletions,
        personalBestWeekKey = personalBestWeekKey,
        weeksSincePersonalBest = weeksSincePersonalBest,
        mostRecentBetterWeekKey = mostRecentBetterWeekKey,
        longestEverActiveDays = longestEverActiveDays,
        longestEverActiveItemId = longestEverActiveItemId,
        personalBestFocusMinutesWeek = personalBestFocusMinutesWeek,
        firstEverFlags = firstEverFlags,
        currentQuietRunDays = currentQuietRunDays,
        currentSingleAreaRunDays = currentSingleAreaRunDays,
        currentSingleAreaRunAreaId = currentSingleAreaRunAreaId,
    )

    /** Two answers, both stored with the label the person actually saw. */
    fun pulse(
        recentAnswers: List<AnsweredPulse> = listOf(
            AnsweredPulse("2026-03-12", "persistence", ACTIVE_ITEM, "deep", "Deep work", isPositive = true),
            AnsweredPulse("2026-03-08", "concentration", WORK, "intent", "On purpose", isPositive = true),
        ),
        answeredLifetime: Int = 30,
        answeredInWindow: Int = 2,
        positiveInWindow: Int = 2,
        flaggedInWindow: Int = 0,
        lastGeneratedFamily: String? = "persistence",
        lastGeneratedDateKey: String? = "2026-03-13",
    ): PulseFacts = PulseFacts(
        answeredLifetime = answeredLifetime,
        answeredInWindow = answeredInWindow,
        positiveInWindow = positiveInWindow,
        flaggedInWindow = flaggedInWindow,
        lastGeneratedFamily = lastGeneratedFamily,
        lastGeneratedDateKey = lastGeneratedDateKey,
        recentAnswers = recentAnswers,
        answersByFamily = recentAnswers.groupBy { it.family },
    )

    fun facts(
        window: WindowFacts = window(),
        areas: Map<AreaId, AreaFacts> = listOf(work(), health(), reading()).associateBy { it.areaId },
        rollup: RollupFacts = rollup(),
        items: ItemFacts = items(),
        history: HistoryFacts = history(),
        pulse: PulseFacts = pulse(),
        cues: CueFacts = CueFacts.NONE,
    ): FactSet = FactSet(
        window = window,
        areas = areas,
        rollup = rollup,
        items = items,
        history = history,
        pulse = pulse,
        cues = cues,
    )

    /**
     * A candidate that is true of [facts] in every particular.
     *
     * Every violating candidate in these tests is this one with a single thing changed, so a
     * test that fails names the change rather than a wall of constructor arguments.
     *
     * The [FactRef] is a real one: `itemAgeDays:item-proposal` is the address `Measures`
     * produces for the age of that item, and `FactLookup` reads it back to nine. A fixture
     * carrying an invented address would make every passing test here a test of nothing.
     */
    fun candidate(
        ruleKey: String = "pulse.persistence.s2",
        familyKey: String = "persistence",
        variantKey: String = "persistence.s2.11",
        purpose: Purpose = Purpose.PULSE,
        stage: Int = 2,
        register: Register = Register.REFLECTIVE,
        lengthBand: LengthBand = LengthBand.MEDIUM,
        rendered: String = "Still $ITEM_TITLE. Nine days now.",
        renderedQuestion: String? = "Deep work, or stuck?",
        slots: Map<SlotKey, Slot> = mapOf(
            "itemTitle" to Slot.Text("itemTitle", ITEM_TITLE),
            "ageDays" to Slot.Days("ageDays", 9),
        ),
        sourceFacts: Map<SlotKey, FactRef> = mapOf("ageDays" to FactRef("item", "itemAgeDays:$ACTIVE_ITEM")),
        namedAreaIds: Set<AreaId> = emptySet(),
        namedItemIds: Set<ItemId> = setOf(ACTIVE_ITEM),
        subjectId: String? = ACTIVE_ITEM,
        quotedLabel: String? = null,
    ): Candidate = Candidate(
        ruleKey = ruleKey,
        familyKey = familyKey,
        variantKey = variantKey,
        purpose = purpose,
        stage = stage,
        register = register,
        lengthBand = lengthBand,
        rendered = rendered,
        renderedQuestion = renderedQuestion,
        slots = slots,
        sourceFacts = sourceFacts,
        namedAreaIds = namedAreaIds,
        namedItemIds = namedItemIds,
        subjectId = subjectId,
        quotedLabel = quotedLabel,
    )
}
