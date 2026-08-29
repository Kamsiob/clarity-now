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
import com.kamsiob.claritynow.ui.components.ScrollEdge
import com.kamsiob.claritynow.ui.components.TabBarHeight
import com.kamsiob.claritynow.ui.components.TabBarInset
import com.kamsiob.claritynow.ui.components.scrollEdgeFade
import androidx.compose.ui.semantics.Role
import com.kamsiob.claritynow.ui.components.clarityClickable
import com.kamsiob.claritynow.ui.theme.ClarityHapticEvent
import com.kamsiob.claritynow.ui.theme.ClaritySpacing
import com.kamsiob.claritynow.ui.theme.LocalClarityColors
import com.kamsiob.claritynow.ui.theme.LocalClarityShapes
import com.kamsiob.claritynow.ui.theme.LocalClarityTypography
import com.kamsiob.claritynow.ui.theme.clarityMotion
import com.kamsiob.claritynow.ui.theme.parseAreaColor
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.launch

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
 *
 * **Phase 12b put it on the same measure as everything else on the screen.** It used
 * to sit at 8dp, 11.6dp outside every other element, because the list carried 8dp of
 * content padding and each row added 12dp of its own, so the wash was applied outside
 * the padding that set the text's measure. The list now carries the full 20dp of
 * design-v3.md 6's screen padding and a row carries none, so the block spans exactly
 * the measure the day headers and the title run on. **Its breathing room is vertical
 * rather than horizontal**, 12dp against an ordinary row's 10dp: horizontal room would
 * have to come from either pushing the block back outside the measure or pushing a
 * completed row's sentence 12dp in from every other row's, and the second is the same
 * defect with the sign flipped.
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

/**
 * The width of the icon column. 23dp, which is the circle design-v3.md 11 used to ask
 * for, kept as a measure after the circle itself went so that the text column did not
 * move when phase 12b settled what the circle carried.
 */
private val EVENT_SLOT = 23.dp

/**
 * The glyph, which is now the whole of the icon column.
 *
 * It was 14dp inside a tinted 23dp disc and is 18dp standing on the page, because a
 * glyph that used to be read against a ground of its own has to hold the column by
 * itself. Still inside the 23dp slot, so the sentences beside it keep one measure the
 * whole way down.
 */
private val EVENT_GLYPH = 18.dp

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
 * **The horizontal padding is 20dp in one place, and phase 12b moved it there.** It
 * used to be split 8 plus 12, with the list holding 8 and each row holding 12, which
 * put every sentence on design-v3.md 6's 20dp screen padding and the completed row's
 * mint 12dp outside it. One measure, held by the list, is what makes the mint, the day
 * headers, the title and the sentences all start at the same x.
 */
