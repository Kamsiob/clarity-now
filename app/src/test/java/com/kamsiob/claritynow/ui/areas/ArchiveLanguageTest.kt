package com.kamsiob.claritynow.ui.areas

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Every word the archive screen can show, held against the rule that governs it.
 * MASTER_BUILD_PROMPT section 11, design-v3.md 10.20 and 10.13, issue #15.
 *
 * > Every sentence about a person's own data comes from a corpus file, through the
 * > engine layers in order. No second path. Fixed interface labels and direct readouts
 * > of a queried number live in `strings.xml`; observations never do.
 *
 * **This screen is almost entirely labels and readouts, which is why it could be built
 * while phase 9 held the corpus, and that is exactly what makes it a place the rule can
 * be broken by accident.** It has a list of the parts of a person's life they have put
 * away and a count of what is in each. One more line saying how long each has been in
 * there, or that one of them is nearly empty, would be an observation, would read as
 * completely natural beside the count that is allowed, and would be a second path.
 *
 * **It reads the files rather than the composition**, the same arrangement and for the
 * same reasons as `ReEntryLanguageTest`: this module's unit tests run on a desktop JVM
 * with no Android, and a test given a list of strings only proves the ones somebody
 * remembered to give it. What has to be checked is that there is no further string, no
 * literal and no accessibility label carrying a word the audited ones do not.
 */
class ArchiveLanguageTest {

    private val source = File("src/main/java/com/kamsiob/claritynow/ui/areas/ArchiveScreen.kt")
    private val stringsFile = File("src/main/res/values/strings.xml")

    private fun text(file: File): String {
        assertTrue(
            "expected $file, and this run is in ${File("").absolutePath}. " +
                "Without it this test passes vacuously.",
            file.isFile,
        )
        return file.readText()
    }

    private fun stringsWithoutComments(): String = XML_COMMENT.replace(text(stringsFile), "")

    /** The keys the screen actually refers to, resolved to what they say. */
    private fun auditedStrings(): Map<String, String> {
        val body = stringsWithoutComments()
        val singular = STRING_ENTRY.findAll(body).associate { it.groupValues[1] to it.groupValues[2] }
        val plural = PLURAL_ENTRY.findAll(body).associate { match ->
            match.groupValues[1] to PLURAL_ITEM.findAll(match.groupValues[2])
                .joinToString(" ") { it.groupValues[1] }
        }
        return keysTheScreenReads().associateWith { key ->
            requireNotNull(singular[key] ?: plural[key]) {
                "$key is read by the archive screen and is not in strings.xml"
            }
        }
    }

    private fun keysTheScreenReads(): Set<String> =
        RESOURCE_REFERENCE.findAll(text(source)).map { it.groupValues[1] }.toSet()

    /** Source lines that are code rather than comment or documentation. */
    private fun codeLines(): List<String> = text(source).lines().filter { line ->
        val trimmed = line.trim()
        trimmed.isNotEmpty() && !trimmed.startsWith("//") && !trimmed.startsWith("*") &&
            !trimmed.startsWith("/*")
    }

    /**
     * Everything the screen can say is one of these eight, so the tests below reach all
     * of it. A ninth call site fails here rather than slipping past unaudited.
     */
    @Test
    fun `the screen reads exactly the strings this test audits`() {
        assertEquals(
            setOf(
                "archive_title",
                "archive_empty_title",
                "archive_empty_body",
                "archive_area_no_items",
                "archive_area_items",
                "action_restore",
                "action_delete",
                "cd_archive_restore",
                "cd_archive_delete",
            ),
            keysTheScreenReads(),
        )
    }

    /**
     * **Nothing on this screen reads anything into what is on it.** The count of what is
     * inside an area is a direct readout of a count query and is allowed. How long it
     * has been there, how long since anything happened in it, and whether that is a lot
     * or a little are observations, and MASTER_BUILD_PROMPT section 11 gives those one
     * path that does not run through this file.
     *
     * Time is the whole of the check because time is the whole of the temptation: the
     * area card one screen away already draws `Last active 12 days ago`, the state
     * behind this screen carries the value it is drawn from, and on this screen the same
     * line would be the app telling somebody how long ago they gave up on something.
     */
    @Test
    fun `no string on the screen measures time`() {
        auditedStrings().forEach { (key, value) ->
            TIME_WORDS.forEach { word ->
                assertTrue(
                    "$key says \"$word\", which is a reading rather than a readout: $value",
                    !Regex("\\b${Regex.escape(word)}\\b").containsMatchIn(value.lowercase()),
                )
            }
        }
    }

