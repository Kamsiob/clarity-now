package com.kamsiob.claritynow.ui.nav

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kamsiob.claritynow.R
import com.kamsiob.claritynow.di.ClarityViewModelFactory
import com.kamsiob.claritynow.ui.areas.AreasRoute
import com.kamsiob.claritynow.ui.areas.AreasViewModel
import com.kamsiob.claritynow.ui.components.ClarityTabBar
import com.kamsiob.claritynow.ui.components.TAB_AREAS
import com.kamsiob.claritynow.ui.components.TAB_MOMENTUM
import com.kamsiob.claritynow.ui.components.TAB_REPORT
import com.kamsiob.claritynow.ui.components.TAB_TRAIL
import com.kamsiob.claritynow.ui.components.rememberClarityTabs
import com.kamsiob.claritynow.ui.focus.FocusRoute
import com.kamsiob.claritynow.ui.momentum.MomentumRoute
import com.kamsiob.claritynow.ui.pulse.PulseRoute
import com.kamsiob.claritynow.ui.report.ReportRoute
import com.kamsiob.claritynow.ui.theme.LocalClarityColors
import com.kamsiob.claritynow.ui.theme.LocalClarityTypography
import com.kamsiob.claritynow.ui.theme.TabEntrance
import com.kamsiob.claritynow.ui.theme.clarityMotion
import com.kamsiob.claritynow.ui.trail.TrailRoute
import com.kamsiob.claritynow.ui.trail.TrailViewModel

/**
 * The app shell. design-v3.md 10.15.
 *
 * Four tabs at the root and nothing hidden: no drawer, no hamburger. Switching
 * tabs crossfades with no slide, because a slide would imply a spatial
 * relationship between them and there is none. These are four views of one set of
 * data rather than four places.
 *
 * **The crossfade is a transition and not an entrance**, which is why the once per
 * session rule in design-v3.md 8.4 leaves it alone. It fires on every tab switch, all
 * day, and should: it is how the app says the content underneath has been replaced.
 * What 8.4 removes is the staggered arrival of the rows inside the tab, and only after
 * the first open of that tab in this session.
 *
 * **The Focus surface is the one thing here that is not a tab.** design-v3.md section
 * 2 and 10.15: it is the Contemplative world, a room the app moves into rather than a
 * panel over this one, so it covers the tabs and the tab bar while it is showing.
 * Which of them is showing is [FocusEntry], and every rule about back living in a
 * value rather than in this function is deliberate: see that file.
 *
 * [focusRequest] is a counter that goes up each time something outside the composition
 * asks for the Focus surface, which today means a notification MainActivity received.
 * A counter rather than a boolean because a request is a moment and not a state: a
 * boolean would have to be cleared by whoever handled it, and the frame in which it
 * had not been cleared yet is the frame that re-opens a surface somebody just left.
 */
