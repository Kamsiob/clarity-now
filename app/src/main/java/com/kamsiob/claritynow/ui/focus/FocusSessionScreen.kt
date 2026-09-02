package com.kamsiob.claritynow.ui.focus

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.kamsiob.claritynow.R
import com.kamsiob.claritynow.data.repo.FocusCountdown
import com.kamsiob.claritynow.data.repo.focusEndingIsSilent
import com.kamsiob.claritynow.ui.theme.ClaritySpacing
import com.kamsiob.claritynow.ui.theme.LocalClarityTypography
import com.kamsiob.claritynow.ui.theme.LocalContemplativeColors
import com.kamsiob.claritynow.ui.theme.parseAreaColor

/**
 * A running session. design-v3.md section 11.
 *
 * **Six elements only, and nothing else, ever:** the area label with its dot, the item
 * title in bold sans at 26sp, the 240dp ring with the timer numeral, the word
 * `remaining` beneath the numeral, the End session pill, and `Add 10 minutes` beneath
 * it as a tertiary control. The sixth is Addendum 01 4f's addition and is recorded in
 * section 11 as a change rather than assumed; the hierarchy did not move with it.
 *
 * **Back is not handled here.** design-v3.md 10.15: back navigates away and leaves the
 * session running, so it is the caller's ordinary back, and there is nothing on this
 * screen for it to call. The ongoing notification is the way back in and the Areas card
 * shows the live countdown while this screen is gone.
 *
 * [countdown] is a function rather than a value on purpose. It is read inside the dial,
 * which is the only thing on this screen that redraws once a second, and inside the two
 * click handlers, where a read registers no recomposition at all. design-v3.md 8.2
 * item 7.
 */
@Composable
internal fun FocusSessionScreen(
    session: FocusSessionModel,
    countdown: () -> FocusCountdown?,
    transitionWarningEnabled: Boolean,
    onEnd: (elapsedSeconds: Int) -> Unit,
    onExtend: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contemplative = LocalContemplativeColors.current
    val type = LocalClarityTypography.current
    var confirming by remember { mutableStateOf(false) }

    // MASTER_BUILD_PROMPT section 10: the screen stays on for the duration of a
    // session. Only while one is running: the chooser and the completion screen let the
    // display do what the person's own settings say.
    val view = LocalView.current
    DisposableEffect(view) {
        view.keepScreenOn = true
        onDispose { view.keepScreenOn = false }
    }

    // The insets sit on the Box rather than on the scrolling column, so the content
    // is centered inside the space the system bars leave and the backdrop behind it,
    // which is a sibling, still reaches every edge.
    Box(modifier = modifier.fillMaxSize().safeDrawingPadding()) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = ClaritySpacing.screenPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AreaLabel(name = session.areaName, colorHex = session.colorHex)
            Spacer(Modifier.height(ClaritySpacing.scaled(14.dp)))
            Text(
                // design-v3.md section 11 gives this its own size, 26sp, which is not a
                // role in 5.3. The weight is `itemTitle`'s, because that is what bold
                // sans means in this app, and the tracking is the value the sans ramp
                // in 5.3 reaches between 21sp at -0.022em and 64sp at -0.030em.
                text = session.itemTitle,
                style = type.itemTitle.copy(
                    fontSize = TITLE_SIZE,
                    lineHeight = TITLE_LINE_HEIGHT,
                    letterSpacing = TITLE_TRACKING,
                ),
                color = contemplative.textBright,
                textAlign = TextAlign.Center,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.widthIn(max = TITLE_MEASURE),
            )
            // **The first step, under the title, while the ring runs.** `ADDENDUM_01` 4b
            // and issue #62: the field exists so that a person who cannot start has
            // already written down how to, and a ring counting down over a title alone is
            // the moment somebody who has lost the thread is looking at. It is text with
            // no container, so the one-turn law holds; it is `caption` on `textDim`, one
            // rank under the title, because the title is what the session is about and
            // this is how to begin it.
            session.itemFirstStep?.let { firstStep ->
                Spacer(Modifier.height(ClaritySpacing.scaled(10.dp)))
                Text(
                    text = firstStep,
                    style = type.caption,
                    color = contemplative.textDim,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.widthIn(max = TITLE_MEASURE),
                )
            }
            Spacer(Modifier.height(ClaritySpacing.scaled(36.dp)))
            FocusDial(
                countdown = countdown,
                plannedSecondsFallback = session.plannedSeconds,
                transitionWarningEnabled = transitionWarningEnabled,
            )
            Spacer(Modifier.height(ClaritySpacing.scaled(36.dp)))
            FocusPill(
                label = stringResource(R.string.focus_end_session),
                onClick = {
                    val elapsed = countdown()?.elapsedSeconds ?: 0
                    // Under a minute this is a mis-tap rather than a short session, so
                    // it goes without a question and without a completion screen. The
                    // same function decides both halves, in `data/repo/FocusSession.kt`,
                    // so the threshold is never written down twice.
                    if (focusEndingIsSilent(elapsed)) onEnd(elapsed) else confirming = true
                },
            )
            Spacer(Modifier.height(ClaritySpacing.scaled(6.dp)))
            FocusTextAction(
                // design-v3.md 10.18: no confirmation, no toast, no acknowledgment
                // beyond the ring itself. Repeatable, and there is no limit, because a
                // limit is an argument with someone who is working.
                label = stringResource(R.string.focus_add_ten_minutes),
                onClick = onExtend,
            )
        }
    }

    if (confirming) {
        EndSessionConfirm(
            onEnd = {
                confirming = false
                onEnd(countdown()?.elapsedSeconds ?: 0)
            },
            onKeepGoing = { confirming = false },
        )
    }
}

