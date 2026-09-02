package com.kamsiob.claritynow.ui.tutorial

import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.kamsiob.claritynow.R
import com.kamsiob.claritynow.ui.components.clarityClickable
import com.kamsiob.claritynow.ui.theme.CALM_TUTORIAL_PULSE_OPACITY
import com.kamsiob.claritynow.ui.theme.ClarityHapticEvent
import com.kamsiob.claritynow.ui.theme.ClaritySpacing
import com.kamsiob.claritynow.ui.theme.LocalClarityShapes
import com.kamsiob.claritynow.ui.theme.LocalClarityTypography
import com.kamsiob.claritynow.ui.theme.LocalContemplativeColors
import com.kamsiob.claritynow.ui.theme.clarityMotion
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * The spotlight overlay. MASTER_BUILD_PROMPT 13.2, design-v3.md 8.2 item 19.
 *
 * A 56 percent black radial dim over the whole window, a feathered cutout on the step's
 * target, a slowly pulsing white ring around it, a floating tooltip in `surfaceRaised`
 * carrying a step indicator, and Skip at the top right. Tapping anywhere advances.
 *
 * ## One mechanism, and what it costs
 *
 * Everything drawn below is derived from [target], a rectangle the element itself
 * reported. **There is no branch on [step] anywhere in this file**: not for the shape of
 * the cutout, not for which side the tooltip sits on, not for a nudge on a target near an
 * edge. The FAB's cutout is a circle and the tab bar's is a wide pill because
 * `min(width, height) / 2` says so about each of their rectangles, and the tooltip goes
 * below a target near the top and above one near the bottom because there is room in one
 * direction and not the other. 13.2 calls per step special casing the failure mode of a
 * previous build, and a rule with no exceptions is the only form of that promise that
 * cannot rot.
 *
 * It also settles "spotlights align correctly on the smallest and largest screen sizes"
 * by construction, because nothing here holds a measurement it could be wrong about.
 *
 * ## The dim is radial, and 56 percent is its deepest value
 *
 * design-v3.md 13.2 says "56 percent black radial dim", and the obvious reading is a flat
 * 56 percent rectangle, which is not radial at all. This one runs from
 * [DIM_CENTER_ALPHA] at the cutout to [DIM_EDGE_ALPHA] at the farthest corner, so the
 * specified value is what the edges reach and the pool of light is where the person is
 * being asked to look. Its center travels with the cutout, so the light moves with the
 * attention instead of sitting under the middle of the screen. design-v3.md 15.
 *
 * ## The feather
 *
 * 13.2 asks for an 8dp feathered edge. The knockout uses `DstOut` rather than `Clear`
 * because `Clear` discards the source alpha and would give a hard edge with an
 * antialiased line on it. The band is [FEATHER_STEPS] concentric strokes of decreasing
 * alpha rather than a blurred mask: a mask filter is not dependably supported on a
 * hardware accelerated canvas, and a `RenderEffect` would want a layer of its own for one
 * edge. The strokes tile exactly, so the falloff is smooth, and it costs sixteen draw
 * operations on a surface that redraws only while the ring is pulsing.
 */
