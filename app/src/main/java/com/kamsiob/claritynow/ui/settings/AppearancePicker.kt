package com.kamsiob.claritynow.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kamsiob.claritynow.ui.components.ClarityIcon
import com.kamsiob.claritynow.ui.components.ClarityIcons
import com.kamsiob.claritynow.ui.theme.AreaPalette
import com.kamsiob.claritynow.ui.theme.ClarityColors
import com.kamsiob.claritynow.ui.theme.ClarityDarkColors
import com.kamsiob.claritynow.ui.theme.ClarityHapticEvent
import com.kamsiob.claritynow.ui.theme.ClarityLightColors
import com.kamsiob.claritynow.ui.theme.ClarityThemeSetting
import com.kamsiob.claritynow.ui.theme.LocalClarityColors
import com.kamsiob.claritynow.ui.theme.LocalClarityShapes
import com.kamsiob.claritynow.ui.theme.LocalClarityTypography
import com.kamsiob.claritynow.ui.components.clarityClickable
import com.kamsiob.claritynow.ui.theme.parseAreaColor
import kotlin.math.cos
import kotlin.math.sin

/**
 * The appearance picker, design-v3.md 10.10.
 *
 * Three tiles, each a **real miniature** of the Areas screen rather than three grey
 * bars: the canvas color as the tile background, a title bar at the ink opacity, and
 * three miniature area rows at the card color, each carrying a real 4dp area dot in
 * Ocean, Meadow and Earth. The System tile splits diagonally at 103 degrees and shows
 * both halves including the rows.
 *
 * **The tokens are read from `ClarityLightColors` and `ClarityDarkColors` rather than
 * from the hex values 10.10 writes down, and that difference is deliberate.** 10.10
 * names `#F1F1F6` on `#FFFFFF` and `#0E0E13` on `#191921`; three of those four moved in
 * phase 3c, with the reasoning recorded on the tokens themselves in `ClarityColors.kt`,
 * and the light card in particular moved because design-v3.md 1 and 14 both forbid a
 * pure white background while 3.1 specified one. A miniature drawn from the numbers in
 * 10.10 would be a picture of a screen this app no longer has, which defeats the one
 * thing the word "real" in that section is doing. So the tiles read the tokens, and a
 * later token change reaches them without anybody remembering this file.
 */
private val TILE_HEIGHT = 84.dp
private val TILE_GAP = 9.dp
private val TILE_PADDING = 8.dp
private val MINI_TITLE_HEIGHT = 5.dp
private val MINI_ROW_HEIGHT = 14.dp
private val MINI_ROW_GAP = 4.dp
private val MINI_DOT = 4.dp
private val SELECTED_RING = 2.dp
private val CHECK_BADGE = 14.dp

/** The split on the System tile, design-v3.md 10.10. */
private const val SYSTEM_SPLIT_DEGREES = 103f

/**
 * The three dot colors, design-v3.md 10.10, taken from the mood groups it names in
 * 3.4 rather than written out as hex here, so the picker and the color picker cannot
 * drift apart.
 *
 * These are not area accents and take no calm mode transform: design-v3.md 16.2
 * excludes the area dot from the transform by name, because a dot is identity rather
 * than atmosphere, and a miniature of a dot is a picture of the same thing.
 */
private val MINI_DOT_COLORS: List<Color> = listOf("Ocean", "Meadow", "Earth").map { mood ->
    parseAreaColor(AreaPalette.moods.first { it.name == mood }.colors.first())
}

@Composable
internal fun AppearancePicker(
    selected: ClarityThemeSetting,
    onSelect: (ClarityThemeSetting) -> Unit,
    modifier: Modifier = Modifier,
    lightLabel: String,
    darkLabel: String,
    systemLabel: String,
) {
    Row(
        modifier = modifier.fillMaxWidth().selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(TILE_GAP),
    ) {
        AppearanceTile(
            label = lightLabel,
            selected = selected == ClarityThemeSetting.LIGHT,
            onSelect = { onSelect(ClarityThemeSetting.LIGHT) },
            modifier = Modifier.weight(1f),
        ) { MiniAreas(ClarityLightColors) }

        AppearanceTile(
            label = darkLabel,
            selected = selected == ClarityThemeSetting.DARK,
            onSelect = { onSelect(ClarityThemeSetting.DARK) },
            modifier = Modifier.weight(1f),
        ) { MiniAreas(ClarityDarkColors) }

        AppearanceTile(
            label = systemLabel,
            selected = selected == ClarityThemeSetting.SYSTEM,
            onSelect = { onSelect(ClarityThemeSetting.SYSTEM) },
            modifier = Modifier.weight(1f),
        ) {
            // Both halves are drawn at full tile size and the upper one is clipped to
            // a half plane, so the dark side carries its own title bar and its own
            // three rows rather than half of the light side's. 10.10 asks for both
            // halves "including the rows" and that is the only way to get them.
            MiniAreas(ClarityLightColors)
            Box(modifier = Modifier.fillMaxSize().clip(DiagonalHalfShape(SYSTEM_SPLIT_DEGREES))) {
                MiniAreas(ClarityDarkColors)
            }
        }
    }
}

