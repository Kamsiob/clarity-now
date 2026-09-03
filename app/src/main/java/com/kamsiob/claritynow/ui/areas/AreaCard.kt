package com.kamsiob.claritynow.ui.areas

import androidx.compose.foundation.layout.offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.core.animateFloatAsState
import kotlinx.coroutines.delay
import com.kamsiob.claritynow.ui.components.clarityFocusRing
import com.kamsiob.claritynow.ui.components.clarityClickable
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.draw.drawBehind
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Spacer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kamsiob.claritynow.ui.theme.ClaritySpacing
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import com.kamsiob.claritynow.R
import com.kamsiob.claritynow.ui.components.ClarityIcon
import com.kamsiob.claritynow.ui.components.ClarityIcons
import com.kamsiob.claritynow.ui.components.areaTint
import com.kamsiob.claritynow.ui.components.opticalGlyphNudge
import com.kamsiob.claritynow.ui.theme.ClarityHapticEvent
import com.kamsiob.claritynow.ui.theme.LocalClarityColors
import com.kamsiob.claritynow.ui.theme.LocalClarityHaptics
import com.kamsiob.claritynow.ui.theme.LocalClarityTypography
import com.kamsiob.claritynow.ui.theme.areaLabelColor
import com.kamsiob.claritynow.ui.theme.clarityMotion
import com.kamsiob.claritynow.ui.theme.parseAreaColor

/** The brightness the wash reaches at the peak of a promotion, design-v3.md 8.2 item 1. */
private const val PROMOTION_WASH_PEAK = 0.11f

/**
 * design-v3.md 10.3, the A2 slim card.
 *
 * No border, no colored stripe, no edge treatment. The card carries a shadow in the
 * light world and a lightness step in the dark world, and the area accent appears
 * only as a 7dp dot, a low opacity wash and the label text.
 */
