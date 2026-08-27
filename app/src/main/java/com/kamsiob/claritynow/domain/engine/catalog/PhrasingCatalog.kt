package com.kamsiob.claritynow.domain.engine.catalog

import com.kamsiob.claritynow.domain.engine.FamilyKey
import com.kamsiob.claritynow.domain.engine.VariantKey

/**
 * Layer 4's data. CLARITY_LOGIC_ENGINE.md 7.1.
 *
 * Everything here is parsed out of the three corpus files at catalog load. Nothing here
 * is authored in Kotlin, and that separation is the point: the file an author edits is
 * the file the app reads, so a corpus edit cannot silently disagree with a copy of the
 * corpus embedded in code.
 *
 * The one structural difference from the declaration in 7.1 is response pairs, and it is
 * recorded rather than quietly introduced. 7.1 declares `PhrasingFamily.responses` as a
 * flat `List<ResponseOption>`. `CORPUS_1_PULSE.md` authors responses as **pairs, per
 * stage**, six or seven of them per stage, each pair a set of two options that were
 * written to be read against each other and to pass the equal validity test as a pair.
 * Flattening them would lose the pairing, which is the only thing that makes the equal
 * validity test meaningful, so the pairs live on the stage. CLAUDE.md's authority order
 * gives the corpus the last word on the shape of a sentence, and a response pair is one.
 */
data class PhrasingFamily(
    val key: FamilyKey,
    val purpose: Purpose,
    val keyPrefix: String,
    val cooldownDays: Int,
    val stages: List<EscalationStage>,
) {
    init {
        require(stages.isNotEmpty()) { "family $key has no stages, so nothing could be realized for it" }
        require(cooldownDays >= 0) { "family $key declares a negative cooldown" }
        val indexes = stages.map { it.index }
        require(indexes == indexes.sorted()) { "family $key has stages out of order: $indexes" }
        require(indexes == (1..stages.size).toList()) {
            "family $key has stages $indexes. Stages are numbered from 1 with no gaps, " +
                "because a gap means a stage header was lost rather than retired"
        }
    }

    /** The stage with [index], or null. */
    fun stage(index: Int): EscalationStage? = stages.firstOrNull { it.index == index }

    /** Every variant in the family, across every stage. */
    val allVariants: List<Variant> get() = stages.flatMap { it.variants }
}

/**
 * One rung of a family's ladder. CLARITY_LOGIC_ENGINE.md 7.1 and 7.3.
 *
 * **Escalation is keyed to magnitude, not to firing count.** Nine days is stage 2 whether
 * the family fired once or fifty times. That is why the threshold is a range over the
 * family's escalation fact rather than a counter.
 *
 * [threshold] is parsed from the corpus stage header and never declared in Kotlin, per
 * 7.3: the corpus is the file an author edits, and where this document and the corpus
 * disagree on a number the corpus wins.
 */
data class EscalationStage(
    val index: Int,
    val threshold: ClosedRange<Int>,
    val variants: List<Variant>,
    /**
     * The header this stage's threshold was parsed from, kept verbatim so a test can
     * report which line of which corpus file it disagrees with.
     */
    val header: StageHeader,
    /** Pulse only. The question bench for this stage. */
    val questions: List<QuestionLine> = emptyList(),
    /** Report only. The extension bench for this stage. */
    val extensions: List<Variant> = emptyList(),
    /** Pulse only. The response pairs for this stage. See [PhrasingFamily]. */
    val responsePairs: List<ResponsePair> = emptyList(),
) {
    init {
        require(index >= 1) { "stage index $index" }
        require(variants.isNotEmpty()) { "stage $index has no statements or leads, so it can never speak" }
    }
}

/**
 * A stage header as the corpus wrote it, and the conditions parsed out of it.
 * CLARITY_LOGIC_ENGINE.md 7.3.
 *
 * **A compound header becomes two rules pointing at this one stage, never a disjunctive
 * range.** `Stage 3, ninety five percent and above, or four or more consecutive days`
 * carries two conditions over two different facts. A single `ClosedRange<Int>` cannot
 * express that without lying about one of them, so [conditions] holds both and the rule
 * catalog declares one rule per condition. The sentences are shared because the sentence
 * is the same either way; the reason for saying it is not.
 */
