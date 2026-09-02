package com.kamsiob.claritynow.ui.onboarding

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kamsiob.claritynow.ui.components.ClarityIcon
import com.kamsiob.claritynow.ui.components.ClarityIcons
import com.kamsiob.claritynow.ui.components.clarityClickable
import com.kamsiob.claritynow.ui.components.clarityFocusRing
import com.kamsiob.claritynow.ui.components.clarityPressScale
import com.kamsiob.claritynow.ui.theme.ClarityHapticEvent
import com.kamsiob.claritynow.ui.theme.ClarityDarkColors
import com.kamsiob.claritynow.ui.theme.ClaritySpacing
import com.kamsiob.claritynow.ui.theme.LocalCalmMode
import com.kamsiob.claritynow.ui.theme.LocalClarityShapes
import com.kamsiob.claritynow.ui.theme.LocalClarityTypography
import com.kamsiob.claritynow.ui.theme.LocalContemplativeColors
import com.kamsiob.claritynow.ui.theme.areaLabelColor
import com.kamsiob.claritynow.ui.theme.calmed
import com.kamsiob.claritynow.ui.theme.clarityMotion
import com.kamsiob.claritynow.ui.theme.parseAreaColor

/**
 * The Contemplative controls onboarding needs, and nothing else.
 *
 * **Written here rather than reused from `ui/components`**, because every control in that
 * package resolves its colors from `LocalClarityColors`, which is the Daylight world.
 * design-v3.md section 2 makes onboarding Contemplative from the first frame to the last,
 * and a Daylight button on `deepBlack` is not a theming bug that shows up in a
 * screenshot: it is `actionBlue` fill and white label, which happens to look plausible
 * and is the wrong world. The Pulse surface made the same call for the same reason and
 * its response pill lives in `ui/pulse`.
 *
 * What is shared is everything that is not color: the press scale, the focus ring, the
 * click handling and the haptics all come from `ui/components`, so these behave exactly
 * as their Daylight counterparts do.
 */

/** design-v3.md 10.7, Contemplative primary: a translucent white pill with a bright label. */
@Composable
internal fun OnboardingPrimaryButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val contemplative = LocalContemplativeColors.current
    val type = LocalClarityTypography.current
    val motion = clarityMotion()
    val interaction = remember { MutableInteractionSource() }
    val fill by animateColorAsState(
        targetValue = Color.White.copy(alpha = if (enabled) PILL_ALPHA else PILL_ALPHA_DISABLED),
        animationSpec = motion.effects(),
        label = "onboardingButtonFill",
    )

    Box(
        modifier = modifier
            .clarityPressScale(interaction, enabled = enabled, label = "onboardingButton")
            .heightIn(min = 50.dp)
            .clip(CircleShape)
            .background(fill)
            .clarityFocusRing(interaction, CircleShape)
            .clarityClickable(
                enabled = enabled,
                interactionSource = interaction,
                haptic = ClarityHapticEvent.TAP,
                role = Role.Button,
                onClickLabel = label,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = type.bodyStrong,
            color = if (enabled) contemplative.textBright else contemplative.textFaint,
            modifier = Modifier.padding(
                horizontal = 28.dp,
                vertical = ClaritySpacing.scaled(13.dp,
            )),
        )
    }
}

/**
 * One of the two paths at the top of beat 2. MASTER_BUILD_PROMPT 14b.11, Addendum 01 8a.
 *
 * **There is one composable, it is called once per path, and it takes no parameter that
 * could make either of them louder.** That is the same rule `PulseResponsePill` holds for
 * the same reason: the addendum requires `Just start` to be "a genuine equal alternative,
 * not buried, not a text link under the real button", and equal weight is a property of
 * the code rather than an intention. A primary and a secondary role, a filled and an
 * outlined panel, or a wider and a narrower one would each be the interface answering the
 * question on the person's behalf.
 */
