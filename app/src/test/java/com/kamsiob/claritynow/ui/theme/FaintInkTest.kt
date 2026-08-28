package com.kamsiob.claritynow.ui.theme

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * The tokens that may never carry a foreground, and the gate that proves nothing
 * does. design-v3.md 3.1 and 13.
 *
 * `inkTertiary` and the Contemplative `textFaint` are the only colors in this design that
 * clear no floor it states. That is not an oversight in the palette; both exist so that a
 * shape which is not read can be quieter than one that is. design-v3.md 3.1 says so in
 * one sentence: **"inkTertiary carries no text anywhere in this app."** design-v3.md 13
 * says the Contemplative half: **"Contemplative text stays at or above 55 percent opacity
 * where it is meant to be read"**, and `textFaint` is 32.
 *
 * **A floor cannot hold that, so a source scan does.** A contrast test measures a pair it
 * is given; it has no way to notice a screen that quietly started drawing one. That is
 * exactly how these two tokens spread: `TrailContrastTest` has proved since phase 3 that
 * no Trail text takes `inkTertiary`, and it proved only that, so the same token reached
 * the sheets, the settings screen, About, the color picker, the inbox and two widgets
 * while a green test said the rule was held.
 *
 * ## The measurements
 *
 * | token | light canvas | light card | dark card | Contemplative ground |
 * |---|---|---|---|---|
 * | `inkTertiary` | 2.337 | 2.402 | 3.256 | |
 * | `textFaint` | | | | 2.636 |
 * | `positiveGreen` | 1.833 | 2.203 | 7.345 | |
 *
 * Against 4.5 for text and 3.0 for a mark that carries meaning, design-v3.md 13. In the
 * light world `inkTertiary` misses both, so nothing turns on whether a given line is a
 * label or a glyph.
 *
 * ## The third one is here for the same gate and the opposite reason
 *
 * This file is named for the first two, which are faint by construction. `positiveGreen`
 * is neither faint nor translucent: it is a saturated fill that fails as a foreground in
 * the light world because it is too light, at 1.833 on the canvas and 1.680 as the
 * positive button's own label on its own 13 percent fill. The phase 13 audit split the
 * completion color into a fill and a foreground, 3.1, and the fill then needs exactly
 * what these two need, which is proof that no screen draws it. It is the same gate, so it
 * is in the same file rather than in a second one that would have to be found. **What a
 * floor cannot do is notice a screen that quietly started drawing a token**, and that is
 * true whichever direction the token fails in.
 *
 * ## What is allowed, and why
 *
 * WCAG 1.4.3 exempts **inactive user interface components**, and this design uses that
 * exemption in four places: a destructive button before its condition is met, a Focus
 * chooser row for an area that cannot be started, and an onboarding control that is not
 * yet enabled. Those are listed in [INACTIVE_STATES] by the expression that makes them
 * inactive rather than by a line number, so they survive the file moving. Nothing else is
 * allowed, and adding to the list is a decision somebody has to write a sentence for.
 */
class FaintInkTest {

    /**
     * The four inactive-control uses, matched on the conditional that makes them
     * inactive. WCAG 1.4.3: "Text or images of text that are part of an inactive user
     * interface component ... have no contrast requirement."
     */
    private val inactiveStates = listOf(
        "if (enabled) colors.card else colors.inkTertiary" to
            "design-v3.md 10.7. The destructive button is inert until its condition is " +
                "met, and an inert button is an inactive component.",
        "if (option.selectable) contemplative.textDim else contemplative.textFaint" to
            "design-v3.md 11. A Focus chooser row for an area with nothing to work on " +
                "cannot be started.",
        "if (option.selectable) contemplative.textBright else contemplative.textFaint" to
            "design-v3.md 11, the same row's title.",
        "if (enabled) contemplative.textBright else contemplative.textFaint" to
            "design-v3.md 10.7 in the Contemplative world, an onboarding control that is not yet enabled.",
    )

