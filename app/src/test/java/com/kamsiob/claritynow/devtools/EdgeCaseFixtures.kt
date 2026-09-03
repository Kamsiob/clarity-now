package com.kamsiob.claritynow.devtools

import com.kamsiob.claritynow.data.event.AppOpened
import com.kamsiob.claritynow.data.event.ClarityEvent
import com.kamsiob.claritynow.data.event.FocusCompleted
import com.kamsiob.claritynow.data.event.FocusStarted
import com.kamsiob.claritynow.data.event.ItemAdded
import com.kamsiob.claritynow.data.event.PulseAnswered
import com.kamsiob.claritynow.data.event.PulseGenerated
import com.kamsiob.claritynow.data.event.ReflectionPeriod
import com.kamsiob.claritynow.data.export.BackupCodec
import com.kamsiob.claritynow.data.export.BackupFixture
import com.kamsiob.claritynow.data.export.ExportSnapshot
import com.kamsiob.claritynow.domain.query.TEST_ZONE
import com.kamsiob.claritynow.domain.query.TrailTestLog
import com.kamsiob.claritynow.domain.query.area
import com.kamsiob.claritynow.domain.query.at
import com.kamsiob.claritynow.domain.query.complete
import com.kamsiob.claritynow.domain.query.item
import com.kamsiob.claritynow.domain.query.promote
import com.kamsiob.claritynow.domain.replay.ClarityReplay
import org.junit.Assume
import org.junit.Test
import java.io.File
import java.time.LocalDate

/**
 * Writes real, importable backup files for the content edge cases the polish pass has to
 * look at on a device.
 *
 * **Why this exists rather than tapping them in by hand.** A forty item queue is about two
 * hundred taps through a bottom sheet, twelve areas is another eighty, and neither can be
 * reproduced exactly a second time when a fix needs re-checking. These build the same
 * states from the app's own event constructors and write them through the app's own
 * `BackupCodec`, so what lands on the phone is a file the shipping importer accepts for
 * the shipping reasons, checksum and all.
 *
 * Never runs in the ordinary suite. `-PwriteFixtures=true` turns it on, the same shape of
 * switch `-PregenerateGolden=true` already uses, so a fixture is only rewritten when
 * somebody means to rewrite it.
 */
class EdgeCaseFixtures {

    private val out = File(System.getProperty("clarity.fixtureDir") ?: "/tmp/clarity-fixtures")

    private fun write(name: String, events: List<ClarityEvent>) {
        out.mkdirs()
        val snapshot = ExportSnapshot(
            events = events,
            state = ClarityReplay.replay(events),
            rebuildMatched = true,
        )
        val document = BackupCodec.write(
            snapshot = snapshot,
            password = null,
            appVersion = "0.12.0-fixture",
            createdAt = BackupFixture.NOW_MILLIS,
            createdOn = "2026-09-01",
            random = BackupFixture.fixedRandom(),
        )
        File(out, "$name.json").writeBytes(document)
        println("wrote $name.json, ${events.size} events, ${document.size} bytes")
    }

    private fun log() = TrailTestLog(BackupFixture.ORIGIN)

    /** An area with one active item and nothing behind it. */
    private fun TrailTestLog.stocked(
        day: Int,
        id: String,
        name: String,
        color: String,
        order: String,
        title: String,
    ) {
        area(at(day, 8), id, name, color, order)
        item(at(day, 9), "$id-1", id, title, "a0", name)
        promote(at(day, 9, 1), "$id-1", id, title, name)
    }

