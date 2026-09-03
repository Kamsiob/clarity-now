package com.kamsiob.claritynow.devtools

import com.kamsiob.claritynow.data.event.PlanAccepted
import com.kamsiob.claritynow.data.event.PlanOffered
import com.kamsiob.claritynow.data.event.PulseAnswered
import com.kamsiob.claritynow.data.event.PulseGenerated
import com.kamsiob.claritynow.data.event.ReflectionPeriod
import com.kamsiob.claritynow.data.event.ReportGenerated
import com.kamsiob.claritynow.domain.report.ClarityReport
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
import com.kamsiob.claritynow.domain.engine.Validated
import com.kamsiob.claritynow.domain.engine.catalog.ClarityCatalog
import com.kamsiob.claritynow.domain.engine.catalog.LengthBand
import com.kamsiob.claritynow.domain.engine.catalog.Purpose
import com.kamsiob.claritynow.domain.engine.catalog.Register
import com.kamsiob.claritynow.domain.engine.catalog.ResponseOption
import com.kamsiob.claritynow.domain.engine.catalog.LengthBands
import com.kamsiob.claritynow.domain.engine.realize.Candidate
import com.kamsiob.claritynow.domain.engine.realize.Slot
import com.kamsiob.claritynow.domain.engine.validate.ClarityValidator
import com.kamsiob.claritynow.domain.engine.validate.ValidationResult
import com.kamsiob.claritynow.domain.guidance.ClarityPlan
import com.kamsiob.claritynow.domain.guidance.FollowThrough
import com.kamsiob.claritynow.domain.guidance.GuidanceComposer
import com.kamsiob.claritynow.domain.guidance.GuidanceResult
import com.kamsiob.claritynow.domain.guidance.PlanHistory
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
    /**
     * Called with the fact set behind every invocation, before anything is decided about it.
     *
     * **A [SimulationRun] keeps what the engine said and throws away what it was looking
     * at.** That is the right shape for the dump, which is about output, and it is the wrong
     * shape for the one question the corpus phase has to answer about every authored line:
     * given facts a real life actually produced, can this line be filled and does layer 5
     * believe it. There is nowhere else those fact sets exist. Layer one builds them from a
     * log that grew as the engine spoke into it, so they cannot be reconstructed afterwards
     * from the run, and a second simulator written to capture them would be a second day
     * loop to drift from this one.
     *
     * So the fact set is handed out as it is built, and the default does nothing. A caller
     * that wants none pays nothing and a caller that wants a sample keeps what it needs
     * rather than the year: eleven persona years hold roughly thirty thousand fact sets and
     * no reading needs all of them at once.
     */
    private val onFacts: (Int, SimulatedSurface, FactSet) -> Unit = { _, _, _ -> },
) {

    /** Every rule by key, so the dump can print the criteria that fired. */
    private val rulesByKey = catalog.rules.associateBy { it.key }

    /**
     * Layer 6, built once for the run. CLARITY_LOGIC_ENGINE.md 10.
     *
     * The same object the Report screen builds, over the same catalog and the same zone,
     * so a plan a persona is offered here is a plan a device would offer.
     */
    private val guidance = GuidanceComposer(catalog, zone)

    /**
     * Layer 5 again, and for the same one purpose `ReportComposer` needs it for: minting
     * the `Validated` layer 6 takes.
     *
     * 10.4 rule 2 rests on layer 6 being unable to see an observation that did not appear,
     * and the type is what enforces it. `RecordingValidator` above wraps the same checks
     * and answers a reason rather than a token, because that is the seam the engine loop
     * takes. This is the plain one.
     */
    private val minting = ClarityValidator(zone)

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

        // A day nobody was there is a day nothing is written. `SimulationPersona.isPresentOn`
        // carries the whole test and the reasoning behind it: an ITEM_ADDED on a day with no
        // APP_OPENED is an event the app cannot produce, and the areas are created inside
        // the first session rather than beside it.
        for (day in 0 until days) {
            if (!persona.isPresentOn(day)) continue
            if (day == persona.installDay) persona.setUp(log)
            openDays++
            log.opened(day, OPEN_HOUR)
            openTheApp(persona, log, engine, validator, day, invocations)
            persona.act(log, day)
        }

        return SimulationRun(
            persona = persona,
            catalog = catalog,
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
        // Layer 6's follow through, rebuilt from the log like everything else. An offer with
        // no acceptance beside it produces no entry at all, so a persona that declines every
        // plan reaches the ranking with an empty set and is indistinguishable from one that
        // was never offered anything.
        val plans = PlanHistory.from(queries, openInstant)

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
            invocations += generateReport(persona, log, engine, validator, extractor, day, history, plans)
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
        plans: PlanHistory,
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
        val observations = engine.observeObservations(
            facts,
            history,
            headline.spoken?.familyKey,
            boosted = FollowThrough.boosted(plans, log.dateKey(day - REPORT_WINDOW_DAYS)),
        )
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
        // **The closing is composed before the report is written, issue #60.** It used to
        // be the other way round, and the order was the whole defect: a closing with no
        // plan in it was recorded nowhere, so `FiringHistory` could never hold a `cls.*`
        // key, `VariantChoice` always took its fresh branch, and a person whose weeks kept
        // their shape met the same eight line bench every week.
        val closing = closing(
            persona = persona,
            log = log,
            day = day,
            facts = facts,
            headline = (headlineResult as? EngineResult.Spoke)?.output?.meta,
            observations = observations,
            plans = plans,
            history = history,
        )
        writeReport(log, day, headline.spoken, observations, pattern, closing.recorded)
        out += closing.invocations
        return out
    }

    /**
     * Layer 6, on the observations that actually appeared. 11.3 steps 7 and 8.
     *
     * **The engine composes the plan and the persona answers it**, which is the arrangement
     * the non compliance test needs and the one this file did not have until layer 6
     * existed. The plan the accepting persona accepts is now a real plan, assembled from
     * `CORPUS_2_REPORT.md` 4 by `GuidanceComposer`, filed under a real `PLAN_OFFERED` and
     * accepted with a real `PLAN_ACCEPTED`. Until phase 9b it was three corpus keys and a
     * marker where a sentence would go, because nothing could compose one.
     *
     * A week that produced no closing at all records no invocation, which is what
     * `SimulationChecks.layerSixSilence` counts against the number of reports.
     */
    private fun closing(
        persona: SimulationPersona,
        log: SimulatorLog,
        day: Int,
        facts: FactSet,
        headline: Candidate?,
        observations: List<RenderedOutput>,
        plans: PlanHistory,
        history: FiringHistory,
    ): ClosingOutcome {
        val weekStartKey = log.dateKey(day - REPORT_WINDOW_DAYS)
        val result = guidance.compose(
            headline = headline?.let { minted(facts, it) },
            appeared = observations.mapNotNull { minted(facts, it.meta) },
            facts = facts,
            plans = plans,
            history = history,
            weekStartKey = weekStartKey,
        )
        val line = when (result) {
            is GuidanceResult.Plan -> result.plan.offeredLine
            is GuidanceResult.Closing -> result.line.text
            is GuidanceResult.Nothing -> return ClosingOutcome.NONE
        }
        // The real corpus key, so 7.6's ninety day repetition reading is about the line a
        // person would recognize rather than about a constant. A plan is three authored
        // lines and has no single key, so it records the three it used.
        val variantKey = when (result) {
            is GuidanceResult.Plan ->
                "${result.plan.frameKey}+${result.plan.cueKey}+${result.plan.actionKey}"
            is GuidanceResult.Closing -> result.line.meta.variantKey
            is GuidanceResult.Nothing -> return ClosingOutcome.NONE
        }
        val plan = (result as? GuidanceResult.Plan)?.plan
        if (plan != null && persona.acceptsEveryPlan) writeAcceptedPlan(log, day, plan)
        // Once per recorded invocation, exactly as [record] does it. `CorpusRenderGate`
        // harvests the fact set behind every line the simulator produced and checks that
        // the hook fired as often as the dump has entries, so a surface recorded without
        // it makes that harvest quietly incomplete.
        onFacts(day, SimulatedSurface.REPORT_CLOSING, facts)
        return ClosingOutcome(
            invocations = listOf(
                SimulatedInvocation(
                    day = day,
                    dateKey = log.dateKey(day),
                    surface = SimulatedSurface.REPORT_CLOSING,
                    ordinal = CLOSING_ORDINAL,
                    spoken = closingLine(line, variantKey, plan),
                    silence = null,
                    vetoes = emptyList(),
                    areaEventsInWindow = facts.areas.mapValues { it.value.eventsInWindow },
                ),
            ),
            recorded = (result as? GuidanceResult.Closing)?.line?.meta,
        )
    }

    /**
     * What layer 6 produced, and the half of it that goes into the log. Issue #60.
     *
     * [recorded] is the candidate behind a closing with no plan in it, and null for a plan
     * or for silence. A plan's keys reach `FiringHistory` through `PLAN_OFFERED`, which the
     * simulator already writes; a closing had no route into the log at all, which is the
     * defect this type exists to close.
     */
    private data class ClosingOutcome(
        val invocations: List<SimulatedInvocation>,
        val recorded: Candidate?,
    ) {
        companion object {
            val NONE = ClosingOutcome(invocations = emptyList(), recorded = null)
        }
    }

    /** Layer 5's proof that [candidate] may be shown, which is what layer 6 takes. */
    private fun minted(facts: FactSet, candidate: Candidate): Validated? =
        (minting.validate(candidate, facts) as? ValidationResult.Passed)?.validated

    /** The dump's record of one closing. See [SimulatedSurface.REPORT_CLOSING]. */
    private fun closingLine(text: String, variantKey: String, plan: ClarityPlan?): SpokenLine = SpokenLine(
        ruleKey = if (plan == null) GUIDANCE_CLOSING_RULE else GUIDANCE_PLAN_RULE,
        // **Never the motivating family**, and this is the one field where that matters.
        // Every coverage reading in `SimulationAggregate` counts by `(purpose, familyKey)`,
        // so recording `neglectedArea` here would add a firing that family did not have and
        // could carry it over the one fifth share ceiling on the strength of a closing line.
        // The family is in [fired] instead, which the dump prints and nothing counts.
        familyKey = if (plan == null) GUIDANCE_CLOSING_RULE else GUIDANCE_PLAN_RULE,
        stage = 1,
        register = Register.PLAIN,
        lengthBand = LengthBands.bandFor(text),
        variantKey = variantKey,
        fired = listOfNotNull(plan?.let { "motivated by ${it.familyKey}" }),
        facts = emptyList(),
        factSnapshot = emptyMap(),
        statement = text,
        question = null,
        maskedStatement = text,
        maskedQuestion = null,
        responses = emptyList(),
        subjectId = plan?.subjectId,
        subjectKind = null,
        namedAreaIds = emptySet(),
        namedItemIds = emptySet(),
    )

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
        closing: Candidate?,
    ) {
        val weekStartKey = log.dateKey(day - REPORT_WINDOW_DAYS)
        val sections = buildList<ReportSectionSnapshot> {
            observations.forEach { add(sectionOf("observation", it.meta)) }
            pattern?.let { add(sectionOf("pattern", it.meta)) }
            // Issue #60, and only a closing with no plan in it. A plan's three keys are on
            // `PLAN_OFFERED`, which is where 7.6 already reads them from, and this is the
            // same rule `ClarityReport.payload` states at the same place in the app.
            closing?.let { add(sectionOf(ClarityReport.CLOSING_SECTION_KEY, it)) }
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
     * The plan the plan accepting persona accepts and never acts on.
     *
     * **The plan is the engine's now, and that is the whole point of this persona.** Until
     * layer 6 existed the simulator composed the shape by hand, three real corpus keys and
     * a marker where a sentence would go, because nothing could produce one and writing a
     * sentence here would have been composing a plan outside the corpus. It writes the real
     * offer and the real acceptance, so `PlanHistory` reads what a device would read and
     * the follow through in 10.6 is exercised rather than described.
     *
     * The persona never acts on any of it, which is what makes the year it produces the
     * evidence section 12 asks for: a real, visible, repeated non compliance, with nothing
     * in the dump that references it.
     */
    private fun writeAcceptedPlan(log: SimulatorLog, day: Int, plan: ClarityPlan) {
        log.add(
            log.at(day, REPORT_HOUR),
            PlanOffered(
                planId = plan.id,
                weekStartKey = plan.weekStartKey,
                frameKey = plan.frameKey,
                cueKey = plan.cueKey,
                actionKey = plan.actionKey,
                familyKey = plan.familyKey,
                subjectId = plan.subjectId,
                offeredLine = plan.offeredLine,
                committedLine = plan.committedLine,
                resolutionFactRef = plan.resolutionFactRef,
            ),
        )
        log.add(log.at(day, PLAN_ACCEPT_HOUR), PlanAccepted(plan.id))
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
    ): SimulatedInvocation {
        onFacts(day, surface, facts)
        return invocation(day, log, surface, facts, result, ordinal, vetoes)
    }

    private fun invocation(
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
            maskedStatement = ClarityValidator.maskPersonalStrings(output.text, candidate.slots.values),
            maskedQuestion = output.question?.let {
                ClarityValidator.maskPersonalStrings(it, candidate.slots.values)
            },
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
        private const val CLOSING_ORDINAL = 6

        private const val OPEN_HOUR = 7
        private const val PULSE_HOUR = 8
        private const val REPORT_HOUR = 8
        private const val PLAN_ACCEPT_HOUR = 9
        private const val ANSWER_HOUR = 20

        /** The family key recorded when a week produced no headline at all. */
        private const val NO_HEADLINE = "none"

        /** What a closing invocation records instead of a rule key. */
        const val GUIDANCE_PLAN_RULE = "guidance.plan"
        const val GUIDANCE_CLOSING_RULE = "guidance.closing"
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

/**
 * One persona's simulated year.
 *
 * [catalog] is the one the year was run against, carried because a reading of what **did
 * not** happen needs it. A firing count taken from the invocations alone can only list the
 * families that fired, which is how phase 5's "six of eleven Pulse families ever fired"
 * stayed a sentence somebody remembered rather than a number something watched: the five
 * that did not fire leave no trace in a year of output. The catalog is where they are, so
 * it travels with the run rather than being fetched again beside it and drifting.
 *
 * All eleven fire today, and that is the point rather than a reason to drop this. The
 * reading only became watchable once something held the denominator, and what it then
 * caught was that the quiet ones were quiet because of the persona set rather than the
 * catalog.
 */
data class SimulationRun(
    val persona: SimulationPersona,
    val catalog: ClarityCatalog,
    val days: Int,
    val openDays: Int,
    val eventCount: Int,
    val invocations: List<SimulatedInvocation>,
) {
    /** Every invocation on one surface, in the order it happened. */
    fun of(surface: SimulatedSurface): List<SimulatedInvocation> =
        invocations.filter { it.surface == surface }
}

/**
 * The seven surfaces the engine speaks on, named as section 12's dump names them.
 *
 * [purpose] is carried rather than derived at each call site, because counting how many of
 * a purpose's families ever fired means putting a firing back beside the families the
 * catalog declares for it, and a surface is the only thing an invocation records. The
 * mapping is one to one for the six that select a rule, and is the same one
 * [ClaritySimulator.openTheApp] makes on the way in, so a surface that ever stopped
 * agreeing with the purpose it was invoked with would make every coverage reading quietly
 * wrong.
 *
 * **[REPORT_CLOSING] is the exception and it is safe rather than tolerated.** Layer 6
 * selects no rule and a closing is not a `Purpose`, so the value it carries is the nearest
 * one rather than a claim. Nothing keyed by `(purpose, family)` can see it, because the
 * family key of a closing invocation is never a family any catalog declares: it is
 * `guidance.plan` or `guidance.closing`. `ClaritySimulator.closingLine` says so where the
 * field is set, which is where somebody would otherwise put the motivating family.
 */
enum class SimulatedSurface(val label: String, val purpose: Purpose) {
    PULSE("pulse", Purpose.PULSE),
    MOMENTUM("momentum", Purpose.MOMENTUM_HEADLINE),
    BANNER("banner", Purpose.AREAS_BANNER),
    REPORT_HEADLINE("report headline", Purpose.REPORT_HEADLINE),
    REPORT_OBSERVATION("report observation", Purpose.REPORT_OBSERVATION),
    REPORT_PATTERN("report pattern", Purpose.REPORT_PATTERN),

    /**
     * Layer 6's closing line. `CORPUS_2_REPORT.md` 4 and CLARITY_LOGIC_ENGINE.md 10.
     *
     * It carries the Report's observation purpose because a closing is not a `Purpose` of
     * its own; 8 check 9 gives it its own word limit and `LengthLimits.CLOSING_MAX_WORDS`
     * is where that lives.
     *
     * **What reaches the dump is the offered line and never the committed one.** The dump
     * is the record of what the app said about a person, and the committed line is what the
     * person said about themselves: it exists only because they tapped `I'll do that` and
     * it is shown to nobody else. Keeping it out is not a gap in the non compliance test,
     * which reads both through `GuidanceNonComplianceTest`; it is what stops the dump from
     * being a place a reader could count acceptances.
     */
    REPORT_CLOSING("report closing", Purpose.REPORT_OBSERVATION),
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
    /**
     * The same two strings with the person's own area names, item titles and tapped labels
     * replaced, exactly as `ClarityValidator` masks them before checks 7, 8 and 10.
     *
     * Carried rather than recomputed because the vocabulary check in `SimulationChecks` is
     * a claim about the words the app chose, and the rendered form contains words the person
     * chose. See `ClarityValidator.maskPersonalStrings`.
     */
    val maskedStatement: String,
    val maskedQuestion: String?,
    val responses: List<ResponseOption>,
    val subjectId: String?,
    val subjectKind: SubjectKind?,
    val namedAreaIds: Set<String>,
    val namedItemIds: Set<String>,
)
