package com.kamsiob.claritynow.domain.guidance

import com.kamsiob.claritynow.domain.engine.FactRef
import com.kamsiob.claritynow.domain.engine.FactSet
import com.kamsiob.claritynow.domain.engine.FamilyKey
import com.kamsiob.claritynow.domain.engine.FiringHistory
import com.kamsiob.claritynow.domain.engine.PartOfDay
import com.kamsiob.claritynow.domain.engine.RenderedOutput
import com.kamsiob.claritynow.domain.engine.Validated
import com.kamsiob.claritynow.domain.engine.Weekday
import com.kamsiob.claritynow.domain.engine.catalog.ClarityCatalog
import com.kamsiob.claritynow.domain.engine.catalog.CorpusLine
import com.kamsiob.claritynow.domain.engine.catalog.LengthBand
import com.kamsiob.claritynow.domain.engine.catalog.LengthBands
import com.kamsiob.claritynow.domain.engine.catalog.Purpose
import com.kamsiob.claritynow.domain.engine.catalog.Register
import com.kamsiob.claritynow.domain.engine.catalog.SlotKey
import com.kamsiob.claritynow.domain.engine.catalog.Template
import com.kamsiob.claritynow.domain.engine.realize.Candidate
import com.kamsiob.claritynow.domain.engine.realize.MeasureValue
import com.kamsiob.claritynow.domain.engine.realize.Measures
import com.kamsiob.claritynow.domain.engine.realize.Slot
import com.kamsiob.claritynow.domain.engine.realize.SlotRenderer
import com.kamsiob.claritynow.domain.engine.realize.VariantChoice
import com.kamsiob.claritynow.domain.engine.validate.ClarityValidator
import com.kamsiob.claritynow.domain.engine.validate.LengthLimits
import com.kamsiob.claritynow.domain.engine.validate.ValidationResult
import java.time.DayOfWeek
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale

/**
 * Layer 6. CLARITY_LOGIC_ENGINE.md 10.
 *
 * The engine otherwise only observes. This is the one place in the whole application that
 * offers a person anything, and it is therefore the one that has to be hardest to misuse.
 * `MASTER_BUILD_PROMPT.md` 19 registers a formal reservation against it and names it the
 * first thing removed if it reads as supervision when tested.
 *
 * ## What it can do, stated as a limit rather than as a capability
 *
 * It can assemble one sentence from three authored benches, offer it with two equally
 * weighted answers, and re-render it in the first person if the answer is yes. It can
 * choose one complete authored line instead. It can say nothing.
 *
 * **It cannot write about a plan.** Not the plan it is offering, not one offered before,
 * not one accepted, not one ignored. The follow through it applies to a later week is a
 * rank, and `FollowThrough` and `PlanHistory` are shaped so that a rank is the only thing
 * that can come out of it. `GuidanceNonComplianceTest` holds that shut from four
 * directions and was written before this file.
 *
 * ## The sequence
 *
 * ```
 * 10.4 rule 6  the report is otherwise heavy       -> Nothing
 * 10.4 rule 1  no stable rhythm, 3.7               -> a noRhythmYet closing
 * 10.4 rule 3  no friction family appeared         -> a trustThePace or letItBe closing
 * 10.4 rule 4  an accepted plan is still open      -> an ordinary closing
 * 10.4 rule 2  the motivating observation appeared -> structural, see [compose]
 * 10.4 rule 5  a single act, inside a week         -> structural, see [action]
 *              nothing renders or validates        -> an ordinary closing
 *              otherwise                           -> the plan
 * ```
 *
 * ## Two deviations from 10.3's declared signature, both recorded
 *
 * 10.3 declares `object GuidanceComposer { fun compose(appeared, facts, plans) }`.
 *
 * **It is a class taking a catalog and a zone**, because every sentence it produces comes
 * out of `CORPUS_2_REPORT.md` 4 and an object would have to reach a corpus through a
 * global. Issue #55 is already open about corpus catalogs being built more than once per
 * process, and adding a fourth construction site to settle a signature would make that
 * worse. `ReportLanguage` and `PulseLanguage` are classes over the same two arguments for
 * the same reason.
 *
 * **[compose] takes three arguments 10.3 does not name**, and each one is a thing the
 * section's own rules need and the declaration predates. [headline] is 10.4 rule 6, which
 * is written in terms of a declining headline and cannot be evaluated from observations
 * alone. [weekStartKey] is what the plan is filed under and what rule 4 measures two weeks
 * back from; deriving it here from the window would let it disagree with the key
 * `ReportComposer` files the report under, and `ReportComposer.compose` already takes it
 * for that reason. [history] is 7.6's ninety day variant exclusion, without which the same
 * frame is offered every week of the year.
 */
