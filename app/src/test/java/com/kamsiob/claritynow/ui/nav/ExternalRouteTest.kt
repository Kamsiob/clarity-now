package com.kamsiob.claritynow.ui.nav

import com.kamsiob.claritynow.notifications.FocusIntents
import com.kamsiob.claritynow.notifications.PulseIntents
import com.kamsiob.claritynow.shortcuts.ClarityShortcuts
import com.kamsiob.claritynow.widget.WidgetIntents
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Every way into this app, and where each one lands. MASTER_BUILD_PROMPT 13.3, 13.4 and
 * 13.5.
 *
 * **This file exists because the defect it checks for shipped.** Phase 12 built six
 * widgets, three app shortcuts and a quick settings tile, and phase 6 built the Pulse
 * reminder before them. Five of those actions had a contract, a constant and a
 * `PendingIntent`, and no receiver, so a tap opened the app at whatever tab it was left
 * on. Nothing was red. A widget that opens the wrong screen is degraded rather than
 * broken, which is exactly the kind of defect that survives a screenshot and a device
 * pass, and there was no test that could have failed because the decision was written
 * against `Intent`, which this module cannot build: there is no Robolectric here and
 * `unitTests.isReturnDefaultValues` makes a constructed one answer null to everything.
 *
 * [destinationFor] takes the three values an intent carries instead, so the routing table
 * is a value a JVM test can call. Every constant below is a `const val`, so nothing here
 * loads an Android class, which is the same arrangement `ShortcutContractTest` relies on.
 */
class ExternalRouteTest {

    /**
     * Every action that can arrive at `MainActivity`.
     *
     * The three action constants inside `FocusIntents` that are not here, `Add 10 min`,
     * `End` and the dismissal, are deliberately absent: they are broadcasts to
     * `FocusActionReceiver` and they never reach an Activity. Adding them to this list
     * would be asking the app to open a screen when somebody taps `End`.
     */
    private val everyActivityAction = listOf(
        FocusIntents.ACTION_OPEN_FOCUS,
        PulseIntents.ACTION_OPEN_PULSE,
        WidgetIntents.ACTION_OPEN_MOMENTUM,
        WidgetIntents.ACTION_CAPTURE_UNFILED,
        WidgetIntents.ACTION_OPEN_AREA,
        WidgetIntents.ACTION_START_FOCUS,
    )

    // ---------------------------------------------------------------------------
    // The gap this file exists for.
    // ---------------------------------------------------------------------------

    /**
     * The one assertion that would have caught phase 12's blocking gap and phase 6's.
     * Every action, routed. A new action added to any of the three intent files without
     * a branch in the table fails here rather than on somebody's home screen.
     */
    @Test
    fun `every action this app can send reaches a destination`() {
        everyActivityAction.forEach { action ->
            assertNotNull(
                "$action routes nowhere, so a tap on it opens the app at whatever tab " +
                    "it was left on",
                destinationFor(action, areaId = AREA, itemId = ITEM),
            )
        }
    }

    /**
     * The same assertion again, against the source rather than against a list written by
     * hand, so that an action **added** later is caught too.
     *
     * The list above is a copy, and a copy is the thing that goes stale without anybody
     * noticing, which is the argument `ClarityShortcuts` makes about the shortcut
     * resource and `EraseContractTest` makes about its own source scan. This reads the
     * three intent files, takes every action constant declared in them, and requires each
     * one to be either routed or named below as a broadcast. A seventh action added to
     * any of them fails here on the commit that adds it.
     */
    @Test
    fun `every action constant declared in the intent files is accounted for`() {
        val declared = INTENT_FILES.flatMap { path ->
            val file = File(path)
            assertTrue(
                "missing $path, and this run is in ${File("").absolutePath}",
                file.isFile,
            )
            ACTION_CONSTANT.findAll(file.readText())
                .map { it.groupValues[1] to it.groupValues[2] }
                .toList()
        }
        assertTrue("no action constants found, so this test proved nothing", declared.size >= 6)

        declared.forEach { (name, action) ->
            if (name in BROADCAST_ONLY) {
                assertNull(
                    "$name is a broadcast to FocusActionReceiver and must not open a " +
                        "screen: tapping End would open the app",
                    destinationFor(action, areaId = AREA, itemId = ITEM),
                )
            } else {
                assertNotNull(
                    "$name routes nowhere. Add a branch to destinationFor, or add it to " +
                        "BROADCAST_ONLY if it is not meant to reach MainActivity",
                    destinationFor(action, areaId = AREA, itemId = ITEM),
                )
            }
        }
    }

