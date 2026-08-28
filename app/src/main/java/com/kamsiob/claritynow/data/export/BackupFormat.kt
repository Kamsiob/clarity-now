package com.kamsiob.claritynow.data.export

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.security.MessageDigest

/**
 * The first line of every backup file. MASTER_BUILD_PROMPT 14b.7 requires a schema
 * version, a creation date, an item count, an event count and a checksum, and this
 * is all five plus what a person opening the file on another machine would need.
 *
 * [eventCount] is a property of the log and is checked against the file on import.
 * [itemCount] and [areaCount] are properties of the projection, so they are
 * informational: the log is the truth and the projection is a cache, and a build
 * whose reducer has improved would count differently from the build that wrote the
 * file. Refusing somebody their own history over a derived number would be the
 * wrong way around. [BackupCodec] says the same thing where it decides.
 *
 * [checksum] covers this header and the body together, which is why the header can
 * carry it: it is computed with the field blanked and checked the same way, so
 * every claim above is covered by it and none of it can be edited quietly.
 */
@Serializable
data class BackupHeader(
    val format: String = BackupFormat.FORMAT,
    val formatVersion: Int = BackupFormat.FORMAT_VERSION,
    val app: String = BackupFormat.APP,
    /** The app version that wrote it. Informational, and never a gate. */
    val appVersion: String,
    /** `ClarityEvent.SCHEMA_VERSION` at the moment of writing. */
    val eventSchemaVersion: Int,
    val createdAt: Long,
    /** The local calendar day of [createdAt], so a person can read the date. */
    val createdOn: String,
    val eventCount: Int,
    val itemCount: Int,
    val areaCount: Int,
    val encryption: String,
    val checksum: String,
    val kdf: BackupCrypto.KdfSpec? = null,
    val cipher: BackupCrypto.CipherSpec? = null,
) {
    val isEncrypted: Boolean get() = encryption != BackupCrypto.ENCRYPTION_NONE
}

/**
 * The shape of the file itself: one header line, a newline, then the body.
 *
 * **Two parts rather than one JSON object, and the reason is the checksum.** The
 * obvious shape is a single document with the body nested inside it, and it has a
 * problem that does not show up until somebody tries to verify one: a checksum over
 * a nested object has to be a checksum over some canonical serialization of it, and
 * two writers that disagree about key order or number formatting produce two
 * checksums for the same data. Splitting the file means the checksum is over exact
 * bytes, which is a thing a second implementation can reproduce with no rules to
 * agree about beyond "hash these bytes".
 *
 * It costs nothing a person would notice. An unencrypted body is pretty printed
 * JSON starting on line 2, so the file still opens in a text editor and reads as
 * what it is, which is the point of 14b.7's requirement that a file with no
 * password says plainly that it is readable.
 *
 * The body is UTF-8 JSON when there is no password, and base64 of the sealed bytes
 * when there is one.
 */
object BackupFormat {

    const val FORMAT: String = "clarity-now-backup"

    /**
     * The version of this envelope, which is not the event schema version.
     *
     * They move for different reasons: this one moves when the file's shape changes,
     * the other when the log's shape does, and a backup carries both because a
     * reader has to be able to refuse each for its own reason.
     */
    const val FORMAT_VERSION: Int = 1

    const val APP: String = "Clarity Now"

    const val CHECKSUM_PREFIX: String = "sha256:"

    /** The suggested file name. The Storage Access Framework lets a person change it. */
    fun suggestedFileName(dateKey: String): String = "clarity-now-$dateKey.json"

    const val MIME_TYPE: String = "application/json"

    /**
     * The most that will be read into memory from a file somebody picked.
     *
     * A decade of one person's queue is a few megabytes. The picker will hand back
     * whatever they tapped, including a video, and an unbounded read of that is the
     * process dying instead of a sentence on a screen. Anything past this is
     * refused as not one of ours, which is what it is.
     */
    const val MAX_DOCUMENT_BYTES: Int = 64 * 1024 * 1024

