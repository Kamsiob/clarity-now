package com.kamsiob.claritynow.domain.engine

/**
 * One area, with the window counted and the lifetime behind it.
 * CLARITY_LOGIC_ENGINE.md 3.1.
 *
 * **An archived or tombstoned area never becomes one of these.** `FactSet.areas`
 * holds only the areas live at the window end, so a rule cannot reach an area the
 * person can no longer see and therefore cannot name one. Prohibition 3 of 1.1 is
 * enforced by the shape of the map rather than by a check somebody could forget to
 * write. Do not add a parameter that lets archived areas in.
 *
 * [nameSnapshot] and [colorHex] were resolved by folding the log to the window end,
 * never read from a live entity. An area renamed after the window still reports the
 * name it had when the events happened.
 */
data class AreaFacts(
    val areaId: AreaId,
    val nameSnapshot: String,
    val colorHex: String,
    val eventsInWindow: Int,
    val completionsInWindow: Int,
    val additionsInWindow: Int,
    /**
     * Swaps in this area in the window. `TrailQueries.swapsPerArea`.
     *
     * The `switching` family is given the area as its subject and the swap count as
     * its escalation fact, per CLARITY_LOGIC_ENGINE.md 6.1, and all eighteen of its
     * statements name an area. `WindowFacts.swaps` counts the whole window across
     * every area, so a rule reading that one would say `you changed what is active
     * in Work twice` on a week holding one swap in Work and one in Health.
     *
     * A swap is a promotion that displaced something, which is an ITEM_PROMOTED
     * carrying a non null `demotedItemId`. The facade encodes that once, so the
     * distinction between a promotion and a swap is not rediscovered here.
     */
    val swapsInWindow: Int,
    /**
     * This area's share of `WindowFacts.totalEvents`, 0.0 to 1.0.
     *
     * **0.0 when the window holds no events at all.** Never a division by zero and
     * never a NaN, because a NaN formatted into a percentage slot reaches a screen
     * as the word NaN and there is no recovering from that.
     *
     * **The most misused fact in the system**, per 3.1. One event in a one event
     * week is 100 percent concentration and reads as a claim about how somebody
     * spent their week. Every rule reading this carries a minimum [eventsInWindow]
     * floor and a catalog test enforces it.
     */
    val shareOfEvents: Double,
    val hasActiveItem: Boolean,
    val activeItemId: ItemId?,
    val activeItemTitleSnapshot: String?,
    /**
     * Whole local days since the active item was promoted, or null when there is
     * none.
     *
     * Measured from the promotion rather than from when the item was added, which
     * is what `TrailQueries.daysActiveForItem` answers and what the persistence
     * ladder in 7.3 escalates on. A promotion resets it, and 7.3 names that as the
     * behavior escalation monotonicity absorbs rather than as a defect.
     */
    val activeItemAgeDays: Int?,
    /** Items queued behind the active one at the window end. The active item is not queued. */
    val queueLength: Int,
    val queueLengthAtWindowStart: Int,
    /** [queueLength] minus [queueLengthAtWindowStart]. Positive means the queue grew. */
    val queueDelta: Int,
    /**
     * The height this area's queue fell from, in one uninterrupted fall to nothing
     * that has held to the window end. Null when no such fall happened here.
     *
     * This is the mirror of [dormantDaysBeforeReturn], and it exists for the same
     * reason. That field measures the gap an area **returned from** rather than the
     * gap since the window opened; this one measures the queue an area **drained
     * from** rather than the queue it happened to be holding when the window opened.
     * A drain that starts and finishes between two boundaries is invisible to a
     * boundary pair, and an unrelated pair of endpoints looks exactly like one.
     *
     * **The fall is read backwards from the window end, and any arrival ends it.**
     * Walking back through `TrailQueries.queueSizeSeriesByArea`, each sample that is
     * at least as large as the one after it is still part of the fall; the first
     * sample smaller than its successor is the arrival that interrupted it, and the
     * height is the sample after that one. So a queue that went 5, 4, 3, 2, 1, 0
     * reads 5, a queue that went 0, 3, 1, 4, 0 reads 4, and a queue that went 0, 5,
     * 0, 2, 0 reads 2 rather than 5, because two things arrived after the five left
     * and `{n} things left {areaName}, and nothing replaced them` would be false of
     * the larger number. The conservative reading is the correct one here: every
     * sentence this licenses claims a fall to nothing that is still nothing.
     *
     * That is exactly what all three drain families claim. `CORPUS_1_PULSE.md` 10
     * says `{areaName}'s queue went from {n} to nothing` and `{areaName} finished
     * everything it was holding`; `CORPUS_2_REPORT.md` 2.17 says `{areaName} cleared
     * its entire queue this week` and 1.13's trigger is `one or more areas fully
     * drained`. Not one of them says the queue was anything in particular at a
     * boundary, and the one line that does date itself, `ob.drain.l01`, is bound
     * through a measure that reads this only when the fall began at or before the
     * window opened.
     *
     * **Null, never zero, and the two cases are indistinguishable on purpose**,
     * exactly as in [dormantDaysBeforeReturn]. The queue is not empty at the window
     * end, or it is empty and nothing fell to get it there. Nothing drained either
     * way, and a rule that could tell them apart would be a rule that could say so.
     *
     * It carries no claim about **how** the items left. A queue also empties by
     * deletion, and `RuleBuilders.drainedByFinishing` is the guard that makes the
     * difference, because every sentence on both benches claims somebody finished
     * something.
     */
    val queueDrainedFrom: Int?,
    /**
     * Whole local days since this area's most recent event, as of the window end.
     *
     * **`Int.MAX_VALUE` when the area has never had one**, which is the value 3.1
     * specifies. It is not a sentinel a template may ever render; a rule comparing
     * against a threshold reads it correctly and a rule that would print it is a
     * rule that must carry a criterion excluding the never case.
     */
    val daysSinceLastEvent: Int,
    /**
     * How long this area had been still before it moved again inside the window, in
     * whole local days, or null when it did not come back here.
     *
     * The gap runs from the area's own previous event to its **first event inside
     * the window**, never to the window start, so a return is a real return rather
     * than an artifact of where the boundary fell. Null has two causes and a rule
     * cannot tell them apart, deliberately: the area had no event in the window, or
     * it had no event before its first one here. Nothing came back in either case.
     *
     * This is what `rebalance` escalates on, per CLARITY_LOGIC_ENGINE.md 6.1, and
     * `CORPUS_1_PULSE.md` splits it at five to thirteen days and fourteen or more.
     * [daysSinceLastEvent] cannot serve: it is zero the moment the area moves, so it
     * answers how long the area has been quiet **since** the return rather than
     * before it. `RollupFacts.dormantReturnedAreaIds` is this field with the corpus
     * floor of five days applied.
     */
    val dormantDaysBeforeReturn: Int?,
    /**
     * The local day of this area's own last event **before** the gap it returned from,
     * or null when it did not come back here.
     *
     * The other end of [dormantDaysBeforeReturn], which is measured from exactly this
     * event to the area's first event inside the window. The gap was already computed
     * from this instant and the instant was thrown away; the corpus needs it, because
     * *{areaName} had been quiet since {sinceRef}* names the month the quiet started
     * and `daysSinceLastEvent` is zero the moment the area moves again.
     *
     * Null exactly where [dormantDaysBeforeReturn] is null and for the same two
     * reasons, so a line reading one and a line reading the other are available on the
     * same windows.
     */
    val dormancyStartKey: String?,
    /**
     * Completions anywhere in the log since this area's active item became active, or 0
     * when there is no active item.
     *
     * **Everything finished while this one thing did not.** The `persistence` ladder
     * says it twice, *You have finished {n} other things since {itemTitle} became
     * active* and *You have completed {n} things elsewhere while {itemTitle} stayed
     * put*, and `persistentItem` says it once. The subject item is still active and so
     * is not among them, which is what makes `other` true without an exclusion.
     *
     * Counted across the whole log rather than across the window, because the age it is
     * set against is the item's whole age. A window count beside a lifetime age would
     * be two spans in one sentence.
     */
    val completionsSinceActiveItemStarted: Int,
    val lifetimeEvents: Int,
    val lifetimeCompletions: Int,
    /** Whole local days since AREA_CREATED, as of the window end. */
    val ageDays: Int,
    /** `ageDays < 14`, per 3.1. Kept as a field so no rule has to restate the number. */
    val isNew: Boolean,
    val focusSecondsInWindow: Long,
    val focusSessionsInWindow: Int,
    /**
     * This area's user activity events in each weekly bucket, oldest first, up to 12.
     *
     * The same seven day buckets anchored at the window end that every series in
     * `HistoryFacts` uses, so a week means one thing across the whole fact set. Read
     * `HistoryFacts` for why those buckets are not Sunday aligned calendar weeks.
     *
     * It exists for `comebackPattern`, which claims an area has gone quiet and
     * returned **twice**. `RollupFacts.dormantReturnedAreaIds` describes this window
     * only and can therefore see one return, never a second, so the family had no
     * fact and no rule. A second return is a second transition from a zero bucket to
     * a non zero one in this list.
     *
     * A rule counting those transitions must require enough buckets to be looking at
     * the area's real history rather than at its first fortnight, exactly as every
     * pattern family requires `weeksOfData >= 3`. An area created three weeks ago has
     * leading zeros here that are not silences: nothing had happened yet.
     */
    val weekEventsSeries: List<Int>,
    /**
     * Whether this area has been as quiet as it is now, for as long, before.
     * MASTER_BUILD_PROMPT 14b.9.
     *
     * Read over this area's own weekly events, back to its first week with anything
     * in it, on the definition in [Precedent]. `neglectedArea` and `areaGoneQuiet`
     * are the two families 14b.9 names that take an area as their subject, and both
     * of them are true of an area a person picks up every second month and false as a
     * claim about that person. This is the fact that tells the two apart.
     *
     * **The leading empty weeks are not silences and are not read as any.** An area
     * created three weeks ago has zeros in every bucket before it existed, exactly as
     * [weekEventsSeries] says, and counting them would give a new area a history of
     * falls it was never in. The walk starts at the area's first week with an event,
     * which is the same rule `returnsAfterSilence` uses on the series beside it.
     */
    val dipPrecedent: Precedent,
)

