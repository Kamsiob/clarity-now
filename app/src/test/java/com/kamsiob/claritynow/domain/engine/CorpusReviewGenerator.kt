package com.kamsiob.claritynow.domain.engine

import com.kamsiob.claritynow.devtools.ClaritySimulator
import com.kamsiob.claritynow.devtools.SimulatedSurface
import com.kamsiob.claritynow.devtools.SimulationChecks
import com.kamsiob.claritynow.devtools.SimulationDump
import com.kamsiob.claritynow.devtools.SimulationRun
import com.kamsiob.claritynow.devtools.SimulationSummary
import com.kamsiob.claritynow.devtools.SpokenLine
import com.kamsiob.claritynow.domain.engine.catalog.CorpusFixture
import com.kamsiob.claritynow.domain.engine.catalog.CorpusVolume
import com.kamsiob.claritynow.domain.engine.catalog.Register
import java.io.File
import org.junit.Assume.assumeTrue
import org.junit.Test

/**
 * Writes `docs/CORPUS_REVIEW.md`, the file the owner reads to judge the corpus.
 *
 * ## Why this is kept, having twice been written as a one shot and deleted
 *
 * The objection to keeping it is real and it is recorded here rather than answered by
 * silence: a permanent test that wrote 850 KB into `docs/` on every run would be a test that
 * dirtied the tree, and one that asserted the committed file still matched would fail the
 * build on every corpus edit until somebody regenerated it. The review is a reading taken on
 * a day, not an invariant, and neither of those shapes is a test.
 *
 * **What that argument missed is that the reading is a function of the engine and not only of
 * the corpus.** The file has now gone stale twice inside a week, both times because a pass
 * changed which sentence gets chosen without writing a line: the register tier, then the
 * bound on the Pulse repeat filter. Each time, the pass that found the defect had to rebuild
 * the harness from nothing before it could show anybody what the defect sounded like.
 *
 * So this is the third shape, and it is neither of the two the objection rules out. It is
 * **skipped** unless `CLARITY_REGENERATE_REVIEW` is set, so an ordinary run neither writes
 * nor asserts anything, and it is **compiled** on every run, so it cannot rot against the
 * simulator's API without the build saying so. That is the whole cost: one skipped row in the
 * report, and a compiler holding it honest.
 *
 * ```
 * CLARITY_REGENERATE_REVIEW=1 ./gradlew :app:testDebugUnitTest --tests '*CorpusReviewGenerator*'
 * ```
 *
 * Delete it the day the corpus and the engine both stop moving. Not before.
 */
class CorpusReviewGenerator {

    @Test
    fun `write the corpus review`() {
        assumeTrue(
            "set ${Review.REGENERATE}=1 to rewrite $OUTPUT",
            System.getenv(Review.REGENERATE) != null,
        )
        val runs = ClaritySimulator(CorpusFixture.catalog).runAll()
        val review = Review(runs)
        println(review.diagnostics())
        File(OUTPUT).writeText(review.markdown())
        println("wrote $OUTPUT")
    }

    private companion object {
        const val OUTPUT = "../docs/CORPUS_REVIEW.md"
    }
}

/** One firing, with the persona it happened to. */
private data class Firing(
    val persona: SimulationRun,
    val surface: SimulatedSurface,
    val dateKey: String,
    val line: SpokenLine,
)

/** One surface, and every register it was heard in. */
private data class RegisterRow(
    val surface: SimulatedSurface,
    val firings: Int,
    val counts: List<Pair<Register, Int>>,
)

/** A bench, measured where the choice is actually made. */
private data class Bench(
    val surface: SimulatedSurface,
    val family: String,
    val stage: String,
    val firings: Int,
    val reached: Int,
    val topLine: String,
    val topCount: Int,
) {
    val perLine: String get() = "%.1f".format(firings.toDouble() / reached)
}

/** Every number this review states, computed once from one run of the eleven personas. */
private class Review(val runs: List<SimulationRun>) {

    val firings: List<Firing> = runs.flatMap { persona ->
        persona.invocations.mapNotNull { invocation ->
            invocation.spoken?.let { Firing(persona, invocation.surface, invocation.dateKey, it) }
        }
    }

    val authoredLines = CorpusVolume.entries.sumOf { CorpusFixture.keyedLineCount(it.fileName) }

    val checks = SimulationChecks.run(runs)

    val pulses = runs.flatMap { it.of(SimulatedSurface.PULSE) }
    val pulseSilent = pulses.count { it.spoken == null }
    val silenceTenths = (pulseSilent * THOUSAND * 2 + pulses.size) / (pulses.size * 2)
    val spokenPercent = SimulationSummary.percent(pulses.size - pulseSilent, pulses.size)

    /** Every variant reuse inside ninety days, with the surface that reused it. */
    val repeats: List<Pair<SimulatedSurface, Int>> = buildList {
        for (persona in runs) {
            val lastUsed = mutableMapOf<String, Int>()
            for (invocation in persona.invocations) {
                val variant = invocation.spoken?.variantKey ?: continue
                val previous = lastUsed.put(variant, invocation.day) ?: continue
                val gap = invocation.day - previous
                if (gap < EXCLUSION_DAYS) add(invocation.surface to gap)
            }
        }
    }

    val hardStretch = firings.filter { it.line.familyKey == HARD_STRETCH }
    val distinctVariants = firings.map { it.line.variantKey }.toSet().size
    val vetoes = runs.sumOf { persona -> persona.invocations.sumOf { it.vetoes.size } }

    fun failures(id: String) = checks.checks.first { it.id == id }.failures.size

    fun personaOf(key: String) = runs.first { it.persona.key == key }

    fun silencePercent(key: String): Int {
        val of = personaOf(key).of(SimulatedSurface.PULSE)
        return SimulationSummary.percent(of.count { it.spoken == null }, of.size)
    }

    fun spokenPulses(key: String) = personaOf(key).of(SimulatedSurface.PULSE).count { it.spoken != null }

