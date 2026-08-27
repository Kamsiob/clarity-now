package com.kamsiob.claritynow.data.event

import com.kamsiob.claritynow.domain.engine.FactRef
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The event format is the contract between this app and the Linux desktop app that
 * will be written in a separate session. Without a test per event type the two
 * implementations drift, and drifting means data loss.
 *
 * `docs/EVENT_FORMAT.md` describes the same thing in prose. If one changes, so does
 * the other.
 *
 * Four of the twenty eight types and several of the payload fields arrived from
 * Addendum 01 Step 2 ahead of the phases that build their interfaces, on the
 * grounds that a payload change is nearly free before user data exists and painful
 * afterward. That makes this file the only thing checking them until those phases
 * land, so it covers the shapes rather than only the count.
 */
class EventFormatTest {

    /** One event of every type in the catalog. Adding a type without adding it here fails. */
    private val everyPayload: Map<ClarityEventType, EventPayload> = mapOf(
        ClarityEventType.AREA_CREATED to AreaCreated("area-1", "Work", "#2D7FF9", "a0"),
        ClarityEventType.AREA_RENAMED to AreaRenamed("area-1", "Work", "Studio"),
        ClarityEventType.AREA_RECOLORED to AreaRecolored("area-1", "#2D7FF9", "#22C55E"),
        ClarityEventType.AREA_REORDERED to AreaReordered("area-1", "a0", "a1"),
        ClarityEventType.AREA_ARCHIVED to AreaArchived("area-1", "Work"),
        ClarityEventType.AREA_UNARCHIVED to AreaUnarchived("area-1", "Work"),
        ClarityEventType.AREA_DELETED to AreaDeleted("area-1", "Work"),
        ClarityEventType.ITEM_ADDED to ItemAdded(
            itemId = "item-1",
            areaId = "area-1",
            title = "Call the printer",
            note = null,
            orderKey = "a0",
            areaNameSnapshot = "Work",
            estimateMinutes = 20,
            firstStep = "Find the invoice number",
        ),
        ClarityEventType.ITEM_FILED to ItemFiled("item-9", "area-1", "a3", "Work"),
        ClarityEventType.ITEM_EDITED to ItemEdited("item-1", "Call", "Call the printer", null, "today"),
        ClarityEventType.ITEM_ESTIMATED to ItemEstimated("item-1", 20, 45),
        ClarityEventType.ITEM_QUEUED to ItemQueued("item-1", "area-1", "a1", ItemStatus.ACTIVE),
        ClarityEventType.ITEM_PROMOTED to ItemPromoted(
            itemId = "item-2",
            areaId = "area-1",
            previousStatus = ItemStatus.QUEUED,
            demotedItemId = "item-1",
            demotedToOrderKey = "Zz",
            titleSnapshot = "Rewrite the proposal intro",
            areaNameSnapshot = "Work",
        ),
        ClarityEventType.ITEM_COMPLETED to ItemCompleted("item-1", "area-1", "Call the printer", "Work", 3),
        ClarityEventType.ITEM_REOPENED to ItemReopened("item-1", "area-1", "a0"),
        ClarityEventType.ITEM_REORDERED to ItemReordered("item-1", "area-1", "a0", "a2"),
        ClarityEventType.ITEM_DELETED to ItemDeleted("item-1", "area-1", "Call the printer"),
        ClarityEventType.FOCUS_STARTED to FocusStarted("focus-1", "area-1", "item-1", 1500),
        ClarityEventType.FOCUS_COMPLETED to FocusCompleted("focus-1", 1500),
        ClarityEventType.FOCUS_ENDED_EARLY to FocusEndedEarly("focus-1", 240),
        ClarityEventType.FOCUS_EXTENDED to FocusExtended("focus-1", 600, 2100),
        ClarityEventType.PULSE_GENERATED to PulseGenerated(
            pulseId = "pulse-1",
            dateKey = "2026-03-14",
            family = "persistence",
            escalationStage = 2,
            register = "REFLECTIVE",
            variantKey = "persistence.s2.11",
            renderedObservation = "Still Rewrite the proposal intro. Nine days now.",
            renderedQuestion = "Deep work, or stuck?",
            factSnapshot = mapOf("activeItemAgeDays" to "9"),
            reflectionPeriod = ReflectionPeriod.YESTERDAY,
            subjectId = "item-1",
            subjectKind = SubjectKind.ITEM,
        ),
        ClarityEventType.PULSE_ANSWERED to PulseAnswered(
            pulseId = "pulse-1",
            responseKey = "deep",
            responseLabel = "Deep work",
            responseIsPositive = true,
            subjectId = "item-1",
            subjectKind = SubjectKind.ITEM,
        ),
        ClarityEventType.REPORT_GENERATED to ReportGenerated(
            reportId = "report-1",
            weekStartKey = "2026-03-08",
            headlineKey = "steadyPace",
            renderedSections = listOf(
                ReportSectionSnapshot(
                    sectionKey = "observations",
                    sidehead = "Your week, honestly",
                    text = "Six things left.",
                    familyKey = "intakeVsOutput",
                    variantKey = "ob.flow.s1.l08",
                    escalationStage = 1,
                    register = "PLAIN",
                    subjectId = "area-1",
                    subjectKind = SubjectKind.AREA,
                ),
            ),
            factSnapshot = mapOf("completions" to "6"),
            headlineVariantKey = "hd.steady.01",
        ),
        ClarityEventType.PLAN_OFFERED to PlanOffered(
            planId = "plan-1",
            weekStartKey = "2026-03-08",
            frameKey = "frm.01",
            cueKey = "cue.band.01",
            actionKey = "act.neg.01",
            familyKey = "neglectedArea",
            subjectId = "area-1",
            offeredLine = "One option for Wednesday morning.",
            committedLine = "If it is Wednesday morning, I will start in Personal.",
            resolutionFactRef = FactRef("area", "eventsInWindow"),
        ),
        ClarityEventType.PLAN_ACCEPTED to PlanAccepted("plan-1"),
        ClarityEventType.SETTING_CHANGED to SettingChanged("afterCompleting", "AUTO_PROMOTE", "CHOOSE_FROM_QUEUE"),
        ClarityEventType.APP_OPENED to AppOpened("2026-03-14"),
    )

