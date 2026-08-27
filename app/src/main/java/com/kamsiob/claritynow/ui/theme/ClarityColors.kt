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
 *
 * ## The surface ladder, and what phase 3c changed
 *
 * `canvas`, `raise` and `card` are a **rank ladder**, not three interchangeable
 * greys. Read from the page upward in both worlds:
 *
 * | rank | token | light | dark | what sits here |
 * |---|---|---|---|---|
 * | ground | `canvas` | L* 91.4 | L* 4.1 | the page |
 * | chrome | `raise` | L* 95.8 | L* 8.5 | the floating tab bar, unselected chips |
 * | content | `card` | L* 98.6 | L* 11.1 | area cards, sheets, the undo snackbar |
 *
 * **Content is the top plane and chrome recedes from it.** That is the whole rule,
 * and it is the same statement in both worlds, which is why the gaps are matched
 * across them rather than each world being tuned on its own.
 *
 * Before phase 3c the light world spanned 4.73 L* from `canvas` to `card` and
 * `raise` had no call site at all, so the card, the tab bar, the sheets, the chips
 * and the snackbar were one value doing four different jobs and the whole app was
 * two colors: a device capture of the Areas screen measured 75.7 percent `canvas`
 * and 15.9 percent `card`. Dark measured the same way, 86.6 percent `canvas` and
 * 5.1 percent `card`.
 *
 * `raise + a shadow` is one separation device, not two. design-v3.md 3.1 defines
 * `raise` as "the 3 percent lightness step used *instead of* a border", and 6.1's
 * prohibition is specifically "never a hairline and a shadow on the same element".
 * A card has always been lighter than the canvas *and* carried a shadow; a value is
 * what a surface is, and only a deliberate step standing in for a hairline is the
 * device. Putting a hairline on anything in this table is still forbidden.
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

/**
 * design-v3.md 3.1. Four values moved in phase 3c and each one is explained where it
 * is written, because the next session's instinct will be to put them back.
 *
 * - **`canvas` `#F1F1F6` to `#E6E6EC`, L* 95.3 to 91.4.** Depth had to be bought
 *   downward. `card` was pure `#FFFFFF`, so there was no headroom above it and no
 *   amount of lifting the card could produce a step. Moving the ground instead takes
 *   card against canvas from 1.126:1 to 1.202:1 and costs nothing, because the canvas
 *   carries no text of its own that was not already measured. It keeps the cool lean
 *   design-v3.md 1 asks for, at 6 points of blue over red.
 * - **`card` `#FFFFFF` to `#FCFBF9`, L* 100 to 98.6.** design-v3.md 1 says
 *   "backgrounds are never pure white or pure black" and 14 says "no pure white or
 *   pure black backgrounds", while 3.1 said `#FFFFFF`. Two statements against one, and
 *   the build had followed the one. **The contradiction is resolved in favor of the
 *   two**, and it is recorded here rather than quietly fixed. The new value is warm,
 *   3 points of red over blue, which is the "warmth in the cards" section 1 promises
 *   and which nothing in the app delivered before phase 5's parchment arrives:
 *   `cardWash` is the user's own area color and four of the eight moods are cool, so
 *   the default Ocean area put a cool wash on a pure white card.
 * - **`raise` `#FAFAFC` to `#F4F3F0`, L* 98.3 to 95.8.** It was 1.7 L* under the card
 *   and would have been invisible even if anything had drawn it. It now sits between
 *   the ground and the content, which is what a rank in a ladder is for, and it is
 *   warm for the same reason the card is.
 * - **`inkSecondary` 0.60 to 0.64.** Required, not chosen. At 0.60 on the new canvas
 *   it measures 4.33:1, under design-v3.md 13's floor of 4.5. At 0.64 the worst
 *   ground in the app is the canvas at 4.88:1. The raise also retires a defect phase
 *   3b had to pin as unfixable: at 0.60 this token measured 4.27:1 on an in-session
 *   area card, and at 0.64 it measures 4.75:1, so every ground now clears.
 *
 * Card to canvas: 1.126:1 to 1.202:1. Span: 4.73 L* to 7.19 L*.
 */
val ClarityLightColors = ClarityColors(
    isDark = false,
    canvas = Color(0xFFE6E6EC),
    card = Color(0xFFFCFBF9),
    raise = Color(0xFFF4F3F0),
    inkPrimary = InkLight,
    inkSecondary = InkLight.copy(alpha = 0.64f),
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

/**
 * design-v3.md 3.2. Two values moved in phase 3c, on the measurement rather than by
 * analogy with the light world.
 *
 * The light world was audited first and dark was measured afterward with the same
 * lens. It has the same shape of problem and one aggravating factor: **design-v3.md
 * 6.1 gives the dark world no shadows at all**, so the lightness ladder is not one of
 * dark's separation devices, it is the only one. Measured on the shipped tokens, card
 * against canvas was 1.102:1 over 4.98 L*, weaker than the light world that also had a
 * paired shadow to help it, and a device capture was 86.6 percent canvas.
 *
 * - **`card` `#191921` to `#1D1D25`, L* 9.1 to 11.1.** Card against canvas 1.102:1 to
 *   1.150:1, and the step 4.98 L* to 6.98, against the new light world's 7.19.
 * - **`raise` `#15151C` to `#18181F`, L* 7.0 to 8.5.** The gap dark chrome lives on is
 *   canvas to raise, because chrome here has no shadow behind it. It was 2.94 L*
 *   against the light world's 4.39; it is now 4.43, which is the closest match of any
 *   pair in the ladder and deliberately so.
 *
 * **`canvas` is deliberately held at `#0E0E13`.** Dark's depth had to be bought
 * upward, the exact mirror of the light world's problem and the reverse of the
 * statistically common 2026 answer, which is to take a dark theme toward pure black
 * for OLED. design-v3.md 15's open-choice rule applies and this is the recorded
 * reason: design-v3.md 14 bans pure black outright, and the Contemplative world's
 * `deepBlack` is only 0.97 L* below this canvas, so a darker Daylight ground would
 * collapse the two worlds' floors and take the "room dimming" out of entering Focus.
 *
 * Temperature is not touched. Every dark surface here still leans cool by 5 to 8
 * points of blue over red, and design-v3.md 1's "dark surfaces are warm blacks" is the
 * unresolved half of the same contradiction the light `card` resolves. It is a
 * separate decision and is left to be taken as one.
 */
val ClarityDarkColors = ClarityColors(
    isDark = true,
    canvas = Color(0xFF0E0E13),
    card = Color(0xFF1D1D25),
    raise = Color(0xFF18181F),
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
