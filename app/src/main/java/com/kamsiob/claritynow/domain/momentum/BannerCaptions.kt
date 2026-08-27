package com.kamsiob.claritynow.domain.momentum

import com.kamsiob.claritynow.domain.engine.FactSet
import com.kamsiob.claritynow.domain.engine.FiringHistory
import com.kamsiob.claritynow.domain.engine.catalog.ClarityCatalog
import com.kamsiob.claritynow.domain.engine.catalog.CorpusLine
import com.kamsiob.claritynow.domain.engine.catalog.Purpose
import com.kamsiob.claritynow.domain.engine.catalog.SlotKey
import com.kamsiob.claritynow.domain.engine.realize.Slot
import com.kamsiob.claritynow.domain.engine.realize.SlotRenderer
import com.kamsiob.claritynow.domain.engine.realize.VariantChoice

/**
 * The banner's caption bench. `CORPUS_3_MOMENTUM.md`, "Banner captions", and design-v3.md
 * 10.2.
 *
 * The banner is two parts. The sentence is a family with rules behind it and comes through
 * the engine like every other sentence in the app. The caption is a bench of ten lines
 * that belongs to no family, is "selected independently of the sentence and combined with
 * it", and carries no register and no tone: the corpus calls it arithmetic.
 *
 * ## This file holds a slot binding table, and that table belongs in `SlotBindings`
 *
 * **Recorded rather than smuggled in.** Layer 4 authors every slot binding in
 * `domain.engine.realize.SlotBindings`, keyed by purpose, family, stage and variant, and
 * says on itself why the mapping has to be authored rather than derived: `{n}` means
 * whatever its own sentence means, and no check can read English well enough to work it
 * out. The caption bench has slots and is not a family, so `bindingsFor` has nowhere to
 * look it up and phase 5 left it unbound. The correct fix is an entry in that file and it
 * is one this slice could not make: `domain/engine/` is outside the file list this phase
 * was given. So the table is here, in the same shape, with the corpus line quoted beside
 * every entry, and `BannerCaptionsTest` fails the build if a caption line in the real
 * corpus has no binding or cannot be rendered. **When the table moves, this file goes with
 * it and nothing else changes**, because everything else here is the engine's own:
 * `VariantChoice` picks the line, `SlotRenderer` fills it, and `Slot` carries the values.
 *
 * ## Why a line can be refused, and why that is the safe direction
 *
 * Three guards, in the order they are applied.
 *
 * **Zero never renders through a count slot.** `CLARITY_LOGIC_ENGINE.md` 7.2 forbids it
 * and validator check 4 is the backstop, and `CORPUS_3_MOMENTUM.md` says `bnc.04` and
 * `bnc.10` exist precisely so that the zero case has authored lines rather than a template
 * producing `0 completed`. A line whose binding yields nought is dropped and one of those
 * two is what is left.
 *
 * **A line that would claim nothing was completed is unavailable in a week where
 * something was.** `bnc.04` and `bnc.10` are the only lines with a claim rather than a
 * number in them, and both are false the moment the week has a completion.
 *
 * **A line naming a count of areas is unavailable to somebody with one area.** `one of one
 * area active` is grammatical and it is not arithmetic anybody wants to read.
 *
 * If every line is refused the banner shows its sentence and no caption, which is 10.2's
 * two part shape with one part honestly missing rather than one part quietly wrong.
 */
internal object BannerCaptions {

    /** The bench key the walker files these under. `CORPUS_3_MOMENTUM.md` keys them `bnc.NN`. */
    const val BENCH: String = "bnc"

    /**
     * One caption line's bindings, plus the condition under which the line may be shown at
     * all.
     *
     * [slots] is read against the fact set the banner sentence was selected from, so the
     * caption and the sentence describe the same week by construction rather than by two
     * callers agreeing to pass the same window.
     */
    private class CaptionBinding(
        val line: String,
        val available: (FactSet) -> Boolean,
        val slots: (FactSet) -> Map<SlotKey, Slot>,
    )

    private fun binding(
        line: String,
        available: (FactSet) -> Boolean = { true },
        slots: (FactSet) -> Map<SlotKey, Slot> = { emptyMap() },
    ) = CaptionBinding(line, available, slots)

    /** Items, the noun every count of things in this bench governs. */
    private fun items(key: SlotKey, value: Int) = Slot.Count(key, value, "item", "items")

    private fun areas(key: SlotKey, value: Int) = Slot.Count(key, value, "area", "areas")

    private fun minutes(key: SlotKey, value: Int) = Slot.Count(key, value, "minute", "minutes")

    private fun sessions(key: SlotKey, value: Int) = Slot.Count(key, value, "session", "sessions")

    /** Everything waiting behind an active item, across every live area. */
    private fun waiting(facts: FactSet): Int = facts.areas.values.sumOf { it.queueLength }

    /** More than one area to compare, so `{m} of {areaCount}` is a comparison. */
    private fun manyAreas(facts: FactSet): Boolean = facts.rollup.areasTotal >= 2

    private fun nothingCompleted(facts: FactSet): Boolean = facts.window.completions == 0

