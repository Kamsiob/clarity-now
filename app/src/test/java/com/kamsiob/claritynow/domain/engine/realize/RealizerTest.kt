package com.kamsiob.claritynow.domain.engine.realize

import com.kamsiob.claritynow.domain.engine.FactSet
import com.kamsiob.claritynow.domain.engine.FiringHistory
import com.kamsiob.claritynow.domain.engine.PartOfDay
import com.kamsiob.claritynow.domain.engine.catalog.ClarityRule
import com.kamsiob.claritynow.domain.engine.catalog.CorpusFixture
import com.kamsiob.claritynow.domain.engine.catalog.LengthBand
import com.kamsiob.claritynow.domain.engine.catalog.Purpose
import com.kamsiob.claritynow.domain.engine.catalog.Register
import com.kamsiob.claritynow.domain.engine.catalog.Subject
import com.kamsiob.claritynow.domain.engine.catalog.SubjectKind
import com.kamsiob.claritynow.domain.engine.select.Selection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Layer 4 against the real corpus. CLARITY_LOGIC_ENGINE.md 7.
 *
 * **These run against the committed corpus files rather than a fixture**, for the same
 * reason the catalog tests do. A realizer tested against three invented lines proves the
 * template code and nothing about the 1,519 lines the app will actually read, and the
 * failure this suite exists to catch is a family whose whole bench turns out to be
 * unfillable from the facts 3.1 declares.
 */
class RealizerTest {

    private val catalog = CorpusFixture.catalog

    private val realizer = Realizer(catalog, EngineFacts.ZONE)

    private val morning = EngineMoment(EngineFacts.dateKey(1), PartOfDay.MORNING)

    /** Enough days for the variant hash to walk a bench of seventy. */
    private val NAMING_DAYS = 40

    /**
     * Enough days for the register chooser to be seen choosing.
     *
     * A voice is one of three, so a handful of days proves nothing: three registers over
     * ten days come up one-voiced about one run in twenty thousand, and over three days
     * about one in nine. A season is the shortest window where a frozen chooser cannot
     * pass by luck.
     */
    private val SEASON_DAYS = 60

    private fun rule(key: String): ClarityRule =
        catalog.rules.firstOrNull { it.key == key } ?: error("no rule $key in the catalog")

    private fun selection(key: String, subject: Subject? = null, windowDays: Int = 1) =
        Selection(rule(key), rule(key).purpose.first(), subject, callback = null, windowDays = windowDays)

    private fun realize(
        key: String,
        facts: FactSet,
        subject: Subject? = null,
        history: FiringHistory = FiringHistory.EMPTY,
        moment: EngineMoment = morning,
        options: RealizationOptions = RealizationOptions(),
    ): Realization = realizer.realize(selection(key, subject), facts, history, moment, options)

    private fun rendered(realization: Realization): Candidate {
        assertTrue(
            "expected a sentence and got: ${(realization as? Realization.NotProducible)?.reason}",
            realization is Realization.Rendered,
        )
        return (realization as Realization.Rendered).candidate
    }

    // ------------------------------------------------------------------ scenarios

    private val persistenceFacts: FactSet
        get() {
            val work = EngineFacts.area(
                areaId = "work", name = "Work", events = 4, completions = 1, share = 1.0,
                activeItemId = "item-1", activeItemTitle = "Rewrite the proposal intro",
                activeItemAgeDays = 9, queueLength = 2,
            )
            return EngineFacts.factSet(
                window = EngineFacts.window(totalEvents = 4, completions = 1, activeDays = 1),
                areas = listOf(work),
                dominantAreaId = "work",
            )
        }

    private val item = Subject("item-1", SubjectKind.ITEM)

    private val workArea = Subject("work", SubjectKind.AREA)

    // ------------------------------------------------------------------ the tests

    @Test
    fun `a persistence pulse renders a statement, a question and two answers`() {
        val candidate = rendered(realize("pulse.persistence.s2", persistenceFacts, item))
        assertEquals("persistence", candidate.familyKey)
        assertEquals(2, candidate.stage)
        assertTrue(candidate.variantKey.startsWith("persistence.s2."))
        assertFalse("a marker reached the screen: ${candidate.rendered}", '{' in candidate.rendered)
        assertTrue(candidate.rendered.isNotBlank())
        assertTrue(
            "a Pulse that states without asking is not a Pulse",
            !candidate.renderedQuestion.isNullOrBlank(),
        )
        assertEquals(2, candidate.responses.size)
        assertTrue("the first response of a pair is the positive one", candidate.responses.first().isPositive)
        assertFalse(candidate.responses.last().isPositive)
    }

