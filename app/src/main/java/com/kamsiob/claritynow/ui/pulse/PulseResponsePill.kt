package com.kamsiob.claritynow.ui.pulse

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kamsiob.claritynow.domain.engine.catalog.ResponseOption
import com.kamsiob.claritynow.ui.components.clarityClickable
import com.kamsiob.claritynow.ui.components.clarityFocusRing
import com.kamsiob.claritynow.ui.components.clarityPressScale
import com.kamsiob.claritynow.ui.theme.ClarityHapticEvent
import com.kamsiob.claritynow.ui.theme.ClaritySpacing
import com.kamsiob.claritynow.ui.theme.LocalCalmMode
import com.kamsiob.claritynow.ui.theme.LocalClarityTypography
import com.kamsiob.claritynow.ui.theme.LocalContemplativeColors
import com.kamsiob.claritynow.ui.theme.PulsePalette
import com.kamsiob.claritynow.ui.theme.calmed
import com.kamsiob.claritynow.ui.theme.clarityMotion
import kotlin.math.hypot
import kotlin.math.max

/**
 * One tappable answer. design-v3.md 10.7 Contemplative primary, and 8.2 item 10.
 *
 * **Both options carry exactly the same treatment**, which is a rule and not a
 * convenience. `CLARITY_LOGIC_ENGINE.md` 6.1: both responses must feel equally valid read
 * out of context, and no option is the good answer. A primary and a secondary, a filled
 * and an outlined, or a wider and a narrower pill would each be the interface answering
 * the question on the person's behalf. So there is one composable, it is called once per
 * option, and it takes no parameter that could make one of them louder.
 *
 * ## The answer animation, 8.2 item 10
 *
 * Amber fills from the tap point over 220ms; the unselected pill fades to 30 percent and
 * drops 4dp. The fill is drawn as an expanding circle centered on the point the finger
 * went down, clipped to the pill, so the color arrives from under the thumb rather than
 * from an edge.
 *
 * **The fill goes to full strength and the label crosses to `deepBlack` with it**, rather
 * than settling at a tint under a bright label. design-v3.md 15 asks for the choice to be
 * made rather than defaulted, and the tint is the default: it is what a selected chip
 * looks like everywhere. Two things argue for the fill. Section 11 gives this surface one
 * vocabulary, in which **filled amber means answered**, and the rhythm row three
 * centimetres below is about to say exactly that about today; a pill that stopped at 30
 * percent would be saying something quieter than the mark it produces. And a full fill
 * under a bright label cannot be read at all: `#F3F1EC` on `#E8A15C` measures 1.6 to one,
 * so the label has to move, and `deepBlack` on the fill measures 8.1 to one, comfortably
 * clear of design-v3.md 13's floor in both ordinary and calm mode.
 *
 * **Under reduce motion or calm mode the fill is a crossfade**, per 16.6 item 10: the
 * circle is already at full radius and its opacity is what animates, so nothing travels
 * and the pill still ends up filled. The unselected pill fades and does not drop, because
 * a crossfade has no travel.
 *
 * **An activation with no position starts from the center**: a keyboard, a switch, or
 * TalkBack's double tap all reach the click handler without a finger having landed
 * anywhere. The animation is identical, it simply begins in the middle, so nobody gets a
 * different feedback for using a different input.
 */
