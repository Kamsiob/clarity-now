package com.kamsiob.claritynow.ui.focus

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import com.kamsiob.claritynow.R
import com.kamsiob.claritynow.data.repo.FocusCountdown
import com.kamsiob.claritynow.ui.components.ClarityIcon
import com.kamsiob.claritynow.ui.components.ClarityIcons
import com.kamsiob.claritynow.ui.components.TabularNumber
import com.kamsiob.claritynow.ui.theme.ClarityHapticEvent
import com.kamsiob.claritynow.ui.theme.ClaritySpacing
import com.kamsiob.claritynow.ui.theme.FocusPalette
import com.kamsiob.claritynow.ui.theme.LocalCalmMode
import com.kamsiob.claritynow.ui.theme.LocalClarityHaptics
import com.kamsiob.claritynow.ui.theme.LocalClarityTypography
import com.kamsiob.claritynow.ui.theme.LocalContemplativeColors
import com.kamsiob.claritynow.ui.theme.calmed
import com.kamsiob.claritynow.ui.theme.cappedFontScale
import com.kamsiob.claritynow.ui.theme.clarityMotion
import kotlin.math.cos
import kotlin.math.sin
import kotlinx.coroutines.launch

/** design-v3.md section 11. The one dimension the ring is allowed to be. */
internal val RING_DIAMETER = 240.dp

/**
 * The dial: the ring, the numeral inside it, and the word beneath the numeral.
 *
 * Two of design-v3.md 11's six elements, drawn together because 10.18 puts the word
 * `remaining` beneath the numeral rather than beneath the ring.
 *
 * **This is the only composable on the surface that recomposes once a second**, which
 * is what design-v3.md 8.2 item 7 asks for: one ticker, and only the numeral and the
 * arc redraw. Everything else on the session screen is handed values that a tick
 * cannot change.
 *
 * **Duration reads as a shape before it reads as a number.** Addendum 01 8d and
 * design-v3.md 11.3: the depleting arc is the primary carrier and the digits confirm
 * it, which is why the numeral is capped at 1.3x the font scale and the ring is not.
 * A person reading a shrinking arc is not doing arithmetic.
 *
 * **Nothing here fires a haptic**, which design-v3.md 9 requires: a session fires
 * nothing between start and end, with one exception the user has to switch on. That
 * exception is the single `transitionWarn` tick at the five minute mark, and it wants a
 * haptic event that `ui/theme/ClarityHaptics.kt` does not carry yet. The visible half
 * of the warning, the tick brightening and the word changing, is complete below.
 */