    /**
     * Written as an invariant over many days rather than over one, because the bench moved.
     *
     * It used to assert that one realization of `persistence` stage 2 named the item, which
     * held while every line of an eighteen line bench carried `{itemTitle}`. Phase 9 grew that
     * bench past sixty and some of the new lines name the area or nothing at all, exactly as
     * `persistence.s2.15` already did, so which line the hash picks decides whether an item is
     * named. What the validator actually needs is the correspondence: an id is recorded when
     * and only when the sentence carries the name.
     *
     * **The area is named `Allotment` here and nowhere else in this file, and that is the
     * whole point.** The correspondence is checked by looking for the area's name inside the
     * rendered string, so the name has to be one that cannot arrive any other way.
     * `persistence.s2.56` reads `Work has happened elsewhere.`, where `Work` is the common
     * noun at the head of a sentence, and against a fixture area called `Work` that reads as
     * a named area the candidate never recorded. The line is correct, the fixture was not,
     * and it went unseen for as long as it did because the register was chosen by a list:
     * that line is `[O]`, the morning Pulse always took `[P]`, and no test in the build could
     * reach it.
     */
    @Test
    fun `the item and the area a sentence names are recorded for the validator`() {
        val allotment = EngineFacts.area(
            areaId = "allotment", name = "Allotment", events = 4, completions = 1, share = 1.0,
            activeItemId = "item-1", activeItemTitle = "Rewrite the proposal intro",
            activeItemAgeDays = 9, queueLength = 2,
        )
        val facts = EngineFacts.factSet(
            window = EngineFacts.window(totalEvents = 4, completions = 1, activeDays = 1),
            areas = listOf(allotment),
            dominantAreaId = "allotment",
        )
        var named = 0
        for (day in 1..NAMING_DAYS) {
            val moment = EngineMoment(EngineFacts.dateKey(day), PartOfDay.MORNING)
            val candidate = rendered(realize("pulse.persistence.s2", facts, item, moment = moment))
            val carriesTitle = "Rewrite the proposal intro" in candidate.rendered
            if (carriesTitle) named++
            assertEquals(
                "the recorded item ids have to match the names in `${candidate.rendered}`",
                if (carriesTitle) setOf("item-1") else emptySet(),
                candidate.namedItemIds,
            )
            assertTrue(candidate.namedAreaIds.all { it == "allotment" })
            assertEquals(
                "the recorded area ids have to match the names in `${candidate.rendered}`",
                if ("Allotment" in candidate.rendered) setOf("allotment") else emptySet(),
                candidate.namedAreaIds,
            )
        }
        assertTrue(
            "no sampled day produced a sentence naming the item, so the correspondence above " +
                "was never tested in the direction that matters",
            named > 0,
        )
    }

    @Test
    fun `every number rendered carries a fact that re-reads to the same value`() {
        val facts = persistenceFacts
        for (day in 0 until 60) {
            val candidate = rendered(
                realize(
                    "pulse.persistence.s2",
                    facts,
                    item,
                    moment = EngineMoment(EngineFacts.dateKey(day), PartOfDay.MORNING),
                ),
            )
            for ((key, slot) in candidate.slots) {
                val expected = slot.numericValue ?: continue
                val ref = requireNotNull(candidate.sourceFacts[key]) { "$key rendered $expected with no FactRef" }
                assertEquals(
                    "${candidate.variantKey} slot $key",
                    expected,
                    FactLookup.readNumber(facts, ref, EngineFacts.ZONE),
                )
            }
        }
    }

    @Test
    fun `the same day and the same facts always produce the same line`() {
        val facts = persistenceFacts
        val first = rendered(realize("pulse.persistence.s2", facts, item))
        val second = rendered(Realizer(catalog, EngineFacts.ZONE).realize(selection("pulse.persistence.s2", item), facts, FiringHistory.EMPTY, morning))
        assertEquals(first.variantKey, second.variantKey)
        assertEquals(first.rendered, second.rendered)
    }

