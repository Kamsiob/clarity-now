package com.kamsiob.claritynow.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.semantics.text
import androidx.compose.ui.unit.dp
import com.kamsiob.claritynow.ui.theme.ClaritySpacing
import com.kamsiob.claritynow.ui.theme.LocalClarityColors
import com.kamsiob.claritynow.ui.theme.LocalClarityShapes
import com.kamsiob.claritynow.ui.theme.LocalClarityTypography
import com.kamsiob.claritynow.ui.theme.clarityMotion

/**
 * A field is a sidehead and a well. design-v3.md 10.19, settled in phase 12b.
 *
 * ## What it used to be, and why that was the wrong answer
 *
 * Section 10 covered buttons, chips, settings rows and sideheads and had no field entry
 * at all, so phase 2 filled the silence with an underlined field carrying a hairline that
 * warmed to `actionBlue` on focus. That is the most common form treatment in existence,
 * which design-v3.md 15 makes a reason on its own, and it also reached past the rule that
 * was supposed to decide it: **6.1 puts a hairline fourth among separation devices, "only
 * if all three above have genuinely failed"**, and the four stacked rules in the add sheet
 * were the third and fourth devices in the whole app.
 *
 * Walking 6.1 in the order 6.1 gives produces a different field. Whitespace, device one,
 * genuinely fails: an empty field separated by air alone has nothing to aim a thumb at,
 * which is the one thing a field has to have. Device two is a background lightness shift,
 * and it does not fail. So the field is a **well**: the surface steps down one rank of the
 * phase 3c ladder and the rule is gone entirely.
 *
 * ## Why down rather than up
 *
 * A field is a place content is put into, so it reads as a recess rather than as
 * something floating on the sheet. The step is `raise`, which design-v3.md 3.1 defines as
 * "the 3 percent lightness step used *instead of* a border", which is the sentence this
 * whole component now rests on. The ladder's chrome rank is where anything that recedes
 * from content lives, and a well and a tab bar recede for the same reason.
 *
 * **Every field in this app is inside a sheet, whose ground is `card`.** That is what
 * makes `raise` a step down. A field placed directly on `canvas` would be a step up and
 * would read as a raised tile instead of a well, and the fix then is the ground under it
 * rather than a value here. There is no such call site and this note exists so that
 * adding one is a decision.
 *
 * ## Focus is a deeper well, not a colored line
 *
 * The statistically common focus signal is a two dp accent rule or an accent border, and
 * both are a second separation device on an element that already carries one. Focus takes
 * the well one further rank down instead, from `raise` to `canvas`, which is the same
 * device speaking louder rather than a new one. The caret is the second signal, in
 * `actionBlue`, which 16.2 excludes from the calm mode transform because it is function
 * rather than atmosphere. Section 13 is satisfied without color doing the work alone: the
 * keyboard, the caret and the depth all change together.
 *
 * ## The placeholder is inkSecondary, and that is a floor rather than a preference
 *
 * It was `inkTertiary`, which measures 2.40 to one on a light card against design-v3.md
 * 13's floor of 4.5. This is the same contradiction 10.3 resolved for `Add your first
 * item` and it is resolved the same way: a floor is a floor. What says "not filled in" is
 * that the text disappears on the first keystroke, not that it was too faint to read.
 *
 * [placeholder] draws inside the empty field and disappears on the first character.
 * design-v3.md 10.17 asks for exactly one of these, on the first step field, and is
 * specific about what it may contain: **an example, never an instruction.** An instruction
 * to break a task down is a second task, handed to the person least able to take one on,
 * which is the failure Addendum 01 4b exists to avoid. Nothing here enforces that; the
 * call sites do, and the string is the review.
 *
 * [keyboardType] exists for the one numeric field in the app, the optional estimate in
 * design-v3.md 10.17. Capitalization stays on Sentences for every type, because it is
 * ignored on a numeric keyboard and a second branch would be a second thing to keep in
 * step.
 *
 * ## One thing 17.6 asks to be written down
 *
 * design-v3.md 17.3 lists text fields under "everything else starts at step 1", and this
 * one is a `BasicTextField` rather than a themed Material `TextField`, so the app reached
 * step 4 here in phase 2 without recording it. Phase 12b records it rather than reopening
 * it: 17.4 forbids a polish pass from rebuilding a working component to change how it
 * looks, and that cuts both ways, so the structure is left alone and only the tokens on
 * it moved. The reason that applies is 17.2's third, the platform component fighting a
 * rule in this document: Material's filled field carries an indicator line and its
 * outlined field a notched border, and both are the hairline 6.1 puts last.
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
    /**
     * **Null, not an empty lambda, and the difference is the whole of finding 2.**
     *
     * A non-null `KeyboardActions` handler *suppresses the platform default*. With
     * `onImeAction: () -> Unit = {}` every one of the twelve call sites was silently
     * installing a handler that did nothing, so Next never advanced focus and Done never
     * dismissed the keyboard anywhere in the app, including on the capture sheet where
     * the action key is the natural way to finish. Null restores the default behavior and
     * a caller that wants its own still gets it.
     */
    onImeAction: (() -> Unit)? = null,
) {
    val colors = LocalClarityColors.current
    val shapes = LocalClarityShapes.current
    val type = LocalClarityTypography.current
    val motion = clarityMotion()
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    val well by animateColorAsState(
        targetValue = if (focused) colors.canvas else colors.raise,
        // design-v3.md 8.3 and 16.8: a color change runs on the crossfade spec, so this
        // is one flag away from being instant when motion is reduced or calm mode is on.
        animationSpec = motion.effects(),
        label = "fieldWell",
    )

    Column(modifier = modifier.fillMaxWidth()) {
        // **The label is drawn here and spoken by the field below it, never both.**
        // `BasicTextField` has no label slot, so a sibling `Text` is the only way to draw
        // one and nothing associates the two: TalkBack announced "Edit box" plus whatever
        // had been typed, which on the capture sheet is two consecutive fields that sound
        // identical. The field takes the name and the drawn label is cleared, so it is
        // read once.
        Text(
            text = label,
            style = type.sidehead,
            color = colors.inkSecondary,
            modifier = Modifier.clearAndSetSemantics { },
        )
        Spacer(Modifier.height(ClaritySpacing.scaled(FIELD_LABEL_GAP)))
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
            keyboardActions = if (onImeAction == null) {
                androidx.compose.foundation.text.KeyboardActions.Default
            } else {
                androidx.compose.foundation.text.KeyboardActions(
                    onDone = { onImeAction() },
                    onNext = { onImeAction() },
                )
            },
            decorationBox = { field ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(shapes.button)
                        .background(well)
                        // design-v3.md 13's touch minimum. The field was 28dp tall until
                        // phase 12b, which is 20dp under it, and a field is the most
                        // deliberate tap target on a sheet.
                        .defaultMinSize(minHeight = ClaritySpacing.minTouchTarget)
                        .padding(
                            horizontal = FIELD_PADDING_HORIZONTAL,
                            vertical = ClaritySpacing.scaled(FIELD_PADDING_VERTICAL),
                        ),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    // The placeholder sits behind the text rather than in front of it, so a
                    // cursor placed in an empty field is never drawn under the example.
                    if (placeholder != null && value.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = type.body,
                            color = colors.inkSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    field()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                // **The label is the field's `text`, not its `contentDescription`.**
                //
                // A `contentDescription` on an editable node REPLACES what TalkBack reads,
                // so the first version of this fix announced "Title" and hid "Buy milk",
                // which is worse than the anonymous field it replaced: a person could no
                // longer hear what they had typed. Compose's own resolution order for an
                // editable node is contentDescription, then text, then editableText, so
                // putting the label in `text` names the field and leaves the typed value
                // to be read after it.
                .semantics { text = AnnotatedString(label) }
                .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier),
        )
    }
}

/**
 * The sidehead sits close to the well it names.
 *
 * Six dp rather than the eight the underlined field used, because a label above a filled
 * shape needs less air than a label above a line: the shape's own edge already says where
 * the field begins.
 */
private val FIELD_LABEL_GAP = 6.dp

/**
 * The text's inset inside the well.
 *
 * Fourteen horizontal, which is `button`'s 12dp radius plus two, so the first glyph clears
 * the corner rather than sitting inside its curve. Vertical is 12, which puts a 15sp line
 * of `body` in a 48dp well with 12dp of air above and below and leaves the minimum doing
 * nothing on a single line field.
 */
private val FIELD_PADDING_HORIZONTAL = 14.dp
private val FIELD_PADDING_VERTICAL = 12.dp
