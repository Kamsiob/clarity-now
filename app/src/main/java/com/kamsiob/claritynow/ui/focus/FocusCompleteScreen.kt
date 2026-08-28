package com.kamsiob.claritynow.ui.focus

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.kamsiob.claritynow.R
import com.kamsiob.claritynow.ui.theme.ClarityHapticEvent
import com.kamsiob.claritynow.ui.theme.ClaritySpacing
import com.kamsiob.claritynow.ui.theme.LocalClarityHaptics
import com.kamsiob.claritynow.ui.theme.LocalClarityTypography
import com.kamsiob.claritynow.ui.theme.LocalContemplativeColors

/**
 * A session that has finished. design-v3.md section 11.
 *
 * The ring is replaced by a circle bloom and a check, `Session complete` is set in the
 * serif, the item title follows, then one small line reading the duration and the area,
 * then `Mark item complete` in the accent and `Done` beneath it.
 *
 * **A session ended early reaches this screen, in these words, with these actions.**
 * Addendum 01 4e and design-v3.md section 11: the serif line reads `Session complete`,
 * the duration line reads the real duration, and there is no qualifier, no shortfall,
 * no comparison against what was planned and no second, quieter version of this screen
 * for a shorter session. Fourteen minutes is fourteen minutes. Nothing on this screen
 * is told which kind of ending it is drawing, because the model it is handed does not
 * carry that fact.
 *
 * **And nothing here congratulates.** design-v3.md 14 forbids celebration of any kind,
 * so the bloom is a soft circle that fades as it grows and the line above it states
 * what happened. Neither this screen nor any accessibility label on it contains the
 * word design-v3.md 13.1 keeps off every surface.
 */
@Composable
internal fun FocusCompleteScreen(
    completion: FocusCompletionModel,
    onMarkItemComplete: () -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contemplative = LocalContemplativeColors.current
    val type = LocalClarityTypography.current
    val haptics = LocalClarityHaptics.current

    // design-v3.md 9, focusEnd: a quick rise then a thud, at a natural completion, and
    // only when the person was here to feel it. A session resolved on the next resume
    // arrives with `announce` false, because section 9 fires nothing on screen entry.
    LaunchedEffect(completion.sessionId) {
        if (completion.announce) haptics.perform(ClarityHapticEvent.FOCUS_END)
    }

    Box(modifier = modifier.fillMaxSize().safeDrawingPadding()) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = ClaritySpacing.screenPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            FocusBloom()
            Spacer(Modifier.height(ClaritySpacing.scaled(28.dp)))
            Text(
                text = stringResource(R.string.focus_complete_title),
                style = type.readSerif,
                color = contemplative.textBright,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(ClaritySpacing.scaled(10.dp)))
            Text(
                text = completion.itemTitle,
                style = type.itemTitle.copy(
                    fontSize = COMPLETE_TITLE_SIZE,
                    lineHeight = COMPLETE_TITLE_LINE_HEIGHT,
                    letterSpacing = COMPLETE_TITLE_TRACKING,
                ),
                color = contemplative.textBright,
                textAlign = TextAlign.Center,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.widthIn(max = COMPLETE_TITLE_MEASURE),
            )
            Spacer(Modifier.height(ClaritySpacing.scaled(12.dp)))
            DurationAndArea(completion)
            Spacer(Modifier.height(ClaritySpacing.scaled(36.dp)))
            if (completion.canCompleteItem) {
                FocusPill(
                    label = stringResource(R.string.focus_complete_mark_item),
                    onClick = onMarkItemComplete,
                    haptic = ClarityHapticEvent.COMPLETE,
                )
                Spacer(Modifier.height(ClaritySpacing.scaled(6.dp)))
            }
            FocusTextAction(
                label = stringResource(R.string.action_done),
                onClick = onDone,
            )
        }
    }
}

/**
 * The small line. design-v3.md section 11: duration and area, and nothing else.
 *
 * A direct readout of two things the app already holds, which is what keeps it out of
 * the corpus: no interpretation, no comparison, no adjective. The minutes are the real
 * duration rounded the way the Trail rounds it, so the same session does not read as
 * fourteen here and fifteen there.
 *
 * **No dot, and no color anywhere on this screen.** Section 11 asks for one line
 * reading the duration and the area, and the area is already in the words. A 7dp dot
 * here would be an unrequested embellishment on the quietest screen in the app, which
 * is the shape of mistake design-v3.md 15.3 closes with.
 */
@Composable
private fun DurationAndArea(completion: FocusCompletionModel, modifier: Modifier = Modifier) {
    val contemplative = LocalContemplativeColors.current
    val type = LocalClarityTypography.current
    Text(
        text = pluralStringResource(
            R.plurals.focus_complete_duration,
            completion.minutes,
            completion.minutes,
            completion.areaName,
        ),
        style = type.caption,
        color = contemplative.textDim,
        textAlign = TextAlign.Center,
        modifier = modifier,
    )
}

private val COMPLETE_TITLE_SIZE = 26.sp
private val COMPLETE_TITLE_LINE_HEIGHT = 32.sp
private val COMPLETE_TITLE_TRACKING = (-0.024).em
private val COMPLETE_TITLE_MEASURE = 320.dp
