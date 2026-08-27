package com.kamsiob.claritynow.data.repo

import androidx.room.withTransaction
import com.kamsiob.claritynow.data.db.ClarityDatabase
import com.kamsiob.claritynow.data.db.WeekSnapshotRow
import com.kamsiob.claritynow.data.db.toEvent
import com.kamsiob.claritynow.data.db.toState
import com.kamsiob.claritynow.data.db.toRow
import com.kamsiob.claritynow.data.event.AreaArchived
import com.kamsiob.claritynow.data.event.AreaCreated
import com.kamsiob.claritynow.data.event.AreaDeleted
import com.kamsiob.claritynow.data.event.AreaRecolored
import com.kamsiob.claritynow.data.event.AreaRenamed
import com.kamsiob.claritynow.data.event.AreaReordered
import com.kamsiob.claritynow.data.event.AppOpened
import com.kamsiob.claritynow.data.event.AreaUnarchived
import com.kamsiob.claritynow.data.event.ClarityEvent
import com.kamsiob.claritynow.data.event.ClarityEventType
import com.kamsiob.claritynow.data.event.EventPayload
import com.kamsiob.claritynow.data.event.FocusExtended
import com.kamsiob.claritynow.data.event.ItemAdded
import com.kamsiob.claritynow.data.event.ItemCompleted
import com.kamsiob.claritynow.data.event.ItemDeleted
import com.kamsiob.claritynow.data.event.ItemEdited
import com.kamsiob.claritynow.data.event.ItemEstimated
import com.kamsiob.claritynow.data.event.ItemFiled
import com.kamsiob.claritynow.data.event.ItemPromoted
import com.kamsiob.claritynow.data.event.ItemReopened
import com.kamsiob.claritynow.data.event.ItemReordered
import com.kamsiob.claritynow.data.event.ItemStatus
import com.kamsiob.claritynow.data.prefs.AfterCompleting
import com.kamsiob.claritynow.data.prefs.ClarityPreferences
import com.kamsiob.claritynow.domain.ClarityClock
import com.kamsiob.claritynow.domain.dateKey
import com.kamsiob.claritynow.domain.daysBetween
import com.kamsiob.claritynow.domain.replay.AreaState
import com.kamsiob.claritynow.domain.replay.ClarityCheckpoint
import com.kamsiob.claritynow.domain.replay.ClarityConflict
import com.kamsiob.claritynow.domain.replay.ClarityReducer
import com.kamsiob.claritynow.domain.replay.ClarityReplay
import com.kamsiob.claritynow.domain.replay.ClarityState
import com.kamsiob.claritynow.domain.replay.FocusOutcome
import com.kamsiob.claritynow.domain.replay.ItemState
import com.kamsiob.claritynow.domain.replay.OrderKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import java.util.UUID

/**
 * The only thing in this app that writes.
 *
 * MASTER_BUILD_PROMPT 5.5 allows exactly one path: the repository builds the event,
 * assigns its lamport and originId, then inside a single transaction appends the
 * event and applies the reducer's effect to the cache. No UI, no ViewModel and no
 * engine ever writes, and there is no second way to change state.
 *
 * The authoritative projection is the in memory [ClarityState] produced by folding
 * the log. The Room cache tables mirror it so a cold start does not have to replay
 * a year of events. They hold current state and carry no history, so they cannot
 * serve the Trail at all: the Trail pages the event log itself, a wall clock window
 * at a time, through [trailPage].
 */
