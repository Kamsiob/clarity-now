package com.kamsiob.claritynow.domain.engine.corpus

import com.kamsiob.claritynow.domain.engine.FamilyKey
import com.kamsiob.claritynow.domain.engine.catalog.CatalogIntegrity
import com.kamsiob.claritynow.domain.engine.catalog.ClarityCatalog
import com.kamsiob.claritynow.domain.engine.catalog.CorpusVolume
import com.kamsiob.claritynow.domain.engine.catalog.KnownCorpusViolations
import com.kamsiob.claritynow.domain.engine.catalog.LengthBand
import com.kamsiob.claritynow.domain.engine.catalog.LengthBands
import com.kamsiob.claritynow.domain.engine.catalog.Purpose
import com.kamsiob.claritynow.domain.engine.catalog.Register
import com.kamsiob.claritynow.domain.engine.catalog.Template
import com.kamsiob.claritynow.domain.engine.catalog.Variant
import com.kamsiob.claritynow.domain.engine.realize.MeasureKind
import com.kamsiob.claritynow.domain.engine.realize.Measures
import com.kamsiob.claritynow.domain.engine.realize.SlotBindings
import com.kamsiob.claritynow.domain.engine.validate.LengthLimits
import com.kamsiob.claritynow.domain.engine.validate.ValidatorVocabulary

/**
 * The seven gates that read the corpus as text. CLARITY_LOGIC_ENGINE.md 7.5, 7.7, 11.1, 11.3.
 *
 * The eighth, the one that renders every line against facts a simulated year produced, is in
 * [CorpusRenderGate], because it needs three minutes and these seven need a millisecond. That
 * split is the point: an author runs these after every batch of forty and the render gate
 * after every family.
 *
 * Everything here reads the parsed catalog rather than the raw markdown, so a line that the
 * parser does not reach cannot pass a gate by being invisible to it, and every finding
 * carries the `file:line key` the parser recorded.
 */
internal object CorpusGates {

    /** Every gate, over one catalog. */
    fun run(catalog: ClarityCatalog): GateReport = GateReport(
        listOf(
            sharedFragments(catalog),
            overusedConstructions(catalog),
            bannedVocabulary(catalog),
            slotBindings(catalog),
            unitNouns(catalog),
            lengthBands(catalog),
            registerDepth(catalog),
            nearDuplicates(catalog),
        ),
    )

    // ------------------------------------------------------------------ 1. fragments

    /**
     * Length of a shared run of words that counts as distinctive.
     *
     * **Six, and the reasoning is the shape of the corpus rather than a preference.** The
     * median authored line sits in the `MEDIUM` band, seven to fourteen words, so a shared
     * run of six is at least half of a typical line and usually most of its clause. That is
     * the length at which two lines stop sharing vocabulary and start being one line said
     * twice.
     *
     * Below it the check turns on the app's own subject matter. `of the last fourteen days`,
     * `at the front of`, `three weeks running` and `this week` are things any family
     * describing a fortnight, a queue or a trend has to say, and a gate that called those a
     * tell would be asking two families to describe the same fortnight in different English.
     */
    const val FRAGMENT_TOKENS = 6

    /**
     * How many of those six words must be content words.
     *
     * Two, so that a run which is mostly grammar does not qualify on length alone. `has been
     * the same for the` is six words and says nothing; `has been active in {} for` is six
     * words and is a sentence.
     */
    const val FRAGMENT_MIN_CONTENT = 2

    /**
     * No distinctive fragment appears in two families. 7.7.
     *
     * **Scoped to one purpose, which is where a reader could notice.** A Pulse statement and
     * a Report lead that share a clause are seen days apart on two different surfaces; a
     * headline and an observation that share one are seen in a single glance. That is
     * `CatalogIntegrity.fragmentsInTwoFamilies`'s reasoning and this gate keeps it rather
     * than inventing a second one. The cross purpose collisions are counted and printed all
     * the same, because a number nobody prints is a number nobody argues about.
     *
     * This gate is wider than the production check rather than a copy of it. That one
     * compares whole sentences, so it finds two families that authored the same sentence and
     * misses two families that authored the same clause inside different sentences, which is
     * the more common shape and the one an eight session authoring phase will produce.
     */
    fun sharedFragments(catalog: ClarityCatalog): GateOutcome {
        val owners = mutableMapOf<Pair<Purpose, String>, MutableMap<FamilyKey, Variant>>()
        val everywhere = mutableMapOf<String, MutableSet<FamilyKey>>()
        for (variant in catalog.allVariants) {
            val words = CorpusText.tokens(variant.statement.text).size
            for (size in FRAGMENT_TOKENS..words) {
                for (gram in CorpusText.ngrams(variant.statement.text, size).distinct()) {
                    if (CorpusText.contentWordsIn(gram) < FRAGMENT_MIN_CONTENT) continue
                    owners.getOrPut(variant.purpose to gram) { mutableMapOf() }.putIfAbsent(variant.family, variant)
                    everywhere.getOrPut(gram) { mutableSetOf() } += variant.family
                }
            }
        }
        val shared = owners.filterValues { it.size > 1 }
        val maximal = longestRuns(shared.mapValues { it.value.keys.toSet() }).keys
        val crossPurpose = longestRuns(
            everywhere.filterValues { it.size > 1 }.mapKeys { ANY_PURPOSE to it.key }.mapValues { it.value.toSet() },
        ).size
        val findings = mutableListOf<GateFinding>()
        val grandfathered = mutableListOf<GateFinding>()
        for (key in maximal.sortedBy { "${it.first} ${it.second}" }) {
            val (purpose, gram) = key
            val lines = shared.getValue(key)
            val finding = GateFinding(
                subject = "$purpose `$gram`",
                detail = "shared by " + lines.entries.sortedBy { it.key }
                    .joinToString(", ") { "${it.key} at ${it.value.origin}" },
            )
            if (CorpusGateBaseline.isRecordedFragment(purpose, gram, lines.keys)) grandfathered += finding
            else findings += finding
        }
        return GateOutcome(
            id = "fragment",
            name = "no distinctive fragment in two families of one purpose",
            citation = "CLARITY_LOGIC_ENGINE.md 7.7, a run of $FRAGMENT_TOKENS words or more with " +
                "$FRAGMENT_MIN_CONTENT or more content words",
            findings = findings,
            grandfathered = grandfathered,
            measured = "${maximal.size} clauses shared inside a purpose, $crossPurpose across the whole corpus",
        )
    }