class GuidanceComposer(catalog: ClarityCatalog, private val zone: ZoneId) {

    private val benches = PlanBenches.of(catalog)

    private val validator = ClarityValidator(zone)

    /**
     * One closing line for the week [facts] describes, or nothing.
     *
     * **[appeared] is the whole of 10.4 rule 2, and it is enforced by the type rather than
     * by a check.** `Validated` is constructed only by layer 5, `ReportComposer` hands in
     * exactly the observations the page will show, and this function reads no other source
     * of observations. There is deliberately no fact set path to a family that did not
     * appear: an engine that could reach an observation the reader never saw is an engine
     * that can refer to something that did not happen.
     */
    fun compose(
        headline: Validated?,
        appeared: List<Validated>,
        facts: FactSet,
        plans: PlanHistory,
        history: FiringHistory,
        weekStartKey: String,
    ): GuidanceResult {
        val families = appeared.map { it.candidate.familyKey }.toSet()

        // 10.4 rule 6. A heavy report gets no closing at all, and not a gentle one. A week
        // that was genuinely hard does not need a line underneath it saying so, and the
        // three non plan benches all say something about the week: two of them say it went
        // well and the third says the app is still learning. On this week each of those
        // would be false in a way the reader can feel.
        if (heavy(headline, families)) return GuidanceResult.Nothing

        val plan = plan(appeared, facts, plans, history, weekStartKey)
        if (plan != null) return GuidanceResult.Plan(plan)
        return closing(facts, families, history, weekStartKey)
    }

    // ------------------------------------------------------------------ the six rules

    /**
     * 10.4 rule 6. `A declining headline plus a neglected area, or any hardStretch.`
     *
     * Quoted rather than interpreted. Both halves are read off what appeared, so a report
     * that was heavy and did not say so is not treated as heavy: the rule is about the page
     * the person is reading, not about the facts behind it.
     */
    private fun heavy(headline: Validated?, families: Set<FamilyKey>): Boolean {
        if (HARD_STRETCH in families) return true
        val declining = headline?.candidate?.familyKey == DECLINING_ACTIVITY
        return declining && NEGLECTED_AREA in families
    }

    /** The plan, or null when any of the six rules refuses one. */
    private fun plan(
        appeared: List<Validated>,
        facts: FactSet,
        plans: PlanHistory,
        history: FiringHistory,
        weekStartKey: String,
    ): ClarityPlan? {
        // 10.4 rule 1, and 3.7. Not one cue in the whole set cleared its three thresholds,
        // so every cue this could anchor to would be invented, and "an invented cue is
        // worse than no plan, because it makes a claim about the user's life the user knows
        // to be false".
        if (!facts.cues.hasStableRhythm) return null

        // 10.4 rule 3. A plan needs a real friction pattern, and the action bank is the
        // corpus's own list of the frictions it judged a plan could help with. A week whose
        // observations are all outside it had no barrier worth acting on, which is 10.4's
        // "when barriers are low, plan formation is superfluous" without anything here
        // having to decide that a week was a good one.
        val motivating = appeared.firstOrNull { benches.motivates(it.candidate.familyKey) } ?: return null

        // 10.4 rule 4. Stacking unfinished plans is how this becomes a nag. See
        // PlanHistory.stillUnresolved for what unresolved means when nothing records it.
        val subjects = appeared.map { it.candidate.familyKey to it.candidate.subjectId }.toSet()
        if (plans.stillUnresolved(weekStartKey, subjects)) return null

        return assemble(motivating.candidate, facts, history, weekStartKey)
    }

    // ------------------------------------------------------------------ assembly

