package com.kamsiob.claritynow.ui.trail

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kamsiob.claritynow.R
import com.kamsiob.claritynow.domain.query.TrailRow
import com.kamsiob.claritynow.domain.query.TrailSentenceKey
import com.kamsiob.claritynow.ui.components.ClarityChip
import com.kamsiob.claritynow.ui.components.ClarityIcon
import com.kamsiob.claritynow.ui.components.ClarityIcons
import com.kamsiob.claritynow.ui.components.TabBarHeight
import com.kamsiob.claritynow.ui.components.TabBarInset
import com.kamsiob.claritynow.ui.theme.ClaritySpacing
import com.kamsiob.claritynow.ui.theme.LocalCalmMode
import com.kamsiob.claritynow.ui.theme.LocalClarityColors
import com.kamsiob.claritynow.ui.theme.LocalClarityShapes
import com.kamsiob.claritynow.ui.theme.LocalClarityTypography
import com.kamsiob.claritynow.ui.theme.calmed
import com.kamsiob.claritynow.ui.theme.clarityMotion
import com.kamsiob.claritynow.ui.theme.parseAreaColor
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.launch

/**
 * The tint of an event's circle, and the only alpha it is ever drawn at.
 *
 * design-v3.md 11 asks for "a 23dp circle tinted with the event color" and defines
 * no such color anywhere else in the document. It cannot be a semantic palette
 * (green completions, amber pulses, red deletes): 3.1 scopes `warnAmber` to the
 * Pulse dot and nothing else, `deleteMuted` to the delete swipe only, and 3.4 closes
 * the door on the rest: color appears only as a 7dp dot, a 5 to 14 percent wash, a
 * 60 percent Momentum tile and the area label, and "never as a stripe, bar, edge,
 * border or filled block". A 23dp circle at full saturation is a filled block, so the
 * only legal slot is the wash and the only broadly available color is the area's own,
 * as of that event.
 *
 * 12 percent sits inside 3.4's 5 to 14 band and inside 10.11's "tinted at 11 to 14
 * percent", which is the document's only precedent for tinting a shape of this size.
 * Named once so it cannot drift apart from the completed row's mint.
 */
private const val EVENT_TINT_ALPHA = 0.12f

/**
 * design-v3.md 11, exactly: "Completed events get a mint wash card at positiveGreen
 * 8 percent." A literal in both worlds, because `cardWashAlpha` is 0.06 in light and
 * 0.08 in dark and using it would quietly put light mode out of specification.
 *
 * **The mint sits on `card`, not on `canvas`, and that is a contrast requirement as
 * well as a reading of the word "card".** Composited over the light canvas the mint
 * darkens the ground to `#E0EDEA`, where `inkSecondary` measures 4.40 to one and
 * misses design-v3.md 13's floor of 4.5. Over `card` the ground is `#EDFAF2` and the
 * same text measures 4.57. `TrailContrastTest` holds both numbers.
 */
private const val COMPLETED_WASH_ALPHA = 0.08f

/** design-v3.md 8.2 entry 22: "Placeholder shimmer. 4 percent ink moving slowly." */
private const val SHIMMER_HIGH = 0.04f
private const val SHIMMER_LOW = 0.02f

/**
 * What the day header keeps back from its label for the count, the gap and a run of
 * hairline. Enough that the rule reads as a rule at 200 percent font scale, where the
 * count itself is roughly 24dp wide.
 */
private val DAY_HEADER_RULE_RESERVE = 96.dp

/** The circle is 23dp from design-v3.md 11, deliberately off the 4dp grid. */
private val EVENT_CIRCLE = 23.dp

/**
 * 14dp inside a 23dp circle leaves 4.5dp of tinted ring on every side, which reads
 * as a tinted disc rather than as a badge with a glyph jammed into it. Not a number
 * design-v3.md states; chosen, and stated here so it is one decision rather than
 * five call sites.
 */
private val EVENT_GLYPH = 14.dp

/** The height of a shimmering stand in for a line of body text. */
private val SHIMMER_BAR = 14.dp

