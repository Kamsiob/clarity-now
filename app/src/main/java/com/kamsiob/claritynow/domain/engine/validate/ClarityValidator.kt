package com.kamsiob.claritynow.domain.engine.validate

import com.kamsiob.claritynow.domain.engine.CandidateValidator
import com.kamsiob.claritynow.domain.engine.FactRef
import com.kamsiob.claritynow.domain.engine.FactSet
import com.kamsiob.claritynow.domain.engine.Validated
import com.kamsiob.claritynow.domain.engine.catalog.LengthBands
import com.kamsiob.claritynow.domain.engine.catalog.Purpose
import com.kamsiob.claritynow.domain.engine.catalog.Register
import com.kamsiob.claritynow.domain.engine.catalog.SlotKey
import com.kamsiob.claritynow.domain.engine.catalog.Template
import com.kamsiob.claritynow.domain.engine.realize.Candidate
import com.kamsiob.claritynow.domain.engine.realize.FactLookup
import com.kamsiob.claritynow.domain.engine.realize.MeasureValue
import com.kamsiob.claritynow.domain.engine.realize.Measures
import com.kamsiob.claritynow.domain.engine.realize.Slot
import java.time.ZoneId

/**
 * The eleven checks in CLARITY_LOGIC_ENGINE.md 8, in order, over one realized candidate.
 *
 * Ten of them are section 8's own. The eleventh is the estimate delta veto of
 * `MASTER_BUILD_PROMPT.md` 14b.8, appended rather than inserted so the ten keep the
 * numbers three documents cite them by.
 *
 * ## What this layer is for
 *
 * Layers 1 to 4 are already careful. The facts come from one query facade, the rules carry
 * no strings, archived areas are absent from the fact set by construction, and the realizer
 * never sees a live entity table. This layer exists because every one of those is a
 * property somebody has to keep true while editing, and the prime directive in section 1
 * does not degrade gracefully: **one fabricated area name or off by one number permanently
 * destroys the credibility of everything else the app says**, and the person reading it has
 * no way to verify anything afterwards.
 *
 * So the last thing that happens before a sentence is allowed out is that it is compared
 * against the facts it claims to be about.
 *
 * ## Behavior on a veto
 *
 * [veto] answers null when a candidate may be shown and the reason when it may not.
 * `ClarityEngine` owns the loop that a veto turns: it realizes the next ranked selection
 * and tries again, and returns `Silent(ALL_CANDIDATES_VETOED)` when the list runs out.
 * Never an exception, never a fallback sentence, and never the unvalidated candidate.
 * `MASTER_BUILD_PROMPT.md` 11.4 states the corollary that keeps this honest: if the
 * validator vetoes something it should not, **the rule is wrong, not the validator**.
 *
 * ## The order is data, not control flow
 *
 * Section 8 numbers the checks and the numbering is part of the specification, so [CHECKS]
 * is a list in that order and a test asserts it holds every member of [ValidationCheck]
 * exactly once. Running them in order matters for one practical reason beyond obedience: a
 * candidate that violates several checks is reported against the lowest numbered one, so
 * the detail names the most fundamental thing wrong with it rather than whichever check
 * happened to run first.
 *
 * ## Which text each check reads
 *
 * Checks 7, 8 and 10 read the rendered sentence **with the person's own strings masked
 * out**. An area is named by the person who made it, and a person may reasonably spell a
 * name the way they were taught, put an exclamation mark in an item title, or write one in
 * a language this file cannot spell. Vetoing the sentence would silence the engine over
 * somebody's own vocabulary, and the app already shows that exact string on every other
 * screen. The words the app chose are the words the app is answerable for, so those are the
 * words these three checks read. Check 9 measures the sentence as it will appear, because a
 * long name really does make a long headline.
 *
 * ## Why it holds a zone
 *
 * `FactLookup` re-reads a `FactRef` through the measure that produced it, and three of
 * those measures are about local days. The zone the extractor counted with is therefore
 * handed in at construction, exactly as `ClarityEngine` takes it, rather than read from the
 * ambient default. `ZoneId.systemDefault()` is the documented cause of two Pulses in one
 * day or none at all, and `DomainPurityTest` fails the build on it.
 */
