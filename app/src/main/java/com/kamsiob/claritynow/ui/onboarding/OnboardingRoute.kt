package com.kamsiob.claritynow.ui.onboarding

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kamsiob.claritynow.R
import com.kamsiob.claritynow.di.ClarityViewModelFactory
import com.kamsiob.claritynow.ui.components.ClarityIcon
import com.kamsiob.claritynow.ui.components.ClarityIcons
import com.kamsiob.claritynow.ui.components.clarityClickable
import com.kamsiob.claritynow.ui.theme.ClarityHapticEvent
import com.kamsiob.claritynow.ui.theme.ClaritySpacing
import com.kamsiob.claritynow.ui.theme.ContemplativeTheme
import com.kamsiob.claritynow.ui.theme.LocalCalmMode
import com.kamsiob.claritynow.ui.theme.LocalClarityShapes
import androidx.compose.runtime.CompositionLocalProvider
import com.kamsiob.claritynow.ui.theme.LocalIsContemplative
import com.kamsiob.claritynow.ui.theme.LocalClarityTypography
import com.kamsiob.claritynow.ui.theme.LocalContemplativeColors
import com.kamsiob.claritynow.ui.theme.clarityMotion
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.hypot

/**
 * Onboarding. MASTER_BUILD_PROMPT 13.1 and 14b.11, design-v3.md section 11 and 10.15.
 *
 * Four beats, Contemplative from the first frame to the last, with one persistent nav
 * overlay over all of them. Tap or swipe left advances, swipe right goes back, and back
 * returns to the previous beat and is hidden on beat 1.
 *
 * ## What [onRevealStarted] is for
 *
 * 13.1's beat 3 uncovers "the user's actual Areas screen rendered live behind the
 * overlay", which means the app itself has to be composed and laid out before the iris
 * opens. This route cannot compose it, so it says when: the caller, `FirstRunGate`, puts
 * the real shell behind this surface the moment the reveal begins and leaves it there.
 * The call happens after the areas have been written and before the iris moves, so what
 * comes up through the hole is the person's own screen with their own areas on it.
 *
 * ## The order inside beat 3, and why the closing line goes first
 *
 * The line fades in on black, holds, and fades out as the iris opens. 13.1 asks for the
 * iris and the line "fading in and out" without fixing their order, and this is the order
 * that keeps the line readable: `textBright` over the revealed Areas screen would be
 * near-white type on the Daylight canvas, and a person whose theme is light would get one
 * unreadable sentence at the emotional peak of the flow. Putting the line before the hole
 * costs nothing and needs no second scrim over somebody's own screen. design-v3.md 15.
 *
 * The nav overlay is the one thing that does sit over the revealed screen, because 13.1
 * says `Jump in` is always visible and that outranks its contrast for the second and a
 * half the transition lasts.
 *
 * ## Tap to advance is not offered on beat 2
 *
 * Beat 2 is the only beat made of controls. Everywhere else a tap anywhere advances, per
 * 13.1; there, a tap is aimed at a chip, a field, a swatch or Continue, and a stray tap
 * that skipped the beat would throw away everything the person had chosen. Swipes still
 * work on every beat, and swiping left out of beat 2 is refused while it has nothing to
 * carry forward, which is the same condition Continue is enabled under.
 */
