package com.kamsiob.claritynow.data.widget

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The snapshot as text. MASTER_BUILD_PROMPT 13.3.
 *
 * This is the file a widget decodes in the moment it has to draw, on a phone that may
 * have restored a backup or been downgraded, so the three cases that matter are a round
 * trip, a document from a build that knew more than this one, and a file that is not a
 * document at all. None of them may throw: an exception here is Glance's error layout
 * sitting on somebody's home screen until they remove it by hand.
 */
class ClarityWidgetSnapshotCodecTest {

    private val snapshot = ClarityWidgetSnapshot(
        writtenAtMillis = 1_724_000_000_000L,
        dateKey = "2026-08-27",
        calmMode = true,
        areas = listOf(
            WidgetArea(
                id = "work",
                name = "Work",
                colorHex = "#2D7FF9",
                activeItemId = "a",
                activeItemTitle = "Draft the proposal",
                activeItemFirstStep = "Open the doc and read what is there",
                queueCount = 3,
                lastEventAtMillis = 99L,
            ),
            WidgetArea(id = "old", name = "Old", colorHex = "#8B5CF6", archived = true),
        ),
        automaticAreaId = "work",
        automaticDateKey = "2026-08-27",
        inboxCount = 2,
        focus = WidgetFocus(
            sessionId = "s1",
            areaId = "work",
            itemId = "a",
            itemTitle = "Draft the proposal",
            startedAtMillis = 1_000L,
            endsAtMillis = 1_501_000L,
            plannedSeconds = 1_500,
        ),
        rhythm = WidgetRhythm(
            activeDays = listOf(true, false, true),
            todayIndex = 2,
            line = "Active 2 of the last 3 days.",
        ),
        week = WidgetWeek(completed = 4, focusMinutes = 75, reflections = 3),
        guidance = WidgetGuidance(acceptedPlanLine = "I will start on Tuesday"),
    )

    @Test
    fun `every field survives a round trip`() {
        val decoded = ClarityWidgetSnapshotCodec.decode(ClarityWidgetSnapshotCodec.encode(snapshot))

        assertEquals(snapshot, decoded)
    }

    @Test
    fun `a document from a build that knew more is read for what this one understands`() {
        val text = """
            {
              "schema": 99,
              "dateKey": "2026-08-27",
              "inboxCount": 4,
              "somethingNobodyHasWrittenYet": { "a": 1 },
              "areas": [
                { "id": "work", "name": "Work", "colorHex": "#2D7FF9", "wallpaper": "no" }
              ]
            }
        """.trimIndent()

        val decoded = ClarityWidgetSnapshotCodec.decode(text)

        assertEquals(99, decoded?.schema)
        assertEquals(4, decoded?.inboxCount)
        assertEquals("Work", decoded?.areas?.single()?.name)
        // Everything this build knows about and that document did not.
        assertNull(decoded?.focus)
        assertEquals(0, decoded?.areas?.single()?.queueCount)
    }

    @Test
    fun `nothing readable is absence rather than an exception`() {
        assertNull(ClarityWidgetSnapshotCodec.decode(null))
        assertNull(ClarityWidgetSnapshotCodec.decode(""))
        assertNull(ClarityWidgetSnapshotCodec.decode("   "))
        assertNull(ClarityWidgetSnapshotCodec.decode("not a document at all"))
        assertNull(ClarityWidgetSnapshotCodec.decode("{ \"areas\": 7 }"))
    }

    @Test
    fun `an absent field is written as an absent key rather than as a null`() {
        val text = ClarityWidgetSnapshotCodec.encode(ClarityWidgetSnapshot.NOTHING)

        assertTrue("nulls do not belong in this file: $text", !text.contains("null"))
        assertEquals(ClarityWidgetSnapshot.NOTHING, ClarityWidgetSnapshotCodec.decode(text))
    }

    @Test
    fun `the fourteen day row counts a set and never a run`() {
        val rhythm = WidgetRhythm(
            activeDays = listOf(true, true, false, true),
            todayIndex = 3,
        )

        assertEquals(3, rhythm.activeCount)
        assertEquals(4, rhythm.length)
    }
}
