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
        ClarityEventType.ITEM_ADDED to ItemAdded("item-1", "area-1", "Call the printer", null, "a0", "Work"),
        ClarityEventType.ITEM_EDITED to ItemEdited("item-1", "Call", "Call the printer", null, "today"),
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
        ClarityEventType.FOCUS_ABANDONED to FocusAbandoned("focus-1", 240),
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
        ),
        ClarityEventType.PULSE_ANSWERED to PulseAnswered("pulse-1", "deep", "Deep work", true),
        ClarityEventType.REPORT_GENERATED to ReportGenerated(
            reportId = "report-1",
            weekStartKey = "2026-03-08",
            headlineKey = "steadyPace",
            renderedSections = listOf(
                ReportSectionSnapshot("observations", "Your week, honestly", "Six things left."),
            ),
            factSnapshot = mapOf("completions" to "6"),
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
    )

    @Test
    fun `the catalog and the fixture agree`() {
        val missing = ClarityEventType.entries - everyPayload.keys
        assertTrue("no fixture for: ${missing.joinToString()}", missing.isEmpty())
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
}
