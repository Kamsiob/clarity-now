package com.kamsiob.claritynow.ui.report

import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kamsiob.claritynow.ui.theme.clarityMotion
import kotlinx.coroutines.delay

/**
 * The Report reveal. `design-v3.md` 8.2 item 12, 8.3 and 8.4.
 *
 * > Eyebrow, then headline scaling from 0.96 with springGentle, then the week ribbon
 * > drawing left to right at 45ms per day, then sections fading and rising 12dp at 90ms
 * > stagger. Under 1.4 seconds. The ribbon draw should be the most satisfying single
 * > animation after the promotion.
 *
 * ## The budget, written down because 1.4 seconds is a hard ceiling
 *
 * v3 gives the ribbon's 45ms per day, the sections' 90ms stagger and the total, and leaves
 * the four start times open. These are them, and they are chosen so that the last thing on
 * the longest page has settled at 1,380ms:
 *
 * | element | starts | ends |
 * |---|---|---|
 * | eyebrow | 0 | 240 |
 * | headline | 140 | settles under the spring |
 * | ribbon block | 380 | 380 + 270 of stagger + 180 of draw = 830 |
 * | sections | 780 | 780 + 360 of stagger + 240 = 1,380 |
 *
 * **The stagger is capped at [MAX_STAGGER_STEPS] steps and that is what makes the ceiling
 * hold.** A report can carry a first week note, three sideheads, a pattern, a closing line
 * and a footer, and eight blocks at 90ms would put the last one 720ms behind the first and
 * take the whole reveal past 1.7 seconds. Past the fifth block the delay stops growing and
 * the remainder arrive together, which nobody can see as a fault and which keeps a long
 * report from being slower to read than a short one. `design-v3.md` 15: the obvious answer
 * is to stagger everything, and the obvious answer breaks the one number this entry states
 * as a limit.
 *
 * ## The two flags
 *
 * Calm mode removes the entrance entirely, 16.2, and the caller passes `playing = false`.
 * Reduce motion keeps it and turns it into one 150ms crossfade with no stagger and no
 * travel, 8.3, which is every delay here collapsing to zero and the rise going with it.
 */
@Composable
internal fun Modifier.reveal(playing: Boolean, delayMillis: Int, rise: Dp = 0.dp): Modifier {
    val motion = clarityMotion()
    if (!playing) return this

    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { started = true }

    val progress by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(
            durationMillis = if (motion.reduced) REDUCED_MILLIS else FADE_MILLIS,
            delayMillis = if (motion.reduced) 0 else delayMillis,
            easing = EaseOutCubic,
        ),
        label = "reportReveal",
    )
    // A crossfade has no travel, so reduce motion drops the rise rather than shortening it.
    val risePx = with(LocalDensity.current) { if (motion.reduced) 0f else rise.toPx() }

    return graphicsLayer {
        alpha = progress
        translationY = (1f - progress) * risePx
    }
}

/**
 * The headline's own entrance: scaling from 0.96 with springGentle, per 8.2 item 12.
 *
 * A spring rather than a tween because that is what the entry names, and the fade rides the
 * same value so the type never arrives at full opacity while it is still moving. Under
 * reduce motion it is the same 150ms crossfade as everything else, with the scale held at
 * one: a scale is travel.
 */
@Composable
internal fun Modifier.revealHeadline(playing: Boolean): Modifier {
    val motion = clarityMotion()
    if (!playing) return this

    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(if (motion.reduced) 0L else HEADLINE_AT.toLong())
        started = true
    }

    val progress by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = if (motion.reduced) {
            tween(durationMillis = REDUCED_MILLIS, easing = EaseOutCubic)
        } else {
            motion.springGentle()
        },
        label = "reportHeadline",
    )

    return graphicsLayer {
        alpha = progress
        val scale = if (motion.reduced) 1f else HEADLINE_FROM + (1f - HEADLINE_FROM) * progress
        scaleX = scale
        scaleY = scale
    }
}

/** The delay a block in the sections run waits, in the order it is read. */
internal fun sectionAt(step: Int): Int =
    SECTIONS_AT + SECTION_STAGGER * step.coerceIn(0, MAX_STAGGER_STEPS)

/** How long the whole reveal takes, which is when the caller may mark it spent. */
internal fun totalRevealMillis(reduced: Boolean): Long =
    if (reduced) REDUCED_MILLIS.toLong() else (sectionAt(MAX_STAGGER_STEPS) + FADE_MILLIS).toLong()

/** 8.2 item 12. Where the headline scales from. */
private const val HEADLINE_FROM = 0.96f

internal const val EYEBROW_AT = 0
internal const val HEADLINE_AT = 140
internal const val RIBBON_BLOCK_AT = 380
internal const val SECTIONS_AT = 780

/** 8.2 item 12. */
internal const val SECTION_STAGGER = 90

/** See the note above. The stagger stops growing here so the 1.4 second ceiling holds. */
internal const val MAX_STAGGER_STEPS = 4

internal const val FADE_MILLIS = 240

/** 8.3. The one crossfade every animation becomes. */
internal const val REDUCED_MILLIS = 150

/** 8.2 item 12. Sections fade and rise this far. */
internal val RISE = 12.dp
