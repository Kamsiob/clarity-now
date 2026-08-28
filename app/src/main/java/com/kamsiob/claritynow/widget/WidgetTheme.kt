package com.kamsiob.claritynow.widget

import android.content.ContentResolver
import android.content.Context
import android.provider.Settings
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.FontFamily
import androidx.glance.text.FontWeight
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.kamsiob.claritynow.di.ClarityGraph
import com.kamsiob.claritynow.ui.theme.ClarityDarkColors
import com.kamsiob.claritynow.ui.theme.ClarityLightColors
import com.kamsiob.claritynow.ui.theme.FocusPalette
import com.kamsiob.claritynow.ui.theme.resolveCalmMode
import kotlinx.coroutines.flow.first

/**
 * The one design DNA every widget in this app is built from. `design-v3.md` 12.1.
 *
 * Six widgets share 16dp of padding, one serif element each at most, 8dp inner radii,
 * **no borders and no colored edges**, and one separation device. A widget is already a
 * card, so nothing drawn inside one gets a second device: there are no hairlines here,
 * no nested surfaces and no elevation, and a reader looking for them should stop rather
 * than assume they were forgotten.
 *
 * ## Why the colors are restated rather than read from the theme
 *
 * `ui.theme.ClarityColors` is a Compose `CompositionLocal` resolved inside
 * `ClarityTheme`, and a widget has no composition of that kind: Glance renders to
 * `RemoteViews` in the launcher's process, where nothing this app provides is in scope.
 * What is shared is the **value**: every token below is read from
 * [ClarityLightColors] and [ClarityDarkColors] rather than typed out again, so a
 * change to the palette reaches the home screen without anybody remembering to copy it.
 *
 * Glance resolves day and night per render through `androidx.glance.color.ColorProvider`,
 * which is why every token here is a pair. It follows the **system** dark mode, not the
 * app's theme setting in 10.10: a widget is drawn by the launcher, the setting is a
 * preference about this app's own surfaces, and a widget that fought the home screen it
 * sits on would be the one dark card on a light wallpaper.
 *
 * ## Calm mode
 *
 * **No widget reads the calm mode preference while it is drawing.** It reaches a widget
 * in the snapshot, `data/widget/ClarityWidgetSnapshot.kt`, like everything else a widget
 * knows. [widgetCalmMode] below is what resolves it, on the writing side, and it is here
 * rather than in the writer because resolving it is a design question this file already
 * answers: `design-v3.md` 16.1 makes an untouched switch follow the system reduce motion
 * setting, so the answer is two inputs and not one preference.
 *
 * A widget has nothing that pulses and nothing that animates, so the motion half of the
 * switch has nothing to do on a home screen. **The tint transform still applies**, which
 * 12.1 requires of all eight widgets.
 */
internal object WidgetTheme {

    /** `design-v3.md` 12.1, and the only padding a widget in this app ever uses. */
    val padding = 16.dp

    /**
     * 12.1's inner radius. Nothing in the three widgets built in this file's package
     * needs it yet, because none of them draws a second surface inside the widget, and
     * it is stated here so that the first one that does uses this rather than picking
     * a number.
     */
    val innerRadius = 8.dp

    /** The content plane, `ui/theme/ClarityColors.kt`'s top rank. A widget is a card. */
    val card = ColorProvider(day = ClarityLightColors.card, night = ClarityDarkColors.card)

    val inkPrimary =
        ColorProvider(day = ClarityLightColors.inkPrimary, night = ClarityDarkColors.inkPrimary)

    val inkSecondary =
        ColorProvider(day = ClarityLightColors.inkSecondary, night = ClarityDarkColors.inkSecondary)

    val inkTertiary =
        ColorProvider(day = ClarityLightColors.inkTertiary, night = ClarityDarkColors.inkTertiary)

    /**
     * The one large element a widget is allowed, `design-v3.md` 12.1.
     *
     * **The system serif rather than Newsreader, and that is a limitation rather than a
     * choice.** Glance sets a typeface by family name on a `RemoteViews` text node, and
     * the launcher's process cannot load a font this app ships in `res/font`. The
     * families the platform exposes are serif, sans serif, monospace and cursive. So the
     * home screen gets the nearest true statement, a serif, and the app keeps
     * Newsreader. Nothing about the widget's proportions depends on the exact face.
     */
    val serifLarge = TextStyle(
        color = inkPrimary,
        fontSize = 22.sp,
        fontWeight = FontWeight.Normal,
        fontFamily = FontFamily.Serif,
        textAlign = TextAlign.Center,
    )

