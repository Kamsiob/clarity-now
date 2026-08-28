package com.kamsiob.claritynow.domain.engine.corpus

import com.kamsiob.claritynow.domain.engine.FamilyKey
import com.kamsiob.claritynow.domain.engine.catalog.Purpose

/**
 * What the corpus looked like on the day the gates were written, so a gate can fail on
 * something new without failing on everything old.
 *
 * **This file is the honest answer to "which gates does the corpus already fail".** On the
 * day it was written the seven fast gates produced **259 findings** over 1,554 rendered
 * strings, and the slow render gate produced 82 more. The eighth fast gate, `unit`, was
 * added later by the binding pass and its one finding was fixed rather than recorded, so
 * its row is here to say what it found and not what it excuses:
 *
 * | gate | findings on the day it was written |
 * |---|---|
 * | fragment | 9 clauses shared by two families of one purpose |
 * | construction | 4 shapes past the cap of two families, two of them already recorded by the catalog |
 * | vocabulary | 0 |
 * | binding | 99 markers with no fact behind them, across 80 lines |
 * | unit | 1 marker in front of a unit noun its measure does not count, fixed rather than recorded |
 * | lengthBand | 74 of 108 measurable benches with one band over sixty percent |
 * | register | 63, across 53 benches with a thin or missing register |
 * | duplicate | 10 pairs of lines that read as one line |
 * | renders | 82 lines that no real moment could fill or that layer 5 always vetoed |
 *
 * Every one of them is recorded below and nothing else is, so the next finding fails the
 * build. An entry leaves this file only when the corpus stops producing the finding it
 * records, which has happened once and is argued where it happened.
 *
 * Every entry is a debt with a name on it, and phase 9 should leave this file shorter rather
 * than longer. Nothing is added here without a corpus edit being the alternative, which is
 * the rule `KnownCorpusViolations` states for the two checks it already grandfathers.
 *
 * ## Two kinds of entry, and why the difference matters
 *
 * A **bench** entry records the size of the bench on the day it was recorded, and the
 * exemption holds at that size and at no other. That is the whole mechanism, and it is what
 * makes this file expire instead of accumulating: phase 9 grows a hot bench from a dozen
 * lines to sixty, and on the thirteenth line the exemption is gone and the whole bench is
 * held to the rule. An author who grows a bench inherits its debt, which is right, because
 * they are the only person who will ever be in a position to pay it.
 *
 * **It used to hold at that size or smaller, and that was a hole rather than a generosity.**
 * A bench that lost a line was excused more deeply than it had been, so the suite was at its
 * weakest exactly when work had gone missing, and the day 336 uncommitted lines were
 * destroyed mid phase every one of these gates went green over the shortened corpus. An
 * entry here is one person's reading of one specific bench; a bench with lines cut out of it
 * is not that bench, and the exemption lapses in both directions now. `CorpusCensus` is the
 * other half, and it is the half that says which lines went.
 *
 * A **line** entry records specific keys, and never lapses. The instruction to phase 9 is
 * not to reword an approved line, so a collision between two lines that were both approved
 * before the gate existed cannot be fixed by somebody who is only allowed to add. One of
 * them is worth an editor's eye all the same and is called out where it is recorded.
 */
internal object CorpusGateBaseline {

    /** `purpose`, the shared clause, and the families that shared it when this was recorded. */
    data class RecordedFragment(val purpose: Purpose, val fragment: String, val families: Set<FamilyKey>)

