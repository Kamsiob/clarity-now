package com.kamsiob.claritynow.domain.pulse

import com.kamsiob.claritynow.data.event.PulseGenerated
import com.kamsiob.claritynow.data.event.SubjectKind
import com.kamsiob.claritynow.domain.engine.ClarityEngine
import com.kamsiob.claritynow.domain.engine.EngineResult
import com.kamsiob.claritynow.domain.engine.FactExtractor
import com.kamsiob.claritynow.domain.engine.FactSet
import com.kamsiob.claritynow.domain.engine.FiringHistory
import com.kamsiob.claritynow.domain.engine.RenderedOutput
import com.kamsiob.claritynow.domain.engine.SilenceReason
import com.kamsiob.claritynow.domain.engine.catalog.ClarityCatalog
import com.kamsiob.claritynow.domain.engine.catalog.Purpose
import com.kamsiob.claritynow.domain.engine.catalog.ResponseOption
import com.kamsiob.claritynow.domain.engine.realize.Candidate
import com.kamsiob.claritynow.domain.engine.realize.Slot
import com.kamsiob.claritynow.domain.engine.validate.ClarityValidator
import com.kamsiob.claritynow.domain.query.TrailQueries
import java.time.ZoneId
import com.kamsiob.claritynow.domain.engine.catalog.SubjectKind as EngineSubjectKind

/**
 * What one day's generation decided. Four outcomes, and three of them write nothing.
 *
 * A sealed set rather than a nullable payload, because the three silent shapes are
 * different states that a reader has to be able to tell apart. `MASTER_BUILD_PROMPT.md`
 * 12.1 makes all three IDLE on the screen, which is the point: a day the engine chose
 * not to speak on and a day suppressed after a return look identical to the person and
 * must not look identical in a log line.
 */
sealed interface PulseDecision {

    /** The day this was decided for, which is the day any writer files the entry under. */
    val day: PulseDay

    /**
     * Step 2. An entry already exists for this date key.
     *
     * **Nothing is regenerated and nothing is compared.** The entry is immutable once
     * written, so this is not a merge and not a refresh: the stored observation is the
     * one the person saw this morning and it stays the one they see tonight.
     */
    data class AlreadyWritten(override val day: PulseDay) : PulseDecision

    /**
     * `MASTER_BUILD_PROMPT.md` 14b.4. The first two days after a return from a long
     * absence. Writes nothing, so the day is IDLE and no reminder is posted.
     *
     * **Not engine silence, and deliberately a separate outcome.** 14b.4 puts these days
     * outside the Pulse silence floor in both the numerator and the denominator, because
     * that floor measures how often the engine chose not to speak and this is not a
     * choice it made. Folding this into [Silent] would quietly make a suppressed day
     * count as discretion.
     */
    data class SuppressedAfterReturn(override val day: PulseDay) : PulseDecision

    /**
     * Step 7. The engine had nothing to say. **Nothing at all is written**: not an empty
     * entry, not a row with a null observation.
     */
    data class Silent(override val day: PulseDay, val reason: SilenceReason) : PulseDecision

    /**
     * Step 8. One observation, ready to be appended by the only writer in the app.
     *
     * [responses] are carried beside the payload rather than inside it because the
     * payload has no field for them, and the sheet needs them the moment it opens. The
     * key of the pair they came from is recorded in the payload's fact snapshot, so a
     * process that dies before the answer can recover the same pair rather than choosing
     * a second one. See [PulseGenerator.RESPONSE_PAIR_KEY].
     */
    data class Speak(
        override val day: PulseDay,
        val payload: PulseGenerated,
        val responses: List<ResponseOption>,
    ) : PulseDecision
}

