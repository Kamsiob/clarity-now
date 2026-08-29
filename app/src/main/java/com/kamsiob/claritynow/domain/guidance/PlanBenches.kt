package com.kamsiob.claritynow.domain.guidance

import com.kamsiob.claritynow.domain.engine.FamilyKey
import com.kamsiob.claritynow.domain.engine.catalog.ClarityCatalog
import com.kamsiob.claritynow.domain.engine.catalog.CorpusLine

/**
 * 4.1's twelve frames, as the set a cue shape that reads everywhere is given.
 *
 * Declared at file scope rather than on the companion because an enum entry is
 * constructed before its class's companion object exists, so an entry reading
 * `Companion.EVERY_FRAME` would read null on the first call and never again.
 *
 * Written out rather than generated from a count, so that a thirteenth frame added to
 * 4.1 is missing from it rather than silently included. `PlanFormTest` holds the two
 * ends together by asserting that the shapes between them name every frame the corpus
 * carries and no frame it does not.
 */
private val EVERY_FRAME: Set<String> = setOf(
    "frm.01", "frm.02", "frm.03", "frm.04", "frm.05", "frm.06",
    "frm.07", "frm.08", "frm.09", "frm.10", "frm.11", "frm.12",
)

/** 4.4's if then form, and the only one that takes a cue after a copula. */
private const val IF_THEN_FORM = "com.01"

/** 4.4's two forms that put the cue in an adjunct. */
private val ADJUNCT_COMMITMENTS: Set<String> = setOf("com.02", "com.03")

/** All three of 4.4's forms. */
private val EVERY_COMMITMENT: Set<String> = setOf(IF_THEN_FORM) + ADJUNCT_COMMITMENTS

/**
 * Section 4 of `CORPUS_2_REPORT.md`, as the five banks a plan is assembled from.
 *
 * `ReportWalker` files every section 4 line into the catalog's auxiliary map under the
 * bench its key names. This is the structured view of that map: three banks a plan is
 * built from, one that renders it back in first person on acceptance, and four complete
 * lines to close with when there is no plan to offer.
 *
 * **Nothing here authors a word.** Every string reaching a person through layer 6 is a
 * line an author wrote into section 4 and a reviewer read against that section's four
 * tests: a single concrete act, completable inside a week, implying no failure, and
 * surviving being read preceded by *You should have*.
 */
