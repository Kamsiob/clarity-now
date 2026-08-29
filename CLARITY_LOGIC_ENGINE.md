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
    val focusStarted: Int, val focusCompleted: Int, val focusEndedEarly: Int,
    val focusSecondsTotal: Long, val focusMinutesTotal: Int,
    val focusDays: Int,                  // days with a session STARTED, never the session count
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
    val focusSecondsInWindow: Long, val focusSessionsInWindow: Int,
    val swapsInWindow: Int,              // this area's swaps, never the window's
    val dormantDaysBeforeReturn: Int?,   // the gap it returned from, null when it did not
    val dormancyStartKey: String?,       // the day before that gap. Null exactly where the gap is
    val completionsSinceActiveItemStarted: Int,  // lifetime, from the promotion. 0 when nothing active
    val weekEventsSeries: List<Int>,     // oldest first, up to 12
    val dipPrecedent: Precedent          // has this area been this quiet, this long, before
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
    val weekStartKeySeries: List<String>,   // the first day of each bucket, aligned with every series
    val weekCompletionsSeries: List<Int>,   // oldest first, up to 12
    val weekQueueSizeSeries: List<Int>, val weekTotalEventsSeries: List<Int>,
    val weekAreaCountSeries: List<Int>,     // live areas that moved, per bucket
    val weekFocusStartedSeries: List<Int>,  // sessions STARTED, per bucket
    val weekFocusCompletedSeries: List<Int>, val weekFocusEndedEarlySeries: List<Int>,
    val weekWeekendEventsSeries: List<Int>,
    val weekOverWeekDelta: Int?,
    val completionsTrend: Trend, val queueSizeTrend: Trend, val activityTrend: Trend,
    val dominantAreaLastThreeWeeks: List<AreaId?>,   // oldest first, nulls allowed
    val personalBestWeekCompletions: Int, val personalBestWeekKey: String?,
    val weeksSincePersonalBest: Int?,
    val mostRecentBetterWeekKey: String?,   // newest week STRICTLY exceeding this one
    val weeksSinceBetterWeek: Int?,         // the same bucket as a length. Null with the key
    val longestEverActiveDays: Int, val longestEverActiveItemId: ItemId?,
    val personalBestFocusMinutesWeek: Int,
    val firstEverFlags: Set<FirstEver>,     // present only in the window where each first occurred
    val currentQuietRunDays: Int,           // the scoped streak exception, capped at 30
    val currentSingleAreaRunDays: Int, val currentSingleAreaRunAreaId: AreaId?,
    val estimatedCompletions: Int,          // the 14b.8 floor of five, and it travels as a FactRef
    val activeToEstimateRatio: Double?,     // a multiple, never a percentage. Null under the floor
    val estimateTendency: EstimateTendency,
    val activityDipPrecedent: Precedent, val focusDipPrecedent: Precedent,
    val isJustBackFromAbsence: Boolean      // 14b.4. No date, no length, nothing to render
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

**`weekStartKeySeries` dates the buckets and does not resolve a day.** Every weekly series here is seven day buckets anchored at the window end, and until this field existed the dates those buckets covered lived only inside the extractor. A corpus line saying `since {sinceRef}` about the oldest week of a three week run needs one of them, and the alternative was to recompute the anchoring rule inside a measure, which is a second copy of a bucketing rule that can disagree with the first in a month name printed beside a claim about a trend. It is one entry per bucket and its entries are day keys, so it names one day in seven and resolves no day inside a week: `StreakExceptionAudit`'s per day series check classifies it at week grain for exactly that reason, and nothing about it brings a run of days within reach of a rule.

**`weeksSinceBetterWeek` is `mostRecentBetterWeekKey` read as a length**, the same relation `weeksSincePersonalBest` has to `personalBestWeekKey`, and it is null under the same condition. It exists because one bench says both `No week since {sinceRef} finished more` and `It has been {n} weeks`, and without a fact for the second the marker was bound to the week's event count and rendered `It has been 47 weeks` about a week five weeks ago.

**`focusDays` is days and `focusStarted` is sessions.** Four sessions on one afternoon and four sessions across four days are the same total and a different week, and `focusInvestment` says so in two of its lines. Neither number may be inferred from the other.

**`dormancyStartKey` is the other end of `dormantDaysBeforeReturn`**, the day of the area's own last event before the gap. `daysSinceLastEvent` cannot serve, because it is zero the moment the area moves again and therefore answers how long the area has been quiet **since** the return. Both are read from the same two events and both are null in the same two cases, so a line reading the length and a line reading the date are available on the same windows.

**`completionsSinceActiveItemStarted` is counted across the whole log and not across the window.** The age it is set against in `You have finished {n} other things since {itemTitle} became active` is the item's whole age, and a window count beside a lifetime age would be two spans in one sentence. The subject item is still active and so is not among the completions, which is what makes `other` true without an exclusion.

**`queueDrainedFrom` is a transition and not a difference of two boundaries.** It is the height an area's queue fell from, in one uninterrupted fall to nothing that has held to the window end, and null when no such fall happened inside the window. Read it backwards from the window end through `TrailQueries.queueSizeSeriesByArea`: a sample at least as large as the one after it is still part of the fall, and the first sample smaller than its successor is the moment something arrived, which puts the top of the fall at that successor. So 5, 4, 3, 2, 1, 0 reads five; 0, 3, 1, 4, 0 reads four; and 0, 5, 0, 2, 0 reads two rather than five, because two things arrived after the five left and `{n} things left {areaName}, and nothing replaced them` would be false of the larger number.

