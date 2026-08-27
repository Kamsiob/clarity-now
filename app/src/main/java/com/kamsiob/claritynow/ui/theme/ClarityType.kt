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
 * because both of them drive a variable axis that an XML family cannot express. An
 * XML font family resolves by weight and style alone, so Newsreader's two optical
 * sizes at the same weight have nowhere to live, and neither does a named weight
 * instance cut from a single variable file. The font files themselves are still
 * plain resources in res/font, which is what the no-network requirement actually
 * turns on.
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

/**
 * Every weight the design system asks Hanken Grotesk for, in one family.
 *
 * 250 is the timer numeral, 400 body and caption, 500 the idle card title, 600
 * bodyStrong and label, 650 itemTitle, 700 title, sidehead and swipeLabel and the
 * selected mood name. A request for a weight that is not on this list resolves to
 * the nearest one that is, which is the ordinary contract and is what makes a
 * missing entry a slightly wrong rendering rather than a silent no-op.
 */
private val HANKEN_WEIGHTS = listOf(250, 400, 500, 600, 650, 700)

/**
 * Hanken Grotesk, one variable file, exposed as a family carrying a named instance
 * at every weight above.
 *
 * **This replaces a per-weight family builder, and the reason is a defect it hid
 * for two phases.** A `Font` created with `FontVariation.weight` in its
 * `variationSettings` has that axis baked into the typeface at load. When the
 * family holds exactly one such face, the font matcher returns it for every
 * request, so `TextStyle.copy(fontWeight = ...)` resolves to the same pinned
 * instance and the requested weight is discarded without an error. Two call sites
 * in `ui/areas/AreaCard.kt` and one in `ui/areas/ColorPicker.kt` asked for a weight
 * that design-v3.md names and rendered a different one, and nothing failed.
 *
 * Registering each weight as its own face restores the contract: the matcher now
 * has something to choose between, so a weight change on a `TextStyle` copy
 * actually applies. The cost is five extra typeface instances of one file, which
 * Compose caches per resource and variation setting.
 */
private val HankenGrotesk = FontFamily(
    HANKEN_WEIGHTS.map { weight ->
        Font(
            resId = R.font.hanken_grotesk,
            weight = FontWeight(weight),
            style = FontStyle.Normal,
            variationSettings = FontVariation.Settings(FontVariation.weight(weight)),
        )
    },
)

/**
 * Trimmed line height with both edges proportional, so a serif set at 1.62 does not
 * gain a stray half line above the first baseline of a paragraph.
 */
private val TrimmedLineHeight = LineHeightStyle(
    alignment = LineHeightStyle.Alignment.Center,
    trim = LineHeightStyle.Trim.None,
)

/** The fourteen named roles of design-v3.md 5.3, resolved once and read through a local. */
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