    /**
     * Drops every shared run that a longer shared run already contains.
     *
     * Without this, one shared clause of nine words reports as four findings, because every
     * six word window inside it is shared too. An author would be told four times about one
     * collision and would have to work out that it was one. Only the longest run between the
     * same set of families survives.
     */
    private fun longestRuns(
        shared: Map<Pair<Purpose, String>, Set<FamilyKey>>,
    ): Map<Pair<Purpose, String>, Set<FamilyKey>> = shared.filterNot { (key, families) ->
        val (purpose, gram) = key
        shared.any { (otherKey, otherFamilies) ->
            otherKey.first == purpose &&
                otherKey.second.length > gram.length &&
                otherKey.second.containsRun(gram) &&
                otherFamilies == families
        }
    }

    /** The stand in purpose used when the same walk is run over the whole corpus at once. */
    private val ANY_PURPOSE = Purpose.PULSE

    /** True when [inner] is a whole word run inside this one. */
    private fun String.containsRun(inner: String): Boolean =
        startsWith("$inner ") || endsWith(" $inner") || contains(" $inner ")

    // ------------------------------------------------------------------ 2. constructions

    /**
     * The shapes this gate adds to the four the catalog already checks.
     *
     * A construction is a shape rather than a phrase, so each of these is written against a
     * normalized sentence with its commas intact, using `CatalogIntegrity.sentencesOf` so
     * that the production check and this one cannot disagree about what a sentence is.
     *
     * The first three are the shapes the phase 9 brief names by hand. `twoBeats` is the one
     * the measurement added: two short sentences in one line, `Nine things arrived. Six
     * left.`, which is the most recognizable rhythm in the whole corpus and the easiest for
     * an author to reach for forty times running.
     */
    val EXTRA_CONSTRUCTIONS: Map<String, Regex> = mapOf(
        "notXthenY" to Regex("""^not\b[^,]{2,40}$"""),
        "xCommaAndY" to Regex("""^[^,]{3,40},\s*and\s+[^,]{3,60}$"""),
        "nOfThem" to Regex("""\band\s+\{}\s+of\s+(?:them|those|these)\b"""),
        "whateverElse" to Regex("""^whatever else\b"""),
        "twoBeats" to Regex("""^\S[^.?]{0,24}[.?]\s+\S[^.?]{0,24}[.?]$"""),
    )

    /** Every shape checked: the catalog's four, then the five above. */
    val CONSTRUCTIONS: Map<String, Regex> = CatalogIntegrity.CONSTRUCTIONS + EXTRA_CONSTRUCTIONS

    /**
     * Shapes measured for concentration inside a bench and not against the family cap.
     *
     * **One entry, and it is here because the measurement said so.** `twoBeats`, two short
     * sentences in one line, is in fifteen of the seventy eight families. A shape that
     * widespread is the house voice rather than a tell, and capping it at two families would
     * record a fact about the corpus instead of enforcing a rule on it: the only thing such
     * a cap could ever catch is a sixteenth family adopting a rhythm the other fifteen are
     * allowed. What is worth catching is one bench leaning on it, and the concentration
     * reading below catches exactly that.
     *
     * Nothing else earns this. `xCommaAndY` is in ten families and stays under the cap
     * because the phase 9 brief names it as a construction by hand, so an eleventh family
     * reaching for it is a finding the owner asked for.
     */
    val HOUSE_SHAPES: Set<String> = setOf("twoBeats")

    /** 7.7. No construction in more than two families. */
    const val CONSTRUCTION_FAMILY_CAP = CatalogIntegrity.CONSTRUCTION_CAP

    /** The share of one bench a single construction may account for. */
    const val CONSTRUCTION_BENCH_SHARE = 0.34

    /** Below this a share is one line moving the number by more than a tenth, so it is not read. */
    const val CONSTRUCTION_MIN_BENCH = 8

