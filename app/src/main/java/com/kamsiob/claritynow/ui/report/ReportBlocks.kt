package com.kamsiob.claritynow.ui.report

import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kamsiob.claritynow.R
import com.kamsiob.claritynow.ui.components.clarityClickable
import com.kamsiob.claritynow.ui.components.clarityFocusRing
import com.kamsiob.claritynow.ui.components.clarityPressScale
import com.kamsiob.claritynow.ui.theme.ClarityHapticEvent
import com.kamsiob.claritynow.ui.theme.ClaritySpacing
import com.kamsiob.claritynow.ui.theme.LocalCalmMode
import com.kamsiob.claritynow.ui.theme.LocalClarityTypography
import com.kamsiob.claritynow.ui.theme.LocalContemplativeColors
import com.kamsiob.claritynow.ui.theme.ReportPalette
import com.kamsiob.claritynow.ui.theme.calmed
import com.kamsiob.claritynow.ui.theme.clarityMotion

/**
 * The pattern break. `design-v3.md` 11.1 item 7, **the one deliberate grid break**.
 *
 * > Bleeds to full screen width, sits on gold at 4.5 percent, inset 30dp horizontally so
 * > its measure is visibly narrower, set in Newsreader at opsz 28 rather than 17, bounded
 * > top and bottom by full-bleed gold rules.
 *
 * Every clause of that is here and there is nothing else in it. It is the only element on
 * the page that escapes the 20dp measure, and it is allowed to because a page of prose read
 * 52 times a year needs exactly one place where the eye is told something different is
 * happening. Two would be a layout.
 *
 * ## The measure really is narrower, and by how much
 *
 * The body sits on `ClaritySpacing.screenPadding`, which is 20dp, and this sits 30dp in
 * from a full bleed. So the pattern's line is 20dp shorter than the prose above it while
 * its ground is 40dp wider, which is the shape 11.1 describes: a band that reaches further
 * out than anything else on the page and holds less.
 *
 * ## The optical size, which is the one number here that reads two ways
 *
 * 11.1 says "set in Newsreader at opsz 28 rather than 17", and `bodySerif` in 5.3 is
 * "Newsreader 17, opsz 17", where the two 17s are the point size and the optical size and
 * happen to be equal. So the instruction is either "change the optical size" or "change
 * both", and it names only the axis.
 *
 * **This changes the axis and leaves the size alone**, which is the reading that invents no
 * number. `CLAUDE.md` is explicit that every dimension is stated in dp in `design-v3.md`
 * and that nothing may take a number from anywhere else, so a size this document does not
 * give is a size this file must not choose. Newsreader at opsz 28 is a display cut: higher
 * stroke contrast, tighter fitting, a smaller x-height for its size. Set at 17sp on a
 * narrower measure over a gold ground between two full bleed rules, the difference is
 * legible as a difference without the paragraph shouting. Recorded under `design-v3.md` 15
 * because the document left it open, and it is worth an owner's glance on the device.
 *
 * ## The family is built here, and it should not stay here
 *
 * `ClarityType.kt` builds every Newsreader instance through a private helper and 5.3 has no
 * role at opsz 28, so there is nothing to reach for. The right fix is one more entry in
 * `ClarityTypography`, in a file this slice does not own. What is duplicated is four lines
 * of font construction, not a type decision: the weight, the size and the line height are
 * `bodySerif`'s own, copied off the theme at the call site rather than restated.
 */
@Composable
internal fun PatternBreak(sidehead: String, line: String, modifier: Modifier = Modifier) {
    val type = LocalClarityTypography.current
    val gold = ReportPalette.gold.calmed(LocalCalmMode.current)

    Column(modifier = modifier.fillMaxWidth()) {
        GoldRule()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(gold.copy(alpha = GROUND_ALPHA))
                .padding(
                    horizontal = PATTERN_INSET,
                    vertical = ClaritySpacing.scaled(PATTERN_VERTICAL,
                )),
        ) {
            Text(text = sidehead, style = type.sidehead, color = gold)
            Spacer(Modifier.height(ClaritySpacing.scaled(SIDEHEAD_GAP)))
            Text(
                text = line,
                style = type.bodySerif.copy(fontFamily = PatternSerif),
                color = ReportPalette.body,
            )
        }
        GoldRule()
    }
}