**It exists because a boundary pair cannot see a drain.** `queueLengthAtWindowStart` and `queueLength` are the same two numbers for a week that opened holding five and closed holding nothing and for a week that built five on Tuesday and finished them on Saturday, and only the second is what `CORPUS_1_PULSE.md` 10's `{areaName}'s queue went from {n} to nothing` and `CORPUS_2_REPORT.md` 2.17's `{areaName} cleared its entire queue this week` describe. `queueDrain`, `clearing` and `queueDrained` were dark across eleven simulated years for exactly that reason, and no criterion could approximate it, because `queueLength`, `queueLengthAtWindowStart` and `queueDelta` are all read at the same two instants.

**Null rather than zero, for both of the cases where nothing drained**, exactly as in `dormantDaysBeforeReturn`, which this mirrors: that field measures the gap an area returned from rather than the gap since the window opened, and this one measures the queue an area drained from rather than the queue it happened to hold at a boundary. The queue is not empty now, or it is empty and nothing fell to get it there. A rule cannot tell those apart and must not be able to.

**It carries no claim about how the items left.** A queue also empties by deletion. `RuleBuilders.drainedByFinishing` requires `completionsInWindow >= queueDrainedFrom` and is carried by both drain families, because every sentence on both benches says somebody finished something.

**Phase 5, three readings this section leaves open, resolved and recorded.**

- **`busiestDayKey` on a tie.** 3.1 makes `dominantAreaId` null on a tie and says nothing about this field, so the tie had to go somewhere. It resolves to the **earliest** day, which is the day the peak was first reached. That is not enough on its own: a sentence of the shape `Tuesday carried the week` is false on a three way tie whichever day wins, so the family that names the day carries a floor requiring `busiestDayCount` to be a real share of `totalEvents`, exactly as every share based rule carries an event floor
- **`focusEndedEarly` counts `FOCUS_ENDED_EARLY`, and the field was renamed with the event.** This block said `focusAbandoned` and phase 5 said the field would keep that name. It did not, and the code is right: DECISIONS.md C6 argues that a name in a document a second implementation is built from is an instruction about what the concept means, and the log cannot know a session was abandoned. That argument applies with more force to a field than to an event type, because rules are authored against these names and every rule author reads them. **A session with no terminal event is in neither count**, so `focusStarted` may exceed `focusCompleted` plus `focusEndedEarly` and no rule may infer the difference
- **Every band is present in `eventsByPartOfDay`, including zeros.** A share is a division, and a missing denominator term produces a set of percentages that do not reach a hundred with nothing on the screen to explain why

`responseLabel` is stored verbatim in the `PULSE_ANSWERED` event so a callback quotes what the user actually saw, not a label reworded in a later app version.

---

**Three facts from `MASTER_BUILD_PROMPT.md` 14b, declared here because 14b names the requirement and leaves the definition to this file.** Each of them exists to stop a sentence rather than to enable one, and each is shaped so that the sentence it stops cannot be assembled rather than merely being caught.

**Estimate calibration: `estimatedCompletions`, `activeToEstimateRatio` and `estimateTendency`.** 14b.8 permits `Things you estimate at an hour tend to take about three` and forbids both `You underestimated by two hours` and `You were off by 140 percent`. Only ratios and tendencies.

- **The window is twelve weeks**, the same seven day buckets every series here uses and the same span 3.7 already calls a pattern rather than an accident. A one week reading over five items is an accident, and a lifetime reading averages how somebody estimated a year ago into how they estimate now, which is the thing that is supposed to be able to change. The consequence is a constraint on the corpus rather than a freedom: a family reading these may not say `this week`.
- **An item counts when it was completed inside that window, was active when it was completed, and carried an estimate at the moment it became active.** An item finished without ever having been promoted has no actual: there is a moment it was added and no moment it was started, and reading back to the add turns how long something waited into how long it took, which is a distinction `TrailQueries.daysActiveForItem` and `daysSinceItemAdded` already keep.
- **The prediction is the estimate in force at the promotion, and a revision made after it is ignored.** An estimate changed while the work is under way is not a prediction any more; it is a progress report informed by exactly the thing being measured, and honoring it would move every ratio toward one and quietly flatter the person the fact is about. A revision made before the promotion is a better prediction and is the one used.
- **The actual is the elapsed time of the item's last active spell**, which is the actual 14b.3 says comes free. A reopened item is measured over the spell that ended in the completion being read, and a completion whose wall clock precedes its own promotion is two devices disagreeing about the time rather than a fast finish, so it is dropped rather than clamped to zero.
- **The reading is the median of the per item ratios**, never the mean and never the ratio of two totals. One item left active over a holiday moves both of those to a number no week of the person's life resembles, and `tend to` is a median word.
- **It is a multiple and never a percentage.** A ratio of 2.4 rendered as 240 percent is one literal hundred away from the second forbidden line, and the corpus family is authored against a count slot for that reason.
- **The floor is five and it travels.** `estimatedCompletions` is reported truthfully whatever it is; under five there is no ratio and the tendency is `INSUFFICIENT`. 14b.8 requires the count itself to reach the validator as a `FactRef` so the number that gated the sentence is re-read rather than trusted.
- **`CLOSE` is the band in which there is nothing to say**, and it is drawn where the rendering is rather than at a chosen number: a median that would print as `about one` is somebody whose estimates land.

**No quantity of minutes exists anywhere in the fact set, and that is the prohibition rather than a consequence of it.** `TrailQueries.estimateOutcomes` divides the two magnitudes inside its own body and returns a ratio with no unit attached, so `actual - estimate` is not a subtraction any rule, measure or template above it is able to write. A validator catching the number afterward would leave the number computed; this leaves it unformable. **Do not add a field holding an estimate or an elapsed time in minutes**, whatever it is for.

**The ratio is a stay and not an effort, and a family reading it must say so.** Nothing in this app measures time spent working. What the log holds is how long a thing sat active, so a thing estimated at an hour that occupies a day and a half is a true reading of how somebody's estimates map onto their days and a false one of how long the work took. Two quantities, two names, as with `focusStarted` and `focusCompleted`.

**Precedent: `AreaFacts.dipPrecedent`, `HistoryFacts.activityDipPrecedent` and `focusDipPrecedent`.** 14b.9 is a correctness fix and not a politeness one. A fluctuating condition and a decline are the same numbers, and without this the app tells somebody with a cyclical or relapsing condition that they are deteriorating, on a fixed schedule, forever, being technically accurate every time. The question is whether a fall of this depth and this duration has happened to this subject before.

- **A subject's `normal` is the median of the weeks it moved in**, over its whole history, not the median of all its weeks. The all weeks median is zero for anybody quiet more than half the time, and a zero normal makes nothing low, which would pass the gate for precisely the most cyclical people it exists to protect.
- **Depth is banded, in four steps**: at or above three quarters of normal is steady, below three quarters is low, below half is deep, and nothing at all is its own band because a subject that stops and a subject that halves are different shapes. Banding rather than an exact depth is what makes two falls comparable at all; a continuous measure would make almost no two falls alike and the answer would be `NONE` forever, which is the answer the fact exists to stop the app giving by default.
- **The current fall is the unbroken stretch of low weeks ending at the newest closed bucket**, its duration that stretch and its depth the deepest band in it. **A precedent is any strictly earlier unbroken stretch that lasted at least as long and reached at least as deep.**
- **The newest bucket is skipped when its seven days have not all closed.** The Areas banner and the Momentum headline recompute during the day, and a part week is low against any normal, so reading it would put every subject into a fall every Wednesday morning and lengthen every fall by one week, which makes a precedent harder to find rather than easier. The Report and the Pulse both end on a day boundary and are unaffected.
- **The weeks before a subject's first week with anything in it are dropped.** They are not silences; nothing had happened yet. This is the rule `AreaFacts.weekEventsSeries` already states and `FactAccess.returnsAfterSilence` already applies.

**`Precedent` has four values because there are two ways of not knowing and they must not be folded together.** `NONE` is the permission and `PRESENT` is the veto. `INSUFFICIENT` is neither, and it arrives by two routes: fewer than twelve weeks of the subject's own history, which is 14b.9's own example of a person who has no precedent for anything, and a fall so long that no earlier fall of the same length could have fit behind it. `NOT_IN_A_DIP` is the honest nothing: no fall here to find a precedent for.

**The rhythm branch tests for `PRESENT` and the gate closes on `PRESENT` alone. This paragraph said something else and the phase that built the gate settled it the other way.** It said both branches test for their own value, so that a subject with too little history gets neither sentence, which asks a decline family to require `NONE`. The argument that decided it is `NOT_IN_A_DIP` rather than `INSUFFICIENT`. This fact's notion of low is a week under three quarters of the subject's own normal, and **no decline family asks that question**: `decliningActivity` reads a run of three falling weeks, which can end on a perfectly ordinary week, and `neglectedArea` reads a gap measured in days, which can open inside a week the area was busy at the start of. Requiring `NONE` would silence a true observation every time the two definitions came apart, and a missing sentence is the one defect nothing on the screen reveals. Closing on `INSUFFICIENT` as well would withhold every decline observation from every install between its fourth week, where a series first exists to read, and its twelfth, where a precedent becomes answerable, which is eight weeks of an app that has noticed something and decided not to say it. `FamilyAvailability.CLOSES_THE_GATE` is the set, it holds one value, and it is one word from holding the other reading.

**Only the verdict travels.** There is deliberately no depth, no duration and no date beside it. Those are numbers about somebody's worst weeks, and a fact set carrying them would be one measure away from a sentence counting them out.

**The re-entry quiet week: `HistoryFacts.isJustBackFromAbsence`.** 14b.4 requires every decline, neglect and gap family to be unavailable to selection for seven days from a re-entry date, in the Report, the Momentum headline and the Areas banner, which read the same catalog. All three extract their facts through layer one, so all three read this and cannot disagree about the day the withholding ends. The Pulse's own two day window is older and sits above layer one in `PulseGeneration`, because the Pulse declines to run the engine at all rather than withholding some of its families.

**It is a boolean, and that is 14b.4's prohibition kept by shape.** A returning person must never be greeted by a measurement of their absence, not in days, not in weeks, not as a date. `TrailQueries.lastReEntryOnOrBefore` answers with a value carrying the date of the return and nothing else, and what reaches a rule from it here is one bit less than that: whether the app is inside the quiet week. There is no length to leak and no date to print. **Do not add a field holding either.** It is asked of the last local day the window describes, which is the day the sentence would be said on; asking about the window start would buy a fortnight of withholding where 14b.4 asks for seven days.

---

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
4. **Repeat filter, Pulse only.** Drop pairs whose family equals `PulseFacts.lastGeneratedFamily`, **and only where that Pulse was generated yesterday**, which is the reach 7.3 states for this rule. A Pulse older than yesterday is the cooldown's business and not this filter's
5. **Cooldown filter.** Drop pairs whose `(family, subjectId)` fired within `cooldownDays`
6. **Rank.** Specificity descending, then priority descending, then `rule.key` ascending. The final key sort removes the last ordering ambiguity and must be present even though it rarely matters
7. **Take the head.** Empty returns `Silent`

**Step 4 reaches one day, and the fact it names does not.** `PulseFacts.lastGeneratedFamily` is the family of the most recent Pulse generated at any point in the past, which equals yesterday's family only on the day after a day the Pulse spoke. 7.3 states the rule in words and states it as one day: the cooldown covers `cooldownDays` and is "separate from the no-repeat rule, which covers only yesterday". Section 12's own table calls this filter "yesterday's family cannot be today's". The code read the fact name rather than the rule and blocked the last family whenever it had spoken, and **that reading is self-reinforcing**: a family blocked here writes no `PULSE_GENERATED`, so the fact does not advance, so the same family is blocked again tomorrow. A life whose only qualifying family is the blocked one loses the Pulse permanently. The ninth measurement found one persona held silent for 348 consecutive days by a Pulse from January 20, and `Selector.REPEAT_WINDOW_DAYS` is the bound that makes that unreachable rather than unlikely.

**Step 1b, family availability.** `MASTER_BUILD_PROMPT.md` 14b.4 and 14b.9 each remove a family from selection rather than reorder it, and `Selector` applies both between step 1 and step 2. **It is numbered rather than inserted** for the reason 11.3's own sequence numbers its 2b: the seven steps above are cited by number from three documents and from the tests, and renumbering them would break those citations in silence.

The two gates are 14b.4's week of withholding after a return, which applies to the Report, the Momentum headline and the Areas banner and not to the Pulse, and 14b.9's capacity gate, which applies everywhere. **Neither can be a criterion**, and the reason is arithmetic rather than taste: specificity is `criteria.size`, so a criterion added to `quietWeek` would make `quietWeek` outrank a rule that genuinely required more, and 14b.4's test is true on all but seven days of a person's life, which is the trivially true criterion section 4 forbids. Both are filters, so they commute with steps 2 to 5 and the placement changes no outcome; they run first among them because 14b.4's own word is `unavailable`. **They run inside the filter chain rather than over the qualified list**, so a purpose whose every qualifying family was withheld reports `ALL_QUALIFIED_RULES_FILTERED` and not `NO_RULE_QUALIFIED`: a week the engine chose not to describe is not a week it had nothing to say about, and 5.1's whole argument turns on the simulator being able to tell those apart. `FamilyAvailability` holds both tables and the reasoning for each family in them.

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

**Observations**, 2 to 4: `singleFocus`, `intakeVsOutput`, `focusInvestment`, `neglectedArea`, `completionSplit`, `selfReportVsData`, `quietWeek`, `queuePressure`, `areaRevival`, `persistentItem`, `personalBest`, `mostActiveSince`, `dayShape`, `timeOfDay`, `switchingBehavior`, `focusAbandonment`, `queueDrained`, `steadyPace`, `firstMilestone`, `areaBalance`, **`hardStretch`**, **`familiarDip`**, **`estimateCalibration`**.

**Patterns**, at most one, requiring `weeksOfData >= 3`: `shiftingFocus`, `growingQueues`, `improvingThroughput`, `decliningActivity`, `areaGoneQuiet`, `consistentRhythm`, `narrowingFocus`, `broadeningFocus`, `focusHabitForming`, `focusHabitFading`, `reportedVsActual`, `queueEquilibrium`, `weekendShift`, `abandonmentPattern`, `comebackPattern`, `insufficientData`.

**Closings**, produced by layer 6: eight plan-action families plus `trustThePace`, `letItBe`, `noRhythmYet` and `review`.

**The last two observation families landed in phase 9, and each waited on a different half of the engine.** `familiarDip` is the second branch of 14b.9's capacity gate, which excludes a decline, neglect or fading family when the fall has a precedent and requires that **a different family fire with different language** rather than that the first one be softened. Its three rules were written in the facts phase, one per subject the precedent facts measure, and were held in `FamiliesAwaitingLanguage` because a family declared here with no bench in `CORPUS_2_REPORT.md` fails the parser, and a family with a rule and no bench would qualify, say nothing, and look exactly like a family that never happened to fire. `estimateCalibration` is 14b.8's, and it waited from the other side: its facts, its floor and validator check 11 were all built before any rule read them.

**Both are in the observation list above now, because that list is what the parser checks the corpus against in both directions.** `CORPUS_2_REPORT.md` 2.22 and 2.23 carry the benches and the constraints each is written under.

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

Five registers, offered in **tiers** of equal standing. A tier is tried, a voice is chosen inside it, and a register with nothing it can fill falls through to the next voice and then to the next tier:

1. **`NEUTRAL_AGENT` when the rule is marked `unflattering`.** A tier of one. If the family has a `[N]` variant at the selected stage, prefer it
2. **Time of day, Pulse only.** Dawn and midday offer `PLAIN` and `OBSERVATIONAL` as one tier; evening offers `REFLECTIVE`
3. **Editorial budget, Report only.** At most two `EDITORIAL` leads per report. A third is re-realized in the open tier
4. **The open tier:** whatever of `REFLECTIVE`, `OBSERVATIONAL` and `PLAIN` the steps above did not already offer, chosen among rather than ordered

**Step 4 was a list until phase 9's successor, and a list is what made half the corpus unreachable.** The realizer took the first register with a line it could fill, so the head of the list won every time a rule left the question open. Measured across eleven simulated persona years: the Pulse spoke `PLAIN` on 1,080 of 1,081 firings, Momentum and the Areas banner spoke `REFLECTIVE` on all 5,594 of theirs, and 487 of `CORPUS_3_MOMENTUM.md`'s 748 register tagged lines sat in registers nothing could ask for. 11.1 sizes a bench per stage and the chooser sees a register, so a stage of sixty lines split three ways bought twenty lines of variety. Both figures for the Pulse are partly the instrument, because the simulator only builds the before 17:00 window; the Momentum and banner figures are not, because those surfaces have no reflection period.

**Inside a tier the voice is chosen the way 7.6 chooses a line, and deliberately by the same instrument.** The register the family used most recently is held back where anything else remains, for the reason 7.6 step 2 holds back the most recently used line: the voice a person heard last is the only one they might recognize. The rest are ordered by `stableHash(dateKey + familyKey + stage + register)` and the head is taken. Determinism and cross-device agreement therefore carry over from 7.6 unchanged, and the register name is both the last term of the key and the tie break, so no map iteration order can reach the decision. **Read across the whole family rather than the selected stage**, because a person hears a family and not a rung of its ladder.

**Steps 1 to 3 are rules about content and step 4 is not, which is why only step 4 rotates.** `design-v3.md` 15 asks for the answer that is not the obvious one, and the obvious answer here is a fourth rule keyed on something in the fact set. It is rejected because the corpus does not author to one. Where a situation genuinely subdivides, the corpus splits it into another family or another stage; `bn.start` holds sixty two lines under one trigger in four voices, and no fact distinguishes *Early in the week.* from *The week is young.* A fourth rule would make the engine claim a distinction its own language never made, and an author would then be writing lines for a condition nobody stated. **The rotation is confined to the question the rules leave open**, and it never reaches `NEUTRAL_AGENT` or `EDITORIAL`: a budgeted voice that turned up one time in three would not be budgeted, and an unflattering voice offered to a good week is the thing this section exists to prevent.

**Step 2 gained a second register out of this section's own words.** "Dawn and midday prefer `PLAIN` and `OBSERVATIONAL`" names two registers as equally preferred, and the code turned that into an order in which plain always won. It is one tier of two.

**Which rules carry `unflattering = true`.** Enumerated so it is not a judgment call:

`intakeVsOutput` at stages 1 and 2 (intake exceeding output), `neglectedArea` at both stages, `queuePressure`, `persistentItem` at stages 3 and 4, `switchingBehavior` at stage 2, `focusAbandonment`, `decliningActivity`, `quietWeek`, `hardStretch`, `estimateCalibration`, `singleFocus` at stage 2, the Areas banner's `weekQuiet`, and the pattern families `growingQueues`, `decliningActivity`, `areaGoneQuiet`, `narrowingFocus`, `focusHabitFading`, `abandonmentPattern`.

Everything else is `false`. A family that is neutral or positive never uses the neutral-agent register, because making the fact the subject of a good week reads as withholding credit.

**Phase 9: the widening of `MASTER_BUILD_PROMPT.md` 14b.10 is applied above, and it added two entries rather than a list.** 14b.10 widens the enumeration to cover every rule concerning a decline, a gap, a neglect, an imbalance or an unmet expectation, and the enumeration already named every decline, gap and neglect the catalog carries. The two it missed are **`intakeVsOutput` at stage 1**, whose own corpus stage header reads `mild imbalance, gap of two to four`, and **`estimateCalibration`**, the family 14b.8 adds, whose whole subject is a prediction the days did not meet. Both got the `[N]` bench the flag now reaches for.

**The widening's second pass added one entry, `weekQuiet`, and it is the only rule outside the Report that carries the flag.** The first pass read 14b.10 against `CORPUS_2_REPORT.md` and never opened volume 3. All eight of the Areas banner's quiet week lines were `[N]`, which `CORPUS_3_MOMENTUM.md`'s authoring rule 5 requires of a quiet state and which that file argues for by name: a quiet week is the one banner state where agentive second person would read as an accusation on a screen the user sees every time they open the app. Nothing could ask for that register, so the family qualified on real windows and produced not one sentence across eleven simulated persona years. Adding lines could not have reached it and adding non `[N]` lines would have fixed the symptom by breaking rule 5, so the amendment is here. **The bench that followed is sixty lines and every one of them is still `[N]`, which this section is the reason for.** Step 1 is a tier of one, `Realizer.realize` leaves a tier only when nothing in it can be filled, and no line in `bn.quiet` carries a slot, so the neutral agent tier fills on all 240 of the family's firings a year. A plain, observational or reflective line authored into that bench would be unreachable for the same structural reason its `[N]` lines used to be, which is why the widening bought the family a voice rather than a choice of voices.

**Three Momentum and banner families were considered beside it and left alone.** `quietStretch` is the closest call in this section, because it is the Momentum analogue of `quietWeek` and `quietWeek` is enumerated. It is left alone because **the flag exists to stop agentive second person landing on unflattering content, and `mo.quiet` has no agentive line to stop**: the fortnight is the grammatical subject of all sixty six of them, whatever the register tag says, which is what volume 3's own rule that Momentum observes and never interprets produces. Marking it would buy nothing a reader could feel and would cost a family that fires a hundred and twenty four times a year two of its three voices, because step 1 is a tier of one and eighteen of its lines are `[N]`. `singleAreaWeek` and `weekMixed` are distributions rather than imbalances, which is the reading this section already took for `singleFocus` at stage 1 and for `fragmented`. **`weekStarting` keeps ten `[N]` lines that nothing can reach**, and that is the residue of this decision rather than an oversight: an early week is not a decline, a gap, a neglect, an imbalance or an unmet expectation, so 14b.10's enumeration does not reach it, and marking it would hand a fifty two line bench in three voices to a ten line bench in one.

**Four families were considered and left alone, and the reasons are here so the next reader does not have to re-derive them.** `singleFocus` at stage 1 is a distribution rather than an imbalance, and this section already split that family at stage 2 deliberately, so widening it would relitigate a decision rather than repair an omission. `netInflow` and `fragmented` are headlines, and `selfReportVsData` points both ways by construction: half its bench is the app siding with the person against its own numbers, and a register selected for the whole family would be selected for those lines too.

**The widening stops where the register does.** The flag has exactly one effect, which is whether the realizer may reach `NEUTRAL_AGENT`, and `CORPUS_2_REPORT.md` carries a register tag in section 2 alone: `ReportWalker` refuses one on a headline or a pattern line, so every variant in those two sections is `PLAIN` by construction and the open tier in step 4 is what they get, with one register in it. Eight families already enumerated above are headline or pattern families and can never carry an `[N]` line without a change to the corpus format: `quietWeek`, `decliningActivity` and `queuePressure` as headlines, and `growingQueues`, `areaGoneQuiet`, `narrowingFocus`, `focusHabitFading` and `abandonmentPattern` as patterns. All three of the headline families carry an `[N]` bench in their observation family; it is the headline rule that has nowhere to reach. Marking more of them would change nothing a person reads and would owe a bench nobody can author, which is why the widening added no headline and no pattern.

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

Related: **no more than two parallel numeric clauses consecutively.** The three-part list is a rhetorical reflex and once a reader sees it they cannot stop seeing it.

**Both of those are preferences on the reading order and neither removes a line.** This section used to say that where a third numeric clause would follow, the composer drops it or re-realizes it at a different length. Re-realizing needs the bench and the composer holds finished sentences; dropping is the trade 11.4 forbids in the other direction, and it fired twice across eleven persona years while the rule it was meant to enforce was broken 147 times. 9.2 states what both rules do instead, what counts as a numeric clause, and which of the two wins where a line cannot hold both.

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

11. **Estimate delta.** No rendered sentence states a difference between an estimate and an actual. `MASTER_BUILD_PROMPT.md` 14b.8 permits `Things you estimate at an hour tend to take about three` and forbids both `You underestimated by two hours` and `You were off by 140 percent`. Two rules: any of the delta forms in `ValidatorVocabulary.ESTIMATE_DELTA_FORMS` anywhere in the sentence, whether or not it says estimate, because the percentage example never does; and a `Percent` slot in a sentence that is about an estimate, because 14b.8 makes the reading a multiple and never a percentage and 2.4 shown as 240 percent is one literal hundred from the second forbidden line

**Check 11 is 14b's and not this section's, and it is appended rather than inserted.** The ten above are cited by number from this file, from `MASTER_BUILD_PROMPT.md` and from the tests, and putting an eleventh in the middle would renumber them silently. It runs last for the same reason the order is data below: a fabricated area name is more fundamental than a sentence shape. **It is a backstop and the prohibition does not rest on it**: `TrailQueries.estimateOutcomes` divides the two magnitudes inside its own body, no quantity of minutes exists anywhere in the fact set, and no measure produces one, so the subtraction is unformable above this layer. 14b.8 asks for the check anyway, for a number arriving some other way, and section 17 lists a test that constructs the forbidden form.

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

**The two rhythm rules are preferences, and neither of them may remove a line.** Length variation and the parallel clause cap are the only rules in this section that are about how a page sounds rather than about what it may claim, and 11.4 decides what that is worth: a report is allowed to be shorter and it is not allowed to be padded, and dropping a true observation to improve the cadence is the same trade in the other direction. So both are expressed in the **reading order**. The next line is the highest ranked one in its section that holds both rules; where no remaining line holds both, the highest ranked one that holds the clause cap; then the highest ranked one that holds the band; then the highest ranked one, said anyway. `ReportComposer.arrange` is the whole of it.

**Where the two disagree, the clause cap wins.** A band collision is a property of two adjacent lines and the very next line is another chance to fix the rhythm; a run of numeric leads is a property of three, so the opportunity to repair it is scarcer. 7.5 also says what the failure costs, and the three part list a reader cannot stop seeing is a louder complaint than two sentences of one length.

**A parallel numeric clause is a lead that renders a number at all**, counted from its slots rather than from the digits in its rendered string. The narrower reading, a lead setting one number against another, was written while the rule removed the third lead and was correct for that: nearly every observation in this corpus states one number, so the wide reading plus a drop would have quietly shortened almost every report. The drop is gone and the argument went with it. The two readings are far apart in practice. Across eleven persona years of composed reports the narrow one finds a run of three **twice** and the wide one finds it **117 times**, so the narrow reading is a rule that does not fire. Slots rather than digits because a person may name an area `Q3` or title an item `Rewrite intro v2`, and a digit this app did not choose to say is not a number it stated.

**The headline seeds both rules and the pattern is not looked ahead to.** The headline is read immediately above the first observation, so its band and its number start the sequence; leaving it out left the one seam a reader meets first unchecked, and it was 117 of the 302 band collisions the composer produced over eleven persona years. The pattern is read immediately after the last observation, its position is fixed, and the only thing a lookahead could change is which observation lands last, so it is measured rather than optimized: 131 of the 219 remaining band collisions are that one seam.

**Neither rule may be asserted as an invariant of a finished report, and both are counted.** A preference that can be overruled by there being nothing else to say is not a property a report holds, and a test that asserted it would be asserting that a true sentence was thrown away to satisfy it. `ReportRhythm` counts both over the same runs that check the rest of section 9, and prints the cause of every residue beside the number: the line was the last one in its section, or every line left in the section collided the same way, or the other rule took the pick, or it was the pattern.

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
> Stored: *If it's Wednesday morning, my one thing is ten minutes in Personal.*

**The imperative form never exists anywhere in the app.** The directive only ever appears as something the person said about themselves.

**Phase 9b, the if then form, and where it came from.** It shipped unrenderable and the guidance language pass closed it, from the corpus rather than from the code, and the sequence is worth keeping because the answer that lost is the obvious one.

4.4 declared `com.01  If it's {cue}, I'll {actionVerb}.` and said in prose that every action carries a verb form alongside its gerund. **The action bank carried none**: fifty four actions, no verb forms, and 4.4's own worked example was the only place a verb form appeared in the file. So `{actionVerb}` had no binding, `com.01` failed to fill, and it left its bench the way any unfillable line leaves any bench in this app. The stored line was 4.4's second or third form, both first person and neither an if then, which left a stated acceptance criterion unmet.

**Authoring the fifty four verb forms was the obvious way to close it and it loses on a hazard rather than on effort.** A verb form is *spend ten minutes in Personal*, *close the oldest item in Work*, *decide whether Reading stays or goes*. In isolation each one is an imperative. Fifty four of them sitting in the corpus is a complete imperative action bank one frame away from a screen, in an app whose own 10.2 says the imperative form never exists anywhere, and `PlanFormTest` asserts exactly that over the benches rather than over the output. It would have had to be weakened to admit them. Deriving them from the gerunds was worse again: half the bank is not a gerund phrase, `ten minutes in Personal` needs the supplied `spend` rather than an inflection, and an irregular verb table in Kotlin whose failure mode is *I'll decid the oldest item* is the second path to a sentence that CLAUDE.md rule 8 exists to forbid.

**So the if then is built out of the noun phrase every action already is**: `com.01  If it's {cue}, my one thing is {actionNoun}.` It is first person, it is a proper if then, it needs no marker the corpus does not supply, and it cannot be read as a command on any day by anybody. The prose promising verb forms is withdrawn from 4.4 rather than left standing.

