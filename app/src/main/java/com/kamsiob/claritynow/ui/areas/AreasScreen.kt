package com.kamsiob.claritynow.ui.areas

import androidx.annotation.DrawableRes
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
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
import com.kamsiob.claritynow.ui.components.ScrollEdge
import com.kamsiob.claritynow.ui.components.SwipeActions
import com.kamsiob.claritynow.ui.components.SwipeCoordinator
import com.kamsiob.claritynow.ui.components.SwipeableRow
import com.kamsiob.claritynow.ui.components.TabBarHeight
import com.kamsiob.claritynow.ui.components.TabBarInset
import com.kamsiob.claritynow.ui.components.clarityClickable
import com.kamsiob.claritynow.ui.components.clarityPressScale
import androidx.compose.ui.semantics.Role
import com.kamsiob.claritynow.ui.components.clarityFocusRing
import com.kamsiob.claritynow.ui.components.clarityShadow
import com.kamsiob.claritynow.ui.components.rememberReorderState
import com.kamsiob.claritynow.ui.components.rememberSwipeCoordinator
import com.kamsiob.claritynow.ui.components.reorderableItem
import com.kamsiob.claritynow.ui.components.scrollEdgeFade
import com.kamsiob.claritynow.ui.momentum.AreasBanner
import com.kamsiob.claritynow.ui.settings.SettingsSurface
import com.kamsiob.claritynow.ui.theme.ClarityElevation
import com.kamsiob.claritynow.ui.theme.ClarityHapticEvent
import com.kamsiob.claritynow.ui.theme.ClarityEntranceRole
import com.kamsiob.claritynow.ui.theme.ClaritySpacing
import com.kamsiob.claritynow.ui.theme.LocalClarityColors
import com.kamsiob.claritynow.ui.theme.LocalClarityShapes
import com.kamsiob.claritynow.ui.theme.LocalClarityTypography
import com.kamsiob.claritynow.ui.theme.clarityEntrance
import com.kamsiob.claritynow.ui.theme.clarityMotion
import com.kamsiob.claritynow.ui.tutorial.TutorialStep
import com.kamsiob.claritynow.ui.tutorial.tutorialTarget

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
/** The header assembles in three beats of its own, so cards start at the fourth. */
private const val CARD_ENTRANCE_INDEX = 3

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
    onOpenPulse: () -> Unit,
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

    // The Settings destination, design-v3.md 10.15, held here rather than in
    // `ClarityShell`.
    //
    // **Held here rather than in the shell, and that stopped being a seam in phase
    // 12c.** 10.15 makes Settings a pushed screen over the tab it was entered from,
    // which has to cover the floating tab bar, and the shell draws that bar as a sibling
    // above the tab content. Phase 11 read that as meaning the screen had to be hoisted
    // into the shell to cover it and could not be, so the bar went on floating over
    // Settings. Issue #58 answered it the other way round: a pushed screen says it is
    // one, through `CoversTheTabBar`, and the shell reads that and does not draw the bar
    // at all. So the host no longer decides anything about the bar, and this is simply
    // where the glyph that opens it lives. `ui/nav/PushedScreens.kt` carries the whole
    // of that decision. The archive, 10.20, is the same arrangement one file up, in
    // `AreasRoute`, because its glyph already had a callback there.
    //
    // `rememberSaveable` rather than `remember`, because a tab switch takes this
    // screen's composition with it and coming back to a Settings screen that closed
    // itself while you were reading the Report is a screen that cannot be trusted.
    var settingsOpen by rememberSaveable { mutableStateOf(false) }

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
        // Read once, because both the content padding and the phase 12b scroll edge
        // fade are measured from them. design-v3.md 6.1.
        val statusBar = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
        val navigationBar = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                // Cards no longer pass hard edged under the clock or behind the floating
                // pill: they dissolve into the page at both ends. See `ScrollEdge.kt`
                // for why this erases rather than paints, and for why it is a fade and
                // not the blur design-v3.md 15.3 refuses.
                //
                // The fade sits on the list and not on the Box, so the FAB, which is a
                // sibling rather than a child, keeps its full weight. A FAB that faded
                // into the ground it floats above would be the one control on this
                // screen that is hardest to see and the one that most has to be seen.
                .scrollEdgeFade(
                    top = statusBar + ScrollEdge.underTheClock,
                    bottom = navigationBar + TabBarInset + TabBarHeight +
                        ScrollEdge.aboveTheBar,
                ),
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                // The list scrolls under the status bar rather than stopping at it,
                // so the first card passes behind the clock instead of clipping.
                top = statusBar + 8.dp,
                bottom = navigationBar + TabBarHeight + TabBarInset + 76.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(ClaritySpacing.scaled(11.dp)),
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
                    pulseReady = state.pulseReady,
                    onOpenFocus = onOpenFocus,
                    onOpenPulse = onOpenPulse,
                    onOpenArchive = onOpenArchive,
                    onOpenSettings = { settingsOpen = true },
                    onOpenInbox = onOpenInbox,
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
                        .clarityEntrance(CARD_ENTRANCE_INDEX + index, ClarityEntranceRole.ROW)
                        // Only the first card is a tutorial target. The step teaches
                        // what a card is, not which card this is, so the registry
                        // wants one rectangle rather than the topmost of several.
                        .then(
                            if (index == 0) {
                                Modifier.tutorialTarget(TutorialStep.AREA_CARD)
                            } else {
                                Modifier
                            },
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
                .tutorialTarget(TutorialStep.FAB)
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                // Clears the floating tab bar: its own 17dp inset, its height, and
                // a gap so the two never read as one control.
                .padding(end = 20.dp, bottom = TabBarHeight + 17.dp + 14.dp),
        )

        // Above everything this screen draws, including the FAB. It is not above the
        // floating tab bar, which is a sibling of this whole screen in `ClarityShell`
        // and is the seam recorded on `settingsOpen`.
        if (settingsOpen) {
            SettingsSurface(
                onDismiss = { settingsOpen = false },
                modifier = Modifier.zIndex(2f),
            )
        }
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
 * without it being redrawn. Focus arrived in phase 4 and Pulse in phase 6, in that
 * order and both permanent, and the unfiled inbox chip, 10.16, stays last and present
 * only while the inbox holds something, so it can never displace them.
 */
