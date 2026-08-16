# Clarity Now V2 Amendments

Complete replacement sections for `CLARITY_LOGIC_ENGINE.md`, `CORPUS_2_REPORT.md` and `MASTER_BUILD_PROMPT.md`, following the expert panel review.

**How to apply this file.** Each part below states exactly which section of which document it replaces. The replaced text is superseded entirely. Everything not mentioned here is unchanged and still authoritative.

`design.md` and the visual reference are already fully rebuilt as **v2** and do not need amending.

`CORPUS_1_PULSE.md` is **unchanged**. None of the twenty two recommendations touch Pulse language. The Pulse surface was not the problem and reissuing 750 identical lines would only create a chance of divergence between two files.

---

# PART ONE: THE GUIDANCE ENGINE

**Replaces `CLARITY_LOGIC_ENGINE.md` section 2 (Architecture) and adds a new layer 6.**

## 1.1 Why this exists

The panel's behavioural scientist made the case that the engine, for all its sophistication, only ever observes. The research is blunt about where that leads.

<cite index="69-1">A review of self-tracking found that insights gained through reflection on personal data are frequently not actionable towards behaviour change. Many systems tacitly assume that reflection will occur naturally once data is processed and visualised, while most theories of reflection highlight that reflection needs to be explicitly supported.</cite>

The strongest available intervention is not more observation. It is a change in the grammatical form of the closing line.

<cite index="72-1">Gollwitzer and Sheeran's 2006 meta-analysis of 94 independent studies found implementation intentions produced an effect size of d equals 0.65 on goal achievement.</cite> <cite index="72-1">In the original study, participants forming implementation intentions completed at 71 percent against 32 percent for those with goal intentions only.</cite>

The mechanism is specific. <cite index="74-1">A goal intention specifies an outcome. An implementation intention specifies the when, where and how of the behaviour that will produce it.</cite>

Our v1 closing line was a goal intention. *Before adding anything new, try finishing one thing already in a queue.* That is the weak arm of the experiment.

The strong form needs a cue, and this is the part that makes it fit our app specifically: **we already know the user's cues.** Their strongest day, their most productive hours, when their focus sessions actually happen. The engine is already computing all of it for other purposes.

## 1.2 The revised architecture

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
[6] GuidanceComposer   : (Validated[], FactSet, PlanHistory) -> Plan?     <-- NEW
    |
    v
Rendered report, with or without a plan
```

Layer 6 runs **only for the Report**, only after sections 1 to 5 have produced the report body, and only ever produces one output. Pulse and Momentum never reach it.

```kotlin
object GuidanceComposer {
    fun compose(
        appeared: List<Validated>,   // the observations that actually made it into the report
        facts: FactSet,
        plans: PlanHistory
    ): GuidanceResult
}

