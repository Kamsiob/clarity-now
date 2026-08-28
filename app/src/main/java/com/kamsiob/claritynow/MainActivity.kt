package com.kamsiob.claritynow

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kamsiob.claritynow.di.ClarityGraph
import com.kamsiob.claritynow.ui.nav.ClarityShell
import com.kamsiob.claritynow.ui.nav.ExternalRequest
import com.kamsiob.claritynow.ui.nav.destinationFor
import com.kamsiob.claritynow.ui.onboarding.FirstRunGate
import com.kamsiob.claritynow.ui.theme.ClarityHaptics
import com.kamsiob.claritynow.ui.theme.ClarityTextSize
import com.kamsiob.claritynow.ui.theme.ClarityTheme
import com.kamsiob.claritynow.ui.theme.ClarityThemeSetting
import com.kamsiob.claritynow.ui.theme.LocalClarityHaptics
import com.kamsiob.claritynow.widget.WidgetIntents

class MainActivity : ComponentActivity() {

    /**
     * What something outside the composition has asked this app to open, and how many
     * times it has asked. Six widgets, three app shortcuts, two notifications and a
     * quick settings tile all arrive here, per `MASTER_BUILD_PROMPT.md` 13.3, 13.4 and
     * 13.5.
     *
     * **A serial rather than a flag, and read rather than consumed.** A flag would have
     * to be cleared by whoever acted on it, and the frame between acting and clearing is
     * the frame that re-opens a surface the person has just left. A number that only
     * goes up says "this happened again" with nothing to reset. That was the reasoning
     * for the Focus counter this replaces, and it is unchanged: the other five actions
     * joined the pattern rather than inventing a second one. See
     * [com.kamsiob.claritynow.ui.nav.ExternalRequest].
     *
     * It is Compose state so that a value written in [onNewIntent], which arrives with
     * no composition running, reaches the shell on the next frame with nothing in
     * between to hold it.
     */
    private var request by mutableStateOf(ExternalRequest())

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        // The launch intent, which is the cold start half of every tap outside the app.
        // This Activity is `singleTask`, so the other half arrives at onNewIntent below,
        // and both halves go through the one function so the two starts cannot differ.
        noteRequest(intent)
        val haptics: ClarityHaptics = ClarityGraph.haptics
        setContent {
            val theme by ClarityGraph.preferences.theme
                .collectAsStateWithLifecycle(initialValue = ClarityThemeSetting.SYSTEM)
            // Null means the person has never touched the switch, which is a storage
            // state rather than an interface state: while it is null, calm mode
            // follows the system reduce motion setting, which design-v3.md 16.1 makes
            // the default. The theme resolves that, not this call site.
            val calmMode by ClarityGraph.preferences.calmMode
                .collectAsStateWithLifecycle(initialValue = null)
            // design-v3.md 13. It multiplies the OS font scale rather than replacing
            // it, so DEFAULT is already "whatever this phone asks for" and there is no
            // absent third state to resolve the way calm mode has one.
            val textSize by ClarityGraph.preferences.textSize
                .collectAsStateWithLifecycle(initialValue = ClarityTextSize.DEFAULT)
            ClarityTheme(setting = theme, calmMode = calmMode, textSize = textSize) {
                CompositionLocalProvider(LocalClarityHaptics provides haptics) {
                    // design-v3.md 10.15's first launch rules, and nothing else, live in
                    // FirstRunGate. This Activity asks it what a cold start does and
                    // renders whatever it answers; the shell is what it answers with in
                    // every case except an install that has never finished onboarding.
                    FirstRunGate(preferences = ClarityGraph.preferences) { tutorialQueued ->
                        ClarityShell(
                            request = request,
                            tutorialQueued = tutorialQueued,
                        )
                    }
                }
            }
        }
    }

    /**
     * A widget, a shortcut, a notification or the tile, tapped while this task is
     * already alive. The Activity is `singleTask`, so this is where every intent after
     * the first one arrives.
     *
     * The new intent is kept, so that anything asking this Activity what it was last
     * started with gets the current answer rather than the launcher intent from this
     * morning.
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        this.intent = intent
        noteRequest(intent)
    }

    /**
     * **Notices, and never decides.** Which tab is showing, whether a sheet opens over
     * it, what happens to a running session and whether a session may start at all are
     * the shell's, the screen's and the repository's questions. This reads three fields
     * off an intent, records what was asked for, and stops.
     *
     * The decision itself is
     * [com.kamsiob.claritynow.ui.nav.destinationFor], which takes the action and the two
     * extras rather than the `Intent`, so that the one table naming every action this
     * app can receive is a value a unit test can call. Phase 12 shipped five actions
     * with a contract and no receiver and nothing went red.
     *
     * **An intent that names no destination is not a request.** A launcher tap, and the
     * tap on a widget with nothing to show, both send `Intent.ACTION_MAIN`; the serial
     * does not move and the app opens where it was, which is what those taps mean.
     *
     * The session id on a Focus intent, `FocusIntents.sessionIdIn`, is still deliberately
     * not read, for the reason it always was: there is one running session per device and
     * the surface finds it from the log.
     */
    private fun noteRequest(intent: Intent?) {
        val destination = destinationFor(
            action = intent?.action,
            // The two extractors, rather than the extra keys written out a second time.
            areaId = WidgetIntents.areaIdIn(intent),
            itemId = WidgetIntents.itemIdIn(intent),
        ) ?: return
        request = request.asking(destination)
    }
}
