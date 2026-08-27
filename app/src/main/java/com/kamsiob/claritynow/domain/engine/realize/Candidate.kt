package com.kamsiob.claritynow.domain.engine.realize

import com.kamsiob.claritynow.domain.engine.AreaId
import com.kamsiob.claritynow.domain.engine.FactRef
import com.kamsiob.claritynow.domain.engine.FamilyKey
import com.kamsiob.claritynow.domain.engine.ItemId
import com.kamsiob.claritynow.domain.engine.VariantKey
import com.kamsiob.claritynow.domain.engine.catalog.LengthBand
import com.kamsiob.claritynow.domain.engine.catalog.Purpose
import com.kamsiob.claritynow.domain.engine.catalog.Register
import com.kamsiob.claritynow.domain.engine.catalog.ResponseOption
import com.kamsiob.claritynow.domain.engine.catalog.RuleKey
import com.kamsiob.claritynow.domain.engine.catalog.SlotKey

/**
 * A realized sentence, with everything the validator needs to disbelieve it.
 * CLARITY_LOGIC_ENGINE.md 8.
 *
 * **A candidate is not output.** It is a proposal, and layer 5 exists to reject it. Every
 * field below except [rendered] is here so that some check in section 8 can be written
 * without the validator having to recompute anything the realizer already knew: what was
 * named, what number came from where, and what was quoted.
 *
 * Four fields are not in 8's declaration and are recorded rather than smuggled in.
 * [purpose] and [subjectId] are what `FiringHistory` is keyed by, so the caller that
 * writes the `PULSE_GENERATED` event has them without re-deriving them from the rule.
 * [responses] carries the tappable answers, which 2.1 puts on `RenderedOutput` and which
 * have to survive the trip from the stage that owns them. [quotedLabel] is the stored
 * `responseLabel` a callback line quoted, which check 6 compares against and which cannot
 * be recovered from the rendered string once it has been placed in a sentence.
 */
data class Candidate(
    val ruleKey: RuleKey,
    val familyKey: FamilyKey,
    val variantKey: VariantKey,
    val purpose: Purpose,
    val stage: Int,
    val register: Register,
    val lengthBand: LengthBand,
    val rendered: String,
    val renderedQuestion: String?,
    val slots: Map<SlotKey, Slot>,
    /**
     * The address of the fact behind every numeric slot. Check 3 re-reads each one and
     * compares. A `Count`, `Percent` or `Days` slot with no entry here is a veto, not an
     * oversight to be forgiven.
     */
    val sourceFacts: Map<SlotKey, FactRef>,
    val namedAreaIds: Set<AreaId>,
    val namedItemIds: Set<ItemId>,
    val subjectId: String? = null,
    val responses: List<ResponseOption> = emptyList(),
    val quotedLabel: String? = null,
)
