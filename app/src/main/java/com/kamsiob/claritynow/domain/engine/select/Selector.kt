package com.kamsiob.claritynow.domain.engine.select

import com.kamsiob.claritynow.domain.engine.FactDates
import com.kamsiob.claritynow.domain.engine.FactSet
import com.kamsiob.claritynow.domain.engine.FamilyKey
import com.kamsiob.claritynow.domain.engine.FiringHistory
import com.kamsiob.claritynow.domain.engine.SilenceReason
import com.kamsiob.claritynow.domain.engine.StableHash
import com.kamsiob.claritynow.domain.engine.catalog.CallbackRequirement
import com.kamsiob.claritynow.domain.engine.catalog.ClarityCatalog
import com.kamsiob.claritynow.domain.engine.catalog.ClarityRule
import com.kamsiob.claritynow.domain.engine.catalog.Purpose
import com.kamsiob.claritynow.domain.engine.catalog.SubjectKind
import com.kamsiob.claritynow.domain.engine.realize.EngineMoment

/**
 * Layer 3. CLARITY_LOGIC_ENGINE.md 5.
 *
 * **Seven steps, in this order, and the order is not an implementation detail.** Qualify,
 * resolve callbacks, horizon, the Pulse repeat filter, cooldown, rank, take the head. Any
 * deviation produces device divergence, which is the one class of defect this project
 * cannot detect after the fact: two phones holding the same log say different things about
 * the same week, and nothing on either screen looks wrong.
 *
 * **Step 1b is numbered rather than inserted**, for the reason `PulseGeneration` numbers
 * its own 2b: the seven steps are cited by number from three documents and renumbering
 * them would break those citations in silence. It holds the two family scope gates of
 * `MASTER_BUILD_PROMPT.md` 14b, the week of withholding after a return and the capacity
 * gate, both of which decide whether a family gets a turn rather than which turn it gets.
 * Every step from 1b to 5 is a filter, so they commute and the placement changes no
 * outcome; it is first among them because 14b.4's word is `unavailable`. **It runs inside
 * the filter chain rather than over `qualified`**, so a purpose whose every qualifying
 * family was withheld reports `ALL_QUALIFIED_RULES_FILTERED` and not `NO_RULE_QUALIFIED`.
 * Those are different states, the simulator has to be able to tell them apart, and a week
 * the engine chose not to describe is not a week it had nothing to say about.
 *
 * The ranking is specificity descending, then priority descending, then key ascending, and
 * the last term is the load bearing one even though it decides almost nothing. Without it,
 * two rules of equal specificity and priority resolve by whatever order the catalog was
 * built in.
 *
 * ## Silence is an outcome, not a failure
 *
 * Four of the five `SilenceReason` values are produced here and every one of them is a
 * described state rather than an error. The fifth, `ALL_CANDIDATES_VETOED`, belongs to the
 * engine loop, because only the loop knows that every ranked selection was realized and
 * rejected.
 */
class Selector(private val catalog: ClarityCatalog) {

    /** Everything that could be said for [purpose], best first, or the reason for saying nothing. */
    fun select(
        purpose: Purpose,
        facts: FactSet,
        history: FiringHistory,
        moment: EngineMoment,
    ): SelectionOutcome {
        // 1. Qualify. Every criterion of the rule, against every subject its selector yields.
        val qualified = qualify(purpose, facts)
        if (qualified.isEmpty()) {
            return SelectionOutcome.Silent(
                if (nothingToDescribe(purpose, facts)) SilenceReason.INSUFFICIENT_DATA
                else SilenceReason.NO_RULE_QUALIFIED,
            )
        }

        // 1b. Availability. A family withheld for the week after a return, or gated by a
        // precedent, per MASTER_BUILD_PROMPT 14b.4 and 14b.9. See [FamilyAvailability] for
        // both tables and for why neither gate is a criterion.
        // 2. Resolve callbacks. A rule with an unresolvable callback does not fire and never
        // degrades into a version without the quote, because the sentence was authored
        // around it.
        // 3. Horizon. Drop a pair referencing a fact older than the rule declares.
        // 4. Repeat filter, Pulse only. Yesterday's family cannot be today's, and only
        // yesterday's, per 7.3. See [repeatsYesterday].
        // 5. Cooldown. A (family, subject) pair that fired inside its cooldown, which is
        // the family's own everywhere but the pattern section. See [PATTERN_COOLDOWN_DAYS].
        val ranked = qualified
            .filter { FamilyAvailability.unavailable(it, facts) == null }
            .mapNotNull { withCallback(it, facts, moment) }
            .filter { withinHorizon(it, facts, moment) }
            .filterNot { purpose == Purpose.PULSE && repeatsYesterday(it, facts, moment) }
            .filterNot { inCooldown(it, history, moment) }
            // 6. Rank.
            .sortedWith(RANKING)

        if (ranked.isEmpty()) return SelectionOutcome.Silent(SilenceReason.ALL_QUALIFIED_RULES_FILTERED)
        if (deliberatelySilent(purpose, ranked, moment)) {
            return SelectionOutcome.Silent(SilenceReason.DELIBERATE_SILENCE)
        }
        // 7. The head, with the rest kept so a vetoed candidate can fall through to the next.
        return SelectionOutcome.Ranked(ranked)
    }