    /**
     * The three app shortcuts, which are strings in `res/xml/shortcuts.xml` rather than
     * calls. `ShortcutContractTest` already holds the resource to the constants; this
     * holds the constants to a destination, so the pair of them covers the whole path
     * from a long press on the icon to a screen.
     */
    @Test
    fun `every app shortcut reaches a destination`() {
        ClarityShortcuts.actions.forEach { (id, action) ->
            assertNotNull(
                "the $id shortcut routes nowhere",
                destinationFor(action, areaId = null, itemId = null),
            )
        }
    }

    /** No two actions land in the same place, which is what makes six of them worth six. */
    @Test
    fun `the six actions land in six different places`() {
        val destinations = everyActivityAction.map { destinationFor(it, AREA, ITEM) }
        assertEquals(everyActivityAction.size, destinations.toSet().size)
    }

    // ---------------------------------------------------------------------------
    // Each destination, named.
    // ---------------------------------------------------------------------------

    @Test
    fun `the focus notification and the tile open the focus surface`() {
        assertEquals(
            ExternalDestination.FocusSurface,
            destinationFor(FocusIntents.ACTION_OPEN_FOCUS, areaId = null, itemId = null),
        )
    }

    @Test
    fun `the pulse reminder opens the pulse`() {
        assertEquals(
            ExternalDestination.Pulse,
            destinationFor(PulseIntents.ACTION_OPEN_PULSE, areaId = null, itemId = null),
        )
    }

    @Test
    fun `the rhythm widget opens momentum`() {
        assertEquals(
            ExternalDestination.Momentum,
            destinationFor(WidgetIntents.ACTION_OPEN_MOMENTUM, areaId = null, itemId = null),
        )
    }

    @Test
    fun `quick capture opens the unfiled inbox`() {
        assertEquals(
            ExternalDestination.UnfiledCapture,
            destinationFor(WidgetIntents.ACTION_CAPTURE_UNFILED, areaId = null, itemId = null),
        )
    }

    @Test
    fun `next up opens the area it names`() {
        assertEquals(
            ExternalDestination.Area(AREA),
            destinationFor(WidgetIntents.ACTION_OPEN_AREA, areaId = AREA, itemId = null),
        )
    }

    /**
     * The area id on this intent is carried and ignored. `FocusViewModel` resolves the
     * area from the log, because an item belongs to exactly one area and a copy taken
     * from a widget snapshot can only be a staler opinion about a fact with one answer.
     */
    @Test
    fun `first step starts a session on the item it names and ignores the area`() {
        assertEquals(
            ExternalDestination.FocusOnItem(ITEM),
            destinationFor(WidgetIntents.ACTION_START_FOCUS, areaId = AREA, itemId = ITEM),
        )
        assertEquals(
            ExternalDestination.FocusOnItem(ITEM),
            destinationFor(
                action = WidgetIntents.ACTION_START_FOCUS,
                areaId = "somewhere else",
                itemId = ITEM,
            ),
        )
    }

    // ---------------------------------------------------------------------------
    // The taps that name nothing, and the malformed ones.
    // ---------------------------------------------------------------------------

    /**
     * `WidgetIntents.app` and the launcher both send this. It is the absence of a request
     * rather than a request for nothing: the app opens where it was.
     */
    @Test
    fun `a plain launch asks for nothing`() {
        assertNull(destinationFor("android.intent.action.MAIN", areaId = null, itemId = null))
        assertNull(destinationFor(null, areaId = null, itemId = null))
        assertNull(destinationFor("com.example.something.else", areaId = AREA, itemId = ITEM))
    }