sealed interface GuidanceResult {
    data class Plan(val plan: ClarityPlan) : GuidanceResult
    data class Closing(val line: RenderedOutput) : GuidanceResult  // non-plan closing
    data object Nothing : GuidanceResult
}
```

`Nothing` is a first class outcome and must be common. See 1.7.

## 1.3 Cue facts

New fact category on `FactSet`, computed over a **12 week** window rather than the report window, because a cue needs to be a pattern rather than an accident.

```kotlin
data class CueFacts(
    val strongestWeekday: Weekday?,          // most events, 12 weeks, needs 1.5x the mean
    val strongestWeekdayConfidence: Double,
    val quietestWeekday: Weekday?,
    val productiveBand: PartOfDay?,          // where completions concentrate
    val productiveBandShare: Double,
    val focusTypicalWeekday: Weekday?,       // where focus sessions actually land
    val focusTypicalBand: PartOfDay?,
    val addingBand: PartOfDay?,              // when items get captured
    val weekdayOnly: Boolean,                // no weekend activity in 4+ weeks
    val hasStableRhythm: Boolean             // any cue above threshold at all
)
```

**Confidence thresholds, all mandatory.** A cue may only be used in a plan if:
- it is drawn from at least 6 weeks of data, and
- the pattern holds in at least 60 percent of those weeks, and
- the underlying count is at least 8 events

If no cue clears these thresholds, `hasStableRhythm` is false and layer 6 may not produce a plan. It falls through to a non-plan closing line or to nothing. **An invented cue is worse than no plan**, because it makes a claim about the user's life that the user knows to be false.

## 1.4 Plan construction

```kotlin
data class ClarityPlan(
    val id: String,
    val weekStartKey: String,
    val cueKey: CueKey,              // which cue bank line was used
    val actionKey: ActionKey,        // which action bank line was used
    val familyKey: FamilyKey,        // the observation that motivated it
    val subjectId: String?,          // area or item the action concerns
    val renderedLine: String,
    val acceptedAt: Long?,           // null unless the user tapped
    val resolutionFactRef: FactRef,  // the fact that will show whether it moved
    val resolvedAt: Long?,
    val resolvedValue: String?
)
```

The plan is written as a `PLAN_OFFERED` event, and on acceptance a `PLAN_ACCEPTED` event. Both append-only, both replayable, both merge like everything else. `PlanHistory` is derived from the log, never from DataStore, so two devices agree.

**The rendered line is always cue then action, in that order, in one sentence.**

> On Wednesday morning, before you open Work, spend ten minutes in Personal.

Not: *Consider giving Personal some attention this week.*

## 1.5 The composition rules

Layer 6 may only produce a plan when **all** of the following hold:

1. **`CueFacts.hasStableRhythm` is true** and the specific cue clears its thresholds.
2. **The motivating observation actually appeared in the report.** The plan may never advise on something the report did not mention. This carries over from v1 and is now enforced by passing only `appeared` into layer 6 rather than the full candidate set.
3. **There is a real friction pattern.** <cite index="76-1">When there are few barriers to goal achievement, favourable goal intentions can suffice and implementation intention formation might be superfluous.</cite> If the week was straightforwardly good, no plan is offered.
4. **No plan was offered in the previous two weeks that is still unresolved.** Stacking unfinished plans is how this becomes a nag.
5. **The action is completable inside one week and is a single concrete act.** Not "work on Personal." "Spend ten minutes in Personal on Wednesday morning."
6. **The report is not otherwise heavy.** If the report already carries a declining headline plus a neglected area observation, no plan is offered. A hard week does not also get homework.

## 1.6 Acceptance

The plan renders with one optional tap reading `I'll do that`.

- Accepting writes `PLAN_ACCEPTED` and nothing visible happens beyond the pill settling. No toast, no celebration, no bounce, no haptic heavier than an ordinary tap.
- Not accepting costs nothing, is never mentioned, is never counted, and is never referenced.
- There is no reminder, no notification, no badge, no card on the home screen. The plan exists in the report and nowhere else.

This is the concession that made the panel's psychologist accept the feature at all. Her framing was the deciding one: **reporting on your own commitment is not surveillance; reporting on the app's instruction is.** The tap is what transfers authorship.

## 1.7 Follow-through, and the rule that keeps it safe

**A plan is only ever followed up if it was accepted.** Unaccepted plans vanish without trace.

When followed up, the next report may state the **underlying fact**, never the compliance.

- Permitted: `Personal moved on Wednesday for the first time in three weeks.`
- Forbidden: `You did the thing you said you would.`
- Forbidden: `You planned to spend time in Personal and did not.`

**The negative-case rule, and this is the load-bearing safeguard.** A follow-up observation may only appear when it would have qualified as an ordinary observation on its own merits, independently of any plan. If Personal did not move, the report may say so **only if `neglectedArea` would have fired anyway.** It may never fire *because* a plan existed.

