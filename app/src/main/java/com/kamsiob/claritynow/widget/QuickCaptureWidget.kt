package com.kamsiob.claritynow.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.height
import androidx.glance.semantics.contentDescription
import androidx.glance.semantics.semantics
import androidx.glance.text.Text
import com.kamsiob.claritynow.R
import com.kamsiob.claritynow.data.widget.ClarityWidgetSnapshotStore

/**
 * Quick Capture. `MASTER_BUILD_PROMPT.md` 13.3, `design-v3.md` 12.2.
 *
 * One large tap target that opens capture straight into the unfiled inbox, with the
 * keyboard already up and no area to choose. Beneath it, the inbox count as plain text,
 * absent at zero.
 *
 * ## Why it asks for nothing
 *
 * **Every decision standing between the thought and the record is a place the thought
 * is lost.** That sentence is the reason the inbox exists at all, and this widget is the
 * shortest version of it: from a home screen, one tap, a keyboard, and a field. Not a
 * choice of area, not a priority, not a due date, not a confirmation. A person who has
 * to answer a question before writing something down will sometimes answer it, sometimes
 * put the phone away, and will not be able to tell you afterwards which of the two
 * happened. Filing is a thing that can be done later or never; capture is a thing that
 * has one moment and no second chance.
 *
 * ## No badge and no dot
 *
 * The count is plain text, per `design-v3.md` 12.2, section 14 as amended by Addendum
 * 01, and Addendum 01 4a, which says it twice. It is **not** a badge, **not** a red dot,
 * not a colored pill and not a number in a circle. An inbox count is a description of a
 * container, not an alert about a failure: a badge would turn thirty captured thoughts,
 * which is a person using the app exactly as intended, into thirty unread messages.
 * The same rule already governs the inbox chip on the Areas screen, `strings.xml`.
 *
 * At zero the line is absent rather than reading `0 items`, which is 12.2's own
 * instruction and the same idea one step further on: an empty inbox is not a state that
 * needs reporting.
 */
class QuickCaptureWidget : GlanceAppWidget() {

    /**
     * 12.2 allows this one at 2x2 or 1x1, and the two are different layouts rather than
     * one layout scaled, so the render needs the real size rather than a bucket.
     */
    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // Null until the app has ever written a snapshot, which is a fresh install and
        // is drawn as an inbox with nothing in it rather than as an error.
        val snapshot = ClarityWidgetSnapshotStore(context).read()
        provideContent {
            QuickCaptureContent(
                inboxCount = snapshot?.inboxCount ?: 0,
                runningSessionId = snapshot?.focus?.sessionId,
            )
        }
    }
}

class QuickCaptureWidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget: GlanceAppWidget = QuickCaptureWidget()
}

@Composable
private fun QuickCaptureContent(inboxCount: Int, runningSessionId: String?) {
    val context = LocalContext.current
    val size = LocalSize.current
    // A 1x1 has room for one short word and nothing else. The threshold is well above a
    // single cell and well below two, so it never lands on a launcher's grid boundary.
    val compact = size.width < COMPACT_EDGE || size.height < COMPACT_EDGE

    val countLine = context.resources.getQuantityString(
        R.plurals.widget_capture_inbox,
        inboxCount,
        inboxCount,
    )
    // One node for a screen reader, reading as a sentence rather than as a label, per
    // `design-v3.md` 12.1. The root is clickable and carries the description, so a
    // widget is announced once and what the tap does is part of what is announced.
    val spoken = if (inboxCount > 0) {
        context.resources.getQuantityString(
            R.plurals.cd_widget_capture_waiting,
            inboxCount,
            inboxCount,
        )
    } else {
        context.getString(R.string.cd_widget_capture)
    }

    Box(
        modifier = WidgetTheme.surface()
            .clickable(
                actionStartActivity(
                    WidgetIntents.tap(
                        context = context,
                        destination = WidgetIntents.capture(context),
                        runningSessionId = runningSessionId,
                    ),
                ),
            )
            .semantics { contentDescription = spoken },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val word = if (compact) {
                R.string.widget_capture_word_compact
            } else {
                R.string.widget_capture_word
            }
            Text(
                text = context.getString(word),
                style = if (compact) WidgetTheme.serifSmall else WidgetTheme.serifLarge,
                maxLines = 2,
            )
            if (!compact && inboxCount > 0) {
                Spacer(GlanceModifier.height(6.dp))
                Text(text = countLine, style = WidgetTheme.caption, maxLines = 1)
            }
        }
    }
}

/** Below this on either edge the widget is a 1x1 and shows the word alone. */
private val COMPACT_EDGE = 100.dp
