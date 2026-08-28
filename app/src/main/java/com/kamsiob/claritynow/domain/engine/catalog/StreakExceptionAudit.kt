package com.kamsiob.claritynow.domain.engine.catalog

import com.kamsiob.claritynow.domain.engine.FactSet
import com.kamsiob.claritynow.domain.engine.FamilyKey
import com.kamsiob.claritynow.domain.engine.HistoryFacts

/**
 * The audit that keeps the streak exception scoped. CLARITY_LOGIC_ENGINE.md 1.1 and 3.1,
 * and the class note on `HistoryFacts`.
 *
 * ## What the exception is
 *
 * 1.1 bans streak facts outright: no `currentStreak`, no `longestStreak`, no `daysInARow`.
 * Their absence is what makes streak language structurally impossible rather than merely
 * discouraged, and the ban is correct. It was also blocking the wrong thing.
 * `HistoryFacts.currentQuietRunDays` and `currentSingleAreaRunDays` count **absence**, and
 * the loss aversion the ban exists to prevent cannot attach to an absence: nobody protects
 * a quiet week, and nobody can be told they lost one. Without them the app could observe
 * one quiet day and never a quiet week, which is backwards for this audience.
 *
 * The owner approved both in a form the inversion cannot use. Current run only, ending with
 * the last day the window describes. No longest, no best, no past run. Capped at
 * [HistoryFacts.MAX_RUN_DAYS], so a value at the cap says at least that many and nothing
 * more. No per day series behind either one and none exposed.
 *
 * ## What this file does
 *
 * **The exception is a shape, and a shape can be widened by a rule that looks reasonable.**
 * Four things below are checked mechanically rather than left to review, because each of
 * them is the thing a careful person would do next.
 *
 * 1. [undeclaredReaders]. Every criterion that reads a run is found by **probing**, not by
 *    reading its name, and the set found has to equal [PERMITTED]. A new reader fails the
 *    build with the reasoning attached rather than passing because its id looked ordinary.
 * 2. [firesOnAZeroRun]. A reader must be false when its run is zero. This is the whole of
 *    the direction rule and it is exact: any criterion that fires on a **short or absent**
 *    run is reading the fact as evidence that something has been kept up, because a short
 *    run bottoms out at zero. `currentQuietRunDays == 0` says only that yesterday had
 *    something in it; a rule built on it is reading a run of activity out of a fact that
 *    counts the lack of one, which is the inversion the ban is about.
 * 3. [distinguishesTheCap]. A reader must give the same answer everywhere from
 *    [SATURATION_FLOOR] to the cap. Every stage boundary the corpus draws on either run is
 *    at four days or fewer, so a criterion that changed its mind up there would be treating
 *    a capped value as an exact count, and at the cap the value means at least thirty and
 *    nothing more.
 * 4. [runsWithoutASubject]. A rule reading [RunFact.SINGLE_AREA] must fail when the run
 *    belongs to an area other than its subject. A length with no subject is `four days
 *    running` about nobody, which is a streak sentence with the area filed off, and it is
 *    the one place these two facts differ: a single area run is also, unavoidably, a run of
 *    days that had activity in them. Pairing it to its own area is what keeps it a claim
 *    about narrowing rather than about keeping something up.
 *
 * Two further checks need types this package does not import and live in the test beside
 * this one, with their reasoning in [NEVER_RENDERED] and [NO_PER_DAY_SERIES].
 *
 * ## What none of this proves
 *
 * Running the extractor once per day for a month and differencing the results recovers a
 * per day series from any of these facts. That is true, it is what the simulator does, and
 * it is **not** something these two facts introduced: the same sequence recovers the same
 * series from `WindowFacts.totalEvents`, which has been there since phase 1. The ban has
 * never been about what a log can answer. It is about what layer one hands to a rule, and
 * that is what is checked here.
 */
internal object StreakExceptionAudit {

    /** One finding, with enough in it to go and look. Same shape as [CatalogIntegrity.Finding]. */
    data class Finding(val check: String, val detail: String)

    /** The two facts the exception covers. */
    enum class RunFact(val field: String, val counts: String) {
        QUIET(
            "HistoryFacts.currentQuietRunDays",
            "local days with no user activity at all, counted back from the window",
        ),
        SINGLE_AREA(
            "HistoryFacts.currentSingleAreaRunDays",
            "local days on which one area held every event, counted back from the window",
        ),
    }

