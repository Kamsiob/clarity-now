package com.kamsiob.claritynow.domain.engine

/**
 * Everything layer one extracted, for one window, in one pass.
 * CLARITY_LOGIC_ENGINE.md 3.
 *
 * **Fully populated at construction. Nothing here is lazy and nothing here may
 * become lazy.** The reason is not performance, it is truth: a fact computed when
 * the validator asks for it was computed against whatever the log looked like at
 * that moment, and the rule that fired was answered from the log as it looked
 * earlier. The two could disagree by one completion and the sentence on the screen
 * would carry a number nothing in the app could reproduce. Every value below is
 * therefore a plain field, filled by `FactExtractor` before anything reads it.
 *
 * **Every number in here came from `domain.query.TrailQueries`.** No second path
 * exists to a displayed number, per MASTER_BUILD_PROMPT 9. Where a fact is a
 * comparison, a share or a bucketing rather than a count, the counts it is built
 * from still came from the facade and the arithmetic is stated in the field's own
 * documentation.
 *
 * **Every name in here is a snapshot.** `nameSnapshot`, `titleSnapshot` and
 * `areaNameSnapshot` were resolved by folding the log to the instant being
 * described. Nothing downstream is given a live entity table, which is how
 * CLARITY_LOGIC_ENGINE.md 8 check 5 is enforced structurally rather than by
 * checking: the realizer cannot name a stale name because it has no way to reach
 * one.
 *
 * The map is keyed by area id and holds **only areas that are live at the window
 * end**. Archived and tombstoned areas are absent entirely, which is prohibition 3
 * of 1.1 turned into a shape rather than a rule somebody has to remember.
 */
data class FactSet(
    val window: WindowFacts,
    val areas: Map<AreaId, AreaFacts>,
    val rollup: RollupFacts,
    val items: ItemFacts,
    val history: HistoryFacts,
    val pulse: PulseFacts,
    val cues: CueFacts,
)

/**
 * The window itself, counted. CLARITY_LOGIC_ENGINE.md 3.1.
 *
 * The window is half open, `[startInstant, endInstant)`, matching every bound in
 * `domain.query`. Two adjacent windows therefore share no event and a day boundary
 * belongs to exactly one of them.
 *
 * **[netFlow] is completions minus additions and the Pulse accumulation family
 * escalates on the opposite sign.** 3.1 declares this convention and
 * `CORPUS_1_PULSE.md` family 3 declares the other, so a rule wanting the intake gap
 * negates this rather than reading a second field. One signed number with one stated
 * direction is the only arrangement in which nobody has to guess.
 */
data class WindowFacts(
    val startInstant: Long,
    val endInstant: Long,
    /** Local calendar days the window touches. One for a single day window. */
    val dayCount: Int,
    val totalEvents: Int,
    val completions: Int,
    val additions: Int,
    val promotions: Int,
    val swaps: Int,
    /** Items tombstoned in the window. Areas deleted are not counted here. */
    val deletions: Int,
    val focusStarted: Int,
    val focusCompleted: Int,
    /**
     * Sessions the person ended before the timer ran out.
     *
     * Sessions a person ended before the timer did.
     *
     * `CLARITY_LOGIC_ENGINE.md` 3.1 declares this field as `focusAbandoned`, and the
     * name is deliberately not used. The owner renamed the event it counts in the
     * August 2026 schema window, and `DECISIONS.md` C6 records the reason: a name in a
     * document a second implementation is built from is an instruction about what the
     * concept means, and this one teaches the wrong thing. That reasoning applies with
     * more force here than it did to the event type, because rules are authored against
     * these field names and every rule author reads them.
     *
     * **Never infer this by subtraction.** `focusStarted` may exceed
     * `focusCompleted + focusEndedEarly`, because a killed process leaves a session
     * with no terminal event and that is a legal state rather than an abandonment.
     */
    val focusEndedEarly: Int,
    val focusSecondsTotal: Long,
    val focusMinutesTotal: Int,
    /**
     * Local days inside the window on which at least one focus session was started.
     *
     * Not [activeDays], which counts days with anything in them at all, and not
     * [focusStarted], which counts the sessions. Four sessions on one afternoon and
     * four sessions across four days are the same total and a different week, and
     * `focusInvestment` says so in two of its lines: *Focused time appeared on {n}
     * different days* and *You protected time on {n} of the seven days*.
     *
     * Read from the same lifetime per day focus map `CueFacts` is built from, so a day
     * means one thing to a cue and to a count.
     */
    val focusDays: Int,
    val activeDays: Int,
    /**
     * The local day carrying the most events, or null when the window is empty.
     *
     * **Ties resolve to the earliest day, and a rule that names this day must carry
     * its own floor.** Nothing in 3.1 makes this null on a tie the way
     * `RollupFacts.dominantAreaId` is, so the tie has to go somewhere, and the
     * earliest is the day the peak was first reached. A sentence of the shape
     * "Tuesday carried the week" is false on a three way tie whichever day is
     * chosen, so the family that says it requires [busiestDayCount] to be a real
     * share of [totalEvents], exactly as every share based rule carries an event
     * floor.
     */
    val busiestDayKey: String?,
    val busiestDayCount: Int,
    /**
     * Events by band of the local day. Every band is present, including zeros.
     *
     * Present rather than absent at zero because a share is a division and a missing
     * denominator term produces a percentage that does not reach a hundred with
     * nothing on screen to explain why.
     */
    val eventsByPartOfDay: Map<PartOfDay, Int>,
    /** Completions minus additions. See the class note on the sign. */
    val netFlow: Int,
)
