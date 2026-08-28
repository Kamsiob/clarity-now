package com.kamsiob.claritynow.ui.report

import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kamsiob.claritynow.R
import com.kamsiob.claritynow.domain.report.ClarityReport
import com.kamsiob.claritynow.domain.report.ReportSection
import com.kamsiob.claritynow.ui.theme.ClaritySpacing
import com.kamsiob.claritynow.ui.theme.LocalCalmMode
import com.kamsiob.claritynow.ui.theme.LocalClarityTypography
import com.kamsiob.claritynow.ui.theme.LocalContemplativeColors
import com.kamsiob.claritynow.ui.theme.ReportPalette
import com.kamsiob.claritynow.ui.theme.clarityMotion
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * The Clarity Report. `design-v3.md` 11.1.
 *
 * **Four treatments and no more than four**, because this is read 52 times a year and
 * anything clever becomes exhausting by the tenth reading. They are the faint controls,
 * type, the gold rule and the week ribbon. Everything else on the page is one of those four
 * at a different size. There is no card, no container, no chip, no badge, no icon beside a
 * sidehead, no second color and no colored edge on anything.
 *
 * The order below is 11.1's order, and the numbers in the comments are 11.1's numbers.
 *
 * ## Every sentence came from a corpus and every number from a query
 *
 * `CLAUDE.md` rule 8. The headline, the observations, the pattern, the first week note, the
 * generated line and the basis line are finished strings off [ClarityReport] and this file
 * never edits one. What it supplies is the sideheads, the eyebrow, the control labels and
 * the ribbon caption, which are fixed interface labels and direct readouts of queried
 * numbers, and they live in `strings.xml`.
 *
 * The ribbon caption is the only number this screen prints, and it prints it out of
 * `ClarityReport.numbers` rather than counting anything. See [ribbonCaption].
 *
 * ## TalkBack
 *
 * `design-v3.md` 13 asks for eyebrow, headline, then a spoken summary of the ribbon, then
 * sections. The page is a column in exactly that order. The controls are drawn over it and
 * carry a later traversal index, so a screen reader reaches the report first and the
 * toolbar after it, which is the order the specification names rather than the order the
 * layout draws in.
 */
@Composable
internal fun ReportScreen(
    state: ReportUiState,
    onHistory: () -> Unit,
    onRegenerate: () -> Unit,
    onRevealFinished: (String) -> Unit,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val contemplative = LocalContemplativeColors.current
    val type = LocalClarityTypography.current
    val motion = clarityMotion()
    val calm = LocalCalmMode.current
    val scroll = rememberScrollState()

    val page = state.page
    val labels = reportLabels(state.weekStart, page)

    // 8.2 item 12 and 8.4. A different report is different content and re-arms the reveal;
    // re-reading the same one does not. Calm mode removes the entrance outright, 16.2.
    val revealKey = state.revealKey
    val playing = revealKey != null && !calm
    LaunchedEffect(revealKey, playing, motion.reduced) {
        if (revealKey == null) return@LaunchedEffect
        if (playing) delay(totalRevealMillis(motion.reduced))
        onRevealFinished(revealKey)
    }

    Box(modifier = modifier.fillMaxSize().semantics { isTraversalGroup = true }) {
        // 11.1's last line: the background gradient extends under the status bar to the
        // very top edge, so it is drawn behind everything and takes no inset.
        ReportBackdrop()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scroll)
                .semantics { traversalIndex = CONTENT_TRAVERSAL }
                .padding(contentInsets()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (page != null) {
                // 2. One centered caption line, and one line only: a week that wrapped
                // would read as a heading rather than as a dateline.
                labels.eyebrow?.let { eyebrow ->
                    Text(
                        text = eyebrow,
                        style = type.caption,
                        color = contemplative.textDim,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        modifier = Modifier
                            .padding(horizontal = ClaritySpacing.screenPadding)
                            .reveal(playing, EYEBROW_AT),
                    )
                }

                when (page) {
                    is ReportPage.Composed -> ComposedReport(
                        page = page,
                        labels = labels,
                        regenerating = state.regenerating,
                        playing = playing,
                        onAccept = onAccept,
                        onDecline = onDecline,
                    )

                    is ReportPage.Empty -> ReportNotice(line = page.note?.text, detail = null)

                    ReportPage.Withheld -> ReportNotice(
                        line = stringResource(R.string.report_withheld),
                        detail = stringResource(R.string.report_withheld_detail),
                    )

                    is ReportPage.Unavailable -> ReportNotice(
                        line = stringResource(R.string.report_unavailable),
                        detail = stringResource(R.string.report_unavailable_detail),
                    )
                }
            }
        }

        // 1. Faint, top right, over the page rather than in it, so the eyebrow and the
        // headline keep the full measure and stay centered on it.
        ReportControls(
            onHistory = onHistory,
            onRegenerate = onRegenerate,
            onCopy = {
                val composed = page as? ReportPage.Composed
                if (composed != null) {
                    copyToClipboard(
                        context = context,
                        label = labels.clipboardLabel,
                        text = reportPlainText(
                            report = composed.report,
                            caption = labels.caption,
                            closing = composed.closing,
                            labels = labels.plainText,
                        ),
                    )
                }
            },
            regenerating = state.regenerating,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
                .padding(end = CONTROLS_END)
                .semantics { traversalIndex = CONTROLS_TRAVERSAL },
        )
    }
}

