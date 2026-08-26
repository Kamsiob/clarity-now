package com.kamsiob.claritynow.ui.areas

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.kamsiob.claritynow.R
import com.kamsiob.claritynow.domain.replay.ItemState
import com.kamsiob.claritynow.ui.components.ClarityButton
import com.kamsiob.claritynow.ui.components.ClarityButtonRole
import com.kamsiob.claritynow.ui.components.ClarityIcon
import com.kamsiob.claritynow.ui.components.ClarityIcons
import com.kamsiob.claritynow.ui.components.ClaritySheet
import com.kamsiob.claritynow.ui.components.ClarityTextField
import com.kamsiob.claritynow.ui.components.Sidehead
import com.kamsiob.claritynow.ui.components.clarityClickable
import com.kamsiob.claritynow.ui.theme.ClarityHapticEvent
import com.kamsiob.claritynow.ui.theme.LocalClarityColors
import com.kamsiob.claritynow.ui.theme.LocalClarityTypography
import com.kamsiob.claritynow.ui.theme.clarityMotion
import com.kamsiob.claritynow.ui.theme.parseAreaColor

private val SheetPadding = 20.dp

// Add an item ----------------------------------------------------------------

/**
 * The add sheet states where the item will land before the person commits, because
 * an item that silently becomes active is a surprise and an item that silently
 * joins a queue is a different surprise.
 */
@Composable
fun AddItemSheet(
    areaName: String,
    landsActive: Boolean,
    onAdd: (String, String?) -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = LocalClarityColors.current
    val type = LocalClarityTypography.current
    var title by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    val focus = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) { focus.requestFocus() }

    ClaritySheet(onDismiss = onDismiss, title = stringResource(R.string.sheet_add_item_title)) {
        Column(modifier = Modifier.padding(horizontal = SheetPadding)) {
            Text(text = areaName, style = type.label, color = colors.inkSecondary)
            Spacer(Modifier.height(16.dp))
            ClarityTextField(
                value = title,
                onValueChange = { title = it },
                label = stringResource(R.string.field_title),
                focusRequester = focus,
                imeAction = ImeAction.Next,
            )
            Spacer(Modifier.height(20.dp))
            ClarityTextField(
                value = note,
                onValueChange = { note = it },
                label = stringResource(R.string.field_note_optional),
                singleLine = false,
            )
            Spacer(Modifier.height(18.dp))
            Text(
                text = stringResource(
                    if (landsActive) R.string.add_item_lands_active else R.string.add_item_lands_queue,
                ),
                style = type.caption,
                color = colors.inkTertiary,
            )
            Spacer(Modifier.height(22.dp))
            ClarityButton(
                label = stringResource(R.string.action_add),
                enabled = title.isNotBlank(),
                onClick = {
                    keyboard?.hide()
                    onAdd(title, note.ifBlank { null })
                },
            )
        }
    }
}

// Edit a queued or active item ------------------------------------------------

/**
 * design-v3.md 10.15. A queued item is tappable and this is the only way to edit
 * one. Without it the queue is read only, which nobody expects.
 */
@Composable
fun EditItemSheet(
    item: ItemState,
    canMoveToFront: Boolean,
    onSave: (String, String?) -> Unit,
    onDelete: () -> Unit,
    onMoveToFront: () -> Unit,
    onDismiss: () -> Unit,
) {
    var title by remember(item.id) { mutableStateOf(item.title) }
    var note by remember(item.id) { mutableStateOf(item.note.orEmpty()) }
    val keyboard = LocalSoftwareKeyboardController.current

    ClaritySheet(onDismiss = onDismiss, title = stringResource(R.string.sheet_edit_item_title)) {
        Column(modifier = Modifier.padding(horizontal = SheetPadding)) {
            ClarityTextField(
                value = title,
                onValueChange = { title = it },
                label = stringResource(R.string.field_title),
                imeAction = ImeAction.Next,
            )
            Spacer(Modifier.height(20.dp))
            ClarityTextField(
                value = note,
                onValueChange = { note = it },
                label = stringResource(R.string.field_note_optional),
                singleLine = false,
            )
            Spacer(Modifier.height(24.dp))

            if (canMoveToFront) {
                SheetActionRow(
                    icon = ClarityIcons.moveToFront,
                    label = stringResource(R.string.action_move_to_front),
                    onClick = onMoveToFront,
                )
            }
            SheetActionRow(
                icon = ClarityIcons.deleteSwipe,
                label = stringResource(R.string.action_delete),
                tint = LocalClarityColors.current.deleteMuted,
                onClick = onDelete,
            )

            Spacer(Modifier.height(20.dp))
            ClarityButton(
                label = stringResource(R.string.action_save),
                enabled = title.isNotBlank(),
                onClick = {
                    keyboard?.hide()
                    onSave(title, note.ifBlank { null })
                },
            )
        }
    }
}