internal class PlanBenches private constructor(
    val frames: List<CorpusLine>,
    val cues: List<CorpusLine>,
    val actions: List<CorpusLine>,
    val commitments: List<CorpusLine>,
    val closings: Map<String, List<CorpusLine>>,
) {

    /** Every section 4 line, for the checks that read the whole of it. */
    val allLines: List<CorpusLine>
        get() = frames + cues + actions + commitments + closings.values.flatten()

    /** The frames [cue] may be rendered inside, or empty when its shape is undeclared. */
    fun framesFor(cue: CorpusLine): List<CorpusLine> {
        val allowed = CUE_SHAPES[cue.key]?.frames ?: return emptyList()
        return frames.filter { it.key in allowed }
    }

    /** The actions [family] may motivate. `CORPUS_2_REPORT.md` 4.3. */
    fun actionsFor(family: FamilyKey): List<CorpusLine> = actions.filter { action ->
        ACTION_FAMILIES[action.key.substringBeforeLast('.')]?.contains(family) == true
    }

    /** True where the corpus wrote actions for [family] at all. See [ACTION_FAMILIES]. */
    fun motivates(family: FamilyKey): Boolean = family in MOTIVATING_FAMILIES

    /**
     * How a cue reads grammatically, which decides the frames and the commitment forms it
     * fits inside.
     *
     * ## Why this is declared rather than derived
     *
     * `CORPUS_2_REPORT.md` 4.9 rule 5 asks an author to write every cue out inside every
     * frame and every commitment form and check it reads. No cue reads everywhere: *One
     * option for before you open Work* is not English, neither is *There is room your first
     * hour for ten minutes in Reading*, and neither is *If it's before you add anything
     * new, my one thing is ten minutes in Personal.*
     *
     * The obvious answer is a grammatical type system that infers the shape from the
     * line's own first word, and `design-v3.md` 15 asks for the obvious answer to be
     * examined rather than taken. It loses here on a specific failure: the difference
     * between *the next time you finish something*, which reads in all twelve frames and
     * in no if clause, and *Wednesday morning*, which reads in both, is not visible in
     * either line's morphology. An inference that got it wrong would produce an
     * ungrammatical sentence with nothing failing, which is the worst outcome available.
     *
     * So the shapes are written down, one per cue, and each is a judgment somebody can
     * check by reading it against the twelve frames and the three forms. **A cue with no
     * entry here is unavailable**, which fails toward no plan rather than toward a bad one,
     * and `PlanFormTest` reports any cue in the corpus that has no entry so that a bench
     * grown without a shape is loud rather than silent.
     *
     * ## Why the commitment forms are on the shape too
     *
     * They were a boolean before, because 4.4's two fillable forms both wanted an adjunct
     * and the only question was whether a cue could be one. 4.4 now opens with the if then
     * form 10.2 calls the mechanism, that form wants the opposite kind of phrase, and a
     * boolean cannot say which. Splitting them is also what makes [NOMINAL] a shape a plan
     * can use: *your first hour* reads in no adjunct form and reads perfectly after *If
     * it's*, and three approved cues that had never once reached a screen are reachable
     * through `com.01` and through nothing else.
     */
    enum class CueShape(val frames: Set<String>, val commitments: Set<String>) {

        /**
         * A bare calendar phrase: *Wednesday*, *Wednesday morning*, *next Wednesday*,
         * *Wednesday before midday*.
         *
         * Reads as an argument, as an adjunct, and after *If it's*, so every frame and
         * every commitment form. It is the only shape that reads in all three forms, which
         * is why 10.2's own worked example uses one.
         */
        DATED(EVERY_FRAME, EVERY_COMMITMENT),

        /**
         * A temporal noun phrase that is not a date: *the next time you finish something*,
         * *the next time you open the app*.
         *
         * Every frame except the two that put the cue between a verb and its own
         * complement. *There is room the next time you finish something for ten minutes in
         * Reading* separates *room* from *for* by seven words and stops being a sentence;
         * a date does not, because *There is room Wednesday for ten minutes in Reading* is
         * two words instead of seven. **This shape carried those two frames until the
         * guidance language pass read every cue against every frame**, which is 4.9 rule 5
         * and is the reading that found it.
         *
         * It does not read after *If it's* either, where the copula wants something a day
         * can be: *If it's the next time you finish something* has no reading at all.
         */
        TEMPORAL(EVERY_FRAME - setOf("frm.06", "frm.10"), ADJUNCT_COMMITMENTS),

        /**
         * A determiner headed noun phrase: *your first hour*, *the start of Wednesday*,
         * *the day you usually get most done*.
         *
         * An argument only. *Something to consider your first hour* has no preposition to
         * hang the phrase on, so the adjunct frames are out, and so are the two commitment
         * forms that put the cue in an adjunct. `com.01` is the one form it reads in, and
         * it reads there cleanly: *If it's your first hour, my one thing is ten minutes in
         * Personal.*
         */
        NOMINAL(
            setOf("frm.01", "frm.02", "frm.03", "frm.07", "frm.09"),
            setOf(IF_THEN_FORM),
        ),

        /**
         * A noun phrase carrying an appositive: *Wednesday, your busiest day*.
         *
         * The frames that punctuate immediately after the cue, and no commitment form at
         * all. An appositive needs a closing comma and only those frames supply one:
         * *Wednesday, your busiest day might be the moment* reads as a sentence missing a
         * mark. **It is the one shape with no commitment form**, so a cue declared here is
         * offered by nothing, and that is recorded in 4.4 rather than worked around: *If
         * it's the morning, when you finish most things, my one thing is ten minutes in
         * Personal* is two subordinate clauses before its subject.
         */
        APPOSITIVE(setOf("frm.01", "frm.02", "frm.12"), emptySet()),

        /**
         * A prepositional phrase or a subordinate clause: *before Friday*, *after your
         * next focus session*, *before you open Work*, *once Wednesday arrives*.
         *
         * The adjunct frames, plus the argument frames whose own preposition absorbs it.
         * 4.7's worked plans use two of these pairings by name, `frm.02` with `cue.hab.03`
         * and `frm.03` with `cue.hab.02`, which is the corpus settling the question.
         * `frm.01` is out because *One option for before you add anything new* puts two
         * prepositions together, and `frm.07` and `frm.09` are out because a prepositional
         * phrase cannot be the subject of *would suit* and a clause shaped one doubles the
         * copula in *Before Work is opened is one place for ten minutes in Reading.*
         */
        ADJUNCT(
            setOf(
                "frm.02", "frm.03", "frm.04", "frm.05", "frm.06",
                "frm.08", "frm.10", "frm.11", "frm.12",
            ),
            ADJUNCT_COMMITMENTS,
        ),

        /**
         * A cue ending in a clause of its own: *when Wednesday comes around*, *before
         * midday, where most of your completions land*.
         *
         * Only the frames that follow the cue with a colon. Anything that continues the
         * sentence after the cue collides with the clause that closed it: *There is room
         * before midday, where most of your completions land for ten minutes in Reading*
         * attaches the wrong words to each other.
         */
        TRAILING_CLAUSE(
            setOf("frm.04", "frm.05", "frm.08", "frm.11", "frm.12"),
            ADJUNCT_COMMITMENTS,
        ),

        ;

        /**
         * True where some form in 4.4 can store a cue of this shape.
         *
         * The stored line is mandatory, so a cue that reads in a frame and in no commitment
         * form would produce an offer nobody could accept. Derived rather than declared,
         * because a second declaration is a second thing to disagree with [commitments].
         */
        val commitmentReady: Boolean get() = commitments.isNotEmpty()
    }

    companion object {

        /** 4.1. The seven frames, keyed `frm.NN`. */
        const val FRAME_BENCH = "frm"

        /** 4.4. The commitment forms, keyed `com.NN`. */
        const val COMMITMENT_BENCH = "com"

        /** The bank prefix of every cue bench, `cue.day`, `cue.band`, `cue.hab`, `cue.bound`. */
        const val CUE_BANK = "cue"

        /** The bank prefix of every action bench. */
        const val ACTION_BANK = "act"

        /** 4.6. The four non plan closings, in the order the corpus lists them. */
        val CLOSING_BENCHES: List<String> = listOf("cls.trust", "cls.let", "cls.new", "cls.rev")

        /**
         * Which cue reads inside which frames. One entry per line in 4.2. See [CueShape].
         *
         * Read this against the corpus rather than against the code: every entry is a
         * claim that one authored line reads grammatically inside a named set of authored
         * frames, and the only way to check it is to say the sentences out loud.
         */
        val CUE_SHAPES: Map<CueKey, CueShape> = mapOf(
            // 4.2 Weekday
            "cue.day.01" to CueShape.DATED,
            "cue.day.02" to CueShape.APPOSITIVE,
            "cue.day.03" to CueShape.TRAILING_CLAUSE,
            "cue.day.04" to CueShape.ADJUNCT,
            "cue.day.05" to CueShape.NOMINAL,
            "cue.day.06" to CueShape.DATED,
            "cue.day.07" to CueShape.NOMINAL,
            "cue.day.08" to CueShape.NOMINAL,
            "cue.day.09" to CueShape.TRAILING_CLAUSE,
            // 4.2 Part of day
            "cue.band.01" to CueShape.DATED,
            "cue.band.02" to CueShape.APPOSITIVE,
            "cue.band.03" to CueShape.TRAILING_CLAUSE,
            "cue.band.04" to CueShape.NOMINAL,
            "cue.band.05" to CueShape.ADJUNCT,
            "cue.band.06" to CueShape.TRAILING_CLAUSE,
            "cue.band.07" to CueShape.NOMINAL,
            "cue.band.08" to CueShape.DATED,
            "cue.band.09" to CueShape.TRAILING_CLAUSE,
            "cue.band.10" to CueShape.ADJUNCT,
            "cue.band.11" to CueShape.NOMINAL,
            // 4.2 Behavioral
            "cue.hab.01" to CueShape.ADJUNCT,
            "cue.hab.02" to CueShape.ADJUNCT,
            "cue.hab.03" to CueShape.ADJUNCT,
            "cue.hab.04" to CueShape.TEMPORAL,
            "cue.hab.05" to CueShape.ADJUNCT,
            "cue.hab.06" to CueShape.TEMPORAL,
            "cue.hab.07" to CueShape.ADJUNCT,
            "cue.hab.08" to CueShape.ADJUNCT,
            "cue.hab.09" to CueShape.ADJUNCT,
            "cue.hab.10" to CueShape.ADJUNCT,
            "cue.hab.11" to CueShape.TRAILING_CLAUSE,
            "cue.hab.12" to CueShape.ADJUNCT,
            // 4.2 Boundary
            "cue.bound.01" to CueShape.ADJUNCT,
            "cue.bound.02" to CueShape.ADJUNCT,
            "cue.bound.03" to CueShape.ADJUNCT,
            "cue.bound.04" to CueShape.ADJUNCT,
            "cue.bound.05" to CueShape.NOMINAL,
            "cue.bound.06" to CueShape.ADJUNCT,
            "cue.bound.07" to CueShape.ADJUNCT,
            "cue.bound.08" to CueShape.ADJUNCT,
            "cue.bound.09" to CueShape.ADJUNCT,
            "cue.bound.10" to CueShape.ADJUNCT,
            "cue.bound.11" to CueShape.ADJUNCT,
        )

        /**
         * Which observation family motivates which action bench. `CORPUS_2_REPORT.md` 4.3.
         *
         * **The corpus states this in its own subheadings**, `### From intakeVsOutput or
         * queuePressure` and the seven like it, and this is a transcription of them. A
         * transcription is a second place to disagree, so `PlanFormTest` reads those
         * subheadings out of the committed file and asserts this map equals them. The
         * corpus stays the source and the build fails if the two drift.
         *
         * Parsing them into the catalog instead was the other option and it is the one
         * this file does not take. It would put a field on `ParsedCorpus` and on
         * `ClarityCatalog` that only layer 6 reads, for a mapping of eight entries that
         * changes when the action bank changes and not otherwise. The checked
         * transcription gives the same guarantee and can be deleted in one file.
         *
         * **This map is also 10.4 rule 3.** A plan may be produced only when there is a
         * real friction pattern, and an action bank exists for exactly those families the
         * corpus judged a plan could help with. A week whose observations are all outside
         * this map is a week with no friction to act on, and it gets no plan without
         * anything having to decide that a week was good.
         */
        val ACTION_FAMILIES: Map<String, Set<FamilyKey>> = mapOf(
            "act.fin" to setOf("intakeVsOutput", "queuePressure"),
            "act.neg" to setOf("neglectedArea"),
            "act.oth" to setOf("singleFocus"),
            "act.brk" to setOf("persistentItem"),
            "act.foc" to setOf("focusInvestment", "focusHabitFading"),
            "act.pick" to setOf("queueDrained"),
            "act.set" to setOf("switchingBehavior"),
            "act.rep" to setOf("dayShape", "timeOfDay"),
        )

        /** Every family the action bank was written for. 10.4 rule 3. */
        val MOTIVATING_FAMILIES: Set<FamilyKey> = ACTION_FAMILIES.values.flatten().toSet()

        /**
         * The banks read out of the catalog's auxiliary map.
         *
         * Every bench section 4 declares must be present and non empty. A bank that
         * parsed to nothing is a section the walker stopped reading, and a plan assembled
         * from two of three banks is not a plan.
         */
        fun of(catalog: ClarityCatalog): PlanBenches {
            fun bench(key: String): List<CorpusLine> = catalog.auxiliary[key].orEmpty()
            fun bank(prefix: String): List<CorpusLine> = catalog.auxiliary
                .filterKeys { it == prefix || it.startsWith("$prefix.") }
                .toSortedMap()
                .values
                .flatten()
            return PlanBenches(
                frames = bench(FRAME_BENCH),
                cues = bank(CUE_BANK),
                actions = bank(ACTION_BANK),
                commitments = bench(COMMITMENT_BENCH),
                closings = CLOSING_BENCHES.associateWith { bench(it) },
            )
        }
    }
}
