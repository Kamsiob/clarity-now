package com.kamsiob.claritynow.domain.engine.select

import com.kamsiob.claritynow.domain.engine.FactSet
import com.kamsiob.claritynow.domain.engine.FamilyKey
import com.kamsiob.claritynow.domain.engine.Precedent
import com.kamsiob.claritynow.domain.engine.catalog.FamiliesAwaitingLanguage
import com.kamsiob.claritynow.domain.engine.catalog.Purpose
import com.kamsiob.claritynow.domain.engine.catalog.Subject
import com.kamsiob.claritynow.domain.engine.catalog.SubjectKind

/**
 * The two family scope gates of `MASTER_BUILD_PROMPT.md` 14b, applied at step 1b of
 * selection. 14b.4 and 14b.9.
 *
 * ## Why these are one mechanism and not two
 *
 * Both are the same sentence: **this family may not fire at all right now.** 14b.4 says a
 * decline, neglect or gap family is "unavailable to selection and the next ranked
 * candidate is taken instead" for a week after a return; 14b.9 says the engine asks
 * "before any decline, neglect or fading family may fire" whether the shape has happened
 * before. Neither is a condition on a week, which is what a criterion is. They are
 * conditions on whether a family gets a turn.
 *
 * **Neither could be a criterion, and the reason is arithmetic rather than taste.**
 * `ClarityRule.specificity` is `criteria.size` and nothing else, so a criterion added to
 * `quietWeek` would make `quietWeek` outrank a rule that genuinely required more. 14b.4's
 * test is `!isJustBackFromAbsence`, which is true on all but seven days of a person's
 * life: section 4 forbids padding a rule with a trivially true criterion for exactly that
 * reason, and it would be padding on the families the section wants demoted rather than
 * promoted. A filter removes and never reorders, which is what both sections describe.
 *
 * ## What each one is
 *
 * [WITHHELD_ON_RE_ENTRY] is 14b.4: for seven days from a return, on the Report, the
 * Momentum headline and the Areas banner, nothing may say the week was quiet, that
 * something has fallen away, that an area has been neglected, or that a gap ended. **The
 * Pulse is deliberately absent**, because 14b.4 gives the Pulse a different and older rule
 * that lives above layer one: it declines to run the engine at all for two days rather
 * than withholding some of its families, per `PulseGeneration`.
 *
 * [PRECEDENT_GATED] is 14b.9: a fall this deep and this long that has happened to this
 * subject before is a rhythm rather than a decline, so the family does not fire and
 * `FamiliesAwaitingLanguage.FAMILIAR_DIP` is what speaks in its place. It has no purpose
 * scope, because 14b.9 has none: the claim is false on every surface it could appear on.
 *
 * ## Where they overlap, and why the overlap is safe
 *
 * A person returning from a fortnight is exactly the person a decline family fires on, so
 * the two gates overlap on nearly every family in the smaller map. **Re-entry wins**,
 * because it is unconditional and it runs first, and it does not matter that it does:
 * both gates remove the same selection, and the only thing the precedent gate adds is
 * permission for the rhythm family to speak instead. That family is in
 * [WITHHELD_ON_RE_ENTRY] too, so the two cannot fight. A sentence about a familiar stretch
 * of low weeks, said on the first report after a fortnight away, is a measurement of the
 * absence with a kinder vocabulary, which is the thing 14b.4 exists to prevent.
 */
internal object FamilyAvailability {

    /** Which of the three precedent facts answers for a family, per its own claim. */
    enum class DipSubject {

        /** `HistoryFacts.activityDipPrecedent`. The person's weeks as a whole. */
        ACTIVITY,

        /** `HistoryFacts.focusDipPrecedent`. Focus sessions started per week. */
        FOCUS,

        /** `AreaFacts.dipPrecedent`, for the subject area. */
        AREA,
    }

