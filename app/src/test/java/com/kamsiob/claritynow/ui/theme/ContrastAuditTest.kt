package com.kamsiob.claritynow.ui.theme

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * **Contrast, as a computed number, everywhere.** design-v3.md 13 and issue #51.
 *
 * Phase 3 shipped a 4.40 to one failure on a screen that looked completely correct, and
 * it was found by computing the number rather than by looking at it. Issue #51 names the
 * second risk as well: treating contrast as done because it passed once, when calm mode
 * changes saturation and every one of the 48 area colors has to be re-measured under it.
 *
 * So this file does not check the pairs somebody remembered to list. It runs three rules,
 * each total on its own axis, over four worlds:
 *
 * | rule | what is total about it | where it lives |
 * |---|---|---|
 * | A | every color token the theme declares, found by reflection | [AuditRun.inkOnTheLadder] |
 * | B | every ground the app can draw, against its world's running text | [AuditRun.runningTextOnEveryGround] |
 * | C | the pairs that are neither, each named with the file that draws it | [AuditRun.namedPlacements] |
 *
 * The four worlds are Daylight light, Daylight dark, and Contemplative with calm mode off
 * and on. Calm is not a fifth column bolted on the end: the transform is applied where
 * design-v3.md 16.2 says it is applied, so every area wash, every Contemplative gradient
 * and every glow is measured twice, and design-v3.md 16.4's "this is the one place where
 * serving one accessibility need could break another" is a run of the suite rather than a
 * sentence.
 *
 * **What makes it stay total.** Adding a token to `ClarityColors` fails
 * `every color token the theme declares has a role` until somebody says what the token
 * is. Adding a ground family puts it in front of the world's running text automatically.
 * A pair that is deliberately not asserted on carries the sentence of the specification
 * that permits it, in [ROLES] or [RULE_B_EXEMPTIONS], and is still measured and still
 * printed in a failure message.
 */
class ContrastAuditTest {

    // -----------------------------------------------------------------------
    // Coverage: the audit cannot be narrowed without failing
    // -----------------------------------------------------------------------

    /**
     * The reflection gate. Every `Color` the theme declares is enumerated from the
     * declared fields of the token holders, so a new one arrives here whether or not
     * anybody remembers this file.
     */
    @Test
    fun `every color token the theme declares has a role`() {
        val unclassified = ContrastAudit.allThemeTokens().keys.filter { ROLES[it] == null }
        assertTrue(
            "these tokens reached the theme with nothing said about what they are: " +
                unclassified.joinToString() +
                ". Add an entry to ROLES saying whether it carries text, carries a " +
                "graphic, is only ever a ground, or is measured and not asserted on with " +
                "the sentence of the specification that permits that. Do not delete this " +
                "test: the point of it is that a color cannot enter the app without " +
                "somebody deciding what it is allowed to sit on.",
            unclassified.isEmpty(),
        )
    }

    /** And the reverse, so a token that leaves the theme does not leave a rule behind. */
    @Test
    fun `every role names a token the theme still declares`() {
        val declared = ContrastAudit.allThemeTokens().keys
        val orphans = ROLES.keys.filterNot { it in declared }
        assertTrue("ROLES describes tokens that no longer exist: " + orphans.joinToString(), orphans.isEmpty())
    }

    /** An exemption that names nothing exempts nothing, and reads as though it does. */
    @Test
    fun `every exemption names a real ink and a real ground family`() {
        val inks = AuditRun.worlds().flatMap { it.bodyInks }.map { it.name }.toSet()
        val families = AuditRun.worlds().flatMap { it.families }.map { it.name }.toSet()
        val stale = RULE_B_EXEMPTIONS.keys.filterNot { it.first in inks && it.second in families }
        assertTrue(
            "these rule B exemptions match no pair the audit builds, so they are " +
                "silently exempting nothing: " + stale.joinToString(),
            stale.isEmpty(),
        )
        val reasonless = RULE_B_EXEMPTIONS.filterValues { it.length < 40 }.keys
        assertTrue(
            "an exemption is a sentence of the specification, not a shrug: " + reasonless.joinToString(),
            reasonless.isEmpty(),
        )
    }

