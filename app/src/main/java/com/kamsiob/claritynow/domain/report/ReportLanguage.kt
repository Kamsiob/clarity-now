package com.kamsiob.claritynow.domain.report

import com.kamsiob.claritynow.domain.engine.FactRef
import com.kamsiob.claritynow.domain.engine.FactSet
import com.kamsiob.claritynow.domain.engine.FiringHistory
import com.kamsiob.claritynow.domain.engine.catalog.ClarityCatalog
import com.kamsiob.claritynow.domain.engine.catalog.CorpusLine
import com.kamsiob.claritynow.domain.engine.catalog.Purpose
import com.kamsiob.claritynow.domain.engine.catalog.Register
import com.kamsiob.claritynow.domain.engine.catalog.ReportRules
import com.kamsiob.claritynow.domain.engine.catalog.SlotKey
import com.kamsiob.claritynow.domain.engine.catalog.Template
import com.kamsiob.claritynow.domain.engine.realize.Candidate
import com.kamsiob.claritynow.domain.engine.realize.MeasureValue
import com.kamsiob.claritynow.domain.engine.realize.Measures
import com.kamsiob.claritynow.domain.engine.realize.Slot
import com.kamsiob.claritynow.domain.engine.realize.SlotRenderer
import com.kamsiob.claritynow.domain.engine.realize.VariantChoice
import com.kamsiob.claritynow.domain.engine.validate.ClarityValidator
import com.kamsiob.claritynow.domain.engine.validate.ValidationResult
import java.time.ZoneId

/**
 * The four Report benches that are not families: the footer, the basis line and the two
 * edge states. `CORPUS_2_REPORT.md` 5 and 6.
 *
 * ## Why this exists and why it is not a second path
 *
 * `MASTER_BUILD_PROMPT.md` 11.1 has no exception for a footer or an empty state, so these
 * lines come out of the corpus like every other sentence. They cannot come out of the
 * engine loop, because that loop selects a rule, and sections 5 and 6 of the corpus are
 * benches with no families, no stages and no rules; `ReportWalker` parses them into the
 * catalog's auxiliary map and says in as many words that the phase which builds the Report
 * screen is the phase that gives them a caller.
 *
 * So this resolves a bench, exactly as `PulseLanguage` resolves the acknowledgment bench,
 * and it does three things that keep it honest:
 *
 * - It **chooses** with [VariantChoice], which is the corpus's own stated selection rule
 *   and the same function the realizer calls
 * - It **renders** with [SlotRenderer], which is the one function in this app that turns a
 *   number into text, so the basis line gets the Report's digits and the plural agreement
 *   rules without a second implementation of either
 * - It **validates** with [ClarityValidator], so a line from here has passed the same ten
 *   checks as a line from the engine. 11.4: never bypass the validator, not for a simple
 *   sentence, not for an empty state
 *
 * Nothing here composes, concatenates, or writes a word.
 *
 * ## The one gap, named rather than approximated
 *
 * 5.2's fuller basis lines read `Based on {n} Pulse responses, and {m} weeks of data.`
 * `{m}` is `HistoryFacts.weeksOfData`, which 3.1 declares and which **no entry in
 * `Measures` reads**, so those lines cannot be filled and drop out of the bench exactly as
 * an unfillable variant drops out of a family's bench. The report then states the basis it
 * can prove, `Based on 3 Pulse responses.`, which is true and shorter.
 *
 * The fix is one entry in `Measures`, in `domain.engine.realize`, which this slice does not
 * own. It is recorded here rather than worked around, because the alternative was to read
 * `facts.history.weeksOfData` directly and mint a `FactRef` for it that nothing could
 * re-read, and an unre-readable reference is the untraceable number section 8 check 3
 * exists to veto.
 */
class ReportLanguage(private val catalog: ClarityCatalog, private val zone: ZoneId) {

    private val validator = ClarityValidator(zone)

    /**
     * `CORPUS_2_REPORT.md` 5.1. Always present, never varied.
     *
     * The corpus states its own reason: it is a factual claim about where the report was
     * generated, and varying a factual claim weakens it. There is one line in the bench and
     * this returns it.
     */
    fun generatedLine(): ReportNote? =
        catalog.auxiliary[GENERATED_BENCH]?.firstOrNull()?.let { ReportNote(GENERATED_BENCH, it.text) }

