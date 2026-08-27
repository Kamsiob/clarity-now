package com.kamsiob.claritynow.domain.engine.catalog

import com.kamsiob.claritynow.domain.engine.FactSet
import com.kamsiob.claritynow.domain.engine.FamilyKey

/**
 * Layer 2. CLARITY_LOGIC_ENGINE.md 4.
 *
 * **Rules contain no strings.** Not one. That is what lets the catalog reach several
 * hundred entries without the code growing, and what makes a rule testable without
 * reading a single line of copy. A rule says when something is worth saying; the corpus
 * says what the sentence is; the two meet at realization and nowhere else.
 *
 * The one number nobody authors is [specificity]. It is `criteria.size`, computed here,
 * and it is the whole mechanism behind the illusion the engine creates: a rule that
 * required four things to be true at once describes a narrower and therefore more
 * surprising situation than one that required two, so it wins the ranking. Authoring it
 * by hand would let a rule claim to be interesting, and every rule would.
 *
 * The corollary is the rule nobody enforces by review: **a rule must never be padded
 * with a trivially true criterion.** Section 14 asserts no criterion passes on more than
 * 90 percent of a large simulated fact corpus, which is where padding shows up.
 */
data class ClarityRule(
    val key: RuleKey,
    val purpose: Set<Purpose>,
    val family: FamilyKey,
    val subject: SubjectSelector,
    val criteria: List<Criterion>,
    val priority: Int,
    val horizonDays: Int,
    val unflattering: Boolean,
    val requiresCallback: CallbackRequirement? = null,
    /**
     * The escalation stage this rule points at, or null for a family with no ladder.
     *
     * A compound stage header such as `Stage 3, ninety five percent and above, or four
     * or more consecutive days` becomes **two rules carrying the same [stage]**, never
     * one rule holding a disjunctive range. CLARITY_LOGIC_ENGINE.md 7.3 is explicit
     * about this, and the reason is that a range cannot express a disjunction over two
     * different facts without lying about one of them.
     */
    val stage: Int? = null,
) {
    init {
        require(key.isNotBlank()) { "a rule with no key cannot be ranked, sorted or reported" }
        require(purpose.isNotEmpty()) { "rule $key belongs to no purpose, so nothing would ever select it" }
        require(criteria.isNotEmpty()) {
            "rule $key has no criteria, so its specificity would be 0 and it would qualify on " +
                "every fact set that reached it"
        }
        require(horizonDays > 0) { "rule $key declares a horizon of $horizonDays days" }
        require(stage == null || stage >= 1) { "rule $key points at stage $stage" }
    }

    /**
     * `criteria.size`. **Never authored.** CLARITY_LOGIC_ENGINE.md 4 and 5.
     *
     * There is deliberately no constructor parameter to override this. A rule that wants
     * to be more specific has to actually require more, which is the honest version of
     * the same wish.
     */
    val specificity: Int get() = criteria.size

    /** Ranking order, CLARITY_LOGIC_ENGINE.md 5 step 6: specificity, then priority, then key. */
    companion object {
        /**
         * Specificity descending, then priority descending, then [key] ascending.
         *
         * The final key sort rarely changes anything and must be present anyway. Without
         * it two rules of equal specificity and priority are ordered by whatever order
         * the catalog happened to be built in, and two devices that built it from
         * different code paths would disagree in a way no test would catch until a user
         * noticed the phone and the desktop saying different things about the same week.
         */
        val RANKING: Comparator<ClarityRule> =
            compareByDescending<ClarityRule> { it.specificity }
                .thenByDescending { it.priority }
                .thenBy { it.key }
    }
}

/**
 * One condition a rule requires. CLARITY_LOGIC_ENGINE.md 4.
 *
 * [describe] is plain English and exists for the simulator, which prints the criteria
 * that fired beside every sentence so an author reading a year of output can see why the
 * engine spoke. It is never shown to a user and never assembled into one.
 *
 * [id] is stable and is what the discrimination test in section 14 reports against, so
 * renaming one loses the history of how often it passed.
 */
data class Criterion(
    val id: String,
    val describe: String,
    val test: (FactSet, Subject?) -> Boolean,
) {
    init {
        require(id.isNotBlank()) { "a criterion with no id cannot be reported on" }
        require(describe.isNotBlank()) { "criterion $id has no description, so the simulator would print a blank reason" }
    }
}

/**
 * A rule that quotes something the user said. CLARITY_LOGIC_ENGINE.md 4.
 *
 * Resolved against `PulseFacts.recentAnswers` **before** the rule can qualify, with the
 * resolved answer attached so the realizer quotes the exact stored label rather than a
 * label reworded in a later app version.
 *
 * **A rule with an unresolvable callback does not fire, and never degrades into a
 * version without the callback**, because the sentence was authored around the quote.
 * Dropping the quote and keeping the rest produces a sentence that reads as though the
 * app remembered something and then forgot the half that mattered.
 */
data class CallbackRequirement(
    val family: FamilyKey,
    val withinDays: Int,
    val responseKey: String?,
    val subjectMustMatch: Boolean,
) {
    init {
        require(withinDays > 0) { "a callback window of $withinDays days reaches nothing" }
    }
}

/**
 * The three subject selectors the corpus families need, CLARITY_LOGIC_ENGINE.md 4 and 6.1.
 *
 * [AREA] never yields an archived or tombstoned area, and it does not have to check:
 * `FactSet.areas` excludes them by construction, per section 3.1. Prohibition 3 is
 * enforced by the shape of the data rather than by a test everyone remembers to run.
 */
object Subjects {

    /** Exactly one null subject, so a family with no subject falls out of the same loop. */
    val NONE: SubjectSelector = SubjectSelector { listOf(null) }

    /** Every area with facts in this window. Archived and tombstoned areas are already absent. */
    val AREA: SubjectSelector = SubjectSelector { facts ->
        facts.areas.keys.sorted().map { Subject(it, SubjectKind.AREA) }
    }

    /** The active item of every area, which is the only item any Pulse family speaks about. */
    val ACTIVE_ITEM: SubjectSelector = SubjectSelector { facts ->
        facts.areas.values
            .mapNotNull { it.activeItemId }
            .sorted()
            .map { Subject(it, SubjectKind.ITEM) }
    }
}
