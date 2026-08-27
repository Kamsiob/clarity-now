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

/**
 * Which secondary surface is open. Everything here is a bottom sheet.
 *
 * **[fromInbox] is a return address rather than a mode.** Editing and filing are
 * reached from two places now, an area's queue and the inbox, and dismissing either
 * has to land back where the person came from. One flag beats a navigation stack for
 * a set of sheets this small, and it beats duplicating the sheets, which is how the
 * two copies drift apart. Nothing about the sheet itself changes because of it.
 */
private sealed interface AreaSheet {
    data class Detail(val areaId: String) : AreaSheet

    /** A null area is a capture into the inbox. Addendum 01 4a. */
    data class AddItem(val areaId: String?) : AreaSheet
    data class EditItem(val itemId: String, val fromInbox: Boolean = false) : AreaSheet
    data class EditArea(val areaId: String) : AreaSheet
    data object NewArea : AreaSheet
    data class Swap(val areaId: String) : AreaSheet
    data class DeleteArea(val areaId: String) : AreaSheet
    data class LongPressMenu(val areaId: String) : AreaSheet

    /** design-v3.md 10.16. The unfiled inbox and the two sheets it leads to. */
    data object Inbox : AreaSheet
    data class FileItem(val itemId: String) : AreaSheet
    data class NewAreaForFiling(val itemId: String) : AreaSheet
}

/**
 * Owns which sheet is open and connects the screen to the view model.
 *
 * The screen itself takes plain values and callbacks, so it can be looked at in
 * isolation and has no idea a repository exists.
 *
 * **[onOpenFocus] leaves this screen rather than opening a sheet**, which is why it is
 * a parameter and not one more entry in [AreaSheet]. design-v3.md 10.15 makes the
 * Focus surface a destination reached from the Focus chip, and section 2 makes it the
 * Contemplative world: it is a room the app moves into and not a panel over this one,
 * so the shell owns it and this screen only asks.
 */
@Composable
fun AreasRoute(
    viewModel: AreasViewModel,
    onOpenFocus: () -> Unit,
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
            onOpenInbox = { sheet = AreaSheet.Inbox },
            onOpenFocus = onOpenFocus,
            // MASTER_BUILD_PROMPT 14b.1 and 8.4. At zero areas the FAB creates an
            // area, which 8.4 states and this phase does not change.
            //
            // Otherwise **the FAB captures, and capture means the inbox.** It used to
            // add into whichever area happened to sort first, which is a decision the
            // app made on the person's behalf and got right only by accident. A null
            // area is the honest version of the same gesture: the thought is recorded,
            // nothing is guessed, and the destination is chosen later or never.
            // Adding straight into a known area is still one tap away, from that
            // area's own detail sheet, where the area is context rather than a choice.
            // Recorded in `DECISIONS.md`.
            onFabClick = {
                sheet = if (state.isEmpty) AreaSheet.NewArea else AreaSheet.AddItem(areaId = null)
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
            val areaId = current.areaId
            val area = areaId?.let { id -> state.areas.firstOrNull { it.id == id } }
            // An area that vanished under an open sheet is a dismissal. A capture with
            // no area is not: it has nothing to lose.
            if (areaId != null && area == null) {
                sheet = null
            } else {
                AddItemSheet(
                    areaName = area?.name,
                    landsActive = area != null && viewModel.wouldBecomeActive(area.id),
                    onAdd = { title, note, firstStep, estimateMinutes ->
                        viewModel.addItem(area?.id, title, note, firstStep, estimateMinutes)
                        sheet = null
                    },
                    onDismiss = { sheet = null },
                )
            }
        }

        is AreaSheet.EditItem -> {
            val item = viewModel.itemFor(current.itemId)
            val back = if (current.fromInbox) AreaSheet.Inbox else null
            if (item == null) {
                sheet = back
            } else {
                // An unfiled item has no area and therefore no queue to move to the
                // front of. Addendum 01 4a: it can be filed, edited or deleted, and
                // nothing else, until it is filed.
                val queue = item.areaId?.let { viewModel.queueFor(it) }.orEmpty()
                EditItemSheet(
                    item = item,
                    canMoveToFront = queue.firstOrNull()?.id != item.id &&
                        queue.any { it.id == item.id },
                    onSave = { title, note, firstStep, estimateMinutes ->
                        viewModel.saveItem(item.id, title, note, firstStep, estimateMinutes)
                        sheet = back
                    },
                    onDelete = {
                        sheet = back
                        // Nothing is written until the window closes, so undo has
                        // nothing to compensate for and the log stays honest. This is
                        // the delete an unfiled item gets too, per Addendum 01 4a:
                        // the same five second window as anywhere else.
                        undo = UndoRequest(
                            id = item.id,
                            message = undoMessage,
                            actionLabel = undoAction,
                            onCommit = { viewModel.deleteItem(item.id) },
                        )
                    },
                    onMoveToFront = {
                        viewModel.moveItemToFront(item.id)
                        sheet = back
                    },
                    onDismiss = { sheet = back },
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

        AreaSheet.Inbox -> InboxSheet(
            items = viewModel.inboxItems(),
            onOpenItem = { sheet = AreaSheet.EditItem(it.id, fromInbox = true) },
            onMoveItem = { sheet = AreaSheet.FileItem(it.id) },
            onDismiss = { sheet = null },
        )

        is AreaSheet.FileItem -> {
            val item = viewModel.itemFor(current.itemId)
            // Already filed, or deleted under the sheet. Either way the inbox behind
            // this is the right place to land, and it is where the person was.
            if (item == null || item.areaId != null) {
                sheet = AreaSheet.Inbox
            } else {
                FileItemSheet(
                    itemTitle = item.title,
                    areas = state.areas,
                    onChoose = { area ->
                        viewModel.fileItem(item.id, area.id)
                        sheet = AreaSheet.Inbox
                    },
                    onCreateArea = { sheet = AreaSheet.NewAreaForFiling(item.id) },
                    onDismiss = { sheet = AreaSheet.Inbox },
                )
            }
        }

        // design-v3.md 10.16's zero areas case. The same editor a new area always
        // uses, and the item is filed into what it creates in the same gesture.
        is AreaSheet.NewAreaForFiling -> AreaEditorSheet(
            initialName = "",
            initialColorHex = AreaPalette.defaultColorForIndex(viewModel.suggestedColorIndex()),
            isNew = true,
            previewItemTitle = viewModel.itemFor(current.itemId)?.title,
            onSave = { name, hex ->
                viewModel.createAreaAndFile(current.itemId, name, hex)
                sheet = AreaSheet.Inbox
            },
            onDismiss = { sheet = AreaSheet.Inbox },
        )
    }

    queueChoiceFor?.let { areaId ->
        QueueChooserSheet(
            queue = viewModel.queueFor(areaId),
            onChoose = { viewModel.chooseFromQueue(it.id) },
            onDismiss = { viewModel.dismissQueueChoice() },
        )
    }
}
