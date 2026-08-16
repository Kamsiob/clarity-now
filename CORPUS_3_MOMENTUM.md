# Clarity Phrasing Corpus, Volume 3: Momentum and the Areas Banner

The authored language library for the Momentum headline and the weekly status banner on the Areas screen. `CLARITY_LOGIC_ENGINE.md` governs selection, rendering and validation; this file governs what is said.

**Volume 3 of 3.** Volume 1 is Pulse, volume 2 is the Report and guidance. This file covers both remaining engine purposes, `MOMENTUM_HEADLINE` and `AREAS_BANNER`, because both are short, both are read by `MomentumEngine`, and splitting them across two files would create a fourth place to look for one sentence.

---

## How this file is structured

Two purposes, thirteen families, one bench each. No escalation stages: these surfaces state the current shape and have nothing to escalate about.

Registers are `[P]` plain, `[O]` observational, `[R]` reflective, `[N]` neutral-agent. **There is no `[E]` editorial register here.** Editorial voice belongs to the Report, which is read once a week and can afford it. Momentum is read many times a day and an editorial line becomes exhausting by the third reading.

**Key prefixes.** `mo.` for Momentum headlines, `bn.` for banner sentences, `bnc.` for banner captions. The mapping to `FamilyKey` is in the table at the end.

**Slots.** `{n}` `{m}` `{dayCount}` `{areaCount}` `{areaName}` `{otherArea}` `{minutes}` `{sessions}` `{ageDays}` `{sinceRef}` `{pct}`

Counts of two through nine render as **words**, matching Pulse and not the Report. See `CLARITY_LOGIC_ENGINE.md` 7.2.

---

## The rule that governs every line in this file

**Momentum observes and never interprets.** No line here may contain `because`, `suggests`, `means`, `so`, `therefore`, `which is why`, or any causal construction. It may not offer advice, ask a question, or draw a conclusion. It states the shape of the last fourteen days and stops.

That vocabulary belongs to the Report. A catalog test enforces the boundary across every bench below.

Under 12 words per headline. Only real area names.

---

# PURPOSE: MOMENTUM_HEADLINE

Under 12 words, serif, the first thing on the Momentum screen. Selected on screen entry.

## mo.steady, steadyStretch

Trigger: active on 9 or more of the last 14 days.

```
mo.steady.01  [P]  Active {dayCount} of the last fourteen days.
mo.steady.02  [P]  {dayCount} active days out of fourteen.
mo.steady.03  [O]  A steady stretch, {dayCount} of the last fourteen days.
mo.steady.04  [O]  Most days in the last fortnight had something in them.
mo.steady.05  [O]  You have shown up on {dayCount} of the last fourteen days.
mo.steady.06  [O]  Something moved on most days this fortnight.
mo.steady.07  [R]  A consistent stretch.
mo.steady.08  [R]  The last two weeks have been fairly even.
mo.steady.09  [R]  Steady, across fourteen days.
mo.steady.10  [P]  {dayCount} of fourteen.
mo.steady.11  [O]  Only {m} quiet days in the last fortnight.
mo.steady.12  [N]  Fourteen days, {dayCount} with activity.
mo.steady.13  [R]  A rhythm, more than a run.
mo.steady.14  [O]  {n} things finished across those days.
mo.steady.15  [O]  {areaName} has been part of most of them.
mo.steady.16  [R]  Even going, two weeks running.
mo.steady.17  [P]  Active most days this fortnight.
mo.steady.18  [O]  A stretch with very few gaps in it.
```

## mo.quiet, quietStretch

Trigger: active on 4 or fewer of the last 14 days.

