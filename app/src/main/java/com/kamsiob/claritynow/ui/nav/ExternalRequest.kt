package com.kamsiob.claritynow.ui.nav

import androidx.compose.runtime.Immutable
import com.kamsiob.claritynow.notifications.FocusIntents
import com.kamsiob.claritynow.notifications.PulseIntents
import com.kamsiob.claritynow.widget.WidgetIntents

/**
 * Where something outside the composition has asked this app to go.
 *
 * Six widgets, three app shortcuts, two notifications and a quick settings tile all
 * reach this app the same way: an `Intent` at `MainActivity`, which is `singleTask`, so
 * half of them arrive at `onCreate` and half at `onNewIntent`. Every one of them is one
 * of the six values below.
 *
 * **There is no seventh value for "the app".** A launcher tap, and the tap on a widget
 * that has nothing to show, send `Intent.ACTION_MAIN`, which names no destination
 * because there is none to name: the app opens where it was. That is the absence of a
 * request rather than a request for nothing, and it is modeled as a null
 * [ExternalRequest.destination].
 */
@Immutable
sealed interface ExternalDestination {

    /**
     * The Focus surface: the chooser, or the running session when there is one.
     *
     * It carries no session id, deliberately, for the reason `MainActivity` has always
     * given: there is one running session per device and the surface finds it from the
     * log, so an id taken from a notification or from a widget snapshot could only ever
     * be a second, staler opinion about which session a person is in.
     */
    data object FocusSurface : ExternalDestination

    /** The Pulse. The daily reminder, and the `Today's Pulse` shortcut. */
    data object Pulse : ExternalDestination

    /** The Momentum tab. The `Rhythm` widget. */
    data object Momentum : ExternalDestination

    /** Capture, straight into the unfiled inbox. `Quick Capture`, and the shortcut. */
    data object UnfiledCapture : ExternalDestination

    /** One area's detail sheet. `Next Up`, and a row of `All Areas`. */
    data class Area(val areaId: String) : ExternalDestination

    /**
     * A focus session on one named item. `First Step`.
     *
     * **The area is not here, and the intent's [WidgetIntents.EXTRA_AREA_ID] is
     * deliberately not read.** An item belongs to exactly one area and the log says
     * which, so an area id copied out of a widget snapshot is a second opinion about a
     * fact that has one answer. `FocusViewModel` resolves it from the projection at the
     * moment it starts the session, which is also the moment the answer has to be true.
     */
    data class FocusOnItem(val itemId: String) : ExternalDestination
}

/**
 * How many times something outside the composition has asked, and what it asked for
 * last.
 *
 * **A serial rather than a flag, and read rather than consumed.** This is the pattern
 * `MainActivity` has used for the Focus notification since phase 4, generalized to the
 * other five actions rather than joined by a second one. A flag has to be cleared by
 * whoever acted on it, and the frame between acting and clearing is the frame that
 * re-opens a surface the person has just left. A number that only goes up says "this
 * happened again" with nothing to reset, so two taps on the same widget are two
 * requests and a recomposition is none.
 *
 * A serial of zero and a null destination is the resting state: nothing has been asked.
 */
@Immutable
data class ExternalRequest(
    val serial: Long = 0L,
    val destination: ExternalDestination? = null,
) {

    /** One more request, for [destination]. */
    fun asking(destination: ExternalDestination): ExternalRequest =
        ExternalRequest(serial = serial + 1, destination = destination)
}

/**
 * The whole routing table, as a function of the three things an intent carries.
 *
 * **It takes an action and two extras rather than an `Intent`, and that is the point of
 * it.** This module has no Robolectric and `unitTests.isReturnDefaultValues` makes a
 * constructed `Intent` answer null to everything, so a routing decision written against
 * `Intent` cannot be exercised by a single test on this machine. Phase 12 shipped five
 * actions with a contract and no receiver and nothing went red, which is the defect this
 * shape exists to make impossible: `ExternalRouteTest` enumerates every action this app
 * can send and fails when one of them routes nowhere.
 *
 * **Every branch names the constant rather than the string**, so the three intent files
 * stay the one place an action is spelled and a rename is a red build rather than a
 * widget that quietly opens the app at whatever tab it was left on. Their predicates,
 * `WidgetIntents.opensArea` and its five counterparts, remain the readable statement of
 * what each action means for anything holding an `Intent`; this is the same six facts
 * arranged so a test can reach them.
 *
 * Two malformed cases, answered differently on purpose:
 *
 * - **[WidgetIntents.ACTION_OPEN_AREA] with no area id routes nowhere.** "An area
 *   detail sheet with no area" is not a destination, and the Areas list is where the app
 *   already is
 * - **[WidgetIntents.ACTION_START_FOCUS] with no item id opens the Focus surface**,
 *   because the chooser is a real destination and it is the one the `First Step` widget
 *   already sends for itself when the area it is pinned to has nothing active. It is the
 *   same degradation the Focus chip performs in `MASTER_BUILD_PROMPT.md` section 10
 */
fun destinationFor(
    action: String?,
    areaId: String?,
    itemId: String?,
): ExternalDestination? = when (action) {
    FocusIntents.ACTION_OPEN_FOCUS -> ExternalDestination.FocusSurface
    PulseIntents.ACTION_OPEN_PULSE -> ExternalDestination.Pulse
    WidgetIntents.ACTION_OPEN_MOMENTUM -> ExternalDestination.Momentum
    WidgetIntents.ACTION_CAPTURE_UNFILED -> ExternalDestination.UnfiledCapture
    WidgetIntents.ACTION_OPEN_AREA -> areaId?.let { ExternalDestination.Area(it) }
    WidgetIntents.ACTION_START_FOCUS ->
        itemId?.let { ExternalDestination.FocusOnItem(it) } ?: ExternalDestination.FocusSurface
    else -> null
}