class ClarityValidator(private val zone: ZoneId) : CandidateValidator {

    /** The engine's seam. Null when [candidate] may be shown, or the reason it may not. */
    override fun veto(candidate: Candidate, facts: FactSet): String? =
        when (val result = validate(candidate, facts)) {
            is ValidationResult.Passed -> null
            is ValidationResult.Vetoed -> "${result.check}: ${result.detail}"
        }

    /**
     * Checks [candidate] against [facts]. The first failing check wins.
     *
     * Richer than [veto] and used by the simulator and by tests, which need the check that
     * fired rather than one string. [veto] is this function with the detail flattened,
     * because the engine only ever asks whether a sentence may be shown.
     *
     * [maxWords] overrides the limit for the candidate's purpose and exists for one caller:
     * layer 6, whose closing lines are limited to a different number by section 8 and which
     * is not a [Purpose] of its own. See [LengthLimits].
     */
    fun validate(candidate: Candidate, facts: FactSet, maxWords: Int? = null): ValidationResult {
        val inspection = Inspection(
            candidate = candidate,
            facts = facts,
            maxWords = maxWords ?: LengthLimits.maxWords(candidate.purpose),
            maskedStatement = mask(candidate.rendered, candidate),
            maskedQuestion = candidate.renderedQuestion?.let { mask(it, candidate) },
        )
        for ((check, inspect) in CHECKS) {
            val detail = inspect(inspection)
            if (detail != null) return ValidationResult.Vetoed(candidate, check, detail)
        }
        return ValidationResult.Passed(Validated(candidate))
    }

    /** Everything one check needs, computed once. */
    private data class Inspection(
        val candidate: Candidate,
        val facts: FactSet,
        val maxWords: Int,
        val maskedStatement: String,
        val maskedQuestion: String?,
    ) {
        /** The rendered text as a person sees it, statement and question together. */
        val rendered: List<String> = listOfNotNull(candidate.rendered, candidate.renderedQuestion)

        /** The same two strings with the person's own words replaced. */
        val masked: List<String> = listOfNotNull(maskedStatement, maskedQuestion)

        /** Slots in a stable order, so a candidate breaking two checks reports the same one twice. */
        val slots: List<Slot> = candidate.slots.values.sortedBy { it.key }
    }

    /**
     * The eleven checks of section 8, in the order that section states.
     *
     * A check returns null when it passes and the veto detail when it does not. The detail
     * is written to be read months later beside a rule key, so it names the fact, the id or
     * the word rather than saying that something was wrong.
     */
    private val CHECKS: List<Pair<ValidationCheck, (Inspection) -> String?>> = listOf(
        ValidationCheck.AREA_EXISTENCE to ::areaExistence,
        ValidationCheck.ITEM_EXISTENCE to ::itemExistence,
        ValidationCheck.NUMBER_PROVENANCE to ::numberProvenance,
        ValidationCheck.NO_ZEROS to ::noZeros,
        ValidationCheck.SNAPSHOT_USAGE to ::snapshotUsage,
        ValidationCheck.CALLBACK_FIDELITY to ::callbackFidelity,
        ValidationCheck.UNFILLED_MARKERS to ::unfilledMarkers,
        ValidationCheck.FORBIDDEN_VOCABULARY to ::forbiddenVocabulary,
        ValidationCheck.LENGTH to ::length,
        ValidationCheck.REGISTER_INTEGRITY to ::registerIntegrity,
        ValidationCheck.ESTIMATE_DELTA to ::estimateDelta,
    )

    /** Exposed so a test can assert the list is complete and in the order section 8 gives. */
    internal val checkOrder: List<ValidationCheck> get() = CHECKS.map { it.first }

