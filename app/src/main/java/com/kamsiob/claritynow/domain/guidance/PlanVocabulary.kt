package com.kamsiob.claritynow.domain.guidance

/**
 * What the app referring back to a plan of its own would leave behind.
 * CLARITY_LOGIC_ENGINE.md 10.6 and 12, and `MASTER_BUILD_PROMPT.md` 19.
 *
 * The non compliance test greps a simulated year for these shapes. It lives in main
 * source beside the mechanism it constrains, for the reason `ValidatorVocabulary` does:
 * a list of things the app must never say is part of the app, and a copy of it in a test
 * is a copy that goes stale the day somebody adds a sentence. `SimulationChecks` and
 * `GuidanceNonComplianceTest` both read this one list.
 *
 * ## The bare noun is not the test, and getting that wrong would break real language
 *
 * `CORPUS_1_PULSE.md` asks *Was that the plan?* and *Planned, or did it just flow?*, and
 * offers *A commitment / A trial* as a response pair. Those are questions about the
 * person's own day in ordinary English and are exactly the kind of line 6.1 asks for.
 * Banning the word would delete twenty three approved lines and prove nothing.
 *
 * What section 12 forbids is narrower and worse: the app **attributing a past commitment
 * to the person**. Every pattern below is second person plus a prior undertaking, which
 * is the shape a follow through mechanism produces when it leaks.
 *
 * `You said {priorLabel}` is deliberately absent. That is `selfReportVsData`, the
 * flagship family of 2.6, quoting an answer the person actually gave against what the
 * data shows. It is a self report set beside a fact, not a promise recalled, and
 * validator check 6 already guarantees the quote is exact.
 */
object PlanVocabulary {

    /** Each pattern, with what a sentence matching it would be referencing. */
    val FORBIDDEN: List<Pair<Regex, String>> = listOf(
        Regex("""\byou(?:r)?\s+plan(?:s|ned|ning)?\b""", RegexOption.IGNORE_CASE) to "a plan of the person's",
        Regex("""\bthe\s+plan\s+you\b""", RegexOption.IGNORE_CASE) to "a plan the person was given",
        Regex("""\byou\s+(?:agreed|committed|promised|intended|meant)\b""", RegexOption.IGNORE_CASE)
            to "an undertaking attributed to the person",
        Regex("""\byou\s+were\s+going\s+to\b""", RegexOption.IGNORE_CASE) to "an unmet intention",
        Regex("""\byou\s+accepted\b""", RegexOption.IGNORE_CASE) to "an acceptance recalled",
        Regex("""\byour\s+commitment\b""", RegexOption.IGNORE_CASE) to "a commitment",
        Regex("""\bcommitted\s+to\b""", RegexOption.IGNORE_CASE) to "a commitment",
        Regex("""\bfollow\s*[- ]?\s*through\b""", RegexOption.IGNORE_CASE) to "follow through",
        Regex("""\bset\s+out\s+to\b""", RegexOption.IGNORE_CASE) to "an unmet intention",
    )

    /**
     * What [text] references, or null when it references none of it.
     *
     * The first match wins, because the caller reports one reason per sentence and the
     * sentence is already a failure by the time a second would be found.
     */
    fun referenceIn(text: String): String? =
        FORBIDDEN.firstOrNull { it.first.containsMatchIn(text) }?.second
}
