package com.kamsiob.claritynow.ui.momentum

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kamsiob.claritynow.R
import com.kamsiob.claritynow.domain.momentum.ActivityWindow
import com.kamsiob.claritynow.domain.momentum.MomentumView
import com.kamsiob.claritynow.domain.momentum.WeekStat
import com.kamsiob.claritynow.domain.momentum.WeekStatKind
import com.kamsiob.claritynow.ui.components.ScrollEdge
import com.kamsiob.claritynow.ui.components.Sidehead
import com.kamsiob.claritynow.ui.components.TabBarHeight
import com.kamsiob.claritynow.ui.components.TabBarInset
import com.kamsiob.claritynow.ui.components.TabularNumber
import com.kamsiob.claritynow.ui.components.scrollEdgeFade
import com.kamsiob.claritynow.ui.theme.ClaritySpacing
import com.kamsiob.claritynow.ui.theme.LocalClarityColors
import com.kamsiob.claritynow.ui.theme.LocalClarityTypography
import com.kamsiob.claritynow.ui.theme.parseAreaColor

/** design-v3.md section 6. Section spacing on the Daylight world's calmest screen. */
private val SECTION_SPACING = 28.dp

/** design-v3.md section 6. Momentum tiles are 11dp, and this is the only place they exist. */

/**
 * Three columns of tiles, not four and not a scrolling row.
 *
 * **An open choice, recorded.** design-v3.md section 11 asks for area tiles and gives them
 * a corner radius and nothing else. The obvious answers are a horizontally scrolling row,
 * which hides areas off the edge of the one screen whose job is to show all of them at
 * once, and a four column grid, which makes the tile small enough that section 3.4's "one
 * place where an area color gets real presence" stops being true. Three columns at the
 * 20dp screen padding gives a tile wide enough to read as a block of the person's own
 * color, and five areas, which design-v3.md section 11 calls a comfortable screenful, fill
 * two rows with the second one short, which is what a mosaic looks like rather than what a
 * table looks like.
 */


/**
 * Momentum. design-v3.md section 11: "Daylight, the calmest screen in that world. Headline
 * in readSerif, the 14 dot row, area tiles, three stats as pure typography with no cards,
 * then insight modules under sideheads."
 *
 * ## What is on this screen and where each part came from
 *
 * One sentence, from the engine, through the corpus, at the top. Everything under it is a
 * number the log was asked for, drawn as a dot, a tile, a figure or a mark, with its label
 * out of `strings.xml`. `CLAUDE.md` rule 8 draws exactly that line and this screen is where
 * it is easiest to cross: a caption reading "a strong week" under a figure would be an
 * observation written in a composable, and there is not one anywhere below.
 *
 * ## Momentum observes and never interprets
 *
 * `CLARITY_LOGIC_ENGINE.md` 6.5, and it governs the screen as well as the corpus. Nothing
 * here says why. The dot row does not explain the gaps, the tiles do not rank the areas,
 * the stats carry no comparison against last week and the insight modules state a shape
 * and stop. The vocabulary that would do otherwise, because, suggests, means, belongs to
 * the Report, and a test over the real corpus holds the language half of the same line.
 *
 * ## No streak, structurally
 *
 * The dot row is handed fourteen independent days and nothing that relates them, and
 * design-v3.md 14 says what a gap looks like: "a gap is rendered as a lighter dot with
 * nothing said about it anywhere". So an inactive day is drawn smaller and lighter, it has
 * no content description of its own, and there is no run length on the screen or in the
 * value behind it.
 */