    /**
     * Frame plus cue plus action, rendered, validated, and re-rendered in the first person.
     *
     * The search is bounded and deterministic: the actions the motivating family licenses,
     * then the cues that can be substantiated and read inside a commitment form, then that
     * cue's frames. Each bench is ordered by [VariantChoice], which is 7.6's rule and the
     * same function the realizer uses, so two devices holding one log assemble the same
     * plan and a person never meets the same frame twice inside ninety days.
     *
     * **A plan that cannot be rendered or cannot be validated is not a plan.** There is no
     * fallback that drops a slot or shortens a line. That is the same refusal
     * `SlotRenderer` makes for every other sentence in the app, and here it is the
     * difference between an offer and a claim about somebody's week that is not true.
     */
    private fun assemble(
        motivating: Candidate,
        facts: FactSet,
        history: FiringHistory,
        weekStartKey: String,
    ): ClarityPlan? {
        val subject = motivating.subjectId
        val subjectIsItem = subject != null && subject in motivating.namedItemIds
        val actions = ordered(benches.actionsFor(motivating.familyKey), weekStartKey, history)
        val cues = ordered(substantiatedCues(facts), weekStartKey, history)

        for (action in actions) {
            val renderedAction = fill(action, facts, subject, subjectIsItem) ?: continue
            for (cue in cues) {
                val renderedCue = fill(cue, facts, subject, subjectIsItem) ?: continue
                val committed = commitment(cue, renderedCue, action, renderedAction, facts, motivating)
                    ?: continue
                for (frame in ordered(benches.framesFor(cue), weekStartKey, history)) {
                    val offered = render(frame, renderedCue, renderedAction)
                        ?.takeIf { passes(it, frame.key, motivating, facts) }
                        ?: continue
                    return ClarityPlan(
                        id = "$PLAN_ID_PREFIX$weekStartKey",
                        weekStartKey = weekStartKey,
                        frameKey = frame.key,
                        cueKey = cue.key,
                        actionKey = action.key,
                        familyKey = motivating.familyKey,
                        subjectId = subject,
                        offeredLine = offered,
                        committedLine = committed,
                        resolutionFactRef = resolutionFactOf(motivating, subject),
                    )
                }
            }
        }
        return null
    }

    /**
     * 4.4. The plan re-rendered in the first person, or null when no form can carry it.
     *
     * ## `com.01` is the if then form and it renders
     *
     * 10.2 makes the stored line a proper if then, and 4.4's first form is it: `If it's
     * {cue}, my one thing is {actionNoun}.` It is built out of the noun phrase every action
     * in 4.3 already is, and that is the whole of why it works.
     *
     * The form this section carried before was `If it's {cue}, I'll {actionVerb}.`, 4.4 said
     * in prose that every action carries a verb form alongside its gerund, and the bank
     * carried none, so the form never rendered once. **The corpus closed it from the corpus
     * side rather than the code side, and the reasoning is in 4.4.** A verb form is *spend
     * ten minutes in Personal*, *close the oldest item in Work*, *decide whether Reading
     * stays or goes*: in isolation each one is an imperative, and fifty four of them in the
     * file would be a complete imperative action bank one frame away from a screen, in an
     * app whose 10.2 says the imperative form exists nowhere. `PlanFormTest` would have had
     * to be weakened to admit them. The noun phrase gives the same if then and can be read
     * as a command by nobody.
     *
     * ## Which form a cue can be stored in
     *
     * Every form is filtered by the cue's shape before its slots are looked at, because the
     * three forms want three different kinds of phrase and no cue reads in all of them.
     * `com.01` wants something a day can be, which is `DATED` or `NOMINAL`; `com.02` and
     * `com.03` want an adjunct. `PlanBenches.CueShape.commitments` is where that is
     * declared, one entry per shape, with the reading it came from written beside it.
     *
     * Inside what the shape allows, `com.02` takes the action as a gerund phrase and roughly
     * half the bank is one; `com.03` takes it as a noun phrase and the whole bank is one.
     * That test is the action's own first word, which is morphology rather than judgment,
     * and a form whose slot cannot be filled is skipped rather than forced.
     */
    private fun commitment(
        cue: CorpusLine,
        renderedCue: String,
        action: CorpusLine,
        renderedAction: String,
        facts: FactSet,
        motivating: Candidate,
    ): String? {
        val shape = PlanBenches.CUE_SHAPES[cue.key] ?: return null
        if (!shape.commitmentReady) return null
        for (form in benches.commitments.sortedBy { it.key }) {
            if (form.key !in shape.commitments) continue
            val template = templateOf(form) ?: continue
            val slots = mutableMapOf<SlotKey, Slot>()
            var fillable = true
            for (key in template.slots) {
                val value = when (key) {
                    CUE_SLOT -> renderedCue
                    ACTION_GERUND_SLOT -> renderedAction.takeIf { isGerundPhrase(it) }
                    ACTION_NOUN_SLOT -> renderedAction
                    else -> null
                }
                if (value == null) {
                    fillable = false
                    break
                }
                slots[key] = Slot.Text(key, value)
            }
            if (!fillable) continue
            val rendered = SlotRenderer.render(template.text, slots, PURPOSE)?.let(::opening) ?: continue
            if (passes(rendered, form.key, motivating, facts)) return rendered
        }
        return null
    }

