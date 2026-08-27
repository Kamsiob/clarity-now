package com.kamsiob.claritynow.domain.engine.validate

import com.kamsiob.claritynow.domain.engine.AreaId
import com.kamsiob.claritynow.domain.engine.FactRef
import com.kamsiob.claritynow.domain.engine.FactSet
import com.kamsiob.claritynow.domain.engine.FamilyKey
import com.kamsiob.claritynow.domain.engine.catalog.ClarityCatalog
import com.kamsiob.claritynow.domain.engine.catalog.ClarityRule
import com.kamsiob.claritynow.domain.engine.catalog.Purpose
import com.kamsiob.claritynow.domain.engine.catalog.Subject
import com.kamsiob.claritynow.domain.engine.catalog.SubjectKind
import com.kamsiob.claritynow.domain.engine.realize.Candidate
import com.kamsiob.claritynow.domain.engine.realize.FactLookup
import com.kamsiob.claritynow.domain.engine.realize.MeasureValue
import com.kamsiob.claritynow.domain.engine.select.IncompatibilityMatrix
import com.kamsiob.claritynow.domain.engine.select.Selection
import java.time.ZoneId

/**
 * Layer 5 at the scale of a whole report. CLARITY_LOGIC_ENGINE.md 9, and
 * `MASTER_BUILD_PROMPT.md` 12.3.
 *
 * ## Why a second validator exists
 *
 * [ClarityValidator] checks one sentence against the facts it claims. That is the whole
 * defense for the Pulse, which says one thing. The Report says eight to ten things at
 * once, which creates two failure classes a per sentence validator cannot see:
 *
 * - **Two true sentences that contradict each other.** One says the week was narrow, the
 *   next says it was broad. Both pass every check in section 8 and the pair is incoherent
 * - **One fact rendering two different numbers.** Two observations reach for the same
 *   count and print `9` and `8`. Each number re-reads correctly against the fact it was
 *   given, and the report is still wrong, because the two were given different facts by a
 *   recomputation somewhere upstream
 *
 * 12.3 calls data integrity the prime directive and states why it does not degrade
 * gracefully: **one fabricated area name or off by one number permanently destroys the
 * credibility of everything else the app says**, and the person reading it has no way to
 * verify anything afterwards. A report is read 52 times a year. It only has to be caught
 * being wrong once.
 *
 * ## What a veto is, and what it is not
 *
 * A veto here means **a claim the app cannot prove**, or a pair of claims that cannot both
 * be the truth about one week. It never means a matter of rhythm. The length band rule and
 * the editorial budget are composition constraints applied while the report is assembled,
 * and a report that ends up plainer than the composer wanted is still true; suppressing it
 * would trade a real observation for a cadence. So those are not here, and nothing in this
 * class can fire because a report reads slightly flat.
 *
 * The whole report is vetoed rather than the offending line, per 9.2. That reads as
 * disproportionate until you ask which line is wrong: two numbers disagreeing about one
 * fact means the fact was computed twice and differently, and nothing on the page can say
 * which of the two computations was the good one. Dropping one sentence would leave the
 * other one on the screen, and the surviving number has a one in two chance of being the
 * false one.
 *
 * ## Every check is reachable
 *
 * `MASTER_BUILD_PROMPT.md` 12.3: the veto path must be reachable in unit tests. There is
 * one test per member of [ReportCheck] that builds a report violating exactly that check
 * and asserts the veto, because a validator whose failure branch never executes is a
 * validator nobody has verified.
 */
class ReportIntegrity(private val catalog: ClarityCatalog, private val zone: ZoneId) {

    private val rulesByKey: Map<String, ClarityRule> = catalog.rules.associateBy { it.key }

    /**
     * Checks a whole assembled report against the facts it was composed from.
     *
     * [lines] is every line the screen will show, in the order it will show them, including
     * the headline, the pattern and the basis line. A line the report does not show must not
     * be here: 10.4 rule 2 turns on layer 6 only being handed the observations that actually
     * appeared, and the same reasoning applies to every check below that counts something.
     */
    fun inspect(lines: List<ReportLine>, facts: FactSet): ReportVerdict {
        for ((check, inspect) in CHECKS) {
            val detail = inspect(Report(lines, facts))
            if (detail != null) return ReportVerdict.Vetoed(check, detail)
        }
        return ReportVerdict.Passed
    }

