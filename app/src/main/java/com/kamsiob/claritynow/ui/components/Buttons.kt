package com.kamsiob.claritynow.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.kamsiob.claritynow.ui.theme.ClarityElevation
import com.kamsiob.claritynow.ui.theme.ClarityHapticEvent
import com.kamsiob.claritynow.ui.theme.ClaritySpacing
import com.kamsiob.claritynow.ui.theme.LocalClarityColors
import com.kamsiob.claritynow.ui.theme.LocalClarityTypography
import com.kamsiob.claritynow.ui.theme.clarityMotion
import com.kamsiob.claritynow.ui.theme.opticallyCentered

/**
 * design-v3.md 10.7. Five button roles, none of them bordered.
 *
 * Destructive is inert grey until its condition is met and then ink filled. It is
 * never red, because red for an ordinary deliberate action is a warning tone this
 * design does not use.
 *
 * ## Two label colors moved in the phase 13 contrast audit
 *
 * **A filled surface inverts its label to `card`, and that is now one rule for two
 * roles.** Primary's label was `Color.White`, which measured 3.81 to one on the light
 * `actionBlue` and **2.63** on the dark one, against design-v3.md 13's floor of 4.5. The
 * light half of that went away when `actionBlue` darkened, 3.1; the dark half could not,
 * because no blue is both light enough to be read on `#0E0E13` and dark enough to hold
 * white. Destructive already inverted to `card` on its ink fill, 10.7 and 10.8, so
 * Primary takes the same inversion rather than a second rule: it measures 7.76 in light
 * and 6.38 in dark. [ClarityFab]'s glyph moved with it for the same reason.
 *
 * **Positive's label is `positiveInk`.** `positiveGreen` on its own 13 percent fill
 * measured 1.68 on the page and 1.98 on a card. 3.1 splits the completion color into a
 * fill and a foreground precisely here: the fill has to stay a light mint for what sits
 * on it, and a label on that fill has to be dark. The label reads 5.34 and 6.29.
 */
enum class ClarityButtonRole { PRIMARY, POSITIVE, SECONDARY, TERTIARY, DESTRUCTIVE }

@Composable
fun ClarityButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    role: ClarityButtonRole = ClarityButtonRole.PRIMARY,
    enabled: Boolean = true,
    fillWidth: Boolean = true,
) {
    val colors = LocalClarityColors.current
    val type = LocalClarityTypography.current
    val motion = clarityMotion()
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && enabled) 0.97f else 1f,
        animationSpec = motion.springStandard(),
        label = "buttonPress",
    )

    val background = when (role) {
        ClarityButtonRole.PRIMARY -> colors.actionBlue
        ClarityButtonRole.POSITIVE -> colors.positiveGreen.copy(alpha = 0.13f)
        ClarityButtonRole.SECONDARY -> colors.inkPrimary.copy(alpha = 0.05f)
        ClarityButtonRole.TERTIARY -> Color.Transparent
        ClarityButtonRole.DESTRUCTIVE ->
            if (enabled) colors.inkPrimary else colors.inkPrimary.copy(alpha = 0.10f)
    }
    val labelColor = when (role) {
        ClarityButtonRole.PRIMARY -> colors.card
        ClarityButtonRole.POSITIVE -> colors.positiveInk
        ClarityButtonRole.SECONDARY -> colors.inkPrimary
        ClarityButtonRole.TERTIARY -> colors.actionBlue
        ClarityButtonRole.DESTRUCTIVE -> if (enabled) colors.card else colors.inkTertiary
    }

    val haptic = if (role == ClarityButtonRole.DESTRUCTIVE) {
        ClarityHapticEvent.WARN
    } else {
        ClarityHapticEvent.TAP
    }

    Box(
        modifier = modifier
            .then(if (fillWidth) Modifier.fillMaxWidth() else Modifier)
            .scale(scale)
            // design-v3.md 10.7's 50dp, as a minimum rather than a height, and scaled.
            // A fixed box here was a clipping site: the label is `bodyStrong`, it has no
            // `maxLines`, and a two word button at 200 percent wraps to a second line
            // that a `height` would have cut off with nothing to see it happen. The
            // minimum grows with the text so the button stays a button rather than
            // becoming a label with a tight collar. design-v3.md 13, Addendum 01 8f.
            .heightIn(min = ClaritySpacing.scaled(BUTTON_HEIGHT))
            .clip(RoundedCornerShape(12.dp))
            .background(background)
            .clarityFocusRing(interaction, RoundedCornerShape(12.dp))
            .clarityClickable(
                enabled = enabled,
                interactionSource = interaction,
                haptic = haptic,
                role = Role.Button,
                onClickLabel = label,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = type.bodyStrong.opticallyCentered(),
            color = labelColor,
            modifier = Modifier.padding(horizontal = 20.dp),
        )
    }
}

/**
 * design-v3.md 10.5 and 8.2 item 16. A 48dp circle above the tab bar at the
 * trailing edge, pressing to 0.94 with the snappy spring.
 */