**What it cost is a second declared table, and what it bought is three cues that had never once reached a screen.** An if clause wants something a day can be and the two other forms want an adjunct, so no cue reads in all three; `PlanBenches.CueShape` therefore carries the commitment forms each shape reads in beside the frames it reads in, and `commitmentReady` is derived from that rather than declared a second time. The shape 4.2 calls nominal, *your first hour*, *the day you usually get most done*, *your next quiet evening*, reads in no adjunct form and reads cleanly after *If it's*, so those three are offered now and `com.01` is the only form that can store them.

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

**Phase 9b, what was built and where it differs from the declaration above.** Every difference is recorded here rather than in a commit message, because this block is what a second implementation would be built from.

**`GuidanceComposer` is a class over a catalog and a zone, not an object.** Every sentence it produces comes out of `CORPUS_2_REPORT.md` 4, and an object would have to reach a corpus through a global. Issue #55 is already open about corpus catalogs being built more than once per process and a fourth construction site would make it worse. `ReportLanguage` and `PulseLanguage` are classes over the same two arguments for the same reason.

**`compose` takes three arguments this declaration does not name**, and each is something the rules below need that the declaration predates. `headline` is 10.4 rule 6, which is written in terms of a declining headline and cannot be evaluated from observations alone. `weekStartKey` is what the plan is filed under and what rule 4 measures two weeks back from; deriving it inside layer 6 would let it disagree with the key the report is filed under, and `ReportComposer.compose` already takes it for that reason. `history` is 7.6's ninety day variant exclusion, without which the same frame is offered every week of the year.

