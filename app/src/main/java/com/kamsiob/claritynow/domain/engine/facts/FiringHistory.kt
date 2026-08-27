package com.kamsiob.claritynow.domain.engine

import com.kamsiob.claritynow.domain.query.TrailQueries

/**
 * What the engine has already said, rebuilt from the log on every invocation.
 * CLARITY_LOGIC_ENGINE.md 2.1 and 7.6.
 *
 * **Derived entirely from PULSE_GENERATED, REPORT_GENERATED and PLAN_OFFERED.
 * Never from DataStore.** This is not a preference about where state lives. A device
 * that has just merged a log has to compute the same next variant as the device that
 * produced it, and DataStore does not merge: two phones would drift apart silently,
 * each convinced it had never used a line the other had used yesterday. The log
 * merges, so the history derived from it merges too.
 *
 * The Addendum 01 schema window in August 2026 added `subjectId` and `subjectKind`
 * to the Pulse pair, and `familyKey`, `variantKey`, `escalationStage`, `register`,
 * `subjectId` and `subjectKind` to `ReportSectionSnapshot`, plus `headlineVariantKey`
 * to `ReportGenerated`, precisely so that all four fields below are derivable. Issue
 * #19 carries the reasoning. Without them there is nothing to key an escalation
 * ladder or a family cooldown by.
 *
 * ## Determinism
 *
 * Two objects rebuilt independently from the same merged log produce identical
 * selections for the same `dateKey`, which is one of the required tests in 14. Two
 * properties get that: the payloads arrive in `TrailQueries`'s total order, which is
 * `(lamport, originId, id)` and therefore identical on both devices, and every
 * "most recent" below resolves by greatest `dateKey` first with the later position
 * in that total order breaking a tie. No wall clock is read and no insertion order
 * is trusted.
 */
