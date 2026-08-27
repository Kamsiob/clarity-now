package com.kamsiob.claritynow.data.event

/**
 * The event catalog, MASTER_BUILD_PROMPT 5.2.
 *
 * Stored as the string name, never the ordinal, so reordering this enum can never
 * silently reinterpret an existing log. A type the running version does not know is
 * kept in the log and skipped by the reducer with a diagnostic, which is what lets
 * a newer desktop build and an older phone build share a file without data loss.
 *
 * Twenty eight types. Four of them, ITEM_FILED, ITEM_ESTIMATED, APP_OPENED and
 * FOCUS_EXTENDED, arrived from Addendum 01 Step 2 well ahead of the phases that
 * build their interfaces, and FOCUS_ABANDONED was renamed to FOCUS_ENDED_EARLY in
 * the same commit. That is deliberate rather than premature: a payload change is
 * nearly free before user data exists and expensive afterward, and a type name is
 * worse than a payload field because it appears in the log, the reducer, the query
 * facade, the golden fixture and `docs/EVENT_FORMAT.md` at once. DECISIONS.md C6.
 *
 * **Every new type is classified against `ClarityEventType.isUserActivity` in the
 * same commit that adds it, with the classification argued in a comment.** That
 * predicate is written as a negation, so a type added without a second thought
 * counts as something a person did. APP_OPENED is the case that proves the rule and
 * DECISIONS.md C7 works through what it would have cost.
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
    ITEM_FILED,
    ITEM_EDITED,
    ITEM_ESTIMATED,
    ITEM_QUEUED,
    ITEM_PROMOTED,
    ITEM_COMPLETED,
    ITEM_REOPENED,
    ITEM_REORDERED,
    ITEM_DELETED,

    FOCUS_STARTED,
    FOCUS_COMPLETED,
    FOCUS_ENDED_EARLY,
    FOCUS_EXTENDED,

    PULSE_GENERATED,
    PULSE_ANSWERED,

    REPORT_GENERATED,

    PLAN_OFFERED,
    PLAN_ACCEPTED,

    SETTING_CHANGED,

    /** A date key and nothing else. Never counted as activity. See [AppOpened]. */
    APP_OPENED,
    ;

    companion object {
        fun fromName(name: String): ClarityEventType? = entries.firstOrNull { it.name == name }
    }
}

/**
 * The status of an item in its area's queue. Lives in the log vocabulary because
 * `previousStatus` is carried in payloads and must survive a schema change.
 *
 * An item with no area has a status too, and it is always QUEUED. ACTIVE and
 * COMPLETED are area scoped states: at most one ACTIVE per area is the app's whole
 * mechanic, and there is no area to be the one thing in. Addendum 01 4a states the
 * limit and `ClarityInvariants` detects a state that breaks it.
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

/**
 * What kind of thing an observation was about. CLARITY_LOGIC_ENGINE.md 2.1.
 *
 * Declared here rather than in `domain.engine` for the same reason [ItemStatus] is
 * here: it is carried in a payload, so it belongs to the log vocabulary and has to
 * survive a schema change independently of whatever the engine's own types look
 * like when they are written. 2.1's `Subject` pairs an id with one of these.
 *
 * **An id on its own cannot be resolved back to its kind, and guessing is banned.**
 * `TrailQueries.areaIdOf` already refuses for exactly this reason: PLAN_OFFERED
 * carries a `subjectId` and no kind, so testing that id against the known area ids
 * would work in practice and is a heuristic no document authorizes. Storing the
 * kind removes the temptation rather than documenting against it.
 *
 * This is the proposed shape for issue #19 and the owner may still adjust it.
 */
enum class SubjectKind { AREA, ITEM }
