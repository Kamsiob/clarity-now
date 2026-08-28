package com.kamsiob.claritynow.ui.focus

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kamsiob.claritynow.data.repo.FocusCountdown
import com.kamsiob.claritynow.di.ClarityViewModelFactory
import com.kamsiob.claritynow.notifications.NotificationPermissionOnFocusStart
import com.kamsiob.claritynow.ui.theme.ContemplativeTheme
import com.kamsiob.claritynow.ui.theme.LocalCalmMode
import com.kamsiob.claritynow.ui.theme.clarityMotion

/**
 * The whole Focus surface: the chooser, a running session and the completion state.
 *
 * One entry point rather than three destinations, because all three are the same room
 * and which one is showing is a fact about the log rather than about navigation. A
 * caller shows this and is told when to stop showing it.
 *
 * **[onExit] must never end a session.** design-v3.md 10.15: back navigates away and
 * leaves the session running, and it does not prompt or warn. Ending a session is a
 * deliberate act with a button and never a side effect of navigating. A caller that
 * stops showing this while a session runs is doing the right thing; the ongoing
 * notification is the way back in and the Areas card carries the live countdown while
 * this is gone.
 *
 * [onExit] has one call site, and it fires only once the surface has reached
 * [FocusPhase.Dismissed]: back, `Done`, `Mark item complete`, and a session ended under
 * a minute that is discarded silently all arrive there, and each of them writes what it
 * had to write before the phase moves. A screen that navigated away in a click handler
 * would take the ViewModel scope with it and could cancel that write halfway.
 *
 * **How a caller decides to show this.** Watch `ClarityRepository.runningFocusSession`
 * and show this surface while it is not null, and when the Focus chip is tapped. Do not
 * call `ClarityRepository.restoreFocus` from outside: this surface calls it on every
 * entry, and a caller that got in first would consume the completion of a session that
 * ran out while the app was away, leaving the person on the chooser instead of on the
 * completion screen. A session in that state is still `RUNNING` in the log until
 * something resolves it, so watching the running session is enough to know to come
 * here.
 *
 * **The Contemplative theme is entered here and is a theme rather than a branch**,
 * design-v3.md section 2, so the theme setting cannot invert it. Calm mode carries
 * through from whatever the app resolved: passing the already resolved flag back into
 * `resolveCalmMode` returns the same answer, so this surface is calm exactly when the
 * rest of the app is and never decides on its own.
 *
 * **[startOn] is the `First Step` widget, and it arrives here rather than acting on its
 * own.** `MASTER_BUILD_PROMPT.md` 13.3: that widget's tap starts a session on the item
 * it is showing. It is delivered as a value into this surface for two reasons that are
 * really one. The write path in this app is `ClarityRepository`, so a widget that
 * appended `FOCUS_STARTED` from a broadcast receiver would put somebody in a running
 * session with nothing on screen saying so; and this surface calls `restoreFocus` on
 * every entry, so a start that ran before that call would be refused by an unloaded
 * repository on a cold start and accepted on a warm one. [FocusViewModel.startOnItem]
 * waits for the entry read for exactly that reason.
 */
