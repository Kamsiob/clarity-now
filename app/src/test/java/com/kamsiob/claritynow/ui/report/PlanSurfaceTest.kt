package com.kamsiob.claritynow.ui.report

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The plan exists in the Report and nowhere else, asserted by reading the app's sources.
 *
 * `CLARITY_LOGIC_ENGINE.md` 10.5 and `design-v3.md` 11.1 item 8. Accepting a plan settles a
 * pill, changes one sentence and writes one event. **No reminder, no notification, no
 * badge, no home screen card, no tile and no widget slot**, ever, and no second screen that
 * asks about it again. That is a rule about the whole app rather than about one composable,
 * so it is held here by a scan rather than by a paragraph in the file it applies to.
 *
 * ## Why a scan and not a review
 *
 * Every other guard on layer six is structural: the corpus cannot say it,
 * `PlanHistory.Accepted` has no field to carry it, and no public member of `FollowThrough`
 * returns a `CharSequence`. This one cannot be, because the offense is a new file somewhere
 * else in the app that reads a plan and draws it, and no type in `domain.guidance` can stop
 * a later session from writing one. What can stop it is a list of the places a plan is
 * allowed to be, which a person adding a surface has to edit on purpose, in a commit that
 * says so.
 *
 * ## Two things this deliberately does not fail on
 *
 * The Trail carries `PLAN_OFFERED` and `PLAN_ACCEPTED` rows, because the Trail is the event
 * log rendered and those are events. Its two sentences are fixed interface copy that name
 * nothing and quote nothing, `TrailRow` gives both rows an empty `TrailRowContent`, and
 * declining writes no event so it can leave no row. A record that something happened is not
 * a second place the plan is offered.
 *
 * And `data/widget` composes an accepted plan's stored line into the widget snapshot,
 * because `design-v3.md` 12.3 names a One Thing widget and requires it to show an accepted
 * plan and never an unaccepted or declined one. It is optional, no widget renders it today,
 * and the launcher scan below is what keeps it that way, which is the half of 12.3 this
 * test can hold.
 */
class PlanSurfaceTest {

    private val main = "src/main/java/com/kamsiob/claritynow"

    /**
     * The one surface, and the layers that carry a plan to it.
     *
     * A layer list rather than a file list, because the architecture is the invariant and
     * the file names are not. Everything outside it is a second surface by definition.
     */
    private val allowed = listOf(
        // Layer six itself.
        "$main/domain/guidance",
        // The composer that puts layer six's result on the report.
        "$main/domain/report",
        // The projection of the log, which is where an acceptance lives.
        "$main/domain/replay",
        // The log record, the cache row and the one writer in the app.
        "$main/data/event",
        "$main/data/db",
        "$main/data/repo",
        // design-v3.md 12.3's One Thing widget, which is a snapshot and not a screen. See
        // the class note.
        "$main/data/widget",
        // The surface.
        "$main/ui/report",
    )

    /**
     * The launcher, the shade and the background, which may never mention a plan in any
     * form. Not the sentences, not the control, not even the event types.
     */
    private val launcher = listOf(
        "src/main/java/com/kamsiob/claritynow/notifications",
        "src/main/java/com/kamsiob/claritynow/shortcuts",
        "src/main/java/com/kamsiob/claritynow/tile",
        "src/main/java/com/kamsiob/claritynow/widget",
        "src/main/java/com/kamsiob/claritynow/work",
    )

    /**
     * What identifies the guidance plan, as opposed to a focus session's planned minutes.
     *
     * The bare word is useless here: `plannedSeconds` is on every session and would make
     * this test a list of exceptions. These are the plan's two sentences, the control that
     * accepts one, and the types that carry either.
     */
    private val sentences = listOf(
        "offeredLine",
        "committedLine",
        "acceptedPlanLine",
        "acceptPlan",
        "ClosingPlan",
        "ClarityPlan",
        "GuidanceResult",
    )

    /** The above, plus the events themselves, for the surfaces that may not have even those. */
    private val everything = sentences + listOf(
        "PLAN_OFFERED",
        "PLAN_ACCEPTED",
        "PlanOffered",
        "PlanAccepted",
    )

