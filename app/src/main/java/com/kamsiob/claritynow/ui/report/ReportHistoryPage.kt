package com.kamsiob.claritynow.ui.report

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kamsiob.claritynow.R
import com.kamsiob.claritynow.ui.components.ClarityIcon
import com.kamsiob.claritynow.ui.components.ClarityIcons
import com.kamsiob.claritynow.ui.components.clarityClickable
import com.kamsiob.claritynow.ui.components.clarityFocusRing
import com.kamsiob.claritynow.ui.components.clarityPressScale
import com.kamsiob.claritynow.ui.theme.ClarityHapticEvent
import com.kamsiob.claritynow.ui.theme.ClaritySpacing
import com.kamsiob.claritynow.ui.theme.LocalClarityTypography
import com.kamsiob.claritynow.ui.theme.LocalContemplativeColors
import com.kamsiob.claritynow.ui.theme.ReportPalette
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Past reports. `MASTER_BUILD_PROMPT.md` 12.3 and `design-v3.md` 10.15.
 *
 * > Past weeks remain forever.
 *
 * Reached by the history glyph on the Report and left by back, which is the whole of its
 * navigation. Every string on it was written by the engine and stored on
 * `REPORT_GENERATED`; nothing here composes a sentence and there is no field for one that
 * could be composed.
 *
 * ## The ribbon at 60 percent, and the two things it must not do
 *
 * `design-v3.md` 11.1: the ribbon "repeats at 60 percent scale in the past reports list and
 * nowhere else". So it repeats here, at [SMALL_SCALE], and it keeps its caption, because
 * section 13's rule that the ribbon is never the sole carrier of a claim does not have a
 * small size exemption. The day initials are the one thing dropped, and [WeekRibbon] says
 * why: 8sp is already this design's floor and 60 percent of it is 4.8sp.
 *
 * The caption's numbers come off the report's own fact snapshot rather than from a fresh
 * query, so a row states what that week's report stated. See [PastReport.totals].
 *
 * ## What this page cannot show yet, named rather than approximated
 *
 * `design-v3.md` section 5 gives past report headlines the display role, and the payload
 * does not carry one: `ReportGenerated` records `headlineKey` and `headlineVariantKey` and
 * the rendered text of every section except the headline. Re-realizing the variant from the
 * corpus would be a second path to a sentence, which `CLAUDE.md` rule 8 closes, and the
 * facts of that week are gone in any case. So a row leads with its week and its ribbon, and
 * [PastReport.headline] is a field waiting for one more string on the payload.
 *
 * **The list is empty on every build that exists today**, because nothing writes
 * `REPORT_GENERATED`. See the note on [ReportCoordinator].
 */
@Composable
internal fun ReportHistoryPage(
    reports: List<PastReport>,
    loading: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contemplative = LocalContemplativeColors.current
    val type = LocalClarityTypography.current

    Box(modifier = modifier.fillMaxSize()) {
        ReportBackdrop()

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() +
                    ClaritySpacing.minTouchTarget + TITLE_TOP,
                bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() +
                    ClaritySpacing.tabBarHeight + ClaritySpacing.tabBarInset + LIST_BOTTOM,
            ),
        ) {
            item(key = "title") {
                Text(
                    text = stringResource(R.string.report_history_title),
                    style = type.displayTitle,
                    color = contemplative.textBright,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = ClaritySpacing.screenPadding),
                )
                Spacer(Modifier.height(TITLE_GAP))
            }

            if (reports.isEmpty() && !loading) {
                item(key = "empty") { HistoryEmptyState() }
            }

            items(items = reports, key = { it.weekStartKey }) { report ->
                PastReportRow(report)
                Spacer(Modifier.height(ROW_GAP))
            }
        }

        BackControl(
            onBack = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
                .padding(start = CONTROL_INSET),
        )
    }
}

@Composable
private fun PastReportRow(report: PastReport, modifier: Modifier = Modifier) {
    val contemplative = LocalContemplativeColors.current
    val type = LocalClarityTypography.current
    val caption = ribbonCaption(report.totals.toList())

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = ClaritySpacing.screenPadding),
        verticalArrangement = Arrangement.spacedBy(ROW_ELEMENT_GAP),
    ) {
        Text(
            text = report.weekStart?.let { weekOf(it) } ?: report.weekStartKey,
            style = type.caption,
            color = contemplative.textDim,
        )

        report.headline?.let { headline ->
            Text(text = headline, style = type.readSerif, color = contemplative.textBright)
        }

        if (report.ribbon.isNotEmpty() && caption != null) {
            WeekRibbon(
                days = report.ribbon,
                caption = caption,
                reveal = false,
                scale = SMALL_SCALE,
            )
        }

        report.sections.forEach { line ->
            Text(text = line, style = type.bodySerif, color = ReportPalette.body)
        }
    }
}

/**
 * `design-v3.md` 10.13. An invitation, not a scold, naming what will fill it.
 *
 * It says what the app will do rather than what the person has not done, and it carries no
 * count of anything. A fixed interface string, because it is the app describing itself.
 */
@Composable
private fun HistoryEmptyState() {
    val contemplative = LocalContemplativeColors.current
    val type = LocalClarityTypography.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ClaritySpacing.screenPadding, vertical = EMPTY_SPACE),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(ROW_ELEMENT_GAP),
    ) {
        Text(
            text = stringResource(R.string.report_history_empty_title),
            style = type.readSerif,
            color = contemplative.textBright,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(R.string.report_history_empty_body),
            style = type.body,
            color = contemplative.textDim,
            textAlign = TextAlign.Center,
        )
    }
}

/**
 * The visible way back. `design-v3.md` 10.15 leaves this page by back, and a control that
 * does not depend on a dispatcher reaching a composition is the way out that always works.
 */
@Composable
private fun BackControl(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val contemplative = LocalContemplativeColors.current
    val interaction = remember { MutableInteractionSource() }
    val label = stringResource(R.string.cd_report_history_back)

    Box(
        modifier = modifier
            .size(ClaritySpacing.minTouchTarget)
            .clarityPressScale(interaction, label = "reportBack")
            .clarityFocusRing(interaction, CircleShape)
            .clarityClickable(
                interactionSource = interaction,
                haptic = ClarityHapticEvent.TAP,
                role = Role.Button,
                onClickLabel = label,
                onClick = onBack,
            ),
        contentAlignment = Alignment.Center,
    ) {
        ClarityIcon(
            icon = ClarityIcons.back,
            contentDescription = null,
            tint = contemplative.textFaint,
            modifier = Modifier.size(GLYPH_SIZE),
        )
    }
}

/** The week a past report describes, formatted as the eyebrow formats it. */
@Composable
private fun weekOf(weekStart: LocalDate): String {
    val locale = Locale.getDefault()
    val formatter = remember(locale) { DateTimeFormatter.ofPattern(HISTORY_WEEK_PATTERN, locale) }
    return stringResource(R.string.report_history_week, formatter.format(weekStart))
}

/**
 * A year here where the eyebrow has none.
 *
 * The eyebrow names the week a person is reading now and 11.1 gives it one line. This list
 * goes back to install and two Julys look identical without one.
 */
private const val HISTORY_WEEK_PATTERN = "MMMM d, yyyy"

private val TITLE_TOP = 10.dp
private val TITLE_GAP = 24.dp
private val ROW_GAP = 34.dp
private val ROW_ELEMENT_GAP = 12.dp
private val LIST_BOTTOM = 40.dp
private val EMPTY_SPACE = 48.dp
private val CONTROL_INSET = 8.dp
private val GLYPH_SIZE = 20.dp
