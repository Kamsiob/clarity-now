package com.kamsiob.claritynow.ui.nav

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * The floating tab bar is not drawn over a pushed screen. design-v3.md 10.15, issue #58.
 *
 * Two things are checked, and the second is the one that matters. The counter has to
 * survive About being pushed over Settings, which is the only place in the app where two
 * pushed screens are composed at once. And the bar has to have exactly one call site and
 * that call site has to be inside the guard, because a second one added later would be a
 * bar drawn over a screen that had declared itself covered, and it would look correct in
 * every screenshot taken by somebody who never opened Settings.
 */
class PushedScreensTest {

    @Test
    fun `a screen pushed over a pushed screen keeps the bar away until both are gone`() {
        val screens = PushedScreens()
        assertFalse(screens.any)

        screens.entered()
        assertTrue("Settings", screens.any)
        screens.entered()
        assertEquals("About over Settings", 2, screens.depth)

        screens.left()
        assertTrue("About closed, Settings still up", screens.any)
        screens.left()
        assertFalse("both gone", screens.any)
    }

    /**
     * A stray `left` cannot take the count negative and leave the bar hidden forever.
     *
     * The pairing is a `DisposableEffect`, so this is not reachable today. It is here
     * because the failure it would produce is an app with no navigation at all, found
     * by a person rather than by a build.
     */
    @Test
    fun `leaving more times than entering cannot hide the bar forever`() {
        val screens = PushedScreens()
        screens.left()
        screens.left()
        assertEquals(0, screens.depth)

        screens.entered()
        assertTrue(screens.any)
        screens.left()
        assertFalse(screens.any)
    }

    @Test
    fun `the tab bar has one call site and it is inside the guard`() {
        val ui = File("src/main/java/com/kamsiob/claritynow/ui")
        assertTrue(
            "expected the sources at ${ui.path}, and this run is in ${File("").absolutePath}. " +
                "Without them this test passes vacuously.",
            ui.isDirectory,
        )

        val callSites = ui.walkTopDown()
            .filter { it.isFile && it.extension == "kt" && it.name != "ClarityTabBar.kt" }
            .flatMap { file ->
                file.readLines().asSequence().mapIndexedNotNull { index, line ->
                    val code = !line.trim().startsWith("//") && !line.trim().startsWith("*")
                    if (code && line.contains("ClarityTabBar(")) file to index else null
                }
            }
            .toList()

        assertEquals("the bar is drawn in more than one place: $callSites", 1, callSites.size)

        val (file, line) = callSites.single()
        val lines = file.readLines()
        assertEquals(
            "the bar is drawn outside the pushed screen guard, in ${file.name}",
            "if (!pushedScreens.any) {",
            lines[line - 1].trim(),
        )
    }
}