    /**
     * Check 1. Every named area has real events in the window being described, unless the
     * absence is what the sentence is about.
     *
     * **Not merely exists, not merely unarchived.** An area with no events in the window is
     * a phantom: the sentence is about a week, and an area that did nothing that week cannot
     * be part of what the week was. An archived or tombstoned area fails this same check by
     * a shorter route, because 3.1 keeps it out of `FactSet.areas` entirely.
     *
     * **The one exception is [AbsenceSubject], and it is a narrowing rather than a
     * widening.** Three families say that an area has been still, and against the check as
     * it was first written every candidate they produced was vetoed. A rule flagged
     * `absenceSubject` may name an area with no events in the window when that area has a
     * real history behind the silence; everything else is refused exactly as before, and a
     * new empty area is refused by any rule at all. Read that file for what was wrong and
     * why the fix went this way rather than the other.
     */
    private fun areaExistence(inspection: Inspection): String? {
        for (areaId in inspection.candidate.namedAreaIds.sorted()) {
            val area = inspection.facts.areas[areaId]
                ?: return "names area $areaId, which is not in this window's facts. Archived and " +
                    "tombstoned areas are absent from AreaFacts by construction, per 3.1"
            if (area.eventsInWindow > 0) continue
            val refusal = AbsenceSubject.refusalFor(inspection.candidate.ruleKey, area) ?: continue
            return "names ${area.nameSnapshot} ($areaId), which had ${area.eventsInWindow} events " +
                "in this window. $refusal"
        }
        return null
    }

    /**
     * Check 2. Every named item resolves in the fact set and is therefore not tombstoned.
     *
     * Resolution and the tombstone test are one question rather than two. `ItemFacts`
     * excludes a tombstoned item from `completedInWindow` and from `longestEverActiveItemId`,
     * and an item that is active cannot be tombstoned, so an id that resolves anywhere in
     * the fact set is live by construction. An id that resolves nowhere is either deleted or
     * invented and neither may be named.
     */
    private fun itemExistence(inspection: Inspection): String? {
        val live = liveItemIds(inspection.facts)
        val unknown = inspection.candidate.namedItemIds.sorted().firstOrNull { it !in live }
        return if (unknown == null) {
            null
        } else {
            "names item $unknown, which nothing in this window's facts resolves. A deleted item " +
                "leaves the fact set entirely, so an unresolvable id is a tombstone or an invention"
        }
    }

    /**
     * Check 3. Every rendered number is re-read from the fact it claims and compared.
     *
     * **This is the check `FactRef` exists for.** A `Count`, a `Percent` and a `Days` slot
     * must each carry a reference, that reference must address a measure the app declares,
     * and re-reading it must still produce what the sentence says. A number whose reference
     * resolves to nothing is untraceable, and section 8 is explicit that an untraceable
     * number is a veto rather than a warning.
     *
     * **A slot at zero or below is left to check 4.** No measure in the table can return one:
     * `Measures` answers null for zero and for the never sentinel, so a zero slot would fail
     * here with `reads nothing`, which is a true statement about the fact and a misleading
     * description of what is wrong with the sentence. Nothing escapes by being skipped here,
     * because check 4 vetoes every non positive number unconditionally and runs next.
     *
     * A `DateRef` is verified when it carries a reference and is not required to. Section 8
     * requires provenance for the three numeric slots only, and a week key is not a number.
     */
    private fun numberProvenance(inspection: Inspection): String? {
        for (slot in inspection.slots) {
            val ref = inspection.candidate.sourceFacts[slot.key]
            val value = slot.numericValue
            if (value != null) {
                if (value <= 0) continue
                if (ref == null) {
                    return "renders {${slot.key}} as $value with no FactRef. Every number carries the " +
                        "fact it came from, per MASTER_BUILD_PROMPT 11.4"
                }
                val detail = when (val read = FactLookup.read(inspection.facts, ref, zone)) {
                    null -> "renders {${slot.key}} as $value from $ref, which ${unreadable(ref)}"
                    is MeasureValue.Number ->
                        if (read.value == value) null
                        else "renders {${slot.key}} as $value and $ref reads ${read.value}"
                    is MeasureValue.Text -> "renders {${slot.key}} as a number and $ref reads text"
                    is MeasureValue.Date -> "renders {${slot.key}} as a number and $ref reads a week"
                }
                if (detail != null) return detail
            } else if (slot is Slot.DateRef && ref != null) {
                val read = FactLookup.read(inspection.facts, ref, zone)
                if (read !is MeasureValue.Date) {
                    return "renders {${slot.key}} as the week ${slot.weekKey} from $ref, which ${unreadable(ref)}"
                }
                if (read.weekKey != slot.weekKey) {
                    return "renders {${slot.key}} as the week ${slot.weekKey} and $ref reads ${read.weekKey}"
                }
            }
        }
        return null
    }

