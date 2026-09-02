package com.kamsiob.claritynow.ui.areas

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kamsiob.claritynow.R
import com.kamsiob.claritynow.ui.components.predictiveBackPreview
import com.kamsiob.claritynow.ui.components.rememberPredictiveBack
import com.kamsiob.claritynow.ui.components.ClarityButton
import com.kamsiob.claritynow.ui.components.ClarityButtonRole
import com.kamsiob.claritynow.ui.components.ClarityCard
import com.kamsiob.claritynow.ui.components.clarityClickable
import com.kamsiob.claritynow.ui.settings.PushedScreen
import com.kamsiob.claritynow.ui.theme.ClarityHapticEvent
import com.kamsiob.claritynow.ui.theme.ClaritySpacing
import com.kamsiob.claritynow.ui.theme.LocalClarityColors
import com.kamsiob.claritynow.ui.theme.LocalClarityTypography
import com.kamsiob.claritynow.ui.theme.parseAreaColor

/**
 * The archive. design-v3.md 10.20 and 10.15's destination table, issue #15.
 *
 * **This screen is the undo for archiving**, and until it existed there was none.
 * Archiving is offered on the area detail sheet one row above Delete, it takes one tap,
 * it takes the whole queue with it, and the only way back was to export the log, edit
 * JSON by hand and import it again. `MASTER_BUILD_PROMPT.md` section 5 has always said
 * the archive view "allows unarchiving or permanent deletion with a typed
 * confirmation"; this is that view.
 *
 * **There is deliberately no undo snackbar on archiving**, which is the other way the
 * gap could have been closed and is the weaker one. A five second window is an undo for
 * a slip noticed inside five seconds. This screen is an undo with no expiry, it is
 * reachable from the Areas header on any day, and it does not put a compensating pair
 * of events in the Trail every time somebody changes their mind. Two mechanisms for one
 * job would also mean two, and the rule this app applies to a delete, design-v3.md
 * 10.14 and the deliberate tap in `CLAUDE.md`, is satisfied by the durable one.
 *
 * The delete offered here is the same [DeleteAreaSheet] the Areas list offers, typed
 * confirmation and all, rather than a second confirmation written for this screen. One
 * sentence about what a delete does, in one place.
 */
@Composable
fun ArchiveRoute(
    viewModel: AreasViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val areas by viewModel.archivedAreas.collectAsStateWithLifecycle()
    var deleting by remember { mutableStateOf<String?>(null) }

    // A pushed screen is left by back, design-v3.md 10.15. The shell's own handler is
    // disabled on the Areas tab, and a sheet is a window of its own that consumes back
    // before this ever sees it, so this is the only handler in play.
    //
    // Predictive, issue #63. This screen is drawn over the Areas tab rather than in place
    // of it, so the room the preview uncovers is the room back arrives in.
    val predictiveBack = rememberPredictiveBack(onBack = onBack)

    ArchiveScreen(
        areas = areas,
        onRestore = { viewModel.unarchiveArea(it.id) },
        onDelete = { deleting = it.id },
        onBack = onBack,
        modifier = modifier.predictiveBackPreview(predictiveBack),
    )

    // Read back out of the list rather than held as a value, so an area deleted or
    // restored underneath the sheet closes it instead of confirming against a name that
    // is no longer there.
    val target = areas?.firstOrNull { it.id == deleting }
    if (deleting != null && target == null) {
        deleting = null
    } else if (target != null) {
        DeleteAreaSheet(
            areaName = target.name,
            onConfirm = {
                viewModel.deleteArea(target.id)
                deleting = null
            },
            onDismiss = { deleting = null },
        )
    }
}

/**
 * design-v3.md 10.20. The list, its empty state, and nothing that reads the log.
 *
 * Plain values and callbacks, like [AreasScreen], so the surface can be looked at on
 * its own and has no idea a repository exists.
 */
