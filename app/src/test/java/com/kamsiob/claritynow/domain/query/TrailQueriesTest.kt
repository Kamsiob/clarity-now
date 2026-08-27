package com.kamsiob.claritynow.domain.query

import com.kamsiob.claritynow.data.event.AreaArchived
import com.kamsiob.claritynow.data.event.AreaRenamed
import com.kamsiob.claritynow.data.event.FocusAbandoned
import com.kamsiob.claritynow.data.event.FocusCompleted
import com.kamsiob.claritynow.data.event.FocusStarted
import com.kamsiob.claritynow.data.event.ItemDeleted
import com.kamsiob.claritynow.data.event.ItemEdited
import com.kamsiob.claritynow.data.event.ItemPromoted
import com.kamsiob.claritynow.data.event.ItemStatus
import com.kamsiob.claritynow.data.event.PulseAnswered
import com.kamsiob.claritynow.data.event.PulseGenerated
import com.kamsiob.claritynow.data.event.ReflectionPeriod
import com.kamsiob.claritynow.data.event.SettingChanged
import com.kamsiob.claritynow.domain.replay.ClarityReplay
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneId

/**
 * The arithmetic every displayed number in this app will eventually come through.
 * MASTER_BUILD_PROMPT 9, CLARITY_LOGIC_ENGINE.md 3.1.
 *
 * Most of what is checked here is a definition rather than a calculation, because
 * the calculations are one line each and the definitions are what two people, or two
 * implementations, disagree about. Whether an abandoned session counts toward focus
 * minutes, whether the active item is in the queue, whether a day with an edit and no
 * completion is an active day, and whether the age of an item runs from when it was
 * added or from when it last became active are all decisions that produce a
 * plausible looking number either way. CLARITY_LOGIC_ENGINE.md 1: one off by one
 * number permanently destroys the credibility of everything else the app says.
 */
class TrailQueriesTest {

    // Windows -----------------------------------------------------------------

    @Test
    fun `completions between two instants exclude the upper bound`() {
        val log = TrailTestLog()
        log.area(at(0, 9), "area-work", "Work")
        log.item(at(0, 9), "item-a", "area-work", "Call the printer")
        log.promote(at(0, 9), "item-a", "area-work", "Call the printer")
        log.item(at(0, 9), "item-b", "area-work", "Draft the release notes", orderKey = "a1")
        val first = at(1, 10)
        val second = at(2, 10)
        log.complete(first, "item-a", "area-work", "Call the printer")
        log.promote(first, "item-b", "area-work", "Draft the release notes")
        log.complete(second, "item-b", "area-work", "Draft the release notes")

        val queries = log.queries()
        // Half open, always. The lower bound is in, the upper bound is out, so two
        // adjacent windows share no event and a day belongs to exactly one of them.
        assertEquals(1, queries.completionsBetween(first, second))
        assertEquals(2, queries.completionsBetween(first, second + 1))
        assertEquals(0, queries.completionsBetween(first + 1, second))
    }

    @Test
    fun `the queue size at an instant is the queue as that instant began`() {
        val log = TrailTestLog()
        log.area(at(0, 9), "area-work", "Work")
        log.item(at(0, 9), "item-a", "area-work", "Call the printer")
        log.item(at(0, 9), "item-b", "area-work", "Draft the release notes", orderKey = "a1")
        log.item(at(0, 9), "item-c", "area-work", "Rewrite the intro", orderKey = "a2")
        val promotion = at(1, 9)
        log.promote(promotion, "item-a", "area-work", "Call the printer")

        val queries = log.queries()
        assertEquals(3, queries.queueSizeAt(promotion))
        assertEquals(2, queries.queueSizeAt(promotion + 1))
    }

    // Active days -------------------------------------------------------------

    @Test
    fun `active days counts days with any user event, not days with a completion`() {
        val log = TrailTestLog()
        log.area(at(0, 9), "area-work", "Work")
        log.item(at(0, 10), "item-a", "area-work", "Call the printer")

        val queries = log.queries()
        assertEquals(1, queries.activeDays(startOfDay(0), startOfDay(1)))
        assertEquals(0, queries.completionsBetween(startOfDay(0), startOfDay(1)))
        assertEquals(setOf("2026-01-04"), queries.activeDayKeys(startOfDay(0), startOfDay(1)))
    }

