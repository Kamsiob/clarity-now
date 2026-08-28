package com.kamsiob.claritynow.ui.onboarding

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kamsiob.claritynow.R
import com.kamsiob.claritynow.ui.theme.ClaritySpacing
import com.kamsiob.claritynow.ui.theme.LocalCalmMode
import com.kamsiob.claritynow.ui.theme.LocalClarityShapes
import com.kamsiob.claritynow.ui.theme.LocalClarityTypography
import com.kamsiob.claritynow.ui.theme.LocalContemplativeColors
import com.kamsiob.claritynow.ui.theme.OnboardingPalette
import com.kamsiob.claritynow.ui.theme.PulsePalette
import com.kamsiob.claritynow.ui.theme.ReportPalette
import com.kamsiob.claritynow.ui.theme.calmed
import com.kamsiob.claritynow.ui.theme.clarityMotion
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Beat 4, The Depth. MASTER_BUILD_PROMPT 13.1, and Addendum 01 8b's one line.
 *
 * Four auto paced moments with the captions 13.1 states verbatim, about twenty two
 * seconds end to end, and a tap advances at any point.
 *
 * ## The last moment does not auto advance, and that is deliberate
 *
 * 13.1 puts the beat at twenty to twenty five seconds, which the three paced moments plus
 * moment four's own reveals spend. What moment four does not do is time out into the app:
 * it carries the line Addendum 01 8b requires and then waits.
 *
 * **8b is the reason.** It asks for one line at the end of onboarding announcing the
 * Pulse before it ever appears, because "predictability matters enormously to autistic
 * users, and interface behavior that arrives unannounced is a real cost". A line that
 * appears for two seconds and is then replaced by the app on a timer is exactly the
 * unannounced behavior the addendum is objecting to, one level up. So the last thing
 * onboarding does is hold still and let the person leave when they have read it.
 *
 * ## The line is fixed copy and must stay that way
 *
 * 14b.11 says so in a sentence written for whoever reads this next: "this is the exact
 * kind of sentence a session will be tempted to route through the engine, and it must
 * not". It is a statement about how the app behaves, not an observation about a person,
 * and MASTER_BUILD_PROMPT 11.2 puts onboarding's strings in `strings.xml` by name.
 *
 * ## The glow
 *
 * design-v3.md 3.3 gives beat 4 a cycle of amber, blue and gold for three moments and
 * leaves the philosophy moment on black, which is what 13.1 calls it. The colors are
 * resolved by [glowForMoment] and drawn by the backdrop, so nothing in this file paints
 * a background.
 */
@Composable
internal fun OnboardingBeatFour(
    moment: Int,
    onMoment: (Int) -> Unit,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val motion = clarityMotion()

    LaunchedEffect(moment) {
        if (moment >= ONBOARDING_LAST_MOMENT) return@LaunchedEffect
        delay(MOMENT_MILLIS)
        onMoment(moment + 1)
    }

    Crossfade(
        targetState = moment,
        animationSpec = motion.easeSlow(),
        label = "onboardingMoment",
        modifier = modifier.fillMaxSize(),
    ) { current ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = ClaritySpacing.screenPadding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            when (current) {
                0 -> Philosophy()
                1 -> PulseMoment()
                2 -> MomentumMoment()
                else -> ReportMoment(onFinish = onFinish)
            }
        }
    }
}

/** Moment 1. Two lines on black, the second arriving after the first has been read. */
@Composable
private fun ColumnScope.Philosophy() {
    val contemplative = LocalContemplativeColors.current
    val type = LocalClarityTypography.current
    val motion = clarityMotion()
    val second = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        delay(SECOND_LINE_AT)
        second.animateTo(1f, motion.easeOut())
    }

    Text(
        text = stringResource(R.string.onboarding_depth_philosophy_one),
        style = type.readSerif,
        color = contemplative.textBright,
        textAlign = TextAlign.Center,
    )
    Spacer(Modifier.height(18.dp))
    Text(
        text = stringResource(R.string.onboarding_depth_philosophy_two),
        style = type.readSerif,
        color = contemplative.textDim,
        textAlign = TextAlign.Center,
        modifier = Modifier.graphicsLayer { alpha = second.value },
    )
}

