package com.kamsiob.claritynow.ui.report

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.kamsiob.claritynow.ui.theme.LocalCalmMode
import com.kamsiob.claritynow.ui.theme.LocalContemplativeColors
import com.kamsiob.claritynow.ui.theme.ReportPalette
import com.kamsiob.claritynow.ui.theme.calmed
import kotlin.math.hypot

/**
 * The gold editorial ground. `design-v3.md` 3.3 and 11.1.
 *
 * `deepBlack`, and two centers of light: a radial gold glow at 6 to 8 percent behind the
 * headline block, and a second, fainter one behind the closing line. It fills the whole
 * surface, including the space under the status bar, which is 11.1's last line.
 *
 * ## The glows are fixed to the room and do not follow the content
 *
 * 11.1 places them behind the headline and behind the closing line, and the page scrolls,
 * so those two are not the same instruction once a reader's thumb is on the screen. This
 * takes the room's reading, which is the one 3.3 already made for the Focus surface in the
 * same words: the gradient's center sits in the same place on the chooser and on the
 * completion screen, "since a light that moved when the content changed would make the room
 * itself feel like it had moved". A light anchored to a scrolling headline would do exactly
 * that, and on a short report it would also chase the headline off the top of the screen.
 *
 * So the two centers sit where those two elements sit when the page is at rest, which is
 * where it is when it is revealed and where it stays for a report short enough not to
 * scroll. Recorded under `design-v3.md` 15 as a choice the document left open.
 *
 * ## There are no specks here, and that is deliberate
 *
 * 3.3 gives the Contemplative world eight to fourteen specks of light and the Focus and
 * Pulse surfaces both take them. 11.1 is more specific about this surface than 3.3 is:
 * **four treatments and no more than four**, and the week ribbon is "the only non-text
 * element in the entire report". A field of specks is a fifth treatment and a second
 * non-text element, so the more specific rule wins and this surface is lit rather than
 * decorated. `design-v3.md` 15, and it is the unobvious answer: every other Contemplative
 * surface in this app has stars.
 *
 * ## Calm mode
 *
 * 16.7's table is explicit and unusually precise: the gold is transformed, the glows drop
 * to 4 percent, and **two centers of light become one, behind the headline only**. That is
 * the one place in this app where calm mode removes an element rather than quieting it,
 * and it is applied here rather than argued with.
 */
@Composable
internal fun ReportBackdrop(modifier: Modifier = Modifier) {
    val contemplative = LocalContemplativeColors.current
    val calm = LocalCalmMode.current
    val gold = ReportPalette.gold.calmed(calm)

    Canvas(modifier = modifier.fillMaxSize()) {
        drawRect(color = contemplative.deepBlack)

        val diagonal = hypot(size.width, size.height)

        glow(
            center = Offset(size.width * CENTER_X, size.height * HEADLINE_Y),
            radius = diagonal * HEADLINE_REACH,
            color = gold,
            alpha = if (calm) CALM_ALPHA else HEADLINE_ALPHA,
        )

        // 16.7: the second center of light does not exist in calm mode.
        if (!calm) {
            glow(
                center = Offset(size.width * CENTER_X, size.height * CLOSING_Y),
                radius = diagonal * CLOSING_REACH,
                color = gold,
                alpha = CLOSING_ALPHA,
            )
        }
    }
}

/**
 * One center of light, drawn as a radial fade to transparent.
 *
 * A rectangle brushed with a radial gradient rather than a circle, so the falloff reaches
 * the corners of whatever it is drawn over instead of ending at a visible circular edge.
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.glow(
    center: Offset,
    radius: Float,
    color: Color,
    alpha: Float,
) {
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(color.copy(alpha = alpha), Color.Transparent),
            center = center,
            radius = radius,
        ),
    )
}

/** Both centers sit on the measure's midline, because both elements they light are centered. */
private const val CENTER_X = 0.5f

/**
 * Where the two lights sit, as fractions of the surface height.
 *
 * The headline is the third element down a page whose first two are one caption line and
 * nothing, so its block sits a little above the middle. The closing line is the last thing
 * before the footer.
 */
private const val HEADLINE_Y = 0.30f
private const val CLOSING_Y = 0.86f

/**
 * How far each light reaches, as a fraction of the surface diagonal.
 *
 * The headline's is the larger of the two by design: 11.1 calls the second one fainter, and
 * a light that is both fainter and smaller is the difference a reader can actually see
 * between a page with two centers and a page with one and a smudge.
 */
private const val HEADLINE_REACH = 0.62f
private const val CLOSING_REACH = 0.42f

/** 3.3. Six to eight percent, taken at the middle of its range. */
private const val HEADLINE_ALPHA = 0.07f

/** Fainter, per 3.3, and by enough to read as the second light rather than a match. */
private const val CLOSING_ALPHA = 0.045f

/** 16.7. */
private const val CALM_ALPHA = 0.04f
