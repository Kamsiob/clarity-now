package com.kamsiob.claritynow.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kamsiob.claritynow.BuildConfig
import com.kamsiob.claritynow.R
import com.kamsiob.claritynow.data.prefs.AfterCompleting
import com.kamsiob.claritynow.notifications.NotificationPermissionOnReminderEnabled
import com.kamsiob.claritynow.ui.about.SupportBlock
import com.kamsiob.claritynow.ui.components.ClarityCard
import com.kamsiob.claritynow.ui.components.ClarityIcons
import com.kamsiob.claritynow.ui.theme.ClaritySpacing
import com.kamsiob.claritynow.ui.theme.ClarityTextSize
import com.kamsiob.claritynow.ui.theme.ClarityThemeSetting
import com.kamsiob.claritynow.ui.theme.LocalClarityColors
import com.kamsiob.claritynow.ui.theme.LocalClarityTypography
import java.time.ZoneId

/**
 * Settings, MASTER_BUILD_PROMPT 14.1.
 *
 * A Daylight screen, rows on canvas under sentence case sideheads, **no card
 * containers**, in the order 14.1 gives: Daily routine, Focus, After completing,
 * Appearance, Your data, Privacy, Help, then the support block, then the version line.
 *
 * The one card on this screen is the permission card under Privacy, which 14.1 asks
 * for by name. It is not a row and it is not a container for rows.
 *
 * **Appearance holds three things rather than two since issue #51**: the theme tiles, the
 * text size control and calm mode. Text size went in that group rather than in an eighth
 * one because 14.1 fixes the seven groups and their order, and because it is plainly an
 * appearance setting. It also makes this screen the preview for itself: every row,
 * sidehead, caption and switch above and below the control re-lays out at the chosen size
 * the moment it is tapped, which is the whole reason `TextSizePicker` ships no specimen
 * paragraph of its own. design-v3.md 13.2.
 *
 * [calmMode] arrives already resolved, from `LocalCalmMode`, because the stored value
 * is nullable and the interface never shows the third state. design-v3.md 16.1.
 */
