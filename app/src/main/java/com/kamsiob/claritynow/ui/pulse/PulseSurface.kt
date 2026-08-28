package com.kamsiob.claritynow.ui.pulse

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kamsiob.claritynow.R
import com.kamsiob.claritynow.domain.engine.catalog.ResponseOption
import com.kamsiob.claritynow.domain.pulse.DailyPulse
import com.kamsiob.claritynow.ui.components.ClarityIcon
import com.kamsiob.claritynow.ui.components.ClarityIcons
import com.kamsiob.claritynow.ui.components.clarityClickable
import com.kamsiob.claritynow.ui.components.clarityFocusRing
import com.kamsiob.claritynow.ui.components.clarityPressScale
import com.kamsiob.claritynow.ui.theme.ClaritySpacing
import com.kamsiob.claritynow.ui.theme.LocalCalmMode
import com.kamsiob.claritynow.ui.theme.LocalClarityTypography
import com.kamsiob.claritynow.ui.theme.LocalContemplativeColors
import com.kamsiob.claritynow.ui.theme.PulsePalette
import com.kamsiob.claritynow.ui.theme.calmed
import com.kamsiob.claritynow.ui.theme.clarityMotion
import kotlinx.coroutines.delay

/**
 * The Pulse itself: the question, the answer, and what the room settles to.
 *
 * design-v3.md section 11. The observation in `readSerif` centered, the question in
 * `body` at `textDim`, then response pills. After answering, an acknowledgment fades in,
 * then ambient mode: a fourteen day rhythm row, today's answered Pulse, and a History
 * entry.
 *
 * ## Dismissing is a first class answer and this file proves it by omission
 *
 * `MASTER_BUILD_PROMPT.md` 11.6 and `CLARITY_LOGIC_ENGINE.md` 6.2: not answering is a
 * fully supported state, never chased, never counted against the user and never
 * mentioned. There is no code here that could break that, because there is nothing to
 * write: **no dismissal is recorded, no counter is kept, no return prompt exists, and no
 * string in this file or in `strings.xml` refers to a Pulse that went unanswered.** The
 * only representation of it anywhere is the hollow ring in the rhythm row, which is a
 * mark and not a message. A later session looking for the place that nudges will not find
 * one, and adding one is a change to three documents rather than a line of code.
 *
 * ## Which phase is showing
 *
 * Four, derived rather than stored, so there is no state that can disagree with the log:
 *
 * - **loading**, while the log is being read. Nothing is drawn over the backdrop
 * - **the question**, when today's entry exists and is unanswered
 * - **answering**, from the tap until the acknowledgment has been read. Local to this
 *   composition, because it is a moment rather than a fact, and 12.1's PRESENTED state is
 *   deliberately never written down
 * - **ambient**, when today's entry is answered, when the day is IDLE, and after the
 *   answering sequence finishes
 */
@Composable
internal fun PulseSurface(
    state: PulseUiState,
    onAnswer: (ResponseOption) -> Unit,
    onOpenHistory: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val motion = clarityMotion()
    val entry = state.today?.entry

    // Keyed on the entry, so a day rolling over or a first load arriving resets both.
    var chosen by remember(entry?.id) { mutableStateOf<ResponseOption?>(null) }
    var settled by remember(entry?.id) { mutableStateOf(false) }

    val acknowledgment = state.today?.acknowledgment

    // design-v3.md 8.2 item 10, then item 11. The sequence runs once per answer and its
    // only effect is to move the surface to ambient at the end of it. The write happened
    // in the tap handler and does not wait for any of this.
    LaunchedEffect(chosen) {
        if (chosen == null) return@LaunchedEffect
        val fill = if (motion.reduced) REDUCED_MILLIS else FILL_MILLIS
        val fade = if (motion.reduced) REDUCED_MILLIS else ACK_FADE_MILLIS
        val ack = if (acknowledgment == null) 0 else fade + ACK_HOLD_MILLIS
        delay((fill + HOLD_MILLIS + ack).toLong())
        settled = true
    }

    val ambient = entry == null || settled || (entry.isAnswered && chosen == null)

    Box(modifier = modifier.fillMaxSize()) {
        if (state.loading) return@Box

        Crossfade(
            targetState = ambient,
            animationSpec = tween(
                durationMillis = if (motion.reduced) REDUCED_MILLIS else SETTLE_MILLIS,
            ),
            label = "pulseSettle",
        ) { showAmbient ->
            if (showAmbient) {
                PulseAmbient(state = state, onOpenHistory = onOpenHistory)
            } else if (state.today != null) {
                PulseQuestion(
                    pulse = state.today,
                    chosen = chosen,
                    onAnswer = { option ->
                        chosen = option
                        onAnswer(option)
                    },
                )
            }
        }
    }
}

