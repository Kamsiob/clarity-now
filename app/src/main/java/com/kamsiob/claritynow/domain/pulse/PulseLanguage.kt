package com.kamsiob.claritynow.domain.pulse

import com.kamsiob.claritynow.domain.engine.FiringHistory
import com.kamsiob.claritynow.domain.engine.catalog.ClarityCatalog
import com.kamsiob.claritynow.domain.engine.catalog.EscalationStage
import com.kamsiob.claritynow.domain.engine.catalog.Purpose
import com.kamsiob.claritynow.domain.engine.catalog.ResponseOption
import com.kamsiob.claritynow.domain.engine.realize.VariantChoice
import com.kamsiob.claritynow.domain.replay.PulseEntryState

/**
 * The two benches a stored Pulse entry needs and the event does not carry: the answers
 * that were offered with it, and the acknowledgment shown after one is tapped.
 *
 * ## Why this exists at all
 *
 * `PULSE_GENERATED` carries the observation and the question as rendered strings, so the
 * sheet can be redrawn from the log alone. It carries no response pair, because the
 * payload has no field for one. The sheet still needs two pills the next time the app is
 * opened, and the common case for that is the reminder at eight in the evening waking a
 * process that was killed hours ago.
 *
 * **This is not a second path to a sentence.** Nothing here composes, concatenates or
 * chooses between families. It resolves a bench that the engine already chose from, by
 * the key the engine recorded, out of the same catalog the engine holds, in the one
 * class that already holds it so that no composable ever opens a corpus. Where the key
 * is missing it re-runs `VariantChoice`, which is the corpus's own stated selection rule
 * and the same function the realizer called, with the same inputs.
 *
 * **The right fix is a field on the payload**, recorded on issue #19 while the schema
 * window is open, and then the fallback below can go.
 *
 * ## Why an empty firing history is the correct history here
 *
 * `VariantChoice` excludes a line used within ninety days, which it reads out of
 * `FiringHistory.variantsUsed`. That map is built from the keys the log records, and the
 * log records the statement variant only: no response pair key and no acknowledgment key
 * has ever been written to it by anything. So the exclusion set for both benches is empty
 * whatever the log holds, and passing [FiringHistory.EMPTY] states that rather than
 * pretending to consult a history that cannot contain them. A test asserts the two agree,
 * so the day somebody starts recording those keys, this stops compiling a lie.
 */
class PulseLanguage(val catalog: ClarityCatalog) {

    /**
     * The answers that were offered with [entry], or an empty list when the family or the
     * stage is no longer in the corpus.
     *
     * An empty list is a real possibility rather than an error: a corpus edit can retire a
     * stage, and an entry generated before it was retired keeps its rendered observation
     * forever. The sheet shows the observation with no pills in that case, which is the
     * unanswerable state and is exactly the state dismissing already produces.
     */
    fun responsesFor(entry: PulseEntryState): List<ResponseOption> {
        val stage = stageOf(entry) ?: return emptyList()
        val recorded = entry.factSnapshot[PulseGenerator.RESPONSE_PAIR_KEY]
        val pair = stage.responsePairs.firstOrNull { it.key == recorded }
            ?: VariantChoice.choose(stage.responsePairs, entry.dateKey, FiringHistory.EMPTY) { it.key }?.value
        return pair?.options.orEmpty()
    }

    /**
     * The line shown briefly after an answer, before the sheet settles to ambient.
     * `CORPUS_1_PULSE.md`, the acknowledgment bench.
     *
     * One bench shared across every family, because it responds to the act of answering
     * rather than to what was answered. The corpus states its own selection rule for it:
     * "the same deterministic hash used for variants, over `dateKey` plus `ack` key",
     * which is [VariantChoice] over the day's key.
     *
     * Null when the bench is absent, and the caller shows nothing rather than a stand in.
     * `PulseLanguageTest` fails if the real corpus stops producing it, so an absence here
     * is a corpus edit somebody made on purpose and not a heading that quietly moved.
     */
    fun acknowledgmentFor(dateKey: String): String? {
        val bench = catalog.auxiliary[ACKNOWLEDGMENTS].orEmpty()
        return VariantChoice.choose(bench, dateKey, FiringHistory.EMPTY) { it.key }?.value?.text
    }

    /** The stage [entry] was realized from, or null when the corpus no longer has it. */
    private fun stageOf(entry: PulseEntryState): EscalationStage? =
        catalog.familiesFor(Purpose.PULSE)
            .firstOrNull { it.key == entry.family }
            ?.stage(entry.stage)

    companion object {

        /**
         * The auxiliary bench the acknowledgment lines are parsed into.
         *
         * A literal because the walker that names it is internal to its own package and
         * its companion is private. The heading it comes from is `Acknowledgment lines`
         * and the keys are `ack.01` upward.
         */
        const val ACKNOWLEDGMENTS: String = "ack"
    }
}
