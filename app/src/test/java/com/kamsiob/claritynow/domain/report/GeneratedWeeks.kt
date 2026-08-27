package com.kamsiob.claritynow.domain.report

import com.kamsiob.claritynow.domain.engine.AnsweredPulse
import com.kamsiob.claritynow.domain.engine.FactSet
import com.kamsiob.claritynow.domain.engine.FiringHistory
import com.kamsiob.claritynow.domain.engine.StableHash
import com.kamsiob.claritynow.domain.engine.catalog.ClarityCatalog
import com.kamsiob.claritynow.domain.engine.realize.EngineFacts

/**
 * Ten thousand weeks, built from a hash of a case number and nothing else.
 *
 * The shape follows `EngineDeterminismTest`'s generator, and for the same reason: the
 * ranges straddle the thresholds the corpus states rather than sitting inside them, so a
 * run crosses every share floor, every event floor and every stage boundary instead of
 * generating ten thousand copies of one week. A generator that stayed inside one band would
 * make the composition rules look like they hold when they had never been tested.
 *
 * **Deterministic, so a failure is reproducible.** Case 6,214 is the same week on every
 * machine and every run, which is what makes a property failure something a person can go
 * and look at rather than something they have to catch again.
 */
internal object GeneratedWeeks {

    /** Section 14 and issue #6 both state the number. */
    const val CASES: Int = 10_000

    fun facts(case: Int): FactSet {
        fun pick(label: String, bound: Int) = StableHash.bucket("$label|$case", bound)

        // The window slides across a year so the variant hash is salted differently on
        // every case. Ten thousand weeks that all ended on one day would exercise one
        // day's worth of the corpus and call it coverage.
        val offset = pick("day", DATE_SPREAD)
        val areaCount = pick("areas", MAX_AREAS + 1)
        val totalEvents = pick("events", MAX_EVENTS)
        val areas = (0 until areaCount).map { index ->
            val events = pick("areaEvents$index", MAX_EVENTS)
            val hasActive = pick("hasActive$index", 3) != 0
            EngineFacts.area(
                areaId = "area-$index",
                name = AREA_NAMES[index % AREA_NAMES.size],
                events = events,
                completions = pick("areaDone$index", events + 1),
                additions = pick("areaAdd$index", events + 1),
                share = if (totalEvents == 0) 0.0 else (events.toDouble() / totalEvents).coerceAtMost(1.0),
                activeItemId = if (hasActive) "item-$index" else null,
                activeItemTitle = if (hasActive) ITEM_TITLES[index % ITEM_TITLES.size] else null,
                activeItemAgeDays = if (hasActive) pick("age$index", MAX_ITEM_AGE) else null,
                queueLength = pick("queue$index", MAX_QUEUE),
                queueLengthAtWindowStart = pick("queueWas$index", MAX_QUEUE),
                daysSinceLastEvent = pick("quiet$index", MAX_QUIET_DAYS),
                lifetimeEvents = events + pick("lifetime$index", MAX_LIFETIME),
                lifetimeCompletions = pick("lifetimeDone$index", MAX_LIFETIME),
                ageDays = pick("areaAge$index", MAX_AREA_AGE),
                focusSessions = pick("sessions$index", MAX_SESSIONS),
                focusMinutes = pick("minutes$index", MAX_FOCUS_MINUTES),
            )
        }
        val weeks = (0 until WEEK_SERIES).map { pick("week$it", MAX_EVENTS) }
        val answers = (0 until pick("answers", MAX_ANSWERS)).map { index ->
            AnsweredPulse(
                dateKey = EngineFacts.dateKey(-pick("answerOn$index", MAX_EXCLUSION_DAYS)),
                family = PULSE_FAMILIES[pick("answerFamily$index", PULSE_FAMILIES.size)],
                subjectId = if (pick("answerSubject$index", 2) == 0) {
                    null
                } else {
                    areas.getOrNull(pick("answerWhich$index", MAX_AREAS))?.activeItemId
                        ?: "area-${pick("answerWhich$index", MAX_AREAS)}"
                },
                responseKey = "r$index",
                responseLabel = RESPONSE_LABELS[index % RESPONSE_LABELS.size],
                isPositive = pick("answerPositive$index", 2) == 0,
            )
        }
        return EngineFacts.factSet(
            window = EngineFacts.window(
                startDay = offset,
                endDay = offset + WINDOW_DAYS,
                totalEvents = totalEvents,
                completions = pick("completions", MAX_EVENTS),
                additions = pick("additions", MAX_EVENTS),
                promotions = pick("promotions", MAX_SMALL),
                swaps = pick("swaps", MAX_SMALL),
                deletions = pick("deletions", MAX_SMALL),
                focusStarted = pick("focusStarted", MAX_SMALL),
                focusCompleted = pick("focusCompleted", MAX_SMALL),
                focusEndedEarly = pick("focusEndedEarly", MAX_SMALL),
                focusMinutes = pick("focusMinutes", MAX_FOCUS_MINUTES),
                activeDays = pick("activeDays", WINDOW_DAYS + 1),
                busiestDayKey = if (totalEvents == 0) {
                    null
                } else {
                    EngineFacts.dateKey(offset + pick("busiest", WINDOW_DAYS))
                },
                busiestDayCount = pick("busiestCount", totalEvents + 1),
            ),
            areas = areas,
            dominantAreaId = areas.maxByOrNull { it.eventsInWindow }?.areaId,
            history = EngineFacts.history(
                daysSinceInstall = pick("install", MAX_INSTALL_DAYS),
                lifetimeCompletions = pick("lifetimeCompletions", MAX_LIFETIME),
                lastWeekCompletions = if (pick("hasLastWeek", 4) == 0) null else pick("lastWeek", MAX_EVENTS),
                weekCompletions = weeks,
                weekQueueSizes = (0 until WEEK_SERIES).map { pick("queueWeek$it", MAX_QUEUE) },
                weekTotalEvents = (0 until WEEK_SERIES).map { pick("eventWeek$it", MAX_EVENTS) },
                dominantAreaLastThreeWeeks = (0 until DOMINANT_WEEKS).map {
                    areas.getOrNull(pick("dom$it", MAX_AREAS))?.areaId
                },
                personalBestWeekCompletions = pick("best", MAX_EVENTS),
                personalBestWeekKey = EngineFacts.dateKey(-pick("bestWeek", MAX_INSTALL_DAYS)),
                weeksSincePersonalBest = pick("sinceBest", WEEK_SERIES),
                mostRecentBetterWeekKey = if (pick("better", 3) == 0) {
                    null
                } else {
                    EngineFacts.dateKey(-pick("betterWeek", MAX_INSTALL_DAYS))
                },
                longestEverActiveDays = pick("longest", MAX_ITEM_AGE),
                longestEverActiveItemId = areas.firstOrNull()?.activeItemId,
                personalBestFocusMinutesWeek = pick("bestFocus", MAX_FOCUS_MINUTES),
            ),
            pulse = EngineFacts.pulse(
                answeredLifetime = pick("answered", MAX_LIFETIME),
                answeredInWindow = pick("answeredWindow", MAX_SMALL),
                positiveInWindow = pick("positive", MAX_SMALL),
                flaggedInWindow = pick("flagged", MAX_SMALL),
                lastGeneratedFamily = PULSE_FAMILIES.getOrNull(pick("lastFamily", PULSE_FAMILIES.size + 1)),
                lastGeneratedDateKey = EngineFacts.dateKey(-1),
                recentAnswers = answers,
            ),
        )
    }

