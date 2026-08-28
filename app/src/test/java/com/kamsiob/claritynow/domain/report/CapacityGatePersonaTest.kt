package com.kamsiob.claritynow.domain.report

import com.kamsiob.claritynow.devtools.ClaritySimulator
import com.kamsiob.claritynow.devtools.SimulationPersona
import com.kamsiob.claritynow.devtools.SimulatorLog
import com.kamsiob.claritynow.domain.engine.ClarityEngine
import com.kamsiob.claritynow.domain.engine.FactExtractor
import com.kamsiob.claritynow.domain.engine.FactSet
import com.kamsiob.claritynow.domain.engine.FamilyKey
import com.kamsiob.claritynow.domain.engine.FiringHistory
import com.kamsiob.claritynow.domain.engine.Precedent
import com.kamsiob.claritynow.domain.engine.catalog.CorpusFixture
import com.kamsiob.claritynow.domain.engine.catalog.Subject
import com.kamsiob.claritynow.domain.engine.catalog.Purpose
import com.kamsiob.claritynow.domain.engine.catalog.SubjectKind
import com.kamsiob.claritynow.domain.engine.realize.Candidate
import com.kamsiob.claritynow.domain.engine.realize.EngineMoment
import com.kamsiob.claritynow.domain.engine.select.FamilyAvailability
import com.kamsiob.claritynow.domain.engine.select.SelectionOutcome
import com.kamsiob.claritynow.domain.engine.select.Selector
import com.kamsiob.claritynow.domain.engine.validate.ClarityValidator
import com.kamsiob.claritynow.domain.query.TrailWindow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The cyclical persona of `MASTER_BUILD_PROMPT.md` 14b.9, over a simulated year.
 *
 * ## The claim
 *
 * A fluctuating condition and a decline are the same numbers. Both are a fall in
 * completions, a rise in idle days, an area going quiet. Without the capacity gate the app
 * tells somebody with a cyclical or relapsing condition that they are deteriorating **on a
 * fixed schedule, forever, and it is technically accurate every single time**: every
 * individual report passes every integrity rule, and the claim the sequence makes across
 * the year is still false, because the shape being read is a cycle and the app has read
 * half of one.
 *
 * Section 17 lists this beside the non-compliance test in 9b, which it resembles: both
 * assert that a whole year of output contains no sentence of a given kind. The assertion
 * here is 14b.9's own sentence, unqualified: **no decline, neglect or fading observation
 * at all**, in any of the fifty two weekly reports.
 *
 * ## Why the year is composed twice
 *
 * A test that only asserted silence would pass on a persona nothing ever qualified for,
 * which is the easiest way in the world to write a green test that proves nothing. So each
 * week is composed twice from the same facts and the same firing history, and the only
 * difference between the two is the three precedent values: the control run has them forced
 * to [Precedent.NONE], which is the permission, so it is the report this person would have
 * received before 14b.9 existed.
 *
 * **What the control run says is the finding.** It is the count of times this app would
 * have told a person with a cyclical condition that they were falling away.
 *
 * ## Why silence for the whole year is honest and not an arrangement
 *
 * The app cannot know a fall is familiar until it has seen its like, and
 * `Precedent.MIN_HISTORY_WEEKS` is twelve. So a year that is silent from its first week has
 * to be silent for two different reasons at two different times, and the second test below
 * is what holds them apart.
 *
 * Before the twelfth week **nothing qualifies**, because `Precedent`'s idea of a low week is
 * much wider than any decline family's. A week under three quarters of this person's normal
 * is a fall to the fact and is nothing at all to `quietWeek`, which wants a week holding
 * fewer events than it has days, or to `decliningActivity`, which wants three weeks falling
 * strictly. `CyclicalDips` spends its first season in exactly that gap, and the second test
 * asserts it rather than trusting it: every gated observation the control run produced sits
 * on a fall whose precedent is `PRESENT`, so **there is no week in this year where silence
 * came from the ranking, from a cooldown, or from a family simply not qualifying.**
 *
 * After the twelfth week the gate is doing the work, and the first test is what proves it,
 * because the control run at those same weeks is full.
 */
