package com.kamsiob.claritynow.ui.report

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kamsiob.claritynow.di.ClarityViewModelFactory
import com.kamsiob.claritynow.ui.theme.ContemplativeTheme
import com.kamsiob.claritynow.ui.theme.LocalCalmMode
import com.kamsiob.claritynow.ui.theme.LocalContemplativeColors

/**
 * The Report tab: the gold editorial night, and the past reports page over it.
 *
 * `design-v3.md` section 2 puts the Clarity Report in the Contemplative world, so the theme
 * is entered here and it is a theme rather than a branch: the appearance setting in 10.10
 * can never invert it, and calm mode carries through from whatever the app already
 * resolved. That is the same shape `PulseRoute` and `FocusRoute` take, and the reason
 * 10.10's caption line reads `Focus, Pulse and Report are always dark by design`.
 *
 * **It is a tab and not a room the app moves into.** Unlike Focus, which covers the tab bar
 * because it is a place, this is one of the four destinations in 10.15 and the tab bar stays
 * where it is. The page therefore leaves room for it rather than drawing under it.
 *
 * ## Generation happens here and nowhere else
 *
 * Unlike the Pulse, whose generation is once per day on the first app foreground and whose
 * surface only reads, `MASTER_BUILD_PROMPT.md` 12.3 generates a report on the first open of
 * this tab in a new week. So the ViewModel composes on entry, which is what its `init`
 * does, and the regenerate control asks for the same thing again.
 *
 * ## The ViewModel is resolved against the Activity's store
 *
 * Which is what carries `ReportUiState.revealKey` across a tab switch, and what makes a
 * process death re-arm the reveal while a rotation does not. `ReportViewModel` has the
 * argument in full. It also means `ClarityViewModelFactory` receives the Activity's
 * creation extras and can reach the Application, which is where the corpus assets live.
 */
@Composable
fun ReportRoute(
    modifier: Modifier = Modifier,
    viewModel: ReportViewModel = viewModel(factory = ClarityViewModelFactory),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Saveable, so a rotation while reading the history does not throw a person back to
    // this week's report. It is one boolean and it is the only navigation this tab has.
    var historyOpen by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(historyOpen) {
        if (historyOpen) viewModel.loadPast()
    }

    // 10.15: the history page is left by back. Registered inside the shell's own handler so
    // it wins while the page is showing, and it stands down when the page closes, at which
    // point the shell's handler takes back over and back returns to Areas.
    //
    // Guarded on the dispatcher owner being present for the same reason `PulseRoute` guards
    // its own: `BackHandler` throws rather than degrading when no owner reaches it, and the
    // visible back control on the page does not depend on this at all.
    if (LocalOnBackPressedDispatcherOwner.current != null) {
        BackHandler(enabled = historyOpen) { historyOpen = false }
    }

    ContemplativeTheme(calmMode = LocalCalmMode.current) {
        // The ground is painted here as well as in the backdrop, so that the surface behind
        // a scroll overshoot is the Contemplative black rather than the Daylight canvas the
        // shell drew underneath this tab.
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(LocalContemplativeColors.current.deepBlack),
        ) {
            if (historyOpen) {
                ReportHistoryPage(
                    reports = state.past,
                    loading = state.pastLoading,
                    onBack = { historyOpen = false },
                )
            } else {
                ReportScreen(
                    state = state,
                    onHistory = { historyOpen = true },
                    onRegenerate = viewModel::regenerate,
                    onRevealFinished = viewModel::revealFinished,
                    onAccept = viewModel::acceptPlan,
                    onDecline = viewModel::declinePlan,
                )
            }
        }
    }
}
