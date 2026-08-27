package com.kamsiob.claritynow.data.event

import com.kamsiob.claritynow.domain.engine.FactRef
import kotlinx.serialization.Serializable

/**
 * The event payloads, MASTER_BUILD_PROMPT 5.2.
 *
 * Every payload carries full before and after values, so a replay reconstructs state
 * without reading any other table. This is the trap in event sourcing that is easy
 * to miss: a log that reads nicely is not the same as a log that replays correctly.
 * `ITEM_PROMOTED` carrying `demotedItemId` is the clearest example, and a descriptive
 * log would omit it.
 *
 * Display snapshots are a separate question, and they are not universal. Eight of
 * these twenty eight carry enough of one to name both the subject and the area of a
 * Trail row with no lookup at all: the five area events that carry a name, and
 * [ItemAdded], [ItemPromoted] and [ItemCompleted], which carry a title and an area
 * name together. The other twenty carry a partial snapshot or none, and an entry
 * from a year ago resolves what it is missing by folding the log to the instant of
 * the event, which is what `domain.query.TrailQueries` does. Neither path reads a
 * live entity table, so renaming an area never rewrites an older Trail entry.
 *
 * Two qualifications on that eight, both from Addendum 01. [ItemAdded] names an
 * area only when the item was filed at capture; an unfiled one names a title and
 * nothing else, because there is no area to name. And [ItemFiled] is the mirror
 * image: it carries the destination area's name and folds for the title.
 */
sealed interface EventPayload {
    /** The entity this event is primarily about. Indexed on the row. */
    val primaryEntityId: String
}

// Areas ----------------------------------------------------------------------

@Serializable
data class AreaCreated(
    val areaId: String,
    val name: String,
    val colorHex: String,
    val orderKey: String,
) : EventPayload {
    override val primaryEntityId get() = areaId
}

@Serializable
data class AreaRenamed(
    val areaId: String,
    val previousName: String,
    val newName: String,
) : EventPayload {
    override val primaryEntityId get() = areaId
}

@Serializable
data class AreaRecolored(
    val areaId: String,
    val previousHex: String,
    val newHex: String,
) : EventPayload {
    override val primaryEntityId get() = areaId
}

@Serializable
data class AreaReordered(
    val areaId: String,
    val previousOrderKey: String,
    val newOrderKey: String,
) : EventPayload {
    override val primaryEntityId get() = areaId
}

@Serializable
data class AreaArchived(val areaId: String, val nameSnapshot: String) : EventPayload {
    override val primaryEntityId get() = areaId
}

@Serializable
data class AreaUnarchived(val areaId: String, val nameSnapshot: String) : EventPayload {
    override val primaryEntityId get() = areaId
}

/** A tombstone. The row is never removed, so Trail entries keep their subject. */
@Serializable
data class AreaDeleted(val areaId: String, val nameSnapshot: String) : EventPayload {
    override val primaryEntityId get() = areaId
}

// Items ----------------------------------------------------------------------

/**
 * Capture. The one event that is allowed to name no area at all.
 *
 * [areaId] is nullable because Addendum 01 4a takes the decision out of capture:
 * adding an item must never require choosing where it goes. A null area is an
 * unfiled item in the inbox. It is real, queryable, editable and deletable, and it
 * sits outside every area scoped invariant in MASTER_BUILD_PROMPT 6.2, outside
 * `queueSizeAt`, and outside every engine fact. It cannot be ACTIVE or COMPLETED
 * until an [ItemFiled] gives it an area, which is the only transition into one.
 *
 * **[areaNameSnapshot] is nullable with it, and the two are a pair: both set, or
 * neither.** DECISIONS.md C8 rejected a synthetic inbox area on the ground that a
 * placeholder area name eventually gets printed and CLARITY_LOGIC_ENGINE.md 1 is
 * blunt about what one fabricated area name costs. An empty string sitting in this
 * field would be that same placeholder wearing a different coat, so the type says
 * absent instead. Nothing enforces the pairing at construction, because a payload
 * arriving from a foreign log must never throw; the reducer reads [areaId] and
 * ignores the snapshot when it is null.
 *
 * [estimateMinutes] is Addendum 01 4c and [firstStep] is 4b. Both are optional
 * forever, never required and never prompted for, and both are here rather than in
 * the phase that builds their fields because an event payload is nearly free to
 * change before user data exists and expensive afterward. An estimate can be
 * changed later through [ItemEstimated], which is what keeps history unedited.
 */
