package com.kamsiob.claritynow.domain.query

import com.kamsiob.claritynow.data.event.AppOpened
import com.kamsiob.claritynow.data.event.AreaArchived
import com.kamsiob.claritynow.data.event.AreaCreated
import com.kamsiob.claritynow.data.event.AreaDeleted
import com.kamsiob.claritynow.data.event.AreaRecolored
import com.kamsiob.claritynow.data.event.AreaRenamed
import com.kamsiob.claritynow.data.event.AreaReordered
import com.kamsiob.claritynow.data.event.AreaUnarchived
import com.kamsiob.claritynow.data.event.ClarityEvent
import com.kamsiob.claritynow.data.event.ClarityEventType
import com.kamsiob.claritynow.data.event.FocusCompleted
import com.kamsiob.claritynow.data.event.FocusEndedEarly
import com.kamsiob.claritynow.data.event.FocusExtended
import com.kamsiob.claritynow.data.event.FocusStarted
import com.kamsiob.claritynow.data.event.ItemAdded
import com.kamsiob.claritynow.data.event.ItemCompleted
import com.kamsiob.claritynow.data.event.ItemDeleted
import com.kamsiob.claritynow.data.event.ItemEdited
import com.kamsiob.claritynow.data.event.ItemEstimated
import com.kamsiob.claritynow.data.event.ItemFiled
import com.kamsiob.claritynow.data.event.ItemPromoted
import com.kamsiob.claritynow.data.event.ItemQueued
import com.kamsiob.claritynow.data.event.ItemReopened
import com.kamsiob.claritynow.data.event.ItemReordered
import com.kamsiob.claritynow.data.event.PlanAccepted
import com.kamsiob.claritynow.data.event.PlanOffered
import com.kamsiob.claritynow.data.event.PulseAnswered
import com.kamsiob.claritynow.data.event.PulseGenerated
import com.kamsiob.claritynow.data.event.ReportGenerated
import com.kamsiob.claritynow.data.event.SettingChanged
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.abs

/**
 * What a Trail row says, chosen from the payload alone. MASTER_BUILD_PROMPT 9.
 *
 * An enum rather than a string resource id, so this mapping stays in pure Kotlin
 * where it can be tested without an Android runtime, and so a row shape is decided
 * once by the log rather than twice by two composables. The `when` from a key to a
 * string lives in `ui/trail/TrailStrings.kt` and the compiler enforces that it
 * covers every value here.
 *
 * There are twenty eight values for twenty eight event types, and the match is a
 * coincidence rather than a correspondence. Two things cancel out. ITEM_PROMOTED is
 * the one type with two row shapes: a promotion into an empty seat and a swap that
 * displaced something read as different events to the person who made them, and
 * only `ItemPromoted.demotedItemId` tells them apart. That distinction exists in no
 * prose anywhere, which is exactly why it is encoded here rather than rediscovered.
 * APP_OPENED runs the other way and has no value at all, because it prints no row.
 *
 * **APP_OPENED renders nothing, and that is a rule rather than an omission.** A
 * daily "opened the app" line would be noise in a chronological log, and it would
 * also be a visible tally of a person's presence, which is the same measurement
 * Addendum 01 4d forbids turned inside out. `sentenceKeyFor` returns null for it
 * and [trailRows] never builds a row, so it is absent from the day header count
 * too, which counts rows. MASTER_BUILD_PROMPT 5.2 and 9, DECISIONS.md C7.
 *
 * Every one of these is a record of what happened, never an observation about it.
 * CLAUDE.md rule 8 draws that line: a transcript entry that names a verb and a
 * snapshot carries no interpretation, so it belongs in `strings.xml`, while any
 * sentence that compares, counts across rows or qualifies with an adverb is an
 * observation and has to come through the engine layers from a corpus.
 */
enum class TrailSentenceKey {
    AREA_CREATED,
    AREA_RENAMED,
    AREA_RECOLORED,
    AREA_REORDERED,
    AREA_ARCHIVED,
    AREA_UNARCHIVED,
    AREA_DELETED,

    ITEM_ADDED,
    ITEM_FILED,
    ITEM_EDITED,
    ITEM_ESTIMATED,
    ITEM_ESTIMATE_CLEARED,
    ITEM_QUEUED,
    ITEM_PROMOTED,
    ITEM_SWAPPED,
    ITEM_COMPLETED,
    ITEM_REOPENED,
    ITEM_REORDERED,
    ITEM_DELETED,

    FOCUS_STARTED,
    FOCUS_COMPLETED,

