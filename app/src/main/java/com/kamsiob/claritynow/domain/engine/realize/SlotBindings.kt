package com.kamsiob.claritynow.domain.engine.realize

import com.kamsiob.claritynow.domain.engine.AreaFacts
import com.kamsiob.claritynow.domain.engine.FactSet
import com.kamsiob.claritynow.domain.engine.FamilyKey
import com.kamsiob.claritynow.domain.engine.VariantKey
import com.kamsiob.claritynow.domain.engine.catalog.Purpose
import com.kamsiob.claritynow.domain.engine.catalog.SlotKey
import com.kamsiob.claritynow.domain.engine.catalog.Subject
import com.kamsiob.claritynow.domain.engine.catalog.SubjectKind
import java.time.ZoneOffset

/**
 * Which fact fills which marker, in which line. CLARITY_LOGIC_ENGINE.md 7.2.
 *
 * ## Why this is a table and not a function
 *
 * A corpus line writes `{n}` and means whatever its sentence means. Inside one family
 * `{n}` is the intake in *You added {n} things and finished {m}* and the backlog in *Your
 * queues hold {n} things now*. Nothing about the marker says which, and no check can read
 * English well enough to find out. So the mapping is authored, once, here, where it can be
 * read against the corpus line by line and where a reviewer can disagree with an entry.
 *
 * ## The rule that keeps a wrong entry from becoming a false sentence
 *
 * **A slot with no binding is not a blank. It makes the line unusable.** The realizer
 * drops any variant it cannot fill completely and takes another from the same bench, per
 * 7.2's slot completeness rule. That turns the dangerous failure into the harmless one:
 * forgetting a binding costs a line out of a bench of fifteen, while binding a slot to the
 * wrong fact prints a number that is arithmetically correct and untrue.
 *
 * Everything below follows from that asymmetry. Family level bindings are declared **only
 * for slots whose sense is the same in every line of the family**, verified by reading
 * every line of the family that uses the slot. Where one line disagrees it gets an
 * [OVERRIDES] entry. Where the sentence needs a fact 3.1 does not declare, nothing is
 * declared and the line waits, exactly as `RulesAwaitingFacts` has families waiting.
 * [EXCLUDED] records the lines that would otherwise be filled wrongly, with the fact each
 * one is waiting for.
 *
 * ## What is deliberately absent
 *
 * There is no fallback. No "if the slot is unbound, use the window count". A default here
 * would be a guess about what a sentence means, applied to every line nobody has read yet,
 * which is the mechanism by which a corpus of 1,519 authored lines starts producing claims
 * no author wrote.
 */
internal object SlotBindings {

    /** Where the entity a measure needs comes from, once a subject has been chosen. */
    enum class EntitySource {
        /** The measure reads the whole fact set. */
        NONE,

        /** The rule's own subject, an area id or an item id. */
        SUBJECT,

        /** The busiest area of the window. Null on a tie, per `RollupFacts.dominantAreaId`. */
        DOMINANT_AREA,

        /** The second busiest area, by events then by id so two devices agree. */
        SECOND_AREA,

        /** The third busiest area. */
        THIRD_AREA,

        /** The busiest area that is not the subject, for a line that sets one against another. */
        OTHER_THAN_SUBJECT,

        /** The area holding the most behind its active item. */
        LONGEST_QUEUE_AREA,

        /** An area that went from a queue to nothing inside the window. */
        DRAINED_AREA,

        /** The label from the callback the rule resolved, quoted exactly as it was stored. */
        CALLBACK_LABEL,

        /** The label the person has given most often. */
        MOST_GIVEN_LABEL,

        /** A literal, for the weekly series offsets a pattern line reads. */
        LITERAL,
    }

    /** One slot, filled from one measure, read for one entity. */
    data class Binding(val measure: String, val entity: EntitySource = EntitySource.NONE, val literal: String? = null)

    private fun bind(measure: String, entity: EntitySource = EntitySource.NONE, literal: String? = null) =
        Binding(measure, entity, literal)

