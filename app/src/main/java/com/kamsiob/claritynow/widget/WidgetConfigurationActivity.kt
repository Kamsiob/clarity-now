package com.kamsiob.claritynow.widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kamsiob.claritynow.R
import com.kamsiob.claritynow.data.widget.ClarityWidgetSnapshotStore
import com.kamsiob.claritynow.di.ClarityGraph
import com.kamsiob.claritynow.ui.components.ClarityButton
import com.kamsiob.claritynow.ui.components.ClarityButtonRole
import com.kamsiob.claritynow.ui.components.ClarityIcon
import com.kamsiob.claritynow.ui.components.ClarityIcons
import com.kamsiob.claritynow.ui.components.clarityClickable
import com.kamsiob.claritynow.ui.theme.ClarityHapticEvent
import com.kamsiob.claritynow.ui.theme.ClaritySpacing
import com.kamsiob.claritynow.ui.theme.ClarityTextSize
import com.kamsiob.claritynow.ui.theme.ClarityTheme
import com.kamsiob.claritynow.ui.theme.ClarityThemeSetting
import com.kamsiob.claritynow.ui.theme.LocalClarityColors
import com.kamsiob.claritynow.ui.theme.LocalClarityHaptics
import com.kamsiob.claritynow.ui.theme.LocalClarityTypography
import com.kamsiob.claritynow.ui.theme.parseAreaColor
import kotlinx.coroutines.launch

/**
 * Where a placed widget is pointed at an area. design-v3.md 12.2.
 *
 * ## It is optional, and it is reachable
 *
 * The provider metadata carries `configuration_optional`, so this never opens when a
 * widget is placed: the widget appears immediately showing the automatic area, and this
 * screen is reached afterward from the launcher's own edit affordance and from the line
 * a widget shows when the area it was pinned to has gone. [WidgetConfiguration] records
 * why that is the choice rather than the usual placement time form.
 *
 * ## One screen for three widgets
 *
 * Which widget is being configured comes from the app widget id, because the launcher
 * starts this with nothing else, so there is one entrance rather than two that could
 * drift. `Next Up` and `First Step` choose one area or automatic; `All Areas` chooses a
 * subset, or all of them.
 *
 * ## A single choice commits itself, a subset does not
 *
 * One tap is already the whole decision, so the single choice saves and closes on it
 * with no button to press afterward. A subset cannot do that, since choosing three
 * areas is three taps, so it keeps a `Done`. The rows are the app's own choice rows
 * from `ui/settings`: a label, a check when it is the current answer, `Role` on the row
 * and `selected` in its semantics, because design-v3.md 13 does not let a check be the
 * only signal.
 *
 * ## The result matters even though placement never waits for it
 *
 * `RESULT_CANCELED` is set before anything is drawn. The launcher reads it on the
 * placement path and would remove a widget whose configuration was abandoned, which is
 * the correct behavior on the day `configuration_optional` is ever taken off.
 */
class WidgetConfigurationActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val appWidgetId = intent?.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID,
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID
        setResult(RESULT_CANCELED, result(appWidgetId))

        val kind = WidgetConfiguration.kindOf(this, appWidgetId)
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID || kind == null) {
            // Started with no widget to configure, which is either a launcher that has
            // forgotten it or something outside the app poking at an exported Activity.
            // Nothing to show and nothing to save.
            finish()
            return
        }

        setContent {
            val theme by ClarityGraph.preferences.theme
                .collectAsStateWithLifecycle(initialValue = ClarityThemeSetting.SYSTEM)
            val calmMode by ClarityGraph.preferences.calmMode
                .collectAsStateWithLifecycle(initialValue = null)
            // The configuration screen is an app screen and reads the app's text size
            // like every other one. The widget itself does not: it renders in the
            // launcher's process, where none of this app's composition locals exist,
            // and it takes the launcher's own font scale. design-v3.md 12.1.
            val textSize by ClarityGraph.preferences.textSize
                .collectAsStateWithLifecycle(initialValue = ClarityTextSize.DEFAULT)
            ClarityTheme(setting = theme, calmMode = calmMode, textSize = textSize) {
                CompositionLocalProvider(LocalClarityHaptics provides ClarityGraph.haptics) {
                    ConfigurationScreen(
                        kind = kind,
                        appWidgetId = appWidgetId,
                        onSaved = {
                            setResult(RESULT_OK, result(appWidgetId))
                            finish()
                        },
                        onDismissed = { finish() },
                    )
                }
            }
        }
    }

    private fun result(appWidgetId: Int): Intent =
        Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
}