    /**
     * No rhetorical construction in more than two families, and none carrying a third of a
     * bench. 7.7 and 13.
     *
     * **Two readings, because the family cap alone does not protect a bench being grown from
     * twelve lines to sixty.** A construction already allowed in a family is allowed there
     * forever by the first reading, so an author with a favorite shape could write it forty
     * times inside one hot bench and fail nothing. The second reading is what stops that, and
     * it is the reading that matters most in this phase: the family cap is about the corpus
     * looking machine written across families, and the bench share is about one bench
     * looking machine written on its own.
     */
    fun overusedConstructions(catalog: ClarityCatalog): GateOutcome {
        val owners = mutableMapOf<String, MutableMap<FamilyKey, Variant>>()
        for (variant in catalog.allVariants) {
            for (sentence in shapeUnits(variant.statement.text)) {
                for ((name, shape) in CONSTRUCTIONS) {
                    if (shape.containsMatchIn(sentence)) {
                        owners.getOrPut(name) { mutableMapOf() }.putIfAbsent(variant.family, variant)
                    }
                }
            }
        }
        val findings = mutableListOf<GateFinding>()
        val grandfathered = mutableListOf<GateFinding>()
        for ((name, lines) in owners.entries.sortedBy { it.key }) {
            if (name in HOUSE_SHAPES || lines.size <= CONSTRUCTION_FAMILY_CAP) continue
            val allowed = CorpusGateBaseline.constructionAllowance(name)
            val beyond = lines.keys - allowed
            val finding = GateFinding(
                subject = "`$name`",
                detail = "in ${lines.size} families, ${lines.keys.sorted()}, cap is $CONSTRUCTION_FAMILY_CAP" +
                    if (beyond.isEmpty()) "" else ". Beyond the recorded set: " +
                        beyond.sorted().joinToString(", ") { "$it at ${lines.getValue(it).origin}" },
            )
            if (beyond.isEmpty()) grandfathered += finding else findings += finding
        }
        val concentrated = mutableListOf<GateFinding>()
        for (bench in CorpusBenches.of(catalog)) {
            if (bench.size < CONSTRUCTION_MIN_BENCH) continue
            for ((name, shape) in CONSTRUCTIONS.entries.sortedBy { it.key }) {
                val hits = bench.lines.filter { line ->
                    shapeUnits(line.statement.text).any { shape.containsMatchIn(it) }
                }
                if (hits.size <= CONSTRUCTION_BENCH_SHARE * bench.size) continue
                concentrated += GateFinding(
                    subject = bench.id,
                    detail = "`$name` is ${hits.size} of ${bench.size} lines, over " +
                        "${(CONSTRUCTION_BENCH_SHARE * PERCENT).toInt()} percent. " +
                        hits.take(SAMPLE).joinToString(", ") { it.key },
                )
            }
        }
        return GateOutcome(
            id = "construction",
            name = "no construction in more than two families, and none over a third of a bench",
            citation = "CLARITY_LOGIC_ENGINE.md 7.7 and 13, ${CONSTRUCTIONS.size} shapes",
            findings = findings + concentrated,
            grandfathered = grandfathered,
            measured = owners.entries.sortedByDescending { it.value.size }
                .joinToString(", ") { "${it.key} in ${it.value.size} families" } +
                ". ${CONSTRUCTIONS.size - owners.size} of ${CONSTRUCTIONS.size} shapes absent, " +
                "${concentrated.size} benches over the share",
        )
    }

    /**
     * What a construction is matched against: each sentence, and then the whole line.
     *
     * Both, because a construction can live inside one sentence or across two. `X, not Y.`
     * is a sentence shape and `Nine things arrived. Six left.` is a line shape, and a check
     * that only saw sentences would be blind to every two beat line in the corpus, which is
     * the rhythm an author reaches for most easily forty times running.
     *
     * The sentence form is `CatalogIntegrity.sentencesOf`, so the production check and this
     * one cannot disagree about what a sentence is. The line form keeps its punctuation,
     * which is the only way the boundary between two beats is visible at all.
     */
    fun shapeUnits(text: String): List<String> =
        CatalogIntegrity.sentencesOf(text) + Template.MARKER.replace(text, CorpusText.MARKER_TOKEN).lowercase().trim()

    // ------------------------------------------------------------------ 3. vocabulary

