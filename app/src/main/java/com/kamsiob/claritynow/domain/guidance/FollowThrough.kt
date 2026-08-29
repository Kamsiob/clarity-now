package com.kamsiob.claritynow.domain.guidance

import com.kamsiob.claritynow.domain.engine.FamilyKey

/**
 * The whole of the follow through. CLARITY_LOGIC_ENGINE.md 10.6.
 *
 * > Layer 6 does not inject sentences. It sets a **priority boost** on the observation
 * > family whose `resolutionFactRef` matches the accepted plan, raising it in the ranking.
 * > If that family does not qualify, nothing appears. **The user can never be told about a
 * > plan they did not keep, because the mechanism has no way to say it.**
 *
 * This file is that paragraph. It is deliberately the smallest thing in the package, and
 * it is a separate file rather than four lines inside the composer for two reasons.
 *
 * ## It is built to be removed
 *
 * `MASTER_BUILD_PROMPT.md` 19 registers a formal reservation: the follow through is the
 * last thing built and **the first thing removed if it reads as supervision when tested**,
 * and it says removed rather than tuned. A thing that has to be removable cleanly should
 * be one file and one parameter, and it is: deleting this file and the `boosted` argument
 * on `Selector.select` removes the mechanism entirely, leaves every plan still offerable
 * and every observation still selectable, and changes no sentence in the corpus.
 *
 * ## It cannot say anything, and that is checked rather than intended
 *
 * Everything here is a key. [boosted] answers a set of `(family, subject)` pairs, which
 * is what escalation and cooldown are already keyed by. There is no signature in this file
 * that a sentence could travel through, and `GuidanceNonComplianceTest` asserts that by
 * reflection rather than by reading.
 *
 * The amount a boosted pair is worth is `Selector.FOLLOW_THROUGH_BOOST` and deliberately
 * not a constant here, so that deleting this file leaves layer 3 compiling. That file
 * carries the reasoning for the direction of the dependency.
 *
 * ## Why the boost is on priority and never on specificity
 *
 * Section 5 step 6 ranks by specificity descending, then priority descending, then key.
 * **Specificity is `criteria.size` and is the whole mechanism behind the illusion**: a
 * rule requiring four conditions describes a narrower situation than one requiring two,
 * and that is what makes the engine seem to notice things. Priority is a tie break and
 * section 4 says so in as many words.
 *
 * Boosting priority therefore does exactly what 10.6 asks and nothing more. Among
 * observations that describe situations of equal narrowness, the one a person said yes to
 * goes first. It **cannot** promote a family over a more specific observation, and it
 * **cannot** make a family qualify: qualification happened in step 1, before any ranking,
 * against criteria this file cannot see. "That family still has to qualify on its own
 * merits" is not a rule anybody has to enforce here, because there is no reachable value
 * from this file that step 1 reads.
 *
 * A boost on specificity would break both of those at once, which is why the number below
 * is small and the term it is added to is the one section 4 calls a tie break only.
 */
object FollowThrough {

    /**
     * The `(family, subject)` pairs an accepted plan is still pointing at.
     *
     * A set of keys. The caller hands it to `Selector.select` and the only thing that
     * happens to it there is a comparison inside a comparator.
     */
    fun boosted(plans: PlanHistory, weekStartKey: String): Set<Pair<FamilyKey, String?>> =
        plans.boosted(weekStartKey)
}