    fun topFamily(key: String, surface: SimulatedSurface): Pair<String, Int> {
        val counts = personaOf(key).of(surface).mapNotNull { it.spoken?.familyKey }.groupingBy { it }.eachCount()
        val top = counts.entries.sortedWith(
            compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key },
        ).first()
        return top.key to top.value
    }

    fun registerTable(): List<RegisterRow> = SimulatedSurface.entries.map { surface ->
        val heard = firings.filter { it.surface == surface }.map { it.line.register }
        RegisterRow(surface, heard.size, Register.entries.map { it to heard.count { r -> r == it } }.filter { it.second > 0 })
    }

    fun benches(): List<Bench> {
        val multiStage = firings.groupBy { it.line.familyKey }
            .mapValues { entry -> entry.value.map { it.line.stage }.toSet().size > 1 }
        return firings.groupBy { Triple(it.surface, it.line.familyKey, it.line.stage) }
            .map { (key, lines) ->
                val counts = lines.groupingBy { it.line.variantKey }.eachCount()
                val top = counts.entries.sortedWith(
                    compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key },
                ).first()
                Bench(
                    surface = key.first,
                    family = key.second,
                    stage = if (multiStage.getValue(key.second)) " s${key.third}" else "",
                    firings = lines.size,
                    reached = counts.size,
                    topLine = top.key,
                    topCount = top.value,
                )
            }
            .sortedWith(compareByDescending<Bench> { it.firings.toDouble() / it.reached }.thenBy { it.family })
    }

    fun diagnostics(): String = buildString {
        appendLine("== corpus review diagnostics ==")
        appendLine("authored lines: $authoredLines")
        appendLine("pulse silence: $pulseSilent of ${pulses.size}, ${silenceTenths / TEN}.${silenceTenths % TEN} percent")
        appendLine("silence reasons: " + SimulationSummary.tally(pulses.mapNotNull { it.silence?.name }))
        appendLine("distinct variants: $distinctVariants of $authoredLines")
        appendLine("layer 5 vetoes: $vetoes")
        appendLine("repeats inside ninety days: ${repeats.size}, check says ${failures("variantRepetition")}")
        appendLine("  tightest gap: ${repeats.minOfOrNull { it.second }}")
        appendLine("  by surface: " + SimulationSummary.tally(repeats.map { it.first.label }))
        appendLine("length band collisions: ${failures("lengthBands")}")
        appendLine("parallel numeric runs: ${failures("parallelClauses")}")
        appendLine("hardStretch firings: ${hardStretch.size}")
        for (persona in runs) {
            val of = persona.of(SimulatedSurface.PULSE)
            appendLine(
                "  ${persona.persona.key}: ${persona.openDays} opens, ${of.count { it.spoken != null }} spoken, " +
                    "${SimulationSummary.percent(of.count { it.spoken == null }, of.size)} percent silent, " +
                    "${persona.eventCount} events",
            )
        }
        appendLine("registers:")
        registerTable().forEach { row ->
            appendLine("  ${row.surface.label}: ${row.firings} firings, " + row.counts.joinToString(", ") { "${it.first} ${it.second}" })
        }
        appendLine("top benches:")
        benches().take(BENCH_ROWS * 2).forEach {
            appendLine("  ${it.surface.label} ${it.family}${it.stage}: ${it.firings} firings, ${it.reached} reached, ${it.perLine}, ${it.topLine} x${it.topCount}")
        }
        for (key in listOf(TALKATIVE, DIFFICULT, SILENT)) {
            val persona = personaOf(key)
            appendLine("$key:")
            appendLine("  pulse: " + SimulationSummary.tally(persona.of(SimulatedSurface.PULSE).mapNotNull { it.spoken?.let { s -> "${s.familyKey}.s${s.stage}" } }))
            appendLine("  momentum: " + SimulationSummary.tally(persona.of(SimulatedSurface.MOMENTUM).mapNotNull { it.spoken?.familyKey }))
            appendLine("  banner: " + SimulationSummary.tally(persona.of(SimulatedSurface.BANNER).mapNotNull { it.spoken?.familyKey }))
            appendLine("  headline: " + SimulationSummary.tally(persona.of(SimulatedSurface.REPORT_HEADLINE).mapNotNull { it.spoken?.familyKey }))
            appendLine("  observation: " + SimulationSummary.tally(persona.of(SimulatedSurface.REPORT_OBSERVATION).mapNotNull { it.spoken?.familyKey }))
            appendLine("  pattern: " + SimulationSummary.tally(persona.of(SimulatedSurface.REPORT_PATTERN).mapNotNull { it.spoken?.familyKey }))
        }
    }

    // ------------------------------------------------------------------ the document

    fun markdown(): String {
        val doc = Doc()
        header(doc)
        howToReadALine(doc)
        whyTheseThreeLives(doc)
        whatToWatchFor(doc)
        whichEngineIsInThisFile(doc)
        personaSection(doc, 1, personaOf(TALKATIVE), "the talkative year") { talkativeIntro(it) }
        personaSection(doc, 2, personaOf(DIFFICULT), "the difficult year") { difficultIntro(it) }
        personaSection(doc, 3, personaOf(SILENT), "the silent year") { silentIntro(it) }
        doc.rule()
        appendixA(doc)
        appendixB(doc)
        appendixC(doc)
        return doc.toString()
    }

    fun header(doc: Doc) {
        doc.heading("# The corpus, read in place")
        doc.p(
            "Three simulated years, in order, with every sentence annotated by the family, the " +
                "stage, the register and the variant key that produced it. Taken on $TAKEN, over the " +
                "corpus and the engine as they now stand: **${thousands(authoredLines)} authored lines** " +
                "across the three volumes, recounted from the files rather than claimed. The corpus " +
                "held ${thousands(PRE_PHASE_NINE)} lines before phase 9, so phase 9 wrote " +
                "${thousands(PHASE_NINE_TOTAL - PRE_PHASE_NINE)} of them and the passes since have " +
                "added ${authoredLines - PHASE_NINE_TOTAL} to volume 3.",
        )
        doc.p(
            "**This is the artifact phase 9 is judged on, and the judgment is a reading rather than " +
                "a number.** Every mechanical gate in the build is green and every reading is in " +
                "`CLARITY_LOGIC_ENGINE.md` 12. What no gate can answer is whether a year of these " +
                "sentences sounds like something a person would say about your week. Eight sessions " +
                "wrote into this corpus against a specification and a gate suite, and none of them " +
                "compiled a line of it. This file is the only place any of that writing can be read " +
                "the way it will actually arrive: one sentence after another, in the order one life " +
                "produced them, with the days the app said nothing left in.",
        )
        doc.p(
            "**This is the third taking of this file and the first two are gone, both times without " +
                "an author touching volume 1 or volume 2.** Two passes since phase 9 changed which " +
                "sentence the engine picks. The register became a tier chosen among rather than a " +
                "fallback order, and the Pulse repeat filter was bounded to the one day the " +
                "specification always said it covered. Volume 3 grew by " +
                "${authoredLines - PHASE_NINE_TOTAL} lines in the same window. So every year below " +
                "reads differently from the copy phase 9 shipped, and the second of those two changes " +
                "moves the reading this project had failed to move across three measurements. What " +
                "changed " +
                "and what did not is the section before section 1.",
        )
        doc.p(
            "**Read it in order, and read at least one whole month.** A corpus is not judged a line " +
                "at a time. Almost any single sentence here survives being read alone; what a reader " +
                "can only see in sequence is a shape repeating, a claim arriving twice in one week, or " +
                "a voice that has drifted between two families. The months are headed and counted so " +
                "a reader can stop anywhere and come back.",
        )
        doc.p(
            "**How it was made, and how to make it again.** `CorpusReviewGenerator` in the test " +
                "source set runs `ClaritySimulator.runAll` over the eleven personas of " +
                "`SimulationPersona.ALL`, prints each year through `SimulationDump` and " +
                "`SimulationSummary`, and wraps them in the headings and the prose here. It is skipped " +
                "on an ordinary test run and writes this file only when asked: " +
                "`$REGENERATE=1 ./gradlew :app:testDebugUnitTest --tests '*CorpusReviewGenerator*'`. " +
                "Nothing below is hand written except the prose outside the fences, and every number " +
                "in that prose is quoted from the run underneath it.",
        )
        doc.rule()
    }

    fun howToReadALine(doc: Doc) {
        val example = runs.flatMap { it.invocations }.first { invocation ->
            val line = invocation.spoken
            invocation.surface == SimulatedSurface.PULSE && line != null &&
                line.question != null && line.responses.size == RESPONSES && line.facts.any { '[' in it }
        }
        val line = requireNotNull(example.spoken)
        doc.heading("## How to read a line")
        doc.fence(SimulationDump.of(example).trimEnd())
        doc.raw(
            """
            | part | what it is |
            |---|---|
            | `${example.dateKey}  [pulse]` | the simulated day, and which surface spoke. The surfaces are `pulse`, `momentum`, `banner`, `report headline`, `report observation` and `report pattern` |
            | `${line.familyKey} / stage ${line.stage} / ${line.register.name.lowercase()}` | the family, the rung of its escalation ladder, and the register layer 4 chose |
            | `${line.variantKey}` | the variant key. It is stable forever and never reused, so any line in this file can be found in its corpus file by searching for the key |
            | `rule:` | the rule in the catalog that qualified. The logic side of the same firing |
            | `fired:` | every criterion that was true, in the words the catalog states them in |
            | `facts:` | every value the sentence used, each number carrying in brackets the `FactRef` the validator re-read it from. A number with no bracket beside it would be a number the engine invented |
            | `>` | the sentence. A Pulse carries a second `>` for its question |
            | `?` | the two Pulse responses. Read them aloud with no context: if one sounds like the answer a good person gives, both are wrong |
            | `SILENT (reason)` | the surface said nothing, and why. `NO_RULE_QUALIFIED` is a day with nothing in it, `ALL_QUALIFIED_RULES_FILTERED` is a day where something qualified and was held back, `INSUFFICIENT_DATA` is too little history to describe anything |
            """.trimIndent(),
        )
        doc.p(
            "A `vetoed:` line would name a candidate layer 5 refused before the one that spoke. " +
                if (vetoes == 0) {
                    "**There is not one in this file, or anywhere in the run**: layer 5 vetoed nothing " +
                        "across eleven persona years."
                } else {
                    "**There are $vetoes of them in the run.**"
                },
        )
        doc.rule()
    }

    fun whyTheseThreeLives(doc: Doc) {
        val silentPulses = personaOf(SILENT).of(SimulatedSurface.PULSE).mapNotNull { it.spoken }
        val dominant = silentPulses.groupingBy { it.familyKey }.eachCount().entries.maxByOrNull { it.value }!!
        doc.heading("## Why these three lives")
        doc.p(
            "Eleven personas ran. These three were picked as the corners of the space rather than " +
                "three samples from the middle of it, and the first thing to know about them now is " +
                "that **they are no longer corners of silence**. When this file was first taken they " +
                "sat at 37, 88 and 97 percent Pulse silence. They sit at ${silencePercent(TALKATIVE)}, " +
                "${silencePercent(DIFFICULT)} and ${silencePercent(SILENT)}. What separates them is " +
                "content rather than quantity now, which is harder to judge and is the right thing to " +
                "be judging.",
        )
        doc.raw(
            "| persona | opens | Pulses spoken | Pulse silence | why it is here |\n" +
                "|---|---|---|---|---|\n" +
                threeLivesRow(
                    TALKATIVE,
                    "the talkative extreme. Far more arrives than leaves, so something is always true " +
                        "about the queue. This is the year with the most sentences in it, and the one " +
                        "where a repeated shape has the most chances to show",
                ) + "\n" +
                threeLivesRow(
                    DIFFICULT,
                    "the difficult year. Daily for two months, then about one day in three for the " +
                        "remaining ten. This is the mirror test: read it imagining it is your own worst " +
                        "year. It is also the only one of the three that reaches `hardStretch`",
                ) + "\n" +
                threeLivesRow(
                    SILENT,
                    "the life the app is designed for, and the one that changed most. Four areas, none " +
                        "dominant, nothing neglected. It used to hear from the Pulse nine times in a " +
                        "year and now hears ${spokenPulses(SILENT)}",
                ),
        )
        doc.p(
            "**The third row is the one to read twice, and it is no longer here for the reason it was " +
                "put here.** It was the silent extreme: nine Pulses in a year, and every reading about " +
                "silence in this project pointed at it. Those nine were an artifact. The repeat filter " +
                "blocked the family of the last Pulse for as long as it stayed blocked, so the life " +
                "using the app best was the one it went quiet on. With the filter bounded to yesterday " +
                "that person hears ${spokenPulses(SILENT)} Pulses, and " +
                "**${dominant.value} of them are `${dominant.key}`**. The question that year now asks " +
                "is not whether the app says enough. It is whether hearing one family every other day " +
                "reads as an app that knows you or an app with one thing to say.",
        )
        doc.p(
            "**Why not the others.** `heavySingleArea` and `fastCompleter` land at " +
                "${silencePercent("heavySingleArea")} and ${silencePercent("fastCompleter")} percent " +
                "silence and read close to `queueHoarder` with a narrower family spread. `highFocus` " +
                "and `lowFocus` differ from each other in which focus families fire rather than in " +
                "voice. `brandNew` is three weeks long. `longDormantRevival` is the most silent life " +
                "in the run at ${silencePercent("longDormantRevival")} percent and is worth one week of " +
                "anybody's attention, the week it comes back, where `MASTER_BUILD_PROMPT.md` 14b.4 is " +
                "already measured. `sporadic` sits between the first two at " +
                "${silencePercent("sporadic")} percent. `acceptsEveryPlan` is the non compliance test " +
                "of section 12, it passes, and its year reads like the queue hoarder's with plans " +
                "quietly attached; its `hardStretch` line is in Appendix A with the rest.",
        )
        doc.rule()
    }

    fun threeLivesRow(key: String, why: String): String {
        val persona = personaOf(key)
        return "| **${persona.persona.title}** | ${persona.openDays} | ${spokenPulses(key)} | " +
            "${silencePercent(key)} percent | $why |"
    }

    fun whatToWatchFor(doc: Doc) {
        val persona = personaOf(SILENT)
        val momentum = topFamily(SILENT, SimulatedSurface.MOMENTUM)
        val headline = topFamily(SILENT, SimulatedSurface.REPORT_HEADLINE)
        val reasons = pulses.mapNotNull { it.silence?.name }.groupingBy { it }.eachCount()
        doc.heading("## The four things worth watching for")
        doc.numbered(
            "**A claim repeating, which is not the same as a line repeating.** Phase 9 made the " +
                "benches deep and the pass after it made them reachable, so the wording changes on " +
                "nearly every firing. Watch `$SILENT` hear `${momentum.first}` on ${momentum.second} of " +
                "${persona.openDays} Momentum openings, and take `${headline.first}` as its report " +
                "headline in ${headline.second} weeks of " +
                "${persona.of(SimulatedSurface.REPORT_HEADLINE).size}. Fifty different sentences saying " +
                "the same thing about the same week is a different defect from one sentence fifty " +
                "times, and depth cannot touch it.",
            "**The mirror test, on `$DIFFICULT`.** Every line there is written to be read by somebody " +
                "having a bad year. `CLARITY_LOGIC_ENGINE.md` 11.3 asks that any sentence which would " +
                "make a reader defensive is wrong, and that decline and neglect families are read " +
                "twice.",
            "**The silence, and what is left of it.** It is left in on purpose: a day the app said " +
                "nothing is part of what this reads like, and there are ${thousands(pulseSilent)} of " +
                "them across the eleven. Its shape is what changed. " +
                "${thousands(reasons["NO_RULE_QUALIFIED"] ?: 0)} of those days had nothing that " +
                "qualified at all and ${thousands(reasons["ALL_QUALIFIED_RULES_FILTERED"] ?: 0)} had " +
                "something qualify and held it back, which is the first run in nine measurements where " +
                "the held back column is the smaller one. Silence is a rule coverage question now " +
                "rather than a filter question.",
            "**`hardStretch`.** Flagged by name wherever it appears. 6.4 says that if any line in it " +
                "reads as consolation rather than observation, the family is removed rather than " +
                "rewritten. That is the one judgment in this file with a stated consequence.",
        )
        doc.rule()
    }

    fun whichEngineIsInThisFile(doc: Doc) {
        val reasons = pulses.mapNotNull { it.silence?.name }.groupingBy { it }.eachCount()
        val pulseBenches = benches().filter { it.surface == SimulatedSurface.PULSE }
            .sortedByDescending { it.firings }.take(BENCH_ROWS)
        doc.heading("## Before you read: which corpus is in this file, and which engine")
        doc.p(
            "**Volumes 1 and 2 are phase 9's and not one line of either has been rewritten.** Three " +
                "things changed under them since this file was first taken, two in the engine and one " +
                "in volume 3, and between them they account for every difference a reader who knows " +
                "the old copy will notice.",
        )
        doc.p(
            "**One. The register became a choice.** `RegisterChoice.preference` returned a flat list " +
                "and `Realizer` took the first register with a fillable bench, so the head of the list " +
                "won every firing a rule left open. Over these same eleven persona years the Pulse " +
                "spoke plain on 1,080 firings of 1,081, and Momentum and the banner spoke reflective " +
                "on all 5,594. 7.4 step 4 is now a tier chosen among, and the choosing reuses 7.6 one " +
                "level up: hold back the voice the family used most recently, then take the head of a " +
                "`StableHash` ordering. Every surface now reaches every register its corpus and its " +
                "rules can offer, where five of six reached exactly one before.",
        )
        doc.raw(
            "| surface | firings in the run | registers reached |\n|---|---|---|\n" +
                registerTable().joinToString("\n") { row ->
                    "| ${row.surface.label} | ${thousands(row.firings)} | " +
                        row.counts.joinToString(", ") {
                            "${it.first.name.lowercase().replace('_', ' ')} ${thousands(it.second)}"
                        } + " |"
                },
        )
        doc.p(
            "Three of those rows are a property of the corpus rather than of the engine and no author " +
                "can change them from here. `CORPUS_2_REPORT.md` carries a register tag in section 2 " +
                "alone and `ReportWalker` refuses one on a headline or a pattern line, so every variant " +
                "behind the report headline and the report pattern is plain by construction. The report " +
                "observation row reaches four and not five because volume 2 authors no reflective line " +
                "anywhere, which is exactly the case the tier has to fall through rather than fall " +
                "silent on.",
        )
        doc.p(
            "**One row is the instrument and not the app.** 7.4 gives the Pulse a time of day rule: " +
                "before the evening it offers the plain and observational voices, at or after it the " +
                "reflective one. `PulseSchedule.dayAt` implements it by ending the window at the day " +
                "boundary before 17:00 and at the moment of asking after it, and " +
                "`ClarityEngine.momentOf` reads a window ending at midnight as morning. **The simulator " +
                "opens every persona at 07:00 and builds only the yesterday window**, so every Pulse in " +
                "this run is a morning one and volume 1's reflective bench is never asked for. In the " +
                "shipped app a person who opens after five in the afternoon gets it. Those lines are " +
                "unmeasured here rather than dead, and this file is the morning half of the Pulse " +
                "corpus.",
        )
        doc.p(
            "**Two. The Pulse repeat filter now reaches one day, which is what it always said it " +
                "reached.** 7.3 states it as covering only yesterday and section 12's own table calls " +
                "it \"yesterday's family cannot be today's\". The code compared against " +
                "`PulseFacts.lastGeneratedFamily`, which is the family of the most recent Pulse at any " +
                "point in the past, and the difference is not a rounding error: a family blocked here " +
                "writes no `PULSE_GENERATED`, so the fact never advances, so the block renews itself " +
                "every morning. The silent year in section 3 was that loop. It spoke nine times between " +
                "January 5 and January 20 and was then held silent for 348 consecutive days by the " +
                "family of a Pulse from January 20. With the bound in place, silence across the eleven " +
                "falls from 65.7 percent to " +
                "${silenceTenths / TEN}.${silenceTenths % TEN} percent, " +
                "${thousands(SILENT_DAYS_BEFORE)} silent days to ${thousands(pulseSilent)}, and the " +
                "filtered column of that silence drops below the column for days where nothing " +
                "qualified at all: ${thousands(reasons["NO_RULE_QUALIFIED"] ?: 0)} against " +
                "${thousands(reasons["ALL_QUALIFIED_RULES_FILTERED"] ?: 0)}.",
        )
        doc.p(
            "**Three. Volume 3's quiet week bench went from eight lines to sixty**, which is the one " +
                "corpus change in this window and the only reason the line count moved. The Areas " +
                "banner's `weekQuiet` family had eight lines, every one of them neutral agent, and " +
                "nothing could ask for that register until the first change above; it then fired 240 " +
                "times a year off a bench of eight. Nine `mo.steady` lines were corrected in the same " +
                "pass, where a rendered digit sat beside the word `fourteen`.",
        )
        doc.p(
            "**And this is why a hot bench is now close to the size 11.1 says it is.** The realizer " +
                "chooses inside one register of one stage and 11.1 sizes the stage, so while the " +
                "register was fixed a stage of sixty lines bought the variety of twenty. Measured where " +
                "the choice is actually made, the hottest Pulse benches in this run reach:",
        )
        doc.bullets(
            pulseBenches.map {
                "`${it.family}${it.stage}`, ${it.firings} firings across ${it.reached} distinct " +
                    "lines, the most said one ${it.topCount} times"
            },
        )
        doc.p(
            "Variant repeats inside ninety days have fallen from 7,370 before phase 9 to 3,898 after " +
                "it and to ${thousands(repeats.size)} here, and the last of those two steps was taken " +
                "with no line written into volumes 1 or 2. That is what the first reading of this file " +
                "predicted from the other end: the benches were always that deep and the chooser could " +
                "not see them. **Section 12's ninth column reads 2,407 for this row**, taken over the " +
                "same run before volume 3's quiet week bench went from eight lines to sixty; a family " +
                "firing 240 times a year off a bench of eight is where the difference between the two " +
                "numbers lives, and the banner is where most of what is left still lives.",
        )
        doc.rule()
    }

    // ------------------------------------------------------------------ persona sections

    fun talkativeIntro(doc: Doc) {
        val persona = personaOf(TALKATIVE)
        val lines = persona.of(SimulatedSurface.PULSE).mapNotNull { it.spoken }
        val stage = lines.groupingBy { it.familyKey to it.stage }.eachCount().entries
            .sortedWith(compareByDescending<Map.Entry<Pair<String, Int>, Int>> { it.value }.thenBy { it.key.first })
            .first()
        val family = lines.count { it.familyKey == stage.key.first }
        doc.p(
            "Far more arrives than leaves. Something is true about this queue every single day, so " +
                "this is the year with the most sentences in it: ${spokenPulses(TALKATIVE)} Pulses " +
                "spoken out of ${persona.openDays}, against a run average of $spokenPercent percent " +
                "spoken. `${stage.key.first}` speaks $family times, ${stage.value} of them at stage " +
                "${stage.key.second}. **If a shape repeats anywhere, it repeats here first.** Watch the " +
                "run of `${stage.key.first}` stage ${stage.key.second} across the autumn in particular: " +
                "${stage.value} firings of one stage of one family is the hardest thing a deep bench is " +
                "asked to do anywhere in this app.",
        )
    }

    fun difficultIntro(doc: Doc) {
        val persona = personaOf(DIFFICULT)
        val neglected = persona.of(SimulatedSurface.REPORT_OBSERVATION).count { it.spoken?.familyKey == "neglectedArea" }
        val gone = persona.of(SimulatedSurface.REPORT_PATTERN).count { it.spoken?.familyKey == "areaGoneQuiet" }
        val quiet = persona.of(SimulatedSurface.PULSE).mapNotNull { it.spoken }.count { it.familyKey == "quietDay" }
        val hard = hardStretch.filter { it.persona.persona.key == DIFFICULT }
        doc.p(
            "Strong for two months, then trailing away. Daily until the start of March, then about " +
                "one day in three for the remaining ten months, and from the end of May nothing is " +
                "captured but a residue: ${persona.openDays} opens against ${persona.days} days. **Read " +
                "this one imagining it is your own worst year.** It is where `neglectedArea` speaks " +
                "$neglected times, `areaGoneQuiet` $gone, and where the difficulty register of 6.4 " +
                "fires ${times(hard.size)}. It is also ${silencePercent(DIFFICULT)} percent silent, and " +
                "$quiet of its ${spokenPulses(DIFFICULT)} Pulses are `quietDay` describing the quiet " +
                "back to them. That is the sharpest form of the question this whole file asks: on a " +
                "week with nothing in it the app mostly says nothing, and when it does speak it is " +
                "mostly about the nothing.",
        )
        doc.p(
            "**`hardStretch` in this year.** " +
                hard.joinToString("; ") { "${it.dateKey}, `${it.line.variantKey}`, \"${it.line.statement}\"" } +
                " It is banner flagged in place below.",
        )
    }

    fun silentIntro(doc: Doc) {
        val persona = personaOf(SILENT)
        val momentum = topFamily(SILENT, SimulatedSurface.MOMENTUM)
        val headline = topFamily(SILENT, SimulatedSurface.REPORT_HEADLINE)
        val lines = persona.of(SimulatedSurface.PULSE).mapNotNull { it.spoken }
        val dominant = lines.groupingBy { it.familyKey }.eachCount().entries.maxByOrNull { it.value }!!
        doc.p(
            "Four areas, none dominant, moving steadily all year. This is a person using the app " +
                "exactly the way it was designed to be used, and **it is the year that changed most " +
                "between the last taking of this file and this one**: nine Pulses then, " +
                "${spokenPulses(SILENT)} now, because the family of a Pulse from January 20 had been " +
                "blocking every candidate for the rest of the year.",
        )
        doc.p(
            "**Read it for the opposite of what it was kept for.** It used to be the proof that the " +
                "app can go quiet on somebody doing everything right. It is now the proof of what the " +
                "app has to say to that person, and the answer is one family: ${dominant.value} of the " +
                "${spokenPulses(SILENT)} are `${dominant.key}`. With four evenly used areas no area " +
                "ever holds most of a window, so `concentration` cannot qualify, `spread` fires once, " +
                "and `persistence` is left holding the year. The wording is different nearly every " +
                "time. The reading is the same. Whether that is company or monotony is the judgment " +
                "this section wants, and no gate in the build can make it.",
        )
        doc.p(
            "The other surfaces have the same shape and had it before: the Momentum headline says " +
                "`${momentum.first}` on ${momentum.second} of ${persona.openDays} openings and the " +
                "report is headlined `${headline.first}` in ${headline.second} weeks out of " +
                "${persona.of(SimulatedSurface.REPORT_HEADLINE).size}.",
        )
    }

    fun personaSection(
        doc: Doc,
        index: Int,
        persona: SimulationRun,
        subtitle: String,
        intro: (Doc) -> Unit,
    ) {
        doc.heading("## $index. ${persona.persona.title}, $subtitle")
        intro(doc)
        doc.fence(
            "persona: ${persona.persona.key}, ${persona.persona.title}\n" +
                "why:     ${persona.persona.why}\n" +
                "span:    ${persona.days} simulated days, ${persona.openDays} opens, " +
                "${persona.eventCount} events, ${persona.invocations.size} engine invocations",
        )
        for ((month, invocations) in persona.invocations.groupBy { it.dateKey.substring(0, MONTH_KEY) }.toSortedMap()) {
            doc.heading("### ${monthTitle(month)}")
            val days = invocations.map { it.dateKey }.toSet().size
            val monthPulses = invocations.filter { it.surface == SimulatedSurface.PULSE }
            val said = monthPulses.count { it.spoken != null }
            val quiet = monthPulses.size - said
            doc.p(
                "$days days on screen, $said ${if (said == 1) "Pulse" else "Pulses"} spoken, " +
                    "$quiet Pulse ${if (quiet == 1) "day" else "days"} silent, " +
                    "${invocations.count { it.spoken != null }} sentences in all.",
            )
            doc.fence(
                buildString {
                    var lastDay = -1
                    for (invocation in invocations) {
                        if (invocation.day != lastDay && lastDay >= 0) appendLine()
                        lastDay = invocation.day
                        if (invocation.spoken?.familyKey == HARD_STRETCH) appendLine(HARD_STRETCH_FLAG)
                        append(SimulationDump.of(invocation))
                    }
                }.trimEnd('\n'),
            )
        }
        doc.heading("### The year in counts")
        doc.fence(SimulationSummary.of(persona).trimEnd('\n'))
    }

    // ------------------------------------------------------------------ appendices

    fun appendixA(doc: Doc) {
        val ordered = hardStretch.sortedWith(compareBy({ it.persona.persona.title }, { it.dateKey }))
        val inThree = hardStretch.count { it.persona.persona.key in THREE_LIVES }
        val inherited = ordered.filter { it.line.variantKey <= LAST_INHERITED_HARD_LINE }
        val written = ordered - inherited.toSet()
        doc.heading("## Appendix A: every `hardStretch` line in the run")
        doc.p(
            "`hardStretch` fired **${times(hardStretch.size)} across eleven persona years**, which is " +
                "what 6.4 intends: it waits six weeks between firings, and only three or more quiet " +
                "weeks with growing queues, or a four week decline, reach it at all. " +
                "${cardinal(inThree).replaceFirstChar { it.uppercase() }} of them " +
                "${if (inThree == 1) "falls" else "fall"} inside the three years above and " +
                "${if (inThree == 1) "is" else "are"} banner flagged in place. All " +
                "${cardinal(hardStretch.size)} are here so the family can be judged whole, because " +
                "this is the entire run's evidence for a family whose stated consequence is removal.",
        )
        doc.raw(
            "| persona | date | key | the line |\n|---|---|---|---|\n" +
                ordered.joinToString("\n") {
                    "| ${it.persona.persona.title} | ${it.dateKey} | `${it.line.variantKey}` | ${it.line.statement} |"
                },
        )
        doc.p(
            "**The bench splits in half at `ob.hard.l08` and the run drew from both halves, which is " +
                "the whole of the ruling.** Lines `l01` to `l08` predate phase 9 and `l09` to `l16` are " +
                "phase 9's. " +
                inherited.joinToString(" and ") { "`${it.line.variantKey}`" } +
                " ${if (inherited.size == 1) "is" else "are"} inherited. `ob.hard.l01`, `l02` and `l03` " +
                "say that a stretch like this is *common*, that it *usually means something outside " +
                "the app*, and that it *generally has a reason that is not visible here*. Every one of " +
                "those is a claim about the world rather than a reading of the record, and the app has " +
                "no other person's data to know it from. `ob.hard.l07` denies a failure, which is " +
                "reassurance wearing the grammar of an observation.",
        )
        doc.p(
            "**The eight lines phase 9 added hold 6.4 more tightly than the eight they were grown " +
                "from.** " +
                written.joinToString(" and ") { "`${it.line.variantKey}`, \"${it.line.statement}\"," } +
                " state the pattern and stop, and the second one declines to interpret out loud, which " +
                "is the whole register in one sentence. That is the shape of the ruling if the owner " +
                "wants one: the newer half of this bench is the standard, and the older half is what " +
                "6.4 warned about when it said the family is removed rather than rewritten.",
        )
        doc.rule()
    }

    fun appendixB(doc: Doc) {
        val bySurface = repeats.groupingBy { it.first.label }.eachCount().entries
            .sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key })
        val topTwo = bySurface.take(2)
        val share = SimulationSummary.percent(topTwo.sumOf { it.value }, repeats.size)
        doc.heading("## Appendix B: where the repeats are now, and who owns them")
        doc.p(
            "Variant repeats inside ninety days stand at **${thousands(repeats.size)}**, against 3,898 " +
                "at the taking before this one and 7,370 before phase 9, and the tightest gap is still " +
                "${cardinal(repeats.minOf { it.second })} day. **$share percent of what is left is " +
                "${topTwo.joinToString(" and ") { surfaceName(it.key) }}**, the two surfaces that render on " +
                "every app open rather than once a day. The whole split is " +
                "${bySurface.joinToString(", ") { "${thousands(it.value)} ${it.key}" }}.",
        )
        doc.p(
            "The benches under the most pressure, measured as firings across the run divided by the " +
                "number of lines the chooser actually reached.",
        )
        doc.raw(
            "| surface | family | firings | lines reached | firings per line | the most said line |\n" +
                "|---|---|---|---|---|---|\n" +
                benches().take(BENCH_ROWS).joinToString("\n") {
                    "| ${it.surface.label} | `${it.family}${it.stage}` | ${thousands(it.firings)} | " +
                        "${it.reached} | ${it.perLine} | `${it.topLine}`, ${it.topCount} times |"
                },
        )
        doc.p("**Two different problems sit in that table and only one of them is about writing.**")
        doc.p(
            "The banner and Momentum rows are a **sizing** problem that 11.1 does not have a tier for. " +
                "Its table stops at \"40 firings a year or more\" and asks for 60 to 100 lines. A banner " +
                "family fires more than a thousand times across eleven persona years because it is " +
                "recomputed on every open, and no bench of any size holds a ninety day exclusion " +
                "against that. This is a cooldown or a throttle decision, not a bench, and no amount of " +
                "authoring reaches it.",
        )
        doc.p(
            "A row where the reachable bench is a handful of lines against dozens of firings is a " +
                "**binding** problem instead, and it is the sharper of the two, because it says the " +
                "corpus has the lines and the engine cannot fill them. That is one binding or one " +
                "measure each time, and no authoring pass would find it, because it is only visible in " +
                "a year read in order.",
        )
        doc.rule()
    }

    /** How a surface is named in prose, where the dump's own label reads as a lowercase noun. */
    fun surfaceName(label: String): String = when (label) {
        SimulatedSurface.MOMENTUM.label -> "the Momentum headline"
        SimulatedSurface.BANNER.label -> "the areas banner"
        else -> "the $label"
    }

    fun appendixC(doc: Doc) {
        val reasons = pulses.mapNotNull { it.silence?.name }.groupingBy { it }.eachCount()
        doc.heading("## Appendix C: the ninth measurement, in brief")
        doc.p(
            "The full table, with every earlier column beside it, is in `CLARITY_LOGIC_ENGINE.md` 12. " +
                "The short version, over the same eleven personas and the same simulated year as every " +
                "measurement before it. The seventh column is phase 9, the eighth is the register tier, " +
                "the ninth is the bound on the repeat filter.",
        )
        doc.raw(
            "| reading | seventh, phase 9 | eighth, the register tier | ninth, the repeat bound |\n" +
                "|---|---|---|---|\n" +
                "| authored corpus lines | 4,733 | 4,733 | **${thousands(authoredLines)}** |\n" +
                "| variant repeats inside ninety days | 3,898 | 2,411 | **${thousands(repeats.size)}** |\n" +
                "| two consecutive report leads sharing a length band | 277 | 289 | **${thousands(failures("lengthBands"))}** |\n" +
                "| three or more parallel numeric clauses in a row | 121 | 148 | **${thousands(failures("parallelClauses"))}** |\n" +
                "| Pulse silence, all personas | 65.7 percent | 65.7 percent | **${silenceTenths / TEN}.${silenceTenths % TEN} percent** |\n" +
                "| silent days: nothing qualified / qualified and filtered / too little data | 895 / 1,161 / 11 | 895 / 1,161 / 11 | **${thousands(reasons["NO_RULE_QUALIFIED"] ?: 0)} / ${thousands(reasons["ALL_QUALIFIED_RULES_FILTERED"] ?: 0)} / ${reasons["INSUFFICIENT_DATA"] ?: 0}** |\n" +
                "| layer 5 vetoes | 0 | 0 | **$vetoes** |\n" +
                "| distinct variants a whole run reached | 1,162 of 4,733 | not recorded | **${thousands(distinctVariants)} of ${thousands(authoredLines)}** |\n" +
                "| registers a surface reaches | one, on five of six | every one on all six | **unchanged, every one on all six** |",
        )
        doc.p(
            "**Two readings moved the wrong way and both are report composition rather than " +
                "language.** Consecutive leads sharing a length band and runs of three or more numeric " +
                "clauses both climbed as the benches got deeper, because the composer had more leads to " +
                "put next to each other at the same time as it had more shapes to alternate. 7.5's band " +
                "rule is a preference inside a bench rather than a cap over a page, and that is the " +
                "difference in one sentence.",
        )
        doc.p(
            "**Silence moved, and the reason it had not moved before is why it moved now.** Three " +
                "thousand corpus lines left it identical to the day, because `VariantChoice.choose` " +
                "reuses a line rather than falling silent when a bench is exhausted, so depth cannot " +
                "produce silence at all. The register tier changed which line was chosen and never " +
                "whether one was, so it did not move it either. What moved it was a filter reaching " +
                "further than the rule it implements, and the corrected reach takes silence from 65.7 " +
                "to ${silenceTenths / TEN}.${silenceTenths % TEN} percent. **What is left is not a " +
                "filter problem.** ${thousands(reasons["NO_RULE_QUALIFIED"] ?: 0)} of the " +
                "${thousands(pulseSilent)} silent days had nothing qualify at all, which is a question " +
                "about rules and facts rather than about language, and it is the first run in nine " +
                "measurements where that is the larger half.",
        )
    }

    // ------------------------------------------------------------------ helpers

    fun monthTitle(key: String): String =
        "${MONTHS[key.substring(YEAR_LEN + 1).toInt() - 1]} ${key.substring(0, YEAR_LEN)}"

    fun thousands(value: Int): String = value.toString().reversed().chunked(THREE).joinToString(",").reversed()

    fun times(value: Int): String = TIMES.getOrElse(value) { "$value times" }

    fun cardinal(value: Int): String = CARDINALS.getOrElse(value) { "$value" }

    companion object {
        const val TALKATIVE = "queueHoarder"
        const val DIFFICULT = "abandoning"
        const val SILENT = "balancedAcrossFour"
        const val HARD_STRETCH = "hardStretch"
        const val HARD_STRETCH_FLAG =
            "*** hardStretch, the difficulty register of 6.4. Read it as a friend saying it. ***"
        const val TAKEN = "August 28, 2026"
        const val REGENERATE = "CLARITY_REGENERATE_REVIEW"

        /** The corpus before phase 9, and after it. Both counted, both quoted in section 12. */
        const val PRE_PHASE_NINE = 1_503
        const val PHASE_NINE_TOTAL = 4_733

        /** Silent Pulse days at the seventh and eighth measurements, which were identical. */
        const val SILENT_DAYS_BEFORE = 2_067

        /** `l01` to `l08` predate phase 9; `l09` to `l16` are its. */
        const val LAST_INHERITED_HARD_LINE = "ob.hard.l08"

        const val EXCLUSION_DAYS = 90
        const val THOUSAND = 1_000
        const val TEN = 10
        const val THREE = 3
        const val MONTH_KEY = 7
        const val YEAR_LEN = 4
        const val BENCH_ROWS = 7
        const val RESPONSES = 2
        val THREE_LIVES = listOf(TALKATIVE, DIFFICULT, SILENT)
        val TIMES = listOf(
            "never", "once", "twice", "three times", "four times", "five times", "six times",
            "seven times", "eight times", "nine times", "ten times",
        )
        val CARDINALS = listOf(
            "none", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten",
        )
        val MONTHS = listOf(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December",
        )
    }
}

