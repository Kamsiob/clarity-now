package com.kamsiob.claritynow.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.kamsiob.claritynow.R
import com.kamsiob.claritynow.data.export.BackupRead
import com.kamsiob.claritynow.data.export.ImportRefusal
import com.kamsiob.claritynow.data.prefs.ClarityPreferences
import com.kamsiob.claritynow.data.repo.IngestMode
import com.kamsiob.claritynow.ui.components.ClarityButton
import com.kamsiob.claritynow.ui.components.ClarityButtonRole
import com.kamsiob.claritynow.ui.components.ClarityIcon
import com.kamsiob.claritynow.ui.components.ClarityIcons
import com.kamsiob.claritynow.ui.components.ClaritySheet
import com.kamsiob.claritynow.ui.components.ClarityTextField
import com.kamsiob.claritynow.ui.components.clarityClickable
import com.kamsiob.claritynow.ui.theme.ClarityHapticEvent
import com.kamsiob.claritynow.ui.theme.LocalClarityColors
import com.kamsiob.claritynow.ui.theme.LocalClarityTypography
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/** The horizontal inset every sheet's content sits on. design-v3.md 6, screen padding. */
private val SHEET_PADDING = 20.dp

/** A ceiling for the sheets that hold running prose, so their scroll has a bound. */
private val SHEET_SCROLL_MAX = 560.dp

internal val HOUR_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.US)

internal val EXPORT_DATE_FORMAT: DateTimeFormatter =
    DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.US)

/** `h:mm a` for a stored hour, which is all the reminder preference holds. */
internal fun formatHour(hour: Int): String = LocalTime.of(hour.coerceIn(0, 23), 0).format(HOUR_FORMAT)

internal fun formatDate(millis: Long, zone: ZoneId): String =
    Instant.ofEpochMilli(millis).atZone(zone).toLocalDate().format(EXPORT_DATE_FORMAT)

/**
 * Whether a typed confirmation matches the word it has to match.
 *
 * **Case is not part of the test, and that is the deliberate choice rather than the
 * obvious one.** design-v3.md 15 asks for the obvious answer to be checked: an exact
 * comparison against `ERASE` is one line shorter and is what every typed confirmation
 * ships with. It loses here on who this app is for. `ClarityTextField` sets sentence
 * capitalization for every field in the app, so an exact match makes this the one
 * control that cannot be operated without finding caps lock, and the barrier it adds
 * is manual dexterity rather than deliberation. What the gate is for is that somebody
 * typed five specific letters on purpose, and they have done that either way.
 */
internal fun confirmationMatches(typed: String, required: String): Boolean =
    typed.trim().equals(required, ignoreCase = true)

/**
 * The explainer behind the Daily reflection row, MASTER_BUILD_PROMPT 14.1.
 *
 * Fixed copy about how the app works. MASTER_BUILD_PROMPT 11.2 puts a description of a
 * behavior here rather than in a corpus, and 14b.11 makes the same point about the
 * onboarding line that announces the Pulse: telling somebody what the app will do
 * before it does it is not an observation about them, and predictability is worth more
 * to this audience than a surprise.
 */
@Composable
internal fun DailyReflectionSheet(onDismiss: () -> Unit) {
    ClaritySheet(onDismiss = onDismiss, title = stringResource(R.string.settings_reflection_sheet_title)) {
        val colors = LocalClarityColors.current
        val type = LocalClarityTypography.current
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = SHEET_SCROLL_MAX)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = SHEET_PADDING),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            listOf(
                R.string.settings_reflection_body_1,
                R.string.settings_reflection_body_2,
                R.string.settings_reflection_body_3,
            ).forEach { body ->
                Text(
                    text = stringResource(body),
                    style = type.bodySerif,
                    color = colors.inkSecondary,
                )
            }
        }
    }
}

/** Session length, MASTER_BUILD_PROMPT section 10's eight options. */
@Composable
internal fun SessionLengthSheet(
    selected: Int,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    ClaritySheet(onDismiss = onDismiss, title = stringResource(R.string.settings_session_length_title)) {
        Column(modifier = Modifier.fillMaxWidth().selectableGroup()) {
            ClarityPreferences.FOCUS_DURATION_OPTIONS.forEach { minutes ->
                ChoiceRow(
                    label = stringResource(R.string.settings_session_length_value, minutes),
                    selected = minutes == selected,
                    onClick = {
                        onSelect(minutes)
                        onDismiss()
                    },
                )
            }
        }
    }
}

