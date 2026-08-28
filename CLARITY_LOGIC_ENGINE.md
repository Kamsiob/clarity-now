# The Clarity Logic Engine

The deterministic observation and guidance system behind Clarity Pulse, the Clarity Report, Momentum and the Areas banner.

**This document is the authority on everything in `domain.engine` and `domain.guidance`.** Read it completely before writing code in either package. There is no separate amendments file; everything from both review panels is folded in here.

---

## 1. What this is

The engine produces sentences about a person's own behavior that feel observed rather than generated. No model, no inference, no network, no randomness. It is arithmetic over an event log, plus a large library of hand written sentences, plus a selection system that always fires the most specific thing that applies.

**Prime directive: every claim must be true, and provably so from a count query.** One fabricated area name or off-by-one number permanently destroys the credibility of everything else the app says, and there is no recovering from it because the user has no way to verify anything afterwards.

**Second directive: the engine may say nothing.** Silence is supported, expected, and designed. A system that speaks every day is a machine. A system that occasionally has nothing worth saying has judgment.

**Third directive: the engine observes, the user decides.** It never instructs, never evaluates compliance, never claims authority it does not have.

### 1.1 Absolute prohibitions

The engine must never:

1. Name an area with zero events in the window under consideration
2. State a number that did not come from a `TrailQueries` function
3. Reference an archived or deleted entity
4. Interpolate a live entity name; it always uses the snapshot carried in the event
5. Produce different output on two devices given the same event log
6. Read the wall clock, a random number generator, DataStore, or any Android API
7. Emit a sentence whose family was not authored for the criteria that fired it
8. Use the words should, failed, behind, lazy, hurry, streak, must, have to, or any construction assigning blame
9. Repeat a phrasing variant seen inside 90 days unless the bench is genuinely exhausted
10. Contain an em dash, en dash, emoji, or non-ASCII character outside standard punctuation
11. Use a British spelling in any user-facing string
12. **State or imply that the user did or did not act on a plan**
13. **Render a plan as an imperative**

Items 1 to 5, 12 and 13 are enforced by code and covered by tests. The rest are enforced by catalog tests and the authoring protocol.

---

## 2. Architecture

Six layers, each a pure function, each independently testable.

```
Event log
    |
[1] FactExtractor      (EventLog, Window, Clock) -> FactSet
    |
[2] RuleCatalog        static data, no strings
    |
[3] Selector           (FactSet, FiringHistory, RuleCatalog) -> Selection?
    |
[4] Realizer           (Selection, FactSet, PhrasingCatalog) -> Candidate
    |
[5] Validator          (Candidate, FactSet) -> Validated | Vetoed
    |
[6] GuidanceComposer   (Validated[], FactSet, PlanHistory) -> GuidanceResult
    |
Rendered output, or nothing
```

**Layer 6 runs only for the Report**, only after layers 1 to 5 have produced the report body, and produces at most one output. Pulse, Momentum and the banner never reach it.

**Build state, phase 5.** Layers 1 to 5 are built and layer 6 is not; it is phase 9b and section 10 calls it the last thing built. Layer 1 is `FactExtractor` in `domain.engine`, layer 2 is `ClarityCatalog` in `domain.engine.catalog`, and layers 3, 4 and 5 are in `domain.engine.select`, `domain.engine.realize` and `domain.engine.validate`. `ClarityEngine.observe` runs 3, 4 and 5 in one loop: a veto sends it to the next ranked selection, and an exhausted list is silence. Every note below marked **Phase 5** records a place where a builder found this document open to more than one reading and had to choose; the choice is recorded here so the next reader sees it where the ambiguity is.

### 2.1 Type definitions

Declared once so nothing has to be inferred. All are in `domain.engine` unless noted.

```kotlin
typealias AreaId     = String   // UUID
typealias ItemId     = String   // UUID
typealias RuleKey    = String   // "pulse.persistence.long"
typealias FamilyKey  = String   // "persistence", camelCase for Report families
typealias VariantKey = String   // "persistence.s2.11"
typealias SlotKey    = String   // "itemTitle"
typealias FrameKey   = String   // "frm.01"        (domain.guidance)
typealias CueKey     = String   // "cue.band.01"   (domain.guidance)
typealias ActionKey  = String   // "act.neg.01"    (domain.guidance)

enum class Purpose { PULSE, REPORT_HEADLINE, REPORT_OBSERVATION, REPORT_PATTERN,
                     MOMENTUM_HEADLINE, AREAS_BANNER }
enum class Register { PLAIN, OBSERVATIONAL, REFLECTIVE, EDITORIAL, NEUTRAL_AGENT }
enum class LengthBand { SHORT, MEDIUM, LONG }
enum class Trend { RISING, FALLING, FLAT, INSUFFICIENT }
enum class PartOfDay { MORNING, AFTERNOON, EVENING, NIGHT }
enum class Weekday { MON, TUE, WED, THU, FRI, SAT, SUN }
enum class SilenceReason { NO_RULE_QUALIFIED, ALL_QUALIFIED_RULES_FILTERED,
                           INSUFFICIENT_DATA, ALL_CANDIDATES_VETOED, DELIBERATE_SILENCE }
enum class FirstEver { FIRST_COMPLETION, FIRST_FOCUS_SESSION, FIRST_SWAP,
                       FIRST_AREA_ARCHIVED, FIRST_QUEUE_DRAIN,
                       FIRST_WEEK_WITH_ALL_AREAS_ACTIVE }

/** Names the fact a rendered number came from, so the validator can re-read and compare. */
data class FactRef(val category: String, val path: String)   // ("window", "completions")

/** A subject a rule is evaluated against. NONE yields exactly one null subject. */
fun interface SubjectSelector { fun select(facts: FactSet): List<Subject?> }
data class Subject(val id: String, val kind: SubjectKind)
enum class SubjectKind { AREA, ITEM }

data class ResponseOption(val key: String, val label: String, val isPositive: Boolean)

/** Rebuilt from the log on every invocation. Never persisted, never from DataStore. */
data class FiringHistory(
    val variantsUsed: Map<VariantKey, String>,              // -> most recent dateKey
    val lastStageBySubject: Map<Pair<FamilyKey, String?>, Int>,
    val lastFiredBySubject: Map<Pair<FamilyKey, String?>, String>,
    val lastPulseFamily: FamilyKey?
)

data class PlanHistory(val plans: List<ClarityPlan>)        // domain.guidance

data class Validated(val candidate: Candidate)              // a Candidate that passed layer 5
data class RenderedOutput(val text: String, val question: String?,
                          val responses: List<ResponseOption>, val meta: Candidate)
```

`WindowFacts`, `AreaFacts`, `RollupFacts`, `ItemFacts`, `HistoryFacts` and `PulseFacts` are declared in 3.1. `CueFacts` is declared in 3.7.

**Phase 5, and it is a build artifact rather than a decision.** This section places all of the above in `domain.engine`. The fact classes, `FiringHistory`, `FactRef`, `Validated` and the engine's own result types are there. `Purpose`, `Register`, `LengthBand`, `SubjectKind`, `RuleKey` and `SlotKey` ended up one level down in `domain.engine.catalog`, because phase 5 was built as parallel slices and the catalog slice was the first that needed them. Nothing depends on which of the two packages they live in, and the file that declares them says so and says how to move them. Recorded rather than corrected, because moving a declaration is a change to every import in the package and this document is the thing that says where they belong.

### 2.2 Purity contract

```kotlin
package com.kamsiob.claritynow.domain.engine

// No Android imports. Ever.
// No System.currentTimeMillis(), no Random, no UUID.randomUUID(), no String.hashCode().

object ClarityEngine {
    fun observe(facts: FactSet, history: FiringHistory, purpose: Purpose): EngineResult
}

sealed interface EngineResult {
    data class Spoke(val output: RenderedOutput) : EngineResult
    data class Silent(val reason: SilenceReason) : EngineResult
}
```

`SilenceReason` is `NO_RULE_QUALIFIED`, `ALL_QUALIFIED_RULES_FILTERED`, `INSUFFICIENT_DATA`, `ALL_CANDIDATES_VETOED`, or `DELIBERATE_SILENCE`. Recorded in simulator output and debug logs, never shown to the user.

Given identical inputs the engine returns identical output on any device, in any process, at any time. This is what makes it safe to run on a phone and a Linux desktop against a merged log.

---

## 3. Layer 1: Fact extraction

The only layer that touches data. Runs once per invocation and produces a fully populated, immutable `FactSet`. No lazy evaluation; a fact computed at validation time could differ from the fact that fired the rule.

```kotlin
data class FactSet(
    val window: WindowFacts,
    val areas: Map<AreaId, AreaFacts>,
    val rollup: RollupFacts,
    val items: ItemFacts,
    val history: HistoryFacts,
    val pulse: PulseFacts,
    val cues: CueFacts
)
```

