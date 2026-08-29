package com.kamsiob.claritynow.domain.guidance

import com.kamsiob.claritynow.domain.engine.CueFacts
import com.kamsiob.claritynow.domain.engine.FiringHistory
import com.kamsiob.claritynow.domain.engine.PartOfDay
import com.kamsiob.claritynow.domain.engine.Validated
import com.kamsiob.claritynow.domain.engine.Weekday
import com.kamsiob.claritynow.domain.engine.catalog.CorpusFixture
import com.kamsiob.claritynow.domain.engine.catalog.Purpose
import com.kamsiob.claritynow.domain.engine.catalog.Template
import com.kamsiob.claritynow.domain.engine.validate.ValidateFixture
import java.io.File
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The form a plan takes, and the one form that must exist nowhere.
 * CLARITY_LOGIC_ENGINE.md 10.2 and 10.5, and `CORPUS_2_REPORT.md` 4.
 *
 * > The plan is **offered** in a frame that is explicitly optional and grammatically
 * > **nominal**, never imperative. On acceptance it is **stored in first person** as a
 * > proper if then, and that is the only form ever shown afterwards. **The imperative form
 * > never exists anywhere in the app.**
 *
 * Everything here is asserted over the committed corpus rather than over sentences the
 * composer happened to produce. A test of the output can only find the imperatives the
 * fixtures provoked; a test of the benches finds the one an author writes next week.
 */
class PlanFormTest {

    private val benches = PlanBenches.of(CorpusFixture.catalog)

    // ----------------------------------------------------------------- the imperative

    /**
     * 10.2 and 4.9 rule 6. Not one line in section 4 is an imperative.
     *
     * ## How an imperative is recognized without a parts of speech library
     *
     * An English imperative opens with a bare verb. Every line in section 4 opens with
     * something else and each opening is one of three kinds: a gerund, which is what 4.9
     * rule 6 asks an action to be; a determiner, quantifier or number, which is what a
     * noun phrase opens with; or a preposition or subordinator, which is what an adjunct
     * opens with.
     *
     * So the test has two halves and both are needed. The first is a **closed list of the
     * bare verbs this bank would produce if somebody rewrote a line as a command**, taken
     * from the gerunds actually in it: `finishing` becomes `finish`, `deciding` becomes
     * `decide`. The second is that every opening word is in one of the three permitted
     * kinds, which catches a verb nobody thought to list.
     *
     * The closings in 4.6 are complete sentences rather than fragments and two of them do
     * open with a bare verb, `Leave it.` and `Keep the shape.`, which the corpus wrote on
     * purpose and the owner approved. They are not plans and 10.2's prohibition is about
     * the plan. They are excluded by name, which is a shorter list than the exception
     * would otherwise be and puts the two lines where somebody will see them.
     */
    @Test
    fun `no frame, cue, action or commitment form is an imperative`() {
        val planLines = benches.frames + benches.cues + benches.actions + benches.commitments
        val offenders = planLines.mapNotNull { line ->
            val first = line.text.substringBefore(' ').trim(',', ':', '.').lowercase()
            when {
                first in BARE_VERBS -> "${line.key} opens with the bare verb `$first`: ${line.text}"
                first.endsWith(GERUND) -> null
                first in PERMITTED_OPENERS -> null
                first.startsWith("{") -> null
                first.toIntOrNull() != null -> null
                else -> "${line.key} opens with `$first`, which is not a gerund, a " +
                    "determiner or a preposition, so it may be a verb: ${line.text}"
            }
        }
        assertEquals("an imperative, or an opening nobody has classified", emptyList<String>(), offenders)
    }

    /** 4.9 rule 6, the other two halves of it. No exclamation marks and no `try to`. */
    @Test
    fun `no guidance line hedges or exclaims`() {
        val offenders = benches.allLines.filter { line ->
            "!" in line.text || TRY_TO in line.text.lowercase()
        }.map { "${it.key}: ${it.text}" }
        assertEquals("an exclamation mark or a `try to` in section 4", emptyList<String>(), offenders)
    }

    // ----------------------------------------------------------------- the benches

    /** Every bank section 4 declares parsed, and none of them empty. */
    @Test
    fun `all five guidance banks parsed out of the corpus`() {
        assertEquals("frames, CORPUS_2_REPORT.md 4.1", FRAMES, benches.frames.size)
        assertEquals("cues, 4.2", CUES, benches.cues.size)
        assertEquals("actions, 4.3", ACTIONS, benches.actions.size)
        assertEquals("commitment forms, 4.4", COMMITMENTS, benches.commitments.size)
        assertEquals("non plan closings, 4.6", CLOSINGS, benches.closings.values.sumOf { it.size })
    }

