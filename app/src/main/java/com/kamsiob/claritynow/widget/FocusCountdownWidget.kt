package com.kamsiob.claritynow.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import androidx.glance.layout.size
import androidx.glance.semantics.contentDescription
import androidx.glance.semantics.semantics
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.kamsiob.claritynow.R
import com.kamsiob.claritynow.data.widget.ClarityWidgetSnapshotStore
import com.kamsiob.claritynow.data.widget.WidgetFocus
import com.kamsiob.claritynow.ui.theme.FocusPalette
import com.kamsiob.claritynow.ui.theme.calmed

/**
 * Focus Countdown. `MASTER_BUILD_PROMPT.md` 13.3, `design-v3.md` 12.2 and 11.3,
 * Addendum 01 8d.
 *
 * Live during a session: the depleting arc as the primary carrier, the digits
 * secondary. With no session running, a `Start focus` target. Either way one tap opens
 * the focus screen.
 *
 * ## Time reads as a shape before it reads as a number
 *
 * That is Addendum 01 8d and it decides the whole layout. The arc is as large as the
 * widget allows and the numeral is a fifth of it, which is close to the hierarchy the
 * 240dp ring and the 64sp numeral have on the session screen. A person glancing at a
 * home screen is not doing arithmetic: they are asking how much of this is left, and a
 * shrinking shape answers that in the time it takes to look at it.
 *
 * ## The refresh cadence, chosen rather than tuned
 *
 * `design-v3.md` 12.2 asks for the reasoning to be recorded rather than adjusted until
 * it looks right, and it is in [FocusCountdownRefresh]. The short version, because it
 * governs what is drawn here: **the widget refreshes once a minute, so the arc moves in
 * whole minutes.** [minutesRemaining] rounds up and [fractionRemaining] is a ratio of
 * whole minutes rather than of seconds, so the shape never claims a precision the
 * launcher will not deliver. 12.2 names the failure this avoids: an arc that jumps four
 * minutes at a time is worse than one that moves in minutes and is honest about it.
 *
 * Remaining time is computed at render, from the end instant in the snapshot. A stored
 * countdown would be wrong the second after it was written, and the system runs a widget
 * update when it is ready rather than when it was asked; subtracting at render means a
 * refresh that arrives late still draws the right shape.
 *
 * ## The indigo ground
 *
 * See [FocusWidgetPalette]. This is the one widget in the app that does not follow the
 * home screen's theme, because the room it is a window into does not follow it either.
 */
class FocusCountdownWidget : GlanceAppWidget() {

    /** The arc is drawn to the widget's real size, so a size bucket is not enough. */
    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val snapshot = ClarityWidgetSnapshotStore(context).read()
        val now = widgetNowMillis()
        // Calm mode arrives in the snapshot like everything else a widget knows, per
        // `design-v3.md` 12.1 and 16.3, rather than being read from preferences here. A
        // widget with no snapshot yet has no session to draw either.
        val calm = snapshot?.calmMode ?: false
        // Arms the once a minute chain while a session is running, and does nothing at
        // all otherwise. Deliberately never cancels: see FocusCountdownRefresh.
        FocusCountdownRefresh.arm(context, snapshot?.focus, now)
        provideContent {
            FocusCountdownContent(focus = snapshot?.focus, nowMillis = now, calm = calm)
        }
    }
}

class FocusCountdownWidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget: GlanceAppWidget = FocusCountdownWidget()
}

@Composable
private fun FocusCountdownContent(focus: WidgetFocus?, nowMillis: Long, calm: Boolean) {
    val context = LocalContext.current
    val size = LocalSize.current
    val minutes = focus?.let { minutesRemaining(it, nowMillis) } ?: 0

    // One node for a screen reader, reading as a sentence rather than as a label, per
    // `design-v3.md` 12.1. The root is clickable and carries the description, so the
    // widget is announced once and what a tap does is part of what is announced.
    //
    // Zero minutes on a running session is the planned time having run out while nobody
    // was looking at the app, which is a completion waiting to be resolved rather than a
    // failure, and the line says exactly that and nothing more.
    val spoken = when {
        focus == null -> context.getString(R.string.cd_widget_focus_start)
        minutes <= 0 -> context.getString(R.string.cd_widget_focus_elapsed)
        else -> context.resources.getQuantityString(
            R.plurals.cd_widget_focus_remaining,
            minutes,
            minutes,
        )
    }

    Box(
        modifier = WidgetTheme.surface(FocusWidgetPalette.ground)
            .clickable(
                // The session id when there is one, so the intent says which session it
                // is about. MainActivity reads the request and never the id, which is
                // what lets the no session case send the same action with nothing on it.
                actionStartActivity(WidgetIntents.focusSurface(context, focus?.sessionId)),
            )
            .semantics { contentDescription = spoken },
        contentAlignment = Alignment.Center,
    ) {
        if (focus == null) {
            Text(
                text = context.getString(R.string.widget_focus_start),
                style = WidgetTheme.serifLarge.copy(color = FocusWidgetPalette.textBright),
                maxLines = 2,
            )
        } else {
            FocusArc(
                minutes = minutes,
                fractionRemaining = fractionRemaining(focus, nowMillis),
                edge = arcEdge(size.width, size.height),
                calm = calm,
            )
        }
    }
}

