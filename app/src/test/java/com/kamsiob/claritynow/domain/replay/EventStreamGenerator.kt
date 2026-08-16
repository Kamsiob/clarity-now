package com.kamsiob.claritynow.domain.replay

import com.kamsiob.claritynow.data.event.AreaArchived
import com.kamsiob.claritynow.data.event.AreaCreated
import com.kamsiob.claritynow.data.event.AreaDeleted
import com.kamsiob.claritynow.data.event.AreaRecolored
import com.kamsiob.claritynow.data.event.AreaRenamed
import com.kamsiob.claritynow.data.event.AreaUnarchived
import com.kamsiob.claritynow.data.event.ClarityEvent
import com.kamsiob.claritynow.data.event.EventPayload
import com.kamsiob.claritynow.data.event.FocusAbandoned
import com.kamsiob.claritynow.data.event.FocusCompleted
import com.kamsiob.claritynow.data.event.FocusStarted
import com.kamsiob.claritynow.data.event.ItemAdded
import com.kamsiob.claritynow.data.event.ItemCompleted
import com.kamsiob.claritynow.data.event.ItemDeleted
import com.kamsiob.claritynow.data.event.ItemEdited
import com.kamsiob.claritynow.data.event.ItemPromoted
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

    private fun pickOperation(): EventPayload? = when (random.nextInt(100)) {
        in 0..6 -> if (state.liveAreas.size < 6) createArea() else addItem()
        in 7..29 -> addItem()
        in 30..44 -> promoteOrComplete()
        in 45..52 -> swap()
        in 53..59 -> reorderItem()
        in 60..65 -> editItem()
        in 66..69 -> deleteItem()
        in 70..72 -> reopenItem()
        in 73..75 -> renameArea()
        in 76..77 -> recolorArea()
        in 78..79 -> archiveOrUnarchive()
        80 -> deleteArea()
        in 81..89 -> focus()
        in 90..94 -> pulse()
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
        val queue = state.queueIn(area.id)
        return ItemAdded(
            itemId = nextId("item"),
            areaId = area.id,
            title = ITEM_TITLES[random.nextInt(ITEM_TITLES.size)],
            note = if (random.nextInt(4) == 0) "a note" else null,
            orderKey = if (queue.isEmpty()) OrderKey.first(jitter) else OrderKey.last(queue.last().orderKey, jitter),
            areaNameSnapshot = area.name,
        )
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

    private fun reorderItem(): EventPayload? {
        val area = state.liveAreas.randomOrNull() ?: return null
        val queue = state.queueIn(area.id)
        if (queue.size < 2) return null
        val moving = queue[random.nextInt(queue.size)]
        val others = queue.filter { it.id != moving.id }
        val target = random.nextInt(others.size + 1)
        val before = others.getOrNull(target - 1)?.orderKey
        val after = others.getOrNull(target)?.orderKey
        return ItemReordered(moving.id, area.id, moving.orderKey, OrderKey.between(before, after, jitter))
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

    private fun deleteItem(): EventPayload? {
        val item = liveItems().filter { it.status != ItemStatus.ACTIVE }.randomOrNull() ?: return null
        return ItemDeleted(item.id, item.areaId, item.title)
    }

    private fun reopenItem(): EventPayload? {
        val area = state.liveAreas.randomOrNull() ?: return null
        val done = state.completedIn(area.id).randomOrNull() ?: return null
        val queue = state.queueIn(area.id)
        val target = if (queue.isEmpty()) OrderKey.first(jitter) else OrderKey.before(queue.first().orderKey, jitter)
        return ItemReopened(done.id, area.id, target)
    }

    // Focus, Pulse, Report, Plans ---------------------------------------------

    private fun focus(): EventPayload? {
        val running = state.focusSessions.values.firstOrNull { it.outcome == FocusOutcome.RUNNING }
        if (running != null) {
            val seconds = random.nextInt(30, running.plannedSeconds + 1)
            return if (seconds >= running.plannedSeconds) {
                FocusCompleted(running.id, running.plannedSeconds)
            } else {
                FocusAbandoned(running.id, seconds)
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

    private fun pulse(): EventPayload {
        val unanswered = state.pulses.values.firstOrNull { !it.isAnswered }
        if (unanswered != null && random.nextBoolean()) {
            val positive = random.nextBoolean()
            return PulseAnswered(
                pulseId = unanswered.id,
                responseKey = if (positive) "yes" else "no",
                responseLabel = if (positive) "Deep work" else "Stuck",
                responseIsPositive = positive,
            )
        }
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
        )
    }

    private fun report(): EventPayload = ReportGenerated(
        reportId = nextId("report"),
        weekStartKey = weekStartKeyOf(wallClock),
        headlineKey = "steadyPace",
        renderedSections = listOf(
            ReportSectionSnapshot("observations", "Your week, honestly", "Six things left."),
        ),
        factSnapshot = mapOf("completions" to random.nextInt(1, 12).toString()),
    )

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