    @Test
    fun `engine authored events never make a day active`() {
        val log = TrailTestLog()
        log.add(
            at(1, 9),
            PulseGenerated(
                pulseId = "pulse-1",
                dateKey = "2026-01-05",
                family = "quietDay",
                escalationStage = 1,
                register = "PLAIN",
                variantKey = "quietday.s1.02",
                renderedObservation = "Nothing moved yesterday.",
                renderedQuestion = null,
                factSnapshot = emptyMap(),
                reflectionPeriod = ReflectionPeriod.YESTERDAY,
            ),
        )
        log.add(at(2, 9), PulseAnswered("pulse-1", "yes", "Still the right thing", true))

        val queries = log.queries()
        // A person who opens the app and touches nothing has not had an active day,
        // or quietDay could never fire on anyone who opens the app.
        assertEquals(0, queries.activeDays(startOfDay(1), startOfDay(2)))
        assertEquals(0, queries.totalEvents(startOfDay(1), startOfDay(2)))
        assertTrue(queries.eventsPerDay(startOfDay(1), startOfDay(2)).isEmpty())
        // Answering one is something the person did.
        assertEquals(1, queries.activeDays(startOfDay(2), startOfDay(3)))
        assertEquals(1, queries.totalEvents(startOfDay(2), startOfDay(3)))
    }

    @Test
    fun `completions are bucketed by the local calendar day, not by the day in UTC`() {
        val log = TrailTestLog()
        log.area(at(0, 9), "area-work", "Work")
        log.item(at(0, 9), "item-a", "area-work", "Call the printer")
        log.item(at(0, 9), "item-b", "area-work", "Draft the release notes", orderKey = "a1")
        log.promote(at(0, 9), "item-a", "area-work", "Call the printer")
        log.complete(at(0, 23, 30), "item-a", "area-work", "Call the printer")
        log.promote(at(0, 23, 31), "item-b", "area-work", "Draft the release notes")
        log.complete(at(1, 0, 30), "item-b", "area-work", "Draft the release notes")

        // Both instants fall on 2026-01-05 in UTC and on two different days in New
        // York, which is the whole reason the zone is a constructor parameter.
        assertEquals(
            mapOf("2026-01-04" to 1, "2026-01-05" to 1),
            log.queries().completionsPerDay(startOfDay(0), startOfDay(2)),
        )
        assertEquals(
            mapOf("2026-01-05" to 2),
            TrailQueries(log.events(), ZoneId.of("UTC"))
                .completionsPerDay(startOfDay(0), startOfDay(2)),
        )
    }

    // Focus -------------------------------------------------------------------

    @Test
    fun `focus minutes include abandoned sessions`() {
        val log = focusFixture()
        log.add(at(1, 14), FocusStarted("focus-1", "area-work", "item-a", 1500))
        log.add(at(1, 15), FocusCompleted("focus-1", 600))
        log.add(at(1, 16), FocusStarted("focus-2", "area-work", "item-a", 1500))
        log.add(at(1, 17), FocusAbandoned("focus-2", 300))

        val queries = log.queries()
        // MASTER_BUILD_PROMPT 10 treats abandonment neutrally everywhere. Deleting
        // those five minutes would punish it in the arithmetic while the language is
        // careful not to.
        assertEquals(900L, queries.focusSecondsTotal(startOfDay(1), startOfDay(2)))
        assertEquals(15, queries.focusMinutes(startOfDay(1), startOfDay(2)))
        assertEquals(
            FocusCounts(started = 2, completed = 1, abandoned = 1, unresolved = 0),
            queries.focusSessionCounts(startOfDay(1), startOfDay(2)),
        )
    }

    @Test
    fun `focus minutes round down so a part minute is never advertised as a whole one`() {
        val log = focusFixture()
        log.add(at(1, 14), FocusStarted("focus-1", "area-work", "item-a", 1500))
        log.add(at(1, 15), FocusCompleted("focus-1", 119))

        val queries = log.queries()
        assertEquals(119L, queries.focusSecondsTotal(startOfDay(1), startOfDay(2)))
        assertEquals(1, queries.focusMinutes(startOfDay(1), startOfDay(2)))
    }

    @Test
    fun `a focus session with no terminal event is unresolved, never abandoned`() {
        val log = focusFixture()
        log.add(at(1, 14), FocusStarted("focus-1", "area-work", "item-a", 1500))

        val queries = log.queries()
        // A killed process leaves exactly this. Inferring abandonment by subtraction
        // would put a number behind language that never blames.
        assertEquals(
            FocusCounts(started = 1, completed = 0, abandoned = 0, unresolved = 1),
            queries.focusSessionCounts(startOfDay(1), startOfDay(2)),
        )
        assertEquals(0L, queries.focusSecondsTotal(startOfDay(1), startOfDay(2)))
    }

