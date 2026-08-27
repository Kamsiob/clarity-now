package com.kamsiob.claritynow.devtools

import com.kamsiob.claritynow.data.event.PlanAccepted
import com.kamsiob.claritynow.data.event.PlanOffered
import com.kamsiob.claritynow.data.event.PulseAnswered
import com.kamsiob.claritynow.data.event.PulseGenerated
import com.kamsiob.claritynow.data.event.ReflectionPeriod
import com.kamsiob.claritynow.data.event.ReportGenerated
import com.kamsiob.claritynow.data.event.ReportSectionSnapshot
import com.kamsiob.claritynow.data.event.SubjectKind
import com.kamsiob.claritynow.domain.engine.CandidateValidator
import com.kamsiob.claritynow.domain.engine.ClarityEngine
import com.kamsiob.claritynow.domain.engine.EngineResult
import com.kamsiob.claritynow.domain.engine.FactExtractor
import com.kamsiob.claritynow.domain.engine.FactRef
import com.kamsiob.claritynow.domain.engine.FactSet
import com.kamsiob.claritynow.domain.engine.FiringHistory
import com.kamsiob.claritynow.domain.engine.RenderedOutput
import com.kamsiob.claritynow.domain.engine.SilenceReason
import com.kamsiob.claritynow.domain.engine.StableHash
import com.kamsiob.claritynow.domain.engine.catalog.ClarityCatalog
import com.kamsiob.claritynow.domain.engine.catalog.LengthBand
import com.kamsiob.claritynow.domain.engine.catalog.Purpose
import com.kamsiob.claritynow.domain.engine.catalog.Register
import com.kamsiob.claritynow.domain.engine.catalog.ResponseOption
import com.kamsiob.claritynow.domain.engine.realize.Candidate
import com.kamsiob.claritynow.domain.engine.realize.Slot
import com.kamsiob.claritynow.domain.engine.validate.ClarityValidator
import com.kamsiob.claritynow.domain.engine.validate.ValidationResult
import com.kamsiob.claritynow.domain.query.TrailWindow
import java.time.LocalDate
import java.time.ZoneId

/**
 * The simulator. CLARITY_LOGIC_ENGINE.md 12, and `MASTER_BUILD_PROMPT.md` 11.5.
 *
 * A full simulated year per persona, with the engine run **day by day for the Pulse, week
 * by week for the Report, and Momentum on every simulated open**, and every invocation
 * recorded with the rule that fired, the stage, the register, the variant key and the facts
 * used.
 *
 * ## Why this exists before the corpus
 *
 * 11.2 of this document says to judge every batch of authored lines against simulator
 * output rather than in isolation, and 11.5 of `MASTER_BUILD_PROMPT.md` says that without
 * this you are authoring blind. The reason is not convenience. A line reads differently
 * when it is the fourth thing the app has said this week than it does on the page, and the
 * only way to see that is to see a year of them in order with the machinery that chose them
 * showing its work. **The annotation is the product.** A wall of sentences with no
 * provenance cannot be used to judge a corpus, because a reader cannot tell a line that
 * fired for the right reason from one that fired for the wrong reason and happened to be
 * true.
 *
 * ## What runs, and on which window
 *
 * | surface | cadence | window |
 * |---|---|---|
 * | Pulse | every simulated open | yesterday, `[startOfDay(d - 1), startOfDay(d))` |
 * | Momentum | every simulated open | the last fourteen days, ending at the open |
 * | Areas banner | every simulated open | this week so far, ending at the open |
 * | Report | the first open of a new week | the trailing seven days, `[d - 7, d)` |
 *
 * The Pulse window is yesterday because `MASTER_BUILD_PROMPT.md` 11.3 says the reflection
 * period before 17:00 is yesterday and a simulated open is in the morning. That is also why
 * the Pulse window ends exactly at local midnight, which `ClarityEngine.momentOf` reads as
 * the morning rather than as the night before.
 *
 * ## Purity, and the one thing that is not
 *
 * Nothing here reads a clock or draws a random number. The zone and the start date are
 * constructor parameters, every instant is built from a local date and a local time, and
 * every persona decision is a hash of the day. Two runs of this class produce identical
 * dumps, which [SimulationChecks] and the determinism test both rely on.
 *
 * The one thing this class does that the app does not is **write the engine's own output
 * back into the log**. That is not a liberty, it is the point: `FiringHistory` is derived
 * entirely from `PULSE_GENERATED`, `REPORT_GENERATED` and `PLAN_OFFERED` and never from
 * DataStore, per 2.1, so the ninety day variant exclusion, the family cooldowns and the
 * escalation ladders only exist in a run that records what it said. A simulator that
 * dropped its own output on the floor would show every family at stage one forever and
 * would repeat lines it had used the day before.
 */
