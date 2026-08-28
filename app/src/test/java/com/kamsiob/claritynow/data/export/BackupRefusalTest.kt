package com.kamsiob.claritynow.data.export

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Deliberately corrupted files, and the clean refusal each one has to get.
 * MASTER_BUILD_PROMPT 14b.7 names this test and lists its minimum contents: a
 * truncation, a flipped bit inside the payload, a wrong checksum, an unknown schema
 * version, a foreign JSON document and a wrong password.
 *
 * **Every case asserts twice.** Once that the refusal is the right one, because a
 * file refused for the wrong reason tells a person the wrong thing to do about it.
 * Once that [RecordingBackupStore.ingestCount] is still zero, because 14b.7's
 * requirement is not that a bad file is refused but that the database is untouched
 * when it is, and those are different claims. The second one is why the store is an
 * interface at all.
 */
class BackupRefusalTest {

    private val events = BackupFixture.events(count = 120, seed = 11L)
    private val snapshot = BackupFixture.snapshot(events)
    private val store = RecordingBackupStore(snapshot)
    private val service = BackupService(
        store = store,
        clock = BackupFixture.clock(),
        appVersion = "0.9.0",
        random = BackupFixture.fixedRandom(),
    )

    private val valid: ByteArray = runBlocking {
        val target = MemoryTarget()
        service.export(target, password = null)
        target.document
    }

    // The file itself ---------------------------------------------------------

    @Test
    fun `a file that is not text with a header line is not a backup`() = runTest {
        expect(ImportRefusal.NOT_A_BACKUP, ByteArray(64) { it.toByte() })
    }

    @Test
    fun `an empty file is not a backup`() = runTest {
        expect(ImportRefusal.NOT_A_BACKUP, ByteArray(0))
    }

    @Test
    fun `a foreign JSON document is not a backup`() = runTest {
        val foreign = """{"format":"some-other-app","version":3}""" + "\n" + """{"notes":[]}"""
        expect(ImportRefusal.NOT_A_BACKUP, foreign.toByteArray(Charsets.UTF_8))
    }

    @Test
    fun `a truncated file is damaged`() = runTest {
        expect(ImportRefusal.DAMAGED, valid.copyOfRange(0, valid.size / 2))
    }

    @Test
    fun `a flipped bit inside the body is damaged`() = runTest {
        val flipped = valid.copyOf()
        val at = valid.size - 200
        flipped[at] = (flipped[at].toInt() xor 0x01).toByte()
        expect(ImportRefusal.DAMAGED, flipped)
    }

    @Test
    fun `an edited checksum is damaged`() = runTest {
        val (line, body) = BackupFormat.split(valid)!!
        val header = BackupFormat.decodeHeader(line)
        val wrong = header.copy(checksum = BackupFormat.CHECKSUM_PREFIX + "00".repeat(32))
        val document = (BackupFormat.encodeHeader(wrong) + "\n").toByteArray(Charsets.UTF_8) + body
        expect(ImportRefusal.DAMAGED, document)
    }

    @Test
    fun `an edited count in the header is damaged because the checksum covers it`() = runTest {
        val (line, body) = BackupFormat.split(valid)!!
        val header = BackupFormat.decodeHeader(line)
        val lying = header.copy(itemCount = header.itemCount + 40)
        val document = (BackupFormat.encodeHeader(lying) + "\n").toByteArray(Charsets.UTF_8) + body
        expect(ImportRefusal.DAMAGED, document)
    }

    // Versions ----------------------------------------------------------------

    @Test
    fun `a later envelope version is refused as newer`() = runTest {
        val refused = expect(
            ImportRefusal.NEWER_VERSION,
            rebuild(headerMutate = { it.copy(formatVersion = it.formatVersion + 1) }),
        )
        assertTrue(refused.diagnostic.contains("format version"))
    }

    @Test
    fun `a later event schema version is refused as newer`() = runTest {
        val refused = expect(
            ImportRefusal.NEWER_VERSION,
            rebuild(headerMutate = { it.copy(eventSchemaVersion = it.eventSchemaVersion + 1) }),
        )
        assertTrue(refused.diagnostic.contains("event schema version"))
    }

    /**
     * A record of a kind this build has never heard of. Importing the rest and
     * dropping this one is the failure MASTER_BUILD_PROMPT 6.4 describes, so the
     * whole file is refused instead.
     */
    @Test
    fun `a record of an unknown kind is refused as newer`() = runTest {
        val document = rebuild(
            mutate = { body ->
                replaceFirstEvent(body) { first ->
                    JsonObject(first + ("type" to JsonPrimitive("ITEM_TELEPORTED")))
                }
            },
        )
        val refused = expect(ImportRefusal.NEWER_VERSION, document)
        assertTrue(refused.diagnostic.contains("ITEM_TELEPORTED"))
    }

    // Internal integrity ------------------------------------------------------

    @Test
    fun `a record count that disagrees with the file is inconsistent`() = runTest {
        val refused = expect(
            ImportRefusal.INCONSISTENT,
            rebuild(headerMutate = { it.copy(eventCount = it.eventCount + 5) }),
        )
        assertTrue(refused.diagnostic.contains("the header says"))
    }

