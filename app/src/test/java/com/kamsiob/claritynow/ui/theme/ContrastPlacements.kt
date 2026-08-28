package com.kamsiob.claritynow.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * The pairs that are not "a body ink on a ground", named one at a time with the file
 * that draws them.
 *
 * The two rules in [ContrastAuditTest] are total on their own axes: every ground is
 * measured against its world's running text, and every ink token is measured against the
 * three ranks of its world's surface ladder. What neither reaches is a foreground that
 * is not an ink token on a ground that is not a surface, which is most of the colored
 * furniture in the app: a white label on a filled button, an area's accent on the face
 * of a swipe action, the mark on its plate.
 *
 * **Every entry cites the file it is read from.** A failure that says `ClarityTabBar.kt`
 * is a failure somebody can go and fix; a failure that says `actionBlue` is a palette
 * argument.
 */

/**
 * design-v3.md 10.9's check, named by which of the two inks the swatch took, so a failure
 * says whether the white half or the ink half of [swatchCheckColor] missed.
 */
private fun swatchCheck(accent: Color): Swatch {
    val check = swatchCheckColor(accent)
    return Swatch(if (check == Color.White) "swatch check, white" else "swatch check, ink", check)
}

internal fun daylightPlacements(name: String, c: ClarityColors): List<Measured> {
    val page = Swatch("$name canvas", c.canvas)
    val chrome = Swatch("$name raise", c.raise)
    val content = Swatch("$name card", c.card)
    val text = ContrastAudit.TEXT_FLOOR
    val graphic = ContrastAudit.GRAPHIC_FLOOR
    // design-v3.md 10.3.1. Each face is its own action's token at its base alpha,
    // deepened by 40 percent past the commit threshold, which is the deepest the ground
    // under its 22dp glyph and its 10.5sp label ever gets. A swipe face is therefore the
    // one ground in the app where a token has to be legible on its own tint.
    val swapFace = Swatch("$name Swap face", ContrastAudit.over(c.actionBlue, 0.12f * SWIPE_DEEPEN, page.color))
    val completeFace = Swatch("$name Complete face", ContrastAudit.over(c.positiveGreen, 0.18f * SWIPE_DEEPEN, page.color))
    val deleteFace = Swatch("$name Delete face", ContrastAudit.over(c.deleteMuted, 0.13f * SWIPE_DEEPEN, page.color))
    val out = mutableListOf<Measured>()

    AreaPalette.all.forEach { hex ->
        val accent = parseAreaColor(hex)
        val label = Swatch("$name label $hex", areaLabelColor(accent, c))

        // design-v3.md 3.4. The label sits on the card carrying its own area's wash, at
        // every depth 3.1 and 3.2 permit, which since phase 13 is exactly the depth 3.4
        // pins it against, with the calm transform on and off. 16.2 excludes the label
        // itself from the transform, so it is one color in both.
        listOf(false, true).forEach { calm ->
            val tint = accent.calmed(calm)
            washPercents(c.isDark).forEach { percent ->
                val washedCard = Swatch(
                    "$name card, $hex at $percent percent${if (calm) ", calm" else ""}",
                    ContrastAudit.over(tint, percent / 100f, content.color),
                )
                out += Measured(
                    ink = label,
                    ground = washedCard,
                    floor = text,
                    where = "design-v3.md 3.4, the area label on its own card, AreaCard.kt",
                )
                // The 7dp dot sits on the same washed card, one row above the label, and
                // that is a third ground for it: the rows below put it on the three ranks
                // of the ladder, where a chip and a Focus chooser row draw it, and this is
                // where an area card draws it. Not asserted for the same reason, 3.4.
                out += Measured(
                    ink = Swatch("$name dot $hex", accent),
                    ground = washedCard,
                    floor = null,
                    where = "design-v3.md 3.4 and 10.3, the 7dp dot on the card carrying " +
                        "that area's own wash, AreaCard.kt. **Not asserted, and 3.4 is " +
                        "why**: \"never adjust the dot or wash to compensate\", and the " +
                        "card names the area in text beside it.",
                )
            }
        }

        // design-v3.md 3.4 and 16.2. The dot is identity and never takes the transform,
        // so there is one dot color and it lands on all three ranks of the ladder.
        listOf(page, chrome, content).forEach { on ->
            out += Measured(
                ink = Swatch("$name dot $hex", accent),
                ground = on,
                floor = null,
                where = "design-v3.md 3.4 and 10.8, the 7dp area dot on a chip and on the " +
                    "Focus chooser rows. **Not asserted, and 3.4 is why**: it names the only " +
                    "permitted remedy for an area color that misses the floor, \"darken the " +
                    "label variant\", and then closes the question, \"never adjust the dot " +
                    "or wash to compensate\". design-v3.md 13 carries it: the chip and the " +
                    "row both name the area in text beside the dot, so color is not the " +
                    "only signal.",
            )
        }

        // design-v3.md 10.9's check on the selected swatch, over the swatch's own color
        // at full strength. 16.7 keeps the grid untransformed. 10.9 asked for a white
        // check and 17 of the 48 swatches cannot hold one, so the ink is chosen per
        // swatch and the audit measures the one the app will actually draw.
        out += Measured(
            ink = swatchCheck(accent),
            ground = Swatch("$name swatch $hex", accent),
            floor = graphic,
            where = "design-v3.md 10.9, the selected swatch's check, ColorPicker.kt, and the " +
                "same check on onboarding's color rows, OnboardingColorRows.kt",
        )
    }

    // design-v3.md 10.3.1. Each face's icon and label, in the token its own ground is
    // tinted from. The Swap face used to carry the area's accent instead, which failed on
    // 43 of the 48 colors in light and 28 in dark, worst at 1.029: see the note on
    // SwipeableRow.kt for why the remedy 3.4 names does not reach this ground.
    out += Measured(
        Swatch("$name actionBlue", c.actionBlue), swapFace, text,
        "design-v3.md 10.3.1, the Swap action's label at 10.5sp and its 22dp glyph, SwipeableRow.kt",
    )
    out += Measured(
        Swatch("$name positiveInk", c.positiveInk), completeFace, text,
        "design-v3.md 10.3.1, the Complete action's label at 10.5sp and its check, SwipeableRow.kt",
    )
    out += Measured(
        Swatch("$name deleteMuted", c.deleteMuted), deleteFace, text,
        "design-v3.md 10.3.1, the Delete action's label at 10.5sp and its glyph, SwipeableRow.kt",
    )

    listOf(page, content).forEach { under ->
        out += Measured(
            Swatch("$name positiveInk", c.positiveInk),
            Swatch("$name positive button on ${under.name}", ContrastAudit.over(c.positiveGreen, 0.13f, under.color)),
            text,
            "design-v3.md 10.7's positive role: a positiveInk label on a 13 percent " +
                "positiveGreen fill, Buttons.kt, used by the queue sheet, AreaSheets.kt",
        )
        out += Measured(
            Swatch("$name actionBlue", c.actionBlue), under, text,
            "design-v3.md 10.7's tertiary role, Buttons.kt. Also the undo snackbar's " +
                "action at 17sp, UndoSnackbar.kt, the sheet actions at 12sp in " +
                "AreaSheets.kt and InboxSheet.kt, the appearance picker's selected label, " +
                "AppearancePicker.kt, and the conflict banner at 17sp, AreasScreen.kt",
        )
        out += Measured(
            Swatch("$name positiveInk", c.positiveInk), under, graphic,
            "design-v3.md 13, \"completed Trail events carry the check icon\": the check " +
                "is the non-color half of the completion signal, AreaSheets.kt and TrailScreen.kt",
        )
    }

    out += Measured(
        Swatch("$name actionBlue", c.actionBlue),
        Swatch("$name tab pill", ContrastAudit.over(c.actionBlue, 0.10f, chrome.color)),
        text,
        "design-v3.md 10.4, the selected tab's label at 13sp and its filled icon, ClarityTabBar.kt",
    )
    out += Measured(
        Swatch("$name inkSecondary", c.inkSecondary), chrome, text,
        "design-v3.md 10.4, an unselected tab's label, ClarityTabBar.kt",
    )
    // design-v3.md 10.7 and 3.1: a filled action surface inverts its label to `card`,
    // which is the inversion the destructive button and the selected chip already take.
    // White measured 3.808 here in light and 2.626 in dark, and no blue is both light
    // enough to be read on the dark ladder and dark enough to hold white.
    out += Measured(
        Swatch("$name card", c.card), Swatch("$name actionBlue fill", c.actionBlue), text,
        "design-v3.md 10.7, the primary button's inverted label at 17sp, Buttons.kt",
    )
    out += Measured(
        Swatch("$name card", c.card), Swatch("$name actionBlue fill", c.actionBlue), graphic,
        "design-v3.md 10.5, the FAB's add glyph, and 10.10's filled check badge on the selected appearance tile",
    )
    out += Measured(
        Swatch("$name card", c.card), Swatch("$name inkPrimary fill", c.inkPrimary), text,
        "design-v3.md 10.7 and 10.8, the destructive button's label and a selected chip's label",
    )
    out += Measured(
        Swatch("mark foreground", MarkForeground), Swatch("mark background", MarkBackground), graphic,
        "design-v3.md 4.2, the mark on its plate, AboutScreen.kt",
    )
    out += Measured(
        Swatch("support accent", SupportAccent), Swatch("$name parchment", c.parchment), graphic,
        "MASTER_BUILD_PROMPT 14.5, the support block's icon, SupportBlock.kt",
    )
    out += Measured(
        Swatch("white", Color.White), Swatch("support accent fill", SupportAccent), text,
        "MASTER_BUILD_PROMPT 14.5, the support button's label at 17sp, SupportBlock.kt",
    )
    out += Measured(
        Swatch("$name warnAmber", c.warnAmber), chrome, ROLES.getValue("$name.warnAmber").graphicFloor,
        "design-v3.md 10.1, the Pulse ready dot on a chip, PulseChip.kt. " + ROLES.getValue("$name.warnAmber").why,
    )
    out += Measured(
        Swatch("$name hairline", c.hairline), content, ROLES.getValue("$name.hairline").graphicFloor,
        "design-v3.md 3.1, a row separator. " + ROLES.getValue("$name.hairline").why,
    )
    return out
}