    /**
     * The surfaces 14b.4 names, and only those.
     *
     * The Report is three purposes rather than one, so a headline is withheld alongside
     * the observation it would have framed. Leaving the headline out would let
     * `report.headline.quietWeek` set the frame for a page whose quiet week observation
     * had just been withheld, which is the same sentence in the largest type in the app.
     */
    val RE_ENTRY_PURPOSES: Set<Purpose> = setOf(
        Purpose.REPORT_HEADLINE,
        Purpose.REPORT_OBSERVATION,
        Purpose.REPORT_PATTERN,
        Purpose.MOMENTUM_HEADLINE,
        Purpose.AREAS_BANNER,
    )

    /**
     * Every family withheld for the week after a return, and what makes it one. 14b.4.
     *
     * **The set is derived from a stated rule rather than from taste**, and the rule is:
     * a family belongs here when its trigger is a fall, a silence, or the gap a return
     * came back from. Those are the three shapes an absence creates in the data, so they
     * are the three shapes that would greet a returning person with a reading of their own
     * absence.
     *
     * The gap families are the half a reader will not expect, and they are the reason this
     * map is not simply the decline list. `mo.come.01` is `Back after {ageDays}` and
     * `ob.rev.l01` is `{areaName} moved again after {ageDays} of nothing`. Both are
     * warm, both are true, and both state the length of the absence in days on the first
     * screen back, which 14b.4 forbids in as many words: not in days, not in weeks, not as
     * a date.
     *
     * **What is deliberately absent.** `queuePressure` and `growingQueues` read a queue
     * that grew, and a queue does not grow while nobody is there: nothing is added either,
     * so both boundaries are equal and neither can fire from an absence. `focusAbandonment`
     * and `abandonmentPattern` need sessions inside the window, so they describe what
     * somebody did after coming back rather than the fact that they were gone.
     * `intakeVsOutput` needs both an intake and an output. None of the four is a decline, a
     * neglect or a gap in 14b.4's sense, and withholding a family an absence cannot trigger
     * costs a true observation for nothing.
     */
    val WITHHELD_ON_RE_ENTRY: Map<FamilyKey, String> = mapOf(
        "quietWeek" to "the week was quiet, which is what a week away looks like",
        "quietStretch" to "it counts the active days of a fortnight the person was not here for",
        "weekQuiet" to "it is the banner on the first screen back, and it says the week has been still",
        "decliningActivity" to "activity fell across the weeks the person was away",
        "narrowingFocus" to "fewer areas moved in each of those weeks because nobody was moving them",
        "focusHabitFading" to "focus fell away across the absence",
        "hardStretch" to "its trigger is three quiet weeks or a four week decline, which an absence supplies",
        "neglectedArea" to "every area is silent after a fortnight away",
        "areaGoneQuiet" to "the same silence, read over three weeks",
        "comeback" to "it names the gap the return came back from",
        "areaRevival" to "it names the days an area was still before it moved again",
        "comebackPattern" to "it counts the gaps an area has come back from",
        FamiliesAwaitingLanguage.FAMILIAR_DIP to
            "a familiar stretch of low weeks is the absence in a kinder vocabulary",
    )

    /**
     * The families 14b.9 gates, and which precedent fact answers for each.
     *
     * **The mapping is the one the facts phase declared**, in `AreaFacts.dipPrecedent`,
     * `HistoryFacts.activityDipPrecedent` and `focusDipPrecedent`, each of which names the
     * families it was extracted for. It is not widened here, and the discipline behind that
     * is worth stating: **a family is gated only where a precedent fact measures the same
     * quantity its claim is about.**
     *
     * `narrowingFocus` is the family that tests the discipline. It is a decline by any
     * reading, and its claim is about the number of areas that moved, which no precedent
     * fact measures. Gating it on the activity precedent would suppress a claim about
     * breadth on the strength of a finding about volume, and the two come apart on exactly
     * the person this section protects: somebody whose cycle narrows to one area without
     * their total activity falling has a real narrowing and no precedent for it in any
     * fact this app holds. It is withheld on re-entry, where the question is only whether
     * an absence produced the shape, and it is not gated here.
     */
    val PRECEDENT_GATED: Map<FamilyKey, DipSubject> = mapOf(
        "decliningActivity" to DipSubject.ACTIVITY,
        "quietWeek" to DipSubject.ACTIVITY,
        "hardStretch" to DipSubject.ACTIVITY,
        "focusHabitFading" to DipSubject.FOCUS,
        "neglectedArea" to DipSubject.AREA,
        "areaGoneQuiet" to DipSubject.AREA,
    )

