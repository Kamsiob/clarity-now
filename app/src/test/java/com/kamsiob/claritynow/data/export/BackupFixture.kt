package com.kamsiob.claritynow.data.export

import com.kamsiob.claritynow.data.event.ClarityEvent
import com.kamsiob.claritynow.data.repo.IngestMode
import com.kamsiob.claritynow.data.repo.RebuildCheck
import com.kamsiob.claritynow.domain.FixedClarityClock
import com.kamsiob.claritynow.domain.replay.ClarityReplay
import com.kamsiob.claritynow.domain.replay.ClarityState
import com.kamsiob.claritynow.domain.replay.EventStreamGenerator
import java.security.SecureRandom
import java.time.ZoneId

/**
 * What the backup tests share: a real event stream, a store that records every
 * write it is asked to make, and a target that keeps a document in memory.
 *
 * The stream comes from the replay harness's own generator rather than from three
 * events written by hand, because a round trip over three events proves the round
 * trip works for three events. What it has to prove is that it works for a log with
 * areas, an inbox, focus sessions, pulses, reports, plans, conflicts and tombstones
 * in it, and that is what the generator emits.
 */
object BackupFixture {

    const val ORIGIN = "test-origin"

    val zone: ZoneId = ZoneId.of("UTC")

    /** A fixed instant, so a header written twice is written the same. */
    const val NOW_MILLIS = 1_800_000_000_000L

    fun clock() = FixedClarityClock(NOW_MILLIS, zone)

    fun events(count: Int = 400, seed: Long = 7L): List<ClarityEvent> =
        EventStreamGenerator(seed = seed, originId = ORIGIN).generate(count)

    fun snapshot(events: List<ClarityEvent> = events()): ExportSnapshot = ExportSnapshot(
        events = events,
        state = ClarityReplay.replay(events),
        rebuildMatched = true,
    )

    /**
     * A `SecureRandom` with a fixed seed, so a test that runs twice writes the same
     * salt and nonce. Never used for anything that ships: `BackupService` takes the
     * platform's own by default.
     */
    fun fixedRandom(): SecureRandom = SecureRandom.getInstance("SHA1PRNG").apply {
        setSeed(byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8))
    }
}

/**
 * A store that answers with a fixed snapshot and counts every write it is asked
 * for. [ingestCount] staying at zero is the assertion the corruption suite is
 * really making: MASTER_BUILD_PROMPT 14b.7 requires that a refused file leaves the
 * database untouched, and a test of that has to be able to watch for the touch.
 */
class RecordingBackupStore(private val snapshot: ExportSnapshot) : ClarityBackupStore {

    var snapshotCount: Int = 0
        private set
    var ingestCount: Int = 0
        private set
    var lastMode: IngestMode? = null
        private set
    var lastIngested: List<ClarityEvent> = emptyList()
        private set
    val exportsRecorded: MutableList<Long> = mutableListOf()

    override suspend fun exportSnapshot(): ExportSnapshot {
        snapshotCount++
        return snapshot
    }

    override suspend fun ingestForeignLog(
        incoming: List<ClarityEvent>,
        mode: IngestMode,
    ): RebuildCheck {
        ingestCount++
        lastMode = mode
        lastIngested = incoming
        val state: ClarityState = ClarityReplay.replay(incoming)
        return RebuildCheck(state = state, eventCount = state.eventsApplied, matched = null)
    }

    override suspend fun recordExport(atMillis: Long) {
        exportsRecorded += atMillis
    }
}

/** One document, held in memory. The Storage Access Framework does not run here. */
class MemoryTarget(var document: ByteArray = ByteArray(0)) : SyncTarget {
    override suspend fun send(document: ByteArray) {
        this.document = document
    }

    override suspend fun receive(): ByteArray = document
}
