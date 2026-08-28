package com.kamsiob.claritynow.domain.engine.realize

import com.kamsiob.claritynow.domain.engine.FiringHistory
import com.kamsiob.claritynow.domain.engine.PartOfDay
import com.kamsiob.claritynow.domain.engine.catalog.CorpusFixture
import com.kamsiob.claritynow.domain.engine.catalog.Purpose
import com.kamsiob.claritynow.domain.engine.catalog.Register
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/**
 * CLARITY_LOGIC_ENGINE.md 7.4, as tiers and a choice rather than as an order.
 *
 * The two halves are tested apart because they fail apart. [RegisterChoice.preference]
 * answers which voices are permitted, which is a rule about content and is the same every
 * day; [RegisterChoice.choose] answers which of them speaks, which is a rotation and is
 * different every day. A single test over both would pass while either one was frozen,
 * which is exactly the defect this section was opened to fix.
 */
class RegisterChoiceTest {

    // ------------------------------------------------------------------ the tiers

    @Test
    fun `an unflattering rule reaches for the neutral agent register first, and alone`() {
        val tiers = RegisterChoice.preference(
            Purpose.REPORT_OBSERVATION, unflattering = true, partOfDay = PartOfDay.MORNING,
        )
        assertEquals(
            "7.4 step 1 is a rule about the content and not a voice among others",
            setOf(Register.NEUTRAL_AGENT),
            tiers.first(),
        )
    }

    @Test
    fun `a neutral or positive family can never reach the neutral agent register`() {
        for (purpose in Purpose.entries) {
            for (band in PartOfDay.entries) {
                val tiers = RegisterChoice.preference(purpose, unflattering = false, partOfDay = band, notable = true)
                assertFalse(
                    "$purpose in $band offered the neutral agent register to a family that is not unflattering",
                    tiers.any { Register.NEUTRAL_AGENT in it },
                )
            }
        }
    }

    @Test
    fun `dawn and midday offer both plainer voices at equal standing, and evening the reflective one`() {
        val plainer = setOf(Register.PLAIN, Register.OBSERVATIONAL)
        for (band in listOf(PartOfDay.MORNING, PartOfDay.AFTERNOON)) {
            val tiers = RegisterChoice.preference(Purpose.PULSE, unflattering = false, partOfDay = band)
            assertEquals(
                "7.4 step 2 names two registers for the first half of the day, not an order",
                plainer,
                tiers.first(),
            )
        }
        for (band in listOf(PartOfDay.EVENING, PartOfDay.NIGHT)) {
            val tiers = RegisterChoice.preference(Purpose.PULSE, unflattering = false, partOfDay = band)
            assertEquals(setOf(Register.REFLECTIVE), tiers.first())
        }
    }

    @Test
    fun `the time of day never decides a register outside the Pulse`() {
        val morning = RegisterChoice.preference(
            Purpose.REPORT_OBSERVATION, unflattering = false, partOfDay = PartOfDay.MORNING,
        )
        val evening = RegisterChoice.preference(
            Purpose.REPORT_OBSERVATION, unflattering = false, partOfDay = PartOfDay.EVENING,
        )
        assertEquals(morning, evening)
    }

    @Test
    fun `the editorial register belongs to the Report, to a notable fact, and to a budget`() {
        val notable = RegisterChoice.preference(
            Purpose.REPORT_OBSERVATION, unflattering = false, partOfDay = PartOfDay.MORNING, notable = true,
        )
        assertEquals("a budgeted voice is offered on its own or not at all", setOf(Register.EDITORIAL), notable.first())

        val ordinary = RegisterChoice.preference(
            Purpose.REPORT_OBSERVATION, unflattering = false, partOfDay = PartOfDay.MORNING, notable = false,
        )
        assertFalse(ordinary.any { Register.EDITORIAL in it })

        val spent = RegisterChoice.preference(
            Purpose.REPORT_OBSERVATION, unflattering = false, partOfDay = PartOfDay.MORNING,
            notable = true, editorialBudgetSpent = true,
        )
        assertFalse("a third editorial lead is re-realized in the open tier", spent.any { Register.EDITORIAL in it })

        val pulse = RegisterChoice.preference(
            Purpose.PULSE, unflattering = false, partOfDay = PartOfDay.MORNING, notable = true,
        )
        assertFalse("the Pulse corpus authors no editorial line", pulse.any { Register.EDITORIAL in it })
    }