    @Test
    fun `a line used inside ninety days is not said again`() {
        val facts = persistenceFacts
        val first = rendered(realize("pulse.persistence.s2", facts, item))
        val history = FiringHistory(
            variantsUsed = mapOf(first.variantKey to EngineFacts.dateKey(0)),
            lastStageBySubject = emptyMap(),
            lastFiredBySubject = emptyMap(),
            lastPulseFamily = null,
        )
        val second = rendered(realize("pulse.persistence.s2", facts, item, history = history))
        assertNotEquals(first.variantKey, second.variantKey)
    }

    @Test
    fun `a ladder never steps back down while the condition is still true`() {
        // Stage 3 was shown about this item two days ago. A promotion reset the age, so the
        // stage 1 rule now qualifies. 7.3 says the ladder must not go backwards, and saying
        // nothing is the only honest way to obey it: the stage 3 bench is authored around
        // fourteen days and this item is at four.
        val work = EngineFacts.area(
            areaId = "work", name = "Work", events = 3, share = 1.0,
            activeItemId = "item-1", activeItemTitle = "Rewrite the proposal intro", activeItemAgeDays = 4,
        )
        val facts = EngineFacts.factSet(
            window = EngineFacts.window(totalEvents = 3),
            areas = listOf(work),
            dominantAreaId = "work",
        )
        val history = FiringHistory(
            variantsUsed = emptyMap(),
            lastStageBySubject = mapOf(("persistence" to "item-1") to 3),
            lastFiredBySubject = mapOf(("persistence" to "item-1") to EngineFacts.dateKey(0)),
            lastPulseFamily = null,
        )
        val moment = EngineMoment(EngineFacts.dateKey(2), PartOfDay.MORNING)
        val result = realize("pulse.persistence.s1", facts, item, history = history, moment = moment)
        assertTrue(result is Realization.NotProducible)
    }

    @Test
    fun `a ladder resets once the condition has been gone long enough`() {
        val work = EngineFacts.area(
            areaId = "work", name = "Work", events = 3, share = 1.0,
            activeItemId = "item-1", activeItemTitle = "Rewrite the proposal intro", activeItemAgeDays = 4,
        )
        val facts = EngineFacts.factSet(
            window = EngineFacts.window(totalEvents = 3),
            areas = listOf(work),
            dominantAreaId = "work",
        )
        val history = FiringHistory(
            variantsUsed = emptyMap(),
            lastStageBySubject = mapOf(("persistence" to "item-1") to 3),
            lastFiredBySubject = mapOf(("persistence" to "item-1") to EngineFacts.dateKey(0)),
            lastPulseFamily = null,
        )
        val muchLater = EngineMoment(EngineFacts.dateKey(40), PartOfDay.MORNING)
        val candidate = rendered(realize("pulse.persistence.s1", facts, item, history = history, moment = muchLater))
        assertEquals(1, candidate.stage)
    }

    @Test
    fun `an unflattering rule speaks in the neutral agent register where the corpus has one`() {
        val quiet = EngineFacts.area(
            areaId = "personal", name = "Personal", ageDays = 300, lifetimeEvents = 40,
            queueLength = 3, daysSinceLastEvent = 20,
        )
        val work = EngineFacts.area(areaId = "work", name = "Work", events = 9, share = 1.0)
        val facts = EngineFacts.factSet(
            window = EngineFacts.window(startDay = 0, endDay = 7, totalEvents = 9, completions = 4),
            areas = listOf(quiet, work),
            dominantAreaId = "work",
        )
        val candidate = rendered(
            realizeWeek(
                "report.observation.neglectedArea.s2",
                facts,
                Subject("personal", SubjectKind.AREA),
                windowDays = 7,
            ),
        )
        assertEquals(Register.NEUTRAL_AGENT, candidate.register)
        assertFalse('{' in candidate.rendered)
    }

