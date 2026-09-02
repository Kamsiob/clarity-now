package com.kamsiob.claritynow.data.repo

import com.kamsiob.claritynow.data.event.ReportGenerated
import com.kamsiob.claritynow.data.event.ReportSectionSnapshot
import com.kamsiob.claritynow.domain.query.TrailTestLog
import com.kamsiob.claritynow.domain.query.at
import com.kamsiob.claritynow.domain.replay.ClarityReplay
import com.kamsiob.claritynow.domain.report.ReportSchedule
import java.io.File
import java.time.ZoneId
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The report write path, and 12.3's cadence, which is one rule with two readers.
 * `MASTER_BUILD_PROMPT.md` 11.3 step 9 and 12.3, and issue #64.
 *
 * `REPORT_GENERATED` was in the catalog, the reducer folded it, the Trail rendered a row
 * for it and `FiringHistory` read it for 7.6's ninety day exclusion. What was missing was a
 * method on the only writer in the app, so the history page could never hold anything and
 * the Report could not vary itself week to week. This is the sibling of `PlanWritePathTest`
 * and it is written the same way, because the two events are written from one place under
 * one lock and the guards on them are the interesting part.
 *
 * ## The two week keys, which is where this could still go wrong
 *
 * A report is **generated** in a calendar week and **describes** the seven days before the
 * day it was generated on, and those are different spans on every day but Sunday. The event
 * is filed under the Sunday, because that is what the merge rule in `docs/EVENT_FORMAT.md`
 * resolves duplicates by and what the cadence counts; the window's first day rides along so
 * a past report can name the span its own eyebrow named. Keyed the other way round, a phone
 * opened on Wednesday and a laptop opened on Friday would file two reports for one week and
 * neither would look like a duplicate to anything.
 */
class ReportWritePathTest {

    // ------------------------------------------------------------------ the seam

    @Test
    fun `the only writer in the app has a method for the report event`() {
        assertTrue(
            "without this the history page is empty forever and FiringHistory never " +
                "learns what the Report said",
            repository().contains("suspend fun recordReportGenerated("),
        )
        assertTrue(
            "the write goes through the one write path",
            "commitLocked(payload)" in body("suspend fun recordReportGenerated("),
        )
    }

    /**
     * Regenerating is a control on the screen and composing happens on every open, so the
     * guards are not an edge case: they are what the method does almost every time.
     */
    @Test
    fun `a second report for one week is refused before it reaches the log`() {
        val write = body("suspend fun recordReportGenerated(")
        assertTrue(
            "a regenerate must not file the same week twice",
            "_state.value.reports[payload.weekStartKey]?.let { return@withLock it }" in write,
        )
        assertTrue(
            "and neither must an open on a later day of the same week",
            "reportFiledSince(weekBeganAtMillis)?.let { return@withLock it }" in write,
        )
    }

    /**
     * One predicate, called by the writer and by the screen's question.
     *
     * 12.3's cadence has exactly one right answer at any moment. Asked two ways it would
     * eventually be answered two ways, and the failure would be silent: a duplicate row in
     * a list nobody checks against the log.
     */
    @Test
    fun `the cadence is one function and both readers call it`() {
        assertTrue(
            "the writer decides with it",
            "reportFiledSince(weekBeganAtMillis)" in body("suspend fun recordReportGenerated("),
        )
        assertTrue(
            "and so does the coordinator's question",
            "repository.reportFiledSince(week.currentWeekStartMillis)" in coordinator(),
        )
        assertFalse(
            "asking the log a second way here is how the two answers start to differ",
            "reportsGeneratedBetween" in coordinator(),
        )
    }

    /** Composing without recording is what left the history page empty for ten phases. */
    @Test
    fun `generating a report records it`() {
        assertTrue(
            "11.3 step 9 is a write and this is where it happens",
            "recordReportIfFirstThisWeek(outcome, week)" in coordinator(),
        )
        assertTrue(
            "and it goes to the only writer in the app",
            "repository.recordReportGenerated(" in coordinator(),
        )
    }

    // ------------------------------------------------------------------ the two keys

