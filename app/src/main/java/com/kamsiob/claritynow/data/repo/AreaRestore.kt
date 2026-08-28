package com.kamsiob.claritynow.data.repo

import com.kamsiob.claritynow.domain.replay.ClarityState
import com.kamsiob.claritynow.domain.replay.OrderKey

/**
 * Where an area lands in the order when it comes back out of the archive, and the one
 * case where that is a question at all. `MASTER_BUILD_PROMPT.md` 5.3 and issue #15.
 *
 * **A restored area goes back exactly where it was, and the ordinary answer here is
 * therefore null: nothing has to move.** That is not a convenience, it is what the
 * whole ordering space is arranged to make true. [ClarityRepository] chooses every
 * area key against the restorable set rather than the visible one, so an archived
 * area's key is reserved for as long as it is archived and no live area can be
 * standing on it. Archiving and unarchiving an area is then a round trip that leaves
 * the list byte for byte as it was.
 *
 * **The obvious answer is the end of the list**, which is what an archive restore does
 * in almost every application that has one, and design-v3.md 15 makes that a reason to
 * look at it rather than a reason to take it. It is refused. The person most likely to
 * use this screen is the person who archived an area by accident ten seconds ago, and
 * for them the end of the list is a second thing to undo: they get their area back and
 * then have to remember where it used to sit and drag it there. Position is also part
 * of what an area is in this app, because the Areas list is ordered by hand and 10.3
 * puts the thing a person cares about most at the top. Handing it back somewhere else
 * is handing back something slightly different from what was put away.
 *
 * ## The case that is not null
 *
 * A key is only reserved against writers that respect the reservation, and this app is
 * not the only thing that can put events in this log. A backup can be merged from
 * another device, a person can hand edit an exported file and import it, and
 * `OrderKey.between` drops its jitter when the jittered key would sort past the upper
 * bound, which is the one path by which two devices can compute the same key for two
 * different areas without either of them doing anything wrong.
 *
 * If the key an archived area is holding turns out to be occupied by a **live** area,
 * restoring it as it stands would put two live areas on one key. That is silent when it
 * is made: the list still renders, because the id breaks the tie. It surfaces later as
 * an `IllegalArgumentException` out of `OrderKey.between`, the first time a drag asks
 * for a key between the two of them. It is the same defect `OrderKeyCollisionTest`
 * holds for items, which shipped in 0.2.0 and took a replay harness to find.
 *
 * So this returns a fresh key in that case, immediately above the area holding its old
 * one, which is as close to "where it was" as the space allows.
 *
 * **Two live areas sharing a key is the only collision that matters**, and that is why
 * the check looks at [ClarityState.liveAreas] rather than at every restorable area.
 * `OrderKey.between` is asked for a key between two **live** neighbors and nothing
 * else; a duplicate that is archived is only ever one of the occupied keys a bound is
 * tightened past, and a maximum does not mind being taken twice.
 */
fun restoredOrderKey(state: ClarityState, areaId: String, jitter: String): String? {
    val area = state.areas[areaId] ?: return null
    if (state.liveAreas.none { it.orderKey == area.orderKey }) return null

    val occupied = state.areas.values
        .filter { it.deletedAt == null && it.id != areaId }
        .map { it.orderKey }
    return keyTightenedBetween(
        lower = area.orderKey,
        upper = occupied.filter { it > area.orderKey }.minOrNull(),
        occupied = occupied,
        jitter = jitter,
    )
}

/**
 * A key strictly between [lower] and [upper], tightened by anything already [occupied]
 * inside that gap.
 *
 * **The rule this exists to enforce: a key must be chosen against every entity that can
 * occupy the ordering space, not against the ones currently in view.** Both of this
 * app's ordering spaces have members that a filtered list leaves out, and both produced
 * real collisions.
 *
 * For an area's items, the active item holds a key in the same space as the queue but is
 * not a member of the queue, so bounds taken from queue neighbors can enclose it, and at
 * the ends of the queue one bound is null and encloses everything. It cannot even be
 * assumed the active item sits below the whole queue: promotion from the head leaves it
 * there, but a swap promotes whichever item the person chose.
 *
 * For areas, an archived area keeps its key. Archiving is reversible, so that key is not
 * free, and unarchiving puts the area back among the live ones holding it.
 *
 * In both cases the collision is silent when it is made and surfaces much later, as an
 * exception out of `OrderKey.between`, the first time a drag asks for a key between two
 * entities that share one. The replay harness found it in August 2026; the defect
 * shipped in 0.2.0. `OrderKeyCollisionTest` holds the proof.
 *
 * **It is a top level function rather than a method on [ClarityRepository], and that is
 * this change rather than the arrangement phase 2 left.** [restoredOrderKey] above needs
 * exactly this rule and has to be reachable from a desktop JVM to be tested at all, the
 * same reason `FocusSession.kt` and `ReEntryChoice.kt` sit beside the repository rather
 * than inside it. A second copy of a tightening pass would have been the other way, and
 * a second copy of this particular rule is how the 0.2.0 defect existed in the first
 * place. The repository's own `tightenedBetween` is now one line that calls this.
 */
internal fun keyTightenedBetween(
    lower: String?,
    upper: String?,
    occupied: List<String>,
    jitter: String,
): String {
    val inside = occupied.filter {
        (lower == null || it > lower) && (upper == null || it < upper)
    }
    return OrderKey.between((listOfNotNull(lower) + inside).maxOrNull(), upper, jitter)
}
