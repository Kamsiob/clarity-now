package com.kamsiob.claritynow.ui.areas

import com.kamsiob.claritynow.data.repo.FocusCountdown
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * The in session area card. MASTER_BUILD_PROMPT section 10, design-v3.md 10.3,
 * issue #2.
 *
 * "While a session runs and `focusHighlightEnabled` is on, that area's card shows the
 * intensified wash and a live countdown. **There is no bar.**" The absence of a bar is
 * not something a test can assert, but the shape of what the card is handed is: whole
 * minutes and an area id, with no planned total and no fraction anywhere in the value,
 * so the ratio a bar would be drawn from does not exist on this side of the boundary.
 */
class AreasFocusTest {

    private fun countdown(
        areaId: String = "area-1",
        plannedSeconds: Int = 1_500,
        remainingSeconds: Int,
    ) = FocusCountdown(
        sessionId = "session-1",
        areaId = areaId,
        itemId = "item-1",
        startedAt = 0L,
        plannedSeconds = plannedSeconds,
        endsAtMillis = plannedSeconds * 1_000L,
        remainingSeconds = remainingSeconds,
    )

    // ---------------------------------------------------------------------------
    // The setting. MASTER_BUILD_PROMPT section 10.
    // ---------------------------------------------------------------------------

    /**
     * `focusHighlightEnabled` governs the wash and the countdown together, because
     * they are one state rather than two decorations. Off means the card is an
     * ordinary active card while the session runs perfectly normally elsewhere.
     */
    @Test
    fun `the highlight setting turns the whole state off`() {
        val running = countdown(remainingSeconds = 900)
        assertNull(focusHighlightFor(running, highlightEnabled = false))
        assertEquals(
            FocusHighlight(areaId = "area-1", minutesRemaining = 15),
            focusHighlightFor(running, highlightEnabled = true),
        )
    }

    /** No session on this device, nothing on any card. */
    @Test
    fun `no session means no highlight`() {
        assertNull(focusHighlightFor(countdown = null, highlightEnabled = true))
    }

    // ---------------------------------------------------------------------------
    // What the countdown reads. design-v3.md 10.3 and 11.3.
    // ---------------------------------------------------------------------------

    /**
     * Rounded up, so a session with forty seconds left reads one minute rather than
     * zero and the row only reaches zero when the time is actually gone. The
     * notification rounds the same way: the card and the shade are read seconds apart
     * and one saying seven while the other says eight is how a person stops trusting
     * both.
     */
    @Test
    fun `minutes round up`() {
        assertEquals(25, focusMinutesLeft(1_500))
        assertEquals(25, focusMinutesLeft(1_499))
        assertEquals(15, focusMinutesLeft(870))
        assertEquals(1, focusMinutesLeft(60))
        assertEquals(1, focusMinutesLeft(40))
        assertEquals(1, focusMinutesLeft(1))
        assertEquals(0, focusMinutesLeft(0))
    }

    /**
     * A session whose planned time has run out is over, and the card says nothing
     * about it until it is resolved. `In focus, 0 minutes left` would be a sentence
     * that was false for as long as it took the person to come back to it, and
     * Addendum 01 4e is the reason a false one is worse here than none: nothing on this
     * surface may imply anything about how a session went.
     */
    @Test
    fun `an elapsed session leaves the card alone`() {
        assertNull(focusHighlightFor(countdown(remainingSeconds = 0), highlightEnabled = true))
    }

    /**
     * The area is carried rather than assumed, because a merged log can hold a running
     * session per device and only this phone's session belongs on this phone's cards.
     */
    @Test
    fun `the highlight names the area it belongs to`() {
        val highlight = focusHighlightFor(
            countdown(areaId = "area-7", remainingSeconds = 300),
            highlightEnabled = true,
        )
        assertEquals("area-7", highlight?.areaId)
        assertEquals(5, highlight?.minutesRemaining)
    }

    /**
     * The card is given no planned total, so nothing downstream can compute the
     * fraction a progress bar needs. MASTER_BUILD_PROMPT section 10 and design-v3.md
     * 10.3 both forbid the bar; this is what makes the forbidding structural.
     */
    @Test
    fun `the card is handed no denominator`() {
        val fields = FocusHighlight::class.java.declaredFields
            // Statics and synthetics are the compiler's, not the design's. The Compose
            // plugin adds a stability constant to classes it can prove immutable.
            .filterNot { it.isSynthetic || java.lang.reflect.Modifier.isStatic(it.modifiers) }
            .map { it.name }
            .sorted()
        assertEquals(listOf("areaId", "minutesRemaining"), fields)
    }
}
