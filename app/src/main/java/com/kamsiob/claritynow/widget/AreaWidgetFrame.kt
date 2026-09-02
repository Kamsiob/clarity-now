package com.kamsiob.claritynow.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.action.Action
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.height
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.semantics.contentDescription
import androidx.glance.semantics.semantics
import androidx.glance.text.FontFamily
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.kamsiob.claritynow.R
import com.kamsiob.claritynow.ui.theme.ClarityDarkColors
import com.kamsiob.claritynow.ui.theme.ClarityLightColors
import com.kamsiob.claritynow.ui.theme.ClaritySpacing
import com.kamsiob.claritynow.ui.theme.blendWith
import com.kamsiob.claritynow.ui.theme.calmed
import com.kamsiob.claritynow.ui.theme.parseAreaColor

/**
 * What `Next Up`, `First Step` and `All Areas` are drawn from. design-v3.md 12.1.
 *
 * The DNA itself lives in [WidgetTheme], which the whole phase shares. This file adds
 * the parts only an area shaped widget needs: the tint, the dot, the left aligned type
 * and the two notices a widget shows when it has nothing or when what it pointed at has
 * gone.
 *
 * ## The three rules that are easiest to break here
 *
 * **Color carries identity, and it carries it once.** The 7dp dot and the ground tint
 * are the two forms design-v3.md 3.4 allows a widget, and text is ink. The obvious
 * alternative, setting an area's name in its own color the way the area card does, is
 * the statistically common answer and it is refused per 15: on `All Areas` it would
 * make a list of six names into six colors of text and turn a quiet list into a chart,
 * and on the two small widgets it would be a third carrier of a fact the dot and the
 * ground already carry twice.
 *
 * **The tint is transformed by calm mode and the dot is not.** design-v3.md 16.2's
 * exclusion list keeps the 7dp dot and the area label at full saturation because they
 * are how an area is recognized and where contrast was measured, and 16.7 puts widget
 * tints in the transformed column. [areaSurface] and [AreaDot] are the two halves of
 * that sentence.
 *
 * **No border and no colored edge, ever.** Section 14. The widget is a card and the
 * card is the separation device, so nothing in here draws an outline, a hairline or a
 * second surface. There is no place in this file where one could be added without
 * deleting a line of this comment first.
 */
internal object AreaWidgetType {

    /** The area name beside the dot. `label` in design-v3.md 5.3, left aligned. */
    val areaLabel = TextStyle(
        color = WidgetTheme.inkSecondary,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
    )

    /**
     * The single serif element, 12.1.
     *
     * 17sp rather than the 22sp [WidgetTheme.serifLarge] uses, because these two
     * widgets set a sentence a person wrote rather than one short word, and a title of
     * eight words at 22sp in a 2x2 is three lines that do not fit. The face is the
     * platform serif for the reason [WidgetTheme.serifLarge] states.
     */
    val serif = TextStyle(
        color = WidgetTheme.inkPrimary,
        fontSize = 17.sp,
        fontWeight = FontWeight.Normal,
        fontFamily = FontFamily.Serif,
    )

    /**
     * The count, the prompt and the notices. `caption` in 5.3.
     *
     * **`inkSecondary`, and a widget is the place to be most careful about this.** A
     * launcher draws a widget on a wallpaper this app cannot see, so the one thing that
     * makes a number here measurable is that every widget paints its own opaque ground
     * first, `WidgetTheme.surface` or [areaSurface]. That ground is the `card` token or
     * `card` under a 3 to 5 percent area tint, 12.1, which is inside the range the
     * contrast audit already measures `inkSecondary` across. At `inkTertiary` this line
     * measured 2.402 to one there, against design-v3.md 13's floor of 4.5, and the
     * launcher's own day and night switch meant it was that faint in one theme or the
     * other on every home screen. What keeps it a rank under [serif] and [rowName] is
     * 12sp against 17 and 15, and under [areaLabel] the weight, not the color.
     */
    val caption = TextStyle(
        color = WidgetTheme.inkSecondary,
        fontSize = 12.5.sp,
        fontWeight = FontWeight.Normal,
    )

    /** An area's name in a row of them, `All Areas`. */
    val rowName = TextStyle(
        color = WidgetTheme.inkPrimary,
        fontSize = 15.sp,
        fontWeight = FontWeight.Medium,
    )

    /** What is active in that area, or that nothing is. */
    val rowStatus = TextStyle(
        color = WidgetTheme.inkSecondary,
        fontSize = 13.sp,
        fontWeight = FontWeight.Normal,
    )
}

/** Sizes this package uses more than once, so that no two files pick two numbers. */
internal object AreaWidgetMetrics {

    /** design-v3.md 3.4's dot, at the size every other surface draws it. */
    val dot = ClaritySpacing.areaDot

    /** Between the dot and the name, on every surface that pairs them. */
    val dotGap = 8.dp

    /**
     * One row in `All Areas`, and it is a touch target rather than a line of text.
     *
     * design-v3.md 13 puts the floor at 48dp and does not exempt a widget, so a row
     * that opens an area is 48dp even though its content is half that. The cost is
     * real and is the reason 12.2 specifies an overflow line at all: a 4x2 fits two or
     * three of these, says `and 2 more` under them, and grows if somebody drags it
     * taller. A denser row that reached more areas would be a target this app's own
     * accessibility section forbids.
     */
    val rowHeight = ClaritySpacing.minTouchTarget

    /** The overflow line, reserved out of the height before rows are counted. */
    val overflowHeight = 18.dp
}