    /**
     * The property the whole preference exists to have, per 7.4 and the brief that reopened
     * it: reachability is the fix, and a new way to be silent is the opposite of it.
     */
    @Test
    fun `every preference offers all three open registers, so a thin bench still speaks`() {
        for (purpose in Purpose.entries) {
            for (band in PartOfDay.entries) {
                for (unflattering in listOf(true, false)) {
                    val tiers = RegisterChoice.preference(purpose, unflattering, band, notable = true)
                    val offered = tiers.flatten()
                    assertTrue(
                        "$purpose in $band did not offer every open register",
                        offered.containsAll(RegisterChoice.OPEN),
                    )
                    assertEquals(
                        "a register offered twice would be a bench searched twice",
                        offered.size,
                        offered.distinct().size,
                    )
                }
            }
        }
    }

    // ------------------------------------------------------------------ the choice

    @Test
    fun `nothing offered is nothing chosen, and one register offered is that register`() {
        assertEquals(null, RegisterChoice.choose(emptySet(), null, DATE, "quietDay", 1))
        assertEquals(
            Register.PLAIN,
            RegisterChoice.choose(setOf(Register.PLAIN), null, DATE, "quietDay", 1),
        )
    }

    /**
     * The defect this file exists for, stated as a test: an open tier asked on a run of days
     * has to answer with more than one voice.
     *
     * Ninety days rather than a handful, because a rotation that turned over once a quarter
     * would pass a short window and is exactly what a person feels as one voice.
     */
    @Test
    fun `the open tier is heard in every voice across a season`() {
        val heard = (0L until 90L)
            .map { LocalDate.of(2026, 3, 1).plusDays(it).toString() }
            .mapNotNull { RegisterChoice.choose(RegisterChoice.OPEN, null, it, "weekMixed", 1) }
            .toSet()
        assertEquals("the open tier answered with fewer than three voices", RegisterChoice.OPEN, heard)
    }

    @Test
    fun `the voice a family used last is held back where another remains`() {
        for (last in RegisterChoice.OPEN) {
            val chosen = RegisterChoice.choose(RegisterChoice.OPEN, last, DATE, "weekMixed", 1)
            assertNotNull(chosen)
            assertFalse(
                "the voice heard most recently is the only one a person might recognize",
                chosen == last,
            )
        }
    }

    @Test
    fun `holding back the last voice never empties a tier of one`() {
        assertEquals(
            Register.NEUTRAL_AGENT,
            RegisterChoice.choose(setOf(Register.NEUTRAL_AGENT), Register.NEUTRAL_AGENT, DATE, "weekQuiet", 1),
        )
    }

    /**
     * Two engines reaching the same day by different routes reach the same voice.
     *
     * The set is handed in twice with its iteration order reversed, which is the cheapest
     * available imitation of the risk section 14's determinism test names: a map iteration
     * order leaking into a decision, invisible at three keys and not invisible above that.
     */
    @Test
    fun `the choice does not depend on the order the registers arrive in`() {
        for (day in 1..28) {
            val dateKey = "2026-04-%02d".format(day)
            val forward = RegisterChoice.choose(
                linkedSetOf(Register.REFLECTIVE, Register.OBSERVATIONAL, Register.PLAIN), null, dateKey, "quietDay", 2,
            )
            val backward = RegisterChoice.choose(
                linkedSetOf(Register.PLAIN, Register.OBSERVATIONAL, Register.REFLECTIVE), null, dateKey, "quietDay", 2,
            )
            assertEquals("day $day", forward, backward)
        }
    }

    @Test
    fun `two families on the same day are not made to speak in the same voice`() {
        val voices = CorpusFixture.catalog.families
            .mapNotNull { RegisterChoice.choose(RegisterChoice.OPEN, null, DATE, it.key, 1) }
            .toSet()
        assertTrue(
            "the family key is in the hashed term precisely so the banner and Momentum do " +
                "not move in lockstep",
            voices.size > 1,
        )
    }

    // ------------------------------------------------------------------ the last voice

    @Test
    fun `a family that has never spoken has no last voice`() {
        val family = CorpusFixture.catalog.familiesFor(Purpose.AREAS_BANNER).first { it.key == "weekMixed" }
        assertEquals(null, RegisterChoice.lastSpoken(family.allVariants, FiringHistory.EMPTY))
    }

    @Test
    fun `the last voice is the register of the most recently used line, across every stage`() {
        val family = CorpusFixture.catalog.familiesFor(Purpose.PULSE).first { it.key == "persistence" }
        val older = family.allVariants.first { it.register == Register.PLAIN }
        val newer = family.allVariants.first { it.register == Register.REFLECTIVE }
        val history = FiringHistory.EMPTY.copy(
            variantsUsed = mapOf(older.key to "2026-03-01", newer.key to "2026-03-02"),
        )
        assertEquals(newer.register, RegisterChoice.lastSpoken(family.allVariants, history))
    }

    private companion object {
        const val DATE = "2026-03-17"
    }
}
