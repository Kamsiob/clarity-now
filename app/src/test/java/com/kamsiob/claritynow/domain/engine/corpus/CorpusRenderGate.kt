package com.kamsiob.claritynow.domain.engine.corpus

import com.kamsiob.claritynow.devtools.ClaritySimulator
import com.kamsiob.claritynow.devtools.SimulatedSurface
import com.kamsiob.claritynow.devtools.SimulationPersona
import com.kamsiob.claritynow.domain.engine.FactSet
import com.kamsiob.claritynow.domain.engine.FamilyKey
import com.kamsiob.claritynow.domain.engine.catalog.ClarityCatalog
import com.kamsiob.claritynow.domain.engine.catalog.ClarityRule
import com.kamsiob.claritynow.domain.engine.catalog.Purpose
import com.kamsiob.claritynow.domain.engine.catalog.Subject
import com.kamsiob.claritynow.domain.engine.catalog.SubjectKind
import com.kamsiob.claritynow.domain.engine.catalog.Variant
import com.kamsiob.claritynow.domain.engine.realize.SlotBindings
import com.kamsiob.claritynow.domain.engine.validate.ClarityValidator
import com.kamsiob.claritynow.domain.engine.validate.ValidationResult
import java.time.ZoneId

/**
 * Gate 6. Every authored line, rendered against facts eleven real lives produced, and put
 * in front of layer 5.
 *
 * ## The failure this exists for
 *
 * A line whose slot has no binding is not a crash and not a blank. `Realizer.fill` drops it
 * from the bench and takes another line, per 7.2's slot completeness rule, and the family
 * goes on speaking. So a family can be authored to sixty lines, look complete in the file,
 * and be reaching only forty of them, forever, with nothing on any screen and nothing in
 * any test to say so. The same is true of a line layer 5 always vetoes: check 3 re-reads
 * every number and a line whose number does not re-read is silently never said.
 *
 * That is the exact defect phase 9 is most likely to introduce, because an author writing
 * forty lines for one stage will reach for a fact the stage does not carry, and the corpus
 * file has no way to show it.
 *
 * ## How it is measured
 *
 * The simulator is run once per persona with a hook that hands out the fact set behind
 * every invocation. For each of them, step 1 of layer 3 is repeated, which is every rule of
 * that surface's purpose against every subject its selector yields, and up to
 * [SAMPLES_PER_STAGE] qualifying `(facts, subject)` pairs are kept per family stage. Then
 * every line in the corpus is filled against its own stage's samples and handed to
 * `ClarityValidator`.
 *
 * ## Three outcomes, and only one of them is a failure
 *
 * A line that fills and passes layer 5 at least once is **renderable**, and that is all this
 * gate asks: a line that works on one real day is a line the engine can reach.
 *
 * A line whose stage never qualified anywhere in eleven simulated years is **unreached**,
 * and it is reported rather than failed. Six families are dark in this persona set for
 * reasons the sixth measurement records, and it would be dishonest to call their language
 * broken on evidence that does not exist.
 *
 * A line whose stage did qualify and which could never be filled or never passed layer 5 is
 * **unrenderable**, and that is the finding. It names the marker that stopped it or the
 * check that vetoed it.
 */
internal object CorpusRenderGate {

    /**
     * How many qualifying fact sets are kept per family stage, and they are kept by shape
     * rather than by arrival.
     *
     * **Twenty four distinct shapes, not twenty four consecutive days.** A stage that
     * qualifies on Monday usually qualifies on Tuesday with almost the same numbers, so the
     * first six qualifying fact sets are frequently six views of one situation. A line
     * needing a completion count would then be reported as unrenderable because the six days
     * that happened to be sampled had no completions, which is a fact about the sample and
     * not about the line. [shapeOf] is what a fact set is deduplicated by, and only a shape
     * not already held is kept.
     */
    const val SAMPLES_PER_STAGE = 24

    /** The zone the simulated year runs in, so the gate reads dates the way the run wrote them. */
    val ZONE: ZoneId = ClaritySimulator.DEFAULT_ZONE

