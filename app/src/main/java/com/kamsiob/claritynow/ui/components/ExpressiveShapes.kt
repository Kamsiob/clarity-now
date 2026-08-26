package com.kamsiob.claritynow.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.toPath
import com.kamsiob.claritynow.ui.theme.clarityMotion

/**
 * Shape morphing, the Material 3 Expressive treatment that turns a press from a
 * state change into a movement.
 *
 * Used sparingly and only where a shape already carries meaning: the FAB, the
 * completion mark, the color swatches. design-v3.md fixes every radius in the app,
 * so morphing is applied to round elements whose corner radius is already fully
 * round and therefore has nothing to contradict.
 */
class MorphShape(
    private val morph: Morph,
    private val progress: Float,
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        // RoundedPolygon lives in a two unit box centered on the origin, so the
        // path is scaled to half the target size and then pushed into view.
        val matrix = Matrix()
        matrix.scale(size.width / 2f, size.height / 2f)
        matrix.translate(1f, 1f)
        val path: Path = morph.toPath(progress = progress).asComposePath()
        path.transform(matrix)
        return Outline.Generic(path)
    }
}

@Composable
fun rememberMorph(start: RoundedPolygon, end: RoundedPolygon): Morph =
    remember(start, end) { Morph(start, end) }

/**
 * A shape that eases between [resting] and [pressed] as the element is touched.
 *
 * Returns the resting shape unchanged when the person has asked for reduced
 * motion, because a morph is movement and the global rule covers it.
 */
@Composable
fun morphingPressShape(
    interactionSource: MutableInteractionSource,
    resting: RoundedPolygon,
    pressed: RoundedPolygon,
    restingShape: Shape,
): Shape {
    val motion = clarityMotion()
    val isPressed by interactionSource.collectIsPressedAsState()
    val morph = rememberMorph(resting, pressed)
    val progress by animateFloatAsState(
        targetValue = if (isPressed) 1f else 0f,
        animationSpec = motion.springSnappy(),
        label = "shapeMorph",
    )
    if (motion.reduced) return restingShape
    return if (progress == 0f) restingShape else MorphShape(morph, progress)
}
