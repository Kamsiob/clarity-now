package com.kamsiob.claritynow.ui.theme

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import kotlin.math.abs

/**
 * Calm mode's switch and its color transform, design-v3.md 16, issue #48.
 *
 * The contrast half is in `CalmModeContrastTest`, which is the one design-v3.md 16.4
 * calls the place where serving one accessibility need could break another. This file
 * holds the two claims underneath it: that the default really does follow the system,
 * and that the transform really is "less colorful, same lightness".
 *
 * Nothing here needs a device or a composition. `resolveCalmMode` and `Color.calmed`
 * are plain functions for exactly that reason: a rule that can only be checked by
 * looking at a phone is a rule that gets checked once.
 */
class CalmModeTest {

    private val palette = AreaPalette.all.map { parseAreaColor(it) }

    // ---------------------------------------------------------------------------
    // The switch. design-v3.md 16.1.
    // ---------------------------------------------------------------------------

    /**
     * The default is not "off". It is "whatever the system asks for", which is the
     * whole point: the person who most needs calm mode is the person who already
     * turned reduce motion on system wide and will never open this app's settings.
     */
    @Test
    fun `a fresh install follows the system reduce motion setting in both directions`() {
        assertFalse(
            "with nothing stored and the system setting off, calm mode is off",
            resolveCalmMode(stored = null, systemReduceMotion = false),
        )
        assertTrue(
            "with nothing stored and the system setting on, calm mode is on",
            resolveCalmMode(stored = null, systemReduceMotion = true),
        )
    }

    /**
     * design-v3.md 16.1: "The first time the user touches it, it takes a value of its
     * own and stops following." All four combinations, because the interesting two are
     * the ones where the stored value and the system disagree.
     */
    @Test
    fun `an explicit choice stops following the system`() {
        assertTrue(resolveCalmMode(stored = true, systemReduceMotion = false))
        assertTrue(resolveCalmMode(stored = true, systemReduceMotion = true))
        assertFalse(resolveCalmMode(stored = false, systemReduceMotion = true))
        assertFalse(resolveCalmMode(stored = false, systemReduceMotion = false))
    }

    /**
     * design-v3.md 16.1: **reduce motion always wins on motion.** Calm mode off while
     * the system asks for reduced motion restores color and not movement.
     *
     * That rule is one `or` in `clarityMotion`, which needs a composition to call and
     * cannot be exercised here, so the expression is read out of the source instead.
     * Pinning it is worth more than exercising it: an `and`, or a check that read only
     * `LocalCalmMode`, would let a preference inside the app animate against an
     * accessibility setting, and nothing on a screen would look wrong.
     */
    @Test
    fun `the motion flag is the or of the system setting and calm mode`() {
        assertFalse(
            "the color half is off when the user turned calm mode off",
            resolveCalmMode(stored = false, systemReduceMotion = true),
        )

        val source = File("src/main/java/com/kamsiob/claritynow/ui/theme/ClarityMotion.kt")
        assertTrue("expected ${source.path} to exist", source.isFile)
        val text = source.readText().filterNot { it.isWhitespace() }
        assertTrue(
            "clarityMotion must read LocalReduceMotion.current || LocalCalmMode.current, " +
                "which is design-v3.md 8.5's one flag and 16.1's superset rule at once",
            text.contains("if(LocalReduceMotion.current||LocalCalmMode.current)ReducedMotion"),
        )
    }

    // ---------------------------------------------------------------------------
    // The transform. design-v3.md 16.2.
    // ---------------------------------------------------------------------------

    @Test
    fun `the transform is inert when calm mode is off`() {
        palette.forEach { accent ->
            assertEquals(
                "an accent must come back untouched when calm mode is off",
                accent,
                accent.calmed(false),
            )
        }
    }