@Composable
fun OnboardingRoute(
    onRevealStarted: () -> Unit,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel = viewModel(factory = ClarityViewModelFactory),
) {
    ContemplativeTheme(calmMode = LocalCalmMode.current) {
        val state by viewModel.state.collectAsStateWithLifecycle()
        val motion = clarityMotion()
        val todayName = stringResource(R.string.onboarding_today_area)
        val scope = rememberCoroutineScope()

        var moment by rememberSaveable { mutableIntStateOf(0) }
        val reveal = remember { Animatable(0f) }
        val closing = remember { Animatable(0f) }

        LightSystemBars(key = state.beat)

        LaunchedEffect(state.beat) {
            if (state.beat != OnboardingBeat.THE_DEPTH) moment = 0
        }

        LaunchedEffect(state.beat) {
            if (state.beat != OnboardingBeat.THE_REVEAL) {
                // **The closing line is snapped away rather than left where it stopped.**
                // Its fade runs in a child coroutine of this effect, and leaving beat 3
                // cancels this effect and the child with it. A person who taps through
                // beat 3 rather than waiting cancels it mid hold, at full opacity, and
                // `Nothing here can break.` then stayed on the screen for the whole of
                // beat 4, drawn over the Pulse sample's second answer. It is the one
                // defect in this sequence that only appears on the fast path, which is
                // the path the fifteen second budget is about.
                closing.snapTo(0f)
                // Leaving the reveal closes the iris again, which is design-v3.md 8.2
                // item 6's world transition back into the Contemplative room that beat 4
                // is read in. Beats 1 and 2 have never opened it, so this is a no-op.
                reveal.animateTo(0f, motion.easeSlow())
                return@LaunchedEffect
            }

            // 13.1: the selected areas become real events here and nowhere earlier, so an
            // onboarding abandoned before this leaves nothing behind.
            viewModel.commit(todayName)
            viewModel.awaitCommitted()
            onRevealStarted()

            launch {
                delay(CLOSING_IN_AT)
                closing.animateTo(1f, motion.easeOut())
                delay(CLOSING_HOLD)
                closing.animateTo(0f, motion.easeOut())
            }
            delay(IRIS_AT)
            launch { reveal.animateTo(1f, motion.springGentle()) }
            delay(REVEAL_MILLIS - IRIS_AT)
            viewModel.advance()
        }

        val advance = {
            if (state.beat == OnboardingBeat.THE_DEPTH) {
                if (moment < ONBOARDING_LAST_MOMENT) moment += 1 else onFinished()
            } else {
                viewModel.advance()
            }
        }

        // The two gesture detectors below are built once and outlive many recompositions,
        // so what they call has to be the current version of it rather than the one that
        // existed when the detector was created. A stale `advance` on beat 4 would keep
        // advancing to the moment after whichever one was showing when it was captured.
        val advanceNow by rememberUpdatedState(advance)
        val goBackNow by rememberUpdatedState<() -> Unit>({
            if (state.canGoBack) viewModel.back()
        })
        val tapAdvances = state.beat != OnboardingBeat.YOUR_AREAS

        // **Not predictive, issue #63.** Back inside onboarding goes to the previous
        // beat, which is a step in a sequence rather than a screen behind this one, and
        // nothing is composed underneath to uncover. A preview would also say the wrong
        // thing about what the gesture does: on the first beat it leaves the app, and on
        // every other beat it does not, and one animation cannot mean both.
        BackHandler(enabled = state.canGoBack) { viewModel.back() }

        // **Onboarding is a Contemplative surface and has to say so.**
        //
        // 3.3's deepBlack room, drawn outside `ClarityShell`, which is where every other
        // Contemplative surface declares its world. Without the flag the press veil on
        // Skip setup, Continue, the two choice panels, the area rows and all 48 color
        // swatches was `inkPrimary` on near black in the light world: arithmetically
        // present and invisible to a person. The swatches and the color rows carry no
        // press scale either, so tapping one answered with nothing at all.
        CompositionLocalProvider(LocalIsContemplative provides true) {
            Box(modifier = modifier.fillMaxSize()) {
                // Everything behind onboarding is untouchable, which matters from beat 3
                // onward, when the live Areas screen is composed under it and part of this
                // surface is transparent. A sibling drawn behind the content rather than an
                // ancestor of it, so it can never starve the gestures below.
                Spacer(Modifier.fillMaxSize().swallowsPointerInput())

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            var travel = 0f
                            detectHorizontalDragGestures(
                                onDragStart = { travel = 0f },
                                onDragEnd = {
                                    when {
                                        travel <= -SWIPE_THRESHOLD_PX -> advanceNow()
                                        travel >= SWIPE_THRESHOLD_PX -> goBackNow()
                                        else -> Unit
                                    }
                                },
                            ) { _, amount -> travel += amount }
                        }
                        .pointerInput(tapAdvances) {
                            if (!tapAdvances) return@pointerInput
                            detectTapGestures { advanceNow() }
                        },
                ) {
                    // The iris. design-v3.md 8.2 item 18: a circular reveal from center over
                    // 600ms springGentle, and 16.6 item 18 replaces it with a crossfade when
                    // motion is reduced or calm mode is on. Both are the same value driving a
                    // different property, so there is one animation and one branch.
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                compositingStrategy = CompositingStrategy.Offscreen
                                alpha = if (motion.reduced) 1f - reveal.value else 1f
                            }
                            .drawWithContent {
                                drawContent()
                                if (motion.reduced || reveal.value <= 0f) return@drawWithContent
                                drawCircle(
                                    color = Color.Black,
                                    radius = hypot(size.width, size.height) / 2f * reveal.value,
                                    center = center,
                                    blendMode = BlendMode.DstOut,
                                )
                            },
                    ) {
                        OnboardingBackdrop(glow = glowForMoment(state.beat, moment))

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .statusBarsPadding()
                                .navigationBarsPadding()
                                .padding(top = NAV_HEIGHT),
                        ) {
                            when (state.beat) {
                                OnboardingBeat.SEE_IT_WORK -> OnboardingBeatOne(onAdvance = advance)

                                OnboardingBeat.YOUR_AREAS -> OnboardingBeatTwo(
                                    state = state,
                                    onJustStart = viewModel::chooseJustStart,
                                    onPickAreas = viewModel::choosePickAreas,
                                    onFirstItemChange = viewModel::setFirstItemTitle,
                                    onToggleSuggestion = viewModel::toggleSuggestion,
                                    onAddCustom = viewModel::addCustom,
                                    onRemove = viewModel::remove,
                                    onFocus = viewModel::focusOn,
                                    onRecolor = viewModel::recolor,
                                    onContinue = viewModel::advance,
                                )

                                // The reveal draws nothing of its own. What it is made of is
                                // the black being taken away and the closing line above it.
                                OnboardingBeat.THE_REVEAL -> Unit

                                OnboardingBeat.THE_DEPTH -> OnboardingBeatFour(
                                    moment = moment,
                                    onMoment = { moment = it },
                                    onFinish = onFinished,
                                )
                            }
                        }
                    }

                    // Guarded on the beat as well as on the opacity, because the snap
                    // above happens in an effect and effects run after the frame that
                    // caused them. Without this the stranded line would still be drawn
                    // once, on the first frame of beat 4.
                    if (state.beat == OnboardingBeat.THE_REVEAL && closing.value > 0f) {
                        ClosingLine(opacity = closing.value)
                    }

                    OnboardingNav(
                        beat = state.beat,
                        canGoBack = state.canGoBack,
                        onBack = viewModel::back,
                        onJumpIn = {
                            viewModel.leaveEarly(todayName)
                            onFinished()
                        },
                    )
                }
            }
        }
    }
}