    /**
     * FOCUS_ENDED_EARLY. The key kept its name through the event rename, because
     * the string it selects, "Stopped after N minutes", never contained the word
     * that was renamed away and did not change.
     */
    FOCUS_STOPPED,
    FOCUS_EXTENDED,

    PULSE_GENERATED,
    PULSE_ANSWERED,

    REPORT_GENERATED,

    PLAN_OFFERED,
    PLAN_ACCEPTED,

    SETTING_CHANGED,
}

/**
 * The values a row needs that the event's own payload does not carry.
 *
 * Resolved by folding the log, never by reading a live entity. Ten of the
 * twenty eight payloads carry no title or name snapshot of their own, and the
 * tempting fix for those is `state.items[id]?.title`, which produces a correct
 * looking screen that silently rewrites its own history the first time anything is
 * renamed and shows nothing at all for anything deleted. Keeping the resolved
 * values in a separate object handed to the mapper is what makes that shortcut
 * unavailable rather than merely discouraged. See CLARITY_LOGIC_ENGINE.md 1.1
 * prohibition 4.
 */
data class TrailRowContext(
    /** The area this event belongs to, or null for the types that have none. */
    val areaId: String?,
    /** The area's color as of this event, not as of now. */
    val areaColorHex: String?,
    /** The item's title as of this event, for the payloads that carry no snapshot. */
    val itemTitle: String?,
    /** The area's name as of this event, for the payloads that carry no snapshot. */
    val areaName: String?,
)

/**
 * One rendered Trail row.
 *
 * Deliberately has no field that could hold a live entity name, a resource id, a
 * color object or an icon. Everything here is either a value out of the log or a
 * decision the pure mapper made about it, so the screen cannot reach past this type
 * to a shortcut and the whole mapping stays testable off device.
 */
data class TrailRow(
    val eventId: String,
    val wallClock: Long,
    val sentence: TrailSentenceKey,
    /** The snapshot title or name. Never a live entity name. */
    val subject: String?,
    /**
     * The second slot, for the two rows that name two things: a rename, which names
     * both sides of itself, and a filing, which names the destination area.
     */
    val secondary: String?,
    /**
     * Whole minutes from a focus payload's own seconds. The two terminal rows read
     * what the session ran for; the extension row reads what was added to it.
     */
    val minutes: Int?,
    val areaId: String?,
    /** The area's color hex as of this event, or null when the event has no area. */
    val areaColorHex: String?,
    val isCompletion: Boolean,
    /**
     * The item a completion row names, so the screen can offer to undo it.
     *
     * **Set only on a completion**, and it is the one live entity reference in a type
     * whose whole point is that it holds none. The rule it does not break is the one
     * stated above: nothing here is a *name*, a color or an icon that could be resolved
     * live and silently rewrite history. An id is an address. A row still renders its
     * title from the snapshot in its own payload, so a reopened item that is later
     * renamed keeps saying what it said on the day it was completed, and the id only
     * decides which item an action would act on.
     *
     * It is null on every other row shape, including `ITEM_REOPENED`, because reopening
     * a reopening is not an act.
     */
    val itemId: String?,
    /** False on every row of a cluster after the first. See [trailRows]. */
    val showsTimestamp: Boolean,
)

/**
 * Newest first, with the three tiebreaks that make the order total.
 *
 * `wallClock` orders the display and never the state. MASTER_BUILD_PROMPT 5.1 bans
 * it from the total order because two devices disagree about the time; it is the
 * only wall time in the log and so the only thing that can answer which day
 * something happened on. Nothing the reducer, a projection, a checkpoint or a merge
 * does may consult it, and nothing here folds state.
 *
 * The tiebreaks are not decoration. `ClarityRepository.commit` reads the clock once
 * per commit and stamps every event it writes with that one reading, so an
 * ITEM_COMPLETED and the ITEM_PROMOTED it caused carry identical wall clocks and
 * consecutive lamports. Without `lamport` descending the promotion prints below the
 * completion that caused it. After a merge two devices can match on both, so
 * `originId` and `id` finish the job.
 */
val TRAIL_DISPLAY_ORDER: Comparator<ClarityEvent> = compareByDescending<ClarityEvent> { it.wallClock }
    .thenByDescending { it.lamport }
    .thenByDescending { it.originId }
    .thenByDescending { it.id }

/**
 * MASTER_BUILD_PROMPT 9: "timestamps on the first event of each 10 minute cluster".
 */
private const val CLUSTER_MILLIS = 10L * 60L * 1000L