    /**
     * Clauses in two families of one purpose today. See [CorpusGates.sharedFragments].
     *
     * Seven, and only the longest run of each collision is listed, so one shared clause is
     * one entry rather than one entry per window inside it. One of the seven is also
     * recorded in `KnownCorpusViolations` as a whole shared sentence; the other six are
     * clauses inside sentences that differ, which the production check cannot see.
     *
     * **Nine were recorded on the day this was written, and the ninth is deleted rather
     * than left standing.** It was `on {} of the seven days`, shared by `dayShape` and
     * `focusInvestment`, and the reach pass rewrote both lines for issue 57 so that no
     * digit slot sits beside a number word: `ob.day.l05` now reads `of the 7 days` and
     * `ob.focus.s3.l04` was restructured to `on {n} days out of 7`. The collision is gone,
     * so the entry matched nothing. This gate grandfathers by subset, so an entry that
     * matches nothing cannot fail and cannot lapse either, which makes it the one shape of
     * row that quietly costs the file its meaning: it records a debt that has been paid,
     * and left alone it would go on recording it for as long as the file exists.
     *
     * **The eighth left the same way and for the same defect.** It was `active {} of the
     * last fourteen days`, shared by `mo.steady.01` and `mo.quiet.01`, and `mo.steady` was
     * the half of it that was also wrong: `{dayCount}` renders 9 to 14 there and Momentum
     * renders ten and above as digits, so the line read `Active 12 of the last fourteen
     * days.` on five of the six values the slot can take. The nine `mo.steady` lines that
     * paired a rendered digit with the word `fourteen` now write `14`; `mo.quiet` keeps
     * `fourteen` because its own slot never reaches ten. One edit fixed the sentence and
     * ended the collision, which is what issue 57 is about in both places it has landed.
     */
    val FRAGMENTS: List<RecordedFragment> = listOf(
        RecordedFragment(
            Purpose.MOMENTUM_HEADLINE,
            "the last two weeks have been",
            setOf("quietStretch", "steadyStretch"),
        ),
        RecordedFragment(
            Purpose.REPORT_OBSERVATION,
            "been waiting more than a fortnight",
            setOf("intakeVsOutput", "queuePressure"),
        ),
        RecordedFragment(
            Purpose.REPORT_OBSERVATION,
            "the queues have grown three weeks running",
            setOf("intakeVsOutput", "queuePressure"),
        ),
        RecordedFragment(
            Purpose.REPORT_OBSERVATION,
            "{} has been active in {}",
            setOf("neglectedArea", "persistentItem"),
        ),
        RecordedFragment(
            Purpose.REPORT_PATTERN,
            "each of the last three weeks",
            setOf("decliningActivity", "reportedVsActual", "shiftingFocus"),
        ),
        RecordedFragment(
            Purpose.REPORT_PATTERN,
            "three weeks ago you touched {} areas this week {}",
            setOf("broadeningFocus", "narrowingFocus"),
        ),
        RecordedFragment(
            Purpose.REPORT_PATTERN,
            "{} then {} then {} sessions",
            setOf("focusHabitFading", "focusHabitForming"),
        ),
    )
    /** True when this exact collision, between these exact families, is recorded. */
    fun isRecordedFragment(purpose: Purpose, fragment: String, families: Set<FamilyKey>): Boolean =
        FRAGMENTS.any { it.purpose == purpose && it.fragment == fragment && it.families == families }

    /**
     * Families each over used construction is allowed in today, beyond the two shapes the
     * catalog already records for itself.
     *
     * `notXthenY` is `Not X.` standing as a whole sentence, in five families. `xCommaAndY`
     * is `X, and Y.`, in ten. Both are named as constructions by the phase 9 brief, so both
     * stay under the cap of two and an eleventh family reaching for either is a finding.
     */
    val CONSTRUCTIONS: Map<String, Set<FamilyKey>> = mapOf(
        "notXthenY" to setOf(
            "accumulation", "insufficientData", "mostActiveSince", "quietWeek", "weekQuiet"
        ),
        "xCommaAndY" to setOf(
            "concentration", "focusInvestment", "growingQueues", "hardStretch", "intakeVsOutput",
            "persistence", "queueDrain", "reportedVsActual", "singleFocus", "weekStrong"
        ),
    )
    /** The catalog's recorded allowance and this file's, together. */
    fun constructionAllowance(name: String): Set<FamilyKey> =
        CorpusGates.catalogAllowance(name) + CONSTRUCTIONS[name].orEmpty()

