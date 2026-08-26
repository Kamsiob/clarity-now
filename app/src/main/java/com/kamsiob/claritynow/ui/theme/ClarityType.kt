package com.kamsiob.claritynow.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.kamsiob.claritynow.R

/**
 * design-v3.md section 5. Two families, both variable, both bundled as files in
 * res/font, both SIL OFL. The OFL text for each ships alongside them.
 *
 * Downloadable Fonts is never used. That API fetches over the network through Play
 * Services and this app declares no INTERNET permission, so it would fail at
 * runtime in a way no screenshot reveals. MASTER_BUILD_PROMPT 3.1.
 *
 * The families below are built in Kotlin rather than as res/font XML families
 * because Newsreader serves two roles through its optical size axis, and a single
 * XML font family resolves by weight and style alone. Two optical sizes at the same
 * weight cannot be expressed there. The font files themselves are still plain
 * resources in res/font, which is what the no-network requirement actually turns on.
 */
private fun newsreader(weight: Int, opticalSize: Float): FontFamily = FontFamily(
    Font(
        resId = R.font.newsreader,
        weight = FontWeight(weight),
        style = FontStyle.Normal,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(weight),
            FontVariation.opticalSizing(opticalSize.sp),
        ),
    ),
    Font(
        resId = R.font.newsreader_italic,
        weight = FontWeight(weight),
        style = FontStyle.Italic,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(weight),
            FontVariation.opticalSizing(opticalSize.sp),
        ),
    ),
)

private fun hanken(weight: Int): FontFamily = FontFamily(
    Font(
        resId = R.font.hanken_grotesk,
        weight = FontWeight(weight),
        style = FontStyle.Normal,
        variationSettings = FontVariation.Settings(FontVariation.weight(weight)),
    ),
)

/**
 * Trimmed line height with both edges proportional, so a serif set at 1.62 does not
 * gain a stray half line above the first baseline of a paragraph.
 */
private val TrimmedLineHeight = LineHeightStyle(
    alignment = LineHeightStyle.Alignment.Center,
    trim = LineHeightStyle.Trim.None,
)

@Immutable
data class ClarityTypography(
    val displayHero: TextStyle,
    val displayTitle: TextStyle,
    val readSerif: TextStyle,
    val closingLine: TextStyle,
    val bodySerif: TextStyle,
    val itemTitle: TextStyle,
    val title: TextStyle,
    val body: TextStyle,
    val bodyStrong: TextStyle,
    val label: TextStyle,
    val sidehead: TextStyle,
    val swipeLabel: TextStyle,
    val caption: TextStyle,
    val timerNumeral: TextStyle,
)