@Composable
internal fun TutorialOverlay(
    step: TutorialStep,
    stepNumber: Int,
    stepCount: Int,
    target: Rect,
    onAdvance: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val motion = clarityMotion()
    val contemplative = LocalContemplativeColors.current
    val type = LocalClarityTypography.current
    val shapes = LocalClarityShapes.current
    val density = LocalDensity.current

    // Where this overlay sits in the composition root, so a target's root coordinates can
    // be expressed in this canvas's own. It is zero in the shell as it stands, and it is
    // read rather than assumed because the shell takes a modifier.
    var origin by remember { mutableStateOf(Offset.Zero) }
    var overlaySize by remember { mutableStateOf(IntSize.Zero) }
    var tooltipHeight by remember { mutableIntStateOf(0) }

    val padPx = with(density) { SPOTLIGHT_PAD.toPx() }
    val gapPx = with(density) { TOOLTIP_GAP.toPx() }
    val edgePx = with(density) { SCREEN_EDGE.toPx() }
    val featherPx = with(density) { FEATHER.toPx() }
    val ringPx = with(density) { RING_WIDTH.toPx() }
    val topInset = WindowInsets.statusBars.getTop(density).toFloat()
    val bottomInset = WindowInsets.navigationBars.getBottom(density).toFloat()

    // design-v3.md 8.2 item 19: the cutout animates between targets with springGentle.
    // Four springs rather than one, given one spec and started by one change of target,
    // so they travel as a single rectangle.
    val left by animateFloatAsState(
        targetValue = target.left - origin.x - padPx,
        animationSpec = motion.springGentle(),
        label = "spotlightLeft",
    )
    val top by animateFloatAsState(
        targetValue = target.top - origin.y - padPx,
        animationSpec = motion.springGentle(),
        label = "spotlightTop",
    )
    val right by animateFloatAsState(
        targetValue = target.right - origin.x + padPx,
        animationSpec = motion.springGentle(),
        label = "spotlightRight",
    )
    val bottom by animateFloatAsState(
        targetValue = target.bottom - origin.y + padPx,
        animationSpec = motion.springGentle(),
        label = "spotlightBottom",
    )

    // design-v3.md 16.6 item 19. Held rather than slowed, and the infinite animation is
    // never started at all rather than started and ignored.
    val ringAlpha = if (motion.reduced) CALM_TUTORIAL_PULSE_OPACITY else pulsingRingAlpha()

    val tooltipY = tooltipTop(
        spotlightTop = top,
        spotlightBottom = bottom,
        tooltipHeight = tooltipHeight,
        overlayHeight = overlaySize.height,
        gap = gapPx,
        edge = edgePx,
        topInset = topInset,
        bottomInset = bottomInset,
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned {
                origin = it.positionInRoot()
                overlaySize = it.size
            },
    ) {
        // Nothing beneath this overlay may be touched while it is showing. The same
        // arrangement the shell uses under the Focus surface: a full size sibling drawn
        // first, so it swallows a scroll or a swipe that the tap layer above would not
        // have claimed, and so it can never starve this overlay's own controls.
        Spacer(Modifier.fillMaxSize().swallowsPointerInput())

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                // The knockout has to erase the dim rather than the screen, which means
                // the dim and the erasing share one layer.
                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen },
        ) {
            val spotlight = Rect(left, top, right, bottom)
            val radius = min(spotlight.width, spotlight.height) / 2f

            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.Black.copy(alpha = DIM_CENTER_ALPHA),
                        Color.Black.copy(alpha = DIM_EDGE_ALPHA),
                    ),
                    center = spotlight.center,
                    radius = max(size.width, size.height),
                ),
            )

            drawRoundRect(
                color = Color.Black,
                topLeft = spotlight.topLeft,
                size = spotlight.size,
                cornerRadius = CornerRadius(radius, radius),
                blendMode = BlendMode.DstOut,
            )

            val bandWidth = featherPx / FEATHER_STEPS
            repeat(FEATHER_STEPS) { index ->
                val distance = (index + 0.5f) * bandWidth
                val band = spotlight.inflate(distance)
                drawRoundRect(
                    color = Color.Black,
                    topLeft = band.topLeft,
                    size = band.size,
                    cornerRadius = CornerRadius(radius + distance, radius + distance),
                    alpha = 1f - (index + 0.5f) / FEATHER_STEPS,
                    style = Stroke(width = bandWidth),
                    blendMode = BlendMode.DstOut,
                )
            }

            // Drawn after the knockout and in the ordinary blend mode, so it sits on the
            // feather rather than being erased with it.
            drawRoundRect(
                color = Color.White.copy(alpha = ringAlpha),
                topLeft = spotlight.topLeft,
                size = spotlight.size,
                cornerRadius = CornerRadius(radius, radius),
                style = Stroke(width = ringPx),
            )
        }

        // 13.2: tap anywhere advances. Above the drawing and below Skip, so the finger
        // that is over Skip reaches Skip and every other finger advances.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clarityClickable(
                    haptic = ClarityHapticEvent.STEP,
                    role = Role.Button,
                    onClickLabel = stringResource(R.string.tutorial_advance),
                    onClick = onAdvance,
                ),
        )

        TutorialTooltip(
            step = step,
            stepNumber = stepNumber,
            stepCount = stepCount,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(horizontal = SCREEN_EDGE)
                .offset { IntOffset(0, tooltipY.roundToInt()) }
                .onSizeChanged { tooltipHeight = it.height },
        )

        // **Skip sits on its own ground.** It is drawn over whatever the app happens to
        // have in its top right, and on the Areas screen that is the settings gear: the
        // word and the glyph landed on each other and neither could be read. A scrim at
        // the world's own raised surface gives the word a surface to stand on, and it is
        // one device rather than a second edge or a shadow.
        Text(
            text = stringResource(R.string.tutorial_skip),
            style = type.bodyStrong,
            color = contemplative.textBright,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(end = SCREEN_EDGE - SKIP_TOUCH_INSET, top = SKIP_TOP)
                .clip(shapes.pill)
                .background(contemplative.surfaceRaised.copy(alpha = 0.92f))
                .clarityClickable(
                    haptic = ClarityHapticEvent.TAP,
                    role = Role.Button,
                    onClickLabel = stringResource(R.string.tutorial_skip),
                    onClick = onSkip,
                )
                .padding(
                    horizontal = SKIP_TOUCH_INSET,
                    vertical = ClaritySpacing.scaled(SKIP_TOUCH_VERTICAL,
                )),
        )
    }
}

