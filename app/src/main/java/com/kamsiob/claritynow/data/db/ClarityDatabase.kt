package com.kamsiob.claritynow.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.kamsiob.claritynow.data.event.ClarityEventDao

/**
 * One database file. The log table is truth, every other table is a cache.
 *
 * There is no `fallbackToDestructiveMigration` here on purpose. A person's history
 * back to install date is the whole point of this app, and a schema mistake must
 * fail loudly during development rather than quietly wipe a year of events.
 */
@Database(
    version = 3,
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

        /**
         * Adds the area's last activity stamp. Existing rows get zero and are
         * corrected the first time the cache is rebuilt from the log, which is
         * lossless because every value here is derived.
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE clarity_area ADD COLUMN lastEventAt INTEGER NOT NULL DEFAULT 0")
            }
        }

        /**
         * An item may now have no area, and carries an optional first step and an
         * optional estimate. Addendum 01 4a, 4b and 4c, and DECISIONS.md C8.
         *
         * **The table is rebuilt rather than altered, because SQLite cannot drop a
         * NOT NULL constraint in place.** There is no ALTER COLUMN: the only way to
         * widen a constraint is to create the table again without it, copy every
         * row across, drop the original and take its name. The two new columns
         * would each have been a one line ALTER TABLE on their own, and they are
         * folded into the recreate because one pass over the table is one place for
         * this to go wrong rather than three.
         *
         * The copy names its columns on both sides rather than relying on
         * `INSERT INTO ... SELECT *`, which binds by position and would put the
         * wrong value in the wrong column the moment either table's order changes.
         * The two new columns are absent from the list and default to null, which
         * is the right answer for both, because schema 2 had nowhere to record
         * either one. Every `areaId` copied is non null, because schema 2 required
         * it, so nothing here has to decide what an older row's inbox state was.
         *
         * The three indices are recreated by hand because DROP TABLE takes a
         * table's indices with it, and Room compares them against the entity on the
         * next open. Their names are Room's own convention, `index_table_column`,
         * and a hand rolled name would fail that comparison rather than the query.
         *
         * There are no foreign keys anywhere in this schema, so the recreate needs
         * no deferral. If one is ever added, a recreate like this one has to turn
         * enforcement off around itself, because both the drop and the rename move
         * a table that other rows point at.
         *
         * Nothing migrates for the FOCUS_ABANDONED rename in the same commit. The
         * outcome is stored on `clarity_focus_session` as an enum name, and focus
         * is phase 4, so no row anywhere holds the old spelling. The cache is
         * rebuildable from the log in any case, which is the safety net under every
         * row in this file that the log table does not have.
         */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `clarity_item_new` (
                        `id` TEXT NOT NULL,
                        `areaId` TEXT,
                        `title` TEXT NOT NULL,
                        `note` TEXT,
                        `firstStep` TEXT,
                        `estimateMinutes` INTEGER,
                        `orderKey` TEXT NOT NULL,
                        `status` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        `activeSince` INTEGER,
                        `completedAt` INTEGER,
                        `deletedAt` INTEGER,
                        `lastEventLamport` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    INSERT INTO `clarity_item_new` (
                        `id`, `areaId`, `title`, `note`, `orderKey`, `status`,
                        `createdAt`, `activeSince`, `completedAt`, `deletedAt`,
                        `lastEventLamport`
                    )
                    SELECT
                        `id`, `areaId`, `title`, `note`, `orderKey`, `status`,
                        `createdAt`, `activeSince`, `completedAt`, `deletedAt`,
                        `lastEventLamport`
                    FROM `clarity_item`
                    """.trimIndent(),
                )
                db.execSQL("DROP TABLE `clarity_item`")
                db.execSQL("ALTER TABLE `clarity_item_new` RENAME TO `clarity_item`")
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_clarity_item_areaId` " +
                        "ON `clarity_item` (`areaId`)",
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_clarity_item_status` " +
                        "ON `clarity_item` (`status`)",
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_clarity_item_orderKey` " +
                        "ON `clarity_item` (`orderKey`)",
                )
            }
        }

        fun build(context: Context): ClarityDatabase =
            Room.databaseBuilder(context, ClarityDatabase::class.java, NAME)
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .build()

        /** For the replay harness and the rebuild proof. Never touches disk. */
        fun inMemory(context: Context): ClarityDatabase =
            Room.inMemoryDatabaseBuilder(context, ClarityDatabase::class.java)
                .allowMainThreadQueries()
                .build()
    }
}