    /**
     * A record of a decline, in any form. There is no `PLAN_DECLINED` in the catalog, there
     * must never be one, and 10.5 makes ignoring the offer identical to declining it.
     */
    private val declines = listOf("PLAN_DECLINED", "PlanDeclined", "declinedAt", "declineCount")

    @Test
    fun `the sources are where this test looks`() {
        assertTrue(
            "unit tests are expected to run from the app module directory, and this run " +
                "is in ${File("").absolutePath}",
            File("build.gradle.kts").isFile && File(main).isDirectory,
        )
        (allowed + launcher).forEach {
            assertTrue("nothing to scan under $it", kotlinIn(it).isNotEmpty())
        }
        assertTrue(
            "the vocabulary matches nothing in the one place a plan is supposed to be, so " +
                "every scan in this class would pass without proving anything",
            mentionsOf(sentences, "$main/ui/report").isNotEmpty(),
        )
    }

    /**
     * No reminder, no notification, no badge, no home screen card, no tile, no widget slot.
     *
     * This is the sharpest of the three, because these are the surfaces that reach a person
     * who is not reading their Report, and a plan that reaches somebody who did not open
     * the Report is the supervision `MASTER_BUILD_PROMPT.md` 19 reserves the right to
     * delete this whole layer over.
     */
    @Test
    fun `no launcher surface mentions a plan at all`() {
        val found = launcher.flatMap { mentionsOf(everything, it) }

        assertEquals(
            "a plan reached the launcher, the shade or the background. There is no version " +
                "of this that is a feature: delete it",
            emptyList<String>(),
            found,
        )
    }

    /**
     * The plan's own sentences reach one screen, and only the layers that carry them there.
     *
     * A new entry in this list is a new place a plan can be shown, so adding one has to be
     * a deliberate act with a reason in the commit rather than an import somebody added
     * while doing something else.
     */
    @Test
    fun `the plan's sentences reach one surface and the layers that carry them to it`() {
        val strays = mentionsOf(sentences, main).filterNot { line ->
            allowed.any { line.startsWith(it) }
        }

        assertEquals(
            "a plan reached a second surface. The list of places one may be is in this " +
                "test, and it is short on purpose",
            emptyList<String>(),
            strays,
        )
    }

    /**
     * Nothing in the app records a decline. `CLARITY_LOGIC_ENGINE.md` 10.5.
     *
     * Declining writes no event, is never counted and is never referenced, and ignoring the
     * offer is identical to declining it. So there is no state a person can be left in by
     * not answering, and there is nothing anywhere that could one day be read back to them.
     */
    @Test
    fun `nothing anywhere records a decline`() {
        assertEquals(
            "something recorded a decline. 10.5 says the cost of declining is nothing, and " +
                "a thing that is written down has a cost",
            emptyList<String>(),
            mentionsOf(declines, main),
        )
    }

    // ------------------------------------------------------------------ scanning

    private fun kotlinIn(path: String): List<File> {
        val dir = File(path)
        if (!dir.isDirectory) return emptyList()
        return dir.walkTopDown().filter { it.isFile && it.extension == "kt" }.toList()
    }

    /**
     * Lines of real code under [path] that name one of [words].
     *
     * Comments are skipped, deliberately, so that a file can explain this rule without
     * breaking it. The same text scan `DomainPurityTest` uses, and not a parser: the only
     * thing it can miss is a name hidden in a trailing comment on a line of code, and the
     * code on that line is read anyway.
     */
    private fun mentionsOf(words: List<String>, path: String): List<String> =
        kotlinIn(path).flatMap { file ->
            file.readLines().mapIndexedNotNull { index, text ->
                val trimmed = text.trimStart()
                val comment = trimmed.startsWith("//") ||
                    trimmed.startsWith("*") ||
                    trimmed.startsWith("/*")
                when {
                    comment -> null
                    words.none { it in text } -> null
                    else -> "${file.path}:${index + 1}: ${text.trim()}"
                }
            }
        }.sorted()
}