@Composable
internal fun PulseResponsePill(
    option: ResponseOption,
    selected: Boolean,
    dimmed: Boolean,
    enabled: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contemplative = LocalContemplativeColors.current
    val type = LocalClarityTypography.current
    val motion = clarityMotion()
    val accent = PulsePalette.accent.calmed(LocalCalmMode.current)
    val interaction = remember { MutableInteractionSource() }

    // Where the finger went down, in the pill's own coordinates. Recorded in the initial
    // pass and never consumed, so the clickable underneath still sees the whole gesture.
    //
    // Held as a `MutableState` read only inside the draw lambda rather than as a `by`
    // delegate read in the body, so a press invalidates the drawing and not the
    // composition. Every press in the app would otherwise recompose a pill to store a
    // point that nothing composed depends on.
    val tapPoint = remember { mutableStateOf(Offset.Unspecified) }

    val fill = remember { Animatable(0f) }
    LaunchedEffect(selected) {
        if (selected) {
            fill.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = if (motion.reduced) REDUCED_MILLIS else FILL_MILLIS,
                    easing = EaseOutCubic,
                ),
            )
        }
    }

    // design-v3.md 8.2 item 10. Thirty percent, and four dp down where motion allows it.
    val faded by animateFloatAsState(
        targetValue = if (dimmed) UNSELECTED_ALPHA else 1f,
        animationSpec = motion.effects(),
        label = "pulsePillFade",
    )
    val dropped by animateDpAsState(
        targetValue = if (dimmed && !motion.reduced) UNSELECTED_DROP else 0.dp,
        animationSpec = motion.effects(),
        label = "pulsePillDrop",
    )

    Box(
        modifier = modifier
            .widthIn(max = PILL_MAX_WIDTH)
            .fillMaxWidth()
            .graphicsLayer {
                alpha = faded
                translationY = dropped.toPx()
            }
            .clarityPressScale(interaction, enabled = enabled, label = "pulsePill")
            .heightIn(min = PILL_MIN_HEIGHT)
            .clip(CircleShape)
            .background(accent.copy(alpha = REST_ALPHA))
            .drawBehind {
                val progress = fill.value
                if (progress <= 0f) return@drawBehind
                val down = tapPoint.value
                val origin = if (down.isSpecified) down else center
                val radius = farthestCorner(origin, size)
                // Full motion grows the circle. Reduced motion has it already grown and
                // fades it in, which is design-v3.md 8.3's one path.
                if (motion.reduced) {
                    drawCircle(color = accent, radius = radius, center = origin, alpha = progress)
                } else {
                    drawCircle(color = accent, radius = radius * progress, center = origin)
                }
            }
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        event.changes
                            .firstOrNull { it.pressed && !it.previousPressed }
                            ?.let { tapPoint.value = it.position }
                    }
                }
            }
            .clarityFocusRing(interaction, CircleShape)
            .clarityClickable(
                enabled = enabled,
                interactionSource = interaction,
                haptic = ClarityHapticEvent.SELECT,
                role = Role.Button,
                onClickLabel = option.label,
                onClick = onSelect,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = option.label,
            style = type.bodyStrong,
            // The label rides the fill rather than switching at its end, so there is no
            // frame in which bright type sits on full amber.
            color = lerp(contemplative.textBright, contemplative.deepBlack, fill.value),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(
                horizontal = LABEL_PADDING,
                vertical = ClaritySpacing.scaled(LABEL_PADDING_VERTICAL,
            )),
        )
    }
}

/**
 * The distance from [from] to the farthest corner of [size].
 *
 * What the fill has to reach for the pill to be entirely covered, whichever end of it the
 * finger landed on.
 */
private fun farthestCorner(from: Offset, size: Size): Float =
    hypot(max(from.x, size.width - from.x), max(from.y, size.height - from.y))

/** design-v3.md 10.7. The Contemplative primary's resting fill. */
private const val REST_ALPHA = 0.14f

/** design-v3.md 8.2 item 10. */
private const val UNSELECTED_ALPHA = 0.30f
private val UNSELECTED_DROP = 4.dp
private const val FILL_MILLIS = 220

/** design-v3.md 8.3. The one crossfade every animation becomes. */
private const val REDUCED_MILLIS = 150

/**
 * The pill's floor rather than its height, so a long label at a large font scale grows
 * the pill instead of being clipped by it. design-v3.md 13 asks for 200 percent without
 * clipping, and the corpus authors response labels as short phrases rather than words.
 */
private val PILL_MIN_HEIGHT = 50.dp

/**
 * A measure rather than the full width.
 *
 * Two or three pills stacked edge to edge across a phone would each be a 320dp target for
 * a two word answer, which reads as a form rather than as a choice. This keeps them
 * comfortably wide, centered, and identical to one another.
 */
private val PILL_MAX_WIDTH = 300.dp

private val LABEL_PADDING = 24.dp
private val LABEL_PADDING_VERTICAL = 13.dp
