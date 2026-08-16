# The Clarity Logic Engine

Specification for the deterministic observation system behind Clarity Pulse, the Clarity Report, and the Momentum and banner sentences in Clarity Now by Kamsiob.

This document is the authority on everything the engine does. `MASTER_BUILD_PROMPT.md` section 12 is a summary of this file and defers to it. `design.md` governs how engine output is presented, never what it says.

Read this document completely before writing any code in `domain.engine`.

---

## 1. What this is and what it is not

The engine produces sentences about the user's own behaviour that feel observed rather than generated. It has no model, no inference, no network, no randomness. It is arithmetic over an event log, plus a large library of hand written sentences, plus a selection system that always fires the most specific thing that applies.

**The prime directive: every claim the engine makes must be true, and provably so from a count query.** A single fabricated area name or an off by one number permanently destroys the credibility of everything else the app says. There is no recovering from it, because the user has no way to verify anything else afterwards.

**The second directive: the engine may say nothing.** Silence is a supported, expected, designed outcome. A system that speaks every day is a machine. A system that occasionally has nothing worth saying has judgment.

### 1.1 Absolute prohibitions

The engine must never:

1. Name an area that has zero events in the window under consideration
2. State a number that did not come from a `TrailQueries` function
3. Reference an archived or deleted entity
4. Interpolate a live entity name; it always uses the snapshot carried in the event
5. Produce different output on two devices given the same event log
6. Read the wall clock, a random number generator, DataStore, or any Android API
7. Emit a sentence whose family was not authored for the criteria that fired it
8. Use the words should, failed, behind, lazy, hurry, streak, or any construction that assigns blame
9. Repeat a phrasing variant the user has seen inside 90 days, unless the family bench is genuinely exhausted
10. Contain an em dash, an en dash, or an emoji

Items 1 through 5 are enforced by code and covered by tests. Items 6 through 10 are enforced by tests over the catalogue and by the authoring protocol in section 9.

---

## 2. Architecture

Five layers, each a pure function, each independently testable.

```
Event log
    |
    v
[1] FactExtractor      : (EventLog, Window, Clock) -> FactSet
    |
    v
[2] RuleCatalogue      : static data, no strings
    |
    v
[3] Selector           : (FactSet, FiringHistory, RuleCatalogue) -> Selection?
    |
    v
[4] Realiser           : (Selection, FactSet, PhrasingCatalogue) -> Candidate
    |
    v
[5] Validator          : (Candidate, FactSet) -> Validated | Vetoed
    |
    v
Rendered sentence, or nothing
```

### 2.1 Purity contract

```kotlin
package com.kamsiob.claritynow.domain.engine

// No Android imports in this package. Ever.
// No java.time.now(), no System.currentTimeMillis(), no Random, no UUID.randomUUID().

object ClarityEngine {
    fun observe(
        facts: FactSet,
        history: FiringHistory,
        purpose: Purpose
    ): EngineResult
}

sealed interface EngineResult {
    data class Spoke(val output: RenderedOutput) : EngineResult
    data class Silent(val reason: SilenceReason) : EngineResult
}
```

Every input arrives as data. The engine cannot observe the outside world. Given identical `FactSet`, `FiringHistory` and `Purpose`, it returns identical `EngineResult` on any device, in any process, at any time. This is what makes the engine safe to run on a phone and a Linux desktop against a merged log.

**`SilenceReason`** is one of `NO_RULE_QUALIFIED`, `ALL_QUALIFIED_RULES_FILTERED`, `INSUFFICIENT_DATA`, `ALL_CANDIDATES_VETOED`. It is recorded in the simulator output and in debug logs, never shown to the user.

---

## 3. Layer 1: Fact extraction

`FactExtractor` is the only layer that touches data. It runs once per engine invocation and produces an immutable, fully populated `FactSet`. No lazy evaluation, no queries executed later, because a fact computed at validation time could differ from the fact that fired the rule.

```kotlin
data class FactSet(
    val window: WindowFacts,
    val areas: Map<AreaId, AreaFacts>,
    val rollup: RollupFacts,
    val items: ItemFacts,
    val history: HistoryFacts,
    val pulse: PulseFacts
)
```

Every field is non nullable or explicitly optional. There is no map lookup that can return null at realisation time.

### 3.1 WindowFacts

| fact | type | definition |
|---|---|---|
| `startInstant` | Long | inclusive start of the window |
| `endInstant` | Long | exclusive end |
| `dayCount` | Int | calendar days spanned |
| `totalEvents` | Int | events of any type in window |
| `completions` | Int | `ITEM_COMPLETED` count |
| `additions` | Int | `ITEM_ADDED` count |
| `promotions` | Int | `ITEM_PROMOTED` count |
| `swaps` | Int | promotions carrying a `demotedItemId` |
| `deletions` | Int | `ITEM_DELETED` count |
| `focusStarted` | Int | |
| `focusCompleted` | Int | |
| `focusAbandoned` | Int | |
| `focusSecondsTotal` | Long | sum of actual seconds across completed and abandoned |
| `focusMinutesTotal` | Int | derived, rounded down |
| `activeDays` | Int | distinct calendar days with at least one event |
| `busiestDayKey` | String? | null when `totalEvents` is 0 |
| `busiestDayCount` | Int | |
| `eventsByPartOfDay` | Map<PartOfDay, Int> | MORNING 05 to 12, AFTERNOON 12 to 17, EVENING 17 to 22, NIGHT 22 to 05 |
| `netFlow` | Int | `completions - additions` |

