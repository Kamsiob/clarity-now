package com.kamsiob.claritynow.ui.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * How many screens are currently pushed over a tab. design-v3.md 10.15, issue #58.
 *
 * **The floating tab bar belongs to the four views and is not drawn over anything
 * else.** Settings, About and the Report's history page are pushed screens: they cover
 * the tab they were entered from and are left by back. Through 0.9.0 the bar kept
 * floating over them, showing the tab underneath as selected, and tapping that selected
 * tab did nothing at all. Back worked, so section 17's requirement that every screen can
 * be left was met, and what was left was a control that looks live and is inert, which
 * is the thing 10.16 objects to elsewhere in this app.
 *
 * **The statistically common answer is that tapping the selected tab pops to that tab's
 * root**, which is what every Android and iOS app with a bottom bar does, and design-v3
 * 15 makes that a reason to look at it twice rather than a reason to take it. It is
 * refused here. It would give one control two meanings that differ by state a person
 * cannot see: `Areas` means "go to Areas" from three tabs and "leave Settings" from
 * Settings. It is also undiscoverable, so the bar goes on looking live to everybody who
 * does not already know the convention. This audience pays for both of those.
 *
 * Not drawing the bar is the answer, and it serves the brief better on three counts.
 * The inert control is removed rather than given a second job. The app already has this
 * rule and applies it to the Focus surface, which covers the bar because it is not one
 * of the four views, so a pushed screen doing the same is the app being consistent with
 * itself rather than with Android. And it deletes a state instead of adding one: with
 * the bar gone there is no way to leave Settings sideways, so there is no coming back to
 * the Areas tab and finding Settings still open where you left it.
 *
 * What it costs is the sideways move, one tap from any pushed screen to any other tab.
 * Back is the way out of a pushed screen in 10.15's own table, it is one press, and it
 * lands on the tab the bar would have.
 *
 * ## How it works
 *
 * A pushed screen calls [CoversTheTabBar] once, and the bar is not composed while any
 * screen has. A count rather than a flag, because About is a pushed screen over Settings
 * and both are composed at once; the bar returns when the last of them leaves.
 *
 * **Registration rather than the shell knowing.** `ClarityShell` draws the bar as a
 * sibling above the tab content and the pushed screens are hosted inside the tabs, so
 * the shell cannot see them. Hoisting all of them into the shell is the other way, and
 * it is three navigation rewrites for a rule that is one line at each site.
 */
@Stable
class PushedScreens {

    var depth by mutableIntStateOf(0)
        private set

    /** True while anything is pushed over a tab. */
    val any: Boolean get() = depth > 0

    fun entered() {
        depth += 1
    }

    fun left() {
        depth = (depth - 1).coerceAtLeast(0)
    }
}

/**
 * Provided by `ClarityShell`. The default instance exists so that a screen composed on
 * its own, in a preview or a test, still runs.
 */
val LocalPushedScreens = staticCompositionLocalOf { PushedScreens() }

/**
 * Declares that the caller covers the tab bar for as long as it is composed.
 *
 * One line at the top of a pushed screen. It takes no parameters and returns nothing on
 * purpose: what a pushed screen has to say is that it is one, and everything else about
 * what that means belongs to [PushedScreens] and to the shell.
 */
@Composable
fun CoversTheTabBar() {
    val screens = LocalPushedScreens.current
    DisposableEffect(screens) {
        screens.entered()
        onDispose { screens.left() }
    }
}