    /**
     * Bench id to the number of lines it held when its band spread was recorded as over the
     * cap.
     *
     * **Seventy four of the hundred and eight measurable benches when this was recorded,
     * which was the largest single finding in the corpus.** Read as one number it said the
     * corpus was written almost entirely at one length: most of the seventy four were over
     * the cap on `MEDIUM`, seven to fourteen words. That is not a defect in any one line, it
     * is exactly what a reader means when generated text feels flat, and it is why this gate
     * exists rather than being left to somebody's ear at the end.
     *
     * **Phase 9 took it to twenty eight of a hundred and thirteen**, because an exemption
     * here lapses on the first line added to a bench and every bench phase 9 grew had to
     * meet the cap on its own. The entries below that name a grown bench are therefore dead
     * records rather than live exemptions, and are kept only so a later reader can see what
     * the bench looked like before.
     */
    val LENGTH_BANDS: Map<String, Int> = mapOf(
        "AREAS_BANNER weekBuilding s1" to 8,
        "AREAS_BANNER weekMixed s1" to 8,
        // Dead since the batch that took this bench to sixty, and kept for the reason the
        // note above gives: the grown bench meets the cap on its own, at 26 SHORT and 34
        // MEDIUM, and this row is what it looked like at eight lines all in one band.
        "AREAS_BANNER weekQuiet s1" to 8,
        "AREAS_BANNER weekStarting s1" to 8,
        "AREAS_BANNER weekStrong s1" to 8,
        "MOMENTUM_HEADLINE balancedWeek s1" to 12,
        "MOMENTUM_HEADLINE cleanSlate s1" to 6,
        "MOMENTUM_HEADLINE comeback s1" to 12,
        "MOMENTUM_HEADLINE firstDays s1" to 10,
        "MOMENTUM_HEADLINE strongPace s1" to 12,
        "PULSE accumulation s3" to 11,
        "PULSE concentration s3" to 12,
        "PULSE persistence s1" to 15,
        "PULSE persistence s2" to 18,
        "PULSE persistence s3" to 16,
        "PULSE persistence s4" to 12,
        "PULSE queueDrain s1" to 8,
        "PULSE queueDrain s2" to 7,
        "PULSE quietDay s1" to 12,
        "PULSE rebalance s1" to 8,
        "PULSE spread s1" to 11,
        "PULSE spread s2" to 11,
        "PULSE throughput s3" to 10,
        "REPORT_OBSERVATION areaBalance s1 ext" to 4,
        "REPORT_OBSERVATION areaRevival s1" to 6,
        "REPORT_OBSERVATION areaRevival s1 ext" to 5,
        "REPORT_OBSERVATION completionSplit s1" to 7,
        "REPORT_OBSERVATION dayShape s1" to 7,
        "REPORT_OBSERVATION dayShape s1 ext" to 5,
        "REPORT_OBSERVATION firstMilestone s1" to 6,
        "REPORT_OBSERVATION focusAbandonment s1" to 5,
        "REPORT_OBSERVATION focusAbandonment s1 ext" to 4,
        "REPORT_OBSERVATION focusInvestment s1" to 6,
        "REPORT_OBSERVATION focusInvestment s2" to 7,
        "REPORT_OBSERVATION focusInvestment s3" to 6,
        "REPORT_OBSERVATION hardStretch s1" to 8,
        "REPORT_OBSERVATION intakeVsOutput s1" to 8,
        "REPORT_OBSERVATION intakeVsOutput s1 ext" to 5,
        "REPORT_OBSERVATION intakeVsOutput s2" to 16,
        "REPORT_OBSERVATION intakeVsOutput s2 ext" to 6,
        "REPORT_OBSERVATION intakeVsOutput s3 ext" to 5,
        "REPORT_OBSERVATION mostActiveSince s1 ext" to 4,
        "REPORT_OBSERVATION neglectedArea s1 ext" to 5,
        "REPORT_OBSERVATION neglectedArea s2 ext" to 5,
        "REPORT_OBSERVATION persistentItem s1" to 7,
        "REPORT_OBSERVATION persistentItem s1 ext" to 5,
        "REPORT_OBSERVATION personalBest s1" to 8,
        "REPORT_OBSERVATION queueDrained s1" to 5,
        "REPORT_OBSERVATION queuePressure s1" to 6,
        "REPORT_OBSERVATION queuePressure s1 ext" to 5,
        "REPORT_OBSERVATION quietWeek s1" to 11,
        "REPORT_OBSERVATION selfReportVsData s1" to 10,
        "REPORT_OBSERVATION selfReportVsData s1 ext" to 6,
        "REPORT_OBSERVATION singleFocus s1" to 10,
        "REPORT_OBSERVATION singleFocus s1 ext" to 8,
        "REPORT_OBSERVATION singleFocus s2 ext" to 7,
        "REPORT_OBSERVATION steadyPace s1" to 5,
        "REPORT_OBSERVATION steadyPace s1 ext" to 4,
        "REPORT_OBSERVATION switchingBehavior s1" to 9,
        "REPORT_OBSERVATION switchingBehavior s1 ext" to 5,
        "REPORT_OBSERVATION timeOfDay s1" to 6,
        "REPORT_OBSERVATION timeOfDay s1 ext" to 4,
        "REPORT_PATTERN abandonmentPattern s1" to 4,
        "REPORT_PATTERN areaGoneQuiet s1" to 9,
        "REPORT_PATTERN comebackPattern s1" to 5,
        "REPORT_PATTERN consistentRhythm s1" to 8,
        "REPORT_PATTERN decliningActivity s1" to 8,
        "REPORT_PATTERN focusHabitFading s1" to 6,
        "REPORT_PATTERN focusHabitForming s1" to 7,
        "REPORT_PATTERN growingQueues s1" to 10,
        "REPORT_PATTERN insufficientData s1" to 4,
        "REPORT_PATTERN queueEquilibrium s1" to 5,
        "REPORT_PATTERN reportedVsActual s1" to 7,
        "REPORT_PATTERN shiftingFocus s1" to 10,
    )
    /** True only at the exact size the exemption was written against. See the file comment. */
    fun bandExemptAt(benchId: String, size: Int): Boolean = size == LENGTH_BANDS[benchId]

