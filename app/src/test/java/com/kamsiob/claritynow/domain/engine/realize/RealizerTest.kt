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

    @Test
    fun `the item and the area a sentence names are recorded for the validator`() {
        val candidate = rendered(realize("pulse.persistence.s2", persistenceFacts, item))
        assertTrue(
            "every stage 2 line names the item, so the candidate has to carry it",
            candidate.namedItemIds == setOf("item-1"),
        )
        assertTrue(candidate.namedAreaIds.all { it == "work" })
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

    @Test
    fun `a rule whose bench cannot be filled says so rather than rendering a marker`() {
        // No active item anywhere, so every persistence line loses its subject.
        val facts = EngineFacts.factSet(window = EngineFacts.window(totalEvents = 3))
        val result = realize("pulse.persistence.s2", facts, item)
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

    /**
     * Families the register rule cannot reach, recorded rather than worked around.
     *
     * `bn.quiet` is authored entirely in the neutral agent register, because
     * `CORPUS_3_MOMENTUM.md` authoring rule 5 puts every quiet or low activity line there.
     * 7.4 reaches that register only through a rule marked `unflattering` and enumerates no
     * Momentum rule, so the family cannot speak. `MomentumRules` records the same conflict
     * from the other side and `MASTER_BUILD_PROMPT.md` 14b.10 marks the amendment pending
     * in phase 9. Widening the register rule here would pre-empt a decision that belongs to
     * that phase.
     */
    private val UNREACHABLE_UNDER_7_4 = setOf("weekQuiet")

    @Test
    fun `every Pulse family with a rule can be realized from facts that qualify it`() {
        for ((key, scenario) in pulseScenarios()) {
            val candidate = rendered(realize(key, scenario.facts, scenario.subject))
            assertFalse("$key rendered a marker: ${candidate.rendered}", '{' in candidate.rendered)
            assertTrue("$key rendered nothing", candidate.rendered.isNotBlank())
        }
    }

    @Test
    fun `the one family whose whole bench is out of reach is the one 7_4 has not been amended for`() {
        val unreachable = (catalog.familiesFor(Purpose.MOMENTUM_HEADLINE) + catalog.familiesFor(Purpose.AREAS_BANNER))
            .filter { family -> family.allVariants.all { it.register == Register.NEUTRAL_AGENT } }
            .map { it.key }
            .toSet()
        assertEquals(
            "a family whose every line is neutral agent can only be reached by a rule marked " +
                "unflattering, and 7.4 marks no Momentum rule. MomentumRules records the same " +
                "conflict, and Addendum 01 7c is the amendment that resolves it in phase 9",
            UNREACHABLE_UNDER_7_4,
            unreachable,
        )
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
        val rules = (catalog.rulesFor(Purpose.MOMENTUM_HEADLINE) + catalog.rulesFor(Purpose.AREAS_BANNER))
            .filterNot { it.family in UNREACHABLE_UNDER_7_4 }
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