    /** Why a reference produced nothing, in the two different ways that can happen. */
    private fun unreadable(ref: FactRef): String {
        val measure = FactLookup.measureOf(ref)
        return if (measure == null) {
            "no measure declares. A reference nothing can re-read is an untraceable number"
        } else {
            "reads nothing now: ${measure.describe}"
        }
    }

    /**
     * Check 4. No numeric slot is zero.
     *
     * `You completed 0 things` is the failure this prevents, and the guard is doubled on
     * purpose: 7.2 requires that a rule which could produce zero carry a criterion making
     * zero unreachable, `Measures` refuses to produce one, and this is what catches a number
     * that arrived by some third route.
     *
     * **A negative is vetoed here too**, which section 8 does not say in as many words. It is
     * the same failure with a sign on it: a minus rendered into a sentence is a number nobody
     * authored, and a family that wants to speak about a gap or a decline passes the
     * magnitude, which is what `intakeVsOutput` and every corpus line reading `{k} more
     * things` was written against.
     */
    private fun noZeros(inspection: Inspection): String? {
        for (slot in inspection.slots) {
            val value = slot.numericValue ?: continue
            if (value == 0) return "renders {${slot.key}} as zero, and zero never reaches a template"
            if (value < 0) {
                return "renders {${slot.key}} as $value. A sentence carries the magnitude, and the sign " +
                    "belongs to the rule that chose the family"
            }
        }
        return null
    }

    /**
     * Check 5. Names are snapshots.
     *
     * Section 8 enforces this structurally: the realizer receives only the `FactSet`, whose
     * name fields were resolved by folding the log, and has no access to live entity tables
     * at all. There is nothing here a correct realizer can fail, and this check does not
     * pretend otherwise. What it can still do is compare, in the two directions a name can
     * go wrong.
     *
     * **Every named entity's snapshot is in the sentence.** The realizer records an id at the
     * moment it reads that entity's name, so an id in `namedAreaIds` whose `nameSnapshot`
     * does not appear in the text means the sentence named one thing and recorded another,
     * and checks 1 and 2 were then answered about the wrong entity.
     *
     * **Every name slot holds a snapshot the fact set carries.** A name that arrived from
     * anywhere else, a live table reached through a repository, a string built at the call
     * site, a title held over from a previous render, matches nothing and is vetoed. That is
     * the stale name failure in section 13 caught at the last possible moment rather than
     * assumed away.
     */
    private fun snapshotUsage(inspection: Inspection): String? {
        val facts = inspection.facts
        for (areaId in inspection.candidate.namedAreaIds.sorted()) {
            val snapshot = facts.areas[areaId]?.nameSnapshot ?: continue
            if (inspection.rendered.none { it.contains(snapshot, ignoreCase = true) }) {
                return "records area $areaId as named and the sentence does not contain its snapshot " +
                    "`$snapshot`, so checks 1 and 2 were answered about an entity this sentence does not name"
            }
        }
        for (itemId in inspection.candidate.namedItemIds.sorted()) {
            val snapshot = titleSnapshotOf(facts, itemId) ?: continue
            if (inspection.rendered.none { it.contains(snapshot, ignoreCase = true) }) {
                return "records item $itemId as named and the sentence does not contain its snapshot `$snapshot`"
            }
        }
        val areaNames = areaSnapshots(facts)
        val itemTitles = itemSnapshots(facts)
        for (slot in inspection.slots.filterIsInstance<Slot.Text>()) {
            val known = when (slot.key) {
                in AREA_NAME_SLOTS -> areaNames
                in ITEM_TITLE_SLOTS -> itemTitles
                else -> continue
            }
            if (slot.value !in known) {
                return "renders {${slot.key}} as `${slot.value}`, which is not a snapshot this window " +
                    "carries. Names come from the fact set and from nowhere else"
            }
        }
        return null
    }