### 3.2 AreaFacts, one per area with any lifetime history

Areas that are archived or tombstoned are **excluded from this map entirely**. They cannot reach a rule, so they cannot reach a sentence. This is prohibition 3 enforced structurally rather than by checking.

| fact | type | definition |
|---|---|---|
| `areaId` | AreaId | |
| `nameSnapshot` | String | the name as of the newest event referencing it in this window, or its current name if untouched |
| `colorHex` | String | for surfaces that tint by area |
| `eventsInWindow` | Int | |
| `completionsInWindow` | Int | |
| `additionsInWindow` | Int | |
| `shareOfEvents` | Double | `eventsInWindow / window.totalEvents`, 0.0 when total is 0 |
| `hasActiveItem` | Boolean | |
| `activeItemId` | ItemId? | |
| `activeItemTitleSnapshot` | String? | |
| `activeItemAgeDays` | Int? | days since the promotion that made it active |
| `queueLength` | Int | at window end |
| `queueLengthAtWindowStart` | Int | |
| `queueDelta` | Int | end minus start |
| `daysSinceLastEvent` | Int | from window end. `Int.MAX_VALUE` when never |
| `lifetimeEvents` | Int | |
| `lifetimeCompletions` | Int | |
| `ageDays` | Int | since `AREA_CREATED` |
| `isNew` | Boolean | `ageDays < 14` |
| `focusSecondsInWindow` | Long | |
| `focusSessionsInWindow` | Int | |

**`shareOfEvents` is the single most misused fact.** Any rule using it must also require a minimum `eventsInWindow`, otherwise one event in a one event week reads as 100 percent concentration. Every share based rule in section 6 carries such a floor and no new rule may omit one.

### 3.3 RollupFacts

| fact | type | definition |
|---|---|---|
| `areasWithEvents` | Int | areas with `eventsInWindow > 0` |
| `areasTotal` | Int | non archived areas |
| `areasIdle` | Int | non archived areas with no active item |
| `dominantAreaId` | AreaId? | highest `eventsInWindow`, null on a tie or when all are zero |
| `dominantShare` | Double | |
| `neglectedAreaIds` | List<AreaId> | `lifetimeEvents >= 5` and `daysSinceLastEvent >= 7` and not `isNew` |
| `dormantReturnedAreaIds` | List<AreaId> | had a gap of 5 or more days that ended inside this window |
| `queueDrainedAreaIds` | List<AreaId> | `queueLengthAtWindowStart >= 3` and `queueLength == 0` |
| `queueGrowingAreaIds` | List<AreaId> | `queueDelta >= 2` |
| `freshStartAreaIds` | List<AreaId> | created in window, or received a first ever item in window |

Ties on `dominantAreaId` resolve to null rather than to an arbitrary winner. A rule that needs a dominant area simply does not fire on a tie, which is correct: there was no dominant area.

### 3.4 ItemFacts

| fact | type | definition |
|---|---|---|
| `activeByArea` | Map<AreaId, ActiveItem> | id, title snapshot, age in days, area name snapshot |
| `longestActiveItemId` | ItemId? | |
| `longestActiveDays` | Int | 0 when none |
| `completedInWindow` | List<CompletedItem> | id, title snapshot, area id, area name snapshot, days it was active |
| `medianDaysToComplete` | Int? | across `completedInWindow`, null under 3 items |

### 3.5 HistoryFacts

These are what make callbacks and comparisons possible. All derived from week snapshots and the full log, never from DataStore.

| fact | type | definition |
|---|---|---|
| `daysSinceInstall` | Int | |
| `weeksOfData` | Int | completed week snapshots |
| `isFirstWeekEver` | Boolean | |
| `lifetimeCompletions` | Int | |
| `lastWeekCompletions` | Int? | |
| `weekCompletionsSeries` | List<Int> | oldest first, up to 12 entries |
| `weekQueueSizeSeries` | List<Int> | |
| `weekTotalEventsSeries` | List<Int> | |
| `weekOverWeekDelta` | Int? | |
| `completionsTrend` | Trend | RISING, FALLING, FLAT, INSUFFICIENT. RISING requires 3 consecutive increases |
| `queueSizeTrend` | Trend | |
| `activityTrend` | Trend | |
| `dominantAreaLastThreeWeeks` | List<AreaId?> | oldest first, nulls allowed |
| `personalBestWeekCompletions` | Int | |
| `personalBestWeekKey` | String? | |
| `weeksSincePersonalBest` | Int? | |
| `mostRecentBetterWeekKey` | String? | the most recent week that beat the current one. Powers `your most active week since X` |
| `longestEverActiveDays` | Int | the longest any item has ever remained active |
| `longestEverActiveItemId` | ItemId? | |
| `personalBestFocusMinutesWeek` | Int | |
| `firstEverFlags` | Set<FirstEver> | FIRST_COMPLETION, FIRST_FOCUS_SESSION, FIRST_SWAP, FIRST_AREA_ARCHIVED, FIRST_QUEUE_DRAIN, FIRST_WEEK_WITH_ALL_AREAS_ACTIVE. Each is present only in the window where it first occurred |

