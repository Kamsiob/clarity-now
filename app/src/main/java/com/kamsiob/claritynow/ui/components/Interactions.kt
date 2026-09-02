package com.kamsiob.claritynow.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.animation.animateColorAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kamsiob.claritynow.ui.theme.ClarityHapticEvent
import com.kamsiob.claritynow.ui.theme.LocalContemplativeColors
import com.kamsiob.claritynow.ui.theme.LocalIsContemplative
import com.kamsiob.claritynow.ui.theme.LocalClarityColors
import com.kamsiob.claritynow.ui.theme.LocalClarityHaptics
import com.kamsiob.claritynow.ui.theme.clarityMotion

/**
 * Every tappable thing in the app goes through here.
 *
 * Indication is null on purpose: feedback is the scale press in design-v3.md 8.2
 * item 2, and a Material ripple underneath it would be a second, borrowed
 * treatment on the same gesture. The haptic fires once per action, never on the
 * way in and again on the way out.
 */
/**
 * The press scale from design-v3.md 8.2 item 2: 0.97 on `springStandard`.
 *
 * A modifier rather than a copied block, because it was written twice inside
 * `Buttons.kt` and then not written at all on the area card, which is the largest
 * tap target in the app. `clarityClickable` passes `indication = null` on purpose,
 * so nothing supplies feedback unless a caller asks for it, and the card was asking
 * for nothing while every button around it scaled.
 *
 * Never an overshoot. design-v3.md 15.1 lists "a bounce on every hover or press,
 * rather than overshoot reserved for weight" as a tell, and 8.1 assigns this the
 * standard spring for the same reason.
 */
@Composable
fun Modifier.clarityPressScale(
    interaction: InteractionSource,
    enabled: Boolean = true,
    label: String = "press",
): Modifier {
    val motion = clarityMotion()
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && enabled) 0.97f else 1f,
        animationSpec = motion.springStandard(),
        label = label,
    )
    return scale(scale)
}

/**
 * **Every tappable thing in this app acknowledges the thumb, and until now most did not.**
 *
 * Counted across `ui/`: 54 call sites, 15 with any press response. Every Settings row and
 * toggle, every Trail row, every queue row in the area sheet, the pushed screen back
 * chevron and both swipe faces did nothing at all under a finger, which is the difference
 * between a control and a picture of a control. `indication = null` was set on purpose,
 * to keep Material's ripple out, and then nothing was put in its place.
 *
 * What goes in its place is a **flat ink ground at 6 percent**, not a ripple and not a
 * scale. A ripple animates outward from a point and is the platform's signature rather
 * than this app's; a scale is right for a card or a button, which is why
 * [clarityPressScale] exists and stays, and wrong for a full width row, which would
 * detach from its neighbors as it shrank. A tonal press is the one treatment that works
 * on a row, a glyph, a chip and a face alike.
 *
 * It rises on the **fast effects spring**, because a press response that a person can
 * outrun is worse than none: `clickable` already delays its press interaction by 100ms
 * inside a scrollable parent, so everything after that has to be immediate.
 *
 * `pressShape` clips the ground. A row leaves it null and the ground fills the rectangle
 * it was given; anything with a corner passes its own shape, or the tint would square off
 * the element it is meant to be inside.
 */
