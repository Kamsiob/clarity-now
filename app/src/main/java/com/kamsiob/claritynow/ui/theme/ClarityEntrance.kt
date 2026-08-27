package com.kamsiob.claritynow.ui.theme

import android.os.SystemClock
import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * Whether a screen composing now is arriving or is already at rest. design-v3.md 8.4,
 * Addendum 01 item 8e.
 *
 * An entrance is a way of saying "this is new". A screen opened twenty times a day is
 * not new, and an entrance that fires every time is not delight: it delays the content
 * by its own duration, every time, for the reader least able to afford the wait. So the
 * entrance fires on the first open of a tab per app session and never again, and every
 * return after that renders already settled, at rest, with no fade and no offset.
 *
 * **An app session is the process lifetime**, recorded in `DECISIONS.md` and stated in
 * design-v3.md 8.4. The alternative, re-arming after some period in the background, was
 * rejected: it invents a threshold nobody asked for and makes one screen behave two ways
 * for a reason the user cannot see.
 *
 * **Calm mode removes the entrance; reduce motion only reduces it.** design-v3.md 16.2
 * says entrances do not fire at all in calm mode, and 8.3 turns every other animation
 * into a 150ms crossfade, which for an entrance means the fade without the rise and
 * without the stagger. Both flags are read in [clarityEntrance], once, rather than at
 * any call site.
 *
 * Item 25, the empty state entrance, is deliberately not routed through here. Its 150ms
 * delay exists to stop a flash during a load that resolves quickly rather than to
 * announce anything, and design-v3.md 8.4 exempts it by name.
 *
 * The default carried by [LocalScreenEntrance] is `false`, so a composable rendered
 * outside a [TabEntrance], in a preview or a test, draws settled. Settled is always
 * safe; an entrance that fires when it should not is the defect this file exists to
 * prevent.
 */
@Immutable
class ScreenEntrance(val playing: Boolean)

val LocalScreenEntrance = compositionLocalOf { ScreenEntrance(false) }

/** design-v3.md 8.2 item 4. The distance an arriving item travels. */
private val ENTRANCE_RISE = 16.dp

/**
 * How long after a tab's first open an item composed for the first time still counts as
 * part of the arrival.
 *
 * 350ms for the fade itself, per 8.2 item 4, plus enough stagger for a screen's worth of
 * rows: design-v3.md 11 puts five area cards on screen comfortably and a tall device
 * holds a few more, so twelve slots at the 50ms stagger is a generous ceiling. Past it, a
 * row scrolled into view is a row being read rather than a screen arriving.
 */
private const val ENTRANCE_WINDOW_MILLIS = 350L + 12L * 50L

/** No session has spent this tab's entrance yet. `SystemClock` never returns zero. */
private const val NO_SESSION = 0L

/**
 * The app session, as a value that can be written into a saved instance state bundle and
 * compared after a restore.
 *
 * `elapsedRealtimeNanos` is read once when this object initializes, which happens once
 * per process. Two launches cannot read the same nanosecond, and a reboot resets the
 * clock but also discards every saved bundle, so the token is unique across every
 * restore that can actually happen.
 */
private object ClaritySession {
    val token: Long = SystemClock.elapsedRealtimeNanos()
}

/**
 * Holds one tab's entrance flag and provides it to everything inside.
 *
 * Placed inside the shell's `SaveableStateProvider` for the tab, which is what puts the
 * flag in the per tab saveable state the shell has held since phase 3 rather than in a
 * global or in DataStore. A global would make the second tab opened think it was the
 * second open of the first; DataStore would spend the entrance permanently at first
 * install and it would never be seen again.
 *
 * **The flag is a session token rather than a boolean**, and that is the whole trick.
 * design-v3.md 8.4 wants a rotation to leave the entrance spent and a process death to
 * re-arm it, and those pull in opposite directions: `remember` loses a rotation,
 * `rememberSaveable` survives a system initiated process death, and neither alone is
 * right. Storing *which* session spent it settles both. A bundle restored into a new
 * process carries a token that matches nothing, so the entrance fires again.
 *
 * Wrapping the tab rather than the screen is what keeps a sheet or a detail view from
 * counting as a first open.
 *
 * Phase 8's Report reveal, 8.2 item 12, is the one entrance that re-arms on a content
 * change as well as on a session change. It will need a key here; it does not have one
 * yet, because nothing whose content changes under an entrance exists to test it
 * against.
 */
@Composable
fun TabEntrance(content: @Composable () -> Unit) {
    var spentBySession by rememberSaveable { mutableStateOf(NO_SESSION) }
    val firstOpen = remember { spentBySession != ClaritySession.token }
    var playing by remember { mutableStateOf(firstOpen) }

    LaunchedEffect(Unit) {
        if (!firstOpen) return@LaunchedEffect
        spentBySession = ClaritySession.token
        delay(ENTRANCE_WINDOW_MILLIS)
        playing = false
    }

    val entrance = remember(playing) { ScreenEntrance(playing) }
    CompositionLocalProvider(LocalScreenEntrance provides entrance) { content() }
}

/**
 * design-v3.md 8.2 item 4. Fades from 0 and rises 16dp over 350ms easeOut, delayed by
 * [index] times the stagger.
 *
 * [index] is the item's position in the arrival order rather than in the data, so a
 * screen with a header and then any number of cards staggers as one sequence.
 *
 * The decision is captured once per item, in a `remember`, and everything below depends
 * on it never changing: an item whose entrance is in flight when the arrival window
 * closes finishes it, and a row scrolled off and back never arrives a second time.
 */
@Composable
fun Modifier.clarityEntrance(index: Int): Modifier {
    val motion = clarityMotion()
    val calm = LocalCalmMode.current
    val playing = LocalScreenEntrance.current.playing

    val plays = remember { playing && !calm }
    if (!plays) return this

    val rise = with(LocalDensity.current) {
        // A crossfade has no travel. design-v3.md 8.3 turns the entrance into one, and
        // leaving the 16dp in would be the slide that rule exists to remove.
        if (motion.reduced) 0f else ENTRANCE_RISE.toPx()
    }
    val progress = remember { Animatable(0f) }
    val delayMillis = (index.coerceAtLeast(0) * motion.staggerMillis).toLong()

    LaunchedEffect(Unit) {
        if (delayMillis > 0L) delay(delayMillis)
        progress.animateTo(1f, motion.easeOut())
    }

    return graphicsLayer {
        alpha = progress.value
        translationY = (1f - progress.value) * rise
    }
}