@Composable
fun ArchiveScreen(
    areas: List<ArchivedAreaModel>?,
    onRestore: (ArchivedAreaModel) -> Unit,
    onDelete: (ArchivedAreaModel) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PushedScreen(
        title = stringResource(R.string.archive_title),
        onBack = onBack,
        modifier = modifier,
    ) {
        // Null is the answer not being in yet, and the screen says nothing at all until
        // it is. `AreasViewModel.archivedAreas` carries why that is a separate state
        // from an empty archive rather than the same one: an empty state is a claim, and
        // `Nothing archived` drawn for a frame in front of somebody who is here to find
        // an area is the one wrong sentence this screen could say.
        when {
            areas == null -> Unit
            areas.isEmpty() -> ArchiveEmptyState()
            else -> areas.forEachIndexed { index, area ->
                if (index > 0) Spacer(Modifier.height(ClaritySpacing.scaled(11.dp)))
                ArchivedAreaCard(
                    area = area,
                    onRestore = { onRestore(area) },
                    onDelete = { onDelete(area) },
                )
            }
        }
    }
}

/**
 * design-v3.md 10.13. An invitation, never a scold. No illustration, no mascot.
 *
 * **An empty archive is the ordinary state and this screen says so**, which is the whole
 * of what 10.13 asks for here. The obvious empty state names the next action, the way
 * `areas_empty_action` puts a Create an area button under the Areas one, and there is no
 * such action to name: an app that invited somebody to go and archive something would be
 * inviting them to put a part of their life away. So the invitation is the reassurance
 * instead, which is what a person opening an empty archive actually needs to read, and
 * it is the sentence that would have saved the person who found issue #15.
 *
 * No button, and the way out is the back glyph the screen already carries.
 */
@Composable
private fun ArchiveEmptyState() {
    val colors = LocalClarityColors.current
    val type = LocalClarityTypography.current
    Column(modifier = Modifier.fillMaxWidth().padding(top = ClaritySpacing.scaled(40.dp))) {
        Text(
            text = stringResource(R.string.archive_empty_title),
            style = type.readSerif,
            color = colors.inkPrimary,
        )
        Spacer(Modifier.height(ClaritySpacing.scaled(10.dp)))
        Text(
            text = stringResource(R.string.archive_empty_body),
            style = type.body,
            color = colors.inkSecondary,
        )
    }
}

/**
 * One archived area. design-v3.md 10.20 and 10.3.
 *
 * **The name takes the subject rank here and it does not on the Areas screen**, which is
 * the one deliberate difference between this card and that one. 10.3 draws the area name
 * small and in the area's own label color, because on that screen the subject is the item
 * and the area is the context around it. Nothing is happening in an archived area, so
 * there is no item to be the subject and the area is one: it takes `itemTitle` in
 * inkPrimary, and the 7dp dot goes on carrying identity beside it. That also keeps the
 * area label color, which 3.4 pins against the wash it is drawn on, off a card that has
 * no wash.
 *
 * **No wash, no stripe and no edge.** The card is idle by definition and an idle card
 * carries no wash on the Areas screen either, 3.4. The one separation device is the
 * card, 6.1.
 *
 * **Both actions are written out rather than hidden behind the row.** The statistically
 * common archive row is a tap that opens something, or a trailing icon, or a swipe, and
 * design-v3.md 15 makes each of those a reason to look twice. All three are refused, and
 * the same fact refuses them: the person most likely to be on this screen archived
 * something by accident and is looking for the way back. A labeled `Restore` they can
 * read is worth more than a gesture they have to know, an unlabeled glyph they have to
 * interpret, or one more surface between them and their area. Swipe is refused twice
 * over, because a delete is on this row and a full swipe never commits one.
 *
 * **Restore is the weightier of the two** and delete is a quiet label at the delete ink,
 * which is the same treatment the detail sheet gives the pair. The permanent delete is
 * still two deliberate taps and a typed word away, so nothing rests on its weight.
 */