@Composable
fun ClarityShell(focusRequest: Long, modifier: Modifier = Modifier) {
    val colors = LocalClarityColors.current
    val motion = clarityMotion()
    var selected by rememberSaveable { mutableStateOf(TAB_AREAS) }

    // Hoisted out of the Areas tab, because two things need it. The tab draws from it,
    // and the shell reads the one fact about a running session that decides whether the
    // Focus surface should be showing. It is the same instance either way: the store
    // owner is the Activity, so this is where it is asked for rather than where it is
    // created, and the Areas tab keeps its state across a tab switch as it already did.
    val areasViewModel: AreasViewModel = viewModel(factory = ClarityViewModelFactory)
    val focusPresence by areasViewModel.focusPresence.collectAsStateWithLifecycle()

    // **Not rememberSaveable, and that is the choice rather than an oversight.**
    // MASTER_BUILD_PROMPT section 10 requires a session to survive process death and
    // the focus screen to be restored on the next launch, and this is the state that
    // would defeat it: a saved "they had walked away from this session" flag would
    // come back after a force stop and leave the person on the Areas screen wondering
    // where their session went. A configuration change this Activity does not declare
    // is the only other thing that resets it, and re-offering a running session there
    // is the same answer this gives on a cold start.
    var focusEntry by remember { mutableStateOf(FocusEntry()) }

    // The Pulse is a sheet rather than a destination, so it rides above whatever tab
    // is showing rather than replacing it. design-v3.md 10.15's destination table has
    // no Pulse row for the same reason: answering one is a moment, not a place.
    var pulseOpen by rememberSaveable { mutableStateOf(false) }

    // design-v3.md 8.2 entry 24 puts the tab crossfade at 180ms, one of the two places
    // in the document that names a duration rather than a spring. The nearest token,
    // motion.easeOut, is the 350ms entrance curve: nearly twice as long, and an
    // entrance is the wrong idea here, because these four tabs are one set of data
    // seen four ways rather than four places to arrive at. No 180ms token exists to
    // reach for, so the duration is written out, and the reduce motion branch has to
    // be written out with it: design-v3.md 8.3 replaces every animation with a 150ms
    // crossfade, and a literal duration is the one thing that global check cannot see.
    val tabFade: FiniteAnimationSpec<Float> = tween(
        durationMillis = if (motion.reduced) 150 else 180,
        easing = EaseOutCubic,
    )

    val tabs = rememberClarityTabs(
        areasLabel = stringResource(R.string.tab_areas),
        momentumLabel = stringResource(R.string.tab_momentum),
        reportLabel = stringResource(R.string.tab_report),
        trailLabel = stringResource(R.string.tab_trail),
    )

    // A notification, or an app shortcut later, asking for the session. It arrives as a
    // value rather than as a call because MainActivity has no composition to call into
    // when the intent lands during a cold start.
    LaunchedEffect(focusRequest) {
        if (focusRequest > 0L) focusEntry = focusEntry.requested()
    }

    // The session itself, which opens the surface on a relaunch and offers a finished
    // session its completion once. Keyed on the presence rather than on a tick, so this
    // runs twice in a session and not three thousand times.
    LaunchedEffect(focusPresence) {
        val presence = focusPresence
        focusEntry = focusEntry.sessionSeen(presence?.sessionId, presence?.timeIsUp == true)
    }

    // The system bars belong to whichever world is in front. design-v3.md section 2
    // and section 13.
    //
    // `enableEdgeToEdge` answers this once, at launch, from the phone's own dark mode
    // setting, and there are two ways that answer is wrong in this app. The theme
    // setting, 10.10, can put the Daylight world in dark while the phone is in light.
    // And the Focus surface is the indigo night whatever the theme says, so dark status
    // bar glyphs over it are a clock and a battery a person cannot read for the whole
    // of a session.
    SystemBarAppearance(darkContent = !focusEntry.open && !colors.isDark)

    // Back on a non root tab returns to Areas. Back on Areas leaves the app, with
    // no double tap prompt, which is a pattern that only exists where navigation
    // is confusing.
    //
    // Disabled while the Focus surface is showing. That surface registers its own
    // handler after this one and would win anyway; saying so here is what stops a
    // later edit to either file from quietly making back mean two things at once.
    BackHandler(enabled = !focusEntry.open && selected != TAB_AREAS) { selected = TAB_AREAS }

    // A tab's content leaves composition when another tab is selected, which throws
    // away everything it had remembered, scroll position first among them. The Trail
    // keeps its loaded pages in a ViewModel that outlives the switch, so without this
    // a person who paged a month back would return to the top of a list that still
    // holds the month. The holder gives each tab its own saveable slot, keyed by the
    // tab, so every screen comes back where it was left.
    //
    // design-v3.md 8.4 puts the once per session entrance flag in that same slot.
    // TabEntrance below reads and writes it, so the flag is per tab, survives a
    // rotation, is spent by the first open and is re-armed by a process death. Keying
    // it to the tab rather than to a screen is deliberate: a sheet opening over Areas
    // must not count as a first open.
    val tabStates = rememberSaveableStateHolder()

    Box(modifier = modifier.fillMaxSize().background(colors.canvas)) {
        AnimatedContent(
            targetState = selected,
            transitionSpec = { fadeIn(tabFade) togetherWith fadeOut(tabFade) },
            label = "tabContent",
        ) { tab ->
            tabStates.SaveableStateProvider(tab) {
                TabEntrance {
                    when (tab) {
                        TAB_AREAS -> AreasRoute(
                            viewModel = areasViewModel,
                            onOpenFocus = { focusEntry = focusEntry.requested() },
                            onOpenPulse = { pulseOpen = true },
                        )

                        TAB_TRAIL -> {
                            val trailViewModel: TrailViewModel =
                                viewModel(factory = ClarityViewModelFactory)
                            TrailRoute(viewModel = trailViewModel)
                        }

                        // Phase 7. The tab's own ViewModel is resolved inside the route,
                        // against the Activity's store, so this branch is the whole of the
                        // wiring. TabEntrance above is what makes the dot cascade and the
                        // number roll fire once per session, per design-v3.md 8.4.
                        TAB_MOMENTUM -> MomentumRoute()

                        // Phase 8. Like Momentum, the tab's own ViewModel is resolved
                        // inside the route against the Activity's store, so this branch is
                        // the whole of the wiring. **The Report reveal is deliberately not
                        // driven by TabEntrance above.** design-v3.md 8.4 makes it the one
                        // entrance that re-arms on a content change as well as on a session
                        // change, which needs a key TabEntrance has none of and says so in
                        // its own documentation, so ReportViewModel holds it instead.
                        TAB_REPORT -> ReportRoute()

                        else -> UnderConstruction()
                    }
                }
            }
        }

        if (pulseOpen) {
            PulseRoute(onDismiss = { pulseOpen = false })
        }

        ClarityTabBar(
            tabs = tabs,
            selectedKey = selected,
            onSelect = { selected = it },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 17.dp),
        )

        if (focusEntry.open) {
            // The tabs and the tab bar stay composed underneath, because design-v3.md
            // 8.2 item 6 has the Contemplative surface fade in over the room it is
            // replacing rather than over a bare canvas. Something has to stop a thumb
            // reaching them through it, and this is that: a full size sibling drawn
            // behind the surface and in front of everything else, which swallows every
            // pointer event the surface itself did not want. It is behind rather than
            // in front so that it can never starve the surface's own controls.
            Spacer(Modifier.fillMaxSize().swallowsPointerInput())

            // A ViewModel store of its own, cleared the moment the surface goes.
            //
            // **The Focus surface must be built fresh every time it is entered.** Its
            // ViewModel reads the log and resolves an outstanding session in `init`,
            // which is what puts a person back on the ring after a process death and on
            // the completion screen after a session ran out while they were away, and
            // it reaches a terminal state when they leave. Held in the Activity's store
            // it would be entered once and be finished forever afterwards. This is what
            // a navigation library gives a back stack entry, written out because this
            // app has one destination that needs it.
            val focusStore = remember { FocusSurfaceStore() }
            DisposableEffect(focusStore) {
                onDispose { focusStore.viewModelStore.clear() }
            }

            CompositionLocalProvider(LocalViewModelStoreOwner.provides(focusStore)) {
                FocusRoute(
                    // **This leaves the surface and it never ends a session.**
                    // design-v3.md 10.15. The session keeps running, the ongoing
                    // notification stays in the shade, and the Areas card keeps its
                    // countdown; all this records is that the person went elsewhere.
                    onExit = { focusEntry = focusEntry.left(focusPresence?.sessionId) },
                )
            }
        }
    }
}

