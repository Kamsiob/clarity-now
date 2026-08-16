package com.kamsiob.claritynow.domain.replay

import com.kamsiob.claritynow.data.event.AreaArchived
import com.kamsiob.claritynow.data.event.AreaCreated
import com.kamsiob.claritynow.data.event.AreaDeleted
import com.kamsiob.claritynow.data.event.AreaRecolored
import com.kamsiob.claritynow.data.event.AreaRenamed
import com.kamsiob.claritynow.data.event.AreaReordered
import com.kamsiob.claritynow.data.event.AreaUnarchived
import com.kamsiob.claritynow.data.event.ClarityEvent
import com.kamsiob.claritynow.data.event.EventPayload
import com.kamsiob.claritynow.data.event.FocusAbandoned
import com.kamsiob.claritynow.data.event.FocusCompleted
import com.kamsiob.claritynow.data.event.FocusStarted
import com.kamsiob.claritynow.data.event.ItemAdded
import com.kamsiob.claritynow.data.event.ItemCompleted
import com.kamsiob.claritynow.data.event.ItemDeleted
import com.kamsiob.claritynow.data.event.ItemEdited
import com.kamsiob.claritynow.data.event.ItemPromoted
import com.kamsiob.claritynow.data.event.ItemQueued
import com.kamsiob.claritynow.data.event.ItemReopened
import com.kamsiob.claritynow.data.event.ItemReordered
import com.kamsiob.claritynow.data.event.ItemStatus
import com.kamsiob.claritynow.data.event.PlanAccepted
import com.kamsiob.claritynow.data.event.PlanOffered
import com.kamsiob.claritynow.data.event.PulseAnswered
import com.kamsiob.claritynow.data.event.PulseGenerated
import com.kamsiob.claritynow.data.event.ReflectionPeriod
import com.kamsiob.claritynow.data.event.ReportGenerated
import com.kamsiob.claritynow.data.event.ReportSectionSnapshot
import com.kamsiob.claritynow.data.event.SettingChanged
import com.kamsiob.claritynow.domain.engine.FactRef

/**
 * The canonical event stream committed as `testdata/golden-log.json`, and the exact
 * state it must produce, committed as `testdata/golden-state.json`.
 *
 * These two files are the contract between this app and the Linux desktop app that
 * will be built in a separate session. A second implementation that replays this log
 * and reaches this state agrees with this one about everything that matters.
 *
 * The stream is hand written rather than generated, so it reads as a small life:
 * three areas, a fortnight of ordinary use, one week closed with a report, and two
 * deliberate divergences from a second device at the end. It exercises all twenty
 * four event types and both conflict kinds.
 */
object GoldenFixture {

    const val LOG_PATH = "../testdata/golden-log.json"
    const val STATE_PATH = "../testdata/golden-state.json"

    private const val DAY = 86_400_000L

    /** 2026-01-04T09:00:00Z, a Sunday, so the first week closes on a week boundary. */
    private const val START = 1_767_484_800_000L + 9 * 3_600_000L

    private const val PHONE = "01947b3f-0000-4000-8000-000000000001"
    private const val LAPTOP = "01947b3f-0000-4000-8000-000000000002"

    private val phoneJitter = OrderKey.jitterFor(PHONE)

    fun log(): List<ClarityEvent> = Builder().build()

    fun state(): ClarityState = ClarityReplay.replay(log())

    private class Builder {
        private val events = mutableListOf<ClarityEvent>()
        private var lamport = 0L
        private var sequence = 0

        // Precomputed so the two head insertions in Personal cannot collide.
        private val personalHeadA = OrderKey.between(null, "a0", phoneJitter)
        private val personalHeadB = OrderKey.between(null, personalHeadA, phoneJitter)
        private val personalHeadC = OrderKey.between(null, personalHeadB, phoneJitter)

        fun build(): List<ClarityEvent> {
            week1()
            week2()
            weekClose()
            divergence()
            return events
        }

        /** Ids are readable and fixed. A UUID here would make the file unreviewable. */
        private fun at(dayOffset: Long, hour: Int, origin: String = PHONE, payload: EventPayload) {
            lamport += 1
            sequence += 1
            events += ClarityEvent.of(
                id = "evt-%03d".format(sequence),
                wallClock = START + dayOffset * DAY + (hour - 9) * 3_600_000L,
                lamport = lamport,
                originId = origin,
                payload = payload,
            )
        }

