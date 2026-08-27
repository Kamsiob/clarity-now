package com.kamsiob.claritynow.ui.areas

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.kamsiob.claritynow.R
import com.kamsiob.claritynow.ui.components.ClarityButton
import com.kamsiob.claritynow.ui.components.ClarityButtonRole
import com.kamsiob.claritynow.ui.components.ClarityCard
import com.kamsiob.claritynow.ui.components.ClarityChip
import com.kamsiob.claritynow.ui.components.ClarityFab
import com.kamsiob.claritynow.ui.components.ClarityIcon
import com.kamsiob.claritynow.ui.components.ClarityIcons
import com.kamsiob.claritynow.ui.components.SwipeActions
import com.kamsiob.claritynow.ui.components.SwipeCoordinator
import com.kamsiob.claritynow.ui.components.SwipeableRow
import com.kamsiob.claritynow.ui.components.TabBarHeight
import com.kamsiob.claritynow.ui.components.clarityClickable
import com.kamsiob.claritynow.ui.components.clarityPressScale
import com.kamsiob.claritynow.ui.components.rememberReorderState
import com.kamsiob.claritynow.ui.components.rememberSwipeCoordinator
import com.kamsiob.claritynow.ui.components.reorderableItem
import com.kamsiob.claritynow.ui.theme.ClarityHapticEvent
import com.kamsiob.claritynow.ui.theme.LocalClarityColors
import com.kamsiob.claritynow.ui.theme.LocalClarityTypography
import com.kamsiob.claritynow.ui.theme.clarityEntrance
import com.kamsiob.claritynow.ui.theme.clarityMotion
import com.kamsiob.claritynow.ui.theme.parseAreaColor

/** The height one area card settles at, used by the drag reorder arithmetic. */
private val CARD_HEIGHT_ESTIMATE = 96.dp

/**
 * The title is the first thing to arrive, and the cards stagger in behind it.
 * design-v3.md 8.2 item 4.
 *
 * The obvious answer, and the one most list screens use, is to stagger the rows and
 * leave the header fixed. It is rejected under design-v3.md 15: a fixed title with rows
 * pouring in underneath reads as content loading into a frame, and what this entrance
 * is for is the app arriving. Starting at the title costs one stagger step, 50ms, and
 * makes the screen one thing rather than two. Recorded in `DECISIONS.md`.
 */
private const val HEADER_ENTRANCE_INDEX = 0

/**
 * The Daylight home. design-v3.md section 11.
 *
 * Must pass the three second test: what is active everywhere, at a glance. Five
 * areas fit on screen comfortably, which is what the slim card and the 11dp gap
 * between cards are for.
 */
