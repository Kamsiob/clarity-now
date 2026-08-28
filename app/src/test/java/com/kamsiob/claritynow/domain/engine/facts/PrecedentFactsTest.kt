package com.kamsiob.claritynow.domain.engine.facts

import com.kamsiob.claritynow.domain.engine.FactExtractor
import com.kamsiob.claritynow.domain.engine.FactSet
import com.kamsiob.claritynow.domain.engine.Precedent
import com.kamsiob.claritynow.domain.query.TrailTestLog
import com.kamsiob.claritynow.domain.query.TrailWindow
import com.kamsiob.claritynow.domain.query.area
import com.kamsiob.claritynow.domain.query.at
import com.kamsiob.claritynow.domain.query.item
import com.kamsiob.claritynow.domain.query.startOfDay
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Whether a fall has happened before. MASTER_BUILD_PROMPT 14b.9, Addendum 01 7b.
 *
 * **This is a correctness fix and not a politeness one, so the tests are about false
 * claims rather than about wording.** A fluctuating condition and a decline are the
 * same numbers. Without this fact the app tells a person with a cyclical or relapsing
 * condition that they are deteriorating, on a fixed schedule, forever, and every
 * report it says it in passes its own integrity rules. The first two tests are the
 * two people the fact has to tell apart, and they are the whole point of it.
 *
 * The rest are the ways of getting the answer wrong that would look right. Reporting
 * "no precedent" on a history too short to hold one is a false confidence a decline
 * family would then fire on, and it arrives by two different routes: a short history,
 * and a fall so long that no earlier fall of the same length could fit behind it.
 * Counting the empty weeks before a subject existed as silences hands every new area
 * a history of falls it was never in. And reading a week that has not finished puts
 * every subject into a fall every Wednesday morning.
 */
class PrecedentFactsTest {

    /** Sixteen weeks of history, ending on the last day of week fifteen. */
    private val fullYearish = 16

    private fun log(): TrailTestLog {
        val log = TrailTestLog()
        log.area(at(0, 8), "work", "Work")
        return log
    }

    /**
     * [levels] events in each seven day bucket, oldest first, one bucket per entry.
     *
     * [dayInWeek] positions them inside the bucket. It matters only for the part week
     * test, which needs a bucket that holds nothing because the week it covers has not
     * happened yet.
     */
    private fun TrailTestLog.weeks(
        areaId: String,
        levels: List<Int>,
        dayInWeek: Int = 3,
    ) {
        levels.forEachIndexed { weekIndex, level ->
            repeat(level) { n ->
                item(at(7 * weekIndex + dayInWeek, 9, n), "$areaId-$weekIndex-$n", areaId, "Thing")
            }
        }
    }

    /** The window covering the last week of a [weekCount] week log. */
    private fun lastWeekOf(weekCount: Int): TrailWindow =
        window(7 * weekCount - 7, 7 * weekCount)

    private fun factsFor(log: TrailTestLog, weekCount: Int): FactSet =
        FactExtractor(log.queries()).extract(lastWeekOf(weekCount))

    // ------------------------------------------------- the two people to tell apart

    /**
     * A person whose activity cycles is not declining, and the app must be able to
     * know that.
     *
     * Three good weeks and a bad one, four times over, ending on a bad one. Every
     * individual reading of that last week is true and every sentence built on it
     * would pass validation. The sequence of them is the false claim, and the only
     * thing that separates this person from the next test is what came before.
     */
    @Test
    fun `a fall that has happened three times before is a rhythm`() {
        val log = log()
        log.weeks("work", List(fullYearish) { if (it % 4 == 3) 1 else 10 })

        assertEquals(Precedent.PRESENT, factsFor(log, fullYearish).history.activityDipPrecedent)
    }

    /**
     * A first ever fall, after thirteen steady weeks, has no precedent and says so.
     *
     * The permission rather than the veto. If this answered anything but
     * [Precedent.NONE] the gate would suppress every decline family for everybody and
     * 14b.9 would have cost the app the observation instead of correcting it.
     */
    @Test
    fun `a first fall after a steady history has no precedent`() {
        val log = log()
        log.weeks("work", List(fullYearish) { if (it >= 13) 1 else 10 })

        assertEquals(Precedent.NONE, factsFor(log, fullYearish).history.activityDipPrecedent)
    }

    // ------------------------------------------------------ the two ways of not knowing

    /**
     * Six weeks of data is no precedent for anything, and that is not the same as no
     * precedent.
     *
     * 14b.9 says this in as many words. The distinction is the whole reason this fact
     * has four values rather than a boolean: the branch that fires a decline tests for
     * [Precedent.NONE] and the branch that fires a rhythm tests for
     * [Precedent.PRESENT], so a person in their second month gets neither sentence
     * rather than whichever one a boolean happened to round to.
     */
    @Test
    fun `six weeks of history cannot answer and does not pretend to`() {
        val log = log()
        log.weeks("work", List(6) { if (it == 5) 1 else 10 })

        assertEquals(Precedent.INSUFFICIENT, factsFor(log, 6).history.activityDipPrecedent)
    }

