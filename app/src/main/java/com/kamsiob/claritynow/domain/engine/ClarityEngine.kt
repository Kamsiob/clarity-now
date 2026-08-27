package com.kamsiob.claritynow.domain.engine

import com.kamsiob.claritynow.domain.engine.catalog.ClarityCatalog
import com.kamsiob.claritynow.domain.engine.catalog.LengthBand
import com.kamsiob.claritynow.domain.engine.catalog.Purpose
import com.kamsiob.claritynow.domain.engine.catalog.Register
import com.kamsiob.claritynow.domain.engine.catalog.ResponseOption
import com.kamsiob.claritynow.domain.engine.realize.Candidate
import com.kamsiob.claritynow.domain.engine.realize.EngineMoment
import com.kamsiob.claritynow.domain.engine.realize.Realization
import com.kamsiob.claritynow.domain.engine.realize.RealizationOptions
import com.kamsiob.claritynow.domain.engine.realize.Realizer
import com.kamsiob.claritynow.domain.engine.select.Selection
import com.kamsiob.claritynow.domain.engine.select.SelectionOutcome
import com.kamsiob.claritynow.domain.engine.select.Selector
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId

/**
 * The engine, layers 3 to 5 in order. CLARITY_LOGIC_ENGINE.md 2.2.
 *
 * ```
 * observe(facts, history, purpose)
 *   -> Selector    ranked selections, or one of five reasons for silence
 *   -> Realizer    the first selection that can be filled from the facts
 *   -> Validator   which may veto it, sending the next one round again
 *   -> Spoke, or Silent
 * ```
 *
 * ## Purity, and why the zone is here
 *
 * Nothing below reads a clock, a random number, DataStore or an Android API, and
 * `DomainPurityTest` scans this package to make sure of it. Two facts the layers need are
 * therefore constructor parameters rather than ambient values.
 *
 * **The zone.** 7.6 hashes the date key, 5.1 buckets it and every exclusion window in
 * `FiringHistory` measures against it, so the engine needs to know which local day it is
 * speaking on. `FactSet` carries the window as two instants and no zone, and
 * `ZoneId.systemDefault()` is the documented cause of two Pulses in one day or none at all.
 * So the zone the extractor counted with is handed in once, at construction, and
 * [momentOf] derives both the date key and the part of day from the window itself. The
 * signature 2.2 specifies, `observe(facts, history, purpose)`, is unchanged.
 *
 * **The validator.** Layer 5 is a separate slice and this class must not be able to
 * bypass it, so it arrives as a [CandidateValidator] with no default. 11.4: never bypass
 * the validator, not for a simple sentence, not for an empty state, not to fix a bug.
 *
 * ## What it never does
 *
 * It never speaks twice for one purpose, never pads a section to reach a minimum, and
 * never returns a sentence that has not been through layer 5.
 */
class ClarityEngine(
    catalog: ClarityCatalog,
    private val validator: CandidateValidator,
    private val zone: ZoneId,
) {

    private val selector = Selector(catalog)

    private val realizer = Realizer(catalog, zone)

    /** CLARITY_LOGIC_ENGINE.md 2.2. One observation, or one reason for silence. */
    fun observe(facts: FactSet, history: FiringHistory, purpose: Purpose): EngineResult {
        val moment = momentOf(facts)
        return when (val outcome = selector.select(purpose, facts, history, moment)) {
            is SelectionOutcome.Silent -> EngineResult.Silent(outcome.reason)
            is SelectionOutcome.Ranked -> speak(outcome.selections, facts, history, moment)
        }
    }

    /**
     * The Report's two to four observations, section 5's second paragraph.
     *
     * The three composition constraints that belong to layers 3 and 4 are applied here: the
     * family exclusion and the incompatibility matrix in the selector, the editorial budget
     * from 7.4, and the length band alternation from 7.5. The rest of section 9, which is
     * the one area two mentions rule and the number consistency check across a whole
     * report, belongs to the composer that assembles the page.
     *
     * **Never padded.** If two observations qualify, two come back.
     */
    fun observeObservations(
        facts: FactSet,
        history: FiringHistory,
        headlineFamily: FamilyKey? = null,
        limit: Int = MAX_OBSERVATIONS,
    ): List<RenderedOutput> {
        val moment = momentOf(facts)
        val chosen = selector.selectObservations(facts, history, moment, headlineFamily, limit)
        val spoken = mutableListOf<RenderedOutput>()
        var editorialUsed = 0
        var previousBand: LengthBand? = null
        for (selection in chosen) {
            val options = RealizationOptions(
                avoidBand = previousBand,
                editorialBudgetSpent = editorialUsed >= EDITORIAL_BUDGET,
            )
            val output = realizeOne(selection, facts, history, moment, options) ?: continue
            if (output.meta.register == Register.EDITORIAL) editorialUsed++
            previousBand = output.meta.lengthBand
            spoken += output
        }
        return spoken
    }

    /**
     * The local day the engine is speaking on, and where in it.
     *
     * **A window that ends exactly at local midnight is a completed day being reflected on
     * in the morning.** That is the Pulse's own shape, per `MASTER_BUILD_PROMPT.md` 11.3:
     * before 17:00 the reflection period is yesterday, so the window ends at the boundary,
     * and at or after 17:00 it is today so far, so the window ends at the moment of asking.
     * Reading the hour off the end instant would call the first of those night, and 7.4
     * would answer the morning Pulse in the evening's voice.
     *
     * The date key is the local date of the window end in both cases, which is the day the
     * observation is shown on: midnight belongs to the day it opens.
     */
    fun momentOf(facts: FactSet): EngineMoment {
        val end = Instant.ofEpochMilli(facts.window.endInstant).atZone(zone)
        val partOfDay =
            if (end.toLocalTime() == LocalTime.MIDNIGHT) PartOfDay.MORNING else PartOfDay.of(end.hour)
        return EngineMoment(FactDates.keyOf(facts.window.endInstant, zone), partOfDay)
    }

    /**
     * Realizes and validates down the ranked list until something survives.
     *
     * Two ways to reach the end of the list, and they are different states. Everything was
     * vetoed, which means layer 5 caught something and is `ALL_CANDIDATES_VETOED`. Or
     * nothing could be filled from the facts on hand, which is a bench problem rather than
     * an integrity one and is `ALL_QUALIFIED_RULES_FILTERED`. The simulator prints the
     * reason, and a run where the first is common is a run with a rule that qualifies on
     * facts its sentences cannot describe.
     */
    private fun speak(
        selections: List<Selection>,
        facts: FactSet,
        history: FiringHistory,
        moment: EngineMoment,
    ): EngineResult {
        var vetoed = false
        for (selection in selections) {
            when (val realization = realizer.realize(selection, facts, history, moment)) {
                is Realization.NotProducible -> continue
                is Realization.Rendered -> {
                    if (validator.veto(realization.candidate, facts) != null) {
                        vetoed = true
                        continue
                    }
                    return EngineResult.Spoke(outputOf(realization.candidate))
                }
            }
        }
        return EngineResult.Silent(
            if (vetoed) SilenceReason.ALL_CANDIDATES_VETOED else SilenceReason.ALL_QUALIFIED_RULES_FILTERED,
        )
    }

    private fun realizeOne(
        selection: Selection,
        facts: FactSet,
        history: FiringHistory,
        moment: EngineMoment,
        options: RealizationOptions,
    ): RenderedOutput? {
        val realization = realizer.realize(selection, facts, history, moment, options)
        if (realization !is Realization.Rendered) return null
        if (validator.veto(realization.candidate, facts) != null) return null
        return outputOf(realization.candidate)
    }

    private fun outputOf(candidate: Candidate) = RenderedOutput(
        text = candidate.rendered,
        question = candidate.renderedQuestion,
        responses = candidate.responses,
        meta = candidate,
    )

    private companion object {

        /** Section 5, second paragraph. Two to four, and never padded to reach two. */
        const val MAX_OBSERVATIONS = 4

        /** 7.4 step 3. At most two editorial leads per report; a third is re-realized. */
        const val EDITORIAL_BUDGET = 2
    }
}

