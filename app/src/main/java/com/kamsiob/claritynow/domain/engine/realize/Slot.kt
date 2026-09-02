package com.kamsiob.claritynow.domain.engine.realize

import com.kamsiob.claritynow.domain.engine.catalog.Purpose
import com.kamsiob.claritynow.domain.engine.catalog.SlotKey

/**
 * The five things a template marker can be filled with. CLARITY_LOGIC_ENGINE.md 7.2.
 *
 * **A slot carries a value and not a string.** Every rule about how a number appears on a
 * screen is applied centrally by [SlotRenderer], never by the author of a line and never
 * at the point a fact is read. That is what makes the register difference in 7.2 possible
 * at all: the same [Count] of four renders as `four` in a Pulse and as `4` in the Report,
 * and neither the corpus nor the fact extractor knows anything about it.
 *
 * [Count] carries **both noun forms and has no default**, per 7.2 and the `Broken plural`
 * failure mode in 13. `1 items` is the kind of defect that survives every test a person
 * writes and is the first thing a reader notices.
 */
sealed interface Slot {

    val key: SlotKey

    /** A snapshot name. Never a live entity name; see [Realizer] for why that is structural. */
    data class Text(override val key: SlotKey, val value: String) : Slot

    /**
     * A count of things, with the noun in both forms.
     *
     * The forms are not decoration. Templates are authored with the noun in the line,
     * `{n} things`, so a value of one has to make the line agree, and the only way to do
     * that without a parts of speech library is to know what the noun was going to be.
     * [SlotRenderer] uses them for exactly that and for nothing else.
     */
    data class Count(
        override val key: SlotKey,
        val value: Int,
        val singular: String,
        val plural: String,
    ) : Slot

    /** A span of whole days, rendered by magnitude: `yesterday`, `nine days`, `three weeks`. */
    data class Days(override val key: SlotKey, val value: Int) : Slot

    /** A whole percentage. Renders as `78 percent`, never with a sign. */
    data class Percent(override val key: SlotKey, val value: Int) : Slot

    /** A week, rendered as a month name. Never a numeric date. */
    data class DateRef(override val key: SlotKey, val weekKey: String, val display: String) : Slot

    /** The number a validator re-reads and compares, or null for the two textual slots. */
    val numericValue: Int?
        get() = when (this) {
            is Count -> value
            is Days -> value
            is Percent -> value
            is Text, is DateRef -> null
        }
}

/**
 * Fills a template. CLARITY_LOGIC_ENGINE.md 7.2.
 *
 * **Every rule in 7.2 is applied here and nowhere else**, which is the only arrangement in
 * which the rules can be checked. There is one function that turns a number into text in
 * this app, and this is it.
 *
 * Rendering can **fail**, and that is a feature rather than an error path. It returns null
 * when a required slot is absent, and when a count of one lands in front of a plural noun
 * the slot cannot make agree. The caller drops that variant and takes another from the
 * same bench, per 7.2's slot completeness rule. A template must never reach a screen with
 * a visible marker and must never reach one reading `1 things`, and the way to guarantee
 * both is for this function to refuse rather than to do its best.
 */
object SlotRenderer {

    /**
     * Renders [text] with [slots], in the number register [purpose] asks for, or null when
     * it cannot be rendered truthfully.
     */
    fun render(text: String, slots: Map<SlotKey, Slot>, purpose: Purpose): String? {
        val out = StringBuilder()
        var cursor = 0
        for (match in MARKER.findAll(text)) {
            out.append(text, cursor, match.range.first)
            val slot = slots[match.groupValues[1]] ?: return null
            out.append(renderValue(slot, purpose))
            cursor = match.range.last + 1
            if (slot is Slot.Count && slot.value == SINGULAR) {
                val agreed = agree(text, cursor, slot) ?: return null
                out.append(agreed.text)
                cursor = agreed.cursor
            }
        }
        out.append(text, cursor, text.length)
        return out.toString().capitalizedOpener(text, slots)
    }