**`mostRecentBetterWeekKey` requires care.** It is the newest week whose completions strictly exceed the current window's. If no such week exists, the sentence `your most active week since X` must not fire, and instead a personal best family applies. Getting this backwards produces a claim that is subtly false, which is worse than one that is obviously false.

**No streak facts exist.** Deliberately. There is no `currentStreak`, no `longestStreak`, no `daysInARow`. Their absence is what makes it impossible for streak language to appear by accident. Do not add them.

### 3.6 PulseFacts

| fact | type | definition |
|---|---|---|
| `answeredLifetime` | Int | |
| `answeredInWindow` | Int | |
| `positiveInWindow` | Int | |
| `flaggedInWindow` | Int | |
| `lastGeneratedFamily` | FamilyKey? | yesterday's, for the no repeat rule |
| `lastGeneratedDateKey` | String? | |
| `recentAnswers` | List<AnsweredPulse> | newest first, up to 30. Each carries dateKey, family, subjectId, responseKey, responseLabel, isPositive |
| `answersByFamily` | Map<FamilyKey, List<AnsweredPulse>> | |

`responseLabel` is stored verbatim in the `PULSE_ANSWERED` event so a callback can quote what the user actually saw, not a label that has since been reworded in a later app version. This is why the event carries the rendered label and not just the key.

---

## 4. Layer 2: The rule catalogue

A rule declares the conditions under which something is worth saying. **Rules contain no strings.** This is what lets the catalogue reach several hundred entries without the code growing, and it is what makes rules independently testable from copy.

```kotlin
data class ClarityRule(
    val key: RuleKey,                 // stable, never reused, e.g. "pulse.persistence.long"
    val purpose: Set<Purpose>,        // PULSE, REPORT_OBSERVATION, REPORT_PATTERN,
                                      // REPORT_HEADLINE, REPORT_ONE_THING,
                                      // MOMENTUM_HEADLINE, AREAS_BANNER
    val family: FamilyKey,            // which phrasing family realises it
    val subject: SubjectSelector,     // NONE, AREA, ITEM, or a specific extractor
    val criteria: List<Criterion>,    // all must pass
    val priority: Int,                // 0 to 100, tie break only, higher wins
    val horizonDays: Int,             // max age of the oldest fact it references
    val minimumEscalationFacts: Set<FactRef> = emptySet(),
    val requiresCallback: CallbackRequirement? = null
)

data class Criterion(
    val id: String,                   // for diagnostics and tests
    val describe: String,             // plain English, for simulator output
    val test: (FactSet, Subject?) -> Boolean
)
```

### 4.1 Specificity

`specificity = criteria.size`. It is not authored. A rule requiring four conditions is more specific than one requiring two, so it wins, because it describes a narrower and therefore more surprising situation. This is the whole mechanism behind the illusion, and it is why rules must never be padded with trivially true criteria to game their score. A test asserts that no criterion in the catalogue passes on more than 90 percent of a large simulated fact corpus.

### 4.2 Subject

Many rules concern a specific area or item. `SubjectSelector` extracts zero or more candidate subjects from the `FactSet`, and the rule is evaluated once per candidate. A rule can therefore qualify for `Work` and not for `Health` in the same window.

The winning `(rule, subject)` pair is what proceeds. Subject identity matters for escalation, which is tracked per `(family, subjectId)` and not per family alone. Nine days on one item and three days on another are two independent ladders.

### 4.3 Horizon

`horizonDays` is the maximum age of the oldest fact a rule references. A callback to something the user said last week is attentive. A callback to fourteen months ago in the wrong context is uncanny. Rules referencing only current window facts use the window length. Rules using `HistoryFacts` declare their own, typically 90 or 180 days. `mostRecentBetterWeekKey` rules are the one place a longer horizon is acceptable, because the sentence explicitly names how far back it is reaching.

### 4.4 Callback requirement

```kotlin
data class CallbackRequirement(
    val family: FamilyKey,        // which prior pulse family
    val withinDays: Int,
    val responseKey: String?,     // null means any answer
    val subjectMustMatch: Boolean
)
```

The selector resolves this against `PulseFacts.recentAnswers` before the rule can qualify, and the resolved `AnsweredPulse` is attached to the `Selection` so the realiser can fill a slot with the exact label the user tapped. A rule with an unresolvable callback does not fire. It never degrades into a version of the sentence without the callback, because the sentence was authored around it.

---

## 5. Layer 3: Selection

Deterministic, in this exact order. Any deviation produces device divergence.