    /**
     * The precedent values that close the gate. `PRESENT` alone, and this is the one
     * reading in 14b.9 that had to be settled rather than transcribed.
     *
     * ## The two readings
     *
     * 14b.9 and `CLARITY_LOGIC_ENGINE.md` 3.1 both say that `NONE` is the permission and
     * `PRESENT` is the veto, and that `INSUFFICIENT` is neither, "so a person with too
     * short a history gets neither sentence". Read strictly, the second half asks the
     * decline families to require `NONE`, which would close the gate on `INSUFFICIENT` and
     * on `NOT_IN_A_DIP` as well.
     *
     * ## Why the veto is `PRESENT` alone
     *
     * **`NOT_IN_A_DIP` is the argument, and it is not about new users.** This fact's notion
     * of being low is a week under three quarters of the subject's own normal, and that is
     * not the same question any decline family asks. `decliningActivity` reads a run of
     * three falling weeks, which can end on a perfectly ordinary week; `neglectedArea`
     * reads a gap in days, which can open inside a week the area was busy at the start of.
     * Requiring `NONE` would silence a true observation every time the two definitions came
     * apart, and the silence would be invisible: nothing on the screen looks wrong when a
     * sentence is missing.
     *
     * **`INSUFFICIENT` is the narrower question and it went the same way.** Closing on it
     * would withhold every decline observation from every install between its fourth week,
     * where the families first have a series to read, and its twelfth, where a precedent
     * becomes answerable. That is eight weeks of an app that has noticed something and
     * decided not to say it, on exactly the people 14b.10 says are deciding whether to keep
     * it. The instruction this phase was given names that outcome and rejects it.
     *
     * **It is one line and it is meant to be.** Adding `Precedent.INSUFFICIENT` here is the
     * whole of the other reading, and both persona tests would then measure it. The choice
     * is the owner's and the cost of changing it should be a word.
     */
    val CLOSES_THE_GATE: Set<Precedent> = setOf(Precedent.PRESENT)

    /**
     * Why [selection] may not fire at all in this window, or null when it may.
     *
     * Returns a reason rather than a boolean so the simulator dump and the debug log can
     * print which of the two gates closed and on what, which is the difference between a
     * silence somebody can account for and one nobody can.
     */
    fun unavailable(selection: Selection, facts: FactSet): String? {
        val family = selection.rule.family
        if (selection.purpose in RE_ENTRY_PURPOSES && facts.history.isJustBackFromAbsence) {
            WITHHELD_ON_RE_ENTRY[family]?.let { why ->
                return "$family is withheld for the week after a return, per 14b.4: $why"
            }
        }
        val dip = PRECEDENT_GATED[family] ?: return null
        val precedent = precedentFor(dip, facts, selection.subject)
        return if (precedent in CLOSES_THE_GATE) {
            "$family is gated by 14b.9: this subject has been this low, this long, before, " +
                "so the shape is a rhythm rather than a decline"
        } else {
            null
        }
    }

    /**
     * The precedent [dip] reads, for [subject] where it needs one.
     *
     * An area the fact set does not carry answers [Precedent.INSUFFICIENT], which is the
     * safe value in both directions: it is neither the permission nor the veto, so a
     * subject nothing knows about is neither told it is declining nor told it has a
     * rhythm. A rule with an area subject cannot reach here on an absent area anyway,
     * because `FactSet.areas` is what its subject selector enumerates.
     */
    fun precedentFor(dip: DipSubject, facts: FactSet, subject: Subject?): Precedent = when (dip) {
        DipSubject.ACTIVITY -> facts.history.activityDipPrecedent
        DipSubject.FOCUS -> facts.history.focusDipPrecedent
        DipSubject.AREA ->
            if (subject == null || subject.kind != SubjectKind.AREA) {
                Precedent.INSUFFICIENT
            } else {
                facts.areas[subject.id]?.dipPrecedent ?: Precedent.INSUFFICIENT
            }
    }
}

