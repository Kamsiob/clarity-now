package com.kamsiob.claritynow.ui.areas

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
                        type.itemTitle.copy(fontWeight = FontWeight(500))
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
            val ringWidth by animateDpAsState(
                targetValue = if (isSelected) 2.dp else 0.dp,
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
                    haptic = ClarityHapticEvent.TAP,
                    onClickLabel = mood.name,
                ) { onSelect(mood) },
            ) {
                Box(
                    modifier = Modifier
                        .scale(lift)
                        .padding(1.5.dp)
                        .then(
                            // A zero width border still lays down a hairline, so the
                            // ring is attached only while this mood is the selected one.
                            if (isSelected) {
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
                    fontSize = 10.sp,
                    fontWeight = if (isSelected) FontWeight(700) else FontWeight(400),
                    // `inkSecondary` rather than the `inkTertiary` design-v3.md 10.9
                    // named, which measures 2.402 to one on this sheet against section
                    // 13's floor of 4.5. Nothing is lost: the weight above is what
                    // says which mood is selected, and it says it at 700 against 400
                    // whether or not the unselected names are also too faint to read.
                    // 10.9 is corrected rather than left standing beside 3.1.
                    color = if (isSelected) colors.inkPrimary else colors.inkSecondary,
                    style = LocalClarityTypography.current.caption.copy(fontSize = 10.sp),
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

@Composable
private fun Swatch(hex: String, selected: Boolean, onClick: () -> Unit) {
    val motion = clarityMotion()
    val accent = parseAreaColor(hex)
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
            .clarityClickable(haptic = ClarityHapticEvent.TAP, onClickLabel = hex, onClick = onClick),
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
