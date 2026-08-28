package com.kamsiob.claritynow.domain.engine.realize

import com.kamsiob.claritynow.domain.engine.FamilyKey
import com.kamsiob.claritynow.domain.engine.FiringHistory
import com.kamsiob.claritynow.domain.engine.PartOfDay
import com.kamsiob.claritynow.domain.engine.StableHash
import com.kamsiob.claritynow.domain.engine.catalog.Purpose
import com.kamsiob.claritynow.domain.engine.catalog.Register
import com.kamsiob.claritynow.domain.engine.catalog.Variant

/**
 * Which voice a sentence is said in. CLARITY_LOGIC_ENGINE.md 7.4, and 6.4 for the one
 * family whose register is a constraint on grammar rather than a matter of taste.
 *
 * ## Why this is a choice and not an order
 *
 * 7.4 gives four steps. **The first three are rules about content and the fourth is not.**
 * `NEUTRAL_AGENT` answers a question about the fact, the editorial budget answers a
 * question about the lead, and the time of day answers a question about how far the reader
 * stands from the day being described. Step 4 answered nothing: it was a list, and the
 * realizer took the first register on it with a line it could fill, so the first entry won
 * every time a rule left the question open.
 *
 * Measured over eleven simulated persona years that cost the corpus half of itself. The
 * Pulse spoke plain on 1,080 of 1,081 firings, Momentum and the Areas banner spoke
 * reflective on all 5,594, and 487 of volume 3's 748 register tagged lines sat in registers
 * nothing could ask for. 11.1 sizes a bench per stage; the chooser sees a register; so a
 * stage of sixty lines split three ways bought twenty lines of variety.
 *
 * ## What replaced it
 *
 * The three rules stay, as **tiers of equal standing** rather than as positions in a list,
 * and the fourth step becomes a choice among whatever the rules left open. Inside a tier
 * the register is picked the way 7.6 picks a line: hold back the voice the family used most
 * recently, then take the head of a `StableHash` ordering keyed on the date. That is not a
 * new instrument, it is the existing one asked one level up, so the determinism and the
 * cross-device arguments in 7.6 carry over unchanged.
 *
 * **Step 2 gained a second register out of 7.4's own words.** "Dawn and midday prefer
 * `PLAIN` and `OBSERVATIONAL`" names two registers as equally preferred, and the old code
 * turned that into an order in which plain always won. It is one tier of two.
 *
 * ## Why a rotation, when a rule would be better
 *
 * `design-v3.md` 15 asks for the answer that is not the obvious one, and the obvious answer
 * here is a fourth rule keyed on something in the fact set. It was rejected because the
 * corpus does not author to one. Where a situation genuinely subdivides, the corpus splits
 * it into another family or another stage; a bench like `bn.start` holds sixty two lines
 * under one trigger, in four voices, and no fact distinguishes *Early in the week.* from
 * *The week is young.* A fourth rule would make the engine claim a distinction its own
 * language never made, and an author would then be writing lines for a condition nobody
 * stated.
 *
 * **`NEUTRAL_AGENT` is not a fourth voice in the rotation**, and it is absent from the open
 * tier. 7.4: a family that is neutral or positive never uses it, because making the fact
 * the subject of a good week reads as withholding credit. `EDITORIAL` is absent for the
 * matching reason: it is budgeted, so it is offered or it is not.
 */
object RegisterChoice {

    /**
     * The tiers to try, best first. A tier holds registers of equal standing.
     *
     * The caller walks the tiers in order, and inside a tier it takes [choose]'s answer and
     * falls through to the next register in the same tier when that one has nothing it can
     * fill. **A register with nothing sayable in it falls through and never produces
     * silence**, which is the property the whole preference exists to have: a bench with no
     * reflective line at stage 3 must produce the next voice down.
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
    ): List<Set<Register>> {
        val tiers = mutableListOf<Set<Register>>()
        // 1. The unflattering register, alone, and only here. This one is not a choice:
        // 7.4 says prefer it, and the whole point of the flag is that the voice is decided
        // by the content rather than by the day.
        if (unflattering) tiers += setOf(Register.NEUTRAL_AGENT)
        // 3. The editorial budget, Report only. A third editorial lead is re-realized in
        // the open tier, which is what withholding this one does.
        if (isReport(purpose) && notable && !editorialBudgetSpent) tiers += setOf(Register.EDITORIAL)
        // 2. Time of day, Pulse only, and 7.4 names two registers for the first half of the
        // day and one for the second. The band boundaries are PartOfDay's, which are the
        // app's own dawn, midday and evening rather than an even quarter split.
        if (purpose == Purpose.PULSE) {
            tiers += when (partOfDay) {
                PartOfDay.MORNING, PartOfDay.AFTERNOON -> setOf(Register.PLAIN, Register.OBSERVATIONAL)
                PartOfDay.EVENING, PartOfDay.NIGHT -> setOf(Register.REFLECTIVE)
            }
        }
        // 4. Everything the steps above left open, as one tier rather than as an order.
        val open = OPEN - tiers.flatten().toSet()
        if (open.isNotEmpty()) tiers += open
        return tiers
    }

    /**
     * The registers 7.4 step 4 leaves open. Never `EDITORIAL`, never `NEUTRAL_AGENT`.
     *
     * Both of those are reached by a rule or not at all, so neither can sit in a tier that
     * is chosen among: a budgeted voice that turned up one time in three would not be
     * budgeted, and an unflattering voice offered to a good week is the thing 7.4 exists to
     * prevent.
     */
    val OPEN: Set<Register> = setOf(Register.REFLECTIVE, Register.OBSERVATIONAL, Register.PLAIN)

    /**
     * Which of [offered] speaks today, or null when nothing was offered.
     *
     * 7.6 one level up. [lastSpoken] is held back where anything else remains, for the
     * reason `VariantChoice` holds back the most recently used line: the voice a person
     * heard last is the only one they might recognize. The rest are ordered by
     * `stableHash(dateKey + familyKey + stage + register)` and the head is taken, so two
     * devices holding the same merged log reach the same voice with no shared state.
     *
     * The register name is the last term of the hashed key and the tie break, so the answer
     * cannot depend on the iteration order of [offered]. That matters more than it looks:
     * the determinism test in 14 exists because a map iteration order leaking into a
     * decision is invisible at three keys and stops being invisible above that.
     */
    fun choose(
        offered: Set<Register>,
        lastSpoken: Register?,
        dateKey: String,
        familyKey: FamilyKey,
        stage: Int,
    ): Register? {
        if (offered.isEmpty()) return null
        val pool = offered.filterNot { it == lastSpoken }.ifEmpty { offered.toList() }
        return pool.minWithOrNull(
            compareBy<Register> { StableHash.spread(dateKey + familyKey + stage + it.name) }.thenBy { it.name },
        )
    }

    /**
     * The voice [variants]' family used most recently, or null when it has never spoken.
     *
     * Read across the whole family rather than across the selected stage, because a person
     * hears a family and not a rung of its ladder: `persistence` speaking plainly yesterday
     * at stage 1 is the plain voice they heard, whatever stage it reaches today.
     *
     * Resolved by greatest `dateKey` with the register name breaking a tie, which is the
     * same total order `FiringHistory` documents for every other "most recent" in the
     * engine, and for the same reason.
     */
    fun lastSpoken(variants: List<Variant>, history: FiringHistory): Register? = variants
        .asSequence()
        .mapNotNull { variant -> history.variantsUsed[variant.key]?.let { it to variant.register } }
        .maxWithOrNull(compareBy<Pair<String, Register>> { it.first }.thenBy { it.second.name })
        ?.second

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
