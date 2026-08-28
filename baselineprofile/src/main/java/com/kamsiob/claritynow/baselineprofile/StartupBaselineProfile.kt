package com.kamsiob.claritynow.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test

/**
 * MASTER_BUILD_PROMPT 19, phase 13. Generates the baseline profile for the release
 * build by cold starting the real app and using it.
 *
 * **The journey is deliberately short: cold start, wait for the first frame, scroll.**
 * The obvious answer is a long scripted tour through all four tabs, the Pulse, a focus
 * session and the Report, on the theory that more coverage is a better profile. Two
 * things argue against it here.
 *
 * A baseline profile is compiled ahead of time and costs install time and disk for
 * every class it names, so its value is concentrated almost entirely in the path from
 * tapping the icon to the first usable frame. Everything after that is already warm.
 *
 * And a longer journey is a more fragile one. Every step that waits on a specific
 * string or a specific control is a step that silently stops contributing the day that
 * control moves, and a generator that quietly covers less than it claims is worse than
 * one that covers less and says so. `MASTER_BUILD_PROMPT.md` 3.4 applies: a script that
 * looks thorough and is not is the failure mode to design against.
 *
 * So this covers the launch path, the projection replaying the event log into
 * `ClarityState`, the theme, the shell and the first list, and stops. If the startup
 * path later gains a phase that is genuinely expensive and genuinely on the critical
 * path, add it here and say in the commit which frame it was bought for.
 *
 * Run with a phone connected:
 * ```
 * ./gradlew :app:generateBaselineProfile
 * ```
 */
class StartupBaselineProfile {

    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun startup() = rule.collect(
        packageName = PACKAGE_NAME,
        // The default is three, which trades collection time for stability. Startup is
        // the one journey where a class that loads on some runs and not others is worth
        // catching, because a miss costs a frame on every launch afterwards.
        maxIterations = 5,
    ) {
        pressHome()
        startActivityAndWait()

        // Wait for the shell rather than for a fixed delay: the projection rebuilds
        // from the event log before anything renders, and on a large log that is the
        // slowest part of a cold start and the part most worth compiling.
        device.wait(Until.hasObject(By.pkg(PACKAGE_NAME).depth(0)), FIRST_FRAME_TIMEOUT_MS)

        // One scroll, so the row composables and the paging query are on the profile.
        // `scrollable(true)` rather than a resource id, because the app does not assign
        // ids to Compose nodes and a testTag added only for this would be production
        // code that exists for a benchmark.
        device.findObject(By.scrollable(true))?.let { list ->
            list.setGestureMargin(device.displayWidth / GESTURE_MARGIN_FRACTION)
            list.fling(Direction.DOWN)
            device.waitForIdle()
        }
    }

    private companion object {
        /**
         * The release application id. The generator installs and starts the variant a
         * person would install, which is not the `.debug` build every device check in
         * this project otherwise runs against.
         */
        const val PACKAGE_NAME = "com.kamsiob.claritynow"

        const val FIRST_FRAME_TIMEOUT_MS = 10_000L

        /** A fifth of the screen, so the fling starts inside the list and not on an edge gesture. */
        const val GESTURE_MARGIN_FRACTION = 5
    }
}
