package com.kamsiob.claritynow.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * What each color token is allowed to be, and the floor it has to clear when it is that
 * thing. One entry per token, and [ContrastAuditTest] fails until every token the theme
 * declares has one.
 *
 * A `null` floor does not mean unmeasured. Every pair this package builds is measured;
 * a null floor means the pair is not asserted on, and [why] then has to carry the
 * sentence of the specification that permits it. **Nothing escapes measurement. Some
 * things escape assertion, by name, with a reason.**
 */
internal data class Role(
    val textFloor: Double?,
    val graphicFloor: Double?,
    val isGround: Boolean,
    val why: String,
) {
    /** The floor rule A holds this token to on a neutral surface, or null. */
    val inkFloor: Double? get() = textFloor ?: graphicFloor
}

private fun ink(why: String) = Role(ContrastAudit.TEXT_FLOOR, ContrastAudit.GRAPHIC_FLOOR, false, why)
private fun inkAndGround(why: String) =
    Role(ContrastAudit.TEXT_FLOOR, ContrastAudit.GRAPHIC_FLOOR, true, why)
private fun graphic(why: String) = Role(null, ContrastAudit.GRAPHIC_FLOOR, false, why)
private fun graphicAndGround(why: String) = Role(null, ContrastAudit.GRAPHIC_FLOOR, true, why)
private fun ground(why: String) = Role(null, null, true, why)
private fun measuredOnly(why: String) = Role(null, null, false, why)

/**
 * The roles, by the name [ContrastAudit.allThemeTokens] gives each token.
 *
 * **Read the nulls.** Each one is a decision, and each one is why a gate elsewhere in
 * this package exists: a token that may never carry text needs something to prove that
 * nothing draws text in it, and a floor cannot prove that. [FaintInkTest] does.
 */
