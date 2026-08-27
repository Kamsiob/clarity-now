package com.kamsiob.claritynow.domain.engine.validate

/**
 * The word level checks in CLARITY_LOGIC_ENGINE.md 8, checks 8 and 10, as patterns.
 *
 * ## Why a sentence is checked again at runtime when the corpus is checked at build time
 *
 * The build gate reads every `.md` in the repository, so no authored line can ship with a
 * dash, a character above ASCII or a spelling from the other side of the Atlantic. This
 * file is not a second copy of that gate for its own sake. It is the check that survives
 * the two things the gate cannot see: a sentence assembled at runtime from a frame and a
 * clause bench, per 7.7, and a corpus loaded from anywhere other than the file the gate
 * read. Section 8 requires the check on the rendered sentence, which is the only string a
 * person ever actually sees.
 *
 * ## The word list is written in halves on purpose
 *
 * [OTHER_SPELLINGS] holds words this project does not use. Writing them out would fail
 * the repository's own language gate on this very file, which reads every `.kt` line and
 * knows nothing about intent. `build.gradle.kts` solves the same problem by excluding
 * itself from its own scan; this file cannot be excluded, so each entry carries a [SPLIT]
 * marker that is removed when the patterns are built. The marker is a nuisance and it is
 * the smaller of the two nuisances.
 *
 * ## What is deliberately not banned
 *
 * The bare word `behind` is not banned. 11.3 is explicit that the ban targets the
 * evaluative sense, `falling behind`, and that the spatial sense is correct and common in
 * this app because a queue literally has things behind the active item. The two patterns
 * in [BLAME_CONSTRUCTIONS] are the ones 11.3 specifies, character for character.
 *
 * The word `failure` is not banned either. `A period like this is not a failure of the
 * system you set up` is an approved line in the hard stretch family, and a check that
 * vetoed it would silence the one family written for the worst weeks.
 */
internal object ValidatorVocabulary {

    /** Written as an escape so this file stays ASCII and passes the repository gate. */
    const val EM_DASH = '\u2014'

    /** Written as an escape for the same reason. */
    const val EN_DASH = '\u2013'

    /**
     * Single words no sentence about a person's own week may contain.
     * CLARITY_LOGIC_ENGINE.md 1.1 prohibition 8 and 11.3.
     *
     * `streak` is here twice over: 11.3 bans the word, and 3.1 removed the facts that
     * would let anyone write it truthfully. The word ban is the part that still matters
     * when somebody counts one out of a series by hand.
     */
    val BANNED_WORDS: List<Pair<Regex, String>> = listOf(
        Regex("""\bshould(n't|'ve|ve)?\b""", RegexOption.IGNORE_CASE) to "should",
        Regex("""\bmust(n't)?\b""", RegexOption.IGNORE_CASE) to "must",
        Regex("""\b(have|has|had)\s+to\b""", RegexOption.IGNORE_CASE) to "have to",
        Regex("""\bfailed\b""", RegexOption.IGNORE_CASE) to "failed",
        Regex("""\bstreaks?\b""", RegexOption.IGNORE_CASE) to "streak",
        Regex("""\bhurr(y|ies|ied|ying)\b""", RegexOption.IGNORE_CASE) to "hurry",
        Regex("""\blaz(y|ier|iest|ily)\b""", RegexOption.IGNORE_CASE) to "lazy",
    )

    /**
     * Phrases that assign blame, chase, or congratulate. 11.3.
     *
     * Congratulation is on this list for the same reason blame is. `Well done` claims the
     * authority to grade a week, which is the third directive in section 1 read from the
     * pleasant end.
     */
    val BANNED_PHRASES: List<Pair<Regex, String>> = listOf(
        Regex("""\bdon'?t\s+forget\b""", RegexOption.IGNORE_CASE) to "don't forget",
        Regex("""\byou\s+haven'?t\b""", RegexOption.IGNORE_CASE) to "you haven't",
        Regex("""\byou\s+have\s+not\b""", RegexOption.IGNORE_CASE) to "you have not",
        Regex("""\bmake\s+sure\b""", RegexOption.IGNORE_CASE) to "make sure",
        Regex("""\btry\s+to\b""", RegexOption.IGNORE_CASE) to "try to",
        Regex("""\bremember\s+to\b""", RegexOption.IGNORE_CASE) to "remember to",
        Regex("""\bkeep\s+it\s+up\b""", RegexOption.IGNORE_CASE) to "keep it up",
        Regex("""\bwell\s+done\b""", RegexOption.IGNORE_CASE) to "well done",
        Regex("""\bgreat\s+job\b""", RegexOption.IGNORE_CASE) to "great job",
    )