    private fun subject(measure: String) = Binding(measure, EntitySource.SUBJECT)

    private fun dominant(measure: String) = Binding(measure, EntitySource.DOMINANT_AREA)

    private fun weeksAgo(measure: String, back: Int) = Binding(measure, EntitySource.LITERAL, back.toString())

    /** A family's bindings, at one stage or at every stage. */
    private data class FamilyBinding(
        val purpose: Purpose,
        val family: FamilyKey,
        val stage: Int?,
        val slots: Map<SlotKey, Binding>,
    )

    private fun family(
        purpose: Purpose,
        family: FamilyKey,
        vararg slots: Pair<SlotKey, Binding>,
    ) = FamilyBinding(purpose, family, null, slots.toMap())

    private fun stage(
        purpose: Purpose,
        family: FamilyKey,
        stage: Int,
        vararg slots: Pair<SlotKey, Binding>,
    ) = FamilyBinding(purpose, family, stage, slots.toMap())

    // ------------------------------------------------------------------ Pulse

    private val PULSE = listOf(
        // persistence, the item ladder. Every line is about the subject item and the area
        // holding it, so all three markers resolve through the item.
        family(
            Purpose.PULSE, "persistence",
            "itemTitle" to subject("itemTitle"),
            "areaName" to subject("itemAreaName"),
            "ageDays" to subject("itemAgeDays"),
        ),
        // concentration, the area's share of the window. `{m}` is the window total in
        // every line but one, which is in OVERRIDES.
        family(
            Purpose.PULSE, "concentration",
            "areaName" to subject("areaName"),
            "pct" to subject("areaShare"),
            "n" to subject("areaEvents"),
            "m" to bind("totalEvents"),
            "otherArea" to bind("areaName", EntitySource.OTHER_THAN_SUBJECT),
        ),
        // accumulation, intake against output. The family's own direction: `{n}` is what
        // arrived.
        family(
            Purpose.PULSE, "accumulation",
            "n" to bind("additions"),
            "m" to bind("completions"),
            "areaName" to dominant("areaName"),
        ),
        // throughput, the same pair in the other direction: `{n}` is what left.
        family(
            Purpose.PULSE, "throughput",
            "n" to bind("completions"),
            "m" to bind("additions"),
            "areaName" to dominant("areaName"),
        ),
        family(
            Purpose.PULSE, "spread",
            "areaCount" to bind("areasWithEvents"),
            "n" to bind("totalEvents"),
        ),
        family(
            Purpose.PULSE, "burst",
            "areaName" to subject("areaName"),
            "n" to subject("areaCompletions"),
        ),
        family(
            Purpose.PULSE, "queueDrain",
            "areaName" to subject("areaName"),
            "n" to subject("areaQueueAtStart"),
        ),
        family(
            Purpose.PULSE, "freshStart",
            "areaName" to subject("areaName"),
            "itemTitle" to subject("areaActiveItemTitle"),
        ),
        // quietDay stage 1 is written without a marker in it, which is why nothing is
        // declared here. Stages 2 and 3 count consecutive quiet days, the fact
        // RulesAwaitingFacts records as missing, and have no rule to reach them anyway.
    )

    // ------------------------------------------------------------------ Momentum