    @Test
    fun `a focus session is attributed to the day it started`() {
        val log = focusFixture()
        log.add(at(0, 23, 50), FocusStarted("focus-1", "area-work", "item-a", 1500))
        log.add(at(1, 0, 10), FocusCompleted("focus-1", 1200))

        val queries = log.queries()
        assertEquals(1200L, queries.focusSecondsTotal(startOfDay(0), startOfDay(1)))
        assertEquals(
            FocusCounts(started = 1, completed = 1, abandoned = 0, unresolved = 0),
            queries.focusSessionCounts(startOfDay(0), startOfDay(1)),
        )
        // One session lands on exactly one day, so a heat strip cannot double count
        // it and "{sessions} started, {n} completed" cannot disagree with itself.
        assertEquals(0L, queries.focusSecondsTotal(startOfDay(1), startOfDay(2)))
        assertEquals(
            FocusCounts(started = 0, completed = 0, abandoned = 0, unresolved = 0),
            queries.focusSessionCounts(startOfDay(1), startOfDay(2)),
        )
    }

    // Queue -------------------------------------------------------------------

    @Test
    fun `queue size excludes the active item`() {
        val log = TrailTestLog()
        log.area(at(0, 9), "area-work", "Work")
        log.item(at(0, 9), "item-a", "area-work", "Call the printer")
        log.item(at(0, 9), "item-b", "area-work", "Draft the release notes", orderKey = "a1")
        log.item(at(0, 9), "item-c", "area-work", "Rewrite the intro", orderKey = "a2")
        log.promote(at(0, 10), "item-a", "area-work", "Call the printer")

        val queries = log.queries()
        val asOf = at(1, 9)
        // "{areaName} is holding {n} items behind its active one." An area with a
        // queue of zero is still allowed to have an active item.
        assertEquals(2, queries.queueSizeAt(asOf))
        assertEquals(mapOf("area-work" to 2), queries.queueSizeByAreaAt(asOf))
        assertEquals(mapOf("area-work" to "item-a"), queries.activeItemPerAreaAt(asOf))
    }

    @Test
    fun `queue size excludes completed and tombstoned items`() {
        val log = TrailTestLog()
        log.area(at(0, 9), "area-work", "Work")
        log.item(at(0, 9), "item-a", "area-work", "Call the printer")
        log.item(at(0, 9), "item-b", "area-work", "Draft the release notes", orderKey = "a1")
        log.item(at(0, 9), "item-c", "area-work", "Rewrite the intro", orderKey = "a2")
        log.promote(at(0, 10), "item-a", "area-work", "Call the printer")
        log.complete(at(1, 10), "item-a", "area-work", "Call the printer")
        log.promote(at(1, 10), "item-b", "area-work", "Draft the release notes")
        log.add(at(2, 10), ItemDeleted("item-c", "area-work", "Rewrite the intro"))

        val queries = log.queries()
        val asOf = at(3, 9)
        assertEquals(0, queries.queueSizeAt(asOf))
        assertEquals(mapOf("area-work" to 0), queries.queueSizeByAreaAt(asOf))
        assertEquals(mapOf("area-work" to "item-b"), queries.activeItemPerAreaAt(asOf))
        // The rows are all still there. Tombstones, never row deletes.
        assertEquals(3, ClarityReplay.replay(log.events()).items.size)
    }

