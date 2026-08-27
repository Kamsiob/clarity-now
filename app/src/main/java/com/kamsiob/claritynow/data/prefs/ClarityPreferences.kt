package com.kamsiob.claritynow.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.kamsiob.claritynow.ui.theme.ClarityThemeSetting
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID

/** What happens to an area the moment its active item is completed. */
enum class AfterCompleting {
    /** The queue head is promoted immediately, with the hero animation. */
    AUTO_PROMOTE,

    /** A chooser opens. Dismissing it leaves the area idle, which is a real state. */
    CHOOSE_FROM_QUEUE,
}

/**
 * Per device settings and flags. MASTER_BUILD_PROMPT 5.4.
 *
 * Nothing the Logic Engine reads may live here. Variation history, escalation
 * state, personal records, first ever flags and plan history all derive from the
 * event log, so that two devices holding the same log compute the same sentence.
 * DataStore does not merge, and getting this wrong does not fail loudly.
 *
 * **The date the app was last opened belongs to that list and is the easiest of
 * them to get wrong**, because it looks like a device preference and is one line to
 * store here. It is engine state: Addendum 01 4d greets a person differently after
 * a fortnight away, so a key here would have two devices disagreeing about whether
 * someone had been absent, and the one they happened to open would decide. It lives
 * in the log as `APP_OPENED`, written by `ClarityRepository.recordAppOpened`, which
 * reads the log to decide whether today already has one. MASTER_BUILD_PROMPT 14b.4
 * and DECISIONS.md C7.
 *
 * The two values here that are not user settings, `originId` and `lamportCounter`,
 * are device identity rather than engine state. They are what a merge is performed
 * with, not something a merge has to agree about.
 */
class ClarityPreferences(private val context: Context) {

    private val Context.store: DataStore<Preferences> by preferencesDataStore(name = "clarity_settings")

    private object Keys {
        val theme = stringPreferencesKey("theme")
        val focusDurationMinutes = intPreferencesKey("focusDurationMinutes")
        val focusHighlightEnabled = booleanPreferencesKey("focusHighlightEnabled")
        val afterCompleting = stringPreferencesKey("afterCompleting")
        val pulseRemindersEnabled = booleanPreferencesKey("pulseRemindersEnabled")
        val pulseReminderHour = intPreferencesKey("pulseReminderHour")
        val hasCompletedOnboarding = booleanPreferencesKey("hasCompletedOnboarding")
        val hasSeenTutorial = booleanPreferencesKey("hasSeenTutorial")
        val originId = stringPreferencesKey("originId")
        val lamportCounter = longPreferencesKey("lamportCounter")
        val lastExportAt = longPreferencesKey("lastExportAt")
    }

    val theme: Flow<ClarityThemeSetting> = context.store.data.map { prefs ->
        prefs[Keys.theme]?.let { name ->
            ClarityThemeSetting.entries.firstOrNull { it.name == name }
        } ?: ClarityThemeSetting.SYSTEM
    }

    val focusDurationMinutes: Flow<Int> =
        context.store.data.map { it[Keys.focusDurationMinutes] ?: DEFAULT_FOCUS_MINUTES }

    val focusHighlightEnabled: Flow<Boolean> =
        context.store.data.map { it[Keys.focusHighlightEnabled] ?: true }

    val afterCompleting: Flow<AfterCompleting> = context.store.data.map { prefs ->
        prefs[Keys.afterCompleting]?.let { name ->
            AfterCompleting.entries.firstOrNull { it.name == name }
        } ?: AfterCompleting.AUTO_PROMOTE
    }

    val pulseRemindersEnabled: Flow<Boolean> =
        context.store.data.map { it[Keys.pulseRemindersEnabled] ?: true }

    val pulseReminderHour: Flow<Int> =
        context.store.data.map { it[Keys.pulseReminderHour] ?: DEFAULT_REMINDER_HOUR }

    val hasCompletedOnboarding: Flow<Boolean> =
        context.store.data.map { it[Keys.hasCompletedOnboarding] ?: false }

    val hasSeenTutorial: Flow<Boolean> =
        context.store.data.map { it[Keys.hasSeenTutorial] ?: false }

    val lastExportAt: Flow<Long?> = context.store.data.map { it[Keys.lastExportAt] }

    suspend fun setTheme(value: ClarityThemeSetting) = put(Keys.theme, value.name)
    suspend fun setFocusDurationMinutes(value: Int) = put(Keys.focusDurationMinutes, value)
    suspend fun setFocusHighlightEnabled(value: Boolean) = put(Keys.focusHighlightEnabled, value)
    suspend fun setAfterCompleting(value: AfterCompleting) = put(Keys.afterCompleting, value.name)
    suspend fun setPulseRemindersEnabled(value: Boolean) = put(Keys.pulseRemindersEnabled, value)
    suspend fun setPulseReminderHour(value: Int) = put(Keys.pulseReminderHour, value)
    suspend fun setHasCompletedOnboarding(value: Boolean) = put(Keys.hasCompletedOnboarding, value)
    suspend fun setHasSeenTutorial(value: Boolean) = put(Keys.hasSeenTutorial, value)
    suspend fun setLastExportAt(value: Long) = put(Keys.lastExportAt, value)

    suspend fun currentAfterCompleting(): AfterCompleting = afterCompleting.first()

    /**
     * Generated once at install and stable afterwards. Identifies the device so two
     * logs can be merged and two order keys can differ. It is never sent anywhere,
     * because there is nowhere to send it, and it identifies a phone rather than a
     * person.
     */
    suspend fun originId(): String {
        context.store.data.first()[Keys.originId]?.let { return it }
        val generated = UUID.randomUUID().toString()
        var winner = generated
        context.store.edit { prefs ->
            // Re-check inside the transaction so two callers racing at first launch
            // cannot end up with two different device identities.
            val existing = prefs[Keys.originId]
            if (existing != null) {
                winner = existing
            } else {
                prefs[Keys.originId] = generated
            }
        }
        return winner
    }

    /**
     * Reserves [count] consecutive lamport values and returns the first.
     *
     * [atLeast] carries the highest lamport already seen in the log, so a counter
     * that fell behind after an import advances to max(local, seen) + 1 rather than
     * minting values that sort before events already written.
     */
    suspend fun reserveLamport(count: Int, atLeast: Long): Long {
        var first = 0L
        context.store.edit { prefs ->
            val current = maxOf(prefs[Keys.lamportCounter] ?: 0L, atLeast)
            first = current + 1
            prefs[Keys.lamportCounter] = current + count
        }
        return first
    }

    /**
     * Erase all data, MASTER_BUILD_PROMPT 14.2. Every key goes, and a fresh
     * `originId` is minted, because the old one is device identity tied to a log
     * that no longer exists.
     */
    suspend fun eraseEverything() {
        context.store.edit { prefs ->
            prefs.clear()
            prefs[Keys.originId] = UUID.randomUUID().toString()
        }
    }

    private suspend fun <T> put(key: Preferences.Key<T>, value: T) {
        context.store.edit { it[key] = value }
    }

    companion object {
        const val DEFAULT_FOCUS_MINUTES = 25
        const val DEFAULT_REMINDER_HOUR = 20
        val FOCUS_DURATION_OPTIONS = listOf(5, 10, 15, 20, 25, 30, 45, 60)
    }
}