/**
 * The ring, and the numeral inside it.
 *
 * Every dimension is a proportion of [edge] rather than a fixed dp, because this widget
 * exists at 2x2 and at whatever a person resizes it to, and those proportions are what
 * make it the same object as the ring on the session screen. The two floors are there
 * because a stroke that is honest at 240dp is invisible at 78dp: the in app ring is
 * deliberately fine, `ui/focus/FocusRing.kt`, and fineness does not survive being scaled
 * down by two thirds and looked at from arm's length.
 */
@Composable
private fun FocusArc(minutes: Int, fractionRemaining: Float, edge: Dp, calm: Boolean) {
    val context = LocalContext.current
    val density = context.resources.displayMetrics.density
    val strokeDp = (edge.value * STROKE_RATIO).coerceAtLeast(MIN_STROKE_DP)
    val tipDp = (edge.value * TIP_RATIO).coerceAtLeast(MIN_TIP_DP)

    val arc = WidgetCanvas.depletingArc(
        diameterPx = (edge.value * density).toInt(),
        strokePx = strokeDp * density,
        tipDiameterPx = tipDp * density,
        fractionRemaining = fractionRemaining,
        // 16.7: the track is untouched by calm mode, the stroke and the tip take the
        // transform. The same three tokens the session screen draws.
        trackColor = FocusPalette.ringTrack.toArgb(),
        progressColor = FocusPalette.ringProgress.calmed(calm).toArgb(),
        tipColor = FocusPalette.ringTip.calmed(calm).toArgb(),
    )

    val numeral = TextStyle(
        color = FocusWidgetPalette.textBright,
        fontSize = (edge.value * NUMERAL_RATIO).coerceIn(MIN_NUMERAL_SP, MAX_NUMERAL_SP).sp,
        textAlign = TextAlign.Center,
    )

    Box(modifier = GlanceModifier.size(edge), contentAlignment = Alignment.Center) {
        Image(
            provider = ImageProvider(arc),
            // The whole widget is one node and carries the sentence. A description here
            // would have a screen reader say the time twice.
            contentDescription = null,
            modifier = GlanceModifier.size(edge),
        )
        Column(
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = minutes.toString(), style = numeral, maxLines = 1)
            if (edge >= WORD_MIN_EDGE) {
                Text(
                    // The session screen's own word, so the two surfaces say the same
                    // thing rather than nearly the same thing. design-v3.md 10.18.
                    text = context.getString(R.string.focus_remaining),
                    style = WidgetTheme.caption.copy(color = FocusWidgetPalette.textFaint),
                    maxLines = 1,
                )
            }
        }
    }
}

/** The largest ring the widget can hold, inside 12.1's 16dp of padding. */
private fun arcEdge(width: Dp, height: Dp): Dp {
    val inner = minOf(width, height) - WidgetTheme.padding * 2
    return if (inner < MIN_ARC_EDGE) MIN_ARC_EDGE else inner
}

/**
 * Whole minutes left, rounded up, so a session reads its full length for the whole of
 * its first minute. The same rule as `FocusCountdown.countdownAt` and the same rounding
 * as the spoken figure in `ui/focus/FocusRing.kt`.
 */
private fun minutesRemaining(focus: WidgetFocus, nowMillis: Long): Int {
    val remainingMillis = (focus.endsAtMillis - nowMillis).coerceAtLeast(0L)
    val plannedMillis = focus.plannedSeconds.coerceAtLeast(0) * MILLIS_PER_SECOND
    val capped = minOf(remainingMillis, plannedMillis)
    return ((capped + MILLIS_PER_MINUTE - 1L) / MILLIS_PER_MINUTE).toInt()
}

/**
 * How much of the ring is still drawn, in whole minutes over whole minutes.
 *
 * **Quantized rather than continuous, and that is the honest arithmetic**, per 12.2. The
 * widget is redrawn once a minute, so an arc computed from seconds would sit at a
 * position it had left up to fifty nine seconds earlier and imply a smoothness nothing
 * behind it has.
 */
private fun fractionRemaining(focus: WidgetFocus, nowMillis: Long): Float {
    val plannedMinutes = (focus.plannedSeconds + SECONDS_PER_MINUTE - 1) / SECONDS_PER_MINUTE
    if (plannedMinutes <= 0) return 0f
    return (minutesRemaining(focus, nowMillis).toFloat() / plannedMinutes).coerceIn(0f, 1f)
}

private const val MILLIS_PER_SECOND = 1_000L

private const val MILLIS_PER_MINUTE = 60_000L

private const val SECONDS_PER_MINUTE = 60

/** 6dp on a 240dp ring, `ui/focus/FocusRing.kt`, expressed as the proportion it is. */
private const val STROKE_RATIO = 0.025f

/** 10dp on a 240dp ring. */
private const val TIP_RATIO = 0.042f

/**
 * The numeral is a fifth of the ring rather than the quarter it is in app, and the two
 * bounds keep it inside the circle at both ends of the widget's size range and at the
 * font scales a widget cannot cap the way the session screen does.
 */
private const val NUMERAL_RATIO = 0.20f

private const val MIN_NUMERAL_SP = 16f

private const val MAX_NUMERAL_SP = 30f

private const val MIN_STROKE_DP = 3f

private const val MIN_TIP_DP = 6f

private val MIN_ARC_EDGE = 48.dp

/** Below this the ring has no room for a word under the numeral, so it carries none. */
private val WORD_MIN_EDGE = 110.dp
