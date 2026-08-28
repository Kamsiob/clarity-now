package com.kamsiob.claritynow.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

/**
 * Contrast with calm mode applied, measured per area color. design-v3.md 13 and 16.4,
 * Addendum 01 8f, issue #48.
 *
 * design-v3.md 16.4 calls calm mode "the one place where serving one accessibility need
 * could break another, so it is measured". This file is that measurement, and it is
 * where it stops being an eye judgment: phase 3 found a 4.40 to one failure on a screen
 * that looked fine, and this phase found a 3.83 to one failure the same way.
 *
 * **Every ratio here is measured against the ground the text is actually drawn on**,
 * which for anything on an area card means the card carrying that area's own wash. That
 * is the whole finding. `areaLabelColor` used to verify against the bare `card` token,
 * which is what design-v3.md 3.4's word "card" reads as until you notice that the card a
 * label sits on is never bare. The rejected reading is measured below rather than
 * described, so this file fails if somebody restores it.
 *
 * The three grounds are the three the design permits: no wash on an idle area, the
 * resting wash, and the in-session wash. design-v3.md 8.2 item 1's promotion peak of 11
 * percent is shallower than the in-session wash in both worlds and is covered by it.
 *
 * **Phase 3c moved four light tokens and two dark ones and every number below is the
 * re-measurement.** `areaLabelColor` recomputes its ground from the token set, so the
 * variants adapted on their own: eight of the 96 changed, three in light and five in
 * dark. What did not adapt was the assumption underneath them, and it was wrong. See
 * the calm ground note in `AreaPalette.kt`.
 *
 * ## What this file still owns, since the audit arrived
 *
 * `ContrastAuditTest` now runs the total enumeration: every token by reflection, every
 * ground the design permits, every one of the 48 colors at every wash depth, calm on and
 * off, in four worlds. It subsumes the first test below and holds a wider version of it.
 *
 * What is kept here is the part that is about **calm mode specifically** and that a
 * general audit cannot state: the reading of design-v3.md 3.4 that was rejected and what
 * it costs, the fact that the label is one color with the switch either way, and the
 * bound on how far the transform is allowed to move a ratio it does not own. Those are
 * findings rather than floors, and a finding has to be written down where the argument
 * for it is.
 */
class CalmModeContrastTest {

    /** design-v3.md 13. Normal sized text, so 4.5 to 1 rather than 3 to 1. */
    private val floor = 4.5

    /** design-v3.md 13. A glyph is a graphic, and graphics take the 3 to 1 floor. */
    private val graphicFloor = 3.0

    private val palette = AreaPalette.all

    private data class World(
        val name: String,
        val colors: ClarityColors,
        val calm: ClarityColors,
    )

    private val worlds = listOf(
        World("Daylight light", ClarityLightColors, ClarityLightColors.calmed()),
        World("Daylight dark", ClarityDarkColors, ClarityDarkColors.calmed()),
    )

    /** The card as drawn: the area's accent pooled onto it at [alpha]. */
    private fun ground(accent: Color, alpha: Float, card: Color): Color =
        if (alpha <= 0f) card else accent.copy(alpha = alpha).compositeOver(card)

    /** Every ground an area label or an item title can sit on, ordinary and calm. */
    private fun grounds(hex: String, world: World): Map<String, Color> {
        val accent = parseAreaColor(hex)
        val calmAccent = accent.calmed(true)
        return mapOf(
            "idle card" to world.colors.card,
            "resting wash" to ground(accent, world.colors.cardWashAlpha, world.colors.card),
            "in session wash" to
                ground(accent, world.colors.cardWashActiveAlpha, world.colors.card),
            "resting wash, calm" to
                ground(calmAccent, world.calm.cardWashAlpha, world.colors.card),
            "in session wash, calm" to
                ground(calmAccent, world.calm.cardWashActiveAlpha, world.colors.card),
        )
    }

    /**
     * design-v3.md 3.4 requires 4.5 to one per area color, and 16.2 leaves the label
     * unchanged in calm mode, so the same variant has to clear on the calm ground as
     * well. All 48 colors, both worlds, five grounds, 480 measurements.
     *
     * The tightest is 4.538 to one, `#A68B6B` on a light in-session card. The dark
     * world's tightest is 4.655, `#92400E` in calm mode, and dark used to be the tighter
     * of the two with 0.04 over the floor. It is no longer, because phase 3c made the
     * calm ground one of the two the variant is solved against rather than an
     * assumption about it.
     *
     * This is a test rather than a note because a later change to a wash opacity, to a
     * surface token or to the 48 colors moves it. Phase 3c is the proof: it moved.
     */
    @Test
    fun `every area label clears the floor on every ground it can sit on`() {
        val failures = mutableListOf<String>()
        var tightest = Double.MAX_VALUE

        worlds.forEach { world ->
            palette.forEach { hex ->
                val label = areaLabelColor(parseAreaColor(hex), world.colors)
                grounds(hex, world).forEach { (name, on) ->
                    val ratio = contrastRatio(label, on)
                    if (ratio < tightest) tightest = ratio
                    if (ratio < floor) {
                        failures += "${world.name} $hex on $name at $ratio"
                    }
                }
            }
        }

        assertTrue(
            "below design-v3.md 13's floor of $floor to one: " + failures.joinToString("; ") +
                ". design-v3.md 3.4 names the only permitted remedy: adjust the label " +
                "variant, never the dot and never the wash.",
            failures.isEmpty(),
        )
        assertTrue(
            "the tightest measurement was $tightest, and it is supposed to sit just " +
                "above the floor. Well above it means the label variants moved; below " +
                "it means the assertion above should have caught it.",
            tightest in 4.50..4.80,
        )
    }

