package com.kamsiob.claritynow.data.widget

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.kamsiob.claritynow.widget.WidgetSnapshotStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

/**
 * Where [ClarityWidgetSnapshot] lives. MASTER_BUILD_PROMPT 13.3, "written to DataStore
 * on every meaningful change".
 *
 * ## One document, one key, one file
 *
 * The snapshot is a tree with a list of areas in it, and preferences are a flat map, so
 * a preferences shaped version of it would be a hand written encoding with an index in
 * the key names. One JSON string in one key is the same file on disk, is versioned by
 * [ClarityWidgetSnapshot.schema], and is decoded by code a unit test can run with no
 * Android at all. The typed `DataStore<T>` with a custom serializer is the other
 * reasonable answer and was not taken, because it buys nothing here and costs a
 * serializer to keep in step with a type that is already `@Serializable`.
 *
 * ## The instance is borrowed, and that is not an accident
 *
 * **DataStore permits one instance per file per process and throws on the second.**
 * `widget/WidgetSnapshotStore.kt` owns the instance for `clarity_widget_snapshot` and
 * this reaches it through [WidgetSnapshotStore.dataStore] rather than declaring a
 * second delegate over the same name, which would be a crash on somebody's home screen
 * the first time both halves of this phase were touched in one process. The two shapes
 * in that one file are phase 12's open seam and both sides say so.
 *
 * ## One process
 *
 * DataStore is not multi process safe, and this is read from a widget update. That is
 * safe because **no receiver, service or activity in this app declares
 * `android:process`**, so a Glance update runs in the same process as the app that
 * wrote the file. Anything that later moves a widget receiver into its own process has
 * to move this to a store that can take it, and that is a decision rather than a
 * detail.
 */
class ClarityWidgetSnapshotStore(context: Context) {

    private val appContext = context.applicationContext

    private val store = WidgetSnapshotStore.dataStore(appContext)

    /**
     * The snapshot as it changes, and null until one has been written.
     *
     * An [IOException] from the store is a file that has gone missing or a disk that
     * failed, which on this path means a widget with nothing to draw rather than an
     * exception thrown into a launcher. It is logged and reported as absence.
     */
    val snapshot: Flow<ClarityWidgetSnapshot?> = store.data
        .catch { failure ->
            if (failure is IOException) {
                Log.w(
                    TAG,
                    "the widget snapshot could not be read: " +
                        (failure.message ?: "no detail"),
                )
                emit(emptyPreferences())
            } else {
                throw failure
            }
        }
        .map { preferences -> ClarityWidgetSnapshotCodec.decode(preferences[KEY]) }

    /** One read, for a caller that is drawing once rather than following. */
    suspend fun read(): ClarityWidgetSnapshot? = snapshot.first()

    /**
     * Writes [snapshot], replacing whatever was there.
     *
     * There is no merge and there cannot be one: this is a projection of the log, and
     * the log is the truth, so a partial write would be a second opinion about state
     * that has one source. A failed write leaves the previous snapshot in place, which
     * is the right degradation: a widget showing what was true an hour ago is better
     * than a widget showing nothing, and the next change writes again.
     */
    suspend fun write(snapshot: ClarityWidgetSnapshot): Boolean = try {
        val encoded = ClarityWidgetSnapshotCodec.encode(snapshot)
        store.edit { preferences -> preferences[KEY] = encoded }
        true
    } catch (failure: IOException) {
        Log.w(TAG, "the widget snapshot could not be written: ${failure.message ?: "no detail"}")
        false
    }

    /**
     * Erase all data, MASTER_BUILD_PROMPT 14.2.
     *
     * **A widget is a place erased data can survive**, which is exactly the kind of copy
     * that outlives a wipe if nobody names it. [ClarityWidgetSnapshotWriter] already
     * writes an empty snapshot the moment the projection empties, so this is the belt to
     * that pair of braces and is safe to call twice.
     */
    suspend fun clear(): Boolean = write(ClarityWidgetSnapshot.NOTHING)

    private companion object {

        const val TAG = "ClarityWidgets"

        val KEY = stringPreferencesKey("document")
    }
}