@Composable
private fun ArchivedAreaCard(
    area: ArchivedAreaModel,
    onRestore: () -> Unit,
    onDelete: () -> Unit,
) {
    val colors = LocalClarityColors.current
    val type = LocalClarityTypography.current
    val accent = parseAreaColor(area.colorHex)

    ClarityCard(modifier = Modifier.fillMaxWidth(), colors = colors) {
        Column(
            modifier = Modifier.padding(
                horizontal = ClaritySpacing.cardPaddingHorizontal,
                vertical = ClaritySpacing.scaled(16.dp),
            ),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(ClaritySpacing.areaDot)
                        .clip(CircleShape)
                        // The 45 percent an idle card's dot takes, 10.3. An archived
                        // area is idle by construction, so it takes the idle dot.
                        .background(accent.copy(alpha = 0.45f)),
                )
                Text(
                    text = area.name,
                    style = type.itemTitle,
                    color = colors.inkPrimary,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }

            Spacer(Modifier.height(ClaritySpacing.scaled(4.dp)))
            Text(
                // A count query and nothing read into it. What is inside is a number a
                // person needs before deleting something permanently; how long it has
                // been in here, or what that says about them, is an observation and
                // comes from a corpus through the engine or from nowhere at all.
                text = if (area.itemCount == 0) {
                    stringResource(R.string.archive_area_no_items)
                } else {
                    pluralStringResource(
                        R.plurals.archive_area_items,
                        area.itemCount,
                        area.itemCount,
                    )
                },
                style = type.caption,
                color = colors.inkSecondary,
                modifier = Modifier.padding(start = ClaritySpacing.areaDot + 8.dp),
            )

            Spacer(Modifier.height(ClaritySpacing.scaled(14.dp)))
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // **Both controls name their area to a screen reader**, which the
                // visible labels do not have to do and a screen reader cannot do
                // without. Reading this card in order supplies the name once, but
                // TalkBack can also step through controls alone, and a list of
                // `Restore, button` and `Delete, button` with nothing between them is a
                // permanent delete offered without saying what it deletes.
                val restoreDescription = stringResource(R.string.cd_archive_restore, area.name)
                val deleteDescription = stringResource(R.string.cd_archive_delete, area.name)
                ClarityButton(
                    label = stringResource(R.string.action_restore),
                    role = ClarityButtonRole.SECONDARY,
                    onClick = onRestore,
                    fillWidth = false,
                    modifier = Modifier
                        .weight(1f)
                        .semantics { contentDescription = restoreDescription },
                )
                QuietDeleteAction(
                    label = stringResource(R.string.action_delete),
                    description = deleteDescription,
                    onClick = onDelete,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

/**
 * A delete that is present without being loud.
 *
 * **It is written here rather than added to [ClarityButtonRole]**, and the reason is
 * that it is not a new kind of button. `DESTRUCTIVE` is the solid inkPrimary pill the
 * confirmation sheet ends with, which is right for the last tap of a delete and wrong
 * for one row of a list; `TERTIARY` is the same transparent shape as this but reads its
 * label in actionBlue, which is the app's color for the safe action. This is `TERTIARY`
 * with the delete ink the detail sheet's own delete row already takes, at the same
 * height, radius and label style every other button on the screen has, so it is that
 * component's geometry with one color changed rather than a second button in the app.
 *
 * It carries the warn haptic, which is what `DESTRUCTIVE` carries and what section 9
 * gives a destructive control.
 */
@Composable
private fun QuietDeleteAction(
    label: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalClarityColors.current
    val type = LocalClarityTypography.current
    val interaction = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            // design-v3.md 10.7's button height, as a minimum and scaled, exactly as
            // ClarityButton takes it: the label has no maxLines and a larger text
            // setting has to be able to push it taller rather than be clipped.
            .heightIn(min = ClaritySpacing.scaled(50.dp))
            .clip(RoundedCornerShape(12.dp))
            .semantics { contentDescription = description }
            .clarityClickable(
                interactionSource = interaction,
                haptic = ClarityHapticEvent.WARN,
                role = Role.Button,
                onClickLabel = label,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = type.bodyStrong,
            color = colors.deleteMuted,
            modifier = Modifier.padding(horizontal = 20.dp),
        )
    }
}