    /**
     * A fact set the audit can vary one run at a time.
     *
     * Everything else on the fact set is held constant, which is what makes a criterion
     * whose answer moves a criterion that read the run. The probe has to carry **two**
     * areas, both with events in the window, or [runsWithoutASubject] has nothing to set
     * one against and [undeclaredReaders] cannot reach an area subject rule at all.
     */
    interface Probe {

        /** At least two, all live in the fact set, all with events in the window. */
        val areaIds: List<String>

        /** The same facts every time but for the three values named. */
        fun factsWith(quietRunDays: Int, singleAreaRunDays: Int, runAreaId: String?): FactSet
    }

    /**
     * One criterion permitted to read a run length, and the absence its family claims.
     *
     * The claim is here rather than in a comment because the failure message prints it. A
     * person adding a fourth entry has to be able to write this sentence about their own
     * criterion, and if they cannot, the criterion is not reading an absence.
     */
    data class PermittedReader(
        val criterionId: String,
        val fact: RunFact,
        val family: FamilyKey,
        val claims: String,
    )

    /**
     * Every criterion allowed to read a run length. Three, in two families.
     *
     * There is deliberately no entry for a Report rule. A run ending with the window is a
     * fact about the last few days, and the Report speaks about a week that has closed; a
     * pattern family reading one would be describing the days since the week it names.
     */
    val PERMITTED: List<PermittedReader> = listOf(
        PermittedReader(
            "quietDay.run.2to3",
            RunFact.QUIET,
            "quietDay",
            "two or three days running in which nothing moved, which is what every stage 2 " +
                "line in CORPUS_1_PULSE.md says",
        ),
        PermittedReader(
            "quietDay.run.4plus",
            RunFact.QUIET,
            "quietDay",
            "four or more days running in which nothing moved, the stage 3 header word for word",
        ),
        PermittedReader(
            "concentration.run.4plus",
            RunFact.SINGLE_AREA,
            "concentration",
            "four or more days in which nothing outside one area moved, which is the second " +
                "branch of the stage 3 header and a claim about the other areas being still",
        ),
    )

    /**
     * The length above which no criterion may change its answer.
     *
     * Ten, because the highest boundary the corpus draws on either run is four, and the cap
     * is thirty. Anything that moved between here and the cap would be reading a value that
     * means `at least thirty` as though it meant `exactly thirty`.
     */
    const val SATURATION_FLOOR = 10

    /** The reasoning behind the rendering check, which lives in the test beside this file. */
    const val NEVER_RENDERED: String =
        "a measure's value moved when only a run fact moved, which means a run can reach a " +
            "sentence as a number. Every slot in the corpus is filled by a Measure and by " +
            "nothing else, so a measure that reads a run is the whole distance between a fact " +
            "that scopes an observation and a number on a screen. `Thirty days` printed from a " +
            "value that means `at least thirty` would be false as well as forbidden, and a " +
            "number a person can watch go up is the thing the ban in 1.1 is about"

    /** The reasoning behind the per day series check, which also lives in the test. */
    const val NO_PER_DAY_SERIES: String =
        "a fact set member holds one entry per day. A capped current run plus a per day " +
            "activity series is a streak: the series says which days had something in them and " +
            "the run says where the current stretch begins, and between them a rule could count " +
            "days in a row. Every series layer one hands out is weekly and at most twelve " +
            "entries long, so seven days collapse into one number and no day can be resolved " +
            "out of it. That is the property this checks and it is the property the exception " +
            "was granted under"

    // ------------------------------------------------------------------ the checks

    /**
     * Every check that needs only the rules and a probe.
     *
     * A list of rules rather than a [ClarityCatalog], so a test can hand this a doctored
     * list holding one deliberately wrong rule and watch each check fail on it. A check
     * whose failure branch has never run is a check nobody has verified, which is the
     * argument CLARITY_LOGIC_ENGINE.md 8 makes about the validator's own veto path.
     */
    fun checkAll(rules: List<ClarityRule>, probe: Probe): List<Finding> =
        undeclaredReaders(rules, probe) +
            firesOnAZeroRun(rules, probe) +
            distinguishesTheCap(rules, probe) +
            runsWithoutASubject(rules, probe)

