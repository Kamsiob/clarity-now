package com.kamsiob.claritynow.ui.report

import androidx.compose.runtime.Immutable
import com.kamsiob.claritynow.data.repo.ClarityRepository
import com.kamsiob.claritynow.domain.ClarityClock
import com.kamsiob.claritynow.domain.engine.FactExtractor
import com.kamsiob.claritynow.domain.corpus.CatalogLoad
import com.kamsiob.claritynow.domain.corpus.SharedCatalog
import com.kamsiob.claritynow.domain.engine.FiringHistory
import com.kamsiob.claritynow.domain.engine.realize.Measures
import com.kamsiob.claritynow.domain.parseDateKey
import com.kamsiob.claritynow.domain.query.TrailQueries
import com.kamsiob.claritynow.domain.replay.ReportState
import com.kamsiob.claritynow.data.event.PlanOffered
import com.kamsiob.claritynow.domain.guidance.GuidanceResult
import com.kamsiob.claritynow.domain.guidance.PlanHistory
import com.kamsiob.claritynow.domain.report.ReportOutcome
import com.kamsiob.claritynow.domain.report.ReportComposer
import com.kamsiob.claritynow.domain.report.ReportLanguage
import com.kamsiob.claritynow.domain.report.ReportSchedule
import com.kamsiob.claritynow.domain.report.ReportWeek
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.ZoneId

/**
 * One day of the week ribbon. `design-v3.md` 11.1 item 4.
 *
 * [count] is `TrailQueries.eventsPerDay` for this local calendar day and there is no other
 * path to it. The mark's height and opacity are that count against [WeekRibbon]'s busiest,
 * which is the whole of what the ribbon draws.
 *
 * **There is no field here that relates one day to the next**, which is deliberate and is
 * the same guard `PulseMark` carries: no run length, no "days since", no consecutive count.
 * `design-v3.md` 14 forbids streaks and `FactSet` has no streak fact by construction, so
 * the surface that would reintroduce one by eye is handed seven independent days and
 * nothing that joins them.
 */
@Immutable
data class RibbonDay(val date: LocalDate, val count: Int)

/**
 * What one generation produced, with everything the screen draws around it.
 *
 * [outcome] is null only when the corpus could not be read, which costs every sentence and
 * leaves [ribbon] intact: the marks are counted from the log and need no language at all,
 * so a packaging fault must not take the whole tab down with it. [languageFailure] says
 * why, so a month of silent Reports cannot be mistaken for a month with nothing to say.
 *
 * [planAccepted] is read off the log rather than remembered by the screen, so the closing
 * block shows the stored first person line to somebody who accepted this week's plan
 * yesterday. False when there is no plan, which is the same as false when there is one
 * nobody answered: `CLARITY_LOGIC_ENGINE.md` 10.5 makes ignoring an offer identical to
 * declining it, so there is no third value to carry.
 */
@Immutable
data class ReportGeneration(
    val week: ReportWeek,
    val ribbon: List<RibbonDay>,
    val outcome: ReportOutcome?,
    val languageFailure: String?,
    val planAccepted: Boolean = false,
)

/**
 * One past report, as the History page renders it. `MASTER_BUILD_PROMPT.md` 12.3.
 *
 * Every string here was written by the engine and stored on `REPORT_GENERATED`. Nothing on
 * that page composes a sentence and there is no field for one that could be composed.
 *
 * **[headline] is null for every report this build can store, and that is a gap rather
 * than a design.** `ReportGenerated` carries `headlineKey` and `headlineVariantKey` and
 * does not carry the headline's rendered text, while `renderedSections` carries the text of
 * every observation. `design-v3.md` section 5 gives past report headlines the display role,
 * so the page has a treatment for a string the payload cannot supply. See the note in
 * `ReportHistoryPage`.
 */
@Immutable
data class PastReport(
    val weekStartKey: String,
    val weekStart: LocalDate?,
    val headline: String?,
    val sections: List<String>,
    val ribbon: List<RibbonDay>,
    /**
     * The three numbers this report's own caption stated, by measure id.
     *
     * Read back out of `REPORT_GENERATED`'s fact snapshot under the same `FactRef` the
     * caption was rendered from, rather than counted again now. A past report states what
     * it stated: re-querying would let a row show a number the report itself never
     * carried, which is the failure `CLARITY_LOGIC_ENGINE.md` 9.2's map exists to prevent,
     * moved a week later where nobody would ever catch it.
     */
    val totals: Map<String, Int>,
)