    /**
     * Zero banned vocabulary, dashes, exclamation marks, characters above ASCII and
     * spellings from the other side of the Atlantic, over every rendered string in the
     * corpus. 11.3 and section 8, checks 8, 10 and 11.
     *
     * **The patterns are `ValidatorVocabulary`'s, not a copy of them.** A second list would
     * be a second list to disagree with the first, and the first is the one that vetoes a
     * sentence at runtime. What this adds to `CorpusVocabularyTest` is the address: that test
     * reads the markdown and reports a file and a line, and this one reads the parsed catalog
     * and reports the family, the stage and the bench the line sits on, which is what an
     * author needs to fix it.
     *
     * Questions and response labels are checked with the statements. All three are rendered
     * text, and a banned word is banned in a tappable label exactly as much as in a sentence.
     */
    fun bannedVocabulary(catalog: ClarityCatalog): GateOutcome {
        val patterns = ValidatorVocabulary.BANNED_WORDS +
            ValidatorVocabulary.BANNED_PHRASES +
            ValidatorVocabulary.BLAME_CONSTRUCTIONS +
            ValidatorVocabulary.ESTIMATE_DELTA_FORMS +
            ValidatorVocabulary.OTHER_SPELLING_FORMS
        val findings = mutableListOf<GateFinding>()
        var checked = 0
        for (line in everyRenderedString(catalog)) {
            checked++
            patterns.firstOrNull { (pattern, _) -> pattern.containsMatchIn(line.text) }?.let { (_, name) ->
                findings += GateFinding(line.subject, "uses `$name`: ${line.text}", line.origin)
            }
            when {
                line.text.contains(ValidatorVocabulary.EM_DASH) ->
                    findings += GateFinding(line.subject, "has an em dash: ${line.text}", line.origin)
                line.text.contains(ValidatorVocabulary.EN_DASH) ->
                    findings += GateFinding(line.subject, "has an en dash: ${line.text}", line.origin)
                line.text.contains('!') ->
                    findings += GateFinding(line.subject, "has an exclamation mark: ${line.text}", line.origin)
                line.text.any { it.code > ASCII_CEILING } ->
                    findings += GateFinding(line.subject, "has a character above ASCII: ${line.text}", line.origin)
            }
            if (line.register != Register.NEUTRAL_AGENT) continue
            ValidatorVocabulary.AGENT_DELETED_PASSIVES
                .firstNotNullOfOrNull { (pattern, name) -> pattern.find(line.text)?.let { name to it.value } }
                ?.let { (name, hit) ->
                    findings += GateFinding(line.subject, "reads as $name: `$hit` in ${line.text}", line.origin)
                }
        }
        return GateOutcome(
            id = "vocabulary",
            name = "no banned word, dash, exclamation mark, non ASCII character or other spelling",
            citation = "CLARITY_LOGIC_ENGINE.md 11.3 and 8, checks 8, 10 and 11",
            findings = findings,
            measured = "$checked rendered strings read against ${patterns.size} patterns",
        )
    }

    /** One rendered string with somewhere to look when it fails. */
    private data class Rendered(
        val subject: String,
        val text: String,
        val origin: String,
        val register: Register?,
    )

    /**
     * Every string the corpus can put on a screen.
     *
     * Statements, extensions, Pulse questions, response option labels, and the auxiliary
     * benches. The auxiliary ones are here and nowhere else in this file: they are not
     * families, so no bench rule applies to them, and they are still text a person reads.
     */
    private fun everyRenderedString(catalog: ClarityCatalog): List<Rendered> = buildList {
        for (family in catalog.families) {
            for (stage in family.stages) {
                for (variant in stage.variants + stage.extensions) {
                    add(
                        Rendered(
                            subject = "${family.purpose} ${family.key} s${stage.index} ${variant.key}",
                            text = variant.statement.text,
                            origin = variant.origin,
                            register = variant.register,
                        ),
                    )
                }
                for (question in stage.questions) {
                    add(
                        Rendered(
                            subject = "${family.purpose} ${family.key} s${stage.index} ${question.key}",
                            text = question.text.text,
                            origin = "${question.sourceFile}:${question.sourceLine} ${question.key}",
                            register = null,
                        ),
                    )
                }
                for (pair in stage.responsePairs) {
                    for (option in pair.options) {
                        add(
                            Rendered(
                                subject = "${family.purpose} ${family.key} s${stage.index} ${option.key}",
                                text = option.label,
                                origin = "${pair.sourceFile}:${pair.sourceLine} ${pair.key}",
                                register = null,
                            ),
                        )
                    }
                }
            }
        }
        for ((bench, lines) in catalog.auxiliary) {
            for (line in lines) {
                add(
                    Rendered(
                        subject = "auxiliary $bench ${line.key}",
                        text = line.text,
                        origin = "${line.sourceFile}:${line.sourceLine} ${line.key}",
                        register = line.register,
                    ),
                )
            }
        }
    }

    // ------------------------------------------------------------------ 6a. bindings

