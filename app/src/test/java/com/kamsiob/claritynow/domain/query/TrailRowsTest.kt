package com.kamsiob.claritynow.domain.query

import com.kamsiob.claritynow.data.event.AreaArchived
import com.kamsiob.claritynow.data.event.AreaCreated
import com.kamsiob.claritynow.data.event.AreaDeleted
import com.kamsiob.claritynow.data.event.AreaRecolored
import com.kamsiob.claritynow.data.event.AreaRenamed
import com.kamsiob.claritynow.data.event.AreaReordered
import com.kamsiob.claritynow.data.event.AreaUnarchived
import com.kamsiob.claritynow.data.event.ClarityEvent
import com.kamsiob.claritynow.data.event.ClarityEventType
import com.kamsiob.claritynow.data.event.EventPayload
import com.kamsiob.claritynow.data.event.FocusAbandoned
import com.kamsiob.claritynow.data.event.FocusCompleted
import com.kamsiob.claritynow.data.event.FocusStarted
import com.kamsiob.claritynow.data.event.ItemAdded
import com.kamsiob.claritynow.data.event.ItemCompleted
import com.kamsiob.claritynow.data.event.ItemDeleted
import com.kamsiob.claritynow.data.event.ItemEdited
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
import com.kamsiob.claritynow.domain.engine.FactRef
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The mapping from one event to one Trail row. MASTER_BUILD_PROMPT 9, design-v3.md 11.
 *
 * Two things are being protected here. The first is that every event type renders:
 * a Phase 3 build writes only the area and item families, and the rest arrive from a
 * merged or imported log long before their own phase lands, so a type with no row
 * shape would be invisible in a transcript that claims to be complete. The second is
 * that a row's words come from the log rather than from the current state, which is
 * the difference between a transcript and a screen that quietly rewrites its own
 * history whenever anything is renamed.
 *
 * The clustering rule is tested here rather than on the screen because it is
 * arithmetic over instants and a local calendar, and it is wrong only across a day
 * boundary, which is not a thing anyone notices by looking.
 */
class TrailRowsTest {

    private val blank = TrailRowContext(
        areaId = null,
        areaColorHex = null,
        itemTitle = null,
        areaName = null,
    )

    /** One payload of every type in the catalog, on the `EventFormatTest` precedent. */
    private val everyPayload: Map<ClarityEventType, EventPayload> = mapOf(
        ClarityEventType.AREA_CREATED to AreaCreated("area-1", "Work", "#2D7FF9", "a0"),
        ClarityEventType.AREA_RENAMED to AreaRenamed("area-1", "Work", "Studio"),
        ClarityEventType.AREA_RECOLORED to AreaRecolored("area-1", "#2D7FF9", "#22C55E"),
        ClarityEventType.AREA_REORDERED to AreaReordered("area-1", "a0", "a1"),
        ClarityEventType.AREA_ARCHIVED to AreaArchived("area-1", "Work"),
        ClarityEventType.AREA_UNARCHIVED to AreaUnarchived("area-1", "Work"),
        ClarityEventType.AREA_DELETED to AreaDeleted("area-1", "Work"),
        ClarityEventType.ITEM_ADDED to ItemAdded("item-1", "area-1", "Call the printer", null, "a0", "Work"),
        ClarityEventType.ITEM_EDITED to ItemEdited("item-1", "Call", "Call the printer", null, null),
        ClarityEventType.ITEM_QUEUED to ItemQueued("item-1", "area-1", "a1", ItemStatus.ACTIVE),
        ClarityEventType.ITEM_PROMOTED to promotion(demotedItemId = null),
        ClarityEventType.ITEM_COMPLETED to ItemCompleted("item-1", "area-1", "Call the printer", "Work", 3),
        ClarityEventType.ITEM_REOPENED to ItemReopened("item-1", "area-1", "a0"),
        ClarityEventType.ITEM_REORDERED to ItemReordered("item-1", "area-1", "a0", "a2"),
        ClarityEventType.ITEM_DELETED to ItemDeleted("item-1", "area-1", "Call the printer"),
        ClarityEventType.FOCUS_STARTED to FocusStarted("focus-1", "area-1", "item-1", 1500),
        ClarityEventType.FOCUS_COMPLETED to FocusCompleted("focus-1", 1500),
        ClarityEventType.FOCUS_ABANDONED to FocusAbandoned("focus-1", 240),
        ClarityEventType.PULSE_GENERATED to pulse(),
        ClarityEventType.PULSE_ANSWERED to PulseAnswered("pulse-1", "deep", "Deep work", true),
        ClarityEventType.REPORT_GENERATED to report(),
        ClarityEventType.PLAN_OFFERED to plan(),
        ClarityEventType.PLAN_ACCEPTED to PlanAccepted("plan-1"),
        ClarityEventType.SETTING_CHANGED to SettingChanged("afterCompleting", "AUTO_PROMOTE", "CHOOSE_FROM_QUEUE"),
    )