    /** One report, as the checks read it. */
    private data class Report(val lines: List<ReportLine>, val facts: FactSet) {

        val observations: List<ReportLine> = lines.filter { it.role == ReportRole.OBSERVATION }

        val headlineFamily: FamilyKey? =
            lines.firstOrNull { it.role == ReportRole.HEADLINE }?.candidate?.familyKey

        val pattern: ReportLine? = lines.firstOrNull { it.role == ReportRole.PATTERN }
    }

    /**
     * The checks, in order, cheapest claim first.
     *
     * The order is the same idea as section 8's: a report breaking several is reported
     * against the most fundamental thing wrong with it. A named area with no events is a
     * fabricated claim about a week; a fifth observation is a report the app does not
     * define. Both are vetoes and only one of them would end somebody's trust in the app.
     */
    private val CHECKS: List<Pair<ReportCheck, (Report) -> String?>> = listOf(
        ReportCheck.AREA_HAS_EVENTS to ::areaHasEvents,
        ReportCheck.ITEM_RESOLVES to ::itemResolves,
        ReportCheck.NUMBER_CONSISTENCY to ::numberConsistency,
        ReportCheck.NUMBER_PROVENANCE to ::numberProvenance,
        ReportCheck.INCOMPATIBLE_PAIR to ::incompatiblePair,
        ReportCheck.HEADLINE_CONFLICT to ::headlineConflict,
        ReportCheck.AREA_MENTION_CAP to ::areaMentionCap,
        ReportCheck.OBSERVATION_COUNT to ::observationCount,
        ReportCheck.PATTERN_WITHOUT_HISTORY to ::patternWithoutHistory,
    )

    /** Exposed so a test can assert the list is complete rather than infer it. */
    internal val checkOrder: List<ReportCheck> get() = CHECKS.map { it.first }

    /**
     * Check 1. Every area named anywhere in the report had real events in the window.
     *
     * The same question section 8 check 1 asks of one sentence, asked of the page. It is
     * asked twice on purpose. Every candidate the engine produced has already answered it,
     * and the basis line is realized by `domain.report` rather than by the engine loop, so
     * this is the one place that covers **every** line the screen will show without anybody
     * having to remember which of them took which route.
     *
     * It is also where three of 12.3's four named prohibitions land at once. An archived or
     * tombstoned area is absent from `FactSet.areas` by construction, per 3.1, so it fails
     * on the first branch. **A new area with no activity** fails on the second, which is the
     * one a reader is most likely to think is harmless: the area exists, the person made it
     * this week, and a sentence naming it in a report about what happened is still a claim
     * about a week the area had nothing to do with.
     */
    private fun areaHasEvents(report: Report): String? {
        for (line in report.lines) {
            for (areaId in line.candidate.namedAreaIds.sorted()) {
                val area = report.facts.areas[areaId]
                    ?: return "${line.where()} names area $areaId, which is not in this window's facts"
                if (area.eventsInWindow <= 0) {
                    return "${line.where()} names ${area.nameSnapshot} ($areaId), which had " +
                        "${area.eventsInWindow} events in the window this report describes"
                }
            }
        }
        return null
    }

    /**
     * Check 2. Every item named anywhere in the report resolves in the fact set.
     *
     * Resolution and the tombstone test are one question, for the reason section 8 check 2
     * gives: a deleted item leaves the fact set entirely, so an id that resolves nowhere is
     * a tombstone or an invention and neither may be named.
     */
    private fun itemResolves(report: Report): String? {
        val live = liveItemIds(report.facts)
        for (line in report.lines) {
            val unknown = line.candidate.namedItemIds.sorted().firstOrNull { it !in live }
            if (unknown != null) {
                return "${line.where()} names item $unknown, which nothing in this window's facts resolves"
            }
        }
        return null
    }

