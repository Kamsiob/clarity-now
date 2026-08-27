package com.kamsiob.claritynow.ui.focus

import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kamsiob.claritynow.domain.engine.StableHash
import com.kamsiob.claritynow.ui.theme.FocusPalette
import com.kamsiob.claritynow.ui.theme.LocalCalmMode
import com.kamsiob.claritynow.ui.theme.LocalContemplativeColors
import com.kamsiob.claritynow.ui.theme.REDUCED_GLOW_OPACITY
import com.kamsiob.claritynow.ui.theme.calmed
import com.kamsiob.claritynow.ui.theme.clarityMotion
import kotlin.math.hypot

/**
 * The indigo night. design-v3.md 3.3 and section 11.
 *
 * A radial gradient from `#262A5E` through `#191C42` to `#10122B` over `deepBlack`,
 * and eight to fourteen specks of light. It is drawn once behind every phase of the
 * Focus surface, so choosing an area, running a session and finishing one all happen
 * in the same room and only the content in front changes.
 *
 * **It is one Canvas and it recomposes never.** The countdown, design-v3.md 8.2
 * item 7, reaches the ring and the numeral and nothing else; the breathing glow in
 * item 8 is read inside the draw lambda, so it invalidates the draw phase and not the
 * composition. The specks are computed once for the life of the surface.
 *
 * **The gradient is indigo into darker indigo and never reaches purple.**
 * design-v3.md 15.1 names that family three times, and 15.3 answers it for this
 * surface by name: the Focus gradient stays on this side of the line. The three stops
 * are the ones in 3.3 and are not to be interpolated toward anything warmer.
 */
@Composable
internal fun FocusBackdrop(modifier: Modifier = Modifier) {
    val contemplative = LocalContemplativeColors.current
    val calm = LocalCalmMode.current
    val glow = rememberFocusGlow()

    // design-v3.md 16.2: the gradient keeps its geometry and loses its intensity,
    // because a Contemplative surface with no center of light is not calmer, it is a
    // black rectangle. Chroma comes off the three stops through the one transform.
    val stops = remember(calm) {
        listOf(
            FocusPalette.gradientCenter.calmed(calm),
            FocusPalette.gradientMid.calmed(calm),
            FocusPalette.gradientEdge.calmed(calm),
        )
    }
    val specks = remember(calm) { specksFor(calm) }

    Canvas(modifier = modifier.fillMaxSize()) {
        drawRect(color = contemplative.deepBlack)

        val center = Offset(size.width * GRADIENT_CENTER_X, size.height * GRADIENT_CENTER_Y)
        drawRect(
            brush = Brush.radialGradient(
                colors = stops,
                center = center,
                radius = hypot(size.width, size.height) * GRADIENT_RADIUS,
            ),
            alpha = glow.value,
        )

        specks.forEach { speck ->
            drawCircle(
                color = Color.White.copy(alpha = speck.alpha),
                radius = speck.radius.toPx(),
                center = Offset(size.width * speck.x, size.height * speck.y),
            )
        }
    }
}

/**
 * design-v3.md 8.2 item 8, the focus glow breathing: 0.85 to 1.0 over eight seconds,
 * infinite, on the easeSlow curve.
 *
 * **When motion is reduced or calm mode is on it holds at 0.92 and the repeating
 * animation is never started**, per 8.3 and 16.6 item 8, which say disabled rather
 * than slowed. Starting an infinite animation whose two ends are the same value would
 * hold the frame loop open all session for a value that never changes.
 *
 * The duration is written out because `easeSlow` is the 600ms token and this is the
 * same curve over eight seconds, the way `ui/nav/ClarityShell.kt` writes out the tab
 * crossfade. The reduce motion branch is written out with it, since a literal duration
 * is the one thing the global motion check cannot see.
 */
@Composable
private fun rememberFocusGlow(): State<Float> {
    val motion = clarityMotion()
    if (motion.reduced) return remember { mutableStateOf(REDUCED_GLOW_OPACITY) }
    val breathing = rememberInfiniteTransition(label = "focusGlow")
    return breathing.animateFloat(
        initialValue = GLOW_LOW,
        targetValue = GLOW_HIGH,
        animationSpec = infiniteRepeatable(
            animation = tween(GLOW_MILLIS, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "focusGlow",
    )
}

/**
 * One speck of light. design-v3.md 3.3.
 *
 * [radius] is half the specified dot: 3.3 gives the dots as 1 to 2dp across, and a
 * canvas draws circles by radius.
 */
@Immutable
private data class Speck(val x: Float, val y: Float, val radius: Dp, val alpha: Float)

/**
 * The specks, from a fixed seed so they never re-randomize. design-v3.md 3.3.
 *
 * `StableHash` rather than `Random`, and not only because `CLAUDE.md` rule 5 keeps
 * `Random` out of the pure layers. A seeded random still has to be seeded from
 * somewhere and re-seeded identically on every recomposition, a rotation and a process
 * death, and the specification asks for one arrangement per surface that never moves.
 * Hashing a per speck key gives that with nothing to remember and nothing to store.
 *
 * In calm mode, eight dots at three percent, which is the low end of both ranges,
 * per design-v3.md 16.7.
 */
private fun specksFor(calm: Boolean): List<Speck> {
    val count = if (calm) SPECKS_MIN else SPECKS_MIN + StableHash.bucket("$SEED.count", SPECK_SPREAD)
    return (0 until count).map { index ->
        Speck(
            x = StableHash.bucket("$SEED.$index.x", POSITION_STEPS) / POSITION_STEPS.toFloat(),
            y = StableHash.bucket("$SEED.$index.y", POSITION_STEPS) / POSITION_STEPS.toFloat(),
            radius = if (calm || StableHash.bucket("$SEED.$index.r", 2) == 0) 0.5.dp else 1.dp,
            alpha = if (calm) {
                SPECK_ALPHA_MIN
            } else {
                SPECK_ALPHA_MIN + StableHash.bucket("$SEED.$index.a", ALPHA_STEPS) * SPECK_ALPHA_STEP
            },
        )
    }
}

/**
 * Where the center of light sits, as a fraction of the surface.
 *
 * Above the middle, because on the session screen that is where the ring is and the
 * pool of light is the room the ring sits in. It is the same on the chooser and on the
 * completion screen, which have no ring: a light that moved when the content changed
 * would make the room itself feel like it had moved.
 */
private const val GRADIENT_CENTER_X = 0.5f
private const val GRADIENT_CENTER_Y = 0.42f

/**
 * How far the gradient reaches, as a fraction of the surface diagonal.
 *
 * Under one, so the darkest stop is reached before the corners and the corners are the
 * edge color rather than a fourth value. design-v3.md 3.3 names three stops and this
 * keeps the drawing to three.
 */
private const val GRADIENT_RADIUS = 0.72f

private const val GLOW_LOW = 0.85f
private const val GLOW_HIGH = 1f
private const val GLOW_MILLIS = 8_000

private const val SEED = "clarity.focus.specks"
private const val SPECKS_MIN = 8
private const val SPECK_SPREAD = 7
private const val POSITION_STEPS = 1_000
private const val SPECK_ALPHA_MIN = 0.03f
private const val SPECK_ALPHA_STEP = 0.01f
private const val ALPHA_STEPS = 4
