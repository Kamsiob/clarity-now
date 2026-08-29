package com.kamsiob.claritynow.ui.trail

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.kamsiob.claritynow.R
import com.kamsiob.claritynow.ui.components.ClarityIcon
import com.kamsiob.claritynow.ui.components.ClarityIcons
import com.kamsiob.claritynow.ui.components.ClaritySheet
import com.kamsiob.claritynow.ui.components.clarityClickable
import com.kamsiob.claritynow.ui.theme.ClarityHapticEvent
import com.kamsiob.claritynow.ui.theme.ClaritySpacing
import com.kamsiob.claritynow.ui.theme.LocalClarityColors
import com.kamsiob.claritynow.ui.theme.LocalClarityShapes
import com.kamsiob.claritynow.ui.theme.LocalClarityTypography

/**
 * **Putting something back, from the screen a person actually looks for it on.**
 *
 * Completing a thing is the one act in this app that removes it from every screen a
 * person works on, and until now the only way back was the area detail sheet, which
 * lists an area's completions but is two taps behind a card and is not where anyone
 * looks. The Trail is where they look, because it is the one screen that holds
 * everything in the order it happened.
 *
 * **Two destinations, because there are genuinely two intentions**, and guessing
 * between them is the thing that would make the feature annoying:
 *
 * - *Put it back in the queue* is the correction. It was not finished, or it came back,
 *   and it should wait its turn. It goes to the head of the queue and nothing else on
 *   the screen moves.
 * - *Make it the active one* is the decision to work on it now. Whatever was active in
 *   that area is demoted to the head of the queue, which is the same eviction Swap
 *   performs, so nothing is lost and there is nothing to warn about.
 *
 * The second is named as what it does, including the eviction, because an action that
 * quietly displaces something a person chose earlier is the kind of surprise this app
 * does not get to spend.
 *
 * **No confirmation and no destructive styling.** Neither of these loses anything: the
 * completion stays in the Trail as the event it always was, and the reopening is one
 * more event on top of it. design-v3.md's undo discipline is for acts that remove; this
 * is the act that restores.
 */
@Composable
fun ReopenSheet(
    title: String,
    onReopenToQueue: () -> Unit,
    onReopenAsActive: () -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = LocalClarityColors.current
    val type = LocalClarityTypography.current

    ClaritySheet(onDismiss = onDismiss) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.sidehead_completed),
                style = type.sidehead,
                color = colors.inkSecondary,
                modifier = Modifier.padding(horizontal = ClaritySpacing.step),
            )
            Spacer(Modifier.height(ClaritySpacing.snug))
            Text(
                text = title,
                style = type.itemTitle,
                color = colors.inkPrimary,
                modifier = Modifier.padding(horizontal = ClaritySpacing.step),
            )
            Spacer(Modifier.height(ClaritySpacing.rest))
            ReopenChoice(
                icon = ClarityIcons.expand,
                label = stringResource(R.string.reopen_to_queue),
                detail = stringResource(R.string.reopen_to_queue_detail),
                onClick = onReopenToQueue,
            )
            ReopenChoice(
                icon = ClarityIcons.promoted,
                label = stringResource(R.string.reopen_as_active),
                detail = stringResource(R.string.reopen_as_active_detail),
                onClick = onReopenAsActive,
            )
            Spacer(Modifier.height(ClaritySpacing.step))
        }
    }
}

@Composable
private fun ReopenChoice(
    @DrawableRes icon: Int,
    label: String,
    detail: String,
    onClick: () -> Unit,
) {
    val colors = LocalClarityColors.current
    val type = LocalClarityTypography.current
    val shapes = LocalClarityShapes.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ClaritySpacing.snug, vertical = ClaritySpacing.hair)
            .clip(shapes.row)
            .clarityClickable(
                haptic = ClarityHapticEvent.TAP,
                role = Role.Button,
                onClickLabel = label,
                onClick = onClick,
            )
            .padding(horizontal = ClaritySpacing.tight, vertical = ClaritySpacing.snug),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(colors.raise),
            contentAlignment = Alignment.Center,
        ) {
            ClarityIcon(
                icon = icon,
                contentDescription = null,
                tint = colors.inkSecondary,
                modifier = Modifier.size(20.dp),
            )
        }
        Column(modifier = Modifier.padding(start = ClaritySpacing.snug)) {
            Text(text = label, style = type.bodyStrong, color = colors.inkPrimary)
            Text(text = detail, style = type.caption, color = colors.inkSecondary)
        }
    }
}