/**
 * A gold rule. `design-v3.md` 11.1 item 5 and 3.3.
 *
 * **A horizontal gradient fading to transparent at both ends, never a solid line.** 3.3
 * states that for this surface without qualification, so there is one of these and every
 * rule on the page is it: the one under the ribbon at the body measure, and the two that
 * bound the pattern at full bleed. The only difference between those is the padding the
 * caller gives it.
 */
@Composable
internal fun GoldRule(modifier: Modifier = Modifier) {
    val gold = ReportPalette.gold.calmed(LocalCalmMode.current)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(RULE_HEIGHT)
            .background(
                Brush.horizontalGradient(
                    listOf(Color.Transparent, gold.copy(alpha = RULE_ALPHA), Color.Transparent),
                ),
            ),
    )
}

/**
 * A sidehead in the Report's gold. `design-v3.md` 10.12 and 3.3.
 *
 * The geometry is 10.12's: a sentence case label at sidehead spec, then a hairline running
 * to the trailing edge, vertically centered on the label. Sentence case, never all caps,
 * which 15.1 lists as a tell and section 14 repeats.
 *
 * **It does not reuse the shared `Sidehead` component, and the reason is one sentence in
 * 3.3.** On this surface rules are gradients fading at both ends and never solid lines, and
 * the shared component takes a solid color. Everything else about the two is identical, so
 * what is duplicated is a row and a hairline and what is not duplicated is the one thing
 * this surface says differently. The alternative was a brush parameter on the shared
 * component, in a file this slice does not own.
 */
@Composable
internal fun ReportSidehead(text: String, modifier: Modifier = Modifier) {
    val type = LocalClarityTypography.current
    val gold = ReportPalette.gold.calmed(LocalCalmMode.current)
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = text, style = type.sidehead, color = gold)
        GoldRule(modifier = Modifier.padding(start = SIDEHEAD_RULE_GAP).weight(1f))
    }
}

/**
 * The closing line, and the two answers to it. `design-v3.md` 11.1 item 8.
 *
 * A caption eyebrow reading `One thing`, then the line in `closingLine` at `textBright`,
 * **roman, never italic**, then an accept pill in gold at 14 percent reading `I'll do that`
 * and a decline in text only at `textFaint` reading `Not this week`.
 *
 * ## Roman, never italic
 *
 * 11.1 says it in bold and section 14 and 15.1 say why: serif italic used as an accent is
 * on the tell list, and this is the one line on the page most likely to attract one. The
 * style is taken from the theme and never copied with a `fontStyle`, so there is no italic
 * anywhere in this file to remove later.
 *
 * ## Both optional, both costless, neither ever mentioned again
 *
 * That is a rule about the whole app rather than a note about this screen.
 * `MASTER_BUILD_PROMPT.md` 11.4 requires an explicit decline, because one button is not a
 * choice, and there is no `PLAN_DECLINED` event to write: declining removes the block and
 * nothing else happens, ever. No reminder, no second offer, no count of how many were
 * declined, and no surface anywhere that could show one.
 *
 * The two controls differ in treatment because they are different kinds of thing rather
 * than a primary and a secondary of the same thing. `CLARITY_LOGIC_ENGINE.md` 6.1's rule
 * that both options must feel equally valid governs the Pulse, where the two answers are
 * both statements about the person; here one accepts a plan and one closes the subject, and
 * 11.1 names the treatments outright.
 *
 * After an accept, 8.2 item 26: the pill fills, the label crossfades to a confirmation and
 * it settles at reduced prominence. **It never bounces, never celebrates and produces no
 * toast.** The fill is not animated from a tap point here, because that is the Pulse's
 * treatment for its own vocabulary and this pill is not a Pulse answer.
 */
