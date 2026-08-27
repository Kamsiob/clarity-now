package com.kamsiob.claritynow.ui.trail

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kamsiob.claritynow.data.repo.ClarityRepository
import com.kamsiob.claritynow.domain.ClarityClock
import com.kamsiob.claritynow.domain.localDate
import com.kamsiob.claritynow.domain.query.TrailPaging
import com.kamsiob.claritynow.domain.query.TrailQueries
import com.kamsiob.claritynow.domain.query.TrailRow
import com.kamsiob.claritynow.domain.query.TrailWindow
import com.kamsiob.claritynow.domain.query.itemIdsNeededBy
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** One filter chip. Live area identity, which is what a filter is a filter over. */
@Immutable
data class TrailAreaChip(
    val areaId: String,
    val name: String,
    val colorHex: String,
)

/**
 * One day group, newest first inside it. design-v3.md 11.
 *
 * The count is the number of rows actually rendered under the header, which with a
 * filter active is the number for that area. It is deliberately not
 * `TrailQueries.totalEvents`, which excludes the three types the engine writes on
 * its own: both numbers are true and they can differ by one on a day the engine
 * spoke, so only one of them may ever appear on this screen.
 */
@Immutable
data class TrailDayModel(
    val date: LocalDate,
    val rows: List<TrailRow>,
) {
    val count: Int get() = rows.size
}

/**
 * Everything the Trail draws. No `ClarityState` reaches the screen through it.
 *
 * That absence is structural rather than tidy. The tempting way to render a row is
 * `state.areas[areaId]?.name`, which produces a correct looking screen that rewrites
 * its own history the first time anybody renames an area and shows nothing at all
 * for a deleted one. Every name and color on a row arrives inside [TrailRow],
 * resolved by folding the log to the instant of the event, and there is no field
 * here that could hold a live one. [areas] is the exception that proves it: those
 * are the filter chips, which are a control over what exists now.
 */
@Immutable
data class TrailUiState(
    val today: LocalDate,
    val zone: ZoneId,
    val loading: Boolean = true,
    val appending: Boolean = false,
    val logIsEmpty: Boolean = false,
    val endOfHistory: Boolean = false,
    val days: List<TrailDayModel> = emptyList(),
    val areas: List<TrailAreaChip> = emptyList(),
    val selectedAreaId: String? = null,
) {
    /** The empty state belongs to an empty log, never to a filter that found nothing. */
    val isEmpty: Boolean get() = !loading && logIsEmpty

    /**
     * The year a day header may leave off, which is the year of the newest thing on
     * screen rather than the current year. A person reading a page from last March
     * should not have every header stamped with a year, and a person whose newest
     * event is from last year should not read `Monday, August 24` and assume today.
     */
    val referenceYear: Int get() = days.firstOrNull()?.date?.year ?: today.year
}

/**
 * The paging state, in one object rather than in six flows.
 *
 * A page load moves several of these at once and they have to move together: a page
 * that arrives sets the rows, the new oldest bound and both loading flags in the
 * same write, or the affordance briefly sees rows with a stale bound and asks for
 * the same page twice.
 */
private data class TrailLoad(
    val loading: Boolean = true,
    val appending: Boolean = false,
    val logIsEmpty: Boolean = false,
    val endOfHistory: Boolean = false,
    val days: List<TrailDayModel> = emptyList(),
    /** The lower bound of the oldest page loaded. The next page anchors before it. */
    val oldestFrom: Long? = null,
)

/**
 * The Trail's paging, filtering and day grouping. MASTER_BUILD_PROMPT 9.
 *
 * ViewModels never touch a DAO, so every read here goes through the six bounded
 * repository methods on the Trail path, and none of them can select the whole table.
 *
 * **This object outlives the tab.** `viewModel()` inside the shell's `AnimatedContent`
 * resolves against the Activity, so this is created once and survives every tab
 * switch until the Activity dies. Paging depth and the selected filter persist,
 * which is what a person expects when they glance at Areas and come back. The
 * consequence is that nothing is refreshed by arriving: new events are picked up by
 * watching `repository.state`, which is the only thing that changes while the Trail
 * is off screen.
 */
