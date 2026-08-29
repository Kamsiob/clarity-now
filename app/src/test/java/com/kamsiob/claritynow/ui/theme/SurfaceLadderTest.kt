package com.kamsiob.claritynow.ui.theme

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import kotlin.math.abs
import kotlin.math.pow

/**
 * The three neutral surfaces are a ladder, and this file holds it. design-v3.md 3.1 and
 * 3.2 as amended in phase 3c, issue #53.
 *
 * **Why a test and not a table in a document.** Before phase 3c the light world spanned
 * 4.73 L* from `canvas` to `card`, `raise` sat 1.68 L* under the card, and nothing in the
 * app drew `raise` at all, so a device capture of the Areas screen was 75.7 percent one
 * color and 15.9 percent another. Every one of those facts was true of a codebase that
 * conformed to the document it was built from. The document said what the values were
 * and never said what they had to be to each other, which is the only thing that decides
 * whether a person can see a card.
 *
 * The relationships pinned here are the ones a later session would undo by accident:
 *
 * 1. The order. Ground, then chrome, then content, in both worlds.
 * 2. The size of each step, as a range rather than a value, so a token can be nudged and
 *    a token cannot be reverted.
 * 3. `card` is not pure white and `canvas` is not pure black, which design-v3.md 1 and 14
 *    both require and which 3.1 contradicted until phase 3c.
 * 4. Chrome's step off the ground is matched across the two worlds, because design-v3.md
 *    6.1 gives the dark world no shadows and the step is all it has.
 * 5. `raise` is drawn somewhere. A rank nothing occupies is not a rank.
 *
 * L* is CIE lightness, which is the measure that says whether two surfaces look a
 * different amount of light. A WCAG contrast ratio answers a different question and is
 * the wrong tool for a step between two near-white or two near-black grounds; both are
 * reported here, and only L* is asserted on.
 */
class SurfaceLadderTest {

    private fun lStar(color: Color): Double {
        val y = relativeLuminance(color)
        val d = 6.0 / 29.0
        val f = if (y > d.pow(3)) y.pow(1.0 / 3.0) else y / (3 * d * d) + 4.0 / 29.0
        return 116 * f - 16
    }

    private data class Ladder(val name: String, val colors: ClarityColors) {
        override fun toString() = name
    }

    private val ladders = listOf(
        Ladder("Daylight light", ClarityLightColors),
        Ladder("Daylight dark", ClarityDarkColors),
    )

    @Test
    fun `the ladder runs ground, chrome, content, in both worlds`() {
        ladders.forEach { (name, c) ->
            val canvas = lStar(c.canvas)
            val raise = lStar(c.raise)
            val card = lStar(c.card)
            assertTrue(
                "$name: expected canvas $canvas below raise $raise below card $card. " +
                    "The ladder reads the same way in both worlds on purpose: chrome " +
                    "sits between the page and the content, because the statement it " +
                    "makes is the same statement.",
                canvas < raise && raise < card,
            )
        }
    }

    /**
     * Light measures 4.39 and 2.80, dark 4.43 and 2.55. The ranges are wide enough that a
     * token can be tuned and narrow enough that reverting one fails here.
     *
     * The floor of 2.0 is the real assertion. design-v3.md 3.1 calls `raise` "the 3
     * percent lightness step", and a step materially under that is a token that exists in
     * a table and not on a screen, which is what `raise` was for two phases.
     */
    @Test
    fun `every rung is a step a person can see`() {
        ladders.forEach { (name, c) ->
            val toChrome = abs(lStar(c.raise) - lStar(c.canvas))
            val toContent = abs(lStar(c.card) - lStar(c.raise))
            val whole = abs(lStar(c.card) - lStar(c.canvas))
            assertTrue(
                "$name: ground to chrome measured $toChrome L*, expected 6.0 to 8.0",
                toChrome in 6.0..8.0,
            )
            assertTrue(
                "$name: chrome to content measured $toContent L*, expected 4.0 to 6.5",
                toContent in 4.0..6.5,
            )
            assertTrue(
                "$name: ground to content measured $whole L*, expected 10.5 to 13.5. It " +
                    "was 4.73 in light and 4.98 in dark before phase 3c, 7.19 and 6.98 " +
                    "after it, and the app still read as one field of grey. A card is " +
                    "seen rather than inferred at 1.3:1; the ladder now runs 1.400:1.",
                whole in 10.5..13.5,
            )
        }
    }