    /**
     * Every criterion whose answer moves with [fact], found by varying it and nothing else.
     *
     * Names are not consulted. A criterion that reads a run and is called
     * `concentration.window.share` is found here exactly as one that announces itself,
     * which is the difference between an audit and a convention.
     */
    fun readersOf(fact: RunFact, rules: List<ClarityRule>, probe: Probe): List<Criterion> {
        val seen = LinkedHashMap<String, Criterion>()
        for (rule in rules) {
            for (criterion in rule.criteria) {
                if (criterion.id in seen) continue
                if (varies(criterion, rule, fact, probe)) seen[criterion.id] = criterion
            }
        }
        return seen.values.toList()
    }

    /** The set that reads a run has to be the set that was allowed to. */
    fun undeclaredReaders(rules: List<ClarityRule>, probe: Probe): List<Finding> =
        RunFact.entries.flatMap { fact ->
            val found = readersOf(fact, rules, probe).map { it.id }.toSet()
            val declared = PERMITTED.filter { it.fact == fact }.map { it.criterionId }.toSet()
            (found - declared).map { id ->
                Finding(
                    "a criterion reads a run that nothing declared it may read",
                    "$id reads ${fact.field}, which counts ${fact.counts}. It is the one " +
                        "exception to the streak ban in 1.1 and it is scoped by shape rather " +
                        "than by instruction, so a new reader is a decision and has to be " +
                        "written down in StreakExceptionAudit.PERMITTED with the absence it " +
                        "claims. If you cannot write that sentence about this criterion, it is " +
                        "not reading an absence and the rule needs a different fact",
                )
            } + PERMITTED.filter { it.fact == fact && it.criterionId !in found }.map { reader ->
                Finding(
                    "a declared reader reads nothing",
                    "${reader.criterionId} is recorded here as ${reader.family}'s reading of " +
                        "${fact.field}, claiming ${reader.claims}. Its answer does not move " +
                        "anywhere between zero and the cap, so either the criterion changed and " +
                        "this record is stale, or it never read the run at all",
                )
            }
        }

    /** A reader must be false at a run of zero. The direction rule, stated exactly. */
    fun firesOnAZeroRun(rules: List<ClarityRule>, probe: Probe): List<Finding> =
        RunFact.entries.flatMap { fact ->
            readersOf(fact, rules, probe).flatMap { criterion ->
                val rule = ruleHolding(criterion, rules) ?: return@flatMap emptyList<Finding>()
                if (!holdsAtLength(criterion, rule, fact, 0, probe)) return@flatMap emptyList<Finding>()
                listOf(
                    Finding(
                        "a criterion fires on a run of zero",
                        "${criterion.id} is true when ${fact.field} is zero, which is a rule " +
                            "reading a run of absence as evidence that something has been kept " +
                            "up. The two runs are the one exception to 1.1 and the exception " +
                            "rests entirely on their counting absence: a run may be long and a " +
                            "rule may say so, and a rule may never fire because a run is short. " +
                            "A zero quiet run says only that yesterday had something in it, and " +
                            "a family built on that is a streak family with a different name",
                    ),
                )
            }
        }

    /** A reader must not change its answer anywhere between [SATURATION_FLOOR] and the cap. */
    fun distinguishesTheCap(rules: List<ClarityRule>, probe: Probe): List<Finding> =
        RunFact.entries.flatMap { fact ->
            readersOf(fact, rules, probe).flatMap { criterion ->
                val rule = ruleHolding(criterion, rules) ?: return@flatMap emptyList<Finding>()
                val answers = (SATURATION_FLOOR..HistoryFacts.MAX_RUN_DAYS)
                    .map { holdsAtLength(criterion, rule, fact, it, probe) }
                if (answers.distinct().size <= 1) return@flatMap emptyList<Finding>()
                listOf(
                    Finding(
                        "a criterion reads the capped end of a run",
                        "${criterion.id} changes its answer somewhere between $SATURATION_FLOOR " +
                            "and ${HistoryFacts.MAX_RUN_DAYS} days of ${fact.field}. That value " +
                            "is capped, so at the cap it means at least " +
                            "${HistoryFacts.MAX_RUN_DAYS} and nothing more, and a criterion that " +
                            "separates thirty from twenty nine is treating it as an exact count. " +
                            "The cap exists so neither run can become a record somebody protects; " +
                            "a threshold up there gives it one",
                    ),
                )
            }
        }

