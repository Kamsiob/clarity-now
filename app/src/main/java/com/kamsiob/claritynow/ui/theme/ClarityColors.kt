package com.kamsiob.claritynow.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * The Daylight world palette, design-v3.md sections 3.1 and 3.2.
 *
 * Wash alphas are carried here rather than computed at the call site so that the
 * "one separation device" rule in design-v3.md 6.1 has a single place to be read
 * from. No composable may invent an opacity for an area accent.
 */
@Immutable
data class ClarityColors(
    val isDark: Boolean,
    val canvas: Color,
    val card: Color,
    val raise: Color,
    val inkPrimary: Color,
    val inkSecondary: Color,
    val inkTertiary: Color,
    val hairline: Color,
    val actionBlue: Color,
    val positiveGreen: Color,
    val warnAmber: Color,
    val parchment: Color,
    val deleteMuted: Color,
    /** Area accent opacity for a resting card. */
    val cardWashAlpha: Float,
    /** Area accent opacity for a card whose area has a running focus session. */
    val cardWashActiveAlpha: Float,
)

private val InkLight = Color(0xFF17171C)
private val InkDark = Color(0xFFF0EEF1)

val ClarityLightColors = ClarityColors(
    isDark = false,
    canvas = Color(0xFFF1F1F6),
    card = Color(0xFFFFFFFF),
    raise = Color(0xFFFAFAFC),
    inkPrimary = InkLight,
    inkSecondary = InkLight.copy(alpha = 0.60f),
    inkTertiary = InkLight.copy(alpha = 0.38f),
    hairline = InkLight.copy(alpha = 0.08f),
    actionBlue = Color(0xFF2D7FF9),
    positiveGreen = Color(0xFF22C55E),
    warnAmber = Color(0xFFF59E0B),
    parchment = Color(0xFFEFEEE2),
    deleteMuted = Color(0xFF8A5A5A),
    cardWashAlpha = 0.06f,
    cardWashActiveAlpha = 0.13f,
)

val ClarityDarkColors = ClarityColors(
    isDark = true,
    canvas = Color(0xFF0E0E13),
    card = Color(0xFF191921),
    raise = Color(0xFF15151C),
    inkPrimary = InkDark,
    inkSecondary = InkDark.copy(alpha = 0.62f),
    inkTertiary = InkDark.copy(alpha = 0.38f),
    hairline = Color.White.copy(alpha = 0.09f),
    actionBlue = Color(0xFF4DA3FF),
    positiveGreen = Color(0xFF22C55E),
    warnAmber = Color(0xFFF59E0B),
    parchment = Color(0xFF211F16),
    deleteMuted = Color(0xFF8A5A5A),
    cardWashAlpha = 0.08f,
    cardWashActiveAlpha = 0.16f,
)

/**
 * The Contemplative world, design-v3.md section 3.3. Always dark. Held in its own
 * type so it can never be reached through the Daylight colors and therefore can
 * never be accidentally inverted by the theme setting.
 */
@Immutable
data class ContemplativeColors(
    val deepBlack: Color = Color(0xFF0B0B10),
    val surfaceRaised: Color = Color(0xFF14141C),
    val textBright: Color = Color(0xFFF3F1EC),
    val textDim: Color = Color(0xFFF3F1EC).copy(alpha = 0.55f),
    val textFaint: Color = Color(0xFFF3F1EC).copy(alpha = 0.32f),
)

val ClarityContemplativeColors = ContemplativeColors()

/** Focus, the indigo night. design-v3.md 3.3. */
object FocusPalette {
    val gradientCenter = Color(0xFF262A5E)
    val gradientMid = Color(0xFF191C42)
    val gradientEdge = Color(0xFF10122B)
    val ringTrack = Color.White.copy(alpha = 0.16f)
    val ringProgress = Color(0xFF8BA4FF)
    val ringTip = Color(0xFFB9C8FF)
}

/** Pulse, the amber night. The time of day tints are felt rather than noticed. */
object PulsePalette {
    val accent = Color(0xFFE8A15C)
    val dawnTint = Color(0xFF2B2340)
    val eveningTint = Color(0xFF2E1F14)
}

/** Clarity Report, gold editorial. */
object ReportPalette {
    val gold = Color(0xFFD4B16A)
    val body = Color(0xFFEDE9DF)
}

/** Onboarding, one glow per beat. */
object OnboardingPalette {
    val beatOne = Color(0xFF2D7FF9)
    val beatTwo = Color(0xFF6366F1)
    val beatFourAmber = Color(0xFFE8A15C)
    val beatFourBlue = Color(0xFF8BA4FF)
    val beatFourGold = Color(0xFFD4B16A)
}

/** The app mark background, design-v3.md 4.2. */
val MarkBackground = Color(0xFF141A2E)
val MarkForeground = Color(0xFFF3F1EC)

/** The support block, MASTER_BUILD_PROMPT 14.5. The only warm accent in the app. */
val SupportAccent = Color(0xFFB45309)

val LocalClarityColors = staticCompositionLocalOf { ClarityLightColors }
val LocalContemplativeColors = staticCompositionLocalOf { ClarityContemplativeColors }
