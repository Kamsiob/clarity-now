package com.kamsiob.claritynow.domain.report

import com.kamsiob.claritynow.data.event.ReportGenerated
import com.kamsiob.claritynow.data.event.ReportSectionSnapshot
import com.kamsiob.claritynow.data.event.SubjectKind
import com.kamsiob.claritynow.domain.engine.FactRef
import com.kamsiob.claritynow.domain.engine.FamilyKey
import com.kamsiob.claritynow.domain.engine.VariantKey
import com.kamsiob.claritynow.domain.engine.realize.Candidate
import com.kamsiob.claritynow.domain.engine.validate.ReportLine
import com.kamsiob.claritynow.domain.engine.validate.ReportRole
import com.kamsiob.claritynow.domain.engine.validate.ReportVerdict

/**
 * One composed Clarity Report, in the order it is read.
 * `design-v3.md` 11.1, `MASTER_BUILD_PROMPT.md` 12.3, CLARITY_LOGIC_ENGINE.md 9.
 *
 * **Every sentence here came out of a corpus file through the engine layers in order.**
 * Nothing on this type is a string a composable may add to, reword or replace. The fixed
 * parts of the screen, the sideheads, the eyebrow, the control labels and the caption
 * beneath the ribbon, are interface labels and direct readouts of queried numbers, and
 * they live in `strings.xml`, per CLAUDE.md rule 8. An observation never does.
 *
 * **The one exception is [patternNote], and it was decided rather than allowed.** It is the
 * pattern section's empty state, it is a corpus line, and it does not come through rule
 * selection because it is not an observation: it makes no claim about the person at all.
 * `ReportComposer` carries the reasoning at the point the condition is read.
 *
 * ## What the screen may add, and what it may not
 *
 * The screen chooses type, spacing, motion and the sideheads. It does not choose which
 * observation goes where: [observations] arrive in the order they are to be read, already
 * grouped, already capped and already checked against each other. A screen that re-sorted
 * them would break the length band rhythm in 7.5 and could put two mentions of one area
 * side by side after the composer had spread them.
 *
 * ## Numbers
 *
 * [numbers] is the map CLARITY_LOGIC_ENGINE.md 9.2 requires: every rendered numeric slot
 * in the whole report against the [FactRef] it came from. It is held on the finished report
 * rather than thrown away with the composer, because the screen prints numbers of its own,
 * in the caption beneath the week ribbon, and **there is no second path to a displayed
 * number**. A caption reading a total that disagrees with the observation above it is the
 * same defect as two observations disagreeing, and it would not be caught by anything if
 * the map did not outlive composition. Read them with [numberFor].
 */
data class ClarityReport(
    /** The Sunday the described week begins on, `yyyy-MM-dd`. The eyebrow reads it. */
    val weekStartKey: String,
    /** One per report, under eight words. Null only where nothing qualified. */
    val headline: Candidate?,
    /** Two to four, in reading order, grouped by section. Never padded to reach two. */
    val observations: List<ReportObservation>,
    /** At most one, and only with three weeks of data. Absent means the section is omitted. */
    val pattern: Candidate?,
    /**
     * `CORPUS_2_REPORT.md` 3.16. The pattern section's empty state, under three weeks.
     *
     * **Never set at the same time as [pattern]**: the two conditions are complements, so
     * the section shows a pattern, or says that patterns need a few more weeks, or is
     * omitted because there is a pattern's worth of history and no pattern in it.
     *
     * A [ReportNote] rather than a [Candidate] for the same reason the footer and the two
     * edge states are: there is no number and no name in it, so there is nothing for the
     * report scope checks to compare against facts, and it would enter them as a row of
     * empty sets. It comes out of a corpus file and it has been through layer 5.
     */
    val patternNote: ReportNote? = null,
    /** The footer's basis line, or null when every clause of it would have been zero. */
    val basis: Candidate?,
    /**
     * `CORPUS_2_REPORT.md` 5.1. Fixed, because it is a factual claim and varying it weakens it.
     *
     * Nullable, and null means the bench is not in the corpus. **Not an empty string**: a
     * blank line is a sentence nobody wrote, rendered in the place a sentence belongs, and
     * the whole of 11.1 is that there is no such thing. A test asserts the real corpus
     * produces one, so a null here is an edit somebody made on purpose.
     */
    val generated: ReportNote?,
    /** `CORPUS_2_REPORT.md` 6.2, present only in the first week there has ever been. */
    val firstWeekNote: ReportNote?,
    /** The numbers the caption beneath the week ribbon states. See [ReportTotal]. */
    val totals: List<ReportTotal>,
    /** 9.2's consistency map, kept so the screen's own numbers can be read out of it. */
    val numbers: Map<FactRef, ReportNumber>,
    /** What the composer dropped and why. Never shown; read by the simulator and by tests. */
    val dropped: List<DroppedLine>,
) {

    /** Every line the screen will show, in reading order, as the report scope checks read them. */
    val lines: List<ReportLine>
        get() = buildList {
            headline?.let { add(ReportLine(ReportRole.HEADLINE, it)) }
            observations.forEach { add(ReportLine(ReportRole.OBSERVATION, it.candidate)) }
            pattern?.let { add(ReportLine(ReportRole.PATTERN, it)) }
            basis?.let { add(ReportLine(ReportRole.BASIS, it)) }
        }

    /**
     * The number this report already states for [ref], or null.
     *
     * The caption beneath the ribbon uses this rather than counting again. Counting again
     * is how one fact becomes two numbers, which is the failure 9.2's map exists to catch,
     * and a screen that counted for itself would be doing it outside the map.
     */
    fun numberFor(ref: FactRef): Int? = numbers[ref]?.value

    /**
     * The `REPORT_GENERATED` payload for this report.
     *
     * [sidehead] resolves a section to its label. It is a parameter rather than a field
     * because the labels are fixed interface strings in `strings.xml` and `domain` cannot
     * read resources; the payload carries them so a past report renders from the log alone
     * even after a label is reworded.
     *
     * Every section that **actually appeared** is recorded, and nothing else. `FiringHistory`
     * is rebuilt from exactly these fields, so a report that recorded a line it did not show
     * would exclude that line's variant from the next ninety days of reports nobody ever saw.
     */
    fun payload(reportId: String, sidehead: (ReportSection) -> String): ReportGenerated = ReportGenerated(
        reportId = reportId,
        weekStartKey = weekStartKey,
        headlineKey = headline?.familyKey ?: NO_HEADLINE,
        headlineVariantKey = headline?.variantKey,
        renderedSections = buildList {
            observations.forEach { add(it.snapshot(sidehead(it.section))) }
            pattern?.let { add(snapshotOf(PATTERN_SECTION_KEY, PATTERN_SECTION_KEY, it)) }
        },
        factSnapshot = numbers.entries
            .sortedBy { it.key.toString() }
            .associate { (ref, number) -> ref.toString() to number.value.toString() },
    )

    companion object {

        /** Recorded on the event when a week produced no headline at all. */
        const val NO_HEADLINE = "none"

        /** The pattern is its own section and its sidehead is the one word `Pattern`. */
        const val PATTERN_SECTION_KEY = "pattern"
    }
}