@Composable
internal fun ClosingLine(
    eyebrow: String,
    closing: ReportClosing,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contemplative = LocalContemplativeColors.current
    val type = LocalClarityTypography.current

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = eyebrow,
            style = type.caption,
            color = contemplative.textDim,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(ClaritySpacing.scaled(EYEBROW_GAP)))
        Text(
            text = closing.line,
            style = type.closingLine,
            color = contemplative.textBright,
            textAlign = TextAlign.Center,
        )

        if (closing.offersPlan) {
            Spacer(Modifier.height(ClaritySpacing.scaled(ANSWER_GAP)))
            AcceptPill(accepted = closing.accepted, onAccept = onAccept)
            if (!closing.accepted) {
                Spacer(Modifier.height(ClaritySpacing.scaled(DECLINE_GAP)))
                Decline(onDecline = onDecline)
            }
        }
    }
}

@Composable
private fun AcceptPill(accepted: Boolean, onAccept: () -> Unit) {
    val contemplative = LocalContemplativeColors.current
    val type = LocalClarityTypography.current
    val gold = ReportPalette.gold.calmed(LocalCalmMode.current)
    val interaction = remember { MutableInteractionSource() }
    val label = stringResource(if (accepted) R.string.report_accepted else R.string.report_accept)

    Box(
        modifier = Modifier
            .heightIn(min = PILL_MIN_HEIGHT)
            .clarityPressScale(interaction, enabled = !accepted, label = "reportAccept")
            .clip(CircleShape)
            .background(gold.copy(alpha = if (accepted) SETTLED_ALPHA else PILL_ALPHA))
            .clarityFocusRing(interaction, CircleShape)
            .clarityClickable(
                enabled = !accepted,
                interactionSource = interaction,
                haptic = ClarityHapticEvent.SELECT,
                role = Role.Button,
                onClickLabel = label,
                onClick = onAccept,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = type.bodyStrong,
            color = if (accepted) contemplative.textDim else contemplative.textBright,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(
                horizontal = PILL_PADDING,
                vertical = ClaritySpacing.scaled(PILL_PADDING_VERTICAL,
            )),
        )
    }
}

@Composable
private fun Decline(onDecline: () -> Unit) {
    val contemplative = LocalContemplativeColors.current
    val type = LocalClarityTypography.current
    val interaction = remember { MutableInteractionSource() }
    val label = stringResource(R.string.report_decline)

    Box(
        modifier = Modifier
            .heightIn(min = ClaritySpacing.minTouchTarget)
            .clarityFocusRing(interaction, CircleShape)
            .clarityClickable(
                interactionSource = interaction,
                haptic = ClarityHapticEvent.TAP,
                role = Role.Button,
                onClickLabel = label,
                onClick = onDecline,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = type.body,
            // design-v3.md 11.1 item 8 said `textFaint` here and that section is
            // corrected: this is the label of a control a person taps, at 2.637 to one
            // against 13's floor of 4.5. **What makes declining costless is that it is
            // text only against an accept pill in gold**, which is the whole of item
            // 8's asymmetry and is structural rather than a matter of opacity. A
            // decline nobody can read is not costless, it is hidden.
            color = contemplative.textDim,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = PILL_PADDING),
        )
    }
}

/**
 * What the headline block shows while a regenerate is in flight.
 * `MASTER_BUILD_PROMPT.md` 12.3 and `design-v3.md` 8.2 item 22.
 *
 * **12.3 calls this a spinner and it is not one**, and the two documents settle that
 * between themselves rather than needing an owner. 12.3 owns behavior and says the wait is
 * shown on the headline block only and that regenerating is near instant; `design-v3.md`
 * owns everything visual and item 22 is one sentence long: placeholder shimmer, 4 percent
 * ink moving slowly, **never a spinner**. `CLAUDE.md`'s authority order gives the look to
 * `design-v3.md`, so the wait is a shimmer in the shape of the headline, it is on the
 * headline block and nothing else, and the rest of the page does not move.
 *
 * **The percentage is inverted for a dark ground and that is the one number chosen here.**
 * Four percent ink is a pale grey on the Daylight world's near white canvas and is
 * invisible on `deepBlack`; the same relationship the other way up is a low band with a
 * brighter one traveling through it. Under reduce motion nothing travels and the bars sit
 * still, which is 8.3 rather than an exception to it.
 */
