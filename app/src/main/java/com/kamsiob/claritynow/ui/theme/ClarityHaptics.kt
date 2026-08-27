package com.kamsiob.claritynow.ui.theme

import android.content.Context
import android.media.AudioAttributes
import android.os.Build
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * design-v3.md section 9. The sixteen haptic events, and the only place in the app
 * that is allowed to touch the vibrator.
 *
 * Never fires on scroll, on screen entry, on notification arrival, or more than
 * once per user action. A focus session fires nothing between start and end.
 */
enum class ClarityHapticEvent {
    /** Card press, chip, tab, swatch, mood pill. */
    TAP,

    /** Pulse answer, segmented choice, theme tile. */
    SELECT,
    TOGGLE_ON,
    TOGGLE_OFF,

    /** An item was completed. */
    COMPLETE,

    /** Fired as the newly promoted title lands, not when the completion commits. */
    PROMOTE,

    /** Long press to drag. */
    PICK_UP,
    PUT_DOWN,

    /** Crossing a swipe commit point. Once per gesture, never on the way back. */
    SWIPE_THRESHOLD,
    FOCUS_START,

    /** Natural completion of a focus session only. Ending early is silent. */
    FOCUS_END,

    /**
     * Five minutes left, and only when the person turned the warning on.
     *
     * design-v3.md 9 otherwise says a focus session fires nothing between its start
     * and its end, and this is the single authorized exception, added by Addendum 01
     * 4g and recorded in the v3.1 history entry. It is the lightest primitive in the
     * catalogue on purpose: a transition warning that startles is worse than none,
     * because the whole point is to let someone finish a thought rather than be
     * pulled out of one.
     */
    TRANSITION_WARN,
    REPORT_READY,

    /** Deliberately the weight of an ordinary tap. Accepting a plan is not an achievement. */
    PLAN_ACCEPTED,

    /** A destructive confirmation arming. */
    WARN,

    /** An action that could not be performed. Quieter than a tap, so it feels like
     *  nothing happening rather than like being told off. */
    REJECT,

    /** The undo action in a snackbar was tapped. */
    UNDO,

    /** A tutorial step or onboarding beat advanced. The lightest event in the system. */
    STEP,
}

interface ClarityHaptics {
    fun perform(event: ClarityHapticEvent)
}

object NoHaptics : ClarityHaptics {
    override fun perform(event: ClarityHapticEvent) = Unit
}

/**
 * Checks primitive support once at construction and degrades in steps rather than
 * falling silent. A device without composition primitives still gets a short
 * one shot vibration, and a device with haptics switched off in system settings
 * gets nothing at all.
 */
class AndroidClarityHaptics(context: Context) : ClarityHaptics {

    private val appContext = context.applicationContext

    private val vibrator: Vibrator? = runCatching {
        val manager = appContext.getSystemService(VibratorManager::class.java)
        manager?.defaultVibrator
    }.getOrNull()?.takeIf { it.hasVibrator() }

    private val supported: Set<Int> = if (vibrator == null) emptySet() else {
        ALL_PRIMITIVES.filter { vibrator.areAllPrimitivesSupported(it) }.toSet()
    }

    /**
     * Tagging every effect as touch feedback is how the user's haptics setting gets
     * respected. The platform suppresses a vibration with this usage when touch
     * feedback is switched off, which is better than reading the setting directly:
     * that setting field is deprecated, and a suppression the system performs
     * cannot drift out of step with the system.
     *
     * Android 13 introduced VibrationAttributes for this. On 12 and 12L the audio
     * attributes form is the only one available, and the platform maps sonification
     * usage onto touch usage internally, so the behavior is the same.
     */
    private val touchFeedbackAudio: AudioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .build()