    /**
     * The defect this suite could not see, and the reason it could not.
     *
     * Every other test here asks the realizer for one sentence on one day and checks the
     * sentence. The register was decided by a list, so a stage of sixty lines in three
     * voices handed back the same voice on every one of those days, and no single day's
     * assertion could tell that from a bench doing its job. It takes a run of days.
     */
    @Test
    fun `a Momentum family is heard in every voice its bench holds, across a season`() {
        val facts = EngineFacts.factSet(
            window = EngineFacts.window(
                startDay = 0, endDay = 14, totalEvents = 20, completions = 9, additions = 8, activeDays = 10,
            ),
            areas = listOf(
                EngineFacts.area("work", "Work", events = 12, completions = 6, share = 0.6),
                EngineFacts.area("health", "Health", events = 8, completions = 3, share = 0.4),
            ),
            dominantAreaId = "work",
            history = EngineFacts.history(daysSinceInstall = 120),
        )
        val heard = (0 until SEASON_DAYS)
            .map { day ->
                rendered(
                    realize(
                        "momentum.steadyStretch", facts,
                        moment = EngineMoment(EngineFacts.dateKey(day), PartOfDay.MORNING),
                    ),
                ).register
            }
            .toSet()
        assertEquals(
            "the open tier in 7.4 step 4 offers three voices and this family authored all three",
            setOf(Register.REFLECTIVE, Register.OBSERVATIONAL, Register.PLAIN),
            heard,
        )
    }

    /** The morning Pulse hears both of the voices 7.4 step 2 names, not only the first one. */
    @Test
    fun `the morning Pulse is heard in both of the plainer voices`() {
        val heard = (0 until SEASON_DAYS)
            .map { day ->
                rendered(
                    realize(
                        "pulse.persistence.s2", persistenceFacts, item,
                        moment = EngineMoment(EngineFacts.dateKey(day), PartOfDay.MORNING),
                    ),
                ).register
            }
            .toSet()
        assertEquals(setOf(Register.PLAIN, Register.OBSERVATIONAL), heard)
    }

    /**
     * A voice with nothing sayable in it falls through to the next one rather than to
     * silence, which is the property 7.4 step 4 exists to have.
     *
     * Read against `persistentItem` at stage 1, whose rule carries no flag and whose bench
     * has no reflective line, because `CORPUS_2_REPORT.md` authors none anywhere. The open
     * tier offers `REFLECTIVE` on every one of these days, it is empty on every one of them,
     * and every one of them still speaks, in both of the voices that are left.
     */
    @Test
    fun `a register with nothing at this stage falls through rather than going silent`() {
        val stage = requireNotNull(
            catalog.familiesFor(Purpose.REPORT_OBSERVATION).first { it.key == "persistentItem" }.stage(1),
        )
        assertTrue(
            "this test is vacuous unless the bench really has no reflective line",
            stage.variants.none { it.register == Register.REFLECTIVE },
        )
        val heard = (0 until SEASON_DAYS).map { day ->
            rendered(
                realizer.realize(
                    selection("report.observation.persistentItem.low", item, windowDays = 7),
                    persistenceFacts,
                    FiringHistory.EMPTY,
                    EngineMoment(EngineFacts.dateKey(day), PartOfDay.MORNING),
                ),
            ).register
        }.toSet()
        assertEquals(setOf(Register.OBSERVATIONAL, Register.PLAIN), heard)
    }

    @Test
    fun `the previous lead's length band is avoided where the bench allows it`() {
        val facts = persistenceFacts
        val plain = rendered(realize("pulse.persistence.s2", facts, item))
        val avoided = rendered(
            realize("pulse.persistence.s2", facts, item, options = RealizationOptions(avoidBand = plain.lengthBand)),
        )
        assertNotEquals(plain.lengthBand, avoided.lengthBand)
    }

    @Test
    fun `a band with nothing outside it still speaks rather than going silent for rhythm`() {
        val facts = persistenceFacts
        val bands = LengthBand.entries.map { band ->
            realize("pulse.persistence.s2", facts, item, options = RealizationOptions(avoidBand = band))
        }
        assertTrue(bands.all { it is Realization.Rendered })
    }

    /**
     * Read against `burst`, whose every line names its area, rather than against
     * `persistence`.
     *
     * The bench this needs is one where no line can be filled once the subject is gone, and
     * `persistence` stopped being that bench: `persistence.s3.13` was already marker free
     * before phase 9 and the lines phase 9 added include more, so a stage with no active item
     * would now render a sentence about items in general instead of falling silent. Both
     * `burst` benches carry a marker in every line and 11.1 sizes the family in the tier below
     * hot, so nothing in this phase grows it out from under this test.
     */
    @Test
    fun `a rule whose bench cannot be filled says so rather than rendering a marker`() {
        // No areas anywhere, so every burst line loses the subject its markers read.
        val facts = EngineFacts.factSet(window = EngineFacts.window(totalEvents = 3))
        val result = realize("pulse.burst.s1", facts, workArea)
        assertTrue(result is Realization.NotProducible)
    }