/**
 * Every date and time on this screen, pinned to one locale.
 *
 * The app ships one language and `strings.xml` has no translations, so a device
 * locale left to itself would put a month name or an AM marker in another language
 * beside sentences that are all in English. Pinning it keeps a header reading the way
 * the rest of the screen reads. The day patterns are what MASTER_BUILD_PROMPT 9 and
 * the phase brief name; the year appears only when it is not the year of the newest
 * event on screen.
 */
private val TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.US)
private val DAY_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.US)
private val DAY_FORMAT_WITH_YEAR: DateTimeFormatter =
    DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy", Locale.US)

/**
 * The Trail. MASTER_BUILD_PROMPT 9 and design-v3.md 11.
 *
 * A transcript, not a second way to operate the app. Nothing here is tappable,
 * swipeable or long pressable except the filter chips, and that is a decision rather
 * than an omission: design-v3.md 10.15 lists every navigation edge in the app and has
 * no Trail entry, nine of the twenty four event types name nothing that could be
 * opened, and seven more name something archived or tombstoned, so a tap would lead
 * somewhere for some rows and nowhere for others. It also lets a row be 44dp tall
 * without breaking design-v3.md 13's 48dp minimum, which applies to touch targets.
 *
 * The whole screen carries one haptic, on a chip. design-v3.md 9 closes with "Never
 * on scroll, screen entry, notification arrival, or more than once per user action",
 * and pagination here is scroll driven, so loading a page, reaching the end of
 * history and entering the tab are all silent.
 *
 * **The horizontal padding is split 8 plus 12 rather than being 20 in one place.**
 * That is what lets the completed row's mint reach 12dp wider than its own text while
 * the text stays on the same measure as every ordinary row above and below it.
 * design-v3.md 6 puts screen padding at 20dp and 8 plus 12 is 20.
 */
@Composable
fun TrailScreen(
    state: TrailUiState,
    onSelectArea: (String?) -> Unit,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalClarityColors.current
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // The footer is empty while there is more to load and nothing to say, and an
    // empty lazy item is not reliably reported as visible, so the trigger watches for
    // the last real row instead of for the footer itself.
    val atEnd by remember {
        derivedStateOf {
            val info = listState.layoutInfo
            val last = info.visibleItemsInfo.lastOrNull()
            last != null && last.index >= info.totalItemsCount - 2
        }
    }
    LaunchedEffect(atEnd, state.days.size, state.appending, state.endOfHistory, state.loading) {
        if (atEnd && !state.appending && !state.loading && !state.endOfHistory) onLoadMore()
    }

    Box(modifier = modifier.fillMaxSize().background(colors.canvas)) {
        if (state.isEmpty) {
            TrailEmptyState()
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 8.dp,
                    end = 8.dp,
                    // The list scrolls under the status bar rather than stopping at it.
                    top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 8.dp,
                    // No FAB on this screen, so the trailing gap is 24dp rather than
                    // the 76dp Areas needs to clear one. design-v3.md 10.5 puts the
                    // FAB on Areas only.
                    bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() +
                        TabBarHeight + TabBarInset + 24.dp,
                ),
            ) {
                item(key = "title") { TrailHeader() }

                item(key = "filters") {
                    Column {
                        TrailFilterRow(
                            areas = state.areas,
                            selectedAreaId = state.selectedAreaId,
                            onSelect = { areaId ->
                                onSelectArea(areaId)
                                // A filter change replaces the whole list, so it has
                                // to bring the chip row back into reach. Item 0 is the
                                // title rather than the chips, which is what is wanted:
                                // the header and the chips arrive together and the
                                // screen re-reads from the top. Instant rather than
                                // animated, because an animated scroll would travel
                                // through rows that are being replaced underneath it.
                                scope.launch { listState.scrollToItem(0) }
                            },
                        )
                        // The chip row carries 4dp of its own, and the first day
                        // header takes no top gap, so the 12dp between them is made
                        // up here. On the 4dp grid, and chosen rather than specified.
                        Spacer(Modifier.height(8.dp))
                    }
                }

                state.days.forEachIndexed { index, day ->
                    item(key = "day:${day.date}") {
                        TrailDayHeader(
                            day = day,
                            today = state.today,
                            referenceYear = state.referenceYear,
                            isFirst = index == 0,
                        )
                    }
                    items(day.rows, key = { "row:" + it.eventId }) { row ->
                        TrailEventRow(row = row, zone = state.zone)
                    }
                }

                item(key = "footer") {
                    TrailFooter(
                        loading = state.loading || state.appending,
                        endOfHistory = state.endOfHistory,
                    )
                }
            }
        }
    }
}