@Composable
private fun AreasHeader(
    unfiledCount: Int,
    pulseReady: Boolean,
    onOpenFocus: () -> Unit,
    onOpenPulse: () -> Unit,
    onOpenArchive: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenInbox: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalClarityColors.current
    val type = LocalClarityTypography.current
    Column(modifier = modifier.fillMaxWidth().padding(top = ClaritySpacing.snug)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .clarityEntrance(0, ClarityEntranceRole.HEADER),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.areas_title),
                style = type.readSerif,
                color = colors.inkPrimary,
                modifier = Modifier.weight(1f),
            )
            HeaderGlyph(
                icon = ClarityIcons.archive,
                label = stringResource(R.string.areas_open_archive),
                tint = colors.inkSecondary,
                onClick = onOpenArchive,
            )
            HeaderGlyph(
                icon = ClarityIcons.settings,
                label = stringResource(R.string.cd_settings_open),
                tint = colors.inkSecondary,
                onClick = onOpenSettings,
            )
        }
        AreasBanner(
            modifier = Modifier
                .padding(top = ClaritySpacing.snug)
                .clarityEntrance(1, ClarityEntranceRole.DOMINANT),
        )
        AreasChipRow(
            unfiledCount = unfiledCount,
            pulseReady = pulseReady,
            onOpenFocus = onOpenFocus,
            onOpenPulse = onOpenPulse,
            onOpenInbox = onOpenInbox,
        )
    }
}

@Composable
private fun HeaderGlyph(
    @DrawableRes icon: Int,
    label: String,
    tint: Color,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(24.dp))
            .clarityClickable(onClickLabel = label, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        ClarityIcon(
            icon = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(22.dp),
        )
    }
}

/**
 * design-v3.md 10.1 and 10.8. Pill chips in a horizontally scrolling row.
 *
 * **Every chip here is an ordinary unselected `ClarityChip`, and exactly one of them
 * can carry a dot.** 10.1 gives the permanent chips soft elevation and no border, and
 * phase 3c moved app chrome down one step of the value ladder, so an unselected chip
 * sits at `raise` rather than at `card` and this row inherits that from the component
 * rather than restating it. The one dot is the warnAmber Pulse dot in 10.1, which 3.1
 * scopes to that single use; it lives in [PulseChip] and nothing else in this row may
 * grow one. The inbox chip in particular carries a count in its label and never a
 * badge, per 10.16 and Addendum 01 4a.
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
/**
 * **Two anchors, not three chips, and each one says what it will do.**
 *
 * Focus and Pulse shipped as 38dp `ClarityChip`s in a horizontally scrolling row, the
 * same component and the same geometry as the Trail's filters, which set a filter, and
 * as the Inbox chip, which opens a sheet. One shape was doing navigation, filtering and
 * disclosure, and B.1 measured 26 of the app's 35 controls inside an 18dp band of
 * height. A screen with no rung ladder has no loud element and no quiet one, and that is
 * what "lifeless" is, mechanically.
 *
 * These are the Areas screen's two standing invitations, so they take the **Standard 48
 * rung at 56dp** and split the measure between them. Each carries a second line that is
 * a live readout rather than a label: `7:00 left` when a session is running, `ready now`
 * when a Pulse is waiting. **Only a chip with something true to say is filled**, so
 * color on this row is earned by state and never spent on decoration, which is 3.4's
 * rule applied to chrome instead of to an area.
 *
 * The Inbox chip keeps the old chip geometry deliberately and sits under the pair: it is
 * conditional, it is a disclosure rather than an invitation, and it should not look like
 * one of the two things this screen is for.
 */
