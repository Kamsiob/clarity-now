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
import com.kamsiob.claritynow.data.event.FocusCompleted
import com.kamsiob.claritynow.data.event.FocusEndedEarly
import com.kamsiob.claritynow.data.event.FocusExtended
import com.kamsiob.claritynow.data.event.FocusStarted
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
import com.kamsiob.claritynow.data.event.PulseAnswered
import com.kamsiob.claritynow.data.event.PulseGenerated
import com.kamsiob.claritynow.data.export.ClarityBackupStore
import com.kamsiob.claritynow.data.export.ExportSnapshot
import com.kamsiob.claritynow.data.prefs.AfterCompleting
import com.kamsiob.claritynow.data.prefs.ClarityPreferences
import com.kamsiob.claritynow.domain.ClarityClock
import com.kamsiob.claritynow.domain.dateKey
import com.kamsiob.claritynow.domain.daysBetween
import com.kamsiob.claritynow.domain.parseDateKey
import com.kamsiob.claritynow.domain.weekStartKey
import com.kamsiob.claritynow.domain.engine.catalog.ResponseOption
import com.kamsiob.claritynow.domain.query.ReEntry
import com.kamsiob.claritynow.domain.query.TrailQueries
import com.kamsiob.claritynow.domain.replay.AreaState
import com.kamsiob.claritynow.domain.replay.ClarityCheckpoint
import com.kamsiob.claritynow.domain.replay.ClarityCheckpointCodec
import com.kamsiob.claritynow.domain.replay.ClarityConflict
import com.kamsiob.claritynow.domain.replay.ClarityReducer
import com.kamsiob.claritynow.domain.replay.ClarityReplay
import com.kamsiob.claritynow.domain.replay.ClarityState
import com.kamsiob.claritynow.domain.replay.FocusOutcome
import com.kamsiob.claritynow.domain.replay.FocusSessionState
import com.kamsiob.claritynow.domain.replay.ItemState
import com.kamsiob.claritynow.domain.replay.OrderKey
import com.kamsiob.claritynow.domain.replay.PulseEntryState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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
) : ClarityBackupStore {

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
     * log it is sitting on. MASTER_BUILD_PROMPT 6.4.
     *
     * **The tail is read from SQL, not filtered out of the whole log.** An earlier
     * shape of this method read every row, decoded every payload and then skipped
     * the fold, which saves the cheaper half of a cold start and leaves the
     * expensive half in place. [resumeOrRebuildLocked] asks the log three bounded
     * questions instead, and touches nothing before the checkpoint unless it has
     * decided it cannot trust it.
     *
     * The last thing it does is close last week if last week is still open, which
     * is a full rebuild from event zero and happens at most once a week. That is
     * where a checkpoint comes from, and [writeCheckpointLocked] says why it is the
     * only place one can come from.
     */
    suspend fun load() {
        mutex.withLock {
            if (loaded) return@withLock
            originId = prefs.originId()
            jitter = OrderKey.jitterFor(originId)

            val next = resumeOrRebuildLocked()
            _state.value = next
            writeCache(ClarityState.EMPTY, next)
            closeWeekIfNeededLocked()
            loaded = true
        }
    }

    /**
     * The state to start this session with, and the whole of the checkpoint read
     * path.
     *
     * Three bounded queries decide, and none of them reads the head of the log:
     * `count()` is one aggregate, `after()` is a range scan from the checkpoint
     * forward on the `lamport` index, and `exists()` is a primary key seek. What
     * they answer is `ClarityReplay.canResume`, which is stated once, over there,
     * beside the fold it protects.
     *
     * The count is the half that catches a merged log, and it is stated in rows
     * rather than in decoded events on purpose. A row this build cannot decode, from
     * an event type a newer version introduced, makes the two numbers disagree and
     * sends the load down the rebuild path, which is the conservative direction: the
     * alternative is a state folded from fewer events than the checkpoint was.
     *
     * The three queries are not one statement and do not need to be. Everything runs
     * under [mutex], and this class is the only writer in the app, so no row can
     * appear between the count and the range scan.
     */
    private suspend fun resumeOrRebuildLocked(): ClarityState {
        val checkpoint = newestCheckpoint() ?: return fullReplayLocked()
        val position = checkpoint.position ?: return fullReplayLocked()

        val tail = events.after(position.lamport, position.originId, position.eventId)
        val resumable = ClarityReplay.canResume(
            checkpoint = checkpoint,
            eventsAtOrBeforePosition = events.count() - tail.size,
            positionIsInLog = events.exists(position.eventId),
        )
        if (!resumable) return fullReplayLocked()
        return ClarityReplay.replayFrom(checkpoint, tail.mapNotNull { it.toEvent() })
    }

    /** A fold of the entire log. Always correct, and the fallback for everything. */
    private suspend fun fullReplayLocked(): ClarityState =
        ClarityReplay.replay(events.allInOrder().mapNotNull { it.toEvent() })

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

    /**
     * Brings an area back out of the archive, with everything that was in it.
     * `MASTER_BUILD_PROMPT.md` section 5's archive rule, design-v3.md 10.20, issue #15.
     *
     * **It lands where it was**, because an archived area keeps its key and every
     * writer here chooses against the restorable set rather than the visible one, so
     * nothing has taken it. [restoredOrderKey] carries why that is the answer rather
     * than the end of the list, and the one case where a log this app did not write
     * makes it a question.
     *
     * The reorder goes first and both payloads go into one [commit], so the two are one
     * transaction and one fold. In the ordinary case there is no reorder at all and this
     * writes the single event the Trail already renders.
     */
    suspend fun unarchiveArea(areaId: String) {
        val area = _state.value.areas[areaId] ?: return
        if (!area.archived || area.deletedAt != null) return
        val moved = restoredOrderKey(_state.value, areaId, jitter)
        val unarchived = AreaUnarchived(areaId = areaId, nameSnapshot = area.name)
        if (moved == null) {
            commit(unarchived)
        } else {
            commit(
                AreaReordered(
                    areaId = areaId,
                    previousOrderKey = area.orderKey,
                    newOrderKey = moved,
                ),
                unarchived,
            )
        }
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
     * The whole of the rule, and the collision that made it necessary, is on
     * [keyTightenedBetween] in `AreaRestore.kt`. It moved there when the archive view
     * landed, because restoring an area needs the same rule and needs it from a
     * desktop JVM, and two copies of this particular pass is how the 0.2.0 defect
     * existed in the first place. This binds it to this device's [jitter].
     */
    private fun tightenedBetween(lower: String?, upper: String?, occupied: List<String>): String =
        keyTightenedBetween(lower = lower, upper = upper, occupied = occupied, jitter = jitter)

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
        val filed = ItemFiled(
            itemId = itemId,
            areaId = areaId,
            orderKey = tailOrderKey(current.liveItemsIn(areaId)),
            areaNameSnapshot = area.name,
        )
        // Filing into an idle area promotes in the same transaction, exactly as an add
        // does in 8.2. MASTER_BUILD_PROMPT 14b.1 requires it, and the alternative
        // leaves an area sitting idle with something queued behind it, which is a
        // state the Areas screen has no way to read: it would show `Add your first
        // item` above a queue that is not empty.
        //
        // Filing is still the separate, optional act Addendum 01 4a asks for. What is
        // not optional is that an area with something in it has an active item.
        if (current.activeItemIn(areaId) == null) {
            commit(
                filed,
                ItemPromoted(
                    itemId = itemId,
                    areaId = areaId,
                    previousStatus = ItemStatus.QUEUED,
                    demotedItemId = null,
                    demotedToOrderKey = null,
                    titleSnapshot = item.title,
                    areaNameSnapshot = area.name,
                ),
            )
        } else {
            commit(filed)
        }
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
     * An unfiled item is deleted the same way anything else is. `ITEM_DELETED`
     * carries a nullable area for exactly this case: taking something back out of
     * the inbox is the one operation an inbox must always support, and requiring an
     * area would have meant filing it first in order to throw it away.
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

    // Focus -------------------------------------------------------------------
    //
    // MASTER_BUILD_PROMPT 10 and 14b.5. Four write paths and one read model, and the
    // rules they apply are pure functions in FocusSession.kt so that a unit test can
    // reach them without Room or DataStore.
    //
    // **One running session at a time is enforced here and nowhere else.** The
    // chooser is not the only door: a notification action, an app shortcut and a
    // widget all reach this object, and a rule that lives in a screen is a rule that
    // holds until the next screen.

    /**
     * The scope the shared ticker and the shared countdown live in.
     *
     * Process lifetime, because ClarityGraph builds one repository for the
     * process and the countdown has to outlive any one Activity: backing out of a
     * focus session leaves it running, per design-v3.md 10.15, and the ongoing
     * notification keeps reading it while no screen exists. Every flow started in
     * it uses `WhileSubscribed`, so at rest it holds no coroutine at all.
     */
    private val focusScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /**
     * The single 1Hz ticker. design-v3.md 8.2 item 7.
     *
     * Private on purpose. Everything reads [focusCountdown] instead, so the focus
     * screen, the ongoing notification and the Live Update cannot derive remaining
     * time three slightly different ways.
     */
    private val focusTicks: Flow<Long> = secondTicks(clock::nowMillis)
        .shareIn(focusScope, SharingStarted.WhileSubscribed(), replay = 1)

    /**
     * The running session this device owns, with no ticker attached.
     *
     * Collect this when all you need is whether a session exists and which item it
     * is on. Collecting [focusCountdown] instead would keep the ticker awake once a
     * second for a screen that only redraws once a minute.
     *
     * Driven by the stored handle rather than by the log fallback, because the
     * fallback needs a query and this is a hot flow. [restoreFocus] runs the
     * fallback on every foreground and repairs the handle, so the two agree by the
     * time any screen is looking.
     */
    val runningFocusSession: StateFlow<FocusSessionState?> =
        combine(state, prefs.focusHandle) { current, handle ->
            handle?.sessionId
                ?.let { current.focusSessions[it] }
                ?.takeIf { it.outcome == FocusOutcome.RUNNING }
        }.stateIn(focusScope, SharingStarted.WhileSubscribed(SUBSCRIPTION_GRACE_MILLIS), null)

    /**
     * The one countdown in the app, shared. design-v3.md 8.2 item 7.
     *
     * There is exactly one `combine` and one ticker behind this no matter how many
     * collectors it has, which is the point: a notification that disagrees with the
     * screen by a second is a notification a person stops trusting. Only the numeral
     * and the arc are meant to redraw on a tick, so a collector should read
     * [FocusCountdown.fractionRemaining] and [FocusCountdown.remainingSeconds] and
     * leave the rest of its surface alone.
     *
     * Null when this device has no running session, which is the ordinary state.
     */
    val focusCountdown: StateFlow<FocusCountdown?> =
        combine(runningFocusSession, focusTicks) { session, now -> session?.countdownAt(now) }
            .stateIn(focusScope, SharingStarted.WhileSubscribed(SUBSCRIPTION_GRACE_MILLIS), null)

    /**
     * Starts a session on an area's active item. Returns the session id, or null if
     * it was refused.
     *
     * Refused when the area has no active item, when [itemId] is not that item,
     * when [plannedSeconds] is not a duration, or when this device already has a
     * session running. The check and the append are taken under one lock, so two
     * taps a frame apart cannot both pass the one at a time rule.
     *
     * The end instant is stored the moment the session exists, which is what makes
     * the session survive the process being killed. See [ClarityPreferences] and
     * `data.prefs.FocusHandle` for why a per device instant is not engine state.
     * The handle is written after the event rather than before, so the worst
     * failure leaves a session in the log with no handle, and [restoreFocus] finds
     * that one from the log and repairs it.
     */
    suspend fun startFocus(areaId: String, itemId: String, plannedSeconds: Int): String? =
        mutex.withLock {
            if (!loaded) return@withLock null
            val current = _state.value
            val running = deviceSessionLocked(current)
            if (!canStartFocus(current, areaId, itemId, plannedSeconds, running)) return@withLock null
            val sessionId = UUID.randomUUID().toString()
            commitLocked(
                FocusStarted(
                    sessionId = sessionId,
                    areaId = areaId,
                    itemId = itemId,
                    plannedSeconds = plannedSeconds,
                ),
            )
            val started = _state.value.focusSessions[sessionId] ?: return@withLock null
            prefs.setFocusHandle(sessionId, started.plannedEndsAt)
            sessionId
        }

    /**
     * A session that ran its planned course. Writes `FOCUS_COMPLETED`.
     *
     * [actualSeconds] is a real duration and never a comparison against the plan.
     * Pass [FocusCountdown.elapsedSeconds], which is the same arithmetic the numeral
     * on the ring was drawn from.
     */
    suspend fun completeFocus(sessionId: String, actualSeconds: Int) =
        endSession(sessionId, actualSeconds, completed = true)

    /**
     * A session a person ended before its planned time. Writes `FOCUS_ENDED_EARLY`.
     *
     * **This is a completed short session and the surfaces above it say so.**
     * Addendum 01 4e: fourteen minutes is fourteen minutes. The event type carries
     * no judgment either, which is why it was renamed out of `FOCUS_ABANDONED` while
     * the schema window was open. DECISIONS.md C6.
     *
     * Under [FOCUS_DISCARD_UNDER_SECONDS] the ending is a mis-tap and the interface
     * shows neither a confirm before it nor a completion screen after it, but the
     * event is still written, because the log records what happened. The threshold
     * is a constant in `FocusSession.kt` so that the screen and this agree on it.
     */
    suspend fun endFocusEarly(sessionId: String, actualSeconds: Int) =
        endSession(sessionId, actualSeconds, completed = false)

    private suspend fun endSession(sessionId: String, actualSeconds: Int, completed: Boolean) {
        val seconds = actualSeconds.coerceAtLeast(0)
        mutex.withLock {
            val session = _state.value.focusSessions[sessionId] ?: return@withLock
            if (session.outcome != FocusOutcome.RUNNING) return@withLock
            commitLocked(
                if (completed) {
                    FocusCompleted(sessionId = sessionId, actualSeconds = seconds)
                } else {
                    FocusEndedEarly(sessionId = sessionId, actualSeconds = seconds)
                },
            )
            releaseHandleFor(sessionId)
        }
    }

    /**
     * Adds time to a running session without ending it. Addendum 01 4f, 14b.5.
     *
     * A session has exactly one `FOCUS_STARTED` and at most one terminal event, and
     * an extension is neither, so nothing here restarts a timer or opens a second
     * session. Ending a timer must never be the price of needing ten more minutes.
     * Repeatable and uncapped, per 14b.5, because a limit is an argument with
     * someone who is working.
     *
     * The payload carries the absolute new figure as well as the delta, and this
     * computes it from the session's current `plannedSeconds` in the projection
     * rather than from what the screen last drew. Two extensions in the same minute
     * then agree, and a replay cannot arrive at a different number from the one the
     * person was shown, which is what applying a delta at fold time would risk.
     *
     * **The stored end instant moves with it**, which 14b.5 requires: an extension
     * that moved the planned total without moving the stored instant would give the
     * added minutes back at the next cold start. Both happen under one lock.
     *
     * Refused for a session that has already ended, matching the reducer, and for a
     * non positive [addedSeconds], which is not an extension.
     */
    suspend fun extendFocus(sessionId: String, addedSeconds: Int) {
        if (addedSeconds <= 0) return
        mutex.withLock {
            val session = _state.value.focusSessions[sessionId] ?: return@withLock
            if (session.outcome != FocusOutcome.RUNNING) return@withLock
            commitLocked(
                FocusExtended(
                    sessionId = sessionId,
                    addedSeconds = addedSeconds,
                    newPlannedSeconds = session.plannedSeconds + addedSeconds,
                ),
            )
            val extended = _state.value.focusSessions[sessionId] ?: return@withLock
            if (prefs.focusHandle.first()?.sessionId == sessionId) {
                prefs.setFocusHandle(sessionId, extended.plannedEndsAt)
            }
        }
    }

    /**
     * What to do about a focus session on a cold start or a resume. Called by the
     * app shell on every foreground, beside `recordAppOpened`.
     *
     * Three answers, and the third is the case MASTER_BUILD_PROMPT 10 calls being
     * backgrounded at completion:
     *
     * - [FocusRestore.None]: nothing is running. The ordinary route stands.
     * - [FocusRestore.Running]: restore the focus screen at the countdown returned.
     *   This is the process death case, and the remaining time is computed from the
     *   log rather than from anything that had to survive the kill.
     * - [FocusRestore.Completed]: the planned time ran out while the process was
     *   dead or the app was away. `FOCUS_COMPLETED` has been written here, with
     *   `actualSeconds` equal to the **planned** seconds rather than the wall clock
     *   gap since the session started, because the session ran for the time it was
     *   planned to run and the gap is a fact about the phone. The completion state
     *   is what a person should see, in the same words a session that finished
     *   while they watched would use.
     *
     * Safe to call on every resume: with nothing running it is one preference read
     * and one map lookup, and it writes nothing.
     *
     * A handle that names no running session is cleared, so a preference that
     * outlived its session cannot keep the one at a time rule locked shut.
     */
    suspend fun restoreFocus(): FocusRestore = mutex.withLock {
        if (!loaded) return@withLock FocusRestore.None
        val current = _state.value
        val session = deviceSessionLocked(current)
        if (session == null) {
            releaseHandle()
            return@withLock FocusRestore.None
        }
        when (val decision = focusRestoreFor(session, clock.nowMillis())) {
            is FocusRestore.Running -> {
                // Repairs a handle the fallback found in the log, and is a no op
                // when the handle was already right.
                prefs.setFocusHandle(session.id, session.plannedEndsAt)
                decision
            }

            is FocusRestore.Completed -> {
                commitLocked(
                    FocusCompleted(
                        sessionId = session.id,
                        actualSeconds = session.plannedSeconds,
                    ),
                )
                releaseHandle()
                FocusRestore.Completed(_state.value.focusSessions[session.id] ?: session)
            }

            FocusRestore.None -> {
                releaseHandle()
                FocusRestore.None
            }
        }
    }

    /**
     * The running session this device owns, resolved the expensive way.
     *
     * The stored handle answers it in a map lookup on the ordinary path. When it
     * cannot, the log answers it: the `FOCUS_STARTED` rows for the sessions that are
     * still running carry the origin id of the device that wrote them, and one of
     * them is this phone. That fallback is what keeps the stored instant a cache
     * rather than a second source of truth, and it is the reason `CLAUDE.md` rule 6
     * is not violated by storing it. The query runs only when the handle misses.
     */
    private suspend fun deviceSessionLocked(current: ClarityState): FocusSessionState? {
        val handle = prefs.focusHandle.first()
        val byHandle = pickDeviceSession(current, handle?.sessionId, originId) { null }
        if (byHandle != null) return byHandle
        val running = current.focusSessions.values
            .filter { it.outcome == FocusOutcome.RUNNING }
            .map { it.id }
        if (running.isEmpty()) return null
        val startedBy = focusOriginsFor(running)
            .mapNotNull { event -> event.entityId?.let { it to event.originId } }
            .toMap()
        return pickDeviceSession(current, null, originId) { startedBy[it] }
    }

    private suspend fun releaseHandleFor(sessionId: String) {
        if (prefs.focusHandle.first()?.sessionId == sessionId) prefs.clearFocusHandle()
    }

    private suspend fun releaseHandle() {
        if (prefs.focusHandle.first() != null) prefs.clearFocusHandle()
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

    /**
     * The return this open is, or null on every other open. MASTER_BUILD_PROMPT 14b.4.
     *
     * The one question the re-entry screen asks, and the only place in the app that
     * asks it. It answers non null on the day of a return and never again for that
     * gap, because [TrailQueries.reEntryOn] is true on exactly one calendar day.
     *
     * **It writes today's marker itself rather than assuming somebody else has.**
     * `ClarityApp` writes it on the first foreground and this runs on the first
     * composition of the same launch, so the two are concurrent, and "the write has
     * probably landed by now" is a race whose failure mode is a returning person seeing
     * nothing at all. [recordAppOpened] is at most once per calendar day, decides under
     * the same lock it writes under, and costs one indexed read when the day is already
     * there, so a second caller is what it was built for rather than a cost. Reading
     * before the marker exists is the exact trap that method's own documentation names:
     * the gap is measured to the newest open strictly before today, and today has to be
     * in the log for [TrailQueries.reEntryOn] to have a today to be about.
     *
     * **[load] first, because a commit before a load is a crash rather than a missing
     * event**, and it is idempotent under the same lock, so whichever caller reaches it
     * first does the work.
     *
     * **The log handed to [TrailQueries] is the presence markers and nothing else**,
     * through the one bounded query that can select them. `reEntryOn` reads
     * `APP_OPENED` payloads and no other event, so the answer is identical to the one a
     * whole log would give, and the difference is that this runs on a cold start beside
     * everything else a cold start is doing. A full `allEvents` here would be a second
     * pass over the largest table in the app for a question about a handful of rows.
     *
     * **The value is the date of the return. It is never the length of the absence**,
     * and [ReEntry] has no field and no function that yields one. DECISIONS.md.
     */
    suspend fun reEntryOnThisOpen(): ReEntry? {
        load()
        recordAppOpened()
        val opens = events.ofTypes(listOf(ClarityEventType.APP_OPENED.name))
            .mapNotNull { it.toEvent() }
        return TrailQueries(opens, clock.zone()).reEntryOn(clock.dateKey())
    }

    /**
     * The re-entry screen's second choice. Every active item returns to the head of its
     * own queue. MASTER_BUILD_PROMPT 14b.4.
     *
     * **Nothing is deleted and nothing is completed**, and [activeItemsBackInTheirQueues]
     * is where that is proved rather than here: it decides the whole answer, it can only
     * return `ITEM_QUEUED`, and it runs on a plain JVM where a test can read it.
     *
     * One commit rather than one per item, so a person who chose this and lost the
     * process halfway through does not come back to half a fresh start. The keys are
     * chosen against one state, which is correct because an area holds at most one
     * active item: no two payloads here are choosing a key in the same ordering space.
     */
    suspend fun putActiveItemsBackInTheirQueues() {
        val payloads = activeItemsBackInTheirQueues(_state.value, jitter)
        commit(*payloads.toTypedArray())
    }

    // Pulse -------------------------------------------------------------------
    //
    // Two writes and one read, and between them they are the whole of the Pulse's
    // access to the log. The decision about whether there is anything to say is not
    // here: it is `domain.pulse.PulseGenerator`, which is a pure function of the log,
    // can therefore be tested, and has no way to write. This end holds the writes and
    // enforces the two rules that have to hold against a race rather than merely
    // against a slow caller.

    /**
     * The Pulse entry for [dateKey], or null when the day is IDLE.
     *
     * Read from the in memory projection, which is the authority, per section 4. A
     * silent day has no row, so null here is a real state and not a miss.
     */
    fun pulseFor(dateKey: String): PulseEntryState? = _state.value.pulses[dateKey]

    /**
     * Appends `PULSE_GENERATED` unless the day already has an entry, and answers the
     * entry that stands either way. MASTER_BUILD_PROMPT 11.3 step 2 and 12.1.
     *
     * **At most one per calendar day, and immutable once written.** The check and the
     * append are taken under one lock, through [commitLocked], for the same reason
     * [recordAppOpened] is: two foregrounds racing at launch would otherwise both read
     * an empty answer and both write, and here that is not harmless. A second entry for
     * one date key is a second observation about the same day, and the reducer would
     * record it as a `DUPLICATE_DATE_KEY` conflict, which is the shape reserved for two
     * devices disagreeing after a merge rather than for one device disagreeing with
     * itself.
     *
     * The caller does the expensive part outside this lock. Extracting facts and
     * rebuilding the firing history read the whole log, and holding the write lock
     * across them would block every other write in the app on the first foreground of
     * the day.
     */
    suspend fun recordPulseGenerated(payload: PulseGenerated): PulseEntryState = mutex.withLock {
        val existing = _state.value.pulses[payload.dateKey]
        if (existing != null) return@withLock existing
        commitLocked(payload)
        requireNotNull(_state.value.pulses[payload.dateKey]) {
            "the reducer did not file the Pulse for ${payload.dateKey}"
        }
    }

    /**
     * Appends `PULSE_ANSWERED` for [dateKey], storing [option]'s label **verbatim**.
     *
     * Null when there is no entry for the day. The entry unchanged when it has already
     * been answered: a second answer is a double tap or a stale screen, and neither is
     * an error worth telling anyone about. The reducer ignores it too, so the two agree.
     *
     * **The whole [ResponseOption] travels rather than three strings**, so the label,
     * the key and the polarity cannot be assembled from different places. A callback
     * quotes what the person actually saw, per CLARITY_LOGIC_ENGINE.md 3.1, and that is
     * only true if the string stored here is the string the pill carried. A label
     * reworded in a later release must not rewrite what an old answer said.
     *
     * **The subject is denormalized off the `PULSE_GENERATED` this answers**, read back
     * by one seek of the `entityId` index rather than joined at read time. The payload
     * documents why: `selfReportVsData` is the family CORPUS_2_REPORT.md calls the
     * flagship of the whole engine and it sets what somebody said about an area against
     * what they did in it, so it must not depend on a join that can miss. The
     * projection cannot supply it, because `PulseEntryState` carries no subject.
     */
    suspend fun answerPulse(dateKey: String, option: ResponseOption): PulseEntryState? =
        mutex.withLock {
            val entry = _state.value.pulses[dateKey] ?: return@withLock null
            if (entry.isAnswered) return@withLock entry
            val generated = pulseGeneratedPayload(entry.id)
            commitLocked(
                PulseAnswered(
                    pulseId = entry.id,
                    responseKey = option.key,
                    responseLabel = option.label,
                    responseIsPositive = option.isPositive,
                    subjectId = generated?.subjectId,
                    subjectKind = generated?.subjectKind,
                ),
            )
            _state.value.pulses[dateKey]
        }

    /** The event a Pulse entry was built from. `PulseGenerated` uses its id as its entity id. */
    private suspend fun pulseGeneratedPayload(pulseId: String): PulseGenerated? =
        events.forEntity(pulseId)
            .asSequence()
            .mapNotNull { it.toEvent()?.payload as? PulseGenerated }
            .firstOrNull()

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
     * cache is a cache. MASTER_BUILD_PROMPT 5.4 and 6.4: exposed in the debug menu,
     * and run by the export path as a correctness check before a person is handed a
     * file they may one day restore from.
     *
     * **It answers what it found rather than only doing the work**, because a
     * correctness check that reports nothing is not a check. [RebuildCheck.matched]
     * is the interesting half: false means the state every screen has been reading
     * disagrees with the log it was supposedly folded from, which is the one defect
     * in this app that cannot be seen by looking.
     *
     * Every checkpoint goes with the cache, and one is taken again at the end if a
     * week has closed, from this same rebuild. So the expensive part is paid once
     * and the next cold start is still fast.
     */
    suspend fun rebuildCacheFromLog(): RebuildCheck = mutex.withLock {
        rebuildFromLogLocked().check
    }

    /**
     * The whole database at one instant, for [ClarityBackupStore] and the export
     * path. MASTER_BUILD_PROMPT 6.4 and 14b.7.
     *
     * **The rebuild and the read of the log happen under one lock, and that is the
     * point of this method existing rather than the caller making two calls.** An
     * export writes the events and the state that was folded from them, and this
     * class is the only writer in the app, so taking the two under one lock is what
     * makes the pair inside the file consistent with each other. Two calls could be
     * separated by a completion, and the file would carry a state one event ahead
     * of the log beside it, which nothing downstream would ever notice.
     */
    override suspend fun exportSnapshot(): ExportSnapshot = mutex.withLock {
        val rebuilt = rebuildFromLogLocked()
        ExportSnapshot(
            events = rebuilt.events,
            state = rebuilt.check.state,
            rebuildMatched = rebuilt.check.matched,
        )
    }

    /** MASTER_BUILD_PROMPT 14b.7. Settings reads it back to show the date. */
    override suspend fun recordExport(atMillis: Long) = prefs.setLastExportAt(atMillis)

    /**
     * When the oldest item this person still has was created, or null when they
     * have none. `data.export.ExportReminder` turns it into the quiet line, and the
     * note there says why an item and not an area is what counts as real data.
     */
    fun dataWorthKeepingSince(): Long? = _state.value.items.values
        .filter { it.deletedAt == null }
        .minOfOrNull { it.createdAt }

    /** The body of the rebuild, so the export path and the debug action share one. */
    private suspend fun rebuildFromLogLocked(): RebuiltLog {
        val logged = events.allInOrder().mapNotNull { it.toEvent() }
        val rebuilt = ClarityReplay.checkpoint(logged)
        val matched = if (loaded) rebuilt.state.canonical() == _state.value.canonical() else null

        db.withTransaction {
            cache.clearCache()
            cache.clearSnapshots()
            writeCache(ClarityState.EMPTY, rebuilt.state)
        }
        _state.value = rebuilt.state
        closeWeekIfNeededLocked(rebuilt)

        return RebuiltLog(
            events = logged,
            check = RebuildCheck(
                state = rebuilt.state,
                eventCount = rebuilt.state.eventsApplied,
                matched = matched,
            ),
        )
    }

    /**
     * Takes a foreign log into this one and rebuilds from event zero.
     * MASTER_BUILD_PROMPT 6.3, 6.4 and 14b.7.
     *
     * **This is the only door foreign events come through, and the reason it exists
     * before the screen that opens it does.** Import is phase 11: file format,
     * password, checksum, pre validation and the replace or merge choice all belong
     * there. What belongs here is the part that is invisible when it is missing.
     * Both modes throw every checkpoint away and fold the whole log again, because a
     * merge can introduce events that sort *before* a checkpoint's position, and a
     * checkpoint resumed over one of those quietly drops it. Nothing looks wrong
     * afterwards. The numbers are just smaller, forever.
     *
     * [IngestMode.REPLACE] empties the log first and is one transaction.
     * [IngestMode.MERGE] is the deterministic union 6.3 specifies: union by event
     * id, which `ClarityEventDao.appendAll` gets from `OnConflictStrategy.IGNORE`,
     * ordered by `(lamport, originId, id)`, which the fold gets from `inTotalOrder`.
     *
     * A checkpoint is taken again at the end when a week has closed, from the
     * merged log rather than from the one that was here before. That is the same
     * rebuild 6.4 asks for, so the fast cold start comes back on the next launch
     * instead of after the next week closes.
     *
     * The lamport counter is advanced to the merged maximum before this returns.
     * [commitLocked] would reach the same floor on its own, since it reserves
     * against `lastLamport` from the projection, but a counter that is only correct
     * because the next writer happens to repair it is a counter that is wrong in
     * between.
     */
    override suspend fun ingestForeignLog(
        incoming: List<ClarityEvent>,
        mode: IngestMode,
    ): RebuildCheck =
        mutex.withLock {
            check(loaded) { "ingest before load" }
            val rebuilt = db.withTransaction {
                if (mode == IngestMode.REPLACE) events.eraseEverything()
                events.appendAll(incoming.map { it.toRow() })
                cache.clearCache()
                cache.clearSnapshots()
                val merged = ClarityReplay.checkpoint(
                    events.allInOrder().mapNotNull { it.toEvent() },
                )
                writeCache(ClarityState.EMPTY, merged.state)
                merged
            }
            _state.value = rebuilt.state
            prefs.reserveLamport(count = 0, atLeast = rebuilt.state.lastLamport)
            closeWeekIfNeededLocked(rebuilt)
            RebuildCheck(
                state = rebuilt.state,
                eventCount = rebuilt.state.eventsApplied,
                matched = null,
            )
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

    // Checkpoints -------------------------------------------------------------
    //
    // MASTER_BUILD_PROMPT 6.4. A closed week doubles as a replay checkpoint, and the
    // two things it is are worth keeping apart while reading this section. The
    // *content* of a checkpoint is a fold of a prefix of the total order and knows
    // nothing about calendars. The *decision to take one* is a calendar question,
    // and a calendar is a wall clock object.
    //
    // That distinction is the reason a wall clock appears in this section at all,
    // when `ClarityEventDao` says plainly that nothing a checkpoint does may consult
    // one. The rule that comment states is about ordering, and it holds here without
    // an exception: no position, no prefix and no folded event below is chosen by a
    // wall clock. What a wall clock decides is only whether a checkpoint is due now,
    // and the worst a skewed one can do is take a checkpoint a week early or a week
    // late, which costs a slower cold start and nothing else.

    private suspend fun newestCheckpoint(): ClarityCheckpoint? {
        val row = cache.newestSnapshot() ?: return null
        return ClarityCheckpointCodec.decode(row.positionJson, row.stateJson)
    }

    /**
     * Takes the checkpoint for the week that has just closed, if one is due.
     * Answers the week it wrote, or null when there was nothing to do.
     *
     * Called at the end of [load], which is the first thing every entry point into
     * this app does, so the first launch of a new week pays for it and no launch
     * after that does. Safe to call at any other time; it is a lookup and two
     * bounded queries when the answer is no.
     */
    suspend fun closeWeekIfNeeded(): String? = mutex.withLock {
        check(loaded) { "close week before load" }
        closeWeekIfNeededLocked()
    }

    /**
     * The decision, for a caller that already holds [mutex].
     *
     * Two conditions, and both are guards against writing a checkpoint that buys
     * nothing. The stored checkpoint already being last week's means the week has
     * been closed. No event older than this week means there is no closed week to
     * checkpoint yet, which is every first week and is the honest answer for one.
     *
     * [rebuilt] lets a caller that has just folded the whole log hand that fold
     * over rather than pay for a second one.
     */
    private suspend fun closeWeekIfNeededLocked(rebuilt: ClarityCheckpoint? = null): String? {
        val weekStartMillis = currentWeekStartMillis()
        val closedWeekKey = clock.weekStartKey(weekStartMillis - 1)
        if (cache.newestSnapshot()?.weekStartKey == closedWeekKey) return null
        if (events.newestWallClockBefore(weekStartMillis) == null) return null
        return writeCheckpointLocked(closedWeekKey, rebuilt)
    }

    /**
     * Takes a checkpoint labeled [weekStartKey]. True when one was written.
     *
     * Public because a week can close while the app is running and because the
     * debug menu should be able to force one. Ordinary operation goes through
     * [closeWeekIfNeeded].
     */
    suspend fun writeCheckpoint(weekStartKey: String): Boolean = mutex.withLock {
        check(loaded) { "checkpoint before load" }
        writeCheckpointLocked(weekStartKey, rebuilt = null) != null
    }

    /**
     * Writes the checkpoint, and refuses to write a wrong one.
     *
     * **Every checkpoint this app stores is a full rebuild from event zero, checked
     * against the state the app is running on.** That is the expensive choice and it
     * is deliberate. A checkpoint taken from the live projection instead would be
     * one line shorter and would carry any error the projection had picked up, and
     * carry it forever: the next checkpoint resumes from this one, so a state that
     * is wrong on the day it is written is wrong in every checkpoint after it and in
     * every cold start that resumes from any of them. There is no later check that
     * finds this. The event log is the truth, so the checkpoint is taken from the
     * event log.
     *
     * A disagreement writes nothing and clears what is stored, which leaves every
     * future cold start doing a full rebuild. Slow is a fine outcome. It also self
     * heals: the next cold start folds the log, and the log is the truth.
     *
     * Exactly one checkpoint row survives, and the clear before the write is what
     * makes that true. **The obvious answer is a row per week accumulating forever,
     * and it is the wrong one**, `design-v3.md` 15. Nothing reads any checkpoint but
     * the newest, each row is a serialized copy of everything the person owns, and
     * anything that ever did read an older one to say what a past week held would be
     * engine state living outside the log, which `CLAUDE.md` rule 6 forbids. Past
     * reports are what remain forever, in `clarity_report`, and they are a different
     * table for a different reason. The key stays the week so the row still says
     * when it was taken and why.
     */
    private suspend fun writeCheckpointLocked(
        weekStartKey: String,
        rebuilt: ClarityCheckpoint?,
    ): String? {
        val checkpoint = rebuilt
            ?: ClarityReplay.checkpoint(events.allInOrder().mapNotNull { it.toEvent() })
        val position = checkpoint.position ?: return null
        if (checkpoint.state.canonical() != _state.value.canonical()) {
            cache.clearSnapshots()
            return null
        }
        db.withTransaction {
            cache.clearSnapshots()
            cache.upsertSnapshot(
                WeekSnapshotRow(
                    weekStartKey = weekStartKey,
                    takenAt = clock.nowMillis(),
                    lamport = position.lamport,
                    positionJson = ClarityCheckpointCodec.encodePosition(position),
                    stateJson = ClarityCheckpointCodec.encodeState(checkpoint.state),
                ),
            )
        }
        return weekStartKey
    }

    /** The instant this local week began. Sunday start, per MASTER_BUILD_PROMPT 12.3. */
    private fun currentWeekStartMillis(): Long =
        parseDateKey(clock.weekStartKey()).atStartOfDay(clock.zone()).toInstant().toEpochMilli()

    private fun activeDurationDays(item: ItemState): Int {
        val since = item.activeSince ?: item.createdAt
        return clock.daysBetween(since, clock.nowMillis()).coerceAtLeast(0)
    }

    companion object {
        const val MAX_AREA_NAME = 40
        const val MAX_ITEM_TITLE = 200

        /**
         * How long a shared focus flow stays alive after its last collector goes.
         *
         * Long enough that a rotation or a tab change does not tear down the ticker
         * and build it again, short enough that a backgrounded app stops waking once
         * a second. The ongoing notification keeps its own collector while a session
         * runs, so nothing depends on this value to stay correct.
         */
        private const val SUBSCRIPTION_GRACE_MILLIS = 5_000L

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
    }
}

/** The two halves one rebuild produces: the log it read, and what it found. */
private class RebuiltLog(val events: List<ClarityEvent>, val check: RebuildCheck)

/**
 * What a full rebuild from event zero found. MASTER_BUILD_PROMPT 6.4.
 *
 * [matched] is the correctness check itself: true when the rebuilt state equals the
 * state the app was running on, false when they disagree, and null when there was no
 * loaded state to compare against. A false here is not a slow path or a stale cache.
 * It means the app has been reading a projection the log does not produce, and every
 * number it has shown since is unexplained.
 */
data class RebuildCheck(
    val state: ClarityState,
    /** Events folded, which is every row this build could decode. */
    val eventCount: Int,
    val matched: Boolean?,
)

/**
 * How a foreign log joins this one. MASTER_BUILD_PROMPT 14b.7 offers both to a
 * person importing a file, and 6.3 defines what the second one means.
 *
 * Both rebuild from event zero. The difference is only what is in the log first.
 */
enum class IngestMode {
    /** The imported log becomes the log. One transaction, nothing of the old kept. */
    REPLACE,

    /** Union by event id, ordered by `(lamport, originId, id)`. Nothing is lost. */
    MERGE,
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
