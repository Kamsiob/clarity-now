package com.kamsiob.claritynow.data.export

import com.kamsiob.claritynow.data.event.ItemAdded
import com.kamsiob.claritynow.data.repo.IngestMode
import com.kamsiob.claritynow.domain.replay.ClarityCheckpointCodec
import com.kamsiob.claritynow.domain.replay.ClarityReplay
import kotlinx.coroutines.test.runTest
import java.io.IOException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The round trip, encrypted and unencrypted, asserting byte identical state.
 * MASTER_BUILD_PROMPT 14b.7 names this test, and Addendum 01 4h names it twice.
 *
 * **Byte identical means byte identical.** The assertion is over the canonical
 * serialization of the whole projection, which is the same encoding a checkpoint is
 * written with, so a field that survived the round trip with a different value fails
 * here rather than being invisible behind a `==` on a data class somebody later
 * widened. The events themselves are compared as a list, in order, so an event that
 * arrived in a different position fails too.
 *
 * The encrypted case runs at the parameters that ship rather than at reduced ones.
 * It costs a few seconds and it is the only way this test is testing what a person
 * will actually be handed.
 */
class BackupRoundTripTest {

    private val events = BackupFixture.events()
    private val snapshot = BackupFixture.snapshot(events)
    private val store = RecordingBackupStore(snapshot)
    private val target = MemoryTarget()
    private val service = BackupService(
        store = store,
        clock = BackupFixture.clock(),
        appVersion = "0.9.0",
        random = BackupFixture.fixedRandom(),
    )

    @Test
    fun `a file with no password round trips to byte identical state`() = runTest {
        val outcome = service.export(target, password = null)
        assertFalse(outcome.encrypted)

        val read = service.read(target, password = null)
        val opened = read as? BackupRead.Opened
            ?: error("refused: ${(read as BackupRead.Refused).diagnostic}")

        assertEquals(events, opened.events)
        assertEquals(
            ClarityCheckpointCodec.encodeState(snapshot.state),
            ClarityCheckpointCodec.encodeState(opened.foldedState),
        )
        assertTrue("the file records its own projection correctly", opened.foldMatchesRecordedState)
    }

    @Test
    fun `a file with a password round trips to byte identical state`() = runTest {
        val outcome = service.export(target, password = PASSWORD.toCharArray())
        assertTrue(outcome.encrypted)

        val read = service.read(target, password = PASSWORD.toCharArray())
        val opened = read as? BackupRead.Opened
            ?: error("refused: ${(read as BackupRead.Refused).diagnostic}")

        assertEquals(events, opened.events)
        assertEquals(
            ClarityCheckpointCodec.encodeState(snapshot.state),
            ClarityCheckpointCodec.encodeState(opened.foldedState),
        )
    }

    /**
     * The claim the export screen has to make about a file with no password, checked
     * rather than asserted in prose. 14b.7: the screen says plainly that the file is
     * readable rather than implying a safety it does not provide.
     */
    @Test
    fun `a file with no password is readable and a file with one is not`() = runTest {
        val title = events.map { it.payload }.filterIsInstance<ItemAdded>().first().title

        service.export(target, password = null)
        val readable = String(target.document, Charsets.UTF_8)
        assertTrue("an unencrypted export has to be readable", readable.contains(title))

        service.export(target, password = PASSWORD.toCharArray())
        val sealed = String(target.document, Charsets.UTF_8)
        assertFalse("an encrypted export may not leak a title", sealed.contains(title))
        assertTrue("the header stays readable so the file can be identified", sealed.contains("createdOn"))
    }

    /**
     * The header carries what 14b.7 asks it to carry, and the counts it states are
     * the counts in the file.
     */
    @Test
    fun `the header states the schema version the date the counts and a checksum`() = runTest {
        service.export(target, password = null)
        val header = BackupFormat.decodeHeader(BackupFormat.split(target.document)!!.first)

        assertEquals(BackupFormat.FORMAT, header.format)
        assertEquals(BackupFormat.FORMAT_VERSION, header.formatVersion)
        assertEquals(1, header.eventSchemaVersion)
        assertEquals(BackupFixture.NOW_MILLIS, header.createdAt)
        assertEquals("2027-01-15", header.createdOn)
        assertEquals(events.size, header.eventCount)
        assertEquals(
            snapshot.state.items.values.count { it.deletedAt == null },
            header.itemCount,
        )
        assertTrue(header.checksum.startsWith(BackupFormat.CHECKSUM_PREFIX))
        assertEquals(BackupCrypto.ENCRYPTION_NONE, header.encryption)
    }

    /** The rebuild from event zero is the correctness check 6.4 asks of this path. */
    @Test
    fun `the export rebuilds from event zero before it writes`() = runTest {
        service.export(target, password = null)
        assertEquals(1, store.snapshotCount)
    }

    /**
     * The date is recorded after the bytes are handed over, so a failed write does
     * not leave somebody believing they have a backup they do not have.
     */
    @Test
    fun `a write that fails records no export date`() = runTest {
        val failing = object : SyncTarget {
            override suspend fun send(document: ByteArray): Unit = throw IOException("no room")
            override suspend fun receive(): ByteArray = ByteArray(0)
        }
        runCatching { service.export(failing, password = null) }
        assertTrue(store.exportsRecorded.isEmpty())

        service.export(target, password = null)
        assertEquals(listOf(BackupFixture.NOW_MILLIS), store.exportsRecorded)
    }

    /** Both modes reach the one door foreign events come through, unchanged. */
    @Test
    fun `apply hands every event to the store in the chosen mode`() = runTest {
        service.export(target, password = null)
        val opened = service.read(target, password = null) as BackupRead.Opened

        val check = service.apply(opened, IngestMode.MERGE)
        assertEquals(1, store.ingestCount)
        assertEquals(IngestMode.MERGE, store.lastMode)
        assertEquals(events, store.lastIngested)
        assertEquals(ClarityReplay.replay(events).eventsApplied, check.eventCount)
    }

    private companion object {
        const val PASSWORD = "a long enough password"
    }
}