class ClaritySimulator(
    private val catalog: ClarityCatalog,
    private val zone: ZoneId = DEFAULT_ZONE,
    private val startDate: LocalDate = DEFAULT_START_DATE,
    private val days: Int = DAYS_IN_YEAR,
) {

    /** Every rule by key, so the dump can print the criteria that fired. */
    private val rulesByKey = catalog.rules.associateBy { it.key }

    /** Runs every persona section 12 names. */
    fun runAll(personas: List<SimulationPersona> = SimulationPersona.ALL): List<SimulationRun> =
        personas.map { run(it) }

    /** One persona's year. */
    fun run(persona: SimulationPersona): SimulationRun {
        val log = SimulatorLog(zone, startDate, originId = persona.key)
        val validator = RecordingValidator(ClarityValidator(zone))
        val engine = ClarityEngine(catalog, validator, zone)
        val invocations = mutableListOf<SimulatedInvocation>()
        var openDays = 0

        for (day in 0 until days) {
            if (day == persona.installDay) persona.setUp(log)
            if (day >= persona.installDay && persona.opensOn(day)) {
                openDays++
                log.opened(day, OPEN_HOUR)
                openTheApp(persona, log, engine, validator, day, invocations)
            }
            persona.act(log, day)
        }

        return SimulationRun(
            persona = persona,
            days = days,
            openDays = openDays,
            eventCount = log.eventCount,
            invocations = invocations.toList(),
        )
    }

    /**
     * One simulated foreground: Momentum, the banner, the Pulse, and the Report on a Sunday.
     *
     * The facade and the firing history are built **once** for the whole open and shared
     * across the surfaces, which is what a real foreground does and is also what keeps a
     * year of this from being quadratic. Each surface gets its own window and therefore its
     * own fact set, because a `FactSet` is a fully populated snapshot of one window and
     * reusing one across two windows would put a fourteen day count into a sentence about
     * yesterday.
     */
    private fun openTheApp(
        persona: SimulationPersona,
        log: SimulatorLog,
        engine: ClarityEngine,
        validator: RecordingValidator,
        day: Int,
        invocations: MutableList<SimulatedInvocation>,
    ) {
        val queries = log.queries()
        val extractor = FactExtractor(queries)
        val openInstant = log.at(day, OPEN_HOUR)
        val history = FiringHistory.from(queries, openInstant)

        // Momentum, on every open. The rolling fourteen days of 12.2, which never becomes a
        // streak: a missed day narrows the count and resets nothing.
        val momentumFacts = extractor.extract(
            TrailWindow(log.startOfDay(day - MOMENTUM_WINDOW_DAYS + 1), openInstant),
        )
        invocations += record(
            day, log, SimulatedSurface.MOMENTUM, momentumFacts,
            engine.observe(momentumFacts, history, Purpose.MOMENTUM_HEADLINE),
            ordinal = null, vetoes = validator.drain(),
        )

        // The areas banner, week to date. Throttled once an hour in the ViewModel per 6.5,
        // which is not the engine's business and so is not simulated here.
        val bannerFacts = extractor.extract(
            TrailWindow(log.startOfDay(weekStartDay(day)), openInstant),
        )
        invocations += record(
            day, log, SimulatedSurface.BANNER, bannerFacts,
            engine.observe(bannerFacts, history, Purpose.AREAS_BANNER),
            ordinal = null, vetoes = validator.drain(),
        )

        // The Pulse, once per local day, describing yesterday.
        val pulseFacts = extractor.extract(TrailWindow(log.startOfDay(day - 1), log.startOfDay(day)))
        val pulse = record(
            day, log, SimulatedSurface.PULSE, pulseFacts,
            engine.observe(pulseFacts, history, Purpose.PULSE),
            ordinal = null, vetoes = validator.drain(),
        )
        invocations += pulse
        recordPulse(persona, log, day, pulse)

        if (isReportDay(day)) {
            invocations += generateReport(persona, log, engine, validator, extractor, day, history)
        }
    }

    /**
     * The Report, per `MASTER_BUILD_PROMPT.md` 11.3: headline first, then two to four
     * observations, then at most one pattern and only with three weeks of data.
     *
     * **Never padded.** One qualifying observation means one observation, and a week with no
     * pattern trend has no pattern section at all rather than a section saying so.
     */
    private fun generateReport(
        persona: SimulationPersona,
        log: SimulatorLog,
        engine: ClarityEngine,
        validator: RecordingValidator,
        extractor: FactExtractor,
        day: Int,
        history: FiringHistory,
    ): List<SimulatedInvocation> {
        val facts = extractor.extract(
            TrailWindow(log.startOfDay(day - REPORT_WINDOW_DAYS), log.startOfDay(day)),
        )
        val out = mutableListOf<SimulatedInvocation>()

        val headlineResult = engine.observe(facts, history, Purpose.REPORT_HEADLINE)
        val headline = record(
            day, log, SimulatedSurface.REPORT_HEADLINE, facts, headlineResult,
            ordinal = HEADLINE_ORDINAL, vetoes = validator.drain(),
        )
        out += headline

        // The observation pass realizes and validates every chosen selection before it
        // returns, so its vetoes arrive as one batch rather than one per observation. They
        // are recorded against the first section for that reason, and never dropped.
        val observations = engine.observeObservations(facts, history, headline.spoken?.familyKey)
        val observationVetoes = validator.drain()
        if (observations.isEmpty()) {
            out += record(
                day, log, SimulatedSurface.REPORT_OBSERVATION, facts,
                engine.observe(facts, history, Purpose.REPORT_OBSERVATION),
                ordinal = FIRST_OBSERVATION_ORDINAL, vetoes = observationVetoes + validator.drain(),
            )
        } else {
            observations.forEachIndexed { index, rendered ->
                out += record(
                    day, log, SimulatedSurface.REPORT_OBSERVATION, facts,
                    EngineResult.Spoke(rendered),
                    ordinal = FIRST_OBSERVATION_ORDINAL + index,
                    vetoes = if (index == 0) observationVetoes else emptyList(),
                )
            }
        }

        // At most one pattern, and only with three weeks of data. 6.3.
        val patternResult = if (facts.history.weeksOfData >= PATTERN_MIN_WEEKS) {
            engine.observe(facts, history, Purpose.REPORT_PATTERN)
        } else {
            null
        }
        if (patternResult != null) {
            out += record(
                day, log, SimulatedSurface.REPORT_PATTERN, facts, patternResult,
                ordinal = PATTERN_ORDINAL, vetoes = validator.drain(),
            )
        }

        val pattern = (patternResult as? EngineResult.Spoke)?.output
        writeReport(log, day, headline.spoken, observations, pattern)
        if (persona.acceptsEveryPlan) writeAcceptedPlan(log, day, observations)
        return out
    }

    // ------------------------------------------------------------ recording the log

    /** Writes `PULSE_GENERATED`, and `PULSE_ANSWERED` when the person answers. */
    private fun recordPulse(
        persona: SimulationPersona,
        log: SimulatorLog,
        day: Int,
        invocation: SimulatedInvocation,
    ) {
        val spoken = invocation.spoken ?: return
        val pulseId = "pulse-${log.dateKey(day)}"
        log.add(
            log.at(day, PULSE_HOUR),
            PulseGenerated(
                pulseId = pulseId,
                dateKey = log.dateKey(day),
                family = spoken.familyKey,
                escalationStage = spoken.stage,
                register = spoken.register.name,
                variantKey = spoken.variantKey,
                renderedObservation = spoken.statement,
                renderedQuestion = spoken.question,
                factSnapshot = spoken.factSnapshot,
                // The open is in the morning, so the observation describes yesterday.
                reflectionPeriod = ReflectionPeriod.YESTERDAY,
                subjectId = spoken.subjectId,
                subjectKind = spoken.subjectKind,
            ),
        )
        if (!persona.answersPulse(day) || spoken.responses.isEmpty()) return
        val chosen = spoken.responses[StableHash.bucket("${persona.key}|$day|response", spoken.responses.size)]
        log.add(
            log.at(day, ANSWER_HOUR),
            PulseAnswered(
                pulseId = pulseId,
                responseKey = chosen.key,
                responseLabel = chosen.label,
                responseIsPositive = chosen.isPositive,
                subjectId = spoken.subjectId,
                subjectKind = spoken.subjectKind,
            ),
        )
    }

    /** Writes `REPORT_GENERATED` with every section that actually appeared. */
    private fun writeReport(
        log: SimulatorLog,
        day: Int,
        headline: SpokenLine?,
        observations: List<RenderedOutput>,
        pattern: RenderedOutput?,
    ) {
        val weekStartKey = log.dateKey(day - REPORT_WINDOW_DAYS)
        val sections = buildList<ReportSectionSnapshot> {
            observations.forEach { add(sectionOf("observation", it.meta)) }
            pattern?.let { add(sectionOf("pattern", it.meta)) }
        }
        log.add(
            log.at(day, REPORT_HOUR),
            ReportGenerated(
                reportId = "report-$weekStartKey",
                weekStartKey = weekStartKey,
                headlineKey = headline?.familyKey ?: NO_HEADLINE,
                renderedSections = sections,
                factSnapshot = emptyMap(),
                headlineVariantKey = headline?.variantKey,
            ),
        )
    }

    /**
     * The plan the plan-accepting persona accepts and never acts on.
     *
     * **The simulator writes this, not the engine.** Layer 6 is phase 9b and
     * CLARITY_LOGIC_ENGINE.md 2 puts it outside layers 1 to 5, so nothing yet composes a
     * plan. What the log needs is the shape: three real corpus keys from
     * `CORPUS_2_REPORT.md` 4, a family that actually appeared in this report per composition
     * rule 10.4, and an acceptance. `FiringHistory` reads exactly those keys.
     *
     * `offeredLine` and `committedLine` are **not** sentences. Composing one here would mean
     * writing a plan outside the engine and outside the corpus, which
     * `MASTER_BUILD_PROMPT.md` 11.1 forbids without exception, so both carry a marker
     * instead. The non-compliance check asserts that marker never reaches the dump, which is
     * the same assertion in the other direction: if a plan line ever appears in a year of
     * this persona's output, something composed one.
     */
    private fun writeAcceptedPlan(log: SimulatorLog, day: Int, observations: List<RenderedOutput>) {
        val motivating = observations.firstOrNull()?.meta ?: return
        val weekStartKey = log.dateKey(day - REPORT_WINDOW_DAYS)
        val planId = "plan-$weekStartKey"
        log.add(
            log.at(day, REPORT_HOUR),
            PlanOffered(
                planId = planId,
                weekStartKey = weekStartKey,
                frameKey = PLAN_FRAME_KEY,
                cueKey = PLAN_CUE_KEY,
                actionKey = PLAN_ACTION_KEY,
                familyKey = motivating.familyKey,
                subjectId = motivating.subjectId,
                offeredLine = PLAN_LINE_MARKER,
                committedLine = PLAN_LINE_MARKER,
                resolutionFactRef = PLAN_RESOLUTION_FACT,
            ),
        )
        log.add(log.at(day, PLAN_ACCEPT_HOUR), PlanAccepted(planId))
    }

    // ------------------------------------------------------------ one invocation

    private fun record(
        day: Int,
        log: SimulatorLog,
        surface: SimulatedSurface,
        facts: FactSet,
        result: EngineResult,
        ordinal: Int?,
        vetoes: List<String>,
    ): SimulatedInvocation = SimulatedInvocation(
        day = day,
        dateKey = log.dateKey(day),
        surface = surface,
        ordinal = ordinal,
        spoken = (result as? EngineResult.Spoke)?.let { spokenLine(it.output) },
        silence = (result as? EngineResult.Silent)?.reason,
        vetoes = vetoes,
        areaEventsInWindow = facts.areas.mapValues { it.value.eventsInWindow },
    )

    /**
     * Everything the dump prints about one sentence.
     *
     * The criteria come back out of the catalog by rule key rather than being carried on the
     * candidate, because `ClarityRule` holds no strings and `Criterion.describe` is plain
     * English written for exactly this. That is the `fired:` line in section 12's format,
     * and it is the line that lets a reader tell a rule that fired for its own reason from
     * one that fired because a criterion was padding.
     */
    private fun spokenLine(output: RenderedOutput): SpokenLine {
        val candidate = output.meta
        val rule = rulesByKey[candidate.ruleKey]
        return SpokenLine(
            ruleKey = candidate.ruleKey,
            familyKey = candidate.familyKey,
            stage = candidate.stage,
            register = candidate.register,
            lengthBand = candidate.lengthBand,
            variantKey = candidate.variantKey,
            fired = rule?.criteria?.map { it.describe } ?: emptyList(),
            facts = factTrail(candidate),
            factSnapshot = factSnapshot(candidate),
            statement = output.text,
            question = output.question,
            responses = output.responses,
            subjectId = candidate.subjectId,
            subjectKind = candidate.payloadSubjectKind(),
            namedAreaIds = candidate.namedAreaIds,
            namedItemIds = candidate.namedItemIds,
        )
    }

    /**
     * The `facts:` line, one entry per slot, with the `FactRef` behind every number.
     *
     * Section 12's format is `name=value`, and the bracketed reference is added to it rather
     * than replacing it. Every rendered number carries a `FactRef` so the validator can
     * re-read the fact and compare, per check 3, and printing it is what lets a reader of
     * the dump follow a number back to the query it came from without opening the code.
     */
    private fun factTrail(candidate: Candidate): List<String> =
        candidate.slots.entries.sortedBy { it.key }.map { (key, slot) ->
            val value = slotValue(slot)
            val ref = candidate.sourceFacts[key]
            if (ref == null) "$key=$value" else "$key=$value [${ref.category}.${ref.path}]"
        }

    /** The same values as a map, for the `PULSE_GENERATED` payload's fact snapshot. */
    private fun factSnapshot(candidate: Candidate): Map<String, String> =
        candidate.slots.entries.associate { (key, slot) -> key to slotValue(slot) }

    private fun slotValue(slot: Slot): String = when (slot) {
        is Slot.Text -> slot.value
        is Slot.Count -> slot.value.toString()
        is Slot.Days -> slot.value.toString()
        is Slot.Percent -> slot.value.toString()
        is Slot.DateRef -> slot.weekKey
    }

    private fun sectionOf(sectionKey: String, candidate: Candidate) = ReportSectionSnapshot(
        sectionKey = sectionKey,
        sidehead = sectionKey,
        text = candidate.rendered,
        familyKey = candidate.familyKey,
        variantKey = candidate.variantKey,
        escalationStage = candidate.stage,
        register = candidate.register.name,
        subjectId = candidate.subjectId,
        subjectKind = candidate.payloadSubjectKind(),
    )

    /** Sunday, and never the first one, because a report needs a week behind it. */
    private fun isReportDay(day: Int): Boolean = day >= DAYS_PER_WEEK && day % DAYS_PER_WEEK == 0

    private fun weekStartDay(day: Int): Int = day - (day % DAYS_PER_WEEK)

    companion object {

        /**
         * Not UTC, deliberately.
         *
         * An implementation that quietly reached for `ZoneId.systemDefault()` or assumed a
         * zero offset would agree with a UTC simulation on every assertion and be wrong on a
         * phone. This zone also carries two daylight saving transitions inside the simulated
         * year, which is the boundary case section 14 asks for by name.
         */
        val DEFAULT_ZONE: ZoneId = ZoneId.of("America/New_York")

        /** A Sunday, so the report week and the simulated day zero begin together. */
        val DEFAULT_START_DATE: LocalDate = LocalDate.of(2026, 1, 4)

        /** A full simulated year, per section 12. */
        const val DAYS_IN_YEAR = 365

        private const val DAYS_PER_WEEK = 7
        private const val MOMENTUM_WINDOW_DAYS = 14
        private const val REPORT_WINDOW_DAYS = 7
        private const val PATTERN_MIN_WEEKS = 3

        /** Where each section sits in a report: the headline, then observations, then a pattern. */
        private const val HEADLINE_ORDINAL = 0
        private const val FIRST_OBSERVATION_ORDINAL = 1
        private const val PATTERN_ORDINAL = 5

        private const val OPEN_HOUR = 7
        private const val PULSE_HOUR = 8
        private const val REPORT_HOUR = 8
        private const val PLAN_ACCEPT_HOUR = 9
        private const val ANSWER_HOUR = 20

        /** The family key recorded when a week produced no headline at all. */
        private const val NO_HEADLINE = "none"

        // Three real keys from CORPUS_2_REPORT.md 4.1, 4.2 and 4.3. Keys only: the lines
        // themselves belong to layer 6, which does not exist yet.
        private const val PLAN_FRAME_KEY = "frm.01"
        private const val PLAN_CUE_KEY = "cue.hab.06"
        private const val PLAN_ACTION_KEY = "act.fin.01"

        /**
         * What goes where a rendered plan would go, and why it is not a sentence.
         *
         * Deliberately unreadable as English. If this string ever appears in a dump,
         * something outside the engine composed a plan.
         */
        const val PLAN_LINE_MARKER = "<<layer6-not-built>>"

        private val PLAN_RESOLUTION_FACT = FactRef("area", "completionsInWindow")
    }
}

