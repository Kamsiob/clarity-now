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
    val momentumTile: RoundedCornerShape = RoundedCornerShape(12.dp),
    /**
     * 8dp, and it is the only thing left at this rung. It was named `settingsBadge`
     * for a badge that the refresh deletes; the segmented control's own corner is
     * the same 8dp, sitting concentrically inside its 12dp track with 4dp of track
     * padding, so the value survives under the name of its one remaining caller.
     */
    val segment: RoundedCornerShape = RoundedCornerShape(8.dp),
    val swatch: RoundedCornerShape = RoundedCornerShape(18.dp),
    val moodPill: RoundedCornerShape = RoundedCornerShape(8.dp),
    val appearanceTile: RoundedCornerShape = RoundedCornerShape(12.dp),
    val markBadge: RoundedCornerShape = RoundedCornerShape(18.dp),
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
    /**
     * **The spacing ladder. Six values, a Fibonacci sequence snapped to the 4dp unit.**
     *
     * `ui/` carried 27 distinct spacing values before the refresh, 19 of them off the
     * 4dp grid section 6 claims: 10, 14, 6, 18, 26, 22, 7, 5, 9, 13, 17, 11, 15, 30, 34.
     * That is not a grid with exceptions, it is a habit of picking a number, and it is
     * most of why the app read as assembled rather than made.
     *
     * | token | dp | units | ratio | job |
     * |---|---|---|---|---|
     * | [hair] | 4 | 1 | | inside one unit: a glyph to its label, a dot to its name |
     * | [tight] | 8 | 2 | 2.00 | lines of a single thought: a title to its caption |
     * | [snug] | 12 | 3 | 1.50 | peers in a group: card to card, row to row |
     * | [step] | 20 | 5 | 1.67 | the screen inset, and a block to a sibling block |
     * | [rest] | 32 | 8 | 1.60 | section to section |
     * | [movement] | 52 | 13 | 1.63 | movement to movement, and the bottom reservation |
     *
     * Three properties an 8pt ladder does not have, which is why it is refused.
     *
     * 1. **It is additive.** `snug + step = rest` and `step + rest = movement`, so a
     *    nested layout can never drift off the ladder.
     * 2. **The ratio holds** at 1.50 to 1.67, converging on phi. An 8pt ladder is
     *    arithmetic above its first step and its ratio collapses to 1.25 as it climbs,
     *    so its top steps stop being distinguishable, which is exactly why a team
     *    reaches for a number between two of them.
     * 3. **It is closed under the 200 percent text multiply**: 8, 16, 24, 40, 64, 104.
     *
     * Vertical rhythm is then a function of the type above it, so nobody picks a number:
     * line to line inside one thought is [tight]; block to sibling block is one line slot
     * of the block's own role snapped down; block to a new section is two slots of the
     * largest role above it; a sidehead to its content is [snug], fixed.
     */
    val hair: Dp = 4.dp
    val tight: Dp = 8.dp
    val snug: Dp = 12.dp
    val step: Dp = 20.dp
    val rest: Dp = 32.dp
    val movement: Dp = 52.dp


    /** Fixed. Width is the scarce axis and a growing margin shortens the measure. */
    val screenPadding: Dp = 20.dp

    /** Fixed, for the reason [screenPadding] is. */
    val cardPaddingHorizontal: Dp = 18.dp

    /** Fixed. design-v3.md 3.4 gives the dot one size and it is identity, not rhythm. */
    /**
     * 9dp, up from 7. The dot is the app's identity device under 3.4 and it was the
     * smallest thing on a card carrying the most meaning; at 7dp against a 21.5sp title
     * it read as a bullet rather than as the area. It is still well under 16.7's floor
     * for a device that has to be seen, which is why it is paired with the name and
     * never asked to carry the area alone.
     */
    val areaDot: Dp = 9.dp

    /** Fixed. A grip, sized for a thumb rather than for a line of text. */
    val sheetHandleWidth: Dp = 34.dp

    /** Fixed, as [sheetHandleWidth]. */
    val sheetHandleHeight: Dp = 4.dp

    /**
     * Fixed. design-v3.md 10.4 gives the floating bar 61dp and `ClarityTabBar` measures
     * its contents against the 200 percent condition: a 26dp glyph that does not scale
     * at all, and one 13sp label whose trimmed box reaches about 31dp at the cap. Both
     * clear the 48dp item inside the bar with room over, so there is nothing here for a
     * larger text size to rescue, and a bar that grew would take the reserved bottom
     * padding of four screens with it.
     *
     * **68dp, up from 61.** The bar became a stacked layout in the appeal pass: a 32dp
     * indicator with a 26dp glyph centered in it, 3dp, and a 13sp label under it. That is
     * 51dp of content, and 68 gives it 8dp of air top and bottom while staying under
     * Material 3's own 80dp navigation bar. The old 61 was sized for a single row of
     * glyphs and was the largest single reason the bar read as an afterthought.
     */
    val tabBarHeight: Dp = 68.dp

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
    val cardPaddingVertical: Dp @Composable get() = scaled(12.dp)

    /** Scales. design-v3.md 6's card rhythm. */
    val cardGap: Dp @Composable get() = scaled(snug)

    /** Scales. design-v3.md 6's 28dp between sections. */
    val sectionGap: Dp @Composable get() = scaled(rest)

    /** Scales. The drop from a sheet's handle to its first line. */
    val sheetContentTop: Dp @Composable get() = scaled(step)

    /** Scales, because it is a box drawn to hold a `sidehead` label. design-v3.md 10.3.1. */
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