@Composable
private fun ConfigurationScreen(
    kind: WidgetKind,
    appWidgetId: Int,
    onSaved: () -> Unit,
    onDismissed: () -> Unit,
) {
    val context = LocalContext.current
    val colors = LocalClarityColors.current
    val type = LocalClarityTypography.current
    val scope = rememberCoroutineScope()
    val store = remember(context) { ClarityWidgetSnapshotStore(context) }
    // The areas come from the snapshot rather than from the repository, which is the
    // same boundary the widgets read through: this screen can only ever offer what a
    // widget could draw, and it needs no replay to open.
    val snapshot by store.snapshot.collectAsStateWithLifecycle(initialValue = null)
    val areas = snapshot?.liveAreas.orEmpty()

    var pinnedAreaId by remember { mutableStateOf<String?>(null) }
    var loaded by remember { mutableStateOf(false) }
    // Empty means every area, which is what the widget stores and what an unconfigured
    // one already holds.
    val chosen = remember { mutableStateListOf<String>() }
    LaunchedEffect(appWidgetId) {
        val glanceId = WidgetConfiguration.glanceIdOf(context, appWidgetId)
        pinnedAreaId = WidgetConfiguration.areaId(context, glanceId)
        chosen.addAll(WidgetConfiguration.areaIds(context, glanceId))
        loaded = true
    }

    fun save(block: suspend (GlanceId) -> Unit) {
        scope.launch {
            val glanceId = WidgetConfiguration.glanceIdOf(context, appWidgetId)
            block(glanceId)
            // The widget redraws before this screen closes, so the home screen is
            // already right when it reappears behind it.
            WidgetConfiguration.widgetFor(kind).update(context, glanceId)
            onSaved()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.canvas)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = ClaritySpacing.scaled(28.dp)),
    ) {
        Text(
            text = context.getString(
                if (kind == WidgetKind.ALL_AREAS) {
                    R.string.widget_config_title_subset
                } else {
                    R.string.widget_config_title_one
                },
            ),
            style = type.title,
            color = colors.inkPrimary,
        )
        Spacer(Modifier.height(ClaritySpacing.scaled(20.dp)))

        if (areas.isEmpty()) {
            Text(
                text = context.getString(R.string.widget_no_areas),
                style = type.body,
                color = colors.inkSecondary,
            )
            Spacer(Modifier.height(ClaritySpacing.scaled(24.dp)))
            ClarityButton(
                label = context.getString(R.string.action_close),
                onClick = onDismissed,
                role = ClarityButtonRole.SECONDARY,
            )
            return@Column
        }

        when (kind) {
            WidgetKind.NEXT_UP, WidgetKind.FIRST_STEP -> {
                ChoiceRow(
                    label = context.getString(R.string.widget_config_automatic),
                    explainer = context.getString(R.string.widget_config_automatic_explainer),
                    colorHex = null,
                    selected = loaded && pinnedAreaId == null,
                    role = Role.RadioButton,
                    onClick = { save { id -> WidgetConfiguration.setAreaId(context, id, null) } },
                )
                areas.forEach { area ->
                    ChoiceRow(
                        label = area.name,
                        explainer = null,
                        colorHex = area.colorHex,
                        selected = pinnedAreaId == area.id,
                        role = Role.RadioButton,
                        onClick = {
                            save { id -> WidgetConfiguration.setAreaId(context, id, area.id) }
                        },
                    )
                }
            }

            WidgetKind.ALL_AREAS -> {
                Text(
                    text = context.getString(R.string.widget_config_all_explainer),
                    style = type.caption,
                    color = colors.inkSecondary,
                )
                Spacer(Modifier.height(ClaritySpacing.scaled(12.dp)))
                areas.forEach { area ->
                    val checked = chosen.isEmpty() || chosen.contains(area.id)
                    ChoiceRow(
                        label = area.name,
                        explainer = null,
                        colorHex = area.colorHex,
                        selected = checked,
                        role = Role.Checkbox,
                        onClick = {
                            // An empty list means all, so the first tap has to start
                            // from all of them and take one away. Without this, turning
                            // one area off would turn every other area off with it.
                            if (chosen.isEmpty()) chosen.addAll(areas.map { it.id })
                            if (checked) chosen.remove(area.id) else chosen.add(area.id)
                        },
                    )
                }
                Spacer(Modifier.height(ClaritySpacing.scaled(24.dp)))
                ClarityButton(
                    label = context.getString(R.string.action_done),
                    onClick = {
                        save { id ->
                            // Every area chosen is the same thing as no subset at all,
                            // and storing it as none keeps a widget following areas
                            // made after today.
                            val subset =
                                if (chosen.size == areas.size) emptyList() else chosen.toList()
                            WidgetConfiguration.setAreaIds(context, id, subset)
                        }
                    },
                )
            }
        }
    }
}

/** One row of a choice list: a label, and a check when it is the current answer. */
@Composable
private fun ChoiceRow(
    label: String,
    explainer: String?,
    colorHex: String?,
    selected: Boolean,
    role: Role,
    onClick: () -> Unit,
) {
    val colors = LocalClarityColors.current
    val type = LocalClarityTypography.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 56.dp)
            .clarityClickable(
                haptic = ClarityHapticEvent.SELECT,
                role = role,
                onClickLabel = label,
                onClick = onClick,
            )
            // Without this the chosen row and an unchosen one are the same node to a
            // screen reader, which leaves the check as a sighted only signal.
            // design-v3.md 13.
            .semantics { this.selected = selected }
            .padding(vertical = ClaritySpacing.scaled(10.dp)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (colorHex != null) {
            Spacer(
                Modifier
                    .size(7.dp)
                    .background(parseAreaColor(colorHex), CircleShape),
            )
            Spacer(Modifier.width(12.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = type.body,
                color = if (selected) colors.inkPrimary else colors.inkSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (explainer != null) {
                // design-v3.md 3.1 and 13. The explainer is what tells somebody which
                // of two similar choices they are about to pin to their home screen,
                // and it measured 2.337 to one. The row's own rank is already carried
                // twice, by the `caption` role and by the label above going from
                // `inkSecondary` to `inkPrimary` when the row is chosen.
                Text(text = explainer, style = type.caption, color = colors.inkSecondary)
            }
        }
        if (selected) {
            Spacer(Modifier.width(12.dp))
            ClarityIcon(
                icon = ClarityIcons.check,
                contentDescription = null,
                tint = colors.actionBlue,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}