/**
 * The engine's subject kind, as the event payload spells it.
 *
 * Two enums with the same name and the same two constants exist on purpose: the engine's
 * lives in `domain.engine.catalog` and the payload's in `data.event`, and neither package
 * depends on the other. The translation is here rather than anywhere a rule could reach it.
 */
private fun Candidate.payloadSubjectKind(): SubjectKind? = when {
    subjectId == null -> null
    subjectId in namedItemIds -> SubjectKind.ITEM
    else -> SubjectKind.AREA
}

/**
 * The validator, with the vetoes kept.
 *
 * `ClarityEngine` holds a [CandidateValidator] it cannot inspect, which is the seam that
 * makes bypassing layer 5 impossible, and the price is that the engine's own return value
 * says only that everything was vetoed rather than by which check. A dump that could not
 * name the check would be a dump in which a rule qualifying on facts its sentences cannot
 * describe looks exactly like a quiet week.
 *
 * **It delegates and records. It never decides.** The veto is `ClarityValidator`'s answer,
 * unchanged, which is what keeps this from being the bypass 11.4 forbids.
 */
private class RecordingValidator(private val delegate: ClarityValidator) : CandidateValidator {

    private val vetoes = mutableListOf<String>()

    override fun veto(candidate: Candidate, facts: FactSet): String? =
        when (val result = delegate.validate(candidate, facts)) {
            is ValidationResult.Passed -> null
            is ValidationResult.Vetoed -> {
                vetoes += "${result.candidate.variantKey} ${result.check}: ${result.detail}"
                "${result.check}: ${result.detail}"
            }
        }

