package com.kamsiob.claritynow.ui.areas

import com.kamsiob.claritynow.ui.theme.groundLight
import androidx.compose.runtime.derivedStateOf
import java.time.format.DateTimeFormatter
import java.time.LocalDate
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import android.text.format.DateFormat
import androidx.compose.ui.graphics.Brush
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
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
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
    onOpenManageAreas: () -> Unit,
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
    onNewArea: () -> Unit,
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

    val cardHeightPx = with(density) { (CARD_HEIGHT_ESTIMATE + ClaritySpacing.cardGap).toPx() }
    val ordered = remember(state.areas, reorder.previewOrder) {
        val preview = reorder.previewOrder
        if (preview == null) state.areas else preview.mapNotNull { id -> state.areas.firstOrNull { it.id == id } }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            // **Not a flat fill.** `groundLight` carries the whole argument: a field of
            // one value has no anywhere in it, so the empty part of this screen read as
            // vacancy rather than as room. Two and a half percent of lightness, one
            // radial pair, no hue shift, no animation.
            .background(colors.canvas)
            .groundLight(colors)
            // A tap anywhere while a row is open closes it, and is spent doing so.
            .clarityClickable(enabled = swipe.hasOpenRow, haptic = null) { swipe.close() },
    ) {
        // Read once, because both the content padding and the phase 12b scroll edge
        // fade are measured from them. design-v3.md 6.1.
        val statusBar = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
        val navigationBar = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

        // **The plate's ground, drawn behind the list rather than inside it.**
        //
        // `scrollEdgeFade` composites the list into an offscreen layer and erases its
        // alpha across the status bar, which is what stops a card clipping at a hard
        // pixel edge under the clock. A ground painted inside that layer is erased with
        // everything else, so the parchment arrived at the top of the screen as a
        // gradient into canvas: a soft top edge on a region whose whole separation
        // device is a hard one, and the exact treatment `design-v3.md` 15.1 lists.
        //
        // Behind the layer, the ground is crisp to y = 0 and the fade does what it was
        // written for: the dateline and the doors dissolve into the parchment they sit
        // on, rather than the parchment dissolving into the page.
        //
        // Its height tracks the plate's own bottom edge through the list's layout
        // information, so it scrolls away exactly with the item it belongs to and is
        // zero once that item is gone. No second measurement and no guessed constant.
        val plateBottom by remember(listState) {
            derivedStateOf {
                val plate = listState.layoutInfo.visibleItemsInfo
                    .firstOrNull { it.key == "header" }
                    ?: return@derivedStateOf 0
                (plate.offset + plate.size).coerceAtLeast(0)
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(with(density) { plateBottom.toDp() })
                .background(colors.parchment),
        )

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
            // **No horizontal padding on the list, and that is what lets the plate
            // bleed.** Content padding insets every item equally, so a full width
            // region was impossible while it was set here. Each item that wants the
            // measure now takes `screenPadding` itself, and the plate takes none.
            //
            // No top padding either: the plate carries the status bar inset inside its
            // own padding, so its ground reaches y = 0 and the clock sits on parchment
            // rather than on a strip of canvas above it.
            contentPadding = PaddingValues(
                bottom = navigationBar + TabBarHeight + TabBarInset + 76.dp,
            ),
            // `cardGap`, which is `scaled(snug)`, rather than the 11dp literal that was
            // here. Eleven is off the 4dp grid and off a token that already existed at
            // 12 with no call sites, and the same 11 was repeated in the drag
            // arithmetic, so the two could drift.
            verticalArrangement = Arrangement.spacedBy(ClaritySpacing.cardGap),
        ) {
            // design-v3.md 8.2 item 4 and 8.4. The screen arrives as one sequence, the
            // title first and the cards behind it, on the first open of this tab per
            // app session and never again. Everything about when it fires, and whether
            // it fires at all, is inside Modifier.clarityEntrance.
            //
            // **This comment was false for two passes and is true again.** `TabEntrance`
            // had been changed to replay on every entry, so returning to this tab
            // re-assembled the whole screen over 748ms, every time. 8.4 is explicit that
            // an entrance which fires on every open is a toll rather than delight, and
            // the authority order gives it the last word. A comment that asserts the
            // opposite of what ships is the worse half of that defect, because the next
            // reader reasons from it.
            //
            // The conflict cards deliberately take no entrance index. A conflict card is
            // an interruption that arrives when a merge produced one, carrying its own
            // reveal, rather than part of the screen's resting content.
            item(key = "header") {
                AreasPlate(
                    today = state.today,
                    unfiledCount = state.unfiledCount,
                    pulseReady = state.pulseReady,
                    onOpenFocus = onOpenFocus,
                    onOpenPulse = onOpenPulse,
                    onOpenArchive = onOpenArchive,
                    onOpenManageAreas = onOpenManageAreas,
                    onOpenSettings = { settingsOpen = true },
                    onOpenInbox = onOpenInbox,
                )
            }

            // The plate's hard bottom edge to the first card is `rest`, and the list's
            // own `cardGap` supplies 12 of it. A page's regions are separated by more
            // than its rows are, or there are no regions.
            item(key = "plateGap") { Spacer(Modifier.height(ClaritySpacing.rest - ClaritySpacing.snug)) }

            items(state.conflicts, key = { "conflict:${it.id}" }) { conflict ->
                Box(modifier = Modifier.padding(horizontal = ClaritySpacing.screenPadding)) {
                    ConflictCard(conflict = conflict, onDismiss = { onDismissConflict(conflict.id) })
                }
            }

            if (state.isEmpty) {
                item(key = "empty") {
                    Box(modifier = Modifier.padding(horizontal = ClaritySpacing.screenPadding)) {
                        AreasEmptyState(onCreate = onFabClick)
                    }
                }
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
                        .padding(horizontal = ClaritySpacing.screenPadding)
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
                        onLongPress = { onLongPressArea(area) },
                        onMoveToFront = if (index == 0) null else { { onMoveArea(area.id, 0) } },
                        onPromotionPlayed = { onPromotionPlayed(area.id) },
                    )
                }
            }

            // **The door that was not there.** Until now nothing in the app created a
            // second area: `AreaSheet.NewArea` had one trigger and it was gated on there
            // being no areas at all, and the inbox's own `Create an area` was gated the
            // same way. A person who finished onboarding with three areas had three
            // forever, and the only recoveries were deleting all of them or replaying
            // the welcome, which silently made a second area with the same name.
            //
            // At the foot of the list rather than in the header, and that is the
            // deliberate choice rather than the obvious one. A glyph beside the archive
            // and settings icons would be one more unlabeled 24dp target in a corner,
            // which is the shape the two people in testing who read labels never find.
            // The end of a list is where every list app on this platform puts `add one
            // more`, it is reached by the scroll a person is already doing, and it can
            // carry a word.
            if (!state.isEmpty) {
                item(key = "newAreaGap") {
                    Spacer(Modifier.height(ClaritySpacing.rest - ClaritySpacing.snug))
                }
                item(key = "newArea") {
                    // The row carries `cardPaddingHorizontal` inside its own tap target,
                    // so the measure is 2dp short of a card's edge here rather than 20
                    // plus 18. The plus glyph lands on the edge the cards do.
                    Box(
                        modifier = Modifier.padding(
                            start = ClaritySpacing.screenPadding - ClaritySpacing.cardPaddingHorizontal,
                        ),
                    ) {
                        NewAreaRow(onClick = onNewArea)
                    }
                }
            }
        }

        // **Not composed at all while the list is empty.** `AreasEmptyState` names the
        // same action in a labeled button 400dp higher up, calling the same lambda, so
        // an empty screen offered one job twice: once in a word and once as an unlabeled
        // circle in the corner. The button is the one that survives, because it says
        // what it does.
        if (!state.isEmpty) {
            ClarityFab(
                onClick = onFabClick,
                contentDescription = stringResource(R.string.fab_add_item),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    // Clears the floating tab bar: its own inset, its height, and a gap
                    // so the two never read as one control.
                    //
                    // `TabBarInset` on the end as well, where a 20dp literal used to
                    // sit. The bar below insets itself by 17, so the two objects that
                    // sit closest together on the whole page were 3dp out of line with
                    // each other.
                    .padding(
                        end = TabBarInset,
                        bottom = TabBarHeight + TabBarInset + 14.dp,
                    )
                // **After the padding, and that is the whole of the fix.** A modifier to
                // the left of another wraps it, so `onGloballyPositioned` reports the node
                // including everything to its right. Registered first, this reported the
                // FAB plus the navigation bar inset plus 20dp of end padding plus the
                // 92dp that clears the tab bar, and the tutorial lit the entire bottom
                // right corner of the screen instead of the button. `ClarityShell` states
                // the same rule at the tab bar's own target, where it was got right.
                    .tutorialTarget(TutorialStep.FAB),
            )
        }

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
    reorderModifier: Modifier,
    onOpen: () -> Unit,
    onComplete: () -> Unit,
    onSwap: () -> Unit,
    onDelete: () -> Unit,
    onLongPress: () -> Unit,
    onMoveToFront: (() -> Unit)?,
    onPromotionPlayed: () -> Unit,
) {
    val colors = LocalClarityColors.current
    val shape = RoundedCornerShape(18.dp)
    val moreActionsLabel = stringResource(R.string.cd_more_actions)
    val reorderLabel = stringResource(R.string.cd_reorder)

    val actions = SwipeActions(
        completeLabel = stringResource(R.string.action_complete),
        swapLabel = stringResource(R.string.action_swap),
        deleteLabel = stringResource(R.string.action_delete),
        onComplete = if (area.offersComplete) onComplete else null,
        // **Swap is offered on an idle area that has a queue, which is new.**
        //
        // `offersSwap` required an active item, so an area whose active slot is empty and
        // whose queue is full revealed Delete and nothing else. That is the ordinary
        // result of dismissing the swap chooser, of completing inside a focus session with
        // `Choose from queue` set, and of the re-entry screen's second option, which puts
        // every area into that state at once. Two usability testers swiped such a card to
        // see what it offered and found only the destructive action.
        //
        // The verb does not change meaning: Swap has always meant "choose which item is
        // active from this area's queue", and the chooser already handles there being
        // nothing to demote, because `SwapChooserSheet.demotedTitle` is nullable and says
        // so.
        onSwap = if (area.offersSwap || area.offersPromote) onSwap else null,
        // **The last card offers delete too, and it used to be the one card that did
        // not.** The reasoning was that reaching zero areas should be deliberate, and
        // the effect was that the swipe on a brand new person's only card revealed
        // nothing at all: Complete needs an active item, Swap needs a queue, and Delete
        // was suppressed, so the first gesture the app teaches did nothing on the first
        // card it is tried on. Deliberateness is already carried by the thing that
        // carries it everywhere else, `DeleteAreaSheet` and its typed confirmation, and
        // an empty Areas list is not a hole to fall into: it has an empty state that
        // invites making one, which is the screen this app opens on for everybody on
        // their first day.
        onDelete = onDelete,
    )

    SwipeableRow(
        key = area.id,
        coordinator = swipe,
        actions = actions,
        shape = shape,
        // The two gestures beyond a tap. Both strings were written in phase 2 and had no
        // call site until now, so a screen reader was told about neither the actions menu
        // nor the reordering.
        extraActions = listOfNotNull(
            CustomAccessibilityAction(moreActionsLabel) { onLongPress(); true },
            // Reordering is a long press and drag with no non-gesture path at all. `Move
            // to front` is the reachable half of the same intent.
            onMoveToFront?.let { move -> CustomAccessibilityAction(reorderLabel) { move(); true } },
        ),
    ) { rowActions ->
        // design-v3.md 8.2 item 2 gives a card the same 0.97 press as a button, and
        // section 9 gives a card press the tap haptic. The largest target in the app
        // had neither: `clarityClickable` sets `indication = null` deliberately, and
        // nothing had been put in its place.
        val cardInteraction = remember { MutableInteractionSource() }
        ClarityCard(
            modifier = Modifier
                .fillMaxWidth()
                // The swipe row's three custom actions, on the node that also carries the
                // card's description and its click, which is the node a screen reader
                // actually stops on.
                // Carries all five actions: the three swipe verbs plus the two gestures
                // this card supports beyond a tap, built as one list inside SwipeableRow.
                .then(rowActions)
                .then(reorderModifier)
                .clarityPressScale(cardInteraction, label = "areaCardPress")
                .clarityClickable(
                    interactionSource = cardInteraction,
                    haptic = ClarityHapticEvent.TAP,
                    // The card answers with a scale, one line above. A veil as well would
                    // be two press treatments on one element.
                    showPress = false,
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
                // **The card speaks as one node.** `AreaCardSemantics` was written in
                // phase 2, documented as "kept next to the card so the semantics and the
                // visuals cannot drift apart", and never called from anywhere, so the
                // largest and most important object in the app reached TalkBack as five
                // loose nodes: a dot with no name, an area label, a title, a first step
                // and a status line, in that order, with no statement of what they are.
                // It goes inside the clickable rather than above it, or
                // `clearAndSetSemantics` would wipe the click action with them.
                modifier = AreaCardSemantics(area),
            )
        }
    }
}

