package com.kamsiob.claritynow.domain.momentum

import com.kamsiob.claritynow.domain.query.TEST_ZONE
import com.kamsiob.claritynow.domain.query.at
import com.kamsiob.claritynow.domain.query.dateKey
import com.kamsiob.claritynow.domain.query.item
import com.kamsiob.claritynow.domain.query.opened
import com.kamsiob.claritynow.domain.query.promote
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The Momentum surface, composed from a real log. `MASTER_BUILD_PROMPT.md` 12.2, issue #5.
 *
 * **Nothing here asserts which family the headline came from.** That is the engine's
 * business and phase 5 tests it, and pinning it here would make this file fail every time
 * a corpus line is authored, which is what phase 9 does for a living. What it asserts is
 * everything that has to be true of whatever the engine said, and everything that has to
 * be true of the numbers regardless: fourteen rolling days, a count that is the size of a
 * set, tiles that match the areas the person can see, figures that equal the queries a
 * screen would otherwise have to ask twice, and a module that is absent rather than empty.
 */
class MomentumComposerTest {

    // The rolling window ---------------------------------------------------

    @Test
    fun `the dot row is fourteen local days with today last`() {
        val view = MomentumFixture().seedTwoAreas().capture(20).view(day = 20)

        assertEquals("design-v3.md section 11 and 12.2: fourteen, always", 14, view.activity.length)
        assertEquals(
            "the row reads left to right and today is the trailing mark",
            listOf(dateKey(20)),
            view.activity.days.filter { it.isToday }.map { it.dateKey },
        )
        assertEquals(dateKey(20), view.activity.days.last().dateKey)
        assertEquals(dateKey(7), view.activity.days.first().dateKey)
        assertEquals(
            "the keys are consecutive calendar days, walked rather than subtracted",
            (7..20).map(::dateKey),
            view.activity.days.map { it.dateKey },
        )
    }

    @Test
    fun `the count is the size of a set and a missed day resets nothing`() {
        // Two identical fortnights except that one of them has nothing on day 17.
        val full = MomentumFixture().seedTwoAreas()
        val gapped = MomentumFixture().seedTwoAreas()
        (14..20).forEach { day ->
            full.capture(day)
            if (day != 17) gapped.capture(day)
        }

        val withEveryDay = full.view(day = 20)
        val withOneMissed = gapped.view(day = 20)

        assertEquals("seven captures, seven active days", 7, withEveryDay.activity.activeCount)
        assertEquals(
            "a missed day costs exactly one day and nothing else. There is no run to break, " +
                "no counter to reset and no field on this value that could carry one. " +
                "design-v3.md 14 and CLARITY_LOGIC_ENGINE.md 3.1",
            6,
            withOneMissed.activity.activeCount,
        )
        val differences = withEveryDay.activity.days.zip(withOneMissed.activity.days)
            .filter { (left, right) -> left.active != right.active }
        assertEquals("exactly one dot differs", 1, differences.size)
        assertEquals(dateKey(17), differences.single().first.dateKey)
    }

    @Test
    fun `opening the app is not activity`() {
        // DECISIONS.md C7. `TrailQueries.activeDayKeys` counts user activity only, and this
        // is the assertion that keeps the dot row honest about it: a fortnight of opening
        // the app and doing nothing draws fourteen empty dots.
        val fixture = MomentumFixture().seedTwoAreas()
        (14..20).forEach { fixture.log.opened(it) }

        val view = fixture.view(day = 20)

        assertEquals(0, view.activity.activeCount)
        assertTrue("an app that was opened every day is still an empty fortnight", view.isEmpty)
    }

    // The tiles ------------------------------------------------------------