    /**
     * The evaluative sense of `behind`, and nothing else. 11.3 states both patterns and
     * this is a transcription of them.
     */
    val BLAME_CONSTRUCTIONS: List<Pair<Regex, String>> = listOf(
        Regex(
            """\b(?:fall(?:ing|s|en)?|get(?:ting)?|slip(?:ping)?|running|are|is|am|were|was)\s+behind\b""",
            RegexOption.IGNORE_CASE,
        ) to "the evaluative sense of behind",
        Regex("""\bbehind\s+(?:schedule|target|plan|where|the\s+curve)\b""", RegexOption.IGNORE_CASE)
            to "the evaluative sense of behind",
    )

    /**
     * The past participles of the actions a person performs in this app, as one alternation.
     *
     * Deliberately short. Every verb here is one whose deleted agent could only be the
     * person, which is the construction 7.4 forbids in the neutral agent register. A
     * participle whose absent agent is the app, or nobody, does not belong on this list
     * and adding one silences an approved line.
     */
    private const val ACTION_PARTICIPLES =
        "added|completed|finished|created|deleted|removed|promoted|swapped|moved|started|" +
            "abandoned|cleared|drained|archived|logged|recorded|tracked|touched|updated|" +
            "edited|opened|closed|queued|dropped|skipped|postponed|handled|marked|sorted|" +
            "scheduled|reordered|renamed|restored"

    /**
     * Check 10. A passive with the agent deleted, in a neutral agent line.
     * CLARITY_LOGIC_ENGINE.md 7.4 and 8.
     *
     * **This is the check most likely to be written too widely, and a check written too
     * widely here silences approved language.** 7.4 gives the constraint precisely: the
     * neutral agent register makes the fact the grammatical subject, `Nine things arrived.
     * Six left.`, and it is **not** passive voice and **not** agent deletion. `Nine things
     * were added by you` is banned outright, and so is the same sentence with the agent
     * dropped.
     *
     * So the pattern is a form of `be` followed by the past participle of an action **the
     * person performs in this app**, which is where the deleted agent would have gone. It
     * is not every participle. Three approved neutral agent lines read `{areaName} has
     * been still since {sinceRef}`, `The week has been quiet here` and `Nothing has been
     * lost. It is all still here.`, and a bare ban on `have been`, which section 8 lists
     * among its examples, would veto all three. The first two are a copula and an
     * adjective, which is not a passive at all. The third is a passive whose absent agent
     * is the app rather than the person, and the register exists to stop the app
     * attributing action to the person.
     */
    val AGENT_DELETED_PASSIVES: List<Pair<Regex, String>> = listOf(
        Regex(
            """\b(?:was|were|is|are|am|be|been|being)\s+(?:$ACTION_PARTICIPLES)\b""",
            RegexOption.IGNORE_CASE,
        ) to "a passive construction with the agent deleted",
        // The agent restored is worse, not better. 7.4 bans this form by name.
        Regex("""\bby\s+you\b""", RegexOption.IGNORE_CASE) to "the person restored as a by phrase",
    )

    /**
     * The marker that keeps a spelling this project does not use out of the raw text of
     * this file. Removed when the patterns are built.
     */
    private const val SPLIT = "|"

    /**
     * Stems that take a `z` here and an `s` elsewhere.
     *
     * Curated rather than a blanket rule, exactly as `build.gradle.kts` argues: a great
     * many `-ise` words are correct here too, and a gate that fails on correct copy is a
     * gate somebody eventually switches off.
     */
    private val Z_STEMS = listOf(
        "apologi", "authori", "categori", "critici", "customi", "digiti", "emphasi",
        "finali", "generali", "hospitali", "initiali", "maximi", "memori", "minimi",
        "normali", "optimi", "organi", "personali", "prioriti", "randomi", "reali",
        "recogni", "seriali", "speciali", "stabili", "standardi", "sterili", "summari",
        "synchroni", "utili", "visuali",
    )