@Composable
fun AreasScreen(
    state: AreasUiState,
    onOpenArea: (String) -> Unit,
    onOpenArchive: () -> Unit,
    onCompleteArea: (AreaCardModel) -> Unit,
    onSwapArea: (AreaCardModel) -> Unit,
    onDeleteArea: (AreaCardModel) -> Unit,
    onLongPressArea: (AreaCardModel) -> Unit,
    onMoveArea: (String, Int) -> Unit,
    onPromotionPlayed: (String) -> Unit,
    onDismissConflict: (String) -> Unit,
    onOpenInbox: () -> Unit,
    onOpenFocus: () -> Unit,
    onFabClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalClarityColors.current
    val type = LocalClarityTypography.current
    val motion = clarityMotion()
    val density = LocalDensity.current
    val listState = rememberLazyListState()
    val swipe = rememberSwipeCoordinator()
    val reorder = rememberReorderState(listState) { key, toIndex -> onMoveArea(key, toIndex) }

    // A drag owns the pointer, so a swipe must not also be listening.
    LaunchedEffect(reorder.isDragging) { swipe.enabled = !reorder.isDragging }

    val cardHeightPx = with(density) { (CARD_HEIGHT_ESTIMATE + 11.dp).toPx() }
    val ordered = remember(state.areas, reorder.previewOrder) {
        val preview = reorder.previewOrder
        if (preview == null) state.areas else preview.mapNotNull { id -> state.areas.firstOrNull { it.id == id } }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.canvas)
            // A tap anywhere while a row is open closes it, and is spent doing so.
            .clarityClickable(enabled = swipe.hasOpenRow, haptic = null) { swipe.close() },
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                // The list scrolls under the status bar rather than stopping at it,
                // so the first card passes behind the clock instead of clipping.
                top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 8.dp,
                bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() +
                    TabBarHeight + 17.dp + 76.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(11.dp),
        ) {
            // design-v3.md 8.2 item 4 and 8.4. The screen arrives as one sequence, the
            // title first and the cards behind it, on the first open of this tab per
            // app session and never again. Everything about when it fires, and whether
            // it fires at all, is inside Modifier.clarityEntrance.
            //
            // The conflict cards deliberately take no entrance index. A conflict card is
            // an interruption that arrives when a merge produced one, carrying its own
            // reveal, rather than part of the screen's resting content.
            item(key = "header") {
                AreasHeader(
                    unfiledCount = state.unfiledCount,
                    onOpenFocus = onOpenFocus,
                    onOpenArchive = onOpenArchive,
                    onOpenInbox = onOpenInbox,
                    modifier = Modifier.clarityEntrance(HEADER_ENTRANCE_INDEX),
                )
            }

            items(state.conflicts, key = { "conflict:${it.id}" }) { conflict ->
                ConflictCard(conflict = conflict, onDismiss = { onDismissConflict(conflict.id) })
            }

            if (state.isEmpty) {
                item(key = "empty") { AreasEmptyState(onCreate = onFabClick) }
            }

            items(ordered, key = { it.id }) { area ->
                val index = ordered.indexOfFirst { it.id == area.id }
                val dragging = reorder.draggingKey == area.id
                val lift by animateFloatAsState(
                    targetValue = if (dragging) 1.02f else 1f,
                    animationSpec = motion.springGentle(),
                    label = "dragLift",
                )

                Box(
                    modifier = Modifier
                        .animateItem(
                            placementSpec = motion.springStandard(),
                            fadeInSpec = motion.easeOut(),
                            fadeOutSpec = motion.easeOut(),
                        )
                        .clarityEntrance(HEADER_ENTRANCE_INDEX + 1 + index)
                        .offset {
                            if (dragging) IntOffset(0, reorder.dragOffset.toInt()) else IntOffset.Zero
                        }
                        .scale(lift)
                        // A picked up card floats above its neighbors rather than
                        // sliding under them.
                        .zIndex(if (dragging) 1f else 0f),
                ) {
                    AreaRow(
                        area = area,
                        promotion = state.promotions[area.id],
                        swipe = swipe,
                        isLastArea = ordered.size == 1,
                        reorderModifier = Modifier.reorderableItem(
                            state = reorder,
                            key = area.id,
                            index = index,
                            keys = ordered.map { it.id },
                            itemHeightPx = cardHeightPx,
                            enabled = true,
                            onLongPressWithoutDrag = { onLongPressArea(area) },
                        ),
                        onOpen = { onOpenArea(area.id) },
                        onComplete = { onCompleteArea(area) },
                        onSwap = { onSwapArea(area) },
                        onDelete = { onDeleteArea(area) },
                        onPromotionPlayed = { onPromotionPlayed(area.id) },
                    )
                }
            }
        }

        ClarityFab(
            onClick = onFabClick,
            contentDescription = stringResource(
                if (state.isEmpty) R.string.fab_add_area else R.string.fab_add_item,
            ),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                // Clears the floating tab bar: its own 17dp inset, its height, and
                // a gap so the two never read as one control.
                .padding(end = 20.dp, bottom = TabBarHeight + 17.dp + 14.dp),
        )
    }
}

