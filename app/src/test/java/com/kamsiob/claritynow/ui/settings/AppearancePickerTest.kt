package com.kamsiob.claritynow.ui.settings

import com.kamsiob.claritynow.ui.theme.AreaPalette
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Every mood the app names by string is a mood the palette has.
 *
 * ## Why this exists
 *
 * `AppearancePicker` takes its three miniature dot colors from the palette by name rather
 * than writing hexes out, so the appearance tiles and the color picker cannot drift
 * apart. That is the right design and it has one failure mode: the lookup is a
 * `first { }`, so a mood renamed out from under it throws.
 *
 * It threw. The palette moved to Flexoki, `Meadow` became `Moss` and `Earth` became
 * `Clay`, and the next open of Settings died with an `ExceptionInInitializerError`
 * pointing at a composable eighty lines away, because the failure was in a top level
 * `val` rather than in the code the trace named. Nothing caught it until a person tapped
 * the gear on the device.
 *
 * A name is a contract between two files and nothing was holding it.
 */
class AppearancePickerTest {

    @Test
    fun `every mood named in the sources is a mood the palette holds`() {
        val known = AreaPalette.moods.map { it.name }.toSet()
        assertEquals("the palette is eight moods, design-v3.md 3.4", 8, known.size)

        val root = File("src/main/java/com/kamsiob/claritynow/ui")
        assertTrue(
            "expected the ui sources at ${root.path}; without them this passes vacuously",
            root.isDirectory,
        )

        // Only files that look a mood up by name. A quoted capitalized word anywhere else
        // is a string key or a person's own area, and neither has to be a mood.
        val offenders = root.walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .filter { it.readText().contains("it.name == mood") }
            .flatMap { file ->
                val text = file.readText()
                NAMES.findAll(text)
                    .flatMap { match -> QUOTED.findAll(match.groupValues[1]).map { it.groupValues[1] } }
                    .filter { it !in known }
                    .map { "${file.name} names the mood $it" }
            }
            .toList()

        assertTrue(
            "a mood name in the sources is not in AreaPalette, so the lookup that reads " +
                "it throws inside a static initializer and takes a whole screen with it.\n" +
                offenders.joinToString("\n") + "\nThe palette holds " + known.sorted(),
            offenders.isEmpty(),
        )
    }

    private companion object {
        val NAMES = Regex("""listOf\(((?:"[A-Za-z]+"(?:,\s*)?)+)\)""")
        val QUOTED = Regex(""""([A-Za-z]+)"""")
    }
}
