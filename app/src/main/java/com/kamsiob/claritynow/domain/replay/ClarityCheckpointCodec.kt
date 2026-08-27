package com.kamsiob.claritynow.domain.replay

import kotlinx.serialization.json.Json

/**
 * The one encoding of a checkpoint, and the one decision about what to do with a
 * checkpoint that will not decode.
 *
 * `clarity_week_snapshot` stores the two halves in two columns, so this hands back
 * two strings rather than one document. The shape is the storage layer's, and the
 * rules about what may be written into it are not, which is why they live here in
 * the pure package beside the fold they have to agree with.
 *
 * **Two serializer settings, both load bearing across app versions.**
 *
 * `encodeDefaults` writes a field even when it holds its default. Without it a
 * checkpoint records only what happened to differ from the defaults of the build
 * that wrote it, and a later build that changes a default silently reinterprets
 * every checkpoint written before it.
 *
 * `ignoreUnknownKeys` lets a checkpoint written by a newer build decode here rather
 * than throw, which matters because a person can install an older APK over a newer
 * one. It is safe in exactly one direction: a field this build has never heard of
 * is dropped, and the resulting state is then short of whatever that field held, so
 * [decode] is not the last line of defense. `ClarityReplay.canResume` is: it counts
 * the log against the checkpoint's own `eventsApplied`, and a checkpoint this build
 * cannot fully understand fails that count and is thrown away.
 *
 * A checkpoint that fails to decode outright answers null, and null means a full
 * rebuild from event zero. That is always correct, so there is nothing to report to
 * anyone and nothing for a person to do. A cache that cannot be read is a cache
 * that gets rebuilt.
 */
object ClarityCheckpointCodec {

    private val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    fun encodePosition(position: ReplayPosition): String =
        json.encodeToString(ReplayPosition.serializer(), position)

    /**
     * Encodes the state in canonical form, so two devices that folded the same log
     * write the same bytes and a diff of two checkpoints is readable.
     */
    fun encodeState(state: ClarityState): String =
        json.encodeToString(ClarityState.serializer(), state.canonical())

    /** Null when either half is unreadable, which means rebuild from event zero. */
    fun decode(positionJson: String, stateJson: String): ClarityCheckpoint? = runCatching {
        ClarityCheckpoint(
            position = json.decodeFromString(ReplayPosition.serializer(), positionJson),
            state = json.decodeFromString(ClarityState.serializer(), stateJson),
        )
    }.getOrNull()
}
