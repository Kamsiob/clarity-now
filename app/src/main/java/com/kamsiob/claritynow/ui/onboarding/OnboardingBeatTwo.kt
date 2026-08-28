package com.kamsiob.claritynow.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.kamsiob.claritynow.R
import com.kamsiob.claritynow.ui.theme.ClaritySpacing
import com.kamsiob.claritynow.ui.theme.LocalClarityTypography
import com.kamsiob.claritynow.ui.theme.LocalContemplativeColors

/**
 * Beat 2, Your Areas. MASTER_BUILD_PROMPT 13.1 and 14b.11, Addendum 01 8a.
 *
 * ## The fork, and why the beat opens with it
 *
 * Addendum 01 8a requires `Just start` to be offered "as a genuine equal alternative and
 * not buried", and rules out by name the shape it would otherwise take: a text link under
 * the real button. The acceptance criterion is equal weight, and equal weight is a
 * property of the arrangement rather than of the intention behind it.
 *
 * **So beat 2 opens as a fork rather than as a form.** Two panels, one composable called
 * twice, identical width, identical surface, identical type, no role parameter that could
 * make one of them louder. The area picker is the second screen of one path rather than
 * being the beat with an escape hatch attached to it. That is the same rule
 * `PulseResponsePill` holds, and design-v3.md section 11 gives the reason there in one
 * sentence that applies here word for word: side by side does not survive, because a left
 * position reads as a recommendation. Stacked, one above the other, neither does.
 *
 * **The order is `Just start` first, and that is the deliberate choice rather than the
 * obvious one.** design-v3.md 15. The obvious order is the fuller path first, because it
 * is the original of the two and the addendum is the addition. Some order has to exist,
 * vertical position is the only weight left once treatment is identical, and the tie goes
 * to the path that costs nothing: the person who most needs a zero decision start is the
 * least likely to read past the first option. The other path is one thumb below it at the
 * same size, which is what "not buried" means.
 *
 * **The cost is one tap for the person who wanted the picker**, and that is the right
 * trade for this app: 14b.11's whole argument is that decisions are the expensive thing
 * and taps are not. One decision replaces up to twelve.
 *
 * ## The picker
 *
 * Six suggestions plus a custom field, each selection showing a mini card, and the mood
 * color rows opening on the selection that was last touched. Every selection is a
 * transient struct, per 13.1: **nothing here writes anything.**
 *
 * The copy says `Pick two to four` and the Continue control is live from one selection.
 * MASTER_BUILD_PROMPT 8.1 settles that in as many words: "there is no limit on the number
 * of areas. The philosophy is carried by copy and layout, not a cap."
 *
 * ## The Just start path
 *
 * One line, optional, and the person's first item. 14b.11 asks for `Just start` to drop
 * them "straight into adding their first item", and this is that with nothing between the
 * tap and the field. It is a capture rather than a decision, per 14b.1, so it can be left
 * empty and Continue does not care.
 */
@Composable
internal fun OnboardingBeatTwo(
    state: OnboardingUiState,
    onJustStart: () -> Unit,
    onPickAreas: () -> Unit,
    onFirstItemChange: (String) -> Unit,
    onToggleSuggestion: (String) -> Unit,
    onAddCustom: (String) -> Unit,
    onRemove: (String) -> Unit,
    onFocus: (String) -> Unit,
    onRecolor: (String, String) -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Centered while the content is short, scrolling once it is not. A column inside a
    // vertical scroll is measured with an unbounded height, so `Arrangement.Center` alone
    // would silently do nothing and every stage would sit against the top of the screen;
    // the viewport minimum is what gives the arrangement something to center inside.
    BoxWithConstraints(modifier = modifier.fillMaxSize().imePadding()) {
        val viewport = maxHeight
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .heightIn(min = viewport)
                .fillMaxWidth()
                .padding(horizontal = ClaritySpacing.screenPadding),
            verticalArrangement = Arrangement.Center,
        ) {
            when (state.stage) {
                BeatTwoStage.FORK -> Fork(onJustStart = onJustStart, onPickAreas = onPickAreas)

                BeatTwoStage.JUST_START -> JustStart(
                    title = state.firstItemTitle,
                    onTitleChange = onFirstItemChange,
                    onContinue = onContinue,
                )

                BeatTwoStage.PICK_AREAS -> PickAreas(
                    state = state,
                    onToggleSuggestion = onToggleSuggestion,
                    onAddCustom = onAddCustom,
                    onRemove = onRemove,
                    onFocus = onFocus,
                    onRecolor = onRecolor,
                    onContinue = onContinue,
                )
            }
        }
    }
}

@Composable
private fun ColumnScope.Fork(onJustStart: () -> Unit, onPickAreas: () -> Unit) {
    val contemplative = LocalContemplativeColors.current
    val type = LocalClarityTypography.current

    Text(
        text = stringResource(R.string.onboarding_beat_two_question),
        style = type.readSerif,
        color = contemplative.textBright,
    )
    Spacer(Modifier.height(30.dp))
    OnboardingChoicePanel(
        title = stringResource(R.string.onboarding_just_start_title),
        detail = stringResource(R.string.onboarding_just_start_detail),
        onClick = onJustStart,
    )
    Spacer(Modifier.height(14.dp))
    OnboardingChoicePanel(
        title = stringResource(R.string.onboarding_pick_areas_title),
        detail = stringResource(R.string.onboarding_pick_areas_detail),
        onClick = onPickAreas,
    )
}