@Composable
private fun AreaRow(
    area: AreaCardModel,
    promotion: PromotionCue?,
    swipe: SwipeCoordinator,
    isLastArea: Boolean,
    reorderModifier: Modifier,
    onOpen: () -> Unit,
    onComplete: () -> Unit,
    onSwap: () -> Unit,
    onDelete: () -> Unit,
    onPromotionPlayed: () -> Unit,
) {
    val colors = LocalClarityColors.current
    val shape = RoundedCornerShape(18.dp)

    val actions = SwipeActions(
        completeLabel = stringResource(R.string.action_complete),
        swapLabel = stringResource(R.string.action_swap),
        deleteLabel = stringResource(R.string.action_delete),
        onComplete = if (area.offersComplete) onComplete else null,
        onSwap = if (area.offersSwap) onSwap else null,
        // An app with zero areas is reached deliberately through the archive view,
        // never by accident on a list, so the last card does not offer delete.
        onDelete = if (isLastArea) null else onDelete,
    )

    SwipeableRow(
        key = area.id,
        coordinator = swipe,
        actions = actions,
        accent = parseAreaColor(area.colorHex),
        shape = shape,
    ) {
        // design-v3.md 8.2 item 2 gives a card the same 0.97 press as a button, and
        // section 9 gives a card press the tap haptic. The largest target in the app
        // had neither: `clarityClickable` sets `indication = null` deliberately, and
        // nothing had been put in its place.
        val cardInteraction = remember { MutableInteractionSource() }
        ClarityCard(
            modifier = Modifier
                .fillMaxWidth()
                .then(reorderModifier)
                .clarityPressScale(cardInteraction, label = "areaCardPress")
                .clarityClickable(
                    interactionSource = cardInteraction,
                    haptic = ClarityHapticEvent.TAP,
                    onClick = {
                        if (swipe.hasOpenRow) swipe.close() else onOpen()
                    },
                ),
            shape = shape,
            colors = colors,
        ) {
            AreaCardContent(
                area = area,
                promotion = promotion,
                onPromotionPlayed = onPromotionPlayed,
            )
        }
    }
}

/**
 * design-v3.md 10.1. The serif title, the archive glyph, and the chip row beneath.
 *
 * The row was written in phase 2 as a shape the permanent chips could prepend to
 * without it being redrawn, and this is the first of them arriving. Focus is first and
 * permanent, Pulse joins it in phase 6, and the unfiled inbox chip, 10.16, stays last
 * and present only while the inbox holds something, so it can never displace them.
 */
@Composable
private fun AreasHeader(
    unfiledCount: Int,
    onOpenFocus: () -> Unit,
    onOpenArchive: () -> Unit,
    onOpenInbox: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalClarityColors.current
    val type = LocalClarityTypography.current
    Column(modifier = modifier.fillMaxWidth().padding(top = 12.dp, bottom = 6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = stringResource(R.string.areas_title),
                style = type.displayTitle,
                color = colors.inkPrimary,
            )
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .clarityClickable(onClickLabel = stringResource(R.string.areas_open_archive)) {
                        onOpenArchive()
                    },
                contentAlignment = Alignment.Center,
            ) {
                ClarityIcon(
                    icon = ClarityIcons.archive,
                    contentDescription = stringResource(R.string.areas_open_archive),
                    tint = colors.inkSecondary,
                    modifier = Modifier.size(22.dp),
                )
            }
        }

        AreasChipRow(
            unfiledCount = unfiledCount,
            onOpenFocus = onOpenFocus,
            onOpenInbox = onOpenInbox,
        )
    }
}

/**
 * design-v3.md 10.1 and 10.8. Pill chips in a horizontally scrolling row.
 *
 * **Every chip here is an ordinary unselected `ClarityChip` and none of them carries a
 * count badge, a dot or an accent.** 10.1 gives the permanent chips soft elevation and
 * no border, and phase 3c moved app chrome down one step of the value ladder, so an
 * unselected chip sits at `raise` rather than at `card` and this row inherits that from
 * the component rather than restating it. The one dot in this row's future is the
 * warnAmber Pulse dot in 10.1, which arrives with Pulse in phase 6 and is scoped by
 * 3.1 to that one use.
 *
 * **Focus is permanent and is present even when nothing can be focused on.** The
 * obvious answer is to hide it, or to dim it, while there is no area with an active
 * item. 10.1 calls it permanent, and the chooser has an empty state of its own,
 * `Nothing to focus on yet`, which is a sentence that explains the situation where a
 * missing chip would leave a person wondering where the feature went. 10.16 settles
 * the same question for the inbox rows in the opposite direction, and the difference
 * is real: a disabled control is a question a person has to answer, while a permanent
 * one that leads to an explanation is an answer.
 *
 * **The chip does not become a countdown while a session is running**, and that is the
 * deliberate choice rather than the obvious one, per section 15. It would be one line,
 * and the area card two thumbs below it already carries the live countdown, 10.3, next
 * to the name of the item the session is on. Two surfaces reporting the same number in
 * one screenful is how a person learns to read neither, and the chip's job is to be the
 * way back in, which it does under the same label either way.
 */
