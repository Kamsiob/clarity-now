package com.kamsiob.claritynow.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.height
import androidx.glance.semantics.contentDescription
import androidx.glance.semantics.semantics
import androidx.glance.text.Text
import com.kamsiob.claritynow.R
import com.kamsiob.claritynow.data.widget.ClarityWidgetSnapshot
import com.kamsiob.claritynow.data.widget.ClarityWidgetSnapshotStore
import com.kamsiob.claritynow.data.widget.WidgetArea
import com.kamsiob.claritynow.data.widget.WidgetTarget

/**
 * `Next Up`, 2x2. One active item, and what waits behind it.
 * `MASTER_BUILD_PROMPT.md` 13.3, `design-v3.md` 12.2.
 *
 * The dot, the area's name, the item's title as the single serif element, and a plain
 * count under it. Configurable to a pinned area, or automatic, which shows the least
 * recently touched area with something in it and changes once a day. A tap opens that
 * area, unless a focus session is running, in which case it opens the session; that
 * rule lives in [WidgetIntents.tap] rather than in any widget.
 *
 * **It reads the snapshot and nothing else.** No repository, no database, no corpus and
 * no engine, per 13.3. Every string below is either a name the person typed or a fixed
 * label from `strings.xml`, and there is no sentence on this widget that anything had
 * to decide how to say.
 *
 * **The count is absent at zero rather than reading `0 waiting`.** design-v3.md 12.2
 * asks for a plain count of what waits behind the active item, and a person with one
 * thing in an area is being told something they can see. 10.13's rule against reporting
 * emptiness back to somebody is the same rule the inbox chip follows on the Areas
 * screen.
 */
class NextUpWidget : GlanceAppWidget() {

    /**
     * Nothing here branches on size, so one composition is drawn and the launcher lays
     * it out at whatever the widget is. `SizeMode.Exact` would recompose on every
     * resize for a layout that would come out the same. `All Areas` is the widget that
     * genuinely needs the size, and it is the one that asks for it.
     */
    override val sizeMode: SizeMode = SizeMode.Single

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val snapshot = ClarityWidgetSnapshotStore(context).read() ?: ClarityWidgetSnapshot.NOTHING
        val configuredAreaId = WidgetConfiguration.areaId(context, id)
        val appWidgetId = WidgetConfiguration.appWidgetIdOf(context, id)
        provideContent {
            NextUpContent(context, snapshot, configuredAreaId, appWidgetId)
        }
    }
}

class NextUpWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = NextUpWidget()
}

@Composable
private fun NextUpContent(
    context: Context,
    snapshot: ClarityWidgetSnapshot,
    configuredAreaId: String?,
    appWidgetId: Int,
) {
    when (val target = snapshot.resolve(configuredAreaId)) {
        is WidgetTarget.Live -> NextUpArea(context, snapshot, target.area)
        is WidgetTarget.Archived -> ReconfigureNotice(context, appWidgetId, target.area.name)
        WidgetTarget.Deleted -> ReconfigureNotice(context, appWidgetId, null)
        WidgetTarget.NoAreas -> NoAreasNotice(context)
    }
}

@Composable
private fun NextUpArea(
    context: Context,
    snapshot: ClarityWidgetSnapshot,
    area: WidgetArea,
) {
    val title = area.activeItemTitle ?: context.getString(R.string.widget_idle)
    val waiting = waitingLine(context, area.queueCount)
    val destination = WidgetIntents.area(context, area.id)
    Column(
        modifier = areaSurface(area.colorHex, snapshot.calmMode)
            .clickable(
                actionStartActivity(
                    WidgetIntents.tap(context, destination, snapshot.focus?.sessionId),
                ),
            )
            .semantics {
                contentDescription = listOfNotNull(area.name, title, waiting).joinToString(". ")
            },
        verticalAlignment = Alignment.Vertical.Top,
    ) {
        AreaHeader(colorHex = area.colorHex, name = area.name)
        Spacer(GlanceModifier.height(10.dp))
        Text(text = title, style = AreaWidgetType.serif, maxLines = 3)
        Spacer(GlanceModifier.defaultWeight())
        if (waiting != null) {
            Text(text = waiting, style = AreaWidgetType.caption, maxLines = 1)
        }
    }
}
