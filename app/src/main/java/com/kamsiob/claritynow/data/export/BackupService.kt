package com.kamsiob.claritynow.data.export

import com.kamsiob.claritynow.data.repo.IngestMode
import com.kamsiob.claritynow.data.repo.RebuildCheck
import com.kamsiob.claritynow.domain.ClarityClock
import com.kamsiob.claritynow.domain.dateKey
import java.io.IOException
import java.security.SecureRandom

/**
 * Export, import and the order the two halves of an import happen in.
 * MASTER_BUILD_PROMPT 14.1 and 14b.7, Addendum 01 4h.
 *
 * **Import is two calls and not one, and that is the requirement rather than a
 * convenience.** 14b.7 says a file is validated before anything is touched.
 * [read] can only read: it holds no store, it returns a value, and every refusal it
 * can produce happens with the database exactly as it was. [apply] takes an
 * [BackupRead.Opened], which is a type nothing but [BackupCodec.read] can produce,
 * so there is no path into the database that skipped the checks. It also gives the
 * screen what it needs in between, which is a file's date and counts to show before
 * asking somebody to choose replace or merge.
 *
 * **Nothing here composes a sentence.** A refusal travels as an [ImportRefusal] and
 * the screen renders the fixed string for it. CLAUDE.md rule 8 and the note in
 * [ImportRefusal] about why these particular sentences are not corpus lines.
 */
class BackupService(
    private val store: ClarityBackupStore,
    private val clock: ClarityClock,
    private val appVersion: String,
    private val random: SecureRandom = SecureRandom(),
) {

    /**
     * Writes the whole database to [target]. [password] null writes a readable
     * file, which the screen has to say plainly rather than implying a safety that
     * is not there.
     *
     * The order is deliberate. The rebuild runs first, so the file is written from
     * a state just proved against the log. The date is recorded last, after the
     * bytes have been handed over, so a write that failed does not leave a person
     * believing they have a backup they do not have.
     */
    suspend fun export(target: SyncTarget, password: CharArray?): ExportOutcome {
        val snapshot = store.exportSnapshot()
        val at = clock.nowMillis()
        val document = BackupCodec.write(
            snapshot = snapshot,
            password = password,
            appVersion = appVersion,
            createdAt = at,
            createdOn = clock.dateKey(at),
            random = random,
        )
        target.send(document)
        store.recordExport(at)
        return ExportOutcome(
            exportedAt = at,
            encrypted = password != null,
            eventCount = snapshot.events.size,
            itemCount = snapshot.state.items.values.count { it.deletedAt == null },
            areaCount = snapshot.state.areas.values.count { it.deletedAt == null },
            documentBytes = document.size,
            rebuildMatched = snapshot.rebuildMatched,
        )
    }

    /**
     * Reads and validates. Writes nothing, whatever the file turns out to be.
     *
     * A file that could not be opened at all and a file too large to be one of ours
     * are both answered here rather than thrown, because the screen has one place
     * to put a refusal and a person picking the wrong file in a system picker is
     * the ordinary case rather than an error.
     */
    suspend fun read(target: SyncTarget, password: CharArray?): BackupRead {
        val document = try {
            target.receive()
        } catch (failure: IOException) {
            return BackupRead.Refused(
                ImportRefusal.DAMAGED,
                "the file could not be read: ${failure.message}",
            )
        }
        if (document.size > BackupFormat.MAX_DOCUMENT_BYTES) {
            return BackupRead.Refused(
                ImportRefusal.NOT_A_BACKUP,
                "larger than ${BackupFormat.MAX_DOCUMENT_BYTES} bytes",
            )
        }
        return BackupCodec.read(document, password)
    }

    /**
     * Takes an opened file into the log. One transaction for a replace, a
     * deterministic union for a merge, and a fold from event zero for both.
     */
    suspend fun apply(opened: BackupRead.Opened, mode: IngestMode): RebuildCheck =
        store.ingestForeignLog(opened.events, mode)

    /** The suggested name for today's file. The person can change it in the picker. */
    fun suggestedFileName(): String = BackupFormat.suggestedFileName(clock.dateKey())
}

/**
 * What an export did. [rebuildMatched] false means the rebuild from event zero
 * disagreed with the state the app was running on, per
 * [ExportSnapshot.rebuildMatched]. The file is correct either way.
 */
data class ExportOutcome(
    val exportedAt: Long,
    val encrypted: Boolean,
    val eventCount: Int,
    val itemCount: Int,
    val areaCount: Int,
    val documentBytes: Int,
    val rebuildMatched: Boolean?,
)
