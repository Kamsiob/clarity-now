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
 * which is the mechanism by which a corpus of nearly five thousand authored lines starts producing claims
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

        /**
         * The oldest item still active anywhere, which is `ItemFacts.longestActiveItemId`.
         *
         * For the one family that speaks about an item without having selected one. A
         * quiet week has no subject and `ob.quiet.e04` names an item all the same; of
         * every item it could mean, the one that has been sitting longest is the only
         * choice a reader would recognize, and it is the item `persistentItem` would have
         * spoken about had it fired instead.
         */
        LONGEST_ACTIVE_ITEM,

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
            // `{n}` appears in two lines of this family and means the same thing in both:
            // everything finished while this one thing did not. Nothing counts the item
            // itself, which is still active.
            "n" to subject("completionsSinceItemActive"),
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
        // queueDrain. `{n}` is the height the queue fell from, never the queue at the window
        // boundary. Every one of the fifteen statements describes the fall: `went from {n}
        // to nothing`, `It held {n} things`, `{n} things left {areaName}, and nothing
        // replaced them`. Not one dates itself, which is why this family needs no override.
        family(
            Purpose.PULSE, "queueDrain",
            "areaName" to subject("areaName"),
            "n" to subject("areaDrainedFrom"),
        ),
        family(
            Purpose.PULSE, "freshStart",
            "areaName" to subject("areaName"),
            "itemTitle" to subject("areaActiveItemTitle"),
        ),
        // switching, over the area's own swaps. `{itemTitle}` is read through the area
        // rather than through an item subject, because 6.1 gives this family the area: the
        // one line naming an item, `switching.s1.02`, names what is at the front now, and a
        // swap inside the window is what put it there.
        family(
            Purpose.PULSE, "switching",
            "areaName" to subject("areaName"),
            "itemTitle" to subject("areaActiveItemTitle"),
            "n" to subject("areaSwaps"),
        ),
        // rebalance. `{ageDays}` is the gap the area returned from and never the days since
        // it returned, which is the distinction `AreaFacts.dormantDaysBeforeReturn` exists
        // to hold. `{sinceRef}` is the month the area was last active **before** that gap,
        // which is the same two events read as a date; `areaLastEventRef` still cannot
        // serve, because it reads the last event of any kind and the return is one of them.
        family(
            Purpose.PULSE, "rebalance",
            "areaName" to subject("areaName"),
            "ageDays" to subject("areaDormancyDays"),
            "sinceRef" to subject("areaDormancyStartRef"),
        ),
        // quietDay stage 1 is written without a marker in it, which is why nothing is
        // declared for it. Stages 2 and 3 have a rule now, over
        // `HistoryFacts.currentQuietRunDays`, and still bind nothing. That is a refusal
        // rather than a gap, and it is worth stating in full because the fact those stages
        // waited for has arrived.
        //
        // `{dayCount}` is the run itself. `StreakExceptionAudit` asserts that no measure's
        // value moves when only a run moves, and the run is capped at thirty, so at the cap
        // it means at least thirty and `thirty days` would be false as well as forbidden.
        //
        // `{sinceRef}` is the day that run began, which is the same fact read as a date.
        // The audit's differential covers every measure and not only the numeric ones, so a
        // month name derived from the run is the same violation in another type.
        //
        // `{itemTitle}` appears only beside `{dayCount}` in `quietday.s2.10` and
        // `quietday.s3.08`, and this family's subject is NONE, so there is no item to reach
        // even if the other marker were fillable.
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
        // comeback names the area that returned and, since the facts phase, the gap it
        // returned from. The rule requires its subject to be in `dormantReturnedAreaIds`,
        // which is `dormantDaysBeforeReturn` at five days or more, so the gap is always
        // there to read. `mo.come.03` is in EXCLUDED because it renders the days **since**
        // the return, which is the other end of the same gap.
        family(
            Purpose.MOMENTUM_HEADLINE, "comeback",
            "areaName" to subject("areaName"),
            "ageDays" to subject("areaDormancyDays"),
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
        // The comeback headline names its subject area and the gap it came back from. Its
        // rule reads the same `dormantReturnedAreaIds` the Momentum family does, so
        // `hd.back.04`, `{areaName}, after {ageDays}`, is that gap and not the wait since.
        family(
            Purpose.REPORT_HEADLINE, "comeback",
            "areaName" to subject("areaName"),
            "ageDays" to subject("areaDormancyDays"),
        ),
        family(Purpose.REPORT_HEADLINE, "datedFallback", "weekRef" to bind("weekRef")),
        family(Purpose.REPORT_HEADLINE, "queuePressure", "n" to bind("queueTotal")),
        // The clearing headline names the area that emptied, which is the one fact that
        // makes `{areaName} finished everything` true rather than merely plausible.
        family(Purpose.REPORT_HEADLINE, "clearing", "areaName" to bind("areaName", EntitySource.DRAINED_AREA)),
    )

    // ------------------------------------------------------------------ Report observations

    private val OBSERVATIONS = listOf(
        // singleFocus. The two runner up names are ranked by events like everywhere else,
        // so both are areas that moved. `ob.single.s1.l10` is the only line that can use
        // them, because it says the others were *largely* still, which is what second and
        // third place in a week one area dominated actually is. The two lines that said an
        // other area had not moved at all are in EXCLUDED: check 1 refuses to name an area
        // with no events outside an absence family, and this is not one.
        family(
            Purpose.REPORT_OBSERVATION, "singleFocus",
            "areaName" to dominant("areaName"),
            "pct" to dominant("areaShare"),
            "n" to dominant("areaEvents"),
            "m" to bind("totalEvents"),
            "sessions" to dominant("areaFocusSessions"),
            "otherArea" to bind("areaName", EntitySource.SECOND_AREA),
            "thirdArea" to bind("areaName", EntitySource.THIRD_AREA),
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
        // focusInvestment. `{n}` is the number of **days** focus appeared on in the two
        // leads that use it bare, `Focused time appeared on {n} different days` and `You
        // protected time on {n} of the seven days`. Every other `{n}` in the family sits
        // in an extension that names what it counts and carries its own override.
        family(
            Purpose.REPORT_OBSERVATION, "focusInvestment",
            "sessions" to bind("focusSessions"),
            "minutes" to bind("focusMinutes"),
            "areaName" to dominant("areaName"),
            "n" to bind("focusDays"),
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
        // quietWeek names no subject, and one of its extensions names an item anyway. The
        // oldest thing still sitting there is the only item a reader of a quiet week would
        // take `{itemTitle}` to mean, and check 2 asks only that the id resolve in the fact
        // set, which `ItemFacts.longestActiveItemId` does by construction.
        family(
            Purpose.REPORT_OBSERVATION, "quietWeek",
            "n" to bind("totalEvents"),
            "itemTitle" to bind("itemTitle", EntitySource.LONGEST_ACTIVE_ITEM),
            "ageDays" to bind("itemAgeDays", EntitySource.LONGEST_ACTIVE_ITEM),
        ),
        family(
            Purpose.REPORT_OBSERVATION, "queuePressure",
            "n" to bind("queueTotal"),
            "m" to bind("queueTotalAtStart"),
            "areaCount" to bind("areasWithQueue"),
            "areaName" to bind("areaName", EntitySource.LONGEST_QUEUE_AREA),
        ),
        // areaRevival names the area, says it came back, and now says how long it had been
        // away. How much of its queue went is still not knowable from a completion count,
        // because the active item is not part of the queue, which is why `ob.rev.e01` is in
        // EXCLUDED and `{n}` stays unbound here.
        family(
            Purpose.REPORT_OBSERVATION, "areaRevival",
            "areaName" to subject("areaName"),
            "ageDays" to subject("areaDormancyDays"),
            "sinceRef" to subject("areaDormancyStartRef"),
            "n" to subject("areaCompletions"),
        ),
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
        // switchingBehavior counts swaps across the window. `AreaFacts.swapsInWindow` now
        // exists and `{areaName}` still stays unbound, because this rule's subject is NONE
        // and stays NONE by decision: the Pulse `switching` family is the per area reading
        // of the same behavior, and 9.1 exists to stop two families sitting on one fact at
        // two grains. There is no area here to name, so the six leads that name one stay
        // out of the bench.
        family(Purpose.REPORT_OBSERVATION, "switchingBehavior", "n" to bind("swaps")),
        family(
            Purpose.REPORT_OBSERVATION, "focusAbandonment",
            "n" to bind("focusEndedEarly"),
            "m" to bind("focusStarted"),
            "sessions" to bind("focusStarted"),
        ),
        // queueDrained, on the same reading as the Pulse family, with one line excepted in
        // OVERRIDES because it is the only sentence in either volume that names the boundary.
        family(
            Purpose.REPORT_OBSERVATION, "queueDrained",
            "areaName" to subject("areaName"),
            "n" to subject("areaDrainedFrom"),
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
        //
        // familiarDip declares one marker and no number, which its constraints all but
        // require in the same way: 14b.9 forbids it from stating the depth, the duration or
        // the date of any fall, and `Precedent` carries the verdict and nothing else, so
        // there is no count for a line to ask for. The area name is bound to the rule's own
        // subject, so the three lines that carry it fill for the area rule and drop out of
        // the bench for the two rules whose subject is the person's whole record.
        family(
            Purpose.REPORT_OBSERVATION, "familiarDip",
            "areaName" to subject("areaName"),
        ),
        // The estimate reading is a multiple and never a percentage, per 14b.8, so `{n}` is
        // a count of times rather than a share and there is deliberately no `pct` here.
        // `{m}` is the sample the ratio was read across, which is the same count the floor
        // criterion gates on, so the number in the sentence and the number that let the
        // sentence fire are one fact read twice.
        family(
            Purpose.REPORT_OBSERVATION, "estimateCalibration",
            "n" to bind("estimateMultiple"),
            "m" to bind("estimatedCompletions"),
        ),
    )

    // ------------------------------------------------------------------ Report patterns

    /**
     * How far back `since {sinceRef}` reaches in a pattern family, as a weekly offset.
     *
     * **The offset is the family's own run length minus one, and it is stated at each
     * family rather than computed.** `growingQueues` reads three buckets strictly rising,
     * so its claim starts at the oldest of the three and that is offset two;
     * `consistentRhythm` reads four and starts at offset three. The number belongs beside
     * the family because it is a fact about that family's rule, and a shared constant here
     * would be a claim that every pattern reaches the same distance, which four of them do
     * not.
     *
     * Every one of these is true rather than merely permitted: each rule requires enough
     * weeks of data that the week named is at or after the day the person installed the
     * app, so the reach never points at a week that did not happen.
     */
    private fun sinceWeeksBack(back: Int) = weeksAgo("weekRefAgo", back)

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
            "sinceRef" to sinceWeeksBack(2),
        ),
        family(
            Purpose.REPORT_PATTERN, "improvingThroughput",
            "k" to weeksAgo("weekCompletionsAgo", 2),
            "m" to weeksAgo("weekCompletionsAgo", 1),
            "n" to weeksAgo("weekCompletionsAgo", 0),
            "sinceRef" to sinceWeeksBack(2),
        ),
        family(
            Purpose.REPORT_PATTERN, "decliningActivity",
            "k" to weeksAgo("weekEventsAgo", 2),
            "m" to weeksAgo("weekEventsAgo", 1),
            "n" to weeksAgo("weekEventsAgo", 0),
            "sinceRef" to sinceWeeksBack(2),
        ),
        family(
            Purpose.REPORT_PATTERN, "areaGoneQuiet",
            "areaName" to subject("areaName"),
            "ageDays" to subject("areaDaysSinceLastEvent"),
            "n" to subject("areaQueue"),
            "sinceRef" to subject("areaLastEventRef"),
        ),
        family(
            Purpose.REPORT_PATTERN, "consistentRhythm",
            "n" to bind("activityBandWidth"),
            "sinceRef" to sinceWeeksBack(3),
        ),
        // queueEquilibrium reads four buckets inside a band and says so, so `since
        // {sinceRef}` is the oldest of the four. It was recorded as a family with nothing
        // to bind on the reading that the sentence meant the week the queues stopped
        // moving, which no fact carries. The family's own rule is what settles it: the
        // claim is about four weeks and it starts at the first of them.
        family(Purpose.REPORT_PATTERN, "queueEquilibrium", "sinceRef" to sinceWeeksBack(3)),
        // The area count series, read at both ends of the sentence the two families share:
        // `Three weeks ago you touched {n} areas. This week, {m}`. Both numbers come from
        // one series, so the comparison is between two readings of one measurement, and
        // that series counts only areas live at the window end in every bucket, which is
        // what stops a week three back being counted with an area the person can no longer
        // see. `{sinceRef}` is the week the movement began in both families and is
        // unbound, and `pt.narrow.02` is in EXCLUDED.
        family(
            Purpose.REPORT_PATTERN, "narrowingFocus",
            "n" to weeksAgo("weekAreaCountAgo", 2),
            "m" to weeksAgo("weekAreaCountAgo", 0),
            "sinceRef" to sinceWeeksBack(2),
        ),
        family(
            Purpose.REPORT_PATTERN, "broadeningFocus",
            "n" to weeksAgo("weekAreaCountAgo", 2),
            "m" to weeksAgo("weekAreaCountAgo", 0),
        ),
        // The two focus habit families read sessions **started**, which is what their rules
        // read and what `pt.fade.01` claims. `{k}, then {m}, then {n} sessions` sits in the
        // same bench as that line, so binding these three to the finished count would list
        // one series under a headline about another, and that is the exact shape of a
        // person who started five a week and finished fewer each time.
        family(
            Purpose.REPORT_PATTERN, "focusHabitForming",
            "k" to weeksAgo("weekFocusStartedAgo", 2),
            "m" to weeksAgo("weekFocusStartedAgo", 1),
            "n" to weeksAgo("weekFocusStartedAgo", 0),
            // Four rather than three: this rule requires a session in each of four weeks,
            // which is what `pt.hab.01` claims when it says every week since.
            "sinceRef" to sinceWeeksBack(3),
        ),
        family(
            Purpose.REPORT_PATTERN, "focusHabitFading",
            "k" to weeksAgo("weekFocusStartedAgo", 2),
            "m" to weeksAgo("weekFocusStartedAgo", 1),
            "n" to weeksAgo("weekFocusStartedAgo", 0),
        ),
        // `{pct} of your activity has been on weekdays for a month` is the weekend series
        // against the total series over the four buckets this family speaks about. The two
        // count the same events over the same days, so the share is a division and not an
        // estimate.
        family(
            Purpose.REPORT_PATTERN, "weekendShift",
            "pct" to bind("weekdayShareOfMonth"),
            "sinceRef" to sinceWeeksBack(3),
        ),
        // `{n} of your last {m} sessions ended before the timer` is this week's pair, and
        // the same two counts `focusAbandonment` reads for the same sentence. The family's
        // claim spans three weeks and this line does not: it names sessions, and the week
        // the report covers is the week they were started in.
        family(
            Purpose.REPORT_PATTERN, "abandonmentPattern",
            "n" to bind("focusEndedEarly"),
            "m" to bind("focusStarted"),
            // `Started sessions have outnumbered finished ones since {sinceRef}` follows
            // from the rule rather than needing one of its own: a session either finishes,
            // ends early, or is still open, so more ending early than finishing in each of
            // three weeks makes more starting than finishing in each of them.
            "sinceRef" to sinceWeeksBack(2),
        ),
        // comebackPattern names its subject area. The two other markers it carries are
        // quantities nothing holds: `{sinceRef}` is the week the first of the two silences
        // began, and `{ageDays}` in `pt.come.05` is how long a return usually lasts.
        family(Purpose.REPORT_PATTERN, "comebackPattern", "areaName" to subject("areaName")),
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
        // `It has been {n} weeks` is the family's own `{sinceRef}` read as a length. The
        // family binds `{n}` to the week's event count, which is right for `{n} moves went
        // through the app this week` and rendered `It has been 47 weeks` here.
        "ob.since.e02" to mapOf("n" to bind("weeksSinceBetterWeek")),
        "ob.day.l03" to mapOf("n" to bind("activeDays")),
        "ob.day.l05" to mapOf("n" to bind("quietDays")),
        "ob.aban.l05" to mapOf("n" to bind("focusSessions")),
        // `It held {n} things on Sunday` is the one drain line that dates its count, so it
        // reads the window boundary rather than the fall, and only when the two are the same
        // thing. The measure answers null when the fall began after Sunday, which drops this
        // lead off the bench and leaves the other four to speak.
        "ob.drain.l01" to mapOf("n" to subject("areaDrainedFromAtStart")),
        // `has outlasted {n} other items you completed` counts what was finished while
        // this item sat, which is a different quantity from what is queued behind it.
        "ob.pers.l03" to mapOf("n" to subject("completionsSinceItemActive")),
        "ob.stead.l05" to mapOf(
            "n" to weeksAgo("weekEventsAgo", 0),
            "m" to weeksAgo("weekEventsAgo", 1),
            "k" to weeksAgo("weekEventsAgo", 2),
        ),
        "ob.bal.l06" to mapOf("n" to bind("areaSpread")),
        "ob.bal.e02" to mapOf("n" to dominant("areaCompletions"), "m" to bind("completions")),
        "ob.qp.l02" to mapOf("n" to bind("areaQueue", EntitySource.LONGEST_QUEUE_AREA)),
        "ob.qp.l06" to mapOf("n" to bind("areaQueue", EntitySource.LONGEST_QUEUE_AREA)),
        // Report patterns ----------------------------------------------------
        // `What is waiting has doubled since {sinceRef}` reaches for a week the doubling
        // is true of rather than for the start of the run, so it is the one line in this
        // family whose `since` is not offset two.
        "pt.grow.08" to mapOf("sinceRef" to bind("queueDoubledSinceRef")),
    )

    /**
     * Lines a family binding could fill and must not.
     *
     * Each one uses a marker the family binds, in a sense the bound fact does not carry.
     * `ob.neg.s1.e05`, *Its last completion was {ageDays} ago*, would render the days since
     * the area's last **event**, which is a different day and a claim the person can check.
     *
     * **A second shape arrived with the facts phase.** A line whose every marker is bound
     * correctly and whose **words** state a count no rule fixes: *Three weeks of stillness*
     * under a stage that begins at fourteen days, *This is the second time* under a rule
     * that requires two or more. Nothing about the binding is wrong and the sentence is
     * still false, and the marker is what makes the line reachable, so this is where it is
     * held out. `ReportRules` names four of them where it writes the rule and points here,
     * because holding a line out of a bench belongs to the realizer and not to a criterion.
     */
    val EXCLUDED: Map<VariantKey, String> = mapOf(
        // Lines whose only quantity is a run of days. -------------------------------
        //
        // **Eighteen, and they are the largest single group in this table.** Every one of
        // them asks for `HistoryFacts.currentQuietRunDays` or `currentSingleAreaRunDays`,
        // as a count in `{dayCount}` or as the day it began in `{sinceRef}`, and 1.1's
        // streak ban was granted exactly one exception and this is not inside it.
        // `StreakExceptionAudit.NEVER_RENDERED` says why in full: the run may scope an
        // observation and may never become a number, because it is capped at thirty and a
        // value at the cap means at least thirty, so `thirty days` would be false as well
        // as forbidden, and a number a person can watch go up is the thing the ban is
        // about. The audit is a test rather than a convention, so no binding could be
        // written for these even by somebody who disagreed.
        //
        // Stage 1 of `quietDay` and stages 1 and 2 of `concentration` are authored without
        // a marker and speak normally. What is retired here is the half of two hot benches
        // that was written against a fact the app is not allowed to print, and retiring it
        // changes nothing a person would see: not one of these has ever rendered.
        "quietday.s2.01" to "{dayCount} is the run itself and {sinceRef} is the day it began, and StreakExceptionAudit.NEVER_RENDERED forbids a measure whose value moves with a run",
        "quietday.s2.02" to "{dayCount} is the run itself and {sinceRef} is the day it began, and StreakExceptionAudit.NEVER_RENDERED forbids a measure whose value moves with a run",
        "quietday.s2.04" to "{dayCount} is the run itself and {sinceRef} is the day it began, and StreakExceptionAudit.NEVER_RENDERED forbids a measure whose value moves with a run",
        "quietday.s2.05" to "{dayCount} is the run itself and {sinceRef} is the day it began, and StreakExceptionAudit.NEVER_RENDERED forbids a measure whose value moves with a run",
        "quietday.s2.08" to "{dayCount} is the run itself and {sinceRef} is the day it began, and StreakExceptionAudit.NEVER_RENDERED forbids a measure whose value moves with a run",
        "quietday.s2.10" to "{dayCount} is the run itself and {sinceRef} is the day it began, and StreakExceptionAudit.NEVER_RENDERED forbids a measure whose value moves with a run",
        "quietday.s3.01" to "{dayCount} is the run itself and {sinceRef} is the day it began, and StreakExceptionAudit.NEVER_RENDERED forbids a measure whose value moves with a run",
        "quietday.s3.02" to "{dayCount} is the run itself and {sinceRef} is the day it began, and StreakExceptionAudit.NEVER_RENDERED forbids a measure whose value moves with a run",
        "quietday.s3.03" to "{dayCount} is the run itself and {sinceRef} is the day it began, and StreakExceptionAudit.NEVER_RENDERED forbids a measure whose value moves with a run",
        "quietday.s3.06" to "{dayCount} is the run itself and {sinceRef} is the day it began, and StreakExceptionAudit.NEVER_RENDERED forbids a measure whose value moves with a run",
        "quietday.s3.08" to "{dayCount} is the run itself and {sinceRef} is the day it began, and StreakExceptionAudit.NEVER_RENDERED forbids a measure whose value moves with a run",
        "quietday.s3.09" to "{dayCount} is the run itself and {sinceRef} is the day it began, and StreakExceptionAudit.NEVER_RENDERED forbids a measure whose value moves with a run",
        "concentration.s3.02" to "{dayCount} is the run itself and {sinceRef} is the day it began, and StreakExceptionAudit.NEVER_RENDERED forbids a measure whose value moves with a run",
        "concentration.s3.03" to "{dayCount} is the run itself and {sinceRef} is the day it began, and StreakExceptionAudit.NEVER_RENDERED forbids a measure whose value moves with a run",
        "concentration.s3.04" to "{dayCount} is the run itself and {sinceRef} is the day it began, and StreakExceptionAudit.NEVER_RENDERED forbids a measure whose value moves with a run",
        "concentration.s3.06" to "{dayCount} is the run itself and {sinceRef} is the day it began, and StreakExceptionAudit.NEVER_RENDERED forbids a measure whose value moves with a run",
        "concentration.s3.07" to "{dayCount} is the run itself and {sinceRef} is the day it began, and StreakExceptionAudit.NEVER_RENDERED forbids a measure whose value moves with a run",
        "concentration.s3.10" to "{dayCount} is the run itself and {sinceRef} is the day it began, and StreakExceptionAudit.NEVER_RENDERED forbids a measure whose value moves with a run",

        // Lines that name an area with no events. -------------------------------------
        //
        // Check 1 refuses to name an area that did nothing in the window unless the rule
        // carries `absenceSubject`, and none of these three rules does. `SlotBindings.ranked`
        // enforces the same thing one layer earlier by ranking only areas with events, so
        // `{otherArea}` in a family like this can never resolve to the area the sentence is
        // about. Both halves would have to be undone to say these, and `AbsenceSubject`
        // carries the argument for why neither should be.
        "concentration.s3.11" to "`{otherArea} has not moved while {areaName} took everything` needs to name an area with no events, and this rule is not an absence rule",
        "ob.single.s1.e06" to "`{otherArea} has now been quiet for {ageDays}` needs to name an area with no events in a family whose subject is the busiest one",
        "ob.single.s2.e03" to "`{otherArea} has not moved in {ageDays}` makes the same claim as ob.single.s1.e06 and is refused by check 1 for the same reason",

        // Lines claiming a record the family's own rule does not establish. -----------
        //
        // Each of these says `the highest`, `the widest`, `the strongest`, `the previous
        // high` or `the shortest`, and every one of them sits in a family whose rule is a
        // band or a count. A measure that searched the history for a week making the claim
        // true would be a criterion written into the measure table, which is the one thing
        // `Measures` says it is not for, and it would put a superlative in front of a
        // person on a week nothing about the data called exceptional.
        "throughput.s3.03" to "`the shortest they have been in {sinceRef}` is a record claim about the queues, and a Pulse describes one day against a history bucketed by week",
        "ob.flow.s2.e01" to "`the widest gap in {sinceRef}` needs intake against output for each earlier week, and the weekly series carry completions, events and queue size",
        "ob.flow.s3.e01" to "`the strongest net week since {sinceRef}` needs the same weekly intake series as ob.flow.s2.e01, and the rule establishes no record either",
        "ob.focus.s2.e01" to "`That is your highest since {sinceRef}` names no quantity, follows leads that carry two, and sits under a rule that is a band of four to seven sessions",
        "ob.focus.s3.e01" to "`The previous high was {m} minutes, in {sinceRef}` presupposes a current high that a rule counting eight sessions does not establish, and no fact carries the week the focus minutes record fell in",

        // Lines needing a quantity the fact set does not carry. -----------------------
        "ob.flow.s1.e05" to "`Nothing added on {dayName} has moved yet` needs the day things were added, and the window carries the busiest day by events of any kind",
        "ob.focus.s1.e01" to "`All of it on {dayName}` needs the days the sessions fell on, which is what ob.day.e05 is held out for as well",
        "ob.focus.s1.e02" to "`More than last week, which had {m}` is a comparison the rule does not make, under a stage that is a band of one to three sessions",
        "ob.focus.s2.e02" to "`{n} of them were in the morning` needs focus sessions split by part of day, and eventsByPartOfDay counts events of every kind",
        "ob.qp.e03" to "`The oldest is {itemTitle}, queued {ageDays} ago` needs a queued item and its age, and the fact set carries only each area active item",
        "ob.qp.e05" to "`Nothing in {areaName} queue has moved since {sinceRef}` needs the age of the queued items, which is what ob.qp.e01 is held out for",
        "ob.rev.e03" to "`It has come back before, in {sinceRef}, and stayed for {n} weeks` needs an earlier return dated and measured, and the rule reads one window return",
        "ob.since.e01" to "`That week was mostly {areaName} too` needs the leading area of the week being named, and dominantAreaLastThreeWeeks reaches three weeks",
        "ob.since.e04" to "`The weeks between averaged {m}` needs a mean across the span to the named week, and averageWeekCompletions is a fixed eight week mean",
        "ob.tod.l02" to "`{n} of your {m} completions were before midday` needs completions split by part of day, and only events of every kind are bucketed",
        "ob.tod.l05" to "`{pct} of your activity was after 5pm` needs a share that stops at midnight, and PartOfDay.NIGHT runs from ten at night to five in the morning",
        "ob.drain.e04" to "`The last item had been queued since {sinceRef}` needs the day an item entered a queue, which nothing in ItemFacts records",
        "ob.first.l02" to "`Your first focus session, {minutes} minutes` needs one session own length, and focusMinutesTotal is the window total across every session in it",
        "ob.first.e01" to "`It took {ageDays} from when you added it` needs the age of one specific completed item, and nothing selects which of the window completions is meant",
        "ob.first.e03" to "`There were {n} more after it` counts completions after a first, and this family fires once per FirstEver flag and five of the six are not a completion",

        // The switching leads, held out by a decision recorded in ReportRules. --------
        //
        // `switchingBehavior` keeps `Subjects.NONE` on the argument that the Pulse
        // `switching` family is the per area reading of the same fact and 9.1 exists to
        // stop two families sitting on one fact at two grains. Seven of its nine leads and
        // one extension need an area, a per area swap count or a count of items rather
        // than of swaps, and the rule that would give them one is the rule that decision
        // refuses. They are recorded here rather than left unbound so that the reason
        // reads as a decision rather than as an oversight.
        "ob.swi.l02" to "`{areaName} changed its active item {n} times` needs an area subject, which ReportRules withholds from this family on the 9.1 argument",
        "ob.swi.l03" to "`{n} different items took turns at the front of {areaName}` counts items rather than swaps, and two swaps leave three items at the front",
        "ob.swi.l04" to "`{areaName} was hard to settle this week` needs an area subject, which ReportRules withholds from this family on the 9.1 argument",
        "ob.swi.l05" to "`{n} swaps across {areaCount} areas` needs how many areas the swaps fell in, and the window carries one total and each area own count",
        "ob.swi.l06" to "`Nothing stayed at the front of {areaName} for more than {ageDays}` needs the shortest time an item held the front, which nothing measures",
        "ob.swi.l07" to "`The front of {areaName} changed {n} times` needs an area subject, which ReportRules withholds from this family on the 9.1 argument",
        "ob.swi.l08" to "`{n} different items held {areaName} active slot` counts items rather than swaps and needs an area, so it fails on both halves",
        "ob.swi.e01" to "`{n} of those swaps went back and forth between the same two items` needs the items each swap moved between, and only a count of swaps is kept",

        // The callback lines that need the answer rather than its label. --------------
        //
        // `selfReportVsData` resolves a specific stored answer and `ResolvedCallback`
        // carries it with its own age in days. Only the **label** reaches the realizer:
        // `SlotBindings.resolveEntity` is handed `callbackLabel` and nothing else, so a
        // measure cannot read the day the answer was given. `ob.srvd.l08` and `ob.srvd.l10`
        // are held out for that reason and no other, and the fix is plumbing rather than a
        // fact: widening `resolveEntity` to take the resolved callback would reach both.
        // Finding the answer by its label instead would be wrong often enough to matter,
        // because the same label is given about different items on different days.
        "ob.srvd.l02" to "`you said deep work` names one of the two options while this callback resolves either, and the age of the item on the day of the answer is not carried",
        "ob.srvd.l04" to "`It has since finished {n} things` counts completions in an area since the answer, and the fact set counts them across the window",
        "ob.srvd.l07" to "`It has grown by {n} since` needs the queue growth since the answer, and queueGrowth is measured across the window",
        "ob.srvd.l08" to "`{n} days later, it is unchanged` is the age of the resolved answer, which ResolvedCallback holds and resolveEntity is not given",
        "ob.srvd.l10" to "`You said {priorLabel} on {dayName}` is the weekday of the resolved answer, which resolveEntity cannot reach for the same reason as ob.srvd.l08",

        // Report patterns. -----------------------------------------------------------
        "pt.grow.06" to "`The queues have not been shorter since {sinceRef}` reads as naming the last time they were shorter, which on a strictly rising series is always last week",
        "pt.broad.05" to "`{areaName} share has fallen each week since {sinceRef}` needs a dominant share per week, which is what pt.broad.03 is held out for",
        "pt.fade.03" to "`Protected time has been dropping since {sinceRef}` is minutes and the series behind this family is sessions, which is what pt.hab.03 is held out for",
        "pt.fade.05" to "`The last focus session was {ageDays} ago` is a gap in days and every focus fact behind this family is a weekly bucket",
        "pt.fade.06" to "`Focus was weekly in {sinceRef}` is a claim about a calendar month, and the buckets are twelve rolling seven day spans that do not align to one",
        "pt.rva.01" to "`You have answered deep work {n} times about {itemTitle}` names one response option and one item, and this family has neither a subject nor a callback",
        "pt.rva.02" to "`{n} weeks running` needs the answers bucketed by week, which is what pt.rva.05 is held out for",
        "pt.rva.07" to "`and again this week` is a claim the rule does not make, and pinning the earlier answer needs the answer rather than a label, which only a callback rule resolves",
        "pt.come.05" to "`Every time {areaName} returns, it lasts about {ageDays}` names a typical duration in days, and an area own history is twelve weekly buckets",
        // Lines with no marker at all, claiming a record their rule does not establish. -
        //
        // **The one group here that no gate could have found, and the only one already
        // reaching a screen.** Every other entry in this table is a line that never
        // rendered: a marker with no binding drops it, and both corpus gates report it.
        // These eight carry no marker or carry only names, so they filled, passed layer 5,
        // and were said. `ob.focus.s3.l02` is said thirty five times a year to one persona.
        //
        // They are the second shape this table already records, `Three weeks of stillness`
        // under a stage that begins at fourteen days, taken one step further: the words
        // claim a **record** and the family's rule counts something else entirely. A
        // criterion cannot rescue them, because the rule is right for the other five or ten
        // lines of its bench and narrowing it to the record would silence all of them, and
        // 7.3's device of a second rule is not available either: both rules would draw from
        // one bench and the looser one could pick the strict line. `ReportRules` makes that
        // argument where it declines to split `weekendShift`.
        //
        // Each of them is a corpus split rather than a missing fact, in the sense
        // `pt.fade.04` is recorded under: the shape is real and belongs to a family of its
        // own. For `ob.focus.s3.l02` the fact even exists,
        // `HistoryFacts.personalBestFocusMinutesWeek`, and what is missing is the family,
        // which would be the focus sibling of `personalBest`.
        "ob.focus.s3.l02" to "`more focused time than any week before this one` is a record and the " +
            "rule counts eight sessions, which says nothing about minutes against any earlier week",
        "persistence.s3.16" to "`the longest anything has stayed active in {areaName}` is a per area " +
            "record, and longestEverActiveDays is across the whole app rather than one area",
        "ob.pers.e02" to "`the longest anything has been active in {areaName}` makes the same per area " +
            "record claim as persistence.s3.16 and needs the same fact",
        "burst.s2.02" to "`the most {areaName} has moved in one day` is a per area daily record, and " +
            "the only per area history is twelve weekly buckets",
        "burst.s2.08" to "`{areaName} has not had a day like that before` is the same record as " +
            "burst.s2.02 in other words, and this rule counts five completions in a window",
        "ob.single.s2.e01" to "`the most concentrated week you have had` needs a dominant share per " +
            "earlier week, which is the fact pt.broad.03 is held out for",
        "ob.tod.e04" to "`Your longest focus sessions were the early ones` needs the length of each " +
            "session and the hour it fell in, and every focus fact is a count or a total",
        "concentration.s3.24" to "`Yesterday's record has one area in it` is true on the run branch of " +
            "this stage and false on the share branch, which fires at ninety five percent",
        // The lines held out before this pass. ---------------------------------------
        "switching.s2.02" to "`{areaName} has had {n} different priorities recently` counts " +
            "the items that reached the front, and two swaps leave three of them there",
        "switching.s2.04" to "`Three items have taken turns being active` states a count in " +
            "words that holds at exactly two swaps, and stage 2 is two swaps or more",
        "rebalance.s2.03" to "`Three weeks of stillness` fixes the gap at three weeks while " +
            "stage 2 begins at fourteen days, so a fortnight would read as three weeks",
        "rebalance.s2.04" to "`came back after the longest gap it has had` is a record claim, " +
            "and no fact carries this area's earlier gaps to set this one against",
        "queuedrain.s2.03" to "`That is the biggest queue you have cleared out` is a record " +
            "claim, and no fact carries the queues this area cleared before this one",
        "queuedrain.s2.05" to "`{areaName} is completely clear for the first time` is a first " +
            "ever claim, and this rule reads a fall to nothing rather than FIRST_QUEUE_DRAIN",
        "mo.come.03" to "`Things started moving again {ageDays} ago` is the days since the " +
            "return, and `{ageDays}` here is the gap before it, which is its other end",
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
        "ob.drain.l03" to "`Two areas ended the week with nothing waiting` states a count in " +
            "words, and this rule fires on one drained area as readily as on two",
        "ob.drain.l04" to "`There is an area with nothing in it for the first time` is a first " +
            "ever claim, and this rule reads a fall to nothing rather than FIRST_QUEUE_DRAIN",
        "ob.drain.e01" to "`It still has an active item` and ob.drain.e02 are the two halves of " +
            "a fact nothing chooses between here, so whichever the bench yields is a coin flip",
        "ob.drain.e02" to "`It has nothing active either` is the other half of ob.drain.e01, and " +
            "an area whose queue fell to nothing may hold an active item or may not",
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
        "pt.narrow.02" to "`{areaName}'s share has gone {k}, {m}, {pct}` reads `{m}` as one " +
            "area's share of a week, and it is bound here to how many areas moved in one",
        "pt.broad.03" to "`No area has held a majority for three weeks` needs a dominant " +
            "share per week, and the history carries each week's leader by id and not its share",
        "pt.hab.03" to "`Protected time has increased every week for three weeks` is minutes " +
            "and the series behind this family is sessions, and five short ones are less time",
        "pt.fade.04" to "`There has been no focus time in two weeks` and `pt.fade.01`, " +
            "sessions falling every week for three, cannot both be true of one series: " +
            "falling needs the last two weeks to differ and two empty weeks need them equal",
        "pt.ab.02" to "`Sessions have been getting shorter each week` is the length of a " +
            "session, and every focus fact behind this family is a count of them",
        "pt.come.01" to "`has gone quiet and returned twice` fixes the count at two while " +
            "the rule requires two or more, so a third return would print as the second",
        "pt.come.02" to "`This is the second time {areaName} has come back` makes the same " +
            "claim as pt.come.01 and is false in the same way on a third return",
        "pt.come.04" to "`has never been active two weeks in a row` is not implied by two " +
            "returns: three, four, zero, two, zero, five satisfies the rule and contradicts it",
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
        EntitySource.LONGEST_ACTIVE_ITEM -> facts.items.longestActiveItemId
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