1. **Qualify.** For each rule whose `purpose` matches, for each subject its `SubjectSelector` yields, evaluate every criterion. All must pass. Collect `(rule, subject)` pairs.
2. **Resolve callbacks.** Drop pairs whose `CallbackRequirement` cannot be satisfied.
3. **Horizon filter.** Drop pairs referencing a fact older than `horizonDays`.
4. **Repeat filter (Pulse only).** Drop pairs whose `family` equals `PulseFacts.lastGeneratedFamily`.
5. **Cooldown filter.** Drop pairs whose `(family, subjectId)` fired within the family's `cooldownDays` (section 7.3).
6. **Rank.** Sort by `specificity` descending, then `priority` descending, then `rule.key` ascending. The final ascending key sort is what removes the last trace of ordering ambiguity and it must be present even though it will rarely matter.
7. **Take the head.** If the list is empty, return `Silent`.

For `REPORT_OBSERVATION`, which needs two to four results rather than one, take the head, then repeat from step 6 excluding any rule sharing a family with an already selected one, until four are chosen or the list is empty. Never pad to reach a minimum. One qualifying observation means one observation.

### 5.1 Deliberate silence

Beyond natural non qualification, Pulse applies one additional rule. If the highest specificity available is 1, meaning nothing more interesting than a single bare condition applies, the engine returns `Silent` with reason `NO_RULE_QUALIFIED` on days where `stableHash(dateKey) % 3 == 0`.

This is intentional and it is the only place the engine chooses not to speak when it could. It exists because a Pulse that appears every single day becomes wallpaper, and one that occasionally does not appear reads as discretion. It is deterministic, so both devices agree, and it never suppresses a specificity 2 or higher observation.

---

## 6. The families

A family is a phrasing group tied to one situation. Rules point at families; families own the sentences.

### 6.1 Pulse families

Each entry gives the trigger shape, the subject, the escalation ladder, and the response pair. Response options must feel equally valid read out of context. No option is the good answer.

| family | trigger shape | subject | escalation keyed to | responses |
|---|---|---|---|---|
| `concentration` | one area at 70 percent or more of window events, minimum 4 events | area | share and event count | On purpose / It just happened |
| `spread` | 3 or more areas with events, none above 50 percent, minimum 5 events | none | area count | Felt manageable / Felt stretched |
| `throughput` | completions exceed additions, minimum 2 completions | none | net flow magnitude | Clearing the deck / Running low |
| `accumulation` | additions exceed completions by 2 or more | none | gap magnitude | Building up / Avoiding |
| `persistence` | an active item at 3 or more days | item | age in days | Deep work / Stuck |
| `switching` | one or more swaps in window | area | swap count | Reprioritising / Restless |
| `burst` | 3 or more completions in one area in one day | area | completion count | Momentum / Clearing out |
| `quietDay` | fewer than 2 events in window | none | how quiet | Recharging / Overwhelmed / Busy elsewhere |
| `rebalance` | activity returned to an area dormant 5 or more days | area | dormancy length | Planned / Just happened |
| `queueDrain` | an area went from 3 or more queued to 0 | area | starting queue size | Finished strong / Running empty |
| `freshStart` | new area created, or first item added to an empty area | area | none | Expanding / Exploring |

Compound families (`burst`, `quietDay`, `rebalance`, `queueDrain`, `freshStart`) naturally carry more criteria and therefore win on specificity without needing a priority thumb on the scale.

**Response polarity.** The first response of each pair is marked positive. For `quietDay`, `Recharging` and `Busy elsewhere` are positive, `Overwhelmed` is flagged. This boolean is what the Report aggregates and it is the only interpretation the app ever makes of an answer.

**Adding families.** The selector does not care how many exist. A new family requires: a `FamilyKey`, at least one rule, an escalation ladder, a response pair passing the equal validity test, and a phrasing bench sized per section 9.1. Nothing else changes.

### 6.2 Report families

**Headline**, one per report, under 8 words. Selection order is by specificity as usual, with these authored priorities so the intended order emerges: quiet week, net outflow, net inflow, balanced across 3 or more areas, single area at 80 percent or more, 5 or more focus sessions, declining activity, first report ever, dated fallback. Never a motivational platitude. The dated fallback exists so the headline is never absent.

**Observation families** for `YOUR WEEK, HONESTLY`: `singleFocus`, `intakeVsOutput`, `focusInvestment`, `neglectedArea`, `completionSplit`, `quietWeek`, `queuePressure`, `areaRevival`, `steadyPace`, `personalBest`, `mostActiveSince`.

`completionSplit` requires `PulseFacts.answeredInWindow >= 3`. Below that the report is trail data only and this family cannot fire.

**Pattern families**, at most one, requiring `weeksOfData >= 3`: `shiftingFocus`, `growingQueues`, `improvingThroughput`, `decliningActivity`, `areaGoneQuiet`. Under 3 weeks of data the section shows a single faint line about patterns appearing later. With 3 or more weeks and no trend, the section is omitted entirely rather than filled.

**One thing families**, exactly one, in serif italic, derived from the highest priority triggered observation: `finishBeforeAdding`, `reconsiderArea`, `checkOtherAreas`, `smallestAction`, `protectFocusTime`, `reviewActiveItems`.

