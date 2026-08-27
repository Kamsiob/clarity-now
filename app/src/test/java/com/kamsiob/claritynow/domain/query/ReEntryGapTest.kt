package com.kamsiob.claritynow.domain.query

import com.kamsiob.claritynow.data.event.AppOpened
import com.kamsiob.claritynow.data.event.ClarityEventType
import com.kamsiob.claritynow.domain.FixedClarityClock
import com.kamsiob.claritynow.domain.dateKey
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.lang.reflect.Modifier
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Re-entry gap detection. MASTER_BUILD_PROMPT 14b.4, Addendum 01 4d, issue #27.
 *
 * **The screen this protects is the one nobody who builds or tests this app daily
 * will ever see.** Somebody who opens it every day never has a gap, so every defect
 * in here ships looking exactly like working code and is found by a person coming
 * back after a hospital stay, which is the single worst audience for a mistake this
 * app could have. That is why 14b.4 specifies it rather than leaving it to be
 * discovered, and why the assertions below are about the awkward cases rather than
 * about the arithmetic, which is one line.
 *
 * The surface is phase 6 and none of it is tested here. Detection is a value, and
 * whether that value produces a screen, and what the screen may say, is an engine
 * decision that has to be able to come out as nothing at all.
 *
 * Four defects are individually plausible, individually invisible, and each has its
 * own test below.
 *
 * 1. **Measuring the absence with subtraction.** The two wall clocks are fourteen
 *    days apart minus an hour after a spring forward, and minus twenty three hours
 *    for somebody who opened the app late one night and early one morning. Dividing
 *    by 86400000 calls both of those thirteen and shows nothing.
 * 2. **Measuring to today rather than between opens.** The app writes today's marker
 *    on the first foreground, so the newest key is already today by the time
 *    anything asks. A query that compares the newest key to today reports every
 *    return as a gap of zero.
 * 3. **Treating a first ever open as a gap.** There is no earlier marker, and the
 *    two available defaults are the same day, which shows the screen to nobody, and
 *    the epoch, which greets every new user as somebody returning from decades away.
 * 4. **Writing the marker more than once a day.** Harmless to the projection, which
 *    is exactly why nothing downstream would ever notice, and a small lie in the
 *    file `docs/EVENT_FORMAT.md` promises a second implementation.
 */
class ReEntryGapTest {

    /** Daylight saving begins here in the United States. A twenty three hour day. */
    private val springForward: LocalDate = LocalDate.of(2026, 3, 8)

    /** Daylight saving ends here. A twenty five hour day, with 01:30 happening twice. */
    private val fallBack: LocalDate = LocalDate.of(2026, 11, 1)

    /**
     * `ClarityRepository.recordAppOpened`, reduced to the two lines that decide and
     * with the database replaced by the log itself.
     *
     * The repository asks the same question of one seek of the `entityId` index,
     * because it runs on the first foreground and the log is the largest table in
     * the app. The two cannot drift, because `AppOpened` uses its own date key as
     * its entity id, which is what makes the indexed form and [TrailQueries.hasOpenedOn]
     * the same question. What this shape adds is that a test can drive it, which the
     * repository's own form cannot be without a database.
     */
    private fun openTheApp(log: TrailTestLog, wallClock: Long, key: String) {
        if (log.queries().hasOpenedOn(key)) return
        log.add(wallClock, AppOpened(key))
    }

    private fun openedEvents(log: TrailTestLog) =
        log.events().filter { it.type == ClarityEventType.APP_OPENED }

    private fun instant(zone: ZoneId, local: LocalDateTime): Long =
        local.atZone(zone).toInstant().toEpochMilli()

    // At most once per calendar day ------------------------------------------

    /**
     * Opening the app twice on one date key writes one event. Addendum 01 2d.
     *
     * The second open is a person coming back to the app after lunch, which is the
     * ordinary case and happens several times on most days. Every one of those
     * reaches the foreground callback in `ClarityApp`, so the guard is not an edge
     * case; it is the common path, and without it the log would hold a row per
     * foreground, which is a session count. 14b.4 allows a date key and nothing
     * else, and a count of daily opens is the beginning of the tracking this app
     * says it does not do.
     */
    @Test
    fun `opening the app twice on one date key writes exactly one presence marker`() {
        val log = TrailTestLog()
        log.area(at(0, 8), "area-work", "Work")

        openTheApp(log, at(0, 7), dateKey(0))
        openTheApp(log, at(0, 13), dateKey(0))
        openTheApp(log, at(0, 21), dateKey(0))

        val opened = openedEvents(log)
        assertEquals(1, opened.size)
        assertEquals(dateKey(0), (opened.single().payload as AppOpened).dateKey)
        assertEquals(listOf(dateKey(0)), log.queries().openedDayKeys())

        // The next day is a different key and does get its own marker, or the guard
        // would be silently permanent rather than daily.
        openTheApp(log, at(1, 7), dateKey(1))
        assertEquals(2, openedEvents(log).size)
        assertEquals(listOf(dateKey(0), dateKey(1)), log.queries().openedDayKeys())
    }

