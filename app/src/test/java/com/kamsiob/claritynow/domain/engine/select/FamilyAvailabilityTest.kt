package com.kamsiob.claritynow.domain.engine.select

import com.kamsiob.claritynow.domain.engine.FactSet
import com.kamsiob.claritynow.domain.engine.FamilyKey
import com.kamsiob.claritynow.domain.engine.FiringHistory
import com.kamsiob.claritynow.domain.engine.PartOfDay
import com.kamsiob.claritynow.domain.engine.Precedent
import com.kamsiob.claritynow.domain.engine.catalog.CorpusFixture
import com.kamsiob.claritynow.domain.engine.catalog.EngineFamilies
import com.kamsiob.claritynow.domain.engine.catalog.FamiliesAwaitingLanguage
import com.kamsiob.claritynow.domain.engine.catalog.Purpose
import com.kamsiob.claritynow.domain.engine.realize.EngineFacts
import com.kamsiob.claritynow.domain.engine.realize.EngineMoment
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Step 1b of selection: the capacity gate of `MASTER_BUILD_PROMPT.md` 14b.9 and the week of
 * withholding after a return of 14b.4.
 *
 * ## What has to be provable here
 *
 * 14b.9 asks for three things and the middle one is the one a gate usually fails: "that the
 * fact exist, **that it gate those families rather than merely re-word them**, and that the
 * gate be reachable in a test". So every assertion below reads the ranked list rather than
 * a sentence. A family that was re-worded is still in the list; a family that was gated is
 * not there at all.
 *
 * 14b.4 asks for the same shape on a different trigger, and adds one thing a test can be
 * written against directly: the next ranked candidate is taken instead, and the report is
 * shorter when there is no next one, because 11.4 forbids padding to reach a minimum.
 *
 * ## The distinction this file exists to hold
 *
 * `Precedent.NONE` is the permission and `Precedent.PRESENT` is the veto, and
 * `INSUFFICIENT` is neither. A gate that folded `INSUFFICIENT` into `PRESENT` would silence
 * every decline observation for every new user, which is a different bug wearing the same
 * clothes as the one 14b.9 fixes. Three of the tests below are that distinction, one value
 * at a time.
 */
class FamilyAvailabilityTest {

    private val selector = Selector(CorpusFixture.catalog)

    private val moment = EngineMoment(EngineFacts.dateKey(120), PartOfDay.MORNING)

    private fun families(purpose: Purpose, facts: FactSet): List<FamilyKey> =
        (selector.select(purpose, facts, FiringHistory.EMPTY, moment) as? SelectionOutcome.Ranked)
            ?.selections.orEmpty()
            .map { it.rule.family }

    /** The two to four the observation pass actually chooses, which is where a fall through shows. */
    private fun chosen(facts: FactSet): List<FamilyKey> =
        selector.selectObservations(facts, FiringHistory.EMPTY, moment).map { it.rule.family }

    // ------------------------------------------------------------------ fixtures

    /**
     * A year of history whose activity has fallen for three weeks running, with one area
     * that has been silent long enough to be neglected.
     *
     * Shaped to make the gated families qualify rather than to make them fire, so that a
     * test asserting one is absent is asserting the gate and not the shape of the week.
     */
    private fun decliningYear(
        activityPrecedent: Precedent = Precedent.NONE,
        areaPrecedent: Precedent = Precedent.NONE,
        focusPrecedent: Precedent = Precedent.NONE,
        justBack: Boolean = false,
    ): FactSet {
        val work = EngineFacts.area(
            areaId = "work", name = "Work", events = 3, completions = 1, share = 1.0,
            lifetimeEvents = 300, lifetimeCompletions = 90, ageDays = 360,
        )
        val music = EngineFacts.area(
            areaId = "music", name = "Music", events = 0, share = 0.0,
            daysSinceLastEvent = 24, lifetimeEvents = 60, lifetimeCompletions = 20, ageDays = 360,
            dipPrecedent = areaPrecedent,
        )
        return EngineFacts.factSet(
            window = EngineFacts.window(totalEvents = 3, completions = 1, activeDays = 2, endDay = 7),
            areas = listOf(work, music),
            dominantAreaId = "work",
            history = EngineFacts.history(
                daysSinceInstall = 360,
                weekTotalEvents = listOf(20, 14, 9, 3),
                weekAreaCounts = listOf(4, 3, 2, 1),
                weekFocusStarted = listOf(6, 4, 2, 1),
                weekQueueSizes = listOf(2, 3, 4, 5),
                activityDipPrecedent = activityPrecedent,
                focusDipPrecedent = focusPrecedent,
                isJustBackFromAbsence = justBack,
            ),
        )
    }