Every field is non-nullable or explicitly optional. No map lookup can return null at realization time.

### 3.1 to 3.6, the fact classes

Declared as Kotlin so nullability and type are never inferred.

```kotlin
data class WindowFacts(
    val startInstant: Long, val endInstant: Long, val dayCount: Int,
    val totalEvents: Int, val completions: Int, val additions: Int,
    val promotions: Int, val swaps: Int, val deletions: Int,
    val focusStarted: Int, val focusCompleted: Int, val focusAbandoned: Int,
    val focusSecondsTotal: Long, val focusMinutesTotal: Int,
    val activeDays: Int,
    val busiestDayKey: String?,          // null when totalEvents == 0
    val busiestDayCount: Int,
    val eventsByPartOfDay: Map<PartOfDay, Int>,
    val netFlow: Int                     // completions - additions
)

/** One per area with lifetime history. Archived and tombstoned areas are ABSENT from the map. */
data class AreaFacts(
    val areaId: AreaId,
    val nameSnapshot: String, val colorHex: String,
    val eventsInWindow: Int, val completionsInWindow: Int, val additionsInWindow: Int,
    val shareOfEvents: Double,           // 0.0 when window.totalEvents == 0
    val hasActiveItem: Boolean,
    val activeItemId: ItemId?, val activeItemTitleSnapshot: String?, val activeItemAgeDays: Int?,
    val queueLength: Int, val queueLengthAtWindowStart: Int, val queueDelta: Int,
    val queueDrainedFrom: Int?,          // the height of the fall to nothing, null when none
    val daysSinceLastEvent: Int,         // Int.MAX_VALUE when never
    val lifetimeEvents: Int, val lifetimeCompletions: Int,
    val ageDays: Int, val isNew: Boolean, // isNew == ageDays < 14
    val focusSecondsInWindow: Long, val focusSessionsInWindow: Int
)

data class RollupFacts(
    val areasWithEvents: Int, val areasTotal: Int, val areasIdle: Int,
    val dominantAreaId: AreaId?,         // null on a tie, or when all are zero
    val dominantShare: Double,
    val neglectedAreaIds: List<AreaId>,  // lifetimeEvents >= 5, daysSinceLastEvent >= 7, !isNew
    val dormantReturnedAreaIds: List<AreaId>,
    val queueDrainedAreaIds: List<AreaId>,   // queueDrainedFrom >= 3
    val queueGrowingAreaIds: List<AreaId>,
    val freshStartAreaIds: List<AreaId>
)

data class ActiveItem(val itemId: ItemId, val titleSnapshot: String,
                      val ageDays: Int, val areaNameSnapshot: String)
data class CompletedItem(val itemId: ItemId, val titleSnapshot: String,
                         val areaId: AreaId, val areaNameSnapshot: String, val daysActive: Int)

data class ItemFacts(
    val activeByArea: Map<AreaId, ActiveItem>,
    val longestActiveItemId: ItemId?, val longestActiveDays: Int,   // 0 when none
    val completedInWindow: List<CompletedItem>,
    val medianDaysToComplete: Int?       // null under 3 completed items
)

data class HistoryFacts(
    val daysSinceInstall: Int, val weeksOfData: Int, val isFirstWeekEver: Boolean,
    val lifetimeCompletions: Int, val lastWeekCompletions: Int?,
    val weekCompletionsSeries: List<Int>,   // oldest first, up to 12
    val weekQueueSizeSeries: List<Int>, val weekTotalEventsSeries: List<Int>,
    val weekOverWeekDelta: Int?,
    val completionsTrend: Trend, val queueSizeTrend: Trend, val activityTrend: Trend,
    val dominantAreaLastThreeWeeks: List<AreaId?>,   // oldest first, nulls allowed
    val personalBestWeekCompletions: Int, val personalBestWeekKey: String?,
    val weeksSincePersonalBest: Int?,
    val mostRecentBetterWeekKey: String?,   // newest week STRICTLY exceeding this one
    val longestEverActiveDays: Int, val longestEverActiveItemId: ItemId?,
    val personalBestFocusMinutesWeek: Int,
    val firstEverFlags: Set<FirstEver>      // present only in the window where each first occurred
)

data class AnsweredPulse(val dateKey: String, val family: FamilyKey, val subjectId: String?,
                         val responseKey: String, val responseLabel: String,
                         val isPositive: Boolean)

data class PulseFacts(
    val answeredLifetime: Int, val answeredInWindow: Int,
    val positiveInWindow: Int, val flaggedInWindow: Int,
    val lastGeneratedFamily: FamilyKey?, val lastGeneratedDateKey: String?,
    val recentAnswers: List<AnsweredPulse>,          // newest first, up to 30
    val answersByFamily: Map<FamilyKey, List<AnsweredPulse>>
)
```

**Three rules that the type signatures cannot express and that matter more than the types.**

**Archived and tombstoned areas are absent from `AreaFacts` entirely.** They cannot reach a rule, so they cannot reach a sentence. Prohibition 3 enforced structurally rather than by checking.

**`shareOfEvents` is the most misused fact in the system.** Any rule using it must also require a minimum `eventsInWindow`, otherwise one event in a one-event week reads as 100 percent concentration. Every share-based rule carries such a floor and a catalog test enforces it.

**`mostRecentBetterWeekKey` is strictly greater, not greater or equal.** If no week beats the current one it is null, `your most active week since X` must not fire, and a personal-best family applies instead. Getting this backwards produces a claim that is subtly false, which is worse than obviously false.

**No streak facts exist.** Deliberately. No `currentStreak`, no `longestStreak`, no `daysInARow`. Their absence makes it structurally impossible for streak language to appear by accident. Do not add them.

**`queueDrainedFrom` is a transition and not a difference of two boundaries.** It is the height an area's queue fell from, in one uninterrupted fall to nothing that has held to the window end, and null when no such fall happened inside the window. Read it backwards from the window end through `TrailQueries.queueSizeSeriesByArea`: a sample at least as large as the one after it is still part of the fall, and the first sample smaller than its successor is the moment something arrived, which puts the top of the fall at that successor. So 5, 4, 3, 2, 1, 0 reads five; 0, 3, 1, 4, 0 reads four; and 0, 5, 0, 2, 0 reads two rather than five, because two things arrived after the five left and `{n} things left {areaName}, and nothing replaced them` would be false of the larger number.

**It exists because a boundary pair cannot see a drain.** `queueLengthAtWindowStart` and `queueLength` are the same two numbers for a week that opened holding five and closed holding nothing and for a week that built five on Tuesday and finished them on Saturday, and only the second is what `CORPUS_1_PULSE.md` 10's `{areaName}'s queue went from {n} to nothing` and `CORPUS_2_REPORT.md` 2.17's `{areaName} cleared its entire queue this week` describe. `queueDrain`, `clearing` and `queueDrained` were dark across eleven simulated years for exactly that reason, and no criterion could approximate it, because `queueLength`, `queueLengthAtWindowStart` and `queueDelta` are all read at the same two instants.

**Null rather than zero, for both of the cases where nothing drained**, exactly as in `dormantDaysBeforeReturn`, which this mirrors: that field measures the gap an area returned from rather than the gap since the window opened, and this one measures the queue an area drained from rather than the queue it happened to hold at a boundary. The queue is not empty now, or it is empty and nothing fell to get it there. A rule cannot tell those apart and must not be able to.

**It carries no claim about how the items left.** A queue also empties by deletion. `RuleBuilders.drainedByFinishing` requires `completionsInWindow >= queueDrainedFrom` and is carried by both drain families, because every sentence on both benches says somebody finished something.

**Phase 5, three readings this section leaves open, resolved and recorded.**

- **`busiestDayKey` on a tie.** 3.1 makes `dominantAreaId` null on a tie and says nothing about this field, so the tie had to go somewhere. It resolves to the **earliest** day, which is the day the peak was first reached. That is not enough on its own: a sentence of the shape `Tuesday carried the week` is false on a three way tie whichever day wins, so the family that names the day carries a floor requiring `busiestDayCount` to be a real share of `totalEvents`, exactly as every share based rule carries an event floor
- **`focusAbandoned` counts `FOCUS_ENDED_EARLY`.** The event was renamed in the Addendum 01 schema window because the log cannot know a session was abandoned. The field keeps the name 3.1 gives it. **A session with no terminal event is in neither count**, so `focusStarted` may exceed `focusCompleted` plus `focusAbandoned` and no rule may infer the difference
- **Every band is present in `eventsByPartOfDay`, including zeros.** A share is a division, and a missing denominator term produces a set of percentages that do not reach a hundred with nothing on the screen to explain why

`responseLabel` is stored verbatim in the `PULSE_ANSWERED` event so a callback quotes what the user actually saw, not a label reworded in a later app version.

### 3.7 CueFacts

Computed over a **12 week** window, because a cue must be a pattern rather than an accident. Used only by layer 6.

```kotlin
data class CueFacts(
    val strongestWeekday: Weekday?, val strongestWeekdayConfidence: Double,
    val quietestWeekday: Weekday?,
    val productiveBand: PartOfDay?, val productiveBandShare: Double,
    val focusTypicalWeekday: Weekday?, val focusTypicalBand: PartOfDay?,
    val addingBand: PartOfDay?, val weekdayOnly: Boolean, val hasStableRhythm: Boolean
)
```

**Confidence thresholds, all mandatory.** A cue may be used only if drawn from at least 6 weeks of data, holding in at least 60 percent of those weeks, over an underlying count of at least 8 events. If nothing clears these, `hasStableRhythm` is false and layer 6 may not produce a plan.

**An invented cue is worse than no plan**, because it makes a claim about the user's life the user knows to be false.

**Phase 5.** `CueFacts` is extracted and gated on all three thresholds already, and **nothing reads it**. Layer 6 is phase 9b. It is built now because it is a fact extraction problem rather than a guidance problem, and because a report pattern family wants a weekday distribution that only this class holds; that family has no rule for exactly that reason, since 3.7 restricts these facts to layer 6.

---

## 4. Layer 2: The rule catalog

**Rules contain no strings.** This is what lets the catalog reach several hundred entries without the code growing, and what makes rules testable independently of copy.

```kotlin
data class ClarityRule(
    val key: RuleKey,
    val purpose: Set<Purpose>,        // PULSE, REPORT_HEADLINE, REPORT_OBSERVATION,
                                      // REPORT_PATTERN, MOMENTUM_HEADLINE, AREAS_BANNER
    val family: FamilyKey,
    val subject: SubjectSelector,     // NONE, AREA, ITEM, or a specific extractor
    val criteria: List<Criterion>,
    val priority: Int,                // tie break only
    val horizonDays: Int,
    val unflattering: Boolean,        // drives register selection, see 7.4
    val requiresCallback: CallbackRequirement? = null
)