    /**
     * Every marker in every line has a binding, and every binding names a measure.
     *
     * **The fast half of gate 6.** The slow half runs a simulated year and asks whether a
     * line can actually be filled from facts a life produced; this one asks the part of that
     * question a table can answer in a millisecond, which is whether the marker has any fact
     * behind it at all. That split matters for how the gates get used: an author finishing a
     * batch of forty needs to know in seconds that `{dayCount}` is not bound in this family,
     * not in three minutes.
     *
     * It is also the harder finding of the two. A slot that reads nothing today might read
     * something next Tuesday; a slot with no binding is a line that can never be said, on any
     * day, on any device, and nothing in the app will ever show it.
     *
     * Lines held out by `SlotBindings.EXCLUDED` are skipped rather than reported. They are
     * unbound on purpose, each with a recorded reason, and reporting them here would bury the
     * ones that are unbound by accident.
     */
    fun slotBindings(catalog: ClarityCatalog): GateOutcome {
        val findings = mutableListOf<GateFinding>()
        val grandfathered = mutableListOf<GateFinding>()
        var excluded = 0
        var checked = 0
        for (variant in catalog.allVariants) {
            if (SlotBindings.isExcluded(variant.key)) {
                excluded++
                continue
            }
            checked++
            val bindings = SlotBindings.bindingsFor(variant.purpose, variant.family, variant.stage, variant.key)
            for (slot in variant.statement.slots.sorted()) {
                val binding = bindings[slot]
                val detail = when {
                    binding == null -> "{$slot} has no binding in SlotBindings for " +
                        "${variant.purpose} ${variant.family} s${variant.stage}, so the line can never render"
                    Measures.byId(binding.measure) == null ->
                        "{$slot} is bound to `${binding.measure}`, which is not a measure this app declares"
                    else -> continue
                }
                val finding = GateFinding(variant.key, detail, variant.origin)
                if (CorpusGateBaseline.isRecordedUnrenderable(variant.key)) grandfathered += finding
                else findings += finding
            }
        }
        return GateOutcome(
            id = "binding",
            name = "every marker in every line has a fact behind it",
            citation = "CLARITY_LOGIC_ENGINE.md 7.2, the slot completeness rule",
            findings = findings,
            grandfathered = grandfathered,
            measured = "${grandfathered.size + findings.size} unbound markers across $checked lines, " +
                "$excluded lines held out of their benches on purpose",
        )
    }

    /**
     * Which markers a hot bench can fill, for an author about to write forty lines into it.
     *
     * Printed rather than asserted. The rule an author has to keep is that every slot they
     * use has a binding, and the only way to keep it today is to read a nine hundred line
     * table in `SlotBindings`; this is that table turned around, listed by the bench somebody
     * is actually writing for.
     */
    fun bindableSlotTable(catalog: ClarityCatalog): String = buildString {
        appendLine("--------------------------------------------------------------------")
        appendLine("markers with a binding, hot benches only, for the lines phase 9 adds")
        appendLine("--------------------------------------------------------------------")
        for (bench in CorpusBenches.of(catalog).filter { it.isHot && it.kind == Bench.Kind.STATEMENT }) {
            val bindable = bench.lines
                .flatMap { SlotBindings.bindingsFor(it.purpose, it.family, it.stage, it.key).keys }
                .toSortedSet()
            val used = bench.lines.flatMap { it.statement.slots }.toSortedSet()
            appendLine(
                "  ${bench.id}: bindable ${bindable.ifEmpty { "none" }}" +
                    if ((used - bindable).isEmpty()) "" else ", used with no binding ${used - bindable}",
            )
        }
    }


    // ------------------------------------------------------------------ 4b. unit nouns

    /**
     * A marker standing in front of a unit noun is bound to a measure that counts in that unit.
     *
     * **This is the failure the binding gate cannot see and the render gate cannot see
     * either.** A slot with no binding drops its line, which is the harmless outcome
     * `SlotBindings` is built around. A slot bound to the *wrong* measure renders, and it
     * passes layer 5, because check 3 re-reads the `FactRef` the binding produced and the
     * number really is that number. What reaches the screen is a sentence that is
     * arithmetically correct and false, which 1.1 calls the one failure there is no
     * recovering from.
     *
     * `ob.since.e02` is the shape, and it is what this gate was written from: *It has been
     * {n} weeks* with `{n}` bound to the family's event count rendered *It has been 47
     * weeks* about a week five weeks ago. Nothing in the build could see it. The binding
     * gate saw a marker with a binding, the render gate saw a line that filled and passed,
     * and the two together are why it had survived every pass so far.
     *
     * ## Why the check is possible at all
     *
     * A [Measure] carries the noun it counts, because 7.2 makes the measure responsible for
     * the plural. So where an authored line writes the noun out after the marker, the line
     * has said which quantity it means in English and the table has said which quantity it
     * means in code, and the two can be compared. Nothing else in the build compares them.
     *
     * ## Why only some nouns
     *
     * **Only a noun that carries a dimension counts as a claim.** `things`, `items`,
     * `moves`, `events` and `times` are interchangeable ways to count occurrences, and
     * English lets any of them stand in front of any count: *you swapped {n} times*,
     * *{n} things happened*. A gate that read those as claims would report forty four
     * findings on this corpus, forty three of which are one word standing in for another.
     * `days`, `weeks`, `months`, `minutes`, `areas`, `sessions` and `sittings` are
     * different: each names what is being counted, and a count of one of them standing for
     * a count of another is a false sentence rather than a synonym.
     *
     * The asymmetry is deliberate and runs one way. A dimensioned noun in the corpus must
     * meet a measure in the same dimension. A dimensioned **measure** in front of a generic
     * corpus noun is fine, because *{sessions} times* is exactly how English counts
     * sessions.
     *
     * [Slot.Text] and [Slot.DateRef] slots are skipped entirely. A name can stand in front
     * of any word at all, and both false positives on this corpus were of that shape:
     * *It was a {areaName} week* and *{areaName} moves in bursts*.
     */
    fun unitNouns(catalog: ClarityCatalog): GateOutcome {
        val findings = mutableListOf<GateFinding>()
        val grandfathered = mutableListOf<GateFinding>()
        var checked = 0
        for (variant in catalog.allVariants) {
            if (SlotBindings.isExcluded(variant.key)) continue
            val bindings = SlotBindings.bindingsFor(variant.purpose, variant.family, variant.stage, variant.key)
            for ((slot, noun) in markersBeforeAUnit(variant.statement.text)) {
                val measure = bindings[slot]?.let { Measures.byId(it.measure) } ?: continue
                checked++
                val wanted = DIMENSIONS.getValue(noun)
                val detail = when (measure.kind) {
                    MeasureKind.TEXT, MeasureKind.DATE -> continue
                    MeasureKind.DAYS, MeasureKind.PERCENT ->
                        "{$slot} stands in front of `$noun` and is bound to `${measure.id}`, a " +
                            "${measure.kind} slot, which renders its own unit and never a bare number"
                    MeasureKind.COUNT ->
                        if (DIMENSIONS[measure.plural.lowercase()] == wanted) {
                            continue
                        } else {
                            "{$slot} stands in front of `$noun` and is bound to `${measure.id}`, which " +
                                "counts ${measure.plural}. The line names a count of $wanted and the " +
                                "table reads something else, so the sentence would be arithmetic that is " +
                                "right about a quantity nobody asked for"
                        }
                }
                val finding = GateFinding(variant.key, detail, variant.origin)
                if (CorpusGateBaseline.isRecordedMisbound(variant.key, slot)) grandfathered += finding
                else findings += finding
            }
        }
        return GateOutcome(
            id = "unit",
            name = "a marker in front of a unit noun counts in that unit",
            citation = "CLARITY_LOGIC_ENGINE.md 7.2 and 8 check 3, against Measure.singular and plural",
            findings = findings,
            grandfathered = grandfathered,
            measured = "$checked markers stand in front of one of the ${DIMENSIONS.size} " +
                "dimensioned nouns the corpus uses",
        )
    }