    @Test
    fun `tiles follow the order the person arranged and name the active ones`() {
        val fixture = MomentumFixture().seedTwoAreas().completeOne(19)
        // Work has an item added, promoted and completed, so nothing is active in it.
        fixture.log.item(at(20, 9), "work-live", MomentumFixture.WORK, "Live one", orderKey = "b0")
        fixture.log.promote(at(20, 9, 1), "work-live", MomentumFixture.WORK, "Live one")

        val view = fixture.view(day = 20, areaOrder = listOf(MomentumFixture.HOME, MomentumFixture.WORK))

        assertEquals(
            "the tile order is the order the Areas screen shows, not the order the fact set " +
                "happened to be built in",
            listOf("Home", "Work"),
            view.tiles.map { it.name },
        )
        assertEquals(
            "an area with an active item is the 60 percent tile, per design-v3.md 3.4",
            listOf(false, true),
            view.tiles.map { it.hasActiveItem },
        )
    }

    @Test
    fun `an archived area has no tile`() {
        val fixture = MomentumFixture().seedTwoAreas().capture(19)
        fixture.archive(day = 19, areaId = MomentumFixture.HOME, name = "Home")

        val view = fixture.view(day = 20)

        assertEquals(
            "FactSet.areas holds only areas live at the window end, which is what makes " +
                "prohibition 3 of CLARITY_LOGIC_ENGINE.md 1.1 a shape rather than a filter",
            listOf("Work"),
            view.tiles.map { it.name },
        )
    }

    // This Week ------------------------------------------------------------

    @Test
    fun `every This Week figure equals the query behind it`() {
        val fixture = MomentumFixture().seedTwoAreas()
        // Day 22 is a Monday, so the week to date on day 25 opens there.
        (22..25).forEach { fixture.completeOne(it) }
        fixture.focusSession(day = 23, minutes = 25)
        val now = at(25, 18)
        val week = MomentumWindows.weekToDate(now, TEST_ZONE)
        val queries = fixture.queries()

        val view = fixture.view(day = 25, hour = 18)

        assertEquals(
            "there is no second path to a displayed number, per MASTER_BUILD_PROMPT 9",
            queries.completionsBetween(week.fromMillis, week.toMillis),
            view.week.completed.value,
        )
        assertEquals(
            queries.additionsBetween(week.fromMillis, week.toMillis),
            view.week.added.value,
        )
        assertEquals(
            queries.focusMinutes(week.fromMillis, week.toMillis),
            view.week.focused.value,
        )
        assertEquals("four completions, Monday to Thursday", 4, view.week.completed.value)
        assertEquals("one twenty five minute session", 25, view.week.focused.value)
    }

    @Test
    fun `the week opens on Monday`() {
        val fixture = MomentumFixture().seedTwoAreas()
        // Day 21 is a Sunday and day 22 is the Monday after it.
        fixture.completeOne(21)
        fixture.completeOne(22)

        val view = fixture.view(day = 22, hour = 18)

        assertEquals(
            "CORPUS_3_MOMENTUM.md triggers the weekStarting family on Monday or Tuesday, " +
                "which is only true of a window that opens on Monday. The Report's Sunday " +
                "week is a different question and never meets this one",
            1,
            view.week.completed.value,
        )
    }

    @Test
    fun `a feature that has never been used renders undiscovered`() {
        val neverFocused = MomentumFixture().seedTwoAreas().completeOne(25).view(day = 25, hour = 18)

        assertFalse(
            "no focus session has ever been started, so the figure is dimmed and carries a " +
                "discovery line rather than being hidden. MASTER_BUILD_PROMPT 12.2",
            neverFocused.week.focused.discovered,
        )
        assertTrue("something has been completed", neverFocused.week.completed.discovered)
        assertTrue("intake never carries a discovery line", neverFocused.week.added.discovered)
    }

    @Test
    fun `a zero this week is a zero and not a missing feature`() {
        // A session weeks ago and none this week. 12.2 asks for an unused feature to be
        // dimmed, and this person has used it: telling them what a focus session is would
        // be the app forgetting what they did.
        val fixture = MomentumFixture().seedTwoAreas().focusSession(day = 3, minutes = 20)

        val view = fixture.view(day = 25, hour = 18)

        assertEquals(0, view.week.focused.value)
        assertTrue("discovery is a lifetime question, never a weekly one", view.week.focused.discovered)
    }

