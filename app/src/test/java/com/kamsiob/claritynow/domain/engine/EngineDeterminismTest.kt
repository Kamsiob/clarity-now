package com.kamsiob.claritynow.domain.engine

import com.kamsiob.claritynow.domain.engine.catalog.CorpusFixture
import com.kamsiob.claritynow.domain.engine.catalog.Purpose
import com.kamsiob.claritynow.domain.engine.realize.EngineFacts
import com.kamsiob.claritynow.domain.engine.validate.ClarityValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Determinism, over ten thousand generated cases. CLARITY_LOGIC_ENGINE.md 14.
 *
 * > Identical inputs produce identical output across 10,000 generated cases.
 *
 * ## Why this needs ten thousand and not ten
 *
 * The engine is deterministic by construction: no clock, no random number, no
 * `String.hashCode`, and a total order on every sort. None of those is the risk. The risk
 * is a **map iteration order** leaking into a decision, which is invisible at small scale
 * because a `HashMap` of three keys usually iterates the same way twice and stops doing so
 * somewhere above that. `FactSet.areas` is a map, `FiringHistory` holds three maps, and a
 * bench is filtered and sorted out of a family. Ten thousand fact sets with up to five
 * areas and a firing history built two different ways is what makes that reachable.
 *
 * So each case is run through **two independently constructed engines**, and the history is
 * handed to the second one with every map rebuilt in reverse insertion order. Two devices
 * that merged the same log arrive at the same facts by different routes, and this is the
 * cheapest available imitation of that.
 *
 * ## What identical means
 *
 * The whole `Candidate`, not the rendered string. Two runs that produced the same sentence
 * from different rules, different stages or different variants would have diverged in a way
 * a reader could not see and the next day's exclusion window could.
 */
class EngineDeterminismTest {

    private val catalog = CorpusFixture.catalog

    private fun engine() = ClarityEngine(catalog, ClarityValidator(EngineFacts.ZONE), EngineFacts.ZONE)

    @Test
    fun `identical inputs produce identical output across ten thousand generated cases`() {
        val first = engine()
        val second = engine()
        var spoke = 0
        for (case in 0 until CASES) {
            val facts = generatedFacts(case)
            val history = generatedHistory(case)
            val purpose = PURPOSES[StableHash.bucket("purpose|$case", PURPOSES.size)]

            val a = first.observe(facts, history, purpose)
            val b = second.observe(facts, reversed(history), purpose)

            assertEquals("case $case, $purpose", a, b)
            if (a is EngineResult.Spoke) spoke++
        }
        // A run where nothing ever spoke would pass the assertion above and prove nothing,
        // so the count is asserted and printed. The number itself is a reading for phase 9:
        // it is how often a generated week has anything the engine can say about it.
        println("determinism: $spoke of $CASES generated cases produced a sentence")
        assertTrue("the generated corpus never produced a sentence", spoke > 0)
    }

    @Test
    fun `the same engine called twice on the same facts answers the same`() {
        val engine = engine()
        for (case in 0 until REPEAT_CASES) {
            val facts = generatedFacts(case)
            val history = generatedHistory(case)
            assertEquals(
                engine.observe(facts, history, Purpose.PULSE),
                engine.observe(facts, history, Purpose.PULSE),
            )
        }
    }

