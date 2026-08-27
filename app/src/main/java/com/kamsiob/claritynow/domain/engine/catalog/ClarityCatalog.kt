package com.kamsiob.claritynow.domain.engine.catalog

import com.kamsiob.claritynow.domain.engine.FamilyKey

/**
 * Layer 2, assembled: the rules and the language they select from, in one object built at
 * load and never mutated. CLARITY_LOGIC_ENGINE.md 2 and 4.
 *
 * The two halves stay separate on purpose. [rules] hold no strings and can be tested
 * against generated fact sets with no corpus present at all; [families] hold nothing but
 * strings and can be checked for voice, register and length with no rule present. They
 * meet at exactly two points, both of them checked by [CatalogIntegrity]: a rule names a
 * family, and a rule names a stage in it.
 *
 * Built by [build] from the three corpus files' text. Nothing here opens a file, so the
 * whole catalog can be constructed in a unit test from a string, which is what makes the
 * corpus edits in phase 9 testable before they ship.
 */
class ClarityCatalog private constructor(
    val families: List<PhrasingFamily>,
    val rules: List<ClarityRule>,
    val auxiliary: Map<String, List<CorpusLine>>,
    val prefixes: Map<String, FamilyKey>,
    val skipped: List<SkippedSection>,
) {

    private val familyIndex: Map<Pair<Purpose, FamilyKey>, PhrasingFamily> =
        families.associateBy { it.purpose to it.key }

    private val rulesByPurpose: Map<Purpose, List<ClarityRule>> =
        Purpose.entries.associateWith { purpose ->
            rules.filter { purpose in it.purpose }.sortedWith(ClarityRule.RANKING)
        }

    /** The family a rule speaks through, or null when the rule points at nothing. */
    fun familyFor(rule: ClarityRule): PhrasingFamily? =
        rule.purpose.firstNotNullOfOrNull { familyIndex[it to rule.family] }

    /** The stage a rule points at, or null. */
    fun stageFor(rule: ClarityRule): EscalationStage? {
        val stage = rule.stage ?: return null
        return familyFor(rule)?.stage(stage)
    }

    /** Every rule for [purpose], already in ranking order. */
    fun rulesFor(purpose: Purpose): List<ClarityRule> = rulesByPurpose[purpose].orEmpty()

    /** Every family for [purpose]. */
    fun familiesFor(purpose: Purpose): List<PhrasingFamily> = families.filter { it.purpose == purpose }

    /** Every rule belonging to [family] at [purpose]. */
    fun rulesOf(purpose: Purpose, family: FamilyKey): List<ClarityRule> =
        rulesFor(purpose).filter { it.family == family }

    /** Every authored line in the catalog, leads and extensions together, families only. */
    val allVariants: List<Variant>
        get() = families.flatMap { family ->
            family.stages.flatMap { it.variants + it.extensions }
        }

    companion object {

        /**
         * Builds the catalog from the three corpus files' text.
         *
         * The rules are the same regardless of the corpus, so a caller that wants to test
         * a corpus edit against the real rules passes edited text and gets a catalog the
         * integrity checks can be run over before anything ships.
         */
        fun build(pulseText: String, reportText: String, momentumText: String): ClarityCatalog {
            val parsed = listOf(
                CorpusParser.parse(CorpusVolume.PULSE, pulseText),
                CorpusParser.parse(CorpusVolume.REPORT, reportText),
                CorpusParser.parse(CorpusVolume.MOMENTUM, momentumText),
            )
            return ClarityCatalog(
                families = parsed.flatMap { it.families },
                rules = PulseRules.ALL + ReportRules.ALL + MomentumRules.ALL,
                auxiliary = parsed.fold(emptyMap()) { accumulated, volume -> accumulated + volume.auxiliary },
                prefixes = parsed.fold(emptyMap()) { accumulated, volume -> accumulated + volume.prefixes },
                skipped = parsed.flatMap { it.skipped },
            )
        }
    }
}
