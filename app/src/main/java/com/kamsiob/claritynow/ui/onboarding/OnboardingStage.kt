package com.kamsiob.claritynow.ui.onboarding

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
 * **It is applied as bottom padding, and bottom padding lifts a centered box by HALF its
 * value**, because shortening a box by 64 moves its midpoint up by 32. The first version
 * of this put a raw 64 on all three beats and produced 32dp of lift on the two that center
 * inside a `fillMaxSize` box and 64dp on the one that subtracts it from a viewport
 * minimum, so the three beats sat at two different heights. [OnboardingOpticalLiftPadding]
 * is what a caller applies; the constant above is what a reader should be able to measure
 * on the screen.
 */
val OnboardingOpticalLift: Dp = 64.dp

/** Twice the lift, because bottom padding on a centered box buys half of what it spends. */
val OnboardingOpticalLiftPadding: Dp = OnboardingOpticalLift * 2
