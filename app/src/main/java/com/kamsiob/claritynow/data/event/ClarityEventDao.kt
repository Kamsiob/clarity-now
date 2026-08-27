package com.kamsiob.claritynow.data.event

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kamsiob.claritynow.data.db.ClarityEventRow
import kotlinx.coroutines.flow.Flow

/**
 * The only way into the log, and there is no update or delete for a single row.
 *
 * `IGNORE` on insert is what makes delivering the same event twice a no op at the
 * storage layer, which is half of the idempotency guarantee. The other half is the
 * dedupe in `inTotalOrder`.
 */
@Dao
interface ClarityEventDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun append(event: ClarityEventRow): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun appendAll(events: List<ClarityEventRow>): List<Long>

    @Query("SELECT * FROM clarity_event ORDER BY lamport ASC, originId ASC, id ASC")
    suspend fun allInOrder(): List<ClarityEventRow>

    @Query("SELECT * FROM clarity_event ORDER BY lamport ASC, originId ASC, id ASC")
    fun observeAllInOrder(): Flow<List<ClarityEventRow>>

    @Query(
        """
        SELECT * FROM clarity_event
        WHERE lamport > :lamport
           OR (lamport = :lamport AND originId > :originId)
           OR (lamport = :lamport AND originId = :originId AND id > :eventId)
        ORDER BY lamport ASC, originId ASC, id ASC
        """,
    )
    suspend fun after(lamport: Long, originId: String, eventId: String): List<ClarityEventRow>

    @Query("SELECT COALESCE(MAX(lamport), 0) FROM clarity_event")
    suspend fun maxLamport(): Long

    @Query("SELECT COUNT(*) FROM clarity_event")
    suspend fun count(): Int

    @Query("SELECT EXISTS(SELECT 1 FROM clarity_event WHERE id = :id)")
    suspend fun exists(id: String): Boolean

    @Query("SELECT * FROM clarity_event WHERE entityId = :entityId ORDER BY lamport ASC, originId ASC, id ASC")
    suspend fun forEntity(entityId: String): List<ClarityEventRow>

    @Query(
        """
        SELECT * FROM clarity_event
        WHERE type IN (:types)
        ORDER BY lamport ASC, originId ASC, id ASC
        """,
    )
    suspend fun ofTypes(types: List<String>): List<ClarityEventRow>

    // The Trail path ----------------------------------------------------------
    //
    // Six queries, and every one of them is bounded in a way a person can see by
    // reading the SQL: a half open wall clock window, a LIMIT 1, or an explicit
    // id list. Nothing on this path selects the whole table.
    //
    // These are the only queries in the app that mention wallClock, and they are
    // not a contradiction of MASTER_BUILD_PROMPT 5.1's "never order by wallClock".
    // That rule is scoped to replay and merge, where a wall clock sort silently
    // produces different state on two devices. wallClock orders the DISPLAY and
    // never the state. Nothing the reducer, the projection, a checkpoint or a
    // merge does may consult it, and lamport remains the only ordering the log
    // itself knows about.

    /**
     * One page of the Trail, newest first. MASTER_BUILD_PROMPT 9.
     *
     * The three tiebreaks are not decoration. `ClarityRepository.commit` reads the
     * clock once per commit and stamps every event in that commit with it, so an
     * `ITEM_COMPLETED` and the `ITEM_PROMOTED` it caused share a wallClock to the
     * millisecond and only lamport puts them in causal order. Without it the
     * promotion can print below the completion it followed. After a merge two
     * devices can agree on both, so originId and id make the sort total and
     * stable, matching the chain in `ClarityEvent.TOTAL_ORDER`.
     */
    @Query(
        """
        SELECT * FROM clarity_event
        WHERE wallClock >= :fromMillis AND wallClock < :toMillis
        ORDER BY wallClock DESC, lamport DESC, originId DESC, id DESC
        """,
    )
    suspend fun betweenWallClock(fromMillis: Long, toMillis: Long): List<ClarityEventRow>

    /**
     * The anchor for the first page. Null on an empty log, which is the empty state.
     *
     * `ORDER BY ... LIMIT 1` rather than `SELECT MAX(wallClock)` deliberately. Both
     * are one seek of the wallClock index, but only this form shows its bound to
     * someone reading it, and the criterion issue #1 states is a reading test.
     */
    @Query("SELECT wallClock FROM clarity_event ORDER BY wallClock DESC LIMIT 1")
    suspend fun newestWallClock(): Long?

    /**
     * The anchor for the next page. This is what stops an empty stretch of history
     * costing one page load per fortnight: a log whose only older event is a year
     * back is reached in one more page, not twenty six.
     */
    @Query(
        """
        SELECT wallClock FROM clarity_event
        WHERE wallClock < :beforeMillis
        ORDER BY wallClock DESC LIMIT 1
        """,
    )
    suspend fun newestWallClockBefore(beforeMillis: Long): Long?

    /**
     * Naming and coloring history for exactly the areas on one page.
     *
     * Ordered by the total order, not by wallClock, because the caller folds these
     * forward to answer what an area was called at the moment of an older event.
     * A fold is state, and state is never ordered by a wall clock.
     */
    @Query(
        """
        SELECT * FROM clarity_event
        WHERE type IN ('AREA_CREATED', 'AREA_RENAMED', 'AREA_RECOLORED')
          AND entityId IN (:areaIds)
        ORDER BY lamport ASC, originId ASC, id ASC
        """,
    )
    suspend fun areaHistory(areaIds: List<String>): List<ClarityEventRow>

    /** Area binding and title history for exactly the items on one page. */
    @Query(
        """
        SELECT * FROM clarity_event
        WHERE type IN ('ITEM_ADDED', 'ITEM_EDITED')
          AND entityId IN (:itemIds)
        ORDER BY lamport ASC, originId ASC, id ASC
        """,
    )
    suspend fun itemHistory(itemIds: List<String>): List<ClarityEventRow>

    /**
     * Area and item binding for exactly the focus sessions on one page.
     *
     * `FOCUS_STARTED` is the only focus payload that carries an areaId and an
     * itemId, and all three use the sessionId as their entityId, so one row per
     * session answers where a completion or an abandonment belongs.
     */
    @Query(
        """
        SELECT * FROM clarity_event
        WHERE type = 'FOCUS_STARTED' AND entityId IN (:sessionIds)
        """,
    )
    suspend fun focusOrigins(sessionIds: List<String>): List<ClarityEventRow>

    /** Only ever called by Erase all data. There is no other delete anywhere. */
    @Query("DELETE FROM clarity_event")
    suspend fun eraseEverything()
}