/**
 * The screen title, in the serif. design-v3.md 11 and 5.3.
 *
 * **Phase 3 decided the Trail had no title and phase 3c reverses that decision.** The
 * original reasoning was sound as far as it went: the tab bar already says Trail, so a
 * heading reading Trail repeats a word the user can see at the other end of the same
 * screen. What that reasoning did not price is what section 11 gives every other
 * surface. Areas opens with a `displayTitle`, the Report has an eyebrow and a
 * `displayHero`, Momentum has a `readSerif` headline and About has a `displayTitle`, so
 * the Trail was the one screen in the document that began with its content. The design
 * audit measured the cost: the built Trail contained no serif glyph at all, and across
 * the whole app Newsreader had five call sites, of which four were the empty states in
 * `AreaSheets`, `InboxSheet`, `AreasScreen` and this file, and the fifth was the Areas
 * title. Four uses in five said "there is nothing here", which is not what a signature
 * typeface should mean. This line is the sixth, and it changes the ratio as well as
 * this screen.
 *
 * The audit's second finding here is the same fact from the other end. With no title,
 * the loudest thing on the Trail was the selected `All` filter chip, so a filter
 * outranked everything it filters. Thirty sp of serif above it puts the ranking back.
 *
 * Recorded rather than flipped: design-v3.md 11 now names this title, and the
 * reversal and its reason are written there as well as here.
 *
 * **The same treatment as Areas, deliberately.** Same role, same 12dp above and 6dp
 * below, same `inkPrimary`, no glyph beside it, because the Trail has nothing that
 * belongs next to a title the way the archive does on Areas. Two screens that open the
 * same way are one app; a Trail title styled to be interesting would be a second
 * design language for a screen that reads history.
 *
 * The 12dp of horizontal padding is the list's own 8dp brought up to the 20dp screen
 * padding in design-v3.md 6, which is the same arithmetic every row and day header on
 * this screen does. The title's left edge therefore lands on the day headers' left
 * edge exactly.
 *
 * No entrance. design-v3.md 8.4's once-per-tab entrance is wired on Areas and nowhere
 * else, and giving the Trail one is a motion decision rather than a type one.
 */
@Composable
private fun TrailHeader() {
    val colors = LocalClarityColors.current
    val type = LocalClarityTypography.current
    Text(
        text = stringResource(R.string.trail_title),
        style = type.displayTitle,
        color = colors.inkPrimary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .padding(top = 12.dp, bottom = 6.dp),
    )
}

/**
 * design-v3.md 10.8. Pill chips in a horizontally scrolling row, `All` first.
 *
 * An ordinary list item rather than a pinned bar. A bar that stays put over
 * scrolling content needs an opaque or blurred backing to stay legible, and that
 * backing is a second separation device on a row that is already separated by the
 * whitespace around it; the translucent escape from that is design-v3.md 15.1's
 * "Glassmorphism used as decoration rather than to solve a layering problem". It also
 * leaves exactly one thing consuming the top inset, which is the class of defect that
 * `docs/BUILD_STATE.md` records against the Areas screen.
 *
 * The chips are the live areas, not every area that ever existed: a tombstoned area
 * cannot be offered as a filter, which CLARITY_LOGIC_ENGINE.md 1.1 prohibition 3
 * forbids outright, and its events still appear under `All` because they carry their
 * own name snapshots.
 */