@Composable
fun AreaCardContent(
    area: AreaCardModel,
    promotion: PromotionCue?,
    onPromotionPlayed: () -> Unit,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier,
    textSemantics: Modifier = Modifier,
) {
    val colors = LocalClarityColors.current
    val type = LocalClarityTypography.current
    val motion = clarityMotion()
    val accent = parseAreaColor(area.colorHex)
    val inSession = area.focusMinutesRemaining != null

    val baseWash = when {
        area.isIdle -> 0f
        inSession -> colors.cardWashActiveAlpha
        else -> colors.cardWashAlpha
    }
    val wash = remember { Animatable(baseWash) }
    LaunchedEffect(baseWash) { if (promotion == null) wash.snapTo(baseWash) }

    // The hero. Old and new titles are never both at full opacity.
    val outgoing = remember { Animatable(0f) }
    val incoming = remember { Animatable(0f) }
    var playing by remember { mutableStateOf<PromotionCue?>(null) }
    val played by rememberUpdatedState(onPromotionPlayed)

    // **The app's one moment, and it had no haptic for eleven phases.** `PROMOTE` was
    // defined in `ClarityHaptics`, `rememberPromotionHaptic` was written to fire it, and
    // nothing called either: an item completing and the next one rising, which is the
    // whole mechanic happening in front of somebody, landed in silence. It fires as the
    // new title arrives rather than when the cue is handed over, so the feeling and the
    // movement are the same event.
    val promotionHaptic = rememberPromotionHaptic()

    LaunchedEffect(promotion?.id) {
        val cue = promotion ?: return@LaunchedEffect
        playing = cue
        outgoing.snapTo(0f)
        incoming.snapTo(0f)
        promotionHaptic()
        if (motion.reduced) {
            // A crossfade, not a snap. design-v3.md 8.3 turns an animation into a
            // crossfade under reduced motion and calm mode; it does not delete it.
            // Snapping made the struck through title vanish on the same frame the new
            // one appeared, so the card never said which item had just been completed,
            // which is the one thing this cue exists to say. The translation offsets
            // are dropped, because those are the movement; the fade is not.
            coroutineScope {
                launch { outgoing.animateTo(1f, tween(motion.promotionMillis)) }
                launch { incoming.animateTo(1f, tween(motion.promotionMillis)) }
            }
        } else {
            coroutineScope {
                launch { wash.animateTo(PROMOTION_WASH_PEAK, tween(250)); wash.animateTo(baseWash, tween(250)) }
                launch { outgoing.animateTo(1f, tween(motion.promotionMillis)) }
                launch { incoming.animateTo(1f, motion.springStandard()) }
            }
        }
        playing = null
        played()
    }

    // **The card is a body and a deck, and the deck is its one separation device.**
    //
    // The status line used to be a fourth line of text inside one padded block, so a
    // card was four things at one rank with nothing to say which was the object and
    // which was its state. The deck is a tone step of the area's own accent across the
    // full width, which 6.1 ranks as a legal separator and which 6.1 also means the card
    // may never grow a hairline to divide itself. It appears only when there is state to
    // report, so a resting card is one block and a running one is two.
    Column(modifier = modifier.areaTint(accent, wash.value)) {
        Row(
            modifier = Modifier.padding(
                horizontal = ClaritySpacing.cardPaddingHorizontal,
                // `cardPaddingVertical`, which is `scaled(12.dp)` and had no call sites
                // at all, rather than the 16 that was hardcoded here. A token that
                // exists and is not used is a number waiting to disagree with itself.
                vertical = ClaritySpacing.cardPaddingVertical,
            ),
        ) {
        // **The leading gutter, which is the whole of why this app now reads as a to-do
        // app.** See `CompletionControl` and the September 3 amendment to design-v3.md
        // 10.3. It is reserved on every card and drawn only when there is something to
        // finish, so an idle card and an active one share a left edge and the text column
        // never moves under a thumb.
        CompletionGutter(area = area, onComplete = onComplete)

        Column(modifier = textSemantics) {
        // **The card had no right edge, and this row is where it gets one.**
        //
        // Every string on this card is leading aligned and ragged right, so the
        // rightmost third to half of every card was empty and the only vertical
        // alignment anywhere on the right half of the screen was the ragged ends of
        // four titles. The queue count used to sit at the bottom left as a fourth
        // stacked caption, indistinguishable in size, weight and color from the first
        // step above it. Moved to the trailing end of the identity row it becomes the
        // card's second alignment and stops being the card's fourth identical line.
        //
        // It is also what makes the area name's 55dp indent read: the name is now the
        // leading half of a band that spans the card, rather than one stray line set in
        // further than the title beneath it.
        //
        // **No badge, no pill, no color, and never a bare numeral.** `queue_waiting`
        // reads `3 more`, in the card's own caption ink. CLAUDE.md rule 10 and
        // design-v3.md 10.3.
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(ClaritySpacing.areaDot)
                    .clip(CircleShape)
                    .background(if (area.isIdle) accent.copy(alpha = 0.45f) else accent),
            )
            Text(
                text = area.name,
                style = type.label,
                color = areaLabelColor(accent, colors),
                // A 40 character name wraps at 200 percent and pushed the dot out of
                // shape. `fill = false` keeps the dot at its declared size.
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .padding(start = ClaritySpacing.tight)
                    .weight(1f, fill = false),
            )
            Spacer(Modifier.weight(1f))
            trailingReadout(area)?.let { readout ->
                Text(
                    text = readout,
                    style = type.caption,
                    color = colors.inkSecondary,
                    textAlign = TextAlign.End,
                    // Two, so a long name takes the width and this wraps rather than
                    // either of them clipping at the 200 percent scale.
                    maxLines = 2,
                    modifier = Modifier.padding(start = ClaritySpacing.snug),
                )
            }
        }

        Box(modifier = Modifier.fillMaxWidth().padding(top = ClaritySpacing.tight)) {
            val cue = playing
            if (cue != null) {
                Text(
                    text = cue.previousTitle,
                    style = type.itemTitle.copy(textDecoration = TextDecoration.LineThrough),
                    color = colors.inkPrimary,
                    // A translation for the same reason as the incoming title below,
                    // though this one is a tween and never went negative.
                    modifier = Modifier
                        .alpha(1f - outgoing.value)
                        .graphicsLayer {
                            translationY = if (motion.reduced) 0f else 8.dp.toPx() * outgoing.value
                        },
                )
            }
            if (area.isIdle) {
                Text(
                    // **Which invitation depends on whether there is a queue behind it.**
                    // An empty area has never been used and can be asked for a first item.
                    // An area whose queue is full has plenty of items and no active one,
                    // which is what dismissing the swap chooser, completing inside a focus
                    // session with `Choose from queue` set, and the re-entry screen's
                    // second option all produce, the last of them on every card at once.
                    text = stringResource(idleTitleFor(area)),
                    // 500 rather than itemTitle's 650, design-v3.md 10.3. The
                    // invitation is the same string at the same size as a real item
                    // title, one step lighter, so an empty card reads as a place
                    // waiting to be filled rather than as a card that is already
                    // full. This line rendered at 650 through phases 2 and 3b: the
                    // sans family pinned a single weight instance, so the copy had
                    // nothing to resolve against and was dropped in silence. See
                    // ClarityType.kt. The tracking stays at the role's own value,
                    // because the size and the job have not changed.
                    style = type.itemTitle.copy(fontWeight = FontWeight(400)),
                    // inkSecondary, and 10.3 says inkTertiary. **That is a
                    // contradiction inside design-v3.md and section 13 wins**, because
                    // a floor is a floor: `inkTertiary` measures 2.40 to one on the
                    // card and 3.22 in dark, against 13's 4.5, and 10.3 calls this
                    // same string the most important one on the screen. Resolved in
                    // 10.3 rather than patched here. `inkSecondary` is the one step
                    // down from `inkPrimary` that clears, at 5.29 to one in light and
                    // 6.36 in dark on the phase 3c card, and the weight above already
                    // carries the "one step lighter" the idle state is for.
                    color = colors.inkSecondary,
                )
            } else {
                Text(
                    text = area.activeItemTitle.orEmpty(),
                    style = type.itemTitle,
                    color = colors.inkPrimary,
                    // **Two lines, and design-v3 10.3's four line budget is why.** A title
                    // may be 200 characters, which at 21.5sp on a 371dp measure is about
                    // nine lines, so one long item could make a card taller than the
                    // phone while the first step and the status line under it were both
                    // capped at one. The full text is in the detail sheet, which is where
                    // reading happens.
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    // **This crashed the app on every completion, and nothing had found
                    // it because completing was hard to reach.**
                    //
                    // The rise was written as top padding of `(16 * (1 - incoming)).dp`.
                    // `incoming` runs on `springStandard`, which is underdamped and
                    // overshoots past 1, so the expression goes negative and Compose
                    // throws `IllegalArgumentException: Padding must be non-negative`.
                    // The app's single most important moment, the promotion, took the
                    // process down whenever the spring did what that spring is for. It
                    // surfaced the hour a visible completion control was added, which is
                    // the whole argument for visible controls in one sentence.
                    //
                    // `graphicsLayer` rather than a clamp, because padding was the wrong
                    // tool regardless: it relayouts the column on every frame of the
                    // animation, and a translation is a draw-time offset that cannot go
                    // out of range and cannot move anything else.
                    modifier = if (cue == null) {
                        Modifier
                    } else {
                        Modifier
                            .alpha(incoming.value)
                            .graphicsLayer {
                                translationY = if (motion.reduced) {
                                    0f
                                } else {
                                    16.dp.toPx() * (1f - incoming.value)
                                }
                            }
                    },
                )
            }
        }

        FirstStepLine(area = area)
        StatusLine(area = area)
        }
        }
        FocusDeck(area = area, accent = accent)
    }
}

