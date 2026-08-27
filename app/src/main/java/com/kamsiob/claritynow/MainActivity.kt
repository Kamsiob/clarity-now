package com.kamsiob.claritynow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kamsiob.claritynow.di.ClarityGraph
import com.kamsiob.claritynow.ui.nav.ClarityShell
import com.kamsiob.claritynow.ui.theme.ClarityHaptics
import com.kamsiob.claritynow.ui.theme.ClarityTheme
import com.kamsiob.claritynow.ui.theme.ClarityThemeSetting
import com.kamsiob.claritynow.ui.theme.LocalClarityHaptics
import androidx.compose.runtime.CompositionLocalProvider

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val haptics: ClarityHaptics = ClarityGraph.haptics
        setContent {
            val theme by ClarityGraph.preferences.theme
                .collectAsStateWithLifecycle(initialValue = ClarityThemeSetting.SYSTEM)
            // Null means the person has never touched the switch, which is a storage
            // state rather than an interface state: while it is null, calm mode
            // follows the system reduce motion setting, which design-v3.md 16.1 makes
            // the default. The theme resolves that, not this call site.
            val calmMode by ClarityGraph.preferences.calmMode
                .collectAsStateWithLifecycle(initialValue = null)
            ClarityTheme(setting = theme, calmMode = calmMode) {
                CompositionLocalProvider(LocalClarityHaptics provides haptics) {
                    ClarityShell()
                }
            }
        }
    }
}