/**
 * The Pulse generation lifecycle, exactly as `MASTER_BUILD_PROMPT.md` 11.3 writes it.
 *
 * ```
 * 1. Compute dateKey from ClarityClock with an explicit zone
 * 2. If a ClarityPulseEntry exists for dateKey, stop. Display it. It is immutable
 * 3. reflectionPeriod: before 17:00 use yesterday, at or after 17:00 use today so far
 * 4. FactExtractor(queries).extract(window) -> FactSet
 * 5. FiringHistory.from the log: PULSE_GENERATED, REPORT_GENERATED, PLAN_OFFERED
 * 6. ClarityEngine(catalog, ClarityValidator(zone), zone).observe(facts, history, PULSE)
 * 7. If Silent, write nothing. The day is IDLE. The chip shows no dot
 * 8. If Spoke, write PULSE_GENERATED with family, stage, register, variantKey,
 *    rendered strings and the fact snapshot
 * ```
 *
 * One step is added and it is numbered rather than hidden: **2b, the re-entry
 * suppression**, from 14b.4, which postdates the sequence above. It sits after the
 * existence check because a day that already has an entry has nothing to suppress, and
 * before the extraction because the whole point is that the facts of an absence are
 * never read.
 *
 * ## Why this is a class and not a method on the repository
 *
 * Every line below is a pure function of the log, the zone and one instant, and none of
 * it needs Room, DataStore or a Context. Held inside `ClarityRepository` it would be
 * reachable only by an instrumented test, and "one Pulse per local day across a daylight
 * saving boundary" is exactly the kind of statement that has to be checked by a test
 * rather than by reasoning. `FocusSession.kt` was split out of the repository for the
 * same reason in phase 4.
 *
 * It is in `domain.pulse` rather than beside `FocusSession.kt` in `data.repo` because of
 * the distinction that file draws on itself: it answers what one device should show right
 * now and folds nothing, and this reads the whole log through `TrailQueries`, extracts
 * facts from it and rebuilds a firing history out of it. That is domain work.
 *
 * ## What it will not do
 *
 * It has no way to write. It returns a decision and the caller commits it, so there is
 * no path by which a silent day, a suppressed day or an already answered day can leave
 * a row behind. It also has no clock: the instant arrives as a parameter, so a test can
 * stand on either side of 17:00 and on either side of a daylight saving boundary
 * deliberately rather than by waiting.
 */
