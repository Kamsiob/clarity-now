package com.kamsiob.claritynow.widget

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * The two shapes a widget in this package draws, as bitmaps.
 *
 * ## Why a bitmap rather than Glance elements
 *
 * Glance emits `RemoteViews`, which can lay out boxes and text and cannot draw. A ring
 * is not a box. The fourteen dot row is fourteen boxes plus a ring around today, and
 * the ring is a stroke, which is the same problem one dot smaller.
 *
 * The alternative was to approximate: a stack of rounded boxes for the countdown and a
 * filled circle behind today's dot for the row. Both are the obvious answer and both
 * fail the same test, `design-v3.md` 12.2's "exactly as Momentum renders it" and 11.3's
 * "matching the in app ring". A drawn shape is the same shape in both places because it
 * is the same arithmetic; an approximation is a second design that drifts every time
 * either half is touched.
 *
 * ## What is drawn, and what is deliberately not
 *
 * No shadow, no gradient, no glow and no edge. `design-v3.md` 12.1 allows a widget one
 * separation device and the widget itself is already it, and section 14 forbids the
 * colored edge treatment outright. The in app ring carries a soft radial glow at its
 * tip, 3.3, and it is **not** reproduced here: that glow is atmosphere in a dark room,
 * and over a launcher's wallpaper at a sixth of the size it reads as a smudge.
 *
 * Every bitmap is transparent where nothing is drawn, so the widget's own ground shows
 * through and an ink at 38 percent composites the way it does everywhere else.
 */
internal object WidgetCanvas {

    /**
     * The depleting arc. `design-v3.md` 11.3 and 12.2.
     *
     * **It depletes clockwise from the top**, which `ui/focus/FocusRing.kt` documents as
     * the one case section 15 exempts from the open choice rule: a countdown running the
     * other way is a puzzle, and this is the one element in the app that has to be read
     * at a glance while doing something else. The empty part grows clockwise from twelve
     * o'clock and the head of what is left chases it around.
     *
     * [fractionRemaining] is quantized by the caller, not here. See
     * `FocusCountdownWidget`: the arc moves in whole minutes because the refresh arrives
     * in whole minutes, and a shape that moved in seconds would be claiming a precision
     * the launcher will not deliver.
     */
    fun depletingArc(
        diameterPx: Int,
        strokePx: Float,
        tipDiameterPx: Float,
        fractionRemaining: Float,
        trackColor: Int,
        progressColor: Int,
        tipColor: Int,
    ): Bitmap {
        val requested = diameterPx.coerceAtLeast(1)
        val size = requested.coerceAtMost(MAX_ARC_EDGE_PX)
        // Everything is scaled by the same factor when the cap bites, so a capped ring
        // is the same drawing at a smaller resolution rather than a different one with
        // a heavier stroke. It is one when the cap does not bite, which is every size
        // this widget is ordinarily used at.
        val scale = size.toFloat() / requested
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val center = size / 2f
        val radius = (size - strokePx * scale) / 2f

        val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = strokePx * scale
            strokeCap = Paint.Cap.ROUND
        }

        stroke.color = trackColor
        canvas.drawCircle(center, center, radius, stroke)

        val remaining = fractionRemaining.coerceIn(0f, 1f)
        if (remaining <= 0f) return bitmap

        val head = angleFor(remaining)
        stroke.color = progressColor
        canvas.drawArc(
            RectF(center - radius, center - radius, center + radius, center + radius),
            head,
            DEGREES * remaining,
            false,
            stroke,
        )

        val radians = head * DEGREES_TO_RADIANS
        val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = tipColor
        }
        canvas.drawCircle(
            center + radius * cos(radians),
            center + radius * sin(radians),
            tipDiameterPx * scale / 2f,
            fill,
        )
        return bitmap
    }

    /**
     * The fourteen day row, drawn the way `ui/momentum/MomentumScreen.kt` draws it.
     *
     * Every dimension here is that file's: a 16dp cell, a 9dp dot on an active day, a
     * 5dp dot on a quiet one, and a 1.5dp ring around today's cell. **A gap is a lighter
     * dot and a smaller one**, which is section 13's rule that color is never the only
     * signal, and it is the only thing this row ever says about a gap.
     *
     * The cells are distributed evenly across [widthPx] with the first and last centered
     * on the ends, which is what `Arrangement.SpaceBetween` produces on the screen.
     *
     * Item 13's cascade is absent, per `design-v3.md` 12.2: widgets do not animate.
     */
    fun rhythmRow(
        widthPx: Int,
        cellPx: Float,
        activeDays: List<Boolean>,
        todayIndex: Int,
        activeDotPx: Float,
        idleDotPx: Float,
        ringStrokePx: Float,
        activeColor: Int,
        idleColor: Int,
    ): Bitmap {
        val requested = widthPx.coerceAtLeast(1)
        val width = requested.coerceAtMost(MAX_BITMAP_EDGE_PX)
        // The same uniform scale the arc uses, so a capped row keeps its aspect ratio
        // and the image view's fit scaling puts it back at full width.
        val scale = width.toFloat() / requested
        val cell = cellPx * scale
        val ringStroke = ringStrokePx * scale
        val height = cell.roundToInt().coerceAtLeast(1)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        if (activeDays.isEmpty()) return bitmap

        val canvas = Canvas(bitmap)
        val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
        val ring = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = ringStroke
            color = activeColor
        }

        val centerY = height / 2f
        val half = min(cell, width.toFloat()) / 2f
        val span = (width - half * 2f).coerceAtLeast(0f)
        val step = if (activeDays.size > 1) span / (activeDays.size - 1) else 0f

        activeDays.forEachIndexed { index, active ->
            val centerX = half + step * index
            if (index == todayIndex) {
                canvas.drawCircle(centerX, centerY, half - ringStroke / 2f, ring)
            }
            fill.color = if (active) activeColor else idleColor
            val dot = if (active) activeDotPx else idleDotPx
            canvas.drawCircle(centerX, centerY, dot * scale / 2f, fill)
        }
        return bitmap
    }

    /**
     * Ceilings on a bitmap handed to `RemoteViews`, and they are not decoration.
     *
     * The platform gives each host a bitmap memory budget and **refuses an update that
     * exceeds it silently from here**: the widget goes on showing whatever it showed
     * last, which for a countdown is a frozen number that looks like a bug in the
     * session rather than in the drawing.
     *
     * A ring is square, so its cost grows with the square of the edge: at full density on
     * a large phone a resized widget would ask for something over a megabyte, twice,
     * because a size mode of `Exact` composes once per size the host offers. It is capped
     * at 384 and the image view scales it, which costs a little softness on a stroke that
     * is three dp wide at its thinnest and is invisible at the sizes this widget is
     * actually used at. The row is one cell tall, so its cost is linear and the cap only
     * has to stop something absurd.
     */
    private const val MAX_ARC_EDGE_PX = 384

    private const val MAX_BITMAP_EDGE_PX = 1024

    private const val DEGREES = 360f

    /**
     * Zero degrees is the trailing edge in canvas coordinates, so the top is -90, and
     * the head of the remaining arc has traveled the elapsed fraction clockwise from
     * there. The same two lines as `ui/focus/FocusRing.kt`, and they agree because they
     * are the same arithmetic rather than because somebody checked.
     */
    private fun angleFor(fractionRemaining: Float): Float =
        -90f + DEGREES * (1f - fractionRemaining)

    private const val DEGREES_TO_RADIANS = 0.017453292f
}