/**
 * Moment 2. A sample of the Pulse: one line in the serif and two response pills.
 *
 * **The sample line says what the surface is, not what the person did.** A demonstration
 * observation would be a sentence shaped exactly like the engine's own about someone the
 * app knows nothing about, and 13.1 makes onboarding replayable from Settings, so the
 * same string would later sit on a screen belonging to a person with a year of history.
 *
 * The pills carry the resting treatment `PulseResponsePill` gives every option, identical
 * to each other, because `CLARITY_LOGIC_ENGINE.md` 6.1's rule that no option is the good
 * answer is true of a picture of the surface as well as of the surface.
 */
@Composable
private fun ColumnScope.PulseMoment() {
    val contemplative = LocalContemplativeColors.current
    val type = LocalClarityTypography.current
    val shapes = LocalClarityShapes.current
    val accent = PulsePalette.accent.calmed(LocalCalmMode.current)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shapes.card)
            .background(contemplative.surfaceRaised)
            .padding(horizontal = 22.dp, vertical = 26.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.onboarding_depth_pulse_sample),
            style = type.readSerif,
            color = contemplative.textBright,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(22.dp))
        SamplePill(label = stringResource(R.string.onboarding_depth_pulse_option_one), accent = accent)
        Spacer(Modifier.height(10.dp))
        SamplePill(label = stringResource(R.string.onboarding_depth_pulse_option_two), accent = accent)
    }
    Caption(stringResource(R.string.onboarding_depth_pulse_caption))
}

@Composable
private fun SamplePill(label: String, accent: Color) {
    val contemplative = LocalContemplativeColors.current
    val type = LocalClarityTypography.current
    Box(
        modifier = Modifier
            .widthIn(max = 300.dp)
            .fillMaxWidth()
            .heightIn(min = 50.dp)
            .clip(CircleShape)
            .background(accent.copy(alpha = 0.14f)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = type.bodyStrong,
            color = contemplative.textBright,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 13.dp),
        )
    }
}

/**
 * Moment 3. The fourteen day rhythm row, filling day by day.
 *
 * **Two of the fourteen stay faint on purpose.** design-v3.md 12.2 and the caption 13.1
 * gives this moment are the same statement: a gap is a lighter mark and nothing else,
 * there is no streak and nothing counts consecutive days. A demonstration row with all
 * fourteen filled would teach the opposite of the sentence printed under it.
 *
 * In calm mode the row renders complete rather than cascading, per design-v3.md 16.6
 * item 13.
 */
@Composable
private fun ColumnScope.MomentumMoment() {
    val motion = clarityMotion()
    val calm = LocalCalmMode.current
    val accent = OnboardingPalette.beatFourBlue.calmed(calm)
    val arrived = remember { List(RHYTHM_DAYS) { Animatable(if (calm) 1f else 0f) } }

    LaunchedEffect(Unit) {
        if (calm) return@LaunchedEffect
        arrived.forEachIndexed { index, animatable ->
            launch {
                delay(index * RHYTHM_STAGGER)
                animatable.animateTo(1f, motion.easeOut())
            }
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(RHYTHM_GAP, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        arrived.forEachIndexed { index, animatable ->
            val quiet = index in QUIET_DAYS
            Box(
                modifier = Modifier
                    .size(if (quiet) RHYTHM_DOT_QUIET else RHYTHM_DOT)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = if (quiet) RHYTHM_QUIET_ALPHA else 1f))
                    .graphicsLayer { alpha = animatable.value },
            )
        }
    }
    Caption(stringResource(R.string.onboarding_depth_momentum_caption))
}

/**
 * Moment 4. A miniature of the Report, then the line Addendum 01 8b requires, then the
 * way out.
 *
 * **The sample headline is a description of the page rather than a headline about a
 * week**, for the reason [PulseMoment] gives about its own sample line: this beat is
 * replayable from Settings, and a plausible looking headline shown to a person who has
 * six months of history would read as a claim about that history. The gold, the serif,
 * the eyebrow and the rule are what the moment is demonstrating.
 */