/**
 * The gutter the completion control sits in, reserved whether or not the control is drawn.
 *
 * **Reserved rather than conditional**, which is the one layout decision here worth
 * stating. An idle card has nothing to complete and 10.16 is explicit that the app prefers
 * a control to be absent rather than present and inert, because "a disabled control is a
 * question the user then has to answer". Taking the *space* with it would move the text
 * column sideways between one card and the next and again the moment a card went idle,
 * which is COGA o4p01 and is the thing this audience is least able to absorb. So the
 * column is fixed and only the ink is conditional.
 *
 * The control is top aligned rather than centered. A title runs to two lines and a first
 * step sits under it, so a vertically centered control drifts down the card as the content
 * grows and stops reading as belonging to the title. Sorted 3 aligns to the first title
 * line for the same reason and it is the convention across the apps that were measured.
 */
@Composable
private fun CompletionGutter(area: AreaCardModel, onComplete: () -> Unit) {
    Box(
        modifier = Modifier
            // **The gutter is the target's width, not the ring's, and the first build of
            // this got it wrong.** It was `CONTROL_SIZE + snug`, 34dp, and a `size(48.dp)`
            // inside a 34dp parent is coerced to 34: the ring drew correctly and the touch
            // target was 14dp short in the axis a thumb misses on, so the first tap of the
            // new control fell through to the card underneath and opened the detail sheet.
            //
            // The 48dp is section 13's floor and the platform's, and it is now real.
            .width(ClaritySpacing.minTouchTarget)
            // Pulled back into the card's own padding so the **ring's ink** lands on the
            // content edge rather than the target's box doing. A 22dp ring centered in a
            // 48dp target sits 13dp inside it, which is the same correction the header
            // glyphs make against the screen measure.
            .offset(x = -CONTROL_INSET)
            // The identity row sits above the title, so the control drops to land on the
            // title's first line rather than beside the area name.
            .padding(top = ClaritySpacing.scaled(CONTROL_DROP)),
    ) {
        if (area.offersComplete) {
            CompletionControl(onComplete = onComplete)
        }
    }
}

