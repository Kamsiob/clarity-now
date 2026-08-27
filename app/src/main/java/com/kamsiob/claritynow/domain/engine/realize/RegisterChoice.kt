package com.kamsiob.claritynow.domain.engine.realize

import com.kamsiob.claritynow.domain.engine.PartOfDay
import com.kamsiob.claritynow.domain.engine.catalog.Purpose
import com.kamsiob.claritynow.domain.engine.catalog.Register

/**
 * Which voice a sentence is said in. CLARITY_LOGIC_ENGINE.md 7.4, and 6.4 for the one
 * family whose register is a constraint on grammar rather than a matter of taste.
 *
 * 7.4 gives four selection steps and a fallback order. This returns them as one ordered
 * preference, and the realizer takes the first register with a line available at the stage
 * it is speaking from. Expressing it as an order rather than as a decision is what makes
 * it testable: a bench with no reflective line at stage 3 must not produce silence, it must
 * produce the next voice down.
 *
 * **`NEUTRAL_AGENT` is reachable only through a rule marked `unflattering`**, and it is
 * absent from the fallback otherwise. 7.4: a family that is neutral or positive never uses
 * it, because making the fact the subject of a good week reads as withholding credit.
 */
object RegisterChoice {

    /**
     * The registers to try, best first.
     *
     * [notable] and [editorialBudgetSpent] together decide whether `EDITORIAL` is offered,
     * and both are needed. 7.4 caps a report at two editorial leads and the Report corpus
     * says the register is "reserved for leads that have earned it with a genuinely notable
     * fact", which is a condition on the lead rather than on the report. Nothing in 3.1 or
     * 4 carries a notability flag, so the realizer uses the one measure of notability the
     * engine already computes: **specificity**. A rule that required four things to be true
     * at once describes a narrower situation than one that required two, which is the whole
     * mechanism of section 5, and it is the same thing an editor means by a fact worth
     * writing up. See [NOTABLE_SPECIFICITY].
     */
    fun preference(
        purpose: Purpose,
        unflattering: Boolean,
        partOfDay: PartOfDay,
        notable: Boolean = false,
        editorialBudgetSpent: Boolean = false,
    ): List<Register> {
        val order = mutableListOf<Register>()
        // 1. The unflattering register, first, and only here.
        if (unflattering) order += Register.NEUTRAL_AGENT
        // 3. The editorial budget, Report only. A third editorial lead is re-realized in
        // the fallback, which is what dropping it from this list does.
        if (isReport(purpose) && notable && !editorialBudgetSpent) order += Register.EDITORIAL
        // 2. Time of day, Pulse only. Dawn and midday prefer the plainer voices; evening
        // prefers the reflective one. The band boundaries are PartOfDay's, which are the
        // app's own dawn, midday and evening rather than an even quarter split.
        if (purpose == Purpose.PULSE) {
            when (partOfDay) {
                PartOfDay.MORNING, PartOfDay.AFTERNOON -> {
                    order += Register.PLAIN
                    order += Register.OBSERVATIONAL
                }
                PartOfDay.EVENING, PartOfDay.NIGHT -> order += Register.REFLECTIVE
            }
        }
        // 4. The fallback order, with anything already preferred left where it is.
        for (register in FALLBACK) if (register !in order) order += register
        return order
    }

    /** 7.4's fallback order, and nothing else is ever reached without being asked for. */
    private val FALLBACK = listOf(Register.REFLECTIVE, Register.OBSERVATIONAL, Register.PLAIN)

    /**
     * The specificity at which a fact is notable enough for the editorial register.
     *
     * Three, because two is the ordinary shape of a rule in this catalog: a condition and
     * the floor that keeps it honest. A third condition is the point at which the rule is
     * describing a situation rather than a number.
     */
    const val NOTABLE_SPECIFICITY = 3

    /** 7.4 step 3 applies to the Report alone; the other volumes author no editorial line. */
    private fun isReport(purpose: Purpose): Boolean = when (purpose) {
        Purpose.REPORT_HEADLINE, Purpose.REPORT_OBSERVATION, Purpose.REPORT_PATTERN -> true
        Purpose.PULSE, Purpose.MOMENTUM_HEADLINE, Purpose.AREAS_BANNER -> false
    }
}
