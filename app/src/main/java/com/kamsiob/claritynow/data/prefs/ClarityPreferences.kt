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
import com.kamsiob.claritynow.ui.theme.ClarityTextSize
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
 * The focus session this device is running, and the instant its planned time runs
 * out. MASTER_BUILD_PROMPT 10, "the computed end timestamp persists so the session
 * survives process death".
 *
 * **Read `CLAUDE.md` rule 6 first and then read this, because the rule makes anyone
 * who finds this key suspicious and the distinction is real.** No engine state may
 * live in DataStore, and this is not engine state.
 *
 * The session's truth is the log: `FOCUS_STARTED` gives the start and the item, and
 * the folded `FOCUS_EXTENDED` events give the planned duration, so any device
 * holding the log computes the same end instant with no help from here. What this
 * key adds is the one fact in a focus session that is genuinely about a phone
 * rather than about a person: **which running session this device is the one
 * running.** A merged log can legitimately carry two running sessions, one per
 * device, and each phone must show its own; a stored id answers that in a map
 * lookup where the log answers it with a query against the origin of a
 * `FOCUS_STARTED`. [endsAtMillis] rides along because the ongoing notification and
 * the completion alarm need an absolute instant at moments in the process lifecycle
 * where no projection has been loaded yet.
 *
 * **The log wins whenever the two disagree.** `ClarityRepository.pickDeviceSession`
 * falls back to the log when this key is missing or names a session that is no
 * longer running, and repairs the key from what it finds. Nothing reads
 * [endsAtMillis] in preference to the folded value, so a stale copy cannot move a
 * countdown, a Trail row or a duration a person is shown.
 *
 * No corpus line, no observation and no engine layer reads this, which is the test
 * `CLAUDE.md` rule 6 is really applying: two devices holding the same log still
 * compute the same sentence about focus while showing different countdowns, because
 * only one of them is running the session.
 *
 * Both halves are written and cleared in one `edit`, so a reader can never see an
 * id with no instant or an instant with no id.
 */
