package com.kamsiob.claritynow.ui.reentry

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Every word the re-entry screen can show, held against the sentence that governs it.
 * MASTER_BUILD_PROMPT 14b.4, design-v3.md 11.2.
 *
 * > A returning user must never be greeted by a measurement of their absence. If a
 * > sentence, a number, a dot row or an empty chart on the first screen back can be
 * > read as a report on how long they were gone, it is wrong, whatever else is true
 * > about it.
 *
 * **This screen cannot be verified by looking at it**, which is the whole reason it is
 * specified rather than discovered: somebody who opens this app every day never has a
 * gap, so nobody building or testing it will ever see this screen, and every defect in
 * it ships looking exactly like working code. `ReEntryGapTest` says the same thing
 * about detection and lists the four invisible ways it can be wrong. This is the same
 * gate one layer up, on the words.
 *
 * **It reads the files rather than the composition**, for two reasons. The screen is
 * Compose and this module's unit tests run on a desktop JVM with no Android, which is
 * why `FocusSessionTest` exists in the shape it does. And a test that asserted about
 * strings it was handed would only prove the ones somebody remembered to hand it: what
 * has to be checked is that there is no fifth string, no literal, and no accessibility
 * label carrying a word the other four do not.
 */
class ReEntryLanguageTest {

    private val sourceDir = File("src/main/java/com/kamsiob/claritynow/ui/reentry")
    private val stringsFile = File("src/main/res/values/strings.xml")

    /**
     * The words this screen can put on a phone. Read out of the resource file by the
     * keys the screen's own source refers to, so a string added to the family without a
     * call site is not audited and a call site added without a string fails the first
     * test below.
     */
    private fun auditedStrings(): Map<String, String> {
        val declared = STRING_ENTRY.findAll(stringsWithoutComments())
            .associate { it.groupValues[1] to it.groupValues[2] }
        return keysTheScreenReads().associateWith { key ->
            requireNotNull(declared[key]) { "$key is read by the screen and is not in strings.xml" }
        }
    }

    private fun stringsWithoutComments(): String {
        assertTrue(
            "expected $stringsFile, and this run is in ${File("").absolutePath}. " +
                "Without it this test passes vacuously.",
            stringsFile.isFile,
        )
        return XML_COMMENT.replace(stringsFile.readText(), "")
    }

    private fun sources(): List<File> {
        assertTrue(
            "expected the sources at ${sourceDir.path}, and this run is in " +
                "${File("").absolutePath}. Without them this test passes vacuously.",
            sourceDir.isDirectory,
        )
        return sourceDir.walkTopDown().filter { it.isFile && it.extension == "kt" }.toList()
    }

    private fun keysTheScreenReads(): Set<String> =
        sources().flatMap { file ->
            STRING_REFERENCE.findAll(file.readText()).map { it.groupValues[1] }
        }.toSet()

    /** Source lines that are code rather than comment or documentation. */
    private fun codeLines(): List<Pair<String, String>> =
        sources().flatMap { file ->
            file.readLines().mapNotNull { line ->
                val trimmed = line.trim()
                val comment = trimmed.startsWith("//") || trimmed.startsWith("*") ||
                    trimmed.startsWith("/*")
                if (comment || trimmed.isEmpty()) null else file.name to line
            }
        }

    /**
     * Everything the screen can say is one of these four, so the tests below reach all
     * of it. A fifth call site fails here rather than slipping past unaudited.
     */
    @Test
    fun `the screen reads exactly the four strings this test audits`() {
        assertEquals(
            setOf("reentry_title", "reentry_body", "reentry_keep", "reentry_requeue"),
            keysTheScreenReads(),
        )
    }

    /**
     * **No string states the length of the gap**, in days, in weeks, as a date or as a
     * month name. 14b.4 lists all four and adds `since March` as the one that looks
     * warm and is the same measurement.
     *
     * A digit is forbidden outright rather than checked for meaning. 11.2: "No number
     * appears on the screen at all."
     */
    @Test
    fun `no string the screen can show states the length of the gap`() {
        auditedStrings().forEach { (key, value) ->
            val lowered = value.lowercase()
            assertTrue("$key states a number: $value", value.none { it.isDigit() })
            ABSENCE_WORDS.forEach { word ->
                assertTrue(
                    "$key says \"$word\", which is a reading of how long they were gone: $value",
                    !Regex("\\b${Regex.escape(word)}\\b").containsMatchIn(lowered),
                )
            }
        }
    }