    /**
     * Check 4. Every number on the page carries a [FactRef] that still reads the same.
     *
     * **This is the check `MASTER_BUILD_PROMPT.md` 12.3 means by "before any number is
     * stated it must come from an actual count query".** Everything the whole report says
     * has been counted, and the count is repeatable from the same fact set. A number with no reference is
     * untraceable, and an untraceable number is a veto rather than a warning: nobody can
     * tell afterwards whether it was counted or guessed, and the person holding the phone
     * certainly cannot.
     */
    private fun numberProvenance(report: Report): String? {
        for (line in report.lines) {
            for (slot in line.candidate.slots.values.sortedBy { it.key }) {
                val value = slot.numericValue ?: continue
                val ref = line.candidate.sourceFacts[slot.key]
                    ?: return "${line.where()} renders {${slot.key}} as $value with no FactRef"
                val read = FactLookup.read(report.facts, ref, zone)
                if (read !is MeasureValue.Number) {
                    return "${line.where()} renders {${slot.key}} as $value from $ref, which reads no number now"
                }
                if (read.value != value) {
                    return "${line.where()} renders {${slot.key}} as $value and $ref reads ${read.value}"
                }
            }
        }
        return null
    }

    /**
     * Check 3. One fact, one number, across the whole report. CLARITY_LOGIC_ENGINE.md 9.2.
     *
     * **The check a per sentence validator cannot make.** Every number in the report is
     * entered into a map keyed by its [FactRef], and two entries under one key that
     * disagree veto the report. Check 3 cannot catch this: it compares each number against
     * the fact set it was handed, and two numbers handed two different fact sets both pass
     * while the page says the week held nine things and six things.
     *
     * 9.2 says what a hit here means, and it is worth repeating because it is the reason
     * the whole report goes rather than one line: this **indicates a fact recomputation bug
     * rather than a copy problem**. The lines are innocent. Something read the log twice
     * and got two answers, and no sentence on the page knows which time it was right.
     *
     * **It runs before the provenance check, and the order is load bearing.** Two lines that
     * disagree about one fact also disagree with the fact set, so the provenance check would
     * fire on whichever of them ran into it first and report one line as carrying a stale
     * number. That is true and it is the smaller half of the truth: the useful detail names
     * both lines and says the fact was computed twice. Running provenance first would also
     * make this branch unreachable, and an unreachable check is one nobody has verified.
     */
    private fun numberConsistency(report: Report): String? {
        val seen = mutableMapOf<FactRef, Pair<Int, ReportLine>>()
        for (line in report.lines) {
            for (slot in line.candidate.slots.values.sortedBy { it.key }) {
                val value = slot.numericValue ?: continue
                val ref = line.candidate.sourceFacts[slot.key] ?: continue
                val earlier = seen[ref]
                if (earlier == null) {
                    seen[ref] = value to line
                } else if (earlier.first != value) {
                    return "$ref renders as ${earlier.first} in ${earlier.second.where()} and as " +
                        "$value in ${line.where()}. One fact produced two numbers in one report"
                }
            }
        }
        return null
    }

    /**
     * Check 5. No report holds both members of an incompatible pair.
     * CLARITY_LOGIC_ENGINE.md 9.1.
     *
     * The selector applies the matrix while it is choosing, which is where the work belongs
     * and where a dropped family can still be replaced by the next non conflicting one.
     * This is the proof that it held, made over the assembled page rather than over the
     * selection, so a pair introduced by anything other than the observation pass is caught
     * too.
     *
     * **It calls the same [IncompatibilityMatrix] the selector calls.** Re-encoding 9.1's
     * fifteen rows here would create a second copy of the matrix, and a second copy is a
     * place for the two to disagree. The candidates are turned back into [Selection]s for
     * that call, which is exact rather than approximate: the matrix reads a family key and a
     * subject id, and a candidate carries both.
     */
    private fun incompatiblePair(report: Report): String? {
        val selections = report.observations.mapNotNull { line -> selectionOf(line)?.let { line to it } }
        for (i in selections.indices) {
            for (j in i + 1 until selections.size) {
                val (firstLine, first) = selections[i]
                val (secondLine, second) = selections[j]
                if (IncompatibilityMatrix.conflicts(first, second)) {
                    return "${firstLine.where()} and ${secondLine.where()} are an incompatible pair " +
                        "under CLARITY_LOGIC_ENGINE.md 9.1"
                }
            }
        }
        return null
    }