@Composable
private fun TrailFilterRow(
    areas: List<TrailAreaChip>,
    selectedAreaId: String?,
    onSelect: (String?) -> Unit,
) {
    val allLabel = stringResource(R.string.trail_filter_all)
    val allDescription = stringResource(R.string.cd_trail_filter_all)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .selectableGroup()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ClarityChip(
            label = allLabel,
            onClick = { onSelect(null) },
            selected = selectedAreaId == null,
            modifier = Modifier.semantics { contentDescription = allDescription },
        )
        areas.forEach { area ->
            val description = stringResource(R.string.cd_trail_filter_area, area.name)
            ClarityChip(
                label = area.name,
                onClick = { onSelect(area.areaId) },
                selected = selectedAreaId == area.areaId,
                dotColor = parseAreaColor(area.colorHex),
                modifier = Modifier.semantics { contentDescription = description },
            )
        }
    }
}

/**
 * design-v3.md 11: "Day headers as bodyStrong with an inline count and a hairline to
 * the trailing edge."
 *
 * **bodyStrong and sentence case, never a sidehead and never all caps.** 10.12's
 * sidehead is 13sp at weight 700 and section 11 overrides it here; all caps is banned
 * three separate times, in the 15.1 tell list, in 10.12 and in 14. An all caps date
 * header is the near universal convention in activity logs, which makes it the
 * highest risk tell on this screen.
 *
 * **The header only started outranking its rows in phase 3c, and not by anything this
 * file does.** `body` and `bodyStrong` were both 16sp through phase 3b, so section 11's
 * instruction to set the header in one and the rows in the other bought a weight change
 * and nothing else, and a device capture of four events read as a wall of one size at
 * 12 and 16sp. 5.3 now puts `body` at 15 and `bodyStrong` at 17, so the same two lines
 * of code that were a 1.00 size ratio are a 1.13 one, on top of the 600 against 400.
 * With the serif title above it the screen reads 30, 17, 15, 12 rather than 16 and 12.
 * Nothing here changed; the scale under it did, which is the whole argument for having
 * fixed the scale before building the eight screens that do not exist yet.
 *
 * The hairline is the header's one separation device, which is why there is no card,
 * no background shift, no shadow and no stickiness underneath it. It is drawn with
 * `drawBehind` rather than with `Modifier.border`, because a zero width border still
 * draws a hairline and had once put a permanent outline on every focusable element in
 * this app.
 *
 * The count is `inkSecondary` rather than `inkTertiary`. `inkTertiary` measures
 * 2.37:1 against the canvas in light mode against design-v3.md 13's 4.5:1 floor, and
 * the obvious de-emphasis choice is therefore design-v3.md 15.1's "low contrast body
 * text" in its light mode form.
 */
@Composable
private fun TrailDayHeader(
    day: TrailDayModel,
    today: LocalDate,
    referenceYear: Int,
    isFirst: Boolean,
) {
    val colors = LocalClarityColors.current
    val type = LocalClarityTypography.current
    val label = trailDayLabel(date = day.date, today = today, referenceYear = referenceYear)
    val description = trailDayDescription(label, day.count)
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .padding(top = if (isFirst) 0.dp else ClaritySpacing.sectionGap, bottom = 12.dp)
            .clearAndSetSemantics { contentDescription = description },
    ) {
        // The label is capped rather than left to measure freely. A long date at 200
        // percent font scale measures wider than the whole row, which takes the
        // hairline to zero width, and the hairline is this header's one separation
        // device. Capped, the date wraps and the rule always has somewhere to run.
        val labelCap = maxWidth - DAY_HEADER_RULE_RESERVE
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = type.bodyStrong,
                color = colors.inkPrimary,
                modifier = Modifier.widthIn(max = labelCap),
            )
            Spacer(Modifier.width(8.dp))
            // A bare number rather than a badge, and not TabularNumber: a header count
            // is fixed the moment its page loads, and design-v3.md 5.2 scopes tabular
            // figures to updating numeric displays.
            Text(
                text = day.count.toString(),
                style = type.caption,
                color = colors.inkSecondary,
            )
            Box(
                modifier = Modifier
                    .padding(start = 10.dp)
                    .weight(1f)
                    .height(1.dp)
                    .drawBehind { drawRect(colors.hairline) },
            )
        }
    }
}