        // Day 0 to 6. Three areas, first items, first completion.
        private fun week1() {
            at(0, 9, payload = AreaCreated("area-work", "Work", "#2D7FF9", OrderKey.first(phoneJitter)))
            at(0, 9, payload = AreaCreated("area-personal", "Personal", "#6366F1", "a1"))
            at(0, 9, payload = AreaCreated("area-health", "Health", "#22C55E", "a2"))
            at(0, 10, payload = AreaCreated("area-scratch", "Side Project", "#F59E0B", "a3"))

            at(0, 10, payload = ItemAdded("item-proposal", "area-work", "Rewrite the proposal intro", null, "a0", "Work"))
            at(0, 10, payload = ItemPromoted("item-proposal", "area-work", ItemStatus.QUEUED, null, null, "Rewrite the proposal intro", "Work"))
            at(0, 10, payload = ItemAdded("item-printer", "area-work", "Call the printer", "before five", "a1", "Work"))
            at(0, 11, payload = ItemAdded("item-notes", "area-work", "Draft the release notes", null, "a2", "Work"))

            at(0, 11, payload = ItemAdded("item-dentist", "area-personal", "Book the dentist", null, "a0", "Personal"))
            at(0, 11, payload = ItemPromoted("item-dentist", "area-personal", ItemStatus.QUEUED, null, null, "Book the dentist", "Personal"))
            at(0, 12, payload = ItemAdded("item-tap", "area-personal", "Fix the leaking tap", null, "a1", "Personal"))

            at(1, 8, payload = ItemAdded("item-walk", "area-health", "Walk before breakfast", null, "a0", "Health"))
            at(1, 8, payload = ItemPromoted("item-walk", "area-health", ItemStatus.QUEUED, null, null, "Walk before breakfast", "Health"))

            at(1, 14, payload = FocusStarted("focus-1", "area-work", "item-proposal", 1500))
            at(1, 15, payload = FocusCompleted("focus-1", 1500))

            at(2, 9, payload = PulseGenerated(
                pulseId = "pulse-1",
                dateKey = "2026-01-06",
                family = "persistence",
                escalationStage = 1,
                register = "OBSERVATIONAL",
                variantKey = "persistence.s1.04",
                renderedObservation = "Rewrite the proposal intro has been active for two days.",
                renderedQuestion = "Still the right thing?",
                factSnapshot = mapOf("activeItemAgeDays" to "2"),
                reflectionPeriod = ReflectionPeriod.YESTERDAY,
            ))
            at(2, 9, payload = PulseAnswered("pulse-1", "yes", "Still the right thing", true))

            at(2, 17, payload = ItemCompleted("item-dentist", "area-personal", "Book the dentist", "Personal", 2))
            at(2, 17, payload = ItemPromoted("item-tap", "area-personal", ItemStatus.QUEUED, null, null, "Fix the leaking tap", "Personal"))

            at(3, 10, payload = ItemEdited("item-printer", "Call the printer", "Call the printer about the covers", "before five", "before five, ask for Dan"))
            at(3, 11, payload = ItemReordered("item-notes", "area-work", "a2", OrderKey.between("a0", "a1", phoneJitter)))

            at(4, 9, payload = FocusStarted("focus-2", "area-work", "item-proposal", 1500))
            at(4, 9, payload = FocusAbandoned("focus-2", 320))

            at(5, 20, payload = SettingChanged("afterCompleting", "AUTO_PROMOTE", "CHOOSE_FROM_QUEUE"))
            at(6, 9, payload = AreaRecolored("area-health", "#22C55E", "#10B981"))
        }

        // Day 7 to 13. A swap, an archive, a delete, a reopen.
        private fun week2() {
            at(7, 9, payload = ItemPromoted(
                itemId = "item-printer",
                areaId = "area-work",
                previousStatus = ItemStatus.QUEUED,
                demotedItemId = "item-proposal",
                demotedToOrderKey = OrderKey.between(null, "a0", phoneJitter),
                titleSnapshot = "Call the printer about the covers",
                areaNameSnapshot = "Work",
            ))
            at(7, 16, payload = ItemCompleted("item-printer", "area-work", "Call the printer about the covers", "Work", 1))
            at(7, 16, payload = ItemPromoted("item-proposal", "area-work", ItemStatus.QUEUED, null, null, "Rewrite the proposal intro", "Work"))

            at(8, 10, payload = AreaRenamed("area-scratch", "Side Project", "Workshop"))
            at(8, 10, payload = AreaReordered("area-scratch", "a3", OrderKey.between("a0", "a1", phoneJitter)))
            at(8, 11, payload = AreaArchived("area-scratch", "Workshop"))

            at(9, 9, payload = ItemDeleted("item-notes", "area-work", "Draft the release notes"))
            at(9, 12, payload = ItemReopened("item-dentist", "area-personal", personalHeadA))

            at(10, 8, payload = ItemCompleted("item-walk", "area-health", "Walk before breakfast", "Health", 9))
            at(10, 8, payload = ItemAdded("item-swim", "area-health", "Swim on Thursday", null, "a1", "Health"))
            at(10, 8, payload = ItemPromoted("item-swim", "area-health", ItemStatus.QUEUED, null, null, "Swim on Thursday", "Health"))

            at(11, 9, payload = AreaUnarchived("area-scratch", "Workshop"))
            at(11, 9, payload = AreaArchived("area-scratch", "Workshop"))
            at(11, 10, payload = AreaDeleted("area-scratch", "Workshop"))

            at(12, 9, payload = ItemAdded("item-letter", "area-personal", "Write the landlord a letter", null, "a2", "Personal"))

            // Putting the active item back in the queue without promoting anything,
            // which leaves the area idle on purpose.
            at(12, 21, payload = ItemQueued("item-swim", "area-health", OrderKey.first(phoneJitter), ItemStatus.ACTIVE))
        }