    // Coverage ----------------------------------------------------------------

    @Test
    fun `every event type produces a row`() {
        val missing = ClarityEventType.entries - everyPayload.keys
        assertTrue("no fixture for: ${missing.joinToString()}", missing.isEmpty())

        val produced = everyPayload.entries.map { (type, payload) ->
            val built = event(payload)
            assertEquals(type.name, type, built.type)
            trailRowFor(built, blank).sentence
        }.toSet()
        // ITEM_PROMOTED is the one type with two row shapes, and only the payload
        // tells them apart. Twenty four types, twenty five shapes, and the day a
        // twenty fifth type is added this fails rather than rendering it as nothing.
        val covered = produced + trailRowFor(event(promotion("item-2")), blank).sentence
        val uncovered = TrailSentenceKey.entries.toSet() - covered
        assertTrue("no event produces: ${uncovered.joinToString()}", uncovered.isEmpty())
        assertEquals(TrailSentenceKey.entries.size, covered.size)
    }

    @Test
    fun `a promotion with a demoted item reads as a swap`() {
        val plain = trailRowFor(event(promotion(demotedItemId = null)), blank)
        val swap = trailRowFor(event(promotion(demotedItemId = "item-2")), blank)

        assertEquals(TrailSentenceKey.ITEM_PROMOTED, plain.sentence)
        assertEquals(TrailSentenceKey.ITEM_SWAPPED, swap.sentence)
        // A swap names only the item that moved to the front. `demotedItemId` carries
        // no title snapshot, so naming both would need a second resolution for a fact
        // the person already saw at the moment they swapped.
        assertEquals("Rewrite the proposal intro", swap.subject)
        assertNull(swap.secondary)
    }

    @Test
    fun `only a completion is marked as a completion`() {
        everyPayload.forEach { (type, payload) ->
            val row = trailRowFor(event(payload), blank)
            assertEquals(type.name, type == ClarityEventType.ITEM_COMPLETED, row.isCompletion)
        }
    }

    // Snapshots ---------------------------------------------------------------

    @Test
    fun `a row never carries a live entity name`() {
        val log = TrailTestLog()
        log.area(at(0, 9), "area-work", "Work", colorHex = "#2D7FF9")
        log.item(at(0, 9), "item-a", "area-work", "Call the printer")
        log.promote(at(0, 9), "item-a", "area-work", "Call the printer")
        // Two rows whose payloads carry no snapshot of their own, so both have to be
        // resolved by folding the log back to the instant they happened.
        val queued = log.add(at(1, 9), ItemQueued("item-a", "area-work", "a0", ItemStatus.ACTIVE))
        val reordered = log.add(at(1, 10), AreaReordered("area-work", "a0", "a1"))
        // Everything is renamed and recolored afterwards.
        log.add(
            at(5, 9),
            ItemEdited("item-a", "Call the printer", "Call the printer about the covers", null, null),
        )
        log.add(at(5, 9), AreaRenamed("area-work", "Work", "Studio"))
        log.add(at(5, 9), AreaRecolored("area-work", "#2D7FF9", "#22C55E"))

        val rows = log.queries().rows(log.events()).associateBy { it.eventId }
        assertEquals("Call the printer", rows.getValue(queued.id).subject)
        assertEquals("Work", rows.getValue(reordered.id).subject)
        assertEquals("#2D7FF9", rows.getValue(queued.id).areaColorHex)
        assertEquals("area-work", rows.getValue(queued.id).areaId)
    }

    @Test
    fun `a recolor row takes its color from its own payload`() {
        val log = TrailTestLog()
        log.area(at(0, 9), "area-work", "Work", colorHex = "#2D7FF9")
        val recolored = log.add(at(3, 9), AreaRecolored("area-work", "#2D7FF9", "#22C55E"))

        val row = log.queries().rows(log.events()).single { it.eventId == recolored.id }
        // The one place a payload beats the fold. Two recolors sharing a wall clock
        // would otherwise resolve to whichever sorted last, and this event states its
        // own answer.
        assertEquals("#22C55E", row.areaColorHex)
        assertEquals("Work", row.subject)
    }