    /** The Report window is seven days, and both the ladder and the horizon read it. */
    private fun realizeWeek(
        key: String,
        facts: FactSet,
        subject: Subject?,
        windowDays: Int,
    ): Realization = realizer.realize(
        selection(key, subject, windowDays = windowDays),
        facts,
        FiringHistory.EMPTY,
        morning,
    )

    @Test
    fun `every Pulse family with a rule can be realized from facts that qualify it`() {
        for ((key, scenario) in pulseScenarios()) {
            val candidate = rendered(realize(key, scenario.facts, scenario.subject))
            assertFalse("$key rendered a marker: ${candidate.rendered}", '{' in candidate.rendered)
            assertTrue("$key rendered nothing", candidate.rendered.isNotBlank())
        }
    }

    /**
     * The family whose whole bench is `[N]`, and the rule that now reaches it.
     *
     * `bn.quiet` is authored entirely in the neutral agent register because
     * `CORPUS_3_MOMENTUM.md` authoring rule 5 puts every quiet or low activity line there,
     * and 7.4 reaches that register only through a rule marked `unflattering`. Before the
     * widening in 14b.10 and Addendum 01 7c, no Momentum or banner rule carried the flag, so
     * the family qualified on real windows and the realizer returned `NotProducible` every
     * time. This asserts both halves of the repair: the family is still the only one on
     * these two surfaces written wholly in one gated register, and the catalog now marks it.
     */
    @Test
    fun `the family written wholly in the neutral agent register is the one 7_4 marks unflattering`() {
        val surfaces = catalog.familiesFor(Purpose.MOMENTUM_HEADLINE) + catalog.familiesFor(Purpose.AREAS_BANNER)
        val wholeBenchIsGated = surfaces
            .filter { family -> family.allVariants.all { it.register == Register.NEUTRAL_AGENT } }
            .map { it.key }
            .toSet()
        assertEquals(setOf("weekQuiet"), wholeBenchIsGated)
        val marked = catalog.rules.filter { it.unflattering }.map { it.family }.toSet()
        assertTrue(
            "a family whose every line is neutral agent can only be reached by a rule marked " +
                "unflattering, and without one it qualifies and says nothing",
            wholeBenchIsGated.all { it in marked },
        )
    }

    /** The same claim from the realizer's end: it renders, in the register nothing could ask for. */
    @Test
    fun `the banner's quiet week renders now that a rule marks it`() {
        val rule = catalog.rulesFor(Purpose.AREAS_BANNER).first { it.family == "weekQuiet" }
        val quietWeek = EngineFacts.factSet(
            window = EngineFacts.window(startDay = 0, endDay = 3, totalEvents = 1, completions = 0, activeDays = 1),
            areas = listOf(EngineFacts.area("work", "Work", events = 1, share = 1.0)),
            dominantAreaId = "work",
        )
        val candidate = rendered(
            realizer.realize(
                Selection(rule, Purpose.AREAS_BANNER, null, null, windowDays = 7),
                quietWeek,
                FiringHistory.EMPTY,
                morning,
            ),
        )
        assertEquals(Register.NEUTRAL_AGENT, candidate.register)
        assertFalse('{' in candidate.rendered)
    }

    private data class Scenario(val facts: FactSet, val subject: Subject?)

