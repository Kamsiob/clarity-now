package com.kamsiob.claritynow.ui.settings

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.kamsiob.claritynow.BuildConfig
import com.kamsiob.claritynow.data.export.BackupRead
import com.kamsiob.claritynow.data.export.BackupService
import com.kamsiob.claritynow.data.export.ExportReminder
import com.kamsiob.claritynow.data.export.ImportRefusal
import com.kamsiob.claritynow.data.export.SyncTarget
import com.kamsiob.claritynow.data.prefs.AfterCompleting
import com.kamsiob.claritynow.data.prefs.ClarityPreferences
import com.kamsiob.claritynow.data.repo.ClarityRepository
import com.kamsiob.claritynow.data.repo.IngestMode
import com.kamsiob.claritynow.di.ClarityGraph
import com.kamsiob.claritynow.domain.ClarityClock
import com.kamsiob.claritynow.ui.theme.ClarityTextSize
import com.kamsiob.claritynow.ui.theme.ClarityThemeSetting
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException
import java.time.ZoneId

/** Which long running data operation is in flight, if any. */
internal enum class DataTask { EXPORTING, IMPORTING, ERASING }

/**
 * What just happened to the person's data, said once and then dismissed.
 *
 * Every one of these is about a file or is a direct readout of a count, which is why
 * they resolve to fixed strings rather than to corpus lines. `ImportRefusal` carries
 * the argument for the refusals and MASTER_BUILD_PROMPT 14b.7 is where it is granted;
 * the rest are readouts of a number the screen has just produced.
 *
 * [ExportFailed.diagnostic] is the same idea as `BackupRead.Refused.diagnostic`: it is
 * for a bug report and it never reaches a screen, because there is nothing a person can
 * do with the difference between a revoked grant and a full disk.
 */
internal sealed interface DataMessage {
    data class Exported(val eventCount: Int, val encrypted: Boolean) : DataMessage
    data class Imported(val eventCount: Int) : DataMessage
    data class ImportWasRefused(val reason: ImportRefusal) : DataMessage
    data class ExportFailed(val diagnostic: String) : DataMessage
    data object Erased : DataMessage
}

@Immutable
internal data class SettingsUiState(
    val theme: ClarityThemeSetting = ClarityThemeSetting.SYSTEM,
    val textSize: ClarityTextSize = ClarityTextSize.DEFAULT,
    val afterCompleting: AfterCompleting = AfterCompleting.AUTO_PROMOTE,
    val focusHighlight: Boolean = true,
    val focusMinutes: Int = ClarityPreferences.DEFAULT_FOCUS_MINUTES,
    val transitionWarning: Boolean = false,
    val pulseReminders: Boolean = true,
    val pulseReminderHour: Int = ClarityPreferences.DEFAULT_REMINDER_HOUR,
    val lastExportAt: Long? = null,
    val exportIsStale: Boolean = false,
    val busy: DataTask? = null,
    val message: DataMessage? = null,
    val pendingImport: BackupRead.Opened? = null,
)

/**
 * Everything the Settings screen reads and every write it makes.
 * MASTER_BUILD_PROMPT 14.1, 14.2 and 14b.7.
 *
 * **The backup path is `data.export`'s and this only drives it.** Export, the optional
 * password, the checksum, the pre validation and the replace or merge choice all live
 * in `BackupService`, and the two step shape of an import, read then apply, is the
 * mechanism 14b.7 asks for: [confirmImport] takes a `BackupRead.Opened`, which nothing
 * but a passing validation can produce, so there is no path from this screen into the
 * database that skipped a check.
 *
 * **Calm mode is deliberately absent from [SettingsUiState].** Its stored value is
 * nullable and the interface never shows the third state, per design-v3.md 16.1, and
 * the resolved two state value already reaches every composable through
 * `LocalCalmMode`, which `ClarityTheme` computes from the stored value and the live
 * system reduce motion setting. Putting a copy here would be a second answer to a
 * question that already has one, and it would be the answer that does not follow the
 * system setting live.
 */
