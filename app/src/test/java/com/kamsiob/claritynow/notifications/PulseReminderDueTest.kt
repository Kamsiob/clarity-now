package com.kamsiob.claritynow.notifications

import com.kamsiob.claritynow.data.event.ReflectionPeriod
import com.kamsiob.claritynow.domain.replay.PulseEntryState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * The one rule the daily reminder has. MASTER_BUILD_PROMPT 12.1, design-v3.md 12.1,
 * issue #4's second named risk.
 *
 * "Posted only if that day's entry exists and is unanswered. Never when IDLE." Issue #4
 * lists getting this wrong as a risk in its own right, because a notification on a day
 * the engine chose to stay silent turns designed silence into a broken promise, and it
 * is the kind of defect that ships: the check is one line, and everything looks correct
 * without it.
 *
 * **So the check is a type rather than a line, and this is the test of the type.**
 * `PulseReminderPoster.post` takes a [PulseReminderDue] and nothing else can be passed
 * to it, the constructor is private, and the only way to obtain one is the function
 * below. If these three assertions hold, no caller anywhere can post on a day that is
 * not READY, however the calling code is later rearranged.
 */
class PulseReminderDueTest {

    private fun entry(
        dateKey: String = "2026-06-10",
        responseKey: String? = null,
    ) = PulseEntryState(
        id = "pulse-1",
        dateKey = dateKey,
        family = "quietDay",
        stage = 1,
        register = "plain",
        variantKey = "quietDay.s1.v1",
        observation = "an observation the engine wrote",
        question = "a question the engine wrote",
        reflectionPeriod = ReflectionPeriod.YESTERDAY,
        generatedAt = 0L,
        responseKey = responseKey,
        responseLabel = responseKey?.let { "Yes" },
        answeredAt = responseKey?.let { 1L },
        lastEventLamport = 1L,
    )

    /**
     * A silent day, and every day before the app was installed. There is no entry and
     * there is therefore no token, so nothing can be posted.
     */
    @Test
    fun `an IDLE day produces no token`() {
        assertNull(PulseReminderDue.from(null))
    }

    /** Generated and waiting. The one state a reminder may be posted in. */
    @Test
    fun `a READY day produces a token carrying that day`() {
        val due = PulseReminderDue.from(entry())

        assertNotNull(due)
        assertEquals("2026-06-10", due?.dateKey)
    }

    /**
     * Already answered. Asking again would be the app failing to notice what somebody
     * did, which is the one thing it is supposed to be good at.
     */
    @Test
    fun `an ANSWERED day produces no token`() {
        assertNull(PulseReminderDue.from(entry(responseKey = "yes")))
    }
}
