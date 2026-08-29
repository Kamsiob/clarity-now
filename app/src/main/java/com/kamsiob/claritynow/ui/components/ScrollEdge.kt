package com.kamsiob.claritynow.ui.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * The scroll edge, and what happens to content that passes under the status bar or
 * behind the floating tab bar. design-v3.md 6.1, 10.4 and 15.3.
 *
 * Through phase 12 there was no treatment at all: a grep of `ui/` for `verticalGradient`,
 * `fadingEdge`, `blur(` or `overscroll` returned nothing, so a row scrolled up to the
 * clock and stopped being drawn at a hard pixel edge, and the tab bar was a pill with
 * sentences sliding out from under it. That is a genuine layering problem and it is the
 * one this file answers.
 *
 * ## Why a fade and not a blur, recorded because the blur is the obvious answer
 *
 * The statistically common 2026 answer to content passing behind a floating bar is a
 * translucent blur, and design-v3.md 15 requires the obvious answer to be named and
 * beaten rather than taken. It is refused here on two grounds and only the second is
 * about taste.
 *
 * design-v3.md 15.1 lists "glassmorphism used as decoration rather than to solve a
 * layering problem", and 15.3 already writes out this exact case: "content passing under
 * a floating bar is a real layering problem and it does deserve an answer; a fade to the
 * ground color is the permitted form of one, and reaching for the blur because it looks
 * more modern is exactly the move the entry describes". A blur here would also be a
 * second separation device on a bar that already carries elevation, which 6.1 forbids
 * outright.
 *
 * The second reason is that a fade is **true** and a blur is not. Behind the tab bar
 * there is a page, and a page is what a reader should see less and less of as the content
 * approaches the bar. A blur invents a frosted pane that is not part of this design's
 * material vocabulary anywhere else.
 *
 * ## It erases rather than paints, which is the whole of the implementation
 *
 * The obvious implementation is a rectangle of the ground color faded to transparent,
 * drawn over the content. That is wrong on three of this app's surfaces: the Report's
 * ground is two centers of gold light, the Pulse's shifts with the time of day, and the
 * Focus surface is a radial gradient. Painting a flat ground over any of them would cut a
 * dark band across a gradient at exactly the edge this treatment exists to soften.
 *
 * So the content is composited into an offscreen layer and the fade is drawn into it with
 * [BlendMode.DstOut], which removes the content's own alpha and reveals whatever is
 * actually behind it. It costs one offscreen composite per frame on the surfaces that use
 * it, and it takes no ground color parameter at all, so there is nothing at a call site
 * to get wrong and nothing to keep in step when a token moves.
 *
 * ## The curve
 *
 * A straight linear ramp has a visible termination: the eye finds the line where the fade
 * stops. The stops below are an ease that falls quickly at the edge and tails off, so the
 * far end of the band arrives at nothing rather than at almost nothing.
 *
 * @param top how far from the top edge the fade reaches. Zero leaves the top alone.
 * @param bottom how far from the bottom edge the fade reaches. Zero leaves it alone.
 */
fun Modifier.scrollEdgeFade(top: Dp = 0.dp, bottom: Dp = 0.dp): Modifier {
    if (top <= 0.dp && bottom <= 0.dp) return this
    return this
        .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
        .drawWithContent {
            drawContent()
            if (top > 0.dp) {
                val height = top.toPx()
                drawRect(
                    brush = Brush.verticalGradient(
                        colorStops = FADE_IN_FROM_EDGE,
                        startY = 0f,
                        endY = height,
                    ),
                    size = Size(size.width, height),
                    blendMode = BlendMode.DstOut,
                )
            }
            if (bottom > 0.dp) {
                val height = bottom.toPx()
                val topLeft = Offset(0f, size.height - height)
                drawRect(
                    brush = Brush.verticalGradient(
                        colorStops = FADE_OUT_TO_EDGE,
                        startY = topLeft.y,
                        endY = size.height,
                    ),
                    topLeft = topLeft,
                    size = Size(size.width, height),
                    blendMode = BlendMode.DstOut,
                )
            }
        }
}

/**
 * The two distances a screen adds to its own inset to get a band, named here so the
 * surfaces that fade cannot drift apart from each other.
 *
 * A screen computes `statusBars + [ScrollEdge.underTheClock]` for its top band and
 * `navigationBars + [TabBarInset] + [TabBarHeight] + [ScrollEdge.aboveTheBar]` for its
 * bottom one, which is the same arithmetic each of them already does for its content
 * padding.
 */
object ScrollEdge {
    /**
     * How far past the status bar the top fade runs.
     *
     * Short on purpose. The band has to finish above the first line of content at rest,
     * and every scrolling surface in this app starts its content at least 8dp below the
     * status bar, so a longer lead would put a permanent veil on a title nobody scrolled.
     */
    val underTheClock: Dp = 12.dp

    /**
     * How far above the tab bar's top edge the bottom fade begins.
     *
     * Longer than the top lead because the bar is 61dp of chrome rather than a 24dp
     * strip, and because content is fully gone by the bar's lower edge and only starting
     * to go at its upper one. The result is that a row sinks under the pill over about
     * 94dp rather than meeting it.
     */
    val aboveTheBar: Dp = 16.dp
}

/**
 * Fully removed at the edge, nothing removed at the far end.
 *
 * **The curve holds at full removal through the first third and then ramps.** It used to
 * start ramping immediately, which put content at 72 percent opacity a quarter of the way
 * into a band whose first three quarters *is* the status bar: scrolling Settings put a
 * readable `Calm mode` directly across the clock. A band is not a gradient over the whole
 * inset, it is an inset that is clear and then a gradient below it, and the hold is what
 * says so.
 */
private val FADE_IN_FROM_EDGE = arrayOf(
    0.00f to Color.Black,
    0.34f to Color.Black,
    0.55f to Color.Black.copy(alpha = 0.62f),
    0.75f to Color.Black.copy(alpha = 0.28f),
    1.00f to Color.Transparent,
)

/** The same curve, read from the other end. */
private val FADE_OUT_TO_EDGE = arrayOf(
    0.00f to Color.Transparent,
    0.25f to Color.Black.copy(alpha = 0.28f),
    0.45f to Color.Black.copy(alpha = 0.62f),
    0.66f to Color.Black,
    1.00f to Color.Black,
)