/**
 * Turns a page of events into rows, deciding which of them print a timestamp.
 *
 * The clustering rule lives here rather than in the composable for two reasons.
 * It is arithmetic over instants and a local calendar, which is the kind of thing
 * that is wrong only across a day boundary or a daylight saving shift and therefore
 * has to be testable without a device. And it is a decision about the whole list
 * rather than about one row, so a composable computing it per item would either
 * recompute the page on every recomposition or quietly depend on the order the lazy
 * list happened to ask in.
 *
 * A cluster is anchored on its first row in display order, which is its newest
 * member, and a row joins it when it falls within ten minutes of that anchor. The
 * anchor rather than the previous row is what "the first event of each 10 minute
 * cluster" means: with the previous row as the reference a slow steady stream would
 * print one timestamp and then nothing for an hour.
 *
 * **A cluster never spans a local day boundary.** Two events four minutes apart
 * across midnight belong to two different day groups with two different headers,
 * and the second one inheriting a suppressed timestamp from the first would leave
 * the top row of a day with no time on it at all.
 *
 * **An event that prints no row cannot anchor a cluster either.** APP_OPENED is
 * written on the first foreground of a day, which is by definition a few
 * milliseconds before whatever the person then did, so an invisible anchor would
 * take the timestamp off the first real row of almost every day. The anchor is
 * therefore advanced only once a row has actually been produced, which keeps the
 * two facts in one place instead of relying on a filter here agreeing with the
 * table in [trailRowFor].
 *
 * [events] may arrive in any order; they are put into display order here so a
 * caller cannot get the clustering wrong by handing over a page it sorted itself.
 */
fun trailRows(
    events: List<ClarityEvent>,
    zone: ZoneId,
    context: (ClarityEvent) -> TrailRowContext,
): List<TrailRow> {
    val ordered = events.sortedWith(TRAIL_DISPLAY_ORDER)
    val rows = ArrayList<TrailRow>(ordered.size)
    var anchorMillis = 0L
    var anchorDate: LocalDate? = null
    for (event in ordered) {
        val date = Instant.ofEpochMilli(event.wallClock).atZone(zone).toLocalDate()
        val inCluster = anchorDate == date && abs(anchorMillis - event.wallClock) < CLUSTER_MILLIS
        val row = trailRowFor(event, context(event), showsTimestamp = !inCluster) ?: continue
        if (!inCluster) {
            anchorMillis = event.wallClock
            anchorDate = date
        }
        rows += row
    }
    return rows
}

/**
 * One row from one event and the context resolved for it, or null for an event that
 * prints no row.
 *
 * Two exhaustive `when` expressions rather than one, on purpose. The first is over
 * `ClarityEventType` and is the row shape table: adding a twenty ninth type breaks
 * it at compile time, which is the only mechanism that reliably stops a new event
 * being invisible in the transcript. The second is over the sealed `EventPayload`
 * hierarchy and reads the fields, which keeps twenty eight downcasts out of this
 * file. Each guards the other.
 *
 * Null is returned only where the table says a type has no sentence at all, which
 * today is APP_OPENED and nothing else. It is a return value rather than a filter
 * on the caller's side so that a type deciding not to print is stated once, in the
 * table, where the compiler is watching.
 */
fun trailRowFor(
    event: ClarityEvent,
    context: TrailRowContext,
    showsTimestamp: Boolean = true,
): TrailRow? {
    val content = contentOf(event, context)
    val sentence = sentenceKeyFor(event.type, content) ?: return null
    return TrailRow(
        eventId = event.id,
        wallClock = event.wallClock,
        sentence = sentence,
        subject = content.subject,
        secondary = content.secondary,
        minutes = content.minutes,
        areaId = context.areaId,
        areaColorHex = content.areaColorHex ?: context.areaColorHex,
        isCompletion = event.type == ClarityEventType.ITEM_COMPLETED,
        itemId = (event.payload as? ItemCompleted)?.itemId,
        showsTimestamp = showsTimestamp,
    )
}

/**
 * The row shape table. One branch per event type, so a new type fails the build
 * here rather than rendering as nothing on a screen nobody rereads.
 *
 * Null means the type deliberately prints nothing. Deciding that here rather than
 * by leaving a type out of a list somewhere is what keeps "invisible on purpose"
 * and "invisible by accident" different states.
 */
/**
 * Two event types produce more than one row shape, so this takes the whole content
 * rather than one flag: a promotion reads differently when it displaced something, and
 * an estimate reads differently when it was removed rather than set. A row that said
 * "Set a time estimate" about a removal would be a false sentence, which is the one
 * thing a transcript may never be.
 */
