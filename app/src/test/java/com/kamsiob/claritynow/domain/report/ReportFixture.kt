package com.kamsiob.claritynow.domain.report

import com.kamsiob.claritynow.domain.engine.FactRef
import com.kamsiob.claritynow.domain.engine.FactSet
import com.kamsiob.claritynow.domain.engine.FamilyKey
import com.kamsiob.claritynow.domain.engine.VariantKey
import com.kamsiob.claritynow.domain.engine.catalog.LengthBand
import com.kamsiob.claritynow.domain.engine.catalog.Purpose
import com.kamsiob.claritynow.domain.engine.catalog.Register
import com.kamsiob.claritynow.domain.engine.catalog.SlotKey
import com.kamsiob.claritynow.domain.engine.realize.Candidate
import com.kamsiob.claritynow.domain.engine.realize.Slot
import com.kamsiob.claritynow.domain.engine.validate.ValidateFixture
import java.time.ZoneId

/**
 * The week the report tests are written against, and the lines that describe it.
 *
 * **The week is `ValidateFixture`'s**, not a second one. Layer 5's tests already built a
 * consistent week with three areas of which one is idle, an item active nine days, a queue
 * that shrank and two answered Pulses, and the report scope checks are the same checks at a
 * larger scale. A second fixture would be a second week for the same facts to be true of,
 * and the first thing that would happen is the two drifting apart.
 *
 * Every [FactRef] below is a real address from the `Measures` table and re-reads to the
 * number the sentence states, so a candidate built here passes report check 4 by being
 * re-read rather than by being unreadable in a way the check tolerates. The numbers are
 * `ValidateFixture`'s: twelve events, five completions, three additions, nine of them in
 * Work, an item nine days old.
 */
internal object ReportFixture {

    val ZONE: ZoneId = ZoneId.of("UTC")

    /** A Sunday, and the day the fixture reports are generated on. */
    const val DATE_KEY: String = "2026-03-15"

    /** The Sunday seven days before it, which is the first of the seven days described. */
    const val WEEK_START_KEY: String = "2026-03-08"

    val WORK: String = ValidateFixture.WORK
    val HEALTH: String = ValidateFixture.HEALTH
    val READING: String = ValidateFixture.READING

    fun facts(): FactSet = ValidateFixture.facts()

    // Real addresses, and the value each one re-reads to against `facts()`.

    /** Twelve. */
    val TOTAL_EVENTS = FactRef("window", "totalEvents")

    /** Five. */
    val COMPLETIONS = FactRef("window", "completions")

    /** Three. */
    val ADDITIONS = FactRef("window", "additions")

    /** Nine. */
    val WORK_EVENTS = FactRef("area", "areaEvents:${ValidateFixture.WORK}")

    /** Four. */
    val WORK_COMPLETIONS = FactRef("area", "areaCompletions:${ValidateFixture.WORK}")

    /** Three. */
    val HEALTH_EVENTS = FactRef("area", "areaEvents:${ValidateFixture.HEALTH}")

    /** Nine. */
    val ITEM_AGE = FactRef("item", "itemAgeDays:${ValidateFixture.ACTIVE_ITEM}")

    /** Six: two behind Work, one behind Health, three behind Reading. */
    val QUEUE_TOTAL = FactRef("rollup", "queueTotal")

    /** Eight, which is what those three held when the window opened. */
    val QUEUE_AT_START = FactRef("rollup", "queueTotalAtStart")

    /**
     * One realized observation, true of [facts] unless a test deliberately makes it false.
     *
     * [ruleKey] defaults to the real catalog key for the family, because the report scope
     * matrix check turns a candidate back into a selection by looking its rule up, and a
     * made up key would make that check pass by finding nothing.
     */
    fun observation(
        family: FamilyKey,
        rendered: String,
        variantKey: VariantKey = "ob.$family.l01",
        ruleKey: String = "report.observation.$family",
        register: Register = Register.OBSERVATIONAL,
        lengthBand: LengthBand = LengthBand.MEDIUM,
        stage: Int = 1,
        slots: Map<SlotKey, Slot> = emptyMap(),
        sourceFacts: Map<SlotKey, FactRef> = emptyMap(),
        namedAreaIds: Set<String> = emptySet(),
        namedItemIds: Set<String> = emptySet(),
        subjectId: String? = null,
    ): Candidate = Candidate(
        ruleKey = ruleKey,
        familyKey = family,
        variantKey = variantKey,
        purpose = Purpose.REPORT_OBSERVATION,
        stage = stage,
        register = register,
        lengthBand = lengthBand,
        rendered = rendered,
        renderedQuestion = null,
        slots = slots,
        sourceFacts = sourceFacts,
        namedAreaIds = namedAreaIds,
        namedItemIds = namedItemIds,
        subjectId = subjectId,
    )

