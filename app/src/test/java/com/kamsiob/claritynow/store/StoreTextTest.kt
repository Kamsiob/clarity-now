package com.kamsiob.claritynow.store

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The text that leaves this repository, and the promises about it that nothing enforced.
 *
 * Three documents claim to be identical to something in the app: the store listing's
 * disclaimer, the privacy policy this project publishes, and the vocabulary rules Play's
 * health policy turns on. Until this file, all three were held by a sentence asking a
 * human to remember, and one of them had already failed: **two store listing files
 * existed**, `docs/STORE_LISTING.md` and `store-assets/LISTING.md`, written five days
 * apart, disagreeing about the app's title, its short description and every line of its
 * full description, with `HANDOFF.md` pointing at the one nobody had updated.
 *
 * That is the defect this file exists to make impossible, and the reason it is a test
 * rather than a note. **A listing is the one artifact in this project with no gate on
 * it**: it is pasted into a text box on somebody else's website, where nothing here can
 * see it again, and a mistake in it gets the app removed rather than reviewed.
 */
class StoreTextTest {

    // ------------------------------------------------------------------ one of each

    /**
     * One listing file, and one privacy policy.
     *
     * The second copy is always written for a good reason and always drifts. This fails
     * the moment another appears anywhere in the repository, which is earlier than
     * somebody noticing that two of them disagree.
     */
    @Test
    fun `there is exactly one store listing and exactly one privacy policy`() {
        val listings = markdown().filter { file ->
            val name = file.name.uppercase()
            "LISTING" in name || "STORE_LISTING" in name
        }
        assertEquals(
            "a second store listing. The two that existed until 0.14.0 disagreed about " +
                "the title, the short description and the whole full description",
            listOf("store-assets/LISTING.md"),
            listings.map { it.relative() }.sorted(),
        )
        val policies = markdown().filter { "PRIVACY" in it.name.uppercase() }
        assertEquals(listOf("PRIVACY.md"), policies.map { it.relative() }.sorted())
    }

    // ------------------------------------------------------------------ the app's words

    /**
     * The disclaimer is the app's, character for character.
     *
     * `MASTER_BUILD_PROMPT.md` 16.11 requires this sentence in both places and both files
     * say so about each other. Neither of them could tell.
     */
    @Test
    fun `the listing's disclaimer is the sentence the About screen renders`() {
        val inApp = string("about_disclaimer")
        assertTrue("the About screen lost its disclaimer", inApp.isNotBlank())
        assertTrue(
            "the listing's disclaimer is not the app's. In the app: \"$inApp\"",
            oneLine(listing()).contains(oneLine(inApp)),
        )
        assertTrue(
            "and the privacy policy carries it too",
            oneLine(privacy()).contains(oneLine(inApp)),
        )
    }

    /**
     * The published privacy policy is the app's own screen, paragraph for paragraph.
     *
     * Play wants a URL and the app renders text, so there are necessarily two copies. The
     * comment above these strings in `strings.xml` says the published one has to be
     * identical; this is what makes that true rather than intended.
     */
    @Test
    fun `every paragraph of the published policy is a string the app renders`() {
        val published = oneLine(privacy())
        val missing = (1..7).flatMap { n ->
            listOfNotNull(
                if (n > 1) string("privacy_lead_$n") else null,
                string("privacy_body_$n"),
            )
        }.filterNot { published.contains(oneLine(it)) }
        assertEquals(
            "the published policy and the app's own screen have come apart. Regenerate " +
                "PRIVACY.md from strings.xml rather than editing it",
            emptyList<String>(),
            missing,
        )
        assertTrue("the policy's heading is the app's", published.contains(oneLine(string("privacy_heading"))))
    }

    // ------------------------------------------------------------------ the vocabulary

    /**
     * The words Play's health policy turns on, checked where they would actually appear.
     *
     * `diagnosis` is the one exception and only inside the disclaimer, where it is a
     * denial. The check is on the listing's own pasteable blocks and on the published
     * policy, not on the prose around them: this file's section 1 has to be able to name
     * the forbidden words in order to forbid them.
     */
    @Test
    fun `no forbidden word appears in anything that leaves this repository`() {
        val forbidden = listOf(
            "treats", "manages", "cures", "therapy", "therapeutic",
            "clinically proven", "medically", "symptoms",
        )
        val disclaimer = oneLine(string("about_disclaimer")).lowercase()
        // Normalized to one line **before** the disclaimer is taken out, because both
        // documents wrap it across two lines and a replace against the wrapped form
        // would silently match nothing and pass this test for the wrong reason.
        val checked = (pasteable(listing()) + listOf(privacy()))
            .joinToString("\n") { oneLine(it).lowercase().replace(disclaimer, " ") }

        val found = forbidden.filter { it in checked }
        assertEquals("a word Play's health policy triggers on", emptyList<String>(), found)
        assertTrue(
            "`diagnosis` may appear only inside the disclaimer, where it is a denial",
            "diagnosis" !in checked,
        )
    }

    // ------------------------------------------------------------------ the limits

    /**
     * Play truncates rather than refuses, which is why this is a test.
     *
     * A title one character over is not rejected at upload; it is quietly cut, and the
     * first person to see it is somebody in a search result.
     */
    @Test
    fun `the title and the short description are inside Play's limits`() {
        val title = fenced(listing(), "## 3. Title")
        val short = fenced(listing(), "## 4. Short description")
        assertTrue("the title is ${title.length} characters, cap 30: $title", title.length <= 30)
        assertTrue("the short description is ${short.length}, cap 80: $short", short.length <= 80)
        assertTrue(
            "the full description is over Play's 4000 character cap",
            fenced(listing(), "## 5. Full description").length <= 4000,
        )
    }

    // ------------------------------------------------------------------ helpers

    private val root = File("../")

    private fun File.relative(): String = relativeTo(root).path

    private fun markdown(): List<File> = root.walkTopDown()
        .onEnter { it.name !in setOf("build", ".git", ".gradle", "node_modules") }
        .filter { it.isFile && it.extension == "md" }
        .toList()

    private fun listing(): String = File(root, "store-assets/LISTING.md").readText()

    private fun privacy(): String = File(root, "PRIVACY.md").readText()

    /** A string resource, unescaped the way the app renders it. */
    private fun string(name: String): String =
        Regex("""<string name="$name">(.*?)</string>""", RegexOption.DOT_MATCHES_ALL)
            .find(File("src/main/res/values/strings.xml").readText())
            ?.groupValues
            ?.get(1)
            .orEmpty()
            .replace("\\'", "'")
            .replace("&#183;", "·")
            .trim()

    /** Every fenced block in the listing: the parts that are pasted into Play. */
    private fun pasteable(source: String): List<String> =
        Regex("```\\n(.*?)\\n```", RegexOption.DOT_MATCHES_ALL)
            .findAll(source)
            .map { it.groupValues[1] }
            .toList()

    /** The first fenced block under a heading. */
    private fun fenced(source: String, heading: String): String =
        pasteable(source.substringAfter(heading)).first()

    /**
     * Line breaks collapsed to single spaces.
     *
     * The app holds each sentence as one string and the documents wrap them at 90
     * columns, so a comparison that did not do this would fail on every line that was
     * ever reflowed, which is a comparison nobody would keep.
     */
    private fun oneLine(text: String): String = text.replace(Regex("\\s+"), " ").trim()
}