/**
 * The question, and the answer landing on it. design-v3.md section 11 and 8.2 item 10.
 *
 * The observation is `readSerif` centered and the question is `body` at `textDim`,
 * verbatim from the specification. Both are strings the engine wrote and the log stored;
 * nothing here composes, shortens or reformats either of them.
 *
 * **The pills are stacked rather than set side by side**, which design-v3.md leaves open
 * and section 15 therefore asks to be decided rather than defaulted. Side by side is the
 * obvious answer and it has two costs. It puts one option on the left, and in a left to
 * right reading order the left position is a recommendation, which
 * `CLARITY_LOGIC_ENGINE.md` 6.1 forbids the interface from making. And it does not
 * survive `quietDay`, which has three options, so the surface would rearrange itself
 * between families for a reason the person cannot see. Stacked, identical in width and in
 * treatment, one layout answers both.
 *
 * **The acknowledgment is composed from the first frame at zero opacity**, so its space
 * is reserved and nothing moves when it arrives. It is hidden from a screen reader until
 * it is visible and is then announced as a polite live region, which is how somebody who
 * cannot see it fading in hears it at all.
 *
 * An entry whose stage has been retired from the corpus arrives with no responses. The
 * observation and the question still render and there are simply no pills, which is the
 * unanswerable state and is exactly the state dismissing already produces. Nothing is
 * said about it.
 */
@Composable
private fun PulseQuestion(
    pulse: DailyPulse,
    chosen: ResponseOption?,
    onAnswer: (ResponseOption) -> Unit,
    modifier: Modifier = Modifier,
) {
    val contemplative = LocalContemplativeColors.current
    val type = LocalClarityTypography.current
    val motion = clarityMotion()

    // The opacity is an `Animatable` read only inside a draw lambda, so the fade costs no
    // recomposition. The flag beside it is what the semantics need, and it flips once
    // rather than on every frame of the fade.
    val acknowledgmentAlpha = remember { Animatable(0f) }
    var acknowledged by remember { mutableStateOf(false) }
    LaunchedEffect(chosen) {
        if (chosen == null) return@LaunchedEffect
        val fill = if (motion.reduced) REDUCED_MILLIS else FILL_MILLIS
        delay((fill + HOLD_MILLIS).toLong())
        acknowledged = true
        acknowledgmentAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = if (motion.reduced) REDUCED_MILLIS else ACK_FADE_MILLIS,
                easing = EaseOutCubic,
            ),
        )
    }

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(
                    horizontal = ClaritySpacing.screenPadding,
                    vertical = ClaritySpacing.scaled(ROOM_PADDING,
                )),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = pulse.entry.observation,
                style = type.readSerif,
                color = contemplative.textBright,
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(max = MEASURE),
            )

            pulse.entry.question?.let { question ->
                Spacer(Modifier.height(ClaritySpacing.scaled(18.dp)))
                Text(
                    text = question,
                    style = type.body,
                    color = contemplative.textDim,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.widthIn(max = MEASURE),
                )
            }

            if (pulse.responses.isNotEmpty()) {
                Spacer(Modifier.height(ClaritySpacing.scaled(36.dp)))
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(ClaritySpacing.scaled(10.dp)),
                ) {
                    pulse.responses.forEach { option ->
                        PulseResponsePill(
                            option = option,
                            selected = option.key == chosen?.key,
                            dimmed = chosen != null && option.key != chosen.key,
                            enabled = chosen == null,
                            onSelect = { onAnswer(option) },
                        )
                    }
                }
            }

            pulse.acknowledgment?.let { line ->
                Spacer(Modifier.height(ClaritySpacing.scaled(26.dp)))
                Text(
                    text = line,
                    style = type.body,
                    color = contemplative.textDim,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .widthIn(max = MEASURE)
                        .graphicsLayer { alpha = acknowledgmentAlpha.value }
                        .then(
                            if (acknowledged) {
                                Modifier.semantics { liveRegion = LiveRegionMode.Polite }
                            } else {
                                Modifier.clearAndSetSemantics { }
                            },
                        ),
                )
            }
        }
    }
}