internal class SettingsViewModel(
    private val repository: ClarityRepository,
    private val preferences: ClarityPreferences,
    private val backups: BackupService,
    private val clock: ClarityClock,
) : ViewModel() {

    private val tasks = MutableStateFlow(TaskState())

    /**
     * The three settings that are a choice among named values.
     *
     * **Text size sits here and not where calm mode sits, which is nowhere.** Calm mode
     * is absent from this state because its stored value is nullable and the resolved
     * answer already reaches the screen through `LocalCalmMode`. Text size has no third
     * state to resolve and nothing computes it from a system setting, so the screen needs
     * the stored value itself to show which row carries the check, and this is where it
     * comes from. What the screen must not read for that is `LocalDensity`, whose
     * `fontScale` is the phone's setting and this one already multiplied together.
     */
    private val choicePrefs: Flow<ChoicePrefs> = combine(
        preferences.theme,
        preferences.textSize,
        preferences.afterCompleting,
    ) { theme, textSize, after -> ChoicePrefs(theme, textSize, after) }

    private val focusPrefs: Flow<FocusPrefs> = combine(
        preferences.focusHighlightEnabled,
        preferences.focusDurationMinutes,
        preferences.transitionWarningEnabled,
    ) { highlight, minutes, warning -> FocusPrefs(highlight, minutes, warning) }

    private val reminderPrefs: Flow<ReminderPrefs> = combine(
        preferences.pulseRemindersEnabled,
        preferences.pulseReminderHour,
    ) { enabled, hour -> ReminderPrefs(enabled, hour) }

    /**
     * The export date, and when the data worth keeping started.
     *
     * The projection is the trigger rather than the source: what "real data" means is
     * `ClarityRepository.dataWorthKeepingSince`, and `ExportReminder` documents why it
     * is an item and not an area. Restating that rule here would be a second definition
     * of it, and the two would drift.
     */
    private val dataPrefs: Flow<DataPrefs> =
        combine(preferences.lastExportAt, repository.state) { lastExport, _ ->
            DataPrefs(lastExport, repository.dataWorthKeepingSince())
        }

    /**
     * Five flows, grouped rather than flattened, because `combine` is typed to five
     * and the groups are the shape of the screen anyway.
     */
    val state: StateFlow<SettingsUiState> = combine(
        choicePrefs,
        focusPrefs,
        reminderPrefs,
        dataPrefs,
        tasks,
    ) { choice, focus, reminder, data, task ->
        SettingsUiState(
            theme = choice.theme,
            textSize = choice.textSize,
            afterCompleting = choice.afterCompleting,
            focusHighlight = focus.highlight,
            focusMinutes = focus.minutes,
            transitionWarning = focus.warning,
            pulseReminders = reminder.enabled,
            pulseReminderHour = reminder.hour,
            lastExportAt = data.lastExportAt,
            exportIsStale = ExportReminder.isDue(clock, data.lastExportAt, data.dataSince),
            busy = task.busy,
            message = task.message,
            pendingImport = task.pendingImport,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), SettingsUiState())

    /**
     * The zone every date on this screen is formatted in.
     *
     * Read through the injected clock rather than from `ZoneId.systemDefault()` at the
     * call site, because this app has exactly one source of time and one source of zone
     * and a screen is not allowed to be a second one. `ClarityClock` says why.
     */
    val zone: ZoneId get() = clock.zone()

    /** The name the picker is opened with. The person can change it. */
    fun suggestedFileName(): String = backups.suggestedFileName()

    fun setTheme(value: ClarityThemeSetting) = write { preferences.setTheme(value) }

    /**
     * design-v3.md 13. It multiplies the phone's font scale rather than replacing it,
     * so every step including `DEFAULT` still follows the phone.
     */
    fun setTextSize(value: ClarityTextSize) = write { preferences.setTextSize(value) }

    /** The first call stops calm mode following the system. design-v3.md 16.1. */
    fun setCalmMode(value: Boolean) = write { preferences.setCalmMode(value) }

    fun setFocusHighlight(value: Boolean) = write { preferences.setFocusHighlightEnabled(value) }

    fun setFocusMinutes(value: Int) = write { preferences.setFocusDurationMinutes(value) }

    fun setTransitionWarning(value: Boolean) =
        write { preferences.setTransitionWarningEnabled(value) }

    /**
     * After completing, MASTER_BUILD_PROMPT 14.1.
     *
     * **This is the one preference in this screen that writes to the log**, and the seam
     * it needed took four phases to land. Phase 11 built the screen, found `commit` was
     * private with no method taking an arbitrary payload, and reported the gap rather
     * than reaching around the single write path. `ClarityRepository.recordSettingChanged`
     * is that method, and it is deliberately not a general one: it says at its own
     * declaration which preferences belong in the log and which do not.
     *
     * **The previous value is read before the write and passed in**, because
     * `SettingChanged` carries both and a reducer that had to look up what a setting used
     * to be would be reading state to describe a change to state.
     *
     * Everything downstream already existed. `ClarityEventType.SETTING_CHANGED` is in the
     * catalog, the reducer folds it, `TrailRow` renders it and `TrailFacts` keeps it in as
     * something the person did, so the row appeared in the Trail with nothing else to
     * change.
     */
    fun setAfterCompleting(value: AfterCompleting) = write {
        val previous = state.value.afterCompleting
        preferences.setAfterCompleting(value)
        repository.recordSettingChanged(
            key = "afterCompleting",
            previousValue = previous.name,
            newValue = value.name,
        )
    }

    fun setPulseReminders(value: Boolean) = write { preferences.setPulseRemindersEnabled(value) }

    fun setPulseReminderHour(value: Int) = write { preferences.setPulseReminderHour(value) }

    /** MASTER_BUILD_PROMPT 14.1, Help. The tutorial runs again on the next launch. */
    fun replayTutorial() = write { preferences.setHasSeenTutorial(false) }

    /** MASTER_BUILD_PROMPT 14.1, Help. Onboarding runs again, and nothing is erased. */
    fun replayWelcome() = write { preferences.setHasCompletedOnboarding(false) }

    /**
     * Writes the whole database to the file the person chose. MASTER_BUILD_PROMPT 14b.7.
     *
     * [password] null writes a readable file, which the export sheet says plainly rather
     * than implying a safety that is not there. The rebuild from event zero that 6.4
     * requires of this path happens inside `BackupService.export`, before a byte is
     * handed over.
     *
     * `IOException` is caught here because `LocalFileTarget` deliberately does not: a
     * target that swallowed a failed write would report a backup that does not exist,
     * which is the one lie this feature cannot tell.
     */
    fun export(target: SyncTarget, password: CharArray?) {
        if (tasks.value.busy != null) return
        tasks.value = TaskState(busy = DataTask.EXPORTING)
        viewModelScope.launch {
            val outcome = try {
                backups.export(target, password)
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (failure: IOException) {
                tasks.value = TaskState(
                    message = DataMessage.ExportFailed(failure.message ?: "no detail"),
                )
                return@launch
            }
            tasks.value = TaskState(
                message = DataMessage.Exported(outcome.eventCount, outcome.encrypted),
            )
        }
    }

    /**
     * Reads a file and proves it, and writes nothing whatever the answer is.
     *
     * A refusal of `PASSWORD_REQUIRED` or `WRONG_PASSWORD` is an ordinary answer rather
     * than an error, and the screen offers the field again. Nothing has been touched in
     * either case and nothing can have been: the reading half of `data.export` holds no
     * store.
     */
    fun readForImport(target: SyncTarget, password: CharArray?) {
        if (tasks.value.busy != null) return
        tasks.value = TaskState(busy = DataTask.IMPORTING)
        viewModelScope.launch {
            tasks.value = when (val read = backups.read(target, password)) {
                is BackupRead.Refused ->
                    TaskState(message = DataMessage.ImportWasRefused(read.reason))

                is BackupRead.Opened -> TaskState(pendingImport = read)
            }
        }
    }

    /**
     * Takes the validated file in. Both modes throw every checkpoint away and fold the
     * log again from event zero, which `ClarityRepository.ingestForeignLog` does and
     * explains: a merge can introduce events that sort before a checkpoint's position,
     * and a checkpoint resumed over one of those quietly drops it forever.
     */
    fun confirmImport(mode: IngestMode) {
        val pending = tasks.value.pendingImport ?: return
        tasks.value = TaskState(busy = DataTask.IMPORTING)
        viewModelScope.launch {
            val check = backups.apply(pending, mode)
            tasks.value = TaskState(message = DataMessage.Imported(check.eventCount))
        }
    }

    fun cancelImport() {
        tasks.update { it.copy(pendingImport = null, busy = null) }
    }

    /**
     * MASTER_BUILD_PROMPT 14.2. The log, every cache table, every checkpoint and every
     * DataStore key except a freshly minted `originId`.
     *
     * `hasCompletedOnboarding` goes with the rest, which is what returns the app to
     * onboarding: the first run gate reads that key and finds it absent, exactly as it
     * does on a new install.
     */
    fun eraseEverything() {
        if (tasks.value.busy != null) return
        tasks.value = TaskState(busy = DataTask.ERASING)
        viewModelScope.launch {
            repository.eraseEverything()
            tasks.value = TaskState(message = DataMessage.Erased)
        }
    }

    fun dismissMessage() {
        tasks.update { it.copy(message = null) }
    }

    /** Called when the screen leaves, so a file waiting for a choice does not outlive it. */
    fun forgetTransientState() {
        tasks.value = TaskState()
    }

    private fun write(block: suspend () -> Unit) {
        viewModelScope.launch { block() }
    }

    @Immutable
    private data class TaskState(
        val busy: DataTask? = null,
        val message: DataMessage? = null,
        val pendingImport: BackupRead.Opened? = null,
    )

    private data class ChoicePrefs(
        val theme: ClarityThemeSetting,
        val textSize: ClarityTextSize,
        val afterCompleting: AfterCompleting,
    )

    private data class FocusPrefs(val highlight: Boolean, val minutes: Int, val warning: Boolean)

    private data class ReminderPrefs(val enabled: Boolean, val hour: Int)

    private data class DataPrefs(val lastExportAt: Long?, val dataSince: Long?)

    companion object {

        private const val STOP_TIMEOUT_MILLIS = 5_000L

        /**
         * A local factory, and the reason it is local rather than a branch in
         * `ClarityViewModelFactory`.
         *
         * **`di/ViewModels.kt` and `di/ClarityGraph.kt` were both outside phase 11's file
         * list**: the binding belongs in that `when`, the `BackupService` belongs beside the
         * repository in `ClarityGraph`, and moving both is a paste and a delete. Momentum and
         * the Report were in the same position and are out of it since issue #55, so this is
         * now the last local factory in the app. Nothing here reads the `CreationExtras`, so
         * it can be resolved against any store owner, including one that supplies no
         * Application.
         *
         * **One `BackupService` per ViewModel is correct rather than a leak.** It holds
         * three references and a `SecureRandom` and no state of its own; the state that
         * matters, the log and the export date, lives in the repository and the
         * preferences, which are process wide.
         */
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                check(modelClass == SettingsViewModel::class.java) {
                    "SettingsViewModel.Factory was asked for ${modelClass.name}"
                }
                return SettingsViewModel(
                    repository = ClarityGraph.repository,
                    preferences = ClarityGraph.preferences,
                    backups = BackupService(
                        store = ClarityGraph.repository,
                        clock = ClarityGraph.clock,
                        appVersion = BuildConfig.VERSION_NAME,
                    ),
                    clock = ClarityGraph.clock,
                ) as T
            }
        }
    }
}