    /**
     * design-v3.md 6.1: "Dark and Contemplative worlds: elevation is lightness only. No
     * shadows at all." The dark tab bar and the dark unselected chip therefore have one
     * separation device between them and the page, and it is this step. The light world
     * has a paired shadow as well.
     *
     * So the two worlds are allowed to differ on the content rung, where light gets help,
     * and are held together on the chrome rung, where dark does not.
     */
    @Test
    fun `chrome's step off the ground matches across the two worlds`() {
        val light = abs(lStar(ClarityLightColors.raise) - lStar(ClarityLightColors.canvas))
        val dark = abs(lStar(ClarityDarkColors.raise) - lStar(ClarityDarkColors.canvas))
        assertTrue(
            "light $light L*, dark $dark L*, and they are supposed to be within half a " +
                "point of each other because dark chrome carries no shadow",
            abs(light - dark) < 0.5,
        )
    }

    /**
     * design-v3.md 1: "Backgrounds are never pure white or pure black." design-v3.md 14:
     * "No pure white or pure black backgrounds." design-v3.md 3.1 said `card` `#FFFFFF`
     * until phase 3c and the build followed 3.1, so an idle card rendered literally
     * `#FFFFFF` across its whole face.
     *
     * **Two statements against one, resolved in favor of the two.** Pinned here because a
     * contradiction that was resolved by argument can be un-resolved by a value.
     */
    @Test
    fun `no surface is pure white or pure black`() {
        val surfaces = ladders.flatMap { (name, c) ->
            listOf(
                "$name canvas" to c.canvas,
                "$name raise" to c.raise,
                "$name card" to c.card,
                "$name parchment" to c.parchment,
            )
        } + listOf(
            "Contemplative deepBlack" to ClarityContemplativeColors.deepBlack,
            "Contemplative surfaceRaised" to ClarityContemplativeColors.surfaceRaised,
        )
        val offenders = surfaces.filter { (_, color) ->
            color == Color.White || color == Color.Black
        }
        assertTrue(
            "design-v3.md 1 and 14 both forbid it: " + offenders.joinToString { it.first },
            offenders.isEmpty(),
        )
    }

    /**
     * design-v3.md 1 asks light surfaces to "lean slightly cool grey with warmth in the
     * cards", and until phase 3c every neutral in the app was cool or exactly neutral.
     * The card is where the warmth was promised and the card is where it now is.
     *
     * Measured as red minus blue, which for a near-neutral is the whole of its
     * temperature. The dark world is the unresolved half of the same sentence and is
     * deliberately not asserted here; see the note on [ClarityDarkColors].
     */
    @Test
    fun `the light world is a cool page with warm surfaces on it`() {
        fun warmth(color: Color) = (color.red - color.blue) * 255f
        assertTrue(
            "the canvas is supposed to lean cool and measured ${warmth(ClarityLightColors.canvas)}",
            warmth(ClarityLightColors.canvas) < -2f,
        )
        listOf("card" to ClarityLightColors.card, "raise" to ClarityLightColors.raise)
            .forEach { (name, color) ->
                assertTrue(
                    "$name is supposed to lean warm and measured ${warmth(color)}",
                    warmth(color) > 2f,
                )
            }
    }

    /**
     * `raise` was declared in design-v3.md 3.1, handed to Material's `surfaceVariant` and
     * never drawn, for the whole of phases 1 to 3b. One value was therefore doing four
     * semantically different jobs: the content card, the tab bar, the sheets and the
     * chips were all `card`.
     *
     * This asserts a rank is occupied, not where. The assignment made in phase 3c is
     * chrome: the floating tab bar and an unselected chip. Handing it only to
     * `surfaceVariant` is what it did before, so that line does not count.
     */
    @Test
    fun `raise is drawn by something other than the material color scheme`() {
        val ui = File("src/main/java/com/kamsiob/claritynow/ui")
        assertTrue(
            "expected the UI sources at ${ui.path}, and this run is in " +
                File("").absolutePath + ". Without them this test passes vacuously.",
            ui.isDirectory,
        )
        val callSites = ui.walkTopDown()
            .filter { it.isFile && it.extension == "kt" && it.parentFile?.name != "theme" }
            .flatMap { file ->
                file.readLines().asSequence().mapIndexedNotNull { index, text ->
                    val trimmed = text.trimStart()
                    val isComment = trimmed.startsWith("//") || trimmed.startsWith("*") ||
                        trimmed.startsWith("/*")
                    if (!isComment && text.contains(".raise")) {
                        "${file.name}:${index + 1}"
                    } else {
                        null
                    }
                }
            }
            .toList()
        assertTrue(
            "nothing outside ui/theme draws `raise`, which is the state it was in for " +
                "three phases: a token in a table that no pixel ever took. Assign it or " +
                "delete it, and design-v3.md 3.1 assigns it to chrome.",
            callSites.isNotEmpty(),
        )
    }
}