    /**
     * Two to four observations, per the second paragraph of section 5.
     *
     * Take the head, then repeat from the ranking excluding any rule sharing a family with
     * one already selected and applying the incompatibility matrix, until [limit] are chosen
     * or the list is empty.
     *
     * **Never padded to reach a minimum.** One qualifying observation means one observation,
     * per 11.4. There is deliberately no floor in this function and nothing that reaches for
     * a weaker rule when the list runs short: a report with two observations is a report
     * about a week that had two things worth saying.
     */
    fun selectObservations(
        facts: FactSet,
        history: FiringHistory,
        moment: EngineMoment,
        headlineFamily: FamilyKey? = null,
        limit: Int = MAX_OBSERVATIONS,
    ): List<Selection> {
        val ranked = when (val outcome = select(Purpose.REPORT_OBSERVATION, facts, history, moment)) {
            is SelectionOutcome.Ranked -> outcome.selections
            is SelectionOutcome.Silent -> return emptyList()
        }
        val chosen = mutableListOf<Selection>()
        for (candidate in ranked) {
            if (chosen.size >= limit) break
            if (chosen.any { it.rule.family == candidate.rule.family }) continue
            if (IncompatibilityMatrix.conflictsWithHeadline(headlineFamily, candidate)) continue
            if (chosen.any { IncompatibilityMatrix.conflicts(it, candidate) }) continue
            chosen += candidate
        }
        return chosen
    }

    // ---------------------------------------------------------------- step 1

    /**
     * Every rule of this purpose, against every subject its selector yields.
     *
     * A subject selector reads only `FactSet.areas` and the active items in it, and
     * archived and tombstoned areas are absent from that map by construction. Prohibition 3
     * of 1.1 therefore holds here without a check: there is no subject to qualify.
     */
    private fun qualify(purpose: Purpose, facts: FactSet): List<Selection> {
        val windowDays = facts.window.dayCount
        val out = mutableListOf<Selection>()
        for (rule in catalog.rulesFor(purpose)) {
            for (subject in rule.subject.select(facts)) {
                if (rule.criteria.all { it.test(facts, subject) }) {
                    out += Selection(rule, purpose, subject, callback = null, windowDays = windowDays)
                }
            }
        }
        return out
    }

    /**
     * The floor below which the fact set describes nothing at all.
     *
     * Told apart from `NO_RULE_QUALIFIED` because the two are different states and the
     * simulator has to be able to see which happened. A week where nothing qualified is a
     * week the engine had nothing to say about. A fact set with no areas and no events is an
     * app nobody has used yet, and `MOMENTUM_HEADLINE` has a family for exactly that, so the
     * distinction is not academic: `cleanSlate` speaks where this would be silent, and if
     * both were the same reason nobody could tell a broken rule from an empty app.
     *
     * The pattern section has its own floor, which 6.3 sets at three weeks of data.
     */
    private fun nothingToDescribe(purpose: Purpose, facts: FactSet): Boolean = when (purpose) {
        Purpose.REPORT_PATTERN -> facts.history.weeksOfData < PATTERN_WEEKS
        else -> facts.window.totalEvents == 0 && facts.rollup.areasTotal == 0
    }

    // ---------------------------------------------------------------- step 2