/** The first of the six elements. design-v3.md section 11. */
@Composable
private fun AreaLabel(name: String, colorHex: String, modifier: Modifier = Modifier) {
    val contemplative = LocalContemplativeColors.current
    val type = LocalClarityTypography.current
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(ClaritySpacing.areaDot)
                .clip(CircleShape)
                // Excluded from calm mode's transform by name, design-v3.md 16.2: the
                // dot is how an area is recognized.
                .background(parseAreaColor(colorHex)),
        )
        Spacer(Modifier.size(width = 9.dp, height = 1.dp))
        Text(
            text = name,
            style = type.label,
            color = contemplative.textDim,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/**
 * design-v3.md section 10: past sixty seconds, a small confirm reading `End this
 * session?` with `End` and `Keep going`.
 *
 * The platform dialog, themed, which is step 2 of design-v3.md 17.1: it arrives with
 * predictive back, its own scrim, focus handling and a screen reader contract already
 * correct, and 17.3 does not list a dialog among the six things this app builds itself.
 * Two values are set explicitly because the theme's defaults are wrong for this shape:
 * the corner, since Material's extra large shape is this app's sheet corner and would
 * round only the top two, and the label size, since Material's action label is 13sp.
 *
 * **Both actions carry the same weight, and that is the deliberate choice.** The
 * obvious answer is a filled confirm and a quiet dismiss, which would either recommend
 * carrying on or dress ending up as a destructive act. Ending is neither: Addendum 01
 * 4e makes a session ended early a completed short session, so nothing on this dialog
 * may push a person toward one answer. Neither is styled as the recommendation, and
 * neither carries the `warn` haptic, which design-v3.md 9 scopes to a destructive
 * confirmation arming. Section 15.
 */
@Composable
private fun EndSessionConfirm(onEnd: () -> Unit, onKeepGoing: () -> Unit) {
    val contemplative = LocalContemplativeColors.current
    val type = LocalClarityTypography.current
    AlertDialog(
        onDismissRequest = onKeepGoing,
        title = {
            Text(
                text = stringResource(R.string.focus_end_confirm_title),
                style = type.title,
                color = contemplative.textBright,
            )
        },
        confirmButton = {
            TextButton(onClick = onEnd) {
                Text(
                    text = stringResource(R.string.focus_end_confirm_end),
                    style = type.bodyStrong,
                    color = contemplative.textBright,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onKeepGoing) {
                Text(
                    text = stringResource(R.string.focus_end_confirm_keep),
                    style = type.bodyStrong,
                    color = contemplative.textBright,
                )
            }
        },
        containerColor = contemplative.surfaceRaised,
        // Zero, because `surfaceRaised` is also this theme's `surface`, and Material
        // would otherwise tint it a second time for elevation. The Contemplative world
        // has no shadows and its depth is lightness only. design-v3.md 6.1.
        tonalElevation = 0.dp,
        shape = RoundedCornerShape(18.dp),
    )
}

private val TITLE_SIZE = 26.sp
private val TITLE_LINE_HEIGHT = 32.sp
private val TITLE_TRACKING = (-0.024).em

/** Wide enough for three lines of 26sp and narrow enough that they are readable. */
private val TITLE_MEASURE = 320.dp