    // ------------------------------------------------------------------ 14b.9

    /**
     * The control. Without it every assertion below would pass on a fact set where the
     * families never qualified in the first place.
     */
    @Test
    fun `a fall with no precedent still reaches the decline families`() {
        val qualified = families(Purpose.REPORT_OBSERVATION, decliningYear()) +
            families(Purpose.REPORT_PATTERN, decliningYear()) +
            families(Purpose.REPORT_HEADLINE, decliningYear())
        assertTrue(
            "the fixture has to make the gated families qualify or this file proves nothing: $qualified",
            "quietWeek" in qualified && "neglectedArea" in qualified && "decliningActivity" in qualified,
        )
    }

    @Test
    fun `a fall this person has had before excludes the decline families rather than re-wording them`() {
        val facts = decliningYear(
            activityPrecedent = Precedent.PRESENT,
            areaPrecedent = Precedent.PRESENT,
            focusPrecedent = Precedent.PRESENT,
        )
        val spoken = families(Purpose.REPORT_OBSERVATION, facts) +
            families(Purpose.REPORT_PATTERN, facts) +
            families(Purpose.REPORT_HEADLINE, facts)
        for (family in FamilyAvailability.PRECEDENT_GATED.keys) {
            assertFalse(
                "$family is gated by 14b.9 and it is still in the ranking: $spoken",
                family in spoken,
            )
        }
    }

    /**
     * A person with too little history gets neither sentence, which is the whole reason
     * `Precedent` has four values.
     */
    @Test
    fun `too little history is neither the permission nor the veto`() {
        val facts = decliningYear(
            activityPrecedent = Precedent.INSUFFICIENT,
            areaPrecedent = Precedent.INSUFFICIENT,
            focusPrecedent = Precedent.INSUFFICIENT,
        )
        val spoken = families(Purpose.REPORT_OBSERVATION, facts)
        assertTrue(
            "INSUFFICIENT is not the veto. Gating on it would silence every decline " +
                "observation for every new user, which is a different bug in the same clothes",
            "quietWeek" in spoken,
        )
        assertNull(
            "and INSUFFICIENT is not the permission either, so the rhythm family says nothing",
            FamiliesAwaitingLanguage.RULES.firstOrNull { rule ->
                rule.criteria.all { it.test(facts, null) }
            },
        )
    }

    /**
     * The second branch. 14b.9 requires a different family with different language, not the
     * same family softened, so the family that speaks when the gate closes is a different
     * family and it qualifies on exactly the value that closed the gate.
     */
    @Test
    fun `the second branch qualifies on the precedent that closed the gate, and only on that`() {
        val rhythm = FamiliesAwaitingLanguage.RULES.single { it.key.endsWith("activity") }
        assertTrue(
            "PRESENT is what the rhythm family speaks on",
            rhythm.criteria.all { it.test(decliningYear(activityPrecedent = Precedent.PRESENT), null) },
        )
        for (other in listOf(Precedent.NONE, Precedent.INSUFFICIENT, Precedent.NOT_IN_A_DIP)) {
            assertFalse(
                "$other is not PRESENT, so the rhythm family has nothing to say",
                rhythm.criteria.all { it.test(decliningYear(activityPrecedent = other), null) },
            )
        }
    }

    /** The gate reads the subject's own precedent, so one quiet area does not gate another. */
    @Test
    fun `the area gate is per area`() {
        val cyclical = EngineFacts.area(
            areaId = "music", name = "Music", events = 0, daysSinceLastEvent = 24,
            lifetimeEvents = 60, lifetimeCompletions = 20, ageDays = 360,
            dipPrecedent = Precedent.PRESENT,
        )
        val newlyQuiet = EngineFacts.area(
            areaId = "admin", name = "Admin", events = 0, daysSinceLastEvent = 9,
            lifetimeEvents = 40, lifetimeCompletions = 15, ageDays = 360,
            dipPrecedent = Precedent.NONE,
        )
        val work = EngineFacts.area(areaId = "work", name = "Work", events = 4, completions = 2, share = 1.0)
        val facts = EngineFacts.factSet(
            window = EngineFacts.window(totalEvents = 4, completions = 2, activeDays = 3, endDay = 7),
            areas = listOf(work, cyclical, newlyQuiet),
            dominantAreaId = "work",
            history = EngineFacts.history(daysSinceInstall = 360),
        )
        val neglected = (selector.select(Purpose.REPORT_OBSERVATION, facts, FiringHistory.EMPTY, moment)
            as SelectionOutcome.Ranked)
            .selections
            .filter { it.rule.family == "neglectedArea" }
            .mapNotNull { it.subjectId }
        assertEquals(listOf("admin"), neglected)
    }

