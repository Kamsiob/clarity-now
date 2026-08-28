package com.kamsiob.claritynow.ui.focus

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.kamsiob.claritynow.ui.components.clarityClickable
import com.kamsiob.claritynow.ui.components.clarityFocusRing
import com.kamsiob.claritynow.ui.components.clarityPressScale
import com.kamsiob.claritynow.ui.theme.ClarityHapticEvent
import com.kamsiob.claritynow.ui.theme.ClaritySpacing
import com.kamsiob.claritynow.ui.theme.FocusPalette
import com.kamsiob.claritynow.ui.theme.LocalCalmMode
import com.kamsiob.claritynow.ui.theme.LocalClarityTypography
import com.kamsiob.claritynow.ui.theme.LocalContemplativeColors
import com.kamsiob.claritynow.ui.theme.calmed
import com.kamsiob.claritynow.ui.theme.opticallyCentered

/**
 * The Contemplative primary. design-v3.md 10.7.
 *
 * **10.7 offers two forms and this app uses one of them everywhere.** The surface
 * accent at 14 percent with a bright label, rather than the translucent white pill at
 * 9 percent, because section 11 specifies `Mark item complete` on the completion screen
 * as being in the accent, and a control that changed its treatment between two screens
 * of the same surface would read as two different controls. So the End session pill and
 * the Mark item complete pill are one component with one appearance. Recorded per
 * section 15, which asks for the choice to be made rather than defaulted.
 *
 * One separation device, design-v3.md 6.1: a background lightness shift. No border, and
 * no shadow, because the Contemplative world has no shadows at all.
 */
@Composable
internal fun FocusPill(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    haptic: ClarityHapticEvent = ClarityHapticEvent.TAP,
) {
    val contemplative = LocalContemplativeColors.current
    val type = LocalClarityTypography.current
    val accent = FocusPalette.ringProgress.calmed(LocalCalmMode.current)
    val interaction = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .clarityPressScale(interaction, label = "focusPill")
            // A minimum rather than a height, for the reason `ClarityButton` gives.
            .heightIn(min = ClaritySpacing.scaled(PILL_HEIGHT))
            .defaultMinSize(minWidth = PILL_MIN_WIDTH)
            .clip(CircleShape)
            .background(accent.copy(alpha = PILL_FILL_ALPHA))
            .clarityFocusRing(interaction, CircleShape)
            .clarityClickable(
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
            color = contemplative.textBright,
            modifier = Modifier.padding(horizontal = PILL_PADDING),
        )
    }
}

/**
 * The Contemplative tertiary. design-v3.md 10.7 and 10.18.
 *
 * Text only in the Focus accent, subordinate, and carrying no container of its own.
 * `Add 10 minutes` is the one on the session screen and `Done` is the one on the
 * completion screen, and both are deliberately quieter than the pill above them.
 *
 * The label is drawn at its own size and sits inside a 48dp target rather than being
 * grown to fill one, the way `ClarityChip` separates what is drawn from what is
 * touched. design-v3.md 13 fixes the minimum target and nothing in 10.7 gives a text
 * only control a height.
 */
@Composable
internal fun FocusTextAction(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    haptic: ClarityHapticEvent = ClarityHapticEvent.TAP,
) {
    val type = LocalClarityTypography.current
    val accent = FocusPalette.ringProgress.calmed(LocalCalmMode.current)
    val interaction = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .sizeIn(minHeight = ClaritySpacing.minTouchTarget)
            .clip(CircleShape)
            .clarityFocusRing(interaction, CircleShape)
            .clarityClickable(
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
            color = accent,
            modifier = Modifier
                .clarityPressScale(interaction, label = "focusTextAction")
                .padding(horizontal = TEXT_ACTION_PADDING),
        )
    }
}

private val PILL_HEIGHT = 50.dp
private val PILL_MIN_WIDTH = 160.dp
private val PILL_PADDING = 28.dp
private val TEXT_ACTION_PADDING = 16.dp

/** design-v3.md 10.7, the Contemplative primary's fill. */
private const val PILL_FILL_ALPHA = 0.14f
