package com.kamsiob.claritynow.domain.engine

/**
 * One Pulse the person answered. CLARITY_LOGIC_ENGINE.md 3.1.
 *
 * [responseLabel] is the string stored verbatim on the `PULSE_ANSWERED` event, not a
 * label looked up in the current app version. A callback quotes what the person
 * actually saw, and validator check 6 compares the quoted text against this field
 * exactly. Reword a response option in a later release and every old callback still
 * quotes the old wording, which is the only honest arrangement.
 *
 * [dateKey] and [family] come from the `PULSE_GENERATED` this answers, joined on
 * `pulseId`. [subjectId] is denormalized onto the answer event itself and is read
 * from there.
 */
data class AnsweredPulse(
    val dateKey: String,
    val family: FamilyKey,
    val subjectId: String?,
    val responseKey: String,
    val responseLabel: String,
    val isPositive: Boolean,
)

/**
 * What the person has said back. CLARITY_LOGIC_ENGINE.md 3.1.
 *
 * **`isPositive` is the only interpretation the app ever makes of an answer**, per
 * 6.1. There is no scoring, no aggregate shown to the person, and no family that
 * treats a flagged answer as a problem. It exists so a callback can set what
 * somebody said against what happened, and for nothing else.
 *
 * [recentAnswers] is lifetime rather than window scoped, because a
 * `CallbackRequirement` declares its own `withinDays` and resolving it against a
 * window would silently drop callbacks the rule was authored to make.
 */
data class PulseFacts(
    val answeredLifetime: Int,
    val answeredInWindow: Int,
    val positiveInWindow: Int,
    /** Answers in the window that were not the positive option. Never shown as a total. */
    val flaggedInWindow: Int,
    /**
     * The family of the most recent Pulse generated before the window end, or null.
     *
     * Step 4 of selection drops every candidate whose family equals this, which is
     * the whole no repeat rule: yesterday's family cannot be today's.
     */
    val lastGeneratedFamily: FamilyKey?,
    val lastGeneratedDateKey: String?,
    /** Newest first, up to 30. Newest is the latest in the log's total order. */
    val recentAnswers: List<AnsweredPulse>,
    /** [recentAnswers] grouped by family, each list still newest first. */
    val answersByFamily: Map<FamilyKey, List<AnsweredPulse>>,
)
