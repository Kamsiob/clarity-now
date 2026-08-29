package com.kamsiob.claritynow.ui.report

import com.kamsiob.claritynow.domain.report.ClarityReport
import com.kamsiob.claritynow.domain.report.ReportFixture
import com.kamsiob.claritynow.domain.report.ReportNote
import com.kamsiob.claritynow.domain.report.ReportObservation
import com.kamsiob.claritynow.domain.report.ReportSection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The three parts of the Report screen that are decisions rather than drawing.
 *
 * A composable cannot be asserted without an instrumented run, and the two things on this
 * surface most likely to be wrong are not drawing at all: the reveal's timing, which
 * `design-v3.md` 8.2 item 12 caps at a number, and the plain text the copy control puts on
 * the clipboard, which is the one place a sentence could be assembled by accident.
 */
class ReportPageTest {

    /**
     * `design-v3.md` 8.2 item 12: the whole reveal is under 1.4 seconds.
     *
     * The one number in that entry that is a limit rather than a rate, so it is the one
     * worth a test. A later session adding a block to the page cannot push the reveal past
     * it without this failing, which is exactly what the stagger cap is for.
     */
    @Test
    fun `the reveal finishes inside the ceiling design-v3 gives it`() {
        assertTrue(
            "the reveal ran to ${totalRevealMillis(reduced = false)}ms",
            totalRevealMillis(reduced = false) < CEILING_MILLIS,
        )
    }

    /** 8.3. Reduce motion is one crossfade, so the whole sequence is that crossfade. */
    @Test
    fun `reduce motion collapses the reveal to one crossfade`() {
        assertEquals(REDUCED_MILLIS.toLong(), totalRevealMillis(reduced = true))
    }

    /**
     * The stagger stops growing, which is what makes the ceiling hold on a long report.
     *
     * A report can carry a first week note, three sideheads, a pattern, a closing line and
     * a footer. Every block past the cap arrives with the one before it.
     */
    @Test
    fun `the stagger stops growing past the cap`() {
        assertTrue(sectionAt(0) < sectionAt(1))
        assertEquals(sectionAt(MAX_STAGGER_STEPS), sectionAt(MAX_STAGGER_STEPS + 1))
        assertEquals(sectionAt(MAX_STAGGER_STEPS), sectionAt(MAX_STAGGER_STEPS + 20))
    }

    /**
     * Adjacent observations from one section are read under one sidehead.
     *
     * The composer already groups them, per `ReportComposer.arrange`, and the screen walks
     * that order rather than re-sorting it. A repeated sidehead reads as a bug and the
     * sideheads are the only structure a page of prose has.
     */
    @Test
    fun `one sidehead covers the run of observations beneath it`() {
        val grouped = groupedSections(
            listOf(
                observation(ReportSection.YOUR_WEEK, "One."),
                observation(ReportSection.YOUR_WEEK, "Two."),
                observation(ReportSection.FOCUS, "Three."),
            ),
        )

        assertEquals(listOf(ReportSection.YOUR_WEEK, ReportSection.FOCUS), grouped.map { it.first })
        assertEquals(listOf("One.", "Two."), grouped[0].second.map { it.rendered })
        assertEquals(listOf("Three."), grouped[1].second.map { it.rendered })
    }

    /** The screen never re-sorts, so an order the composer produced survives it exactly. */
    @Test
    fun `grouping preserves the order the composer decided`() {
        val observations = listOf(
            observation(ReportSection.WHAT_YOU_SAID, "Said."),
            observation(ReportSection.YOUR_WEEK, "Week."),
        )
        val grouped = groupedSections(observations)

        assertEquals(
            observations.map { it.candidate.rendered },
            grouped.flatMap { (_, lines) -> lines.map { it.rendered } },
        )
    }

    /**
     * The clipboard carries every sentence the engine wrote and invents none of its own.
     *
     * `MASTER_BUILD_PROMPT.md` 12.3 makes copy the app's only integration surface with
     * anything else, and 11.4 forbids building a sentence by concatenation at runtime. The
     * assertion below is that test stated directly: every line of the output is either one
     * of the labels the caller resolved out of `strings.xml` or one of the finished strings
     * on the report, and there is nothing else in it at all.
     */
    @Test
    fun `the plain text is the report's own sentences and the fixed labels`() {
        val report = report()
        val text = reportPlainText(report, CAPTION, closing = CLOSING, labels = LABELS)

        val engineLines = setOf(
            HEADLINE,
            FIRST,
            SECOND,
            PATTERN,
            FIRST_WEEK,
            GENERATED,
            BASIS,
            CLOSING.line,
        )
        val fixedLabels = setOf(
            EYEBROW,
            CAPTION,
            SIDEHEADS.getValue(ReportSection.YOUR_WEEK),
            SIDEHEADS.getValue(ReportSection.FOCUS),
            PATTERN_SIDEHEAD,
            CLOSING_EYEBROW,
        )

        val lines = text.lines().filter { it.isNotBlank() }
        assertEquals(engineLines, lines.filterNot { it in fixedLabels }.toSet())
        assertTrue(lines.all { it in engineLines || it in fixedLabels })
    }

