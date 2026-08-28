package com.kamsiob.claritynow.domain.engine.catalog

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The rules themselves: their subjects, their horizons, and the two things about them that
 * are easy to get wrong and impossible to see. CLARITY_LOGIC_ENGINE.md 4 and 5.
 *
 * The last test is the package's own purity guard. `DomainPurityTest` scans
 * `domain/engine` as a whole and catches Android imports, wall clocks and JVM hashes; this
 * one catches the thing that would not look wrong there, which is the catalog reaching
 * into `data` for a stored value. **No engine state in DataStore**: variation history,
 * escalation stages, personal records and first ever flags all derive from the event log,
 * or two devices silently disagree, and this is the failure that does not fail loudly.
 */
class RuleCatalogTest {

    @Test
    fun `every rule declares a positive horizon and a family`() {
        for (rule in CorpusFixture.catalog.rules) {
            assertTrue("${rule.key} horizon", rule.horizonDays > 0)
            assertTrue("${rule.key} family", rule.family.isNotBlank())
            assertEquals("${rule.key} purpose count", 1, rule.purpose.size)
        }
    }

    @Test
    fun `a rule reaching into history declares a horizon long enough to reach it`() {
        val historical = CorpusFixture.catalog.rules.filter { rule ->
            rule.criteria.any { it.id.contains("rising") || it.id.contains("declining") || it.id.contains("Average") }
        }
        assertTrue("no historical rule found, so this test is passing on nothing", historical.isNotEmpty())
        assertTrue(
            "a rule comparing across weeks with a seven day horizon would be filtered out at " +
                "step 3 of selection every time it qualified: " +
                historical.filter { it.horizonDays < 14 }.map { it.key },
            historical.all { it.horizonDays >= 14 },
        )
    }

    /**
     * Validator check 1 vetoes any candidate naming an area with no events in the window
     * being described, so a rule with an area subject that could select a silent area
     * would produce a candidate that is always vetoed, which is a silence with no reason
     * anyone could find.
     *
     * **Three families are exceptions, and the exception is a real conflict in the
     * specification rather than an oversight here.** `neglectedArea` and `areaGoneQuiet`
     * exist precisely to name an area that did nothing, and prohibition 1 forbids naming
     * an area with zero events **in the window under consideration**. For these the
     * window under consideration is the neglect window rather than the report week, and
     * check 1 has to be scoped to say so. Recorded in the phase 5 report for the validator
     * slice; the catalog's part is to name the families rather than leave them looking
     * like rules that forgot the criterion.
     *
     * **`familiarDip` is the third and it is the second branch of the same silence.**
     * 14b.9 removes `neglectedArea` and `areaGoneQuiet` from the ranking when the area's
     * quiet has a precedent, and this is the family that speaks in their place about the
     * same area over the same window. A gate that excused the two it relieves and not the
     * one that replaces them would swap a vetoed sentence for a differently vetoed one.
     * `AbsenceSubject` still requires a real lifetime, a non new area and a measured gap,
     * so a phantom area stays unnameable by this rule as by the other two.
     */
    @Test
    fun `a rule with an area subject requires that area to have events, or is one of the three silence families`() {
        val areaRules = CorpusFixture.catalog.rules.filter { it.subject === Subjects.AREA }
        assertTrue("no area subject rules found", areaRules.size >= 5)
        val namesASilentArea = areaRules.filterNot { rule ->
            rule.criteria.any {
                it.id.startsWith("area.hasEvents") || it.id.startsWith("$SHARE_FLOOR_PREFIX.area.")
            }
        }
        assertEquals(
            "these are the only rules that may name an area with no events in the week",
            setOf("neglectedArea", "areaGoneQuiet", "familiarDip"),
            namesASilentArea.map { it.family }.toSet(),
        )
    }

    @Test
    fun `a rule with an item subject requires the holding area to have events`() {
        val itemRules = CorpusFixture.catalog.rules.filter { it.subject === Subjects.ACTIVE_ITEM }
        assertTrue("no item subject rules found", itemRules.size >= 4)
        assertTrue(
            "an item is named alongside its area, so the area has to be real too",
            itemRules.all { rule -> rule.criteria.any { it.id.startsWith("holdingArea.hasEvents") } },
        )
    }

    @Test
    fun `persistence stage 4 requires the item to actually hold the record`() {
        val rule = CorpusFixture.catalog.rules.single { it.key == "pulse.persistence.s4" }
        assertTrue(
            "stage 4's bench reaches into HistoryFacts.longestEverActiveDays, and every line " +
                "there would be a lie the moment a longer running item existed. 7.3 requires this",
            rule.criteria.any { it.id == "persistence.holdsTheRecord" },
        )
    }

    @Test
    fun `the callback family carries a real callback requirement`() {
        val rule = CorpusFixture.catalog.rules.single { it.key == "report.observation.selfReportVsData" }
        val callback = requireNotNull(rule.requiresCallback)
        assertEquals("persistence", callback.family)
        assertTrue("a callback window that reaches nothing", callback.withinDays > 0)
        assertTrue(
            "the quote is about one item, so the subject has to match or the sentence quotes " +
                "an answer given about something else",
            callback.subjectMustMatch,
        )
    }

