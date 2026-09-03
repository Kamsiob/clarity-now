package com.kamsiob.claritynow.ui.theme

import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.Composable
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
 * `MotionScheme` interface that would carry them is **internal** in the material3
 * version this project builds against. Tracked as issue #13 rather than worked around,
 * because a reflective workaround here would break silently on the next alpha.
 *
 * **Checked again in the appeal pass and it is still internal.** A research pass
 * reported the interface as public and the five argument `MaterialTheme` overload as
 * available; writing it produced `Cannot access 'interface MotionScheme': it is
 * internal in file` at four sites. Recorded here so the next reader does not spend the
 * same hour: the way to test this claim is to write the override and compile, which
 * takes a minute, and not to read a release note.
 *
 * What this costs is small and worth naming. Four Material call sites do not inherit
 * calm mode: two `ModalBottomSheet`s, one `Switch` and one `AlertDialog`. The sheet is
 * the app's most opened surface, so it is the one that matters.
 *
 * Reduce motion is one global check, not twenty six individual ones, and calm mode
 * joins that same check rather than adding a second one. design-v3.md 8.5.
 */
interface ClarityMotion {
    /** Presses, promotions, selections, the tab pill. design-v3.md springStandard. */
    fun <T> springStandard(): FiniteAnimationSpec<T>

    /** Sheets, reveals, large elements settling. design-v3.md springGentle. */
    fun <T> springGentle(): FiniteAnimationSpec<T>

    /** Swatches, chips, small immediate feedback. design-v3.md springSnappy. */
    fun <T> springSnappy(): FiniteAnimationSpec<T>

    /**
     * Material 3 Expressive's slow spatial spring, damping 0.8 stiffness 200. For a
     * transform a person is meant to watch rather than to be told about: a sheet
     * settling, a ring closing, a card taking a new place in a list.
     */
    fun <T> springSlowSpatial(): FiniteAnimationSpec<T>

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

    /**
     * **Grounds, scrims and washes. Never content.**
     *
     * The sixth effects token, present in both of Material's own schemes and absent
     * here until the appeal pass. [effects] at 157ms is right for content opacity and
     * reads as a snap on a full screen ground, which is why eight files had reached
     * for a hand written `if (motion.reduced) 150 else 400` instead: ten such branches
     * across four different value pairs, none of them a token.
     *
     * Critically damped at 800, so 222ms and no overshoot. A scrim that overshoots is
     * a scrim that goes darker than it was asked for.
     */
    fun <T> slowEffects(): FiniteAnimationSpec<T>

    /** True when animations should be replaced by an immediate or crossfaded result. */
    val reduced: Boolean

    /** Milliseconds a staggered entrance waits per item. Zero when motion is reduced. */
    val staggerMillis: Int

    /** Duration of the promotion hero, in milliseconds. */
    val promotionMillis: Int

}

/** design-v3.md 8.1. The named curves, used whenever motion is not reduced. */
/**
 * **The spring set is Material 3 Expressive's, read off the tokens rather than tuned.**
 *
 * Expressive ships two spring families and the distinction is the whole system: a
 * **spatial** spring is underdamped and overshoots, and is what anything that moves or
 * changes size uses; an **effects** spring is critically damped at 1.0 and never
 * overshoots, and is what color, opacity and elevation use, because an overshoot in
 * opacity is a flicker and an overshoot in color is a wrong color.
 *
 * | token | damping | stiffness | this app's name |
 * |---|---|---|---|
 * | spatial default | 0.8 | 380 | [springStandard] |
 * | spatial fast | 0.6 | 800 | [springSnappy] |
 * | spatial slow | 0.8 | 200 | [springSlowSpatial] |
 * | effects fast | 1.0 | 3800 | [effectsFast] |
 * | effects default | 1.0 | 1600 | [effects] |
 *
 * [springGentle] at 0.9 / 200 is the one value not from the token set and it is kept: it
 * is the near critically damped spatial spring the focus ring and the report reveal use,
 * where an overshoot would read as the number being wrong.
 *
 * `springSnappy` moved from 0.75 / 600 to the token's 0.6 / 800 in the visual refresh.
 * It is faster and it overshoots more, which is what "fast" means in this system; the
 * old value was a slightly quicker default rather than a different character.
 */
object FullMotion : ClarityMotion {
    override fun <T> springStandard(): FiniteAnimationSpec<T> =
        spring(dampingRatio = 0.8f, stiffness = 380f)

    override fun <T> springGentle(): FiniteAnimationSpec<T> =
        spring(dampingRatio = 0.9f, stiffness = 200f)

    // **0.75 / 600, which is design-v3.md 8.1's value, restored.**
    //
    // The visual refresh set this to Material's `SpringFastSpatial`, 0.6 / 800, whose
    // bounce is 0.40. Apple's own guidance puts the ceiling for a UI element at about
    // 0.4, and design-v3.md 15.1 independently names "a bounce on every hover or press,
    // rather than overshoot reserved for weight" as a tell. This spring drives swatches,
    // chips and the plus button: the highest frequency and lowest weight interactions in
    // the app, which is exactly where overshoot should not be. The authority order settles
    // it in any case.
    override fun <T> springSnappy(): FiniteAnimationSpec<T> =
        spring(dampingRatio = 0.75f, stiffness = 600f)

