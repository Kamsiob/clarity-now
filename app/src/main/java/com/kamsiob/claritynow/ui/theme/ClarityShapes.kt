package com.kamsiob.claritynow.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** design-v3.md section 6. Every radius in the app is named here. */
@Immutable
data class ClarityShapes(
    val card: RoundedCornerShape = RoundedCornerShape(18.dp),
    val row: RoundedCornerShape = RoundedCornerShape(12.dp),
    val sheet: RoundedCornerShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    val button: RoundedCornerShape = RoundedCornerShape(12.dp),
    val pill: RoundedCornerShape = RoundedCornerShape(percent = 50),
    val widgetInner: RoundedCornerShape = RoundedCornerShape(8.dp),
    val momentumTile: RoundedCornerShape = RoundedCornerShape(11.dp),
    val settingsBadge: RoundedCornerShape = RoundedCornerShape(8.dp),
    val swatch: RoundedCornerShape = RoundedCornerShape(16.dp),
    val moodPill: RoundedCornerShape = RoundedCornerShape(8.dp),
    val appearanceTile: RoundedCornerShape = RoundedCornerShape(12.dp),
    val weeklyBanner: RoundedCornerShape = RoundedCornerShape(14.dp),
    val markBadge: RoundedCornerShape = RoundedCornerShape(16.dp),
    val snackbar: RoundedCornerShape = RoundedCornerShape(12.dp),
)

val ClarityShapeScale = ClarityShapes()

/** design-v3.md section 6, spacing. A 4dp base grid, named so nobody guesses. */
object ClaritySpacing {
    val screenPadding: Dp = 20.dp
    val cardPaddingHorizontal: Dp = 18.dp
    val cardPaddingVertical: Dp = 17.dp
    val cardGap: Dp = 11.dp
    val sectionGap: Dp = 28.dp
    val sheetContentTop: Dp = 18.dp
    val sheetHandleWidth: Dp = 34.dp
    val sheetHandleHeight: Dp = 4.dp
    val areaDot: Dp = 7.dp
    val swipeActionWidth: Dp = 66.dp
    val tabBarHeight: Dp = 61.dp
    val tabBarInset: Dp = 17.dp
    val fabSize: Dp = 48.dp
    val minTouchTarget: Dp = 48.dp
}

val LocalClarityShapes = staticCompositionLocalOf { ClarityShapeScale }