    /**
     * An area detail sheet with no area is not a destination, and the Areas list is where
     * the app already is.
     */
    @Test
    fun `an area intent with no area asks for nothing`() {
        assertNull(destinationFor(WidgetIntents.ACTION_OPEN_AREA, areaId = null, itemId = ITEM))
    }

    /**
     * A session on no item degrades to the chooser rather than to nothing, because the
     * chooser is a real destination and it is the one the `First Step` widget already
     * sends for itself when the area it is pinned to has nothing active.
     */
    @Test
    fun `a start focus intent with no item opens the chooser`() {
        assertEquals(
            ExternalDestination.FocusSurface,
            destinationFor(WidgetIntents.ACTION_START_FOCUS, areaId = AREA, itemId = null),
        )
    }

    // ---------------------------------------------------------------------------
    // The serial. A request is a moment, not a state.
    // ---------------------------------------------------------------------------

    /** Nothing has been asked, which is the state a launcher tap leaves behind. */
    @Test
    fun `the resting state names no destination`() {
        assertEquals(0L, ExternalRequest().serial)
        assertNull(ExternalRequest().destination)
    }

    /**
     * Two taps on the same widget are two requests. This is the whole reason the value
     * carries a number rather than a destination alone: the shell keys its effect on the
     * request, and a second identical tap that produced an equal value would be a tap
     * that did nothing on a warm start.
     */
    @Test
    fun `asking twice for the same thing is two requests`() {
        val first = ExternalRequest().asking(ExternalDestination.Momentum)
        val second = first.asking(ExternalDestination.Momentum)
        assertEquals(1L, first.serial)
        assertEquals(2L, second.serial)
        assertTrue("an equal value would be one request", first != second)
    }

    /** The serial only ever goes up, across any run of destinations. */
    @Test
    fun `the serial only goes up`() {
        var request = ExternalRequest()
        val taps = listOf(
            ExternalDestination.Pulse,
            ExternalDestination.Area(AREA),
            ExternalDestination.Pulse,
            ExternalDestination.FocusOnItem(ITEM),
            ExternalDestination.FocusSurface,
        )
        taps.forEachIndexed { index, destination ->
            val next = request.asking(destination)
            assertTrue(next.serial > request.serial)
            assertEquals((index + 1).toLong(), next.serial)
            assertEquals(destination, next.destination)
            request = next
        }
    }

    private companion object {
        const val AREA = "area-1"
        const val ITEM = "item-1"

        /**
         * The three files that declare an action string. Relative to the app module,
         * which is where unit tests run from, exactly as `ShortcutContractTest` reads
         * the shortcut resource.
         */
        val INTENT_FILES = listOf(
            "src/main/java/com/kamsiob/claritynow/notifications/FocusIntents.kt",
            "src/main/java/com/kamsiob/claritynow/notifications/PulseIntents.kt",
            "src/main/java/com/kamsiob/claritynow/widget/WidgetIntents.kt",
        )

        /**
         * `const val ACTION_SOMETHING: String = "..."`, with or without `internal`, and
         * with the value on the next line, which one of the four in `FocusIntents` is.
         *
         * It deliberately does not match the closing quote of the value, because a raw
         * string that ends in a quote is the one place Kotlin's raw string syntax is
         * ambiguous, and `[^"]+` has already stopped at it.
         */
        val ACTION_CONSTANT = Regex("""const val (ACTION_\w+): String =\s*"([^"]+)""")

        /**
         * The actions that go to `FocusActionReceiver` rather than to an Activity:
         * `Add 10 min`, `End`, and the swipe that dismisses the ongoing notification.
         *
         * They are excluded rather than forgotten, and the exclusion is asserted in the
         * other direction: routing one of them would mean tapping `End` in the shade
         * opened the app, which is the opposite of what that button is for.
         */
        val BROADCAST_ONLY = setOf("ACTION_ADD_TEN", "ACTION_END", "ACTION_DISMISSED")
    }
}