/**
 * The scale itself, and the sans tracking ramp that runs through it, design-v3.md 5.3.
 *
 * Hanken Grotesk carries one set of sidebearings, drawn to fit at reading sizes.
 * Set large they leave proportionally too much room, because the space between
 * letters does not need to grow at the same rate the letters do; set small they
 * leave too little, for the same reason in reverse. The correction is the oldest
 * one in typesetting: open the small sizes, close the large ones. Through phase 3b
 * only two of the nine sans roles carried a value, so the seven that make up almost
 * every string on both built screens ran at whatever the file happened to ship.
 *
 * The ramp, in size order, and why each value is what it is.
 *
 * - `swipeLabel` 10.5sp, **+0.032em**, the most open value in the scale. It is the
 *   smallest type in the app, it is set at 700, and it is read once, quickly, off a
 *   row that is moving under the thumb. Legibility outranks evenness of color here
 *   and nowhere else.
 * - `caption` 12sp, **+0.022em**. Timestamps, helper text and the first step line,
 *   all of which are read at a glance next to something larger. This is the size at
 *   which the default fit first becomes visibly tight.
 * - `label` 13sp, **+0.016em**. Area labels, tab labels and chips. Short strings in
 *   a contained shape, where a little air is what separates a label from a caption
 *   that happens to be bold.
 * - `sidehead` 13sp, **+0.024em**, deliberately off the ramp, above `label` at the
 *   same size. A section label has to read as a marker rather than as a sentence,
 *   and the conventional way to buy that is capitals, which 5.3 and 15.1 both
 *   forbid. Tracking delivers the same signal at a fraction of the volume and
 *   leaves the sentence case intact. Recorded under section 15 as an open choice.
 * - `body` 15sp, **+0.004em**. Running interface prose and list rows. Almost the
 *   pivot: the face is close to right here, and the small opening is for the fact
 *   that a list row is scanned rather than read.
 * - `bodyStrong` 17sp, **-0.006em**. Semibold at a heading size, where the thicker
 *   stems already close the counters, so the space between letters can come in with
 *   them.
 * - `title` 19sp, **-0.014em**. Sheet titles at 700. The first size where an untouched
 *   fit reads as loose rather than merely comfortable.
 * - `itemTitle` 21sp, **-0.022em**, unchanged. Already specified in 5.3.
 * - `timerNumeral` 64sp, **-0.030em**, unchanged. Already specified in 5.3.
 *
 * The serif roles are not on this ramp and do not need one. Newsreader ships an
 * optical size axis and each serif role sets it, which is the same correction made
 * inside the outlines instead of between them. `displayHero` carries an additional
 * -0.012em because 5.3 asks for it at 40sp.
 *
 * **Not a design tell.** 15.1's typographic entries are named families, all caps
 * section labels and serif italic accents. Optical tracking is none of the three,
 * and the `sidehead` value above exists precisely to avoid the second.
 */
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
        fontFamily = HankenGrotesk,
        fontWeight = FontWeight(650),
        fontSize = 21.sp,
        lineHeight = 26.sp,
        letterSpacing = (-0.022).em,
        lineHeightStyle = TrimmedLineHeight,
    ),
    title = TextStyle(
        fontFamily = HankenGrotesk,
        fontWeight = FontWeight(700),
        fontSize = 19.sp,
        lineHeight = 24.sp,
        letterSpacing = (-0.014).em,
        lineHeightStyle = TrimmedLineHeight,
    ),
    // 15sp, down from 16. This role and bodyStrong were both 16sp until phase 3c,
    // which made a size hierarchy impossible inside a run of text: design-v3.md 11
    // gives a Trail day header to bodyStrong and the rows under it to body, so the
    // header could differ only by weight. body drops to 15 and bodyStrong rises
    // to 17, which puts most of the step below rather than above, because
    // bodyStrong is also the button label in 10.7 and the undo action in 10.14 and
    // one step is as far as either can move without starting to shout.
    // 16sp is also Material's bodyLarge default, so 15 is the step off it that
    // section 15's open choice rule asks for, and 10.11 already sets a settings
    // row title at 15sp, which makes 15 an established row size in this document.
    body = TextStyle(
        fontFamily = HankenGrotesk,
        fontWeight = FontWeight(400),
        fontSize = 15.sp,
        lineHeight = 22.5.sp,
        letterSpacing = 0.004.em,
        lineHeightStyle = TrimmedLineHeight,
    ),
    // 17sp, up from 16, and a tighter line than body because this role now heads a
    // section. 1.35 puts it between body at 1.5 and title at 1.26.
    bodyStrong = TextStyle(
        fontFamily = HankenGrotesk,
        fontWeight = FontWeight(600),
        fontSize = 17.sp,
        lineHeight = 23.sp,
        letterSpacing = (-0.006).em,
        lineHeightStyle = TrimmedLineHeight,
    ),
    label = TextStyle(
        fontFamily = HankenGrotesk,
        fontWeight = FontWeight(600),
        fontSize = 13.sp,
        lineHeight = 17.sp,
        letterSpacing = 0.016.em,
        lineHeightStyle = TrimmedLineHeight,
    ),
    sidehead = TextStyle(
        fontFamily = HankenGrotesk,
        fontWeight = FontWeight(700),
        fontSize = 13.sp,
        lineHeight = 17.sp,
        letterSpacing = 0.024.em,
        lineHeightStyle = TrimmedLineHeight,
    ),
    swipeLabel = TextStyle(
        fontFamily = HankenGrotesk,
        fontWeight = FontWeight(700),
        fontSize = 10.5.sp,
        lineHeight = 13.sp,
        letterSpacing = 0.032.em,
        lineHeightStyle = TrimmedLineHeight,
    ),
    caption = TextStyle(
        fontFamily = HankenGrotesk,
        fontWeight = FontWeight(400),
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.022.em,
        lineHeightStyle = TrimmedLineHeight,
    ),
    timerNumeral = TextStyle(
        fontFamily = HankenGrotesk,
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
