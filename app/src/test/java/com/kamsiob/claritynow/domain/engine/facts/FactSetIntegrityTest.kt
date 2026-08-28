package com.kamsiob.claritynow.domain.engine.facts

import com.kamsiob.claritynow.domain.engine.AreaFacts
import com.kamsiob.claritynow.domain.engine.CueFacts
import com.kamsiob.claritynow.domain.engine.FactExtractor
import com.kamsiob.claritynow.domain.engine.FactSet
import com.kamsiob.claritynow.domain.engine.HistoryFacts
import com.kamsiob.claritynow.domain.engine.ItemFacts
import com.kamsiob.claritynow.domain.engine.PulseFacts
import com.kamsiob.claritynow.domain.engine.RollupFacts
import com.kamsiob.claritynow.domain.engine.WindowFacts
import com.kamsiob.claritynow.domain.query.TrailTestLog
import com.kamsiob.claritynow.domain.query.area
import com.kamsiob.claritynow.domain.query.at
import com.kamsiob.claritynow.domain.query.complete
import com.kamsiob.claritynow.domain.query.dateKey
import com.kamsiob.claritynow.domain.query.item
import com.kamsiob.claritynow.domain.query.promote
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.lang.reflect.Modifier

/**
 * The prohibitions in CLARITY_LOGIC_ENGINE.md 1.1 and 3.1, checked rather than
 * trusted.
 *
 * Each of these guards a failure that would be invisible on screen. A phantom area
 * looks like an area. A superlative that used `>=` looks like a superlative. A
 * streak fact looks like arithmetic until somebody writes a sentence with it.
 */
class FactSetIntegrityTest {

    private fun TrailTestLog.finish(day: Int, id: String, hour: Int = 9) {
        item(at(day, hour), id, "work", "Item $id")
        promote(at(day, hour), id, "work", "Item $id")
        complete(at(day, hour), id, "work", "Item $id", activeDurationDays = 1)
    }

    private fun extract(log: TrailTestLog, fromDay: Int, toDay: Int): FactSet =
        FactExtractor(log.queries()).extract(window(fromDay, toDay))

    // Prohibition 3: never reference an archived or deleted entity -------------

    @Test
    fun `an archived area is absent from the facts entirely`() {
        val log = TrailTestLog()
        log.area(at(0, 9), "work", "Work", orderKey = "a0")
        log.area(at(0, 9, 1), "old", "Old", orderKey = "a1")
        log.item(at(1, 10), "o1", "old", "Something", areaName = "Old")
        log.archiveArea(day = 2, areaId = "old", name = "Old")

        val facts = extract(log, 1, 7)
        assertFalse("an archived area must not reach a rule", "old" in facts.areas)
        assertEquals(setOf("work"), facts.areas.keys)
        assertEquals(1, facts.rollup.areasTotal)
    }

    @Test
    fun `a tombstoned area is absent from the facts entirely`() {
        val log = TrailTestLog()
        log.area(at(0, 9), "work", "Work", orderKey = "a0")
        log.area(at(0, 9, 1), "gone", "Gone", orderKey = "a1")
        log.item(at(1, 10), "g1", "gone", "Something", areaName = "Gone")
        log.deleteArea(day = 2, areaId = "gone", name = "Gone")

        val facts = extract(log, 1, 7)
        assertFalse("a tombstoned area must not reach a rule", "gone" in facts.areas)
        assertEquals(setOf("work"), facts.areas.keys)
    }

    @Test
    fun `a completion whose item was later deleted is not named`() {
        val log = TrailTestLog()
        log.area(at(0, 9), "work", "Work")
        log.item(at(1, 10), "i1", "work", "Kept")
        log.promote(at(1, 10, 1), "i1", "work", "Kept")
        log.complete(at(2, 10), "i1", "work", "Kept", activeDurationDays = 1)
        log.item(at(1, 11), "i2", "work", "Deleted later")
        log.promote(at(1, 11, 1), "i2", "work", "Deleted later")
        log.complete(at(2, 11), "i2", "work", "Deleted later", activeDurationDays = 1)
        log.deleteItem(day = 3, itemId = "i2", areaId = "work", title = "Deleted later")

        val facts = extract(log, 1, 7)
        assertEquals(listOf("i1"), facts.items.completedInWindow.map { it.itemId })
        // The count is a number rather than a name, and a number about something
        // deleted is still true, so it stays.
        assertEquals(2, facts.window.completions)
    }

