package com.kamsiob.claritynow.ui.momentum

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.kamsiob.claritynow.R
import com.kamsiob.claritynow.domain.momentum.AreaBalance
import com.kamsiob.claritynow.domain.momentum.AreaShare
import com.kamsiob.claritynow.domain.momentum.CompletionPace
import com.kamsiob.claritynow.domain.momentum.FocusPattern
import com.kamsiob.claritynow.domain.momentum.IdleArea
import com.kamsiob.claritynow.domain.momentum.MomentumInsights
import com.kamsiob.claritynow.ui.components.Sidehead
import com.kamsiob.claritynow.ui.theme.ClaritySpacing
import com.kamsiob.claritynow.ui.theme.LocalClarityColors
import com.kamsiob.claritynow.ui.theme.LocalClarityTypography

/**
 * The four insight modules. `MASTER_BUILD_PROMPT.md` 12.2, design-v3.md section 11 and
 * 10.12.
 *
 * **A module with no data is absent, not empty.** The composer decides that and hands a
 * null; nothing here draws a placeholder, a zero or a "not enough data yet" card. 11.4:
 * never pad a section to reach a minimum.
 *
 * ## The two drawn graphics
 *
 * The sparkline and the heat strip are the only non text elements on this screen, and both
 * follow the constraints design-v3.md 11.1 puts on the Report's week ribbon, which is the
 * one graphic this design system has fully specified: no axes, no gridlines, no values on
 * the marks, no card around it, no gradient, and a caption beneath stating the number.
 *
 * **Both are drawn in ink and never in an area color.** design-v3.md 3.4 permits an area
 * accent in four forms and a data mark is none of them, and section 14 forbids a colored
 * bar treatment outright. The one place an area color appears in these modules is the 7dp
 * identity dot beside a name, which is the first of the four.
 *
 * **Neither carries red.** 12.2 asks for the idle module in particular to be gentle and to
 * use no red, and there is no red anywhere on this screen to reach for: the Daylight
 * palette in 3.1 has no red token at all, and `deleteMuted` is scoped to one job.
 */
@Composable
fun MomentumInsightModules(insights: MomentumInsights) {
    if (!insights.any) return

    insights.areaBalance?.let { balance ->
        SectionGap()
        AreaBalanceModule(balance)
    }
    insights.completionPace?.let { pace ->
        SectionGap()
        CompletionPaceModule(pace)
    }
    insights.focusPattern?.let { pattern ->
        SectionGap()
        FocusPatternModule(pattern)
    }
    insights.idleAreas?.let { areas ->
        SectionGap()
        IdleAreasModule(areas)
    }
}

/**
 * Each area's share of the fortnight, busiest first.
 *
 * **Typography and a dot, with no bar behind the numbers.** The obvious rendering of a
 * share is a horizontal bar per row, and it is forbidden three times over: design-v3.md
 * 3.4 ends "never as a stripe, bar, edge, border or filled block", section 14 repeats it
 * as the rule about colored bars, and 15.3 names the same fix as a refusal. The percentage
 * is the whole of the information and a row of numbers is legible without a chart behind
 * it.
 *
 * The percentage reads `78 percent` rather than `78%`, which is `CLARITY_LOGIC_ENGINE.md`
 * 7.2's rule for a number in a sentence, applied here so the app renders a percentage one
 * way rather than two.
 *
 * **The caption beneath names the denominator, and it is not decoration.** The shares do
 * not sum to a hundred: `AreaFacts.shareOfEvents` divides by every user activity event in
 * the fortnight, and answering a Pulse, changing a setting or writing into the unfiled
 * inbox belongs to no area. A device check found two areas reading 64 and 21 percent with
 * nothing on the screen accounting for the other fifteen points, and a person reading two
 * rows adds them.
 *
 * **The denominator was not changed to make the column sum**, which is the obvious fix.
 * The headline above this module can state the same percentage about the same area through
 * the engine's `areaShare` measure, and 11.4 gives one fact exactly one number, so a module
 * dividing by a different total would disagree with the sentence above it. The caption is
 * a legend for a readout rather than an observation, so it lives in `strings.xml` under
 * 11.2 and does not go through the engine.
 *
 * It is the same job the sparkline's caption and the ribbon's caption already do: give the
 * figure above it a scale.
 */
@Composable
private fun AreaBalanceModule(balance: AreaBalance) {
    val colors = LocalClarityColors.current
    val type = LocalClarityTypography.current

    Sidehead(text = stringResource(R.string.momentum_sidehead_area_balance))
    Spacer(Modifier.height(ClaritySpacing.scaled(ClaritySpacing.snug)))
    Column(verticalArrangement = Arrangement.spacedBy(ClaritySpacing.scaled(12.dp))) {
        balance.shares.forEach { share ->
            InsightRow(
                colorHex = share.colorHex,
                name = share.name,
                trailing = stringResource(R.string.momentum_percent, share.percent),
            )
        }
    }
    Spacer(Modifier.height(ClaritySpacing.scaled(10.dp)))
    Text(
        text = stringResource(R.string.momentum_area_balance_basis, balance.total),
        style = type.caption,
        color = colors.inkSecondary,
    )
}

/**
 * The eight week completion sparkline. 12.2.
 *
 * A line and nothing else: no axis, no baseline, no dots on every point, no fill under it.
 * The only emphasis is a small mark on the newest week, so the eye knows which end is now
 * without a label saying so. The caption beneath states the busiest week's figure, which
 * is what gives the line a scale and is the same job 11.1 gives the ribbon's caption.
 */