data class FiringHistory(
    /** Every variant ever rendered, mapped to the most recent `dateKey` it appeared on. */
    val variantsUsed: Map<VariantKey, String>,
    /** The escalation stage last shown for a `(family, subjectId)` pair. */
    val lastStageBySubject: Map<Pair<FamilyKey, String?>, Int>,
    /** The most recent `dateKey` a `(family, subjectId)` pair fired on. */
    val lastFiredBySubject: Map<Pair<FamilyKey, String?>, String>,
    /**
     * The family of the most recent Pulse.
     *
     * Step 4 of selection drops every candidate sharing it, which is the entire no
     * repeat rule.
     */
    val lastPulseFamily: FamilyKey?,
) {

    /**
     * Whole days from the last use of [variantKey] to [dateKey], or null when it has
     * never been used.
     *
     * Null means available. 7.6 filters variants used within 90 days, and a variant
     * with no recorded use has not been used within any number of days.
     */
    fun daysSinceVariant(variantKey: VariantKey, dateKey: String): Int? {
        val used = variantsUsed[variantKey] ?: return null
        return FactDates.daysBetweenKeys(used, dateKey)
    }

    /**
     * True when [variantKey] was used inside [days] before [dateKey].
     *
     * A variant recorded against a key that cannot be parsed reads as available, per
     * `FactDates.daysBetweenKeys`. Losing one exclusion costs at worst a repeat the
     * bench would have avoided; treating an unparseable key as used today would
     * retire the line permanently.
     */
    fun variantUsedWithin(variantKey: VariantKey, dateKey: String, days: Int): Boolean {
        val since = daysSinceVariant(variantKey, dateKey) ?: return false
        return since in 0 until days
    }

    /** Whole days since `(family, subjectId)` last fired, or null when it never has. */
    fun daysSinceFiring(family: FamilyKey, subjectId: String?, dateKey: String): Int? {
        val fired = lastFiredBySubject[family to subjectId] ?: return null
        return FactDates.daysBetweenKeys(fired, dateKey)
    }

    /**
     * True when `(family, subjectId)` fired inside its cooldown.
     *
     * Step 5 of selection. A pair that has never fired is never in cooldown.
     */
    fun inCooldown(
        family: FamilyKey,
        subjectId: String?,
        dateKey: String,
        cooldownDays: Int,
    ): Boolean {
        val since = daysSinceFiring(family, subjectId, dateKey) ?: return false
        return since in 0 until cooldownDays
    }

    /**
     * The stage last shown for `(family, subjectId)`, or null when it has not fired.
     *
     * 7.3's monotonicity rule reads this: while a condition stays continuously true,
     * a lower stage than this one must never be shown, so that nine days on Tuesday
     * cannot become three days on Wednesday because a promotion reset an age.
     */
    fun lastStage(family: FamilyKey, subjectId: String?): Int? =
        lastStageBySubject[family to subjectId]

    companion object {

        /** Nothing has ever fired. What a fresh install rebuilds to. */
        val EMPTY = FiringHistory(
            variantsUsed = emptyMap(),
            lastStageBySubject = emptyMap(),
            lastFiredBySubject = emptyMap(),
            lastPulseFamily = null,
        )

        /**
         * Rebuild the history from every engine authored event before [asOfMillis].
         *
         * Lifetime rather than windowed. The 90 day variant exclusion needs 90 days,
         * a family cooldown needs at most 42, and an escalation ladder needs however
         * long the condition has been true. Bounding the read would make the bound a
         * fourth number nobody could see, so it reads everything and the maps stay
         * small: one entry per variant ever rendered, and one per family and subject
         * pair.
         */
        fun from(queries: TrailQueries, asOfMillis: Long): FiringHistory {
            val variants = HashMap<VariantKey, String>()
            val stages = HashMap<Pair<FamilyKey, String?>, Int>()
            val fired = HashMap<Pair<FamilyKey, String?>, String>()

            fun noteVariant(variantKey: String?, dateKey: String) {
                if (variantKey.isNullOrEmpty()) return
                val seen = variants[variantKey]
                if (seen == null || dateKey >= seen) variants[variantKey] = dateKey
            }

            fun noteFiring(family: String, subjectId: String?, dateKey: String, stage: Int?) {
                val key = family to subjectId
                val seen = fired[key]
                if (seen != null && dateKey < seen) return
                fired[key] = dateKey
                if (stage != null) stages[key] = stage
            }

            var lastPulseKey: String? = null
            var lastPulseFamily: FamilyKey? = null

            // Pulse. The payloads arrive in total order, so a later entry with an
            // equal dateKey deliberately wins: it is the one the device wrote last.
            for (pulse in queries.pulsesGeneratedBetween(Long.MIN_VALUE, asOfMillis)) {
                noteVariant(pulse.variantKey, pulse.dateKey)
                noteFiring(pulse.family, pulse.subjectId, pulse.dateKey, pulse.escalationStage)
                val newestSoFar = lastPulseKey
                if (newestSoFar == null || pulse.dateKey >= newestSoFar) {
                    lastPulseKey = pulse.dateKey
                    lastPulseFamily = pulse.family
                }
            }

            // The Report. A report is keyed by the week it covers, so its week start
            // key is the dateKey every exclusion window measures from. The headline
            // has no stage of its own on the payload and records only a firing.
            for (report in queries.reportsGeneratedBetween(Long.MIN_VALUE, asOfMillis)) {
                noteVariant(report.headlineVariantKey, report.weekStartKey)
                noteFiring(report.headlineKey, null, report.weekStartKey, null)
                for (section in report.renderedSections) {
                    noteVariant(section.variantKey, report.weekStartKey)
                    noteFiring(
                        section.familyKey,
                        section.subjectId,
                        report.weekStartKey,
                        section.escalationStage,
                    )
                }
            }

            // Plans. A plan carries three authored keys and no escalation stage, so
            // it records the keys and nothing else. It deliberately does not record a
            // firing against its family: the observation that motivated the plan was
            // already recorded by the report section that carried it, and recording
            // it twice would put the family into a cooldown the observation did not
            // earn. 10.6 is emphatic that guidance never gets its own hold over what
            // an observation may say.
            for (plan in queries.plansOfferedBetween(Long.MIN_VALUE, asOfMillis)) {
                noteVariant(plan.frameKey, plan.weekStartKey)
                noteVariant(plan.cueKey, plan.weekStartKey)
                noteVariant(plan.actionKey, plan.weekStartKey)
            }

            return FiringHistory(
                variantsUsed = variants.toMap(),
                lastStageBySubject = stages.toMap(),
                lastFiredBySubject = fired.toMap(),
                lastPulseFamily = lastPulseFamily,
            )
        }
    }
}