/**
 * Ambient mode. design-v3.md section 11.
 *
 * The rhythm row, then today, then the History entry. Today is the answered Pulse on a
 * day that had one and a short fixed line on a day that did not.
 *
 * **Today is a block of type and not a container**, which is the one place this surface
 * had to choose between two sentences in design-v3.md. Section 11 calls it "today's
 * answered card" and section 14 forbids a card inside a sheet, with 10.6 adding that its
 * single exception may not be extended by analogy and 15.3 listing "a card inside a sheet
 * to give its rows an affordance" among the refusals. The two are reconciled by 6.1,
 * which orders the separation devices and says to stop as soon as one reads: whitespace
 * is the first, and in a room this empty it reads on its own. So today gets air rather
 * than a surface, no rule is bent, and nothing is drawn that section 14 would have to
 * make an exception for.
 *
 * **The IDLE line says nothing about the person.** A silent day is the engine's own
 * discretion, per `CLARITY_LOGIC_ENGINE.md` 5.1, and the two sentences shown for it
 * describe how the Pulse works rather than how the day went. That is what keeps them out
 * of the corpus and inside `strings.xml`: `MASTER_BUILD_PROMPT.md` 11.2 admits fixed
 * copy about the app's own behavior, and 14b.11 asks for exactly this kind of line
 * because interface behavior that arrives unannounced is a real cost. A sentence here
 * that mentioned activity, quietness, or anything the person did would be an observation
 * and would have to come from a corpus through the engine, which is why there is not one.
 */
@Composable
private fun PulseAmbient(
    state: PulseUiState,
    onOpenHistory: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contemplative = LocalContemplativeColors.current
    val type = LocalClarityTypography.current
    val accent = PulsePalette.accent.calmed(LocalCalmMode.current)
    val entry = state.today?.entry

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(
                    horizontal = ClaritySpacing.screenPadding,
                    vertical = ClaritySpacing.scaled(ROOM_PADDING,
                )),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            PulseRhythmRow(marks = state.rhythm, modifier = Modifier.widthIn(max = MEASURE))

            Spacer(Modifier.height(ClaritySpacing.sectionGap))

            if (entry != null) {
                Text(
                    text = entry.observation,
                    style = type.bodySerif,
                    color = contemplative.textBright,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.widthIn(max = MEASURE),
                )
                entry.question?.let { question ->
                    Spacer(Modifier.height(ClaritySpacing.scaled(6.dp)))
                    Text(
                        text = question,
                        style = type.body,
                        color = contemplative.textDim,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.widthIn(max = MEASURE),
                    )
                }
                entry.responseLabel?.let { label ->
                    Spacer(Modifier.height(ClaritySpacing.scaled(20.dp)))
                    Text(
                        text = stringResource(R.string.pulse_answered_label),
                        style = type.caption,
                        // design-v3.md 13's 55 percent floor. The eyebrow is a rank
                        // under the question by being `caption` against `body`, which
                        // is 5.3's scale doing the work; 32 percent measured 2.637 to
                        // one on this ground.
                        color = contemplative.textDim,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(ClaritySpacing.scaled(4.dp)))
                    Text(
                        // Verbatim, off the event. `PULSE_ANSWERED` stores the label the
                        // pill carried, so this quotes what the person actually saw.
                        text = label,
                        style = type.bodyStrong,
                        color = accent,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.widthIn(max = MEASURE),
                    )
                }
            } else {
                Text(
                    text = stringResource(R.string.pulse_idle_title),
                    style = type.bodySerif,
                    color = contemplative.textBright,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.widthIn(max = MEASURE),
                )
                Spacer(Modifier.height(ClaritySpacing.scaled(8.dp)))
                Text(
                    text = stringResource(R.string.pulse_idle_body),
                    style = type.body,
                    color = contemplative.textDim,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.widthIn(max = MEASURE),
                )
            }

            Spacer(Modifier.height(ClaritySpacing.sectionGap))

            PulseHistoryEntry(
                onClick = onOpenHistory,
                modifier = Modifier.widthIn(max = MEASURE),
            )
        }
    }
}