    /**
     * A fact set built entirely from a hash of the case number.
     *
     * Every value a criterion in the catalog reads is varied, and the ranges are chosen to
     * straddle the thresholds the corpus states rather than to sit inside them: an active
     * item age of 0 to 40 crosses all four `persistence` stages, a share of 0.0 to 1.0
     * crosses `concentration` and `spread`, and a window of 0 to 30 events crosses every
     * event floor. A generator that stayed inside one band would generate ten thousand
     * copies of one case.
     */
    private fun generatedFacts(case: Int): FactSet {
        fun pick(label: String, bound: Int) = StableHash.bucket("$label|$case", bound)

        val areaCount = pick("areas", MAX_AREAS + 1)
        val totalEvents = pick("events", MAX_EVENTS)
        val areas = (0 until areaCount).map { index ->
            val events = pick("areaEvents$index", MAX_EVENTS)
            EngineFacts.area(
                areaId = "area-$index",
                name = AREA_NAMES[index % AREA_NAMES.size],
                events = events,
                completions = pick("areaDone$index", events + 1),
                additions = pick("areaAdd$index", events + 1),
                share = if (totalEvents == 0) 0.0 else (events.toDouble() / totalEvents).coerceAtMost(1.0),
                activeItemId = if (pick("hasActive$index", 3) == 0) null else "item-$index",
                activeItemTitle = if (pick("hasActive$index", 3) == 0) null else ITEM_TITLES[index % ITEM_TITLES.size],
                activeItemAgeDays = if (pick("hasActive$index", 3) == 0) null else pick("age$index", MAX_ITEM_AGE),
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
        val completions = pick("completions", MAX_EVENTS)
        val weeks = (0 until WEEK_SERIES).map { pick("week$it", MAX_EVENTS) }
        return EngineFacts.factSet(
            window = EngineFacts.window(
                startDay = 0,
                endDay = 1 + pick("windowDays", MAX_WINDOW_DAYS),
                totalEvents = totalEvents,
                completions = completions,
                additions = pick("additions", MAX_EVENTS),
                promotions = pick("promotions", MAX_SMALL),
                swaps = pick("swaps", MAX_SMALL),
                deletions = pick("deletions", MAX_SMALL),
                focusStarted = pick("focusStarted", MAX_SMALL),
                focusCompleted = pick("focusCompleted", MAX_SMALL),
                focusEndedEarly = pick("focusEndedEarly", MAX_SMALL),
                focusMinutes = pick("focusMinutes", MAX_FOCUS_MINUTES),
                activeDays = pick("activeDays", MAX_WINDOW_DAYS + 1),
                busiestDayKey = if (totalEvents == 0) null else EngineFacts.dateKey(pick("busiest", MAX_WINDOW_DAYS)),
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
                dominantAreaLastThreeWeeks = (0 until DOMINANT_WEEKS).map { areas.getOrNull(pick("dom$it", MAX_AREAS))?.areaId },
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
                lastGeneratedFamily = FAMILIES.getOrNull(pick("lastFamily", FAMILIES.size + 1)),
                lastGeneratedDateKey = EngineFacts.dateKey(-1),
                recentAnswers = (0 until pick("answers", MAX_ANSWERS)).map { index ->
                    AnsweredPulse(
                        dateKey = EngineFacts.dateKey(-pick("answerOn$index", MAX_EXCLUSION_DAYS)),
                        family = FAMILIES[pick("answerFamily$index", FAMILIES.size)],
                        subjectId = if (pick("answerSubject$index", 2) == 0) {
                            null
                        } else {
                            "area-${pick("answerWhich$index", MAX_AREAS)}"
                        },
                        responseKey = "r$index",
                        responseLabel = RESPONSE_LABELS[index % RESPONSE_LABELS.size],
                        isPositive = pick("answerPositive$index", 2) == 0,
                    )
                },
            ),
        )
    }

    /**
     * A firing history built from the case number, so exclusions and cooldowns are live
     * rather than empty on every case.
     *
     * A determinism test run entirely against `FiringHistory.EMPTY` would never touch the
     * ninety day exclusion, the family cooldown or the escalation ladder, which is where
     * three of the engine's four map lookups are.
     */
    private fun generatedHistory(case: Int): FiringHistory {
        fun pick(label: String, bound: Int) = StableHash.bucket("$label|$case", bound)
        val variants = catalog.allVariants
        if (variants.isEmpty()) return FiringHistory.EMPTY
        val used = (0 until pick("usedCount", MAX_USED_VARIANTS)).associate { index ->
            variants[pick("variant$index", variants.size)].key to
                EngineFacts.dateKey(-pick("usedOn$index", MAX_EXCLUSION_DAYS))
        }
        val stages = (0 until pick("stageCount", MAX_TRACKED_SUBJECTS)).associate { index ->
            val family = FAMILIES[pick("stageFamily$index", FAMILIES.size)]
            val subject = if (pick("stageSubject$index", 2) == 0) null else "area-${pick("which$index", MAX_AREAS)}"
            (family to subject) to (1 + pick("stage$index", MAX_STAGE))
        }
        val fired = stages.keys.associateWith { EngineFacts.dateKey(-pick("firedOn${it.first}", MAX_EXCLUSION_DAYS)) }
        return FiringHistory(
            variantsUsed = used,
            lastStageBySubject = stages,
            lastFiredBySubject = fired,
            lastPulseFamily = FAMILIES.getOrNull(pick("lastPulse", FAMILIES.size + 1)),
        )
    }

    /**
     * The same history with every map rebuilt in the opposite order.
     *
     * Equal by `equals`, different by iteration. Anything that reads a map by iterating it
     * rather than by looking a key up diverges here and nowhere else.
     */
    private fun reversed(history: FiringHistory) = FiringHistory(
        variantsUsed = history.variantsUsed.entries.reversed().associate { it.key to it.value },
        lastStageBySubject = history.lastStageBySubject.entries.reversed().associate { it.key to it.value },
        lastFiredBySubject = history.lastFiredBySubject.entries.reversed().associate { it.key to it.value },
        lastPulseFamily = history.lastPulseFamily,
    )

    private companion object {

        /** Section 14 states the number. */
        const val CASES = 10_000

        /** The repeat test is the same property proved a second way and does not need ten thousand. */
        const val REPEAT_CASES = 500

        val PURPOSES = Purpose.entries.toList()

        val FAMILIES = listOf(
            "persistence", "concentration", "accumulation", "throughput", "spread",
            "quietDay", "switching", "burst", "queueDrain", "rebalance", "freshStart",
        )

        val AREA_NAMES = listOf("Work", "Home", "Health", "Learning", "Personal")

        /** Stored answer labels, quoted verbatim by a callback and compared by check 6. */
        val RESPONSE_LABELS = listOf("Deep work", "Stuck", "Recharging", "Busy elsewhere", "On purpose")

        val ITEM_TITLES = listOf(
            "Rewrite the proposal intro",
            "Book the dentist",
            "Sort the photo backlog",
            "Draft the quarterly summary",
            "Call the bank",
        )

        const val MAX_AREAS = 5
        const val MAX_EVENTS = 30
        const val MAX_SMALL = 6
        const val MAX_QUEUE = 12
        const val MAX_QUIET_DAYS = 30
        const val MAX_ITEM_AGE = 40
        const val MAX_LIFETIME = 200
        const val MAX_AREA_AGE = 400
        const val MAX_SESSIONS = 10
        const val MAX_FOCUS_MINUTES = 600
        const val MAX_WINDOW_DAYS = 7
        const val MAX_INSTALL_DAYS = 400
        const val WEEK_SERIES = 12
        const val MAX_USED_VARIANTS = 40
        const val MAX_TRACKED_SUBJECTS = 12
        const val MAX_EXCLUSION_DAYS = 120
        const val MAX_STAGE = 4
        const val MAX_ANSWERS = 8
        const val DOMINANT_WEEKS = 3
    }
}