        // The week closes: a report, a plan offered and accepted.
        private fun weekClose() {
            at(13, 8, payload = ReportGenerated(
                reportId = "report-1",
                weekStartKey = "2026-01-11",
                headlineKey = "steadyPace",
                renderedSections = listOf(
                    ReportSectionSnapshot(
                        sectionKey = "observations",
                        sidehead = "Your week, honestly",
                        text = "Three things left Work this week and two arrived.",
                    ),
                    ReportSectionSnapshot(
                        sectionKey = "focus",
                        sidehead = "Focus",
                        text = "You protected 25 minutes on Monday.",
                    ),
                ),
                factSnapshot = mapOf("completions" to "3", "additions" to "2", "focusMinutes" to "25"),
            ))
            at(13, 8, payload = PlanOffered(
                planId = "plan-1",
                weekStartKey = "2026-01-11",
                frameKey = "frm.03",
                cueKey = "cue.band.02",
                actionKey = "act.neg.05",
                familyKey = "neglectedArea",
                subjectId = "area-personal",
                offeredLine = "One option for Wednesday morning: ten minutes in Personal before you open Work.",
                committedLine = "If it is Wednesday morning, I will spend ten minutes in Personal before opening Work.",
                resolutionFactRef = FactRef("area", "eventsInWindow"),
            ))
            at(13, 8, payload = PlanAccepted("plan-1"))
        }

        /**
         * Two devices that were apart. The laptop promoted a different item in Work
         * at the same logical time, and generated its own Pulse for the same day.
         * Both resolve by the higher (lamport, originId), and both are recorded.
         */
        private fun divergence() {
            // Both devices did an ordinary swap in Personal, each unaware of the
            // other, and each correctly named the item it was demoting. Neither
            // event is wrong; they simply disagree about what is active now.
            val contested = lamport + 1
            at(14, 9, PHONE, ItemPromoted(
                itemId = "item-letter",
                areaId = "area-personal",
                previousStatus = ItemStatus.QUEUED,
                demotedItemId = "item-tap",
                demotedToOrderKey = personalHeadB,
                titleSnapshot = "Write the landlord a letter",
                areaNameSnapshot = "Personal",
            ))
            lamport = contested - 1
            at(14, 9, LAPTOP, ItemPromoted(
                itemId = "item-dentist",
                areaId = "area-personal",
                previousStatus = ItemStatus.QUEUED,
                demotedItemId = "item-tap",
                demotedToOrderKey = personalHeadC,
                titleSnapshot = "Book the dentist",
                areaNameSnapshot = "Personal",
            ))

            val pulseLamport = lamport + 1
            at(14, 20, PHONE, PulseGenerated(
                pulseId = "pulse-2-phone",
                dateKey = "2026-01-18",
                family = "switching",
                escalationStage = 1,
                register = "PLAIN",
                variantKey = "switching.s1.02",
                renderedObservation = "Personal changed hands today.",
                renderedQuestion = "New priority, or second thoughts?",
                factSnapshot = mapOf("swaps" to "1"),
                reflectionPeriod = ReflectionPeriod.TODAY_SO_FAR,
            ))
            lamport = pulseLamport - 1
            at(14, 20, LAPTOP, PulseGenerated(
                pulseId = "pulse-2-laptop",
                dateKey = "2026-01-18",
                family = "switching",
                escalationStage = 1,
                register = "PLAIN",
                variantKey = "switching.s1.07",
                renderedObservation = "Something in Personal moved aside today.",
                renderedQuestion = "New priority, or second thoughts?",
                factSnapshot = mapOf("swaps" to "1"),
                reflectionPeriod = ReflectionPeriod.TODAY_SO_FAR,
            ))
        }
    }
}