    /**
     * Check 6. Nothing argues with the headline. CLARITY_LOGIC_ENGINE.md 9.2.
     *
     * **The headline is selected first and constrains everything after it**, and a
     * conflicting observation is excluded entirely rather than deprioritized. The headline
     * is the largest text in the app and it sets the frame the rest of the page is read
     * inside; an observation that disagrees with it does not read as nuance, it reads as
     * the app disagreeing with itself in two different type sizes.
     */
    private fun headlineConflict(report: Report): String? {
        val headline = report.headlineFamily ?: return null
        for (line in report.observations) {
            val selection = selectionOf(line) ?: continue
            if (IncompatibilityMatrix.conflictsWithHeadline(headline, selection)) {
                return "the headline is $headline and ${line.where()} conflicts with the frame it set"
            }
        }
        return null
    }

    /**
     * Check 7. No area named in more than two observations. CLARITY_LOGIC_ENGINE.md 9.2.
     *
     * Beyond two, the report reads as being about one area rather than about the week, and
     * the pattern section and the closing line lose their weight. Counted over the
     * observations only, exactly as 9.2 states it: the headline and the pattern are one line
     * each and are allowed to name the area the week was actually about.
     */
    private fun areaMentionCap(report: Report): String? {
        val mentions = mutableMapOf<AreaId, Int>()
        for (line in report.observations) {
            for (areaId in line.candidate.namedAreaIds) {
                mentions[areaId] = (mentions[areaId] ?: 0) + 1
            }
        }
        val over = mentions.entries.sortedBy { it.key }.firstOrNull { it.value > MAX_AREA_MENTIONS }
        return if (over == null) {
            null
        } else {
            val name = report.facts.areas[over.key]?.nameSnapshot ?: over.key
            "$name is named in ${over.value} of the ${report.observations.size} observations, " +
                "and 9.2 allows $MAX_AREA_MENTIONS"
        }
    }

    /**
     * Check 8. Two to four observations, and never five.
     *
     * Section 5 defines the Report's observation pass as two to four. The floor is not
     * checked here and cannot be: **one qualifying observation means one observation**, per
     * 11.4, and a report of one is a report about a week with one thing worth saying. The
     * ceiling is checked, because a fifth observation is not a longer report, it is a report
     * assembled by something that does not know what this one is.
     */
    private fun observationCount(report: Report): String? =
        if (report.observations.size <= MAX_OBSERVATIONS) {
            null
        } else {
            "the report carries ${report.observations.size} observations, and section 5 " +
                "defines the pass as two to four"
        }

    /**
     * Check 9. A pattern needs three weeks behind it. CLARITY_LOGIC_ENGINE.md 6.3.
     *
     * A pattern is a claim about how somebody's weeks compare to each other, and two weeks
     * of data cannot support one. 11.4 states the other half, which the composer holds
     * rather than this class: **no trend means the section is omitted entirely** rather than
     * filled with a line saying there is no trend yet.
     */
    private fun patternWithoutHistory(report: Report): String? {
        val pattern = report.pattern ?: return null
        val weeks = report.facts.history.weeksOfData
        return if (weeks >= PATTERN_WEEKS) {
            null
        } else {
            "${pattern.where()} is a pattern and there are $weeks weeks of data, " +
                "against the $PATTERN_WEEKS 6.3 requires"
        }
    }