    // 3.1: shares, superlatives and ties ---------------------------------------

    @Test
    fun `a share is zero rather than a division by zero when nothing happened`() {
        val log = TrailTestLog()
        log.area(at(0, 9), "work", "Work")

        val facts = extract(log, 10, 17)
        assertEquals(0, facts.window.totalEvents)
        val work = facts.areas.getValue("work")
        assertEquals(0, work.eventsInWindow)
        assertEquals(0.0, work.shareOfEvents, 0.0)
        assertFalse("a share must never be NaN", work.shareOfEvents.isNaN())
    }

    @Test
    fun `a dominant area is null on a tie`() {
        val log = TrailTestLog()
        log.area(at(0, 9), "work", "Work", orderKey = "a0")
        log.area(at(0, 9, 1), "health", "Health", orderKey = "a1")
        log.item(at(1, 10), "i1", "work", "One")
        log.item(at(1, 11), "h1", "health", "Two", areaName = "Health")

        val rollup = extract(log, 1, 7).rollup
        assertNull("two areas at one event each have no dominant one", rollup.dominantAreaId)
        assertEquals(0.0, rollup.dominantShare, 0.0)
    }

    /**
     * The strictly greater rule, which 13 names as a false superlative when it is
     * got backwards.
     *
     * Four weeks of completions: 3, 1, 5, then 2 in the week being described. The
     * newest earlier week that beats 2 is the one holding 5.
     */
    @Test
    fun `the most recent better week is the newest week that genuinely beats this one`() {
        val log = weeklyLog()
        val history = extract(log, 21, 28).history
        assertEquals(listOf(3, 1, 5, 2), history.weekCompletionsSeries)
        assertEquals(dateKey(14), history.mostRecentBetterWeekKey)
        assertEquals(5, history.personalBestWeekCompletions)
        assertEquals(dateKey(14), history.personalBestWeekKey)
        assertEquals(1, history.weeksSincePersonalBest)
        assertEquals(5, history.lastWeekCompletions)
        assertEquals(-3, history.weekOverWeekDelta)
    }

    @Test
    fun `no better week leaves the key null so the personal best family applies instead`() {
        val log = weeklyLog()
        for (day in 28..34) log.finish(day, "n$day")
        for (day in 28..29) log.finish(day, "m$day")

        val history = extract(log, 28, 35).history
        assertEquals(9, history.weekCompletionsSeries.last())
        assertNull(
            "nothing beat this week, so nothing may say since",
            history.mostRecentBetterWeekKey,
        )
        assertEquals(5, history.personalBestWeekCompletions)
        assertEquals(2, history.weeksSincePersonalBest)
    }

    /** Equal is not better. This is the whole difference between true and subtly false. */
    @Test
    fun `a week that only equals this one is not a better week`() {
        val log = TrailTestLog()
        log.area(at(0, 9), "work", "Work")
        for (index in 1..3) log.finish(day = 3, id = "a$index", hour = 9 + index)
        for (index in 1..3) log.finish(day = 10, id = "b$index", hour = 9 + index)

        val history = extract(log, 7, 14).history
        assertEquals(listOf(3, 3), history.weekCompletionsSeries)
        assertNull(history.mostRecentBetterWeekKey)
        assertEquals(3, history.personalBestWeekCompletions)
    }

    private fun weeklyLog(): TrailTestLog {
        val log = TrailTestLog()
        log.area(at(0, 9), "work", "Work")
        for (index in 1..3) log.finish(day = 2, id = "w0$index", hour = 9 + index)
        log.finish(day = 9, id = "w11")
        for (index in 1..5) log.finish(day = 16, id = "w2$index", hour = 9 + index)
        for (index in 1..2) log.finish(day = 23, id = "w3$index", hour = 9 + index)
        return log
    }

    // 3.1: the facts that must not exist ---------------------------------------

    private val factClasses = listOf(
        FactSet::class.java,
        WindowFacts::class.java,
        AreaFacts::class.java,
        RollupFacts::class.java,
        ItemFacts::class.java,
        HistoryFacts::class.java,
        PulseFacts::class.java,
        CueFacts::class.java,
    )

