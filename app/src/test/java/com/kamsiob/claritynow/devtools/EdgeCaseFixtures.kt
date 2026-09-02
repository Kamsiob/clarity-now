package com.kamsiob.claritynow.devtools

import com.kamsiob.claritynow.data.event.ClarityEvent
import com.kamsiob.claritynow.data.export.BackupCodec
import com.kamsiob.claritynow.data.export.BackupFixture
import com.kamsiob.claritynow.data.export.ExportSnapshot
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
    }

    private companion object {
        const val ORDER = "abcdefghijklmnopqrstuvwxyz"

        val QUEUE_TITLES = listOf(
            "Book the follow up call", "Clear the reading pile", "Reply to the landlord",
            "Renew the parking permit", "Draft the handover note", "Chase the invoice",
            "Sort the photos", "Fix the bathroom light", "Order more coffee",
            "Update the address", "Cancel the old subscription", "Write the thank you note",
        )
    }
}