    /** One qualifying fact set per Pulse rule, so a bench that cannot be filled is visible. */
    private fun pulseScenarios(): Map<String, Scenario> {
        val threeAreas = listOf(
            EngineFacts.area("work", "Work", events = 3, completions = 1, share = 0.375),
            EngineFacts.area("health", "Health", events = 3, completions = 1, share = 0.375),
            EngineFacts.area("home", "Home", events = 2, completions = 1, share = 0.25),
        )
        return mapOf(
            "pulse.persistence.s2" to Scenario(persistenceFacts, item),
            "pulse.concentration.s1" to Scenario(
                EngineFacts.factSet(
                    window = EngineFacts.window(totalEvents = 8, completions = 3, activeDays = 1),
                    areas = listOf(
                        EngineFacts.area("work", "Work", events = 6, completions = 3, share = 0.75),
                        EngineFacts.area("health", "Health", events = 2, completions = 1, share = 0.25),
                    ),
                    dominantAreaId = "work",
                ),
                workArea,
            ),
            "pulse.accumulation.s1" to Scenario(
                EngineFacts.factSet(
                    window = EngineFacts.window(totalEvents = 7, completions = 2, additions = 5, activeDays = 1),
                    areas = listOf(EngineFacts.area("work", "Work", events = 7, completions = 2, additions = 5, share = 1.0, queueLength = 4)),
                    dominantAreaId = "work",
                ),
                null,
            ),
            "pulse.throughput.s1" to Scenario(
                EngineFacts.factSet(
                    window = EngineFacts.window(totalEvents = 7, completions = 4, additions = 3, activeDays = 1),
                    areas = listOf(EngineFacts.area("work", "Work", events = 7, completions = 4, additions = 3, share = 1.0)),
                    dominantAreaId = "work",
                ),
                null,
            ),
            "pulse.spread.s1" to Scenario(
                EngineFacts.factSet(
                    window = EngineFacts.window(totalEvents = 8, completions = 3, activeDays = 1),
                    areas = threeAreas,
                ),
                null,
            ),
            "pulse.burst.s1" to Scenario(
                EngineFacts.factSet(
                    window = EngineFacts.window(totalEvents = 4, completions = 3, activeDays = 1),
                    areas = listOf(EngineFacts.area("work", "Work", events = 4, completions = 3, share = 1.0)),
                    dominantAreaId = "work",
                ),
                workArea,
            ),
            "pulse.queueDrain.s1" to Scenario(
                EngineFacts.factSet(
                    window = EngineFacts.window(totalEvents = 4, completions = 3, activeDays = 1),
                    areas = listOf(
                        EngineFacts.area("work", "Work", events = 4, completions = 3, share = 1.0, queueLengthAtWindowStart = 3),
                    ),
                    dominantAreaId = "work",
                ),
                workArea,
            ),
            "pulse.freshStart.s1" to Scenario(
                EngineFacts.factSet(
                    window = EngineFacts.window(totalEvents = 2, additions = 2, activeDays = 1),
                    areas = listOf(
                        EngineFacts.area(
                            "garden", "Garden", events = 2, additions = 2, share = 1.0, ageDays = 2,
                            activeItemId = "item-9", activeItemTitle = "Order the bulbs", activeItemAgeDays = 1,
                        ),
                    ),
                    dominantAreaId = "garden",
                ),
                Subject("garden", SubjectKind.AREA),
            ),
            "pulse.quietDay.s1" to Scenario(
                EngineFacts.factSet(
                    window = EngineFacts.window(totalEvents = 1, completions = 1, activeDays = 1),
                    areas = listOf(EngineFacts.area("work", "Work", events = 1, completions = 1, share = 1.0)),
                    dominantAreaId = "work",
                ),
                null,
            ),
        )
    }

    @Test
    fun `every Momentum and banner family with a rule can be realized`() {
        val areas = listOf(
            EngineFacts.area("work", "Work", events = 12, completions = 6, share = 0.6, queueLength = 2),
            EngineFacts.area("health", "Health", events = 8, completions = 3, share = 0.4, queueLength = 1),
        )
        val fortnight = EngineFacts.factSet(
            window = EngineFacts.window(
                startDay = 0, endDay = 14, totalEvents = 20, completions = 9, additions = 8,
                focusCompleted = 3, focusMinutes = 75, activeDays = 10,
            ),
            areas = areas,
            dominantAreaId = "work",
            history = EngineFacts.history(
                daysSinceInstall = 120,
                weekCompletions = listOf(2, 3, 4, 5),
                mostRecentBetterWeekKey = "2026-01-11",
            ),
        )
        val rules = catalog.rulesFor(Purpose.MOMENTUM_HEADLINE) + catalog.rulesFor(Purpose.AREAS_BANNER)
        for (rule in rules) {
            val subject = if (rule.family == "comeback" || rule.family == "singleAreaWeek") workArea else null
            val result = realizer.realize(
                Selection(rule, rule.purpose.first(), subject, null, windowDays = 14),
                fortnight,
                FiringHistory.EMPTY,
                morning,
            )
            val candidate = rendered(result)
            assertFalse("${rule.key} rendered a marker: ${candidate.rendered}", '{' in candidate.rendered)
        }
    }
}
