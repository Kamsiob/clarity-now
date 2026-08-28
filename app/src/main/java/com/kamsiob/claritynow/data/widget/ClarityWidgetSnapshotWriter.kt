package com.kamsiob.claritynow.data.widget

import com.kamsiob.claritynow.data.prefs.ClarityPreferences
import com.kamsiob.claritynow.data.repo.ClarityRepository
import com.kamsiob.claritynow.domain.ClarityClock
import com.kamsiob.claritynow.domain.dateKey
import com.kamsiob.claritynow.domain.momentum.MomentumWindows
import com.kamsiob.claritynow.domain.query.TrailQueries
import com.kamsiob.claritynow.domain.replay.ClarityState
import com.kamsiob.claritynow.domain.replay.FocusSessionState
import kotlinx.coroutines.flow.combine

/**
 * Keeps the widget snapshot in step with the log. MASTER_BUILD_PROMPT 13.3, "written to
 * DataStore on every meaningful change, plus a WorkManager refresh every 6 hours".
 *
 * ## A collector rather than a call at every write site
 *
 * `ClarityRepository` is the only writer in the app and it publishes the projection as
 * a `StateFlow`, so "every meaningful change" is already a thing this can subscribe to.
 * The alternative is a call to refresh the widgets at the end of every repository
 * method, which is thirty call sites and one of them will be forgotten, and the way it
 * fails is a home screen that is quietly a day behind. Following the state cannot be
 * forgotten, because there is nothing to remember.
 *
 * The stored calm mode joins the projection in the same combine, so moving that switch
 * writes a new snapshot the same second. It is a trigger rather than the value: what is
 * written is [calmMode], resolved the way the app resolves it, because a stored null
 * means "follow the system" and only the resolver knows what the system says.
 *
 * ## Why the periodic refresh exists when the collector never misses a change
 *
 * Two things go stale with no change to the log at all. The automatic area rotates on
 * the local day, so a phone left alone overnight would show yesterday's choice, and the
 * fourteen day row that the deferred `Rhythm` widget reads rolls forward the same way.
 * Both are functions of the clock rather than of the state, and the six hourly refresh
 * is what re-asks the clock. It is also the recovery path for a write the system
 * dropped and for a process that was killed before it collected anything.
 *
 * ## The write that is skipped
 *
 * A snapshot whose only difference from the stored one is the instant it was written is
 * not written. Every write costs a file write and a redraw of every placed widget, and
 * the state flow re-emits on events that change nothing a widget shows, `APP_OPENED`
 * being the daily example. [ClarityWidgetSnapshot.sameContentAs] is where that
 * comparison is defined, deliberately in the type rather than here.
 */
class ClarityWidgetSnapshotWriter(
    private val repository: ClarityRepository,
    private val preferences: ClarityPreferences,
    private val clock: ClarityClock,
    private val store: ClarityWidgetSnapshotStore,
    private val calmMode: suspend () -> Boolean,
    private val onWritten: suspend () -> Unit,
) {

    /**
     * Follows the projection for the life of the process, and never returns.
     *
     * The load is idempotent under the repository's own lock, which is why every other
     * process wide collector in this app calls it too: the projection has to be real
     * before an empty one can be trusted, or a cold start would write a snapshot saying
     * a person has no areas and every widget would redraw as an invitation to make one.
     */
    suspend fun follow() {
        repository.load()
        combine(
            repository.state,
            repository.runningFocusSession,
            preferences.calmMode,
        ) { state, session, _ -> Reading(state, session) }
            .collect { reading -> writeFrom(reading.state, reading.session) }
    }

    /** One pass, for the periodic refresh and for anything else that needs to be sure. */
    suspend fun refresh() {
        repository.load()
        writeFrom(repository.state.value, repository.runningFocusSession.value)
    }

    private suspend fun writeFrom(state: ClarityState, session: FocusSessionState?) {
        val previous = store.read()
        val now = clock.nowMillis()
        val next = ClarityWidgetSnapshotComposer.compose(
            state = state,
            nowMillis = now,
            dateKey = clock.dateKey(now),
            calmMode = calmMode(),
            runningSession = session,
            previous = previous,
            speech = speechFor(now),
        )
        if (next.sameContentAs(previous)) return
        if (store.write(next)) onWritten()
    }

    /**
     * The parts of the snapshot that are not in the projection.
     *
     * **The fourteen day row is counted here rather than in the composer**, because it
     * is a fold over the log and the composer takes a projection. It is the same window
     * and the same count Momentum draws, through the same two objects, so the row on a
     * home screen and the row on the Momentum screen cannot disagree: design-v3.md 12.2
     * asks for the dot row "exactly as Momentum renders it" and this is what exactly
     * means.
     *
     * **It reads a fortnight of the log and never the whole of it.** `TrailQueries` over
     * a bounded page answers `activeDayKeys` for that page identically, because the
     * function filters by the same window it is given, and a full read on every write
     * would put the cost of a Trail page behind every completion.
     *
     * `line` is deliberately left empty. The sentence under the row is a fixed string
     * with two numbers in it, which `CLAUDE.md` rule 8 keeps in `strings.xml`, and the
     * widget reads it from there; the field exists for a sentence the engine wrote, and
     * this is not one. The week figures and the accepted plan line are filled by the
     * composer and by whoever builds the two deferred widgets.
     */
    private suspend fun speechFor(nowMillis: Long): WidgetSpeech {
        val zone = clock.zone()
        val window = MomentumWindows.fortnight(nowMillis, zone)
        val days = MomentumWindows.dayKeys(nowMillis, zone, MomentumWindows.FORTNIGHT_DAYS)
        val active = TrailQueries(repository.trailPage(window.fromMillis, window.toMillis), zone)
            .activeDayKeys(window.fromMillis, window.toMillis)
        return WidgetSpeech(
            rhythm = WidgetRhythm(
                activeDays = days.map { it in active },
                // Today is the trailing end of the row by construction, and it is
                // carried rather than assumed for the reason MomentumView carries it.
                todayIndex = days.lastIndex,
            ),
        )
    }
}

/** The three flows as one value, so the combine has something to hand on. */
private data class Reading(val state: ClarityState, val session: FocusSessionState?)
