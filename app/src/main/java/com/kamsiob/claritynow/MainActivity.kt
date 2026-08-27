package com.kamsiob.claritynow

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kamsiob.claritynow.di.ClarityGraph
import com.kamsiob.claritynow.notifications.FocusIntents
import com.kamsiob.claritynow.ui.nav.ClarityShell
import com.kamsiob.claritynow.ui.theme.ClarityHaptics
import com.kamsiob.claritynow.ui.theme.ClarityTheme
import com.kamsiob.claritynow.ui.theme.ClarityThemeSetting
import com.kamsiob.claritynow.ui.theme.LocalClarityHaptics

class MainActivity : ComponentActivity() {

    /**
     * How many times something outside the composition has asked for the Focus
     * surface. Today that means a notification: the body of the ongoing notification,
     * of the Live Update and of the completion notification all carry
     * [FocusIntents.ACTION_OPEN_FOCUS], per MASTER_BUILD_PROMPT section 10 and
     * Addendum 01 5c.
     *
     * **A counter rather than a flag, and read rather than consumed.** A flag would
     * have to be cleared by whoever acted on it, and the frame between acting and
     * clearing is the frame that re-opens a surface the person has just left. A number
     * that only goes up says "this happened again" with nothing to reset.
     *
     * It is Compose state so that a value written in `onNewIntent`, which arrives with
     * no composition running, reaches the shell on the next frame with nothing in
     * between to hold it.
     */
    private var focusRequest by mutableLongStateOf(0L)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        // The launch intent, which is the cold start half of a notification tap. This
        // Activity is `singleTask`, so the other half arrives at onNewIntent below.
        noteFocusRequest(intent)
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
            ClarityTheme(setting = theme, calmMode = calmMode) {
                CompositionLocalProvider(LocalClarityHaptics provides haptics) {
                    ClarityShell(focusRequest = focusRequest)
                }
            }
        }
    }

    /**
     * A notification tapped while this task is already alive. The Activity is
     * `singleTask`, so this is where every intent after the first one arrives.
     *
     * The new intent is kept, so that anything asking this Activity what it was last
     * started with gets the current answer rather than the launcher intent from this
     * morning.
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        this.intent = intent
        noteFocusRequest(intent)
    }

    /**
     * **Notices, and never decides.** Whether the Focus surface opens, what it shows
     * and what happens to a running session are the shell's and the surface's
     * questions, and design-v3.md 10.15 turns on nothing here going near them: this
     * counts a request and stops.
     *
     * The session id on the intent, [FocusIntents.sessionIdIn], is deliberately not
     * read. There is one running session per device and the surface finds it from the
     * log, so an id taken from a notification could only ever be a second, staler
     * opinion about which session a person is in.
     */
    private fun noteFocusRequest(intent: Intent?) {
        if (FocusIntents.opensFocusSession(intent)) focusRequest += 1L
    }
}