    /** Every marker in [text] immediately followed by a dimensioned noun, lowercased. */
    private fun markersBeforeAUnit(text: String): List<Pair<String, String>> =
        Template.MARKER.findAll(text).mapNotNull { marker ->
            val after = NEXT_WORD.find(text, marker.range.last + 1) ?: return@mapNotNull null
            if (after.range.first != marker.range.last + 1) return@mapNotNull null
            val noun = after.groupValues[1].lowercase()
            if (noun !in DIMENSIONS) return@mapNotNull null
            marker.groupValues[1] to noun
        }.toList()

    /**
     * The nouns that name what is being counted, and what each of them names.
     *
     * Read out of the corpus and out of [Measures], both forms of each. Everything absent
     * from this map is a generic countable and makes no claim this gate can check.
     *
     * **`hours` is deliberately absent, and it is the one exception worth stating.** The
     * only corpus line that uses it is `ob.est.l02`, *A thing estimated at an hour tends to
     * be active for about {n} hours*, where `{n}` is the estimate multiple and the sentence
     * multiplies it by a unit it states itself. The line is true and the marker counts
     * `times`; a gate that read the following noun as the unit would call it a defect. One
     * line is not worth a rule with a hole in it, so the noun is out rather than exempted.
     */
    private val DIMENSIONS: Map<String, String> = mapOf(
        "day" to "days", "days" to "days",
        "week" to "weeks", "weeks" to "weeks",
        "month" to "months", "months" to "months",
        "minute" to "minutes", "minutes" to "minutes",
        "area" to "areas", "areas" to "areas",
        "session" to "sessions", "sessions" to "sessions",
        "sitting" to "sessions", "sittings" to "sessions",
    )

    private val NEXT_WORD = Regex("""\s+([A-Za-z]+)""")

    // ------------------------------------------------------------------ 4. length bands

    /** 11.1's smallest declared bench, and the smallest size at which a share is a reading. */
    const val BAND_MIN_BENCH = 4

    /** No band holds more than this share of a bench. */
    const val BAND_SHARE_CEILING = 60

    /** How many distinct word counts a band needs before a surface is asked to reach it. */
    const val BAND_ROOM = 3

