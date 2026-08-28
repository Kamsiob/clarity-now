package com.kamsiob.claritynow.data.export

import com.kamsiob.claritynow.domain.FixedClarityClock
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneId

/**
 * The quiet line in Settings, and every case where it stays quiet.
 * MASTER_BUILD_PROMPT 14b.7.
 *
 * The two that matter are the last two. A person with no items never sees it however
 * long they have had the app, and a person with a month of items sees it once even
 * though they have never exported anything, which is the case the whole feature
 * exists for: most people never export, so a rule that only measured from the last
 * export would be silent forever for exactly the people it is meant to reach.
 */
class ExportReminderTest {

    private val zone = ZoneId.of("UTC")
    private val now = 1_800_000_000_000L
    private val clock = FixedClarityClock(now, zone)

    private fun daysAgo(days: Long): Long = now - days * 86_400_000L

    @Test
    fun `no data means no line, however long ago the last export was`() {
        assertFalse(ExportReminder.isDue(clock, lastExportAt = daysAgo(400), dataSince = null))
        assertFalse(ExportReminder.isDue(clock, lastExportAt = null, dataSince = null))
    }

    @Test
    fun `a recent export means no line`() {
        assertFalse(ExportReminder.isDue(clock, daysAgo(29), dataSince = daysAgo(200)))
    }

    @Test
    fun `exactly thirty days is not more than thirty days`() {
        assertFalse(ExportReminder.isDue(clock, daysAgo(30), dataSince = daysAgo(200)))
        assertTrue(ExportReminder.isDue(clock, daysAgo(31), dataSince = daysAgo(200)))
    }

    @Test
    fun `an old export with real data shows the line`() {
        assertTrue(ExportReminder.isDue(clock, daysAgo(90), dataSince = daysAgo(200)))
    }

    @Test
    fun `a new person who has never exported is left alone`() {
        assertFalse(ExportReminder.isDue(clock, lastExportAt = null, dataSince = daysAgo(3)))
        assertFalse(ExportReminder.isDue(clock, lastExportAt = null, dataSince = daysAgo(30)))
    }

    @Test
    fun `a month of history and no export ever shows the line`() {
        assertTrue(ExportReminder.isDue(clock, lastExportAt = null, dataSince = daysAgo(31)))
    }

    /** A clock that moved backwards is a reason to say nothing, never to say it twice. */
    @Test
    fun `a date in the future shows nothing`() {
        assertFalse(ExportReminder.isDue(clock, lastExportAt = now + 86_400_000L, dataSince = daysAgo(200)))
    }
}
