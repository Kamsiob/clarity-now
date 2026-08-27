package com.kamsiob.claritynow.ui.report

import androidx.compose.runtime.Immutable
import com.kamsiob.claritynow.domain.engine.realize.Candidate
import com.kamsiob.claritynow.domain.report.ClarityReport
import com.kamsiob.claritynow.domain.report.ReportObservation
import com.kamsiob.claritynow.domain.report.ReportSection

/**
 * The observations in reading order, grouped under the sidehead each is read below.
 *
 * The composer has already decided both the order and the grouping, per
 * `ReportComposer.arrange`, so this walks the list and starts a new group when the section
 * changes rather than re-sorting anything. A screen that grouped for itself could put two
 * mentions of one area side by side after the composer had spread them, and would break the
 * length band rhythm `CORPUS_2_REPORT.md` 7.5 applies over the reading order.
 */
internal fun groupedSections(
    observations: List<ReportObservation>,
): List<Pair<ReportSection, List<Candidate>>> {
    val groups = mutableListOf<Pair<ReportSection, MutableList<Candidate>>>()
    for (observation in observations) {
        val last = groups.lastOrNull()
        if (last != null && last.first == observation.section) {
            last.second += observation.candidate
        } else {
            groups += observation.section to mutableListOf(observation.candidate)
        }
    }
    return groups.map { (section, lines) -> section to lines.toList() }
}

/**
 * Every fixed label the plain text needs, resolved from `strings.xml` by the caller.
 *
 * A parameter rather than a resource lookup here, because this function has to be readable
 * end to end as "the page, in the order it is read" and because the same shape is what a
 * later export would take.
 */
@Immutable
internal data class ReportLabels(
    val eyebrow: String?,
    /**
     * Every section that can reach the page, by value rather than as a lookup function.
     *
     * A map rather than a lambda so this type compares by value: a function property would
     * make two otherwise identical label sets unequal on every recomposition, which costs
     * the screen its ability to skip.
     */
    val sideheads: Map<ReportSection, String>,
    val patternSidehead: String,
    val closingEyebrow: String,
)

/**
 * The report as plain text, for the clipboard. `MASTER_BUILD_PROMPT.md` 12.3.
 *
 * The copy control is the app's only integration surface with anything else, so what
 * leaves is what a person can read: the sentences the engine wrote, under the sideheads
 * they were read below, in the order they were read in.
 *
 * **Nothing here composes a sentence.** Every line is either a finished corpus line off
 * [ClarityReport] or a fixed label the caller resolved, and the only thing this function
 * does is put newlines between them. `MASTER_BUILD_PROMPT.md` 11.4 forbids building a
 * sentence by concatenation at runtime, and joining finished paragraphs into a document is
 * the thing that rule is not about; the test is whether any sentence here exists that
 * nobody wrote, and none does.
 *
 * The week ribbon does not travel, because it is a picture. Its [caption] does, which is
 * the same reason `design-v3.md` 13 puts that caption on the screen: the numbers are the
 * claim and the marks are the illustration.
 */
internal fun reportPlainText(
    report: ClarityReport,
    caption: String?,
    closing: ReportClosing?,
    labels: ReportLabels,
): String {
    val blocks = mutableListOf<String>()
    labels.eyebrow?.let { blocks += it }
    report.headline?.let { blocks += it.rendered }
    caption?.let { blocks += it }
    report.firstWeekNote?.let { blocks += it.text }

    for ((section, lines) in groupedSections(report.observations)) {
        val sidehead = labels.sideheads.getValue(section)
        blocks += (listOf(sidehead) + lines.map { it.rendered }).joinToString("\n")
    }

    report.pattern?.let { blocks += "${labels.patternSidehead}\n${it.rendered}" }
    closing?.let { blocks += "${labels.closingEyebrow}\n${it.line}" }
    report.generated?.let { blocks += it.text }
    report.basis?.let { blocks += it.rendered }

    return blocks.joinToString("\n\n")
}
