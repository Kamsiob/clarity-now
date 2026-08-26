package com.kamsiob.claritynow.ui.theme

import android.provider.Settings
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** The three appearance choices. System is the default. */
enum class ClarityThemeSetting { LIGHT, DARK, SYSTEM }

/**
 * design-v3.md 6.1. Elevation is one separation device expressed as a paired
 * shadow, never two devices. Dark and Contemplative surfaces carry no shadow at
 * all; there, depth is lightness only.
 */
@Immutable
data class ShadowLayer(val offsetY: Dp, val blur: Dp, val color: Color)

object ClarityElevation {
    val card = listOf(
        ShadowLayer(1.dp, 3.dp, Color.Black.copy(alpha = 0.04f)),
        ShadowLayer(6.dp, 20.dp, Color.Black.copy(alpha = 0.05f)),
    )
    val tabBar = listOf(
        ShadowLayer(2.dp, 10.dp, Color.Black.copy(alpha = 0.07f)),
        ShadowLayer(10.dp, 30.dp, Color.Black.copy(alpha = 0.08f)),
    )
    val sheet = listOf(
        ShadowLayer((-8).dp, 40.dp, Color.Black.copy(alpha = 0.28f)),
    )

    fun fab(actionBlue: Color) = listOf(
        ShadowLayer(5.dp, 16.dp, actionBlue.copy(alpha = 0.40f)),
    )
}

/**
 * The Daylight world theme.
 *
 * Material You dynamic color is explicitly not used. The color scheme below exists
 * only so Material 3 components that read from it do not fall back to the default
 * purple; every color the app actually draws comes from [LocalClarityColors].
 */
@Composable
fun ClarityTheme(
    setting: ClarityThemeSetting = ClarityThemeSetting.SYSTEM,
    content: @Composable () -> Unit,
) {
    val systemDark = isSystemInDarkTheme()
    val dark = when (setting) {
        ClarityThemeSetting.LIGHT -> false
        ClarityThemeSetting.DARK -> true
        ClarityThemeSetting.SYSTEM -> systemDark
    }
    val colors = if (dark) ClarityDarkColors else ClarityLightColors
    val context = LocalContext.current
    val reduceMotion = remember(context) { shouldReduceMotion(context) }

    CompositionLocalProvider(
        LocalClarityColors provides colors,
        LocalContemplativeColors provides ClarityContemplativeColors,
        LocalClarityTypography provides ClarityTypeScale,
        LocalClarityShapes provides ClarityShapeScale,
        LocalReduceMotion provides reduceMotion,
    ) {
        MaterialTheme(
            colorScheme = materialSchemeFor(colors, dark),
            shapes = materialShapesFor(ClarityShapeScale),
            typography = materialTypographyFor(ClarityTypeScale),
            content = content,
        )
    }
}

/**
 * The Contemplative world: Focus, Pulse, Clarity Report and Onboarding.
 *
 * Always dark regardless of the theme setting, and scoped separately rather than
 * conditionally, so it can never be accidentally inverted. design-v3.md section 2.
 */
@Composable
fun ContemplativeTheme(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val reduceMotion = remember(context) { shouldReduceMotion(context) }
    val contemplative = ClarityContemplativeColors

    CompositionLocalProvider(
        LocalContemplativeColors provides contemplative,
        LocalClarityTypography provides ClarityTypeScale,
        LocalClarityShapes provides ClarityShapeScale,
        LocalReduceMotion provides reduceMotion,
    ) {
        MaterialTheme(
            colorScheme = darkColorScheme(
                primary = contemplative.textBright,
                onPrimary = contemplative.deepBlack,
                background = contemplative.deepBlack,
                onBackground = contemplative.textBright,
                surface = contemplative.surfaceRaised,
                onSurface = contemplative.textBright,
            ),
            shapes = materialShapesFor(ClarityShapeScale),
            typography = materialTypographyFor(ClarityTypeScale),
            content = content,
        )
    }
}

private fun materialSchemeFor(colors: ClarityColors, dark: Boolean) = if (dark) {
    darkColorScheme(
        primary = colors.actionBlue,
        onPrimary = Color.White,
        background = colors.canvas,
        onBackground = colors.inkPrimary,
        surface = colors.card,
        onSurface = colors.inkPrimary,
        surfaceVariant = colors.raise,
        onSurfaceVariant = colors.inkSecondary,
        outline = colors.hairline,
    )
} else {
    lightColorScheme(
        primary = colors.actionBlue,
        onPrimary = Color.White,
        background = colors.canvas,
        onBackground = colors.inkPrimary,
        surface = colors.card,
        onSurface = colors.inkPrimary,
        surfaceVariant = colors.raise,
        onSurfaceVariant = colors.inkSecondary,
        outline = colors.hairline,
    )
}

/**
 * Material's own shape scale, filled from the Clarity radii so an expressive
 * component that reaches for a corner size lands on one this design already uses.
 */
private fun materialShapesFor(shapes: ClarityShapes) = androidx.compose.material3.Shapes(
    extraSmall = shapes.settingsBadge,
    small = shapes.widgetInner,
    medium = shapes.momentumTile,
    large = shapes.card,
    extraLarge = shapes.sheet,
)

/**
 * Material components that reach for a text style get one of ours rather than
 * Roboto. Nothing in this app should ever render in the platform default face.
 */
private fun materialTypographyFor(type: ClarityTypography) = Typography(
    displayLarge = type.displayHero,
    displayMedium = type.displayTitle,
    displaySmall = type.displayTitle,
    headlineLarge = type.readSerif,
    headlineMedium = type.readSerif,
    headlineSmall = type.closingLine,
    titleLarge = type.title,
    titleMedium = type.title,
    titleSmall = type.bodyStrong,
    bodyLarge = type.body,
    bodyMedium = type.body,
    bodySmall = type.caption,
    labelLarge = type.label,
    labelMedium = type.label,
    labelSmall = type.caption,
)

/**
 * design-v3.md 8.3. One global check. The animator duration scale is the setting
 * Android actually exposes for this, and developer options set it to zero for the
 * same reason an accessibility user would.
 */
private fun shouldReduceMotion(context: android.content.Context): Boolean {
    val scale = Settings.Global.getFloat(
        context.contentResolver,
        Settings.Global.ANIMATOR_DURATION_SCALE,
        1f,
    )
    return scale == 0f
}
