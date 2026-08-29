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
import com.kamsiob.claritynow.data.event.inTotalOrder
import com.kamsiob.claritynow.domain.replay.ClarityReducer
import com.kamsiob.claritynow.domain.replay.ClarityState
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * The only path from the event log to a displayed number. MASTER_BUILD_PROMPT 9.
 *
 * The caller decides the extent of [events]. The engine hands it the whole log; the
 * Trail screen hands it one page. Every function is a fold over exactly what it was
 * given and reads no clock, so the same call with the same list and the same bounds
 * answers the same on two devices and on the Linux build that will one day
 * reimplement this from the same specification.
 *
 * [zone] supplies calendar boundaries only. It is never used to ask what time it is.
 * A zone is not a clock: it converts an instant the caller already supplied into a
 * calendar date, and `activeDays`, `eventsPerDay` and the busiest day facts are
 * literally uncomputable without one. What nothing in this package may ever call is
 * `Instant.now`, `LocalDate.now`, `ZoneId.systemDefault` or `System.currentTimeMillis`.
 *
 * **Two conventions hold everywhere, and both are load bearing.**
 *
 * Every window is half open, `[startMillis, endMillis)`, and `atMillis`,
 * `asOfMillis` and `beforeMillis` mean strictly before that instant. So
 * `queueSizeAt(startOfSunday)` reads "the queue as Sunday began", which is what the
 * approved Report line "longer than they were on Sunday" is comparing against. The
 * three `AsOf` snapshot resolvers are the deliberate exception and say so on
 * themselves.
 *
 * Anything that folds state forward filters the log by `wallClock` and folds the
 * survivors in `(lamport, originId, id)` order. Sorting a fold by `wallClock`
 * produces wrong state on a merged log and only on a merged log, which is the one
 * case nobody ever checks by hand. `inTotalOrder()` in the constructor is half of
 * that guarantee; never breaking out of a wall clock filter early is the other half,
 * because a later event in the total order is allowed to carry an earlier wall clock.
 *
 * **Streak facts are deliberately absent.** CLARITY_LOGIC_ENGINE.md 3.1: "No streak
 * facts exist. Deliberately. No `currentStreak`, no `longestStreak`, no
 * `daysInARow`. Their absence makes it structurally impossible for streak language
 * to appear by accident. Do not add them." [activeDayKeys] makes a streak three
 * lines of work. Those three lines must never be written here.
 *
 * The two capped runs of **absence** that `HistoryFacts` now carries are a scoped
 * exception the owner approved, and they are computed in layer one out of the per
 * day counts below rather than here, so nothing on this facade answers how long
 * anything has been kept up. That is the shape the exception was granted in, and it
 * is the reason this paragraph still reads as an absolute. Read `HistoryFacts` for
 * what the exception covers and why a run of nothing cannot carry loss aversion.
 */
