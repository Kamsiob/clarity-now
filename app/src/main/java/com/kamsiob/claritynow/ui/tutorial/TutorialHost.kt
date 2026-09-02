package com.kamsiob.claritynow.ui.tutorial

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kamsiob.claritynow.data.prefs.ClarityPreferences
import com.kamsiob.claritynow.di.ClarityViewModelFactory
import kotlinx.coroutines.launch

/**
 * Runs the five step tutorial, or does nothing. MASTER_BUILD_PROMPT 13.2.
 *
 * The shell composes this above everything it draws, including the floating tab bar, and
 * hands it [queued], which is what the first run gate decided on this cold start. Nothing
 * else is needed to wire it: the steps find their own targets through
 * [LocalTutorialTargets].
 *
 * ## When it starts
 *
 * design-v3.md 10.15 asks for the tutorial to start "once the first frame has settled".
 * The obvious implementation is a delay, and a delay guesses: differently on a cold start,
 * on a slow device, and on a phone whose area list is long. [TutorialTargets.ready] is the
 * fact the delay stands in for, so this waits for that instead. Four of the five targets
 * live on the Areas screen, so readiness cannot be true before Areas has laid out.
 *
 * **A consequence worth stating, because it is a feature rather than an accident.** If a
 * target is not wearing [tutorialTarget], the tutorial does not run and `hasSeenTutorial`
 * stays false, so it runs correctly on the next launch after the modifier is added rather
 * than running once, wrongly, and marking itself seen. The same rule covers the real case
 * it was written for: an install with no areas at all, design-v3.md 10.15's zero areas
 * state, where there is no card to point at and the tutorial waits until there is.
 *
 * ## How it ends
 *
 * Three ways, and they are the same way. Skip, back and advancing past the last step all
 * call one function, which writes `hasSeenTutorial` once and takes the overlay down.
 * design-v3.md 10.15 requires back to skip the whole tutorial rather than stepping
 * backwards through it, and the cheapest way to guarantee that is for back to have no
 * separate path to be wrong on.
 */
@Composable
fun TutorialHost(
    queued: Boolean,
    modifier: Modifier = Modifier,
    viewModel: TutorialViewModel = viewModel(factory = ClarityViewModelFactory),
) {
    val targets = LocalTutorialTargets.current
    val steps = TutorialStep.entries

    // Saveable, so a rotation mid tutorial does not restart it. A process death does, and
    // should: `hasSeenTutorial` has not been written yet at that point.
    var index by rememberSaveable { mutableIntStateOf(NOT_STARTED) }
    var finished by rememberSaveable { mutableStateOf(false) }

    val ready = targets.ready
    LaunchedEffect(queued, ready, finished) {
        if (queued && !finished && ready && index == NOT_STARTED) index = 0
    }

    val step = steps.getOrNull(index)
    val live = step?.let { targets[it] }

    // The last rectangle a step reported, so a target that is briefly between layouts
    // cannot blank the overlay and hand the screen underneath back to the person's thumb.
    var lastKnown by remember { mutableStateOf<Rect?>(null) }
    LaunchedEffect(live) { if (live != null) lastKnown = live }
    val target = live ?: lastKnown

    val finish = {
        index = NOT_STARTED
        finished = true
        viewModel.markSeen()
    }

    // **Not predictive, issue #63.** This dismisses a coach mark, and the destination is
    // the screen already fully drawn behind it: there is nothing to uncover because
    // nothing is covered. What would move under a preview is the card and its scrim, and
    // a card that slid away before the gesture was released would read as the tutorial
    // being dragged rather than dismissed.
    BackHandler(enabled = step != null) { finish() }

    if (step == null || target == null) return

    TutorialOverlay(
        step = step,
        stepNumber = index + 1,
        stepCount = steps.size,
        target = target,
        onAdvance = { if (index + 1 < steps.size) index += 1 else finish() },
        onSkip = finish,
        modifier = modifier,
    )
}

/** No step is showing. Distinct from step zero, which is the FAB. */
private const val NOT_STARTED = -1

/**
 * Writes the one flag the tutorial owns.
 *
 * A ViewModel for one setter rather than a repository call from a composable, because
 * `MASTER_BUILD_PROMPT` 5.5 and `docs/ARCHITECTURE.md` put one write path in front of
 * every store in this app and a tutorial is not worth an exception to it.
 */
class TutorialViewModel(private val preferences: ClarityPreferences) : ViewModel() {

    fun markSeen() {
        viewModelScope.launch { preferences.setHasSeenTutorial(true) }
    }
}
