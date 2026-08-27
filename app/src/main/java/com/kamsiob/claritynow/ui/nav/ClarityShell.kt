package com.kamsiob.claritynow.ui.nav

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kamsiob.claritynow.R
import com.kamsiob.claritynow.di.ClarityViewModelFactory
import com.kamsiob.claritynow.ui.areas.AreasRoute
import com.kamsiob.claritynow.ui.areas.AreasViewModel
import com.kamsiob.claritynow.ui.components.ClarityTabBar
import com.kamsiob.claritynow.ui.components.TAB_AREAS
import com.kamsiob.claritynow.ui.components.TAB_MOMENTUM
import com.kamsiob.claritynow.ui.components.TAB_REPORT
import com.kamsiob.claritynow.ui.components.TAB_TRAIL
import com.kamsiob.claritynow.ui.components.rememberClarityTabs
import com.kamsiob.claritynow.ui.theme.LocalClarityColors
import com.kamsiob.claritynow.ui.theme.LocalClarityTypography
import com.kamsiob.claritynow.ui.theme.clarityMotion
import com.kamsiob.claritynow.ui.trail.TrailRoute
import com.kamsiob.claritynow.ui.trail.TrailViewModel

/**
 * The app shell. design-v3.md 10.15.
 *
 * Four tabs at the root and nothing hidden: no drawer, no hamburger. Switching
 * tabs crossfades with no slide, because a slide would imply a spatial
 * relationship between them and there is none. These are four views of one set of
 * data rather than four places.
 */
@Composable
fun ClarityShell(modifier: Modifier = Modifier) {
    val colors = LocalClarityColors.current
    val motion = clarityMotion()
    var selected by rememberSaveable { mutableStateOf(TAB_AREAS) }

    // design-v3.md 8.2 entry 24 puts the tab crossfade at 180ms, one of the two places
    // in the document that names a duration rather than a spring. The nearest token,
    // motion.easeOut, is the 350ms entrance curve: nearly twice as long, and an
    // entrance is the wrong idea here, because these four tabs are one set of data
    // seen four ways rather than four places to arrive at. No 180ms token exists to
    // reach for, so the duration is written out, and the reduce motion branch has to
    // be written out with it: design-v3.md 8.3 replaces every animation with a 150ms
    // crossfade, and a literal duration is the one thing that global check cannot see.
    val tabFade: FiniteAnimationSpec<Float> = tween(
        durationMillis = if (motion.reduced) 150 else 180,
        easing = EaseOutCubic,
    )

    val tabs = rememberClarityTabs(
        areasLabel = stringResource(R.string.tab_areas),
        momentumLabel = stringResource(R.string.tab_momentum),
        reportLabel = stringResource(R.string.tab_report),
        trailLabel = stringResource(R.string.tab_trail),
    )

    // Back on a non root tab returns to Areas. Back on Areas leaves the app, with
    // no double tap prompt, which is a pattern that only exists where navigation
    // is confusing.
    BackHandler(enabled = selected != TAB_AREAS) { selected = TAB_AREAS }

    // A tab's content leaves composition when another tab is selected, which throws
    // away everything it had remembered, scroll position first among them. The Trail
    // keeps its loaded pages in a ViewModel that outlives the switch, so without this
    // a person who paged a month back would return to the top of a list that still
    // holds the month. The holder gives each tab its own saveable slot, keyed by the
    // tab, so every screen comes back where it was left.
    val tabStates = rememberSaveableStateHolder()

    Box(modifier = modifier.fillMaxSize().background(colors.canvas)) {
        AnimatedContent(
            targetState = selected,
            transitionSpec = { fadeIn(tabFade) togetherWith fadeOut(tabFade) },
            label = "tabContent",
        ) { tab ->
            tabStates.SaveableStateProvider(tab) {
                when (tab) {
                    TAB_AREAS -> {
                        val areasViewModel: AreasViewModel =
                            viewModel(factory = ClarityViewModelFactory)
                        AreasRoute(viewModel = areasViewModel)
                    }

                    TAB_TRAIL -> {
                        val trailViewModel: TrailViewModel =
                            viewModel(factory = ClarityViewModelFactory)
                        TrailRoute(viewModel = trailViewModel)
                    }

                    TAB_MOMENTUM, TAB_REPORT -> UnderConstruction()
                    else -> UnderConstruction()
                }
            }
        }

        ClarityTabBar(
            tabs = tabs,
            selectedKey = selected,
            onSelect = { selected = it },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 17.dp),
        )
    }
}

/**
 * A destination whose phase has not landed yet. Deliberately plain and honest
 * rather than a fake skeleton, because a convincing placeholder is a lie about
 * what has been built.
 */
@Composable
private fun UnderConstruction() {
    val colors = LocalClarityColors.current
    val type = LocalClarityTypography.current
    Box(
        modifier = Modifier.fillMaxSize().padding(40.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.under_construction),
            style = type.body,
            color = colors.inkTertiary,
            textAlign = TextAlign.Center,
        )
    }
}