### 6.3 Momentum and banner families

`MOMENTUM_HEADLINE` produces one serif sentence under twelve words containing only real area names. Families: `steadyStretch`, `quietStretch`, `comeback`, `balancedWeek`, `singleAreaWeek`, `strongPace`, `firstDays`, `cleanSlate`.

`AREAS_BANNER` produces the weekly status sentence plus its caption line. Families: `weekStarting`, `weekBuilding`, `weekStrong`, `weekQuiet`, `weekMixed`. The banner recomputes at most once per hour of app use to avoid flicker, and that throttle lives in the ViewModel, not the engine.

Momentum observes and never interprets. Its families must never contain the words because, suggests, means, or any causal construction. That vocabulary belongs to the Report and a test enforces the boundary across the Momentum benches.

---

## 7. Layer 4: Realisation

### 7.1 Structure

```kotlin
data class PhrasingFamily(
    val key: FamilyKey,
    val cooldownDays: Int,
    val stages: List<EscalationStage>,
    val responses: List<ResponseOption>   // Pulse families only
)

data class EscalationStage(
    val index: Int,                       // 1 upward
    val threshold: ClosedRange<Int>,      // over the family's escalation fact
    val variants: List<Variant>
)

data class Variant(
    val key: VariantKey,                  // stable, unique within the family
    val register: Register,               // PLAIN, OBSERVATIONAL, REFLECTIVE
    val statement: Template,
    val question: Template?,              // Pulse only
    val requiredSlots: Set<SlotKey>
)
```

`Template` is an authored string containing typed slot markers. It is never assembled at runtime from fragments belonging to another family.

### 7.2 Slots

```kotlin
sealed interface Slot {
    data class Text(val key: SlotKey, val value: String) : Slot
    data class Count(
        val key: SlotKey,
        val value: Int,
        val singular: String,
        val plural: String
    ) : Slot
    data class Days(val key: SlotKey, val value: Int) : Slot
    data class Percent(val key: SlotKey, val value: Int) : Slot
    data class DateRef(val key: SlotKey, val weekKey: String, val display: String) : Slot
}
```

**Number rendering rules, applied centrally and never by a template author:**

- Percentages render as `78 percent`, never `78%`
- Counts of one render the singular noun. `1 item`, not `1 items`. Every `Count` slot carries both forms and there is no default
- Counts from two to nine render as words in Pulse and Momentum, as digits in the Report. `three days` in a Pulse observation, `3 completions` in a report line. This is a deliberate register difference and it is enforced by the renderer, not left to authors
- Counts of ten or more always render as digits
- Zero never reaches a template, because a rule that could produce zero must carry a criterion preventing it
- Day counts use `Days`, which renders `yesterday`, `two days`, `nine days`, `three weeks` at the appropriate magnitudes rather than always saying a raw number
- Date references render as a month name, `since March`, never a numeric date

**Slot completeness is checked before rendering.** If any `requiredSlot` is absent from the resolved slot map, the candidate is dropped and the next ranked selection is realised instead. A template must never render with an unfilled marker visible. A test walks the entire catalogue and asserts every variant's `requiredSlots` are producible from the facts its rules guarantee.

### 7.3 Escalation

Escalation is keyed to **magnitude**, not to firing count. Nine days is stage 2 whether the family has fired once or fifty times.

Example ladder for `persistence`, over `activeItemAgeDays`:

- stage 1, range 3 to 5: a note. `Rewrite the proposal intro has been active in Work for three days.`
- stage 2, range 6 to 13: pointed. `Still Rewrite the proposal intro. Nine days now.`
- stage 3, range 14 to 29: comparative. `Rewrite the proposal intro has been active for three weeks. Most things you finish take four days.`
- stage 4, range 30 and above: historical. `This has been active longer than anything you have ever kept active.`

Stage 4 there references `HistoryFacts.longestEverActiveDays` and its rule carries a criterion asserting the current item genuinely holds that record. Without that criterion the sentence would be a lie the moment a longer running item existed.

**Monotonicity.** For a given `(family, subjectId)`, the engine never shows a lower stage than it previously showed while the condition has remained continuously true. `FiringHistory` carries `lastStageBySubject`, derived from prior `PULSE_GENERATED` events. If the computed stage is lower than the recorded one and the condition never lapsed, use the recorded stage. This prevents the engine from saying `nine days now` on Tuesday and then `has been active for three days` on Wednesday because a promotion reset an age calculation.

**Reset.** When the condition genuinely lapses, meaning the item was completed or swapped away, the ladder resets. A new active item starts at stage 1 regardless of what the previous one reached.

**Cooldown.** Each family declares `cooldownDays`. A `(family, subjectId)` pair cannot fire again within that window. Hot families use 3 to 5 days, compound families 10 to 21. Cooldown is separate from the no repeat rule in selection step 4, which covers only yesterday.

### 7.4 Register

Each variant carries a register. Pulse selects register by time of day, matching the background gradient described in `design.md` 3.3:

