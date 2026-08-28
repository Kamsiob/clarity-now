package com.kamsiob.claritynow.data.export

import com.kamsiob.claritynow.domain.ClarityClock
import com.kamsiob.claritynow.domain.daysBetween

/**
 * Whether Settings shows the quiet line about the last export.
 * MASTER_BUILD_PROMPT 14b.7, Addendum 01 4h.
 *
 * **In Settings only. Never a notification, never a nag, never a badge, never a
 * card on Areas.** That is the whole of the specification's constraint on this, and
 * the reason the rule is a function here rather than a condition inside a screen:
 * one place to read means one place a second surface could have been added, and it
 * would be visible.
 *
 * **The rule has two halves and the second one is the one that is easy to lose.**
 * Thirty days is not enough on its own. A person who installed the app last week
 * and has three items would otherwise be told to protect data they have barely
 * started making, which is a nag in the exact shape the specification forbids. So
 * the line waits for real data, and real data means at least one item they have not
 * deleted. Areas are not enough: an area is a name and a color and takes ten seconds
 * to make again. An item is a thought they had once and would not get back.
 *
 * **The anchor when nothing has ever been exported is when the data started.** Not
 * the install, and not immediately. Most people never export anything, so a rule
 * that only measured from the last export would be silent forever for exactly the
 * people it exists for; and a rule with no anchor at all would speak on their first
 * day. Measuring from the oldest thing they still have gives the line the same
 * meaning in both cases: it has been more than a month since anything was made
 * safe.
 */
object ExportReminder {

    const val DAYS: Int = 30

    /**
     * @param lastExportAt the instant of the last successful export, or null.
     * @param dataSince when the oldest item a person still has was created, or null
     *   when there is nothing worth keeping. `ClarityRepository.dataWorthKeepingSince`
     *   answers it.
     */
    fun isDue(clock: ClarityClock, lastExportAt: Long?, dataSince: Long?): Boolean {
        if (dataSince == null) return false
        val anchor = lastExportAt ?: dataSince
        return clock.daysBetween(anchor, clock.nowMillis()) > DAYS
    }
}
