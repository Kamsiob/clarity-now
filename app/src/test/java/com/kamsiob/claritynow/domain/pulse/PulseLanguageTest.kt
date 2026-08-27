package com.kamsiob.claritynow.domain.pulse

import com.kamsiob.claritynow.domain.engine.catalog.ClarityCatalog
import com.kamsiob.claritynow.domain.engine.catalog.CorpusFixture
import com.kamsiob.claritynow.domain.engine.catalog.Purpose
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The two benches a stored entry needs and the event does not carry.
 * `MASTER_BUILD_PROMPT.md` 11.6, `CLARITY_LOGIC_ENGINE.md` 6.2, `CORPUS_1_PULSE.md`.
 *
 * **The case this protects is the ordinary one.** A Pulse is generated at breakfast, the
 * process is killed, the reminder arrives at eight in the evening and the sheet opens
 * from a cold start. If the answers it offers are not the answers it offered this
 * morning, the person is answering a different question from the one they read, and the
 * stored label that a callback will quote months later is the label of a pair they never
 * saw.
 */
class PulseLanguageTest {

    private val catalog: ClarityCatalog = CorpusFixture.catalog

    private val language = PulseLanguage(catalog)

    private val pairs = catalog.familiesFor(Purpose.PULSE)
        .flatMap { family -> family.stages.map { family.key to it } }

    @Test
    fun `every option key carries the key of the pair it belongs to`() {
        pairs.flatMap { it.second.responsePairs }.forEach { pair ->
            pair.options.forEach { option ->
                assertEquals(
                    "PulseGenerator reads the pair key back off an option key, so the two " +
                        "have to be related the way CorpusParser builds them",
                    pair.key,
                    option.key.substringBeforeLast('.'),
                )
            }
        }
    }

    @Test
    fun `two options everywhere, three in quietDay, and no universal third`() {
        pairs.forEach { (familyKey, stage) ->
            val expected = if (familyKey == "quietDay") 3 else 2
            stage.responsePairs.forEach { pair ->
                assertEquals(
                    "CLARITY_LOGIC_ENGINE.md 6.2 settles the format at two, except quietDay, " +
                        "and 11.6 forbids a universal third. ${pair.key} has ${pair.options.size}",
                    expected,
                    pair.options.size,
                )
            }
        }
    }

    @Test
    fun `a stored entry recovers the answers it was generated with`() {
        val month = PulseMonth().run()
        val state = month.state()
        assertTrue("the month said nothing, so this asserts nothing", month.spoken.isNotEmpty())

        month.spoken.forEach { speak ->
            val entry = state.pulses[speak.payload.dateKey]
            assertNotNull("the projection lost the Pulse for ${speak.payload.dateKey}", entry)
            assertEquals(
                "the sheet reopened from the log must offer the pills it offered this morning",
                speak.responses,
                entry?.let { language.responsesFor(it) },
            )
        }
    }

    @Test
    fun `the recorded pair key is what the recovery reads`() {
        val month = PulseMonth().run()
        assertTrue("the month said nothing, so this asserts nothing", month.spoken.isNotEmpty())

        month.spoken.forEach { speak ->
            val recorded = speak.payload.factSnapshot[PulseGenerator.RESPONSE_PAIR_KEY]
            assertNotNull(
                "the pair the person was shown was not recorded on the event, so the only " +
                    "way back to it is to choose again",
                recorded,
            )
            assertTrue(
                "the recorded key $recorded is not a pair in the corpus",
                pairs.flatMap { it.second.responsePairs }.any { it.key == recorded },
            )
        }
    }

    /**
     * The fallback, and the claim it rests on.
     *
     * `VariantChoice` excludes lines used within ninety days out of `FiringHistory`, which
     * only ever holds the statement variants the log records. No response pair key has ever
     * been written to it, so choosing again from an empty history reaches the same pair the
     * realizer reached from a full one. This asserts that rather than trusting it: the day
     * somebody starts recording pair keys, the two stop agreeing and this fails.
     */
    @Test
    fun `an entry with no recorded pair key still recovers the same answers`() {
        val month = PulseMonth().run()
        val state = month.state()
        assertTrue("the month said nothing, so this asserts nothing", month.spoken.isNotEmpty())

        month.spoken.forEach { speak ->
            val entry = state.pulses.getValue(speak.payload.dateKey)
            val stripped = entry.copy(
                factSnapshot = entry.factSnapshot - PulseGenerator.RESPONSE_PAIR_KEY,
            )
            assertEquals(
                "choosing again from the corpus reached a different pair from the one the " +
                    "realizer chose on ${entry.dateKey}",
                speak.responses,
                language.responsesFor(stripped),
            )
        }
    }

    // The acknowledgment ------------------------------------------------------

    @Test
    fun `the acknowledgment bench is where the corpus puts it`() {
        val bench = catalog.auxiliary[PulseLanguage.ACKNOWLEDGMENTS].orEmpty()

        assertTrue(
            "CORPUS_1_PULSE.md carries an `Acknowledgment lines` bench of twelve, and this " +
                "is the only reader of it. An empty bench here means the heading moved and " +
                "the sheet would settle to ambient with nothing in between",
            bench.isNotEmpty(),
        )
    }

    @Test
    fun `the acknowledgment is one line from that bench, and the same one all day`() {
        val bench = catalog.auxiliary[PulseLanguage.ACKNOWLEDGMENTS].orEmpty()
        val key = "2026-01-05"

        val line = language.acknowledgmentFor(key)

        assertNotNull("no acknowledgment for $key", line)
        assertTrue("$line is not a line in the bench", bench.any { it.text == line })
        assertEquals(
            "the selection is a hash of the date key, so it does not change between the " +
                "answer and the sheet settling",
            line,
            language.acknowledgmentFor(key),
        )
    }
}
