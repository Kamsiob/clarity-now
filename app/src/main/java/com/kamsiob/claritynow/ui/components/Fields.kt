package com.kamsiob.claritynow.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kamsiob.claritynow.ui.theme.LocalClarityColors
import com.kamsiob.claritynow.ui.theme.LocalClarityTypography
import com.kamsiob.claritynow.ui.theme.clarityMotion

/**
 * A text field with no box and no outline. design-v3.md keeps structure in
 * sideheads and whitespace, so a field is a label, the text, and a hairline that
 * warms to the action color while it has focus.
 *
 * [placeholder] draws inside the empty field at inkTertiary and disappears on the
 * first character. design-v3.md 10.17 asks for exactly one of these, on the first
 * step field, and is specific about what it may contain: **an example, never an
 * instruction.** An instruction to break a task down is a second task, handed to the
 * person least able to take one on, which is the failure Addendum 01 4b exists to
 * avoid. Nothing here enforces that; the call sites do, and the string is the review.
 *
 * [keyboardType] exists for the one numeric field in the app, the optional estimate
 * in design-v3.md 10.17. Capitalization stays on Sentences for every type, because it
 * is ignored on a numeric keyboard and a second branch would be a second thing to
 * keep in step.
 */
@Composable
fun ClarityTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    imeAction: ImeAction = ImeAction.Done,
    keyboardType: KeyboardType = KeyboardType.Text,
    placeholder: String? = null,
    focusRequester: FocusRequester? = null,
    onImeAction: () -> Unit = {},
) {
    val colors = LocalClarityColors.current
    val type = LocalClarityTypography.current
    val motion = clarityMotion()
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    val rule by animateColorAsState(
        targetValue = if (focused) colors.actionBlue else colors.hairline,
        animationSpec = motion.effects(),
        label = "fieldRule",
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Text(text = label, style = type.sidehead, color = colors.inkSecondary)
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = singleLine,
            textStyle = type.body.copy(color = colors.inkPrimary),
            cursorBrush = SolidColor(colors.actionBlue),
            interactionSource = interaction,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                keyboardType = keyboardType,
                imeAction = imeAction,
            ),
            keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                onDone = { onImeAction() },
                onNext = { onImeAction() },
            ),
            decorationBox = { field ->
                // The placeholder sits behind the text rather than in front of it, so a
                // cursor placed in an empty field is never drawn under the example.
                Box {
                    if (placeholder != null && value.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = type.body,
                            color = colors.inkTertiary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    field()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .defaultMinSize(minHeight = 28.dp)
            .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier),
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp)
                .clip(RoundedCornerShape(1.dp))
                .background(rule)
                .defaultMinSize(minHeight = if (focused) 1.5.dp else 1.dp),
        )
    }
}
