package com.kamsiob.claritynow.domain.query

import com.kamsiob.claritynow.data.event.FocusCompleted
import com.kamsiob.claritynow.data.event.FocusStarted
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * What a page has to fetch before it can name what is on it.
 *
 * A Trail page is loaded by entity id, and for every event family except one the
 * page's own entity ids are exactly the ids whose history is needed. The focus
 * family breaks that: a focus event is keyed by its session, and the item it was run
 * on is named only inside the `FOCUS_STARTED` payload.
 *
 * This was a real defect. The loader fetched item history for the page's entity ids,
 * which for a focus row is a session id, so a session on an item added more than a
 * fortnight earlier rendered as "Finished 25 minutes of focus on" with nothing after
 * the preposition. Nothing in the suite could see it, because every other fixture
 * hands the facade a complete log and so never exercises what a page failed to load.
 */
class TrailPageLoadingTest {

    @Test
    fun `an item touched on the page is needed by the page`() {
        val log = TrailTestLog()
        log.area(at(0), "area-1", "Kitchen")
        val added = log.item(at(1), "item-1", "area-1", "Fix the shelf")

        assertEquals(setOf("item-1"), itemIdsNeededBy(listOf(added), emptyList()))
    }

    @Test
    fun `a focus session's item is needed even though the page never names it`() {
        val log = TrailTestLog()
        val started = log.add(
            wallClock = at(20),
            payload = FocusStarted(
                sessionId = "focus-1",
                areaId = "area-1",
                itemId = "item-9",
                plannedSeconds = 1_500,
            ),
        )
        val finished = log.add(
            wallClock = at(20, 9, 25),
            payload = FocusCompleted(sessionId = "focus-1", actualSeconds = 1_500),
        )

        // This is the whole point. The page's own entity ids are both "focus-1".
        val pageEntityIds = listOf(started, finished).mapNotNull { it.entityId }.toSet()
        assertEquals(setOf("focus-1"), pageEntityIds)

        val needed = itemIdsNeededBy(listOf(started, finished), listOf(started))
        assertTrue(
            "a page carrying a focus session needs the title of the item it ran on, " +
                "and that id appears nowhere in the page's entity ids. Needed: $needed",
            "item-9" in needed,
        )
    }

    @Test
    fun `a terminal focus event alone still needs its item, through the session start`() {
        val log = TrailTestLog()
        val started = log.add(
            wallClock = at(1),
            payload = FocusStarted(
                sessionId = "focus-2",
                areaId = "area-1",
                itemId = "item-4",
                plannedSeconds = 600,
            ),
        )
        // The page holds only the terminal event; the start is older than the window
        // and arrives from the session lookup instead.
        val abandoned = log.add(
            wallClock = at(20),
            payload = FocusCompleted(sessionId = "focus-2", actualSeconds = 600),
        )

        assertEquals(setOf("item-4"), itemIdsNeededBy(listOf(abandoned), listOf(started)))
    }

    @Test
    fun `a page with no focus events needs nothing beyond the items it names`() {
        val log = TrailTestLog()
        log.area(at(0), "area-1", "Kitchen")
        val added = log.item(at(1), "item-1", "area-1", "Fix the shelf")
        val completed = log.complete(at(2), "item-1", "area-1", "Fix the shelf", "Kitchen")

        assertEquals(
            setOf("item-1"),
            itemIdsNeededBy(listOf(added, completed), emptyList()),
        )
    }
}
