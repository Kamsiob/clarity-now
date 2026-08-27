package com.kamsiob.claritynow.domain.query

import com.kamsiob.claritynow.data.event.AreaArchived
import com.kamsiob.claritynow.data.event.AreaDeleted
import com.kamsiob.claritynow.data.event.AreaRecolored
import com.kamsiob.claritynow.data.event.AreaRenamed
import com.kamsiob.claritynow.data.event.AreaUnarchived
import com.kamsiob.claritynow.data.event.ItemEdited
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Renaming an area does not change how an older Trail entry reads. Issue #1,
 * CLARITY_LOGIC_ENGINE.md 1.1 prohibition 4.
 *
 * This is the criterion the whole screen is built to satisfy, and the shortcut that
 * breaks it, `state.areas[areaId]?.name`, produces a screen that looks entirely
 * correct until the day somebody renames something. Then every entry back to install
 * silently rewrites itself, and every entry belonging to a deleted area renders as
 * nothing at all. Neither failure announces itself.
 *
 * The resolvers below fold the log to the instant of the event instead, which is the
 * thing that makes a tombstone worth keeping: an area deleted last March still has to
 * be able to say its own name.
 */
class TrailSnapshotsTest {

    @Test
    fun `an area name resolves to what it was at the instant, not what it is now`() {
        val log = TrailTestLog()
        log.area(at(0, 9), "area-work", "Work")
        log.add(at(5, 9), AreaRenamed("area-work", "Work", "Career"))

        val queries = log.queries()
        assertEquals("Work", queries.areaNameAsOf("area-work", at(3, 9)))
        assertEquals("Career", queries.areaNameAsOf("area-work", at(7, 9)))
    }

    @Test
    fun `an area color resolves to what it was at the instant`() {
        val log = TrailTestLog()
        log.area(at(0, 9), "area-work", "Work", colorHex = "#2D7FF9")
        log.add(at(5, 9), AreaRecolored("area-work", "#2D7FF9", "#22C55E"))

        val queries = log.queries()
        // A recolor no more rewrites the past than a rename does, which is what keeps
        // the tint on an old row the color that row was actually shown in.
        assertEquals("#2D7FF9", queries.areaColorHexAsOf("area-work", at(3, 9)))
        assertEquals("#22C55E", queries.areaColorHexAsOf("area-work", at(7, 9)))
    }

    @Test
    fun `an item title resolves to what it was at the instant`() {
        val log = TrailTestLog()
        log.area(at(0, 9), "area-work", "Work")
        log.item(at(0, 9), "item-a", "area-work", "Call the printer")
        log.add(
            at(5, 9),
            ItemEdited("item-a", "Call the printer", "Call the printer about the covers", null, null),
        )

        val queries = log.queries()
        assertEquals("Call the printer", queries.itemTitleAsOf("item-a", at(3, 9)))
        assertEquals(
            "Call the printer about the covers",
            queries.itemTitleAsOf("item-a", at(7, 9)),
        )
    }

    @Test
    fun `a resolver answers at the instant of the event itself`() {
        val log = TrailTestLog()
        val created = log.area(at(0, 9), "area-work", "Work")
        val renamed = log.add(at(5, 9), AreaRenamed("area-work", "Work", "Career"))

        val queries = log.queries()
        // The one place in this package where a bound is inclusive, and it has to be.
        // ClarityRepository reads the clock once per commit and stamps every event in
        // that commit with the one reading, so an exclusive bound would leave an area
        // unable to name itself on the row that says it was created.
        assertEquals("Work", queries.areaNameAsOf("area-work", created.wallClock))
        assertEquals("Career", queries.areaNameAsOf("area-work", renamed.wallClock))
    }

    @Test
    fun `a rename after the instant never reaches back`() {
        val log = TrailTestLog()
        log.area(at(0, 9), "area-work", "Work")
        val touched = log.add(at(2, 9), AreaRecolored("area-work", "#2D7FF9", "#6366F1"))
        log.add(at(5, 9), AreaRenamed("area-work", "Work", "Career"))
        log.add(at(6, 9), AreaRecolored("area-work", "#6366F1", "#22C55E"))

        val queries = log.queries()
        assertEquals("Work", queries.areaNameAsOf("area-work", touched.wallClock))
        assertEquals("#6366F1", queries.areaColorHexAsOf("area-work", touched.wallClock))
    }

    @Test
    fun `an archived area still resolves its name`() {
        val log = TrailTestLog()
        log.area(at(0, 9), "area-work", "Work")
        log.add(at(4, 9), AreaRenamed("area-work", "Work", "Workshop"))
        log.add(at(5, 9), AreaArchived("area-work", "Workshop"))

        val queries = log.queries()
        assertEquals("Workshop", queries.areaNameAsOf("area-work", at(9, 9)))
        assertEquals(at(5, 9), queries.areaArchivedAt("area-work", at(9, 9)))
        // Archived, not gone. Out of the live set and still in the log.
        assertTrue(queries.liveAreaIdsAt(at(9, 9)).isEmpty())
    }

    @Test
    fun `an unarchive supersedes the archive before it`() {
        val log = TrailTestLog()
        log.area(at(0, 9), "area-work", "Work")
        log.add(at(3, 9), AreaArchived("area-work", "Work"))
        log.add(at(5, 9), AreaUnarchived("area-work", "Work"))

        val queries = log.queries()
        assertEquals(at(3, 9), queries.areaArchivedAt("area-work", at(4, 9)))
        assertNull(queries.areaArchivedAt("area-work", at(9, 9)))
        assertEquals(setOf("area-work"), queries.liveAreaIdsAt(at(9, 9)))
    }

    @Test
    fun `a tombstoned area still resolves its name`() {
        val log = TrailTestLog()
        log.area(at(0, 9), "area-work", "Work", colorHex = "#2D7FF9")
        log.item(at(0, 9), "item-a", "area-work", "Call the printer")
        log.add(at(5, 9), AreaDeleted("area-work", "Work"))

        val queries = log.queries()
        // Issue #1: events for archived and deleted areas still render. A live lookup
        // answers null here and the row loses its subject.
        assertEquals("Work", queries.areaNameAsOf("area-work", at(9, 9)))
        assertEquals("#2D7FF9", queries.areaColorHexAsOf("area-work", at(9, 9)))
        assertEquals("Call the printer", queries.itemTitleAsOf("item-a", at(9, 9)))
        assertEquals(at(5, 9), queries.areaDeletedAt("area-work", at(9, 9)))
    }

    @Test
    fun `an entity this log has never seen resolves to nothing rather than to a guess`() {
        val log = TrailTestLog()
        log.area(at(0, 9), "area-work", "Work")

        val queries = log.queries()
        assertNull(queries.areaNameAsOf("area-nowhere", at(9, 9)))
        assertNull(queries.areaColorHexAsOf("area-nowhere", at(9, 9)))
        assertNull(queries.itemTitleAsOf("item-nowhere", at(9, 9)))
        assertNull(queries.areaCreatedAt("area-nowhere"))
        // And before it existed, its own id resolves to nothing either.
        assertNull(queries.areaNameAsOf("area-work", at(0, 8)))
    }
}
