package com.kamsiob.claritynow.data.widget

import kotlinx.serialization.Serializable

/**
 * Everything every widget is allowed to know, written by the app and read by the
 * launcher surfaces. MASTER_BUILD_PROMPT 13.3, design-v3.md 12.1.
 *
 * ## This type is a boundary, not a convenience
 *
 * **Widgets never read a corpus and never run the engine.** A widget that composed
 * its own sentence would be a second path to the screen, which MASTER_BUILD_PROMPT
 * 11.1 does not allow, and it would be the one path with no validator on it. So
 * every sentence a widget can show is a `String` in here, rendered by the engine
 * before it was written, and repeated verbatim on the other side. Nothing in this
 * file selects a line, formats an observation or decides what is worth saying.
 *
 * The corollary is the part that is easy to lose: **a field here is either a fact or
 * a finished sentence.** A key, a family name, a stage or a fact reference would be
 * an invitation to render something on the far side of the boundary, so none of them
 * appears below. [WidgetGuidance.acceptedPlanLine] is the sharpest case. It is a
 * sentence, it came out of the log where the engine wrote it when the person accepted
 * the plan, and it is carried whole rather than as the four keys it was built from.
 *
 * ## Why everything has a default
 *
 * This is decoded from JSON written by a possibly older build of the app, on a phone
 * that restored a backup, and it is decoded in the moment a widget has to draw. A
 * missing field must degrade to a quiet widget rather than to an exception, so every
 * field has a default and the decoder ignores keys it does not know. [schema] exists
 * to say what wrote it, never to gate a read: a snapshot from the future is read for
 * the fields this build understands.
 *
 * ## What is in here that this phase does not draw
 *
 * [week] belongs to a widget design-v3.md 12.3 leaves optional and nothing writes it
 * yet, and the acceptance criteria on issue #11 ask for a format that takes the deferred
 * widgets without a schema change. It is here, it is nullable, and whoever builds that
 * widget fills it at the one call site `WidgetSpeech` exists to provide rather than by
 * editing this type. A null means "nothing has written this yet", which every widget
 * already has to handle, because it is also what a fresh install looks like.
 *
 * ## The other half of phase 12, and the convergence this is waiting for
 *
 * `widget/WidgetSnapshotStore.kt` holds a second, smaller snapshot, written at the same
 * time as this one by the slice that built `Quick Capture`, `Focus Countdown` and
 * `Rhythm`. It carries the running session, the inbox count and the fourteen day row as
 * scalar preference keys in the same file this document is stored in, and it cannot
 * carry a list of areas without inventing an index in its key names, which is why this
 * shape exists beside it.
 *
 * **Two shapes is one too many and they should be one.** This one already carries every
 * field that one does, under the same names, so the merge is mechanical: point
 * `FocusWidgetFacts` at [WidgetFocus] and `RhythmWidgetFacts` at [WidgetRhythm], both of
 * which were given matching members for exactly that reason, and delete the scalar keys.
 * Recorded on issue #11.
 */
@Serializable
data class ClarityWidgetSnapshot(
    /** The build that wrote this. Recorded, never used to refuse a read. */
    val schema: Int = SCHEMA,
    /**
     * When this was written, from `ClarityClock`. Diagnostic only.
     *
     * **Nothing computes a countdown from it.** [WidgetFocus.endsAtMillis] is an
     * absolute instant for exactly that reason: a widget subtracting a duration from
     * the moment a snapshot was written would drift by however long the system took
     * to deliver the update.
     */
    val writtenAtMillis: Long = 0L,
    /** The local calendar day it was written on. What makes the daily rotation daily. */
    val dateKey: String = "",
    /**
     * design-v3.md 16.3. Calm mode is a device preference rather than engine state,
     * so it travels to the widget process here like everything else a widget reads.
     * DataStore is not multi process safe and this file is not the place to test that.
     */
    val calmMode: Boolean = false,
    /**
     * Every area that still exists, archived ones included, in the order the Areas
     * screen shows them.
     *
     * **Archived areas are carried on purpose.** A widget pointed at one has to be
     * able to name it, per design-v3.md 12.1: an area can be archived while a widget
     * points at it, and the widget then says what happened rather than showing a stale
     * name or an empty box. A deleted area is absent entirely, which is the other half
     * of the same rule and the reason [WidgetArea.archived] is a flag rather than a
     * second list: the two states differ in what can be said, not in where they live.
     */
    val areas: List<WidgetArea> = emptyList(),
    /**
     * The area an unconfigured Next Up or First Step is showing today.
     *
     * The least recently touched area with something active, chosen once per local day
     * and held for the rest of it. See `ClarityWidgetSnapshotComposer.chooseAutomatic` for why
     * it is held rather than recomputed on every write.
     */
    val automaticAreaId: String? = null,
    /** The day [automaticAreaId] was chosen on. A different day chooses again. */
    val automaticDateKey: String? = null,
    /** The unfiled inbox, MASTER_BUILD_PROMPT 14b.1. Plain text on a widget, never a badge. */
    val inboxCount: Int = 0,
    /** The session this device is running, or null. */
    val focus: WidgetFocus? = null,
    /** The fourteen day row, counted over the same fortnight Momentum counts. */
    val rhythm: WidgetRhythm? = null,
    /** Three figures from Momentum. Written by whoever builds the This Week widget. */
    val week: WidgetWeek? = null,
    /** The one sentence guidance is allowed outside the Report. */
    val guidance: WidgetGuidance? = null,
) {

    /** Areas a person can see. Archived ones are carried, and are not these. */
    val liveAreas: List<WidgetArea> get() = areas.filterNot { it.archived }

    fun areaOrNull(areaId: String?): WidgetArea? =
        areaId?.let { id -> areas.firstOrNull { it.id == id } }

    /**
     * What a widget configured to [configuredAreaId] should draw.
     *
     * Null means automatic, and automatic can never be [WidgetTarget.Deleted]: it is
     * recomputed from the areas that exist every time this snapshot is written, so the
     * worst it can do is find none at all.
     *
     * A configured id that is not in [areas] at all means the area was deleted; one
     * that is there and archived means it was archived. design-v3.md 12.1 asks for a
     * plain line naming what happened, so the two are told apart here rather than
     * collapsed into one apology.
     */
    fun resolve(configuredAreaId: String?): WidgetTarget {
        if (configuredAreaId == null) {
            val automatic = areaOrNull(automaticAreaId)?.takeUnless { it.archived }
                ?: liveAreas.firstOrNull()
            return automatic?.let { WidgetTarget.Live(it) } ?: WidgetTarget.NoAreas
        }
        val area = areaOrNull(configuredAreaId) ?: return WidgetTarget.Deleted
        return if (area.archived) WidgetTarget.Archived(area) else WidgetTarget.Live(area)
    }

    /**
     * True when [other] says the same thing this does.
     *
     * [writtenAtMillis] is excluded, and it is the only field that is. It changes on
     * every pass and nothing on a screen reads it, so comparing with it in would make
     * every pass a write and every write a redraw of every placed widget. Everything
     * else is content: a name, a count, a title, a day, a session.
     */
    fun sameContentAs(other: ClarityWidgetSnapshot?): Boolean =
        other != null && copy(writtenAtMillis = 0L) == other.copy(writtenAtMillis = 0L)

    companion object {

        /**
         * The shape this build writes.
         *
         * 1: phase 12. Areas, the automatic choice, the inbox count, the running
         * session, and the three slots the deferred widgets fill.
         */
        const val SCHEMA: Int = 1

        /** What a widget draws before the app has ever written a snapshot. */
        val NOTHING = ClarityWidgetSnapshot()
    }
}