/**
 * Items 3 to 9, for a report that exists.
 *
 * Separate from the surface above so that the three states which are not a report cannot
 * acquire a ribbon, a gold rule or a footer by accident: each of those is emitted exactly
 * once, here.
 */
@Composable
private fun ComposedReport(
    page: ReportPage.Composed,
    labels: ScreenLabels,
    regenerating: Boolean,
    playing: Boolean,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
) {
    val contemplative = LocalContemplativeColors.current
    val type = LocalClarityTypography.current
    val report = page.report

    // 3. Generous space above and below, which is what makes the headline the page rather
    // than the top of it.
    Spacer(Modifier.height(HEADLINE_SPACE))

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ClaritySpacing.screenPadding)
            .revealHeadline(playing),
        contentAlignment = Alignment.Center,
    ) {
        if (regenerating) {
            HeadlinePlaceholder()
        } else {
            report.headline?.let { headline ->
                Text(
                    text = headline.rendered,
                    style = type.displayHero,
                    color = contemplative.textBright,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }

    Spacer(Modifier.height(HEADLINE_SPACE))

    // 4. The glance layer, with the caption that keeps it from being the sole carrier of a
    // claim. A week whose every total was nought draws neither: seven floor marks under an
    // empty caption is a picture of nothing.
    if (labels.caption != null) {
        WeekRibbon(
            days = page.ribbon,
            caption = labels.caption,
            reveal = playing,
            modifier = Modifier.reveal(playing, RIBBON_BLOCK_AT),
        )
        Spacer(Modifier.height(SECTION_GAP))
    }

    // 5.
    GoldRule(
        modifier = Modifier
            .padding(horizontal = ClaritySpacing.screenPadding)
            .reveal(playing, sectionAt(0), RISE),
    )

    // The step counter is the position in the arrival order rather than in any list, so the
    // whole page below the rule staggers as one sequence whatever it happens to contain.
    var step = 1

    report.firstWeekNote?.let { note ->
        Spacer(Modifier.height(SECTION_GAP))
        Text(
            text = note.text,
            style = type.bodySerif,
            color = contemplative.textDim,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(horizontal = ClaritySpacing.screenPadding)
                .reveal(playing, sectionAt(step), RISE),
        )
        step++
    }

    // 6. Each a sidehead followed by bodySerif prose, 28dp apart.
    for ((section, lines) in groupedSections(report.observations)) {
        Spacer(Modifier.height(SECTION_GAP))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = ClaritySpacing.screenPadding)
                .reveal(playing, sectionAt(step), RISE),
        ) {
            ReportSidehead(text = labels.plainText.sideheads.getValue(section))
            Spacer(Modifier.height(SIDEHEAD_GAP))
            lines.forEachIndexed { index, line ->
                if (index > 0) Spacer(Modifier.height(PARAGRAPH_GAP))
                Text(text = line.rendered, style = type.bodySerif, color = ReportPalette.body)
            }
        }
        step++
    }

    // 7. The one deliberate grid break, and the only element that escapes the measure.
    //
    // Two sources, one block. `report.pattern` is a pattern the engine selected, realized
    // and validated. `report.patternNote` is the section's empty state, shown under three
    // weeks of history, and it is a corpus line the Report renders directly: no rule, no
    // engine selection. That is an owner authorized exception to the rule that every
    // sentence comes through the engine, and it is narrow. `insufficientData` says there
    // are not three weeks of snapshots yet. It names nothing, counts nothing and claims
    // nothing about the person, so it is an empty state rather than an observation, in the
    // same family as `Nothing to report yet`. It has still been through layer 5.
    // `ReportComposer.patternNote` holds the full reasoning. Do not move it into the
    // engine: the composer only asks for a pattern when there are three weeks, so a rule
    // for this could never fire, which is how it got here.
    //
    // The two are complements and never both present, so the section is drawn once.
    (report.pattern?.rendered ?: report.patternNote?.text)?.let { line ->
        Spacer(Modifier.height(SECTION_GAP))
        PatternBreak(
            sidehead = labels.plainText.patternSidehead,
            line = line,
            modifier = Modifier.reveal(playing, sectionAt(step), RISE),
        )
        step++
    }

    // 8. Absent until layer six lands. See ReportClosing.
    page.closing?.let { closing ->
        Spacer(Modifier.height(CLOSING_SPACE))
        ClosingLine(
            eyebrow = labels.plainText.closingEyebrow,
            closing = closing,
            onAccept = onAccept,
            onDecline = onDecline,
            modifier = Modifier
                .padding(horizontal = ClaritySpacing.screenPadding)
                .reveal(playing, sectionAt(step), RISE),
        )
        step++
    }

    // 9. Both lines in textFaint at caption, and both out of the corpus.
    Spacer(Modifier.height(CLOSING_SPACE))
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ClaritySpacing.screenPadding)
            .reveal(playing, sectionAt(step), RISE),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(FOOTER_GAP),
    ) {
        report.generated?.let {
            Text(
                text = it.text,
                style = type.caption,
                color = contemplative.textFaint,
                textAlign = TextAlign.Center,
            )
        }
        report.basis?.let {
            Text(
                text = it.rendered,
                style = type.caption,
                color = contemplative.textFaint,
                textAlign = TextAlign.Center,
            )
        }
    }
}

