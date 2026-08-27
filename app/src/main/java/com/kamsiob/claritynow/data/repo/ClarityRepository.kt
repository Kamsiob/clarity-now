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
import com.kamsiob.claritynow.data.event.AreaUnarchived
import com.kamsiob.claritynow.data.event.ClarityEvent
import com.kamsiob.claritynow.data.event.EventPayload
import com.kamsiob.claritynow.data.event.ItemAdded
import com.kamsiob.claritynow.data.event.ItemCompleted
import com.kamsiob.claritynow.data.event.ItemDeleted
import com.kamsiob.claritynow.data.event.ItemEdited
import com.kamsiob.claritynow.data.event.ItemPromoted
import com.kamsiob.claritynow.data.event.ItemReopened
import com.kamsiob.claritynow.data.event.ItemReordered
import com.kamsiob.claritynow.data.event.ItemStatus
import com.kamsiob.claritynow.data.prefs.AfterCompleting
import com.kamsiob.claritynow.data.prefs.ClarityPreferences
import com.kamsiob.claritynow.domain.ClarityClock
import com.kamsiob.claritynow.domain.daysBetween
import com.kamsiob.claritynow.domain.replay.ClarityCheckpoint
import com.kamsiob.claritynow.domain.replay.ClarityConflict
import com.kamsiob.claritynow.domain.replay.ClarityReducer
import com.kamsiob.claritynow.domain.replay.ClarityReplay
import com.kamsiob.claritynow.domain.replay.ClarityState
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
        val orderKey = current.liveAreas.lastOrNull()
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
        val before = without.getOrNull(target - 1)?.orderKey
        val after = without.getOrNull(target)?.orderKey
        val newKey = OrderKey.between(before, after, jitter)
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
     * Adds an item, and promotes it in the same commit when the area is idle.
     *
     * The add sheet states where the item will land before the person commits, and
     * [wouldBecomeActive] is what it asks.
     */
    suspend fun addItem(areaId: String, rawTitle: String, rawNote: String? = null): String? {
        val title = rawTitle.trim()
        if (title.isEmpty() || title.length > MAX_ITEM_TITLE) return null
        val current = _state.value
        val area = current.areas[areaId]?.takeIf { it.deletedAt == null } ?: return null

        val note = rawNote?.trim()?.takeIf { it.isNotEmpty() }
        val itemId = UUID.randomUUID().toString()
        val queue = current.queueIn(areaId)
        val orderKey = queue.lastOrNull()
            ?.let { OrderKey.last(it.orderKey, jitter) }
            ?: OrderKey.first(jitter)

        val added = ItemAdded(
            itemId = itemId,
            areaId = areaId,
            title = title,
            note = note,
            orderKey = orderKey,
            areaNameSnapshot = area.name,
        )
        if (current.activeItemIn(areaId) == null) {
            commit(
                added,
                ItemPromoted(
                    itemId = itemId,
                    areaId = areaId,
                    previousStatus = ItemStatus.QUEUED,
                    demotedItemId = null,
                    demotedToOrderKey = null,
                    titleSnapshot = title,
                    areaNameSnapshot = area.name,
                ),
            )
        } else {
            commit(added)
        }
        return itemId
    }

    fun wouldBecomeActive(areaId: String): Boolean = _state.value.activeItemIn(areaId) == null

    /**
     * How many areas the default color walk has already spent. The palette itself
     * is a visual concern and lives in `ui.theme`, so the count comes from here and
     * the color is chosen there.
     */
    fun areaCountForColorWalk(): Int = _state.value.areas.values.count { it.deletedAt == null }

    suspend fun editItem(itemId: String, rawTitle: String, rawNote: String?) {
        val title = rawTitle.trim()
        if (title.isEmpty() || title.length > MAX_ITEM_TITLE) return
        val item = _state.value.items[itemId]?.takeIf { it.deletedAt == null } ?: return
        val note = rawNote?.trim()?.takeIf { it.isNotEmpty() }
        if (item.title == title && item.note == note) return
        commit(
            ItemEdited(
                itemId = itemId,
                previousTitle = item.title,
                newTitle = title,
                previousNote = item.note,
                newNote = note,
            ),
        )
    }

    /**
     * Completes the active item, then does whatever the After completing setting
     * says. Only an active item can be completed, and that rule is the one doing
     * the philosophical work, so it is enforced here as well as in the reducer.
     */
    suspend fun completeItem(itemId: String): CompletionOutcome {
        val current = _state.value
        val item = current.items[itemId]?.takeIf { it.deletedAt == null }
            ?: return CompletionOutcome.NotAllowed
        if (item.status != ItemStatus.ACTIVE) return CompletionOutcome.NotAllowed
        val area = current.areas[item.areaId] ?: return CompletionOutcome.NotAllowed

        val completed = ItemCompleted(
            itemId = itemId,
            areaId = item.areaId,
            titleSnapshot = item.title,
            areaNameSnapshot = area.name,
            activeDurationDays = activeDurationDays(item),
        )

        val queue = current.queueIn(item.areaId)
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
                        areaId = item.areaId,
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
                CompletionOutcome.ChooseFromQueue(item.areaId)
            }
        }
    }

    /**
     * Swap. The active item goes to the head of the queue and [itemId] takes its
     * place, as one event carrying the demoted id, which is what makes it replay
     * correctly on another device. Swaps are ordinary behavior and carry no warning
     * tone anywhere.
     */
    suspend fun swapToItem(itemId: String) {
        val current = _state.value
        val item = current.items[itemId]?.takeIf { it.deletedAt == null } ?: return
        if (item.status != ItemStatus.QUEUED) return
        val area = current.areas[item.areaId] ?: return
        val sitting = current.activeItemIn(item.areaId)

        val demotedKey = sitting?.let {
            val queue = current.queueIn(item.areaId).filterNot { queued -> queued.id == itemId }
            OrderKey.before(queue.firstOrNull()?.orderKey ?: item.orderKey, jitter)
        }
        commit(
            ItemPromoted(
                itemId = itemId,
                areaId = item.areaId,
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

    suspend fun reopenItem(itemId: String) {
        val current = _state.value
        val item = current.items[itemId]?.takeIf { it.deletedAt == null } ?: return
        if (item.status != ItemStatus.COMPLETED) return
        val head = current.queueIn(item.areaId).firstOrNull()
        val targetKey = head?.let { OrderKey.before(it.orderKey, jitter) } ?: OrderKey.first(jitter)
        commit(ItemReopened(itemId = itemId, areaId = item.areaId, targetOrderKey = targetKey))
    }

    /** Moves a queued item to [toIndex] within its area's queue. */
    suspend fun moveItem(itemId: String, toIndex: Int) {
        val current = _state.value
        val item = current.items[itemId] ?: return
        val queue = current.queueIn(item.areaId)
        val from = queue.indexOfFirst { it.id == itemId }
        if (from < 0) return
        val target = toIndex.coerceIn(0, queue.size - 1)
        if (from == target) return

        val without = queue.filterNot { it.id == itemId }
        val newKey = OrderKey.between(
            without.getOrNull(target - 1)?.orderKey,
            without.getOrNull(target)?.orderKey,
            jitter,
        )
        commit(
            ItemReordered(
                itemId = itemId,
                areaId = item.areaId,
                previousOrderKey = item.orderKey,
                newOrderKey = newKey,
            ),
        )
    }

    suspend fun moveItemToFront(itemId: String) = moveItem(itemId, 0)

    /**
     * A tombstone. The undo window lives in the UI and expires before this is
     * called, so there is no event to compensate for and nothing to explain later.
     */
    suspend fun deleteItem(itemId: String) {
        val current = _state.value
        val item = current.items[itemId]?.takeIf { it.deletedAt == null } ?: return
        commit(
            ItemDeleted(
                itemId = itemId,
                areaId = item.areaId,
                titleSnapshot = item.title,
            ),
        )
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
        mutex.withLock {
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