    /**
     * The reading this function used to have, kept as a measurement so it cannot come
     * back quietly.
     *
     * Verifying the label against `colors.card` clears the floor on all 48 colors and is
     * wrong, because no label is ever drawn on a bare card unless its area is idle. On an
     * in-session card the worst of the 48 measures 3.707 to one in light, which is
     * further under the floor than the Trail defect phase 3 found. It measured 3.833
     * before phase 3c took `card` off pure white, so the rejected reading got worse
     * rather than better, which is the direction that matters: the cost of getting this
     * wrong grows with the design rather than shrinking.
     */
    @Test
    fun `verifying the label against the bare card is the reading that fails`() {
        val light = ClarityLightColors
        var worstOnBareCard = Double.MAX_VALUE
        var worstOnWash = Double.MAX_VALUE

        palette.forEach { hex ->
            val accent = parseAreaColor(hex)
            val naive = naiveLabelColor(accent, light)
            val wash = ground(accent, light.cardWashActiveAlpha, light.card)
            worstOnBareCard = minOf(worstOnBareCard, contrastRatio(naive, light.card))
            worstOnWash = minOf(worstOnWash, contrastRatio(naive, wash))
        }

        assertTrue(
            "the rejected reading is supposed to look correct: it measured " +
                "$worstOnBareCard to one against the bare card",
            worstOnBareCard >= floor,
        )
        assertTrue(
            "and to fail on the card as drawn, which is the point. It measured " +
                "$worstOnWash to one, and if this is now above the floor then either " +
                "the palette or the wash opacities changed and the finding needs " +
                "re-checking rather than the test relaxing.",
            worstOnWash < 4.0,
        )
    }

    /**
     * The item title, which is the largest thing on an area card and the one a person
     * actually reads. `inkPrimary` at full opacity, so it has a lot of headroom, and the
     * assertion exists to catch a later session reaching for a lighter ink on a card
     * whose wash it did not account for.
     */
    @Test
    fun `the item title clears the floor on every wash`() {
        val failures = worlds.flatMap { world ->
            palette.flatMap { hex ->
                grounds(hex, world).mapNotNull { (name, on) ->
                    val ratio = contrastRatio(world.colors.inkPrimary, on)
                    if (ratio < floor) "${world.name} $hex on $name at $ratio" else null
                }
            }
        }
        assertTrue(failures.joinToString("; "), failures.isEmpty())
    }

    /**
     * `inkSecondary` used to sit on the floor on a resting card, 4.48 to one, and under
     * it on an in-session one, 4.27, both in light mode on `#1E293B`.
     *
     * Phase 3b pinned that as a failure rather than fixing it, and wrote down why:
     * "fixing it would mean raising an ink token's opacity for every screen in the app,
     * which is a change to design-v3.md 3.1 rather than to calm mode". **Phase 3c is
     * that change to 3.1.** The token went from 0.60 to 0.64, required by the new canvas
     * on its own, and closing this was the second thing it bought. Resting is now 5.05
     * to one and in-session 4.75, on every one of the 48 colors in both worlds.
     *
     * The test is kept and inverted rather than deleted, because the reason it existed
     * has not gone away: nothing draws a caption on an in-session card today, so if this
     * regresses no screen shows it. It now asserts clearance so that the day something
     * does land there, the floor is already true.
     */
    @Test
    fun `ink secondary clears the floor on an area card carrying a wash`() {
        val failures = worlds.flatMap { world ->
            palette.flatMap { hex ->
                grounds(hex, world).mapNotNull { (name, on) ->
                    val ink = world.colors.inkSecondary
                    val ratio = contrastRatio(ink.compositeOver(on), on)
                    if (ratio < floor) "${world.name} $hex on $name at $ratio" else null
                }
            }
        }
        assertTrue(
            "below design-v3.md 13's floor of $floor to one: " + failures.joinToString("; ") +
                ". This cleared for the first time in phase 3c, when inkSecondary went " +
                "to 0.64. Lowering it again puts every caption in the app back under " +
                "the floor, not just the ones on an area card.",
            failures.isEmpty(),
        )

        val light = ClarityLightColors
        val inSession = palette.minOf { hex ->
            val on = ground(parseAreaColor(hex), light.cardWashActiveAlpha, light.card)
            contrastRatio(light.inkSecondary.compositeOver(on), on)
        }
        assertTrue(
            "the in session card is supposed to stay the tightest of the grounds and " +
                "measured $inSession to one",
            inSession in 4.65..4.90,
        )
    }