    /** The compact form of [serifLarge], for a 1x1 that has room for one short word. */
    val serifSmall = serifLarge.copy(fontSize = 17.sp)

    /** 12.1: sans for everything that is not the single large element. */
    val caption = TextStyle(
        color = inkSecondary,
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal,
        textAlign = TextAlign.Center,
    )

    val label = TextStyle(
        color = inkSecondary,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        textAlign = TextAlign.Center,
    )

    /**
     * The whole widget, as one surface with one separation device and no edge.
     *
     * The radius is the platform's own `system_app_widget_background_radius` rather than
     * a number of this app's choosing, because the launcher masks a widget to its own
     * radius and a second radius drawn inside that mask is the hairline of a corner.
     */
    fun surface(background: androidx.glance.unit.ColorProvider = card): GlanceModifier =
        GlanceModifier
            .fillMaxSize()
            .background(background)
            .cornerRadius(android.R.dimen.system_app_widget_background_radius)
            .padding(WidgetTheme.padding)
}

/**
 * The Contemplative world's ground, for the one widget that is a window into it.
 * `design-v3.md` 3.3 and 11.3.
 *
 * **It does not follow dark mode, and that is the point.** The Focus surface is the
 * indigo night whatever the theme says, per `ui/nav/ClarityShell.kt` and section 2, and
 * the ring's three colors, a white track at 16 percent and two pale indigos, were chosen
 * against this ground and are legible on nothing else. Painting the countdown widget on
 * a light card and keeping the specified ring would have been the obvious answer and
 * would have shipped a 6dp `#8BA4FF` line on a near white surface. `design-v3.md` 15
 * asks for the deliberate choice instead, and this is it: the widget is a piece of the
 * room, it renders identically in both themes because that room does, and one tap opens
 * the room it is showing.
 */
internal object FocusWidgetPalette {

    /** The outer stop of 3.3's radial gradient, drawn flat. A widget is not a room. */
    val ground = ColorProvider(day = FocusPalette.gradientEdge, night = FocusPalette.gradientEdge)

    val textBright = ColorProvider(day = ContemplativeBright, night = ContemplativeBright)

    /**
     * `design-v3.md` 3.3's `textDim` at 55 percent, and it replaced `textFaint` at 32.
     *
     * 13 says Contemplative text stays at or above 55 percent opacity where it is meant
     * to be read, and 32 percent measures **2.674 to one** on this widget's ground, the
     * outer stop of 3.3's radial drawn flat. This value measures 5.577 on the same
     * ground. It is the one word under the numeral, `remaining`, and a widget that says
     * how long is left has to be readable at arm's length on a home screen.
     */
    val textDim = ColorProvider(day = ContemplativeDim, night = ContemplativeDim)
}

private val ContemplativeBright = Color(0xFFF3F1EC)
private val ContemplativeDim = Color(0xFFF3F1EC).copy(alpha = 0.55f)

/**
 * Calm mode as one boolean, resolved the way the app resolves it. `design-v3.md` 16.1
 * and 12.1.
 *
 * A stored null means the person has never touched the switch, and while it is null the
 * answer follows the system's animator duration scale, exactly as
 * `ui/theme/ClarityTheme.kt` does. It is read once per call rather than observed,
 * because the caller is writing one snapshot and not holding a composition open.
 *
 * **It is called on the writing side and never while a widget draws.** The value it
 * returns goes into the snapshot, and every widget reads it from there, which is what
 * keeps the preference off the drawing path along with everything else 13.3 keeps off
 * it.
 */
internal suspend fun widgetCalmMode(context: Context): Boolean {
    if (!ClarityGraph.isInstalled) return false
    val stored = ClarityGraph.preferences.calmMode.first()
    return resolveCalmMode(stored, animationsAreOff(context.contentResolver))
}

private fun animationsAreOff(resolver: ContentResolver): Boolean =
    Settings.Global.getFloat(resolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