- dawn and midday windows prefer PLAIN and OBSERVATIONAL
- evening prefers REFLECTIVE

If the family has no variant in the preferred register at the selected stage, fall back in the order REFLECTIVE, OBSERVATIONAL, PLAIN. The user should feel this rather than notice it.

### 7.5 Variant choice, deterministic and apparently random

Within the selected `(family, stage, register)` bench:

1. Filter out variants used within 90 days, from `FiringHistory.variantsUsed`
2. If the filtered list is empty, use the full bench minus the single least recently used variant, and record a bench exhaustion diagnostic
3. Sort the remaining variants by `stableHash(dateKey + variantKey)` ascending
4. Take the head

`stableHash` is a specified, implemented, tested hash (FNV-1a 64 bit) and never `String.hashCode()`, which is not guaranteed stable across platforms. Two devices computing this on the same `dateKey` reach the same variant with no shared state and no synchronisation, and the result reads as random to a user because they never see the ordering.

**`FiringHistory` is derived entirely from `PULSE_GENERATED` and `REPORT_GENERATED` events.** Never from DataStore. This is a hard rule and the reason for it is sync: a device that has just merged a log must compute the same next variant as the device that produced it. DataStore does not merge.

### 7.6 Multiplying the surface without concatenating

Hot families use frames with two or three interchangeable clause slots:

```
frame: "{opener} {subject} {closer}"
opener bench (family-owned): 6 authored options
closer bench (family-owned): 6 authored options
```

Thirteen authored fragments yield 36 surfaces. Across the hot families this puts the effective surface into the tens of thousands while the authored, human read corpus stays in the low thousands.

**The rule that stops this becoming slop: fragments are never generic and never shared across families or registers.** A clause bench belongs to exactly one family and one register. There is no global opener pool. A test asserts no fragment string appears in two families. Global fragment pools are precisely how these systems start sounding like assembled Mad Libs, and once a user detects the seams the illusion is gone permanently.

Frames are also authored as wholes and reviewed as wholes: an author writes all 36 resulting sentences out during review, not just the thirteen parts.

---

## 8. Layer 5: Validation

The validator receives every candidate with the facts that produced it and vetoes anything that fails. A vetoed candidate causes the next ranked selection to be realised. If everything is vetoed, the engine returns `Silent` with reason `ALL_CANDIDATES_VETOED`.

```kotlin
data class Candidate(
    val ruleKey: RuleKey,
    val familyKey: FamilyKey,
    val variantKey: VariantKey,
    val stage: Int,
    val rendered: String,
    val renderedQuestion: String?,
    val slots: Map<SlotKey, Slot>,
    val sourceFacts: Map<SlotKey, FactRef>,
    val namedAreaIds: Set<AreaId>,
    val namedItemIds: Set<ItemId>
)
```

The checks, in order, all mandatory:

1. **Area existence.** Every id in `namedAreaIds` has `eventsInWindow > 0` in the `FactSet`. Not merely exists. Not merely is not archived. Has real events in the window being described.
2. **Item existence.** Every id in `namedItemIds` resolves to an item present in the `FactSet` and not tombstoned.
3. **Number provenance.** Every `Count`, `Percent` and `Days` slot carries a `FactRef` naming the fact it came from, and re-reading that fact from the `FactSet` yields the same value. A number that cannot be traced is a veto.
4. **No zeros.** No numeric slot has value 0.
5. **Snapshot usage.** Every `Text` slot holding an entity name came from a snapshot field, not from a live lookup. Enforced by making the live entities unavailable to the realiser at all: it receives only the `FactSet`, whose name fields are snapshots by construction.
6. **Callback fidelity.** If a callback slot is present, the quoted response label matches the `responseLabel` recorded in the referenced `PULSE_ANSWERED` event exactly.
7. **Unfilled markers.** The rendered string contains no residual slot syntax.
8. **Forbidden vocabulary.** The rendered string contains none of the banned words in section 1.1 item 8, no em dash, no en dash, no emoji.
9. **Length.** Report headlines under 8 words. Momentum headlines under 12 words. Pulse observations under 30 words. One thing lines under 20 words.

Checks 1 through 4 are the integrity core. **The veto path for each must be reachable in a unit test**, meaning there is a test that deliberately constructs a candidate violating it and asserts the veto fires. A validator whose failure branch is never executed in testing is a validator nobody has verified.

---

## 9. The corpus

### 9.1 Sizing

Size by expected firing frequency. Even sizing across families is the mistake that produces repeats exactly where they hurt most, because a handful of families fire fifty times a year and most fire four.

| tier | firings per year | variants per stage bench |
|---|---|---|
| hot, roughly 15 families | 40 or more | 60 to 100 |
| warm, roughly 30 families | 5 to 20 | 15 to 30 |
| long tail, rare and first time | under 5 | 4 to 8 |
| Report sections | 52 total across types | deep on common, thin on rare |

Target 2,500 to 4,000 authored sentences, roughly 400KB, against a 15MB app budget. Storage is not a consideration. Quality is the only constraint.

