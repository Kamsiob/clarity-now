package com.kamsiob.claritynow.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.launch
import androidx.compose.ui.unit.dp
import com.kamsiob.claritynow.ui.theme.ClaritySpacing
import com.kamsiob.claritynow.ui.theme.LocalClarityColors
import com.kamsiob.claritynow.ui.theme.LocalClarityTypography

/**
 * design-v3.md 10.6. Every secondary flow is a bottom sheet over a 42 percent
 * scrim, with a 28dp top radius and a 34 by 4dp handle at 18 percent ink.
 *
 * There are no cards inside sheets. Structure comes from sideheads. The single
 * exception in the whole design is the color picker's live preview, which renders
 * an actual miniature area card because showing the person their card is the entire
 * purpose of that element.
 *
 * **There is no sheet shadow, and that is settled rather than missing.** design-v3.md
 * 6.1 states one, `y -8dp blur 40dp black 28 percent`, and it has never had a call site.
 * A sheet's shadow points up out of its own top edge, which only something outside the
 * sheet can draw, and `ModalBottomSheet` exposes nothing outside it that an app can
 * reach. The full analysis is on `ClarityElevation.sheet`, and the short version is that
 * the 42 percent scrim is already a 42 point step of lightness under this surface, so
 * 6.1's own "stop as soon as it reads" stopped at device two.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClaritySheet(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    content: @Composable () -> Unit,
) {
    val colors = LocalClarityColors.current
    val type = LocalClarityTypography.current
    // Held internally so no caller has to name an experimental Material type.
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    // **A sheet closed by finishing it had no exit at all.**
    //
    // Every Save, Add, Done and Never mind in the app calls the caller's `onDismiss`
    // directly, which flips the hosting boolean and removes the sheet from composition in
    // one frame. Material plays the hide animation only on the scrim tap and the drag,
    // so the two ways a person leaves by accident were animated and the way they leave on
    // purpose was a cut, after entering on a 300ms lift. Saving an area was the least
    // resolved moment in the product.
    //
    // `LocalSheetClose` hands every control inside a sheet the animated route out. It is a
    // local rather than a parameter so the twenty three call sites did not each have to
    // grow one, and it falls back to the caller's own `onDismiss` outside a sheet, which
    // is what a preview or a test gets.
    val close: () -> Unit = remember(sheetState, onDismiss, scope) {
        {
            scope.launch {
                runCatching { sheetState.hide() }
                onDismiss()
            }
        }
    }

    CompositionLocalProvider(LocalSheetClose provides close) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        // `card`, and it stays `card`. design-v3.md 3.1 names this token "cards and
        // sheets", and the phase 3c ladder puts content at the top: a sheet is where a
        // person reads and types, not chrome. `raise` is for a surface drawn *inside* a
        // sheet that has to separate from it, and phase 12b gave it its first one: a
        // text field's well steps down to `raise` against this ground, 10.19.
        containerColor = colors.card,
        contentColor = colors.inkPrimary,
        scrimColor = Color.Black.copy(alpha = 0.42f),
        tonalElevation = 0.dp,
        dragHandle = { SheetHandle() },
        modifier = modifier,
    ) {
        // Without this the keyboard covers the lower half of a sheet and the
        // primary action sits underneath it, unreachable.
        Column(
            modifier = Modifier.imePadding().padding(top = ClaritySpacing.sheetContentTop),
        ) {
            if (title != null) {
                Text(
                    text = title,
                    style = type.title,
                    color = colors.inkPrimary,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 0.dp),
                )
                Box(Modifier.height(ClaritySpacing.scaled(14.dp)))
            }
            content()
            Box(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
            Box(Modifier.height(ClaritySpacing.scaled(12.dp)))
        }
    }
    }
}

/**
 * The animated way out of the sheet a control is inside.
 *
 * Outside a sheet it does nothing, so a control that uses it is safe to reuse anywhere.
 */
val LocalSheetClose = staticCompositionLocalOf<() -> Unit> { {} }

@Composable
private fun SheetHandle() {
    val colors = LocalClarityColors.current
    Box(
        modifier = Modifier.fillMaxWidth().padding(vertical = ClaritySpacing.scaled(12.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(width = 34.dp, height = 4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(colors.inkPrimary.copy(alpha = 0.18f)),
        )
    }
}
