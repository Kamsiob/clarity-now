package com.kamsiob.claritynow.domain.report

import com.kamsiob.claritynow.domain.engine.AreaFacts
import com.kamsiob.claritynow.domain.engine.FactSet
import com.kamsiob.claritynow.domain.engine.FamilyKey
import com.kamsiob.claritynow.domain.engine.catalog.Register
import com.kamsiob.claritynow.domain.engine.realize.Candidate

/**
 * Everything section 9 says must be true of a finished report, restated here from the
 * specification rather than read out of the code that enforces it.
 *
 * **The duplication is the point.** `ReportIntegrity` holds one encoding of the
 * incompatibility matrix and it calls the selector's, which is the right arrangement for
 * production code: one matrix, one place to change it. A test that checked the matrix by
 * calling that same object would prove only that the object agrees with itself. So 9.1's
 * fifteen rows are written out again below, by hand, from the document, and a report that
 * satisfies the code and violates this list is a defect in one of the two.
 *
 * [violations] answers a list rather than throwing, so a run over ten thousand generated
 * weeks reports how many reports were wrong and in how many ways, rather than stopping at
 * the first one and hiding the shape of the failure.
 */
internal object ReportInvariants {

    /**
     * 9.1, the pairs that may not both appear, as unordered family pairs.
     *
     * Three rows of the table are not here and each is enforced somewhere a per report
     * check cannot see. `intakeVsOutput` stage 2 against stage 3 is a family against itself,
     * covered by the distinct family rule below. `selfReportVsData` against itself on one
     * subject ever is a cooldown across reports. `hardStretch` against any plan is layer 6.
     */
    private val PAIRS: Set<Set<FamilyKey>> = setOf(
        setOf("singleFocus", "areaBalance"),
        setOf("quietWeek", "personalBest"),
        setOf("quietWeek", "focusInvestment"),
        setOf("steadyPace", "personalBest"),
        setOf("steadyPace", "mostActiveSince"),
        setOf("selfReportVsData", "neglectedArea"),
        setOf("hardStretch", "selfReportVsData"),
    )

    /** 9.1. The same area cannot be both neglected and revived. */
    private val SAME_SUBJECT_PAIRS: Set<Set<FamilyKey>> = setOf(
        setOf("neglectedArea", "areaRevival"),
    )

    /** 9.1. What a `quietWeek` headline leaves room for. */
    private val QUIET_WEEK_ALLOWS: Set<FamilyKey> =
        setOf("quietWeek", "neglectedArea", "persistentItem", "hardStretch")

    /** 9.1's `a declining headline`, for the `selfReportVsData` pile-on row. */
    private val DECLINING_HEADLINES: Set<FamilyKey> = setOf("decliningActivity", "quietWeek")

    /** Everything wrong with [report], or an empty list. */
    fun violations(report: ClarityReport, facts: FactSet): List<String> {
        val out = mutableListOf<String>()
        val observations = report.observations.map { it.candidate }
        val families = observations.map { it.familyKey }

        // Section 5. Two to four, and the pass never takes two rules of one family.
        if (observations.size > MAX_OBSERVATIONS) out += "${observations.size} observations"
        if (families.size != families.toSet().size) out += "two observations share a family: $families"

        // 9.1, pair by pair.
        for (i in observations.indices) {
            for (j in i + 1 until observations.size) {
                val pair = setOf(families[i], families[j])
                if (pair.size == 2 && pair in PAIRS) out += "incompatible pair $pair"
                if (pair.size == 2 && pair in SAME_SUBJECT_PAIRS &&
                    observations[i].subjectId != null &&
                    observations[i].subjectId == observations[j].subjectId
                ) {
                    out += "incompatible pair $pair on one subject"
                }
            }
        }

        // 9.2, the headline set the frame.
        val headline = report.headline?.familyKey
        if (headline != null) {
            for (family in families) {
                if (headline == "quietWeek" && family !in QUIET_WEEK_ALLOWS) {
                    out += "a quietWeek headline with a $family observation"
                }
                if (headline == "singleFocus" && family == "areaBalance") out += "singleFocus headline with areaBalance"
                if (headline == "balanced" && family == "singleFocus") out += "balanced headline with singleFocus"
                if (headline in DECLINING_HEADLINES && family == "selfReportVsData") {
                    out += "selfReportVsData under a $headline headline"
                }
            }
        }

        // 9.2, one area, two mentions.
        val mentions = observations.flatMap { it.namedAreaIds }.groupingBy { it }.eachCount()
        mentions.filter { it.value > MAX_AREA_MENTIONS }.forEach { (areaId, count) ->
            out += "area $areaId named in $count observations"
        }

        // 7.4 step 3.
        val editorial = observations.count { it.register == Register.EDITORIAL }
        if (editorial > EDITORIAL_BUDGET) out += "$editorial editorial leads"

        // 7.4b. Three parallel numeric leads in a row.
        var run = 0
        for (candidate in observations) {
            run = if (isParallelNumeric(candidate)) run + 1 else 0
            if (run > MAX_PARALLEL_CLAUSES) out += "three parallel numeric leads in a row"
        }

        // 6.3. A pattern needs three weeks behind it, and no trend means no section.
        if (report.pattern != null && facts.history.weeksOfData < PATTERN_WEEKS) {
            out += "a pattern with ${facts.history.weeksOfData} weeks of data"
        }

        // design-v3.md 11.1 item 6. A sidehead is never drawn twice.
        val sections = report.observations.map { it.section }
        val runs = sections.fold(emptyList<ReportSection>()) { seen, section ->
            if (seen.lastOrNull() == section) seen else seen + section
        }
        if (runs != runs.distinct()) out += "a section is drawn twice: $sections"

        // 9.2. One fact, one number.
        val seen = mutableMapOf<String, Int>()
        for (line in report.lines) {
            for (slot in line.candidate.slots.values) {
                val value = slot.numericValue ?: continue
                val ref = line.candidate.sourceFacts[slot.key]?.toString() ?: continue
                val earlier = seen.put(ref, value)
                if (earlier != null && earlier != value) out += "$ref rendered as $earlier and as $value"
            }
        }

        // 12.3. Nothing is named that the window did not carry, and the one narrowing.
        for (line in report.lines) {
            for (areaId in line.candidate.namedAreaIds) {
                val area = facts.areas[areaId]
                if (area == null) {
                    out += "${line.candidate.familyKey} names area $areaId, which the window does not carry"
                    continue
                }
                if (area.eventsInWindow > 0) continue
                phantomRefusal(line.candidate.familyKey, area)?.let { out += it }
            }
        }

        // 12.3. A callback insight needs three answered pulses behind it.
        if (facts.pulse.answeredInWindow < INTENT_QUALIFIED_ANSWERS) {
            families.filter { it in ReportSection.CALLBACK_FAMILIES }.forEach {
                out += "$it with ${facts.pulse.answeredInWindow} answered pulses"
            }
        }

        return out
    }