fun Modifier.clarityClickable(
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource? = null,
    haptic: ClarityHapticEvent? = ClarityHapticEvent.TAP,
    role: Role? = null,
    onClickLabel: String? = null,
    pressShape: Shape? = null,
    showPress: Boolean = true,
    onClick: () -> Unit,
): Modifier = composed {
    val haptics = LocalClarityHaptics.current
    val source = interactionSource ?: remember { MutableInteractionSource() }
    val motion = clarityMotion()
    val colors = LocalClarityColors.current
    val night = LocalContemplativeColors.current
    val pressed by source.collectIsPressedAsState()
    // **The veil follows the surface it is drawn on, not the theme.**
    //
    // `LocalClarityColors` is provided once at the root and never swapped for a
    // Contemplative surface, so `inkPrimary` on the Report or the Pulse in the light world
    // is a near black veil over a near black page: arithmetically present and invisible.
    // Every Contemplative surface provides `LocalContemplativeColors`, and its bright text
    // is the right ink there for the same reason `inkPrimary` is the right ink on a page.
    val contemplative = LocalIsContemplative.current
    val veil = if (contemplative) night.textBright else colors.inkPrimary
    val ground by animateColorAsState(
        targetValue = if (pressed && enabled && showPress) {
            veil.copy(alpha = PRESS_GROUND_ALPHA)
        } else {
            Color.Transparent
        },
        animationSpec = motion.effectsFast(),
        label = "press",
    )
    // **Over the content, not behind it.** Almost every call site in this app reads
    // `.clip(...).background(...).clarityClickable(...)`, so a ground drawn behind would
    // sit under the element's own fill and never be seen. A 6 percent ink veil over the
    // top is what a press looks like on a filled surface anyway, and on a bare row it is
    // the same value doing the same job.
    val shaped = if (pressShape != null) Modifier.clip(pressShape) else Modifier
    shaped
        .drawWithContent {
            drawContent()
            if (ground != Color.Transparent) drawRect(ground)
        }
        .clickable(
            interactionSource = source,
            indication = null,
            enabled = enabled,
            onClickLabel = onClickLabel,
            role = role,
        ) {
            haptic?.let(haptics::perform)
            onClick()
        }
}

/** design-v3.md 8.2's press ground, and the one value every press in the app uses. */
private const val PRESS_GROUND_ALPHA = 0.06f

/**
 * Tap plus long press. The long press is how every swipe action stays reachable
 * without swiping, which design-v3.md 10.3.1 makes mandatory rather than optional.
 */
@OptIn(ExperimentalFoundationApi::class)
fun Modifier.clarityCombinedClickable(
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource? = null,
    role: Role? = null,
    onClickLabel: String? = null,
    onLongClickLabel: String? = null,
    onLongClick: (() -> Unit)? = null,
    onClick: () -> Unit,
): Modifier = composed {
    val haptics = LocalClarityHaptics.current
    combinedClickable(
        interactionSource = interactionSource,
        indication = null,
        enabled = enabled,
        onClickLabel = onClickLabel,
        role = role,
        onLongClickLabel = onLongClickLabel,
        onLongClick = onLongClick?.let {
            {
                haptics.perform(ClarityHapticEvent.PICK_UP)
                it()
            }
        },
    ) {
        haptics.perform(ClarityHapticEvent.TAP)
        onClick()
    }
}

/**
 * The keyboard focus state, designed rather than borrowed.
 *
 * A premium interface treats an interactive element as having six states, not
 * three: default, pressed, disabled, focused, and in other products hover and
 * loading. Focus was the one this app was missing, and it is the one a person
 * navigating by keyboard or switch access depends on entirely.
 *
 * A ring is not a separation device and does not fall under the one device rule in
 * design-v3.md 6.1. It is transient, it belongs to a state rather than to a
 * boundary, and it disappears the moment focus leaves.
 */
fun Modifier.clarityFocusRing(
    interactionSource: MutableInteractionSource,
    shape: Shape,
    width: Dp = 2.dp,
): Modifier = composed {
    val focused by interactionSource.collectIsFocusedAsState()
    val colors = LocalClarityColors.current
    val motion = clarityMotion()
    val ring by animateDpAsState(
        targetValue = if (focused) width else 0.dp,
        animationSpec = motion.effectsFast(),
        label = "focusRing",
    )
    // A zero width border still lays down a hairline, which would put a permanent
    // outline on every focusable element in the app. The ring is attached only
    // while it has a width to draw.
    if (ring <= 0.dp) this else border(ring, colors.actionBlue, shape)
}

/**
 * design-v3.md section 7 icons are drawn on a square canvas, but an asymmetric
 * glyph such as a play triangle carries its mass to one side, so centering it by
 * layout leaves it looking off center. Shifting it by roughly four percent of its
 * width toward the point corrects what the eye sees.
 *
 * Applies only to glyphs that are actually asymmetric. Used on a symmetric icon it
 * would introduce the error it exists to remove.
 */
fun Modifier.opticalGlyphNudge(iconSize: Dp): Modifier = offset(x = iconSize * 0.04f)