```
mo.quiet.01  [P]  Active {dayCount} of the last fourteen days.
mo.quiet.02  [N]  {dayCount} of the last fourteen days had activity.
mo.quiet.03  [N]  A quiet fortnight.
mo.quiet.04  [O]  The last two weeks have been mostly still.
mo.quiet.05  [N]  Fourteen days, {dayCount} with something in them.
mo.quiet.06  [R]  A slow stretch.
mo.quiet.07  [N]  Most of the last fortnight passed without a move here.
mo.quiet.08  [R]  Quiet, across two weeks.
mo.quiet.09  [P]  {dayCount} active days in fourteen.
mo.quiet.10  [N]  The queues have mostly held where they were.
mo.quiet.11  [R]  A still fortnight.
mo.quiet.12  [N]  Little has moved in two weeks.
mo.quiet.13  [O]  {areaName} accounts for most of what did happen.
mo.quiet.14  [N]  Two weeks, {n} completions.
```

## mo.come, comeback

Trigger: activity resumed after a gap of 5 or more days inside the window.

```
mo.come.01  [P]  Back after {ageDays}.
mo.come.02  [O]  Activity picked up again after {ageDays} of nothing.
mo.come.03  [O]  Things started moving again {ageDays} ago.
mo.come.04  [R]  A return, after a gap.
mo.come.05  [P]  {ageDays} quiet, then this week.
mo.come.06  [O]  The last few days have been busier than the fortnight before.
mo.come.07  [R]  Something restarted.
mo.come.08  [O]  {areaName} came back first.
mo.come.09  [P]  Moving again.
mo.come.10  [R]  The gap ended.
mo.come.11  [O]  {n} things have gone since it picked up.
mo.come.12  [R]  Back in motion.
```

## mo.bal, balancedWeek

Trigger: three or more areas active, none above 50 percent.

```
mo.bal.01  [P]  {areaCount} areas moved this fortnight.
mo.bal.02  [O]  Attention spread across {areaCount} areas.
mo.bal.03  [O]  No single area has taken over.
mo.bal.04  [R]  A wide fortnight.
mo.bal.05  [P]  All {areaCount} areas have moved.
mo.bal.06  [O]  Every area has seen something in the last two weeks.
mo.bal.07  [R]  Evenly spread, across two weeks.
mo.bal.08  [O]  {areaName} and {otherArea} have moved about the same amount.
mo.bal.09  [P]  {areaCount} areas, {n} completions.
mo.bal.10  [R]  Broad rather than deep.
mo.bal.11  [O]  The gap between your busiest and quietest area is small.
mo.bal.12  [R]  Nothing has dominated.
```

## mo.single, singleAreaWeek

Trigger: one area at 70 percent or more of window events.

```
mo.single.01  [P]  {pct} of the last fortnight was {areaName}.
mo.single.02  [O]  {areaName} has held most of the last two weeks.
mo.single.03  [O]  Attention has stayed mostly in one area.
mo.single.04  [R]  A narrow fortnight.
mo.single.05  [P]  Mostly {areaName}, for two weeks.
mo.single.06  [O]  {n} of your {m} moves were in {areaName}.
mo.single.07  [R]  One area, most of the time.
mo.single.08  [N]  Most of the fortnight landed in a single area.
mo.single.09  [O]  {otherArea} has seen little by comparison.
mo.single.10  [R]  Deep rather than broad.
mo.single.11  [P]  {areaName}, mostly.
mo.single.12  [N]  One area holds {pct} of the fortnight.
```

## mo.pace, strongPace

Trigger: completions in the window exceed the 8 week average by a clear margin.

```
mo.pace.01  [P]  {n} things finished in the last fortnight.
mo.pace.02  [O]  More has gone through than usual.
mo.pace.03  [O]  {n} completions, above your usual pace.
mo.pace.04  [R]  A quick fortnight.
mo.pace.05  [P]  {n} finished, {m} added.
mo.pace.06  [O]  The queues are shorter than they were two weeks ago.
mo.pace.07  [R]  Things have been moving.
mo.pace.08  [O]  {areaName} accounts for {m} of them.
mo.pace.09  [P]  {n} out in fourteen days.
mo.pace.10  [R]  A faster stretch than usual.
mo.pace.11  [O]  Your best fortnight since {sinceRef}.
mo.pace.12  [P]  {n} completions and {sessions} focus sessions.
```

