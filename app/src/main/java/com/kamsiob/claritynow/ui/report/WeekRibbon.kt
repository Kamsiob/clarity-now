package com.kamsiob.claritynow.ui.report

import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kamsiob.claritynow.R
import androidx.compose.material3.Text
import com.kamsiob.claritynow.ui.theme.LocalCalmMode
import com.kamsiob.claritynow.ui.theme.LocalClarityTypography
import com.kamsiob.claritynow.ui.theme.LocalContemplativeColors
import com.kamsiob.claritynow.ui.theme.ReportPalette
import com.kamsiob.claritynow.ui.theme.calmed
import com.kamsiob.claritynow.ui.theme.clarityMotion
import java.time.format.TextStyle as DayNameStyle
import java.util.Locale

/**
 * The week ribbon. `design-v3.md` 11.1 item 4, 8.2 item 12 and section 13.
 *
 * Seven vertical marks, one per day, 5dp wide at 2dp radius, height and opacity scaled to
 * that day's activity against the week's busiest. Gold. Day initials beneath at 8sp
 * `textFaint`. Below it one caption line reading the three headline numbers.
 *
 * **No axes, no gridlines, no values on the marks, no card around it, no gradient**, and it
 * is the only non-text element in the entire report. It repeats at [SMALL_SCALE] in the
 * past reports list and nowhere else.
 *
 * ## Every number here came from a query
 *
 * The marks are `TrailQueries.eventsPerDay` and the caption is the three totals the
 * composer already put in `ClarityReport.numbers` against their `FactRef`. Neither counts
 * anything for itself, and [caption] arrives already rendered because assembling it needs
 * resources, which this package can reach and `domain` cannot.
 *
 * ## The ribbon is never the sole carrier of a claim
 *
 * `design-v3.md` 13. The picture says which days were busy in relation to each other and
 * says it in a form a screen reader cannot see, so the caption beneath states the numbers
 * as text and [ribbonDescriptionOf] names the busiest day aloud. [caption] is
 * therefore not optional in the sense of a nicety: a ribbon drawn with no line beneath it
 * is a claim nothing else carries, and the only case that happens is a week whose every
 * total was nought, where the ribbon is not drawn at all.
 *
 * ## This is not a streak
 *
 * Each mark is drawn from its own day's count and knows nothing about the day beside it. A
 * quiet day is a shorter, fainter mark and nothing else: no break in the row, no dimming of
 * what follows it, no separator, no color that reads as a lapse. `design-v3.md` 14 and
 * `CLARITY_LOGIC_ENGINE.md` 3.1, which has no streak fact to reconstruct one from.
 *
 * ## The three numbers this composable had to choose, which v3 leaves open
 *
 * v3 gives the mark's width and radius and says the height and opacity are scaled against
 * the busiest day. It does not say what a day with nothing in it looks like, and the
 * obvious answer is nought: proportional height from zero, which draws an empty Tuesday as
 * no mark at all. That is the reading this rejects. A row with a hole in it reads as broken
 * rather than as calm, and it turns the absence of activity into the loudest thing on the
 * page, which is the one thing this app never does with a quiet day. So both scales run
 * from a floor:
 *
 * - **Height from [MARK_MIN_HEIGHT] to [MARK_MAX_HEIGHT]**, linearly. Nought is the floor
 * - **Opacity from [QUIET_ALPHA] to full**, linearly. The floor is not a taste: gold at
 *   half strength measures 3.0 to one against `deepBlack`, which is the ratio
 *   `design-v3.md` 16.7 holds a graphic to, and it is the same floor the Pulse rhythm row
 *   arrived at for the same reason. Below it a mark stops being quiet and starts being
 *   absent
 * - **A fixed [MARK_GAP] rather than marks distributed across the measure.** v3 asks for
 *   the ribbon to repeat at 60 percent scale, which only means something if it has a size
 *   of its own; a row stretched to whatever width it is given cannot be scaled, only
 *   re-flowed
 *
 * The scaling itself is linear and deliberately not curved. v3 says the height is that
 * day's activity against the week's busiest, and a square root would make a quiet day look
 * busier than it was, which is a flattering lie rather than an unobvious answer.
 *
 * ## The entrance, 8.2 item 12
 *
 * The marks draw left to right at [DAY_MILLIS] per day, growing from the baseline. Under
 * reduce motion 8.3 says the ribbon appears complete, and in calm mode 16.2 says the
 * entrance does not fire at all, so both render it already drawn. [reveal] is false on
 * every re-read of a report already seen, per 8.4's content exception.
 */
