package com.kamsiob.claritynow.domain.engine.catalog

import com.kamsiob.claritynow.domain.engine.FamilyKey

/**
 * The catalog integrity checks. CLARITY_LOGIC_ENGINE.md 14, and the catalog integrity
 * list in `MASTER_BUILD_PROMPT.md` 17.
 *
 * **These are functions that return findings, not assertions.** The tests assert; this
 * object only looks. Two reasons, and the second is the one that matters.
 *
 * The first is that a failure has to name the file and the line an author edits, and an
 * assertion buried in a parser produces a stack trace instead.
 *
 * The second is that some of these currently find things. The corpus was authored before
 * the checks existed, and two of the checks below have real violations in it today, listed
 * with their exact contents in [KnownCorpusViolations]. Encoding them rather than
 * disabling the check means a **new** violation still fails the build, and phase 9 gets an
 * exact list of what to fix rather than a check nobody can turn on.
 */
internal object CatalogIntegrity {

    /** One finding, with enough in it to go and look. */
    data class Finding(val check: String, val detail: String)

    /** Every check, in one pass. */
    fun checkAll(catalog: ClarityCatalog): List<Finding> =
        rulesPointAtExistingFamilies(catalog) +
            everyFamilyHasARule(catalog) +
            duplicateKeys(catalog) +
            specificityIsCriteriaSize(catalog) +
            shareRulesCarryAFloor(catalog) +
            undeclaredSlots(catalog) +
            qualitativeStagesCarryTheirOwnCriteria(catalog) +
            stageRangesAreContiguous(catalog) +
            unflatteringMatchesTheEnumeration(catalog) +
            fragmentsInTwoFamilies(catalog) +
            constructionsInMoreThanTwoFamilies(catalog)

    /** Every rule points at a family that exists, at the purpose the rule declares. */
    fun rulesPointAtExistingFamilies(catalog: ClarityCatalog): List<Finding> =
        catalog.rules.mapNotNull { rule ->
            val family = catalog.familyFor(rule)
            when {
                family == null -> Finding("rule points at a missing family", "${rule.key} names ${rule.family}")
                rule.stage != null && family.stage(rule.stage) == null ->
                    Finding("rule points at a missing stage", "${rule.key} names ${rule.family} stage ${rule.stage}")
                else -> null
            }
        }

    /**
     * Every family has at least one rule, or is listed in [RulesAwaitingFacts] with the
     * fact it needs.
     *
     * A family with authored language and no rule is silent, and a silent family looks
     * exactly like a family that never happened to qualify. This is the check that keeps
     * the difference visible.
     */
    fun everyFamilyHasARule(catalog: ClarityCatalog): List<Finding> =
        catalog.families.mapNotNull { family ->
            if (catalog.rulesOf(family.purpose, family.key).isNotEmpty()) return@mapNotNull null
            if (family.key in RulesAwaitingFacts.FAMILIES_WITHOUT_RULES) return@mapNotNull null
            Finding(
                "family with no rule",
                "${family.purpose} ${family.key} has ${family.allVariants.size} authored lines and no " +
                    "rule, and is not listed in RulesAwaitingFacts",
            )
        }

    /** No duplicate rule keys, and no duplicate variant keys anywhere in the corpus. */
    fun duplicateKeys(catalog: ClarityCatalog): List<Finding> {
        val ruleDuplicates = catalog.rules.groupingBy { it.key }.eachCount().filterValues { it > 1 }
        val variantDuplicates = catalog.allVariants.groupingBy { it.key }.eachCount().filterValues { it > 1 }
        return ruleDuplicates.map { Finding("duplicate rule key", "${it.key} appears ${it.value} times") } +
            variantDuplicates.map { Finding("duplicate variant key", "${it.key} appears ${it.value} times") }
    }

    /**
     * Specificity is `criteria.size`. Never authored.
     *
     * There is no constructor parameter to get wrong, so this check cannot fail as the
     * code stands. It is here anyway, because the day someone adds one to make a rule win
     * a ranking is the day this stops being obvious, and a check that has always passed is
     * cheaper than the argument.
     */
    fun specificityIsCriteriaSize(catalog: ClarityCatalog): List<Finding> =
        catalog.rules.mapNotNull { rule ->
            if (rule.specificity == rule.criteria.size) null
            else Finding("specificity was authored", "${rule.key} reports ${rule.specificity} for ${rule.criteria.size} criteria")
        }

