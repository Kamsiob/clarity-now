package com.kamsiob.claritynow.ui.onboarding

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.kamsiob.claritynow.data.prefs.ClarityPreferences
import kotlinx.coroutines.flow.first

/**
 * What a cold start does. design-v3.md 10.15's first launch rules, in one place.
 *
 * > Cold start reads two flags in order. `hasCompletedOnboarding` false routes to
 * > onboarding. Otherwise, `hasSeenTutorial` false routes to Areas with the tutorial
 * > queued to start once the first frame has settled. Otherwise, Areas.
 *
 * ## The decision is taken once and then held
 *
 * The flags are read once, in that order, and the answer is latched for the process
 * rather than being recollected from the flows. That is not an optimization, it is what
 * makes the flow survive its own writes: beat 3 sets `hasCompletedOnboarding` while
 * onboarding is still showing, and a route that recomputed itself from a flow would swap
 * onboarding out for the app in the middle of the reveal.
 *
 * **Beat 3 writing that flag is itself required rather than early.** design-v3.md 10.15:
 * "a user who force-quits after beat 3 and relaunches lands on a populated Areas screen
 * rather than starting over". The flag is what decides that on the next cold start, and
 * the areas are already real events by then, so onboarding is genuinely complete at that
 * moment. Beat 4 is depth, and depth is not setup.
 *
 * ## Why the app is composed underneath rather than after
 *
 * MASTER_BUILD_PROMPT 13.1's beat 3 uncovers "the user's actual Areas screen rendered
 * live behind the overlay". So from the moment the reveal begins, [app] is composed
 * beneath onboarding and stays composed.
 *
 * **It is composed at the start of beat 3 rather than a few frames before the iris, and
 * that is a trade taken on purpose.** Composing it late would let the staggered card
 * entrance in design-v3.md 8.2 item 4 play into the opening hole, which is the prettier
 * version. It would also mean the iris opening onto a screen that is still loading the
 * log on any launch where that takes longer than the head start it was given, and a
 * half drawn screen at the emotional peak of the flow is a worse outcome than a settled
 * one. Beat 3 gives it the whole of the closing line's hold, about 1.8 seconds, to load,
 * lay out and finish arriving, and what comes up through the hole is at rest.
 *
 * It is equally deliberately not composed before beat 3. The entrance would be spent
 * behind an opaque black screen for the whole of beats 1 and 2, and the Areas tab would
 * be running against the log through a flow nobody can see.
 *
 * [app] receives `tutorialQueued` as false until onboarding has finished, for a related
 * reason: the tutorial must not start behind beat 4.
 *
 * ## The third check, which is not here
 *
 * design-v3.md 10.15 adds a re-entry route after both flags when the gap since the last
 * recorded open is fourteen days or more, and it "is checked last so that it can never
 * delay or replace a first run". It is not built, in this file or anywhere: 11.2 is
 * phase 6's surface and the routing that reaches it has no owner yet. When it arrives it
 * belongs after both branches below, never inside them.
 */
@Composable
fun FirstRunGate(
    preferences: ClarityPreferences,
    app: @Composable (tutorialQueued: Boolean) -> Unit,
) {
    var decision by remember { mutableStateOf<FirstRunRoute?>(null) }

    LaunchedEffect(preferences) {
        // In this order, and read rather than observed. design-v3.md 10.15.
        val completedOnboarding = preferences.hasCompletedOnboarding.first()
        val seenTutorial = preferences.hasSeenTutorial.first()
        decision = if (!completedOnboarding) {
            FirstRunRoute.Onboarding
        } else {
            FirstRunRoute.App(tutorialQueued = !seenTutorial)
        }
    }

    when (val route = decision) {
        // One or two frames while DataStore answers. Deliberately paints nothing, so what
        // shows is the launch window's own background rather than a color chosen here
        // that would be wrong for one of the two branches about to be taken.
        null -> Box(Modifier.fillMaxSize())

        is FirstRunRoute.App -> app(route.tutorialQueued)

        FirstRunRoute.Onboarding -> {
            var revealing by remember { mutableStateOf(false) }
            var finished by remember { mutableStateOf(false) }

            Box(Modifier.fillMaxSize()) {
                if (revealing || finished) app(finished)
                if (!finished) {
                    OnboardingRoute(
                        onRevealStarted = { revealing = true },
                        onFinished = { finished = true },
                    )
                }
            }
        }
    }
}

/** The two destinations a cold start has. */
@Immutable
private sealed interface FirstRunRoute {

    data object Onboarding : FirstRunRoute

    data class App(val tutorialQueued: Boolean) : FirstRunRoute
}
