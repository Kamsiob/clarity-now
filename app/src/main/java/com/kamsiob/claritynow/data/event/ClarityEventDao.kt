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

    @Query(
        """
        SELECT * FROM clarity_event
        WHERE wallClock >= :fromMillis AND wallClock < :toMillis
        ORDER BY wallClock DESC, lamport DESC
        """,
    )
    suspend fun betweenWallClock(fromMillis: Long, toMillis: Long): List<ClarityEventRow>

    /** Only ever called by Erase all data. There is no other delete anywhere. */
    @Query("DELETE FROM clarity_event")
    suspend fun eraseEverything()
}