    override fun <T> springSlowSpatial(): FiniteAnimationSpec<T> =
        spring(dampingRatio = 0.8f, stiffness = 200f)

    override fun <T> easeOut(): FiniteAnimationSpec<T> = tween(350, easing = EaseOutCubic)

    override fun <T> easeSlow(): FiniteAnimationSpec<T> = tween(600, easing = EaseInOutCubic)

    override fun <T> effects(): FiniteAnimationSpec<T> =
        spring(dampingRatio = 1f, stiffness = 1600f)

    override fun <T> effectsFast(): FiniteAnimationSpec<T> =
        spring(dampingRatio = 1f, stiffness = 3800f)

    override fun <T> slowEffects(): FiniteAnimationSpec<T> =
        spring(dampingRatio = 1f, stiffness = 800f)

    override val reduced = false
    override val staggerMillis = 46
    override val promotionMillis = 250
}

/**
 * design-v3.md 8.3. Every animation becomes a 150ms crossfade. The breathing glow
 * holds, the ribbon appears complete, the timer still updates, and a swipe still
 * tracks the finger but commits instantly.
 */
object ReducedMotion : ClarityMotion {
    // **Asymmetric, because a symmetric crossfade is the obvious answer and the
    // slightly cheaper reading one.** Material's own fade tokens are 150 in and 75 out,
    // and NN/g puts an exit 50 to 100ms shorter than its entrance. Reduced motion is a
    // substitute animation, not the absence of one, so it can be shaped.
    private fun <T> fadeIn(): FiniteAnimationSpec<T> = tween(150, easing = EaseOutCubic)
    private fun <T> fadeOut(): FiniteAnimationSpec<T> = tween(75, easing = EaseOutCubic)

    override fun <T> springStandard(): FiniteAnimationSpec<T> = fadeIn()
    override fun <T> springGentle(): FiniteAnimationSpec<T> = fadeIn()
    override fun <T> springSnappy(): FiniteAnimationSpec<T> = fadeOut()

    override fun <T> springSlowSpatial(): FiniteAnimationSpec<T> = fadeIn()
    override fun <T> easeOut(): FiniteAnimationSpec<T> = fadeIn()
    override fun <T> easeSlow(): FiniteAnimationSpec<T> = fadeIn()
    override fun <T> effects(): FiniteAnimationSpec<T> = fadeIn()
    override fun <T> effectsFast(): FiniteAnimationSpec<T> = fadeOut()
    override fun <T> slowEffects(): FiniteAnimationSpec<T> = fadeIn()

    override val reduced = true

    // **Not zero, and this is a correction.** The stagger carries no displacement, so
    // it is not a vestibular trigger and there was nothing about it to reduce. WebKit's
    // guidance is explicit that the instruction is to remove the animations known to be
    // triggers, and it warns against over-reduction; MDN's canonical example replaces a
    // transform with an opacity keyframe rather than removing the animation. Apple's is
    // about magnitude and dimensionality, never about existence.
    //
    // What zeroing it actually removed was reading order, from the reader most helped by
    // having one. Halved rather than kept, because a reduced sequence should still be
    // shorter than a full one.
    override val staggerMillis = 23
    override val promotionMillis = 150
}

/** The breathing glow holds here instead of animating when motion is reduced. */
const val REDUCED_GLOW_OPACITY = 0.92f

/**
 * The tutorial ring pulse holds here rather than animating. design-v3.md 16.2.
 *
 * The midpoint of the 0.25 to 0.45 range in 8.2 item 19, which is the same treatment
 * [REDUCED_GLOW_OPACITY] gives the breathing glow. Unlike the glow, this one is calm
 * mode's addition rather than 8.3's, because 8.3 never says what the pulse does.
 */
const val CALM_TUTORIAL_PULSE_OPACITY = 0.35f

/**
 * True when the system animator duration scale is 0 or the accessibility setting
 * asks for reduced motion. Set once at the top of the tree by ClarityTheme.
 */
val LocalReduceMotion = compositionLocalOf { false }

/**
 * The one global motion check. design-v3.md 8.3 and 8.5.
 *
 * **Calm mode joins this flag rather than adding a level beside it.** One boolean is
 * true when the system asks for reduced motion **or** calm mode is on, and every
 * animation in the app reads it through this function. There is no third motion level,
 * no per-animation opt out, and nothing anywhere that reads `LocalCalmMode` in order to
 * pick a different curve.
 *
 * The `or` is also what makes design-v3.md 16.1's rule true in code: **reduce motion
 * always wins on motion.** Calm mode is a superset of 8.3, never an override of it, so
 * turning calm mode off while the system asks for reduced motion restores color and not
 * movement. There is no arrangement of the two switches that animates against an
 * accessibility setting.
 *
 * Calm mode goes beyond this flag in exactly two places, and neither is a curve: the
 * entrances in 8.4 do not fire at all rather than firing as a crossfade, which
 * `ClarityEntrance.kt` handles, and the tutorial ring pulse holds at
 * [CALM_TUTORIAL_PULSE_OPACITY].
 */
@Composable
@ReadOnlyComposable
fun clarityMotion(): ClarityMotion =
    if (LocalReduceMotion.current || LocalCalmMode.current) ReducedMotion else FullMotion