@Composable
internal fun FocusDial(
    countdown: () -> FocusCountdown?,
    plannedSecondsFallback: Int,
    transitionWarningEnabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val contemplative = LocalContemplativeColors.current
    val type = LocalClarityTypography.current
    val motion = clarityMotion()

    // The tick is read here and nowhere above, which is what keeps the once a second
    // redraw to this composable. design-v3.md 8.2 item 7.
    val tick = countdown()
    val targetFraction = tick?.fractionRemaining ?: 1f
    val targetSeconds = tick?.remainingSeconds ?: plannedSecondsFallback
    val plannedSeconds = tick?.plannedSeconds ?: plannedSecondsFallback

    val arc = remember { Animatable(targetFraction) }
    val seconds = remember { Animatable(targetSeconds.toFloat()) }
    var lastPlanned by remember { mutableIntStateOf(plannedSeconds) }

    // design-v3.md 8.2 item 28. An extension grows the arc to its new length and rolls
    // the numeral over the same interval rather than either of them jumping. Every
    // other second is a snap, because item 7 puts the depletion at 1Hz from one ticker
    // and animating between ticks would turn the countdown into a per frame animation.
    // A tick arriving mid roll retargets the spring instead of cutting it off.
    LaunchedEffect(targetSeconds, plannedSeconds) {
        val extended = plannedSeconds > lastPlanned
        lastPlanned = plannedSeconds
        if (extended || arc.isRunning) {
            launch { arc.animateTo(targetFraction, motion.springGentle()) }
            launch { seconds.animateTo(targetSeconds.toFloat(), motion.springGentle()) }
        } else {
            arc.snapTo(targetFraction)
            seconds.snapTo(targetSeconds.toFloat())
        }
    }

    val shownSeconds = seconds.value.toInt().coerceAtLeast(0)
    val mark = if (transitionWarningEnabled) tick?.transitionMarkFraction else null
    val markReached = transitionWarningEnabled && tick?.pastTransitionMark == true

    // The one haptic a focus session is allowed between its start and its end, and
    // only when the person turned the warning on. design-v3.md 9 otherwise says a
    // session fires nothing in between; Addendum 01 4g is the single authorized
    // exception and the v3.1 history entry records it.
    //
    // Keyed on the boolean rather than on the tick, so it fires once when the mark is
    // crossed and never again, including when the arc grows past it again after an
    // Add 10 minutes and crosses back later.
    val haptics = LocalClarityHaptics.current
    LaunchedEffect(markReached) {
        if (markReached) haptics.perform(ClarityHapticEvent.TRANSITION_WARN)
    }

    val spoken = if (shownSeconds < SECONDS_PER_MINUTE) {
        stringResource(R.string.cd_focus_under_a_minute)
    } else {
        val minutes = spokenMinutes(shownSeconds)
        pluralStringResource(R.plurals.cd_focus_remaining, minutes, minutes)
    }

    Box(
        // One node for a screen reader rather than a ring, four digits, a colon and a
        // word. The description is in whole minutes, so it changes once a minute rather
        // than once a second, which is also the honest resolution of a spoken figure.
        modifier = modifier
            .size(RING_DIAMETER)
            .clearAndSetSemantics { contentDescription = spoken },
        contentAlignment = Alignment.Center,
    ) {
        FocusArc(fraction = arc.value, markFraction = mark, markReached = markReached)

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // design-v3.md 5.3 and 13: the timer numeral caps at 1.3x the font scale.
            // The ring does not grow with the text, so a numeral that kept scaling
            // would eventually be wider than the shape that has to contain it.
            //
            // **The cap is against the combined scale, phone times app setting, and it
            // is met with nothing added here.** `ClarityTheme` applies the in app text
            // size to `LocalDensity`, so `fontScale` below is already the product; a cap
            // written against the phone's figure alone would have let 200 percent times
            // Largest through at 2.0 and put a 128sp numeral inside a 244dp ring.
            //
            // `cappedFontScale` replaced a plain `Density(density, 1.3f)` for the reason
            // `ClarityTextSize.kt` gives: that constructor discards the platform's non
            // linear sp curve, and this is the largest size in the app, which is exactly
            // where the curve is doing the most work.
            val density = LocalDensity.current
            val capped = remember(density) { density.cappedFontScale(NUMERAL_MAX_FONT_SCALE) }
            CompositionLocalProvider(LocalDensity provides capped) {
                // Hanken Grotesk ships no tnum feature, so the digits are laid out in
                // slots of the widest digit rather than trusted to the file.
                TabularNumber(
                    text = formatCountdown(shownSeconds),
                    style = type.timerNumeral,
                    color = contemplative.textBright,
                )
            }
            Spacer(Modifier.height(ClaritySpacing.scaled(2.dp)))
            Text(
                // design-v3.md 10.18: at the mark the word becomes `5 minutes left` and
                // stays. It is a landmark that was already on the track rather than an
                // event that arrives, and the numeral goes on carrying the live figure.
                text = if (markReached) {
                    stringResource(R.string.focus_transition_mark)
                } else {
                    stringResource(R.string.focus_remaining)
                },
                style = type.label,
                // `textDim`. design-v3.md 11 listed this word as `textFaint` among the
                // six elements of the Focus surface, and 13 says Contemplative text
                // stays at or above 55 percent opacity where it is meant to be read.
                // The section is corrected rather than left standing beside the floor:
                // 32 percent measures 2.554 to one at the center of the indigo
                // gradient and 55 measures 4.659, and at the mark this line changes to
                // `5 minutes left`, which is a thing the surface says rather than a
                // decoration under the numeral.
                color = contemplative.textDim,
            )
        }
    }
}

/**
 * The depleting ring itself. design-v3.md 3.3, 11.3 and 17.3.
 *
 * One of the six components this app builds by hand, and the platform first analysis
 * is already recorded: a determinate progress indicator counts up toward a goal and is
 * drawn to say so, while this counts down, holds a 64sp numeral inside a 240dp ring and
 * carries the transition mark on its track. `DECISIONS.md` has the register row.
 *
 * **The arc depletes clockwise from the top**, 11.3, which is the one case that section
 * names as exempt from the open choice rule: a countdown running the other way is a
 * puzzle, and legibility outranks distinctiveness on the one element in this app that
 * has to be read at a glance while doing something else. So the empty part of the ring
 * grows clockwise from the top and the head of what is left chases it around.
 *
 * **It does not mirror in a right to left layout**, which is why nothing here reads the
 * layout direction. design-v3.md 17.5 mirrors anything with a leading and a trailing
 * edge; a clock face has neither, and time does not run backwards in any locale.
 */