    /**
     * Bench id to the number of lines it held when its thin or missing register was recorded.
     *
     * Fifty three benches, and their shape is worth stating: thirty of the findings are a
     * single editorial line in a Report observation bench, which means that every time the
     * realizer reaches for the editorial voice in that family it gets the same sentence,
     * until the ninety day exclusion takes it away and it gets a different register instead.
     * The rest are hot benches with no line at all in a register their volume's fallback
     * order will ask for.
     */
    val REGISTERS: Map<String, Int> = mapOf(
        "AREAS_BANNER weekBuilding s1" to 8,
        "AREAS_BANNER weekMixed s1" to 8,
        // **The one entry in this file that is not a debt anybody can pay.** `bn.quiet` is
        // sixty lines and every one of them is `[N]`, which `CORPUS_3_MOMENTUM.md`
        // authoring rule 5 requires of a quiet state, so it carries no plain, observational
        // or reflective line at all. This entry recorded eight lines and lapsed on the
        // ninth, and that debt was paid the way 11.1 asks: the bench went from eight lines
        // to the hot floor of sixty. It went there in one voice, and the reason is
        // mechanical rather than editorial. 7.4 step 1 offers `NEUTRAL_AGENT` as a tier of
        // one to a rule marked unflattering, `Realizer.realize` leaves a tier only when
        // nothing in it can be filled, and no line in this bench carries a slot. So the
        // neutral agent tier fills on every one of the family's 240 firings a year and the
        // open tier is never reached: a plain, observational or reflective line written
        // here would be a line the app can never say, which is the defect the register pass
        // had just removed, pointing the other way. This gate's reason for asking a hot
        // bench for the three open registers is that a stage missing one has a register the
        // realizer will ask for and never get, and that premise is false for exactly this
        // bench. **The durable fix is in the gate rather than here**: `registerDepth` should
        // not ask for the open tier of a hot bench whose rule is unflattering and whose
        // `[N]` bench is not empty. Until it does, this holds at sixty and lapses at sixty
        // one, like every other row.
        "AREAS_BANNER weekQuiet s1" to 60,
        "AREAS_BANNER weekStarting s1" to 8,
        "AREAS_BANNER weekStrong s1" to 8,
        "MOMENTUM_HEADLINE cleanSlate s1" to 6,
        "MOMENTUM_HEADLINE steadyStretch s1" to 18,
        "REPORT_HEADLINE balanced s1" to 12,
        "REPORT_HEADLINE comeback s1" to 8,
        "REPORT_HEADLINE mostActiveSince s1" to 8,
        "REPORT_HEADLINE personalBest s1" to 10,
        "REPORT_OBSERVATION areaBalance s1" to 6,
        "REPORT_OBSERVATION areaBalance s1 ext" to 4,
        "REPORT_OBSERVATION areaRevival s1" to 6,
        "REPORT_OBSERVATION areaRevival s1 ext" to 5,
        "REPORT_OBSERVATION completionSplit s1 ext" to 6,
        "REPORT_OBSERVATION dayShape s1 ext" to 5,
        "REPORT_OBSERVATION firstMilestone s1" to 6,
        "REPORT_OBSERVATION firstMilestone s1 ext" to 3,
        "REPORT_OBSERVATION focusAbandonment s1" to 5,
        "REPORT_OBSERVATION focusAbandonment s1 ext" to 4,
        "REPORT_OBSERVATION focusInvestment s1" to 6,
        "REPORT_OBSERVATION focusInvestment s1 ext" to 5,
        "REPORT_OBSERVATION focusInvestment s2 ext" to 6,
        "REPORT_OBSERVATION focusInvestment s3 ext" to 5,
        "REPORT_OBSERVATION intakeVsOutput s1" to 8,
        "REPORT_OBSERVATION intakeVsOutput s1 ext" to 5,
        "REPORT_OBSERVATION intakeVsOutput s2 ext" to 6,
        "REPORT_OBSERVATION intakeVsOutput s3 ext" to 5,
        "REPORT_OBSERVATION mostActiveSince s1" to 5,
        "REPORT_OBSERVATION mostActiveSince s1 ext" to 4,
        "REPORT_OBSERVATION neglectedArea s1" to 7,
        "REPORT_OBSERVATION neglectedArea s1 ext" to 5,
        "REPORT_OBSERVATION persistentItem s1 ext" to 5,
        "REPORT_OBSERVATION personalBest s1" to 8,
        "REPORT_OBSERVATION personalBest s1 ext" to 5,
        "REPORT_OBSERVATION queueDrained s1" to 5,
        "REPORT_OBSERVATION queueDrained s1 ext" to 4,
        "REPORT_OBSERVATION queuePressure s1" to 6,
        "REPORT_OBSERVATION queuePressure s1 ext" to 5,
        "REPORT_OBSERVATION selfReportVsData s1" to 10,
        "REPORT_OBSERVATION selfReportVsData s1 ext" to 6,
        "REPORT_OBSERVATION singleFocus s1 ext" to 8,
        "REPORT_OBSERVATION singleFocus s2 ext" to 7,
        "REPORT_OBSERVATION steadyPace s1" to 5,
        "REPORT_OBSERVATION steadyPace s1 ext" to 4,
        "REPORT_OBSERVATION switchingBehavior s1" to 9,
        "REPORT_OBSERVATION switchingBehavior s1 ext" to 5,
        "REPORT_OBSERVATION timeOfDay s1" to 6,
        "REPORT_OBSERVATION timeOfDay s1 ext" to 4,
        "REPORT_PATTERN comebackPattern s1" to 5,
        "REPORT_PATTERN consistentRhythm s1" to 8,
        "REPORT_PATTERN growingQueues s1" to 10,
        "REPORT_PATTERN reportedVsActual s1" to 7,
    )
    /** True only at the exact size the exemption was written against. See the file comment. */
    fun registerExemptAt(benchId: String, size: Int): Boolean = size == REGISTERS[benchId]

