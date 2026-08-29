package com.kamsiob.claritynow.domain.guidance

import com.kamsiob.claritynow.domain.engine.FactRef
import com.kamsiob.claritynow.domain.engine.FamilyKey
import com.kamsiob.claritynow.domain.engine.RenderedOutput

/** The frame bench key a plan was built from. `CORPUS_2_REPORT.md` 4.1. */
typealias FrameKey = String

/** The cue bench key a plan was built from. `CORPUS_2_REPORT.md` 4.2. */
typealias CueKey = String

/** The action bench key a plan was built from. `CORPUS_2_REPORT.md` 4.3. */
typealias ActionKey = String

/**
 * What layer 6 produced. CLARITY_LOGIC_ENGINE.md 10.3.
 *
 * Three outcomes and two of them are silence of a kind. 10.7 requires `Nothing` or a
 * non plan closing on at least fifteen percent of reports across the persona set,
 * because a report that always has advice is a report inventing problems.
 */
sealed interface GuidanceResult {

    /** A plan to offer, with both of its forms already rendered and validated. */
    data class Plan(val plan: ClarityPlan) : GuidanceResult

    /** `CORPUS_2_REPORT.md` 4.6. A complete closing line with no plan in it and no pill. */
    data class Closing(val line: RenderedOutput) : GuidanceResult

    /** No closing line at all. The report ends with its last observation. */
    data object Nothing : GuidanceResult
}

/**
 * One composed plan, in both of its grammatical forms. CLARITY_LOGIC_ENGINE.md 10.3.
 *
 * ## Four fields of 10.3's declaration are deliberately absent
 *
 * 10.3 declares `acceptedAt`, `declinedAt`, `resolvedAt` and `resolvedValue` on this
 * type. None of the four is here, and the omissions are the safeguard in section 19
 * made structural rather than remembered.
 *
 * - **`acceptedAt`.** A composed plan has not been accepted. Acceptance is a
 *   `PLAN_ACCEPTED` event and it lives in the log and in `PlanState`, which is the
 *   projection of that log. A nullable field here would be a second place to hold it,
 *   and two places holding one fact is how two devices come to disagree
 * - **`declinedAt`.** 10.5 is unambiguous: declining writes nothing, costs nothing, is
 *   never counted and is never referenced, and ignoring both options is identical to
 *   declining. There is no `PLAN_DECLINED` event in the catalog and there must never
 *   be one, so this field could only ever be null. **A field that can only be null is
 *   a field somebody will one day try to fill**
 * - **`resolvedAt` and `resolvedValue`.** There is no resolution event either, so
 *   neither could be derived from the log, and 10.6 forbids the sentence they would
 *   enable. Whether an accepted plan's situation is still standing is asked and
 *   answered at composition time, from this week's facts, by
 *   [PlanHistory.stillUnresolved]. It is a boolean that lives for one call. **Stored,
 *   it would be a record of whether a person kept a promise, which is the one record
 *   this app must not hold**
 *
 * ## What is here instead
 *
 * [offeredLine] is nominal and explicitly optional. [committedLine] is first person
 * and is rendered only on acceptance. Both are composed from the corpus benches in
 * `CORPUS_2_REPORT.md` 4 and both have been through layer 5. The imperative form of
 * either exists nowhere in this app, and `PlanFormTest` asserts it over the benches
 * themselves rather than over the sentences they happen to have produced.
 */
data class ClarityPlan(
    val id: String,
    val weekStartKey: String,
    val frameKey: FrameKey,
    val cueKey: CueKey,
    val actionKey: ActionKey,
    /** The observation family that motivated this plan. It appeared in the report. */
    val familyKey: FamilyKey,
    /** The area or item the motivating observation was about, or null. */
    val subjectId: String?,
    /** Nominal, never imperative. Shown in the report. */
    val offeredLine: String,
    /** First person, a proper if then. Rendered only on accept. */
    val committedLine: String,
    /** The fact whose movement the plan is about. Recorded on the event, never compared. */
    val resolutionFactRef: FactRef,
)
