package com.kamsiob.claritynow.ui.reentry

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.kamsiob.claritynow.data.prefs.ClarityPreferences
import com.kamsiob.claritynow.data.repo.ClarityRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Whether this open shows the re-entry state. MASTER_BUILD_PROMPT 14b.4.
 *
 * The whole rule as one expression, so that the three ways it can be wrong are three
 * lines a test can read rather than three branches inside a composable nothing on a
 * desktop JVM can run. `ReEntryRoutingTest` drives it.
 *
 * - **Not before onboarding is complete.** design-v3.md 10.15 puts this check after
 *   both first run flags "so that it can never delay or replace a first run", and
 *   14b.4 says the same. The query cannot know this and says so: the onboarding gate
 *   belongs to the surface.
 * - **Not when there is no return.** [returnedOn] is `TrailQueries.reEntryOn`, which is
 *   non null on the day of a return and null on every other day, including the day
 *   somebody opens this app for the first time. A first ever open has nothing before
 *   it and is not a return.
 * - **Not twice for the same gap.** [settledOn] is the return this device has already
 *   offered and had answered. The detection is true for exactly one calendar day, so
 *   this is what covers the process dying and being relaunched inside that day.
 *
 * Both dates are the date of a **return** and never the length of an absence, and this
 * function compares them rather than subtracting them. There is no arithmetic here and
 * there is nowhere for any to go.
 */
fun offersReEntry(
    onboardingComplete: Boolean,
    returnedOn: String?,
    settledOn: String?,
): Boolean = onboardingComplete && returnedOn != null && returnedOn != settledOn

/**
 * Asks the two questions the re-entry state needs and hands back the offer.
 *
 * Built in `MainActivity` out of the graph, and handed to the first run gate, which is
 * the one place in the app that decides what a cold start does.
 */
class ReEntryDecision(
    private val repository: ClarityRepository,
    private val preferences: ClarityPreferences,
) {

    /**
     * The offer this open carries, or null when there is nothing to offer.
     *
     * [onboardingComplete] is the latched flag the gate already read, passed in rather
     * than read again here, so the order design-v3.md 10.15 states is visible at the
     * call site: two flags, then this, last.
     *
     * It is a suspend function called once per cold start. What it costs is the log
     * load every other reader of the log is already waiting for, plus one bounded query
     * over the presence markers. See `ClarityRepository.reEntryOnThisOpen`.
     */
    suspend fun offerOnThisOpen(onboardingComplete: Boolean): ReEntryOffer? {
        if (!onboardingComplete) return null
        val returnedOn = repository.reEntryOnThisOpen()?.returnedOn
        val settledOn = preferences.reEntrySettledOn.first()
        if (!offersReEntry(onboardingComplete, returnedOn, settledOn)) return null
        return ReEntryOffer(checkNotNull(returnedOn), repository, preferences)
    }
}

/**
 * The two things the screen can do, with the date of the return closed over rather than
 * carried.
 *
 * **That is the point of this type existing at all.** The screen needs to record which
 * return it answered, and the only value that could record it is a date. A date passed
 * into a composable is one `Text` away from the sentence 14b.4 exists to prevent, and
 * the argument that nobody would render it is the argument every one of that section's
 * prohibitions declines to rest on. So the date stops here, one layer below the
 * composition, and `ReEntryScreen` takes two callbacks and no values at all.
 */
class ReEntryOffer internal constructor(
    private val returnedOn: String,
    private val repository: ClarityRepository,
    private val preferences: ClarityPreferences,
) {

    /** The default, and the one that costs nothing. Nothing is written to the log. */
    suspend fun keepEverything() {
        preferences.setReEntrySettledOn(returnedOn)
    }

    /**
     * Every active item back to the head of its own queue, then the offer is answered.
     *
     * **In that order, and it is the order that fails safely.** Settling first and
     * losing the process before the demotion would drop a choice somebody made and
     * never offer it again. This way round, the same interruption leaves the screen
     * still standing on the next launch that day, which is a repeated question rather
     * than a silent refusal.
     */
    suspend fun putItemsBack() {
        repository.putActiveItemsBackInTheirQueues()
        preferences.setReEntrySettledOn(returnedOn)
    }
}

/**
 * The re-entry state, wired to the one writer in the app.
 *
 * There is no back, per design-v3.md 10.15's destination table, and no `BackHandler` is
 * registered for it: back does what back does on the first screen of the app, which is
 * leave. That is onboarding beat 1's behavior for the same reason, and it is the honest
 * one here, because the offer has not been answered and is waiting on the next launch.
 * A handler that swallowed back would be a control that looks live and does nothing,
 * which is the defect issue #58 is about.
 *
 * [onSettled] runs after the write, so the app revealed underneath is already the app
 * the person chose. A second tap while the first is in flight is dropped rather than
 * queued, and the guard is in the callback rather than on the buttons: a control that
 * disables itself for the length of one transaction is a flicker, and neither role
 * changes appearance when it is disabled anyway, per 10.7.
 *
 * The write runs in the composition's own scope, which this screen leaving would
 * cancel. The only thing that takes this screen out of composition is [onSettled], and
 * that is the last statement of both branches, so there is no path where a choice is
 * canceled halfway. If one were ever added, the failure is the safe one described on
 * [ReEntryOffer.putItemsBack].
 */
@Composable
fun ReEntryRoute(
    offer: ReEntryOffer,
    onSettled: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    var settling by remember { mutableStateOf(false) }

    ReEntryScreen(
        onKeepEverything = {
            if (!settling) {
                settling = true
                scope.launch {
                    offer.keepEverything()
                    onSettled()
                }
            }
        },
        onPutItemsBack = {
            if (!settling) {
                settling = true
                scope.launch {
                    offer.putItemsBack()
                    onSettled()
                }
            }
        },
        modifier = modifier,
    )
}
