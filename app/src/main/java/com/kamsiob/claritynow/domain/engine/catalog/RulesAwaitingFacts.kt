package com.kamsiob.claritynow.domain.engine.catalog

import com.kamsiob.claritynow.domain.engine.FamilyKey

/**
 * Families and stages that have authored language and no rule, because the fact their
 * trigger names is not declared in CLARITY_LOGIC_ENGINE.md 3.1.
 *
 * **The register is empty, and that is the result of the facts phase rather than the
 * absence of a problem.** It held twelve entries when phase 5 wrote it: `switching`
 * wanting an area's own swap count, `rebalance` wanting the dormancy an area returned
 * from, `quietDay` stages 2 and 3 and `concentration` stage 3's second branch wanting a
 * run of consecutive days, and seven Report patterns wanting weekly series that
 * `HistoryFacts` did not carry. Every one of those facts was declared, every one of those
 * rules is now written, and the entries were deleted as they were satisfied. The empty
 * list is the proof, because [CatalogIntegrity.everyFamilyHasARule] reads it.
 *
 * **This exists instead of an approximation.** Every entry it held could have been given a
 * criterion that was nearly the right shape: `window.activeDays` in place of a run of
 * consecutive days, `window.swaps` in place of a per area swap count,
 * `AreaFacts.daysSinceLastEvent` in place of the dormancy an area returned from. Each of
 * those would fire the family on a shape it does not describe, and the sentence that came
 * out would be arithmetic nobody could fault and a claim about a person's week that was
 * not true. Prime directive: every claim must be true, and provably so from a count query.
 *
 * It is a data structure rather than a comment because a test reads it. [CatalogIntegrity]
 * asserts that every family in the corpus either has a rule or is listed here, so a family
 * cannot go quiet without someone deciding it should. **An entry appearing here is a rule
 * being lost, now that the list has been emptied once.** Add one only for a family whose
 * trigger names a fact 3.1 genuinely does not declare, and never to park a rule that is
 * inconvenient to write.
 */
internal object RulesAwaitingFacts {

    /** One family or stage, the fact it needs, and where the trigger is written down. */
    data class Gap(
        val family: FamilyKey,
        val purpose: Purpose,
        val stage: Int?,
        val missingFact: String,
        val citation: String,
    )

    val GAPS: List<Gap> = emptyList()

    /** Families with no rule at all, as opposed to a single stage that has none. */
    val FAMILIES_WITHOUT_RULES: Set<FamilyKey> = GAPS.filter { it.stage == null }.map { it.family }.toSet()
}