/** design-v3.md 13.1's beat 3 line, on the black the iris is about to take away. */
@Composable
private fun BoxScope.ClosingLine(opacity: Float) {
    val contemplative = LocalContemplativeColors.current
    val type = LocalClarityTypography.current
    Text(
        text = stringResource(R.string.onboarding_beat_three_line),
        style = type.displayTitle,
        color = contemplative.textBright,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .align(Alignment.Center)
            .fillMaxWidth()
            .padding(horizontal = ClaritySpacing.screenPadding)
            // Named `opacity` rather than `alpha`, because inside a graphicsLayer block
            // the receiver's own `alpha` shadows an enclosing parameter of that name and
            // the assignment would silently be `alpha = alpha`.
            .graphicsLayer { alpha = opacity },
    )
}

/**
 * The persistent nav overlay. MASTER_BUILD_PROMPT 13.1.
 *
 * A back chevron at 35 percent white, hidden on beat 1; an 80dp progress line filling by
 * beat; and `Jump in` in `textDim`, always visible. The three sit in one row with
 * the line centered on the screen rather than between the two controls, so it does not
 * shift sideways when the chevron appears on beat 2.
 *
 * **`Jump in` was 30 percent white and MASTER_BUILD_PROMPT 13.1 still says so.** At that
 * opacity it measures 2.643 to one on `deepBlack`, against design-v3.md 13's floor of 4.5
 * for text, and it is the control that leaves onboarding: the one label on the surface a
 * person who does not want the tour is looking for. design-v3.md wins on anything visual,
 * 13 states one floor for text, and the value that meets it is the one 3.3 already names
 * for secondary Contemplative type, `textDim`, at 5.629. It is the same token the
 * progress line's fill takes, one element away, so the two things in this row that a
 * person is meant to read now carry the same value rather than two guesses at it.
 *
 * The chevron is left at 35 percent white, which measures 3.133 against the 3.0 a graphic
 * takes: it is a glyph rather than a word, its label is spoken, and it is the control a
 * person reaches for second.
 *
 * The progress line's track and fill are the two values 13.1 leaves open. The fill is
 * `textDim`, which is the value design-v3.md 3.3 already gives to secondary Contemplative
 * type, and the track is white at 12 percent, which is the faintest step that is visible
 * at 2dp against `deepBlack`. design-v3.md 15: the obvious answer is a filled bar in the
 * beat's own glow color, which would make the progress indicator the loudest element on
 * a surface whose content is one sentence, and would change color four times.
 */