    private val MOMENTUM = listOf(
        family(
            Purpose.MOMENTUM_HEADLINE, "steadyStretch",
            "dayCount" to bind("activeDays"),
            "m" to bind("quietDays"),
            "n" to bind("completions"),
            "areaName" to dominant("areaName"),
        ),
        family(
            Purpose.MOMENTUM_HEADLINE, "quietStretch",
            "dayCount" to bind("activeDays"),
            "n" to bind("completions"),
            "areaName" to dominant("areaName"),
        ),
        // comeback names the area that returned. The length of the gap it returned from is
        // the fact RulesAwaitingFacts records as missing, so `{ageDays}` is unbound and the
        // four lines that render it wait.
        family(
            Purpose.MOMENTUM_HEADLINE, "comeback",
            "areaName" to subject("areaName"),
            "n" to subject("areaCompletions"),
        ),
        family(
            Purpose.MOMENTUM_HEADLINE, "balancedWeek",
            "areaCount" to bind("areasWithEvents"),
            "n" to bind("completions"),
            "areaName" to dominant("areaName"),
            "otherArea" to bind("areaName", EntitySource.SECOND_AREA),
        ),
        family(
            Purpose.MOMENTUM_HEADLINE, "singleAreaWeek",
            "areaName" to subject("areaName"),
            "pct" to subject("areaShare"),
            "n" to subject("areaEvents"),
            "m" to bind("totalEvents"),
            "otherArea" to bind("areaName", EntitySource.OTHER_THAN_SUBJECT),
        ),
        family(
            Purpose.MOMENTUM_HEADLINE, "strongPace",
            "n" to bind("completions"),
            "m" to bind("additions"),
            "sessions" to bind("focusSessions"),
            "sinceRef" to bind("mostRecentBetterWeekRef"),
            "areaName" to dominant("areaName"),
        ),
        family(
            Purpose.MOMENTUM_HEADLINE, "firstDays",
            "dayCount" to bind("daysSinceInstall"),
            "n" to bind("completions"),
            "areaCount" to bind("areasWithEvents"),
        ),
        family(Purpose.AREAS_BANNER, "weekBuilding", "n" to bind("completions")),
        family(Purpose.AREAS_BANNER, "weekStrong", "n" to bind("completions")),
        family(
            Purpose.AREAS_BANNER, "weekMixed",
            "areaName" to dominant("areaName"),
            "areaCount" to bind("areasWithEvents"),
            "m" to bind("areasTotal"),
        ),
    )

    // ------------------------------------------------------------------ Report headlines

    private val HEADLINES = listOf(
        family(Purpose.REPORT_HEADLINE, "singleFocus", "areaName" to dominant("areaName")),
        family(Purpose.REPORT_HEADLINE, "focusProtected", "sessions" to bind("focusSessions")),
        family(Purpose.REPORT_HEADLINE, "personalBest", "n" to bind("completions")),
        family(Purpose.REPORT_HEADLINE, "mostActiveSince", "sinceRef" to bind("mostRecentBetterWeekRef")),
        // The comeback headline names its subject area and nothing else. `{ageDays}` would
        // be the length of the gap, which no fact carries.
        family(Purpose.REPORT_HEADLINE, "comeback", "areaName" to subject("areaName")),
        family(Purpose.REPORT_HEADLINE, "datedFallback", "weekRef" to bind("weekRef")),
        family(Purpose.REPORT_HEADLINE, "queuePressure", "n" to bind("queueTotal")),
        // The clearing headline names the area that emptied, which is the one fact that
        // makes `{areaName} finished everything` true rather than merely plausible.
        family(Purpose.REPORT_HEADLINE, "clearing", "areaName" to bind("areaName", EntitySource.DRAINED_AREA)),
    )

    // ------------------------------------------------------------------ Report observations

