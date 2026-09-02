package com.kamsiob.claritynow.ui.theme

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * **The gate that would have caught what the polish pass found by hand.**
 *
 * Before this file, nothing in 1,089 tests asserted a single thing about the interface:
 * not that a control had a label, not that a target cleared the touch floor, not that a
 * string resource was referenced by anything. Every test targeted the domain, the corpus
 * or the replay. That is the direct reason three separate pieces of code could be written,
 * documented at length, and never called:
 *
 * - `AreaCardSemantics` and `areaCardDescription`, written in phase 2 and documented as
 *   "kept next to the card so the semantics and the visuals cannot drift apart", had zero
 *   call sites, so the largest object in the app reached TalkBack as five loose nodes.
 * - `area_never_active` and `undo_area_archived` sat in `strings.xml` unreferenced while
 *   the screens that needed them said something false instead.
 * - `AreasViewModel.offersPromote` documented the defect it exists to fix and was read
 *   nowhere.
 *
 * These are source scans rather than Compose UI tests, which is the same choice
 * `DomainPurityTest` and `FaintInkTest` already make: they need no device, they run inside
 * `verifyClarity`, and they fail naming the file and the line. A Compose UI test suite
 * would catch more and is worth having; it is not a reason to have nothing in the
 * meantime.
 */
class InterfaceContractTest {

    private val uiRoots = listOf(
        "src/main/java/com/kamsiob/claritynow/ui",
        "src/main/java/com/kamsiob/claritynow/widget",
    )

    private fun sources(): List<File> {
        val roots = uiRoots.map { File(it) }
        roots.forEach {
            assertTrue(
                "expected the sources at ${it.path}, and this run is in " +
                    File("").absolutePath + ". Without them this test passes vacuously.",
                it.isDirectory,
            )
        }
        return roots.flatMap { root ->
            root.walkTopDown().filter { it.isFile && it.extension == "kt" }.toList()
        }
    }

    private fun File.codeLines(): List<Pair<Int, String>> =
        readLines().mapIndexedNotNull { i, text ->
            val t = text.trimStart()
            if (t.startsWith("//") || t.startsWith("*") || t.startsWith("/*")) null else i + 1 to text
        }

    /**
     * **Everything that names a `Role` names what pressing it does.**
     *
     * A `Role.Button` with no `onClickLabel` announces as "button" and nothing else, which
     * is a control a screen reader user can find and cannot identify. The scan is per
     * declaration rather than per line, because the two are usually four lines apart.
     */
    @Test
    fun `every control that declares a role also declares a click label`() {
        val offenders = mutableListOf<String>()
        sources().forEach { file ->
            val text = file.readText()
            Regex("""clarity(?:Combined)?Clickable\s*\(([^)]*)\)""", RegexOption.DOT_MATCHES_ALL)
                .findAll(text)
                .forEach { match ->
                    val args = match.groupValues[1]
                    if (args.contains("role =") && !args.contains("onClickLabel")) {
                        val line = text.take(match.range.first).count { it == '\n' } + 1
                        offenders += "${file.name}:$line declares a role and no onClickLabel"
                    }
                }
        }
        assertTrue(
            "design-v3.md 13: a control a screen reader can reach has to say what it does.\n" +
                offenders.joinToString("\n"),
            offenders.isEmpty(),
        )
    }

    /**
     * **No type below the sans scale's own floor.**
     *
     * COMPONENT_AND_LAYOUT A.2 sets the floor at `meta` 12.5sp and deleted the 10.5sp role
     * by name, because it was the smallest type in the app sitting on its highest
     * consequence control. The build then shipped 9.5sp on the appearance labels and 10sp
     * twice in the color picker, all three as `fontSize =` overrides at the call site,
     * which put them outside the scale entirely where nothing could see them.
     */
    @Test
    fun `no call site sets type below the scale's floor`() {
        val size = Regex("""fontSize\s*=\s*(\d+(?:\.\d+)?)\.sp""")
        val offenders = sources().flatMap { file ->
            file.codeLines().mapNotNull { (line, text) ->
                val found = size.find(text)?.groupValues?.get(1)?.toDoubleOrNull()
                if (found != null && found < 12.5) {
                    "${file.name}:$line sets ${found}sp, under the 12.5sp floor"
                } else {
                    null
                }
            }
        }
        assertTrue(
            "COMPONENT_AND_LAYOUT A.2: the sans floor is meta 12.5sp.\n" +
                offenders.joinToString("\n"),
            offenders.isEmpty(),
        )
    }

    /**
     * **A control's box is never smaller than the touch floor.**
     *
     * `Modifier.size(n.dp)` on the same chain as a clickable pins the target, and design-v3
     * 13 puts the floor at 48dp. The scan is deliberately narrow: it looks only at a
     * `.size(` immediately preceding a `clarityClickable` or `toggleable` in the same
     * modifier chain, because a 20dp glyph inside a 48dp box is correct and common.
     */
    @Test
    fun `no clickable box is pinned under the touch floor`() {
        val offenders = mutableListOf<String>()
        sources().forEach { file ->
            val text = file.readText()
            Regex("""\.size\((\d+(?:\.\d+)?)\.dp\)\s*(?://[^\n]*\n\s*)*\.(?:clarityClickable|clarityCombinedClickable|toggleable)""")
                .findAll(text)
                .forEach { match ->
                    val dp = match.groupValues[1].toDouble()
                    if (dp < 48.0) {
                        val line = text.take(match.range.first).count { it == '\n' } + 1
                        offenders += "${file.name}:$line pins a clickable box at ${dp}dp"
                    }
                }
        }
        assertTrue(
            "design-v3.md 13: 48dp minimum touch target.\n" + offenders.joinToString("\n"),
            offenders.isEmpty(),
        )
    }

    /**
     * **The four things that were written, documented and never called.**
     *
     * Named individually rather than by a general dead code scan, because a general scan
     * over a Compose codebase is mostly false positives and this list is the evidence that
     * the problem is real. Each one shipped a defect a person met: a card with no
     * description, two false statements on screen, and a card whose only swipe was Delete.
     */
    @Test
    fun `the helpers that exist to prevent a defect are actually called`() {
        val ui = sources().joinToString("\n") { it.readText() }
        val strings = File("src/main/res/values/strings.xml").readText()
        val kotlinAndXml = ui + "\n" + strings

        listOf(
            "AreaCardSemantics" to "the area card's whole TalkBack description",
            "area_never_active" to "what a card says when nothing has ever been in the area",
            "undo_area_archived" to "the only way back from archiving an area",
            "offersPromote" to "whether an idle area with a queue can be started from its card",
        ).forEach { (symbol, why) ->
            // The lookbehind excludes a letter and an underscore and NOT a dot, because
            // `area.offersPromote` is the only way a property on a model is ever read and
            // excluding the dot made this check unable to see its own subject.
            val uses = Regex("""(?<![A-Za-z_])${Regex.escape(symbol)}(?![A-Za-z_])""")
                .findAll(kotlinAndXml).count()
            assertTrue(
                "`$symbol` is $why, and it is referenced $uses time(s). One reference is the " +
                    "declaration itself, so anything at or below one means it is dead again. " +
                    "Every one of these four shipped dead once and every one of them was a " +
                    "defect somebody hit.",
                uses > 1,
            )
        }
    }
}
