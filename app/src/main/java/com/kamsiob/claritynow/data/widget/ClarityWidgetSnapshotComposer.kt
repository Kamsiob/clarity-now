package com.kamsiob.claritynow.data.widget

import com.kamsiob.claritynow.domain.replay.ClarityState
import com.kamsiob.claritynow.domain.replay.FocusSessionState

/**
 * Turns the projection into the one value the launcher surfaces are allowed to read.
 * MASTER_BUILD_PROMPT 13.3.
 *
 * **Pure, and deliberately so.** It takes the instant and the day as parameters rather
 * than reading a clock, holds no Android import, and answers the same snapshot for the
 * same inputs on any runtime. That is what makes the daily rotation, the automatic
 * choice and the queue counts assertable in a unit test with no device, and it is the
 * same rule `domain.engine` follows for the same reason.
 *
 * **It renders no sentence and selects no line.** Everything below is a name, a number
 * or a copy of a string somebody else wrote. The two sentence carrying fields it can
 * fill, [ClarityWidgetSnapshot.guidance] and [ClarityWidgetSnapshot.rhythm], arrive here already
 * written: the accepted plan line is read out of the log where the engine put it when
 * the person accepted it, and everything in [WidgetSpeech] is handed in by a caller
 * that ran the engine. If a future session finds itself wanting a corpus import in
 * this file, the design has gone wrong one layer up.
 */
object ClarityWidgetSnapshotComposer {

    fun compose(
        state: ClarityState,
        nowMillis: Long,
        dateKey: String,
        calmMode: Boolean,
        runningSession: FocusSessionState?,
        previous: ClarityWidgetSnapshot?,
        speech: WidgetSpeech = WidgetSpeech.NOTHING,
    ): ClarityWidgetSnapshot {
        val areas = areasIn(state)
        val live = areas.filterNot { it.archived }
        return ClarityWidgetSnapshot(
            schema = ClarityWidgetSnapshot.SCHEMA,
            writtenAtMillis = nowMillis,
            dateKey = dateKey,
            calmMode = calmMode,
            areas = areas,
            automaticAreaId = chooseAutomatic(live, dateKey, previous),
            automaticDateKey = if (live.isEmpty()) null else dateKey,
            inboxCount = state.unfiledItems.size,
            focus = focusIn(state, runningSession),
            rhythm = speech.rhythm,
            week = speech.week,
            guidance = guidanceIn(state, speech.reportHeadline),
        )
    }

    /**
     * Every area that still exists, in the order the Areas screen shows them.
     *
     * Deleted areas are dropped and archived ones are kept and flagged, per
     * [ClarityWidgetSnapshot.areas]. The sort is the one `ClarityState.liveAreas` uses, order
     * key then id, so a widget listing areas and the app listing them cannot disagree.
     */
    private fun areasIn(state: ClarityState): List<WidgetArea> = state.areas.values
        .filter { it.deletedAt == null }
        .sortedWith(compareBy({ it.orderKey }, { it.id }))
        .map { area ->
            val active = state.activeItemIn(area.id)
            WidgetArea(
                id = area.id,
                name = area.name,
                colorHex = area.colorHex,
                archived = area.archived,
                activeItemId = active?.id,
                activeItemTitle = active?.title,
                activeItemFirstStep = active?.firstStep,
                queueCount = state.queueIn(area.id).size,
                lastEventAtMillis = area.lastEventAt,
            )
        }

    /**
     * The area an unconfigured Next Up or First Step shows today. design-v3.md 12.2,
     * "the least recently touched active area, and rotates daily".
     *
     * **Chosen once a day and then held, which is what daily means here.** Recomputing
     * on every write would rotate the moment somebody touched the area on screen,
     * because touching it makes it the most recently touched and hands the widget to
     * the next area along. A person would watch their home screen change while they
     * worked, for a reason they could not see. So a choice made today survives every
     * write today, and tomorrow chooses again.
     *
     * It is held only while the area it named is still live. An archived or deleted
     * area cannot be what automatic shows, so the choice is made again the moment it
     * stops being real, whatever day it is.
     *
     * Areas with something active are preferred, and an install where everything is
     * idle falls back to the whole list rather than to nothing: an idle area on the
     * home screen still says which area is quietest, and a blank widget says nothing.
     */
    private fun chooseAutomatic(
        live: List<WidgetArea>,
        dateKey: String,
        previous: ClarityWidgetSnapshot?,
    ): String? {
        if (live.isEmpty()) return null
        val held = previous?.takeIf { it.automaticDateKey == dateKey }?.automaticAreaId
        if (held != null && live.any { it.id == held }) return held
        val pool = live.filterNot { it.isIdle }.ifEmpty { live }
        return pool.minWithOrNull(compareBy({ it.lastEventAtMillis }, { it.id }))?.id
    }

    /**
     * The session this device is running, or null.
     *
     * The end instant is folded from the log rather than taken from the device
     * preference that also holds one: `FocusHandle` says the log wins wherever the two
     * disagree, and a widget is the last place that should be holding the second
     * opinion.
     */
    private fun focusIn(state: ClarityState, session: FocusSessionState?): WidgetFocus? {
        if (session == null) return null
        val item = state.items[session.itemId]
        return WidgetFocus(
            sessionId = session.id,
            areaId = session.areaId,
            itemId = session.itemId,
            itemTitle = item?.title.orEmpty(),
            startedAtMillis = session.startedAt,
            endsAtMillis = session.startedAt + session.plannedSeconds * 1_000L,
            plannedSeconds = session.plannedSeconds,
        )
    }

    /**
     * The plan the person accepted, and nothing else. design-v3.md 12.3.
     *
     * **Never an unaccepted plan and never a declined one.** Declining writes nothing,
     * so an unaccepted plan is simply one that was never accepted, and the filter below
     * is the whole of the rule. The most recent one wins, by the week it belongs to and
     * then by when it was accepted, so a week that somehow carries two cannot depend on
     * map order.
     */
    private fun guidanceIn(state: ClarityState, reportHeadline: String?): WidgetGuidance? {
        val accepted = state.plans.values
            .filter { it.isAccepted }
            .maxWithOrNull(compareBy({ it.weekStartKey }, { it.acceptedAt ?: 0L }, { it.id }))
        if (accepted == null && reportHeadline == null) return null
        return WidgetGuidance(
            acceptedPlanLine = accepted?.committedLine,
            reportHeadline = reportHeadline,
        )
    }
}

/**
 * The sentences and figures a widget can show that this file is not allowed to work
 * out for itself.
 *
 * Everything in here was produced by the engine or counted over the log, above this
 * layer, and is carried through it untouched. It exists so that the composer can stay
 * pure and so that whoever builds a widget needing a new one of these has one parameter
 * to fill rather than a schema to change.
 *
 * [ClarityWidgetSnapshotWriter] fills [rhythm] today, over the same fortnight Momentum
 * counts. [week] and [reportHeadline] are still absent, because the two widgets that
 * read them are the two design-v3.md 12.3 leaves optional.
 */
data class WidgetSpeech(
    val rhythm: WidgetRhythm? = null,
    val week: WidgetWeek? = null,
    val reportHeadline: String? = null,
) {
    companion object {
        val NOTHING = WidgetSpeech()
    }
}