    /**
     * Pairs of keys that read as one line today. Recorded by key, because neither may be
     * reworded by an author who is only allowed to add.
     *
     * **One of these is not a near duplicate, it is a duplicate.** `ob.neg.s2.l02` and
     * `ob.neg.s2.l09` are the same sentence, character for character, under two keys, so
     * that bench is one line smaller than it counts itself. It is recorded rather than fixed
     * because retiring a key is the owner's call and a key is never reused, and it is called
     * out here so that the call can be made.
     */
    val DUPLICATES: Set<Set<String>> = setOf(
        setOf("accumulation.s1.01", "accumulation.s1.03"),
        setOf("throughput.s1.01", "throughput.s1.03"),
        setOf("hd.single.03", "hd.single.06"),
        setOf("hd.back.01", "hd.back.06"),
        setOf("hd.fall.01", "hd.fall.02"),
        setOf("hd.fall.01", "hd.fall.04"),
        setOf("ob.single.s2.l04", "ob.single.s2.l13"),
        setOf("ob.neg.s2.l02", "ob.neg.s2.l09"),
        setOf("bn.quiet.01", "bn.quiet.04"),
        setOf("bn.quiet.04", "bn.quiet.08"),
    )
    /** True when this pair is recorded. */
    fun isRecordedDuplicate(one: String, other: String): Boolean = setOf(one, other) in DUPLICATES