data class Criterion(
    val id: String,
    val describe: String,             // plain English, for simulator output
    val test: (FactSet, Subject?) -> Boolean
)
```

**Specificity** is `criteria.size`, never authored. A rule requiring four conditions beats one requiring two, because it describes a narrower and therefore more surprising situation. This is the whole mechanism behind the illusion. Rules must never be padded with trivially true criteria, and a test asserts no criterion passes on more than 90 percent of a large simulated fact corpus.

**Subject.** The rule is evaluated once per candidate subject, so it can qualify for Work and not Health in the same window. Escalation is tracked per `(family, subjectId)`, so nine days on one item and three on another are independent ladders.

**Horizon.** The maximum age of the oldest fact referenced. A callback to last week is attentive; one to fourteen months ago in the wrong context is uncanny. Current-window rules use the window length; `HistoryFacts` rules declare their own, typically 90 or 180 days.

**Phase 5, what "the oldest fact referenced" turned out to mean.** Three ages are generic and cover almost everything: the window itself, the subject, and a quoted answer. An area subject's age is **how long it has been still**, deliberately not how old the area is, because an area's age is not a fact any line renders and counting it would put every area older than ninety days out of reach of every rule with a ninety day horizon, which is most of the areas in a year old install. Those three miss one case, and it is the case the horizon exists for: a family whose sentence names **a week** rather than a subject. `mostActiveSince` renders the month of a week that beat this one, and that week can be two years old while every other fact in the set is seven days old. Those families are enumerated in the selector with the week key each one would name.

**Callback requirement.**

```kotlin
data class CallbackRequirement(
    val family: FamilyKey, val withinDays: Int,
    val responseKey: String?,     // null means any answer
    val subjectMustMatch: Boolean
)
```

Resolved against `PulseFacts.recentAnswers` before the rule can qualify, with the resolved answer attached so the realizer can quote the exact label. A rule with an unresolvable callback does not fire, and never degrades into a version without the callback, because the sentence was authored around it.

---

## 5. Layer 3: Selection

Deterministic, in this exact order. Any deviation produces device divergence.

1. **Qualify.** For each rule matching the purpose, for each subject its selector yields, evaluate every criterion. All must pass
2. **Resolve callbacks.** Drop unsatisfiable ones
3. **Horizon filter.** Drop pairs referencing a fact older than `horizonDays`
4. **Repeat filter, Pulse only.** Drop pairs whose family equals `PulseFacts.lastGeneratedFamily`
5. **Cooldown filter.** Drop pairs whose `(family, subjectId)` fired within `cooldownDays`
6. **Rank.** Specificity descending, then priority descending, then `rule.key` ascending. The final key sort removes the last ordering ambiguity and must be present even though it rarely matters
7. **Take the head.** Empty returns `Silent`

For `REPORT_OBSERVATION`, which needs 2 to 4, take the head then repeat from step 6 excluding any rule sharing a family with an already selected one, **and applying the incompatibility matrix in section 9**, until four are chosen or the list is empty. **Never pad to reach a minimum.**

### 5.1 Deliberate silence

If the highest available specificity is 1, meaning nothing more interesting than a single bare condition applies, Pulse returns `Silent` with reason `DELIBERATE_SILENCE` on days where `stableHash(dateKey) % 3 == 0`.

This is the only place the engine chooses not to speak when it could. A Pulse appearing every single day becomes wallpaper; one that occasionally does not appear reads as discretion. Deterministic, so both devices agree, and it never suppresses a specificity 2 or higher observation.

**Phase 5.** The bucketing is done through a sign safe helper rather than a bare `% 3`. `stableHash` is signed and the sign of a Kotlin remainder follows the dividend, so `% 3 == 0` is correct here and would invite the next person to write `% 3 == 1` somewhere and get a third of the days on one platform and a sixth on another.

---

## 6. The families

### 6.1 Pulse families

Full language in `CORPUS_1_PULSE.md`.

| family | trigger | subject | escalation keyed to |
|---|---|---|---|
| `concentration` | one area at 70 percent or more, minimum 4 events | area | share and event count |
| `spread` | 3 or more areas, none above 50 percent, minimum 5 events | none | area count |
| `throughput` | completions exceed additions, minimum 2 completions | none | net flow |
| `accumulation` | additions exceed completions by 2 or more | none | gap magnitude |
| `persistence` | an active item at 3 or more days | item | age in days |
| `switching` | one or more swaps in window | area | swap count |
| `burst` | 3 or more completions in one area in one day | area | completion count |
| `quietDay` | fewer than 2 events in window | none | consecutive quiet days |
| `rebalance` | activity returned to an area dormant 5 or more days | area | dormancy length |
| `queueDrain` | an area went from 3 or more queued to 0 | area | starting queue size |
| `freshStart` | new area, or first item in an empty area | area | none |

**Tone rules, non-negotiable.** Both response options must feel equally valid read out of context. No option is the good answer. Switching and abandonment language normalizes, never flags. Milestone families get responses that are both positive.

**Response polarity.** The first response of each pair is positive. For `quietDay`, `Recharging` and `Busy elsewhere` are positive and the third is flagged. This boolean is the only interpretation the app ever makes of an answer.

**Phase 5: the escalation fact named in the table above does not exist for every family.** Nine families and three single stages across all six purposes have authored language and no rule, because the fact their trigger names is not declared in 3.1. Two of them are in this table: `switching` is given the area as its subject and the swap count as its escalation fact, and 3.1 declares swaps only for the whole window; `rebalance` is given dormancy length, and the rollup says an area returned but not from how long. `quietDay` stages 2 and 3 and `concentration` stage 3's second branch want a run of consecutive days, which nothing counts.

**Each of those was left without a rule rather than approximated**, and the list lives in code with the missing fact and the corpus line that needs it, so a catalog test fails if a family goes quiet without somebody deciding it should. The approximations available were all nearly right: window active days for a run of consecutive days, the window's swap count for an area's, days since last event for the dormancy an area returned from. Each would fire the family on a shape it does not describe, and the sentence that came out would be arithmetic nobody could fault and a claim about a person's week that was not true. That is the prime directive in section 1, and it is the reason the fact comes first and the rule second. **The fix is a fact in 3.1, not a criterion.**

### 6.2 The Pulse response format, resolved

**Two options, always, except where a family genuinely needs three.** The language panel split on whether to add a universal third. Resolved in favor of two, for a reason the debate missed.

**A third path already exists: not answering.** Dismissing the sheet without responding is a fully supported state with its own representation, the hollow amber ring in the ambient rhythm row. It is never chased, never counted against the user, never mentioned. Someone for whom neither option is honest already has a dignified exit that costs nothing.

Adding `Neither` would produce a response with no signal, which is worse than no response, because it enters the aggregation and dilutes it. `quietDay` carries three because three are genuinely distinct there, which demonstrates the format flexes when it must.

### 6.3 Report families

Full language in `CORPUS_2_REPORT.md`.

**Headline**, one per report, under 8 words: `quietWeek`, `netOutflow`, `netInflow`, `singleFocus`, `balanced`, `focusProtected`, `personalBest`, `mostActiveSince`, `decliningActivity`, `risingActivity`, `comeback`, `queuePressure`, `clearing`, `steadyPace`, `fragmented`, `firstWeek`, `datedFallback`.

**Observations**, 2 to 4: `singleFocus`, `intakeVsOutput`, `focusInvestment`, `neglectedArea`, `completionSplit`, `selfReportVsData`, `quietWeek`, `queuePressure`, `areaRevival`, `persistentItem`, `personalBest`, `mostActiveSince`, `dayShape`, `timeOfDay`, `switchingBehavior`, `focusAbandonment`, `queueDrained`, `steadyPace`, `firstMilestone`, `areaBalance`, **`hardStretch`**.

**Patterns**, at most one, requiring `weeksOfData >= 3`: `shiftingFocus`, `growingQueues`, `improvingThroughput`, `decliningActivity`, `areaGoneQuiet`, `consistentRhythm`, `narrowingFocus`, `broadeningFocus`, `focusHabitForming`, `focusHabitFading`, `reportedVsActual`, `queueEquilibrium`, `weekendShift`, `abandonmentPattern`, `comebackPattern`, `insufficientData`.

**Closings**, produced by layer 6: eight plan-action families plus `trustThePace`, `letItBe`, `noRhythmYet` and `review`.

### 6.4 The difficulty register

`hardStretch` fills a gap identified by the review panel's therapist: nothing in three thousand lines could acknowledge a hard stretch as hard.

**Trigger.** Three or more consecutive quiet weeks combined with growing queues, or a sustained decline across four weeks.

**Hard constraints, all mandatory, all enforced in code.**
- The grammatical subject is always the pattern, never the person
- It never names or infers an emotional state
- It never asks a question
- It never offers help, resources, or advice
- It fires at most once every six weeks
- It cannot appear alongside a plan
- It cannot appear alongside `selfReportVsData`

If any line reads as consolation rather than observation, the family is removed rather than rewritten. That was the editor's condition for accepting it.

### 6.5 Momentum and banner families

Full language in `CORPUS_3_MOMENTUM.md`.

`MOMENTUM_HEADLINE`, under twelve words, only real area names: `steadyStretch`, `quietStretch`, `comeback`, `balancedWeek`, `singleAreaWeek`, `strongPace`, `firstDays`, `cleanSlate`.

`AREAS_BANNER`: `weekStarting`, `weekBuilding`, `weekStrong`, `weekQuiet`, `weekMixed`. Recomputed at most once per hour of app use, throttled in the ViewModel and not in the engine.

**Momentum observes and never interprets.** Its families must never contain because, suggests, means, or any causal construction. A test enforces this.

---

## 7. Layer 4: Realization

### 7.1 Structure

```kotlin
data class PhrasingFamily(
    val key: FamilyKey, val cooldownDays: Int,
    val stages: List<EscalationStage>,
    val responses: List<ResponseOption>   // Pulse only
)