@Composable
fun MomentumScreen(view: MomentumView, modifier: Modifier = Modifier) {
    val colors = LocalClarityColors.current
    val type = LocalClarityTypography.current
    val scroll = rememberScrollState()

    val statusBar = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navigationBar = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.canvas)
            // Phase 12b. The headline dissolves into the page as it goes under the clock,
            // and the dot row does the same as it sinks behind the floating pill, rather
            // than either being cut off at a hard edge. The fade sits outside the scroll
            // so it stays put while the content moves through it, and outside the
            // background so there is something for it to reveal. `ScrollEdge.kt`.
            .scrollEdgeFade(
                top = statusBar + ScrollEdge.underTheClock,
                bottom = navigationBar + TabBarInset + TabBarHeight + ScrollEdge.aboveTheBar,
            )
            .verticalScroll(scroll)
            .padding(
                start = ClaritySpacing.screenPadding,
                end = ClaritySpacing.screenPadding,
                top = statusBar + 20.dp,
                bottom = navigationBar + TabBarHeight + TabBarInset + 24.dp,
            ),
    ) {
        // The headline, and nothing in its place when the engine was silent. A fixed
        // sentence standing here would be the second path MASTER_BUILD_PROMPT 11.1 forbids.
        view.headline?.let { headline ->
            Text(text = headline, style = type.readSerif, color = colors.inkPrimary)
            Spacer(Modifier.height(ClaritySpacing.scaled(SECTION_SPACING)))
        }

        ActivityRow(view.activity)

        Spacer(Modifier.height(ClaritySpacing.scaled(SECTION_SPACING)))
        ThisWeek(view, dimmed = view.isEmpty)

        MomentumInsightModules(view.insights)
    }
}

// --------------------------------------------------------------------- the dots

/**
 * The fourteen dot row and the readout beneath it. `MASTER_BUILD_PROMPT.md` 12.2.
 *
 * **The readout sits under the dots rather than over them**, which is the deliberate
 * choice rather than the obvious one, per design-v3.md 15. A label above a graphic makes
 * the graphic an illustration of the label. Under it, the row reads first as a texture and
 * the sentence confirms what the eye already got, which is the arrangement design-v3.md
 * 11.1 gives the Report's week ribbon: marks, then one caption line stating the numbers.
 * That is also what section 13 asks for, since the caption rather than the row is what
 * carries the claim aloud.
 *
 * The row itself is one node to a screen reader, named rather than tallied, exactly as the
 * Pulse rhythm row is: a spoken count of the gaps would be saying something about them,
 * and design-v3.md 14 says nothing is said about them anywhere.
 */
@Composable
private fun ActivityRow(activity: ActivityWindow) {
    val colors = LocalClarityColors.current
    val type = LocalClarityTypography.current
    val description = stringResource(R.string.cd_momentum_activity, activity.length)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = description },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // 8.2 item 13. The ring draws last, so today is given the position after the last
        // dot rather than its own dot's position.
        val ringIndex = activity.length
        activity.days.forEachIndexed { index, day ->
            val alpha by dotCascadeAlpha(if (day.isToday) ringIndex else index)
            Box(
                modifier = Modifier.size(ACTIVITY_CELL).alpha(alpha),
                contentAlignment = Alignment.Center,
            ) {
                if (day.isToday) {
                    Box(
                        modifier = Modifier
                            .size(ACTIVITY_CELL)
                            .border(1.5.dp, colors.inkSecondary, CircleShape),
                    )
                }
                Box(
                    modifier = Modifier
                        .size(if (day.active) ACTIVE_DOT else IDLE_DOT)
                        .clip(CircleShape)
                        .background(if (day.active) colors.inkSecondary else colors.inkTertiary),
                )
            }
        }
    }

    Spacer(Modifier.height(ClaritySpacing.scaled(10.dp)))
    Text(
        text = if (activity.activeCount == 0) {
            stringResource(R.string.momentum_active_days_none, activity.length)
        } else {
            stringResource(R.string.momentum_active_days, activity.activeCount, activity.length)
        },
        style = type.caption,
        color = colors.inkSecondary,
    )
}

/** The cell one mark occupies, sized so the today ring has room inside it. */
private val ACTIVITY_CELL = 16.dp

/**
 * design-v3.md 14: a gap is a lighter dot. It is also a smaller one.
 *
 * Section 13 requires that color is never the only signal, and the Pulse rhythm row
 * already answers the same question the same way: its silent mark "is drawn smaller as
 * well as fainter so the three states differ in form and not only in opacity".
 */
private val ACTIVE_DOT = 9.dp

private val IDLE_DOT = 5.dp