    /** `Work held 9 of the 12 events.` Two numbers, so it is a parallel numeric lead. */
    fun workShare(
        variantKey: VariantKey = "ob.single.s1.l01",
        lengthBand: LengthBand = LengthBand.MEDIUM,
    ): Candidate = observation(
        family = "singleFocus",
        ruleKey = "report.observation.singleFocus.s1",
        variantKey = variantKey,
        lengthBand = lengthBand,
        rendered = "Work held 9 of the 12 events.",
        slots = mapOf(
            "n" to Slot.Count("n", 9, "event", "events"),
            "m" to Slot.Count("m", 12, "event", "events"),
        ),
        sourceFacts = mapOf("n" to WORK_EVENTS, "m" to TOTAL_EVENTS),
        namedAreaIds = setOf(ValidateFixture.WORK),
        subjectId = ValidateFixture.WORK,
    )

    /** `You finished 5 things and added 3.` Two numbers. */
    fun flow(
        variantKey: VariantKey = "ob.flow.s3.l01",
        lengthBand: LengthBand = LengthBand.MEDIUM,
    ): Candidate = observation(
        family = "intakeVsOutput",
        ruleKey = "report.observation.intakeVsOutput.s3",
        variantKey = variantKey,
        lengthBand = lengthBand,
        stage = 3,
        rendered = "You finished 5 things and added 3.",
        slots = mapOf(
            "n" to Slot.Count("n", 5, "thing", "things"),
            "m" to Slot.Count("m", 3, "thing", "things"),
        ),
        sourceFacts = mapOf("n" to COMPLETIONS, "m" to ADDITIONS),
    )

    /** `Health had 3 events.` One number, so it is not a parallel numeric lead. */
    fun healthEvents(
        variantKey: VariantKey = "ob.bal.l01",
        lengthBand: LengthBand = LengthBand.SHORT,
    ): Candidate = observation(
        family = "areaBalance",
        ruleKey = "report.observation.areaBalance",
        variantKey = variantKey,
        lengthBand = lengthBand,
        rendered = "Health had 3 events.",
        slots = mapOf("n" to Slot.Count("n", 3, "event", "events")),
        sourceFacts = mapOf("n" to HEALTH_EVENTS),
        namedAreaIds = setOf(ValidateFixture.HEALTH),
        subjectId = ValidateFixture.HEALTH,
    )

    /** `Rewrite the proposal intro has been active nine days.` One number, one item. */
    fun persistentItem(
        variantKey: VariantKey = "ob.pers.l01",
        lengthBand: LengthBand = LengthBand.LONG,
    ): Candidate = observation(
        family = "persistentItem",
        ruleKey = "report.observation.persistentItem.low",
        variantKey = variantKey,
        lengthBand = lengthBand,
        rendered = "${ValidateFixture.ITEM_TITLE} has been the active item for nine days.",
        slots = mapOf("ageDays" to Slot.Days("ageDays", 9)),
        sourceFacts = mapOf("ageDays" to ITEM_AGE),
        namedItemIds = setOf(ValidateFixture.ACTIVE_ITEM),
        subjectId = ValidateFixture.ACTIVE_ITEM,
    )

    /** A focus observation, so a test can make the sections interleave. No numbers. */
    fun focus(
        variantKey: VariantKey = "ob.focus.s1.l01",
        lengthBand: LengthBand = LengthBand.SHORT,
    ): Candidate = observation(
        family = "focusInvestment",
        ruleKey = "report.observation.focusInvestment.s1",
        variantKey = variantKey,
        lengthBand = lengthBand,
        rendered = "You sat down with one thing at a time.",
    )

    /** `The queues hold 6 things, where they held 8.` Two numbers. */
    fun queues(
        variantKey: VariantKey = "ob.qp.l01",
        lengthBand: LengthBand = LengthBand.MEDIUM,
    ): Candidate = observation(
        family = "queuePressure",
        ruleKey = "report.observation.queuePressure",
        variantKey = variantKey,
        lengthBand = lengthBand,
        rendered = "The queues hold 6 things, where they held 8.",
        slots = mapOf(
            "n" to Slot.Count("n", 6, "thing", "things"),
            "m" to Slot.Count("m", 8, "thing", "things"),
        ),
        sourceFacts = mapOf("n" to QUEUE_TOTAL, "m" to QUEUE_AT_START),
    )

    /** A callback observation. The intent gate is about this family and one other. */
    fun completionSplit(variantKey: VariantKey = "ob.split.l01"): Candidate = observation(
        family = "completionSplit",
        ruleKey = "report.observation.completionSplit",
        variantKey = variantKey,
        rendered = "Most of what closed this week was in Work.",
        namedAreaIds = setOf(ValidateFixture.WORK),
    )

    /** The headline, `Work carried the week.` */
    fun headline(family: FamilyKey = "singleFocus", rendered: String = "Work carried the week."): Candidate =
        observation(
            family = family,
            ruleKey = "report.headline.$family",
            variantKey = "hd.$family.01",
            rendered = rendered,
            namedAreaIds = if ("Work" in rendered) setOf(ValidateFixture.WORK) else emptySet(),
        ).copy(purpose = Purpose.REPORT_HEADLINE)

    /** A pattern line. Real key, so the matrix can look it up. */
    fun pattern(family: FamilyKey = "consistentRhythm"): Candidate = observation(
        family = family,
        ruleKey = "report.pattern.$family",
        variantKey = "pt.rhy.01",
        rendered = "The last four weeks have sat inside a narrow band.",
    ).copy(purpose = Purpose.REPORT_PATTERN)
}