val ClarityTypeScale = ClarityTypography(
    displayHero = TextStyle(
        fontFamily = newsreader(weight = 400, opticalSize = 68f),
        fontWeight = FontWeight(400),
        fontSize = 40.sp,
        lineHeight = 43.2.sp,
        letterSpacing = (-0.012).em,
        lineHeightStyle = TrimmedLineHeight,
    ),
    displayTitle = TextStyle(
        fontFamily = newsreader(weight = 400, opticalSize = 48f),
        fontWeight = FontWeight(400),
        fontSize = 30.sp,
        lineHeight = 36.sp,
        lineHeightStyle = TrimmedLineHeight,
    ),
    readSerif = TextStyle(
        fontFamily = newsreader(weight = 400, opticalSize = 34f),
        fontWeight = FontWeight(400),
        fontSize = 26.sp,
        lineHeight = 35.36.sp,
        lineHeightStyle = TrimmedLineHeight,
    ),
    closingLine = TextStyle(
        fontFamily = newsreader(weight = 400, opticalSize = 34f),
        fontWeight = FontWeight(400),
        fontSize = 24.sp,
        lineHeight = 34.08.sp,
        lineHeightStyle = TrimmedLineHeight,
    ),
    bodySerif = TextStyle(
        fontFamily = newsreader(weight = 400, opticalSize = 17f),
        fontWeight = FontWeight(400),
        fontSize = 17.sp,
        lineHeight = 27.54.sp,
        lineHeightStyle = TrimmedLineHeight,
    ),
    itemTitle = TextStyle(
        fontFamily = hanken(weight = 650),
        fontWeight = FontWeight(650),
        fontSize = 21.sp,
        lineHeight = 26.sp,
        letterSpacing = (-0.022).em,
        lineHeightStyle = TrimmedLineHeight,
    ),
    title = TextStyle(
        fontFamily = hanken(weight = 700),
        fontWeight = FontWeight(700),
        fontSize = 19.sp,
        lineHeight = 24.sp,
        lineHeightStyle = TrimmedLineHeight,
    ),
    body = TextStyle(
        fontFamily = hanken(weight = 400),
        fontWeight = FontWeight(400),
        fontSize = 16.sp,
        lineHeight = 24.sp,
        lineHeightStyle = TrimmedLineHeight,
    ),
    bodyStrong = TextStyle(
        fontFamily = hanken(weight = 600),
        fontWeight = FontWeight(600),
        fontSize = 16.sp,
        lineHeight = 24.sp,
        lineHeightStyle = TrimmedLineHeight,
    ),
    label = TextStyle(
        fontFamily = hanken(weight = 600),
        fontWeight = FontWeight(600),
        fontSize = 13.sp,
        lineHeight = 17.sp,
        lineHeightStyle = TrimmedLineHeight,
    ),
    sidehead = TextStyle(
        fontFamily = hanken(weight = 700),
        fontWeight = FontWeight(700),
        fontSize = 13.sp,
        lineHeight = 17.sp,
        lineHeightStyle = TrimmedLineHeight,
    ),
    swipeLabel = TextStyle(
        fontFamily = hanken(weight = 700),
        fontWeight = FontWeight(700),
        fontSize = 10.5.sp,
        lineHeight = 13.sp,
        lineHeightStyle = TrimmedLineHeight,
    ),
    caption = TextStyle(
        fontFamily = hanken(weight = 400),
        fontWeight = FontWeight(400),
        fontSize = 12.sp,
        lineHeight = 16.sp,
        lineHeightStyle = TrimmedLineHeight,
    ),
    timerNumeral = TextStyle(
        fontFamily = hanken(weight = 250),
        fontWeight = FontWeight(250),
        fontSize = 64.sp,
        lineHeight = 68.sp,
        letterSpacing = (-0.03).em,
        lineHeightStyle = TrimmedLineHeight,
    ),
)

/**
 * Cap height trimming, for a label that sits inside a contained control.
 *
 * A line box reserves room for descenders whether or not the string has any, so a
 * button label centered by layout sits visibly low. Trimming to the cap height and
 * the baseline centers what the eye actually sees. Applied to buttons, chips, the
 * tab bar and swipe labels, and never to running prose, where the reserved space is
 * what keeps successive lines apart.
 */
fun TextStyle.opticallyCentered(): TextStyle = copy(
    lineHeightStyle = LineHeightStyle(
        alignment = LineHeightStyle.Alignment.Center,
        trim = LineHeightStyle.Trim.Both,
    ),
)

/**
 * Hanken Grotesk ships no `tnum` feature, which the build time check in
 * design-v3.md 5.2 anticipated. Its default figures are already uniform in advance
 * width at the default instance, but the font carries HVAR, so advance widths do
 * vary along the weight axis and uniformity at weight 250 is not guaranteed by the
 * file. Every updating numeric display therefore goes through the fixed width
 * numeral treatment rather than relying on a font feature that is not there.
 *
 * Newsreader does carry `tnum` and uses it directly.
 */
const val HANKEN_HAS_TABULAR_FIGURES = false
const val NEWSREADER_TABULAR_FEATURE = "tnum"

val LocalClarityTypography = staticCompositionLocalOf { ClarityTypeScale }