/**
 * Tap to finish the one thing this area is on.
 *
 * ## Why it exists, in one paragraph
 *
 * Until the September 3 2026 amendment to `design-v3.md` 10.3 there was no visible way to
 * complete anything. A test user said the app looked nothing like a to-do app and that you
 * could not tell what you were looking at; the words `task`, `to-do` and `done` appeared
 * zero times in any user-visible string; and the only paths to Complete were a swipe, a
 * long press and a sheet, all three invisible. A straight swipe is a path-based gesture, so
 * that arrangement was a **WCAG 2.1 SC 2.5.1 Level A failure** matching published failure
 * F105, and NN/g's finding on this exact interaction is that most people never find such a
 * gesture except by accident while trying to delete something.
 *
 * ## Leading, and the research split on this
 *
 * Three research passes agreed the control had to exist and one of them argued for the
 * **trailing** edge, on two grounds: it keeps the item title as the first thing the eye
 * meets, which 10.3 calls the most important string on the screen, and a leading box makes
 * a row read as a checklist, which is the shape the shame literature attaches to.
 *
 * It is leading, and the reasons are stronger. Material's list guidance says outright that
 * "states and primary actions are placed on the left side of a list tile". Of the apps
 * measured, Todoist, Apple Reminders, Things, Sorted, Sunsama, Akiflow, Amazing Marvin,
 * Goblin Tools, Numo, Habitica and Routinery all lead; Tiimo and Structured trail. And the
 * deciding argument is the one this whole change exists for: **the category judgment is
 * made in about 50ms and it is made on prototypicality.** A leading circle is the single
 * most diagnostic mark a to-do app has. Putting it anywhere else spends novelty on exactly
 * the feature that cannot afford it, which is the mistake being corrected.
 *
 * The accusation argument does not reach this card, and that is the honest reason it can
 * be set aside rather than overruled: it is about a **column** of unchecked boxes standing
 * as a tally of what a person has not done. This screen shows one item per area and keeps
 * the rest quiet. There is no column.
 *
 * ## The shape, and what each number is for
 *
 * A 22dp ring inside a 48dp target, which is section 13's touch floor and the platform's.
 * The stroke is `inkSecondary`, which measures 5.78 to one on canvas and better on a card,
 * comfortably past 13's 3.0 floor for a graphic; `inkTertiary` would have been the quieter
 * choice and measures 2.40, which is why it is not used anywhere a person has to find
 * something.
 *
 * **The ring is not a second separation device.** Rule 11 and 6.1 govern how an element is
 * told apart from the ground it sits on, and the card has already spent its device on
 * elevation. This stroke is not a boundary drawn around content, it is the control's own
 * shape: remove it and there is no control, only a gap. The same reading `Interactions.kt`
 * records for the focus ring, which belongs to a state rather than to a boundary.
 *
 * ## What happens on tap
 *
 * The write goes immediately. Immediacy is the half of the evidence that actually holds:
 * Barkley's account of executive function asks for the gap between an action and its
 * consequence to be compressed at the point of performance, and delay aversion in this
 * population is a medium effect across 4,320 children. So there is no confirmation, no
 * pending state and no held write.
 *
 * The ring fills and takes the check for as long as the promotion runs, and then the next
 * item arrives in the title above it and the ring is empty again. That is the app's whole
 * mechanic performed in one gesture, and it is what the swipe was hiding.
 *
 * **`positiveGreen` for the fill and `positiveInk` for the check**, which is not a choice
 * made here: 3.1 scopes the first to "completion only, and a fill only" and lists "the
 * completion check" as the first job of the second. The palette already had a token whose
 * stated purpose was this glyph.
 *
 * Undo is the five second window the completion already had, `AreasRoute`, which is also
 * what `SC 2.5.2` wants standing behind any control that commits on contact.
 */