    /**
     * Lines the engine cannot say today, with the reason each one is out of reach.
     *
     * **Two, and both of them are lines whose binding is right.** The list held eighty six
     * when it was written and the binding pass that followed it decided every one: twenty
     * two gained a binding or a fact, sixty two were retired into `SlotBindings.EXCLUDED`
     * with the reason recorded beside the key, and these two are neither. Their markers
     * name the quantity the table reads, and the fact behind that quantity was simply not
     * there on any of the twenty four differently shaped moments their stage produced
     * across eleven simulated years.
     *
     * That is a different claim from the one this list used to make, and it is worth
     * keeping the difference visible. A line with no binding can never be said, on any day,
     * on any device. A line like these two is one busy Tuesday away from being said, and
     * the honest thing to record is that the simulated years did not contain that Tuesday.
     *
     * The list is keyed by variant and never lapses on size. Growing the bench around a
     * line changes nothing about whether its fact exists.
     */
    val UNRENDERABLE: Map<String, String> = mapOf(
        // `Most things you complete take {m}. This one is at {ageDays}.` `{m}` reads
        // `ItemFacts.medianDaysToComplete`, which is null under three completions, and a
        // Pulse window is one day. Three things finished in one day is a real day and not a
        // common one, and widening the median's horizon would change what `persistentItem`
        // means by `usually` on the Report as well.
        "persistence.s3.09" to "{m} reads medianDaysToComplete, which needs three completions inside a one day window",
        // `{n} completions and {sessions} focus sessions.` `{sessions}` reads sessions that
        // **finished**, which is what the line says, and `strongPace` is a fortnight of
        // completions rather than of focus. No persona finished a session inside a window
        // this family also qualified on.
        "mo.pace.12" to "{sessions} reads focusSessions, which read nothing in any sampled moment",
    )

    /**
     * Markers standing in front of a unit noun their measure does not count.
     *
     * **Empty, and it is meant to stay empty.** The gate that finds these was written after
     * the binding table existed, and the one finding it made on the day it was written was
     * a live defect rather than a debt: `ob.since.e02` was rendering *It has been 47 weeks*
     * from the week's event count, on a screen, in a family firing eighty eight times a
     * year. It was fixed rather than recorded.
     *
     * Everything else in this file grandfathers something the corpus already does. This
     * hook is here for symmetry with the other gates and because a gate with no exemption
     * path invites somebody to widen the rule instead, but an entry added here would be a
     * decision that one false sentence is acceptable, which is the one thing 1.1 says is
     * never recoverable.
     */
    val MISBOUND: Set<Pair<String, String>> = emptySet()

    /** True when this exact marker in this exact line is recorded. */
    fun isRecordedMisbound(variantKey: String, slot: String): Boolean = (variantKey to slot) in MISBOUND

    /** True when this line is one of the ones already known to be unsayable. */
    fun isRecordedUnrenderable(variantKey: String): Boolean = variantKey in UNRENDERABLE
}
