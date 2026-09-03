package com.kamsiob.claritynow.ui.components

import androidx.compose.ui.text.style.TextAlign
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.kamsiob.claritynow.ui.theme.ClarityHapticEvent
import com.kamsiob.claritynow.ui.theme.LocalClarityColors
import com.kamsiob.claritynow.ui.theme.LocalClarityHaptics
import com.kamsiob.claritynow.ui.theme.LocalClarityTypography
import com.kamsiob.claritynow.ui.theme.clarityMotion
import com.kamsiob.claritynow.ui.theme.opticallyCentered
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * The three actions a swipe can reach. A null callback means the action is not
 * offered in this row's current state, which is how design-v3.md 10.3.1 gates an
 * idle area out of Complete and an area with an empty queue out of Swap.
 */
data class SwipeActions(
    val completeLabel: String,
    val swapLabel: String,
    val deleteLabel: String,
    val onComplete: (() -> Unit)? = null,
    val onSwap: (() -> Unit)? = null,
    val onDelete: (() -> Unit)? = null,
)

/**
 * Two open rows is a state nobody can reason about, so exactly one is open at a
 * time and opening a second closes the first. [enabled] carries the global disable
 * used while a drag reorder is running or any sheet is open.
 */
class SwipeCoordinator {
    var openKey by mutableStateOf<String?>(null)
        private set
    var enabled by mutableStateOf(true)

    fun open(key: String) { openKey = key }
    fun close() { openKey = null }
    fun closeIfOpen(key: String) { if (openKey == key) openKey = null }

    /** True when a tap should be spent closing the open row rather than passing through. */
    val hasOpenRow: Boolean get() = openKey != null
}

@Composable
fun rememberSwipeCoordinator(): SwipeCoordinator = remember { SwipeCoordinator() }

private const val REVEAL_FRACTION = 0.25f
private const val COMMIT_FRACTION = 0.55f
private const val FLING_DP_PER_SECOND = 1_200f
private val ACTION_WIDTH = 66.dp

/**
 * design-v3.md 10.3.1.
 *
 * Delete is never reachable by a full swipe. A full left swipe commits Swap, and
 * delete requires a deliberate tap on a specific target. Destructive actions must
 * not be committed by momentum.
 *
 * A gesture whose initial direction is predominantly vertical never becomes a
 * swipe: the horizontal drag only claims the pointer after horizontal touch slop,
 * so a list scroll that curves still scrolls.
 *
 * ## Every face carries its own color, and the phase 13 audit is why that is a token
 *
 * A face is its action's token at 12 to 18 percent over the page, deepening by 40
 * percent past the commit threshold, and the icon and the 10.5sp label on it are the
 * same color read at full strength. **So a swipe face is the one ground in the app where
 * a token has to be legible on its own tint**, and it was the weakest reading in the
 * whole app when it was measured: 3.40 on Complete, 3.66 on Delete in light and 2.93 in
 * dark, and 1.03 on Swap.
 *
 * Two of the three are answered by 3.1's tokens, which moved for this: `actionBlue`
 * carries Swap at 4.95 in light and 5.74 in dark, and `deleteMuted`, now one value per
 * world, carries Delete at 4.93 and 4.88. Complete takes `positiveInk` rather than
 * `positiveGreen`, because its face has to stay a light mint while its label has to be
 * dark, and it reads 4.91 and 5.42. That token replaces the literal `#15803D` this file
 * used to hold, which design-v3.md 10.3.1 named and which measured 3.40 on the face it
 * was drawn on: a hex in a component is a value nothing re-measures when a ground moves.
 *
 * **The Swap face no longer draws the area's accent, and that is the finding rather than
 * a simplification.** 10.3.1 asked for the area color here, and it fails on 43 of the 48
 * colors in light and 28 in dark, worst at 1.03. The remedy 3.4 names for an area color
 * that misses the floor is to darken the label variant, and that mechanism does not
 * reach this ground: on a card the variant sits on a 13 percent wash of its own hue, so
 * a small blend keeps it recognizable, while a swipe face is a fixed blue and 44 of the
 * 48 would have to move, a median of 34 percent toward black and five of them by more
 * than half. A color blended past half is no longer the color it identifies, so the face
 * would be claiming an identity it had already lost. 3.4 permits an area accent in four
 * forms and a 10.5sp action label is not one of them; the card being swiped carries the
 * dot and the area's name eight dp away, which is where identity already lives.
 */