    /**
     * **And nothing on it judges what is on it.** design-v3.md 10.13 makes an empty
     * state an invitation and never a scold, and 10.20 extends that to the rows: an
     * archive is where somebody put a part of their life down, and every word on the
     * list of those has to be able to be read on the worst day of a year.
     *
     * The exclamation mark is 10.13 by name. The question mark generalizes it: this
     * screen states what each control does and there is no sentence here that needs to
     * be a question.
     */
    @Test
    fun `no string on the screen judges, asks or exclaims`() {
        auditedStrings().forEach { (key, value) ->
            assertTrue("$key exclaims: $value", !value.contains('!'))
            assertTrue("$key asks a question: $value", !value.contains('?'))
            JUDGING_WORDS.forEach { word ->
                assertTrue(
                    "$key says \"$word\", which is a judgment: $value",
                    !Regex("\\b${Regex.escape(word)}\\b").containsMatchIn(value.lowercase()),
                )
            }
        }
    }

    /**
     * **The only things substituted into a string are a count and an area's own name.**
     * Both are direct readouts. A third placeholder is how a sentence gets assembled on
     * a screen, and an assembled sentence about somebody's own data is the thing that
     * has one path and it is not this one.
     */
    @Test
    fun `the only placeholders are the count and the name`() {
        auditedStrings().forEach { (key, value) ->
            val placeholders = PLACEHOLDER.findAll(value).map { it.value }.toList()
            val allowed = when (key) {
                "archive_area_items" -> listOf("%d", "%d")
                "cd_archive_restore", "cd_archive_delete" -> listOf("%1\$s")
                else -> emptyList()
            }
            assertEquals("$key substitutes something this test does not audit", allowed, placeholders)
        }
    }

    /**
     * Nothing reaches the screen except through those keys.
     *
     * A string literal is the one way a word gets onto a screen without a resource, and
     * that covers the accessibility labels too: both descriptions this screen sets are
     * built from an audited `cd_` string, and the test above proves those are the only
     * two the file refers to. The only other thing drawn here is an area's own name,
     * which is the person's word rather than the app's.
     */
    @Test
    fun `nothing on the screen is a word this test cannot see`() {
        val literals = codeLines().filter { it.contains('"') }
        assertTrue("a string literal on the archive screen: $literals", literals.isEmpty())
    }

    /**
     * **The screen cannot reach the values a reading would be built from**, which is
     * what makes the time test above hold against a future edit rather than against
     * today's file. design-v3.md 10.20 keeps the last active line off this screen and
     * `ArchivedAreaModel` does not carry it, so the way that line arrives is somebody
     * putting the field back and drawing it. Naming the fields here is what makes that
     * an argument somebody has to have rather than a line somebody adds.
     */
    @Test
    fun `the screen does not reach for a value that measures time`() {
        val reaching = codeLines().filter { line -> TIME_VALUES.containsMatchIn(line) }
        assertTrue("the archive screen reads a time value: $reaching", reaching.isEmpty())
    }

    private companion object {

        val STRING_ENTRY = Regex("""<string name="([^"]+)">(.*?)</string>""", RegexOption.DOT_MATCHES_ALL)
        val PLURAL_ENTRY = Regex("""<plurals name="([^"]+)">(.*?)</plurals>""", RegexOption.DOT_MATCHES_ALL)
        val PLURAL_ITEM = Regex("""<item quantity="[^"]+">(.*?)</item>""", RegexOption.DOT_MATCHES_ALL)
        val XML_COMMENT = Regex("<!--.*?-->", RegexOption.DOT_MATCHES_ALL)
        val RESOURCE_REFERENCE = Regex("""R\.(?:string|plurals)\.(\w+)""")
        val PLACEHOLDER = Regex("""%\d*\$?[a-z]""")

        /**
         * Every way a screen states an amount of time. `last` and `still` are on the
         * list because `Last active` and `Still here` are the two lines that would be
         * written first, and both are a reading.
         */
        val TIME_WORDS = listOf(
            "day", "days", "week", "weeks", "month", "months", "year", "years",
            "ago", "since", "last", "recent", "recently", "still", "already",
            "long", "old", "older", "while", "yet",
        )

        /**
         * The fields on the state and the strings on the area card that state an amount
         * of time or a total. Every one of them is one screen away and none of them may
         * be here.
         */
        val TIME_VALUES = Regex(
            """daysSince|lastEvent|createdAt|completedAt|completedCount|""" +
                """area_last_active|area_never_active""",
        )

        /** Words that grade what somebody did with their own areas. */
        val JUDGING_WORDS = listOf(
            "forgot", "forgotten", "abandoned", "neglected", "stale", "overdue",
            "should", "never", "failed", "unfinished", "left", "gave", "just",
        )
    }
}