    // ------------------------------------------------------------------ cues, 3.7

    /**
     * The cues this person's own twelve weeks actually substantiate. 3.7 and 4.2.
     *
     * Three gates, and all three are 3.7's rather than this file's.
     *
     * 1. **A shape.** A cue with no entry in `PlanBenches.CUE_SHAPES` cannot be placed in
     *    a frame at all, so it is not a cue here
     * 2. **A commitment form.** The stored line is mandatory, so a cue that reads in a
     *    frame and not in any of 4.4's forms would produce an offer that could not be
     *    accepted. Dropping it here rather than discovering it after the render keeps the
     *    two halves of a plan from disagreeing about which cues exist
     * 3. **The part of day bank names the morning in every line.** `the morning, when you
     *    finish most things`, `before midday`, `your first hour`, `early on`. Those are
     *    true of a person whose productive band is the morning and false of everyone else,
     *    and 3.7 calls a cue the data cannot substantiate worse than no plan. So the whole
     *    bank waits on `productiveBand`, and `PlanFormTest` asserts every line in it really
     *    does name the morning, so that an evening line added later fails the build rather
     *    than quietly telling somebody they finish most things before midday
     * 4. **The weekday bank waits on `strongestWeekday`**, which is what 4.2's own heading
     *    for it says, `### Weekday, requires strongestWeekday`. Nine of its ten lines carry
     *    the marker and are gated by the fill; `cue.day.05`, *the day you usually get most
     *    done*, carries none and is the whole fact in words. It was unreachable while the
     *    nominal shape had no commitment form, and became reachable the moment 4.4 grew
     *    one, which is how a cue with no marker to fail on came to be offered to a person
     *    with no strongest weekday. **A bank gate is the only thing that can hold a line
     *    like that**, because there is nothing in it for the renderer to refuse
     *
     * Everything else is the ordinary slot mechanism: a cue naming `{strongestWeekday}` on
     * a person with no strongest weekday cannot be filled, and an unfillable line leaves
     * the bench. `CueFacts` nulls every field that did not clear all three thresholds, so
     * the confidence gate is the fill.
     */
    private fun substantiatedCues(facts: FactSet): List<CorpusLine> = benches.cues.filter { cue ->
        val shape = PlanBenches.CUE_SHAPES[cue.key]
        when {
            shape == null -> false
            !shape.commitmentReady -> false
            cue.key.startsWith(BAND_BENCH) -> facts.cues.productiveBand == MORNING_BAND
            cue.key.startsWith(DAY_BENCH) -> facts.cues.strongestWeekday != null
            else -> true
        }
    }

    // ------------------------------------------------------------------ closings, 4.6

