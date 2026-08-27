package com.kamsiob.claritynow.di

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.kamsiob.claritynow.ClarityApp
import com.kamsiob.claritynow.domain.pulse.PulseCoordinator
import com.kamsiob.claritynow.ui.areas.AreasViewModel
import com.kamsiob.claritynow.ui.focus.FocusViewModel
import com.kamsiob.claritynow.ui.pulse.PulseViewModel
import com.kamsiob.claritynow.ui.trail.TrailViewModel

/**
 * The hand written equivalent of an injected ViewModel factory.
 *
 * One `when` over the requested class, resolved from [ClarityGraph]. It is longer
 * than an annotation but it is readable end to end, which is the trade this project
 * chose deliberately in MASTER_BUILD_PROMPT section 3.
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

            FocusViewModel::class.java -> FocusViewModel(
                repository = ClarityGraph.repository,
                preferences = ClarityGraph.preferences,
            ) as T

            PulseViewModel::class.java -> PulseViewModel(
                repository = ClarityGraph.repository,
                coordinator = pulseCoordinator(extras),
                clock = ClarityGraph.clock,
            ) as T

            TrailViewModel::class.java -> TrailViewModel(
                repository = ClarityGraph.repository,
                clock = ClarityGraph.clock,
            ) as T

            else -> error("no ViewModel registered for ${modelClass.name}")
        }

    /**
     * The one Pulse lifecycle for the process, reached through the Application.
     *
     * **It belongs in [ClarityGraph] beside the repository and the clock**, and it is on
     * `ClarityApp` because the slice that built the lifecycle could not edit that file.
     * The catalog inside it parses three markdown files and runs its integrity checks, so
     * there must be exactly one of them for the process, per MASTER_BUILD_PROMPT 11.7:
     * building a second here would be a second catalog and a second parse on the first
     * open of the sheet. When the binding moves, this function becomes
     * `ClarityGraph.pulse` and the [CreationExtras] parameter goes with it.
     *
     * The Application arrives in the creation extras, which a `ComponentActivity`
     * populates and a bare `ViewModelStoreOwner` does not. That is why `PulseRoute`
     * deliberately takes the ordinary store rather than one of its own, and why the error
     * below says so rather than reading `no ViewModel registered`.
     */
    private fun pulseCoordinator(extras: CreationExtras): PulseCoordinator {
        val key = ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY
        val application: Application? = extras[key]
        val app = application as? ClarityApp
        return checkNotNull(app?.pulse) {
            "the Pulse ViewModel was built without the Application in its creation extras, " +
                "which means it was resolved against a ViewModelStoreOwner that supplies none"
        }
    }
}