    /**
     * A fall taking up half a person's history leaves no room behind it for an earlier
     * one, and the honest answer is still that nothing can be said.
     *
     * Twelve weeks clears the history floor, so this is the second route to the same
     * ignorance and it is the one that would otherwise slip through: six steady weeks
     * and six low ones is a person the app knows nothing about yet, and answering
     * "no precedent" there would let a decline family fire on the strength of a
     * history that could not have contained a counter example.
     */
    @Test
    fun `a fall with no room for a precedent behind it cannot answer either`() {
        val log = log()
        log.weeks("work", List(12) { if (it >= 6) 1 else 20 })

        assertEquals(Precedent.INSUFFICIENT, factsFor(log, 12).history.activityDipPrecedent)
    }

    // --------------------------------------------------------------- the two readings

    @Test
    fun `a steady person is not in a fall at all`() {
        val log = log()
        log.weeks("work", List(fullYearish) { 10 })

        assertEquals(Precedent.NOT_IN_A_DIP, factsFor(log, fullYearish).history.activityDipPrecedent)
    }

    /**
     * A week that has not finished is not a week anything may call low.
     *
     * The Areas banner and the Momentum headline recompute during the day, so their
     * newest bucket holds a part week and is low against any normal. Reading it would
     * put every subject into a fall every Wednesday morning, and a fall one week longer
     * than the real one needs a precedent one week longer than the one that exists, so
     * the reading would drift toward "no precedent" on exactly the surfaces that
     * refresh most often. Without the fix this answers [Precedent.NONE].
     */
    @Test
    fun `a part week at the end of the window is not read as a fall`() {
        val log = log()
        log.weeks("work", List(fullYearish) { 10 }, dayInWeek = 0)

        val midday = TrailWindow(startOfDay(7 * fullYearish - 7), at(7 * fullYearish, 12))
        val facts = FactExtractor(log.queries()).extract(midday)
        assertEquals(Precedent.NOT_IN_A_DIP, facts.history.activityDipPrecedent)
    }

    /**
     * Focus is asked the same question over the sessions somebody started.
     *
     * `focusHabitFading` claims focus sessions are falling away, and a habit that comes
     * and goes with a condition is the same series as a habit that is fading. The
     * series read is starts rather than finishes, because that is the quantity the
     * family speaks about and the one `HistoryFacts.weekFocusStartedSeries` carries.
     */
    @Test
    fun `focus that comes and goes has its own precedent`() {
        val log = log()
        for (week in 0 until fullYearish) {
            if (week % 4 == 3) continue
            for ((index, hour) in listOf(9, 11, 13).withIndex()) {
                log.focusRun(
                    day = 7 * week + 3,
                    sessionId = "s-$week-$index",
                    areaId = "work",
                    itemId = "i-$week-$index",
                    hour = hour,
                )
            }
        }

        assertEquals(Precedent.PRESENT, factsFor(log, fullYearish).history.focusDipPrecedent)
    }

    // ---------------------------------------------------------------- per subject

    /**
     * The question is asked of each area separately, because the families 14b.9 names
     * that take an area as their subject are asked about that area.
     *
     * One area the person picks up and puts down, one that has fallen away for the
     * first time. The whole point of a per subject fact is that these two get different
     * answers in the same week.
     */
    @Test
    fun `two areas in one week get their own answers`() {
        val log = TrailTestLog()
        log.area(at(0, 8), "cycles", "Cycles")
        log.area(at(0, 8, 1), "falls", "Falls", orderKey = "a1")
        log.weeks("cycles", List(fullYearish) { if (it % 4 == 3) 1 else 10 })
        log.weeks("falls", List(fullYearish) { if (it >= 13) 1 else 10 }, dayInWeek = 4)

        val areas = factsFor(log, fullYearish).areas
        assertEquals(Precedent.PRESENT, areas.getValue("cycles").dipPrecedent)
        assertEquals(Precedent.NONE, areas.getValue("falls").dipPrecedent)
    }

    /**
     * The weeks before an area existed are not weeks it was quiet.
     *
     * An area created in week ten has zeros in every bucket behind it, and reading them
     * as silences gives it a ten week fall to find precedents in, so its first quiet
     * week would come back as a rhythm it has never had. Its own history is six weeks
     * long, which is not enough to answer, and that is the answer.
     */
    @Test
    fun `the empty weeks before an area existed are not a fall it once had`() {
        val log = log()
        log.weeks("work", List(fullYearish) { 10 })
        log.area(at(73, 8), "late", "Late", orderKey = "a1")
        val levels = List(fullYearish) { week ->
            when {
                week < 10 -> 0
                week == 15 -> 1
                else -> 10
            }
        }
        log.weeks("late", levels, dayInWeek = 4)

        val areas = factsFor(log, fullYearish).areas
        assertEquals(Precedent.INSUFFICIENT, areas.getValue("late").dipPrecedent)
    }

    /**
     * An area nobody has ever put anything in has no history to answer from.
     *
     * Its own creation is the only week it has ever moved in, and every week since is
     * empty. There is no normal to measure a fall against and nothing behind it to
     * compare one to, which is [Precedent.INSUFFICIENT] and not a rhythm.
     */
    @Test
    fun `an area that has never had an item answers that it cannot say`() {
        val log = log()
        log.weeks("work", List(fullYearish) { 10 })
        log.area(at(1, 8), "empty", "Empty", orderKey = "a1")

        val areas = factsFor(log, fullYearish).areas
        assertEquals(Precedent.INSUFFICIENT, areas.getValue("empty").dipPrecedent)
    }
}
