package com.kamsiob.claritynow.ui.onboarding

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kamsiob.claritynow.R
import com.kamsiob.claritynow.ui.theme.ClaritySpacing
import com.kamsiob.claritynow.ui.theme.LocalClarityTypography
import com.kamsiob.claritynow.ui.theme.LocalContemplativeColors
import com.kamsiob.claritynow.ui.theme.clarityMotion
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * The first three seconds of the app, and the only three it will ever have.
 *
 * ## Why there is one at all
 *
 * There was no opening. The launch window is a flat `launch_canvas`, which is `#F1F1F6`
 * in light, and onboarding's first beat is Contemplative on `deepBlack`. **So the first
 * frame a person on a light phone ever saw was a near white rectangle that slammed to
 * near black**, and then four demo cards appeared with no title, no name and nothing
 * saying what they were looking at. The owner's word for it was that it landed flat. It
 * is a defect before it is an absence.
 *
 * ## Why a brand moment is earned here, when it usually is not
 *
 * The literature is against them. Android's own splash guidance recommends against a
 * branding image, caps an animated icon at 1000ms, and warns that showing an incomplete
 * interface is jarring. Nielsen's one second limit is the boundary for uninterrupted
 * thought. Every one of those says the same thing: **held time is a tax unless it buys
 * the person something.** The test that follows is whether removing it would cost the
 * reader information or only cost the brand a bow.
 *
 * This one passes, and for a reason no other app's does. `design-v3.md` 4.1: the mark is
 * "a queue seen face on: one solid card in front, two narrower cards peeking out behind
 * it at decreasing opacity. It is the product's core idea, one thing at the front and the
 * rest waiting, expressed as a shape."
 *
 * **The logo is the mechanic.** Drawing it card by card is not a title card, it is the
 * first sentence of the lesson, and it shows the exact thing beat 1 was failing to show:
 * that there is something behind the front one. A person taps an icon of three cards,
 * watches three cards assemble, and then meets four cards that behave that way.
 *
 * ## The room dims rather than cutting
 *
 * The ground begins at `launch_canvas`, which is the color the window already is, and
 * crosses to `deepBlack` over the first 350ms. Nothing flashes because nothing changes
 * abruptly: `design-v3.md` 2 already describes entering a Contemplative surface as the
 * room dimming, and this is that sentence made literal at the one moment the app crosses
 * from the system's world into its own.
 *
 * ## It advances itself, and it is the only thing in the sequence that does
 *
 * Every beat waits for a tap, because "a page that leaves while somebody is on it is the
 * worst behavior in the sequence". **This is not a page.** It carries the app's name and
 * one line about how long the next minute is, and there is nothing on it to read twice.
 * A title card that waited for a tap would be asking permission to say hello. A tap
 * anywhere still skips the rest of it, so nobody is ever held.
 *
 * Under reduce motion or calm mode the whole thing is one crossfade to the settled mark
 * and a shorter hold, per 8.3's single global check.
 */
@Composable
internal fun OnboardingOpening(onDone: () -> Unit, modifier: Modifier = Modifier) {
    val contemplative = LocalContemplativeColors.current
    val type = LocalClarityTypography.current
    val motion = clarityMotion()

    val dim = remember { Animatable(0f) }
    val front = remember { Animatable(0f) }
    val middle = remember { Animatable(0f) }
    val back = remember { Animatable(0f) }
    val words = remember { Animatable(0f) }
    val leaving = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        if (motion.reduced) {
            // 8.3: one crossfade, everything already settled behind it.
            listOf(dim, front, middle, back, words).forEach { it.snapTo(1f) }
            delay(REDUCED_HOLD)
        } else {
            launch { dim.animateTo(1f, motion.easeSlow()) }
            delay(FRONT_AT)
            launch { front.animateTo(1f, motion.easeOut()) }
            delay(MIDDLE_AT - FRONT_AT)
            launch { middle.animateTo(1f, motion.easeOut()) }
            delay(BACK_AT - MIDDLE_AT)
            launch { back.animateTo(1f, motion.easeOut()) }
            delay(WORDS_AT - BACK_AT)
            launch { words.animateTo(1f, motion.easeOut()) }
            delay(HOLD)
            leaving.animateTo(0f, motion.easeOut())
        }
        onDone()
    }

    val ground = androidx.compose.ui.graphics.lerp(
        LAUNCH_CANVAS,
        contemplative.deepBlack,
        dim.value,
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .drawBehind { drawRect(ground) }
            .graphicsLayer { alpha = leaving.value },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.onboardingLift(),
        ) {
            Box(
                modifier = Modifier
                    .size(MARK_SIZE)
                    .drawBehind {
                        // The three cards of `ic_mark.xml`, drawn one at a time so each
                        // can arrive on its own. The geometry is the drawable's, in its
                        // own 100 unit space, so the mark that assembles here is the mark
                        // on the launcher rather than a picture of it.
                        drawMarkCard(BACK_CARD, back.value * BACK_CARD.alpha, contemplative.textBright)
                        drawMarkCard(MIDDLE_CARD, middle.value * MIDDLE_CARD.alpha, contemplative.textBright)
                        drawMarkCard(FRONT_CARD, front.value * FRONT_CARD.alpha, contemplative.textBright)
                    },
            )
            Spacer(Modifier.height(ClaritySpacing.scaled(26.dp)))
            Text(
                text = stringResource(R.string.areas_title),
                style = type.displayTitle,
                color = contemplative.textBright,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(words.value),
            )
            Spacer(Modifier.height(ClaritySpacing.scaled(12.dp)))
            Text(
                // The one piece of guidance the opening carries, and it is about the
                // reader's time rather than about the app. Somebody deciding whether to
                // start a setup they cannot see the end of is the case the anxiety
                // guidance names: never leave a person unsure of the timeframe.
                text = stringResource(R.string.onboarding_opening_duration),
                style = type.body,
                color = contemplative.textDim,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .alpha(words.value)
                    .padding(horizontal = ClaritySpacing.screenPadding),
            )
        }
    }
}

/** One card of the mark, in the drawable's own 100 unit space. */
private data class MarkCard(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val radius: Float,
    val alpha: Float,
)

private val BACK_CARD = MarkCard(35.5f, 14f, 29f, 11f, 5.5f, 0.26f)
private val MIDDLE_CARD = MarkCard(28f, 29f, 44f, 12f, 6f, 0.50f)
private val FRONT_CARD = MarkCard(14f, 46f, 72f, 40f, 11f, 1f)

private fun DrawScope.drawMarkCard(card: MarkCard, alpha: Float, ink: Color) {
    if (alpha <= 0f) return
    val unit = size.minDimension / 100f
    drawRoundRect(
        color = ink,
        topLeft = Offset(card.x * unit, card.y * unit),
        size = Size(card.width * unit, card.height * unit),
        cornerRadius = CornerRadius(card.radius * unit, card.radius * unit),
        alpha = alpha.coerceIn(0f, 1f),
    )
}

/** `launch_canvas` in light, which is the color the window already is at frame one. */
private val LAUNCH_CANVAS = Color(0xFFF1F1F6)

private val MARK_SIZE = 108.dp

private const val FRONT_AT = 220L
private const val MIDDLE_AT = 360L
private const val BACK_AT = 480L
private const val WORDS_AT = 640L
private const val HOLD = 1150L
private const val REDUCED_HOLD = 900L