class TrailQueries(
    events: List<ClarityEvent>,
    private val zone: ZoneId,
) {

    /**
     * Sorted and deduplicated once, at construction.
     *
     * An unsorted list is the single most likely source of a silently wrong number,
     * and no caller can hand one to this class. Doing it here also costs one sort
     * per fact extraction rather than one per function, which matters when layer one
     * of the engine builds thirty facts in a single pass.
     */
    private val log: List<ClarityEvent> = events.inTotalOrder()

    /**
     * Every event grouped by the entity it is primarily about, still in total order.
     *
     * Keyed on `payload.primaryEntityId` rather than on the row's `entityId` column,
     * because the payload is the authority and the column is a denormalized copy of
     * it. Built lazily: a `TrailQueries` constructed only to count a window never
     * pays for it.
     */
    private val byEntity: Map<String, List<ClarityEvent>> by lazy {
        log.groupBy { it.payload.primaryEntityId }
    }

    /**
     * The most recent forward fold, kept so that asking three questions about one
     * instant costs one replay rather than three. Memoization changes no answer;
     * it is stored as a single reference so a concurrent reader sees either the
     * whole previous result or none of it, never half of it.
     */
    private var memo: Pair<Long, ClarityState>? = null

    /**
     * Every day the log says the app was opened, parsed once and sorted once.
     *
     * Held as calendar dates rather than as the raw keys so that the gap arithmetic
     * has no string in it. Keys that do not parse are dropped here, which is the
     * only place they could be: an imported file or a second implementation writing
     * against `docs/EVENT_FORMAT.md` can put a malformed key in the log, and one bad
     * marker must cost that marker and not the first screen a returning person sees.
     */
    private val openedDates: List<LocalDate> by lazy {
        log.asSequence()
            .mapNotNull { (it.payload as? AppOpened)?.dateKey }
            .mapNotNull(::parseDayKey)
            .distinct()
            .sorted()
            .toList()
    }

    // Completions -------------------------------------------------------------

    /** ITEM_COMPLETED events in `[startMillis, endMillis)`. */
    fun completionsBetween(startMillis: Long, endMillis: Long): Int =
        eventsIn(startMillis, endMillis).count { it.type == ClarityEventType.ITEM_COMPLETED }

    /** Completions in the window keyed by the area the payload names. */
    fun completionsPerArea(startMillis: Long, endMillis: Long): Map<String, Int> {
        val counts = HashMap<String, Int>()
        for (event in eventsIn(startMillis, endMillis)) {
            val payload = event.payload
            if (payload is ItemCompleted) counts[payload.areaId] = (counts[payload.areaId] ?: 0) + 1
        }
        return counts.toSortedMap()
    }

    /**
     * Every completion in the window with its snapshots, newest first.
     *
     * Newest first uses the display order, not the total order: a completion and the
     * promotion that followed it share one wall clock reading, and `lamport`
     * descending is what keeps a same instant pair in the order they happened.
     */
    fun completedItemsBetween(startMillis: Long, endMillis: Long): List<CompletedRecord> =
        eventsIn(startMillis, endMillis)
            .filter { it.payload is ItemCompleted }
            .sortedWith(TRAIL_DISPLAY_ORDER)
            .mapNotNull { event ->
                val payload = event.payload as? ItemCompleted ?: return@mapNotNull null
                CompletedRecord(
                    itemId = payload.itemId,
                    titleSnapshot = payload.titleSnapshot,
                    areaId = payload.areaId,
                    areaNameSnapshot = payload.areaNameSnapshot,
                    activeDurationDays = payload.activeDurationDays,
                    completedAt = event.wallClock,
                )
            }

    /** Completions bucketed by local calendar day. Days with none are absent. */
    fun completionsPerDay(startMillis: Long, endMillis: Long): Map<String, Int> {
        val counts = HashMap<String, Int>()
        for (event in eventsIn(startMillis, endMillis)) {
            if (event.type != ClarityEventType.ITEM_COMPLETED) continue
            val key = dateKeyOf(event.wallClock)
            counts[key] = (counts[key] ?: 0) + 1
        }
        return counts.toSortedMap()
    }

    // Events and areas --------------------------------------------------------

    /**
     * User activity events in the window keyed by resolved area.
     *
     * An event whose area cannot be resolved is absent from the map entirely rather
     * than bucketed under a placeholder key. A placeholder would eventually be
     * printed, and CLARITY_LOGIC_ENGINE.md 1 is blunt about what one fabricated area
     * name costs.
     *
     * Everything done to an unfiled item takes that path and is therefore absent
     * from every area's count, which is correct rather than a shortfall: it happened
     * in no area. It is still in [totalEvents] and still marks the day active.
     */
    fun eventsPerArea(startMillis: Long, endMillis: Long): Map<String, Int> {
        val counts = HashMap<String, Int>()
        for (event in eventsIn(startMillis, endMillis)) {
            if (!event.type.isUserActivity) continue
            val areaId = areaIdOf(event) ?: continue
            counts[areaId] = (counts[areaId] ?: 0) + 1
        }
        return counts.toSortedMap()
    }

    /** User activity events in the window. The denominator of every share. */
    fun totalEvents(startMillis: Long, endMillis: Long): Int =
        eventsIn(startMillis, endMillis).count { it.type.isUserActivity }

    /**
     * The newest event for that area strictly before [beforeMillis], or null when
     * the area has never been touched.
     *
     * A wall clock maximum rather than the last event in the total order, because
     * the question this answers is "how long ago", which is a calendar question.
     */
    fun lastEventForArea(areaId: String, beforeMillis: Long): Long? {
        var newest: Long? = null
        for (event in log) {
            if (event.wallClock >= beforeMillis) continue
            if (areaIdOf(event) != areaId) continue
            if (newest == null || event.wallClock > newest) newest = event.wallClock
        }
        return newest
    }

    /**
     * The area an event belongs to, or null for the types that have none.
     *
     * Sixteen of the twenty eight resolve straight out of their own payload. Five
     * resolve through another event: ITEM_EDITED and ITEM_ESTIMATED carry only an
     * item id and resolve through that item's own history, and the three focus
     * types that are not FOCUS_STARTED resolve through their session's
     * FOCUS_STARTED, which can genuinely be missing on an imported or merged log,
     * so the answer is nullable rather than a crash.
     *
     * **Resolving through an item is now time bounded, and it was not before.** It
     * used to be exact on the ground that an item's area is assigned once and no
     * event moves it between areas. ITEM_FILED breaks the first half of that: an
     * item can begin with no area and acquire one later. Resolving an edit made
     * while the item was unfiled against the area it was filed into a week later
     * would attribute an event to an area retroactively, which is the same class of
     * mistake as letting a rename rewrite an older Trail entry. So it resolves as
     * of the event's own instant, and an edit to an unfiled item belongs to no area,
     * which is the honest answer rather than a missing one.
     *
     * ITEM_ADDED also returns null now, for an unfiled capture. That is not a
     * failure to resolve. The item is in no area, and DECISIONS.md C8 chose that
     * over a synthetic inbox area precisely so this function has nothing to invent.
     *
     * The seven that cannot resolve are excluded rather than guessed. PULSE_GENERATED,
     * PULSE_ANSWERED and REPORT_GENERATED carry a `factSnapshot` whose keys no
     * document specifies, so nothing may be parsed out of it. PLAN_OFFERED carries a
     * `subjectId` but not the `SubjectKind` that would say whether it names an area
     * or an item; testing the id against the known area ids would work in practice
     * and is a heuristic no document authorizes. PLAN_ACCEPTED carries a plan id
     * with the same missing kind behind it, a setting is global by definition, and
     * APP_OPENED carries a date and nothing else.
     *
     * The Pulse payloads carry a subject and a kind since the Addendum 01 schema
     * commit, so the middle case above is answerable now and is deliberately still
     * not answered here. A Pulse is not something that happened in an area; it is
     * something the app said about one, and counting it in `eventsPerArea` would
     * mix the two. Anything wanting the subject reads the payload for it.
     */
    fun areaIdOf(event: ClarityEvent): String? = when (val payload = event.payload) {
        is AreaCreated -> payload.areaId
        is AreaRenamed -> payload.areaId
        is AreaRecolored -> payload.areaId
        is AreaReordered -> payload.areaId
        is AreaArchived -> payload.areaId
        is AreaUnarchived -> payload.areaId
        is AreaDeleted -> payload.areaId

        is ItemAdded -> payload.areaId
        is ItemFiled -> payload.areaId
        is ItemEdited -> areaIdOfItemAsOf(payload.itemId, event.wallClock)
        is ItemEstimated -> areaIdOfItemAsOf(payload.itemId, event.wallClock)
        is ItemQueued -> payload.areaId
        is ItemPromoted -> payload.areaId
        is ItemCompleted -> payload.areaId
        is ItemReopened -> payload.areaId
        is ItemReordered -> payload.areaId
        is ItemDeleted -> payload.areaId

        is FocusStarted -> payload.areaId
        is FocusCompleted -> areaIdOfFocusSession(payload.sessionId)
        is FocusEndedEarly -> areaIdOfFocusSession(payload.sessionId)
        is FocusExtended -> areaIdOfFocusSession(payload.sessionId)

        is PulseGenerated, is PulseAnswered, is ReportGenerated,
        is PlanOffered, is PlanAccepted, is SettingChanged, is AppOpened,
        -> null
    }

    /**
     * Areas created, not archived and not tombstoned strictly before the instant.
     *
     * CLARITY_LOGIC_ENGINE.md 1.1 prohibition 3 forbids ever referencing a deleted
     * entity, and archived areas are outside `AreaFacts` entirely, so a rollup that
     * counted either would print a number about things the person cannot see.
     */
    fun liveAreaIdsAt(atMillis: Long): Set<String> =
        stateBefore(atMillis).liveAreas.map { it.id }.toSet()

    /** The wall clock of AREA_CREATED, or null when no such area was ever created. */
    fun areaCreatedAt(areaId: String): Long? = areaCreatedEvent(areaId)?.wallClock

    /**
     * The newest AREA_ARCHIVED not superseded by a later AREA_UNARCHIVED, or null.
     *
     * Archive and unarchive after a delete are ignored, matching the reducer, which
     * refuses both on a tombstoned area and records a diagnostic instead.
     */
    fun areaArchivedAt(areaId: String, asOfMillis: Long): Long? {
        var archivedAt: Long? = null
        var deleted = false
        for (event in byEntity[areaId].orEmpty()) {
            if (event.wallClock >= asOfMillis) continue
            when (event.payload) {
                is AreaArchived -> if (!deleted) archivedAt = event.wallClock
                is AreaUnarchived -> if (!deleted) archivedAt = null
                is AreaDeleted -> deleted = true
                else -> Unit
            }
        }
        return archivedAt
    }

    /** The wall clock of AREA_DELETED. A tombstone, never a row delete. */
    fun areaDeletedAt(areaId: String, asOfMillis: Long): Long? =
        stateBefore(asOfMillis).areas[areaId]?.deletedAt

    // Snapshots as of an instant ----------------------------------------------

    /**
     * The area's name at [atMillis], not its name now.
     *
     * This and the two resolvers below are the reason renaming an area does not
     * rewrite what an older Trail entry says. They fold the log; they never consult
     * a live entity, which would return the current name for a renamed area and
     * nothing at all for a deleted one.
     *
     * **Inclusive of [atMillis], unlike every other bound in this class.** A
     * snapshot resolver is asked "what was this called when that happened", and one
     * commit stamps every event it writes with one clock reading, so an exclusive
     * bound would make an area unable to resolve its own name at the instant it was
     * created. Stated on all three, so the exception is visible wherever it is used.
     */
    fun areaNameAsOf(areaId: String, atMillis: Long): String? {
        var name: String? = null
        for (event in byEntity[areaId].orEmpty()) {
            if (event.wallClock > atMillis) continue
            when (val payload = event.payload) {
                is AreaCreated -> name = payload.name
                is AreaRenamed -> name = payload.newName
                else -> Unit
            }
        }
        return name
    }

    /** The area's color hex at [atMillis]. Inclusive of the instant, see [areaNameAsOf]. */
    fun areaColorHexAsOf(areaId: String, atMillis: Long): String? {
        var hex: String? = null
        for (event in byEntity[areaId].orEmpty()) {
            if (event.wallClock > atMillis) continue
            when (val payload = event.payload) {
                is AreaCreated -> hex = payload.colorHex
                is AreaRecolored -> hex = payload.newHex
                else -> Unit
            }
        }
        return hex
    }

    /** The item's title at [atMillis]. Inclusive of the instant, see [areaNameAsOf]. */
    fun itemTitleAsOf(itemId: String, atMillis: Long): String? {
        var title: String? = null
        for (event in byEntity[itemId].orEmpty()) {
            if (event.wallClock > atMillis) continue
            when (val payload = event.payload) {
                is ItemAdded -> title = payload.title
                is ItemEdited -> title = payload.newTitle
                else -> Unit
            }
        }
        return title
    }

    /**
     * The area the item was in at [atMillis], or null when it was in none.
     *
     * Two events assign an item's area and no third one exists: ITEM_ADDED, which
     * may name none, and ITEM_FILED, which is the only transition into one and only
     * ever applies to an item that had none. Nothing moves an item between areas
     * and there is no unfile, so folding those two is exact rather than an
     * approximation of a current value.
     *
     * **Time bounded, unlike the version that shipped in phase 3.** That one had no
     * bound because there was nothing to bound: every item had an area from the
     * instant it existed. An item can now be unfiled for a week and filed
     * afterward, and answering "which area" without saying "when" would attribute
     * everything done to it while it sat in the inbox to the area it eventually
     * landed in.
     *
     * **Inclusive of [atMillis], like the three `AsOf` snapshot resolvers above and
     * unlike every other bound in this class.** One commit stamps every event it
     * writes with one clock reading, so an exclusive bound would leave an item
     * unable to resolve its own area at the instant it was added or filed.
     */
    fun areaIdOfItemAsOf(itemId: String, atMillis: Long): String? {
        var areaId: String? = null
        for (event in byEntity[itemId].orEmpty()) {
            if (event.wallClock > atMillis) continue
            when (val payload = event.payload) {
                is ItemAdded -> areaId = payload.areaId
                is ItemFiled -> areaId = payload.areaId
                else -> Unit
            }
        }
        return areaId
    }

    /** The area a focus session ran in, from its own FOCUS_STARTED. */
    fun areaIdOfFocusSession(sessionId: String): String? = focusStartPayload(sessionId)?.areaId

    /** The item a focus session ran on, from its own FOCUS_STARTED. */
    fun itemIdOfFocusSession(sessionId: String): String? = focusStartPayload(sessionId)?.itemId

    // Days --------------------------------------------------------------------

    /**
     * User activity events bucketed by local calendar day.
     *
     * Bucketed by converting each instant to a date in [zone], never by dividing
     * milliseconds by the length of a day. The division is wrong across every
     * daylight saving boundary and in every zone whose offset is not a whole number
     * of hours, and it is wrong quietly.
     */
    fun eventsPerDay(startMillis: Long, endMillis: Long): Map<String, Int> {
        val counts = HashMap<String, Int>()
        for (event in eventsIn(startMillis, endMillis)) {
            if (!event.type.isUserActivity) continue
            val key = dateKeyOf(event.wallClock)
            counts[key] = (counts[key] ?: 0) + 1
        }
        return counts.toSortedMap()
    }

    /**
     * Days with at least one user activity event. Not days with a completion.
     *
     * The two are different quantities and must never be folded together: the Report
     * needs "{n} of seven days had activity" and the Pulse separately needs
     * "{dayCount} without a completion". A single number cannot serve both.
     */
    fun activeDays(startMillis: Long, endMillis: Long): Int =
        eventsPerDay(startMillis, endMillis).values.count { it > 0 }

    /** The active days themselves, for the fourteen dot grid. */
    fun activeDayKeys(startMillis: Long, endMillis: Long): Set<String> =
        eventsPerDay(startMillis, endMillis).filterValues { it > 0 }.keys

    /**
     * User activity events by local day, and inside each day by resolved area.
     *
     * The per day refinement of [eventsPerArea], answered in one pass because its
     * callers want the same fold at three grains: which areas moved in a week, how
     * many events one area had in each of twelve weeks, and whether every event of a
     * day belonged to a single area.
     *
     * **A day's per area counts can sum to less than [eventsPerDay] for that day**,
     * and the difference is not a defect to be reconciled. An event on an unfiled
     * item resolves to no area, exactly as [eventsPerArea] documents, so it is in the
     * day's total and in no area's count. A caller asking whether one area held a
     * whole day must compare against [eventsPerDay] rather than against the sum of
     * this map's inner values, or it will call a day single area when something
     * happened outside every area.
     *
     * Days with no user activity are absent, and so are areas with none inside a day
     * that has some. Both maps are sorted, so two devices iterate alike.
     */
    fun eventsPerAreaByDay(
        startMillis: Long,
        endMillis: Long,
    ): Map<String, Map<String, Int>> {
        val byDay = HashMap<String, HashMap<String, Int>>()
        for (event in eventsIn(startMillis, endMillis)) {
            if (!event.type.isUserActivity) continue
            val areaId = areaIdOf(event) ?: continue
            val key = dateKeyOf(event.wallClock)
            val counts = byDay.getOrPut(key) { HashMap() }
            counts[areaId] = (counts[areaId] ?: 0) + 1
        }
        return byDay.mapValues { (_, counts) -> counts.toSortedMap() }.toSortedMap()
    }

    // Presence ----------------------------------------------------------------
    //
    // The one section in this class that reads no wall clock at all, not even
    // through [zone]. Every answer below is folded from `AppOpened.dateKey`, the
    // string the device that was there computed in the zone it was in, and the
    // arithmetic is calendar dates from end to end. Two consequences, and both are
    // the reason MASTER_BUILD_PROMPT 14b.4 asks for it this way.
    //
    // A restored backup and a second device reach the same answer, because the
    // answer never passes through the reader's zone or the reader's clock. And a
    // person who opens the app at 23:00 and again at 00:30 fourteen days later has
    // been away fourteen days, which is what a calendar says and not what thirteen
    // and a bit times 86400000 milliseconds says. The obvious implementation, which
    // subtracts the two wall clocks, is wrong on that person, wrong across every
    // spring forward, and right on the author's own phone in August.
    //
    // Nothing here counts. `openedDayKeys` is a list of days somebody was present,
    // which is a measurement of an absence turned inside out, and 14b.4 forbids a
    // returning person from being greeted by one. It exists so a gap can be found
    // and so a test can see its own fixture. It never reaches a screen, the Trail
    // renders no row for the event, and section 9's day header does not count it.

    /**
     * Every local day the log holds an APP_OPENED for, ascending and distinct.
     *
     * Distinct because two devices holding one log may each have written the marker
     * for the same day, which is correct: each of them was opened, and every reader
     * of this event asks which days appear rather than how many rows a day has.
     * Ascending because these keys are ISO-8601, so string order is calendar order,
     * and the newest is therefore the last.
     */
    fun openedDayKeys(): List<String> = openedDates.map(LocalDate::toString)

    /**
     * True when the log already holds the presence marker for [dateKey].
     *
     * The pure statement of the rule `ClarityRepository.recordAppOpened` enforces on
     * the way in: at most once per calendar day. The repository answers the same
     * question with one seek of the `entityId` index rather than by folding the
     * whole log, because it runs on the first foreground and the log is the largest
     * table in the app. This is the form a test can drive, and the two agree by
     * construction: `AppOpened` uses its own date key as its entity id.
     */
    fun hasOpenedOn(dateKey: String): Boolean = dateKey in openedDayKeys()

    /**
     * The most recent return after a long absence, on or before [dateKey]. Null when
     * there has not been one. MASTER_BUILD_PROMPT 14b.4.
     *
     * A return is an open with an open before it and [ReEntry.MIN_GAP_DAYS] or more
     * calendar days between the two. Walked newest first, so a log with two
     * absences answers with the later one, which is the only one anything is still
     * suppressing for.
     *
     * **A first ever open is not a return, and this cannot be made to say otherwise.**
     * The oldest open in the log has nothing before it, the loop leaves rather than
     * substituting anything for the missing end, and [gapDays] has no overload that
     * accepts a missing one. The two shapes that mistake would take are both
     * plausible and both cost somebody something: a missing previous open defaulted
     * to the same day makes every return a gap of zero, so the state never appears
     * for anyone, and defaulted to the epoch makes every brand new user a person
     * coming back from an absence of decades.
     *
     * @param dateKey the day being asked about, `yyyy-MM-dd`. It comes from
     *   `ClarityClock.dateKey()` at the one call site that has a clock; a key this
     *   package cannot parse is a programming error rather than bad data, because
     *   unlike the payload keys it did not come from the log.
     */
    fun lastReEntryOnOrBefore(dateKey: String): ReEntry? {
        val asOf = requireNotNull(parseDayKey(dateKey)) {
            "a date key is yyyy-MM-dd, and this one is '$dateKey'"
        }
        val opens = openedDates.filter { !it.isAfter(asOf) }
        for (index in opens.indices.reversed()) {
            val previousOpen = opens.getOrNull(index - 1) ?: return null
            if (gapDays(previousOpen, opens[index]) >= ReEntry.MIN_GAP_DAYS) {
                return ReEntry(returnedOn = opens[index].toString())
            }
        }
        return null
    }

    /**
     * The return that [dateKey]'s own open triggers, or null on every other day.
     *
     * 14b.4 puts the app into the re-entry state "on the foreground that writes
     * today's APP_OPENED and only then, so it appears at most once per calendar day
     * and in practice once per gap". This is that question, and
     * [lastReEntryOnOrBefore] is the one the suppression windows ask for days
     * afterward. Both run the same arithmetic, once, so the day the screen appears
     * and the day the Report starts withholding can never be two different days.
     *
     * **The trap this restates from `ClarityRepository.recordAppOpened`.** The app
     * writes today's marker on the first foreground, so by the time any screen can
     * ask, the newest key in the log is already today. The absence is measured to
     * the newest open strictly before today, which the loop above gets right by
     * comparing consecutive pairs rather than by comparing anything to today.
     */
    fun reEntryOn(dateKey: String): ReEntry? =
        lastReEntryOnOrBefore(dateKey)?.takeIf { it.returnedOn == dateKey }

    // Focus -------------------------------------------------------------------

    /**
     * Seconds of focus in the window, over both kinds of ended session alike.
     *
     * Sessions ended early count. CLARITY_LOGIC_ENGINE.md 3.1 gives three separate
     * session counts and one undivided total, and it is not named `completedSeconds`.
     * The approved Report line "The sessions that finished averaged {minutes}
     * minutes" only means something if the ordinary minutes figure is unrestricted.
     * And MASTER_BUILD_PROMPT 10 treats an early ending neutrally everywhere, which
     * deleting those minutes from the total would quietly stop being true. Addendum
     * 01 4e says the same thing in the language the person sees: fourteen minutes is
     * fourteen minutes.
     *
     * A session is attributed entirely to the instant it started, so one that
     * crosses midnight belongs to the day it began and lands on exactly one day of a
     * heat strip. Attributing to the terminal event instead would split a session
     * across two windows and make the two halves of "{sessions} started, {n}
     * completed" disagree with each other.
     */
    fun focusSecondsTotal(startMillis: Long, endMillis: Long): Long {
        var total = 0L
        for (event in log) {
            val terminal = terminalFocusOf(event) ?: continue
            if (focusTerminalEvent(terminal.first)?.id != event.id) continue
            val start = focusStartEvent(terminal.first) ?: continue
            if (start.wallClock < startMillis || start.wallClock >= endMillis) continue
            total += terminal.second.toLong()
        }
        return total
    }

    /** [focusSecondsTotal] in whole minutes, rounded down. Rounding happens once, here. */
    fun focusMinutes(startMillis: Long, endMillis: Long): Int =
        (focusSecondsTotal(startMillis, endMillis) / 60L).toInt()

    /**
     * Session outcomes for the sessions that started in the window.
     *
     * A session with no terminal event is counted as unresolved, never as ended
     * early. A killed process leaves exactly that, and inferring an early ending
     * from `started - completed` would put a number behind language that is careful
     * never to blame. FOCUS_EXTENDED is not a terminal event and changes nothing
     * here: a session that was extended and then finished is one completed session.
     */
    fun focusSessionCounts(startMillis: Long, endMillis: Long): FocusCounts {
        var started = 0
        var completed = 0
        var endedEarly = 0
        var unresolved = 0
        for (event in eventsIn(startMillis, endMillis)) {
            val payload = event.payload
            if (payload !is FocusStarted) continue
            // A second start for a known session changes nothing in the reducer, so
            // it must not add a session here either.
            if (focusStartEvent(payload.sessionId)?.id != event.id) continue
            started++
            when (focusTerminalEvent(payload.sessionId)?.payload) {
                is FocusCompleted -> completed++
                is FocusEndedEarly -> endedEarly++
                else -> unresolved++
            }
        }
        return FocusCounts(
            started = started,
            completed = completed,
            endedEarly = endedEarly,
            unresolved = unresolved,
        )
    }

    // Queue and intake --------------------------------------------------------

    /**
     * The active item in each live area at the instant. At most one per area.
     *
     * An unfiled item is never an answer here. It cannot be ACTIVE at all until it
     * is filed, which `ClarityReducer.itemPromoted` refuses to produce and
     * `ClarityInvariants` reports if a merged log somehow contains it.
     */
    fun activeItemPerAreaAt(atMillis: Long): Map<String, String> {
        val state = stateBefore(atMillis)
        return state.liveAreas.mapNotNull { area ->
            state.activeItemIn(area.id)?.let { area.id to it.id }
        }.toMap()
    }

    /**
     * Queued items per area at the instant. The active one is not queued.
     *
     * ACTIVE, COMPLETED and DELETED are all excluded. The corpus settles it beyond
     * argument: "{areaName} is holding {n} items behind its active one", and an area
     * whose queue reads zero is still allowed to say "It still has an active item".
     *
     * Archived areas are excluded by default, because CLARITY_LOGIC_ENGINE.md 3.1
     * leaves them out of `AreaFacts` entirely and a rollup including them would count
     * things the person cannot see. Tombstoned areas are excluded unconditionally,
     * with no parameter to turn that off.
     *
     * Every qualifying area appears, including one whose queue is empty, because
     * "an area went from 3 or more queued to 0" is a trigger that needs the zero.
     *
     * Unfiled items are in no area's queue and so are in none of these counts and
     * not in [queueSizeAt] either. An inbox is not a backlog and must not be spoken
     * about as one: the whole point of Addendum 01 4a is that writing something down
     * costs nothing, and a queue length that grew every time somebody had a thought
     * would make it cost something after all.
     */
    fun queueSizeByAreaAt(atMillis: Long, includeArchived: Boolean = false): Map<String, Int> =
        queueSizesOf(stateBefore(atMillis), includeArchived).toSortedMap()

    /** The whole queue at the instant, summed over [queueSizeByAreaAt]. */
    fun queueSizeAt(atMillis: Long, includeArchived: Boolean = false): Int =
        queueSizeByAreaAt(atMillis, includeArchived).values.sum()

    /**
     * Every area's queue length as the window opened and after each event inside it.
     *
     * Entry zero of every list is that area's queue at [startMillis], the same instant
     * [queueSizeByAreaAt] answers and the same number `AreaFacts.queueLengthAtWindowStart`
     * carries. One entry follows for each event in the window, whether or not that event
     * touched a queue, so every list is the same length and index `i` names the same
     * moment in all of them. A caller comparing two areas therefore never has to carry an
     * instant around, and a caller reading one area sees its shape rather than its ends.
     *
     * **It exists because two boundaries cannot see a transition.** A queue that held five
     * as the window opened and holds nothing now, and a queue that held nothing, grew to
     * five on Tuesday and was worked down to nothing on Saturday, are the same pair of
     * numbers and two different weeks. `CORPUS_1_PULSE.md` 10 says `{areaName}'s queue
     * went from {n} to nothing` and `CORPUS_2_REPORT.md` 2.17 says `{areaName} cleared its
     * entire queue this week`, which is the second of those, so the fact those families
     * read is folded out of this series in layer one instead of subtracted from two ends.
     * There is no boundary pair anywhere that can approximate it, which is why this is a
     * function here rather than arithmetic there.
     *
     * An area is present with a zero at every moment before it existed and after it stopped
     * holding anything, so an area created inside the window has a full length list and no
     * caller has to reason about a shorter one. Which areas are counted at all follows
     * [queueSizeByAreaAt] exactly, archived areas included only on request and tombstoned
     * areas never, because a series that counted an area the person can no longer see would
     * feed a rollup that names areas out loud.
     *
     * The fold starts from the state at [startMillis] and applies the window's events in
     * total order, which is what [queueSizeByAreaAt] would answer at each of those instants,
     * computed once rather than once per event.
     */
    fun queueSizeSeriesByArea(
        startMillis: Long,
        endMillis: Long,
        includeArchived: Boolean = false,
    ): Map<String, List<Int>> {
        var state = stateBefore(startMillis)
        val samples = mutableListOf(queueSizesOf(state, includeArchived))
        for (event in eventsIn(startMillis, endMillis)) {
            state = ClarityReducer.apply(state, event)
            samples += queueSizesOf(state, includeArchived)
        }
        val areaIds = samples.flatMapTo(sortedSetOf<String>()) { it.keys }
        return areaIds.associateWith { areaId -> samples.map { it[areaId] ?: 0 } }
    }

    /**
     * Items added into an area in the window. Intake, not gestures.
     *
     * **An unfiled capture is not counted here, and it is counted by
     * [totalEvents].** The two questions are different and the split is deliberate.
     * `totalEvents` and `activeDays` ask what a person did, and writing something
     * down in the inbox is unambiguously something they did. This asks how much
     * arrived in the queues, which is what every rule reading it goes on to say
     * something about: the Pulse accumulation family escalates on additions minus
     * completions, and telling someone their queue is growing because they emptied
     * their head into an inbox would be a false sentence about their own data.
     * Nothing is hidden from the person by this; the capture is in the Trail like
     * anything else. Addendum 01 4a and DECISIONS.md C8.
     *
     * It also keeps this equal to the sum of [additionsPerArea], which a share
     * needs. A total larger than the parts it is divided into produces percentages
     * that do not reach a hundred, and nothing on the screen would explain why.
     *
     * There is no signed net flow function here and there never will be.
     * CLARITY_LOGIC_ENGINE.md 3.1 declares `netFlow` as completions minus additions
     * while the Pulse accumulation family escalates on additions minus completions.
     * Two opposite conventions, so each caller chooses its own sign at the point of
     * use and neither can leak into the other.
     */
    fun additionsBetween(startMillis: Long, endMillis: Long): Int =
        eventsIn(startMillis, endMillis).count {
            val payload = it.payload
            payload is ItemAdded && payload.areaId != null
        }

    /** Additions in the window keyed by the area the payload names. */
    fun additionsPerArea(startMillis: Long, endMillis: Long): Map<String, Int> {
        val counts = HashMap<String, Int>()
        for (event in eventsIn(startMillis, endMillis)) {
            val payload = event.payload
            if (payload !is ItemAdded) continue
            val areaId = payload.areaId ?: continue
            counts[areaId] = (counts[areaId] ?: 0) + 1
        }
        return counts.toSortedMap()
    }

    // Swaps and promotions ----------------------------------------------------

    /**
     * Every ITEM_PROMOTED in the window, including the automatic promotion that
     * follows a completion and the first item added to an empty area.
     */
    fun promotionsBetween(startMillis: Long, endMillis: Long): Int =
        eventsIn(startMillis, endMillis).count { it.type == ClarityEventType.ITEM_PROMOTED }

    /**
     * Promotions that displaced something, which is what a swap is.
     *
     * A swap is an ITEM_PROMOTED carrying a non null `demotedItemId`. That fact is
     * stated in no prose anywhere in the specification and lives only in the shape of
     * the payload, which is why it is encoded once here rather than rediscovered by
     * every later phase that needs it.
     */
    fun swapsBetween(startMillis: Long, endMillis: Long): Int =
        eventsIn(startMillis, endMillis).count {
            val payload = it.payload
            payload is ItemPromoted && payload.demotedItemId != null
        }

    /** Swaps in the window keyed by the area the payload names. */
    fun swapsPerArea(startMillis: Long, endMillis: Long): Map<String, Int> {
        val counts = HashMap<String, Int>()
        for (event in eventsIn(startMillis, endMillis)) {
            val payload = event.payload
            if (payload is ItemPromoted && payload.demotedItemId != null) {
                counts[payload.areaId] = (counts[payload.areaId] ?: 0) + 1
            }
        }
        return counts.toSortedMap()
    }

    // Item age. Two quantities, two names, deliberately ------------------------

    /**
     * When the item last became active, or null when it is not active at the instant.
     *
     * Measured from the most recent ITEM_PROMOTED, not from ITEM_ADDED, and cleared
     * by a later ITEM_QUEUED, by being demoted under someone else's promotion, and by
     * completion. CLARITY_LOGIC_ENGINE.md 7.3 names a promotion resetting the age as
     * the behavior escalation monotonicity exists to absorb downstream, which settles
     * that it is the intended reading rather than a defect: "a new active item starts
     * at stage 1".
     */
    fun activeSinceForItem(itemId: String, asOfMillis: Long): Long? =
        stateBefore(asOfMillis).items[itemId]?.activeSince

    /**
     * Whole local days the item has been active, or null when it is not.
     *
     * **No fallback to when the item was added.** `ClarityRepository` has one,
     * unreachable there because adding an item into an empty area writes the add and
     * the promotion in a single commit. Copying it here would silently turn a never
     * promoted item into an add based age, which is the wrong number with no visible
     * symptom. A null answer is the honest one.
     */
    fun daysActiveForItem(itemId: String, asOfMillis: Long): Int? {
        val since = activeSinceForItem(itemId, asOfMillis) ?: return null
        return wholeDaysBetween(since, asOfMillis)
    }

    /** The wall clock of ITEM_ADDED. */
    fun itemAddedAt(itemId: String): Long? = itemAddedEvent(itemId)?.wallClock

    /**
     * Whole local days since the item was added.
     *
     * How long it has been waiting, which is a different quantity from how long it
     * has been active. Three approved Report lines need this one and not the other,
     * and one function serving both meanings would be silently wrong in one of them.
     */
    fun daysSinceItemAdded(itemId: String, asOfMillis: Long): Int? {
        val added = itemAddedAt(itemId) ?: return null
        return wholeDaysBetween(added, asOfMillis)
    }

    // Rows --------------------------------------------------------------------

    /**
     * The snapshot values a row needs that its own payload does not carry.
     *
     * Resolved at the event's own instant, so a later rename or recolor cannot reach
     * back into it.
     */
    fun contextFor(event: ClarityEvent): TrailRowContext {
        val areaId = areaIdOf(event)
        val itemId = subjectItemIdOf(event)
        return TrailRowContext(
            areaId = areaId,
            areaColorHex = areaId?.let { areaColorHexAsOf(it, event.wallClock) },
            itemTitle = itemId?.let { itemTitleAsOf(it, event.wallClock) },
            areaName = areaId?.let { areaNameAsOf(it, event.wallClock) },
        )
    }

    /**
     * One page of events as rows, in display order, with the timestamp clusters
     * already decided.
     *
     * The screen calls this and gets everything it needs. It is never handed a
     * `ClarityState`, which is what makes the live entity shortcut unavailable to it
     * rather than merely discouraged.
     */
    fun rows(events: List<ClarityEvent>): List<TrailRow> =
        trailRows(events, zone, this::contextFor)


    // Layer one additions -----------------------------------------------------
    //
    // Everything below exists because CLARITY_LOGIC_ENGINE.md 3.1 to 3.7 declares a
    // fact that no function above could answer. MASTER_BUILD_PROMPT 9 is absolute
    // that "every Report and Pulse claim traces to one of these" and that "there is
    // no other path to a displayed number", so the alternative to adding them was
    // the engine counting events itself, which is the second path that section
    // forbids.

    /**
     * The zone every date in this class is resolved against.
     *
     * Handed out so that `FactExtractor`, which has to bucket instants into local
     * days, weekdays and bands of the day, uses the same zone this class used to
     * count them. The alternative is passing a zone into the extractor beside the
     * facade, and two zones that are meant to be equal and are not produce a set of
     * facts that disagree with each other by one day, silently, at a boundary.
     */
    fun zone(): ZoneId = zone

    /**
     * The wall clock of the oldest event in the log, or null when the log is empty.
     *
     * The install instant, in the only sense the app can know one: there is no
     * INSTALL event and there must not be, because a restored backup would carry the
     * old one and a fresh log on a second device would carry a new one. The oldest
     * event answers the question every caller actually asks, which is how far back
     * the record goes.
     *
     * A wall clock minimum rather than the first event in the total order. The two
     * differ on a merged log, and "how long has this person been using the app" is a
     * calendar question.
     */
    fun firstEventAt(): Long? = log.minOfOrNull { it.wallClock }

    /**
     * Items that exist and are not tombstoned strictly before the instant.
     *
     * The item side of [liveAreaIdsAt], and the reason is validator check 2 in
     * CLARITY_LOGIC_ENGINE.md 8: an id that reaches a sentence must resolve and must
     * not be a tombstone. A completed item is live; a deleted one is not. Deleting
     * something and then reading about it in a report a week later is the
     * resurrection failure in 13, and this is what layer one filters against.
     */
    fun liveItemIdsAt(atMillis: Long): Set<String> =
        stateBefore(atMillis).items.values
            .filter { it.deletedAt == null }
            .map { it.id }
            .toSet()

    /**
     * The earliest wall clock of an event in that area inside the window, or null.
     *
     * The mirror of [lastEventForArea], and it exists for one fact: an area is a
     * revival rather than an ordinary week only if the gap between its previous
     * event and its first event *in this window* is long enough. Measuring the gap
     * to the window start instead would call an area dormant for a reason that is an
     * artifact of where the window boundary fell.
     */
    fun firstEventForArea(areaId: String, startMillis: Long, endMillis: Long): Long? =
        eventsIn(startMillis, endMillis)
            .filter { areaIdOf(it) == areaId }
            .minOfOrNull { it.wallClock }

    /** Items tombstoned in the window. Areas deleted are a different quantity and not counted. */
    fun deletionsBetween(startMillis: Long, endMillis: Long): Int =
        eventsIn(startMillis, endMillis).count { it.type == ClarityEventType.ITEM_DELETED }

    /**
     * Areas archived in the window, counted as events rather than as areas.
     *
     * An area archived, unarchived and archived again inside one window counts twice,
     * which is correct for the only thing that reads this: the first ever archive
     * flag, which asks whether the act happened here for the first time.
     */
    fun areaArchivesBetween(startMillis: Long, endMillis: Long): Int =
        eventsIn(startMillis, endMillis).count { it.type == ClarityEventType.AREA_ARCHIVED }

    /**
     * Focus seconds in the window keyed by the area the session ran in.
     *
     * Attributed to the FOCUS_STARTED instant and to that session's own area,
     * exactly as [focusSecondsTotal] is, so the per area parts sum to the whole.
     * A session whose FOCUS_STARTED is missing from a merged log is in neither.
     */
    fun focusSecondsPerArea(startMillis: Long, endMillis: Long): Map<String, Long> {
        val seconds = HashMap<String, Long>()
        for (event in log) {
            val terminal = terminalFocusOf(event) ?: continue
            if (focusTerminalEvent(terminal.first)?.id != event.id) continue
            val start = focusStartEvent(terminal.first) ?: continue
            if (start.wallClock < startMillis || start.wallClock >= endMillis) continue
            val areaId = (start.payload as? FocusStarted)?.areaId ?: continue
            seconds[areaId] = (seconds[areaId] ?: 0L) + terminal.second.toLong()
        }
        return seconds.toSortedMap()
    }

    /** Sessions that started in the window, keyed by area. Outcome is not considered. */
    fun focusSessionsPerArea(startMillis: Long, endMillis: Long): Map<String, Int> {
        val counts = HashMap<String, Int>()
        for (event in eventsIn(startMillis, endMillis)) {
            val payload = event.payload
            if (payload !is FocusStarted) continue
            if (focusStartEvent(payload.sessionId)?.id != event.id) continue
            counts[payload.areaId] = (counts[payload.areaId] ?: 0) + 1
        }
        return counts.toSortedMap()
    }

    /**
     * Focus seconds bucketed by the local day the session started on.
     *
     * One pass over the log rather than one windowed call per week, which is what a
     * personal best over a year of history would otherwise cost.
     */
    fun focusSecondsPerDay(startMillis: Long, endMillis: Long): Map<String, Long> {
        val seconds = HashMap<String, Long>()
        for (event in log) {
            val terminal = terminalFocusOf(event) ?: continue
            if (focusTerminalEvent(terminal.first)?.id != event.id) continue
            val start = focusStartEvent(terminal.first) ?: continue
            if (start.wallClock < startMillis || start.wallClock >= endMillis) continue
            val key = dateKeyOf(start.wallClock)
            seconds[key] = (seconds[key] ?: 0L) + terminal.second.toLong()
        }
        return seconds.toSortedMap()
    }

    /** Sessions that started in the window, bucketed by the local day they started on. */
    fun focusStartsPerDay(startMillis: Long, endMillis: Long): Map<String, Int> {
        val counts = HashMap<String, Int>()
        for (event in eventsIn(startMillis, endMillis)) {
            val payload = event.payload
            if (payload !is FocusStarted) continue
            if (focusStartEvent(payload.sessionId)?.id != event.id) continue
            val key = dateKeyOf(event.wallClock)
            counts[key] = (counts[key] ?: 0) + 1
        }
        return counts.toSortedMap()
    }

    /**
     * User activity events in the window bucketed by local hour, 0 to 23.
     *
     * The hour rather than a named band, deliberately. A band of the day is an engine
     * concept, declared as `PartOfDay` in CLARITY_LOGIC_ENGINE.md 2.1 with boundaries
     * the design system already fixed, and a facade that knew where the boundaries
     * fell would be a second place they are written down. This returns the raw shape
     * of the day and the engine names it.
     *
     * Resolved through the zone, never by dividing milliseconds, so an hour is the
     * hour the person saw on their own clock.
     */
    fun eventsPerHourOfDay(startMillis: Long, endMillis: Long): Map<Int, Int> =
        hoursOf(eventsIn(startMillis, endMillis).filter { it.type.isUserActivity })

    /** Items added into an area in the window, bucketed by local hour. See [eventsPerHourOfDay]. */
    fun additionsPerHourOfDay(startMillis: Long, endMillis: Long): Map<Int, Int> =
        hoursOf(
            eventsIn(startMillis, endMillis).filter {
                val payload = it.payload
                payload is ItemAdded && payload.areaId != null
            },
        )

    /** Sessions that started in the window, bucketed by local hour. See [eventsPerHourOfDay]. */
    fun focusStartsPerHourOfDay(startMillis: Long, endMillis: Long): Map<Int, Int> =
        hoursOf(
            eventsIn(startMillis, endMillis).filter {
                val payload = it.payload
                payload is FocusStarted && focusStartEvent(payload.sessionId)?.id == it.id
            },
        )

    /**
     * Every completion in the window that had a prediction behind it, as ratios.
     * MASTER_BUILD_PROMPT 14b.8, Addendum 01 7a.
     *
     * One entry per item completed inside the window that was active when it was
     * completed and carried an estimate when it became active. The count of the list
     * is the floor 14b.8 sets at five, and it is a count rather than a length nobody
     * can see: layer one carries it as a fact and the validator re-reads it.
     *
     * **The two magnitudes are divided here and are not returned.** Read
     * [EstimateOutcome] for why that is the shape rather than a convenience. Nothing
     * above this line receives a number of minutes, estimated or actual, so the delta
     * 14b.8 forbids cannot be formed by anything downstream, in one subtraction or in
     * any number of them.
     *
     * **The prediction is the estimate in force when the item became active**, and a
     * revision made afterward is ignored. An estimate changed while the work is under
     * way is not a prediction any more; it is a progress report, informed by exactly
     * the thing the calibration is trying to measure. Honoring it would move every
     * ratio toward one and the fact would quietly flatter the person it is about. A
     * revision made **before** the promotion is a better prediction and is the one
     * used, which is why this reads the estimate at the promotion rather than the
     * estimate on ITEM_ADDED.
     *
     * **The actual is the elapsed time of the item's last active spell**, from the
     * ITEM_PROMOTED that started it to the ITEM_COMPLETED that ended it, which is the
     * same spell `ItemCompleted.activeDurationDays` reports and the same one
     * `activeSinceForItem` answers about. An item completed without ever having been
     * promoted has no spell and is absent: there is a moment it was added and no
     * moment it was started, and how long something waited is not how long it took.
     * That is the distinction [daysActiveForItem] and [daysSinceItemAdded] already
     * keep apart, and folding it here would make the ratio mean two things.
     *
     * Elapsed time is a difference of two wall clocks in milliseconds, which is the
     * one place in this class that is correct: a duration inside a day is not a
     * calendar question, and a daylight saving shift inside a spell really did add or
     * remove an hour of the person's life. A completion whose wall clock precedes its
     * own promotion is two devices disagreeing about the time rather than a fast
     * finish, and it is **dropped rather than clamped**, because a clamp would enter
     * the sample as a ratio of zero and read as somebody who beat every estimate.
     *
     * The item's own events are read in total order rather than by wall clock, so a
     * merged log resolves the promotion and the estimate the same way on both
     * devices. Ordered oldest completion first, by the log's own order.
     */
    fun estimateOutcomes(startMillis: Long, endMillis: Long): List<EstimateOutcome> {
        val outcomes = ArrayList<EstimateOutcome>()
        for (event in eventsIn(startMillis, endMillis)) {
            val payload = event.payload
            if (payload !is ItemCompleted) continue
            val history = byEntity[payload.itemId] ?: continue
            val completedAt = history.indexOfFirst { it.id == event.id }
            if (completedAt < 0) continue
            val promotedAt = promotionBehind(history, completedAt) ?: continue
            val estimate = estimateInForceAt(history, promotedAt) ?: continue
            val elapsed = event.wallClock - history[promotedAt].wallClock
            if (elapsed < 0) continue
            outcomes += EstimateOutcome(
                itemId = payload.itemId,
                activeToEstimate = (elapsed / MILLIS_PER_MINUTE) / estimate,
            )
        }
        return outcomes
    }

    /**
     * The index of the promotion that began the spell ending at [completedAt].
     *
     * Walked backwards through the item's own events and stopped by an earlier
     * completion, so an item finished, reopened and finished again is measured over
     * its second spell rather than over both of them and the gap between. Null when
     * the item was never promoted, which is an item that was never active.
     */
    private fun promotionBehind(history: List<ClarityEvent>, completedAt: Int): Int? {
        for (index in completedAt - 1 downTo 0) {
            when (history[index].payload) {
                is ItemPromoted -> return index
                is ItemCompleted -> return null
                else -> Unit
            }
        }
        return null
    }

    /**
     * The estimate the item carried immediately before [promotedAt], or null.
     *
     * Null covers three shapes that are one shape to this function: the item was
     * captured without an estimate and never given one, the estimate it was given was
     * cleared again, or the only estimate it has was set after it became active.
     * Nothing predicted anything in any of them, so there is nothing to calibrate.
     */
    private fun estimateInForceAt(history: List<ClarityEvent>, promotedAt: Int): Double? {
        for (index in promotedAt - 1 downTo 0) {
            when (val payload = history[index].payload) {
                is ItemEstimated -> return payload.newEstimateMinutes?.takeIf { it > 0 }?.toDouble()
                is ItemAdded -> return payload.estimateMinutes?.takeIf { it > 0 }?.toDouble()
                else -> Unit
            }
        }
        return null
    }

    // The engine's own record of what it has already said ---------------------
    //
    // CLARITY_LOGIC_ENGINE.md 7.6: "FiringHistory is derived entirely from
    // PULSE_GENERATED, REPORT_GENERATED and PLAN_OFFERED events. Never from
    // DataStore." A device that has just merged a log must compute the same next
    // variant as the device that produced it, and DataStore does not merge. These
    // three functions are the only path to that history, and there is no writer
    // anywhere that could produce a fourth.
    //
    // They hand back the payloads rather than a re-wrapped record. The payloads are
    // already the shape the engine needs, because issue #19 shaped them for exactly
    // this: subjectId and subjectKind on the Pulse pair, and familyKey, variantKey,
    // escalationStage, register and subject on every report section.

    /** PULSE_GENERATED payloads in the window, in the log's total order. */
    fun pulsesGeneratedBetween(startMillis: Long, endMillis: Long): List<PulseGenerated> =
        eventsIn(startMillis, endMillis).mapNotNull { it.payload as? PulseGenerated }

    /** PULSE_ANSWERED payloads in the window, in the log's total order. */
    fun pulseAnswersBetween(startMillis: Long, endMillis: Long): List<PulseAnswered> =
        eventsIn(startMillis, endMillis).mapNotNull { it.payload as? PulseAnswered }

    /** REPORT_GENERATED payloads in the window, in the log's total order. */
    fun reportsGeneratedBetween(startMillis: Long, endMillis: Long): List<ReportGenerated> =
        eventsIn(startMillis, endMillis).mapNotNull { it.payload as? ReportGenerated }

    /** PLAN_OFFERED payloads in the window, in the log's total order. */
    fun plansOfferedBetween(startMillis: Long, endMillis: Long): List<PlanOffered> =
        eventsIn(startMillis, endMillis).mapNotNull { it.payload as? PlanOffered }

    /**
     * PLAN_ACCEPTED payloads in the window, in the log's total order.
     *
     * The sibling of the one above, and `PlanHistory` needs both: an offer carries
     * everything about a plan and nothing about whether it was taken up, and this
     * carries an id and nothing else. **There is no third function here and there
     * must not be**, because there is no third event: 10.5 says declining writes
     * nothing and ignoring both options is identical to declining, so an offer with
     * no acceptance beside it is the whole of what a decline looks like in the log.
     */
    fun plansAcceptedBetween(startMillis: Long, endMillis: Long): List<PlanAccepted> =
        eventsIn(startMillis, endMillis).mapNotNull { it.payload as? PlanAccepted }

    // Internals ---------------------------------------------------------------

    /** The half open window filter. Every windowed function starts here. */
    private fun eventsIn(startMillis: Long, endMillis: Long): List<ClarityEvent> =
        log.filter { it.wallClock >= startMillis && it.wallClock < endMillis }

    /**
     * The state as it stood strictly before [atMillis].
     *
     * Folded through `ClarityReducer` rather than through a second implementation of
     * the same rules. A queue length or an active item computed by a private copy of
     * the promotion, demotion and cascade logic would agree with the reducer on the
     * cases anyone writes a test for and diverge on the conflict path, which is
     * exactly the path that only appears after a merge. There is one definition of
     * what the log means and this reads it.
     *
     * Note the filter rather than a `takeWhile`: `log` is in total order, and a later
     * event in that order is allowed to carry an earlier wall clock, so stopping at
     * the first event past the bound would drop real history.
     */
    private fun stateBefore(atMillis: Long): ClarityState {
        memo?.let { (at, cached) -> if (at == atMillis) return cached }
        var state = ClarityState.EMPTY
        for (event in log) {
            if (event.wallClock < atMillis) state = ClarityReducer.apply(state, event)
        }
        memo = atMillis to state
        return state
    }

    /**
     * The queue length of every area the state holds, unsorted.
     *
     * One definition, read by the instant query and by the series, so the two can never
     * disagree about what counts as queued or about which areas are visible.
     */
    private fun queueSizesOf(state: ClarityState, includeArchived: Boolean): Map<String, Int> {
        val areas = if (includeArchived) {
            state.areas.values.filter { it.deletedAt == null }
        } else {
            state.liveAreas
        }
        return areas.associate { it.id to state.queueIn(it.id).size }
    }

    private fun areaCreatedEvent(areaId: String): ClarityEvent? =
        byEntity[areaId]?.firstOrNull { it.payload is AreaCreated }

    private fun itemAddedEvent(itemId: String): ClarityEvent? =
        byEntity[itemId]?.firstOrNull { it.payload is ItemAdded }

    private fun focusStartEvent(sessionId: String): ClarityEvent? =
        byEntity[sessionId]?.firstOrNull { it.payload is FocusStarted }

    private fun focusStartPayload(sessionId: String): FocusStarted? =
        focusStartEvent(sessionId)?.payload as? FocusStarted

    /**
     * The first terminal event for a session. A second one changes nothing.
     *
     * FOCUS_EXTENDED is not terminal and is not looked for here. It moves the plan,
     * not the outcome, and a session can carry several of them.
     */
    private fun focusTerminalEvent(sessionId: String): ClarityEvent? =
        byEntity[sessionId]?.firstOrNull {
            it.payload is FocusCompleted || it.payload is FocusEndedEarly
        }

    /** The session and the seconds of a terminal focus event, or null for anything else. */
    private fun terminalFocusOf(event: ClarityEvent): Pair<String, Int>? =
        when (val payload = event.payload) {
            is FocusCompleted -> payload.sessionId to payload.actualSeconds
            is FocusEndedEarly -> payload.sessionId to payload.actualSeconds
            else -> null
        }

    /** The item a row is about, for the types whose payload carries no title. */
    private fun subjectItemIdOf(event: ClarityEvent): String? = when (val payload = event.payload) {
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
        is FocusCompleted -> itemIdOfFocusSession(payload.sessionId)
        is FocusEndedEarly -> itemIdOfFocusSession(payload.sessionId)
        is FocusExtended -> itemIdOfFocusSession(payload.sessionId)
        else -> null
    }

    /**
     * The absence between two consecutive presence markers, in calendar days.
     *
     * **Both ends are required, and that is the whole design of this function.**
     * There is no overload taking a nullable previous open and no default for a
     * missing one, so the first ever open cannot reach the arithmetic at all. See
     * [lastReEntryOnOrBefore] for what the two available defaults would each cost.
     *
     * Counted between calendar dates, never by subtracting the two wall clocks, so
     * the answer does not depend on the time of day either open happened at and
     * does not move when the clocks do.
     */
    private fun gapDays(previousOpen: LocalDate, thisOpen: LocalDate): Int =
        ChronoUnit.DAYS.between(previousOpen, thisOpen).toInt()

    private fun localDateOf(atMillis: Long): LocalDate =
        Instant.ofEpochMilli(atMillis).atZone(zone).toLocalDate()

    /**
     * The local hour of each event, counted. Resolved through the zone rather than
     * by arithmetic on the epoch, so the hour is the one the person's own clock read.
     */
    private fun hoursOf(events: List<ClarityEvent>): Map<Int, Int> {
        val counts = HashMap<Int, Int>()
        for (event in events) {
            val hour = Instant.ofEpochMilli(event.wallClock).atZone(zone).hour
            counts[hour] = (counts[hour] ?: 0) + 1
        }
        return counts.toSortedMap()
    }

    /**
     * The `yyyy-MM-dd` key every daily thing is stored under, matching
     * `ClarityClock.dateKey`.
     *
     * Formatted with the ISO formatter rather than a pattern, because a pattern
     * resolves its digits against the default locale, and a default locale is
     * ambient environment of exactly the kind this package refuses to read.
     */
    private fun dateKeyOf(atMillis: Long): String =
        DateTimeFormatter.ISO_LOCAL_DATE.format(localDateOf(atMillis))

    /**
     * Whole local days between two instants, counted by calendar date rather than by
     * dividing milliseconds, so a daylight saving shift does not lose or gain a day.
     */
    private fun wholeDaysBetween(fromMillis: Long, toMillis: Long): Int =
        ChronoUnit.DAYS.between(localDateOf(fromMillis), localDateOf(toMillis))
            .toInt()
            .coerceAtLeast(0)

    private companion object {

        /**
         * The one duration in this class measured in milliseconds rather than in
         * calendar days.
         *
         * [estimateOutcomes] compares a span of a few minutes or a few days against a
         * number of minutes somebody typed, and a calendar has nothing to say about
         * either. Every other duration here is whole local days and is counted between
         * dates for the reason the class note gives.
         */
        const val MILLIS_PER_MINUTE = 60_000.0
    }
}