    /**
     * design-v3.md 16.2 excludes the area label from the transform by name, and the
     * exclusion has to survive the token set changing underneath it: `ClarityColors` in
     * calm mode carries different wash opacities, and a label variant computed from
     * those would shift color when the switch was thrown.
     */
    @Test
    fun `the area label is the same color in calm mode as out of it`() {
        worlds.forEach { world ->
            palette.forEach { hex ->
                val accent = parseAreaColor(hex)
                assertEquals(
                    "${world.name} $hex changed its label color in calm mode",
                    areaLabelColor(accent, world.colors),
                    areaLabelColor(accent, world.calm),
                )
            }
        }
    }

    /**
     * design-v3.md 16.2 warns that "reducing saturation moves contrast, and the
     * direction depends on the token", so the movement is bounded rather than assumed to
     * be one way.
     *
     * It moves both ways. In light mode the calm wash is a shallower and less colored
     * ground and every label improves, the largest by 0.30 of a ratio; in dark mode it
     * goes the other way for fourteen of the 48, the largest by 0.19. Neither is a
     * problem, and the assertion is that neither is ever large: a token whose contrast
     * swung by a whole point when the switch was thrown would be a token calm mode is
     * not allowed to touch.
     *
     * **The dark direction is why `areaLabelColor` solves against the calm ground as
     * well.** A move that is small is still a move, and phase 3b's variants were sitting
     * on 0.04 of margin against it.
     */
    @Test
    fun `desaturating the wash moves contrast only slightly, in both directions`() {
        var largestMove = 0.0
        worlds.forEach { world ->
            palette.forEach { hex ->
                val accent = parseAreaColor(hex)
                val label = areaLabelColor(accent, world.colors)
                listOf(
                    ground(accent, world.colors.cardWashAlpha, world.colors.card) to
                        ground(accent.calmed(true), world.calm.cardWashAlpha, world.colors.card),
                    ground(accent, world.colors.cardWashActiveAlpha, world.colors.card) to
                        ground(
                            accent.calmed(true),
                            world.calm.cardWashActiveAlpha,
                            world.colors.card,
                        ),
                ).forEach { (ordinary, calm) ->
                    val move = contrastRatio(label, calm) - contrastRatio(label, ordinary)
                    if (abs(move) > abs(largestMove)) largestMove = move
                }
            }
        }
        assertTrue(
            "calm mode moved a measured contrast ratio by $largestMove",
            abs(largestMove) < 0.5,
        )
    }

    /**
     * A mark drawn on a tinted ground, with the tint desaturated.
     *
     * **This used to be the Trail's event circle and that circle no longer exists.**
     * design-v3.md 11 asked for "a 23dp circle tinted with the event color", phase 3
     * resolved the undefined phrase to the area accent at 12 percent, and phase 12b
     * removed the circle entirely: see the note on `TrailEventRow` in `TrailScreen.kt`,
     * which records that a container able to hold no information is a container. Nothing
     * in `ui/` now puts an ink mark on an accent tint over the canvas.
     *
     * The measurement is kept because the shape it measures is still permitted and still
     * arriving. design-v3.md 3.4 allows an accent as a wash from 3 to 16 percent, 12.1
     * puts a widget's tint at 3 to 5 percent light and 5 to 7 dark, and a mark on one of
     * those is the next thing somebody draws. It measures 4.48 in light and 5.99 in dark
     * against the 3 to 1 graphic floor, and calm mode moves it by 0.01.
     */
    @Test
    fun `an ink mark on a desaturated accent tint clears the graphic floor`() {
        val failures = worlds.flatMap { world ->
            palette.flatMap { hex ->
                val accent = parseAreaColor(hex)
                listOf(accent, accent.calmed(true)).map { tint ->
                    val circle = tint.copy(alpha = 0.12f).compositeOver(world.colors.canvas)
                    val glyph = world.colors.inkSecondary.compositeOver(circle)
                    Triple("${world.name} $hex", contrastRatio(glyph, circle), tint)
                }
            }
        }.filter { it.second < graphicFloor }

        assertTrue(
            "below design-v3.md 13's graphic floor of $graphicFloor to one: " +
                failures.joinToString { "${it.first} at ${it.second}" },
            failures.isEmpty(),
        )
    }

    /**
     * The label variant as it was verified before phase 3b: measured against the bare
     * card token rather than against the card as drawn. Kept here, and only here, so the
     * test above can measure what it costs. It reads `colors.card` live, so it tracks
     * the token rather than freezing the pure white it was written against.
     */
    private fun naiveLabelColor(accent: Color, colors: ClarityColors): Color {
        if (contrastRatio(accent, colors.card) >= floor) return accent
        var amount = 0.25f
        while (amount <= 0.95f) {
            val candidate = accent.blendWith(Color.Black, amount)
            if (contrastRatio(candidate, colors.card) >= floor) return candidate
            amount += 0.05f
        }
        return accent.blendWith(Color.Black, 0.95f)
    }
}