    private val OBSERVATIONS = listOf(
        family(
            Purpose.REPORT_OBSERVATION, "singleFocus",
            "areaName" to dominant("areaName"),
            "pct" to dominant("areaShare"),
            "n" to dominant("areaEvents"),
            "m" to bind("totalEvents"),
            "sessions" to dominant("areaFocusSessions"),
        ),
        // intakeVsOutput is the one family whose markers change meaning between stages,
        // because stages 1 and 2 describe intake running ahead and stage 3 describes output
        // running ahead. Reversing `{n}` and `{m}` at stage 3 is what the corpus lines
        // themselves do: `You added {n} things and finished {m}` at stage 1 against `You
        // finished {n} things and added {m}` at stage 3.
        stage(
            Purpose.REPORT_OBSERVATION, "intakeVsOutput", 1,
            "n" to bind("additions"), "m" to bind("completions"), "k" to bind("queueGrowth"),
            "areaName" to dominant("areaName"),
        ),
        stage(
            Purpose.REPORT_OBSERVATION, "intakeVsOutput", 2,
            "n" to bind("additions"), "m" to bind("completions"), "k" to bind("queueGrowth"),
            "areaName" to dominant("areaName"),
        ),
        stage(
            Purpose.REPORT_OBSERVATION, "intakeVsOutput", 3,
            "n" to bind("completions"), "m" to bind("additions"), "k" to bind("queueShrink"),
            "areaName" to dominant("areaName"),
        ),
        family(
            Purpose.REPORT_OBSERVATION, "focusInvestment",
            "sessions" to bind("focusSessions"),
            "minutes" to bind("focusMinutes"),
            "areaName" to dominant("areaName"),
        ),
        family(
            Purpose.REPORT_OBSERVATION, "neglectedArea",
            "areaName" to subject("areaName"),
            "ageDays" to subject("areaDaysSinceLastEvent"),
            "n" to subject("areaQueue"),
            "itemTitle" to subject("areaActiveItemTitle"),
            "sinceRef" to subject("areaLastEventRef"),
            "otherArea" to dominant("areaName"),
        ),
        family(
            Purpose.REPORT_OBSERVATION, "completionSplit",
            "n" to bind("answeredInWindow"),
            "m" to bind("positiveInWindow"),
            "k" to bind("flaggedInWindow"),
            "priorLabel" to bind("mostGivenLabel"),
            "priorCount" to bind("labelCountInWindow", EntitySource.MOST_GIVEN_LABEL),
        ),
        // The flagship. It is the one rule in the catalog carrying a real callback, so
        // `{priorLabel}` is the label that callback resolved and nothing else.
        family(
            Purpose.REPORT_OBSERVATION, "selfReportVsData",
            "itemTitle" to subject("itemTitle"),
            "areaName" to subject("itemAreaName"),
            "ageDays" to subject("itemAgeDays"),
            "priorLabel" to bind("labelText", EntitySource.CALLBACK_LABEL),
        ),
        family(Purpose.REPORT_OBSERVATION, "quietWeek", "n" to bind("totalEvents")),
        family(
            Purpose.REPORT_OBSERVATION, "queuePressure",
            "n" to bind("queueTotal"),
            "m" to bind("queueTotalAtStart"),
            "areaCount" to bind("areasWithQueue"),
            "areaName" to bind("areaName", EntitySource.LONGEST_QUEUE_AREA),
        ),
        // areaRevival can name the area and say it came back. How long it had been away is
        // the missing dormancy fact, and how much of its queue went is not knowable from a
        // completion count, because the active item is not part of the queue.
        family(Purpose.REPORT_OBSERVATION, "areaRevival", "areaName" to subject("areaName")),
        family(
            Purpose.REPORT_OBSERVATION, "persistentItem",
            "itemTitle" to subject("itemTitle"),
            "areaName" to subject("itemAreaName"),
            "ageDays" to subject("itemAgeDays"),
            "n" to subject("itemQueueBehind"),
            "medianDays" to bind("medianDaysToComplete"),
        ),
        family(
            Purpose.REPORT_OBSERVATION, "personalBest",
            "n" to bind("completions"),
            "m" to bind("personalBestCompletions"),
            "sinceRef" to bind("personalBestWeekRef"),
            "areaName" to dominant("areaName"),
            "sessions" to bind("focusSessions"),
        ),
        family(
            Purpose.REPORT_OBSERVATION, "mostActiveSince",
            "n" to bind("totalEvents"),
            "sinceRef" to bind("mostRecentBetterWeekRef"),
        ),
        family(
            Purpose.REPORT_OBSERVATION, "dayShape",
            "dayName" to bind("busiestDayName"),
            "n" to bind("busiestDayCount"),
            "m" to bind("totalEvents"),
        ),
        // switchingBehavior counts swaps across the window. Six of its nine leads name the
        // area that did the swapping, which needs the per area swap count ReportRules
        // already records as missing, so `{areaName}` stays unbound here.
        family(Purpose.REPORT_OBSERVATION, "switchingBehavior", "n" to bind("swaps")),
        family(
            Purpose.REPORT_OBSERVATION, "focusAbandonment",
            "n" to bind("focusEndedEarly"),
            "m" to bind("focusStarted"),
            "sessions" to bind("focusStarted"),
        ),
        family(
            Purpose.REPORT_OBSERVATION, "queueDrained",
            "areaName" to subject("areaName"),
            "n" to subject("areaQueueAtStart"),
        ),
        family(
            Purpose.REPORT_OBSERVATION, "steadyPace",
            "n" to bind("completions"),
            "m" to bind("averageWeekCompletions"),
        ),
        family(
            Purpose.REPORT_OBSERVATION, "areaBalance",
            "areaCount" to bind("areasWithEvents"),
            "areaName" to dominant("areaName"),
            "otherArea" to bind("areaName", EntitySource.SECOND_AREA),
            "pct" to dominant("areaShare"),
            "otherPct" to bind("areaShare", EntitySource.SECOND_AREA),
            "n" to bind("totalEvents"),
        ),
        // firstMilestone and hardStretch declare nothing. hardStretch is authored without a
        // marker in it, which 6.4 all but requires: the grammatical subject is the pattern,
        // and a number would make it the person.
    )

