package com.kamsiob.claritynow.ui.momentum

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kamsiob.claritynow.di.ClarityViewModelFactory
import com.kamsiob.claritynow.ui.theme.LocalClarityColors

/**
 * The Momentum tab. design-v3.md section 11 and `MASTER_BUILD_PROMPT.md` 12.2.
 *
 * ## How the shell shows it
 *
 * ```
 * TAB_MOMENTUM -> MomentumRoute()
 * ```
 *
 * That is the whole contract. **It generates nothing and writes nothing**, so entering the
 * tab twice costs two reads of the log and changes no state anywhere: Momentum is a mirror
 * and the event catalog has nothing for it to append.
 *
 * ## The ViewModel is resolved against the Activity's store
 *
 * Like the Pulse and unlike the Focus surface, this one has no work in `init` that has to
 * be redone on every entry, so it takes the ordinary store. That is also what puts the
 * Application into `ClarityViewModelFactory`'s creation extras, which is where the corpus
 * assets are reached from. See `di/ViewModels.kt`.
 *
 * ## Loading draws the ground and nothing else
 *
 * design-v3.md 14: no loading spinners. The read is a local database and a fold, the
 * screen it resolves into arrives with its own entrance, and a spinner in front of it for
 * two frames would be a second animation announcing the absence of the first. The Areas
 * screen answers the same question the same way.
 */
@Composable
fun MomentumRoute(
    modifier: Modifier = Modifier,
    viewModel: MomentumViewModel = viewModel(factory = ClarityViewModelFactory),
) {
    val colors = LocalClarityColors.current
    val state by viewModel.state.collectAsStateWithLifecycle()

    when (val view = state.view) {
        null -> Box(modifier = modifier.fillMaxSize().background(colors.canvas))
        else -> MomentumScreen(view = view, modifier = modifier)
    }
}
