package com.kamsiob.claritynow.domain.engine.corpus

import com.kamsiob.claritynow.domain.engine.FactRef
import com.kamsiob.claritynow.domain.engine.FactSet
import com.kamsiob.claritynow.domain.engine.catalog.ClarityRule
import com.kamsiob.claritynow.domain.engine.catalog.SlotKey
import com.kamsiob.claritynow.domain.engine.catalog.Subject
import com.kamsiob.claritynow.domain.engine.catalog.Variant
import com.kamsiob.claritynow.domain.engine.realize.Candidate
import com.kamsiob.claritynow.domain.engine.realize.MeasureKind
import com.kamsiob.claritynow.domain.engine.realize.MeasureScope
import com.kamsiob.claritynow.domain.engine.realize.MeasureValue
import com.kamsiob.claritynow.domain.engine.realize.Measures
import com.kamsiob.claritynow.domain.engine.realize.Slot
import com.kamsiob.claritynow.domain.engine.realize.SlotBindings
import com.kamsiob.claritynow.domain.engine.realize.SlotRenderer
import java.time.ZoneId

/**
 * Filling one named line, rather than whichever line the realizer would have picked.
 *
 * ## Why this exists and is not a shortcut
 *
 * `Realizer.realize` answers a different question. It takes a selection, works out the
 * stage, the register and the bench, and hands back **one** sentence, choosing by the
 * deterministic hash in 7.6. There is no way to ask it "can line 47 of this bench be
 * filled from these facts", and that is the only question a corpus gate has: a line that
 * cannot be filled is dropped silently, costs nothing visible, and leaves a family looking
 * authored and going quiet.
 *
 * So the slot loop is written out here, over the **production** tables. The bindings come
 * from `SlotBindings`, the measures from `Measures`, the entity resolution from
 * `SlotBindings.resolveEntity` and the rendering from `SlotRenderer`. Nothing about which
 * fact fills which marker is restated; what is restated is the twenty line loop that walks
 * them.
 *
 * ## What stops that loop drifting from the realizer's
 *
 * `CorpusRenderGate` reproduces every sentence eleven persona years actually produced, and
 * compares it to what the simulator recorded, character for character. If this loop and
 * `Realizer.fill` ever disagree about anything, thousands of comparisons fail at once and
 * name the first line they disagreed on. A copy nobody checks is a copy that rots; this one
 * is checked against roughly thirteen thousand real renderings on every run.
 */
internal object CorpusFill {

    /** A filled line, with everything layer 5 reads. */
    data class Filled(
        val rendered: String,
        val slots: Map<SlotKey, Slot>,
        val refs: Map<SlotKey, FactRef>,
        val areas: Set<String>,
        val items: Set<String>,
        val quotedLabel: String?,
    )

    /** Why a line could not be filled, in the words an author would need. */
    data class Unfilled(val slot: SlotKey, val reason: String)

    /** The result of trying to fill one line. */
    sealed interface Attempt {
        data class Ok(val filled: Filled) : Attempt
        data class No(val why: Unfilled) : Attempt
    }