    @Test
    fun `write the edge case fixtures`() {
        Assume.assumeTrue(System.getProperty("clarity.writeFixtures") == "true")

        // ONE AREA. The narrowest live state: a single card carrying the whole screen.
        write(
            "one-area",
            log().apply {
                stocked(1, "solo", "Today", "#D8453A", "a0", "Rewrite the proposal introduction")
            }.events(),
        )

        // TWELVE AREAS, three times what onboarding suggests, and the case the Areas
        // screen, the Momentum balance list and the Trail filter row all have to survive.
        write(
            "twelve-areas",
            log().apply {
                val names = listOf(
                    "Work", "Health", "Family", "Learning", "Side project", "Home",
                    "Money", "Friends", "Garden", "The move", "Music", "Reading",
                )
                val colors = listOf(
                    "#2D7FF9", "#D8453A", "#F59E0B", "#10B981", "#6366F1", "#A16207",
                    "#0D9488", "#D946EF", "#22C55E", "#EF4444", "#7C3AED", "#64748B",
                )
                names.forEachIndexed { i, n ->
                    stocked(1 + i, "ar$i", n, colors[i], "a" + ORDER[i], "The one thing in $n")
                }
            }.events(),
        )

        // A FORTY ITEM QUEUE behind one active item. The area sheet lists every one of
        // them, so this is the case its scroll, its row rhythm and its Make active button
        // repeat under.
        write(
            "forty-queue",
            log().apply {
                area(at(1, 8), "deep", "Work", "#2D7FF9", "a0")
                item(at(1, 9), "deep-0", "deep", "Send the revised deck", "a0", "Work")
                promote(at(1, 9, 1), "deep-0", "deep", "Send the revised deck", "Work")
                repeat(40) { i ->
                    item(
                        at(2, 9, i),
                        "deep-q$i",
                        "deep",
                        QUEUE_TITLES[i % QUEUE_TITLES.size] + " " + (i + 1),
                        "b" + ORDER[i / 26] + ORDER[i % 26],
                        "Work",
                    )
                }
            }.events(),
        )

        // THE LONG STRINGS. A 40 character area name is the field's own cap, and the title
        // is deliberately past any width the layout reserves.
        write(
            "long-strings",
            log().apply {
                val areaName = "Reading and the long slow book pile"
                val title = "Finish reading the chapter about the second half of the " +
                    "nineteenth century before the reading group meets on Thursday"
                area(at(1, 8), "long", areaName, "#7C3AED", "a0")
                item(at(1, 9), "long-1", "long", title, "a0", areaName)
                promote(at(1, 9, 1), "long-1", "long", title, areaName)
                area(at(2, 8), "short", "Home", "#0D9488", "a1")
                item(at(2, 9), "short-1", "short", "Bin", "a0", "Home")
                promote(at(2, 9, 1), "short-1", "short", "Bin", "Home")
            }.events(),
        )

        // AN IDLE AREA, the state a person returns to after a gap, beside a live one so
        // the two treatments can be compared in one frame.
        write(
            "idle-and-live",
            log().apply {
                stocked(1, "live", "Work", "#2D7FF9", "a0", "Send the revised deck")
                area(at(1, 8), "idle", "Garden", "#22C55E", "a1")
                item(at(1, 9), "idle-1", "idle", "Cut back the ivy", "a0", "Garden")
                promote(at(1, 9, 1), "idle-1", "idle", "Cut back the ivy", "Garden")
                complete(at(2, 10), "idle-1", "idle", "Cut back the ivy", "Garden")
            }.events(),
        )

        // FOUR WEEKS ENDING YESTERDAY. Only written when an anchor is given, because
        // without one there is no honest day to count back from.
        System.getProperty("clarity.fixtureAnchor")
            ?.takeIf { it.isNotBlank() }
            ?.let {
                val anchor = LocalDate.parse(it)
                write("recent-week", recentWeek(anchor))
                write("readme", readme(anchor))
            }
    }

