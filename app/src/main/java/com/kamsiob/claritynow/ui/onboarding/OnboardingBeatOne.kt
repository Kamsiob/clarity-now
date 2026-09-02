package com.kamsiob.claritynow.ui.onboarding

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kamsiob.claritynow.R
import com.kamsiob.claritynow.ui.theme.AreaPalette
import com.kamsiob.claritynow.ui.theme.ClarityDarkColors
import com.kamsiob.claritynow.ui.theme.ClaritySpacing
import com.kamsiob.claritynow.ui.theme.LocalCalmMode
import com.kamsiob.claritynow.ui.theme.LocalClarityShapes
import com.kamsiob.claritynow.ui.theme.LocalClarityTypography
import com.kamsiob.claritynow.ui.theme.LocalContemplativeColors
import com.kamsiob.claritynow.ui.theme.areaLabelColor
import com.kamsiob.claritynow.ui.theme.calmed
import com.kamsiob.claritynow.ui.theme.clarityMotion
import com.kamsiob.claritynow.ui.theme.parseAreaColor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Beat 1, See It Work. MASTER_BUILD_PROMPT 13.1.
 *
 * About nine seconds, auto advancing. Four colored demo cards arrive, the top card's item
 * strikes through and completes, the next queued title slides up and takes its place, and
 * one sentence names what just happened. **The beat has to land the whole model in five
 * seconds**, which is the only number in it that is not negotiable, so the timeline below
 * has the promotion finishing at 3.7 seconds and spends the rest on reading.
 *
 * ## The demo areas are the real first four
 *
 * Their colors are `AreaPalette.defaultColorForIndex(0..3)`, which is exactly what the
 * mood walk in design-v3.md 3.4 hands the first four areas a person creates. A set of
 * colors invented for a demo would be a promise the app then breaks on the next screen.
 *
 * ## Under calm mode and reduce motion
 *
 * design-v3.md 16.2: entrances do not fire at all in calm mode, so the four cards render
 * already settled. The promotion is not an entrance and still runs, as 16.6 item 1's
 * crossfade with no travel and no wash brightening, **with the completed title still
 * struck through as it fades**, because the card has to keep saying which item completed
 * and that sentence is the whole content of this beat. The nine seconds are unchanged: a
 * hold is not motion.
 */
@Composable
internal fun OnboardingBeatOne(onAdvance: () -> Unit, modifier: Modifier = Modifier) {
    val contemplative = LocalContemplativeColors.current
    val type = LocalClarityTypography.current
    val motion = clarityMotion()

    var sentenceShown by remember { mutableStateOf(false) }
    var promoted by remember { mutableStateOf(false) }

    val cards = demoCards()
    var hintShown by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(SENTENCE_AT)
        sentenceShown = true
        delay(PROMOTION_AT - SENTENCE_AT)
        promoted = true
        // **No auto advance.** The argument written into `OnboardingBeatFour` applies here
        // word for word: a page that leaves while somebody is on it is the worst behavior
        // in the sequence, and it is pure loss, because a tap does the same job better.
        // Beat 1 used to take the screen away at a fixed nine seconds whether the demo had
        // been understood or not. A tap or a swipe anywhere still advances, which
        // `OnboardingRoute` provides for every beat.
        hintShown = true
    }

    // The hint the tutorial already shows five times, on the two beats that now wait
    // rather than advancing themselves. It arrives after the demo has finished playing,
    // so it never competes with the thing it is waiting for.
    val hintAlpha = remember { Animatable(0f) }
    LaunchedEffect(hintShown) {
        if (hintShown) {
            delay(HINT_AFTER)
            hintAlpha.animateTo(1f, motion.easeOut())
        }
    }

    val sentenceAlpha = remember { Animatable(0f) }
    LaunchedEffect(sentenceShown) {
        if (sentenceShown) sentenceAlpha.animateTo(1f, motion.easeOut())
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(
                start = ClaritySpacing.screenPadding,
                end = ClaritySpacing.screenPadding,
                bottom = OnboardingOpticalLiftPadding,
            ),
        verticalArrangement = Arrangement.Center,
    ) {
        cards.forEachIndexed { index, card ->
            DemoCard(
                card = card,
                index = index,
                promoted = promoted && index == 0,
            )
            if (index != cards.lastIndex) Spacer(Modifier.height(ClaritySpacing.cardGap))
        }

        Spacer(Modifier.height(ClaritySpacing.scaled(38.dp)))

        Text(
            text = stringResource(R.string.onboarding_beat_one_line),
            style = type.readSerif,
            color = contemplative.textBright,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .graphicsLayer { alpha = sentenceAlpha.value },
        )

        Spacer(Modifier.height(ClaritySpacing.rest))

        Text(
            text = stringResource(R.string.tutorial_advance),
            style = type.caption,
            color = contemplative.textDim,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer { alpha = hintAlpha.value },
        )
    }
}