// The area detail sheet -------------------------------------------------------

/**
 * Everything an area can do, reachable without a swipe. design-v3.md 10.3.1 makes
 * this mandatory rather than a convenience: swipe is invisible to a screen reader
 * and is only ever an accelerator.
 */
@Composable
fun AreaDetailSheet(
    area: AreaCardModel,
    active: ItemState?,
    queue: List<ItemState>,
    completed: List<ItemState>,
    onAddItem: () -> Unit,
    onEditArea: () -> Unit,
    onComplete: () -> Unit,
    onSwap: () -> Unit,
    onArchive: () -> Unit,
    onDelete: () -> Unit,
    onOpenItem: (ItemState) -> Unit,
    onReopenItem: (ItemState) -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = LocalClarityColors.current
    val type = LocalClarityTypography.current
    val motion = clarityMotion()
    val accent = parseAreaColor(area.colorHex)
    var showCompleted by remember { mutableStateOf(false) }

    ClaritySheet(onDismiss = onDismiss) {
        Column(
            modifier = Modifier
                .heightIn(max = 620.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = SheetPadding),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(9.dp).clip(CircleShape).background(accent))
                    Text(
                        text = area.name,
                        style = type.title,
                        color = colors.inkPrimary,
                        modifier = Modifier.padding(start = 10.dp),
                    )
                }
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .clarityClickable(onClickLabel = stringResource(R.string.cd_edit_area)) { onEditArea() },
                    contentAlignment = Alignment.Center,
                ) {
                    ClarityIcon(
                        icon = ClarityIcons.editArea,
                        contentDescription = stringResource(R.string.cd_edit_area),
                        tint = colors.inkSecondary,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            Spacer(Modifier.height(18.dp))
            Sidehead(
                text = stringResource(R.string.sidehead_active),
                modifier = Modifier.padding(horizontal = SheetPadding),
            )
            Spacer(Modifier.height(10.dp))
            if (active == null) {
                Text(
                    text = stringResource(R.string.area_idle_title),
                    style = type.itemTitle,
                    color = colors.inkTertiary,
                    modifier = Modifier.padding(horizontal = SheetPadding),
                )
            } else {
                Text(
                    text = active.title,
                    style = type.itemTitle,
                    color = colors.inkPrimary,
                    modifier = Modifier
                        .padding(horizontal = SheetPadding)
                        .clarityClickable { onOpenItem(active) },
                )
                active.note?.let { note ->
                    Text(
                        text = note,
                        style = type.body,
                        color = colors.inkSecondary,
                        modifier = Modifier.padding(horizontal = SheetPadding, vertical = 6.dp),
                    )
                }
                Spacer(Modifier.height(14.dp))
                Row(
                    modifier = Modifier.padding(horizontal = SheetPadding),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    ClarityButton(
                        label = stringResource(R.string.action_complete),
                        role = ClarityButtonRole.POSITIVE,
                        onClick = onComplete,
                        fillWidth = false,
                        modifier = Modifier.weight(1f),
                    )
                    if (area.offersSwap) {
                        ClarityButton(
                            label = stringResource(R.string.action_swap),
                            role = ClarityButtonRole.SECONDARY,
                            onClick = onSwap,
                            fillWidth = false,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            Spacer(Modifier.height(26.dp))
            Sidehead(
                text = stringResource(R.string.sidehead_queue),
                modifier = Modifier.padding(horizontal = SheetPadding),
            )
            Spacer(Modifier.height(6.dp))
            if (queue.isEmpty()) {
                Text(
                    text = stringResource(R.string.queue_empty),
                    style = type.body,
                    color = colors.inkTertiary,
                    modifier = Modifier.padding(horizontal = SheetPadding, vertical = 8.dp),
                )
            } else {
                queue.forEach { item ->
                    QueueRow(item = item, onClick = { onOpenItem(item) })
                }
            }

            Spacer(Modifier.height(10.dp))
            SheetActionRow(
                icon = ClarityIcons.add,
                label = stringResource(R.string.area_add_item),
                tint = colors.actionBlue,
                onClick = onAddItem,
            )

            if (completed.isNotEmpty()) {
                Spacer(Modifier.height(18.dp))
                val chevronRotation by animateFloatAsState(
                    targetValue = if (showCompleted) 180f else 0f,
                    animationSpec = motion.springStandard(),
                    label = "completedChevron",
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clarityClickable { showCompleted = !showCompleted }
                        .padding(horizontal = SheetPadding, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.sidehead_completed),
                        style = type.sidehead,
                        color = colors.inkSecondary,
                    )
                    Spacer(Modifier.size(8.dp))
                    Text(
                        text = completed.size.toString(),
                        style = type.caption,
                        color = colors.inkTertiary,
                    )
                    Spacer(Modifier.weight(1f))
                    ClarityIcon(
                        icon = ClarityIcons.expand,
                        contentDescription = null,
                        tint = colors.inkTertiary,
                        modifier = Modifier.size(20.dp).rotate(chevronRotation),
                    )
                }
                AnimatedVisibility(
                    visible = showCompleted,
                    enter = fadeIn(motion.easeOut()) + expandVertically(motion.springGentle()),
                    exit = fadeOut(motion.easeOut()) + shrinkVertically(motion.springGentle()),
                ) {
                    Column {
                        completed.forEach { item ->
                            CompletedRow(item = item, onReopen = { onReopenItem(item) })
                        }
                    }
                }
            }

            Spacer(Modifier.height(22.dp))
            Sidehead(
                text = stringResource(R.string.sidehead_area_actions),
                modifier = Modifier.padding(horizontal = SheetPadding),
            )
            Spacer(Modifier.height(4.dp))
            SheetActionRow(
                icon = ClarityIcons.archive,
                label = stringResource(R.string.action_archive),
                onClick = onArchive,
            )
            SheetActionRow(
                icon = ClarityIcons.deleteSwipe,
                label = stringResource(R.string.action_delete),
                tint = colors.deleteMuted,
                onClick = onDelete,
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun QueueRow(item: ItemState, onClick: () -> Unit) {
    val colors = LocalClarityColors.current
    val type = LocalClarityTypography.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clarityClickable(onClickLabel = item.title, onClick = onClick)
            // The trailing chevron's viewBox carries its own whitespace, so the
            // trailing padding is reduced to match what the eye measures.
            .padding(start = SheetPadding, end = SheetPadding - 3.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(text = item.title, style = type.body, color = colors.inkPrimary)
            item.note?.let {
                Text(text = it, style = type.caption, color = colors.inkTertiary)
            }
        }
        ClarityIcon(
            icon = ClarityIcons.chevron,
            contentDescription = null,
            tint = colors.inkTertiary,
            modifier = Modifier.size(18.dp),
        )
    }
}

@Composable
private fun CompletedRow(item: ItemState, onReopen: () -> Unit) {
    val colors = LocalClarityColors.current
    val type = LocalClarityTypography.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = SheetPadding, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ClarityIcon(
            icon = ClarityIcons.completed,
            contentDescription = null,
            tint = colors.positiveGreen,
            modifier = Modifier.size(17.dp),
        )
        Text(
            text = item.title,
            style = type.body.copy(textDecoration = TextDecoration.LineThrough),
            color = colors.inkTertiary,
            modifier = Modifier.weight(1f).padding(start = 10.dp),
        )
        Text(
            text = stringResource(R.string.action_reopen),
            style = type.caption,
            color = colors.actionBlue,
            modifier = Modifier.clarityClickable(onClick = onReopen),
        )
    }
}

@Composable
private fun SheetActionRow(
    icon: Int,
    label: String,
    onClick: () -> Unit,
    tint: Color = LocalClarityColors.current.inkSecondary,
) {
    val colors = LocalClarityColors.current
    val type = LocalClarityTypography.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clarityClickable(onClickLabel = label, onClick = onClick)
            .padding(horizontal = SheetPadding, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ClarityIcon(icon = icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
        Text(
            text = label,
            style = type.body,
            color = if (tint == colors.inkSecondary) colors.inkPrimary else tint,
            modifier = Modifier.padding(start = 14.dp),
        )
    }
}

// Area editor -----------------------------------------------------------------

@Composable
fun AreaEditorSheet(
    initialName: String,
    initialColorHex: String,
    isNew: Boolean,
    previewItemTitle: String?,
    onSave: (String, String) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf(initialName) }
    var hex by remember { mutableStateOf(initialColorHex) }
    val keyboard = LocalSoftwareKeyboardController.current
    val nameFocus = remember { FocusRequester() }

    // A new area opens on its name, an existing one does not, because reopening an
    // editor with the keyboard up hides the thing being edited.
    LaunchedEffect(isNew) { if (isNew) nameFocus.requestFocus() }

    ClaritySheet(
        onDismiss = onDismiss,
        title = stringResource(if (isNew) R.string.sheet_new_area_title else R.string.sheet_edit_area_title),
    ) {
        Column(
            modifier = Modifier
                .heightIn(max = 640.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Column(modifier = Modifier.padding(horizontal = SheetPadding)) {
                ClarityTextField(
                    value = name,
                    onValueChange = { if (it.length <= 40) name = it },
                    label = stringResource(R.string.field_area_name),
                    focusRequester = nameFocus,
                )
            }
            Spacer(Modifier.height(26.dp))
            AreaColorPicker(
                areaName = name,
                selectedHex = hex,
                onSelect = { hex = it },
                previewItemTitle = previewItemTitle,
            )
            Spacer(Modifier.height(20.dp))
            Column(modifier = Modifier.padding(horizontal = SheetPadding)) {
                ClarityButton(
                    label = stringResource(R.string.action_save),
                    enabled = name.isNotBlank(),
                    onClick = {
                        keyboard?.hide()
                        onSave(name, hex)
                    },
                )
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

// Swap and queue choosers -----------------------------------------------------

/** Names the item being demoted, so nothing disappears silently. No warning tone. */
@Composable
fun SwapChooserSheet(
    demotedTitle: String?,
    queue: List<ItemState>,
    onChoose: (ItemState) -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = LocalClarityColors.current
    val type = LocalClarityTypography.current

    ClaritySheet(onDismiss = onDismiss, title = stringResource(R.string.sheet_swap_title)) {
        Column {
            if (demotedTitle != null) {
                Column(modifier = Modifier.padding(horizontal = SheetPadding)) {
                    Text(text = demotedTitle, style = type.bodyStrong, color = colors.inkPrimary)
                    Text(
                        text = stringResource(R.string.swap_demoting),
                        style = type.caption,
                        color = colors.inkTertiary,
                    )
                }
                Spacer(Modifier.height(18.dp))
            }
            queue.forEach { item ->
                QueueRow(item = item, onClick = { onChoose(item) })
            }
            Spacer(Modifier.height(10.dp))
            Column(modifier = Modifier.padding(horizontal = SheetPadding)) {
                ClarityButton(
                    label = stringResource(R.string.action_never_mind),
                    role = ClarityButtonRole.TERTIARY,
                    onClick = onDismiss,
                )
            }
        }
    }
}

/** Dismissing leaves the area idle, which is a supported state and not a failure. */
@Composable
fun QueueChooserSheet(
    queue: List<ItemState>,
    onChoose: (ItemState) -> Unit,
    onDismiss: () -> Unit,
) {
    ClaritySheet(onDismiss = onDismiss, title = stringResource(R.string.sheet_queue_choice_title)) {
        Column {
            queue.forEach { item ->
                QueueRow(item = item, onClick = { onChoose(item) })
            }
            Spacer(Modifier.height(10.dp))
            Column(modifier = Modifier.padding(horizontal = SheetPadding)) {
                ClarityButton(
                    label = stringResource(R.string.queue_choice_dismiss),
                    role = ClarityButtonRole.TERTIARY,
                    onClick = onDismiss,
                )
            }
        }
    }
}

// Delete an area --------------------------------------------------------------

/**
 * A typed confirmation rather than an undo, because an area carries everything
 * inside it and a five second window is not a fair amount of time to notice.
 */
@Composable
fun DeleteAreaSheet(
    areaName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = LocalClarityColors.current
    val type = LocalClarityTypography.current
    var typed by remember { mutableStateOf("") }
    val required = stringResource(R.string.delete_area_confirm_word)
    val armed = typed.trim().equals(required, ignoreCase = false)
    val haptics = com.kamsiob.claritynow.ui.theme.LocalClarityHaptics.current

    LaunchedEffect(armed) { if (armed) haptics.perform(ClarityHapticEvent.WARN) }

    ClaritySheet(onDismiss = onDismiss) {
        Column(modifier = Modifier.padding(horizontal = SheetPadding)) {
            Text(
                text = stringResource(R.string.sheet_delete_area_title),
                style = type.readSerif,
                color = colors.inkPrimary,
            )
            Spacer(Modifier.height(12.dp))
            Text(text = areaName, style = type.bodyStrong, color = colors.inkSecondary)
            Spacer(Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.delete_area_body),
                style = type.body,
                color = colors.inkSecondary,
            )
            Spacer(Modifier.height(22.dp))
            ClarityTextField(
                value = typed,
                onValueChange = { typed = it },
                label = stringResource(R.string.delete_area_confirm_hint),
            )
            Spacer(Modifier.height(22.dp))
            ClarityButton(
                label = stringResource(R.string.action_delete),
                role = ClarityButtonRole.DESTRUCTIVE,
                enabled = armed,
                onClick = onConfirm,
            )
            Spacer(Modifier.height(8.dp))
            ClarityButton(
                label = stringResource(R.string.delete_area_keep),
                role = ClarityButtonRole.TERTIARY,
                onClick = onDismiss,
            )
        }
    }
}
