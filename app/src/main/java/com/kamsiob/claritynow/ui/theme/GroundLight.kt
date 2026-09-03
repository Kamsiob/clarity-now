package com.kamsiob.claritynow.ui.theme

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlin.math.sqrt

/**
 * A very slight center of light on the Areas ground. `docs/VISUAL_DIRECTION.md`.
 *
 * ## What it is for
 *
 * The Areas screen is mostly ground. At two areas on a tall phone something like sixty
 * percent of it is canvas, and a flat field of one value reads as vacancy: nothing on it
 * is anywhere in particular, because there is no anywhere. The same emptiness on a field
 * that has a center reads as room around the content rather than as content that ran out.
 *
 * That is the whole claim, and it is why this is worth two percent of lightness and one
 * draw call.
 *
 * ## The numbers, and why they are so small
 *
 * Center at half the width and **0.32 of the height**, which is above the geometric
 * middle: the content sits in the upper two thirds, and a light that centered on the
 * screen would center on the empty part. Radius 0.85 of the diagonal, so the falloff is
 * gentle and the corners are not visibly darker than the middle. Amplitude **2.5 percent
 * of lightness**, which is about four steps of 8 bit sRGB either side of the token.
 *
 * At that amplitude nobody will ever point at this and name it, which is the intent. It
 * is felt as the page having a shape and it is never seen as a gradient.
 *
 * ## Why this is not the gradient the design rules refuse
 *
 * `design-v3.md` 14 bans a colored stripe, bar or edge treatment on an element; this is
 * a ground and touches no element. 15.1 and 15.3 refuse gradients on titles, numerals and
 * large figures, and gradient meshes as decoration; this carries no hue shift at all, only
 * lightness, and it is one radial stop pair rather than a mesh. 6.1's one separation
 * device per element is untouched, because a ground is not a device on anything.
 *
 * It does not animate. Section 14: only time moves.
 *
 * ## Banding
 *
 * Two and a half percent stretched over 2400 pixels at 8 bits can band. Skia dithers its
 * gradients and the reach here is short, so it should not, but a PNG capture and the panel
 * are different pipelines and a screenshot cannot answer it. It was looked at on the
 * device.
 *
 * The fallback, if a future panel does band, is a tiled 8 bit noise bitmap at two to three
 * percent through an `ImageShader`, which is one texture read. **Not AGSL**: `RuntimeShader`
 * is API 33 against this app's floor of 31, so a shader needs a branch and a dead path
 * while a bitmap needs neither.
 */
fun Modifier.groundLight(colors: ClarityColors): Modifier = drawBehind {
    val diagonal = sqrt(size.width * size.width + size.height * size.height)
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(colors.groundCenter, colors.groundEdge),
            center = Offset(size.width * 0.5f, size.height * CENTER_HEIGHT_FRACTION),
            radius = diagonal * RADIUS_FRACTION,
        ),
    )
}

/** Above the geometric middle, because the content is. */
private const val CENTER_HEIGHT_FRACTION = 0.32f

private const val RADIUS_FRACTION = 0.85f

/**
 * The two ends of the light, derived from `canvas` rather than declared beside it.
 *
 * Held here rather than in `ClarityColors` because they are not tokens: nothing chooses
 * between them, no component reads one, and a person cannot name either. They are the one
 * token plus and minus an amount, and writing them as two more entries in the palette
 * would invite somebody to set them independently and put a hue shift in the ground.
 */
internal val ClarityColors.groundCenter: Color
    get() = if (isDark) Color(0xFF14131A) else Color(0xFFE3E1D7)

internal val ClarityColors.groundEdge: Color
    get() = if (isDark) Color(0xFF0C0C10) else Color(0xFFDCDAD0)