/** Today, Yesterday, or the date, with the year only when it is not the one on screen. */
@Composable
private fun trailDayLabel(date: LocalDate, today: LocalDate, referenceYear: Int): String = when {
    date == today -> stringResource(R.string.trail_day_today)
    date == today.minusDays(1) -> stringResource(R.string.trail_day_yesterday)
    date.year != referenceYear -> DAY_FORMAT_WITH_YEAR.format(date)
    else -> DAY_FORMAT.format(date)
}

/**
 * One event. design-v3.md 11: "Events as icon plus text rows, the icon a 23dp circle
 * tinted with the event color."
 *
 * **Top aligned, not centered.** design-v3.md 13 requires the app to hold together at
 * 200 percent font scale; the circle is a fixed 23dp and the text is not, so a
 * centered circle would float in the middle of a three line label. Top keeps it
 * beside the first line, where it belongs.
 *
 * A completion is the same row inside a flat mint at `positiveGreen` 8 percent over
 * `card`, clipped to the 12dp row radius. Not `ClarityCard`, which applies elevation
 * unconditionally in light mode and would leave this element carrying a wash and a
 * shadow at once; not `Modifier.areaWash`, which pools its color toward a corner
 * chosen by hashing an area id, which section 11 never asked for here; and 12dp
 * rather than the 18dp of a content card, because every neighbor on this screen is a
 * row and reading it as a card invites both of the mistakes above.
 *
 * **The completion's circle keeps the area color.** A mint card behind a mint circle
 * behind a green check is three greens on one row and it erases the only piece of
 * area identity the row carries. design-v3.md 13 requires that completion not be
 * signaled by color alone, and the check glyph plus the word "Completed" is what
 * satisfies that.
 *
 * The whole row is one semantics node whose description always includes the time,
 * even on the rows where the cluster rule hides it.
 */
@Composable
private fun TrailEventRow(row: TrailRow, zone: ZoneId) {
    val colors = LocalClarityColors.current
    val type = LocalClarityTypography.current
    val shapes = LocalClarityShapes.current

    val time = remember(row.wallClock, zone) {
        TIME_FORMAT.format(Instant.ofEpochMilli(row.wallClock).atZone(zone))
    }
    val sentence = trailSentence(row)
    val description = trailRowDescription(sentence, time)
    // design-v3.md 16.2. The circle is the area's accent at 12 percent, which is an
    // atmospheric use and not one of the two the transform excludes by name, so calm
    // mode desaturates it. The filter chip's 7dp dot above is the excluded one and
    // keeps the true color, which is the point: the dot is how an area is recognized
    // and the circle is how a row is tinted.
    val calm = LocalCalmMode.current
    val tint = row.areaColorHex?.let { parseAreaColor(it).calmed(calm) } ?: colors.inkPrimary

    val outer = Modifier
        .fillMaxWidth()
        .clearAndSetSemantics { contentDescription = description }
    val surface = if (row.isCompletion) {
        val mint = colors.positiveGreen.copy(alpha = COMPLETED_WASH_ALPHA)
            .compositeOver(colors.card)
        outer.clip(shapes.row).background(mint)
    } else {
        outer
    }

    Box(modifier = surface) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                modifier = Modifier
                    .size(EVENT_CIRCLE)
                    .clip(CircleShape)
                    .background(tint.copy(alpha = EVENT_TINT_ALPHA)),
                contentAlignment = Alignment.Center,
            ) {
                // No description on the glyph: the row's own sentence already says
                // what happened, and a second node would have TalkBack say it twice.
                ClarityIcon(
                    icon = iconFor(row.sentence),
                    contentDescription = null,
                    tint = colors.inkSecondary,
                    modifier = Modifier.size(EVENT_GLYPH),
                )
            }
            Spacer(Modifier.width(12.dp))
            // No maxLines and no ellipsis. design-v3.md 13 requires the app to hold
            // together at 200 percent font scale without clipping, and no Trail row is
            // tappable, so a truncated sentence is one a person has no way to recover.
            // A title is capped at 200 characters where it is written, so a row is
            // bounded even when it is tall.
            Text(
                text = sentence,
                style = type.body,
                color = colors.inkPrimary,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(12.dp))
            // MASTER_BUILD_PROMPT 9 puts a timestamp on the first event of each ten
            // minute cluster only. The suppressed ones still occupy their slot, drawn
            // in nothing, so the text column keeps one measure the whole way down the
            // page instead of widening on every row that happens to be in a cluster.
            Text(
                text = time,
                style = type.caption,
                color = if (row.showsTimestamp) colors.inkSecondary else Color.Transparent,
            )
        }
    }
}