/**
 * The plate. `design-v3.md` 10.1 and 10.2, rewritten.
 *
 * ## What it replaces, and why four things became one
 *
 * This region used to be four objects stacked on the page ground: a serif wordmark row,
 * the weekly banner, a row of two elevated white pills with a third chip under them,
 * and a hairline rule to separate all of that from the areas. Every one of them was
 * drawn on `canvas` in a rounded shape at one measure, so the screen read as a stack of
 * pale rectangles with a line across it, and the line existed only because the two
 * regions it divided were made of the same material.
 *
 * **The plate is one full bleed region of `parchment` with a hard top and a hard
 * bottom.** Its only separation device is the ground change itself, `#EFEEE2` against
 * `#D6D6DB`, which is eight steps of lightness and a shift from cool to warm; there is
 * no shadow, no border, no radius and no gradient on it. That is `design-v3.md` 6.1's
 * one device, and it is what lets the hairline go.
 *
 * It spends a token that was very nearly dead: `parchment` had one live call site in the
 * whole app, and 3.1 assigns it to exactly this content.
 *
 * ## Why the app's name is gone
 *
 * `readSerif` at 26sp on the word `Clarity Now` was the largest type on the app's own
 * home screen, sitting above a tab bar whose selected item already says `Areas`. The
 * biggest type role in the product was spent naming the app to the person who had just
 * opened it. In its place is the date, which is a fact that person may actually want:
 * time blindness is documented in this audience, `what day is it` is a real question,
 * and the Report and the Trail both carry a dateline already.
 *
 * The statistically common replacement is a greeting, with or without a name.
 * `design-v3.md` 15 refuses it twice over: it would be a sentence about the person
 * written in a composable rather than drawn from a corpus through the engine, which
 * `CLAUDE.md` rule 8 forbids outright, and it is the single most recognizable
 * assistant app opening of the year.
 *
 * ## Why the doors have no pill any more
 *
 * They cannot keep one. On parchment, `card` measures 1.09:1 and `raise` measures
 * 1.01:1; no neutral in the palette separates from this ground. That is a measurement
 * rather than a preference, and it is the whole reason Focus, the Pulse and the inbox
 * became three bare labels in a `FlowRow` instead of two centered pills and a chip.
 *
 * What they gain is a leading edge. Centered content inside two `weight(1f)` boxes sat
 * on no alignment at all, which was the screen's fourth left edge by way of having
 * none. Hugging labels on the 20dp measure sit on the same edge as everything else and
 * terminate ragged, which is a shape.
 *
 * **The press ground is a pill of ink at 6 percent and it exists only while a finger is
 * down.** A press treatment is state and not a separation device, which `Interactions.kt`
 * already establishes for the app's other unhoused controls.
 *
 * ## The doors sit above the sentence, not below it
 *
 * The sentence is variable in height and often absent. With the doors under it, the two
 * permanent controls of this screen moved 52 to 92dp between one opening and the next,
 * which is COGA o4p01 happening on the surface the owner opens every morning. Above it,
 * their y depends on the status bar inset and nothing else, and the sentence grows
 * downward into the plate.
 */