    /** `CORPUS_2_REPORT.md` 6.1. Replaces the whole body when nothing happened at all. */
    fun nothingToReport(facts: FactSet, dateKey: String): ReportNote? =
        note(NOTHING_BENCH, facts, dateKey)

    /** `CORPUS_2_REPORT.md` 6.2. Shown only in the first week there has ever been. */
    fun firstWeek(facts: FactSet, dateKey: String): ReportNote? =
        note(FIRST_WEEK_BENCH, facts, dateKey)

    /**
     * `CORPUS_2_REPORT.md` 3.16. The pattern section's empty state, under three weeks.
     *
     * **No rule and no engine, on purpose, and it is an exception somebody decided.**
     * 3.16 is written as a pattern family and it is not one. Its four lines say there is
     * not enough history yet to see a shape: no subject, no escalation, no number, and
     * nothing a person could disagree with about their own week. It is the pattern
     * section's empty state, exactly as 6.1 is the whole report's, and 6.1 comes through
     * this class rather than through a rule for the same reason.
     *
     * As a rule it was also unreachable. `ReportComposer` asks the engine for a pattern
     * only when `weeksOfData >= 3` and the rule required `weeksOfData < 3`, so the two
     * conditions could never both hold. `ReportRules.RENDERED_DIRECTLY` carries the
     * decision and `ReportComposer` carries the note at the point the condition is read.
     *
     * **It is not a bypass of layer 5.** The line is chosen with [VariantChoice], rendered
     * with [SlotRenderer] and validated with [ClarityValidator], exactly like the three
     * benches above it. 11.4 says never bypass the validator, not for a simple sentence,
     * not for an empty state, and that still holds; what this skips is rule selection,
     * which is the part that decides whether something is worth saying about a person, and
     * there is nothing here to decide.
     *
     * Read from the family rather than from the auxiliary map because the corpus authors it
     * as a family and the parser is the corpus's reader, not this class's.
     */
    fun insufficientData(facts: FactSet, dateKey: String): ReportNote? {
        val family = catalog.familiesFor(Purpose.REPORT_PATTERN)
            .firstOrNull { it.key in ReportRules.RENDERED_DIRECTLY }
            ?: return null
        val chosen = VariantChoice.choose(family.allVariants, dateKey, FiringHistory.EMPTY) { it.key }?.value
            ?: return null
        // An empty state that needed a number would be stating a fact about the person, and
        // this is the one line in the report that states nothing about them. A slotted line
        // is dropped rather than filled, exactly as an unfillable variant leaves a bench.
        if (chosen.statement.slots.isNotEmpty()) return null
        val rendered = SlotRenderer.render(chosen.statement.text, emptyMap(), Purpose.REPORT_PATTERN)
            ?: return null
        val candidate = Candidate(
            ruleKey = "$RULE_KEY_PREFIX${family.key}",
            familyKey = family.key,
            variantKey = chosen.key,
            purpose = Purpose.REPORT_PATTERN,
            stage = chosen.stage,
            register = chosen.register,
            lengthBand = chosen.lengthBand,
            rendered = rendered,
            renderedQuestion = null,
            slots = emptyMap(),
            sourceFacts = emptyMap(),
            namedAreaIds = emptySet(),
            namedItemIds = emptySet(),
        )
        return when (validator.validate(candidate, facts)) {
            is ValidationResult.Passed -> ReportNote(candidate.variantKey, candidate.rendered)
            is ValidationResult.Vetoed -> null
        }
    }

