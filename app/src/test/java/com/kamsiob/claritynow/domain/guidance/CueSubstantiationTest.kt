package com.kamsiob.claritynow.domain.guidance

import com.kamsiob.claritynow.domain.engine.CueFacts
import com.kamsiob.claritynow.domain.engine.FiringHistory
import com.kamsiob.claritynow.domain.engine.PartOfDay
import com.kamsiob.claritynow.domain.engine.StableHash
import com.kamsiob.claritynow.domain.engine.Validated
import com.kamsiob.claritynow.domain.engine.Weekday
import com.kamsiob.claritynow.domain.engine.catalog.CorpusFixture
import com.kamsiob.claritynow.domain.engine.catalog.Purpose
import com.kamsiob.claritynow.domain.engine.validate.ValidateFixture
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Cue substantiation, at scale. CLARITY_LOGIC_ENGINE.md 3.7 and 10.4 rule 1, and the
 * acceptance criterion in issue #8: **no plan renders with a cue below threshold, across
 * ten thousand generated fact sets.**
 *
 * > A cue may be used only if drawn from at least 6 weeks of data, holding in at least 60
 * > percent of those weeks, over an underlying count of at least 8 events. If nothing
 * > clears these, `hasStableRhythm` is false and layer 6 may not produce a plan.
 *
 * > **An invented cue is worse than no plan**, because it makes a claim about the user's
 * > life the user knows to be false.
 *
 * ## Where the three thresholds are, and why this test is where it is
 *
 * The thresholds are applied in `FactExtractor.cueFacts`, which nulls every field that did
 * not clear all three and sets `hasStableRhythm` only when at least one did.
 * `CueFactsTest` drives that from a log and is where the arithmetic is checked.
 *
 * **This is the other half, and it is the half the acceptance criterion asks for**: given a
 * `CueFacts` in any state the extractor can produce, layer 6 never puts an unsubstantiated
 * cue in front of a person. The two halves have to be separate, because a fact set can
 * carry a cue that cleared and a cue that did not at the same time, and a composer that
 * read the wrong field would still pass every test the extractor has.
 *
 * ## The generation
 *
 * Ten thousand fact sets, each with an independently present or absent value for every one
 * of the six cues, driven by `StableHash` so the corpus of fact sets is identical on every
 * run and on every machine. Every one of them is composed against a week that has a real
 * friction pattern in it and would otherwise produce a plan, so a fact set that produces no
 * plan produced none because of its cues.
 */
class CueSubstantiationTest {

    private val composer = GuidanceComposer(CorpusFixture.catalog, ZONE)

    /**
     * The criterion, stated in one assertion: no rendered plan names a cue the person's
     * own twelve weeks did not substantiate.
     *
     * A plan carries the key of the cue it used. Each key names the `CueFacts` field it
     * requires, `CueFacts` nulls a field that did not clear, and the check is that the
     * field behind the key it used was not null. **The plan is checked by its key rather
     * than by its text**, because a weekday's name can appear in a sentence for more than
     * one reason and the question here is which fact the engine reached for.
     */
    @Test
    fun `no plan renders with a cue below threshold across ten thousand fact sets`() {
        val offenders = mutableListOf<String>()
        var planned = 0
        var silent = 0
        for (index in 0 until FACT_SETS) {
            val cues = generate(index)
            val result = composer.compose(
                headline = null,
                appeared = listOf(motivating()),
                facts = ValidateFixture.facts(cues = cues),
                plans = PlanHistory.EMPTY,
                history = FiringHistory.EMPTY,
                weekStartKey = weekOf(index),
            )
            val plan = (result as? GuidanceResult.Plan)?.plan
            if (plan == null) {
                silent++
                continue
            }
            planned++
            if (!cues.hasStableRhythm) {
                offenders += "$index offered a plan with no stable rhythm at all"
                continue
            }
            val required = requirementOf(plan.cueKey)
            if (required != null && !required(cues)) {
                offenders += "$index used ${plan.cueKey}, whose fact did not clear: ${plan.offeredLine}"
            }
        }
        assertTrue("no fact set produced a plan, so nothing was tested", planned > 0)
        assertTrue("every fact set produced a plan, so the gate never fired", silent > 0)
        assertEquals("plans naming an unsubstantiated cue", emptyList<String>(), offenders.take(TO_REPORT))
    }