@Composable
private fun AreasPlate(
    today: LocalDate?,
    unfiledCount: Int,
    pulseReady: Boolean,
    onOpenFocus: () -> Unit,
    onOpenPulse: () -> Unit,
    onOpenArchive: () -> Unit,
    onOpenManageAreas: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenInbox: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalClarityColors.current
    val type = LocalClarityTypography.current
    val statusBar = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    Column(
        modifier = modifier
            .fillMaxWidth()
            // **No background here.** The parchment is drawn by a sibling behind the
            // whole list, in `AreasScreen`, and `PlateGround` carries why: the scroll
            // edge fade erases the list's own alpha, so a ground painted inside the list
            // would be erased along with the text and the plate would arrive at the top
            // of the screen as a gradient rather than as an edge.
            .clarityEntrance(0, ClarityEntranceRole.HEADER)
            .padding(
                start = ClaritySpacing.screenPadding,
                end = ClaritySpacing.screenPadding,
                top = statusBar + ClaritySpacing.snug,
                bottom = ClaritySpacing.step,
            ),
    ) {
        // The three doors out of this screen, on a row of their own.
        //
        // **The glyph trio is pushed out by 12dp so its ink lands on the measure.** A
        // 22dp glyph centered in a 48dp target sits 13dp inside its own box, so a
        // trailing target flush with the 20dp padding put the settings gear 13dp short
        // of the right measure while every other element on the screen terminated on it.
        // The targets keep their size and move; the ink lands where the eye expects an
        // edge.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                // `heightIn`, not `height`. A fixed height constrains its children, so
                // the three glyphs that correctly declare `size(48.dp)` measured 48 by 44.
                .heightIn(min = ClaritySpacing.scaled(48.dp))
                .offset(x = 12.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HeaderGlyph(
                icon = ClarityIcons.archive,
                label = stringResource(R.string.areas_open_archive),
                tint = colors.inkSecondary,
                onClick = onOpenArchive,
            )
            HeaderGlyph(
                icon = ClarityIcons.manageAreas,
                label = stringResource(R.string.cd_manage_areas_open),
                tint = colors.inkSecondary,
                onClick = onOpenManageAreas,
            )
            HeaderGlyph(
                icon = ClarityIcons.settings,
                label = stringResource(R.string.cd_settings_open),
                tint = colors.inkSecondary,
                onClick = onOpenSettings,
            )
        }

        // **The date is the masthead, in the app's own serif, at the app's own headline
        // rank.**
        //
        // This is the correction of the change that removed the wordmark. Taking
        // `Clarity Now` off the home screen was right: it was the largest type in the
        // product, spent naming the app to somebody who had just opened it. Putting a
        // 12.5sp grey sans dateline in its place was not, and the owner's verdict on
        // that build was that it looked worse than before. What left with the wordmark
        // was the only serif on the screen, which is to say the app's voice.
        //
        // The two surfaces in this app that are admired, the Report and Momentum, both
        // open with a large serif line on a bare ground. The Areas screen is the one
        // that opens with a stack of containers and no voice at all, and that is the
        // difference a person feels before they read a word. So the date takes
        // `readSerif` and `inkPrimary`: it is the app speaking, section 5.1, and it says
        // something a person with time blindness actually wants to know.
        //
        // It carries the -0.06em stem correction Newsreader needs at this size, which is
        // the same offset the banner below it takes.
        Spacer(Modifier.height(ClaritySpacing.tight))
        Text(
            // Absent rather than a placeholder for the frame before the projection
            // lands. A dateline is either right or it is not there.
            text = today?.let { dateline(it) }.orEmpty(),
            style = type.readSerif,
            color = colors.inkPrimary,
            // Two, so a long locale date wraps rather than ellipsizing a date.
            maxLines = 2,
            modifier = Modifier.offset(x = (-2).dp),
        )

        // **Above the sentence, not below it**, which is the one thing about this block
        // that is load bearing. The sentence is variable in height and often absent, so
        // doors underneath it moved 52 to 92dp between one opening and the next: COGA
        // o4p01, on the surface the owner opens every morning. Above it, their y depends
        // on the status bar inset and the date, both of which are constant.
        Spacer(Modifier.height(ClaritySpacing.snug))
        AreasDoors(
            unfiledCount = unfiledCount,
            pulseReady = pulseReady,
            onOpenFocus = onOpenFocus,
            onOpenPulse = onOpenPulse,
            onOpenInbox = onOpenInbox,
        )
        AreasBanner(
            modifier = Modifier
                .padding(top = ClaritySpacing.snug)
                .clarityEntrance(2, ClarityEntranceRole.DOMINANT),
        )
    }
}