@Composable
private fun CompletionControl(onComplete: () -> Unit) {
    val colors = LocalClarityColors.current
    val motion = clarityMotion()
    val interaction = remember { MutableInteractionSource() }
    val label = stringResource(R.string.cd_complete_active_item)

    // Local, and deliberately not derived from the model. The item is gone from this card
    // within a frame of the write, so a checked state read back from state would never be
    // seen. This is the acknowledgment, and it lasts exactly as long as the promotion it
    // introduces.
    var taken by remember { mutableStateOf(false) }
    val fill by animateFloatAsState(
        targetValue = if (taken) 1f else 0f,
        animationSpec = if (motion.reduced) motion.effects() else motion.springSnappy(),
        label = "completionFill",
    )
    LaunchedEffect(taken) {
        if (!taken) return@LaunchedEffect
        delay(motion.promotionMillis.toLong())
        taken = false
    }

    Box(
        modifier = Modifier
            .size(ClaritySpacing.minTouchTarget)
            .clip(CircleShape)
            .clarityFocusRing(interaction, CircleShape)
            .semantics {
                role = Role.Checkbox
                contentDescription = label
            }
            .clarityClickable(
                interactionSource = interaction,
                haptic = ClarityHapticEvent.TAP,
                pressShape = CircleShape,
                onClick = {
                    taken = true
                    onComplete()
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(CONTROL_SIZE)
                .drawBehind {
                    // The ring, and only the ring. **The fill is a `background` below
                    // rather than a second `drawCircle`**, which is not a style
                    // preference: `design-v3.md` 3.1 scopes `positiveGreen` to
                    // "completion only, and a fill only", and `FaintInkTest` enforces
                    // that by reading every `color =` and `tint =` in the app. A fill
                    // drawn through a color parameter is indistinguishable, to that test,
                    // from a glyph drawn in the same token. Every other fill in the app
                    // goes through `Modifier.background`; this one does too.
                    if (fill < 1f) {
                        val stroke = STROKE.toPx()
                        drawCircle(
                            color = colors.inkSecondary,
                            radius = (size.minDimension - stroke) / 2f,
                            style = Stroke(width = stroke),
                        )
                    }
                },
            contentAlignment = Alignment.Center,
        ) {
            if (fill > 0f) {
                Box(
                    modifier = Modifier
                        .size(CONTROL_SIZE * fill)
                        .clip(CircleShape)
                        .background(colors.positiveGreen),
                )
            }
            if (fill > 0.35f) {
                ClarityIcon(
                    icon = ClarityIcons.check,
                    contentDescription = null,
                    tint = colors.positiveInk,
                    modifier = Modifier.size(CONTROL_SIZE * 0.62f).alpha(fill),
                )
            }
        }
    }
}

/** The ring. 22dp of ink inside a 48dp target. */
private val CONTROL_SIZE = 22.dp

/** Heavy enough to read as a control rather than as a drawn outline. */
private val STROKE = 2.dp

/** Half the difference between the target and the ring, so the ink lands on the measure. */
private val CONTROL_INSET = (ClaritySpacing.minTouchTarget - CONTROL_SIZE) / 2

/**
 * How far the 48dp target drops so the ring inside it lands on the title's first line.
 *
 * The identity row above is a 9dp dot beside `label` at 13.5sp on an 18sp line box, then
 * `tight` separating it from the title, whose line box is 27sp. So the first title line
 * centers about 35dp down and the ring, which sits 24dp into its own target, wants its
 * target to start 11dp down.
 *
 * Scaled, so the control follows the text it is aligned to when a person raises their text
 * size rather than staying where it was at the default.
 */
private val CONTROL_DROP = 11.dp

/**
 * Row three, design-v3.md 10.3 and 10.17. The active item's first step, Addendum
 * 01 4b.
 *
 * **Absent entirely when there is none.** No placeholder, no dash, no reserved row
 * that changes the card's height, and above all no invitation to add one: the whole
 * value of this field is that it is free, and a card that asks for it turns a help
 * into a chore. design-v3.md 10.3 says it in one line, `a card is not a form`.
 *
 * **`body` 15 rather than `caption` 12.5, which reverses an earlier decision here.**
 * That decision said a body sized first step competes with the title, and it is right
 * that it must not; what it produced was a card with two type sizes doing four jobs, on
 * which the identity line, the next physical action and the queue count were all drawn
 * at 12.5 and stacked. 10.17 calls the first step the thing that makes an item
 * startable, and it was set at the size of a timestamp. At 15 against the title's 21.5
 * the hierarchy is unambiguous by size alone and does not need the extra step down; the
 * competition the old comment feared was with a 12.5 count line that has since moved to
 * the trailing end of row one.
 *
 * One line, ellipsized, because design-v3.md 10.3 caps the card at four lines and
 * makes this the row that truncates first when a status line is also present. The
 * full text is in the area detail sheet, which is where reading happens.
 *
 * An idle area draws nothing here even if some queued item has a first step. The
 * card is about the one thing that is happening, and there is not one.
 */
@Composable
private fun FirstStepLine(area: AreaCardModel) {
    val colors = LocalClarityColors.current
    val type = LocalClarityTypography.current
    val firstStep = area.activeItemFirstStep?.takeIf { !area.isIdle } ?: return

    Text(
        text = firstStep,
        style = type.body,
        color = colors.inkSecondary,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        // Tighter than the 6dp above the status line, because this line belongs to
        // the title above it and the status line belongs to the card as a whole.
        modifier = Modifier.padding(top = ClaritySpacing.tight),
    )
}

/**
 * **The deck, and the line that is not on it.**
 *
 * design-v3.md 10.3's status row was one composable doing two unrelated jobs: reporting a
 * running session, and reporting how long an area has been quiet. They are opposites. A
 * session is state the app is holding right now and it belongs on its own ground, where
 * it can carry the area's accent at full strength without competing with the title. An
 * idle line is the absence of state and belongs under the title as one more quiet
 * caption, because giving absence its own deck would make an untouched area the loudest
 * card on the screen.
 *
 * The weight hack goes with the split. The comment on the old line said weight was "the
 * only device available to say so", which was true when the line sat inside the same
 * padded block as everything else. On a deck the device is the ground, so the text can
 * be `label` and stop shouting in a register it does not have.
 */
@Composable
private fun FocusDeck(area: AreaCardModel, accent: Color) {
    val colors = LocalClarityColors.current
    val type = LocalClarityTypography.current
    val minutes = area.focusMinutesRemaining ?: return

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
        modifier = Modifier
            .fillMaxWidth()
            .areaTint(accent, colors.cardDeckAlpha)
            .padding(
                horizontal = ClaritySpacing.cardPaddingHorizontal,
                vertical = ClaritySpacing.scaled(10.dp),
            ),
    ) {
        ClarityIcon(
            icon = ClarityIcons.focus,
            contentDescription = null,
            tint = accent,
            // The play triangle carries its mass left of its bounding box, so
            // centering it by layout leaves it looking off center.
            modifier = Modifier.size(13.dp).opticalGlyphNudge(13.dp),
        )
        Text(
            text = pluralStringResource(R.plurals.area_in_focus_minutes, minutes, minutes),
            style = type.label,
            color = accent,
            modifier = Modifier.padding(start = ClaritySpacing.hair + 1.dp),
        )
    }
}

