package com.kamsiob.claritynow.data.export

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream

/**
 * Where a backup document goes, and where one comes from.
 *
 * MASTER_BUILD_PROMPT 7 asks for exactly this: an interface declared in v1 with one
 * implementation, so that sync later means writing a second one rather than
 * reshaping the app around it. Nothing in v1 opens a socket, requests a permission
 * or shows a sync setting, and this interface does not change that. It is the shape
 * of the seam, present so that the seam exists.
 *
 * A document is one whole file. There is no streaming and no partial write on
 * purpose: a half written backup is worse than no backup, because it looks like one.
 */
interface SyncTarget {

    /** Writes one whole document, replacing anything already there. */
    suspend fun send(document: ByteArray)

    /**
     * Reads one whole document, and at most [BackupFormat.MAX_DOCUMENT_BYTES] plus
     * one byte of it. The extra byte is what lets the caller tell a file at the
     * ceiling from a file past it without having read the whole of a file past it.
     */
    suspend fun receive(): ByteArray
}

/**
 * The only implementation, and the only one v1 has. The Storage Access Framework
 * hands back a stream for a location the person chose, and this wraps it.
 *
 * The streams arrive as lambdas rather than as open streams, so that a target can
 * be built before a person has picked anywhere and so that nothing is left open
 * when an export is abandoned. Both run on the IO dispatcher, because a document is
 * megabytes and the caller is a button.
 */
class LocalFileTarget(
    private val openSink: () -> OutputStream,
    private val openSource: () -> InputStream,
) : SyncTarget {

    override suspend fun send(document: ByteArray) = withContext(Dispatchers.IO) {
        openSink().use { it.write(document) }
    }

    /**
     * **Bounded, and that is not a nicety.** The file is whatever the person picked
     * in a system picker, which can be a video. `readBytes` on an arbitrary stream
     * is an allocation somebody else chose the size of, and the failure is the
     * process dying rather than a sentence on a screen.
     */
    override suspend fun receive(): ByteArray = withContext(Dispatchers.IO) {
        openSource().use { source ->
            val ceiling = BackupFormat.MAX_DOCUMENT_BYTES + 1
            val buffer = ByteArray(READ_CHUNK_BYTES)
            val out = java.io.ByteArrayOutputStream()
            while (out.size() < ceiling) {
                val wanted = minOf(buffer.size, ceiling - out.size())
                val read = source.read(buffer, 0, wanted)
                if (read < 0) break
                out.write(buffer, 0, read)
            }
            out.toByteArray()
        }
    }

    private companion object {
        const val READ_CHUNK_BYTES = 64 * 1024
    }
}