**Four fields of `ClarityPlan` are deliberately absent, and the omissions are the safeguard rather than tidiness.**

- `acceptedAt`. A composed plan has not been accepted. Acceptance is a `PLAN_ACCEPTED` event, it lives in the log and in `PlanState`, and a second place holding one fact is how two devices come to disagree
- `declinedAt`. 10.5 is unambiguous that declining writes nothing and is never counted, there is no `PLAN_DECLINED` event and there must never be one, so this field could only ever be null. **A field that can only be null is a field somebody will one day try to fill**
- `resolvedAt` and `resolvedValue`. There is no resolution event either, so neither could be derived from the log, and 10.6 forbids the sentence they would enable. **Stored, they would be a record of whether a person kept a promise, which is the one record this app must not hold.** Whether an accepted plan's situation is still standing is asked and answered at composition time, from this week's facts, and the answer is a boolean that lives for one call

**`resolutionFactRef` is recorded and never compared.** It is the leading fact reference of the observation that motivated the plan, it is carried on `PLAN_OFFERED` because this declaration says a plan carries one, and the follow through matches on `(familyKey, subjectId)` instead. Those two are what the payload already carries and what escalation and cooldown are already keyed by, so the boost needs no second addressing scheme.

**`PlanHistory.Accepted` carries three fields and every one is a key**: a week, a family and a subject. No plan id, no rendered line, no timestamp, no count. Those exist on `PlanOffered`, they are read while the history is rebuilt, and not one of them is copied out. A family key and a week key are exactly what every ordinary observation already carries, so nothing downstream can compose a sentence about a plan that it could not already have composed about the week. **A leak would have to add a field to that class**, which is a visible act rather than a quiet one, and a test reads the class's declared fields and fails if one appears.

### 10.4 Composition rules

A plan may be produced only when **all** hold:

1. `CueFacts.hasStableRhythm` is true and the specific cue clears its thresholds
2. **The motivating observation actually appeared in the report.** Enforced by passing only `appeared` into layer 6
3. There is a real friction pattern. When barriers are low, plan formation is superfluous, so a straightforwardly good week gets no plan
4. No plan offered in the previous two weeks is still unresolved. Stacking unfinished plans is how this becomes a nag
5. The action is completable inside one week and is a single concrete act
6. The report is not otherwise heavy. A declining headline plus a neglected area, or any `hardStretch`, means no plan

**Phase 9b, how each of the six is enforced.** Three of them are structural rather than checked, which is what 10.4 asks for and is worth stating as built.

- **Rule 1** is `CueFacts.hasStableRhythm`, read before anything else is chosen, plus the ordinary slot mechanism: a cue naming `{strongestWeekday}` on a person with no strongest weekday cannot be filled and leaves its bench, because 3.7's extractor nulls every field that did not clear all three thresholds. `CueSubstantiationTest` asserts it over ten thousand generated fact sets
- **Rule 2** is the `appeared` parameter and nothing else. `Validated` is constructed by layer 5 alone, the composer reads no other source of observations, and there is no fact set path to a family that did not appear. `ReportComposer` validates the kept candidates a second time to obtain the tokens rather than wrapping them, so the type stays a promise layer 5 makes
- **Rule 3 is the action bank.** `CORPUS_2_REPORT.md` 4.3 heads each bank `### From intakeVsOutput or queuePressure` and the seven like it, which is the corpus naming the frictions it judged a plan could help with. A week whose observations are all outside that list has no barrier worth acting on and gets no plan, and **nothing in the engine has to decide that a week was a good one**
- **Rule 4** reads accepted plans only, and a plan is unresolved when the same `(family, subject)` is on this week's page again. Taken literally the rule says "offered", which would include a plan the person declined, and 10.5 forbids that: holding back next week's plan because somebody said no last week is a cost and a reference at once. The two reconcile by reading a decline as a resolution, which it is. The person answered and the plan is finished. Ignoring both options resolves the same way, which is what 10.5 means by identical
- **Rule 5** is a property of the bank rather than a runtime test. Every line in 4.3 was authored under 4.9 rule 1, "if it cannot be finished in a sitting it is a project, not an action", so a plan built from the bank satisfies the rule by construction. Re-judging it at runtime would be the engine second guessing the corpus
- **Rule 6** is read off what appeared rather than off the facts, so a report that was heavy and did not say so is not treated as heavy. **A heavy report gets `Nothing`, not a gentle closing.** All three non plan benches say something about the week, two of them that it went well and the third that the app is still learning, and on a genuinely hard week each of those would be false in a way the reader can feel

**A cue must also read grammatically, and that is declared rather than inferred.** 4.9 rule 5 asks an author to write every cue out inside every frame and every commitment form and check that it reads. Some frames put the cue in an argument position and some in an adjunct position, the if then form puts it after a copula, and no cue reads in all three places: *One option for before you open Work* is not English, neither is *There is room your first hour for ten minutes in Reading*, and neither is *If it's before you add anything new, my one thing is ten minutes in Personal.* `PlanBenches.CUE_SHAPES` records one shape per cue, each shape carries the frames and the commitment forms it reads in, a cue with no entry is unavailable, and a test reports any cue in the corpus that has none. **Inferring the shape from the line's own words was the obvious answer and it loses**: the difference between *the next time you finish something*, which reads in every frame and in no if clause, and *Wednesday morning*, which reads in both, is not visible in either line's morphology, and an inference that got it wrong would produce an ungrammatical sentence with nothing failing.

**The part of day bank waits on the morning.** Every line in 4.2's `cue.band` bank names it: *the morning, when you finish most things*, *before midday*, *your first hour*, *early on*. Those are true of a person whose productive band is the morning and false of everybody else, and 3.7 calls an unsubstantiated cue worse than no plan. So the bank is held back unless `productiveBand` is `MORNING`, and a test asserts every line in it really is about mornings, so an evening line added later fails the build rather than telling somebody something untrue about their own day.

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

**Phase 9b: the boost is one file, one parameter and one integer, and it was built to be removed.** Section 19 of `MASTER_BUILD_PROMPT.md` says the follow through is the first thing removed if it reads as supervision, and it says removed rather than tuned. So `domain/guidance/FollowThrough.kt` answers a set of `(family, subject)` pairs and nothing else, `Selector.select` takes that set with an empty default, and the amount a boosted pair is worth is `Selector.FOLLOW_THROUGH_BOOST`. **The constant lives in layer 3 rather than layer 6 precisely so that deleting the file leaves the engine compiling**: every caller outside the Report already omits the argument.

**The boost is added to priority and never to specificity**, and that is what makes "still has to qualify on its own merits" a property rather than a rule anybody enforces. Specificity is `criteria.size` and is compared first, so a boosted family cannot pass a narrower observation however large the integer. Qualification happens in step 1, before any ranking, against criteria the boost cannot reach. And the boost is one, because any positive value orders two equal observations identically and a larger one would only start crossing authored priorities inside a specificity level, which are the corpus author's ordering rather than layer 6's to overrule.

**The rule that keeps it safe fits together with 10.4 rule 4.** The boost raises the motivating family, so a situation that persists is more likely to appear; and a family that appears is exactly what stops another plan being stacked on top of the first. The mechanism that would cause the nagging is the mechanism that prevents it.

**The non compliance test was written before this**, in the order section 19 asks for, and it holds the door shut from four directions rather than one: no line in any section 4 bench matches the forbidden vocabulary, `PlanHistory.Accepted` has three fields and all three are keys, no public member of `FollowThrough` returns text, and a year of accepting every plan and acting on none of them produces no sentence referencing one. `PlanVocabulary` holds the forbidden shapes in main source so that the unit test and the simulator check read one list.

### 10.7 Silence

Across the persona set, layer 6 must return `Nothing` or a non-plan closing on **at least 15 percent** of reports. A report that always has advice is a report inventing problems.

