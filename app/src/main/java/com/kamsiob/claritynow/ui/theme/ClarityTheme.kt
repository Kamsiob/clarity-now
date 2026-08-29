package com.kamsiob.claritynow.ui.theme

import android.content.ContentResolver
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
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
    /**
     * design-v3.md 6.1's sheet value, **and it has no call site, which phase 12b settled
     * rather than fixed.**
     *
     * A sheet's shadow points up, out of its own top edge, so it can only be drawn by
     * something outside the sheet. In `ModalBottomSheet` there is nothing outside it that
     * an app can reach. The caller's `modifier` is the outermost link of a chain that
     * ends `.draggableAnchors(...).anchoredDraggable(...)` before the `Surface`, and
     * those place the sheet's content at an offset **inside** a node that stays at the
     * top of the window, so a shadow attached to the caller's modifier draws at the top
     * of the screen rather than at the sheet's edge. `SheetState` exposes the offset only
     * through `requireOffset`, which throws before the anchors exist. The parameter list
     * carries `shape`, `containerColor`, `contentColor`, `tonalElevation` and
     * `scrimColor`, and no shadow of any kind. Verified against the shipped
     * `material3-android` rather than from memory.
     *
     * design-v3.md 17.4 says a polish pass never reimplements a working platform
     * component in order to change how it looks, and that anything unreachable through
     * theming is raised as a decision instead of done quietly. This is that record, and
     * it is the second entry of its kind against this component: 16.8 already carries the
     * one about calm mode.
     *
     * **What separates a sheet is the scrim, and it always was.** A `card` at L* 98.6
     * sits on a ground the 42 percent scrim has taken to roughly L* 56, which is a 42
     * point step. design-v3.md 6.1 stops at the first device that reads and this reads at
     * the second, so the shadow was the third device on an element that already had one.
     * The value is kept because a later Material release may expose a hook, and because
     * the number is worth more attached to this analysis than in a commit message.
     */
    val sheet = listOf(
        ShadowLayer((-8).dp, 40.dp, Color.Black.copy(alpha = 0.28f)),
    )

    /**
     * **Two layers of the action color, not one halo of it.**
     *
     * A single 16dp blur at 40 percent of a saturated blue is a glow, and a glow around
     * a button is the most legible generated-interface tell on a screen that otherwise
     * has none. Measured on a device capture it put a visible blue field 16dp out from
     * every edge of the control. The replacement is the card's own two layer shadow
     * shape, tinted: a tight contact layer that seats the control on the page, and a
     * wider ambient layer at a third of the old opacity that gives it height without
     * lighting the canvas.
     */
    fun fab(actionBlue: Color) = listOf(
        ShadowLayer(2.dp, 4.dp, actionBlue.copy(alpha = 0.22f)),
        ShadowLayer(8.dp, 18.dp, actionBlue.copy(alpha = 0.14f)),
    )
}

/**
 * The Daylight world theme.
 *
 * Material You dynamic color is explicitly not used. The color scheme below exists
 * only so Material 3 components that read from it do not fall back to the default
 * purple; every color the app actually draws comes from [LocalClarityColors].
 *
 * **[textSize] multiplies the OS font scale rather than replacing it**, and is applied
 * here by overriding [LocalDensity] rather than by rewriting the type scale, so every sp
 * in the app moves together and the combined figure is the one `fontScale` reports. The
 * reasoning, the 200 percent cap and why the platform's own sp curve is preserved are
 * all in `ClarityTextSize.kt` and design-v3.md 13. `ContemplativeTheme` deliberately
 * does not repeat any of it: it is always composed inside this theme, so it inherits the
 * scaled density, and applying the factor a second time would square it.
 *
 * **[calmMode] is null until the user has touched the switch**, and while it is null
 * calm mode follows the system reduce-motion setting, live, per design-v3.md 16.1. That
 * is why the parameter is nullable rather than defaulting to `false`: a `false` default
 * would silently mean "off" for every caller that has not been taught about the setting
 * yet, and the specified default is not "off", it is "whatever the system asks for".
 * [resolveCalmMode] holds that rule in one place so a test can assert both halves of it.
 */