class CapacityGatePersonaTest {

    private val composer = ReportComposer(CorpusFixture.catalog, ClaritySimulator.DEFAULT_ZONE)

    private val persona = SimulationPersona.CYCLICAL

    /**
     * The year, run once and read by all three tests.
     *
     * A simulated year is the most expensive thing in this suite and none of the three
     * tests changes it, so running it three times would be three times the cost for the
     * same list.
     */
    private val year: List<Week> by lazy { runTheYear() }

    /** One week's reading: the day, what spoke, what would have, and the facts behind both. */
    private data class Week(
        val day: Int,
        val spoken: List<Line>,
        val control: List<Line>,
        val facts: FactSet,
        /** What had fired before this week, so the selector can be re-run exactly as it ran. */
        val history: FiringHistory,
        /** True when the composer's own integrity layer refused the week, so nothing spoke. */
        val suppressed: Boolean,
    )

    /** One observation, as the family it belongs to and the subject it is about. */
    private data class Line(val family: FamilyKey, val subjectId: String?)

    @Test
    fun `a cyclical year receives no decline, neglect or fading observation at all`() {
        val weeks = year
        assertTrue("no report was composed at all, so this test would prove nothing", weeks.size > MIN_WEEKS)

        val gated = FamilyAvailability.PRECEDENT_GATED.keys
        val controlHits = weeks.flatMap { week -> week.control.filter { it.family in gated }.map { week to it } }
        val spokenHits = weeks
            .flatMap { week ->
                week.spoken.filter { it.family in gated }.map { "day ${week.day}: ${it.family}" }
            }

        println("cyclical persona, ${weeks.size} weekly reports across a simulated year")
        println(
            "  without the gate: ${controlHits.size} decline, neglect or fading observations, " +
                "which is the number of times this app would have told somebody with a " +
                "fluctuating condition that they were falling away",
        )
        println("  with the gate: ${spokenHits.size}")
        println("  the families they belong to: ${controlHits.map { it.second.family }.toSortedSet()}")
        val everySpoken = weeks.flatMap { week -> week.spoken.map { it.family } }.toSortedSet()
        println("  the families this year did speak: $everySpoken")
        // A week the integrity layer refused says nothing for a reason that is not the gate,
        // so the silence assertion is vacuous on it. Reported rather than asserted: the
        // second test below covers those weeks from the fact side either way.
        println("  weeks the composer's own integrity layer refused: ${weeks.count { it.suppressed }}")

        assertTrue(
            "the control run has to contain the observations the gate is supposed to remove, " +
                "or this persona is proving nothing about the gate",
            controlHits.isNotEmpty(),
        )
        assertEquals(
            "14b.9: a persona whose activity is cyclical across a simulated year receives no " +
                "decline, neglect or fading observation, because every dip they have has a " +
                "precedent. These spoke anyway",
            emptyList<String>(),
            spokenHits,
        )
    }

    /**
     * Every fall this year holds has had its like before, so the gate is what silenced it.
     *
     * **This is an assertion about the persona rather than about the engine, and it is the
     * one to read first if this file goes red.** The test above would pass on a year in
     * which a gated family never qualified, never won its ranking, or was held off by a
     * cooldown, and none of those is the capacity gate. This one closes that door: for
     * every gated observation the control run produced, the precedent for that observation's
     * own subject is `PRESENT`, which is the only value that closes the gate.
     *
     * A failure here names the week, the family and the precedent it found instead.
     * `NOT_IN_A_DIP` or `NONE` means the persona has drifted and its dips no longer have
     * the twins they were built with; `INSUFFICIENT` means a family qualified in the first
     * twelve weeks, where no precedent is answerable and the gate cannot help. Either way
     * the fix is in `CyclicalDips` and not in `FamilyAvailability`.
     */
    @Test
    fun `every fall in this year has had its like before, so nothing is silent by luck`() {
        val unfamiliar = mutableListOf<String>()
        var checked = 0
        for (week in year) {
            for (line in week.control) {
                val dip = FamilyAvailability.PRECEDENT_GATED[line.family] ?: continue
                checked++
                val subject = line.subjectId?.let { Subject(it, kindOf(week.facts, it)) }
                val precedent = FamilyAvailability.precedentFor(dip, week.facts, subject)
                if (precedent != Precedent.PRESENT) {
                    unfamiliar += "day ${week.day}: ${line.family} on ${line.subjectId ?: "the window"} " +
                        "reads $precedent, so the gate is not what kept it quiet"
                }
            }
        }
        println(
            "  gated observations the control run produced: $checked, of which " +
                "${unfamiliar.size} sit on a fall this app had never seen the like of",
        )
        assertEquals(
            "every fall this persona has is supposed to have a twin behind it, so that the " +
                "silence above is the gate and not a ranking, a cooldown or a family that " +
                "never qualified. These do not",
            emptyList<String>(),
            unfamiliar,
        )
    }

