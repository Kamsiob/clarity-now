package com.kamsiob.claritynow.domain.engine.realize

import com.kamsiob.claritynow.domain.engine.FactRef
import com.kamsiob.claritynow.domain.engine.FactSet
import com.kamsiob.claritynow.domain.engine.FiringHistory
import com.kamsiob.claritynow.domain.engine.PartOfDay
import com.kamsiob.claritynow.domain.engine.catalog.ClarityCatalog
import com.kamsiob.claritynow.domain.engine.catalog.EscalationStage
import com.kamsiob.claritynow.domain.engine.catalog.LengthBand
import com.kamsiob.claritynow.domain.engine.catalog.PhrasingFamily
import com.kamsiob.claritynow.domain.engine.catalog.Purpose
import com.kamsiob.claritynow.domain.engine.catalog.Register
import com.kamsiob.claritynow.domain.engine.catalog.SlotKey
import com.kamsiob.claritynow.domain.engine.catalog.Variant
import com.kamsiob.claritynow.domain.engine.select.Selection
import java.time.ZoneId

/**
 * Layer 4. A selection becomes a sentence. CLARITY_LOGIC_ENGINE.md 7.
 *
 * ## What this can see
 *
 * A [FactSet] and a corpus. **No live entity table, and there is no parameter through
 * which one could be passed.** That is validator check 5 in section 8, enforced by the
 * shape of this class rather than by a check: every name it can reach is a snapshot field
 * that layer one resolved by folding the log to the window, so a sentence realized today
 * about a week in March still says what the area was called in March. The `Stale name`
 * failure mode in section 13 is closed by there being nowhere for a live name to come from.
 *
 * ## What it does, in order
 *
 * The escalation stage, then the register, then the bench, then the line. 7.3 for the
 * ladder, 7.4 for the voice, 7.6 for the line. Every step can fail, and failure is
 * ordinary: the caller takes the next ranked selection, which is what 8 prescribes for a
 * vetoed candidate and what 7.2 prescribes for a missing slot.
 *
 * ## The one thing it refuses to do
 *
 * It never renders a template it cannot fill completely, and it never fills a slot from
 * anything but the table in [SlotBindings]. A marker with no binding takes its line out of
 * the bench, which costs one line out of fifteen. The alternative, a default that guesses
 * what a number means, costs the credibility of everything the app says, per 1.1.
 */
class Realizer(private val catalog: ClarityCatalog, private val zone: ZoneId) {

    /**
     * Realizes [selection], or explains why it cannot be.
     *
     * [options] carries the two composition constraints that belong to the Report and that
     * the composer holds rather than the engine: the band of the previous lead, per 7.5,
     * and whether the editorial budget in 7.4 is spent.
     */
    fun realize(
        selection: Selection,
        facts: FactSet,
        history: FiringHistory,
        moment: EngineMoment,
        options: RealizationOptions = RealizationOptions(),
    ): Realization {
        val rule = selection.rule
        val purpose = selection.purpose
        val family = catalog.familyFor(rule)
            ?: return Realization.NotProducible("rule ${rule.key} points at no family for $purpose")
        val stageIndex = rule.stage ?: FIRST_STAGE
        laddersBack(family, selection, history, moment, stageIndex)?.let { return it }
        val stage = family.stage(stageIndex)
            ?: return Realization.NotProducible("${family.key} has no stage $stageIndex")

        val registers = RegisterChoice.preference(
            purpose = purpose,
            unflattering = rule.unflattering,
            partOfDay = moment.partOfDay,
            notable = rule.specificity >= RegisterChoice.NOTABLE_SPECIFICITY,
            editorialBudgetSpent = options.editorialBudgetSpent,
        )

        for (register in registers) {
            val bench = producible(stage, register, selection, facts, purpose)
            if (bench.isEmpty()) continue
            val choice = choose(bench, options.avoidBand, moment.dateKey, history) ?: continue
            return rendered(choice, selection, facts, history, moment, family, stage, register)
        }
        return Realization.NotProducible(
            "no line in ${family.key} stage $stageIndex can be filled from the facts on hand",
        )
    }

    // --------------------------------------------------------------- the ladder

