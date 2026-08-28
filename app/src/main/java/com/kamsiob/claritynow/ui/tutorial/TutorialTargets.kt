package com.kamsiob.claritynow.ui.tutorial

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.testTag
import com.kamsiob.claritynow.R

/**
 * The five spotlight steps, in order. MASTER_BUILD_PROMPT 13.2.
 *
 * [tag] is the stable test tag 13.2 asks the mechanism to be keyed by. It is applied to
 * the element by [tutorialTarget], so the same string identifies the target to this
 * overlay and to a UI test, and neither can drift from the other.
 *
 * **The enum's declaration order is the tutorial's order, and it is the only place that
 * order is written down.** There is no separate list of steps to keep in step with this
 * one, and adding a sixth step is adding one entry here, one modifier at its call site
 * and two strings.
 *
 * **The copy rides on the entry rather than in a `when` somewhere else.** A step's words
 * are the only thing about a step that is genuinely its own, and holding them here keeps
 * the overlay free of any branch on which step it is drawing, which is what 13.2 means by
 * one uniform mechanism. Neither string says anything about a person's data, so both are
 * fixed interface copy in `strings.xml`, per MASTER_BUILD_PROMPT 11.2.
 */
enum class TutorialStep(
    val tag: String,
    val titleRes: Int,
    val bodyRes: Int,
) {
    FAB("clarity.tutorial.fab", R.string.tutorial_fab_title, R.string.tutorial_fab_body),
    AREA_CARD("clarity.tutorial.areaCard", R.string.tutorial_area_title, R.string.tutorial_area_body),
    FOCUS_CHIP("clarity.tutorial.focusChip", R.string.tutorial_focus_title, R.string.tutorial_focus_body),
    PULSE_CHIP("clarity.tutorial.pulseChip", R.string.tutorial_pulse_title, R.string.tutorial_pulse_body),
    TAB_BAR("clarity.tutorial.tabBar", R.string.tutorial_tabs_title, R.string.tutorial_tabs_body),
}

/**
 * Where each spotlight target currently is, in the coordinates of the composition root.
 *
 * **This is the whole of the tutorial's uniform mechanism, and its uniformity is the
 * point.** MASTER_BUILD_PROMPT 13.2 calls per step special casing the hard won lesson of
 * the iOS build and says not to mix strategies. So there is exactly one way a target is
 * found: the element wears [tutorialTarget], reports its bounds through
 * `onGloballyPositioned`, and this map holds the answer. Nothing in the overlay knows
 * what kind of element a step points at, nothing measures a step by hand, and no step
 * carries a nudge, an inset or a shape of its own. Every visual difference between one
 * spotlight and the next is a consequence of the rectangle that arrived here.
 *
 * A target that leaves the composition removes itself, so a stale rectangle can never
 * outlive the element it described.
 *
 * The snapshot map is written only when a rectangle actually changes. `onGloballyPositioned`
 * fires on every layout pass, and an unguarded write would recompose every reader of this
 * object on every frame of a scroll.
 */
@Stable
class TutorialTargets {

    private val bounds = mutableStateMapOf<TutorialStep, Rect>()

    /** Plain, not snapshot state: it exists so [report] can decide whether to write. */
    private val lastReported = HashMap<TutorialStep, Rect>()

    fun report(step: TutorialStep, rect: Rect) {
        if (lastReported[step] == rect) return
        lastReported[step] = rect
        bounds[step] = rect
    }

    fun forget(step: TutorialStep) {
        lastReported.remove(step)
        bounds.remove(step)
    }

    operator fun get(step: TutorialStep): Rect? = bounds[step]

    /**
     * True when every step has somewhere to point.
     *
     * **This is also the answer to "has the first frame settled".** design-v3.md 10.15
     * asks for the tutorial to start once the first frame has settled, and a delay is the
     * obvious way to express that and the wrong one: a delay guesses, and it guesses
     * differently on a cold start, on a slow device and on a device with a long area
     * list. Readiness is the fact the delay was standing in for. Four of the five targets
     * live on the Areas screen, so this cannot be true until Areas has laid out, and it
     * goes false again the moment it stops being.
     */
    val ready: Boolean
        get() = TutorialStep.entries.all { bounds.containsKey(it) }
}

/**
 * The one instance for the app shell, provided by `ClarityShell`.
 *
 * The default is a live but unread registry rather than an error, so an element wearing
 * [tutorialTarget] composes correctly in a preview, in a test and anywhere else outside
 * the shell. Reporting into a registry nobody reads costs one map write.
 */
val LocalTutorialTargets = staticCompositionLocalOf { TutorialTargets() }

/**
 * Marks this element as [step]'s spotlight target.
 *
 * One line at the call site and nothing else. It applies the step's stable test tag and
 * reports the element's bounds in the composition root, which is the coordinate space
 * the overlay draws in.
 *
 * **Bounds rather than a position and a size**, because a spotlight is a rectangle and
 * every one of the five is a different shape. `boundsInRoot` also survives the element
 * being inside a scrolling list, a padded column or an aligned box, none of which the
 * overlay has to know about.
 */
@Composable
fun Modifier.tutorialTarget(step: TutorialStep): Modifier {
    val targets = LocalTutorialTargets.current
    DisposableEffect(targets, step) {
        onDispose { targets.forget(step) }
    }
    return testTag(step.tag)
        .onGloballyPositioned { targets.report(step, it.boundsInRoot()) }
}
