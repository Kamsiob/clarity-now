package com.kamsiob.claritynow.ui.report

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kamsiob.claritynow.domain.guidance.GuidanceResult
import com.kamsiob.claritynow.domain.report.ClarityReport
import com.kamsiob.claritynow.domain.report.ReportNote
import com.kamsiob.claritynow.domain.report.ReportOutcome
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * The plan a closing line offers, when it offers one. `CLARITY_LOGIC_ENGINE.md` 10.3.
 *
 * The id and the first person line travel together because neither is any use without the
 * other. The id is what `PLAN_ACCEPTED` is written against, and [committedLine] is the one
 * visible thing writing it does. Two nullable fields on [ReportClosing] would have allowed
 * a closing that can be accepted and has nothing to say afterwards, and one that has a
 * stored line and nothing to accept, so the pair is one field.
 *
 * The id is the plan's own, derived from the week it belongs to, so a tap cannot accept a
 * plan from a report the screen is no longer showing.
 */
@Immutable
data class ClosingPlan(
    val id: String,
    /** First person. Rendered only after an accept, and never offered in this form. */
    val committedLine: String,
)

/**
 * The closing line, and the two answers to it. `design-v3.md` 11.1 item 8.
 *
 * **[line] is derived rather than stored, and it is the whole of what an accept shows.**
 * Until a plan is accepted the block reads the nominal offer; after one it reads the plan's
 * stored first person form and the offer is gone. A single mutable `line` beside a flag
 * would have made the two forms a thing to keep in step, and there would have been a state
 * in which the flag was set and the sentence had not changed.
 *
 * [plan] is null for a closing line with no plan in it, `CORPUS_2_REPORT.md` 4.6, and then
 * there is no pill and no decline because there is nothing to accept or refuse.
 *
 * **There is no declined state and there must never be one.** There is no `PLAN_DECLINED`
 * event, declining removes the block and records nothing, and 10.5 makes ignoring both
 * options identical to declining. So there is no answer a person can fail to give that
 * leaves them anywhere.
 */
@Immutable
data class ReportClosing(
    /** Nominal, never imperative. What the block reads until a plan is accepted. */
    val offeredLine: String,
    val plan: ClosingPlan?,
    val accepted: Boolean = false,
) {
    /** Whether a pill and a decline are drawn under the line at all. */
    val offersPlan: Boolean get() = plan != null

    /** The sentence on the screen, and the one the copy control puts on the clipboard. */
    val line: String get() = plan?.takeIf { accepted }?.committedLine ?: offeredLine
}

/**
 * The three things the Report body can be. `MASTER_BUILD_PROMPT.md` 12.3.
 *
 * They are three states rather than a report and a fallback, because the two that are not a
 * report mean completely different things and 12.3 is explicit that they must not be shown
 * as each other.
 */
sealed interface ReportPage {

    /** A composed report, with the seven marks that go above it. */
    @Immutable
    data class Composed(
        val report: ClarityReport,
        val ribbon: List<RibbonDay>,
        val closing: ReportClosing?,
    ) : ReportPage

    /**
     * `CORPUS_2_REPORT.md` 6.1. Nothing happened in the window, so one corpus line replaces
     * the whole body and no observation is generated to fill the page.
     *
     * [note] is null only when the bench is missing from the corpus, which is a packaging
     * fault rather than a state of the person's week.
     */
    @Immutable
    data class Empty(val note: ReportNote?) : ReportPage

    /**
     * The report scope checks refused what was composed, so nothing was written and nothing
     * is shown. `CLARITY_LOGIC_ENGINE.md` 9.2 and 12.3's prime directive.
     *
     * **Not the empty state.** `Nothing to report yet` is a true sentence about a week in
     * which nothing happened and a false one about a week the app could not prove its
     * arithmetic for. This state says what happened in the app's own words, states nothing
     * about the person's data, and carries no number.
     */
    data object Withheld : ReportPage

    /**
     * The corpus could not be read, so no sentence exists to show. A packaging fault, and
     * it is its own state so it appears in a log line as itself.
     */
    @Immutable
    data class Unavailable(val reason: String) : ReportPage
}

