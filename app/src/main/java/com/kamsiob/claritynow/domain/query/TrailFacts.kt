package com.kamsiob.claritynow.domain.query

import com.kamsiob.claritynow.data.event.ClarityEvent
import com.kamsiob.claritynow.data.event.ClarityEventType
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
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

/**
 * One completion, read straight from the payload snapshots. MASTER_BUILD_PROMPT 8.1.
 *
 * Every field is a value the writing device stored at the moment it happened. None
 * of them is a lookup against a live entity, because an area renamed in July must
 * not rewrite what a completion in March says, and a deleted area must still name
 * itself. [activeDurationDays] in particular is the payload's own number: the
 * device that wrote it had already shown that figure to the person, and recomputing
 * it here against a second device's clock would contradict what they were told.
 */
data class CompletedRecord(
    val itemId: String,
    val titleSnapshot: String,
    val areaId: String,
    val areaNameSnapshot: String,
    /** The payload's own value. Never recomputed. */
    val activeDurationDays: Int,
    val completedAt: Long,
)

/**
 * One completed item's actual against its own estimate, as a ratio and nothing else.
 * MASTER_BUILD_PROMPT 14b.8, Addendum 01 7a.
 *
 * **Neither magnitude survives this type, and that is the whole point of it.** 14b.8
 * bans a rendered delta between an estimate and an actual, and the strongest form of
 * that ban is a fact set in which the subtraction cannot be performed rather than one
 * in which a validator catches the answer afterward. [TrailQueries.estimateOutcomes]
 * divides the two numbers inside its own body and returns this, so the minutes a
 * person typed and the minutes an item was active exist nowhere a rule, a measure or
 * a template can reach them. `actual - estimate` is not a subtraction anything above
 * this line is able to write.
 *
 * [activeToEstimate] is how many times the item's own estimate the item spent active:
 * 1.0 is a prediction that landed, 3.0 is a thing that took three times as long as
 * the person thought. It is a **multiple and never a percentage**, because a
 * percentage is one subtraction from the second forbidden line in 14b.8: a ratio of
 * 2.4 rendered as 240 percent is `You were off by 140 percent` with a literal
 * hundred taken off it. A multiple has no such neighbor.
 *
 * **It is a stay, not an effort.** The actual is elapsed time from the promotion that
 * made the item active to the completion, which is what the log records and what
 * 14b.3 means by "the actual comes free". It is not time spent working, which nothing
 * in this app measures, and a family reading it must say what it means: an hour long
 * thing that sits on a plate for a day and a half is a true and useful reading of a
 * person's days, and "took a day and a half" is not.
 */
data class EstimateOutcome(val itemId: String, val activeToEstimate: Double)

/**
 * Focus session outcomes in a window, attributed to the FOCUS_STARTED instant.
 *
 * [unresolved] exists so that no rule anywhere ever infers an early ending by
 * subtraction. A killed process leaves a started session with no terminal event, so
 * `started != completed + endedEarly` is a legal state rather than a bug, and
 * `ClarityReducer.focusEnded` already records the mirror case as a diagnostic.
 * MASTER_BUILD_PROMPT 10 treats an early ending neutrally everywhere; guessing that
 * an unresolved session ended early would put a number behind that language that
 * the log does not support.
 *
 * [endedEarly] was `abandoned` until the Addendum 01 schema commit renamed the
 * event, and this field moved with it. The comment above had already made the
 * argument for the new name on its own grounds, before the addendum arrived: the
 * log does not reliably know that a session was abandoned, so a counter that says
 * it does is the wrong name for the number regardless of what a person can see.
 * DECISIONS.md C6.
 */
data class FocusCounts(
    val started: Int,
    val completed: Int,
    val endedEarly: Int,
    val unresolved: Int,
)

/**
 * A half open wall clock window, `[fromMillis, toMillis)`.
 *
 * Every bound in this package is exclusive on the upper end, so two adjacent
 * windows share no event and a day boundary belongs to exactly one of them.
 */
data class TrailWindow(val fromMillis: Long, val toMillis: Long)

/**
 * A return to the app after an absence long enough to change what the app may say.
 * MASTER_BUILD_PROMPT 14b.4, Addendum 01 4d, issue #27.
 *
 * This audience leaves and comes back, and a fortnight of nothing is what a
 * fluctuating condition, a bad month or a hospital stay looks like from inside the
 * data. 14b.4 spends its whole length on what the app may not do with that fact,
 * and this type is the first place those prohibitions are enforced rather than
 * described.
 *
 * **It deliberately does not carry the length of the absence, and it never will.**
 * 14b.4 forbids stating it "not in days, not in weeks, not as a date, not as `since
 * March`", and a field holding the number would leave that prohibition resting on
 * somebody remembering it. The comparison against [MIN_GAP_DAYS] happens inside
 * [TrailQueries.lastReEntryOnOrBefore] and the number does not survive the return.
 * Nothing that renders can obtain it, because nothing hands it out. This is
 * design-v3.md 15 applied to a type signature: the statistically common shape here
 * is `fun gapDays(): Int?`, which is the shape that makes the forbidden screen a
 * one line mistake, so it is not the shape used.
 *
 * [returnedOn] is the date key of the open that noticed the absence, never the date
 * the absence began, which is the other value a surface must never reach. It is
 * here because both suppression windows 14b.4 requires are measured forward from
 * it: Pulse generates nothing for the first two days back, and the Report withholds
 * every decline, neglect and gap family for the first full week. Both are later
 * phases, and both need [daysSince] to decide. That is why this is a value rather
 * than a boolean.
 *
 * Constructing one directly is legal, and a test of the phase 6 surface will do
 * exactly that. What is not legal anywhere is inventing the absence: a [ReEntry]
 * asserts an absence with two ends, and the only thing that knows both ends is the
 * event log.
 */