    @Test
    fun `the catalog and the fixture agree`() {
        val missing = ClarityEventType.entries - everyPayload.keys
        assertTrue("no fixture for: ${missing.joinToString()}", missing.isEmpty())
    }

    /**
     * The catalog is twenty eight types, and a twenty ninth has to be noticed.
     *
     * A count assertion looks like busywork next to the coverage test above, which
     * already fails on a type with no fixture. It is not the same guarantee. The
     * coverage test is satisfied by adding one line to a map, which is exactly what
     * somebody adding a type does without thinking, and it says nothing about the
     * two decisions that have to be made at the same time: the classification
     * against `ClarityEventType.isUserActivity`, which is written as a negation and
     * so counts a new type as something a person did unless it is told otherwise,
     * and the row shape in `TrailSentenceKey`, which decides whether the type is
     * visible in the transcript at all. DECISIONS.md C7 is the record of both very
     * nearly going wrong at once.
     *
     * So this fails on the twenty ninth type, loudly, and says what else to go and
     * do. MASTER_BUILD_PROMPT 5.2.
     */
    @Test
    fun `the catalog is twenty eight types`() {
        assertEquals(
            "the event catalog changed size. A new type is four decisions, not one: " +
                "the payload and its serializer, the row shape in TrailSentenceKey or " +
                "a deliberate null, the classification against " +
                "ClarityEventType.isUserActivity, which counts a new type as user " +
                "activity by default, and a place in the golden fixture. " +
                "MASTER_BUILD_PROMPT 5.2, DECISIONS.md C7.",
            CATALOG_SIZE,
            ClarityEventType.entries.size,
        )
        assertEquals(CATALOG_SIZE, everyPayload.size)
    }

    /**
     * The one type that was renamed, and the reason the rename was cheap.
     *
     * `FOCUS_ABANDONED` is written by a phase that has not shipped, so no log
     * anywhere contains the old spelling and there is no reader that has to accept
     * both. That is why `ClarityEvent.SCHEMA_VERSION` did not move. If this ever
     * fails because something reintroduced the old name, the version has to move
     * with it. DECISIONS.md C6.
     */
    @Test
    fun `the old focus type name is gone from the catalog entirely`() {
        assertNull(ClarityEventType.fromName("FOCUS_ABANDONED"))
        assertNotNull(ClarityEventType.fromName("FOCUS_ENDED_EARLY"))
        assertTrue(ClarityEventType.entries.none { it.name.contains("ABANDON") })
    }

