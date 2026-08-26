package com.kamsiob.claritynow.ui.areas

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.kamsiob.claritynow.R
import com.kamsiob.claritynow.ui.components.ClarityButton
import com.kamsiob.claritynow.ui.components.ClarityButtonRole
import com.kamsiob.claritynow.ui.components.ClarityCard
import com.kamsiob.claritynow.ui.components.ClarityFab
import com.kamsiob.claritynow.ui.components.ClarityIcon
import com.kamsiob.claritynow.ui.components.ClarityIcons
import com.kamsiob.claritynow.ui.components.SwipeActions
import com.kamsiob.claritynow.ui.components.SwipeCoordinator
import com.kamsiob.claritynow.ui.components.SwipeableRow
import com.kamsiob.claritynow.ui.components.TabBarHeight
import com.kamsiob.claritynow.ui.components.clarityClickable
import com.kamsiob.claritynow.ui.components.rememberReorderState
import com.kamsiob.claritynow.ui.components.rememberSwipeCoordinator
import com.kamsiob.claritynow.ui.components.reorderableItem
import com.kamsiob.claritynow.ui.theme.LocalClarityColors
import com.kamsiob.claritynow.ui.theme.LocalClarityTypography
import com.kamsiob.claritynow.ui.theme.clarityMotion
import com.kamsiob.claritynow.ui.theme.parseAreaColor

/** The height one area card settles at, used by the drag reorder arithmetic. */
private val CARD_HEIGHT_ESTIMATE = 96.dp

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
            item(key = "header") {
                AreasHeader(onOpenArchive = onOpenArchive)
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
        ClarityCard(
            modifier = Modifier
                .fillMaxWidth()
                .then(reorderModifier)
                .clarityClickable(haptic = null, onClick = {
                    if (swipe.hasOpenRow) swipe.close() else onOpen()
                }),
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

@Composable
private fun AreasHeader(onOpenArchive: () -> Unit) {
    val colors = LocalClarityColors.current
    val type = LocalClarityTypography.current
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 6.dp),
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

    AnimatedVisibility(visible = shown, enter = fadeIn(motion.easeOut())) {
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