/**
 * Wednesday 3 September, in whatever shape the reader's locale writes that.
 *
 * `getBestDateTimePattern` and not a literal `EEEE d MMMM`. The app has five other date
 * formatters and all five hardcode both an English word order and `Locale.US`, which is
 * a defect this one declines to make a sixth time: a skeleton names the fields that
 * should appear and lets the platform decide their order and their separators, so a
 * device set to Japanese gets a Japanese dateline rather than an English one rendered in
 * Japanese words.
 *
 * The year is deliberately not in the skeleton. A dateline on a home screen answers
 * `what day is it`, and nobody opening this app is unsure what year it is.
 */
@Composable
private fun dateline(date: LocalDate): String {
    val locale = LocalConfiguration.current.locales[0]
    val formatter = remember(locale) {
        DateTimeFormatter.ofPattern(
            DateFormat.getBestDateTimePattern(locale, DATELINE_SKELETON),
            locale,
        )
    }
    return remember(date, formatter) { date.format(formatter) }
}

private const val DATELINE_SKELETON = "EEEEdMMMM"

/**
 * Focus, the Pulse, and the inbox when it holds something.
 *
 * **A `FlowRow` and not a `Row` of weights.** Two halves of the measure forced both
 * labels to be centered in a box they did not fill; hugging content leaves the row's
 * right end ragged, which is the only ragged terminal on a screen where everything else
 * runs to 371.4dp. At the 200 percent text scale `Today's Pulse` no longer fits beside
 * `Focus` and wraps to a second line instead of clipping, which is the other thing the
 * weights could not do.
 *
 * **Focus is permanent and present even when nothing can be focused on**, which 10.1
 * states and this rewrite does not change. The chooser has an empty state of its own,
 * and a control that leads to an explanation beats a control that vanishes.
 *
 * **A waiting Pulse changes its label as well as growing a dot**, and that is a defect
 * fix rather than a flourish. `areas_chip_pulse_ready` has existed since phase 6 and was
 * referenced by nothing, so the only signal that a Pulse was ready was a 9dp amber dot:
 * one channel, which section 13 forbids, and which 10.1 says in as many words is not the
 * signal on its own.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AreasDoors(
    unfiledCount: Int,
    pulseReady: Boolean,
    onOpenFocus: () -> Unit,
    onOpenPulse: () -> Unit,
    onOpenInbox: () -> Unit,
) {
    val colors = LocalClarityColors.current
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            // The doors carry 12dp of their own so the press pill has something to
            // fill, so the block moves out by the same 12 to put the first label's ink
            // back on the measure.
            .offset(x = (-12).dp)
            .clarityEntrance(1, ClarityEntranceRole.ROW),
        horizontalArrangement = Arrangement.spacedBy(ClaritySpacing.tight),
        verticalArrangement = Arrangement.spacedBy(ClaritySpacing.hair),
    ) {
        AreasDoor(
            icon = ClarityIcons.focus,
            label = stringResource(R.string.areas_chip_focus),
            dotColor = null,
            onClick = onOpenFocus,
            modifier = Modifier.tutorialTarget(TutorialStep.FOCUS_CHIP),
        )
        AreasDoor(
            icon = ClarityIcons.pulse,
            label = stringResource(
                if (pulseReady) R.string.areas_chip_pulse_ready else R.string.areas_chip_pulse,
            ),
            dotColor = if (pulseReady) colors.warnAmber else null,
            onClick = onOpenPulse,
            modifier = Modifier.tutorialTarget(TutorialStep.PULSE_CHIP),
        )
        if (unfiledCount > 0) {
            val description = pluralStringResource(
                R.plurals.cd_areas_chip_inbox,
                unfiledCount,
                unfiledCount,
            )
            AreasDoor(
                // 7.2 assigns the inbox no glyph, so it has none here either, and the
                // count rides in the label rather than in a badge. 10.16 and Addendum
                // 01 4a.
                icon = null,
                label = stringResource(R.string.areas_chip_inbox, unfiledCount),
                dotColor = null,
                onClick = onOpenInbox,
                modifier = Modifier.semantics { contentDescription = description },
            )
        }
    }
}

/**
 * One door. A label, its glyph, and a press ground that is there only while pressed.
 */
