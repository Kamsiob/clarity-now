package com.kamsiob.claritynow.data.export

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Erase all data, checked by reading the source. MASTER_BUILD_PROMPT 14.2 and 6.5.
 *
 * **This is a source scan for the same reason `DomainPurityTest` is: the thing that
 * has to be true cannot be reached from a unit test.** Erase spans Room and
 * DataStore, both of which need a device, and the replay half of the promise is
 * already proved where it can be, in `ReplayHarnessTest.erasing everything returns
 * a virgin state`, which folds an emptied log and asserts no personal record, no
 * spent first ever flag, no variation history and no plan history survives. What
 * that test cannot see is whether the code that empties things empties all of them.
 *
 * The failure this guards against is specific and quiet. `ClarityPreferences`
 * erases by calling `clear()` and then minting one fresh `originId`, so a key added
 * next year is erased without anybody remembering this method exists. Rewritten as
 * a list of `remove` calls, which is the shape somebody reaches for when they want
 * to keep one more key, it would erase everything on the list and quietly keep
 * whatever was not on it. Nothing on any screen would look wrong. The reset
 * virginity promise in 6.5 is the one promise the Report cannot survive breaking,
 * and it would be broken by an edit that reads like tidying.
 */
class EraseContractTest {

    private val preferences = File("src/main/java/com/kamsiob/claritynow/data/prefs/ClarityPreferences.kt")
    private val repository = File("src/main/java/com/kamsiob/claritynow/data/repo/ClarityRepository.kt")

    @Test
    fun `both files are where this test looks`() {
        assertTrue(
            "unit tests are expected to run from the app module directory, and this run " +
                "is in ${File("").absolutePath}",
            File("build.gradle.kts").isFile,
        )
        assertTrue("missing ${preferences.path}", preferences.isFile)
        assertTrue("missing ${repository.path}", repository.isFile)
    }

    @Test
    fun `erasing preferences clears every key and mints a new originId`() {
        val body = bodyOf(preferences, "suspend fun eraseEverything()")
        assertTrue(
            "eraseEverything has to call clear(), so that a key added later is erased " +
                "without anybody having to remember this method",
            body.contains("prefs.clear()"),
        )
        assertFalse(
            "eraseEverything may not name keys one at a time. A list is a list somebody " +
                "will add a key without, and MASTER_BUILD_PROMPT 14.2 says every key goes " +
                "except a freshly regenerated originId",
            body.contains(".remove("),
        )
        assertTrue(
            "the old originId is device identity for a log that no longer exists, so a " +
                "fresh one is minted rather than kept",
            body.contains("Keys.originId") && body.contains("UUID.randomUUID()"),
        )
    }

    @Test
    fun `erasing the repository takes the log the cache and every checkpoint`() {
        val body = bodyOf(repository, "suspend fun eraseEverything()")
        listOf(
            "cache.clearCache()",
            "cache.clearSnapshots()",
            "events.eraseEverything()",
            "prefs.eraseEverything()",
            "_state.value = ClarityState.EMPTY",
        ).forEach { required ->
            assertTrue(
                "MASTER_BUILD_PROMPT 14.2: the event log, every cache table, every " +
                    "checkpoint and every DataStore key. Missing $required",
                body.contains(required),
            )
        }
    }

    /** The text between the declaration's opening brace and its matching close. */
    private fun bodyOf(file: File, declaration: String): String {
        val text = file.readText()
        val at = text.indexOf(declaration)
        assertTrue("no $declaration in ${file.path}", at >= 0)
        var depth = 0
        var started = false
        val body = StringBuilder()
        for (index in at until text.length) {
            val character = text[index]
            if (character == '{') {
                depth++
                started = true
            }
            if (started) body.append(character)
            if (character == '}') {
                depth--
                if (depth == 0) break
            }
        }
        return body.toString()
    }
}
