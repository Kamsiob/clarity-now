package com.kamsiob.claritynow.ui.areas

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kamsiob.claritynow.ui.theme.ClaritySpacing
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import com.kamsiob.claritynow.R
import com.kamsiob.claritynow.ui.components.ClarityIcon
import com.kamsiob.claritynow.ui.components.ClarityIcons
import com.kamsiob.claritynow.ui.components.areaWash
import com.kamsiob.claritynow.ui.components.opticalGlyphNudge
import com.kamsiob.claritynow.ui.theme.ClarityHapticEvent
import com.kamsiob.claritynow.ui.theme.LocalClarityColors
import com.kamsiob.claritynow.ui.theme.LocalClarityHaptics
import com.kamsiob.claritynow.ui.theme.LocalClarityTypography
import com.kamsiob.claritynow.ui.theme.areaLabelColor
import com.kamsiob.claritynow.ui.theme.clarityMotion
import com.kamsiob.claritynow.ui.theme.parseAreaColor

/** The brightness the wash reaches at the peak of a promotion, design-v3.md 8.2 item 1. */
private const val PROMOTION_WASH_PEAK = 0.11f

/**
 * design-v3.md 10.3, the A2 slim card.
 *
 * No border, no colored stripe, no edge treatment. The card carries a shadow in the
 * light world and a lightness step in the dark world, and the area accent appears
 * only as a 7dp dot, a low opacity wash and the label text.
 */
@Composable
fun AreaCardContent(
    area: AreaCardModel,
    promotion: PromotionCue?,
    onPromotionPlayed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalClarityColors.current
    val type = LocalClarityTypography.current
    val motion = clarityMotion()
    val accent = parseAreaColor(area.colorHex)
    val inSession = area.focusMinutesRemaining != null

    val baseWash = when {
        area.isIdle -> 0f
        inSession -> colors.cardWashActiveAlpha
        else -> colors.cardWashAlpha
    }
    val wash = remember { Animatable(baseWash) }
    LaunchedEffect(baseWash) { if (promotion == null) wash.snapTo(baseWash) }

    // The hero. Old and new titles are never both at full opacity.
    val outgoing = remember { Animatable(0f) }
    val incoming = remember { Animatable(0f) }
    var playing by remember { mutableStateOf<PromotionCue?>(null) }
    val played by rememberUpdatedState(onPromotionPlayed)

    LaunchedEffect(promotion?.id) {
        val cue = promotion ?: return@LaunchedEffect
        playing = cue
        outgoing.snapTo(0f)
        incoming.snapTo(0f)
        if (motion.reduced) {
            // A crossfade, not a snap. design-v3.md 8.3 turns an animation into a
            // crossfade under reduced motion and calm mode; it does not delete it.
            // Snapping made the struck through title vanish on the same frame the new
            // one appeared, so the card never said which item had just been completed,
            // which is the one thing this cue exists to say. The translation offsets
            // are dropped, because those are the movement; the fade is not.
            coroutineScope {
                launch { outgoing.animateTo(1f, tween(motion.promotionMillis)) }
                launch { incoming.animateTo(1f, tween(motion.promotionMillis)) }
            }
        } else {
            coroutineScope {
                launch { wash.animateTo(PROMOTION_WASH_PEAK, tween(250)); wash.animateTo(baseWash, tween(250)) }
                launch { outgoing.animateTo(1f, tween(motion.promotionMillis)) }
                launch { incoming.animateTo(1f, motion.springStandard()) }
            }
        }
        playing = null
        played()
    }

    Column(
        modifier = modifier
            .areaWash(accent, wash.value, area.id)
            .padding(horizontal = 18.dp, vertical = ClaritySpacing.scaled(17.dp)),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(if (area.isIdle) accent.copy(alpha = 0.45f) else accent),
            )
            Text(
                text = area.name,
                style = type.label,
                color = areaLabelColor(accent, colors),
                modifier = Modifier.padding(start = 8.dp),
            )
        }

        Box(modifier = Modifier.fillMaxWidth().padding(top = ClaritySpacing.scaled(7.dp))) {
            val cue = playing
            if (cue != null) {
                Text(
                    text = cue.previousTitle,
                    style = type.itemTitle.copy(textDecoration = TextDecoration.LineThrough),
                    color = colors.inkPrimary,
                    modifier = Modifier
                        .alpha(1f - outgoing.value)
                        .padding(top = if (motion.reduced) 0.dp else (8 * outgoing.value).dp),
                )
            }
            if (area.isIdle) {
                Text(
                    text = stringResource(R.string.area_idle_title),
                    // 500 rather than itemTitle's 650, design-v3.md 10.3. The
                    // invitation is the same string at the same size as a real item
                    // title, one step lighter, so an empty card reads as a place
                    // waiting to be filled rather than as a card that is already
                    // full. This line rendered at 650 through phases 2 and 3b: the
                    // sans family pinned a single weight instance, so the copy had
                    // nothing to resolve against and was dropped in silence. See
                    // ClarityType.kt. The tracking stays at the role's own value,
                    // because the size and the job have not changed.
                    style = type.itemTitle.copy(fontWeight = FontWeight(500)),
                    // inkSecondary, and 10.3 says inkTertiary. **That is a
                    // contradiction inside design-v3.md and section 13 wins**, because
                    // a floor is a floor: `inkTertiary` measures 2.40 to one on the
                    // card and 3.22 in dark, against 13's 4.5, and 10.3 calls this
                    // same string the most important one on the screen. Resolved in
                    // 10.3 rather than patched here. `inkSecondary` is the one step
                    // down from `inkPrimary` that clears, at 5.29 to one in light and
                    // 6.36 in dark on the phase 3c card, and the weight above already
                    // carries the "one step lighter" the idle state is for.
                    color = colors.inkSecondary,
                )
            } else {
                Text(
                    text = area.activeItemTitle.orEmpty(),
                    style = type.itemTitle,
                    color = colors.inkPrimary,
                    modifier = if (cue == null) {
                        Modifier
                    } else {
                        Modifier
                            .alpha(incoming.value)
                            .padding(
                                top = if (motion.reduced) {
                                    0.dp
                                } else {
                                    (16 * (1f - incoming.value)).dp
                                },
                            )
                    },
                )
            }
        }

        FirstStepLine(area = area)
        StatusLine(area = area, accent = accent)
    }
}