/**
 * The card. design-v3.md 13.2: `surfaceRaised`, floating, with a step indicator.
 *
 * **One separation device**, per design-v3.md 6.1: the lightness step from
 * `surfaceRaised` against a dim that reaches 56 percent black. No hairline and no shadow,
 * and a Contemplative surface carries no shadow in any case.
 *
 * **The step indicator is a caption line reading `1 of 5`.** A row of five dots is the
 * obvious answer and design-v3.md 15 asks for the other one wherever the document leaves
 * the choice open. Two things settle it past the rule. This app has already answered "how
 * do you show a count" in 10.16, where the inbox chip's count is its label rather than a
 * badge. And a screen reader gets the position from the text for free, where a row of
 * circles would need a content description invented to say the same thing.
 */
@Composable
private fun TutorialTooltip(
    step: TutorialStep,
    stepNumber: Int,
    stepCount: Int,
    modifier: Modifier = Modifier,
) {
    val contemplative = LocalContemplativeColors.current
    val type = LocalClarityTypography.current
    val shapes = LocalClarityShapes.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shapes.card)
            .background(contemplative.surfaceRaised)
            .padding(horizontal = 20.dp, vertical = ClaritySpacing.scaled(18.dp)),
    ) {
        Text(
            text = stringResource(step.titleRes),
            style = type.bodyStrong,
            color = contemplative.textBright,
        )
        Spacer(Modifier.height(ClaritySpacing.scaled(6.dp)))
        Text(
            text = stringResource(step.bodyRes),
            style = type.body,
            color = contemplative.textDim,
        )
        Spacer(Modifier.height(ClaritySpacing.scaled(14.dp)))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            // Both take `textDim`, design-v3.md 13's 55 percent floor. `Step 2 of 5`
            // is how far in somebody is and the other line is how to go on, so this
            // row is the two things the card is for after its own sentence. At 32
            // percent they measured 2.709 to one on `surfaceRaised`. The rank under
            // the body above is the `caption` role, 5.3.
            Text(
                text = stringResource(R.string.tutorial_step_indicator, stepNumber, stepCount),
                style = type.caption,
                color = contemplative.textDim,
            )
            Text(
                text = stringResource(R.string.tutorial_advance),
                style = type.caption,
                color = contemplative.textDim,
            )
        }
    }
}