internal val ROLES: Map<String, Role> = buildMap {
    listOf("light", "dark").forEach { world ->
        put("$world.canvas", ground("design-v3.md 3.1 and 3.2, the page, and a focused field's well"))
        put("$world.raise", ground("design-v3.md 3.1, the floating tab bar, an unselected chip, a resting field's well"))
        put(
            "$world.card",
            ground(
                "design-v3.md 3.1, cards and sheets. It is also the inverted label on an " +
                    "inkPrimary fill, design-v3.md 10.7 and 10.8, which is a named " +
                    "placement rather than a general ink: it lands on exactly one ground.",
            ),
        )
        put("$world.parchment", ground("design-v3.md 3.1, the weekly banner"))
        put(
            "$world.inkPrimary",
            inkAndGround(
                "design-v3.md 3.1. Titles and item text, and the fill of a selected chip " +
                    "and of the destructive button",
            ),
        )
        put(
            "$world.inkSecondary",
            ink(
                "design-v3.md 3.1. Captions, timestamps, the unselected tab label, and a " +
                    "field's placeholder since phase 12b",
            ),
        )
        put(
            "$world.inkTertiary",
            measuredOnly(
                "design-v3.md 3.1: \"never on text\", and \"it survives as an opacity for " +
                    "shapes that are not read\". It measures 2.40 to one on the light card, " +
                    "so it clears no floor this document states and is not asked to. What " +
                    "holds it to that is the source gate in FaintInkTest, not a number.",
            ),
        )
        put(
            "$world.hairline",
            measuredOnly(
                "design-v3.md 3.1: \"row separators only\". A separator is not a graphical " +
                    "object under WCAG 1.4.11: nothing in a list is understood through it, " +
                    "and design-v3.md 6.1 has an element carry it instead of a shadow " +
                    "rather than in addition to the spacing that already separates the rows.",
            ),
        )
        put(
            "$world.actionBlue",
            inkAndGround(
                "design-v3.md 3.1. The FAB, the active tab, primary buttons, the label of " +
                    "every text button in the app, and the Swap swipe face and the label on " +
                    "it. One value carries all of that in each world, which is why the light " +
                    "value moved to #004BAE: a token that is both a fill and a foreground " +
                    "has to be legible on the ladder, on its own 10 percent tab pill and on " +
                    "its own swipe face, and a filled surface inverts its label to `card` " +
                    "rather than asking the token to hold white as well",
            ),
        )
        put(
            "$world.positiveGreen",
            ground(
                "design-v3.md 3.1, completion only, and **a fill only**: the positive " +
                    "button at 13 percent, the Trail's mint row at 8, the Complete swipe " +
                    "face at 18. Every one of those has to stay light enough for what sits " +
                    "on it, and design-v3.md 11 calls the Trail's ground a mint. As a " +
                    "foreground the same value measured 1.833 on the light canvas and 1.680 " +
                    "as the positive button's own label, so the foreground is a second " +
                    "token, positiveInk. What holds this one to a fill is the source gate " +
                    "in FaintInkTest, not a number.",
            ),
        )
        put(
            "$world.positiveInk",
            ink(
                "design-v3.md 3.1 and 10.3.1. Every green foreground in the app: the " +
                    "completion check, the positive button's label, and the Complete swipe " +
                    "face's icon and label, where it replaces the literal #15803D that " +
                    "10.3.1 named and that measured 3.403 on the face it was drawn on",
            ),
        )
        put(
            "$world.warnAmber",
            measuredOnly(
                "design-v3.md 3.1, \"the Pulse ready dot, nothing else\". design-v3.md 13 " +
                    "requires that color is never the only signal and names this case: " +
                    "\"the Pulse ready state is a dot plus a changed chip label\". The dot " +
                    "restates a label change rather than carrying the state alone, so WCAG " +
                    "1.4.11 does not reach it. The number is measured and printed anyway.",
            ),
        )
        put(
            "$world.deleteMuted",
            inkAndGround(
                "design-v3.md 3.1, the delete action wherever it is offered: the swipe " +
                    "face and its label, the detail sheet's Delete row and the archive " +
                    "row's. The swipe face is the ground and the icon and label on that " +
                    "face are the ink, which is why it is one value per world since " +
                    "phase 13: at #8A5A5A in both it measured 2.942 on the dark card and " +
                    "3.656 on its own face in light",
            ),
        )
    }

    put("contemplative.deepBlack", ground("design-v3.md 3.3, the Contemplative ground"))
    put("contemplative.surfaceRaised", ground("design-v3.md 3.3"))
    put("contemplative.textBright", ink("design-v3.md 3.3, Contemplative reading text"))
    put(
        "contemplative.textDim",
        ink("design-v3.md 3.3, and design-v3.md 13's floor of 55 percent for text meant to be read"),
    )
    put(
        "contemplative.textFaint",
        measuredOnly(
            "design-v3.md 13: \"Contemplative text stays at or above 55 percent opacity " +
                "where it is meant to be read\". At 32 percent this token is under that " +
                "line by construction, so it is the Contemplative twin of inkTertiary: an " +
                "opacity for shapes, held there by the source gate in FaintInkTest rather " +
                "than by a floor.",
        ),
    )

    put("focus.gradientCenter", ground("design-v3.md 3.3, the Focus backdrop"))
    put("focus.gradientMid", ground("design-v3.md 3.3"))
    put("focus.gradientEdge", ground("design-v3.md 3.3"))
    put(
        "focus.ringTrack",
        measuredOnly(
            "design-v3.md 3.3, white at 16 percent, the unfilled part of the ring. The " +
                "time remaining is stated by the numeral inside it, design-v3.md 11.3, so " +
                "the track is what the arc is measured against and not a carrier of the " +
                "reading.",
        ),
    )
    put(
        "focus.ringProgress",
        ink("design-v3.md 3.3. The depleting arc, and the label on a Contemplative text action, FocusControls.kt"),
    )
    put("focus.ringTip", graphic("design-v3.md 3.3, the filled point of light at the head of the arc"))

    put("pulse.accent", ink("design-v3.md 3.3. The Pulse amber, on option labels, the pill fill and the rhythm row"))
    put("pulse.dawnTint", ground("design-v3.md 3.3, a time of day blend into the Pulse ground"))
    put("pulse.eveningTint", ground("design-v3.md 3.3"))

    put("report.gold", ink("design-v3.md 3.3 and 11.1. The eyebrow, the ribbon's marks, the rules and the pills"))
    put("report.body", ink("design-v3.md 3.3, the Report's serif body"))

    put("onboarding.beatOne", ground("design-v3.md 3.3, one glow per beat, drawn at 9 percent behind the beat"))
    put("onboarding.beatTwo", ground("design-v3.md 3.3"))
    put("onboarding.beatFourAmber", ground("design-v3.md 3.3"))
    put(
        "onboarding.beatFourBlue",
        graphicAndGround("design-v3.md 3.3. Beat four's glow, and the rhythm dots drawn on it, OnboardingBeatFour.kt"),
    )
    put("onboarding.beatFourGold", ground("design-v3.md 3.3"))

    // The file level vals in ClarityColors.kt. They reach the audit through
    // ContrastAudit.fileLevelTokens rather than through a token holder, which is the one
    // way a color could otherwise enter the theme unclassified.
    put("file.MarkBackground", ground("design-v3.md 4.2, the plate the mark is drawn on"))
    put("file.MarkForeground", graphic("design-v3.md 4.2, the mark itself"))
    put(
        "file.SupportAccent",
        graphicAndGround(
            "MASTER_BUILD_PROMPT 14.5. The one warm accent in the app: the support " +
                "block's icon on parchment, and the fill of its button, which carries a " +
                "white label",
        ),
    )
    put(
        "file.InkLight",
        graphic(
            "`#17171C` is the base the light world's three ink tokens are struck from, and " +
                "it reaches a screen as inkPrimary, inkSecondary, inkTertiary or hairline, " +
                "each of which is classified above at the alpha it is actually drawn at. " +
                "It has one placement of its own: design-v3.md 10.9's check on a selected " +
                "swatch is drawn in this or in white, whichever reads on that swatch, and a " +
                "swatch belongs to neither world's ladder so it cannot take a world's ink " +
                "token. That pair is measured as a placement, on all 48 swatches, which is " +
                "the only ground this value is ever drawn on directly.",
        ),
    )
    put(
        "file.InkDark",
        measuredOnly("not a token. The dark world's half of the note on file.InkLight."),
    )
}