    /**
     * `CORPUS_2_REPORT.md` 4.6. One complete authored line, chosen by what the week was.
     *
     * The four benches carry their own conditions in the corpus and this reads them off
     * rather than inventing a fifth. `noRhythmYet` is "when cues have not stabilized",
     * `letItBe` is "for genuinely quiet weeks", `trustThePace` is "when the week worked",
     * and `review` is "the safe general closing", which is what a week that is none of the
     * first three gets.
     *
     * **The reason a plan was not offered never chooses the closing, with one exception.**
     * A week where rule 4 held a plan back gets whatever closing that week would have got
     * anyway, because a closing that changed shape after somebody accepted something would
     * be a way of telling them so. The exception is the rhythm, and it is the corpus's own:
     * `noRhythmYet` exists to be said when cues have not stabilized, which is the same
     * condition rule 1 refuses a plan under.
     */
    private fun closing(
        facts: FactSet,
        families: Set<FamilyKey>,
        history: FiringHistory,
        weekStartKey: String,
    ): GuidanceResult {
        val bench = when {
            !facts.cues.hasStableRhythm -> NO_RHYTHM_BENCH
            QUIET_WEEK in families -> LET_IT_BE_BENCH
            families.none { benches.motivates(it) } -> TRUST_THE_PACE_BENCH
            else -> REVIEW_BENCH
        }
        val lines = benches.closings[bench].orEmpty()
        val chosen = VariantChoice.choose(lines, weekStartKey, history) { it.key }?.value
            ?: return GuidanceResult.Nothing
        val template = templateOf(chosen) ?: return GuidanceResult.Nothing
        // A closing line states nothing about the person, so it has no slots and one that
        // grew any would be a claim this bench was never reviewed for.
        if (template.slots.isNotEmpty()) return GuidanceResult.Nothing
        val rendered = SlotRenderer.render(template.text, emptyMap(), PURPOSE) ?: return GuidanceResult.Nothing
        val band = bandOf(rendered) ?: return GuidanceResult.Nothing
        val candidate = candidateOf(rendered, chosen.key, CLOSING_RULE_KEY, bench, band)
        return when (validator.validate(candidate, facts, LengthLimits.CLOSING_MAX_WORDS)) {
            is ValidationResult.Passed -> GuidanceResult.Closing(
                RenderedOutput(text = rendered, question = null, responses = emptyList(), meta = candidate),
            )
            is ValidationResult.Vetoed -> GuidanceResult.Nothing
        }
    }

    // ------------------------------------------------------------------ rendering

    /**
     * One bench line with every marker filled, or null.
     *
     * Every slot in section 4 is textual: a weekday, a band, an area name, an item title.
     * There is not one number anywhere in the frames, cues or actions, which is why nothing
     * here mints a `FactRef`. Validator checks 3 and 4 are about numbers and pass on an
     * empty map by having nothing to disbelieve, rather than by being skipped.
     *
     * The two name slots go through [Measures] rather than reading `AreaFacts.nameSnapshot`
     * directly, so a plan names an area the same way every other sentence in the app does
     * and check 1 gets the `namedAreaIds` it reads. A name that cannot be read leaves the
     * line unfillable, which is how an archived area becomes unnameable here without this
     * file knowing what archiving is.
     */
    private fun fill(line: CorpusLine, facts: FactSet, subject: String?, subjectIsItem: Boolean): String? {
        val template = templateOf(line) ?: return null
        val slots = mutableMapOf<SlotKey, Slot>()
        for (key in template.slots) {
            val value = when (key) {
                STRONGEST_WEEKDAY_SLOT -> facts.cues.strongestWeekday?.let(::dayName)
                QUIETEST_WEEKDAY_SLOT -> facts.cues.quietestWeekday?.let(::dayName)
                FOCUS_WEEKDAY_SLOT -> facts.cues.focusTypicalWeekday?.let(::dayName)
                AREA_NAME_SLOT -> areaName(facts, subject, subjectIsItem)
                ITEM_TITLE_SLOT -> if (subjectIsItem) textMeasure(ITEM_TITLE_MEASURE, facts, subject) else null
                OTHER_AREA_SLOT -> otherArea(facts, subject, subjectIsItem)
                else -> null
            } ?: return null
            slots[key] = Slot.Text(key, value)
        }
        return SlotRenderer.render(template.text, slots, PURPOSE)
    }

    /**
     * The frame with the cue and the action in it, opened with a capital.
     *
     * 4.0 settles the shape: the frame takes the two as slots, cue lines carry no terminal
     * punctuation and action lines have no leading capital, so the frame supplies both. The
     * capital is 4.7's own: `cue.hab.02` is authored as *after your next focus session* and
     * the worked plan renders it as *After your next focus session might be the moment
     * for...* Three frames and one commitment form put the cue first, and [opening] is what
     * makes those read as sentences. It changes a letter's case and never a word.
     */
    private fun render(frame: CorpusLine, cue: String, action: String): String? {
        val template = templateOf(frame) ?: return null
        val slots = mapOf(
            CUE_SLOT to Slot.Text(CUE_SLOT, cue),
            ACTION_SLOT to Slot.Text(ACTION_SLOT, action),
        )
        if (template.slots != slots.keys) return null
        return SlotRenderer.render(template.text, slots, PURPOSE)?.let(::opening)
    }