@Composable
private fun ColumnScope.JustStart(
    title: String,
    onTitleChange: (String) -> Unit,
    onContinue: () -> Unit,
) {
    val contemplative = LocalContemplativeColors.current
    val type = LocalClarityTypography.current

    Text(
        text = stringResource(R.string.onboarding_just_start_heading),
        style = type.readSerif,
        color = contemplative.textBright,
    )
    Spacer(Modifier.height(10.dp))
    Text(
        text = stringResource(R.string.onboarding_just_start_note),
        style = type.body,
        color = contemplative.textDim,
    )
    Spacer(Modifier.height(30.dp))
    OnboardingField(
        value = title,
        onValueChange = { onTitleChange(it.take(MAX_TITLE)) },
        label = stringResource(R.string.onboarding_just_start_field),
        placeholder = stringResource(R.string.onboarding_just_start_placeholder),
        imeAction = ImeAction.Done,
        onImeAction = onContinue,
    )
    Spacer(Modifier.height(34.dp))
    OnboardingPrimaryButton(
        label = stringResource(R.string.onboarding_continue),
        onClick = onContinue,
        modifier = Modifier.align(Alignment.CenterHorizontally),
    )
}

@Composable
private fun ColumnScope.PickAreas(
    state: OnboardingUiState,
    onToggleSuggestion: (String) -> Unit,
    onAddCustom: (String) -> Unit,
    onRemove: (String) -> Unit,
    onFocus: (String) -> Unit,
    onRecolor: (String, String) -> Unit,
    onContinue: () -> Unit,
) {
    val contemplative = LocalContemplativeColors.current
    val type = LocalClarityTypography.current
    var custom by remember { mutableStateOf("") }
    val suggestions = suggestionLabels()

    Text(
        text = stringResource(R.string.onboarding_areas_heading),
        style = type.readSerif,
        color = contemplative.textBright,
    )
    Spacer(Modifier.height(10.dp))
    Text(
        text = stringResource(R.string.onboarding_areas_detail),
        style = type.body,
        color = contemplative.textDim,
    )
    Spacer(Modifier.height(22.dp))

    // Two to a row rather than a wrapping flow, so every suggestion is the same width and
    // none of them reads as the recommended one because its label happens to be longer.
    suggestions.chunked(2).forEach { pair ->
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            pair.forEach { label ->
                val chosen = state.selections.firstOrNull { it.name.equals(label, true) }
                OnboardingSuggestionChip(
                    label = label,
                    selected = chosen != null,
                    colorHex = chosen?.colorHex,
                    onClick = { onToggleSuggestion(label) },
                    modifier = Modifier.weight(1f),
                )
            }
            // Keeps the last row's single chip the same width as every other chip rather
            // than letting it stretch across the row.
            if (pair.size == 1) Box(Modifier.weight(1f))
        }
    }

    Spacer(Modifier.height(12.dp))
    OnboardingField(
        value = custom,
        onValueChange = { custom = it.take(MAX_AREA_NAME) },
        label = stringResource(R.string.onboarding_areas_custom_label),
        placeholder = stringResource(R.string.onboarding_areas_custom_placeholder),
        imeAction = ImeAction.Done,
        onImeAction = {
            onAddCustom(custom)
            custom = ""
        },
    )

    if (state.selections.isNotEmpty()) {
        Spacer(Modifier.height(26.dp))
        state.selections.forEach { area ->
            OnboardingMiniCard(
                name = area.name,
                colorHex = area.colorHex,
                focused = state.focused == area.name,
                onClick = { onFocus(area.name) },
                onRemove = { onRemove(area.name) },
                removeLabel = stringResource(R.string.cd_onboarding_remove_area, area.name),
                modifier = Modifier.padding(bottom = 8.dp),
            )
            if (state.focused == area.name) {
                OnboardingColorRows(
                    selectedHex = area.colorHex,
                    onPick = { onRecolor(area.name, it) },
                    modifier = Modifier.padding(bottom = 14.dp),
                )
            }
        }
    }

    Spacer(Modifier.height(28.dp))
    OnboardingPrimaryButton(
        label = stringResource(R.string.onboarding_continue),
        onClick = onContinue,
        enabled = state.canAdvance,
        modifier = Modifier.align(Alignment.CenterHorizontally),
    )
    Spacer(Modifier.height(12.dp))
}

/** The six starter suggestions named in MASTER_BUILD_PROMPT 13.1. */
@Composable
private fun suggestionLabels(): List<String> = listOf(
    stringResource(R.string.onboarding_suggestion_work),
    stringResource(R.string.onboarding_suggestion_personal),
    stringResource(R.string.onboarding_suggestion_health),
    stringResource(R.string.onboarding_suggestion_family),
    stringResource(R.string.onboarding_suggestion_learning),
    stringResource(R.string.onboarding_suggestion_side_project),
)

/** MASTER_BUILD_PROMPT 8.1 and 8.2. The repository trims and rejects past these anyway. */
private const val MAX_AREA_NAME = 40
private const val MAX_TITLE = 200