    /**
     * A Wednesday, where the week and the window are seven days and four days apart.
     *
     * Wednesday 2026-03-11. The calendar week began on Sunday the 8th; the seven days
     * described are the 4th to the 10th inclusive.
     */
    @Test
    fun `the week a report is filed under is the Sunday and not the window`() {
        val week = ReportSchedule.weekAt(wednesday(), ZONE)
        assertEquals("the Sunday that began the calendar week", "2026-03-08", week.currentWeekStartKey)
        assertEquals("the first of the seven days described", "2026-03-04", week.weekStartKey)
        assertNotEquals(
            "these are the same only on a Sunday, and a test that used one would pass there",
            week.currentWeekStartKey,
            week.weekStartKey,
        )
    }

    /** And on a Sunday they agree, which is the case the other reading would have passed. */
    @Test
    fun `on a Sunday the week and the window begin on the same day`() {
        val week = ReportSchedule.weekAt(sunday(), ZONE)
        assertEquals("2026-03-08", week.currentWeekStartKey)
        assertEquals("2026-03-01", week.weekStartKey)
    }

    // ------------------------------------------------------------------ the fold

    /**
     * Two events for one week fold to one report and raise no conflict.
     *
     * The repository refuses the second before it is written, so this is the backstop and
     * the case that matters after a merge: the id is derived from the week, so a report
     * filed by a second device is filed under the id this one would have used.
     */
    @Test
    fun `a report written twice for one week folds to one report and no conflict`() {
        val log = TrailTestLog()
        log.add(at(1), report())
        log.add(at(1, hour = 11), report())
        val state = ClarityReplay.replay(log.events())
        assertEquals("one week, one report", 1, state.reports.size)
        assertTrue("the same report twice is not a conflict", state.conflicts.isEmpty())
    }

    /** Everything the history page draws survives the fold, including the words. */
    @Test
    fun `the projection carries the described window and the headline it led with`() {
        val log = TrailTestLog()
        log.add(at(1), report())
        val stored = ClarityReplay.replay(log.events()).reports.getValue(WEEK)
        assertEquals("filed under the Sunday", WEEK, stored.weekStartKey)
        assertEquals("and naming the span it described", WINDOW, stored.windowStartKey)
        assertEquals(
            "the headline's keys cannot be turned back into prose, so the words are stored",
            HEADLINE,
            stored.headlineText,
        )
        assertEquals(listOf(OBSERVATION), stored.sections.map { it.text })
    }

    // ------------------------------------------------------------------ helpers

    private fun report() = ReportGenerated(
        reportId = "report:$WEEK",
        weekStartKey = WEEK,
        windowStartKey = WINDOW,
        headlineKey = "steadyPace",
        headlineVariantKey = "hd.steady.01",
        headlineText = HEADLINE,
        renderedSections = listOf(
            ReportSectionSnapshot(
                sectionKey = "yourWeek",
                sidehead = "Your week, honestly",
                text = OBSERVATION,
                familyKey = "intakeVsOutput",
                variantKey = "ob.flow.s1.l08",
                escalationStage = 1,
                register = "PLAIN",
            ),
        ),
        factSnapshot = mapOf("completions" to "5"),
    )

    private fun wednesday(): Long =
        ZonedDateTime.of(2026, 3, 11, 18, 30, 0, 0, ZONE).toInstant().toEpochMilli()

    private fun sunday(): Long =
        ZonedDateTime.of(2026, 3, 8, 9, 0, 0, 0, ZONE).toInstant().toEpochMilli()

    private fun repository(): String =
        File("src/main/java/com/kamsiob/claritynow/data/repo/ClarityRepository.kt").readText()

    private fun coordinator(): String =
        File("src/main/java/com/kamsiob/claritynow/ui/report/ReportCoordinator.kt").readText()

    private fun body(signature: String): String =
        repository().substringAfter(signature).substringBefore("\n    /**")

    private companion object {
        val ZONE: ZoneId = ZoneId.of("UTC")
        const val WEEK = "2026-03-08"
        const val WINDOW = "2026-03-04"
        const val HEADLINE = "A steady week."
        const val OBSERVATION = "Three things left Work this week and two arrived."
    }
}
