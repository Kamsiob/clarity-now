package com.kamsiob.claritynow.domain.momentum

import com.kamsiob.claritynow.data.event.AreaArchived
import com.kamsiob.claritynow.data.event.ClarityEvent
import com.kamsiob.claritynow.data.event.FocusCompleted
import com.kamsiob.claritynow.data.event.FocusStarted
import com.kamsiob.claritynow.domain.engine.FactExtractor
import com.kamsiob.claritynow.domain.engine.FactSet
import com.kamsiob.claritynow.domain.engine.catalog.ClarityCatalog
import com.kamsiob.claritynow.domain.engine.catalog.CorpusFixture
import com.kamsiob.claritynow.domain.query.TEST_ZONE
import com.kamsiob.claritynow.domain.query.TrailQueries
import com.kamsiob.claritynow.domain.query.TrailTestLog
import com.kamsiob.claritynow.domain.query.area
import com.kamsiob.claritynow.domain.query.at
import com.kamsiob.claritynow.domain.query.complete
import com.kamsiob.claritynow.domain.query.item
import com.kamsiob.claritynow.domain.query.promote

/**
 * A log, and the composer run over it. Shared by the Momentum tests.
 *
 * **Built out of the same helpers the Trail and Pulse tests use**, so a fixture cannot
 * name a day the events do not land on: `at` and `dateKey` both derive from
 * `TEST_START_DATE` in `TEST_ZONE`, which is deliberately not UTC.
 *
 * The catalog is the real one, parsed from the three committed corpus files, for the
 * reason `PulseGenerationTest` gives: nothing here asserts which family fires on which
 * day, so a real corpus costs nothing and a synthetic one would prove nothing about the
 * files the app actually reads. A test that wants the corpus absent passes null.
 */
internal class MomentumFixture(catalog: ClarityCatalog? = CorpusFixture.catalog) {

    val log = TrailTestLog()

    private val composer = MomentumComposer(catalog, TEST_ZONE)

    fun queries(): TrailQueries = log.queries()

    /** The Momentum surface as it would be on [day] at [hour]. */
    fun view(day: Int, hour: Int = 12, areaOrder: List<String> = emptyList()): MomentumView =
        composer.compose(queries(), at(day, hour), areaOrder)

    /**
     * The week to date fact set the banner and its caption are both read against.
     *
     * The same window `MomentumComposer.banner` extracts over, so a caption test and the
     * screen cannot be looking at two different weeks.
     */
    fun weekFacts(day: Int, hour: Int = 12): FactSet =
        FactExtractor(queries()).extract(MomentumWindows.weekToDate(at(day, hour), TEST_ZONE))

    /** The Areas banner as it would be on [day] at [hour], or null on silence. */
    fun banner(day: Int, hour: Int = 12): AreasBannerView? =
        composer.banner(queries(), at(day, hour))

    /** Two areas on day zero, which is where every scenario below starts. */
    fun seedTwoAreas(): MomentumFixture {
        log.area(at(0, 6, 0), WORK, "Work")
        log.area(at(0, 6, 1), HOME, "Home", colorHex = "#3E9E6E", orderKey = "a1")
        return this
    }

    /** One capture into Work, which is one user activity event on that day. */
    fun capture(day: Int, suffix: String = ""): MomentumFixture {
        val id = "work-$day$suffix"
        log.item(at(day, 9, 0), id, WORK, "Work item $day$suffix", orderKey = "a$day$suffix")
        return this
    }

    /** One item added, promoted and finished on the same day, in [areaId]. */
    fun completeOne(day: Int, areaId: String = WORK, areaName: String = "Work"): MomentumFixture {
        val id = "$areaId-done-$day"
        log.item(at(day, 8, 0), id, areaId, "Finished $day", orderKey = "z$day", areaName = areaName)
        log.promote(at(day, 8, 1), id, areaId, "Finished $day", areaName = areaName)
        log.complete(at(day, 8, 2), id, areaId, "Finished $day", areaName = areaName)
        return this
    }

    /** One completed focus session of [minutes], started at 14:00 on [day]. */
    fun focusSession(day: Int, minutes: Int, areaId: String = WORK): MomentumFixture {
        val seconds = minutes * 60
        val sessionId = "session-$day"
        log.add(at(day, 14, 0), FocusStarted(sessionId, areaId, "$areaId-item", seconds))
        log.add(at(day, 14, minutes.coerceAtMost(59)), FocusCompleted(sessionId, seconds))
        return this
    }

    fun archive(day: Int, areaId: String, name: String): ClarityEvent =
        log.add(at(day, 7, 0), AreaArchived(areaId, name))

    companion object {
        const val WORK = "area-work"
        const val HOME = "area-home"
    }
}
