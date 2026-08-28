package com.kamsiob.claritynow.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kamsiob.claritynow.R
import com.kamsiob.claritynow.ui.theme.ClaritySpacing
import com.kamsiob.claritynow.ui.theme.ClarityTextSize
import com.kamsiob.claritynow.ui.theme.LocalClarityColors
import com.kamsiob.claritynow.ui.theme.LocalClarityTypography
import com.kamsiob.claritynow.ui.theme.LocalSystemFontScale
import com.kamsiob.claritynow.ui.theme.isClampedByCeiling

/**
 * The text size control, design-v3.md 13 and Addendum 01 8f.
 *
 * ## The open choice, design-v3.md 15
 *
 * **The statistically common answer is a slider with a live preview paragraph**, and it
 * loses here on three counts.
 *
 * A slider is a continuous control for a quantity that is not continuous. The platform
 * offers font size in discrete steps, `ClarityTextSize` takes those same steps, and
 * design-v3.md 5.3 is a ladder of five sans sizes rather than a curve. A control that
 * slides suggests values between the steps that nothing can render, and it asks for a
 * drag, which is the one gesture this audience is least able to place precisely.
 *
 * A preview paragraph is a preview of a paragraph. The half of this feature that matters
 * is the half 8f warns about, that spacing has to move with the type, and a specimen in
 * a fixed box is the one thing that cannot show it. It also shows the choice in a place
 * the choice does not live: a person is deciding whether they can read *this app*, and a
 * sample of lorem prose in a bordered rectangle is not this app.
 *
 * **So the control is five named steps and the screen it sits on is the preview.**
 * Settings is real content at real sizes: sideheads, row titles, captions, a paragraph
 * of explanation, switches and a card. Every one of those re-lays out on the tap, at the
 * chosen size, with the spacing that comes with it, while the person is looking at it.
 * The preview is free, it is honest, and it is larger and more varied than any specimen
 * would have been.
 *
 * ## Every option label renders at the current size, not at its own
 *
 * The tempting version of this control sets each row in the size it offers, so the
 * ladder is visible at a glance. It is the wrong control for the person it is for: the
 * `Small` and `Default` rows would then be set below the size somebody has already told
 * this app they cannot read, which makes the affordance for a size setting fail exactly
 * the need the size setting exists to serve. The rows are all set at the current size,
 * ordered, and named in plain words; the size itself is shown by the whole screen.
 *
 * ## What happens at the ceiling
 *
 * The combined scale is capped at 200 percent, `MAX_COMBINED_FONT_SCALE`,
 * so a phone already at or near its own maximum has little or no headroom left here. The
 * rows above the ceiling stay tappable and the choice is still stored, because a stored
 * choice becomes true again the moment the phone's setting comes down, and because
 * design-v3.md 10.16's rule about disabled controls applies: a control greyed out is a
 * question a person then has to answer. What appears instead is one line saying the
 * phone's own setting is what is deciding, which is the fact they need and the only
 * thing the app can honestly say.
 */
@Composable
internal fun TextSizePicker(
    selected: ClarityTextSize,
    onSelect: (ClarityTextSize) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalClarityColors.current
    val type = LocalClarityTypography.current
    val systemFontScale = LocalSystemFontScale.current

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            // The same treatment a settings row title takes, so this reads as one more
            // thing in the Appearance group rather than as a second sidehead inside it.
            text = stringResource(R.string.settings_text_size),
            style = type.body.copy(fontWeight = FontWeight(600)),
            color = colors.inkPrimary,
        )
        Spacer(Modifier.height(ClaritySpacing.scaled(TITLE_GAP)))
        Column(modifier = Modifier.fillMaxWidth().selectableGroup()) {
            // Ascending, which is the order the words are already in, so the reading
            // order a screen reader announces and the order the sizes run are the same
            // list. Nothing here reflows into a second axis at any size, so that order
            // is the same at 85 percent and at the cap. design-v3.md 13.
            ClarityTextSize.entries.forEach { step ->
                SettingsChoiceRow(
                    label = stringResource(labelOf(step)),
                    selected = step == selected,
                    horizontalInset = 0.dp,
                    onClick = { onSelect(step) },
                )
            }
        }
        Spacer(Modifier.height(ClaritySpacing.scaled(NOTE_GAP)))
        Text(
            // `inkSecondary`, not `inkTertiary`, and on this control of all controls.
            // design-v3.md 3.1 says inkTertiary carries no text anywhere in this app and
            // 13 measures it at 2.337 to one on the light canvas, well under the 4.5
            // floor; phase 12b moved a field placeholder off it for the same reason. A
            // line explaining a text size setting, set in the one ink that fails the
            // contrast rule, would be the joke this feature cannot afford.
            text = stringResource(R.string.settings_text_size_note),
            style = type.caption,
            color = colors.inkSecondary,
        )
        if (isClampedByCeiling(systemFontScale, selected)) {
            Spacer(Modifier.height(ClaritySpacing.scaled(NOTE_GAP)))
            Text(
                text = stringResource(R.string.settings_text_size_at_ceiling),
                style = type.caption,
                color = colors.inkSecondary,
            )
        }
    }
}

/**
 * The five names, in `strings.xml` and not in a corpus.
 *
 * MASTER_BUILD_PROMPT 14b.11 and design-v3.md 13.1 draw the line at whether a sentence
 * is an observation about the person's own data. A size setting has no data behind it
 * and says nothing about anybody, so these are fixed interface labels, which is where
 * `strings.xml` is correct rather than a second path around the engine.
 */
private fun labelOf(step: ClarityTextSize): Int = when (step) {
    ClarityTextSize.SMALL -> R.string.settings_text_size_small
    ClarityTextSize.DEFAULT -> R.string.settings_text_size_default
    ClarityTextSize.LARGE -> R.string.settings_text_size_large
    ClarityTextSize.LARGER -> R.string.settings_text_size_larger
    ClarityTextSize.LARGEST -> R.string.settings_text_size_largest
}

private val TITLE_GAP = 4.dp
private val NOTE_GAP = 10.dp