**Phase 9b, measured: 41 percent.** Across eleven persona years, 185 of 451 reports carried no plan. 179 of those were a non plan closing from 4.6 and 6 were nothing at all. **This read 43 percent when layer 6 was built and 41 once the guidance language pass landed**, because the two cues that pass revived make a few more weeks plannable; the earlier figure described a tree that existed for an afternoon and the number in section 12's tenth column is the one taken over all three lanes together. The check is enforced from this phase rather than deferred, and it was the last of the ten in section 12 that could not be measured because the thing it measures did not exist.

**One property 7.6 gives every other bench, this one does not have.** A non plan closing is not recorded anywhere: there is no event for it, `REPORT_GENERATED` carries sections rather than a closing, and `FiringHistory` is derived from the log. So the ninety day variant exclusion cannot see a closing line, and the same one can return inside it. The bench a person actually meets is one of the four sub benches rather than the whole of 4.6, and with `cls.trust` at eight lines the simulator found a closing repeating after seven days on a persona whose weeks keep their shape. **Closing it properly means a field on a committed event payload**, which is a change to `docs/EVENT_FORMAT.md` and outside this phase.

**What was done instead is the only thing left, which is bench size**, and the guidance language pass took 4.6 from twenty four lines to seventy eight: `cls.trust` from 8 to 23, `cls.let` from 7 to 20, `cls.new` from 4 to 17 and `cls.rev` from 5 to 18. That is the warm tier of 11.1 for a bench a person meets roughly seventeen times a year, and it is sizing rather than a fix: the choice is `VariantChoice` over the week key, deterministic across devices, and it rotates through a bench two to four times larger than the one that repeated inside a week.

---

## 11. The corpus

### 11.1 Sizing

By expected firing frequency, never evenly. Even sizing produces repeats exactly where they hurt most, because a handful of families fire fifty times a year and most fire four.

| tier | firings per year | variants per stage bench |
|---|---|---|
| hot, 36 benches measured | 40 or more | 60 to 100 |
| warm, 18 measured | 5 to 20 | 15 to 30 |
| long tail, 8 measured | under 5 | 4 to 8 |

**Three things the seventh measurement says about this table, and none of them is an argument for writing more.** The estimates of roughly fifteen hot families and roughly thirty warm ones were the phase 5 guesses; measured over eleven persona years they are 36 and 18, and the counts above are now the measured ones.

**The table has a hole between 20 and 40 firings and eight families sit in it**: `freshStart` 39, `focusInvestment` 36, `throughput` 35, `netInflow` 33, `dayShape` 31, `steadyPace` 28 as a headline, `familiarDip` 24 and `neglectedArea` 23. Read strictly they are warm and want 15 to 30 lines, which is what they have; read by proximity they are nearly hot. Nothing has gone wrong, and the boundary is stated here so the next pass chooses rather than rounds.

**The table has no tier above 40 and six families fire between 335 and 1,308 times a year.** `weekMixed` 1,308, `singleAreaWeek` 1,054, `balancedWeek` 831, `weekStarting` 804, `comeback` 604 and `steadyStretch` 335, all of them Momentum or the banner, which recompute on every app open rather than once a day. A bench of a hundred lines does not hold a ninety day exclusion against three hundred firings in one person's year, and `bn.mixed.25` was said 101 times across the run. **That is a cooldown or a throttle decision and not a bench**, and it is where the 3,898 remaining variant repeats live: 95 percent of them are those two surfaces.

**The number in the third column was not the number the chooser saw, and 7.4 is what fixed it.** `RegisterChoice.preference` returned registers in order and `Realizer.realize` took the first one it could fill, so a stage bench of 60 split three ways was three benches of 20 and only one of them was ever offered: `quietDay` stage 1 holds 67 lines and the seventh measurement reached the 21 plain ones across 212 firings. **Measured where the choice is made, phase 9's hot benches were warm.** The two ways out were restating this column per register or making the realizer choose across the registers it can fill, and the second one landed: step 4 of 7.4 is now a tier chosen among rather than an order, so a stage bench is a bench again and this column means what it says. Section 12 carries the measurement, and the register table in it is the reading that closed this.

Current, counted rather than estimated: **1,775 Pulse lines producing 291,807 surfaces, 2,357 Report and guidance lines producing roughly 40,500, and 810 Momentum and banner lines producing 3,797.** Combined, 4,942 authored lines and roughly 336,000 surfaces. **Every figure here is now recounted from the file rather than owed.** Two of them were left open on purpose and both have since landed: volume 3 closed while volume 1 was mid growth, its Pulse line count was 1,380 and climbing at the moment that sentence was written, and a figure recorded then would have been wrong before it was read. The Report line count was the last of them, and it moved from 804 to 1,617 in the pass that grew the ten hottest Report families; `CORPUS_2_REPORT.md` states the same figure in six places and all of them moved together, which is why they belonged to one pass over that volume rather than to a second hand correction here. Storage is not a consideration; quality is the only constraint. All three volumes now have their hot families inside the band in the table above. Volume 2 closed last, taking its final nine hot benches from 8 to 18 lines each to 60 and moving from 1,617 lines to 2,200. **It moved once more, to 2,357, when phase 9b's guidance pass grew section 4**, which is the last movement any figure in this paragraph is owed: 157 lines across six batches into the frames, the cues, the eight action banks and the four non plan closings, sized against how often layer 6 actually fires rather than evenly. **The 1,874 this sentence held before that pass was never a count of anything**: it was written when the Report file carried 1,617, and `CorpusTotalsAuditTest` had been failing on it, here and in `CORPUS_3_MOMENTUM.md`, from the moment it was written. A projected total is the same defect as a stale one and the audit does not distinguish them.

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

**Nothing is written on a day the app was not opened, and that was a defect for six phases.** A persona answers two questions, whether it is there and what it did, and only the first was ever conditional: `act` was called on every day of the year regardless. So `sporadic` and `abandoning` wrote `ITEM_ADDED` and `ITEM_COMPLETED` onto days carrying no `APP_OPENED`, **which the real app cannot produce**: ninety six such days across the two of them in a single year, and every measurement below was read through an instrument that could hold them. It is the same class of defect as the persona set that could not finish a backlog and it is fixed the same way, in the instrument rather than in each life: `SimulationPersona.isPresentOn` is the one gate and the simulator, `ReportPersonaTest` and `CapacityGatePersonaTest` all apply it. **The install day is inside the gate**, because `AREA_CREATED` is a screen gesture like any other and an install day nobody opened would put the same impossible event one line earlier. A session that falls on a day nobody was there does not happen and is **not** moved to the next day opened: `roll` is a hash of the day and of nothing else, which is the property the whole persona file rests on, and a session carried forward would make what happens on a day depend on the days before it.

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

The readings the deferred checks produce are the baseline phase 9 is judged against and the full tables, with the reasoning, are in `DECISIONS.md` and `docs/BUILD_STATE.md`. **Ten measurements exist and the tenth is the current one.** Phase 5, the facts phase, the slot bindings phase, the rules pass, the pass that repaired the persona set, the pass that built the two family scope gates of `MASTER_BUILD_PROMPT.md` 14b, phase 9's authoring, the integration pass that ran the register chooser, the slot bindings, the report composer and three corpus corrections together for the first time, the pass that bounded the Pulse repeat filter, and phase 9b, which is layer 6, its language and its surface run together for the first time. Over the same eleven personas and the same simulated year each time. **This sentence read eight for two passes after the ninth column was already in the table below**, which is worth one line: a count kept in prose beside a table that grows is a count that goes stale silently, and it did. The twelfth persona `cyclicalDips` is deliberately outside `SimulationPersona.ALL` and outside every column: every measurement recorded here is quoted against the eleven, and a twelfth would move all of them silently.

**The sixth measurement was the first run of the repaired instrument.** The fifth was taken before the presence fix above, so it was read through an instrument that could hold events the app cannot produce. The move it cost is small and it is entirely in the two personas the defect touched: `sporadic` 51 to 65 and `abandoning` 75 to 88, aggregate silence 63 to 65. Every other persona is unchanged to the point.

| reading | target | phase 5 | facts | bindings | rules pass | persona repair | the gates | phase 9 | the integration pass | the recency bound | layer six |
|---|---|---|---|---|---|---|---|---|---|---|---|
| Pulse silence, every persona together | 8 to 25 percent of opened days | 76 percent | 73 percent | 68 percent | 68 percent | 63 percent | 65 percent, 65.7 exact | 65 percent, 65.7 exact | 65 percent, 65.7 exact | **55 percent, 56.0 exact** | **55 percent, 55.8 exact** |
| Pulse silence, per persona | the same band | 43 to 98 | 42 to 98 | 40 to 97 | 40 to 97 | 37 to 97 | 37 to 97 | 37 to 97, none in band | 37 to 97, none in band | **35 to 72, none in band** | **35 to 72, none in band** |
| layer 6 silence | at least 15 percent of reports | not measured | not measured | not measured | not measured | not measured | not measured | not measured | not measured | not measured | **185 of 451 reports, 41 percent** |
| Pulse families that ever fired | 11 of 11 | 6 of 11 | 7 of 11 | 8 of 11 | 8 of 11 | 11 of 11 | 11 of 11 | 11 of 11 | 11 of 11 | **11 of 11** | **11 of 11** |
| every family the corpus declares fires | every one | not measured | 58 of 78 | 60 of 78 | 65 of 78 | 71 of 78 | 69 of 78 | 70 of 80, and 69 of the same 78 | 71 of 80 | **72 of 80** | **72 of 80** |
| every stage of every hot family fires | all | 29 hot, one gap | 31 hot, two gaps | 33 hot, two gaps | 35 hot, two gaps | 36 hot, one gap | 36 hot, one gap | 36 hot, one gap | 37 hot, one gap | **36 hot, one gap** | **36 hot, one gap** |
| no variant repeats inside ninety days | none | 7,384 | 7,430 | 7,445 | 7,376 | 7,418 | 7,370 | 3,898, tightest after 1 day | 2,411, tightest after 1 day | **2,286, tightest after 1 day** | **2,328, tightest after 1 day** |
| layer 5 vetoes across the run | none | not reported | not reported | 107, every one check 1 | 0, and 92 absences | 0, and 85 absences | 0, and 38 absences | 0, and 41 absences on purpose | 0, and 39 absences on purpose | **0, and 41 absences on purpose** | **0, and 41 absences on purpose** |
| pattern slots, and their concentration | no family holds a section | not reported | not reported | 416 of 419, 8 families, top three 402 | 401 of 419, 12 families, top three 296 | 399 of 419, 13 families, top three 295 | 397 of 419, 12 families, top three 298 | 397 of 419, 12 families, top three 298 | 397 of 419, 12 families, top three 298 | **399 of 419, 12 families, top three 305** | not re-measured |
| no family over a fifth of a year's Pulses | 20 percent | 27 to 60 | 25 to 57 | 25 to 51 | 25 to 51 | 25 to 48 | not recorded here | 25 to 48 | 25 to 48 | **25 to 97** | **25 to 97** |
| no two consecutive report leads share a band | none | 715 | 725 | 716 | 712 | 719 | not recorded here | 277 | 289 | **305** | **302** |
| no three consecutive parallel numeric clauses | none | 27 runs | 37 runs | 36 runs | 41 runs | 41 runs | not recorded here | 121 runs | 148 runs | **148 runs** | **148 runs** |
| lines the engine cannot say | none | not measured | not measured | not measured | not measured | not measured | not measured | 86 | 2 | **2** | not re-measured |
| unbound markers in the corpus | none | not measured | not measured | not measured | not measured | not measured | not measured | 99 across 80 lines | 0 across 3,828 lines | **0 across 3,828 lines** | not re-measured |
| registers a surface actually reaches | every one authored | not measured | not measured | not measured | not measured | not measured | not measured | one, on five of the six surfaces | every one its corpus and its rules can offer, on all six | **unchanged, every one on all six** | not re-measured |

