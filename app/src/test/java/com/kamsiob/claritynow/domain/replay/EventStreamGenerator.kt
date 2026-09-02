package com.kamsiob.claritynow.domain.replay

import com.kamsiob.claritynow.data.event.AppOpened
import com.kamsiob.claritynow.data.event.AreaArchived
import com.kamsiob.claritynow.data.event.AreaCreated
import com.kamsiob.claritynow.data.event.AreaDeleted
import com.kamsiob.claritynow.data.event.AreaRecolored
import com.kamsiob.claritynow.data.event.AreaRenamed
import com.kamsiob.claritynow.data.event.AreaReordered
import com.kamsiob.claritynow.data.event.AreaUnarchived
import com.kamsiob.claritynow.data.event.ClarityEvent
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
import com.kamsiob.claritynow.data.event.ItemQueued
import com.kamsiob.claritynow.data.event.ItemReopened
import com.kamsiob.claritynow.data.event.ItemReordered
import com.kamsiob.claritynow.data.event.ItemStatus
import com.kamsiob.claritynow.data.event.PlanAccepted
import com.kamsiob.claritynow.data.event.PlanOffered
import com.kamsiob.claritynow.data.event.PulseAnswered
import com.kamsiob.claritynow.data.event.PulseGenerated
import com.kamsiob.claritynow.data.event.ReflectionPeriod
import com.kamsiob.claritynow.data.event.ReportGenerated
import com.kamsiob.claritynow.data.event.ReportSectionSnapshot
import com.kamsiob.claritynow.data.event.SettingChanged
import com.kamsiob.claritynow.data.event.SubjectKind
import com.kamsiob.claritynow.domain.engine.FactRef
import kotlin.random.Random

/**
 * Produces valid, varied event streams for the replay harness.
 *
 * Every event is generated against a running reduction of what has been produced
 * so far, so the stream is always something the app itself could have written:
 * real area ids, real order keys, a real demoted item on every swap. A generator
 * that emitted plausible looking nonsense would prove nothing, because the reducer
 * would spend the whole run in its diagnostics path.
 *
 * Deterministic for a given seed, and free of UUIDs, so a failing case can be
 * reproduced from its seed alone.
 *
 * **Every event type in the catalog has to be reachable from here, and a type that
 * is not is invisible to every property test in `ReplayHarnessTest`.** Determinism,
 * idempotency, divergence merge and checkpoint equivalence are all proved over
 * whatever this emits, so a type left out is a type none of those four properties
 * has ever been checked against. `the generator reaches every event type` asserts
 * the coverage rather than trusting it, which is what makes adding an operation
 * below part of adding a type rather than an afterthought.
 */