/** A ground the app can draw, and the members it expands to. */
internal data class GroundFamily(val name: String, val where: String, val members: List<Swatch>)

/** One world's grounds and the inks that carry its running text. */
internal data class AuditWorld(
    val name: String,
    val bodyInks: List<Swatch>,
    val families: List<GroundFamily>,
)

/**
 * Rule B pairs that are measured but not asserted, keyed by body ink token and ground
 * family. Every entry is a sentence of the specification, not a judgment about how it
 * looks.
 *
 * These exist because rule B's default is deliberately aggressive: **every ground in a
 * world must be legible to that world's running text**, which is the right default
 * because most grounds do carry running text and a new one usually will. A ground that
 * does not has to say so here.
 */
internal val RULE_B_EXEMPTIONS: Map<Pair<String, String>, String> = buildMap {
    listOf("light", "dark").forEach { w ->
        listOf("inkPrimary", "inkSecondary").forEach { token ->
            put(
                "$w.$token" to "action fill",
                "design-v3.md 10.5 and 10.7. A filled actionBlue surface carries a white " +
                    "label and a white glyph, both measured as placements. No ink token is " +
                    "ever drawn on one.",
            )
            put(
                "$w.$token" to "ink fill",
                "design-v3.md 10.7 and 10.8. A solid inkPrimary pill inverts its label to " +
                    "`card`, measured as a placement. Ink on ink is not a pair the app can make.",
            )
            put(
                "$w.$token" to "the mark",
                "design-v3.md 4.2 puts one glyph on the mark's plate and nothing else.",
            )
            put(
                "$w.$token" to "support",
                "MASTER_BUILD_PROMPT 14.5. The support button is a filled accent with a " +
                    "white label, measured as a placement. The block's prose sits on " +
                    "parchment beside it, which is the `banner` family.",
            )
            put(
                "$w.$token" to "area tile",
                "design-v3.md 3.4 and MomentumScreen.kt: the 60 percent tile is empty. The " +
                    "area's name sits beneath it on the page, in inkSecondary, which the " +
                    "`page` family measures.",
            )
            put(
                "$w.$token" to "swipe action faces",
                "design-v3.md 10.3.1 and SwipeableRow.kt: a face carries its own icon and " +
                    "its own 10.5sp label, measured as placements, and the card slides " +
                    "over it rather than sitting on it. No app text is drawn on a face.",
            )
        }
    }
}