    private val sourceRoots = listOf(
        "src/main/java/com/kamsiob/claritynow/ui",
        "src/main/java/com/kamsiob/claritynow/widget",
    )

    @Test
    fun `nothing in the app draws a foreground in inkTertiary`() {
        assertNoForeground(
            token = "inkTertiary",
            measured = "2.337 to one on the light canvas, 2.402 on the light card and " +
                "3.256 on the dark card",
            rule = "design-v3.md 3.1: \"inkTertiary carries no text anywhere in this app\", " +
                "and \"never on text\" in the table above it. The remedy the last three " +
                "phases used every time this came up is the same one: inkSecondary, which " +
                "measures 4.880 on the light canvas. See the notes in AreaCard.kt 10.3, " +
                "Fields.kt 10.19, TrailScreen.kt and MomentumScreen.kt, each of which is a " +
                "correction of exactly this on one screen.",
        )
    }

    @Test
    fun `nothing in the app draws a foreground in positiveGreen`() {
        assertNoForeground(
            token = "positiveGreen",
            measured = "1.833 to one on the light canvas, 2.203 on the light card and " +
                "1.680 as the positive button's own label on its 13 percent fill",
            rule = "design-v3.md 3.1 as amended by the phase 13 contrast audit: " +
                "positiveGreen is a fill and positiveInk is the foreground. The fill has " +
                "to stay light enough for what sits on it, since design-v3.md 11 calls " +
                "the Trail's completed ground a mint and 10.7 puts a label on a 13 " +
                "percent button, and a foreground on those grounds has to be dark. One " +
                "value cannot be both, so the remedy is never to lighten the fill: it is " +
                "positiveInk, which measures 5.825 on the light canvas and 4.912 on the " +
                "Complete swipe face.",
        )
    }

    @Test
    fun `nothing in the app draws a foreground in the Contemplative textFaint`() {
        assertNoForeground(
            token = "textFaint",
            measured = "2.636 to one on deepBlack",
            rule = "design-v3.md 13: \"Contemplative text stays at or above 55 percent " +
                "opacity where it is meant to be read\". textFaint is 32 percent and " +
                "textDim is 55, and textDim measures 5.697 on the same ground.",
        )
    }

    private fun assertNoForeground(token: String, measured: String, rule: String) {
        val roots = sourceRoots.map { File(it) }
        roots.forEach { dir ->
            assertTrue(
                "expected the sources at ${dir.path}, and this run is in " +
                    File("").absolutePath + ". Without them this test passes vacuously.",
                dir.isDirectory,
            )
        }
        val offenders = roots.flatMap { root ->
            root.walkTopDown()
                .filter { it.isFile && it.extension == "kt" && it.parentFile?.name != "theme" }
                .flatMap { file ->
                    file.readLines().asSequence().mapIndexedNotNull { index, text ->
                        val trimmed = text.trimStart()
                        val isComment = trimmed.startsWith("//") || trimmed.startsWith("*") ||
                            trimmed.startsWith("/*")
                        val isForeground = FOREGROUND.containsMatchIn(text)
                        val excused = inactiveStates.any { (snippet, _) -> text.contains(snippet) }
                        if (!isComment && isForeground && text.contains(token) && !excused) {
                            "${file.path}:${index + 1}: ${trimmed.trimEnd()}"
                        } else {
                            null
                        }
                    }
                }
        }
        assertTrue(
            "$token measures $measured, against design-v3.md 13's floor of 4.5 for text " +
                "and 3.0 for a mark that carries meaning. ${offenders.size} foregrounds " +
                "are drawn in it:\n" + offenders.joinToString("\n") +
                "\n\n$rule\n\nThe four inactive-control uses WCAG 1.4.3 excuses are named " +
                "in this file and are not in the list above.",
            offenders.isEmpty(),
        )
    }

    private companion object {
        /** A `color =` or `tint =` argument, which is how a foreground reaches a composable or a draw call. */
        val FOREGROUND = Regex("""\b(color|tint) = """)
    }
}