@Composable
private fun AppearanceTile(
    label: String,
    selected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val colors = LocalClarityColors.current
    val type = LocalClarityTypography.current
    val shapes = LocalClarityShapes.current
    Column(
        modifier = modifier
            .clarityClickable(
                haptic = ClarityHapticEvent.SELECT,
                role = Role.RadioButton,
                onClickLabel = label,
                onClick = onSelect,
            )
            .semantics { this.selected = selected },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(TILE_HEIGHT)
                .clip(shapes.appearanceTile)
                .then(
                    if (selected) {
                        Modifier.border(SELECTED_RING, colors.actionBlue, shapes.appearanceTile)
                    } else {
                        Modifier
                    },
                ),
            content = {
                content()
                if (selected) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(5.dp)
                            .size(CHECK_BADGE)
                            .clip(CircleShape)
                            .background(colors.actionBlue),
                        contentAlignment = Alignment.Center,
                    ) {
                        ClarityIcon(
                            icon = ClarityIcons.check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(9.dp),
                        )
                    }
                }
            },
        )
        Spacer(Modifier.height(7.dp))
        Text(
            text = label,
            style = type.caption.copy(
                fontSize = 9.5.sp,
                fontWeight = if (selected) FontWeight(700) else FontWeight(400),
            ),
            color = if (selected) colors.actionBlue else colors.inkSecondary,
            textAlign = TextAlign.Center,
        )
    }
}

/** One miniature Areas screen, drawn from a real token set. */
@Composable
private fun MiniAreas(world: ClarityColors) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(world.canvas)
            .padding(TILE_PADDING),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.42f)
                .height(MINI_TITLE_HEIGHT)
                .clip(RoundedCornerShape(2.dp))
                .background(world.inkPrimary),
        )
        Spacer(Modifier.height(8.dp))
        MINI_DOT_COLORS.forEachIndexed { index, dot ->
            if (index > 0) Spacer(Modifier.height(MINI_ROW_GAP))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(MINI_ROW_HEIGHT)
                    .clip(RoundedCornerShape(4.dp))
                    .background(world.card),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Spacer(Modifier.width(5.dp))
                Box(
                    modifier = Modifier
                        .size(MINI_DOT)
                        .clip(CircleShape)
                        .background(dot),
                )
            }
        }
    }
}

/**
 * The trailing half of a rectangle, cut by a line through its center at [degrees].
 *
 * The angle is read the way it is written on paper, counterclockwise from the
 * positive x axis, so 103 degrees leans thirteen degrees off vertical. Screen
 * coordinates grow downward, which is why the y component of the direction is
 * negated here rather than in the caller: an angle in a design document is not a
 * quantity in a canvas.
 *
 * A data class so that two identical shapes compare equal and the clip does not
 * invalidate on every recomposition.
 */
private data class DiagonalHalfShape(val degrees: Float) : Shape {

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val radians = Math.toRadians(degrees.toDouble())
        val dirX = cos(radians).toFloat()
        val dirY = -sin(radians).toFloat()
        // The normal, rotated a quarter turn from the direction, points into the half
        // this shape keeps. With 103 degrees that is the trailing side.
        val normalX = -dirY
        val normalY = dirX
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        // Longer than any diagonal of the rectangle, so the polygon always covers it.
        val reach = size.width + size.height
        val startX = centerX - dirX * reach
        val startY = centerY - dirY * reach
        val endX = centerX + dirX * reach
        val endY = centerY + dirY * reach
        val path = Path().apply {
            moveTo(startX, startY)
            lineTo(endX, endY)
            lineTo(endX + normalX * reach, endY + normalY * reach)
            lineTo(startX + normalX * reach, startY + normalY * reach)
            close()
        }
        return Outline.Generic(path)
    }
}