@Composable
fun FocusRoute(
    onExit: () -> Unit,
    startOn: FocusStart?,
    modifier: Modifier = Modifier,
    viewModel: FocusViewModel = viewModel(factory = ClarityViewModelFactory),
) {
    val phase by viewModel.phase.collectAsStateWithLifecycle()
    val transitionWarningEnabled by viewModel.transitionWarningEnabled.collectAsStateWithLifecycle()

    // Held as a State and read only inside the dial and inside two click handlers, so
    // that a value arriving once a second reaches the numeral and the arc and nothing
    // else. design-v3.md 8.2 item 7.
    val countdownState = viewModel.countdown.collectAsStateWithLifecycle()
    val countdown = remember(countdownState) { { countdownState.value } }

    LaunchedEffect(phase) { if (phase is FocusPhase.Dismissed) onExit() }

    // Keyed on the whole request rather than on `Unit`, so that a second tap on the
    // `First Step` widget while this surface is already showing is a second request. The
    // serial is what makes two taps on the same item two values; the ViewModel is what
    // makes the second one harmless, since the repository refuses a session while one is
    // running rather than starting a second.
    LaunchedEffect(startOn) {
        if (startOn != null) viewModel.startOnItem(startOn.itemId)
    }

    // Back, from every phase. It leaves the surface and writes nothing.
    BackHandler { viewModel.leave() }

    // MASTER_BUILD_PROMPT 13.4: contextually, the first time a session starts, and
    // never at launch. The notifications package owns the rule and derives the moment
    // from the countdown, so nothing on this surface has to remember to ask and a
    // session restored after a process death cannot be mistaken for a started one.
    FocusNotificationPermission(countdown)

    ContemplativeTheme(calmMode = LocalCalmMode.current) {
        val motion = clarityMotion()

        // design-v3.md 8.2 item 6 and section 2: entering a Contemplative surface feels
        // like the room dimming, so the dark fades in and the content scales from 0.97
        // over 350ms on the easeSlow curve. In calm mode it is a crossfade with no
        // scale, per 16.6. A transition rather than an entrance, so 8.4's once per
        // session rule leaves it alone and it runs every time.
        val arriving = remember { Animatable(0f) }
        LaunchedEffect(Unit) {
            arriving.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = if (motion.reduced) 150 else 350,
                    easing = EaseInOutCubic,
                ),
            )
        }

        Box(
            modifier = modifier
                .fillMaxSize()
                .graphicsLayer {
                    val arrived = arriving.value
                    alpha = arrived
                    val scaled = if (motion.reduced) 1f else DIM_FROM + (1f - DIM_FROM) * arrived
                    scaleX = scaled
                    scaleY = scaled
                },
        ) {
            FocusBackdrop()

            when (val current = phase) {
                FocusPhase.Loading, FocusPhase.Dismissed -> Unit

                is FocusPhase.Choosing -> FocusChooserScreen(
                    options = current.options,
                    durationMinutes = current.durationMinutes,
                    onSelect = viewModel::start,
                )

                is FocusPhase.Running -> FocusSessionScreen(
                    session = current.session,
                    countdown = countdown,
                    transitionWarningEnabled = transitionWarningEnabled,
                    onEnd = { elapsed -> viewModel.endSession(current.session.sessionId, elapsed) },
                    onExtend = { viewModel.addTenMinutes(current.session.sessionId) },
                )

                is FocusPhase.Complete -> FocusCompleteScreen(
                    completion = current.completion,
                    onMarkItemComplete = { viewModel.markItemComplete(current.completion.itemId) },
                    onDone = viewModel::leave,
                )
            }
        }
    }
}

/**
 * The one place this surface reads the tick outside the dial, and it is one line deep
 * on purpose.
 *
 * `NotificationPermissionOnFocusStart` in the notifications package takes the countdown
 * by value and derives the one moment MASTER_BUILD_PROMPT 13.4 allows a request from
 * it. Reading the tick up in [FocusRoute] to hand it over would recompose the whole
 * surface once a second, so the read is scoped to this wrapper, which draws nothing.
 */
@Composable
private fun FocusNotificationPermission(countdown: () -> FocusCountdown?) {
    NotificationPermissionOnFocusStart(countdown())
}

/**
 * A session the `First Step` widget has asked for, on the item it was showing.
 *
 * [serial] is the shell's request counter and it is here so that two taps are two
 * values. Without it, tapping the same widget twice would be one request that the
 * surface had already acted on, and the second tap would do nothing at all on a warm
 * start while doing the right thing on a cold one.
 */
@Immutable
data class FocusStart(val serial: Long, val itemId: String)

/** design-v3.md 8.2 item 6. What the incoming surface scales from. */
private const val DIM_FROM = 0.97f