data class FocusHandle(val sessionId: String, val endsAtMillis: Long)

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
        val calmMode = booleanPreferencesKey("calmMode")
        val textSize = stringPreferencesKey("textSize")
        val focusDurationMinutes = intPreferencesKey("focusDurationMinutes")
        val focusHighlightEnabled = booleanPreferencesKey("focusHighlightEnabled")
        val focusSessionId = stringPreferencesKey("focusSessionId")
        val focusSessionEndsAt = longPreferencesKey("focusSessionEndsAt")
        val transitionWarningEnabled = booleanPreferencesKey("transitionWarningEnabled")
        val afterCompleting = stringPreferencesKey("afterCompleting")
        val pulseRemindersEnabled = booleanPreferencesKey("pulseRemindersEnabled")
        val pulseReminderHour = intPreferencesKey("pulseReminderHour")
        val hasCompletedOnboarding = booleanPreferencesKey("hasCompletedOnboarding")
        val hasSeenTutorial = booleanPreferencesKey("hasSeenTutorial")
        val hasSeenPulseIntro = booleanPreferencesKey("hasSeenPulseIntro")
        val reEntrySettledOn = stringPreferencesKey("reEntrySettledOn")
        val originId = stringPreferencesKey("originId")
        val lamportCounter = longPreferencesKey("lamportCounter")
        val lastExportAt = longPreferencesKey("lastExportAt")
    }

    val theme: Flow<ClarityThemeSetting> = context.store.data.map { prefs ->
        prefs[Keys.theme]?.let { name ->
            ClarityThemeSetting.entries.firstOrNull { it.name == name }
        } ?: ClarityThemeSetting.SYSTEM
    }

    /**
     * Calm mode, design-v3.md 16 and MASTER_BUILD_PROMPT 14b.12. Addendum 01 item 8c.
     *
     * **Null, not false, while the user has never touched the switch**, and that is the
     * whole design of this key. The specified default is not "off"; it is "whatever the
     * system reduce-motion setting says, live, with no restart", per design-v3.md 16.1.
     * A `Boolean` defaulting to false would silently mean off for every person who has
     * the system setting on and never opens Settings, which is exactly the person the
     * feature exists for. Absence is therefore a third state that the *storage* carries
     * and the *interface* does not: `resolveCalmMode` collapses it to a two-state
     * switch before anything renders, because design-v3.md 16.1 rejects a three-state
     * control on the screen.
     *
     * There is deliberately no way to clear it back to following the system. Once the
     * user has expressed a preference, the app keeps it: a control that can silently
     * revert to tracking something else is a control that changes when you did not
     * touch it. Erasing all data clears it with everything else.
     *
     * **This is correctly a device preference and not engine state.** No corpus line,
     * no observation and no engine layer reads it, so two devices holding the same log
     * still compute the same sentence while rendering it at different saturations,
     * which is what it means for a setting to be about a screen rather than about a
     * person. `CLAUDE.md` rule 6.
     *
     * Widgets, design-v3.md 16.3, do not read this key from the widget process.
     * DataStore is not multi process safe, so calm mode travels to a widget in the
     * widget snapshot like every other value a widget reads. Phase 12, issue #11.
     */
    val calmMode: Flow<Boolean?> = context.store.data.map { it[Keys.calmMode] }

    /**
     * The in app text size, design-v3.md 13 and Addendum 01 8f.
     *
     * **It multiplies the OS font scale rather than replacing it**, and the combined
     * result is held at or below 200 percent. `ui.theme.ClarityTextSize` carries the
     * reasoning for both halves and design-v3.md 13 records the decision.
     *
     * **Not null the way `calmMode` is null, and the difference is the point.** Calm
     * mode's absent state means "follow the system", because its specified default is a
     * system setting rather than a value. This setting already follows the system, at
     * every step including [ClarityTextSize.DEFAULT], because it is a multiplier on what
     * the phone asked for. So absence has nothing left to mean and `DEFAULT` is a real
     * default rather than a placeholder: it says "whatever my phone says", which is
     * exactly what a person who has never opened this row wants.
     *
     * **A device preference and not engine state**, by the test `CLAUDE.md` rule 6 is
     * really applying: no corpus line, no observation and no engine layer reads it, so
     * two devices holding the same log compute the same sentence and render it at
     * different sizes. It is a fact about a screen, like `theme` and `calmMode`.
     * MASTER_BUILD_PROMPT 5.4 and 14b.12.
     *
     * An unrecognized stored name resolves to `DEFAULT` rather than throwing, which is
     * what a downgrade to a build with fewer steps looks like from here.
     */
    val textSize: Flow<ClarityTextSize> = context.store.data.map { prefs ->
        prefs[Keys.textSize]?.let { name ->
            ClarityTextSize.entries.firstOrNull { it.name == name }
        } ?: ClarityTextSize.DEFAULT
    }

    val focusDurationMinutes: Flow<Int> =
        context.store.data.map { it[Keys.focusDurationMinutes] ?: DEFAULT_FOCUS_MINUTES }

    val focusHighlightEnabled: Flow<Boolean> =
        context.store.data.map { it[Keys.focusHighlightEnabled] ?: true }

    /** The running session this device owns, or null. See [FocusHandle]. */
    val focusHandle: Flow<FocusHandle?> = context.store.data.map { prefs ->
        val sessionId = prefs[Keys.focusSessionId]
        val endsAt = prefs[Keys.focusSessionEndsAt]
        if (sessionId != null && endsAt != null) FocusHandle(sessionId, endsAt) else null
    }

    /**
     * The optional five minute transition warning. Addendum 01 4g, design-v3.md 10.18.
     *
     * **Off by default, and that is the specified default rather than a placeholder.**
     * Switching from one task to another is the expensive act for this audience, so a
     * warning is the difference between a transition and an interruption; an
     * unannounced signal is also an interruption, which is why it is opt in. The
     * Settings row that turns it on is phase 11.
     */
    val transitionWarningEnabled: Flow<Boolean> =
        context.store.data.map { it[Keys.transitionWarningEnabled] ?: false }

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

    /**
     * Whether the one time line above the first real Pulse has been shown.
     *
     * **Local, and rule 6 in `CLAUDE.md` is worth checking this against**, for the reason
     * [reEntrySettledOn] gives at greater length: this is a fact about one install rather
     * than engine state, and two phones should disagree about it, because reading an
     * explanation on one of them is not reading it on the other.
     *
     * It is set when the person answers, not when the line is drawn. Somebody who opens
     * the Pulse, reads it and leaves without answering has not finished reading it.
     */
    val hasSeenPulseIntro: Flow<Boolean> =
        context.store.data.map { it[Keys.hasSeenPulseIntro] ?: false }

    /**
     * The return this device has already offered the re-entry state for, as the date
     * key of that return. Null until one has been answered. MASTER_BUILD_PROMPT 14b.4.
     *
     * **A date key rather than a boolean, and it is the date of the return rather than
     * anything about the absence.** A boolean would have to be cleared by somebody, and
     * the day it was not cleared is the day the screen never appears again. Holding the
     * return's own date means the next gap has a different value and offers the screen
     * again with nothing to reset, which is the same shape `ExternalRequest`'s serial
     * uses for the same reason. `ReEntry` hands out exactly this value and no other, per
     * DECISIONS.md, so nothing here can be subtracted from anything.
     *
     * **Local rather than derived from the log, which rule 6 in CLAUDE.md is worth
     * checking this against.** That rule keeps engine state out of DataStore because two
     * devices holding one log must not disagree about what the engine has said. This is
     * not engine state and the two devices should disagree: each one is a phone somebody
     * picked up after a fortnight, and answering the question on one of them is not an
     * answer given on the other. It sits beside [hasSeenTutorial], which is the same kind
     * of fact about this install.
     */
    val reEntrySettledOn: Flow<String?> = context.store.data.map { it[Keys.reEntrySettledOn] }

    val lastExportAt: Flow<Long?> = context.store.data.map { it[Keys.lastExportAt] }

    suspend fun setTheme(value: ClarityThemeSetting) = put(Keys.theme, value.name)

    /** The first call stops calm mode following the system setting, per design-v3.md 16.1. */
    suspend fun setCalmMode(value: Boolean) = put(Keys.calmMode, value)
    suspend fun setTextSize(value: ClarityTextSize) = put(Keys.textSize, value.name)
    suspend fun setFocusDurationMinutes(value: Int) = put(Keys.focusDurationMinutes, value)
    suspend fun setFocusHighlightEnabled(value: Boolean) = put(Keys.focusHighlightEnabled, value)

    /**
     * Records that this device is running [sessionId] until [endsAtMillis]. Both
     * halves in one transaction, so no reader sees half a handle.
     */
    suspend fun setFocusHandle(sessionId: String, endsAtMillis: Long) {
        context.store.edit { prefs ->
            prefs[Keys.focusSessionId] = sessionId
            prefs[Keys.focusSessionEndsAt] = endsAtMillis
        }
    }

    /** Cleared when a session ends, by any of the ways a session can end. */
    suspend fun clearFocusHandle() {
        context.store.edit { prefs ->
            prefs.remove(Keys.focusSessionId)
            prefs.remove(Keys.focusSessionEndsAt)
        }
    }

    suspend fun setTransitionWarningEnabled(value: Boolean) =
        put(Keys.transitionWarningEnabled, value)
    suspend fun setAfterCompleting(value: AfterCompleting) = put(Keys.afterCompleting, value.name)
    suspend fun setPulseRemindersEnabled(value: Boolean) = put(Keys.pulseRemindersEnabled, value)
    suspend fun setPulseReminderHour(value: Int) = put(Keys.pulseReminderHour, value)
    suspend fun setHasCompletedOnboarding(value: Boolean) = put(Keys.hasCompletedOnboarding, value)
    suspend fun setHasSeenTutorial(value: Boolean) = put(Keys.hasSeenTutorial, value)

    suspend fun setHasSeenPulseIntro(value: Boolean) = put(Keys.hasSeenPulseIntro, value)
    suspend fun setReEntrySettledOn(returnedOn: String) = put(Keys.reEntrySettledOn, returnedOn)
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