/** Everything the Report surface draws. */
@Immutable
data class ReportUiState(
    val loading: Boolean = true,
    /** True while a regenerate is in flight. Only the headline block reacts. */
    val regenerating: Boolean = false,
    /** The first of the seven days described. The eyebrow reads it. */
    val weekStart: LocalDate? = null,
    val page: ReportPage? = null,
    /**
     * The identity of the report to play the reveal for, or null to render already settled.
     * `design-v3.md` 8.2 item 12 and 8.4. See [ReportViewModel.revealFinished].
     */
    val revealKey: String? = null,
    val past: List<PastReport> = emptyList(),
    val pastLoading: Boolean = false,
)

/**
 * The Report surface's state, and the one control that does work.
 *
 * **It composes no sentence and reaches no corpus.** Every sentence the screen shows came
 * out of `ReportCoordinator`, which is the only thing here that holds a catalog, and every
 * number came through a `TrailQueries` function into a `FactRef`. `MASTER_BUILD_PROMPT.md`
 * 11.2 closes the list of things that may read a corpus and a ViewModel is not on it.
 *
 * **Every date comes from the injected clock through the coordinator.** Never from
 * `LocalDate.now()` and never from a composable, for the reason `ClarityClock` states: a
 * date computed against a default zone is the documented cause of a surface disagreeing
 * with itself about which day it is.
 *
 * ## Where the reveal lives, and why here
 *
 * `design-v3.md` 8.4 makes the Report reveal the one entrance that re-arms on a **content**
 * change as well as on a session change: a different report is different content, and
 * re-reading the same report does not re-animate it. `TabEntrance` has no key for that and
 * says so in its own documentation.
 *
 * A ViewModel in the Activity's store is exactly the lifetime that rule describes. It
 * survives a rotation and a theme switch, which 8.4 says must not re-arm anything; it
 * survives a tab switch, so returning to the Report does not replay the ribbon; and it dies
 * with the process, which 8.4 says is a new session and does re-arm. [revealKey] is
 * therefore held here rather than in the composition, where a tab switch would lose it, and
 * rather than in `rememberSaveable`, which would survive the process death that is supposed
 * to re-arm it.
 */
class ReportViewModel(private val coordinator: ReportCoordinator) : ViewModel() {

    private val _state = MutableStateFlow(ReportUiState())
    val state: StateFlow<ReportUiState> = _state.asStateFlow()

    /** The report whose reveal has already been played, in this process. */
    private var revealed: String? = null

    init {
        viewModelScope.launch { load(regenerate = false) }
    }

    /**
     * 12.3's regenerate control. Recalculates the window and composes again.
     *
     * Near instant, and it does not clear the page: [ReportUiState.regenerating] is what the
     * headline block reads, so the rest of the page stays where the reader left it rather
     * than collapsing and coming back.
     *
     * Regenerating twice in one day produces the same report, and that is correct rather
     * than a defect. Variant choice is salted with the local date and the firing history,
     * both of which are functions of the log, so a page that changed under a person who
     * tapped refresh twice would mean one of those was not being read.
     */
    fun regenerate() {
        if (_state.value.regenerating) return
        viewModelScope.launch { load(regenerate = true) }
    }

    /** Loads the History page's contents, on the first open of it. */
    fun loadPast() {
        if (_state.value.pastLoading) return
        _state.update { it.copy(pastLoading = true) }
        viewModelScope.launch {
            val past = coordinator.pastReports()
            _state.update { it.copy(past = past, pastLoading = false) }
        }
    }

    /**
     * Records that this report's reveal has been played, so returning to the tab renders it
     * at rest. `design-v3.md` 8.4.
     *
     * Called by the screen when the sequence finishes rather than when it starts, so a tab
     * switch part way through the ribbon draw replays it rather than leaving it half drawn.
     */
    fun revealFinished(key: String) {
        revealed = key
        _state.update { if (it.revealKey == key) it.copy(revealKey = null) else it }
    }

    /**
     * Accepts the closing line's plan. `design-v3.md` 11.1 item 8 and 8.2 item 26, and
     * `CLARITY_LOGIC_ENGINE.md` 10.5.
     *
     * **The line changes, the pill settles, one event is written, and that is the whole of
     * it.** No toast, no celebration, no bounce, no haptic heavier than an ordinary tap,
     * and no notification, badge, widget or home screen card afterwards. The plan exists in
     * the report and nowhere else, which `PlanSurfaceTest` holds by reading the sources.
     *
     * The one visible consequence is that the block stops offering and starts stating: the
     * nominal offer is replaced by the plan's stored first person line, which is a form
     * this app renders only here and only after this tap.
     *
     * The state settles first and the write follows, because the tap has to feel immediate
     * and the write is idempotent: `acceptPlan` on an already accepted plan returns it
     * unchanged. What the event buys is 10.6, and only 10.6: next week the motivating
     * observation family is ranked one place higher, and if it does not qualify on its own
     * merits nothing appears at all.
     */
    fun acceptPlan() {
        val plan = (state.value.page as? ReportPage.Composed)?.closing?.plan ?: return
        _state.update { state ->
            val page = state.page as? ReportPage.Composed ?: return@update state
            val closing = page.closing ?: return@update state
            if (closing.plan == null || closing.accepted) return@update state
            state.copy(page = page.copy(closing = closing.copy(accepted = true)))
        }
        viewModelScope.launch { coordinator.acceptPlan(plan.id) }
    }

