package com.kamsiob.claritynow.ui.report

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
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
import kotlin.math.hypot

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
 * and a decline in text only at `textDim` reading `Not this week`.
 *
 * ## Roman, never italic
 *
 * 11.1 says it in bold and section 14 and 15.1 say why: serif italic used as an accent is
 * on the tell list, and this is the one line on the page most likely to attract one. The
 * style is taken from the theme and never copied with a `fontStyle`, so there is no italic
 * anywhere in this file to remove later.
 *
 * ## The sentence changes and the pill settles, and that is everything an accept does
 *
 * `ReportClosing.line` is the nominal offer until the plan is accepted and the plan's
 * stored first person line afterwards, so the block stops offering and starts stating.
 * Nothing else in the app changes: no toast, no celebration, no bounce, no haptic heavier
 * than an ordinary tap, and no notification, badge, widget or home screen card. The plan
 * exists in the report and nowhere else, and `PlanSurfaceTest` reads the sources to keep
 * that true rather than trusting this paragraph.
 *
 * ## Both optional, both costless, neither ever mentioned again
 *
 * That is a rule about the whole app rather than a note about this screen.
 * `MASTER_BUILD_PROMPT.md` 11.4 requires an explicit decline, because one button is not a
 * choice, and there is no `PLAN_DECLINED` event to write: declining removes the block and
 * nothing else happens, ever. No reminder, no second offer, no count of how many were
 * declined, and no surface anywhere that could show one. `CLARITY_LOGIC_ENGINE.md` 10.5
 * goes further and makes ignoring both identical to declining, so somebody who reads the
 * line and scrolls past has already answered.
 *
 * ## Where the two options sit, which 11.1 leaves open. `design-v3.md` 15
 *
 * **The statistically common answer is a row**: two controls on one baseline, the
 * dismissive one leading and the affirmative one trailing, which is what a card and a
 * dialog do with a yes and a no everywhere. It is refused twice over.
 *
 * A row is a dialog footer, and a dialog footer is a question that holds the page until it
 * is answered. This block is the opposite of that by construction, because not answering
 * is a complete answer. And two things on one baseline read as a matched pair whatever
 * their treatment, with the one carrying a ground winning the pair, so a row would take
 * 11.1's one deliberate asymmetry and put a second, louder one on top of it.
 *
 * **Stacked and centered on the page's own axis instead**, so the block reads as three
 * things at the end of a page of centered prose: a sentence, one thing that can be done
 * about it, and a way to close the subject. The decline sits on the same axis, in the same
 * text color and at the same size as the body, which is how it stays quiet without being
 * hidden.
 *
 * **And the air under the pill is the air above it**, [DECLINE_GAP] against [ANSWER_GAP],
 * which is the second half of the same decision. The common answer groups the two controls
 * within four to eight dp of one another and separates the group from the content, and
 * that grouping is exactly what makes them read as a pair of buttons. The counter argument
 * is real, that both are answers to the line above and belong together, and it loses to
 * the same sentence: a group of two reads as a question with a required answer. The two
 * numbers differ because the decline's 48dp touch target already carries about 18dp of its
 * own space above its text while the closing line leaves about 6dp of descent below its
 * own, so 24dp and 12dp of layout draw as roughly 30dp each. Worth an owner's glance on
 * the device, like every optical number in this section.
 *
 * ## What a settled pill looks like, which 11.1 also leaves open
 *
 * 8.2 item 26: the pill fills from the center over 250ms, the label crossfades to a
 * confirmation, and it settles at reduced prominence. **The fill traveling out from the
 * center is the reduced prominence arriving, not a brightening.** The obvious reading of a
 * pill that fills is that it fills to a stronger gold, which is a flash of light at the
 * moment somebody accepts, which is the celebration section 10 forbids, and which would
 * need a peak value no document states. Filling to the settled value needs no second
 * number and no second animation: one front leaves the center, and what is behind it is
 * the pill at rest. Nothing here ever gets brighter than it already was.
 *
 * **The ground halves and the label does not.** Seven percent is fourteen halved, the one
 * reduction that invents no number, and it is the ground rather than the label because the
 * ground is what made the pill a control. Dimming both would be two reductions where 6.1's
 * habit through this whole design is one, and it would make the sentence somebody has just
 * chosen the hardest thing on the page to read.
 *
 * **A settled pill is not a control and stops describing itself as one.** It carries no
 * click handling, no focus ring and no button role, so it reaches TalkBack as the word it
 * shows rather than as a disabled button. There is no un-accept: the event is written, a
 * second acceptance is ignored by the repository, and a control that did nothing would be
 * a lie about that.
 *
 * The decline leaves over the same 250ms rather than vanishing on the frame of the tap,
 * because an element removed with no transition during another element's animation reads
 * as a glitch and moves the footer under the reader's eye. It borrows item 26's duration
 * and 8.3's reduced path and introduces no timing of its own.
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
    val leavingMillis = settleMillis(clarityMotion().reduced)
    val leaving = tween<Float>(leavingMillis)
    val collapsing = tween<IntSize>(leavingMillis)

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
            AnimatedVisibility(
                visible = !closing.accepted,
                // It never comes back. A block that offered again after an accept would be
                // the second offer 11.1 rules out, and the only route back to an offer is a
                // week in which the engine composes a new plan.
                enter = EnterTransition.None,
                exit = fadeOut(leaving) + shrinkVertically(collapsing, Alignment.Top),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(Modifier.height(ClaritySpacing.scaled(DECLINE_GAP)))
                    Decline(onDecline = onDecline)
                }
            }
        }
    }
}