/**
 * Row three, design-v3.md 10.3 and 10.17. The active item's first step, Addendum
 * 01 4b.
 *
 * **Absent entirely when there is none.** No placeholder, no dash, no reserved row
 * that changes the card's height, and above all no invitation to add one: the whole
 * value of this field is that it is free, and a card that asks for it turns a help
 * into a chore. design-v3.md 10.3 says it in one line, `a card is not a form`.
 *
 * `caption` rather than `body`, which is the rule worth stating rather than leaving
 * to a style constant. At body weight it competes with the title. It is read second,
 * at the moment the title has already failed to start someone, and the hierarchy on
 * the card has to say so.
 *
 * One line, ellipsized, because design-v3.md 10.3 caps the card at four lines and
 * makes this the row that truncates first when a status line is also present. The
 * full text is in the area detail sheet, which is where reading happens.
 *
 * An idle area draws nothing here even if some queued item has a first step. The
 * card is about the one thing that is happening, and there is not one.
 */
@Composable
private fun FirstStepLine(area: AreaCardModel) {
    val colors = LocalClarityColors.current
    val type = LocalClarityTypography.current
    val firstStep = area.activeItemFirstStep?.takeIf { !area.isIdle } ?: return

    Text(
        text = firstStep,
        style = type.caption,
        color = colors.inkSecondary,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        // Tighter than the 6dp above the status line, because this line belongs to
        // the title above it and the status line belongs to the card as a whole.
        modifier = Modifier.padding(top = ClaritySpacing.scaled(5.dp)),
    )
}

/**
 * Row four, shown only when it carries information. design-v3.md 10.3.
 *
 * It was row three until the first step took that place. Both are single lines and
 * both are conditional, so the card still never exceeds the four lines 10.3 caps it
 * at, and neither one moves the other when it is absent.
 */
