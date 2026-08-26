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
            ClarityTheme(setting = theme) {
                CompositionLocalProvider(LocalClarityHaptics provides haptics) {
                    ClarityShell()
                }
            }
        }
    }
}
