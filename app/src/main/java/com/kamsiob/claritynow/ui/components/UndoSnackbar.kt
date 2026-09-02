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
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import com.kamsiob.claritynow.ui.theme.LocalClarityShapes
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import com.kamsiob.claritynow.ui.theme.ClarityElevation
import com.kamsiob.claritynow.ui.theme.ClarityHapticEvent
import com.kamsiob.claritynow.ui.theme.ClaritySpacing
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
    val shapes = LocalClarityShapes.current
    val haptics = LocalClarityHaptics.current
    val motion = clarityMotion()
    val remaining = remember { Animatable(1f) }
    var undone by remember(request?.id) { mutableStateOf(false) }

    // The depleting line runs at its full five seconds whatever the motion setting is.
    // design-v3.md 16.2 names it: it is the only readout of a window that is closing,
    // which makes it information rather than decoration, and calm mode removes motion
    // and never information.
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

    // design-v3.md 8.3 replaces every animation with a 150ms crossfade, and a slide
    // driven by a 150ms spec is still a slide. The translation is dropped rather than
    // shortened, which is the same call the tab crossfade in ClarityShell makes. Calm
    // mode reaches this through the one global flag, 8.5, and nothing here reads it.
    AnimatedVisibility(
        visible = request != null,
        enter = if (motion.reduced) {
            fadeIn(motion.easeOut())
        } else {
            slideInVertically(motion.springGentle()) { it } + fadeIn(motion.easeOut())
        },
        exit = if (motion.reduced) {
            fadeOut(motion.easeOut())
        } else {
            slideOutVertically(motion.easeOut()) { it } + fadeOut(motion.easeOut())
        },
        modifier = modifier,
    ) {
        val current = request
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 17.dp)
                .clarityShadow(ClarityElevation.card, RoundedCornerShape(12.dp), enabled = !colors.isDark)
                .clip(RoundedCornerShape(12.dp))
                // `card` rather than `raise`, and it is the one piece of floating
                // furniture in the app that keeps the content value. For five seconds
                // this is the top plane on the screen, and what it holds is the only
                // way back from a deletion. Chrome recedes; this cannot afford to.
                .background(colors.card)
                // Nothing told a screen reader the snackbar had appeared, on a control
                // with a five second life. Assertive, because polite would be queued
                // behind whatever is speaking and the window would close first.
                .semantics { liveRegion = LiveRegionMode.Assertive },
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(
                        horizontal = 16.dp,
                        vertical = ClaritySpacing.scaled(13.dp,
                    )),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = current?.message.orEmpty(),
                        style = type.body,
                        color = colors.inkPrimary,
                        modifier = Modifier.padding(end = 12.dp),
                    )
                    // **The one way back from a deletion, and it was a bare word.**
                    // No role, no click label, and a `bodyStrong` line box is about 24dp
                    // against design-v3 13's 48dp floor, on a control that disappears
                    // after five seconds. The box gives it the target; the role and the
                    // label give a screen reader something to announce and act on.
                    Box(
                        modifier = Modifier
                            .sizeIn(
                                minWidth = ClaritySpacing.minTouchTarget,
                                minHeight = ClaritySpacing.minTouchTarget,
                            )
                            .clip(shapes.pill)
                            .clarityClickable(
                                haptic = null,
                                role = Role.Button,
                                onClickLabel = current?.actionLabel.orEmpty(),
                            ) {
                                undone = true
                                haptics.perform(ClarityHapticEvent.UNDO)
                                current?.onUndo?.invoke()
                            }
                            .padding(horizontal = ClaritySpacing.snug),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = current?.actionLabel.orEmpty(),
                            style = type.bodyStrong,
                            color = colors.actionBlue,
                        )
                    }
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
