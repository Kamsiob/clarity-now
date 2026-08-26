package com.kamsiob.claritynow.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kamsiob.claritynow.ui.theme.LocalClarityColors
import com.kamsiob.claritynow.ui.theme.clarityMotion
import androidx.compose.ui.composed
import androidx.compose.ui.semantics.Role
import com.kamsiob.claritynow.ui.theme.ClarityHapticEvent
import com.kamsiob.claritynow.ui.theme.LocalClarityHaptics

/**
 * Every tappable thing in the app goes through here.
 *
 * Indication is null on purpose: feedback is the scale press in design-v3.md 8.2
 * item 2, and a Material ripple underneath it would be a second, borrowed
 * treatment on the same gesture. The haptic fires once per action, never on the
 * way in and again on the way out.
 */
fun Modifier.clarityClickable(
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource? = null,
    haptic: ClarityHapticEvent? = ClarityHapticEvent.TAP,
    role: Role? = null,
    onClickLabel: String? = null,
    onClick: () -> Unit,
): Modifier = composed {
    val haptics = LocalClarityHaptics.current
    clickable(
        interactionSource = interactionSource,
        indication = null,
        enabled = enabled,
        onClickLabel = onClickLabel,
        role = role,
    ) {
        haptic?.let(haptics::perform)
        onClick()
    }
}

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
