package com.kamsiob.claritynow.ui.components

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import com.kamsiob.claritynow.ui.theme.ClarityHapticEvent
import com.kamsiob.claritynow.ui.theme.LocalClarityHaptics

/**
 * Long press to pick up, drag to reorder, release to drop.
 *
 * The order changes on screen as the finger moves, and exactly one event is
 * written on release. Writing an event per crossed neighbor would fill the Trail
 * with a record of a gesture rather than a record of a decision.
 */
class ReorderState internal constructor(
    val listState: LazyListState,
    private val onCommit: (key: String, toIndex: Int) -> Unit,
) {
    var draggingKey by mutableStateOf<String?>(null)
        private set
    var dragOffset by mutableFloatStateOf(0f)
        private set

    private var draggingIndex by mutableIntStateOf(-1)
    private var startIndex by mutableIntStateOf(-1)

    /** The live order while a drag is in flight, or null when nothing is moving. */
    var previewOrder by mutableStateOf<List<String>?>(null)
        private set

    val isDragging: Boolean get() = draggingKey != null

    fun start(key: String, index: Int, keys: List<String>) {
        draggingKey = key
        startIndex = index
        draggingIndex = index
        dragOffset = 0f
        previewOrder = keys
    }

    fun drag(delta: Float, itemHeight: Float) {
        if (draggingKey == null || itemHeight <= 0f) return
        dragOffset += delta
        val order = previewOrder ?: return
        while (dragOffset > itemHeight / 2f && draggingIndex < order.size - 1) {
            previewOrder = order.toMutableList().apply { add(draggingIndex + 1, removeAt(draggingIndex)) }
            draggingIndex += 1
            dragOffset -= itemHeight
        }
        while (dragOffset < -itemHeight / 2f && draggingIndex > 0) {
            previewOrder = order.toMutableList().apply { add(draggingIndex - 1, removeAt(draggingIndex)) }
            draggingIndex -= 1
            dragOffset += itemHeight
        }
    }

    fun drop() {
        val key = draggingKey
        val target = draggingIndex
        draggingKey = null
        dragOffset = 0f
        previewOrder = null
        if (key != null && target >= 0 && target != startIndex) onCommit(key, target)
        startIndex = -1
        draggingIndex = -1
    }

    fun cancel() {
        draggingKey = null
        dragOffset = 0f
        previewOrder = null
        startIndex = -1
        draggingIndex = -1
    }
}

@Composable
fun rememberReorderState(
    listState: LazyListState,
    onCommit: (key: String, toIndex: Int) -> Unit,
): ReorderState = remember(listState) { ReorderState(listState, onCommit) }

/**
 * Attaches the pick up and drag gesture to one row.
 *
 * The long press haptic is the pick up thud and the release is the lighter put
 * down tick, so the gesture has a beginning and an end you can feel.
 */
@Composable
fun Modifier.reorderableItem(
    state: ReorderState,
    key: String,
    index: Int,
    keys: List<String>,
    itemHeightPx: Float,
    enabled: Boolean = true,
    onLongPressWithoutDrag: () -> Unit = {},
): Modifier {
    val haptics = LocalClarityHaptics.current
    return if (!enabled) {
        this
    } else {
        this.pointerInput(key, keys, itemHeightPx, enabled) {
            var moved = 0f
            detectDragGesturesAfterLongPress(
                onDragStart = {
                    haptics.perform(ClarityHapticEvent.PICK_UP)
                    moved = 0f
                    state.start(key, index, keys)
                },
                onDrag = { change, dragAmount ->
                    change.consume()
                    moved += kotlin.math.abs(dragAmount.y)
                    state.drag(dragAmount.y, itemHeightPx)
                },
                onDragEnd = {
                    // A long press that never traveled is a request for the menu,
                    // not a reorder. Both gestures start the same way, so the
                    // distinction has to be made on release.
                    if (moved < LONG_PRESS_TRAVEL_SLOP) {
                        state.cancel()
                        onLongPressWithoutDrag()
                    } else {
                        haptics.perform(ClarityHapticEvent.PUT_DOWN)
                        state.drop()
                    }
                },
                onDragCancel = { state.cancel() },
            )
        }
    }
}

/** Travel below this on a long press means the finger never really moved. */
private const val LONG_PRESS_TRAVEL_SLOP = 12f
