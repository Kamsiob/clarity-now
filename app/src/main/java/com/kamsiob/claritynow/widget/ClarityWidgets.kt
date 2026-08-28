package com.kamsiob.claritynow.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.updateAll
import com.kamsiob.claritynow.data.widget.ClarityWidgetSnapshotStore
import com.kamsiob.claritynow.data.widget.ClarityWidgetSnapshotWriter
import com.kamsiob.claritynow.di.ClarityGraph
import com.kamsiob.claritynow.work.WidgetRefreshSchedule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * The widgets, as one thing the process starts. MASTER_BUILD_PROMPT 13.3.
 *
 * `ClarityApp.onCreate` calls [install] and nothing else has to know these exist. It
 * posts nothing, shows nothing and asks for nothing: a widget needs no permission and
 * has no prompt, and a person with no widget placed pays one collector and one piece of
 * periodic work for it.
 *
 * **The dispatcher is [Dispatchers.Default] and the job is a [SupervisorJob]**, for the
 * same two reasons `ClarityApp` gives for its own scope: the first thing this does is
 * replay the log, which is the wrong work to start inline on the main thread before the
 * first frame, and a failure here must take nothing else down with it. A home screen
 * that is an hour stale is a small thing beside a queue that will not open.
 */
object ClarityWidgets {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private var installed = false

    /**
     * Every widget phase 12 draws, both halves of it.
     *
     * New instances rather than singletons, because `GlanceAppWidget` holds no state of
     * its own and the receivers construct their own anyway. The list is the extension
     * point: a widget that is not in it is a widget the snapshot never redraws, which is
     * the one way a new widget can look correct on the day it is written and be a day
     * stale a week later.
     */
    private val all: List<GlanceAppWidget>
        get() = listOf(
            NextUpWidget(),
            FirstStepWidget(),
            AllAreasWidget(),
            QuickCaptureWidget(),
            FocusCountdownWidget(),
            RhythmWidget(),
        )

    fun install(context: Context) {
        if (installed || !ClarityGraph.isInstalled) return
        installed = true
        val appContext = context.applicationContext
        scope.launch { writer(appContext).follow() }
        WidgetRefreshSchedule.ensure(appContext)
    }

    /** One pass over the snapshot, for the periodic refresh. */
    suspend fun refresh(context: Context) {
        writer(context.applicationContext).refresh()
    }

    /**
     * Redraws every placed widget.
     *
     * A widget class with nothing placed costs one lookup and no work, so this is
     * unconditional rather than guarded by a count that would have to be kept.
     */
    suspend fun redrawAll(context: Context) {
        all.forEach { widget -> widget.updateAll(context) }
    }

    private fun writer(context: Context): ClarityWidgetSnapshotWriter =
        ClarityWidgetSnapshotWriter(
            repository = ClarityGraph.repository,
            preferences = ClarityGraph.preferences,
            clock = ClarityGraph.clock,
            store = ClarityWidgetSnapshotStore(context),
            // design-v3.md 16.1's two halves, resolved once, in the file that already
            // knows how. A stored null follows the system's reduce motion setting.
            calmMode = { widgetCalmMode(context) },
            onWritten = { redrawAll(context) },
        )
}
