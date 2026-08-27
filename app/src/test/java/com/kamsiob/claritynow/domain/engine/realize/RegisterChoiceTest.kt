package com.kamsiob.claritynow.domain.engine.realize

import com.kamsiob.claritynow.domain.engine.PartOfDay
import com.kamsiob.claritynow.domain.engine.catalog.Purpose
import com.kamsiob.claritynow.domain.engine.catalog.Register
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** CLARITY_LOGIC_ENGINE.md 7.4, as an order rather than as a decision. */
class RegisterChoiceTest {

    @Test
    fun `an unflattering rule reaches for the neutral agent register first`() {
        val order = RegisterChoice.preference(Purpose.REPORT_OBSERVATION, unflattering = true, partOfDay = PartOfDay.MORNING)
        assertEquals(Register.NEUTRAL_AGENT, order.first())
    }

    @Test
    fun `a neutral or positive family can never reach the neutral agent register`() {
        for (purpose in Purpose.entries) {
            for (band in PartOfDay.entries) {
                val order = RegisterChoice.preference(purpose, unflattering = false, partOfDay = band, notable = true)
                assertFalse(
                    "$purpose in $band offered the neutral agent register to a family that is not unflattering",
                    Register.NEUTRAL_AGENT in order,
                )
            }
        }
    }

    @Test
    fun `dawn and midday prefer the plainer voices and evening prefers the reflective one`() {
        val morning = RegisterChoice.preference(Purpose.PULSE, unflattering = false, partOfDay = PartOfDay.MORNING)
        assertEquals(Register.PLAIN, morning.first())
        val afternoon = RegisterChoice.preference(Purpose.PULSE, unflattering = false, partOfDay = PartOfDay.AFTERNOON)
        assertEquals(Register.PLAIN, afternoon.first())
        val evening = RegisterChoice.preference(Purpose.PULSE, unflattering = false, partOfDay = PartOfDay.EVENING)
        assertEquals(Register.REFLECTIVE, evening.first())
    }

    @Test
    fun `the time of day never decides a register outside the Pulse`() {
        val morning = RegisterChoice.preference(Purpose.REPORT_OBSERVATION, unflattering = false, partOfDay = PartOfDay.MORNING)
        val evening = RegisterChoice.preference(Purpose.REPORT_OBSERVATION, unflattering = false, partOfDay = PartOfDay.EVENING)
        assertEquals(morning, evening)
    }

    @Test
    fun `the editorial register belongs to the Report, to a notable fact, and to a budget`() {
        val notable = RegisterChoice.preference(
            Purpose.REPORT_OBSERVATION, unflattering = false, partOfDay = PartOfDay.MORNING, notable = true,
        )
        assertTrue(Register.EDITORIAL in notable)

        val ordinary = RegisterChoice.preference(
            Purpose.REPORT_OBSERVATION, unflattering = false, partOfDay = PartOfDay.MORNING, notable = false,
        )
        assertFalse(Register.EDITORIAL in ordinary)

        val spent = RegisterChoice.preference(
            Purpose.REPORT_OBSERVATION, unflattering = false, partOfDay = PartOfDay.MORNING,
            notable = true, editorialBudgetSpent = true,
        )
        assertFalse("a third editorial lead is re-realized in another register", Register.EDITORIAL in spent)

        val pulse = RegisterChoice.preference(
            Purpose.PULSE, unflattering = false, partOfDay = PartOfDay.MORNING, notable = true,
        )
        assertFalse("the Pulse corpus authors no editorial line", Register.EDITORIAL in pulse)
    }

    @Test
    fun `every preference ends in the fallback order so a thin bench still speaks`() {
        val order = RegisterChoice.preference(Purpose.PULSE, unflattering = false, partOfDay = PartOfDay.EVENING)
        assertTrue(Register.REFLECTIVE in order)
        assertTrue(Register.OBSERVATIONAL in order)
        assertTrue(Register.PLAIN in order)
        assertEquals("a register offered twice would be a bench searched twice", order.size, order.distinct().size)
    }
}
