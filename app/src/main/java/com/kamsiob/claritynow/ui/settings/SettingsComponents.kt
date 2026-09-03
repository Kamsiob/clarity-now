package com.kamsiob.claritynow.ui.settings

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kamsiob.claritynow.ui.components.ClarityIcon
import com.kamsiob.claritynow.ui.components.ClarityIcons
import com.kamsiob.claritynow.ui.components.Sidehead
import com.kamsiob.claritynow.ui.components.clarityClickable
import com.kamsiob.claritynow.ui.theme.ClarityHapticEvent
import com.kamsiob.claritynow.ui.theme.ClaritySpacing
import com.kamsiob.claritynow.ui.theme.LocalClarityColors
import com.kamsiob.claritynow.ui.theme.LocalClarityHaptics
import com.kamsiob.claritynow.ui.theme.LocalClarityShapes
import com.kamsiob.claritynow.ui.theme.LocalClarityTypography
import com.kamsiob.claritynow.ui.theme.calmAccent
import com.kamsiob.claritynow.ui.theme.clarityMotion
import com.kamsiob.claritynow.ui.theme.opticallyCentered

/**
 * The Settings row's floor and its air, design-v3.md 10.11.
 *
 * **No card containers.** Rows sit directly on the canvas, grouped under sideheads, and
 * a row is a title at `readStrong` 18/600 on the 20dp measure, a caption under it where
 * one helps, a trailing value, and a chevron where it navigates.
 *
 * Air is this element's one separation device, per 6.1. Nothing here may also take a
 * shadow, a lightness step, a border or a hairline, and the reason the group is not a
 * card is that a card would be a second one.
 *
 * **This paragraph described a badge and a per-row hairline until the appeal pass**, both
 * of which `SettingsRowFrame` deletes and explains at length. Two orphaned blocks
 * describing the badge's geometry and its tint stood underneath it, attached to nothing,
 * documenting a component that had been removed; Kotlin binds only the block adjacent to
 * a declaration, so they were invisible to every reader but a person scrolling the file.
 */
private val ROW_MIN_HEIGHT = 56.dp
private val ROW_PADDING_VERTICAL = 10.dp

/**
 * The per group colors, chosen here because 10.11 requires one per group and names
 * none of them.
 *
 * **None of them is one of the app's four function colors**, which design-v3.md 3.1
 * scopes to exactly one job each: `actionBlue` is a control, `positiveGreen` is a
 * completion, `warnAmber` is a Pulse waiting and `deleteMuted` is a destructive swipe.
 * A settings badge in any of those would be the second meaning that scoping exists to
 * prevent, and it would be the seventh row on the screen that a person read as a
 * button. They are taken instead from the mood groups in 3.4, one hue apart from each
 * other, and they are never the color of a real area on a real card: an area accent
 * reaches the screen only through the four forms 3.4 permits, and a 26dp badge on a
 * settings row is none of them. What is shared is a hex value, not a meaning.
 *
 * **The glyph is `inkSecondary`, not the group color, and that is the deliberate
 * choice rather than the obvious one.** The statistically common settings screen of
 * 2026 puts a saturated glyph on a tinted square of the same hue, which turns a column
 * of rows into a column of badges and makes color the first thing read on a screen
 * whose entire content is words. Holding the glyph in ink keeps the tint as a quiet
 * grouping signal underneath the icon that carries the meaning, which is also what
 * design-v3.md 13 asks for: color is never the only signal, and here it is not a
 * signal at all. design-v3.md 15.
 */
internal object SettingsGroupColors {
    val daily = Color(0xFF6366F1)
    val focus = Color(0xFF1B6ACB)
    val afterCompleting = Color(0xFF16A34A)
    val appearance = Color(0xFFD946EF)
    val data = Color(0xFF0D9488)
    val privacy = Color(0xFF78716C)
    val help = Color(0xFFF97316)
}