/**
 * Which invitation an idle card carries, in one place because it is read twice.
 *
 * Three states, and the third was missing. An area with a queue is asked to pick what is
 * next. An area that has **never held anything** is asked for a first item. An area that
 * has held things and finished them all was asked for a first item too, which is the app
 * telling somebody who has completed nine things in that area that they have not started:
 * `Add your first item` sitting directly above `Last active 2 days ago`.
 *
 * `neverHeldAnything` was already computed and already correct, and was applied only to
 * the status line. This is the same fact governing the title it was always about.
 */
@Composable
private fun idleTitleFor(area: AreaCardModel): Int = when {
    area.queueLength > 0 -> R.string.area_idle_queued_title
    area.neverHeldAnything -> R.string.area_idle_title
    else -> R.string.area_idle_cleared_title
}

/**
 * Row four, `design-v3.md` 10.3: the status line, drawn only when it carries information.
 *
 * > Idle areas show `Last active 21 days ago`. In-session areas show the live countdown.
 * > An ordinary active area shows nothing.
 *
 * ## The queue count, issue #65, and why it is a choice rather than an addition
 *
 * This is a queue app whose main screen hid the queue: with eleven things behind Work the
 * card was identical to the day there was one. The app knew and would not say, while the
 * All Areas widget printed the count on the home screen. So the count is here now, in the
 * card's own caption and ink, **with no badge, no dot and no color**, which is what the
 * focus group asked for and what `CLAUDE.md` rule 10 requires in any case.
 *
 * It is one line and not two, because 10.3 caps the card at four rows and the budget was
 * already spent. So this row states one thing, chosen in this order:
 *
 * 1. **A running session states nothing here.** The countdown is the status, and it has
 *    its own deck below. Two status rows for one card is the four line budget broken by a
 *    surface that already had its answer
 * 2. **A queue states what is waiting.** This is the fact a person opens the screen for
 * 3. **An idle area with no queue states how long it has been.** The count would be zero
 *    and absent, so the row is free for the one thing left to say
 *
 * The obvious composition, `Last active 3 days ago, 3 waiting`, is the one thing rule 14
 * says to interrogate, and it fails on its own terms: it wraps to two rows at 200 percent
 * text and it says twice what the card already says once, because an idle area with a
 * queue is titled `Pick what is next` two rows above. Speech has no line budget and takes
 * both, which is what `areaCardDescription` builds.
 */