    /**
     * Why [family] may not name [area], which had no events in the window, or null when it may.
     *
     * **The narrowing of check 1, restated by hand from `MASTER_BUILD_PROMPT.md` 12.3 and
     * `AbsenceSubject`'s four conditions, for the reason the class comment gives.** A
     * family whose subject **is** the absence has to name an area with no events in the
     * week, and `neglectedArea` and `areaGoneQuiet` are the two the catalog allows,
     * asserted by name in `RuleCatalogTest`. Without this, every one of them read as a
     * fabricated claim here while `ReportIntegrity` and `ClarityValidator` both passed it,
     * and the disagreement was the defect: 358 of these over ten thousand generated weeks
     * and 112 across the eleven persona years, none of them a phantom area.
     *
     * **The three remaining conditions are what keep the phantom out, and they are
     * restated with their literals rather than read from `FactExtractor`.** That is the
     * whole point of a second encoding: a floor that moved in production would move here
     * silently if this read the same constant, and the failure that hides is an area
     * nobody has used being named as though it had been left alone.
     */
    private fun phantomRefusal(family: FamilyKey, area: AreaFacts): String? = when {
        family !in ABSENCE_SUBJECT_FAMILIES ->
            "$family names area ${area.areaId} with no events, and only " +
                "$ABSENCE_SUBJECT_FAMILIES may"

        area.lifetimeEvents < ABSENCE_LIFETIME_EVENTS ->
            "$family names area ${area.areaId}, which has ${area.lifetimeEvents} events in " +
                "its whole life against the $ABSENCE_LIFETIME_EVENTS an absence needs to be " +
                "an absence from"

        area.isNew ->
            "$family names area ${area.areaId}, which is ${area.ageDays} days old, and an " +
                "area nobody has had time to use is not an area anybody left alone"

        area.daysSinceLastEvent == Int.MAX_VALUE ->
            "$family names area ${area.areaId}, which has never had an event, so its " +
                "silence is a sentinel rather than a measured one"

        else -> null
    }

    private fun isParallelNumeric(candidate: Candidate): Boolean =
        candidate.slots.values.count { it.numericValue != null } >= 2

    /**
     * 12.3's narrowing, as families. `neglectedArea` speaks about seven and fourteen days
     * of silence and `areaGoneQuiet` about three weeks of it, and both name the area
     * precisely because it did nothing. `familiarDip` is the third and it is the second
     * branch of the same silence: 14b.9 removes the other two from the ranking when the
     * area's quiet has a precedent, and this is what speaks in their place, about the same
     * area over the same window. `areaRevival` carries the same flag on its rule and is
     * deliberately absent: its own criteria require the area to have moved again inside
     * the window, so a revival naming a silent area is a defect this list should catch.
     */
    private val ABSENCE_SUBJECT_FAMILIES: Set<FamilyKey> =
        setOf("neglectedArea", "areaGoneQuiet", "familiarDip")

    /** `RollupFacts.neglectedAreaIds`'s own floor, written out rather than imported. */
    private const val ABSENCE_LIFETIME_EVENTS = 5

    private const val MAX_OBSERVATIONS = 4
    private const val MAX_AREA_MENTIONS = 2
    private const val EDITORIAL_BUDGET = 2
    private const val MAX_PARALLEL_CLAUSES = 2
    private const val PATTERN_WEEKS = 3
    private const val INTENT_QUALIFIED_ANSWERS = 3
}
