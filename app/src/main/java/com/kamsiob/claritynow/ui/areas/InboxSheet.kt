package com.kamsiob.claritynow.ui.areas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kamsiob.claritynow.R
import com.kamsiob.claritynow.domain.replay.ItemState
import com.kamsiob.claritynow.ui.components.ClarityButton
import com.kamsiob.claritynow.ui.components.ClarityButtonRole
import com.kamsiob.claritynow.ui.components.ClaritySheet
import com.kamsiob.claritynow.ui.components.clarityClickable
import com.kamsiob.claritynow.ui.theme.ClaritySpacing
import com.kamsiob.claritynow.ui.theme.LocalClarityColors
import com.kamsiob.claritynow.ui.theme.LocalClarityTypography
import com.kamsiob.claritynow.ui.theme.parseAreaColor

/**
 * The unfiled inbox. design-v3.md 10.16, MASTER_BUILD_PROMPT 14b.1, Addendum 01 4a.
 *
 * **Capture must never require a decision.** Every decision between a thought and a
 * record is a place the thought is lost, and for an audience whose central difficulty
 * is deciding, choosing an area before the thought is written down is exactly such a
 * place. So an item can be added with no area, and this is where it waits.
 *
 * **It is a sheet, not a tab and not a pinned row.** design-v3.md 10.15's destination
 * table has four tabs and this is not a fifth. The obvious answer, a pinned row at the
 * top of the area list carrying a count badge, is what every inbox in every app does
 * and it is rejected twice over: a badge is forbidden by design-v3.md 14 and by the
 * addendum's own wording, and a pinned row puts the pile of things a person has not
 * dealt with above the one thing they opened the app to see, every single time. That
 * feeling is the one this app exists to remove.
 *
 * **Complete, Swap and Focus are absent rather than present and disabled.** An unfiled
 * item cannot be active and cannot be completed until it is filed, and a disabled
 * control is a question the person then has to answer. Three things are offered: move
 * it to an area, edit it, delete it.
 *
 * **Nothing here reports on the pile.** There is no review prompt, no weekly filing
 * reminder, no sorting ceremony and no observation about how much is in here. An
 * inbox that grows is not a finding. The header chip is the only surface in the whole
 * document that states its size, and it states it as plain text.
 *
 * Plain rows on the sheet surface, no cards, per design-v3.md 10.6.
 */
@Composable
fun InboxSheet(
    items: List<ItemState>,
    onOpenItem: (ItemState) -> Unit,
    onMoveItem: (ItemState) -> Unit,
    onDismiss: () -> Unit,
) {
    ClaritySheet(onDismiss = onDismiss, title = stringResource(R.string.sheet_inbox_title)) {
        Column(
            modifier = Modifier
                .heightIn(max = 560.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            if (items.isEmpty()) {
                InboxEmptyState()
            } else {
                items.forEach { item ->
                    InboxRow(
                        item = item,
                        onOpen = { onOpenItem(item) },
                        onMove = { onMoveItem(item) },
                    )
                }
            }
            Spacer(Modifier.height(ClaritySpacing.scaled(10.dp)))
        }
    }
}

/**
 * One captured thought. Its title, and its first step when it has one.
 *
 * design-v3.md 10.16 puts the first step on these rows, which is the one place other
 * than the active item card and the area detail sheet that it appears. It earns the
 * space here for the same reason it earns it there: this is a list of things a person
 * has not started, and the first step is the sentence that makes starting possible.
 *
 * **Two targets, and the split is deliberate.** The row itself opens the edit sheet,
 * which is where editing and deleting already live for a queued item, so an inbox
 * item behaves like every other item a person taps. The trailing control is filing,
 * which is the one act this surface exists for. Its visible label is short because the
 * title beside it is the content and must keep the width; the full phrase
 * `Move to an area` is what a screen reader announces and what the sheet it opens is
 * titled, so nothing about the action is abbreviated where it matters.
 *
 * The row carries no separation device of its own, per design-v3.md 6.1. The sheet
 * surface is the separation, and a hairline between rows on top of it would be the
 * second device the rule forbids.
 */
@Composable
private fun InboxRow(item: ItemState, onOpen: () -> Unit, onMove: () -> Unit) {
    val colors = LocalClarityColors.current
    val type = LocalClarityTypography.current
    val moveLabel = stringResource(R.string.cd_move_to_area)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clarityClickable(onClickLabel = item.title, onClick = onOpen)
            .padding(
                start = ClaritySpacing.screenPadding,
                // The trailing control carries its own 8dp, so the row's own trailing
                // padding gives that much back and the two edges measure the same.
                end = ClaritySpacing.screenPadding - 8.dp,
                top = 12.dp,
                bottom = 12.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(text = item.title, style = type.body, color = colors.inkPrimary)
            // Absent entirely when there is none, and never an invitation to add one.
            item.firstStep?.let { firstStep ->
                Text(
                    text = firstStep,
                    style = type.caption,
                    // design-v3.md 3.1 and 13. The first step is the way in, 10.17,
                    // and `inkTertiary` measures 2.402 to one on this sheet against a
                    // floor of 4.5. `caption` against the title's `body` is what keeps
                    // it a rank below, which is a size and not a color.
                    color = colors.inkSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Box(
            // design-v3.md 13. A short label in a full sized target rather than a
            // target grown to fit a short label.
            modifier = Modifier
                .sizeIn(
                    minWidth = ClaritySpacing.minTouchTarget,
                    minHeight = ClaritySpacing.minTouchTarget,
                )
                .clarityClickable(onClickLabel = moveLabel, onClick = onMove)
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(R.string.action_move),
                style = type.caption,
                color = colors.actionBlue,
            )
        }
    }
}

/**
 * design-v3.md 10.13. An invitation, never a scold.
 *
 * There is no entry point to this sheet while the inbox is empty, so the only way to
 * see this is to empty it with the sheet open, which is the moment it matters most:
 * a person has just filed or deleted the last thing, and what the app says next is
 * either congratulation, which this design does not do, or a plain description of
 * what the place is for.
 *
 * The second sentence is the one that had to be written down. An inbox that nags is
 * a worse place to put a thought than a notes app, and a person who learns that
 * capture produces a scolding number stops capturing.
 */
@Composable
private fun InboxEmptyState() {
    val colors = LocalClarityColors.current
    val type = LocalClarityTypography.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = ClaritySpacing.screenPadding,
                vertical = ClaritySpacing.scaled(24.dp,
            )),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.inbox_empty_title),
            style = type.readSerif,
            color = colors.inkPrimary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(ClaritySpacing.scaled(10.dp)))
        Text(
            text = stringResource(R.string.inbox_empty_body),
            style = type.body,
            color = colors.inkSecondary,
            textAlign = TextAlign.Center,
        )
    }
}

