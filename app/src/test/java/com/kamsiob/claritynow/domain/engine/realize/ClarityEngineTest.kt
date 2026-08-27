package com.kamsiob.claritynow.domain.engine.realize

import com.kamsiob.claritynow.domain.engine.CandidateValidator
import com.kamsiob.claritynow.domain.engine.ClarityEngine
import com.kamsiob.claritynow.domain.engine.EngineResult
import com.kamsiob.claritynow.domain.engine.FactSet
import com.kamsiob.claritynow.domain.engine.FiringHistory
import com.kamsiob.claritynow.domain.engine.PartOfDay
import com.kamsiob.claritynow.domain.engine.SilenceReason
import com.kamsiob.claritynow.domain.engine.catalog.CorpusFixture
import com.kamsiob.claritynow.domain.engine.catalog.Purpose
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * The engine end to end, layers 3 to 5 in order. CLARITY_LOGIC_ENGINE.md 2.2 and 8.
 *
 * The validator is a test double here on purpose. Layer 5 has its own suite, and what this
 * one has to prove is the loop around it: that a veto sends the next ranked selection round
 * again, that everything being vetoed is told apart from nothing qualifying, and that the
 * same inputs produce the same sentence every time on any device.
 */
class ClarityEngineTest {

    private val catalog = CorpusFixture.catalog

    private fun engine(validator: CandidateValidator = CandidateValidator.ACCEPT_NOTHING_CHECKED) =
        ClarityEngine(catalog, validator, EngineFacts.ZONE)

    /** Work is concentrated and holds a nine day old item, so two families qualify at once. */
    private fun twoFamilies(): FactSet {
        val work = EngineFacts.area(
            areaId = "work", name = "Work", events = 6, completions = 3, share = 0.75,
            activeItemId = "item-1", activeItemTitle = "Rewrite the proposal intro", activeItemAgeDays = 9,
        )
        val health = EngineFacts.area(areaId = "health", name = "Health", events = 2, completions = 1, share = 0.25)
        return EngineFacts.factSet(
            window = EngineFacts.window(totalEvents = 8, completions = 3, activeDays = 1),
            areas = listOf(work, health),
            dominantAreaId = "work",
        )
    }

    @Test
    fun `an observation comes out whole, or not at all`() {
        val result = engine().observe(twoFamilies(), FiringHistory.EMPTY, Purpose.PULSE)
        assertTrue(result is EngineResult.Spoke)
        val output = (result as EngineResult.Spoke).output
        assertFalse('{' in output.text)
        assertTrue(output.text.isNotBlank())
        assertTrue("a Pulse always asks", !output.question.isNullOrBlank())
        assertTrue(output.responses.size >= 2)
        assertEquals(Purpose.PULSE, output.meta.purpose)
    }

    @Test
    fun `a vetoed candidate hands over to the next ranked selection`() {
        val unvetted = (engine().observe(twoFamilies(), FiringHistory.EMPTY, Purpose.PULSE) as EngineResult.Spoke).output
        val vetoTopFamily = CandidateValidator { candidate, _ ->
            if (candidate.familyKey == unvetted.meta.familyKey) "vetoed by the test" else null
        }
        val next = engine(vetoTopFamily).observe(twoFamilies(), FiringHistory.EMPTY, Purpose.PULSE)
        assertTrue(next is EngineResult.Spoke)
        assertNotEquals(unvetted.meta.familyKey, (next as EngineResult.Spoke).output.meta.familyKey)
    }

    @Test
    fun `everything vetoed is silence with a reason nobody has to guess at`() {
        val vetoEverything = CandidateValidator { _, _ -> "vetoed by the test" }
        val result = engine(vetoEverything).observe(twoFamilies(), FiringHistory.EMPTY, Purpose.PULSE)
        assertEquals(EngineResult.Silent(SilenceReason.ALL_CANDIDATES_VETOED), result)
    }

    @Test
    fun `nothing to describe is silence too, and a different reason`() {
        val empty = EngineFacts.factSet(history = EngineFacts.history(daysSinceInstall = 0, lifetimeCompletions = 0))
        assertEquals(
            EngineResult.Silent(SilenceReason.INSUFFICIENT_DATA),
            engine().observe(empty, FiringHistory.EMPTY, Purpose.PULSE),
        )
    }

    @Test
    fun `the moment comes from the window rather than from a clock`() {
        val morningReflection = EngineFacts.factSet(window = EngineFacts.window(startDay = 3, endDay = 4, endHour = 0))
        assertEquals(EngineFacts.dateKey(4), engine().momentOf(morningReflection).dateKey)
        assertEquals(
            "a window that ends on the day boundary is yesterday being read this morning",
            PartOfDay.MORNING,
            engine().momentOf(morningReflection).partOfDay,
        )

        val eveningReflection = EngineFacts.factSet(window = EngineFacts.window(startDay = 4, endDay = 4, endHour = 19))
        assertEquals(EngineFacts.dateKey(4), engine().momentOf(eveningReflection).dateKey)
        assertEquals(PartOfDay.EVENING, engine().momentOf(eveningReflection).partOfDay)
    }