/**
 * The Report lifecycle, wired. `MASTER_BUILD_PROMPT.md` 11.3 and 12.3.
 *
 * Everything impure about the surface is here: the clock, the log and the corpus text.
 * `ReportComposer` holds the composition rules and can be tested with no Android and no
 * database; this holds the plumbing and cannot. It is the same split `PulseCoordinator` and
 * `PulseGenerator` already make, and the same one `MomentumCoordinator` makes.
 *
 * ## It is in `ui.report` and it belongs in `domain.report`
 *
 * Recorded rather than hidden, because the two files it would sit beside are already
 * written. `PulseCoordinator` is in `domain.pulse` and `MomentumCoordinator` is in
 * `domain.momentum`, and this is the third of exactly the same thing. It is here because
 * `domain/report/` was outside the file list this slice was given, and moving it is a
 * package line and an import. Nothing about it depends on Android, which is what makes the
 * move free.
 *
 * ## Step 9 is not here, and nothing else fills in for it
 *
 * 11.3's sequence ends `9. Write REPORT_GENERATED, and PLAN_OFFERED if a plan was
 * produced`, and that step belongs to `ClarityRepository`, which is the only writer in the
 * app and which has no method for it yet. So this composes and does not persist, and three
 * things follow that a later session must not mistake for design decisions:
 *
 * - **The cadence cannot be satisfied.** 12.3 generates on the first open in a new week
 *   and [isDue] asks the log exactly that question, correctly, against
 *   `ReportWeek.currentWeekStartMillis`. With nothing writing the event it always answers
 *   true, so the report is composed on every open. It is deterministic, so a person sees
 *   the same page every time, but it is not the specified cadence
 * - **`FiringHistory` never learns what the Report said.** The ninety day variant exclusion
 *   and the fourteen day family cooldowns are rebuilt from `REPORT_GENERATED` events, so
 *   until one is written the Report cannot vary itself week to week
 * - **Past reports are empty.** [pastReports] reads the projection, which is fed by the
 *   log, which is the right way round and has nothing in it
 *
 * ## The catalog is not built here
 *
 * It arrives as [SharedCatalog], the one catalog for the process per `MASTER_BUILD_PROMPT.md`
 * 11.7. This class built a third one of its own until issue #55, behind a mutex and a cached
 * field that were character for character the two other coordinators' copies.
 */