Implementation: layer 6 does not inject follow-up sentences. It sets a **priority boost** on the observation family whose `resolutionFactRef` matches the accepted plan, raising it in the ranking. If that family does not qualify, nothing appears. The user can never be told about a plan they did not keep, because the mechanism has no way to say it.

**The test.** Generate a persona who accepts every plan and completes none of them, run a simulated year, and assert that no sentence in any report references a plan, a commitment, an intention, or a failure to act. If a reader of that dump could tell the persona had accepted plans, the implementation has failed and must be removed rather than tuned.

## 1.8 Silence

The panel raised the floor. Across the persona set, layer 6 must return `Nothing` or a non-plan closing on **at least 15 percent** of reports, up from 12 in v1.

A report that always has advice is a report inventing problems. The `trustThePace` and `letItBe` families exist for exactly this and must not be eroded by the new machinery.

---

# PART TWO: THE GUIDANCE CORPUS

**Replaces `CORPUS_2_REPORT.md` section 4 (ONE THING THIS WEEK) entirely.**

Cue and action are separate benches, combined by layer 6. A cue combines only with an action from a compatible family. Sixteen cues against roughly a hundred actions gives well over a thousand plan surfaces, all of them grammatical and all of them true.

Rendered as: **`{cue}, {action}.`** Cue lines carry no terminal punctuation. Action lines carry no leading capital.

## 2.1 The cue bank

Each cue names its required `CueFacts` field. A cue that cannot be substantiated is not available.

### Temporal, weekday
```
cue.day.01   On {strongestWeekday}
cue.day.02   On {strongestWeekday}, your busiest day
cue.day.03   When {strongestWeekday} comes around
cue.day.04   Before {strongestWeekday} is over
cue.day.05   On the day you usually get most done
```
*Requires `strongestWeekday` at confidence.*

### Temporal, part of day
```
cue.band.01  On {strongestWeekday} morning
cue.band.02  In the morning, when you finish most things
cue.band.03  Before midday, where most of your completions land
cue.band.04  In your first hour
cue.band.05  Early on {strongestWeekday}
```
*Requires `productiveBand` and, for 01 and 05, `strongestWeekday`.*

### Behavioural, anchored to an existing habit
```
cue.hab.01   Before you open {areaName}
cue.hab.02   After your next focus session
cue.hab.03   Before you add anything new
cue.hab.04   The next time you finish something
cue.hab.05   Before you start on {areaName} again
cue.hab.06   When you next open the app
```
*`cue.hab.02` requires `focusTypicalWeekday`. The rest require only the named entity to exist.*

### Boundary
```
cue.bound.01 Before the week ends
cue.bound.02 Before {quietestWeekday}
cue.bound.03 At the start of next week
cue.bound.04 Before Friday
cue.bound.05 On your next quiet evening
```

## 2.2 The action bank

Grouped by the observation family that motivates them. An action may only be paired with a cue when its family appeared in the report.

### From `intakeVsOutput` or `queuePressure`

```
act.fin.01   finish one thing already waiting in {areaName}
act.fin.02   close the oldest item in {areaName}
act.fin.03   pick the smallest thing in a queue and finish it
act.fin.04   clear one item before adding another
act.fin.05   take {itemTitle} off the queue for good
act.fin.06   finish something before you write anything down
act.fin.07   move one queued item all the way through
act.fin.08   let one thing leave before the next arrives
act.fin.09   choose the easiest item in {areaName} and be done with it
act.fin.10   spend fifteen minutes closing whatever is nearest to finished
```

### From `neglectedArea`

```
act.neg.01   spend ten minutes in {areaName}
act.neg.02   open {areaName} and read what is in it
act.neg.03   do the smallest thing {areaName} is holding
act.neg.04   give {areaName} one item and one hour
act.neg.05   decide whether {areaName} stays or goes to the archive
act.neg.06   look at {areaName} before anything else
act.neg.07   put one new thing at the front of {areaName}
act.neg.08   find out whether {areaName} still matters to you
act.neg.09   move anything at all in {areaName}
act.neg.10   read {areaName}'s queue and delete what is dead
```

