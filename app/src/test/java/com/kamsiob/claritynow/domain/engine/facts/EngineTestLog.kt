package com.kamsiob.claritynow.domain.engine.facts

import com.kamsiob.claritynow.data.event.AreaArchived
import com.kamsiob.claritynow.data.event.AreaDeleted
import com.kamsiob.claritynow.data.event.ClarityEvent
import com.kamsiob.claritynow.data.event.FocusCompleted
import com.kamsiob.claritynow.data.event.FocusEndedEarly
import com.kamsiob.claritynow.data.event.FocusStarted
import com.kamsiob.claritynow.data.event.ItemDeleted
import com.kamsiob.claritynow.data.event.PlanOffered
import com.kamsiob.claritynow.data.event.PulseAnswered
import com.kamsiob.claritynow.data.event.PulseGenerated
import com.kamsiob.claritynow.data.event.ReflectionPeriod
import com.kamsiob.claritynow.data.event.ReportGenerated
import com.kamsiob.claritynow.data.event.ReportSectionSnapshot
import com.kamsiob.claritynow.data.event.SubjectKind
import com.kamsiob.claritynow.domain.engine.FactRef
import com.kamsiob.claritynow.domain.query.TrailTestLog
import com.kamsiob.claritynow.domain.query.TrailWindow
import com.kamsiob.claritynow.domain.query.at
import com.kamsiob.claritynow.domain.query.dateKey
import com.kamsiob.claritynow.domain.query.startOfDay

/**
 * The shapes layer one needs that the Trail fixtures did not, plus the window
 * helper.
 *
 * Built on `domain.query`'s `TrailTestLog` rather than beside it, so a fact test and
 * a query test disagreeing about what a log looks like is not a shape this suite can
 * take. Everything here writes the same payloads `ClarityRepository` writes.
 */

/** The half open window covering local days `[fromDay, toDay)`. */
internal fun window(fromDay: Int, toDay: Int): TrailWindow =
    TrailWindow(startOfDay(fromDay), startOfDay(toDay))

internal fun TrailTestLog.archiveArea(day: Int, areaId: String, name: String, hour: Int = 9): ClarityEvent =
    add(at(day, hour), AreaArchived(areaId, name))

internal fun TrailTestLog.deleteArea(day: Int, areaId: String, name: String, hour: Int = 9): ClarityEvent =
    add(at(day, hour), AreaDeleted(areaId, name))

internal fun TrailTestLog.deleteItem(
    day: Int,
    itemId: String,
    areaId: String?,
    title: String,
    hour: Int = 9,
): ClarityEvent = add(at(day, hour), ItemDeleted(itemId, areaId, title))

internal fun TrailTestLog.focusRun(
    day: Int,
    sessionId: String,
    areaId: String,
    itemId: String,
    seconds: Int = 1500,
    hour: Int = 9,
    finished: Boolean = true,
) {
    add(at(day, hour), FocusStarted(sessionId, areaId, itemId, seconds))
    val endHour = hour + 1
    if (finished) {
        add(at(day, endHour), FocusCompleted(sessionId, seconds))
    } else {
        add(at(day, endHour), FocusEndedEarly(sessionId, seconds))
    }
}

internal fun TrailTestLog.pulse(
    day: Int,
    pulseId: String,
    family: String,
    variantKey: String,
    stage: Int = 1,
    subjectId: String? = null,
    subjectKind: SubjectKind? = null,
    register: String = "PLAIN",
    hour: Int = 8,
): ClarityEvent = add(
    at(day, hour),
    PulseGenerated(
        pulseId = pulseId,
        dateKey = dateKey(day),
        family = family,
        escalationStage = stage,
        register = register,
        variantKey = variantKey,
        renderedObservation = "observation for $variantKey",
        renderedQuestion = "question for $variantKey",
        factSnapshot = emptyMap(),
        reflectionPeriod = ReflectionPeriod.YESTERDAY,
        subjectId = subjectId,
        subjectKind = subjectKind,
    ),
)

internal fun TrailTestLog.answer(
    day: Int,
    pulseId: String,
    responseKey: String,
    responseLabel: String,
    isPositive: Boolean,
    subjectId: String? = null,
    hour: Int = 20,
): ClarityEvent = add(
    at(day, hour),
    PulseAnswered(pulseId, responseKey, responseLabel, isPositive, subjectId, null),
)

internal fun TrailTestLog.report(
    day: Int,
    reportId: String,
    weekStartKey: String,
    headlineKey: String,
    headlineVariantKey: String?,
    sections: List<ReportSectionSnapshot> = emptyList(),
    hour: Int = 8,
): ClarityEvent = add(
    at(day, hour),
    ReportGenerated(
        reportId = reportId,
        weekStartKey = weekStartKey,
        headlineKey = headlineKey,
        renderedSections = sections,
        factSnapshot = emptyMap(),
        headlineVariantKey = headlineVariantKey,
    ),
)

internal fun section(
    familyKey: String,
    variantKey: String,
    stage: Int = 1,
    subjectId: String? = null,
): ReportSectionSnapshot = ReportSectionSnapshot(
    sectionKey = "observation",
    sidehead = "Your week, honestly",
    text = "text for $variantKey",
    familyKey = familyKey,
    variantKey = variantKey,
    escalationStage = stage,
    register = "OBSERVATIONAL",
    subjectId = subjectId,
    subjectKind = subjectId?.let { SubjectKind.AREA },
)

internal fun TrailTestLog.planOffered(
    day: Int,
    planId: String,
    weekStartKey: String,
    frameKey: String,
    cueKey: String,
    actionKey: String,
    familyKey: String,
    subjectId: String? = null,
    hour: Int = 8,
): ClarityEvent = add(
    at(day, hour),
    PlanOffered(
        planId = planId,
        weekStartKey = weekStartKey,
        frameKey = frameKey,
        cueKey = cueKey,
        actionKey = actionKey,
        familyKey = familyKey,
        subjectId = subjectId,
        offeredLine = "one option",
        committedLine = "I will",
        resolutionFactRef = FactRef("area", "events"),
    ),
)