/**
 * Asks the platform for dark or light system bar content, and nothing else.
 *
 * [darkContent] is true when the surface behind the bars is light, which is the way
 * round the platform names it: `isAppearanceLightStatusBars` means the bar sits on a
 * light background and its glyphs should therefore be dark.
 *
 * It draws nothing and takes no space, so it costs one effect that runs when the answer
 * changes, which is on a theme switch and on the two edges of a focus session.
 */
@Composable
private fun SystemBarAppearance(darkContent: Boolean) {
    val view = LocalView.current
    if (view.isInEditMode) return
    LaunchedEffect(view, darkContent) {
        val window = view.context.findActivity()?.window ?: return@LaunchedEffect
        WindowCompat.getInsetsController(window, view).apply {
            isAppearanceLightStatusBars = darkContent
            isAppearanceLightNavigationBars = darkContent
        }
    }
}

/**
 * The Activity a composition is running in, or null.
 *
 * Written as a loop rather than as a `tailrec` because a tail recursive extension that
 * recurses on a different receiver is the kind of thing the compiler is entitled to
 * decline to optimize, and a warning is a build failure here.
 */
private fun Context.findActivity(): Activity? {
    var context: Context? = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}

/**
 * A ViewModel store that lives exactly as long as the Focus surface is showing.
 *
 * It implements nothing else on purpose. `SavedStateRegistryOwner` and
 * `HasDefaultViewModelProviderFactory` are what a real back stack entry adds, and
 * neither is reachable from here: the only ViewModel built in this store is created by
 * `ClarityViewModelFactory`, which takes everything it needs from `ClarityGraph` and
 * has never used a `SavedStateHandle`.
 */
private class FocusSurfaceStore : ViewModelStoreOwner {
    override val viewModelStore: ViewModelStore = ViewModelStore()
}

/**
 * Consumes every pointer event that reaches this element, so nothing drawn beneath it
 * can be touched.
 *
 * Consuming in the initial pass is what makes it total: the initial pass runs from the
 * root down, so by the time a change has been consumed here, no node below this one in
 * the tree and no sibling behind it will act on it. That is also why an element wearing
 * this must never be an ancestor of something that needs the pointer, and why the one
 * use of it is a sibling drawn behind the surface it protects rather than a scrim over
 * it.
 */
private fun Modifier.swallowsPointerInput(): Modifier = pointerInput(Unit) {
    awaitPointerEventScope {
        while (true) {
            awaitPointerEvent(PointerEventPass.Initial).changes.forEach { it.consume() }
        }
    }
}

/**
 * A destination whose phase has not landed yet. Deliberately plain and honest
 * rather than a fake skeleton, because a convincing placeholder is a lie about
 * what has been built.
 */
@Composable
private fun UnderConstruction() {
    val colors = LocalClarityColors.current
    val type = LocalClarityTypography.current
    Box(
        modifier = Modifier.fillMaxSize().padding(40.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.under_construction),
            style = type.body,
            color = colors.inkTertiary,
            textAlign = TextAlign.Center,
        )
    }
}
