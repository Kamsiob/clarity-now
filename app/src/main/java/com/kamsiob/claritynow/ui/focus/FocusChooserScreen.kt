package com.kamsiob.claritynow.ui.focus

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kamsiob.claritynow.R
import com.kamsiob.claritynow.ui.components.clarityClickable
import com.kamsiob.claritynow.ui.components.clarityFocusRing
import com.kamsiob.claritynow.ui.components.clarityPressScale
import com.kamsiob.claritynow.ui.theme.ClarityHapticEvent
import com.kamsiob.claritynow.ui.theme.ClaritySpacing
import com.kamsiob.claritynow.ui.theme.LocalClarityTypography
import com.kamsiob.claritynow.ui.theme.LocalContemplativeColors
import com.kamsiob.claritynow.ui.theme.clarityMotion
import com.kamsiob.claritynow.ui.theme.parseAreaColor

/**
 * The chooser. MASTER_BUILD_PROMPT section 10, design-v3.md 10.15.
 *
 * Areas with an active item, and areas without one dimmed, non selectable and reading
 * `Add an item first`. Selecting starts a session on that area's active item at the
 * length in settings.
 *
 * **A pushed screen rather than a sheet**, which is what design-v3.md 10.15's table
 * says: every sheet in that table is left by "drag down, scrim tap, or back" and the
 * Focus chooser is left by "back" alone. It is also the door into the Contemplative
 * world, so the room dims here rather than one screen later.
 *
 * **The rows carry one separation device and it is whitespace.** design-v3.md 6.1.
 * There is no card, no hairline and no raised surface under a row: the indigo gradient
 * is the surface, and a row that sat on its own panel would cover the light this
 * surface is made of.
 */
@Composable
internal fun FocusChooserScreen(
    options: List<FocusAreaOption>,
    durationMinutes: Int,
    onSelect: (areaId: String, itemId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val contemplative = LocalContemplativeColors.current
    val type = LocalClarityTypography.current

    Column(
        modifier = modifier
            .fillMaxSize()
            // Outside the scroll, so the system bars inset the viewport rather than
            // scrolling away with the content. The gradient behind this reaches the
            // very edge of the screen either way, because it is a sibling.
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = ClaritySpacing.screenPadding),
    ) {
        Spacer(Modifier.height(ClaritySpacing.scaled(TITLE_TOP)))
        Text(
            // design-v3.md section 11 opens every surface with a headline treatment,
            // and the Trail entry there settles the objection that the chip the person
            // just tapped already said this word: one serif line is what makes a screen
            // begin rather than start mid content.
            text = stringResource(R.string.focus_title),
            style = type.displayTitle,
            color = contemplative.textBright,
        )
        Spacer(Modifier.height(ClaritySpacing.scaled(8.dp)))
        Text(
            // Fixed interface copy and a direct readout of a setting, never an
            // observation. It is here because a tap that silently commits someone to
            // twenty five minutes is exactly the unannounced behavior design-v3.md
            // section 11 asks this app to spend a line of copy on, and the second
            // sentence answers the fear that starting a timer is a commitment.
            text = pluralStringResource(
                R.plurals.focus_chooser_duration,
                durationMinutes,
                durationMinutes,
            ),
            style = type.caption,
            color = contemplative.textDim,
        )
        Spacer(Modifier.height(ClaritySpacing.sectionGap))

        if (options.isEmpty()) {
            FocusChooserEmptyState()
        } else {
            options.forEach { option ->
                FocusAreaRow(
                    option = option,
                    onSelect = {
                        val itemId = option.activeItemId
                        if (itemId != null) onSelect(option.areaId, itemId)
                    },
                )
                Spacer(Modifier.height(ClaritySpacing.scaled(ROW_GAP)))
            }
        }
        Spacer(Modifier.height(ClaritySpacing.scaled(BOTTOM_ROOM)))
    }
}

/**
 * One area. Two lines: the area, then the thing that would be focused on.
 *
 * The item title is the larger of the two because it is what the session is actually
 * about, which is the same hierarchy design-v3.md 10.3 gives the area card.
 *
 * A dimmed row is inert rather than disabled: it carries no click at all, so a screen
 * reader announces two pieces of text and not a button that refuses. The hint says why
 * in words, so the dimming is never the only signal, per design-v3.md 13.
 */