    /**
     * Every rule that reads a share carries a minimum event floor.
     *
     * `shareOfEvents` is the most misused fact in the system. One event in a one event
     * window is a hundred percent concentration, and the sentence that comes out of it is
     * correct arithmetic and a false claim about somebody's week. 3.1 requires the floor
     * and this is the test that enforces it.
     */
    fun shareRulesCarryAFloor(catalog: ClarityCatalog): List<Finding> =
        catalog.rules.mapNotNull { rule ->
            val readsShare = rule.criteria.any { it.id.startsWith(SHARE_READING_PREFIX) }
            val hasFloor = rule.criteria.any { it.id.startsWith(SHARE_FLOOR_PREFIX) }
            if (!readsShare || hasFloor) null
            else Finding("share based rule with no event floor", rule.key)
        }

    /** Every slot marker the corpus uses has a declared production source. */
    fun undeclaredSlots(catalog: ClarityCatalog): List<Finding> {
        val used = catalog.allVariants.flatMap { it.requiredSlots }.toSet() +
            catalog.families.flatMap { family -> family.stages.flatMap { it.questions } }
                .flatMap { it.text.slots }
        return (used - SlotProduction.DECLARED).map {
            Finding("slot with no production source", "{$it} is used in the corpus and SlotProduction has no entry")
        }
    }

    /**
     * A rule pointing at a stage whose header states its condition qualitatively has to
     * carry a criterion of its own for that condition.
     *
     * `Stage 2, clear imbalance toward intake` gives the stage no range, so the range on
     * the stage is the widest one and decides nothing. Without a criterion in the rule,
     * the stage would qualify on anything that reached it.
     */
    fun qualitativeStagesCarryTheirOwnCriteria(catalog: ClarityCatalog): List<Finding> =
        catalog.rules.mapNotNull { rule ->
            val stage = catalog.stageFor(rule) ?: return@mapNotNull null
            val qualitative = stage.header.numericConditions.isEmpty()
            if (!qualitative || rule.criteria.size >= 2) null
            else Finding(
                "qualitative stage with an underspecified rule",
                "${rule.key} points at ${rule.family} stage ${stage.index}, whose header " +
                    "`${stage.header.text}` states no range, and the rule carries one criterion",
            )
        }

    /**
     * Stage ranges are contiguous and non overlapping within a family.
     *
     * Checked only over the families whose every stage header states a numeric range, and
     * the families that do not are reported separately by the test rather than silently
     * passing here. A ladder with a gap in it is a magnitude nothing describes, and a
     * ladder with an overlap is two stages qualifying at once and the ranking deciding
     * which by accident.
     */
    fun stageRangesAreContiguous(catalog: ClarityCatalog): List<Finding> =
        catalog.families.flatMap { family ->
            // The ladder is the FIRST branch of each header. A compound header's second
            // branch is over a different fact entirely, so folding it into the span would
            // make concentration stage 3 run from four to the maximum and overlap stages 1
            // and 2, which are percentages.
            val numeric = family.stages.mapNotNull { stage ->
                val first = stage.header.conditions.firstOrNull()
                if (first is StageCondition.Numeric) stage.index to first.range else null
            }
            if (numeric.size != family.stages.size || numeric.size < 2) return@flatMap emptyList()
            numeric.zipWithNext().mapNotNull { (lower, upper) ->
                val expected = lower.second.last + 1
                if (lower.second.last == Int.MAX_VALUE) {
                    Finding(
                        "an open ended stage that is not the last",
                        "${family.key} stage ${lower.first} runs to the maximum and stage ${upper.first} follows it",
                    )
                } else if (upper.second.first != expected) {
                    Finding(
                        if (upper.second.first < expected) "overlapping stages" else "a gap between stages",
                        "${family.key} stage ${lower.first} ends at ${lower.second.last} and stage " +
                            "${upper.first} begins at ${upper.second.first}",
                    )
                } else {
                    null
                }
            }
        }

    /**
     * Every rule's `unflattering` flag matches the enumeration in 7.4, and no neutral or
     * positive family carries it.
     *
     * The two families 7.4 qualifies by a stage their corpus family does not have are
     * exempted by name, because the catalog resolves them by splitting the family into a
     * low and a high rule instead. See [UnflatteringRules].
     */
    fun unflatteringMatchesTheEnumeration(catalog: ClarityCatalog): List<Finding> =
        catalog.rules.mapNotNull { rule ->
            if (rule.family in UnflatteringRules.SPLIT_AT_MAGNITUDE) return@mapNotNull null
            val expected = UnflatteringRules.isUnflattering(rule.family, rule.stage ?: 1)
            if (rule.unflattering == expected) null
            else Finding(
                "unflattering flag disagrees with 7.4",
                "${rule.key} is ${rule.unflattering} and the enumeration says $expected",
            )
        }