@Composable
private fun ColumnScope.ReportMoment(onFinish: () -> Unit) {
    val contemplative = LocalContemplativeColors.current
    val type = LocalClarityTypography.current
    val calm = LocalCalmMode.current
    val motion = clarityMotion()
    val gold = ReportPalette.gold.calmed(calm)
    val closing = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        delay(CLOSING_AT)
        closing.animateTo(1f, motion.easeOut())
    }

    Text(
        text = stringResource(R.string.onboarding_depth_report_eyebrow),
        style = type.caption,
        color = gold.copy(alpha = 0.75f),
        textAlign = TextAlign.Center,
    )
    Spacer(Modifier.height(14.dp))
    Text(
        text = stringResource(R.string.onboarding_depth_report_headline),
        style = type.displayTitle,
        color = ReportPalette.body,
        textAlign = TextAlign.Center,
    )
    Spacer(Modifier.height(18.dp))
    // design-v3.md 3.3: a Report rule is a horizontal gradient fading to transparent at
    // both ends, never a solid line.
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(
                Brush.horizontalGradient(
                    listOf(Color.Transparent, gold.copy(alpha = 0.55f), Color.Transparent),
                ),
            ),
    )
    Caption(stringResource(R.string.onboarding_depth_report_caption))

    Spacer(Modifier.height(40.dp))
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.graphicsLayer { alpha = closing.value },
    ) {
        Text(
            text = stringResource(R.string.onboarding_pulse_announcement),
            style = type.body,
            color = contemplative.textDim,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(24.dp))
        OnboardingPrimaryButton(
            label = stringResource(R.string.onboarding_start),
            onClick = onFinish,
        )
    }
}

/** The one caption line each of the last three moments carries. 13.1 states each verbatim. */
@Composable
private fun ColumnScope.Caption(text: String) {
    val contemplative = LocalContemplativeColors.current
    val type = LocalClarityTypography.current
    Spacer(Modifier.height(26.dp))
    Text(
        text = text,
        style = type.body,
        color = contemplative.textDim,
        textAlign = TextAlign.Center,
    )
}

/**
 * The glow behind a beat. design-v3.md 3.3: beat 1 actionBlue, beat 2 twilight violet,
 * beat 4 cycling amber, blue and gold.
 *
 * Beat 3 and moment 1 of beat 4 are null, which is the same answer for the same reason.
 * The reveal's light is the Areas screen coming up through the iris, and 13.1 calls the
 * philosophy moment "philosophy on black".
 */
internal fun glowForMoment(beat: OnboardingBeat, moment: Int): Color? = when (beat) {
    OnboardingBeat.SEE_IT_WORK -> OnboardingPalette.beatOne
    OnboardingBeat.YOUR_AREAS -> OnboardingPalette.beatTwo
    OnboardingBeat.THE_REVEAL -> null
    OnboardingBeat.THE_DEPTH -> when (moment) {
        0 -> null
        1 -> OnboardingPalette.beatFourAmber
        2 -> OnboardingPalette.beatFourBlue
        else -> OnboardingPalette.beatFourGold
    }
}

/**
 * The beat's pace. Four moments, three of them timed.
 *
 * 5,500ms each puts the three paced moments at 16.5 seconds, and moment 4 spends another
 * 5 before its closing line has arrived, which lands the beat inside 13.1's twenty to
 * twenty five and leaves the reading of the last line to the reader.
 */
private const val MOMENT_MILLIS = 5_500L
/**
 * The index of the last moment. Read by the route, which owns the moment because a tap
 * anywhere advances it and the gesture layer is up there.
 */
internal const val ONBOARDING_LAST_MOMENT = 3
private const val SECOND_LINE_AT = 1_800L
private const val CLOSING_AT = 2_600L

/** design-v3.md 12.2 and 11. The row is fourteen days and a gap is a lighter mark. */
private const val RHYTHM_DAYS = 14
private val QUIET_DAYS = setOf(4, 9)
private val RHYTHM_DOT = 10.dp
private val RHYTHM_DOT_QUIET = 7.dp
private val RHYTHM_GAP = 7.dp
private const val RHYTHM_QUIET_ALPHA = 0.32f
private const val RHYTHM_STAGGER = 90L
