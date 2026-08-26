package com.kamsiob.claritynow.ui.areas

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kamsiob.claritynow.R
import com.kamsiob.claritynow.ui.components.TabBarHeight
import com.kamsiob.claritynow.ui.components.UndoRequest
import com.kamsiob.claritynow.ui.components.UndoSnackbar
import com.kamsiob.claritynow.ui.theme.AreaPalette

/** Which secondary surface is open. Everything here is a bottom sheet. */
private sealed interface AreaSheet {
    data class Detail(val areaId: String) : AreaSheet
    data class AddItem(val areaId: String) : AreaSheet
    data class EditItem(val itemId: String) : AreaSheet
    data class EditArea(val areaId: String) : AreaSheet
    data object NewArea : AreaSheet
    data class Swap(val areaId: String) : AreaSheet
    data class DeleteArea(val areaId: String) : AreaSheet
    data class LongPressMenu(val areaId: String) : AreaSheet
}

/**
 * Owns which sheet is open and connects the screen to the view model.
 *
 * The screen itself takes plain values and callbacks, so it can be looked at in
 * isolation and has no idea a repository exists.
 */
@Composable
fun AreasRoute(
    viewModel: AreasViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val queueChoiceFor by viewModel.queueChoiceFor.collectAsStateWithLifecycle()
    var sheet by remember { mutableStateOf<AreaSheet?>(null) }
    var undo by remember { mutableStateOf<UndoRequest?>(null) }
    val scope = rememberCoroutineScope()

    val undoMessage = stringResource(R.string.undo_item_deleted)
    val undoAction = stringResource(R.string.action_undo)

    Box(modifier = modifier.fillMaxSize()) {
        AreasScreen(
            state = state,
            onOpenArea = { sheet = AreaSheet.Detail(it) },
            onOpenArchive = { /* Archived areas arrive with the archive view. */ },
            onCompleteArea = { area ->
                area.activeItemId?.let { viewModel.completeItem(area.id, it) }
            },
            onSwapArea = { sheet = AreaSheet.Swap(it.id) },
            onDeleteArea = { sheet = AreaSheet.DeleteArea(it.id) },
            onLongPressArea = { sheet = AreaSheet.Detail(it.id) },
            onMoveArea = { areaId, index -> viewModel.moveArea(areaId, index) },
            onPromotionPlayed = { viewModel.promotionPlayed(it) },
            onDismissConflict = { viewModel.dismissConflict(it) },
            onFabClick = {
                sheet = if (state.isEmpty) {
                    AreaSheet.NewArea
                } else {
                    AreaSheet.AddItem(state.areas.first().id)
                }
            },
        )

        UndoSnackbar(
            request = undo,
            onDismiss = { undo = null },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = TabBarHeight + 26.dp),
        )
    }

    when (val current = sheet) {
        null -> Unit

        is AreaSheet.Detail -> {
            val area = state.areas.firstOrNull { it.id == current.areaId }
            if (area == null) {
                sheet = null
            } else {
                AreaDetailSheet(
                    area = area,
                    active = viewModel.activeFor(area.id),
                    queue = viewModel.queueFor(area.id),
                    completed = viewModel.completedFor(area.id),
                    onAddItem = { sheet = AreaSheet.AddItem(area.id) },
                    onEditArea = { sheet = AreaSheet.EditArea(area.id) },
                    onComplete = {
                        area.activeItemId?.let { viewModel.completeItem(area.id, it) }
                        sheet = null
                    },
                    onSwap = { sheet = AreaSheet.Swap(area.id) },
                    onArchive = {
                        viewModel.archiveArea(area.id)
                        sheet = null
                    },
                    onDelete = { sheet = AreaSheet.DeleteArea(area.id) },
                    onOpenItem = { sheet = AreaSheet.EditItem(it.id) },
                    onReopenItem = { viewModel.reopenItem(it.id) },
                    onDismiss = { sheet = null },
                )
            }
        }

        is AreaSheet.AddItem -> {
            val area = state.areas.firstOrNull { it.id == current.areaId }
            if (area == null) {
                sheet = null
            } else {
                AddItemSheet(
                    areaName = area.name,
                    landsActive = viewModel.wouldBecomeActive(area.id),
                    onAdd = { title, note ->
                        viewModel.addItem(area.id, title, note)
                        sheet = null
                    },
                    onDismiss = { sheet = null },
                )
            }
        }

        is AreaSheet.EditItem -> {
            val item = viewModel.itemFor(current.itemId)
            if (item == null) {
                sheet = null
            } else {
                EditItemSheet(
                    item = item,
                    canMoveToFront = viewModel.queueFor(item.areaId).firstOrNull()?.id != item.id &&
                        viewModel.queueFor(item.areaId).any { it.id == item.id },
                    onSave = { title, note ->
                        viewModel.editItem(item.id, title, note)
                        sheet = null
                    },
                    onDelete = {
                        sheet = null
                        // Nothing is written until the window closes, so undo has
                        // nothing to compensate for and the log stays honest.
                        undo = UndoRequest(
                            id = item.id,
                            message = undoMessage,
                            actionLabel = undoAction,
                            onCommit = { viewModel.deleteItem(item.id) },
                        )
                    },
                    onMoveToFront = {
                        viewModel.moveItemToFront(item.id)
                        sheet = null
                    },
                    onDismiss = { sheet = null },
                )
            }
        }

        AreaSheet.NewArea -> AreaEditorSheet(
            initialName = "",
            initialColorHex = AreaPalette.defaultColorForIndex(viewModel.suggestedColorIndex()),
            isNew = true,
            previewItemTitle = null,
            onSave = { name, hex ->
                viewModel.createArea(name, hex)
                sheet = null
            },
            onDismiss = { sheet = null },
        )

        is AreaSheet.EditArea -> {
            val area = state.areas.firstOrNull { it.id == current.areaId }
            if (area == null) {
                sheet = null
            } else {
                AreaEditorSheet(
                    initialName = area.name,
                    initialColorHex = area.colorHex,
                    isNew = false,
                    previewItemTitle = area.activeItemTitle,
                    onSave = { name, hex ->
                        viewModel.renameArea(area.id, name)
                        viewModel.recolorArea(area.id, hex)
                        sheet = null
                    },
                    onDismiss = { sheet = null },
                )
            }
        }

        is AreaSheet.Swap -> {
            val area = state.areas.firstOrNull { it.id == current.areaId }
            if (area == null) {
                sheet = null
            } else {
                SwapChooserSheet(
                    demotedTitle = area.activeItemTitle,
                    queue = viewModel.queueFor(area.id),
                    onChoose = {
                        viewModel.swapToItem(it.id)
                        sheet = null
                    },
                    onDismiss = { sheet = null },
                )
            }
        }

        is AreaSheet.DeleteArea -> {
            val area = state.areas.firstOrNull { it.id == current.areaId }
            if (area == null) {
                sheet = null
            } else {
                DeleteAreaSheet(
                    areaName = area.name,
                    onConfirm = {
                        viewModel.deleteArea(area.id)
                        sheet = null
                    },
                    onDismiss = { sheet = null },
                )
            }
        }

        is AreaSheet.LongPressMenu -> sheet = AreaSheet.Detail(current.areaId)
    }

    queueChoiceFor?.let { areaId ->
        QueueChooserSheet(
            queue = viewModel.queueFor(areaId),
            onChoose = { viewModel.chooseFromQueue(it.id) },
            onDismiss = { viewModel.dismissQueueChoice() },
        )
    }
}
