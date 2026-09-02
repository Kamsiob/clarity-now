package com.kamsiob.claritynow.ui.areas

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
    modifier: Modifier = Modifier,
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

    LaunchedEffect(promotion?.id) {
        val cue = promotion ?: return@LaunchedEffect
        playing = cue
        outgoing.snapTo(0f)
        incoming.snapTo(0f)
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
        Column(
            modifier = Modifier.padding(
                horizontal = ClaritySpacing.cardPaddingHorizontal,
                vertical = ClaritySpacing.scaled(16.dp),
            ),
        ) {
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
        }

        Box(modifier = Modifier.fillMaxWidth().padding(top = ClaritySpacing.tight)) {
            val cue = playing
            if (cue != null) {
                Text(
                    text = cue.previousTitle,
                    style = type.itemTitle.copy(textDecoration = TextDecoration.LineThrough),
                    color = colors.inkPrimary,
                    modifier = Modifier
                        .alpha(1f - outgoing.value)
                        .padding(top = if (motion.reduced) 0.dp else (8 * outgoing.value).dp),
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
                    text = stringResource(
                        if (area.queueLength > 0) {
                            R.string.area_idle_queued_title
                        } else {
                            R.string.area_idle_title
                        },
                    ),
                    // 500 rather than itemTitle's 650, design-v3.md 10.3. The
                    // invitation is the same string at the same size as a real item
                    // title, one step lighter, so an empty card reads as a place
                    // waiting to be filled rather than as a card that is already
                    // full. This line rendered at 650 through phases 2 and 3b: the
                    // sans family pinned a single weight instance, so the copy had
                    // nothing to resolve against and was dropped in silence. See
                    // ClarityType.kt. The tracking stays at the role's own value,
                    // because the size and the job have not changed.
                    style = type.itemTitle.copy(fontWeight = FontWeight(500)),
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
                    modifier = if (cue == null) {
                        Modifier
                    } else {
                        Modifier
                            .alpha(incoming.value)
                            .padding(
                                top = if (motion.reduced) {
                                    0.dp
                                } else {
                                    (16 * (1f - incoming.value)).dp
                                },
                            )
                    },
                )
            }
        }

        FirstStepLine(area = area)
        StatusLine(area = area)
        }
        FocusDeck(area = area, accent = accent)
    }
}

/**
 * Row three, design-v3.md 10.3 and 10.17. The active item's first step, Addendum
 * 01 4b.
 *
 * **Absent entirely when there is none.** No placeholder, no dash, no reserved row
 * that changes the card's height, and above all no invitation to add one: the whole
 * value of this field is that it is free, and a card that asks for it turns a help
 * into a chore. design-v3.md 10.3 says it in one line, `a card is not a form`.
 *
 * `caption` rather than `body`, which is the rule worth stating rather than leaving
 * to a style constant. At body weight it competes with the title. It is read second,
 * at the moment the title has already failed to start someone, and the hierarchy on
 * the card has to say so.
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
        style = type.caption,
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
@Composable
private fun StatusLine(area: AreaCardModel) {
    val colors = LocalClarityColors.current
    val type = LocalClarityTypography.current

    // 1. The deck is the status while a session is running.
    if (area.focusMinutesRemaining != null) return

    val text = when {
        // 2. What is behind the active item, in the words the widget uses.
        area.queueLength > 0 -> pluralStringResource(
            R.plurals.queue_waiting,
            area.queueLength,
            area.queueLength,
        )

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
        append(if (area.queueLength == 1) " item waiting" else " items waiting")
    }
}

/** Kept next to the card so the semantics and the visuals cannot drift apart. */
@Composable
fun AreaCardSemantics(area: AreaCardModel, modifier: Modifier = Modifier): Modifier {
    // The same branch the visible title takes. When only one of the two learned it, the
    // card said "Pick what is next" and the description said "Add your first item", which
    // is the bug this pass fixed visually reappearing in speech.
    val idleTitle = stringResource(
        if (area.queueLength > 0) R.string.area_idle_queued_title else R.string.area_idle_title,
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