    /**
     * Every cue in the corpus has a declared shape, so none is silently unavailable.
     *
     * `PlanBenches.CUE_SHAPES` fails closed: a cue with no entry can enter no frame and is
     * simply never offered. That is the right failure and it is a silent one, so this is
     * the thing that makes it loud.
     */
    @Test
    fun `every cue in the corpus has a declared grammatical shape`() {
        val undeclared = benches.cues.filter { it.key !in PlanBenches.CUE_SHAPES }.map { it.key }
        assertEquals(
            "a cue with no entry in PlanBenches.CUE_SHAPES can enter no frame and will " +
                "never be offered. Give it a shape or say why it has none",
            emptyList<String>(),
            undeclared,
        )
        val extra = PlanBenches.CUE_SHAPES.keys - benches.cues.map { it.key }.toSet()
        assertEquals("a shape declared for a cue the corpus does not have", emptySet<String>(), extra)
    }

    /**
     * `PlanBenches.ACTION_FAMILIES` says what the corpus's own subheadings say.
     *
     * 4.3 heads each action bank `### From intakeVsOutput or queuePressure` and the seven
     * like it, and that map is a transcription of them. This reads the headings out of the
     * committed file and compares, so the corpus stays the source and a bank retargeted
     * there fails the build here rather than quietly keeping the old family.
     */
    @Test
    fun `the action to family map is what the corpus subheadings say`() {
        assertEquals(
            "PlanBenches.ACTION_FAMILIES has drifted from CORPUS_2_REPORT.md 4.3",
            actionFamiliesFromCorpus(),
            PlanBenches.ACTION_FAMILIES,
        )
    }

    /**
     * Every line in the part of day bank names the morning.
     *
     * `GuidanceComposer.substantiatedCues` holds the whole bank back unless
     * `CueFacts.productiveBand` is the morning, because *the morning, when you finish most
     * things* is false of anybody whose band is the evening and 3.7 calls an unsubstantiated
     * cue worse than no plan. That gate is only correct while the bank really is about
     * mornings, so this is what makes an evening line added later fail the build instead of
     * telling somebody something untrue about their own day.
     */
    @Test
    fun `every part of day cue names the morning`() {
        val offenders = benches.cues
            .filter { it.key.startsWith("cue.band") }
            .filterNot { line -> MORNING_WORDS.any { it in line.text.lowercase() } }
            .map { "${it.key}: ${it.text}" }
        assertEquals(
            "a cue.band line that is not about the morning. GuidanceComposer gates the " +
                "whole bank on productiveBand being MORNING, and that gate is now wrong",
            emptyList<String>(),
            offenders,
        )
    }

    // ----------------------------------------------------------------- 4.5, and the gap

    /**
     * 4.5. The two labels are fixed, and `strings.xml` says exactly what the corpus says.
     *
     * `ReportWalker` skips 4.5's fenced block because the labels carry no key and are
     * interface labels rather than observations, which CLAUDE.md rule 8 puts in
     * `strings.xml`. A skip has to be closed by something, and this is it: the two
     * resources are read out of the real file and compared with the two lines in the real
     * corpus. 4.5 says varying them would make the choice feel like a game.
     */
    @Test
    fun `the accept and decline labels in strings agree with the corpus`() {
        val corpus = CorpusFixture.reportText
            .substringAfter(ACCEPT_HEADING)
            .substringAfter("```")
            .substringBefore("```")
            .trim()
            .lines()
            .associate { it.substringBefore(':').trim() to it.substringAfter(':').trim() }
        val strings = File("..", STRINGS_PATH).readText()
        fun resource(name: String) = Regex("""<string name="$name">(.*?)</string>""")
            .find(strings)!!
            .groupValues[1]
            .replace("\\'", "'")
        assertEquals("R.string.report_accept against 4.5", corpus["Accept"], resource("report_accept"))
        assertEquals("R.string.report_decline against 4.5", corpus["Decline"], resource("report_decline"))
    }