**The tenth measurement is phase 9b, and it is the first run in which layer 6, its language and its surface exist together.** Three lanes built it without ever running the suite as one tree, so the first thing this column establishes is that the three agree: `verifyClarity --rerun-tasks`, 44 of 44 tasks executed, **1,076 tests, 0 failures, 0 errors** and the one deliberate skip that is `CorpusReviewGenerator`. An up to date run of the same task returns green in 541 milliseconds and proves nothing, because the corpus files are read at runtime and Gradle does not track them as inputs; every number in this column is from a forced run.

**Layer 6 silence is the row that could not be measured for five phases, and it reads 41 percent.** 185 of 451 reports across the eleven personas carried no plan, 179 as a complete non plan closing from 4.6 and 6 as nothing at all, against a floor of 15. It is enforced from this phase rather than deferred, and it is the last of the ten checks in this section to become measurable. Being nearly three times the floor is not slack: the four closing benches are what a person actually meets on five weeks in six, which is why 4.6 was sized at 78 lines rather than 24.

**The non compliance test passes, and it passes for the first time non trivially.** 1,441 invocations of `acceptsEveryPlan` were read and **0 referenced a plan, a commitment, an intention or a failure to act**. Until this phase that check was true because layer 6 composed nothing; the persona now accepts real engine composed plans, `PLAN_OFFERED` and `PLAN_ACCEPTED` are written, `PlanHistory` reads them back and the boost runs on every later report. Section 19 attaches a consequence to this reading that no other check carries, and the consequence is not triggered.

**The reading behind the count, which is what section 19 actually asks for.** A count cannot say whether the reports around an accepted plan feel different, so the year was measured against itself: the same eleven personas were run with `FOLLOW_THROUGH_BOOST` set to 0, the two dumps were diffed, and the constant was restored and the file checksummed back to byte identity. **The boost touches 118 lines in one surface and no others.** Report observations only, on 27 of the 52 weeks, nothing on the Pulse, Momentum or the banner, because every caller outside the Report omits the argument. On 51 of the 52 weeks it changed the order of four true observations and not one word of what was said. **A reader cannot see the counterfactual, so on those 51 weeks there is nothing to notice.**

**On one week it changed what was said, and that week is the finding.** 2026-09-06 is the persona's only `hardStretch` week of the year. Without the boost the report carries `The stretch is three weeks old.` and layer 6 returns `Nothing`, which is 10.4 rule 6 working. With the boost, `queuePressure` and `hardStretch` are at equal specificity and equal priority, the boost breaks the tie the rule key would have given to `hardStretch`, `hardStretch` falls outside `MAX_OBSERVATIONS`, and because `heavy` reads off what appeared rather than off what qualified, **the heaviest week of the year stops looking heavy and gets a closing.** So an accepted plan can remove the one family 6.4 exists for and turn a deliberate silence into a sentence.

**It is not layer 6's defect, and the instrument says so rather than the argument.** `Selector.selectObservations` was temporarily patched to report every week where `hardStretch` qualified and was crowded out, the eleven years were re-run, and the patch was reverted. It happens **twice in eleven simulated years: once with a non empty boost set and once with an empty one.** A cap of four and a tie broken on rule key can drop `hardStretch` with no plan anywhere near it, so this is a report composer property that layer 6 is one more way to trigger. It is recorded here rather than fixed here because the remedy is a change to rule 6's stated reading, which 10.4 argues for deliberately, and that is the owner's call rather than this phase's.

**The ninth reading's repeat figure was taken twice and the second one stands.** The
Selector pass measured 2,407 immediately after its own change; the Areas banner's
`weekQuiet` bench then went from 8 lines to 60 in the same afternoon, and a family firing
240 times a year off eight lines is where a hundred repeats live. The regenerated review
measures **2,286** over the tree with both changes in it, and that is the number in the
table. The 2,407 was correct when it was taken and describes a tree that existed for about
an hour.


**The ninth measurement is the Pulse repeat filter bounded to yesterday, and it is the current column.** Nothing else moved: the same eleven personas, the same year, the same corpus to the character, the same rules, the same instrument. One filter changed its reach and **silence fell from 65.7 percent to 56.0**, which is 2,067 silent Pulse days to 1,762. That is 9.7 points, larger than every previous pass put together and smaller than the removal the eighth measurement priced, which is the shape a correct answer to this question was always going to have.

**What the eighth asked, and what the answer turned out to be.** It left the filter alone and recorded one open question, remove it or not, at a price of 574 spoken days. The narrower question underneath it had never been measured: whether the filter's only distinct effect is blocking a **different subject** of the same family, which nobody has ever argued for. Two readings answer it, both taken over the same eleven persona years, by the same kind of temporary instrumentation, reverted the same way.

- **Of the 869 days where this filter alone emptied the candidate list, 366 carry a candidate whose subject differs from the last Pulse's, and 359 of those would speak if the filter were keyed on `(family, subjectId)`.** The cross subject share is 42 percent of the days and narrowing buys 41 percent of the filter's cost
- **Every Pulse family in 7.3's table declares three days of cooldown or more.** On consecutive days step 5 already blocks the exact `(family, subjectId)` pair, so narrowing this filter to that pair is an **exact no-op on the one day the rule is about**. Measured rather than argued: of the 691 same pair candidates it dropped, not one at a gap of a single day escaped its own family's cooldown

**Those two readings rule narrowing out, and they invert the reason it looked like the safe option.** Narrowing does not keep the protection the rule was written for and drop only the cross subject blocking. It removes exactly the part the specification states, because on consecutive days the cooldown is already standing behind the pair, and it keeps exactly the part the specification never asked for, because after a long gap it goes on blocking off a Pulse nobody remembers. Cross subject blocking on consecutive days is not a side effect of step 4. It is the whole of what step 4 contributes that nothing else does.

**The defect was recency, and it was sitting inside the eighth's own numbers.** Of those same 869 days, **169 were a gap of one day**. 287 were two to six days, 121 were a week to a month, 78 were one to three months, and **214 were ninety days or more**. A rule documented three times over as covering yesterday was firing, in a quarter of its cases, off a Pulse at least a season old. `PulseFacts.lastGeneratedFamily` carries no recency of its own and the code took the fact's name for the rule. Section 5 step 4 now states the bound and `Selector.REPEAT_WINDOW_DAYS` is it.

**The absorbing state was the second half of the problem and it is gone.** The eighth recorded that `balancedAcrossFour` speaks nine times, all between January 5 and January 20, and is then filtered on 348 consecutive days, and said that for that life silence is a state rather than a rate that no change to a wait could reach. **It was this filter**, and the mechanism is a loop rather than a rate: a family blocked at step 4 writes no `PULSE_GENERATED`, so `lastGeneratedFamily` never advances, so the block renews itself every morning to the end of the year. Four evenly used areas qualified `persistence` on a different item almost every day and every one of them was discarded at family level before any bench was consulted. **That persona now hears 183 Pulses rather than 9, its silence is 49 percent rather than 97, and its longest silent run is one day rather than 348.** The bound makes the loop unreachable rather than unlikely, because a day the Pulse did not speak is a day this filter does not apply at all.

**Two readings moved the wrong way and one of them is the finding under the finding.** Consecutive report leads sharing a length band went from 289 to 305, by the mechanism the eighth already recorded for it: more Pulses mean more answers, more answers change which report leads are chosen, and 7.5's band rule is a preference inside a bench rather than a cap over a page. **The one that matters is family concentration, whose worst persona went from 48 percent to 97.** `balancedAcrossFour` takes 178 of its 183 Pulses from `persistence`. That is less a cost of the bound than something the bound made visible: with four evenly used areas no area ever holds seventy percent of a window, so `concentration` cannot qualify at all, `spread` fires once, and `persistence` is the only family whose rules that life satisfies. Silence used to hide it. **A person who hears one family every other day is a rule coverage finding rather than a rhythm one**, it belongs to the `NO_RULE_QUALIFIED` column, and it is the next thing to measure rather than a reason to put the block back.

**Where silence is now, and it is a different shape.** 1,762 silent days of 3,148 opens, splitting **907 nothing qualified, 844 filtered, 11 insufficient**. The filtered column has gone from the majority to the minority for the first time in nine measurements, and inside it the repeat filter fell from 869 days to 299, every one of them now a genuine gap of one day. Cooldown is the largest filter at 481, having absorbed part of what the repeat filter used to take. **Rule coverage is the whole of the rest**, which is where the fourth measurement predicted the floor would be, and this is the first run to actually arrive there.

**What is still not decided, and it is a fifth of the size it was.** Removing the filter outright would let one family speak two days running and would unlock a further 120 days, which is 3.8 points. That is a behavior question, it is the owner's, it is unchanged in kind by this pass, and it stays recorded in `DECISIONS.md` with the recommendation stated and not taken. What changed is the price of leaving it alone: it was 574 days, and it is 120.

**The seventh measurement is the first run after phase 9 wrote into the corpus, and it is the current column.** Nothing else moved between it and the sixth that touches the Pulse: the same eleven personas, the same year, the same rules, the same instrument but for a defaulted observer the gate suite added to `ClaritySimulator`. Two Report families landed in between and are the whole of the coverage denominator's move from 78 to 80, `familiarDip` and `estimateCalibration`, of which the first fires 24 times and the second not at all. Against the same 78 families the sixth counted, this run is 69, which is the sixth's number exactly. What did change is the corpus, from the 1,503 lines the last commit records to **4,733**, and every hot bench in all three volumes is now inside 11.1's band.

**Silence did not move by one day, and that is the finding rather than a disappointment.** 2,067 silent Pulse days out of 3,148 opens, splitting 1,161 filtered, 895 nothing qualified, 11 insufficient, which is the sixth measurement's split to the day. **The prediction on record was that emptying the filtered column would leave silence near 28.7 percent, and the column did not empty by a single day, because bench depth cannot empty it.** That conditional was never testable by authoring, and this run is what shows it:

- `VariantChoice.choose` returns a line whenever the bench is not empty. When every line has been used inside ninety days it drops the most recently used one and reuses the rest, per 7.6 step 2. **A bench can be exhausted and still speak, so depth cannot produce silence at all**
- `Realizer.realize` returns `NotProducible` only when no line at any register can be **filled** from the facts on hand, which is a question about slot bindings and rule shape, not about how many lines a bench holds. A hundred lines carrying an unbound marker are as silent as one
- The rest of `ALL_QUALIFIED_RULES_FILTERED` is produced at step 6 of `Selector.select`, from family availability, callback resolution, the horizon, the Pulse repeat filter and the cooldowns. **None of those five reads the corpus**