class ClarityRepository(
    private val db: ClarityDatabase,
    private val prefs: ClarityPreferences,
    private val clock: ClarityClock,
) {

    private val events = db.events()
    private val cache = db.cache()

    private val mutex = Mutex()
    private val _state = MutableStateFlow(ClarityState.EMPTY)
    val state: StateFlow<ClarityState> = _state.asStateFlow()

    private var loaded = false
    private lateinit var originId: String
    private lateinit var jitter: String

    /**
     * Cold start. Loads the newest checkpoint and replays only what came after it,
     * falling back to a full rebuild whenever the checkpoint cannot account for the
     * log it is sitting on.
     */
    suspend fun load() = mutex.withLock {
        if (loaded) return@withLock
        originId = prefs.originId()
        jitter = OrderKey.jitterFor(originId)

        val checkpoint = newestCheckpoint()
        val all = events.allInOrder().mapNotNull { it.toEvent() }
        val next = if (checkpoint != null && ClarityReplay.canResume(checkpoint, all)) {
            ClarityReplay.replayFrom(checkpoint, all)
        } else {
            ClarityReplay.replay(all)
        }
        _state.value = next
        writeCache(ClarityState.EMPTY, next)
        loaded = true
    }

    // Areas -------------------------------------------------------------------

    suspend fun createArea(rawName: String, colorHex: String): String? {
        val name = rawName.trim()
        if (name.isEmpty() || name.length > MAX_AREA_NAME) return null
        val current = _state.value
        val areaId = UUID.randomUUID().toString()
        // Every restorable area, not just the live ones: an archived area keeps its
        // key and unarchiving brings it back. See `tightenedBetween`.
        val orderKey = restorableAreas().lastOrNull()
            ?.let { OrderKey.last(it.orderKey, jitter) }
            ?: OrderKey.first(jitter)

        commit(AreaCreated(areaId = areaId, name = name, colorHex = colorHex, orderKey = orderKey))
        return areaId
    }

    suspend fun renameArea(areaId: String, rawName: String) {
        val name = rawName.trim()
        if (name.isEmpty() || name.length > MAX_AREA_NAME) return
        val area = _state.value.areas[areaId] ?: return
        if (area.name == name) return
        commit(AreaRenamed(areaId = areaId, previousName = area.name, newName = name))
    }

    suspend fun recolorArea(areaId: String, newHex: String) {
        val area = _state.value.areas[areaId] ?: return
        if (area.colorHex.equals(newHex, ignoreCase = true)) return
        commit(AreaRecolored(areaId = areaId, previousHex = area.colorHex, newHex = newHex))
    }

    /** Moves an area to [toIndex] within the live area list, both ends included. */
    suspend fun moveArea(areaId: String, toIndex: Int) {
        val live = _state.value.liveAreas
        val from = live.indexOfFirst { it.id == areaId }
        if (from < 0) return
        val target = toIndex.coerceIn(0, live.size - 1)
        if (from == target) return

        val without = live.filterNot { it.id == areaId }
        val newKey = tightenedBetween(
            lower = without.getOrNull(target - 1)?.orderKey,
            upper = without.getOrNull(target)?.orderKey,
            occupied = restorableAreas().filter { it.id != areaId }.map { it.orderKey },
        )
        commit(
            AreaReordered(
                areaId = areaId,
                previousOrderKey = live[from].orderKey,
                newOrderKey = newKey,
            ),
        )
    }

    suspend fun archiveArea(areaId: String) {
        val area = _state.value.areas[areaId] ?: return
        if (area.archived || area.deletedAt != null) return
        commit(AreaArchived(areaId = areaId, nameSnapshot = area.name))
    }

    suspend fun unarchiveArea(areaId: String) {
        val area = _state.value.areas[areaId] ?: return
        if (!area.archived || area.deletedAt != null) return
        commit(AreaUnarchived(areaId = areaId, nameSnapshot = area.name))
    }

    /** A tombstone, never a row removal. Trail entries keep their subject forever. */
    suspend fun deleteArea(areaId: String) {
        val area = _state.value.areas[areaId] ?: return
        if (area.deletedAt != null) return
        commit(AreaDeleted(areaId = areaId, nameSnapshot = area.name))
    }

    // Items -------------------------------------------------------------------

    /**
     * Adds an item, filed or unfiled, and promotes it in the same commit when it
     * lands in an area that is idle.
     *
     * The add sheet states where the item will land before the person commits, and
     * [wouldBecomeActive] is what it asks.
     *
     * **A null [areaId] is a capture into the inbox and is never a failure to
     * resolve an area.** Addendum 01 4a: capture must never require a decision, so
     * the thought gets out of the person's head and filing is a separate, later,
     * optional act. An unfiled add can never promote, and not only because there is
     * no area to be idle: an item with no area cannot be ACTIVE at all, which
     * `ClarityInvariants` checks and `ClarityReducer.itemPromoted` refuses.
     *
     * [areaId] and the payload's `areaNameSnapshot` are set together or not at all,
     * which is why the destination area is carried here as one nullable value
     * rather than as a nullable id and a name looked up beside it. An empty string
     * in that snapshot would be the fabricated area name DECISIONS.md C8 rejected a
     * synthetic inbox area to avoid.
     *
     * [rawFirstStep] is Addendum 01 4b and [estimateMinutes] is 4c. Both are
     * optional forever and neither is ever prompted for. The first step is bounded
     * the way the note is, which is to say not at all: [MAX_ITEM_TITLE] exists
     * because the title is the line every surface prints, and refusing a capture
     * over a long second line would lose the thought this path exists to keep. A
     * non positive estimate is dropped rather than stored, because zero minutes is
     * not an estimate and a negative one is a typo, and neither should reach
     * phase 8's calibration facts.
     */
    suspend fun addItem(
        areaId: String?,
        rawTitle: String,
        rawNote: String? = null,
        rawFirstStep: String? = null,
        estimateMinutes: Int? = null,
    ): String? {
        val title = rawTitle.trim()
        if (title.isEmpty() || title.length > MAX_ITEM_TITLE) return null
        val current = _state.value
        val destination = areaId?.let { id ->
            current.areas[id]?.takeIf { it.deletedAt == null } ?: return null
        }

        val note = rawNote?.trim()?.takeIf { it.isNotEmpty() }
        val firstStep = rawFirstStep?.trim()?.takeIf { it.isNotEmpty() }
        val estimate = estimateMinutes?.takeIf { it > 0 }
        val itemId = UUID.randomUUID().toString()
        val orderKey = tailOrderKey(
            if (destination == null) current.unfiledItems else current.liveItemsIn(destination.id),
        )

        val added = ItemAdded(
            itemId = itemId,
            areaId = destination?.id,
            title = title,
            note = note,
            orderKey = orderKey,
            areaNameSnapshot = destination?.name,
            estimateMinutes = estimate,
            firstStep = firstStep,
        )
        if (destination != null && current.activeItemIn(destination.id) == null) {
            commit(
                added,
                ItemPromoted(
                    itemId = itemId,
                    areaId = destination.id,
                    previousStatus = ItemStatus.QUEUED,
                    demotedItemId = null,
                    demotedToOrderKey = null,
                    titleSnapshot = title,
                    areaNameSnapshot = destination.name,
                ),
            )
        } else {
            commit(added)
        }
        return itemId
    }

    /**
     * The key that puts an item at the end of [existing], or the first key when
     * there is nothing there yet.
     *
     * One helper rather than the same three lines copied, because the inbox and a
     * queue have to agree about this. Both are ordered lists of items, and a person
     * moves things from one into the other, so a filing that computed its key
     * differently from an add would produce two orderings that drift apart for no
     * reason a user could see.
     */
    /**
     * The key a new item takes at the end of an area's ordering.
     *
     * [existing] must be every live item in the area, not just its queue. The active
     * item and the queued items share one ordering space, so a key chosen by looking
     * at the queue alone collides with the active item's key on the second item added
     * to a fresh area: the queue is empty at that moment and `OrderKey.first` is
     * deterministic for a given jitter. That defect shipped in 0.2.0 and was found by
     * the replay harness in August 2026. `OrderKeyCollisionTest` holds the proof.
     *
     * `maxByOrNull` rather than `lastOrNull` because a caller is not required to hand
     * this a sorted list, and being wrong here is invisible until someone reorders.
     */
    private fun tailOrderKey(existing: List<ItemState>): String =
        existing.maxByOrNull { it.orderKey }
            ?.let { OrderKey.last(it.orderKey, jitter) }
            ?: OrderKey.first(jitter)

    /**
     * A key strictly between [lower] and [upper], tightened by anything already
     * [occupied] inside that gap.
     *
     * **The rule this exists to enforce: a key must be chosen against every entity
     * that can occupy the ordering space, not against the ones currently in view.**
     * Both of this app's ordering spaces have members that a filtered list leaves
     * out, and both produced real collisions.
     *
     * For an area's items, the active item holds a key in the same space as the queue
     * but is not a member of the queue, so bounds taken from queue neighbors can
     * enclose it, and at the ends of the queue one bound is null and encloses
     * everything. It cannot even be assumed the active item sits below the whole
     * queue: promotion from the head leaves it there, but a swap promotes whichever
     * item the person chose.
     *
     * For areas, an archived area keeps its key. Archiving is reversible, so that key
     * is not free, and unarchiving puts the area back among the live ones holding it.
     *
     * In both cases the collision is silent when it is made and surfaces much later,
     * as an exception out of `OrderKey.between`, the first time a drag asks for a key
     * between two entities that share one. The replay harness found it in August 2026;
     * the defect shipped in 0.2.0. `OrderKeyCollisionTest` holds the proof.
     */
    private fun tightenedBetween(lower: String?, upper: String?, occupied: List<String>): String {
        val inside = occupied.filter {
            (lower == null || it > lower) && (upper == null || it < upper)
        }
        return OrderKey.between((listOfNotNull(lower) + inside).maxOrNull(), upper, jitter)
    }

    /** [tightenedBetween] over an area's live items, which the active item belongs to. */
    private fun keyBetween(
        areaId: String,
        lower: String?,
        upper: String?,
        excludingItemId: String,
    ): String = tightenedBetween(
        lower = lower,
        upper = upper,
        occupied = _state.value.liveItemsIn(areaId)
            .filter { it.id != excludingItemId }
            .map { it.orderKey },
    )

    /**
     * Every area whose key still occupies the ordering space, archived included,
     * ordered by key. A tombstoned area is excluded, because a tombstone never
     * comes back and its key is genuinely free.
     */
    private fun restorableAreas(): List<AreaState> =
        _state.value.areas.values
            .filter { it.deletedAt == null }
            .sortedWith(compareBy({ it.orderKey }, { it.id }))

    fun wouldBecomeActive(areaId: String): Boolean = _state.value.activeItemIn(areaId) == null

    /**
     * How many areas the default color walk has already spent. The palette itself
     * is a visual concern and lives in `ui.theme`, so the count comes from here and
     * the color is chosen there.
     */
    fun areaCountForColorWalk(): Int = _state.value.areas.values.count { it.deletedAt == null }

    /**
     * [rawFirstStep] defaults to [KEEP_FIRST_STEP] rather than to null, because null is
     * a meaningful value here: it clears the field. An editor with no first step field,
     * which is every editor before phase 3b, must pass the sentinel and not null, or
     * saving a title change would silently delete the person's first step.
     */
    suspend fun editItem(
        itemId: String,
        rawTitle: String,
        rawNote: String?,
        rawFirstStep: String? = KEEP_FIRST_STEP,
    ) {
        val title = rawTitle.trim()
        if (title.isEmpty() || title.length > MAX_ITEM_TITLE) return
        val item = _state.value.items[itemId]?.takeIf { it.deletedAt == null } ?: return
        val note = rawNote?.trim()?.takeIf { it.isNotEmpty() }
        val firstStep = if (rawFirstStep === KEEP_FIRST_STEP) {
            item.firstStep
        } else {
            rawFirstStep?.trim()?.takeIf { it.isNotEmpty() }
        }
        if (item.title == title && item.note == note && item.firstStep == firstStep) return
        commit(
            ItemEdited(
                itemId = itemId,
                previousTitle = item.title,
                newTitle = title,
                previousNote = item.note,
                newNote = note,
                previousFirstStep = item.firstStep,
                newFirstStep = firstStep,
            ),
        )
    }

    /**
     * Sets, changes or clears an item's estimate. Addendum 01 4c, 14b.3.
     *
     * A separate event rather than a field on `ITEM_EDITED`, so that revising a
     * guess never rewrites what the person first wrote down. Nothing is written
     * when the value is unchanged, the same early return [renameArea] makes and for
     * the same reason: an event that changes nothing still lands in the Trail, and
     * a row saying an estimate was revised to the number it already was is a lie
     * about what happened.
     *
     * A non positive number clears the estimate rather than storing one, matching
     * [addItem], so a person who empties the field and a person who types zero get
     * the same answer.
     *
     * An unfiled item can be estimated. Addendum 01 4a lets an inbox item be
     * edited, and an estimate carries no area, so nothing about this is area
     * scoped. What may ever be said about these numbers is 14b.8, and it is a
     * correctness rule rather than a matter of tone: a ratio or a tendency, never a
     * delta against the actual.
     */
    suspend fun estimateItem(itemId: String, newEstimateMinutes: Int?) {
        val item = _state.value.items[itemId]?.takeIf { it.deletedAt == null } ?: return
        val next = newEstimateMinutes?.takeIf { it > 0 }
        if (item.estimateMinutes == next) return
        commit(
            ItemEstimated(
                itemId = itemId,
                previousEstimateMinutes = item.estimateMinutes,
                newEstimateMinutes = next,
            ),
        )
    }

    /**
     * Files an unfiled item into an area. The only transition into one.
     *
     * The order key is the tail of that area's live items, computed by the same
     * [tailOrderKey] an add uses, so an item filed from the inbox sits where an
     * item added straight into the area would have.
     *
     * **This does not promote, and MASTER_BUILD_PROMPT 14b.1 says it should.** That
     * section has filing into an area with no active item write `ITEM_FILED` then
     * `ITEM_PROMOTED` in one transaction, matching what [addItem] does on an add.
     * The build instruction for this commit says the opposite and gives its reason:
     * filing is a separate, later, optional act, and promotion is the person's
     * move. Both cannot be true, so this takes the narrower reading and the
     * disagreement is recorded here rather than settled quietly. Reversing it is
     * one more payload in the commit below: it changes no schema and invalidates no
     * log, because a promotion that did not happen is not a fact anything replays.
     *
     * The two readings agree about the case that could actually cost someone
     * something. Filing never displaces the item a person is already working on:
     * 14b.1's promotion fires only into an idle area, and `ClarityReducer.itemFiled`
     * refuses to move a status in either direction. Writing something down can
     * never become the most disruptive act in the app.
     *
     * Refused for an item that already has an area, because there is no unfile and
     * no move between areas, and refused for an area that is unknown, deleted or
     * archived, which the reducer refuses too. A refusal leaves the item in the
     * inbox, where it is visible and can be filed again, rather than somewhere the
     * person cannot see it.
     */
    suspend fun fileItem(itemId: String, areaId: String) {
        val current = _state.value
        val item = current.items[itemId]?.takeIf { it.deletedAt == null } ?: return
        if (item.areaId != null) return
        val area = current.areas[areaId]?.takeIf { it.deletedAt == null && !it.archived } ?: return
        commit(
            ItemFiled(
                itemId = itemId,
                areaId = areaId,
                orderKey = tailOrderKey(current.liveItemsIn(areaId)),
                areaNameSnapshot = area.name,
            ),
        )
    }

    /**
     * Completes the active item, then does whatever the After completing setting
     * says. Only an active item can be completed, and that rule is the one doing
     * the philosophical work, so it is enforced here as well as in the reducer.
     *
     * The unfiled check below is unreachable through the ACTIVE check above it, and
     * is written anyway. An item with no area cannot be ACTIVE, by invariant and by
     * the reducer, so the two guards cover the same ground twice. The alternative
     * is a non null assertion sitting on the philosophical rule of the app, and the
     * failure it would produce is a completion recorded against no area at all.
     */
    suspend fun completeItem(itemId: String): CompletionOutcome {
        val current = _state.value
        val item = current.items[itemId]?.takeIf { it.deletedAt == null }
            ?: return CompletionOutcome.NotAllowed
        if (item.status != ItemStatus.ACTIVE) return CompletionOutcome.NotAllowed
        val areaId = item.areaId ?: return CompletionOutcome.NotAllowed
        val area = current.areas[areaId] ?: return CompletionOutcome.NotAllowed

        val completed = ItemCompleted(
            itemId = itemId,
            areaId = areaId,
            titleSnapshot = item.title,
            areaNameSnapshot = area.name,
            activeDurationDays = activeDurationDays(item),
        )

        val queue = current.queueIn(areaId)
        val head = queue.firstOrNull()
        if (head == null) {
            commit(completed)
            return CompletionOutcome.AreaIdle
        }

        return when (prefs.currentAfterCompleting()) {
            AfterCompleting.AUTO_PROMOTE -> {
                commit(
                    completed,
                    ItemPromoted(
                        itemId = head.id,
                        areaId = areaId,
                        previousStatus = ItemStatus.QUEUED,
                        demotedItemId = null,
                        demotedToOrderKey = null,
                        titleSnapshot = head.title,
                        areaNameSnapshot = area.name,
                    ),
                )
                CompletionOutcome.Promoted(head.id, head.title)
            }

            AfterCompleting.CHOOSE_FROM_QUEUE -> {
                commit(completed)
                CompletionOutcome.ChooseFromQueue(areaId)
            }
        }
    }

    /**
     * Swap. The active item goes to the head of the queue and [itemId] takes its
     * place, as one event carrying the demoted id, which is what makes it replay
     * correctly on another device. Swaps are ordinary behavior and carry no warning
     * tone anywhere.
     *
     * **The unfiled guard is load bearing rather than defensive.** This is the only
     * path in the repository that promotes anything, an unfiled item is QUEUED like
     * any other, and Addendum 01 4a is that an item with no area cannot be active.
     * Without the guard, a call naming an inbox item would write an `ITEM_PROMOTED`
     * that the reducer then refuses, which leaves a real event in the log that
     * changes nothing, a diagnostic nobody reads, and a Trail row for a promotion
     * that did not happen. Refusing before the write is the difference between a
     * request that was not honored and a log that says something false.
     */
    suspend fun swapToItem(itemId: String) {
        val current = _state.value
        val item = current.items[itemId]?.takeIf { it.deletedAt == null } ?: return
        if (item.status != ItemStatus.QUEUED) return
        val areaId = item.areaId ?: return
        val area = current.areas[areaId] ?: return
        val sitting = current.activeItemIn(areaId)

        val demotedKey = sitting?.let {
            val queue = current.queueIn(areaId).filterNot { queued -> queued.id == itemId }
            OrderKey.before(queue.firstOrNull()?.orderKey ?: item.orderKey, jitter)
        }
        commit(
            ItemPromoted(
                itemId = itemId,
                areaId = areaId,
                previousStatus = ItemStatus.QUEUED,
                demotedItemId = sitting?.id,
                demotedToOrderKey = demotedKey,
                titleSnapshot = item.title,
                areaNameSnapshot = area.name,
            ),
        )
    }

    /** Promotes the queue head into an idle area. */
    suspend fun promoteHead(areaId: String) {
        val current = _state.value
        if (current.activeItemIn(areaId) != null) return
        val head = current.queueIn(areaId).firstOrNull() ?: return
        swapToItem(head.id)
    }

    /** A COMPLETED item is always filed, so the area check here is the type's, not a rule. */
    suspend fun reopenItem(itemId: String) {
        val current = _state.value
        val item = current.items[itemId]?.takeIf { it.deletedAt == null } ?: return
        if (item.status != ItemStatus.COMPLETED) return
        val areaId = item.areaId ?: return
        // A reopened item goes to the head of the queue, and both bounds have to be
        // tightened against the active item, which shares this ordering space and is
        // not in the queue. With a queue, `before(head)` alone can land on the active
        // item's key; with no queue, `first` can. One call covers both.
        val targetKey = keyBetween(
            areaId = areaId,
            lower = null,
            upper = current.queueIn(areaId).firstOrNull()?.orderKey,
            excludingItemId = itemId,
        )
        commit(ItemReopened(itemId = itemId, areaId = areaId, targetOrderKey = targetKey))
    }

    /**
     * Moves a queued item to [toIndex] within its area's queue.
     *
     * **An unfiled item cannot be reordered, and that is a gap rather than a rule.**
     * `ITEM_REORDERED` carries a non null `areaId`, per MASTER_BUILD_PROMPT 5.2, so
     * a move inside the inbox cannot be expressed in the log at all. Nothing about
     * the inbox wants that: `ClarityState.unfiledItems` is ordered by `orderKey`
     * exactly like a queue, and design-v3 10.16 gives the inbox sheet an order.
     * Closing it means widening that payload, which is nearly free while the schema
     * window is open and expensive afterward, and it is recorded here because the
     * early return below is otherwise indistinguishable from a decision.
     */
    suspend fun moveItem(itemId: String, toIndex: Int) {
        val current = _state.value
        val item = current.items[itemId] ?: return
        val areaId = item.areaId ?: return
        val queue = current.queueIn(areaId)
        val from = queue.indexOfFirst { it.id == itemId }
        if (from < 0) return
        val target = toIndex.coerceIn(0, queue.size - 1)
        if (from == target) return

        val without = queue.filterNot { it.id == itemId }
        val newKey = keyBetween(
            areaId = areaId,
            lower = without.getOrNull(target - 1)?.orderKey,
            upper = without.getOrNull(target)?.orderKey,
            excludingItemId = itemId,
        )
        commit(
            ItemReordered(
                itemId = itemId,
                areaId = areaId,
                previousOrderKey = item.orderKey,
                newOrderKey = newKey,
            ),
        )
    }

    suspend fun moveItemToFront(itemId: String) = moveItem(itemId, 0)

    /**
     * A tombstone. The undo window lives in the UI and expires before this is
     * called, so there is no event to compensate for and nothing to explain later.
     *
     * **An unfiled item cannot be deleted, and that is a gap that has to be closed
     * before the inbox ships.** MASTER_BUILD_PROMPT 14b.1 is explicit that an
     * inbox item can be deleted, with the same five second undo as anywhere else,
     * and Addendum 01 4a says the same. `ITEM_DELETED` carries a non null `areaId`,
     * per 5.2's catalog, so the event cannot be built for an item that has no area,
     * and the only honest thing this method can do meanwhile is refuse.
     *
     * The consequence, stated plainly because a silent early return does not state
     * it: until that payload is widened, a person can put something in the inbox
     * and has no way to take it back out except by filing it first. That is the
     * one operation an inbox must always support, so this is not a defect a later
     * phase can absorb, and the fix is a schema change that is nearly free now.
     */
    suspend fun deleteItem(itemId: String) {
        val current = _state.value
        val item = current.items[itemId]?.takeIf { it.deletedAt == null } ?: return
        val areaId = item.areaId ?: return
        commit(
            ItemDeleted(
                itemId = itemId,
                areaId = areaId,
                titleSnapshot = item.title,
            ),
        )
    }

    // Focus -------------------------------------------------------------------

    /**
     * Adds time to a running session without ending it. Addendum 01 4f, 14b.5.
     *
     * A session has exactly one `FOCUS_STARTED` and at most one terminal event, and
     * an extension is neither, so nothing here restarts a timer or opens a second
     * session. Ending a timer must never be the price of needing ten more minutes.
     * Repeatable and uncapped, per 14b.5.
     *
     * **This writes the event and nothing else, and phase 4 owes it one more
     * thing.** 14b.5 requires the persisted end timestamp that lets a session
     * survive process death to be recomputed on every extension. That timestamp is
     * a running timer's business rather than the log's, so it belongs to the focus
     * service when there is one, and an extension that moved the planned total
     * without moving it would give back the added minutes at the next cold start.
     *
     * The payload carries the absolute new figure as well as the delta, and this
     * computes it from the session's current `plannedSeconds` in the projection
     * rather than from what the screen last drew. Two extensions in the same minute
     * then agree, and a replay cannot arrive at a different number from the one the
     * person was shown, which is what applying a delta at fold time would risk.
     *
     * Refused for a session that has already ended, matching the reducer, and for a
     * non positive [addedSeconds], which is not an extension.
     */
    suspend fun extendFocus(sessionId: String, addedSeconds: Int) {
        if (addedSeconds <= 0) return
        val session = _state.value.focusSessions[sessionId] ?: return
        if (session.outcome != FocusOutcome.RUNNING) return
        commit(
            FocusExtended(
                sessionId = sessionId,
                addedSeconds = addedSeconds,
                newPlannedSeconds = session.plannedSeconds + addedSeconds,
            ),
        )
    }

    // Presence ----------------------------------------------------------------

    /**
     * Records that the app was opened today, at most once per local calendar day.
     * Addendum 01 2d and 4d, MASTER_BUILD_PROMPT 14b.4.
     *
     * A date key and nothing else. No time, no count, no duration. It exists so a
     * long absence can be noticed without any tracking, and it is excluded from
     * `ClarityEventType.isUserActivity`, renders no Trail row and is absent from the
     * Trail day header count. DECISIONS.md C7 works through what each of those
     * would cost if it were forgotten.
     *
     * **The once a day guard reads the log, and may not read anything else.**
     * CLAUDE.md rule 6: no engine state in DataStore. A last opened date is engine
     * state, because gap detection derives from it, and DataStore does not merge, so
     * two devices holding one log would disagree about a person's absence. The log
     * is the only place the answer can live, and reading it is one seek of the
     * `entityId` index: [AppOpened] uses its date key as its entity id, exactly as
     * `SettingChanged` uses its key.
     *
     * The check and the append are taken under one lock, through [commitLocked],
     * rather than checking and then calling [commit]. Two callers racing at launch
     * would otherwise both read an empty answer and both write. A duplicate is not
     * harmful, since nothing counts these, but "at most once per calendar day" is
     * the contract this event was specified with and a second row would be a small
     * lie in the file the desktop app is built against. Two devices can still each
     * write one for the same day after a merge, which is correct: each of them was
     * opened, and every reader of this event asks which days appear, never how many
     * rows a day has.
     *
     * **The trap for whoever builds the re-entry surface.** The app shell calls this
     * on the first foreground, so by the time any screen can ask how long the gap
     * was, today's row is already in the log and the newest date key is today. The
     * gap is measured to the newest `APP_OPENED` strictly before today, and a query
     * that forgets that reports every return as a gap of zero days.
     */
    suspend fun recordAppOpened() {
        val today = clock.dateKey()
        mutex.withLock {
            val alreadyToday = events.forEntity(today)
                .any { it.type == ClarityEventType.APP_OPENED.name }
            if (alreadyToday) return@withLock
            commitLocked(AppOpened(dateKey = today))
        }
    }

    // Conflicts ---------------------------------------------------------------

    /**
     * The conflicts still worth explaining. Dismissal is a per device act rather
     * than a fact about the log, so it lives on the cache row and this reads from
     * there rather than from the replayed state.
     */
    val openConflicts: Flow<List<ClarityConflict>> =
        cache.observeOpenConflicts().map { rows -> rows.map { it.toState() } }

    suspend fun dismissConflict(conflictId: String) {
        cache.dismissConflict(conflictId, clock.nowMillis())
    }

    // Maintenance -------------------------------------------------------------

    /**
     * Drops every derived row and rebuilds it from the log. The proof that the
     * cache is a cache. Exposed in the debug menu and used by the export path as a
     * correctness check.
     */
    suspend fun rebuildCacheFromLog(): ClarityState = mutex.withLock {
        val all = events.allInOrder().mapNotNull { it.toEvent() }
        val rebuilt = ClarityReplay.replay(all)
        db.withTransaction {
            cache.clearCache()
            cache.clearSnapshots()
            writeCache(ClarityState.EMPTY, rebuilt)
        }
        _state.value = rebuilt
        rebuilt
    }

    /** MASTER_BUILD_PROMPT 14.2. The log goes, the cache goes, the checkpoints go. */
    suspend fun eraseEverything() = mutex.withLock {
        db.withTransaction {
            cache.clearCache()
            cache.clearSnapshots()
            events.eraseEverything()
        }
        prefs.eraseEverything()
        originId = prefs.originId()
        jitter = OrderKey.jitterFor(originId)
        _state.value = ClarityState.EMPTY
    }

    /**
     * Every event, oldest first. The export path starts here, and nothing else does.
     *
     * This is a `SELECT *` over the whole table, which is correct for an export and
     * wrong for anything a person waits on. The Trail in particular does not start
     * here: it pages the log by wall clock window, through [trailPage].
     */
    suspend fun allEvents(): List<ClarityEvent> = events.allInOrder().mapNotNull { it.toEvent() }

    // The Trail path ----------------------------------------------------------
    //
    // MASTER_BUILD_PROMPT section 4: ViewModels never touch DAOs, only
    // repositories. These six methods are therefore the whole of the Trail's
    // access to the log, and every one of them is bounded: a half open wall clock
    // window, a single anchor row, or an explicit list of ids taken from a page
    // that has already been loaded.

    /**
     * One page of the Trail, newest first. MASTER_BUILD_PROMPT 9.
     *
     * Half open, like every other bound in this app: `[fromMillis, toMillis)`.
     */
    suspend fun trailPage(fromMillis: Long, toMillis: Long): List<ClarityEvent> =
        events.betweenWallClock(fromMillis, toMillis).mapNotNull { it.toEvent() }

    /**
     * The anchor for the first page: the wall clock of the newest event there is.
     *
     * Null means the log is empty, which is the Trail's empty state and needs no
     * further query.
     */
    suspend fun newestEventAt(): Long? = events.newestWallClock()

    /**
     * The anchor for the next page. Null means there is nothing older than
     * [beforeMillis] and pagination is finished.
     *
     * Asking the log where the next event actually is, rather than stepping back a
     * fortnight at a time, is what keeps a long quiet stretch of history cheap.
     */
    suspend fun newestEventBefore(beforeMillis: Long): Long? =
        events.newestWallClockBefore(beforeMillis)

    // The three id keyed lookups below chunk their arguments at [MAX_BOUND_IDS]
    // before they reach the DAO. A fourteen day page can legitimately name more
    // distinct items than SQLite will bind in one statement, and the failure is a
    // runtime exception on a busy log rather than anything a small test would see.

    /** Naming and coloring history for exactly the areas one page mentions. */
    suspend fun areaHistoryFor(areaIds: List<String>): List<ClarityEvent> =
        areaIds.chunked(MAX_BOUND_IDS)
            .flatMap { events.areaHistory(it) }
            .mapNotNull { it.toEvent() }

    /** Title history and area binding for exactly the items one page mentions. */
    suspend fun itemHistoryFor(itemIds: List<String>): List<ClarityEvent> =
        itemIds.chunked(MAX_BOUND_IDS)
            .flatMap { events.itemHistory(it) }
            .mapNotNull { it.toEvent() }

    /** The `FOCUS_STARTED` rows for exactly the sessions one page mentions. */
    suspend fun focusOriginsFor(sessionIds: List<String>): List<ClarityEvent> =
        sessionIds.chunked(MAX_BOUND_IDS)
            .flatMap { events.focusOrigins(it) }
            .mapNotNull { it.toEvent() }

    // The write path ----------------------------------------------------------

    /**
     * The single write. Everything above funnels through here.
     *
     * All payloads passed in one call belong to one user action and get consecutive
     * lamport values, so a completion and the promotion it caused can never be
     * separated by an event from another device.
     */
    private suspend fun commit(vararg payloads: EventPayload) {
        if (payloads.isEmpty()) return
        mutex.withLock { commitLocked(*payloads) }
    }

    /**
     * The write itself, for a caller that already holds [mutex].
     *
     * There is exactly one such caller, [recordAppOpened], and it needs to decide
     * whether to write at all from a query it must not race against its own append.
     * [mutex] is not reentrant, so a check that took the lock and then called
     * [commit] would deadlock rather than misbehave visibly.
     *
     * Split out rather than duplicated. Two write paths would be two places to
     * forget a lamport reservation or a cache upsert, and MASTER_BUILD_PROMPT 5.5
     * allows exactly one.
     */
    private suspend fun commitLocked(vararg payloads: EventPayload) {
        check(loaded) { "commit before load" }
        val previous = _state.value
        val wallClock = clock.nowMillis()
        val firstLamport = prefs.reserveLamport(payloads.size, previous.lastLamport)

        val built = payloads.mapIndexed { index, payload ->
            ClarityEvent.of(
                id = UUID.randomUUID().toString(),
                wallClock = wallClock,
                lamport = firstLamport + index,
                originId = originId,
                payload = payload,
            )
        }
        val next = built.fold(previous, ClarityReducer::apply)

        db.withTransaction {
            events.appendAll(built.map { it.toRow() })
            writeCache(previous, next)
        }
        _state.value = next
    }

    /** Upserts only what changed. Every row here is derived and rebuildable. */
    private suspend fun writeCache(previous: ClarityState, next: ClarityState) {
        val areas = next.areas.values.filter { previous.areas[it.id] != it }
        if (areas.isNotEmpty()) cache.upsertAreas(areas.map { it.toRow() })

        val items = next.items.values.filter { previous.items[it.id] != it }
        if (items.isNotEmpty()) cache.upsertItems(items.map { it.toRow() })

        val sessions = next.focusSessions.values.filter { previous.focusSessions[it.id] != it }
        if (sessions.isNotEmpty()) cache.upsertFocusSessions(sessions.map { it.toRow() })

        val pulses = next.pulses.values.filter { previous.pulses[it.dateKey] != it }
        if (pulses.isNotEmpty()) cache.upsertPulses(pulses.map { it.toRow() })

        val reports = next.reports.values.filter { previous.reports[it.weekStartKey] != it }
        if (reports.isNotEmpty()) cache.upsertReports(reports.map { it.toRow() })

        val plans = next.plans.values.filter { previous.plans[it.id] != it }
        if (plans.isNotEmpty()) cache.upsertPlans(plans.map { it.toRow() })

        val known = previous.conflicts.mapTo(HashSet()) { it.id }
        val conflicts = next.conflicts.filterNot { it.id in known }
        if (conflicts.isNotEmpty()) cache.upsertConflicts(conflicts.map { it.toRow() })
    }

    private suspend fun newestCheckpoint(): ClarityCheckpoint? {
        val row = cache.newestSnapshot() ?: return null
        return runCatching {
            ClarityCheckpoint(
                position = snapshotJson.decodeFromString(row.positionJson),
                state = snapshotJson.decodeFromString(row.stateJson),
            )
        }.getOrNull()
    }

    /** Written when a week closes. Phase 8 calls this; the read path exists now. */
    suspend fun writeCheckpoint(weekStartKey: String) = mutex.withLock {
        val current = _state.value
        val all = events.allInOrder().mapNotNull { it.toEvent() }
        val checkpoint = ClarityReplay.checkpoint(all)
        val position = checkpoint.position ?: return@withLock
        cache.upsertSnapshot(
            WeekSnapshotRow(
                weekStartKey = weekStartKey,
                takenAt = clock.nowMillis(),
                lamport = position.lamport,
                positionJson = snapshotJson.encodeToString(position),
                stateJson = snapshotJson.encodeToString(current.canonical()),
            ),
        )
    }

    private fun activeDurationDays(item: ItemState): Int {
        val since = item.activeSince ?: item.createdAt
        return clock.daysBetween(since, clock.nowMillis()).coerceAtLeast(0)
    }

    companion object {
        const val MAX_AREA_NAME = 40
        const val MAX_ITEM_TITLE = 200

        /**
         * Distinguishes "leave the first step alone" from "clear it". Identity
         * compared, never equals, so it can never collide with a real value.
         */
        val KEEP_FIRST_STEP: String = String("keep the first step".toCharArray())

        /**
         * SQLite binds at most 999 variables in one statement on older builds, so
         * every `IN (:ids)` list is chunked at this before it reaches the DAO.
         */
        private const val MAX_BOUND_IDS = 900

        private val snapshotJson = Json {
            encodeDefaults = true
            ignoreUnknownKeys = true
        }
    }
}

/** What the UI has to do after a completion, decided by the After completing setting. */
sealed interface CompletionOutcome {
    /** The queue head took over. The hero animation runs on this. */
    data class Promoted(val itemId: String, val title: String) : CompletionOutcome

    /** The queue had something in it and the person chooses. Dismissing leaves the area idle. */
    data class ChooseFromQueue(val areaId: String) : CompletionOutcome

    /** Nothing was waiting. An idle area is a real state, not an error. */
    data object AreaIdle : CompletionOutcome

    /** Only an active item can be completed. */
    data object NotAllowed : CompletionOutcome
}