    /** The reading order is the page's order, so the headline leads and the basis ends. */
    @Test
    fun `the plain text is in the order the page is read`() {
        val text = reportPlainText(report(), CAPTION, closing = CLOSING, labels = LABELS)
        val positions = listOf(EYEBROW, HEADLINE, CAPTION, FIRST_WEEK, FIRST, PATTERN, GENERATED, BASIS)
            .map { text.indexOf(it) }

        assertTrue("one of the blocks is missing: $positions", positions.none { it < 0 })
        assertEquals(positions.sorted(), positions)
    }

    /**
     * `design-v3.md` 11.1 item 8: after an accept, the stored first person line is what
     * shows and the nominal offer is gone.
     *
     * The one visible thing accepting does, and the reason `ReportClosing.line` is derived
     * rather than held: there is no state in which the flag is set and the sentence has not
     * changed, because there is no second place holding the sentence.
     */
    @Test
    fun `accepting replaces the offer with the plan's stored line`() {
        assertEquals(OFFERED, CLOSING.line)
        assertEquals(COMMITTED, CLOSING.copy(accepted = true).line)
    }

    /**
     * `CORPUS_2_REPORT.md` 4.6. A closing line with no plan in it has nothing to accept and
     * nothing to refuse, so neither control is drawn.
     */
    @Test
    fun `a closing line with no plan offers no answer`() {
        val quiet = "A quiet week, and that is all of it."
        val closing = ReportClosing(offeredLine = quiet, plan = null)

        assertFalse(closing.offersPlan)
        assertTrue(CLOSING.offersPlan)
    }

    /**
     * The clipboard carries what a person can read, which after an accept is the stored
     * line. `MASTER_BUILD_PROMPT.md` 12.3.
     *
     * It follows from `line` being derived and is asserted anyway, because copy is the only
     * surface in this app that hands a sentence to anything outside it, and a stale offer
     * leaving through it would be the one copy of the plan nobody could correct.
     */
    @Test
    fun `the clipboard carries the stored line once the plan is accepted`() {
        val text = reportPlainText(report(), CAPTION, CLOSING.copy(accepted = true), LABELS)

        assertTrue(text.contains(COMMITTED))
        assertFalse(text.contains(OFFERED))
    }

    // ------------------------------------------------------------------ fixtures

    private fun observation(section: ReportSection, rendered: String) = ReportObservation(
        section = section,
        candidate = ReportFixture.observation(family = "quietWeek", rendered = rendered),
    )

    private fun report() = ClarityReport(
        weekStartKey = ReportFixture.WEEK_START_KEY,
        headline = ReportFixture.observation(family = "quietWeek", rendered = HEADLINE),
        observations = listOf(
            observation(ReportSection.YOUR_WEEK, FIRST),
            observation(ReportSection.FOCUS, SECOND),
        ),
        pattern = ReportFixture.observation(family = "weekdayShape", rendered = PATTERN),
        basis = ReportFixture.observation(family = "bs", rendered = BASIS),
        generated = ReportNote("footer.generated", GENERATED),
        firstWeekNote = ReportNote("ed.first.01", FIRST_WEEK),
        totals = emptyList(),
        numbers = emptyMap(),
        dropped = emptyList(),
    )

    private companion object {
        const val CEILING_MILLIS = 1_400L

        const val HEADLINE = "A steady week, mostly in one place."
        const val FIRST = "Work held nine of the twelve events."
        const val SECOND = "Two sessions, both in the morning."
        const val PATTERN = "Tuesdays are your quietest day."
        const val FIRST_WEEK = "Your first week. There is not much to compare against yet."
        const val GENERATED = "Generated on your device"
        const val BASIS = "Based on three Pulse responses."
        const val EYEBROW = "Clarity Report, week of March 8"
        const val CAPTION = "12 events, 5 completed, 3 added"
        const val PATTERN_SIDEHEAD = "Pattern"
        const val CLOSING_EYEBROW = "One thing"

        const val OFFERED = "One session on Tuesday would even the week out."
        const val COMMITTED = "My one thing before Tuesday is over: one session in Reading."

        val CLOSING = ReportClosing(
            offeredLine = OFFERED,
            plan = ClosingPlan(id = "plan-2026-03-08", committedLine = COMMITTED),
        )

        val SIDEHEADS = mapOf(
            ReportSection.YOUR_WEEK to "Your week, honestly",
            ReportSection.WHAT_YOU_SAID to "What you said",
            ReportSection.FOCUS to "Focus",
        )

        val LABELS = ReportLabels(
            eyebrow = EYEBROW,
            sideheads = SIDEHEADS,
            patternSidehead = PATTERN_SIDEHEAD,
            closingEyebrow = CLOSING_EYEBROW,
        )
    }
}