data class ReEntry(val returnedOn: String) {

    /**
     * Calendar days from the return to [dateKey]. Zero on the day of the return
     * itself, one on the day after it.
     *
     * **This decides whether the engine speaks. It is never rendered.** It counts
     * days back rather than days away, so it is not the forbidden number, but 14b.4
     * also says the screen counts nothing at all, and "you have been back for three
     * days" is a count. Every caller of this is a suppression window, and a window
     * is a comparison rather than a sentence.
     *
     * Counted between calendar dates, so a daylight saving shift inside the window
     * neither shortens nor extends it. An unparseable key on either end answers
     * zero, which reads as the day of the return and therefore leaves every
     * suppression window at its widest: when this cannot tell, it protects.
     *
     * Negative when [dateKey] falls before the return, which a caller comparing
     * against a window reads as outside it, correctly.
     */
    fun daysSince(dateKey: String): Int {
        val from = parseDayKey(returnedOn) ?: return 0
        val to = parseDayKey(dateKey) ?: return 0
        return ChronoUnit.DAYS.between(from, to).toInt()
    }

    companion object {

        /**
         * The absence that changes what the app does, in calendar days.
         * MASTER_BUILD_PROMPT 14b.4: "A gap of 14 or more days puts the app into the
         * re-entry state".
         *
         * Fourteen or more, so exactly fourteen qualifies and thirteen does not.
         * Stated once, here, because a threshold copied into a second place is a
         * threshold that will one day be two different numbers, and the two screens
         * reading it are built four phases apart.
         */
        const val MIN_GAP_DAYS = 14
    }
}

/**
 * A `yyyy-MM-dd` key as a calendar date, or null when it is not one.
 *
 * Parsed with the ISO parser rather than a pattern, for the reason [TrailQueries]
 * gives about its own formatter: a pattern resolves against the ambient default
 * locale, and this package reads no ambient anything.
 *
 * Null rather than an exception, because the keys that reach here come from event
 * payloads, and a payload can arrive from an imported file or from a second
 * implementation of this app built against `docs/EVENT_FORMAT.md`. One malformed
 * key in a restored backup must cost that one marker, not the first screen a
 * returning person sees.
 */
internal fun parseDayKey(dateKey: String): LocalDate? =
    runCatching { LocalDate.parse(dateKey) }.getOrNull()

/**
 * Events a person caused. Excludes the four the app writes without a user act.
 *
 * PULSE_GENERATED, REPORT_GENERATED and PLAN_OFFERED arrive with no user gesture.
 * If they counted, someone who opened the app daily and touched nothing would read
 * as active on every day of the fortnight, and CLARITY_LOGIC_ENGINE.md 6.1's
 * `quietDay` and `quietWeek` families could never fire at all.
 *
 * **APP_OPENED is excluded for the same reason and it is the sharp case**, because
 * for it that sentence is not a hypothetical. It is written on the first foreground
 * of each local day, so counting it would literally mark a day active for someone
 * who opened the app and did nothing. Three numbers would go wrong and each would
 * still look plausible: CORPUS_3's `mo.steady`, active on 9 or more of the last 14
 * days, would tell that person they had been steady; CORPUS_2's `ob.day.l03`,
 * `{n} of 7 days had activity`, would become a count of app opens presented as
 * a count of activity; and `quietDay`, which fires below two events in a window,
 * would be close to unreachable. Worse than any of those, APP_OPENED exists only to
 * detect an absence for Addendum 01 4d, and a returning person must never be
 * greeted by a measurement of their absence. DECISIONS.md C7.
 *
 * PULSE_ANSWERED, PLAN_ACCEPTED and SETTING_CHANGED stay in: the person did those.
 * A single setting change on its own still leaves a day below `quietDay`'s bar of
 * two events, so including it costs nothing and excluding it would be an
 * interpretation this predicate is not entitled to make. ITEM_FILED, ITEM_ESTIMATED
 * and FOCUS_EXTENDED stay in for the same reason: filing something, revising an
 * estimate and adding ten minutes to a running session are all gestures.
 *
 * **This predicate is a negation, so a new event type counts as activity by
 * default.** That is safe only while somebody is looking at it, which is why every
 * type added from here on is classified against it deliberately, in the same commit
 * that adds it, with the classification argued in a comment. APP_OPENED was one
 * line away from shipping the other way.
 *
 * There is deliberately one predicate here, not two. A narrower "what counts as
 * movement" test, which would also drop ITEM_REORDERED and AREA_RECOLORED, is a
 * Phase 5 question about Pulse language, and inventing it early would leave two
 * meanings of the word active in the codebase with nothing to tell them apart.
 */