    /**
     * The gate 3.7 states in as many words: `hasStableRhythm` false means no plan at all.
     *
     * Separate from the sweep above because it is a different statement. The sweep says no
     * plan names a cue that did not clear; this says that when **nothing** cleared, there
     * is no plan whatever the rest of the week looked like, including on a week whose
     * friction is obvious and whose cue banks contain lines with no slots in them at all.
     * `before the week ends` needs no fact to render, and without this it would be
     * reachable on a person the app knows nothing about yet.
     */
    @Test
    fun `no rhythm means no plan even where a cue would need no fact to render`() {
        val result = composer.compose(
            headline = null,
            appeared = listOf(motivating()),
            facts = ValidateFixture.facts(cues = CueFacts.NONE),
            plans = PlanHistory.EMPTY,
            history = FiringHistory.EMPTY,
            weekStartKey = "2026-03-08",
        )
        assertTrue("3.7 forbids a plan when hasStableRhythm is false", result !is GuidanceResult.Plan)
    }

    /**
     * A person whose productive band is not the morning is never told about their morning.
     *
     * Every line in 4.2's part of day bank names the morning, so the bank is true of a
     * morning person and false of everybody else. This is the one cue bank whose gate is
     * about the value of a fact rather than its presence, and `PlanFormTest` holds the
     * other end of it by asserting the bank really is about mornings.
     */
    @Test
    fun `an evening person is never offered a morning cue`() {
        val evening = STABLE.copy(productiveBand = PartOfDay.EVENING)
        val used = (0 until BAND_TRIALS).mapNotNull { index ->
            val result = composer.compose(
                headline = null,
                appeared = listOf(motivating()),
                facts = ValidateFixture.facts(cues = evening),
                plans = PlanHistory.EMPTY,
                history = FiringHistory.EMPTY,
                weekStartKey = weekOf(index),
            )
            (result as? GuidanceResult.Plan)?.plan?.cueKey
        }
        assertTrue("no plan was offered at all, so the band gate was not what was tested", used.isNotEmpty())
        assertEquals(
            "a part of day cue reached somebody whose productive band is the evening",
            emptyList<String>(),
            used.filter { it.startsWith("cue.band") },
        )
    }

    // ----------------------------------------------------------------- the generation

    /**
     * One fact set's cues, from its index alone.
     *
     * Every cue is present or absent independently, which is the state space the extractor
     * can actually produce: 3.7 gates each cue on its own underlying count and its own
     * share of weeks, so a person can have a rock solid busiest day and no productive band.
     * `hasStableRhythm` is then computed the way the extractor computes it, as whether any
     * of them cleared, rather than drawn separately, because a fact set where it disagreed
     * with the six fields is not one the extractor can build and a test of it would be a
     * test of nothing.
     */
    private fun generate(index: Int): CueFacts {
        // Six bits off one avalanched hash rather than six hashes of six salted keys.
        // `StableHash.of` is FNV-1a, whose low bits are close to a function of the last
        // byte alone, and six keys differing only in a trailing word are exactly the input
        // shape `StableHash.spread` exists for. Drawn the other way, the six presences
        // came out correlated hard enough that not one fact set in ten thousand had all
        // six absent, and the case this test most needs to reach is the one where nothing
        // cleared at all.
        val seed = StableHash.spread("cueFacts|$index")
        fun bit(position: Int): Boolean = (seed ushr position) and 1L == 1L
        fun pick(position: Int, size: Int): Int = ((seed ushr position) and 0xFFFFL).toInt() % size
        val strongest = if (bit(0)) Weekday.entries[pick(SPREAD_DAY, DAYS)] else null
        val quietest = if (bit(1)) Weekday.entries[pick(SPREAD_QUIET, DAYS)] else null
        val band = if (bit(2)) PartOfDay.entries[pick(SPREAD_BAND, BANDS)] else null
        val focusDay = if (bit(3)) Weekday.entries[pick(SPREAD_FOCUS_DAY, DAYS)] else null
        val focusBand = if (bit(4)) PartOfDay.entries[pick(SPREAD_FOCUS_BAND, BANDS)] else null
        val adding = if (bit(5)) PartOfDay.entries[pick(SPREAD_ADD, BANDS)] else null
        val any = listOfNotNull(strongest, quietest, band, focusDay, focusBand, adding).isNotEmpty()
        return CueFacts(
            strongestWeekday = strongest,
            strongestWeekdayConfidence = if (strongest == null) 0.0 else CONFIDENCE,
            quietestWeekday = quietest,
            productiveBand = band,
            productiveBandShare = if (band == null) 0.0 else CONFIDENCE,
            focusTypicalWeekday = focusDay,
            focusTypicalBand = focusBand,
            addingBand = adding,
            weekdayOnly = bit(6),
            hasStableRhythm = any,
        )
    }