data class StageHeader(
    val sourceFile: String,
    val sourceLine: Int,
    val text: String,
    val index: Int,
    val conditions: List<StageCondition>,
) {
    init {
        require(conditions.isNotEmpty()) { "stage header at $sourceFile:$sourceLine parsed to no condition: $text" }
    }

    /** True when this header carries more than one condition and therefore needs more than one rule. */
    val isCompound: Boolean get() = conditions.size > 1

    /** The numeric conditions, which are the only ones a contiguity check can reason about. */
    val numericConditions: List<StageCondition.Numeric>
        get() = conditions.filterIsInstance<StageCondition.Numeric>()
}

/** One branch of a stage header. */
sealed interface StageCondition {

    /** The prose this branch was parsed from, for a failure message that points at the file. */
    val text: String

    /**
     * A range over the family's escalation fact, parsed from words rather than digits
     * because the corpus is written in prose. `six to thirteen days` is `6..13`; `thirty
     * days and beyond` is `30..Int.MAX_VALUE`.
     */
    data class Numeric(override val text: String, val range: IntRange) : StageCondition

    /**
     * A branch the header states qualitatively, such as `clear imbalance toward intake`
     * or `queues growing three weeks running`.
     *
     * These are parsed and recorded rather than guessed at. Inferring `5..MAX` from
     * `clear imbalance` would put a number in the engine that no author wrote and no
     * reviewer approved, and 7.3 says the corpus owns every threshold. A rule pointing
     * at a qualitative branch therefore has to carry its own criterion for the shape,
     * and [CatalogIntegrity] asserts it does.
     */
    data class Qualitative(override val text: String) : StageCondition
}

/**
 * One authored sentence. CLARITY_LOGIC_ENGINE.md 7.1.
 *
 * [lengthBand] is computed by [Template] at load and is never read from a corpus tag.
 * [shortMarker] records whether the author wrote `[S]` beside the line, purely so a test
 * can report where an advisory hint and the computed band disagree. It decides nothing.
 */
data class Variant(
    val key: VariantKey,
    val family: FamilyKey,
    val purpose: Purpose,
    val stage: Int,
    val register: Register,
    val statement: Template,
    val question: Template? = null,
    val shortMarker: Boolean = false,
    val sourceFile: String = "",
    val sourceLine: Int = 0,
) {
    /** Computed, per 7.5. */
    val lengthBand: LengthBand get() = statement.lengthBand

    /** Every slot the line cannot render without. Derived from the template, never declared. */
    val requiredSlots: Set<SlotKey> get() = statement.slots + (question?.slots ?: emptySet())

    /** Where to look when a test fails. */
    val origin: String get() = "$sourceFile:$sourceLine $key"
}

/** One Pulse question. Pulse only; the Report and Momentum never ask. */
data class QuestionLine(
    val key: VariantKey,
    val family: FamilyKey,
    val stage: Int,
    val text: Template,
    val sourceFile: String = "",
    val sourceLine: Int = 0,
)

/**
 * One tappable answer set. CLARITY_LOGIC_ENGINE.md 6.1 and 6.2.
 *
 * **Two options, always, except `quietDay`, which needs three.** The first option is the
 * positive one; for the three option pairs the first two are positive and the third is
 * the flagged one, per 6.1. That boolean is the only interpretation the app ever makes
 * of an answer.
 *
 * A universal third option was rejected. A third path already exists, which is not
 * answering at all, and it is a fully supported state with its own representation.
 */
data class ResponsePair(
    val key: String,
    val options: List<ResponseOption>,
    val sourceFile: String = "",
    val sourceLine: Int = 0,
) {
    init {
        require(options.size == 2 || options.size == 3) {
            "response pair $key has ${options.size} options. CLARITY_LOGIC_ENGINE.md 6.2 " +
                "settles the format at two, except quietDay at three"
        }
        require(options.first().isPositive) { "the first option of $key must be the positive one, per 6.1" }
        require(!options.last().isPositive) { "the last option of $key must be the flagged one, per 6.1" }
    }
}
