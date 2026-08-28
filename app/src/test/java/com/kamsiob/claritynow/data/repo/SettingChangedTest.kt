package com.kamsiob.claritynow.data.repo

import com.kamsiob.claritynow.data.event.ClarityEvent
import com.kamsiob.claritynow.data.event.ClarityEventType
import com.kamsiob.claritynow.data.event.SettingChanged
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * `SETTING_CHANGED` had every part except the one that writes it, for four phases.
 * MASTER_BUILD_PROMPT 14.1, and rule 9: the event log is the truth.
 *
 * The type was in the catalog, the reducer folded it, the Trail rendered a row for it and
 * `TrailFacts` counted it as something a person did. What was missing was a method on the
 * only writer in the app, because `commit` is private and phase 11 correctly refused to
 * reach around it. **A gap of that shape is invisible from either end**: the screen looks
 * complete, the Trail looks complete, and the row simply never appears.
 *
 * These read the source rather than driving a database, for the same reason
 * `EraseContractTest` does: what is being asserted is that one particular call exists at
 * one particular place, and a test that exercised the behavior would pass just as happily
 * against a second write path added later.
 */
class SettingChangedTest {

    @Test
    fun `the only writer in the app has a method that writes a setting change`() {
        val source = read("main/java/com/kamsiob/claritynow/data/repo/ClarityRepository.kt")
        assertTrue(
            "ClarityRepository.recordSettingChanged is the seam phase 11 reported missing, " +
                "and without it the SETTING_CHANGED row cannot appear in the Trail at all",
            source.contains("suspend fun recordSettingChanged("),
        )
        assertTrue(
            "it writes the payload through commitLocked like every other event, because an " +
                "event written down a second path is worse than an event that is missing",
            source.contains("commitLocked(") && source.contains("SettingChanged("),
        )
    }

    @Test
    fun `a setting change that changes nothing writes nothing`() {
        val source = read("main/java/com/kamsiob/claritynow/data/repo/ClarityRepository.kt")
        val body = source.substringAfter("suspend fun recordSettingChanged(")
        assertTrue(
            "a settings screen can hand back the value it is already holding, and a row " +
                "saying somebody changed a setting to what it already was is a small lie " +
                "in the file the desktop app is built against",
            body.substringBefore("mutex.withLock").contains("if (previousValue == newValue) return"),
        )
    }

    @Test
    fun `After completing is the preference that writes, and it passes the previous value`() {
        val source = read("main/java/com/kamsiob/claritynow/ui/settings/SettingsViewModel.kt")
        val method = source.substringAfter("fun setAfterCompleting(").substringBefore("\n    fun ")
        assertTrue(
            "After completing changes what completing an item does, so a Trail that does " +
                "not record the change cannot explain why two completions behaved differently",
            method.contains("recordSettingChanged("),
        )
        assertTrue(
            "the previous value is read before the write, because SettingChanged carries " +
                "both and a reducer that looked up what a setting used to be would be " +
                "reading state to describe a change to state",
            method.contains("val previous = state.value.afterCompleting"),
        )
    }

    @Test
    fun `no other preference writes to the log`() {
        val source = read("main/java/com/kamsiob/claritynow/ui/settings/SettingsViewModel.kt")
        assertEquals(
            "a text size, a theme and a reminder hour are facts about one install rather " +
                "than about a life, they already live in DataStore where a second device " +
                "is supposed to disagree with them, and a row in the Trail every time " +
                "somebody drags a slider is not what rule 9 asks for",
            1,
            Regex("recordSettingChanged\\(").findAll(source).count(),
        )
    }

    @Test
    fun `the payload carries both values, so the row can say what changed to what`() {
        val payload = SettingChanged(key = "afterCompleting", previousValue = "A", newValue = "B")
        assertEquals(ClarityEventType.SETTING_CHANGED, ClarityEvent.typeOf(payload))
        assertEquals("afterCompleting", payload.primaryEntityId)
    }

    private fun read(path: String): String {
        val file = File("src/$path")
        assertTrue("expected to find $path", file.exists())
        return file.readText()
    }
}