@Composable
private fun FocusAreaRow(
    option: FocusAreaOption,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contemplative = LocalContemplativeColors.current
    val type = LocalClarityTypography.current
    val interaction = remember { MutableInteractionSource() }
    val accent = parseAreaColor(option.colorHex)
    val startLabel = stringResource(R.string.cd_focus_start, option.activeItemTitle.orEmpty())

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(ROW_SHAPE)
            .then(
                if (option.selectable) {
                    Modifier
                        .clarityPressScale(interaction, label = "focusAreaRow")
                        .clarityFocusRing(interaction, ROW_SHAPE)
                        .clarityClickable(
                            interactionSource = interaction,
                            // design-v3.md 9, focusStart: two low ticks 90ms apart, at
                            // the moment the session begins.
                            haptic = ClarityHapticEvent.FOCUS_START,
                            role = Role.Button,
                            onClickLabel = startLabel,
                            onClick = onSelect,
                        )
                } else {
                    Modifier
                },
            )
            .padding(vertical = ClaritySpacing.scaled(ROW_PADDING)),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
        ) {
            Box(
                modifier = Modifier
                    .size(ClaritySpacing.areaDot)
                    .clip(CircleShape)
                    // The 7dp dot is one of the four forms design-v3.md 3.4 permits an
                    // area color to take, and it is excluded from calm mode's transform
                    // by name, because it is how an area is recognized. On a dimmed row
                    // it drops to 45 percent, matching the idle card in 10.3.
                    .background(accent.copy(alpha = if (option.selectable) 1f else DIMMED_DOT_ALPHA)),
            )
            Spacer(Modifier.size(width = 9.dp, height = 1.dp))
            Text(
                // The area name is textDim rather than the area color. design-v3.md
                // 3.4 permits the label in the accent, and `areaLabelColor` computes
                // that variant against a Daylight card: on the indigo gradient it would
                // be a fifth application point on a ground nothing has measured, which
                // is the exact defect the phase 3c contrast audit found. The dot beside
                // it already carries the identity. Recorded per section 15.
                text = option.areaName,
                style = type.label,
                color = if (option.selectable) contemplative.textDim else contemplative.textFaint,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.height(ClaritySpacing.scaled(6.dp)))
        Text(
            text = option.activeItemTitle ?: stringResource(R.string.focus_chooser_needs_item),
            style = if (option.selectable) type.itemTitle else type.body,
            color = if (option.selectable) contemplative.textBright else contemplative.textFaint,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/**
 * design-v3.md 10.13. An invitation, never a scold, naming the next action in plain
 * words. No illustration, no mascot, no exclamation mark.
 *
 * Item 25's entrance, written out for the same reason `ui/areas/AreasScreen.kt` writes
 * it out: the 150ms delay is a guard against a flash during a load that resolves
 * quickly, so design-v3.md 8.4 keeps the delay when motion is reduced and shortens only
 * the fade.
 */
@Composable
private fun FocusChooserEmptyState() {
    val contemplative = LocalContemplativeColors.current
    val type = LocalClarityTypography.current
    val motion = clarityMotion()
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { shown = true }

    val entrance = tween<Float>(
        durationMillis = if (motion.reduced) 150 else 400,
        delayMillis = 150,
        easing = EaseOutCubic,
    )

    AnimatedVisibility(visible = shown, enter = fadeIn(entrance)) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(top = ClaritySpacing.scaled(48.dp)),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.focus_chooser_empty_title),
                style = type.readSerif,
                color = contemplative.textBright,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(ClaritySpacing.scaled(10.dp)))
            Text(
                text = stringResource(R.string.focus_chooser_empty_body),
                style = type.body,
                color = contemplative.textDim,
                textAlign = TextAlign.Center,
            )
        }
    }
}

private val TITLE_TOP = 24.dp
private val ROW_PADDING = 14.dp
private val ROW_GAP = 10.dp
private val BOTTOM_ROOM = 48.dp
/** design-v3.md section 6: rows are 12dp. It shapes the keyboard focus ring and
 *  nothing else, because the row has no fill of its own. */
private val ROW_SHAPE = RoundedCornerShape(12.dp)
private const val DIMMED_DOT_ALPHA = 0.45f
