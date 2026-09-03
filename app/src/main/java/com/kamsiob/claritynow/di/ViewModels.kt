package com.kamsiob.claritynow.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.kamsiob.claritynow.ui.areas.AreasViewModel
import com.kamsiob.claritynow.ui.focus.FocusViewModel
import com.kamsiob.claritynow.ui.momentum.AreasBannerViewModel
import com.kamsiob.claritynow.ui.momentum.MomentumViewModel
import com.kamsiob.claritynow.ui.onboarding.OnboardingViewModel
import com.kamsiob.claritynow.ui.pulse.PulseViewModel
import com.kamsiob.claritynow.ui.report.ReportViewModel
import com.kamsiob.claritynow.ui.trail.TrailViewModel
import com.kamsiob.claritynow.ui.tutorial.TutorialViewModel

/**
 * The hand written equivalent of an injected ViewModel factory.
 *
 * One `when` over the requested class, resolved from [ClarityGraph]. It is longer
 * than an annotation but it is readable end to end, which is the trade this project
 * chose deliberately in MASTER_BUILD_PROMPT section 3.
 *
 * **Nothing here reads the creation extras any more.** Three of these branches used to pull
 * the `Application` out of them to reach a coordinator that hung off `ClarityApp` or off a
 * holder object in this file, because the phase that built each surface could not edit
 * [ClarityGraph]. Every one of those now resolves from the graph like everything else, so
 * this factory can be used against a `ViewModelStoreOwner` that supplies no Application at
 * all. The parameter stays because the interface declares it.
 */
object ClarityViewModelFactory : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T =
        when (modelClass) {
            AreasViewModel::class.java -> AreasViewModel(
                repository = ClarityGraph.repository,
                preferences = ClarityGraph.preferences,
                clock = ClarityGraph.clock,
            ) as T

            AreasBannerViewModel::class.java -> AreasBannerViewModel(
                coordinator = ClarityGraph.momentum,
                clock = ClarityGraph.clock,
            ) as T

            FocusViewModel::class.java -> FocusViewModel(
                repository = ClarityGraph.repository,
                preferences = ClarityGraph.preferences,
            ) as T

            // Phase 10. It holds the beats, the transient starter areas and the one
            // write beat 3 makes, MASTER_BUILD_PROMPT 13.1, so it needs both the only
            // writer in the app and the per device flags.
            OnboardingViewModel::class.java -> OnboardingViewModel(
                repository = ClarityGraph.repository,
                preferences = ClarityGraph.preferences,
            ) as T

            // Phase 10. One flag, and it exists so that a composable does not reach a
            // store directly. MASTER_BUILD_PROMPT 5.5.
            TutorialViewModel::class.java -> TutorialViewModel(
                preferences = ClarityGraph.preferences,
            ) as T

            MomentumViewModel::class.java -> MomentumViewModel(
                repository = ClarityGraph.repository,
                coordinator = ClarityGraph.momentum,
            ) as T

            PulseViewModel::class.java -> PulseViewModel(
                repository = ClarityGraph.repository,
                coordinator = ClarityGraph.pulse,
                clock = ClarityGraph.clock,
                preferences = ClarityGraph.preferences,
            ) as T

            ReportViewModel::class.java -> ReportViewModel(
                coordinator = ClarityGraph.report,
            ) as T

            TrailViewModel::class.java -> TrailViewModel(
                repository = ClarityGraph.repository,
                clock = ClarityGraph.clock,
            ) as T

            else -> error("no ViewModel registered for ${modelClass.name}")
        }
}