/** A sidehead and the rows under it. design-v3.md 10.12 and 10.11. */
@Composable
internal fun SettingsGroup(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = LocalClarityColors.current
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(ClaritySpacing.snug),
    ) {
        // **The sidehead's rule is the group's one separation device, and the rows
        // beneath it are separated by air.**
        //
        // The screen drew sixteen hairlines: one under every row. A hairline between two
        // rows says the rows are a table; air says they are a list of settings, which is
        // what they are. 6.1 ranks whitespace first and the rules exist only where a
        // group starts, where something genuinely divides.
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = ClaritySpacing.hair),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Sidehead(text = title)
            Box(
                modifier = Modifier
                    .padding(start = ClaritySpacing.tight)
                    .weight(1f)
                    .height(1.dp)
                    .background(colors.hairline),
            )
        }
        content()
    }
}

/**
 * A row that navigates or acts. [value] is the trailing readout and [chevron] says
 * whether this row goes somewhere.
 *
 * [divider] is passed explicitly rather than derived, because a group's last row is
 * the only one that knows it is last and a hairline under it would be a rule under
 * the group rather than between two rows.
 */
@Composable
internal fun SettingsRow(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    caption: String? = null,
    value: String? = null,
    chevron: Boolean = true,
) {
    SettingsRowFrame(
        title = title,
        caption = caption,
        modifier = modifier.clarityClickable(
            haptic = ClarityHapticEvent.TAP,
            role = Role.Button,
            onClickLabel = title,
            onClick = onClick,
        ),
    ) {
        val colors = LocalClarityColors.current
        val type = LocalClarityTypography.current
        // design-v3.md 10.11 said `caption inkTertiary` here and 3.1 says
        // `inkTertiary` carries no text anywhere in this app. 10.11 is corrected
        // rather than left standing, the same way 10.3 and 10.19 were: this readout
        // is the current value of the setting, which is the one thing on the row a
        // person came to check, and at 2.337 to one on the canvas it was the least
        // readable thing on it. The `caption` role against the title's 15sp semibold
        // is what keeps it trailing and quiet.
        if (value != null) {
            Text(text = value, style = type.caption, color = colors.inkSecondary)
        }
        if (chevron) {
            Spacer(Modifier.width(6.dp))
            ClarityIcon(
                icon = ClarityIcons.chevron,
                contentDescription = null,
                // The chevron is what says the row goes somewhere, so it is a graphic
                // that carries meaning and takes design-v3.md 13's 3.0 floor.
                tint = colors.inkSecondary,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

/**
 * A row carrying a switch. design-v3.md 17.1 keeps the switch itself as platform work,
 * themed, so this is the platform control in the app's colors and nothing redrawn.
 *
 * The whole row is the target, not the switch, and the switch is handed a null
 * callback so it can never take a tap the row was going to handle. That is what makes
 * the target the full 56dp width rather than a 52dp control at the trailing edge.
 */
@Composable
internal fun SettingsToggleRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    caption: String? = null,
) {
    val colors = LocalClarityColors.current
    val haptics = LocalClarityHaptics.current
    val interaction = remember { MutableInteractionSource() }
    SettingsRowFrame(
        title = title,
        caption = caption,
        // `indication = null`, which is what `clarityClickable` passes and therefore
        // what every other tappable thing in this app does. Taking the default here
        // would put a ripple on the toggle rows and none on the rows above and below
        // them, on the one screen where the two kinds sit in the same column.
        modifier = modifier.toggleable(
            value = checked,
            interactionSource = interaction,
            indication = null,
            role = Role.Switch,
            onValueChange = { next ->
                haptics.perform(
                    if (next) ClarityHapticEvent.TOGGLE_ON else ClarityHapticEvent.TOGGLE_OFF,
                )
                onCheckedChange(next)
            },
        ),
    ) {
        Switch(
            checked = checked,
            onCheckedChange = null,
            // **The track is ink, not `actionBlue`.** 3.1 scopes the action color to a
            // control a person presses, and a switch track is a state readout. Measured
            // on a device capture, three saturated `#004BAE` blobs were the loudest
            // objects on a screen whose entire content is words. Ink is quieter and more
            // emphatic at once: on a page of grey and near white the darkest thing is
            // where the eye goes, and it does not have to shout to get there. It is also
            // the same fill the selected segment uses 200dp below.
            colors = SwitchDefaults.colors(
                checkedThumbColor = colors.card,
                checkedTrackColor = colors.inkPrimary,
                uncheckedThumbColor = colors.inkTertiary,
                uncheckedTrackColor = colors.raise,
                uncheckedBorderColor = colors.hairline,
            ),
            // The row already announces itself as a switch with a state. Without this
            // the same fact is read twice, once by the row and once by the control.
            modifier = Modifier.clearAndSetSemantics { },
        )
    }
}

/**
 * **The badge is deleted, and that is the change the whole screen rests on.**
 *
 * Every row carried a 26dp rounded square in one of seven hues at 12.5 percent with a
 * 15dp glyph inside it. Three things were wrong and they compound.
 *
 * It **set a second left edge**. The badge sat on the 20dp measure and the title began at
 * 61dp, so the group sideheads, the screen title and the paragraphs ran on one edge and
 * every row's text ran on another. That is the misalignment a person sees on this screen
 * without being able to name it.
 *
 * It **spent the app's one color device on nothing**. design-v3.md 3.4 gives area color a
 * job: it says which part of a person's life something belongs to. A settings group is
 * not an area, so the seven hues were decoration in the one app whose section 1 says
 * there is none, and they were the brightest thing on a screen made of words.
 *
 * And the glyphs **restated their own labels**. A bell beside `Pulse reminder`, a palette
 * beside `Appearance`. An icon earns its place by being faster to recognize than the word
 * or by disambiguating it, and in a single column of full labels it does neither.
 *
 * What replaces it is nothing at all. The title moves to the 20dp measure at `readStrong`
 * 18/600, the same rank as a sheet title, and the row does its job with size, position
 * and air. The per-row hairline goes with it: sixteen drawn lines said these were rows of
 * a table, and `snug` 12 of air says they are a list of settings, which is what they are.
 */
@Composable
private fun SettingsRowFrame(
    title: String,
    caption: String?,
    modifier: Modifier = Modifier,
    trailing: @Composable () -> Unit,
) {
    val colors = LocalClarityColors.current
    val type = LocalClarityTypography.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = ClaritySpacing.scaled(ROW_MIN_HEIGHT))
            .padding(vertical = ClaritySpacing.scaled(ROW_PADDING_VERTICAL)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = type.bodyStrong, color = colors.inkPrimary)
            if (caption != null) {
                Spacer(Modifier.height(ClaritySpacing.hair))
                // The caption explains what the setting does, and 10.11's note naming
                // `inkTertiary` for it is corrected in that section.
                Text(text = caption, style = type.caption, color = colors.inkSecondary)
            }
        }
        Spacer(Modifier.width(ClaritySpacing.snug))
        Row(verticalAlignment = Alignment.CenterVertically) { trailing() }
    }
}

