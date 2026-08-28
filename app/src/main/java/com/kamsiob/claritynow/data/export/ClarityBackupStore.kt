package com.kamsiob.claritynow.data.export

import com.kamsiob.claritynow.data.event.ClarityEvent
import com.kamsiob.claritynow.data.repo.IngestMode
import com.kamsiob.claritynow.data.repo.RebuildCheck
import com.kamsiob.claritynow.domain.replay.ClarityState

/**
 * Everything the backup path needs from the database, and nothing else.
 * `ClarityRepository` is the only implementation.
 *
 * **It exists so the tests can be honest.** MASTER_BUILD_PROMPT 14b.7 requires a
 * test that feeds deliberately corrupted files and proves the database is
 * untouched, and a test that proves a thing was not written has to be able to watch
 * for the write. Room needs a device, so a unit test cannot; three methods it can
 * implement means the corruption suite runs on a plain JVM and actually asserts
 * what it claims to.
 *
 * It is also the whole surface. Nothing in `data.export` can delete a row, write an
 * event or move a setting except through these three, which is the other half of
 * "validate before touching anything": the validating half does not implement this
 * interface and does not hold one.
 */
interface ClarityBackupStore {

    /**
     * A full rebuild from event zero, and the events it was folded from.
     *
     * MASTER_BUILD_PROMPT 6.4 requires the export path to run this rebuild as a
     * correctness check, so the file is written from a state that was just proved
     * to be what the log produces rather than from the projection the app happened
     * to be holding. [ExportSnapshot.rebuildMatched] carries what the check found.
     */
    suspend fun exportSnapshot(): ExportSnapshot

    /**
     * The one door foreign events come through. Both modes discard every checkpoint
     * and fold from event zero, per MASTER_BUILD_PROMPT 6.4.
     */
    suspend fun ingestForeignLog(incoming: List<ClarityEvent>, mode: IngestMode): RebuildCheck

    /** Records the instant of a successful export, which Settings reads back. */
    suspend fun recordExport(atMillis: Long)
}

/**
 * The whole database at one instant: the log, and the projection folded from it.
 *
 * [rebuildMatched] is the correctness check MASTER_BUILD_PROMPT 6.4 asks the export
 * path to run. False means the state the app has been showing disagrees with the
 * log it was supposedly folded from. **The file is written either way, and from the
 * rebuilt state**, so a false here is a report about the running app rather than a
 * fault in the backup, and it is never a reason to withhold a person's own data
 * from them at the moment they asked to save it.
 */
data class ExportSnapshot(
    val events: List<ClarityEvent>,
    val state: ClarityState,
    val rebuildMatched: Boolean?,
)
