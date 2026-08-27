package com.kamsiob.claritynow.domain.engine.catalog

import com.kamsiob.claritynow.domain.engine.FactRef

/**
 * Every slot the corpus uses, and the fact that produces it.
 * CLARITY_LOGIC_ENGINE.md 7.2 and 8, and `MASTER_BUILD_PROMPT.md` 9 and 11.4.
 *
 * **This is the table that makes validator check 3 possible.** Every `Count`, `Percent`
 * and `Days` slot has to carry a [FactRef], and the validator re-reads that fact and
 * compares. A number without one is a veto. So the mapping from a slot marker to the fact
 * behind it cannot live in the realizer's control flow, where it would be a series of
 * decisions nobody could enumerate; it has to be a table something can walk.
 *
 * **A slot with no entry here is a corpus edit nobody wired up**, and
 * [CatalogIntegrity.undeclaredSlots] finds it. That is the failure this table exists to
 * catch: a new marker in a new line, rendering as literal `{whatever}` on a screen, or
 * silently dropping the line at slot completeness.
 *
 * The three name slots carry no [FactRef] because they are not numbers. They are snapshot
 * strings, and snapshot usage is enforced structurally rather than by comparison: the
 * realizer receives only the `FactSet`, whose name fields are snapshots by construction,
 * and has no access to live entity tables at all. Validator check 5.
 */
internal object SlotProduction {

    /** How a slot is filled, and whether the value is a number the validator must re-read. */
    data class Source(
        val slot: SlotKey,
        val factRef: FactRef?,
        val describe: String,
        val availability: Availability,
    )

    /**
     * When a slot can be filled.
     *
     * [ALWAYS] holds for any fact set a rule could qualify on. [SUBJECT] needs the rule to
     * carry a subject of the right kind, or a fact set with a dominant area or an active
     * item to stand in for one. [CONDITIONAL] needs a specific fact to be non null, and
     * the realizer drops the candidate and takes the next ranked selection when it is not.
     */
    enum class Availability { ALWAYS, SUBJECT, CONDITIONAL }

    val SOURCES: List<Source> = listOf(
        Source("n", FactRef("window", "count"), "the family's primary count", Availability.ALWAYS),
        Source("m", FactRef("window", "count"), "the family's second count", Availability.ALWAYS),
        Source("k", FactRef("window", "count"), "the family's third count", Availability.ALWAYS),
        Source("pct", FactRef("rollup", "dominantShare"), "a share, rendered as `78 percent`", Availability.ALWAYS),
        Source("otherPct", FactRef("rollup", "dominantShare"), "a second share, from an earlier week or a second area", Availability.CONDITIONAL),
        Source("areaCount", FactRef("rollup", "areasWithEvents"), "how many areas moved", Availability.ALWAYS),
        Source("dayCount", FactRef("window", "activeDays"), "how many days had something in them", Availability.ALWAYS),
        Source("minutes", FactRef("window", "focusMinutesTotal"), "focused minutes in the window", Availability.CONDITIONAL),
        Source("sessions", FactRef("window", "focusCompleted"), "finished focus sessions in the window", Availability.CONDITIONAL),
        Source("medianDays", FactRef("items", "medianDaysToComplete"), "the median days to complete, null under three completions", Availability.CONDITIONAL),
        Source("priorCount", FactRef("pulse", "recentAnswers"), "how many times a stored label was given", Availability.CONDITIONAL),
        Source("ageDays", FactRef("items", "activeByArea.ageDays"), "an age, rendered as `yesterday` or `three weeks`", Availability.SUBJECT),
        Source("areaName", null, "the area name snapshot carried on the fact set", Availability.SUBJECT),
        Source("otherArea", null, "a second area's name snapshot", Availability.CONDITIONAL),
        Source("thirdArea", null, "a third area's name snapshot", Availability.CONDITIONAL),
        Source("itemTitle", null, "the active item's title snapshot", Availability.SUBJECT),
        Source("priorLabel", null, "the stored responseLabel from a PULSE_ANSWERED event, quoted verbatim", Availability.CONDITIONAL),
        Source("dayName", FactRef("window", "busiestDayKey"), "the busiest day of the window, as a name", Availability.CONDITIONAL),
        Source("sinceRef", FactRef("history", "personalBestWeekKey"), "a month name, rendered as `since March`", Availability.CONDITIONAL),
        Source("weekRef", FactRef("window", "startInstant"), "the week's start, as a month and day", Availability.ALWAYS),
    )

    private val bySlot: Map<SlotKey, Source> = SOURCES.associateBy { it.slot }

    /** The source for [slot], or null when the corpus uses a marker nothing produces. */
    fun sourceFor(slot: SlotKey): Source? = bySlot[slot]

    /** Every declared slot key. */
    val DECLARED: Set<SlotKey> = bySlot.keys

    /**
     * Slots that render a number and therefore need a [FactRef] on the way to the
     * validator. `pct` is here even though it renders with the word `percent` rather than
     * a sign, because 78 is still a number that came from a query.
     */
    val NUMERIC: Set<SlotKey> = SOURCES.filter { it.factRef != null }.map { it.slot }.toSet()
}
