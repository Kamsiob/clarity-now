package com.kamsiob.claritynow.data.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

/**
 * The materialized cache. Every row here is derived from the log, so every write is
 * an upsert and every delete is a wholesale clear before a rebuild.
 *
 * Nothing outside `data.repo` calls this.
 */
@Dao
interface CacheDao {

    @Upsert suspend fun upsertAreas(rows: List<AreaRow>)
    @Upsert suspend fun upsertItems(rows: List<ItemRow>)
    @Upsert suspend fun upsertFocusSessions(rows: List<FocusSessionRow>)
    @Upsert suspend fun upsertPulses(rows: List<PulseEntryRow>)
    @Upsert suspend fun upsertReports(rows: List<ReportRow>)
    @Upsert suspend fun upsertPlans(rows: List<PlanRow>)
    @Upsert suspend fun upsertConflicts(rows: List<ConflictRow>)
    @Upsert suspend fun upsertSnapshot(row: WeekSnapshotRow)

    @Query("SELECT * FROM clarity_area WHERE deletedAt IS NULL ORDER BY orderKey ASC, id ASC")
    fun observeAreas(): Flow<List<AreaRow>>

    @Query("SELECT * FROM clarity_area ORDER BY orderKey ASC, id ASC")
    suspend fun allAreas(): List<AreaRow>

    @Query("SELECT * FROM clarity_item WHERE deletedAt IS NULL ORDER BY orderKey ASC, id ASC")
    fun observeItems(): Flow<List<ItemRow>>

    @Query("SELECT * FROM clarity_item ORDER BY orderKey ASC, id ASC")
    suspend fun allItems(): List<ItemRow>

    @Query("SELECT * FROM clarity_focus_session ORDER BY startedAt DESC")
    suspend fun allFocusSessions(): List<FocusSessionRow>

    @Query("SELECT * FROM clarity_focus_session WHERE outcome = 'RUNNING' ORDER BY startedAt DESC LIMIT 1")
    suspend fun runningFocusSession(): FocusSessionRow?

    @Query("SELECT * FROM clarity_pulse_entry ORDER BY dateKey DESC")
    suspend fun allPulses(): List<PulseEntryRow>

    @Query("SELECT * FROM clarity_pulse_entry WHERE dateKey = :dateKey LIMIT 1")
    suspend fun pulseFor(dateKey: String): PulseEntryRow?

    @Query("SELECT * FROM clarity_report ORDER BY weekStartKey DESC")
    suspend fun allReports(): List<ReportRow>

    @Query("SELECT * FROM clarity_report WHERE weekStartKey = :weekStartKey LIMIT 1")
    suspend fun reportFor(weekStartKey: String): ReportRow?

    @Query("SELECT * FROM clarity_plan ORDER BY offeredAt DESC")
    suspend fun allPlans(): List<PlanRow>

    @Query("SELECT * FROM clarity_conflict WHERE dismissedAt IS NULL ORDER BY detectedAtLamport DESC")
    fun observeOpenConflicts(): Flow<List<ConflictRow>>

    @Query("UPDATE clarity_conflict SET dismissedAt = :atMillis WHERE id = :id")
    suspend fun dismissConflict(id: String, atMillis: Long)

    @Query("SELECT * FROM clarity_week_snapshot ORDER BY lamport DESC LIMIT 1")
    suspend fun newestSnapshot(): WeekSnapshotRow?

    @Query("DELETE FROM clarity_area") suspend fun clearAreas()
    @Query("DELETE FROM clarity_item") suspend fun clearItems()
    @Query("DELETE FROM clarity_focus_session") suspend fun clearFocusSessions()
    @Query("DELETE FROM clarity_pulse_entry") suspend fun clearPulses()
    @Query("DELETE FROM clarity_report") suspend fun clearReports()
    @Query("DELETE FROM clarity_plan") suspend fun clearPlans()
    @Query("DELETE FROM clarity_conflict") suspend fun clearConflicts()
    @Query("DELETE FROM clarity_week_snapshot") suspend fun clearSnapshots()

    /**
     * Drops every derived row. Used by the rebuild proof in the debug menu and by
     * Erase all data. The log is untouched by this; erasing the log is separate and
     * deliberate.
     */
    @Transaction
    suspend fun clearCache() {
        clearItems()
        clearAreas()
        clearFocusSessions()
        clearPulses()
        clearReports()
        clearPlans()
        clearConflicts()
    }
}
