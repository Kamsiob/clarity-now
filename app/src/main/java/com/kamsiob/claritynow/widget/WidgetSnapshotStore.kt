package com.kamsiob.claritynow.widget

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

/**
 * The one DataStore the widget snapshot lives in.
 *
 * This object holds an instance and nothing else. What is stored in it, and every field
 * a widget reads, is `data/widget/ClarityWidgetSnapshot.kt`, which is one JSON document
 * under one key; `data/widget/ClarityWidgetSnapshotStore.kt` is what reads and writes it.
 *
 * ## Why the instance lives here rather than beside the document
 *
 * **DataStore permits one instance per file per process and throws on the second.** Both
 * halves of phase 12 need this file: the half that composes the snapshot writes it, and
 * the three widgets in this package read it. A delegate declared on each side would be
 * two instances over one file and a crash on somebody's home screen the first time both
 * halves ran in one process. So the instance is declared once, here, at the layer that
 * cannot be avoided, and everything else borrows it through [dataStore].
 *
 * The delegate is a property on `Context`, which is how `preferencesDataStore` is meant
 * to be used and is the same shape `data/prefs/ClarityPreferences.kt` uses. It resolves
 * to one store per file regardless of how many times it is asked for.
 *
 * ## One process
 *
 * DataStore is not multi process safe, and this is read during a widget update. That is
 * safe because no receiver, service or activity in this app declares `android:process`,
 * so a Glance update runs in the same process as the app that wrote the file. Moving a
 * widget receiver into a process of its own means moving this to a store that can take
 * it, and that is a decision rather than a detail.
 */
object WidgetSnapshotStore {

    private val Context.snapshotStore: DataStore<Preferences> by preferencesDataStore(
        name = "clarity_widget_snapshot",
    )

    /** The one instance, for whichever side of the seam is asking. */
    fun dataStore(context: Context): DataStore<Preferences> =
        context.applicationContext.snapshotStore
}