data class EscalationStage(
    val index: Int, val threshold: ClosedRange<Int>, val variants: List<Variant>
)

data class Variant(
    val key: VariantKey,
    val register: Register,     // PLAIN, OBSERVATIONAL, REFLECTIVE,
                                // EDITORIAL (Report only), NEUTRAL_AGENT
    val lengthBand: LengthBand, // COMPUTED at catalog load, never authored. See 7.5
    val statement: Template, val question: Template?, val requiredSlots: Set<SlotKey>
)
```

`Template` is an authored string with typed slot markers. **Never assembled at runtime from fragments belonging to another family.**

**Phase 5: responses are pairs on a stage, not a flat list on a family.** 7.1 declares `PhrasingFamily.responses` as a flat `List<ResponseOption>`. `CORPUS_1_PULSE.md` authors responses as pairs, per stage, six or seven per stage, each pair written to be read against the other half and to pass the equal validity test in 11.3 **as a pair**. Flattening them loses the pairing, which is the only thing that makes that test mean anything, so the pairs live on the stage and the family has no flat list. CLAUDE.md's authority order gives the corpus the last word on the shape of a sentence, and a response pair is one.

### 7.2 Slots and number rendering

```kotlin
sealed interface Slot {
    data class Text(val key: SlotKey, val value: String) : Slot
    data class Count(val key: SlotKey, val value: Int,
                     val singular: String, val plural: String) : Slot
    data class Days(val key: SlotKey, val value: Int) : Slot
    data class Percent(val key: SlotKey, val value: Int) : Slot
    data class DateRef(val key: SlotKey, val weekKey: String, val display: String) : Slot
}
```

**Rules applied centrally, never by a template author:**

- Percentages render as `78 percent`, never `78%`
- Counts of one render the singular noun. `1 item`, not `1 items`. Both forms carried, no default
- Counts of two to nine render as **words in Pulse and Momentum, digits in the Report**. A deliberate register difference enforced by the renderer
- Counts of ten or more always render as digits
- Zero never reaches a template; a rule that could produce zero must carry a criterion preventing it
- `Days` renders `yesterday`, `two days`, `nine days`, `three weeks`, `two months` at the appropriate magnitude
- `DateRef` renders a month name, `since March`, never a numeric date
- American spelling only. A catalog test asserts no British form appears

**Slot completeness is checked before rendering.** Any missing required slot drops the candidate and the next ranked selection is realized. A template must never render with a visible marker. A test walks the catalog asserting every variant's required slots are producible from the facts its rules guarantee.

### 7.3 Escalation

Keyed to **magnitude**, not firing count. Nine days is stage 2 whether the family fired once or fifty times.

**Where the thresholds live.** The engine declares `EscalationStage.threshold` as a range but does **not** define the values. **Every threshold is authoritative in the corpus stage headers**, which are written as prose ranges over the family's escalation fact.

```
CORPUS_1_PULSE.md    ## Stage 2, six to thirteen days      ->  6..13   over activeItemAgeDays
CORPUS_1_PULSE.md    ## Stage 1, gap of two to three       ->  2..3    over the intake gap
CORPUS_1_PULSE.md    ## Stage 3, four or more consecutive quiet days  ->  4..MAX
```

Parse them into `ClosedRange<Int>` at catalog load. **A stage header carrying a compound condition, such as `Stage 3, ninety five percent and above, or four or more consecutive days`, becomes two rules pointing at the same stage**, not one range. Do not attempt to encode a disjunction inside a range.

The escalation fact for each family is named in the table in 6.1. The stage ranges for one family must be contiguous and non-overlapping, and a catalog test asserts this.

**Phase 5: a stage header that names no number gets no range.** Some headers are qualitative, `clear imbalance toward intake` and the like. The parser gives those a qualitative condition rather than a guessed range, and the rule that points at such a stage carries its own criteria. 7.3 gives the corpus the last word on every threshold, and a threshold no author wrote is not the corpus's word. The other refusal is the one this section already states, made mechanical: the compound header splits on `, or ` and never on a bare ` or `, because `four or more consecutive days` is one branch and splitting it on ` or ` produces two nonsense ones.

Example ladder for `persistence`, over `activeItemAgeDays`: stage 1 days 3 to 5, a note; stage 2 days 6 to 13, pointed; stage 3 days 14 to 29, comparative; stage 4 days 30 and above, historical. That matches the four stage headers in `CORPUS_1_PULSE.md` exactly, and where this document and the corpus ever disagree on a number, **the corpus wins**, because it is the file an author edits.

Stage 4 references `HistoryFacts.longestEverActiveDays` and its rule carries a criterion asserting the item genuinely holds that record. Without it the sentence would be a lie the moment a longer-running item existed.

**Monotonicity.** For a given `(family, subjectId)`, never show a lower stage than previously shown while the condition remained continuously true. `FiringHistory` carries `lastStageBySubject`, derived from prior `PULSE_GENERATED` events. This prevents saying `nine days now` on Tuesday and `has been active for three days` on Wednesday because a promotion reset an age calculation.

**Phase 5, and this rule has an obvious implementation that is wrong.** The obvious one is to raise the stage to the one last shown. Stage 2 of `persistence` is authored around six to thirteen days, so rendering it for an item whose age has just been reset to three would say `going into its second week` about a three day old item. A false sentence is worse than a missing one, so the pair is **dropped** instead and the family says nothing until the magnitude catches up.

**Continuity has to be bounded or the ladder never resets.** This section says the ladder resets when the condition genuinely lapses, and nothing in 3.1 records when that happened. The bound used is the family's own cooldown plus the window it describes: inside that the family was either speaking or forbidden from speaking, so the condition plausibly held, and beyond it the engine has no evidence either way and the ladder starts again.

**Reset.** When the condition genuinely lapses, the ladder resets and a new active item starts at stage 1.

**Cooldown.** Each family declares `cooldownDays`, during which the same `(family, subjectId)` pair cannot fire again. Separate from the no-repeat rule, which covers only yesterday.

| family | cooldownDays | reasoning |
|---|---|---|
| `persistence` | 3 | the condition persists by definition, so it must be able to escalate |
| `concentration` | 4 | |
| `accumulation` | 4 | |
| `throughput` | 4 | |
| `spread` | 5 | |
| `quietDay` | 5 | |
| `switching` | 7 | |
| `burst` | 10 | rare and high signal |
| `queueDrain` | 14 | |
| `rebalance` | 21 | a revival is not interesting twice in a month |
| `freshStart` | 30 | per subject, so a new area still fires |

Report families use a flat 14 days per `(family, subjectId)`, except `selfReportVsData` which never repeats on the same subject at all, and `hardStretch` at 42 days per 6.4.

**The pattern section waits three weeks on top of that, and the floor sits on the surface rather than on the declaration.** Every `REPORT_PATTERN` selection waits at least 21 days per `(family, subjectId)`, keyed the way the Pulse is keyed and read out of `FiringHistory` by the same call, so this is the instrument above and not a second one. It is a floor on the selection because one family key serves two surfaces: `decliningActivity` is a headline family and a pattern family at once and the two share one cooldown key, so a longer number on the declaration would hold the headline back as well, and only the pattern section was ordered to wait.

**Counted in reports rather than in days, because on this surface the two are not the same.** A report is recorded against its week start key and selected against its week end, so two consecutive reports are 14 days apart on the only clock `FiringHistory` keeps. A cooldown of `C` days therefore holds a pair out of the next `ceil(C / 7) - 2` reports, or out of none where that is not positive, and the head of the ranking rotates among `ceil(C / 7) - 1` pairs. **The flat 14 holds nobody out of anything**, which is what the facts phase measured from the other end: 419 pattern slots across a simulated year, 416 of them filled, and three families holding 402. Three weeks rotates two. Rotating seven would take eight. `PatternCooldownTest` measures every figure in this paragraph against the real catalog.

### 7.4 Register selection

Five registers, selected in this order:

1. **`NEUTRAL_AGENT` when the rule is marked `unflattering`.** If the family has a `[N]` variant at the selected stage, prefer it
2. **Time of day, Pulse only.** Dawn and midday prefer `PLAIN` and `OBSERVATIONAL`; evening prefers `REFLECTIVE`
3. **Editorial budget, Report only.** At most two `EDITORIAL` leads per report. A third is re-realized in `OBSERVATIONAL`
4. **Fallback order:** `REFLECTIVE`, `OBSERVATIONAL`, `PLAIN`

**Which rules carry `unflattering = true`.** Enumerated so it is not a judgment call:

`intakeVsOutput` at stage 2 (intake exceeding output), `neglectedArea` at both stages, `queuePressure`, `persistentItem` at stages 3 and 4, `switchingBehavior` at stage 2, `focusAbandonment`, `decliningActivity`, `quietWeek`, `hardStretch`, `singleFocus` at stage 2, and the pattern families `growingQueues`, `decliningActivity`, `areaGoneQuiet`, `narrowingFocus`, `focusHabitFading`, `abandonmentPattern`.

Everything else is `false`. A family that is neutral or positive never uses the neutral-agent register, because making the fact the subject of a good week reads as withholding credit.

**Phase 5: two entries above name a stage their corpus family does not have.** `persistentItem` is marked at stages 3 and 4 and `switchingBehavior` at stage 2, and both are single stage families in `CORPUS_2_REPORT.md`. Marking the whole family would over apply a qualification this section wrote deliberately; marking none of it would leave the three neutral agent lines authored for the switching family unreachable. So the qualification survives as a property of the **rule** rather than of the stage: the catalog declares two rules per family, split at the magnitude the corpus already states for the matching Pulse ladder, and only the higher one is unflattering. `persistentItem` splits at fourteen days, the start of `persistence` stage 3; `switchingBehavior` splits at two swaps, the start of `switching` stage 2.

**Phase 5: nothing declares which lead is notable enough for the editorial register.** Step 3 caps a report at two editorial leads, and `CORPUS_2_REPORT.md` says the register is reserved for leads that have earned it with a genuinely notable fact, which is a condition on the lead and not on the report. Nothing in 3.1 or section 4 carries a notability flag, so the realizer uses the one measure of notability the engine already computes: **specificity**, at three or more. A rule that required four things to be true at once describes a narrower situation than one that required two, which is the whole mechanism of section 5, and it is the same thing an editor means by a fact worth writing up. Two is the ordinary shape of a rule in this catalog, a condition and the floor that keeps it honest, so three is where a rule starts describing a situation rather than a number.

**On `NEUTRAL_AGENT`.** The observation corpus otherwise uses transitive second person with the user as grammatical agent throughout. *You added 9 things and finished 6.* Where the numbers are unflattering, sustained agentive framing reads as attribution of responsibility even when no evaluative word appears. The neutral-agent register makes the fact the subject instead: *Nine things arrived. Six left.*

**Constraint, and it matters.** This is **not passive voice and not agent deletion**. *Nine things were added by you* is banned outright. The construction is intransitive or existential with the fact as subject. A line reading as evasion is worse than the agentive original.

### 7.5 Length variation, Report only

**`lengthBand` is computed at catalog load time from the realized word count, never authored.** Counting a template's words after replacing every slot marker with a single placeholder token gives a stable band:

- `SHORT` under 7 words
- `MEDIUM` 7 to 14
- `LONG` 15 to 24

Computing rather than tagging is deliberate. A hand-applied tag drifts the moment a line is edited, and 213 leads is too many to keep honest by hand. A handful of `[S]` markers appear in `CORPUS_2_REPORT.md` as authoring hints where a short line was written on purpose; **they are advisory and the computed value always wins.**

The composer may not select two consecutive leads from the same `lengthBand`. This forces rhythm without anyone writing to a word count.

Related: **no more than two parallel numeric clauses consecutively.** Where a third would follow, the composer drops it or re-realizes it at a different length. The three-part list is a rhetorical reflex and once a reader sees it they cannot stop seeing it.

### 7.6 Variant choice: deterministic, apparently random

Within the selected `(family, stage, register)` bench:

1. Filter out variants used within 90 days, from `FiringHistory.variantsUsed`
2. If empty, use the full bench minus the least recently used, and record a bench exhaustion diagnostic
3. Sort remaining by `stableHash(dateKey + variantKey)` ascending
4. Take the head

`stableHash` is a specified, implemented, tested **FNV-1a 64 bit**. Never `String.hashCode()`, which is not guaranteed stable across platforms. Two devices computing this on the same `dateKey` reach the same variant with no shared state, and it reads as random to a user who never sees the ordering.

**`FiringHistory` is derived entirely from `PULSE_GENERATED`, `REPORT_GENERATED` and `PLAN_OFFERED` events. Never from DataStore.** A device that has just merged a log must compute the same next variant as the device that produced it, and DataStore does not merge.

### 7.7 Multiplying the surface without concatenating

Hot families use frames with two or three interchangeable clause slots. Thirteen authored fragments yield 36 surfaces.

**The rule that stops this becoming slop: fragments are never generic and never shared across families or registers.** A clause bench belongs to exactly one family and one register. There is no global opener pool. A test asserts no fragment string appears in two families, and no rhetorical construction appears in more than two.

Frames are authored and reviewed as wholes: an author writes out all 36 resulting sentences during review, not just the thirteen parts.

---

## 8. Layer 5: Validation

Every candidate arrives with the facts that produced it. A vetoed candidate causes the next ranked selection to be realized. If everything is vetoed, the engine returns `Silent`.

```kotlin
data class Candidate(
    val ruleKey: RuleKey, val familyKey: FamilyKey, val variantKey: VariantKey,
    val stage: Int, val register: Register, val lengthBand: LengthBand,
    val rendered: String, val renderedQuestion: String?,
    val slots: Map<SlotKey, Slot>, val sourceFacts: Map<SlotKey, FactRef>,
    val namedAreaIds: Set<AreaId>, val namedItemIds: Set<ItemId>
)
```

The checks, in order, all mandatory:

1. **Area existence.** Every id in `namedAreaIds` has `eventsInWindow > 0`. Not merely exists, not merely unarchived. Has real events in the window being described
2. **Item existence.** Every id in `namedItemIds` resolves and is not tombstoned
3. **Number provenance.** Every `Count`, `Percent` and `Days` slot carries a `FactRef`, and re-reading that fact yields the same value. An untraceable number is a veto
4. **No zeros.** No numeric slot has value 0
5. **Snapshot usage.** Enforced structurally: the realizer receives only the `FactSet`, whose name fields are snapshots by construction, and has no access to live entity tables
6. **Callback fidelity.** Any quoted response label matches the stored `responseLabel` exactly, case-insensitively after the renderer's sentence-position lowercasing
7. **Unfilled markers.** No residual slot syntax
8. **Forbidden vocabulary.** None of the banned words. No em dash, en dash, emoji, non-ASCII outside standard punctuation, or British spelling
9. **Length.** Report headlines under 8 words. Momentum headlines under 12. Pulse observations under 30. Closing lines under 22
10. **Register integrity.** A `NEUTRAL_AGENT` variant contains no passive construction with a deleted agent. Checked by pattern against banned forms including `were added`, `was completed`, `have been`

Checks 1 through 4 are the integrity core. **The veto path for each must be reachable in a unit test** that deliberately constructs a violating candidate and asserts the veto. A validator whose failure branch is never executed is a validator nobody has verified.

**Phase 5, three things this list leaves open.**

- **Check 4 vetoes a negative as well as a zero.** Section 8 does not say so in as many words, and it is the same failure with a sign on it: a minus rendered into a sentence is a number nobody authored. A family that wants to speak about a gap or a decline is passed the **magnitude**, which is what `intakeVsOutput` and every corpus line reading `{k} more things` was written against
- **Checks 7, 8 and 10 read the sentence with the person's own strings masked out.** An area is named by the person who made it. Somebody may reasonably spell a name the way they were taught, put an exclamation mark in an item title, or write one in a language this file cannot spell. Vetoing the sentence would silence the engine over somebody's own vocabulary, and the app already shows that exact string on every other screen. **The words the app chose are the words the app is answerable for.** Check 9 is the exception and measures the sentence as it will appear, because a long name really does make a long headline
- **Check 5 has nothing a correct realizer can fail**, and does not pretend otherwise. It is enforced by the shape of layer 4, which receives only the `FactSet`. What it can still do is compare in both directions a name can go wrong, and it does that rather than passing vacuously

**The order is data.** The checks are a list in the order this section numbers them and a test asserts it holds each one exactly once, so a candidate that breaks several is reported against the lowest numbered one and the detail names the most fundamental thing wrong with it.

---

## 9. Composition rules, Report only

The Report emits eight to ten sentences at once, creating a failure class Pulse cannot have: two individually true sentences that contradict each other.

### 9.1 The incompatibility matrix

No report may contain both members of any pair:

| A | B | why |
|---|---|---|
| `singleFocus` | `areaBalance` | one says narrow, the other broad |
| `quietWeek` | `personalBest` | contradictory |
| `quietWeek` | `focusInvestment` | a quiet week that also protected eight hours reads as broken |
| `intakeVsOutput` stage 2 | `intakeVsOutput` stage 3 | opposite directions |
| `neglectedArea` | `areaRevival` on the same area | the same area cannot be both |
| `steadyPace` | `personalBest` | a record is not steady |
| `steadyPace` | `mostActiveSince` | same |
| `selfReportVsData` | a declining headline | pile-on |
| `selfReportVsData` | `neglectedArea` | pile-on |
| `selfReportVsData` | itself, same subject, ever | rarity is what gives it force |
| `hardStretch` | any plan | a hard stretch does not also get homework |
| `hardStretch` | `selfReportVsData` | pile-on |
| headline `quietWeek` | any observation but `quietWeek`, `neglectedArea`, `persistentItem`, `hardStretch` | the headline set the frame |
| headline `singleFocus` | `areaBalance` | frame conflict |
| headline `balanced` | `singleFocus` | frame conflict |

Applied after ranking, before taking the second, third and fourth observations. On conflict the lower-ranked family is dropped and the next non-conflicting one considered.

### 9.2 Further rules

- **Headline first.** Selected before anything else, constraining everything after. A conflicting observation is excluded entirely, not merely deprioritized
- **One area, two mentions.** No area named in more than two of the four observations, or the report reads as being about one area rather than about the week
- **Editorial budget.** At most two `EDITORIAL` leads
- **Length variation.** No two consecutive leads from the same band
- **Parallel clause cap.** No more than two consecutive parallel numeric clauses
- **The closing must follow.** Layer 6 may only advise on something the report actually mentioned
- **Number consistency.** The composer holds a map of every rendered numeric slot and its `FactRef` across the whole report and vetoes the entire report if one `FactRef` renders two different values, which indicates a fact recomputation bug rather than a copy problem

---

## 10. Layer 6: Guidance

### 10.1 Why it exists

The engine otherwise only observes. The research is blunt: insights from reflection on personal data are frequently not actionable, and most systems assume reflection happens naturally once data is visualized, when the literature says reflection needs explicit support.

The strongest available intervention is a change in the grammatical form of the closing line. Implementation intentions produce d = 0.65 across 94 studies, and the mechanism is specific: a goal intention names the destination, an implementation intention specifies the when, where and how.

**And the part that fits this app: we already know the user's cues.** Strongest day, productive hours, when focus sessions actually happen. All already computed for other purposes.

### 10.2 The righting reflex, and how it is avoided

Motivational interviewing names the failure mode this feature invites: the righting reflex, the tendency to give advice and push solutions, to which an ambivalent person responds by defending the status quo. Giving advice without permission is an MI-inconsistent response. Separately, controlling message phrasing, meaning commands that do not provide choice, produces reactance.

The resolution is sequence rather than hedging, because MI objects to **unsolicited** advice while the implementation-intention requirement governs the form of a plan **already adopted**. Different moments.

**Therefore:**

1. The plan is **offered** in a frame that is explicitly optional and grammatically **nominal**, never imperative
2. The user accepts or declines. Both one tap, both costless
3. On acceptance it is **stored in first person** as a proper if-then, and that is the only form ever shown afterwards

> Offered: *One option for Wednesday morning: ten minutes in Personal before you open Work.*
> Stored: *If it's Wednesday morning, I'll spend ten minutes in Personal before opening Work.*

**The imperative form never exists anywhere in the app.** The directive only ever appears as something the person said about themselves.

### 10.3 Interface

```kotlin
package com.kamsiob.claritynow.domain.guidance