    /**
     * 7.3's monotonicity rule, and the only way it can be honored without inventing a
     * number.
     *
     * The rule is that a `(family, subject)` pair never shows a lower stage than it last
     * showed while the condition stayed continuously true. The obvious implementation is to
     * raise the stage to the one last shown, and it is wrong: stage 2 of `persistence` is
     * authored around six to thirteen days, so rendering it for an item whose age has just
     * been reset to three would say `going into its second week` about a three day old
     * item. A false sentence is worse than a missing one, so the pair is dropped instead
     * and the family says nothing until the magnitude catches up.
     *
     * **Continuity is bounded, or the ladder would never reset.** 7.3 says the ladder
     * resets when the condition genuinely lapses, and nothing in 3.1 records when that
     * happened. The bound used here is the family's own cooldown plus the window it
     * describes: inside that, the family was either speaking or forbidden from speaking, so
     * the condition plausibly held. Beyond it, the engine has no evidence either way and
     * the ladder starts again.
     */
    private fun laddersBack(
        family: PhrasingFamily,
        selection: Selection,
        history: FiringHistory,
        moment: EngineMoment,
        stageIndex: Int,
    ): Realization? {
        val lastStage = history.lastStage(family.key, selection.subjectId) ?: return null
        if (lastStage <= stageIndex) return null
        val since = history.daysSinceFiring(family.key, selection.subjectId, moment.dateKey) ?: return null
        // Long, because `selfReportVsData` declares a cooldown of Int.MAX_VALUE and the
        // sum would otherwise overflow into a negative continuity window.
        val continuity = family.cooldownDays.toLong() + selection.windowDays
        if (since.toLong() !in 0..continuity) return null
        return Realization.NotProducible(
            "${family.key} last spoke at stage $lastStage about the same subject $since days ago, " +
                "and this would be stage $stageIndex",
        )
    }

    // --------------------------------------------------------------- the bench

    /** Every line at this stage and register that can be filled, already rendered. */
    private fun producible(
        stage: EscalationStage,
        register: Register,
        selection: Selection,
        facts: FactSet,
        purpose: Purpose,
    ): List<Producible> = stage.variants
        .asSequence()
        .filter { it.register == register }
        .filterNot { SlotBindings.isExcluded(it.key) }
        .mapNotNull { variant -> fill(variant, selection, facts, purpose)?.let { Producible(variant, it) } }
        .toList()

    /**
     * Takes the head of the bench, preferring a length band the previous lead did not use.
     *
     * 7.5 forbids two consecutive Report leads from sharing a band, and this honors it as a
     * preference rather than as a constraint. A bench with nothing outside the band still
     * speaks, because 11.4 is explicit that a section is never padded and, read the other
     * way, never dropped for rhythm either. Rhythm is worth a line, not a paragraph.
     */
    private fun choose(
        bench: List<Producible>,
        avoidBand: LengthBand?,
        dateKey: String,
        history: FiringHistory,
    ): VariantChoice.Choice<Producible>? {
        val preferred = if (avoidBand == null) bench else bench.filter { it.variant.lengthBand != avoidBand }
        val pool = preferred.ifEmpty { bench }
        return VariantChoice.choose(pool, dateKey, history) { it.variant.key }
    }

    // --------------------------------------------------------------- the sentence

    private fun rendered(
        choice: VariantChoice.Choice<Producible>,
        selection: Selection,
        facts: FactSet,
        history: FiringHistory,
        moment: EngineMoment,
        family: PhrasingFamily,
        stage: EscalationStage,
        register: Register,
    ): Realization {
        val variant = choice.value.variant
        val filled = choice.value.filled
        val question = questionFor(stage, filled, moment, history, selection.purpose)
        val responses = responsesFor(stage, moment, history)
        return Realization.Rendered(
            candidate = Candidate(
                ruleKey = selection.rule.key,
                familyKey = family.key,
                variantKey = variant.key,
                purpose = selection.purpose,
                stage = stage.index,
                register = register,
                lengthBand = variant.lengthBand,
                rendered = filled.rendered,
                renderedQuestion = question,
                slots = filled.slots,
                sourceFacts = filled.refs,
                namedAreaIds = filled.areas,
                namedItemIds = filled.items,
                subjectId = selection.subjectId,
                responses = responses,
                quotedLabel = filled.quotedLabel,
            ),
            benchExhausted = choice.benchExhausted,
        )
    }

    /**
     * The question that follows a Pulse statement, chosen from the same stage.
     *
     * A statement combines only with a question from its own family and its own stage,
     * which `CORPUS_1_PULSE.md` states as its combination rule and 7.7 states as the rule
     * that stops the whole system reading as assembled. There is no global question pool
     * and there is nowhere for one to come from.
     */
    private fun questionFor(
        stage: EscalationStage,
        filled: Filled,
        moment: EngineMoment,
        history: FiringHistory,
        purpose: Purpose,
    ): String? {
        if (purpose != Purpose.PULSE) return null
        val renderable = stage.questions.mapNotNull { line ->
            val text = SlotRenderer.render(line.text.text, filled.slots, purpose) ?: return@mapNotNull null
            line.key to text
        }
        if (renderable.isEmpty()) return null
        return VariantChoice.choose(renderable, moment.dateKey, history) { it.first }?.value?.second
    }

