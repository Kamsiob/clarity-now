package com.kamsiob.claritynow.ui.trail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Connects the Trail to its view model, and does nothing else.
 *
 * There is no sheet state to own here, and there never will be: design-v3.md 10.15
 * gives the Trail no destination and section 6 of this phase's brief settles that a
 * row is not tappable, so the screen has exactly one control. That leaves this route
 * thinner than `AreasRoute` on purpose rather than by omission. The screen below
 * still takes plain values and callbacks, so it can be read in isolation and has no
 * idea a repository exists.
 */
@Composable
fun TrailRoute(
    viewModel: TrailViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    TrailScreen(
        state = state,
        onSelectArea = viewModel::selectArea,
        onLoadMore = viewModel::loadMore,
        modifier = modifier,
    )
}
