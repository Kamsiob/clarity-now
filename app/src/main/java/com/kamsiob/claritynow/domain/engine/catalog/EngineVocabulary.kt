package com.kamsiob.claritynow.domain.engine.catalog

import com.kamsiob.claritynow.domain.engine.FactSet

/**
 * The shared vocabulary CLARITY_LOGIC_ENGINE.md 2.1 declares once so nothing has to be
 * inferred: the purposes, the registers, the length bands, and the subject types a rule
 * is evaluated against.
 *
 * **Where these belong.** 2.1 places them in `domain.engine` rather than in this
 * subpackage. They are here because the rule catalog is the first slice of phase 5 that
 * needs them and phase 5 was built as parallel slices, so this file is the one place
 * they were certain to exist. If another slice declares any of these in
 * `com.kamsiob.claritynow.domain.engine`, delete the duplicate here and re-point the
 * imports; nothing in this package depends on which package they live in.
 *
 * Every one of them is a closed set on purpose. An open string where an enum belongs is
 * how a corpus tag nobody declared reaches a screen, and the parser in this package
 * rejects a tag it cannot map rather than defaulting it.
 *
 * `AreaId`, `ItemId`, `FamilyKey` and `VariantKey` are deliberately absent. The fact slice
 * declares all four in `domain.engine`, and a second set of aliases to `String` here would
 * be an ambiguous reference the first time a file star imported both packages. They are
 * imported from there instead.
 */

/** CLARITY_LOGIC_ENGINE.md 2.1. For example `pulse.persistence.s2`. */
typealias RuleKey = String

/** CLARITY_LOGIC_ENGINE.md 2.1. For example `itemTitle`. */
typealias SlotKey = String

/**
 * The six surfaces the engine speaks on. CLARITY_LOGIC_ENGINE.md 2.1.
 *
 * A purpose is not a screen, it is a slot in a composition. The Report alone uses three
 * of them because its headline, its observations and its pattern line are selected
 * separately and constrain each other, per section 9.
 */
enum class Purpose {
    PULSE,
    REPORT_HEADLINE,
    REPORT_OBSERVATION,
    REPORT_PATTERN,
    MOMENTUM_HEADLINE,
    AREAS_BANNER,
}

/**
 * The five voices. CLARITY_LOGIC_ENGINE.md 2.1, selected per 7.4.
 *
 * [EDITORIAL] is the Report's alone and is budgeted at two leads per report.
 * [NEUTRAL_AGENT] is reached only through a rule marked `unflattering`, and it is the
 * one register that is a constraint on grammar rather than a matter of taste: the fact
 * becomes the subject of the sentence instead of the person. It is not passive voice,
 * and validator check 10 in section 8 rejects the passive forms it is mistaken for.
 */
enum class Register {
    PLAIN,
    OBSERVATIONAL,
    REFLECTIVE,
    EDITORIAL,
    NEUTRAL_AGENT,
}

/**
 * CLARITY_LOGIC_ENGINE.md 2.1, computed per 7.5.
 *
 * Never authored. See [LengthBands] for why, and for the boundaries.
 */
enum class LengthBand {
    SHORT,
    MEDIUM,
    LONG,
}

/** CLARITY_LOGIC_ENGINE.md 2.1. */
enum class SubjectKind {
    AREA,
    ITEM,
}

/**
 * A subject a rule is evaluated against. CLARITY_LOGIC_ENGINE.md 2.1 and 4.
 *
 * The rule runs once per subject, so it can qualify for Work and not for Health in the
 * same window, and escalation is tracked per `(family, subjectId)` so nine days on one
 * item and three on another are independent ladders.
 */
data class Subject(val id: String, val kind: SubjectKind)

/**
 * Yields the subjects a rule is evaluated against. CLARITY_LOGIC_ENGINE.md 2.1.
 *
 * `NONE` yields exactly one null subject, which is what makes a family with no subject
 * fall out of the same loop as one with eleven.
 */
fun interface SubjectSelector {
    fun select(facts: FactSet): List<Subject?>
}

/** CLARITY_LOGIC_ENGINE.md 2.1. `isPositive` is the only interpretation the app makes of an answer. */
data class ResponseOption(val key: String, val label: String, val isPositive: Boolean)