    /**
     * No length band holds more than sixty percent of a bench. 7.5.
     *
     * **Measured with `LengthBands`, never read from a tag**, which is the same rule 7.5
     * puts on the band itself: an authored `[S]` marker is advisory and the computed value
     * always wins, so a gate reading the tag would be checking what somebody meant rather
     * than what they wrote.
     *
     * **A surface that can only reach one band is exempt**, and which surfaces those are is
     * computed rather than listed. A Report headline is capped at seven words by check 9, so
     * `SHORT` is the only band with room in it and every headline bench is at a hundred
     * percent of one band by construction. Asking otherwise would be asking for a rule that
     * cannot be satisfied, and a gate that cannot be satisfied is a gate somebody turns off.
     * Every other surface reaches at least two bands and is held to the cap.
     */
    fun lengthBands(catalog: ClarityCatalog): GateOutcome {
        val findings = mutableListOf<GateFinding>()
        val grandfathered = mutableListOf<GateFinding>()
        var measurable = 0
        for (bench in CorpusBenches.of(catalog)) {
            if (bench.size < BAND_MIN_BENCH || bandsWithRoom(bench.purpose) < 2) continue
            measurable++
            val counts = bench.lines.groupingBy { it.lengthBand }.eachCount()
            val worst = counts.maxByOrNull { it.value } ?: continue
            if (worst.value * PERCENT <= BAND_SHARE_CEILING * bench.size) continue
            val finding = GateFinding(
                subject = bench.id,
                detail = "${worst.value} of ${bench.size} lines are ${worst.key}, over " +
                    "$BAND_SHARE_CEILING percent. Bands: " + LengthBand.entries.joinToString(", ") {
                        "$it ${counts[it] ?: 0}"
                    },
            )
            if (CorpusGateBaseline.bandExemptAt(bench.id, bench.size)) grandfathered += finding
            else findings += finding
        }
        return GateOutcome(
            id = "lengthBand",
            name = "no length band over $BAND_SHARE_CEILING percent of a bench",
            citation = "CLARITY_LOGIC_ENGINE.md 7.5, computed by LengthBands and never read from a tag",
            findings = findings,
            grandfathered = grandfathered,
            measured = "${findings.size + grandfathered.size} of $measurable measurable benches over the cap",
        )
    }

    /**
     * How many bands a purpose can actually reach, given the word limit check 9 puts on it.
     *
     * A band counts only if the limit leaves it [BAND_ROOM] distinct word counts. One word
     * count is a knife edge rather than a band: a Report headline may be seven words and
     * therefore `MEDIUM`, and requiring forty percent of a headline bench to be exactly seven
     * words long is a rule about arithmetic rather than about rhythm.
     */
    fun bandsWithRoom(purpose: Purpose): Int {
        val ceiling = minOf(LengthLimits.maxWords(purpose), LengthBands.LONG_MAX)
        val bounds = listOf(
            1 to LengthBands.SHORT_MAX,
            LengthBands.SHORT_MAX + 1 to LengthBands.MEDIUM_MAX,
            LengthBands.MEDIUM_MAX + 1 to LengthBands.LONG_MAX,
        )
        return bounds.count { (low, high) -> minOf(high, ceiling) - low + 1 >= BAND_ROOM }
    }

    // ------------------------------------------------------------------ 5. registers

    /** A register present at a stage needs at least this many lines. */
    const val REGISTER_FLOOR = 2

    /**
     * Every register a stage uses carries at least two lines, and every hot stage carries all
     * of its volume's core registers. 7.4.
     *
     * **The failure this closes is invisible on a screen.** The realizer asks for a register
     * and takes the first line at that stage in it. A stage with one reflective line hands
     * back that line every evening until the ninety day exclusion in 7.6 takes it away, and
     * then hands back a different register instead. Nothing looks broken either way: the
     * reader sees one line often and then stops seeing it, and no test in the build has ever
     * been able to tell that from a bench doing its job.
     *
     * **The core registers are derived from the volume rather than listed.** Each volume
     * declares which registers it permits, and the core is that set without `EDITORIAL` and
     * `NEUTRAL_AGENT`: 7.4 budgets editorial at two leads per report and reaches the neutral
     * agent only through a rule marked unflattering, so neither is asked of every stage,
     * while plain, observational and reflective are the fallback order itself and a stage
     * missing one has a register the realizer will ask for and never get.
     */
    fun registerDepth(catalog: ClarityCatalog): GateOutcome {
        val findings = mutableListOf<GateFinding>()
        val grandfathered = mutableListOf<GateFinding>()
        var thin = 0
        for (bench in CorpusBenches.of(catalog)) {
            val counts = bench.lines.groupingBy { it.register }.eachCount()
            for ((register, count) in counts.entries.sortedBy { it.key.ordinal }) {
                if (count >= REGISTER_FLOOR) continue
                thin++
                val only = bench.lines.first { it.register == register }
                val finding = GateFinding(
                    subject = bench.id,
                    detail = "one $register line in a bench of ${bench.size}, so the realizer " +
                        "repeats it whenever it asks for that voice",
                    origin = only.origin,
                )
                if (CorpusGateBaseline.registerExemptAt(bench.id, bench.size)) grandfathered += finding
                else findings += finding
            }
            if (!bench.isHot) continue
            for (register in coreRegisters(bench.purpose)) {
                val count = counts[register] ?: 0
                if (count >= REGISTER_FLOOR) continue
                if (count == 1) continue
                val finding = GateFinding(
                    subject = bench.id,
                    detail = "a hot bench of ${bench.size} with no $register line at all, and " +
                        "$register is in this volume's fallback order",
                )
                if (CorpusGateBaseline.registerExemptAt(bench.id, bench.size)) grandfathered += finding
                else findings += finding
            }
        }
        return GateOutcome(
            id = "register",
            name = "at least $REGISTER_FLOOR variants per register per stage",
            citation = "CLARITY_LOGIC_ENGINE.md 7.4 and 7.6",
            findings = findings,
            grandfathered = grandfathered,
            measured = "$thin single line registers across the corpus",
        )
    }

