package com.kamsiob.claritynow.ui.components

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
import com.kamsiob.claritynow.ui.theme.calmAccent
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

/** design-v3.md 10.3.1. The check and label on the Complete action. */
private val CompleteInk = Color(0xFF15803D)

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
 */
@Composable
fun SwipeableRow(
    key: String,
    coordinator: SwipeCoordinator,
    actions: SwipeActions,
    accent: Color,
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(18.dp),
    content: @Composable () -> Unit,
) {
    val colors = LocalClarityColors.current
    val haptics = LocalClarityHaptics.current
    val motion = clarityMotion()
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()

    // design-v3.md 16.2. The Swap action's face is tinted with the area's accent, and
    // the accent is not one of the two uses 16.2 excludes by name, so it takes the
    // transform like every other atmospheric use of a color. The three action
    // backgrounds behind it do not: positiveGreen, actionBlue and deleteMuted are each
    // scoped to one job in 3.1, and a desaturated action color is a less legible one.
    val faceAccent = calmAccent(accent)

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
    Box(
        modifier = modifier
            .onSizeChanged { rowWidth = it.width }
            .semantics {
                // Swipe is invisible to a screen reader and is only ever an
                // accelerator. These duplicates are what make it safe to have,
                // alongside the long press menu and the detail sheet.
                customActions = buildList {
                    actions.onComplete?.let { run -> add(CustomAccessibilityAction(actions.completeLabel) { run(); true }) }
                    actions.onSwap?.let { run -> add(CustomAccessibilityAction(actions.swapLabel) { run(); true }) }
                    actions.onDelete?.let { run -> add(CustomAccessibilityAction(actions.deleteLabel) { run(); true }) }
                }
            },
    ) {
        // The action layer matches the card exactly rather than sizing itself,
        // so a revealed action is the full height of the row and is clipped by the
        // card's own radius. matchParentSize does not feed back into the parent's
        // measurement, which is what keeps the card the thing that sets the height.
        Box(modifier = Modifier.matchParentSize().clip(shape)) {
            if (liveOffset > 0f && actions.onComplete != null) {
                SwipeActionSlot(
                    modifier = Modifier.align(Alignment.CenterStart),
                    background = colors.positiveGreen,
                    baseAlpha = 0.18f,
                    reveal = reveal,
                    deepened = deepened,
                ) {
                    SwipeActionFace(
                        icon = ClarityIcons.check,
                        label = actions.completeLabel,
                        tint = CompleteInk,
                        reveal = reveal,
                        onClick = { commit(actions.onComplete) },
                    )
                }
            }

            if (liveOffset < 0f && leftActions.isNotEmpty()) {
                Row(
                    modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    actions.onSwap?.let { swap ->
                        SwipeActionSlot(
                            background = colors.actionBlue,
                            baseAlpha = 0.12f,
                            reveal = reveal,
                            deepened = deepened,
                        ) {
                            SwipeActionFace(
                                icon = ClarityIcons.swap,
                                label = actions.swapLabel,
                                tint = faceAccent,
                                reveal = reveal,
                                onClick = { commit(swap) },
                            )
                        }
                    }
                    actions.onDelete?.let { delete ->
                        SwipeActionSlot(
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
            content()
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
    Box(
        modifier = modifier
            .width(ACTION_WIDTH)
            .fillMaxHeight()
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
        Text(text = label, style = type.swipeLabel.opticallyCentered(), color = tint)
    }
}

