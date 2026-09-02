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
 * middle to be. It is applied as bottom padding on the box the arrangement centers inside,
 * so it shortens the box rather than moving the content, and a beat that grows past the
 * viewport still scrolls normally.
 */
val OnboardingOpticalLift: Dp = 64.dp
