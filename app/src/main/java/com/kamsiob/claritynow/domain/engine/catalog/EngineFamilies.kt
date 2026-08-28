package com.kamsiob.claritynow.domain.engine.catalog

import com.kamsiob.claritynow.domain.engine.FamilyKey

/**
 * The families the engine declares, their cooldowns and their register flag.
 * CLARITY_LOGIC_ENGINE.md 6.1, 6.3, 6.5, 7.3 and 7.4.
 *
 * This is the one place in this package where a value is authored in Kotlin rather than
 * read out of a corpus file, and the boundary is deliberate. The corpus owns what is
 * said and every threshold behind it, per 7.3. The engine owns which families exist,
 * what they are called, how long they wait before repeating, and whether they are
 * allowed to reach the neutral agent register. None of those four is a sentence, and
 * none of them belongs in a file an author edits for voice.
 *
 * Both directions are checked. [CatalogIntegrity] asserts that every family declared
 * here is present in a corpus file and that every family in a corpus file is declared
 * here, so a family added to one and not the other fails the build rather than
 * disappearing.
 */
object EngineFamilies {

    /**
     * Cooldown for every Report family that 7.3 does not name individually.
     *
     * `selfReportVsData` never repeats on a subject at all, and `hardStretch` waits six
     * weeks per 6.4. Both are below.
     */
    const val REPORT_DEFAULT_COOLDOWN_DAYS = 14

    /** 6.4 and 7.3. Six weeks. */
    const val HARD_STRETCH_COOLDOWN_DAYS = 42

    /**
     * `selfReportVsData` never repeats on the same subject, ever. 7.3 and the
     * incompatibility matrix in 9.1, which says the same thing a second way: rarity is
     * what gives the flagship its force, and a callback that arrives twice about one
     * item reads as a system with one trick.
     */
    const val NEVER_REPEATS_COOLDOWN_DAYS = Int.MAX_VALUE

    /**
     * Momentum and the banner declare no cooldown, and 7.3's table does not list them.
     *
     * They are not on a ladder and they do not fire: Momentum is recomputed on screen
     * entry and the banner at most once per hour of app use, and 6.5 puts that throttle
     * in the ViewModel rather than in the engine. A cooldown here would be a second
     * throttle in a second place, disagreeing with the first.
     */
    const val NO_COOLDOWN = 0

    /** One family as the engine declares it. */
    data class Declaration(
        val key: FamilyKey,
        val purpose: Purpose,
        val cooldownDays: Int,
        /** The corpus key prefix, where the corpus abbreviates. Null means the prefix is the key, lowercased. */
        val keyPrefix: String? = null,
    ) {
        /** The prefix a corpus line carries, per each volume's own prefix rule. */
        val prefix: String get() = keyPrefix ?: key.lowercase()
    }

    /** CLARITY_LOGIC_ENGINE.md 6.1 and the cooldown table in 7.3. */
    val PULSE: List<Declaration> = listOf(
        Declaration("persistence", Purpose.PULSE, cooldownDays = 3),
        Declaration("concentration", Purpose.PULSE, cooldownDays = 4),
        Declaration("accumulation", Purpose.PULSE, cooldownDays = 4),
        Declaration("throughput", Purpose.PULSE, cooldownDays = 4),
        Declaration("spread", Purpose.PULSE, cooldownDays = 5),
        Declaration("quietDay", Purpose.PULSE, cooldownDays = 5),
        Declaration("switching", Purpose.PULSE, cooldownDays = 7),
        Declaration("burst", Purpose.PULSE, cooldownDays = 10),
        Declaration("queueDrain", Purpose.PULSE, cooldownDays = 14),
        Declaration("rebalance", Purpose.PULSE, cooldownDays = 21),
        Declaration("freshStart", Purpose.PULSE, cooldownDays = 30),
    )

    /** CLARITY_LOGIC_ENGINE.md 6.3, headline list. Prefixes come from the table in the corpus. */
    val REPORT_HEADLINE: List<FamilyKey> = listOf(
        "quietWeek", "netOutflow", "netInflow", "singleFocus", "balanced", "focusProtected",
        "personalBest", "mostActiveSince", "decliningActivity", "risingActivity", "comeback",
        "queuePressure", "clearing", "steadyPace", "fragmented", "firstWeek", "datedFallback",
    )

    /** CLARITY_LOGIC_ENGINE.md 6.3, observation list. */
    val REPORT_OBSERVATION: List<FamilyKey> = listOf(
        "singleFocus", "intakeVsOutput", "focusInvestment", "neglectedArea", "completionSplit",
        "selfReportVsData", "quietWeek", "queuePressure", "areaRevival", "persistentItem",
        "personalBest", "mostActiveSince", "dayShape", "timeOfDay", "switchingBehavior",
        "focusAbandonment", "queueDrained", "steadyPace", "firstMilestone", "areaBalance",
        "hardStretch", "familiarDip", "estimateCalibration",
    )

    /** CLARITY_LOGIC_ENGINE.md 6.3, pattern list. All require `weeksOfData >= 3`. */
    val REPORT_PATTERN: List<FamilyKey> = listOf(
        "shiftingFocus", "growingQueues", "improvingThroughput", "decliningActivity",
        "areaGoneQuiet", "consistentRhythm", "narrowingFocus", "broadeningFocus",
        "focusHabitForming", "focusHabitFading", "reportedVsActual", "queueEquilibrium",
        "weekendShift", "abandonmentPattern", "comebackPattern", "insufficientData",
    )