object GuidanceComposer {
    fun compose(appeared: List<Validated>, facts: FactSet, plans: PlanHistory): GuidanceResult
}

sealed interface GuidanceResult {
    data class Plan(val plan: ClarityPlan) : GuidanceResult
    data class Closing(val line: RenderedOutput) : GuidanceResult
    data object Nothing : GuidanceResult
}

data class ClarityPlan(
    val id: String, val weekStartKey: String,
    val frameKey: FrameKey, val cueKey: CueKey, val actionKey: ActionKey,
    val familyKey: FamilyKey, val subjectId: String?,
    val offeredLine: String,          // nominal
    val committedLine: String,        // first person, rendered on accept
    val acceptedAt: Long?, val declinedAt: Long?,
    val resolutionFactRef: FactRef, val resolvedAt: Long?, val resolvedValue: String?
)
```

Same purity contract as `domain.engine`. `PlanHistory` derives from `PLAN_OFFERED` and `PLAN_ACCEPTED` events, never DataStore.

### 10.4 Composition rules

A plan may be produced only when **all** hold:

1. `CueFacts.hasStableRhythm` is true and the specific cue clears its thresholds
2. **The motivating observation actually appeared in the report.** Enforced by passing only `appeared` into layer 6
3. There is a real friction pattern. When barriers are low, plan formation is superfluous, so a straightforwardly good week gets no plan
4. No plan offered in the previous two weeks is still unresolved. Stacking unfinished plans is how this becomes a nag
5. The action is completable inside one week and is a single concrete act
6. The report is not otherwise heavy. A declining headline plus a neglected area, or any `hardStretch`, means no plan

### 10.5 Acceptance and decline

Two options, never one. `I'll do that` and `Not this week`.