    /**
     * A twenty five hour day is still one day, so it still gets one marker.
     *
     * On the day daylight saving ends, 01:30 happens twice, an hour apart in real
     * time. A guard that remembered an instant and compared elapsed milliseconds
     * would let the second one through, and the log would hold two markers for one
     * calendar day in one zone twice a year. The guard compares date keys, so the
     * two are the same open.
     */
    @Test
    fun `the day the clocks go back gets one marker even though its local times repeat`() {
        val log = TrailTestLog()
        val ambiguous = fallBack.atTime(1, 30)
        val firstPass = ambiguous.atZone(TEST_ZONE).withEarlierOffsetAtOverlap()
        val secondPass = ambiguous.atZone(TEST_ZONE).withLaterOffsetAtOverlap()

        // The fixture is worthless unless these really are two different instants an
        // hour apart that a person would read the same clock face for.
        assertEquals(
            3_600_000L,
            secondPass.toInstant().toEpochMilli() - firstPass.toInstant().toEpochMilli(),
        )

        val clock = FixedClarityClock(firstPass.toInstant().toEpochMilli(), TEST_ZONE)
        openTheApp(log, clock.nowMillis(), clock.dateKey())
        clock.set(secondPass.toInstant().toEpochMilli())
        openTheApp(log, clock.nowMillis(), clock.dateKey())

        assertEquals(1, openedEvents(log).size)
        assertEquals(listOf("2026-11-01"), log.queries().openedDayKeys())
    }

    // The date key across a daylight saving boundary --------------------------

    /**
     * The date key is correct across spring forward and fall back.
     * CLARITY_LOGIC_ENGINE.md 14, boundary tests.
     *
     * Walked hour by hour from local midnight rather than asserted at three chosen
     * instants, because the failure this guards against is a key computed by
     * dividing an epoch value, and that key is right at midnight and wrong later in
     * the day. A twenty three hour day and a twenty five hour day each have to
     * produce exactly one key, and the day after each has to produce its own.
     *
     * `ClarityClock.dateKey` is what writes the payload and therefore the only place
     * in the presence path where a zone is read at all. Everything downstream of it
     * compares calendar dates, so this is the whole of the daylight saving exposure
     * in re-entry detection.
     */
    @Test
    fun `the date key is one key per local day across both daylight saving boundaries`() {
        listOf(springForward to 23, fallBack to 25).forEach { (date, hoursInDay) ->
            val midnight = date.atStartOfDay(TEST_ZONE).toInstant().toEpochMilli()
            val clock = FixedClarityClock(midnight, TEST_ZONE)

            val keys = (0 until hoursInDay).map { hour ->
                clock.set(midnight + hour * 3_600_000L)
                clock.dateKey()
            }
            assertEquals("$date is $hoursInDay hours long", setOf(date.toString()), keys.toSet())

            // One hour further is the next calendar day, so the day really was that
            // long and the key really did turn over at its own local midnight.
            clock.set(midnight + hoursInDay * 3_600_000L)
            assertEquals(date.plusDays(1).toString(), clock.dateKey())
        }
    }

    /**
     * An absence that spans a spring forward is fourteen days, not thirteen and
     * change.
     *
     * March 1 to March 15 is fourteen calendar days and 335 hours, because the hour
     * between 02:00 and 03:00 on March 8 does not exist. Millisecond subtraction
     * makes that 13.96 days, which floors to thirteen and shows a person coming back
     * after a fortnight the ordinary queue screen. The fixture asserts the hour is
     * genuinely missing first, so the test cannot pass by accident in a zone without
     * daylight saving.
     */
    @Test
    fun `a fourteen day absence spanning a spring forward still triggers`() {
        val log = TrailTestLog()
        val before = instant(TEST_ZONE, springForward.minusDays(7).atTime(9, 0))
        val after = instant(TEST_ZONE, springForward.plusDays(7).atTime(9, 0))

        assertEquals(14 * 86_400_000L - 3_600_000L, after - before)

        log.add(before, AppOpened("2026-03-01"))
        log.add(after, AppOpened("2026-03-15"))

        val reEntry = log.queries().reEntryOn("2026-03-15")
        assertNotNull("fourteen calendar days is a gap whatever the clocks did", reEntry)
        assertEquals("2026-03-15", reEntry?.returnedOn)
    }

