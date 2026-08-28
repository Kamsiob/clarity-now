package com.kamsiob.claritynow.domain.engine.catalog

import com.kamsiob.claritynow.domain.engine.AreaFacts
import com.kamsiob.claritynow.domain.engine.FactSet

/**
 * Small builders so a rule reads as what it requires rather than as constructor noise.
 * CLARITY_LOGIC_ENGINE.md 4.
 *
 * There is deliberately no builder for specificity, priority or length band. Two of those
 * are computed and the third is a tie break, and a builder that let an author reach any
 * of them would be a builder that let an author put a thumb on the ranking.
 */

/** One condition, with the plain English the simulator prints beside a fired rule. */
internal fun criterion(id: String, describe: String, test: (FactSet, Subject?) -> Boolean): Criterion =
    Criterion(id, describe, test)

/** A condition that reads only the window, which most Pulse and Report rules do. */
internal fun window(id: String, describe: String, test: (FactSet) -> Boolean): Criterion =
    Criterion(id, describe) { facts, _ -> test(facts) }

/**
 * A condition on the subject area. False when the subject is not an area or the area is
 * absent from the fact set, which is where archived and tombstoned areas fall out.
 */
internal fun area(id: String, describe: String, test: (AreaFacts) -> Boolean): Criterion =
    Criterion(id, describe) { facts, subject -> facts.areaOf(subject)?.let(test) == true }

/** A condition on the subject item's age, false when the subject is not a currently active item. */
internal fun activeItemAge(id: String, describe: String, test: (Int) -> Boolean): Criterion =
    Criterion(id, describe) { facts, subject -> facts.activeItemOf(subject)?.ageDays?.let(test) == true }

/**
 * The one criterion nearly every area naming rule carries, and the reason it exists is
 * validator check 1.
 *
 * The validator vetoes any candidate naming an area with no events in the window being
 * described. A rule that could select such an area would produce a candidate that is
 * always vetoed, which is a silence with no reason anyone could find. Requiring it here
 * means the rule never gets that far.
 */
internal fun areaHasEvents(minimum: Int = 1): Criterion = area(
    "area.hasEvents.$minimum",
    "the area has at least $minimum events in the window, so naming it cannot be a phantom claim",
) { it.eventsInWindow >= minimum }

/** The same requirement for the area holding a subject item. */
internal fun holdingAreaHasEvents(minimum: Int = 1): Criterion = criterion(
    "holdingArea.hasEvents.$minimum",
    "the area holding the item has at least $minimum events in the window",
) { facts, subject -> (facts.areaHolding(subject)?.eventsInWindow ?: 0) >= minimum }

/**
 * The event floor every share based rule carries. CLARITY_LOGIC_ENGINE.md 3.1 and 13.
 *
 * `shareOfEvents` is the most misused fact in the system: one event in a one event week
 * reads as 100 percent concentration, and the sentence that comes out of it is true
 * arithmetic and a false claim. A catalog test asserts that every rule reading a share
 * carries one of these, and the id prefix is what that test looks for.
 */
/**
 * The app has existed for at least [days] days.
 *
 * A family whose lines claim a span may not fire before that span exists. The window a
 * fact is measured over is always its full width, so `window.dayCount` cannot tell a
 * fortnight of data from a fortnight of window, and without this every fortnight family
 * qualifies on day two and describes days that had not happened.
 *
 * Addendum 01 7d asks every reflective surface to be honest about what it does not have
 * yet, and CLARITY_LOGIC_ENGINE 1 says one wrong number destroys the credibility of
 * everything else the app says. This is the cheapest place to hold both.
 */
internal fun fortnightOfHistory(days: Int = 14): Criterion = window(
    "history.installedFor.$days",
    "the app has existed for the $days days being described",
) { it.history.daysSinceInstall >= days }

internal fun shareFloor(minimum: Int): Criterion = window(
    "$SHARE_FLOOR_PREFIX.window.$minimum",
    "the window holds at least $minimum events, so a share is describing something real",
) { it.window.totalEvents >= minimum }

/** The same floor on the subject area rather than on the window. */
internal fun areaShareFloor(minimum: Int): Criterion = area(
    "$SHARE_FLOOR_PREFIX.area.$minimum",
    "the area has at least $minimum events in the window, so its share is describing something real",
) { it.eventsInWindow >= minimum }

/** Every share floor criterion id starts with this, and [CatalogIntegrity] looks for it. */
internal const val SHARE_FLOOR_PREFIX = "floor"

/** Every criterion that reads a share carries this in its id, so the floor test can find it. */
internal const val SHARE_READING_PREFIX = "share"

/**
 * The queue emptied because its items were finished, and not because they were thrown away.
 *
 * A queue loses one item per promotion and a promotion follows a completion, so an area
 * that fell from `n` queued to nothing by working through it has at least `n` completions
 * behind it. A queued item can also be deleted, and nothing in the drain facts can tell the
 * two apart: `AreaFacts.queueDrainedFrom` measures how far the queue fell and never asks
 * how the items left.
 *
 * That matters because both drain benches claim somebody finished something.
 * `CORPUS_1_PULSE.md` 10 authors `{areaName} finished everything it was holding` and
 * `You cleared everything in {areaName}`, and `CORPUS_2_REPORT.md` 2.17 authors
 * `{areaName} cleared its entire queue this week`. Every one of those is false of a person
 * who deleted three things, and the realizer may select any line on the bench, so one false
 * line is enough to require this. `report.headline.clearing` carries the window scoped
 * version of the same guard already, which is where the requirement was first recognized.
 *
 * **It counts against the fall rather than against the window boundary**, and that is not
 * cosmetic. A queue built inside the window and then emptied holds nothing at the boundary,
 * so the boundary reading made this `completions >= 0` on exactly the shape the drain fact
 * was declared to reach: a person who added five things and deleted all five would have
 * been told they cleared them. The guard has to be measured against the same number the
 * sentence names.
 *
 * **It is stricter than the truth by one item in exactly one case**, an area whose fall
 * began with nothing active, where the first promotion costs no completion. One item of
 * slack toward silence is the right direction for a family whose sentences claim somebody
 * finished something.
 *
 * The null case reads as zero and so passes, because this is a guard and never the
 * condition that selects. Both rules carrying it also require the fall itself, at a
 * height of three or more, and a null fall fails those before this is reached.
 */
internal fun drainedByFinishing(): Criterion = area(
    "queue.drainedByFinishing",
    "the area finished at least as many things as the queue it fell from was holding, so " +
        "the queue emptied by being worked through rather than by being deleted",
) { it.completionsInWindow >= (it.queueDrainedFrom ?: 0) }