@Composable
fun ClarityFab(
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    val colors = LocalClarityColors.current
    val motion = clarityMotion()
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.94f else 1f,
        animationSpec = motion.springSnappy(),
        label = "fabPress",
    )

    Box(
        modifier = modifier
            .scale(scale)
            .size(48.dp)
            // design-v3.md 6.1: "Dark and Contemplative worlds: elevation is
            // lightness only. No shadows at all." Every other clarityShadow call
            // site guards on this; the FAB was drawing its colored glow in dark.
            .clarityShadow(
                ClarityElevation.fab(colors.actionBlue),
                CircleShape,
                enabled = !colors.isDark,
            )
            .clip(CircleShape)
            .background(colors.actionBlue)
            .clarityFocusRing(interaction, CircleShape)
            .clarityClickable(
                interactionSource = interaction,
                haptic = ClarityHapticEvent.TAP,
                role = Role.Button,
                onClickLabel = contentDescription,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        ClarityIcon(
            icon = ClarityIcons.add,
            contentDescription = contentDescription,
            // The same inversion the primary button's label takes, and for the same
            // measurement. See the note on ClarityButtonRole.
            tint = colors.card,
            modifier = Modifier.size(24.dp),
        )
    }
}

/**
 * A pill chip. design-v3.md 10.1 and 10.8. Card colored with soft elevation and no
 * border when unselected, a solid ink pill with an inverted label when selected.
 *
 * Those two states carry different separation devices on purpose, and only ever one
 * at a time: the unselected chip is separated by its soft elevation, and the selected
 * chip by a background lightness shift taken to its maximum. A shadow surviving under
 * the ink fill would be the two device violation in design-v3.md 6.1, so it is gated
 * off at the same instant the fill arrives.
 *
 * The pill is 38dp and sits inside a 48dp target rather than being grown to fill one.
 * design-v3.md 10.8 fixes the chip's padding at 15dp by 9dp and design-v3.md 13 fixes
 * the minimum touch target at 48dp. The only way to honor both is to separate the
 * thing that is drawn from the thing that is touched; growing the pill would have
 * quietly overwritten a dimension the design already stated.
 *
 * [dotColor] draws the 7dp area dot that design-v3.md 10.8 puts on an area chip and
 * leaves off the All chip. It is a typed color rather than an open composable slot
 * because design-v3.md 3.4 permits area color in exactly four places, of which a 7dp
 * dot is one, and a free slot at the leading edge is an invitation to the other
 * three. The dot keeps its own color while the chip is selected: filling the pill
 * with the area color would be 3.4's banned filled block, and dropping the dot would
 * erase the area's identity at the moment it becomes relevant.
 */
@Composable
fun ClarityChip(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    dotColor: Color? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    val colors = LocalClarityColors.current
    val type = LocalClarityTypography.current
    val motion = clarityMotion()
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = motion.springSnappy(),
        label = "chipPress",
    )

    // Selection is a change of color, not of position, so it runs on the critically
    // damped effects spring rather than the snappy one the press uses. springSnappy
    // overshoots by design, and an ink fill or a label color that overshoots on the
    // way in reads as a bounce on every tap, which design-v3.md 15.1 lists as a tell.
    // ClarityMotion says the same thing about the token itself: an alpha that
    // overshoots is a bug rather than a flourish. The press scale keeps springSnappy,
    // which design-v3.md 8.1 assigns to chips by name.
    //
    // An unselected chip is chrome and sits at `raise`, one step under the content it
    // filters. design-v3.md 3.1 as amended in phase 3c. A selected chip inverts to ink
    // and leaves the ladder entirely, which is the point of the inversion.
    val background by animateColorAsState(
        targetValue = if (selected) colors.inkPrimary else colors.raise,
        animationSpec = motion.effects(),
        label = "chipBackground",
    )
    val labelColor by animateColorAsState(
        targetValue = if (selected) colors.card else colors.inkSecondary,
        animationSpec = motion.effects(),
        label = "chipLabel",
    )

    Box(
        modifier = modifier
            .sizeIn(minHeight = ClaritySpacing.minTouchTarget)
            .clarityClickable(
                interactionSource = interaction,
                haptic = ClarityHapticEvent.TAP,
                role = Role.Button,
                onClickLabel = label,
                onClick = onClick,
            )
            // Without this a selected chip and an unselected one are the same node to
            // TalkBack, which leaves the inverted ink as a color only signal.
            // design-v3.md 13: color is never the only signal. The caller puts the row
            // in a selectableGroup so the set is announced as a set.
            .semantics { this.selected = selected },
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier
                .scale(scale)
                .defaultMinSize(minHeight = 38.dp)
                .clarityShadow(ClarityElevation.card, CircleShape, enabled = !colors.isDark && !selected)
                .clip(CircleShape)
                .background(background)
                .clarityFocusRing(interaction, CircleShape)
                .padding(horizontal = 15.dp, vertical = ClaritySpacing.scaled(9.dp)),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            if (dotColor != null) {
                // No content description. The chip's own label already names the area,
                // and a second node here would make TalkBack read it twice.
                Box(
                    modifier = Modifier
                        .size(ClaritySpacing.areaDot)
                        .clip(CircleShape)
                        .background(dotColor),
                )
                Box(Modifier.size(width = 7.dp, height = 1.dp))
            }
            Text(
                text = label,
                style = type.label.opticallyCentered(),
                color = labelColor,
            )
            if (trailing != null) {
                Box(Modifier.size(width = 6.dp, height = 1.dp))
                trailing()
            }
        }
    }
}

/** design-v3.md 10.7. A minimum, never a height. See [ClarityButton]. */
private val BUTTON_HEIGHT = 50.dp
