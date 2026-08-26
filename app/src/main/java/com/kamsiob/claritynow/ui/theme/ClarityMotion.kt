package com.kamsiob.claritynow.ui.theme

import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf

/**
 * design-v3.md section 8, expressed as a Material 3 Expressive motion theme.
 *
 * Material 3 Expressive replaces duration and easing pairs with springs as the
 * primary motion model, because a spring can be retargeted mid flight and the
 * physics engine recalculates a natural trajectory, where a duration based
 * animation interrupted halfway looks broken. design-v3.md already specified this
 * app's motion as springs, so the two systems agree on the model and disagree only
 * on the constants.
 *
 * Where they disagree, design-v3.md wins. Material 3 Expressive's own spatial and
 * effects tokens are close enough to these springs that a Material component and a
 * hand written Clarity animation running side by side read as one piece.
 *
 * Injecting these springs into Material directly would be better still, and the
 * MotionScheme interface that would carry them is internal in material3
 * 1.5.0-alpha26 while the expressive theme is folded into MaterialTheme. Tracked as
 * an issue rather than worked around, because a reflective workaround here would
 * break silently on the next alpha.
 *
 * Reduce motion is one global check, not twenty six individual ones.
 */
interface ClarityMotion {
    /** Presses, promotions, selections, the tab pill. design-v3.md springStandard. */
    fun <T> springStandard(): FiniteAnimationSpec<T>

    /** Sheets, reveals, large elements settling. design-v3.md springGentle. */
    fun <T> springGentle(): FiniteAnimationSpec<T>

    /** Swatches, chips, small immediate feedback. design-v3.md springSnappy. */
    fun <T> springSnappy(): FiniteAnimationSpec<T>

    /** Entrances and fades. One of the two places the design names a duration. */
    fun <T> easeOut(): FiniteAnimationSpec<T>

    /** World transitions and the breathing glow. */
    fun <T> easeSlow(): FiniteAnimationSpec<T>

    /**
     * Non spatial change: opacity, color, elevation. Critically damped, because an
     * alpha that overshoots is a bug rather than a flourish.
     */
    fun <T> effects(): FiniteAnimationSpec<T>

    fun <T> effectsFast(): FiniteAnimationSpec<T>

    /** True when animations should be replaced by an immediate or crossfaded result. */
    val reduced: Boolean

    /** Milliseconds a staggered entrance waits per item. Zero when motion is reduced. */
    val staggerMillis: Int

    /** Duration of the promotion hero, in milliseconds. */
    val promotionMillis: Int

}

/** design-v3.md 8.1. The named curves, used whenever motion is not reduced. */
object FullMotion : ClarityMotion {
    override fun <T> springStandard(): FiniteAnimationSpec<T> =
        spring(dampingRatio = 0.8f, stiffness = 380f)

    override fun <T> springGentle(): FiniteAnimationSpec<T> =
        spring(dampingRatio = 0.9f, stiffness = 200f)

    override fun <T> springSnappy(): FiniteAnimationSpec<T> =
        spring(dampingRatio = 0.75f, stiffness = 600f)

    override fun <T> easeOut(): FiniteAnimationSpec<T> = tween(350, easing = EaseOutCubic)

    override fun <T> easeSlow(): FiniteAnimationSpec<T> = tween(600, easing = EaseInOutCubic)

    override fun <T> effects(): FiniteAnimationSpec<T> =
        spring(dampingRatio = 1f, stiffness = 1600f)

    override fun <T> effectsFast(): FiniteAnimationSpec<T> =
        spring(dampingRatio = 1f, stiffness = 3800f)

    override val reduced = false
    override val staggerMillis = 50
    override val promotionMillis = 250
}

/**
 * design-v3.md 8.3. Every animation becomes a 150ms crossfade. The breathing glow
 * holds, the ribbon appears complete, the timer still updates, and a swipe still
 * tracks the finger but commits instantly.
 */
object ReducedMotion : ClarityMotion {
    private fun <T> crossfade(): FiniteAnimationSpec<T> = tween(150, easing = EaseOutCubic)

    override fun <T> springStandard(): FiniteAnimationSpec<T> = crossfade()
    override fun <T> springGentle(): FiniteAnimationSpec<T> = crossfade()
    override fun <T> springSnappy(): FiniteAnimationSpec<T> = crossfade()
    override fun <T> easeOut(): FiniteAnimationSpec<T> = crossfade()
    override fun <T> easeSlow(): FiniteAnimationSpec<T> = crossfade()
    override fun <T> effects(): FiniteAnimationSpec<T> = crossfade()
    override fun <T> effectsFast(): FiniteAnimationSpec<T> = crossfade()

    override val reduced = true
    override val staggerMillis = 0
    override val promotionMillis = 150
}

/** The breathing glow holds here instead of animating when motion is reduced. */
const val REDUCED_GLOW_OPACITY = 0.92f

/**
 * True when the system animator duration scale is 0 or the accessibility setting
 * asks for reduced motion. Set once at the top of the tree by ClarityTheme.
 */
val LocalReduceMotion = compositionLocalOf { false }

@Composable
@ReadOnlyComposable
fun clarityMotion(): ClarityMotion = if (LocalReduceMotion.current) ReducedMotion else FullMotion
