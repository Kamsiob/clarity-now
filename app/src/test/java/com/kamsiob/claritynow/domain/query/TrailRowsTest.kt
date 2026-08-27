package com.kamsiob.claritynow.domain.query

import com.kamsiob.claritynow.data.event.AppOpened
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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
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
        ClarityEventType.ITEM_FILED to ItemFiled("item-1", "area-1", "a3", "Work"),
        ClarityEventType.ITEM_EDITED to ItemEdited("item-1", "Call", "Call the printer", null, null),
        ClarityEventType.ITEM_ESTIMATED to ItemEstimated("item-1", 20, 45),
        ClarityEventType.ITEM_QUEUED to ItemQueued("item-1", "area-1", "a1", ItemStatus.ACTIVE),
        ClarityEventType.ITEM_PROMOTED to promotion(demotedItemId = null),
        ClarityEventType.ITEM_COMPLETED to ItemCompleted("item-1", "area-1", "Call the printer", "Work", 3),
        ClarityEventType.ITEM_REOPENED to ItemReopened("item-1", "area-1", "a0"),
        ClarityEventType.ITEM_REORDERED to ItemReordered("item-1", "area-1", "a0", "a2"),
        ClarityEventType.ITEM_DELETED to ItemDeleted("item-1", "area-1", "Call the printer"),
        ClarityEventType.FOCUS_STARTED to FocusStarted("focus-1", "area-1", "item-1", 1500),
        ClarityEventType.FOCUS_COMPLETED to FocusCompleted("focus-1", 1500),
        ClarityEventType.FOCUS_ENDED_EARLY to FocusEndedEarly("focus-1", 240),
        ClarityEventType.FOCUS_EXTENDED to FocusExtended("focus-1", 600, 2100),
        ClarityEventType.PULSE_GENERATED to pulse(),
        ClarityEventType.PULSE_ANSWERED to PulseAnswered("pulse-1", "deep", "Deep work", true),
        ClarityEventType.REPORT_GENERATED to report(),
        ClarityEventType.PLAN_OFFERED to plan(),
        ClarityEventType.PLAN_ACCEPTED to PlanAccepted("plan-1"),
        ClarityEventType.SETTING_CHANGED to SettingChanged("afterCompleting", "AUTO_PROMOTE", "CHOOSE_FROM_QUEUE"),
        ClarityEventType.APP_OPENED to AppOpened("2026-01-05"),
    )

    // Coverage ----------------------------------------------------------------

    @Test
    fun `every event type either produces a row or deliberately produces none`() {
        val missing = ClarityEventType.entries - everyPayload.keys
        assertTrue("no fixture for: ${missing.joinToString()}", missing.isEmpty())

        val produced = everyPayload.entries.mapNotNull { (type, payload) ->
            val built = event(payload)
            assertEquals(type.name, type, built.type)
            trailRowFor(built, blank)?.sentence
        }.toSet()
        // Two types carry two row shapes each, and in both cases only the payload
        // tells them apart: a promotion that displaced something reads as a swap, and
        // an estimate set to null reads as a removal rather than as a setting. One
        // type, APP_OPENED, runs the other way and has no row shape at all.
        //
        // So the two catalogs do not match and are not supposed to: twenty eight
        // event types produce twenty nine sentence keys. The arithmetic is asserted
        // rather than the equality, so that adding a type, adding a variant, or
        // adding a type that renders nothing each fail here with the reason visible.
        val covered = produced +
            rowFor(promotion("item-2")).sentence +
            rowFor(ItemEstimated("item-1", 45, null)).sentence
        val uncovered = TrailSentenceKey.entries.toSet() - covered
        assertTrue("no event produces: ${uncovered.joinToString()}", uncovered.isEmpty())
        assertEquals(TrailSentenceKey.entries.size, covered.size)

        val typesWithTwoShapes = 2
        val typesWithNoShape = 1
        assertEquals(
            "sentence keys should be the event types, plus one for each type that " +
                "carries a second row shape, minus one for each type that renders " +
                "nothing. If this moved, work out which of those three changed " +
                "before adjusting the numbers.",
            ClarityEventType.entries.size + typesWithTwoShapes - typesWithNoShape,
            TrailSentenceKey.entries.size,
        )
    }

    /**
     * APP_OPENED is the one type that prints nothing, and it is a rule rather than
     * an omission. MASTER_BUILD_PROMPT 5.2 and 9, DECISIONS.md C7.
     *
     * A daily "opened the app" line would be noise in a chronological log, and it
     * would also be a running tally of a person's presence, which is Addendum 01
     * 4d's prohibition turned inside out: an event added in order to detect an
     * absence must never become the thing that displays a presence. The day header
     * counts rows, so a type that prints no row is absent from that number too, and
     * this checks both halves rather than only the visible one.
     */
    @Test
    fun `an app open prints no row and is not counted in the day it happened on`() {
        assertNull(trailRowFor(event(AppOpened("2026-01-05")), blank))

        val log = TrailTestLog()
        log.area(at(0, 9), "area-work", "Work")
        log.opened(1)
        val added = log.item(at(1, 10), "item-a", "area-work", "Call the printer")

        val rows = log.queries().rows(log.events())
        // Three events, two rows. The day of the app open holds exactly one.
        assertEquals(3, log.events().size)
        assertEquals(2, rows.size)
        assertEquals(
            listOf(added.id),
            rows.filter { it.wallClock >= startOfDay(1) }.map { it.eventId },
        )
        // Exactly one type in the whole catalog is allowed to do this.
        val silent = everyPayload.filterValues { trailRowFor(event(it), blank) == null }.keys
        assertEquals(setOf(ClarityEventType.APP_OPENED), silent)
    }

    /**
     * An event that prints no row cannot anchor a timestamp cluster.
     *
     * Clusters are anchored on their newest member, so this bites when an
     * APP_OPENED is newer than a real event within ten minutes of it. That is not a
     * contrived shape. It happens on one device whenever the app was already in the
     * foreground when the local day turned over: the person does something at four
     * minutes past midnight, backgrounds the app, comes back at nine past, and only
     * then is the new day's first foreground recorded. It also happens on any merged
     * log, where a second device's app open lands after the first device's work.
     *
     * If an invisible event anchored the cluster, the newest row a person can
     * actually see would inherit a suppressed timestamp from a row that is not on
     * the screen, and the top of that group would carry no time at all. The anchor
     * is therefore advanced only once a row has been produced.
     */
    @Test
    fun `an event that prints no row cannot anchor a timestamp cluster`() {
        val log = TrailTestLog()
        log.area(at(-1, 9), "area-work", "Work")
        val early = log.item(at(0, 0, 4), "item-a", "area-work", "Call the printer")
        // Five minutes later, and newer than the only row of that day. Written with
        // the payload directly because the helper takes an hour and this needs a
        // minute; it is still one app open on one calendar day.
        log.add(at(0, 0, 9), AppOpened(dateKey(0)))

        val rows = trailRows(log.events().drop(1), TEST_ZONE) { blank }
        assertEquals(listOf(early.id), rows.map { it.eventId })
        assertTrue(
            "the newest visible row of a day must carry its own time. An app open " +
                "five minutes later is not on the screen and must not have taken it.",
            rows.single().showsTimestamp,
        )
    }

    /**
     * And the ordinary case, where the app open is the oldest event of its day,
     * changes nothing about the rows above it.
     */
    @Test
    fun `an app open before the day's work leaves the clustering alone`() {
        val log = TrailTestLog()
        log.area(at(-1, 9), "area-work", "Work")
        log.opened(0, hour = 9)
        log.item(at(0, 9, 1), "item-a", "area-work", "Call the printer")
        log.item(at(0, 9, 3), "item-b", "area-work", "Draft the release notes", orderKey = "a1")

        val rows = trailRows(log.events().drop(1), TEST_ZONE) { blank }
        assertEquals(2, rows.size)
        // Newest first. The nine oh three row anchors and prints its time; the nine
        // oh one row joins it.
        assertEquals(listOf(true, false), rows.map { it.showsTimestamp })
    }

    @Test
    fun `a promotion with a demoted item reads as a swap`() {
        val plain = rowFor(promotion(demotedItemId = null))
        val swap = rowFor(promotion(demotedItemId = "item-2"))

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
            val row = trailRowFor(event(payload), blank) ?: return@forEach
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
        val row = rowFor(AreaRenamed("area-1", "Work", "Studio"))
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
        // MASTER_BUILD_PROMPT 10 discards a session under sixty seconds rather than
        // recording it as a completion, so a zero minute row is legal and honest
        // rather than a defect to round away.
        assertEquals(1, rowFor(FocusEndedEarly("focus-1", 89)).minutes)
        assertEquals(2, rowFor(FocusEndedEarly("focus-1", 90)).minutes)
        assertEquals(0, rowFor(FocusEndedEarly("focus-1", 29)).minutes)
        assertNull(rowFor(FocusStarted("focus-1", "area-1", "item-1", 1500)).minutes)
    }

    /**
     * An extension row reads what was added, not the new total. Addendum 01 4f.
     *
     * The row records what the person did, and what they did was add ten minutes.
     * The total is a state the focus screen was already showing them, and a row
     * carrying it would put the same number on screen from two paths, computed at
     * two different moments.
     */
    @Test
    fun `an extension row reads the time added rather than the new total`() {
        val row = rowFor(FocusExtended("focus-1", 600, 2100))
        assertEquals(TrailSentenceKey.FOCUS_EXTENDED, row.sentence)
        assertEquals(10, row.minutes)
    }

    /**
     * An estimate row names the item and never the number.
     *
     * Addendum 01 7a forbids any rendered sentence stating a delta between an
     * estimate and an actual, and it is a correctness rule rather than a matter of
     * tone. A row reading "Estimated 30 minutes" sitting a few lines above one
     * reading "Finished 50 minutes of focus" invites the reader to do exactly the
     * subtraction the corpus is forbidden from doing, in a surface that is meant to
     * be a transcript rather than an observation. Same reasoning as
     * `activeDurationDays` being absent from a completion row.
     */
    @Test
    fun `an estimate row carries no minutes at all`() {
        val row = rowFor(ItemEstimated("item-1", 20, 45))
        assertEquals(TrailSentenceKey.ITEM_ESTIMATED, row.sentence)
        assertNull(row.minutes)
        assertNull(row.secondary)
    }

    /**
     * A filing names the item and the area it went into, and takes each from the
     * right place.
     *
     * `areaNameSnapshot` is on the payload for exactly this, per
     * MASTER_BUILD_PROMPT 5.2, so a filing from three months ago still names the
     * area it went into after that area has been renamed. The title is not on the
     * payload and is folded, because carrying it would be a second copy of a value
     * that already has one home. This checks that both halves survive a later
     * rename of the area and a later edit of the item.
     */
    @Test
    fun `a filing row names the item by folding and the area from its own payload`() {
        val log = TrailTestLog()
        log.area(at(0, 9), "area-work", "Work")
        log.unfiled(at(0, 21), "item-idea", "Look into the loft insulation")
        val filed = log.file(at(3, 9), "item-idea", "area-work", orderKey = "a1", areaName = "Work")
        log.add(at(9, 9), AreaRenamed("area-work", "Work", "Studio"))
        log.add(
            at(9, 9),
            ItemEdited("item-idea", "Look into the loft insulation", "Price the insulation", null, null),
        )

        val row = log.queries().rows(log.events()).single { it.eventId == filed.id }
        assertEquals(TrailSentenceKey.ITEM_FILED, row.sentence)
        assertEquals("Look into the loft insulation", row.subject)
        assertEquals("Work", row.secondary)
        assertEquals("area-work", row.areaId)
        assertNotNull(row.areaColorHex)
    }

    /**
     * Everything done to an item while it sat in the inbox belongs to no area.
     *
     * A row with no area gets no tint, which is the honest rendering: the event did
     * not happen in an area, and borrowing the color of the area the item was filed
     * into a week later would color the past with a decision made afterward.
     */
    @Test
    fun `a row for an unfiled item carries no area and no tint`() {
        val log = TrailTestLog()
        log.area(at(0, 9), "area-work", "Work")
        val captured = log.unfiled(at(1, 21), "item-idea", "Look into the loft insulation")
        val estimated = log.estimate(at(1, 21), "item-idea", null, 90)
        log.file(at(5, 9), "item-idea", "area-work", orderKey = "a1")

        val rows = log.queries().rows(log.events()).associateBy { it.eventId }
        listOf(captured, estimated).forEach { happened ->
            val row = rows.getValue(happened.id)
            assertNull(happened.type.name, row.areaId)
            assertNull(happened.type.name, row.areaColorHex)
        }
        // The capture still names what was captured. Nothing about it is missing.
        assertEquals("Look into the loft insulation", rows.getValue(captured.id).subject)
        assertEquals("Look into the loft insulation", rows.getValue(estimated.id).subject)
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

    /**
     * The row for a payload, asserting that it produced one.
     *
     * `trailRowFor` is nullable because exactly one type deliberately prints
     * nothing. Unwrapping it here rather than at every call site keeps that
     * exception something two tests state on purpose, instead of a scattering of
     * force unwraps through twenty assertions that are about something else.
     */
    private fun rowFor(payload: EventPayload): TrailRow =
        requireNotNull(trailRowFor(event(payload), blank)) {
            "${payload::class.simpleName} produced no Trail row"
        }

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
            ReportSectionSnapshot(
                sectionKey = "observations",
                sidehead = "Your week, honestly",
                text = "Six things left.",
                familyKey = "intakeVsOutput",
                variantKey = "ob.flow.s1.l08",
                escalationStage = 1,
                register = "PLAIN",
                subjectId = "area-work",
                subjectKind = SubjectKind.AREA,
            ),
        ),
        factSnapshot = mapOf("completions" to "6"),
        headlineVariantKey = "hd.steady.01",
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