    @Test
    fun `every payload round trips through its column form`() {
        everyPayload.forEach { (type, payload) ->
            val encoded = ClarityEventJson.encodePayload(payload)
            val decoded = ClarityEventJson.decodePayload(type, encoded)
            assertEquals(type.name, payload, decoded)
        }
    }

    @Test
    fun `every event round trips through the log form`() {
        everyPayload.entries.forEachIndexed { index, (type, payload) ->
            val event = ClarityEvent.of(
                id = "evt-$index",
                wallClock = 1_772_000_000_000L + index,
                lamport = index.toLong() + 1,
                originId = "device-a",
                payload = payload,
            )
            assertEquals(type, event.type)
            val text = ClarityEventJson.encodeLog(listOf(event))
            val decoded = ClarityEventJson.decodeLog(text)
            assertTrue(decoded.skippedTypes.isEmpty())
            assertEquals(type.name, event, decoded.events.single())
        }
    }

    @Test
    fun `the payload is a nested object rather than an escaped string`() {
        val event = ClarityEvent.of(
            id = "evt-1",
            wallClock = 1L,
            lamport = 1L,
            originId = "device-a",
            payload = everyPayload.getValue(ClarityEventType.AREA_CREATED),
        )
        val obj = ClarityEventJson.toJsonObject(event)
        assertEquals("Work", obj.getValue("payload").jsonObject.getValue("name").jsonPrimitive.content)
        assertEquals("AREA_CREATED", obj.getValue("type").jsonPrimitive.content)
    }

    @Test
    fun `entity id is derived from the payload`() {
        everyPayload.forEach { (type, payload) ->
            val event = ClarityEvent.of("e", 1L, 1L, "device-a", payload)
            assertEquals(type.name, payload.primaryEntityId, event.entityId)
        }
    }

    @Test
    fun `schema version is present on every event from the first one`() {
        val event = ClarityEvent.of("e", 1L, 1L, "device-a", everyPayload.getValue(ClarityEventType.AREA_CREATED))
        assertEquals(1, event.schemaVersion)
        assertEquals(1, ClarityEvent.SCHEMA_VERSION)
    }

    @Test
    fun `an unknown event type is skipped rather than refused`() {
        val text = """
            [
              {
                "id": "evt-future",
                "schemaVersion": 2,
                "type": "SOMETHING_NEWER",
                "wallClock": 1,
                "lamport": 1,
                "originId": "device-z",
                "entityId": null,
                "payload": { "whatever": true }
              }
            ]
        """.trimIndent()
        val decoded = ClarityEventJson.decodeLog(text)
        assertTrue(decoded.events.isEmpty())
        assertEquals(listOf("SOMETHING_NEWER"), decoded.skippedTypes)
    }

    @Test
    fun `an unknown payload field does not refuse the event`() {
        val payload = """{"areaId":"area-1","name":"Work","colorHex":"#2D7FF9","orderKey":"a0","futureField":7}"""
        val decoded = ClarityEventJson.decodePayload(ClarityEventType.AREA_CREATED, payload)
        assertEquals(AreaCreated("area-1", "Work", "#2D7FF9", "a0"), decoded)
    }

    @Test
    fun `a payload cannot be attached to the wrong type`() {
        val failure = runCatching {
            ClarityEvent(
                id = "e",
                schemaVersion = 1,
                type = ClarityEventType.ITEM_ADDED,
                wallClock = 1L,
                lamport = 1L,
                originId = "device-a",
                payload = AreaCreated("area-1", "Work", "#2D7FF9", "a0"),
                entityId = "area-1",
            )
        }
        assertNotNull(failure.exceptionOrNull())
        assertNull(failure.getOrNull())
    }