    /**
     * The claim in design-v3.md 16.2 that the whole design rests on: holding lightness
     * means calm mode cannot break a contrast measurement that passed.
     *
     * OKLab lightness and WCAG relative luminance are not the same quantity, so
     * "lightness held" does not mean "luminance identical". What it has to mean, to be
     * worth anything, is that luminance barely moves, and across all 48 area colors the
     * largest movement is 0.0185 on a zero to one scale. The bound below is that number
     * with room, and it is the number quoted in `CalmMode.kt`.
     */
    @Test
    fun `the transform holds lightness, so no contrast measurement can move far`() {
        val worst = palette.maxOf { accent ->
            abs(relativeLuminance(accent) - relativeLuminance(accent.calmed(true)))
        }
        assertTrue(
            "the transform moved relative luminance by $worst, which is more than " +
                "holding lightness allows. Blending toward grey rather than scaling " +
                "chroma is what this looks like when it goes wrong, and design-v3.md " +
                "16.2 rejects that implementation by name.",
            worst < 0.02,
        )
    }

    /**
     * And the other half: it has to actually desaturate, and by the same amount
     * everywhere, or it is a tint rather than a treatment.
     *
     * The distance between a color's brightest and dimmest channel is a coarse stand in
     * for chroma, and coarse is the point: OKLab chroma scaled by 0.6 should land near
     * 0.6 of the sRGB spread for every hue, and a transform that hit 0.42 on one color
     * and 0.85 on another would read as inconsistent no matter what the math said.
     */
    @Test
    fun `the transform takes roughly forty percent of the color out of every hue`() {
        val ratios = palette.map { accent ->
            val calm = accent.calmed(true)
            spread(calm) / spread(accent)
        }
        val offenders = ratios.filter { it !in 0.35f..0.70f }
        assertTrue(
            "expected every area color to keep between 35 and 70 percent of its " +
                "channel spread with CALM_CHROMA_SCALE at $CALM_CHROMA_SCALE, and " +
                "these did not: $offenders",
            offenders.isEmpty(),
        )
        val mean = ratios.average()
        assertTrue("the mean spread ratio was $mean", mean in 0.45..0.65)
    }

    /** A color with no chroma has nothing to take away, and must come back untouched. */
    @Test
    fun `neutrals are unchanged`() {
        listOf(Color.White, Color.Black, Color(0xFF808080), Color(0xFF404040))
            .forEach { neutral ->
                val calm = neutral.calmed(true)
                assertTrue(
                    "$neutral moved to $calm",
                    abs(neutral.red - calm.red) < 0.01f &&
                        abs(neutral.green - calm.green) < 0.01f &&
                        abs(neutral.blue - calm.blue) < 0.01f,
                )
            }
    }

    @Test
    fun `alpha survives the transform`() {
        // 0.37f does not survive a round trip through Color's 8 bit alpha channel,
        // so the tolerance is a quantization step rather than a float epsilon. What
        // is being asserted is that the transform does not touch alpha at all, not
        // that Color stores more precision than it does.
        val translucent = parseAreaColor("#2D7FF9").copy(alpha = 0.37f)
        assertEquals(translucent.alpha, translucent.calmed(true).alpha, 1f / 255f)
    }

    // ---------------------------------------------------------------------------
    // The token half. design-v3.md 16.2's two pinned rows.
    // ---------------------------------------------------------------------------

    /**
     * The wash opacities are pinned to the low end of the ranges in 3.1 and 3.2, and
     * nothing else in section 3 moves, because design-v3.md 16.4 says calm mode is not
     * a theme. Both halves are asserted, and the second is the one that would rot: a
     * later session adding a calm value for `canvas` or for an ink token would be
     * building a theme and calling it an accessibility setting.
     */
    @Test
    fun `calm mode pins the wash opacities and changes no other token`() {
        val light = ClarityLightColors.calmed()
        // 0.05, which is the low end of 3.1's own `5 to 7 percent` band for the Daylight
        // wash. It was 0.04, below the range the specification states, and that was
        // invisible while the ordinary value sat at the bottom of the band too. The
        // appeal pass moved the ordinary value to 7, and this is the pinning rule this
        // test's own comment describes, applied rather than assumed.
        assertEquals(0.05f, light.cardWashAlpha, 0.0001f)
        assertEquals(0.08f, light.cardWashActiveAlpha, 0.0001f)

        assertEquals(0.09f, light.cardDeckAlpha, 0.0001f)

        val dark = ClarityDarkColors.calmed()
        assertEquals(0.09f, dark.cardWashAlpha, 0.0001f)
        assertEquals(0.16f, dark.cardWashActiveAlpha, 0.0001f)
        assertEquals(0.13f, dark.cardDeckAlpha, 0.0001f)

        listOf(ClarityLightColors to light, ClarityDarkColors to dark).forEach { (ordinary, calm) ->
            assertEquals(
                "calm mode is not a theme, design-v3.md 16.4",
                ordinary,
                calm.copy(
                    cardWashAlpha = ordinary.cardWashAlpha,
                    cardWashActiveAlpha = ordinary.cardWashActiveAlpha,
                    cardDeckAlpha = ordinary.cardDeckAlpha,
                ),
            )
        }
        assertTrue(
            "the calm wash is supposed to be shallower than the ordinary one",
            light.cardWashAlpha < ClarityLightColors.cardWashAlpha &&
                dark.cardWashAlpha < ClarityDarkColors.cardWashAlpha &&
                light.cardDeckAlpha < ClarityLightColors.cardDeckAlpha &&
                dark.cardDeckAlpha < ClarityDarkColors.cardDeckAlpha,
        )
    }