    @Test
    fun `identical inputs produce identical output across ten thousand generated cases`() {
        val first = engine()
        val second = ClarityEngine(catalog, CandidateValidator.ACCEPT_NOTHING_CHECKED, EngineFacts.ZONE)
        var spoke = 0
        for (case in 0 until CASES) {
            val facts = generated(case)
            val history = FiringHistory.EMPTY
            val left = first.observe(facts, history, Purpose.PULSE)
            val right = second.observe(facts, history, Purpose.PULSE)
            assertEquals("case $case diverged", left, right)
            if (left is EngineResult.Spoke) spoke++
        }
        assertTrue("a generator that never produced a sentence would prove nothing", spoke > CASES / 10)
    }

    /**
     * Section 14's cross device agreement test, at the layer that decides.
     *
     * Two histories holding the same entries in a different order are what two devices
     * rebuild from one merged log: the log's total order is the same on both, but nothing
     * guarantees the order a map was filled in. If iteration order could reach the choice,
     * two phones would say different things about the same week and neither screen would
     * look wrong.
     */
    @Test
    fun `two histories rebuilt independently from the same log choose the same line`() {
        val used = listOf(
            "persistence.s2.01" to EngineFacts.dateKey(1),
            "persistence.s2.05" to EngineFacts.dateKey(2),
            "persistence.s2.11" to EngineFacts.dateKey(3),
            "concentration.s1.02" to EngineFacts.dateKey(4),
        )
        fun historyOf(order: List<Pair<String, String>>) = FiringHistory(
            variantsUsed = order.toMap(),
            lastStageBySubject = emptyMap(),
            lastFiredBySubject = emptyMap(),
            lastPulseFamily = null,
        )
        val facts = twoFamilies()
        val forwards = engine().observe(facts, historyOf(used), Purpose.PULSE)
        val backwards = engine().observe(facts, historyOf(used.reversed()), Purpose.PULSE)
        assertTrue("a silent pair would prove nothing", forwards is EngineResult.Spoke)
        assertEquals(forwards, backwards)
    }

    @Test
    fun `no engine source reads DataStore`() {
        val offenders = ENGINE_SOURCES
            .flatMap { path -> File(path).walkTopDown().filter { it.isFile && it.extension == "kt" } }
            .flatMap { file ->
                file.readLines().mapIndexedNotNull { index, line ->
                    val trimmed = line.trimStart()
                    val comment = trimmed.startsWith("//") || trimmed.startsWith("*") || trimmed.startsWith("/*")
                    if (!comment && "DataStore" in line) "${file.path}:${index + 1}" else null
                }
            }
        assertTrue(
            "engine state must derive from the log, because DataStore does not merge and two " +
                "devices would silently disagree:\n" + offenders.joinToString("\n"),
            offenders.isEmpty(),
        )
    }

    /**
     * A cheap deterministic generator.
     *
     * No `Random`, not even in a test. A failing case has to be reproducible from its
     * number alone, and a seeded generator whose implementation changes between Kotlin
     * versions would make an old failure unreproducible.
     */
    private fun generated(case: Int): FactSet {
        val events = case % 11
        val completions = (case / 11) % 5
        val additions = (case / 55) % 6
        val age = (case / 7) % 40
        val queue = (case / 13) % 5
        val work = EngineFacts.area(
            areaId = "work",
            name = "Work",
            events = events,
            completions = completions,
            additions = additions,
            share = if (events == 0) 0.0 else events.toDouble() / (events + 2),
            activeItemId = if (age > 0) "item-1" else null,
            activeItemTitle = if (age > 0) "Rewrite the proposal intro" else null,
            activeItemAgeDays = if (age > 0) age else null,
            queueLength = queue,
            queueLengthAtWindowStart = (case / 3) % 6,
        )
        val health = EngineFacts.area(
            areaId = "health", name = "Health", events = 2, completions = case % 3, share = 0.2,
        )
        return EngineFacts.factSet(
            window = EngineFacts.window(
                startDay = case % 5,
                endDay = case % 5 + 1,
                totalEvents = events + 2,
                completions = completions,
                additions = additions,
                activeDays = 1,
            ),
            areas = listOf(work, health),
            dominantAreaId = if (events > 2) "work" else null,
            history = EngineFacts.history(daysSinceInstall = 30 + case % 300),
        )
    }

    private companion object {

        /** Section 14's determinism test asks for ten thousand. */
        const val CASES = 10_000

        val ENGINE_SOURCES = listOf(
            "src/main/java/com/kamsiob/claritynow/domain/engine",
            "src/main/java/com/kamsiob/claritynow/domain/guidance",
        )
    }
}