@Composable
internal fun SettingsScreen(
    state: SettingsUiState,
    calmMode: Boolean,
    zone: ZoneId,
    onBack: () -> Unit,
    onThemeChange: (ClarityThemeSetting) -> Unit,
    onTextSizeChange: (ClarityTextSize) -> Unit,
    onCalmModeChange: (Boolean) -> Unit,
    onFocusHighlightChange: (Boolean) -> Unit,
    onTransitionWarningChange: (Boolean) -> Unit,
    onAfterCompletingChange: (AfterCompleting) -> Unit,
    onPulseRemindersChange: (Boolean) -> Unit,
    onOpenReflection: () -> Unit,
    onOpenSessionLength: () -> Unit,
    onOpenReminderHour: () -> Unit,
    onExport: () -> Unit,
    onImport: () -> Unit,
    onOpenErase: () -> Unit,
    onOpenPrivacy: () -> Unit,
    onOpenLicenses: () -> Unit,
    onReplayTour: () -> Unit,
    onReplayWelcome: () -> Unit,
    onOpenAbout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalClarityColors.current
    val type = LocalClarityTypography.current

    // MASTER_BUILD_PROMPT 13.4. The one moment this app may ask for permission to post
    // a notification other than the start of a focus session, and it fires on the
    // transition rather than on the value, so a person who leaves the default alone is
    // never asked. The rule lives in NotificationMoment; this is the call site it was
    // written for.
    NotificationPermissionOnReminderEnabled(state.pulseReminders)

    PushedScreen(
        title = stringResource(R.string.settings_title),
        onBack = onBack,
        modifier = modifier,
    ) {
        SettingsGroup(title = stringResource(R.string.settings_group_daily)) {
            SettingsRow(
                icon = ClarityIcons.pulse,
                groupColor = SettingsGroupColors.daily,
                title = stringResource(R.string.settings_daily_reflection),
                onClick = onOpenReflection,
            )
            SettingsToggleRow(
                icon = ClarityIcons.reminders,
                groupColor = SettingsGroupColors.daily,
                title = stringResource(R.string.settings_pulse_reminder),
                checked = state.pulseReminders,
                onCheckedChange = onPulseRemindersChange,
                divider = state.pulseReminders,
            )
            // 14.1: shown only when the toggle is on. Absent rather than present and
            // disabled, for the reason design-v3.md 10.16 gives about the unfiled row:
            // a disabled control is a question the person then has to answer.
            if (state.pulseReminders) {
                SettingsRow(
                    icon = ClarityIcons.time,
                    groupColor = SettingsGroupColors.daily,
                    title = stringResource(R.string.settings_remind_at),
                    value = formatHour(state.pulseReminderHour),
                    onClick = onOpenReminderHour,
                    divider = false,
                )
            }
        }

        GroupGap()

        SettingsGroup(title = stringResource(R.string.settings_group_focus)) {
            SettingsToggleRow(
                icon = ClarityIcons.focus,
                groupColor = SettingsGroupColors.focus,
                title = stringResource(R.string.settings_focus_highlight),
                checked = state.focusHighlight,
                onCheckedChange = onFocusHighlightChange,
            )
            SettingsRow(
                icon = ClarityIcons.focusEvent,
                groupColor = SettingsGroupColors.focus,
                title = stringResource(R.string.settings_session_length),
                value = stringResource(R.string.settings_session_length_value, state.focusMinutes),
                onClick = onOpenSessionLength,
            )
            SettingsToggleRow(
                icon = ClarityIcons.reminders,
                groupColor = SettingsGroupColors.focus,
                title = stringResource(R.string.settings_transition_warning),
                caption = stringResource(R.string.settings_transition_warning_caption),
                checked = state.transitionWarning,
                onCheckedChange = onTransitionWarningChange,
                divider = false,
            )
        }

        GroupGap()

        SettingsGroup(title = stringResource(R.string.settings_group_after)) {
            Spacer(Modifier.height(ClaritySpacing.scaled(4.dp)))
            SettingsSegmentedChoice(
                options = listOf(
                    AfterCompleting.AUTO_PROMOTE to stringResource(R.string.settings_after_promote),
                    AfterCompleting.CHOOSE_FROM_QUEUE to
                        stringResource(R.string.settings_after_choose),
                ),
                selected = state.afterCompleting,
                onSelect = onAfterCompletingChange,
            )
            Spacer(Modifier.height(ClaritySpacing.scaled(10.dp)))
            Text(
                text = when (state.afterCompleting) {
                    AfterCompleting.AUTO_PROMOTE ->
                        stringResource(R.string.settings_after_promote_explainer)

                    AfterCompleting.CHOOSE_FROM_QUEUE ->
                        stringResource(R.string.settings_after_choose_explainer)
                },
                style = type.body,
                color = colors.inkSecondary,
            )
        }

        GroupGap()

        SettingsGroup(title = stringResource(R.string.settings_group_appearance)) {
            Spacer(Modifier.height(ClaritySpacing.scaled(4.dp)))
            AppearancePicker(
                selected = state.theme,
                onSelect = onThemeChange,
                lightLabel = stringResource(R.string.settings_theme_light),
                darkLabel = stringResource(R.string.settings_theme_dark),
                systemLabel = stringResource(R.string.settings_theme_system),
            )
            Spacer(Modifier.height(ClaritySpacing.scaled(12.dp)))
            Text(
                text = stringResource(R.string.settings_appearance_note),
                style = type.caption,
                // design-v3.md 3.1 and 13. The note explains what the three theme
                // choices actually do, which is the only place that is said.
                color = colors.inkSecondary,
            )
            // Text size sits inside Appearance rather than in a group of its own,
            // because MASTER_BUILD_PROMPT 14.1 fixes the seven groups and their order and
            // an eighth would be a change to that document rather than an addition to
            // this screen. It is also, plainly, an appearance setting.
            Spacer(Modifier.height(ClaritySpacing.scaled(20.dp)))
            TextSizePicker(selected = state.textSize, onSelect = onTextSizeChange)
            Spacer(Modifier.height(ClaritySpacing.scaled(16.dp)))
            SettingsToggleRow(
                icon = ClarityIcons.editArea,
                groupColor = SettingsGroupColors.appearance,
                title = stringResource(R.string.settings_calm_mode),
                caption = stringResource(R.string.settings_calm_mode_caption),
                checked = calmMode,
                onCheckedChange = onCalmModeChange,
                divider = false,
            )
        }

        GroupGap()

        SettingsGroup(title = stringResource(R.string.settings_group_data)) {
            SettingsRow(
                icon = ClarityIcons.export,
                groupColor = SettingsGroupColors.data,
                title = stringResource(R.string.settings_export),
                value = state.lastExportAt
                    ?.let { formatDate(it, zone) }
                    ?: stringResource(R.string.settings_export_never),
                onClick = onExport,
                chevron = false,
            )
            SettingsRow(
                icon = ClarityIcons.importData,
                groupColor = SettingsGroupColors.data,
                title = stringResource(R.string.settings_import),
                onClick = onImport,
                chevron = false,
            )
            SettingsRow(
                icon = ClarityIcons.erase,
                groupColor = SettingsGroupColors.data,
                title = stringResource(R.string.settings_erase),
                onClick = onOpenErase,
                divider = false,
            )
            // MASTER_BUILD_PROMPT 14b.7, in Settings only and nowhere else in the app.
            // Addendum 01 4h's plain statement about an unencrypted file is on the
            // export sheet rather than here, because with the password built it is
            // conditional: a permanent line on this screen saying the file is readable
            // would be false for everybody who used one.
            if (state.exportIsStale) {
                Spacer(Modifier.height(ClaritySpacing.scaled(12.dp)))
                Text(
                    text = stringResource(R.string.settings_export_stale),
                    style = type.caption,
                    color = colors.inkSecondary,
                )
            }
            val busy = state.busy
            val message = state.message
            if (busy != null) {
                Spacer(Modifier.height(ClaritySpacing.scaled(6.dp)))
                Text(
                    text = stringResource(busyLabelOf(busy)),
                    style = type.caption,
                    color = colors.inkSecondary,
                )
            } else if (message != null) {
                Spacer(Modifier.height(ClaritySpacing.scaled(6.dp)))
                Text(
                    text = dataMessageText(message),
                    style = type.caption,
                    color = colors.inkSecondary,
                )
            }
        }

        GroupGap()

        SettingsGroup(title = stringResource(R.string.settings_group_privacy)) {
            SettingsRow(
                icon = ClarityIcons.privacy,
                groupColor = SettingsGroupColors.privacy,
                title = stringResource(R.string.settings_privacy_policy),
                onClick = onOpenPrivacy,
            )
            SettingsRow(
                icon = ClarityIcons.licenses,
                groupColor = SettingsGroupColors.privacy,
                title = stringResource(R.string.settings_licenses),
                onClick = onOpenLicenses,
                divider = false,
            )
            Spacer(Modifier.height(ClaritySpacing.scaled(14.dp)))
            PermissionCard()
        }

        GroupGap()

        SettingsGroup(title = stringResource(R.string.settings_group_help)) {
            SettingsRow(
                icon = ClarityIcons.regenerate,
                groupColor = SettingsGroupColors.help,
                title = stringResource(R.string.settings_replay_tour),
                onClick = onReplayTour,
                chevron = false,
            )
            SettingsRow(
                icon = ClarityIcons.regenerate,
                groupColor = SettingsGroupColors.help,
                title = stringResource(R.string.settings_replay_welcome),
                onClick = onReplayWelcome,
                chevron = false,
            )
            SettingsRow(
                icon = ClarityIcons.mark,
                groupColor = SettingsGroupColors.help,
                title = stringResource(R.string.settings_about),
                onClick = onOpenAbout,
                divider = false,
            )
        }

        Spacer(Modifier.height(ClaritySpacing.scaled(30.dp)))
        SupportBlock()

        Spacer(Modifier.height(ClaritySpacing.scaled(26.dp)))
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.settings_version_line, BuildConfig.VERSION_NAME),
                style = type.caption,
                // The colophon is quiet because of where it sits and how small it is,
                // 6.1's first device, not because of its color. A version string a
                // person is asked to quote in a bug report has to be readable.
                // design-v3.md 3.1 and 13.
                color = colors.inkSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(ClaritySpacing.scaled(3.dp)))
            Text(
                text = stringResource(R.string.settings_license_line),
                style = type.caption,
                color = colors.inkSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/**
 * The permission card, MASTER_BUILD_PROMPT 14.1.
 *
 * The one card on this screen, and the one claim in the app that a person is invited to
 * go and check for themselves. `verifyNoInternetPermission` is what keeps it true, on
 * the merged manifest of every variant, on every build.
 */
@Composable
private fun PermissionCard() {
    val colors = LocalClarityColors.current
    val type = LocalClarityTypography.current
    ClarityCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(
                horizontal = ClaritySpacing.cardPaddingHorizontal,
                vertical = ClaritySpacing.cardPaddingVertical,
            ),
        ) {
            Text(
                text = stringResource(R.string.permission_card_heading),
                style = type.bodyStrong,
                color = colors.inkPrimary,
            )
            Spacer(Modifier.height(ClaritySpacing.scaled(8.dp)))
            Text(
                text = stringResource(R.string.permission_card_body),
                style = type.bodySerif,
                color = colors.inkSecondary,
            )
        }
    }
}