class TrailViewModel(
    private val repository: ClarityRepository,
    private val clock: ClarityClock,
) : ViewModel() {

    private val load = MutableStateFlow(TrailLoad())
    private val selected = MutableStateFlow<String?>(null)

    /**
     * One page load at a time. A filter change and a state change can both ask for a
     * reload while a page is in flight, and without this the second one appends its
     * days to a list the first one is about to replace.
     */
    private val pageLock = Mutex()

    /**
     * The local date, re-emitted at every local midnight while anything is collecting.
     *
     * Nothing else in this app emits when nobody touches it, so without a tick of its
     * own a Trail left open across midnight goes on calling yesterday "Today" until
     * the next commit or tab switch happens to rebuild the state. The wait is computed
     * against the zone rather than as a fixed 24 hours, so a DST boundary lands on the
     * real midnight rather than an hour off it.
     */
    private val today: Flow<LocalDate> = flow {
        while (true) {
            val now = clock.nowMillis()
            val date = clock.localDate(now)
            emit(date)
            val nextMidnight = date.plusDays(1).atStartOfDay(clock.zone()).toInstant().toEpochMilli()
            delay((nextMidnight - now).coerceAtLeast(MIN_MIDNIGHT_WAIT_MILLIS))
        }
    }

    val uiState: StateFlow<TrailUiState> =
        combine(load, selected, repository.state, today) { current, filter, state, date ->
            TrailUiState(
                today = date,
                zone = clock.zone(),
                loading = current.loading,
                appending = current.appending,
                logIsEmpty = current.logIsEmpty,
                endOfHistory = current.endOfHistory,
                days = current.days,
                areas = state.liveAreas.map {
                    TrailAreaChip(areaId = it.id, name = it.name, colorHex = it.colorHex)
                },
                selectedAreaId = filter,
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            TrailUiState(today = clock.localDate(), zone = clock.zone()),
        )

    init {
        viewModelScope.launch {
            repository.load()
            // The first emission is the loaded state, so this is also the first page
            // load. Every later one is a commit that happened while the person was
            // somewhere else in the app, because nothing on this screen writes.
            // StateFlow conflates, so a burst of commits costs one reload, not ten.
            repository.state
                .map { it.lastLamport }
                .distinctUntilChanged()
                .collect {
                    dropFilterOnVanishedArea()
                    reload()
                }
        }
    }

    /**
     * Filter to one area, or to all of them with null.
     *
     * A filter change reloads from the newest event rather than filtering the pages
     * already in memory. Filtering in place would keep an unbounded slice of the log
     * resident just to answer a question the log itself answers in one indexed query,
     * and it would leave the person looking at a page of history that is now mostly
     * empty. Reloading also lets the empty page rule below do its work.
     */
    fun selectArea(areaId: String?) {
        if (selected.value == areaId) return
        selected.value = areaId
        viewModelScope.launch { reload() }
    }

    /**
     * The next page, requested by the footer becoming visible rather than by a button.
     *
     * The guard is set before the coroutine starts, so two frames of the same scroll
     * cannot both get through.
     */
    fun loadMore() {
        val current = load.value
        if (current.loading || current.appending || current.endOfHistory) return
        if (current.oldestFrom == null) return
        load.value = current.copy(appending = true)
        viewModelScope.launch {
            pageLock.withLock {
                val from = load.value.oldestFrom
                if (from == null) {
                    load.value = load.value.copy(appending = false)
                } else {
                    appendFrom(repository.newestEventBefore(from))
                }
            }
        }
    }

    /** Back to the newest page, discarding everything below it. */
    private suspend fun reload() = pageLock.withLock {
        load.value = TrailLoad(loading = true)
        val anchor = repository.newestEventAt()
        if (anchor == null) {
            // Null here means the log is empty, whatever the filter says, and no
            // further query is needed to know it.
            load.value = TrailLoad(loading = false, logIsEmpty = true, endOfHistory = true)
        } else {
            appendFrom(anchor)
        }
    }

    /**
     * Load pages backward from [startAnchor] until one of them has something in it.
     *
     * With no filter the first page always does, because it is anchored on a real
     * event rather than on today. With a filter it may not: an area last touched six
     * months ago is thirteen empty fortnights away, and asking a person to tap
     * thirteen times to find their own history is not an affordance. So an empty page
     * fetches the next one automatically, up to [MAX_PAGES_PER_LOAD] per request, and
     * then hands control back so the screen keeps drawing between attempts.
     *
     * The anchor for each next page is the newest event strictly before this page
     * started, never a fixed fourteen day step, so an empty stretch of history costs
     * one index seek instead of one page load per fortnight. MASTER_BUILD_PROMPT 9
     * keeps history forever, and a person returning after a year away has to be able
     * to reach the week they used this app.
     */
    private suspend fun appendFrom(startAnchor: Long?) {
        if (startAnchor == null) {
            load.value = load.value.copy(loading = false, appending = false, endOfHistory = true)
            return
        }
        val zone = clock.zone()
        var anchor: Long? = startAnchor
        var pages = 0
        var oldestFrom = load.value.oldestFrom
        var ended = false
        val collected = ArrayList<TrailDayModel>()
        while (pages < MAX_PAGES_PER_LOAD) {
            val at = anchor ?: break
            val window = TrailPaging.pageEndingAt(at, zone)
            collected += daysIn(window, zone)
            oldestFrom = window.fromMillis
            pages += 1
            val next = repository.newestEventBefore(window.fromMillis)
            if (next == null) {
                ended = true
                break
            }
            anchor = next
            if (collected.isNotEmpty()) break
        }
        val current = load.value
        load.value = current.copy(
            loading = false,
            appending = false,
            endOfHistory = ended,
            days = current.days + collected,
            oldestFrom = oldestFrom,
        )
    }

    /**
     * One page of events, resolved and grouped into days.
     *
     * Three id keyed queries stand between the page and its display values, and the
     * order they run in is forced. An ITEM_EDITED names only an item, so its area is
     * only knowable once that item's ITEM_ADDED is in hand; the same is true of the
     * two terminal focus types and their session's FOCUS_STARTED. So the item and
     * session history come first, an interim facade resolves which areas the page
     * actually mentions, and only then is the naming and coloring history for exactly
     * those areas fetched. Every one of the three is bounded by what one page named.
     *
     * The first two are handed every entity the page mentions rather than a sorted
     * subset of them, because each query filters by event type as well, so an area id
     * offered to the item history matches nothing and costs one bound variable.
     */
    private suspend fun daysIn(window: TrailWindow, zone: ZoneId): List<TrailDayModel> {
        val page = repository.trailPage(window.fromMillis, window.toMillis)
        if (page.isEmpty()) return emptyList()

        val entityIds = page.mapNotNull { it.entityId }.distinct()
        val items = repository.itemHistoryFor(entityIds)
        val sessions = repository.focusOriginsFor(entityIds)

        // A focus event is keyed by its session and never by the item it was run on,
        // so the item's own naming history is not reachable from the page's entity
        // ids at all. The session rows are the only thing that reveals it, which is
        // why the item history needs a second round the way the area history does.
        // Without this a session on an item added more than a fortnight ago renders
        // as "Finished 25 minutes of focus on" with nothing after the preposition.
        val focusedItemIds = itemIdsNeededBy(page, sessions).filterNot { it in entityIds }
        val focusedItems =
            if (focusedItemIds.isEmpty()) emptyList() else repository.itemHistoryFor(focusedItemIds)

        val resolvable = page + items + sessions + focusedItems

        val areaIds = TrailQueries(resolvable, zone).let { interim ->
            page.mapNotNull { interim.areaIdOf(it) }.distinct()
        }
        val queries = TrailQueries(resolvable + repository.areaHistoryFor(areaIds), zone)

        // Filtering before the rows are built, never after. The ten minute cluster
        // decides which rows print a timestamp, and a row hidden after that decision
        // leaves the cluster it anchored with no time on it at all.
        val filter = selected.value
        val visible = if (filter == null) page else page.filter { queries.areaIdOf(it) == filter }
        return queries.rows(visible).groupByDay(zone)
    }

    /**
     * Rows to day groups, preserving the display order they arrived in.
     *
     * Rows arrive newest first, so every day is already contiguous, and `groupBy`
     * keeps first encounter order, which makes the grouped list newest day first with
     * no sort of its own.
     *
     * A page's lower bound is local midnight and the next page anchors strictly before
     * that, so a day never spans two pages and an appended group can never need to
     * merge into the one above it. The date comes from the zone rather than from
     * dividing milliseconds by a day, which is wrong across every daylight saving
     * boundary and in every zone whose offset is not a whole number of hours.
     */
    private fun List<TrailRow>.groupByDay(zone: ZoneId): List<TrailDayModel> =
        groupBy { Instant.ofEpochMilli(it.wallClock).atZone(zone).toLocalDate() }
            .map { (date, rows) -> TrailDayModel(date = date, rows = rows) }

    /**
     * An area archived or deleted elsewhere in the app stops being a chip, so a
     * filter still pointing at it would leave the row with nothing selected and the
     * list showing a subset nobody asked for.
     */
    private fun dropFilterOnVanishedArea() {
        val current = selected.value ?: return
        if (repository.state.value.liveAreas.none { it.id == current }) selected.value = null
    }

    private companion object {
        /**
         * Fifty six days of empty history per request before the screen gets a turn.
         * The cap is about staying responsive, not about stopping: the footer asks
         * again as soon as it is still on screen.
         */
        const val MAX_PAGES_PER_LOAD = 4

        /**
         * A floor under the wait for the next local midnight, so a clock that jumps
         * backwards over the boundary cannot turn the tick into a busy loop.
         */
        const val MIN_MIDNIGHT_WAIT_MILLIS = 1_000L
    }
}