- **Accepting** writes `PLAN_ACCEPTED`. Nothing visible happens beyond the pill settling. No toast, no celebration, no bounce, no haptic heavier than an ordinary tap
- **Declining** writes nothing, costs nothing, is never counted, never referenced
- **Ignoring both** is identical to declining
- No reminder, notification, badge, or home screen card. The plan exists in the report and nowhere else

One button is not a choice. Provision of choice was tested as a factor independent of message language, and that is why the decline exists.

### 10.6 Follow-through, and the rule that keeps it safe

**A plan is only ever followed up if it was accepted.** Unaccepted plans vanish without trace.

When followed up, the next report may state the **underlying fact**, never the compliance.

- Permitted: *Personal moved on Wednesday for the first time in three weeks.*
- Forbidden: *You did the thing you said you would.*
- Forbidden: *You planned to spend time in Personal and did not.*

**The negative-case rule, the load-bearing safeguard.** A follow-up observation may appear only when it would have qualified as an ordinary observation on its own merits, independently of any plan. If Personal did not move, the report may say so **only if `neglectedArea` would have fired anyway.**

**Implementation.** Layer 6 does not inject sentences. It sets a **priority boost** on the observation family whose `resolutionFactRef` matches the accepted plan, raising it in the ranking. If that family does not qualify, nothing appears. **The user can never be told about a plan they did not keep, because the mechanism has no way to say it.**

