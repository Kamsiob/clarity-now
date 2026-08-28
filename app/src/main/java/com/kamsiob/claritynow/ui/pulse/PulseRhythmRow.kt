package com.kamsiob.claritynow.ui.pulse

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import com.kamsiob.claritynow.R
import com.kamsiob.claritynow.domain.pulse.PulseDayState
import com.kamsiob.claritynow.ui.theme.LocalCalmMode
import com.kamsiob.claritynow.ui.theme.PulsePalette
import com.kamsiob.claritynow.ui.theme.calmed
import com.kamsiob.claritynow.ui.theme.clarityMotion

/**
 * The fourteen day rhythm row. design-v3.md section 11 and `MASTER_BUILD_PROMPT.md` 12.1.
 *
 * Filled amber means answered, a hollow ring means generated but unanswered, faint means
 * a silent day. **Those three are the whole vocabulary and there is no fourth**, which is
 * enforced by the mark taking a `PulseDayState` rather than a display enum of its own: a
 * fourth kind of mark would need a fourth day state, and the log cannot produce one.
 *
 * ## This is not a streak and must never become one
 *
 * design-v3.md 14 forbids streaks, consecutive day counts, chains and badges outright,
 * and `CLARITY_LOGIC_ENGINE.md` builds an engine with no streak fact in it. The row is
 * the place that would reintroduce one by accident, so:
 *
 * - each mark is drawn from its own day and knows nothing about the day beside it
 * - **a gap is a fainter mark and nothing else.** No break in the row, no dimming of what
 *   follows it, no separator, no color that reads as a lapse
 * - today carries no ring of its own. That is Momentum's treatment, 12.2, and importing
 *   it here would add the fourth state this row is not allowed to have. Today is the
 *   rightmost mark, which is what a row read left to right already says
 * - nothing counts anything. There is no caption, and the spoken description names the
 *   element rather than tallying it
 *
 * ## Why the silent mark is drawn the way it is
 *
 * Faint is the specification, and faint has a floor: design-v3.md 16.7 and WCAG 1.4.11
 * hold a graphic at 3.0 to one. Below that it stops being quiet and starts being absent,
 * and an absent mark would make a fortnight of silence look like a broken row rather
 * than a calm one.
 *
 * **The floor is measured against the ground this row is actually drawn on, and the
 * phase 13 audit is where that was corrected.** At half strength the mark measures 3.003
 * against `deepBlack`, which is the Pulse surface at midday and only at midday: 3.3 blends
 * a whisper of `#2B2340` into the top of that surface from 05 to 11, and on the dawn
 * ground the same mark measures **2.969**. A ground that shifts through the day is a
 * ground the floor has to be met on at every hour of it. At 55 percent the three grounds
 * read 3.373, 3.277 and 3.345, and the calm variant of the accent reads 3.434 on the
 * midday ground 16.7 holds it to all day.
 *
 * **55 percent is the number design-v3.md 13 already states** for Contemplative text
 * meant to be read, and it is the value `textDim` carries in 3.3. This mark is not text
 * and takes the graphic floor rather than that one, and taking the same step as the quiet
 * text on the same surface rather than a step of its own is the point. It is also drawn
 * smaller, so the three states differ in form as well as in opacity and
 * design-v3.md 13's "color is never the only signal" holds without relying on the reader
 * seeing amber at all.
 *
 * ## The entrance, 8.2 item 11
 *
 * The row fills left to right at a 30ms stagger. It is an entrance, so under 8.4 and 16.2
 * it does not fire at all in calm mode and the row renders complete, and under reduce
 * motion it becomes one 150ms crossfade with no stagger.
 *
 * **8.4's once per tab per app session does not map onto a sheet**, and the equivalent
 * here is once per opening of the surface, which is what this composition's own lifetime
 * already gives: the row arrives when ambient mode does, whether that is the settle after
 * an answer or the sheet opening on a day already answered, and does not arrive again
 * while the sheet stays open. The rule 8.4 is protecting is that a screen opened twenty
 * times a day should not be announced twenty times, and this surface is opened once.
 */
@Composable
internal fun PulseRhythmRow(marks: List<PulseMark>, modifier: Modifier = Modifier) {
    val calm = LocalCalmMode.current
    val motion = clarityMotion()
    val accent = PulsePalette.accent.calmed(calm)
    val description = stringResource(R.string.cd_pulse_rhythm, marks.size)

    // design-v3.md 16.2: in calm mode the entrance does not fire and the row renders
    // already settled, so it starts at its target and never animates.
    var settled by remember { mutableStateOf(calm) }
    LaunchedEffect(Unit) { settled = true }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clearAndSetSemantics { contentDescription = description },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        marks.forEachIndexed { index, mark ->
            val appearance by animateFloatAsState(
                targetValue = if (settled) 1f else 0f,
                animationSpec = tween(
                    durationMillis = if (motion.reduced) REDUCED_MILLIS else MARK_FADE_MILLIS,
                    delayMillis = if (motion.reduced) 0 else index * STAGGER_MILLIS,
                ),
                label = "pulseMark",
            )

            Canvas(modifier = Modifier.size(MARK_BOX)) {
                when (mark.state) {
                    // Answered.
                    PulseDayState.ANSWERED -> drawCircle(
                        color = accent,
                        radius = MARK_RADIUS.toPx(),
                        alpha = appearance,
                    )

                    // Generated and not answered. Never chased, never counted, and this
                    // ring is the whole of its representation.
                    PulseDayState.READY -> drawCircle(
                        color = accent,
                        radius = (MARK_RADIUS - RING_STROKE / 2).toPx(),
                        alpha = appearance,
                        style = Stroke(width = RING_STROKE.toPx()),
                    )

                    // A silent day, a suppressed day after a return, and every day before
                    // install. All three are one mark on purpose: an absence and the
                    // app's own discretion are indistinguishable here, which is what
                    // keeps a fortnight away from being drawn as a fortnight away.
                    PulseDayState.IDLE -> drawCircle(
                        color = accent.copy(alpha = QUIET_ALPHA),
                        radius = QUIET_RADIUS.toPx(),
                        alpha = appearance,
                    )
                }
            }
        }
    }
}

/** The box each mark is centered in. Fourteen of these fit a phone with room between. */
private val MARK_BOX = 12.dp
private val MARK_RADIUS = 5.dp
private val RING_STROKE = 1.5.dp
private val QUIET_RADIUS = 3.dp

/**
 * design-v3.md 13's 55 percent, which measures 3.277 to one on the dawn ground, the
 * weakest of the three the Pulse surface can be. See the note above.
 */
private const val QUIET_ALPHA = 0.55f

/** design-v3.md 8.2 item 11. */
private const val STAGGER_MILLIS = 30
private const val MARK_FADE_MILLIS = 200

/** design-v3.md 8.3. */
private const val REDUCED_MILLIS = 150