    /** CLARITY_LOGIC_ENGINE.md 6.5. */
    val MOMENTUM_HEADLINE: List<FamilyKey> = listOf(
        "steadyStretch", "quietStretch", "comeback", "balancedWeek", "singleAreaWeek",
        "strongPace", "firstDays", "cleanSlate",
    )

    /** CLARITY_LOGIC_ENGINE.md 6.5. */
    val AREAS_BANNER: List<FamilyKey> = listOf(
        "weekStarting", "weekBuilding", "weekStrong", "weekQuiet", "weekMixed",
    )

    /** The cooldown for a Report family, per 7.3. */
    fun reportCooldownDays(family: FamilyKey): Int = when (family) {
        "selfReportVsData" -> NEVER_REPEATS_COOLDOWN_DAYS
        "hardStretch" -> HARD_STRETCH_COOLDOWN_DAYS
        else -> REPORT_DEFAULT_COOLDOWN_DAYS
    }

    /** Every declared family key, for whichever purpose. */
    fun keysFor(purpose: Purpose): List<FamilyKey> = when (purpose) {
        Purpose.PULSE -> PULSE.map { it.key }
        Purpose.REPORT_HEADLINE -> REPORT_HEADLINE
        Purpose.REPORT_OBSERVATION -> REPORT_OBSERVATION
        Purpose.REPORT_PATTERN -> REPORT_PATTERN
        Purpose.MOMENTUM_HEADLINE -> MOMENTUM_HEADLINE
        Purpose.AREAS_BANNER -> AREAS_BANNER
    }
}

/**
 * Which rules carry `unflattering = true`. CLARITY_LOGIC_ENGINE.md 7.4.
 *
 * 7.4 enumerates these so it is not a judgment call, and everything not enumerated is
 * false. The flag drives one thing: whether the realizer may reach the neutral agent
 * register, where the fact becomes the grammatical subject instead of the person.
 * *Nine things arrived. Six left.* rather than *You added nine things and finished six.*
 *
 * **A family that is neutral or positive never uses that register**, because making the
 * fact the subject of a good week reads as withholding credit.
 *
 * **Two entries in 7.4 name stages their corpus family does not have**, and both are
 * resolved here rather than guessed at each call site. 7.4 marks `persistentItem` at
 * stages 3 and 4 and `switchingBehavior` at stage 2, and both are single stage families
 * in `CORPUS_2_REPORT.md`. Marking the whole family unflattering would over apply a
 * qualification 7.4 wrote deliberately; marking none of it would leave the three `[N]`
 * lines authored for `ob.swi` unreachable. So the qualification survives as a property
 * of the **rule** rather than of the stage: the catalog declares two rules per family,
 * split at the magnitude the corpus already states for the matching Pulse ladder, and
 * only the higher one is unflattering. Recorded in the phase 5 report.
 *
 * **Addendum 01 7c's widening has landed, and it added two entries rather than a list.**
 * 14b.10 widens the enumeration to cover every rule concerning a decline, a gap, a neglect,
 * an imbalance or an unmet expectation, and 7.4 now names the two the old enumeration
 * missed: `intakeVsOutput` **stage 1**, whose own corpus header reads `mild imbalance`, and
 * `estimateCalibration`, whose whole subject is a prediction that days did not meet.
 *
 * **The widening stops where the register does, and 7.4 says so.** The flag has exactly one
 * effect, which is whether the realizer may reach `NEUTRAL_AGENT`, and `CORPUS_2_REPORT.md`
 * carries a register tag in section 2 alone: `ReportWalker` refuses one in a headline or a
 * pattern line, so those benches are `PLAIN` by construction. Marking a headline rule would
 * change nothing a person reads and would owe a bench nobody can author. The three families
 * considered and left alone are recorded in 7.4 with the reason for each.
 */
object UnflatteringRules {

    /** Report families where every rule is unflattering, whatever its stage. 7.4. */
    val WHOLE_FAMILY: Set<FamilyKey> = setOf(
        "estimateCalibration",
        "queuePressure",
        "focusAbandonment",
        "decliningActivity",
        "quietWeek",
        "hardStretch",
        "neglectedArea",
        "growingQueues",
        "areaGoneQuiet",
        "narrowingFocus",
        "focusHabitFading",
        "abandonmentPattern",
    )

    /** Report families unflattering only at named stages. 7.4. */
    val BY_STAGE: Map<FamilyKey, Set<Int>> = mapOf(
        "intakeVsOutput" to setOf(1, 2),
        "singleFocus" to setOf(2),
    )

    /**
     * The two families 7.4 qualifies by a stage the corpus does not have. See the class
     * note. The rule catalog splits each into a low and a high rule and marks only the
     * high one, so the value here is the magnitude at which the high rule begins, taken
     * from the matching Pulse ladder in `CORPUS_1_PULSE.md`.
     */
    val SPLIT_AT_MAGNITUDE: Map<FamilyKey, String> = mapOf(
        "persistentItem" to "activeItemAgeDays at 14 or more, the start of persistence stage 3",
        "switchingBehavior" to "swaps at 2 or more, the start of switching stage 2",
    )

    /**
     * Whether a rule for [family] at [stage] is unflattering, for every family except the
     * two in [SPLIT_AT_MAGNITUDE], which the catalog decides per rule.
     *
     * A headline and an observation can share a family key, and 7.4 names some families
     * without qualifying which surface it means. `quietWeek` and `decliningActivity` are
     * unflattering wherever they appear, which is what the unqualified entry says.
     */
    fun isUnflattering(family: FamilyKey, stage: Int): Boolean =
        family in WHOLE_FAMILY || stage in (BY_STAGE[family] ?: emptySet())
}