    /** The registers a volume's fallback order can always reach. */
    fun coreRegisters(purpose: Purpose): Set<Register> =
        registersWithRoom(purpose) - Register.EDITORIAL - Register.NEUTRAL_AGENT

    /**
     * Which registers a purpose's own surface can carry, given how its volume is written.
     *
     * **The Report tags a register in section 2 and nowhere else.** `ReportWalker` fails the
     * parse on a register tag under a headline or a pattern heading and gives every line in
     * those two sections the fallback, so `PLAIN` is not merely what they use, it is the only
     * thing they can be. Asking a hot headline bench for an observational line is asking for a
     * line the parser rejects.
     *
     * That is the same shape of unsatisfiable rule [bandsWithRoom] exists to avoid one gate
     * up, and it is computed here for the same reason: a gate nobody can satisfy is a gate
     * somebody turns off. The restriction costs nothing where a bench can carry a register,
     * because every section 2 bench is still held to plain and observational.
     */
    fun registersWithRoom(purpose: Purpose): Set<Register> = when (purpose) {
        Purpose.REPORT_HEADLINE, Purpose.REPORT_PATTERN -> setOf(Register.PLAIN)
        Purpose.REPORT_OBSERVATION, Purpose.PULSE,
        Purpose.MOMENTUM_HEADLINE, Purpose.AREAS_BANNER,
        -> volumeOf(purpose).permittedRegisters
    }

    /** Which file a purpose is authored in. */
    fun volumeOf(purpose: Purpose): CorpusVolume = when (purpose) {
        Purpose.PULSE -> CorpusVolume.PULSE
        Purpose.REPORT_HEADLINE, Purpose.REPORT_OBSERVATION, Purpose.REPORT_PATTERN -> CorpusVolume.REPORT
        Purpose.MOMENTUM_HEADLINE, Purpose.AREAS_BANNER -> CorpusVolume.MOMENTUM
    }

    // ------------------------------------------------------------------ 7. near duplicates

    /**
     * How many content words two lines in one bench may differ by and still be one line.
     *
     * **One, and one means a word present in one line and absent from the other.** A word
     * swapped for a different word is a distance of two by [CorpusText.signatureDistance],
     * because a swap is a removal and an addition, and it is deliberately not caught: a
     * swapped content word is usually a different claim. `A quiet week` and `A still week`
     * are two ways to describe a week and the corpus wants both; `Noted.` and `Noted for the
     * week.` are one line written twice.
     *
     * The threshold was set against the corpus rather than guessed. At zero it finds one
     * pair, two lines with identical text under different keys. At one it finds eleven, and
     * every one of them reads as the same sentence twice. At two it begins finding the
     * corpus's deliberate singular and plural pair, `Based on {n} Pulse responses` against
     * `Based on {n} Pulse response`, which is two lines on purpose.
     */
    const val DUPLICATE_DISTANCE = 1

    /**
     * Two lines in one bench that differ only in function words, in word order, or by one
     * content word, are one line.
     *
     * Scoped to one bench because that is the pool 7.6 chooses from: two lines a stage apart
     * describe different magnitudes and are meant to be close, while two lines in one bench
     * are two chances at the same moment and a reader gets one of them.
     */
    fun nearDuplicates(catalog: ClarityCatalog): GateOutcome {
        val findings = mutableListOf<GateFinding>()
        val grandfathered = mutableListOf<GateFinding>()
        var pairs = 0
        for (bench in CorpusBenches.of(catalog)) {
            val signatures = bench.lines.map { it to CorpusText.contentSignature(it.statement.text) }
            for (left in signatures.indices) {
                for (right in left + 1 until signatures.size) {
                    val (one, first) = signatures[left]
                    val (other, second) = signatures[right]
                    val distance = CorpusText.signatureDistance(first, second)
                    if (distance > DUPLICATE_DISTANCE) continue
                    pairs++
                    val finding = GateFinding(
                        subject = bench.id,
                        detail = "${one.key} and ${other.key} differ by $distance content word: " +
                            "`${one.statement.text}` against `${other.statement.text}`",
                        origin = other.origin,
                    )
                    if (CorpusGateBaseline.isRecordedDuplicate(one.key, other.key)) grandfathered += finding
                    else findings += finding
                }
            }
        }
        return GateOutcome(
            id = "duplicate",
            name = "no two lines in one bench differing by at most $DUPLICATE_DISTANCE content word",
            citation = "CLARITY_LOGIC_ENGINE.md 7.6 and 13, the repetition failure mode",
            findings = findings,
            grandfathered = grandfathered,
            measured = "$pairs pairs inside the threshold",
        )
    }

    private const val PERCENT = 100
    private const val ASCII_CEILING = 127
    private const val SAMPLE = 4

    /**
     * The construction allowance the catalog already records for its own four shapes.
     *
     * Read by [CorpusGateBaseline] rather than restated there, so the two lists cannot
     * disagree about which families `tripleThen` is already allowed in.
     */
    fun catalogAllowance(name: String): Set<FamilyKey> =
        KnownCorpusViolations.CONSTRUCTION_ALLOWANCE[name].orEmpty()
}