/**
 * The hour the reminder arrives, MASTER_BUILD_PROMPT 12.1.
 *
 * A list of the twenty four hours rather than a clock dial. The preference stores an
 * hour and nothing finer, so a dial would offer a minute this app cannot keep: the
 * reminder is WorkManager work, not an exact alarm, and a picker that let somebody
 * choose 20:37 would be promising a precision the scheduler is explicit about not
 * having.
 */
@Composable
internal fun ReminderHourSheet(
    selected: Int,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    ClaritySheet(onDismiss = onDismiss, title = stringResource(R.string.settings_remind_at)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = SHEET_SCROLL_MAX)
                .verticalScroll(rememberScrollState())
                .selectableGroup(),
        ) {
            (0..23).forEach { hour ->
                ChoiceRow(
                    label = formatHour(hour),
                    selected = hour == selected,
                    onClick = {
                        onSelect(hour)
                        onDismiss()
                    },
                )
            }
        }
    }
}

/**
 * Erase all data, MASTER_BUILD_PROMPT 14.2.
 *
 * **A sheet, not a dialog**, and the difference is not cosmetic: a dialog is a thing
 * that happened to you and a sheet is a place you went, and this is somewhere a person
 * has to be able to leave by the same gesture they leave everything else by.
 *
 * The typed word lives in a plain `remember` rather than a `rememberSaveable`, which is
 * how design-v3.md 10.15's "back dismisses without erasing and discards the typed text"
 * is kept: leaving the sheet takes the state with it, so there is nothing to discard by
 * hand and nothing that can come back.
 *
 * The button is `DESTRUCTIVE`, which design-v3.md 10.7 defines as inert grey until its
 * condition is met and then ink filled, and **never red**.
 */
@Composable
internal fun EraseSheet(
    onErase: () -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = LocalClarityColors.current
    val type = LocalClarityTypography.current
    val requiredWord = stringResource(R.string.settings_erase_word)
    var typed by remember { mutableStateOf("") }

    ClaritySheet(onDismiss = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = SHEET_SCROLL_MAX)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = SHEET_PADDING),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = stringResource(R.string.settings_erase_title),
                style = type.displayTitle,
                color = colors.inkPrimary,
            )
            Text(
                text = stringResource(R.string.settings_erase_body_1),
                style = type.bodySerif,
                color = colors.inkSecondary,
            )
            Text(
                text = stringResource(R.string.settings_erase_body_2),
                style = type.bodySerif,
                color = colors.inkSecondary,
            )
            Text(
                text = stringResource(R.string.settings_erase_nudge),
                style = type.body,
                color = colors.inkTertiary,
            )
            ClarityTextField(
                value = typed,
                onValueChange = { typed = it },
                label = stringResource(R.string.settings_erase_prompt),
            )
            Spacer(Modifier.height(2.dp))
            ClarityButton(
                label = stringResource(R.string.settings_erase_confirm),
                onClick = onErase,
                role = ClarityButtonRole.DESTRUCTIVE,
                enabled = confirmationMatches(typed, requiredWord),
            )
            ClarityButton(
                label = stringResource(R.string.settings_erase_keep),
                onClick = onDismiss,
                role = ClarityButtonRole.SECONDARY,
            )
        }
    }
}

/**
 * The export sheet, MASTER_BUILD_PROMPT 14b.7 and Addendum 01 4h.
 *
 * **The password is offered and never required, and the sheet says which of the two
 * files it is about to write.** 4h asks for exactly that: when there is no password the
 * file is readable and the screen says so plainly rather than implying a safety it does
 * not provide.
 *
 * **The password is visible while it is typed, which is the deliberate choice rather
 * than the obvious one.** design-v3.md 15: every password field in every app masks by
 * default. It loses here on what the two failures cost. A shoulder surfer is not the
 * threat model for a backup of your own task list, written to your own phone; a
 * mistyped password is, because the file it produces cannot be opened by anybody
 * including the person who made it, and they will not find that out until the day they
 * need it. `ClarityTextField` carries no masking parameter, so this is also what the
 * component does today, and adding one would be the change to make if this is ever
 * revisited.
 */
@Composable
internal fun ExportSheet(
    onExport: (CharArray?) -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = LocalClarityColors.current
    val type = LocalClarityTypography.current
    var password by remember { mutableStateOf("") }

    ClaritySheet(onDismiss = onDismiss, title = stringResource(R.string.settings_export_title)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = SHEET_SCROLL_MAX)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = SHEET_PADDING),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            ClarityTextField(
                value = password,
                onValueChange = { password = it },
                label = stringResource(R.string.settings_export_password_label),
            )
            Text(
                text = if (password.isEmpty()) {
                    stringResource(R.string.settings_export_password_none)
                } else {
                    stringResource(R.string.settings_export_password_set)
                },
                style = type.bodySerif,
                color = colors.inkSecondary,
            )
            Spacer(Modifier.height(2.dp))
            ClarityButton(
                label = stringResource(R.string.settings_export_choose),
                onClick = { onExport(password.takeIf { it.isNotEmpty() }?.toCharArray()) },
            )
        }
    }
}