@Composable
internal fun WeekRibbon(
    days: List<RibbonDay>,
    caption: String?,
    reveal: Boolean,
    modifier: Modifier = Modifier,
    scale: Float = 1f,
) {
    val calm = LocalCalmMode.current
    val motion = clarityMotion()
    val gold = ReportPalette.gold.calmed(calm)
    val contemplative = LocalContemplativeColors.current
    val type = LocalClarityTypography.current
    val initials = stringArrayResource(R.array.report_day_initials)
    val description = ribbonDescriptionOf(days)

    val busiest = days.maxOfOrNull { it.count } ?: 0
    val plays = reveal && !calm && !motion.reduced

    var drawn by remember(days) { mutableStateOf(!plays) }
    LaunchedEffect(days, plays) { drawn = true }

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(MARK_GAP * scale),
            verticalAlignment = Alignment.Top,
            modifier = Modifier.clearAndSetSemantics { contentDescription = description },
        ) {
            days.forEachIndexed { index, day ->
                val fraction = if (busiest <= 0) 0f else day.count.toFloat() / busiest
                val progress by animateFloatAsState(
                    targetValue = if (drawn) 1f else 0f,
                    animationSpec = tween(
                        durationMillis = if (plays) MARK_MILLIS else 0,
                        delayMillis = if (plays) index * DAY_MILLIS else 0,
                        easing = EaseOutCubic,
                    ),
                    label = "ribbonMark",
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Canvas(
                        modifier = Modifier.size(
                            width = MARK_WIDTH * scale,
                            height = MARK_MAX_HEIGHT * scale,
                        ),
                    ) {
                        val full = (MARK_MIN_HEIGHT + (MARK_MAX_HEIGHT - MARK_MIN_HEIGHT) * fraction)
                            .toPx() * scale
                        val drawnHeight = full * progress
                        if (drawnHeight <= 0f) return@Canvas
                        val radius = (MARK_RADIUS.toPx() * scale).coerceAtMost(drawnHeight / 2f)
                        drawRoundRect(
                            color = gold,
                            alpha = QUIET_ALPHA + (1f - QUIET_ALPHA) * fraction,
                            topLeft = Offset(0f, size.height - drawnHeight),
                            size = Size(size.width, drawnHeight),
                            cornerRadius = CornerRadius(radius, radius),
                        )
                    }

                    // The initials are dropped rather than shrunk at the small scale. 8sp is
                    // already this design's smallest type and 60 percent of it is 4.8sp,
                    // which section 13 would not accept from any other label on any other
                    // screen. The row it repeats in carries the week in words, so nothing
                    // is lost that the reader cannot read somewhere else on the same line.
                    if (scale >= 1f) {
                        Spacer(Modifier.height(INITIAL_GAP))
                        Text(
                            text = initials.getOrElse(day.date.dayOfWeek.value - 1) { "" },
                            style = type.caption.copy(fontSize = INITIAL_SIZE),
                            color = contemplative.textFaint,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }

        if (caption != null) {
            Spacer(Modifier.height(if (scale >= 1f) CAPTION_GAP else CAPTION_GAP_SMALL))
            Text(
                text = caption,
                style = type.caption,
                color = contemplative.textDim,
                textAlign = TextAlign.Center,
            )
        }
    }
}

/**
 * What TalkBack hears in place of the ribbon. `design-v3.md` 13.
 *
 * 13 asks for "a spoken summary of the ribbon", and gives "busiest on Wednesday, quiet at
 * the weekend" as the example. **Half of that example is built and half of it deliberately
 * is not.** The busiest day is the argument of a maximum over a query result and is exactly
 * what the tallest mark already says to somebody who can see it, so naming it aloud is a
 * direct readout of a queried fact and lives in `strings.xml` like the Momentum screen's
 * `Busiest week, {n} completed`. "Quiet at the weekend" is a characterization of somebody's
 * week, and a characterization is an observation: `CLAUDE.md` rule 8 and
 * `MASTER_BUILD_PROMPT.md` 11.4 put every one of those in a corpus, through the engine, and
 * a content description is not an exception to that.
 *
 * A week with nothing in it has no busiest day, and the description then names the element
 * and stops rather than reaching for a superlative over seven zeroes.
 */
@Composable
internal fun ribbonDescriptionOf(days: List<RibbonDay>): String {
    val busiest = days.filter { it.count > 0 }.maxByOrNull { it.count }
    return if (busiest == null) {
        stringResource(R.string.cd_report_ribbon, days.size)
    } else {
        stringResource(
            R.string.cd_report_ribbon_busiest,
            days.size,
            busiest.date.dayOfWeek.getDisplayName(DayNameStyle.FULL, Locale.getDefault()),
        )
    }
}

/**
 * The one caption line beneath the ribbon. `design-v3.md` 11.1 item 4 and section 13.
 *
 * [values] are measure ids against the numbers already stated for them, in the order the
 * caption reads them, which is `ReportLanguage.CAPTION_MEASURES`. **Nothing here counts
 * anything**: the caller reads the value out of `ClarityReport.numbers`, which is
 * `CLARITY_LOGIC_ENGINE.md` 9.2's map of every rendered numeric slot against the fact
 * behind it, or off the fact snapshot a past report recorded.
 *
 * An empty list is no caption at all, and a week with no caption gets no ribbon either,
 * because 13 forbids the picture carrying a claim on its own.
 *
 * The clauses are joined with a comma. That is punctuation between three readouts rather
 * than a sentence being assembled: each clause is a whole string resource with one number
 * in it, and there is no wording here that somebody did not write in `strings.xml`.
 */
@Composable
internal fun ribbonCaption(values: List<Pair<String, Int>>): String? {
    val clauses = values.mapNotNull { (measure, value) ->
        when (measure) {
            MEASURE_EVENTS -> pluralStringResource(R.plurals.report_total_events, value, value)
            MEASURE_COMPLETIONS -> stringResource(R.string.report_total_completed, value)
            MEASURE_ADDITIONS -> stringResource(R.string.report_total_added, value)
            else -> null
        }
    }
    return clauses.takeIf { it.isNotEmpty() }?.joinToString(CLAUSE_SEPARATOR)
}

/**
 * The measure ids the caption reads, which are `ReportLanguage.CAPTION_MEASURES`.
 *
 * Named here so a measure renamed in `Measures` drops a clause out of the caption rather
 * than crashing the page, and so a reader can see which three without opening another file.
 * Which three, and why not the obvious three, is on `ReportTotal`.
 */
private const val MEASURE_EVENTS = "totalEvents"
private const val MEASURE_COMPLETIONS = "completions"
private const val MEASURE_ADDITIONS = "additions"

/** Punctuation between readouts, which is why it is not a string resource. */
private const val CLAUSE_SEPARATOR = ", "

/** `design-v3.md` 11.1 item 4. */
private val MARK_WIDTH = 5.dp
private val MARK_RADIUS = 2.dp

/** 11.1 asks for 8sp. `caption` is 12sp, so the size is overridden and nothing else is. */
private val INITIAL_SIZE = 8.sp

/**
 * The two ends of the height scale. See the note above for why the floor is not nought.
 *
 * 44dp is a graphic with presence on a page whose other elements are all type, and it is
 * short of the height at which seven marks would start to read as a chart. Both sit on the
 * 4dp grid section 6 lays the app out on.
 */
private val MARK_MIN_HEIGHT = 8.dp
private val MARK_MAX_HEIGHT = 44.dp

/**
 * Half strength for a day with nothing in it, which measures 3.0 to one against
 * `deepBlack`. `design-v3.md` 16.7's floor for a graphic.
 */
private const val QUIET_ALPHA = 0.5f

/**
 * The space between two marks.
 *
 * Seven 5dp marks at this gap make the ribbon 143dp across, which sits comfortably inside
 * the measure on the narrowest phone this app supports and leaves each day a column wide
 * enough for its initial. It is a fixed distance rather than a share of the width so that
 * the whole element can be scaled, per 11.1.
 */
private val MARK_GAP = 18.dp

private val INITIAL_GAP = 7.dp
private val CAPTION_GAP = 14.dp
private val CAPTION_GAP_SMALL = 8.dp

/** 8.2 item 12. Seven days at this stagger is 270ms of draw. */
private const val DAY_MILLIS = 45

/** How long one mark takes to grow, once its turn arrives. */
private const val MARK_MILLIS = 180

/** 11.1. The ribbon repeats at 60 percent in the past reports list and nowhere else. */
const val SMALL_SCALE = 0.6f