class EventStreamGenerator(
    seed: Long,
    private val originId: String,
    private var lamport: Long = 0L,
    private var wallClock: Long = START_MILLIS,
    private val idPrefix: String = originId,
) {

    private val random = Random(seed)

    /** Every key this device writes carries its own jitter, exactly as the app does. */
    private val jitter = OrderKey.jitterFor(originId)
    private var counter = 0
    var state: ClarityState = ClarityState.EMPTY
        private set

    private val emitted = mutableListOf<ClarityEvent>()

    /**
     * The local days this device has already marked as opened.
     *
     * Kept here rather than derived from [state], because the reducer deliberately
     * folds APP_OPENED into nothing at all: it is a presence marker whose only
     * reader is the gap detection in Addendum 01 4d, and storing a last opened date
     * on the projection would be a tally of someone's presence sitting in the object
     * every screen reads. MASTER_BUILD_PROMPT 5.2 has it written at most once per
     * calendar day per device, and this is what makes the generated stream obey that
     * rather than merely contain the type.
     */
    private val openedDays = mutableSetOf<String>()

    /**
     * The subject each generated Pulse was about, so its answer can carry the same
     * one.
     *
     * Kept here because `ClarityState.PulseEntryState` does not store the subject
     * even though `PulseGenerated` now carries it, so the projection is not a route
     * back to it. That is consistent with CLARITY_LOGIC_ENGINE.md 7.6, which
     * requires `FiringHistory` to derive from the events rather than from any stored
     * state, and it is why the denormalized copy on `PulseAnswered` exists at all.
     */
    private val pulseSubjects = mutableMapOf<String, String>()

    /** Forks a second device from this one, sharing history and diverging after it. */
    fun fork(seed: Long, otherOriginId: String): EventStreamGenerator {
        val fork = EventStreamGenerator(
            seed = seed,
            originId = otherOriginId,
            lamport = lamport,
            wallClock = wallClock,
            idPrefix = otherOriginId,
        )
        fork.state = state
        fork.openedDays += openedDays
        fork.pulseSubjects += pulseSubjects
        return fork
    }

    fun generate(count: Int): List<ClarityEvent> {
        repeat(count) { step() }
        return emitted.toList()
    }

    val events: List<ClarityEvent> get() = emitted.toList()

    private fun step() {
        // Areas have to exist before anything else can happen.
        val payload = if (state.liveAreas.isEmpty()) createArea() else pickOperation()
        if (payload != null) emit(payload)
        wallClock += random.nextLong(60_000L, 6 * 3_600_000L)
    }

    private fun emit(payload: EventPayload) {
        if (payload is AppOpened) openedDays += payload.dateKey
        if (payload is PulseGenerated) payload.subjectId?.let { pulseSubjects[payload.pulseId] = it }
        lamport += 1
        val event = ClarityEvent.of(
            id = nextId("evt"),
            wallClock = wallClock,
            lamport = lamport,
            originId = originId,
            payload = payload,
        )
        emitted += event
        state = ClarityReducer.apply(state, event)
    }

    private fun nextId(kind: String): String {
        counter += 1
        return "$idPrefix-$kind-$counter"
    }

    /**
     * The operation table. Weights are rough on purpose; what matters is that every
     * branch is reachable and that the common ones stay common, so a two hundred
     * event stream looks like a fortnight of use rather than a tour of the catalog.
     *
     * A branch returning null is a legal outcome and not a failure: it means the
     * state offered nothing to do, such as a filing with an empty inbox. [step]
     * simply emits nothing that turn.
     */
    private fun pickOperation(): EventPayload? = when (random.nextInt(100)) {
        in 0..5 -> if (state.liveAreas.size < 6) createArea() else addItem()
        in 6..24 -> addItem()
        in 25..29 -> captureUnfiled()
        in 30..42 -> promoteOrComplete()
        in 43..47 -> swap()
        in 48..51 -> fileItem()
        in 52..55 -> reorderItem()
        in 56..60 -> editItem()
        in 61..63 -> estimateItem()
        in 64..65 -> deleteItem()
        in 66..67 -> reopenItem()
        in 68..69 -> requeue()
        in 70..72 -> renameArea()
        73 -> recolorArea()
        74 -> reorderArea()
        in 75..77 -> archiveOrUnarchive()
        78 -> deleteArea()
        in 79..87 -> focus()
        in 88..90 -> openApp()
        in 91..94 -> pulse()
        in 95..97 -> report()
        else -> plan()
    }

    // Areas -------------------------------------------------------------------

    private fun createArea(): EventPayload {
        val existing = state.areas.values.map { it.orderKey }.maxOrNull()
        return AreaCreated(
            areaId = nextId("area"),
            name = AREA_NAMES[random.nextInt(AREA_NAMES.size)],
            colorHex = COLORS[random.nextInt(COLORS.size)],
            orderKey = if (existing == null) OrderKey.first(jitter) else OrderKey.last(existing, jitter),
        )
    }

    private fun renameArea(): EventPayload? {
        val area = state.liveAreas.randomOrNull() ?: return null
        return AreaRenamed(area.id, area.name, AREA_NAMES[random.nextInt(AREA_NAMES.size)])
    }

    private fun recolorArea(): EventPayload? {
        val area = state.liveAreas.randomOrNull() ?: return null
        return AreaRecolored(area.id, area.colorHex, COLORS[random.nextInt(COLORS.size)])
    }

    /** Moves an area among the live ones, the way a drag on the Areas screen does. */
    private fun reorderArea(): EventPayload? {
        val live = state.liveAreas
        if (live.size < 2) return null
        val moving = live[random.nextInt(live.size)]
        val others = live.filter { it.id != moving.id }
        val target = random.nextInt(others.size + 1)
        val before = others.getOrNull(target - 1)?.orderKey
        val after = others.getOrNull(target)?.orderKey
        // Archived areas keep their keys and unarchiving brings them back, so they
        // occupy this space even though `liveAreas` leaves them out. Mirrors the
        // repository's `tightenedBetween`; see OrderKeyCollisionTest.
        val occupied = state.areas.values
            .filter { it.deletedAt == null && it.id != moving.id }
            .map { it.orderKey }
            .filter { (before == null || it > before) && (after == null || it < after) }
        val low = (listOfNotNull(before) + occupied).maxOrNull()
        return AreaReordered(moving.id, moving.orderKey, OrderKey.between(low, after, jitter))
    }

    private fun archiveOrUnarchive(): EventPayload? {
        val archived = state.archivedAreas.randomOrNull()
        if (archived != null && random.nextBoolean()) {
            return AreaUnarchived(archived.id, archived.name)
        }
        // Never archive the last live area; the app reaches zero areas deliberately.
        val live = state.liveAreas
        if (live.size <= 1) return null
        val area = live.randomOrNull() ?: return null
        return AreaArchived(area.id, area.name)
    }

    private fun deleteArea(): EventPayload? {
        val area = state.archivedAreas.randomOrNull() ?: return null
        return AreaDeleted(area.id, area.name)
    }

    // Items -------------------------------------------------------------------

    private fun addItem(): EventPayload? {
        val area = state.liveAreas.randomOrNull() ?: return null
        // Live items, not the queue. See fileItem below and OrderKeyCollisionTest.
        val live = state.liveItemsIn(area.id)
        return ItemAdded(
            itemId = nextId("item"),
            areaId = area.id,
            title = ITEM_TITLES[random.nextInt(ITEM_TITLES.size)],
            note = if (random.nextInt(4) == 0) "a note" else null,
            orderKey = live.maxByOrNull { it.orderKey }
                ?.let { OrderKey.last(it.orderKey, jitter) }
                ?: OrderKey.first(jitter),
            areaNameSnapshot = area.name,
        )
    }

    /**
     * A capture into the inbox. Addendum 01 4a: no area, and none required.
     *
     * The order key comes from `unfiledItems` rather than from any queue, because
     * the inbox is its own ordered list. Sharing a key space with an area's queue
     * would make a filing land at an arbitrary position in it.
     */
    private fun captureUnfiled(): EventPayload {
        val inbox = state.unfiledItems
        return ItemAdded(
            itemId = nextId("item"),
            areaId = null,
            title = ITEM_TITLES[random.nextInt(ITEM_TITLES.size)],
            note = null,
            orderKey = if (inbox.isEmpty()) {
                OrderKey.first(jitter)
            } else {
                OrderKey.last(inbox.last().orderKey, jitter)
            },
            areaNameSnapshot = null,
            estimateMinutes = if (random.nextInt(3) == 0) ESTIMATES[random.nextInt(ESTIMATES.size)] else null,
            firstStep = if (random.nextInt(3) == 0) "Open the file and read what is there" else null,
        )
    }

    /** Moves an inbox item into an area. The only transition into one. */
    private fun fileItem(): EventPayload? {
        val item = state.unfiledItems.randomOrNull() ?: return null
        val area = state.liveAreas.randomOrNull() ?: return null
        // Every live item in the area, not just the queue. The active item shares
        // this ordering space, and taking the tail of an empty queue returns the key
        // it is already holding. See OrderKeyCollisionTest.
        val live = state.liveItemsIn(area.id)
        return ItemFiled(
            itemId = item.id,
            areaId = area.id,
            orderKey = live.maxByOrNull { it.orderKey }
                ?.let { OrderKey.last(it.orderKey, jitter) }
                ?: OrderKey.first(jitter),
            areaNameSnapshot = area.name,
        )
    }

    /**
     * Sets, changes or clears an estimate. Reachable for a filed item and for one
     * still in the inbox alike, which is the point: an estimate carries no area, so
     * nothing about it is area scoped.
     */
    private fun estimateItem(): EventPayload? {
        val item = liveItems().randomOrNull() ?: return null
        val next = if (random.nextInt(5) == 0) null else ESTIMATES[random.nextInt(ESTIMATES.size)]
        if (item.estimateMinutes == next) return null
        return ItemEstimated(item.id, item.estimateMinutes, next)
    }

    /** Promotes the queue head when an area is idle, otherwise completes what is active. */
    private fun promoteOrComplete(): EventPayload? {
        val area = state.liveAreas.randomOrNull() ?: return null
        val active = state.activeItemIn(area.id)
        if (active == null) {
            val head = state.queueIn(area.id).firstOrNull() ?: return null
            return ItemPromoted(
                itemId = head.id,
                areaId = area.id,
                previousStatus = ItemStatus.QUEUED,
                demotedItemId = null,
                demotedToOrderKey = null,
                titleSnapshot = head.title,
                areaNameSnapshot = area.name,
            )
        }
        return ItemCompleted(
            itemId = active.id,
            areaId = area.id,
            titleSnapshot = active.title,
            areaNameSnapshot = area.name,
            activeDurationDays = daysBetween(active.activeSince ?: wallClock, wallClock),
        )
    }

    /** A real swap: one event carrying both the promoted item and the demoted one. */
    private fun swap(): EventPayload? {
        val area = state.liveAreas.randomOrNull() ?: return null
        val active = state.activeItemIn(area.id) ?: return null
        val queue = state.queueIn(area.id)
        if (queue.isEmpty()) return null
        val incoming = queue[random.nextInt(queue.size)]
        return ItemPromoted(
            itemId = incoming.id,
            areaId = area.id,
            previousStatus = ItemStatus.QUEUED,
            demotedItemId = active.id,
            demotedToOrderKey = OrderKey.before(queue.first().orderKey, jitter),
            titleSnapshot = incoming.title,
            areaNameSnapshot = area.name,
        )
    }

    /**
     * Puts the active item back in the queue without promoting anything, which
     * leaves the area idle. The only thing that emits ITEM_QUEUED.
     *
     * The key goes ahead of the current head, so the item lands where it was rather
     * than at the back, and no two queued items in the area can share one.
     */
    private fun requeue(): EventPayload? {
        val area = state.liveAreas.randomOrNull() ?: return null
        val active = state.activeItemIn(area.id) ?: return null
        val queue = state.queueIn(area.id)
        // Guarded the way `ClarityReducer.freshHeadKey` is. Repeated insertion at
        // the head is the one pattern fractional indexing degrades under, and a
        // generator that threw there would fail a property test for a reason that
        // has nothing to do with the property.
        val key = runCatching {
            if (queue.isEmpty()) OrderKey.first(jitter) else OrderKey.before(queue.first().orderKey, jitter)
        }.getOrNull() ?: return null
        return ItemQueued(active.id, area.id, key, ItemStatus.ACTIVE)
    }

    private fun reorderItem(): EventPayload? {
        val area = state.liveAreas.randomOrNull() ?: return null
        val queue = state.queueIn(area.id)
        if (queue.size < 2) return null
        val moving = queue[random.nextInt(queue.size)]
        val others = queue.filter { it.id != moving.id }
        val target = random.nextInt(others.size + 1)
        val before = others.getOrNull(target - 1)?.orderKey
        val after = others.getOrNull(target)?.orderKey
        // Tightened against the active item, which holds a key in this same space and
        // is not a member of the queue, so queue neighbors alone can enclose it. The
        // repository does the same thing for the same reason; see its `keyBetween`.
        val active = state.activeItemIn(area.id)?.orderKey
            ?.takeIf { (before == null || it > before) && (after == null || it < after) }
        val low = listOfNotNull(before, active).maxOrNull()
        return ItemReordered(moving.id, area.id, moving.orderKey, OrderKey.between(low, after, jitter))
    }

    private fun editItem(): EventPayload? {
        val item = liveItems().randomOrNull() ?: return null
        return ItemEdited(
            itemId = item.id,
            previousTitle = item.title,
            newTitle = ITEM_TITLES[random.nextInt(ITEM_TITLES.size)],
            previousNote = item.note,
            newNote = if (random.nextBoolean()) "revised note" else null,
        )
    }

    /**
     * **Only a filed item, and that is a gap rather than a rule.**
     * `ITEM_DELETED` carries a non null `areaId`, so the payload cannot be built for
     * an item that has none, and `ClarityRepository.deleteItem` refuses the same
     * case for the same reason. MASTER_BUILD_PROMPT 14b.1 and Addendum 01 4a both
     * require that an inbox item be deletable, so the payload has to widen before
     * the inbox ships. Until it does, generating one here would be generating an
     * event the app cannot write.
     */
    private fun deleteItem(): EventPayload? {
        val item = liveItems()
            .filter { it.status != ItemStatus.ACTIVE && it.areaId != null }
            .randomOrNull() ?: return null
        val areaId = item.areaId ?: return null
        return ItemDeleted(item.id, areaId, item.title)
    }

    private fun reopenItem(): EventPayload? {
        val area = state.liveAreas.randomOrNull() ?: return null
        val done = state.completedIn(area.id).randomOrNull() ?: return null
        // The head of the queue, tightened against the active item's key, which
        // shares this space and is not in the queue. Mirrors the repository's
        // `keyBetween`; see OrderKeyCollisionTest.
        val upper = state.queueIn(area.id).firstOrNull()?.orderKey
        val inside = state.liveItemsIn(area.id)
            .filter { it.id != done.id }
            .map { it.orderKey }
            .filter { upper == null || it < upper }
        val target = OrderKey.between(inside.maxOrNull(), upper, jitter)
        return ItemReopened(done.id, area.id, target)
    }

    // Focus, Pulse, Report, Plans ---------------------------------------------

    /**
     * One running session at a time, extended or ended.
     *
     * An extension is not a terminal event, so it is chosen before the two that are
     * and the session carries on. The absolute new total is what the payload states,
     * because folding a delta would make the replayed plan depend on how many times
     * the event was delivered. Addendum 01 4f.
     */
    private fun focus(): EventPayload? {
        val running = state.focusSessions.values.firstOrNull { it.outcome == FocusOutcome.RUNNING }
        if (running != null) {
            if (random.nextInt(4) == 0) {
                val added = 600
                return FocusExtended(running.id, added, running.plannedSeconds + added)
            }
            // A deliberate branch rather than a draw that has to land exactly on the
            // planned duration. The old form drew a number in [30, plannedSeconds]
            // and called it complete only on an exact hit, which for a 1500 second
            // session is one turn in 1471: FOCUS_COMPLETED was unreachable in
            // practice, and every property test in this class is a proof over what
            // the generator emits, so an unreachable type is an unproved one.
            return if (random.nextInt(3) != 0) {
                FocusCompleted(running.id, running.plannedSeconds)
            } else {
                FocusEndedEarly(running.id, random.nextInt(30, running.plannedSeconds))
            }
        }
        val area = state.liveAreas.randomOrNull() ?: return null
        val active = state.activeItemIn(area.id) ?: return null
        return FocusStarted(
            sessionId = nextId("focus"),
            areaId = area.id,
            itemId = active.id,
            plannedSeconds = listOf(300, 900, 1500, 2700)[random.nextInt(4)],
        )
    }

    /**
     * The presence marker, at most once per local day. Addendum 01 2d.
     *
     * Returns null on a day already marked, rather than emitting a second one, so
     * the stream stays something the app itself could have written. A duplicate
     * would replay to the same state either way, which is exactly why it has to be
     * prevented here: nothing downstream would ever notice.
     */
    private fun openApp(): EventPayload? {
        val today = dateKeyOf(wallClock)
        if (today in openedDays) return null
        return AppOpened(today)
    }

    private fun pulse(): EventPayload {
        val unanswered = state.pulses.values.firstOrNull { !it.isAnswered }
        if (unanswered != null && random.nextBoolean()) {
            val positive = random.nextBoolean()
            // The subject is carried on the answer as well as on the question,
            // denormalized deliberately: a join through pulseId can miss on a page
            // that does not hold the origin event. See PulseAnswered's own comment.
            return PulseAnswered(
                pulseId = unanswered.id,
                responseKey = if (positive) "yes" else "no",
                responseLabel = if (positive) "Deep work" else "Stuck",
                responseIsPositive = positive,
                subjectId = pulseSubjects[unanswered.id],
                subjectKind = pulseSubjects[unanswered.id]?.let { SubjectKind.ITEM },
            )
        }
        // A family with a subject and a family without both occur, because
        // CLARITY_LOGIC_ENGINE.md 2.1's SubjectSelector NONE yields a null pair and
        // FiringHistory has to key by it either way.
        val subject = if (random.nextInt(3) == 0) null else liveItems().randomOrNull()?.id
        return PulseGenerated(
            pulseId = nextId("pulse"),
            dateKey = dateKeyOf(wallClock),
            family = "persistence",
            escalationStage = random.nextInt(1, 5),
            register = "OBSERVATIONAL",
            variantKey = "persistence.s1.${random.nextInt(20)}",
            renderedObservation = "Something held still.",
            renderedQuestion = "Deep work, or stuck?",
            factSnapshot = mapOf("activeItemAgeDays" to random.nextInt(1, 40).toString()),
            reflectionPeriod = if (random.nextBoolean()) {
                ReflectionPeriod.YESTERDAY
            } else {
                ReflectionPeriod.TODAY_SO_FAR
            },
            subjectId = subject,
            subjectKind = subject?.let { SubjectKind.ITEM },
        )
    }

    /**
     * A report carrying the keys the variant exclusion is actually stated in.
     *
     * `familyKey`, `variantKey`, `escalationStage` and `register` vary across runs
     * because CLARITY_LOGIC_ENGINE.md 7.6 step 1 filters on the variant, 7.3 cools
     * down on the family and 6.4 caps a stage, and a fixture that emitted one
     * constant for all four would replay identically whether those fields survived
     * a round trip or not.
     */
    private fun report(): EventPayload {
        val area = state.liveAreas.randomOrNull()
        val variant = random.nextInt(1, 9)
        return ReportGenerated(
            reportId = nextId("report"),
            weekStartKey = weekStartKeyOf(wallClock),
            headlineKey = "steadyPace",
            renderedSections = listOf(
                ReportSectionSnapshot(
                    sectionKey = "observations",
                    sidehead = "Your week, honestly",
                    text = "Six things left.",
                    familyKey = "intakeVsOutput",
                    variantKey = "ob.flow.s1.l%02d".format(variant),
                    escalationStage = random.nextInt(1, 4),
                    register = "PLAIN",
                    subjectId = area?.id,
                    subjectKind = area?.let { SubjectKind.AREA },
                ),
            ),
            factSnapshot = mapOf("completions" to random.nextInt(1, 12).toString()),
            headlineVariantKey = "hd.steady.%02d".format(variant),
            windowStartKey = weekStartKeyOf(wallClock - 7 * 86_400_000L),
            headlineText = "A steady week.",
        )
    }

    private fun plan(): EventPayload {
        val open = state.plans.values.firstOrNull { !it.isAccepted }
        if (open != null && random.nextBoolean()) return PlanAccepted(open.id)
        if (random.nextInt(6) == 0) {
            return SettingChanged("afterCompleting", "AUTO_PROMOTE", "CHOOSE_FROM_QUEUE")
        }
        return PlanOffered(
            planId = nextId("plan"),
            weekStartKey = weekStartKeyOf(wallClock),
            frameKey = "frm.01",
            cueKey = "cue.band.01",
            actionKey = "act.neg.01",
            familyKey = "neglectedArea",
            subjectId = state.liveAreas.randomOrNull()?.id,
            offeredLine = "One option for Wednesday morning.",
            committedLine = "If it is Wednesday morning, I will start there.",
            resolutionFactRef = FactRef("area", "eventsInWindow"),
        )
    }

    // Helpers -----------------------------------------------------------------

    private fun liveItems(): List<ItemState> = state.items.values
        .filter { it.deletedAt == null }
        .sortedBy { it.id }

    private fun <T> List<T>.randomOrNull(): T? = if (isEmpty()) null else this[random.nextInt(size)]

    private fun daysBetween(from: Long, to: Long): Int =
        ((to - from) / 86_400_000L).toInt().coerceAtLeast(0)

    private fun dateKeyOf(millis: Long): String {
        val day = millis / 86_400_000L
        return "day-$day"
    }

    private fun weekStartKeyOf(millis: Long): String {
        val week = millis / (7 * 86_400_000L)
        return "week-$week"
    }

    companion object {
        const val START_MILLIS = 1_735_689_600_000L // 2025-01-01T00:00:00Z

        val ESTIMATES = listOf(15, 30, 45, 60, 90, 120)
        val AREA_NAMES = listOf("Work", "Personal", "Health", "Family", "Learning", "Side Project")
        val COLORS = listOf("#2D7FF9", "#22C55E", "#F59E0B", "#6366F1", "#EC4899", "#0D9488")
        val ITEM_TITLES = listOf(
            "Rewrite the proposal intro",
            "Call the printer",
            "Book the dentist",
            "Draft the release notes",
            "Clear the inbox",
            "Plan the week",
            "Fix the leaking tap",
            "Read one chapter",
        )
    }
}