@Composable
private fun BoxScope.OnboardingNav(
    beat: OnboardingBeat,
    canGoBack: Boolean,
    onBack: () -> Unit,
    onJumpIn: () -> Unit,
) {
    val contemplative = LocalContemplativeColors.current
    val type = LocalClarityTypography.current
    val shapes = LocalClarityShapes.current
    val motion = clarityMotion()
    // **The rule reaches full at the START of the last beat, and beat 4 is four screens
    // long**, so it read 100 percent with three screens still to come and the flow looked
    // like it had ended before it had. Counting the beat as begun rather than finished
    // leaves the last quarter for the beat that is actually running.
    val filled = beat.ordinal.toFloat() / (OnboardingBeat.entries.size - 1)
    val width = remember { Animatable(0f) }

    LaunchedEffect(filled) { width.animateTo(filled, motion.springGentle()) }

    Box(
        modifier = Modifier
            .align(Alignment.TopCenter)
            .fillMaxWidth()
            .statusBarsPadding()
            .height(NAV_HEIGHT),
    ) {
        if (canGoBack) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 6.dp)
                    .size(ClaritySpacing.minTouchTarget)
                    .clip(shapes.pill)
                    .clarityClickable(
                        haptic = ClarityHapticEvent.STEP,
                        role = Role.Button,
                        onClickLabel = stringResource(R.string.cd_onboarding_back),
                        onClick = onBack,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                ClarityIcon(
                    icon = ClarityIcons.chevron,
                    contentDescription = stringResource(R.string.cd_onboarding_back),
                    tint = Color.White.copy(alpha = CHEVRON_ALPHA),
                    modifier = Modifier
                        .size(22.dp)
                        .graphicsLayer { rotationZ = 180f },
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .width(PROGRESS_WIDTH)
                .height(PROGRESS_HEIGHT)
                .clip(shapes.pill)
                .background(Color.White.copy(alpha = PROGRESS_TRACK_ALPHA)),
        ) {
            Box(
                modifier = Modifier
                    // Coerced, because springGentle is underdamped and a fraction of
                    // 1.02 is not a fraction. design-v3.md 8.1.
                    .fillMaxWidth(width.value.coerceIn(0f, 1f))
                    .height(PROGRESS_HEIGHT)
                    .clip(shapes.pill)
                    .background(contemplative.textDim),
            )
        }

        Text(
            text = stringResource(R.string.onboarding_jump_in),
            style = type.label,
            color = contemplative.textDim,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 6.dp)
                .clip(shapes.pill)
                .clarityClickable(
                    haptic = ClarityHapticEvent.TAP,
                    role = Role.Button,
                    onClickLabel = stringResource(R.string.onboarding_jump_in),
                    onClick = onJumpIn,
                )
                .padding(horizontal = 14.dp, vertical = ClaritySpacing.scaled(15.dp)),
        )
    }
}

/**
 * Light status bar and navigation bar glyphs for as long as onboarding is showing.
 *
 * [key] re-asserts it on every beat, which matters exactly once: beat 3 composes the app
 * shell behind this surface, and the shell answers the same question for the Daylight
 * world it is in. Both are effects, effects run in composition order, and this surface is
 * composed after the shell, so re-keying here is what makes the front-most world the one
 * the bars agree with.
 */
@Composable
private fun LightSystemBars(key: Any) {
    val view = LocalView.current
    if (view.isInEditMode) return
    LaunchedEffect(view, key) {
        val window = view.context.findActivity()?.window ?: return@LaunchedEffect
        WindowCompat.getInsetsController(window, view).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }
    }
}

private fun Context.findActivity(): Activity? {
    var context: Context? = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}

/** See the shell's copy of this. Front-most sibling, drawn behind what it protects. */
private fun Modifier.swallowsPointerInput(): Modifier = pointerInput(Unit) {
    awaitPointerEventScope {
        while (true) {
            awaitPointerEvent(PointerEventPass.Initial).changes.forEach { it.consume() }
        }
    }
}

/**
 * Beat 3's timeline, in milliseconds from the moment the write finishes.
 *
 * The line arrives at 200, is fully there from 600 to 1,800, and fades out over the same
 * 400ms the iris takes to start moving. The iris settles at about 2,400 and the revealed
 * screen is held for a second before beat 4 draws the black back over it.
 */
private const val CLOSING_IN_AT = 200L
private const val CLOSING_HOLD = 3_000L
private const val IRIS_AT = 3_600L
private const val REVEAL_MILLIS = 5_200L

/** design-v3.md 13.1. An 80dp progress line, and the nav row it sits in. */
private val PROGRESS_WIDTH = 80.dp
private val PROGRESS_HEIGHT = 2.dp
private const val PROGRESS_TRACK_ALPHA = 0.12f
private val NAV_HEIGHT = 56.dp
/** 3.133 to one on `deepBlack`, against design-v3.md 13's 3.0 for a graphic. */
private const val CHEVRON_ALPHA = 0.35f

/**
 * How far a horizontal drag has to travel to count as a swipe.
 *
 * In pixels, because the gesture is measured in the pointer's own units. Sixty is a
 * deliberate distance rather than the platform's touch slop, which a scroll would clear
 * on its way past: a beat change is not something to do by accident.
 */
private const val SWIPE_THRESHOLD_PX = 60f