class ReportCoordinator(
    private val repository: ClarityRepository,
    private val clock: ClarityClock,
    private val catalog: SharedCatalog,
) {

    /**
     * One report for the trailing seven days ending today. 11.3 steps 1 to 6.
     *
     * **Recalculated on every call**, per 12.3, which is also what makes regenerate a call
     * to this and nothing else. The firing history is rebuilt from the whole log every
     * time, per 11.7: caching it is how two devices holding one log start disagreeing.
     */
    suspend fun generate(): ReportGeneration {
        repository.load()
        val zone = clock.zone()
        val now = clock.nowMillis()

        // 1. The window, and the week the cadence counts against. Two different questions,
        // and `ReportSchedule` exists to keep them apart.
        val week = ReportSchedule.weekAt(now, zone)
        val queries = TrailQueries(repository.allEvents(), zone)
        val ribbon = ribbonOf(queries, week)

        // The catalog and the reason it is missing come back in one value, so the page can
        // never report a failure that belongs to some other surface or, worse, render no
        // sentences and name no reason. See `CatalogLoad`.
        val language = when (val load = catalog.load()) {
            is CatalogLoad.Ready -> load.catalog
            is CatalogLoad.Failed ->
                return ReportGeneration(week, ribbon, outcome = null, languageFailure = load.reason)
        }

        // 2 to 6. Extraction folds the whole log and composition realizes and validates
        // every line, so both run off the main thread. Neither touches Android.
        val outcome = withContext(Dispatchers.Default) {
            val facts = FactExtractor(queries).extract(week.window)
            val history = FiringHistory.from(queries, now)
            // 10.6's follow through, rebuilt from the log on every call for 11.7's reason.
            // An offer with no acceptance beside it leaves no entry, so a declined plan
            // reaches the ranking as an empty set.
            val plans = PlanHistory.from(queries, now)
            ReportComposer(language, zone).compose(facts, history, week.weekStartKey, plans)
        }
        // 7. Step 9's second half. The plan is recorded when it is **offered**, not when it
        // is accepted, because `FiringHistory` reads the frame, cue and action keys out of
        // `PLAN_OFFERED` for 7.6's ninety day exclusion and a plan nobody sees twice is the
        // point of that exclusion. It is not a record of an answer: 10.5's decline still
        // writes nothing, and ignoring the offer still writes nothing. The id is derived
        // from the week, so a second generation of the same week files nothing new.
        val accepted = recordPlanIfOffered(outcome)
        return ReportGeneration(
            week,
            ribbon,
            outcome,
            languageFailure = null,
            planAccepted = accepted,
        )
    }

    /**
     * Appends `PLAN_OFFERED` for a plan this generation produced, and answers whether that
     * plan has already been accepted. 11.3 step 9.
     *
     * Idempotent by the plan's own id, which is derived from the week it belongs to, so the
     * report regenerating does not file a second plan for one week. Nothing visible happens
     * when it is filed and nothing is shown: the offer is in the log so that layer 6 does
     * not put the same frame in front of somebody twice in a season.
     *
     * **The acceptance comes back from the same call that files the offer**, because the
     * writer already returns the plan's state and it is the state of the one plan this
     * report is about. Asking a second time would be a second read of the same fact, and
     * two reads are how a screen comes to disagree with the log it was drawn from. False
     * for a report with no plan in it, which is what the closing block does with it.
     */
    private suspend fun recordPlanIfOffered(outcome: ReportOutcome): Boolean {
        val composed = outcome as? ReportOutcome.Composed ?: return false
        val plan = (composed.report.closing as? GuidanceResult.Plan)?.plan ?: return false
        return repository.recordPlanOffered(
            PlanOffered(
                planId = plan.id,
                weekStartKey = plan.weekStartKey,
                frameKey = plan.frameKey,
                cueKey = plan.cueKey,
                actionKey = plan.actionKey,
                familyKey = plan.familyKey,
                subjectId = plan.subjectId,
                offeredLine = plan.offeredLine,
                committedLine = plan.committedLine,
                resolutionFactRef = plan.resolutionFactRef,
            ),
        ).isAccepted
    }

    /**
     * Accepts the plan [planId]. CLARITY_LOGIC_ENGINE.md 10.5.
     *
     * One event and nothing else. The screen settles its pill; there is no toast, no
     * celebration, no bounce and no haptic heavier than an ordinary tap, and nothing
     * anywhere afterwards refers to the plan again.
     */
    suspend fun acceptPlan(planId: String) {
        repository.load()
        repository.acceptPlan(planId)
    }

    /**
     * Whether 12.3's cadence says a report is due: no `REPORT_GENERATED` since local
     * midnight on the Sunday that began this week.
     *
     * Asked of the log rather than of a stored flag, so it cannot disagree with itself and
     * so a merged log answers it correctly. Every event carries its wall clock, which is
     * why this needs no field on the payload.
     *
     * **It always answers true today**, because nothing writes the event. See the class
     * note. It is written now so that the cadence is already correct on the day the writer
     * lands rather than being derived a second time by whoever adds it.
     */
    suspend fun isDue(): Boolean {
        repository.load()
        val zone = clock.zone()
        val week = ReportSchedule.weekAt(clock.nowMillis(), zone)
        val queries = TrailQueries(repository.allEvents(), zone)
        return queries.reportsGeneratedBetween(week.currentWeekStartMillis, Long.MAX_VALUE).isEmpty()
    }

    /**
     * Every report ever written, newest first. 12.3: past weeks remain forever.
     *
     * Read from the projection, which is folded from the log, so a report survives a cache
     * rebuild and an import for the same reason every other row does.
     */
    suspend fun pastReports(): List<PastReport> {
        repository.load()
        val zone = clock.zone()
        val stored = repository.state.value.reports.values.sortedByDescending { it.generatedAt }
        if (stored.isEmpty()) return emptyList()
        val queries = TrailQueries(repository.allEvents(), zone)
        return stored.map { report -> pastReportOf(report, queries, zone) }
    }

    // ------------------------------------------------------------------ the ribbon

    /**
     * The seven marks, counted from the log.
     *
     * `eventsPerDay` buckets by local calendar day in the facade's own zone rather than by
     * dividing milliseconds, so the week the clocks change still has seven days in it. A
     * day with no activity is present with a count of nought rather than missing: the
     * ribbon draws it at its floor, and a mark that vanished would make a quiet Tuesday
     * look like a broken row.
     */
    private fun ribbonOf(queries: TrailQueries, week: ReportWeek): List<RibbonDay> {
        val counts = queries.eventsPerDay(week.window.fromMillis, week.window.toMillis)
        val first = parseDateKey(week.weekStartKey)
        return (0 until ReportSchedule.WINDOW_DAYS).map { offset ->
            val date = first.plusDays(offset.toLong())
            RibbonDay(date = date, count = counts[date.toString()] ?: 0)
        }
    }

    private fun pastReportOf(report: ReportState, queries: TrailQueries, zone: ZoneId): PastReport {
        val start = runCatching { parseDateKey(report.weekStartKey) }.getOrNull()
        val window = start?.let { first ->
            val from = first.atStartOfDay(zone).toInstant().toEpochMilli()
            val to = first.plusDays(ReportSchedule.WINDOW_DAYS.toLong())
                .atStartOfDay(zone).toInstant().toEpochMilli()
            val counts = queries.eventsPerDay(from, to)
            (0 until ReportSchedule.WINDOW_DAYS).map { offset ->
                val date = first.plusDays(offset.toLong())
                RibbonDay(date = date, count = counts[date.toString()] ?: 0)
            }
        }
        return PastReport(
            weekStartKey = report.weekStartKey,
            weekStart = start,
            // Not on the payload. See the note on `PastReport`.
            headline = null,
            sections = report.sections.map { it.text },
            ribbon = window.orEmpty(),
            totals = totalsOf(report),
        )
    }

    /**
     * The caption's three numbers as this report recorded them.
     *
     * `Measures` resolves each id to the `FactRef` it was read under, and the snapshot on
     * the event is keyed by that reference's string form. A measure the snapshot does not
     * carry is simply absent, exactly as a total of nought is absent on a fresh report.
     */
    private fun totalsOf(report: ReportState): Map<String, Int> =
        ReportLanguage.CAPTION_MEASURES.mapNotNull { id ->
            val ref = Measures.byId(id)?.refFor(null) ?: return@mapNotNull null
            val value = report.factSnapshot[ref.toString()]?.toIntOrNull() ?: return@mapNotNull null
            id to value
        }.toMap()
}