    @Test
    fun `queue size excludes archived areas by default`() {
        val log = TrailTestLog()
        log.area(at(0, 9), "area-work", "Work")
        log.area(at(0, 9), "area-home", "Home", colorHex = "#22C55E", orderKey = "a1")
        log.item(at(0, 9), "item-a", "area-work", "Call the printer")
        log.item(at(0, 9), "item-b", "area-work", "Draft the release notes", orderKey = "a1")
        log.item(at(0, 9), "item-c", "area-work", "Rewrite the intro", orderKey = "a2")
        log.promote(at(0, 10), "item-a", "area-work", "Call the printer")
        log.item(at(0, 9), "item-d", "area-home", "Fix the leaking tap", areaName = "Home")
        log.item(at(0, 9), "item-e", "area-home", "Book the dentist", orderKey = "a1", areaName = "Home")
        log.item(at(0, 9), "item-f", "area-home", "Write the letter", orderKey = "a2", areaName = "Home")
        log.promote(at(0, 10), "item-d", "area-home", "Fix the leaking tap", areaName = "Home")
        log.add(at(1, 9), AreaArchived("area-home", "Home"))

        val queries = log.queries()
        val asOf = at(2, 9)
        // An archived area is outside AreaFacts entirely, so a rollup that counted it
        // would print a number about things the person cannot see.
        assertEquals(2, queries.queueSizeAt(asOf))
        assertEquals(setOf("area-work"), queries.queueSizeByAreaAt(asOf).keys)
        assertEquals(4, queries.queueSizeAt(asOf, includeArchived = true))
        assertEquals(
            setOf("area-home", "area-work"),
            queries.queueSizeByAreaAt(asOf, includeArchived = true).keys,
        )
        assertEquals(setOf("area-work"), queries.liveAreaIdsAt(asOf))
    }

    // Item age ----------------------------------------------------------------

    @Test
    fun `days active for an item runs from the most recent promotion`() {
        val queries = agedItemFixture().queries()
        assertEquals(at(3, 9), queries.activeSinceForItem("item-a", at(10, 9)))
        assertEquals(7, queries.daysActiveForItem("item-a", at(10, 9)))
    }

    @Test
    fun `days since an item was added is a different number from days active`() {
        val queries = agedItemFixture().queries()
        // The trap this pair exists to stop. Three approved Report lines ask how long
        // something has been waiting and the Pulse asks how long it has been active,
        // and one function serving both meanings is silently wrong in one of them.
        assertEquals(10, queries.daysSinceItemAdded("item-a", at(10, 9)))
        assertEquals(7, queries.daysActiveForItem("item-a", at(10, 9)))
        assertEquals(at(0, 9), queries.itemAddedAt("item-a"))
    }

    @Test
    fun `a demotion and a later promotion restart the active clock`() {
        val log = TrailTestLog()
        log.area(at(0, 9), "area-work", "Work")
        log.item(at(0, 9), "item-a", "area-work", "Call the printer")
        log.item(at(0, 9), "item-b", "area-work", "Draft the release notes", orderKey = "a1")
        log.promote(at(3, 9), "item-a", "area-work", "Call the printer")
        log.promote(
            at(5, 9),
            "item-b",
            "area-work",
            "Draft the release notes",
            demotedItemId = "item-a",
            demotedToOrderKey = "Zz",
        )
        log.promote(
            at(8, 9),
            "item-a",
            "area-work",
            "Call the printer",
            demotedItemId = "item-b",
            demotedToOrderKey = "Zy",
        )

        val queries = log.queries()
        // CLARITY_LOGIC_ENGINE.md 7.3: when the condition lapses the ladder resets and
        // a new active item starts at stage 1. A promotion resetting the age is the
        // intended behavior, absorbed downstream by escalation monotonicity.
        assertEquals(2, queries.daysActiveForItem("item-a", at(10, 9)))
        assertNull(queries.activeSinceForItem("item-b", at(10, 9)))
        assertNull(queries.daysActiveForItem("item-b", at(10, 9)))
    }

    @Test
    fun `an item that was never promoted has no active age at all`() {
        val log = TrailTestLog()
        log.area(at(0, 9), "area-work", "Work")
        log.item(at(0, 9), "item-a", "area-work", "Call the printer")

        val queries = log.queries()
        // No fallback to the moment it was added. That fallback is unreachable in
        // ClarityRepository, where an add into an empty area writes the promotion in
        // the same commit, and here it would quietly answer with the wrong quantity.
        assertNull(queries.activeSinceForItem("item-a", at(10, 9)))
        assertNull(queries.daysActiveForItem("item-a", at(10, 9)))
        assertEquals(10, queries.daysSinceItemAdded("item-a", at(10, 9)))
    }

    // Promotions and swaps ----------------------------------------------------

