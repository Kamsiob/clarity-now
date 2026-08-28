package com.kamsiob.claritynow.tile

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.kamsiob.claritynow.MainActivity
import com.kamsiob.claritynow.R
import com.kamsiob.claritynow.di.ClarityGraph
import com.kamsiob.claritynow.domain.replay.FocusSessionState
import com.kamsiob.claritynow.notifications.FocusActionReceiver
import com.kamsiob.claritynow.notifications.FocusIntents
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * The quick settings tile. MASTER_BUILD_PROMPT 13.5, design-v3.md 12.4, issue #41.
 *
 * One tile with two states and one job: start a focus session, or end the one that is
 * running, from the shade the person already has open.
 *
 * ## What it reads, and why it is not the widget snapshot
 *
 * [com.kamsiob.claritynow.data.repo.ClarityRepository.runningFocusSession], which is
 * the same flow the Focus surface and the ongoing notification read: the persisted
 * session handle resolved against the log. Issue #41 asks for exactly that in as many
 * words, and the risk it names is three surfaces reading three copies. The widget
 * snapshot is a copy, written for a process that cannot open the database on the frame
 * it draws, and a tile that read it could sit in the shade offering to end a session
 * that ended a minute ago.
 *
 * A collector is attached while the shade is open and canceled when it closes, so a
 * session started or ended anywhere else, in the app, from the Live Update's `End`, or
 * from the First Step widget, is on the tile the moment it happens.
 *
 * ## What a tap does, in each state
 *
 * **Running: it ends the session, in place, without opening anything.** The intent goes
 * to [FocusActionReceiver], which is the same receiver the Live Update's `End` action
 * already reaches, so this is one more caller of one path rather than a second way to
 * end a session. That file computes the real elapsed seconds from the projection,
 * writes `FOCUS_ENDED_EARLY` through the repository and clears the shade. Addendum 01
 * 4e: fourteen minutes is fourteen minutes, and nothing here or there calls it
 * abandoned.
 *
 * There is no confirm, for the reason that file gives about its own `End` action:
 * MASTER_BUILD_PROMPT 10 puts a confirm behind the pill on the focus screen, a confirm
 * on this surface could only be a notification or a screen, and a labeled control in
 * the shade that reads `End focus` is already a deliberate tap rather than a gesture
 * that completes by accident.
 *
 * **Not running: it opens the Focus surface**, which is the chooser. It does not start
 * a session on the app's guess about which item was meant. 13.5 requires the chooser
 * when there is no active item anywhere, and it is the answer in every other case too:
 * a session is a row in a log that only gains rows, `design-v3.md` 15 asks for the
 * unobvious answer where a choice is open, and the obvious one here is an app deciding
 * on somebody's behalf, which is the thing 14b.1 took out of the FAB. The chooser is
 * also where "no active item anywhere" explains itself, with every area dimmed and
 * `Add an item first` under them, per section 10.
 *
 * ## Behind a locked screen, stated rather than discovered
 *
 * **Both halves go through [unlockAndRun], so neither does anything until the person is
 * present.** On an unlocked phone that runs immediately and nothing is different. On a
 * locked one the system asks for the passcode first and then runs the tap.
 *
 * Ending could be done without it, since it opens nothing and reveals nothing. It is
 * not, because the two halves of one tile should not have two different rules about a
 * locked phone, and because ending writes to the log, which is the truth this app keeps
 * and not a thing for whoever is holding the handset.
 *
 * ## What is deliberately not on it
 *
 * No subtitle and no item title. The shade is readable over a lock screen, and what
 * somebody is working on is not for a passer by. No countdown either: the tile is
 * updated when the session changes and not once a second, the Focus Countdown widget
 * and the ongoing notification both carry the remaining time already, and a number that
 * only moves when the shade opens is worse than no number at all.
 *
 * No permission is added by any of this. `BIND_QUICK_SETTINGS_TILE` in the manifest is
 * a permission the **system** must hold to bind this service, not one this app asks
 * for, so the merged manifest is unchanged and `verifyNoInternetPermission` is
 * untouched.
 */
class FocusTileService : TileService() {

