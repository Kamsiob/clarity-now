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
 * `First Step`, 2x2. The smallest physical action, on the home screen.
 * `MASTER_BUILD_PROMPT.md` 13.3, `design-v3.md` 12.2, Addendum 01 6b.
 *
 * ## Why this widget exists, kept here because it decides what it draws
 *
 * The hardest moment is starting, and the title of a task is very often the intimidating
 * part of it. `Rewrite the proposal intro` is a wall. `Open the doc and read what is
 * there` is not. Putting the smallest possible action on the home screen removes the
 * activation barrier at the exact moment it bites, which is a thing a widget can do and
 * a list cannot.
 *
 * **So when a first step exists, the title is not drawn at all.** Not underneath, not at
 * caption weight, not as a content description. Setting the wall in small type under the
 * step would put the whole of the problem back on the screen the widget exists to keep
 * it off.
 *
 * ## The one place in the app that may ask for a first step
 *
 * Addendum 01 4b: the field is never required, never prompted for and never inferred,
 * and `MASTER_BUILD_PROMPT.md` 14b.2 repeats it. This widget is the single exception,
 * and it is an exception because somebody chose to put it on their home screen. The
 * prompt is one quiet caption under the title, it never moves, it never repeats itself,
 * and it is the only surface in this app that will ever say it. Nothing else may copy
 * this line.
 *
 * A tap starts a session on that item. With nothing active, it opens the Focus surface,
 * which is the same degradation the Focus chip performs in `design-v3.md` section 10.
 */
class FirstStepWidget : GlanceAppWidget() {

    /** One layout at every size, exactly as [NextUpWidget] draws one. */
    override val sizeMode: SizeMode = SizeMode.Single

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val snapshot = ClarityWidgetSnapshotStore(context).read() ?: ClarityWidgetSnapshot.NOTHING
        val configuredAreaId = WidgetConfiguration.areaId(context, id)
        val appWidgetId = WidgetConfiguration.appWidgetIdOf(context, id)
        provideContent {
            FirstStepContent(context, snapshot, configuredAreaId, appWidgetId)
        }
    }
}

class FirstStepWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = FirstStepWidget()
}

@Composable
private fun FirstStepContent(
    context: Context,
    snapshot: ClarityWidgetSnapshot,
    configuredAreaId: String?,
    appWidgetId: Int,
) {
    when (val target = snapshot.resolve(configuredAreaId)) {
        is WidgetTarget.Live -> FirstStepArea(context, snapshot, target.area)
        is WidgetTarget.Archived -> ReconfigureNotice(context, appWidgetId, target.area.name)
        WidgetTarget.Deleted -> ReconfigureNotice(context, appWidgetId, null)
        WidgetTarget.NoAreas -> NoAreasNotice(context)
    }
}

@Composable
private fun FirstStepArea(
    context: Context,
    snapshot: ClarityWidgetSnapshot,
    area: WidgetArea,
) {
    val itemId = area.activeItemId
    val step = area.activeItemFirstStep
    // The serif line is the step when there is one, the title when there is not, and
    // the idle word when there is no item at all. In that order, because the whole
    // point of the widget is that the step outranks the title.
    val headline = step
        ?: area.activeItemTitle
        ?: context.getString(R.string.widget_idle)
    // The prompt appears only in the middle case: an item is showing, by its title,
    // because it has no step yet.
    val prompt = if (step == null && area.activeItemTitle != null) {
        context.getString(R.string.widget_add_first_step)
    } else {
        null
    }
    val destination = if (itemId == null) {
        WidgetIntents.focusSurface(context, sessionId = null)
    } else {
        WidgetIntents.startFocus(context, areaId = area.id, itemId = itemId)
    }
    Column(
        modifier = areaSurface(area.colorHex, snapshot.calmMode)
            .clickable(
                actionStartActivity(
                    WidgetIntents.tap(context, destination, snapshot.focus?.sessionId),
                ),
            )
            .semantics {
                contentDescription = listOfNotNull(area.name, headline, prompt).joinToString(". ")
            },
        verticalAlignment = Alignment.Vertical.Top,
    ) {
        AreaHeader(colorHex = area.colorHex, name = area.name)
        Spacer(GlanceModifier.height(10.dp))
        Text(text = headline, style = AreaWidgetType.serif, maxLines = 4)
        Spacer(GlanceModifier.defaultWeight())
        if (prompt != null) {
            Text(text = prompt, style = AreaWidgetType.caption, maxLines = 1)
        }
    }
}
