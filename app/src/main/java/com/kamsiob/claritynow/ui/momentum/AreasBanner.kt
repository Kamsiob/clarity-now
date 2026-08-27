package com.kamsiob.claritynow.ui.momentum

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kamsiob.claritynow.di.ClarityViewModelFactory
import com.kamsiob.claritynow.ui.theme.LocalClarityColors
import com.kamsiob.claritynow.ui.theme.LocalClarityTypography
import com.kamsiob.claritynow.ui.theme.clarityMotion

/**
 * The weekly banner on the Areas screen. design-v3.md 10.2 and `CORPUS_3_MOMENTUM.md`.
 *
 * "Full width, parchment, 14dp radius, no border, no progress track. A bodyStrong sentence
 * and a caption line, both from the Logic Engine."
 *
 * ## Why it arrives now and not in phase 2
 *
 * `docs/BUILD_STATE.md` records this element as deliberately absent since phase 2, and the
 * reason is the one rule everything follows from: its sentence comes from the engine,
 * through a corpus, and neither existed until phase 5 and 6. A banner shipped earlier
 * would have had a sentence written in a composable, which `MASTER_BUILD_PROMPT.md` 11.1
 * forbids outright, or a fixed line in `strings.xml`, which is the same thing with a
 * resource id in front of it. This is its arrival, and the sentence and the caption are
 * both authored corpus lines.
 *
 * ## The throttle is in the ViewModel and this calls into it on every entry
 *
 * `CLARITY_LOGIC_ENGINE.md` 6.5: at most once per hour of app use, in the ViewModel and
 * not in the engine. [AreasBannerViewModel.refresh] is a lock, a subtraction and a return
 * when the hour has not passed, which is what makes it safe to call from a
 * `LaunchedEffect` that fires on every entry to the Areas tab. **Nothing here recomputes
 * on recomposition and nothing here collects the projection**, which is issue #5's second
 * named risk.
 *
 * ## No banner at all, rather than a banner with nothing in it
 *
 * The engine is entitled to say nothing, and it says nothing on a week no family
 * describes. The block is then absent and takes no space, which is the same answer the
 * inbox chip gives an empty inbox: an entry point to nothing is worse than no entry point.
 *
 * ## One separation device
 *
 * Parchment against the canvas is a lightness and temperature shift, which is device two
 * of design-v3.md 6.1, so there is no border and no shadow. 10.2 says no border in as many
 * words, and 6.1 forbids pairing either of the others with it.
 */
@Composable
fun AreasBanner(
    modifier: Modifier = Modifier,
    viewModel: AreasBannerViewModel = viewModel(factory = ClarityViewModelFactory),
) {
    val colors = LocalClarityColors.current
    val type = LocalClarityTypography.current
    val motion = clarityMotion()
    val banner by viewModel.banner.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.refresh() }

    // A fade rather than a slide or a height animation. The block resolves after a read of
    // the log, so it arrives a moment after the screen around it, and design-v3.md 8.2 item
    // 4 makes a fade the app's vocabulary for something arriving. There is no exit
    // animation because there is no path that removes it: the throttle only ever replaces
    // one sentence with another.
    AnimatedVisibility(visible = banner != null, enter = fadeIn(motion.easeOut())) {
        banner?.let { current ->
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(BANNER_RADIUS))
                    .background(colors.parchment)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
            ) {
                Text(text = current.sentence, style = type.bodyStrong, color = colors.inkPrimary)
                current.caption?.let { caption ->
                    Spacer(Modifier.height(4.dp))
                    Text(text = caption, style = type.caption, color = colors.inkSecondary)
                }
            }
        }
    }
}

/** design-v3.md 10.2. Not the card's 18dp and not a row's 12dp; the banner has its own. */
private val BANNER_RADIUS = 14.dp