    /**
     * `com.01` is the if then form, and it renders.
     *
     * 10.2 makes the stored line a proper if then and 4.4's first form is it. The form that
     * stood here before was `If it's {cue}, I'll {actionVerb}.`, 4.4 promised a verb form
     * beside every gerund in 4.3, the bank carried none, and the form never rendered once.
     * **The corpus closed it rather than the code**, by building the if then out of the noun
     * phrase every action already is, and 4.4 records why the fifty four verb forms were the
     * worse answer: in isolation each one is an imperative, and a bank of them is the one
     * artifact 10.2 says exists nowhere in this app.
     *
     * So this asserts the three things that were false the day before: the first form is an
     * if then, it needs no marker the corpus does not supply, and a real plan comes back
     * wearing it.
     */
    @Test
    fun `the if then commitment form renders from what the action bank carries`() {
        val ifThen = benches.commitments.minByOrNull { it.key }!!
        assertTrue("4.4's first form is the if then one", ifThen.text.startsWith(IF_THEN_OPENER))
        val markers = Template(ifThen.text).slots
        assertTrue("com.01 still asks for a verb form no action carries", ACTION_VERB !in markers)
        assertEquals("com.01 asks for a marker the composer does not fill", SUPPLIED, markers)
        val stored = (0 until SAMPLE_WEEKS).mapNotNull { week ->
            val result = GuidanceComposer(CorpusFixture.catalog, ZONE).compose(
                headline = null,
                appeared = listOf(neglected()),
                facts = ValidateFixture.facts(cues = RHYTHM),
                plans = PlanHistory.EMPTY,
                history = FiringHistory.EMPTY,
                weekStartKey = "2026-03-0${week + 1}",
            )
            (result as? GuidanceResult.Plan)?.plan?.committedLine
        }
        assertTrue("no plan was composed at all", stored.isNotEmpty())
        assertTrue(
            "no week stored its plan as an if then, and 10.2 makes that the mechanism: $stored",
            stored.any { it.startsWith(IF_THEN_OPENER) },
        )
    }

    /**
     * Every frame is reachable and every frame a shape names exists.
     *
     * `PlanBenches.CueShape` carries a written out set of frame keys per shape rather than a
     * generated one, so that a thirteenth frame is absent from it rather than silently
     * included. Absent is the safe failure and it is a silent one, so this is what makes it
     * loud, in both directions: a frame no shape names can never be offered, and a shape
     * naming a frame the corpus does not have is a judgment about a line nobody wrote.
     */
    @Test
    fun `the cue shapes between them name every frame and no other`() {
        val named = PlanBenches.CueShape.entries.flatMap { it.frames }.toSet()
        val real = benches.frames.map { it.key }.toSet()
        assertEquals("a frame in 4.1 that no cue shape can enter", emptySet<String>(), real - named)
        assertEquals("a cue shape naming a frame 4.1 does not have", emptySet<String>(), named - real)
    }

    /**
     * Every commitment form is reachable and every form a shape names exists.
     *
     * The same check on the other half of `CueShape`. A form no shape names is a stored line
     * nobody ever sees, and a shape naming a form 4.4 does not have would drop every cue of
     * that shape out of the plan silently.
     */
    @Test
    fun `the cue shapes between them name every commitment form and no other`() {
        val named = PlanBenches.CueShape.entries.flatMap { it.commitments }.toSet()
        val real = benches.commitments.map { it.key }.toSet()
        assertEquals("a form in 4.4 that no cue shape can be stored in", emptySet<String>(), real - named)
        assertEquals("a cue shape naming a form 4.4 does not have", emptySet<String>(), named - real)
    }

    // ----------------------------------------------------------------- what it reads like

    /**
     * The offered and stored forms of a real plan, printed for a person to read.
     *
     * Not an assertion about wording, which is the corpus's. It asserts the two structural
     * properties 10.2 states, that the offered line is not first person and the stored line
     * is, and prints both so that the four tests in section 4 can be applied by the only
     * thing that can apply them.
     */
    @Test
    fun `the offered line is not first person and the stored line is`() {
        val plans = (0 until SAMPLE_WEEKS).mapNotNull { week ->
            val result = GuidanceComposer(CorpusFixture.catalog, ZONE).compose(
                headline = null,
                appeared = listOf(neglected()),
                facts = ValidateFixture.facts(cues = RHYTHM),
                plans = PlanHistory.EMPTY,
                history = FiringHistory.EMPTY,
                weekStartKey = "2026-03-0${week + 1}",
            )
            (result as? GuidanceResult.Plan)?.plan
        }
        assertTrue("no plan was composed at all", plans.isNotEmpty())
        println("Worked plans, CORPUS_2_REPORT.md 4.7:")
        for (plan in plans) {
            println("  ${plan.frameKey} + ${plan.cueKey} + ${plan.actionKey}")
            println("    offered: ${plan.offeredLine}")
            println("    stored:  ${plan.committedLine}")
        }
        for (plan in plans) {
            assertFalse(
                "the offered line is in the first person, and 10.2 makes it nominal: " +
                    plan.offeredLine,
                FIRST_PERSON.containsMatchIn(plan.offeredLine),
            )
            assertTrue(
                "the stored line is not in the first person, and 10.2 requires it: " +
                    plan.committedLine,
                FIRST_PERSON.containsMatchIn(plan.committedLine),
            )
        }
    }

    // ----------------------------------------------------------------- helpers

