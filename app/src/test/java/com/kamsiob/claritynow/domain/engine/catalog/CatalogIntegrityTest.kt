package com.kamsiob.claritynow.domain.engine.catalog

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Catalog integrity, over the real corpus files. CLARITY_LOGIC_ENGINE.md 14 and
 * `MASTER_BUILD_PROMPT.md` 17.
 *
 * These are the checks that belong here rather than in the validator, because they are
 * true or false of the catalog as a whole rather than of one candidate sentence. A
 * validator can only veto what it is handed; nothing hands it a family that has no rule.
 */
class CatalogIntegrityTest {

    @Test
    fun `the catalog passes every integrity check`() {
        val findings = CatalogIntegrity.checkAll(CorpusFixture.catalog)
        assertTrue(
            findings.joinToString("\n") { "${it.check}: ${it.detail}" },
            findings.isEmpty(),
        )
    }

    @Test
    fun `every rule points at an existing family and an existing stage`() {
        val findings = CatalogIntegrity.rulesPointAtExistingFamilies(CorpusFixture.catalog)
        assertTrue(findings.joinToString("\n"), findings.isEmpty())
    }

    @Test
    fun `every family has a rule, or is recorded as awaiting a fact that does not exist`() {
        val findings = CatalogIntegrity.everyFamilyHasARule(CorpusFixture.catalog)
        assertTrue(findings.joinToString("\n"), findings.isEmpty())

        val silent = CorpusFixture.catalog.families
            .filter { CorpusFixture.catalog.rulesOf(it.purpose, it.key).isEmpty() }
            .map { it.key }
            .toSet()
        assertEquals(
            "a family with authored language and no rule is silent, and a silent family looks " +
                "exactly like one that never happened to qualify. Nine of them were, and the " +
                "facts phase declared what their triggers named, so the register is empty and " +
                "every family in the corpus now has a rule. A family appearing in this set is a " +
                "rule being lost; a family appearing in RulesAwaitingFacts is a decision that " +
                "one may be",
            RulesAwaitingFacts.FAMILIES_WITHOUT_RULES,
            silent,
        )
        assertTrue(
            "no family is silent, which is the state the facts phase was for. If this ever " +
                "fails, read the message above before adding a register entry to make it pass",
            silent.isEmpty(),
        )
    }

    @Test
    fun `no duplicate rule keys and no duplicate variant keys`() {
        val findings = CatalogIntegrity.duplicateKeys(CorpusFixture.catalog)
        assertTrue(findings.joinToString("\n"), findings.isEmpty())
    }

    @Test
    fun `specificity is the criteria count and is never authored`() {
        val findings = CatalogIntegrity.specificityIsCriteriaSize(CorpusFixture.catalog)
        assertTrue(findings.joinToString("\n"), findings.isEmpty())
        assertTrue(
            "every rule requires at least two things, because a rule that required one would " +
                "rank last anyway and would still be a sentence about a person's week",
            CorpusFixture.catalog.rules.all { it.specificity >= 2 },
        )
    }

    @Test
    fun `every share based rule carries a minimum event floor`() {
        val findings = CatalogIntegrity.shareRulesCarryAFloor(CorpusFixture.catalog)
        assertTrue(findings.joinToString("\n"), findings.isEmpty())
        assertTrue(
            "at least one rule reads a share, or this test is passing on nothing",
            CorpusFixture.catalog.rules.any { rule ->
                rule.criteria.any { it.id.startsWith(SHARE_READING_PREFIX) }
            },
        )
    }

    @Test
    fun `every slot the corpus uses has a declared production source`() {
        val findings = CatalogIntegrity.undeclaredSlots(CorpusFixture.catalog)
        assertTrue(findings.joinToString("\n"), findings.isEmpty())
    }

    @Test
    fun `every numeric slot carries the fact reference the validator re-reads`() {
        for (slot in SlotProduction.NUMERIC) {
            val source = requireNotNull(SlotProduction.sourceFor(slot))
            assertTrue("{$slot} renders a number and must carry a FactRef", source.factRef != null)
        }
        val nameSlots = setOf("areaName", "otherArea", "thirdArea", "itemTitle", "priorLabel")
        for (slot in nameSlots) {
            val source = requireNotNull(SlotProduction.sourceFor(slot))
            assertEquals(
                "{$slot} is a snapshot string, not a number. Snapshot usage is enforced " +
                    "structurally by the realizer only ever seeing the FactSet, per validator check 5",
                null,
                source.factRef,
            )
        }
    }

    @Test
    fun `no fragment appears in two families of the same purpose, beyond the recorded six`() {
        val findings = CatalogIntegrity.fragmentsInTwoFamilies(CorpusFixture.catalog)
        assertTrue(
            "a new shared fragment. 7.7 gives a clause bench to exactly one family, because " +
                "shared phrasing is how the seams become visible:\n" +
                findings.joinToString("\n") { it.detail },
            findings.isEmpty(),
        )
        assertEquals(
            "the recorded set is a debt list for phase 9 and should only ever shrink",
            6,
            KnownCorpusViolations.SHARED_FRAGMENTS.size,
        )
    }

    @Test
    fun `no rhetorical construction appears in more than two families, beyond the recorded two`() {
        val findings = CatalogIntegrity.constructionsInMoreThanTwoFamilies(CorpusFixture.catalog)
        assertTrue(findings.joinToString("\n") { it.detail }, findings.isEmpty())
        assertEquals(
            "two of the four shapes checked are over the cap today",
            2,
            KnownCorpusViolations.CONSTRUCTION_ALLOWANCE.size,
        )
    }

    @Test
    fun `the fragment check would catch a new collision`() {
        // A guard on the guard. A check whose failure branch never runs is a check nobody
        // has verified, which is the same argument CLARITY_LOGIC_ENGINE.md 8 makes about
        // the validator's veto path.
        val collided = CorpusFixture.pulseText.replace(
            "spread.s1.01  [P]  Yesterday touched {areaCount} areas.",
            "spread.s1.01  [P]  Nothing has moved past {itemTitle} yet.",
        )
        assertTrue("the replacement did not apply", collided != CorpusFixture.pulseText)
        val catalog = ClarityCatalog.build(collided, CorpusFixture.reportText, CorpusFixture.momentumText)
        val findings = CatalogIntegrity.fragmentsInTwoFamilies(catalog)
        assertTrue("expected a finding, got none", findings.isNotEmpty())
    }

    @Test
    fun `a rule pointing at a qualitative stage carries criteria of its own`() {
        val findings = CatalogIntegrity.qualitativeStagesCarryTheirOwnCriteria(CorpusFixture.catalog)
        assertTrue(findings.joinToString("\n"), findings.isEmpty())
    }

    @Test
    fun `ranking is specificity, then priority, then key`() {
        val ranked = CorpusFixture.catalog.rulesFor(Purpose.PULSE)
        val expected = ranked.sortedWith(
            compareByDescending<ClarityRule> { it.criteria.size }
                .thenByDescending { it.priority }
                .thenBy { it.key },
        )
        assertEquals(expected.map { it.key }, ranked.map { it.key })
    }

    @Test
    fun `every purpose has rules`() {
        for (purpose in Purpose.entries) {
            assertTrue("$purpose has no rules", CorpusFixture.catalog.rulesFor(purpose).isNotEmpty())
        }
    }
}