    /**
     * Check 6. A quoted answer is quoted exactly.
     *
     * The label compared is the one stored verbatim on the `PULSE_ANSWERED` event, never a
     * label looked up in the current app version, so a response option reworded in a later
     * release leaves every old callback quoting the old wording. Comparison ignores case
     * because the renderer lowercases a label at a sentence position.
     *
     * Both the candidate's own `quotedLabel`, which the realizer records when it fills a
     * slot from a label measure, and any label slot are checked. The recorded one is the
     * reliable half: a quote can be found again in a sentence only by knowing what it was.
     *
     * A quote matching nothing is the fabricated callback in section 13, and it is the one
     * output that ends the app's credibility outright, because the person knows what they
     * said.
     */
    private fun callbackFidelity(inspection: Inspection): String? {
        val stored = inspection.facts.pulse.recentAnswers.map { it.responseLabel }
        val quoted = buildList {
            inspection.candidate.quotedLabel?.let { add(it) }
            inspection.slots.filterIsInstance<Slot.Text>()
                .filter { it.key in RESPONSE_LABEL_SLOTS }
                .forEach { add(it.value) }
        }
        for (label in quoted) {
            if (stored.none { it.equals(label, ignoreCase = true) }) {
                return "quotes `$label` as something the person answered, and no stored responseLabel " +
                    "in this window's facts matches it"
            }
        }
        return null
    }

    /** Check 7. Nothing that looks like slot syntax survives into the rendered text. */
    private fun unfilledMarkers(inspection: Inspection): String? {
        for (text in inspection.masked) {
            val marker = Template.MARKER.find(text)
            if (marker != null) return "renders the unfilled marker ${marker.value}"
            val brace = text.firstOrNull { it == '{' || it == '}' }
            if (brace != null) return "renders a stray `$brace`, which is slot syntax reaching a screen"
        }
        return null
    }

    /**
     * Check 8. No banned word, no dash, nothing above ASCII, no spelling from elsewhere.
     *
     * Read over the masked text, per the note on this class: the app answers for the words
     * the app chose. An exclamation mark is included because 11.3 bans it outright and
     * because it is the clearest tell that a sentence was written to be motivating rather
     * than true.
     */
    private fun forbiddenVocabulary(inspection: Inspection): String? {
        for (text in inspection.masked) {
            if (text.contains(ValidatorVocabulary.EM_DASH)) return "contains an em dash"
            if (text.contains(ValidatorVocabulary.EN_DASH)) return "contains an en dash"
            if (text.contains('!')) return "contains an exclamation mark"
            val above = text.firstOrNull { it.code > LAST_ASCII }
            if (above != null) {
                return "contains U+%04X, and every authored line in all three corpora is ASCII".format(above.code)
            }
            for ((pattern, name) in ValidatorVocabulary.BANNED_WORDS + ValidatorVocabulary.BANNED_PHRASES) {
                if (pattern.containsMatchIn(text)) return "contains the banned word or phrase `$name`"
            }
            for ((pattern, name) in ValidatorVocabulary.BLAME_CONSTRUCTIONS) {
                if (pattern.containsMatchIn(text)) return "contains $name"
            }
            for ((pattern, preferred) in ValidatorVocabulary.OTHER_SPELLING_FORMS) {
                val hit = pattern.find(text)
                if (hit != null) return "spells `${hit.value}`, and this app writes `$preferred`"
            }
        }
        return null
    }

    /**
     * Check 9. Length, measured on the sentence as it will appear.
     *
     * Unmasked, unlike the three checks above it, because a person with a long area name
     * really does get a long headline and the limit is about what fits and what reads as a
     * headline. A vetoed candidate here falls through to the next ranked selection, which is
     * very often a shorter line from the same bench.
     */
    private fun length(inspection: Inspection): String? {
        for (text in inspection.rendered) {
            val words = LengthBands.wordCount(text)
            if (words > inspection.maxWords) {
                return "runs to $words words against a limit of ${inspection.maxWords}: `$text`"
            }
        }
        return null
    }