    /**
     * **A sentence that begins with a spelled number begins with a capital letter.**
     *
     * 177 corpus lines open with a numeric slot, and on a word spelling surface
     * [number] returns `one`, `two`, `three` lowercase, so the Areas caption read
     * "one completed this week" directly under a serif headline ending in a period, and
     * the Pulse and the Momentum headline did the same. It fires on nearly every banner.
     * Neighboring captions that happen to open with a word rather than a slot are
     * capitalized in the corpus, so the surface was inconsistent with itself line by
     * line.
     *
     * **Only when the template itself opened with the marker**, so a line whose first
     * character is authored prose keeps exactly the case the corpus gave it, and
     * **only for a spelled number**, because a name slot must never be recased: an area
     * called `iPhone stuff` is that person's own capitalization and not a typo.
     */
    private fun String.capitalizedOpener(template: String, slots: Map<SlotKey, Slot>): String {
        if (isEmpty()) return this
        val first = MARKER.find(template) ?: return this
        if (first.range.first != 0) return this
        // Only a spelled number. A name, a date reference or a percent either carries a
        // case its owner chose or is already a digit, and recasing any of them would be
        // the app rewriting somebody's own words.
        if (slots[first.groupValues[1]] !is Slot.Count && slots[first.groupValues[1]] !is Slot.Days) {
            return this
        }
        if (!this[0].isLowerCase()) return this
        return this[0].uppercaseChar() + substring(1)
    }

    /** One slot's own text, with no regard for what surrounds it. */
    fun renderValue(slot: Slot, purpose: Purpose): String = when (slot) {
        is Slot.Text -> slot.value
        is Slot.DateRef -> slot.display
        // 7.2: `78 percent`, never `78%`. The sign is a readout and this app is not one.
        is Slot.Percent -> "${slot.value} percent"
        is Slot.Count -> number(slot.value, purpose)
        is Slot.Days -> days(slot.value, purpose)
    }

    /**
     * 7.2's register difference: two through nine are **words in Pulse and Momentum and
     * digits in the Report**, and ten and above are always digits.
     *
     * One is rendered as a word on the surfaces that spell out the small numbers, which
     * 7.2 does not state and which follows from it: a Pulse reading `1 thing left the
     * queue` beside another reading `three things left the queue` would be the register
     * changing for no reason a reader could see. The Report spells nothing out and renders
     * it as a digit.
     */
    fun number(value: Int, purpose: Purpose): String {
        // Zero is a digit here and never reaches a template anyway: 7.2 forbids it, the
        // measures answer null rather than nought, and validator check 4 is the backstop.
        // Spelling it would put the word `zero` one refactor away from a screen.
        if (value < 1) return value.toString()
        val word = WORDS.getOrNull(value)
        return if (word != null && spellsSmallNumbers(purpose)) word else value.toString()
    }

    /**
     * 7.2: `yesterday`, `two days`, `nine days`, `three weeks`, `two months`, at the
     * appropriate magnitude.
     *
     * **The magnitudes change where the corpus's own ladders change.** Days up to
     * thirteen, weeks from fourteen, months from thirty are exactly the stage boundaries
     * `CORPUS_1_PULSE.md` gives the persistence family, so a line authored for stage 3
     * cannot render in the unit stage 2 was written around. Choosing round numbers here
     * instead would have put the change of unit somewhere no author had looked at.
     */
    fun days(value: Int, purpose: Purpose): String = when {
        value <= 0 -> value.toString()
        value == 1 -> "yesterday"
        value < WEEKS_FROM -> "${number(value, purpose)} days"
        value < MONTHS_FROM -> plural(value / DAYS_PER_WEEK, "week", purpose)
        else -> plural(value / DAYS_PER_MONTH, "month", purpose)
    }

    private fun plural(value: Int, noun: String, purpose: Purpose): String =
        if (value == SINGULAR) "${number(value, purpose)} $noun" else "${number(value, purpose)} ${noun}s"