    /**
     * Haptics are feedback, never function. Anything that goes wrong here is
     * swallowed rather than propagated: a missing permission or an unsupported
     * effect must degrade to silence, not take a screen down with it.
     */
    @Suppress("DEPRECATION")
    private fun Vibrator.play(effect: VibrationEffect) {
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                vibrate(effect, VibrationAttributes.createForUsage(VibrationAttributes.USAGE_TOUCH))
            } else {
                vibrate(effect, touchFeedbackAudio)
            }
        }
    }

    override fun perform(event: ClarityHapticEvent) {
        val device = vibrator ?: return
        val steps = stepsFor(event)
        if (steps.isEmpty()) return

        val usable = steps.filter { it.primitive in supported }
        if (usable.isEmpty()) {
            // No composition primitive on this device. One short pulse is honest
            // feedback; a long buzz would be worse than nothing.
            device.play(VibrationEffect.createOneShot(12L, VibrationEffect.DEFAULT_AMPLITUDE))
            return
        }

        val composition = VibrationEffect.startComposition()
        usable.forEach { composition.addPrimitive(it.primitive, it.scale, it.delayMillis) }
        device.play(composition.compose())
    }

    private fun stepsFor(event: ClarityHapticEvent): List<Step> = when (event) {
        ClarityHapticEvent.TAP -> listOf(Step(TICK, 0.4f))
        ClarityHapticEvent.TRANSITION_WARN -> listOf(Step(LOW_TICK, 0.35f))
        ClarityHapticEvent.SELECT -> listOf(Step(CLICK, 0.6f))
        ClarityHapticEvent.TOGGLE_ON -> listOf(Step(CLICK, 0.5f))
        ClarityHapticEvent.TOGGLE_OFF -> listOf(Step(TICK, 0.5f))
        ClarityHapticEvent.COMPLETE -> listOf(Step(TICK, 0.5f), Step(CLICK, 0.8f, 60))
        ClarityHapticEvent.PROMOTE -> listOf(Step(QUICK_RISE, 0.5f))
        ClarityHapticEvent.PICK_UP -> listOf(Step(THUD, 0.5f))
        ClarityHapticEvent.PUT_DOWN -> listOf(Step(TICK, 0.4f))
        ClarityHapticEvent.SWIPE_THRESHOLD -> listOf(Step(TICK, 0.3f))
        ClarityHapticEvent.FOCUS_START -> listOf(Step(LOW_TICK, 0.5f), Step(LOW_TICK, 0.5f, 90))
        ClarityHapticEvent.FOCUS_END -> listOf(Step(QUICK_RISE, 0.6f), Step(THUD, 0.7f, 120))
        ClarityHapticEvent.REPORT_READY ->
            if (SPIN in supported) listOf(Step(SPIN, 0.4f)) else listOf(Step(QUICK_RISE, 0.4f))
        ClarityHapticEvent.PLAN_ACCEPTED -> listOf(Step(TICK, 0.5f))
        ClarityHapticEvent.WARN -> listOf(Step(THUD, 0.7f))
        ClarityHapticEvent.REJECT -> listOf(Step(LOW_TICK, 0.3f))
        ClarityHapticEvent.UNDO -> listOf(Step(TICK, 0.4f))
        ClarityHapticEvent.STEP -> listOf(Step(TICK, 0.25f))
    }

    private data class Step(val primitive: Int, val scale: Float, val delayMillis: Int = 0)

    private companion object {
        const val TICK = VibrationEffect.Composition.PRIMITIVE_TICK
        const val CLICK = VibrationEffect.Composition.PRIMITIVE_CLICK
        const val LOW_TICK = VibrationEffect.Composition.PRIMITIVE_LOW_TICK
        const val QUICK_RISE = VibrationEffect.Composition.PRIMITIVE_QUICK_RISE
        const val THUD = VibrationEffect.Composition.PRIMITIVE_THUD
        const val SPIN = VibrationEffect.Composition.PRIMITIVE_SPIN

        val ALL_PRIMITIVES = listOf(TICK, CLICK, LOW_TICK, QUICK_RISE, THUD, SPIN)
    }
}

val LocalClarityHaptics = staticCompositionLocalOf<ClarityHaptics> { NoHaptics }