    /**
     * The table. One entry per line in the bench, with the line quoted so an entry can be
     * read against the corpus without opening it.
     *
     * `bnc.07` requires two or more sessions rather than one, and the reason is grammar
     * rather than taste: the line reads `{sessions} focus sessions`, the word after the
     * marker is `focus` rather than the noun the count governs, and `SlotRenderer` can only
     * make a count of one agree with the word immediately after it. At one session the line
     * would render `one focus sessions`, and 7.2's answer to a line that cannot be made to
     * agree is to drop it and take another from the bench.
     */
    private val TABLE: Map<String, CaptionBinding> = mapOf(
        "bnc.01" to binding(
            line = "{n} completed, {m} of {areaCount} areas active",
            available = ::manyAreas,
            slots = { facts ->
                mapOf(
                    "n" to items("n", facts.window.completions),
                    "m" to areas("m", facts.rollup.areasWithEvents),
                    "areaCount" to areas("areaCount", facts.rollup.areasTotal),
                )
            },
        ),
        "bnc.02" to binding(
            line = "{n} completed this week",
            slots = { facts -> mapOf("n" to items("n", facts.window.completions)) },
        ),
        "bnc.03" to binding(
            line = "{n} completed, {m} added",
            slots = { facts ->
                mapOf(
                    "n" to items("n", facts.window.completions),
                    "m" to items("m", facts.window.additions),
                )
            },
        ),
        "bnc.04" to binding(
            line = "No items completed yet, {m} of {areaCount} areas active",
            available = { facts -> nothingCompleted(facts) && manyAreas(facts) },
            slots = { facts ->
                mapOf(
                    "m" to areas("m", facts.rollup.areasWithEvents),
                    "areaCount" to areas("areaCount", facts.rollup.areasTotal),
                )
            },
        ),
        "bnc.05" to binding(
            line = "{m} of {areaCount} areas active",
            available = ::manyAreas,
            slots = { facts ->
                mapOf(
                    "m" to areas("m", facts.rollup.areasWithEvents),
                    "areaCount" to areas("areaCount", facts.rollup.areasTotal),
                )
            },
        ),
        "bnc.06" to binding(
            line = "{n} completed, {minutes} minutes focused",
            slots = { facts ->
                mapOf(
                    "n" to items("n", facts.window.completions),
                    "minutes" to minutes("minutes", facts.window.focusMinutesTotal),
                )
            },
        ),
        "bnc.07" to binding(
            line = "{n} completed, {sessions} focus sessions",
            available = { facts -> facts.window.focusStarted >= 2 },
            slots = { facts ->
                mapOf(
                    "n" to items("n", facts.window.completions),
                    "sessions" to sessions("sessions", facts.window.focusStarted),
                )
            },
        ),
        "bnc.08" to binding(
            line = "{n} waiting across {areaCount} areas",
            available = ::manyAreas,
            slots = { facts ->
                mapOf(
                    "n" to items("n", waiting(facts)),
                    "areaCount" to areas("areaCount", facts.rollup.areasTotal),
                )
            },
        ),
        "bnc.09" to binding(
            line = "{n} completed, {m} waiting",
            slots = { facts ->
                mapOf(
                    "n" to items("n", facts.window.completions),
                    "m" to items("m", waiting(facts)),
                )
            },
        ),
        "bnc.10" to binding(
            line = "Nothing completed yet this week",
            available = ::nothingCompleted,
        ),
    )

    /**
     * Every key this table binds, against the line each entry was written for.
     *
     * The quoted line is not decoration and is not documentation. `BannerCaptionsTest`
     * compares it against the text the parser read out of `CORPUS_3_MOMENTUM.md`, so an
     * author who edits a caption's wording without looking at its bindings fails the
     * build rather than shipping a number attached to a different sentence. That check is
     * the reason a binding table living outside `SlotBindings` is survivable at all.
     */
    val quotedLines: Map<String, String> get() = TABLE.mapValues { (_, binding) -> binding.line }

    /**
     * The caption for one week, or null when no line in the bench can be filled truthfully.
     *
     * [dateKey] salts the choice, so the caption is stable for a local day and reads as a
     * shuffle across days, which is `CLARITY_LOGIC_ENGINE.md` 7.6 and the same rule the
     * headline is chosen by.
     *
     * [history] is passed through to `VariantChoice`, which excludes a line used inside
     * ninety days. Nothing has ever recorded a caption key in the log, so that exclusion
     * set is empty today whatever is passed; it is threaded rather than replaced with
     * `FiringHistory.EMPTY` so that the day a banner event exists, this starts honoring it
     * without an edit.
     */
    fun render(
        catalog: ClarityCatalog,
        facts: FactSet,
        dateKey: String,
        history: FiringHistory,
    ): String? {
        val bench = catalog.auxiliary[BENCH].orEmpty().filter { renderable(it, facts) != null }
        val chosen = VariantChoice.choose(bench, dateKey, history) { it.key } ?: return null
        return renderable(chosen.value, facts)
    }

    /**
     * [line] filled from [facts], or null when it may not be shown.
     *
     * Null covers all four refusals in one place: no binding for the key, the line's own
     * availability condition failing, a count slot at nought, and `SlotRenderer` declining
     * to render. Every one of them is ordinary and every one leaves the bench a line
     * shorter rather than putting something wrong on a screen.
     */
    fun renderable(line: CorpusLine, facts: FactSet): String? {
        val binding = TABLE[line.key] ?: return null
        if (!binding.available(facts)) return null
        val slots = binding.slots(facts)
        // 7.2, and validator check 4. A count that would render as nought takes the line
        // out of the bench, which is why `bnc.04` and `bnc.10` were authored.
        if (slots.values.any { (it.numericValue ?: 1) < 1 }) return null
        return SlotRenderer.render(line.text, slots, Purpose.AREAS_BANNER)
    }
}
