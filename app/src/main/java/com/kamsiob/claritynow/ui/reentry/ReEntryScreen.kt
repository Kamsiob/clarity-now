package com.kamsiob.claritynow.ui.reentry

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kamsiob.claritynow.R
import com.kamsiob.claritynow.ui.components.ClarityButton
import com.kamsiob.claritynow.ui.components.ClarityButtonRole
import com.kamsiob.claritynow.ui.nav.swallowsPointerInput
import com.kamsiob.claritynow.ui.theme.ClaritySpacing
import com.kamsiob.claritynow.ui.theme.LocalClarityColors
import com.kamsiob.claritynow.ui.theme.LocalClarityTypography
import com.kamsiob.claritynow.ui.theme.clarityMotion

/**
 * The screen somebody sees on the day they come back. MASTER_BUILD_PROMPT 14b.4 and
 * design-v3.md 11.2.
 *
 * > A returning user must never be greeted by a measurement of their absence. If a
 * > sentence, a number, a dot row or an empty chart on the first screen back can be
 * > read as a report on how long they were gone, it is wrong, whatever else is true
 * > about it.
 *
 * **This composable takes two callbacks and nothing else, and that is the whole of how
 * the sentence above is held.** It has no state, no ViewModel, no repository and no
 * date: there is no value reaching it that could be counted, subtracted, formatted or
 * rendered, so the prohibition is a fact about the signature rather than a rule
 * somebody has to keep. `ReEntryLanguageTest` reads this file and `strings.xml` and
 * asserts the same thing from the outside.
 *
 * ## Daylight, and why the obvious answer is refused
 *
 * design-v3.md 11.2 settles the world and gives the reason, which is worth repeating
 * here because it is the decision most likely to be undone by somebody polishing:
 *
 * > The obvious answer is a dark ceremonial welcome, a serif line and a soft glow,
 * > which is exactly what this design system is good at and exactly the wrong
 * > instrument. Ceremony says the absence was an event. A quiet Daylight screen says
 * > the app kept the user's place and is ready when they are.
 *
 * So: `canvas`, one `readSerif` line, one body line, two options, and nothing else. No
 * illustration, no mascot, no exclamation mark, per 10.13. No mark, no eyebrow, no
 * card. The Contemplative world is not reachable from here.
 *
 * **"With the Areas structure behind it" is read as structure rather than as a
 * rendering of the Areas screen**, and both halves of that are deliberate. The block
 * starts where the Areas screen's own title starts and keeps its 20dp screen padding
 * and its left alignment, so what a person sees is the room they left with two
 * sentences in it. The app itself genuinely is composed behind this surface, which is
 * what makes 14b.4's "a conflict card waits behind it rather than being dropped" true,
 * but nothing of it shows through: this is opaque, and `ReEntryRoute` is what makes it
 * untouchable.
 *
 * **The block sits at the top and is left aligned, which is the section 15 choice.**
 * The statistically common welcome screen centers a short line in the middle of an
 * empty page with its buttons pinned to the bottom edge, which is a dialog, and a
 * dialog is a demand. Sitting where the screen's own content sits says the app is
 * carrying on rather than staging a moment, and it is the arrangement that makes the
 * screen after this one look like the same screen with the sentences taken out.
 *
 * ## Motion
 *
 * design-v3.md 11.2: item 25's entrance, and nothing else. No iris, no bloom, no
 * transition into a different world. Item 25 is also the right entrance for the reason
 * it exists: its 150ms delay is a guard against a flash during a load that resolves
 * quickly, and the answer this screen waits for is a question asked of the log.
 */
@Composable
fun ReEntryScreen(
    onKeepEverything: () -> Unit,
    onPutItemsBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalClarityColors.current
    val type = LocalClarityTypography.current
    val motion = clarityMotion()

    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { shown = true }

    // design-v3.md 8.2 item 25, written out because `motion.easeOut` is the 350ms
    // entrance curve with no delay and this one is 400ms after a 150ms wait. 8.4 keeps
    // the delay when motion is reduced or calm mode is on and shortens only the fade,
    // because shortening a fade is motion and removing the delay would reintroduce the
    // flash the delay exists to prevent. This is the same expression `AreasScreen` uses
    // for the same catalogue entry.
    val entrance = tween<Float>(
        durationMillis = if (motion.reduced) 150 else 400,
        delayMillis = 150,
        easing = EaseOutCubic,
    )

    // The status bar and the navigation bar are read here rather than taken from a
    // Scaffold, as every other full screen surface in this app does, because this one is
    // drawn above the shell and inherits none of its insets.
    val statusBar = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navigationBar = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    AnimatedVisibility(visible = shown, enter = fadeIn(entrance), modifier = modifier) {
        Box(Modifier.fillMaxSize().background(colors.canvas)) {
            // Nothing behind this can be touched. It is a sibling drawn behind the
            // content rather than a scrim over it, so the two buttons keep their taps.
            // The surface is opaque, so this is about the app underneath rather than
            // about anything visible: without it a tap on the empty half of this screen
            // reaches the tab bar the shell is still drawing beneath it.
            Spacer(Modifier.fillMaxSize().swallowsPointerInput())

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    // design-v3.md 13. At the 200 percent cap the two sentences and the
                    // two buttons are taller than a small phone, and a screen whose only
                    // way out is a control below the fold has no way out at all.
                    .verticalScroll(rememberScrollState())
                    .padding(
                        start = ClaritySpacing.screenPadding,
                        end = ClaritySpacing.screenPadding,
                        top = statusBar + TOP_OF_THE_TITLE,
                        bottom = navigationBar + ClaritySpacing.scaled(32.dp),
                    ),
            ) {
                Text(
                    text = stringResource(R.string.reentry_title),
                    style = type.readSerif,
                    color = colors.inkPrimary,
                )
                Spacer(Modifier.height(ClaritySpacing.scaled(10.dp)))
                Text(
                    text = stringResource(R.string.reentry_body),
                    style = type.body,
                    // design-v3.md 3.1 and 13. `inkTertiary` carries no text anywhere in
                    // this app; a second rank under the serif line is `inkSecondary`.
                    color = colors.inkSecondary,
                )
                // design-v3.md 6, section spacing. The options are a second block rather
                // than a continuation of the sentence above them.
                Spacer(Modifier.height(ClaritySpacing.scaled(28.dp)))
                ClarityButton(
                    label = stringResource(R.string.reentry_keep),
                    onClick = onKeepEverything,
                    role = ClarityButtonRole.PRIMARY,
                    modifier = Modifier.fillMaxWidth(),
                )
                // Deliberately not another 28dp. The tertiary control is text only and
                // carries its own 50dp minimum box, per 10.7, so a section sized gap
                // would read as a third block on a screen that has two choices.
                Spacer(Modifier.height(ClaritySpacing.scaled(8.dp)))
                ClarityButton(
                    label = stringResource(R.string.reentry_requeue),
                    onClick = onPutItemsBack,
                    role = ClarityButtonRole.TERTIARY,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

/**
 * Where the Areas screen's own title sits: that list's 8dp of top padding plus the 12dp
 * its header holds above the word `Areas`, design-v3.md 10.1. The first line of this
 * screen lands there, which is the whole of "with the Areas structure behind it".
 */
private val TOP_OF_THE_TITLE = 20.dp
