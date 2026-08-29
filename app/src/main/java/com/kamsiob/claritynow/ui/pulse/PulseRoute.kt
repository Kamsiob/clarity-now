package com.kamsiob.claritynow.ui.pulse

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kamsiob.claritynow.di.ClarityViewModelFactory
import com.kamsiob.claritynow.ui.theme.ClaritySpacing
import com.kamsiob.claritynow.ui.theme.ContemplativeTheme
import com.kamsiob.claritynow.ui.theme.LocalCalmMode
import com.kamsiob.claritynow.ui.theme.LocalContemplativeColors

/**
 * The Pulse surface: the amber night, the sheet, ambient mode and the History page.
 *
 * design-v3.md 10.15 lists the Pulse as a destination entered from the Pulse chip in the
 * Areas header and left by a drag down, a scrim tap or back, which makes it a bottom sheet
 * and not a room the app moves into the way Focus is. It is nonetheless the Contemplative
 * world, design-v3.md section 2, so the theme is entered here and is a theme rather than a
 * branch: the appearance setting can never invert it, and calm mode carries through from
 * whatever the app already resolved.
 *
 * ## How a caller shows it
 *
 * ```
 * if (pulseOpen) PulseRoute(onDismiss = { pulseOpen = false })
 * ```
 *
 * That is the whole contract. **It generates nothing**, so showing it is free and showing
 * it twice is harmless: generation is `PulseCoordinator.generateOnForeground`, called once
 * per process foreground from `ClarityApp`, per `MASTER_BUILD_PROMPT.md` 11.3. This
 * surface reads what is there.
 *
 * [onDismiss] fires for every way out, including the two the person did not ask for by
 * name: dragging the sheet down and tapping the scrim. **Dismissing without answering is a
 * fully supported state**, 11.6, so there is nothing to record on the way out and this
 * callback takes no argument saying how it ended.
 *
 * ## The ViewModel is resolved against the Activity's store
 *
 * Unlike the Focus surface, which builds a store of its own because its ViewModel resolves
 * an outstanding session in `init` and must be built fresh on every entry, this one has no
 * such work: it reads a flow and writes one answer. It therefore takes the ordinary store.
 *
 * **It once had to.** `ClarityViewModelFactory` reached the Pulse lifecycle through the
 * `Application` in the Activity's creation extras, so a store of this route's own would have
 * failed the check there. That coordinator is on `ClarityGraph` since issue #55 and the
 * factory reads no extras at all, so this is now a preference and no longer a constraint.
 */
@Composable
fun PulseRoute(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PulseViewModel = viewModel(factory = ClarityViewModelFactory),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Saveable, so a rotation while reading the history does not throw a person back to
    // the rhythm row. It is one boolean and it is the only navigation this surface has.
    var historyOpen by rememberSaveable { mutableStateOf(false) }

    // design-v3.md 10.15: the History page is left by back. It is drawn inside the sheet
    // rather than pushed onto the shell, so back has to be intercepted here, and only
    // while the page is showing: with it closed the handler stands down and the sheet's
    // own back dismisses the whole surface, which is what the same table asks for.
    //
    // Guarded on the dispatcher owner being present. A `ModalBottomSheet` renders in a
    // window of its own and `BackHandler` throws rather than degrading when no owner
    // reaches it. The visible back control on the page is the way out that does not
    // depend on this at all.
    if (LocalOnBackPressedDispatcherOwner.current != null) {
        BackHandler(enabled = historyOpen) { historyOpen = false }
    }

    ContemplativeTheme(calmMode = LocalCalmMode.current) {
        PulseSheet(
            timeOfDay = state.timeOfDay,
            onDismiss = onDismiss,
            modifier = modifier,
        ) {
            if (historyOpen) {
                PulseHistoryPage(
                    entries = state.past,
                    onBack = { historyOpen = false },
                )
            } else {
                PulseSurface(
                    state = state,
                    onAnswer = viewModel::answer,
                    onOpenHistory = { historyOpen = true },
                )
            }
        }
    }
}

/**
 * The sheet the amber night is drawn in. design-v3.md 10.6 and 3.3.
 *
 * The platform `ModalBottomSheet` at step one of design-v3.md 17.1's order, configured
 * rather than reimplemented: 28dp top radius, a handle, springGentle entrance and a 42
 * percent scrim are all the component's, and what changes is the world it is painted in.
 * The one component in this app that cannot honor calm mode is this sheet's own travel,
 * which 16.8 records as a decision rather than a defect; everything drawn inside it does.
 *
 * **It is a fixed room and not a sheet that resizes with its contents.** The question, the
 * acknowledgment and ambient mode are three states of one surface and the person watches
 * the second become the third, so a container that shrank underneath that transition would
 * turn the calmest moment in the app into a jump. Both phases scroll inside the room they
 * are given rather than growing it, which is what carries a long observation at a large
 * font scale without the sheet moving under the reader.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PulseSheet(
    timeOfDay: PulseTimeOfDay,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val contemplative = LocalContemplativeColors.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = SHEET_RADIUS, topEnd = SHEET_RADIUS),
        containerColor = contemplative.deepBlack,
        contentColor = contemplative.textBright,
        scrimColor = Color.Black.copy(alpha = SCRIM_ALPHA),
        tonalElevation = 0.dp,
        dragHandle = { PulseSheetHandle() },
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = ROOM_MIN_HEIGHT, max = ROOM_HEIGHT),
        ) {
            PulseBackdrop(timeOfDay = timeOfDay)
            content()
        }
        Box(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
    }
}

/** design-v3.md 10.6, in the Contemplative world's ink: 34 by 4dp at 18 percent. */
@Composable
private fun PulseSheetHandle(modifier: Modifier = Modifier) {
    val contemplative = LocalContemplativeColors.current
    Box(
        modifier = modifier.fillMaxWidth().padding(vertical = ClaritySpacing.scaled(12.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(width = HANDLE_WIDTH, height = HANDLE_HEIGHT)
                .clip(RoundedCornerShape(HANDLE_HEIGHT / 2))
                .background(contemplative.textBright.copy(alpha = HANDLE_ALPHA)),
        )
    }
}

/**
 * How tall the room is.
 *
 * design-v3.md gives the Pulse no height, so section 15 applies. The obvious answer is a
 * sheet that wraps its content, which would be about 300dp for the question and about
 * 250dp for ambient mode, and would make the amber night a panel rather than a room and
 * would resize under the settle. This is a little over half a phone: enough for a serif
 * observation to sit in space with its question and its answers beneath it, and short of
 * the full screen that would make a drag down feel like leaving an app.
 *
 * A band rather than a value, because the Activity handles its own rotation and a fixed
 * 520dp is taller than a phone in landscape. The maximum is the room; the minimum is what
 * it collapses to when there is genuinely less than that, and both phases scroll inside
 * whatever they are given, which is also what carries a long observation at a 200 percent
 * font scale.
 */
private val ROOM_HEIGHT = 520.dp
private val ROOM_MIN_HEIGHT = 320.dp

private val SHEET_RADIUS = 28.dp
private val HANDLE_WIDTH = 34.dp
private val HANDLE_HEIGHT = 4.dp
private const val HANDLE_ALPHA = 0.18f

/** design-v3.md 10.6. */
private const val SCRIM_ALPHA = 0.42f