internal fun contemplativePlacements(calm: Boolean): List<Measured> {
    val t = ClarityContemplativeColors
    val night = t.deepBlack
    val suffix = if (calm) ", calm" else ""
    val text = ContrastAudit.TEXT_FLOOR
    val graphic = ContrastAudit.GRAPHIC_FLOOR
    fun tint(color: Color) = color.calmed(calm)
    val gold = tint(ReportPalette.gold)
    val amber = tint(PulsePalette.accent)

    val focusField = listOf(
        Swatch("focus gradient center$suffix", tint(FocusPalette.gradientCenter)),
        Swatch("focus gradient mid$suffix", tint(FocusPalette.gradientMid)),
        Swatch("focus gradient edge$suffix", tint(FocusPalette.gradientEdge)),
    )
    val reportField = if (calm) {
        listOf(Swatch("report glow, calm", ContrastAudit.over(gold, 0.04f, night)))
    } else {
        listOf(
            Swatch("report headline glow", ContrastAudit.over(gold, 0.07f, night)),
            Swatch("report closing glow", ContrastAudit.over(gold, 0.045f, night)),
        )
    }
    val pulseField = if (calm) {
        listOf(Swatch("pulse midday, calm", night))
    } else {
        listOf(
            Swatch("pulse midday", night),
            Swatch("pulse dawn", ContrastAudit.over(PulsePalette.dawnTint, 0.55f, night)),
            Swatch("pulse evening", ContrastAudit.over(PulsePalette.eveningTint, 0.55f, night)),
        )
    }

    val out = mutableListOf<Measured>()

    focusField.forEach { field ->
        out += Measured(
            Swatch("focus.ringProgress$suffix", tint(FocusPalette.ringProgress)), field, text,
            "design-v3.md 3.3 and 11.3. The arc, and the label on a Contemplative text action, FocusControls.kt",
        )
        out += Measured(
            Swatch("focus.ringTip$suffix", tint(FocusPalette.ringTip)), field, graphic,
            "design-v3.md 3.3, the point of light at the head of the arc, FocusRing.kt",
        )
        out += Measured(
            Swatch("focus.ringTrack", FocusPalette.ringTrack), field, ROLES.getValue("focus.ringTrack").graphicFloor,
            "design-v3.md 3.3. " + ROLES.getValue("focus.ringTrack").why,
        )
    }
    reportField.forEach { field ->
        out += Measured(
            Swatch("report.gold$suffix", gold), field, text,
            "design-v3.md 11.1, the eyebrow and the closing rule, ReportScreen.kt",
        )
        out += Measured(
            Swatch("report.body", ReportPalette.body), field, text,
            "design-v3.md 3.3, the Report's serif body, ReportScreen.kt",
        )
        out += Measured(
            Swatch("report.gold at 50 percent$suffix", gold.copy(alpha = 0.5f)), field, graphic,
            "design-v3.md 11.1, the quietest day's mark in the ribbon, WeekRibbon.kt. A drawn " +
                "rectangle rather than type, so it takes the graphic floor, and the caption " +
                "beneath states the numbers the ribbon shows, design-v3.md 13",
        )
    }
    pulseField.forEach { field ->
        out += Measured(
            Swatch("pulse.accent$suffix", amber), field, text,
            "design-v3.md 3.3 and 11, the Pulse option labels and the acknowledgment, PulseSurface.kt",
        )
        out += Measured(
            Swatch("pulse.accent at 55 percent$suffix", amber.copy(alpha = 0.55f)), field, graphic,
            "design-v3.md 11, a quiet day's 3dp mark in the rhythm row, PulseRhythmRow.kt. " +
                "A drawn circle rather than type. It was 50 percent, which clears on the " +
                "midday ground at 3.003 and misses on the dawn one at 2.969: 3.3 blends a " +
                "whisper of #2B2340 into the top of this surface from 05 to 11, so the " +
                "floor has to be met at every hour rather than at the one the mark was " +
                "measured against. 55 percent is design-v3.md 13's own number for " +
                "Contemplative material meant to be read",
        )
    }
    listOf(
        Swatch("contemplative.textBright", t.textBright) to text,
        Swatch("report.gold$suffix", gold) to text,
        Swatch("pulse.accent$suffix", amber) to text,
    ).forEach { (inkSwatch, floor) ->
        listOf(
            Swatch("report pill$suffix", ContrastAudit.over(gold, 0.14f, night)),
            Swatch("pulse pill$suffix", ContrastAudit.over(amber, 0.14f, night)),
            Swatch("onboarding chip$suffix", ContrastAudit.over(Color.White, 0.16f, night)),
        ).forEach { field ->
            out += Measured(
                inkSwatch, field, floor,
                "design-v3.md 11.1's Report pills, 11's Pulse response pill and onboarding's " +
                    "chips: a soft fill with its own label on it, ReportBlocks.kt, " +
                    "PulseResponsePill.kt and OnboardingControls.kt",
            )
        }
    }
    AreaPalette.all.forEach { hex ->
        out += Measured(
            Swatch("dot $hex", parseAreaColor(hex)), Swatch("deepBlack", night), null,
            "design-v3.md 3.4 and 16.2, the 7dp dot on the Focus chooser rows and on " +
                "onboarding's color rows. Not asserted for the reason 3.4 gives: \"never " +
                "adjust the dot or wash to compensate\", and the row names the area in text",
        )
        out += Measured(
            swatchCheck(parseAreaColor(hex)),
            Swatch("swatch $hex", parseAreaColor(hex)), graphic,
            "design-v3.md 10.9, the check on a selected swatch, OnboardingColorRows.kt",
        )
    }
    out += Measured(
        Swatch("contemplative.textDim", t.textDim), Swatch("deepBlack$suffix", night), text,
        "the jump in label at the foot of the onboarding route, OnboardingRoute.kt. It was " +
            "white at 30 percent, which measures 2.643, and MASTER_BUILD_PROMPT 13.1 still " +
            "states that opacity: design-v3.md wins on anything visual and 13 states one " +
            "floor for text, so the label takes the token 3.3 already gives to secondary " +
            "Contemplative type",
    )
    out += Measured(
        Swatch("white at 35 percent", Color.White.copy(alpha = 0.35f)), Swatch("deepBlack$suffix", night), graphic,
        "the back chevron in the same row, OnboardingRoute.kt. A glyph rather than a word, " +
            "so it takes design-v3.md 13's graphic floor, and its label is spoken",
    )
    out += Measured(
        Swatch("contemplative.textFaint", t.textFaint), Swatch("deepBlack", night),
        ROLES.getValue("contemplative.textFaint").textFloor,
        "design-v3.md 13. " + ROLES.getValue("contemplative.textFaint").why,
    )
    out += Measured(
        Swatch("onboarding.beatFourBlue$suffix", tint(OnboardingPalette.beatFourBlue)),
        Swatch("onboarding beat four glow$suffix", ContrastAudit.over(tint(OnboardingPalette.beatFourBlue), 0.09f, night)),
        graphic,
        "design-v3.md 3.3, the rhythm dots on beat four, OnboardingBeatFour.kt",
    )
    return out
}
