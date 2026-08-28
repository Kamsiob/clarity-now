package com.kamsiob.claritynow.data.widget

import com.kamsiob.claritynow.data.event.ItemStatus
import com.kamsiob.claritynow.data.widget.Fixture.DAY
import com.kamsiob.claritynow.domain.replay.ClarityState
import com.kamsiob.claritynow.domain.replay.FocusSessionState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * What the widgets are allowed to know, and what they are not.
 * MASTER_BUILD_PROMPT 13.3, design-v3.md 12.2.
 *
 * The composer is pure, so every case below is an input and an assertion with no
 * device, no clock and no database in it. That is the point of the layer: the daily
 * rotation, the queue counts and the vanished area are the three things most likely to
 * be wrong on somebody's home screen, and none of them can be checked by looking at a
 * screenshot.
 */
class ClarityWidgetSnapshotComposerTest {

    private fun compose(
        state: ClarityState,
        nowMillis: Long = 10 * DAY,
        dateKey: String = "2026-08-27",
        calmMode: Boolean = false,
        runningSession: FocusSessionState? = null,
        previous: ClarityWidgetSnapshot? = null,
    ) = ClarityWidgetSnapshotComposer.compose(
        state = state,
        nowMillis = nowMillis,
        dateKey = dateKey,
        calmMode = calmMode,
        runningSession = runningSession,
        previous = previous,
    )

    @Test
    fun `an area carries its active item, its first step and what waits behind it`() {
        val state = Fixture.state(
            areas = listOf(Fixture.area("work")),
            items = listOf(
                Fixture.item("a", "work", "Draft the proposal", ItemStatus.ACTIVE, "Open the doc"),
                Fixture.item("b", "work", "Call the bank"),
                Fixture.item("c", "work", "Book the room"),
                Fixture.item("d", "work", "Old thing", ItemStatus.COMPLETED),
            ),
        )

        val area = compose(state).areas.single()

        assertEquals("Draft the proposal", area.activeItemTitle)
        assertEquals("Open the doc", area.activeItemFirstStep)
        // Two queued, and neither the active one nor the completed one.
        assertEquals(2, area.queueCount)
    }

    @Test
    fun `an idle area is idle rather than absent`() {
        val state = Fixture.state(
            areas = listOf(Fixture.area("health")),
            items = listOf(Fixture.item("a", "health")),
        )

        val area = compose(state).areas.single()

        assertTrue(area.isIdle)
        assertNull(area.activeItemTitle)
        assertEquals(1, area.queueCount)
    }

    @Test
    fun `an archived area is carried and flagged, and a deleted one is gone`() {
        val state = Fixture.state(
            areas = listOf(
                Fixture.area("work", orderKey = "a"),
                Fixture.area("old", orderKey = "b", archived = true),
                Fixture.area("gone", orderKey = "c", deletedAt = 1L),
            ),
        )

        val snapshot = compose(state)

        assertEquals(listOf("work", "old"), snapshot.areas.map { it.id })
        assertEquals(listOf("work"), snapshot.liveAreas.map { it.id })
    }

    @Test
    fun `a widget pinned to an area tells archived apart from deleted`() {
        val state = Fixture.state(
            areas = listOf(Fixture.area("work"), Fixture.area("old", archived = true)),
        )

        val snapshot = compose(state)

        assertTrue(snapshot.resolve("work") is WidgetTarget.Live)
        assertTrue(snapshot.resolve("old") is WidgetTarget.Archived)
        assertEquals(WidgetTarget.Deleted, snapshot.resolve("never-existed"))
    }

    @Test
    fun `automatic picks the least recently touched area that has something active`() {
        val state = Fixture.state(
            areas = listOf(
                Fixture.area("fresh", orderKey = "a", lastEventAt = 9 * DAY),
                Fixture.area("stale", orderKey = "b", lastEventAt = 2 * DAY),
                // Older still, and idle, so it loses to the stale one that is active.
                Fixture.area("idle", orderKey = "c", lastEventAt = 1 * DAY),
            ),
            items = listOf(
                Fixture.item("f", "fresh", status = ItemStatus.ACTIVE),
                Fixture.item("s", "stale", status = ItemStatus.ACTIVE),
                Fixture.item("i", "idle"),
            ),
        )

        assertEquals("stale", compose(state).automaticAreaId)
    }

    @Test
    fun `automatic falls back to an idle area rather than showing nothing`() {
        val state = Fixture.state(
            areas = listOf(
                Fixture.area("newer", orderKey = "a", lastEventAt = 5 * DAY),
                Fixture.area("older", orderKey = "b", lastEventAt = 1 * DAY),
            ),
        )

        assertEquals("older", compose(state).automaticAreaId)
    }