    // ---------------------------------------------------------------------------
    // The gate. design-v3.md 16.5.
    // ---------------------------------------------------------------------------

    /**
     * design-v3.md 16.5 asks for a test that fails the build when an accent reaches the
     * screen without passing through the transform. There is no way to see "an accent
     * reaching the screen" from a source scan, so this checks the thing that actually
     * causes it: a **new** place where an area's stored hex becomes a `Color`.
     *
     * Every existing call site is enumerated below with its disposition, atmosphere or
     * identity, because 16.2's exclusion list is closed and the only way to keep it
     * closed is to make adding a call site a decision somebody has to write down. When
     * this fails, the fix is not to update the number; it is to decide which of the two
     * kinds the new one is, route it accordingly, and then update the number.
     *
     * `washBrush` is left out because it cannot be called from outside its file any
     * more. That is the better kind of enforcement, and where a future accent use can
     * be made structural rather than counted, it should be.
     */
    @Test
    fun `every place an area color becomes a Color is a place someone decided about`() {
        val expected = mapOf(
            // Defines it. The transform is deliberately not here: two of the six
            // exclusions come through this function.
            "ui/theme/AreaPalette.kt" to 1,
            // The 7dp dot and the label, both excluded by name; the wash, through
            // Modifier.areaWash.
            "ui/areas/AreaCard.kt" to 1,
            // **`ui/areas/AreasScreen.kt` was here and is not any more.** It read the
            // area's color to hand it to `SwipeableRow`, which drew the Swap face's
            // 22dp glyph and 10.5sp label in it, transformed. The phase 13 contrast
            // audit measured that pair at 1.029 to one in dark and 1.127 in light and
            // took the accent off the face: 3.4 permits an area color in four forms and
            // an action label is not one of them, and the card being swiped already
            // carries the dot and the area's name. One fewer place is the best outcome
            // this census can have.
            // Three, and all three are identity, so all three are excluded.
            //
            // The 9dp dot in the detail sheet header, the 9dp dot in the long press
            // menu's header, and the 9dp dot on each area in the capture sheet's
            // `Goes to` row. The second arrived with the destination
            // picker: a person choosing where a thought lands recognizes an area by the
            // same mark the card, the archive row and the filing sheet use, which is
            // 3.4's first permitted form. Calm mode desaturates atmosphere and never
            // identity, and a dot that changed color between this row and the card it
            // files into would be the one place the transform made an area harder to
            // recognize rather than quieter.
            "ui/areas/AreaSheets.kt" to 3,
            // The archive row's 7dp dot, design-v3.md 10.20. **Identity, so excluded**,
            // and it is the clearest case the split has: it is the first of 16.2's two
            // exclusions by name, and on this screen it is the only thing on the row
            // that says which area a person is about to restore or delete forever. The
            // card takes no wash at all, because an archived area is idle and 3.4 gives
            // an idle card none, so there is no atmospheric use here to route.
            "ui/areas/ArchiveScreen.kt" to 1,
            // The manage screen's row dot, at the same 9dp and the same two alphas the
            // Areas card and the archive row use. **Identity, so excluded**, and this is
            // the screen where the case is strongest: the whole job of that list is
            // telling one area from another in order to put them in an order, and a dot
            // that changed color between this row and the card it moves would be the one
            // place the transform made an area harder to recognize rather than quieter.
            "ui/areas/ManageAreasScreen.kt" to 1,
            // The area dot on each row of the filing chooser. Identity, so excluded:
            // it is how a person recognizes which area they are about to file into,
            // and 16.2 names the dot as one of the two uses that never transform.
            "ui/areas/InboxSheet.kt" to 1,
            // The live preview's wash and label, plus the swatches and the selection
            // ring, which show the true color because that is what is being chosen.
            "ui/areas/ColorPicker.kt" to 4,
            // Two, and both are identity, so neither takes the transform. The filter
            // chip's dot has always been one. **The visual refresh added the row's own
            // dot**, which replaced the glyph column: the glyph restated the verb the
            // sentence had already written, and the dot answers the one question the
            // sentence does not, which is which area this belongs to. It is the same
            // device as the card's dot at the same size and 16.2 puts it on the same
            // side of the split. Phase 12b's removal of the event circle's 12 percent
            // tint still stands and is a different thing: that was an accent at *wash*
            // strength being asked to carry identity, which is what fails.
            "ui/trail/TrailScreen.kt" to 2,
            // Onboarding. The preview card's accent is atmosphere and takes the
            // transform; the 7dp dot beside the name is identity and never does. Both
            // files already read LocalCalmMode at the site, which is where the split
            // is actually made.
            "ui/onboarding/OnboardingBeatOne.kt" to 1,
            "ui/onboarding/OnboardingControls.kt" to 2,
            // The mood strip's slivers, the swatch a person taps, and the swatch the
            // check is measured against. **Not transformed**, on the precedent phase 3c
            // set for the color picker: a swatch is a choice, and showing a desaturated
            // version of a color while someone is choosing that color would be showing
            // them the wrong answer. The third use is the phase 13 check color: the
            // check reads the true accent because it is drawn on the true accent, and a
            // check chosen against a transformed swatch would be the wrong ink on the
            // swatch as drawn.
            "ui/onboarding/OnboardingColorRows.kt" to 3,
            // The appearance picker's miniature dots. Identity, and its own KDoc says
            // so: a miniature of a dot is a picture of the same thing.
            "ui/settings/AppearancePicker.kt" to 1,
            // Both are the area dot: the one on a chooser row and the one above the
            // ring. Identity, so both excluded. The Focus surface takes no area wash
            // at all, because design-v3.md 11 allows it six elements and a tinted
            // ground is not one of them.
            "ui/focus/FocusChooserScreen.kt" to 1,
            "ui/focus/FocusSessionScreen.kt" to 1,
            // Momentum. One use, and it is identity.
            //
            // **The 60 percent area tile was the second and the visual refresh deletes
            // it.** It was atmosphere and went through `calmAccent`, correctly; what
            // could not be defended was its size. Two 52dp blocks were the loudest
            // objects in the app and each carried one bit, directly above an Area balance
            // module that listed the same areas with real figures against them.
            //
            // What is left is the dot beside a name in an insight module, which is
            // **identity** and never transforms. It is the first of 3.4's four uses and
            // the first of 16.2's two exclusions by name, and it is how a person tells
            // which area a row is about.
            "ui/momentum/MomentumScreen.kt" to 1,
        )

        val root = File("src/main/java/com/kamsiob/claritynow/ui")
        assertTrue(
            "expected the ui sources at ${root.path}, and this run is in " +
                File("").absolutePath + ". Without them this test passes vacuously.",
            root.isDirectory,
        )
        val found = root.walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .mapNotNull { file ->
                val uses = file.readText().split("parseAreaColor(").size - 1
                if (uses == 0) null else file.path.substringAfter("claritynow/") to uses
            }
            .toMap()

        assertEquals(
            "a place where an area color becomes a Color was added or removed. " +
                "design-v3.md 16.2 splits these into atmosphere, which takes the " +
                "transform, and identity, the 7dp dot and the label, which never " +
                "does. Decide which this one is, route it, and record it here.",
            expected,
            found,
        )
    }

    private fun spread(color: Color): Float =
        maxOf(color.red, color.green, color.blue) - minOf(color.red, color.green, color.blue)
}