    private val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    /** One line, no newlines inside it, which is what makes the split unambiguous. */
    fun encodeHeader(header: BackupHeader): String =
        json.encodeToString(BackupHeader.serializer(), header)

    fun decodeHeader(line: String): BackupHeader =
        json.decodeFromString(BackupHeader.serializer(), line)

    /**
     * The body as it is written, hashed and read: no carriage returns, and no
     * trailing blank space.
     *
     * **A checksum that fails on files that are fine is a checksum people learn to
     * work around**, and there are exactly two things that happen to a text file in
     * transit without anybody meaning to change it: an editor rewrites the line
     * endings, and a tool appends a newline at the end. Neither changes a single
     * thing this file means. So both are normalized away before the hash is taken,
     * on both sides, which is one rule a second implementation can follow rather
     * than a retry with different rules on the way in.
     *
     * It is lossless for what this file holds. The body is either JSON, where
     * kotlinx escapes a carriage return inside a string as two characters and never
     * emits a raw one, or base64, which has no carriage returns in it at all. So
     * dropping the byte cannot alter a title somebody typed.
     *
     * Everything else still fails. A flipped bit anywhere that matters, a
     * truncation, an edited count in the header: all of them change the digest.
     */
    fun normalizeBody(body: ByteArray): ByteArray {
        val kept = ByteArray(body.size)
        var length = 0
        for (byte in body) {
            if (byte != CARRIAGE_RETURN) kept[length++] = byte
        }
        while (length > 0 && kept[length - 1].isTrailingSpace()) length--
        return kept.copyOf(length)
    }

    /**
     * The checksum, over the header with its own checksum field blanked, then a
     * newline, then the normalized body.
     *
     * Blanking rather than omitting, so the two sides hash a string of the same
     * shape and a reader does not have to reconstruct a field order. Hashing the
     * header's own re-encoding rather than the line as it arrived is what makes the
     * checksum cover every claim the header makes, with no way for it to cover
     * itself.
     */
    fun checksumOf(header: BackupHeader, body: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(encodeHeader(header.copy(checksum = "")).toByteArray(Charsets.UTF_8))
        digest.update('\n'.code.toByte())
        digest.update(normalizeBody(body))
        return CHECKSUM_PREFIX + digest.digest().toHex()
    }

    fun assemble(header: BackupHeader, body: ByteArray): ByteArray {
        val normalized = normalizeBody(body)
        val stamped = header.copy(checksum = checksumOf(header, normalized))
        val line = encodeHeader(stamped).toByteArray(Charsets.UTF_8)
        return ByteArray(line.size + 1 + normalized.size).also { out ->
            line.copyInto(out)
            out[line.size] = '\n'.code.toByte()
            normalized.copyInto(out, line.size + 1)
        }
    }

    /**
     * The header line and the normalized body, or null when there is no newline to
     * split on. Everything downstream reads the body this hands back, so the bytes
     * that were hashed and the bytes that are parsed are the same bytes.
     */
    fun split(document: ByteArray): Pair<String, ByteArray>? {
        val newline = document.indexOf('\n'.code.toByte())
        if (newline < 0) return null
        val line = String(document, 0, newline, Charsets.UTF_8)
        return line to normalizeBody(document.copyOfRange(newline + 1, document.size))
    }

    private fun Byte.isTrailingSpace(): Boolean =
        this == '\n'.code.toByte() || this == ' '.code.toByte() || this == '\t'.code.toByte()

    private const val CARRIAGE_RETURN: Byte = 13

    /**
     * Written out rather than reached through `String.format`, which formats
     * against the default locale. A checksum is bytes and has to read the same in
     * every locale the app runs in.
     */
    private fun ByteArray.toHex(): String {
        val out = StringBuilder(size * 2)
        for (byte in this) {
            val value = byte.toInt() and 0xFF
            out.append(HEX[value ushr 4]).append(HEX[value and 0x0F])
        }
        return out.toString()
    }

    private const val HEX = "0123456789abcdef"
}