    // ------------------------------------------------------------------ 14b.4

    @Test
    fun `the week after a return withholds every decline, neglect and gap family`() {
        val facts = decliningYear(justBack = true)
        val spoken = Purpose.entries
            .filter { it in FamilyAvailability.RE_ENTRY_PURPOSES }
            .flatMap { families(it, facts) }
            .toSet()
        val leaked = spoken.filter { it in FamilyAvailability.WITHHELD_ON_RE_ENTRY }
        assertEquals(
            "14b.4: for seven days from the re-entry date every rule in those families is " +
                "unavailable to selection",
            emptyList<FamilyKey>(),
            leaked,
        )
    }

    /**
     * The next ranked candidate is taken instead, over the pass that actually chooses.
     *
     * Read through `selectObservations` rather than through the ranking, because that is
     * where the substitution happens: the ranking only loses entries, and a test asserting
     * that a shorter list is a subset of a longer one would pass on a gate that did
     * nothing.
     */
    @Test
    fun `the next ranked candidate is taken, and nothing is added to replace what was withheld`() {
        val qualified = families(Purpose.REPORT_OBSERVATION, decliningYear())
        val ordinary = chosen(decliningYear())
        val justBack = chosen(decliningYear(justBack = true))
        assertTrue(
            "the fixture has to withhold something or this proves nothing: $ordinary",
            ordinary.any { it in FamilyAvailability.WITHHELD_ON_RE_ENTRY },
        )
        assertTrue(
            "nothing withheld may survive the pass: $justBack",
            justBack.none { it in FamilyAvailability.WITHHELD_ON_RE_ENTRY },
        )
        assertTrue(
            "what replaced it has to be something that already qualified. A candidate " +
                "appearing here that was not in the ranking is a report inventing an " +
                "observation to fill a slot: $justBack against $qualified",
            justBack.all { it in qualified },
        )
    }

    /**
     * The report is shorter when nothing else qualifies. 11.4 forbids padding a section to
     * reach a minimum, and 14b.4 says so again in its own words.
     *
     * A week whose only qualifying observation is a withheld one, so the pass has nothing
     * to fall through to and the section is empty rather than filled.
     */
    @Test
    fun `a report with nothing left to say is shorter rather than padded`() {
        val only = EngineFacts.area(
            areaId = "work", name = "Work", events = 2, completions = 1, share = 1.0,
            lifetimeEvents = 200, lifetimeCompletions = 60, ageDays = 360,
        )
        fun week(justBack: Boolean) = EngineFacts.factSet(
            window = EngineFacts.window(totalEvents = 2, completions = 1, activeDays = 1, endDay = 7),
            areas = listOf(only),
            dominantAreaId = "work",
            history = EngineFacts.history(
                daysSinceInstall = 360,
                personalBestWeekCompletions = 5,
                mostRecentBetterWeekKey = EngineFacts.dateKey(30),
                isJustBackFromAbsence = justBack,
            ),
        )
        assertEquals(listOf("quietWeek"), chosen(week(justBack = false)))
        assertEquals(
            "the one thing worth saying was a reading of the absence, so the section is empty",
            emptyList<FamilyKey>(),
            chosen(week(justBack = true)),
        )
    }

    /** The Pulse keeps its own older rule, which is a silence above layer one rather than here. */
    @Test
    fun `the Pulse is not withheld here, because 14b_4 gives it a different instrument`() {
        assertFalse(
            "the Pulse declines to run the engine at all for two days after a return, per " +
                "PulseGeneration step 2b. A second instrument here would be five more days " +
                "of silence nobody asked for",
            Purpose.PULSE in FamilyAvailability.RE_ENTRY_PURPOSES,
        )
        assertTrue(
            "and no Pulse family is in the withheld set either, so the two cannot overlap by accident",
            EngineFamilies.PULSE.none { it.key in FamilyAvailability.WITHHELD_ON_RE_ENTRY },
        )
    }

    // ------------------------------------------------------------------ the two together

