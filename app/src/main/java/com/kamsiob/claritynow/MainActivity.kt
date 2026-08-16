package com.kamsiob.claritynow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import com.kamsiob.claritynow.ui.theme.ClarityTheme
import com.kamsiob.claritynow.ui.theme.LocalClarityColors
import com.kamsiob.claritynow.ui.theme.LocalClarityTypography

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            ClarityTheme {
                FoundationsPlaceholder()
            }
        }
    }
}

/**
 * Phase 1 ships the foundations, not the screens. This placeholder exists so the
 * build is installable and the theme, fonts and mark can be checked on the device.
 * Phase 2 replaces it with the Areas screen.
 */
@Composable
private fun FoundationsPlaceholder() {
    val colors = LocalClarityColors.current
    val type = LocalClarityTypography.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.canvas)
            .padding(20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(R.drawable.ic_mark),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            colorFilter = ColorFilter.tint(colors.inkPrimary),
            modifier = Modifier.size(62.dp),
        )
        Text(
            text = "Clarity Now",
            style = type.displayTitle,
            color = colors.inkPrimary,
            modifier = Modifier.padding(top = 20.dp),
        )
        Text(
            text = "Version ${BuildConfig.VERSION_NAME} by Kamsiob",
            style = type.caption,
            color = colors.inkTertiary,
            modifier = Modifier.padding(top = 6.dp),
        )
    }
}