@Serializable
data class ItemAdded(
    val itemId: String,
    val areaId: String?,
    val title: String,
    val note: String?,
    val orderKey: String,
    val areaNameSnapshot: String?,
    val estimateMinutes: Int? = null,
    val firstStep: String? = null,
) : EventPayload {
    override val primaryEntityId get() = itemId
}

/**
 * Filing an unfiled item into an area. Addendum 01 2c. The only way into one.
 *
 * Sets the item's area and its order key and leaves it QUEUED. **It never
 * promotes.** The item joins the queue like anything else, because filing is a
 * bookkeeping act and promotion is a choice about what to do next, and folding them
 * together would turn the inbox into a way to displace whatever the person is
 * currently working on without meaning to.
 *
 * [areaNameSnapshot] is carried so a Trail row can name the destination with no
 * lookup, per MASTER_BUILD_PROMPT 5.2. The item's title is deliberately not carried
 * and is resolved by folding, which is what every type without a full snapshot
 * does; carrying it would be a second copy of a value that already has one home.
 *
 * There is no unfile event and no `previousAreaId`. Filing is one way and the
 * before value is known without storing it, because it is always null.
 */
@Serializable
data class ItemFiled(
    val itemId: String,
    val areaId: String,
    val orderKey: String,
    val areaNameSnapshot: String,
) : EventPayload {
    override val primaryEntityId get() = itemId
}

@Serializable
data class ItemEdited(
    val itemId: String,
    val previousTitle: String,
    val newTitle: String,
    val previousNote: String?,
    val newNote: String?,
    /**
     * The first step, Addendum 01 4b, which is deletable and therefore has to be
     * changeable after capture.
     *
     * It rides on this event rather than getting one of its own, because it is a
     * property of the item exactly as the title and the note are, and an edit is
     * where a person changes it. The estimate did get its own event, `ITEM_ESTIMATED`,
     * because Addendum 01 2b asks for one by name so that setting an estimate does not
     * have to look like editing the item.
     *
     * Both default to null so that a caller which does not yet know about the field
     * cannot silently clear it: the repository passes the item's current value through
     * when the editor has no first step field, which it does not until phase 3b.
     */
    val previousFirstStep: String? = null,
    val newFirstStep: String? = null,
) : EventPayload {
    override val primaryEntityId get() = itemId
}

/**
 * An estimate set, changed or cleared after capture. Addendum 01 2b.
 *
 * A separate event rather than a field on [ItemEdited] so that revising a guess
 * never rewrites what the person originally wrote down, and so the sequence of
 * guesses stays readable in the log. Both values are nullable in both directions:
 * null to a number sets one, a number to null clears one, and a number to a
 * different number revises one. [previousEstimateMinutes] is the before value the
 * log convention requires and the reducer does not consult it, exactly as it does
 * not consult [ItemEdited.previousTitle].
 *
 * **Addendum 01 7a governs what may ever be said about these numbers, and it is a
 * correctness rule rather than a matter of tone: no rendered sentence may state a
 * delta between an estimate and an actual.** Ratios and tendencies only, and not
 * until at least five completed items carry one. Nothing in this package can
 * enforce that, which is exactly why it is written here, where the number enters
 * the log, rather than only in the phase that reads it back.
 *
 * **A gap in this catalog, recorded rather than left to be discovered.** Nothing
 * here can change [ItemAdded.firstStep] after capture. [ItemEdited] carries the
 * title and the note and nothing else, and there is no equivalent of this type for
 * the first step, yet Addendum 01 4b requires that the field be deletable. Closing
 * that needs either two more fields on [ItemEdited] or one more type, and both are
 * payload changes that are cheap only while the schema window is open.
 */
