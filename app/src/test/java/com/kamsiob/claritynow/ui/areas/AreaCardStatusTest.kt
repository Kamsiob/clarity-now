package com.kamsiob.claritynow.ui.areas

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The card's fourth row, and the count that was missing from it. `design-v3.md` 10.3 and
 * issue #65.
 *
 * This is a queue app whose main screen hid the queue. With eleven items behind Work the
 * card was identical to the day there was one: a dot, a name, a title, and a status line
 * that only spoke when the area was idle. The All Areas widget printed the count on the
 * home screen while the screen the app opens on would not say it.
 *
 * The fix is one line of caption text, and everything that could go wrong with it is a
 * rule somebody could undo without noticing: a badge, a color, a zero, a second row, or a
 * second copy of the wording drifting away from the widget's. So they are asserted.
 */
class AreaCardStatusTest {

    // ------------------------------------------------------------------ one wording

    /**
     * One resource, two readers. Two identical plurals is the definition of a string that
     * drifts, and the acceptance criterion is that the card and the widget state one fact
     * one way.
     */
    @Test
    fun `the card and the widget read the same count from the same resource`() {
        assertTrue("the card's status row states the count", "R.plurals.queue_waiting" in card())
        assertTrue("and so does the All Areas widget", "R.plurals.queue_waiting" in widget())
        assertFalse(
            "the widget's private copy of this wording was renamed, not duplicated",
            "widget_waiting" in strings() || "widget_waiting" in widget(),
        )
        assertEquals(
            "exactly one plural in the app says what is waiting",
            1,
            Regex("<plurals name=\"queue_waiting\">").findAll(strings()).count(),
        )
    }

    // ------------------------------------------------------------------ one line

    /**
     * 10.3 caps the card at four rows, and the count went into the row that already
     * existed rather than beside it. A status row that could draw twice is the budget
     * broken, so the row is one composable with one `Text` in it.
     */
    @Test
    fun `the status row is one line and the deck is what a running session states`() {
        val status = statusLine()
        assertEquals(
            "two Text calls here is a fifth row on a card that is capped at four",
            1,
            Regex("\\bText\\(").findAll(status).count(),
        )
        assertTrue(
            "a running session has its own deck, and two status rows is the budget broken",
            "if (area.focusMinutesRemaining != null) return" in status,
        )
        assertTrue("one line, so a long line cannot become two rows", "maxLines = 1" in status)
    }

    /** Absent at zero rather than reading `0 waiting`, which is the criterion in the issue. */
    @Test
    fun `an empty queue states nothing rather than a zero`() {
        assertTrue(
            "the count is drawn only where there is a count",
            "area.queueLength > 0 -> pluralStringResource(" in statusLine(),
        )
    }

    /**
     * `CLAUDE.md` rule 10, and the thing the focus group was explicit about: the count is
     * text in the card's own ink, not a badge, not a dot and not a color.
     */
    @Test
    fun `the count carries no badge, no dot and no color of its own`() {
        val status = statusLine()
        assertTrue("the card's caption role", "style = type.caption" in status)
        assertTrue("and the card's own secondary ink", "color = colors.inkSecondary" in status)
        assertFalse(
            "a count on a filled shape is a badge, whatever it is called",
            "background(" in status || "Badge" in status || "CircleShape" in status,
        )
        assertFalse("and an accent here would be a colored marker", "accent" in status)
    }

    // ------------------------------------------------------------------ speech

    /**
     * Speech has no line budget, so it takes both facts.
     *
     * The visible row chooses between the count and the last active line because the card
     * has four rows; TalkBack reads a sentence and can hold everything. This is the one
     * place the two are deliberately not identical, which is worth an assertion so that a
     * later reader does not "fix" it by trimming the description to match the row.
     */
    @Test
    fun `the spoken description carries the count whatever the row chose`() {
        val idleWithQueue = model(activeItemId = null, queueLength = 3, daysSinceLastEvent = 4)
        assertEquals(
            "Work. Pick what is next. 3 items waiting",
            areaCardDescription(idleWithQueue, idleTitle = "Pick what is next"),
        )
        val activeWithQueue = model(activeItemId = "i1", queueLength = 1)
        assertEquals(
            "Work. Send the deck. 1 item waiting",
            areaCardDescription(activeWithQueue, idleTitle = "Add your first item"),
        )
        val activeWithNone = model(activeItemId = "i1", queueLength = 0)
        assertEquals(
            "a zero is absent in speech too",
            "Work. Send the deck",
            areaCardDescription(activeWithNone, idleTitle = "Add your first item"),
        )
    }

    // ------------------------------------------------------------------ helpers

    private fun model(
        activeItemId: String?,
        queueLength: Int,
        daysSinceLastEvent: Int = 0,
    ) = AreaCardModel(
        id = "work",
        name = "Work",
        colorHex = "#2D7FF9",
        activeItemId = activeItemId,
        activeItemTitle = activeItemId?.let { "Send the deck" },
        activeItemFirstStep = null,
        queueLength = queueLength,
        completedCount = 2,
        daysSinceLastEvent = daysSinceLastEvent,
        focusMinutesRemaining = null,
    )

    private fun card(): String =
        File("src/main/java/com/kamsiob/claritynow/ui/areas/AreaCard.kt").readText()

    private fun widget(): String =
        File("src/main/java/com/kamsiob/claritynow/widget/AreaWidgetFrame.kt").readText()

    private fun strings(): String = File("src/main/res/values/strings.xml").readText()

    /** The body of the one composable that draws row four. */
    private fun statusLine(): String =
        card().substringAfter("private fun StatusLine(").substringBefore("\n/**")
}