    @Test
    fun `a rename row names both sides out of its own payload`() {
        val row = trailRowFor(event(AreaRenamed("area-1", "Work", "Studio")), blank)
        assertEquals(TrailSentenceKey.AREA_RENAMED, row.sentence)
        assertEquals("Work", row.subject)
        assertEquals("Studio", row.secondary)
    }

    @Test
    fun `an event with no area carries a null area id and a null color`() {
        val log = TrailTestLog()
        // A real area exists throughout, so an implementation that reached for the
        // only area it could find would pass everything else and fail here.
        log.area(at(0, 9), "area-work", "Work")
        val unresolvable = listOf(
            log.add(at(1, 9), pulse()),
            log.add(at(1, 10), PulseAnswered("pulse-1", "yes", "Still the right thing", true)),
            log.add(at(2, 8), report()),
            log.add(at(2, 8), plan()),
            log.add(at(2, 9), PlanAccepted("plan-1")),
            log.add(at(3, 9), SettingChanged("afterCompleting", "AUTO_PROMOTE", "CHOOSE_FROM_QUEUE")),
        )

        val rows = log.queries().rows(log.events()).associateBy { it.eventId }
        unresolvable.forEach { unresolved ->
            val row = rows.getValue(unresolved.id)
            // PLAN_OFFERED carries a subjectId that happens to be an area id, and no
            // subjectKind that would say so. Testing the id against the known areas
            // would work in practice and is a heuristic no document authorizes.
            assertNull(unresolved.type.name, row.areaId)
            assertNull(unresolved.type.name, row.areaColorHex)
        }
    }

    @Test
    fun `a focus row with no session start renders with no subject rather than crashing`() {
        val log = TrailTestLog()
        log.area(at(0, 9), "area-work", "Work")
        val orphan = log.add(at(1, 15), FocusCompleted("focus-orphan", 900))

        val row = log.queries().rows(log.events()).single { it.eventId == orphan.id }
        assertEquals(TrailSentenceKey.FOCUS_COMPLETED, row.sentence)
        assertNull(row.subject)
        assertNull(row.areaId)
        assertEquals(15, row.minutes)
    }

    @Test
    fun `focus minutes on a row round to the nearest minute`() {
        // MASTER_BUILD_PROMPT 10 discards a session under sixty seconds as an
        // abandonment, so a zero minute row is legal and honest rather than a defect.
        assertEquals(1, trailRowFor(event(FocusAbandoned("focus-1", 89)), blank).minutes)
        assertEquals(2, trailRowFor(event(FocusAbandoned("focus-1", 90)), blank).minutes)
        assertEquals(0, trailRowFor(event(FocusAbandoned("focus-1", 29)), blank).minutes)
        assertNull(trailRowFor(event(FocusStarted("focus-1", "area-1", "item-1", 1500)), blank).minutes)
    }

    // Clustering --------------------------------------------------------------

    @Test
    fun `the first event of a ten minute cluster shows a timestamp and the rest do not`() {
        val log = TrailTestLog()
        log.area(at(0, 9), "area-work", "Work")
        log.item(at(0, 10, 0), "item-a", "area-work", "Call the printer")
        log.item(at(0, 10, 3), "item-b", "area-work", "Draft the release notes", orderKey = "a1")
        log.item(at(0, 10, 9), "item-c", "area-work", "Rewrite the intro", orderKey = "a2")
        log.item(at(0, 10, 12), "item-d", "area-work", "Book the dentist", orderKey = "a3")

        val rows = trailRows(log.events().drop(1), TEST_ZONE) { blank }
        // Newest first, so the anchor is the ten twelve row. Everything within ten
        // minutes of the anchor joins it, and the ten o'clock row is twelve minutes
        // away and starts a cluster of its own. The anchor rather than the previous
        // row is what "the first event of each cluster" means: measured against the
        // previous row, a slow steady stream would print one time and then nothing
        // for an hour.
        assertEquals(listOf(true, false, false, true), rows.map { it.showsTimestamp })
    }