@Serializable
data class ItemEstimated(
    val itemId: String,
    val previousEstimateMinutes: Int?,
    val newEstimateMinutes: Int?,
) : EventPayload {
    override val primaryEntityId get() = itemId
}

@Serializable
data class ItemQueued(
    val itemId: String,
    val areaId: String,
    val orderKey: String,
    val previousStatus: ItemStatus,
) : EventPayload {
    override val primaryEntityId get() = itemId
}

/**
 * Promotion, and the swap that shares its shape.
 *
 * [demotedItemId] is what makes a swap replay correctly. Without it a second device
 * replaying this event would leave two active items in one area and have no record
 * of which one moved, so the invariant would be repaired arbitrarily.
 */
@Serializable
data class ItemPromoted(
    val itemId: String,
    val areaId: String,
    val previousStatus: ItemStatus,
    val demotedItemId: String?,
    val demotedToOrderKey: String?,
    val titleSnapshot: String,
    val areaNameSnapshot: String,
) : EventPayload {
    override val primaryEntityId get() = itemId
}

@Serializable
data class ItemCompleted(
    val itemId: String,
    val areaId: String,
    val titleSnapshot: String,
    val areaNameSnapshot: String,
    val activeDurationDays: Int,
) : EventPayload {
    override val primaryEntityId get() = itemId
}

@Serializable
data class ItemReopened(
    val itemId: String,
    val areaId: String,
    val targetOrderKey: String,
) : EventPayload {
    override val primaryEntityId get() = itemId
}

@Serializable
data class ItemReordered(
    val itemId: String,
    val areaId: String,
    val previousOrderKey: String,
    val newOrderKey: String,
) : EventPayload {
    override val primaryEntityId get() = itemId
}

/**
 * A tombstone.
 *
 * [areaId] is null for an item deleted straight out of the inbox. Deleting is the one
 * operation an inbox must always support, and requiring an area here would have meant
 * a person could put something in the inbox and only get it out again by filing it
 * first. Addendum 01 4a and MASTER_BUILD_PROMPT 14b.1 both say an unfiled item can be
 * deleted, with the same undo window as anywhere else.
 */
@Serializable
data class ItemDeleted(
    val itemId: String,
    val areaId: String?,
    val titleSnapshot: String,
) : EventPayload {
    override val primaryEntityId get() = itemId
}

// Focus ----------------------------------------------------------------------

@Serializable
data class FocusStarted(
    val sessionId: String,
    val areaId: String,
    val itemId: String,
    val plannedSeconds: Int,
) : EventPayload {
    override val primaryEntityId get() = sessionId
}

@Serializable
data class FocusCompleted(val sessionId: String, val actualSeconds: Int) : EventPayload {
    override val primaryEntityId get() = sessionId
}

/**
 * A session the person stopped before the planned time. Neutral everywhere.
 *
 * **Renamed from `FOCUS_ABANDONED` in the Addendum 01 schema commit.** No user
 * visible string ever contained the word, because the Trail already reads "Stopped
 * after N minutes", so nothing on screen changed. The exposure was the type name
 * itself, in two places. An export written with no password is readable by whoever
 * made it, and Addendum 01 4h requires the export screen to say so plainly rather
 * than imply safety. More durably, `docs/EVENT_FORMAT.md` is the contract the Linux
 * desktop app is built against in a separate session, and every name in it is an
 * instruction to the next implementer about what the concept means. A type called
 * `FOCUS_ABANDONED` invites a screen that says abandoned, a rule that counts
 * abandonments, and an inference of abandonment by subtraction, which
 * `domain.query.FocusCounts` explicitly forbids. DECISIONS.md C6.
 *
 * Addendum 01 4e: a session ended early is a completed short session. Fourteen
 * minutes is fourteen minutes.
 */
@Serializable
data class FocusEndedEarly(val sessionId: String, val actualSeconds: Int) : EventPayload {
    override val primaryEntityId get() = sessionId
}