    /** One real moment at which a family stage had something to say. */
    data class Sample(
        val facts: FactSet,
        val rule: ClarityRule,
        val subject: Subject?,
        val callbackLabel: String?,
        val persona: String,
        val dateKey: String,
    )

    /** What one pass over eleven persona years produced. */
    data class Harvest(
        val samples: Map<String, List<Sample>>,
        val factSetsSeen: Int,
        val spokenReproduced: Int,
        val spokenSkipped: Int,
        val drift: List<GateFinding>,
        val firings: Map<Pair<Purpose, FamilyKey>, Int>,
    )

    /** The sample table's key: one family stage at one purpose. */
    fun stageKey(purpose: Purpose, family: FamilyKey, stage: Int): String = "$purpose|$family|s$stage"

    // ------------------------------------------------------------------ the harvest

    /**
     * Runs every persona's year, keeping the fact sets and checking this package's fill loop
     * against the realizer's on the way past.
     *
     * **One persona at a time, with its own capture list**, rather than one call to
     * `runAll`. The capture arrives in the order `ClaritySimulator.record` is called, which
     * is the order invocations are appended, so the two lists line up index for index. Doing
     * it per persona means that alignment is asserted eleven times over short lists instead
     * of once over a long one, and a break in it is caught where it happened.
     */
    fun harvest(catalog: ClarityCatalog): Harvest {
        val variants = catalog.allVariants.associateBy { it.key }
        val samples = mutableMapOf<String, MutableList<Sample>>()
        val shapes = mutableMapOf<String, MutableSet<List<Int>>>()
        val needed = Purpose.entries.associateWith { catalog.rulesFor(it).toMutableList() }
        val firings = mutableMapOf<Pair<Purpose, FamilyKey>, Int>()
        val drift = mutableListOf<GateFinding>()
        var seen = 0
        var reproduced = 0
        var skipped = 0

        for (persona in SimulationPersona.ALL) {
            val captured = mutableListOf<Pair<SimulatedSurface, FactSet>>()
            val simulator = ClaritySimulator(
                catalog = catalog,
                onFacts = { _, surface, facts -> captured += surface to facts },
            )
            val run = simulator.run(persona)
            check(captured.size == run.invocations.size) {
                "the fact hook fired ${captured.size} times for ${run.invocations.size} invocations of " +
                    "${persona.key}, so ClaritySimulator no longer calls it once per recorded invocation"
            }
            for ((index, invocation) in run.invocations.withIndex()) {
                val (surface, facts) = captured[index]
                seen++
                collect(
                    purpose = surface.purpose,
                    facts = facts,
                    needed = needed.getValue(surface.purpose),
                    samples = samples,
                    shapesHeld = shapes,
                    persona = persona.key,
                    dateKey = invocation.dateKey,
                )
                val spoken = invocation.spoken ?: continue
                firings[surface.purpose to spoken.familyKey] = (firings[surface.purpose to spoken.familyKey] ?: 0) + 1
                val variant = variants[spoken.variantKey] ?: continue
                if (CorpusFill.quotesACallback(variant)) {
                    skipped++
                    continue
                }
                val subject = spoken.subjectId?.let { Subject(it, kindOf(it, facts)) }
                when (val attempt = CorpusFill.fill(variant, facts, subject, null, ZONE)) {
                    is CorpusFill.Attempt.No -> drift += GateFinding(
                        subject = variant.key,
                        detail = "the realizer rendered `${spoken.statement}` and this gate could not fill " +
                            "the line at all: {${attempt.why.slot}} ${attempt.why.reason}",
                        origin = variant.origin,
                    )
                    is CorpusFill.Attempt.Ok ->
                        if (attempt.filled.rendered == spoken.statement) {
                            reproduced++
                        } else {
                            drift += GateFinding(
                                subject = variant.key,
                                detail = "the realizer rendered `${spoken.statement}` and this gate rendered " +
                                    "`${attempt.filled.rendered}`",
                                origin = variant.origin,
                            )
                        }
                }
            }
        }
        return Harvest(samples, seen, reproduced, skipped, drift, firings)
    }