    @Test
    fun `a swap is a promotion with a demoted item and a promotion is not always a swap`() {
        val log = TrailTestLog()
        log.area(at(0, 9), "area-work", "Work")
        log.area(at(0, 9), "area-home", "Home", colorHex = "#22C55E", orderKey = "a1")
        log.item(at(0, 9), "item-a", "area-work", "Call the printer")
        log.item(at(0, 9), "item-b", "area-work", "Draft the release notes", orderKey = "a1")
        log.item(at(0, 9), "item-c", "area-home", "Fix the leaking tap", areaName = "Home")
        log.promote(at(1, 9), "item-a", "area-work", "Call the printer")
        log.promote(
            at(2, 9),
            "item-b",
            "area-work",
            "Draft the release notes",
            demotedItemId = "item-a",
            demotedToOrderKey = "Zz",
        )
        log.promote(at(3, 9), "item-c", "area-home", "Fix the leaking tap", areaName = "Home")

        val queries = log.queries()
        assertEquals(3, queries.promotionsBetween(startOfDay(0), startOfDay(7)))
        assertEquals(1, queries.swapsBetween(startOfDay(0), startOfDay(7)))
        assertEquals(
            mapOf("area-work" to 1),
            queries.swapsPerArea(startOfDay(0), startOfDay(7)),
        )
    }

    // Area resolution ---------------------------------------------------------

    @Test
    fun `last event for an area is null when the area has never been touched`() {
        val log = TrailTestLog()
        val created = log.area(at(0, 9), "area-work", "Work")
        val renamed = log.add(at(4, 9), AreaRenamed("area-work", "Work", "Studio"))

        val queries = log.queries()
        assertNull(queries.lastEventForArea("area-nowhere", at(10, 9)))
        assertEquals(renamed.wallClock, queries.lastEventForArea("area-work", at(10, 9)))
        // Strictly before the instant, so asking at the moment it was created finds
        // nothing rather than finding the creation itself.
        assertNull(queries.lastEventForArea("area-work", created.wallClock))
    }

    @Test
    fun `events per area omits events whose area cannot be resolved`() {
        val log = TrailTestLog()
        log.area(at(0, 9), "area-work", "Work")
        log.item(at(0, 9), "item-a", "area-work", "Call the printer")
        log.promote(at(0, 9), "item-a", "area-work", "Call the printer")
        log.add(at(0, 20), SettingChanged("afterCompleting", "AUTO_PROMOTE", "CHOOSE_FROM_QUEUE"))
        log.add(
            at(0, 21),
            PulseGenerated(
                pulseId = "pulse-1",
                dateKey = "2026-01-04",
                family = "persistence",
                escalationStage = 1,
                register = "PLAIN",
                variantKey = "persistence.s1.04",
                renderedObservation = "Call the printer has been active for a day.",
                renderedQuestion = null,
                factSnapshot = emptyMap(),
                reflectionPeriod = ReflectionPeriod.TODAY_SO_FAR,
            ),
        )

        val queries = log.queries()
        // Absent from the map entirely, never bucketed under a placeholder key. A
        // placeholder eventually gets printed, and a fabricated area name is the one
        // failure CLARITY_LOGIC_ENGINE.md 1 calls unrecoverable.
        assertEquals(mapOf("area-work" to 3), queries.eventsPerArea(startOfDay(0), startOfDay(1)))
        // A setting change is still something the person did, so it counts as one of
        // the day's events even though it belongs to no area.
        assertEquals(4, queries.totalEvents(startOfDay(0), startOfDay(1)))
    }

    @Test
    fun `a focus terminal event resolves its area through the session start`() {
        val log = focusFixture()
        log.add(at(1, 14), FocusStarted("focus-1", "area-work", "item-a", 1500))
        val finished = log.add(at(1, 15), FocusCompleted("focus-1", 900))

        val queries = log.queries()
        assertEquals("area-work", queries.areaIdOf(finished))
        assertEquals("area-work", queries.areaIdOfFocusSession("focus-1"))
        assertEquals("item-a", queries.itemIdOfFocusSession("focus-1"))
    }

    @Test
    fun `a focus terminal event with no session start resolves to no area`() {
        val log = focusFixture()
        // A merged or imported log can genuinely arrive like this, and the reducer
        // already treats the mirror case as a diagnostic rather than a crash.
        val orphan = log.add(at(1, 15), FocusCompleted("focus-orphan", 900))

        val queries = log.queries()
        assertNull(queries.areaIdOf(orphan))
        assertNull(queries.areaIdOfFocusSession("focus-orphan"))
        assertTrue(
            !queries.eventsPerArea(startOfDay(1), startOfDay(2)).containsKey("area-work"),
        )
    }

