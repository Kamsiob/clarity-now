package com.kamsiob.claritynow.domain.pulse

import com.kamsiob.claritynow.data.event.ReflectionPeriod
import com.kamsiob.claritynow.data.event.SubjectKind
import com.kamsiob.claritynow.domain.engine.catalog.ClarityCatalog
import com.kamsiob.claritynow.domain.engine.catalog.CorpusFixture
import com.kamsiob.claritynow.domain.engine.catalog.Purpose
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The generation lifecycle, driven against the real corpus and a real log.
 * `MASTER_BUILD_PROMPT.md` 11.3, 11.6, 12.1 and 14b.4, and issue #4.
 *
 * **Nothing here asserts which family fires on which day.** That is the engine's business
 * and phase 5 tests it; pinning it here would make this file fail every time a corpus line
 * was authored, which is what phase 9 does for a living. What it asserts is everything
 * that has to be true of **whatever** the engine said: one entry per local day, never
 * regenerated, two answers except for `quietDay`, a subject kind that matches the family's
 * declared subject, and a payload a later reader can resolve back through the catalog.
 *
 * The fixture in [PulseMonth] is a month of ordinary use rather than one engineered day,
 * for the same reason. A test that arranges exactly one qualifying rule proves that rule
 * and nothing about the lifecycle.
 */
class PulseGenerationTest {

    private val catalog: ClarityCatalog = CorpusFixture.catalog

    // What has to be true of whatever was said -------------------------------

    @Test
    fun `a month of ordinary use produces at least one observation`() {
        val month = PulseMonth().run()

        assertTrue(
            "a month with two areas, a daily capture and ten completions produced no Pulse " +
                "at all, which means the sequence never reached the engine",
            month.spoken.isNotEmpty(),
        )
    }

    @Test
    fun `at most one entry per local day`() {
        val keys = PulseMonth().run().spoken.map { it.payload.dateKey }

        assertEquals("one Pulse per calendar day, keyed by its own dateKey", keys.distinct(), keys)
        keys.forEach { key ->
            assertTrue("a date key that is not yyyy-MM-dd: $key", DATE_KEY.matches(key))
        }
    }

    @Test
    fun `an entry that exists is never regenerated`() {
        val month = PulseMonth().run()

        assertTrue("the month said nothing, so this asserts nothing", month.spoken.isNotEmpty())
        month.secondCallsSameDay.forEach { decision ->
            assertTrue(
                "asking again the same day, after the reflection switch, produced $decision " +
                    "rather than stopping at the entry that already exists",
                decision is PulseDecision.AlreadyWritten,
            )
        }
    }

    @Test
    fun `the reflection period is decided once and stored`() {
        val month = PulseMonth().run()
        val state = month.state()

        month.spoken.forEach { speak ->
            val entry = state.pulses[speak.payload.dateKey]
            assertNotNull("the projection lost the Pulse for ${speak.payload.dateKey}", entry)
            assertEquals(
                "every generation ran at 08:00, so every entry describes yesterday, and the " +
                    "second call at 18:00 must not have rewritten it",
                ReflectionPeriod.YESTERDAY,
                entry?.reflectionPeriod,
            )
        }
    }

    @Test
    fun `every payload carries what the firing history is rebuilt from`() {
        PulseMonth().run().spoken.forEach { speak ->
            val payload = speak.payload
            assertTrue("a blank family on ${payload.dateKey}", payload.family.isNotBlank())
            assertTrue("a blank variant key on ${payload.dateKey}", payload.variantKey.isNotBlank())
            assertTrue("a blank register on ${payload.dateKey}", payload.register.isNotBlank())
            assertTrue("a stage below one on ${payload.dateKey}", payload.escalationStage >= 1)
            assertTrue(
                "a blank observation on ${payload.dateKey}",
                payload.renderedObservation.isNotBlank(),
            )
            assertTrue(
                "the fact snapshot on ${payload.dateKey} is empty, so nothing could be " +
                    "checked against the sentence later",
                payload.factSnapshot.isNotEmpty(),
            )
            val family = catalog.familiesFor(Purpose.PULSE).firstOrNull { it.key == payload.family }
            assertNotNull("a family the catalog does not have: ${payload.family}", family)
            val stage = family?.stage(payload.escalationStage)
            assertNotNull("a stage the family does not have: ${payload.variantKey}", stage)
            assertTrue(
                "the variant ${payload.variantKey} does not belong to the stage it was " +
                    "recorded against",
                stage?.variants.orEmpty().any { it.key == payload.variantKey },
            )
        }
    }