    /**
     * The audit enumerates rather than samples, and this is the assertion that says so in
     * numbers. It is deliberately coarse: it cannot tell a good enumeration from a bad
     * one, but it can tell that somebody replaced one with a list of twelve pairs.
     */
    @Test
    fun `the audit enumerates rather than samples`() {
        val all = AuditRun.everything()
        assertTrue("the audit built only ${all.size} pairs", all.size > 8_000)

        AreaPalette.all.forEach { hex ->
            assertTrue(
                "$hex is one of design-v3.md 3.4's 48 colors and the audit never measured it",
                all.any { it.ink.name.contains(hex) || it.ground.name.contains(hex) },
            )
        }
        listOf("light", "dark", "contemplative", "contemplative calm").forEach { world ->
            assertTrue(
                "the $world world produced no grounds",
                AuditRun.worlds().any { it.name == world && it.families.isNotEmpty() },
            )
        }
        assertTrue(
            "no pair was measured with the calm mode transform applied, which is the one " +
                "thing design-v3.md 16.4 says has to be measured rather than assumed",
            all.count { it.ground.name.contains("calm") } > 1_000,
        )
        assertEquals(
            "every ground family is supposed to be reachable and non empty",
            emptyList<String>(),
            AuditRun.worlds().flatMap { it.families }.filter { it.members.isEmpty() }.map { it.name },
        )
    }

    // -----------------------------------------------------------------------
    // The measurement
    // -----------------------------------------------------------------------

    /**
     * design-v3.md 13's floor, on every pair the app can put on a screen.
     *
     * The message is the deliverable. It is sorted worst first and every line names the
     * ink, the ground, the measured ratio, the floor it missed and the file that draws
     * it, because a contrast failure that says `actionBlue` is a palette argument and one
     * that says `ClarityTabBar.kt` is something a person can go and fix.
     */
    @Test
    fun `every pair the app can put on screen clears design-v3 13's floor`() {
        val failures = AuditRun.everything().filter { it.fails }
        val lines = failures
            .groupBy {
                Triple(
                    it.ink.name.replace(HEX, "an area color"),
                    it.ground.name.replace(HEX, "an area color"),
                    it.where,
                )
            }
            .entries
            .map { (key, group) -> group.minByOrNull { it.ratio }!! to group.size }
            .sortedBy { it.first.ratio }
            .map { (worst, count) ->
                "%.3f to one against a floor of %.1f%s: %s on %s. %s".format(
                    worst.ratio,
                    worst.floor,
                    if (count > 1) " ($count of these)" else "",
                    worst.ink.name,
                    worst.ground.name,
                    worst.where,
                )
            }
        assertTrue(
            "${failures.size} measured pairs are under design-v3.md 13's floor, worst " +
                "first:\n" + lines.joinToString("\n") +
                "\n\nEvery number above is computed from the shipped tokens by the app's " +
                "own WCAG implementation, quantized to the 8 bits per channel a Compose " +
                "sRGB color actually holds. None of it is a judgment about how a screen " +
                "looks.",
            failures.isEmpty(),
        )
    }

    /**
     * The margins, which are what a future token change breaks first.
     *
     * A pair sitting on 4.53 is not a pass, it is a pass that a one point change to a
     * wash opacity or a two point change to a canvas will turn into a failure. Phase 3c
     * is the proof: it moved `canvas` by four L* and took `inkSecondary` from 4.50 to
     * 4.33 on a screen nobody had touched.
     */
    @Test
    fun `the tightest passing pairs still have a margin`() {
        val passing = AuditRun.everything().filter { it.floor != null && !it.fails }
        val text = passing.filter { it.floor == ContrastAudit.TEXT_FLOOR }.sortedBy { it.ratio }
        val graphic = passing.filter { it.floor == ContrastAudit.GRAPHIC_FLOOR }.sortedBy { it.ratio }

        val tightestText = text.first()
        assertTrue(
            "the tightest passing text pair is $tightestText, and the two behind it are " +
                "${text[1]} and ${text[2]}. It is supposed to sit just above the floor: " +
                "well above it means the label variants or the ink tokens moved, and " +
                "below it means the assertion above should have caught it first.",
            tightestText.ratio in 4.50..4.80,
        )
        val tightestGraphic = graphic.first()
        assertTrue(
            "the tightest passing graphic pair is $tightestGraphic, and the two behind it " +
                "are ${graphic[1]} and ${graphic[2]}. Three thousandths of a ratio is not " +
                "a margin, and this is the pair to look at first when a Contemplative " +
                "token moves.",
            tightestGraphic.ratio in 3.00..3.30,
        )
    }