@Composable
internal fun HeadlinePlaceholder(modifier: Modifier = Modifier) {
    val contemplative = LocalContemplativeColors.current
    val motion = clarityMotion()
    val base = contemplative.textBright.copy(alpha = PLACEHOLDER_ALPHA)
    val crest = contemplative.textBright.copy(alpha = PLACEHOLDER_CREST)

    val travel by rememberInfiniteTransition(label = "reportShimmer").animateFloat(
        initialValue = 0f,
        targetValue = if (motion.reduced) 0f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = SHIMMER_MILLIS, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Restart,
        ),
        label = "reportShimmerTravel",
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(PLACEHOLDER_GAP),
    ) {
        PLACEHOLDER_WIDTHS.forEach { width ->
            Box(
                modifier = Modifier
                    .width(width)
                    .height(PLACEHOLDER_HEIGHT)
                    .clip(RoundedCornerShape(PLACEHOLDER_RADIUS))
                    .drawBehind {
                        drawRect(base)
                        if (motion.reduced) return@drawBehind
                        val span = size.width
                        val center = -span + travel * (span * SHIMMER_SPAN)
                        drawRect(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color.Transparent, crest, Color.Transparent),
                                startX = center,
                                endX = center + span,
                            ),
                        )
                    },
            )
        }
    }
}

/**
 * Newsreader at optical size 28. See the note on [PatternBreak].
 *
 * One face rather than a family of weights, because nothing in the pattern block asks for a
 * second weight. `ClarityType.kt` documents why a single face family pins the weight axis,
 * and here that is the intent rather than a hazard.
 */
private val PatternSerif = FontFamily(
    Font(
        resId = R.font.newsreader,
        weight = FontWeight(PATTERN_WEIGHT),
        style = FontStyle.Normal,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(PATTERN_WEIGHT),
            FontVariation.opticalSizing(PATTERN_OPTICAL_SIZE.sp),
        ),
    ),
)

/** 5.3's `bodySerif` weight, kept so only the optical size differs. */
private const val PATTERN_WEIGHT = 400

/** 11.1 item 7. */
private const val PATTERN_OPTICAL_SIZE = 28f

/** 11.1 item 7. Inset from a full bleed, so the measure is narrower than the body's. */
private val PATTERN_INSET = 30.dp

/** 11.1 item 7. Gold at 4.5 percent. */
private const val GROUND_ALPHA = 0.045f

/** How much air the band holds above and below its two lines. */
private val PATTERN_VERTICAL = 26.dp

/** The air between a sidehead and the prose under it. */
internal val SIDEHEAD_GAP = 12.dp

/** 10.12. Where the hairline starts, after the label. */
private val SIDEHEAD_RULE_GAP = 10.dp
private val EYEBROW_GAP = 14.dp
private val ANSWER_GAP = 24.dp
private val DECLINE_GAP = 6.dp

/** 3.3. A hairline, and a gradient rather than a solid line. */
private val RULE_HEIGHT = 1.dp
private const val RULE_ALPHA = 0.45f

/** 11.1 item 8. The accept pill is gold at 14 percent. */
private const val PILL_ALPHA = 0.14f

/** 8.2 item 26. Accepted, it settles at reduced prominence and never celebrates. */
private const val SETTLED_ALPHA = 0.07f

private val PILL_MIN_HEIGHT = 48.dp
private val PILL_PADDING = 26.dp
private val PILL_PADDING_VERTICAL = 12.dp

/** Two bars in the proportion a two line headline lands in. */
private val PLACEHOLDER_WIDTHS = listOf(240.dp, 170.dp)
private val PLACEHOLDER_HEIGHT = 26.dp
private val PLACEHOLDER_RADIUS = 6.dp
private val PLACEHOLDER_GAP = 12.dp

/** See the note on [HeadlinePlaceholder]. Four percent inverted for a dark ground. */
private const val PLACEHOLDER_ALPHA = 0.05f
private const val PLACEHOLDER_CREST = 0.11f

/** 8.2 item 22: moving slowly. */
private const val SHIMMER_MILLIS = 1_600
private const val SHIMMER_SPAN = 3f