private fun sentenceKeyFor(
    type: ClarityEventType,
    content: TrailRowContent,
): TrailSentenceKey? = when (type) {
    ClarityEventType.AREA_CREATED -> TrailSentenceKey.AREA_CREATED
    ClarityEventType.AREA_RENAMED -> TrailSentenceKey.AREA_RENAMED
    ClarityEventType.AREA_RECOLORED -> TrailSentenceKey.AREA_RECOLORED
    ClarityEventType.AREA_REORDERED -> TrailSentenceKey.AREA_REORDERED
    ClarityEventType.AREA_ARCHIVED -> TrailSentenceKey.AREA_ARCHIVED
    ClarityEventType.AREA_UNARCHIVED -> TrailSentenceKey.AREA_UNARCHIVED
    ClarityEventType.AREA_DELETED -> TrailSentenceKey.AREA_DELETED

    ClarityEventType.ITEM_ADDED -> TrailSentenceKey.ITEM_ADDED
    ClarityEventType.ITEM_FILED -> TrailSentenceKey.ITEM_FILED
    ClarityEventType.ITEM_EDITED -> TrailSentenceKey.ITEM_EDITED
    ClarityEventType.ITEM_ESTIMATED ->
        if (content.clearsValue) {
            TrailSentenceKey.ITEM_ESTIMATE_CLEARED
        } else {
            TrailSentenceKey.ITEM_ESTIMATED
        }
    ClarityEventType.ITEM_QUEUED -> TrailSentenceKey.ITEM_QUEUED
    ClarityEventType.ITEM_PROMOTED ->
        if (content.isSwap) TrailSentenceKey.ITEM_SWAPPED else TrailSentenceKey.ITEM_PROMOTED
    ClarityEventType.ITEM_COMPLETED -> TrailSentenceKey.ITEM_COMPLETED
    ClarityEventType.ITEM_REOPENED -> TrailSentenceKey.ITEM_REOPENED
    ClarityEventType.ITEM_REORDERED -> TrailSentenceKey.ITEM_REORDERED
    ClarityEventType.ITEM_DELETED -> TrailSentenceKey.ITEM_DELETED

    ClarityEventType.FOCUS_STARTED -> TrailSentenceKey.FOCUS_STARTED
    ClarityEventType.FOCUS_COMPLETED -> TrailSentenceKey.FOCUS_COMPLETED
    ClarityEventType.FOCUS_ENDED_EARLY -> TrailSentenceKey.FOCUS_STOPPED
    ClarityEventType.FOCUS_EXTENDED -> TrailSentenceKey.FOCUS_EXTENDED

    ClarityEventType.PULSE_GENERATED -> TrailSentenceKey.PULSE_GENERATED
    ClarityEventType.PULSE_ANSWERED -> TrailSentenceKey.PULSE_ANSWERED

    ClarityEventType.REPORT_GENERATED -> TrailSentenceKey.REPORT_GENERATED

    ClarityEventType.PLAN_OFFERED -> TrailSentenceKey.PLAN_OFFERED
    ClarityEventType.PLAN_ACCEPTED -> TrailSentenceKey.PLAN_ACCEPTED

    ClarityEventType.SETTING_CHANGED -> TrailSentenceKey.SETTING_CHANGED

    // No row. See the note on TrailSentenceKey.
    ClarityEventType.APP_OPENED -> null
}

/** The slots a row fills, read from the payload and the resolved context. */
private data class TrailRowContent(
    val subject: String? = null,
    val secondary: String? = null,
    val minutes: Int? = null,
    val isSwap: Boolean = false,
    /** Set when the event removed a value rather than setting one. */
    val clearsValue: Boolean = false,
    /** Set only where the payload states the color more exactly than a fold can. */
    val areaColorHex: String? = null,
)

/**
 * Whole minutes from a payload's own seconds, rounded to nearest.
 *
 * MASTER_BUILD_PROMPT 10 discards a session under sixty seconds rather than
 * recording it as a completion, so a zero minute row is legal and honest rather
 * than a defect to round away.
 */
private fun minutesOf(seconds: Int): Int = (seconds + 30) / 60