/**
 * How long a beat waits before saying it is waiting.
 *
 * Long enough that the hint never competes with the demo it follows, short enough that
 * somebody who has finished reading is not left wondering. It is the same value on beat 4.
 */
internal const val HINT_AFTER = 2_500L

/**
 * One demo card, and the three part entrance design-v3.md's beat 1 asks for.
 *
 * The three parts are the card itself, then the area's dot and name, then the item title,
 * each one 8.2 item 4's fade and 16dp rise, one stagger step apart. The four cards are
 * staggered against each other by three of those steps, which is exactly the span of one
 * card's own three parts, so the arrivals interleave without overlapping.
 *
 * Every step is the motion theme's own stagger, which is zero when motion is reduced, so
 * 8.3's rule that everything becomes one 150ms crossfade holds here with no branch.
 */
@Composable
private fun DemoCard(card: OnboardingDemoCard, index: Int, promoted: Boolean) {
    val contemplative = LocalContemplativeColors.current
    val type = LocalClarityTypography.current
    val shapes = LocalClarityShapes.current
    val motion = clarityMotion()
    val calm = LocalCalmMode.current
    val accent = parseAreaColor(card.colorHex)

    val parts = List(PART_COUNT) { remember { Animatable(if (calm) 1f else 0f) } }
    LaunchedEffect(Unit) {
        // design-v3.md 16.2: in calm mode the entrance does not fire, so the card is
        // already settled and there is nothing to start.
        if (calm) return@LaunchedEffect
        delay(index.toLong() * PART_COUNT * motion.staggerMillis)
        parts.forEachIndexed { part, animatable ->
            launch {
                delay(part.toLong() * motion.staggerMillis)
                animatable.animateTo(1f, motion.easeOut())
            }
        }
    }

    // design-v3.md 8.2 item 1. 250ms for the swap, 500ms for the wash to brighten and
    // return. Both are the promotion, which is not an entrance, so calm mode reduces it
    // rather than removing it.
    val promotion = remember { Animatable(0f) }
    val bump = remember { Animatable(0f) }
    LaunchedEffect(promoted) {
        if (!promoted) return@LaunchedEffect
        launch { promotion.animateTo(1f, tween(motion.promotionMillis, easing = EaseOutCubic)) }
        if (motion.reduced) return@LaunchedEffect
        bump.animateTo(1f, tween(WASH_HALF_MILLIS, easing = EaseOutCubic))
        bump.animateTo(0f, tween(WASH_HALF_MILLIS, easing = EaseOutCubic))
    }

    val rise = if (motion.reduced) 0.dp else ENTRANCE_RISE
    val travel = if (motion.reduced) 0.dp else PROMOTION_TRAVEL
    val wash = WASH + (WASH_PEAK - WASH) * bump.value

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                alpha = parts[0].value
                translationY = (1f - parts[0].value) * rise.toPx()
            }
            .clip(shapes.card)
            .background(contemplative.surfaceRaised)
            .background(accent.calmed(calm).copy(alpha = wash))
            .padding(
                horizontal = ClaritySpacing.cardPaddingHorizontal,
                vertical = ClaritySpacing.scaled(15.dp,
            )),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.graphicsLayer {
                alpha = parts[1].value
                translationY = (1f - parts[1].value) * rise.toPx()
            },
        ) {
            Box(
                modifier = Modifier
                    .size(ClaritySpacing.areaDot)
                    .clip(CircleShape)
                    .background(accent),
            )
            Spacer(Modifier.width(9.dp))
            Text(
                text = card.areaName,
                style = type.label,
                color = areaLabelColor(accent, ClarityDarkColors),
            )
        }
        Spacer(Modifier.height(ClaritySpacing.scaled(6.dp)))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    alpha = parts[2].value
                    translationY = (1f - parts[2].value) * rise.toPx()
                },
        ) {
            Text(
                text = card.title,
                style = type.itemTitle,
                color = contemplative.textBright,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                // The struck through title survives the whole fade, per design-v3.md
                // 16.6 item 1: it is how the card says which one completed, and losing
                // it loses the beat.
                textDecoration = if (promotion.value > 0f) TextDecoration.LineThrough else null,
                modifier = Modifier.graphicsLayer {
                    alpha = 1f - promotion.value
                    translationY = promotion.value * travel.toPx()
                },
            )
            if (card.nextTitle != null) {
                Text(
                    text = card.nextTitle,
                    style = type.itemTitle,
                    color = contemplative.textBright,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.graphicsLayer {
                        alpha = promotion.value
                        translationY = (promotion.value - 1f) * travel.toPx()
                    },
                )
            }
        }
    }
}