    // ------------------------------------------------------------------ Report patterns

    private val PATTERNS = listOf(
        // The three week series lines. Oldest first: `{k}, then {m}, then {n}`.
        family(
            Purpose.REPORT_PATTERN, "shiftingFocus",
            "areaName" to weeksAgo("dominantAreaAgo", 2),
            "otherArea" to weeksAgo("dominantAreaAgo", 1),
            "thirdArea" to weeksAgo("dominantAreaAgo", 0),
        ),
        family(
            Purpose.REPORT_PATTERN, "growingQueues",
            "k" to weeksAgo("weekQueueSizeAgo", 2),
            "m" to weeksAgo("weekQueueSizeAgo", 1),
            "n" to weeksAgo("weekQueueSizeAgo", 0),
        ),
        family(
            Purpose.REPORT_PATTERN, "improvingThroughput",
            "k" to weeksAgo("weekCompletionsAgo", 2),
            "m" to weeksAgo("weekCompletionsAgo", 1),
            "n" to weeksAgo("weekCompletionsAgo", 0),
        ),
        family(
            Purpose.REPORT_PATTERN, "decliningActivity",
            "k" to weeksAgo("weekEventsAgo", 2),
            "m" to weeksAgo("weekEventsAgo", 1),
            "n" to weeksAgo("weekEventsAgo", 0),
        ),
        family(
            Purpose.REPORT_PATTERN, "areaGoneQuiet",
            "areaName" to subject("areaName"),
            "ageDays" to subject("areaDaysSinceLastEvent"),
            "n" to subject("areaQueue"),
            "sinceRef" to subject("areaLastEventRef"),
        ),
        family(Purpose.REPORT_PATTERN, "consistentRhythm", "n" to bind("activityBandWidth")),
    )

    private val FAMILIES: List<FamilyBinding> = PULSE + MOMENTUM + HEADLINES + OBSERVATIONS + PATTERNS

    private val byFamily: Map<Triple<Purpose, FamilyKey, Int?>, Map<SlotKey, Binding>> =
        FAMILIES.associate { Triple(it.purpose, it.family, it.stage) to it.slots }