    @Test
    fun `an item resolves to the area it was added to, forever`() {
        val log = TrailTestLog()
        log.area(at(0, 9), "area-work", "Work")
        log.item(at(0, 9), "item-a", "area-work", "Call the printer")
        val edited = log.add(
            at(6, 9),
            ItemEdited("item-a", "Call the printer", "Call the printer about the covers", null, null),
        )

        val queries = log.queries()
        // ITEM_EDITED carries only an item id. Resolving it through the item's own
        // ITEM_ADDED is exact rather than a heuristic: an item's area is assigned once
        // and no event type moves an item between areas.
        assertEquals("area-work", queries.areaIdOfItem("item-a"))
        assertEquals("area-work", queries.areaIdOf(edited))
    }

    @Test
    fun `a completed record reads its snapshots from the payload rather than recomputing them`() {
        val log = TrailTestLog()
        log.area(at(0, 9), "area-work", "Work")
        log.item(at(0, 9), "item-a", "area-work", "Call the printer")
        log.promote(at(9, 9), "item-a", "area-work", "Call the printer")
        // The writing device had already shown this person nine days. It promoted
        // again this morning, so anything recomputed here would answer zero.
        log.complete(at(9, 17), "item-a", "area-work", "Call the printer", activeDurationDays = 9)
        log.add(at(11, 9), AreaRenamed("area-work", "Work", "Studio"))
        log.add(
            at(11, 9),
            ItemEdited("item-a", "Call the printer", "Call the printer about the covers", null, null),
        )

        val record = log.queries()
            .completedItemsBetween(startOfDay(0), startOfDay(14))
            .single()
        assertEquals(9, record.activeDurationDays)
        assertEquals("Call the printer", record.titleSnapshot)
        assertEquals("Work", record.areaNameSnapshot)
        assertEquals(at(9, 17), record.completedAt)
    }

    // Order -------------------------------------------------------------------

    @Test
    fun `a fact is the same whether the log arrives in order or shuffled`() {
        val events = richLog().events()
        val straight = facts(TrailQueries(events, TEST_ZONE))

        assertEquals(straight, facts(TrailQueries(events.reversed(), TEST_ZONE)))
        for (seed in 1L..8L) {
            assertEquals(
                "seed $seed",
                straight,
                facts(TrailQueries(events.shuffled(java.util.Random(seed)), TEST_ZONE)),
            )
        }
    }

    @Test
    fun `a wall clock earlier than a lower lamport does not corrupt a fold`() {
        val log = TrailTestLog()
        log.area(at(0, 9), "area-work", "Work")
        log.item(at(0, 9), "item-a", "area-work", "Call the printer")
        log.item(at(0, 9), "item-b", "area-work", "Draft the release notes", orderKey = "a1")
        log.item(at(0, 9), "item-c", "area-work", "Rewrite the intro", orderKey = "a2")
        // The phone promoted at three in the afternoon. The laptop, which had been
        // apart, promoted something else that morning and its event carries the
        // higher lamport. The wall clock says the phone acted last; the total order
        // says the laptop did, and the total order is what state is folded in.
        log.add(
            at(5, 15),
            ItemPromoted(
                itemId = "item-b",
                areaId = "area-work",
                previousStatus = ItemStatus.QUEUED,
                demotedItemId = null,
                demotedToOrderKey = null,
                titleSnapshot = "Draft the release notes",
                areaNameSnapshot = "Work",
            ),
            origin = "device-a",
            lamportOverride = 20,
        )
        log.add(
            at(5, 9),
            ItemPromoted(
                itemId = "item-c",
                areaId = "area-work",
                previousStatus = ItemStatus.QUEUED,
                demotedItemId = "item-b",
                demotedToOrderKey = "Zz",
                titleSnapshot = "Rewrite the intro",
                areaNameSnapshot = "Work",
            ),
            origin = "device-b",
            lamportOverride = 21,
        )

        val queries = log.queries()
        val asOf = at(6, 9)
        val replayed = ClarityReplay.replay(log.events())
        assertEquals("item-c", replayed.activeItemIn("area-work")?.id)
        assertEquals(mapOf("area-work" to "item-c"), queries.activeItemPerAreaAt(asOf))
        assertEquals(at(5, 9), queries.activeSinceForItem("item-c", asOf))
        assertNull(queries.activeSinceForItem("item-b", asOf))
        assertEquals(replayed.queueIn("area-work").size, queries.queueSizeAt(asOf))
    }

    // Fixtures ----------------------------------------------------------------

