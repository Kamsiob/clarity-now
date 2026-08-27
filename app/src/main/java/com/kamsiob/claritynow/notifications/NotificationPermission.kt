package com.kamsiob.claritynow.notifications

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.kamsiob.claritynow.data.repo.FocusCountdown

/**
 * The two moments this app is allowed to ask for permission to post a notification.
 *
 * **This type exists to make the rule structural rather than remembered.**
 * MASTER_BUILD_PROMPT 13.4 says it twice: request `POST_NOTIFICATIONS`
 * contextually, the first time the user starts a focus session or enables a
 * reminder, **never at launch.** A rule like that written only in a comment drifts,
 * because the request is one line and launch is the easiest place to put it.
 *
 * There is no way to ask without naming one of these two moments, and neither of them
 * can be produced at launch: [FocusSessionStarted] needs the id of a session the
 * repository has already written a `FOCUS_STARTED` for, which does not exist until a
 * person has started one, and [PulseReminderEnabled] is a switch being turned on.
 * **Adding a third moment means editing this file, which is where the rule is
 * written**, and that is the whole point of the type.
 */
sealed interface NotificationMoment {

    /**
     * A focus session has just started. The id is the one
     * `ClarityRepository.startFocus` returned, which is null when the start was
     * refused, so a caller cannot reach this moment without a session.
     */
    data class FocusSessionStarted(val sessionId: String) : NotificationMoment

    /** The Pulse reminder has just been switched on. Phase 6, issue #4. */
    data object PulseReminderEnabled : NotificationMoment
}

/**
 * Asks for permission to post notifications, at one of the two moments
 * [NotificationMoment] allows and at no other.
 *
 * **A refusal costs nothing and is never mentioned.** The session runs, the screen
 * works, the ring depletes, and nothing anywhere tells the person what they are not
 * seeing. design-v3.md 11.4 forbids that sentence for the Live Update in particular
 * and the reasoning covers this too: a person cannot act on it and hearing it only
 * makes their phone feel worse. Nothing branches on the answer, which is why the
 * result callback below is empty rather than absent.
 *
 * **How often it can ask, and why nothing is stored to limit that.** The obvious
 * implementation keeps a "we have asked" flag so the prompt appears exactly once.
 * design-v3.md 15 asks for the obvious answer to be checked rather than taken, and
 * this one does not survive the check: from Android 13 the platform already caps the
 * prompt at two appearances for the life of the install and silently denies
 * afterwards, so the flag would buy one fewer dialog in exchange for a key in
 * DataStore that has to be erased, exported and reasoned about. It would also lie
 * after a person granted the permission and later revoked it, which is the one case
 * where asking again is correct.
 */
@Stable
class NotificationPermission internal constructor(
    private val context: Context,
    private val launcher: ActivityResultLauncher<String>,
) {

    /**
     * Asks, if there is anything to ask for.
     *
     * Silent when the permission is already granted, when the platform does not have
     * it, or when [moment] does not name a real occasion.
     */
    fun requestFor(moment: NotificationMoment) {
        val occasionIsReal = when (moment) {
            // A blank id means no session was written, and a permission prompt for
            // something that did not happen is the drift this whole file guards.
            is NotificationMoment.FocusSessionStarted -> moment.sessionId.isNotBlank()
            NotificationMoment.PulseReminderEnabled -> true
        }
        if (!occasionIsReal) return
        if (!isRequestable(context)) return
        launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}

/**
 * Remembers the one permission request this app makes.
 *
 * Call it wherever the moment happens and pass the moment to
 * [NotificationPermission.requestFor]. Calling this function does not ask for
 * anything, so it is safe to hold in a screen that may never start a session.
 */
@Composable
fun rememberNotificationPermission(): NotificationPermission {
    val context = LocalContext.current
    // Empty on purpose. Nothing in this app behaves differently depending on the
    // answer, so there is nothing to record and nobody to tell.
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { },
    )
    return remember(context, launcher) { NotificationPermission(context, launcher) }
}

/**
 * Asks, once, for the session the person just started, and never for one that was
 * already running. MASTER_BUILD_PROMPT 13.4.
 *
 * Drop it anywhere inside the focus surface and pass it the countdown that surface is
 * already reading. There is nothing to place carefully and nothing to remember to
 * call: the moment is derived rather than announced.
 *
 * **A restored session is not a started one, and telling them apart is the whole of
 * this function.** A session survives process death, per MASTER_BUILD_PROMPT 10, so a
 * cold start can put a running session on screen within a second of launch. Asking
 * then would be asking at launch, which 13.4 forbids in the same sentence that asks
 * for a contextual request, and it is the exact drift [NotificationMoment] exists to
 * prevent. A session that has been running for longer than a few seconds by the time
 * it first reaches a screen was not started at that screen.
 *
 * Deliberately not written as "the session id went from null to something", which is
 * the obvious implementation and is wrong here: on a cold start the id does go from
 * null to something, because the restore is asynchronous and the first composition
 * happens before it finishes. design-v3.md 15.
 */
@Composable
fun NotificationPermissionOnFocusStart(countdown: FocusCountdown?) {
    val permission = rememberNotificationPermission()
    LaunchedEffect(countdown?.sessionId) {
        val session = countdown ?: return@LaunchedEffect
        if (session.elapsedSeconds > JUST_STARTED_SECONDS) return@LaunchedEffect
        permission.requestFor(NotificationMoment.FocusSessionStarted(session.sessionId))
    }
}

/**
 * How much of a session may already have run before it stops counting as one the
 * person just started.
 *
 * A session started by a tap reaches the screen with nothing elapsed. The allowance is
 * for the second or so between the repository writing `FOCUS_STARTED` and the ticker
 * emitting, and it is small enough that a restored session cannot pass through it
 * except in the case where a process died within seconds of a session starting, where
 * asking is contextual anyway.
 */
private const val JUST_STARTED_SECONDS = 5

/**
 * True when the runtime permission exists on this platform and has not been granted.
 *
 * Below Android 13 there is no such permission and notifications are on unless the
 * person switched them off, which is a system settings screen rather than a prompt.
 */
private fun isRequestable(context: Context): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return false
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.POST_NOTIFICATIONS,
    ) != PackageManager.PERMISSION_GRANTED
}
