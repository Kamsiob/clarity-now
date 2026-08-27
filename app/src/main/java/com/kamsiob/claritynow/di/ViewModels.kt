package com.kamsiob.claritynow.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.kamsiob.claritynow.ui.areas.AreasViewModel
import com.kamsiob.claritynow.ui.focus.FocusViewModel
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

            TrailViewModel::class.java -> TrailViewModel(
                repository = ClarityGraph.repository,
                clock = ClarityGraph.clock,
            ) as T

            else -> error("no ViewModel registered for ${modelClass.name}")
        }
}
