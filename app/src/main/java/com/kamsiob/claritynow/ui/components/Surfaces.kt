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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.kamsiob.claritynow.ui.theme.ClarityColors
import com.kamsiob.claritynow.ui.theme.LocalClarityColors
import com.kamsiob.claritynow.ui.theme.ShadowLayer
import com.kamsiob.claritynow.ui.theme.calmAccent

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
/**
 * **A flat tint, not a gradient, and that is the single most visible change in the
 * refresh.**
 *
 * The shipping wash was a radial gradient running from the accent at full alpha in one
 * of the card's four corners to nothing over `hypot(w, h) * 0.92`. Two things were wrong
 * with it.
 *
 * It was **the tell**. A radial color wash on a card is the most common thing a generated
 * interface does, and design-v3.md 15.1 is a list of exactly these; the wash was on the
 * list by construction and had been shipped anyway.
 *
 * It also **cost the card its rank**. Because the gradient reaches zero at 92 percent of
 * the diagonal, most of a card's area carried a fraction of the stated alpha, and the
 * corner that carried the peak was 4 to 6 L* under the card's own value, which put the
 * card within two points of the chrome around it. The card was tinted and darker and
 * read as neither.
 *
 * A flat tint is a paper stock. It is one value across the whole card, so the card holds
 * its place on the ladder, and it never reads as an effect because there is nothing for
 * the eye to trace. design-v3.md 3.4 is unchanged and still binding: area color is a dot
 * and a word, never a stripe, a bar, an edge or a filled block, and a tint at 5 percent
 * is a surface rather than a block of color.
 */
fun Modifier.areaTint(accent: Color, alpha: Float): Modifier = composed {
    if (alpha <= 0f) {
        this
    } else {
        val tint = calmAccent(accent).copy(alpha = alpha)
        drawBehind { drawRect(tint) }
    }
}

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