// ---------------------------------------------------------------------------
// The grounds, generated from the ranges the design permits
// ---------------------------------------------------------------------------

/**
 * The wash strengths a Daylight world can put an area accent on a card at, as whole
 * percents.
 *
 * **A range, not the two values the code happens to use.** design-v3.md 3.1 gives light 5
 * to 7 resting and 12 to 13 in session, 3.2 gives dark 7 to 9 and 15 to 16, 8.2 item 1's
 * promotion peaks at 11, and 12.1 takes a widget's tint down to 3 percent light and 5
 * dark on the same content plane. Measuring the whole span rather than the shipped pair
 * is what makes a later move inside a range the design already states unable to introduce
 * a failure this audit has not seen.
 *
 * **There used to be two of these functions and phase 13 closed the gap between them.**
 * 3.1 and 3.2 read "12 to 14 percent" and "15 to 17" while 3.4 solves the area label
 * against "the deepest opacity the design permits, 13 percent in light and 16 in dark",
 * and at the extra point three light labels fall under design-v3.md 13's floor. Nothing
 * drew it, because `cardWashActiveAlpha` is 13 and 16, so it was a trapdoor under a token
 * that looked safe to nudge rather than a defect on a screen. It is closed by narrowing
 * the two tables to the depth the label is actually solved against, which moves no pixel
 * the app draws, rather than by re-solving 48 labels against a depth nothing uses.
 * `the wash range 3_1 permits is exactly the depth the label variant is solved against`
 * in [ContrastAuditTest] holds both halves of that.
 */
internal fun washPercents(isDark: Boolean): IntRange = if (isDark) 5..16 else 3..13

/** design-v3.md 3.4 and 16.7. Momentum's tile. */
private const val TILE_PERCENT = 60

/**
 * design-v3.md 3.1, 3.2, 10.2, 10.3, 10.4, 10.7, 10.8 and 11: every ground a Daylight
 * world can put behind something. Each family names the file that draws it, so a failure
 * points at a screen rather than at a palette.
 */