    // The insight modules --------------------------------------------------

    @Test
    fun `area balance needs two areas with something in them`() {
        val oneArea = MomentumFixture().seedTwoAreas()
        (14..20).forEach { oneArea.capture(it) }

        assertNull(
            "one area holding a hundred percent of a fortnight is arithmetic rather than a " +
                "balance, and AreaFacts.shareOfEvents calls itself the most misused fact in " +
                "the system for exactly this",
            oneArea.view(day = 20).insights.areaBalance,
        )

        val twoAreas = MomentumFixture().seedTwoAreas()
        (14..20).forEach { twoAreas.capture(it) }
        twoAreas.completeOne(19, areaId = MomentumFixture.HOME, areaName = "Home")

        val balance = twoAreas.view(day = 20).insights.areaBalance
        assertNotNull("two areas with events is a balance", balance)
        val events = balance?.shares.orEmpty().map { it.events }
        assertEquals(2, events.size)
        assertEquals("busiest first", events.sortedDescending(), events)

        // The shares are shares of every user activity event in the window, not of the
        // events that belong to an area, so the column does not sum to a hundred. The
        // denominator travels with them for exactly that reason, and it can only be at
        // least as large as what the areas hold between them.
        val total = balance?.total ?: 0
        assertTrue(
            "the denominator is every user activity event in the fortnight, so it is at " +
                "least the sum of what the areas hold",
            total >= events.sum(),
        )
    }

    @Test
    fun `the area balance denominator counts activity that belongs to no area`() {
        val fixture = MomentumFixture().seedTwoAreas()
        (14..20).forEach { fixture.capture(it) }
        fixture.completeOne(19, areaId = MomentumFixture.HOME, areaName = "Home")
        fixture.answerPulse(20)

        val balance = fixture.view(day = 20).insights.areaBalance
        assertNotNull("two areas with events is a balance", balance)
        val areaEvents = balance?.shares.orEmpty().sumOf { it.events }

        // This is the defect a device check found: two areas reading 64 and 21 percent
        // with nothing on the screen accounting for the other fifteen points. Answering a
        // Pulse is something a person did and it belongs to no area, so it is in the
        // denominator and in no numerator. The screen states the denominator rather than
        // changing it, because the headline above can say the same percentage about the
        // same area and 11.4 gives one fact exactly one number.
        assertTrue(
            "a Pulse answer is user activity with no area, so it widens the denominator " +
                "without widening any share",
            (balance?.total ?: 0) > areaEvents,
        )
        assertTrue(
            "and the shares therefore do not sum to a hundred",
            balance?.shares.orEmpty().sumOf { it.percent } < 100,
        )
    }

    @Test
    fun `idle areas appear at seven days and not a day sooner`() {
        val fixture = MomentumFixture().seedTwoAreas()
        (1..6).forEach { fixture.capture(it) }

        assertNull(
            "Home has been quiet for six days, which is under the floor in 12.2",
            fixture.view(day = 6, hour = 18).insights.idleAreas,
        )

        val idle = fixture.view(day = 8, hour = 18).insights.idleAreas
        assertEquals(
            "Home has been quiet for eight days and Work for three",
            listOf("Home"),
            idle?.map { it.name },
        )
        assertEquals(8, idle?.single()?.daysIdle)
    }

    @Test
    fun `nothing is quiet when nothing is moving`() {
        // Two areas and a hard fortnight. Every area is over the seven day floor, and
        // listing all of them under a heading would be a measurement of an absence on the
        // calmest screen in the app. The headline has already said the fortnight was quiet,
        // in one sentence, from the corpus.
        val fixture = MomentumFixture().seedTwoAreas()

        assertNull(
            "quiet is a comparison, and there is nothing to compare against",
            fixture.view(day = 20).insights.idleAreas,
        )
    }