    /**
     * The same absence measured across a fall back, where the arithmetic errs the
     * other way.
     *
     * October 25 to November 8 is fourteen calendar days and 337 hours. Millisecond
     * subtraction happens to round to fourteen here, so this case would pass under
     * the wrong implementation and is not what it is for. Thirteen calendar days
     * across the same boundary is 313 hours, which is 13.04 days, and a thirteen day
     * absence must not trigger however the clocks moved inside it.
     */
    @Test
    fun `a thirteen day absence spanning a fall back does not trigger`() {
        val log = TrailTestLog()
        val before = instant(TEST_ZONE, fallBack.minusDays(6).atTime(9, 0))
        val after = instant(TEST_ZONE, fallBack.plusDays(7).atTime(9, 0))

        assertEquals(13 * 86_400_000L + 3_600_000L, after - before)

        log.add(before, AppOpened("2026-10-26"))
        log.add(after, AppOpened("2026-11-08"))

        assertNull(log.queries().reEntryOn("2026-11-08"))
        assertNull(log.queries().lastReEntryOnOrBefore("2026-11-08"))
    }

    // Fourteen triggers, thirteen does not ------------------------------------

    /**
     * The threshold itself, at both of the days that decide it.
     * MASTER_BUILD_PROMPT 14b.4: "A gap of 14 or more days puts the app into the
     * re-entry state".
     *
     * Fourteen or more, so exactly fourteen is inside and exactly thirteen is
     * outside. Both halves are asserted, because an off by one here is a person
     * being greeted correctly at fifteen days and being dropped straight onto the
     * queue at fourteen, and nothing anywhere would report that.
     */
    @Test
    fun `fourteen calendar days triggers and thirteen does not`() {
        val thirteen = TrailTestLog()
        thirteen.opened(0)
        thirteen.opened(13)
        assertNull(thirteen.queries().reEntryOn(dateKey(13)))

        val fourteen = TrailTestLog()
        fourteen.opened(0)
        fourteen.opened(14)
        assertEquals(dateKey(14), fourteen.queries().reEntryOn(dateKey(14))?.returnedOn)

        val fifteen = TrailTestLog()
        fifteen.opened(0)
        fifteen.opened(15)
        assertEquals(dateKey(15), fifteen.queries().reEntryOn(dateKey(15))?.returnedOn)
    }

    /**
     * The time of day either open happened at changes nothing.
     *
     * Somebody who checks the app at eleven at night and comes back at half past
     * midnight fourteen days later has been away fourteen days. The two wall clocks
     * are 13.06 days apart, which subtraction calls thirteen. This is the same
     * defect the daylight saving tests describe, reachable in every zone on earth
     * on any day of the year, and it is the reason the arithmetic reads the payload
     * key rather than the instant the row was written at.
     */
    @Test
    fun `a late night open and an early morning open fourteen days later is a gap`() {
        val log = TrailTestLog()
        log.opened(0, hour = 23)
        log.opened(14, hour = 0)

        val elapsed = at(14, 0) - at(0, 23)
        assertTrue("the two instants are less than fourteen days apart", elapsed < 14 * 86_400_000L)
        assertEquals(dateKey(14), log.queries().reEntryOn(dateKey(14))?.returnedOn)
    }

    // A first ever open is not a gap ------------------------------------------