    @Test
    fun `a cluster does not span a day boundary`() {
        val log = TrailTestLog()
        log.area(at(0, 9), "area-work", "Work")
        log.item(at(0, 23, 58), "item-a", "area-work", "Call the printer")
        log.item(at(1, 0, 3), "item-b", "area-work", "Draft the release notes", orderKey = "a1")

        val rows = trailRows(log.events().drop(1), TEST_ZONE) { blank }
        // Five minutes apart and in two different day groups. The second inheriting a
        // suppressed timestamp would leave the top row of a day with no time on it.
        assertEquals(listOf(true, true), rows.map { it.showsTimestamp })
    }

    @Test
    fun `two events written in one commit share a wall clock and print one timestamp`() {
        val commit = oneCommit()
        val rows = trailRows(commit, TEST_ZONE) { blank }

        assertEquals(rows[0].wallClock, rows[1].wallClock)
        assertEquals(listOf(true, false), rows.map { it.showsTimestamp })
    }

    @Test
    fun `a completion and the promotion it caused print promotion first`() {
        val rows = trailRows(oneCommit(), TEST_ZONE) { blank }

        // ClarityRepository reads the clock once per commit and stamps every event it
        // writes with that one reading, so wall clock alone cannot order these two.
        // Without the lamport tiebreak the promotion prints below the completion that
        // caused it, which reads as the app having done things in the wrong order.
        assertEquals(TrailSentenceKey.ITEM_PROMOTED, rows[0].sentence)
        assertEquals(TrailSentenceKey.ITEM_COMPLETED, rows[1].sentence)
        assertEquals(rows[0].wallClock, rows[1].wallClock)
    }

    // Fixtures ----------------------------------------------------------------

    /** A completion and the promotion it caused, one commit, one clock reading. */
    private fun oneCommit(): List<ClarityEvent> {
        val log = TrailTestLog()
        log.area(at(0, 9), "area-work", "Work")
        log.item(at(0, 9), "item-a", "area-work", "Call the printer")
        log.item(at(0, 9), "item-b", "area-work", "Draft the release notes", orderKey = "a1")
        log.promote(at(0, 9), "item-a", "area-work", "Call the printer")
        val instant = at(4, 17)
        val completion = log.complete(instant, "item-a", "area-work", "Call the printer")
        val promotion = log.promote(instant, "item-b", "area-work", "Draft the release notes")
        return listOf(completion, promotion)
    }

    private var sequence = 0

    private fun event(payload: EventPayload): ClarityEvent {
        sequence += 1
        return ClarityEvent.of(
            id = "evt-$sequence",
            wallClock = at(0, 9),
            lamport = sequence.toLong(),
            originId = TEST_ORIGIN,
            payload = payload,
        )
    }

    private fun promotion(demotedItemId: String?): ItemPromoted = ItemPromoted(
        itemId = "item-1",
        areaId = "area-1",
        previousStatus = ItemStatus.QUEUED,
        demotedItemId = demotedItemId,
        demotedToOrderKey = if (demotedItemId == null) null else "Zz",
        titleSnapshot = "Rewrite the proposal intro",
        areaNameSnapshot = "Work",
    )

    private fun pulse(): PulseGenerated = PulseGenerated(
        pulseId = "pulse-1",
        dateKey = "2026-01-05",
        family = "persistence",
        escalationStage = 2,
        register = "REFLECTIVE",
        variantKey = "persistence.s2.11",
        renderedObservation = "Still Rewrite the proposal intro. Nine days now.",
        renderedQuestion = "Deep work, or stuck?",
        factSnapshot = mapOf("activeItemAgeDays" to "9"),
        reflectionPeriod = ReflectionPeriod.YESTERDAY,
    )

    private fun report(): ReportGenerated = ReportGenerated(
        reportId = "report-1",
        weekStartKey = "2026-01-04",
        headlineKey = "steadyPace",
        renderedSections = listOf(
            ReportSectionSnapshot("observations", "Your week, honestly", "Six things left."),
        ),
        factSnapshot = mapOf("completions" to "6"),
    )

    private fun plan(): PlanOffered = PlanOffered(
        planId = "plan-1",
        weekStartKey = "2026-01-04",
        frameKey = "frm.01",
        cueKey = "cue.band.01",
        actionKey = "act.neg.01",
        familyKey = "neglectedArea",
        subjectId = "area-work",
        offeredLine = "One option for Wednesday morning.",
        committedLine = "If it is Wednesday morning, I will start in Personal.",
        resolutionFactRef = FactRef("area", "eventsInWindow"),
    )
}