/**
 * The password prompt for a file that turned out to be protected.
 *
 * It is reached from a refusal rather than from a guess, so the sentence above the
 * field is the refusal itself: [ImportRefusal.PASSWORD_REQUIRED] the first time and
 * [ImportRefusal.WRONG_PASSWORD] on a second attempt. Nothing has been written in
 * either case and nothing can have been.
 */
@Composable
internal fun ImportPasswordSheet(
    refusal: ImportRefusal,
    onOpen: (CharArray) -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = LocalClarityColors.current
    val type = LocalClarityTypography.current
    var password by remember { mutableStateOf("") }

    ClaritySheet(
        onDismiss = onDismiss,
        title = stringResource(R.string.settings_import_password_title),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = SHEET_SCROLL_MAX)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = SHEET_PADDING),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = importRefusalText(refusal),
                style = type.bodySerif,
                color = colors.inkSecondary,
            )
            ClarityTextField(
                value = password,
                onValueChange = { password = it },
                label = stringResource(R.string.settings_import_password_label),
            )
            Spacer(Modifier.height(2.dp))
            ClarityButton(
                label = stringResource(R.string.settings_import_password_open),
                onClick = { onOpen(password.toCharArray()) },
                enabled = password.isNotEmpty(),
            )
            ClarityButton(
                label = stringResource(R.string.settings_import_cancel),
                onClick = onDismiss,
                role = ClarityButtonRole.SECONDARY,
            )
        }
    }
}

/**
 * The one sentence a refused file gets.
 *
 * `ImportRefusal` holds the wording and the argument for why these six are fixed
 * strings rather than corpus lines: they are about a file, not about a person's own
 * data, and MASTER_BUILD_PROMPT 14b.7 grants the exception by name. The diagnostic each
 * refusal also carries is for a bug report and never reaches a screen.
 */
@Composable
internal fun importRefusalText(refusal: ImportRefusal): String = stringResource(
    when (refusal) {
        ImportRefusal.NOT_A_BACKUP -> R.string.import_refused_not_a_backup
        ImportRefusal.NEWER_VERSION -> R.string.import_refused_newer_version
        ImportRefusal.DAMAGED -> R.string.import_refused_damaged
        ImportRefusal.PASSWORD_REQUIRED -> R.string.import_refused_password_required
        ImportRefusal.WRONG_PASSWORD -> R.string.import_refused_wrong_password
        ImportRefusal.INCONSISTENT -> R.string.import_refused_inconsistent
    },
)

/**
 * The choice a validated file offers, MASTER_BUILD_PROMPT 14b.7.
 *
 * Nothing has been written by the time this sheet appears: the file has already been
 * parsed, opened, checked against its checksum and checked for internal consistency,
 * and `BackupRead.Opened` is a value that only `BackupCodec.read` can produce.
 *
 * **Replace is behind a typed confirmation and merge is not**, because they are not the
 * same act. Merge cannot lose anything: it is a union by event id, so the worst it can
 * do is add. Replace empties the log first, and a log is the only copy.
 */
@Composable
internal fun ImportSheet(
    opened: BackupRead.Opened,
    zone: ZoneId,
    onConfirm: (IngestMode) -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = LocalClarityColors.current
    val type = LocalClarityTypography.current
    val requiredWord = stringResource(R.string.settings_import_confirm_word)
    var mode by remember { mutableStateOf(IngestMode.MERGE) }
    var typed by remember { mutableStateOf("") }

    ClaritySheet(onDismiss = onDismiss, title = stringResource(R.string.settings_import_title)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = SHEET_SCROLL_MAX)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = SHEET_PADDING),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = stringResource(
                    R.string.settings_import_summary,
                    opened.events.size,
                    formatDate(opened.header.createdAt, zone),
                ),
                style = type.caption,
                color = colors.inkTertiary,
            )
            SettingsSegmentedChoice(
                options = listOf(
                    IngestMode.MERGE to stringResource(R.string.settings_import_merge),
                    IngestMode.REPLACE to stringResource(R.string.settings_import_replace),
                ),
                selected = mode,
                onSelect = { mode = it },
            )
            Text(
                text = when (mode) {
                    IngestMode.MERGE -> stringResource(R.string.settings_import_merge_explainer)
                    IngestMode.REPLACE -> stringResource(R.string.settings_import_replace_explainer)
                },
                style = type.bodySerif,
                color = colors.inkSecondary,
            )
            if (mode == IngestMode.REPLACE) {
                ClarityTextField(
                    value = typed,
                    onValueChange = { typed = it },
                    label = stringResource(R.string.settings_import_confirm_prompt),
                )
            }
            Spacer(Modifier.height(2.dp))
            ClarityButton(
                label = stringResource(R.string.settings_import),
                onClick = { onConfirm(mode) },
                role = if (mode == IngestMode.REPLACE) {
                    ClarityButtonRole.DESTRUCTIVE
                } else {
                    ClarityButtonRole.PRIMARY
                },
                enabled = mode == IngestMode.MERGE || confirmationMatches(typed, requiredWord),
            )
            ClarityButton(
                label = stringResource(R.string.settings_import_cancel),
                onClick = onDismiss,
                role = ClarityButtonRole.SECONDARY,
            )
        }
    }
}

