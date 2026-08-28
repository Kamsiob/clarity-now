package com.kamsiob.claritynow.ui.theme

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * The scroll edge, and the blur that is refused in its place. design-v3.md 15.1, 15.3 and
 * 6.1, issue #54.
 *
 * Through phase 12 this app had no scroll edge treatment of any kind: a grep of `ui/` for
 * `verticalGradient`, `fadingEdge`, `blur(` or `overscroll` returned nothing, so a
 * sentence scrolling toward the clock stopped being drawn at a hard pixel edge and the
 * floating tab bar was a pill with content sliding out from under it.
 *
 * Two things are pinned here and the second is the more important of the two.
 *
 * The first is that the fade is drawn by something. A modifier nothing calls is the state
 * `raise` was in for three phases, and `SurfaceLadderTest` exists because of it.
 *
 * The second is the refusal. The statistically common 2026 answer to content passing
 * behind a floating bar is a translucent blur, and this one is refused by name in two
 * places: 15.1's "glassmorphism used as decoration rather than to solve a layering
 * problem", and 15.3, which writes out this exact case and says "a fade to the ground
 * color is the permitted form of one, and reaching for the blur because it looks more
 * modern is exactly the move the entry describes". A prohibition that only exists in a
 * document is one somebody rediscovers by shipping it, so it is a test.
 */
class ScrollEdgeTest {

    private val uiRoot = File("src/main/java/com/kamsiob/claritynow/ui")

    private fun sources(): List<File> {
        assertTrue(
            "expected the UI sources at ${uiRoot.path}, and this run is in " +
                File("").absolutePath + ". Without them these tests pass vacuously.",
            uiRoot.isDirectory,
        )
        return uiRoot.walkTopDown().filter { it.isFile && it.extension == "kt" }.toList()
    }

    /** A line of code rather than a line of prose about code. */
    private fun codeLines(file: File): List<Pair<Int, String>> =
        file.readLines().mapIndexedNotNull { index, text ->
            val trimmed = text.trimStart()
            val comment = trimmed.startsWith("//") || trimmed.startsWith("*") ||
                trimmed.startsWith("/*")
            if (comment) null else (index + 1) to text
        }

    @Test
    fun `the scroll edge fade is drawn by surfaces and not only declared`() {
        val callers = sources()
            .filter { it.name != "ScrollEdge.kt" }
            .filter { file -> codeLines(file).any { it.second.contains(".scrollEdgeFade(") } }
            .map { it.name }
            .sorted()

        assertTrue(
            "nothing outside ScrollEdge.kt fades its scroll edge. Content passing under " +
                "the status bar and behind the floating tab bar is design-v3.md 15.3's " +
                "named layering problem, and a modifier with no call site is not an " +
                "answer to it. Found: $callers",
            callers.size >= 3,
        )
    }

    /**
     * Every surface that fades takes both edges.
     *
     * A screen that softens its top and leaves content cut off behind the tab bar has
     * solved the half of the problem nobody complained about. Both distances come from
     * `ScrollEdge`, so a call site naming one and not the other is visible here.
     */
    @Test
    fun `a surface that fades one edge fades both`() {
        val offenders = sources()
            .filter { it.name != "ScrollEdge.kt" }
            .mapNotNull { file ->
                val text = file.readText()
                val start = text.indexOf(".scrollEdgeFade(")
                if (start < 0) return@mapNotNull null
                val call = text.substring(start, minOf(text.length, start + 600))
                val hasTop = call.contains("top =")
                val hasBottom = call.contains("bottom =")
                if (hasTop && hasBottom) null else "${file.name}: top=$hasTop bottom=$hasBottom"
            }
        assertTrue(
            "a scroll edge fade names one edge and not the other: $offenders",
            offenders.isEmpty(),
        )
    }

    /**
     * design-v3.md 15.1 and 15.3. The blur is the obvious answer and it is refused.
     *
     * `RenderEffect` is included because it is how a blur arrives when `Modifier.blur` is
     * not available on the surface somebody wants it on. The one mention of it in the app
     * is a comment in `TutorialOverlay.kt` explaining why the spotlight does not use one,
     * which is why this reads code lines rather than whole files.
     */
    @Test
    fun `no surface reaches for a blur`() {
        val forbidden = listOf(".blur(", "BlurEffect", "RenderEffect", "BlurMaskFilter")
        val offenders = sources().flatMap { file ->
            codeLines(file).mapNotNull { (line, text) ->
                val hit = forbidden.firstOrNull { text.contains(it) }
                if (hit == null) null else "${file.name}:$line uses $hit"
            }
        }
        assertTrue(
            "design-v3.md 15.1 forbids \"glassmorphism used as decoration rather than " +
                "to solve a layering problem\" and 15.3 answers the one real layering " +
                "problem this app has with a fade instead. If a blur is genuinely the " +
                "answer to something new, that is a decision to record and not a " +
                "modifier to add: " + offenders.joinToString("\n"),
            offenders.isEmpty(),
        )
    }
}