@Composable
private fun AreasChipRow(
    unfiledCount: Int,
    pulseReady: Boolean,
    onOpenFocus: () -> Unit,
    onOpenPulse: () -> Unit,
    onOpenInbox: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = ClaritySpacing.step)
            .clarityEntrance(2, ClarityEntranceRole.ROW),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(ClaritySpacing.snug)) {
            AreasAnchor(
                icon = ClarityIcons.focus,
                label = stringResource(R.string.areas_chip_focus),
                readout = null,
                dotColor = null,
                onClick = onOpenFocus,
                modifier = Modifier.weight(1f).tutorialTarget(TutorialStep.FOCUS_CHIP),
            )
            AreasAnchor(
                icon = ClarityIcons.pulse,
                label = stringResource(R.string.areas_chip_pulse),
                readout = if (pulseReady) stringResource(R.string.areas_pulse_ready_now) else null,
                dotColor = if (pulseReady) LocalClarityColors.current.warnAmber else null,
                onClick = onOpenPulse,
                modifier = Modifier.weight(1f).tutorialTarget(TutorialStep.PULSE_CHIP),
            )
        }
        if (unfiledCount > 0) {
            Row(modifier = Modifier.padding(top = ClaritySpacing.snug)) {
                InboxChip(count = unfiledCount, onClick = onOpenInbox)
            }
        }
    }
}

@Composable
private fun AreasAnchor(
    @DrawableRes icon: Int,
    label: String,
    readout: String?,
    dotColor: Color?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalClarityColors.current
    val type = LocalClarityTypography.current
    val shapes = LocalClarityShapes.current
    val interaction = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .height(56.dp)
            .clarityPressScale(interaction, label = "anchorPress")
            .clarityShadow(ClarityElevation.card, shapes.pill, enabled = !colors.isDark)
            .clip(shapes.pill)
            .background(colors.card)
            .clarityFocusRing(interaction, shapes.pill)
            .clarityClickable(
                interactionSource = interaction,
                haptic = ClarityHapticEvent.TAP,
                role = Role.Button,
                onClickLabel = label,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (dotColor != null) {
                    // A live state takes the dot; an idle one takes its own glyph. The
                    // two never appear together, so the row never grows.
                    Box(
                        modifier = Modifier
                            .size(ClaritySpacing.areaDot)
                            .clip(CircleShape)
                            .background(dotColor),
                    )
                } else {
                    ClarityIcon(
                        icon = icon,
                        contentDescription = null,
                        tint = colors.inkSecondary,
                        modifier = Modifier.size(18.dp),
                    )
                }
                Spacer(Modifier.width(ClaritySpacing.tight))
                Text(text = label, style = type.bodyStrong, color = colors.inkPrimary)
            }
            if (readout != null) {
                Spacer(Modifier.height(2.dp))
                Text(text = readout, style = type.caption, color = colors.inkSecondary)
            }
        }
    }
}

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
                Spacer(Modifier.height(ClaritySpacing.scaled(12.dp)))
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
            modifier = Modifier.fillMaxWidth().padding(top = ClaritySpacing.scaled(60.dp)),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.areas_empty_title),
                style = type.readSerif,
                color = colors.inkPrimary,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(ClaritySpacing.scaled(10.dp)))
            Text(
                text = stringResource(R.string.areas_empty_body),
                style = type.body,
                color = colors.inkSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp),
            )
            Spacer(Modifier.height(ClaritySpacing.scaled(24.dp)))
            ClarityButton(
                label = stringResource(R.string.areas_empty_action),
                onClick = onCreate,
                role = ClarityButtonRole.SECONDARY,
                fillWidth = false,
            )
        }
    }
}