    /**
     * The gate removes and never adds, asserted where that sentence is true.
     *
     * **Not at the composed report.** A report shows one headline and at most four
     * observations out of everything that qualified, so removing a candidate from the
     * ranking frees a place and the next candidate takes it. Sixteen lines appear in this
     * year's gated run that the control run had no room for, and every one of them is a
     * true observation the engine had already proved: `growingQueues` six times, `netInflow`
     * three, `queuePressure` and `focusHabitForming` once each, and `datedFallback` five,
     * which `ReportRules` calls the one rule in the catalog meant to pass most of the time
     * and which exists precisely so that a headline slot is never empty.
     * Reading those as inventions would be reading 11.4's `never pad a section to reach a
     * minimum` as a rule against the fallback the catalog is built around.
     *
     * **The claim belongs to the selector**, which is the only thing the gate touches.
     * Step 1b is a filter over `qualified`, so for every purpose the gated ranking has to be
     * a subsequence of the ungated one: same pairs, same order, some missing. That fails the
     * moment somebody makes either gate a criterion instead, because `specificity` is
     * `criteria.size` and a criterion reorders the ranking rather than shortening it, which
     * is the mistake `FamilyAvailability` spends its class comment arguing against.
     */
    @Test
    fun `the gate only ever removes, and never reorders or adds`() {
        val engine = ClarityEngine(CorpusFixture.catalog, ClarityValidator(ClaritySimulator.DEFAULT_ZONE), ClaritySimulator.DEFAULT_ZONE)
        val selector = Selector(CorpusFixture.catalog)
        val wrong = mutableListOf<String>()
        var compared = 0
        for (week in year) {
            val moment = engine.momentOf(week.facts)
            for (purpose in FamilyAvailability.RE_ENTRY_PURPOSES) {
                val gated = ranked(selector, purpose, week.facts, week.history, moment)
                val open = ranked(selector, purpose, withoutPrecedents(week.facts), week.history, moment)
                compared++
                if (!isSubsequenceOf(gated, open)) {
                    wrong += "day ${week.day} $purpose: gated $gated is not a subsequence of open $open"
                }
            }
        }
        println("  rankings compared: $compared, of which ${wrong.size} were not a subsequence")
        assertEquals(
            "the capacity gate is a filter at step 1b, so every ranking it produces has to be " +
                "the ungated ranking with entries removed. A ranking that gained a pair or " +
                "changed an order means the gate is deciding which turn a family gets rather " +
                "than whether it gets one",
            emptyList<String>(),
            wrong,
        )
    }

    /** Every pair the selector would rank for [purpose], as rule key and subject. */
    private fun ranked(
        selector: Selector,
        purpose: Purpose,
        facts: FactSet,
        history: FiringHistory,
        moment: EngineMoment,
    ): List<String> = when (val outcome = selector.select(purpose, facts, history, moment)) {
        is SelectionOutcome.Ranked -> outcome.selections.map { "${it.rule.key}/${it.subject?.id ?: "-"}" }
        is SelectionOutcome.Silent -> emptyList()
    }

