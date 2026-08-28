package com.kamsiob.claritynow.domain.engine.corpus

import com.kamsiob.claritynow.domain.engine.FamilyKey
import com.kamsiob.claritynow.domain.engine.catalog.ClarityCatalog
import com.kamsiob.claritynow.domain.engine.catalog.Purpose
import com.kamsiob.claritynow.domain.engine.catalog.Variant

/**
 * A bench, which is the unit every sizing and variety rule in 11.1 is stated over.
 *
 * **A bench is one family, at one purpose, at one stage, of one kind.** 11.1 sizes
 * "variants per stage bench" and 7.6 chooses a line from within one, so that is the
 * boundary a repeat is felt across and the boundary these gates measure over.
 *
 * [Kind] separates a Report family's leads from its extensions because they are two benches
 * that happen to sit under one heading: a lead opens an observation and an extension
 * continues it, the realizer draws from them at different moments, and merging them would
 * let a family with fifteen leads and one extension read as a bench of sixteen.
 */
internal data class Bench(
    val purpose: Purpose,
    val family: FamilyKey,
    val stage: Int,
    val kind: Kind,
    val lines: List<Variant>,
) {
    enum class Kind { STATEMENT, EXTENSION }

    /**
     * A stable name for the bench, used as the key of every recorded baseline entry.
     *
     * It has to survive a corpus edit that adds or removes lines, so it is built out of the
     * four things that identify the bench and nothing that counts what is in it.
     */
    val id: String = buildString {
        append(purpose.name)
        append(' ')
        append(family)
        append(" s")
        append(stage)
        if (kind == Kind.EXTENSION) append(" ext")
    }

    /** True when 11.1 sizes this bench at sixty to a hundred. */
    val isHot: Boolean get() = HotFamilies.isHot(purpose, family)

    val size: Int get() = lines.size
}

/** Every bench in a catalog, and the lines that are not on one. */
internal object CorpusBenches {

    /**
     * Every statement and extension bench, in a fixed order.
     *
     * Auxiliary benches are deliberately absent. The acknowledgments, the banner captions,
     * the basis lines and the edge states are not families, carry no register ladder and no
     * stage, and every rule these gates check is stated over a family bench. They are
     * checked for vocabulary, where the rule is about a rendered string rather than about a
     * bench, and `CorpusGates.bannedVocabulary` reads them from the catalog's auxiliary map
     * for exactly that reason.
     */
    fun of(catalog: ClarityCatalog): List<Bench> = catalog.families.flatMap { family ->
        family.stages.flatMap { stage ->
            listOfNotNull(
                stage.variants.takeIf { it.isNotEmpty() }?.let {
                    Bench(family.purpose, family.key, stage.index, Bench.Kind.STATEMENT, it)
                },
                stage.extensions.takeIf { it.isNotEmpty() }?.let {
                    Bench(family.purpose, family.key, stage.index, Bench.Kind.EXTENSION, it)
                },
            )
        }
    }
}