    /**
     * Check 10. A neutral agent line is not the passive voice wearing a disguise.
     *
     * 7.4 is unusually direct about why this matters: a line that reads as evasion is worse
     * than the agentive original it replaced. The register exists to stop an unflattering
     * number being framed as something the person did; turning it into something that was
     * done, by nobody, reads as the app declining to say who, which is a worse sentence than
     * either honest option.
     */
    private fun registerIntegrity(inspection: Inspection): String? {
        if (inspection.candidate.register != Register.NEUTRAL_AGENT) return null
        for (text in inspection.masked) {
            for ((pattern, name) in ValidatorVocabulary.AGENT_DELETED_PASSIVES) {
                val hit = pattern.find(text)
                if (hit != null) {
                    return "is a NEUTRAL_AGENT line containing $name, `${hit.value}`. The register makes " +
                        "the fact the subject; it is not the passive voice"
                }
            }
        }
        return null
    }

    /**
     * Check 11. No sentence states a delta between an estimate and an actual.
     * `MASTER_BUILD_PROMPT.md` 14b.8, Addendum 01 7a.
     *
     * **Only ratios and tendencies.** 14b.8 permits `Things you estimate at an hour tend to
     * take about three` and forbids both `You underestimated by two hours` and `You were
     * off by 140 percent`. The difference is not politeness. A ratio describes how this
     * person's estimates map onto their days; a delta is a score against a target they set
     * themselves, and time blindness is the reason the estimate was wrong in the first
     * place, so the delta measures the symptom and reports it as a mistake.
     *
     * **It is a backstop and it knows it.** The prohibition is kept above this layer by
     * arithmetic: `TrailQueries.estimateOutcomes` divides the two magnitudes inside its own
     * body, no quantity of minutes exists anywhere in the fact set, and no measure produces
     * one, so `actual - estimate` is not a subtraction any rule or template can write.
     * 14b.8 asks for this check anyway, for a number arriving some other way, and a
     * backstop with nothing to catch is what a backstop should look like.
     *
     * Two rules, and the second is the one a reader will not expect.
     *
     * **The language rule** vetoes any of [ValidatorVocabulary.ESTIMATE_DELTA_FORMS]
     * anywhere in the sentence, whether or not the word estimate appears, because the
     * percentage example never says it.
     *
     * **The shape rule** vetoes a `Percent` slot in a sentence that is about an estimate,
     * where about an estimate means the sentence says so or one of its numbers came from an
     * estimate measure. 14b.8 makes the ratio a multiple and never a percentage for a
     * stated reason: 2.4 rendered as 240 percent is one literal hundred away from the
     * second forbidden line. Nothing in `Measures` can produce that percentage today, which
     * is exactly why the rule is written against the slot rather than against the table.
     *
     * Read over the masked text, per the note on this class: an area somebody named
     * `Estimates` is their word and not the app's.
     */
    private fun estimateDelta(inspection: Inspection): String? {
        for (text in inspection.masked) {
            for ((pattern, name) in ValidatorVocabulary.ESTIMATE_DELTA_FORMS) {
                val hit = pattern.find(text)
                if (hit != null) {
                    return "states a delta between an estimate and an actual: `${hit.value}` is $name. " +
                        "14b.8 permits a ratio and a tendency and forbids a difference"
                }
            }
        }
        if (!isAboutAnEstimate(inspection)) return null
        val percent = inspection.slots.firstOrNull { it is Slot.Percent } ?: return null
        return "renders {${percent.key}} as a percentage in a sentence about an estimate. 14b.8 " +
            "makes the reading a multiple and never a percentage, because a ratio of 2.4 shown " +
            "as 240 percent is one literal hundred from `You were off by 140 percent`"
    }

    /** Whether the sentence says estimate, or one of its numbers came from an estimate measure. */
    private fun isAboutAnEstimate(inspection: Inspection): Boolean =
        inspection.masked.any { ValidatorVocabulary.ESTIMATE_MENTION.containsMatchIn(it) } ||
            inspection.candidate.sourceFacts.values.any { ref ->
                FactLookup.measureOf(ref)?.id?.startsWith(Measures.ESTIMATE_MEASURE_PREFIX) == true
            }

