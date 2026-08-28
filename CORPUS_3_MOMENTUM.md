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
mo.steady.19   [R]  A regular fortnight.
mo.steady.20   [R]  Steady, more than fast.
mo.steady.21   [R]  Few empty days in it.
mo.steady.22   [R]  A fortnight with few gaps.
mo.steady.23   [R]  Regular, without being heavy.
mo.steady.24   [R]  A stretch that held.
mo.steady.25   [R]  Level going, across a fortnight.
mo.steady.26   [R]  Two weeks with very little missing.
mo.steady.27   [R]  Something on most of the days.
mo.steady.28   [R]  A fortnight that mostly kept going.
mo.steady.29   [R]  Two weeks of fairly even going.
mo.steady.30   [R]  A stretch with more days in it than gaps.
mo.steady.31   [R]  A fortnight with more movement than stillness.
mo.steady.32   [O]  Something moved on {dayCount} separate days.
mo.steady.33   [O]  {dayCount} days out of fourteen had something in them.
mo.steady.34   [O]  {n} things went through in fourteen days.
mo.steady.35   [O]  Activity has landed on {dayCount} of the days.
mo.steady.36   [O]  {m} days passed without anything in them.
mo.steady.37   [O]  Most of the fourteen days carried something.
mo.steady.38   [O]  Fourteen days with activity on most of them.
mo.steady.39   [O]  {dayCount} days had something, {m} did not.
mo.steady.40   [O]  The fortnight has been fairly even day to day.
mo.steady.41   [O]  Activity has been spread through the fortnight.
mo.steady.42   [O]  {n} completions across a fairly even fortnight.
mo.steady.43   [P]  {dayCount} days with something in them.
mo.steady.44   [P]  Fourteen days, {m} of them quiet.
mo.steady.45   [P]  Active on most days.
mo.steady.46   [P]  {dayCount} active days, {m} quiet.
mo.steady.47   [P]  {n} completions in fourteen days.
mo.steady.48   [P]  A steady fortnight.
mo.steady.49   [P]  Fourteen days, mostly active.
mo.steady.50   [P]  Even, {dayCount} days of fourteen.
mo.steady.51   [P]  {m} quiet days, the rest with something.
mo.steady.52   [N]  Fourteen days, {m} of them without activity.
mo.steady.53   [N]  The fortnight holds more active days than quiet ones.
mo.steady.54   [N]  Activity sits on most of the fourteen days.
mo.steady.55  [R]  Two weeks with something in most of them.
mo.steady.56  [R]  A fortnight of small steady days.
mo.steady.57  [O]  Activity turns up on more days than not.
mo.steady.58  [O]  The fortnight has few days with nothing in them.
mo.steady.59  [P]  Fourteen days, {dayCount} of them moving.
mo.steady.60  [N]  The active days outnumber the quiet ones.
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
mo.quiet.15  [R]  A fortnight with a few days in it.
mo.quiet.16  [R]  A fortnight that stayed mostly still.
mo.quiet.17  [R]  Still, rather than busy.
mo.quiet.18  [R]  The last two weeks, mostly unchanged.
mo.quiet.19  [R]  Two weeks that mostly stayed as they were.
mo.quiet.20  [R]  A stretch that stayed where it was.
mo.quiet.21  [R]  The fortnight is much as it started.
mo.quiet.22  [R]  A stretch with little in it.
mo.quiet.23  [R]  A fortnight without much in it.
mo.quiet.24  [R]  A quiet stretch of days.
mo.quiet.25  [R]  Little movement, across a fortnight.
mo.quiet.26  [R]  A fortnight that kept to itself.
mo.quiet.27  [R]  A fortnight that moved a little.
mo.quiet.28  [R]  Mostly still, across fourteen days.
mo.quiet.29  [R]  Fourteen days, little changed.
mo.quiet.30  [R]  A fortnight of small movements.
mo.quiet.31  [R]  Two weeks with a little in them.
mo.quiet.32  [R]  Small movements, over fourteen days.
mo.quiet.33  [R]  Two weeks of small things.
mo.quiet.34  [N]  The fortnight holds {dayCount} active days.
mo.quiet.35  [N]  {dayCount} days of the fourteen carried movement.
mo.quiet.36  [N]  Movement in the fortnight came to {n} things.
mo.quiet.37  [N]  The fortnight passed with little in it.
mo.quiet.38  [N]  Two weeks with {n} things in them.
mo.quiet.39  [N]  {areaName} had more of it than any other area.
mo.quiet.40  [N]  The fourteen days hold {n} finished things.
mo.quiet.41  [N]  Little has come out of the fortnight.
mo.quiet.42  [N]  {n} things came out of the two weeks.
mo.quiet.43  [N]  The two weeks hold {dayCount} days of movement.
mo.quiet.44  [N]  The busiest area of the fortnight was {areaName}.
mo.quiet.45  [O]  The last fourteen days have been slow.
mo.quiet.46  [O]  Two weeks have gone by quietly.
mo.quiet.47  [O]  The fortnight has run slowly.
mo.quiet.48  [O]  {areaName} saw more of it than the other areas did.
mo.quiet.49  [O]  The fortnight has been light.
mo.quiet.50  [O]  The last fortnight has held {n} finished things.
mo.quiet.51  [O]  The last two weeks have held little.
mo.quiet.52  [O]  {n} things have come out of fourteen days.
mo.quiet.53  [O]  {areaName} took the larger part of it.
mo.quiet.54  [O]  Two weeks have passed with little through them.
mo.quiet.55  [O]  The fortnight has been quiet, with {n} things through it.
mo.quiet.56  [P]  {dayCount} days, out of fourteen.
mo.quiet.57  [P]  A quiet fourteen days.
mo.quiet.58  [P]  {n} things in fourteen days.
mo.quiet.59  [P]  {areaName}, busier than the rest.
mo.quiet.60  [P]  Two weeks, {dayCount} active days.
mo.quiet.61  [P]  A slow two weeks.
mo.quiet.62  [P]  {n} out in two weeks.
mo.quiet.63  [P]  A fortnight with {n} things in it.
mo.quiet.64  [P]  A quiet fortnight, {areaName} busiest.
mo.quiet.65  [P]  Still, for fourteen days.
mo.quiet.66  [P]  {n} things across the fortnight.
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
mo.come.13   [R]  A restart.
mo.come.14   [R]  The quiet ended.
mo.come.15   [R]  A gap, then movement.
mo.come.16   [R]  Movement, after a pause.
mo.come.17   [R]  The pause is over.
mo.come.18   [R]  Under way again.
mo.come.19   [R]  A gap closed.
mo.come.20   [R]  The quiet gave way.
mo.come.21   [R]  The thread picked up again.
mo.come.22   [R]  Movement where there was none.
mo.come.23   [R]  What paused is going again.
mo.come.24   [R]  A stretch of nothing, then something.
mo.come.25   [R]  The pause has run out.
mo.come.26   [R]  Quiet, then not quiet.
mo.come.27   [R]  A stop, then a start.
mo.come.28   [R]  A gap with something on the other side of it.
mo.come.29   [R]  Something on the far side of a gap.
mo.come.30   [R]  A pause with something after it.
mo.come.31   [O]  {areaName} has started moving again.
mo.come.32   [O]  Activity has returned to {areaName}.
mo.come.33   [O]  {areaName} has events in it again.
mo.come.34   [O]  Movement has resumed in {areaName}.
mo.come.35   [O]  Something has restarted in {areaName}.
mo.come.36   [O]  {n} things have moved in {areaName}.
mo.come.37   [O]  {areaName} has come back into the fortnight.
mo.come.38   [O]  {areaName} has gone from still to moving.
mo.come.39   [O]  {areaName} has moved after a stretch of not moving.
mo.come.40   [O]  {n} things have gone in {areaName} this fortnight.
mo.come.41   [O]  Movement has come back to this area.
mo.come.42   [O]  Something has moved where nothing was moving.
mo.come.43   [P]  Going again.
mo.come.44   [P]  Started again.
mo.come.45   [P]  Back under way.
mo.come.46   [P]  A gap, then {areaName}.
mo.come.47   [P]  Something after a stop.
mo.come.48   [P]  {areaName} is active again.
mo.come.49   [P]  After a stop, movement.
mo.come.50   [P]  A restart in {areaName}.
mo.come.51   [P]  Something moving in {areaName} after a stop.
mo.come.52   [P]  {areaName} is one of the moving areas again.
mo.come.53   [P]  A gap in {areaName}, then movement after it.
mo.come.54   [P]  Movement in {areaName} where there had been none.
mo.come.55   [O]  Something arrived after a stretch of nothing.
mo.come.56   [O]  {areaName} has picked up after a still stretch.
mo.come.57   [O]  The still stretch in {areaName} has ended.
mo.come.58   [O]  Something moved in {areaName} after nothing did.
mo.come.59   [P]  {areaName} stopped for a stretch and started again.
mo.come.60   [P]  Nothing in {areaName}, then something.
mo.come.61   [R]  Movement in {areaName} on the other side of a gap.
mo.come.62   [O]  {areaName} went quiet and has moved since then.
mo.come.63   [P]  {areaName} was still and then it moved again.
mo.come.64   [P]  Nothing moved in {areaName} for a stretch, then something did.
mo.come.65  [R]  A stretch of nothing with a finish to it.
mo.come.66  [R]  Something that had stopped is going again.
mo.come.67  [O]  {areaName} has had a still stretch and an end to it.
mo.come.68  [O]  The gap in {areaName} has a far side to it.
mo.come.69  [P]  There was a stretch with nothing in {areaName}.
mo.come.70  [P]  There was a stop in {areaName}, then a start.
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
mo.bal.13   [R]  Wide, across two weeks.
mo.bal.14   [R]  A fortnight in several places.
mo.bal.15   [R]  Breadth, more than depth.
mo.bal.16   [R]  No one place took it all.
mo.bal.17   [R]  Room for more than one thing.
mo.bal.18   [R]  Level, more or less.
mo.bal.19   [R]  Spread out, over fourteen days.
mo.bal.20   [R]  No single thing has held it.
mo.bal.21   [R]  Across, rather than down.
mo.bal.22   [R]  Two weeks that went several ways.
mo.bal.23   [R]  Nothing has held more than half.
mo.bal.24   [R]  A fortnight that kept moving around.
mo.bal.25   [R]  Two weeks across several areas.
mo.bal.26   [R]  Several places, none of them all of it.
mo.bal.27   [R]  A fortnight that went in several directions.
mo.bal.28   [R]  A fortnight that did not settle in one place.
mo.bal.29   [R]  Several things at once, for two weeks.
mo.bal.30   [R]  Spread, without any one place holding it.
mo.bal.31   [R]  More than one thing at a time.
mo.bal.32   [R]  A fortnight with its weight spread out.
mo.bal.33   [R]  A fortnight that went around rather than down.
mo.bal.34   [O]  The fortnight has spread across {areaCount} areas.
mo.bal.35   [O]  Activity has gone to {areaCount} places.
mo.bal.36   [O]  Movement has reached {areaCount} areas.
mo.bal.37   [O]  {areaName} and {otherArea} have both moved.
mo.bal.38   [O]  Attention has gone several ways.
mo.bal.39   [O]  The fortnight has been shared out.
mo.bal.40   [O]  {n} things finished, {areaCount} areas moving.
mo.bal.41   [O]  No area has held half of it.
mo.bal.42   [O]  The busiest area holds less than half.
mo.bal.43   [O]  Every one of {areaCount} areas has moved.
mo.bal.44   [O]  {areaCount} areas have had something in them.
mo.bal.45   [O]  No area has run away with the fortnight.
mo.bal.46   [O]  Activity has been split across {areaCount} areas.
mo.bal.47   [O]  {areaCount} areas have had a share of it.
mo.bal.48   [O]  The fortnight has gone to more than one place.
mo.bal.49   [O]  Movement has been divided among {areaCount} areas.
mo.bal.50   [O]  The fortnight has not settled on one area.
mo.bal.51   [O]  Each of {areaCount} areas has seen something.
mo.bal.52   [O]  The fortnight has more than one busy area.
mo.bal.53   [O]  {areaName} has the largest share, under half.
mo.bal.54   [P]  Wide, across {areaCount} areas.
mo.bal.55   [P]  Two weeks across {areaCount} areas.
mo.bal.56   [P]  {areaName}, {otherArea} and the rest.
mo.bal.57   [P]  Nothing over half, for two weeks.
mo.bal.58   [P]  Broad, for a fortnight.
mo.bal.59   [P]  {areaCount} areas, all under half.
mo.bal.60   [P]  A broad fortnight, {areaCount} areas in it.
mo.bal.61   [P]  {areaCount} areas, spread fairly even.
mo.bal.62   [P]  {areaCount} areas moved, none of them more than half.
mo.bal.63   [P]  Fourteen days spread over {areaCount} different areas.
mo.bal.64   [P]  A fortnight that touched {areaCount} areas.
mo.bal.65   [P]  {n} completions, {areaCount} areas with something in them.
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
mo.single.13   [R]  One place, mostly.
mo.single.14   [R]  Deep in one place.
mo.single.15   [R]  A fortnight with one center.
mo.single.16   [R]  Concentrated, across two weeks.
mo.single.17   [R]  A single track, more or less.
mo.single.18   [R]  Two weeks with one subject.
mo.single.19   [R]  Mostly one thing.
mo.single.20   [R]  Depth, rather than reach.
mo.single.21   [R]  One area, fourteen days.
mo.single.22   [R]  The fortnight has one shape.
mo.single.23   [R]  One area held it.
mo.single.24   [R]  A fortnight that did not spread.
mo.single.25   [R]  Concentrated, more than broad.
mo.single.26   [R]  Two weeks in one place.
mo.single.27   [R]  A single line through the fortnight.
mo.single.28   [R]  Narrow, across fourteen days.
mo.single.29   [R]  The fortnight went one way.
mo.single.30   [R]  A long look at one thing.
mo.single.31   [R]  A fortnight spent mostly in one place.
mo.single.32   [R]  One thing at a time, for two weeks.
mo.single.33   [R]  A fortnight that stayed in one place.
mo.single.34   [R]  Two weeks that kept mostly to a single area.
mo.single.35   [R]  A fortnight that went down rather than across.
mo.single.36   [R]  A stretch with one thing at the middle of it.
mo.single.37   [R]  Two weeks that did not move around much.
mo.single.38   [R]  A fortnight with most of itself in one area.
mo.single.39   [R]  The fortnight kept coming back to one place.
mo.single.40   [R]  Two weeks that did not go very wide.
mo.single.41   [O]  {areaName} has taken {pct} of the fortnight.
mo.single.42   [O]  The fortnight has run through {areaName}.
mo.single.43   [O]  Most of what moved was in {areaName}.
mo.single.44   [O]  {areaName} covers {pct} of the fortnight.
mo.single.45   [O]  The other areas have seen less of it.
mo.single.46   [O]  The fortnight has stayed with {areaName}.
mo.single.47   [O]  {n} of {m} events happened in {areaName}.
mo.single.48   [O]  Activity has kept mostly to one area.
mo.single.49   [O]  {pct} of the moves were in one area.
mo.single.50   [O]  {otherArea} has moved much less than {areaName}.
mo.single.51   [O]  The last two weeks have run narrow.
mo.single.52   [O]  One area accounts for most of what happened.
mo.single.53   [O]  {areaName} has been where most of it happened.
mo.single.54   [O]  Most moves in the last fortnight were in {areaName}.
mo.single.55   [O]  Movement has stayed mostly inside one area.
mo.single.56   [O]  The fortnight has had a single main area.
mo.single.57   [O]  {areaName} has held the last fourteen days.
mo.single.58   [O]  Two weeks have run mostly through {areaName}.
mo.single.59   [P]  {pct} in {areaName}.
mo.single.60   [P]  {areaName}, most of the fortnight.
mo.single.61   [P]  {n} of {m} moves.
mo.single.62   [P]  Two weeks, one area.
mo.single.63   [P]  {pct} of the fortnight in one area.
mo.single.64   [P]  Fourteen days, mostly {areaName}.
mo.single.65   [P]  One area, most of the fortnight's moves.
mo.single.66   [P]  Deep in {areaName}.
mo.single.67   [P]  Two weeks mostly in one area.
mo.single.68   [P]  {areaName} held the fortnight.
mo.single.69   [P]  A fortnight mostly in {areaName}.
mo.single.70   [P]  {areaName} took most of it.
mo.single.71   [N]  The fortnight's moves are mostly in one area.
mo.single.72   [N]  The fortnight has a single dominant area.
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
mo.first.11  [R]  The beginning, with most of it still ahead.
mo.first.12  [R]  The start of something.
mo.first.13  [R]  Early, with a little in it.
mo.first.14  [R]  The first days, with little in them yet.
mo.first.15  [R]  Too early for a shape.
mo.first.16  [R]  A start, nothing more.
mo.first.17  [R]  The early days of it.
mo.first.18  [R]  Still near the start.
mo.first.19  [R]  A short history so far.
mo.first.20  [R]  The part of it that has happened so far.
mo.first.21  [R]  Early, before much of it has happened.
mo.first.22  [R]  There are not many days here to look at.
mo.first.23  [R]  A beginning with a few days behind it.
mo.first.24  [R]  Only a few days of this so far.
mo.first.25  [R]  The opening days.
mo.first.26  [R]  A start with a little in it.
mo.first.27  [R]  Early days, with something in them.
mo.first.28  [R]  The first days, no more than that.
mo.first.29  [R]  Early in a history that is {dayCount} days long.
mo.first.30  [R]  Early, with the rest of it ahead.
mo.first.31  [R]  The early days, before there is a shape.
mo.first.32  [O]  {n} things so far, in {dayCount} days.
mo.first.33  [O]  {n} things in the days since you started.
mo.first.34  [O]  The days so far hold {n} things.
mo.first.35  [O]  There are {dayCount} days of this so far.
mo.first.36  [O]  {dayCount} days of it have gone by.
mo.first.37  [O]  Something has moved in {areaCount} areas already.
mo.first.38  [O]  {n} things have gone through in the first days.
mo.first.39  [O]  {areaCount} areas have moved so far.
mo.first.40  [O]  This page has {dayCount} days to show.
mo.first.41  [O]  {n} things and {areaCount} areas so far.
mo.first.42  [O]  The first {dayCount} days have {n} things in them.
mo.first.43  [O]  The days since you started come to {dayCount}.
mo.first.44  [O]  {n} things have gone since the first day.
mo.first.45  [O]  Movement has reached {areaCount} areas in {dayCount} days.
mo.first.46  [O]  {areaCount} areas have seen something so far.
mo.first.47  [O]  What is here is {dayCount} days old.
mo.first.48  [O]  The page has {dayCount} days and {n} things on it.
mo.first.49  [P]  {dayCount} days, {areaCount} areas.
mo.first.50  [P]  {dayCount} days in, {n} things done.
mo.first.51  [P]  {areaCount} areas moving, {dayCount} days in.
mo.first.52  [P]  The first {dayCount} days.
mo.first.53  [P]  Movement in {areaCount} areas, {n} things done.
mo.first.54  [P]  A short history so far, {n} things.
mo.first.55  [P]  {n} things, early on.
mo.first.56  [P]  Early, with {n} things in it.
mo.first.57  [P]  {n} things across {areaCount} areas.
mo.first.58  [P]  {n} things through so far.
mo.first.59  [P]  Early, {areaCount} areas moving.
mo.first.60  [P]  A start with {n} things in it.
mo.first.61  [P]  {areaCount} areas, {n} things, {dayCount} days.
mo.first.62  [P]  {dayCount} days of history, {n} things in it.
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
mo.clean.07  [R]  Nothing here yet.
mo.clean.08  [R]  A blank page.
mo.clean.09  [R]  Still blank, for now.
mo.clean.10  [R]  A page with nothing on it.
mo.clean.11  [R]  Fourteen days with nothing in them.
mo.clean.12  [R]  A fortnight with nothing in it.
mo.clean.13  [R]  A page waiting for something to put on it.
mo.clean.14  [R]  Nothing has happened here in two weeks.
mo.clean.15  [R]  The last fourteen days are blank.
mo.clean.16  [R]  Two weeks with the page unchanged.
mo.clean.17  [R]  An empty two weeks, waiting.
mo.clean.18  [R]  A page that has nothing to say yet.
mo.clean.19  [R]  Two weeks with no marks on them.
mo.clean.20  [R]  A page that has not filled yet.
mo.clean.21  [R]  Nothing across the last fourteen days.
mo.clean.22  [R]  A page still waiting.
mo.clean.23  [R]  An empty stretch of fourteen days.
mo.clean.24  [R]  A stretch of days with nothing on it.
mo.clean.25  [R]  A fortnight with no events in it.
mo.clean.26  [R]  Two weeks that came and went.
mo.clean.27  [R]  A page with the last fortnight still blank.
mo.clean.28  [R]  Empty across two weeks.
mo.clean.29  [R]  Nothing to put on the page yet.
mo.clean.30  [R]  Nothing yet, across the whole fortnight.
mo.clean.31  [O]  The page is blank. It fills as things move.
mo.clean.32  [O]  Nothing in fourteen days. The page waits.
mo.clean.33  [O]  The page fills when something moves in an area.
mo.clean.34  [O]  Nothing has moved in fourteen days.
mo.clean.35  [O]  The areas are here. Nothing has moved in them.
mo.clean.36  [O]  Nothing to show. The page fills when things do.
mo.clean.37  [O]  The last fourteen days hold no events.
mo.clean.38  [O]  Nothing has reached this page in two weeks.
mo.clean.39  [O]  The page has nothing from the last fortnight.
mo.clean.40  [O]  Two weeks with nothing to put on the page.
mo.clean.41  [O]  The page stays blank until something happens.
mo.clean.42  [O]  The page has had nothing to fill it for two weeks.
mo.clean.43  [O]  The page waits for the first thing to move.
mo.clean.44  [O]  Nothing has come to the page in a fortnight.
mo.clean.45  [O]  Nothing has happened in the last fourteen days.
mo.clean.46  [O]  Nothing has gone through the areas in two weeks.
mo.clean.47  [O]  The page has stayed blank across the fortnight.
mo.clean.48  [O]  The last fourteen days have put nothing here.
mo.clean.49  [P]  Two weeks, nothing through.
mo.clean.50  [P]  No events on the page.
mo.clean.51  [P]  No events in fourteen days.
mo.clean.52  [P]  Nothing to put here so far.
mo.clean.53  [P]  Two weeks with nothing in them yet.
mo.clean.54  [P]  No moves in the last fortnight.
mo.clean.55  [P]  No moves in two weeks.
mo.clean.56  [P]  The page shows nothing so far.
mo.clean.57  [P]  The last two weeks are blank.
mo.clean.58  [P]  Nothing in the areas so far.
mo.clean.59  [P]  Nothing has come through in two weeks.
mo.clean.60  [P]  Fourteen days, nothing on any of them.
```

**Momentum headline totals: 467 lines across 8 families.** Phase 9 grew seven of the eight, sized by how often each fires rather than evenly, per `CLARITY_LOGIC_ENGINE.md` 11.1: `singleAreaWeek` fires 1,054 times in eleven simulated years and carries 72 lines, `balancedWeek` 831 and 65, `comeback` 604 and 70, `steadyStretch` 335 and 60, `firstDays` 139 and 62, `quietStretch` 124 and 66, `cleanSlate` 42 and 60. Every family that clears forty firings a year is now inside 11.1's hot band. `strongPace` does not clear it, and is sized where it stands.

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
bn.start.09   [R]  Early yet.
bn.start.10   [R]  The start of a week.
bn.start.11   [R]  Barely begun.
bn.start.12   [R]  Early, with most of it ahead.
bn.start.13   [R]  The opening of a week.
bn.start.14   [R]  Too early for much of a reading.
bn.start.15   [R]  Nothing settled yet.
bn.start.16   [R]  A week still mostly ahead.
bn.start.17   [R]  The first days of it.
bn.start.18   [R]  A beginning.
bn.start.19   [R]  Little to read this early.
bn.start.20   [R]  Still near the beginning.
bn.start.21   [R]  Early, before anything settles.
bn.start.22   [R]  There is more week ahead than behind.
bn.start.23   [R]  Nothing here has had time to become a shape.
bn.start.24   [R]  The shape of this week is still ahead.
bn.start.25   [R]  A week at the point where nothing is decided.
bn.start.26   [R]  A week still near its own beginning.
bn.start.27   [O]  The week has hardly started.
bn.start.28   [O]  A week that has just begun.
bn.start.29   [O]  The front of a week.
bn.start.30   [O]  A week in its first days.
bn.start.31   [O]  Little in it yet.
bn.start.32   [O]  The first stretch of a week.
bn.start.33   [O]  Starting to fill.
bn.start.34   [O]  The week has not taken a shape yet.
bn.start.35   [O]  Most of this week has not happened yet.
bn.start.36   [O]  A week that has barely started to fill.
bn.start.37   [O]  Most of the week is still in front of it.
bn.start.38   [O]  Very little of this week has happened yet.
bn.start.39   [O]  The early part of a week, before it fills.
bn.start.40   [O]  Nothing has had a chance to add up yet.
bn.start.41   [O]  Little of the week has gone by yet.
bn.start.42   [O]  The week has hardly had time to begin.
bn.start.43   [O]  Early in a week that has not filled yet.
bn.start.44   [P]  Nothing much to see yet.
bn.start.45   [P]  The week is new.
bn.start.46   [P]  A start, nothing more than that.
bn.start.47   [P]  A week with room in it.
bn.start.48   [P]  A week not yet filled.
bn.start.49   [P]  The week is not far along.
bn.start.50   [P]  A week with time left.
bn.start.51   [P]  The first days of a week, nothing more.
bn.start.52   [P]  Two days at most, with the rest to come.
bn.start.53   [P]  A week that is mostly still to come.
bn.start.54   [P]  Only a day or two of this week has happened.
bn.start.55   [N]  The week is barely open.
bn.start.56   [N]  Day one or two.
bn.start.57   [N]  The opening days.
bn.start.58   [N]  The week is one or two days old.
bn.start.59   [N]  It is early enough that nothing has settled.
bn.start.60   [N]  The week has had a day or two, no more.
bn.start.61   [N]  There has not been time for a shape yet.
bn.start.62   [N]  The week is young enough that little has landed.
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
bn.build.09  [R]  A week under way.
bn.build.10  [R]  Midweek, with something in it.
bn.build.11  [R]  The middle of a week.
bn.build.12  [R]  Partway through.
bn.build.13  [R]  A few days in, a few things done.
bn.build.14  [R]  Days behind it, days ahead.
bn.build.15  [R]  A week past its start.
bn.build.16  [R]  A week no longer new.
bn.build.17  [R]  A week that has begun to fill.
bn.build.18  [R]  Some of the week has happened.
bn.build.19  [R]  A week with a few days behind it.
bn.build.20  [R]  The middle days.
bn.build.21  [R]  Between the start of a week and the end of it.
bn.build.22  [R]  A week that has started to add up.
bn.build.23  [R]  Something already done, the week still open.
bn.build.24  [R]  The beginning has passed.
bn.build.25  [R]  In motion, midweek.
bn.build.26  [R]  A week with something already in it.
bn.build.27  [R]  A week with a few days already spent.
bn.build.28  [R]  Some days behind it, more of it to come.
bn.build.29  [R]  Some days gone, some things done, the week still open.
bn.build.30  [R]  Far enough in that something has happened.
bn.build.31  [R]  Part of a week, with something in it.
bn.build.32  [R]  Something behind it, more of it ahead.
bn.build.33  [R]  A week with days still in front of it.
bn.build.34  [R]  Some of the week spent, some of it left.
bn.build.35  [O]  A few things have gone through this week.
bn.build.36  [O]  {n} things done since the week opened.
bn.build.37  [O]  The week has a few things in it already.
bn.build.38  [O]  {n} through so far this week.
bn.build.39  [O]  {n} things finished this week.
bn.build.40  [O]  The week has been moving along.
bn.build.41  [O]  {n} things out, midweek.
bn.build.42  [O]  The week has been moving since it opened.
bn.build.43  [O]  {n} finished, midweek.
bn.build.44  [O]  Things have moved this week.
bn.build.45  [O]  The week has {n} things in it so far.
bn.build.46  [O]  The week has moved on since it started.
bn.build.47  [O]  More than one thing has been finished this week.
bn.build.48  [O]  The week has been running for a few days now.
bn.build.49  [O]  {n} things have come out of the week so far.
bn.build.50  [O]  Movement this week, with more days to come.
bn.build.51  [O]  The week has moved {n} things along.
bn.build.52  [O]  More has happened than at the start of the week.
bn.build.53  [O]  The week is a few days old and has {n} in it.
bn.build.54  [P]  {n} done this week.
bn.build.55  [P]  A few done, more days left.
bn.build.56  [P]  A few days in.
bn.build.57  [P]  {n} out so far.
bn.build.58  [P]  {n} completed midweek.
bn.build.59  [P]  {n} finished so far.
bn.build.60  [P]  The week has started to fill.
bn.build.61  [P]  A working week, partway through.
bn.build.62  [P]  {n} done, a few days in.
bn.build.63  [P]  A few days gone, {n} finished.
bn.build.64  [P]  A few days, {n} things.
bn.build.65  [P]  {n} this week, with days left.
bn.build.66  [P]  Partway, with {n} done.
bn.build.67  [P]  {n} out, days still to come.
bn.build.68  [P]  {n} through the first days of the week.
bn.build.69  [N]  {n} finished things sit in the week so far.
bn.build.70  [N]  The week carries {n} so far.
bn.build.71  [N]  The days gone this week hold {n} finished things.
bn.build.72  [N]  Something has come out of the first days of this week.
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
bn.strong.09  [R]  More than usual.
bn.strong.10  [R]  A full week, and not over.
bn.strong.11  [R]  Busier than the weeks around it.
bn.strong.12  [R]  More in it than the weeks before.
bn.strong.13  [R]  A week with more in it than most.
bn.strong.14  [R]  Ahead of the ordinary.
bn.strong.15  [R]  Plenty already, with the week still open.
bn.strong.16  [R]  A quick week so far.
bn.strong.17  [R]  More than the recent weeks have held.
bn.strong.18  [R]  Above what the recent weeks held.
bn.strong.19  [R]  Already past what most weeks hold.
bn.strong.20  [R]  Above your own average.
bn.strong.21  [R]  More finished here than in most weeks.
bn.strong.22  [R]  More movement than the weeks before it.
bn.strong.23  [R]  A week already fuller than most.
bn.strong.24  [R]  Fuller than most, and not finished.
bn.strong.25  [R]  A lot already, and days left.
bn.strong.26  [R]  A week ahead of the ones before it.
bn.strong.27  [R]  Above the recent weeks, and still open.
bn.strong.28  [R]  The week is holding more than most.
bn.strong.29  [R]  Above the usual, so far.
bn.strong.30  [R]  Above the pace of the last few weeks.
bn.strong.31  [O]  More has gone through than in a usual week.
bn.strong.32  [O]  {n} things finished, more than most weeks.
bn.strong.33  [O]  The week is running above your recent average.
bn.strong.34  [O]  The week has passed your recent average already.
bn.strong.35  [O]  The last few weeks were lighter than this one.
bn.strong.36  [O]  {n} things out this week, above the usual.
bn.strong.37  [O]  Above your recent weeks, with days still to come.
bn.strong.38  [O]  {n} through, more than usual.
bn.strong.39  [O]  More has moved this week than in the weeks before.
bn.strong.40  [O]  The week has more in it than the recent ones.
bn.strong.41  [O]  {n} finished, above your usual.
bn.strong.42  [O]  Your recent weeks have held less than this one.
bn.strong.43  [O]  More things have gone this week than usual.
bn.strong.44  [O]  {n} things done, above the recent weeks.
bn.strong.45  [O]  {n} things have gone, more than most weeks hold.
bn.strong.46  [P]  {n} done, with days left.
bn.strong.47  [P]  A busy week so far.
bn.strong.48  [P]  {n} through, above the usual.
bn.strong.49  [P]  More done than usual this week.
bn.strong.50  [P]  {n} out of a week that is not over.
bn.strong.51  [P]  Going faster than usual.
bn.strong.52  [P]  A week above your usual.
bn.strong.53  [P]  {n} already, and the week is not over.
bn.strong.54  [P]  Plenty through this week.
bn.strong.55  [P]  A fuller week than usual.
bn.strong.56  [P]  A lot done already.
bn.strong.57  [P]  This week has done more than the ones around it.
bn.strong.58  [P]  {n} out, and more week to come.
bn.strong.59  [P]  {n} finished, and days still left.
bn.strong.60  [N]  {n} things have gone, above the recent weeks.
bn.strong.61  [N]  The week holds {n} finished things already.
bn.strong.62  [N]  More has come out of this week than out of most.
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
bn.mixed.09   [R]  A week with a tilt to it.
bn.mixed.10   [R]  Lopsided, for now.
bn.mixed.11   [R]  Weighted rather than spread.
bn.mixed.12   [R]  A week that leans one way.
bn.mixed.13   [R]  A week with a center of gravity in it.
bn.mixed.14   [R]  A shape rather than a spread.
bn.mixed.15   [R]  Tilted, so far.
bn.mixed.16   [R]  A week with a heavier side.
bn.mixed.17   [R]  Spread unevenly, so far.
bn.mixed.18   [R]  Unequal, with the week still going.
bn.mixed.19   [R]  A week gathered mostly in one place.
bn.mixed.20   [R]  Concentrated, with the rest thinner.
bn.mixed.21   [R]  A week with more of it in one area.
bn.mixed.22   [R]  Some places busier than others.
bn.mixed.23   [R]  A week that did not spread evenly.
bn.mixed.24   [R]  The weight of the week sits in one place.
bn.mixed.25   [R]  Uneven going.
bn.mixed.26   [R]  Most of it in one area, so far.
bn.mixed.27   [O]  {areaName} has held most of the week so far.
bn.mixed.28   [O]  Movement in {areaCount} areas, most of it in one.
bn.mixed.29   [O]  The week has landed mostly in {areaName}.
bn.mixed.30   [O]  {areaName} has taken half of it or more.
bn.mixed.31   [O]  Activity has gathered in one area.
bn.mixed.32   [O]  {areaCount} of {m} areas have seen something.
bn.mixed.33   [O]  One area holds half the week or more.
bn.mixed.34   [O]  {areaName} accounts for most of the week.
bn.mixed.35   [O]  Attention has pooled in one place.
bn.mixed.36   [O]  More has happened in {areaName} than anywhere else.
bn.mixed.37   [O]  The week has run unevenly across {areaCount} areas.
bn.mixed.38   [O]  {areaName} has carried the week so far.
bn.mixed.39   [O]  The week has leaned toward {areaName}.
bn.mixed.40   [O]  Activity has run heavier in one area.
bn.mixed.41   [O]  Half the week or more has been in {areaName}.
bn.mixed.42   [O]  Movement has concentrated in a single area.
bn.mixed.43   [O]  The week has filled unevenly.
bn.mixed.44   [O]  The week has been busiest in {areaName}.
bn.mixed.45   [O]  Activity has spread across {areaCount} areas unevenly.
bn.mixed.46   [P]  {areaCount} areas moving this week.
bn.mixed.47   [P]  Mostly {areaName} this week.
bn.mixed.48   [P]  A week weighted toward {areaName}.
bn.mixed.49   [P]  Busy in {areaName}, quieter elsewhere.
bn.mixed.50   [P]  At least half the week in {areaName}.
bn.mixed.51   [P]  Uneven across {areaCount} areas.
bn.mixed.52   [P]  A lopsided week so far.
bn.mixed.53   [P]  Concentrated so far.
bn.mixed.54   [P]  Weighted toward one area.
bn.mixed.55   [P]  A week mostly in one area.
bn.mixed.56   [P]  {areaCount} areas, one of them most of the week.
bn.mixed.57   [P]  One area doing most of the week.
bn.mixed.58   [P]  Mostly one area, partly the rest.
bn.mixed.59   [P]  {areaName} and less of everything else.
bn.mixed.60   [P]  Uneven, with the week still open.
bn.mixed.61   [P]  Movement in some areas, less in others.
bn.mixed.62   [P]  Some areas busy, others quieter.
bn.mixed.63   [P]  A week centered on {areaName}.
bn.mixed.64   [N]  Activity in {areaCount} areas, unevenly divided.
bn.mixed.65   [N]  The week is not spread evenly across {areaCount} areas.
bn.mixed.66   [N]  One area accounts for at least half of it.
bn.mixed.67   [N]  The week is uneven across its areas.
bn.mixed.68   [N]  Activity has not been even across the areas.
bn.mixed.69   [O]  One area has at least half of what moved.
bn.mixed.70   [O]  The week has piled up in one area.
bn.mixed.71   [O]  {areaCount} areas moved, one of them at least half.
bn.mixed.72   [O]  The week has bunched up in {areaName}.
bn.mixed.73   [R]  A week that piled up in one place.
bn.mixed.74   [P]  Piled up in {areaName}.
bn.mixed.75   [P]  Bunched in one area.
bn.mixed.76   [N]  The week is bunched rather than spread.
bn.mixed.77   [N]  Half of the week or more sits in one area.
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

**Banner totals: 281 sentences and 10 captions, 2,810 combinations across 5 families.** Four of the five are grown and inside 11.1's hot band: `weekMixed` fires 1,308 times in eleven simulated years and carries 77 sentences, `weekStarting` 804 and 62, `weekBuilding` 284 and 72, `weekStrong` 65 and 62. **The fifth, `weekQuiet`, is left at eight lines on purpose, because it has never once spoken.** Its eight lines are every one of them `[N]`, which authoring rule 5 requires of a quiet state, and 7.4 reaches the neutral agent register only through a rule marked `unflattering`, which no Momentum rule is. So the family qualifies and the realizer finds no line it is allowed to say: across 3,148 simulated banner windows it produced nothing. That is a rule to amend, not a bench to deepen, and adding lines here would hide it. `MASTER_BUILD_PROMPT.md` 14b.10 and Addendum 01 7c are the amendment; `MomentumRules` and `RealizerTest` record the same conflict from the code's side.

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
| Momentum headlines | 467 | 467 |
| Banner sentences | 281 | |
| Banner captions | 10 | 2,810 combined |
| **Total** | **758** | **3,277** |

**Corpus grand total across all three volumes: 4,733 authored lines, and roughly 320,000 distinct surfaces.** Counted when volume 2 closed, with 1,775 Pulse lines and 2,200 Report lines beside this volume's 758. Two earlier readings stood here: 2,942, taken at the moment this volume closed, when volume 1 was still being grown and had moved twice in the hour before it, and 4,407, which assumed 1,874 Report lines against the 1,617 the file carried and was failing the audit from the day it was written. This one is settled rather than a reading: all three volumes have closed. `CorpusTotalsAuditTest` recounts it on every run and names this line when it drifts. The surface figure is the one number here that is approximate on purpose, because two of its three parts are products of benches rather than counts of lines.

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
8. **`mo.come` states no length of absence.** `MASTER_BUILD_PROMPT.md` 14b.4 governs this family: no line may say how long the gap was, count what waited through it, or ask where the person has been, in any wording including a warm one. **Not in days, not in weeks, not as a date, and not as an adjective**, so `a long gap` and `after a while` are the same violation as `{ageDays}`. Every line added in phase 9 is therefore free of `{ageDays}`, and the vague nouns the family already uses, `a gap`, `a pause`, `a stop`, are what is left to say instead. The three approved lines that do render the gap, `mo.come.01`, `mo.come.02` and `mo.come.05`, predate the rule and are the reason `comeback` sits in `FamilyAvailability.WITHHELD_ON_RE_ENTRY`
9. **A count slot never says what is waiting.** `{n}` in this file is always something that moved, never a queue depth, for the same reason as 8
10. **`mo.clean` speaks to two people and every line has to be true for both.** `cleanSlate` triggers on no events in the window plus at least one area, and carries no criterion on the age of the install. So it fires on a fresh install and it fires on an install two years old whose last fourteen days are completely empty. The six approved lines are written for the first reading only, and `A clean slate. Your first steps will show up here.` said to somebody coming back after a fortnight away is the app having forgotten them. Every line added in phase 9 is true of an empty fourteen days whichever person is reading it, and none of them says first, begin or start
11. **`{areaName}` names the busiest area and never a majority.** `RollupFacts.dominantAreaId` is a strict maximum and is null on a tie, so it always means more than any other area and it does not mean more than all of them together. `balancedWeek` and `singleAreaWeek` carry share criteria that make `most` true; `quietStretch`, `steadyStretch` and `strongPace` carry none, and in those three a line saying `most` is false whenever four areas split the window. New lines there say busiest, or more than any other. `mo.quiet.13` and `mo.steady.15` predate this and say most
12. **`quietStretch` counts the days that had something in them, never the days that did not.** `{dayCount}` binds `activeDays`, and `quietDays` has no binding in this family at all, so the harsher arithmetic cannot be rendered even by an author who reaches for it. That is deliberate and is the binding table doing the work rather than somebody's restraint
13. **The reflective bench is the only one either surface reaches today.** `Realizer.realize` takes the first register that has a line it can fill, and `RegisterChoice` puts `REFLECTIVE` first for every rule not marked `unflattering`, which is every rule in `MomentumRules`. An observational or plain line is therefore said only when no reflective line can fill from the day's facts. Phase 9 made reflective the deepest bench in every family it grew, which is a hedge rather than a preference: it is where the firings actually land now, and when 14b.10 widens 7.4 and the order changes, the other three benches are already there to take over
