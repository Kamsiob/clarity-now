package com.kamsiob.claritynow.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.kamsiob.claritynow.data.event.ClarityEventDao

/**
 * One database file. The log table is truth, every other table is a cache.
 *
 * There is no `fallbackToDestructiveMigration` here on purpose. A person's history
 * back to install date is the whole point of this app, and a schema mistake must
 * fail loudly during development rather than quietly wipe a year of events.
 */
@Database(
    version = 1,
    exportSchema = true,
    entities = [
        ClarityEventRow::class,
        AreaRow::class,
        ItemRow::class,
        FocusSessionRow::class,
        PulseEntryRow::class,
        ReportRow::class,
        WeekSnapshotRow::class,
        PlanRow::class,
        ConflictRow::class,
    ],
)
@TypeConverters(Converters::class)
abstract class ClarityDatabase : RoomDatabase() {

    abstract fun events(): ClarityEventDao
    abstract fun cache(): CacheDao

    companion object {
        const val NAME = "clarity.db"

        fun build(context: Context): ClarityDatabase =
            Room.databaseBuilder(context, ClarityDatabase::class.java, NAME).build()

        /** For the replay harness and the rebuild proof. Never touches disk. */
        fun inMemory(context: Context): ClarityDatabase =
            Room.inMemoryDatabaseBuilder(context, ClarityDatabase::class.java)
                .allowMainThreadQueries()
                .build()
    }
}