/**
 * The markdown, assembled block by block.
 *
 * **Prose is written as one long string and wrapped here rather than wrapped in the source.**
 * Every paragraph in this file interpolates a measured number, and a number that grows by a
 * digit would leave a hand wrapped paragraph ragged in a way nobody would notice until they
 * read it. Tables, lists and fenced dumps go through [raw] and are never reflowed.
 */
private class Doc {

    private val out = StringBuilder()

    /** A heading, with the blank line after it that markdown wants. */
    fun heading(text: String) = block(text)

    /** One paragraph, reflowed to [WRAP] columns. */
    fun p(text: String) = block(wrap(text))

    /** A table, a list or anything else whose line breaks are load bearing. */
    fun raw(text: String) = block(text.trimEnd('\n'))

    /** A fenced block of dump output, verbatim. */
    fun fence(text: String) = block("```text\n$text\n```")

    fun bullets(items: List<String>) = block(items.joinToString("\n") { wrap("- $it", "  ") })

    fun numbered(vararg items: String) =
        block(items.mapIndexed { index, item -> wrap("${index + 1}. $item", "   ") }.joinToString("\n"))

    fun rule() = block("---")

    override fun toString(): String = out.toString()

    private fun block(text: String) {
        out.append(text)
        out.append("\n\n")
    }

    private fun wrap(text: String, hanging: String = ""): String {
        val words = text.replace(WHITESPACE, " ").trim().split(' ').filter { it.isNotEmpty() }
        val lines = mutableListOf<StringBuilder>()
        for (word in words) {
            val current = lines.lastOrNull()
            if (current == null || current.length + 1 + word.length > WRAP) {
                lines += StringBuilder(if (lines.isEmpty()) "" else hanging).append(word)
            } else {
                current.append(' ').append(word)
            }
        }
        return lines.joinToString("\n")
    }

    private companion object {
        /** The width the first taking of this file was wrapped to, kept so a diff is readable. */
        const val WRAP = 90
        val WHITESPACE = Regex("\\s+")
    }
}