Hot families include `persistence`, `concentration`, `accumulation`, `throughput`, `quietDay`, `spread`, plus the Report's `intakeVsOutput`, `singleFocus`, `focusInvestment`, and Momentum's `steadyStretch` and `balancedWeek`.

### 9.2 Authoring protocol

This is the part that cannot be fully automated, and attempting to is how a corpus becomes four thousand sentences nobody wants to read.

1. **Research first.** Before writing any batch, research current AI slop tells in language. Do this before writing, not as a cleanup pass afterwards.
2. **Batch size forty.** One family, one stage, one register per batch.
3. **Anchor every batch** with ten already approved lines from that family so the voice does not drift between sessions.
4. **Judge against simulator output**, not in isolation. A line that reads well in a list can land badly in context.
5. **Present for approval.** Expect a quarter to a third of every batch to be rejected. A rejection rate near zero means nobody is reading hard enough, and that is the failure mode that produces volume without quality.
6. **Never generate the whole corpus in one pass.** Voice drifts noticeably by sentence 200.

Three thousand sentences is roughly seventy five batches. Spread across the build that is manageable. Attempted in a week it kills the voice.

### 9.3 Voice rules

Calm, observational, second person where natural, sentence case, active voice. Numbers always real.

**Never:** should, failed, behind, streak, hurry, lazy, don't forget, you haven't, make sure, try to, remember to. No exclamation marks. No rhetorical questions except the Pulse question itself. No advice outside the `ONE THING` section. No em dashes, en dashes or emojis.

**The equal validity test.** Read each Pulse response pair aloud with no context. If one sounds like the answer a good person gives, rewrite both. `Deep work / Stuck` passes. `Focused / Distracted` fails, because nobody wants to be distracted.

**The mirror test.** Read every sentence as though a friend said it about your week. If it would make you defensive, it is wrong. The engine observes; it does not assess.

---

## 10. The simulator

Build this in phase 5, before a single corpus sentence is written. Without it, corpus quality is discovered in month six of real use.

In `devtools`, debug builds only.

**Inputs.** Synthetic user histories of configurable shape: heavy single area, balanced across four, sporadic, abandoning, high focus, low focus, brand new, long dormant with a revival, queue hoarder, fast completer.

**Process.** Generate a full simulated year of events, run the engine day by day for Pulse and week by week for the Report, plus Momentum on each simulated app open.

**Output.** A plain text file per persona, containing for every invocation:

```
2026-03-14  [pulse]  persistence / stage 2 / reflective / v.persist.s2.r.017
  fired because: active item age 9 days (>= 6), area has 4+ events, no swap in window
  facts used: activeItemAgeDays=9, areaName=Work, itemTitle=Rewrite the proposal intro
  > Still Rewrite the proposal intro. Nine days now.
  > Deep work, or stuck?

2026-03-15  [pulse]  SILENT (NO_RULE_QUALIFIED)
```

The rule, stage, register and variant key on every line are what make a bad output traceable to the exact bench that needs work.

**Automated checks over the dump:**

- No variant key appears twice inside 90 simulated days
- No sentence contains a banned word, an em dash, an en dash or an emoji
- No sentence names an area that had no events in its window
- Silence occurs on between 8 and 25 percent of days across the persona set. Below 8 percent the app is chattering; above 25 percent it feels absent
- No family accounts for more than 20 percent of a year's Pulses
- Every stage of every hot family fires at least once across the persona set, proving no bench is unreachable

---

## 11. Failure modes, and the guard for each

These are the specific ways this kind of system goes wrong. Each has a named guard that must exist in the code.

| failure | what it looks like | guard |
|---|---|---|
| Phantom area | naming an area with no activity | validator check 1, structural exclusion of archived areas from `FactSet` |
| Invented number | a count that no query produced | validator check 3, `FactRef` provenance on every numeric slot |
| Stale name | an old area name after a rename | snapshots only, live entities never reach the realiser |
| Resurrection | a deleted item named in a report | tombstones plus validator check 2 |
| Off by one on shares | one event reading as 100 percent concentration | mandatory minimum event floor on every share based rule |
| False superlative | `most active week since March` when March is wrong | `mostRecentBetterWeekKey` computed as strictly greater, plus a criterion asserting the record is genuinely held |
| Broken plural | `1 items` | `Count` slot carries both forms, no default, catalogue test |
| Visible slot syntax | a raw marker in the UI | slot completeness check before render, validator check 7 |
| Escalation whiplash | day nine then day three | monotonicity via `lastStageBySubject` |
| Device divergence | phone and desktop disagree | pure engine, `FiringHistory` from the log not DataStore, FNV-1a hash, deterministic sort with a final key tiebreak |
| Repetition detected | the user recognises a line | 90 day exclusion, frequency based bench sizing, simulator check |
| Mad Libs seams | assembled sentences read wrong | family owned and register owned clause benches, no global pools, cross family fragment uniqueness test |
| Blame language | a sentence that stings | banned vocabulary test, equal validity test, mirror test |
| Chattering | Pulse every single day forever | deliberate silence rule, simulator silence rate check |
| Reset residue | a fresh install says `since March` | reset virginity test in `MASTER_BUILD_PROMPT.md` section 7.5 |
| Timezone drift | two pulses on one day, or none | `dateKey` computed from a single injected `ClarityClock` with an explicit zone, DST transition tests at both boundaries |
| Callback fabrication | quoting an answer the user never gave | callback resolution before qualification, validator check 6 |
| Zero sentence | `you completed 0 things` | validator check 4, plus criteria that make zero unreachable |

