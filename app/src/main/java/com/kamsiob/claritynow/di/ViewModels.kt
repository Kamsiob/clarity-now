package com.kamsiob.claritynow.di

import android.app.Application
import android.content.res.AssetManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.kamsiob.claritynow.ClarityApp
import com.kamsiob.claritynow.domain.engine.catalog.CorpusVolume
import com.kamsiob.claritynow.domain.momentum.MomentumCoordinator
import com.kamsiob.claritynow.domain.pulse.CorpusSource
import com.kamsiob.claritynow.domain.pulse.CorpusText
import com.kamsiob.claritynow.domain.pulse.PulseCoordinator
import com.kamsiob.claritynow.ui.areas.AreasViewModel
import com.kamsiob.claritynow.ui.focus.FocusViewModel
import com.kamsiob.claritynow.ui.momentum.AreasBannerViewModel
import com.kamsiob.claritynow.ui.momentum.MomentumViewModel
import com.kamsiob.claritynow.ui.pulse.PulseViewModel
import com.kamsiob.claritynow.ui.report.ReportCoordinator
import com.kamsiob.claritynow.ui.report.ReportViewModel
import com.kamsiob.claritynow.ui.trail.TrailViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * The hand written equivalent of an injected ViewModel factory.
 *
 * One `when` over the requested class, resolved from [ClarityGraph]. It is longer
 * than an annotation but it is readable end to end, which is the trade this project
 * chose deliberately in MASTER_BUILD_PROMPT section 3.
 */
object ClarityViewModelFactory : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T =
        when (modelClass) {
            AreasViewModel::class.java -> AreasViewModel(
                repository = ClarityGraph.repository,
                preferences = ClarityGraph.preferences,
                clock = ClarityGraph.clock,
            ) as T

            AreasBannerViewModel::class.java -> AreasBannerViewModel(
                coordinator = momentumCoordinator(extras),
                clock = ClarityGraph.clock,
            ) as T

            FocusViewModel::class.java -> FocusViewModel(
                repository = ClarityGraph.repository,
                preferences = ClarityGraph.preferences,
            ) as T

            MomentumViewModel::class.java -> MomentumViewModel(
                repository = ClarityGraph.repository,
                coordinator = momentumCoordinator(extras),
            ) as T

            PulseViewModel::class.java -> PulseViewModel(
                repository = ClarityGraph.repository,
                coordinator = pulseCoordinator(extras),
                clock = ClarityGraph.clock,
            ) as T

            ReportViewModel::class.java -> ReportViewModel(
                coordinator = reportCoordinator(extras),
            ) as T

            TrailViewModel::class.java -> TrailViewModel(
                repository = ClarityGraph.repository,
                clock = ClarityGraph.clock,
            ) as T

            else -> error("no ViewModel registered for ${modelClass.name}")
        }

    /**
     * The one Pulse lifecycle for the process, reached through the Application.
     *
     * **It belongs in [ClarityGraph] beside the repository and the clock**, and it is on
     * `ClarityApp` because the slice that built the lifecycle could not edit that file.
     * The catalog inside it parses three markdown files and runs its integrity checks, so
     * there must be exactly one of them for the process, per MASTER_BUILD_PROMPT 11.7:
     * building a second here would be a second catalog and a second parse on the first
     * open of the sheet. When the binding moves, this function becomes
     * `ClarityGraph.pulse` and the [CreationExtras] parameter goes with it.
     *
     * The Application arrives in the creation extras, which a `ComponentActivity`
     * populates and a bare `ViewModelStoreOwner` does not. That is why `PulseRoute`
     * deliberately takes the ordinary store rather than one of its own, and why the error
     * below says so rather than reading `no ViewModel registered`.
     */
    private fun pulseCoordinator(extras: CreationExtras): PulseCoordinator =
        applicationFrom(extras, "Pulse").pulse

    /**
     * The one Momentum lifecycle for the process, held by [MomentumGraph].
     *
     * **This is the second catalog in the process and that is recorded rather than
     * hidden.** [MomentumCoordinator]'s own documentation has the argument; the short form
     * is that MASTER_BUILD_PROMPT 11.7 wants one catalog for the process, `PulseCoordinator`
     * holds one and hands it to nobody, and the file where the two would meet,
     * [ClarityGraph], was outside the Momentum phase's file list. The fix is one lazy
     * binding there taken by both coordinators as a parameter, at which point [MomentumGraph]
     * and [AssetCorpus] below both go away.
     */
    private fun momentumCoordinator(extras: CreationExtras): MomentumCoordinator =
        MomentumGraph.of(applicationFrom(extras, "Momentum"))

    /**
     * The one Report lifecycle for the process, held by [ReportGraph].
     *
     * **This is the third catalog in the process and it is recorded rather than hidden**,
     * for exactly the reason [momentumCoordinator] states about the second: the right
     * arrangement is one lazy binding in [ClarityGraph] taken by all three coordinators as
     * a parameter, and [ClarityGraph] has been outside every surface phase's file list.
     * The cost is one extra parse of three markdown files, on a background dispatcher, the
     * first time the Report tab is opened. The fix is one binding there and one constructor
     * parameter here, at which point [MomentumGraph], [ReportGraph] and [AssetCorpus] all
     * go away together.
     */
    private fun reportCoordinator(extras: CreationExtras): ReportCoordinator =
        ReportGraph.of(applicationFrom(extras, "Report"))

    /**
     * The `ClarityApp` a ViewModel is being built under.
     *
     * A `ComponentActivity` populates the application key in its creation extras and a bare
     * `ViewModelStoreOwner` does not, which is why every route that needs one deliberately
     * takes the ordinary store. The message says that rather than reading `no ViewModel
     * registered`, because the two failures look identical from a stack trace and have
     * completely different fixes.
     */
    private fun applicationFrom(extras: CreationExtras, surface: String): ClarityApp {
        val key = ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY
        val application: Application? = extras[key]
        return checkNotNull(application as? ClarityApp) {
            "the $surface ViewModel was built without the Application in its creation extras, " +
                "which means it was resolved against a ViewModelStoreOwner that supplies none"
        }
    }
}

