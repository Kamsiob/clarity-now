package com.kamsiob.claritynow.domain.corpus

import com.kamsiob.claritynow.domain.engine.catalog.CorpusVolume

/** The three corpus files, as text. Nothing else is ever read from them. */
data class CorpusText(val pulse: String, val report: String, val momentum: String)

/**
 * Where the three corpus files come from on this platform.
 *
 * A seam rather than a call, because `ClarityCatalog.build` takes text and the catalog
 * package deliberately opens no file: that is what lets phase 9 test a corpus edit
 * against the real rules from a string. On the phone the text is an asset, in a test it
 * is the committed file read off disk, and neither of those belongs in `domain`.
 *
 * ## It is in `domain.corpus` rather than beside the surface that first needed it
 *
 * It lived in `domain/pulse/PulseCoordinator.kt` while the Pulse was the only caller that
 * held a catalog. Momentum and the Report then imported it from there, which read as though
 * reaching the corpus were a Pulse idea two other surfaces had borrowed. It is nobody's idea
 * in particular: it is how this app gets at the corpus at all, and [SharedCatalog] beside it
 * is the one thing in the process that turns it into language.
 *
 * **The package is pure Kotlin and `DomainPurityTest` scans it**, which is what makes it
 * reachable from `domain.pulse`, from `domain.momentum` and from `ui.report` at once. The
 * platform half of the seam is a private class on `ClarityGraph`, which is the one file in
 * the app allowed to know both about Android and about every layer below it.
 */
fun interface CorpusSource {

    /** Reads all three volumes. Throws when one cannot be read; see [CatalogLoad.Failed]. */
    suspend fun read(): CorpusText

    companion object {

        /** The asset path a volume is packaged at. See `ClarityGraph` and the build file. */
        fun assetPathOf(volume: CorpusVolume): String = "corpus/${volume.fileName}"
    }
}
