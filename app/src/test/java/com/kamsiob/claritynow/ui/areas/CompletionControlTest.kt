package com.kamsiob.claritynow.ui.areas

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The area card has a visible way to finish the thing on it.
 *
 * ## Why this is a test and not a comment
 *
 * A test user was handed the built app and said it looked nothing like a to-do app and
 * that you could not tell what you were looking at. The mechanical cause was that
 * completion had no visible control at all: the paths were a swipe, a long press and a
 * sheet, and all three are invisible. `design-v3.md` 10.3.1 had already asked for better
 * in its own words, "swipe is an accelerator, never the only path", and was satisfied by
 * two more undiscoverable gestures.
 *
 * A straight swipe is a path-based gesture, so that arrangement was also a **WCAG 2.1 SC
 * 2.5.1 Level A failure** matching published failure technique F105, on the most frequent
 * action in an app built for disabled users.
 *
 * The control is easy to remove by accident, because every individual piece of it looks
 * like a detail: the gutter's width, the role, where the semantics wrapper stops. Each of
 * those, changed alone, silently returns the app to the state the tester rejected.
 */
class CompletionControlTest {

    @Test
    fun `the card draws a completion control and gates it on there being something to do`() {
        val card = card()
        assertTrue(
            "the card no longer draws a completion gutter, so there is no visible way to " +
                "finish anything and the app is back to swipe only",
            "CompletionGutter(" in card && "private fun CompletionControl(" in card,
        )
        assertTrue(
            "the control is drawn whether or not there is an active item. An idle card " +
                "has nothing to complete and 10.16 wants a control absent rather than " +
                "inert.",
            "if (area.offersComplete)" in card,
        )
    }

    /**
     * **The gutter is the target's width, not the ring's.**
     *
     * The first build made it `CONTROL_SIZE + snug`, 34dp. A `size(48.dp)` inside a 34dp
     * parent is coerced to 34, so the ring drew correctly and the target was 14dp short in
     * the axis a thumb misses on. The very first tap of the new control fell through to
     * the card underneath and opened the detail sheet instead of completing anything.
     */
    @Test
    fun `the control has a real touch target`() {
        val card = card()
        val gutter = card.substringAfter("private fun CompletionGutter(").substringBefore("\n/**")
        assertTrue(
            "the gutter is not the width of the touch target, so a 48dp control inside " +
                "it will be silently coerced smaller. See this test's KDoc.",
            "width(ClaritySpacing.minTouchTarget)" in gutter,
        )
        val control = card.substringAfter("private fun CompletionControl(").substringBefore("\n/**")
        assertTrue(
            "the control does not claim the 48dp section 13 requires",
            "size(ClaritySpacing.minTouchTarget)" in control,
        )
    }

    /**
     * The control has to reach a screen reader, and the card's own semantics actively
     * work against that.
     *
     * `AreaCardSemantics` is a `clearAndSetSemantics`, which wipes every descendant node.
     * While it wrapped the whole card, anything drawn inside it was invisible to TalkBack,
     * so a completion control placed there would have given the card an affordance for
     * everybody except the people 10.3.1's mandatory clause was written for. It is applied
     * to the text column only, and the day somebody moves it back up is the day this
     * regresses in a way no screenshot shows.
     */
    @Test
    fun `the control keeps its own semantics and the text wrapper does not swallow it`() {
        val control = card().substringAfter("private fun CompletionControl(").substringBefore("\n/**")
        assertTrue("the control declares no role", "role = Role.Checkbox" in control)
        assertTrue("the control has no name", "contentDescription = label" in control)

        val screen = File("src/main/java/com/kamsiob/claritynow/ui/areas/AreasScreen.kt").readText()
        assertTrue(
            "AreaCardSemantics is applied as the card's own modifier again, which clears " +
                "the completion control out of the accessibility tree. It belongs on the " +
                "text column: see this test's KDoc.",
            "textSemantics = AreaCardSemantics(area)" in screen,
        )
    }

    /**
     * `design-v3.md` 3.1 scopes `positiveGreen` to "completion only, and a fill only", and
     * `FaintInkTest` reads every `color =` and `tint =` in the app to enforce it. A fill
     * drawn through a color parameter is indistinguishable to that test from a glyph drawn
     * in the same token, so the fill goes through `background` as every other fill in the
     * app does, and the check takes `positiveInk`, whose stated job in 3.1 is exactly this
     * glyph.
     */
    @Test
    fun `the fill is a fill and the check is the ink named for it`() {
        val control = card().substringAfter("private fun CompletionControl(").substringBefore("\n/**")
        assertTrue(
            "the completion fill is not drawn as a background",
            "background(colors.positiveGreen)" in control,
        )
        assertTrue("the check is not drawn in positiveInk", "tint = colors.positiveInk" in control)
        assertTrue(
            "positiveGreen is being used as a foreground, which 3.1 scopes out and " +
                "FaintInkTest fails on",
            "color = colors.positiveGreen" !in control,
        )
    }

    /**
     * The write goes on contact.
     *
     * Immediacy is the half of the evidence that holds: Barkley's account of executive
     * function asks for the gap between an action and its consequence to be compressed at
     * the point of performance, and delay aversion in this population is a medium effect
     * across 4,320 children. A confirmation step here would be the app asking somebody
     * with task-initiation difficulty to initiate twice.
     *
     * Undo is the five second window the completion already had, which is also what
     * `SC 2.5.2` wants standing behind a control that commits on contact.
     */
    @Test
    fun `completing commits immediately and undo stands behind it`() {
        val control = card().substringAfter("private fun CompletionControl(").substringBefore("\n/**")
        assertTrue("the tap does not complete", "onComplete()" in control)
        val route = File("src/main/java/com/kamsiob/claritynow/ui/areas/AreasRoute.kt").readText()
        assertTrue(
            "completing an item no longer offers undo, and the control commits on contact",
            "undoCompleted" in route && "reopenItemAsActive" in route,
        )
    }

    private fun card(): String =
        File("src/main/java/com/kamsiob/claritynow/ui/areas/AreaCard.kt").readText()
}