@Composable
private fun FocusArc(
    fraction: Float,
    markFraction: Float?,
    markReached: Boolean,
    modifier: Modifier = Modifier,
) {
    val calm = LocalCalmMode.current
    val motion = clarityMotion()

    // design-v3.md 16.7: the track is white and is untouched, the stroke and the tip
    // take the transform, and the tip loses its blur.
    val track = FocusPalette.ringTrack
    val progress = FocusPalette.ringProgress.calmed(calm)
    val tip = FocusPalette.ringTip.calmed(calm)

    // design-v3.md 8.2 item 27. The tick brightens once and holds: no color change, no
    // pulse, no repeat, and nothing moves except the tick. 400ms is named in that entry
    // and 150ms is the reduce motion path, so both are written out the way the tab
    // crossfade in `ui/nav/ClarityShell.kt` is.
    val markAlpha = animateFloatAsState(
        targetValue = if (markReached) MARK_ALPHA_BRIGHT else MARK_ALPHA_FAINT,
        animationSpec = tween(
            durationMillis = if (motion.reduced) 150 else 400,
            easing = EaseOutCubic,
        ),
        label = "transitionMark",
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val stroke = RING_STROKE.toPx()
        val radius = (size.minDimension - stroke) / 2f
        val center = Offset(size.width / 2f, size.height / 2f)

        drawCircle(color = track, radius = radius, center = center, style = Stroke(stroke))

        if (markFraction != null) {
            drawTransitionMark(
                center = center,
                radius = radius,
                angle = angleFor(markFraction),
                alpha = markAlpha.value,
            )
        }

        val remaining = fraction.coerceIn(0f, 1f)
        if (remaining <= 0f) return@Canvas

        val head = angleFor(remaining)
        drawArc(
            color = progress,
            startAngle = head,
            sweepAngle = 360f * remaining,
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2f, radius * 2f),
            style = Stroke(width = stroke, cap = StrokeCap.Round),
        )

        val headPoint = pointOn(center, radius, head)
        if (!calm) {
            // The soft blur in 3.3, drawn as a falloff rather than as a mask filter: a
            // gradient is what a blurred point of light looks like, and it costs one
            // draw call instead of an off screen pass. design-v3.md 16.7 removes it in
            // calm mode, which is this branch.
            val glow = TIP_GLOW_RADIUS.toPx()
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(tip.copy(alpha = TIP_GLOW_ALPHA), tip.copy(alpha = 0f)),
                    center = headPoint,
                    radius = glow,
                ),
                radius = glow,
                center = headPoint,
            )
        }
        drawCircle(color = tip, radius = TIP_DIAMETER.toPx() / 2f, center = headPoint)
    }
}

/**
 * design-v3.md 8.2 item 9 and section 11. The ring is replaced by a circle bloom and a
 * check.
 *
 * The ring collapses inward, a soft circle expands from the center over 700ms fading as
 * it grows, and the check scales in from 0.6. **In calm mode and under reduce motion
 * the check simply appears**, per 16.6 item 9: no collapse, no expanding circle, one
 * 150ms fade. It is not a celebration in either case, and design-v3.md 14 forbids one.
 */
@Composable
internal fun FocusBloom(modifier: Modifier = Modifier) {
    val contemplative = LocalContemplativeColors.current
    val calm = LocalCalmMode.current
    val motion = clarityMotion()
    val tip = FocusPalette.ringTip.calmed(calm)

    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        if (motion.reduced) {
            progress.animateTo(1f, tween(150))
        } else {
            progress.animateTo(1f, motion.springGentle())
        }
    }

    Box(modifier = modifier.size(RING_DIAMETER), contentAlignment = Alignment.Center) {
        if (!motion.reduced) {
            Canvas(Modifier.fillMaxSize()) {
                val p = progress.value
                val stroke = RING_STROKE.toPx()
                val radius = (size.minDimension - stroke) / 2f
                val center = Offset(size.width / 2f, size.height / 2f)

                val collapse = (p / COLLAPSE_FRACTION).coerceIn(0f, 1f)
                if (collapse < 1f) {
                    drawCircle(
                        color = FocusPalette.ringProgress.calmed(calm),
                        radius = radius * (1f - COLLAPSE_DEPTH * collapse),
                        center = center,
                        style = Stroke(stroke),
                        alpha = 1f - collapse,
                    )
                }

                val bloom = radius * (BLOOM_FROM + (1f - BLOOM_FROM) * p)
                drawCircle(
                    color = tip,
                    radius = bloom,
                    center = center,
                    alpha = BLOOM_ALPHA * (1f - p),
                )
            }
        }

        ClarityIcon(
            icon = ClarityIcons.check,
            // The line beside it already reads `Session complete`, so a description
            // here would have a screen reader say it twice.
            contentDescription = null,
            tint = contemplative.textBright,
            modifier = Modifier
                .size(CHECK_SIZE)
                .graphicsLayer {
                    val p = progress.value
                    val entry = if (motion.reduced) {
                        p
                    } else {
                        ((p - CHECK_DELAY) / (1f - CHECK_DELAY)).coerceIn(0f, 1f)
                    }
                    alpha = entry
                    val scaled = CHECK_FROM + (1f - CHECK_FROM) * entry
                    scaleX = if (motion.reduced) 1f else scaled
                    scaleY = if (motion.reduced) 1f else scaled
                },
        )
    }
}