@Composable
internal fun OnboardingChoicePanel(
    title: String,
    detail: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contemplative = LocalContemplativeColors.current
    val type = LocalClarityTypography.current
    val shapes = LocalClarityShapes.current
    val interaction = remember { MutableInteractionSource() }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clarityPressScale(interaction, label = "onboardingChoice")
            .clip(shapes.card)
            .background(contemplative.surfaceRaised)
            .clarityFocusRing(interaction, shapes.card)
            .clarityClickable(
                interactionSource = interaction,
                haptic = ClarityHapticEvent.SELECT,
                role = Role.Button,
                onClickLabel = title,
                onClick = onClick,
            )
            .padding(horizontal = 20.dp, vertical = ClaritySpacing.scaled(18.dp)),
    ) {
        Text(text = title, style = type.title, color = contemplative.textBright)
        Spacer(Modifier.height(ClaritySpacing.scaled(5.dp)))
        Text(text = detail, style = type.body, color = contemplative.textDim)
    }
}

/**
 * A starter area suggestion. design-v3.md 10.8, in the Contemplative world.
 *
 * Selected carries a 7dp dot in the area's own color and a brighter ground. **The chip is
 * never filled with the accent**, per design-v3.md 3.4: color reaches a screen as a dot,
 * a wash, a Momentum tile or a label, and never as a filled block.
 */
@Composable
internal fun OnboardingSuggestionChip(
    label: String,
    selected: Boolean,
    colorHex: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contemplative = LocalContemplativeColors.current
    val type = LocalClarityTypography.current
    val motion = clarityMotion()
    val interaction = remember { MutableInteractionSource() }
    val ground by animateColorAsState(
        targetValue = Color.White.copy(alpha = if (selected) CHIP_ALPHA_SELECTED else CHIP_ALPHA),
        animationSpec = motion.effects(),
        label = "onboardingChipGround",
    )

    Row(
        modifier = modifier
            .clarityPressScale(interaction, label = "onboardingChip")
            .heightIn(min = ClaritySpacing.minTouchTarget)
            .clip(CircleShape)
            .background(ground)
            .clarityFocusRing(interaction, CircleShape)
            .clarityClickable(
                interactionSource = interaction,
                haptic = ClarityHapticEvent.SELECT,
                role = Role.Button,
                onClickLabel = label,
                onClick = onClick,
            )
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        if (selected && colorHex != null) {
            Box(
                modifier = Modifier
                    .size(ClaritySpacing.areaDot)
                    .clip(CircleShape)
                    .background(parseAreaColor(colorHex)),
            )
        }
        Text(
            text = label,
            style = type.label,
            color = if (selected) contemplative.textBright else contemplative.textDim,
        )
    }
}

/**
 * A miniature area card. design-v3.md 10.9's live preview, in the Contemplative world.
 *
 * The three permitted forms of color and no fourth: a 7dp dot, the area's wash behind the
 * row, and the name in the accent. The wash goes through the calm mode transform and the
 * dot and the label do not, per design-v3.md 16.2's named exclusions.
 *
 * **The label variant is computed against the Daylight dark card rather than against
 * `surfaceRaised`.** `areaLabelColor` is the one verified answer to design-v3.md 3.4's
 * 4.5:1 requirement and it takes a `ClarityColors`; `surfaceRaised` at `#14141C` is
 * darker than the dark card at `#1D1D25`, and the dark remedy lightens the accent, so
 * every variant it returns measures better here than on the ground it was verified
 * against. Conservative in the right direction, and it keeps one implementation of the
 * rule rather than a second one nobody has measured.
 */