    /**
     * Step 1 of layer 3, over one fact set, keeping what the walk still needs.
     *
     * **The rule list shrinks as the table fills**, which is what makes a year of eleven
     * lives affordable to qualify twice. Once a family stage holds its samples, every rule
     * pointing at it is dropped from the list, so the steady state is a handful of rules for
     * the families that rarely or never qualify rather than the whole catalog.
     */
    private fun collect(
        purpose: Purpose,
        facts: FactSet,
        needed: MutableList<ClarityRule>,
        samples: MutableMap<String, MutableList<Sample>>,
        shapesHeld: MutableMap<String, MutableSet<List<Int>>>,
        persona: String,
        dateKey: String,
    ) {
        if (needed.isEmpty()) return
        val satisfied = mutableListOf<ClarityRule>()
        for (rule in needed) {
            val key = stageKey(purpose, rule.family, rule.stage ?: FIRST_STAGE)
            val bench = samples.getOrPut(key) { mutableListOf() }
            if (bench.size >= SAMPLES_PER_STAGE) {
                satisfied += rule
                continue
            }
            val shapes = shapesHeld.getOrPut(key) { mutableSetOf() }
            for (subject in rule.subject.select(facts)) {
                if (!rule.criteria.all { it.test(facts, subject) }) continue
                if (!shapes.add(shapeOf(facts, subject))) continue
                bench += Sample(
                    facts = facts,
                    rule = rule,
                    subject = subject,
                    callbackLabel = CorpusFill.callbackLabelFor(rule, facts, subject),
                    persona = persona,
                    dateKey = dateKey,
                )
                if (bench.size >= SAMPLES_PER_STAGE) break
            }
        }
        needed -= satisfied.toSet()
    }

    /**
     * What makes one qualifying moment different from another, for sampling.
     *
     * Every term is a quantity some family's lines are built on: how many areas moved, how
     * much happened, how much of it was finishing and how much was starting, whether there
     * was any protected time, whether the person has answered anything lately, and how much
     * history there is. Two days agreeing on all seven are the same situation as far as
     * every line in the corpus is concerned, so keeping both would buy nothing.
     *
     * Counts are capped, because the difference between eleven events and forty is not a
     * different situation for a sentence, and an uncapped term would let one busy persona
     * fill a bench with its own arithmetic.
     */
    private fun shapeOf(facts: FactSet, subject: Subject?): List<Int> = listOf(
        facts.areas.count { it.value.eventsInWindow > 0 },
        facts.window.totalEvents.coerceAtMost(EVENT_SHAPE_CAP),
        facts.window.completions.coerceAtMost(SMALL_SHAPE_CAP),
        facts.window.additions.coerceAtMost(SMALL_SHAPE_CAP),
        facts.window.focusStarted.coerceAtMost(FOCUS_SHAPE_CAP),
        facts.pulse.recentAnswers.size.coerceAtMost(SMALL_SHAPE_CAP),
        facts.history.weeksOfData.coerceAtMost(WEEK_SHAPE_CAP),
        if (subject == null) 0 else 1,
    )

    /**
     * Whether a subject id names an area or an item, read from the fact set rather than from
     * the invocation.
     *
     * The simulator records a payload subject kind that it derives from whether the id was
     * named in the sentence, which is not the same question. Nothing in the fill loop reads
     * the kind, but constructing a `Subject` with the wrong one would be a lie in a data
     * structure, and the fact set answers it exactly.
     */
    private fun kindOf(id: String, facts: FactSet): SubjectKind =
        if (facts.areas.containsKey(id)) SubjectKind.AREA else SubjectKind.ITEM

    // ------------------------------------------------------------------ the gate

