package com.kamsiob.claritynow.data.export

import com.kamsiob.claritynow.data.event.ClarityEvent
import com.kamsiob.claritynow.data.event.ClarityEventJson
import com.kamsiob.claritynow.data.event.ClarityEventType
import com.kamsiob.claritynow.domain.replay.ClarityReplay
import com.kamsiob.claritynow.domain.replay.ClarityState
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.security.GeneralSecurityException
import java.security.SecureRandom
import javax.crypto.AEADBadTagException

/**
 * Reads and writes the whole backup document. Nothing in this file can write to the
 * database, and that is deliberate rather than incidental: MASTER_BUILD_PROMPT 14b.7
 * requires that a file be validated **before anything is touched**, and the way to
 * be sure of that is for the thing doing the validating to have no way to touch
 * anything. [BackupService] holds the two halves apart, and a caller cannot reach
 * the applying half without an [BackupRead.Opened] that this object produced.
 *
 * The body carries both halves of the database, as 14b.7 asks: `events` is the log,
 * which is the truth, and `state` is the projection folded from it, which is a
 * cache. The projection is written so that a person can read the file and see their
 * own areas and items in it, and so that a second implementation can check its own
 * fold against the one that produced the file. **It is never trusted on the way
 * in.** [BackupRead.Opened.foldedState] is always this build's own fold of the
 * events, and a disagreement with the recorded projection is reported rather than
 * refused, because the log is the truth and a rebuild from it is always correct.
 * Refusing a person their own history because a later build's reducer improved
 * would be the failure this whole feature exists to prevent.
 */
object BackupCodec {

