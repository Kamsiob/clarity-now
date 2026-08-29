package com.kamsiob.claritynow.ui.trail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Connects the Trail to its view model.
 *
 * **One row shape became tappable and this route now owns a sheet.** The earlier note
 * here said there never would be one, on the grounds that 10.15 gives the Trail no
 * destination and a row is a record rather than a link. That reasoning still holds for
 * twenty four of the twenty five row shapes and they are all still inert. A completion
 * is the exception because it is the only row that describes a state a person may want
 * to leave, and the Trail is where they go looking for it.
 *
 * The screen below still takes plain values and callbacks and has no idea a repository
 * exists.
 */
@Composable
fun TrailRoute(
    viewModel: TrailViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var reopening by remember { mutableStateOf<ReopenTarget?>(null) }

    TrailScreen(
        state = state,
        onSelectArea = viewModel::selectArea,
        onLoadMore = viewModel::loadMore,
        // A row whose completion has already been undone, here or in the area sheet,
        // reports no target and stays inert rather than offering an act that would be
        // refused one layer down.
        onReopen = { itemId, title ->
            if (viewModel.canReopen(itemId)) reopening = ReopenTarget(itemId, title)
        },
        modifier = modifier,
    )

    reopening?.let { target ->
        ReopenSheet(
            title = target.title,
            onReopenToQueue = {
                viewModel.reopenToQueue(target.itemId)
                reopening = null
            },
            onReopenAsActive = {
                viewModel.reopenAsActive(target.itemId)
                reopening = null
            },
            onDismiss = { reopening = null },
        )
    }
}

/** The title is the row's own snapshot, so the sheet names what the Trail named. */
private data class ReopenTarget(val itemId: String, val title: String)