    /**
     * The footer's basis line, or null when nothing about the basis can be stated.
     *
     * 5.2 states two rules and this honors both structurally rather than by reading the
     * keys. **Clauses are omitted when their value is zero**, which falls out of the slot
     * being unfillable: `Measures` answers null for nought, so a line whose count is zero
     * cannot render and the shorter line is taken instead. **The whole line is omitted when
     * everything is zero**, which is this returning null.
     *
     * ### Choosing between the singular and the plural line
     *
     * 5.2 writes the singular and plural forms as separate lines rather than as a runtime
     * substitution, "so no `1 responses` can ever occur", and nothing in the line's key says
     * which is which. The obvious answer is to write the mapping down here, `bs.01` is the
     * plural, `bs.02` is the singular, which is six entries of Kotlin restating what the
     * corpus already says and one more place for the two to disagree the day an author adds
     * a line.
     *
     * So the pairing is **derived from the bench**. Two lines that need the same slots and
     * differ by exactly one word carrying a trailing `s` are a singular and plural pair, and
     * the one whose form agrees with the count is kept. A line with no partner agrees
     * vacuously, which is what makes a bench of one line per shape work unchanged.
     */
    fun basis(facts: FactSet, dateKey: String): Candidate? {
        val bench = catalog.auxiliary[BASIS_BENCH].orEmpty()
        val filled = bench.mapNotNull { line -> fill(line, facts) }
        // In the first week there is no history to count, so the only line that can state
        // the whole basis is the one that counts nothing. Outside it, a line that states no
        // number states less than the report knows.
        val shaped = if (facts.history.isFirstWeekEver) {
            filled.filter { it.slots.isEmpty() }
        } else {
            filled.filter { it.slots.isNotEmpty() }
        }
        val agreeing = shaped.filter { agreesInNumber(it, shaped) }
        // The most informative line the facts can fill, then the lowest key, so two lines
        // of equal shape resolve the same way on both devices. `maxWith` with a descending
        // second term takes the smallest key, which is the first one an author wrote.
        val best = agreeing.maxWithOrNull(compareBy<Filled> { it.slots.size }.thenByDescending { it.line.key })
            ?: return null
        return candidateOf(best, BASIS_BENCH, facts)
    }

    /**
     * The three numbers the caption beneath the week ribbon states.
     * `design-v3.md` 11.1 item 4. See [ReportTotal] for which three and why.
     *
     * Read through [Measures], so each one carries the [FactRef] that re-reads it, and a
     * total of zero is absent rather than zero for the same reason a corpus slot never
     * renders one.
     */
    fun totals(facts: FactSet): List<ReportTotal> = CAPTION_MEASURES.mapNotNull { id ->
        val measure = Measures.byId(id) ?: return@mapNotNull null
        val value = measure.read(facts, null, zone) as? MeasureValue.Number ?: return@mapNotNull null
        ReportTotal(measure = id, ref = measure.refFor(null), value = value.value)
    }

    // ------------------------------------------------------------------ benches

    private fun note(bench: String, facts: FactSet, dateKey: String): ReportNote? {
        val lines = catalog.auxiliary[bench].orEmpty()
        val chosen = VariantChoice.choose(lines, dateKey, FiringHistory.EMPTY) { it.key }?.value ?: return null
        val filled = fill(chosen, facts) ?: return null
        val candidate = candidateOf(filled, bench, facts) ?: return null
        return ReportNote(candidate.variantKey, candidate.rendered)
    }

    /**
     * One bench line with every marker filled, or null when one of them cannot be.
     *
     * The template is built inside a `runCatching` because [Template] computes its length
     * band at construction and refuses a line longer than the corpus declares a band for.
     * That is a corpus defect rather than a runtime one and `CatalogIntegrity` is where it
     * should surface, so here it costs one line of a bench rather than the report screen.
     */
    private fun fill(line: CorpusLine, facts: FactSet): Filled? {
        val template = runCatching { Template(line.text) }.getOrNull() ?: return null
        val slots = mutableMapOf<SlotKey, Slot>()
        val refs = mutableMapOf<SlotKey, FactRef>()
        for (key in template.slots) {
            val measureId = BINDINGS[key] ?: return null
            val measure = Measures.byId(measureId) ?: return null
            val value = measure.read(facts, null, zone) as? MeasureValue.Number ?: return null
            slots[key] = Slot.Count(key, value.value, measure.singular, measure.plural)
            refs[key] = measure.refFor(null)
        }
        val rendered = SlotRenderer.render(template.text, slots, PURPOSE) ?: return null
        return Filled(line, template, slots, refs, rendered)
    }

