package com.kamsiob.claritynow.data.widget

import kotlinx.serialization.json.Json

/**
 * The snapshot as text, and the one place it is turned back into a value.
 *
 * **A snapshot that cannot be read is not an error a person should ever see.** It is
 * decoded on the path that draws a home screen widget, often in a process the system
 * started for that alone, and the honest failure there is a quiet widget rather than a
 * crash in somebody's launcher. So [decode] answers null, and every caller already has
 * a null branch because a fresh install has no snapshot either. It stays free of
 * Android imports so the round trip can be asserted on a desktop JVM.
 *
 * Three settings, each of them load bearing.
 *
 * - `ignoreUnknownKeys` so that a snapshot written by a newer build, which is what an
 *   install that was downgraded or a backup that was restored produces, still yields
 *   the fields this build understands
 * - `encodeDefaults` so that the written file is complete rather than a diff against a
 *   default this build happens to hold. The file is small and a reader from another
 *   build should not have to know our defaults to understand it
 * - `explicitNulls = false` so an absent value is an absent key, which is the same
 *   thing the defaults above already mean and keeps the file readable
 *
 * It is deliberately not pretty printed. Nobody reads this file by hand: the export in
 * MASTER_BUILD_PROMPT 14b.7 is the readable artifact, and this is a cache.
 */
object ClarityWidgetSnapshotCodec {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        explicitNulls = false
    }

    fun encode(snapshot: ClarityWidgetSnapshot): String =
        json.encodeToString(ClarityWidgetSnapshot.serializer(), snapshot)

    /** The snapshot, or null when there is nothing readable there. */
    fun decode(text: String?): ClarityWidgetSnapshot? {
        if (text.isNullOrBlank()) return null
        return runCatching {
            json.decodeFromString(ClarityWidgetSnapshot.serializer(), text)
        }.getOrNull()
    }
}
