package com.kamsiob.claritynow.ui.pulse

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kamsiob.claritynow.domain.engine.StableHash
import com.kamsiob.claritynow.ui.theme.LocalCalmMode
import com.kamsiob.claritynow.ui.theme.LocalContemplativeColors
import com.kamsiob.claritynow.ui.theme.PulsePalette

/**
 * Which of the three background states the amber night is in. design-v3.md 3.3.
 *
 * Dawn from 05 to 11, midday from 11 to 17, evening from 17 to 05. The boundaries are
 * inclusive at the lower end, matching the way `PulseSchedule` reads "at or after 17:00",
 * so the evening tint and the reflection period switch happen at the same instant rather
 * than an hour apart for no reason a person could see.
 *
 * **Resolved once, from the injected clock, in the ViewModel.** Not read here from a
 * system call: `domain` is not the only place a wall clock reached for by hand causes
 * trouble, and the surface has no business knowing what time it is except through the one
 * clock the rest of the app is tested against.
 */
enum class PulseTimeOfDay {
    DAWN,
    MIDDAY,
    EVENING,

    ;

    companion object {

        /** The state a local hour of day, 0 to 23, puts the background in. */
        fun atHour(hour: Int): PulseTimeOfDay = when (hour) {
            in DAWN_FROM until MIDDAY_FROM -> DAWN
            in MIDDAY_FROM until EVENING_FROM -> MIDDAY
            else -> EVENING
        }

        private const val DAWN_FROM = 5
        private const val MIDDAY_FROM = 11
        private const val EVENING_FROM = 17
    }
}

/**
 * The amber night. design-v3.md 3.3 and section 11.
 *
 * `deepBlack`, a blend of one tint into one edge, and eight to fourteen specks of light.
 * It is the room the observation is read in and the room the rhythm row settles into, so
 * it is drawn once behind every phase of the surface and nothing in front of it changes
 * the light.
 *
 * **The shift "must be felt rather than noticed"**, which is the whole specification of
 * its strength and is why the two numbers below are small and are written down with
 * their reasoning. design-v3.md 15 asks for the unobvious answer where the document
 * leaves a choice open: the obvious reading of "blends a whisper into the top" is a
 * gradient across the whole surface, which is what a hero background looks like in 2026
 * and which would make the time of day the loudest thing on a screen whose content is
 * one sentence. This one reaches under half the height and stops.
 *
 * **In calm mode the tint is not transformed, it is not applied at all**, per
 * design-v3.md 16.7: the surface is held at the midday neutral ground all day. The specks
 * drop to the low end of both their ranges, which is the same treatment the Focus
 * backdrop takes.
 *
 * **It is one Canvas and there is no ticker behind it.** The tint and the specks are
 * remembered against the two things that decide them, and the time of day arrives as a
 * value the ViewModel read off the clock. Nothing here counts down to the next boundary,
 * because a background that redrew itself while somebody was reading would be the
 * opposite of felt rather than noticed.
 */
@Composable
internal fun PulseBackdrop(timeOfDay: PulseTimeOfDay, modifier: Modifier = Modifier) {
    val contemplative = LocalContemplativeColors.current
    val calm = LocalCalmMode.current

    // design-v3.md 16.7: dawn and evening blends at 0 percent in calm mode.
    val tint = remember(timeOfDay, calm) {
        if (calm) null else tintFor(timeOfDay)
    }
    val specks = remember(calm) { specksFor(calm) }

    Canvas(modifier = modifier.fillMaxSize()) {
        drawRect(color = contemplative.deepBlack)

        if (tint != null) {
            val color = tint.color.copy(alpha = EDGE_ALPHA)
            val reach = size.height * TINT_REACH
            drawRect(
                brush = if (tint.fromTop) {
                    Brush.verticalGradient(
                        colors = listOf(color, Color.Transparent),
                        startY = 0f,
                        endY = reach,
                    )
                } else {
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, color),
                        startY = size.height - reach,
                        endY = size.height,
                    )
                },
            )
        }

        specks.forEach { speck ->
            drawCircle(
                color = Color.White.copy(alpha = speck.alpha),
                radius = speck.radius.toPx(),
                center = Offset(size.width * speck.x, size.height * speck.y),
            )
        }
    }
}

/** One tint and which edge it blends from. Null at midday, which blends nothing. */
private data class Tint(val color: Color, val fromTop: Boolean)

private fun tintFor(timeOfDay: PulseTimeOfDay): Tint? = when (timeOfDay) {
    PulseTimeOfDay.DAWN -> Tint(PulsePalette.dawnTint, fromTop = true)
    PulseTimeOfDay.MIDDAY -> null
    PulseTimeOfDay.EVENING -> Tint(PulsePalette.eveningTint, fromTop = false)
}

/**
 * One speck of light. design-v3.md 3.3, the same treatment the Focus backdrop takes.
 *
 * [radius] is half the specified dot, since 3.3 gives the dots as 1 to 2dp across and a
 * canvas draws circles by radius.
 */
@Immutable
private data class Speck(val x: Float, val y: Float, val radius: Dp, val alpha: Float)

/**
 * The specks, from a fixed seed so they never re-randomize. design-v3.md 3.3.
 *
 * `StableHash` rather than `Random`, for the reason the Focus backdrop gives at length: a
 * seeded random still has to be re-seeded identically on every recomposition, rotation
 * and process death, and hashing a per speck key gives one fixed arrangement with nothing
 * to store.
 *
 * **A seed of its own, because 3.3 says one fixed seed per surface.** The Pulse and the
 * Focus surface are two rooms and sharing a seed would put the same stars in both.
 */
private fun specksFor(calm: Boolean): List<Speck> {
    val count = if (calm) SPECKS_MIN else SPECKS_MIN + StableHash.bucket("$SEED.count", SPECK_SPREAD)
    return (0 until count).map { index ->
        Speck(
            x = StableHash.bucket("$SEED.$index.x", POSITION_STEPS) / POSITION_STEPS.toFloat(),
            y = StableHash.bucket("$SEED.$index.y", POSITION_STEPS) / POSITION_STEPS.toFloat(),
            radius = if (calm || StableHash.bucket("$SEED.$index.r", 2) == 0) 0.5.dp else 1.dp,
            alpha = if (calm) {
                SPECK_ALPHA_MIN
            } else {
                SPECK_ALPHA_MIN + StableHash.bucket("$SEED.$index.a", ALPHA_STEPS) * SPECK_ALPHA_STEP
            },
        )
    }
}

/**
 * How strongly the tint arrives at its edge.
 *
 * Both tints are themselves dark, `#2B2340` and `#2E1F14`, so at full strength the edge
 * would land four to five points of lightness above `deepBlack` and read as a band rather
 * than as a time of day. At this alpha the evening edge composites to roughly `#1D1610`,
 * which is warmth a person notices only when they have seen the morning version.
 */
private const val EDGE_ALPHA = 0.55f

/**
 * How far the tint reaches, as a fraction of the height.
 *
 * Under half, so the two ends of the surface are never both tinted and the middle, where
 * the observation sits, is always the neutral ground. That is what keeps the serif line
 * reading against one color rather than against a ramp.
 */
private const val TINT_REACH = 0.45f

private const val SEED = "clarity.pulse.specks"
private const val SPECKS_MIN = 8
private const val SPECK_SPREAD = 7
private const val POSITION_STEPS = 1_000
private const val SPECK_ALPHA_MIN = 0.03f
private const val SPECK_ALPHA_STEP = 0.01f
private const val ALPHA_STEPS = 4