/**
 * The glyph for a row shape. Exhaustive, so a twenty sixth shape fails the build.
 *
 * Twenty five row shapes onto eighteen drawables that already exist, thirteen of them
 * named by design-v3.md 7 directly. Nothing is invented for this screen, which is how
 * it avoids 15.1's "Interchangeable thin line icons with no relationship to the
 * product". There is deliberately no sparkle for the two generated types:
 * design-v3.md 7 bans `auto_awesome` by name because it reads as an AI affordance,
 * which is the opposite of what this app claims.
 */
@DrawableRes
private fun iconFor(sentence: TrailSentenceKey): Int = when (sentence) {
    TrailSentenceKey.AREA_CREATED -> ClarityIcons.areas
    TrailSentenceKey.AREA_RENAMED -> ClarityIcons.edit
    TrailSentenceKey.AREA_RECOLORED -> ClarityIcons.appearance
    TrailSentenceKey.AREA_REORDERED -> ClarityIcons.reorder
    TrailSentenceKey.AREA_ARCHIVED -> ClarityIcons.archive
    TrailSentenceKey.AREA_UNARCHIVED -> ClarityIcons.unarchive
    TrailSentenceKey.AREA_DELETED -> ClarityIcons.erase
    // Filing puts a captured thing into an area, so it takes the areas glyph, the
    // same one AREA_CREATED uses. Reuse is the point: design-v3.md 7 wants the set
    // small and coherent, and twenty nine row shapes still map onto nineteen
    // drawables that already exist.
    TrailSentenceKey.ITEM_FILED -> ClarityIcons.areas
    TrailSentenceKey.ITEM_ESTIMATED -> ClarityIcons.time
    TrailSentenceKey.ITEM_ESTIMATE_CLEARED -> ClarityIcons.time
    TrailSentenceKey.FOCUS_EXTENDED -> ClarityIcons.focusEvent

    TrailSentenceKey.ITEM_ADDED -> ClarityIcons.add
    TrailSentenceKey.ITEM_EDITED -> ClarityIcons.edit
    TrailSentenceKey.ITEM_QUEUED -> ClarityIcons.expand
    TrailSentenceKey.ITEM_PROMOTED -> ClarityIcons.promoted
    TrailSentenceKey.ITEM_SWAPPED -> ClarityIcons.swap
    TrailSentenceKey.ITEM_COMPLETED -> ClarityIcons.completed
    TrailSentenceKey.ITEM_REOPENED -> ClarityIcons.regenerate
    TrailSentenceKey.ITEM_REORDERED -> ClarityIcons.reorder
    TrailSentenceKey.ITEM_DELETED -> ClarityIcons.erase

    TrailSentenceKey.FOCUS_STARTED -> ClarityIcons.focusEvent
    TrailSentenceKey.FOCUS_COMPLETED -> ClarityIcons.focusEvent
    TrailSentenceKey.FOCUS_STOPPED -> ClarityIcons.focusEvent

    TrailSentenceKey.PULSE_GENERATED -> ClarityIcons.pulse
    TrailSentenceKey.PULSE_ANSWERED -> ClarityIcons.pulse

    TrailSentenceKey.REPORT_GENERATED -> ClarityIcons.report
    TrailSentenceKey.PLAN_OFFERED -> ClarityIcons.report
    TrailSentenceKey.PLAN_ACCEPTED -> ClarityIcons.check

    TrailSentenceKey.SETTING_CHANGED -> ClarityIcons.settings
}

/**
 * The bottom of the list in its three states: loading, finished, or nothing at all.
 *
 * There is no load more button. Pagination happens because the end of the list came
 * into view, and it happens without a haptic, because design-v3.md 9 says never on
 * scroll and reaching the end of one's own history is not an event worth buzzing
 * about.
 */