/**
 * The process scoped [MomentumCoordinator], created once.
 *
 * Both Momentum surfaces resolve through here, so the Momentum tab and the Areas banner
 * share one catalog and one cache of the corpus rather than parsing it twice between them.
 *
 * Synchronized rather than `by lazy`, because the instance depends on the Application and
 * a lazy delegate takes no argument. The lock is contended exactly once per process.
 */
private object MomentumGraph {

    private var coordinator: MomentumCoordinator? = null

    @Synchronized
    fun of(app: ClarityApp): MomentumCoordinator = coordinator ?: MomentumCoordinator(
        repository = ClarityGraph.repository,
        clock = ClarityGraph.clock,
        corpus = AssetCorpus(app.assets),
    ).also { coordinator = it }
}

/**
 * The process scoped [ReportCoordinator], created once.
 *
 * One instance rather than one per entry into the tab, so the catalog is parsed once for
 * the process rather than on every visit. It holds no per screen state: the reveal's
 * memory lives in `ReportViewModel`, which is scoped to the Activity's store on purpose.
 *
 * Synchronized rather than `by lazy` for the reason [MomentumGraph] gives: the instance
 * depends on the Application and a lazy delegate takes no argument. The lock is contended
 * exactly once per process.
 */
private object ReportGraph {

    private var coordinator: ReportCoordinator? = null

    @Synchronized
    fun of(app: ClarityApp): ReportCoordinator = coordinator ?: ReportCoordinator(
        repository = ClarityGraph.repository,
        clock = ClarityGraph.clock,
        corpus = AssetCorpus(app.assets),
    ).also { coordinator = it }
}

/**
 * The three corpus files, read out of the packaged assets.
 *
 * **A second copy of `ClarityApp`'s own private reader, and it reads the same three
 * assets.** It is not a second copy of the corpus, which is the thing that must never
 * happen: the build copies the three committed markdown files into `assets/corpus/`, both
 * readers open those, and the file an author edits is still the file the app reads. What is
 * duplicated is eight lines of `AssetManager` plumbing, because `ClarityApp` is outside the
 * Momentum phase's file list and its reader is private. It goes away with [MomentumGraph]
 * when the catalog moves into [ClarityGraph].
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