/**
 * design-v3.md 8.2 item 19. Opacity 0.25 to 0.45 over 2 seconds, infinite.
 *
 * Two seconds is read as the round trip, so each direction takes half of it and the ring
 * is back where it started every two seconds. The other reading, two seconds up and two
 * seconds down, is a four second cycle and is slower than "slowly pulsing" needs to be.
 *
 * Never called when motion is reduced or calm mode is on. The caller substitutes the held
 * value, so no infinite animation is started at all. design-v3.md 16.6 item 19.
 */
@Composable
private fun pulsingRingAlpha(): Float {
    val transition = rememberInfiniteTransition(label = "tutorialRing")
    val alpha by transition.animateFloat(
        initialValue = RING_MIN_ALPHA,
        targetValue = RING_MAX_ALPHA,
        animationSpec = infiniteRepeatable(
            animation = tween(RING_HALF_CYCLE_MILLIS, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "tutorialRingPulse",
    )
    return alpha
}

/**
 * Where the tooltip's top edge goes, in overlay coordinates.
 *
 * Below the spotlight when it fits under it, above it when it does not, clamped into the
 * safe area either way. It reads the spotlight's already animated edges rather than
 * carrying an animation of its own, so the card and the cutout arrive together.
 *
 * A plain function of eight numbers, so the rule is one expression rather than a chain of
 * remembered state, and so it is the kind of thing a test can call.
 */
private fun tooltipTop(
    spotlightTop: Float,
    spotlightBottom: Float,
    tooltipHeight: Int,
    overlayHeight: Int,
    gap: Float,
    edge: Float,
    topInset: Float,
    bottomInset: Float,
): Float {
    if (overlayHeight == 0) return spotlightBottom + gap
    val floor = topInset + edge
    val ceiling = overlayHeight - bottomInset - edge - tooltipHeight
    val below = spotlightBottom + gap
    val wanted = if (below <= ceiling) below else spotlightTop - gap - tooltipHeight
    return wanted.coerceIn(floor, max(floor, ceiling))
}

/**
 * Consumes every pointer event that reaches this element, in the initial pass, so nothing
 * beneath it can be touched. The same modifier the shell uses under the Focus surface,
 * and under the same condition: an element wearing it must be a sibling drawn behind what
 * it protects, never an ancestor of anything that needs the pointer.
 */
private fun Modifier.swallowsPointerInput(): Modifier = pointerInput(Unit) {
    awaitPointerEventScope {
        while (true) {
            awaitPointerEvent(PointerEventPass.Initial).changes.forEach { it.consume() }
        }
    }
}

/** design-v3.md 13.2. What the cutout clears, and how far it fades out over. */
private val SPOTLIGHT_PAD = 8.dp
private val FEATHER = 8.dp
private const val FEATHER_STEPS = 16

/** design-v3.md 13.2 and 8.2 item 19. A 2dp white ring, pulsing 0.25 to 0.45. */
private val RING_WIDTH = 2.dp
private const val RING_MIN_ALPHA = 0.25f
private const val RING_MAX_ALPHA = 0.45f
private const val RING_HALF_CYCLE_MILLIS = 1_000

/**
 * The dim, radial. 56 percent is what the far corners reach, per design-v3.md 13.2, and
 * the pool of light around the cutout is twelve points lighter: enough to read as a pool,
 * not so much that the scrim reads as a vignette drawn on the screen.
 */
private const val DIM_CENTER_ALPHA = 0.44f
private const val DIM_EDGE_ALPHA = 0.56f

private val TOOLTIP_GAP = 18.dp
private val SCREEN_EDGE = 20.dp
private val SKIP_TOP = 8.dp
private val SKIP_TOUCH_INSET = 16.dp
private val SKIP_TOUCH_VERTICAL = 13.dp