    /**
     * **The screen counts nothing.** Not what waits, not what was completed before, not
     * how many areas went idle. 14b.4.
     *
     * Two halves. No string carries a placeholder, so nothing can be substituted into
     * one, and no plural form exists to be chosen by a quantity. And nothing in the
     * source counts: there is no size, no count, no quantity string and no arithmetic
     * for one to come from. The screen takes two callbacks and no values, which is what
     * makes the second half hold by construction rather than by inspection.
     */
    @Test
    fun `the screen counts nothing`() {
        auditedStrings().forEach { (key, value) ->
            assertTrue("$key carries a format placeholder: $value", !value.contains('%'))
        }
        val counting = codeLines().filter { (_, line) ->
            COUNTING.containsMatchIn(line)
        }
        assertTrue("the screen counts something: $counting", counting.isEmpty())
    }

    /**
     * **It does not ask where the user has been**, in any wording, including a warm
     * one. 14b.4. And it carries no exclamation mark, per design-v3.md 10.13.
     *
     * The question mark is the check that generalizes: this screen offers two choices
     * and states what each does, and there is no sentence it needs that is a question.
     */
    @Test
    fun `the screen asks nothing and exclaims nothing`() {
        auditedStrings().forEach { (key, value) ->
            assertTrue("$key asks a question: $value", !value.contains('?'))
            assertTrue("$key exclaims: $value", !value.contains('!'))
            QUESTION_WORDS.forEach { word ->
                assertTrue(
                    "$key asks where they have been: $value",
                    !value.lowercase().contains(word),
                )
            }
        }
    }

    /**
     * Nothing reaches the screen except through those four keys.
     *
     * A literal in the source and an accessibility label are the two ways a word gets
     * onto a screen without a resource, and both are absent here. The buttons take
     * their click label from the label they draw, per `Buttons.kt`, so there is no
     * second wording for a screen reader to read.
     */
    @Test
    fun `nothing on the screen is a word this test cannot see`() {
        val literals = codeLines().filter { (_, line) -> line.contains('"') }
        assertTrue("a string literal on the screen: $literals", literals.isEmpty())

        val labels = codeLines().filter { (_, line) ->
            line.contains("contentDescription") || line.contains("onClickLabel") ||
                line.contains("stateDescription") || line.contains("contentDescription =")
        }
        assertTrue("an accessibility label this test does not audit: $labels", labels.isEmpty())
    }

    private companion object {

        val STRING_ENTRY = Regex("""<string name="([^"]+)">(.*?)</string>""", RegexOption.DOT_MATCHES_ALL)
        val XML_COMMENT = Regex("<!--.*?-->", RegexOption.DOT_MATCHES_ALL)
        val STRING_REFERENCE = Regex("""R\.string\.(\w+)""")

        /**
         * A count, a quantity string, or the arithmetic one would be built from. The
         * date arithmetic is named too, because `daysSince` exists in `domain.query`
         * and is exactly the value that must never reach a screen.
         */
        val COUNTING = Regex(
            """\.size\b|\.count\b|\.count\(|pluralStringResource|R\.plurals|""" +
                """daysSince|gapDays|ChronoUnit""",
        )

        /**
         * Words that measure an absence. `since` and `ago` are on the list because
         * 14b.4 names `since March` itself, and the twelve month names are on it
         * because a date spelled out is still a date.
         */
        val ABSENCE_WORDS = listOf(
            "day", "days", "week", "weeks", "fortnight", "month", "months", "year", "years",
            "ago", "since", "away", "gone", "absence", "last time", "while you were",
            "january", "february", "march", "april", "may", "june", "july", "august",
            "september", "october", "november", "december",
        )

        /** The question 14b.4 forbids, including the warm forms of it. */
        val QUESTION_WORDS = listOf("where have you", "where did you", "how long", "welcome back")
    }
}
