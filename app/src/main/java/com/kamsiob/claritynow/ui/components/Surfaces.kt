package com.kamsiob.claritynow.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.kamsiob.claritynow.ui.theme.ClarityColors
import com.kamsiob.claritynow.ui.theme.LocalClarityColors
import com.kamsiob.claritynow.ui.theme.ShadowLayer
import com.kamsiob.claritynow.ui.theme.WashCorner
import com.kamsiob.claritynow.ui.theme.calmAccent
import com.kamsiob.claritynow.ui.theme.washCornerFor
import kotlin.math.hypot

/**
 * design-v3.md 6.1. One separation device per element, expressed here as a pair of
 * shadows that read as a single soft lift rather than as two effects.
 *
 * In the dark and Contemplative worlds this draws nothing at all. There, depth is
 * a background lightness shift and a shadow on top of it would be the second
 * device the rule forbids.
 */
fun Modifier.clarityShadow(layers: List<ShadowLayer>, shape: Shape, enabled: Boolean = true): Modifier {
    if (!enabled) return this
    // Applied widest first so the tight contact shadow lands on top of the diffuse one.
    return layers.sortedByDescending { it.blur.value }.fold(this) { modifier, layer ->
        modifier.dropShadow(
            shape = shape,
            shadow = Shadow(
                radius = layer.blur,
                color = layer.color,
                offset = DpOffset(0.dp, layer.offsetY),
            ),
        )
    }
}

/**
 * The area accent wash, pooled toward a corner chosen by hashing the area id.
 *
 * A wash that always pooled to the same corner would read as a template. Hashing
 * the id varies it across a screen of cards and keeps it fixed for the life of an
 * area, so a card never appears to move.
 *
 * Takes the accent it is given and does not transform it. [areaWash] is the only
 * caller and is where calm mode's transform is applied, so that the choke point is one
 * function rather than two.
 */
internal fun washBrush(accent: Color, alpha: Float, corner: WashCorner, width: Float, height: Float): Brush {
    val center = when (corner) {
        WashCorner.TOP_START -> Offset(0f, 0f)
        WashCorner.TOP_END -> Offset(width, 0f)
        WashCorner.BOTTOM_START -> Offset(0f, height)
        WashCorner.BOTTOM_END -> Offset(width, height)
    }
    return Brush.radialGradient(
        colors = listOf(accent.copy(alpha = alpha), accent.copy(alpha = 0f)),
        center = center,
        radius = hypot(width, height) * 0.92f,
    )
}

/**
 * The one place an area accent becomes atmosphere, and therefore the one place calm
 * mode's color transform is applied. design-v3.md 16.2.
 *
 * Every wash in the app arrives here: the area card, the color picker's live preview,
 * and anything later that pools an accent behind content. The transform is applied
 * inside rather than at the call sites, so a screen cannot forget it and a new screen
 * inherits it without being told. The two excluded uses of the accent, the 7dp dot and
 * the area label text, never come through this function at all, which is what keeps the
 * exclusion structural rather than a rule somebody has to remember.
 *
 * The alpha is not touched here. 16.2 pins the wash opacity to the low end of its range
 * in calm mode, and that pinning belongs to the token set, in `ClarityColors.calmed`,
 * because the opacity is a design token while the accent is the user's own color.
 *
 * `composed` rather than a `@Composable` extension so the signature does not change: the
 * two call sites in `ui/areas` are outside this phase's scope, and a wash that quietly
 * started honoring calm mode without them being edited is the correct outcome.
 */
fun Modifier.areaWash(accent: Color, alpha: Float, areaId: String): Modifier = composed {
    if (alpha <= 0f) {
        this
    } else {
        val calm = calmAccent(accent)
        val corner = washCornerFor(areaId)
        drawBehind {
            drawRect(washBrush(calm, alpha, corner, size.width, size.height))
        }
    }
}

/**
 * A content card. design-v3.md 10.3.
 *
 * There is no border parameter, and there never will be one. A card carries a
 * shadow in the light world and a lightness step in the dark world, and nothing
 * carries both.
 *
 * The default background is `card`, the top rank of the phase 3c surface ladder, and
 * a content card is what that rank is for. Chrome sits at `raise` one step below it.
 */
@Composable
fun ClarityCard(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(18.dp),
    colors: ClarityColors = LocalClarityColors.current,
    background: Color = colors.card,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .clarityShadow(
                layers = com.kamsiob.claritynow.ui.theme.ClarityElevation.card,
                shape = shape,
                enabled = !colors.isDark,
            )
            .clip(shape)
            .background(background),
        content = content,
    )
}