internal fun daylightWorld(name: String, c: ClarityColors): AuditWorld {
    val page = Swatch("$name canvas", c.canvas)
    val chrome = Swatch("$name raise", c.raise)
    val content = Swatch("$name card", c.card)

    return AuditWorld(
        name = name,
        bodyInks = listOf(
            Swatch("$name.inkPrimary", c.inkPrimary),
            Swatch("$name.inkSecondary", c.inkSecondary),
        ),
        families = listOf(
            GroundFamily("page", "design-v3.md 3.1, the page behind every Daylight screen", listOf(page)),
            GroundFamily(
                "chrome",
                "design-v3.md 3.1 and 10.4. The floating tab bar, an unselected chip, a resting field's well",
                listOf(chrome),
            ),
            GroundFamily(
                "content",
                "design-v3.md 3.1, 10.3 and 10.14. Area cards, sheets, the undo snackbar",
                listOf(content),
            ),
            GroundFamily(
                "banner",
                "design-v3.md 3.1 and 10.2, the weekly banner, and MASTER_BUILD_PROMPT 14.5's support block",
                listOf(
                    Swatch("$name parchment", c.parchment),
                    Swatch("$name parchment, warmed", ContrastAudit.over(SupportAccent, 0.08f, c.parchment)),
                ),
            ),
            GroundFamily(
                "area wash on content",
                "design-v3.md 3.4 and 10.3, an area card carrying its own accent, and 12.1's " +
                    "widget tints, which sit on the same content plane",
                AreaPalette.all.flatMap { hex ->
                    val accent = parseAreaColor(hex)
                    listOf(false, true).flatMap { calm ->
                        val tint = accent.calmed(calm)
                        washPercents(c.isDark).map { percent ->
                            Swatch(
                                "$name card, $hex at $percent percent${if (calm) ", calm" else ""}",
                                ContrastAudit.over(tint, percent / 100f, content.color),
                            )
                        }
                    }
                },
            ),
            GroundFamily(
                "area tile",
                "design-v3.md 3.4's 60 percent tile, MomentumScreen.kt",
                AreaPalette.all.flatMap { hex ->
                    listOf(false, true).map { calm ->
                        Swatch(
                            "$name tile $hex${if (calm) ", calm" else ""}",
                            ContrastAudit.over(parseAreaColor(hex).calmed(calm), TILE_PERCENT / 100f, content.color),
                        )
                    }
                },
            ),
            GroundFamily(
                "action fill",
                "design-v3.md 10.5 and 10.7, the FAB and the primary button",
                listOf(Swatch("$name actionBlue fill", c.actionBlue)),
            ),
            GroundFamily(
                "ink fill",
                "design-v3.md 10.7 and 10.8, the destructive button and a selected chip",
                listOf(Swatch("$name inkPrimary fill", c.inkPrimary)),
            ),
            GroundFamily(
                "soft fills",
                "design-v3.md 10.7's secondary and positive buttons, and 11's completed Trail row",
                listOf(
                    Swatch("$name inkPrimary at 5 percent on content", ContrastAudit.over(c.inkPrimary, 0.05f, content.color)),
                    Swatch("$name inkPrimary at 5 percent on the page", ContrastAudit.over(c.inkPrimary, 0.05f, page.color)),
                    Swatch("$name inkPrimary at 10 percent on content", ContrastAudit.over(c.inkPrimary, 0.10f, content.color)),
                    Swatch("$name positiveGreen at 13 percent on content", ContrastAudit.over(c.positiveGreen, 0.13f, content.color)),
                    Swatch("$name positiveGreen at 13 percent on the page", ContrastAudit.over(c.positiveGreen, 0.13f, page.color)),
                    Swatch("$name positiveGreen at 8 percent on content", ContrastAudit.over(c.positiveGreen, 0.08f, content.color)),
                ),
            ),
            GroundFamily(
                "tab pill",
                "design-v3.md 10.4, ClarityTabBar.kt: the selected pill is actionBlue at 10 percent on the bar",
                listOf(Swatch("$name tab pill", ContrastAudit.over(c.actionBlue, 0.10f, chrome.color))),
            ),
            GroundFamily(
                "swipe action faces",
                "design-v3.md 10.3.1, SwipeableRow.kt. The action layer sits on the page " +
                    "behind the card, and each face fades in to its base alpha and deepens " +
                    "by 40 percent past the commit threshold",
                listOf(
                    "Complete" to (c.positiveGreen to 0.18f),
                    "Swap" to (c.actionBlue to 0.12f),
                    "Delete" to (c.deleteMuted to 0.13f),
                ).map { (label, spec) ->
                    val (tint, base) = spec
                    Swatch("$name $label face", ContrastAudit.over(tint, base * SWIPE_DEEPEN, page.color))
                },
            ),
            GroundFamily("the mark", "design-v3.md 4.2, the app mark's plate", listOf(Swatch("mark background", MarkBackground))),
            GroundFamily("support", "MASTER_BUILD_PROMPT 14.5, the one link in About", listOf(Swatch("support accent fill", SupportAccent))),
        ),
    )
}

/**
 * design-v3.md 3.3 and 11: the Contemplative world, which is always dark and which
 * carries five different fields of light rather than one flat ground.
 */