    /**
     * Alive only while the shade is open, which is the only time anything can see this.
     *
     * [Dispatchers.Main.immediate] because everything it does is set three fields on
     * [android.service.quicksettings.Tile] and call `updateTile`, which is main thread
     * work, and because the read behind it is a flow the repository already keeps warm.
     */
    private var scope: CoroutineScope? = null

    /**
     * The session the tile is currently showing, or null.
     *
     * **A tap acts on what the tile says**, rather than on a fresh read taken inside the
     * click. The two differ only in the moment between the shade being drawn and being
     * touched, and in that moment the honest thing is to do what the label offered. A
     * session that ended in between is already ended, and the repository refuses a
     * second ending rather than writing one.
     *
     * Written in [render] and read in [onClick], both on the main thread.
     */
    private var shownSessionId: String? = null

    override fun onStartListening() {
        super.onStartListening()
        val listening = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        scope = listening
        // A process the tile started itself still runs Application.onCreate first, so
        // this is a guard against a state that should not exist rather than an expected
        // path. The tile draws its resting state and does nothing.
        if (!ClarityGraph.isInstalled) {
            render(null)
            return
        }
        listening.launch {
            val repository = ClarityGraph.repository
            // Idempotent under the repository's own lock. On a cold process this is the
            // replay; on a warm one it returns.
            repository.load()
            repository.runningFocusSession.collect { session -> render(session) }
        }
    }

    override fun onStopListening() {
        scope?.cancel()
        scope = null
        super.onStopListening()
    }

    /**
     * The tap. [unlockAndRun] runs its argument immediately on an unlocked phone and
     * after the passcode on a locked one.
     */
    override fun onClick() {
        val sessionId = shownSessionId
        unlockAndRun {
            if (sessionId == null) openFocusSurface() else endSession(sessionId)
        }
    }

    /** The two states, and everything that differs between them. */
    private fun render(session: FocusSessionState?) {
        shownSessionId = session?.id
        val tile = qsTile ?: return
        val running = session != null
        tile.state = if (running) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.label = getString(if (running) R.string.tile_focus_end else R.string.tile_focus_start)
        tile.contentDescription = getString(
            if (running) R.string.cd_tile_focus_end else R.string.cd_tile_focus_start,
        )
        // The icon is the manifest's and never changes. The label already says which of
        // the two things a tap does, and the state already says which one is true, so a
        // third signal would be the second separation device design-v3.md 6.1 rules out
        // and one of the three would eventually disagree with the others.
        tile.updateTile()
    }

    /**
     * `End`, through the receiver the Live Update already uses.
     *
     * An explicit component, so it can only reach this app's own receiver, which the
     * manifest declares not exported for the same reason.
     */
    private fun endSession(sessionId: String) {
        sendBroadcast(
            Intent(this, FocusActionReceiver::class.java)
                .setAction(FocusIntents.ACTION_END)
                .putExtra(FocusIntents.EXTRA_SESSION_ID, sessionId),
        )
    }

    /**
     * The Focus surface, with the shade collapsing behind it.
     *
     * `SINGLE_TOP` beside the manifest's `singleTask` so a tap while the app is open
     * delivers a new intent rather than rebuilding the task, and `NEW_TASK` because the
     * sender is a service. The same pair the notifications and the widgets use.
     *
     * The overload split is the platform's: `startActivityAndCollapse(PendingIntent)`
     * arrives in Android 14 and the `Intent` overload throws there, while this app's
     * `minSdk` is 31 and three releases before it have only the `Intent` one. The
     * suppression is that deprecation and nothing wider.
     */
    private fun openFocusSurface() {
        val intent = Intent(this, MainActivity::class.java)
            .setAction(FocusIntents.ACTION_OPEN_FOCUS)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startActivityAndCollapse(
                PendingIntent.getActivity(
                    this,
                    REQUEST_OPEN_FOCUS,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                ),
            )
        } else {
            @Suppress("DEPRECATION")
            startActivityAndCollapse(intent)
        }
    }

    private companion object {

        /**
         * Distinct from every code in `FocusIntents`, which uses 1 to 4, and from
         * `PulseIntents`, which uses 5. Two PendingIntents collide when their request
         * codes and their intents match, and this intent matches the one behind the
         * ongoing notification under `filterEquals`, which ignores extras.
         */
        const val REQUEST_OPEN_FOCUS = 6
    }
}