/**
 * The empty state, the withheld state and the unavailable state, which share a shape and
 * mean three different things.
 *
 * `design-v3.md` 8.2 item 25: fades in over 400ms after a 150ms delay, so it never flashes
 * during a load that resolves quickly. The delay is kept under reduce motion and in calm
 * mode, per 8.4, because a hold is not motion and removing it would reintroduce the flash
 * it exists to prevent.
 *
 * [line] is a corpus line for the empty state and a fixed interface string for the other
 * two, and that difference is why they are separate states. `Nothing to report yet` is a
 * sentence about somebody's week and comes out of `CORPUS_2_REPORT.md` 6.1 like every other
 * one; the other two are the app saying something about itself, which `design-v3.md` 13.1
 * puts in `strings.xml` and requires to be one sentence about what went wrong and no
 * verdict on anybody.
 */
@Composable
private fun ReportNotice(line: String?, detail: String?) {
    val contemplative = LocalContemplativeColors.current
    val type = LocalClarityTypography.current
    val motion = clarityMotion()

    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(EMPTY_DELAY_MILLIS)
        shown = true
    }
    val appearance by animateFloatAsState(
        targetValue = if (shown) 1f else 0f,
        animationSpec = tween(
            durationMillis = if (motion.reduced) REDUCED_MILLIS else EMPTY_FADE_MILLIS,
            easing = EaseOutCubic,
        ),
        label = "reportNotice",
    )

    Spacer(Modifier.height(HEADLINE_SPACE))
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ClaritySpacing.screenPadding)
            .graphicsLayer { alpha = appearance },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(PARAGRAPH_GAP),
    ) {
        line?.let {
            Text(
                text = it,
                style = type.readSerif,
                color = contemplative.textBright,
                textAlign = TextAlign.Center,
            )
        }
        detail?.let {
            Text(
                text = it,
                style = type.body,
                color = contemplative.textDim,
                textAlign = TextAlign.Center,
            )
        }
    }
}

/** Every fixed string this screen needs, resolved once. */
@Immutable
internal data class ScreenLabels(
    val eyebrow: String?,
    val caption: String?,
    val clipboardLabel: String,
    val plainText: ReportLabels,
)

