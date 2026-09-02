package com.kamsiob.claritynow.ui.areas

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.kamsiob.claritynow.ui.nav.swallowsPointerInput
import com.kamsiob.claritynow.ui.theme.AreaPalette
import kotlinx.coroutines.flow.first

/**
 * What something outside the composition has asked this tab to open.
 *
 * Two of the six destinations in `ui/nav/ExternalRequest.kt` land on this screen: a
 * `Next Up` or `All Areas` tap, which opens one area's detail sheet, and a `Quick
 * Capture` tap or the matching app shortcut, which opens the add sheet with no area.
 * `design-v3.md` 12.2 and `MASTER_BUILD_PROMPT.md` 13.3.
 *
 * **[serial] is carried down rather than a flag, and this route remembers the last one
 * it acted on rather than clearing anything.** The shell holds one number that only goes
 * up; this tab holds the one it has already answered. Both directions matter and the
 * ordinary flag fails one of them: a flag cleared by whoever acted on it re-opens a
 * sheet in the frame before it is cleared, and a flag that is never cleared re-opens it
 * every time this tab is composed, which is every switch back from Momentum. A number
 * compared against a number does neither.
 */
@Immutable
data class AreasRequest(val serial: Long, val target: AreasTarget)

/** The two things a tap outside the app can ask this screen for. */
@Immutable
sealed interface AreasTarget {

    /** One area, opened. */
    data class Detail(val areaId: String) : AreasTarget

    /** Capture into the unfiled inbox, with no area to choose. Addendum 01 4a. */
    data object Capture : AreasTarget
}

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
 *
 * **[onOpenPulse] is the same arrangement for the same reason**, and it has no default.
 * The Pulse is the second Contemplative surface, design-v3.md 2 and 11, so the shell
 * owns it too. A default of `{}` here would compile everywhere and ship a chip that
 * opens nothing, which is the exact thing phase 2 refused to do when it left this chip
 * out rather than putting an inert one in the header. Leaving it required means the
 * app does not build until the chip has somewhere to go.
 *
 * **[request] is a widget or a shortcut, and it has no default for the same reason.**
 * `MASTER_BUILD_PROMPT.md` 13.3 has a `Next Up` tap open that area and a `Quick Capture`
 * tap open the inbox, and both of those are a sheet in this file. A default of null here
 * would compile at the one call site there is and ship six widgets that open the app at
 * whatever tab it was left on, which is precisely the state phase 12 left behind.
 */