@Composable
fun SwipeableRow(
    key: String,
    coordinator: SwipeCoordinator,
    actions: SwipeActions,
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(18.dp),
    /**
     * Actions the caller wants on the same node, appended to the three swipe actions.
     *
     * They are passed in rather than appended by the caller, because a semantics property
     * cannot be read inside its own receiver: `customActions = customActions + extra`
     * compiles and throws `UnsupportedOperationException` the moment an accessibility
     * service queries the tree. One list, built once, in one place.
     */
    extraActions: List<CustomAccessibilityAction> = emptyList(),
    content: @Composable (Modifier) -> Unit,
) {
    val colors = LocalClarityColors.current
    val haptics = LocalClarityHaptics.current
    val motion = clarityMotion()
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()

    val offset = remember { Animatable(0f) }
    var rowWidth by remember { mutableIntStateOf(0) }
    var liveOffset by remember { mutableFloatStateOf(0f) }
    var crossedThreshold by remember { mutableStateOf(false) }

    val actionWidthPx = with(density) { ACTION_WIDTH.toPx() }
    val leftActions = listOfNotNull(actions.onSwap, actions.onDelete)
    val restRight = if (actions.onComplete != null) actionWidthPx else 0f
    val restLeft = -actionWidthPx * leftActions.size

    LaunchedEffect(coordinator.openKey, coordinator.enabled) {
        if ((coordinator.openKey != key || !coordinator.enabled) && offset.value != 0f) {
            offset.animateTo(0f, motion.springStandard())
            liveOffset = 0f
        }
    }

    suspend fun settle(target: Float) {
        if (target == 0f) coordinator.closeIfOpen(key) else coordinator.open(key)
        offset.animateTo(target, motion.springStandard())
        liveOffset = offset.value
    }

    fun commit(action: () -> Unit) {
        scope.launch {
            val direction = if (offset.value >= 0f) 1f else -1f
            // The row slides fully off before the action runs, so the list changes
            // underneath a card that is already gone. Reduced motion makes this
            // instant rather than removing the ordering.
            offset.animateTo(direction * rowWidth, tween(if (motion.reduced) 0 else 180))
            coordinator.closeIfOpen(key)
            action()
            offset.snapTo(0f)
            liveOffset = 0f
        }
    }

    val progress = if (rowWidth == 0) 0f else abs(liveOffset) / rowWidth
    val reveal = (progress / REVEAL_FRACTION).coerceIn(0f, 1f)
    val deepened = progress >= COMMIT_FRACTION

    // **The clip is on the action layer, never here.** It used to sit on this Box,
    // whose only measured child is the card, and the clip rect and the card bounds
    // are the same rounded rectangle, so it removed the whole of the card's shadow.
    // The card was then the only element on the Areas screen with no separation
    // device at all, while the tab bar, the FAB and the chips all kept theirs, which
    // inverted the depth order: the chrome floated and the content it framed did not.
    // Measured on a device capture before the fix, the card's bottom edge stepped
    // from #FFFFFF straight to the canvas in one pixel, against fourteen pixels of
    // decay under the tab bar. design-v3.md 6.1 gives a light mode card a paired
    // shadow and it has to survive being swipeable.
    // **The actions ride down to the card, because that is the node a person lands on.**
    //
    // They used to sit on this outer Box. Merging flows downward only and the card below
    // carries `clickable`, which is itself a merging boundary, so the outer node stayed
    // separate, held custom actions and no text, and TalkBack never stopped on it: the
    // three actions design-v3 10.3.1 makes mandatory were unreachable without the gesture.
    // Adding `mergeDescendants` here made it worse, because a merging node cannot absorb
    // another merging node, so the result was two focus stops with the first one nameless.
    //
    // Handing the modifier to the content is the only arrangement that puts the actions on
    // the node that already has the card's description and its click.
    val rowActions = Modifier.semantics {
        // Swipe is invisible to a screen reader and is only ever an accelerator. These
        // duplicates are what make it safe to have, alongside the long press menu and the
        // detail sheet.
        customActions = buildList {
            actions.onComplete?.let { run -> add(CustomAccessibilityAction(actions.completeLabel) { run(); true }) }
            actions.onSwap?.let { run -> add(CustomAccessibilityAction(actions.swapLabel) { run(); true }) }
            actions.onDelete?.let { run -> add(CustomAccessibilityAction(actions.deleteLabel) { run(); true }) }
            addAll(extraActions)
        }
    }

    Box(
        modifier = modifier
            .onSizeChanged { rowWidth = it.width }
            // **The actions go on the node that already has the text and the click, and
            // getting here took two attempts.**
            //
            // Originally they sat on a plain `Modifier.semantics` on this outer Box.
            // Merging flows downward only and the card below carries `clickable`, which is
            // itself a merging boundary, so this node stayed separate, held custom actions
            // and no text, and TalkBack never stopped on it: the three actions design-v3
            // 10.3.1 makes mandatory were unreachable without the gesture.
            //
            // Adding `mergeDescendants = true` here did not fix it and made it worse. A
            // merging node cannot absorb another merging node, so the result was TWO focus
            // stops, the first of them nameless, and the actions were still on the wrong
            // one. Handing the modifier to the content is what works, and it is the whole
            // mechanism.

    ) {
        // The action layer matches the card exactly rather than sizing itself,
        // so a revealed action is the full height of the row and is clipped by the
        // card's own radius. matchParentSize does not feed back into the parent's
        // measurement, which is what keeps the card the thing that sets the height.
        // **The revealed ground follows the card, and it did not.**
        //
        // Each face used to be a fixed `ACTION_WIDTH` block pinned to its edge, so the
        // moment the card traveled further than 66dp there was nothing behind it: a
        // strip of bare page opened between the face and the card and widened with the
        // drag. The commit threshold is 55 percent of the row, which on this phone is
        // about 180dp, so **every swipe that was far enough to do anything showed the
        // gap**, and it is what the owner saw as the swipe being broken.
        //
        // The action strip is as wide as the card has moved, floored at the faces' own
        // width, so there is no state in which the page shows through. The face nearest
        // the card is the one that grows, which is the behavior a person already knows
        // from every mail app: the action you are pulling toward opens up, and the one
        // beyond it stays put at the edge.
        val revealed = with(density) { abs(liveOffset).toDp() }
        Box(modifier = Modifier.matchParentSize().clip(shape)) {
            if (liveOffset > 0f && actions.onComplete != null) {
                SwipeActionSlot(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .width(maxOf(ACTION_WIDTH, revealed)),
                    background = colors.positiveGreen,
                    baseAlpha = 0.18f,
                    reveal = reveal,
                    deepened = deepened,
                ) {
                    SwipeActionFace(
                        icon = ClarityIcons.check,
                        label = actions.completeLabel,
                        tint = colors.positiveInk,
                        reveal = reveal,
                        onClick = { commit(actions.onComplete) },
                    )
                }
            }

            if (liveOffset < 0f && leftActions.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .width(maxOf(ACTION_WIDTH * leftActions.size, revealed))
                        .fillMaxHeight(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    actions.onSwap?.let { swap ->
                        SwipeActionSlot(
                            // The inner face takes the overflow. With Delete beyond it,
                            // this is the one the thumb is pulling toward.
                            modifier = Modifier.weight(1f),
                            background = colors.actionBlue,
                            baseAlpha = 0.12f,
                            reveal = reveal,
                            deepened = deepened,
                        ) {
                            SwipeActionFace(
                                icon = ClarityIcons.swap,
                                label = actions.swapLabel,
                                tint = colors.actionBlue,
                                reveal = reveal,
                                onClick = { commit(swap) },
                            )
                        }
                    }
                    actions.onDelete?.let { delete ->
                        SwipeActionSlot(
                            // Fixed, unless it is the only one, in which case it grows.
                            modifier = if (actions.onSwap == null) {
                                Modifier.weight(1f)
                            } else {
                                Modifier.width(ACTION_WIDTH)
                            },
                            background = colors.deleteMuted,
                            baseAlpha = 0.13f,
                            reveal = reveal,
                            deepened = deepened,
                        ) {
                            SwipeActionFace(
                                icon = ClarityIcons.deleteSwipe,
                                label = actions.deleteLabel,
                                tint = colors.deleteMuted,
                                reveal = reveal,
                                onClick = { commit(delete) },
                            )
                        }
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .offset { IntOffset(offset.value.roundToInt(), 0) }
                .draggable(
                    enabled = coordinator.enabled,
                    orientation = Orientation.Horizontal,
                    state = rememberDraggableState { delta ->
                        val proposed = offset.value + delta
                        val clamped = proposed.coerceIn(
                            if (leftActions.isNotEmpty()) -rowWidth.toFloat() else 0f,
                            if (actions.onComplete != null) rowWidth.toFloat() else 0f,
                        )
                        scope.launch { offset.snapTo(clamped) }
                        liveOffset = clamped

                        val fraction = if (rowWidth == 0) 0f else abs(clamped) / rowWidth
                        val commits =
                            if (clamped > 0f) actions.onComplete != null else actions.onSwap != null
                        if (commits && fraction >= COMMIT_FRACTION && !crossedThreshold) {
                            crossedThreshold = true
                            haptics.perform(ClarityHapticEvent.SWIPE_THRESHOLD)
                        } else if (fraction < COMMIT_FRACTION) {
                            crossedThreshold = false
                        }
                    },
                    onDragStarted = {
                        crossedThreshold = false
                        coordinator.open(key)
                    },
                    onDragStopped = { velocity ->
                        // A quick flick commits below the distance threshold, which
                        // is what makes it feel responsive rather than ignored.
                        val flung = with(density) { abs(velocity).toDp().value } > FLING_DP_PER_SECOND
                        val fraction = if (rowWidth == 0) 0f else abs(offset.value) / rowWidth
                        val right = offset.value > 0f
                        val commits = fraction >= COMMIT_FRACTION || (flung && fraction >= REVEAL_FRACTION)
                        when {
                            right && actions.onComplete != null && commits -> commit(actions.onComplete)
                            !right && actions.onSwap != null && commits -> commit(actions.onSwap)
                            fraction >= REVEAL_FRACTION -> settle(if (right) restRight else restLeft)
                            else -> settle(0f)
                        }
                    },
                ),
        ) {
            content(rowActions)
        }
    }
}

@Composable
private fun SwipeActionSlot(
    background: Color,
    baseAlpha: Float,
    reveal: Float,
    deepened: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    // Fades in from nothing as the card moves, then deepens by 40 percent past the
    // commit threshold. Never a full bleed alarm color.
    val alpha = baseAlpha * reveal * (if (deepened) 1.4f else 1f)
    // **The face is painted on the card plane, not on the page.** The slot occupies the
    // rectangle the card is sliding out of, so the ground behind a revealed action is
    // content rank, and a swipe reads as the card lifting off its own surface rather
    // than as a hole cut through to the canvas. It is also what makes the faces legible:
    // measured on the refreshed ladder, the three labels read 4.31, 4.36 and 4.30 to one
    // over `canvas`, all under design-v3.md 13's floor, and 5.84, 5.66 and 5.82 over
    // `card`.
    val ground = LocalClarityColors.current.card
    Box(
        // **The width comes from the caller now.** It was fixed here, which is what left
        // a hole between the face and a card that had traveled further than one face.
        modifier = modifier
            .fillMaxHeight()
            .background(ground)
            .background(background.copy(alpha = alpha)),
        contentAlignment = Alignment.Center,
    ) { content() }
}

@Composable
private fun SwipeActionFace(
    icon: Int,
    label: String,
    tint: Color,
    reveal: Float,
    onClick: () -> Unit,
) {
    val type = LocalClarityTypography.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clarityClickable(haptic = null, onClickLabel = label, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        ClarityIcon(
            icon = icon,
            contentDescription = null,
            tint = tint,
            // Scales from 0.8 across the reveal, so it arrives rather than appears.
            modifier = Modifier.size(22.dp).scale(0.8f + 0.2f * reveal),
        )
        // Two lines, centered. `Delete area` names its own scope and does not fit on one
        // line in a 66dp face, and the alternative was a one word label that was wrong
        // about what it deletes.
        Text(
            text = label,
            style = type.sidehead.opticallyCentered(),
            color = tint,
            textAlign = TextAlign.Center,
            maxLines = 2,
        )
    }
}