    /**
     * Fills every marker in [variant] from [facts], or says which marker stopped it.
     *
     * The four ordinary ways this answers no are the four `Realizer.fill` documents: a
     * marker with no binding, a binding whose entity is not in this fact set, a fact that
     * reads nothing, and a count of one in front of a plural the slot cannot govern.
     */
    fun fill(
        variant: Variant,
        facts: FactSet,
        subject: Subject?,
        callbackLabel: String?,
        zone: ZoneId,
    ): Attempt {
        if (SlotBindings.isExcluded(variant.key)) {
            return Attempt.No(Unfilled("", "the line is held out of its bench by SlotBindings.EXCLUDED"))
        }
        val bindings = SlotBindings.bindingsFor(variant.purpose, variant.family, variant.stage, variant.key)
        val slots = mutableMapOf<SlotKey, Slot>()
        val refs = mutableMapOf<SlotKey, FactRef>()
        val areas = mutableSetOf<String>()
        val items = mutableSetOf<String>()
        var quoted: String? = null

        for (key in variant.statement.slots) {
            val binding = bindings[key]
                ?: return Attempt.No(Unfilled(key, "no binding: SlotBindings has no entry for {$key} here"))
            val measure = Measures.byId(binding.measure)
                ?: return Attempt.No(Unfilled(key, "bound to `${binding.measure}`, which is not a measure"))
            val entity = SlotBindings.resolveEntity(binding, facts, subject, callbackLabel)
            if (measure.scope != MeasureScope.WINDOW && entity == null) {
                return Attempt.No(Unfilled(key, "the ${binding.entity} it reads is not in this fact set"))
            }
            val value = measure.read(facts, entity, zone)
                ?: return Attempt.No(Unfilled(key, "`${measure.id}` read nothing for $entity"))
            when (value) {
                is MeasureValue.Number -> {
                    slots[key] = numberSlot(key, measure.kind, measure.singular, measure.plural, value.value)
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
        val rendered = SlotRenderer.render(variant.statement.text, slots, variant.purpose)
            ?: return Attempt.No(
                Unfilled("", "a count of one in front of a plural the slot cannot make agree"),
            )
        return Attempt.Ok(Filled(rendered, slots, refs, areas, items, quoted))
    }

    /** The candidate layer 5 is handed, built from a fill and the rule that qualified. */
    fun candidateOf(variant: Variant, filled: Filled, rule: ClarityRule, subject: Subject?): Candidate = Candidate(
        ruleKey = rule.key,
        familyKey = variant.family,
        variantKey = variant.key,
        purpose = variant.purpose,
        stage = variant.stage,
        register = variant.register,
        lengthBand = variant.lengthBand,
        rendered = filled.rendered,
        renderedQuestion = null,
        slots = filled.slots,
        sourceFacts = filled.refs,
        namedAreaIds = filled.areas,
        namedItemIds = filled.items,
        subjectId = subject?.id,
        quotedLabel = filled.quotedLabel,
    )

    /**
     * The most recent stored answer a callback rule would quote.
     *
     * Step 2 of layer 3, without the horizon. The gate asks whether a sentence can be built
     * from facts a real life produced, not whether the selector would have offered it that
     * day, and the horizon is a rule about how old an answer may be rather than about
     * whether the sentence has a label to put in it. `PulseFacts.recentAnswers` is newest
     * first, so the first match is the one a person would remember giving.
     */
    fun callbackLabelFor(rule: ClarityRule, facts: FactSet, subject: Subject?): String? {
        val requirement = rule.requiresCallback ?: return null
        return facts.pulse.recentAnswers.firstOrNull { answer ->
            answer.family == requirement.family &&
                (requirement.responseKey == null || answer.responseKey == requirement.responseKey) &&
                (!requirement.subjectMustMatch || answer.subjectId == subject?.id)
        }?.responseLabel
    }

    /** True when this line quotes a label the gate cannot reproduce without the selector's own resolution. */
    fun quotesACallback(variant: Variant): Boolean =
        SlotBindings.bindingsFor(variant.purpose, variant.family, variant.stage, variant.key)
            .values
            .any { it.entity == SlotBindings.EntitySource.CALLBACK_LABEL }

    private fun numberSlot(key: SlotKey, kind: MeasureKind, singular: String, plural: String, value: Int): Slot =
        when (kind) {
            MeasureKind.PERCENT -> Slot.Percent(key, value)
            MeasureKind.DAYS -> Slot.Days(key, value)
            else -> Slot.Count(key, value, singular, plural)
        }

    /** The measures whose text is a stored `responseLabel`. `Realizer` names the same two. */
    private val QUOTED_LABEL_MEASURES = setOf("labelText", "mostGivenLabel")
}
