package com.kamsiob.claritynow.ui.nav

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import com.kamsiob.claritynow.ui.theme.ClarityMotion

/**
 * The scale half of a pushed screen's arrival and departure, in one place.
 *
 * There are three sites, `AreasRoute` twice and `SettingsSurface` once, and they have to
 * agree: the archive, manage areas and About are the same kind of thing arriving over the
 * same kind of ground, and a person who learns one has learned all three.
 *
 * **0.97 and not zero.** Below about 0.9 an element reads as arriving from nowhere. 0.97
 * is the app's own press value, so a screen coming forward and a control being pressed
 * speak the same language, which is `design-v3.md` 8.2 item 6's whole point.
 *
 * **Nothing at all under reduced motion**, and the fade at the call site carries the
 * transition on its own. Scale is the vestibular trigger; opacity is the substitute
 * WebKit and MDN both name. This is the one place in the file where reduced motion
 * removes something rather than shortening it.
 */
fun pushedScreenScaleIn(motion: ClarityMotion): EnterTransition =
    if (motion.reduced) {
        EnterTransition.None
    } else {
        scaleIn(motion.springStandard(), initialScale = PUSHED_SCREEN_SCALE)
    }

fun pushedScreenScaleOut(motion: ClarityMotion): ExitTransition =
    if (motion.reduced) {
        ExitTransition.None
    } else {
        scaleOut(motion.effectsFast(), targetScale = PUSHED_SCREEN_SCALE)
    }

private const val PUSHED_SCREEN_SCALE = 0.97f