/**
 * The privacy policy, MASTER_BUILD_PROMPT 14.3, word for word.
 *
 * **The bold lead-ins are set on their own line in the sans face rather than inline in
 * the serif, and that is a compile time fact rather than a taste.** `ClarityType.kt`
 * builds each serif role as a family holding one face with the weight axis baked into
 * the typeface, so a `SpanStyle(fontWeight = ...)` inside a `bodySerif` paragraph
 * resolves back to the same pinned instance and renders at 400 with no error anywhere.
 * That defect is documented on `HankenGrotesk` in that file, where it cost two phases.
 * Every word 14.3 specifies is here and in order; what changed is the line it sits on.
 */
@Composable
internal fun PrivacySheet(onDismiss: () -> Unit) {
    val colors = LocalClarityColors.current
    val type = LocalClarityTypography.current
    ClaritySheet(onDismiss = onDismiss, title = stringResource(R.string.privacy_title)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = SHEET_SCROLL_MAX)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = SHEET_PADDING),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Text(
                text = stringResource(R.string.privacy_heading),
                style = type.title,
                color = colors.inkPrimary,
            )
            Text(
                text = stringResource(R.string.privacy_body_1),
                style = type.bodySerif,
                color = colors.inkSecondary,
            )
            listOf(
                R.string.privacy_lead_2 to R.string.privacy_body_2,
                R.string.privacy_lead_3 to R.string.privacy_body_3,
                R.string.privacy_lead_4 to R.string.privacy_body_4,
                R.string.privacy_lead_5 to R.string.privacy_body_5,
                R.string.privacy_lead_6 to R.string.privacy_body_6,
                R.string.privacy_lead_7 to R.string.privacy_body_7,
            ).forEach { (lead, body) ->
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = stringResource(lead),
                        style = type.bodyStrong,
                        color = colors.inkPrimary,
                    )
                    Text(
                        text = stringResource(body),
                        style = type.bodySerif,
                        color = colors.inkSecondary,
                    )
                }
            }
        }
    }
}

/** Open source licenses, MASTER_BUILD_PROMPT 14.1. */
@Composable
internal fun LicensesSheet(onDismiss: () -> Unit) {
    val colors = LocalClarityColors.current
    val type = LocalClarityTypography.current
    ClaritySheet(onDismiss = onDismiss, title = stringResource(R.string.licenses_title)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = SHEET_SCROLL_MAX)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = SHEET_PADDING),
        ) {
            listOf(
                R.string.licenses_app to R.string.licenses_app_terms,
                R.string.licenses_newsreader to R.string.licenses_font_terms,
                R.string.licenses_hanken to R.string.licenses_font_terms,
                R.string.licenses_symbols to R.string.licenses_apache_terms,
                R.string.licenses_androidx to R.string.licenses_apache_terms,
            ).forEachIndexed { index, (name, terms) ->
                if (index > 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(colors.hairline),
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 52.dp)
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(name),
                        style = type.body,
                        color = colors.inkPrimary,
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = stringResource(terms),
                        style = type.caption,
                        color = colors.inkTertiary,
                    )
                }
            }
        }
    }
}

/** One row of a choice list: a label, and a check when it is the current answer. */
@Composable
private fun ChoiceRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val colors = LocalClarityColors.current
    val type = LocalClarityTypography.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 52.dp)
            .clarityClickable(
                haptic = ClarityHapticEvent.SELECT,
                role = Role.RadioButton,
                onClickLabel = label,
                onClick = onClick,
            )
            .semantics { this.selected = selected }
            .padding(horizontal = SHEET_PADDING, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = type.body,
            color = if (selected) colors.inkPrimary else colors.inkSecondary,
            modifier = Modifier.weight(1f),
        )
        if (selected) {
            ClarityIcon(
                icon = ClarityIcons.check,
                contentDescription = null,
                tint = colors.actionBlue,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}