@Composable
private fun AreasDoor(
    @DrawableRes icon: Int?,
    label: String,
    dotColor: Color?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalClarityColors.current
    val type = LocalClarityTypography.current
    val shapes = LocalClarityShapes.current
    val interaction = remember { MutableInteractionSource() }

    Row(
        modifier = modifier
            .heightIn(min = ClaritySpacing.minTouchTarget)
            .clip(shapes.pill)
            .clarityFocusRing(interaction, shapes.pill)
            .clarityClickable(
                interactionSource = interaction,
                haptic = ClarityHapticEvent.TAP,
                role = Role.Button,
                pressShape = shapes.pill,
                onClickLabel = label,
                onClick = onClick,
            )
            .padding(horizontal = ClaritySpacing.snug),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (dotColor != null) {
            // A live state takes the dot; an idle one takes its own glyph. The two
            // never appear together, so the row never grows.
            Box(
                modifier = Modifier
                    .size(ClaritySpacing.areaDot)
                    .clip(CircleShape)
                    .background(dotColor),
            )
            Spacer(Modifier.width(ClaritySpacing.tight))
        } else if (icon != null) {
            ClarityIcon(
                icon = icon,
                contentDescription = null,
                tint = colors.inkSecondary,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(ClaritySpacing.tight))
        }
        Text(text = label, style = type.bodyStrong, color = colors.inkPrimary)
    }
}

