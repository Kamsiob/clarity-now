package com.kamsiob.claritynow.data.event

import com.kamsiob.claritynow.domain.replay.FocusOutcome
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * The word that was renamed away stays away. DECISIONS.md C6, Addendum 01 4e.
 *
 * Addendum 01 4e: a focus session ended early is a completed short session, and the
 * word must appear nowhere a person can see it. On a plain reading that was already
 * true before the rename, because no user visible string ever contained it and the
 * Trail already reads "Stopped after N minutes". The owner's decision went past the
 * plain reading, and the reason is the one this test protects:
 *
 * > `docs/EVENT_FORMAT.md` is a public contract that the future Linux desktop app
 * > will be built against in a separate session. The word teaches the wrong framing
 * > to the next implementer.
 *
 * A type name crosses a process boundary into another codebase, and every name in
 * that document is an instruction about what the concept means. A type called
 * `FOCUS_ABANDONED` invites a screen that says abandoned, a rule that counts
 * abandonments, and an inference of abandonment by subtraction, which
 * `domain.query.FocusCounts` explicitly forbids because a killed process leaves a
 * started session with no terminal event and `started != completed + endedEarly` is
 * therefore a legal state rather than a bug.
 *
 * **Comments are exempt, and that exemption is the point rather than a loophole.**
 * The reasoning for a decision has to be readable at the place the decision was
 * made, and every remaining mention in the codebase is a comment explaining either
 * this rename or the neutrality rule behind it. A mention in a comment that is not
 * doing one of those two jobs is a mention that should not be there.
 *
 * Written as a source scan on the `DomainPurityTest` precedent, for the same reason
 * that test gives: a rule enforced by proofreading is a rule that survives exactly
 * as long as the person who remembers it.
 */
class EndedEarlyRenameTest {

    private val mainSource = "src/main/java"
    private val mainRes = "src/main/res"

    /** Case insensitive, and deliberately the stem rather than the whole word. */
    private val forbidden = Regex("""abandon""", RegexOption.IGNORE_CASE)

    /**
     * Where the word is allowed to survive, and why each one is not a loophole.
     *
     * **The engine and the surfaces that name its families are exempt, and this is the
     * important one.** The family keys `focusAbandonment` and `abandonmentPattern` are
     * declared in
     * `CLARITY_LOGIC_ENGINE.md` 6.3 and 6.5 and are the identifiers the corpus files
     * key their line tables on. Renaming them is a corpus edit, and a corpus edit is
     * presented to the owner for approval rather than made by a builder. They also sit
     * behind no surface a person can reach. `domain/report` and `domain/momentum`
     * are exempt for the same reason and only that reason: they name those families
     * in order to compose with them, which is reading the corpus's vocabulary rather
     * than choosing it.
     *
     * That is a narrower claim than C6 made about the event type, and deliberately so.
     * C6's reasoning was that `docs/EVENT_FORMAT.md` is a contract a second
     * implementation is built from, so a name in it instructs the next implementer.
     * The event vocabulary, the strings a person reads and the UI are that contract's
     * surface. An internal family key shared with a corpus file is a different thing,
     * and pretending otherwise would have this test demand a change it has no standing
     * to make.
     *
     * **`ValidatorVocabulary` is exempt because it exists to forbid the word.** Check
     * 10 refuses past participles whose deleted agent could only be the person, and a
     * ban list that cannot spell what it bans is not a ban list. Same shape as the note
     * in `DECISIONS.md` about the language gate being unable to quote the spellings it
     * rejects: a rule covering every file eventually cannot describe itself, and the
     * honest answer is a named exemption rather than a weaker rule.
     */
    private val exemptPathParts = listOf("/domain/engine/", "/domain/report/", "/domain/momentum/")
    private val exemptFiles = setOf("ValidatorVocabulary.kt")

    private fun isExempt(path: String): Boolean =
        exemptPathParts.any { path.replace('\\', '/').contains(it) } ||
            File(path).name in exemptFiles