    /**
     * The state the README's screenshots are taken from.
     *
     * **A fixture rather than a hand tapped state, for the reason every other one here is
     * a fixture:** a screenshot that has to be retaken after a change needs the state it
     * was taken in to still exist, and two hundred taps is not a state, it is an
     * afternoon. It is written through the app's own `BackupCodec` and imported through
     * the app's own importer, so what is photographed is the real app holding real events.
     *
     * Four areas with the queue lengths the home screen now prints, one of them idle with
     * something waiting, and a week of completions behind them so the banner has something
     * true to say. Anchored to the day it is written, because the Areas banner and the
     * Report both describe a window that ends today.
     */
    private fun readme(anchor: LocalDate): List<ClarityEvent> {
        val log = log()
        fun on(daysBack: Int, hour: Int = 9, minute: Int = 0): Long =
            anchor.minusDays(daysBack.toLong())
                .atTime(hour, minute)
                .atZone(TEST_ZONE)
                .toInstant()
                .toEpochMilli()

        data class Seed(
            val id: String,
            val name: String,
            val color: String,
            val order: String,
            val active: String?,
            val step: String?,
            val queue: List<String>,
        )

        val seeds = listOf(
            Seed(
                "work", "Work", "#2D7FF9", "a0",
                "Send the revised deck", "Open the deck and read slide one",
                listOf("Book the follow up call", "Reply to the landlord", "Draft the handover note"),
            ),
            Seed(
                "health", "Health", "#D8453A", "a1",
                "Walk before the call", null,
                listOf("Book the eye test"),
            ),
            Seed(
                "family", "Family", "#F59E0B", "a2",
                "Plan the weekend", null,
                emptyList(),
            ),
            Seed(
                "reading", "Reading", "#7C3AED", "a3",
                null, null,
                listOf("Finish chapter three", "Start the second essay"),
            ),
        )

        seeds.forEachIndexed { index, seed ->
            log.area(on(21, 8, index), seed.id, seed.name, seed.color, seed.order)
        }

        // **The week's completions run first, and the order is the whole of why.** An
        // area holds one active item, so promoting a second is a conflict and the app
        // says so on a card. A fixture that promoted the standing item first and then
        // completed five others would import into three `While you were away` cards,
        // which is the app being right about a fixture that was wrong.
        listOf(
            Triple(6, "work", "Send the quarterly update"),
            Triple(5, "health", "Refill the prescription"),
            Triple(4, "work", "Chase the invoice"),
            Triple(3, "family", "Order the tickets"),
            Triple(2, "reading", "Read the first essay"),
        ).forEachIndexed { index, (back, area, title) ->
            val name = seeds.first { it.id == area }.name
            val id = "done-$index"
            log.item(on(back, 9, index), id, area, title, "c$index", name)
            log.promote(on(back, 9, index + 10), id, area, title, name)
            log.complete(on(back, 16), id, area, title, name)
        }

        // Then the standing item, into an area whose slot the completion above emptied,
        // and the queue behind it after that so nothing is promoted out of it.
        seeds.forEachIndexed { index, seed ->
            seed.active?.let { title ->
                log.add(
                    on(1, 9, index),
                    ItemAdded("${seed.id}-a", seed.id, title, null, "a0", seed.name, null, seed.step),
                )
                log.promote(on(1, 9, index + 30), "${seed.id}-a", seed.id, title, seed.name)
            }
            seed.queue.forEachIndexed { q, title ->
                log.item(on(1, 10, index * 5 + q), "${seed.id}-q$q", seed.id, title, "b$q", seed.name)
            }
        }

        log.add(on(1, 14), FocusStarted("readme-s1", "work", "work-a", 1_500))
        log.add(on(1, 14, 25), FocusCompleted("readme-s1", 1_500))
        (1..7).forEach { back ->
            log.add(on(back, 7), AppOpened(anchor.minusDays(back.toLong()).toString()))
        }
        return log.events()
    }