    @Test
    fun `two answers always, except quietDay which has three`() {
        PulseMonth().run().spoken.forEach { speak ->
            val expected = if (speak.payload.family == QUIET_DAY) 3 else 2
            assertEquals(
                "CLARITY_LOGIC_ENGINE.md 6.2 settles the format at two, except quietDay. " +
                    "${speak.payload.family} offered ${speak.responses.map { it.label }}",
                expected,
                speak.responses.size,
            )
            assertTrue(
                "the first option is the positive one, per 6.1",
                speak.responses.first().isPositive,
            )
            assertFalse(
                "the last option is the flagged one, per 6.1",
                speak.responses.last().isPositive,
            )
        }
    }

    @Test
    fun `the subject kind matches the family's declared subject`() {
        PulseMonth().run().spoken.forEach { speak ->
            val payload = speak.payload
            when (payload.family) {
                in ITEM_SUBJECT_FAMILIES -> {
                    assertNotNull(
                        "${payload.family} speaks about an item and recorded no subject",
                        payload.subjectId,
                    )
                    assertEquals(SubjectKind.ITEM, payload.subjectKind)
                }

                in AREA_SUBJECT_FAMILIES -> {
                    assertNotNull(
                        "${payload.family} speaks about an area and recorded no subject",
                        payload.subjectId,
                    )
                    assertEquals(SubjectKind.AREA, payload.subjectKind)
                }

                in NO_SUBJECT_FAMILIES -> {
                    assertNull("${payload.family} has no subject in 6.1", payload.subjectId)
                    assertNull("a subject kind with no subject", payload.subjectKind)
                }

                else -> throw AssertionError(
                    "${payload.family} is not in this test's table of subjects. The table comes " +
                        "from CLARITY_LOGIC_ENGINE.md 6.1 and PulseRules, and a family added " +
                        "there belongs in one of the three sets above rather than in none",
                )
            }
        }
    }

    @Test
    fun `an entry already in the log stops the sequence`() {
        val month = PulseMonth().run()
        assertTrue("the month said nothing, so this asserts nothing", month.spoken.isNotEmpty())
        val key = month.spoken.first().payload.dateKey

        val decision = month.decide(month.dayOf(key))

        assertTrue(
            "the log already holds a Pulse for $key, so the only legal answer is to display " +
                "it. Got $decision",
            decision is PulseDecision.AlreadyWritten,
        )
    }

    // The outcomes that write nothing -----------------------------------------

    @Test
    fun `the first foreground ever says nothing and writes nothing`() {
        // The real shape of a first run: the presence marker is written before generation,
        // so the log holds one APP_OPENED and nothing else.
        val decision = PulseMonth().opened(0).decide(0)

        assertTrue(
            "there is nothing in the log to describe, so there is nothing to say and no " +
                "entry to write. Got $decision",
            decision is PulseDecision.Silent,
        )
    }

    @Test
    fun `nothing is generated for the first two days after a return`() {
        val month = PulseMonth().seed().opened(0).opened(20)

        assertTrue(
            "the day of the return is the first of the two days 14b.4 suppresses",
            month.decide(20) is PulseDecision.SuppressedAfterReturn,
        )

        month.opened(21)
        assertTrue(
            "the day after the return is the second",
            month.decide(21) is PulseDecision.SuppressedAfterReturn,
        )
    }

    @Test
    fun `the suppression lifts on the third day back`() {
        val month = PulseMonth().seed().opened(0).opened(20).opened(21).opened(22)

        val decision = month.decide(22)

        assertFalse(
            "two days back is two days. The third is an ordinary day, which the engine may " +
                "still be silent on for its own reasons. Got $decision",
            decision is PulseDecision.SuppressedAfterReturn,
        )
    }

    @Test
    fun `a shorter absence is not a return and suppresses nothing`() {
        val month = PulseMonth().seed().opened(0).opened(13)

        val decision = month.decide(13)

        assertFalse(
            "thirteen days is under ReEntry.MIN_GAP_DAYS, so nothing about this day is " +
                "special. Got $decision",
            decision is PulseDecision.SuppressedAfterReturn,
        )
    }

    private companion object {

        val DATE_KEY = Regex("""^\d{4}-\d{2}-\d{2}$""")

        const val QUIET_DAY = "quietDay"

        /** CLARITY_LOGIC_ENGINE.md 6.1 and `PulseRules`. The families whose subject is an item. */
        val ITEM_SUBJECT_FAMILIES = setOf("persistence")

        /** The families whose subject is an area. */
        val AREA_SUBJECT_FAMILIES = setOf(
            "concentration",
            "burst",
            "queueDrain",
            "freshStart",
            "rebalance",
            "switching",
        )

        /** The families that speak about the window rather than about a thing in it. */
        val NO_SUBJECT_FAMILIES = setOf("accumulation", "throughput", QUIET_DAY, "spread")
    }
}
