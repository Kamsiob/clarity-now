package com.kamsiob.claritynow.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import com.kamsiob.claritynow.ui.theme.ClarityElevation
import com.kamsiob.claritynow.ui.theme.ClarityHapticEvent
import com.kamsiob.claritynow.ui.theme.LocalClarityColors
import com.kamsiob.claritynow.ui.theme.LocalClarityHaptics
import com.kamsiob.claritynow.ui.theme.LocalClarityTypography
import com.kamsiob.claritynow.ui.theme.clarityMotion

/** The five second window a deletion waits in before it becomes an event. */
const val UNDO_WINDOW_MILLIS = 5_000L

/**
 * One pending, undoable action. Nothing is written to the log while this exists,
 * so undo has nothing to compensate for and the history never records a mistake
 * that was taken back.
 */
data class UndoRequest(
    val id: String,
    val message: String,
    val actionLabel: String,
    val onCommit: suspend () -> Unit,
    val onUndo: () -> Unit = {},
)

/**
 * design-v3.md 10.14 and 8.2 item 20. Rises above the tab bar, card colored, one
 * line plus an action, with a thin depleting line showing the window. Undo only,
 * because a snackbar that carries anything else becomes a notification channel.
 */
@Composable
fun UndoSnackbar(
    request: UndoRequest?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalClarityColors.current
    val type = LocalClarityTypography.current
    val haptics = LocalClarityHaptics.current
    val motion = clarityMotion()
    val remaining = remember { Animatable(1f) }
    var undone by remember(request?.id) { mutableStateOf(false) }

    LaunchedEffect(request?.id) {
        val current = request ?: return@LaunchedEffect
        undone = false
        remaining.snapTo(1f)
        remaining.animateTo(0f, tween(UNDO_WINDOW_MILLIS.toInt(), easing = { it }))
        if (!undone) {
            current.onCommit()
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible = request != null,
        enter = slideInVertically(motion.springGentle()) { it } + fadeIn(motion.easeOut()),
        exit = slideOutVertically(motion.easeOut()) { it } + fadeOut(motion.easeOut()),
        modifier = modifier,
    ) {
        val current = request
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 17.dp)
                .clarityShadow(ClarityElevation.card, RoundedCornerShape(12.dp), enabled = !colors.isDark)
                .clip(RoundedCornerShape(12.dp))
                .background(colors.card),
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 13.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = current?.message.orEmpty(),
                        style = type.body,
                        color = colors.inkPrimary,
                        modifier = Modifier.padding(end = 12.dp),
                    )
                    Text(
                        text = current?.actionLabel.orEmpty(),
                        style = type.bodyStrong,
                        color = colors.actionBlue,
                        modifier = Modifier.clarityClickable(haptic = null) {
                            undone = true
                            haptics.perform(ClarityHapticEvent.UNDO)
                            current?.onUndo?.invoke()
                            onDismiss()
                        },
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .layout { measurable, constraints ->
                            val width = (constraints.maxWidth * remaining.value).toInt().coerceAtLeast(0)
                            val placeable = measurable.measure(constraints.copy(minWidth = width, maxWidth = width))
                            layout(constraints.maxWidth, placeable.height) { placeable.place(0, 0) }
                        }
                        .background(colors.actionBlue.copy(alpha = 0.35f)),
                )
            }
        }
    }
}