internal fun contemplativeWorld(calm: Boolean): AuditWorld {
    val t = ClarityContemplativeColors
    val night = t.deepBlack
    val suffix = if (calm) ", calm" else ""
    fun tint(color: Color) = color.calmed(calm)
    val gold = tint(ReportPalette.gold)
    val amber = tint(PulsePalette.accent)

    return AuditWorld(
        name = "contemplative${if (calm) " calm" else ""}",
        bodyInks = listOf(
            Swatch("contemplative.textBright", t.textBright),
            Swatch("contemplative.textDim", t.textDim),
        ),
        families = listOf(
            GroundFamily(
                "night",
                "design-v3.md 3.3, the Contemplative ground and its raised surface",
                listOf(Swatch("deepBlack", night), Swatch("surfaceRaised", t.surfaceRaised)),
            ),
            GroundFamily(
                "focus field",
                "design-v3.md 3.3, the indigo radial. Its three stops are three grounds",
                listOf(
                    Swatch("focus gradient center$suffix", tint(FocusPalette.gradientCenter)),
                    Swatch("focus gradient mid$suffix", tint(FocusPalette.gradientMid)),
                    Swatch("focus gradient edge$suffix", tint(FocusPalette.gradientEdge)),
                ),
            ),
            GroundFamily(
                "pulse field",
                "design-v3.md 3.3, the amber night with its time of day blends at 55 percent. " +
                    "16.7 holds the blends at midday in calm mode rather than transforming them",
                if (calm) {
                    listOf(Swatch("pulse midday, calm", night))
                } else {
                    listOf(
                        Swatch("pulse midday", night),
                        Swatch("pulse dawn", ContrastAudit.over(PulsePalette.dawnTint, 0.55f, night)),
                        Swatch("pulse evening", ContrastAudit.over(PulsePalette.eveningTint, 0.55f, night)),
                    )
                },
            ),
            GroundFamily(
                "report field",
                "design-v3.md 11.1, two gold glows behind the page, one in calm mode",
                if (calm) {
                    listOf(Swatch("report glow, calm", ContrastAudit.over(gold, 0.04f, night)))
                } else {
                    listOf(
                        Swatch("report headline glow", ContrastAudit.over(gold, 0.07f, night)),
                        Swatch("report closing glow", ContrastAudit.over(gold, 0.045f, night)),
                    )
                },
            ),
            GroundFamily(
                "onboarding field",
                "design-v3.md 3.3, one glow per beat at 9 percent",
                listOf(
                    "beat one" to OnboardingPalette.beatOne,
                    "beat two" to OnboardingPalette.beatTwo,
                    "beat four amber" to OnboardingPalette.beatFourAmber,
                    "beat four blue" to OnboardingPalette.beatFourBlue,
                    "beat four gold" to OnboardingPalette.beatFourGold,
                ).map { (label, glow) ->
                    Swatch("onboarding $label$suffix", ContrastAudit.over(tint(glow), 0.09f, night))
                },
            ),
            GroundFamily(
                "contemplative soft fills",
                "the onboarding pill and chip in white, OnboardingControls.kt; the Report's " +
                    "ground block, pill and settled pill in gold, ReportBlocks.kt; the Pulse " +
                    "response pill in amber, PulseResponsePill.kt. Every one of them carries " +
                    "a label",
                listOf(
                    Swatch("white at 4 percent$suffix", ContrastAudit.over(Color.White, 0.04f, night)),
                    Swatch("white at 7 percent$suffix", ContrastAudit.over(Color.White, 0.07f, night)),
                    Swatch("white at 9 percent$suffix", ContrastAudit.over(Color.White, 0.09f, night)),
                    Swatch("white at 16 percent$suffix", ContrastAudit.over(Color.White, 0.16f, night)),
                    Swatch("report ground block$suffix", ContrastAudit.over(gold, 0.045f, night)),
                    Swatch("report settled pill$suffix", ContrastAudit.over(gold, 0.07f, night)),
                    Swatch("report pill$suffix", ContrastAudit.over(gold, 0.14f, night)),
                    Swatch("pulse pill$suffix", ContrastAudit.over(amber, 0.14f, night)),
                ),
            ),
        ),
    )
}
