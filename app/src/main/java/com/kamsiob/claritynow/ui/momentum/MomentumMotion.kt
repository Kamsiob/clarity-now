package com.kamsiob.claritynow.ui.momentum

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.kamsiob.claritynow.ui.theme.LocalCalmMode
import com.kamsiob.claritynow.ui.theme.LocalScreenEntrance
import com.kamsiob.claritynow.ui.theme.clarityMotion
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

/**
 * The two animations design-v3.md 8.2 gives Momentum, items 13 and 14.
 *
 * ```
 * 13. Momentum dot cascade. Left to right at 35ms stagger. The today ring draws last.
 * 14. Momentum number roll. The three stats count up from 0 over 600ms easeOut.
 * ```
 *
 * **Both are entrances, so 8.4 governs them and it is not restated at either call site.**
 * They fire on the first open of the Momentum tab per app session and never again;
 * returning to the tab renders settled, at rest, with no fade and no counting. 8.4 names
 * item 14 as the animation the general rule was generalized from, which makes this the one
 * screen where getting it wrong would be getting the precedent wrong.
 *
 * **Written out rather than routed through `Modifier.clarityEntrance`**, and the reason is
 * two numbers. That modifier is 8.2 item 4: a 350ms fade with a 16dp rise at the 50ms
 * stagger the motion theme carries. Item 13 is a 35ms stagger with no rise, and item 14 is
 * not a fade at all. Sharing the modifier would mean widening it with two parameters that
 * exactly one screen ever passes. What is shared is the thing that matters, which is
 * [entrancePlays]: one reading of `LocalScreenEntrance` and one of `LocalCalmMode`, so the
 * once per session rule and calm mode's removal of entrances are answered in one place for
 * both animations.
 *
 * ## Calm mode and reduce motion
 *
 * design-v3.md 16.2: in calm mode entrances do not fire at all. 8.3: under reduce motion
 * every animation becomes a 150ms crossfade, and it says what that means for a drawn
 * graphic in the entry beside item 12, "the ribbon appears complete".
 *
 * So the dots fade in together over 150ms under reduce motion, with no stagger and the
 * ring arriving with them, and do not animate at all in calm mode. **The number roll has
 * no reduced form and renders its value.** A count up is not a fade, and 150ms of counting
 * would be a flicker of wrong numbers rather than a gentler animation. That is the same
 * reading 8.3 takes of the focus ring, which keeps updating rather than crossfading.
 */
private const val CASCADE_STAGGER_MILLIS = 35

/** 8.2 item 13. The dot's own fade, once its turn in the cascade arrives. */
private const val DOT_FADE_MILLIS = 200

/** 8.2 item 14. */
private const val ROLL_MILLIS = 600

/** 8.3's crossfade, for the reduce motion form of the cascade. */
private const val REDUCED_FADE_MILLIS = 150

/**
 * Whether an entrance on this screen should play at all, decided once and never revisited.
 *
 * Captured in a `remember` for the reason `Modifier.clarityEntrance` captures its own: an
 * element whose entrance is in flight when the arrival window closes has to finish it, and
 * a dot recomposed for any other reason must not start a second one.
 */
@Composable
private fun entrancePlays(): Boolean {
    val playing = LocalScreenEntrance.current.playing
    val calm = LocalCalmMode.current
    return remember { playing && !calm }
}

/**
 * The opacity of one mark in the dot row, 0 to 1. design-v3.md 8.2 item 13.
 *
 * [index] is the mark's position in the cascade, left to right, and the today ring is
 * given the position after the last dot so that it draws last, which is the half of item
 * 13 that is easy to lose.
 */
@Composable
fun dotCascadeAlpha(index: Int): State<Float> {
    val motion = clarityMotion()
    val plays = entrancePlays()
    val settled = remember { mutableFloatStateOf(1f) }
    if (!plays) return settled

    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        if (motion.reduced) {
            // 8.3. Everything arrives together over one crossfade, with no stagger.
            progress.animateTo(1f, tween(REDUCED_FADE_MILLIS, easing = EaseOutCubic))
        } else {
            delay(index.toLong() * CASCADE_STAGGER_MILLIS)
            progress.animateTo(1f, tween(DOT_FADE_MILLIS, easing = EaseOutCubic))
        }
    }
    return progress.asState()
}

/**
 * A figure counting up from zero to [target] over 600ms. design-v3.md 8.2 item 14.
 *
 * Returns [target] immediately when the entrance is spent, when calm mode is on, or when
 * motion is reduced. It also returns [target] immediately when the value changes while the
 * screen is open: a stat that re-counted because a number moved would be an entrance
 * firing on a data change, which 8.4 allows for exactly one animation in the app and it is
 * the Report reveal.
 */
@Composable
fun rolledFigure(target: Int): Int {
    val motion = clarityMotion()
    val plays = entrancePlays()
    val rolls = remember { plays && !motion.reduced }
    var shown by remember { mutableIntStateOf(if (rolls) 0 else target) }
    // Spent by the first roll, so a figure that moves while the screen is open changes to
    // its new value rather than counting up to it again.
    var spent by remember { mutableStateOf(false) }

    LaunchedEffect(target) {
        if (!rolls || spent) {
            shown = target
            return@LaunchedEffect
        }
        spent = true
        val progress = Animatable(0f)
        progress.animateTo(1f, tween(ROLL_MILLIS, easing = EaseOutCubic)) {
            shown = (value * target).roundToInt()
        }
        shown = target
    }
    return shown
}