    /** True where layer 5 accepts [text] as a closing line. */
    private fun passes(text: String, variantKey: String, motivating: Candidate, facts: FactSet): Boolean {
        val candidate = candidateOf(
            band = bandOf(text) ?: return false,
            rendered = text,
            variantKey = variantKey,
            // The motivating observation's own rule key, so that a plan may name exactly
            // what the observation that produced it named and nothing else. `neglectedArea`
            // is flagged `absenceSubject`, which is the only reason check 1 lets either of
            // them name an area with no events in the window, and a plan that inherits the
            // key inherits the permission with the reasoning attached rather than by an
            // exception written here.
            ruleKey = motivating.ruleKey,
            familyKey = motivating.familyKey,
            named = motivating.namedAreaIds to motivating.namedItemIds,
        )
        return validator.validate(candidate, facts, LengthLimits.CLOSING_MAX_WORDS) is ValidationResult.Passed
    }

    /**
     * The band, or null for a line longer than 7.5 declares one for.
     *
     * A composed plan is three authored lines end to end, so it is the one place in this
     * app where a sentence can be too long without any single authored line being too
     * long. `LengthBands.bandFor` throws on such a line, because for a corpus line that is
     * an authoring defect; here it is an unlucky combination and the answer is to take
     * another one, so it is caught rather than thrown. Check 9 would refuse the sentence a
     * moment later in any case, at twenty one words rather than twenty four.
     */
    private fun bandOf(text: String): LengthBand? = runCatching { LengthBands.bandFor(text) }.getOrNull()

    private fun candidateOf(
        rendered: String,
        variantKey: String,
        ruleKey: String,
        familyKey: String,
        band: LengthBand,
        named: Pair<Set<String>, Set<String>> = emptySet<String>() to emptySet(),
    ) = Candidate(
        ruleKey = ruleKey,
        familyKey = familyKey,
        variantKey = variantKey,
        purpose = PURPOSE,
        stage = FIRST_STAGE,
        register = Register.PLAIN,
        lengthBand = band,
        rendered = rendered,
        renderedQuestion = null,
        slots = emptyMap(),
        sourceFacts = emptyMap(),
        namedAreaIds = named.first,
        namedItemIds = named.second,
    )

    // ------------------------------------------------------------------ small pieces

    /** 7.6's order over a bench, which is the order the realizer takes for every other line. */
    private fun ordered(bench: List<CorpusLine>, dateKey: String, history: FiringHistory): List<CorpusLine> {
        val remaining = bench.toMutableList()
        val out = mutableListOf<CorpusLine>()
        while (remaining.isNotEmpty()) {
            val next = VariantChoice.choose(remaining, dateKey, history) { it.key }?.value ?: break
            remaining.remove(next)
            out += next
        }
        return out
    }

    /**
     * The area a plan names: the subject when it is an area, and the subject's area when
     * the subject is an item.
     */
    private fun areaName(facts: FactSet, subject: String?, subjectIsItem: Boolean): String? =
        textMeasure(if (subjectIsItem) ITEM_AREA_MEASURE else AREA_NAME_MEASURE, facts, subject)

    /**
     * A live area other than the subject, for the `singleFocus` bank's `{otherArea}`.
     *
     * **It must have events in the window**, because check 1 refuses a named area with
     * none and the absence permission the subject inherits is the subject's alone. The one
     * with the fewest is chosen, which is the area the week gave least to and the one those
     * lines are about, with the id breaking a tie so two devices agree.
     */
    private fun otherArea(facts: FactSet, subject: String?, subjectIsItem: Boolean): String? {
        val exclude = if (subjectIsItem) facts.areas.values.firstOrNull { it.activeItemId == subject }?.areaId else subject
        val candidate = facts.areas.values
            .filter { it.areaId != exclude && it.eventsInWindow > 0 }
            .minWithOrNull(compareBy({ it.eventsInWindow }, { it.areaId }))
            ?: return null
        return textMeasure(AREA_NAME_MEASURE, facts, candidate.areaId)
    }

    private fun textMeasure(id: String, facts: FactSet, entityId: String?): String? {
        if (entityId == null) return null
        val measure = Measures.byId(id) ?: return null
        return (measure.read(facts, entityId, zone) as? MeasureValue.Text)?.value
    }

