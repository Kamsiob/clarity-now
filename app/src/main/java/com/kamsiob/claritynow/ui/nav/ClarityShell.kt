package com.kamsiob.claritynow.ui.nav

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
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

    Box(modifier = modifier.fillMaxSize().background(colors.canvas)) {
        AnimatedContent(
            targetState = selected,
            transitionSpec = { fadeIn(motion.easeOut()) togetherWith fadeOut(motion.easeOut()) },
            label = "tabContent",
        ) { tab ->
            when (tab) {
                TAB_AREAS -> {
                    val areasViewModel: AreasViewModel =
                        viewModel(factory = ClarityViewModelFactory)
                    AreasRoute(viewModel = areasViewModel)
                }

                TAB_MOMENTUM, TAB_REPORT, TAB_TRAIL -> UnderConstruction()
                else -> UnderConstruction()
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