/**
 * The area chooser for filing. design-v3.md 10.15 and 10.16, MASTER_BUILD_PROMPT
 * 14b.1.
 *
 * **One tap, one choice, never demanded and never scheduled.** Filing is a separate,
 * later, optional act, and this sheet is the whole of it.
 *
 * Each row states where the item will land before the person commits, exactly as the
 * add sheet does. Filing into an idle area promotes in the same transaction, so the
 * row says so; filing into an area that already has an active item joins the queue
 * behind it, and it says that instead. **Filing never displaces the thing a person is
 * already working on.** Writing something down can never become the most disruptive
 * act in the app.
 *
 * **The zero areas case is real and is handled here.** design-v3.md 10.15 makes zero
 * areas a supported state and 10.16 says the inbox is unaffected by it, so this sheet
 * can be opened with nothing to choose from. It offers to create an area first, and
 * the item is filed into it in the same gesture rather than being left behind in the
 * inbox while the person works out what just happened.
 */
@Composable
fun FileItemSheet(
    itemTitle: String,
    areas: List<AreaCardModel>,
    onChoose: (AreaCardModel) -> Unit,
    onCreateArea: () -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = LocalClarityColors.current
    val type = LocalClarityTypography.current

    ClaritySheet(onDismiss = onDismiss, title = stringResource(R.string.sheet_file_item_title)) {
        Column(
            modifier = Modifier
                .heightIn(max = 560.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Text(
                text = itemTitle,
                style = type.bodyStrong,
                color = colors.inkPrimary,
                modifier = Modifier.padding(horizontal = ClaritySpacing.screenPadding),
            )
            Spacer(Modifier.height(ClaritySpacing.scaled(14.dp)))

            if (areas.isEmpty()) {
                NoAreasYet(onCreateArea = onCreateArea)
            } else {
                areas.forEach { area ->
                    FileTargetRow(area = area, onClick = { onChoose(area) })
                }
                Spacer(Modifier.height(ClaritySpacing.scaled(10.dp)))
                Column(modifier = Modifier.padding(horizontal = ClaritySpacing.screenPadding)) {
                    ClarityButton(
                        label = stringResource(R.string.action_never_mind),
                        role = ClarityButtonRole.TERTIARY,
                        onClick = onDismiss,
                    )
                }
            }
        }
    }
}

/** One area, its dot, and a plain statement of where the item lands inside it. */
@Composable
private fun FileTargetRow(area: AreaCardModel, onClick: () -> Unit) {
    val colors = LocalClarityColors.current
    val type = LocalClarityTypography.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clarityClickable(onClickLabel = area.name, onClick = onClick)
            .padding(
                horizontal = ClaritySpacing.screenPadding,
                vertical = ClaritySpacing.scaled(12.dp,
            )),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
    ) {
        Box(
            modifier = Modifier
                .size(ClaritySpacing.areaDot)
                .clip(CircleShape)
                .background(parseAreaColor(area.colorHex)),
        )
        Column(modifier = Modifier.padding(start = 10.dp)) {
            Text(text = area.name, style = type.body, color = colors.inkPrimary)
            Text(
                text = stringResource(
                    if (area.isIdle) R.string.file_lands_active else R.string.file_lands_queue,
                ),
                style = type.caption,
                // This line is the whole reason the row is worth reading: it says
                // where the item goes if you tap. design-v3.md 3.1 and 13.
                color = colors.inkSecondary,
            )
        }
    }
}

/**
 * design-v3.md 10.16's zero areas case, and 10.13's rule about what an empty state
 * says. It names the next action in plain words and does not ask why there are no
 * areas.
 */
@Composable
private fun NoAreasYet(onCreateArea: () -> Unit) {
    val colors = LocalClarityColors.current
    val type = LocalClarityTypography.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = ClaritySpacing.screenPadding,
                vertical = ClaritySpacing.scaled(12.dp,
            )),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.areas_empty_body),
            style = type.body,
            color = colors.inkSecondary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(ClaritySpacing.scaled(20.dp)))
        ClarityButton(
            label = stringResource(R.string.areas_empty_action),
            onClick = onCreateArea,
            role = ClarityButtonRole.SECONDARY,
            fillWidth = false,
        )
    }
}