### 10.7 Silence

Across the persona set, layer 6 must return `Nothing` or a non-plan closing on **at least 15 percent** of reports. A report that always has advice is a report inventing problems.

---

## 11. The corpus

### 11.1 Sizing

By expected firing frequency, never evenly. Even sizing produces repeats exactly where they hurt most, because a handful of families fire fifty times a year and most fire four.

| tier | firings per year | variants per stage bench |
|---|---|---|
| hot, roughly 15 families | 40 or more | 60 to 100 |
| warm, roughly 30 families | 5 to 20 | 15 to 30 |
| long tail | under 5 | 4 to 8 |

Current, counted rather than estimated: **620 Pulse lines producing 10,569 surfaces, 737 Report and guidance lines producing roughly 6,100, and 146 Momentum and banner lines producing 496.** Combined, 1,503 authored lines and roughly 17,200 surfaces. Storage is not a consideration; quality is the only constraint, and phase 9 grows the hot families toward the targets above.

**Phase 5 counted the files and found three stated totals had drifted. The facts phase recounted every one of them and corrected the files, because the lines are the ground truth and a stated total is only a claim about them.** `CORPUS_3_MOMENTUM.md` stated 112 Momentum headlines in two places and carries 96, so its volume total is 146 rather than 162 and the combined figure is 1,503 rather than 1,519. Inside the Report volume, section 1's prose stated 176 headlines against its own table's 158, and section 3's stated 128 patterns against 111; in both cases the table was right and the prose was stale. `CORPUS_1_PULSE.md` was correct in every figure, family counts and surfaces included. Every number in the paragraph above is now the counted one. **No corpus line was added or removed to reach it**: correcting a count is not authoring, and the sizing targets in the table above are a separate matter, which phase 9 addresses. A fourth figure was wrong and phase 5 did not catch it, because phase 5 counted lines and this one is a product. Volume 1's surface column sums to 10,569 and its total row stated 10,557, the sum of the eleven families with the twelve acknowledgment lines left out, although the same row's line count includes them. Nothing in the file states that exception, so the total now sums its own column. The alternative reading, that an acknowledgment is not a Pulse surface because it follows an answer rather than being one, is defensible and was rejected for a specific reason: it needs a rule that is nowhere written down, and a total nobody can reproduce from the rows above it is the exact defect this section is correcting. `CorpusTotalsAuditTest` now recounts every stated total in the three corpus files and in this section against the keyed lines beneath them, and fails naming the file, the stated figure and the counted one, so the next drift is caught where it happens rather than discovered years later.

### 11.2 Authoring protocol

1. **Research first.** Before writing any batch, research current AI slop tells in language. Before, not as cleanup
2. **Batches of forty**, one family, one stage, one register
3. **Anchor every batch** with ten already approved lines so voice does not drift between sessions
4. **Judge against simulator output**, never in isolation
5. **Present for approval.** Expect a quarter to a third rejected. A rejection rate near zero means nobody is reading hard enough
6. **Never generate the whole corpus in one pass.** Voice drifts noticeably by sentence 200

### 11.3 Voice rules

Calm, observational, second person where natural, sentence case, active voice, American spelling. Numbers always real.

**Never:** should, failed, behind, streak, hurry, lazy, must, have to, don't forget, you haven't, make sure, try to, remember to, keep it up, well done, great job. No exclamation marks. No rhetorical questions except the Pulse question itself. No advice outside the closing line.

**On the word `behind`.** The ban targets the **evaluative** sense only: falling behind, behind schedule, you are behind. The **spatial** sense is correct and common in this app, because a queue literally has things behind the active item. `The queue behind Rewrite the proposal intro has not moved` is fine. `You are behind on Work` is not.

The build test must therefore match `behind` only in these constructions, not the bare word:
`\b(?:fall(?:ing|s|en)?|get(?:ting)?|slip(?:ping)?|running|are|is|am|were|was)\s+behind\b` and `\bbehind\s+(?:schedule|target|plan|where|the\s+curve)\b`.

**The equal validity test.** Read each Pulse response pair aloud with no context. If one sounds like the answer a good person gives, rewrite both.

**The mirror test.** Read every sentence as though a friend said it about your week. If it would make you defensive, it is wrong. Read decline and neglect families twice: once normally, once imagining the worst week you have had.

**The permission test, guidance only.** Read each action preceded by *You should have*. If it still parses naturally, rewrite it.

---

## 12. The simulator

Build this in phase 5, before a single corpus sentence is written. In `devtools`, debug builds only.

**Built, phase 5.** Eleven personas, a full simulated year each, and a Gradle task that reads the source directories Gradle resolved and fails if the package is missing from the debug source set, present in a release one, or named by any file a release build compiles. Being in `src/debug` is the mechanism; the task is the verification, because the failure mode is silent: nothing breaks the day somebody moves a simulator class into `src/main` so a screen can reach it.

**The simulator writes the engine's own output back into its log**, which is the one thing it does that the app does not. It is not a liberty. `FiringHistory` derives entirely from `PULSE_GENERATED`, `REPORT_GENERATED` and `PLAN_OFFERED`, so the ninety day exclusion, the cooldowns and the ladders only exist in a run that records what it said. A simulator that dropped its own output would show every family at stage one forever and repeat lines it used the day before.

**Inputs.** Synthetic histories: heavy single area, balanced across four, sporadic, abandoning, high focus, low focus, brand new, long dormant with a revival, queue hoarder, fast completer, **and a persona who accepts every plan and completes none.**

**Process.** A full simulated year, engine run day by day for Pulse and week by week for the Report, plus Momentum on each simulated open.

**Output.** Plain text per persona, every invocation annotated:

```
2026-03-14  [pulse]  persistence / stage 2 / reflective / v.persist.s2.r.017
  fired: active item age 9 days (>= 6), area has 4+ events, no swap in window
  facts: activeItemAgeDays=9, areaName=Work, itemTitle=Rewrite the proposal intro
  > Still Rewrite the proposal intro. Nine days now.
  > Deep work, or stuck?

2026-03-15  [pulse]  SILENT (DELIBERATE_SILENCE)
```

**Automated checks over the dump:**

- No variant key appears twice inside 90 simulated days
- No banned word, em dash, en dash, emoji, non-ASCII, or British spelling
- No sentence names an area with no events in its window
- Pulse silence between 8 and 25 percent of days
- Layer 6 silence at least 15 percent of reports
- No family accounts for more than 20 percent of a year's Pulses
- Every stage of every hot family fires at least once
- **Added by the facts phase, and not one of the ten above:** every family the corpus declares fires at least once. Phase 5 read "six of the eleven Pulse families ever fired" out of a dump by hand and nothing watched it afterward, which is possible because a family that never fires leaves no trace in a year of output and every other check here is blind to it. The denominator is the catalog, so the number survives a phase boundary
- No two consecutive Report leads share a length band
- No report contains three consecutive parallel numeric clauses
- **The non-compliance test:** the plan-accepting, plan-ignoring persona produces a year in which no sentence references a plan, a commitment, an intention, or a failure to act. If a reader of that dump could tell plans were accepted, the follow-through implementation has failed and must be **removed rather than tuned**

**Phase 5: all ten are built, four are enforced and six are deferred with a date and an issue. The facts phase added the family coverage reading and rewrote the stage one, so twelve run today, four enforced and eight deferred.** Issue #3 says in advance that the statistical ones cannot pass here, because the corpus is not grown until phase 9 and layer 6 does not exist until 9b. A deferred check is not a skipped one: it runs on every simulation, prints the number it measured, and prints its failures as loudly as an enforced check. What deferral changes is only whether the build goes red.