    /**
     * Resolves a rule's callback against the stored answers, or drops the pair.
     *
     * The match is on family, on age, on the response key where the requirement names one,
     * and on the subject where the requirement demands it. `PulseFacts.recentAnswers` is
     * newest first, so the first match is the most recent one, which is the one a person
     * would remember giving.
     */
    private fun withCallback(selection: Selection, facts: FactSet, moment: EngineMoment): Selection? {
        val requirement = selection.rule.requiresCallback ?: return selection
        val resolved = resolve(requirement, selection, facts, moment) ?: return null
        return selection.copy(callback = resolved)
    }

    private fun resolve(
        requirement: CallbackRequirement,
        selection: Selection,
        facts: FactSet,
        moment: EngineMoment,
    ): ResolvedCallback? {
        for (answer in facts.pulse.recentAnswers) {
            if (answer.family != requirement.family) continue
            if (requirement.responseKey != null && answer.responseKey != requirement.responseKey) continue
            if (requirement.subjectMustMatch && answer.subjectId != selection.subjectId) continue
            val age = FactDates.daysBetweenKeys(answer.dateKey, moment.dateKey) ?: continue
            if (age < 0 || age > requirement.withinDays) continue
            return ResolvedCallback(answer, age)
        }
        return null
    }

    // ---------------------------------------------------------------- step 3

    /**
     * True when nothing the pair references is older than the rule's horizon.
     *
     * Section 4 defines a horizon as the maximum age of the oldest fact referenced, and
     * three things can be old: the window itself, the subject, and a quoted answer. The
     * window is the floor. An area's subject age is how long it has been still, which is
     * exactly what a sentence about a quiet area names. An item's is how long it has been
     * active. A callback's is how long ago it was given.
     *
     * [RECORD_PROBES] adds the one case the three generic terms miss, which is a family
     * whose sentence names a week rather than a subject. `mostActiveSince` renders the month
     * of a week that beat this one, and that week can be two years old while every other
     * fact in the set is seven days old.
     */
    private fun withinHorizon(selection: Selection, facts: FactSet, moment: EngineMoment): Boolean {
        val ages = mutableListOf(selection.windowDays)
        val subject = selection.subject
        selection.callback?.let { ages += it.ageDays }
        if (subject != null) {
            when (subject.kind) {
                // How long the area has been still, and deliberately not how old it is. An
                // area's age is not a fact any line renders, and counting it would make
                // every area older than ninety days unreachable by every rule with a ninety
                // day horizon, which is most of the areas in a year old install.
                SubjectKind.AREA -> facts.areas[subject.id]?.let { area ->
                    if (area.daysSinceLastEvent != Int.MAX_VALUE) ages += area.daysSinceLastEvent
                }
                SubjectKind.ITEM -> facts.areas.values
                    .firstOrNull { it.activeItemId == subject.id }
                    ?.activeItemAgeDays
                    ?.let { ages += it }
            }
        }
        RECORD_PROBES[selection.rule.family]?.invoke(facts)?.let { weekKey ->
            FactDates.daysBetweenKeys(weekKey, moment.dateKey)?.let { ages += it }
        }
        return ages.max() <= selection.rule.horizonDays
    }

    // ---------------------------------------------------------------- step 4

