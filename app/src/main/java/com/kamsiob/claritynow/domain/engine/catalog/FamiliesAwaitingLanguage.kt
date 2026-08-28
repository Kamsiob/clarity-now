package com.kamsiob.claritynow.domain.engine.catalog

import com.kamsiob.claritynow.domain.engine.FamilyKey
import com.kamsiob.claritynow.domain.engine.Precedent

/**
 * The mirror of [RulesAwaitingFacts]: a family whose rules are written and whose language
 * is not, held out of the catalog until the lines exist.
 *
 * ## Why this register exists at all
 *
 * `MASTER_BUILD_PROMPT.md` 14b.9 asks for two branches. When a fall has happened to this
 * subject before it is a rhythm rather than a decline, the decline family is excluded,
 * **and a different family fires with different language.** The exclusion is
 * `FamilyAvailability`; the second branch needs a family to fire into, and the sentences it
 * says are corpus lines, which belong to phase 9 by rule 11.1 and may not be written here.
 *
 * The catalog cannot hold a half built family. `ReportWalker.finish` throws when the
 * families declared in [EngineFamilies] and the families found in the corpus file differ in
 * either direction, and [CatalogIntegrity.rulesPointAtExistingFamilies] fails a rule naming
 * a family the corpus does not carry. Both are right and neither should be relaxed for
 * this: a family with a rule and no bench would qualify, produce no sentence, and look
 * exactly like a family that never happened to fire, which is the failure
 * [RulesAwaitingFacts] was written to make visible from the other side.
 *
 * So the rules are written here, in full, against the real facts, and a test runs them.
 * **This is a declaration and not a parking space.** [RULES] never reaches
 * `ClarityCatalog.build`, so nothing here can speak; what it can do is be wrong in a way a
 * test catches now rather than after the language lands.
 *
 * ## What phase 9 does with it
 *
 * Five steps, and the last two are the ones a reader will not guess:
 *
 * 1. author the bench in `CORPUS_2_REPORT.md` section 2, under the key prefix in
 *    [Reservation.keyPrefix], with the constraints in [FAMILIAR_DIP_CONSTRAINTS]
 * 2. add [FAMILIAR_DIP] to `EngineFamilies.REPORT_OBSERVATION`
 * 3. move [RULES] into `ReportRules.ALL` and delete the entry here
 * 4. add the area rule's family to the two silence family sets: the one in
 *    `RuleCatalogTest`, which asserts that only `neglectedArea` and `areaGoneQuiet` may
 *    name an area with no events in the week, and nothing else, because a third family
 *    doing it is a decision rather than an oversight. `AbsenceSubjectRules` needs no edit:
 *    it reads the three rule lists and will find the flag once the rule is in one
 * 5. decide whether the family joins the widened `unflattering` enumeration 14b.10 brings.
 *    It is `false` here because 7.4 as written does not name it, exactly as `MomentumRules`
 *    is false for the same reason, and the argument runs both ways: the family exists to
 *    say a shape is a rhythm rather than a decline, which is the opposite of unflattering,
 *    and every line it says is still about somebody's low weeks
 */
internal object FamiliesAwaitingLanguage {

    /**
     * The rhythm family of 14b.9. `familiarDip` rather than `rhythm`, and the choice is
     * deliberate.
     *
     * `rhythm` is the obvious name, because 14b.9 says "it is a rhythm, not a decline" in
     * those words, and it loses to a collision the obvious name cannot see:
     * `consistentRhythm` is already a pattern family and it means something else entirely,
     * a person whose weeks sit inside a narrow band. Two family keys both reading as rhythm
     * and meaning opposite shapes is a name that will be misread by somebody writing a rule
     * against it, and a family key is not private: it is stored on every `REPORT_GENERATED`
     * event and is therefore in the export file and in `docs/EVENT_FORMAT.md`, which is the
     * contract a second implementation is built from. DECISIONS.md C6 is the same argument
     * about `FOCUS_ABANDONED`.
     *
     * `familiarDip` names both halves of what the fact says: there is a fall, and this
     * person has had it before.
     */
    const val FAMILIAR_DIP: FamilyKey = "familiarDip"

    /**
     * What phase 9 has to hold while writing the bench, carried here because the rule and
     * the constraint were decided together.
     *
     * Every one of these follows from the fact rather than from taste, and the last is the
     * one that would otherwise be discovered by a reader of the shipped app.
     */
    val FAMILIAR_DIP_CONSTRAINTS: List<String> = listOf(
        "It never states the depth, the duration or the date of any fall. `Precedent` " +
            "carries the verdict and nothing else precisely so that no measure can reach " +
            "one, and a line asking for a count of low weeks has no slot to fill it from",
        "It never claims the person meant it. A fall having a precedent says this shape " +
            "has happened before, and inferring that somebody chose it is the inference " +
            "14b.10 removes from `pt.gone`",
        "It never predicts a return. Nothing in the fact set says a cycle will turn again, " +
            "and `it will pick back up` is a claim about the future of somebody's health",
        "It may not say `this week`. The precedent is read over twelve weekly buckets and " +
            "the newest part week is skipped, so the newest thing it can name is the last " +
            "closed week",
        "It reads as an observation and not as reassurance. 6.4 removes `hardStretch` " +
            "rather than rewriting it if any line reads as consolation, and the same test " +
            "applies here for the same reason",
    )