@Composable
private fun StatusLine(area: AreaCardModel, accent: Color) {
    val colors = LocalClarityColors.current
    val type = LocalClarityTypography.current

    val minutes = area.focusMinutesRemaining
    when {
        minutes != null -> Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier.padding(top = ClaritySpacing.scaled(6.dp)),
        ) {
            ClarityIcon(
                icon = ClarityIcons.focus,
                contentDescription = null,
                tint = accent,
                // The play triangle carries its mass left of its bounding box, so
                // centering it by layout leaves it looking off center.
                modifier = Modifier.size(13.dp).opticalGlyphNudge(13.dp),
            )
            Text(
                text = pluralStringResource(R.plurals.area_in_focus_minutes, minutes, minutes),
                // Semibold, design-v3.md 10.3. This is the one status line that
                // reports something happening right now, and weight is the only
                // device available to say so: the card has no room for a second
                // size and 3.4 forbids giving it a filled block. Inert until phase
                // 3c for the same reason as the idle title above.
                style = type.caption.copy(fontWeight = FontWeight(600)),
                color = accent,
                modifier = Modifier.padding(start = 5.dp),
            )
        }

        area.isIdle -> Text(
            text = when {
                area.daysSinceLastEvent <= 0 -> stringResource(R.string.area_last_active_today)
                else -> pluralStringResource(
                    R.plurals.area_last_active_days,
                    area.daysSinceLastEvent,
                    area.daysSinceLastEvent,
                )
            },
            style = type.caption,
            // The same correction as the idle title above, and a worse failure before
            // it: this line is 12sp rather than 21, and it was the smallest text in
            // the app sitting at 2.40 to one. design-v3.md 10.3 names no color for
            // this row, so there was no contradiction to resolve, only section 13 to
            // obey. It reads quieter than the title anyway, by 9sp of size.
            color = colors.inkSecondary,
            modifier = Modifier.padding(top = ClaritySpacing.scaled(6.dp)),
        )

        // An ordinary active area shows nothing, because there is nothing to add.
        else -> Unit
    }
}

/**
 * Reads the whole card as one thing, in the order a person would say it.
 *
 * **The first step follows the title and never precedes it**, Addendum 01 4b. A
 * screen reader user gets the same hierarchy the sighted card has: the thing itself,
 * then the way in. Reversing them would announce a fragment of a task before naming
 * the task, which is disorienting in exactly the way the card's type scale avoids.
 *
 * **[focusStatus] is the status line and it is read out**, because the in session
 * state is carried visually by an intensified wash and a colored line, design-v3.md
 * 10.3, and section 13 does not let color be the only signal. It is the same sentence
 * the card draws, taken from `strings.xml` by the caller, so there is no second wording
 * of it here to drift or to editorialize: a session is `In focus, 7 minutes left` and
 * nothing about it is ever a judgment.
 */
fun areaCardDescription(
    area: AreaCardModel,
    idleTitle: String,
    focusStatus: String? = null,
): String = buildString {
    append(area.name)
    append(". ")
    append(area.activeItemTitle ?: idleTitle)
    if (!area.isIdle) {
        area.activeItemFirstStep?.let {
            append(". ")
            append(it)
        }
        focusStatus?.let {
            append(". ")
            append(it)
        }
    }
    if (area.queueLength > 0) {
        append(". ")
        append(area.queueLength)
        append(if (area.queueLength == 1) " item waiting" else " items waiting")
    }
}

/** Kept next to the card so the semantics and the visuals cannot drift apart. */
@Composable
fun AreaCardSemantics(area: AreaCardModel, modifier: Modifier = Modifier): Modifier {
    val idleTitle = stringResource(R.string.area_idle_title)
    val minutes = area.focusMinutesRemaining
    val focusStatus = if (minutes == null) {
        null
    } else {
        pluralStringResource(R.plurals.area_in_focus_minutes, minutes, minutes)
    }
    return modifier.clearAndSetSemantics {
        contentDescription = areaCardDescription(area, idleTitle, focusStatus)
    }
}

/** The haptic that fires as a newly promoted title lands. */
@Composable
fun rememberPromotionHaptic(): () -> Unit {
    val haptics = LocalClarityHaptics.current
    return remember(haptics) { { haptics.perform(ClarityHapticEvent.PROMOTE) } }
}