    /**
     * The lines whose marker means something the family binding does not.
     *
     * Every entry here would otherwise render a number that is correct arithmetic and a
     * false claim, which 1.1 calls the one failure there is no recovering from. They are
     * excluded by key rather than by rewriting the corpus, because phase 9 owns the corpus
     * and a line removed here can be restored by an author who reads why.
     */
    val OVERRIDES: Map<VariantKey, Map<SlotKey, Binding>> = mapOf(
        // Pulse -------------------------------------------------------------
        "persistence.s3.09" to mapOf("m" to bind("medianDaysToComplete")),
        "persistence.s4.06" to mapOf("m" to subject("itemActiveWeeks")),
        "concentration.s2.13" to mapOf("m" to subject("eventsOutsideArea")),
        "accumulation.s1.12" to mapOf("n" to bind("queueGrowth")),
        "accumulation.s2.02" to mapOf("n" to bind("queueGrowth")),
        "accumulation.s3.04" to mapOf("n" to bind("queueTotal")),
        "accumulation.s3.10" to mapOf("n" to dominant("areaQueue")),
        "throughput.s1.11" to mapOf("n" to bind("queueShrink")),
        "throughput.s2.02" to mapOf("n" to bind("queueShrink")),
        "throughput.s2.07" to mapOf("n" to dominant("areaCompletions")),
        // Momentum ----------------------------------------------------------
        "mo.pace.08" to mapOf("m" to dominant("areaCompletions")),
        "mo.first.02" to mapOf("n" to bind("totalEvents")),
        "mo.first.06" to mapOf("n" to bind("totalEvents")),
        // Report observations -----------------------------------------------
        "ob.single.s1.l05" to mapOf("m" to dominant("eventsOutsideArea")),
        "ob.single.s1.e03" to mapOf("n" to dominant("areaCompletions")),
        "ob.single.s2.l02" to mapOf("m" to dominant("eventsOutsideArea")),
        "ob.single.s2.l04" to mapOf("m" to dominant("eventsOutsideArea")),
        "ob.single.s2.l12" to mapOf("m" to dominant("eventsOutsideArea")),
        "ob.single.s2.l13" to mapOf("m" to dominant("eventsOutsideArea")),
        "ob.flow.s2.l07" to mapOf("k" to dominant("areaAdditions")),
        "ob.flow.s2.l09" to mapOf("n" to bind("queueTotal")),
        "ob.flow.s2.l13" to mapOf("k" to bind("intakeGap")),
        "ob.flow.s3.l06" to mapOf("k" to dominant("areaCompletions")),
        "ob.flow.s3.l08" to mapOf("n" to bind("queueTotal")),
        "ob.focus.s1.e05" to mapOf("n" to dominant("areaCompletions")),
        "ob.focus.s2.e04" to mapOf("m" to bind("focusEndedEarly")),
        "ob.focus.s2.e06" to mapOf("n" to dominant("areaCompletions")),
        "ob.focus.s3.e02" to mapOf("n" to dominant("areaFocusSessions")),
        "ob.focus.s3.e05" to mapOf("m" to bind("lastWeekCompletions"), "n" to bind("completions")),
        "ob.best.e01" to mapOf("n" to dominant("areaCompletions")),
        "ob.best.e04" to mapOf("m" to bind("queueGrowth")),
        "ob.since.l05" to mapOf("n" to bind("completions")),
        "ob.day.l03" to mapOf("n" to bind("activeDays")),
        "ob.day.l05" to mapOf("n" to bind("quietDays")),
        "ob.aban.l05" to mapOf("n" to bind("focusSessions")),
        "ob.stead.l05" to mapOf(
            "n" to weeksAgo("weekEventsAgo", 0),
            "m" to weeksAgo("weekEventsAgo", 1),
            "k" to weeksAgo("weekEventsAgo", 2),
        ),
        "ob.bal.l06" to mapOf("n" to bind("areaSpread")),
        "ob.bal.e02" to mapOf("n" to dominant("areaCompletions"), "m" to bind("completions")),
        "ob.qp.l02" to mapOf("n" to bind("areaQueue", EntitySource.LONGEST_QUEUE_AREA)),
        "ob.qp.l06" to mapOf("n" to bind("areaQueue", EntitySource.LONGEST_QUEUE_AREA)),
    )