So the column has two producers, one in the selector and one in the realizer, and neither is sensitive to bench size. 3,230 new lines changing the count by zero is the empirical half of the same statement. **The eighth measurement instrumented the five and found the realizer produces none of it, availability and callback resolution produce none of it, and the Pulse repeat filter produces three quarters**; the reading and the counterfactual are below, under which of the two situations this is. This paragraph named the candidates correctly and did not rank them, and the ranking is the thing that turned out to matter.

**What phase 9 did move, it moved a long way.** Variant repeats inside ninety days fell from **7,370 to 3,898**, and two consecutive report leads sharing a length band fell from **719 to 277**, which no brief predicted and which is the length band work in the gate suite arriving on a surface. Layer 5 vetoed nothing, absences named on purpose rose from 38 to 41, and the pattern section's slot allocation is unchanged in every figure.

**The eighth measurement is four passes run together for the first time.** The register chooser reopened 7.4 step 4, the slot bindings pass decided the eighty six lines the gates recorded as unsayable, the report composer turned 9.2's parallel clause cap from a drop into a preference, and three corpus corrections reworded seventeen lines. Each pass verified itself alone; none of them had run beside the other three. The integrated tree builds and every gate is green at **1,024 unit tests**, taken from a full `--rerun-tasks` build rather than an up to date one, which matters here because the corpus files are runtime reads and a cached green over them is the failure this repository has already had once.

**Three cells of this column are not what the register pass measured alone, and the difference is the bindings pass.** Measured on a tree carrying only the register change, variant repeats read 2,414, band collisions 288 and parallel numeric runs 138; integrated they read **2,411, 289 and 148**. `Realizer` drops every variant in `SlotBindings.EXCLUDED` from the bench it picks from, so retiring sixty two lines and binding twenty two changes which line is chosen on days neither pass was thinking about. The report composer cannot move either rhythm row at all, because `ClaritySimulator` never calls `ReportComposer`, and the seventeen corpus rewordings cannot move the numeric row either, because every one of those lines already rendered a digit out of a slot. A column measured on one pass's own tree is not the column, and this is the arithmetic of why.

**Every surface is now heard in every register its corpus and its rules can offer, and five of the six were heard in one.** The seventh measurement read plain on 1,080 of 1,081 Pulses, reflective on all 5,594 Momentum and banner firings, and observational on every Report observation that was not editorial or neutral agent. The eighth reads: morning Pulse 541 plain to 540 observational; Momentum 1,052 plain, 1,007 observational, 1,074 reflective; the banner 788, 840, 760 and 240 neutral agent; the Report observation 327 plain, 320 observational, 390 editorial, 546 neutral agent. The Report headline and the Report pattern are still plain alone and no chooser can change that, because `ReportWalker` refuses a register tag outside section 2 of `CORPUS_2_REPORT.md`. **The Pulse's reflective bench is still unmeasured**, because the simulator builds only the before 17:00 window; that is the instrument and it is the ninth measurement's job.

**Variant repeats inside ninety days fell from 3,898 to 2,411 with no line written**, which is the reading that says what the defect cost. The benches were always that deep. `AREAS_BANNER weekQuiet` went from zero firings in every measurement ever taken to 240 a year, 167 of them on windows where nothing had spoken at all, which is what makes it the eighth's one movement in family coverage, 70 of 80 to 71.

**Two readings moved the wrong way and both are rhythm rather than truth.** Consecutive report leads sharing a length band went from 277 to 289 and runs of three parallel numeric clauses from 121 to 148. The mechanism is the same for both and it is a consequence rather than a bug: 7.5's band rule is a preference applied inside the bench the realizer is about to pick from, and that bench is now one register of a stage chosen for its voice rather than the whole of the first register that could fill. **The fix was considered and rejected**, because it would let rhythm choose the register: 7.4 decides the voice and 7.5 expresses a preference inside it, and inverting that puts cadence above the one thing in this section that is about what a person can hear. Twelve collisions across 451 reports is the price, and 11.4 already says rhythm is worth a line rather than a paragraph.

**One reading moved the wrong way, and it is a composition property rather than a line.** Runs of three or more parallel numeric clauses went from **41 to 121**. Deeper benches gave the composer more numeric leads to place next to each other, and 9.2's band rule survives that because `Realizer.choose` honors it as a preference on every pick while nothing anywhere honors the numeric rule. It stayed invisible while the benches were too shallow to expose it.

**That paragraph called it the one stated rule of section 9 with no implementation, and the pass that answered it found two things wrong with the sentence.** The rule had an implementation, `ReportComposer.parallelClauseCap`, which dropped the third numeric lead; under its own narrow reading of what a numeric clause is it fired **twice** across eleven persona years, and both times it removed a true observation, which 11.4 forbids. And **this row does not measure the composer at all.** `ClaritySimulator` drives the engine directly and never calls `ReportComposer`, so this number and the length band row beside it are properties of the engine's rank order rather than of a page anybody would read: the rank order is what the realizer's own preference acts on, which is what the paragraph above correctly attributes the move to. Neither row can move when a composition rule changes. The composed page is measured separately, by `ReportRhythm` over the same eleven persona years, and 9.2 records what it read before and after.

**The reading that changes what a bench is.** `RegisterChoice.preference` returns registers in order and `Realizer.realize` takes the first one it can fill; it never chooses among them. Across the run that means **one register per surface**: the Pulse spoke plain 1,080 times out of 1,081, Momentum and the banner spoke reflective on all 5,594 of their firings, the report headline and the pattern section spoke plain on all 837, and only the report observation reached four. Two different causes sit under that. For the Pulse it is the instrument: 7.4 gives the Pulse a time of day rule and `PulseSchedule.dayAt` implements it by ending the window at the day boundary before 17:00, so the simulator's fixed 07:00 open makes every one of its Pulses a morning one and the reflective bench is never asked for. In the shipped app the evening branch reaches it. For Momentum and the banner it is the app: `RegisterChoice.preference` has no branch for those two purposes, so the bare fallback's head wins every time and **487 of volume 3's 748 register tagged lines are in registers nothing can ask for**.

**That is why a hot bench is not the size 11.1 states.** The realizer chooses inside one register of one stage and 11.1 sizes the stage. `quietDay` stage 1 holds 67 lines and the chooser saw 21 of them across 212 firings, which is warm by 11.1's own table. The corrected statement of the target is in 11.1.

**Which of the two situations this is.** The seventh measurement named four things in the way and said all four were cheap to state as work. Three of them are now done: every surface reaches every register its corpus and its rules can offer, 9.2's parallel clause rule has an implementation that is a preference rather than a drop, and the eighty six lines waiting on a binding are two. **The fourth is untouched and it is the whole of the remaining problem.**

**Silence did not move by one day, for the third measurement running.** 2,067 silent Pulse days out of 3,148 opens, 65.7 percent, splitting 1,161 filtered, 895 nothing qualified, 11 insufficient. That is the sixth measurement's split, and the seventh's, to the day. Between the sixth and this one the corpus grew by 3,230 lines, the register chooser was rebuilt, eighty six bindings were decided and seventeen corpus lines were reworded, and the number did not move at all. **A reading that survives four passes aimed near it is not noise, it is a mechanism, and this pass measured which one.**

**The filter chain was instrumented and the 1,161 is one filter.** `Selector.select` was patched temporarily to record which of its five filters emptied the candidate list, the year was re-run over the eleven personas, and the patch was reverted and the file checksummed back to byte identity. Of the 1,164 Pulse days the instrument saw:

| filter | days | share |
|---|---|---|
| the Pulse repeat filter, yesterday's family cannot be today's | **878** | **75.4 percent** |
| cooldown | 268 | 23.0 percent |
| horizon | 18 | 1.5 percent |
| family availability | 0 | none |
| callback resolution | 0 | none |

Availability reads zero because no Pulse family is in either table of `MASTER_BUILD_PROMPT.md` 14b: `RE_ENTRY_PURPOSES` excludes the Pulse outright and `PRECEDENT_GATED` holds six Report families and nothing else. `ALL_CANDIDATES_VETOED` is zero in every measurement ever taken, so the realizer never silences a Pulse either. **Three of the six ways a Pulse can go quiet contribute nothing, and one contributes three quarters.**

**One line of code is the largest single lever in eight measurements.** It is `.filterNot { purpose == Purpose.PULSE && it.rule.family == facts.pulse.lastGeneratedFamily }`. Its shape is what makes it expensive: 366 of the 878 are a day where `persistence` was the only family that qualified and `persistence` had spoken the day before, 271 are `quietDay` alone, 168 are `concentration` alone. On those days a stage holding sixty to a hundred lines is discarded whole, at family level, **before `VariantChoice` is ever asked for a different sentence.** That is the mechanical reason 3,230 corpus lines moved silence by zero, and it is not the reason recorded up to now.

**Measured, not estimated: removing it would take silence from 65.7 percent to 47.4 percent.** The same instrumented run computed, for every repeat blocked day, whether the candidate would also have been caught by the cooldown standing behind it. **574 of the 878 would speak; 304 would not.** The two filters are not redundant because they are keyed differently: the repeat filter is keyed on the family alone, and cooldown on the `(family, subject)` pair, so a new item in the same family clears the cooldown and is still refused by the repeat filter. An 18.3 point move is larger than every previous pass put together, against 5 points for the persona repair, 2 for the presence fix and 0 for the corpus.

**It is still not a way into band, and that is the honest half.** 47.4 percent is above the 25 percent ceiling, and what remains is 895 days where nothing qualified at all, which is rule coverage rather than a filter, plus 572 days of cooldown. **Silence has never been one problem.** Its second half is visible in the shape rather than the size: `balancedAcrossFour` speaks nine times, all of them between January 5 and January 20, and is then filtered on 348 consecutive days to the end of the year. For that life silence is an absorbing state and not a rate, so a change that shortens a wait cannot reach it at all.

**The last sentence is right and its conclusion was wrong.** No change to a wait reaches an absorbing state, and this one was not produced by a wait. The ninth measurement found the loop that produces it, in this filter, and bounding the filter to yesterday takes that persona's longest silent run from 348 days to one.

**What this does not decide.** The repeat filter is a deliberate rule and removing it lets one family speak two days running. Whether that is worse than 574 silent days is a question for the owner and not for a measurement, and it is recorded as open in `DECISIONS.md` rather than acted on. The standing instruction that an app shipping at 30 percent silence is better than one that does not ship is older than this pass and is unchanged by it.

**Superseded by the ninth measurement, above.** The 574 was priced against a filter with no recency bound at all, and most of it turned out to be the missing bound rather than the rule. With the bound in place the removal question is worth 120 days and not 574, and it is still open and still the owner's.