    private fun isSubsequenceOf(inner: List<String>, outer: List<String>): Boolean {
        var index = 0
        for (entry in outer) if (index < inner.size && inner[index] == entry) index++
        return index == inner.size
    }

    // ------------------------------------------------------------------ the run

    /**
     * A year of weekly reports, each composed twice.
     *
     * The gated report is written back into the log so that next week's cooldowns and
     * variant exclusions are the real ones, exactly as `ReportPersonaTest` does and for the
     * reason section 12 gives. The control is composed against the same firing history and
     * is never written back: it is a reading of what would have been said, not a second
     * year.
     *
     * `SimulationPersona.isPresentOn` is the gate on writing anything, here as in the
     * simulator: a day this person was not there is a day the app could not have recorded
     * one of their gestures.
     *
     * No `APP_OPENED` is written, so `HistoryFacts.isJustBackFromAbsence` is false all year
     * and the re-entry withholding of 14b.4 never fires. That is deliberate: it leaves the
     * capacity gate as the only thing that can remove a family, so a silence measured here
     * is this gate and not the other one.
     */
    private fun runTheYear(): List<Week> {
        val log = SimulatorLog(ClaritySimulator.DEFAULT_ZONE, ClaritySimulator.DEFAULT_START_DATE, persona.key)
        val weeks = mutableListOf<Week>()
        for (day in 0 until ClaritySimulator.DAYS_IN_YEAR) {
            if (persona.isPresentOn(day)) {
                if (day == persona.installDay) persona.setUp(log)
                persona.act(log, day)
            }
            if (day < DAYS_PER_WEEK || day % DAYS_PER_WEEK != 0) continue

            val queries = log.queries()
            val facts = FactExtractor(queries).extract(
                TrailWindow(log.startOfDay(day - DAYS_PER_WEEK), log.startOfDay(day)),
            )
            val history = FiringHistory.from(queries, log.at(day, REPORT_HOUR))
            val weekKey = log.dateKey(day - DAYS_PER_WEEK)
            val gated = composer.compose(facts, history, weekKey)
            val open = composer.compose(withoutPrecedents(facts), history, weekKey)
            if (gated is ReportOutcome.Composed) {
                weeks += Week(day, lines(gated.report), families(open), facts, history, suppressed = false)
                log.add(
                    log.at(day, REPORT_HOUR),
                    gated.report.payload("report-${persona.key}-$day") { it.key },
                )
            } else if (open is ReportOutcome.Composed) {
                weeks += Week(
                    day,
                    emptyList(),
                    families(open),
                    facts,
                    history,
                    suppressed = gated is ReportOutcome.Suppressed,
                )
            }
        }
        return weeks
    }

    /** Every family the report says out loud, with the subject it says it about. */
    private fun lines(report: ClarityReport): List<Line> =
        candidates(report).map { Line(it.familyKey, it.subjectId) }

    private fun families(outcome: ReportOutcome): List<Line> =
        if (outcome is ReportOutcome.Composed) lines(outcome.report) else emptyList()

    private fun candidates(report: ClarityReport): List<Candidate> =
        listOfNotNull(report.headline) + report.observations.map { it.candidate } + listOfNotNull(report.pattern)

    /** The same week with every precedent read as the permission, which is the year before 14b.9. */
    private fun withoutPrecedents(facts: FactSet): FactSet = facts.copy(
        areas = facts.areas.mapValues { it.value.copy(dipPrecedent = Precedent.NONE) },
        history = facts.history.copy(
            activityDipPrecedent = Precedent.NONE,
            focusDipPrecedent = Precedent.NONE,
        ),
    )

    private fun kindOf(facts: FactSet, subjectId: String): SubjectKind =
        if (subjectId in facts.areas) SubjectKind.AREA else SubjectKind.ITEM

    private companion object {
        const val DAYS_PER_WEEK = 7
        const val REPORT_HOUR = 8

        /** A year holds fifty two report windows. Well under this and the year did not run. */
        const val MIN_WEEKS = 20
    }
}
