package com.kamsiob.claritynow.domain.engine.select

import com.kamsiob.claritynow.domain.engine.AnsweredPulse
import com.kamsiob.claritynow.domain.engine.catalog.ClarityRule
import com.kamsiob.claritynow.domain.engine.catalog.Purpose
import com.kamsiob.claritynow.domain.engine.catalog.Subject

/**
 * A rule, and the subject it qualified for. CLARITY_LOGIC_ENGINE.md 5.
 *
 * **A rule alone is not a selection.** Section 4 evaluates a rule once per candidate
 * subject, so the same rule can qualify for Work and not for Health in one window, and
 * escalation is tracked per `(family, subjectId)` so nine days on one item and three on
 * another are independent ladders. The pair is the unit everything after step 1 filters,
 * ranks and realizes.
 *
 * [callback] is attached here rather than resolved again later, because 4 requires the
 * resolved answer to travel with the rule so the realizer can quote the exact stored label
 * rather than a label reworded in a later app version. A rule with an unresolvable callback
 * never becomes a selection at all.
 */
data class Selection(
    val rule: ClarityRule,
    val purpose: Purpose,
    val subject: Subject?,
    val callback: ResolvedCallback?,
    /** The window's own length in days, which is the youngest a referenced fact can be. */
    val windowDays: Int,
) {
    /** What escalation and cooldown are keyed by, with null for a family with no subject. */
    val subjectId: String? get() = subject?.id

    /** The stored label a callback line quotes, or null. */
    val callbackLabel: String? get() = callback?.answer?.responseLabel

    /** `criteria.size`, never authored. Section 4. */
    val specificity: Int get() = rule.specificity
}

/**
 * The answer a callback rule found, with how old it is.
 *
 * [ageDays] is what the horizon filter reads. A callback to last week is attentive; one to
 * fourteen months ago in the wrong context is uncanny, which is the whole reason section 4
 * gives a rule a horizon at all.
 */
data class ResolvedCallback(val answer: AnsweredPulse, val ageDays: Int)