**The fourth measurement was taken through an instrument that could not represent somebody finishing things, and this one is not.** Every persona reached the log through `SimulationPersona.work`, which takes a capture count and a completion count as adjacent parameters, and every call site in all eleven personas passed a completion count no greater than its capture count. Nobody chose that; it is what two adjacent numeric parameters invite. The result was eleven synthetic lives in which `additions >= completions` held on every day and therefore every week, with per area daily completions capped at two, which made `throughput`, `netOutflow` and `intakeVsOutput` stage 3 impossible and starved `burst` and `queueDrain`. A person who clears a backlog on a Sunday is completely ordinary and no persona in this section could do it. `SimulationPersona.clearOut` is the act `work` could not express, a sitting down that finishes and captures nothing whose size comes from the queue rather than from a literal, and four personas have one. **`acceptsEveryPlan` must never have one and does not.**

**Phase 9's job is named by the fifth reading, and the owner named it in advance.** If silence landed near band, phase 9 would be authoring to fix repeats; it did not, so **phase 9 is authoring to fix silence.** Those are different jobs and the difference is what a bench is grown for.

**Silence is 65.7 percent against a ceiling of 25, and the fifth measurement's reading of it survives its own instrument being repaired.** The 2,067 silent Pulse days split into 1,161 where a rule qualified and every candidate was filtered, 895 where nothing qualified at all, and 11 with too little data. **A bench deep enough to empty the first column entirely would leave silence at 28.7 percent**, against 29.0 at the fifth measurement, which is the same finding to within a rounding: bench depth is necessary and provably not sufficient. At that floor five of the eleven personas are in band, five are above it and `sporadic` is below at 6 percent, which is the one thing the repaired instrument changed about the shape rather than the size. The owner's standing instruction is that this is reported and not ground at: an app that ships at 30 percent silence is better than one that does not ship.

**Two points of the move are the instrument and none of it is the gates.** The sixth measurement is the first run of the repaired instrument and also the first run carrying the capacity gate and the re-entry withholding, so the two were separated by a control: the same year with `FamilyAvailability.unavailable` returning null throughout. **Its silence table is identical to the sixth measurement in every cell**, which is not a coincidence and did not need measuring twice to be believed: no Pulse family appears in either gate's table, and `RE_ENTRY_PURPOSES` excludes the Pulse outright because 14b.4 gives the Pulse a different and older rule above layer one. So 63 to 65 is the presence fix, entirely, and the gates cost no Pulse sentence at all.

**What the gates do cost is two report families and sixty three absence sentences, and both are the point.** Against that control, `quietWeek` as a headline goes from firing to dark, which is one family of the two the coverage row lost; the other, `abandonmentPattern`, went dark from the presence fix and not from the gate, because `abandoning` no longer writes on the days it is not there. Absences named on purpose fall from 101 to 38: `neglectedArea` and `areaGoneQuiet` are both gated on `AreaFacts.dipPrecedent`, and sixty three of the hundred and one were an area falling quiet in the same way it had fallen quiet before. **A family going dark is a cost here and the coverage row prices it as one**, which is correct and is why the row moved down; what it buys is priced in 14b.9 and not in this table.

**The three acceptance criteria of `MASTER_BUILD_PROMPT.md` 14b were measured over the same year and all three pass.**

- **14b.9, the capacity gate.** `cyclicalDips` receives **zero** decline, neglect or fading observations across fifty two weekly reports. The same year composed a second time with all three precedents forced to `NONE`, which is the report this person would have received before 14b.9 existed, produces **34** of them, in `decliningActivity`, `focusHabitFading`, `hardStretch`, `neglectedArea` and `quietWeek`. All 34 sit on a fall whose precedent reads `PRESENT`, so the silence is the gate and not a ranking, a cooldown, or a family that never qualified
- **14b.8, the estimate delta.** **Zero** in 13,576 rendered strings across twelve persona years. Not one line states a delta, and not one line mentions an estimate at all, which is the stronger reading: no quantity of minutes exists anywhere above `TrailQueries.estimateOutcomes`, so the subtraction is unformable rather than merely vetoed, and check 11 is a backstop with nothing yet to catch
- **14b.4, the week after a return.** `longDormantRevival` comes back on day 251 after 195 days away and receives 22 sentences in the seven days that follow, **none** of them a decline, a neglect or a gap. The same seven days with the re-entry half of the gate disabled produce **seven**: `A still fortnight.` on the Momentum headline on four separate days, and a report on day 252 headlined `Work moved again.` carrying `Work had been the quietest area. It was not this week.` and `6 events. All week.` That report is three fifths a measurement of the absence, on the first screen back, which is what 14b.4 exists to prevent

**The conclusion survived its own instrument being repaired, which is the strongest thing that can be said for it.** Family coverage did not: six of eleven, then seven, then eight was never a reading of corpus depth or of rule thresholds, it was a reading of a persona set in which nobody ever finished a backlog, and it is 11 of 11 the first time a life in the set can. Silence was overstated by five points and no more, because it was high for reasons that had almost nothing to do with completions.

**Two things beside it are worth reading before authoring anything, and neither is fixed by authoring.** First, seven of the nine families that never fired now do: `throughput` 35, `burst` 12, `queueDrain` 6, `queueDrained` 11, `clearing` 2, `netOutflow` 1 and `improvingThroughput` 1, with `intakeVsOutput` reaching stage 3 for the first time. Every family the rules pass named is among them, the stage it named closed, and `improvingThroughput` lit without being predicted; nothing it diagnosed as needing something other than these two fixes moved. What remains dark is `fragmented`, which wants a persona that both hoards and switches, `queuePressure` on the headline, `shiftingFocus`, `weekendShift`, which no persona can reach because none of them knows what day of the week it is, and `accumulation` stage 2, which is the last short stage. **`weekQuiet` stood on that list until the register pass and no longer does**: it was dark because 7.4 could not reach the register its whole bench is written in, rather than because no life reaches its trigger, and it now takes 240 banner windows a year. Second, `insufficientData` is not among them. Its rule was unreachable by construction, the Report renders that bench itself through `ReportLanguage`, and `ReportRules.RENDERED_DIRECTLY` records it as a family that left the engine on a decision rather than a family that went quiet.

**`queueDrainedFrom` is justified on correctness and not on coverage, measured.** A control run of the current engine against the pre-fix persona set is byte for byte identical to the fourth measurement, so the fact changed nothing on its own. A probe of the fixed personas with the fact computed the old way, as the boundary queue, isolates what it does buy: it doubles `queueDrained` from 5 to 11, it is solely responsible for `clearing`, which is dark at 0 under boundary anchoring, and it does not move `pulse.queueDrain` by one firing, because a Pulse window is one day and the queue an afternoon clears already sits at that day's opening boundary. It moves no silence number at all. What it prevents is `drainedByFinishing` degenerating into `completions >= 0` on exactly the shape these personas now produce, a queue built inside the window and emptied inside it.

**The check 1 conflict that the third reading recorded as open is closed.** `neglectedArea`, `areaGoneQuiet` and `areaRevival` were the only families vetoed anywhere in the run, 107 times, every one of them check 1 of section 8. The owner ruled that the check was right and the writing was wrong, and section 8 check 1 is narrowed rather than widened: a rule carrying `ClarityRule.absenceSubject` may name an area with no events in the window, and only when that area has a real lifetime, is not new, and has a measured `daysSinceLastEvent`. A new empty area is still refused by every rule. The fifth reading records no vetoes at all and 85 absences named on purpose, the drop from 92 being that an area somebody clears out is an area with events in it. The sixth records 38, and the drop is the capacity gate rather than anything about check 1. **The test that restates section 9 by hand was never given this narrowing and had been failing on it since the day it landed**: `ReportInvariants` refused every one of those sentences while `ReportIntegrity` and `ClarityValidator` both passed them, 358 times over ten thousand generated weeks and 112 across the eleven persona years, and none of them was a phantom area. The four conditions are now written out there too, with their literals rather than the extractor's constants, because a second encoding that reads the first one's numbers is not a second encoding.

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
- **Veto reachability.** One test per validator check constructing a violating candidate and asserting the veto, check 11 included: `EstimateDeltaVetoTest` builds both of 14b.8's forbidden lines word for word, the permitted one beside them, and a count measure funneled into a percentage slot
- **Family availability.** Step 1b removes and never reorders. `FamilyAvailabilityTest` asserts that a fall with a precedent excludes the decline families rather than re-wording them, that `INSUFFICIENT` is neither the permission nor the veto, that the week after a return withholds every decline, neglect and gap family on the three surfaces 14b.4 names and not on the Pulse, and that a report with nothing left to say is shorter rather than padded
- **Escalation monotonicity.** A continuously true condition never shows a decreasing stage
- **Composition.** No report violates the incompatibility matrix across 10,000 generated reports
- **Register.** No `NEUTRAL_AGENT` variant contains a banned passive form. No Momentum line contains a causal construction
- **Silence floors.** Pulse 8 to 25 percent, layer 6 at least 15 percent
- **Repetition.** No variant repeats inside 90 simulated days across every persona
- **Cue substantiation.** No plan renders with a cue below threshold across 10,000 generated fact sets
- **Non-compliance.** Per section 12
- **Estimate shape.** No fact class carries a quantity of minutes, estimated or actual, and the facade hands out a ratio rather than the pair it was computed from, so `MASTER_BUILD_PROMPT.md` 14b.8's forbidden delta is unformable rather than merely caught. Checked by walking the fact classes, so a field added later fails here
- **Precedent.** A cyclical history answers `PRESENT`, a first fall after a steady history answers `NONE`, and both a short history and a fall with no room behind it answer `INSUFFICIENT` rather than either. The year long persona assertion is `CapacityGatePersonaTest`, listed in `MASTER_BUILD_PROMPT.md` 17, and it composes each week twice: once as the app now speaks and once with every precedent forced to `NONE`, which is the year this person would have had before 14b.9. The control run is the finding, the silence is asserted over **every** week of the year, and a second assertion is what stops the first from being empty: every gated observation the control run produced is checked to sit on a fall whose precedent is `PRESENT`, so a year in which nothing qualified, or in which a ranking or a cooldown did the silencing, fails there rather than passing here
- **Re-entry.** The quiet week is closed on the seventh day and open on the sixth, measured at the last day the window describes, and the only fact carrying an absence is a boolean
- **Boundary.** `dateKey` correct across DST spring forward and fall back; the 17:00 reflection switch happens exactly once per day

**Phase 5 built all of these except the ones that need a layer or a corpus that does not exist.** Purity, catalog integrity, criterion discrimination, determinism over ten thousand cases, cross-device agreement, veto reachability for every check, escalation monotonicity, composition, register and non-compliance are unit tests today. Silence floors and repetition are simulator checks, they run, they fail, and each carries the issue that lifts it. **Cue substantiation waits for layer 6**, because nothing renders a cue yet. The 17:00 half of the boundary test waits for phase 6, which owns the generation lifecycle; the `dateKey` half is held by the daylight saving tests phase 3b built.

**Determinism is run through two independently constructed engines**, with the second one handed a history whose maps are rebuilt in reverse insertion order. The engine has no clock and no random number, so neither is the risk; the risk is a map iteration order leaking into a decision, which is invisible at small scale because a hash map of three keys usually iterates the same way twice and stops doing so somewhere above that. Ten thousand fact sets and a reversed history is the cheapest available imitation of two devices reaching the same facts by different routes.