class PulseGenerator(
    private val catalog: ClarityCatalog,
    private val zone: ZoneId,
    /**
     * Mints the id the entry is keyed by. Injected because `UUID.randomUUID` in here
     * would make every decision unreproducible, and the whole file is built so that two
     * devices holding one log reach the same one.
     */
    private val newPulseId: () -> String,
) {

    /**
     * Step 6, and the constructor call is quoted from 11.3 rather than parameterized.
     *
     * There is no seam for the validator here, unlike `ClarityEngine` itself, which takes
     * one so that the engine cannot silently skip layer 5. This class is a caller, and a
     * caller that accepted a validator would be a way to hand the engine one that vetoes
     * nothing. 11.4: never bypass the validator, not for a simple sentence, not for an
     * empty state, not to fix a bug.
     */
    private val engine = ClarityEngine(catalog, ClarityValidator(zone), zone)

    /**
     * Runs the sequence for the local day containing [nowMillis].
     *
     * [queries] must be built over the **whole** log. Layer one reads lifetime maps for
     * its history facts and `FiringHistory` reads every engine authored event ever
     * written, so a windowed facade here would produce a firing history that had
     * forgotten what the app said last week.
     */
    fun decide(queries: TrailQueries, nowMillis: Long): PulseDecision {
        require(queries.zone() == zone) {
            "the facade counted in ${queries.zone()} and this generator is filing days in " +
                "$zone, so the window and the date key would disagree"
        }

        // 1 and 3. One clock reading, one zone, and both the key and the period fall out
        // of it together. See PulseSchedule.
        val day = PulseSchedule.dayAt(nowMillis, zone)

        // 2. Immutable once written.
        if (hasEntryOn(queries, day.dateKey)) return PulseDecision.AlreadyWritten(day)

        // 2b. 14b.4. The first two days back are IDLE, whatever the log says about them.
        val reEntry = queries.lastReEntryOnOrBefore(day.dateKey)
        if (reEntry != null && reEntry.daysSince(day.dateKey) in 0 until SUPPRESSED_DAYS_BACK) {
            return PulseDecision.SuppressedAfterReturn(day)
        }

        // 4.
        val facts = FactExtractor(queries).extract(day.window)

        // 5. Rebuilt from the log on every invocation and never cached, per 11.7.
        val history = FiringHistory.from(queries, nowMillis)

        // 6.
        return when (val result = engine.observe(facts, history, Purpose.PULSE)) {
            // 7. Nothing is written. There is no payload on this branch to write.
            is EngineResult.Silent -> PulseDecision.Silent(day, result.reason)
            // 8.
            is EngineResult.Spoke -> speak(day, result.output, facts)
        }
    }

    /**
     * True when the log already carries a `PULSE_GENERATED` for [dateKey].
     *
     * Asked of the log rather than of the projection, so that this class stays a function
     * of one input. The repository asks the same question of the in memory projection
     * under its own lock before it appends, which is what makes the rule hold against two
     * callers racing at launch rather than merely against a slow one.
     */
    private fun hasEntryOn(queries: TrailQueries, dateKey: String): Boolean =
        queries.pulsesGeneratedBetween(Long.MIN_VALUE, Long.MAX_VALUE).any { it.dateKey == dateKey }

    private fun speak(day: PulseDay, output: RenderedOutput, facts: FactSet): PulseDecision {
        val candidate = output.meta
        val pairKey = responsePairKeyOf(output.responses)
        return PulseDecision.Speak(
            day = day,
            payload = PulseGenerated(
                pulseId = newPulseId(),
                dateKey = day.dateKey,
                family = candidate.familyKey,
                escalationStage = candidate.stage,
                register = candidate.register.name,
                variantKey = candidate.variantKey,
                renderedObservation = output.text,
                renderedQuestion = output.question,
                factSnapshot = snapshotOf(candidate, pairKey),
                reflectionPeriod = day.reflectionPeriod,
                subjectId = candidate.subjectId,
                subjectKind = subjectKindOf(candidate, facts),
            ),
            responses = output.responses,
        )
    }

    /**
     * The facts the sentence rests on, one entry per filled slot.
     *
     * The shape follows the only existing writer of this field, the simulator, so that a
     * log written by the app and a log written by the simulator can be read by the same
     * reader. Slot values are stored as the number or the snapshot name that was used,
     * never as the rendered text: the rendered text is already on the event, and the
     * point of the snapshot is to be able to check the sentence against what it claimed.
     */
    private fun snapshotOf(candidate: Candidate, responsePairKey: String?): Map<String, String> {
        val slots = candidate.slots.entries.associate { (key, slot) -> key to slotValue(slot) }
        return if (responsePairKey == null) slots else slots + (RESPONSE_PAIR_KEY to responsePairKey)
    }

    private fun slotValue(slot: Slot): String = when (slot) {
        is Slot.Text -> slot.value
        is Slot.Count -> slot.value.toString()
        is Slot.Days -> slot.value.toString()
        is Slot.Percent -> slot.value.toString()
        is Slot.DateRef -> slot.weekKey
    }

    /**
     * Which kind of thing the observation was about, resolved rather than guessed.
     *
     * The rule that fired is looked up by its own key and its subject selector is run
     * again over the same fact set, so the answer is the one the selector gave when it
     * produced this subject in the first place. That is a re-derivation, not a heuristic.
     *
     * **Testing the id against the known area ids is the tempting version and it is
     * banned**, per the note on `data.event.SubjectKind`: an id on its own cannot be
     * resolved back to its kind. Testing it against the ids the sentence happened to name
     * is the same mistake with an extra failure mode, because a family whose subject is
     * an item can be realized through a statement that names only the area.
     */
    private fun subjectKindOf(candidate: Candidate, facts: FactSet): SubjectKind? {
        val subjectId = candidate.subjectId ?: return null
        val rule = catalog.rulesFor(Purpose.PULSE).firstOrNull { it.key == candidate.ruleKey }
        val kind = rule?.subject?.select(facts)?.firstOrNull { it?.id == subjectId }?.kind
        return when (kind) {
            EngineSubjectKind.AREA -> SubjectKind.AREA
            EngineSubjectKind.ITEM -> SubjectKind.ITEM
            // Unreachable: the candidate's subject came out of this rule's own selector
            // over these same facts. Null rather than a throw, because the cost of being
            // wrong about that is one underivable field on one event and the cost of a
            // throw is the first screen of somebody's day.
            null -> null
        }
    }

    /**
     * The key of the pair [responses] came from, read back off an option key.
     *
     * `CorpusParser` builds an option key as the pair key plus a dot and the option's
     * position, which is the only relationship between the two, and a test over the real
     * corpus asserts that it holds for every pair in the file. Null when there are no
     * responses, which is every purpose except the Pulse.
     */
    private fun responsePairKeyOf(responses: List<ResponseOption>): String? =
        responses.firstOrNull()?.key?.substringBeforeLast('.')

    companion object {

        /**
         * 14b.4: "Pulse generates nothing for the first two days back." The day of the
         * return itself and the day after it, which are days since zero and one.
         */
        const val SUPPRESSED_DAYS_BACK: Int = 2

        /**
         * Where the response pair key is recorded on the event, inside the fact snapshot.
         *
         * **It is not a slot and it is not a fact**, and it is here because the payload has
         * no field for it while the schema window in issue #19 is open. The sheet has to be
         * able to show the same two answers after the process that generated them has died,
         * and the alternatives were both worse: choosing the pair again at display time is
         * a second selection of a corpus line, and storing nothing means a person who
         * answers in the evening may be answering a different question from the one they
         * read at breakfast.
         *
         * The prefix keeps it out of the way of a slot key, which is always a bare
         * lowerCamel word such as `itemTitle` or `areaName`.
         */
        const val RESPONSE_PAIR_KEY: String = "pulse.responsePairKey"
    }
}