/**
 * Where a point sits on the ring, given how much of the session is left.
 *
 * Zero degrees is the trailing edge in canvas coordinates, so the top is -90, and the
 * head of the remaining arc has traveled the elapsed fraction clockwise from there.
 */
private fun angleFor(fractionRemaining: Float): Float = -90f + 360f * (1f - fractionRemaining)

private fun pointOn(center: Offset, radius: Float, degrees: Float): Offset {
    val radians = degrees * DEGREES_TO_RADIANS
    return Offset(
        x = center.x + radius * cos(radians),
        y = center.y + radius * sin(radians),
    )
}

/**
 * The five minute tick, sitting across the track from the moment the session starts.
 *
 * design-v3.md 10.18 rejects the obvious answer, turning the ring amber or red at five
 * minutes, three times over: warnAmber is scoped to the Pulse dot, section 14 forbids
 * red for normal behavior, and an unannounced color change is the surprise this switch
 * exists to prevent. So the mark is white and only its brightness changes.
 */
private fun DrawScope.drawTransitionMark(
    center: Offset,
    radius: Float,
    angle: Float,
    alpha: Float,
) {
    val half = MARK_LENGTH.toPx() / 2f
    drawLine(
        color = Color.White.copy(alpha = alpha),
        start = pointOn(center, radius - half, angle),
        end = pointOn(center, radius + half, angle),
        strokeWidth = MARK_WIDTH.toPx(),
    )
}

/** `MM:SS`, with the minutes padded so the numeral keeps one width inside the ring. */
private fun formatCountdown(totalSeconds: Int): String {
    val safe = totalSeconds.coerceAtLeast(0)
    val minutes = safe / SECONDS_PER_MINUTE
    val seconds = safe % SECONDS_PER_MINUTE
    return minutes.toString().padStart(2, '0') + ":" + seconds.toString().padStart(2, '0')
}

/** Whole minutes, rounded up, for the spoken figure. Fifty nine seconds is a minute. */
private fun spokenMinutes(totalSeconds: Int): Int =
    (totalSeconds + SECONDS_PER_MINUTE - 1) / SECONDS_PER_MINUTE

private const val SECONDS_PER_MINUTE = 60

/** design-v3.md 5.3 and 13. */
private const val NUMERAL_MAX_FONT_SCALE = 1.3f

/**
 * The ring's weight, and an open choice recorded rather than defaulted.
 *
 * design-v3.md fixes the diameter at 240dp and says nothing about the stroke. The
 * statistically common answer is a heavy ring, twelve to sixteen dp, which is what an
 * activity ring looks like and what section 15.1 warns about under "a ring closing
 * toward a daily target". This one is deliberately thin, with the weight spent on the
 * tip instead: a fine line of light with a bright point traveling it reads as time
 * passing rather than as a target filling, and it is the treatment that belongs in a
 * room lit by a radial glow and eight specks. Section 15.
 */
private val RING_STROKE = 6.dp
private val TIP_DIAMETER = 10.dp
private val TIP_GLOW_RADIUS = 15.dp
private const val TIP_GLOW_ALPHA = 0.38f

private val MARK_LENGTH = 12.dp
private val MARK_WIDTH = 2.dp
private const val MARK_ALPHA_FAINT = 0.20f
private const val MARK_ALPHA_BRIGHT = 0.55f

/** How much of the bloom's timeline the ring's collapse takes, and how far it falls. */
private const val COLLAPSE_FRACTION = 0.35f
private const val COLLAPSE_DEPTH = 0.45f
private const val BLOOM_FROM = 0.18f
private const val BLOOM_ALPHA = 0.22f
private const val CHECK_DELAY = 0.45f
private const val CHECK_FROM = 0.6f
private val CHECK_SIZE = 56.dp

private const val DEGREES_TO_RADIANS = 0.017453292f
