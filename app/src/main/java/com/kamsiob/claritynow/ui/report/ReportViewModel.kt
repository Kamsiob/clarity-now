package com.kamsiob.claritynow.ui.report

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
 * The closing line, and the two answers to it. `design-v3.md` 11.1 item 8.
 *
 * **Always null today, and the composable that draws it is built anyway.** The closing line
 * is layer six, `CLARITY_LOGIC_ENGINE.md` section 8, and layer six is phase 9b: 11.3's
 * sequence passes only the observations that actually appeared into `GuidanceComposer`
 * after the body exists, and `ClarityReport` has no field for what comes back yet. When it
 * does, this type is what the screen already knows how to draw and [ReportPage.Composed]
 * is where it arrives.
 *
 * [accepted] exists so the pill can settle at reduced prominence after a tap, per 8.2 item
 * 26. **Declining records nothing at all**, which is why there is no declined state here:
 * there is no `PLAN_DECLINED` event, deliberately, and 11.1 says both options are costless
 * and neither is ever mentioned again.
 */
@Immutable
data class ReportClosing(val line: String, val offersPlan: Boolean, val accepted: Boolean)

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
     * Accepts the closing line's plan. `design-v3.md` 11.1 item 8 and 8.2 item 26.
     *
     * **This settles the pill and writes nothing**, because there is nothing to write yet:
     * `PLAN_OFFERED` is layer six's, phase 9b, and `PLAN_ACCEPTED` refers to a plan id that
     * only that phase can mint. The screen's half of the interaction is built and the
     * write is one call to the repository when the plan exists.
     */
    fun acceptPlan() {
        _state.update { state ->
            val page = state.page as? ReportPage.Composed ?: return@update state
            val closing = page.closing ?: return@update state
            if (closing.accepted) return@update state
            state.copy(page = page.copy(closing = closing.copy(accepted = true)))
        }
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
                // Layer six, phase 9b. See ReportClosing.
                closing = null,
            )

            is ReportOutcome.Empty -> ReportPage.Empty(outcome.note)
            is ReportOutcome.Suppressed -> ReportPage.Withheld
        }
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