    /**
     * Where the two gates overlap, and why the overlap is safe.
     *
     * A returning person is exactly the person a decline family fires on, so almost every
     * gated family is withheld twice. Re-entry wins, because it is unconditional and runs
     * first, and it does not matter which wins: both remove the same selection. What would
     * matter is the second branch speaking in the withheld family's place, because a
     * sentence about a familiar stretch of low weeks said on the first report back is the
     * absence measured in a kinder vocabulary. So the rhythm family is withheld too.
     */
    @Test
    fun `the rhythm family is withheld for the week after a return as well`() {
        assertTrue(
            "otherwise the capacity gate would replace a withheld decline line with a rhythm " +
                "line that says the same forbidden thing",
            FamiliesAwaitingLanguage.FAMILIAR_DIP in FamilyAvailability.WITHHELD_ON_RE_ENTRY,
        )
    }

    // ------------------------------------------------------------------ the tables

    /** Every family either gate names is a family that exists, or is reserved for phase 9. */
    @Test
    fun `every gated family key names a declared or reserved family`() {
        val declared = Purpose.entries.flatMap { EngineFamilies.keysFor(it) }.toSet() +
            FamiliesAwaitingLanguage.KEYS
        val unknown = (FamilyAvailability.WITHHELD_ON_RE_ENTRY.keys + FamilyAvailability.PRECEDENT_GATED.keys)
            .filterNot { it in declared }
        assertEquals(
            "a gate on a family key nothing declares is a gate that silently does nothing",
            emptyList<FamilyKey>(),
            unknown,
        )
    }

    /**
     * The capacity gate is a subset of the re-entry set, and the subset relation is not a
     * coincidence: an absence produces a fall, a silence or a gap, and 14b.9 gates the
     * falls and the silences among them.
     */
    @Test
    fun `every family the capacity gate closes is also withheld after a return`() {
        val missing = FamilyAvailability.PRECEDENT_GATED.keys
            .filterNot { it in FamilyAvailability.WITHHELD_ON_RE_ENTRY }
        assertEquals(emptyList<FamilyKey>(), missing)
    }

    /**
     * The reservation says enough for phase 9 to author against without asking.
     *
     * A register whose entries nothing reads is a comment in the shape of a data structure.
     * This is what makes the difference: the key, the purpose, the cooldown, the corpus
     * prefix and the constraints the bench is written under all have to be there, and every
     * declared rule has to point at the family the entry names.
     */
    @Test
    fun `the reserved family is declared completely enough to author against`() {
        val reservation = FamiliesAwaitingLanguage.FAMILIES.single()
        assertEquals(FamiliesAwaitingLanguage.FAMILIAR_DIP, reservation.key)
        assertEquals(Purpose.REPORT_OBSERVATION, reservation.purpose)
        assertEquals(EngineFamilies.REPORT_DEFAULT_COOLDOWN_DAYS, reservation.cooldownDays)
        assertTrue("a bench needs a key prefix", reservation.keyPrefix.isNotBlank())
        assertTrue("an entry with no citation is a comment", reservation.citation.isNotBlank())
        assertTrue("an entry with no reason is a parking space", reservation.why.isNotBlank())
        assertTrue(
            "the constraints are half of what phase 9 is being handed",
            FamiliesAwaitingLanguage.FAMILIAR_DIP_CONSTRAINTS.size >= 4,
        )
        assertTrue(
            "every reserved rule points at the family the entry names",
            FamiliesAwaitingLanguage.RULES.all { it.family == reservation.key },
        )
        assertTrue(
            "a reserved rule key may not collide with one the catalog already carries",
            FamiliesAwaitingLanguage.RULES.none { rule ->
                CorpusFixture.catalog.rules.any { it.key == rule.key }
            },
        )
        assertTrue(
            "the family is not declared in EngineFamilies, because the corpus parser " +
                "fails on a declared family with no bench",
            Purpose.entries.none { reservation.key in EngineFamilies.keysFor(it) },
        )
    }

    /** Every subject the gate reads has a rhythm rule to answer with, and no more than that. */
    @Test
    fun `the second branch covers every subject the gate closes on`() {
        val covered = FamiliesAwaitingLanguage.RULES.map { it.key.substringAfterLast('.') }.toSet()
        assertEquals(
            "a gate that closes on a subject with no rhythm rule behind it is a family " +
                "excluded with nothing put in its place, and a rhythm rule for a subject " +
                "nothing gates is a sentence with no gate to relieve",
            FamilyAvailability.PRECEDENT_GATED.values.map { it.name.lowercase() }.toSet(),
            covered,
        )
        assertTrue(
            "and none of them is in the catalog yet, because the language is phase 9's",
            CorpusFixture.catalog.rules.none { it.family == FamiliesAwaitingLanguage.FAMILIAR_DIP },
        )
    }
}