### From `singleFocus`

```
act.oth.01   open {otherArea} before {areaName}
act.oth.02   do one thing outside {areaName}
act.oth.03   give {otherArea} fifteen minutes
act.oth.04   check what the other areas have been holding
act.oth.05   start somewhere other than {areaName}
act.oth.06   let {areaName} wait and do something else first
act.oth.07   spend the first half hour anywhere but {areaName}
```

### From `persistentItem`

```
act.brk.01   write down what {itemTitle} is actually waiting on
act.brk.02   replace {itemTitle} with the first step of it
act.brk.03   spend twenty minutes on {itemTitle} and nothing else
act.brk.04   decide whether {itemTitle} is one thing or several
act.brk.05   add a note to {itemTitle} saying what is in the way
act.brk.06   put the smaller version of {itemTitle} at the front instead
act.brk.07   give {itemTitle} one uninterrupted hour
act.brk.08   name the obstacle, even in three words
```

### From `focusInvestment` or `focusHabitFading`

```
act.foc.01   book one focus session
act.foc.02   do what you did last {focusTypicalWeekday}
act.foc.03   protect twenty five minutes for {areaName}
act.foc.04   start a session before anything else
act.foc.05   put one session in before the week fills up
act.foc.06   sit down for the same length of time that worked before
```

### From `queueDrained` or an idle area

```
act.pick.01  give {areaName} something to hold
act.pick.02  put one thing at the front of {areaName}
act.pick.03  decide what {areaName} is about this month
act.pick.04  write down the next thing for {areaName}
act.pick.05  choose one item for {areaName} or archive it
```

### From `switchingBehaviour`

```
act.set.01   pick one item in {areaName} and leave it there
act.set.02   finish whatever is at the front of {areaName} before changing it again
act.set.03   decide what {areaName} is actually for this week
act.set.04   commit to one thing in {areaName} until it is done
```

### From `dayShape` or `timeOfDay`

```
act.rep.01   do what you did last {strongestWeekday}
act.rep.02   protect the same hours that worked
act.rep.03   start at the time you usually finish things
act.rep.04   put the hardest item in your best hour
```

## 2.3 Non-plan closings

Used when layer 6 cannot or should not produce a plan. These are complete lines, not cue plus action.

### `trustThePace`, when the week worked
```
cls.trust.01  Nothing here needs fixing. Carry on.
cls.trust.02  The week worked. Do that again.
cls.trust.03  Whatever the pattern is, it is holding. Leave it.
cls.trust.04  This is a good shape. Nothing to change.
cls.trust.05  Steady is the result, not the absence of one.
cls.trust.06  No adjustment needed this week.
cls.trust.07  That week does not need a note from anyone.
cls.trust.08  Keep the shape. It suits you.
```

### `letItBe`, for genuinely quiet weeks
```
cls.let.01    Some weeks are for other things. Nothing to do here.
cls.let.02    A quiet week does not need a response.
cls.let.03    Nothing needs acting on. Come back when there is something.
cls.let.04    This one can just be a quiet week.
cls.let.05    Leave it. The app will still be here.
cls.let.06    No week owes you a result.
cls.let.07    Rest is not a gap in the record.
```

### `noRhythmYet`, when cues have not stabilised
```
cls.new.01    A few more weeks and this page will know your rhythm.
cls.new.02    Not enough of a pattern yet to say anything useful.
cls.new.03    Come back in a month and this line will be sharper.
cls.new.04    Still learning what your weeks look like.
```

### `review`, the safe general closing
```
cls.rev.01    Look at your areas. Is the active item in each one still the right one?
cls.rev.02    Read the active items and see if you still agree with them.
cls.rev.03    Check what each area is currently asking of you.
cls.rev.04    One pass over the active items is enough.
cls.rev.05    Ask whether each active item is the thing you would pick today.
```