    /**
     * True when this pair's family is the one the Pulse used **yesterday**. Section 5 step 4,
     * bounded by 7.3.
     *
     * ## The bound is the rule, and it was missing
     *
     * Step 4 names `PulseFacts.lastGeneratedFamily`, which is the family of the most recent
     * Pulse generated at any point in the past. That equals yesterday's family only on a day
     * after a day the Pulse spoke. 7.3 states the rule the other way round and in words:
     * the cooldown covers `cooldownDays`, and it is "separate from the no-repeat rule, which
     * covers **only yesterday**". Section 12's own table calls the filter "yesterday's family
     * cannot be today's". Three statements say one day; the fact name says forever, and the
     * code followed the name.
     *
     * ## What the unbounded reading did
     *
     * **It is self-reinforcing, which is the part no reading of the code shows.** A blocked
     * family generates no `PULSE_GENERATED`, so `lastGeneratedFamily` does not advance, so
     * the same family is blocked again tomorrow. Where one family is the only one a life
     * qualifies for, the Pulse stops for good. Measured across eleven persona years: of 869
     * days where this filter alone emptied the candidate list, only 169 were a gap of one
     * day. 214 were a gap of ninety days or more, and one persona, four areas and a steady
     * week, spoke nine times in January and was then held silent for **348 consecutive
     * days** by the family of a Pulse from January 20.
     *
     * With the bound, a lock can last exactly one day: the day after, no Pulse was generated
     * yesterday and this filter does not apply at all. The absorbing state is not made
     * unlikely, it is made unreachable.
     *
     * ## What still stands behind it
     *
     * Everything the rule was written to prevent. The same `(family, subjectId)` pair waits
     * `cooldownDays`, which is three at the shortest and thirty at the longest, so no pair
     * can return inside three days by any route. 7.6's ninety day variant exclusion
     * guarantees a different sentence. This filter is the only one of the three that reaches
     * a *different* subject of the same family, and on consecutive days that is exactly what
     * it still does.
     *
     * A day count of zero is included for the degenerate case of a second selection on a day
     * the Pulse has already spoken on. `PulseGeneration` holds at most one entry per local
     * day so it should not arise, and a repeat filter that let it through would be the one
     * place the same family could appear twice in one day.
     *
     * An unparseable key reads as available, which is `FactDates.daysBetweenKeys`'s stated
     * direction and the same one `variantUsedWithin` takes: losing one exclusion costs a
     * repeat, and treating an unreadable key as yesterday would restore the permanent block.
     */
    private fun repeatsYesterday(selection: Selection, facts: FactSet, moment: EngineMoment): Boolean {
        if (selection.rule.family != facts.pulse.lastGeneratedFamily) return false
        val lastKey = facts.pulse.lastGeneratedDateKey ?: return false
        val since = FactDates.daysBetweenKeys(lastKey, moment.dateKey) ?: return false
        return since in 0..REPEAT_WINDOW_DAYS
    }

    // ---------------------------------------------------------------- step 5

    /** True when this `(family, subject)` pair fired inside the cooldown that applies to it. */
    private fun inCooldown(selection: Selection, history: FiringHistory, moment: EngineMoment): Boolean {
        val family = catalog.familyFor(selection.rule) ?: return true
        return history.inCooldown(
            selection.rule.family,
            selection.subjectId,
            moment.dateKey,
            cooldownDaysFor(selection, family.cooldownDays),
        )
    }

    /**
     * How long this pair waits, which is what its family declares everywhere but the
     * pattern section. 7.3.
     *
     * **The floor is a property of the selection and not of the declaration**, because one
     * family key serves two surfaces. `decliningActivity` is a headline family and a
     * pattern family at the same time and they share one `(family, subjectId)` cooldown
     * key, so a longer number on the declaration in `EngineFamilies` would hold the
     * headline back as well, and only the pattern section was ordered to wait three weeks.
     *
     * `maxOf` rather than a replacement, so this can only ever lengthen a wait. Every
     * pattern family declares the flat Report fourteen today and the floor decides all of
     * them; one that later declares longer keeps its own number.
     */
    private fun cooldownDaysFor(selection: Selection, declared: Int): Int =
        if (selection.purpose == Purpose.REPORT_PATTERN) maxOf(declared, PATTERN_COOLDOWN_DAYS) else declared

    // ---------------------------------------------------------------- 5.1

    /**
     * CLARITY_LOGIC_ENGINE.md 5.1.
     *
     * **The only place the engine chooses not to speak when it could.** A Pulse that appears
     * every single day becomes wallpaper; one that occasionally does not appear reads as
     * discretion. It fires only where the best thing available is a single bare condition,
     * so it can never suppress a specificity 2 or higher observation, and it is a function
     * of the date alone so two devices agree without sharing anything.
     *
     * `StableHash.bucket` is `% 3` made sign safe. `StableHash.of` is signed and the sign of
     * a Kotlin remainder follows the dividend, so a bare `% 3 == 0` would be correct here
     * and would invite the next person to write `% 3 == 1` somewhere and get a third of the
     * days on one platform and a sixth on another.
     */
    private fun deliberatelySilent(
        purpose: Purpose,
        ranked: List<Selection>,
        moment: EngineMoment,
    ): Boolean {
        if (purpose != Purpose.PULSE) return false
        if (ranked.maxOf { it.specificity } > BARE_SPECIFICITY) return false
        return StableHash.bucket(moment.dateKey, SILENCE_IN) == 0
    }