    /**
     * No fragment string appears in two families of the same purpose. 7.7.
     *
     * Scoped to one purpose because that is where a reader could notice. A Pulse statement
     * and a Report lead that share a clause are seen days apart on two different surfaces;
     * a headline and an observation that share one are seen in the same glance. Families
     * are compared by key, so `personalBest` as a headline and `personalBest` as an
     * observation are one family and not two, which is what they are.
     *
     * The corpus violates this today in six places, listed in [KnownCorpusViolations].
     */
    fun fragmentsInTwoFamilies(catalog: ClarityCatalog): List<Finding> {
        val owners = mutableMapOf<Pair<Purpose, String>, MutableSet<FamilyKey>>()
        for (variant in catalog.allVariants) {
            for (fragment in fragmentsOf(variant.statement.text)) {
                owners.getOrPut(variant.purpose to fragment) { mutableSetOf() }.add(variant.family)
            }
        }
        return owners
            .filterValues { it.size > 1 }
            .mapNotNull { (key, families) ->
                val (purpose, fragment) = key
                if (KnownCorpusViolations.isKnownSharedFragment(purpose, fragment, families)) null
                else Finding(
                    "a fragment in two families",
                    "$purpose: `$fragment` appears in ${families.sorted()}",
                )
            }
    }

    /**
     * No rhetorical construction appears in more than two families. 7.7 and 13.
     *
     * A construction is a shape rather than a string: `A, then B, then C` is the same
     * rhetorical move whether it counts weeks, areas or sessions, and once a reader sees
     * it four times they cannot stop seeing it. Two of the four shapes checked are over
     * the cap in the corpus today and are listed in [KnownCorpusViolations].
     */
    fun constructionsInMoreThanTwoFamilies(catalog: ClarityCatalog): List<Finding> {
        val owners = mutableMapOf<String, MutableSet<FamilyKey>>()
        for (variant in catalog.allVariants) {
            for (sentence in sentencesOf(variant.statement.text)) {
                for ((name, shape) in CONSTRUCTIONS) {
                    if (shape.containsMatchIn(sentence)) owners.getOrPut(name) { mutableSetOf() }.add(variant.family)
                }
            }
        }
        return owners.mapNotNull { (name, families) ->
            val allowance = KnownCorpusViolations.CONSTRUCTION_ALLOWANCE[name].orEmpty()
            val unexpected = families - allowance
            when {
                families.size <= CONSTRUCTION_CAP -> null
                unexpected.isEmpty() -> null
                else -> Finding(
                    "a rhetorical construction in more than two families",
                    "`$name` appears in ${families.sorted()}, and ${unexpected.sorted()} is beyond " +
                        "the recorded set",
                )
            }
        }
    }

    /** 7.7: no rhetorical construction in more than two families. */
    const val CONSTRUCTION_CAP = 2

    /**
     * The shapes checked. Named rather than derived, because a construction is a thing a
     * reader recognizes and there is no way to compute that.
     */
    val CONSTRUCTIONS: Map<String, Regex> = mapOf(
        "tripleThen" to Regex(""",\s*then\b[^.]*,\s*then\b"""),
        "xCommaNotY" to Regex("""^[^,]{2,30},\s*not\s+\w+$"""),
        "notXItIsY" to Regex("""\bis not\b[^.]*\.\s*it is\b"""),
        "andCounting" to Regex("""\band counting\b"""),
    )

    /** Sentences, normalized: slot markers collapsed, lowercased, terminal punctuation dropped. */
    fun sentencesOf(text: String): List<String> =
        SENTENCE_SPLIT.split(Template.MARKER.replace(text, "{}").lowercase())
            .map { it.trim().trimEnd('.', '?') }
            .filter { it.isNotEmpty() }

    /** A fragment is a normalized sentence of at least three words. Shorter is a phrase, not a fragment. */
    fun fragmentsOf(text: String): List<String> =
        sentencesOf(text).filter { it.split(WHITESPACE).size >= MINIMUM_FRAGMENT_WORDS }

    private const val MINIMUM_FRAGMENT_WORDS = 3
    private val SENTENCE_SPLIT = Regex("""(?<=[.?])\s+""")
    private val WHITESPACE = Regex("""\s+""")
}