private fun contentOf(event: ClarityEvent, context: TrailRowContext): TrailRowContent =
    when (val payload = event.payload) {
        // Areas. Six of the seven carry their own name snapshot, which is what lets
        // a deleted area still name itself years later.
        is AreaCreated -> TrailRowContent(subject = payload.name)
        is AreaRenamed -> TrailRowContent(
            subject = payload.previousName,
            secondary = payload.newName,
        )
        // The one place a payload beats the fold: a recolor at the same wall clock
        // as another recolor would resolve to whichever sorted last, and this event
        // states its own answer.
        is AreaRecolored -> TrailRowContent(
            subject = context.areaName,
            areaColorHex = payload.newHex,
        )
        is AreaReordered -> TrailRowContent(subject = context.areaName)
        is AreaArchived -> TrailRowContent(subject = payload.nameSnapshot)
        is AreaUnarchived -> TrailRowContent(subject = payload.nameSnapshot)
        is AreaDeleted -> TrailRowContent(subject = payload.nameSnapshot)

        // Items.
        is ItemAdded -> TrailRowContent(subject = payload.title)
        // The one item row that names two things. `areaNameSnapshot` is carried on
        // this payload for exactly this, per MASTER_BUILD_PROMPT 5.2, so a filing
        // from three months ago still names the area it went into even if that area
        // has since been renamed or deleted. The title is folded, because filing
        // carries no title snapshot of its own.
        is ItemFiled -> TrailRowContent(
            subject = context.itemTitle,
            secondary = payload.areaNameSnapshot,
        )
        is ItemEdited -> TrailRowContent(subject = payload.newTitle)
        // The estimate itself is deliberately not shown, for the same reason
        // `activeDurationDays` is not shown on a completion below: it is a figure
        // the Report speaks about, and a transcript row carrying it would put the
        // same number on screen from two paths. Addendum 01 7a additionally forbids
        // any sentence stating a delta between an estimate and an actual, and a row
        // reading "Estimated 30 minutes" sitting a few lines above one reading
        // "Finished 50 minutes of focus" invites the reader to do the subtraction
        // the corpus is forbidden from doing.
        is ItemEstimated -> TrailRowContent(
            subject = context.itemTitle,
            clearsValue = payload.newEstimateMinutes == null,
        )
        is ItemQueued -> TrailRowContent(subject = context.itemTitle)
        // A swap names only the item that moved to the front. `demotedItemId`
        // carries no title snapshot, and MASTER_BUILD_PROMPT 8.2's rule that the
        // chooser names the item being demoted is about the chooser sheet.
        is ItemPromoted -> TrailRowContent(
            subject = payload.titleSnapshot,
            isSwap = payload.demotedItemId != null,
        )
        // `activeDurationDays` is in this payload and is deliberately not shown.
        // It is the kind of figure the Report states, and a row carrying it would
        // put the same number on screen twice, computed at two different moments.
        is ItemCompleted -> TrailRowContent(subject = payload.titleSnapshot)
        is ItemReopened -> TrailRowContent(subject = context.itemTitle)
        is ItemReordered -> TrailRowContent(subject = context.itemTitle)
        is ItemDeleted -> TrailRowContent(subject = payload.titleSnapshot)

        // Focus. The title comes through the session's own start event, so a
        // terminal event on a merged log with no start renders with no subject
        // rather than crashing, which is how the reducer treats the same case.
        is FocusStarted -> TrailRowContent(subject = context.itemTitle)
        is FocusCompleted -> TrailRowContent(
            subject = context.itemTitle,
            minutes = minutesOf(payload.actualSeconds),
        )
        is FocusEndedEarly -> TrailRowContent(
            subject = context.itemTitle,
            minutes = minutesOf(payload.actualSeconds),
        )
        // The minutes added, not the new total. The row records what the person
        // did, and what they did was add ten minutes; the total is a state the
        // focus screen was already showing them.
        is FocusExtended -> TrailRowContent(
            subject = context.itemTitle,
            minutes = minutesOf(payload.addedSeconds),
        )

        // Pulse. The generated row names nothing: `factSnapshot` is an untyped
        // string map whose keys no document specifies, so nothing may be read out
        // of it. The answered row quotes the label the person actually saw.
        is PulseGenerated -> TrailRowContent()
        is PulseAnswered -> TrailRowContent(subject = payload.responseLabel)

        // The week key is passed through raw. Rendering it as a date is a locale
        // and format decision, which belongs to the screen and not to the log.
        is ReportGenerated -> TrailRowContent(subject = payload.weekStartKey)

        // Guidance. These two never appear as a pair and the absence of an
        // acceptance is never rendered as anything. CLAUDE.md rule 13: the
        // mechanism must have no way to tell someone they broke a promise.
        is PlanOffered -> TrailRowContent()
        is PlanAccepted -> TrailRowContent()

        is SettingChanged -> TrailRowContent()

        // APP_OPENED prints no row, so this is never read. It is here because the
        // `when` is exhaustive over the payload hierarchy, and that exhaustiveness
        // is the mechanism that stops a new type being silently invisible. A type
        // that genuinely should be invisible says so once, in `sentenceKeyFor`.
        is AppOpened -> TrailRowContent()
    }