    /** One family with rules and no language, and what it is waiting for. */
    data class Reservation(
        val key: FamilyKey,
        val purpose: Purpose,
        val cooldownDays: Int,
        /** The corpus key prefix its lines carry, so phase 9 does not have to choose one. */
        val keyPrefix: String,
        val citation: String,
        val why: String,
    )

    val FAMILIES: List<Reservation> = listOf(
        Reservation(
            key = FAMILIAR_DIP,
            purpose = Purpose.REPORT_OBSERVATION,
            cooldownDays = EngineFamilies.REPORT_DEFAULT_COOLDOWN_DAYS,
            keyPrefix = "ob.fam",
            citation = "MASTER_BUILD_PROMPT.md 14b.9, Addendum 01 7b",
            why = "the second branch of the capacity gate. The first branch excludes the " +
                "decline family; this is what speaks instead, and 14b.9 requires that the " +
                "two speak differently rather than that one be re-worded",
        ),
    )

    /** Every reserved family key, for the integrity check that they are not also declared. */
    val KEYS: Set<FamilyKey> = FAMILIES.map { it.key }.toSet()

    /**
     * A comparison against this subject's whole history, which is the oldest fact any of
     * these rules reads.
     *
     * A precedent is the earliest fall at least as deep and at least as long as the current
     * one, and it can sit anywhere in a person's log. Section 4 defines a horizon as the
     * maximum age of the oldest fact referenced, so the honest number here is the one the
     * record families already use for the same reason: they name a week that can be two
     * years old on a fact set where nothing else is.
     */
    private const val HISTORY_HORIZON = 180

    /**
     * The three rules, one per subject the precedent facts measure.
     *
     * **One criterion each, and the second one somebody will want to add is padding.**
     * [Precedent.PRESENT] already means low now, at least twelve weeks of this subject's
     * own history behind it, and an earlier fall at least as deep and at least as long. A
     * criterion restating the history requirement could never separate one fact set from
     * another, and `ClarityRule` says specificity is the one number nobody authors. The
     * consequence is that these rank at the bottom of the observation pass, which is
     * correct: 11.4 forbids padding a report to reach a minimum, and a rhythm line is not
     * entitled to a slot a real observation wants.
     *
     * The subject split is the facts phase's own, and it is the discipline
     * `FamilyAvailability.PRECEDENT_GATED` states: a family is gated only where a precedent
     * fact measures the same quantity its claim is about, so the family that answers has to
     * be split the same way. A rhythm line about somebody's weeks and a rhythm line about
     * one area are different sentences with different slots.
     */
    val RULES: List<ClarityRule> = listOf(
        ClarityRule(
            key = "report.observation.familiarDip.activity",
            purpose = setOf(Purpose.REPORT_OBSERVATION),
            family = FAMILIAR_DIP,
            subject = Subjects.NONE,
            criteria = listOf(
                window(
                    "familiarDip.activity.precedent",
                    "this person's weeks have been this low, for this long, before",
                ) { it.history.activityDipPrecedent == Precedent.PRESENT },
            ),
            priority = 0,
            horizonDays = HISTORY_HORIZON,
            unflattering = false,
            stage = 1,
        ),
        ClarityRule(
            key = "report.observation.familiarDip.focus",
            purpose = setOf(Purpose.REPORT_OBSERVATION),
            family = FAMILIAR_DIP,
            subject = Subjects.NONE,
            criteria = listOf(
                window(
                    "familiarDip.focus.precedent",
                    "focus has fallen away like this, for this long, before",
                ) { it.history.focusDipPrecedent == Precedent.PRESENT },
            ),
            priority = 0,
            horizonDays = HISTORY_HORIZON,
            unflattering = false,
            stage = 1,
        ),
        ClarityRule(
            key = "report.observation.familiarDip.area",
            purpose = setOf(Purpose.REPORT_OBSERVATION),
            family = FAMILIAR_DIP,
            subject = Subjects.AREA,
            criteria = listOf(
                area(
                    "familiarDip.area.precedent",
                    "this area has been this quiet, for this long, before",
                ) { it.dipPrecedent == Precedent.PRESENT },
            ),
            priority = 0,
            horizonDays = HISTORY_HORIZON,
            // The subject is the area's silence, exactly as it is for the two families this
            // one relieves, so check 1 has to be told. `AbsenceSubject` still requires a
            // real lifetime, a non new area and a measured gap, so a phantom area stays
            // unnameable by this rule as by every other.
            unflattering = false,
            absenceSubject = true,
            stage = 1,
        ),
    )
}
