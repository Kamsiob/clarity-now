package com.kamsiob.claritynow.ui.components

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.unit.dp
import com.kamsiob.claritynow.ui.theme.ClarityElevation
import com.kamsiob.claritynow.ui.theme.ClarityHapticEvent
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
        ClarityButtonRole.PRIMARY -> Color.White
        ClarityButtonRole.POSITIVE -> colors.positiveGreen
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
            .height(50.dp)
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
            .clarityShadow(ClarityElevation.fab(colors.actionBlue), CircleShape, enabled = true)
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
            tint = Color.White,
            modifier = Modifier.size(24.dp),
        )
    }
}

/**
 * A pill chip. design-v3.md 10.1 and 10.8. Card colored with soft elevation and no
 * border, because a chip carrying both would be the two device violation.
 */
@Composable
fun ClarityChip(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    leading: (@Composable () -> Unit)? = null,
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

    Box(
        modifier = modifier
            .scale(scale)
            .defaultMinSize(minHeight = 38.dp)
            .clarityShadow(ClarityElevation.card, CircleShape, enabled = !colors.isDark && !selected)
            .clip(CircleShape)
            .background(if (selected) colors.inkPrimary else colors.card)
            .clarityFocusRing(interaction, CircleShape)
            .clarityClickable(
                interactionSource = interaction,
                haptic = ClarityHapticEvent.TAP,
                role = Role.Button,
                onClickLabel = label,
                onClick = onClick,
            )
            .padding(horizontal = 15.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            if (leading != null) {
                leading()
                Box(Modifier.size(width = 7.dp, height = 1.dp))
            }
            Text(
                text = label,
                style = type.label.opticallyCentered(),
                color = if (selected) colors.card else colors.inkSecondary,
            )
            if (trailing != null) {
                Box(Modifier.size(width = 6.dp, height = 1.dp))
                trailing()
            }
        }
    }
}