/**
 * The History entry. design-v3.md section 11 and the 10.15 table, which calls it a row.
 *
 * A label and a chevron, at 10.11's settings row treatment rather than at 10.7's, because
 * it navigates rather than acts. It is always here, including on the day there is nothing
 * behind it: a row that comes and goes is a row a person has to look for, and the page it
 * opens says for itself when it is empty.
 *
 * One separation device, 6.1: whitespace. No hairline, no fill, no shadow, and the
 * Contemplative world has no shadows to give it anyway.
 */
@Composable
private fun PulseHistoryEntry(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val contemplative = LocalContemplativeColors.current
    val type = LocalClarityTypography.current
    val interaction = remember { MutableInteractionSource() }
    val label = stringResource(R.string.pulse_history_row)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clarityPressScale(interaction, label = "pulseHistoryRow")
            .heightIn(min = ClaritySpacing.minTouchTarget)
            .clip(RoundedCornerShape(ROW_RADIUS))
            .clarityFocusRing(interaction, RoundedCornerShape(ROW_RADIUS))
            .clarityClickable(
                interactionSource = interaction,
                role = Role.Button,
                onClickLabel = label,
                onClick = onClick,
            )
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = label, style = type.bodyStrong, color = contemplative.textBright)
        ClarityIcon(
            icon = ClarityIcons.chevron,
            contentDescription = null,
            tint = contemplative.textDim,
            modifier = Modifier.size(CHEVRON),
        )
    }
}

/**
 * The measure every line on this surface is held to.
 *
 * A serif observation set the full width of a modern phone runs past the comfortable
 * line length, and this surface exists to be read once, slowly. The same measure is used
 * for the ambient block and the acknowledgment so the column has one edge.
 */
private val MEASURE = 320.dp

/** Room to breathe at both ends of the sheet, above the design's 20dp screen padding. */
private val ROOM_PADDING = 26.dp

private val ROW_RADIUS = 12.dp
private val CHEVRON = 20.dp

/** design-v3.md 8.2 item 10. */
private const val FILL_MILLIS = 220
private const val HOLD_MILLIS = 250
private const val ACK_FADE_MILLIS = 400

/**
 * How long the acknowledgment is held before the room settles.
 *
 * design-v3.md does not give this number, so section 15 applies and it is chosen rather
 * than defaulted. The corpus calls these lines "shown briefly", and they are short: a
 * second and a bit is one unhurried reading of `That is worth knowing.` and is short of
 * the point where a person starts wondering whether the screen is stuck. Under reduce
 * motion the two fades shorten and this hold does not, for the reason design-v3.md 8.4
 * keeps the empty state's delay: a hold is not motion.
 */
private const val ACK_HOLD_MILLIS = 1_100

/** design-v3.md 8.2 item 11, the ambient settle. */
private const val SETTLE_MILLIS = 450

/** design-v3.md 8.3. */
private const val REDUCED_MILLIS = 150