    @Test
    fun `the same record twice is inconsistent`() = runTest {
        val document = rebuild(
            mutate = { body ->
                val array = body.getValue("events") as JsonArray
                withEvents(body, JsonArray(array + array.first()))
            },
            headerMutate = { it.copy(eventCount = it.eventCount + 1) },
        )
        val refused = expect(ImportRefusal.INCONSISTENT, document)
        assertTrue(refused.diagnostic.contains("appears twice"))
    }

    @Test
    fun `a record with no identity is inconsistent`() = runTest {
        val document = rebuild(
            mutate = { body ->
                replaceFirstEvent(body) { first -> JsonObject(first + ("id" to JsonPrimitive(""))) }
            },
        )
        val refused = expect(ImportRefusal.INCONSISTENT, document)
        assertTrue(refused.diagnostic.contains("no identity"))
    }

    // Passwords ---------------------------------------------------------------

    @Test
    fun `an encrypted file with no password asks for one`() = runTest {
        expect(ImportRefusal.PASSWORD_REQUIRED, encrypted, password = null)
    }

    @Test
    fun `a wrong password is refused and destroys nothing`() = runTest {
        expect(ImportRefusal.WRONG_PASSWORD, encrypted, password = "not the password".toCharArray())
    }

    // What a file is allowed to survive --------------------------------------

    /**
     * The two things that happen to a text file without anybody meaning to change
     * it. Neither alters what the file says, so neither may cost a person their
     * restore. [BackupFormat.normalizeBody] states the rule.
     */
    @Test
    fun `rewritten line endings and an appended newline still open`() = runTest {
        val rewritten = String(valid, Charsets.UTF_8).replace("\n", "\r\n") + "\n"
        val read = service.read(MemoryTarget(rewritten.toByteArray(Charsets.UTF_8)), password = null)
        val opened = read as? BackupRead.Opened
            ?: error("refused: ${(read as BackupRead.Refused).diagnostic}")
        assertEquals(events.size, opened.events.size)
    }

    // The one disagreement that is not a refusal ------------------------------

    /**
     * A file whose recorded projection does not match this build's fold of its own
     * events is opened, not refused, and says so. The log is the truth and the
     * projection is a cache, so the only thing a disagreement can mean is that the
     * two builds fold differently, and refusing somebody their history over that
     * would be the wrong way around. [BackupCodec] states the same rule.
     */
    @Test
    fun `a recorded projection that disagrees with the fold is opened anyway`() = runTest {
        val document = rebuild(mutate = { body -> withState(body, JsonObject(emptyMap())) })
        val opened = service.read(MemoryTarget(document), password = null) as BackupRead.Opened
        assertTrue("the events still came through", opened.events.isNotEmpty())
        assertTrue("and the disagreement is reported", !opened.foldMatchesRecordedState)
        assertEquals(0, store.ingestCount)
    }

    // Helpers -----------------------------------------------------------------

    private suspend fun expect(
        reason: ImportRefusal,
        document: ByteArray,
        password: CharArray? = null,
    ): BackupRead.Refused {
        val read = service.read(MemoryTarget(document), password)
        val refused = read as? BackupRead.Refused ?: error("expected $reason, the file opened")
        assertEquals(reason, refused.reason)
        assertEquals("a refused file may not reach the database", 0, store.ingestCount)
        return refused
    }

    /** Re-assembles a valid document with a changed header, body or both. */
    private fun rebuild(
        mutate: (JsonObject) -> JsonObject = { it },
        headerMutate: (BackupHeader) -> BackupHeader = { it },
    ): ByteArray {
        val (line, body) = BackupFormat.split(valid)!!
        val parsed = Json.parseToJsonElement(String(body, Charsets.UTF_8)).jsonObject
        val next = Json.encodeToString(JsonObject.serializer(), mutate(parsed))
        return BackupFormat.assemble(
            headerMutate(BackupFormat.decodeHeader(line)),
            next.toByteArray(Charsets.UTF_8),
        )
    }

    private fun replaceFirstEvent(body: JsonObject, change: (JsonObject) -> JsonObject): JsonObject {
        val array = body.getValue("events") as JsonArray
        val replaced = listOf(change(array.first().jsonObject)) + array.drop(1)
        return withEvents(body, JsonArray(replaced))
    }

    private fun withEvents(body: JsonObject, events: JsonArray): JsonObject = buildJsonObject {
        put("events", events)
        put("state", body.getValue("state"))
    }

    private fun withState(body: JsonObject, state: JsonObject): JsonObject = buildJsonObject {
        put("events", body.getValue("events"))
        put("state", state)
    }

    private companion object {

        /**
         * One encrypted document for the whole class, because building it runs the
         * shipped scrypt parameters and those are deliberately expensive. Both
         * password cases read this same file.
         */
        val encrypted: ByteArray by lazy {
            runBlocking {
                val events = BackupFixture.events(count = 40, seed = 3L)
                val service = BackupService(
                    store = RecordingBackupStore(BackupFixture.snapshot(events)),
                    clock = BackupFixture.clock(),
                    appVersion = "0.9.0",
                    random = BackupFixture.fixedRandom(),
                )
                val target = MemoryTarget()
                service.export(target, password = "the right password".toCharArray())
                target.document
            }
        }
    }
}
