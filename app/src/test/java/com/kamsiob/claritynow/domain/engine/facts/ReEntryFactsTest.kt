package com.kamsiob.claritynow.domain.engine.facts

import com.kamsiob.claritynow.domain.engine.AreaFacts
import com.kamsiob.claritynow.domain.engine.CueFacts
import com.kamsiob.claritynow.domain.engine.FactExtractor
import com.kamsiob.claritynow.domain.engine.FactSet
import com.kamsiob.claritynow.domain.engine.HistoryFacts
import com.kamsiob.claritynow.domain.engine.ItemFacts
import com.kamsiob.claritynow.domain.engine.PulseFacts
import com.kamsiob.claritynow.domain.engine.RollupFacts
import com.kamsiob.claritynow.domain.engine.WindowFacts
import com.kamsiob.claritynow.domain.query.TrailTestLog
import com.kamsiob.claritynow.domain.query.area
import com.kamsiob.claritynow.domain.query.at
import com.kamsiob.claritynow.domain.query.item
import com.kamsiob.claritynow.domain.query.opened
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.lang.reflect.Modifier

/**
 * The half of re-entry the Report, the Momentum headline and the Areas banner need.
 * MASTER_BUILD_PROMPT 14b.4, Addendum 01 4d.
 *
 * **Detection landed in phase 3b and the Pulse's two day window in phase 6. The seven
 * day one was given to phase 8 and phase 8 did not carry it**, so a person coming back
 * after a fortnight could be told about the gap by the first report they opened. This
 * is the fact that lets the next phase stop that, and there is nothing on it a screen
 * could turn back into a measurement.
 *
 * The three assertions that matter are the two ends of the window and its shape. The
 * ends are off by one in both directions if the comparison is written the obvious way,
 * and the shape is the prohibition: 14b.4 says the value carries the date of the
 * return and never the length of the absence, and what reaches a rule here is one bit
 * less than that.
 */
class ReEntryFactsTest {

    /** Three days of use, a fortnight and more of nothing, then daily use again. */
    private fun returnedOnDayTwenty(): TrailTestLog {
        val log = TrailTestLog()
        log.area(at(0, 8), "work", "Work")
        for (day in 0..2) log.opened(day)
        for (day in 20..30) log.opened(day)
        log.item(at(20, 9), "i1", "work", "Something")
        return log
    }

    private fun history(log: TrailTestLog, fromDay: Int, toDay: Int): HistoryFacts =
        FactExtractor(log.queries()).extract(window(fromDay, toDay)).history

    @Test
    fun `the day of the return is inside the quiet week`() {
        assertTrue(history(returnedOnDayTwenty(), 20, 21).isJustBackFromAbsence)
    }

    /**
     * The seventh day is still inside it, because seven days from the return counts
     * the day of the return.
     *
     * Written as `daysSince in 0 until 7`, so this is the last day that answers true.
     * The obvious `<= 7` gives an eight day week and the obvious `< 6` gives a six day
     * one, and neither shows up anywhere except on a person who has been away.
     */
    @Test
    fun `the sixth day after the return is still inside the quiet week`() {
        assertTrue(history(returnedOnDayTwenty(), 26, 27).isJustBackFromAbsence)
    }

    @Test
    fun `the eighth day is outside it and the app speaks again`() {
        assertFalse(history(returnedOnDayTwenty(), 27, 28).isJustBackFromAbsence)
    }

    /**
     * The day the window ends is the day asked about, not the day it opens.
     *
     * A Report window is the trailing seven days, so it still reaches back into the
     * quiet week for six days after that week closes. Asking about the window start
     * would buy a fortnight of withholding where 14b.4 asks for seven days, and the
     * report a person opens a fortnight after coming back would be short for a reason
     * nothing could explain.
     */
    @Test
    fun `a trailing week that reaches back into the quiet week is not itself inside it`() {
        assertFalse(history(returnedOnDayTwenty(), 24, 31).isJustBackFromAbsence)
    }

    @Test
    fun `somebody who has never been away is never inside it`() {
        val log = TrailTestLog()
        log.area(at(0, 8), "work", "Work")
        for (day in 0..20) log.opened(day)

        assertFalse(history(log, 14, 21).isJustBackFromAbsence)
    }

    @Test
    fun `a log with no presence markers at all is not a return`() {
        val log = TrailTestLog()
        log.area(at(0, 8), "work", "Work")
        log.item(at(3, 9), "i1", "work", "Something")

        assertFalse(history(log, 0, 7).isJustBackFromAbsence)
    }

    /**
     * Nothing on the fact set can be read back into a length of absence.
     *
     * 14b.4 is explicit that there is no field holding the number and no function
     * handing it out, "because a prohibition that rests on somebody remembering it is
     * a prohibition with a shelf life". `ReEntry` keeps that by carrying the date of
     * the return alone, and layer one keeps it by carrying a boolean, which has no
     * length in it and no date either.
     *
     * A second entry here is a number about somebody's absence arriving on the fact
     * set, whatever it was added for.
     */
    @Test
    fun `the only re-entry fact is a boolean`() {
        val banned = Regex("absence|re[-_]?entry|gap", RegexOption.IGNORE_CASE)
        val found = factClasses.flatMap { type ->
            type.declaredFields
                .filterNot { Modifier.isStatic(it.modifiers) }
                .filter { banned.containsMatchIn(it.name) }
                .map { "${type.simpleName}.${it.name}" to it.type.simpleName }
        }.sortedBy { it.first }
        assertEquals(
            "MASTER_BUILD_PROMPT 14b.4 forbids a returning person being greeted by a " +
                "measurement of their absence, and keeps that by there being no number to " +
                "greet them with. A field here that is not a boolean is one",
            listOf("HistoryFacts.isJustBackFromAbsence" to "boolean"),
            found,
        )
    }

    private val factClasses = listOf(
        FactSet::class.java,
        WindowFacts::class.java,
        AreaFacts::class.java,
        RollupFacts::class.java,
        ItemFacts::class.java,
        HistoryFacts::class.java,
        PulseFacts::class.java,
        CueFacts::class.java,
    )
}
