package com.kamsiob.claritynow.ui.about

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.kamsiob.claritynow.R
import com.kamsiob.claritynow.ui.components.ClarityIcon
import com.kamsiob.claritynow.ui.components.ClarityIcons
import com.kamsiob.claritynow.ui.components.clarityClickable
import com.kamsiob.claritynow.ui.components.clarityFocusRing
import com.kamsiob.claritynow.ui.theme.ClarityHapticEvent
import com.kamsiob.claritynow.ui.theme.ClaritySpacing
import com.kamsiob.claritynow.ui.theme.LocalClarityColors
import com.kamsiob.claritynow.ui.theme.LocalClarityShapes
import com.kamsiob.claritynow.ui.theme.LocalClarityTypography
import com.kamsiob.claritynow.ui.theme.SupportAccent
import com.kamsiob.claritynow.ui.theme.clarityMotion
import com.kamsiob.claritynow.ui.theme.opticallyCentered

/**
 * The support block, MASTER_BUILD_PROMPT 14.5.
 *
 * **The only money related element in the entire app**, and it appears at the bottom
 * of Settings and at the bottom of About and nowhere else. Never a dialog, never an
 * interstitial, never after completing a task. There are exactly two call sites and
 * both are in this phase's own files, which is the whole of how that stays true.
 *
 * A rounded card with a warm parchment gradient and no border. The gradient is this
 * element's one separation device, per design-v3.md 6.1, which is why it takes no
 * shadow: `ClarityCard` would add one in the light world and that would be two.
 *
 * ## The copy rules, which are absolute
 *
 * No coffee or caffeine reference anywhere, in the label or the body. No framing that
 * anchors support to a small amount. No begging, no urgency, no counter, no goal bar,
 * no `if you enjoy`, no exclamation marks. The destination is a constant in
 * `ClarityLinks` rather than a string resource so that the one word this copy may not
 * contain is not reachable from a `stringResource` call.
 *
 * ## Where the warm color goes, and where it does not
 *
 * 14.5 reads "Heading `Support this work` with a small outlined heart icon in
 * `#B45309`", and the color attaches to the icon. **The heading is ordinary ink**, and
 * that is a measurement rather than a preference: `#B45309` on the light parchment is
 * 4.27:1 and on the dark parchment is 3.27:1, and design-v3.md 13 holds text to 4.5:1
 * while 16.7 holds a graphic to 3.0:1. As a heading the accent fails in both worlds; as
 * an 18dp glyph it clears in both. The button keeps the filled `#B45309` that 14.5
 * states outright, where a white label measures 4.98:1 against it.
 *
 * Nothing here takes calm mode's transform. design-v3.md 16.2 leaves `parchment`
 * unchanged by name, and the accent is doing the two jobs 16.2 excludes function colors
 * for: it is a control and the mark on a heading, not atmosphere.
 */
@Composable
internal fun SupportBlock(modifier: Modifier = Modifier) {
    val colors = LocalClarityColors.current
    val type = LocalClarityTypography.current
    val shapes = LocalClarityShapes.current
    val context = LocalContext.current

    val gradient = remember(colors.parchment) {
        Brush.linearGradient(
            listOf(
                colors.parchment,
                SupportAccent.copy(alpha = GRADIENT_WARMTH).compositeOver(colors.parchment),
            ),
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shapes.card)
            .background(gradient)
            .padding(horizontal = 18.dp, vertical = ClaritySpacing.scaled(18.dp)),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ClarityIcon(
                icon = ClarityIcons.support,
                contentDescription = null,
                tint = SupportAccent,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(9.dp))
            Text(
                text = stringResource(R.string.support_heading),
                style = type.bodyStrong,
                color = colors.inkPrimary,
            )
        }
        Spacer(Modifier.height(ClaritySpacing.scaled(10.dp)))
        Text(
            text = stringResource(R.string.support_body),
            style = type.bodySerif,
            color = colors.inkSecondary,
        )
        Spacer(Modifier.height(ClaritySpacing.scaled(16.dp)))
        SupportButton(
            label = stringResource(R.string.support_button),
            onClick = { openExternalLink(context, ClarityLinks.SUPPORT) },
        )
    }
}

/**
 * The one button in the app that is neither of design-v3.md 10.7's five roles.
 *
 * It is not a sixth role and must not become one: the geometry is 10.7's Primary
 * exactly, 50dp tall at a 12dp radius with a `bodyStrong` label optically centered and
 * the same 0.97 press on the standard spring, and the only thing it changes is the
 * fill, to the `#B45309` MASTER_BUILD_PROMPT 14.5 names. It is written out here rather
 * than added to `ClarityButton` so that the warm fill has exactly one call site and
 * cannot be reached from anywhere else in the app.
 */
@Composable
private fun SupportButton(label: String, onClick: () -> Unit) {
    val type = LocalClarityTypography.current
    val shapes = LocalClarityShapes.current
    val motion = clarityMotion()
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = motion.springStandard(),
        label = "supportButtonPress",
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            // A minimum rather than a height, for the reason `ClarityButton` gives: the
            // label is `bodyStrong` with no `maxLines`, so a fixed box cuts the second
            // line off at a large text size and nothing reports it.
            .heightIn(min = ClaritySpacing.scaled(SUPPORT_BUTTON_HEIGHT))
            .clip(shapes.button)
            .background(SupportAccent)
            .clarityFocusRing(interaction, shapes.button)
            .clarityClickable(
                interactionSource = interaction,
                haptic = ClarityHapticEvent.TAP,
                role = Role.Button,
                onClickLabel = label,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = type.bodyStrong.opticallyCentered(),
            color = Color.White,
            modifier = Modifier.padding(horizontal = 20.dp),
        )
    }
}

/**
 * How far the gradient leans off parchment.
 *
 * Small on purpose. design-v3.md 15.1 lists a gradient as a tell three times over and
 * every entry is about a saturated one used for impact; this is a warm shift across a
 * card, which is what 14.5 asks for, and eight percent is the point at which it is felt
 * rather than seen.
 */
private const val GRADIENT_WARMTH = 0.08f

/** design-v3.md 10.7's button height, as a minimum. */
private val SUPPORT_BUTTON_HEIGHT = 50.dp