    private data class SourceLine(val path: String, val number: Int, val text: String)

    private fun filesUnder(path: String, extension: String): List<File> {
        val dir = File(path)
        if (!dir.isDirectory) return emptyList()
        return dir.walkTopDown().filter { it.isFile && it.extension == extension }.toList()
    }

    /**
     * A line that starts a comment. Enough for a text scan and deliberately not a
     * parser, exactly as in `DomainPurityTest`: the only false negative it can
     * produce is the word hidden in a trailing comment on a line of real code, and
     * that line's code would still be read.
     */
    private fun isComment(text: String): Boolean {
        val trimmed = text.trimStart()
        return trimmed.startsWith("//") || trimmed.startsWith("*") || trimmed.startsWith("/*")
    }

    @Test
    fun `the scan looks where the source actually is`() {
        assertTrue(
            "unit tests are expected to run from the app module directory, and this " +
                "run is in ${File("").absolutePath}",
            File("build.gradle.kts").isFile,
        )
        assertTrue("no Kotlin source under $mainSource", filesUnder(mainSource, "kt").isNotEmpty())
        assertTrue("no resources under $mainRes", filesUnder(mainRes, "xml").isNotEmpty())
        // A scan that found nothing to read would pass every assertion below.
        assertTrue(
            "the scan should be reading the whole app, not a corner of it",
            filesUnder(mainSource, "kt").size > 20,
        )
    }

    @Test
    fun `no code in the app names the concept by its old name`() {
        val offenders = filesUnder(mainSource, "kt")
            .filterNot { isExempt(it.path) }
            .flatMap { file ->
                file.readLines().mapIndexedNotNull { index, text ->
                    if (isComment(text)) null else SourceLine(file.path, index + 1, text)
                }
            }
            .filter { forbidden.containsMatchIn(it.text) }

        assertTrue(
            "the word survives in code, not in a comment explaining the rename. A " +
                "type, class, property or enum constant carrying it is what " +
                "docs/EVENT_FORMAT.md hands to a second implementation, and " +
                "DECISIONS.md C6 is the record of why that matters more than the " +
                "export path did:\n" +
                offenders.joinToString("\n") { "${it.path}:${it.number}: ${it.text.trim()}" },
            offenders.isEmpty(),
        )
    }

    /**
     * And no string a person can read contains it, which was true before the rename
     * and has to stay true after it.
     *
     * XML comments are stripped first, so a note to a translator explaining the
     * neutrality rule is allowed while a `<string>` carrying the word is not. The
     * scan covers every `values` folder rather than only the default one, because a
     * localization added later is exactly the kind of place a euphemism layer gets
     * reintroduced by someone translating from a type name.
     */
    @Test
    fun `no user visible string contains the word`() {
        val offenders = filesUnder(mainRes, "xml").mapNotNull { file ->
            val text = file.readText().replace(Regex("""<!--.*?-->""", RegexOption.DOT_MATCHES_ALL), "")
            if (forbidden.containsMatchIn(text)) file.path else null
        }

        assertTrue(
            "a resource contains the word. Addendum 01 4e: it appears nowhere a " +
                "person can see it, including the Trail and every accessibility " +
                "label. The Trail row reads \"Stopped after N minutes\":\n" +
                offenders.joinToString("\n"),
            offenders.isEmpty(),
        )
    }

    /**
     * The enum itself, checked directly rather than only through the file scan.
     *
     * The scan above reads text and can be defeated by a name assembled at runtime
     * or by a file it does not walk. This reads the catalog the app actually
     * compiled, which is the thing `ClarityEventJson` writes into a log file.
     */
    @Test
    fun `the compiled catalog carries no trace of the old name`() {
        val offenders = ClarityEventType.entries.filter { forbidden.containsMatchIn(it.name) }
        assertTrue("event types still named for it: $offenders", offenders.isEmpty())
        assertTrue(
            "focus outcomes still named for it",
            FocusOutcome.entries.none { forbidden.containsMatchIn(it.name) },
        )
    }
}