    /**
     * Lines a family binding could fill and must not.
     *
     * Each one uses a marker the family binds, in a sense the bound fact does not carry.
     * `ob.neg.s1.e05`, *Its last completion was {ageDays} ago*, would render the days since
     * the area's last **event**, which is a different day and a claim the person can check.
     */
    val EXCLUDED: Map<VariantKey, String> = mapOf(
        "ob.flow.s1.e03" to "`{k} of what you added is still untouched` needs how much of " +
            "the intake is still queued, and the queue delta is a net figure",
        "ob.flow.s2.e03" to "reads the intake into one area and that area's earlier queue, " +
            "and neither is what `{k}` and `{n}` are bound to here",
        "ob.flow.s3.e05" to "`{m} of the completions had been waiting more than a fortnight` " +
            "needs the age of each completion, and ItemFacts carries a median rather than a distribution",
        "ob.neg.s1.e05" to "`Its last completion was {ageDays} ago` needs the last completion, " +
            "and daysSinceLastEvent is the last event of any kind",
        "ob.split.l01" to "counts which completions the person called momentum, and the only " +
            "interpretation the app makes of an answer is whether it was the positive option",
        "ob.split.l02" to "`{m} pointed at momentum` reads the positive count as an endorsement " +
            "of one specific reading, which 6.1 does not license",
        "ob.split.l03" to "`mostly {priorLabel}` claims a majority, and the most given label " +
            "need not be a majority of the answers",
        "ob.split.l04" to "`more often than not` claims the same majority as l03",
        "ob.split.l07" to "`The week felt like {priorLabel} to you` claims a reading of a whole " +
            "week from one repeated answer",
        "ob.srvd.l01" to "`You called {itemTitle} deep work {ageDays} ago` reads the age of the " +
            "item as the age of the answer, and they are different days",
        "ob.quiet.l03" to "`Your queues hold {n} things and none of them changed` binds `{n}` to " +
            "the event count and then claims the queues held still, which the rule does not require",
        "ob.quiet.e02" to "`Last week had {n}` leaves the unit to the reader while `{n}` is bound " +
            "to this week's event count",
        "ob.qp.e01" to "`{m} of those have been waiting more than a fortnight` needs the age of " +
            "each queued item",
        "ob.rev.e01" to "`{n} of its {m} queued items went` counts completions as queue " +
            "departures, and the active item is not part of the queue",
        "ob.pers.l03" to "`has outlasted {n} other items you completed` needs the completions " +
            "since this item became active, not what is queued behind it",
        "ob.pers.e01" to "`when it was {n} days old` needs the age of the item on the day the " +
            "answer was given",
        "ob.pers.e05" to "`survived {n} focus sessions` needs the sessions since this item " +
            "became active",
        "ob.day.e01" to "`three weeks running` needs a weekday series, which 3.7 keeps in CueFacts " +
            "for layer 6",
        "ob.day.e05" to "`on the same two days` needs the days the sessions fell on",
        "ob.aban.l03" to "`averaged {minutes} minutes` needs the mean of the finished sessions, " +
            "and focusMinutesTotal is a total",
        "ob.aban.e02" to "`All of them were in {areaName}` claims every session was in one area",
        "ob.stead.e04" to "`has led every one of those weeks` needs the dominant area of four " +
            "weeks and dominantAreaLastThreeWeeks carries three",
        "ob.swi.l09" to "`{n} swaps, one area` claims the swaps were all in one area, which needs " +
            "the per area swap count",
        "ob.single.s1.e02" to "`Last week it was {otherPct}` needs an area's share in an earlier week",
        "ob.single.s2.e02" to "`The previous high was {otherPct}, in {sinceRef}` needs the same",
        "ob.bal.e04" to "`Last week one area held {otherPct}` needs the same",
        "pt.shift.10" to "`led, then handed over, twice` counts handovers across weeks",
        "pt.rva.03" to "`Three times you have said` states a count in words that no fact backs",
        "pt.rva.05" to "`in each of the last three weeks` needs the answers bucketed by week",
    )

