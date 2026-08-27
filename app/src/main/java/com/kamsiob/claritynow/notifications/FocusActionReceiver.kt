package com.kamsiob.claritynow.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.kamsiob.claritynow.data.repo.FOCUS_EXTENSION_SECONDS
import com.kamsiob.claritynow.data.repo.countdownAt
import com.kamsiob.claritynow.di.ClarityGraph
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * `Add 10 min` and `End`, performed from the shade without opening the app.
 * Addendum 01 5c, MASTER_BUILD_PROMPT 14b.5.
 *
 * **It writes through [com.kamsiob.claritynow.data.repo.ClarityRepository] and it is
 * not a second write path.** issue #32 names that as the risk this file carries, and
 * the shape below is the answer: nothing here builds an event, assigns a lamport or
 * touches a DAO. `Add 10 min` calls `extendFocus`, which is the same call the in app
 * control makes and writes the same `FOCUS_EXTENDED`, and `End` calls
 * `endFocusEarly`. A rule enforced in one screen is not a rule, which is why the one
 * at a time check and the already ended check live in the repository and are
 * exercised from here for free.
 *
 * **There is no confirmation on `End` and that is deliberate.**
 * MASTER_BUILD_PROMPT 10 puts a confirm behind the End session pill on the focus
 * screen past sixty seconds, and Addendum 01 5c requires this action to work without
 * opening the app. A confirm here could only be a second notification or a screen, so
 * the two requirements cannot both be met on this surface and the addendum is the
 * later document. A notification action is already a deliberate, discrete tap on a
 * labeled control rather than a gesture that can be completed by accident, which is
 * what the confirm on the pill exists to prevent.
 *
 * **Ending from here says nothing afterwards.** No completion notification is posted
 * and no summary arrives. A person who ended a session from the shade is not in the
 * app, and a notification telling them what just happened because of something they
 * did on purpose is the re-engagement notification MASTER_BUILD_PROMPT 13.4 rules
 * out. The Trail records it, and the Trail is where they will look if they look.
 *
 * `goAsync` because the work is a load, an append and a projection, and a receiver's
 * main thread window is not the place for any of it. The result is finished in a
 * `finally`, so a failure releases the receiver rather than holding the process.
 */
class FocusActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val sessionId = intent.getStringExtra(FocusIntents.EXTRA_SESSION_ID) ?: return
        val action = intent.action ?: return
        if (action == FocusIntents.ACTION_DISMISSED) {
            ClarityNotifications.onRunningDismissed(sessionId)
            return
        }
        if (action != FocusIntents.ACTION_ADD_TEN && action != FocusIntents.ACTION_END) return
        if (!ClarityGraph.isInstalled) return

        val appContext = context.applicationContext
        val pending = goAsync()
        scope.launch {
            try {
                val repository = ClarityGraph.repository
                repository.load()
                when (action) {
                    FocusIntents.ACTION_ADD_TEN ->
                        repository.extendFocus(sessionId, FOCUS_EXTENSION_SECONDS)

                    FocusIntents.ACTION_END -> {
                        // A real duration and never a shortfall against the plan.
                        // The same arithmetic the ring was drawn from, taken from
                        // the projection rather than from anything the notification
                        // was carrying, so a stale PendingIntent cannot decide how
                        // long somebody worked.
                        val elapsed = repository.state.value.focusSessions[sessionId]
                            ?.countdownAt(ClarityGraph.clock.nowMillis())
                            ?.elapsedSeconds
                            ?: 0
                        repository.endFocusEarly(sessionId, elapsed)
                        // The session is over, so the shade is cleared here rather
                        // than waiting for the next tick to notice.
                        FocusNotificationPoster(appContext).apply {
                            clearRunning()
                            clearTransitionWarning()
                        }
                    }
                }
            } finally {
                pending.finish()
            }
        }
    }

    private companion object {
        /**
         * Process lifetime, because the work outlives the receiver's own window and
         * `goAsync` is what keeps the process alive around it. A [SupervisorJob] so
         * one failed action cannot take the next one down with it.
         */
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }
}