    /**
     * A rule reading the single area run must fail when the run belongs to another area.
     *
     * The check is over the whole rule rather than one criterion, because the pairing is
     * allowed to live in a second criterion, which is where `concentration` puts it.
     */
    fun runsWithoutASubject(rules: List<ClarityRule>, probe: Probe): List<Finding> {
        val fact = RunFact.SINGLE_AREA
        val readerIds = readersOf(fact, rules, probe).map { it.id }.toSet()
        val other = probe.areaIds.getOrNull(1)
            ?: return listOf(Finding("the probe carries one area", "runsWithoutASubject cannot be performed"))
        val subjectId = probe.areaIds.first()
        val subject = Subject(subjectId, SubjectKind.AREA)
        val length = HistoryFacts.MAX_RUN_DAYS
        return rules.filter { rule -> rule.criteria.any { it.id in readerIds } }.mapNotNull { rule ->
            val whenItsOwn = qualifies(rule, subject, probe.factsWith(length, length, subjectId))
            val whenAnothers = qualifies(rule, subject, probe.factsWith(length, length, other))
            when {
                !whenItsOwn -> Finding(
                    "a run rule the probe could not fire",
                    "${rule.key} reads ${fact.field} and did not qualify on a fact set built to " +
                        "make it qualify, so the check that it names the run's own area could " +
                        "not be performed. A check that cannot fail is not a check",
                )
                whenAnothers -> Finding(
                    "a run rule that would name the wrong area",
                    "${rule.key} qualifies for one area while ${fact.field} belongs to another. " +
                        "The length on its own is a claim with no subject, and paired with " +
                        "whichever area happened to lead the window it eventually prints `has " +
                        "held everything for four days` about an area that held nothing of the " +
                        "kind. It is also the point where this fact stops being a run of absence: " +
                        "a single area run is unavoidably a run of days that had activity in " +
                        "them, and naming the area it narrowed onto is what keeps the sentence " +
                        "about narrowing rather than about keeping something up. Require " +
                        "currentSingleAreaRunAreaId to be the subject",
                )
                else -> null
            }
        }
    }

    // ------------------------------------------------------------------ probing

    /** The rule a criterion belongs to, so the criterion can be evaluated against its subjects. */
    private fun ruleHolding(criterion: Criterion, rules: List<ClarityRule>): ClarityRule? =
        rules.firstOrNull { rule -> rule.criteria.any { it.id == criterion.id } }

    /** True when [criterion] answers differently at some run length, for some subject. */
    private fun varies(criterion: Criterion, rule: ClarityRule, fact: RunFact, probe: Probe): Boolean {
        val answers = LENGTHS.map { holdsAtLength(criterion, rule, fact, it, probe) }
        return answers.distinct().size > 1
    }

    /**
     * [criterion] against a fact set holding [fact] at [length], true for any subject.
     *
     * Any subject rather than every subject: a criterion that reads the run for one area
     * and not another has still read it, and a check that required agreement across
     * subjects would miss exactly that.
     */
    private fun holdsAtLength(
        criterion: Criterion,
        rule: ClarityRule,
        fact: RunFact,
        length: Int,
        probe: Probe,
    ): Boolean {
        val quiet = if (fact == RunFact.QUIET) length else 0
        val single = if (fact == RunFact.SINGLE_AREA) length else 0
        val facts = probe.factsWith(quiet, single, probe.areaIds.first())
        return subjectsOf(rule, facts).any { criterion.test(facts, it) }
    }

    /** Every criterion of [rule] holds, for [subject]. */
    private fun qualifies(rule: ClarityRule, subject: Subject?, facts: FactSet): Boolean =
        rule.criteria.all { it.test(facts, subject) }

    /** The rule's own subjects on this fact set, and null, so a subjectless rule is reached too. */
    private fun subjectsOf(rule: ClarityRule, facts: FactSet): List<Subject?> =
        (rule.subject.select(facts) + listOf<Subject?>(null)).distinct()

    /** Zero, every rung the corpus draws, and the cap. */
    private val LENGTHS: List<Int> = (0..HistoryFacts.MAX_RUN_DAYS).toList()
}
