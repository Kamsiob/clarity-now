package com.kamsiob.claritynow.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.width
import androidx.glance.semantics.contentDescription
import androidx.glance.semantics.semantics
import androidx.glance.text.Text
import com.kamsiob.claritynow.R
import com.kamsiob.claritynow.data.widget.ClarityWidgetSnapshot
import com.kamsiob.claritynow.data.widget.ClarityWidgetSnapshotStore
import com.kamsiob.claritynow.data.widget.WidgetArea

/**
 * `All Areas`, 4x2. Every area as a row, with what is happening in it.
 * `MASTER_BUILD_PROMPT.md` 13.3, `design-v3.md` 12.2.
 *
 * A dot, a name, and the active item or `Idle`. Configurable to all areas or a chosen
 * subset. A tap on a row opens that area, unless a session is running, in which case
 * every tap opens the session.
 *
 * ## Three decisions this widget makes that the others do not
 *
 * **Color carries identity here and text does not.** 12.2 says the list should be
 * parseable without being read, which matters when reading is expensive, and the dot is
 * how that is done. Setting six names in six colors would be the obvious reading of the
 * same sentence and would turn a quiet list into a chart, so the names are ink and the
 * dots are the color. `AreaWidgetFrame.kt` states the rule for the package.
 *
 * **There is no serif element.** 12.1 puts the serif on "the single largest element",
 * and this widget has no single largest anything: it is a list of equals, and six serif
 * names would be six large elements rather than one. A list of rows is sans.
 *
 * **Rows are 48dp because a row is a touch target**, and what does not fit becomes one
 * plain line reading `and 2 more`, never a scroll and never a truncated dot row. See
 * `AreaWidgetMetrics.rowHeight`.
 */
class AllAreasWidget : GlanceAppWidget() {

    /**
     * The one widget in this package that genuinely needs its own size.
     *
     * How many rows fit is the whole layout question here, and the answer changes when
     * somebody drags the widget taller. `SizeMode.Exact` recomposes on a resize, which
     * is exactly the event that changes the answer.
     */
    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val snapshot = ClarityWidgetSnapshotStore(context).read() ?: ClarityWidgetSnapshot.NOTHING
        val chosenAreaIds = WidgetConfiguration.areaIds(context, id)
        val appWidgetId = WidgetConfiguration.appWidgetIdOf(context, id)
        provideContent {
            AllAreasContent(context, snapshot, chosenAreaIds, appWidgetId)
        }
    }
}

class AllAreasWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = AllAreasWidget()
}

@Composable
private fun AllAreasContent(
    context: Context,
    snapshot: ClarityWidgetSnapshot,
    chosenAreaIds: List<String>,
    appWidgetId: Int,
) {
    val live = snapshot.liveAreas
    // An empty subset means every area, which is what an unconfigured widget holds.
    val chosen = if (chosenAreaIds.isEmpty()) live else live.filter { it.id in chosenAreaIds }
    when {
        live.isEmpty() -> NoAreasNotice(context)
        // Every area this widget was pointed at has been archived or deleted. There is
        // no honest row left to draw, and naming one of several vanished areas would be
        // arbitrary, so the widget offers to be pointed somewhere else.
        chosen.isEmpty() -> ReconfigureNotice(context, appWidgetId, archivedName = null)
        else -> AreaRows(context, snapshot, chosen)
    }
}

@Composable
private fun AreaRows(
    context: Context,
    snapshot: ClarityWidgetSnapshot,
    areas: List<WidgetArea>,
) {
    val inner = LocalSize.current.height - WidgetTheme.padding * 2
    val shown = areas.take(rowCapacity(inner, areas.size))
    val hidden = areas.size - shown.size
    Column(
        modifier = WidgetTheme.surface(),
        verticalAlignment = Alignment.Vertical.Top,
    ) {
        shown.forEach { area -> AreaRow(context, snapshot, area) }
        if (hidden > 0) {
            val more = context.resources
                .getQuantityString(R.plurals.widget_and_more, hidden, hidden)
            Text(text = more, style = AreaWidgetType.caption, maxLines = 1)
        }
    }
}

@Composable
private fun AreaRow(context: Context, snapshot: ClarityWidgetSnapshot, area: WidgetArea) {
    val status = area.activeItemTitle ?: context.getString(R.string.widget_idle)
    val destination = WidgetIntents.area(context, area.id)
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .height(AreaWidgetMetrics.rowHeight)
            .clickable(
                actionStartActivity(
                    WidgetIntents.tap(context, destination, snapshot.focus?.sessionId),
                ),
            )
            .semantics { contentDescription = "${area.name}. $status" },
        verticalAlignment = Alignment.Vertical.CenterVertically,
    ) {
        AreaDot(area.colorHex)
        Spacer(GlanceModifier.width(AreaWidgetMetrics.dotGap))
        // The weight is what makes both lines ellipsize rather than push the row wider
        // than the widget. A long item title is the common case, not the edge one.
        Column(modifier = GlanceModifier.defaultWeight()) {
            Text(text = area.name, style = AreaWidgetType.rowName, maxLines = 1)
            Text(text = status, style = AreaWidgetType.rowStatus, maxLines = 1)
        }
    }
}

/**
 * How many rows fit, and whether the overflow line has to be paid for.
 *
 * The line is only reserved when there is something to overflow, so a widget holding
 * exactly as many areas as fit uses the whole height for them. Never fewer than one
 * row: a widget too short for even that is drawn cut off by the launcher rather than
 * drawn empty, and one row cut off still says which area is first.
 */
private fun rowCapacity(innerHeight: Dp, areaCount: Int): Int {
    val rows = (innerHeight / AreaWidgetMetrics.rowHeight).toInt()
    if (rows >= areaCount) return rows.coerceAtLeast(1)
    val withOverflow = (innerHeight - AreaWidgetMetrics.overflowHeight) /
        AreaWidgetMetrics.rowHeight
    return withOverflow.toInt().coerceAtLeast(1)
}