    /**
     * Whether this line's noun agrees with its count, against the rest of the bench.
     *
     * See [basis]. A line agrees when it has no singular or plural partner, or when it is
     * the partner whose form matches the number being counted.
     */
    private fun agreesInNumber(line: Filled, bench: List<Filled>): Boolean {
        val counted = line.slots.values.filterIsInstance<Slot.Count>().firstOrNull() ?: return true
        val partner = bench.firstOrNull { it !== line && it.slots.keys == line.slots.keys && it.pairsWith(line) }
            ?: return true
        val thisIsPlural = line.template.text.length > partner.template.text.length
        return thisIsPlural == (counted.value != SINGULAR)
    }

    /** Builds the candidate that layer 5 checks, or null when layer 5 refuses it. */
    private fun candidateOf(filled: Filled, bench: String, facts: FactSet): Candidate? {
        val candidate = Candidate(
            ruleKey = "$RULE_KEY_PREFIX$bench",
            familyKey = bench,
            variantKey = filled.line.key,
            purpose = PURPOSE,
            stage = FIRST_STAGE,
            register = Register.PLAIN,
            lengthBand = filled.template.lengthBand,
            rendered = filled.rendered,
            renderedQuestion = null,
            slots = filled.slots,
            sourceFacts = filled.refs,
            namedAreaIds = emptySet(),
            namedItemIds = emptySet(),
        )
        return when (validator.validate(candidate, facts)) {
            is ValidationResult.Passed -> candidate
            is ValidationResult.Vetoed -> null
        }
    }

    private data class Filled(
        val line: CorpusLine,
        val template: Template,
        val slots: Map<SlotKey, Slot>,
        val refs: Map<SlotKey, FactRef>,
        val rendered: String,
    ) {

        /**
         * True when [other] is this line with one word's trailing `s` added or removed.
         *
         * The comparison drops trailing punctuation before it looks at the letters, because
         * the corpus writes `responses,` in one line and `response.` in another and the
         * difference between the pair is the letter rather than the mark. Without that, a
         * plural at the end of a sentence would never be recognized as one, which is where
         * every line in this bench happens to put it.
         */
        fun pairsWith(other: Filled): Boolean {
            val mine = template.text.split(' ')
            val theirs = other.template.text.split(' ')
            if (mine.size != theirs.size) return false
            val differing = mine.indices.filter { mine[it] != theirs[it] }
            if (differing.size != 1) return false
            val a = mine[differing.single()].trimEnd { !it.isLetter() }
            val b = theirs[differing.single()].trimEnd { !it.isLetter() }
            return a == "${b}s" || b == "${a}s"
        }
    }

    companion object {

        /** 5.1, parsed as a literal bench because the line carries no key. */
        const val GENERATED_BENCH: String = "footer.generated"

        /** 5.2. */
        const val BASIS_BENCH: String = "bs"

        /** 6.1. */
        const val NOTHING_BENCH: String = "ed.none"

        /** 6.2. */
        const val FIRST_WEEK_BENCH: String = "ed.first"

        /**
         * What fills the basis line's two markers.
         *
         * `SlotBindings` is the app's binding table and it is keyed by purpose, family,
         * stage and variant, which an auxiliary bench has none of. Two entries rather than a
         * second general table, and both name a measure rather than a field, so the number
         * they produce carries a re-readable reference like every other number in the app.
         *
         * **`m` named a measure that did not exist until the appeal pass.** `weeksOfData`
         * was bound here and declared nowhere, so `Measures.byId` returned null, the
         * `and {m} weeks of data` clause could never be filled, and three of the six basis
         * lines in `CORPUS_2_REPORT.md` 5.2 dropped out of the bench on every report ever
         * written. `Measures` declares it now.
         */
        val BINDINGS: Map<SlotKey, String> = mapOf(
            "n" to "answeredInWindow",
            "m" to "weeksOfData",
        )

        /** The three the ribbon caption states. See [ReportTotal]. */
        val CAPTION_MEASURES: List<String> = listOf("totalEvents", "completions", "additions")

        /** These are Report lines, so counts render as digits, per 7.2. */
        private val PURPOSE = Purpose.REPORT_OBSERVATION

        private const val RULE_KEY_PREFIX = "report.auxiliary."

        private const val FIRST_STAGE = 1

        private const val SINGULAR = 1
    }
}
