package com.kamsiob.claritynow.shortcuts

import com.kamsiob.claritynow.notifications.FocusIntents
import com.kamsiob.claritynow.notifications.PulseIntents
import com.kamsiob.claritynow.widget.WidgetIntents

/**
 * The three app shortcuts, as a contract. MASTER_BUILD_PROMPT 13.5, design-v3.md 12.4,
 * issue #40.
 *
 * ## There is no code behind the shortcuts, and that is the feature
 *
 * They are declared in `res/xml/shortcuts.xml` and published by the system from the
 * manifest, so nothing in this app creates them, updates them, ranks them or reports
 * that one was used. **Static, not dynamic**: 13.5 rules out a list that reorders
 * itself around what somebody did most, because that is a measurement of the person,
 * and the app that never takes it is the app that cannot show it back to them.
 *
 * `ShortcutManagerCompat.reportShortcutUsed` is deliberately never called for the same
 * reason. It exists to feed the launcher's ranking, this app has nothing to rank, and
 * a usage report is a usage record.
 *
 * ## Then what is this file for
 *
 * An action string written into a resource file is a copy of a Kotlin constant, and a
 * copy is the thing that goes stale without anybody noticing: the shortcut keeps
 * launching, it just lands nowhere in particular. This object is the one place the
 * pairing is stated, and `ShortcutContractTest` reads the XML and asserts it still
 * matches. A rename on either side is then a red build rather than a shortcut that
 * quietly opens the app at whatever tab it was left on.
 *
 * Every value below is a compile time constant, so nothing here loads an Android class
 * and the test that reads them runs on the JVM like the rest of the suite.
 */
object ClarityShortcuts {

    /**
     * Capture, straight into the unfiled inbox. MASTER_BUILD_PROMPT 14b.1.
     *
     * The same action the Quick Capture widget sends, so there is one capture path
     * from outside the app rather than two that could drift apart.
     */
    const val ID_QUICK_CAPTURE: String = "quick_capture"

    /**
     * The Focus surface: the chooser, or the running session when there is one.
     *
     * **It asks for the surface and never for a session.** The reasoning is in
     * `res/xml/shortcuts.xml`, and it is the same answer the First Step widget gives
     * to the same question.
     */
    const val ID_START_FOCUS: String = "start_focus"

    /**
     * The Pulse surface, in whatever state today is in, including the ambient state of
     * a day the engine stayed silent on. Generating is the lifecycle's job and happens
     * on foreground, MASTER_BUILD_PROMPT 11.3, so opening this can never cause one.
     */
    const val ID_TODAYS_PULSE: String = "todays_pulse"

    /** The Activity every shortcut targets, which is the only one this app has. */
    const val TARGET_CLASS: String = "com.kamsiob.claritynow.MainActivity"

    /**
     * The package the shortcut intents name, as it appears in the resource.
     *
     * It is a string resource rather than a literal because the debug variant carries
     * `.debug` and `${applicationId}` does not reach a resource file. `build.gradle.kts`
     * generates it per build type.
     */
    const val TARGET_PACKAGE_REFERENCE: String = "@string/clarity_application_id"

    /**
     * Every shortcut, and the action it sends, in declaration order.
     *
     * **The order is the rank.** The system ranks manifest shortcuts by the order they
     * appear in the resource, and which end of its list a launcher puts rank zero at is
     * the launcher's business. Capture is first because it is the one with a cost to
     * being slow: 14b.1 exists because every step between a thought and the record is
     * somewhere the thought is lost.
     */
    val actions: List<Pair<String, String>> = listOf(
        ID_QUICK_CAPTURE to WidgetIntents.ACTION_CAPTURE_UNFILED,
        ID_START_FOCUS to FocusIntents.ACTION_OPEN_FOCUS,
        ID_TODAYS_PULSE to PulseIntents.ACTION_OPEN_PULSE,
    )
}