/**
 * **The 60 percent tiles are deleted, and this is where the reasoning is kept.**
 *
 * design-v3.md 3.4 names "a 60 percent tile in Momentum" as one of four permitted uses of
 * an area's color, and section 11 gave it a three column grid of 52dp blocks. On a device
 * capture those blocks were **the two loudest objects in the entire app**, and each one
 * said exactly one bit: this area has an active item, or it does not. Directly beneath
 * them the Area balance module listed the same areas again, by name, with a figure
 * against each. The screen carried two lists of the same thing, and the one that said
 * nothing was fifteen times the size of the one that said something.
 *
 * **Presence was being measured in area when it should have been measured in meaning.**
 * 3.4 states the tile without stating its size, and design-v3.md 15 requires the
 * non-obvious answer where a choice is open, so the size is open and the obvious answer,
 * taken in phase 7, was "as large as three columns allow". The dot beside a name that
 * carries a real figure has more presence than a 115dp block that carries one bit,
 * because presence is what an element means and not how much of the screen it covers.
 *
 * What is left is the Area balance rows, which is the same information at a tenth of the
 * ink, on the same left edge as every sidehead on the page. `AreaTile` stays in the view
 * model: `MomentumView.tiles` is what tells the Areas banner which areas exist.
 */

// ----------------------------------------------------------------- This Week

/**
 * Three figures, Monday to now, as pure typography with no card behind them.
 * `MASTER_BUILD_PROMPT.md` 12.2.
 *
 * **The figures are set in the serif and the labels in the sans**, which is the deliberate
 * choice rather than the obvious one. The obvious treatment for three numbers side by side
 * is a heavy sans figure over a small caps label, which is a dashboard, and design-v3.md
 * 15.1 lists "stat banners" as a tell. Newsreader ties the three figures to the headline
 * above them and makes the block read as part of a page. It also happens to be the only
 * correct answer to a mechanical problem: 5.2 records that Hanken Grotesk ships no `tnum`
 * feature and Newsreader does, and these three figures are the one place in the app where
 * a number counts up through every value between zero and itself.
 *
 * **The columns are left aligned rather than centered**, for the same reason. Centered
 * columns are what a metric row looks like; a common left edge is what a page looks like.
 *
 * [dimmed] is the empty state's whole treatment of this block, per 12.2: dimmed stats and
 * no guilt. A stat is also dimmed on its own when the feature behind it has never been
 * used, and carries a discovery line under the row rather than being hidden.
 *
 * **Dimmed means `inkSecondary`, never `inkTertiary`.** design-v3.md 13 sets a 4.5:1 floor
 * and 10.3 already had this argument on the area card's idle title: `inkTertiary` measures
 * 2.40:1 in light and a floor is a floor. The dim is bought by the figure stepping down
 * from `inkPrimary`, and by the discovery line, which is the second signal 13 requires
 * whenever a state is being carried by color.
 */
