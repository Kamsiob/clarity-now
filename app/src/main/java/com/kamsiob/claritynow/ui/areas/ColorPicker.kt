package com.kamsiob.claritynow.ui.areas

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kamsiob.claritynow.R
import com.kamsiob.claritynow.ui.components.ClarityCard
import com.kamsiob.claritynow.ui.components.ClarityIcon
import com.kamsiob.claritynow.ui.components.ClarityIcons
import com.kamsiob.claritynow.ui.components.Sidehead
import com.kamsiob.claritynow.ui.components.areaTint
import com.kamsiob.claritynow.ui.components.clarityClickable
import com.kamsiob.claritynow.ui.theme.AreaMood
import com.kamsiob.claritynow.ui.theme.AreaPalette
import com.kamsiob.claritynow.ui.theme.ClarityHapticEvent
import com.kamsiob.claritynow.ui.theme.ClaritySpacing
import com.kamsiob.claritynow.ui.theme.LocalClarityColors
import com.kamsiob.claritynow.ui.theme.LocalClarityTypography
import com.kamsiob.claritynow.ui.theme.areaLabelColor
import com.kamsiob.claritynow.ui.theme.clarityMotion
import com.kamsiob.claritynow.ui.theme.parseAreaColor
import com.kamsiob.claritynow.ui.theme.swatchCheckColor
import kotlinx.coroutines.delay

/**
 * design-v3.md 10.9. The two stage area color picker.
 *
 * The live preview above both stages is the single permitted exception to the no
 * cards inside sheets rule, because the entire purpose of that element is to show
 * the person what their card will look like. It is a rendering of a component
 * rather than a container wrapping content, and the exception may not be extended
 * by analogy to anything else.
 */
@Composable
fun AreaColorPicker(
    areaName: String,
    selectedHex: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    previewItemTitle: String? = null,
) {
    var mood by remember(selectedHex) {
        mutableStateOf(AreaPalette.moodOf(selectedHex) ?: AreaPalette.moods.first())
    }

    Column(modifier = modifier) {
        Sidehead(
            text = stringResource(R.string.sidehead_preview),
            modifier = Modifier.padding(horizontal = 20.dp),
        )
        Spacer(Modifier.height(ClaritySpacing.scaled(12.dp)))
        ColorPreviewCard(
            areaName = areaName,
            hex = selectedHex,
            itemTitle = previewItemTitle,
            modifier = Modifier.padding(horizontal = 20.dp),
        )

        Spacer(Modifier.height(ClaritySpacing.scaled(24.dp)))
        Sidehead(
            text = stringResource(R.string.sidehead_mood),
            modifier = Modifier.padding(horizontal = 20.dp),
        )
        Spacer(Modifier.height(ClaritySpacing.scaled(12.dp)))
        MoodStrip(
            selected = mood,
            // The previously selected color is kept when it belongs to the newly
            // selected mood; otherwise the grid simply shows no selection until the
            // person taps, rather than guessing on their behalf.
            onSelect = { picked -> mood = picked },
        )

        Spacer(Modifier.height(ClaritySpacing.scaled(22.dp)))
        Sidehead(
            text = stringResource(R.string.sidehead_color),
            modifier = Modifier.padding(horizontal = 20.dp),
        )
        Spacer(Modifier.height(ClaritySpacing.scaled(12.dp)))
        SwatchGrid(
            mood = mood,
            selectedHex = selectedHex,
            onSelect = onSelect,
            modifier = Modifier.padding(horizontal = 20.dp),
        )
        Spacer(Modifier.height(ClaritySpacing.scaled(8.dp)))
    }
}

/** An actual miniature area card, updating instantly on every tap. */
@Composable
private fun ColorPreviewCard(
    areaName: String,
    hex: String,
    itemTitle: String?,
    modifier: Modifier = Modifier,
) {
    val colors = LocalClarityColors.current
    val type = LocalClarityTypography.current
    val accent = parseAreaColor(hex)
    val motion = clarityMotion()

    ClarityCard(modifier = modifier.fillMaxWidth(), colors = colors) {
        Crossfade(
            targetState = hex,
            animationSpec = motion.easeOut(),
            label = "previewWash",
        ) { current ->
            val currentAccent = parseAreaColor(current)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .areaTint(currentAccent, colors.cardWashAlpha)
                    .padding(horizontal = 18.dp, vertical = ClaritySpacing.scaled(17.dp)),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(currentAccent),
                    )
                    Text(
                        text = areaName.ifBlank { stringResource(R.string.sheet_new_area_title) },
                        style = type.label,
                        color = areaLabelColor(currentAccent, colors),
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
                // The idle line, drawn exactly as `AreaCard.kt` draws it, because the
                // sentence above this function says this is an actual miniature area
                // card. design-v3.md 10.3 already settled this string: the weight
                // drops from 650 to 500 and the color stays on a rank a person can
                // read. `inkTertiary` measures 2.402 to one on this card against
                // section 13's floor of 4.5, and here it was the **only** thing
                // telling the idle state from a real title, so the distinction moves
                // onto the weight rather than being dropped.
                Text(
                    text = itemTitle ?: stringResource(R.string.area_idle_title),
                    style = if (itemTitle == null) {
                        type.itemTitle.copy(fontWeight = FontWeight(400))
                    } else {
                        type.itemTitle
                    },
                    color = if (itemTitle == null) colors.inkSecondary else colors.inkPrimary,
                    modifier = Modifier.padding(top = ClaritySpacing.scaled(7.dp)),
                )
            }
        }
    }
}