    /**
     * A firing history built from the case number, so the cooldowns and the ninety day
     * exclusion are live rather than empty on every case.
     *
     * A composition test run entirely against an empty history would compose ten thousand
     * reports out of the same handful of highest ranked families, which is the one shape
     * that cannot surface an incompatible pair the matrix does not cover.
     */
    fun history(case: Int, catalog: ClarityCatalog): FiringHistory {
        fun pick(label: String, bound: Int) = StableHash.bucket("$label|$case", bound)
        val variants = catalog.allVariants
        if (variants.isEmpty()) return FiringHistory.EMPTY
        val used = (0 until pick("usedCount", MAX_USED_VARIANTS)).associate { index ->
            variants[pick("variant$index", variants.size)].key to
                EngineFacts.dateKey(-pick("usedOn$index", MAX_EXCLUSION_DAYS))
        }
        val stages = (0 until pick("stageCount", MAX_TRACKED_SUBJECTS)).associate { index ->
            val family = REPORT_FAMILIES[pick("stageFamily$index", REPORT_FAMILIES.size)]
            val subject = if (pick("stageSubject$index", 2) == 0) null else "area-${pick("which$index", MAX_AREAS)}"
            (family to subject) to (1 + pick("stage$index", MAX_STAGE))
        }
        val fired = stages.keys.associateWith { EngineFacts.dateKey(-pick("firedOn${it.first}", MAX_EXCLUSION_DAYS)) }
        return FiringHistory(
            variantsUsed = used,
            lastStageBySubject = stages,
            lastFiredBySubject = fired,
            lastPulseFamily = null,
        )
    }

    private val AREA_NAMES = listOf("Work", "Home", "Health", "Learning", "Personal")

    private val ITEM_TITLES = listOf(
        "Rewrite the proposal intro",
        "Book the dentist",
        "Sort the photo backlog",
        "Draft the quarterly summary",
        "Call the bank",
    )

    private val RESPONSE_LABELS = listOf("Deep work", "Stuck", "Recharging", "Busy elsewhere", "On purpose")

    private val PULSE_FAMILIES = listOf(
        "persistence", "concentration", "accumulation", "throughput", "spread",
        "quietDay", "switching", "burst", "queueDrain", "rebalance", "freshStart",
    )

    private val REPORT_FAMILIES = listOf(
        "singleFocus", "intakeVsOutput", "focusInvestment", "neglectedArea", "completionSplit",
        "selfReportVsData", "quietWeek", "queuePressure", "areaRevival", "persistentItem",
        "personalBest", "mostActiveSince", "dayShape", "timeOfDay", "switchingBehavior",
        "focusAbandonment", "queueDrained", "steadyPace", "firstMilestone", "areaBalance",
        "hardStretch",
    )

    private const val MAX_AREAS = 5
    private const val MAX_EVENTS = 30
    private const val MAX_SMALL = 6
    private const val MAX_QUEUE = 12
    private const val MAX_QUIET_DAYS = 30
    private const val MAX_ITEM_AGE = 40
    private const val MAX_LIFETIME = 200
    private const val MAX_AREA_AGE = 400
    private const val MAX_SESSIONS = 10
    private const val MAX_FOCUS_MINUTES = 600
    private const val MAX_INSTALL_DAYS = 400
    private const val MAX_USED_VARIANTS = 40
    private const val MAX_TRACKED_SUBJECTS = 12
    private const val MAX_EXCLUSION_DAYS = 120
    private const val MAX_STAGE = 4
    private const val MAX_ANSWERS = 8
    private const val WEEK_SERIES = 12
    private const val DOMINANT_WEEKS = 3

    /** A report window is always seven days. That is not a thing to vary. */
    private const val WINDOW_DAYS = 7

    /** Enough distinct report days that the variant hash is not salted the same way twice. */
    private const val DATE_SPREAD = 365
}
