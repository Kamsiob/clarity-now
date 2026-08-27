package com.kamsiob.claritynow.data.event

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull

/**
 * The on disk and on wire shape of the event log. This is the contract between
 * this app and the Linux desktop app that will be written later, and
 * `docs/EVENT_FORMAT.md` describes it in prose. Changing anything here changes
 * that contract.
 *
 * Payloads are nested objects rather than escaped strings, so a person can read a
 * log file and a second implementation can parse it without unwrapping twice. The
 * Room column stores the payload object serialized on its own, which is the only
 * place the string form appears.
 */
object ClarityEventJson {

    /**
     * `ignoreUnknownKeys` is deliberate and load bearing for sync. A newer build
     * adding a payload field must not make an older build refuse the whole log.
     */
    private val compact = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
        explicitNulls = true
    }

    private val pretty = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
        explicitNulls = true
        prettyPrint = true
        prettyPrintIndent = "  "
    }

    @Suppress("UNCHECKED_CAST")
    fun serializerFor(type: ClarityEventType): KSerializer<EventPayload> = when (type) {
        ClarityEventType.AREA_CREATED -> AreaCreated.serializer()
        ClarityEventType.AREA_RENAMED -> AreaRenamed.serializer()
        ClarityEventType.AREA_RECOLORED -> AreaRecolored.serializer()
        ClarityEventType.AREA_REORDERED -> AreaReordered.serializer()
        ClarityEventType.AREA_ARCHIVED -> AreaArchived.serializer()
        ClarityEventType.AREA_UNARCHIVED -> AreaUnarchived.serializer()
        ClarityEventType.AREA_DELETED -> AreaDeleted.serializer()
        ClarityEventType.ITEM_ADDED -> ItemAdded.serializer()
        ClarityEventType.ITEM_FILED -> ItemFiled.serializer()
        ClarityEventType.ITEM_EDITED -> ItemEdited.serializer()
        ClarityEventType.ITEM_ESTIMATED -> ItemEstimated.serializer()
        ClarityEventType.ITEM_QUEUED -> ItemQueued.serializer()
        ClarityEventType.ITEM_PROMOTED -> ItemPromoted.serializer()
        ClarityEventType.ITEM_COMPLETED -> ItemCompleted.serializer()
        ClarityEventType.ITEM_REOPENED -> ItemReopened.serializer()
        ClarityEventType.ITEM_REORDERED -> ItemReordered.serializer()
        ClarityEventType.ITEM_DELETED -> ItemDeleted.serializer()
        ClarityEventType.FOCUS_STARTED -> FocusStarted.serializer()
        ClarityEventType.FOCUS_COMPLETED -> FocusCompleted.serializer()
        ClarityEventType.FOCUS_ENDED_EARLY -> FocusEndedEarly.serializer()
        ClarityEventType.FOCUS_EXTENDED -> FocusExtended.serializer()
        ClarityEventType.PULSE_GENERATED -> PulseGenerated.serializer()
        ClarityEventType.PULSE_ANSWERED -> PulseAnswered.serializer()
        ClarityEventType.REPORT_GENERATED -> ReportGenerated.serializer()
        ClarityEventType.PLAN_OFFERED -> PlanOffered.serializer()
        ClarityEventType.PLAN_ACCEPTED -> PlanAccepted.serializer()
        ClarityEventType.SETTING_CHANGED -> SettingChanged.serializer()
        ClarityEventType.APP_OPENED -> AppOpened.serializer()
    } as KSerializer<EventPayload>

    /** The exact string stored in the `payload` column. */
    fun encodePayload(payload: EventPayload): String =
        compact.encodeToString(serializerFor(ClarityEvent.typeOf(payload)), payload)

    fun decodePayload(type: ClarityEventType, json: String): EventPayload =
        compact.decodeFromString(serializerFor(type), json)

    fun toJsonObject(event: ClarityEvent): JsonObject = buildJsonObject {
        put("id", JsonPrimitive(event.id))
        put("schemaVersion", JsonPrimitive(event.schemaVersion))
        put("type", JsonPrimitive(event.type.name))
        put("wallClock", JsonPrimitive(event.wallClock))
        put("lamport", JsonPrimitive(event.lamport))
        put("originId", JsonPrimitive(event.originId))
        put("entityId", event.entityId?.let(::JsonPrimitive) ?: kotlinx.serialization.json.JsonNull)
        put(
            "payload",
            compact.encodeToJsonElement(serializerFor(event.type), event.payload),
        )
    }

    /** Returns null for an event type this build does not know. The caller records it. */
    fun fromJsonObject(obj: JsonObject): ClarityEvent? {
        val typeName = obj["type"]?.jsonPrimitive?.contentOrNull ?: return null
        val type = ClarityEventType.fromName(typeName) ?: return null
        val payload = compact.decodeFromJsonElement(
            serializerFor(type),
            obj.getValue("payload").jsonObject,
        )
        return ClarityEvent(
            id = obj.getValue("id").jsonPrimitive.content,
            schemaVersion = obj["schemaVersion"]?.jsonPrimitive?.content?.toIntOrNull()
                ?: ClarityEvent.SCHEMA_VERSION,
            type = type,
            wallClock = obj.getValue("wallClock").jsonPrimitive.longOrNull ?: 0L,
            lamport = obj.getValue("lamport").jsonPrimitive.longOrNull ?: 0L,
            originId = obj.getValue("originId").jsonPrimitive.content,
            payload = payload,
            entityId = obj["entityId"]?.jsonPrimitive?.contentOrNull,
        )
    }

    data class DecodedLog(val events: List<ClarityEvent>, val skippedTypes: List<String>)

    fun encodeLog(events: List<ClarityEvent>): String {
        val array = kotlinx.serialization.json.JsonArray(events.map(::toJsonObject))
        return pretty.encodeToString(kotlinx.serialization.json.JsonArray.serializer(), array)
    }

    fun decodeLog(text: String): DecodedLog {
        val array = pretty.parseToJsonElement(text) as kotlinx.serialization.json.JsonArray
        val events = ArrayList<ClarityEvent>(array.size)
        val skipped = ArrayList<String>()
        for (element in array) {
            val obj = element.jsonObject
            val event = fromJsonObject(obj)
            if (event == null) {
                skipped += obj["type"]?.jsonPrimitive?.contentOrNull ?: "unknown"
            } else {
                events += event
            }
        }
        return DecodedLog(events, skipped)
    }
}