/**
 * Stage one. Eight mood pills, each divided into six discrete vertical slivers
 * showing that mood's colors. Never a gradient, because a gradient would imply the
 * colors between the six exist.
 */
@Composable
private fun MoodStrip(selected: AreaMood, onSelect: (AreaMood) -> Unit) {
    val colors = LocalClarityColors.current
    val motion = clarityMotion()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        AreaPalette.moods.forEach { mood ->
            val isSelected = mood.name == selected.name
            val interaction = remember(mood.name) { MutableInteractionSource() }
            val pressed by interaction.collectIsPressedAsState()
            // **The ring thickens, issue #67.** A mood pill is six slivers of six
            // different colors, so no single ink drawn on it reads on all of them and the
            // 6 percent ground is as invisible here as it is on a swatch. What the pill
            // already owns is a ring in the sheet's own ink, on the sheet's own ground,
            // where `inkPrimary` is the verified reading. So a press brings it in at
            // 1.5dp and a selected pill's 2dp ring grows to 3.5, which is a change on
            // every pill in every state rather than only on the seven that are not
            // selected. A swatch answers with a ring too and for the same reason, in the
            // one ink measured against that swatch; the two controls speak one language
            // in the two forms each of them owns.
            val ringWidth by animateDpAsState(
                targetValue = (if (isSelected) 2.dp else 0.dp) + (if (pressed) 1.5.dp else 0.dp),
                animationSpec = motion.springSnappy(),
                label = "moodRing",
            )
            val lift by animateFloatAsState(
                targetValue = if (isSelected) 1.05f else 1f,
                animationSpec = motion.springSnappy(),
                label = "moodLift",
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clarityClickable(
                    interactionSource = interaction,
                    haptic = ClarityHapticEvent.TAP,
                    onClickLabel = mood.name,
                    // The ring above is this control's press, and rule 11 gives an
                    // element one separation device and one answer to a thumb.
                    showPress = false,
                ) { onSelect(mood) },
            ) {
                Box(
                    modifier = Modifier
                        .scale(lift)
                        .padding(1.5.dp)
                        .then(
                            // A zero width border still lays down a hairline, so the
                            // ring is attached only while there is a ring to draw.
                            if (isSelected || pressed) {
                                Modifier.border(ringWidth, colors.inkPrimary, RoundedCornerShape(9.5.dp))
                            } else {
                                Modifier
                            },
                        )
                        .padding(1.5.dp)
                        .size(width = 46.dp, height = 26.dp)
                        .clip(RoundedCornerShape(8.dp)),
                ) {
                    Row(Modifier.fillMaxWidth()) {
                        mood.colors.forEach { hex ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(26.dp)
                                    .background(parseAreaColor(hex)),
                            )
                        }
                    }
                }
                Spacer(Modifier.height(ClaritySpacing.scaled(6.dp)))
                Text(
                    text = mood.name,
                    fontSize = 12.5.sp,
                    fontWeight = if (isSelected) FontWeight(700) else FontWeight(400),
                    // `inkSecondary` rather than the `inkTertiary` design-v3.md 10.9
                    // named, which measures 2.402 to one on this sheet against section
                    // 13's floor of 4.5. Nothing is lost: the weight above is what
                    // says which mood is selected, and it says it at 700 against 400
                    // whether or not the unselected names are also too faint to read.
                    // 10.9 is corrected rather than left standing beside 3.1.
                    color = if (isSelected) colors.inkPrimary else colors.inkSecondary,
                    style = LocalClarityTypography.current.caption.copy(fontSize = 12.5.sp),
                )
            }
        }
    }
}

