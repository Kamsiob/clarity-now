package com.kamsiob.claritynow.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
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
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.semantics.contentDescription
import androidx.glance.semantics.semantics
import androidx.glance.text.Text
import com.kamsiob.claritynow.R
import com.kamsiob.claritynow.data.widget.ClarityWidgetSnapshotStore
import com.kamsiob.claritynow.data.widget.WidgetRhythm

/**
 * Rhythm. `MASTER_BUILD_PROMPT.md` 13.3, `design-v3.md` 12.2.
 *
 * The fourteen day dot row exactly as Momentum renders it, and one plain line beneath
 * it. Tap opens Momentum.
 *
 * ## This must never become a streak
 *
 * `design-v3.md` 12.2 calls the reason the strongest in section 14, and it is worth
 * writing down at the place somebody would add one. **No consecutive count, no longest
 * run, no chain, no flame, no comparison with the previous fortnight, and no color that
 * changes because several good days happened to sit together.** A gap is a lighter dot,
 * a smaller one, and nothing else is said about it anywhere.
 *
 * The engine has no streak fact by construction, `domain/momentum/MomentumView.kt`, and
 * [WidgetRhythm] carries fourteen independent days for the same reason. This file
 * must not reconstruct one by eye: nothing here reads two neighboring days together,
 * and the only number it renders is a count of a set, produced by the same
 * `momentum_active_days` string the Momentum screen uses.
 *
 * ## One line, and it is Momentum's line
 *
 * 13.3 quotes the line as `Active 11 of the last 14 days.` and 12.2 of the same document
 * gives the Momentum screen `Active X of last 14 days`, which is what ships. **The
 * shipped string is reused rather than a second one written**, because "exactly as
 * Momentum renders it" is the instruction, and two surfaces stating one fact in two
 * wordings is a worse failure than a widget that reads a definite article short of a
 * quotation. Changing it changes both surfaces at once, in `strings.xml`, which is the
 * property that matters.
 *
 * `WidgetRhythm.line` is where the app puts that line when it writes the snapshot, and
 * it is preferred when it is there. The fallback below renders the same string resource
 * with the same two numbers, so the two branches cannot produce two sentences; it exists
 * so that a snapshot written with the row and without the line still reads correctly
 * rather than silently losing the caption `design-v3.md` 13 requires under a graphic.
 *
 * The cascade in item 13 is absent, per 12.2: widgets do not animate.
 */
class RhythmWidget : GlanceAppWidget() {

    /** The row is drawn to the widget's real width, so a size bucket is not enough. */
    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val snapshot = ClarityWidgetSnapshotStore(context).read()
        provideContent {
            RhythmContent(
                rhythm = snapshot?.rhythm?.takeIf { it.activeDays.isNotEmpty() },
                runningSessionId = snapshot?.focus?.sessionId,
            )
        }
    }
}

class RhythmWidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget: GlanceAppWidget = RhythmWidget()
}

@Composable
private fun RhythmContent(rhythm: WidgetRhythm?, runningSessionId: String?) {
    val context = LocalContext.current

    // One node for a screen reader, reading as a sentence rather than as a label, per
    // `design-v3.md` 12.1. It names the window and the count of a set, and says nothing
    // about which days those were or how they sat together.
    val spoken = if (rhythm == null) {
        context.getString(R.string.widget_rhythm_empty)
    } else {
        context.getString(R.string.cd_widget_rhythm, rhythm.length, rhythm.activeCount)
    }

    Box(
        modifier = WidgetTheme.surface()
            .clickable(
                actionStartActivity(
                    WidgetIntents.tap(
                        context = context,
                        destination = WidgetIntents.momentum(context),
                        runningSessionId = runningSessionId,
                    ),
                ),
            )
            .semantics { contentDescription = spoken },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (rhythm == null) {
                // A widget with nothing behind it yet says what it is for and nothing
                // about the person, which is the latitude the two discovery lines on the
                // Momentum screen take and the limit of it.
                Text(
                    text = context.getString(R.string.widget_rhythm_empty),
                    style = WidgetTheme.caption,
                    maxLines = 2,
                )
            } else {
                DotRow(rhythm)
                Spacer(GlanceModifier.height(ROW_TO_CAPTION))
                Text(
                    text = rhythm.line.ifBlank {
                        context.getString(
                            R.string.momentum_active_days,
                            rhythm.activeCount,
                            rhythm.length,
                        )
                    },
                    style = WidgetTheme.caption,
                    maxLines = 1,
                )
            }
        }
    }
}

/**
 * The fourteen marks, at the dimensions `ui/momentum/MomentumScreen.kt` draws them.
 *
 * The four numbers below are that file's `ACTIVITY_CELL`, `ACTIVE_DOT`, `IDLE_DOT` and
 * the 1.5dp ring around today. They are restated rather than imported because they are
 * private to a Compose screen this package cannot reach into, and they are cited so that
 * a change on one side is findable from the other. **They are the same numbers or the
 * row is not the row.**
 */
@Composable
private fun DotRow(rhythm: WidgetRhythm) {
    val context = LocalContext.current
    val size = LocalSize.current
    val density = context.resources.displayMetrics.density
    val widthDp = (size.width - WidgetTheme.padding * 2).value.coerceAtLeast(CELL.value)

    val row = WidgetCanvas.rhythmRow(
        widthPx = (widthDp * density).toInt(),
        cellPx = CELL.value * density,
        activeDays = rhythm.activeDays,
        todayIndex = rhythm.todayIndex,
        activeDotPx = ACTIVE_DOT.value * density,
        idleDotPx = IDLE_DOT.value * density,
        ringStrokePx = TODAY_RING_STROKE.value * density,
        // The two ink tokens the Momentum row uses, resolved for the theme the launcher
        // is drawing in. A bitmap cannot carry a day and a night value the way a Glance
        // color provider can, so the provider is asked for the one that applies now.
        activeColor = WidgetTheme.inkSecondary.getColor(context).toArgb(),
        idleColor = WidgetTheme.inkTertiary.getColor(context).toArgb(),
    )

    Image(
        provider = ImageProvider(row),
        // The whole widget is one node and carries the sentence. A description here
        // would have a screen reader tally the row, which is a thing said about the
        // gaps, and `design-v3.md` 14 says nothing about them anywhere.
        contentDescription = null,
        modifier = GlanceModifier.fillMaxWidth().height(CELL),
        contentScale = ContentScale.Fit,
    )
}

/** The cell one mark occupies, sized so the ring around today has room inside it. */
private val CELL = 16.dp

private val ACTIVE_DOT = 9.dp

private val IDLE_DOT = 5.dp

private val TODAY_RING_STROKE = 1.5.dp

/** `ui/momentum/MomentumScreen.kt` puts the readout ten below the row. */
private val ROW_TO_CAPTION = 10.dp