    private val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
        explicitNulls = true
        prettyPrint = true
        prettyPrintIndent = "  "
    }

    /**
     * The whole file, ready to write. [password] null means no password, which
     * means a readable file, which the export screen has to say plainly.
     */
    fun write(
        snapshot: ExportSnapshot,
        password: CharArray?,
        appVersion: String,
        createdAt: Long,
        createdOn: String,
        random: SecureRandom,
    ): ByteArray {
        val plain = encodeBody(snapshot.events, snapshot.state).toByteArray(Charsets.UTF_8)
        val header = BackupHeader(
            appVersion = appVersion,
            eventSchemaVersion = ClarityEvent.SCHEMA_VERSION,
            createdAt = createdAt,
            createdOn = createdOn,
            eventCount = snapshot.events.size,
            itemCount = snapshot.state.items.values.count { it.deletedAt == null },
            areaCount = snapshot.state.areas.values.count { it.deletedAt == null },
            encryption = if (password == null) {
                BackupCrypto.ENCRYPTION_NONE
            } else {
                BackupCrypto.ENCRYPTION_SCRYPT_AES_GCM
            },
            checksum = "",
        )
        if (password == null) return BackupFormat.assemble(header, plain)

        val sealed = BackupCrypto.seal(plain, password, random, BackupFormat.FORMAT_VERSION)
        plain.fill(0)
        return BackupFormat.assemble(
            header.copy(kdf = sealed.kdf, cipher = sealed.cipher),
            BackupCrypto.encode(sealed.cipherText).toByteArray(Charsets.US_ASCII),
        )
    }

    fun encodeBody(events: List<ClarityEvent>, state: ClarityState): String {
        val document = buildJsonObject {
            put("events", JsonArray(events.map(ClarityEventJson::toJsonObject)))
            put("state", json.encodeToJsonElement(ClarityState.serializer(), state.canonical()))
        }
        return json.encodeToString(JsonObject.serializer(), document)
    }

    /**
     * Every check, in the order that lets each refusal say the true thing.
     *
     * Cheap and certain first: a file that is not one of ours is told so before a
     * password is asked for, and the checksum is verified before a key is derived,
     * so a damaged file costs no seconds of scrypt and does not report itself as a
     * wrong password.
     */
    fun read(document: ByteArray, password: CharArray?): BackupRead {
        val split = BackupFormat.split(document)
            ?: return refuse(ImportRefusal.NOT_A_BACKUP, "no header line")
        val (line, body) = split

        val probe = runCatching { Json.parseToJsonElement(line).jsonObject }.getOrNull()
            ?: return refuse(ImportRefusal.NOT_A_BACKUP, "the first line is not a JSON object")
        if (probe["format"]?.jsonPrimitive?.contentOrNull != BackupFormat.FORMAT) {
            return refuse(ImportRefusal.NOT_A_BACKUP, "the format field is not this format")
        }

        val header = runCatching { BackupFormat.decodeHeader(line) }.getOrNull()
            ?: return refuse(ImportRefusal.DAMAGED, "the header is not readable")
        if (header.formatVersion > BackupFormat.FORMAT_VERSION) {
            return refuse(ImportRefusal.NEWER_VERSION, "format version ${header.formatVersion}")
        }
        if (header.eventSchemaVersion > ClarityEvent.SCHEMA_VERSION) {
            return refuse(
                ImportRefusal.NEWER_VERSION,
                "event schema version ${header.eventSchemaVersion}",
            )
        }
        if (BackupFormat.checksumOf(header, body) != header.checksum) {
            return refuse(ImportRefusal.DAMAGED, "the checksum does not match the file")
        }

        val plain = when {
            !header.isEncrypted -> body
            else -> {
                val kdf = header.kdf
                val cipher = header.cipher
                if (kdf == null || cipher == null) {
                    return refuse(ImportRefusal.DAMAGED, "encrypted with no parameters recorded")
                }
                if (!BackupCrypto.canDerive(kdf, cipher)) {
                    return refuse(ImportRefusal.NEWER_VERSION, "unknown ${kdf.name} and ${cipher.name}")
                }
                if (password == null || password.isEmpty()) {
                    return refuse(ImportRefusal.PASSWORD_REQUIRED, "encrypted, no password given")
                }
                val cipherText = runCatching { BackupCrypto.decode(String(body, Charsets.US_ASCII)) }
                    .getOrNull()
                    ?: return refuse(ImportRefusal.DAMAGED, "the sealed body is not base64")
                try {
                    BackupCrypto.unseal(cipherText, password, kdf, cipher, header.formatVersion)
                } catch (bad: AEADBadTagException) {
                    // The only signal there is. A wrong password and an altered body
                    // are the same event to AES-GCM, and the password is far and away
                    // the likelier of the two, so that is what the sentence says.
                    return refuse(ImportRefusal.WRONG_PASSWORD, "the tag did not verify: ${bad.message}")
                } catch (failure: GeneralSecurityException) {
                    return refuse(ImportRefusal.DAMAGED, "the sealed body will not open: ${failure.message}")
                } catch (failure: IllegalArgumentException) {
                    return refuse(ImportRefusal.DAMAGED, "the sealing parameters are not usable: ${failure.message}")
                }
            }
        }

        val parsed = runCatching { Json.parseToJsonElement(String(plain, Charsets.UTF_8)).jsonObject }
            .getOrNull()
            ?: return refuse(ImportRefusal.DAMAGED, "the body is not a JSON object")
        val eventsElement = parsed["events"] as? JsonArray
            ?: return refuse(ImportRefusal.DAMAGED, "the body has no records")

        val events = ArrayList<ClarityEvent>(eventsElement.size)
        for (element in eventsElement) {
            val obj = element as? JsonObject
                ?: return refuse(ImportRefusal.DAMAGED, "a record is not an object")
            val typeName = obj["type"]?.jsonPrimitive?.contentOrNull
                ?: return refuse(ImportRefusal.DAMAGED, "a record has no type")
            if (ClarityEventType.fromName(typeName) == null) {
                return refuse(ImportRefusal.NEWER_VERSION, "a record of type $typeName")
            }
            val event = runCatching { ClarityEventJson.fromJsonObject(obj) }.getOrNull()
                ?: return refuse(ImportRefusal.DAMAGED, "a $typeName record will not decode")
            events += event
        }

        integrityFailure(header, events)?.let { return it }

        val recorded = runCatching {
            json.decodeFromJsonElement(
                ClarityState.serializer(),
                parsed["state"]?.jsonObject ?: JsonObject(emptyMap()),
            )
        }.getOrNull() ?: return refuse(ImportRefusal.DAMAGED, "the recorded state will not decode")

        val folded = ClarityReplay.replay(events)
        return BackupRead.Opened(
            header = header,
            events = events,
            foldedState = folded,
            foldMatchesRecordedState = folded.canonical() == recorded.canonical(),
        )
    }

    /**
     * What the log has to be internally true about before it is allowed anywhere
     * near the database. Every one of these is a property of the file itself, so a
     * later build with a different reducer still answers the same way.
     */
    private fun integrityFailure(header: BackupHeader, events: List<ClarityEvent>): BackupRead.Refused? {
        if (events.size != header.eventCount) {
            return refuse(
                ImportRefusal.INCONSISTENT,
                "the header says ${header.eventCount} records and the file holds ${events.size}",
            )
        }
        val seen = HashSet<String>(events.size)
        for (event in events) {
            if (event.id.isBlank() || event.originId.isBlank()) {
                return refuse(ImportRefusal.INCONSISTENT, "a record with no identity")
            }
            if (event.lamport < 0) {
                return refuse(ImportRefusal.INCONSISTENT, "a record with a negative position")
            }
            if (!seen.add(event.id)) {
                return refuse(ImportRefusal.INCONSISTENT, "the record ${event.id} appears twice")
            }
        }
        return null
    }

    private fun refuse(reason: ImportRefusal, diagnostic: String) =
        BackupRead.Refused(reason, diagnostic)
}

/**
 * The result of reading a file, and the only way to reach [BackupService.apply].
 *
 * A sealed interface rather than a nullable pair, so that the applying half takes a
 * type that can only have come from a document that passed every check.
 */
sealed interface BackupRead {

    /**
     * A file that is what it says it is.
     *
     * [foldedState] is this build's own fold of [events] and is what the counts on
     * the confirmation screen should be read from. [foldMatchesRecordedState] is
     * false when the projection recorded in the file disagrees with that fold,
     * which is information about the build that wrote the file rather than a fault
     * in the file, and never a reason to refuse it.
     */
    data class Opened(
        val header: BackupHeader,
        val events: List<ClarityEvent>,
        val foldedState: ClarityState,
        val foldMatchesRecordedState: Boolean,
    ) : BackupRead

    /**
     * [diagnostic] is for a bug report and for the tests. It is never shown to
     * anyone: the screen renders one fixed sentence chosen by [reason], and
     * [ImportRefusal] holds which sentence and why it is not a corpus line.
     */
    data class Refused(val reason: ImportRefusal, val diagnostic: String) : BackupRead
}
