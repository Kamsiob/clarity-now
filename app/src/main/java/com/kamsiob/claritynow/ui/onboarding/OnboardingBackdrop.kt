package com.kamsiob.claritynow.ui.onboarding

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kamsiob.claritynow.domain.engine.StableHash
import com.kamsiob.claritynow.ui.theme.LocalCalmMode
import com.kamsiob.claritynow.ui.theme.LocalContemplativeColors
import com.kamsiob.claritynow.ui.theme.calmed
import com.kamsiob.claritynow.ui.theme.clarityMotion
import kotlin.math.hypot

/**
 * The room onboarding is read in. design-v3.md 3.3, "Onboarding".
 *
 * One ground, one glow, and eight to fourteen specks of light. The glow is the only thing
 * that changes between beats, which is what 3.3 asks for: beat 1 actionBlue, beat 2
 * twilight violet, beat 4 cycling amber, blue and gold. Beat 3 passes null, because the
 * reveal's light is the Areas screen coming up through the iris and a second center of
 * light behind it would compete with it.
 *
 * **The glow crossfades rather than cutting**, on `easeSlow`, which is design-v3.md 8.1's
 * curve for a world transition and is what moving from one beat to the next is. Under
 * reduce motion or calm mode that becomes the 150ms crossfade like everything else, so
 * the color still arrives and nothing travels.
 *
 * **In calm mode the glow is transformed and its geometry is held**, per design-v3.md
 * 16.2: a Contemplative surface with no center of light is not calmer, it is a black
 * rectangle. The specks drop to the low end of both their ranges, which is the treatment
 * the Focus and Pulse backdrops already take.
 *
 * It is one `Canvas` with no ticker behind it. The glow does not breathe: 8.2 item 8 puts
 * a breathing glow on the Focus surface and nowhere else, and a second one here would be
 * an ambient animation on a screen a person reads once.
 */
@Composable
internal fun OnboardingBackdrop(glow: Color?, modifier: Modifier = Modifier) {
    val contemplative = LocalContemplativeColors.current
    val calm = LocalCalmMode.current
    val motion = clarityMotion()

    // Transparent rather than absent, so a beat with no glow is the same animation as any
    // other change of glow rather than a branch that removes the element.
    val target = glow?.calmed(calm)?.copy(alpha = GLOW_ALPHA) ?: Color.Transparent
    val tint by animateColorAsState(
        targetValue = target,
        animationSpec = motion.easeSlow(),
        label = "onboardingGlow",
    )

    val specks = remember(calm) { specksFor(calm) }

    Canvas(modifier = modifier.fillMaxSize()) {
        drawRect(color = contemplative.deepBlack)

        if (tint.alpha > 0f) {
            val center = Offset(size.width * GLOW_X, size.height * GLOW_Y)
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(tint, Color.Transparent),
                    center = center,
                    radius = hypot(size.width, size.height) * GLOW_REACH,
                ),
            )
        }

        specks.forEach { speck ->
            drawCircle(
                color = Color.White.copy(alpha = speck.alpha),
                radius = speck.radius.toPx(),
                center = Offset(size.width * speck.x, size.height * speck.y),
            )
        }
    }
}

/** One speck of light. design-v3.md 3.3, drawn by radius where the design gives a width. */
@Immutable
private data class Speck(val x: Float, val y: Float, val radius: Dp, val alpha: Float)

/**
 * The specks, from a fixed seed so they never re-randomize, with a seed of this surface's
 * own so onboarding does not borrow the Pulse's stars. `StableHash` rather than `Random`,
 * for the reason the other two backdrops give: a hash gives one arrangement that survives
 * a recomposition, a rotation and a process death with nothing to store.
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
 * How strongly the glow arrives at its center.
 *
 * design-v3.md 3.3 gives the Report's two glows as 6 to 8 percent and the Focus gradient
 * as three named stops, and says nothing about this one's strength. Nine percent sits
 * a whisker above the Report's band, because this surface has no body text on it to
 * protect and because the glow is the only thing distinguishing one beat from the next.
 * design-v3.md 15.
 */
private const val GLOW_ALPHA = 0.09f

/**
 * Where the light pools, and how far it reaches.
 *
 * 0.38 down rather than the middle, which is where the Focus gradient sits for the same
 * reason: the content of every beat is in the upper two thirds and the light should be in
 * the room the content is in. The reach clears the corners so the edges are the ground
 * color rather than a fourth value, exactly as 3.3 requires of the Focus gradient.
 */
private const val GLOW_X = 0.5f
private const val GLOW_Y = 0.38f
private const val GLOW_REACH = 0.72f

private const val SEED = "clarity.onboarding.specks"
private const val SPECKS_MIN = 8
private const val SPECK_SPREAD = 7
private const val POSITION_STEPS = 1_000
private const val SPECK_ALPHA_MIN = 0.03f
private const val SPECK_ALPHA_STEP = 0.01f
private const val ALPHA_STEPS = 4