    /** The bindings for one variant: the family's, the stage's, and the line's own. */
    fun bindingsFor(purpose: Purpose, family: FamilyKey, stage: Int, variantKey: VariantKey): Map<SlotKey, Binding> {
        if (variantKey in EXCLUDED) return emptyMap()
        val base = byFamily[Triple(purpose, family, null)].orEmpty()
        val staged = byFamily[Triple(purpose, family, stage)].orEmpty()
        return base + staged + OVERRIDES[variantKey].orEmpty()
    }

    /** True when the line was deliberately taken out of its bench. */
    fun isExcluded(variantKey: VariantKey): Boolean = variantKey in EXCLUDED

    /** Every family that declares a binding, for the coverage test. */
    val DECLARED_FAMILIES: Set<Pair<Purpose, FamilyKey>> = FAMILIES.map { it.purpose to it.family }.toSet()

    /**
     * Resolves the entity a binding names, or null when the fact set has no such thing.
     *
     * Null is the ordinary case rather than an error: a window with two equally busy areas
     * has no dominant area at all, per 3.1, and every line that would have named one is
     * dropped rather than given a winner chosen by tie break.
     */
    fun resolveEntity(
        binding: Binding,
        facts: FactSet,
        subject: Subject?,
        callbackLabel: String?,
    ): String? = when (binding.entity) {
        EntitySource.NONE -> null
        EntitySource.SUBJECT -> subject?.id
        EntitySource.DOMINANT_AREA -> facts.rollup.dominantAreaId
        EntitySource.SECOND_AREA -> ranked(facts).getOrNull(1)?.areaId
        EntitySource.THIRD_AREA -> ranked(facts).getOrNull(2)?.areaId
        EntitySource.OTHER_THAN_SUBJECT ->
            ranked(facts).firstOrNull { it.areaId != subject?.id }?.areaId
        EntitySource.LONGEST_QUEUE_AREA -> longestQueue(facts)?.areaId
        EntitySource.DRAINED_AREA ->
            facts.rollup.queueDrainedAreaIds.sorted().firstOrNull { (facts.areas[it]?.eventsInWindow ?: 0) > 0 }
        EntitySource.CALLBACK_LABEL -> callbackLabel
        EntitySource.MOST_GIVEN_LABEL ->
            (Measures.byId("mostGivenLabel")?.read(facts, null, UTC) as? MeasureValue.Text)?.value
        EntitySource.LITERAL -> binding.literal
    }

    /**
     * Busiest first, ties by id, so two devices reading one log rank them the same way.
     *
     * **Only areas with events in the window.** Validator check 1 vetoes any candidate
     * naming an area with none, and a binding that could resolve to one would produce
     * candidates that are always vetoed, which reads as an unexplained silence rather than
     * as the line being unavailable.
     */
    private fun ranked(facts: FactSet): List<AreaFacts> =
        facts.areas.values
            .filter { it.eventsInWindow > 0 }
            .sortedWith(compareByDescending<AreaFacts> { it.eventsInWindow }.thenBy { it.areaId })

    private fun longestQueue(facts: FactSet): AreaFacts? =
        facts.areas.values
            .filter { it.queueLength > 0 && it.eventsInWindow > 0 }
            .sortedWith(compareByDescending<AreaFacts> { it.queueLength }.thenBy { it.areaId })
            .firstOrNull()

    /**
     * The zone the one zone free measure is read with.
     *
     * `mostGivenLabel` counts stored labels and never touches a date, so the zone it is
     * handed cannot change its answer. It is passed a fixed one rather than the caller's so
     * that entity resolution does not have to carry a zone it has no other use for, and
     * `java.time.ZoneOffset.UTC` is a constant rather than the ambient zone the purity test
     * forbids.
     */
    private val UTC = ZoneOffset.UTC

    /** The subject kinds a binding can read, for the test that checks scope against subject. */
    fun subjectKindFor(binding: Binding): SubjectKind? = when (Measures.byId(binding.measure)?.scope) {
        MeasureScope.AREA -> SubjectKind.AREA
        MeasureScope.ITEM -> SubjectKind.ITEM
        else -> null
    }
}