## mo.first, firstDays

Trigger: fewer than 14 days since install, with at least one event.

```
mo.first.01  [P]  {dayCount} days in.
mo.first.02  [O]  Early days. {n} things so far.
mo.first.03  [O]  {n} completions in your first {dayCount} days.
mo.first.04  [R]  Just getting going.
mo.first.05  [P]  Your first week is taking shape.
mo.first.06  [O]  {areaCount} areas, {n} moves so far.
mo.first.07  [R]  The beginning of a picture.
mo.first.08  [O]  This page fills out as the days do.
mo.first.09  [P]  {dayCount} days, {n} completions.
mo.first.10  [R]  Early, but it is starting.
```

## mo.clean, cleanSlate

Trigger: no events at all.

```
mo.clean.01  [O]  A clean slate. Your first steps will show up here.
mo.clean.02  [O]  Nothing here yet. It fills in as you use the app.
mo.clean.03  [R]  Empty, for now.
mo.clean.04  [O]  This page starts filling once something moves.
mo.clean.05  [R]  A blank fortnight, waiting.
mo.clean.06  [O]  No activity yet. Add something to an area to begin.
```

**Momentum headline totals: 112 lines across 8 families.** At one headline per day, roughly 365 a year, with the 90 day exclusion applied, no line recurs inside three months.

---

# PURPOSE: AREAS_BANNER

Two parts, always rendered together: a **sentence** in bodyStrong and a **caption** beneath it stating the numbers. Recomputed at most once per hour of app use, throttled in the ViewModel and not in the engine.

The sentence never guilts. The caption is pure arithmetic and carries no tone at all.

## bn.start, weekStarting

Trigger: Monday or Tuesday, fewer than 3 completions.

```
bn.start.01  [P]  Your week is just getting started.
bn.start.02  [O]  Early in the week.
bn.start.03  [P]  The week is young.
bn.start.04  [N]  Two days in.
bn.start.05  [O]  Not much yet, which is normal for a Monday.
bn.start.06  [P]  A fresh week.
bn.start.07  [N]  The week has just opened.
bn.start.08  [O]  Starting out.
```

## bn.build, weekBuilding

Trigger: midweek, completions accumulating steadily.

```
bn.build.01  [P]  The week is building.
bn.build.02  [O]  Things are moving.
bn.build.03  [P]  A few things done so far.
bn.build.04  [O]  Steady so far this week.
bn.build.05  [N]  {n} through, midweek.
bn.build.06  [P]  Making progress this week.
bn.build.07  [O]  On pace with last week.
bn.build.08  [P]  A working week.
```

## bn.strong, weekStrong

Trigger: completions clearly above the recent weekly average.

```
bn.strong.01  [P]  A strong week so far.
bn.strong.02  [O]  Ahead of your usual pace.
bn.strong.03  [P]  A lot has moved this week.
bn.strong.04  [O]  More than most weeks, and it is not over.
bn.strong.05  [P]  Picking up pace.
bn.strong.06  [N]  {n} through already.
bn.strong.07  [O]  Your busiest week in a while.
bn.strong.08  [P]  Plenty done this week.
```

## bn.quiet, weekQuiet

Trigger: very low activity for the point in the week.

```
bn.quiet.01  [N]  A quiet week so far.
bn.quiet.02  [N]  Not much has moved this week.
bn.quiet.03  [N]  Still, so far.
bn.quiet.04  [N]  The week has been quiet here.
bn.quiet.05  [N]  Little movement this week.
bn.quiet.06  [N]  Nothing completed yet this week.
bn.quiet.07  [N]  A slow week here.
bn.quiet.08  [N]  Quiet, for now.
```

**Every line in this family is `[N]`.** A quiet week is the one banner state where agentive second person, *you have not completed anything*, would read as an accusation on a screen the user sees every time they open the app.