    /**
     * The fact this plan is about, recorded on the event and never compared against.
     *
     * 10.3 puts a `resolutionFactRef` on a plan and 10.6 names it as what the follow
     * through matches on. The match this app makes is on `(family, subject)`, which is what
     * `PlanOffered` carries and what escalation and cooldown are already keyed by, so this
     * is a record of the fact behind the motivating observation rather than an input to a
     * decision. It is the observation's own leading reference where it has one, which
     * re-reads through `FactLookup` like every other reference in the app.
     */
    private fun resolutionFactOf(motivating: Candidate, subject: String?): FactRef =
        motivating.sourceFacts.toSortedMap().values.firstOrNull()
            ?: Measures.byId(AREA_EVENTS_MEASURE)?.refFor(subject)
            ?: FactRef(FALLBACK_CATEGORY, FALLBACK_PATH)

    private fun templateOf(line: CorpusLine): Template? = runCatching { Template(line.text) }.getOrNull()

    /** `Wednesday`. A weekday as a person writes it, through `java.time` rather than a table. */
    private fun dayName(day: Weekday): String =
        DayOfWeek.of(day.ordinal + 1).getDisplayName(TextStyle.FULL, Locale.US)

    /** The sentence's first letter, capitalized. See [render]. */
    private fun opening(text: String): String =
        if (text.isEmpty()) text else text.replaceFirstChar { it.uppercaseChar() }

    /**
     * True where [text] opens with a gerund, which is what `com.02` needs.
     *
     * `closing the oldest item in Work` is one and `ten minutes in Personal` is not, and
     * 4.4 gives that exact pair as its worked example of why the two forms differ. The test
     * is the first word's ending, which is morphology and not judgment: nothing here
     * decides what a line means, only whether the shape `com.02` was written around is the
     * shape in front of it.
     */
    private fun isGerundPhrase(text: String): Boolean =
        text.substringBefore(' ').lowercase().endsWith(GERUND_ENDING)

    private companion object {

        /** These are Report lines, so 7.2 renders any number as a digit. There are none. */
        val PURPOSE = Purpose.REPORT_OBSERVATION

        const val FIRST_STAGE = 1

        const val PLAN_ID_PREFIX = "plan-"

        /** 10.4 rule 6. */
        const val HARD_STRETCH: FamilyKey = "hardStretch"
        const val DECLINING_ACTIVITY: FamilyKey = "decliningActivity"
        const val NEGLECTED_AREA: FamilyKey = "neglectedArea"

        /** 4.6. */
        const val QUIET_WEEK: FamilyKey = "quietWeek"
        const val NO_RHYTHM_BENCH = "cls.new"
        const val LET_IT_BE_BENCH = "cls.let"
        const val TRUST_THE_PACE_BENCH = "cls.trust"
        const val REVIEW_BENCH = "cls.rev"
        const val CLOSING_RULE_KEY = "guidance.closing"

        /** 4.2's part of day bank, and the band every line in it names. */
        const val BAND_BENCH = "cue.band"

        /** 4.2's weekday bank, and the fact its own heading says it requires. */
        const val DAY_BENCH = "cue.day"
        val MORNING_BAND = PartOfDay.MORNING

        /** The markers section 4 writes. */
        const val CUE_SLOT: SlotKey = "cue"
        const val ACTION_SLOT: SlotKey = "action"
        const val ACTION_GERUND_SLOT: SlotKey = "actionGerund"
        const val ACTION_NOUN_SLOT: SlotKey = "actionNoun"
        const val STRONGEST_WEEKDAY_SLOT: SlotKey = "strongestWeekday"
        const val QUIETEST_WEEKDAY_SLOT: SlotKey = "quietestWeekday"
        const val FOCUS_WEEKDAY_SLOT: SlotKey = "focusTypicalWeekday"
        const val AREA_NAME_SLOT: SlotKey = "areaName"
        const val ITEM_TITLE_SLOT: SlotKey = "itemTitle"
        const val OTHER_AREA_SLOT: SlotKey = "otherArea"

        /** What fills the two name markers, through the one table that reads a name. */
        const val AREA_NAME_MEASURE = "areaName"
        const val ITEM_AREA_MEASURE = "itemAreaName"
        const val ITEM_TITLE_MEASURE = "itemTitle"
        const val AREA_EVENTS_MEASURE = "areaEvents"

        const val FALLBACK_CATEGORY = "window"
        const val FALLBACK_PATH = "totalEvents"

        const val GERUND_ENDING = "ing"
    }
}