    /**
     * The selection a candidate came from, rebuilt for the matrix.
     *
     * Null when the rule is no longer in the catalog, which happens only to a line composed
     * against a rule key nothing declares. The pair check skips it rather than vetoing:
     * check 1 and check 3 have already established that whatever it says is true of the
     * facts, and an unknown rule key is a catalog problem that `CatalogIntegrity` fails the
     * build over rather than a false sentence.
     */
    private fun selectionOf(line: ReportLine): Selection? {
        val rule = rulesByKey[line.candidate.ruleKey] ?: return null
        val subjectId = line.candidate.subjectId
        val subject = subjectId?.let {
            Subject(it, if (it in line.candidate.namedItemIds) SubjectKind.ITEM else SubjectKind.AREA)
        }
        return Selection(
            rule = rule,
            purpose = Purpose.REPORT_OBSERVATION,
            subject = subject,
            callback = null,
            windowDays = 0,
        )
    }

    private fun liveItemIds(facts: FactSet): Set<String> = buildSet {
        facts.items.activeByArea.values.forEach { add(it.itemId) }
        facts.items.completedInWindow.forEach { add(it.itemId) }
        facts.areas.values.mapNotNull { it.activeItemId }.forEach { add(it) }
        facts.items.longestActiveItemId?.let { add(it) }
        facts.history.longestEverActiveItemId?.let { add(it) }
    }

    companion object {

        /** 9.2. No single area in more than two of the four observations. */
        const val MAX_AREA_MENTIONS = 2

        /** Section 5, second paragraph. */
        const val MAX_OBSERVATIONS = 4

        /** 6.3. No pattern under three weeks of data. */
        const val PATTERN_WEEKS = 3
    }
}

/**
 * Where a line sits in a report. CLARITY_LOGIC_ENGINE.md 9 and `design-v3.md` 11.1.
 *
 * The closing line is deliberately absent. It is layer 6, it is produced after the body
 * exists rather than as part of it, and 10.4 rule 2 requires it to be derived from the
 * observations that actually appeared. Giving it a role here would make it possible to hand
 * this class a report whose closing had not been through layer 6 at all.
 */
enum class ReportRole {

    /** One per report, under eight words, the largest text in the app. */
    HEADLINE,

    /** Two to four, never padded to reach two. */
    OBSERVATION,

    /** At most one, and only with three weeks of data behind it. */
    PATTERN,

    /** The footer's basis line, which states what the report was built from. */
    BASIS,
}

/**
 * One line of an assembled report, with the candidate that produced it.
 *
 * The candidate rather than the string, because every check worth making is about the
 * relationship between the sentence and the facts, and a rendered string has thrown that
 * relationship away. This is the same reason `Candidate` exists at all.
 */
data class ReportLine(val role: ReportRole, val candidate: Candidate) {

    /** How a veto detail names this line, written to be read months later beside a rule key. */
    internal fun where(): String = "the ${role.name.lowercase()} ${candidate.variantKey}"
}

/** The report scope checks of CLARITY_LOGIC_ENGINE.md 9, numbered as this class runs them. */
enum class ReportCheck(val number: Int, val what: String) {
    AREA_HAS_EVENTS(1, "area has events in the window"),
    ITEM_RESOLVES(2, "item resolves"),
    NUMBER_CONSISTENCY(3, "number consistency"),
    NUMBER_PROVENANCE(4, "number provenance"),
    INCOMPATIBLE_PAIR(5, "incompatible pair"),
    HEADLINE_CONFLICT(6, "headline conflict"),
    AREA_MENTION_CAP(7, "area mention cap"),
    OBSERVATION_COUNT(8, "observation count"),
    PATTERN_WITHOUT_HISTORY(9, "pattern without history"),
    ;

    override fun toString(): String = "report check $number, $what"
}

/** What the report scope layer answers for one assembled report. */
sealed interface ReportVerdict {

    /** Every check cleared. The report may be shown and written. */
    data object Passed : ReportVerdict

    /**
     * The report may not be shown, with the check that rejected it and what was wrong.
     *
     * **Nothing partial survives a veto.** There is no path here that returns a shorter
     * report, because the composer has already applied every rule that can be satisfied by
     * dropping a line. Reaching this means something the composer could not have caused.
     */
    data class Vetoed(val check: ReportCheck, val detail: String) : ReportVerdict {
        override fun toString(): String = "$check: $detail"
    }
}
