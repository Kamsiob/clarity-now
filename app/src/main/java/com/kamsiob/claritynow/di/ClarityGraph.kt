package com.kamsiob.claritynow.di

import android.content.Context
import android.content.res.AssetManager
import com.kamsiob.claritynow.data.db.ClarityDatabase
import com.kamsiob.claritynow.data.prefs.ClarityPreferences
import com.kamsiob.claritynow.data.repo.ClarityRepository
import com.kamsiob.claritynow.domain.ClarityClock
import com.kamsiob.claritynow.domain.SystemClarityClock
import com.kamsiob.claritynow.domain.corpus.CorpusSource
import com.kamsiob.claritynow.domain.corpus.CorpusText
import com.kamsiob.claritynow.domain.corpus.SharedCatalog
import com.kamsiob.claritynow.domain.engine.catalog.CorpusVolume
import com.kamsiob.claritynow.domain.momentum.MomentumCoordinator
import com.kamsiob.claritynow.domain.pulse.PulseCoordinator
import com.kamsiob.claritynow.R
import com.kamsiob.claritynow.ui.report.ReportCoordinator
import com.kamsiob.claritynow.ui.report.ReportSideheads
import com.kamsiob.claritynow.ui.theme.AndroidClarityHaptics
import com.kamsiob.claritynow.ui.theme.ClarityHaptics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * The dependency container, written by hand on purpose.
 *
 * MASTER_BUILD_PROMPT section 3: for a single module app, an annotation processor
 * based injector adds build time and failure modes that are hard to diagnose
 * without reading source, which is exactly the position the builder is in.
 *
 * Everything here is created lazily and lives for the process. Nothing in this
 * object holds an Activity or any other short lived Context.
 *
 * **It is the one file allowed to know both halves of the app.** `domain` is pure Kotlin
 * and must not import Android; `ui` is Android by definition. Every seam between the two is
 * declared in `domain` as an interface and given its platform half here, which is why
 * [AssetCorpus] is a private class at the foot of this file and not a shape any coordinator
 * can see.
 */
object ClarityGraph {

    private lateinit var appContext: Context

    fun install(context: Context) {
        appContext = context.applicationContext
    }

    val clock: ClarityClock by lazy { SystemClarityClock() }

    val database: ClarityDatabase by lazy { ClarityDatabase.build(appContext) }

    val preferences: ClarityPreferences by lazy { ClarityPreferences(appContext) }

    /** The only writer in the app. MASTER_BUILD_PROMPT 5.5. */
    val repository: ClarityRepository by lazy {
        ClarityRepository(db = database, prefs = preferences, clock = clock)
    }

    val haptics: ClarityHaptics by lazy { AndroidClarityHaptics(appContext) }

    /** The three corpus files, wherever this platform keeps them. */
    val corpus: CorpusSource by lazy { AssetCorpus(appContext.assets) }

    /**
     * **The one engine catalog for the process.** MASTER_BUILD_PROMPT 11.7, issue #55.
     *
     * The three surfaces that speak each held a catalog of their own until this binding
     * existed, because this file was outside every surface phase's file list and it is the
     * only place the three could have met. Each of them was right locally and the corpus was
     * parsed three times per process between them.
     *
     * `by lazy` is correct here and is not correct one level down: this only has to hand out
     * the same holder to three callers, which is not suspending work, and `SharedCatalog`
     * does the part that is.
     */
    val catalog: SharedCatalog by lazy { SharedCatalog(corpus) }

    /**
     * The Pulse lifecycle, built once for the process. MASTER_BUILD_PROMPT 11.3.
     *
     * `ClarityApp` calls `generateOnForeground` on the first foreground and `PulseViewModel`
     * reads the same instance, so the day's entry is generated once however many places ask.
     */
    val pulse: PulseCoordinator by lazy {
        PulseCoordinator(repository = repository, clock = clock, catalog = catalog)
    }

    /**
     * The Momentum lifecycle, built once for the process. MASTER_BUILD_PROMPT 11.3 and 12.2.
     *
     * The Momentum tab and the Areas banner are two surfaces over one coordinator, which is
     * what lets the once an hour banner throttle live in `AreasBannerViewModel` and be the
     * only rate limit in the path.
     */
    val momentum: MomentumCoordinator by lazy {
        MomentumCoordinator(repository = repository, clock = clock, catalog = catalog)
    }

    /**
     * The Report lifecycle, built once for the process. MASTER_BUILD_PROMPT 11.3 and 12.3.
     *
     * One instance rather than one per entry into the tab. It holds no per screen state: the
     * reveal's memory lives in `ReportViewModel`, which is scoped to the Activity's store on
     * purpose.
     */
    val report: ReportCoordinator by lazy {
        ReportCoordinator(
            repository = repository,
            clock = clock,
            catalog = catalog,
            // The Android half of the one seam this coordinator has. `REPORT_GENERATED`
            // stores the labels beside the sentences, so a report read back in a year
            // carries the labels it was written under. Resolved here rather than in the
            // coordinator, which holds no Context.
            sideheads = ReportSideheads(
                yourWeek = appContext.getString(R.string.report_sidehead_your_week),
                whatYouSaid = appContext.getString(R.string.report_sidehead_what_you_said),
                focus = appContext.getString(R.string.report_sidehead_focus),
                pattern = appContext.getString(R.string.report_sidehead_pattern),
            ),
        )
    }

    /** True once [install] has run. Guards against use from a ContentProvider that
     *  initializes before Application.onCreate. */
    val isInstalled: Boolean
        get() = ::appContext.isInitialized
}

/**
 * The three corpus files, read out of the packaged assets.
 *
 * They are assets rather than Kotlin constants because CLAUDE.md's authority order gives the
 * corpus the last word on the wording of every sentence, and a copy of a corpus embedded in
 * code is a second corpus that drifts. The build copies the three committed markdown files
 * into `assets/corpus/` so that the file an author edits is the file the app reads.
 *
 * A missing or malformed asset throws, and `SharedCatalog` turns that into a
 * `CatalogLoad.Failed` rather than into a crash. Every surface that speaks is entirely
 * usable without language: the Pulse reports it, and Momentum and the Report render every
 * number they counted from the log.
 *
 * **There was a copy of this in `ClarityApp` and another in `di/ViewModels.kt`**, because
 * each surface phase needed an asset reader and could not edit the file that already had
 * one. Both are gone. It was never a second copy of the corpus, which is the thing that must
 * not happen; it was the same three assets opened by two readers.
 */
private class AssetCorpus(private val assets: AssetManager) : CorpusSource {

    override suspend fun read(): CorpusText = withContext(Dispatchers.IO) {
        CorpusText(
            pulse = read(CorpusVolume.PULSE),
            report = read(CorpusVolume.REPORT),
            momentum = read(CorpusVolume.MOMENTUM),
        )
    }

    private fun read(volume: CorpusVolume): String =
        assets.open(CorpusSource.assetPathOf(volume)).bufferedReader().use { it.readText() }
}
