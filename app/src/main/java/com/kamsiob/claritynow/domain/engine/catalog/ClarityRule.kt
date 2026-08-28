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
    /**
     * True when the thing this rule is about **is** an area's silence.
     *
     * Validator check 1 forbids naming an area with no events in the window being
     * described, because such a sentence is a claim about a week the area had nothing to
     * do with. Three rules exist to say exactly that the area had nothing to do with the
     * week, and against the check as it was first written every one of their candidates
     * was vetoed: 107 vetoes across a simulated year, all of them check 1, a silence with
     * no reason anybody reading the output could find.
     *
     * **The check was right and the writing was wrong.** Check 1 exists to prevent a
     * phantom area, meaning an area that never had activity being named as though it had.
     * A family whose subject is the absence has to name an area with no events. So the
     * check is narrowed rather than widened: a candidate from a rule carrying this flag
     * may name an area with no events in the window, and only then, and only when that
     * area has a real history behind the silence. `AbsenceSubject` in `domain.engine.validate`
     * holds the three conditions and both check 1 implementations ask it.
     *
     * **Phantom protection survives untouched.** A new empty area still cannot be named,
     * by any rule, flagged or not, because the flag buys a silence about a real history
     * and nothing else.
     *
     * Only `neglectedArea`, `areaGoneQuiet` and `areaRevival` carry it. A rule that wants
     * it because its candidates are being vetoed is a rule with the wrong criteria.
     */
    val absenceSubject: Boolean = false,
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
 * The keys of every rule carrying [ClarityRule.absenceSubject], for the one reader that
 * holds a rule key and no catalog.
 *
 * Layer 5 is handed a `Candidate`, which records the key of the rule that produced it and
 * not the rule itself, and `ClarityValidator` is constructed with a zone and nothing else
 * at five call sites. Threading a catalog through all of them to read one boolean would
 * make the validator's constructor a place where somebody could hand it a catalog whose
 * flags disagreed with the one the engine selected from, which is a worse failure than the
 * one it would solve.
 *
 * The rules are static. `ClarityCatalog.build` assembles exactly these three lists whatever
 * corpus text it is given, so this set and the catalog's rules cannot be different rules.
 *
 * A key that is not in the catalog answers false, which is the safe direction: an unknown
 * rule gets check 1 as it was written, and a hand built test candidate carrying an invented
 * key is not quietly granted the exception.
 */
internal object AbsenceSubjectRules {

    val KEYS: Set<RuleKey> =
        (PulseRules.ALL + ReportRules.ALL + MomentumRules.ALL)
            .filter { it.absenceSubject }
            .map { it.key }
            .toSet()

    operator fun contains(ruleKey: RuleKey): Boolean = ruleKey in KEYS
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
