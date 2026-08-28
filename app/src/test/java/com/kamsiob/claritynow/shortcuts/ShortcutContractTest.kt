package com.kamsiob.claritynow.shortcuts

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

/**
 * The three app shortcuts, checked by reading the resource. MASTER_BUILD_PROMPT 13.5,
 * design-v3.md 12.4, issue #40.
 *
 * **A resource scan for the same reason `EraseContractTest` is a source scan: the thing
 * that has to be true cannot be reached from a unit test.** A shortcut is published by
 * the system from a manifest, so nothing in this app can be asked whether one works.
 * What can be checked is that the copy of each action string in the resource still
 * equals the constant it was copied from, and that every intent is still explicit.
 *
 * The failure this guards against is quiet in the way that matters. Rename
 * `ACTION_OPEN_PULSE` and the `Today's Pulse` shortcut keeps working: it launches the
 * app, at whatever tab it was last on, with no error anywhere and nothing on screen
 * saying a deep link was missed. Nobody finds that by looking at the app, because the
 * app looks fine.
 */
class ShortcutContractTest {

    private val shortcuts = File("src/main/res/xml/shortcuts.xml")
    private val manifest = File("src/main/AndroidManifest.xml")

    @Test
    fun `both files are where this test looks`() {
        assertTrue(
            "unit tests are expected to run from the app module directory, and this run " +
                "is in ${File("").absolutePath}",
            File("build.gradle.kts").isFile,
        )
        assertTrue("missing ${shortcuts.path}", shortcuts.isFile)
        assertTrue("missing ${manifest.path}", manifest.isFile)
    }

    @Test
    fun `the resource declares the three shortcuts, in order`() {
        assertEquals(
            "MASTER_BUILD_PROMPT 13.5 requires three static shortcuts and no more, in " +
                "the order ClarityShortcuts states, which is the order the system ranks " +
                "them in",
            ClarityShortcuts.actions.map { it.first },
            declared().map { it.getAttribute("android:shortcutId") },
        )
    }

    @Test
    fun `every shortcut sends the action the app already routes`() {
        val expected = ClarityShortcuts.actions.toMap()
        declared().forEach { shortcut ->
            val id = shortcut.getAttribute("android:shortcutId")
            assertEquals(
                "shortcut $id sends an action nothing in this app routes. The three " +
                    "constants live in FocusIntents, PulseIntents and WidgetIntents, and " +
                    "a shortcut whose action does not match one of them opens the app at " +
                    "whatever tab it was left on, with nothing anywhere saying so",
                expected[id],
                intentOf(shortcut).getAttribute("android:action"),
            )
        }
    }

    @Test
    fun `every shortcut intent is explicit`() {
        declared().forEach { shortcut ->
            val id = shortcut.getAttribute("android:shortcutId")
            val intent = intentOf(shortcut)
            assertEquals(
                "shortcut $id must name this app's own activity, so that nothing outside " +
                    "the app can be reached and no filter has to be opened on MainActivity",
                ClarityShortcuts.TARGET_CLASS,
                intent.getAttribute("android:targetClass"),
            )
            assertEquals(
                "shortcut $id must take the package from the generated resource. A " +
                    "literal names the release package, and the debug variant carries " +
                    "the .debug suffix, so a literal is three dead shortcuts on every " +
                    "build a device check runs on",
                ClarityShortcuts.TARGET_PACKAGE_REFERENCE,
                intent.getAttribute("android:targetPackage"),
            )
        }
    }

    @Test
    fun `every shortcut carries both labels and an icon`() {
        declared().forEach { shortcut ->
            val id = shortcut.getAttribute("android:shortcutId")
            listOf(
                "android:shortcutShortLabel",
                "android:shortcutLongLabel",
                "android:icon",
            ).forEach { attribute ->
                assertTrue(
                    "shortcut $id is missing $attribute, and a launcher drops a shortcut " +
                        "with no short label rather than showing it unlabeled",
                    shortcut.getAttribute(attribute).startsWith("@"),
                )
            }
        }
    }

    @Test
    fun `the launcher activity points at the resource`() {
        val text = manifest.readText()
        assertTrue(
            "the shortcuts meta-data has to sit on the activity carrying MAIN and " +
                "LAUNCHER, or the platform never reads the resource at all",
            text.contains("android:name=\"android.app.shortcuts\"") &&
                text.contains("android:resource=\"@xml/shortcuts\""),
        )
    }

    @Test
    fun `nothing publishes a shortcut at runtime`() {
        // Call syntax and an import rather than bare names, so that a file explaining
        // why it does not do one of these is not a file that fails this.
        val banned = listOf(
            "import androidx.core.content.pm.ShortcutManagerCompat",
            "pushDynamicShortcut(",
            "setDynamicShortcuts(",
            "addDynamicShortcuts(",
            "reportShortcutUsed(",
        )
        val offenders = mutableListOf<String>()
        File("src/main/java").walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .forEach { file ->
                val body = file.readText()
                banned.forEach { token ->
                    if (body.contains(token)) offenders += "${file.name} calls $token"
                }
            }
        assertEquals(
            "MASTER_BUILD_PROMPT 13.5: static, not dynamic. A list that reordered itself " +
                "around what somebody did most would be a measurement of that person, and " +
                "reportShortcutUsed is how a launcher is told what to rank",
            emptyList<String>(),
            offenders,
        )
    }

    /** The `shortcut` elements, in document order. */
    private fun declared(): List<Element> {
        val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(shortcuts)
        val nodes = document.getElementsByTagName("shortcut")
        return (0 until nodes.length).map { nodes.item(it) as Element }
    }

    /** The one intent inside a shortcut. A shortcut with none would not launch. */
    private fun intentOf(shortcut: Element): Element {
        val intents = shortcut.getElementsByTagName("intent")
        assertEquals(
            "shortcut ${shortcut.getAttribute("android:shortcutId")} needs exactly one intent",
            1,
            intents.length,
        )
        return intents.item(0) as Element
    }
}