/**
 * One row of a choice list: a label, and a check when it is the current answer.
 *
 * **The app's one option list, in one place.** It was private to `SettingsSheets.kt`
 * until the text size control needed the same thing on the screen rather than in a
 * sheet, and a second copy of it would have been a second selection language on the one
 * screen where both would be visible at once. [horizontalInset] is passed rather than
 * defaulted because the two callers genuinely differ: a sheet holds its own 20dp inset
 * and this screen already sits on `screenPadding`, so a default would be wrong for one
 * of them and silently wrong for whichever came third.
 *
 * The inset is inside the row rather than on the column around it, so the target is the
 * full width of the sheet or the screen and not the width of the text.
 *
 * Selection is never carried by color alone, per design-v3.md 13: the check is a shape,
 * the label moves from `inkSecondary` to `inkPrimary` with it, and `selected` is set in
 * semantics so a screen reader is told the same thing a third way.
 */
@Composable
internal fun SettingsChoiceRow(
    label: String,
    selected: Boolean,
    horizontalInset: Dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalClarityColors.current
    val type = LocalClarityTypography.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = ClaritySpacing.scaled(CHOICE_ROW_MIN_HEIGHT))
            .clarityClickable(
                haptic = ClarityHapticEvent.SELECT,
                role = Role.RadioButton,
                onClickLabel = label,
                onClick = onClick,
            )
            .semantics { this.selected = selected }
            .padding(
                horizontal = horizontalInset,
                vertical = ClaritySpacing.scaled(CHOICE_ROW_PADDING),
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = type.body,
            color = if (selected) colors.inkPrimary else colors.inkSecondary,
            modifier = Modifier.weight(1f),
        )
        if (selected) {
            ClarityIcon(
                icon = ClarityIcons.check,
                contentDescription = null,
                tint = colors.actionBlue,
                modifier = Modifier.size(CHOICE_CHECK_SIZE),
            )
        }
    }
}

