package com.kamsiob.claritynow.ui.components

import com.kamsiob.claritynow.ui.theme.LocalClarityShapes
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Column
import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.unit.dp
import com.kamsiob.claritynow.ui.theme.ClarityElevation
import com.kamsiob.claritynow.ui.theme.ClarityHapticEvent
import com.kamsiob.claritynow.ui.theme.ClaritySpacing
import com.kamsiob.claritynow.ui.theme.LocalClarityColors
import com.kamsiob.claritynow.ui.theme.LocalContemplativeColors
import com.kamsiob.claritynow.ui.theme.LocalClarityTypography
import com.kamsiob.claritynow.ui.theme.clarityMotion
import com.kamsiob.claritynow.ui.theme.opticallyCentered

/** One root destination. design-v3.md 10.15: four tabs, no drawer, nothing hidden. */
@Immutable
data class ClarityTab(
    val key: String,
    val label: String,
    @DrawableRes val icon: Int,
    @DrawableRes val iconFilled: Int,
)

/**
 * design-v3.md 10.4, the floating tab bar.
 *
 * A 68dp pill inset from the edges and bottom, card colored, carrying elevation and no
 * border, because an element with both would be the two separation device violation the
 * design forbids.
 *
 * **Four equal slots, each a glyph over its own name, and only the indicator moves.**
 *
 * ## What this replaced, and the three things that were wrong with it
 *
 * Until the appeal pass this bar was 61dp of glyphs in a `SpaceEvenly` row, where the
 * selected destination expanded sideways into a pill carrying its label and the other
 * three were their icon alone. The owner's word for the result was that it looked like a
 * second thought. Three separate causes, and all three are structural rather than a
 * matter of taste:
 *
 * 1. **Three of the four destinations were unlabeled.** COGA o4p06, `Use Clear Visible
 *    Labels`, names ADHD among the conditions it is written for and asks that a label be
 *    visible and next to its control; o1p07 frames an icon as sitting beside content
 *    rather than replacing it. NN/g measured the general case: navigation whose
 *    destinations are not named made people 15 percent slower, cut content discovery by
 *    more than 20 percent and raised perceived difficulty by 21 percent. This app's
 *    audience is the one that pays that most.
 * 2. **Every tap moved the other three.** A pill that grows sideways to fit a word pushes
 *    its neighbors along, so the whole bar reflowed on every navigation. COGA o4p01 is
 *    `Ensure Controls and Content Do Not Move Unexpectedly`, and it is written for people
 *    with, in its words, an impaired ability to screen out movement. Now the four slots
 *    are fixed and **the indicator translates**; nothing else moves, at any text size.
 * 3. **It was sized for one row.** 61dp for a 24dp glyph is a strip. 68dp for a 32dp
 *    indicator, a 26dp glyph and a 13sp name is a control.
 *
 * ## The label rule, and it is a measurement rather than a preference
 *
 * Four labels fit. The bar spans 314dp between its 17dp insets and its own padding on a
 * 360dp phone, which is 78dp a slot, and `Momentum` is the longest of the four at about
 * 68dp of that at 13sp. **They fit at the size most people are actually at and they do
 * not fit far above it**, because a single word cannot wrap and Android's font scale is
 * seven discrete stops rather than a slider: 0.85, 1.0, 1.15, 1.30, 1.50, 1.80, 2.0. The
 * labels are drawn while the combined scale is at or under [LABEL_MAX_FONT_SCALE] and are
 * dropped above it, which is one stop of headroom over the default.
 *
 * **The combined scale is the one that matters, and it is why this is not simply the OS
 * setting.** `ClarityTextSize` multiplies it, so a person on a stock phone who moves only
 * this app's own text control to its top setting is at 1.5 and past the threshold. That
 * is one tap inside Settings rather than an edge case, which is exactly why the rule is
 * expressed against `LocalDensity.current.fontScale` under the theme, the combined
 * figure, rather than against the platform's.
 *
 * **Nothing is lost when the labels go.** The glyph, the indicator and the semantics are
 * unchanged, and TalkBack reads the destination's name either way, because `Role.Tab`
 * plus `selected` plus the click label do not depend on a `Text` being drawn.
 *
 * ## A note on the default this used to blame itself for
 *
 * The previous version of this comment said the unlabeled state came from "adopting a
 * Material 3 Expressive default because it is the default". It did not: Compose's
 * `NavigationBarItem` defaults `alwaysShowLabel` to **true**. Selected-only labels are the
 * Views library's `LABEL_VISIBILITY_AUTO` behavior at four or more items, which this app
 * does not use. The self criticism was aimed at a default that was not there.
 */