    private fun neglected(): Validated = Validated(
        ValidateFixture.candidate(
            ruleKey = "report.observation.neglectedArea.s2",
            purpose = Purpose.REPORT_OBSERVATION,
            familyKey = "neglectedArea",
            variantKey = "ob.neg.s2.l01",
            rendered = "Reading has been quiet for three weeks.",
            renderedQuestion = null,
            slots = emptyMap(),
            sourceFacts = emptyMap(),
            namedAreaIds = setOf(ValidateFixture.READING),
            namedItemIds = emptySet(),
            subjectId = ValidateFixture.READING,
        ),
    )

    /** `### From \`intakeVsOutput\` or \`queuePressure\`` and the seven like it. */
    private fun actionFamiliesFromCorpus(): Map<String, Set<String>> {
        val section = CorpusFixture.reportText
            .substringAfter(ACTION_HEADING)
            .substringBefore(COMMITMENT_HEADING)
        val out = linkedMapOf<String, MutableSet<String>>()
        var families: Set<String> = emptySet()
        var inFence = false
        for (raw in section.lines()) {
            val line = raw.trim()
            if (line.startsWith("```")) {
                inFence = !inFence
                continue
            }
            if (!inFence && line.startsWith("### From ")) {
                families = BACKTICKED.findAll(line).map { it.groupValues[1] }.toSet()
                continue
            }
            if (!inFence) continue
            val key = line.substringBefore(' ')
            if (!key.startsWith("act.")) continue
            out.getOrPut(key.substringBeforeLast('.')) { mutableSetOf() }.addAll(families)
        }
        return out.mapValues { it.value.toSet() }
    }

    private companion object {

        val ZONE: ZoneId = ZoneId.of("UTC")

        /** 4.8's own totals table. */
        const val FRAMES = 12
        const val CUES = 43
        const val ACTIONS = 130
        const val COMMITMENTS = 3
        const val CLOSINGS = 78

        const val SAMPLE_WEEKS = 6

        const val GERUND = "ing"
        const val TRY_TO = "try to"
        const val ACTION_VERB = "actionVerb"

        /** The two markers `GuidanceComposer.commitment` can fill. 4.4. */
        val SUPPLIED = setOf("cue", "actionNoun")
        const val IF_THEN_OPENER = "If it's"
        const val ACCEPT_HEADING = "## 4.5 The accept and decline labels"
        const val ACTION_HEADING = "## 4.3 Action bank"
        const val COMMITMENT_HEADING = "## 4.4 Commitment rendering"
        const val STRINGS_PATH = "app/src/main/res/values/strings.xml"

        val BACKTICKED = Regex("""`([A-Za-z]+)`""")

        val FIRST_PERSON = Regex("""\b(?:I|I'll|I'm|my)\b""", RegexOption.IGNORE_CASE)

        val MORNING_WORDS = listOf("morning", "midday", "first hour", "early")

        /**
         * The bare verbs the gerunds in 4.3 would become if a line were rewritten as a
         * command. Read off the bank rather than imagined, so the list is exactly as long
         * as the bank it guards.
         */
        val BARE_VERBS = setOf(
            "finish", "close", "pick", "clear", "take", "move", "let", "open", "read",
            "decide", "put", "find", "write", "replace", "add", "name", "spend", "check",
            "start", "give", "protect", "look", "try", "do", "make", "keep", "leave",
            "come", "rest", "ask", "carry",
        )

        /**
         * What a noun phrase or an adjunct opens with, in this corpus.
         *
         * A closed list read off the benches, for the reason `SlotRenderer.PLURAL_NOUNS`
         * is one: a heuristic about word shape would pass a verb it had not met. Anything
         * not here and not a gerund fails the test and has to be classified by hand, which
         * is the point.
         */
        val PERMITTED_OPENERS = setOf(
            // determiners and quantifiers
            "a", "an", "the", "one", "two", "three", "your", "my", "whatever", "anything",
            "something", "nothing", "some", "no", "this", "that", "steady", "ten",
            "fifteen", "twenty", "twentyfive", "any",
            // prepositions and subordinators
            "before", "after", "at", "in", "on", "when", "early", "there", "if",
            "once", "next",
        )

        val RHYTHM = CueFacts(
            strongestWeekday = Weekday.WED,
            strongestWeekdayConfidence = 0.75,
            quietestWeekday = Weekday.SUN,
            productiveBand = PartOfDay.MORNING,
            productiveBandShare = 0.62,
            focusTypicalWeekday = Weekday.TUE,
            focusTypicalBand = PartOfDay.MORNING,
            addingBand = PartOfDay.EVENING,
            weekdayOnly = true,
            hasStableRhythm = true,
        )
    }
}