/** design-v3.md section 6: 28dp between sections. */
@Composable
private fun GroupGap() {
    Spacer(Modifier.height(ClaritySpacing.sectionGap))
}

private fun busyLabelOf(task: DataTask): Int = when (task) {
    DataTask.EXPORTING -> R.string.settings_export_working
    DataTask.IMPORTING -> R.string.settings_import_working
    DataTask.ERASING -> R.string.settings_erase_working
}

/**
 * One sentence about what just happened to a file or to the log.
 *
 * Every branch is a fixed string, and MASTER_BUILD_PROMPT 14b.7 is where that is
 * permitted: these are sentences about a file, not observations about the person, and
 * the counts are direct readouts of a number the screen has just produced.
 *
 * The export line names which of the two files was written, because Addendum 01 4h
 * requires the readable case to be said plainly and the moment it is written is the
 * last moment anybody is looking.
 */
@Composable
private fun dataMessageText(message: DataMessage): String = when (message) {
    is DataMessage.Exported -> stringResource(
        if (message.encrypted) {
            R.string.settings_export_done_encrypted
        } else {
            R.string.settings_export_done
        },
        message.eventCount,
    )

    is DataMessage.Imported ->
        stringResource(R.string.settings_import_done, message.eventCount)

    // The diagnostic the failure carries is for a bug report and never reaches a
    // screen: there is nothing a person can do with the difference between a revoked
    // grant and a full disk except pick somewhere else.
    is DataMessage.ExportFailed -> stringResource(R.string.settings_export_error)

    DataMessage.Erased -> stringResource(R.string.settings_erase_done)

    is DataMessage.ImportWasRefused -> importRefusalText(message.reason)
}