@Composable
fun ClarityTabBar(
    tabs: List<ClarityTab>,
    selectedKey: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    /**
     * **True while a Contemplative surface is showing, and the bar changes world with it.**
     *
     * The Report is the app's most art directed screen: a near black ground, gold rules
     * and a serif hero. A `#EBEAE6` pill with a saturated `#004BAE` chip on it was
     * measured as the brightest object on that page, so the one piece of Daylight chrome
     * standing on it took the eye first and undid the room. The same is true of the Pulse.
     *
     * The bar does not disappear, because navigation that vanishes is worse, and it does
     * not become transparent, because a floating pill needs a ground to float on. It
     * changes register: `surfaceRaised` from 3.3 instead of `raise`, the Contemplative
     * text tokens for its labels, and no shadow, because the Contemplative world has none.
     */
    contemplative: Boolean = false,
) {
    val colors = LocalClarityColors.current
    val night = LocalContemplativeColors.current
    // **`surfaceRaised` is 1.05:1 against the Report's own page, which is no boundary at
    // all**, and 6.1 gives the Contemplative world no shadows, so disabling the shadow
    // left the bar with zero separation devices. Overshooting from loudest object to no
    // object is not a fix.
    //
    // The bar lifts to a value ABOVE `surfaceRaised` instead: the world's own bright text
    // at 9 percent over its raised surface, which is one tone step and the same device the
    // Daylight ladder uses. Measured 1.34:1 against the page, which is a boundary a person
    // sees without it becoming the brightest thing on a page it does not own.
    val ground = if (contemplative) {
        night.textBright.copy(alpha = 0.09f).compositeOver(night.surfaceRaised)
    } else {
        colors.raise
    }
    val labeled = LocalDensity.current.fontScale <= LABEL_MAX_FONT_SCALE

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 17.dp)
            .height(ClaritySpacing.tabBarHeight)
            .clarityShadow(
                ClarityElevation.tabBar,
                CircleShape,
                enabled = !colors.isDark && !contemplative,
            )
            .clip(CircleShape)
            // `raise`, not `card`. design-v3.md 3.1 as amended in phase 3c: the
            // surface ladder is a rank, and chrome sits one step below content so it
            // stops competing with it. This bar and an area card used to be the same
            // value, which is why a screen of cards read as a screen of chrome. The
            // shadow stays: a lightness step stands in for a border, never for a
            // shadow, and 6.1's prohibition is on a hairline and a shadow together.
            .background(ground)
            .padding(horizontal = 6.dp)
            // One announcement of "2 of 4" rather than four unrelated tabs. The children
            // already carry `selected`, which is the other half the platform needs.
            .selectableGroup(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        tabs.forEach { tab ->
            TabItem(
                tab = tab,
                selected = tab.key == selectedKey,
                contemplative = contemplative,
                labeled = labeled,
                onClick = { onSelect(tab.key) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/**
 * One destination: an indicator, a glyph inside it, and the name under it.
 *
 * **The slot never changes width and the label never reflows.** Everything that moves on
 * selection moves inside the fixed slot: the indicator fades and lifts, the glyph
 * crossfades outlined to filled, and the ink changes. That is what makes the bar hold
 * still under a thumb, which is o4p01's requirement and the largest single difference
 * between this and what it replaced.
 */
@Composable
private fun TabItem(
    tab: ClarityTab,
    selected: Boolean,
    contemplative: Boolean,
    labeled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalClarityColors.current
    val type = LocalClarityTypography.current
    val motion = clarityMotion()
    val interaction = remember { MutableInteractionSource() }

    // One value drives the indicator, the two glyph alphas and the lift, so they can
    // never disagree about which destination is current.
    val reveal by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = motion.springStandard(),
        label = "tabReveal",
    )
    val shown = reveal.coerceIn(0f, 1f)

    val night = LocalContemplativeColors.current
    // The Contemplative worlds have no action color of their own, so the selected tab
    // takes the world's bright text and the rest take its dim text. That keeps the one
    // signal a tab bar has to carry, without importing a Daylight accent into a room 3.3
    // deliberately built without one.
    val accent = if (contemplative) night.textBright else colors.actionBlue
    val quiet = if (contemplative) night.textDim else colors.inkSecondary
    val tint by animateColorAsState(
        targetValue = if (selected) accent else quiet,
        animationSpec = motion.effects(),
        label = "tabTint",
    )

    Column(
        modifier = modifier
            // design-v3.md 13's touch minimum. A floating pill forfeits the platform's own
            // relaxation to 32dp, which `TouchTargetSizeCheck` grants only to a bar flush
            // with an edge, so the full 48 applies in both directions and this is a floor
            // with no headroom to give away.
            .heightIn(min = ClaritySpacing.minTouchTarget)
            .clip(LocalClarityShapes.current.row)
            .clarityFocusRing(interaction, LocalClarityShapes.current.row)
            .clarityClickable(
                interactionSource = interaction,
                haptic = ClarityHapticEvent.TAP,
                role = Role.Tab,
                onClickLabel = tab.label,
                showPress = false,
                onClick = onClick,
            )
            // **Which tab you are on was carried by color alone**: a 10 percent
            // `actionBlue` pill, a filled icon variant and an `actionBlue` label, all
            // three visual. design-v3 13 says color is never the only signal, and this is
            // the app's primary navigation. TalkBack now reads "Momentum, selected, tab".
            .semantics { this.selected = selected },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(width = INDICATOR_WIDTH, height = INDICATOR_HEIGHT)
                .clip(CircleShape)
                .background(accent.copy(alpha = (if (contemplative) 0.16f else 0.11f) * shown)),
            contentAlignment = Alignment.Center,
        ) {
            ClarityIcon(
                icon = tab.icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(GLYPH).alpha(1f - shown),
            )
            ClarityIcon(
                icon = tab.iconFilled,
                contentDescription = null,
                tint = tint,
                modifier = Modifier
                    .size(GLYPH)
                    .alpha(shown)
                    // A touch of lift as it becomes the current place. The spring is
                    // allowed to overshoot, which is what gives it life, so the scale
                    // reads the raw value and everything clamped reads `shown`.
                    .scale(0.94f + 0.06f * reveal),
            )
        }
        if (labeled) {
            Spacer(Modifier.height(3.dp))
            Text(
                text = tab.label,
                style = type.label.opticallyCentered(),
                color = tint,
                maxLines = 1,
                overflow = TextOverflow.Clip,
                softWrap = false,
            )
        }
    }
}

/**
 * The combined font scale above which the four names are dropped.
 *
 * 1.06 rather than 1.0, so the smallest stop above the default, 1.15, is the first one
 * that loses them and the default itself has a margin. Android's scale is seven discrete
 * stops, so a threshold between two of them is a decision about which stop breaks rather
 * than a fraction anybody can land on.
 */
private const val LABEL_MAX_FONT_SCALE = 1.06f

/** The indicator behind the glyph. Wider than it is tall, so it reads as a place. */
private val INDICATOR_WIDTH = 56.dp
private val INDICATOR_HEIGHT = 32.dp

/** 26dp, up from 24. A glyph in a 32dp indicator, not a glyph on its own. */
private val GLYPH = 26.dp

/** The four root destinations, in the order the tab bar shows them. */
@Composable
fun rememberClarityTabs(
    areasLabel: String,
    momentumLabel: String,
    reportLabel: String,
    trailLabel: String,
): List<ClarityTab> = remember(areasLabel, momentumLabel, reportLabel, trailLabel) {
    listOf(
        ClarityTab(TAB_AREAS, areasLabel, ClarityIcons.areas, ClarityIcons.areasFilled),
        ClarityTab(TAB_MOMENTUM, momentumLabel, ClarityIcons.momentum, ClarityIcons.momentumFilled),
        ClarityTab(TAB_REPORT, reportLabel, ClarityIcons.report, ClarityIcons.reportFilled),
        ClarityTab(TAB_TRAIL, trailLabel, ClarityIcons.trail, ClarityIcons.trailFilled),
    )
}

const val TAB_AREAS = "areas"
const val TAB_MOMENTUM = "momentum"
const val TAB_REPORT = "report"
const val TAB_TRAIL = "trail"

/**
 * Kept so the bar can reserve its own height in a Scaffold without magic numbers, and
 * now reading the one place that number lives rather than restating it.
 *
 * They were two more copies of `ClaritySpacing.tabBarHeight` and `tabBarInset`, which
 * was survivable while all three were literals and would not have survived one of them
 * changing. `ClaritySpacing` says why the bar does not grow with the text.
 */
val TabBarHeight: Dp get() = ClaritySpacing.tabBarHeight
val TabBarInset: Dp get() = ClaritySpacing.tabBarInset