/**
 * Time added to a running session. Addendum 01 4f.
 *
 * It does not end the session and does not start a new one. A session has exactly
 * one FOCUS_STARTED and at most one terminal event, and an extension is neither, so
 * ending a timer never has to break flow to add ten minutes to it.
 *
 * [newPlannedSeconds] is the absolute value after the extension and is what the
 * reducer applies. Applying [addedSeconds] as a delta instead would make the
 * replayed result depend on the order in which two devices extended, and the
 * absolute figure is the one the person was actually shown. [addedSeconds] is the
 * other half of the before and after pair the log convention requires, and it is
 * what a Trail row reads, so "Added ten minutes" comes out of the log rather than
 * out of a subtraction between two events that may not both be on the page.
 */
@Serializable
data class FocusExtended(
    val sessionId: String,
    val addedSeconds: Int,
    val newPlannedSeconds: Int,
) : EventPayload {
    override val primaryEntityId get() = sessionId
}

// Pulse ----------------------------------------------------------------------

/**
 * [subjectId] and [subjectKind] are what make `FiringHistory` derivable at all.
 *
 * CLARITY_LOGIC_ENGINE.md 2.1 keys `lastStageBySubject` and `lastFiredBySubject` by
 * `(FamilyKey, subjectId)`, and 7.6 requires that the whole of `FiringHistory` be
 * derived from PULSE_GENERATED, REPORT_GENERATED and PLAN_OFFERED and never from
 * DataStore, because a device that has just merged a log must compute the same next
 * variant as the device that produced it and DataStore does not merge. Without the
 * subject on the event there is nothing to key by, so monotonicity, the flat family
 * cooldown and every per subject rule are underivable rather than merely awkward.
 * Both are null for a family with no subject, which is what 2.1's `SubjectSelector`
 * NONE yields.
 *
 * This is the proposed shape for issue #19 and the owner may still adjust it.
 */
@Serializable
data class PulseGenerated(
    val pulseId: String,
    val dateKey: String,
    val family: String,
    val escalationStage: Int,
    val register: String,
    val variantKey: String,
    val renderedObservation: String,
    val renderedQuestion: String?,
    val factSnapshot: Map<String, String>,
    val reflectionPeriod: ReflectionPeriod,
    val subjectId: String? = null,
    val subjectKind: SubjectKind? = null,
) : EventPayload {
    override val primaryEntityId get() = pulseId
}

/**
 * [responseLabel] is stored verbatim so a later callback quotes what the person
 * actually saw, not a label reworded in a newer app version.
 *
 * **[subjectId] and [subjectKind] are denormalized from the PULSE_GENERATED this
 * answers, deliberately.** They could be joined through [pulseId], and phase 3
 * shipped a real defect from exactly that pattern: a focus row resolved its item
 * through FOCUS_STARTED and rendered blank whenever the origin event fell outside
 * the loaded page. `selfReportVsData` is the family CORPUS_2_REPORT.md 2.6 calls
 * the flagship of the whole engine, and it compares what a person said about an
 * area against what they did in it. A family of that weight must not depend on a
 * join that can miss, and one duplicated string is a small price for that.
 *
 * This is the proposed shape for issue #19 and the owner may still adjust it.
 */
@Serializable
data class PulseAnswered(
    val pulseId: String,
    val responseKey: String,
    val responseLabel: String,
    val responseIsPositive: Boolean,
    val subjectId: String? = null,
    val subjectKind: SubjectKind? = null,
) : EventPayload {
    override val primaryEntityId get() = pulseId
}

// Report ---------------------------------------------------------------------

/**
 * One rendered section, with everything the selector needs in order not to repeat
 * itself. The sentence alone is not enough, and that is the whole point.
 *
 * CLARITY_LOGIC_ENGINE.md 7.6 step 1 filters out variants used within 90 days, 7.3
 * applies a flat 14 day cooldown per family, and 6.4 caps `hardStretch` at 42 days.
 * None of those three can be computed from rendered text: two different variants of
 * one family read as two different sentences, and a stage 3 line and a stage 1 line
 * of the same family read as unrelated. [familyKey], [variantKey],
 * [escalationStage], [register], [subjectId] and [subjectKind] are the keys those
 * rules are actually stated in, carried on the event so that `FiringHistory`
 * derives from the log the way 7.6 requires rather than from DataStore.
 *
 * This is the proposed shape for issue #19 and the owner may still adjust it.
 */