private val CHOICE_ROW_MIN_HEIGHT = 52.dp
private val CHOICE_ROW_PADDING = 14.dp

/** A glyph, so it holds its size while the label beside it grows. design-v3.md 7. */
private val CHOICE_CHECK_SIZE = 18.dp

/**
 * The two option choice under After completing, MASTER_BUILD_PROMPT 14.1.
 *
 * **It reuses the app's one selection language rather than inventing a second.** The
 * statistically common segmented control of 2026 is a pill track with a thumb that
 * slides between the two halves, and design-v3.md 15 asks for that to be identified
 * and then beaten. A sliding thumb loses here twice: it is a movement, which
 * design-v3.md 8.2 gives no token for and which calm mode would then have to suppress
 * separately, and it would be a second way of saying "this one is chosen" beside
 * `ClarityChip`'s ink fill with an inverted label, which the app already uses on every
 * chip. So selection here is the same ink fill and the same crossfade, on the critically
 * damped effects spring for the reason `ClarityChip` states: an inversion that
 * overshoots reads as a bounce on every tap, which is on the tell list in 15.1.
 */
@Composable
internal fun <T> SettingsSegmentedChoice(
    options: List<Pair<T, String>>,
    selected: T,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalClarityColors.current
    val shapes = LocalClarityShapes.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shapes.button)
            .background(colors.raise)
            .padding(3.dp)
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        options.forEach { (value, label) ->
            SettingsSegment(
                label = label,
                selected = value == selected,
                onSelect = { onSelect(value) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun SettingsSegment(
    label: String,
    selected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalClarityColors.current
    val type = LocalClarityTypography.current
    val shapes = LocalClarityShapes.current
    val motion = clarityMotion()
    val background by animateColorAsState(
        targetValue = if (selected) colors.inkPrimary else Color.Transparent,
        animationSpec = motion.effects(),
        label = "segmentBackground",
    )
    val labelColor by animateColorAsState(
        targetValue = if (selected) colors.card else colors.inkSecondary,
        animationSpec = motion.effects(),
        label = "segmentLabel",
    )
    Box(
        modifier = modifier
            .defaultMinSize(minHeight = ClaritySpacing.scaled(SEGMENT_MIN_HEIGHT))
            .clip(shapes.segment)
            .background(background)
            .clarityClickable(
                haptic = ClarityHapticEvent.SELECT,
                role = Role.RadioButton,
                onClickLabel = label,
                onClick = onSelect,
            )
            // Without this an ink filled segment and an empty one are the same node
            // to a screen reader, which leaves the inversion as a color only signal.
            // design-v3.md 13.
            .semantics { this.selected = selected },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = type.label.opticallyCentered(),
            color = labelColor,
            modifier = Modifier.padding(horizontal = 10.dp),
        )
    }
}

private val SEGMENT_MIN_HEIGHT = 42.dp