/** CLARITY_LOGIC_ENGINE.md 2.2. */
sealed interface EngineResult {

    data class Spoke(val output: RenderedOutput) : EngineResult

    data class Silent(val reason: SilenceReason) : EngineResult
}

/**
 * Why the engine said nothing. CLARITY_LOGIC_ENGINE.md 2.2.
 *
 * **Five, and there is no sixth and no null.** Recorded in simulator output and debug logs
 * and never shown to a person. The reason a system that can be silent needs to say why is
 * that silence and breakage look identical from the outside: a rule that never fires
 * because its criteria contradict each other produces exactly the same screen as a quiet
 * week, and only this enum tells them apart.
 */
enum class SilenceReason {

    /** Nothing qualified. The ordinary shape of a week with nothing worth saying about it. */
    NO_RULE_QUALIFIED,

    /** Something qualified and every one was filtered: horizon, yesterday's family, a cooldown. */
    ALL_QUALIFIED_RULES_FILTERED,

    /** There is not enough in the log to describe anything yet. */
    INSUFFICIENT_DATA,

    /** Everything ranked was realized and rejected by layer 5. */
    ALL_CANDIDATES_VETOED,

    /** 5.1. The engine could have spoken and chose not to. */
    DELIBERATE_SILENCE,
}

/**
 * What a surface renders. CLARITY_LOGIC_ENGINE.md 2.1.
 *
 * [meta] is the whole candidate rather than a summary, because the caller has to write a
 * `PULSE_GENERATED` or `REPORT_GENERATED` event carrying the family, the stage, the
 * register and the variant key, and `FiringHistory` is rebuilt from exactly those fields.
 * A surface that could not record what it showed would break the 90 day exclusion for
 * every device that later merged the log.
 */
data class RenderedOutput(
    val text: String,
    val question: String?,
    val responses: List<ResponseOption>,
    val meta: Candidate,
)

/**
 * Layer 5, as the engine sees it. CLARITY_LOGIC_ENGINE.md 8.
 *
 * The engine holds a validator it cannot inspect and cannot skip. Implemented by the
 * validator slice; declared here because the engine loop is what a veto changes, and
 * because a seam with no default is the only kind that cannot be forgotten.
 *
 * Returns **null when the candidate passed** and the reason when it did not. A reason is a
 * string because it is only ever read by the simulator and by a test, and because a typed
 * failure enumeration here would be a second copy of section 8's list of checks.
 */
fun interface CandidateValidator {

    /** Null when [candidate] may be shown, or the reason it may not. */
    fun veto(candidate: Candidate, facts: FactSet): String?

    companion object {

        /**
         * A validator that vetoes nothing.
         *
         * **For tests of layers 3 and 4 alone.** Wiring this into the app would be the
         * bypass 11.4 forbids, and the failure would be invisible: everything would still
         * render, and nothing would be checked.
         */
        val ACCEPT_NOTHING_CHECKED: CandidateValidator = CandidateValidator { _, _ -> null }
    }
}