    /** One area, one item, already active, so a focus session has somewhere to run. */
    private fun focusFixture(): TrailTestLog {
        val log = TrailTestLog()
        log.area(at(0, 9), "area-work", "Work")
        log.item(at(0, 9), "item-a", "area-work", "Call the printer")
        log.promote(at(0, 9), "item-a", "area-work", "Call the printer")
        return log
    }

    /** Added on day zero, promoted on day three. The two ages differ by three days. */
    private fun agedItemFixture(): TrailTestLog {
        val log = TrailTestLog()
        log.area(at(0, 9), "area-work", "Work")
        log.item(at(0, 9), "item-a", "area-work", "Call the printer")
        log.promote(at(3, 9), "item-a", "area-work", "Call the printer")
        return log
    }

    /** A fortnight with something of every shape in it, for the ordering tests. */
    private fun richLog(): TrailTestLog {
        val log = TrailTestLog()
        log.area(at(0, 9), "area-work", "Work")
        log.area(at(0, 9), "area-home", "Home", colorHex = "#22C55E", orderKey = "a1")
        log.item(at(0, 10), "item-a", "area-work", "Call the printer")
        log.item(at(0, 10), "item-b", "area-work", "Draft the release notes", orderKey = "a1")
        log.item(at(0, 11), "item-c", "area-home", "Fix the leaking tap", areaName = "Home")
        log.promote(at(0, 12), "item-a", "area-work", "Call the printer")
        log.promote(at(0, 12), "item-c", "area-home", "Fix the leaking tap", areaName = "Home")
        log.add(at(1, 14), FocusStarted("focus-1", "area-work", "item-a", 1500))
        log.add(at(1, 15), FocusCompleted("focus-1", 1500))
        log.add(at(2, 9), FocusStarted("focus-2", "area-work", "item-a", 1500))
        log.add(at(2, 9), FocusAbandoned("focus-2", 320))
        log.add(
            at(3, 10),
            ItemEdited("item-b", "Draft the release notes", "Draft the notes", null, "by Friday"),
        )
        log.complete(at(4, 17), "item-a", "area-work", "Call the printer", activeDurationDays = 4)
        log.promote(at(4, 17), "item-b", "area-work", "Draft the notes")
        log.add(at(5, 9), AreaRenamed("area-home", "Home", "Personal"))
        log.add(at(6, 20), SettingChanged("afterCompleting", "AUTO_PROMOTE", "CHOOSE_FROM_QUEUE"))
        log.add(at(7, 9), AreaArchived("area-home", "Personal"))
        log.add(at(8, 9), ItemDeleted("item-b", "area-work", "Draft the notes"))
        return log
    }

    /**
     * Every Phase 3 answer in one list, so the ordering tests compare all of them at
     * once. A function added to the facade without being added here is not covered by
     * the shuffle proof, which is the point of keeping them together.
     */
    private fun facts(queries: TrailQueries): List<Any?> {
        val from = startOfDay(0)
        val to = startOfDay(14)
        return listOf(
            queries.completionsBetween(from, to),
            queries.completionsPerArea(from, to),
            queries.completionsPerDay(from, to),
            queries.completedItemsBetween(from, to),
            queries.eventsPerArea(from, to),
            queries.totalEvents(from, to),
            queries.eventsPerDay(from, to),
            queries.activeDays(from, to),
            queries.activeDayKeys(from, to),
            queries.focusSecondsTotal(from, to),
            queries.focusMinutes(from, to),
            queries.focusSessionCounts(from, to),
            queries.queueSizeAt(to),
            queries.queueSizeByAreaAt(to),
            queries.queueSizeByAreaAt(to, includeArchived = true),
            queries.activeItemPerAreaAt(to),
            queries.liveAreaIdsAt(to),
            queries.additionsBetween(from, to),
            queries.additionsPerArea(from, to),
            queries.promotionsBetween(from, to),
            queries.swapsBetween(from, to),
            queries.swapsPerArea(from, to),
            queries.activeSinceForItem("item-b", to),
            queries.daysActiveForItem("item-b", to),
            queries.daysSinceItemAdded("item-b", to),
            queries.itemAddedAt("item-b"),
            queries.areaCreatedAt("area-home"),
            queries.areaArchivedAt("area-home", to),
            queries.areaDeletedAt("area-home", to),
            queries.lastEventForArea("area-work", to),
            queries.lastEventForArea("area-home", to),
        )
    }
}