/**
 * The whole widget, tinted with one area's accent. design-v3.md 12.1.
 *
 * The tint is 4 percent in light and 6 percent in dark, the middle of the 3 to 5 and 5
 * to 7 percent the DNA allows, and it is composited into the card color rather than
 * drawn as a translucent layer over it: a widget sits on a wallpaper, and an alpha that
 * looked right over the card would let the wallpaper through wherever the launcher
 * chose not to draw a ground.
 */
internal fun areaSurface(colorHex: String, calm: Boolean): GlanceModifier {
    val accent = parseAreaColor(colorHex).calmed(calm)
    return WidgetTheme.surface(
        background = ColorProvider(
            day = ClarityLightColors.card.blendWith(accent, LIGHT_TINT),
            night = ClarityDarkColors.card.blendWith(accent, DARK_TINT),
        ),
    )
}

/**
 * The 7dp identity dot, at full saturation in both worlds and in calm mode.
 *
 * design-v3.md 16.2 excludes it from the calm transform by name, so this takes the raw
 * hex and never the calmed accent. It is the same dot the area card draws, which is
 * what lets somebody recognize an area on the home screen without reading it.
 */
@Composable
internal fun AreaDot(colorHex: String) {
    val accent: Color = parseAreaColor(colorHex)
    Box(
        modifier = GlanceModifier
            .size(AreaWidgetMetrics.dot)
            .cornerRadius(AreaWidgetMetrics.dot / 2)
            .background(ColorProvider(day = accent, night = accent)),
        contentAlignment = Alignment.Center,
        content = {},
    )
}

/** The dot and the area's name, the pair every one of these widgets opens with. */
@Composable
internal fun AreaHeader(colorHex: String, name: String) {
    Row(verticalAlignment = Alignment.Vertical.CenterVertically) {
        AreaDot(colorHex)
        Spacer(GlanceModifier.width(AreaWidgetMetrics.dotGap))
        Text(text = name, style = AreaWidgetType.areaLabel, maxLines = 1)
    }
}

/**
 * What a widget says when it has nothing to show, or when what it was pointed at is
 * gone. design-v3.md 12.1 and 10.13.
 *
 * One serif line and one plain line under it, on the plain card with no tint, because
 * there is no area to take a tint from. An empty state is an invitation and never an
 * error: nothing here apologizes, reports a failure or names a code.
 */
@Composable
internal fun WidgetNotice(headline: String, body: String?, onTap: Action, spoken: String) {
    Column(
        modifier = WidgetTheme.surface()
            .clickable(onTap)
            .semantics { contentDescription = spoken },
        verticalAlignment = Alignment.Vertical.CenterVertically,
    ) {
        Text(text = headline, style = AreaWidgetType.serif, maxLines = 3)
        if (body != null) {
            Spacer(GlanceModifier.height(6.dp))
            Text(text = body, style = AreaWidgetType.caption, maxLines = 3)
        }
    }
}

/**
 * The line a widget shows when the area it was pinned to has been archived or deleted.
 * design-v3.md 12.1.
 *
 * **One plain line naming what happened, and a tap that fixes it.** Never an error,
 * never a blank box, and never a stale name that lies. The two cases are told apart
 * because only one of them still has a name to say: an archived area is still in the
 * log and can be named, a deleted one cannot.
 *
 * **This is the one tap in this package that the running session override does not
 * touch**, and the reason is that the line makes a promise. A widget that said "tap to
 * choose another area" and opened a countdown instead would be a sentence on a screen
 * that is not true, which is worse than the inconsistency. Every other tap in these
 * three widgets goes through [WidgetIntents.tap] and obeys the rule.
 */
@Composable
internal fun ReconfigureNotice(context: Context, appWidgetId: Int, archivedName: String?) {
    val line = if (archivedName == null) {
        context.getString(R.string.widget_area_gone)
    } else {
        context.getString(R.string.widget_area_archived, archivedName)
    }
    WidgetNotice(
        headline = line,
        body = null,
        onTap = actionStartActivity(WidgetConfiguration.configureIntent(context, appWidgetId)),
        spoken = line,
    )
}

/**
 * No areas at all, which is a real state rather than a failure. design-v3.md 10.15 and
 * 10.13.
 *
 * An empty state is an invitation, and the invitation is the one the Areas screen makes
 * in the same situation: the app opens on a screen whose one control makes an area.
 */
@Composable
internal fun NoAreasNotice(context: Context) {
    val headline = context.getString(R.string.widget_no_areas)
    val body = context.getString(R.string.widget_no_areas_invitation)
    WidgetNotice(
        headline = headline,
        body = body,
        onTap = actionStartActivity(WidgetIntents.app(context)),
        spoken = "$headline. $body",
    )
}

/**
 * `3 waiting`, or nothing at all.
 *
 * A direct readout of a queried number, which is what `CLAUDE.md` rule 8 keeps in
 * `strings.xml`. Absent at zero: see [NextUpWidget] for why a widget does not report an
 * empty queue back to the person who emptied it.
 */
internal fun waitingLine(context: Context, queueCount: Int): String? =
    if (queueCount <= 0) {
        null
    } else {
        context.resources.getQuantityString(R.plurals.widget_waiting, queueCount, queueCount)
    }

/** design-v3.md 12.1's light tint, the middle of 3 to 5 percent. */
private const val LIGHT_TINT = 0.04f

/** And its dark counterpart, the middle of 5 to 7. */
private const val DARK_TINT = 0.06f