/**
 * One of the three targets at the top right of the plate.
 *
 * **It had no focus ring and no role.** `clarityClickable` supplies neither on its own,
 * so a keyboard or a switch landing on the archive glyph showed nothing at all, and
 * TalkBack announced the label without saying it was a button. Every other control in
 * the app declares both; these three were written in phase 2 and never revisited.
 */
@Composable
private fun HeaderGlyph(
    @DrawableRes icon: Int,
    label: String,
    tint: Color,
    onClick: () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val shape = RoundedCornerShape(24.dp)
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(shape)
            .clarityFocusRing(interaction, shape)
            .clarityClickable(
                interactionSource = interaction,
                role = Role.Button,
                pressShape = shape,
                onClickLabel = label,
                onClick = onClick,
            ),
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

/**
 * `Add an area`, at the foot of the list. Issue: creating a second area was impossible.
 *
 * **A row and not a card.** Every card on this screen is a place that holds work, and a
 * card that held an invitation instead would be the one card whose title is not an item.
 * `design-v3.md` 6.1 gives an element one separation device and this one's is whitespace:
 * no fill, no elevation, no hairline, dashed or otherwise. A dashed outline is the tell
 * that a placeholder is standing in for content, and nothing is missing here.
 *
 * It reads as quieter than a card and louder than a caption, which is what an invitation
 * at the end of a list is. The glyph is the same `add` the FAB uses, so the two controls
 * that make something new share a mark.
 */
@Composable
private fun NewAreaRow(onClick: () -> Unit) {
    val colors = LocalClarityColors.current
    val type = LocalClarityTypography.current
    val interaction = remember { MutableInteractionSource() }
    val shape = LocalClarityShapes.current.card

    // **It hugs its content rather than filling the width, and that is a collision fix
    // rather than a preference.** The row was `fillMaxWidth`, so with four areas on the
    // list it ran underneath the floating plus button: a full width tap target with an
    // opaque circle sitting on its right half, which is both a visual overlap and a
    // hit testing one. Hugging keeps the target on the words a person is aiming at.
    Row(
        modifier = Modifier
            .heightIn(min = ClaritySpacing.minTouchTarget)
            .clip(shape)
            .clarityFocusRing(interaction, shape)
            .clarityClickable(
                interactionSource = interaction,
                haptic = ClarityHapticEvent.TAP,
                role = Role.Button,
                pressShape = shape,
                onClick = onClick,
            )
            .padding(
                horizontal = ClaritySpacing.cardPaddingHorizontal,
                vertical = ClaritySpacing.scaled(14.dp),
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ClarityIcon(
            icon = ClarityIcons.add,
            contentDescription = null,
            tint = colors.inkSecondary,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(ClaritySpacing.snug))
        Text(
            text = stringResource(R.string.areas_new_area),
            style = type.label,
            color = colors.inkSecondary,
        )
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

    // **Left aligned on the measure, not centered.** Centering is the half of the
    // conventional empty state that nobody questions, and every other block on this
    // screen sits on the 20dp edge. A centered title, a centered paragraph inset by a
    // further 24dp, and a centered button made three more alignments on a page that was
    // already short of them, on the one screen a person sees on their first day.
    // design-v3.md 15.
    AnimatedVisibility(visible = shown, enter = fadeIn(entrance)) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(top = ClaritySpacing.rest),
        ) {
            Text(
                text = stringResource(R.string.areas_empty_title),
                style = type.readSerif,
                color = colors.inkPrimary,
            )
            Spacer(Modifier.height(ClaritySpacing.scaled(10.dp)))
            Text(
                text = stringResource(R.string.areas_empty_body),
                style = type.body,
                color = colors.inkSecondary,
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