/**
 * One observation and the section it is read under. `design-v3.md` 11.1 item 6.
 *
 * The section is decided at composition rather than on the screen because it decides the
 * reading order, and the reading order is what the length band rule in 7.5 and the parallel
 * clause cap in 7.4b are applied against.
 */
data class ReportObservation(val section: ReportSection, val candidate: Candidate) {

    internal fun snapshot(sidehead: String): ReportSectionSnapshot =
        snapshotOf(section.key, sidehead, candidate)
}

private fun snapshotOf(sectionKey: String, sidehead: String, candidate: Candidate) = ReportSectionSnapshot(
    sectionKey = sectionKey,
    sidehead = sidehead,
    text = candidate.rendered,
    familyKey = candidate.familyKey,
    variantKey = candidate.variantKey,
    escalationStage = candidate.stage,
    register = candidate.register.name,
    subjectId = candidate.subjectId,
    subjectKind = candidate.subjectId?.let {
        if (it in candidate.namedItemIds) SubjectKind.ITEM else SubjectKind.AREA
    },
)

/**
 * The three sideheads a report's observations are read under. `design-v3.md` 11.1 item 6.
 *
 * v3 names them and does not say which observation belongs to which, so the mapping is made
 * here, once, where it can be read against the corpus. Two of the three name themselves:
 * `What you said` is where the report quotes the person's own Pulse answers back to them,
 * and `CORPUS_2_REPORT.md`'s authoring rules name exactly two families as the callback
 * families; `Focus` is where the focus sessions are. Everything else is the week, honestly.
 *
 * **The obvious answer was one section holding everything**, which is what the corpus's own
 * section 2 looks like on the page, and it is worth saying why it loses. The report is read
 * 52 times a year and it is a page of prose. Three sideheads are the only structure it has
 * for a reader to skim, and the two that are not the general one are exactly the two a
 * person would look for: what the app made of what they said, and how the focus went.
 * Section 15.
 *
 * [key] is what the `REPORT_GENERATED` payload records and what the screen looks its label
 * up by. The label itself is a fixed interface string and lives in `strings.xml`.
 */
enum class ReportSection(val key: String) {

    /** `Your week, honestly`, and the corpus's own name for its observation section. */
    YOUR_WEEK("yourWeek"),

    /** `What you said`. The two families that quote a stored `responseLabel`. */
    WHAT_YOU_SAID("whatYouSaid"),

    /** `Focus`. */
    FOCUS("focus"),

    ;

