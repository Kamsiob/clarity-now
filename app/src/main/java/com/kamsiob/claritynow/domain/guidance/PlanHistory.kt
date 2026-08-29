package com.kamsiob.claritynow.domain.guidance

import com.kamsiob.claritynow.domain.engine.FactDates
import com.kamsiob.claritynow.domain.engine.FamilyKey
import com.kamsiob.claritynow.domain.query.TrailQueries

/**
 * The plans a person has actually taken up, rebuilt from the log on every invocation.
 * CLARITY_LOGIC_ENGINE.md 10.3 and 10.6.
 *
 * **Derived entirely from PLAN_OFFERED and PLAN_ACCEPTED. Never from DataStore.** The
 * argument is `FiringHistory`'s and it is the same one: a device that has just merged a
 * log has to reach the same decision as the device that produced it, and DataStore does
 * not merge. The log merges, so a history derived from it merges too.
 *
 * ## The shape is the safeguard, and it is the whole of this file
 *
 * Section 19 of `MASTER_BUILD_PROMPT.md` registers a formal reservation against layer 6
 * and names the failure it fears: a mechanism that ends up telling a person they did not
 * do a thing they said they would. 10.6 answers it by making the follow through a
 * priority boost rather than a sentence, and adds the sentence that this file is built
 * around: **the user can never be told about a plan they did not keep, because the
 * mechanism has no way to say it.**
 *
 * So [Accepted] carries **three fields, and every one of them is a key**. A week, a
 * family and a subject. No plan id, no offered line, no committed line, no timestamp, no
 * count. Those five things exist on `PlanOffered` and on `PlanState`, they are read by
 * this file's [from], and **not one of them is copied out of it**.
 *
 * That is not tidiness. A family key and a week key are exactly what every ordinary
 * observation already carries, so nothing downstream of this type can compose a sentence
 * about a plan that it could not already have composed about the week. There is no
 * string here to interpolate and no boolean here that means kept or broken. **If a later
 * change makes it possible to write a line about a plan through this path, that change
 * will have had to add a field to this class**, which is a visible act rather than a
 * quiet one.
 *
 * ## Why an unaccepted plan is not here at all
 *
 * 10.5 and 10.6 both say it and they say it in different words. Declining writes
 * nothing, costs nothing, is never counted, never referenced. Ignoring both options is
 * identical to declining. A plan is only ever followed up if it was accepted, and
 * unaccepted plans vanish without trace.
 *
 * [from] therefore filters offers down to the ones with an acceptance beside them
 * **before** it builds anything, so an offer nobody took up leaves no residue in this
 * object for any later code to find. Not a flag, not an empty entry, not a count of how
 * many times somebody said no.
 */
data class PlanHistory(val accepted: List<Accepted>) {

    /**
     * One plan a person took up, reduced to the three keys the two rules need.
     *
     * Read the class documentation for why there are three fields rather than ten.
     */
    data class Accepted(
        /** The Sunday of the week the plan was offered in. */
        val weekStartKey: String,
        /** The observation family the plan was aimed at. */
        val familyKey: FamilyKey,
        /** The area or item that family named, or null. */
        val subjectId: String?,
    )

    /**
     * The `(family, subject)` pairs accepted inside [WEEKS_CONSIDERED] weeks of
     * [weekStartKey]. CLARITY_LOGIC_ENGINE.md 10.6.
     *
     * A set of keys, and the only thing anything does with it is rank. See
     * [FollowThrough].
     */
    fun boosted(weekStartKey: String): Set<Pair<FamilyKey, String?>> =
        within(weekStartKey).map { it.familyKey to it.subjectId }.toSet()

    /**
     * CLARITY_LOGIC_ENGINE.md 10.4 rule 4. True when a plan from either of the previous
     * two weeks is still unresolved, and therefore no new plan may be offered.
     *
     * ## What resolved means, given that nothing records it
     *
     * There is no resolution event and there must not be one, so this is derived rather
     * than stored. **A plan is unresolved when the situation that motivated it is still
     * on this week's page**, meaning the same `(family, subject)` is among the
     * observations that actually appeared. If that family no longer qualifies, the thing
     * the plan was about is no longer worth saying, and the plan is done with.
     *
     * The two rules then fit together the way 10.4's own note says they should. The
     * follow through boost raises the motivating family, so a situation that persists is
     * more likely to appear; and a family that appears is exactly what stops another plan
     * being stacked on top of the first. **Stacking unfinished plans is how this becomes
     * a nag**, and the mechanism that would cause the stacking is the mechanism that
     * prevents it.
     *
     * ## Why only accepted plans are considered
     *
     * 10.4 rule 4 says "no plan offered in the previous two weeks", and taken on its own
     * that would include one the person declined. It cannot, because 10.5 says declining
     * costs nothing and is never referenced, and holding back next week's plan because
     * somebody said no last week is a cost and a reference at once. The two are
     * reconciled by reading a decline as a resolution: the person answered, the plan is
     * finished, and there is nothing left open. Ignoring both options is identical to
     * declining, so it resolves the same way. [accepted] holds only accepted plans for
     * exactly this reason, so the reconciliation is a property of the data rather than a
     * filter somebody has to remember to write.
     */
    fun stillUnresolved(weekStartKey: String, appearedSubjects: Set<Pair<FamilyKey, String?>>): Boolean =
        within(weekStartKey).any { (it.familyKey to it.subjectId) in appearedSubjects }

    /** Accepted plans from the [WEEKS_CONSIDERED] weeks before [weekStartKey], inclusive of neither end. */
    private fun within(weekStartKey: String): List<Accepted> = accepted.filter {
        val age = FactDates.daysBetweenKeys(it.weekStartKey, weekStartKey) ?: return@filter false
        age in 1..(WEEKS_CONSIDERED * DAYS_PER_WEEK)
    }

    companion object {

        /** 10.4 rule 4 and 10.6. The previous two weeks. */
        const val WEEKS_CONSIDERED = 2

        private const val DAYS_PER_WEEK = 7

        /** Nobody has ever accepted a plan. What a fresh install rebuilds to. */
        val EMPTY = PlanHistory(accepted = emptyList())

        /**
         * Rebuilds the history from every plan event before [asOfMillis].
         *
         * Lifetime rather than windowed, for `FiringHistory`'s reason: bounding the read
         * would make the bound a fourth number nobody could see. The list stays small
         * because at most one plan is offered per week and only the accepted ones are
         * kept.
         *
         * **The offer is read for its keys and discarded.** Its two rendered lines and
         * its id reach a local variable and no further. See the class note.
         */
        fun from(queries: TrailQueries, asOfMillis: Long): PlanHistory {
            val acceptedIds = queries
                .plansAcceptedBetween(Long.MIN_VALUE, asOfMillis)
                .map { it.planId }
                .toSet()
            if (acceptedIds.isEmpty()) return EMPTY
            val kept = queries
                .plansOfferedBetween(Long.MIN_VALUE, asOfMillis)
                .filter { it.planId in acceptedIds }
                .map { Accepted(it.weekStartKey, it.familyKey, it.subjectId) }
            return PlanHistory(accepted = kept)
        }
    }
}
