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
import com.kamsiob.claritynow.ui.reentry.ReEntryDecision
import com.kamsiob.claritynow.ui.reentry.ReEntryOffer
import com.kamsiob.claritynow.ui.reentry.ReEntryRoute
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
 * ## The third check, and where it sits
 *
 * design-v3.md 10.15 adds a re-entry route after both flags when the gap since the last
 * recorded open is fourteen days or more, and it "is checked last so that it can never
 * delay or replace a first run". **Built, phase 12c**, and it is after both branches
 * below rather than inside either of them, which is where phase 10 left the place
 * marked. [ReEntryDecision] holds the whole of the rule and `MASTER_BUILD_PROMPT.md`
 * 14b.4 holds the reason for every part of it.
 *
 * **The app is composed underneath it too, and this one is not a trade.** 14b.4 requires
 * that the re-entry state be alone and that "a conflict card from 6.3 waits behind it
 * rather than being dropped", which is a sentence about something that is still there
 * afterwards. The alternative is to hold the first frame until the log has answered,
 * which would put a blank screen in front of every cold start this app will ever have,
 * for a question that is false on all but one day in a person's whole use of it. So the
 * app arrives when it always did, the surface covers it opaquely when the answer comes
 * back, and item 25's 150ms delay is what stops that from reading as a flash.
 *
 * **The tutorial is held while the answer is unknown, and for the rest of the process
 * once the answer was yes.** The first half closes the one race that would break "it is
 * first and it is alone": the tutorial needs five targets to report before it starts,
 * four of them on the Areas screen, so without it the tour could begin in the frames
 * between the app arriving and the log answering. The second half is a choice rather
 * than a rule, and it is the reading of "it is the first screen and it is alone" that
 * costs the least. A five step spotlight tour starting the instant somebody answers the
 * question on the day they came back is a great deal to arrive at once, and it costs
 * nothing to wait: `hasSeenTutorial` is written only when the tour actually runs, so it
 * runs on the next launch instead, which is what `TutorialHost` already does whenever
 * it is not ready.
 */
@Composable
fun FirstRunGate(
    preferences: ClarityPreferences,
    reEntry: ReEntryDecision,
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

        is FirstRunRoute.App -> ReEntryGate(reEntry = reEntry) { returning ->
            app(route.tutorialQueued && returning == ReEntryAnswer.NOTHING_TO_OFFER)
        }

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

/**
 * The third check. MASTER_BUILD_PROMPT 14b.4, design-v3.md 10.15 and 11.2.
 *
 * Composes [app] immediately, asks the log once whether this open is a return, and
 * covers everything with the re-entry state if it is. [app] is told which of the four
 * states this launch is in, because the tutorial must not start while the answer is
 * outstanding and must not start on a launch this screen has already had.
 *
 * **This never runs on a first run.** It is only reachable from the `App` branch above,
 * which is the branch onboarding is already behind, and [ReEntryDecision] refuses again
 * on its own if it is ever asked otherwise.
 */
@Composable
private fun ReEntryGate(
    reEntry: ReEntryDecision,
    app: @Composable (returning: ReEntryAnswer) -> Unit,
) {
    var answer by remember { mutableStateOf(ReEntryAnswer.NOT_ASKED_YET) }
    var offer by remember { mutableStateOf<ReEntryOffer?>(null) }

    LaunchedEffect(reEntry) {
        val found = reEntry.offerOnThisOpen(onboardingComplete = true)
        offer = found
        answer = if (found == null) ReEntryAnswer.NOTHING_TO_OFFER else ReEntryAnswer.SHOWING
    }

    Box(Modifier.fillMaxSize()) {
        app(answer)

        val standing = offer
        if (standing != null) {
            ReEntryRoute(
                offer = standing,
                // Dropping the offer is what takes the surface down, and it is one way
                // rather than a toggle: the answer has been written by the time this
                // runs, so nothing can put it back for this gap.
                onSettled = {
                    offer = null
                    answer = ReEntryAnswer.ANSWERED
                },
            )
        }
    }
}

/**
 * Where the third check has got to, as the thing the tutorial has to wait on.
 *
 * Exactly one of these lets the tutorial start, and the two that do not are different
 * for a reason: [NOT_ASKED_YET] is a wait and [ANSWERED] is a launch this screen has
 * already had. Folding the second back into [NOTHING_TO_OFFER] would start the tour the
 * moment somebody answered.
 */
private enum class ReEntryAnswer {
    /** The log has not answered yet. Nothing else may claim the first moment. */
    NOT_ASKED_YET,

    /** Not a return, or one this device has already answered. The ordinary launch. */
    NOTHING_TO_OFFER,

    /** The re-entry state is up. */
    SHOWING,

    /** It was up and has been answered. This launch belonged to it. */
    ANSWERED,
}

/** The two destinations a cold start has. */
@Immutable
private sealed interface FirstRunRoute {

    data object Onboarding : FirstRunRoute

    data class App(val tutorialQueued: Boolean) : FirstRunRoute
}
