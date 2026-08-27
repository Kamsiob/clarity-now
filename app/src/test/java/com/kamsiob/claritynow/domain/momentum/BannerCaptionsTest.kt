package com.kamsiob.claritynow.domain.momentum

import com.kamsiob.claritynow.domain.engine.catalog.CorpusFixture
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The banner's caption bench, against the real corpus. `CORPUS_3_MOMENTUM.md`, "Banner
 * captions", and design-v3.md 10.2.
 *
 * **This file is what makes a binding table living outside `SlotBindings` survivable.**
 * [BannerCaptions] explains why the table is there and where it belongs; these are the
 * checks that stop it drifting away from the file it was written against. A caption line
 * added, renamed or reworded in the corpus fails the build here rather than quietly
 * dropping out of the bench or, worse, keeping a binding that now belongs to a different
 * sentence.
 */
class BannerCaptionsTest {

    private val bench = CorpusFixture.momentum.auxiliary.getValue(BannerCaptions.BENCH)

    @Test
    fun `every caption line in the corpus has a binding and nothing binds a line that is gone`() {
        assertEquals(
            "the binding table and the caption bench name the same lines. An unbound line " +
                "can never be shown and a binding with no line is a table nobody maintained",
            bench.map { it.key }.toSet(),
            BannerCaptions.quotedLines.keys,
        )
    }

    @Test
    fun `the binding table quotes the line each entry was written for`() {
        val drifted = bench.mapNotNull { line ->
            val quoted = BannerCaptions.quotedLines[line.key]
            if (quoted == line.text) null else "${line.key}: corpus `${line.text}`, table `$quoted`"
        }

        assertEquals(
            "a caption reworded in the corpus keeps its old bindings, which is how a number " +
                "ends up attached to a sentence nobody wrote it for",
            emptyList<String>(),
            drifted,
        )
    }

    @Test
    fun `a week with no completions renders only the lines authored for that case`() {
        // Four captures into one area, nothing finished. `CORPUS_3_MOMENTUM.md` says
        // bnc.04 and bnc.10 exist precisely so this case has authored lines rather than a
        // template producing `0 completed`.
        val fixture = MomentumFixture().seedTwoAreas()
        (22..25).forEach { fixture.capture(it) }
        val facts = fixture.weekFacts(day = 25, hour = 18)

        val renderable = bench.filter { BannerCaptions.renderable(it, facts) != null }.map { it.key }

        assertEquals(0, facts.window.completions)
        assertEquals(
            "every line whose count slot would render a nought is out of the bench, and what " +
                "is left is the three that state something true about a week with nothing " +
                "finished in it plus the queue line",
            listOf("bnc.04", "bnc.05", "bnc.08", "bnc.10"),
            renderable,
        )
    }

    @Test
    fun `a week with completions can never render the two lines that deny them`() {
        val fixture = MomentumFixture().seedTwoAreas()
        (22..25).forEach { fixture.completeOne(it) }
        val facts = fixture.weekFacts(day = 25, hour = 18)

        val renderable = bench.filter { BannerCaptions.renderable(it, facts) != null }.map { it.key }

        assertTrue("four completions in the window", facts.window.completions > 0)
        assertTrue(
            "bnc.04 and bnc.10 both claim nothing was completed, and both are false here",
            renderable.none { it == "bnc.04" || it == "bnc.10" },
        )
        assertTrue("something is still sayable about the week", renderable.isNotEmpty())
    }

    @Test
    fun `no caption ever renders a nought`() {
        // Walked across a fortnight of an ordinary log rather than at one instant, because
        // the shapes that produce a zero arrive on particular days: a Monday with nothing
        // in it, a week with no focus, an empty queue.
        val fixture = MomentumFixture().seedTwoAreas()
        (15..25).forEach { day ->
            fixture.capture(day)
            if (day % 3 == 0) fixture.completeOne(day)
            if (day % 4 == 0) fixture.focusSession(day, minutes = 20)
        }

        val offenders = (15..25).flatMap { day ->
            val facts = fixture.weekFacts(day = day, hour = 20)
            bench.mapNotNull { line ->
                val rendered = BannerCaptions.renderable(line, facts) ?: return@mapNotNull null
                if (NOUGHT.containsMatchIn(rendered)) "day $day, ${line.key}: `$rendered`" else null
            }
        }

        assertEquals(
            "CLARITY_LOGIC_ENGINE.md 7.2 and validator check 4: zero never reaches a template",
            emptyList<String>(),
            offenders,
        )
    }

    @Test
    fun `the same log at the same instant produces the same banner`() {
        // Determinism, which is the property CLARITY_LOGIC_ENGINE.md 7.6 exists for: two
        // devices holding one merged log must reach the same line with no shared state.
        // Two composers over one log and one instant is the smallest form of that check,
        // and it is the one that would fail the day somebody reached for a random.
        val first = MomentumFixture().seedTwoAreas()
        val second = MomentumFixture().seedTwoAreas()
        (15..25).forEach { day ->
            first.capture(day)
            second.capture(day)
            if (day % 2 == 0) {
                first.completeOne(day)
                second.completeOne(day)
            }
        }

        assertEquals(first.banner(day = 25, hour = 9), second.banner(day = 25, hour = 9))
    }

    private companion object {

        /** A standalone nought. `10 completed` is fine; `0 completed` is the defect. */
        val NOUGHT = Regex("""\b0\b""")
    }
}
