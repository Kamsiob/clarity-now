package com.kamsiob.claritynow.ui.components

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
import androidx.compose.ui.unit.dp
import com.kamsiob.claritynow.ui.theme.ClarityElevation
import com.kamsiob.claritynow.ui.theme.ClarityHapticEvent
import com.kamsiob.claritynow.ui.theme.ClaritySpacing
import com.kamsiob.claritynow.ui.theme.LocalClarityColors
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
 * A 61dp pill inset from the edges and bottom, card colored, carrying elevation
 * and no border, because an element with both would be the two separation device
 * violation the design forbids.
 *
 * Inactive destinations are their icon alone. The current one expands into an
 * inner pill carrying the icon and its name. The expansion runs on springStandard, the
 * icon crossfades outlined to filled, and the label arrives behind the width rather
 * than with it, so the pill never looks like it is dragging text along.
 *
 * ## One label of four, and phase 12b is where that stopped being an accident
 *
 * design-v3.md 10.4 was silent on inactive labels through phase 12, so the build took
 * the platform default, which is exactly the move 15.3 names: "adopting a Material 3
 * Expressive default because it is the default". 10.4 now states the rule and carries
 * the reason; the short version is that this bar is the only element in the app with a
 * width it cannot grow out of.
 *
 * Four labels here are not a style choice, they are a measurement. Icons do not scale
 * with the system font scale and labels do, and a 360dp phone leaves this bar 314dp
 * between its insets and its own padding. At 200 percent text one label plus four icons
 * comes to roughly 290dp of that; four labels come to roughly 570dp, which is nearly
 * twice the room there is. The figures are estimated from Hanken Grotesk's advances at
 * 26sp rather than measured on a device, and the conclusion survives being wrong by a
 * third in either direction, which is the only kind of estimate worth writing down.
 * design-v3.md 13 requires 200 percent without clipping, so labels always would either
 * clip a destination name, which is the defect the rule would exist to prevent, or make
 * the one piece of chrome on every screen grow past 90dp and take the content padding of
 * four screens with it.
 *
 * The constraint that makes the unlabeled state legitimate is on section 7 rather than
 * on this file: a destination whose glyph cannot be recognized on its own does not get
 * an unlabeled state, so the glyph is what changes. `arrow_outward` fails that test
 * today and is recorded against issue #23's mapping table rather than fixed here, since
 * a replacement is a new drawable and not a token.
 */
@Composable
fun ClarityTabBar(
    tabs: List<ClarityTab>,
    selectedKey: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalClarityColors.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 17.dp)
            .height(ClaritySpacing.tabBarHeight)
            .clarityShadow(ClarityElevation.tabBar, CircleShape, enabled = !colors.isDark)
            .clip(CircleShape)
            // `raise`, not `card`. design-v3.md 3.1 as amended in phase 3c: the
            // surface ladder is a rank, and chrome sits one step below content so it
            // stops competing with it. This bar and an area card used to be the same
            // value, which is why a screen of cards read as a screen of chrome. The
            // shadow stays: a lightness step stands in for a border, never for a
            // shadow, and 6.1's prohibition is on a hairline and a shadow together.
            .background(colors.raise)
            .padding(horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        tabs.forEach { tab ->
            TabItem(
                tab = tab,
                selected = tab.key == selectedKey,
                onClick = { onSelect(tab.key) },
            )
        }
    }
}

@Composable
private fun TabItem(
    tab: ClarityTab,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val colors = LocalClarityColors.current
    val type = LocalClarityTypography.current
    val motion = clarityMotion()
    val interaction = remember { MutableInteractionSource() }

    val expansion by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = motion.springStandard(),
        label = "tabExpansion",
    )
    // The label trails the width so the pill opens first and the name lands in it.
    val labelAlpha by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = motion.effects(),
        label = "tabLabel",
    )
    // The spring is allowed to overshoot, which is what gives the pill its life, so
    // anything that must stay inside zero to one reads the clamped value and only
    // the scale reads the raw one.
    val reveal = expansion.coerceIn(0f, 1f)

    val tint by animateColorAsState(
        targetValue = if (selected) colors.actionBlue else colors.inkSecondary,
        animationSpec = motion.effects(),
        label = "tabTint",
    )

    Row(
        modifier = Modifier
            // design-v3.md 13's touch minimum, and it was 46dp until phase 12b. The bar
            // is 61dp, so this leaves 6.5dp above and below and costs the pill nothing.
            //
            // A minimum rather than a height since the text size control landed. The
            // label's trimmed box reaches about 31dp at the 200 percent cap, so 48dp is
            // still what this measures at every size the app can reach; writing it as a
            // floor means a later change to 10.4 that made the label taller would open
            // the pill instead of cutting the name in half.
            .heightIn(min = ClaritySpacing.minTouchTarget)
            .clip(CircleShape)
            .background(colors.actionBlue.copy(alpha = 0.10f * reveal))
            .clarityFocusRing(interaction, CircleShape)
            .clarityClickable(
                interactionSource = interaction,
                haptic = ClarityHapticEvent.TAP,
                role = Role.Tab,
                onClickLabel = tab.label,
                onClick = onClick,
            )
            // **Which tab you are on was carried by color alone**: a 10 percent
            // `actionBlue` pill, a filled icon variant and an `actionBlue` label, all
            // three visual. design-v3 13 says color is never the only signal, and this is
            // the app's primary navigation. TalkBack now reads "Momentum, selected, tab".
            .semantics { this.selected = selected }
            .padding(horizontal = (14 + 4 * reveal).dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Box(contentAlignment = Alignment.Center) {
            ClarityIcon(
                icon = tab.icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(24.dp).alpha(1f - reveal),
            )
            ClarityIcon(
                icon = tab.iconFilled,
                contentDescription = null,
                tint = tint,
                modifier = Modifier
                    .size(24.dp)
                    .alpha(reveal)
                    // A touch of lift as it becomes the current place.
                    .scale(0.94f + 0.06f * expansion),
            )
        }

        Text(
            text = tab.label,
            style = type.label.opticallyCentered(),
            color = tint,
            maxLines = 1,
            overflow = TextOverflow.Clip,
            softWrap = false,
            modifier = Modifier
                .alpha(labelAlpha.coerceIn(0f, 1f))
                // Measured at full width, then revealed by the expansion, so the
                // text never reflows while the pill is opening.
                .layout { measurable, constraints ->
                    val placeable = measurable.measure(constraints.copy(minWidth = 0))
                    val leading = (7.dp.toPx() * reveal).toInt()
                    val width = ((placeable.width + leading) * reveal).toInt().coerceAtLeast(0)
                    layout(width, placeable.height) { placeable.place(leading, 0) }
                },
        )
    }
}

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