    /**
     * The rendered text with the person's own strings replaced by a single token.
     *
     * Only [Slot.Text] values are masked, which is exactly the set of strings the person
     * wrote or chose: area names, item titles and the response label they tapped. Every
     * other word in the sentence came from a corpus file.
     *
     * A blank value is skipped rather than replaced, because replacing an empty string
     * inserts the token between every character of the sentence. Longest first, so a short
     * name that is a substring of a long one does not mask half of it.
     */
    private fun mask(text: String, candidate: Candidate): String =
        candidate.slots.values
            .filterIsInstance<Slot.Text>()
            .map { it.value }
            .filter { it.isNotBlank() }
            .sortedByDescending { it.length }
            .fold(text) { masked, value -> masked.replace(value, MASK_TOKEN, ignoreCase = true) }

    private fun liveItemIds(facts: FactSet): Set<String> = buildSet {
        facts.items.activeByArea.values.forEach { add(it.itemId) }
        facts.items.completedInWindow.forEach { add(it.itemId) }
        facts.areas.values.mapNotNull { it.activeItemId }.forEach { add(it) }
        facts.items.longestActiveItemId?.let { add(it) }
        facts.history.longestEverActiveItemId?.let { add(it) }
    }

    /** Every area name this window carries, from all three places a snapshot is held. */
    private fun areaSnapshots(facts: FactSet): Set<String> = buildSet {
        facts.areas.values.forEach { add(it.nameSnapshot) }
        facts.items.activeByArea.values.forEach { add(it.areaNameSnapshot) }
        facts.items.completedInWindow.forEach { add(it.areaNameSnapshot) }
    }

    /** Every item title this window carries. */
    private fun itemSnapshots(facts: FactSet): Set<String> = buildSet {
        facts.items.activeByArea.values.forEach { add(it.titleSnapshot) }
        facts.areas.values.mapNotNull { it.activeItemTitleSnapshot }.forEach { add(it) }
        facts.items.completedInWindow.forEach { add(it.titleSnapshot) }
    }

    /**
     * The title an item is known by in this window, or null.
     *
     * Null is ordinary rather than a failure: the item holding `longestEverActiveDays` is
     * often not active now and not completed this week, so the fact set knows its id and not
     * its name. Check 5 skips what it cannot compare and check 2 has already established
     * that the id resolves.
     */
    private fun titleSnapshotOf(facts: FactSet, itemId: String): String? =
        facts.items.activeByArea.values.firstOrNull { it.itemId == itemId }?.titleSnapshot
            ?: facts.areas.values.firstOrNull { it.activeItemId == itemId }?.activeItemTitleSnapshot
            ?: facts.items.completedInWindow.firstOrNull { it.itemId == itemId }?.titleSnapshot

    companion object {

        /**
         * The slots that carry an area name.
         *
         * `SlotProduction` in `domain.engine.catalog` is the same table from the other end:
         * what produces each slot. The three sets here are the slots it declares with no
         * `FactRef`, which is exactly the set that holds a string rather than a number, and
         * `NameSlotCoverageTest` asserts the two agree. Without that test a name slot added
         * to the corpus would be skipped by checks 5 and 6 in silence, which is the failure
         * mode this whole layer exists to prevent.
         */
        internal val AREA_NAME_SLOTS: Set<SlotKey> = setOf("areaName", "otherArea", "thirdArea")

        /** The slots that carry an item title. */
        internal val ITEM_TITLE_SLOTS: Set<SlotKey> = setOf("itemTitle")

        /** The slots that quote something the person tapped. */
        internal val RESPONSE_LABEL_SLOTS: Set<SlotKey> = setOf("priorLabel")

        /** Stands in for a person's own words. A letter, so it never reads as slot syntax. */
        private const val MASK_TOKEN = "X"

        private const val LAST_ASCII = 127
    }
}

/**
 * The eleven checks of CLARITY_LOGIC_ENGINE.md 8, numbered as that section numbers them.
 *
 * The numbers are part of the specification and are carried here so a veto in a simulator
 * dump or a debug log can be read against the document without anybody having to count.
 */