    /**
     * The `CueFacts` field a cue key needs, or null where it needs none.
     *
     * Read off 4.2, where each bank names its requirement, and off the slots of the lines
     * themselves. The banks with no requirement are the behavioral and boundary ones, whose
     * lines are anchored to the app's own events rather than to a rhythm; those still need
     * `hasStableRhythm`, which the sweep asserts separately.
     */
    private fun requirementOf(cueKey: String): ((CueFacts) -> Boolean)? = when {
        cueKey == "cue.day.05" -> { cues -> cues.strongestWeekday != null }
        cueKey.startsWith("cue.day") -> { cues -> cues.strongestWeekday != null }
        cueKey.startsWith("cue.band") -> { cues -> cues.productiveBand == PartOfDay.MORNING }
        cueKey in QUIET_WEEKDAY_CUES -> { cues -> cues.quietestWeekday != null }
        else -> null
    }

    private fun motivating(): Validated = Validated(
        ValidateFixture.candidate(
            ruleKey = "report.observation.neglectedArea.s2",
            purpose = Purpose.REPORT_OBSERVATION,
            familyKey = "neglectedArea",
            variantKey = "ob.neg.s2.l01",
            rendered = "Reading has been quiet for three weeks.",
            renderedQuestion = null,
            slots = emptyMap(),
            sourceFacts = emptyMap(),
            namedAreaIds = setOf(ValidateFixture.READING),
            namedItemIds = emptySet(),
            subjectId = ValidateFixture.READING,
        ),
    )

    private fun weekOf(index: Int): String = FIRST.plusWeeks((index % WEEK_SPREAD).toLong()).toString()

    private companion object {

        /** The cues that name `quietestWeekday`. Read off 4.2's boundary bank. */
        val QUIET_WEEKDAY_CUES = setOf("cue.bound.02", "cue.bound.05")

        /** Issue #8's own number. */
        const val FACT_SETS = 10_000

        const val BAND_TRIALS = 60

        /** Enough distinct week keys that `VariantChoice` walks the whole bench. */
        const val WEEK_SPREAD = 200

        const val TO_REPORT = 20

        const val DAYS = 7

        const val BANDS = 4

        /** Bit offsets into the avalanched seed. Far enough apart not to overlap. */
        const val SPREAD_DAY = 8
        const val SPREAD_QUIET = 16
        const val SPREAD_BAND = 24
        const val SPREAD_FOCUS_DAY = 32
        const val SPREAD_FOCUS_BAND = 40
        const val SPREAD_ADD = 46

        /** Above `CueFacts.Thresholds.MIN_CONFIDENCE`. A field that is present cleared. */
        const val CONFIDENCE = 0.75

        val ZONE: ZoneId = ZoneId.of("UTC")

        val FIRST: LocalDate = LocalDate.parse("2026-01-04")

        val STABLE = CueFacts(
            strongestWeekday = Weekday.WED,
            strongestWeekdayConfidence = CONFIDENCE,
            quietestWeekday = Weekday.SUN,
            productiveBand = PartOfDay.MORNING,
            productiveBandShare = CONFIDENCE,
            focusTypicalWeekday = Weekday.TUE,
            focusTypicalBand = PartOfDay.MORNING,
            addingBand = PartOfDay.EVENING,
            weekdayOnly = true,
            hasStableRhythm = true,
        )
    }
}