@Composable
private fun AcceptPill(accepted: Boolean, onAccept: () -> Unit) {
    val gold = ReportPalette.gold.calmed(LocalCalmMode.current)
    val reduced = clarityMotion().reduced
    val interaction = remember { MutableInteractionSource() }
    val offer = stringResource(R.string.report_accept)
    val confirmation = stringResource(R.string.report_accepted)

    val settle by animateFloatAsState(
        targetValue = if (accepted) 1f else 0f,
        animationSpec = tween(settleMillis(reduced), easing = EaseOutCubic),
        label = "reportPlanSettle",
    )
    val resting = gold.copy(alpha = PILL_ALPHA)
    val settled = gold.copy(alpha = SETTLED_ALPHA)

    Box(
        modifier = Modifier
            .heightIn(min = PILL_MIN_HEIGHT)
            .clarityPressScale(interaction, enabled = !accepted, label = "reportAccept")
            .clip(CircleShape)
            .drawBehind {
                drawRect(settleGround(settle, resting, settled, reduced, center, size))
            }
            // Everything that makes this a control, and nothing at all once it is not one.
            .then(
                if (accepted) {
                    Modifier
                } else {
                    Modifier
                        .clarityFocusRing(interaction, CircleShape)
                        .clarityClickable(
                            interactionSource = interaction,
                            // design-v3.md 9: `planAccepted` is PRIMITIVE_TICK at 0.5,
                            // deliberately the same weight as an ordinary tap, because
                            // accepting is not an achievement.
                            haptic = ClarityHapticEvent.PLAN_ACCEPTED,
                            role = Role.Button,
                            onClickLabel = offer,
                            onClick = onAccept,
                        )
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        // Both labels stay in the layout, so the pill is the width of the wider of the two
        // from the first frame and the crossfade moves nothing.
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(
                horizontal = PILL_PADDING,
                vertical = ClaritySpacing.scaled(PILL_PADDING_VERTICAL),
            ),
        ) {
            PillLabel(text = offer, opacity = 1f - settle, voiced = settle < HALF)
            PillLabel(text = confirmation, opacity = settle, voiced = settle >= HALF)
        }
    }
}

/**
 * One of the accept pill's two labels, at whatever the crossfade has reached.
 *
 * The one that is not [voiced] is cleared out of the semantics tree, so a screen reader is
 * never handed the offer and the confirmation at once. The caller decides which, from one
 * comparison rather than from each label's own opacity, because at the midpoint of the
 * crossfade both are at exactly half and both would answer the same question yes.
 */
@Composable
private fun PillLabel(text: String, opacity: Float, voiced: Boolean) {
    val contemplative = LocalContemplativeColors.current
    val type = LocalClarityTypography.current
    Text(
        text = text,
        style = type.bodyStrong,
        color = contemplative.textBright,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .alpha(opacity)
            .then(if (voiced) Modifier else Modifier.clearAndSetSemantics { }),
    )
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
 * The accept pill's ground, part way through 8.2 item 26's settle.
 *
 * One brush and therefore one alpha per pixel, which is the whole reason this is a
 * gradient rather than a second translucent shape drawn over the first: gold at 7 percent
 * painted on top of gold at 14 percent is brighter than either, and brighter is the one
 * direction this animation must not go.
 *
 * [reduced] is 8.3, which makes every animation one crossfade. Nothing travels, so the
 * whole ground moves together and the front never exists.
 */
private fun settleGround(
    progress: Float,
    resting: Color,
    settled: Color,
    reduced: Boolean,
    center: Offset,
    size: Size,
): Brush {
    if (progress <= 0f) return SolidColor(resting)
    if (progress >= 1f) return SolidColor(settled)
    if (reduced) return SolidColor(lerp(resting, settled, progress))
    val radius = hypot(size.width, size.height) / 2f
    if (radius <= 0f) return SolidColor(resting)
    val trail = (progress - SETTLE_FEATHER).coerceAtLeast(0f)
    val stops = if (trail > 0f) {
        arrayOf(0f to settled, trail to settled, progress to resting, 1f to resting)
    } else {
        arrayOf(0f to settled, progress to resting, 1f to resting)
    }
    return Brush.radialGradient(colorStops = stops, center = center, radius = radius)
}

/** 8.2 item 26's 250ms, or 8.3's one crossfade. */
private fun settleMillis(reduced: Boolean) = if (reduced) REDUCED_MILLIS else SETTLE_MILLIS

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

/**
 * The air above and below the accept pill. See the note on [ClosingLine] for why they are
 * one decision, why they are not one number, and what they draw as.
 */
private val ANSWER_GAP = 24.dp
private val DECLINE_GAP = 12.dp

/** 3.3. A hairline, and a gradient rather than a solid line. */
private val RULE_HEIGHT = 1.dp
private const val RULE_ALPHA = 0.45f

/** 11.1 item 8. The accept pill is gold at 14 percent. */
private const val PILL_ALPHA = 0.14f

/**
 * 8.2 item 26. Accepted, it settles at reduced prominence and never celebrates.
 *
 * [PILL_ALPHA] halved, which is the one reduction that invents a number no document
 * states. See the note on [ClosingLine].
 */
private const val SETTLED_ALPHA = 0.07f

/** 8.2 item 26. The settle runs 250ms; 8.1's `easeOut` supplies the curve at it. */
private const val SETTLE_MILLIS = 250

/**
 * How much of the pill's half diagonal the settling front is soft over.
 *
 * A hard circular edge sweeping outward from a tap is a Material ripple, which 15.1's
 * habit of naming the common answer would call this animation's tell, and it is also the
 * shape of a thing arriving rather than of a thing settling. A quarter of the radius is
 * enough that what the eye reads is the pill quietly going dim from the middle.
 */
private const val SETTLE_FEATHER = 0.25f

/** The midpoint of the label crossfade, which decides which label is the one being read. */
private const val HALF = 0.5f

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
