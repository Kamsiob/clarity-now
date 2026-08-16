package com.kamsiob.claritynow.ui.theme

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf

/**
 * design-v3.md section 8. Every animation in the app draws its timing from here.
 *
 * Reduce motion is one global check, not twenty six individual ones: call
 * [clarityMotion] inside a composable and every spec collapses to a 150ms
 * crossfade when the user has asked for less movement.
 */
interface ClarityMotion {
    fun <T> springStandard(): AnimationSpec<T>
    fun <T> springGentle(): AnimationSpec<T>
    fun <T> springSnappy(): AnimationSpec<T>
    fun <T> easeOut(): AnimationSpec<T>
    fun <T> easeSlow(): AnimationSpec<T>

    /** True when animations should be replaced by an immediate or crossfaded result. */
    val reduced: Boolean

    /** Milliseconds a staggered entrance waits per item. Zero when motion is reduced. */
    val staggerMillis: Int

    /** Duration of the promotion hero, in milliseconds. */
    val promotionMillis: Int
}

/** design-v3.md 8.1. The named curves, used whenever motion is not reduced. */
object FullMotion : ClarityMotion {
    override fun <T> springStandard(): AnimationSpec<T> =
        spring(dampingRatio = 0.8f, stiffness = 380f)

    override fun <T> springGentle(): AnimationSpec<T> =
        spring(dampingRatio = 0.9f, stiffness = 200f)

    override fun <T> springSnappy(): AnimationSpec<T> =
        spring(dampingRatio = 0.75f, stiffness = 600f)

    override fun <T> easeOut(): AnimationSpec<T> = tween(350, easing = EaseOutCubic)

    override fun <T> easeSlow(): AnimationSpec<T> = tween(600, easing = EaseInOutCubic)

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
    private fun <T> crossfade(): AnimationSpec<T> = tween(150, easing = EaseOutCubic)

    override fun <T> springStandard(): AnimationSpec<T> = crossfade()
    override fun <T> springGentle(): AnimationSpec<T> = crossfade()
    override fun <T> springSnappy(): AnimationSpec<T> = crossfade()
    override fun <T> easeOut(): AnimationSpec<T> = crossfade()
    override fun <T> easeSlow(): AnimationSpec<T> = crossfade()

    override val reduced = true
    override val staggerMillis = 0
    override val promotionMillis = 150
}

/** The breathing glow holds here instead of animating when motion is reduced. */
const val REDUCED_GLOW_OPACITY = 0.92f

/**
 * True when the system animator duration scale is 0 or the accessibility setting
 * asks for reduced motion. Set once at the top of the tree by [ClarityTheme].
 */
val LocalReduceMotion = compositionLocalOf { false }

@Composable
@ReadOnlyComposable
fun clarityMotion(): ClarityMotion = if (LocalReduceMotion.current) ReducedMotion else FullMotion
