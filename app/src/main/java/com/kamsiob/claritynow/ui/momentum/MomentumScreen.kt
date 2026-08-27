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
import com.kamsiob.claritynow.domain.momentum.AreaTile
import com.kamsiob.claritynow.domain.momentum.MomentumView
import com.kamsiob.claritynow.domain.momentum.WeekStat
import com.kamsiob.claritynow.domain.momentum.WeekStatKind
import com.kamsiob.claritynow.ui.components.Sidehead
import com.kamsiob.claritynow.ui.components.TabBarHeight
import com.kamsiob.claritynow.ui.components.TabularNumber
import com.kamsiob.claritynow.ui.theme.LocalClarityColors
import com.kamsiob.claritynow.ui.theme.LocalClarityTypography
import com.kamsiob.claritynow.ui.theme.calmAccent
import com.kamsiob.claritynow.ui.theme.parseAreaColor

/** design-v3.md section 6. Section spacing on the Daylight world's calmest screen. */
private val SECTION_SPACING = 28.dp

/** design-v3.md section 6. Momentum tiles are 11dp, and this is the only place they exist. */
private val TILE_RADIUS = 11.dp

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
private const val TILE_COLUMNS = 3

private val TILE_HEIGHT = 52.dp

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

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.canvas)
            .verticalScroll(scroll)
            .padding(
                start = 20.dp,
                end = 20.dp,
                top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 20.dp,
                bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() +
                    TabBarHeight + 17.dp + 24.dp,
            ),
    ) {
        // The headline, and nothing in its place when the engine was silent. A fixed
        // sentence standing here would be the second path MASTER_BUILD_PROMPT 11.1 forbids.
        view.headline?.let { headline ->
            Text(text = headline, style = type.readSerif, color = colors.inkPrimary)
            Spacer(Modifier.height(SECTION_SPACING))
        }

        ActivityRow(view.activity)

        if (view.tiles.isNotEmpty()) {
            Spacer(Modifier.height(SECTION_SPACING))
            AreaTiles(view.tiles)
        }

        Spacer(Modifier.height(SECTION_SPACING))
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

    Spacer(Modifier.height(10.dp))
    Text(
        text = stringResource(R.string.momentum_active_days, activity.activeCount, activity.length),
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

// -------------------------------------------------------------------- the tiles

/**
 * One tile per non archived area. design-v3.md section 11 and 3.4.
 *
 * **The active tile is the area color at 60 percent and carries no border. The idle tile
 * is not colored at all and carries a neutral hairline.** 3.4 permits the accent in four
 * forms and one of them is "a 60 percent tile in Momentum"; the same sentence ends "never
 * as a stripe, bar, edge, border or filled block", so the faint outline section 11 asks
 * for on an idle tile cannot be drawn in the area's own color. It is the `hairline` token,
 * which is what that token is for, and it means the two states differ in fill, in edge and
 * in nothing else. One separation device each, per 6.1.
 *
 * **The name sits under the tile rather than inside it.** 3.4 requires an area label to be
 * verified at 4.5:1 against the surface it is drawn on, and it names the two surfaces that
 * were measured: the card carrying that area's wash, in both worlds. A label on a 60
 * percent tile is a third surface, one per area color per theme, and none of them has been
 * measured. Under the tile the name is ordinary caption ink on the canvas, which has been.
 *
 * **Calm mode desaturates the tile and nothing else here.** 3.4: the 7dp dot and the area
 * label text keep their accent at full strength because they are how an area is
 * recognized and they are the two places contrast was verified; washes, tiles and every
 * atmospheric use desaturate. The tile is named in that sentence, so it goes through
 * `calmAccent`, and the name beneath is ordinary ink and never took an accent at all.
 */
@Composable
private fun AreaTiles(tiles: List<AreaTile>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        tiles.chunked(TILE_COLUMNS).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                row.forEach { tile ->
                    AreaTileCell(tile = tile, modifier = Modifier.weight(1f))
                }
                // Keeps a short final row aligned with the one above it rather than
                // stretching two tiles across the whole width.
                repeat(TILE_COLUMNS - row.size) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun AreaTileCell(tile: AreaTile, modifier: Modifier = Modifier) {
    val colors = LocalClarityColors.current
    val type = LocalClarityTypography.current
    val accent = calmAccent(parseAreaColor(tile.colorHex))
    val description = stringResource(
        if (tile.hasActiveItem) R.string.cd_momentum_tile_active else R.string.cd_momentum_tile_idle,
        tile.name,
    )

    Column(
        modifier = modifier.semantics(mergeDescendants = true) { contentDescription = description },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val shape = RoundedCornerShape(TILE_RADIUS)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(TILE_HEIGHT)
                .clip(shape)
                .then(
                    if (tile.hasActiveItem) {
                        Modifier.background(accent.copy(alpha = ACTIVE_TILE_ALPHA))
                    } else {
                        Modifier.border(1.dp, colors.hairline, shape)
                    },
                ),
        )
        Spacer(Modifier.height(7.dp))
        Text(
            text = tile.name,
            style = type.caption,
            color = colors.inkSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** design-v3.md 3.4, the third of the four permitted uses of an area color. */
private const val ACTIVE_TILE_ALPHA = 0.6f

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
    Spacer(Modifier.height(16.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        stats.forEach { stat ->
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
                val figure = rolledFigure(stat.value)
                TabularNumber(
                    text = figure.toString(),
                    style = type.displayTitle,
                    color = if (dimmed || !stat.discovered) colors.inkSecondary else colors.inkPrimary,
                    contentDescription = figure.toString(),
                )
                Spacer(Modifier.height(4.dp))
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
            Spacer(Modifier.height(12.dp))
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
    Spacer(Modifier.height(SECTION_SPACING))
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

/** One 7dp identity dot, the first of design-v3.md 3.4's four permitted uses. */
@Composable
internal fun AreaDot(colorHex: String) {
    Box(
        modifier = Modifier
            .size(7.dp)
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