    companion object {

        /**
         * The families that quote the person's own answer back to them.
         *
         * `CORPUS_2_REPORT.md`'s authoring rules put it plainly: every lead in
         * `selfReportVsData` and `completionSplit` must be traceable to a stored
         * `responseLabel`. Those two are the report's callback families and no other family
         * quotes anything.
         */
        val CALLBACK_FAMILIES: Set<FamilyKey> = setOf("selfReportVsData", "completionSplit")

        /** The families about focus sessions. */
        val FOCUS_FAMILIES: Set<FamilyKey> = setOf("focusInvestment", "focusAbandonment")

        /** Where an observation from [family] is read. */
        fun of(family: FamilyKey): ReportSection = when (family) {
            in CALLBACK_FAMILIES -> WHAT_YOU_SAID
            in FOCUS_FAMILIES -> FOCUS
            else -> YOUR_WEEK
        }
    }
}

/**
 * One corpus line with no slots in it: the generated line and the edge states.
 *
 * A carrier rather than a `Candidate`, because a line with no numbers and no names has
 * nothing for the integrity checks to compare against and would enter them as a row of
 * empty sets. It still comes out of a corpus file and it still goes through layer 5 for
 * vocabulary and length before it reaches here.
 */
data class ReportNote(val variantKey: VariantKey, val text: String)

/**
 * One rendered number, and where on the page it was rendered.
 * CLARITY_LOGIC_ENGINE.md 9.2.
 *
 * [source] is the variant key of the line that rendered it, or [RIBBON_CAPTION] for the
 * three totals the screen states directly beneath the ribbon. Both are numbers on the page
 * and 9.2 says the map holds every one of them, so both are entered.
 */
data class ReportNumber(val value: Int, val slotKey: String, val source: String) {

    companion object {

        /** The source recorded for a number the screen states rather than a corpus line. */
        const val RIBBON_CAPTION: String = "ribbon.caption"
    }
}

/**
 * One number the caption beneath the week ribbon states, with the fact behind it.
 * `design-v3.md` 11.1 item 4.
 *
 * v3 asks for one caption line reading **the three headline numbers**, so that the ribbon
 * is never the sole carrier of a claim, and does not say which three. These are the week's
 * total events, its completions and its additions: the first is what the ribbon draws, and
 * the second and third are the flow that most of the report's own families are about, so
 * the caption states what the picture shows and what the prose keeps returning to.
 *
 * **The obvious three were completions, focus minutes and a percentage**, which is what a
 * weekly summary looks like everywhere else and which section 15 warns about by name. Two
 * of those three are about effort rather than about the week, and a percentage in a caption
 * invites the reader to compare it against a target the app deliberately does not have.
 *
 * The caption itself is a direct readout of queried numbers and its wording lives in
 * `strings.xml`, per CLAUDE.md rule 8. The numbers come from here, through a [FactRef],
 * because there is no second path to a displayed number.
 *
 * A total whose value is zero is **absent** rather than zero: `Measures` answers null for
 * nought, exactly as it does for a corpus slot, and a caption reading `0 completed` states
 * a failure the app has no business stating. The caption shows what is there.
 */
data class ReportTotal(val measure: String, val ref: FactRef, val value: Int)

/**
 * A line the composer chose and then did not show, with the rule that removed it.
 *
 * Kept because the composer's drops are the part of composition nobody can see from the
 * finished page. A report of two observations is either a quiet week or a composition rule
 * firing four times, and those are different states in the same way silence and breakage are
 * different states for the Pulse. The simulator prints these and a test asserts against them.
 */
data class DroppedLine(val variantKey: VariantKey, val family: FamilyKey, val reason: String)

/**
 * What a generation attempt produced. `MASTER_BUILD_PROMPT.md` 12.3.
 *
 * Three outcomes and none of them is an error. The screen shows one of three things and
 * **must not show the empty state for a suppressed report**: `Nothing to report yet` is a
 * true sentence about a week in which nothing happened and a false one about a week the app
 * could not prove its arithmetic for.
 */
sealed interface ReportOutcome {

    /** The week the attempt was made for, so a caller can file the outcome without re-deriving it. */
    val weekStartKey: String

    /** A report to show and to write. */
    data class Composed(val report: ClarityReport) : ReportOutcome {
        override val weekStartKey: String get() = report.weekStartKey
    }

    /**
     * `CORPUS_2_REPORT.md` 6.1. Genuinely no activity, so the body is replaced by one line.
     *
     * The styled empty state of 12.3, and the honest thing to show a person who did not use
     * the app this week. No observations are generated, because there is nothing to generate
     * them from, and none are invented to fill the page.
     */
    data class Empty(override val weekStartKey: String, val note: ReportNote?) : ReportOutcome

    /**
     * The report was composed and the report scope checks refused it.
     *
     * **Nothing is written and nothing is shown.** Reaching this means a claim could not be
     * proved, and 12.3's prime directive says what to do with a claim that cannot be proved.
     * The verdict names the check and belongs in a debug log, never on a screen.
     */
    data class Suppressed(
        override val weekStartKey: String,
        val verdict: ReportVerdict.Vetoed,
    ) : ReportOutcome
}