    /**
     * A first ever open is not a return. There is nothing to return from.
     *
     * The empty log is the case a new user is in on the day they install the app,
     * and the one marker log is the case they are in for the rest of that first day.
     * Neither may produce a re-entry state, at any distance from any epoch, and the
     * assertion runs at four hundred days after the only marker as well as on the
     * day itself, because the plausible wrong implementation is the one that
     * measures from a missing previous open rather than the one that measures from
     * the wrong day.
     */
    @Test
    fun `a first ever open is not a gap and neither is an empty log`() {
        val empty = TrailTestLog()
        assertTrue(empty.queries().openedDayKeys().isEmpty())
        assertNull(empty.queries().reEntryOn(dateKey(0)))
        assertNull(empty.queries().lastReEntryOnOrBefore(dateKey(0)))

        val first = TrailTestLog()
        first.area(at(0, 8), "area-work", "Work")
        first.opened(0)
        assertNull("the only open there has ever been", first.queries().reEntryOn(dateKey(0)))
        assertNull(first.queries().lastReEntryOnOrBefore(dateKey(0)))
        assertNull(first.queries().lastReEntryOnOrBefore(dateKey(400)))
    }

    /**
     * A person who has used the app every day for a fortnight is not returning from
     * one.
     *
     * The complement of the test above, and the one that would fail if the loop
     * compared the newest open to today rather than comparing consecutive opens.
     * Today's marker is already in the log by the time anything asks, so the newest
     * gap in this fixture is one day and there is no re-entry anywhere in it.
     */
    @Test
    fun `an unbroken fortnight of daily opens contains no re-entry`() {
        val log = TrailTestLog()
        repeat(14) { day -> log.opened(day) }

        val queries = log.queries()
        assertEquals(14, queries.openedDayKeys().size)
        (0 until 14).forEach { day ->
            assertNull("day $day", queries.reEntryOn(dateKey(day)))
            assertNull("day $day", queries.lastReEntryOnOrBefore(dateKey(day)))
        }
    }

    // Two gaps, and the days after a return -----------------------------------

    /**
     * A log with two absences answers with the later one.
     *
     * This is a real shape rather than a contrived one: a person who was away in
     * spring, came back, used the app, and was away again in summer. The suppression
     * windows 14b.4 attaches to a return are all measured forward from it, so the
     * only return that anything is still suppressing for is the most recent, and an
     * implementation that walked oldest first would hold the Report open on the
     * strength of an absence six months gone.
     */
    @Test
    fun `a log with two absences names the later return`() {
        val log = TrailTestLog()
        log.opened(0)
        log.opened(20)
        log.opened(21)
        log.opened(22)
        log.opened(60)

        val queries = log.queries()
        assertEquals(dateKey(20), queries.reEntryOn(dateKey(20))?.returnedOn)
        assertEquals(dateKey(60), queries.reEntryOn(dateKey(60))?.returnedOn)

        // Asked on the second return, the second is the answer. Asked from anywhere
        // between the two, the first still is, because the second has not happened.
        assertEquals(dateKey(60), queries.lastReEntryOnOrBefore(dateKey(60))?.returnedOn)
        assertEquals(dateKey(20), queries.lastReEntryOnOrBefore(dateKey(59))?.returnedOn)
        assertEquals(dateKey(20), queries.lastReEntryOnOrBefore(dateKey(22))?.returnedOn)
    }

    /**
     * The state belongs to the day of the return, and the return itself outlives it.
     *
     * Two questions, deliberately separate. 14b.4 puts the app into the re-entry
     * state "on the foreground that writes today's APP_OPENED and only then", which
     * is [TrailQueries.reEntryOn] and is true on exactly one day. The suppression
     * windows run for two days and for a full week afterward, which is
     * [TrailQueries.lastReEntryOnOrBefore] and stays true across all of them. Folding
     * the two into one call is the mistake that either shows the screen every day
     * for a week or lifts the Report suppression the morning after.
     *
     * The day counting is asserted here rather than in phase 6, because
     * [ReEntry.daysSince] is arithmetic and phase 6 is a decision about language.
     */
    @Test
    fun `the return is a single day and the windows measured from it are not`() {
        val log = TrailTestLog()
        log.opened(0)
        log.opened(20)
        log.opened(21)
        log.opened(22)
        log.opened(27)

        val queries = log.queries()
        val reEntry = queries.lastReEntryOnOrBefore(dateKey(27))
        assertEquals(dateKey(20), reEntry?.returnedOn)

        // The screen appears once. Every later day is an ordinary day.
        listOf(21, 22, 27).forEach { day ->
            assertNull("day $day is not itself a return", queries.reEntryOn(dateKey(day)))
        }

        assertEquals(0, reEntry?.daysSince(dateKey(20)))
        assertEquals(1, reEntry?.daysSince(dateKey(21)))
        assertEquals(2, reEntry?.daysSince(dateKey(22)))
        assertEquals(7, reEntry?.daysSince(dateKey(27)))
        // Before the return reads as outside every window rather than as inside one.
        assertTrue((reEntry?.daysSince(dateKey(19)) ?: 0) < 0)
    }