    /**
     * Declines it. `MASTER_BUILD_PROMPT.md` 11.4: never offer a plan without an explicit
     * decline, and one button is not a choice.
     *
     * **Nothing is recorded and nothing is shown afterwards.** There is no `PLAN_DECLINED`
     * event, the closing block simply goes, and 11.1 closes the subject: both options are
     * costless and neither is ever mentioned again.
     */
    fun declinePlan() {
        _state.update { state ->
            val page = state.page as? ReportPage.Composed ?: return@update state
            state.copy(page = page.copy(closing = null))
        }
    }

    private suspend fun load(regenerate: Boolean) {
        _state.update { it.copy(loading = !regenerate, regenerating = regenerate) }
        val generation = coordinator.generate()
        val page = pageOf(generation)
        val key = keyOf(generation, page)
        _state.update {
            it.copy(
                loading = false,
                regenerating = false,
                weekStart = runCatching { LocalDate.parse(generation.week.weekStartKey) }.getOrNull(),
                page = page,
                revealKey = if (key != null && key != revealed) key else null,
            )
        }
    }

    private fun pageOf(generation: ReportGeneration): ReportPage {
        val outcome = generation.outcome
            ?: return ReportPage.Unavailable(generation.languageFailure ?: UNKNOWN_FAILURE)
        return when (outcome) {
            is ReportOutcome.Composed -> ReportPage.Composed(
                report = outcome.report,
                ribbon = generation.ribbon,
                closing = closingOf(outcome.report.closing, generation.planAccepted),
            )

            is ReportOutcome.Empty -> ReportPage.Empty(outcome.note)
            is ReportOutcome.Suppressed -> ReportPage.Withheld
        }
    }

    /**
     * Layer 6's result, as the closing block draws it. `CLARITY_LOGIC_ENGINE.md` 10.
     *
     * Three states and the third is null: `GuidanceResult.Nothing` means the report ends
     * with its last observation and the block is not drawn at all. A non plan closing draws
     * the line with no pill and no decline under it, because there is nothing to accept or
     * refuse; `ReportClosing.offersPlan` is what the composable reads for that.
     *
     * **[accepted] comes from the log rather than from this screen's memory**, through
     * `ReportGeneration.planAccepted`, and that is what makes an accept survive a
     * regenerate, a tab switch and a launch tomorrow. A screen that only remembered its own
     * tap would offer the plan again on the next composition and show the nominal line to
     * somebody who had already answered.
     */
    private fun closingOf(result: GuidanceResult, accepted: Boolean): ReportClosing? =
        when (result) {
            is GuidanceResult.Plan -> ReportClosing(
                offeredLine = result.plan.offeredLine,
                plan = ClosingPlan(
                    id = result.plan.id,
                    committedLine = result.plan.committedLine,
                ),
                accepted = accepted,
            )

            is GuidanceResult.Closing -> ReportClosing(
                offeredLine = result.line.text,
                plan = null,
            )

            is GuidanceResult.Nothing -> null
        }

    /**
     * What makes this report the same report as the last one, for 8.4's content exception.
     *
     * The week and the headline's variant, which is what changes when the engine says
     * something different about a different seven days. A report with no headline is keyed
     * by its week alone, and the states that are not a report do not animate at all: there
     * is no ribbon to draw and nothing to reveal.
     */
    private fun keyOf(generation: ReportGeneration, page: ReportPage): String? {
        if (page !is ReportPage.Composed) return null
        val variant = page.report.headline?.variantKey ?: ClarityReport.NO_HEADLINE
        return "${generation.week.weekStartKey}/$variant"
    }

    private companion object {
        const val UNKNOWN_FAILURE = "the corpus has not been read"
    }
}