    @Test
    fun `nulls are written explicitly so a second implementation sees the field`() {
        val payload = ItemAdded("item-1", "area-1", "Call the printer", null, "a0", "Work")
        val text = ClarityEventJson.encodePayload(payload)
        assertTrue(text, text.contains("\"note\":null"))
        // The Addendum 01 fields default to null in Kotlin, and a default that is
        // omitted from the file is a field a second implementation never learns
        // exists. `encodeDefaults` plus `explicitNulls` is what stops that.
        assertTrue(text, text.contains("\"estimateMinutes\":null"))
        assertTrue(text, text.contains("\"firstStep\":null"))
    }

    /**
     * A capture with no area writes `"areaId": null`, and that is the inbox.
     *
     * The pair is the thing worth checking. DECISIONS.md C8 rejected a synthetic
     * inbox area because a placeholder area name eventually gets printed, and an
     * empty string sitting in `areaNameSnapshot` would be that same placeholder
     * wearing a different coat. A reader of this file has to be able to see the
     * difference between "no area" and "an area whose name I failed to record".
     */
    @Test
    fun `an unfiled capture writes a null area and a null area name, not an empty one`() {
        val payload = ItemAdded("item-1", null, "Look into the loft insulation", null, "a0", null)
        val text = ClarityEventJson.encodePayload(payload)
        assertTrue(text, text.contains("\"areaId\":null"))
        assertTrue(text, text.contains("\"areaNameSnapshot\":null"))
        val decoded = ClarityEventJson.decodePayload(ClarityEventType.ITEM_ADDED, text) as ItemAdded
        assertEquals(payload, decoded)
        assertNull(decoded.areaId)
        assertNull(decoded.areaNameSnapshot)
        // The entity is still the item. An unfiled item is a real item.
        assertEquals("item-1", decoded.primaryEntityId)
    }

    /**
     * A log written before the Addendum 01 fields existed still decodes, and every
     * new field reads as absent rather than as a value.
     *
     * This is the whole argument for leaving `ClarityEvent.SCHEMA_VERSION` at 1. A
     * version number exists so a reader can tell two shapes apart and accept both,
     * and no reader has to: every field the schema commit added is optional with a
     * null default, and `ignoreUnknownKeys` handles the other direction. Moving the
     * number with nothing to distinguish would spend the signal.
     */
    @Test
    fun `a payload written before the new fields existed decodes with them absent`() {
        val oldItem = ClarityEventJson.decodePayload(
            ClarityEventType.ITEM_ADDED,
            """{"itemId":"item-1","areaId":"area-1","title":"Call","note":null,""" +
                """"orderKey":"a0","areaNameSnapshot":"Work"}""",
        ) as ItemAdded
        assertNull(oldItem.estimateMinutes)
        assertNull(oldItem.firstStep)
        assertEquals("area-1", oldItem.areaId)

        val oldPulse = ClarityEventJson.decodePayload(
            ClarityEventType.PULSE_ANSWERED,
            """{"pulseId":"pulse-1","responseKey":"deep","responseLabel":"Deep work",""" +
                """"responseIsPositive":true}""",
        ) as PulseAnswered
        assertNull(oldPulse.subjectId)
        assertNull(oldPulse.subjectKind)
    }

    /**
     * `SubjectKind` is written as its name, never its ordinal.
     *
     * The same rule the event type itself follows, and for the same reason:
     * reordering an enum must never silently reinterpret an existing log. This one
     * is easy to miss because it is nested inside a payload rather than sitting in
     * a column of its own.
     */
    @Test
    fun `a subject kind is stored as its name`() {
        val text = ClarityEventJson.encodePayload(
            PulseAnswered("pulse-1", "deep", "Deep work", true, "area-1", SubjectKind.AREA),
        )
        assertTrue(text, text.contains("\"subjectKind\":\"AREA\""))
    }

    @Test
    fun `the log file is stable across encodings`() {
        val events = everyPayload.entries.mapIndexed { index, (_, payload) ->
            ClarityEvent.of("evt-$index", 1_772_000_000_000L + index, index + 1L, "device-a", payload)
        }
        assertEquals(ClarityEventJson.encodeLog(events), ClarityEventJson.encodeLog(events))
        // And parses as ordinary JSON, so anything can read it.
        Json.parseToJsonElement(ClarityEventJson.encodeLog(events))
    }

    private companion object {
        /** MASTER_BUILD_PROMPT 5.2. Twenty four at phase 3, twenty eight after Addendum 01. */
        const val CATALOG_SIZE = 28
    }
}