    private fun instanceFieldNames(): List<Pair<String, String>> = factClasses.flatMap { type ->
        type.declaredFields
            .filterNot { Modifier.isStatic(it.modifiers) }
            .map { type.simpleName to it.name }
    }

    /**
     * No streak fact exists, and none may be added.
     *
     * 3.1 is explicit that the absence is what makes streak language structurally
     * impossible rather than merely discouraged. A field scan is the only form of
     * that rule a build can enforce, because the moment the number exists somebody
     * three phases later writes a sentence with it in good faith.
     */
    @Test
    fun `no fact is a streak`() {
        val banned = Regex("streak|inarow|consecutive", RegexOption.IGNORE_CASE)
        val offenders = instanceFieldNames().filter { banned.containsMatchIn(it.second) }
        assertTrue(
            "a streak fact appeared in the fact classes: $offenders",
            offenders.isEmpty(),
        )
    }

    /**
     * The scoped exception, held to its scope by the build rather than by its own
     * documentation.
     *
     * The owner approved two runs of **absence**, on the reasoning that a run of
     * nothing has nothing to accumulate and nothing to break, so the loss aversion
     * the ban prevents cannot occur. The exception was granted in a shape: the
     * current run only, ending today, capped, with no per day series behind it. This
     * list is that shape. A third run, or a longest, or a best, fails here, and it
     * fails whatever it is called, because the name is not the thing that makes a run
     * safe.
     */
    @Test
    fun `the only run facts are the two capped current runs and the subject of one`() {
        val expected = listOf(
            "HistoryFacts" to "currentQuietRunDays",
            "HistoryFacts" to "currentSingleAreaRunAreaId",
            "HistoryFacts" to "currentSingleAreaRunDays",
        )
        val found = instanceFieldNames()
            .filter { it.second.contains("run", ignoreCase = true) }
            .sortedBy { it.second }
        assertEquals("a run fact appeared that nobody approved", expected, found)
    }

    /**
     * Neither run is a series, and neither may become one.
     *
     * A single capped current value cannot be used to reconstruct an active run,
     * which is what the ban actually protects against. A list of them could, and a
     * list would also let a later phase find the longest one, which is the record the
     * cap exists to make unreachable.
     */
    @Test
    fun `no run fact is a collection`() {
        val collections = factClasses.flatMap { type ->
            type.declaredFields
                .filterNot { Modifier.isStatic(it.modifiers) }
                .filter { it.name.contains("run", ignoreCase = true) }
                .filter {
                    Collection::class.java.isAssignableFrom(it.type) ||
                        Map::class.java.isAssignableFrom(it.type) ||
                        it.type.isArray
                }
                .map { "${type.simpleName}.${it.name}" }
        }
        assertTrue("a run fact was declared as a series: $collections", collections.isEmpty())
    }

    /**
     * Nothing in the fact classes is lazily evaluated.
     *
     * Section 3: "No lazy evaluation; a fact computed at validation time could
     * differ from the fact that fired the rule." A `by lazy` property compiles to a
     * field of type `kotlin.Lazy`, so the rule is checkable rather than reviewable.
     */
    @Test
    fun `no fact is lazily evaluated`() {
        val lazyFields = factClasses.flatMap { type ->
            type.declaredFields
                .filterNot { Modifier.isStatic(it.modifiers) }
                .filter { Lazy::class.java.isAssignableFrom(it.type) }
                .map { "${type.simpleName}.${it.name}" }
        }
        assertTrue("a fact was declared by lazy: $lazyFields", lazyFields.isEmpty())
    }

    /**
     * A `FactSet` does not change when the log grows underneath it.
     *
     * The value form of the same rule. The facts were read once, at construction,
     * against the log as it stood, and a later event cannot reach back into them.
     */
    @Test
    fun `a fact set does not move when more events arrive`() {
        val log = TrailTestLog()
        log.area(at(0, 9), "work", "Work")
        log.finish(day = 1, id = "i1")
        val before = extract(log, 1, 7)
        log.finish(day = 2, id = "i2")
        assertEquals(1, before.window.completions)
        assertEquals(2, extract(log, 1, 7).window.completions)
    }
}