/**
 * The shape of the week across areas. CLARITY_LOGIC_ENGINE.md 3.1.
 *
 * Every id in every list here is an id of a live area, because every one of them was
 * derived from `FactSet.areas`, which holds no archived or tombstoned area.
 */
data class RollupFacts(
    val areasWithEvents: Int,
    /** Live areas at the window end. Archived and tombstoned areas are not counted. */
    val areasTotal: Int,
    val areasIdle: Int,
    /**
     * The area with the most events in the window.
     *
     * **Null on a tie, and null when every area is at zero.** 3.1 is explicit, and
     * the reason is that "Work carried the week" is a false sentence when Work and
     * Health both had four events. There is no tie break here on purpose: a
     * deterministic winner would still be a wrong one.
     */
    val dominantAreaId: AreaId?,
    /** The dominant area's `shareOfEvents`, or 0.0 when there is no dominant area. */
    val dominantShare: Double,
    /**
     * Areas with a real history that have gone quiet.
     *
     * `lifetimeEvents >= 5`, `daysSinceLastEvent >= 7` and not new, exactly as 3.1
     * declares. The lifetime floor is what stops an area created and touched twice
     * from being described as neglected, and the `isNew` exclusion is what stops an
     * area created eight days ago and used once from being described that way.
     */
    val neglectedAreaIds: List<AreaId>,
    /**
     * Areas whose activity resumed in this window after five or more dormant days.
     *
     * The gap is measured from the area's own previous event to its first event
     * inside the window, so a return is a real return rather than an artifact of
     * where the window boundary fell. An area with no event before the window is
     * absent from this list: nothing came back, something started, and that is
     * [freshStartAreaIds].
     */
    val dormantReturnedAreaIds: List<AreaId>,
    /**
     * Areas whose queue fell to nothing from three or more inside this window.
     *
     * [AreaFacts.queueDrainedFrom] with the corpus floor applied, in the same
     * relationship [dormantReturnedAreaIds] has to [AreaFacts.dormantDaysBeforeReturn]:
     * the field is the measurement and this is the family's threshold. The three is
     * `CORPUS_1_PULSE.md` 10's stage 1 header, `queue of three to four drained`,
     * borrowed by the Report because 1.13 and 2.17 name no figure of their own and a
     * clean sweep of one queued item is not what `A clean sweep` claims.
     *
     * **It is not a queue length at the window start compared with one at the end**,
     * which is what it was until the fall itself was declared as a fact. Those two
     * numbers are the same for a week that opened holding five and closed holding
     * nothing and for a week that built five on Tuesday and finished them on Saturday,
     * and only the second is what `cleared its entire queue this week` describes.
     */
    val queueDrainedAreaIds: List<AreaId>,
    /**
     * Areas whose queue is longer at the window end than at the window start.
     *
     * Any positive delta, with no magnitude floor. "Growing" is a direction and this
     * is the direction; the families that say something about it, `queuePressure`
     * and `accumulation`, set their own magnitude in their criteria, where the
     * escalation stage ranges in the corpus can be read against it.
     */
    val queueGrowingAreaIds: List<AreaId>,
    /**
     * Areas that began in this window, in the sense the `freshStart` family means.
     *
     * `CORPUS_1_PULSE.md` family 11 triggers on "new area, or first item in an empty
     * area", so both shapes are here: an area under fourteen days old with at least
     * one event in the window, and an area that held neither an active item nor a
     * queue at the window start and holds an active item at the end. The second
     * shape is what makes a two year old area that has just been picked up again for
     * the first time a fresh start rather than a revival, which is the reading the
     * corpus lines were written against.
     */
    val freshStartAreaIds: List<AreaId>,
)
