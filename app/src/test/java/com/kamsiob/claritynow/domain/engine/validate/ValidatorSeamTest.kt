package com.kamsiob.claritynow.domain.engine.validate

import com.kamsiob.claritynow.domain.engine.FactRef
import com.kamsiob.claritynow.domain.engine.SilenceReason
import com.kamsiob.claritynow.domain.engine.realize.Candidate
import com.kamsiob.claritynow.domain.engine.realize.Slot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.ZoneId

/**
 * What a veto does next. CLARITY_LOGIC_ENGINE.md 8.
 *
 * "A vetoed candidate causes the next ranked selection to be realized. If everything is
 * vetoed, the engine returns `Silent`." Not an exception, not a fallback sentence, and never
 * the unvalidated candidate. Silence is a supported outcome of this app by the second
 * directive in section 1, so falling silent because nothing could be proven true is a
 * correct day rather than a failed one.
 *
 * **The loop itself lives in `ClarityEngine.speak`, and there is deliberately not a second
 * one here.** Layer 5 answers about one candidate; the engine decides what to do about the
 * answer, and it is the only thing that realizes the next selection. What this file pins
 * down is the contract that loop turns on: null to speak, a reason not to, and a reason that
 * is the same on every call. The loop below is the engine's, written out in the test to show
 * what the contract produces, and it is not exported anywhere.
 */
class ValidatorSeamTest {

    private val validator = ClarityValidator(ZoneId.of("UTC"))

    private val facts = ValidateFixture.facts()

    /** Names the one area that did nothing this week. Vetoed by check 1. */
    private fun phantom(): Candidate = ValidateFixture.candidate(
        variantKey = "rebalance.s1.04",
        rendered = "Reading moved again today.",
        renderedQuestion = null,
        slots = mapOf("areaName" to Slot.Text("areaName", "Reading")),
        sourceFacts = emptyMap(),
        namedAreaIds = setOf(ValidateFixture.READING),
        namedItemIds = emptySet(),
    )

    /** Says seven where the fact says five. Vetoed by check 3. */
    private fun invented(): Candidate = ValidateFixture.candidate(
        variantKey = "throughput.s1.02",
        rendered = "Seven things left the queues this week.",
        renderedQuestion = null,
        slots = mapOf("n" to Slot.Count("n", 7, "thing", "things")),
        sourceFacts = mapOf("n" to FactRef("window", "completions")),
        namedItemIds = emptySet(),
    )

    /** The engine's loop, exactly as `ClarityEngine.speak` runs it. */
    private fun speak(ranked: List<Candidate>): Pair<Candidate?, SilenceReason?> {
        var vetoed = false
        for (candidate in ranked) {
            if (validator.veto(candidate, facts) != null) {
                vetoed = true
                continue
            }
            return candidate to null
        }
        return null to
            if (vetoed) SilenceReason.ALL_CANDIDATES_VETOED else SilenceReason.ALL_QUALIFIED_RULES_FILTERED
    }

    @Test
    fun `a vetoed candidate falls through to the next ranked selection`() {
        val good = ValidateFixture.candidate()
        val (spoken, reason) = speak(listOf(phantom(), invented(), good))
        assertEquals(good, spoken)
        assertNull(reason)
    }

    @Test
    fun `everything vetoed is a silence, and the one the engine reports is the vetoed one`() {
        val (spoken, reason) = speak(listOf(phantom(), invented()))
        assertNull(spoken)
        assertEquals(SilenceReason.ALL_CANDIDATES_VETOED, reason)
    }

    /**
     * A veto is a fact about a candidate and a fact set, not a state the validator holds.
     *
     * The engine calls this once per ranked selection and the simulator calls it again over
     * a dumped year. A validator that answered differently the second time would make a
     * simulator run unreproducible, which is the one thing the whole dump is for.
     */
    @Test
    fun `the same candidate and the same facts give the same answer every time`() {
        val good = ValidateFixture.candidate()
        assertNull(validator.veto(good, facts))
        assertNull(validator.veto(good, facts))
        val first = validator.veto(invented(), facts)
        val second = validator.veto(invented(), facts)
        assertNotNull(first)
        assertEquals(first, second)
    }
}
