package com.kamsiob.claritynow.ui.theme

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * **How a page comes together.**
 *
 * The shipping build faded a screen in with a 16dp rise on a 350ms cubic, one treatment
 * for every element, once per session. Two things were wrong with that and they are
 * different problems. It was **one motion**, so a screen assembled as a single slab and
 * nothing about the arrival said what the screen was made of. And it was **spent after
 * the first visit**, so the app was at its most alive in the ten seconds a person saw it
 * least and inert forever after.
 *
 * The replacement is a choreography with four parts, and the rule that generates it is
 * one sentence:
 *
 * > **Everything enters from where it lives, in the order a person reads it.**
 *
 * | role | motion | why |
 * |---|---|---|
 * | [ClarityEntranceRole.HEADER] | settles down 10dp, no scale | a header is already at the top of the page; it arrives by coming to rest, not by moving |
 * | [ClarityEntranceRole.DOMINANT] | scales 0.94 to 1, rises 8dp, from its own top left | the one large sentence on a screen is the thing that grows into the room |
 * | [ClarityEntranceRole.ROW] | rises 18dp, staggered by reading order | a list arrives as a list, one item after another |
 * | [ClarityEntranceRole.CHROME] | fades only, no travel | a tab bar that slides is a tab bar that moved, and it did not |
 *
 * **Transform runs on a spring and opacity runs on a tween, never the other way round.**
 * A spring on alpha reads as a flicker because the overshoot has nowhere to go, and a
 * tween on a transform reads as a slide because it arrives at constant speed and stops.
 * The spring is the Material 3 Expressive default spatial spring, damping 0.8 and
 * stiffness 380, which is the same spring the rest of this app already presses with, so
 * an arrival and a press are the same physical world.
 *
 * **Reduced motion and calm mode collapse the whole thing to nothing**, not to a shorter
 * version of itself: [clarityMotion] reports `reduced`, every travel distance goes to
 * zero, the stagger goes to zero and only the opacity tween remains.
 */
@Immutable
class ScreenEntrance(val playing: Boolean, val generation: Int)

val LocalScreenEntrance = compositionLocalOf { ScreenEntrance(false, 0) }

enum class ClarityEntranceRole { HEADER, DOMINANT, ROW, CHROME }

private const val ENTRANCE_FLOOR_ALPHA = 0.35f

private val HEADER_FALL: Dp = (-10).dp
private val DOMINANT_RISE: Dp = 8.dp
private val ROW_RISE: Dp = 18.dp
private const val DOMINANT_SCALE_FROM = 0.94f

/**
 * **The stagger has a ceiling.**
 *
 * Index times 46ms with no cap means the twelfth card on a long screen starts half a
 * second after the first, by which time a thumb has already scrolled it, and the window
 * below has to be long enough to cover a list of unknown length. Eight steps is the last
 * one a person can still read as a sequence; past that everything arrives together, which
 * is what "the rest of the list" should look like anyway.
 */
private const val MAX_STAGGER_STEPS = 8

/** Long enough for the last staggered row to finish. */
private const val ENTRANCE_WINDOW_MILLIS = 380L + MAX_STAGGER_STEPS * 46L

/**
 * Plays the choreography every time the screen is entered, which is the change from the
 * shipping build's once per session. A tab a person opens forty times a day is the
 * surface that most needs to feel alive, and a 380ms settle that a thumb outruns is not
 * a cost a person pays; it is what tells them the page is new.
 */
@Composable
fun TabEntrance(content: @Composable () -> Unit) {
    var generation by remember { mutableIntStateOf(0) }
    var playing by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        generation += 1
        playing = true
        delay(ENTRANCE_WINDOW_MILLIS)
        playing = false
    }

    val entrance = remember(playing, generation) { ScreenEntrance(playing, generation) }
    CompositionLocalProvider(LocalScreenEntrance provides entrance) { content() }
}

/**
 * `index` is reading order within the screen, not layout order: it decides only when an
 * element starts, and two elements a person reads as one thing should share an index.
 */
@Composable
fun Modifier.clarityEntrance(
    index: Int = 0,
    role: ClarityEntranceRole = ClarityEntranceRole.ROW,
): Modifier {
    val motion = clarityMotion()
    val calm = LocalCalmMode.current
    val entrance = LocalScreenEntrance.current

    val plays = remember(entrance.generation) { entrance.playing && !calm }
    if (!plays) return this

    val density = LocalDensity.current
    val travel = with(density) {
        if (motion.reduced) {
            0f
        } else {
            when (role) {
                ClarityEntranceRole.HEADER -> HEADER_FALL.toPx()
                ClarityEntranceRole.DOMINANT -> DOMINANT_RISE.toPx()
                ClarityEntranceRole.ROW -> ROW_RISE.toPx()
                ClarityEntranceRole.CHROME -> 0f
            }
        }
    }
    val scaleFrom = if (motion.reduced || role != ClarityEntranceRole.DOMINANT) {
        1f
    } else {
        DOMINANT_SCALE_FROM
    }

    val settle = remember(entrance.generation) { Animatable(0f) }
    val fade = remember(entrance.generation) { Animatable(0f) }
    val delayMillis =
        (index.coerceIn(0, MAX_STAGGER_STEPS) * motion.staggerMillis).toLong()

    LaunchedEffect(entrance.generation) {
        if (delayMillis > 0L) delay(delayMillis)
        // Opacity and transform are started together and land apart on purpose: the
        // fade is over in 200ms so the text is readable immediately, and the spring
        // keeps settling under text a person has already started reading.
        kotlinx.coroutines.coroutineScope {
            launch { fade.animateTo(1f, tween(if (motion.reduced) 150 else 200)) }
            launch { settle.animateTo(1f, motion.springStandard()) }
        }
    }

    return graphicsLayer {
        // **Opacity starts at 0.35, not at 0, and the reason is the tab crossfade.**
        //
        // `AnimatedContent` in `ClarityShell` already fades the whole screen in on every
        // tab switch. An element fading 0 to 1 underneath that is a second opacity ramp
        // multiplied onto the first, so a card was arriving at 0.25 alpha when the screen
        // itself was at 0.5, which reads as a wash rather than as an arrival. Starting
        // partway up leaves the entrance doing what it is for, which is the transform,
        // and lets the crossfade own the fade.
        alpha = ENTRANCE_FLOOR_ALPHA + (1f - ENTRANCE_FLOOR_ALPHA) * fade.value
        translationY = (1f - settle.value) * travel
        val s = scaleFrom + (1f - scaleFrom) * settle.value
        scaleX = s
        scaleY = s
        transformOrigin = TransformOrigin(0f, 0f)
    }
}
