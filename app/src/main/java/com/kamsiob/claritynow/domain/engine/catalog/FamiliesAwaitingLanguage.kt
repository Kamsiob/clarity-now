package com.kamsiob.claritynow.domain.engine.catalog

import com.kamsiob.claritynow.domain.engine.FamilyKey

/**
 * The mirror of [RulesAwaitingFacts]: a family whose rules are written and whose language
 * is not, held out of the catalog until the lines exist.
 *
 * **The register is empty, and it was emptied rather than abandoned.** It held one entry
 * when the facts phase wrote it, `familiarDip`, the second branch of the capacity gate in
 * `MASTER_BUILD_PROMPT.md` 14b.9. Phase 9 authored its bench as `CORPUS_2_REPORT.md` 2.22
 * and took the five steps the entry named: the family is declared in
 * [EngineFamilies.REPORT_OBSERVATION], its three rules moved into [ReportRules] unchanged,
 * its area rule joined the two silence families `RuleCatalogTest` allows to name an area
 * with no events, and 7.4's widened enumeration left it `false` for the reason the entry
 * gave, which is that a family existing to say a shape is a rhythm rather than a decline is
 * the opposite of unflattering.
 *
 * **The constraints the bench is written under moved with it, into the corpus.** They were
 * held here because the rule and the constraint were decided together, and once the bench
 * exists the corpus is the only place a reader of the sentences will look. Two copies of a
 * writing rule is one copy too many, and the one that would go stale is this one.
 *
 * ## Why this register exists at all
 *
 * The catalog cannot hold a half built family. `ReportWalker.finish` throws when the
 * families declared in [EngineFamilies] and the families found in the corpus file differ in
 * either direction, and [CatalogIntegrity.rulesPointAtExistingFamilies] fails a rule naming
 * a family the corpus does not carry. Both are right and neither should be relaxed: a
 * family with a rule and no bench would qualify, produce no sentence, and look exactly like
 * a family that never happened to fire, which is the failure [RulesAwaitingFacts] was
 * written to make visible from the other side.
 *
 * So the rules are written here, in full, against the real facts, and a test runs them.
 * **This is a declaration and not a parking space.** [RULES] never reaches
 * `ClarityCatalog.build`, so nothing here can speak; what it can do is be wrong in a way a
 * test catches now rather than after the language lands.
 *
 * ## What a new entry owes
 *
 * The key, the purpose, the cooldown, the corpus key prefix its lines will carry, the
 * citation, and the reason the family exists at all. An entry that names a family and
 * nothing else is a comment in the shape of a data structure. **An entry appearing here is
 * language being owed**, now that the register has been emptied once, so add one only for a
 * family whose rules genuinely cannot wait for its sentences.
 */
internal object FamiliesAwaitingLanguage {

    /** One family with rules and no language, and what it is waiting for. */
    data class Reservation(
        val key: FamilyKey,
        val purpose: Purpose,
        val cooldownDays: Int,
        /** The corpus key prefix its lines carry, so the authoring phase does not choose one. */
        val keyPrefix: String,
        val citation: String,
        val why: String,
    )

    val FAMILIES: List<Reservation> = emptyList()

    /** Every reserved family key, for the integrity check that they are not also declared. */
    val KEYS: Set<FamilyKey> = FAMILIES.map { it.key }.toSet()

    /** The rules of every reserved family, held out of `ClarityCatalog.build`. */
    val RULES: List<ClarityRule> = emptyList()
}