@Composable
fun ClarityTheme(
    setting: ClarityThemeSetting = ClarityThemeSetting.SYSTEM,
    calmMode: Boolean? = null,
    textSize: ClarityTextSize = ClarityTextSize.DEFAULT,
    content: @Composable () -> Unit,
) {
    val systemDark = isSystemInDarkTheme()
    val dark = when (setting) {
        ClarityThemeSetting.LIGHT -> false
        ClarityThemeSetting.DARK -> true
        ClarityThemeSetting.SYSTEM -> systemDark
    }
    val reduceMotion = rememberSystemReduceMotion()
    val calm = resolveCalmMode(calmMode, reduceMotion)
    val colors = remember(dark, calm) {
        val world = if (dark) ClarityDarkColors else ClarityLightColors
        if (calm) world.calmed() else world
    }
    // The one place the app's own text size is applied, and the reason it is applied to
    // the density rather than to the fourteen roles in ClarityTypeScale. Every sp in the
    // app goes through this, including the ones inside a Material component this project
    // did not write, and `density.fontScale` then reports the combined figure, so
    // design-v3.md 5.3's cap on the timer numeral reads the number it is supposed to be
    // capping with nothing added at the call site. ClarityTextSize.kt carries the rest.
    val systemDensity = LocalDensity.current
    val scaledDensity = systemDensity.withTextSize(textSize)

    CompositionLocalProvider(
        LocalClarityColors provides colors,
        LocalContemplativeColors provides ClarityContemplativeColors,
        LocalClarityTypography provides ClarityTypeScale,
        LocalClarityShapes provides ClarityShapeScale,
        LocalReduceMotion provides reduceMotion,
        LocalCalmMode provides calm,
        LocalClarityTextSize provides textSize,
        LocalSystemFontScale provides systemDensity.fontScale,
        LocalDensity provides scaledDensity,
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
fun ContemplativeTheme(
    calmMode: Boolean? = null,
    content: @Composable () -> Unit,
) {
    val reduceMotion = rememberSystemReduceMotion()
    val calm = resolveCalmMode(calmMode, reduceMotion)
    val contemplative = ClarityContemplativeColors

    CompositionLocalProvider(
        LocalContemplativeColors provides contemplative,
        LocalClarityTypography provides ClarityTypeScale,
        LocalClarityShapes provides ClarityShapeScale,
        LocalReduceMotion provides reduceMotion,
        LocalCalmMode provides calm,
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

/**
 * `surfaceVariant` takes `raise`, which since phase 3c is a real value with real call
 * sites rather than a token nothing drew: the floating tab bar and an unselected chip
 * sit there, one rank under the content they serve. A Material component that reaches
 * for `surfaceVariant` therefore lands on the app's chrome value, which is what such a
 * component almost always is.
 */
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
    extraSmall = shapes.segment,
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
    headlineSmall = type.readSerif,
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
 * design-v3.md 8.3. One global check, and it is live.
 *
 * The animator duration scale is the setting Android actually exposes for this, and
 * developer options set it to zero for the same reason an accessibility user would.
 *
 * **It is observed rather than read once**, which phase 3b changed, because
 * design-v3.md 16.1 says calm mode follows the system setting "live, with no restart"
 * while the user has never touched the switch. Read once at composition, a person who
 * turned the system setting on, opened this app to see the difference and found none
 * would reasonably conclude the app ignores it.
 */
@Composable
private fun rememberSystemReduceMotion(): Boolean {
    val resolver = LocalContext.current.contentResolver
    return produceState(initialValue = animationsAreOff(resolver), resolver) {
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                value = animationsAreOff(resolver)
            }
        }
        resolver.registerContentObserver(
            Settings.Global.getUriFor(Settings.Global.ANIMATOR_DURATION_SCALE),
            false,
            observer,
        )
        awaitDispose { resolver.unregisterContentObserver(observer) }
    }.value
}

private fun animationsAreOff(resolver: ContentResolver): Boolean {
    val scale = Settings.Global.getFloat(
        resolver,
        Settings.Global.ANIMATOR_DURATION_SCALE,
        1f,
    )
    return scale == 0f
}