@Composable
private fun AreasChipRow(
    unfiledCount: Int,
    onOpenFocus: () -> Unit,
    onOpenInbox: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(top = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ClarityChip(
            label = stringResource(R.string.areas_chip_focus),
            onClick = onOpenFocus,
        )

        // The Pulse chip, design-v3.md 10.1, is phase 6. It is absent rather than
        // present and inert, which is what phase 2 decided about both of these and why
        // the Focus chip was not built until it had a surface to open.

        if (unfiledCount > 0) {
            InboxChip(count = unfiledCount, onClick = onOpenInbox)
        }
    }
}

/**
 * design-v3.md 10.16 and Addendum 01 4a. **The count is the label.**
 *
 * Never a badge and never a red dot. The addendum forbids it twice and design-v3.md
 * 14 forbids the color treatment that would carry it, so the chip is an ordinary
 * unselected `ClarityChip`: card colored, soft elevation, no dot, no accent. An app
 * that answers a person writing something down with a scolding number teaches them
 * to stop writing things down, and that is a worse outcome than an unsorted inbox.
 *
 * There is no entry point at all when the inbox is empty, which is why the caller
 * decides whether this composes rather than this drawing a zero.
 *
 * The obvious answer was a pinned row at the top of the area list carrying a count.
 * design-v3.md 10.16 rejects it in writing and section 15 is the rule behind it: a
 * pinned pile of what has not been dealt with would sit above the one thing a person
 * opened the app to see, every single time.
 */
@Composable
private fun InboxChip(count: Int, onClick: () -> Unit) {
    val description = pluralStringResource(R.plurals.cd_areas_chip_inbox, count, count)
    ClarityChip(
        label = stringResource(R.string.areas_chip_inbox, count),
        onClick = onClick,
        modifier = Modifier.semantics { contentDescription = description },
    )
}

/**
 * MASTER_BUILD_PROMPT 6.3. Never silent, never a technical dialog, never data loss.
 * The sentence is assembled from snapshots carried in the events themselves, so it
 * still reads correctly if the area has since been renamed.
 */
@Composable
private fun ConflictCard(conflict: ConflictCardModel, onDismiss: () -> Unit) {
    val colors = LocalClarityColors.current
    val type = LocalClarityTypography.current
    var visible by remember(conflict.id) { mutableStateOf(true) }
    val motion = clarityMotion()

    AnimatedVisibility(
        visible = visible,
        exit = fadeOut(motion.easeOut()) + shrinkVertically(motion.springGentle()),
    ) {
        ClarityCard(modifier = Modifier.fillMaxWidth(), colors = colors) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = stringResource(
                        R.string.conflict_two_active,
                        conflict.areaName,
                        conflict.winnerTitle,
                        conflict.loserTitle,
                    ),
                    style = type.body,
                    color = colors.inkPrimary,
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.conflict_dismiss),
                    style = type.bodyStrong,
                    color = colors.actionBlue,
                    modifier = Modifier.clarityClickable {
                        visible = false
                        onDismiss()
                    },
                )
            }
        }
    }
}

/** design-v3.md 10.13. An invitation, never a scold. No illustration, no mascot. */
@Composable
private fun AreasEmptyState(onCreate: () -> Unit) {
    val colors = LocalClarityColors.current
    val type = LocalClarityTypography.current
    val motion = clarityMotion()
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { shown = true }

    // design-v3.md 8.2 item 25, written out because motion.easeOut is the 350ms
    // entrance curve with no delay and this one is 400ms after a 150ms wait. The delay
    // is what stops it flashing during a load that resolves quickly, so 8.4 keeps it
    // when motion is reduced or calm mode is on and shortens only the fade. This is the
    // one entrance the once per session rule does not govern: it is a guard rather than
    // an announcement, and it fires whenever an empty state appears.
    val entrance = tween<Float>(
        durationMillis = if (motion.reduced) 150 else 400,
        delayMillis = 150,
        easing = EaseOutCubic,
    )

    AnimatedVisibility(visible = shown, enter = fadeIn(entrance)) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(top = 60.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.areas_empty_title),
                style = type.readSerif,
                color = colors.inkPrimary,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.areas_empty_body),
                style = type.body,
                color = colors.inkSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp),
            )
            Spacer(Modifier.height(24.dp))
            ClarityButton(
                label = stringResource(R.string.areas_empty_action),
                onClick = onCreate,
                role = ClarityButtonRole.SECONDARY,
                fillWidth = false,
            )
        }
    }
}