/**
 * What the identity row reports at its trailing end, or null for nothing.
 *
 * **Nothing while a session is running.** The deck under the card carries the live
 * countdown next to the area's own accent, and two surfaces reporting one card's state
 * in one screenful is how a person learns to read neither.
 *
 * **Nothing when the queue is empty**, rather than `0 waiting`, for the same reason the
 * inbox door is absent at zero: an app that prints its zeroes is an app keeping score.
 * A card with no queue has its full width for its name, which is what a person with a
 * long area name and nothing waiting should get.
 *
 * This is deliberately not the whole of the old status line. `Last active 3 days ago`
 * stays where it is, under the title, because it is an observation about an absence and
 * belongs in the quiet register rather than at the top of the card. `StatusLine` carries
 * why the two facts are mutually exclusive.
 */
@Composable
private fun trailingReadout(area: AreaCardModel): String? = when {
    area.focusMinutesRemaining != null -> null
    area.queueLength > 0 -> pluralStringResource(
        R.plurals.queue_waiting,
        area.queueLength,
        area.queueLength,
    )
    else -> null
}

@Composable
private fun StatusLine(area: AreaCardModel) {
    val colors = LocalClarityColors.current
    val type = LocalClarityTypography.current

    // 1. The deck is the status while a session is running.
    if (area.focusMinutesRemaining != null) return

    val text = when {
        // 2. **The queue count is not here any more; it is the trailing end of row
        //    one.** The exclusivity this list encodes is unchanged, and it has to be:
        //    `Last active 3 days ago` and `3 waiting` still never appear together,
        //    because this branch returns for any area that has a queue. What changed is
        //    only where the count is drawn.
        area.queueLength > 0 -> return

        // 3. **An area nothing has ever been in draws no status line at all.**
        //
        // Fixing `Last active today` on a brand new card left it reading `Add your first
        // item` over `Nothing here yet`, which is two sentences making one point on the
        // emptiest card in the app. The title is the invitation and it is enough; this
        // line exists to report a last activity, and there has not been one.
        !area.isIdle || area.neverHeldAnything -> return

        area.daysSinceLastEvent <= 0 -> stringResource(R.string.area_last_active_today)

        else -> pluralStringResource(
            R.plurals.area_last_active_days,
            area.daysSinceLastEvent,
            area.daysSinceLastEvent,
        )
    }

    Text(
        text = text,
        style = type.caption,
        color = colors.inkSecondary,
        // One line, for the budget. A count never reaches it and a last active line at
        // 200 percent would, and the age is the half of this row that can be lost.
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.padding(top = ClaritySpacing.tight),
    )
}