    /**
     * **The finding this test used to pin, and the sentence that closed it.**
     *
     * design-v3.md 3.4 solves the area label variant against "the deepest opacity the
     * design permits, 13 percent in light and 16 in dark", and `areaLabelColor` does
     * exactly that. Through phase 12b design-v3.md 3.1 and 3.2 permitted one point more
     * than either: their tables read "12 to 14 percent" and "15 to 17", and at that extra
     * point three of the light labels fall under design-v3.md 13's floor on a card whose
     * wash never left the range the design states. Nothing drew it, because
     * `cardWashActiveAlpha` is 13 and 16, so it was never a defect a person could see; it
     * was a trapdoor under a token that looked safe to nudge.
     *
     * **Phase 13 closed it by narrowing the two tables to 13 and 16**, which is the depth
     * the label is solved against, rather than by re-solving 48 labels against a depth
     * nothing uses. The narrower statement moves no pixel the app draws and the wider one
     * would have moved every area label in the app; where a specification and a solved
     * value disagree by one point and the value is the one that was measured, the
     * sentence is what moves. design-v3.md 15's open-choice rule is not in play here,
     * because this was a contradiction rather than an open choice.
     *
     * Both halves stay asserted, in the world that narrowing left behind. The first is
     * that every depth the design now permits clears the floor. The second is that the
     * point beyond it still does not, which is what makes the narrowing load bearing: if
     * the extra point ever became free, this test says so rather than quietly passing.
     */
    @Test
    fun `the wash range 3_1 permits is exactly the depth the label variant is solved against`() {
        data class Dip(val world: String, val hex: String, val percent: Int, val ratio: Double)

        val dips = mutableListOf<Dip>()
        var worstAtThePermittedDepth = Double.MAX_VALUE

        listOf("light" to ClarityLightColors, "dark" to ClarityDarkColors).forEach { (name, c) ->
            val permitted = washPercents(c.isDark)
            AreaPalette.all.forEach { hex ->
                val accent = parseAreaColor(hex)
                val label = areaLabelColor(accent, c)
                listOf(false, true).forEach { calm ->
                    val tint = accent.calmed(calm)
                    (permitted.first..permitted.last + 1).forEach { percent ->
                        val ratio = ContrastAudit.ratio(label, ContrastAudit.over(tint, percent / 100f, c.card))
                        if (percent in permitted) {
                            worstAtThePermittedDepth = minOf(worstAtThePermittedDepth, ratio)
                        } else if (ratio < ContrastAudit.TEXT_FLOOR) {
                            dips += Dip(name, hex, percent, ratio)
                        }
                    }
                }
            }
        }

        assertEquals(
            "design-v3.md 3.1 and 3.2 permit an in-session wash exactly as deep as 3.4 " +
                "solves the label against, and these two numbers are how that is held. If " +
                "a table in section 3 is deepened again, deepen this and read the second " +
                "assertion below before deciding it is free.",
            listOf(13, 16),
            listOf(washPercents(false).last, washPercents(true).last),
        )
        assertTrue(
            "at the deepest wash design-v3.md 3.1 and 3.2 permit, the worst of the 48 " +
                "labels measures $worstAtThePermittedDepth to one. If this is under 4.5 " +
                "then areaLabelColor stopped solving against the right ground and the " +
                "audit above will already be red.",
            worstAtThePermittedDepth >= ContrastAudit.TEXT_FLOOR,
        )
        assertTrue(
            "one point deeper than the tables now permit, these labels go under the " +
                "floor: " +
                dips.joinToString { "${it.world} ${it.hex} at ${it.percent} percent, ${"%.3f".format(it.ratio)}" } +
                ". That is why 3.1 and 3.2 stop where they do, and it is the whole cost of " +
                "the one point phase 13 took off each of them. If this list is ever empty, " +
                "the label variant gained headroom and the range could widen again, which " +
                "is worth knowing rather than worth deleting.",
            dips.isNotEmpty(),
        )
    }

    private companion object {
        val HEX = Regex("#[0-9A-Fa-f]{6}")
    }
}
