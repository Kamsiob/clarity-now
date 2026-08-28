package com.kamsiob.claritynow.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition

/** Which of the three area widgets an instance is. */
internal enum class WidgetKind { NEXT_UP, FIRST_STEP, ALL_AREAS }

/**
 * What one placed widget was configured to show. design-v3.md 12.2.
 *
 * ## Per instance, in Glance's own state, and not in the snapshot
 *
 * The snapshot is one document describing the app, and this is a property of a
 * rectangle on somebody's home screen: two `Next Up` widgets side by side, one pinned
 * to Work and one automatic, is a thing 12.2 explicitly allows. So the choice lives in
 * the per widget preferences Glance already keeps and deletes with the widget, which is
 * also the only store that has an id to key it by.
 *
 * ## Absent means automatic, and that is the whole default
 *
 * A widget is placed with nothing here and shows the automatic area straight away.
 * design-v3.md 15 asks for the obvious answer to be named and beaten: the obvious one
 * is a required configuration screen at placement, which every widget with a choice
 * ships with, and it puts a form between somebody and a thing they just decided they
 * wanted. `configuration_optional` in the provider metadata is the platform's own way
 * out of it, and `reconfigurable` beside it is what keeps the screen reachable
 * afterwards, from the launcher's own edit affordance and from the line a widget shows
 * when the area it was pinned to has gone.
 */
internal object WidgetConfiguration {

    /** The pinned area for `Next Up` and `First Step`. Absent means automatic. */
    private val AREA_ID = stringPreferencesKey("configuredAreaId")

    /** The chosen subset for `All Areas`, comma separated. Absent means every area. */
    private val AREA_IDS = stringPreferencesKey("configuredAreaIds")

    suspend fun areaId(context: Context, glanceId: GlanceId): String? =
        state(context, glanceId)[AREA_ID]?.takeIf { it.isNotBlank() }

    /** The chosen subset, or an empty list meaning all of them. */
    suspend fun areaIds(context: Context, glanceId: GlanceId): List<String> =
        state(context, glanceId)[AREA_IDS]
            ?.split(SEPARATOR)
            ?.filter { it.isNotBlank() }
            .orEmpty()

    suspend fun setAreaId(context: Context, glanceId: GlanceId, areaId: String?) {
        updateAppWidgetState(context, glanceId) { preferences ->
            if (areaId == null) preferences.remove(AREA_ID) else preferences[AREA_ID] = areaId
        }
    }

    /** An empty list clears the subset, which is what "all areas" is stored as. */
    suspend fun setAreaIds(context: Context, glanceId: GlanceId, areaIds: List<String>) {
        updateAppWidgetState(context, glanceId) { preferences ->
            if (areaIds.isEmpty()) {
                preferences.remove(AREA_IDS)
            } else {
                preferences[AREA_IDS] = areaIds.joinToString(SEPARATOR)
            }
        }
    }

    /**
     * Which widget an app widget id belongs to, asked of the platform rather than
     * carried on the intent.
     *
     * The launcher starts the configuration screen with nothing but the id, so the
     * screen has to be able to answer this from the id alone. Reading the provider is
     * the one way that works for both entrances, the launcher's and the reconfigure
     * line inside a widget, so there is one path rather than two.
     */
    fun kindOf(context: Context, appWidgetId: Int): WidgetKind? {
        val info = AppWidgetManager.getInstance(context).getAppWidgetInfo(appWidgetId)
        return when (info?.provider?.className) {
            NextUpWidgetReceiver::class.java.name -> WidgetKind.NEXT_UP
            FirstStepWidgetReceiver::class.java.name -> WidgetKind.FIRST_STEP
            AllAreasWidgetReceiver::class.java.name -> WidgetKind.ALL_AREAS
            else -> null
        }
    }

    /** The widget behind [kind], for the redraw that follows a saved choice. */
    fun widgetFor(kind: WidgetKind): GlanceAppWidget = when (kind) {
        WidgetKind.NEXT_UP -> NextUpWidget()
        WidgetKind.FIRST_STEP -> FirstStepWidget()
        WidgetKind.ALL_AREAS -> AllAreasWidget()
    }

    /**
     * The intent that opens this screen for one placed widget.
     *
     * **The unique `data` is load bearing and is not decoration.** Glance turns an
     * action into a `PendingIntent` with a fixed request code, and two `PendingIntent`s
     * whose intents match under `Intent.filterEquals` are one `PendingIntent`, which
     * ignores extras. Two widgets of the same kind on one home screen would then both
     * configure whichever was drawn last. A distinct URI per widget id makes them
     * different intents, which is the same hazard `notifications/FocusIntents.kt` keeps
     * distinct request codes for.
     */
    fun configureIntent(context: Context, appWidgetId: Int): Intent =
        Intent(context, WidgetConfigurationActivity::class.java)
            .setAction(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE)
            .setData(Uri.parse("claritynow://widget/configure/$appWidgetId"))
            .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    fun glanceIdOf(context: Context, appWidgetId: Int): GlanceId =
        GlanceAppWidgetManager(context).getGlanceIdBy(appWidgetId)

    fun appWidgetIdOf(context: Context, glanceId: GlanceId): Int =
        GlanceAppWidgetManager(context).getAppWidgetId(glanceId)

    private suspend fun state(context: Context, glanceId: GlanceId) =
        getAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId)

    private const val SEPARATOR = ","
}
