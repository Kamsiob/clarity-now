package com.kamsiob.claritynow.ui.onboarding

import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * **The optical lift, and why every beat needs one.**
 *
 * Measured on a Pixel 8 at the shipping build: beat 1's four cards plus its sentence
 * occupy about 800px of a 2400px screen, and `Arrangement.Center` put roughly 800px of
 * nothing above them and 800px below. Beat 2's fork sat with its question 40 percent down
 * the page. Beat 4's Pulse card had 600px of empty room over it. On four consecutive
 * screens the composition read as bottom heavy and unresolved, which is the opposite of
 * what a first run is for.
 *
 * The geometric center of a screen is not its optical center, and here the gap is
 * unusually large because the top of every beat already carries chrome: a back chevron, a
 * progress rule and `Jump in`, about 80dp of it, with nothing at all at the foot. Content
 * centered in the whole height is therefore centered against a top that is already
 * occupied, so it reads low twice over.
 *
 * 64dp is 7 percent of the reference height and lands the block where the eye expects the
 * middle to be.
 *
 * **How it is applied took three attempts and the first two were both wrong.**
 *
 * Applied as bottom padding it lifts a centered box by HALF its value, because shortening
 * a box by 64 moves its midpoint up by 32, so a raw 64 on all three beats produced 32dp of
 * lift on the two that center inside a `fillMaxSize` box and 64dp on the one that
 * subtracts it from a viewport minimum: three beats, two heights. Doubling it made them
 * equal and cost 128dp of usable height on the two beats that have no scroll, which on
 * beat 1's four cards at 200 percent text is the difference between fitting and clipping.
 *
 * [Modifier.onboardingLift] translates instead. The box keeps its full height, the
 * arrangement still centers inside all of it, and the content is placed 64dp higher.
 * Nothing is given up to buy it. Beat 2 keeps the padding form because it scrolls, so
 * shortening its viewport minimum is free and is what makes a growing stage anchor.
 */
val OnboardingOpticalLift: Dp = 64.dp

/**
 * The lift for a beat that cannot scroll.
 *
 * A translation rather than a padding: the box keeps its full height, the arrangement
 * still centers inside all of it, and the result is moved up by exactly
 * [OnboardingOpticalLift]. Nothing is given up to buy it, which matters on the two beats
 * that have no scroll to fall back on.
 */
fun Modifier.onboardingLift(): Modifier = layout { measurable, constraints ->
    val placeable = measurable.measure(constraints)
    layout(placeable.width, placeable.height) {
        placeable.place(0, -OnboardingOpticalLift.roundToPx())
    }
}
