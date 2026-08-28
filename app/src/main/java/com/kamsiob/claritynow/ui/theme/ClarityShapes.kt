package com.kamsiob.claritynow.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
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

/**
 * design-v3.md section 6, spacing. A 4dp base grid, named so nobody guesses.
 *
 * ## Two kinds of value live here, and the difference is the whole of Addendum 01 8f
 *
 * **A gap answers to the text.** Anything whose job is to separate one piece of type
 * from another is read through [scaled], so it opens as the type grows. design-v3.md 13
 * calls the numbers in section 6 minimums rather than targets, and a size control that
 * left them fixed would deliver the failure 8f names: longer lines with the same air
 * between them, which is a less legible screen rather than a more legible one.
 *
 * **A physical measure does not.** A touch target is a fingertip, an area dot is a mark
 * of identity at a size design-v3.md 3.4 fixes, a sheet handle is a grip, and the
 * horizontal insets are the one axis a phone has no more of. None of those has anything
 * to do with how large the text is, and growing the horizontal insets in particular
 * would take room from the measure it is there to serve, which is where clipping starts.
 * Those stay plain `val`s at the specified dp and are named below with the reason.
 *
 * Every scaling value is identical to its specified number whenever the combined font
 * scale is at or below 1.0, which `spacingScale` guarantees, so this whole distinction
 * is invisible on a phone at its default setting.
 */
object ClaritySpacing {

    /** Fixed. Width is the scarce axis and a growing margin shortens the measure. */
    val screenPadding: Dp = 20.dp

    /** Fixed, for the reason [screenPadding] is. */
    val cardPaddingHorizontal: Dp = 18.dp

    /** Fixed. design-v3.md 3.4 gives the dot one size and it is identity, not rhythm. */
    val areaDot: Dp = 7.dp

    /** Fixed. A grip, sized for a thumb rather than for a line of text. */
    val sheetHandleWidth: Dp = 34.dp

    /** Fixed, as [sheetHandleWidth]. */
    val sheetHandleHeight: Dp = 4.dp

    /**
     * Fixed. design-v3.md 10.4 gives the floating bar 61dp and `ClarityTabBar` measures
     * its contents against the 200 percent condition: a 24dp glyph that does not scale
     * at all, and one 13sp label whose trimmed box reaches about 31dp at the cap. Both
     * clear the 48dp item inside the bar with room over, so there is nothing here for a
     * larger text size to rescue, and a bar that grew would take the reserved bottom
     * padding of four screens with it.
     */
    val tabBarHeight: Dp = 61.dp

    /** Fixed, as [tabBarHeight]. The bar's distance from the edge, not a gap in text. */
    val tabBarInset: Dp = 17.dp

    /** Fixed. It contains a glyph, never a string. */
    val fabSize: Dp = 48.dp

    /**
     * Fixed, and it is a floor rather than a size. design-v3.md 13's 48dp minimum is a
     * statement about fingers, so it can never shrink with a smaller text setting, and
     * anything that grows with a larger one grows past it on its own.
     */
    val minTouchTarget: Dp = 48.dp

    /** Scales. The air above and below a line inside a card. */
    val cardPaddingVertical: Dp @Composable get() = scaled(17.dp)

    /** Scales. design-v3.md 6's card rhythm. */
    val cardGap: Dp @Composable get() = scaled(11.dp)

    /** Scales. design-v3.md 6's 28dp between sections. */
    val sectionGap: Dp @Composable get() = scaled(28.dp)

    /** Scales. The drop from a sheet's handle to its first line. */
    val sheetContentTop: Dp @Composable get() = scaled(18.dp)

    /** Scales, because it is a box drawn to hold `swipeLabel`. design-v3.md 10.3.1. */
    val swipeActionWidth: Dp @Composable get() = scaled(66.dp)

    /**
     * A measure whose job is to separate text, at the current combined text size.
     *
     * Call it directly for the many gaps design-v3.md states as a number at a call site
     * rather than as a named token: `Spacer(Modifier.height(ClaritySpacing.scaled(12.dp)))`.
     * The argument stays the specified dp, so the document and the code still agree on
     * what the value is, and the scaling is the one thing added to it.
     *
     * There is deliberately no horizontal counterpart. See the class note.
     */
    @Composable
    fun scaled(base: Dp): Dp = base * spacingScale()
}

val LocalClarityShapes = staticCompositionLocalOf { ClarityShapeScale }