    /** The tappable answers, chosen the same way and never split across stages. */
    private fun responsesFor(stage: EscalationStage, moment: EngineMoment, history: FiringHistory) =
        VariantChoice.choose(stage.responsePairs, moment.dateKey, history) { it.key }?.value?.options.orEmpty()

    // --------------------------------------------------------------- the slots

    /**
     * Fills every marker in a line, or answers null.
     *
     * Null has four causes and all four are ordinary: a marker with no binding, a binding
     * whose entity does not exist in this fact set, a fact that is zero or absent, and a
     * count of one in front of a plural noun the slot cannot make agree. Each of them takes
     * one line out of a bench and none of them is an error.
     */
    private fun fill(variant: Variant, selection: Selection, facts: FactSet, purpose: Purpose): Filled? {
        val bindings = SlotBindings.bindingsFor(purpose, variant.family, variant.stage, variant.key)
        val slots = mutableMapOf<SlotKey, Slot>()
        val refs = mutableMapOf<SlotKey, FactRef>()
        val areas = mutableSetOf<String>()
        val items = mutableSetOf<String>()
        var quoted: String? = null

        for (key in variant.statement.slots) {
            val binding = bindings[key] ?: return null
            val measure = Measures.byId(binding.measure) ?: return null
            val entity = SlotBindings.resolveEntity(binding, facts, selection.subject, selection.callbackLabel)
            if (measure.scope != MeasureScope.WINDOW && entity == null) return null
            val value = measure.read(facts, entity, zone) ?: return null
            when (value) {
                is MeasureValue.Number -> {
                    slots[key] = numberSlot(key, measure, value.value)
                    refs[key] = measure.refFor(entity)
                }
                is MeasureValue.Text -> {
                    slots[key] = Slot.Text(key, value.value)
                    value.namedArea?.let { areas += it }
                    value.namedItem?.let { items += it }
                    if (measure.id in QUOTED_LABEL_MEASURES) quoted = value.value
                }
                is MeasureValue.Date -> slots[key] = Slot.DateRef(key, value.weekKey, value.display)
            }
        }
        val rendered = SlotRenderer.render(variant.statement.text, slots, purpose) ?: return null
        return Filled(slots, refs, areas, items, rendered, quoted)
    }

    private fun numberSlot(key: SlotKey, measure: Measure, value: Int): Slot = when (measure.kind) {
        MeasureKind.PERCENT -> Slot.Percent(key, value)
        MeasureKind.DAYS -> Slot.Days(key, value)
        else -> Slot.Count(key, value, measure.singular, measure.plural)
    }

    private data class Producible(val variant: Variant, val filled: Filled)

    private data class Filled(
        val slots: Map<SlotKey, Slot>,
        val refs: Map<SlotKey, FactRef>,
        val areas: Set<String>,
        val items: Set<String>,
        val rendered: String,
        val quotedLabel: String?,
    )

    private companion object {

        /** A family with no ladder is one stage, numbered from one like every other. */
        const val FIRST_STAGE = 1

        /**
         * The measures whose text is a stored `responseLabel`.
         *
         * Recorded on the candidate so validator check 6 can compare what was quoted
         * against what was stored, without having to find the quote inside a sentence.
         */
        val QUOTED_LABEL_MEASURES = setOf("labelText", "mostGivenLabel")
    }
}

/** What layer 4 produced, or why it produced nothing. */
sealed interface Realization {

    /** A sentence, and everything layer 5 needs to reject it. */
    data class Rendered(val candidate: Candidate, val benchExhausted: Boolean) : Realization

    /**
     * No sentence, with the reason in plain English for the simulator.
     *
     * The caller takes the next ranked selection. This is not an error and never reaches a
     * person; it is the ordinary shape of a bench that has nothing to say today.
     */
    data class NotProducible(val reason: String) : Realization
}

/** The two composition constraints the Report holds and hands down. 7.4 and 7.5. */
data class RealizationOptions(
    val avoidBand: LengthBand? = null,
    val editorialBudgetSpent: Boolean = false,
)

/**
 * When the engine is speaking, in the terms every layer below needs.
 *
 * [dateKey] drives the variant hash in 7.6, the deliberate silence in 5.1 and every
 * exclusion window in `FiringHistory`. [partOfDay] drives the register preference in 7.4.
 * Neither can be read from a clock, so both are derived from the window the facts describe
 * and the zone the engine was built with. See `ClarityEngine` for the derivation and why
 * the zone is a constructor parameter rather than an ambient default.
 */
data class EngineMoment(val dateKey: String, val partOfDay: PartOfDay)
