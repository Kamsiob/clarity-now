package com.kamsiob.claritynow.ui.components

import androidx.activity.BackEventCompat
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import com.kamsiob.claritynow.ui.theme.clarityMotion
import kotlin.coroutines.cancellation.CancellationException

/**
 * How far a back gesture has been drawn, and which edge it came from. Issue #63.
 *
 * Held as a class rather than passed as a float so that the modifier below can read both
 * the progress and the edge without a call site threading two values that always travel
 * together and are always wrong apart.
 */
@Stable
class PredictiveBackState internal constructor() {

    /** How far the surface has been drawn back, 0 at rest and 1 at a full gesture. */
    internal val drawn = Animatable(0f)

    /** True while an abandoned gesture is springing home. */
    internal var settling by mutableStateOf(false)

    /** The gesture's own edge. The surface follows the finger, so this is its direction. */
    internal var fromLeftEdge by mutableStateOf(true)

    /**
     * Whether anything is being previewed right now.
     *
     * Read by the surfaces whose destination is not composed until back completes: they
     * compose it **while this is true**, so the room the preview uncovers is the room the
     * gesture actually arrives in. A preview over a bare ground would be a picture of
     * leaving rather than a preview of a destination, which is worse than no preview.
     */
    val isDrawing: Boolean get() = drawn.value > 0f || settling
}

/**
 * Back, with the destination previewed under the finger. Issue #63.
 *
 * The app targets an SDK where predictive back is on by default and every `BackHandler` in
 * it was the non predictive form, so pressing back anywhere committed with no preview of
 * where it went. That is worst on the Focus surface, which is the one place in the app
 * where a person most wants to know whether backing out will end a session before they
 * finish the gesture. It does not, and now the gesture says so before it is released.
 *
 * ## What it draws, and why the surface follows the finger
 *
 * The leaving surface shrinks toward the edge opposite the swipe and slides the way the
 * finger is going, which reveals whatever is composed behind it. Every surface this is
 * used on is drawn over the tab content rather than replacing it, so there is always
 * something behind to see, and the preview is therefore true: the room it uncovers is the
 * room the gesture arrives in.
 *
 * ## The springs are the app's, and there are no durations here
 *
 * While a finger is down the progress is the finger's, applied with [snap] so the surface
 * tracks it exactly. Releasing short of the threshold returns it on `springStandard`, which
 * is the same spatial spring every other movement in this app uses. Nothing here names a
 * millisecond.
 *
 * ## Reduce motion and calm mode collapse it, `design-v3.md` 8.3
 *
 * The preview is a spatial change, so 8.3 gates it off rather than shortening it: the
 * progress is held at zero and back commits on release exactly as it did before. Somebody
 * who has asked for less motion does not get a smaller version of the animation, they get
 * the behavior without it.
 *
 * @param enabled whether this handler takes the gesture at all, the same meaning
 *   `BackHandler` gives the parameter.
 * @param onBack run once, when the gesture completes. Never run on a cancel.
 */
@Composable
fun rememberPredictiveBack(enabled: Boolean = true, onBack: () -> Unit): PredictiveBackState {
    val motion = clarityMotion()
    val state = remember { PredictiveBackState() }

    // **The spring home runs here rather than in the handler's own catch.** A canceled
    // gesture cancels the coroutine the handler's block is running in, and every
    // suspension inside a canceled coroutine throws at once, so an `animateTo` in that
    // catch would never draw a frame. Setting a flag is not a suspension, and this effect
    // lives in the composition, which is still alive.
    LaunchedEffect(state.settling) {
        if (state.settling) {
            state.drawn.animateTo(0f, motion.springStandard())
            state.settling = false
        }
    }

    PredictiveBackHandler(enabled = enabled) { events ->
        try {
            events.collect { event ->
                state.fromLeftEdge = event.swipeEdge == BackEventCompat.EDGE_LEFT
                // Under the finger the progress is the finger's, snapped rather than
                // animated, so the surface tracks it exactly.
                if (!motion.reduced) state.drawn.snapTo(event.progress)
            }
            onBack()
            state.drawn.snapTo(0f)
        } catch (canceled: CancellationException) {
            // The gesture was abandoned. The surface springs home and nothing is run:
            // this is the whole of what "canceling leaves the session running" means on
            // the Focus surface, and it holds because [onBack] is above this line.
            state.settling = true
            throw canceled
        }
    }
    return state
}

/** The preview itself, applied to the surface that is leaving. */
fun Modifier.predictiveBackPreview(state: PredictiveBackState): Modifier = graphicsLayer {
    val drawn = state.drawn.value.coerceIn(0f, 1f)
    if (drawn == 0f) return@graphicsLayer
    scaleX = 1f - PREVIEW_SHRINK * drawn
    scaleY = 1f - PREVIEW_SHRINK * drawn
    // Toward the edge the finger is traveling to, which is away from the one it
    // started at. A surface that moved the other way would read as resisting.
    translationX = (if (state.fromLeftEdge) 1f else -1f) * size.width * PREVIEW_SLIDE * drawn
    // Anchored at the far edge, so the near side is what opens and the eye is led to
    // the room appearing behind it rather than to the corner it came from.
    transformOrigin = TransformOrigin(if (state.fromLeftEdge) 1f else 0f, 0.5f)
}

/** Ten percent at full draw, which is the platform's own figure for a leaving surface. */
private const val PREVIEW_SHRINK = 0.10f

/** Six percent of the width. Enough to open a margin, not enough to look like a drawer. */
private const val PREVIEW_SLIDE = 0.06f