## 2.4 Worked plans

Real combinations from the benches above, showing what the user actually sees.

> **On Wednesday morning, before you open Work, spend ten minutes in Personal.**
> `cue.band.01` + `act.neg.01`, motivated by `neglectedArea`.

> **Before you add anything new, close the oldest item in Work.**
> `cue.hab.03` + `act.fin.02`, motivated by `intakeVsOutput`.

> **After your next focus session, write down what Rewrite the proposal intro is actually waiting on.**
> `cue.hab.02` + `act.brk.01`, motivated by `persistentItem`.

> **On Tuesday, your busiest day, give Health fifteen minutes.**
> `cue.day.02` + `act.oth.03`, motivated by `singleFocus`.

> **Before Friday, decide whether Reading stays or goes to the archive.**
> `cue.bound.04` + `act.neg.05`, motivated by `neglectedArea` at stage 2.

## 2.5 Corpus totals

| bank | lines |
|---|---|
| Cues | 21 |
| Actions | 52 |
| Non-plan closings | 24 |
| **Total** | **97** |

Cue and action pairing is constrained by family compatibility, which yields roughly **380 valid plan surfaces**, plus 24 non-plan closings. That is fewer raw lines than v1's 88, and considerably more surfaces, because every line now does two jobs.

## 2.6 Authoring rules for guidance

Everything from the volume 2 rules applies, plus:

1. **Every action is one concrete act completable in a week.** If it cannot be finished in a sitting, it is not an action, it is a project.
2. **Every action names a thing.** `spend ten minutes in Personal`, not `give an area some attention`. A slot that cannot be filled means the line cannot fire.
3. **No action implies the user failed.** Read each aloud preceded by *You should have*. If it still parses naturally, rewrite it.
4. **Cues must be things the app has observed, never things it assumes.** No `before bed`, no `at the weekend`, no `when you have a moment`. If the data cannot substantiate it, it does not exist.
5. **Every cue must read grammatically before every action in its compatible families.** Write it out against all of them.
6. **No exclamation marks, no imperatives dressed as encouragement, no `try to`.** `finish one thing` not `try to finish one thing`.

---

# PART THREE: SPEC DELTAS

## 3.1 `CLARITY_LOGIC_ENGINE.md`

**Section 1.1, absolute prohibitions.** Add:

> 11. The engine may never state or imply that the user did or did not act on a plan.

**Section 3, fact extraction.** Add `CueFacts` per part one, section 1.3, as a sixth category on `FactSet`.

**Section 6.2, Report families.** The `ONE THING` families are replaced by the guidance system. The family list becomes: `finishBeforeAdding`, `reconsiderArea`, `checkOtherAreas`, `breakItUp`, `protectFocusTime`, `pickOne`, `settleOn`, `repeatWhatWorked`, plus the three non-plan families `trustThePace`, `letItBe`, `noRhythmYet` and the fallback `review`.

**Section 11, failure modes.** Add three rows:

| failure | what it looks like | guard |
|---|---|---|
| Invented cue | a plan anchored to a day the user does not actually work | confidence thresholds in 1.3, no plan without a substantiated cue |
| Plan surveillance | the report notices you did not do it | the negative-case rule in 1.7, priority boost only, no injected sentences |
| Plan pile-up | three unresolved plans stacking | composition rule 4, no plan while one is unresolved |

**Section 13, tests.** Add:

- **Cue substantiation.** No plan renders with a cue below its confidence threshold, across 10,000 generated fact sets.
- **The non-compliance test.** A persona accepting every plan and completing none produces a simulated year in which no sentence references a plan, a commitment, an intention, or a failure to act. A reader of the dump cannot tell plans were accepted.
- **Silence floor.** Layer 6 returns `Nothing` or a non-plan closing on at least 15 percent of reports across the persona set.
- **Determinism.** Two devices with the same merged log produce the same plan for the same week.