@Composable
private fun ThisWeek(view: MomentumView, dimmed: Boolean) {
    val colors = LocalClarityColors.current
    val type = LocalClarityTypography.current
    val stats = view.week.all

    Sidehead(text = stringResource(R.string.momentum_sidehead_this_week))
    // A.5 fixes a sidehead to its content at `snug` 12. This was 16 here and 14 under all
    // four insight sideheads, for one identical relationship.
    Spacer(Modifier.height(ClaritySpacing.snug))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        stats.forEach { stat ->
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
                val figure = rolledFigure(stat.value)
                // **`itemTitle` 21.5, not `displayTitle` 31.** A.3 clause 1 allows one
                // element at a dominant step and this screen shipped three of them side
                // by side, all louder than the 26sp engine sentence the page is anchored
                // on, so the eye landed on "1 4 2" and read the sentence second. The
                // sentence is the only thing here that was written; the figures are a
                // readout. At 21.5 they still lead their own block and the serif headline
                // above them is the largest thing on the page, which is what it is for.
                TabularNumber(
                    text = figure.toString(),
                    style = type.itemTitle,
                    color = if (dimmed || !stat.discovered) colors.inkSecondary else colors.inkPrimary,
                    contentDescription = figure.toString(),
                )
                Spacer(Modifier.height(ClaritySpacing.scaled(4.dp)))
                Text(
                    text = labelOf(stat),
                    style = type.caption,
                    color = colors.inkSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }

    stats.filterNot { it.discovered }.forEach { stat ->
        discoveryLineOf(stat)?.let { line ->
            Spacer(Modifier.height(ClaritySpacing.scaled(12.dp)))
            Text(text = line, style = type.caption, color = colors.inkSecondary)
        }
    }
}

@Composable
private fun labelOf(stat: WeekStat): String = stringResource(
    when (stat.kind) {
        WeekStatKind.COMPLETED -> R.string.momentum_stat_completed
        WeekStatKind.FOCUS_MINUTES -> R.string.momentum_stat_focused
        WeekStatKind.ADDED -> R.string.momentum_stat_added
    },
)

/**
 * The soft discovery line for a feature that has never been used. 12.2.
 *
 * Every one of these describes how a feature works and none of them says anything about
 * the person. That is the whole reason they may live in `strings.xml`: `CLAUDE.md` rule 8
 * puts fixed interface labels there and observations in a corpus, and "swiping an area
 * card to the right completes what is on it" is a fact about the app.
 *
 * Intake has no line, because capture is the first thing anybody does and there is no
 * feature behind it to discover.
 */
@Composable
private fun discoveryLineOf(stat: WeekStat): String? = when (stat.kind) {
    WeekStatKind.COMPLETED -> stringResource(R.string.momentum_discover_completed)
    WeekStatKind.FOCUS_MINUTES -> stringResource(R.string.momentum_discover_focus)
    WeekStatKind.ADDED -> null
}

// ------------------------------------------------------------------- shared

/** A mark's ink, at the strength one value earns against the busiest in its own series. */
internal fun markInk(base: Color, value: Int, busiest: Int, floor: Float, ceiling: Float): Color {
    if (busiest <= 0) return base.copy(alpha = floor)
    val share = value.toFloat() / busiest.toFloat()
    return base.copy(alpha = floor + (ceiling - floor) * share)
}

/** A spacer of one section, so the modules and the blocks above them share one rhythm. */
@Composable
internal fun SectionGap() {
    Spacer(Modifier.height(ClaritySpacing.scaled(SECTION_SPACING)))
}

/** The width the insight graphics are drawn to, so two of them cannot disagree. */
internal val GRAPHIC_HEIGHT = 44.dp

/** A hairline thick enough to read as a drawn line rather than as an edge. */
internal val GRAPHIC_STROKE = 1.5.dp

/** The gap between the cells of the heat strip. */
internal val STRIP_GAP = 5.dp

/** Kept out of the strip so a zero day is still a cell rather than a hole. */
internal const val MARK_FLOOR_ALPHA = 0.10f

internal const val MARK_CEILING_ALPHA = 0.70f

/**
 * One identity dot, the first of design-v3.md 3.4's four permitted uses, and now the only
 * one on this screen. It takes `ClaritySpacing.areaDot`, so the mark that says which area
 * something belongs to is one size everywhere in the app.
 */
@Composable
internal fun AreaDot(colorHex: String) {
    Box(
        modifier = Modifier
            .size(ClaritySpacing.areaDot)
            .clip(CircleShape)
            // 3.4: the dot keeps its accent at full strength in calm mode, because it is
            // how an area is recognized. It never goes through `calmAccent`.
            .background(parseAreaColor(colorHex)),
    )
}

/** A row in an insight module: the identity dot, a name, and a figure on the trailing edge. */
@Composable
internal fun InsightRow(colorHex: String, name: String, trailing: String) {
    val colors = LocalClarityColors.current
    val type = LocalClarityTypography.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AreaDot(colorHex)
        Spacer(Modifier.width(10.dp))
        Text(
            text = name,
            style = type.body,
            color = colors.inkPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.width(12.dp))
        Text(text = trailing, style = type.caption, color = colors.inkSecondary)
    }
}

/** Plural aware minutes, for the focus module's caption. */
@Composable
internal fun focusMinutesLine(minutes: Int): String =
    pluralStringResource(R.plurals.momentum_focus_minutes, minutes, minutes)
