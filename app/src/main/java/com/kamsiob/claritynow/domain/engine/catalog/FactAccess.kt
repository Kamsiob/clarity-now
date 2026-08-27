package com.kamsiob.claritynow.domain.engine.catalog

import com.kamsiob.claritynow.domain.engine.ActiveItem
import com.kamsiob.claritynow.domain.engine.AreaFacts
import com.kamsiob.claritynow.domain.engine.FactSet

/**
 * The small set of readings every rule shares. CLARITY_LOGIC_ENGINE.md 3 and 4.
 *
 * Two of these exist because of a prohibition rather than for convenience.
 *
 * [areaOf] returns null for an area that is not in the fact set, and archived and
 * tombstoned areas are absent from it by construction, per 3.1. A rule that reaches for
 * its subject through this function therefore cannot qualify on an area a person has
 * archived, and prohibition 3 holds without any rule having to remember to check.
 *
 * [strictlyRising] and [strictlyFalling] are strict on purpose. A series that repeated a
 * value is not a trend, and a pattern family that fired on a flat run would tell someone
 * their activity had been climbing for three weeks when it had been level.
 */

/** The subject's area facts, or null when the subject is not an area or the area is absent. */
internal fun FactSet.areaOf(subject: Subject?): AreaFacts? =
    if (subject == null || subject.kind != SubjectKind.AREA) null else areas[subject.id]

/** The subject's active item, or null when the subject is not an item that is currently active. */
internal fun FactSet.activeItemOf(subject: Subject?): ActiveItem? {
    if (subject == null || subject.kind != SubjectKind.ITEM) return null
    return items.activeByArea.values.firstOrNull { it.itemId == subject.id }
}

/** The area holding the subject item, so a rule can require that area to have real events. */
internal fun FactSet.areaHolding(subject: Subject?): AreaFacts? {
    if (subject == null || subject.kind != SubjectKind.ITEM) return null
    return areas.values.firstOrNull { it.activeItemId == subject.id }
}

/** Every queued item across every area in the fact set. */
internal fun FactSet.totalQueueLength(): Int = areas.values.sumOf { it.queueLength }

/** The last [count] entries of [series], or null when there are not that many. */
internal fun tail(series: List<Int>, count: Int): List<Int>? =
    if (series.size < count) null else series.takeLast(count)

/** Strictly increasing, so a flat run is not a rise. */
internal fun strictlyRising(series: List<Int>, count: Int): Boolean {
    val window = tail(series, count) ?: return false
    return window.zipWithNext().all { (a, b) -> b > a }
}

/** Strictly decreasing, so a flat run is not a decline. */
internal fun strictlyFalling(series: List<Int>, count: Int): Boolean {
    val window = tail(series, count) ?: return false
    return window.zipWithNext().all { (a, b) -> b < a }
}

/**
 * Every value within [tolerance] of every other, over the last [count] entries.
 *
 * Used by `steadyPace`, `consistentRhythm` and `queueEquilibrium`, all three of which
 * claim a week was like the weeks around it. The band is absolute rather than
 * proportional because a proportional band is meaninglessly wide at low counts: two
 * events one week and three the next is a fifty percent change and is not a change.
 */
internal fun withinBand(series: List<Int>, count: Int, tolerance: Int): Boolean {
    val window = tail(series, count) ?: return false
    val low = window.min()
    val high = window.max()
    return high - low <= tolerance
}