fun areaCardDescription(
    area: AreaCardModel,
    idleTitle: String,
    focusStatus: String? = null,
): String = buildString {
    append(area.name)
    append(". ")
    append(area.activeItemTitle ?: idleTitle)
    if (!area.isIdle) {
        area.activeItemFirstStep?.let {
            append(". ")
            append(it)
        }
        focusStatus?.let {
            append(". ")
            append(it)
        }
    }
    if (area.queueLength > 0) {
        append(". ")
        append(area.queueLength)
        // `more`, matching the visible count. See `queue_waiting` for why `waiting` was
        // wrong: in this category it means blocked on somebody else, which is the
        // opposite of what an item in this queue is.
        append(if (area.queueLength == 1) " more item here" else " more items here")
    }
}

/** Kept next to the card so the semantics and the visuals cannot drift apart. */
@Composable
fun AreaCardSemantics(area: AreaCardModel, modifier: Modifier = Modifier): Modifier {
    // The same branch the visible title takes. When only one of the two learned it, the
    // card said "Pick what is next" and the description said "Add your first item", which
    // is the bug this pass fixed visually reappearing in speech.
    val idleTitle = stringResource(
        idleTitleFor(area),
    )
    val minutes = area.focusMinutesRemaining
    val focusStatus = if (minutes == null) {
        null
    } else {
        pluralStringResource(R.plurals.area_in_focus_minutes, minutes, minutes)
    }
    return modifier.clearAndSetSemantics {
        contentDescription = areaCardDescription(area, idleTitle, focusStatus)
    }
}

/** The haptic that fires as a newly promoted title lands. */
@Composable
fun rememberPromotionHaptic(): () -> Unit {
    val haptics = LocalClarityHaptics.current
    return remember(haptics) { { haptics.perform(ClarityHapticEvent.PROMOTE) } }
}