    // What the value refuses to carry -----------------------------------------

    /**
     * [ReEntry] holds a date key and nothing else, and this is the assertion that
     * keeps it that way.
     *
     * 14b.4: the surface "does not state the length of the gap. Not in days, not in
     * weeks, not as a date, not as `since March`". A field holding the number would
     * turn that into a rule somebody has to remember four phases from now, and the
     * screen it protects is the one nobody sees during development. design-v3.md 15
     * asks for the statistically common answer to be identified and rejected where
     * something else serves as well: the common answer here is a query returning the
     * gap in days, and it is rejected because the number would then be one field
     * access away from the most expensive sentence this app could write.
     *
     * Read through Java reflection deliberately, so the check needs no reflection
     * library and cannot be satisfied by a property that merely looks harmless.
     * `returnedOn` is the date of the return, which the surface may not render
     * either; what makes it safe to carry is that it is not the length of the
     * absence and nothing can derive the length from it.
     *
     * Static fields are filtered out rather than asserted against, because
     * [ReEntry.MIN_GAP_DAYS] is a compile time constant and lands on this class as
     * one. It is the threshold the query compares against, it is the same fourteen
     * for everybody, and it says nothing about any person. An instance field is the
     * thing that would carry somebody's own absence around.
     */
    @Test
    fun `the re-entry value carries no measurement of the absence`() {
        val fields = ReEntry::class.java.declaredFields
            .filterNot { it.isSynthetic || Modifier.isStatic(it.modifiers) }
            .map { it.name to it.type.simpleName }

        assertEquals(listOf("returnedOn" to "String"), fields)
        assertFalse(
            "a numeric field on this type is a gap length waiting to be rendered: $fields",
            fields.any { (_, type) -> type in setOf("int", "long", "Integer", "Long") },
        )
    }

    // Bad data ----------------------------------------------------------------

    /**
     * A malformed date key costs that marker and nothing else.
     *
     * `docs/EVENT_FORMAT.md` is the contract a second implementation of this app is
     * built against, and MASTER_BUILD_PROMPT 14b.7 accepts an imported file. Either
     * can put a key in the log that this app cannot parse. The presence path is the
     * first thing that runs on a foreground, so an exception here is a launch crash
     * on a restored backup, and the person it happens to is the person who just
     * moved to a new phone.
     */
    @Test
    fun `a marker with an unparseable date key is dropped rather than thrown on`() {
        val log = TrailTestLog()
        log.add(at(0, 7), AppOpened("not-a-date"))
        log.add(at(1, 7), AppOpened(dateKey(1)))
        log.add(at(20, 7), AppOpened(dateKey(20)))

        val queries = log.queries()
        assertEquals(listOf(dateKey(1), dateKey(20)), queries.openedDayKeys())
        assertFalse(queries.hasOpenedOn("not-a-date"))
        // The two keys that did parse are nineteen days apart, which is a return.
        assertEquals(dateKey(20), queries.reEntryOn(dateKey(20))?.returnedOn)
    }

    // The reader's zone is not part of the answer ------------------------------

    /**
     * The same log answers the same in every zone, because no zone is consulted.
     *
     * CLAUDE.md rule 6 requires the gap to derive from the event log so that a
     * restored backup or a second device reaches the same answer. A device in Sydney
     * reading a log written in New York must not decide that somebody was away for
     * fifteen days, and it will if the arithmetic converts the row's wall clock
     * through the reader's zone. Reading the payload key makes the reader's zone
     * structurally unable to reach the answer, and this asserts it across a zone
     * with daylight saving, one without, and one whose offset is not a whole number
     * of hours.
     */
    @Test
    fun `the answer does not depend on the zone the question is asked in`() {
        val log = TrailTestLog()
        log.opened(0, hour = 23)
        log.opened(14, hour = 0)

        val zones = listOf(
            TEST_ZONE,
            ZoneId.of("UTC"),
            ZoneId.of("Australia/Sydney"),
            ZoneId.of("Asia/Kathmandu"),
        )
        val answers = zones.map { zone -> log.queries(zone).reEntryOn(dateKey(14))?.returnedOn }

        assertEquals(zones.size, answers.size)
        assertEquals(setOf(dateKey(14)), answers.toSet())
    }
}