    /** Stems that take `yze` here. The noun form ending in `ysis` is correct and is not matched. */
    private val YZE_STEMS = listOf("analys", "paralys", "catalys")

    /**
     * Everything that is not a regular family, written with [SPLIT] inside it.
     *
     * The list mirrors the one in `build.gradle.kts`, which is the authoritative gate for
     * the repository. Two lists is one more than anybody wants, and the alternative is a
     * validator that cannot check the rule section 8 says it must check, because a Gradle
     * build script is not on the application's classpath.
     */
    private val OTHER_SPELLINGS = listOf(
        "colo|ur" to "color",
        "behavio|ur" to "behavior",
        "favo|urite" to "favorite",
        "hono|ur" to "honor",
        "neighbo|ur" to "neighbor",
        "labo|ur" to "labor",
        "humo|ur" to "humor",
        "flavo|ur" to "flavor",
        "rumo|ur" to "rumor",
        "endeavo|ur" to "endeavor",
        "vapo|ur" to "vapor",
        "valo|ur" to "valor",
        "armo|ur" to "armor",
        "harbo|ur" to "harbor",
        "odo|ur" to "odor",
        "savio|ur" to "savior",
        "splendo|ur" to "splendor",
        "licen|ce" to "license",
        "defen|ce" to "defense",
        "offen|ce" to "offense",
        "preten|ce" to "pretense",
        "practi|se" to "practice",
        "cent|re" to "center",
        "theat|re" to "theater",
        "fib|re" to "fiber",
        "lit|re" to "liter",
        "calib|re" to "caliber",
        "somb|re" to "somber",
        "spect|re" to "specter",
        "lust|re" to "luster",
        "travel|ling" to "traveling",
        "travel|led" to "traveled",
        "cancel|ling" to "canceling",
        "cancel|led" to "canceled",
        "label|ling" to "labeling",
        "label|led" to "labeled",
        "model|ling" to "modeling",
        "model|led" to "modeled",
        "signal|ling" to "signaling",
        "signal|led" to "signaled",
        "level|led" to "leveled",
        "fuel|led" to "fueled",
        "marvel|lous" to "marvelous",
        "skil|ful" to "skillful",
        "wil|ful" to "willful",
        "fulfi|l" to "fulfi|ll",
        "enro|l" to "enro|ll",
        "instal|ment" to "installment",
        "judge|ment" to "judgment",
        "age|ing" to "aging",
        "cos|y" to "cozy",
        "learn|t" to "learned",
        "spel|t" to "spelled",
        "dream|t" to "dreamed",
        "program|me" to "program",
        "alumini|um" to "aluminum",
        "jewel|lery" to "jewelry",
        "store|y" to "story",
        "ty|re" to "tire",
        "ker|b" to "curb",
        "cheq|ue" to "check",
        "draug|ht" to "draft",
        "moul|d" to "mold",
        "smoul|der" to "smolder",
        "scep|tic" to "skeptic",
        "aero|plane" to "airplane",
        "plou|gh" to "plow",
        "gao|l" to "jail",
        "whil|st" to "while",
        "among|st" to "among",
    )

    /**
     * Every spelling pattern, built once.
     *
     * `grey` is deliberately absent, matching the repository gate: the design system uses
     * it, and both spellings are current here.
     */
    val OTHER_SPELLING_FORMS: List<Pair<Regex, String>> = buildList {
        Z_STEMS.forEach { stem ->
            add(Regex("""\b${stem}s(e|es|ed|ing|ation|ations)\b""", RegexOption.IGNORE_CASE) to "${stem}z...")
        }
        YZE_STEMS.forEach { stem ->
            add(Regex("""\b$stem(e|es|ed|ing)\b""", RegexOption.IGNORE_CASE) to "${stem.dropLast(1)}z...")
        }
        OTHER_SPELLINGS.forEach { (masked, preferred) ->
            // Both halves are unmasked. Two of the preferred spellings begin with the
            // one this project does not use, so writing them plainly would fail the
            // repository gate on this file the same way the pattern itself would.
            add(Regex("""\b${masked.replace(SPLIT, "")}""", RegexOption.IGNORE_CASE) to preferred.replace(SPLIT, ""))
        }
    }
}
