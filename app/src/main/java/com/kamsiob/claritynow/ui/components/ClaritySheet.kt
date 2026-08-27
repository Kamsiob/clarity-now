package com.kamsiob.claritynow.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.unit.dp
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

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        // `card`, and it stays `card`. design-v3.md 3.1 names this token "cards and
        // sheets", and the phase 3c ladder puts content at the top: a sheet is where a
        // person reads and types, not chrome. `raise` is for a surface drawn *inside* a
        // sheet that has to separate from it, and there is no such surface yet.
        containerColor = colors.card,
        contentColor = colors.inkPrimary,
        scrimColor = Color.Black.copy(alpha = 0.42f),
        tonalElevation = 0.dp,
        dragHandle = { SheetHandle() },
        modifier = modifier,
    ) {
        // Without this the keyboard covers the lower half of a sheet and the
        // primary action sits underneath it, unreachable.
        Column(modifier = Modifier.imePadding().padding(top = 18.dp)) {
            if (title != null) {
                Text(
                    text = title,
                    style = type.title,
                    color = colors.inkPrimary,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 0.dp),
                )
                Box(Modifier.size(1.dp, 14.dp))
            }
            content()
            Box(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
            Box(Modifier.size(1.dp, 12.dp))
        }
    }
}

@Composable
private fun SheetHandle() {
    val colors = LocalClarityColors.current
    Box(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
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