    /** Everything vetoed since the last call, and clears the buffer. */
    fun drain(): List<String> {
        val out = vetoes.toList()
        vetoes.clear()
        return out
    }
}

/** One persona's simulated year. */
data class SimulationRun(
    val persona: SimulationPersona,
    val days: Int,
    val openDays: Int,
    val eventCount: Int,
    val invocations: List<SimulatedInvocation>,
) {
    /** Every invocation on one surface, in the order it happened. */
    fun of(surface: SimulatedSurface): List<SimulatedInvocation> =
        invocations.filter { it.surface == surface }
}

/** The six surfaces the engine speaks on, named as section 12's dump names them. */
enum class SimulatedSurface(val label: String) {
    PULSE("pulse"),
    MOMENTUM("momentum"),
    BANNER("banner"),
    REPORT_HEADLINE("report headline"),
    REPORT_OBSERVATION("report observation"),
    REPORT_PATTERN("report pattern"),
}

/**
 * One invocation of the engine, spoken or silent.
 *
 * [areaEventsInWindow] is carried rather than recomputed because the check that no sentence
 * names an area with no events in its window has to compare against **that** window, and the
 * fact set it came from is gone by the time the dump is read.
 */
data class SimulatedInvocation(
    val day: Int,
    val dateKey: String,
    val surface: SimulatedSurface,
    /** Position within a report: 0 the headline, 1 to 4 the observations, 5 the pattern. */
    val ordinal: Int?,
    val spoken: SpokenLine?,
    val silence: SilenceReason?,
    /** Candidates layer 5 rejected before this one, with the check that rejected each. */
    val vetoes: List<String>,
    val areaEventsInWindow: Map<String, Int>,
)

/** A sentence, with everything that chose it. */
data class SpokenLine(
    val ruleKey: String,
    val familyKey: String,
    val stage: Int,
    val register: Register,
    val lengthBand: LengthBand,
    val variantKey: String,
    /** `Criterion.describe` for every criterion of the rule that fired. */
    val fired: List<String>,
    /** `slotName=value [factRef]`, one per filled slot. */
    val facts: List<String>,
    val factSnapshot: Map<String, String>,
    val statement: String,
    val question: String?,
    val responses: List<ResponseOption>,
    val subjectId: String?,
    val subjectKind: SubjectKind?,
    val namedAreaIds: Set<String>,
    val namedItemIds: Set<String>,
)