    @Test
    fun `automatic holds its choice for the day even after the area is touched`() {
        val state = Fixture.state(
            areas = listOf(
                Fixture.area("work", orderKey = "a", lastEventAt = 1 * DAY),
                Fixture.area("home", orderKey = "b", lastEventAt = 2 * DAY),
            ),
            items = listOf(
                Fixture.item("w", "work", status = ItemStatus.ACTIVE),
                Fixture.item("h", "home", status = ItemStatus.ACTIVE),
            ),
        )
        val monday = compose(state, dateKey = "2026-08-27")
        assertEquals("work", monday.automaticAreaId)

        // Working in that area makes it the most recently touched, which is exactly the
        // moment a recomputed choice would jump to the other one under somebody's hand.
        val touched = Fixture.state(
            areas = listOf(
                Fixture.area("work", orderKey = "a", lastEventAt = 9 * DAY),
                Fixture.area("home", orderKey = "b", lastEventAt = 2 * DAY),
            ),
            items = state.items.values.toList(),
        )

        val later = compose(touched, dateKey = "2026-08-27", previous = monday)
        assertEquals("work", later.automaticAreaId)

        val tomorrow = compose(touched, dateKey = "2026-08-28", previous = later)
        assertEquals("home", tomorrow.automaticAreaId)
    }

    @Test
    fun `a held choice is dropped the moment its area stops being live`() {
        val yesterday = compose(
            Fixture.state(areas = listOf(Fixture.area("work"), Fixture.area("home"))),
            dateKey = "2026-08-27",
        )
        assertEquals("home", yesterday.automaticAreaId)

        val archived = Fixture.state(
            areas = listOf(Fixture.area("work"), Fixture.area("home", archived = true)),
        )

        val today = compose(archived, dateKey = "2026-08-27", previous = yesterday)

        assertEquals("work", today.automaticAreaId)
        assertTrue(today.resolve(null) is WidgetTarget.Live)
    }

    @Test
    fun `with no areas at all there is nothing to point at and nothing to draw`() {
        val snapshot = compose(Fixture.state())

        assertNull(snapshot.automaticAreaId)
        assertEquals(WidgetTarget.NoAreas, snapshot.resolve(null))
    }

    @Test
    fun `the unfiled inbox is counted and is never an area`() {
        val state = Fixture.state(
            areas = listOf(Fixture.area("work")),
            items = listOf(
                Fixture.item("u1", null, "A thought"),
                Fixture.item("u2", null, "Another"),
                Fixture.item("w", "work", status = ItemStatus.ACTIVE),
            ),
        )

        val snapshot = compose(state)

        assertEquals(2, snapshot.inboxCount)
        assertEquals(0, snapshot.areas.single().queueCount)
    }

    @Test
    fun `a running session carries the instant it ends rather than what is left of it`() {
        val state = Fixture.state(
            areas = listOf(Fixture.area("work")),
            items = listOf(Fixture.item("w", "work", "Draft it", ItemStatus.ACTIVE)),
        )
        val session = Fixture.session(areaId = "work", itemId = "w", startedAt = 1_000L)

        val focus = compose(state, runningSession = session).focus

        assertNotNull(focus)
        assertEquals(1_000L + 1_500L * 1_000L, focus?.endsAtMillis)
        assertEquals("Draft it", focus?.itemTitle)
    }

    @Test
    fun `guidance carries an accepted plan and never an unaccepted one`() {
        val declined = Fixture.state(
            plans = listOf(Fixture.plan("p1", "2026-08-23", "I will start on Tuesday", null)),
        )
        assertNull(compose(declined).guidance)

        val accepted = Fixture.state(
            plans = listOf(
                Fixture.plan("p1", "2026-08-16", "An older promise", 1L),
                Fixture.plan("p2", "2026-08-23", "I will start on Tuesday", 2L),
                Fixture.plan("p3", "2026-08-23", "One I did not take", null),
            ),
        )

        assertEquals(
            "I will start on Tuesday",
            compose(accepted).guidance?.acceptedPlanLine,
        )
    }

    @Test
    fun `what the projection does not hold is left empty rather than invented`() {
        // The fourteen day row, the week figures and the Report headline are folds over
        // the log or sentences the engine wrote. None of them is in a projection, so the
        // composer is handed them or leaves them out; there is no branch here that could
        // decide to work one out.
        val snapshot = compose(Fixture.state(areas = listOf(Fixture.area("work"))))

        assertNull(snapshot.rhythm)
        assertNull(snapshot.week)
        assertNull(snapshot.guidance)
    }

    @Test
    fun `handed a rhythm, it carries it through untouched`() {
        val rhythm = WidgetRhythm(activeDays = listOf(true, false, true), todayIndex = 2)

        val snapshot = ClarityWidgetSnapshotComposer.compose(
            state = Fixture.state(areas = listOf(Fixture.area("work"))),
            nowMillis = 1L,
            dateKey = "2026-08-27",
            calmMode = false,
            runningSession = null,
            previous = null,
            speech = WidgetSpeech(rhythm = rhythm),
        )

        assertEquals(rhythm, snapshot.rhythm)
        assertEquals(2, snapshot.rhythm?.activeCount)
    }

    @Test
    fun `only the instant it was written may differ without being a new snapshot`() {
        val state = Fixture.state(areas = listOf(Fixture.area("work")))
        val first = compose(state, nowMillis = 1_000L)
        val second = compose(state, nowMillis = 9_000L, previous = first)

        assertTrue(second.sameContentAs(first))

        val renamed = Fixture.state(areas = listOf(Fixture.area("work", name = "Work stuff")))

        assertFalse(compose(renamed, previous = first).sameContentAs(first))
    }

    @Test
    fun `calm mode travels in the snapshot rather than being read on the far side`() {
        val state = Fixture.state(areas = listOf(Fixture.area("work")))

        assertTrue(compose(state, calmMode = true).calmMode)
        assertFalse(compose(state, calmMode = false).calmMode)
    }
}