    /**
     * FOUR WEEKS ENDING YESTERDAY, so the Report has something to describe.
     *
     * Every other fixture here is a shape: one area, forty items, a long title. This one is
     * a *span*, and it is the only fixture whose instants cannot come from the suite's fixed
     * epoch. The Report describes the seven days before today and files what it composed, so
     * a fixture written against January 2026 produces an empty report in September and
     * proves nothing about either.
     *
     * Four weeks rather than one, because a report compares the week against the weeks
     * behind it: with nothing behind it the headline falls back and the pattern section,
     * which needs three weeks, is omitted. The middle two weeks are deliberately quieter
     * than the last, so the comparison has a direction.
     *
     * The anchor is passed in rather than read off the clock:
     *
     * ```
     * ./gradlew :app:testDebugUnitTest --tests '*EdgeCaseFixtures*' \
     *     -PwriteFixtures=true -PfixtureAnchor=2026-09-02
     * ```
     */
    private fun recentWeek(anchor: LocalDate): List<ClarityEvent> {
        val log = log()
        // Local noon of a day counted back from the anchor, in the fixture's own zone.
        fun on(daysBack: Int, hour: Int = 9, minute: Int = 0): Long =
            anchor.minusDays(daysBack.toLong())
                .atTime(hour, minute)
                .atZone(TEST_ZONE)
                .toInstant()
                .toEpochMilli()

        log.area(on(27, 8), "work", "Work", "#2D7FF9", "a0")
        log.area(on(27, 8, 1), "health", "Health", "#D8453A", "a1")
        log.area(on(27, 8, 2), "reading", "Reading", "#7C3AED", "a2")

        var n = 0
        // One ordinary day: an item captured, promoted and finished in an area.
        fun day(daysBack: Int, areaId: String, areaName: String, title: String) {
            val id = "rw-${n++}"
            log.item(on(daysBack, 9), id, areaId, title, "a$n", areaName)
            log.promote(on(daysBack, 9, 1), id, areaId, title, areaName)
            log.complete(on(daysBack, 17), id, areaId, title, areaName)
        }

        // Weeks four and three: a steady baseline in two areas.
        listOf(26, 24, 22, 20, 18, 16, 15).forEachIndexed { i, back ->
            day(back, if (i % 2 == 0) "work" else "health", if (i % 2 == 0) "Work" else "Health", BASELINE[i])
        }
        // Week two, quieter, so the last week has something to be busier than.
        listOf(13, 11, 9).forEachIndexed { i, back ->
            day(back, "work", "Work", QUIET[i])
        }
        // The described week. Seven days, six of them with something in them, one addition
        // left in the queue so the week does not read as perfectly cleared.
        listOf(7, 6, 5, 4, 2, 1).forEachIndexed { i, back ->
            day(back, if (i % 3 == 2) "reading" else "work", if (i % 3 == 2) "Reading" else "Work", LAST[i])
        }
        log.item(on(3, 10), "rw-waiting", "health", "Book the eye test", "b0", "Health")

        // Two sessions in the described week, which is what the Focus section reads.
        log.add(on(6, 14), FocusStarted("rw-s1", "work", "rw-10", 1_500))
        log.add(on(6, 14, 25), FocusCompleted("rw-s1", 1_500))
        log.add(on(2, 11), FocusStarted("rw-s2", "reading", "rw-14", 1_500))
        log.add(on(2, 11, 25), FocusCompleted("rw-s2", 1_500))

        // One answered Pulse, so `What you said` has something of the person's own in it.
        val pulseDay = anchor.minusDays(4)
        log.add(
            on(4, 18),
            PulseGenerated(
                pulseId = "rw-pulse",
                dateKey = pulseDay.toString(),
                family = "completionSplit",
                escalationStage = 1,
                register = "PLAIN",
                variantKey = "pl.split.s1.l02",
                renderedObservation = "Most of what you finished this week was in Work.",
                renderedQuestion = "Was that where you meant to be?",
                factSnapshot = mapOf("completions" to "6"),
                reflectionPeriod = ReflectionPeriod.TODAY_SO_FAR,
            ),
        )
        log.add(
            on(4, 18, 1),
            PulseAnswered(
                pulseId = "rw-pulse",
                responseKey = "yes",
                responseLabel = "Yes, that was the plan",
                responseIsPositive = true,
            ),
        )

        // Presence, every day of the described week and a few before it.
        (1..20).forEach { back ->
            log.add(on(back, 7), AppOpened(anchor.minusDays(back.toLong()).toString()))
        }
        return log.events()
    }

    private companion object {
        const val ORDER = "abcdefghijklmnopqrstuvwxyz"

        val BASELINE = listOf(
            "Send the revised deck", "Walk before the call", "Reply to the landlord",
            "Stretch after work", "Draft the handover note", "Refill the prescription",
            "Book the follow up call",
        )
        val QUIET = listOf("Clear the reading pile", "Chase the invoice", "Sort the photos")
        val LAST = listOf(
            "Write the summary", "Finish chapter three", "Renew the parking permit",
            "Update the address", "Read the second essay", "Order more coffee",
        )

        val QUEUE_TITLES = listOf(
            "Book the follow up call", "Clear the reading pile", "Reply to the landlord",
            "Renew the parking permit", "Draft the handover note", "Chase the invoice",
            "Sort the photos", "Fix the bathroom light", "Order more coffee",
            "Update the address", "Cancel the old subscription", "Write the thank you note",
        )
    }
}