## 3.2 `CORPUS_2_REPORT.md`

**Section 4** is replaced by part two above.

**Sections 1, 2, 3, 5, 6** are unchanged in content, with one global find and replace: every section label is now **sentence case, not all caps**, per design.md v2 section 9.10.

- `YOUR WEEK, HONESTLY` becomes `Your week, honestly`
- `PATTERN` becomes `Pattern`
- `ONE THING THIS WEEK` becomes `One thing`
- Section 2.6 `selfReportVsData` renders under the sidehead `What you said`

**Section 7, composition rules.** Add:

| A | B | why |
|---|---|---|
| `selfReportVsData` | a declining headline | pile-on |
| `selfReportVsData` | `neglectedArea` | pile-on |
| `selfReportVsData` | itself, on the same subject, ever | rarity is what gives it force |
| any plan | a declining headline plus `neglectedArea` together | a hard week does not also get homework |

**Defect fix.** Line `hd.frag.06` contains a stray non-Latin character. Replace with `Motion, not output.` and add an ASCII-plus-standard-punctuation test over the entire corpus so nothing similar survives again.

## 3.3 `MASTER_BUILD_PROMPT.md`

**Section 0, companion files.** `design.md` becomes `design-v2.md`. The visual reference becomes `clarity-now-visual-reference-v2.html`. Add this file to the list, marked as authoritative over the sections it replaces.

**Section 3, stack.** Fonts change from Instrument Serif and Inter to **Newsreader** and **Hanken Grotesk**, both variable, both SIL OFL, both bundled and subset. Newsreader must ship with its optical size axis intact, since the design system uses it at two roles.

**Section 4, project structure.** Add `domain.guidance` for layer 6, sitting alongside `domain.engine` and subject to the same purity contract: no Android imports, no clock, no randomness.

**Section 6.3, event catalogue.** Add two events:

| type | payload |
|---|---|
| `PLAN_OFFERED` | planId, weekStartKey, cueKey, actionKey, familyKey, subjectId, renderedLine, resolutionFactRef |
| `PLAN_ACCEPTED` | planId |

**Section 15, Clarity Report.** Section 15.3's `ONE THING THIS WEEK` is replaced by the guidance system. Add 15.6 pointing at part one of this file.

**Section 20, settings.** `Focus Bar` is renamed `Highlight the active session`, since there is no longer a bar.

**Section 27, verification checklist.** Add:

- **No element declares two separation devices.** A test walks the component set and fails the build on any element carrying both a hairline and a shadow, or a border and a wash.
- **No coloured stripe, bar or edge treatment exists anywhere in the codebase.** A test greps for edge-anchored coloured views.
- **No all-caps user-facing string exists** outside the two-letter day initials on the week ribbon.
- **Anti-slop pass** against the dated list in design-v2.md section 14.1, with the list reviewed and updated before each release rather than trusted as written.
- The three guidance tests from 3.1 above.

**Section 30, build order.** Insert a new phase between the current 9 and 10:

> **Phase 9b. Guidance.** Cue fact extraction with its confidence thresholds. Layer 6 and its composition rules. The plan events and acceptance. The non-evaluative follow-through via priority boost. The guidance corpus. The non-compliance test written **before** the follow-through code, not after.

Build order note: the follow-through in 1.7 is the last thing built and the first thing removed if it reads as supervision when tested. The panel's psychologist registered a formal reservation on it and that reservation stands.

---

# What is still open

**R8, the area card treatment.** Both options are rendered in the v2 visual reference, section A. A1 dissolves the card and separates by whitespace plus the wash. A2 keeps a slimmed card with elevation as its only separator. Both fix density, both remove the left border, both are the same content. A1 removes one more nested container. A2 keeps a stronger swipe affordance.

Everything downstream of that choice, the sheets, the widgets, the tutorial spotlight geometry, is written to work either way. Pick one and it applies everywhere.
