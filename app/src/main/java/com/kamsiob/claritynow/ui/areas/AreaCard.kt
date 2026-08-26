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
import androidx.compose.ui.unit.dp
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
            outgoing.snapTo(1f)
            incoming.snapTo(1f)
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
            .padding(horizontal = 18.dp, vertical = 17.dp),
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

        Box(modifier = Modifier.fillMaxWidth().padding(top = 7.dp)) {
            val cue = playing
            if (cue != null) {
                Text(
                    text = cue.previousTitle,
                    style = type.itemTitle.copy(textDecoration = TextDecoration.LineThrough),
                    color = colors.inkPrimary,
                    modifier = Modifier
                        .alpha(1f - outgoing.value)
                        .padding(top = (8 * outgoing.value).dp),
                )
            }
            if (area.isIdle) {
                Text(
                    text = stringResource(R.string.area_idle_title),
                    style = type.itemTitle.copy(fontWeight = FontWeight(500)),
                    color = colors.inkTertiary,
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
                            .padding(top = (16 * (1f - incoming.value)).dp)
                    },
                )
            }
        }

        StatusLine(area = area, accent = accent)
    }
}

/** Row three, shown only when it carries information. design-v3.md 10.3. */
@Composable
private fun StatusLine(area: AreaCardModel, accent: Color) {
    val colors = LocalClarityColors.current
    val type = LocalClarityTypography.current

    val minutes = area.focusMinutesRemaining
    when {
        minutes != null -> Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier.padding(top = 6.dp),
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
            color = colors.inkTertiary,
            modifier = Modifier.padding(top = 6.dp),
        )

        // An ordinary active area shows nothing, because there is nothing to add.
        else -> Unit
    }
}

/** Reads the whole card as one thing, in the order a person would say it. */
fun areaCardDescription(area: AreaCardModel, idleTitle: String): String = buildString {
    append(area.name)
    append(". ")
    append(area.activeItemTitle ?: idleTitle)
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
    return modifier.clearAndSetSemantics {
        contentDescription = areaCardDescription(area, idleTitle)
    }
}

/** The haptic that fires as a newly promoted title lands. */
@Composable
fun rememberPromotionHaptic(): () -> Unit {
    val haptics = LocalClarityHaptics.current
    return remember(haptics) { { haptics.perform(ClarityHapticEvent.PROMOTE) } }
}
