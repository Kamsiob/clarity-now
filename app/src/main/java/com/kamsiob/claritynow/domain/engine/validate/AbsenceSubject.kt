package com.kamsiob.claritynow.domain.engine.validate

import com.kamsiob.claritynow.domain.engine.AreaFacts
import com.kamsiob.claritynow.domain.engine.FactExtractor
import com.kamsiob.claritynow.domain.engine.catalog.AbsenceSubjectRules
import com.kamsiob.claritynow.domain.engine.catalog.RuleKey

/**
 * The one narrowing in check 1, held in one place because check 1 is implemented twice.
 *
 * ## What was wrong
 *
 * Check 1 forbids naming an area with no events in the window being described.
 * `neglectedArea`, `areaGoneQuiet` and `areaRevival` exist to speak about an area that has
 * been still for seven, fourteen or twenty one days, so every candidate they ever produced
 * named an area with no events and every one of them was vetoed: 107 vetoes across a
 * simulated year, all of them check 1, and every one of them a silence with no reason a
 * reader of the output could find. The rule and the check were exact opposites.
 *
 * The check was right. It exists to stop a **phantom** area, meaning an area that never had
 * activity being named as though it had, and that is the failure a person cannot recover
 * from: they read a sentence about an area they know did nothing, and everything else the
 * app has ever told them becomes a guess. What was wrong was the writing. A family whose
 * subject **is** the absence has to name an area with no events, and check 1 had no way to
 * say so.
 *
 * ## What it is now
 *
 * Narrowed, deliberately, rather than widened. An area with no events in the window may be
 * named only when all four of these hold, and the fourth is what keeps the phantom out:
 *
 * 1. the rule carries `ClarityRule.absenceSubject`
 * 2. the area has at least [LIFETIME_EVENTS] events in its whole life
 * 3. the area is not new
 * 4. [AreaFacts.daysSinceLastEvent] is a real computed value rather than `Int.MAX_VALUE`
 *
 * A brand new empty area fails 2, 3 and 4 at once and cannot be named by anything. An area
 * somebody made a year ago, used properly, and has not touched for three weeks passes all
 * four, and that is the only shape the three families were ever about.
 *
 * ## Why the floor is the extractor's constant
 *
 * [LIFETIME_EVENTS] is `FactExtractor.NEGLECT_LIFETIME_EVENTS`, which is the same five
 * `RollupFacts.neglectedAreaIds` applies when it decides an area counts as neglected at
 * all. Restating the number here would let the fact and the check that guards the sentence
 * about the fact drift apart, and the symptom of that drift is silence, which is the
 * failure this whole file exists to have found once.
 */
internal object AbsenceSubject {

    /** The lifetime a silence has to be a silence from. One number, `RollupFacts`'s own. */
    const val LIFETIME_EVENTS: Int = FactExtractor.NEGLECT_LIFETIME_EVENTS

    /**
     * Why [area] may not be named by [ruleKey], or null when it may.
     *
     * Called only for an area with no events in the window; an area with events was never
     * check 1's business. The detail is written to be read months later beside a rule key,
     * so it names the fact and the number rather than saying that something was wrong.
     */
    fun refusalFor(ruleKey: RuleKey, area: AreaFacts): String? = when {
        ruleKey !in AbsenceSubjectRules ->
            "Check 1 requires real events, not merely an area that exists"

        area.lifetimeEvents < LIFETIME_EVENTS ->
            "$ruleKey speaks about an absence, and an absence needs a history to be an absence " +
                "from. This area has ${area.lifetimeEvents} events in its whole life, against " +
                "the $LIFETIME_EVENTS RollupFacts.neglectedAreaIds requires"

        area.isNew ->
            "$ruleKey speaks about an absence, and this area is ${area.ageDays} days old. " +
                "An area nobody has had time to use is not an area anybody left alone"

        area.daysSinceLastEvent == Int.MAX_VALUE ->
            "$ruleKey speaks about an absence, and this area has never had an event, so " +
                "daysSinceLastEvent is the never sentinel rather than a measured silence"

        else -> null
    }
}