@Serializable
data class ReportSectionSnapshot(
    val sectionKey: String,
    val sidehead: String,
    val text: String,
    val familyKey: String,
    val variantKey: String,
    val escalationStage: Int,
    val register: String,
    val subjectId: String? = null,
    val subjectKind: SubjectKind? = null,
)

/**
 * [headlineVariantKey] is here because [headlineKey] names the family and the 90
 * day exclusion in CLARITY_LOGIC_ENGINE.md 7.6 step 1 needs the variant. Without
 * it the headline is the one rendered line in the app that can repeat itself word
 * for word, and it is the largest type on the screen.
 *
 * This is the proposed shape for issue #19 and the owner may still adjust it.
 */
@Serializable
data class ReportGenerated(
    val reportId: String,
    val weekStartKey: String,
    val headlineKey: String,
    val renderedSections: List<ReportSectionSnapshot>,
    val factSnapshot: Map<String, String>,
    val headlineVariantKey: String? = null,
) : EventPayload {
    override val primaryEntityId get() = reportId
}

// Guidance -------------------------------------------------------------------

/**
 * [offeredLine] is nominal and [committedLine] is first person. The imperative form
 * never exists anywhere in this app, so it is not stored here either.
 */
@Serializable
data class PlanOffered(
    val planId: String,
    val weekStartKey: String,
    val frameKey: String,
    val cueKey: String,
    val actionKey: String,
    val familyKey: String,
    val subjectId: String?,
    val offeredLine: String,
    val committedLine: String,
    val resolutionFactRef: FactRef,
) : EventPayload {
    override val primaryEntityId get() = planId
}

/** Declining writes nothing at all. There is no PLAN_DECLINED, deliberately. */
@Serializable
data class PlanAccepted(val planId: String) : EventPayload {
    override val primaryEntityId get() = planId
}

// Settings -------------------------------------------------------------------

/**
 * Only for settings that affect behavior history, such as `afterCompleting`.
 * Ordinary per device preferences live in DataStore and are never events.
 */
@Serializable
data class SettingChanged(
    val key: String,
    val previousValue: String,
    val newValue: String,
) : EventPayload {
    override val primaryEntityId get() = key
}

// Presence -------------------------------------------------------------------

/**
 * A presence marker. The local date, and nothing else. Addendum 01 2d.
 *
 * No time, no count, no session length, no duration. Written at most once per local
 * calendar day on the first foreground. It exists so that a long gap can be
 * detected without any tracking at all, which is what Addendum 01 4d's re-entry
 * state needs: someone who has been away for a fortnight is greeted differently,
 * and the app has no other way to know that. A date key per day is the smallest
 * record that can answer the question.
 *
 * **It is never counted as activity, anywhere, by anything.**
 * `ClarityEventType.isUserActivity` excludes it, so `activeDays`, `eventsPerDay`,
 * `totalEvents` and `eventsPerArea` do not see it, and it renders no Trail row and
 * is absent from the Trail day header count. DECISIONS.md C7 works through what
 * happens otherwise, and all three failures look entirely plausible on screen:
 * CORPUS_3's `mo.steady` would tell someone who did nothing for a fortnight that
 * they had been steady, CORPUS_2's `ob.day.l03` would report a count of app opens
 * as a count of activity, and CLARITY_LOGIC_ENGINE.md 6.1's `quietDay` would be
 * close to unreachable. An event added for the sole purpose of detecting an absence
 * would have become the mechanism that measured a presence, which is the one thing
 * 4d exists to prevent.
 *
 * [primaryEntityId] is the date key, because that is the only identifier this event
 * has and there is nothing else it could honestly be. [SettingChanged] does the
 * same with its key.
 */
@Serializable
data class AppOpened(val dateKey: String) : EventPayload {
    override val primaryEntityId get() = dateKey
}