enum class ValidationCheck(val number: Int, val what: String) {
    AREA_EXISTENCE(1, "area existence"),
    ITEM_EXISTENCE(2, "item existence"),
    NUMBER_PROVENANCE(3, "number provenance"),
    NO_ZEROS(4, "no zeros"),
    SNAPSHOT_USAGE(5, "snapshot usage"),
    CALLBACK_FIDELITY(6, "callback fidelity"),
    UNFILLED_MARKERS(7, "unfilled markers"),
    FORBIDDEN_VOCABULARY(8, "forbidden vocabulary"),
    LENGTH(9, "length"),
    REGISTER_INTEGRITY(10, "register integrity"),

    /**
     * Not in section 8's original list of ten. `MASTER_BUILD_PROMPT.md` 14b.8 requires a
     * veto of its own and section 17 lists a test that constructs the forbidden form, so
     * it is a check rather than an extra clause on check 8: a veto detail naming the
     * estimate rule is what a reader months later needs, and check 8 would report it as a
     * banned word.
     *
     * **Appended rather than inserted.** The numbers are cited from three documents and
     * from the tests, and renumbering ten checks to put an eleventh in the middle would
     * break those citations silently. It runs last for the same reason the order is data:
     * a candidate breaking several checks is reported against the most fundamental thing
     * wrong with it, and a fabricated area name is more fundamental than a sentence shape.
     */
    ESTIMATE_DELTA(11, "estimate delta"),
    ;

    override fun toString(): String = "check $number, $what"
}

/** What layer 5 answers for one candidate. CLARITY_LOGIC_ENGINE.md 2, `Validated | Vetoed`. */
sealed interface ValidationResult {

    /** Cleared every check. The only way a [Validated] is ever constructed. */
    data class Passed(val validated: Validated) : ValidationResult

    /**
     * Rejected, with the check that rejected it and a detail naming what was wrong.
     *
     * Recorded rather than thrown. A veto is an ordinary event: the engine had something to
     * say and could not prove it, and the next ranked selection gets its turn. The detail
     * reaches the simulator dump and the debug log, and never a screen.
     */
    data class Vetoed(val candidate: Candidate, val check: ValidationCheck, val detail: String) : ValidationResult {
        override fun toString(): String = "${candidate.variantKey} vetoed by $check: $detail"
    }
}

/**
 * The word limits in CLARITY_LOGIC_ENGINE.md 8, check 9.
 *
 * Section 8 states four: a Report headline under 8 words, a Momentum headline under 12, a
 * Pulse observation under 30 and a closing line under 22. Three surfaces are not named
 * there, so they take the ceiling 7.5 already puts on every authored line, which is the top
 * of the `LONG` band. That is not a limit invented here: a line above it has no band and
 * `Template` refuses to construct it.
 */
object LengthLimits {

    /** Under 8 words. */
    const val REPORT_HEADLINE_MAX_WORDS = 7

    /** Under 12 words. */
    const val MOMENTUM_HEADLINE_MAX_WORDS = 11

    /** Under 30 words. */
    const val PULSE_MAX_WORDS = 29

    /**
     * Under 22 words, for layer 6.
     *
     * A closing is not a [Purpose], so it is not in [maxWords] and reaches the validator as
     * an explicit argument. Declared now, in the phase that builds the checks, rather than
     * in the phase that builds guidance, because a limit invented alongside the thing it
     * limits is a limit that fits whatever was written.
     */
    const val CLOSING_MAX_WORDS = 21

    /** The limit for [purpose]. */
    fun maxWords(purpose: Purpose): Int = when (purpose) {
        Purpose.REPORT_HEADLINE -> REPORT_HEADLINE_MAX_WORDS
        Purpose.MOMENTUM_HEADLINE -> MOMENTUM_HEADLINE_MAX_WORDS
        Purpose.PULSE -> PULSE_MAX_WORDS
        Purpose.REPORT_OBSERVATION, Purpose.REPORT_PATTERN, Purpose.AREAS_BANNER -> LengthBands.LONG_MAX
    }
}