    companion object {

        /**
         * Section 5 step 6, over the pair rather than the rule.
         *
         * Specificity descending, then priority descending, then key ascending, which is
         * `ClarityRule.RANKING`, and then the subject id. The last term is here for the same
         * reason the key term is there: one rule can qualify for four areas at once, and
         * without it the order of those four would be the order the subject selector
         * happened to return them in.
         *
         * Public so a test can assert the order rather than infer it from which sentence
         * came out.
         */
        val RANKING: Comparator<Selection> =
            compareBy<Selection, ClarityRule>(ClarityRule.RANKING) { it.rule }.thenBy { it.subjectId.orEmpty() }

        /**
         * Step 4's reach in days, which 7.3 states as yesterday and yesterday alone.
         *
         * One rather than a tunable, because it is not a tuning number: it is the length of
         * the word "yesterday". A longer wait for a family is `cooldownDays`, which every
         * family already declares and which the table in 7.3 sets per family for reasons
         * this filter has no way to know.
         */
        const val REPEAT_WINDOW_DAYS = 1

        /** 5.1. A single bare condition, and nothing more interesting available. */
        const val BARE_SPECIFICITY = 1

        /** 5.1. One day in three. */
        const val SILENCE_IN = 3

        /** Section 5, second paragraph. */
        const val MAX_OBSERVATIONS = 4

        /** 6.3. No pattern may fire under three weeks of data. */
        const val PATTERN_WEEKS = 3

        /**
         * 7.3. The three weeks a pattern family waits, keyed per `(family, subjectId)`.
         *
         * ## What it fixes
         *
         * The pattern section had no wait of its own and inherited the flat fourteen day
         * Report cooldown, which cannot block a weekly report at all. A report records its
         * firing against its **week start** key and the next report selects against its own
         * **week end**, so two consecutive reports are fourteen days apart on the only
         * clock `FiringHistory` keeps, and `14 in 0 until 14` is false. The facts phase
         * measured what followed: 419 pattern slots across a simulated year, 416 filled,
         * and three families took 402 of them. Selection is deterministic, so on a person
         * whose week keeps its shape the same rule wins every time and the seven families
         * that also qualify lose every time.
         *
         * ## What it guarantees
         *
         * One `(family, subjectId)` pair never appears in two consecutive reports, so over
         * `n` consecutive weekly reports no pair takes more than `(n + 1) / 2` of them.
         *
         * ## What it does not guarantee
         *
         * - **That a third family speaks.** Ranking is deterministic and a cooldown only
         *   moves the winner aside for as long as it lasts, so the head rotates among
         *   exactly `ceil(days / 7) - 1` pairs and everything below them still loses every
         *   week. Three weeks rotates two. Rotating all seven starved families would take
         *   eight weeks, and that number is the owner's to set rather than this one's
         * - **That one family cannot hold the section.** The key is per subject, exactly as
         *   7.3 states it for the Pulse, so `areaGoneQuiet` with two quiet areas fills
         *   consecutive weeks under two subjects without ever entering its own cooldown
         * - **That a slot stays filled.** A cooldown removes candidates and never adds one.
         *   Where a single pair qualifies and it is the pair that spoke last week, the
         *   section falls silent where it used to speak, so the pattern section's share of
         *   silence rises with this number
         *
         * `PatternCooldownTest` measures all four of those statements against the real
         * catalog rather than restating them.
         */
        const val PATTERN_COOLDOWN_DAYS = 21

        /**
         * Families whose sentence names a week, and the week key it would name.
         *
         * Both of these render a month into a slot, `since March`, and the month can be a
         * year old on a fact set where nothing else is. Without a probe the horizon on those
         * two rules would measure the window and pass everything.
         */
        val RECORD_PROBES: Map<FamilyKey, (FactSet) -> String?> = mapOf(
            "mostActiveSince" to { facts: FactSet -> facts.history.mostRecentBetterWeekKey },
            "personalBest" to { facts: FactSet -> facts.history.personalBestWeekKey },
        )
    }
}

/** What layer 3 produced. */
sealed interface SelectionOutcome {

    /**
     * Everything that qualified, best first.
     *
     * The whole list rather than the head, because section 8 says a vetoed candidate causes
     * the **next ranked selection** to be realized. Returning only the head would make that
     * impossible without running selection twice, and running it twice is how two devices
     * end up disagreeing about what was already tried.
     */
    data class Ranked(val selections: List<Selection>) : SelectionOutcome

    /** Nothing to say, and which of the five reasons it was. */
    data class Silent(val reason: SilenceReason) : SelectionOutcome
}
