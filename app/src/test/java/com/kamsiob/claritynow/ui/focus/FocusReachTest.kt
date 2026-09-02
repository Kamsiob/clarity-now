package com.kamsiob.claritynow.ui.focus

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * How far away a focus session is, and the three things that were missing at the moment it
 * is chosen. Issue #62, `MASTER_BUILD_PROMPT.md` section 10 and `ADDENDUM_01` 4b.
 *
 * Focus is one of two anchors on the home screen and arranging five minutes took eight
 * interactions: the chooser started a session at whatever Settings said, so changing the
 * length meant leaving the surface, finding a row four screens away and coming back, and
 * there was no way at all to start on the item a person was already looking at.
 *
 * These are source scans, for `InterfaceContractTest`'s reason: they need no device, they
 * run inside `verifyClarity`, and they fail naming the file. What they cannot check is
 * whether the result feels close to hand, which is what the device pass is for.
 */
class FocusReachTest {

    // ------------------------------------------------------------------ the length row

    /**
     * One list of lengths and one preference, shared with Settings.
     *
     * A second list here would drift from the eight `MASTER_BUILD_PROMPT` section 10 names,
     * and a second preference would mean the chooser said one thing and Settings said
     * another about the same session.
     */
    @Test
    fun `the chooser offers the same eight lengths settings offers, and writes the same setting`() {
        assertTrue(
            "the chooser reads the one list of lengths",
            "ClarityPreferences.FOCUS_DURATION_OPTIONS" in chooser(),
        )
        assertTrue(
            "and Settings reads it too, so there is one list",
            "ClarityPreferences.FOCUS_DURATION_OPTIONS" in settingsSheets(),
        )
        assertTrue(
            "the chooser writes the preference Settings writes",
            "preferences.setFocusDurationMinutes(minutes)" in viewModel(),
        )
    }

    /**
     * The one-turn law, `docs/COMPONENT_AND_LAYOUT.md` A.4.
     *
     * > A Contemplative surface carries exactly one filled anchor, and everything else on
     * > it is text with no container.
     *
     * The obvious control for eight choices is a segmented track, which is what Settings
     * uses on its own Daylight surface. Here it would have been a second filled thing on a
     * screen whose whole design is one dim room with text in it, so the lengths are eight
     * numerals with nothing behind them. This asserts that nobody quietly gives them a
     * container later.
     */
    @Test
    fun `the length row is text with no container`() {
        val row = body(chooser(), "private fun FocusLengthRow(")
        assertFalse("a fill here is a second anchor on a Contemplative surface", "background(" in row)
        assertFalse("and so is a border", "border(" in row)
        assertFalse("and so is a shape to put one on", "RoundedCornerShape" in row)
        assertTrue(
            "the selected length is a brightness, which is the device the rows below use",
            "contemplative.textBright" in row && "contemplative.textDim" in row,
        )
        assertFalse(
            "an unselected length is a choice on offer, not an inactive control, so it " +
                "may not take the 32 percent token `FaintInkTest` reserves for one",
            "textFaint" in row,
        )
        assertTrue(
            "the numeral is small and the target is not. design-v3.md 13 measures the box",
            "ClaritySpacing.minTouchTarget" in row,
        )
    }

    // ------------------------------------------------------------------ the first step

    /**
     * `ADDENDUM_01` 4b: the field exists so that a person who cannot start has already
     * written down how to. It was on the card, in the area sheet and on a widget, and
     * missing from the two screens that are about starting.
     */
    @Test
    fun `the first step is carried to the moment a session is chosen and the moment it runs`() {
        assertTrue(
            "the chooser's row model carries it",
            "val activeItemFirstStep: String? = null," in viewModel(),
        )
        assertTrue(
            "and the running session's model carries it",
            "val itemFirstStep: String? = null," in viewModel(),
        )
        assertTrue("the chooser draws it", "option.activeItemFirstStep" in chooser())
        assertTrue("the running screen draws it", "session.itemFirstStep" in sessionScreen())
    }

    // ------------------------------------------------------------------ starting here

    /**
     * Starting on the item being read, without crossing the app to find it again.
     *
     * The start itself goes through `FocusViewModel.startOnItem`, which is the path the
     * `First Step` widget already uses, so the decision about whether a session may begin
     * is taken once, in the repository, under the one lock. A sheet that appended
     * `FOCUS_STARTED` itself would be a second write path.
     */
    @Test
    fun `the area sheet can start a session on the item it is showing`() {
        assertTrue("the sheet offers it", "onFocus: () -> Unit," in sheets())
        assertTrue("named for the item rather than for the surface", "action_focus_on_this" in sheets())
        assertTrue(
            "the route hands the active item up rather than starting anything itself",
            "area.activeItemId?.let(onFocusOnItem)" in route(),
        )
        assertTrue(
            "and the shell sends it in on the same value the widget arrives on",
            "focusStart = FocusStart(inAppFocusSerial, itemId)" in shell(),
        )
        assertFalse(
            "the sheet must not reach around the one write path",
            "startFocus(" in sheets() || "FocusStarted(" in sheets(),
        )
    }

    /**
     * Four interactions from the home screen for a five minute session, and the count is a
     * property of the surface rather than a number somebody measured once.
     *
     * The chooser reaches a running session through exactly two taps of its own: a length
     * and a row. Anything that put a sheet, a confirm or a second screen between them
     * would add one, so what this asserts is that neither handler opens anything.
     */
    @Test
    fun `the chooser starts a session in two taps and opens nothing in between`() {
        val chooser = chooser()
        val signature = chooser.substringAfter("internal fun FocusChooserScreen(").substringBefore(") {")
        assertEquals(
            "one callback that starts a session, and one that sets the length. A third " +
                "would be a third thing to do before five minutes begins",
            listOf(
                "onSelect: (areaId: String, itemId: String) -> Unit,",
                "onDurationChange: (Int) -> Unit,",
            ),
            signature.lines().map { it.trim() }.filter { it.startsWith("on") },
        )
        assertFalse(
            "a sheet or a dialog here is a third interaction on the way to five minutes",
            "Sheet(" in chooser || "Dialog(" in chooser,
        )
    }

    // ------------------------------------------------------------------ helpers

    private fun read(path: String): String =
        File("src/main/java/com/kamsiob/claritynow/$path").readText()

    private fun chooser(): String = read("ui/focus/FocusChooserScreen.kt")
    private fun sessionScreen(): String = read("ui/focus/FocusSessionScreen.kt")
    private fun viewModel(): String = read("ui/focus/FocusViewModel.kt")
    private fun route(): String = read("ui/areas/AreasRoute.kt")
    private fun sheets(): String = read("ui/areas/AreaSheets.kt")
    private fun shell(): String = read("ui/nav/ClarityShell.kt")
    private fun settingsSheets(): String = read("ui/settings/SettingsSheets.kt")

    /**
     * A function's body with its comments removed.
     *
     * **The comments have to go or these assertions read the prose instead of the code.**
     * The note above the length row's ink explains why it is not `textFaint`, and an
     * assertion that scanned the raw text would find the word there and fail on the
     * sentence that says the rule is held. That is the same defect these tests exist to
     * catch, one layer up.
     */
    private fun body(source: String, signature: String): String =
        source.substringAfter(signature)
            .substringBefore("\n/**")
            .lines()
            .filterNot { it.trimStart().startsWith("//") }
            .joinToString("\n")
}
