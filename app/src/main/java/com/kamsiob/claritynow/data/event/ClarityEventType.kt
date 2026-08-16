package com.kamsiob.claritynow.data.event

/**
 * The event catalog, MASTER_BUILD_PROMPT 5.2.
 *
 * Stored as the string name, never the ordinal, so reordering this enum can never
 * silently reinterpret an existing log. A type the running version does not know is
 * kept in the log and skipped by the reducer with a diagnostic, which is what lets
 * a newer desktop build and an older phone build share a file without data loss.
 */
enum class ClarityEventType {
    AREA_CREATED,
    AREA_RENAMED,
    AREA_RECOLORED,
    AREA_REORDERED,
    AREA_ARCHIVED,
    AREA_UNARCHIVED,
    AREA_DELETED,

    ITEM_ADDED,
    ITEM_EDITED,
    ITEM_QUEUED,
    ITEM_PROMOTED,
    ITEM_COMPLETED,
    ITEM_REOPENED,
    ITEM_REORDERED,
    ITEM_DELETED,

    FOCUS_STARTED,
    FOCUS_COMPLETED,
    FOCUS_ABANDONED,

    PULSE_GENERATED,
    PULSE_ANSWERED,

    REPORT_GENERATED,

    PLAN_OFFERED,
    PLAN_ACCEPTED,

    SETTING_CHANGED,
    ;

    companion object {
        fun fromName(name: String): ClarityEventType? = entries.firstOrNull { it.name == name }
    }
}

/**
 * The status of an item in its area's queue. Lives in the log vocabulary because
 * `previousStatus` is carried in payloads and must survive a schema change.
 */
enum class ItemStatus {
    /** The one thing happening in this area. At most one per live area, always. */
    ACTIVE,
    QUEUED,
    COMPLETED,

    /** Tombstoned. Rows are never removed, so history and snapshots survive. */
    DELETED,
}

/** Which window a Pulse observation described. MASTER_BUILD_PROMPT 11.3. */
enum class ReflectionPeriod {
    /** Generated before 17:00 local. The observation describes yesterday. */
    YESTERDAY,

    /** Generated at or after 17:00 local. The observation describes today so far. */
    TODAY_SO_FAR,
}