@Composable
private fun TrailFooter(loading: Boolean, endOfHistory: Boolean) {
    val colors = LocalClarityColors.current
    val type = LocalClarityTypography.current
    when {
        loading -> TrailShimmer()
        endOfHistory -> Text(
            text = stringResource(R.string.trail_end_of_history),
            style = type.caption,
            color = colors.inkSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
        )
        // Idle with more to come draws nothing. A row of dots would be a spinner
        // wearing a hat.
        else -> Unit
    }
}

/**
 * design-v3.md 8.2 entry 22, verbatim: "Placeholder shimmer. 4 percent ink moving
 * slowly. **Never a spinner.**" Banned twice, the second time in design-v3.md 14.
 *
 * Three placeholders in the geometry of a real row, so nothing shifts sideways when
 * the content lands. The rows that replace them do not stagger: 8.2 entry 4's 50ms
 * per item staggered rise is written for a screen arriving, and applying it to rows
 * appended under a finger that is still scrolling fights the gesture.
 */
@Composable
private fun TrailShimmer() {
    val colors = LocalClarityColors.current
    val shapes = LocalClarityShapes.current
    val motion = clarityMotion()
    // Held rather than animated when motion is reduced, which is also why the
    // infinite transition is not started at all in that case.
    val alpha = if (motion.reduced) SHIMMER_HIGH else shimmerAlpha()
    val ink = colors.inkPrimary.copy(alpha = alpha.coerceIn(0f, 1f))
    Column(modifier = Modifier.fillMaxWidth()) {
        repeat(3) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Box(Modifier.size(EVENT_CIRCLE).clip(CircleShape).background(ink))
                Spacer(Modifier.width(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(SHIMMER_BAR)
                        .clip(shapes.row)
                        .background(ink),
                )
            }
        }
    }
}

/**
 * The slow breath. 600ms with the easeSlow curve, reversing, which is the token
 * ClarityMotion names for exactly this; it is written out here because
 * `infiniteRepeatable` needs a duration based spec and the token is typed as a
 * general finite one.
 */
@Composable
private fun shimmerAlpha(): Float {
    val transition = rememberInfiniteTransition(label = "trailShimmer")
    val alpha by transition.animateFloat(
        initialValue = SHIMMER_LOW,
        targetValue = SHIMMER_HIGH,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "trailShimmerAlpha",
    )
    return alpha
}

/**
 * design-v3.md 10.13. An explanation, never a scold, and no illustration or mascot.
 *
 * The Trail is the one empty state with no action to offer, because nothing is done
 * on this screen, so the body says what the screen is for instead of naming a next
 * step. The entrance is 8.2 entry 25 exactly: 400ms easeOut after a 150ms delay, so
 * it never flashes during a load that resolves quickly. `motion.easeOut` is the 350ms
 * entrance curve with no delay and is the wrong specification here, which is why the
 * duration is written out.
 */
@Composable
private fun TrailEmptyState() {
    val colors = LocalClarityColors.current
    val type = LocalClarityTypography.current
    val motion = clarityMotion()
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { shown = true }

    // design-v3.md 8.4. The fade shortens to 150ms when motion is reduced or calm mode
    // is on, and the 150ms delay is kept in both cases. Shortening a fade is motion;
    // dropping the delay would only reintroduce the flash the delay exists to prevent,
    // and calm mode has no interest in making a screen flash.
    val entrance = tween<Float>(
        durationMillis = if (motion.reduced) 150 else 400,
        delayMillis = 150,
        easing = EaseOutCubic,
    )

    AnimatedVisibility(visible = shown, enter = fadeIn(entrance)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(40.dp)
                // Centered in what a person can see, which is not the same as the
                // window: the tab bar owns the bottom 78dp of it.
                .padding(bottom = TabBarHeight + TabBarInset),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = stringResource(R.string.trail_empty_title),
                style = type.readSerif,
                color = colors.inkPrimary,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.trail_empty_body),
                style = type.body,
                color = colors.inkSecondary,
                textAlign = TextAlign.Center,
            )
        }
    }
}
