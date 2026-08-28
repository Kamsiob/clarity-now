package com.kamsiob.claritynow.domain.engine.catalog

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The two family level policies the engine owns rather than the corpus: the cooldown table
 * in CLARITY_LOGIC_ENGINE.md 7.3, and the `unflattering` enumeration in 7.4.
 *
 * **Both are parsed out of the specification rather than transcribed into an expectation.**
 * A test that repeated the table in Kotlin would pass forever, including on the day someone
 * edited 7.3 and not the catalog. So the table is read from the document, which makes the
 * document the source and the test the check.
 *
 * 7.4 is prose rather than a table and cannot be parsed the same way, so the check there is
 * the weaker one that every family it names by name is still named in it. That catches the
 * change that actually happens, which is a family being added to or removed from the
 * sentence, and it will need replacing when Addendum 01 7c widens the enumeration in phase
 * 9. See [UnflatteringRules] for what that widening is and why it has not landed.
 */
class FamilyPolicyTest {

    @Test
    fun `every Pulse family declares the cooldown 7 point 3 gives it`() {
        val fromTheSpecification = COOLDOWN_ROW
            .findAll(CorpusFixture.logicEngineText)
            .associate { it.groupValues[1] to it.groupValues[2].toInt() }
        assertEquals(
            "the table in 7.3 lists eleven Pulse families",
            EngineFamilies.PULSE.size,
            fromTheSpecification.size,
        )
        for (family in CorpusFixture.pulse.families) {
            assertEquals(
                "${family.key} cooldown",
                fromTheSpecification[family.key],
                family.cooldownDays,
            )
        }
    }

    @Test
    fun `every Report family declares the flat cooldown, with the two exceptions 7 point 3 names`() {
        for (family in CorpusFixture.report.families) {
            val expected = when (family.key) {
                "selfReportVsData" -> EngineFamilies.NEVER_REPEATS_COOLDOWN_DAYS
                "hardStretch" -> EngineFamilies.HARD_STRETCH_COOLDOWN_DAYS
                else -> EngineFamilies.REPORT_DEFAULT_COOLDOWN_DAYS
            }
            assertEquals("${family.key} cooldown", expected, family.cooldownDays)
        }
        assertTrue(
            "7.3 still says the Report is a flat fourteen days with those two exceptions",
            CorpusFixture.logicEngineText.contains(
                "Report families use a flat 14 days per `(family, subjectId)`, except `selfReportVsData`",
            ),
        )
        assertTrue(
            "6.4 still puts hardStretch at six weeks",
            CorpusFixture.logicEngineText.contains("`hardStretch` at 42 days per 6.4"),
        )
    }

    @Test
    fun `Momentum and the banner declare no cooldown, because 7 point 3 lists none`() {
        val surfaces = CorpusFixture.momentum.families
        assertTrue(surfaces.isNotEmpty())
        assertTrue(
            "a cooldown here would be a second throttle disagreeing with the one 6.5 puts in " +
                "the ViewModel",
            surfaces.all { it.cooldownDays == EngineFamilies.NO_COOLDOWN },
        )
    }

    @Test
    fun `every family named in the 7 point 4 enumeration is still named in it`() {
        val enumeration = CorpusFixture.logicEngineText
            .substringAfter("**Which rules carry `unflattering = true`.**")
            .substringBefore("Everything else is `false`.")
        assertTrue("7.4's enumeration paragraph was not found", enumeration.length in 100..2000)
        val named = UnflatteringRules.WHOLE_FAMILY +
            UnflatteringRules.BY_STAGE.keys +
            UnflatteringRules.SPLIT_AT_MAGNITUDE.keys
        val missing = named.filterNot { enumeration.contains("`$it`") }
        assertTrue("families the catalog marks that 7.4 no longer names: $missing", missing.isEmpty())
    }

    @Test
    fun `every rule's unflattering flag matches the enumeration`() {
        val findings = CatalogIntegrity.unflatteringMatchesTheEnumeration(CorpusFixture.catalog)
        assertTrue(findings.joinToString("\n"), findings.isEmpty())
    }

    @Test
    fun `no neutral or positive family carries the flag`() {
        val flagged = CorpusFixture.catalog.rules.filter { it.unflattering }.map { it.family }.toSet()
        val positive = setOf(
            "personalBest", "mostActiveSince", "risingActivity", "clearing", "netOutflow",
            "focusProtected", "improvingThroughput", "comeback", "areaRevival", "queueDrained",
            "firstMilestone", "balanced", "areaBalance", "steadyStretch", "strongPace",
            "balancedWeek", "weekStrong", "weekBuilding",
        )
        val wrong = flagged intersect positive
        assertTrue(
            "making the fact the subject of a good week reads as withholding credit, per 7.4: $wrong",
            wrong.isEmpty(),
        )
    }

    @Test
    fun `every unflattering rule belongs to a family the corpus gave a neutral agent bench, or is recorded as awaiting one`() {
        val withoutNeutralLines = CorpusFixture.catalog.rules
            .filter { it.unflattering }
            .mapNotNull { rule ->
                val family = CorpusFixture.catalog.familyFor(rule) ?: return@mapNotNull rule.key
                val stage = family.stage(rule.stage ?: 1) ?: return@mapNotNull rule.key
                val hasNeutral = (stage.variants + stage.extensions).any { it.register == Register.NEUTRAL_AGENT }
                if (hasNeutral) null else rule.family
            }
            .toSet()
        assertEquals(
            "7.4 says the neutral agent variant is preferred where the family has one at the " +
                "selected stage, and falls through to reflective, observational or plain where it " +
                "does not. MASTER_BUILD_PROMPT 14b.10 assigned the missing benches to phase 9, " +
                "which wrote them for `neglectedArea` stage 1, `queuePressure`, `persistentItem`, " +
                "`focusAbandonment` and `intakeVsOutput` stage 1. What is left is the eight the " +
                "corpus format cannot reach. A family joining this set is a corpus regression; a " +
                "family leaving it now needs a change to the format rather than a batch of lines",
            setOf(
                // Eight families that no author can take off this list, and it is a
                // property of the corpus format rather than of anybody's diligence.
                // `CORPUS_2_REPORT.md` carries a register tag in section 2 alone, and
                // `ReportWalker` refuses one on a headline or a pattern line, so every
                // variant in those two sections is PLAIN by construction and step 4 of 7.4
                // is what they get. `quietWeek`, `decliningActivity` and `queuePressure`
                // are here for their headline rule and not their observation one, whose
                // benches all three now carry `[N]` lines. Recorded in 7.4 with the
                // argument.
                "quietWeek",
                "decliningActivity",
                "queuePressure",
                "growingQueues",
                "areaGoneQuiet",
                "narrowingFocus",
                "focusHabitFading",
                "abandonmentPattern",
            ),
            withoutNeutralLines,
        )
    }

    private companion object {
        val COOLDOWN_ROW = Regex("""^\|\s*`(\w+)`\s*\|\s*(\d+)\s*\|.*\|$""", RegexOption.MULTILINE)
    }
}