---

## 12. Worked examples

### 12.1 A Pulse, end to end

**Facts.** Window is yesterday. `totalEvents = 7`. `Work` has `eventsInWindow = 5`, `shareOfEvents = 0.71`, `activeItemAgeDays = 9`, `activeItemTitleSnapshot = "Rewrite the proposal intro"`. `Health` has 2 events. `PulseFacts.lastGeneratedFamily = concentration`.

**Qualify.** `pulse.concentration.strong` qualifies on Work at specificity 3. `pulse.persistence.long` qualifies on the Work active item at specificity 3. `pulse.spread.wide` fails; only two areas had events.

**Filter.** Step 4 drops `concentration`, because it fired yesterday. `persistence` survives.

**Rank.** One pair remains.

**Realise.** Escalation fact is `activeItemAgeDays = 9`, which lands in stage 2, range 6 to 13. Time is 19:40, so register is REFLECTIVE. The 90 day filter leaves 63 of 78 variants at that bench. FNV-1a over `2026-03-14` plus each variant key sorts them; the head is `v.persist.s2.r.017`.

Template: `Still {itemTitle}. {ageDays} now.`
Slots: `itemTitle = Text("Rewrite the proposal intro")`, `ageDays = Days(9)` rendering as `Nine days`.

**Render.** `Still Rewrite the proposal intro. Nine days now.` Question: `Deep work, or stuck?`

**Validate.** Work has 5 events, passes check 1. The item resolves, passes 2. `ageDays` re-reads as 9 from the `FactSet`, passes 3. No zeros, no banned words, no residual markers, 8 words. Passes.

**Emit.** `PULSE_GENERATED` is written carrying the family, stage, variant key, rendered strings and the fact snapshot, so the sentence can be re-displayed identically forever and so `FiringHistory` can be rebuilt by replay.

### 12.2 A veto

Same window. `pulse.rebalance.returned` qualifies on `Personal`, because a dormancy gap ended. But `Personal`'s only event in the window was an `AREA_RENAMED`, which the rule's criteria counted through `eventsInWindow` while the sentence would claim activity returned.

The realiser produces `Personal is moving again after eleven quiet days.` The validator runs check 1: `Personal.eventsInWindow` is 1, so check 1 passes. Check 3 then re-reads the fact backing the sentence's implicit claim of resumed work, `completionsInWindow`, which is 0. Check 4 fires on the zero. Vetoed.

The engine falls through to the next ranked selection. This example is why `rebalance` rules must count qualifying event types explicitly rather than relying on `eventsInWindow`, and the correct fix is to tighten the rule rather than to loosen the validator.

### 12.3 A callback

`report.completionSplit.mixed` requires `answeredInWindow >= 3` and a `CallbackRequirement` on family `throughput` within 7 days with any response and no subject match.

`PulseFacts.recentAnswers` contains a `throughput` answer from three days ago with `responseLabel = "Clearing the deck"`. The requirement resolves and attaches.

Template: `You called that {priorLabel}. {n} of the {total} you finished were in {areaName}.`

Rendered: `You called that clearing the deck. Three of the four you finished were in Work.`

Check 6 compares `clearing the deck` against the stored `responseLabel`. It matches case insensitively after the sentence position lowercasing that the renderer applies, which is a rule the renderer owns and the validator knows about. Passes.

---

## 13. Tests that must exist

Beyond the failure mode guards above:

- **Purity.** A test that the `domain.engine` package contains no Android imports, no `System.currentTimeMillis`, no `Random`, no `hashCode()` on a String.
- **Catalogue integrity.** Every rule points at an existing family. Every family has at least one rule. Every variant's `requiredSlots` are producible from the facts guaranteed by every rule that can reach it. No duplicate rule keys or variant keys. No fragment string in two families.
- **Criterion discrimination.** No criterion passes on more than 90 percent of a large simulated fact corpus, which catches padded rules gaming specificity.
- **Determinism.** The same `FactSet` and `FiringHistory` produce the same `EngineResult` across 10,000 generated inputs.
- **Cross device agreement.** Two `FiringHistory` objects rebuilt independently from the same merged log produce identical selections for the same `dateKey`.
- **Veto reachability.** One test per validator check that deliberately constructs a violating candidate and asserts the veto.
- **Escalation monotonicity.** A simulated continuously true condition never shows a decreasing stage.
- **Silence rate.** Across the persona set, silence falls between 8 and 25 percent of days.
- **Repetition.** No variant repeats inside 90 simulated days across every persona.
- **Boundary.** `dateKey` is correct across a DST spring forward and fall back in the device zone, and the 17:00 reflection period switch happens exactly once per day.
