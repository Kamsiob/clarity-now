package com.kamsiob.claritynow.ui.report

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.annotation.DrawableRes
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.kamsiob.claritynow.R
import com.kamsiob.claritynow.ui.components.ClarityIcon
import com.kamsiob.claritynow.ui.components.ClarityIcons
import com.kamsiob.claritynow.ui.components.clarityClickable
import com.kamsiob.claritynow.ui.components.clarityFocusRing
import com.kamsiob.claritynow.ui.components.clarityPressScale
import com.kamsiob.claritynow.ui.theme.ClarityHapticEvent
import com.kamsiob.claritynow.ui.theme.ClaritySpacing
import com.kamsiob.claritynow.ui.theme.LocalContemplativeColors

/**
 * The three controls, faint, top right. `design-v3.md` 11.1 item 1 and
 * `MASTER_BUILD_PROMPT.md` 12.3.
 *
 * History, regenerate, copy, in that order, which is the order both documents name them
 * in. Icon only, in `textFaint`, with no container, no label and no divider between them:
 * they are item 1 of a page that is allowed four treatments, and a toolbar with chrome
 * would be a fifth.
 *
 * **Faint is the resting state and not the whole story.** Each control is a 48dp target,
 * per section 13, around a 20dp glyph, so the tap area is comfortable while the mark stays
 * quiet. Every one carries a content description, because an icon with no label is the one
 * shape that is unreadable to a screen reader by construction.
 */
@Composable
internal fun ReportControls(
    onHistory: () -> Unit,
    onRegenerate: () -> Unit,
    onCopy: () -> Unit,
    regenerating: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(CONTROL_GAP),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // design-v3.md 6.3 names this destination's glyph `history`, which is the same
        // drawable the Trail tab carries. One glyph, one meaning.
        ReportControl(
            icon = ClarityIcons.trail,
            description = stringResource(R.string.cd_report_history),
            onClick = onHistory,
        )
        ReportControl(
            icon = ClarityIcons.regenerate,
            description = stringResource(R.string.cd_report_regenerate),
            enabled = !regenerating,
            onClick = onRegenerate,
        )
        ReportControl(
            icon = ClarityIcons.copy,
            description = stringResource(R.string.cd_report_copy),
            onClick = onCopy,
        )
    }
}

@Composable
private fun ReportControl(
    @DrawableRes icon: Int,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val contemplative = LocalContemplativeColors.current
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .size(ClaritySpacing.minTouchTarget)
            .clarityPressScale(interaction, enabled = enabled, label = "reportControl")
            .clarityFocusRing(interaction, CircleShape)
            .clarityClickable(
                enabled = enabled,
                interactionSource = interaction,
                haptic = ClarityHapticEvent.TAP,
                role = Role.Button,
                onClickLabel = description,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        ClarityIcon(
            icon = icon,
            // The clickable above already carries the label, so the glyph inside it is
            // decorative and announcing it again would read the control's name twice.
            contentDescription = null,
            // An active control's glyph, so design-v3.md 13's 3.0 floor for a graphic
            // is the least it has to clear and 32 percent misses it at 2.637 on this
            // page. It takes `textDim` rather than a value between the two, because
            // 3.3 gives the Contemplative world one token for secondary type and this
            // is the same rank as the `Jump in` label and the onboarding controls.
            tint = contemplative.textDim,
            modifier = Modifier.size(GLYPH_SIZE),
        )
    }
}

/**
 * Puts [text] on the clipboard. `MASTER_BUILD_PROMPT.md` 12.3.
 *
 * **This is the app's only integration surface with anything else**, which is why it is one
 * function taking a finished string rather than a share sheet, a file, an intent or a
 * format. Plain text, one clip, nothing observed and nothing sent.
 *
 * The platform clipboard rather than Compose's, deliberately: `LocalClipboardManager` is
 * deprecated in this Compose version and the build treats every warning as an error, and
 * the replacement's clip entry type is Android's `ClipData` in any case. Nothing is shown
 * afterwards, because Android 13 and later post their own confirmation and a second one
 * from the app would be two toasts for one tap.
 */
internal fun copyToClipboard(context: Context, label: String, text: String) {
    val clipboard = context.getSystemService(ClipboardManager::class.java) ?: return
    clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
}

private val CONTROL_GAP = 2.dp
private val GLYPH_SIZE = 20.dp