@Composable
fun TrailScreen(
    state: TrailUiState,
    onSelectArea: (String?) -> Unit,
    onLoadMore: () -> Unit,
    onReopen: (String, String) -> Unit,
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

    // The two insets the list scrolls under, read once because both the content padding
    // and the phase 12b scroll edge fade are measured from them. design-v3.md 6.1.
    val statusBar = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navigationBar = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Box(modifier = modifier.fillMaxSize().background(colors.canvas)) {
        if (state.isEmpty) {
            TrailEmptyState()
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    // Content no longer passes hard edged under the clock or behind the
                    // floating pill: it dissolves into the page at both ends. See
                    // `ScrollEdge.kt` for why this erases rather than paints, and for
                    // why it is a fade and not the blur design-v3.md 15.3 refuses.
                    .scrollEdgeFade(
                        top = statusBar + ScrollEdge.underTheClock,
                        bottom = navigationBar + TabBarInset + TabBarHeight +
                            ScrollEdge.aboveTheBar,
                    ),
                contentPadding = PaddingValues(
                    start = ClaritySpacing.screenPadding,
                    end = ClaritySpacing.screenPadding,
                    // The list scrolls under the status bar rather than stopping at it.
                    top = statusBar + 8.dp,
                    // No FAB on this screen, so the trailing gap is 24dp rather than
                    // the 76dp Areas needs to clear one. design-v3.md 10.5 puts the
                    // FAB on Areas only.
                    bottom = navigationBar + TabBarHeight + TabBarInset + 24.dp,
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
                        Spacer(Modifier.height(ClaritySpacing.scaled(8.dp)))
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
                        TrailEventRow(row = row, zone = state.zone, onReopen = onReopen)
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
        // `readSerif` 26, matching the Areas wordmark exactly. A screen title names the
        // place and is chrome; the dominant on a list screen is content, and here that
        // is the day header. Two screens that open the same way are one app.
        style = type.readSerif,
        color = colors.inkPrimary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = ClaritySpacing.snug, bottom = ClaritySpacing.tight),
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
            .padding(vertical = ClaritySpacing.scaled(4.dp)),
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
            // **`leadStrong` 31, and it is the screen's dominant.** A.3 measured the
            // Trail at 1.13 to 1 between its loudest content and its modal size, the
            // worst ratio in the app, which is the mechanical statement of "every row
            // looks the same". A day is the unit a person navigates this screen by, so
            // the day is what gets to be large. It repeats down the page because a
            // section marker repeats by definition; what the budget forbids is two
            // *different* things competing to be the dominant, and here there is one
            // kind of thing at 2.48x the modal and everything else beneath it.
            Text(
                text = label,
                style = type.leadStrong,
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
 * One event. design-v3.md 11 as amended in phase 12b: "Events as a glyph plus a
 * sentence. The glyph stands on the page and carries no color of its own."
 *
 * **There is no tinted circle any more, and that is the settlement of an open choice
 * rather than a simplification.** v3 asked for "a 23dp circle tinted with the event
 * color" and defined no event colors anywhere, so phase 3 resolved the phrase to the
 * area's own accent at 12 percent. Two things were wrong with that and the second is
 * the one that closes the question.
 *
 * The first is redundancy: on a single area app every circle is the same disc, and the
 * icon column carried nothing the sentence beside it did not.
 *
 * The second is that **no color could have carried it.** A per event palette is
 * unavailable by construction: design-v3.md 3.1 scopes `positiveGreen` to completion,
 * `warnAmber` to the Pulse dot and `deleteMuted` to the delete swipe, one job each, and
 * 3.4 permits an area accent in four forms only, of which the only one a 23dp shape may
 * take is a 5 to 14 percent wash. Eight moods at 12 percent over the canvas are not
 * distinguishable from one another at a glance, so the disc could not carry identity
 * either. A container that can hold no information is a container, and design-v3.md 14
 * has a name for a glyph in a tonal circle beside a single sentence: a stock Material
 * list row. So the circle went and the glyph grew from 14dp to 18dp.
 *
 * Area identity on this screen is where design-v3.md 3.4 puts it: the 7dp dot on the
 * filter chips above, at the true accent. The completed row's mint is now the only
 * colored surface in the list, which is the second thing this buys, because it used to
 * compete with a disc on every row around it.
 *
 * **Top aligned, not centered.** design-v3.md 13 requires the app to hold together at
 * 200 percent font scale; the circle is a fixed 23dp and the text is not, so a
 * centered circle would float in the middle of a three line label. Top keeps it
 * beside the first line, where it belongs.
 *
 * A completion is the same row inside a flat mint at `positiveGreen` 8 percent over
 * `card`, clipped to the 12dp row radius. Not `ClarityCard`, which applies elevation
 * unconditionally in light mode and would leave this element carrying a wash and a
 * shadow at once; not `Modifier.areaTint`, which is an area's own accent and
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
private fun TrailEventRow(row: TrailRow, zone: ZoneId, onReopen: (String, String) -> Unit) {
    val colors = LocalClarityColors.current
    val type = LocalClarityTypography.current
    val shapes = LocalClarityShapes.current

    val time = remember(row.wallClock, zone) {
        TIME_FORMAT.format(Instant.ofEpochMilli(row.wallClock).atZone(zone))
    }
    val text = trailRowText(row)
    val description = trailRowDescription(trailSentence(row), time)
    val accent = row.areaColorHex?.let { parseAreaColor(it) }
    // A completion is the one row that offers an act, so it is the one row that takes a
    // role, a click label and a press ground. Everything else stays a record.
    val reopenLabel = stringResource(R.string.cd_trail_reopen)
    val reopenable = row.itemId?.takeIf { row.isCompletion && !row.subject.isNullOrBlank() }
    val outer = Modifier
        .fillMaxWidth()
        .semantics { contentDescription = description }
    val surface = if (row.isCompletion) {
        val mint = colors.positiveGreen.copy(alpha = COMPLETED_WASH_ALPHA)
            .compositeOver(colors.card)
        outer
            .clip(shapes.row)
            .background(mint)
            .then(
                if (reopenable == null) {
                    Modifier
                } else {
                    Modifier.clarityClickable(
                        haptic = ClarityHapticEvent.TAP,
                        role = Role.Button,
                        onClickLabel = reopenLabel,
                        onClick = { onReopen(reopenable, row.subject.orEmpty()) },
                    )
                },
            )
    } else {
        outer.clearAndSetSemantics { contentDescription = description }
    }

    Box(modifier = surface) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = if (row.isCompletion) ClaritySpacing.snug else 0.dp,
                    vertical = ClaritySpacing.scaled(10.dp),
                ),
            verticalAlignment = Alignment.Top,
        ) {
            // **The area dot replaces the glyph column, and it says something the
            // sentence does not.**
            //
            // The column used to carry one of eighteen drawables chosen by row shape,
            // which restated the verb the row had already written in English, and it
            // pushed every sentence 35dp in from the day header above it. The dot names
            // which area the event belongs to, which is the one fact the sentence
            // leaves out and the fact a person scanning a day actually wants. A row
            // that belongs to no area draws a spacer, because inventing an area for a
            // Pulse would be a fabrication, and 3.4's dot is the app's identity device
            // rather than a decoration to fill a column with.
            Box(
                modifier = Modifier
                    .padding(top = 6.dp)
                    .size(ClaritySpacing.areaDot - 2.dp),
            ) {
                if (accent != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(accent),
                    )
                }
            }
            Spacer(Modifier.width(ClaritySpacing.snug))
            Column(modifier = Modifier.weight(1f)) {
                // A row that names nothing of the person's own is one line at `body`,
                // not an orphaned action at `caption`. The five rows that record what
                // the app did stay quieter than a row about a person's own work, which
                // is the ranking, but they still sit at a size a row can be read at.
                Text(
                    text = text.action,
                    style = if (text.subject == null) type.body else type.caption,
                    color = colors.inkSecondary,
                )
                if (text.subject != null) {
                    Text(
                        text = text.subject,
                        style = type.bodyStrong,
                        color = colors.inkPrimary,
                        modifier = Modifier.padding(top = 1.dp),
                    )
                }
            }
            Spacer(Modifier.width(ClaritySpacing.snug))
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
            modifier = Modifier.fillMaxWidth().padding(vertical = ClaritySpacing.scaled(24.dp)),
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
                modifier = Modifier.fillMaxWidth().padding(vertical = ClaritySpacing.scaled(10.dp)),
                verticalAlignment = Alignment.Top,
            ) {
                Box(Modifier.size(EVENT_SLOT), contentAlignment = Alignment.Center) {
                    Box(Modifier.size(EVENT_GLYPH).clip(CircleShape).background(ink))
                }
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
            Spacer(Modifier.height(ClaritySpacing.scaled(10.dp)))
            Text(
                text = stringResource(R.string.trail_empty_body),
                style = type.body,
                color = colors.inkSecondary,
                textAlign = TextAlign.Center,
            )
        }
    }
}