@Composable
fun AreasRoute(
    viewModel: AreasViewModel,
    request: AreasRequest?,
    onOpenFocus: () -> Unit,
    onOpenPulse: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val queueChoiceFor by viewModel.queueChoiceFor.collectAsStateWithLifecycle()
    var sheet by remember { mutableStateOf<AreaSheet?>(null) }
    var undo by remember { mutableStateOf<UndoRequest?>(null) }

    // The archive, design-v3.md 10.20 and 10.15, issue #15.
    //
    // **A screen and not an [AreaSheet]**, which is why it is a value of its own here:
    // 10.15's destination table gives `Archived areas` back as its only way out, which
    // is what marks a pushed screen in that table, and the sheets around it are left by
    // a drag or a scrim tap as well. It is hosted from this file rather than from the
    // shell because `PushedScreen` declares itself to `ui/nav/PushedScreens.kt` and the
    // shell reads that and stops drawing the tab bar, so a pushed screen does not have
    // to be hoisted to be able to cover one. Settings is the same arrangement one file
    // down, in `AreasScreen`, where the glyph that opens it is.
    //
    // `rememberSaveable`, for the reason `settingsOpen` is: a tab switch takes this
    // composition with it, and a screen that closed itself while somebody was reading
    // the Report is a screen that cannot be trusted.
    var archiveOpen by rememberSaveable { mutableStateOf(false) }

    // The [AreasRequest.serial] this tab has already acted on.
    //
    // **Saveable, and that is load bearing rather than tidy.** A tab's content leaves
    // composition when another tab is selected, so a plain `remember` would come back at
    // zero and the next switch back from Momentum would re-open a sheet nobody asked for
    // a second time.
    //
    // **And compared for difference rather than for order**, which the saving is what
    // makes necessary. The two numbers come back from different places after a process
    // death: this one is restored from the saved state, while the shell's serial starts
    // again at zero in the new process and the redelivered intent makes it one. A
    // greater than test would read that first request as old and swallow it, which is a
    // cold start behaving differently from a warm one. Inside one process the serial
    // only goes up, so there the two tests are the same test.
    var handledRequest by rememberSaveable { mutableStateOf(0L) }

    val undoMessage = stringResource(R.string.undo_item_deleted)
    val undoCompleted = stringResource(R.string.undo_item_completed)
    val undoArchived = stringResource(R.string.undo_area_archived)
    val undoAction = stringResource(R.string.action_undo)

    // A widget or a shortcut, arriving as a value rather than as a call because
    // MainActivity has no composition to call into during a cold start.
    LaunchedEffect(request) {
        val asked = request ?: return@LaunchedEffect
        if (asked.serial == handledRequest) return@LaunchedEffect
        // Recorded before the work rather than after it, so that leaving this tab while
        // the wait below is still running drops the request instead of queueing it up
        // to fire on the way back.
        handledRequest = asked.serial
        sheet = when (val target = asked.target) {
            // **Capture waits for nothing**, and that is the whole reason this branch is
            // separate. MASTER_BUILD_PROMPT 14b.1: every step between the thought and
            // the record is somewhere the thought is lost, and a capture sheet held back
            // until the log had finished loading would be a step. It needs no area, so
            // there is nothing about the projection for it to be right or wrong about.
            AreasTarget.Capture -> AreaSheet.AddItem(areaId = null)

            // **An area does wait, and a cold start is the only reason it has to.** The
            // detail sheet dismisses itself when its area is not in the state, which is
            // how an area deleted under an open sheet is handled; on a cold start that
            // same rule would fire against a projection that is merely empty so far, and
            // the widget tap would land on the Areas list. A warm start would open the
            // sheet. This is the seam where those two stop agreeing.
            is AreasTarget.Detail -> {
                viewModel.uiState.first { !it.loading }
                AreaSheet.Detail(target.areaId)
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AreasScreen(
            state = state,
            onOpenArea = { sheet = AreaSheet.Detail(it) },
            onOpenArchive = { archiveOpen = true },
            onCompleteArea = { area ->
                area.activeItemId?.let { itemId ->
                    viewModel.completeItem(area.id, itemId)
                    // Compensating, not deferred. The completion writes now so the next
                    // item can be promoted in front of the person who asked for it, and
                    // the undo puts the item back in the active slot it left.
                    undo = UndoRequest(
                        id = itemId,
                        message = undoCompleted,
                        actionLabel = undoAction,
                        onCommit = {},
                        onUndo = { viewModel.reopenItemAsActive(itemId) },
                    )
                }
            },
            onSwapArea = { sheet = AreaSheet.Swap(it.id) },
            onDeleteArea = { sheet = AreaSheet.DeleteArea(it.id) },
            onLongPressArea = { sheet = AreaSheet.Detail(it.id) },
            onMoveArea = { areaId, index -> viewModel.moveArea(areaId, index) },
            onPromotionPlayed = { viewModel.promotionPlayed(it) },
            onDismissConflict = { viewModel.dismissConflict(it) },
            onOpenInbox = { sheet = AreaSheet.Inbox },
            onOpenFocus = onOpenFocus,
            onOpenPulse = onOpenPulse,
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
                // **Where the plus goes, in three cases rather than two.**
                //
                // With no areas at all it makes an area, which was already right. With one
                // area it now adds to that area, because there is no ambiguity to
                // preserve and sending a person's first thought to an inbox they have
                // never heard of is how five of six usability testers lost track of it.
                // With more than one it still captures to the inbox, which is the whole
                // point of the unfiled capture: no decision at the moment of writing.
                val onlyArea = state.areas.singleOrNull()
                sheet = when {
                    state.isEmpty -> AreaSheet.NewArea
                    onlyArea != null -> AreaSheet.AddItem(areaId = onlyArea.id)
                    else -> AreaSheet.AddItem(areaId = null)
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

        if (archiveOpen) {
            // The Areas screen stays composed underneath, so coming back lands on the
            // list where it was left rather than at the top of it, and something has to
            // stop a thumb reaching it through an opaque screen. A full size sibling
            // drawn behind the archive and in front of everything else swallows every
            // pointer the archive itself did not want. Behind rather than in front, or
            // it would starve the archive's own controls: see `PointerBlocking.kt`.
            Spacer(Modifier.fillMaxSize().swallowsPointerInput())
            ArchiveRoute(viewModel = viewModel, onBack = { archiveOpen = false })
        }
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
                        area.activeItemId?.let { itemId ->
                            viewModel.completeItem(area.id, itemId)
                            undo = UndoRequest(
                                id = itemId,
                                message = undoCompleted,
                                actionLabel = undoAction,
                                onCommit = {},
                                onUndo = { viewModel.reopenItemAsActive(itemId) },
                            )
                        }
                        sheet = null
                    },
                    onSwap = { sheet = AreaSheet.Swap(area.id) },
                    onArchive = {
                        viewModel.archiveArea(area.id)
                        // `undo_area_archived` has existed since phase 2 and was
                        // referenced by nothing. Archiving takes a whole area and its
                        // queue off the home screen in one tap, which is a bigger
                        // disappearance than deleting one item, and it had no way back
                        // short of finding the archive screen.
                        undo = UndoRequest(
                            id = area.id,
                            message = undoArchived,
                            actionLabel = undoAction,
                            onCommit = {},
                            onUndo = { viewModel.unarchiveArea(area.id) },
                        )
                        sheet = null
                    },
                    onDelete = { sheet = AreaSheet.DeleteArea(area.id) },
                    onOpenItem = { sheet = AreaSheet.EditItem(it.id) },
                    onReopenItem = { viewModel.reopenItem(it.id) },
                    // The sheet stays open. Promoting from the queue is a thing a
                    // person may do twice in a row while they decide, and closing the
                    // list they are choosing from would make the second choice a
                    // navigation.
                    onMakeActive = { viewModel.swapToItem(it.id) },
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