@Composable
internal fun OnboardingMiniCard(
    name: String,
    colorHex: String,
    focused: Boolean,
    onClick: () -> Unit,
    onRemove: () -> Unit,
    removeLabel: String,
    modifier: Modifier = Modifier,
) {
    val contemplative = LocalContemplativeColors.current
    val type = LocalClarityTypography.current
    val shapes = LocalClarityShapes.current
    val calm = LocalCalmMode.current
    val accent = parseAreaColor(colorHex)
    val interaction = remember { MutableInteractionSource() }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clarityPressScale(interaction, label = "onboardingMiniCard")
            .clip(shapes.row)
            .background(contemplative.surfaceRaised)
            .background(accent.calmed(calm).copy(alpha = if (focused) WASH_FOCUSED else WASH))
            .clarityFocusRing(interaction, shapes.row)
            .clarityClickable(
                interactionSource = interaction,
                haptic = ClarityHapticEvent.SELECT,
                role = Role.Button,
                onClickLabel = name,
                onClick = onClick,
            )
            .padding(start = 16.dp, top = 13.dp, bottom = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(ClaritySpacing.areaDot)
                .clip(CircleShape)
                .background(accent),
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = name,
            style = type.label,
            color = areaLabelColor(accent, ClarityDarkColors),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Box(
            modifier = Modifier
                .size(ClaritySpacing.minTouchTarget)
                .clip(CircleShape)
                .clarityClickable(
                    haptic = ClarityHapticEvent.TAP,
                    role = Role.Button,
                    onClickLabel = removeLabel,
                    onClick = onRemove,
                ),
            contentAlignment = Alignment.Center,
        ) {
            ClarityIcon(
                icon = ClarityIcons.close,
                contentDescription = removeLabel,
                // The only way to undo an area added by mistake. A control's glyph,
                // so design-v3.md 13's 3.0 floor is the least it clears, and 32
                // percent misses it at 2.680 on the beat's ground.
                tint = contemplative.textDim,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

/**
 * A field with no box and no outline, which is what `ClarityTextField` is in the Daylight
 * world and what this is in the Contemplative one. Label, text, and a hairline that
 * brightens while the field has focus.
 */
@Composable
internal fun OnboardingField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    imeAction: ImeAction = ImeAction.Done,
    focusRequester: FocusRequester? = null,
    onImeAction: () -> Unit = {},
) {
    val contemplative = LocalContemplativeColors.current
    val type = LocalClarityTypography.current
    val motion = clarityMotion()
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    val rule by animateColorAsState(
        targetValue = Color.White.copy(alpha = if (focused) RULE_ALPHA_FOCUSED else RULE_ALPHA),
        animationSpec = motion.effects(),
        label = "onboardingFieldRule",
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Text(text = label, style = type.sidehead, color = contemplative.textDim)
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = type.body.copy(color = contemplative.textBright),
            cursorBrush = SolidColor(Color.White.copy(alpha = CURSOR_ALPHA)),
            interactionSource = interaction,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = imeAction,
            ),
            keyboardActions = KeyboardActions(
                onDone = { onImeAction() },
                onGo = { onImeAction() },
            ),
            decorationBox = { field ->
                Box {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = type.body,
                            // The Contemplative twin of design-v3.md 10.19, and the
                            // same answer: what says "not filled in" is that the text
                            // vanishes on the first keystroke, not that it was too
                            // faint to read. 32 percent measured 2.680 to one here.
                            color = contemplative.textDim,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    field()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = ClaritySpacing.scaled(8.dp))
                // design-v3 13's touch minimum. `Fields.kt` moved its own field off 28dp in
                // phase 12b with the note that "a field is the most deliberate tap target
                // on a sheet"; the onboarding twin never got the same change and is the
                // first field anybody in this app ever touches.
                .defaultMinSize(minHeight = ClaritySpacing.minTouchTarget)
                .then(
                    if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier,
                ),
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = ClaritySpacing.scaled(6.dp))
                .height(1.dp)
                .clip(RoundedCornerShape(1.dp))
                .background(rule),
        )
    }
}

private const val PILL_ALPHA = 0.09f
private const val PILL_ALPHA_DISABLED = 0.04f
private const val CHIP_ALPHA = 0.07f
private const val CHIP_ALPHA_SELECTED = 0.16f
private const val WASH = 0.08f
private const val WASH_FOCUSED = 0.14f
private const val RULE_ALPHA = 0.16f
private const val RULE_ALPHA_FOCUSED = 0.42f
private const val CURSOR_ALPHA = 0.75f