/** One demo card's content. Fixed copy, MASTER_BUILD_PROMPT 11.2. */
@Immutable
private data class OnboardingDemoCard(
    val areaName: String,
    val colorHex: String,
    val title: String,
    val nextTitle: String?,
)

@Composable
private fun demoCards(): List<OnboardingDemoCard> = listOf(
    OnboardingDemoCard(
        areaName = stringResource(R.string.onboarding_demo_area_one),
        colorHex = AreaPalette.defaultColorForIndex(0),
        title = stringResource(R.string.onboarding_demo_item_one),
        nextTitle = stringResource(R.string.onboarding_demo_next_one),
    ),
    OnboardingDemoCard(
        areaName = stringResource(R.string.onboarding_demo_area_two),
        colorHex = AreaPalette.defaultColorForIndex(1),
        title = stringResource(R.string.onboarding_demo_item_two),
        nextTitle = null,
    ),
    OnboardingDemoCard(
        areaName = stringResource(R.string.onboarding_demo_area_three),
        colorHex = AreaPalette.defaultColorForIndex(2),
        title = stringResource(R.string.onboarding_demo_item_three),
        nextTitle = null,
    ),
    OnboardingDemoCard(
        areaName = stringResource(R.string.onboarding_demo_area_four),
        colorHex = AreaPalette.defaultColorForIndex(3),
        title = stringResource(R.string.onboarding_demo_item_four),
        nextTitle = null,
    ),
)

/**
 * The beat's timeline, in milliseconds from its first frame.
 *
 * The cards settle at about 1.3 seconds, the sentence arrives while they are still
 * arriving, and the promotion runs at 3.4 and is finished at 3.7. That is the whole model
 * delivered inside 13.1's five seconds. The remainder is reading time for a thirteen word
 * sentence, and it is deliberately not shortened: this is the one screen in the app whose
 * job is to be understood rather than to be quick.
 */
private const val SENTENCE_AT = 1_900L
private const val PROMOTION_AT = 3_400L

/** Card, then area, then title. design-v3.md's "staggered three-part entrances". */
private const val PART_COUNT = 3

/** design-v3.md 8.2 item 4's rise and item 1's travel. */
private val ENTRANCE_RISE = 16.dp
private val PROMOTION_TRAVEL = 8.dp
private const val WASH_HALF_MILLIS = 250

/** design-v3.md 3.2's dark wash range, and 8.2 item 1's 11 percent peak. */
private const val WASH = 0.08f
private const val WASH_PEAK = 0.11f