    /**
     * Makes what follows a count of one agree with it, or refuses.
     *
     * Four outcomes, and the last two are the point.
     *
     * The word after the marker is the slot's own plural, so it becomes the singular and
     * the verb after it is checked as well: *1 item is queued* cannot be reached from *{n}
     * items are queued* by changing the noun alone, and half an agreement reads worse than
     * none, so that line is refused.
     *
     * The word after the marker is not a noun this slot governs, as in `{n} in, {m} out`,
     * so nothing changes and the line renders.
     *
     * The word after the marker is some other plural, or a plural verb, which means the
     * binding and the line disagree about what is being counted. Rather than print `one
     * things`, this returns null, the variant is dropped, and another line from the same
     * bench is chosen. A bench is fifteen lines deep and a broken plural is forever.
     */
    private fun agree(text: String, cursor: Int, slot: Slot.Count): Agreement? {
        val match = NEXT_WORD.find(text, cursor) ?: return Agreement("", cursor)
        if (match.range.first != cursor) return Agreement("", cursor)
        val spacing = match.groupValues[1]
        val lower = match.groupValues[2].lowercase()
        if (lower == slot.plural.lowercase()) {
            val after = NEXT_WORD.find(text, match.range.last + 1)
            val verb = if (after != null && after.range.first == match.range.last + 1) {
                after.groupValues[2].lowercase()
            } else {
                ""
            }
            if (verb in PLURAL_VERBS) return null
            return Agreement(spacing + slot.singular, match.range.last + 1)
        }
        if (lower in PLURAL_NOUNS || lower in PLURAL_VERBS) return null
        return Agreement("", cursor)
    }

    private data class Agreement(val text: String, val cursor: Int)

    /** True where 7.2 asks for small numbers as words. */
    private fun spellsSmallNumbers(purpose: Purpose): Boolean = when (purpose) {
        Purpose.PULSE, Purpose.MOMENTUM_HEADLINE, Purpose.AREAS_BANNER -> true
        Purpose.REPORT_HEADLINE, Purpose.REPORT_OBSERVATION, Purpose.REPORT_PATTERN -> false
    }

    private const val SINGULAR = 1

    /** 7.2, and the persistence ladder in `CORPUS_1_PULSE.md`. */
    private const val WEEKS_FROM = 14

    /** 7.2, and persistence stage 4. */
    private const val MONTHS_FROM = 30

    private const val DAYS_PER_WEEK = 7

    private const val DAYS_PER_MONTH = 30

    /** Index is the value, so `WORDS[3]` is `three`. Ten and above are digits, per 7.2. */
    private val WORDS = listOf("zero", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine")

    /**
     * The plural nouns a count can find itself in front of in the three corpus files.
     *
     * Deliberately a closed list rather than a rule about words ending in `s`. `things` is
     * a plural and `less` is not, and a heuristic that got that wrong would drop good
     * lines silently. Everything here was read out of the corpus.
     */
    private val PLURAL_NOUNS = setOf(
        "things", "items", "moves", "areas", "completions", "sessions", "days", "swaps",
        "events", "weeks", "answers", "pulses", "additions", "minutes", "sittings",
        "priorities", "times", "months", "sundays", "saturdays", "hours", "places",
    )

    /**
     * The verb forms that a singular subject cannot take.
     *
     * Short and closed, like [PLURAL_NOUNS], and read out of the corpus rather than guessed
     * at. A line whose verb is in here after a count of one is refused rather than rewritten:
     * conjugating an authored sentence at runtime is exactly the string assembly
     * `MASTER_BUILD_PROMPT.md` 11.4 forbids.
     */
    private val PLURAL_VERBS = setOf("are", "were", "have", "do", "sit", "take", "hold")

    private val MARKER = Regex("""\{([A-Za-z][A-Za-z0-9]*)\}""")

    private val NEXT_WORD = Regex("""(\s+)([A-Za-z]+)""")
}