## bn.mixed, weekMixed

Trigger: activity uneven across areas or across days.

```
bn.mixed.01  [P]  An uneven week so far.
bn.mixed.02  [O]  Busy in places.
bn.mixed.03  [N]  Some areas have moved, some have not.
bn.mixed.04  [O]  A mixed week.
bn.mixed.05  [P]  Concentrated in {areaName} this week.
bn.mixed.06  [N]  The week has had a shape to it.
bn.mixed.07  [O]  Movement in {areaCount} of {m} areas.
bn.mixed.08  [P]  Patchy so far.
```

## Banner captions

Selected independently of the sentence and combined with it. **Pure arithmetic, no tone.** Any caption may follow any sentence, and every combination was checked.

```
bnc.01  {n} completed, {m} of {areaCount} areas active
bnc.02  {n} completed this week
bnc.03  {n} completed, {m} added
bnc.04  No items completed yet, {m} of {areaCount} areas active
bnc.05  {m} of {areaCount} areas active
bnc.06  {n} completed, {minutes} minutes focused
bnc.07  {n} completed, {sessions} focus sessions
bnc.08  {n} waiting across {areaCount} areas
bnc.09  {n} completed, {m} waiting
bnc.10  Nothing completed yet this week
```

**Zero never renders through a `Count` slot.** `bnc.04` and `bnc.10` exist precisely so the zero case has authored lines rather than a template producing `0 completed`. See `CLARITY_LOGIC_ENGINE.md` 8, validator check 4.

**Banner totals: 40 sentences and 10 captions, 400 combinations across 5 families.**

---

# Key prefixes

| key prefix | purpose | family |
|---|---|---|
| `mo.steady` | MOMENTUM_HEADLINE | steadyStretch |
| `mo.quiet` | MOMENTUM_HEADLINE | quietStretch |
| `mo.come` | MOMENTUM_HEADLINE | comeback |
| `mo.bal` | MOMENTUM_HEADLINE | balancedWeek |
| `mo.single` | MOMENTUM_HEADLINE | singleAreaWeek |
| `mo.pace` | MOMENTUM_HEADLINE | strongPace |
| `mo.first` | MOMENTUM_HEADLINE | firstDays |
| `mo.clean` | MOMENTUM_HEADLINE | cleanSlate |
| `bn.start` | AREAS_BANNER | weekStarting |
| `bn.build` | AREAS_BANNER | weekBuilding |
| `bn.strong` | AREAS_BANNER | weekStrong |
| `bn.quiet` | AREAS_BANNER | weekQuiet |
| `bn.mixed` | AREAS_BANNER | weekMixed |
| `bnc.` | AREAS_BANNER | caption bench, shared across all five |

---

# Totals, volume 3

| bench | lines | surfaces |
|---|---|---|
| Momentum headlines | 112 | 112 |
| Banner sentences | 40 | |
| Banner captions | 10 | 400 combined |
| **Total** | **162** | **512** |

**Corpus grand total across all three volumes: 1,519 authored lines, roughly 17,200 distinct surfaces.**

---

# Authoring rules for this volume

Everything in volumes 1 and 2 applies, plus:

1. **No causal construction, ever.** No because, suggests, means, so, therefore, which is why. A catalog test enforces this and it is the single rule that separates Momentum from the Report
2. **No questions.** The Pulse asks; Momentum does not
3. **No advice.** The closing line of the Report is the only place advice exists
4. **No editorial register.** These surfaces are read many times a day and a clever line becomes tiresome by the third reading
5. **Every quiet or low-activity line is `[N]`.** These states appear on the screen the user opens most often, and agentive framing on a bad week is the fastest way to make someone delete an app
6. **Captions carry no tone.** They are arithmetic. If a caption reads as commentary, rewrite it
7. **Under 12 words per headline**, checked against the realized form with slots filled at their longest plausible value