    @Test
    fun `every criterion carries a description the simulator can print`() {
        for (rule in CorpusFixture.catalog.rules) {
            for (criterion in rule.criteria) {
                assertTrue("${rule.key} / ${criterion.id}", criterion.describe.length > 10)
            }
        }
    }

    /**
     * The register is empty, and the shape checks below still run so that a future entry
     * cannot be a placeholder.
     *
     * It held twelve entries when phase 5 wrote it and the facts phase declared every fact
     * they named, so an entry appearing here again is a rule being given up rather than a
     * gap being recorded for the first time. That is a decision somebody has to make on
     * purpose, which is what the citation and the missing fact are for.
     */
    @Test
    fun `the register of families awaiting a fact is empty`() {
        assertEquals(
            "every family the register held has its fact and its rule. An entry here means a " +
                "family has been allowed to go quiet, and the only reason that is ever right is " +
                "a trigger naming a fact 3.1 genuinely does not declare",
            emptyList<String>(),
            RulesAwaitingFacts.GAPS.map { "${it.family} stage ${it.stage}" },
        )
    }

    @Test
    fun `every recorded gap names a family the corpus actually holds`() {
        val known = CorpusFixture.catalog.families.map { it.key }.toSet()
        val unknown = RulesAwaitingFacts.GAPS.map { it.family }.filterNot { it in known }
        assertTrue("RulesAwaitingFacts names families the corpus does not: $unknown", unknown.isEmpty())
        for (gap in RulesAwaitingFacts.GAPS) {
            assertTrue("${gap.family} names no missing fact", gap.missingFact.length > 10)
            assertTrue("${gap.family} cites nothing", gap.citation.length > 20)
        }
    }

    @Test
    fun `a recorded gap for a stage has no rule at that stage`() {
        for (gap in RulesAwaitingFacts.GAPS) {
            val stage = gap.stage ?: continue
            val rules = CorpusFixture.catalog.rulesOf(gap.purpose, gap.family)
                .filter { it.stage == stage }
            assertTrue(
                "${gap.family} stage $stage is recorded as awaiting ${gap.missingFact} and yet " +
                    "has ${rules.size} rules. Either the fact arrived and the record is stale, or " +
                    "a rule is approximating a fact that does not exist",
                rules.size <= 1,
            )
        }
    }

    /**
     * No criterion id carries two different descriptions.
     *
     * Ids are reused on purpose: `accumulation.additions` is the same requirement in four
     * rules and is written once and read four times. Two **different** conditions under one
     * id is the case that breaks something, because section 14's discrimination report is
     * keyed by id and would merge them, and the pass rate it printed would be of neither.
     * The Pulse `switching` family and the Report `switchingBehavior` family both count
     * swaps, at two grains, and this is what keeps their ids apart.
     */
    @Test
    fun `a criterion id always means the same condition`() {
        val descriptions = mutableMapOf<String, MutableSet<String>>()
        for (rule in CorpusFixture.catalog.rules) {
            for (criterion in rule.criteria) {
                descriptions.getOrPut(criterion.id) { mutableSetOf() }.add(criterion.describe)
            }
        }
        val collisions = descriptions.filterValues { it.size > 1 }
        assertTrue(
            "one criterion id, two conditions:\n" +
                collisions.entries.joinToString("\n") { "${it.key}: ${it.value}" },
            collisions.isEmpty(),
        )
        assertTrue(
            "ids are shared across rules on purpose, so this test is not asserting uniqueness",
            descriptions.size < CorpusFixture.catalog.rules.sumOf { it.criteria.size },
        )
    }

    @Test
    fun `nothing in the catalog package reads a stored value`() {
        check(File("build.gradle.kts").isFile) { "unit tests run from the app module directory" }
        val sources = File("src/main/java/com/kamsiob/claritynow/domain/engine/catalog")
            .walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .toList()
        assertTrue("no catalog sources found, so this scan would pass vacuously", sources.isNotEmpty())
        val offenders = sources.flatMap { file ->
            file.readLines().mapIndexedNotNull { index, text ->
                val trimmed = text.trimStart()
                if (trimmed.startsWith("//") || trimmed.startsWith("*") || trimmed.startsWith("/*")) return@mapIndexedNotNull null
                val reachesIntoData = Regex("""^\s*import\s+com\.kamsiob\.claritynow\.(data|ui|di)\.""")
                if (reachesIntoData.containsMatchIn(text)) "${file.name}:${index + 1}: ${text.trim()}" else null
            }
        }
        assertTrue(
            "the catalog reached outside the domain. Escalation stages, variation history and " +
                "personal records derive from the event log, never from a stored value, or two " +
                "devices that merged the same log compute different next variants:\n" +
                offenders.joinToString("\n"),
            offenders.isEmpty(),
        )
    }
}