    /** Every line, against the samples its own stage produced. */
    fun run(catalog: ClarityCatalog, harvest: Harvest): GateOutcome {
        val validator = ClarityValidator(ZONE)
        val findings = mutableListOf<GateFinding>()
        val unreached = mutableListOf<Variant>()
        val known = mutableListOf<GateFinding>()
        val slotsSeen = mutableMapOf<String, MutableSet<Set<String>>>()
        var renderable = 0
        var excluded = 0

        for (variant in catalog.allVariants) {
            if (SlotBindings.isExcluded(variant.key)) {
                excluded++
                continue
            }
            val key = stageKey(variant.purpose, variant.family, variant.stage)
            val samples = harvest.samples[key].orEmpty()
            if (samples.isEmpty()) {
                unreached += variant
                continue
            }
            var reason = "no sample was tried, which cannot happen"
            var passed = false
            for (sample in samples) {
                when (val attempt = CorpusFill.fill(variant, sample.facts, sample.subject, sample.callbackLabel, ZONE)) {
                    is CorpusFill.Attempt.No ->
                        reason = "{${attempt.why.slot}} ${attempt.why.reason}"
                    is CorpusFill.Attempt.Ok -> {
                        val candidate = CorpusFill.candidateOf(variant, attempt.filled, sample.rule, sample.subject)
                        when (val verdict = validator.validate(candidate, sample.facts)) {
                            is ValidationResult.Passed -> {
                                passed = true
                                slotsSeen.getOrPut(key) { mutableSetOf() } += attempt.filled.slots.keys
                            }
                            is ValidationResult.Vetoed ->
                                reason = "layer 5 ${verdict.check} vetoed `${attempt.filled.rendered}`: " +
                                    verdict.detail
                        }
                    }
                }
                if (passed) break
            }
            when {
                passed -> renderable++
                CorpusGateBaseline.isRecordedUnrenderable(variant.key) ->
                    known += GateFinding(variant.key, reason, variant.origin)
                else -> findings += GateFinding(variant.key, reason, variant.origin)
            }
        }

        findings += unrenderableQuestions(catalog, harvest, slotsSeen)
        val dark = unreached.map { "${it.purpose} ${it.family} s${it.stage}" }.distinct().sorted()
        return GateOutcome(
            id = "renders",
            name = "every line fills from real facts and passes layer 5 at least once",
            citation = "CLARITY_LOGIC_ENGINE.md 7.2 and 8, over ${harvest.factSetsSeen} fact sets from " +
                "${SimulationPersona.ALL.size} simulated years",
            findings = findings,
            grandfathered = known + unreached.map {
                GateFinding(it.key, "its stage never qualified in any simulated year", it.origin)
            },
            measured = "$renderable renderable, ${known.size + findings.size} not, ${unreached.size} " +
                "unreached in ${dark.size} stages, $excluded held out by SlotBindings. Dark stages: $dark",
        )
    }

    /**
     * A Pulse question whose markers no statement in its stage can fill.
     *
     * The realizer renders a question from the slot map of the statement it just filled, so a
     * question asking for a marker the stage's statements never carry is a question that
     * never appears. Nothing else in the build looks at that: the question benches carry no
     * register, no band and no rule, so they are invisible to every other gate here.
     */
    private fun unrenderableQuestions(
        catalog: ClarityCatalog,
        harvest: Harvest,
        slotsSeen: Map<String, Set<Set<String>>>,
    ): List<GateFinding> = catalog.families.flatMap { family ->
        family.stages.flatMap { stage ->
            val key = stageKey(family.purpose, family.key, stage.index)
            if (harvest.samples[key].isNullOrEmpty()) return@flatMap emptyList()
            val available = slotsSeen[key].orEmpty()
            stage.questions.mapNotNull { question ->
                if (question.text.slots.isEmpty()) return@mapNotNull null
                if (available.any { it.containsAll(question.text.slots) }) return@mapNotNull null
                GateFinding(
                    subject = question.key,
                    detail = "asks for ${question.text.slots} and no statement in this stage was ever " +
                        "filled with all of them, so the question can never be shown",
                    origin = "${question.sourceFile}:${question.sourceLine} ${question.key}",
                )
            }
        }
    }

    private const val FIRST_STAGE = 1
    private const val EVENT_SHAPE_CAP = 20
    private const val SMALL_SHAPE_CAP = 8
    private const val FOCUS_SHAPE_CAP = 4
    private const val WEEK_SHAPE_CAP = 8
}