The four enforced now are the ones whose failure would mean something already built is wrong rather than something not yet written: the vocabulary check, the phantom area check, the visible slot syntax check and the non-compliance test. The first three are already vetoed by layer 5, so a dump containing one is evidence that a sentence reached a surface without passing through the validator. The fourth passes trivially today because layer 6 does not exist, and is enforced anyway so that the day layer 6 arrives, this is already watching.

The readings the deferred checks produce are the baseline phase 9 is judged against and the full tables, with the reasoning, are in `DECISIONS.md` and `docs/BUILD_STATE.md`. **Four measurements exist and the fourth is the current one.** Phase 5, the facts phase, the slot bindings phase and the rules pass, over the same eleven personas and the same simulated year each time.

| reading | target | phase 5 | facts | bindings | current |
|---|---|---|---|---|---|
| Pulse silence, every persona together | 8 to 25 percent of opened days | 76 percent | 73 percent | 68 percent | **68 percent, unchanged** |
| Pulse silence, per persona | the same band | 43 to 98 | 42 to 98 | 40 to 97 | **40 to 97, unchanged** |
| Pulse families that ever fired | 11 of 11 | 6 of 11 | 7 of 11 | 8 of 11 | **8 of 11, unchanged** |
| every family the corpus declares fires | 78 of 78 | not measured | 58 of 78 | 60 of 78 | **65 of 78** |
| every stage of every hot family fires | all | 29 hot, one gap | 31 hot, two gaps | 33 hot, two gaps | **35 hot, the same two gaps** |
| no variant repeats inside ninety days | none | 7,384 | 7,430 | 7,445 | **7,376, tightest after 1 day** |
| layer 5 vetoes across the run | none | not reported | not reported | 107, every one check 1 | **0, and 92 absences named on purpose** |
| pattern slots, and their concentration | no family holds a section | not reported | not reported | 416 of 419 filled, 8 families, top three took 402 | **401 of 419 filled, 12 families, top three took 296** |

**Phase 9's job is named by the fourth reading, and the owner named it in advance.** If silence landed near band, phase 9 would be authoring to fix repeats; it did not, so **phase 9 is authoring to fix silence.** Those are different jobs and the difference is what a bench is grown for.

**Silence is 68 percent against a ceiling of 25 and it did not move across the rules pass**, because that pass carried no Pulse instrument: its cooldown applies to pattern selections, its check 1 narrowing serves three Report families, and the one Pulse rule it touched was already dark. The 2,167 silent Pulse days split into 1,185 where a rule qualified and every candidate was filtered, 971 where nothing qualified at all, and 11 with too little data. **A bench deep enough to empty the first column entirely would leave silence at 31 percent, with five of the eleven personas in band and six outside**, so bench depth is necessary and provably not sufficient. The owner's standing instruction is that this is reported and not ground at: an app that ships at 30 percent silence is better than one that does not ship.

**Two things beside it are worth reading before authoring anything, and neither is fixed by authoring.** First, eight of the families that never fire never qualified once, and nor did the two hot family stages that are short. The rules pass diagnosed every one at the rule that carries it and moved no threshold, because a stage threshold is a corpus stage header: three of them are waiting on a fact 3.1 does not declare, the queue an area held immediately before the promotion that emptied it, and the other five are a property of the eleven personas rather than of the rules. Second, `insufficientData` is no longer among them. Its rule was unreachable by construction, the Report renders that bench itself through `ReportLanguage`, and `ReportRules.RENDERED_DIRECTLY` records it as a family that left the engine on a decision rather than a family that went quiet.

**The check 1 conflict that the third reading recorded as open is closed.** `neglectedArea`, `areaGoneQuiet` and `areaRevival` were the only families vetoed anywhere in the run, 107 times, every one of them check 1 of section 8. The owner ruled that the check was right and the writing was wrong, and section 8 check 1 is narrowed rather than widened: a rule carrying `ClarityRule.absenceSubject` may name an area with no events in the window, and only when that area has a real lifetime, is not new, and has a measured `daysSinceLastEvent`. A new empty area is still refused by every rule. The fourth reading records no vetoes at all and 92 absences named on purpose.

---

## 13. Failure modes and guards

| failure | what it looks like | guard |
|---|---|---|
| Phantom area | naming an area with no activity | validator 1, archived areas structurally excluded |
| Invented number | a count no query produced | validator 3, `FactRef` provenance |
| Stale name | an old area name after a rename | snapshots only, live entities never reach the realizer |
| Resurrection | a deleted item named in a report | tombstones plus validator 2 |
| Off-by-one on shares | one event reading as 100 percent | mandatory event floor on every share-based rule |
| False superlative | `most active since March` when March is wrong | `mostRecentBetterWeekKey` strictly greater, plus a record-held criterion |
| Broken plural | `1 items` | `Count` carries both forms, no default |
| Visible slot syntax | a raw marker on screen | slot completeness check, validator 7 |
| Escalation whiplash | day nine then day three | monotonicity via `lastStageBySubject` |
| Device divergence | phone and desktop disagree | pure engine, history from the log, FNV-1a, deterministic sort |
| Repetition detected | the user recognizes a line | 90 day exclusion, frequency-based sizing, simulator check |
| Mad Libs seams | assembled sentences read wrong | family-owned benches, no global pools, uniqueness test |
| Blame language | a sentence that stings | banned vocabulary test, equal validity test, mirror test |
| Attributive framing | agentive second person on unflattering facts | `NEUTRAL_AGENT` register via the `unflattering` flag |
| Evasive framing | passive voice dressed as neutrality | validator 10, banned passive forms |
| Chattering | Pulse every single day forever | deliberate silence rule, simulator silence check |
| Reset residue | a fresh install says `since March` | reset virginity test |
| Timezone drift | two pulses in one day, or none | single injected clock with explicit zone, DST tests |
| Callback fabrication | quoting an answer never given | callback resolution before qualification, validator 6 |
| Zero sentence | `you completed 0 things` | validator 4, plus criteria making zero unreachable |
| Invented cue | a plan anchored to a day the user does not work | confidence thresholds in 3.7 |
| Plan surveillance | the report notices you did not do it | negative-case rule in 10.6, priority boost only |
| Plan pile-up | three unresolved plans stacking | composition rule 4 in 10.4 |
| Righting reflex | the app instructing weekly | nominal frames, first-person storage, explicit decline |
| Rhetorical tic | the same construction four times | no construction in more than two families |
| Uniform rhythm | every sentence the same length | length band rule in 7.5 |

---

## 14. Required tests

- **Purity.** No Android imports, no `System.currentTimeMillis`, no `Random`, no `String.hashCode()` in `domain.engine` or `domain.guidance`
- **Catalog integrity.** Every rule points at an existing family; every family has at least one rule; every variant's required slots are producible; no duplicate keys; no fragment string in two families; no rhetorical construction in more than two; every share-based rule carries an event floor
- **Criterion discrimination.** No criterion passes on more than 90 percent of a large simulated fact corpus
- **Determinism.** Identical inputs produce identical output across 10,000 generated cases
- **Cross-device agreement.** Two `FiringHistory` objects rebuilt independently from the same merged log produce identical selections for the same `dateKey`
- **Veto reachability.** One test per validator check constructing a violating candidate and asserting the veto
- **Escalation monotonicity.** A continuously true condition never shows a decreasing stage
- **Composition.** No report violates the incompatibility matrix across 10,000 generated reports
- **Register.** No `NEUTRAL_AGENT` variant contains a banned passive form. No Momentum line contains a causal construction
- **Silence floors.** Pulse 8 to 25 percent, layer 6 at least 15 percent
- **Repetition.** No variant repeats inside 90 simulated days across every persona
- **Cue substantiation.** No plan renders with a cue below threshold across 10,000 generated fact sets
- **Non-compliance.** Per section 12
- **Boundary.** `dateKey` correct across DST spring forward and fall back; the 17:00 reflection switch happens exactly once per day

**Phase 5 built all of these except the ones that need a layer or a corpus that does not exist.** Purity, catalog integrity, criterion discrimination, determinism over ten thousand cases, cross-device agreement, veto reachability for every check, escalation monotonicity, composition, register and non-compliance are unit tests today. Silence floors and repetition are simulator checks, they run, they fail, and each carries the issue that lifts it. **Cue substantiation waits for layer 6**, because nothing renders a cue yet. The 17:00 half of the boundary test waits for phase 6, which owns the generation lifecycle; the `dateKey` half is held by the daylight saving tests phase 3b built.

**Determinism is run through two independently constructed engines**, with the second one handed a history whose maps are rebuilt in reverse insertion order. The engine has no clock and no random number, so neither is the risk; the risk is a map iteration order leaking into a decision, which is invisible at small scale because a hash map of three keys usually iterates the same way twice and stops doing so somewhere above that. Ten thousand fact sets and a reversed history is the cheapest available imitation of two devices reaching the same facts by different routes.
