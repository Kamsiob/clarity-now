package com.kamsiob.claritynow.ui.onboarding

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kamsiob.claritynow.R
import com.kamsiob.claritynow.ui.components.ClarityIcon
import com.kamsiob.claritynow.ui.components.ClarityIcons
import com.kamsiob.claritynow.ui.components.clarityClickable
import com.kamsiob.claritynow.ui.theme.AreaPalette
import com.kamsiob.claritynow.ui.theme.ClarityHapticEvent
import com.kamsiob.claritynow.ui.theme.ClaritySpacing
import com.kamsiob.claritynow.ui.theme.LocalClarityShapes
import com.kamsiob.claritynow.ui.theme.LocalClarityTypography
import com.kamsiob.claritynow.ui.theme.LocalContemplativeColors
import com.kamsiob.claritynow.ui.theme.clarityMotion
import com.kamsiob.claritynow.ui.theme.parseAreaColor
import com.kamsiob.claritynow.ui.theme.swatchCheckColor

/**
 * The mood color rows beat 2 opens on a selection. design-v3.md 10.9, Contemplative.
 *
 * Both of 10.9's stages, compressed into the space one beat can spare: a scrolling strip
 * of eight mood pills, each six discrete slivers and never a gradient, and beneath it the
 * selected mood's six colors in one row. The live preview 10.9 puts above them is already
 * on this screen as the mini card the person tapped to open this, so it is not drawn
 * twice.
 *
 * **A single row of six rather than 10.9's three by two grid**, which is the one thing
 * here that departs from the section. The grid is right in a sheet that has the screen to
 * itself; inside a beat that also carries chips, a field and a list of selections, a
 * second block of rows pushes the Continue control off the bottom on a small phone. Six
 * squares in a row still clear the 48dp touch minimum in design-v3.md 13 at the narrowest
 * width the app supports.
 *
 * **The selected mood is derived from the current color rather than remembered beside
 * it**, so there is no second piece of state that can disagree with the swatch a person
 * is looking at. Switching mood keeps the color until they tap one, which is 10.9's rule
 * for the case where the previous color does not belong to the new mood.
 */
@Composable
internal fun OnboardingColorRows(
    selectedHex: String,
    onPick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val shapes = LocalClarityShapes.current
    var mood by remember(selectedHex) {
        mutableStateOf(AreaPalette.moodOf(selectedHex) ?: AreaPalette.moods.first())
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AreaPalette.moods.forEach { entry ->
                MoodPill(
                    name = entry.name,
                    colors = entry.colors,
                    selected = entry.name == mood.name,
                    onClick = { mood = entry },
                )
            }
        }

        Spacer(Modifier.height(ClaritySpacing.scaled(16.dp)))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(SWATCH_GAP),
        ) {
            mood.colors.forEach { hex ->
                Swatch(
                    hex = hex,
                    selected = hex.equals(selectedHex, ignoreCase = true),
                    shape = shapes.swatch,
                    onClick = { onPick(hex) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

/**
 * design-v3.md 10.9 stage one. A 46 by 26dp band at 8dp radius, six equal vertical
 * slivers, the name beneath at 10sp, and a 2dp ring at a 1.5dp offset when selected.
 *
 * The ring is drawn as a filled shape behind the band, [MOOD_RING_EXTENT] larger on every
 * side, which is 10.9's 1.5dp offset plus its 2dp ring. It is `textBright` rather than
 * `inkPrimary`, which is the same statement in the Contemplative world: the brightest ink
 * the surface has.
 */
@Composable
private fun MoodPill(
    name: String,
    colors: List<String>,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val contemplative = LocalContemplativeColors.current
    val type = LocalClarityTypography.current
    val shapes = LocalClarityShapes.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clarityClickable(
                haptic = ClarityHapticEvent.SELECT,
                role = Role.Button,
                onClickLabel = name,
                onClick = onClick,
            )
            .padding(vertical = ClaritySpacing.scaled(8.dp)),
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (selected) {
                Box(
                    modifier = Modifier
                        .size(
                            width = MOOD_PILL_WIDTH + MOOD_RING_EXTENT * 2,
                            height = MOOD_PILL_HEIGHT + MOOD_RING_EXTENT * 2,
                        )
                        .clip(shapes.moodPill)
                        .background(contemplative.textBright),
                )
            }
            Canvas(
                modifier = Modifier
                    .size(MOOD_PILL_WIDTH, MOOD_PILL_HEIGHT)
                    .clip(shapes.moodPill),
            ) {
                val sliver = size.width / colors.size
                colors.forEachIndexed { index, hex ->
                    drawRect(
                        color = parseAreaColor(hex),
                        topLeft = Offset(sliver * index, 0f),
                        size = Size(sliver, size.height),
                    )
                }
            }
        }
        Spacer(Modifier.height(ClaritySpacing.scaled(6.dp)))
        Text(
            text = name,
            style = type.caption.copy(
                fontSize = MOOD_NAME_SIZE,
                fontWeight = if (selected) FontWeight(700) else FontWeight(400),
            ),
            // `textDim` rather than `textFaint`, which measures 2.636 to one on
            // `deepBlack`. design-v3.md 13: Contemplative text stays at or above 55
            // percent opacity where it is meant to be read, and a mood's name is read.
            color = if (selected) contemplative.textBright else contemplative.textDim,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(MOOD_PILL_WIDTH + MOOD_RING_EXTENT * 2),
        )
    }
}

/**
 * design-v3.md 10.9 stage two. A square at 16dp radius that scales to 1.06 when selected
 * and carries a check at 20dp in whichever of white or ink reads on that swatch, 3.4.
 *
 * The 2.5dp ring 10.9 gives the selected swatch is drawn as a scale and a check here and
 * not as a ring, because a ring "in the swatch color at 50 percent" needs a ground darker
 * than the swatch to read against, and on `deepBlack` at these sizes it reads as a halo
 * rather than as a selection. The check is the unambiguous half of 10.9's own answer and
 * it is the half that survives at 48dp.
 */
@Composable
private fun Swatch(
    hex: String,
    selected: Boolean,
    shape: Shape,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val motion = clarityMotion()
    val scale by animateFloatAsState(
        targetValue = if (selected) SWATCH_SELECTED_SCALE else 1f,
        animationSpec = motion.springSnappy(),
        label = "onboardingSwatch",
    )

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .scale(scale)
            .clip(shape)
            .background(parseAreaColor(hex))
            .clarityClickable(
                haptic = ClarityHapticEvent.SELECT,
                role = Role.Button,
                onClickLabel = stringResource(R.string.cd_onboarding_swatch),
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (selected) {
            ClarityIcon(
                icon = ClarityIcons.check,
                contentDescription = null,
                // The same check as the Daylight picker's, and the same reason: white
                // fails on 17 of the 48 swatches, worst at 1.67 to one.
                tint = swatchCheckColor(parseAreaColor(hex)),
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

private val MOOD_PILL_WIDTH = 46.dp
private val MOOD_PILL_HEIGHT = 26.dp
private val MOOD_RING_EXTENT = 3.5.dp
private val MOOD_NAME_SIZE = 10.sp
private val SWATCH_GAP = 8.dp
private const val SWATCH_SELECTED_SCALE = 1.06f