/** Stage two. Six swatches in a three by two grid with a staggered arrival. */
@Composable
private fun SwatchGrid(
    mood: AreaMood,
    selectedHex: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val motion = clarityMotion()
    var revealed by remember(mood.name) { mutableStateOf(0) }

    LaunchedEffect(mood.name) {
        revealed = 0
        mood.colors.indices.forEach { index ->
            revealed = index + 1
            if (!motion.reduced) delay(40)
        }
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(ClaritySpacing.scaled(12.dp)),
    ) {
        mood.colors.chunked(3).forEachIndexed { rowIndex, row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEachIndexed { columnIndex, hex ->
                    val index = rowIndex * 3 + columnIndex
                    AnimatedVisibility(
                        visible = index < revealed,
                        enter = fadeIn(motion.easeOut()) + scaleIn(motion.springSnappy(), initialScale = 0.86f),
                        modifier = Modifier.weight(1f),
                    ) {
                        Swatch(
                            hex = hex,
                            selected = hex.equals(selectedHex, ignoreCase = true),
                            onClick = { onSelect(hex) },
                        )
                    }
                }
                // Keeps a short final row aligned with the one above it.
                repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

/**
 * One color. Issue #67, and the one control in the app whose press cannot be a veil.
 *
 * `clarityClickable`'s 6 percent ink ground is the app's press treatment everywhere else
 * and it is invisible here, because the thing being pressed **is** a color: 6 percent of
 * near black over `#22C55E` is a shade of green a person cannot see under their own thumb.
 * So this control opts out of it and answers with a ring instead.
 *
 * ## The ring is drawn inside, in the swatch's own contrast ink
 *
 * `swatchCheckColor` picks white or ink by measurement against this exact color, and the
 * worst of the 48 reads 4.23 to one, so a ring in that ink is visible on every swatch in
 * the palette by construction rather than by inspection. It is drawn **inside** the
 * swatch, because the gutter outside it already belongs to the selection ring and two
 * rings in one gutter is one ring nobody can read.
 *
 * ## Why not the obvious answer, `design-v3.md` 15
 *
 * The obvious answer is a press scale, which is what `clarityPressScale` exists for and
 * what a button gets. On a tile in a grid of six it reads as a wobble: the neighbors do
 * not move, so the pressed one appears to come loose. Scale also collides with what this
 * control already says with size, since a **selected** swatch stands at 1.06. A ring
 * cannot collide with either. Nothing in the grid moves, the tile keeps its footprint,
 * and the press is legible next to a selection rather than confusable with it.
 *
 * ## Calm mode and reduce motion, 8.3
 *
 * The ring fades rather than travels, so 8.3's rule that every animation becomes a 150ms
 * crossfade leaves it doing exactly what it already does. This is the reason the shape
 * morph the FAB uses was not the answer here: 8.3 gates a spatial change off entirely,
 * which would leave the swatches with no press feedback for the people who asked for less
 * motion, which is the defect this fixes rather than a version of it.
 */
@Composable
private fun Swatch(hex: String, selected: Boolean, onClick: () -> Unit) {
    val motion = clarityMotion()
    val accent = parseAreaColor(hex)
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.06f else 1f,
        animationSpec = motion.springSnappy(),
        label = "swatchScale",
    )
    val ring by animateDpAsState(
        targetValue = if (selected) 2.5.dp else 0.dp,
        animationSpec = motion.springSnappy(),
        label = "swatchRing",
    )
    // Alpha rather than width, so a press is a crossfade under 8.3 and not a movement.
    val press by animateColorAsState(
        targetValue = if (pressed) {
            swatchCheckColor(accent).copy(alpha = PRESS_RING_ALPHA)
        } else {
            Color.Transparent
        },
        animationSpec = motion.effectsFast(),
        label = "swatchPress",
    )

    Box(
        modifier = Modifier
            .scale(scale)
            .padding(3.dp)
            .then(
                if (selected) {
                    Modifier.border(ring, accent.copy(alpha = 0.5f), RoundedCornerShape(19.dp))
                } else {
                    Modifier
                },
            )
            .padding(3.dp)
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp))
            .background(accent)
            .border(PRESS_RING_WIDTH, press, RoundedCornerShape(16.dp))
            .clarityClickable(
                interactionSource = interaction,
                haptic = ClarityHapticEvent.TAP,
                onClickLabel = hex,
                // The one control in the app that opts out of the ink ground, and the
                // ring above is what it opts out in favor of. See the note on this
                // function: 6 percent of anything over a saturated fill is not a press.
                showPress = false,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        AnimatedVisibility(
            visible = selected,
            enter = fadeIn(motion.effects()) + scaleIn(motion.springSnappy(), initialScale = 0.6f),
        ) {
            ClarityIcon(
                icon = ClarityIcons.check,
                contentDescription = null,
                // design-v3.md 10.9 asked for a white check and it fails on 17 of the 48
                // swatches, worst at 1.67 to one. swatchCheckColor picks the ink that
                // reads on this swatch; the worst of the 48 is then 4.23.
                tint = swatchCheckColor(accent),
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

/**
 * The press ring on a swatch. `design-v3.md` 8.2 sets the app's press ground at 6 percent
 * and this is the one control that cannot use it, so the value is named here rather than
 * pretending to belong to that ladder.
 *
 * 0.85 rather than full, so the ring reads as a touch rather than as a second selection
 * mark, and 2dp because 1dp on a 16dp corner reads as an artifact of the clip.
 */
private const val PRESS_RING_ALPHA = 0.85f

private val PRESS_RING_WIDTH = 2.dp