@Composable
private fun CompletionPaceModule(pace: CompletionPace) {
    val colors = LocalClarityColors.current
    val type = LocalClarityTypography.current
    val stroke = with(LocalDensity.current) { GRAPHIC_STROKE.toPx() }
    val description = stringResource(R.string.cd_momentum_pace, pace.weeks.size)

    Sidehead(text = stringResource(R.string.momentum_sidehead_completion_pace))
    Spacer(Modifier.height(ClaritySpacing.scaled(ClaritySpacing.snug)))
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(GRAPHIC_HEIGHT)
            .semantics { contentDescription = description },
    ) {
        val busiest = pace.busiestWeek.coerceAtLeast(1)
        val inset = stroke
        val usableHeight = size.height - inset * 2f
        val step = if (pace.weeks.size > 1) size.width / (pace.weeks.size - 1) else 0f
        val points = pace.weeks.mapIndexed { index, value ->
            Offset(
                x = (index * step).coerceIn(inset, size.width - inset),
                y = inset + usableHeight * (1f - value.toFloat() / busiest),
            )
        }
        val path = Path().apply {
            points.forEachIndexed { index, point ->
                if (index == 0) moveTo(point.x, point.y) else lineTo(point.x, point.y)
            }
        }
        drawPath(
            path = path,
            color = colors.inkSecondary,
            style = Stroke(width = stroke, cap = StrokeCap.Round),
        )
        points.lastOrNull()?.let { newest ->
            drawCircle(color = colors.inkPrimary, radius = stroke * 1.6f, center = newest)
        }
    }
    Spacer(Modifier.height(ClaritySpacing.scaled(10.dp)))
    Text(
        text = stringResource(R.string.momentum_pace_busiest, pace.busiestWeek),
        style = type.caption,
        color = colors.inkSecondary,
    )
}

/**
 * Seven days of focus, as a heat strip. 12.2.
 *
 * One cell per local day, oldest on the left, shaded by that day's minutes against the
 * busiest of the seven. A day with no focus keeps its cell at a floor rather than
 * disappearing, for the reason design-v3.md section 11 gives the Pulse's silent mark:
 * below a floor a mark stops being quiet and starts being absent, and a week of absent
 * cells reads as a broken strip rather than a calm one.
 */
@Composable
private fun FocusPatternModule(pattern: FocusPattern) {
    val colors = LocalClarityColors.current
    val type = LocalClarityTypography.current
    val gap = with(LocalDensity.current) { STRIP_GAP.toPx() }
    val radius = with(LocalDensity.current) { 4.dp.toPx() }
    val description = stringResource(R.string.cd_momentum_focus_strip, pattern.days.size)

    Sidehead(text = stringResource(R.string.momentum_sidehead_focus_patterns))
    Spacer(Modifier.height(ClaritySpacing.scaled(ClaritySpacing.snug)))
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(GRAPHIC_HEIGHT)
            .semantics { contentDescription = description },
    ) {
        val count = pattern.days.size
        if (count == 0) return@Canvas
        val cell = (size.width - gap * (count - 1)) / count
        pattern.days.forEachIndexed { index, day ->
            drawRoundRect(
                color = markInk(
                    base = colors.inkPrimary,
                    value = day.minutes,
                    busiest = pattern.busiestDay,
                    floor = MARK_FLOOR_ALPHA,
                    ceiling = MARK_CEILING_ALPHA,
                ),
                topLeft = Offset(x = index * (cell + gap), y = 0f),
                size = Size(width = cell, height = size.height),
                cornerRadius = CornerRadius(radius, radius),
            )
        }
    }
    Spacer(Modifier.height(ClaritySpacing.scaled(10.dp)))
    Text(text = focusMinutesLine(pattern.minutes), style = type.caption, color = colors.inkSecondary)
}

/**
 * Areas with nothing in them for seven days or more. 12.2: only at seven or more days,
 * gentle, and no red.
 *
 * **The sidehead reads `Quiet areas` and 12.2 calls the module Idle Areas.** That is a
 * deliberate change to the label and not to the module, and 12.2's own word for what the
 * label has to be is the reason: gentle. `Idle` is what the state is called in the code and
 * on the area card, and set as a heading over a person's own list it reads as a verdict on
 * them rather than a description of the fortnight. `Quiet` is the word the corpus itself
 * uses for the same shape, in `mo.quiet` and `bn.quiet`, so the screen and the sentences
 * above it name it the same way.
 *
 * The line beside each name is the same `Last active N days ago` the area card carries in
 * design-v3.md 10.3, out of the same plurals resource, so one fact is worded one way
 * wherever it appears.
 */
@Composable
private fun IdleAreasModule(areas: List<IdleArea>) {
    Sidehead(text = stringResource(R.string.momentum_sidehead_quiet_areas))
    Spacer(Modifier.height(ClaritySpacing.scaled(ClaritySpacing.snug)))
    Column(verticalArrangement = Arrangement.spacedBy(ClaritySpacing.scaled(12.dp))) {
        areas.forEach { area ->
            InsightRow(
                colorHex = area.colorHex,
                name = area.name,
                trailing = pluralStringResource(
                    R.plurals.area_last_active_days,
                    area.daysIdle,
                    area.daysIdle,
                ),
            )
        }
    }
}
