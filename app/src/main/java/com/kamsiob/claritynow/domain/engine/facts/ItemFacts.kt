package com.kamsiob.claritynow.domain.engine

/**
 * The item at the front of an area's queue at the window end.
 * CLARITY_LOGIC_ENGINE.md 3.1.
 *
 * Both names are snapshots resolved at the window end. [ageDays] is whole local days
 * since the promotion that made it active, which is the fact the persistence ladder
 * escalates on.
 */
data class ActiveItem(
    val itemId: ItemId,
    val titleSnapshot: String,
    val ageDays: Int,
    val areaNameSnapshot: String,
)

/**
 * One item finished inside the window. CLARITY_LOGIC_ENGINE.md 3.1.
 *
 * [daysActive] is the payload's own `activeDurationDays`, never recomputed. The
 * device that wrote the completion had already shown that figure to the person, and
 * a second device recomputing it against its own clock would contradict what they
 * were told. `TrailFacts.CompletedRecord` makes the same argument at the layer below.
 */
data class CompletedItem(
    val itemId: ItemId,
    val titleSnapshot: String,
    val areaId: AreaId,
    val areaNameSnapshot: String,
    val daysActive: Int,
)

/**
 * Items, across the window. CLARITY_LOGIC_ENGINE.md 3.1.
 *
 * **Nothing here names a tombstoned item or an item in an area the person can no
 * longer see.** A completion whose item was later deleted, or whose area was
 * archived or deleted, is absent from [completedInWindow] entirely, so validator
 * check 2 has nothing left to catch on this path. The count of such a completion
 * still exists in `WindowFacts.completions`, which is a number rather than a name,
 * and a number about something deleted is still true.
 */
data class ItemFacts(
    /** One per area with an active item, keyed by area. Areas without one are absent. */
    val activeByArea: Map<AreaId, ActiveItem>,
    /**
     * The active item that has been active longest, or null when nothing is active.
     *
     * Ties resolve to the lower item id, which is arbitrary and deterministic. It has
     * to be one of the two and neither is more true, so the only property that
     * matters is that two devices pick the same one.
     */
    val longestActiveItemId: ItemId?,
    /** [longestActiveItemId]'s age in whole local days. **0 when nothing is active.** */
    val longestActiveDays: Int,
    val completedInWindow: List<CompletedItem>,
    /**
     * The median of [completedInWindow]'s `daysActive`, or **null under three
     * completions**, per 3.1.
     *
     * The floor is what stops a single completion from being described as a typical
     * time to finish. An even count takes the mean of the two central values rounded
     * down, because the number renders as whole days and rounding down never
     * overstates how long things take.
     */
    val medianDaysToComplete: Int?,
)
