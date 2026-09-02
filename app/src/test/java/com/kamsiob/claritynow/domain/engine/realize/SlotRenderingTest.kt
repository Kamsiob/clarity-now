package com.kamsiob.claritynow.domain.engine.realize

import com.kamsiob.claritynow.domain.engine.catalog.Purpose
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Every rule in CLARITY_LOGIC_ENGINE.md 7.2, one test each.
 *
 * These are the rules a reader notices being broken before they notice anything else in
 * the app. `1 items` and `78%` are both small, both instantly visible, and both are the
 * kind of thing that makes a person stop believing the rest of the sentence.
 */
class SlotRenderingTest {

    private fun render(text: String, purpose: Purpose, vararg slots: Slot): String? =
        SlotRenderer.render(text, slots.associateBy { it.key }, purpose)

    @Test
    fun `percentages render as the word, never as a sign`() {
        assertEquals(
            "Work took 78 percent of what you did.",
            render("{areaName} took {pct} of what you did.", Purpose.PULSE, Slot.Text("areaName", "Work"), Slot.Percent("pct", 78)),
        )
    }

    @Test
    fun `a count of one renders the singular noun`() {
        assertEquals(
            "You added one thing.",
            render("You added {n} things.", Purpose.PULSE, Slot.Count("n", 1, "thing", "things")),
        )
        assertEquals(
            "Work is empty. It held 1 thing on Sunday.",
            render(
                "{areaName} is empty. It held {n} things on Sunday.",
                Purpose.REPORT_OBSERVATION,
                Slot.Text("areaName", "Work"),
                Slot.Count("n", 1, "thing", "things"),
            ),
        )
    }

    @Test
    fun `a count of one in front of a plural verb drops the line rather than half agreeing`() {
        // `1 item are queued` is worse than saying nothing, and the noun cannot be fixed
        // without the verb.
        assertNull(render("{n} items are queued.", Purpose.REPORT_OBSERVATION, Slot.Count("n", 1, "item", "items")))
    }

    @Test
    fun `a count of one in front of a plural the slot cannot govern drops the line`() {
        // The binding counts swaps and the line counts times. Rather than `one times`,
        // nothing is rendered and the bench offers another line.
        assertNull(render("You swapped {n} times.", Purpose.REPORT_OBSERVATION, Slot.Count("n", 1, "swap", "swaps")))
    }

    @Test
    fun `a count of one in front of no noun at all is left alone`() {
        assertEquals(
            // The noun is left alone, which is what this test is about. The opening
            // letter is not: a sentence that begins with a spelled number begins with a
            // capital, the same as one that begins with an authored word. See
            // `a sentence opening on a spelled number is capitalized`.
            "One in, two out.",
            render(
                "{n} in, {m} out.",
                Purpose.PULSE,
                Slot.Count("n", 1, "thing", "things"),
                Slot.Count("m", 2, "thing", "things"),
            ),
        )
    }

    @Test
    fun `two through nine are words in Pulse and Momentum and digits in the Report`() {
        val line = "{n} things happened."
        val four = Slot.Count("n", 4, "thing", "things")
        // Capitalized because the template opens on the marker. The word spelling is
        // what this test pins; the case is pinned one test down.
        assertEquals("Four things happened.", render(line, Purpose.PULSE, four))
        assertEquals("Four things happened.", render(line, Purpose.MOMENTUM_HEADLINE, four))
        assertEquals("Four things happened.", render(line, Purpose.AREAS_BANNER, four))
        assertEquals("4 things happened.", render(line, Purpose.REPORT_OBSERVATION, four))
        assertEquals("4 things happened.", render(line, Purpose.REPORT_HEADLINE, four))
    }

    /**
     * The Areas caption read "one completed this week" under a serif headline ending in
     * a period, on the screen a person opens most, and 177 corpus lines open with a
     * numeric slot on a word spelling surface.
     */
    @Test
    fun `a sentence opening on a spelled number is capitalized`() {
        val one = Slot.Count("n", 1, "thing", "things")
        assertEquals(
            "One completed this week",
            render("{n} completed this week", Purpose.AREAS_BANNER, one),
        )
        // A digit needs no help and must not be touched.
        assertEquals(
            "12 completed this week",
            render("{n} completed this week", Purpose.AREAS_BANNER, Slot.Count("n", 12, "thing", "things")),
        )
        // A slot that is not first leaves the authored opening exactly as written.
        assertEquals(
            "you completed one thing",
            render("you completed {n} thing", Purpose.AREAS_BANNER, one),
        )
    }

    /**
     * **A name is never recased.** An area a person called `iPhone stuff` is their own
     * capitalization and not a typo, and this is the reason the rule is scoped to a
     * number rather than applied to whatever lands at index zero.
     */
    @Test
    fun `a name in the opening slot keeps the case its owner gave it`() {
        assertEquals(
            "iPhone stuff took the week.",
            render("{areaName} took the week.", Purpose.AREAS_BANNER, Slot.Text("areaName", "iPhone stuff")),
        )
    }

    @Test
    fun `ten and above are always digits`() {
        val line = "{n} things happened."
        assertEquals("12 things happened.", render(line, Purpose.PULSE, Slot.Count("n", 12, "thing", "things")))
        assertEquals("12 things happened.", render(line, Purpose.REPORT_OBSERVATION, Slot.Count("n", 12, "thing", "things")))
    }

    @Test
    fun `days render by magnitude, at the boundaries the corpus ladder uses`() {
        fun days(value: Int) = SlotRenderer.renderValue(Slot.Days("ageDays", value), Purpose.PULSE)
        assertEquals("yesterday", days(1))
        assertEquals("two days", days(2))
        assertEquals("nine days", days(9))
        assertEquals("13 days", days(13))
        // Fourteen is where persistence stage 3 begins, and where the unit changes.
        assertEquals("two weeks", days(14))
        assertEquals("four weeks", days(29))
        // Thirty is where stage 4 begins.
        assertEquals("one month", days(30))
        assertEquals("two months", days(60))
    }

    @Test
    fun `days render in the Report register too`() {
        assertEquals("9 days", SlotRenderer.renderValue(Slot.Days("ageDays", 9), Purpose.REPORT_OBSERVATION))
        assertEquals("2 weeks", SlotRenderer.renderValue(Slot.Days("ageDays", 15), Purpose.REPORT_OBSERVATION))
    }

    @Test
    fun `a date reference renders a month name and never a numeric date`() {
        assertEquals(
            "The most since March.",
            render("The most since {sinceRef}.", Purpose.REPORT_HEADLINE, Slot.DateRef("sinceRef", "2026-03-02", "March")),
        )
    }

    @Test
    fun `a missing slot refuses to render rather than leaving a marker on the screen`() {
        assertNull(render("{itemTitle} has been active for {ageDays}.", Purpose.PULSE, Slot.Text("itemTitle", "Taxes")))
    }

    @Test
    fun `a template with no markers is returned unchanged`() {
        assertEquals("A still week.", render("A still week.", Purpose.REPORT_HEADLINE))
    }
}