val ClarityEventType.isUserActivity: Boolean
    get() = !isWrittenWithoutUserAct

/**
 * The four the app writes with no gesture behind them. The complement of
 * [isUserActivity].
 *
 * Named for what is true of all four rather than for who wrote them. Three are
 * authored by the engine; APP_OPENED is written by the app shell on launch and is
 * not engine authored at all. Folding it under the old name to get the right answer
 * would have recorded a false reason, and the reason is the part a later session
 * actually reads.
 */
val ClarityEventType.isWrittenWithoutUserAct: Boolean
    get() = this == ClarityEventType.PULSE_GENERATED ||
        this == ClarityEventType.REPORT_GENERATED ||
        this == ClarityEventType.PLAN_OFFERED ||
        this == ClarityEventType.APP_OPENED

/**
 * The Trail's pagination arithmetic. MASTER_BUILD_PROMPT 9.
 *
 * Pure and separate from the screen so the awkward cases are unit testable: a page
 * that spans a spring forward, a page that spans a fall back, and a page in a zone
 * whose offset is not a whole number of hours. All three are wrong under the
 * obvious implementation, which divides milliseconds by 86400000.
 */
object TrailPaging {

    /** MASTER_BUILD_PROMPT 9: "Pagination loads 14 days per page". */
    const val PAGE_DAYS = 14

    /**
     * The [days] long local day window whose last day contains [anchorMillis].
     *
     * The lower bound is local midnight of the first day, computed by stepping back
     * calendar dates rather than by subtracting a fixed number of milliseconds, so a
     * daylight saving shift inside the window does not lose or gain a day. The upper
     * bound is one millisecond past the anchor, because every window in this package
     * is exclusive on the upper end and the anchor event itself has to be inside its
     * own page.
     */
    fun pageEndingAt(anchorMillis: Long, zone: ZoneId, days: Int = PAGE_DAYS): TrailWindow {
        require(days >= 1) { "a page covers at least one day, not $days" }
        val lastDay = Instant.ofEpochMilli(anchorMillis).atZone(zone).toLocalDate()
        val firstDay = lastDay.minusDays((days - 1).toLong())
        return TrailWindow(
            fromMillis = firstDay.atStartOfDay(zone).toInstant().toEpochMilli(),
            toMillis = anchorMillis + 1,
        )
    }
}

/**
 * Every item id a page of Trail events needs a title for.
 *
 * This exists because one family does not follow the rule the rest of the log
 * follows. Every `ITEM_*` event is keyed by the item it is about, so the ids on a
 * page are enough to fetch the naming history for all of them. The four focus types
 * are keyed by their session instead, and the item they were run on is named only
 * inside the `FOCUS_STARTED` payload, which is itself only reachable by that session
 * id. So a page's own entity ids are not the set of items it needs to name, and a
 * loader that assumes they are renders "Finished 25 minutes of focus on" with nothing
 * after the preposition for any session whose item was added before the page began.
 *
 * FOCUS_EXTENDED joined that family and needs nothing new here: it is keyed by its
 * session like the two terminal types, so the same second round resolves it.
 * ITEM_FILED and ITEM_ESTIMATED are keyed by their item like every other `ITEM_*`
 * type, and are listed below because neither carries a title snapshot of its own,
 * which is precisely the case this set exists to cover.
 *
 * Stated here as a pure rule rather than inline in a loader, because the loader that
 * got this wrong could not be tested and this can.
 *
 * @param page the events being displayed.
 * @param focusStarts the `FOCUS_STARTED` events resolved for that page's sessions.
 */
fun itemIdsNeededBy(page: List<ClarityEvent>, focusStarts: List<ClarityEvent>): Set<String> {
    val direct = page.mapNotNull { event ->
        when (val payload = event.payload) {
            is ItemAdded -> payload.itemId
            is ItemFiled -> payload.itemId
            is ItemEdited -> payload.itemId
            is ItemEstimated -> payload.itemId
            is ItemQueued -> payload.itemId
            is ItemPromoted -> payload.itemId
            is ItemCompleted -> payload.itemId
            is ItemReopened -> payload.itemId
            is ItemReordered -> payload.itemId
            is ItemDeleted -> payload.itemId
            is FocusStarted -> payload.itemId
            else -> null
        }
    }
    val throughSessions = focusStarts.mapNotNull { (it.payload as? FocusStarted)?.itemId }
    return (direct + throughSessions).toSet()
}