/**
 * One area, and the one thing happening in it.
 *
 * [queueCount] is what waits behind the active item, which is the count design-v3.md
 * 12.2 puts under the title on Next Up. It counts the queue and never the active item,
 * so an area with one thing in it and nothing behind it reads as zero rather than one.
 */
@Serializable
data class WidgetArea(
    val id: String,
    val name: String,
    /** `#RRGGBB`, straight from the area. Parsed with `parseAreaColor` on the far side. */
    val colorHex: String,
    val archived: Boolean = false,
    val activeItemId: String? = null,
    val activeItemTitle: String? = null,
    /** MASTER_BUILD_PROMPT 14b.2. Absent far more often than present, and that is fine. */
    val activeItemFirstStep: String? = null,
    val queueCount: Int = 0,
    /** The wall clock of the last event touching this area. Display and ordering only. */
    val lastEventAtMillis: Long = 0L,
) {
    val isIdle: Boolean get() = activeItemId == null
}

/**
 * The focus session this device is running. MASTER_BUILD_PROMPT section 10.
 *
 * [endsAtMillis] is absolute rather than a remaining duration, which is what lets the
 * Focus Countdown widget stay honest between updates: the system throttles widget
 * updates, so a stored remainder would be wrong by however long the update waited,
 * while an instant stays right until the session changes.
 */
@Serializable
data class WidgetFocus(
    val sessionId: String,
    val areaId: String,
    val itemId: String,
    val itemTitle: String,
    val startedAtMillis: Long,
    val endsAtMillis: Long,
    val plannedSeconds: Int,
)

/**
 * The fourteen day row, design-v3.md 12.2, exactly as Momentum renders it.
 *
 * **There is no field here that could become a streak.** Fourteen independent days and
 * a count of a set, never of a run, which is the same refusal `MomentumView` makes and
 * for the same reason. [line] is the plain sentence under the row, rendered by the app
 * from a queried number.
 */
@Serializable
data class WidgetRhythm(
    /** Oldest first, one per day. A gap is a lighter dot and nothing else is said. */
    val activeDays: List<Boolean> = emptyList(),
    /** Which of them is today, carried rather than assumed to be the last. */
    val todayIndex: Int = 0,
    /** The plain line under the row, rendered by the app from a queried number. */
    val line: String = "",
) {

    /** A count of a set, never of a run. */
    val activeCount: Int get() = activeDays.count { it }

    /** Fourteen. Carried rather than assumed, exactly as `ActivityWindow.length` is. */
    val length: Int get() = activeDays.size
}

/** Three figures from Momentum. No target, no ring, no fourth number. */
@Serializable
data class WidgetWeek(
    val completed: Int = 0,
    val focusMinutes: Int = 0,
    val reflections: Int = 0,
)

/**
 * The One Thing widget's content, design-v3.md 12.3.
 *
 * **[acceptedPlanLine] is only ever a plan the person accepted.** An unaccepted plan
 * and a declined one have no home screen presence at all, so the composer reads
 * `PlanState.isAccepted` and writes nothing otherwise. There is no state in which a
 * widget asks anybody to accept anything.
 *
 * [reportHeadline] is the fallback when no plan was accepted. It is a sentence the
 * engine rendered for the Report, carried whole.
 */
@Serializable
data class WidgetGuidance(
    val acceptedPlanLine: String? = null,
    val reportHeadline: String? = null,
)

/** What a widget found when it looked for the area it was configured with. */
sealed interface WidgetTarget {

    data class Live(val area: WidgetArea) : WidgetTarget

    /** Archived while a widget pointed at it. The name is still sayable. */
    data class Archived(val area: WidgetArea) : WidgetTarget

    /** Deleted while a widget pointed at it. There is no name left to say. */
    data object Deleted : WidgetTarget

    /** No areas at all, which is a real state and not an error. */
    data object NoAreas : WidgetTarget
}