    @Test
    fun `the focus strip is absent until there is focus in it`() {
        val noFocus = MomentumFixture().seedTwoAreas().capture(20)
        assertNull(
            "a strip of seven empty cells states nothing and 11.4 forbids padding a section",
            noFocus.view(day = 20, hour = 18).insights.focusPattern,
        )

        val withFocus = MomentumFixture().seedTwoAreas().focusSession(day = 18, minutes = 30)
        val pattern = withFocus.view(day = 20, hour = 18).insights.focusPattern
        assertNotNull("a session two days ago is inside the seven day strip", pattern)
        assertEquals("seven cells, one per local day", 7, pattern?.days?.size)
        assertEquals(30, pattern?.minutes)
        assertEquals(
            "the session is attributed to the day it started on",
            listOf(dateKey(18)),
            pattern?.days?.filter { it.minutes > 0 }?.map { it.dateKey },
        )
    }

    @Test
    fun `the pace sparkline needs three points and two of them carrying something`() {
        val oneSpike = MomentumFixture().seedTwoAreas()
        (22..25).forEach { oneSpike.completeOne(it) }

        assertNull(
            "four completions in one week is a spike, and a line drawn across it would be a " +
                "trend nobody has",
            oneSpike.view(day = 25, hour = 18).insights.completionPace,
        )

        val spread = MomentumFixture().seedTwoAreas()
        listOf(3, 10, 17, 24).forEach { spread.completeOne(it) }

        val pace = spread.view(day = 25, hour = 18).insights.completionPace
        assertNotNull("four weeks with a completion in each is a pace", pace)
        assertTrue("three points at the very least", (pace?.weeks?.size ?: 0) >= 3)
        assertTrue("and at most eight, per 12.2", (pace?.weeks?.size ?: 0) <= MomentumWindows.PACE_WEEKS)
        assertEquals(pace?.weeks?.sum(), pace?.total)
    }

    // The empty state and the failure state --------------------------------

    @Test
    fun `a fortnight with nothing in it is the empty state and still speaks`() {
        val view = MomentumFixture().seedTwoAreas().view(day = 20)

        assertTrue(view.isEmpty)
        assertEquals(0, view.activity.activeCount)
        assertTrue("every figure is zero", view.week.all.all { it.value == 0 })
        assertEquals("both areas still have a tile", 2, view.tiles.size)
        assertTrue("and none of them is active", view.tiles.none { it.hasActiveItem })
        assertNull(
            "and nothing is quiet, because quiet is a comparison against somewhere that " +
                "moved. A list of every untouched area under a heading is the guilt 12.2 " +
                "rules out of this state",
            view.insights.idleAreas,
        )
        assertFalse("no module has data", view.insights.any)
        assertNotNull(
            "the empty state is a welcoming sentence, and it comes from the corpus like " +
                "every other sentence about a person's own data",
            view.headline,
        )
    }

    @Test
    fun `losing the corpus costs the headline and no numbers`() {
        val fixture = MomentumFixture(catalog = null).seedTwoAreas()
        (14..20).forEach { fixture.capture(it) }

        val view = fixture.view(day = 20)

        assertNull("there is no language to speak with", view.headline)
        assertNull("and the banner is language from end to end", fixture.banner(day = 20))
        assertEquals("every number is still counted from the log", 7, view.activity.activeCount)
        assertEquals(2, view.tiles.size)
    }

    // The banner -----------------------------------------------------------

    @Test
    fun `the banner is a sentence and a caption, or nothing`() {
        val fixture = MomentumFixture().seedTwoAreas()
        (22..25).forEach { fixture.completeOne(it) }

        val banner = fixture.banner(day = 25, hour = 18)

        assertNotNull("a week with four completions has a shape", banner)
        assertTrue("the sentence is an authored line", banner?.sentence?.isNotBlank() == true)
        assertTrue(
            "no caption may render a nought through a count slot, per 7.2 and validator " +
                "check 4",
            banner?.caption?.contains(" 0 ") != true,
        )
    }
}