@Composable
private fun reportLabels(weekStart: LocalDate?, page: ReportPage?): ScreenLabels {
    val eyebrow = weekStart?.let { stringResource(R.string.report_eyebrow, weekLabel(it)) }
    val sideheads = mapOf(
        ReportSection.YOUR_WEEK to stringResource(R.string.report_sidehead_your_week),
        ReportSection.WHAT_YOU_SAID to stringResource(R.string.report_sidehead_what_you_said),
        ReportSection.FOCUS to stringResource(R.string.report_sidehead_focus),
    )
    val patternSidehead = stringResource(R.string.report_sidehead_pattern)
    val closingEyebrow = stringResource(R.string.report_closing_eyebrow)
    val clipboardLabel = stringResource(R.string.report_clipboard_label)
    val caption = (page as? ReportPage.Composed)?.let { ribbonCaption(it.report) }

    return ScreenLabels(
        eyebrow = eyebrow,
        caption = caption,
        clipboardLabel = clipboardLabel,
        plainText = ReportLabels(
            eyebrow = eyebrow,
            // Every section that can reach the page has an entry, and it is read with
            // `getValue` rather than with a fallback: a fourth section added to the enum
            // should fail here, loudly, rather than render a sidehead with no words in it.
            sideheads = sideheads,
            patternSidehead = patternSidehead,
            closingEyebrow = closingEyebrow,
        ),
    )
}

/**
 * The caption, with each number read out of the report's own consistency map.
 *
 * `ClarityReport.numbers` is `CLARITY_LOGIC_ENGINE.md` 9.2's map of every rendered numeric
 * slot in the whole report against the `FactRef` behind it, and the caption reads it rather
 * than the totals list. **The difference matters exactly once**: when a corpus line above
 * has already stated the same fact. The map keeps the first value entered under a
 * reference, so the caption then repeats the number the prose used instead of stating a
 * second one for the same fact, which is the failure 9.2 exists to catch. The two can only
 * differ if something is wrong, and if something is wrong the page must not say it twice,
 * two different ways.
 *
 * A total of nought never arrives here at all: `Measures` answers null for nought exactly
 * as it does for a corpus slot, so the composer never built a `ReportTotal` for it and a
 * caption reading `0 completed` cannot occur.
 */
@Composable
private fun ribbonCaption(report: ClarityReport): String? = ribbonCaption(
    report.totals.map { total -> total.measure to (report.numberFor(total.ref) ?: total.value) },
)

/**
 * `Week of July 13`'s date half.
 *
 * A direct readout of the day the window starts on, formatted in the reader's own locale.
 * The pattern carries no year: 11.1 gives the eyebrow one line, and a report is read the
 * week it is written.
 */
@Composable
private fun weekLabel(weekStart: LocalDate): String {
    val locale = Locale.getDefault()
    val formatter = remember(locale) { DateTimeFormatter.ofPattern(WEEK_PATTERN, locale) }
    return remember(weekStart, formatter) { formatter.format(weekStart) }
}

/**
 * What the page keeps clear of the system bars and the tab bar.
 *
 * The top is the status bar plus room for the controls that float over it, so the eyebrow
 * begins below them rather than under them. The bottom clears the floating tab bar, which
 * `design-v3.md` 10.15 has content pass beneath rather than stop at.
 */
@Composable
private fun contentInsets(): PaddingValues = PaddingValues(
    top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() +
        ClaritySpacing.minTouchTarget + CONTENT_TOP,
    bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() +
        ClaritySpacing.tabBarHeight + ClaritySpacing.tabBarInset + CONTENT_BOTTOM,
)

private const val WEEK_PATTERN = "MMMM d"

/** 13's traversal order: the report, then the toolbar over it. */
private const val CONTENT_TRAVERSAL = 0f
private const val CONTROLS_TRAVERSAL = 1f

/** 11.1 item 3. The air above and below the headline. */
private val HEADLINE_SPACE = 36.dp

/** 11.1 item 6, and `ClaritySpacing.sectionGap`, which is the same 28dp. */
private val SECTION_GAP = ClaritySpacing.sectionGap

/** 11.1 item 8. Thirty four dp above the closing line, and above the footer with it. */
private val CLOSING_SPACE = 34.dp

private val PARAGRAPH_GAP = 14.dp
private val FOOTER_GAP = 6.dp
private val CONTENT_TOP = 10.dp
private val CONTENT_BOTTOM = 40.dp
private val CONTROLS_END = 8.dp

/** 8.2 item 25. */
private const val EMPTY_DELAY_MILLIS = 150L
private const val EMPTY_FADE_MILLIS = 400
