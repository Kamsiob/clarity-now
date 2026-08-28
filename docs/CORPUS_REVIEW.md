# The corpus, read in place

Three simulated years, in order, with every sentence annotated by the family, the stage,
the register and the variant key that produced it. Taken on August 28, 2026, at the close
of phase 9, over the corpus as it now stands: **4,733 authored lines** across the three
volumes, recounted from the files rather than claimed. The last commit records 1,503, and
every pass between that commit and phase 9 states in `DECISIONS.md` that it wrote no corpus
line, so **phase 9 added 3,230 of the 4,733**.

**This is the artifact phase 9 is judged on, and the judgment is a reading rather than a
number.** Every mechanical gate in the build is green and every reading is in
`CLARITY_LOGIC_ENGINE.md` 12. What no gate can answer is whether a year of these sentences
sounds like something a person would say about your week. Eight sessions wrote into this
corpus against a specification and a gate suite, and none of them compiled a line of it.
This file is the first time any of that writing can be read the way it will actually
arrive: one sentence after another, in the order one life produced them, with the days the
app said nothing left in.

**Read it in order, and read at least one whole month.** A corpus is not judged a line at a
time. Almost any single sentence here survives being read alone; what a reader can only see
in sequence is a shape repeating, a claim arriving twice in one week, or a voice that has
drifted between two families. The months are headed and counted so a reader can stop
anywhere and come back.

---

## How to read a line

```text
2026-03-14  [pulse]  persistence / stage 2 / reflective / persistence.s2.11
  rule:  pulse.persistence.s2
  fired: active item age 9 days (>= 6), area has 4+ events, no swap in window
  facts: activeItemAgeDays=9 [item.ageDays], areaName=Work
  > Still Rewrite the proposal intro. Nine days now.
  > Deep work, or stuck?
  ? Deep work | Stuck
```

| part | what it is |
|---|---|
| `2026-03-14  [pulse]` | the simulated day, and which surface spoke. The surfaces are `pulse`, `momentum`, `banner`, `report headline`, `report observation` and `report pattern` |
| `persistence / stage 2 / reflective` | the family, the rung of its escalation ladder, and the register layer 4 chose |
| `persistence.s2.11` | the variant key. It is stable forever and never reused, so any line in this file can be found in its corpus file by searching for the key |
| `rule:` | the rule in the catalog that qualified. The logic side of the same firing |
| `fired:` | every criterion that was true, in the words the catalog states them in |
| `facts:` | every value the sentence used, each number carrying in brackets the `FactRef` the validator re-read it from. A number with no bracket beside it would be a number the engine invented |
| `>` | the sentence. A Pulse carries a second `>` for its question |
| `?` | the two Pulse responses. Read them aloud with no context: if one sounds like the answer a good person gives, both are wrong |
| `SILENT (reason)` | the surface said nothing, and why. `NO_RULE_QUALIFIED` is a day with nothing in it, `ALL_QUALIFIED_RULES_FILTERED` is a day where something qualified and was held back, `INSUFFICIENT_DATA` is too little history to describe anything |

A `vetoed:` line would name a candidate layer 5 refused before the one that spoke. **There
is not one in this file, or anywhere in the run**: layer 5 vetoed nothing across eleven
persona years.

---

## Why these three lives

Eleven personas ran. These three are the corners of the space rather than three samples
from the middle of it.

| persona | opens | Pulses spoken | Pulse silence | why it is here |
|---|---|---|---|---|
| **Queue hoarder** | 365 | 228 | 37 percent | the talkative extreme. Far more arrives than leaves, so something is always true about the queue. This is the year with the most sentences in it, and the one where a repeated shape has the most chances to show |
| **Abandoning** | 153 | 18 | 88 percent | the difficult year. Daily for two months, then about one day in three for the remaining ten. This is the mirror test: read it imagining it is your own worst year. It is also the only one of the three that reaches `hardStretch` |
| **Balanced across four** | 365 | 9 | 97 percent | the silent extreme. Four areas, none dominant, a person using the app exactly as intended, who hears from the Pulse nine times in a year. Everything the phase found about silence is visible in this one life |

**Why not the others.** `heavySingleArea` and `fastCompleter` both land at 61 percent
silence and read close to `queueHoarder` with a narrower family spread. `highFocus` and
`lowFocus` differ from each other in which focus families fire rather than in voice.
`brandNew` is three weeks long. `longDormantRevival` is worth one week of anybody's
attention, the week it comes back, and `MASTER_BUILD_PROMPT.md` 14b.4 is already measured
there. `sporadic` sits between the first two. `acceptsEveryPlan` is the non compliance test
of section 12, it passes, and its year reads like the queue hoarder's with plans quietly
attached; its two `hardStretch` lines are in Appendix A with the rest.

---

## The four things worth watching for

1. **A claim repeating, which is not the same as a line repeating.** Phase 9 made the
   benches deep, so the wording changes. Watch `balancedAcrossFour` hear `balancedWeek` on
   351 of 365 Momentum openings and take `personalBest` as its report headline in 50 weeks
   of 52. Fifty different sentences saying the same thing about the same week is a
   different defect from one sentence fifty times, and depth cannot touch it.
2. **The mirror test, on `abandoning`.** Every line there is written to be read by somebody
   having a bad year. `CLARITY_LOGIC_ENGINE.md` 11.3 asks that any sentence which would make
   a reader defensive is wrong, and that decline and neglect families are read twice.
3. **The silence.** It is left in on purpose. A day the app said nothing is part of what
   this reads like, and there are 2,067 of them across the eleven.
4. **`hardStretch`.** Flagged by name wherever it appears. 6.4 says that if any line in it
   reads as consolation rather than observation, the family is removed rather than
   rewritten. That is the one judgment in this file with a stated consequence.

---

---

## Before you read: which third of the corpus is in this file

**Every surface in this app speaks in exactly one register, and it is not the same one for
each surface.** `RegisterChoice.preference` returns the registers to try in order and
`Realizer.realize` takes the first one with a line it can fill. It never chooses among
registers. So for any one surface, one register speaks and the others are reached only when
the first has nothing fillable, which across eleven simulated years happened **once**.

| surface | firings in the run | registers reached |
|---|---|---|
| Pulse | 1,081 | plain 1,080, observational 1, reflective **0** |
| Momentum headline | 3,133 | reflective 3,133 |
| Areas banner | 2,461 | reflective 2,461 |
| report headline | 440 | plain 440 |
| report observation | 1,583 | observational 637, neutral agent 546, editorial 390, plain 10 |
| report pattern | 397 | plain 397 |

Two different things are behind that, and they have different owners.

**The Pulse column is the instrument, not the app.** 7.4 gives the Pulse a time of day rule:
before the evening it prefers the plain voice, at or after it the reflective one.
`PulseSchedule.dayAt` implements it by ending the window at the day boundary before 17:00
and at the moment of asking after it, and `ClarityEngine.momentOf` reads a window ending at
midnight as morning. **The simulator opens every persona at 07:00 and builds only the
yesterday window**, so every one of the 1,081 Pulses in this run is a morning one and the
reflective bench is never asked for. In the shipped app a person who opens after five in the
afternoon gets the reflective voice. So roughly 312 reflective and 427 observational
statement lines of volume 1 are **unmeasured here rather than dead**, and this file shows the
plain third of the Pulse corpus.

**The Momentum and banner column is the app.** `RegisterChoice.preference` has no branch for
those two purposes, so the list is the bare fallback and its head, reflective, always wins.
There is no hour of the day and no fact that reaches the other two. **487 of volume 3's 748
register tagged lines are in registers the app cannot ask for**, including all 63 of the
neutral agent lines, which is the same finding the pass that grew volume 3 recorded from the
other side when it found `weekQuiet` unable to speak at all.

**And it is why a hot bench is not the size 11.1 says it is.** The realizer chooses inside
one register of one stage, and 11.1 sizes the stage. `quietDay` stage 1 holds 67 lines; the
21 plain ones are the whole bench the chooser ever saw across 212 firings, and the run
reached all 21 of them. Measured where the choice is actually made, phase 9's hot Pulse
benches are 16 to 29 lines, which is **warm** by 11.1's own table. That is not an argument
for writing more. It is an argument that one of two things has to change, and neither is
authoring: either the sizing target is stated per register, or the realizer chooses across
the registers it can fill rather than stopping at the first.

---

## 1. Queue hoarder, the talkative year

Far more arrives than leaves. Something is true about this queue every single day, so
this is the year with the most sentences in it: 228 Pulses spoken out of 365, against a
run average of 34 percent spoken. `accumulation` alone speaks 80 times, 78 of them at
stage 3, which is the top of that ladder. **If a shape repeats anywhere, it repeats
here first.** Watch the run of `accumulation` stage 3 across the autumn in particular:
seventy eight firings of one stage of one family is the hardest thing a bench of sixty
lines is asked to do anywhere in this app.

```text
persona: queueHoarder, Queue hoarder
why:     Far more arrives than leaves. Feeds accumulation, queuePressure and growingQueues.
span:    365 simulated days, 365 opens, 1655 events, 1404 engine invocations
```

### January 2026

28 days on screen, 20 Pulses spoken, 8 Pulse days silent, 85 sentences in all.

```text
2026-01-04  [momentum]  SILENT (INSUFFICIENT_DATA)
2026-01-04  [banner]  weekStarting / stage 1 / reflective / bn.start.11
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > Barely begun.
2026-01-04  [pulse]  SILENT (INSUFFICIENT_DATA)

2026-01-05  [momentum]  firstDays / stage 1 / reflective / mo.first.22
  rule:  momentum.firstDays
  fired: fewer than fourteen days since install, at least one event, so this is a beginning rather than an empty app
  > There are not many days here to look at.
2026-01-05  [banner]  weekStarting / stage 1 / reflective / bn.start.15
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > Nothing settled yet.
2026-01-05  [pulse]  spread / stage 1 / plain / spread.s1.10
  rule:  pulse.spread.s1
  fired: exactly three areas had events, no area is above half the window, the window holds at least 5 events, so a share is describing something real
  facts: areaCount=3 [rollup.areasWithEvents], n=7 [window.totalEvents]
  > three areas, seven moves, no clear center.
  > Was that a good shape for the day?
  ? Felt manageable | Felt stretched

2026-01-06  [momentum]  firstDays / stage 1 / reflective / mo.first.30
  rule:  momentum.firstDays
  fired: fewer than fourteen days since install, at least one event, so this is a beginning rather than an empty app
  > Early, with the rest of it ahead.
2026-01-06  [banner]  weekMixed / stage 1 / reflective / bn.mixed.73
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week that piled up in one place.
2026-01-06  [pulse]  freshStart / stage 1 / plain / freshstart.s1.02
  rule:  pulse.freshStart.s1
  fired: the area is one this window began or reopened, the area has at least 1 events in the window, so naming it cannot be a phantom claim
  facts: areaName=Home
  > You added Home.
  > Is this a big one?
  ? A big one | A small one

2026-01-07  [momentum]  firstDays / stage 1 / reflective / mo.first.16
  rule:  momentum.firstDays
  fired: fewer than fourteen days since install, at least one event, so this is a beginning rather than an empty app
  > A start, nothing more.
2026-01-07  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-01-07  [pulse]  accumulation / stage 1 / plain / accumulation.s1.16
  rule:  pulse.accumulation.s1
  fired: additions exceed completions by two or three, at least two things were added, so no count renders as zero
  facts: n=2 [window.additions]
  > The lists took in two things.
  > Capturing, or collecting?
  ? All worth writing down | Some were not

2026-01-08  [momentum]  firstDays / stage 1 / reflective / mo.first.24
  rule:  momentum.firstDays
  fired: fewer than fourteen days since install, at least one event, so this is a beginning rather than an empty app
  > Only a few days of this so far.
2026-01-08  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-01-08  [pulse]  freshStart / stage 1 / plain / freshstart.s1.09
  rule:  pulse.freshStart.s1
  fired: the area is one this window began or reopened, the area has at least 1 events in the window, so naming it cannot be a phantom claim
  facts: areaName=Work
  > Work went from empty to active.
  > A commitment, or a trial?
  ? A big one | A small one

2026-01-09  [momentum]  firstDays / stage 1 / reflective / mo.first.19
  rule:  momentum.firstDays
  fired: fewer than fourteen days since install, at least one event, so this is a beginning rather than an empty app
  > A short history so far.
2026-01-09  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-01-09  [pulse]  persistence / stage 1 / plain / persistence.s1.22
  rule:  pulse.persistence.s1
  fired: the active item is three to five days old, the area holding the item has at least 1 events in the window
  facts: ageDays=4 [item.itemAgeDays:itm-2]
  > Same front, four days on.
  > Moving slowly, or not moving?
  ? It needs the time | It needs a nudge

2026-01-10  [momentum]  firstDays / stage 1 / reflective / mo.first.21
  rule:  momentum.firstDays
  fired: fewer than fourteen days since install, at least one event, so this is a beginning rather than an empty app
  > Early, before much of it has happened.
2026-01-10  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-01-10  [pulse]  freshStart / stage 1 / plain / freshstart.s1.01
  rule:  pulse.freshStart.s1
  fired: the area is one this window began or reopened, the area has at least 1 events in the window, so naming it cannot be a phantom claim
  facts: areaName=Someday
  > Someday has its first item.
  > A commitment, or a trial?
  ? A big one | A small one

2026-01-11  [momentum]  firstDays / stage 1 / reflective / mo.first.07
  rule:  momentum.firstDays
  fired: fewer than fourteen days since install, at least one event, so this is a beginning rather than an empty app
  > The beginning of a picture.
2026-01-11  [banner]  weekStarting / stage 1 / reflective / bn.start.17
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > The first days of it.
2026-01-11  [pulse]  persistence / stage 1 / plain / persistence.s1.18
  rule:  pulse.persistence.s1
  fired: the active item is three to five days old, the area holding the item has at least 1 events in the window
  facts: ageDays=5 [item.itemAgeDays:itm-3]
  > five days at the front.
  > Moving slowly, or not moving?
  ? In progress | Not started
2026-01-11  [report headline]  balanced / stage 1 / plain / hd.bal.25
  rule:  report.headline.balanced
  fired: three or more areas had activity, no area dominates, the window holds at least 5 events, so a share is describing something real
  > Many, not one.
2026-01-11  [report observation]  areaBalance / stage 1 / editorial / ob.bal.l08
  rule:  report.observation.areaBalance
  fired: three or more areas had activity, no area holds half the week, the window holds at least 6 events, so a share is describing something real
  > A week can be wide or it can be deep.
2026-01-11  [report observation]  selfReportVsData / stage 1 / editorial / ob.srvd.l40
  rule:  report.observation.selfReportVsData
  fired: the item the user answered about is still active, the area holding the item has at least 1 events in the window, there is a stored answer to quote
  facts: priorLabel=It needs the time
  > It needs the time was true when you said it. Whether it still is, only you can say.
2026-01-11  [report observation]  firstMilestone / stage 1 / observational / ob.first.l05
  rule:  report.observation.firstMilestone
  fired: something happened this window for the first time ever, the window has activity behind the milestone
  > Every area had something active at once, for the first time.
2026-01-11  [report observation]  completionSplit / stage 1 / observational / ob.split.l31
  rule:  report.observation.completionSplit
  fired: three or more pulses were answered in the window, there are completions for the answers to be about
  facts: n=5 [pulse.answeredInWindow], priorCount=2 [pulse.labelCountInWindow:A small one], priorLabel=A small one
  > 5 answers this week, 2 of them A small one, and the week had completions in it.

2026-01-12  [momentum]  firstDays / stage 1 / reflective / mo.first.25
  rule:  momentum.firstDays
  fired: fewer than fourteen days since install, at least one event, so this is a beginning rather than an empty app
  > The opening days.
2026-01-12  [banner]  weekStarting / stage 1 / reflective / bn.start.26
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > A week still near its own beginning.
2026-01-12  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-01-13  [momentum]  firstDays / stage 1 / reflective / mo.first.20
  rule:  momentum.firstDays
  fired: fewer than fourteen days since install, at least one event, so this is a beginning rather than an empty app
  > The part of it that has happened so far.
2026-01-13  [banner]  weekMixed / stage 1 / reflective / bn.mixed.26
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Most of it in one area, so far.
2026-01-13  [pulse]  accumulation / stage 1 / plain / accumulation.s1.19
  rule:  pulse.accumulation.s1
  fired: additions exceed completions by two or three, at least two things were added, so no count renders as zero
  > The queues are heavier than they were.
  > Room for these, or already full?
  ? Noticing | Postponing

2026-01-14  [momentum]  firstDays / stage 1 / reflective / mo.first.23
  rule:  momentum.firstDays
  fired: fewer than fourteen days since install, at least one event, so this is a beginning rather than an empty app
  > A beginning with a few days behind it.
2026-01-14  [banner]  weekMixed / stage 1 / reflective / bn.mixed.19
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week gathered mostly in one place.
2026-01-14  [pulse]  concentration / stage 1 / plain / concentration.s1.27
  rule:  pulse.concentration.s1
  fired: the area holds seventy to eighty four percent of the window, the area has at least 4 events in the window, so its share is describing something real, the window holds at least 4 events, so a share is describing something real
  facts: pct=85 [area.areaShare:home]
  > 85 percent of yesterday in one area, the rest elsewhere.
  > Chosen, or where the day went?
  ? It was enough | Not quite enough

2026-01-15  [momentum]  firstDays / stage 1 / reflective / mo.first.04
  rule:  momentum.firstDays
  fired: fewer than fourteen days since install, at least one event, so this is a beginning rather than an empty app
  > Just getting going.
2026-01-15  [banner]  weekMixed / stage 1 / reflective / bn.mixed.18
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Unequal, with the week still going.
2026-01-15  [pulse]  persistence / stage 2 / plain / persistence.s2.27
  rule:  pulse.persistence.s2
  fired: the active item is six to thirteen days old, the area holding the item has at least 1 events in the window
  facts: areaName=Work
  > Work has not changed hands.
  > Deep work, or stuck?
  ? Too big | Just slow

2026-01-16  [momentum]  firstDays / stage 1 / reflective / mo.first.22
  rule:  momentum.firstDays
  fired: fewer than fourteen days since install, at least one event, so this is a beginning rather than an empty app
  > There are not many days here to look at.
2026-01-16  [banner]  weekMixed / stage 1 / reflective / bn.mixed.17
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Spread unevenly, so far.
2026-01-16  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-01-17  [momentum]  firstDays / stage 1 / reflective / mo.first.20
  rule:  momentum.firstDays
  fired: fewer than fourteen days since install, at least one event, so this is a beginning rather than an empty app
  > The part of it that has happened so far.
2026-01-17  [banner]  weekMixed / stage 1 / reflective / bn.mixed.20
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Concentrated, with the rest thinner.
2026-01-17  [pulse]  quietDay / stage 1 / plain / quietday.s1.17
  rule:  pulse.quietDay.s1
  fired: fewer than two events in the window, per the family trigger, there is at least a day of history, so this is a quiet day and not an empty install
  > Yesterday passed quietly.
  > Anything happening away from here?
  ? Fine | Busy | Struggling

2026-01-18  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.26
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Several places, none of them all of it.
2026-01-18  [banner]  weekStarting / stage 1 / reflective / bn.start.21
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > Early, before anything settles.
2026-01-18  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-01-18  [report headline]  comeback / stage 1 / plain / hd.back.11
  rule:  report.headline.comeback
  fired: this area returned after a long dormancy, the area has at least 1 events in the window, so naming it cannot be a phantom claim
  facts: ageDays=5 [area.areaDormancyDays:someday], areaName=Someday
  > 5 days on, Someday moved.
2026-01-18  [report observation]  areaRevival / stage 1 / observational / ob.rev.l27
  rule:  report.observation.areaRevival
  fired: this area returned after a dormancy, the area has at least 1 events in the window, so naming it cannot be a phantom claim
  facts: ageDays=5 [area.areaDormancyDays:someday], areaName=Someday
  > There was a gap of 5 days in Someday. The week that ended today closed it.
2026-01-18  [report observation]  completionSplit / stage 1 / observational / ob.split.l12
  rule:  report.observation.completionSplit
  fired: three or more pulses were answered in the window, there are completions for the answers to be about
  facts: m=3 [pulse.positiveInWindow]
  > 3 of your answers were positive.
2026-01-18  [report observation]  dayShape / stage 1 / observational / ob.day.l06
  rule:  report.observation.dayShape
  fired: one day of the week stands out and can be named, that day holds a third of the week or more
  facts: dayName=Tuesday, n=13 [window.busiestDayCount]
  > Your busiest day was Tuesday, at 13 events.
2026-01-18  [report observation]  intakeVsOutput / stage 2 / neutral_agent / ob.flow.s2.l29
  rule:  report.observation.intakeVsOutput.s2
  fired: intake exceeds output by five or more, there are additions to count
  > A collecting week.

2026-01-19  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.20
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > No single thing has held it.
2026-01-19  [banner]  weekStarting / stage 1 / reflective / bn.start.19
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > Little to read this early.
2026-01-19  [pulse]  accumulation / stage 3 / plain / accumulation.s3.19
  rule:  pulse.accumulation.s3.weeks
  fired: the queues have grown three weeks running, additions still exceed completions by two or more this window, at least two things were added, so no count renders as zero
  > Three weeks, all of them longer.
  > Worth a clear out?
  ? Building up | Avoiding

2026-01-20  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.18
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Level, more or less.
2026-01-20  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-01-20  [pulse]  persistence / stage 2 / plain / persistence.s2.19
  rule:  pulse.persistence.s2
  fired: the active item is six to thirteen days old, the area holding the item has at least 1 events in the window
  facts: ageDays=6 [item.itemAgeDays:itm-18]
  > six days and holding.
  > Still the right thing?
  ? Underway | Untouched

2026-01-21  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.22
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Two weeks that went several ways.
2026-01-21  [banner]  weekMixed / stage 1 / reflective / bn.mixed.11
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Weighted rather than spread.
2026-01-21  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-01-22  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.16
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > No one place took it all.
2026-01-22  [banner]  weekMixed / stage 1 / reflective / bn.mixed.12
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week that leans one way.
2026-01-22  [pulse]  rebalance / stage 1 / plain / rebalance.s1.16
  rule:  pulse.rebalance.s1
  fired: the area had been still five to thirteen days before it moved, the area has at least 1 events in the window, so naming it cannot be a phantom claim
  facts: ageDays=7 [area.areaDormancyDays:someday], areaName=Someday
  > Yesterday was the first day in seven days that Someday moved.
  > Was the pause deliberate?
  ? Still mine | Not sure any more

2026-01-23  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.16
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > No one place took it all.
2026-01-23  [banner]  weekMixed / stage 1 / reflective / bn.mixed.26
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Most of it in one area, so far.
2026-01-23  [pulse]  accumulation / stage 3 / plain / accumulation.s3.15
  rule:  pulse.accumulation.s3.weeks
  fired: the queues have grown three weeks running, additions still exceed completions by two or more this window, at least two things were added, so no count renders as zero
  > The lists ended the week longer.
  > Too much coming in, or not enough going out?
  ? It does not bother me | It does

2026-01-24  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.17
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Room for more than one thing.
2026-01-24  [banner]  weekMixed / stage 1 / reflective / bn.mixed.21
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week with more of it in one area.
2026-01-24  [pulse]  persistence / stage 2 / plain / persistence.s2.28
  rule:  pulse.persistence.s2
  fired: the active item is six to thirteen days old, the area holding the item has at least 1 events in the window
  facts: itemTitle=Cancel the old subscription
  > Cancel the old subscription has stayed put.
  > One task, or several?
  ? Making progress | Not really

2026-01-25  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.26
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Several places, none of them all of it.
2026-01-25  [banner]  weekStarting / stage 1 / reflective / bn.start.24
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > The shape of this week is still ahead.
2026-01-25  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-01-25  [report headline]  comeback / stage 1 / plain / hd.back.08
  rule:  report.headline.comeback
  fired: this area returned after a long dormancy, the area has at least 1 events in the window, so naming it cannot be a phantom claim
  facts: areaName=Someday
  > Someday woke up.
2026-01-25  [report observation]  selfReportVsData / stage 1 / editorial / ob.srvd.l05
  rule:  report.observation.selfReportVsData
  fired: the item the user answered about is still active, the area holding the item has at least 1 events in the window, there is a stored answer to quote
  facts: priorLabel=Making progress
  > You described that as Making progress. The week went on to prove you right.
2026-01-25  [report observation]  areaRevival / stage 1 / observational / ob.rev.l07
  rule:  report.observation.areaRevival
  fired: this area returned after a dormancy, the area has at least 1 events in the window, so naming it cannot be a phantom claim
  facts: areaName=Someday
  > Someday moved this week.
2026-01-25  [report observation]  completionSplit / stage 1 / observational / ob.split.l26
  rule:  report.observation.completionSplit
  fired: three or more pulses were answered in the window, there are completions for the answers to be about
  facts: n=3 [pulse.answeredInWindow]
  > You answered 3 pulses and something got finished in the same seven days.
2026-01-25  [report observation]  intakeVsOutput / stage 2 / neutral_agent / ob.flow.s2.l34
  rule:  report.observation.intakeVsOutput.s2
  fired: intake exceeds output by five or more, there are additions to count
  facts: k=12 [rollup.queueGrowth]
  > The queues hold 12 more things than they held on the first day of the week.

2026-01-26  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.23
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Nothing has held more than half.
2026-01-26  [banner]  weekStarting / stage 1 / reflective / bn.start.16
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > A week still mostly ahead.
2026-01-26  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-01-27  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.24
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight that kept moving around.
2026-01-27  [banner]  weekMixed / stage 1 / reflective / bn.mixed.21
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week with more of it in one area.
2026-01-27  [pulse]  concentration / stage 1 / plain / concentration.s1.02
  rule:  pulse.concentration.s1
  fired: the area holds seventy to eighty four percent of the window, the area has at least 4 events in the window, so its share is describing something real, the window holds at least 4 events, so a share is describing something real
  facts: areaName=Home, pct=83 [area.areaShare:home]
  > Home took 83 percent of what you did.
  > Concentrated, or narrow?
  ? It was enough | Not quite enough

2026-01-28  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.17
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Room for more than one thing.
2026-01-28  [banner]  weekMixed / stage 1 / reflective / bn.mixed.16
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week with a heavier side.
2026-01-28  [pulse]  persistence / stage 1 / plain / persistence.s1.20
  rule:  pulse.persistence.s1
  fired: the active item is three to five days old, the area holding the item has at least 1 events in the window
  facts: itemTitle=Repot the balcony plants
  > Repot the balcony plants sits where it sat.
  > Moving slowly, or not moving?
  ? Deep work | Stuck

2026-01-29  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.29
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Several things at once, for two weeks.
2026-01-29  [banner]  weekMixed / stage 1 / reflective / bn.mixed.12
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week that leans one way.
2026-01-29  [pulse]  accumulation / stage 3 / plain / accumulation.s3.20
  rule:  pulse.accumulation.s3.weeks
  fired: the queues have grown three weeks running, additions still exceed completions by two or more this window, at least two things were added, so no count renders as zero
  > More waiting than yesterday.
  > Growing, or gathering?
  ? Building up | Avoiding

2026-01-30  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.25
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Two weeks across several areas.
2026-01-30  [banner]  weekMixed / stage 1 / reflective / bn.mixed.25
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Uneven going.
2026-01-30  [pulse]  persistence / stage 1 / plain / persistence.s1.27
  rule:  pulse.persistence.s1
  fired: the active item is three to five days old, the area holding the item has at least 1 events in the window
  facts: ageDays=3 [item.itemAgeDays:itm-34], areaName=Home
  > three days of Home on one item.
  > Moving slowly, or not moving?
  ? Deep work | Stuck

2026-01-31  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.27
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight that went in several directions.
2026-01-31  [banner]  weekStrong / stage 1 / reflective / bn.strong.26
  rule:  banner.weekStrong
  fired: completions are clearly above the recent weekly average, there are completions to count, there are earlier weeks to be above
  > A week ahead of the ones before it.
2026-01-31  [pulse]  SILENT (NO_RULE_QUALIFIED)
```

### February 2026

28 days on screen, 19 Pulses spoken, 9 Pulse days silent, 89 sentences in all.

```text
2026-02-01  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.21
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Across, rather than down.
2026-02-01  [banner]  weekStarting / stage 1 / reflective / bn.start.17
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > The first days of it.
2026-02-01  [pulse]  quietDay / stage 1 / plain / quietday.s1.03
  rule:  pulse.quietDay.s1
  fired: fewer than two events in the window, per the family trigger, there is at least a day of history, so this is a quiet day and not an empty install
  > One thing happened yesterday.
  > Needed, or not?
  ? Deliberate | Life happened | Too much
2026-02-01  [report headline]  personalBest / stage 1 / plain / hd.best.33
  rule:  report.headline.personalBest
  fired: no earlier week strictly beats this one, this week reaches the personal best, there is more than one week to be best of, there are completions, so no count renders as zero
  facts: n=6 [window.completions]
  > 6 finished. A ceiling.
2026-02-01  [report observation]  personalBest / stage 1 / editorial / ob.best.l18
  rule:  report.observation.personalBest
  fired: no earlier week strictly beats this one, this week reaches the personal best, there is more than one week to be best of, there are completions, so no count renders as zero
  facts: n=6 [window.completions]
  > 6 completions is the largest number this record has held, and a number is all it is.
2026-02-01  [report observation]  persistentItem / stage 1 / neutral_agent / ob.pers.l20
  rule:  report.observation.persistentItem.high
  fired: the item has been active fourteen days or more, the area holding the item has at least 1 events in the window, there is a median completion time to compare against
  facts: ageDays=25 [item.itemAgeDays:itm-6]
  > 3 weeks with the same thing in front.
2026-02-01  [report observation]  areaBalance / stage 1 / editorial / ob.bal.l07
  rule:  report.observation.areaBalance
  fired: three or more areas had activity, no area holds half the week, the window holds at least 6 events, so a share is describing something real
  > The week had no owner.
2026-02-01  [report observation]  selfReportVsData / stage 1 / observational / ob.srvd.l24
  rule:  report.observation.selfReportVsData
  fired: the item the user answered about is still active, the area holding the item has at least 1 events in the window, there is a stored answer to quote
  facts: itemTitle=Draft the quarterly summary, priorLabel=Stuck
  > You said Stuck within the last month. Draft the quarterly summary is still active.
2026-02-01  [report pattern]  reportedVsActual / stage 1 / plain / pt.rva.51
  rule:  report.pattern.reportedVsActual
  fired: there are several stored answers to compare against, one family has been answered about more than twice, there are at least three weeks of snapshots, without which no pattern may fire
  > Three answers about one kind of moment is not a verdict on anything, and it is a record that the moment kept happening.

2026-02-02  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.28
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight that did not settle in one place.
2026-02-02  [banner]  weekMixed / stage 1 / reflective / bn.mixed.20
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Concentrated, with the rest thinner.
2026-02-02  [pulse]  persistence / stage 2 / plain / persistence.s2.37
  rule:  pulse.persistence.s2
  fired: the active item is six to thirteen days old, the area holding the item has at least 1 events in the window
  facts: ageDays=6 [item.itemAgeDays:itm-34], areaName=Home, itemTitle=Draft the quarterly summary
  > Home has been about Draft the quarterly summary for six days now.
  > Waiting on you, or on something else?
  ? Worth the front | Worth a swap

2026-02-03  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.26
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Several places, none of them all of it.
2026-02-03  [banner]  weekMixed / stage 1 / reflective / bn.mixed.15
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Tilted, so far.
2026-02-03  [pulse]  accumulation / stage 3 / plain / accumulation.s3.16
  rule:  pulse.accumulation.s3.weeks
  fired: the queues have grown three weeks running, additions still exceed completions by two or more this window, at least two things were added, so no count renders as zero
  > The queues rose again.
  > Growing, or gathering?
  ? It does not bother me | It does

2026-02-04  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.23
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Nothing has held more than half.
2026-02-04  [banner]  weekMixed / stage 1 / reflective / bn.mixed.15
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Tilted, so far.
2026-02-04  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-02-05  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.25
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Two weeks across several areas.
2026-02-05  [banner]  weekMixed / stage 1 / reflective / bn.mixed.21
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week with more of it in one area.
2026-02-05  [pulse]  persistence / stage 3 / plain / persistence.s3.25
  rule:  pulse.persistence.s3
  fired: the active item is fourteen to twenty nine days old, the area holding the item has at least 1 events in the window, something else was completed, so the comparison has a number
  > The front is unchanged.
  > Is it waiting on you, or on something else?
  ? Waiting on a person | Waiting on a day

2026-02-06  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.30
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Spread, without any one place holding it.
2026-02-06  [banner]  weekMixed / stage 1 / reflective / bn.mixed.21
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week with more of it in one area.
2026-02-06  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-02-07  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.17
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Room for more than one thing.
2026-02-07  [banner]  weekMixed / stage 1 / reflective / bn.mixed.23
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week that did not spread evenly.
2026-02-07  [pulse]  accumulation / stage 3 / plain / accumulation.s3.14
  rule:  pulse.accumulation.s3.weeks
  fired: the queues have grown three weeks running, additions still exceed completions by two or more this window, at least two things were added, so no count renders as zero
  > The queues are longer again.
  > A season, or how it is now?
  ? On purpose | By default

2026-02-08  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.21
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Across, rather than down.
2026-02-08  [banner]  weekStarting / stage 1 / reflective / bn.start.26
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > A week still near its own beginning.
2026-02-08  [pulse]  persistence / stage 1 / plain / persistence.s1.31
  rule:  pulse.persistence.s1
  fired: the active item is three to five days old, the area holding the item has at least 1 events in the window
  facts: ageDays=3 [item.itemAgeDays:itm-14], areaName=Work, itemTitle=Draft the quarterly summary
  > Draft the quarterly summary went to the front of Work three days ago.
  > Still first, or not any more?
  ? Taking time | Taking space
2026-02-08  [report headline]  comeback / stage 1 / plain / hd.back.57
  rule:  report.headline.comeback
  fired: this area returned after a long dormancy, the area has at least 1 events in the window, so naming it cannot be a phantom claim
  facts: ageDays=6 [area.areaDormancyDays:someday], areaName=Someday
  > 6 days gone, Someday back.
2026-02-08  [report observation]  areaRevival / stage 1 / observational / ob.rev.l12
  rule:  report.observation.areaRevival
  fired: this area returned after a dormancy, the area has at least 1 events in the window, so naming it cannot be a phantom claim
  facts: areaName=Someday
  > The quiet in Someday ended this week.
2026-02-08  [report observation]  completionSplit / stage 1 / observational / ob.split.l32
  rule:  report.observation.completionSplit
  fired: three or more pulses were answered in the window, there are completions for the answers to be about
  facts: k=2 [pulse.flaggedInWindow], m=1 [pulse.positiveInWindow], n=3 [pulse.answeredInWindow]
  > Of the 3 answers you gave this week, 1 took the positive option and 2 took the other.
2026-02-08  [report observation]  intakeVsOutput / stage 2 / neutral_agent / ob.flow.s2.l24
  rule:  report.observation.intakeVsOutput.s2
  fired: intake exceeds output by five or more, there are additions to count
  > Intake ran ahead of output.
2026-02-08  [report observation]  persistentItem / stage 1 / observational / ob.pers.l36
  rule:  report.observation.persistentItem.low
  fired: the item has been active seven to thirteen days, the area holding the item has at least 1 events in the window
  facts: areaName=Home, itemTitle=Draft the quarterly summary
  > Nothing in the Home queue has come past Draft the quarterly summary.
2026-02-08  [report pattern]  growingQueues / stage 1 / plain / pt.grow.36
  rule:  report.pattern.growingQueues
  fired: the queues have grown three weeks running, there are at least three weeks of snapshots, without which no pattern may fire
  facts: m=33 [history.weekQueueSizeAgo:1], n=43 [history.weekQueueSizeAgo:0]
  > 43 now, up from 33.

2026-02-09  [momentum]  steadyStretch / stage 1 / reflective / mo.steady.08
  rule:  momentum.steadyStretch
  fired: active on nine or more of the last fourteen days, there are fourteen days to have been active across, the app has existed for the 14 days being described
  > The last two weeks have been fairly even.
2026-02-09  [banner]  weekStarting / stage 1 / reflective / bn.start.17
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > The first days of it.
2026-02-09  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-02-10  [momentum]  steadyStretch / stage 1 / reflective / mo.steady.25
  rule:  momentum.steadyStretch
  fired: active on nine or more of the last fourteen days, there are fourteen days to have been active across, the app has existed for the 14 days being described
  > Level going, across a fortnight.
2026-02-10  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-02-10  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-02-11  [momentum]  steadyStretch / stage 1 / reflective / mo.steady.21
  rule:  momentum.steadyStretch
  fired: active on nine or more of the last fourteen days, there are fourteen days to have been active across, the app has existed for the 14 days being described
  > Few empty days in it.
2026-02-11  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-02-11  [pulse]  accumulation / stage 3 / plain / accumulation.s3.26
  rule:  pulse.accumulation.s3.weeks
  fired: the queues have grown three weeks running, additions still exceed completions by two or more this window, at least two things were added, so no count renders as zero
  > The lists are longer than they were three weeks ago.
  > Does the list still describe the work?
  ? Keep it all | Cut some

2026-02-12  [momentum]  comeback / stage 1 / reflective / mo.come.23
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > What paused is going again.
2026-02-12  [banner]  weekMixed / stage 1 / reflective / bn.mixed.16
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week with a heavier side.
2026-02-12  [pulse]  persistence / stage 2 / plain / persistence.s2.04
  rule:  pulse.persistence.s2
  fired: the active item is six to thirteen days old, the area holding the item has at least 1 events in the window
  facts: areaName=Work, itemTitle=Draft the quarterly summary
  > Draft the quarterly summary is going into its second week in Work.
  > Deep work, or stuck?
  ? Underway | Untouched

2026-02-13  [momentum]  comeback / stage 1 / reflective / mo.come.17
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > The pause is over.
2026-02-13  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-02-13  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-02-14  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.31
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > More than one thing at a time.
2026-02-14  [banner]  weekMixed / stage 1 / reflective / bn.mixed.25
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Uneven going.
2026-02-14  [pulse]  concentration / stage 2 / plain / concentration.s2.18
  rule:  pulse.concentration.s2
  fired: the area holds eighty five to ninety four percent of the window, the area has at least 4 events in the window, so its share is describing something real, the window holds at least 4 events, so a share is describing something real
  facts: areaName=Home, m=11 [window.totalEvents], n=10 [area.areaEvents:home]
  > Home took 10 of the 11 things that happened.
  > On purpose, or it just happened?
  ? Focus | Gravity

2026-02-15  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.33
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight that went around rather than down.
2026-02-15  [banner]  weekStarting / stage 1 / reflective / bn.start.12
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > Early, with most of it ahead.
2026-02-15  [pulse]  persistence / stage 2 / plain / persistence.s2.34
  rule:  pulse.persistence.s2
  fired: the active item is six to thirteen days old, the area holding the item has at least 1 events in the window
  facts: ageDays=10 [item.itemAgeDays:itm-14], itemTitle=Draft the quarterly summary
  > 10 days have passed and Draft the quarterly summary is where it started.
  > Would you pick it again today?
  ? Moving | Parked
2026-02-15  [report headline]  mostActiveSince / stage 1 / plain / hd.since.08
  rule:  report.headline.mostActiveSince
  fired: there is an earlier week that strictly beats this one to name, this week beats the one before it, there are three weeks to reach back across
  facts: sinceRef=2026-01-25
  > The strongest since January.
2026-02-15  [report observation]  persistentItem / stage 1 / neutral_agent / ob.pers.l22
  rule:  report.observation.persistentItem.high
  fired: the item has been active fourteen days or more, the area holding the item has at least 1 events in the window, there is a median completion time to compare against
  facts: ageDays=39 [item.itemAgeDays:itm-6], areaName=Someday
  > The same item has been first in Someday for 1 month.
2026-02-15  [report observation]  mostActiveSince / stage 1 / editorial / ob.since.l45
  rule:  report.observation.mostActiveSince
  fired: there is an earlier week that strictly beats this one to name, this week beats the one before it, there are three weeks to reach back across
  facts: sinceRef=2026-01-25
  > Two facts sit side by side. This week beat last week. January still beats this week.
2026-02-15  [report observation]  selfReportVsData / stage 1 / editorial / ob.srvd.l39
  rule:  report.observation.selfReportVsData
  fired: the item the user answered about is still active, the area holding the item has at least 1 events in the window, there is a stored answer to quote
  facts: ageDays=10 [item.itemAgeDays:itm-14]
  > 10 days of being active, with a sentence from you about it.
2026-02-15  [report observation]  areaRevival / stage 1 / observational / ob.rev.l13
  rule:  report.observation.areaRevival
  fired: this area returned after a dormancy, the area has at least 1 events in the window, so naming it cannot be a phantom claim
  facts: areaName=Someday
  > Someday has events again.
2026-02-15  [report pattern]  reportedVsActual / stage 1 / plain / pt.rva.55
  rule:  report.pattern.reportedVsActual
  fired: there are several stored answers to compare against, one family has been answered about more than twice, there are at least three weeks of snapshots, without which no pattern may fire
  > The same kind of question has come around three times, which says more about the situation than about the answers.

2026-02-16  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.28
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight that did not settle in one place.
2026-02-16  [banner]  weekStarting / stage 1 / reflective / bn.start.25
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > A week at the point where nothing is decided.
2026-02-16  [pulse]  accumulation / stage 3 / plain / accumulation.s3.12
  rule:  pulse.accumulation.s3.weeks
  fired: the queues have grown three weeks running, additions still exceed completions by two or more this window, at least two things were added, so no count renders as zero
  > Three weeks of growth.
  > Worth a clear out?
  ? I look at all of it | I do not

2026-02-17  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.07
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Evenly spread, across two weeks.
2026-02-17  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-02-17  [pulse]  persistence / stage 4 / plain / persistence.s4.47
  rule:  pulse.persistence.s4
  fired: the active item is thirty days old or more, the area holding the item has at least 1 events in the window, this item genuinely holds the longest ever active record, without which stage 4's historical language would be false
  facts: ageDays=41 [item.itemAgeDays:itm-6]
  > one month, and the same name.
  > Still the work, or is it something else now?
  ? Waiting on a decision | Waiting on time

2026-02-18  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.04
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A wide fortnight.
2026-02-18  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-02-18  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-02-19  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.19
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Spread out, over fourteen days.
2026-02-19  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-02-19  [pulse]  quietDay / stage 1 / plain / quietday.s1.21
  rule:  pulse.quietDay.s1
  fired: fewer than two events in the window, per the family trigger, there is at least a day of history, so this is a quiet day and not an empty install
  > Yesterday left things as they were.
  > How did it feel?
  ? A day off | Off the app | Underwater

2026-02-20  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.23
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Nothing has held more than half.
2026-02-20  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-02-20  [pulse]  accumulation / stage 3 / plain / accumulation.s3.21
  rule:  pulse.accumulation.s3.weeks
  fired: the queues have grown three weeks running, additions still exceed completions by two or more this window, at least two things were added, so no count renders as zero
  > The queues rose three weeks running.
  > Has this been useful, or just growing?
  ? Fine for now | Worth a clear out

2026-02-21  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.23
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Nothing has held more than half.
2026-02-21  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-02-21  [pulse]  persistence / stage 2 / plain / persistence.s2.22
  rule:  pulse.persistence.s2
  fired: the active item is six to thirteen days old, the area holding the item has at least 1 events in the window
  facts: itemTitle=Patch the bike tire
  > Patch the bike tire has been here a while.
  > Wanted, or just kept?
  ? By choice | By default

2026-02-22  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.18
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Level, more or less.
2026-02-22  [banner]  weekStarting / stage 1 / reflective / bn.start.10
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > The start of a week.
2026-02-22  [pulse]  SILENT (NO_RULE_QUALIFIED)
2026-02-22  [report headline]  balanced / stage 1 / plain / hd.bal.05
  rule:  report.headline.balanced
  fired: three or more areas had activity, no area dominates, the window holds at least 5 events, so a share is describing something real
  > Spread across the board.
2026-02-22  [report observation]  areaBalance / stage 1 / editorial / ob.bal.l10
  rule:  report.observation.areaBalance
  fired: three or more areas had activity, no area holds half the week, the window holds at least 6 events, so a share is describing something real
  > Nothing here was the main thing.
2026-02-22  [report observation]  selfReportVsData / stage 1 / editorial / ob.srvd.l32
  rule:  report.observation.selfReportVsData
  fired: the item the user answered about is still active, the area holding the item has at least 1 events in the window, there is a stored answer to quote
  facts: priorLabel=By default
  > You said By default. The app has kept it exactly as you said it.
2026-02-22  [report observation]  intakeVsOutput / stage 2 / neutral_agent / ob.flow.s2.l30
  rule:  report.observation.intakeVsOutput.s2
  fired: intake exceeds output by five or more, there are additions to count
  > More in than out.
2026-02-22  [report observation]  persistentItem / stage 1 / observational / ob.pers.l41
  rule:  report.observation.persistentItem.low
  fired: the item has been active seven to thirteen days, the area holding the item has at least 1 events in the window
  facts: areaName=Home, itemTitle=Patch the bike tire
  > The rest of Home moved this week and Patch the bike tire stayed still.
2026-02-22  [report pattern]  growingQueues / stage 1 / plain / pt.grow.12
  rule:  report.pattern.growingQueues
  fired: the queues have grown three weeks running, there are at least three weeks of snapshots, without which no pattern may fire
  > The total is higher than last week, and last week was higher than the one before it.

2026-02-23  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.13
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Wide, across two weeks.
2026-02-23  [banner]  weekStarting / stage 1 / reflective / bn.start.09
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > Early yet.
2026-02-23  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-02-24  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.32
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight with its weight spread out.
2026-02-24  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-02-24  [pulse]  accumulation / stage 3 / plain / accumulation.s3.13
  rule:  pulse.accumulation.s3.weeks
  fired: the queues have grown three weeks running, additions still exceed completions by two or more this window, at least two things were added, so no count renders as zero
  > Longer every week for three weeks.
  > Is there a plan for the older ones?
  ? Fine for now | Worth a clear out

2026-02-25  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.12
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Nothing has dominated.
2026-02-25  [banner]  weekMixed / stage 1 / reflective / bn.mixed.19
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week gathered mostly in one place.
2026-02-25  [pulse]  persistence / stage 2 / plain / persistence.s2.01
  rule:  pulse.persistence.s2
  fired: the active item is six to thirteen days old, the area holding the item has at least 1 events in the window
  facts: ageDays=11 [item.itemAgeDays:itm-48], itemTitle=Patch the bike tire
  > Still Patch the bike tire. 11 days now.
  > Moving inside, or not at all?
  ? Deep work | Stuck

2026-02-26  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.31
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > More than one thing at a time.
2026-02-26  [banner]  weekMixed / stage 1 / reflective / bn.mixed.10
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Lopsided, for now.
2026-02-26  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-02-27  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.12
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Nothing has dominated.
2026-02-27  [banner]  weekMixed / stage 1 / reflective / bn.mixed.16
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week with a heavier side.
2026-02-27  [pulse]  rebalance / stage 1 / plain / rebalance.s1.12
  rule:  pulse.rebalance.s1
  fired: the area had been still five to thirteen days before it moved, the area has at least 1 events in the window, so naming it cannot be a phantom claim
  facts: ageDays=9 [area.areaDormancyDays:someday], areaName=Someday
  > Someday had its first activity in nine days.
  > Was it out of mind, or out of time?
  ? The pause was deliberate | It got away from me

2026-02-28  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.27
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight that went in several directions.
2026-02-28  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-02-28  [pulse]  accumulation / stage 3 / plain / accumulation.s3.31
  rule:  pulse.accumulation.s3.weeks
  fired: the queues have grown three weeks running, additions still exceed completions by two or more this window, at least two things were added, so no count renders as zero
  > The lists took on more than they gave up yesterday.
  > Too much coming in, or not enough going out?
  ? Collecting | Storing
```

### March 2026

31 days on screen, 17 Pulses spoken, 14 Pulse days silent, 98 sentences in all.

```text
2026-03-01  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.17
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Room for more than one thing.
2026-03-01  [banner]  weekStarting / stage 1 / reflective / bn.start.23
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > Nothing here has had time to become a shape.
2026-03-01  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-03-01  [report headline]  balanced / stage 1 / plain / hd.bal.47
  rule:  report.headline.balanced
  fired: three or more areas had activity, no area dominates, the window holds at least 5 events, so a share is describing something real
  > A wide spread.
2026-03-01  [report observation]  areaBalance / stage 1 / editorial / ob.bal.l17
  rule:  report.observation.areaBalance
  fired: three or more areas had activity, no area holds half the week, the window holds at least 6 events, so a share is describing something real
  facts: areaCount=3 [rollup.areasWithEvents]
  > A week that touches 3 areas and gives none of them half has no single subject.
2026-03-01  [report observation]  areaRevival / stage 1 / observational / ob.rev.l11
  rule:  report.observation.areaRevival
  fired: this area returned after a dormancy, the area has at least 1 events in the window, so naming it cannot be a phantom claim
  facts: ageDays=9 [area.areaDormancyDays:someday], areaName=Someday
  > Someday has something in it again after 9 days.
2026-03-01  [report observation]  intakeVsOutput / stage 2 / neutral_agent / ob.flow.s2.l27
  rule:  report.observation.intakeVsOutput.s2
  fired: intake exceeds output by five or more, there are additions to count
  > What arrived outnumbered what left.
2026-03-01  [report observation]  queuePressure / stage 1 / neutral_agent / ob.qp.l26
  rule:  report.observation.queuePressure
  fired: at least one area's queue grew, the queues hold three or more things to count
  facts: n=75 [rollup.queueTotal]
  > 75 things wait behind the items that are moving.
2026-03-01  [report pattern]  reportedVsActual / stage 1 / plain / pt.rva.54
  rule:  report.pattern.reportedVsActual
  fired: there are several stored answers to compare against, one family has been answered about more than twice, there are at least three weeks of snapshots, without which no pattern may fire
  > An answer is a reading taken at a moment, and this app now holds five of yours or more.

2026-03-02  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.27
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight that went in several directions.
2026-03-02  [banner]  weekStarting / stage 1 / reflective / bn.start.19
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > Little to read this early.
2026-03-02  [pulse]  quietDay / stage 1 / plain / quietday.s1.14
  rule:  pulse.quietDay.s1
  fired: fewer than two events in the window, per the family trigger, there is at least a day of history, so this is a quiet day and not an empty install
  > Nothing finished yesterday.
  > Off the app, or off entirely?
  ? Needed the day | Out doing things | Could not start

2026-03-03  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.16
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > No one place took it all.
2026-03-03  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-03-03  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-03-04  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.27
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight that went in several directions.
2026-03-04  [banner]  weekMixed / stage 1 / reflective / bn.mixed.16
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week with a heavier side.
2026-03-04  [pulse]  accumulation / stage 3 / plain / accumulation.s3.17
  rule:  pulse.accumulation.s3.weeks
  fired: the queues have grown three weeks running, additions still exceed completions by two or more this window, at least two things were added, so no count renders as zero
  > The queues grew again yesterday.
  > Is this collecting, or is this storing?
  ? Collecting | Storing

2026-03-05  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.26
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Several places, none of them all of it.
2026-03-05  [banner]  weekMixed / stage 1 / reflective / bn.mixed.11
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Weighted rather than spread.
2026-03-05  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-03-06  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.26
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Several places, none of them all of it.
2026-03-06  [banner]  weekMixed / stage 1 / reflective / bn.mixed.22
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Some places busier than others.
2026-03-06  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-03-07  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.23
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Nothing has held more than half.
2026-03-07  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-03-07  [pulse]  persistence / stage 4 / plain / persistence.s4.67
  rule:  pulse.persistence.s4
  fired: the active item is thirty days old or more, the area holding the item has at least 1 events in the window, this item genuinely holds the longest ever active record, without which stage 4's historical language would be false
  > Months, one item, one area.
  > Still the right place for it?
  ? Still the work | Something else now

2026-03-08  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.27
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight that went in several directions.
2026-03-08  [banner]  weekStarting / stage 1 / reflective / bn.start.24
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > The shape of this week is still ahead.
2026-03-08  [pulse]  SILENT (NO_RULE_QUALIFIED)
2026-03-08  [report headline]  mostActiveSince / stage 1 / plain / hd.since.12
  rule:  report.headline.mostActiveSince
  fired: there is an earlier week that strictly beats this one to name, this week beats the one before it, there are three weeks to reach back across
  facts: sinceRef=2026-02-08
  > February was the last week above this.
2026-03-08  [report observation]  mostActiveSince / stage 1 / editorial / ob.since.l41
  rule:  report.observation.mostActiveSince
  fired: there is an earlier week that strictly beats this one to name, this week beats the one before it, there are three weeks to reach back across
  facts: sinceRef=2026-02-08
  > A week can be the biggest since February and still not be the biggest.
2026-03-08  [report observation]  areaRevival / stage 1 / observational / ob.rev.l29
  rule:  report.observation.areaRevival
  fired: this area returned after a dormancy, the area has at least 1 events in the window, so naming it cannot be a phantom claim
  facts: ageDays=7 [area.areaDormancyDays:someday], areaName=Someday
  > Nothing moved in Someday for 7 days. The week that just ended had something in it.
2026-03-08  [report observation]  intakeVsOutput / stage 2 / neutral_agent / ob.flow.s2.l12
  rule:  report.observation.intakeVsOutput.s2
  fired: intake exceeds output by five or more, there are additions to count
  facts: k=11 [rollup.queueGrowth]
  > 11 more things are waiting than were on Sunday.
2026-03-08  [report observation]  queuePressure / stage 1 / neutral_agent / ob.qp.l08
  rule:  report.observation.queuePressure
  fired: at least one area's queue grew, the queues hold three or more things to count
  facts: m=75 [rollup.queueTotalAtStart], n=86 [rollup.queueTotal]
  > 86 waiting, 75 a week ago.
2026-03-08  [report pattern]  growingQueues / stage 1 / plain / pt.grow.05
  rule:  report.pattern.growingQueues
  fired: the queues have grown three weeks running, there are at least three weeks of snapshots, without which no pattern may fire
  > More arrives than leaves, and has for three weeks.

2026-03-09  [momentum]  comeback / stage 1 / reflective / mo.come.30
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > A pause with something after it.
2026-03-09  [banner]  weekStarting / stage 1 / reflective / bn.start.12
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > Early, with most of it ahead.
2026-03-09  [pulse]  quietDay / stage 1 / plain / quietday.s1.25
  rule:  pulse.quietDay.s1
  fired: fewer than two events in the window, per the family trigger, there is at least a day of history, so this is a quiet day and not an empty install
  > No items moved in any area yesterday.
  > Away on purpose?
  ? Chosen | Circumstance | Overloaded

2026-03-10  [momentum]  comeback / stage 1 / reflective / mo.come.17
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > The pause is over.
2026-03-10  [banner]  weekMixed / stage 1 / reflective / bn.mixed.11
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Weighted rather than spread.
2026-03-10  [pulse]  accumulation / stage 3 / plain / accumulation.s3.24
  rule:  pulse.accumulation.s3.weeks
  fired: the queues have grown three weeks running, additions still exceed completions by two or more this window, at least two things were added, so no count renders as zero
  > The queues were shorter three weeks ago.
  > Would you write all of these again?
  ? It describes the work | It has drifted

2026-03-11  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.26
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Several places, none of them all of it.
2026-03-11  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-03-11  [pulse]  persistence / stage 1 / plain / persistence.s1.17
  rule:  pulse.persistence.s1
  fired: the active item is three to five days old, the area holding the item has at least 1 events in the window
  facts: itemTitle=Prep the interview questions
  > Prep the interview questions, unmoved.
  > Underway, or untouched?
  ? In progress | Not started

2026-03-12  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.31
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > More than one thing at a time.
2026-03-12  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-03-12  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-03-13  [momentum]  comeback / stage 1 / reflective / mo.come.15
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > A gap, then movement.
2026-03-13  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-03-13  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-03-14  [momentum]  comeback / stage 1 / reflective / mo.come.16
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > Movement, after a pause.
2026-03-14  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-03-14  [pulse]  accumulation / stage 3 / plain / accumulation.s3.29
  rule:  pulse.accumulation.s3.weeks
  fired: the queues have grown three weeks running, additions still exceed completions by two or more this window, at least two things were added, so no count renders as zero
  > The lists are longer this morning than yesterday morning.
  > Is anything on there done already?
  ? Still a plan | A pile

2026-03-15  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.23
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Nothing has held more than half.
2026-03-15  [banner]  weekStarting / stage 1 / reflective / bn.start.24
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > The shape of this week is still ahead.
2026-03-15  [pulse]  persistence / stage 4 / plain / persistence.s4.15
  rule:  pulse.persistence.s4
  fired: the active item is thirty days old or more, the area holding the item has at least 1 events in the window, this item genuinely holds the longest ever active record, without which stage 4's historical language would be false
  facts: itemTitle=Label the storage boxes
  > Still Label the storage boxes, months on.
  > Would you add it today?
  ? Waiting on me | Waiting on someone else
2026-03-15  [report headline]  balanced / stage 1 / plain / hd.bal.29
  rule:  report.headline.balanced
  fired: three or more areas had activity, no area dominates, the window holds at least 5 events, so a share is describing something real
  > Nothing above half.
2026-03-15  [report observation]  areaBalance / stage 1 / editorial / ob.bal.l15
  rule:  report.observation.areaBalance
  fired: three or more areas had activity, no area holds half the week, the window holds at least 6 events, so a share is describing something real
  facts: areaCount=3 [rollup.areasWithEvents]
  > Attention went to 3 places and settled in none of them.
2026-03-15  [report observation]  selfReportVsData / stage 1 / editorial / ob.srvd.l44
  rule:  report.observation.selfReportVsData
  fired: the item the user answered about is still active, the area holding the item has at least 1 events in the window, there is a stored answer to quote
  > What you said is a fact about you and the days are a fact about the item.
2026-03-15  [report observation]  steadyPace / stage 1 / observational / ob.stead.l38
  rule:  report.observation.steadyPace
  fired: the last three weeks sit inside a narrow band, there are at least three weeks of snapshots, without which no pattern may fire, the band is a pace rather than a run of empty weeks
  > Three weeks running, the total has stayed put.
2026-03-15  [report observation]  areaRevival / stage 1 / observational / ob.rev.l08
  rule:  report.observation.areaRevival
  fired: this area returned after a dormancy, the area has at least 1 events in the window, so naming it cannot be a phantom claim
  facts: ageDays=5 [area.areaDormancyDays:someday], areaName=Someday
  > Someday had been quiet for 5 days.
2026-03-15  [report pattern]  consistentRhythm / stage 1 / plain / pt.rhy.42
  rule:  report.pattern.consistentRhythm
  fired: four weeks inside a narrow band, there are four weeks to see it across, the band is a rhythm rather than a run of empty weeks
  > The four weeks are the same week, near enough.

2026-03-16  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.19
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Spread out, over fourteen days.
2026-03-16  [banner]  weekStarting / stage 1 / reflective / bn.start.14
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > Too early for much of a reading.
2026-03-16  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-03-17  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.31
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > More than one thing at a time.
2026-03-17  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-03-17  [pulse]  quietDay / stage 1 / plain / quietday.s1.24
  rule:  pulse.quietDay.s1
  fired: fewer than two events in the window, per the family trigger, there is at least a day of history, so this is a quiet day and not an empty install
  > Yesterday came and went with nothing logged.
  > Quiet by choice?
  ? Recharging | Busy elsewhere | Overwhelmed

2026-03-18  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.10
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Broad rather than deep.
2026-03-18  [banner]  weekMixed / stage 1 / reflective / bn.mixed.24
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > The weight of the week sits in one place.
2026-03-18  [pulse]  accumulation / stage 3 / plain / accumulation.s3.27
  rule:  pulse.accumulation.s3.weeks
  fired: the queues have grown three weeks running, additions still exceed completions by two or more this window, at least two things were added, so no count renders as zero
  > Week on week, the queues have grown.
  > Growing on purpose, or growing by default?
  ? A season | How it is now

2026-03-19  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.33
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight that went around rather than down.
2026-03-19  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-03-19  [pulse]  persistence / stage 2 / plain / persistence.s2.20
  rule:  pulse.persistence.s2
  fired: the active item is six to thirteen days old, the area holding the item has at least 1 events in the window
  facts: itemTitle=Prep the interview questions
  > Still Prep the interview questions at the front.
  > Underway, or untouched?
  ? Still going | Stuck on it

2026-03-20  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.15
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Breadth, more than depth.
2026-03-20  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-03-20  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-03-21  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.33
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight that went around rather than down.
2026-03-21  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-03-21  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-03-22  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.15
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Breadth, more than depth.
2026-03-22  [banner]  weekStarting / stage 1 / reflective / bn.start.15
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > Nothing settled yet.
2026-03-22  [pulse]  accumulation / stage 3 / plain / accumulation.s3.30
  rule:  pulse.accumulation.s3.weeks
  fired: the queues have grown three weeks running, additions still exceed completions by two or more this window, at least two things were added, so no count renders as zero
  > Yesterday left the queues longer than it found them.
  > Does the size of it bother you?
  ? They belong | Time to cut some
2026-03-22  [report headline]  balanced / stage 1 / plain / hd.bal.13
  rule:  report.headline.balanced
  fired: three or more areas had activity, no area dominates, the window holds at least 5 events, so a share is describing something real
  > Three areas at least.
2026-03-22  [report observation]  areaBalance / stage 1 / editorial / ob.bal.l04
  rule:  report.observation.areaBalance
  fired: three or more areas had activity, no area holds half the week, the window holds at least 6 events, so a share is describing something real
  > No single area owned this week.
2026-03-22  [report observation]  selfReportVsData / stage 1 / editorial / ob.srvd.l43
  rule:  report.observation.selfReportVsData
  fired: the item the user answered about is still active, the area holding the item has at least 1 events in the window, there is a stored answer to quote
  > Some answers age and some do not. Nothing here says which this is.
2026-03-22  [report observation]  steadyPace / stage 1 / observational / ob.stead.l53
  rule:  report.observation.steadyPace
  fired: the last three weeks sit inside a narrow band, there are at least three weeks of snapshots, without which no pattern may fire, the band is a pace rather than a run of empty weeks
  > No week broke away.
2026-03-22  [report observation]  intakeVsOutput / stage 2 / neutral_agent / ob.flow.s2.l17
  rule:  report.observation.intakeVsOutput.s2
  fired: intake exceeds output by five or more, there are additions to count
  > More came in than went out, by five or more.
2026-03-22  [report pattern]  reportedVsActual / stage 1 / plain / pt.rva.20
  rule:  report.pattern.reportedVsActual
  fired: there are several stored answers to compare against, one family has been answered about more than twice, there are at least three weeks of snapshots, without which no pattern may fire
  > The app kept asking.

2026-03-23  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.22
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Two weeks that went several ways.
2026-03-23  [banner]  weekStarting / stage 1 / reflective / bn.start.21
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > Early, before anything settles.
2026-03-23  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-03-24  [momentum]  comeback / stage 1 / reflective / mo.come.18
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > Under way again.
2026-03-24  [banner]  weekMixed / stage 1 / reflective / bn.mixed.20
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Concentrated, with the rest thinner.
2026-03-24  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-03-25  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.14
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight in several places.
2026-03-25  [banner]  weekStrong / stage 1 / reflective / bn.strong.15
  rule:  banner.weekStrong
  fired: completions are clearly above the recent weekly average, there are completions to count, there are earlier weeks to be above
  > Plenty already, with the week still open.
2026-03-25  [pulse]  concentration / stage 2 / plain / concentration.s2.29
  rule:  pulse.concentration.s2
  fired: the area holds eighty five to ninety four percent of the window, the area has at least 4 events in the window, so its share is describing something real, the window holds at least 4 events, so a share is describing something real
  facts: areaName=Home, pct=91 [area.areaShare:home]
  > Home covered 91 percent of the day.
  > Chosen, or where the day went?
  ? I would call it focus | I would not

2026-03-26  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.17
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Room for more than one thing.
2026-03-26  [banner]  weekStrong / stage 1 / reflective / bn.strong.13
  rule:  banner.weekStrong
  fired: completions are clearly above the recent weekly average, there are completions to count, there are earlier weeks to be above
  > A week with more in it than most.
2026-03-26  [pulse]  persistence / stage 4 / plain / persistence.s4.56
  rule:  pulse.persistence.s4
  fired: the active item is thirty days old or more, the area holding the item has at least 1 events in the window, this item genuinely holds the longest ever active record, without which stage 4's historical language would be false
  > Months, and one item.
  > Is it one thing, or many?
  ? Live | Just listed

2026-03-27  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.27
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight that went in several directions.
2026-03-27  [banner]  weekStrong / stage 1 / reflective / bn.strong.26
  rule:  banner.weekStrong
  fired: completions are clearly above the recent weekly average, there are completions to count, there are earlier weeks to be above
  > A week ahead of the ones before it.
2026-03-27  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-03-28  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.22
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Two weeks that went several ways.
2026-03-28  [banner]  weekStrong / stage 1 / reflective / bn.strong.22
  rule:  banner.weekStrong
  fired: completions are clearly above the recent weekly average, there are completions to count, there are earlier weeks to be above
  > More movement than the weeks before it.
2026-03-28  [pulse]  accumulation / stage 3 / plain / accumulation.s3.18
  rule:  pulse.accumulation.s3.weeks
  fired: the queues have grown three weeks running, additions still exceed completions by two or more this window, at least two things were added, so no count renders as zero
  > More is on the lists now.
  > Worth a clear out?
  ? Building up | Avoiding

2026-03-29  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.19
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Spread out, over fourteen days.
2026-03-29  [banner]  weekStarting / stage 1 / reflective / bn.start.24
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > The shape of this week is still ahead.
2026-03-29  [pulse]  persistence / stage 1 / plain / persistence.s1.30
  rule:  pulse.persistence.s1
  fired: the active item is three to five days old, the area holding the item has at least 1 events in the window
  facts: ageDays=4 [item.itemAgeDays:itm-66], areaName=Home, itemTitle=Patch the bike tire
  > Patch the bike tire has held the front of Home for four days.
  > Is it big, or is it just sitting there?
  ? It needs the time | It needs a nudge
2026-03-29  [report headline]  personalBest / stage 1 / plain / hd.best.11
  rule:  report.headline.personalBest
  fired: no earlier week strictly beats this one, this week reaches the personal best, there is more than one week to be best of, there are completions, so no count renders as zero
  > Nothing earlier beat it.
2026-03-29  [report observation]  personalBest / stage 1 / editorial / ob.best.l13
  rule:  report.observation.personalBest
  fired: no earlier week strictly beats this one, this week reaches the personal best, there is more than one week to be best of, there are completions, so no count renders as zero
  > A high week is a fact.
2026-03-29  [report observation]  persistentItem / stage 1 / neutral_agent / ob.pers.l26
  rule:  report.observation.persistentItem.high
  fired: the item has been active fourteen days or more, the area holding the item has at least 1 events in the window, there is a median completion time to compare against
  facts: ageDays=81 [item.itemAgeDays:itm-6], itemTitle=Label the storage boxes, n=19 [item.itemQueueBehind:itm-6]
  > Label the storage boxes has 19 things in the queue behind it. The item itself has been in place 2 months.
2026-03-29  [report observation]  areaBalance / stage 1 / editorial / ob.bal.l16
  rule:  report.observation.areaBalance
  fired: three or more areas had activity, no area holds half the week, the window holds at least 6 events, so a share is describing something real
  > Spread is not the same as balance. This week was spread.
2026-03-29  [report observation]  areaRevival / stage 1 / observational / ob.rev.l18
  rule:  report.observation.areaRevival
  fired: this area returned after a dormancy, the area has at least 1 events in the window, so naming it cannot be a phantom claim
  facts: areaName=Someday
  > Someday came off the quiet list.
2026-03-29  [report pattern]  growingQueues / stage 1 / plain / pt.grow.60
  rule:  report.pattern.growingQueues
  fired: the queues have grown three weeks running, there are at least three weeks of snapshots, without which no pattern may fire
  > Two weekly readings in a row came in above the one before them, which is the whole of what this section is saying.

2026-03-30  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.32
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight with its weight spread out.
2026-03-30  [banner]  weekStarting / stage 1 / reflective / bn.start.26
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > A week still near its own beginning.
2026-03-30  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-03-31  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.27
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight that went in several directions.
2026-03-31  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-03-31  [pulse]  quietDay / stage 1 / plain / quietday.s1.26
  rule:  pulse.quietDay.s1
  fired: fewer than two events in the window, per the family trigger, there is at least a day of history, so this is a quiet day and not an empty install
  > Nothing completed, nothing added, nothing swapped.
  > What kind of quiet was it?
  ? Deliberate | Life happened | Too much
```

### April 2026

30 days on screen, 21 Pulses spoken, 9 Pulse days silent, 95 sentences in all.

```text
2026-04-01  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.18
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Level, more or less.
2026-04-01  [banner]  weekMixed / stage 1 / reflective / bn.mixed.20
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Concentrated, with the rest thinner.
2026-04-01  [pulse]  accumulation / stage 3 / plain / accumulation.s3.28
  rule:  pulse.accumulation.s3.weeks
  fired: the queues have grown three weeks running, additions still exceed completions by two or more this window, at least two things were added, so no count renders as zero
  facts: n=2 [window.additions]
  > Yesterday put another two things on the lists.
  > Is this collecting, or is this storing?
  ? Useful | Just growing

2026-04-02  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.15
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Breadth, more than depth.
2026-04-02  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-04-02  [pulse]  persistence / stage 4 / plain / persistence.s4.20
  rule:  pulse.persistence.s4
  fired: the active item is thirty days old or more, the area holding the item has at least 1 events in the window, this item genuinely holds the longest ever active record, without which stage 4's historical language would be false
  facts: areaName=Someday, itemTitle=Label the storage boxes
  > Someday still means Label the storage boxes.
  > Is the title still right?
  ? One thing | Many

2026-04-03  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.10
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Broad rather than deep.
2026-04-03  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-04-03  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-04-04  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.33
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight that went around rather than down.
2026-04-04  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-04-04  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-04-05  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.16
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > No one place took it all.
2026-04-05  [banner]  weekStarting / stage 1 / reflective / bn.start.11
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > Barely begun.
2026-04-05  [pulse]  accumulation / stage 3 / plain / accumulation.s3.23
  rule:  pulse.accumulation.s3.weeks
  fired: the queues have grown three weeks running, additions still exceed completions by two or more this window, at least two things were added, so no count renders as zero
  > Your lists have grown in each of the last three weeks.
  > Room for more, or full?
  ? Keep it all | Cut some
2026-04-05  [report headline]  comeback / stage 1 / plain / hd.back.58
  rule:  report.headline.comeback
  fired: this area returned after a long dormancy, the area has at least 1 events in the window, so naming it cannot be a phantom claim
  facts: areaName=Someday
  > Someday put something in the week.
2026-04-05  [report observation]  selfReportVsData / stage 1 / editorial / ob.srvd.l38
  rule:  report.observation.selfReportVsData
  fired: the item the user answered about is still active, the area holding the item has at least 1 events in the window, there is a stored answer to quote
  facts: itemTitle=Patch the bike tire
  > The record holds your reading of Patch the bike tire and the fact that it is still active. It has no third thing.
2026-04-05  [report observation]  areaRevival / stage 1 / observational / ob.rev.l17
  rule:  report.observation.areaRevival
  fired: this area returned after a dormancy, the area has at least 1 events in the window, so naming it cannot be a phantom claim
  facts: ageDays=7 [area.areaDormancyDays:someday], areaName=Someday
  > Nothing had happened in Someday for 7 days. This week did.
2026-04-05  [report observation]  completionSplit / stage 1 / observational / ob.split.l09
  rule:  report.observation.completionSplit
  fired: three or more pulses were answered in the window, there are completions for the answers to be about
  facts: n=3 [pulse.answeredInWindow]
  > 3 answers came back this week.
2026-04-05  [report observation]  intakeVsOutput / stage 2 / neutral_agent / ob.flow.s2.l26
  rule:  report.observation.intakeVsOutput.s2
  fired: intake exceeds output by five or more, there are additions to count
  > The queues took on more than they let go.
2026-04-05  [report pattern]  reportedVsActual / stage 1 / plain / pt.rva.58
  rule:  report.pattern.reportedVsActual
  fired: there are several stored answers to compare against, one family has been answered about more than twice, there are at least three weeks of snapshots, without which no pattern may fire
  > Nothing here compares what you said against what happened. It records that you said something, three times.

2026-04-06  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.12
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Nothing has dominated.
2026-04-06  [banner]  weekStarting / stage 1 / reflective / bn.start.23
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > Nothing here has had time to become a shape.
2026-04-06  [pulse]  persistence / stage 2 / plain / persistence.s2.36
  rule:  pulse.persistence.s2
  fired: the active item is six to thirteen days old, the area holding the item has at least 1 events in the window
  facts: ageDays=12 [item.itemAgeDays:itm-66], areaName=Home
  > The front of Home has not changed in 12 days.
  > Deep work, or stuck?
  ? Close | Not close

2026-04-07  [momentum]  comeback / stage 1 / reflective / mo.come.22
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > Movement where there was none.
2026-04-07  [banner]  weekMixed / stage 1 / reflective / bn.mixed.12
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week that leans one way.
2026-04-07  [pulse]  quietDay / stage 1 / plain / quietday.s1.13
  rule:  pulse.quietDay.s1
  fired: fewer than two events in the window, per the family trigger, there is at least a day of history, so this is a quiet day and not an empty install
  > Yesterday was still.
  > Where did the day go?
  ? A pause | Away | Stalled

2026-04-08  [momentum]  comeback / stage 1 / reflective / mo.come.29
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > Something on the far side of a gap.
2026-04-08  [banner]  weekMixed / stage 1 / reflective / bn.mixed.21
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week with more of it in one area.
2026-04-08  [pulse]  persistence / stage 1 / plain / persistence.s1.26
  rule:  pulse.persistence.s1
  fired: the active item is three to five days old, the area holding the item has at least 1 events in the window
  facts: itemTitle=Read the design chapter
  > Read the design chapter, day after day.
  > Underway, or untouched?
  ? Still going | Stuck on it

2026-04-09  [momentum]  comeback / stage 1 / reflective / mo.come.20
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > The quiet gave way.
2026-04-09  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-04-09  [pulse]  accumulation / stage 3 / plain / accumulation.s3.22
  rule:  pulse.accumulation.s3.weeks
  fired: the queues have grown three weeks running, additions still exceed completions by two or more this window, at least two things were added, so no count renders as zero
  > The queues have been growing for three weeks.
  > Keep it all, or cut some?
  ? A season | How it is now

2026-04-10  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.21
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Across, rather than down.
2026-04-10  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-04-10  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-04-11  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.30
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Spread, without any one place holding it.
2026-04-11  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-04-11  [pulse]  persistence / stage 2 / plain / persistence.s2.39
  rule:  pulse.persistence.s2
  fired: the active item is six to thirteen days old, the area holding the item has at least 1 events in the window
  facts: ageDays=6 [item.itemAgeDays:itm-24], itemTitle=Read the design chapter
  > Read the design chapter arrived, and six days later it is still the front.
  > Is it the size, or the start?
  ? One task | Several

2026-04-12  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.16
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > No one place took it all.
2026-04-12  [banner]  weekStarting / stage 1 / reflective / bn.start.10
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > The start of a week.
2026-04-12  [pulse]  rebalance / stage 1 / plain / rebalance.s1.01
  rule:  pulse.rebalance.s1
  fired: the area had been still five to thirteen days before it moved, the area has at least 1 events in the window, so naming it cannot be a phantom claim
  facts: ageDays=7 [area.areaDormancyDays:someday], areaName=Someday
  > Someday moved again after seven days.
  > Is this area on a slower clock than the rest?
  ? Picking it up | Just checking in
2026-04-12  [report headline]  balanced / stage 1 / plain / hd.bal.03
  rule:  report.headline.balanced
  fired: three or more areas had activity, no area dominates, the window holds at least 5 events, so a share is describing something real
  > Everything got some.
2026-04-12  [report observation]  areaBalance / stage 1 / editorial / ob.bal.l12
  rule:  report.observation.areaBalance
  fired: three or more areas had activity, no area holds half the week, the window holds at least 6 events, so a share is describing something real
  > The week had a shape without a subject.
2026-04-12  [report observation]  selfReportVsData / stage 1 / editorial / ob.srvd.l35
  rule:  report.observation.selfReportVsData
  fired: the item the user answered about is still active, the area holding the item has at least 1 events in the window, there is a stored answer to quote
  facts: itemTitle=Read the design chapter
  > What you said about Read the design chapter may still be true. This is only a note that you said it.
2026-04-12  [report observation]  areaRevival / stage 1 / observational / ob.rev.l02
  rule:  report.observation.areaRevival
  fired: this area returned after a dormancy, the area has at least 1 events in the window, so naming it cannot be a phantom claim
  facts: areaName=Someday
  > Someday came back this week.
2026-04-12  [report observation]  intakeVsOutput / stage 2 / neutral_agent / ob.flow.s2.l23
  rule:  report.observation.intakeVsOutput.s2
  fired: intake exceeds output by five or more, there are additions to count
  > The week collected more than it closed.
2026-04-12  [report pattern]  decliningActivity / stage 1 / plain / pt.dec.05
  rule:  report.pattern.decliningActivity
  fired: total activity has fallen three weeks running, there are at least three weeks of snapshots, without which no pattern may fire
  > Three consecutive weeks of less.

2026-04-13  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.19
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Spread out, over fourteen days.
2026-04-13  [banner]  weekStrong / stage 1 / reflective / bn.strong.20
  rule:  banner.weekStrong
  fired: completions are clearly above the recent weekly average, there are completions to count, there are earlier weeks to be above
  > Above your own average.
2026-04-13  [pulse]  concentration / stage 1 / plain / concentration.s1.03
  rule:  pulse.concentration.s1
  fired: the area holds seventy to eighty four percent of the window, the area has at least 4 events in the window, so its share is describing something real, the window holds at least 4 events, so a share is describing something real
  facts: areaName=Home, m=14 [window.totalEvents], n=11 [area.areaEvents:home]
  > 11 of your 14 moves were in Home.
  > A day for one thing, or a day that became one?
  ? Same again tomorrow | Different tomorrow

2026-04-14  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.28
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight that did not settle in one place.
2026-04-14  [banner]  weekStrong / stage 1 / reflective / bn.strong.10
  rule:  banner.weekStrong
  fired: completions are clearly above the recent weekly average, there are completions to count, there are earlier weeks to be above
  > A full week, and not over.
2026-04-14  [pulse]  accumulation / stage 3 / plain / accumulation.s3.25
  rule:  pulse.accumulation.s3.weeks
  fired: the queues have grown three weeks running, additions still exceed completions by two or more this window, at least two things were added, so no count renders as zero
  > Each of the last three weeks left the queues longer.
  > Would you miss any of them?
  ? I look at all of it | I do not

2026-04-15  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.04
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A wide fortnight.
2026-04-15  [banner]  weekStrong / stage 1 / reflective / bn.strong.21
  rule:  banner.weekStrong
  fired: completions are clearly above the recent weekly average, there are completions to count, there are earlier weeks to be above
  > More finished here than in most weeks.
2026-04-15  [pulse]  persistence / stage 2 / plain / persistence.s2.03
  rule:  pulse.persistence.s2
  fired: the active item is six to thirteen days old, the area holding the item has at least 1 events in the window
  facts: ageDays=10 [item.itemAgeDays:itm-24], itemTitle=Read the design chapter
  > 10 days on Read the design chapter, and counting.
  > Is it the size, or the start?
  ? Yes, still right | Time to swap

2026-04-16  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.23
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Nothing has held more than half.
2026-04-16  [banner]  weekStrong / stage 1 / reflective / bn.strong.19
  rule:  banner.weekStrong
  fired: completions are clearly above the recent weekly average, there are completions to count, there are earlier weeks to be above
  > Already past what most weeks hold.
2026-04-16  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-04-17  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.24
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight that kept moving around.
2026-04-17  [banner]  weekStrong / stage 1 / reflective / bn.strong.09
  rule:  banner.weekStrong
  fired: completions are clearly above the recent weekly average, there are completions to count, there are earlier weeks to be above
  > More than usual.
2026-04-17  [pulse]  quietDay / stage 1 / plain / quietday.s1.28
  rule:  pulse.quietDay.s1
  fired: fewer than two events in the window, per the family trigger, there is at least a day of history, so this is a quiet day and not an empty install
  > The day went by without touching this app.
  > What kind of quiet was it?
  ? Time off | Time elsewhere | No time at all

2026-04-18  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.17
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Room for more than one thing.
2026-04-18  [banner]  weekStrong / stage 1 / reflective / bn.strong.17
  rule:  banner.weekStrong
  fired: completions are clearly above the recent weekly average, there are completions to count, there are earlier weeks to be above
  > More than the recent weeks have held.
2026-04-18  [pulse]  accumulation / stage 3 / plain / accumulation.s3.12
  rule:  pulse.accumulation.s3.weeks
  fired: the queues have grown three weeks running, additions still exceed completions by two or more this window, at least two things were added, so no count renders as zero
  > Three weeks of growth.
  > Is anything on there done already?
  ? Too much coming in | Not enough going out

2026-04-19  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.23
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Nothing has held more than half.
2026-04-19  [banner]  weekStarting / stage 1 / reflective / bn.start.10
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > The start of a week.
2026-04-19  [pulse]  persistence / stage 4 / plain / persistence.s4.14
  rule:  pulse.persistence.s4
  fired: the active item is thirty days old or more, the area holding the item has at least 1 events in the window, this item genuinely holds the longest ever active record, without which stage 4's historical language would be false
  > Months now.
  > Is it one thing, or many?
  ? Keep going | Rethink it
2026-04-19  [report headline]  balanced / stage 1 / plain / hd.bal.21
  rule:  report.headline.balanced
  fired: three or more areas had activity, no area dominates, the window holds at least 5 events, so a share is describing something real
  > Attention divided.
2026-04-19  [report observation]  persistentItem / stage 1 / neutral_agent / ob.pers.l09
  rule:  report.observation.persistentItem.high
  fired: the item has been active fourteen days or more, the area holding the item has at least 1 events in the window, there is a median completion time to compare against
  facts: itemTitle=Read the design chapter
  > Read the design chapter is where it was.
2026-04-19  [report observation]  areaBalance / stage 1 / editorial / ob.bal.l13
  rule:  report.observation.areaBalance
  fired: three or more areas had activity, no area holds half the week, the window holds at least 6 events, so a share is describing something real
  > Every area got some of the week and none got most of it.
2026-04-19  [report observation]  mostActiveSince / stage 1 / editorial / ob.since.l46
  rule:  report.observation.mostActiveSince
  fired: there is an earlier week that strictly beats this one to name, this week beats the one before it, there are three weeks to reach back across
  facts: sinceRef=2026-03-22
  > The week is not a record and it is the nearest thing to one since March.
2026-04-19  [report observation]  completionSplit / stage 1 / observational / ob.split.l21
  rule:  report.observation.completionSplit
  fired: three or more pulses were answered in the window, there are completions for the answers to be about
  facts: n=6 [pulse.answeredInWindow]
  > The record holds 6 answers.
2026-04-19  [report pattern]  reportedVsActual / stage 1 / plain / pt.rva.08
  rule:  report.pattern.reportedVsActual
  fired: there are several stored answers to compare against, one family has been answered about more than twice, there are at least three weeks of snapshots, without which no pattern may fire
  > The same question, three times.

2026-04-20  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.24
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight that kept moving around.
2026-04-20  [banner]  weekStarting / stage 1 / reflective / bn.start.24
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > The shape of this week is still ahead.
2026-04-20  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-04-21  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.17
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Room for more than one thing.
2026-04-21  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-04-21  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-04-22  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.19
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Spread out, over fourteen days.
2026-04-22  [banner]  weekMixed / stage 1 / reflective / bn.mixed.16
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week with a heavier side.
2026-04-22  [pulse]  quietDay / stage 1 / plain / quietday.s1.11
  rule:  pulse.quietDay.s1
  fired: fewer than two events in the window, per the family trigger, there is at least a day of history, so this is a quiet day and not an empty install
  > No completions yesterday.
  > Needed, or not?
  ? Resting | Elsewhere | Stuck

2026-04-23  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.32
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight with its weight spread out.
2026-04-23  [banner]  weekMixed / stage 1 / reflective / bn.mixed.22
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Some places busier than others.
2026-04-23  [pulse]  accumulation / stage 3 / plain / accumulation.s3.15
  rule:  pulse.accumulation.s3.weeks
  fired: the queues have grown three weeks running, additions still exceed completions by two or more this window, at least two things were added, so no count renders as zero
  > The lists ended the week longer.
  > Does the size of it bother you?
  ? Collecting | Storing

2026-04-24  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.19
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Spread out, over fourteen days.
2026-04-24  [banner]  weekMixed / stage 1 / reflective / bn.mixed.15
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Tilted, so far.
2026-04-24  [pulse]  persistence / stage 2 / plain / persistence.s2.28
  rule:  pulse.persistence.s2
  fired: the active item is six to thirteen days old, the area holding the item has at least 1 events in the window
  facts: itemTitle=Book the dentist
  > Book the dentist has stayed put.
  > Moving, or parked?
  ? Worth the time | Worth a rethink

2026-04-25  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.32
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight with its weight spread out.
2026-04-25  [banner]  weekMixed / stage 1 / reflective / bn.mixed.73
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week that piled up in one place.
2026-04-25  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-04-26  [momentum]  comeback / stage 1 / reflective / mo.come.22
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > Movement where there was none.
2026-04-26  [banner]  weekStarting / stage 1 / reflective / bn.start.25
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > A week at the point where nothing is decided.
2026-04-26  [pulse]  SILENT (NO_RULE_QUALIFIED)
2026-04-26  [report headline]  netInflow / stage 1 / plain / hd.in.04
  rule:  report.headline.netInflow
  fired: additions exceed completions by a clear margin, there are additions to speak of
  > Things arrived.
2026-04-26  [report observation]  intakeVsOutput / stage 2 / neutral_agent / ob.flow.s2.l19
  rule:  report.observation.intakeVsOutput.s2
  fired: intake exceeds output by five or more, there are additions to count
  facts: k=8 [rollup.queueGrowth]
  > The queues are carrying 8 more than they were.
2026-04-26  [report observation]  neglectedArea / stage 1 / neutral_agent / ob.neg.s1.l09
  rule:  report.observation.neglectedArea.s1
  fired: the area has been silent seven to thirteen days, the area has real history, so this is a silence and not a new area
  facts: ageDays=7 [area.areaDaysSinceLastEvent:someday], areaName=Someday
  > 7 days without an event in Someday.
2026-04-26  [report observation]  persistentItem / stage 1 / observational / ob.pers.l32
  rule:  report.observation.persistentItem.low
  fired: the item has been active seven to thirteen days, the area holding the item has at least 1 events in the window
  facts: areaName=Home, n=32 [item.itemQueueBehind:itm-86]
  > 32 things have queued up behind the front of Home.
2026-04-26  [report observation]  queuePressure / stage 1 / neutral_agent / ob.qp.l12
  rule:  report.observation.queuePressure
  fired: at least one area's queue grew, the queues hold three or more things to count
  facts: areaCount=3 [rollup.areasWithQueue]
  > Something waits in 3 areas.
2026-04-26  [report pattern]  growingQueues / stage 1 / plain / pt.grow.26
  rule:  report.pattern.growingQueues
  fired: the queues have grown three weeks running, there are at least three weeks of snapshots, without which no pattern may fire
  > Two increases in a row.

2026-04-27  [momentum]  comeback / stage 1 / reflective / mo.come.14
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > The quiet ended.
2026-04-27  [banner]  weekStarting / stage 1 / reflective / bn.start.25
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > A week at the point where nothing is decided.
2026-04-27  [pulse]  accumulation / stage 3 / plain / accumulation.s3.19
  rule:  pulse.accumulation.s3.weeks
  fired: the queues have grown three weeks running, additions still exceed completions by two or more this window, at least two things were added, so no count renders as zero
  > Three weeks, all of them longer.
  > Room for more, or full?
  ? There is room | It is full

2026-04-28  [momentum]  comeback / stage 1 / reflective / mo.come.07
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > Something restarted.
2026-04-28  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-04-28  [pulse]  persistence / stage 4 / plain / persistence.s4.18
  rule:  pulse.persistence.s4
  fired: the active item is thirty days old or more, the area holding the item has at least 1 events in the window, this item genuinely holds the longest ever active record, without which stage 4's historical language would be false
  facts: ageDays=111 [item.itemAgeDays:itm-6]
  > three months and no finish.
  > Is the title still right?
  ? Keep going | Rethink it

2026-04-29  [momentum]  comeback / stage 1 / reflective / mo.come.29
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > Something on the far side of a gap.
2026-04-29  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-04-29  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-04-30  [momentum]  comeback / stage 1 / reflective / mo.come.23
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > What paused is going again.
2026-04-30  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-04-30  [pulse]  quietDay / stage 1 / plain / quietday.s1.27
  rule:  pulse.quietDay.s1
  fired: fewer than two events in the window, per the family trigger, there is at least a day of history, so this is a quiet day and not an empty install
  > Yesterday has little to describe.
  > Choice, or circumstance?
  ? Rested | Busy elsewhere | Running on empty
```

### May 2026

31 days on screen, 17 Pulses spoken, 14 Pulse days silent, 97 sentences in all.

```text
2026-05-01  [momentum]  steadyStretch / stage 1 / reflective / mo.steady.30
  rule:  momentum.steadyStretch
  fired: active on nine or more of the last fourteen days, there are fourteen days to have been active across, the app has existed for the 14 days being described
  > A stretch with more days in it than gaps.
2026-05-01  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-05-01  [pulse]  accumulation / stage 3 / plain / accumulation.s3.20
  rule:  pulse.accumulation.s3.weeks
  fired: the queues have grown three weeks running, additions still exceed completions by two or more this window, at least two things were added, so no count renders as zero
  > More waiting than yesterday.
  > Would you write all of these again?
  ? Useful | Just growing

2026-05-02  [momentum]  comeback / stage 1 / reflective / mo.come.30
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > A pause with something after it.
2026-05-02  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-05-02  [pulse]  persistence / stage 2 / plain / persistence.s2.30
  rule:  pulse.persistence.s2
  fired: the active item is six to thirteen days old, the area holding the item has at least 1 events in the window
  facts: itemTitle=Renew the car registration
  > Renew the car registration keeps the front.
  > Chosen, or defaulted to?
  ? Still going | Stuck on it

2026-05-03  [momentum]  comeback / stage 1 / reflective / mo.come.18
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > Under way again.
2026-05-03  [banner]  weekStarting / stage 1 / reflective / bn.start.20
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > Still near the beginning.
2026-05-03  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-05-03  [report headline]  balanced / stage 1 / plain / hd.bal.46
  rule:  report.headline.balanced
  fired: three or more areas had activity, no area dominates, the window holds at least 5 events, so a share is describing something real
  > No area claimed it.
2026-05-03  [report observation]  areaBalance / stage 1 / editorial / ob.bal.l08
  rule:  report.observation.areaBalance
  fired: three or more areas had activity, no area holds half the week, the window holds at least 6 events, so a share is describing something real
  > A week can be wide or it can be deep.
2026-05-03  [report observation]  selfReportVsData / stage 1 / editorial / ob.srvd.l56
  rule:  report.observation.selfReportVsData
  fired: the item the user answered about is still active, the area holding the item has at least 1 events in the window, there is a stored answer to quote
  > The app is not comparing your answer to anything here. What it does is put the answer and the item side by side.
2026-05-03  [report observation]  areaRevival / stage 1 / observational / ob.rev.l19
  rule:  report.observation.areaRevival
  fired: this area returned after a dormancy, the area has at least 1 events in the window, so naming it cannot be a phantom claim
  facts: ageDays=9 [area.areaDormancyDays:someday], areaName=Someday
  > The silence in Someday ran 9 days.
2026-05-03  [report observation]  intakeVsOutput / stage 2 / neutral_agent / ob.flow.s2.l25
  rule:  report.observation.intakeVsOutput.s2
  fired: intake exceeds output by five or more, there are additions to count
  facts: k=13 [rollup.queueGrowth]
  > 13 more waiting than at the start.
2026-05-03  [report pattern]  reportedVsActual / stage 1 / plain / pt.rva.13
  rule:  report.pattern.reportedVsActual
  fired: there are several stored answers to compare against, one family has been answered about more than twice, there are at least three weeks of snapshots, without which no pattern may fire
  > The record holds five answers.

2026-05-04  [momentum]  comeback / stage 1 / reflective / mo.come.07
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > Something restarted.
2026-05-04  [banner]  weekStarting / stage 1 / reflective / bn.start.19
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > Little to read this early.
2026-05-04  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-05-05  [momentum]  comeback / stage 1 / reflective / mo.come.10
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > The gap ended.
2026-05-05  [banner]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-05-05  [pulse]  quietDay / stage 1 / plain / quietday.s1.19
  rule:  pulse.quietDay.s1
  fired: fewer than two events in the window, per the family trigger, there is at least a day of history, so this is a quiet day and not an empty install
  > Nothing left the queues.
  > Where did the day go?
  ? Recharging | Busy elsewhere | Overwhelmed

2026-05-06  [momentum]  comeback / stage 1 / reflective / mo.come.29
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > Something on the far side of a gap.
2026-05-06  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-05-06  [pulse]  persistence / stage 2 / plain / persistence.s2.33
  rule:  pulse.persistence.s2
  fired: the active item is six to thirteen days old, the area holding the item has at least 1 events in the window
  facts: itemTitle=Renew the car registration
  > Renew the car registration has not finished.
  > One task, or several?
  ? Moving | Parked

2026-05-07  [momentum]  comeback / stage 1 / reflective / mo.come.66
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > Something that had stopped is going again.
2026-05-07  [banner]  weekMixed / stage 1 / reflective / bn.mixed.15
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Tilted, so far.
2026-05-07  [pulse]  accumulation / stage 3 / plain / accumulation.s3.16
  rule:  pulse.accumulation.s3.weeks
  fired: the queues have grown three weeks running, additions still exceed completions by two or more this window, at least two things were added, so no count renders as zero
  > The queues rose again.
  > Does the size of it bother you?
  ? It does not bother me | It does

2026-05-08  [momentum]  comeback / stage 1 / reflective / mo.come.13
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > A restart.
2026-05-08  [banner]  weekMixed / stage 1 / reflective / bn.mixed.16
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week with a heavier side.
2026-05-08  [pulse]  persistence / stage 3 / plain / persistence.s3.19
  rule:  pulse.persistence.s3
  fired: the active item is fourteen to twenty nine days old, the area holding the item has at least 1 events in the window, something else was completed, so the comparison has a number
  facts: itemTitle=Book the dentist
  > Weeks now, on Book the dentist.
  > Waiting on a person, or on a day?
  ? Still mine | Someone else's

2026-05-09  [momentum]  comeback / stage 1 / reflective / mo.come.10
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > The gap ended.
2026-05-09  [banner]  weekMixed / stage 1 / reflective / bn.mixed.26
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Most of it in one area, so far.
2026-05-09  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-05-10  [momentum]  comeback / stage 1 / reflective / mo.come.28
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > A gap with something on the other side of it.
2026-05-10  [banner]  weekStarting / stage 1 / reflective / bn.start.23
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > Nothing here has had time to become a shape.
2026-05-10  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-05-10  [report headline]  mostActiveSince / stage 1 / plain / hd.since.28
  rule:  report.headline.mostActiveSince
  fired: there is an earlier week that strictly beats this one to name, this week beats the one before it, there are three weeks to reach back across
  facts: sinceRef=2026-04-12
  > The last bigger week was in April.
2026-05-10  [report observation]  mostActiveSince / stage 1 / editorial / ob.since.l44
  rule:  report.observation.mostActiveSince
  fired: there is an earlier week that strictly beats this one to name, this week beats the one before it, there are three weeks to reach back across
  facts: sinceRef=2026-04-12
  > The comparison this week invites is with April and with nothing in between.
2026-05-10  [report observation]  selfReportVsData / stage 1 / editorial / ob.srvd.l09
  rule:  report.observation.selfReportVsData
  fired: the item the user answered about is still active, the area holding the item has at least 1 events in the window, there is a stored answer to quote
  > Last time this area went quiet you called it planned. This time it lasted longer.
2026-05-10  [report observation]  areaRevival / stage 1 / observational / ob.rev.l12
  rule:  report.observation.areaRevival
  fired: this area returned after a dormancy, the area has at least 1 events in the window, so naming it cannot be a phantom claim
  facts: areaName=Home
  > The quiet in Home ended this week.
2026-05-10  [report observation]  completionSplit / stage 1 / observational / ob.split.l13
  rule:  report.observation.completionSplit
  fired: three or more pulses were answered in the window, there are completions for the answers to be about
  facts: n=4 [pulse.answeredInWindow]
  > The week produced 4 answers.
2026-05-10  [report pattern]  growingQueues / stage 1 / plain / pt.grow.33
  rule:  report.pattern.growingQueues
  fired: the queues have grown three weeks running, there are at least three weeks of snapshots, without which no pattern may fire
  > A rise, then another rise.

2026-05-11  [momentum]  steadyStretch / stage 1 / reflective / mo.steady.20
  rule:  momentum.steadyStretch
  fired: active on nine or more of the last fourteen days, there are fourteen days to have been active across, the app has existed for the 14 days being described
  > Steady, more than fast.
2026-05-11  [banner]  weekStarting / stage 1 / reflective / bn.start.14
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > Too early for much of a reading.
2026-05-11  [pulse]  accumulation / stage 3 / plain / accumulation.s3.14
  rule:  pulse.accumulation.s3.weeks
  fired: the queues have grown three weeks running, additions still exceed completions by two or more this window, at least two things were added, so no count renders as zero
  > The queues are longer again.
  > Is this collecting, or is this storing?
  ? On purpose | By default

2026-05-12  [momentum]  comeback / stage 1 / reflective / mo.come.04
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > A return, after a gap.
2026-05-12  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-05-12  [pulse]  persistence / stage 1 / plain / persistence.s1.02
  rule:  pulse.persistence.s1
  fired: the active item is three to five days old, the area holding the item has at least 1 events in the window
  facts: ageDays=4 [item.itemAgeDays:itm-30], itemTitle=Book the dentist
  > four days on Book the dentist.
  > The time it needs, or more than that?
  ? Taking time | Taking space

2026-05-13  [momentum]  comeback / stage 1 / reflective / mo.come.10
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > The gap ended.
2026-05-13  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-05-13  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-05-14  [momentum]  comeback / stage 1 / reflective / mo.come.29
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > Something on the far side of a gap.
2026-05-14  [banner]  weekMixed / stage 1 / reflective / bn.mixed.13
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week with a center of gravity in it.
2026-05-14  [pulse]  quietDay / stage 1 / plain / quietday.s1.16
  rule:  pulse.quietDay.s1
  fired: fewer than two events in the window, per the family trigger, there is at least a day of history, so this is a quiet day and not an empty install
  > One quiet day.
  > Full day, or empty one?
  ? Rested | Busy elsewhere | Running on empty

2026-05-15  [momentum]  comeback / stage 1 / reflective / mo.come.16
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > Movement, after a pause.
2026-05-15  [banner]  weekMixed / stage 1 / reflective / bn.mixed.23
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week that did not spread evenly.
2026-05-15  [pulse]  accumulation / stage 3 / plain / accumulation.s3.26
  rule:  pulse.accumulation.s3.weeks
  fired: the queues have grown three weeks running, additions still exceed completions by two or more this window, at least two things were added, so no count renders as zero
  > The lists are longer than they were three weeks ago.
  > Keep it all, or cut some?
  ? It does not bother me | It does

2026-05-16  [momentum]  comeback / stage 1 / reflective / mo.come.23
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > What paused is going again.
2026-05-16  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-05-16  [pulse]  persistence / stage 4 / plain / persistence.s4.21
  rule:  pulse.persistence.s4
  fired: the active item is thirty days old or more, the area holding the item has at least 1 events in the window, this item genuinely holds the longest ever active record, without which stage 4's historical language would be false
  facts: ageDays=129 [item.itemAgeDays:itm-6]
  > One item, four months.
  > Has it changed since you wrote it?
  ? Still the work | Something else now

2026-05-17  [momentum]  comeback / stage 1 / reflective / mo.come.18
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > Under way again.
2026-05-17  [banner]  weekStarting / stage 1 / reflective / bn.start.26
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > A week still near its own beginning.
2026-05-17  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-05-17  [report headline]  balanced / stage 1 / plain / hd.bal.04
  rule:  report.headline.balanced
  fired: three or more areas had activity, no area dominates, the window holds at least 5 events, so a share is describing something real
  > No center.
2026-05-17  [report observation]  areaBalance / stage 1 / editorial / ob.bal.l14
  rule:  report.observation.areaBalance
  fired: three or more areas had activity, no area holds half the week, the window holds at least 6 events, so a share is describing something real
  facts: pct=44 [area.areaShare:work]
  > 44 percent is the largest share anything took. It is not a majority.
2026-05-17  [report observation]  selfReportVsData / stage 1 / editorial / ob.srvd.l40
  rule:  report.observation.selfReportVsData
  fired: the item the user answered about is still active, the area holding the item has at least 1 events in the window, there is a stored answer to quote
  facts: priorLabel=Taking space
  > Taking space was true when you said it. Whether it still is, only you can say.
2026-05-17  [report observation]  areaRevival / stage 1 / observational / ob.rev.l21
  rule:  report.observation.areaRevival
  fired: this area returned after a dormancy, the area has at least 1 events in the window, so naming it cannot be a phantom claim
  facts: areaName=Someday
  > Whatever had stopped in Someday started again this week.
2026-05-17  [report observation]  intakeVsOutput / stage 2 / neutral_agent / ob.flow.s2.l24
  rule:  report.observation.intakeVsOutput.s2
  fired: intake exceeds output by five or more, there are additions to count
  > Intake ran ahead of output.
2026-05-17  [report pattern]  reportedVsActual / stage 1 / plain / pt.rva.29
  rule:  report.pattern.reportedVsActual
  fired: there are several stored answers to compare against, one family has been answered about more than twice, there are at least three weeks of snapshots, without which no pattern may fire
  > You have answered at least five pulses since you started.

2026-05-18  [momentum]  comeback / stage 1 / reflective / mo.come.29
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > Something on the far side of a gap.
2026-05-18  [banner]  weekStarting / stage 1 / reflective / bn.start.21
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > Early, before anything settles.
2026-05-18  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-05-19  [momentum]  comeback / stage 1 / reflective / mo.come.27
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > A stop, then a start.
2026-05-19  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-05-19  [pulse]  accumulation / stage 3 / plain / accumulation.s3.15
  rule:  pulse.accumulation.s3.weeks
  fired: the queues have grown three weeks running, additions still exceed completions by two or more this window, at least two things were added, so no count renders as zero
  > The lists ended the week longer.
  > Would you write all of these again?
  ? There is room | It is full

2026-05-20  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.04
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A wide fortnight.
2026-05-20  [banner]  weekMixed / stage 1 / reflective / bn.mixed.23
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week that did not spread evenly.
2026-05-20  [pulse]  persistence / stage 2 / plain / persistence.s2.24
  rule:  pulse.persistence.s2
  fired: the active item is six to thirteen days old, the area holding the item has at least 1 events in the window
  facts: ageDays=12 [item.itemAgeDays:itm-30], areaName=Work, itemTitle=Book the dentist
  > 12 days of Book the dentist in Work.
  > Is it hard, or is it just sitting there?
  ? By choice | By default

2026-05-21  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.13
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Wide, across two weeks.
2026-05-21  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-05-21  [pulse]  rebalance / stage 1 / plain / rebalance.s1.14
  rule:  pulse.rebalance.s1
  fired: the area had been still five to thirteen days before it moved, the area has at least 1 events in the window, so naming it cannot be a phantom claim
  facts: areaName=Someday
  > Someday is active again.
  > Is this still an area you use?
  ? Noticed the gap | Did not notice

2026-05-22  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.18
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Level, more or less.
2026-05-22  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-05-22  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-05-23  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.19
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Spread out, over fourteen days.
2026-05-23  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-05-23  [pulse]  accumulation / stage 3 / plain / accumulation.s3.21
  rule:  pulse.accumulation.s3.weeks
  fired: the queues have grown three weeks running, additions still exceed completions by two or more this window, at least two things were added, so no count renders as zero
  > The queues rose three weeks running.
  > Growing on purpose, or growing by default?
  ? Fine for now | Worth a clear out

2026-05-24  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.16
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > No one place took it all.
2026-05-24  [banner]  weekStarting / stage 1 / reflective / bn.start.19
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > Little to read this early.
2026-05-24  [pulse]  persistence / stage 4 / plain / persistence.s4.16
  rule:  pulse.persistence.s4
  fired: the active item is thirty days old or more, the area holding the item has at least 1 events in the window, this item genuinely holds the longest ever active record, without which stage 4's historical language would be false
  facts: areaName=Someday
  > Someday has not changed.
  > Worth continuing, or worth reconsidering?
  ? Needs breaking up | Fine as it is
2026-05-24  [report headline]  balanced / stage 1 / plain / hd.bal.40
  rule:  report.headline.balanced
  fired: three or more areas had activity, no area dominates, the window holds at least 5 events, so a share is describing something real
  > A week with breadth.
2026-05-24  [report observation]  areaBalance / stage 1 / editorial / ob.bal.l10
  rule:  report.observation.areaBalance
  fired: three or more areas had activity, no area holds half the week, the window holds at least 6 events, so a share is describing something real
  > Nothing here was the main thing.
2026-05-24  [report observation]  areaRevival / stage 1 / observational / ob.rev.l31
  rule:  report.observation.areaRevival
  fired: this area returned after a dormancy, the area has at least 1 events in the window, so naming it cannot be a phantom claim
  facts: ageDays=5 [area.areaDormancyDays:someday], areaName=Someday
  > An area can be quiet for 5 days and then not be, and Someday is the one that did that here.
2026-05-24  [report observation]  intakeVsOutput / stage 2 / neutral_agent / ob.flow.s2.l14
  rule:  report.observation.intakeVsOutput.s2
  fired: intake exceeds output by five or more, there are additions to count
  > Nothing added this week has left yet.
2026-05-24  [report observation]  queuePressure / stage 1 / neutral_agent / ob.qp.l11
  rule:  report.observation.queuePressure
  fired: at least one area's queue grew, the queues hold three or more things to count
  facts: n=202 [rollup.queueTotal]
  > 202 things are waiting.
2026-05-24  [report pattern]  growingQueues / stage 1 / plain / pt.grow.30
  rule:  report.pattern.growingQueues
  fired: the queues have grown three weeks running, there are at least three weeks of snapshots, without which no pattern may fire
  > Longer than last week. Longer than the week before that.

2026-05-25  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.26
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Several places, none of them all of it.
2026-05-25  [banner]  weekStarting / stage 1 / reflective / bn.start.26
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > A week still near its own beginning.
2026-05-25  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-05-26  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.25
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Two weeks across several areas.
2026-05-26  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-05-26  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-05-27  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.16
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > No one place took it all.
2026-05-27  [banner]  weekMixed / stage 1 / reflective / bn.mixed.09
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week with a tilt to it.
2026-05-27  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-05-28  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.33
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight that went around rather than down.
2026-05-28  [banner]  weekMixed / stage 1 / reflective / bn.mixed.23
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week that did not spread evenly.
2026-05-28  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-05-29  [momentum]  comeback / stage 1 / reflective / mo.come.12
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > Back in motion.
2026-05-29  [banner]  weekMixed / stage 1 / reflective / bn.mixed.13
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week with a center of gravity in it.
2026-05-29  [pulse]  accumulation / stage 3 / plain / accumulation.s3.13
  rule:  pulse.accumulation.s3.weeks
  fired: the queues have grown three weeks running, additions still exceed completions by two or more this window, at least two things were added, so no count renders as zero
  > Longer every week for three weeks.
  > Would you write all of these again?
  ? On purpose | By default

2026-05-30  [momentum]  comeback / stage 1 / reflective / mo.come.29
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > Something on the far side of a gap.
2026-05-30  [banner]  weekMixed / stage 1 / reflective / bn.mixed.12
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week that leans one way.
2026-05-30  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-05-31  [momentum]  comeback / stage 1 / reflective / mo.come.28
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > A gap with something on the other side of it.
2026-05-31  [banner]  weekStarting / stage 1 / reflective / bn.start.26
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > A week still near its own beginning.
2026-05-31  [pulse]  SILENT (NO_RULE_QUALIFIED)
2026-05-31  [report headline]  mostActiveSince / stage 1 / plain / hd.since.57
  rule:  report.headline.mostActiveSince
  fired: there is an earlier week that strictly beats this one to name, this week beats the one before it, there are three weeks to reach back across
  facts: sinceRef=2026-04-12
  > The most in the weeks since April.
2026-05-31  [report observation]  persistentItem / stage 1 / neutral_agent / ob.pers.l11
  rule:  report.observation.persistentItem.high
  fired: the item has been active fourteen days or more, the area holding the item has at least 1 events in the window, there is a median completion time to compare against
  facts: ageDays=48 [item.itemAgeDays:itm-86], areaName=Home, itemTitle=Book the dentist
  > The front of Home has been Book the dentist for 1 month and nothing in the queue has come past it.
2026-05-31  [report observation]  mostActiveSince / stage 1 / editorial / ob.since.l47
  rule:  report.observation.mostActiveSince
  fired: there is an earlier week that strictly beats this one to name, this week beats the one before it, there are three weeks to reach back across
  facts: sinceRef=2026-04-12
  > Somewhere in April there is a week this one did not reach.
2026-05-31  [report observation]  intakeVsOutput / stage 2 / neutral_agent / ob.flow.s2.l10
  rule:  report.observation.intakeVsOutput.s2
  fired: intake exceeds output by five or more, there are additions to count
  facts: m=3 [window.completions], n=12 [window.additions]
  > 12 things arrived. 3 left.
2026-05-31  [report observation]  queuePressure / stage 1 / neutral_agent / ob.qp.l26
  rule:  report.observation.queuePressure
  fired: at least one area's queue grew, the queues hold three or more things to count
  facts: n=211 [rollup.queueTotal]
  > 211 things wait behind the items that are moving.
2026-05-31  [report pattern]  reportedVsActual / stage 1 / plain / pt.rva.34
  rule:  report.pattern.reportedVsActual
  fired: there are several stored answers to compare against, one family has been answered about more than twice, there are at least three weeks of snapshots, without which no pattern may fire
  > Five answers or more sit in the record now.
```

### June 2026

30 days on screen, 21 Pulses spoken, 9 Pulse days silent, 93 sentences in all.

```text
2026-06-01  [momentum]  comeback / stage 1 / reflective / mo.come.66
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > Something that had stopped is going again.
2026-06-01  [banner]  weekStarting / stage 1 / reflective / bn.start.10
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > The start of a week.
2026-06-01  [pulse]  persistence / stage 4 / plain / persistence.s4.24
  rule:  pulse.persistence.s4
  fired: the active item is thirty days old or more, the area holding the item has at least 1 events in the window, this item genuinely holds the longest ever active record, without which stage 4's historical language would be false
  facts: ageDays=145 [item.itemAgeDays:itm-6], areaName=Someday, itemTitle=Label the storage boxes
  > Someday has carried Label the storage boxes for four months.
  > Waiting on you, or waiting on someone else?
  ? Right place | Wrong place

2026-06-02  [momentum]  comeback / stage 1 / reflective / mo.come.14
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > The quiet ended.
2026-06-02  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-06-02  [pulse]  accumulation / stage 3 / plain / accumulation.s3.17
  rule:  pulse.accumulation.s3.weeks
  fired: the queues have grown three weeks running, additions still exceed completions by two or more this window, at least two things were added, so no count renders as zero
  > The queues grew again yesterday.
  > Worth a clear out?
  ? They belong | Time to cut some

2026-06-03  [momentum]  steadyStretch / stage 1 / reflective / mo.steady.07
  rule:  momentum.steadyStretch
  fired: active on nine or more of the last fourteen days, there are fourteen days to have been active across, the app has existed for the 14 days being described
  > A consistent stretch.
2026-06-03  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-06-03  [pulse]  persistence / stage 1 / plain / persistence.s1.29
  rule:  pulse.persistence.s1
  fired: the active item is three to five days old, the area holding the item has at least 1 events in the window
  facts: areaName=Work, itemTitle=Sort the photo backlog
  > Sort the photo backlog is still the active item in Work.
  > Still first, or not any more?
  ? Moving | Parked

2026-06-04  [momentum]  steadyStretch / stage 1 / reflective / mo.steady.30
  rule:  momentum.steadyStretch
  fired: active on nine or more of the last fourteen days, there are fourteen days to have been active across, the app has existed for the 14 days being described
  > A stretch with more days in it than gaps.
2026-06-04  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-06-04  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-06-05  [momentum]  steadyStretch / stage 1 / reflective / mo.steady.31
  rule:  momentum.steadyStretch
  fired: active on nine or more of the last fourteen days, there are fourteen days to have been active across, the app has existed for the 14 days being described
  > A fortnight with more movement than stillness.
2026-06-05  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-06-05  [pulse]  quietDay / stage 1 / plain / quietday.s1.20
  rule:  pulse.quietDay.s1
  fired: fewer than two events in the window, per the family trigger, there is at least a day of history, so this is a quiet day and not an empty install
  > A day with nothing in it.
  > How was it, actually?
  ? Fine | Busy | Struggling

2026-06-06  [momentum]  steadyStretch / stage 1 / reflective / mo.steady.23
  rule:  momentum.steadyStretch
  fired: active on nine or more of the last fourteen days, there are fourteen days to have been active across, the app has existed for the 14 days being described
  > Regular, without being heavy.
2026-06-06  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-06-06  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-06-07  [momentum]  comeback / stage 1 / reflective / mo.come.29
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > Something on the far side of a gap.
2026-06-07  [banner]  weekStarting / stage 1 / reflective / bn.start.24
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > The shape of this week is still ahead.
2026-06-07  [pulse]  accumulation / stage 3 / plain / accumulation.s3.31
  rule:  pulse.accumulation.s3.weeks
  fired: the queues have grown three weeks running, additions still exceed completions by two or more this window, at least two things were added, so no count renders as zero
  > The lists took on more than they gave up yesterday.
  > Is there a plan for the older ones?
  ? Too much coming in | Not enough going out
2026-06-07  [report headline]  balanced / stage 1 / plain / hd.bal.26
  rule:  report.headline.balanced
  fired: three or more areas had activity, no area dominates, the window holds at least 5 events, so a share is describing something real
  > No single center of gravity.
2026-06-07  [report observation]  areaBalance / stage 1 / editorial / ob.bal.l11
  rule:  report.observation.areaBalance
  fired: three or more areas had activity, no area holds half the week, the window holds at least 6 events, so a share is describing something real
  > A wide week and a thin week look the same from a count, and nothing here tells them apart.
2026-06-07  [report observation]  areaRevival / stage 1 / observational / ob.rev.l13
  rule:  report.observation.areaRevival
  fired: this area returned after a dormancy, the area has at least 1 events in the window, so naming it cannot be a phantom claim
  facts: areaName=Someday
  > Someday has events again.
2026-06-07  [report observation]  completionSplit / stage 1 / observational / ob.split.l11
  rule:  report.observation.completionSplit
  fired: three or more pulses were answered in the window, there are completions for the answers to be about
  facts: m=2 [pulse.positiveInWindow], n=3 [pulse.answeredInWindow]
  > Of 3 answers, 2 were the positive one.
2026-06-07  [report observation]  intakeVsOutput / stage 2 / neutral_agent / ob.flow.s2.l16
  rule:  report.observation.intakeVsOutput.s2
  fired: intake exceeds output by five or more, there are additions to count
  facts: k=13 [rollup.queueGrowth]
  > 13 net, into the queues.
2026-06-07  [report pattern]  growingQueues / stage 1 / plain / pt.grow.43
  rule:  report.pattern.growingQueues
  fired: the queues have grown three weeks running, there are at least three weeks of snapshots, without which no pattern may fire
  > The number has climbed twice.

2026-06-08  [momentum]  comeback / stage 1 / reflective / mo.come.21
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > The thread picked up again.
2026-06-08  [banner]  weekStarting / stage 1 / reflective / bn.start.15
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > Nothing settled yet.
2026-06-08  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-06-09  [momentum]  comeback / stage 1 / reflective / mo.come.14
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > The quiet ended.
2026-06-09  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-06-09  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-06-10  [momentum]  comeback / stage 1 / reflective / mo.come.19
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > A gap closed.
2026-06-10  [banner]  weekMixed / stage 1 / reflective / bn.mixed.12
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week that leans one way.
2026-06-10  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-06-11  [momentum]  comeback / stage 1 / reflective / mo.come.25
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > The pause has run out.
2026-06-11  [banner]  weekMixed / stage 1 / reflective / bn.mixed.13
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week with a center of gravity in it.
2026-06-11  [pulse]  persistence / stage 4 / plain / persistence.s4.17
  rule:  pulse.persistence.s4
  fired: the active item is thirty days old or more, the area holding the item has at least 1 events in the window, this item genuinely holds the longest ever active record, without which stage 4's historical language would be false
  facts: itemTitle=Label the storage boxes
  > Label the storage boxes has held for months.
  > Has it changed since you wrote it?
  ? Waiting on a decision | Waiting on time

2026-06-12  [momentum]  comeback / stage 1 / reflective / mo.come.22
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > Movement where there was none.
2026-06-12  [banner]  weekMixed / stage 1 / reflective / bn.mixed.17
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Spread unevenly, so far.
2026-06-12  [pulse]  accumulation / stage 3 / plain / accumulation.s3.24
  rule:  pulse.accumulation.s3.weeks
  fired: the queues have grown three weeks running, additions still exceed completions by two or more this window, at least two things were added, so no count renders as zero
  > The queues were shorter three weeks ago.
  > Is anything on there done already?
  ? Fine for now | Worth a clear out

2026-06-13  [momentum]  comeback / stage 1 / reflective / mo.come.07
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > Something restarted.
2026-06-13  [banner]  weekMixed / stage 1 / reflective / bn.mixed.09
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week with a tilt to it.
2026-06-13  [pulse]  persistence / stage 1 / plain / persistence.s1.20
  rule:  pulse.persistence.s1
  fired: the active item is three to five days old, the area holding the item has at least 1 events in the window
  facts: itemTitle=Finish the tax folder
  > Finish the tax folder sits where it sat.
  > Where is it actually at?
  ? On purpose | Just there

2026-06-14  [momentum]  steadyStretch / stage 1 / reflective / mo.steady.55
  rule:  momentum.steadyStretch
  fired: active on nine or more of the last fourteen days, there are fourteen days to have been active across, the app has existed for the 14 days being described
  > Two weeks with something in most of them.
2026-06-14  [banner]  weekStarting / stage 1 / reflective / bn.start.14
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > Too early for much of a reading.
2026-06-14  [pulse]  quietDay / stage 1 / plain / quietday.s1.21
  rule:  pulse.quietDay.s1
  fired: fewer than two events in the window, per the family trigger, there is at least a day of history, so this is a quiet day and not an empty install
  > Yesterday left things as they were.
  > Did you need it?
  ? Resting | Elsewhere | Stuck
2026-06-14  [report headline]  comeback / stage 1 / plain / hd.back.59
  rule:  report.headline.comeback
  fired: this area returned after a long dormancy, the area has at least 1 events in the window, so naming it cannot be a phantom claim
  > A still stretch, over.
2026-06-14  [report observation]  areaRevival / stage 1 / observational / ob.rev.l11
  rule:  report.observation.areaRevival
  fired: this area returned after a dormancy, the area has at least 1 events in the window, so naming it cannot be a phantom claim
  facts: ageDays=7 [area.areaDormancyDays:someday], areaName=Someday
  > Someday has something in it again after 7 days.
2026-06-14  [report observation]  intakeVsOutput / stage 2 / neutral_agent / ob.flow.s2.l21
  rule:  report.observation.intakeVsOutput.s2
  fired: intake exceeds output by five or more, there are additions to count
  facts: n=11 [window.additions]
  > 11 arrived.
2026-06-14  [report observation]  queuePressure / stage 1 / neutral_agent / ob.qp.l29
  rule:  report.observation.queuePressure
  fired: at least one area's queue grew, the queues hold three or more things to count
  facts: areaCount=3 [rollup.areasWithQueue], areaName=Work
  > 3 areas are holding something behind their active item, and the longest of those queues is in Work.
2026-06-14  [report observation]  timeOfDay / stage 1 / observational / ob.tod.l21
  rule:  report.observation.timeOfDay
  fired: one part of the day holds most of the week, the week was not confined to a single part of one day
  > The week leaned toward one part of the day.
2026-06-14  [report pattern]  reportedVsActual / stage 1 / plain / pt.rva.47
  rule:  report.pattern.reportedVsActual
  fired: there are several stored answers to compare against, one family has been answered about more than twice, there are at least three weeks of snapshots, without which no pattern may fire
  > You have answered at least five pulses, and three of them were about the same kind of moment.

2026-06-15  [momentum]  steadyStretch / stage 1 / reflective / mo.steady.16
  rule:  momentum.steadyStretch
  fired: active on nine or more of the last fourteen days, there are fourteen days to have been active across, the app has existed for the 14 days being described
  > Even going, two weeks running.
2026-06-15  [banner]  weekStarting / stage 1 / reflective / bn.start.17
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > The first days of it.
2026-06-15  [pulse]  persistence / stage 4 / plain / persistence.s4.53
  rule:  pulse.persistence.s4
  fired: the active item is thirty days old or more, the area holding the item has at least 1 events in the window, this item genuinely holds the longest ever active record, without which stage 4's historical language would be false
  > Still the same front.
  > Is this a project rather than an item?
  ? One thing | Many

2026-06-16  [momentum]  steadyStretch / stage 1 / reflective / mo.steady.21
  rule:  momentum.steadyStretch
  fired: active on nine or more of the last fourteen days, there are fourteen days to have been active across, the app has existed for the 14 days being described
  > Few empty days in it.
2026-06-16  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-06-16  [pulse]  accumulation / stage 3 / plain / accumulation.s3.29
  rule:  pulse.accumulation.s3.weeks
  fired: the queues have grown three weeks running, additions still exceed completions by two or more this window, at least two things were added, so no count renders as zero
  > The lists are longer this morning than yesterday morning.
  > Keep it all, or cut some?
  ? There is room | It is full

2026-06-17  [momentum]  comeback / stage 1 / reflective / mo.come.22
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > Movement where there was none.
2026-06-17  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-06-17  [pulse]  persistence / stage 2 / plain / persistence.s2.32
  rule:  pulse.persistence.s2
  fired: the active item is six to thirteen days old, the area holding the item has at least 1 events in the window
  facts: areaName=Work, itemTitle=Finish the tax folder
  > Work starts with Finish the tax folder.
  > One task, or several?
  ? Yes, still right | Time to swap

2026-06-18  [momentum]  comeback / stage 1 / reflective / mo.come.20
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > The quiet gave way.
2026-06-18  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-06-18  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-06-19  [momentum]  comeback / stage 1 / reflective / mo.come.10
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > The gap ended.
2026-06-19  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-06-19  [pulse]  quietDay / stage 1 / plain / quietday.s1.22
  rule:  pulse.quietDay.s1
  fired: fewer than two events in the window, per the family trigger, there is at least a day of history, so this is a quiet day and not an empty install
  > A day of almost no events.
  > How did it feel?
  ? A pause | Away | Stalled

2026-06-20  [momentum]  comeback / stage 1 / reflective / mo.come.24
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > A stretch of nothing, then something.
2026-06-20  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-06-20  [pulse]  accumulation / stage 3 / plain / accumulation.s3.27
  rule:  pulse.accumulation.s3.weeks
  fired: the queues have grown three weeks running, additions still exceed completions by two or more this window, at least two things were added, so no count renders as zero
  > Week on week, the queues have grown.
  > Keep it all, or cut some?
  ? It does not bother me | It does

2026-06-21  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.31
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > More than one thing at a time.
2026-06-21  [banner]  weekStarting / stage 1 / reflective / bn.start.09
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > Early yet.
2026-06-21  [pulse]  persistence / stage 2 / plain / persistence.s2.26
  rule:  pulse.persistence.s2
  fired: the active item is six to thirteen days old, the area holding the item has at least 1 events in the window
  facts: ageDays=13 [item.itemAgeDays:itm-40]
  > 13 days without a finish.
  > Wanted, or just kept?
  ? Yes, still right | Time to swap
2026-06-21  [report headline]  balanced / stage 1 / plain / hd.bal.48
  rule:  report.headline.balanced
  fired: three or more areas had activity, no area dominates, the window holds at least 5 events, so a share is describing something real
  > Three fronts at least.
2026-06-21  [report observation]  areaBalance / stage 1 / editorial / ob.bal.l16
  rule:  report.observation.areaBalance
  fired: three or more areas had activity, no area holds half the week, the window holds at least 6 events, so a share is describing something real
  > Spread is not the same as balance. This week was spread.
2026-06-21  [report observation]  selfReportVsData / stage 1 / editorial / ob.srvd.l45
  rule:  report.observation.selfReportVsData
  fired: the item the user answered about is still active, the area holding the item has at least 1 events in the window, there is a stored answer to quote
  facts: priorLabel=Time to swap
  > The one thing the app knows about this item that it did not measure is Time to swap.
2026-06-21  [report observation]  areaRevival / stage 1 / observational / ob.rev.l09
  rule:  report.observation.areaRevival
  fired: this area returned after a dormancy, the area has at least 1 events in the window, so naming it cannot be a phantom claim
  facts: areaName=Home
  > Something in Home started again.
2026-06-21  [report observation]  intakeVsOutput / stage 2 / neutral_agent / ob.flow.s2.l35
  rule:  report.observation.intakeVsOutput.s2
  fired: intake exceeds output by five or more, there are additions to count
  > Five or more things separate what arrived this week from what left it, and seven days is where both numbers came from.
2026-06-21  [report pattern]  growingQueues / stage 1 / plain / pt.grow.15
  rule:  report.pattern.growingQueues
  fired: the queues have grown three weeks running, there are at least three weeks of snapshots, without which no pattern may fire
  > More is waiting than a week ago.

2026-06-22  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.17
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Room for more than one thing.
2026-06-22  [banner]  weekStarting / stage 1 / reflective / bn.start.17
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > The first days of it.
2026-06-22  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-06-23  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.27
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight that went in several directions.
2026-06-23  [banner]  weekMixed / stage 1 / reflective / bn.mixed.11
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Weighted rather than spread.
2026-06-23  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-06-24  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.20
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > No single thing has held it.
2026-06-24  [banner]  weekMixed / stage 1 / reflective / bn.mixed.25
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Uneven going.
2026-06-24  [pulse]  accumulation / stage 3 / plain / accumulation.s3.30
  rule:  pulse.accumulation.s3.weeks
  fired: the queues have grown three weeks running, additions still exceed completions by two or more this window, at least two things were added, so no count renders as zero
  > Yesterday left the queues longer than it found them.
  > Is anything on there done already?
  ? Keep it all | Cut some

2026-06-25  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.24
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight that kept moving around.
2026-06-25  [banner]  weekStrong / stage 1 / reflective / bn.strong.15
  rule:  banner.weekStrong
  fired: completions are clearly above the recent weekly average, there are completions to count, there are earlier weeks to be above
  > Plenty already, with the week still open.
2026-06-25  [pulse]  concentration / stage 1 / plain / concentration.s1.22
  rule:  pulse.concentration.s1
  fired: the area holds seventy to eighty four percent of the window, the area has at least 4 events in the window, so its share is describing something real, the window holds at least 4 events, so a share is describing something real
  > Your day happened mostly in one area.
  > Would tomorrow look the same?
  ? It was enough | Not quite enough

2026-06-26  [momentum]  comeback / stage 1 / reflective / mo.come.26
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > Quiet, then not quiet.
2026-06-26  [banner]  weekStrong / stage 1 / reflective / bn.strong.28
  rule:  banner.weekStrong
  fired: completions are clearly above the recent weekly average, there are completions to count, there are earlier weeks to be above
  > The week is holding more than most.
2026-06-26  [pulse]  persistence / stage 3 / plain / persistence.s3.17
  rule:  pulse.persistence.s3
  fired: the active item is fourteen to twenty nine days old, the area holding the item has at least 1 events in the window, something else was completed, so the comparison has a number
  facts: ageDays=18 [item.itemAgeDays:itm-40], itemTitle=Finish the tax folder
  > two weeks, still Finish the tax folder.
  > Is it waiting on you, or on something else?
  ? Close | Early

2026-06-27  [momentum]  comeback / stage 1 / reflective / mo.come.14
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > The quiet ended.
2026-06-27  [banner]  weekStrong / stage 1 / reflective / bn.strong.10
  rule:  banner.weekStrong
  fired: completions are clearly above the recent weekly average, there are completions to count, there are earlier weeks to be above
  > A full week, and not over.
2026-06-27  [pulse]  quietDay / stage 1 / plain / quietday.s1.18
  rule:  pulse.quietDay.s1
  fired: fewer than two events in the window, per the family trigger, there is at least a day of history, so this is a quiet day and not an empty install
  > No swaps yesterday.
  > What took the day?
  ? Fine | Busy | Struggling

2026-06-28  [momentum]  comeback / stage 1 / reflective / mo.come.20
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > The quiet gave way.
2026-06-28  [banner]  weekStarting / stage 1 / reflective / bn.start.26
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > A week still near its own beginning.
2026-06-28  [pulse]  accumulation / stage 3 / plain / accumulation.s3.18
  rule:  pulse.accumulation.s3.weeks
  fired: the queues have grown three weeks running, additions still exceed completions by two or more this window, at least two things were added, so no count renders as zero
  > More is on the lists now.
  > Growing, or gathering?
  ? Building up | Avoiding
2026-06-28  [report headline]  personalBest / stage 1 / plain / hd.best.46
  rule:  report.headline.personalBest
  fired: no earlier week strictly beats this one, this week reaches the personal best, there is more than one week to be best of, there are completions, so no count renders as zero
  facts: n=10 [window.completions]
  > 10 finished, the most yet.
2026-06-28  [report observation]  personalBest / stage 1 / editorial / ob.best.l12
  rule:  report.observation.personalBest
  fired: no earlier week strictly beats this one, this week reaches the personal best, there is more than one week to be best of, there are completions, so no count renders as zero
  > The record moved. That is all the record does.
2026-06-28  [report observation]  persistentItem / stage 1 / neutral_agent / ob.pers.l10
  rule:  report.observation.persistentItem.high
  fired: the item has been active fourteen days or more, the area holding the item has at least 1 events in the window, there is a median completion time to compare against
  facts: itemTitle=Finish the tax folder, n=153 [item.itemQueueBehind:itm-40]
  > Finish the tax folder has 153 things behind it.
2026-06-28  [report observation]  completionSplit / stage 1 / observational / ob.split.l18
  rule:  report.observation.completionSplit
  fired: three or more pulses were answered in the window, there are completions for the answers to be about
  facts: n=3 [pulse.answeredInWindow]
  > Your own read on the week is in the record 3 times.
2026-06-28  [report observation]  dayShape / stage 1 / observational / ob.day.l03
  rule:  report.observation.dayShape
  fired: one day of the week stands out and can be named, that day holds a third of the week or more
  facts: n=7 [window.activeDays]
  > 7 of seven days had activity.
2026-06-28  [report pattern]  reportedVsActual / stage 1 / plain / pt.rva.41
  rule:  report.pattern.reportedVsActual
  fired: there are several stored answers to compare against, one family has been answered about more than twice, there are at least three weeks of snapshots, without which no pattern may fire
  > A question the app has asked you more than twice.

2026-06-29  [momentum]  comeback / stage 1 / reflective / mo.come.28
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > A gap with something on the other side of it.
2026-06-29  [banner]  weekStarting / stage 1 / reflective / bn.start.16
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > A week still mostly ahead.
2026-06-29  [pulse]  persistence / stage 4 / plain / persistence.s4.47
  rule:  pulse.persistence.s4
  fired: the active item is thirty days old or more, the area holding the item has at least 1 events in the window, this item genuinely holds the longest ever active record, without which stage 4's historical language would be false
  facts: ageDays=173 [item.itemAgeDays:itm-6]
  > five months, and the same name.
  > Does it need breaking up?
  ? Yes, still | No, not today

2026-06-30  [momentum]  comeback / stage 1 / reflective / mo.come.28
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > A gap with something on the other side of it.
2026-06-30  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-06-30  [pulse]  SILENT (NO_RULE_QUALIFIED)
```

### July 2026

31 days on screen, 18 Pulses spoken, 13 Pulse days silent, 92 sentences in all.

```text
2026-07-01  [momentum]  comeback / stage 1 / reflective / mo.come.27
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > A stop, then a start.
2026-07-01  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-07-01  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-07-02  [momentum]  comeback / stage 1 / reflective / mo.come.65
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > A stretch of nothing with a finish to it.
2026-07-02  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-07-02  [pulse]  accumulation / stage 3 / plain / accumulation.s3.28
  rule:  pulse.accumulation.s3.weeks
  fired: the queues have grown three weeks running, additions still exceed completions by two or more this window, at least two things were added, so no count renders as zero
  facts: n=2 [window.additions]
  > Yesterday put another two things on the lists.
  > Has this been useful, or just growing?
  ? Too much coming in | Not enough going out

2026-07-03  [momentum]  steadyStretch / stage 1 / reflective / mo.steady.27
  rule:  momentum.steadyStretch
  fired: active on nine or more of the last fourteen days, there are fourteen days to have been active across, the app has existed for the 14 days being described
  > Something on most of the days.
2026-07-03  [banner]  weekMixed / stage 1 / reflective / bn.mixed.15
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Tilted, so far.
2026-07-03  [pulse]  persistence / stage 2 / plain / persistence.s2.19
  rule:  pulse.persistence.s2
  fired: the active item is six to thirteen days old, the area holding the item has at least 1 events in the window
  facts: ageDays=7 [item.itemAgeDays:itm-117]
  > seven days and holding.
  > One task, or several?
  ? Worth the time | Worth a rethink

2026-07-04  [momentum]  steadyStretch / stage 1 / reflective / mo.steady.13
  rule:  momentum.steadyStretch
  fired: active on nine or more of the last fourteen days, there are fourteen days to have been active across, the app has existed for the 14 days being described
  > A rhythm, more than a run.
2026-07-04  [banner]  weekMixed / stage 1 / reflective / bn.mixed.12
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week that leans one way.
2026-07-04  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-07-05  [momentum]  steadyStretch / stage 1 / reflective / mo.steady.16
  rule:  momentum.steadyStretch
  fired: active on nine or more of the last fourteen days, there are fourteen days to have been active across, the app has existed for the 14 days being described
  > Even going, two weeks running.
2026-07-05  [banner]  weekStarting / stage 1 / reflective / bn.start.26
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > A week still near its own beginning.
2026-07-05  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-07-05  [report headline]  netInflow / stage 1 / plain / hd.in.09
  rule:  report.headline.netInflow
  fired: additions exceed completions by a clear margin, there are additions to speak of
  > Opened more than closed.
2026-07-05  [report observation]  intakeVsOutput / stage 2 / neutral_agent / ob.flow.s2.l18
  rule:  report.observation.intakeVsOutput.s2
  fired: intake exceeds output by five or more, there are additions to count
  facts: k=11 [rollup.queueGrowth], m=2 [window.completions], n=13 [window.additions]
  > 13 in, 2 out, and 11 added to the queues.
2026-07-05  [report observation]  persistentItem / stage 1 / observational / ob.pers.l44
  rule:  report.observation.persistentItem.low
  fired: the item has been active seven to thirteen days, the area holding the item has at least 1 events in the window
  facts: ageDays=9 [item.itemAgeDays:itm-117], areaName=Home, itemTitle=Tidy the reading list
  > For 9 days the first thing in Home has been Tidy the reading list. It is still the first thing.
2026-07-05  [report observation]  queuePressure / stage 1 / neutral_agent / ob.qp.l24
  rule:  report.observation.queuePressure
  fired: at least one area's queue grew, the queues hold three or more things to count
  facts: n=260 [rollup.queueTotal]
  > The queues came out of the week holding 260 things.
2026-07-05  [report observation]  timeOfDay / stage 1 / observational / ob.tod.l12
  rule:  report.observation.timeOfDay
  fired: one part of the day holds most of the week, the week was not confined to a single part of one day
  > Your week has a preferred time.
2026-07-05  [report pattern]  growingQueues / stage 1 / plain / pt.grow.25
  rule:  report.pattern.growingQueues
  fired: the queues have grown three weeks running, there are at least three weeks of snapshots, without which no pattern may fire
  > The queues took on more than they gave back.

2026-07-06  [momentum]  steadyStretch / stage 1 / reflective / mo.steady.23
  rule:  momentum.steadyStretch
  fired: active on nine or more of the last fourteen days, there are fourteen days to have been active across, the app has existed for the 14 days being described
  > Regular, without being heavy.
2026-07-06  [banner]  weekStarting / stage 1 / reflective / bn.start.21
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > Early, before anything settles.
2026-07-06  [pulse]  quietDay / stage 1 / plain / quietday.s1.26
  rule:  pulse.quietDay.s1
  fired: fewer than two events in the window, per the family trigger, there is at least a day of history, so this is a quiet day and not an empty install
  > Nothing completed, nothing added, nothing swapped.
  > Did you need it?
  ? Needed it | Doing other things | Not coping

2026-07-07  [momentum]  steadyStretch / stage 1 / reflective / mo.steady.09
  rule:  momentum.steadyStretch
  fired: active on nine or more of the last fourteen days, there are fourteen days to have been active across, the app has existed for the 14 days being described
  > Steady, across fourteen days.
2026-07-07  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-07-07  [pulse]  accumulation / stage 3 / plain / accumulation.s3.23
  rule:  pulse.accumulation.s3.weeks
  fired: the queues have grown three weeks running, additions still exceed completions by two or more this window, at least two things were added, so no count renders as zero
  > Your lists have grown in each of the last three weeks.
  > Would you miss any of them?
  ? Building up | Avoiding

2026-07-08  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.17
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Room for more than one thing.
2026-07-08  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-07-08  [pulse]  persistence / stage 1 / plain / persistence.s1.22
  rule:  pulse.persistence.s1
  fired: the active item is three to five days old, the area holding the item has at least 1 events in the window
  facts: ageDays=3 [item.itemAgeDays:itm-44]
  > Same front, three days on.
  > Underway, or untouched?
  ? On purpose | Just there

2026-07-09  [momentum]  steadyStretch / stage 1 / reflective / mo.steady.19
  rule:  momentum.steadyStretch
  fired: active on nine or more of the last fourteen days, there are fourteen days to have been active across, the app has existed for the 14 days being described
  > A regular fortnight.
2026-07-09  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-07-09  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-07-10  [momentum]  steadyStretch / stage 1 / reflective / mo.steady.21
  rule:  momentum.steadyStretch
  fired: active on nine or more of the last fourteen days, there are fourteen days to have been active across, the app has existed for the 14 days being described
  > Few empty days in it.
2026-07-10  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-07-10  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-07-11  [momentum]  steadyStretch / stage 1 / reflective / mo.steady.19
  rule:  momentum.steadyStretch
  fired: active on nine or more of the last fourteen days, there are fourteen days to have been active across, the app has existed for the 14 days being described
  > A regular fortnight.
2026-07-11  [banner]  weekMixed / stage 1 / reflective / bn.mixed.14
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A shape rather than a spread.
2026-07-11  [pulse]  quietDay / stage 1 / plain / quietday.s1.02
  rule:  pulse.quietDay.s1
  fired: fewer than two events in the window, per the family trigger, there is at least a day of history, so this is a quiet day and not an empty install
  > Nothing moved yesterday.
  > Busy elsewhere?
  ? Needed the day | Out doing things | Could not start

2026-07-12  [momentum]  comeback / stage 1 / reflective / mo.come.29
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > Something on the far side of a gap.
2026-07-12  [banner]  weekStarting / stage 1 / reflective / bn.start.13
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > The opening of a week.
2026-07-12  [pulse]  accumulation / stage 3 / plain / accumulation.s3.22
  rule:  pulse.accumulation.s3.weeks
  fired: the queues have grown three weeks running, additions still exceed completions by two or more this window, at least two things were added, so no count renders as zero
  > The queues have been growing for three weeks.
  > Do these all still belong?
  ? Useful | Just growing
2026-07-12  [report headline]  balanced / stage 1 / plain / hd.bal.32
  rule:  report.headline.balanced
  fired: three or more areas had activity, no area dominates, the window holds at least 5 events, so a share is describing something real
  > Some in each.
2026-07-12  [report observation]  areaBalance / stage 1 / editorial / ob.bal.l13
  rule:  report.observation.areaBalance
  fired: three or more areas had activity, no area holds half the week, the window holds at least 6 events, so a share is describing something real
  > Every area got some of the week and none got most of it.
2026-07-12  [report observation]  selfReportVsData / stage 1 / editorial / ob.srvd.l35
  rule:  report.observation.selfReportVsData
  fired: the item the user answered about is still active, the area holding the item has at least 1 events in the window, there is a stored answer to quote
  facts: itemTitle=Book the dentist
  > What you said about Book the dentist may still be true. This is only a note that you said it.
2026-07-12  [report observation]  areaRevival / stage 1 / observational / ob.rev.l15
  rule:  report.observation.areaRevival
  fired: this area returned after a dormancy, the area has at least 1 events in the window, so naming it cannot be a phantom claim
  facts: ageDays=8 [area.areaDormancyDays:someday]
  > A gap of 8 days, now closed.
2026-07-12  [report observation]  intakeVsOutput / stage 2 / neutral_agent / ob.flow.s2.l17
  rule:  report.observation.intakeVsOutput.s2
  fired: intake exceeds output by five or more, there are additions to count
  > More came in than went out, by five or more.
2026-07-12  [report pattern]  reportedVsActual / stage 1 / plain / pt.rva.18
  rule:  report.pattern.reportedVsActual
  fired: there are several stored answers to compare against, one family has been answered about more than twice, there are at least three weeks of snapshots, without which no pattern may fire
  > Answers, and then more answers.

2026-07-13  [momentum]  comeback / stage 1 / reflective / mo.come.16
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > Movement, after a pause.
2026-07-13  [banner]  weekStarting / stage 1 / reflective / bn.start.09
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > Early yet.
2026-07-13  [pulse]  persistence / stage 2 / plain / persistence.s2.35
  rule:  pulse.persistence.s2
  fired: the active item is six to thirteen days old, the area holding the item has at least 1 events in the window
  facts: ageDays=8 [item.itemAgeDays:itm-44], itemTitle=Book the dentist
  > Book the dentist became the active item eight days ago and still is.
  > Big job, or blocked job?
  ? Too big | Just slow

2026-07-14  [momentum]  comeback / stage 1 / reflective / mo.come.20
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > The quiet gave way.
2026-07-14  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-07-14  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-07-15  [momentum]  comeback / stage 1 / reflective / mo.come.14
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > The quiet ended.
2026-07-15  [banner]  weekMixed / stage 1 / reflective / bn.mixed.14
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A shape rather than a spread.
2026-07-15  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-07-16  [momentum]  comeback / stage 1 / reflective / mo.come.15
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > A gap, then movement.
2026-07-16  [banner]  weekMixed / stage 1 / reflective / bn.mixed.19
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week gathered mostly in one place.
2026-07-16  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-07-17  [momentum]  comeback / stage 1 / reflective / mo.come.22
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > Movement where there was none.
2026-07-17  [banner]  weekMixed / stage 1 / reflective / bn.mixed.15
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Tilted, so far.
2026-07-17  [pulse]  accumulation / stage 3 / plain / accumulation.s3.12
  rule:  pulse.accumulation.s3.weeks
  fired: the queues have grown three weeks running, additions still exceed completions by two or more this window, at least two things were added, so no count renders as zero
  > Three weeks of growth.
  > Would you miss any of them?
  ? Fine for now | Worth a clear out

2026-07-18  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.24
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight that kept moving around.
2026-07-18  [banner]  weekMixed / stage 1 / reflective / bn.mixed.19
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week gathered mostly in one place.
2026-07-18  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-07-19  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.22
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Two weeks that went several ways.
2026-07-19  [banner]  weekStarting / stage 1 / reflective / bn.start.16
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > A week still mostly ahead.
2026-07-19  [pulse]  persistence / stage 1 / plain / persistence.s1.13
  rule:  pulse.persistence.s1
  fired: the active item is three to five days old, the area holding the item has at least 1 events in the window
  facts: ageDays=3 [item.itemAgeDays:itm-45], itemTitle=Clear the garage shelf
  > Clear the garage shelf arrived at the front three days ago and has stayed.
  > Part done, or not begun?
  ? In progress | Not started
2026-07-19  [report headline]  mostActiveSince / stage 1 / plain / hd.since.53
  rule:  report.headline.mostActiveSince
  fired: there is an earlier week that strictly beats this one to name, this week beats the one before it, there are three weeks to reach back across
  > Nothing since has been larger.
2026-07-19  [report observation]  mostActiveSince / stage 1 / editorial / ob.since.l60
  rule:  report.observation.mostActiveSince
  fired: there is an earlier week that strictly beats this one to name, this week beats the one before it, there are three weeks to reach back across
  facts: sinceRef=2026-06-28
  > There is a week in June this one did not reach, and nothing between the two got higher than this.
2026-07-19  [report observation]  areaRevival / stage 1 / observational / ob.rev.l18
  rule:  report.observation.areaRevival
  fired: this area returned after a dormancy, the area has at least 1 events in the window, so naming it cannot be a phantom claim
  facts: areaName=Someday
  > Someday came off the quiet list.
2026-07-19  [report observation]  completionSplit / stage 1 / observational / ob.split.l28
  rule:  report.observation.completionSplit
  fired: three or more pulses were answered in the window, there are completions for the answers to be about
  facts: n=3 [pulse.answeredInWindow]
  > The app asked about your days as they went, and you answered 3 times over the seven of them.
2026-07-19  [report observation]  intakeVsOutput / stage 2 / neutral_agent / ob.flow.s2.l12
  rule:  report.observation.intakeVsOutput.s2
  fired: intake exceeds output by five or more, there are additions to count
  facts: k=11 [rollup.queueGrowth]
  > 11 more things are waiting than were on Sunday.
2026-07-19  [report pattern]  growingQueues / stage 1 / plain / pt.grow.58
  rule:  report.pattern.growingQueues
  fired: the queues have grown three weeks running, there are at least three weeks of snapshots, without which no pattern may fire
  > Still rising.

2026-07-20  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.33
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight that went around rather than down.
2026-07-20  [banner]  weekStarting / stage 1 / reflective / bn.start.21
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > Early, before anything settles.
2026-07-20  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-07-21  [momentum]  comeback / stage 1 / reflective / mo.come.29
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > Something on the far side of a gap.
2026-07-21  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-07-21  [pulse]  accumulation / stage 3 / plain / accumulation.s3.25
  rule:  pulse.accumulation.s3.weeks
  fired: the queues have grown three weeks running, additions still exceed completions by two or more this window, at least two things were added, so no count renders as zero
  > Each of the last three weeks left the queues longer.
  > Room for more, or full?
  ? On purpose | By default

2026-07-22  [momentum]  comeback / stage 1 / reflective / mo.come.13
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > A restart.
2026-07-22  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-07-22  [pulse]  persistence / stage 2 / plain / persistence.s2.16
  rule:  pulse.persistence.s2
  fired: the active item is six to thirteen days old, the area holding the item has at least 1 events in the window
  facts: ageDays=6 [item.itemAgeDays:itm-45], itemTitle=Clear the garage shelf
  > Clear the garage shelf became active six days ago and has not moved since.
  > Slow going, or not going?
  ? Close | Not close

2026-07-23  [momentum]  comeback / stage 1 / reflective / mo.come.61
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  facts: areaName=Someday
  > Movement in Someday on the other side of a gap.
2026-07-23  [banner]  weekMixed / stage 1 / reflective / bn.mixed.21
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week with more of it in one area.
2026-07-23  [pulse]  quietDay / stage 1 / plain / quietday.s1.23
  rule:  pulse.quietDay.s1
  fired: fewer than two events in the window, per the family trigger, there is at least a day of history, so this is a quiet day and not an empty install
  > An uneventful day.
  > Away on purpose?
  ? Rested | Busy elsewhere | Running on empty

2026-07-24  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.20
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > No single thing has held it.
2026-07-24  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-07-24  [pulse]  rebalance / stage 1 / plain / rebalance.s1.15
  rule:  pulse.rebalance.s1
  fired: the area had been still five to thirteen days before it moved, the area has at least 1 events in the window, so naming it cannot be a phantom claim
  facts: ageDays=6 [area.areaDormancyDays:someday], areaName=Someday
  > The gap in Someday was six days long.
  > Does this feel like a return?
  ? A break | A drift

2026-07-25  [momentum]  comeback / stage 1 / reflective / mo.come.10
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > The gap ended.
2026-07-25  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-07-25  [pulse]  persistence / stage 2 / plain / persistence.s2.02
  rule:  pulse.persistence.s2
  fired: the active item is six to thirteen days old, the area holding the item has at least 1 events in the window
  facts: ageDays=9 [item.itemAgeDays:itm-45], areaName=Work, itemTitle=Clear the garage shelf
  > Clear the garage shelf has held Work for nine days.
  > Moving inside, or not at all?
  ? Close | Not close

2026-07-26  [momentum]  comeback / stage 1 / reflective / mo.come.25
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > The pause has run out.
2026-07-26  [banner]  weekStarting / stage 1 / reflective / bn.start.21
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > Early, before anything settles.
2026-07-26  [pulse]  accumulation / stage 3 / plain / accumulation.s3.19
  rule:  pulse.accumulation.s3.weeks
  fired: the queues have grown three weeks running, additions still exceed completions by two or more this window, at least two things were added, so no count renders as zero
  > Three weeks, all of them longer.
  > Growing, or gathering?
  ? Collecting | Storing
2026-07-26  [report headline]  balanced / stage 1 / plain / hd.bal.33
  rule:  report.headline.balanced
  fired: three or more areas had activity, no area dominates, the window holds at least 5 events, so a share is describing something real
  > No area over half.
2026-07-26  [report observation]  areaBalance / stage 1 / editorial / ob.bal.l15
  rule:  report.observation.areaBalance
  fired: three or more areas had activity, no area holds half the week, the window holds at least 6 events, so a share is describing something real
  facts: areaCount=3 [rollup.areasWithEvents]
  > Attention went to 3 places and settled in none of them.
2026-07-26  [report observation]  selfReportVsData / stage 1 / editorial / ob.srvd.l56
  rule:  report.observation.selfReportVsData
  fired: the item the user answered about is still active, the area holding the item has at least 1 events in the window, there is a stored answer to quote
  > The app is not comparing your answer to anything here. What it does is put the answer and the item side by side.
2026-07-26  [report observation]  steadyPace / stage 1 / observational / ob.stead.l55
  rule:  report.observation.steadyPace
  fired: the last three weeks sit inside a narrow band, there are at least three weeks of snapshots, without which no pattern may fire, the band is a pace rather than a run of empty weeks
  > The week held.
2026-07-26  [report observation]  areaRevival / stage 1 / observational / ob.rev.l16
  rule:  report.observation.areaRevival
  fired: this area returned after a dormancy, the area has at least 1 events in the window, so naming it cannot be a phantom claim
  facts: areaName=Someday
  > Someday is on the week's list again.
2026-07-26  [report pattern]  reportedVsActual / stage 1 / plain / pt.rva.09
  rule:  report.pattern.reportedVsActual
  fired: there are several stored answers to compare against, one family has been answered about more than twice, there are at least three weeks of snapshots, without which no pattern may fire
  > One subject, answered more than twice.

2026-07-27  [momentum]  comeback / stage 1 / reflective / mo.come.23
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > What paused is going again.
2026-07-27  [banner]  weekStarting / stage 1 / reflective / bn.start.13
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > The opening of a week.
2026-07-27  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-07-28  [momentum]  comeback / stage 1 / reflective / mo.come.61
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  facts: areaName=Someday
  > Movement in Someday on the other side of a gap.
2026-07-28  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-07-28  [pulse]  persistence / stage 2 / plain / persistence.s2.34
  rule:  pulse.persistence.s2
  fired: the active item is six to thirteen days old, the area holding the item has at least 1 events in the window
  facts: ageDays=12 [item.itemAgeDays:itm-45], itemTitle=Clear the garage shelf
  > 12 days have passed and Clear the garage shelf is where it started.
  > One task, or several?
  ? Worth the time | Worth a rethink

2026-07-29  [momentum]  comeback / stage 1 / reflective / mo.come.21
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > The thread picked up again.
2026-07-29  [banner]  weekMixed / stage 1 / reflective / bn.mixed.73
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week that piled up in one place.
2026-07-29  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-07-30  [momentum]  steadyStretch / stage 1 / reflective / mo.steady.13
  rule:  momentum.steadyStretch
  fired: active on nine or more of the last fourteen days, there are fourteen days to have been active across, the app has existed for the 14 days being described
  > A rhythm, more than a run.
2026-07-30  [banner]  weekMixed / stage 1 / reflective / bn.mixed.21
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week with more of it in one area.
2026-07-30  [pulse]  accumulation / stage 3 / plain / accumulation.s3.20
  rule:  pulse.accumulation.s3.weeks
  fired: the queues have grown three weeks running, additions still exceed completions by two or more this window, at least two things were added, so no count renders as zero
  > More waiting than yesterday.
  > Does the size of it bother you?
  ? They belong | Time to cut some

2026-07-31  [momentum]  comeback / stage 1 / reflective / mo.come.14
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > The quiet ended.
2026-07-31  [banner]  weekMixed / stage 1 / reflective / bn.mixed.17
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Spread unevenly, so far.
2026-07-31  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
```

### August 2026

31 days on screen, 17 Pulses spoken, 14 Pulse days silent, 100 sentences in all.

```text
2026-08-01  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.30
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Spread, without any one place holding it.
2026-08-01  [banner]  weekMixed / stage 1 / reflective / bn.mixed.16
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week with a heavier side.
2026-08-01  [pulse]  persistence / stage 1 / plain / persistence.s1.34
  rule:  pulse.persistence.s1
  fired: the active item is three to five days old, the area holding the item has at least 1 events in the window
  facts: ageDays=3 [item.itemAgeDays:itm-47], areaName=Work, itemTitle=Repot the balcony plants
  > Repot the balcony plants has been the first thing in Work for three days.
  > Held on purpose, or just held?
  ? Going somewhere | Going nowhere

2026-08-02  [momentum]  comeback / stage 1 / reflective / mo.come.10
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > The gap ended.
2026-08-02  [banner]  weekStarting / stage 1 / reflective / bn.start.16
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > A week still mostly ahead.
2026-08-02  [pulse]  SILENT (NO_RULE_QUALIFIED)
2026-08-02  [report headline]  mostActiveSince / stage 1 / plain / hd.since.43
  rule:  report.headline.mostActiveSince
  fired: there is an earlier week that strictly beats this one to name, this week beats the one before it, there are three weeks to reach back across
  facts: sinceRef=2026-06-21
  > June was the last one bigger.
2026-08-02  [report observation]  mostActiveSince / stage 1 / editorial / ob.since.l43
  rule:  report.observation.mostActiveSince
  fired: there is an earlier week that strictly beats this one to name, this week beats the one before it, there are three weeks to reach back across
  facts: sinceRef=2026-06-21
  > June is the last time the record went higher than this.
2026-08-02  [report observation]  areaRevival / stage 1 / observational / ob.rev.l28
  rule:  report.observation.areaRevival
  fired: this area returned after a dormancy, the area has at least 1 events in the window, so naming it cannot be a phantom claim
  facts: ageDays=7 [area.areaDormancyDays:someday], areaName=Someday
  > Someday had gone 7 days without a single event, until this week put one in it and ended the gap.
2026-08-02  [report observation]  intakeVsOutput / stage 2 / neutral_agent / ob.flow.s2.l28
  rule:  report.observation.intakeVsOutput.s2
  fired: intake exceeds output by five or more, there are additions to count
  > The list is longer than it was.
2026-08-02  [report observation]  queuePressure / stage 1 / neutral_agent / ob.qp.l17
  rule:  report.observation.queuePressure
  fired: at least one area's queue grew, the queues hold three or more things to count
  facts: areaCount=3 [rollup.areasWithQueue]
  > 3 areas hold something.
2026-08-02  [report pattern]  growingQueues / stage 1 / plain / pt.grow.05
  rule:  report.pattern.growingQueues
  fired: the queues have grown three weeks running, there are at least three weeks of snapshots, without which no pattern may fire
  > More arrives than leaves, and has for three weeks.

2026-08-03  [momentum]  comeback / stage 1 / reflective / mo.come.13
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > A restart.
2026-08-03  [banner]  weekStarting / stage 1 / reflective / bn.start.09
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > Early yet.
2026-08-03  [pulse]  accumulation / stage 3 / plain / accumulation.s3.26
  rule:  pulse.accumulation.s3.weeks
  fired: the queues have grown three weeks running, additions still exceed completions by two or more this window, at least two things were added, so no count renders as zero
  > The lists are longer than they were three weeks ago.
  > Keep it all, or cut some?
  ? Collecting | Storing

2026-08-04  [momentum]  comeback / stage 1 / reflective / mo.come.04
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > A return, after a gap.
2026-08-04  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-08-04  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-08-05  [momentum]  comeback / stage 1 / reflective / mo.come.15
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > A gap, then movement.
2026-08-05  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-08-05  [pulse]  persistence / stage 1 / plain / persistence.s1.33
  rule:  pulse.persistence.s1
  fired: the active item is three to five days old, the area holding the item has at least 1 events in the window
  facts: ageDays=3 [item.itemAgeDays:itm-50], itemTitle=Write the release notes
  > Write the release notes became active three days ago and is active still.
  > Working through it, or working around it?
  ? Moving | Parked

2026-08-06  [momentum]  comeback / stage 1 / reflective / mo.come.18
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > Under way again.
2026-08-06  [banner]  weekMixed / stage 1 / reflective / bn.mixed.21
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week with more of it in one area.
2026-08-06  [pulse]  quietDay / stage 1 / plain / quietday.s1.13
  rule:  pulse.quietDay.s1
  fired: fewer than two events in the window, per the family trigger, there is at least a day of history, so this is a quiet day and not an empty install
  > Yesterday was still.
  > Did you need it?
  ? On purpose | Other things came up | Too much on

2026-08-07  [momentum]  comeback / stage 1 / reflective / mo.come.21
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > The thread picked up again.
2026-08-07  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-08-07  [pulse]  accumulation / stage 3 / plain / accumulation.s3.16
  rule:  pulse.accumulation.s3.weeks
  fired: the queues have grown three weeks running, additions still exceed completions by two or more this window, at least two things were added, so no count renders as zero
  > The queues rose again.
  > Is this what you want it to be?
  ? There is room | It is full

2026-08-08  [momentum]  comeback / stage 1 / reflective / mo.come.12
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > Back in motion.
2026-08-08  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-08-08  [pulse]  persistence / stage 2 / plain / persistence.s2.29
  rule:  pulse.persistence.s2
  fired: the active item is six to thirteen days old, the area holding the item has at least 1 events in the window
  facts: ageDays=6 [item.itemAgeDays:itm-50]
  > six days and the same front.
  > Is it hard, or is it just sitting there?
  ? Close | Not close

2026-08-09  [momentum]  comeback / stage 1 / reflective / mo.come.07
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > Something restarted.
2026-08-09  [banner]  weekStarting / stage 1 / reflective / bn.start.21
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > Early, before anything settles.
2026-08-09  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-08-09  [report headline]  balanced / stage 1 / plain / hd.bal.22
  rule:  report.headline.balanced
  fired: three or more areas had activity, no area dominates, the window holds at least 5 events, so a share is describing something real
  > Nothing held a majority.
2026-08-09  [report observation]  areaBalance / stage 1 / editorial / ob.bal.l17
  rule:  report.observation.areaBalance
  fired: three or more areas had activity, no area holds half the week, the window holds at least 6 events, so a share is describing something real
  facts: areaCount=3 [rollup.areasWithEvents]
  > A week that touches 3 areas and gives none of them half has no single subject.
2026-08-09  [report observation]  selfReportVsData / stage 1 / editorial / ob.srvd.l05
  rule:  report.observation.selfReportVsData
  fired: the item the user answered about is still active, the area holding the item has at least 1 events in the window, there is a stored answer to quote
  facts: priorLabel=Not close
  > You described that as Not close. The week went on to prove you right.
2026-08-09  [report observation]  steadyPace / stage 1 / observational / ob.stead.l52
  rule:  report.observation.steadyPace
  fired: the last three weeks sit inside a narrow band, there are at least three weeks of snapshots, without which no pattern may fire, the band is a pace rather than a run of empty weeks
  > The pace did not change.
2026-08-09  [report observation]  intakeVsOutput / stage 2 / neutral_agent / ob.flow.s2.l25
  rule:  report.observation.intakeVsOutput.s2
  fired: intake exceeds output by five or more, there are additions to count
  facts: k=12 [rollup.queueGrowth]
  > 12 more waiting than at the start.
2026-08-09  [report pattern]  consistentRhythm / stage 1 / plain / pt.rhy.01
  rule:  report.pattern.consistentRhythm
  fired: four weeks inside a narrow band, there are four weeks to see it across, the band is a rhythm rather than a run of empty weeks
  > Your activity has stayed within a narrow band for four weeks.

2026-08-10  [momentum]  comeback / stage 1 / reflective / mo.come.61
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  facts: areaName=Someday
  > Movement in Someday on the other side of a gap.
2026-08-10  [banner]  weekStarting / stage 1 / reflective / bn.start.09
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > Early yet.
2026-08-10  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-08-11  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.30
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Spread, without any one place holding it.
2026-08-11  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-08-11  [pulse]  accumulation / stage 3 / plain / accumulation.s3.14
  rule:  pulse.accumulation.s3.weeks
  fired: the queues have grown three weeks running, additions still exceed completions by two or more this window, at least two things were added, so no count renders as zero
  > The queues are longer again.
  > Growing, or gathering?
  ? There is room | It is full

2026-08-12  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.27
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight that went in several directions.
2026-08-12  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-08-12  [pulse]  persistence / stage 2 / plain / persistence.s2.03
  rule:  pulse.persistence.s2
  fired: the active item is six to thirteen days old, the area holding the item has at least 1 events in the window
  facts: ageDays=10 [item.itemAgeDays:itm-50], itemTitle=Write the release notes
  > 10 days on Write the release notes, and counting.
  > Working through it, or working around it?
  ? Close | Not close

2026-08-13  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.14
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight in several places.
2026-08-13  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-08-13  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-08-14  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.28
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight that did not settle in one place.
2026-08-14  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-08-14  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-08-15  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.33
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight that went around rather than down.
2026-08-15  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-08-15  [pulse]  accumulation / stage 3 / plain / accumulation.s3.29
  rule:  pulse.accumulation.s3.weeks
  fired: the queues have grown three weeks running, additions still exceed completions by two or more this window, at least two things were added, so no count renders as zero
  > The lists are longer this morning than yesterday morning.
  > Does the list still describe the work?
  ? It describes the work | It has drifted

2026-08-16  [momentum]  comeback / stage 1 / reflective / mo.come.18
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > Under way again.
2026-08-16  [banner]  weekStarting / stage 1 / reflective / bn.start.23
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > Nothing here has had time to become a shape.
2026-08-16  [pulse]  SILENT (NO_RULE_QUALIFIED)
2026-08-16  [report headline]  mostActiveSince / stage 1 / plain / hd.since.07
  rule:  report.headline.mostActiveSince
  fired: there is an earlier week that strictly beats this one to name, this week beats the one before it, there are three weeks to reach back across
  facts: sinceRef=2026-07-26
  > July was the last week like this.
2026-08-16  [report observation]  mostActiveSince / stage 1 / editorial / ob.since.l45
  rule:  report.observation.mostActiveSince
  fired: there is an earlier week that strictly beats this one to name, this week beats the one before it, there are three weeks to reach back across
  facts: sinceRef=2026-07-26
  > Two facts sit side by side. This week beat last week. July still beats this week.
2026-08-16  [report observation]  areaRevival / stage 1 / observational / ob.rev.l14
  rule:  report.observation.areaRevival
  fired: this area returned after a dormancy, the area has at least 1 events in the window, so naming it cannot be a phantom claim
  facts: ageDays=7 [area.areaDormancyDays:someday]
  > 7 days of stillness, then a week with something in it.
2026-08-16  [report observation]  intakeVsOutput / stage 2 / neutral_agent / ob.flow.s2.l30
  rule:  report.observation.intakeVsOutput.s2
  fired: intake exceeds output by five or more, there are additions to count
  > More in than out.
2026-08-16  [report observation]  queuePressure / stage 1 / neutral_agent / ob.qp.l22
  rule:  report.observation.queuePressure
  fired: at least one area's queue grew, the queues hold three or more things to count
  facts: m=316 [rollup.queueTotalAtStart], n=329 [rollup.queueTotal]
  > 329 things are waiting. 316 were waiting on Sunday.
2026-08-16  [report pattern]  reportedVsActual / stage 1 / plain / pt.rva.20
  rule:  report.pattern.reportedVsActual
  fired: there are several stored answers to compare against, one family has been answered about more than twice, there are at least three weeks of snapshots, without which no pattern may fire
  > The app kept asking.

2026-08-17  [momentum]  comeback / stage 1 / reflective / mo.come.20
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > The quiet gave way.
2026-08-17  [banner]  weekStarting / stage 1 / reflective / bn.start.16
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > A week still mostly ahead.
2026-08-17  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-08-18  [momentum]  comeback / stage 1 / reflective / mo.come.12
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > Back in motion.
2026-08-18  [banner]  weekMixed / stage 1 / reflective / bn.mixed.17
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Spread unevenly, so far.
2026-08-18  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-08-19  [momentum]  comeback / stage 1 / reflective / mo.come.24
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > A stretch of nothing, then something.
2026-08-19  [banner]  weekMixed / stage 1 / reflective / bn.mixed.19
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week gathered mostly in one place.
2026-08-19  [pulse]  quietDay / stage 1 / plain / quietday.s1.01
  rule:  pulse.quietDay.s1
  fired: fewer than two events in the window, per the family trigger, there is at least a day of history, so this is a quiet day and not an empty install
  > Yesterday was quiet here.
  > Where did the day go?
  ? Needed the day | Out doing things | Could not start

2026-08-20  [momentum]  comeback / stage 1 / reflective / mo.come.15
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > A gap, then movement.
2026-08-20  [banner]  weekMixed / stage 1 / reflective / bn.mixed.73
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week that piled up in one place.
2026-08-20  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-08-21  [momentum]  comeback / stage 1 / reflective / mo.come.61
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  facts: areaName=Someday
  > Movement in Someday on the other side of a gap.
2026-08-21  [banner]  weekMixed / stage 1 / reflective / bn.mixed.17
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Spread unevenly, so far.
2026-08-21  [pulse]  accumulation / stage 3 / plain / accumulation.s3.15
  rule:  pulse.accumulation.s3.weeks
  fired: the queues have grown three weeks running, additions still exceed completions by two or more this window, at least two things were added, so no count renders as zero
  > The lists ended the week longer.
  > Too much coming in, or not enough going out?
  ? It describes the work | It has drifted

2026-08-22  [momentum]  comeback / stage 1 / reflective / mo.come.10
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > The gap ended.
2026-08-22  [banner]  weekMixed / stage 1 / reflective / bn.mixed.12
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week that leans one way.
2026-08-22  [pulse]  persistence / stage 1 / plain / persistence.s1.28
  rule:  pulse.persistence.s1
  fired: the active item is three to five days old, the area holding the item has at least 1 events in the window
  facts: ageDays=4 [item.itemAgeDays:itm-52]
  > four days, and counting.
  > Still the right thing to be on?
  ? Taking time | Taking space

2026-08-23  [momentum]  steadyStretch / stage 1 / reflective / mo.steady.22
  rule:  momentum.steadyStretch
  fired: active on nine or more of the last fourteen days, there are fourteen days to have been active across, the app has existed for the 14 days being described
  > A fortnight with few gaps.
2026-08-23  [banner]  weekStarting / stage 1 / reflective / bn.start.13
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > The opening of a week.
2026-08-23  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-08-23  [report headline]  steadyPace / stage 1 / plain / hd.steady.07
  rule:  report.headline.steadyPace
  fired: the last three weeks sit inside a narrow band, there are at least three weeks of snapshots, without which no pattern may fire, the band is a pace rather than a run of empty weeks
  > No change in tempo.
2026-08-23  [report observation]  selfReportVsData / stage 1 / editorial / ob.srvd.l39
  rule:  report.observation.selfReportVsData
  fired: the item the user answered about is still active, the area holding the item has at least 1 events in the window, there is a stored answer to quote
  facts: ageDays=5 [item.itemAgeDays:itm-52]
  > 5 days of being active, with a sentence from you about it.
2026-08-23  [report observation]  steadyPace / stage 1 / editorial / ob.stead.l46
  rule:  report.observation.steadyPace
  fired: the last three weeks sit inside a narrow band, there are at least three weeks of snapshots, without which no pattern may fire, the band is a pace rather than a run of empty weeks
  > What the app can see is that the size held. What that was like is not in the record.
2026-08-23  [report observation]  completionSplit / stage 1 / observational / ob.split.l26
  rule:  report.observation.completionSplit
  fired: three or more pulses were answered in the window, there are completions for the answers to be about
  facts: n=3 [pulse.answeredInWindow]
  > You answered 3 pulses and something got finished in the same seven days.
2026-08-23  [report observation]  intakeVsOutput / stage 2 / neutral_agent / ob.flow.s2.l22
  rule:  report.observation.intakeVsOutput.s2
  fired: intake exceeds output by five or more, there are additions to count
  > Five apart, at least.
2026-08-23  [report pattern]  consistentRhythm / stage 1 / plain / pt.rhy.34
  rule:  report.pattern.consistentRhythm
  fired: four weeks inside a narrow band, there are four weeks to see it across, the band is a rhythm rather than a run of empty weeks
  > A month with no week larger than the rest by much.

2026-08-24  [momentum]  steadyStretch / stage 1 / reflective / mo.steady.08
  rule:  momentum.steadyStretch
  fired: active on nine or more of the last fourteen days, there are fourteen days to have been active across, the app has existed for the 14 days being described
  > The last two weeks have been fairly even.
2026-08-24  [banner]  weekStarting / stage 1 / reflective / bn.start.26
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > A week still near its own beginning.
2026-08-24  [pulse]  rebalance / stage 1 / plain / rebalance.s1.10
  rule:  pulse.rebalance.s1
  fired: the area had been still five to thirteen days before it moved, the area has at least 1 events in the window, so naming it cannot be a phantom claim
  facts: areaName=Someday
  > The quiet in Someday ended yesterday.
  > Was the pause deliberate?
  ? Back to it | Just a look

2026-08-25  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.20
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > No single thing has held it.
2026-08-25  [banner]  weekStrong / stage 1 / reflective / bn.strong.15
  rule:  banner.weekStrong
  fired: completions are clearly above the recent weekly average, there are completions to count, there are earlier weeks to be above
  > Plenty already, with the week still open.
2026-08-25  [pulse]  concentration / stage 1 / plain / concentration.s1.01
  rule:  pulse.concentration.s1
  fired: the area holds seventy to eighty four percent of the window, the area has at least 4 events in the window, so its share is describing something real, the window holds at least 4 events, so a share is describing something real
  facts: areaName=Home
  > Most of yesterday happened in Home.
  > A day for one thing, or a day that became one?
  ? Felt like focus | Felt like drift

2026-08-26  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.12
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Nothing has dominated.
2026-08-26  [banner]  weekStrong / stage 1 / reflective / bn.strong.16
  rule:  banner.weekStrong
  fired: completions are clearly above the recent weekly average, there are completions to count, there are earlier weeks to be above
  > A quick week so far.
2026-08-26  [pulse]  accumulation / stage 3 / plain / accumulation.s3.21
  rule:  pulse.accumulation.s3.weeks
  fired: the queues have grown three weeks running, additions still exceed completions by two or more this window, at least two things were added, so no count renders as zero
  > The queues rose three weeks running.
  > Is there a plan for the older ones?
  ? Still a plan | A pile

2026-08-27  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.04
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A wide fortnight.
2026-08-27  [banner]  weekStrong / stage 1 / reflective / bn.strong.27
  rule:  banner.weekStrong
  fired: completions are clearly above the recent weekly average, there are completions to count, there are earlier weeks to be above
  > Above the recent weeks, and still open.
2026-08-27  [pulse]  persistence / stage 2 / plain / persistence.s2.04
  rule:  pulse.persistence.s2
  fired: the active item is six to thirteen days old, the area holding the item has at least 1 events in the window
  facts: areaName=Work, itemTitle=Schedule the eye test
  > Schedule the eye test is going into its second week in Work.
  > Is it close?
  ? Making progress | Not really

2026-08-28  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.31
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > More than one thing at a time.
2026-08-28  [banner]  weekStrong / stage 1 / reflective / bn.strong.17
  rule:  banner.weekStrong
  fired: completions are clearly above the recent weekly average, there are completions to count, there are earlier weeks to be above
  > More than the recent weeks have held.
2026-08-28  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-08-29  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.26
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Several places, none of them all of it.
2026-08-29  [banner]  weekStrong / stage 1 / reflective / bn.strong.21
  rule:  banner.weekStrong
  fired: completions are clearly above the recent weekly average, there are completions to count, there are earlier weeks to be above
  > More finished here than in most weeks.
2026-08-29  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-08-30  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.13
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Wide, across two weeks.
2026-08-30  [banner]  weekStarting / stage 1 / reflective / bn.start.13
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > The opening of a week.
2026-08-30  [pulse]  accumulation / stage 3 / plain / accumulation.s3.13
  rule:  pulse.accumulation.s3.weeks
  fired: the queues have grown three weeks running, additions still exceed completions by two or more this window, at least two things were added, so no count renders as zero
  > Longer every week for three weeks.
  > Would you write all of these again?
  ? They belong | Time to cut some
2026-08-30  [report headline]  balanced / stage 1 / plain / hd.bal.06
  rule:  report.headline.balanced
  fired: three or more areas had activity, no area dominates, the window holds at least 5 events, so a share is describing something real
  > Every area moved.
2026-08-30  [report observation]  areaBalance / stage 1 / editorial / ob.bal.l10
  rule:  report.observation.areaBalance
  fired: three or more areas had activity, no area holds half the week, the window holds at least 6 events, so a share is describing something real
  > Nothing here was the main thing.
2026-08-30  [report observation]  mostActiveSince / stage 1 / editorial / ob.since.l46
  rule:  report.observation.mostActiveSince
  fired: there is an earlier week that strictly beats this one to name, this week beats the one before it, there are three weeks to reach back across
  facts: sinceRef=2026-06-21
  > The week is not a record and it is the nearest thing to one since June.
2026-08-30  [report observation]  areaRevival / stage 1 / observational / ob.rev.l21
  rule:  report.observation.areaRevival
  fired: this area returned after a dormancy, the area has at least 1 events in the window, so naming it cannot be a phantom claim
  facts: areaName=Someday
  > Whatever had stopped in Someday started again this week.
2026-08-30  [report observation]  completionSplit / stage 1 / observational / ob.split.l32
  rule:  report.observation.completionSplit
  fired: three or more pulses were answered in the window, there are completions for the answers to be about
  facts: k=2 [pulse.flaggedInWindow], m=2 [pulse.positiveInWindow], n=4 [pulse.answeredInWindow]
  > Of the 4 answers you gave this week, 2 took the positive option and 2 took the other.
2026-08-30  [report pattern]  reportedVsActual / stage 1 / plain / pt.rva.37
  rule:  report.pattern.reportedVsActual
  fired: there are several stored answers to compare against, one family has been answered about more than twice, there are at least three weeks of snapshots, without which no pattern may fire
  > You have given this app more than a handful of answers.

2026-08-31  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.17
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Room for more than one thing.
2026-08-31  [banner]  weekStarting / stage 1 / reflective / bn.start.17
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > The first days of it.
2026-08-31  [pulse]  SILENT (NO_RULE_QUALIFIED)
```

### September 2026

30 days on screen, 21 Pulses spoken, 9 Pulse days silent, 103 sentences in all.

```text
2026-09-01  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.12
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Nothing has dominated.
2026-09-01  [banner]  weekMixed / stage 1 / reflective / bn.mixed.73
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week that piled up in one place.
2026-09-01  [pulse]  persistence / stage 1 / plain / persistence.s1.26
  rule:  pulse.persistence.s1
  fired: the active item is three to five days old, the area holding the item has at least 1 events in the window
  facts: itemTitle=Schedule the eye test
  > Schedule the eye test, day after day.
  > Deep work, or stuck?
  ? Going somewhere | Going nowhere

2026-09-02  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.14
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight in several places.
2026-09-02  [banner]  weekMixed / stage 1 / reflective / bn.mixed.23
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week that did not spread evenly.
2026-09-02  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-09-03  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.13
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Wide, across two weeks.
2026-09-03  [banner]  weekMixed / stage 1 / reflective / bn.mixed.25
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Uneven going.
2026-09-03  [pulse]  quietDay / stage 1 / plain / quietday.s1.03
  rule:  pulse.quietDay.s1
  fired: fewer than two events in the window, per the family trigger, there is at least a day of history, so this is a quiet day and not an empty install
  > One thing happened yesterday.
  > Rest, or overload?
  ? Recovered | Occupied | Worn out

2026-09-04  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.19
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Spread out, over fourteen days.
2026-09-04  [banner]  weekMixed / stage 1 / reflective / bn.mixed.24
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > The weight of the week sits in one place.
2026-09-04  [pulse]  accumulation / stage 3 / plain / accumulation.s3.17
  rule:  pulse.accumulation.s3.weeks
  fired: the queues have grown three weeks running, additions still exceed completions by two or more this window, at least two things were added, so no count renders as zero
  > The queues grew again yesterday.
  > Is anything on there done already?
  ? It does not bother me | It does

2026-09-05  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.29
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Several things at once, for two weeks.
2026-09-05  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-09-05  [pulse]  persistence / stage 2 / plain / persistence.s2.37
  rule:  pulse.persistence.s2
  fired: the active item is six to thirteen days old, the area holding the item has at least 1 events in the window
  facts: ageDays=11 [item.itemAgeDays:itm-137], areaName=Home, itemTitle=Cancel the old subscription
  > Home has been about Cancel the old subscription for 11 days now.
  > Steady, or stalled?
  ? It is big | It is blocked

2026-09-06  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.07
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Evenly spread, across two weeks.
2026-09-06  [banner]  weekStarting / stage 1 / reflective / bn.start.14
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > Too early for much of a reading.
2026-09-06  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-09-06  [report headline]  balanced / stage 1 / plain / hd.bal.57
  rule:  report.headline.balanced
  fired: three or more areas had activity, no area dominates, the window holds at least 5 events, so a share is describing something real
  > Three areas or more had something.
2026-09-06  [report observation]  areaBalance / stage 1 / editorial / ob.bal.l14
  rule:  report.observation.areaBalance
  fired: three or more areas had activity, no area holds half the week, the window holds at least 6 events, so a share is describing something real
  facts: pct=44 [area.areaShare:work]
  > 44 percent is the largest share anything took. It is not a majority.
2026-09-06  [report observation]  selfReportVsData / stage 1 / editorial / ob.srvd.l40
  rule:  report.observation.selfReportVsData
  fired: the item the user answered about is still active, the area holding the item has at least 1 events in the window, there is a stored answer to quote
  facts: priorLabel=It is blocked
  > It is blocked was true when you said it. Whether it still is, only you can say.
2026-09-06  [report observation]  areaRevival / stage 1 / observational / ob.rev.l08
  rule:  report.observation.areaRevival
  fired: this area returned after a dormancy, the area has at least 1 events in the window, so naming it cannot be a phantom claim
  facts: ageDays=5 [area.areaDormancyDays:home], areaName=Home
  > Home had been quiet for 5 days.
2026-09-06  [report observation]  intakeVsOutput / stage 2 / neutral_agent / ob.flow.s2.l14
  rule:  report.observation.intakeVsOutput.s2
  fired: intake exceeds output by five or more, there are additions to count
  > Nothing added this week has left yet.
2026-09-06  [report pattern]  growingQueues / stage 1 / plain / pt.grow.34
  rule:  report.pattern.growingQueues
  fired: the queues have grown three weeks running, there are at least three weeks of snapshots, without which no pattern may fire
  > Two weeks, two increases.

2026-09-07  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.26
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Several places, none of them all of it.
2026-09-07  [banner]  weekStarting / stage 1 / reflective / bn.start.25
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > A week at the point where nothing is decided.
2026-09-07  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-09-08  [momentum]  comeback / stage 1 / reflective / mo.come.65
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > A stretch of nothing with a finish to it.
2026-09-08  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-09-08  [pulse]  accumulation / stage 3 / plain / accumulation.s3.31
  rule:  pulse.accumulation.s3.weeks
  fired: the queues have grown three weeks running, additions still exceed completions by two or more this window, at least two things were added, so no count renders as zero
  > The lists took on more than they gave up yesterday.
  > Do you look at all of it?
  ? It does not bother me | It does

2026-09-09  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.14
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight in several places.
2026-09-09  [banner]  weekStrong / stage 1 / reflective / bn.strong.12
  rule:  banner.weekStrong
  fired: completions are clearly above the recent weekly average, there are completions to count, there are earlier weeks to be above
  > More in it than the weeks before.
2026-09-09  [pulse]  concentration / stage 1 / plain / concentration.s1.27
  rule:  pulse.concentration.s1
  fired: the area holds seventy to eighty four percent of the window, the area has at least 4 events in the window, so its share is describing something real, the window holds at least 4 events, so a share is describing something real
  facts: pct=85 [area.areaShare:home]
  > 85 percent of yesterday in one area, the rest elsewhere.
  > Was that where it needed to be?
  ? Deliberate | Just how it went

2026-09-10  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.24
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight that kept moving around.
2026-09-10  [banner]  weekStrong / stage 1 / reflective / bn.strong.18
  rule:  banner.weekStrong
  fired: completions are clearly above the recent weekly average, there are completions to count, there are earlier weeks to be above
  > Above what the recent weeks held.
2026-09-10  [pulse]  persistence / stage 2 / plain / persistence.s2.21
  rule:  pulse.persistence.s2
  fired: the active item is six to thirteen days old, the area holding the item has at least 1 events in the window
  facts: ageDays=12 [item.itemAgeDays:itm-54]
  > 12 days on the same item.
  > Moving inside, or not at all?
  ? Waiting on me | Waiting on something else

2026-09-11  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.31
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > More than one thing at a time.
2026-09-11  [banner]  weekStrong / stage 1 / reflective / bn.strong.20
  rule:  banner.weekStrong
  fired: completions are clearly above the recent weekly average, there are completions to count, there are earlier weeks to be above
  > Above your own average.
2026-09-11  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-09-12  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.23
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Nothing has held more than half.
2026-09-12  [banner]  weekStrong / stage 1 / reflective / bn.strong.17
  rule:  banner.weekStrong
  fired: completions are clearly above the recent weekly average, there are completions to count, there are earlier weeks to be above
  > More than the recent weeks have held.
2026-09-12  [pulse]  accumulation / stage 3 / plain / accumulation.s3.24
  rule:  pulse.accumulation.s3.weeks
  fired: the queues have grown three weeks running, additions still exceed completions by two or more this window, at least two things were added, so no count renders as zero
  > The queues were shorter three weeks ago.
  > Building up, or avoiding?
  ? It does not bother me | It does

2026-09-13  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.17
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Room for more than one thing.
2026-09-13  [banner]  weekStarting / stage 1 / reflective / bn.start.15
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > Nothing settled yet.
2026-09-13  [pulse]  persistence / stage 1 / plain / persistence.s1.27
  rule:  pulse.persistence.s1
  fired: the active item is three to five days old, the area holding the item has at least 1 events in the window
  facts: ageDays=4 [item.itemAgeDays:itm-154], areaName=Home
  > four days of Home on one item.
  > Chosen, or defaulted to?
  ? By choice | By default
2026-09-13  [report headline]  mostActiveSince / stage 1 / plain / hd.since.35
  rule:  report.headline.mostActiveSince
  fired: there is an earlier week that strictly beats this one to name, this week beats the one before it, there are three weeks to reach back across
  facts: sinceRef=2026-08-23
  > Larger than every week that followed August.
2026-09-13  [report observation]  persistentItem / stage 1 / neutral_agent / ob.pers.l23
  rule:  report.observation.persistentItem.high
  fired: the item has been active fourteen days or more, the area holding the item has at least 1 events in the window, there is a median completion time to compare against
  facts: areaName=Work, itemTitle=Schedule the eye test, n=222 [item.itemQueueBehind:itm-54]
  > Schedule the eye test has 222 things waiting behind it while Work moved.
2026-09-13  [report observation]  mostActiveSince / stage 1 / editorial / ob.since.l58
  rule:  report.observation.mostActiveSince
  fired: there is an earlier week that strictly beats this one to name, this week beats the one before it, there are three weeks to reach back across
  facts: sinceRef=2026-08-23
  > A week is worth naming when the reach back is long, and this reach goes to August without finding anything higher.
2026-09-13  [report observation]  selfReportVsData / stage 1 / editorial / ob.srvd.l37
  rule:  report.observation.selfReportVsData
  fired: the item the user answered about is still active, the area holding the item has at least 1 events in the window, there is a stored answer to quote
  facts: priorLabel=Waiting on something else
  > You called it Waiting on something else. It has not been swapped out since.
2026-09-13  [report observation]  completionSplit / stage 1 / observational / ob.split.l09
  rule:  report.observation.completionSplit
  fired: three or more pulses were answered in the window, there are completions for the answers to be about
  facts: n=3 [pulse.answeredInWindow]
  > 3 answers came back this week.
2026-09-13  [report pattern]  reportedVsActual / stage 1 / plain / pt.rva.54
  rule:  report.pattern.reportedVsActual
  fired: there are several stored answers to compare against, one family has been answered about more than twice, there are at least three weeks of snapshots, without which no pattern may fire
  > An answer is a reading taken at a moment, and this app now holds five of yours or more.

2026-09-14  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.14
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight in several places.
2026-09-14  [banner]  weekStarting / stage 1 / reflective / bn.start.10
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > The start of a week.
2026-09-14  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-09-15  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.29
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Several things at once, for two weeks.
2026-09-15  [banner]  weekMixed / stage 1 / reflective / bn.mixed.24
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > The weight of the week sits in one place.
2026-09-15  [pulse]  rebalance / stage 1 / plain / rebalance.s1.17
  rule:  pulse.rebalance.s1
  fired: the area had been still five to thirteen days before it moved, the area has at least 1 events in the window, so naming it cannot be a phantom claim
  facts: ageDays=9 [area.areaDormancyDays:someday], areaName=Someday
  > Something moved in Someday yesterday, after nine days.
  > Does this feel like a return?
  ? A return | A reminder

2026-09-16  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.13
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Wide, across two weeks.
2026-09-16  [banner]  weekMixed / stage 1 / reflective / bn.mixed.25
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Uneven going.
2026-09-16  [pulse]  accumulation / stage 3 / plain / accumulation.s3.01
  rule:  pulse.accumulation.s3.weeks
  fired: the queues have grown three weeks running, additions still exceed completions by two or more this window, at least two things were added, so no count renders as zero
  facts: m=1 [window.completions], n=3 [window.additions]
  > You added three things and finished one.
  > Does the list still describe the work?
  ? Building up | Avoiding

2026-09-17  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.25
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Two weeks across several areas.
2026-09-17  [banner]  weekMixed / stage 1 / reflective / bn.mixed.22
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Some places busier than others.
2026-09-17  [pulse]  persistence / stage 2 / plain / persistence.s2.39
  rule:  pulse.persistence.s2
  fired: the active item is six to thirteen days old, the area holding the item has at least 1 events in the window
  facts: ageDays=8 [item.itemAgeDays:itm-154], itemTitle=Patch the bike tire
  > Patch the bike tire arrived, and eight days later it is still the front.
  > Underway, or untouched?
  ? By choice | By default

2026-09-18  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.24
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight that kept moving around.
2026-09-18  [banner]  weekMixed / stage 1 / reflective / bn.mixed.24
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > The weight of the week sits in one place.
2026-09-18  [pulse]  quietDay / stage 1 / plain / quietday.s1.29
  rule:  pulse.quietDay.s1
  fired: fewer than two events in the window, per the family trigger, there is at least a day of history, so this is a quiet day and not an empty install
  > Nothing in any of your areas moved yesterday.
  > What kind of quiet was it?
  ? Chosen | Circumstance | Overloaded

2026-09-19  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.04
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A wide fortnight.
2026-09-19  [banner]  weekMixed / stage 1 / reflective / bn.mixed.15
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Tilted, so far.
2026-09-19  [pulse]  persistence / stage 1 / plain / persistence.s1.30
  rule:  pulse.persistence.s1
  fired: the active item is three to five days old, the area holding the item has at least 1 events in the window
  facts: ageDays=3 [item.itemAgeDays:itm-57], areaName=Work, itemTitle=Call the bank
  > Call the bank has held the front of Work for three days.
  > What is it waiting for?
  ? Taking time | Taking space

2026-09-20  [momentum]  comeback / stage 1 / reflective / mo.come.12
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > Back in motion.
2026-09-20  [banner]  weekStarting / stage 1 / reflective / bn.start.13
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > The opening of a week.
2026-09-20  [pulse]  concentration / stage 1 / plain / concentration.s1.23
  rule:  pulse.concentration.s1
  fired: the area holds seventy to eighty four percent of the window, the area has at least 4 events in the window, so its share is describing something real, the window holds at least 4 events, so a share is describing something real
  facts: areaName=Home
  > Home held the larger part of yesterday.
  > Was that where it needed to be?
  ? Same again tomorrow | Different tomorrow
2026-09-20  [report headline]  balanced / stage 1 / plain / hd.bal.19
  rule:  report.headline.balanced
  fired: three or more areas had activity, no area dominates, the window holds at least 5 events, so a share is describing something real
  > Work in more than one place.
2026-09-20  [report observation]  areaBalance / stage 1 / editorial / ob.bal.l08
  rule:  report.observation.areaBalance
  fired: three or more areas had activity, no area holds half the week, the window holds at least 6 events, so a share is describing something real
  > A week can be wide or it can be deep.
2026-09-20  [report observation]  mostActiveSince / stage 1 / editorial / ob.since.l56
  rule:  report.observation.mostActiveSince
  fired: there is an earlier week that strictly beats this one to name, this week beats the one before it, there are three weeks to reach back across
  > The record still holds a week this one did not reach, and everything after it came in lower.
2026-09-20  [report observation]  selfReportVsData / stage 1 / observational / ob.srvd.l11
  rule:  report.observation.selfReportVsData
  fired: the item the user answered about is still active, the area holding the item has at least 1 events in the window, there is a stored answer to quote
  facts: itemTitle=Call the bank
  > You answered a pulse about Call the bank. It is still the active one.
2026-09-20  [report observation]  areaRevival / stage 1 / observational / ob.rev.l30
  rule:  report.observation.areaRevival
  fired: this area returned after a dormancy, the area has at least 1 events in the window, so naming it cannot be a phantom claim
  facts: ageDays=9 [area.areaDormancyDays:someday], areaName=Someday
  > Someday was the area nothing was happening in, for 9 days, and it is not that this week.
2026-09-20  [report pattern]  growingQueues / stage 1 / plain / pt.grow.27
  rule:  report.pattern.growingQueues
  fired: the queues have grown three weeks running, there are at least three weeks of snapshots, without which no pattern may fire
  > The direction has been one way for two weeks.

2026-09-21  [momentum]  comeback / stage 1 / reflective / mo.come.14
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > The quiet ended.
2026-09-21  [banner]  weekStarting / stage 1 / reflective / bn.start.15
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > Nothing settled yet.
2026-09-21  [pulse]  accumulation / stage 3 / plain / accumulation.s3.27
  rule:  pulse.accumulation.s3.weeks
  fired: the queues have grown three weeks running, additions still exceed completions by two or more this window, at least two things were added, so no count renders as zero
  > Week on week, the queues have grown.
  > Does the size of it bother you?
  ? On purpose | By default

2026-09-22  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.17
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Room for more than one thing.
2026-09-22  [banner]  weekMixed / stage 1 / reflective / bn.mixed.09
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week with a tilt to it.
2026-09-22  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-09-23  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.21
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Across, rather than down.
2026-09-23  [banner]  weekMixed / stage 1 / reflective / bn.mixed.14
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A shape rather than a spread.
2026-09-23  [pulse]  persistence / stage 1 / plain / persistence.s1.24
  rule:  pulse.persistence.s1
  fired: the active item is three to five days old, the area holding the item has at least 1 events in the window
  facts: areaName=Home
  > One item has held Home.
  > Close, or nowhere near?
  ? Yes, still | Not any more

2026-09-24  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.22
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Two weeks that went several ways.
2026-09-24  [banner]  weekMixed / stage 1 / reflective / bn.mixed.19
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week gathered mostly in one place.
2026-09-24  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-09-25  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.25
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Two weeks across several areas.
2026-09-25  [banner]  weekMixed / stage 1 / reflective / bn.mixed.14
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A shape rather than a spread.
2026-09-25  [pulse]  quietDay / stage 1 / plain / quietday.s1.14
  rule:  pulse.quietDay.s1
  fired: fewer than two events in the window, per the family trigger, there is at least a day of history, so this is a quiet day and not an empty install
  > Nothing finished yesterday.
  > Anything happening away from here?
  ? A day off | Off the app | Underwater

2026-09-26  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.17
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Room for more than one thing.
2026-09-26  [banner]  weekMixed / stage 1 / reflective / bn.mixed.13
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week with a center of gravity in it.
2026-09-26  [pulse]  accumulation / stage 3 / plain / accumulation.s3.18
  rule:  pulse.accumulation.s3.weeks
  fired: the queues have grown three weeks running, additions still exceed completions by two or more this window, at least two things were added, so no count renders as zero
  > More is on the lists now.
  > Room for more, or full?
  ? Useful | Just growing

2026-09-27  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.10
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Broad rather than deep.
2026-09-27  [banner]  weekStarting / stage 1 / reflective / bn.start.11
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > Barely begun.
2026-09-27  [pulse]  persistence / stage 1 / plain / persistence.s1.21
  rule:  pulse.persistence.s1
  fired: the active item is three to five days old, the area holding the item has at least 1 events in the window
  facts: ageDays=5 [item.itemAgeDays:itm-59]
  > The same item, five days later.
  > Is it big, or is it just sitting there?
  ? In progress | Not started
2026-09-27  [report headline]  balanced / stage 1 / plain / hd.bal.41
  rule:  report.headline.balanced
  fired: three or more areas had activity, no area dominates, the window holds at least 5 events, so a share is describing something real
  > Attention in several places.
2026-09-27  [report observation]  areaBalance / stage 1 / editorial / ob.bal.l16
  rule:  report.observation.areaBalance
  fired: three or more areas had activity, no area holds half the week, the window holds at least 6 events, so a share is describing something real
  > Spread is not the same as balance. This week was spread.
2026-09-27  [report observation]  selfReportVsData / stage 1 / editorial / ob.srvd.l09
  rule:  report.observation.selfReportVsData
  fired: the item the user answered about is still active, the area holding the item has at least 1 events in the window, there is a stored answer to quote
  > Last time this area went quiet you called it planned. This time it lasted longer.
2026-09-27  [report observation]  areaRevival / stage 1 / observational / ob.rev.l11
  rule:  report.observation.areaRevival
  fired: this area returned after a dormancy, the area has at least 1 events in the window, so naming it cannot be a phantom claim
  facts: ageDays=7 [area.areaDormancyDays:someday], areaName=Someday
  > Someday has something in it again after 7 days.
2026-09-27  [report observation]  completionSplit / stage 1 / observational / ob.split.l12
  rule:  report.observation.completionSplit
  fired: three or more pulses were answered in the window, there are completions for the answers to be about
  facts: m=2 [pulse.positiveInWindow]
  > 2 of your answers were positive.
2026-09-27  [report pattern]  reportedVsActual / stage 1 / plain / pt.rva.49
  rule:  report.pattern.reportedVsActual
  fired: there are several stored answers to compare against, one family has been answered about more than twice, there are at least three weeks of snapshots, without which no pattern may fire
  > There are three weeks of history here and five answers or more inside them, three of them about one subject.

2026-09-28  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.14
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight in several places.
2026-09-28  [banner]  weekStarting / stage 1 / reflective / bn.start.19
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > Little to read this early.
2026-09-28  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-09-29  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.33
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight that went around rather than down.
2026-09-29  [banner]  weekMixed / stage 1 / reflective / bn.mixed.17
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Spread unevenly, so far.
2026-09-29  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-09-30  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.25
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Two weeks across several areas.
2026-09-30  [banner]  weekMixed / stage 1 / reflective / bn.mixed.24
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > The weight of the week sits in one place.
2026-09-30  [pulse]  accumulation / stage 3 / plain / accumulation.s3.28
  rule:  pulse.accumulation.s3.weeks
  fired: the queues have grown three weeks running, additions still exceed completions by two or more this window, at least two things were added, so no count renders as zero
  facts: n=2 [window.additions]
  > Yesterday put another two things on the lists.
  > Is anything on there done already?
  ? There is room | It is full
```

### October 2026

31 days on screen, 21 Pulses spoken, 10 Pulse days silent, 100 sentences in all.

```text
2026-10-01  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.17
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Room for more than one thing.
2026-10-01  [banner]  weekMixed / stage 1 / reflective / bn.mixed.14
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A shape rather than a spread.
2026-10-01  [pulse]  persistence / stage 2 / plain / persistence.s2.36
  rule:  pulse.persistence.s2
  fired: the active item is six to thirteen days old, the area holding the item has at least 1 events in the window
  facts: ageDays=11 [item.itemAgeDays:itm-174], areaName=Home
  > The front of Home has not changed in 11 days.
  > One task, or several?
  ? Close | Not close

2026-10-02  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.14
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight in several places.
2026-10-02  [banner]  weekMixed / stage 1 / reflective / bn.mixed.13
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week with a center of gravity in it.
2026-10-02  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-10-03  [momentum]  comeback / stage 1 / reflective / mo.come.27
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > A stop, then a start.
2026-10-03  [banner]  weekMixed / stage 1 / reflective / bn.mixed.12
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week that leans one way.
2026-10-03  [pulse]  quietDay / stage 1 / plain / quietday.s1.16
  rule:  pulse.quietDay.s1
  fired: fewer than two events in the window, per the family trigger, there is at least a day of history, so this is a quiet day and not an empty install
  > One quiet day.
  > Off the app, or off entirely?
  ? Deliberate | Life happened | Too much

2026-10-04  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.23
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Nothing has held more than half.
2026-10-04  [banner]  weekStarting / stage 1 / reflective / bn.start.17
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > The first days of it.
2026-10-04  [pulse]  accumulation / stage 3 / plain / accumulation.s3.30
  rule:  pulse.accumulation.s3.weeks
  fired: the queues have grown three weeks running, additions still exceed completions by two or more this window, at least two things were added, so no count renders as zero
  > Yesterday left the queues longer than it found them.
  > Would you miss any of them?
  ? It describes the work | It has drifted
2026-10-04  [report headline]  balanced / stage 1 / plain / hd.bal.02
  rule:  report.headline.balanced
  fired: three or more areas had activity, no area dominates, the window holds at least 5 events, so a share is describing something real
  > A wide week.
2026-10-04  [report observation]  areaBalance / stage 1 / editorial / ob.bal.l13
  rule:  report.observation.areaBalance
  fired: three or more areas had activity, no area holds half the week, the window holds at least 6 events, so a share is describing something real
  > Every area got some of the week and none got most of it.
2026-10-04  [report observation]  areaRevival / stage 1 / observational / ob.rev.l13
  rule:  report.observation.areaRevival
  fired: this area returned after a dormancy, the area has at least 1 events in the window, so naming it cannot be a phantom claim
  facts: areaName=Someday
  > Someday has events again.
2026-10-04  [report observation]  completionSplit / stage 1 / observational / ob.split.l16
  rule:  report.observation.completionSplit
  fired: three or more pulses were answered in the window, there are completions for the answers to be about
  facts: n=4 [pulse.answeredInWindow]
  > The app asked and you answered, 4 times.
2026-10-04  [report observation]  intakeVsOutput / stage 2 / neutral_agent / ob.flow.s2.l21
  rule:  report.observation.intakeVsOutput.s2
  fired: intake exceeds output by five or more, there are additions to count
  facts: n=13 [window.additions]
  > 13 arrived.
2026-10-04  [report pattern]  growingQueues / stage 1 / plain / pt.grow.19
  rule:  report.pattern.growingQueues
  fired: the queues have grown three weeks running, there are at least three weeks of snapshots, without which no pattern may fire
  facts: k=370 [history.weekQueueSizeAgo:2], m=382 [history.weekQueueSizeAgo:1], n=394 [history.weekQueueSizeAgo:0]
  > From 370 to 382 to 394.

2026-10-05  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.07
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Evenly spread, across two weeks.
2026-10-05  [banner]  weekStarting / stage 1 / reflective / bn.start.17
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > The first days of it.
2026-10-05  [pulse]  persistence / stage 1 / plain / persistence.s1.16
  rule:  pulse.persistence.s1
  fired: the active item is three to five days old, the area holding the item has at least 1 events in the window
  facts: itemTitle=Finish the tax folder
  > Still Finish the tax folder.
  > Underway, or untouched?
  ? It needs the time | It needs a nudge

2026-10-06  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.21
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Across, rather than down.
2026-10-06  [banner]  weekMixed / stage 1 / reflective / bn.mixed.20
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Concentrated, with the rest thinner.
2026-10-06  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-10-07  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.14
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight in several places.
2026-10-07  [banner]  weekMixed / stage 1 / reflective / bn.mixed.22
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Some places busier than others.
2026-10-07  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-10-08  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.07
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Evenly spread, across two weeks.
2026-10-08  [banner]  weekMixed / stage 1 / reflective / bn.mixed.19
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week gathered mostly in one place.
2026-10-08  [pulse]  accumulation / stage 3 / plain / accumulation.s3.23
  rule:  pulse.accumulation.s3.weeks
  fired: the queues have grown three weeks running, additions still exceed completions by two or more this window, at least two things were added, so no count renders as zero
  > Your lists have grown in each of the last three weeks.
  > Is this collecting, or is this storing?
  ? It describes the work | It has drifted

2026-10-09  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.24
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight that kept moving around.
2026-10-09  [banner]  weekMixed / stage 1 / reflective / bn.mixed.18
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Unequal, with the week still going.
2026-10-09  [pulse]  persistence / stage 1 / plain / persistence.s1.29
  rule:  pulse.persistence.s1
  fired: the active item is three to five days old, the area holding the item has at least 1 events in the window
  facts: areaName=Work, itemTitle=Read the design chapter
  > Read the design chapter is still the active item in Work.
  > Held on purpose, or just held?
  ? Going somewhere | Going nowhere

2026-10-10  [momentum]  comeback / stage 1 / reflective / mo.come.15
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > A gap, then movement.
2026-10-10  [banner]  weekMixed / stage 1 / reflective / bn.mixed.23
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week that did not spread evenly.
2026-10-10  [pulse]  quietDay / stage 1 / plain / quietday.s1.11
  rule:  pulse.quietDay.s1
  fired: fewer than two events in the window, per the family trigger, there is at least a day of history, so this is a quiet day and not an empty install
  > No completions yesterday.
  > Rested, or run down?
  ? Needed it | Doing other things | Not coping

2026-10-11  [momentum]  steadyStretch / stage 1 / reflective / mo.steady.25
  rule:  momentum.steadyStretch
  fired: active on nine or more of the last fourteen days, there are fourteen days to have been active across, the app has existed for the 14 days being described
  > Level going, across a fortnight.
2026-10-11  [banner]  weekStarting / stage 1 / reflective / bn.start.16
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > A week still mostly ahead.
2026-10-11  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-10-11  [report headline]  netInflow / stage 1 / plain / hd.in.11
  rule:  report.headline.netInflow
  fired: additions exceed completions by a clear margin, there are additions to speak of
  > Up, not down.
2026-10-11  [report observation]  completionSplit / stage 1 / observational / ob.split.l21
  rule:  report.observation.completionSplit
  fired: three or more pulses were answered in the window, there are completions for the answers to be about
  facts: n=3 [pulse.answeredInWindow]
  > The record holds 3 answers.
2026-10-11  [report observation]  intakeVsOutput / stage 2 / neutral_agent / ob.flow.s2.l35
  rule:  report.observation.intakeVsOutput.s2
  fired: intake exceeds output by five or more, there are additions to count
  > Five or more things separate what arrived this week from what left it, and seven days is where both numbers came from.
2026-10-11  [report observation]  queuePressure / stage 1 / neutral_agent / ob.qp.l14
  rule:  report.observation.queuePressure
  fired: at least one area's queue grew, the queues hold three or more things to count
  facts: areaName=Work
  > The longest queue is Work's.
2026-10-11  [report observation]  timeOfDay / stage 1 / observational / ob.tod.l16
  rule:  report.observation.timeOfDay
  fired: one part of the day holds most of the week, the week was not confined to a single part of one day
  > One part of the day did most of the work.
2026-10-11  [report pattern]  reportedVsActual / stage 1 / plain / pt.rva.39
  rule:  report.pattern.reportedVsActual
  fired: there are several stored answers to compare against, one family has been answered about more than twice, there are at least three weeks of snapshots, without which no pattern may fire
  > Three answers about one family of question, over three weeks or more.

2026-10-12  [momentum]  steadyStretch / stage 1 / reflective / mo.steady.25
  rule:  momentum.steadyStretch
  fired: active on nine or more of the last fourteen days, there are fourteen days to have been active across, the app has existed for the 14 days being described
  > Level going, across a fortnight.
2026-10-12  [banner]  weekStarting / stage 1 / reflective / bn.start.21
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > Early, before anything settles.
2026-10-12  [pulse]  accumulation / stage 3 / plain / accumulation.s3.22
  rule:  pulse.accumulation.s3.weeks
  fired: the queues have grown three weeks running, additions still exceed completions by two or more this window, at least two things were added, so no count renders as zero
  > The queues have been growing for three weeks.
  > Would you write all of these again?
  ? Keep it all | Cut some

2026-10-13  [momentum]  steadyStretch / stage 1 / reflective / mo.steady.30
  rule:  momentum.steadyStretch
  fired: active on nine or more of the last fourteen days, there are fourteen days to have been active across, the app has existed for the 14 days being described
  > A stretch with more days in it than gaps.
2026-10-13  [banner]  weekMixed / stage 1 / reflective / bn.mixed.09
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week with a tilt to it.
2026-10-13  [pulse]  persistence / stage 2 / plain / persistence.s2.01
  rule:  pulse.persistence.s2
  fired: the active item is six to thirteen days old, the area holding the item has at least 1 events in the window
  facts: ageDays=7 [item.itemAgeDays:itm-63], itemTitle=Read the design chapter
  > Still Read the design chapter. seven days now.
  > Waiting on you, or on something else?
  ? Too big | Just slow

2026-10-14  [momentum]  steadyStretch / stage 1 / reflective / mo.steady.09
  rule:  momentum.steadyStretch
  fired: active on nine or more of the last fourteen days, there are fourteen days to have been active across, the app has existed for the 14 days being described
  > Steady, across fourteen days.
2026-10-14  [banner]  weekMixed / stage 1 / reflective / bn.mixed.11
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Weighted rather than spread.
2026-10-14  [pulse]  rebalance / stage 1 / plain / rebalance.s1.18
  rule:  pulse.rebalance.s1
  fired: the area had been still five to thirteen days before it moved, the area has at least 1 events in the window, so naming it cannot be a phantom claim
  facts: ageDays=5 [area.areaDormancyDays:home], areaName=Home
  > The last event in Home before yesterday was five days ago.
  > Was the pause deliberate?
  ? Picking it up | Just checking in

2026-10-15  [momentum]  steadyStretch / stage 1 / reflective / mo.steady.21
  rule:  momentum.steadyStretch
  fired: active on nine or more of the last fourteen days, there are fourteen days to have been active across, the app has existed for the 14 days being described
  > Few empty days in it.
2026-10-15  [banner]  weekMixed / stage 1 / reflective / bn.mixed.20
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Concentrated, with the rest thinner.
2026-10-15  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-10-16  [momentum]  steadyStretch / stage 1 / reflective / mo.steady.56
  rule:  momentum.steadyStretch
  fired: active on nine or more of the last fourteen days, there are fourteen days to have been active across, the app has existed for the 14 days being described
  > A fortnight of small steady days.
2026-10-16  [banner]  weekMixed / stage 1 / reflective / bn.mixed.09
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week with a tilt to it.
2026-10-16  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-10-17  [momentum]  steadyStretch / stage 1 / reflective / mo.steady.27
  rule:  momentum.steadyStretch
  fired: active on nine or more of the last fourteen days, there are fourteen days to have been active across, the app has existed for the 14 days being described
  > Something on most of the days.
2026-10-17  [banner]  weekMixed / stage 1 / reflective / bn.mixed.17
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Spread unevenly, so far.
2026-10-17  [pulse]  quietDay / stage 1 / plain / quietday.s1.18
  rule:  pulse.quietDay.s1
  fired: fewer than two events in the window, per the family trigger, there is at least a day of history, so this is a quiet day and not an empty install
  > No swaps yesterday.
  > How was it, actually?
  ? Time off | Time elsewhere | No time at all

2026-10-18  [momentum]  steadyStretch / stage 1 / reflective / mo.steady.55
  rule:  momentum.steadyStretch
  fired: active on nine or more of the last fourteen days, there are fourteen days to have been active across, the app has existed for the 14 days being described
  > Two weeks with something in most of them.
2026-10-18  [banner]  weekStarting / stage 1 / reflective / bn.start.18
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > A beginning.
2026-10-18  [pulse]  accumulation / stage 3 / plain / accumulation.s3.12
  rule:  pulse.accumulation.s3.weeks
  fired: the queues have grown three weeks running, additions still exceed completions by two or more this window, at least two things were added, so no count renders as zero
  > Three weeks of growth.
  > Growing, or gathering?
  ? They belong | Time to cut some
2026-10-18  [report headline]  comeback / stage 1 / plain / hd.back.09
  rule:  report.headline.comeback
  fired: this area returned after a long dormancy, the area has at least 1 events in the window, so naming it cannot be a phantom claim
  facts: areaName=Home
  > Home is moving again.
2026-10-18  [report observation]  areaRevival / stage 1 / observational / ob.rev.l20
  rule:  report.observation.areaRevival
  fired: this area returned after a dormancy, the area has at least 1 events in the window, so naming it cannot be a phantom claim
  facts: ageDays=5 [area.areaDormancyDays:home], areaName=Home
  > Home started moving again after 5 days.
2026-10-18  [report observation]  intakeVsOutput / stage 2 / neutral_agent / ob.flow.s2.l33
  rule:  report.observation.intakeVsOutput.s2
  fired: intake exceeds output by five or more, there are additions to count
  facts: m=1 [window.completions], n=11 [window.additions]
  > The week took in 11 things and released 1, and the difference between those two runs to five or more.
2026-10-18  [report observation]  queuePressure / stage 1 / neutral_agent / ob.qp.l26
  rule:  report.observation.queuePressure
  fired: at least one area's queue grew, the queues hold three or more things to count
  facts: n=414 [rollup.queueTotal]
  > 414 things wait behind the items that are moving.
2026-10-18  [report observation]  timeOfDay / stage 1 / observational / ob.tod.l23
  rule:  report.observation.timeOfDay
  fired: one part of the day holds most of the week, the week was not confined to a single part of one day
  > The week gathered.
2026-10-18  [report pattern]  decliningActivity / stage 1 / plain / pt.dec.02
  rule:  report.pattern.decliningActivity
  fired: total activity has fallen three weeks running, there are at least three weeks of snapshots, without which no pattern may fire
  facts: k=19 [history.weekEventsAgo:2], m=16 [history.weekEventsAgo:1], n=15 [history.weekEventsAgo:0]
  > 19, then 16, then 15.

2026-10-19  [momentum]  steadyStretch / stage 1 / reflective / mo.steady.56
  rule:  momentum.steadyStretch
  fired: active on nine or more of the last fourteen days, there are fourteen days to have been active across, the app has existed for the 14 days being described
  > A fortnight of small steady days.
2026-10-19  [banner]  weekMixed / stage 1 / reflective / bn.mixed.14
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A shape rather than a spread.
2026-10-19  [pulse]  persistence / stage 3 / plain / persistence.s3.32
  rule:  pulse.persistence.s3
  fired: the active item is fourteen to twenty nine days old, the area holding the item has at least 1 events in the window, something else was completed, so the comparison has a number
  facts: areaName=Home
  > Home has stayed on one item.
  > Does it need time, or does it need breaking up?
  ? Yes, still right | Time to swap

2026-10-20  [momentum]  comeback / stage 1 / reflective / mo.come.12
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > Back in motion.
2026-10-20  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-10-20  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-10-21  [momentum]  comeback / stage 1 / reflective / mo.come.24
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > A stretch of nothing, then something.
2026-10-21  [banner]  weekMixed / stage 1 / reflective / bn.mixed.21
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week with more of it in one area.
2026-10-21  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-10-22  [momentum]  comeback / stage 1 / reflective / mo.come.25
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > The pause has run out.
2026-10-22  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-10-22  [pulse]  accumulation / stage 3 / plain / accumulation.s3.25
  rule:  pulse.accumulation.s3.weeks
  fired: the queues have grown three weeks running, additions still exceed completions by two or more this window, at least two things were added, so no count renders as zero
  > Each of the last three weeks left the queues longer.
  > Is the queue still a plan?
  ? Useful | Just growing

2026-10-23  [momentum]  comeback / stage 1 / reflective / mo.come.10
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > The gap ended.
2026-10-23  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-10-23  [pulse]  persistence / stage 1 / plain / persistence.s1.20
  rule:  pulse.persistence.s1
  fired: the active item is three to five days old, the area holding the item has at least 1 events in the window
  facts: itemTitle=Clear the garage shelf
  > Clear the garage shelf sits where it sat.
  > One task, or several?
  ? Going somewhere | Going nowhere

2026-10-24  [momentum]  comeback / stage 1 / reflective / mo.come.28
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > A gap with something on the other side of it.
2026-10-24  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-10-24  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-10-25  [momentum]  comeback / stage 1 / reflective / mo.come.07
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > Something restarted.
2026-10-25  [banner]  weekStarting / stage 1 / reflective / bn.start.22
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > There is more week ahead than behind.
2026-10-25  [pulse]  quietDay / stage 1 / plain / quietday.s1.20
  rule:  pulse.quietDay.s1
  fired: fewer than two events in the window, per the family trigger, there is at least a day of history, so this is a quiet day and not an empty install
  > A day with nothing in it.
  > Anything happening away from here?
  ? Deliberate | Life happened | Too much
2026-10-25  [report headline]  balanced / stage 1 / plain / hd.bal.40
  rule:  report.headline.balanced
  fired: three or more areas had activity, no area dominates, the window holds at least 5 events, so a share is describing something real
  > A week with breadth.
2026-10-25  [report observation]  areaBalance / stage 1 / editorial / ob.bal.l04
  rule:  report.observation.areaBalance
  fired: three or more areas had activity, no area holds half the week, the window holds at least 6 events, so a share is describing something real
  > No single area owned this week.
2026-10-25  [report observation]  selfReportVsData / stage 1 / editorial / ob.srvd.l45
  rule:  report.observation.selfReportVsData
  fired: the item the user answered about is still active, the area holding the item has at least 1 events in the window, there is a stored answer to quote
  facts: priorLabel=Going nowhere
  > The one thing the app knows about this item that it did not measure is Going nowhere.
2026-10-25  [report observation]  areaRevival / stage 1 / observational / ob.rev.l15
  rule:  report.observation.areaRevival
  fired: this area returned after a dormancy, the area has at least 1 events in the window, so naming it cannot be a phantom claim
  facts: ageDays=7 [area.areaDormancyDays:someday]
  > A gap of 7 days, now closed.
2026-10-25  [report observation]  completionSplit / stage 1 / observational / ob.split.l14
  rule:  report.observation.completionSplit
  fired: three or more pulses were answered in the window, there are completions for the answers to be about
  facts: priorLabel=It does not bother me
  > It does not bother me was the answer you gave most.
2026-10-25  [report pattern]  reportedVsActual / stage 1 / plain / pt.rva.41
  rule:  report.pattern.reportedVsActual
  fired: there are several stored answers to compare against, one family has been answered about more than twice, there are at least three weeks of snapshots, without which no pattern may fire
  > A question the app has asked you more than twice.

2026-10-26  [momentum]  comeback / stage 1 / reflective / mo.come.04
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > A return, after a gap.
2026-10-26  [banner]  weekStarting / stage 1 / reflective / bn.start.11
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > Barely begun.
2026-10-26  [pulse]  accumulation / stage 3 / plain / accumulation.s3.19
  rule:  pulse.accumulation.s3.weeks
  fired: the queues have grown three weeks running, additions still exceed completions by two or more this window, at least two things were added, so no count renders as zero
  > Three weeks, all of them longer.
  > Is this collecting, or is this storing?
  ? Still a plan | A pile

2026-10-27  [momentum]  comeback / stage 1 / reflective / mo.come.29
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > Something on the far side of a gap.
2026-10-27  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-10-27  [pulse]  persistence / stage 2 / plain / persistence.s2.25
  rule:  pulse.persistence.s2
  fired: the active item is six to thirteen days old, the area holding the item has at least 1 events in the window
  facts: itemTitle=Clear the garage shelf
  > Clear the garage shelf is the standing item.
  > Slow going, or not going?
  ? Too big | Just slow

2026-10-28  [momentum]  comeback / stage 1 / reflective / mo.come.04
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > A return, after a gap.
2026-10-28  [banner]  weekMixed / stage 1 / reflective / bn.mixed.11
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Weighted rather than spread.
2026-10-28  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-10-29  [momentum]  comeback / stage 1 / reflective / mo.come.19
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > A gap closed.
2026-10-29  [banner]  weekMixed / stage 1 / reflective / bn.mixed.13
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week with a center of gravity in it.
2026-10-29  [pulse]  rebalance / stage 1 / plain / rebalance.s1.23
  rule:  pulse.rebalance.s1
  fired: the area had been still five to thirteen days before it moved, the area has at least 1 events in the window, so naming it cannot be a phantom claim
  facts: areaName=Someday
  > Something in Someday yesterday.
  > Was there a moment it came back to mind?
  ? A return | A reminder

2026-10-30  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.16
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > No one place took it all.
2026-10-30  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-10-30  [pulse]  accumulation / stage 3 / plain / accumulation.s3.20
  rule:  pulse.accumulation.s3.weeks
  fired: the queues have grown three weeks running, additions still exceed completions by two or more this window, at least two things were added, so no count renders as zero
  > More waiting than yesterday.
  > Is the queue still a plan?
  ? Fine for now | Worth a clear out

2026-10-31  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.10
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Broad rather than deep.
2026-10-31  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-10-31  [pulse]  persistence / stage 1 / plain / persistence.s1.03
  rule:  pulse.persistence.s1
  fired: the active item is three to five days old, the area holding the item has at least 1 events in the window
  facts: areaName=Work, itemTitle=Clear the garage shelf
  > Clear the garage shelf is still what Work is on.
  > Is it big, or is it just sitting there?
  ? Underway | Untouched
```

### November 2026

30 days on screen, 18 Pulses spoken, 12 Pulse days silent, 100 sentences in all.

```text
2026-11-01  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.18
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Level, more or less.
2026-11-01  [banner]  weekStarting / stage 1 / reflective / bn.start.24
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > The shape of this week is still ahead.
2026-11-01  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-11-01  [report headline]  balanced / stage 1 / plain / hd.bal.17
  rule:  report.headline.balanced
  fired: three or more areas had activity, no area dominates, the window holds at least 5 events, so a share is describing something real
  > Several areas, none in charge.
2026-11-01  [report observation]  areaBalance / stage 1 / editorial / ob.bal.l09
  rule:  report.observation.areaBalance
  fired: three or more areas had activity, no area holds half the week, the window holds at least 6 events, so a share is describing something real
  facts: areaCount=3 [rollup.areasWithEvents]
  > 3 areas moved and none of them ran the week.
2026-11-01  [report observation]  selfReportVsData / stage 1 / editorial / ob.srvd.l56
  rule:  report.observation.selfReportVsData
  fired: the item the user answered about is still active, the area holding the item has at least 1 events in the window, there is a stored answer to quote
  > The app is not comparing your answer to anything here. What it does is put the answer and the item side by side.
2026-11-01  [report observation]  areaRevival / stage 1 / observational / ob.rev.l23
  rule:  report.observation.areaRevival
  fired: this area returned after a dormancy, the area has at least 1 events in the window, so naming it cannot be a phantom claim
  facts: ageDays=7 [area.areaDormancyDays:someday]
  > An area that had been quiet for 7 days was part of this week.
2026-11-01  [report observation]  completionSplit / stage 1 / observational / ob.split.l13
  rule:  report.observation.completionSplit
  fired: three or more pulses were answered in the window, there are completions for the answers to be about
  facts: n=5 [pulse.answeredInWindow]
  > The week produced 5 answers.
2026-11-01  [report pattern]  growingQueues / stage 1 / plain / pt.grow.44
  rule:  report.pattern.growingQueues
  fired: the queues have grown three weeks running, there are at least three weeks of snapshots, without which no pattern may fire
  facts: m=426 [history.weekQueueSizeAgo:1], n=438 [history.weekQueueSizeAgo:0]
  > 426 became 438.

2026-11-02  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.30
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Spread, without any one place holding it.
2026-11-02  [banner]  weekStarting / stage 1 / reflective / bn.start.18
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > A beginning.
2026-11-02  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-11-03  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.25
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Two weeks across several areas.
2026-11-03  [banner]  weekMixed / stage 1 / reflective / bn.mixed.25
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Uneven going.
2026-11-03  [pulse]  accumulation / stage 3 / plain / accumulation.s3.26
  rule:  pulse.accumulation.s3.weeks
  fired: the queues have grown three weeks running, additions still exceed completions by two or more this window, at least two things were added, so no count renders as zero
  > The lists are longer than they were three weeks ago.
  > Is anything on there done already?
  ? They belong | Time to cut some

2026-11-04  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.10
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Broad rather than deep.
2026-11-04  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-11-04  [pulse]  persistence / stage 2 / plain / persistence.s2.31
  rule:  pulse.persistence.s2
  fired: the active item is six to thirteen days old, the area holding the item has at least 1 events in the window
  facts: ageDays=7 [item.itemAgeDays:itm-68]
  > seven days and one item.
  > Steady, or stalled?
  ? One task | Several

2026-11-05  [momentum]  comeback / stage 1 / reflective / mo.come.27
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > A stop, then a start.
2026-11-05  [banner]  weekMixed / stage 1 / reflective / bn.mixed.22
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Some places busier than others.
2026-11-05  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-11-06  [momentum]  comeback / stage 1 / reflective / mo.come.21
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > The thread picked up again.
2026-11-06  [banner]  weekMixed / stage 1 / reflective / bn.mixed.73
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week that piled up in one place.
2026-11-06  [pulse]  quietDay / stage 1 / plain / quietday.s1.26
  rule:  pulse.quietDay.s1
  fired: fewer than two events in the window, per the family trigger, there is at least a day of history, so this is a quiet day and not an empty install
  > Nothing completed, nothing added, nothing swapped.
  > What was yesterday for?
  ? Recharging | Busy elsewhere | Overwhelmed

2026-11-07  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.25
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Two weeks across several areas.
2026-11-07  [banner]  weekMixed / stage 1 / reflective / bn.mixed.26
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Most of it in one area, so far.
2026-11-07  [pulse]  accumulation / stage 3 / plain / accumulation.s3.16
  rule:  pulse.accumulation.s3.weeks
  fired: the queues have grown three weeks running, additions still exceed completions by two or more this window, at least two things were added, so no count renders as zero
  > The queues rose again.
  > Too much coming in, or not enough going out?
  ? Fine for now | Worth a clear out

2026-11-08  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.32
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight with its weight spread out.
2026-11-08  [banner]  weekStarting / stage 1 / reflective / bn.start.21
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > Early, before anything settles.
2026-11-08  [pulse]  persistence / stage 1 / plain / persistence.s1.18
  rule:  pulse.persistence.s1
  fired: the active item is three to five days old, the area holding the item has at least 1 events in the window
  facts: ageDays=3 [item.itemAgeDays:itm-70]
  > three days at the front.
  > Held on purpose, or just held?
  ? Yes, still | Not any more
2026-11-08  [report headline]  balanced / stage 1 / plain / hd.bal.46
  rule:  report.headline.balanced
  fired: three or more areas had activity, no area dominates, the window holds at least 5 events, so a share is describing something real
  > No area claimed it.
2026-11-08  [report observation]  areaBalance / stage 1 / editorial / ob.bal.l17
  rule:  report.observation.areaBalance
  fired: three or more areas had activity, no area holds half the week, the window holds at least 6 events, so a share is describing something real
  facts: areaCount=3 [rollup.areasWithEvents]
  > A week that touches 3 areas and gives none of them half has no single subject.
2026-11-08  [report observation]  steadyPace / stage 1 / editorial / ob.stead.l41
  rule:  report.observation.steadyPace
  fired: the last three weeks sit inside a narrow band, there are at least three weeks of snapshots, without which no pattern may fire, the band is a pace rather than a run of empty weeks
  > Nothing here needs explaining, which is a finding rather than an absence.
2026-11-08  [report observation]  completionSplit / stage 1 / observational / ob.split.l15
  rule:  report.observation.completionSplit
  fired: three or more pulses were answered in the window, there are completions for the answers to be about
  facts: n=4 [pulse.answeredInWindow]
  > 4 readings, given as you went.
2026-11-08  [report observation]  intakeVsOutput / stage 2 / neutral_agent / ob.flow.s2.l26
  rule:  report.observation.intakeVsOutput.s2
  fired: intake exceeds output by five or more, there are additions to count
  > The queues took on more than they let go.
2026-11-08  [report pattern]  reportedVsActual / stage 1 / plain / pt.rva.27
  rule:  report.pattern.reportedVsActual
  fired: there are several stored answers to compare against, one family has been answered about more than twice, there are at least three weeks of snapshots, without which no pattern may fire
  > This was not the first asking.

2026-11-09  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.24
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight that kept moving around.
2026-11-09  [banner]  weekStarting / stage 1 / reflective / bn.start.16
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > A week still mostly ahead.
2026-11-09  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-11-10  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.33
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight that went around rather than down.
2026-11-10  [banner]  weekMixed / stage 1 / reflective / bn.mixed.20
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Concentrated, with the rest thinner.
2026-11-10  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-11-11  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.23
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Nothing has held more than half.
2026-11-11  [banner]  weekMixed / stage 1 / reflective / bn.mixed.16
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week with a heavier side.
2026-11-11  [pulse]  accumulation / stage 3 / plain / accumulation.s3.14
  rule:  pulse.accumulation.s3.weeks
  fired: the queues have grown three weeks running, additions still exceed completions by two or more this window, at least two things were added, so no count renders as zero
  > The queues are longer again.
  > A season, or how it is now?
  ? Useful | Just growing

2026-11-12  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.16
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > No one place took it all.
2026-11-12  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-11-12  [pulse]  persistence / stage 2 / plain / persistence.s2.03
  rule:  pulse.persistence.s2
  fired: the active item is six to thirteen days old, the area holding the item has at least 1 events in the window
  facts: ageDays=7 [item.itemAgeDays:itm-70], itemTitle=Call the bank
  > seven days on Call the bank, and counting.
  > Steady, or stalled?
  ? Making progress | Not really

2026-11-13  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.07
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Evenly spread, across two weeks.
2026-11-13  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-11-13  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-11-14  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.31
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > More than one thing at a time.
2026-11-14  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-11-14  [pulse]  quietDay / stage 1 / plain / quietday.s1.28
  rule:  pulse.quietDay.s1
  fired: fewer than two events in the window, per the family trigger, there is at least a day of history, so this is a quiet day and not an empty install
  > The day went by without touching this app.
  > What was yesterday for?
  ? Needed the day | Out doing things | Could not start

2026-11-15  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.26
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Several places, none of them all of it.
2026-11-15  [banner]  weekStarting / stage 1 / reflective / bn.start.18
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > A beginning.
2026-11-15  [pulse]  accumulation / stage 3 / plain / accumulation.s3.29
  rule:  pulse.accumulation.s3.weeks
  fired: the queues have grown three weeks running, additions still exceed completions by two or more this window, at least two things were added, so no count renders as zero
  > The lists are longer this morning than yesterday morning.
  > Is this collecting, or is this storing?
  ? It does not bother me | It does
2026-11-15  [report headline]  balanced / stage 1 / plain / hd.bal.08
  rule:  report.headline.balanced
  fired: three or more areas had activity, no area dominates, the window holds at least 5 events, so a share is describing something real
  > Nothing dominated.
2026-11-15  [report observation]  areaBalance / stage 1 / editorial / ob.bal.l12
  rule:  report.observation.areaBalance
  fired: three or more areas had activity, no area holds half the week, the window holds at least 6 events, so a share is describing something real
  > The week had a shape without a subject.
2026-11-15  [report observation]  selfReportVsData / stage 1 / editorial / ob.srvd.l46
  rule:  report.observation.selfReportVsData
  fired: the item the user answered about is still active, the area holding the item has at least 1 events in the window, there is a stored answer to quote
  facts: ageDays=10 [item.itemAgeDays:itm-70], itemTitle=Call the bank
  > Call the bank has been active 10 days. The answer you gave is the only reading of it anyone has.
2026-11-15  [report observation]  intakeVsOutput / stage 2 / neutral_agent / ob.flow.s2.l16
  rule:  report.observation.intakeVsOutput.s2
  fired: intake exceeds output by five or more, there are additions to count
  facts: k=12 [rollup.queueGrowth]
  > 12 net, into the queues.
2026-11-15  [report observation]  persistentItem / stage 1 / observational / ob.pers.l41
  rule:  report.observation.persistentItem.low
  fired: the item has been active seven to thirteen days, the area holding the item has at least 1 events in the window
  facts: areaName=Work, itemTitle=Call the bank
  > The rest of Work moved this week and Call the bank stayed still.
2026-11-15  [report pattern]  decliningActivity / stage 1 / plain / pt.dec.01
  rule:  report.pattern.decliningActivity
  fired: total activity has fallen three weeks running, there are at least three weeks of snapshots, without which no pattern may fire
  > Total activity has fallen three weeks running.

2026-11-16  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.27
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight that went in several directions.
2026-11-16  [banner]  weekStarting / stage 1 / reflective / bn.start.22
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > There is more week ahead than behind.
2026-11-16  [pulse]  persistence / stage 2 / plain / persistence.s2.22
  rule:  pulse.persistence.s2
  fired: the active item is six to thirteen days old, the area holding the item has at least 1 events in the window
  facts: itemTitle=Call the bank
  > Call the bank has been here a while.
  > Wanted, or just kept?
  ? It is big | It is blocked

2026-11-17  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.26
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Several places, none of them all of it.
2026-11-17  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-11-17  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-11-18  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.18
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Level, more or less.
2026-11-18  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-11-18  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-11-19  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.13
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Wide, across two weeks.
2026-11-19  [banner]  weekMixed / stage 1 / reflective / bn.mixed.17
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Spread unevenly, so far.
2026-11-19  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-11-20  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.31
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > More than one thing at a time.
2026-11-20  [banner]  weekMixed / stage 1 / reflective / bn.mixed.15
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Tilted, so far.
2026-11-20  [pulse]  accumulation / stage 3 / plain / accumulation.s3.15
  rule:  pulse.accumulation.s3.weeks
  fired: the queues have grown three weeks running, additions still exceed completions by two or more this window, at least two things were added, so no count renders as zero
  > The lists ended the week longer.
  > Would you miss any of them?
  ? Useful | Just growing

2026-11-21  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.13
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Wide, across two weeks.
2026-11-21  [banner]  weekMixed / stage 1 / reflective / bn.mixed.16
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week with a heavier side.
2026-11-21  [pulse]  rebalance / stage 1 / plain / rebalance.s1.27
  rule:  pulse.rebalance.s1
  fired: the area had been still five to thirteen days before it moved, the area has at least 1 events in the window, so naming it cannot be a phantom claim
  facts: ageDays=5 [area.areaDormancyDays:someday], areaName=Someday
  > Someday came out of five days of quiet yesterday.
  > Back for good, or a one off?
  ? A return | A reminder

2026-11-22  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.16
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > No one place took it all.
2026-11-22  [banner]  weekStarting / stage 1 / reflective / bn.start.19
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > Little to read this early.
2026-11-22  [pulse]  persistence / stage 1 / plain / persistence.s1.02
  rule:  pulse.persistence.s1
  fired: the active item is three to five days old, the area holding the item has at least 1 events in the window
  facts: ageDays=3 [item.itemAgeDays:itm-71], itemTitle=Reply to the landlord
  > three days on Reply to the landlord.
  > Still first, or not any more?
  ? It needs the time | It needs a nudge
2026-11-22  [report headline]  balanced / stage 1 / plain / hd.bal.54
  rule:  report.headline.balanced
  fired: three or more areas had activity, no area dominates, the window holds at least 5 events, so a share is describing something real
  > A week without a lead.
2026-11-22  [report observation]  areaBalance / stage 1 / editorial / ob.bal.l10
  rule:  report.observation.areaBalance
  fired: three or more areas had activity, no area holds half the week, the window holds at least 6 events, so a share is describing something real
  > Nothing here was the main thing.
2026-11-22  [report observation]  mostActiveSince / stage 1 / editorial / ob.since.l60
  rule:  report.observation.mostActiveSince
  fired: there is an earlier week that strictly beats this one to name, this week beats the one before it, there are three weeks to reach back across
  facts: sinceRef=2026-09-13
  > There is a week in September this one did not reach, and nothing between the two got higher than this.
2026-11-22  [report observation]  completionSplit / stage 1 / observational / ob.split.l20
  rule:  report.observation.completionSplit
  fired: three or more pulses were answered in the window, there are completions for the answers to be about
  facts: priorLabel=Useful
  > Useful is what you said most often this week.
2026-11-22  [report observation]  intakeVsOutput / stage 2 / neutral_agent / ob.flow.s2.l22
  rule:  report.observation.intakeVsOutput.s2
  fired: intake exceeds output by five or more, there are additions to count
  > Five apart, at least.
2026-11-22  [report pattern]  reportedVsActual / stage 1 / plain / pt.rva.29
  rule:  report.pattern.reportedVsActual
  fired: there are several stored answers to compare against, one family has been answered about more than twice, there are at least three weeks of snapshots, without which no pattern may fire
  > You have answered at least five pulses since you started.

2026-11-23  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.07
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Evenly spread, across two weeks.
2026-11-23  [banner]  weekStarting / stage 1 / reflective / bn.start.12
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > Early, with most of it ahead.
2026-11-23  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-11-24  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.13
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Wide, across two weeks.
2026-11-24  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-11-24  [pulse]  quietDay / stage 1 / plain / quietday.s1.15
  rule:  pulse.quietDay.s1
  fired: fewer than two events in the window, per the family trigger, there is at least a day of history, so this is a quiet day and not an empty install
  > Nothing arrived yesterday.
  > Choice, or circumstance?
  ? On purpose | Other things came up | Too much on

2026-11-25  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.12
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Nothing has dominated.
2026-11-25  [banner]  weekMixed / stage 1 / reflective / bn.mixed.14
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A shape rather than a spread.
2026-11-25  [pulse]  accumulation / stage 3 / plain / accumulation.s3.21
  rule:  pulse.accumulation.s3.weeks
  fired: the queues have grown three weeks running, additions still exceed completions by two or more this window, at least two things were added, so no count renders as zero
  > The queues rose three weeks running.
  > Worth a clear out?
  ? Useful | Just growing

2026-11-26  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.18
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Level, more or less.
2026-11-26  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-11-26  [pulse]  persistence / stage 2 / plain / persistence.s2.40
  rule:  pulse.persistence.s2
  fired: the active item is six to thirteen days old, the area holding the item has at least 1 events in the window
  facts: ageDays=7 [item.itemAgeDays:itm-71], areaName=Work
  > Work has spent seven days on one item.
  > Would you pick it again today?
  ? Worth the front | Worth a swap

2026-11-27  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.28
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight that did not settle in one place.
2026-11-27  [banner]  weekMixed / stage 1 / reflective / bn.mixed.16
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week with a heavier side.
2026-11-27  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-11-28  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.13
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Wide, across two weeks.
2026-11-28  [banner]  weekMixed / stage 1 / reflective / bn.mixed.25
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Uneven going.
2026-11-28  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-11-29  [momentum]  comeback / stage 1 / reflective / mo.come.20
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > The quiet gave way.
2026-11-29  [banner]  weekStarting / stage 1 / reflective / bn.start.10
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > The start of a week.
2026-11-29  [pulse]  accumulation / stage 3 / plain / accumulation.s3.13
  rule:  pulse.accumulation.s3.weeks
  fired: the queues have grown three weeks running, additions still exceed completions by two or more this window, at least two things were added, so no count renders as zero
  > Longer every week for three weeks.
  > Keep it all, or cut some?
  ? Fine for now | Worth a clear out
2026-11-29  [report headline]  balanced / stage 1 / plain / hd.bal.53
  rule:  report.headline.balanced
  fired: three or more areas had activity, no area dominates, the window holds at least 5 events, so a share is describing something real
  > Every one of them moved.
2026-11-29  [report observation]  areaBalance / stage 1 / editorial / ob.bal.l07
  rule:  report.observation.areaBalance
  fired: three or more areas had activity, no area holds half the week, the window holds at least 6 events, so a share is describing something real
  > The week had no owner.
2026-11-29  [report observation]  selfReportVsData / stage 1 / editorial / ob.srvd.l44
  rule:  report.observation.selfReportVsData
  fired: the item the user answered about is still active, the area holding the item has at least 1 events in the window, there is a stored answer to quote
  > What you said is a fact about you and the days are a fact about the item.
2026-11-29  [report observation]  areaRevival / stage 1 / observational / ob.rev.l17
  rule:  report.observation.areaRevival
  fired: this area returned after a dormancy, the area has at least 1 events in the window, so naming it cannot be a phantom claim
  facts: ageDays=7 [area.areaDormancyDays:someday], areaName=Someday
  > Nothing had happened in Someday for 7 days. This week did.
2026-11-29  [report observation]  intakeVsOutput / stage 2 / neutral_agent / ob.flow.s2.l29
  rule:  report.observation.intakeVsOutput.s2
  fired: intake exceeds output by five or more, there are additions to count
  > A collecting week.
2026-11-29  [report pattern]  growingQueues / stage 1 / plain / pt.grow.41
  rule:  report.pattern.growingQueues
  fired: the queues have grown three weeks running, there are at least three weeks of snapshots, without which no pattern may fire
  > The queues have not given anything back.

2026-11-30  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.19
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Spread out, over fourteen days.
2026-11-30  [banner]  weekStarting / stage 1 / reflective / bn.start.11
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > Barely begun.
2026-11-30  [pulse]  persistence / stage 2 / plain / persistence.s2.23
  rule:  pulse.persistence.s2
  fired: the active item is six to thirteen days old, the area holding the item has at least 1 events in the window
  facts: ageDays=11 [item.itemAgeDays:itm-71], itemTitle=Reply to the landlord
  > Reply to the landlord again, 11 days on.
  > One task, or several?
  ? Too big | Just slow
```

### December 2026

31 days on screen, 16 Pulses spoken, 15 Pulse days silent, 89 sentences in all.

```text
2026-12-01  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.27
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight that went in several directions.
2026-12-01  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-12-01  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-12-02  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.10
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Broad rather than deep.
2026-12-02  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-12-02  [pulse]  quietDay / stage 1 / plain / quietday.s1.01
  rule:  pulse.quietDay.s1
  fired: fewer than two events in the window, per the family trigger, there is at least a day of history, so this is a quiet day and not an empty install
  > Yesterday was quiet here.
  > How was it, actually?
  ? On purpose | Other things came up | Too much on

2026-12-03  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.33
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight that went around rather than down.
2026-12-03  [banner]  weekMixed / stage 1 / reflective / bn.mixed.23
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week that did not spread evenly.
2026-12-03  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-12-04  [momentum]  comeback / stage 1 / reflective / mo.come.30
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > A pause with something after it.
2026-12-04  [banner]  weekMixed / stage 1 / reflective / bn.mixed.14
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A shape rather than a spread.
2026-12-04  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-12-05  [momentum]  comeback / stage 1 / reflective / mo.come.13
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > A restart.
2026-12-05  [banner]  weekMixed / stage 1 / reflective / bn.mixed.17
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Spread unevenly, so far.
2026-12-05  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-12-06  [momentum]  comeback / stage 1 / reflective / mo.come.04
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > A return, after a gap.
2026-12-06  [banner]  weekStarting / stage 1 / reflective / bn.start.14
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > Too early for much of a reading.
2026-12-06  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-12-06  [report headline]  mostActiveSince / stage 1 / plain / hd.since.23
  rule:  report.headline.mostActiveSince
  fired: there is an earlier week that strictly beats this one to name, this week beats the one before it, there are three weeks to reach back across
  facts: sinceRef=2026-09-13
  > Above every week since September.
2026-12-06  [report observation]  mostActiveSince / stage 1 / editorial / ob.since.l58
  rule:  report.observation.mostActiveSince
  fired: there is an earlier week that strictly beats this one to name, this week beats the one before it, there are three weeks to reach back across
  facts: sinceRef=2026-09-13
  > A week is worth naming when the reach back is long, and this reach goes to September without finding anything higher.
2026-12-06  [report observation]  intakeVsOutput / stage 2 / neutral_agent / ob.flow.s2.l28
  rule:  report.observation.intakeVsOutput.s2
  fired: intake exceeds output by five or more, there are additions to count
  > The list is longer than it was.
2026-12-06  [report observation]  queuePressure / stage 1 / neutral_agent / ob.qp.l15
  rule:  report.observation.queuePressure
  fired: at least one area's queue grew, the queues hold three or more things to count
  > At least one queue got longer.
2026-12-06  [report observation]  timeOfDay / stage 1 / observational / ob.tod.l25
  rule:  report.observation.timeOfDay
  fired: one part of the day holds most of the week, the week was not confined to a single part of one day
  > One part of the day did half of it.
2026-12-06  [report pattern]  reportedVsActual / stage 1 / plain / pt.rva.51
  rule:  report.pattern.reportedVsActual
  fired: there are several stored answers to compare against, one family has been answered about more than twice, there are at least three weeks of snapshots, without which no pattern may fire
  > Three answers about one kind of moment is not a verdict on anything, and it is a record that the moment kept happening.

2026-12-07  [momentum]  comeback / stage 1 / reflective / mo.come.17
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > The pause is over.
2026-12-07  [banner]  weekStarting / stage 1 / reflective / bn.start.14
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > Too early for much of a reading.
2026-12-07  [pulse]  accumulation / stage 3 / plain / accumulation.s3.17
  rule:  pulse.accumulation.s3.weeks
  fired: the queues have grown three weeks running, additions still exceed completions by two or more this window, at least two things were added, so no count renders as zero
  > The queues grew again yesterday.
  > Too much coming in, or not enough going out?
  ? It does not bother me | It does

2026-12-08  [momentum]  comeback / stage 1 / reflective / mo.come.14
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > The quiet ended.
2026-12-08  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-12-08  [pulse]  persistence / stage 1 / plain / persistence.s1.25
  rule:  pulse.persistence.s1
  fired: the active item is three to five days old, the area holding the item has at least 1 events in the window
  facts: itemTitle=Draft the quarterly summary
  > Draft the quarterly summary was here yesterday too.
  > Part done, or not begun?
  ? Taking time | Taking space

2026-12-09  [momentum]  comeback / stage 1 / reflective / mo.come.25
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > The pause has run out.
2026-12-09  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-12-09  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-12-10  [momentum]  comeback / stage 1 / reflective / mo.come.13
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > A restart.
2026-12-10  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-12-10  [pulse]  quietDay / stage 1 / plain / quietday.s1.21
  rule:  pulse.quietDay.s1
  fired: fewer than two events in the window, per the family trigger, there is at least a day of history, so this is a quiet day and not an empty install
  > Yesterday left things as they were.
  > One of those days?
  ? Recovered | Occupied | Worn out

2026-12-11  [momentum]  steadyStretch / stage 1 / reflective / mo.steady.55
  rule:  momentum.steadyStretch
  fired: active on nine or more of the last fourteen days, there are fourteen days to have been active across, the app has existed for the 14 days being described
  > Two weeks with something in most of them.
2026-12-11  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-12-11  [pulse]  persistence / stage 2 / plain / persistence.s2.04
  rule:  pulse.persistence.s2
  fired: the active item is six to thirteen days old, the area holding the item has at least 1 events in the window
  facts: areaName=Work, itemTitle=Draft the quarterly summary
  > Draft the quarterly summary is going into its second week in Work.
  > Is it close?
  ? Waiting on me | Waiting on something else

2026-12-12  [momentum]  steadyStretch / stage 1 / reflective / mo.steady.29
  rule:  momentum.steadyStretch
  fired: active on nine or more of the last fourteen days, there are fourteen days to have been active across, the app has existed for the 14 days being described
  > Two weeks of fairly even going.
2026-12-12  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-12-12  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-12-13  [momentum]  steadyStretch / stage 1 / reflective / mo.steady.13
  rule:  momentum.steadyStretch
  fired: active on nine or more of the last fourteen days, there are fourteen days to have been active across, the app has existed for the 14 days being described
  > A rhythm, more than a run.
2026-12-13  [banner]  weekStarting / stage 1 / reflective / bn.start.23
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > Nothing here has had time to become a shape.
2026-12-13  [pulse]  accumulation / stage 3 / plain / accumulation.s3.31
  rule:  pulse.accumulation.s3.weeks
  fired: the queues have grown three weeks running, additions still exceed completions by two or more this window, at least two things were added, so no count renders as zero
  > The lists took on more than they gave up yesterday.
  > Is this what you want it to be?
  ? On purpose | By default
2026-12-13  [report headline]  balanced / stage 1 / plain / hd.bal.23
  rule:  report.headline.balanced
  fired: three or more areas had activity, no area dominates, the window holds at least 5 events, so a share is describing something real
  > A week split several ways.
2026-12-13  [report observation]  areaBalance / stage 1 / editorial / ob.bal.l11
  rule:  report.observation.areaBalance
  fired: three or more areas had activity, no area holds half the week, the window holds at least 6 events, so a share is describing something real
  > A wide week and a thin week look the same from a count, and nothing here tells them apart.
2026-12-13  [report observation]  selfReportVsData / stage 1 / editorial / ob.srvd.l33
  rule:  report.observation.selfReportVsData
  fired: the item the user answered about is still active, the area holding the item has at least 1 events in the window, there is a stored answer to quote
  facts: itemTitle=Draft the quarterly summary, priorLabel=Waiting on something else
  > Waiting on something else was a reading taken at a moment. Draft the quarterly summary is still active.
2026-12-13  [report observation]  steadyPace / stage 1 / observational / ob.stead.l50
  rule:  report.observation.steadyPace
  fired: the last three weeks sit inside a narrow band, there are at least three weeks of snapshots, without which no pattern may fire, the band is a pace rather than a run of empty weeks
  > No week outgrew the others.
2026-12-13  [report observation]  areaRevival / stage 1 / observational / ob.rev.l31
  rule:  report.observation.areaRevival
  fired: this area returned after a dormancy, the area has at least 1 events in the window, so naming it cannot be a phantom claim
  facts: ageDays=7 [area.areaDormancyDays:someday], areaName=Someday
  > An area can be quiet for 7 days and then not be, and Someday is the one that did that here.
2026-12-13  [report pattern]  growingQueues / stage 1 / plain / pt.grow.40
  rule:  report.pattern.growingQueues
  fired: the queues have grown three weeks running, there are at least three weeks of snapshots, without which no pattern may fire
  > The last two weeks both ended with more.

2026-12-14  [momentum]  comeback / stage 1 / reflective / mo.come.24
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > A stretch of nothing, then something.
2026-12-14  [banner]  weekStarting / stage 1 / reflective / bn.start.16
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > A week still mostly ahead.
2026-12-14  [pulse]  persistence / stage 2 / plain / persistence.s2.29
  rule:  pulse.persistence.s2
  fired: the active item is six to thirteen days old, the area holding the item has at least 1 events in the window
  facts: ageDays=10 [item.itemAgeDays:itm-74]
  > 10 days and the same front.
  > Would you pick it again today?
  ? Worth the time | Worth a rethink

2026-12-15  [momentum]  comeback / stage 1 / reflective / mo.come.29
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > Something on the far side of a gap.
2026-12-15  [banner]  weekMixed / stage 1 / reflective / bn.mixed.21
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week with more of it in one area.
2026-12-15  [pulse]  quietDay / stage 1 / plain / quietday.s1.13
  rule:  pulse.quietDay.s1
  fired: fewer than two events in the window, per the family trigger, there is at least a day of history, so this is a quiet day and not an empty install
  > Yesterday was still.
  > Needed, or not?
  ? Slow on purpose | Slow by circumstance | Stuck

2026-12-16  [momentum]  comeback / stage 1 / reflective / mo.come.21
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > The thread picked up again.
2026-12-16  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-12-16  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-12-17  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.12
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Nothing has dominated.
2026-12-17  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-12-17  [pulse]  accumulation / stage 3 / plain / accumulation.s3.24
  rule:  pulse.accumulation.s3.weeks
  fired: the queues have grown three weeks running, additions still exceed completions by two or more this window, at least two things were added, so no count renders as zero
  > The queues were shorter three weeks ago.
  > Is this what you want it to be?
  ? A season | How it is now

2026-12-18  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.25
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Two weeks across several areas.
2026-12-18  [banner]  weekMixed / stage 1 / reflective / bn.mixed.17
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Spread unevenly, so far.
2026-12-18  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-12-19  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.12
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Nothing has dominated.
2026-12-19  [banner]  weekMixed / stage 1 / reflective / bn.mixed.18
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Unequal, with the week still going.
2026-12-19  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-12-20  [momentum]  comeback / stage 1 / reflective / mo.come.66
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > Something that had stopped is going again.
2026-12-20  [banner]  weekStarting / stage 1 / reflective / bn.start.13
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > The opening of a week.
2026-12-20  [pulse]  SILENT (NO_RULE_QUALIFIED)
2026-12-20  [report headline]  mostActiveSince / stage 1 / plain / hd.since.12
  rule:  report.headline.mostActiveSince
  fired: there is an earlier week that strictly beats this one to name, this week beats the one before it, there are three weeks to reach back across
  facts: sinceRef=2026-09-13
  > September was the last week above this.
2026-12-20  [report observation]  mostActiveSince / stage 1 / editorial / ob.since.l41
  rule:  report.observation.mostActiveSince
  fired: there is an earlier week that strictly beats this one to name, this week beats the one before it, there are three weeks to reach back across
  facts: sinceRef=2026-09-13
  > A week can be the biggest since September and still not be the biggest.
2026-12-20  [report observation]  completionSplit / stage 1 / observational / ob.split.l12
  rule:  report.observation.completionSplit
  fired: three or more pulses were answered in the window, there are completions for the answers to be about
  facts: m=2 [pulse.positiveInWindow]
  > 2 of your answers were positive.
2026-12-20  [report observation]  intakeVsOutput / stage 2 / neutral_agent / ob.flow.s2.l11
  rule:  report.observation.intakeVsOutput.s2
  fired: intake exceeds output by five or more, there are additions to count
  facts: m=2 [window.completions], n=11 [window.additions]
  > The queues took on 11 and released 2.
2026-12-20  [report observation]  queuePressure / stage 1 / neutral_agent / ob.qp.l13
  rule:  report.observation.queuePressure
  fired: at least one area's queue grew, the queues hold three or more things to count
  facts: areaCount=3 [rollup.areasWithQueue], n=518 [rollup.queueTotal]
  > 518 waiting, across 3 areas.
2026-12-20  [report pattern]  reportedVsActual / stage 1 / plain / pt.rva.46
  rule:  report.pattern.reportedVsActual
  fired: there are several stored answers to compare against, one family has been answered about more than twice, there are at least three weeks of snapshots, without which no pattern may fire
  > The same situation has come up enough times to have answers stacked against it.

2026-12-21  [momentum]  comeback / stage 1 / reflective / mo.come.61
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  facts: areaName=Home
  > Movement in Home on the other side of a gap.
2026-12-21  [banner]  weekStarting / stage 1 / reflective / bn.start.23
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > Nothing here has had time to become a shape.
2026-12-21  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-12-22  [momentum]  comeback / stage 1 / reflective / mo.come.25
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > The pause has run out.
2026-12-22  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-12-22  [pulse]  rebalance / stage 1 / plain / rebalance.s1.15
  rule:  pulse.rebalance.s1
  fired: the area had been still five to thirteen days before it moved, the area has at least 1 events in the window, so naming it cannot be a phantom claim
  facts: ageDays=6 [area.areaDormancyDays:someday], areaName=Someday
  > The gap in Someday was six days long.
  > Is this still an area you use?
  ? Still mine | Not sure any more

2026-12-23  [momentum]  comeback / stage 1 / reflective / mo.come.18
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > Under way again.
2026-12-23  [banner]  weekMixed / stage 1 / reflective / bn.mixed.19
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week gathered mostly in one place.
2026-12-23  [pulse]  persistence / stage 1 / plain / persistence.s1.33
  rule:  pulse.persistence.s1
  fired: the active item is three to five days old, the area holding the item has at least 1 events in the window
  facts: ageDays=3 [item.itemAgeDays:itm-77], itemTitle=Repot the balcony plants
  > Repot the balcony plants became active three days ago and is active still.
  > Chosen, or defaulted to?
  ? Going somewhere | Going nowhere

2026-12-24  [momentum]  comeback / stage 1 / reflective / mo.come.66
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > Something that had stopped is going again.
2026-12-24  [banner]  weekMixed / stage 1 / reflective / bn.mixed.22
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Some places busier than others.
2026-12-24  [pulse]  quietDay / stage 1 / plain / quietday.s1.24
  rule:  pulse.quietDay.s1
  fired: fewer than two events in the window, per the family trigger, there is at least a day of history, so this is a quiet day and not an empty install
  > Yesterday came and went with nothing logged.
  > Full day, or empty one?
  ? Needed it | Doing other things | Not coping

2026-12-25  [momentum]  comeback / stage 1 / reflective / mo.come.18
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > Under way again.
2026-12-25  [banner]  weekMixed / stage 1 / reflective / bn.mixed.20
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Concentrated, with the rest thinner.
2026-12-25  [pulse]  accumulation / stage 3 / plain / accumulation.s3.18
  rule:  pulse.accumulation.s3.weeks
  fired: the queues have grown three weeks running, additions still exceed completions by two or more this window, at least two things were added, so no count renders as zero
  > More is on the lists now.
  > Is this collecting, or is this storing?
  ? On purpose | By default

2026-12-26  [momentum]  steadyStretch / stage 1 / reflective / mo.steady.24
  rule:  momentum.steadyStretch
  fired: active on nine or more of the last fourteen days, there are fourteen days to have been active across, the app has existed for the 14 days being described
  > A stretch that held.
2026-12-26  [banner]  weekMixed / stage 1 / reflective / bn.mixed.12
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week that leans one way.
2026-12-26  [pulse]  persistence / stage 2 / plain / persistence.s2.24
  rule:  pulse.persistence.s2
  fired: the active item is six to thirteen days old, the area holding the item has at least 1 events in the window
  facts: ageDays=6 [item.itemAgeDays:itm-77], areaName=Work, itemTitle=Repot the balcony plants
  > six days of Repot the balcony plants in Work.
  > Is it close?
  ? Waiting on me | Waiting on something else

2026-12-27  [momentum]  steadyStretch / stage 1 / reflective / mo.steady.08
  rule:  momentum.steadyStretch
  fired: active on nine or more of the last fourteen days, there are fourteen days to have been active across, the app has existed for the 14 days being described
  > The last two weeks have been fairly even.
2026-12-27  [banner]  weekStarting / stage 1 / reflective / bn.start.15
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > Nothing settled yet.
2026-12-27  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-12-27  [report headline]  balanced / stage 1 / plain / hd.bal.45
  rule:  report.headline.balanced
  fired: three or more areas had activity, no area dominates, the window holds at least 5 events, so a share is describing something real
  > The week never narrowed.
2026-12-27  [report observation]  areaBalance / stage 1 / editorial / ob.bal.l08
  rule:  report.observation.areaBalance
  fired: three or more areas had activity, no area holds half the week, the window holds at least 6 events, so a share is describing something real
  > A week can be wide or it can be deep.
2026-12-27  [report observation]  selfReportVsData / stage 1 / editorial / ob.srvd.l35
  rule:  report.observation.selfReportVsData
  fired: the item the user answered about is still active, the area holding the item has at least 1 events in the window, there is a stored answer to quote
  facts: itemTitle=Repot the balcony plants
  > What you said about Repot the balcony plants may still be true. This is only a note that you said it.
2026-12-27  [report observation]  areaRevival / stage 1 / observational / ob.rev.l06
  rule:  report.observation.areaRevival
  fired: this area returned after a dormancy, the area has at least 1 events in the window, so naming it cannot be a phantom claim
  facts: areaName=Someday
  > Someday had been the quietest area. It was not this week.
2026-12-27  [report observation]  intakeVsOutput / stage 2 / neutral_agent / ob.flow.s2.l30
  rule:  report.observation.intakeVsOutput.s2
  fired: intake exceeds output by five or more, there are additions to count
  > More in than out.
2026-12-27  [report pattern]  growingQueues / stage 1 / plain / pt.grow.60
  rule:  report.pattern.growingQueues
  fired: the queues have grown three weeks running, there are at least three weeks of snapshots, without which no pattern may fire
  > Two weekly readings in a row came in above the one before them, which is the whole of what this section is saying.

2026-12-28  [momentum]  steadyStretch / stage 1 / reflective / mo.steady.30
  rule:  momentum.steadyStretch
  fired: active on nine or more of the last fourteen days, there are fourteen days to have been active across, the app has existed for the 14 days being described
  > A stretch with more days in it than gaps.
2026-12-28  [banner]  weekStarting / stage 1 / reflective / bn.start.12
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > Early, with most of it ahead.
2026-12-28  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-12-29  [momentum]  comeback / stage 1 / reflective / mo.come.16
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > Movement, after a pause.
2026-12-29  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-12-29  [pulse]  accumulation / stage 3 / plain / accumulation.s3.28
  rule:  pulse.accumulation.s3.weeks
  fired: the queues have grown three weeks running, additions still exceed completions by two or more this window, at least two things were added, so no count renders as zero
  facts: n=2 [window.additions]
  > Yesterday put another two things on the lists.
  > Has this been useful, or just growing?
  ? On purpose | By default

2026-12-30  [momentum]  comeback / stage 1 / reflective / mo.come.15
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > A gap, then movement.
2026-12-30  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-12-30  [pulse]  persistence / stage 2 / plain / persistence.s2.19
  rule:  pulse.persistence.s2
  fired: the active item is six to thirteen days old, the area holding the item has at least 1 events in the window
  facts: ageDays=10 [item.itemAgeDays:itm-77]
  > 10 days and holding.
  > Does it still belong at the front?
  ? Yes, still right | Time to swap

2026-12-31  [momentum]  comeback / stage 1 / reflective / mo.come.28
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > A gap with something on the other side of it.
2026-12-31  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-12-31  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
```

### January 2027

3 days on screen, 2 Pulses spoken, 1 Pulse days silent, 12 sentences in all.

```text
2027-01-01  [momentum]  comeback / stage 1 / reflective / mo.come.12
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > Back in motion.
2027-01-01  [banner]  SILENT (NO_RULE_QUALIFIED)
2027-01-01  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2027-01-02  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.27
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight that went in several directions.
2027-01-02  [banner]  SILENT (NO_RULE_QUALIFIED)
2027-01-02  [pulse]  quietDay / stage 1 / plain / quietday.s1.27
  rule:  pulse.quietDay.s1
  fired: fewer than two events in the window, per the family trigger, there is at least a day of history, so this is a quiet day and not an empty install
  > Yesterday has little to describe.
  > What was yesterday for?
  ? Rested | Busy elsewhere | Running on empty

2027-01-03  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.16
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > No one place took it all.
2027-01-03  [banner]  weekStarting / stage 1 / reflective / bn.start.14
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > Too early for much of a reading.
2027-01-03  [pulse]  accumulation / stage 3 / plain / accumulation.s3.30
  rule:  pulse.accumulation.s3.weeks
  fired: the queues have grown three weeks running, additions still exceed completions by two or more this window, at least two things were added, so no count renders as zero
  > Yesterday left the queues longer than it found them.
  > Does the list still describe the work?
  ? They belong | Time to cut some
2027-01-03  [report headline]  balanced / stage 1 / plain / hd.bal.51
  rule:  report.headline.balanced
  fired: three or more areas had activity, no area dominates, the window holds at least 5 events, so a share is describing something real
  > Several areas had a share.
2027-01-03  [report observation]  areaBalance / stage 1 / editorial / ob.bal.l14
  rule:  report.observation.areaBalance
  fired: three or more areas had activity, no area holds half the week, the window holds at least 6 events, so a share is describing something real
  facts: pct=44 [area.areaShare:work]
  > 44 percent is the largest share anything took. It is not a majority.
2027-01-03  [report observation]  intakeVsOutput / stage 2 / neutral_agent / ob.flow.s2.l31
  rule:  report.observation.intakeVsOutput.s2
  fired: intake exceeds output by five or more, there are additions to count
  facts: k=13 [rollup.queueGrowth], n=13 [window.additions]
  > 13 things came in over seven days and the lists are 13 longer for it.
2027-01-03  [report observation]  queuePressure / stage 1 / neutral_agent / ob.qp.l20
  rule:  report.observation.queuePressure
  fired: at least one area's queue grew, the queues hold three or more things to count
  facts: n=544 [rollup.queueTotal]
  > The queues hold 544 things and one of them got longer.
2027-01-03  [report observation]  timeOfDay / stage 1 / observational / ob.tod.l12
  rule:  report.observation.timeOfDay
  fired: one part of the day holds most of the week, the week was not confined to a single part of one day
  > Your week has a preferred time.
2027-01-03  [report pattern]  reportedVsActual / stage 1 / plain / pt.rva.34
  rule:  report.pattern.reportedVsActual
  fired: there are several stored answers to compare against, one family has been answered about more than twice, there are at least three weeks of snapshots, without which no pattern may fire
  > Five answers or more sit in the record now.
```

### The year in counts

```text
-- counts ------------------------------------------------------
pulse: 365 days, 228 spoken, 137 silent (37 percent silent)
silence reasons: ALL_QUALIFIED_RULES_FILTERED 97, NO_RULE_QUALIFIED 39, INSUFFICIENT_DATA 1
pulse families: persistence 87, accumulation 80, quietDay 37, rebalance 11, concentration 9, freshStart 3, spread 1
pulse stages:   accumulation.s3 78, persistence.s2 41, quietDay.s1 37, persistence.s1 29, persistence.s4 13, rebalance.s1 11, concentration.s1 7, persistence.s3 4, freshStart.s1 3, accumulation.s1 2, concentration.s2 2, spread.s1 1
momentum: 365 invocations, 364 spoken
  families: balancedWeek 189, comeback 124, steadyStretch 38, firstDays 13
banner: 365 invocations, 252 spoken
  families: weekMixed 127, weekStarting 102, weekStrong 23
report headline: 52 invocations, 52 spoken
  families: balanced 29, mostActiveSince 10, comeback 6, netInflow 3, personalBest 3, steadyPace 1
report observation: 208 invocations, 208 spoken
  families: intakeVsOutput 36, areaRevival 33, areaBalance 31, selfReportVsData 27, completionSplit 21, mostActiveSince 14, queuePressure 14, persistentItem 12, steadyPace 7, timeOfDay 6, personalBest 3, dayShape 2, firstMilestone 1, neglectedArea 1
report pattern: 49 invocations, 49 spoken
  families: reportedVsActual 24, growingQueues 19, consistentRhythm 3, decliningActivity 3
layer 5 vetoes: 0
distinct variants used: 485
```

## 2. Abandoning, the difficult year

Strong for two months, then trailing away. Daily until the start of March, then about
one day in three for the remaining ten months, and from the end of May nothing is
captured but a residue: 153 opens against 365 days.
**Read this one imagining it is your own worst year.** It is where `neglectedArea`
speaks 13 times, `areaGoneQuiet` 9, and where the difficulty register of 6.4 fires once.
It is also 88 percent silent, which is the other half of the same design: on a week
with nothing in it the app mostly says nothing, and the question is whether the times
it does speak earn it.

**`hardStretch` in this year.** 2026-10-25, `ob.hard.l07`, "A period like this is not a failure of the system you set up". It is banner flagged in place below.

```text
persona: abandoning, Abandoning
why:     Strong for two months, then trailing away. Feeds decliningActivity, focusAbandonment, hardStretch and the difficulty register in 6.4.
span:    365 simulated days, 153 opens, 574 events, 553 engine invocations
```

### January 2026

28 days on screen, 5 Pulses spoken, 23 Pulse days silent, 67 sentences in all.

```text
2026-01-04  [momentum]  SILENT (INSUFFICIENT_DATA)
2026-01-04  [banner]  weekStarting / stage 1 / reflective / bn.start.11
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > Barely begun.
2026-01-04  [pulse]  SILENT (INSUFFICIENT_DATA)

2026-01-05  [momentum]  firstDays / stage 1 / reflective / mo.first.22
  rule:  momentum.firstDays
  fired: fewer than fourteen days since install, at least one event, so this is a beginning rather than an empty app
  > There are not many days here to look at.
2026-01-05  [banner]  weekStarting / stage 1 / reflective / bn.start.15
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > Nothing settled yet.
2026-01-05  [pulse]  spread / stage 1 / plain / spread.s1.10
  rule:  pulse.spread.s1
  fired: exactly three areas had events, no area is above half the window, the window holds at least 5 events, so a share is describing something real
  facts: areaCount=3 [rollup.areasWithEvents], n=9 [window.totalEvents]
  > three areas, nine moves, no clear center.
  > Was that a good shape for the day?
  ? Felt manageable | Felt stretched

2026-01-06  [momentum]  firstDays / stage 1 / reflective / mo.first.30
  rule:  momentum.firstDays
  fired: fewer than fourteen days since install, at least one event, so this is a beginning rather than an empty app
  > Early, with the rest of it ahead.
2026-01-06  [banner]  weekBuilding / stage 1 / reflective / bn.build.32
  rule:  banner.weekBuilding
  fired: three to five days into the week, completions are accumulating
  > Something behind it, more of it ahead.
2026-01-06  [pulse]  freshStart / stage 1 / plain / freshstart.s1.02
  rule:  pulse.freshStart.s1
  fired: the area is one this window began or reopened, the area has at least 1 events in the window, so naming it cannot be a phantom claim
  facts: areaName=Side project
  > You added Side project.
  > Is this a big one?
  ? A big one | A small one

2026-01-07  [momentum]  firstDays / stage 1 / reflective / mo.first.16
  rule:  momentum.firstDays
  fired: fewer than fourteen days since install, at least one event, so this is a beginning rather than an empty app
  > A start, nothing more.
2026-01-07  [banner]  weekBuilding / stage 1 / reflective / bn.build.31
  rule:  banner.weekBuilding
  fired: three to five days into the week, completions are accumulating
  > Part of a week, with something in it.
2026-01-07  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-01-08  [momentum]  firstDays / stage 1 / reflective / mo.first.24
  rule:  momentum.firstDays
  fired: fewer than fourteen days since install, at least one event, so this is a beginning rather than an empty app
  > Only a few days of this so far.
2026-01-08  [banner]  weekMixed / stage 1 / reflective / bn.mixed.13
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week with a center of gravity in it.
2026-01-08  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-01-09  [momentum]  firstDays / stage 1 / reflective / mo.first.19
  rule:  momentum.firstDays
  fired: fewer than fourteen days since install, at least one event, so this is a beginning rather than an empty app
  > A short history so far.
2026-01-09  [banner]  weekMixed / stage 1 / reflective / bn.mixed.25
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Uneven going.
2026-01-09  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-01-10  [momentum]  firstDays / stage 1 / reflective / mo.first.21
  rule:  momentum.firstDays
  fired: fewer than fourteen days since install, at least one event, so this is a beginning rather than an empty app
  > Early, before much of it has happened.
2026-01-10  [banner]  weekMixed / stage 1 / reflective / bn.mixed.14
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A shape rather than a spread.
2026-01-10  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-01-11  [momentum]  firstDays / stage 1 / reflective / mo.first.07
  rule:  momentum.firstDays
  fired: fewer than fourteen days since install, at least one event, so this is a beginning rather than an empty app
  > The beginning of a picture.
2026-01-11  [banner]  weekStarting / stage 1 / reflective / bn.start.17
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > The first days of it.
2026-01-11  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-01-11  [report headline]  firstWeek / stage 1 / plain / hd.first.01
  rule:  report.headline.firstWeek
  fired: this is the first week of data there has ever been, something happened in it
  > Your first week.
2026-01-11  [report observation]  firstMilestone / stage 1 / observational / ob.first.l05
  rule:  report.observation.firstMilestone
  fired: something happened this window for the first time ever, the window has activity behind the milestone
  > Every area had something active at once, for the first time.
2026-01-11  [report observation]  timeOfDay / stage 1 / observational / ob.tod.l28
  rule:  report.observation.timeOfDay
  fired: one part of the day holds most of the week, the week was not confined to a single part of one day
  > Half of the week's activity sat inside one part of the day, and the rest was spread over the others.

2026-01-12  [momentum]  firstDays / stage 1 / reflective / mo.first.25
  rule:  momentum.firstDays
  fired: fewer than fourteen days since install, at least one event, so this is a beginning rather than an empty app
  > The opening days.
2026-01-12  [banner]  weekStarting / stage 1 / reflective / bn.start.26
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > A week still near its own beginning.
2026-01-12  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-01-13  [momentum]  firstDays / stage 1 / reflective / mo.first.20
  rule:  momentum.firstDays
  fired: fewer than fourteen days since install, at least one event, so this is a beginning rather than an empty app
  > The part of it that has happened so far.
2026-01-13  [banner]  weekMixed / stage 1 / reflective / bn.mixed.26
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Most of it in one area, so far.
2026-01-13  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-01-14  [momentum]  firstDays / stage 1 / reflective / mo.first.23
  rule:  momentum.firstDays
  fired: fewer than fourteen days since install, at least one event, so this is a beginning rather than an empty app
  > A beginning with a few days behind it.
2026-01-14  [banner]  weekMixed / stage 1 / reflective / bn.mixed.19
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week gathered mostly in one place.
2026-01-14  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-01-15  [momentum]  firstDays / stage 1 / reflective / mo.first.04
  rule:  momentum.firstDays
  fired: fewer than fourteen days since install, at least one event, so this is a beginning rather than an empty app
  > Just getting going.
2026-01-15  [banner]  weekMixed / stage 1 / reflective / bn.mixed.18
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Unequal, with the week still going.
2026-01-15  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-01-16  [momentum]  firstDays / stage 1 / reflective / mo.first.22
  rule:  momentum.firstDays
  fired: fewer than fourteen days since install, at least one event, so this is a beginning rather than an empty app
  > There are not many days here to look at.
2026-01-16  [banner]  weekMixed / stage 1 / reflective / bn.mixed.17
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Spread unevenly, so far.
2026-01-16  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-01-17  [momentum]  firstDays / stage 1 / reflective / mo.first.20
  rule:  momentum.firstDays
  fired: fewer than fourteen days since install, at least one event, so this is a beginning rather than an empty app
  > The part of it that has happened so far.
2026-01-17  [banner]  weekMixed / stage 1 / reflective / bn.mixed.20
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Concentrated, with the rest thinner.
2026-01-17  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-01-18  [momentum]  steadyStretch / stage 1 / reflective / mo.steady.22
  rule:  momentum.steadyStretch
  fired: active on nine or more of the last fourteen days, there are fourteen days to have been active across, the app has existed for the 14 days being described
  > A fortnight with few gaps.
2026-01-18  [banner]  weekStarting / stage 1 / reflective / bn.start.21
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > Early, before anything settles.
2026-01-18  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-01-18  [report headline]  datedFallback / stage 1 / plain / hd.fall.02
  rule:  report.headline.datedFallback
  fired: there is a week to name, something happened in it
  facts: weekRef=2026-01-11
  > January 11.
2026-01-18  [report observation]  timeOfDay / stage 1 / observational / ob.tod.l18
  rule:  report.observation.timeOfDay
  fired: one part of the day holds most of the week, the week was not confined to a single part of one day
  > The week has a time of day in it.

2026-01-19  [momentum]  steadyStretch / stage 1 / reflective / mo.steady.07
  rule:  momentum.steadyStretch
  fired: active on nine or more of the last fourteen days, there are fourteen days to have been active across, the app has existed for the 14 days being described
  > A consistent stretch.
2026-01-19  [banner]  weekStarting / stage 1 / reflective / bn.start.19
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > Little to read this early.
2026-01-19  [pulse]  rebalance / stage 1 / plain / rebalance.s1.20
  rule:  pulse.rebalance.s1
  fired: the area had been still five to thirteen days before it moved, the area has at least 1 events in the window, so naming it cannot be a phantom claim
  facts: areaName=Health
  > Activity returned to Health yesterday.
  > A return, or a reminder?
  ? Felt long | Went quickly

2026-01-20  [momentum]  steadyStretch / stage 1 / reflective / mo.steady.26
  rule:  momentum.steadyStretch
  fired: active on nine or more of the last fourteen days, there are fourteen days to have been active across, the app has existed for the 14 days being described
  > Two weeks with very little missing.
2026-01-20  [banner]  weekMixed / stage 1 / reflective / bn.mixed.09
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week with a tilt to it.
2026-01-20  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-01-21  [momentum]  steadyStretch / stage 1 / reflective / mo.steady.13
  rule:  momentum.steadyStretch
  fired: active on nine or more of the last fourteen days, there are fourteen days to have been active across, the app has existed for the 14 days being described
  > A rhythm, more than a run.
2026-01-21  [banner]  weekMixed / stage 1 / reflective / bn.mixed.11
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Weighted rather than spread.
2026-01-21  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-01-22  [momentum]  steadyStretch / stage 1 / reflective / mo.steady.16
  rule:  momentum.steadyStretch
  fired: active on nine or more of the last fourteen days, there are fourteen days to have been active across, the app has existed for the 14 days being described
  > Even going, two weeks running.
2026-01-22  [banner]  weekBuilding / stage 1 / reflective / bn.build.33
  rule:  banner.weekBuilding
  fired: three to five days into the week, completions are accumulating
  > A week with days still in front of it.
2026-01-22  [pulse]  spread / stage 1 / plain / spread.s1.02
  rule:  pulse.spread.s1
  fired: exactly three areas had events, no area is above half the window, the window holds at least 5 events, so a share is describing something real
  facts: areaCount=3 [rollup.areasWithEvents]
  > Attention went across three areas yesterday.
  > Balanced, or scattered?
  ? Felt manageable | Felt stretched

2026-01-23  [momentum]  steadyStretch / stage 1 / reflective / mo.steady.30
  rule:  momentum.steadyStretch
  fired: active on nine or more of the last fourteen days, there are fourteen days to have been active across, the app has existed for the 14 days being described
  > A stretch with more days in it than gaps.
2026-01-23  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-01-23  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-01-24  [momentum]  steadyStretch / stage 1 / reflective / mo.steady.21
  rule:  momentum.steadyStretch
  fired: active on nine or more of the last fourteen days, there are fourteen days to have been active across, the app has existed for the 14 days being described
  > Few empty days in it.
2026-01-24  [banner]  weekMixed / stage 1 / reflective / bn.mixed.21
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week with more of it in one area.
2026-01-24  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-01-25  [momentum]  comeback / stage 1 / reflective / mo.come.10
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > The gap ended.
2026-01-25  [banner]  weekStarting / stage 1 / reflective / bn.start.24
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > The shape of this week is still ahead.
2026-01-25  [pulse]  SILENT (NO_RULE_QUALIFIED)
2026-01-25  [report headline]  comeback / stage 1 / plain / hd.back.08
  rule:  report.headline.comeback
  fired: this area returned after a long dormancy, the area has at least 1 events in the window, so naming it cannot be a phantom claim
  facts: areaName=Health
  > Health woke up.
2026-01-25  [report observation]  areaRevival / stage 1 / observational / ob.rev.l07
  rule:  report.observation.areaRevival
  fired: this area returned after a dormancy, the area has at least 1 events in the window, so naming it cannot be a phantom claim
  facts: areaName=Health
  > Health moved this week.
2026-01-25  [report observation]  timeOfDay / stage 1 / observational / ob.tod.l09
  rule:  report.observation.timeOfDay
  fired: one part of the day holds most of the week, the week was not confined to a single part of one day
  > More than one part of the day had something in it.

2026-01-26  [momentum]  comeback / stage 1 / reflective / mo.come.04
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > A return, after a gap.
2026-01-26  [banner]  weekStarting / stage 1 / reflective / bn.start.16
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > A week still mostly ahead.
2026-01-26  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-01-27  [momentum]  comeback / stage 1 / reflective / mo.come.12
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > Back in motion.
2026-01-27  [banner]  weekMixed / stage 1 / reflective / bn.mixed.21
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week with more of it in one area.
2026-01-27  [pulse]  rebalance / stage 1 / plain / rebalance.s1.28
  rule:  pulse.rebalance.s1
  fired: the area had been still five to thirteen days before it moved, the area has at least 1 events in the window, so naming it cannot be a phantom claim
  facts: ageDays=5 [area.areaDormancyDays:side], areaName=Side project
  > Side project did nothing for five days until yesterday.
  > Back for good, or a one off?
  ? Picking it up | Just checking in

2026-01-28  [momentum]  comeback / stage 1 / reflective / mo.come.04
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > A return, after a gap.
2026-01-28  [banner]  weekMixed / stage 1 / reflective / bn.mixed.16
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week with a heavier side.
2026-01-28  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-01-29  [momentum]  comeback / stage 1 / reflective / mo.come.23
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > What paused is going again.
2026-01-29  [banner]  weekMixed / stage 1 / reflective / bn.mixed.12
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week that leans one way.
2026-01-29  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-01-30  [momentum]  comeback / stage 1 / reflective / mo.come.30
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > A pause with something after it.
2026-01-30  [banner]  weekMixed / stage 1 / reflective / bn.mixed.25
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Uneven going.
2026-01-30  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-01-31  [momentum]  comeback / stage 1 / reflective / mo.come.22
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > Movement where there was none.
2026-01-31  [banner]  weekMixed / stage 1 / reflective / bn.mixed.73
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week that piled up in one place.
2026-01-31  [pulse]  SILENT (NO_RULE_QUALIFIED)
```

### February 2026

28 days on screen, 2 Pulses spoken, 26 Pulse days silent, 70 sentences in all.

```text
2026-02-01  [momentum]  steadyStretch / stage 1 / reflective / mo.steady.08
  rule:  momentum.steadyStretch
  fired: active on nine or more of the last fourteen days, there are fourteen days to have been active across, the app has existed for the 14 days being described
  > The last two weeks have been fairly even.
2026-02-01  [banner]  weekStarting / stage 1 / reflective / bn.start.17
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > The first days of it.
2026-02-01  [pulse]  SILENT (NO_RULE_QUALIFIED)
2026-02-01  [report headline]  comeback / stage 1 / plain / hd.back.17
  rule:  report.headline.comeback
  fired: this area returned after a long dormancy, the area has at least 1 events in the window, so naming it cannot be a phantom claim
  > Still, then moving.
2026-02-01  [report observation]  areaRevival / stage 1 / observational / ob.rev.l31
  rule:  report.observation.areaRevival
  fired: this area returned after a dormancy, the area has at least 1 events in the window, so naming it cannot be a phantom claim
  facts: ageDays=5 [area.areaDormancyDays:side], areaName=Side project
  > An area can be quiet for 5 days and then not be, and Side project is the one that did that here.
2026-02-01  [report observation]  neglectedArea / stage 1 / neutral_agent / ob.neg.s1.l09
  rule:  report.observation.neglectedArea.s1
  fired: the area has been silent seven to thirteen days, the area has real history, so this is a silence and not a new area
  facts: ageDays=9 [area.areaDaysSinceLastEvent:health], areaName=Health
  > 9 days without an event in Health.
2026-02-01  [report observation]  timeOfDay / stage 1 / observational / ob.tod.l20
  rule:  report.observation.timeOfDay
  fired: one part of the day holds most of the week, the week was not confined to a single part of one day
  > Half of everything happened in one window of the day.
2026-02-01  [report pattern]  SILENT (NO_RULE_QUALIFIED)

2026-02-02  [momentum]  steadyStretch / stage 1 / reflective / mo.steady.16
  rule:  momentum.steadyStretch
  fired: active on nine or more of the last fourteen days, there are fourteen days to have been active across, the app has existed for the 14 days being described
  > Even going, two weeks running.
2026-02-02  [banner]  weekStarting / stage 1 / reflective / bn.start.23
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > Nothing here has had time to become a shape.
2026-02-02  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-02-03  [momentum]  steadyStretch / stage 1 / reflective / mo.steady.55
  rule:  momentum.steadyStretch
  fired: active on nine or more of the last fourteen days, there are fourteen days to have been active across, the app has existed for the 14 days being described
  > Two weeks with something in most of them.
2026-02-03  [banner]  weekBuilding / stage 1 / reflective / bn.build.22
  rule:  banner.weekBuilding
  fired: three to five days into the week, completions are accumulating
  > A week that has started to add up.
2026-02-03  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-02-04  [momentum]  comeback / stage 1 / reflective / mo.come.61
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  facts: areaName=Side project
  > Movement in Side project on the other side of a gap.
2026-02-04  [banner]  weekMixed / stage 1 / reflective / bn.mixed.15
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Tilted, so far.
2026-02-04  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-02-05  [momentum]  comeback / stage 1 / reflective / mo.come.25
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > The pause has run out.
2026-02-05  [banner]  weekMixed / stage 1 / reflective / bn.mixed.21
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week with more of it in one area.
2026-02-05  [pulse]  spread / stage 1 / plain / spread.s1.01
  rule:  pulse.spread.s1
  fired: exactly three areas had events, no area is above half the window, the window holds at least 5 events, so a share is describing something real
  facts: areaCount=3 [rollup.areasWithEvents]
  > Yesterday touched three areas.
  > Balanced, or scattered?
  ? Felt manageable | Felt stretched

2026-02-06  [momentum]  comeback / stage 1 / reflective / mo.come.23
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > What paused is going again.
2026-02-06  [banner]  weekMixed / stage 1 / reflective / bn.mixed.21
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week with more of it in one area.
2026-02-06  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-02-07  [momentum]  comeback / stage 1 / reflective / mo.come.22
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > Movement where there was none.
2026-02-07  [banner]  weekMixed / stage 1 / reflective / bn.mixed.23
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week that did not spread evenly.
2026-02-07  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-02-08  [momentum]  comeback / stage 1 / reflective / mo.come.16
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > Movement, after a pause.
2026-02-08  [banner]  weekStarting / stage 1 / reflective / bn.start.26
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > A week still near its own beginning.
2026-02-08  [pulse]  SILENT (NO_RULE_QUALIFIED)
2026-02-08  [report headline]  comeback / stage 1 / plain / hd.back.57
  rule:  report.headline.comeback
  fired: this area returned after a long dormancy, the area has at least 1 events in the window, so naming it cannot be a phantom claim
  facts: ageDays=13 [area.areaDormancyDays:health], areaName=Health
  > 13 days gone, Health back.
2026-02-08  [report observation]  areaRevival / stage 1 / observational / ob.rev.l12
  rule:  report.observation.areaRevival
  fired: this area returned after a dormancy, the area has at least 1 events in the window, so naming it cannot be a phantom claim
  facts: areaName=Health
  > The quiet in Health ended this week.
2026-02-08  [report observation]  timeOfDay / stage 1 / observational / ob.tod.l24
  rule:  report.observation.timeOfDay
  fired: one part of the day holds most of the week, the week was not confined to a single part of one day
  > A time of day held it.
2026-02-08  [report pattern]  decliningActivity / stage 1 / plain / pt.dec.02
  rule:  report.pattern.decliningActivity
  fired: total activity has fallen three weeks running, there are at least three weeks of snapshots, without which no pattern may fire
  facts: k=37 [history.weekEventsAgo:2], m=34 [history.weekEventsAgo:1], n=33 [history.weekEventsAgo:0]
  > 37, then 34, then 33.

2026-02-09  [momentum]  comeback / stage 1 / reflective / mo.come.65
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > A stretch of nothing with a finish to it.
2026-02-09  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-02-09  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-02-10  [momentum]  comeback / stage 1 / reflective / mo.come.17
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > The pause is over.
2026-02-10  [banner]  weekBuilding / stage 1 / reflective / bn.build.13
  rule:  banner.weekBuilding
  fired: three to five days into the week, completions are accumulating
  > A few days in, a few things done.
2026-02-10  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-02-11  [momentum]  comeback / stage 1 / reflective / mo.come.28
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > A gap with something on the other side of it.
2026-02-11  [banner]  weekMixed / stage 1 / reflective / bn.mixed.14
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A shape rather than a spread.
2026-02-11  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-02-12  [momentum]  comeback / stage 1 / reflective / mo.come.23
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > What paused is going again.
2026-02-12  [banner]  weekBuilding / stage 1 / reflective / bn.build.22
  rule:  banner.weekBuilding
  fired: three to five days into the week, completions are accumulating
  > A week that has started to add up.
2026-02-12  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-02-13  [momentum]  comeback / stage 1 / reflective / mo.come.17
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > The pause is over.
2026-02-13  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-02-13  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-02-14  [momentum]  comeback / stage 1 / reflective / mo.come.04
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > A return, after a gap.
2026-02-14  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-02-14  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-02-15  [momentum]  comeback / stage 1 / reflective / mo.come.14
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > The quiet ended.
2026-02-15  [banner]  weekStarting / stage 1 / reflective / bn.start.12
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > Early, with most of it ahead.
2026-02-15  [pulse]  SILENT (NO_RULE_QUALIFIED)
2026-02-15  [report headline]  personalBest / stage 1 / plain / hd.best.23
  rule:  report.headline.personalBest
  fired: no earlier week strictly beats this one, this week reaches the personal best, there is more than one week to be best of, there are completions, so no count renders as zero
  > This one is the highest.
2026-02-15  [report observation]  personalBest / stage 1 / editorial / ob.best.l12
  rule:  report.observation.personalBest
  fired: no earlier week strictly beats this one, this week reaches the personal best, there is more than one week to be best of, there are completions, so no count renders as zero
  > The record moved. That is all the record does.
2026-02-15  [report observation]  areaBalance / stage 1 / editorial / ob.bal.l07
  rule:  report.observation.areaBalance
  fired: three or more areas had activity, no area holds half the week, the window holds at least 6 events, so a share is describing something real
  > The week had no owner.
2026-02-15  [report observation]  timeOfDay / stage 1 / observational / ob.tod.l19
  rule:  report.observation.timeOfDay
  fired: one part of the day holds most of the week, the week was not confined to a single part of one day
  > When you work showed up this week.
2026-02-15  [report pattern]  SILENT (NO_RULE_QUALIFIED)

2026-02-16  [momentum]  comeback / stage 1 / reflective / mo.come.17
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > The pause is over.
2026-02-16  [banner]  weekStarting / stage 1 / reflective / bn.start.25
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > A week at the point where nothing is decided.
2026-02-16  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-02-17  [momentum]  comeback / stage 1 / reflective / mo.come.12
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > Back in motion.
2026-02-17  [banner]  weekBuilding / stage 1 / reflective / bn.build.18
  rule:  banner.weekBuilding
  fired: three to five days into the week, completions are accumulating
  > Some of the week has happened.
2026-02-17  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-02-18  [momentum]  steadyStretch / stage 1 / reflective / mo.steady.16
  rule:  momentum.steadyStretch
  fired: active on nine or more of the last fourteen days, there are fourteen days to have been active across, the app has existed for the 14 days being described
  > Even going, two weeks running.
2026-02-18  [banner]  weekMixed / stage 1 / reflective / bn.mixed.17
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Spread unevenly, so far.
2026-02-18  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-02-19  [momentum]  steadyStretch / stage 1 / reflective / mo.steady.26
  rule:  momentum.steadyStretch
  fired: active on nine or more of the last fourteen days, there are fourteen days to have been active across, the app has existed for the 14 days being described
  > Two weeks with very little missing.
2026-02-19  [banner]  weekMixed / stage 1 / reflective / bn.mixed.15
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Tilted, so far.
2026-02-19  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-02-20  [momentum]  steadyStretch / stage 1 / reflective / mo.steady.27
  rule:  momentum.steadyStretch
  fired: active on nine or more of the last fourteen days, there are fourteen days to have been active across, the app has existed for the 14 days being described
  > Something on most of the days.
2026-02-20  [banner]  weekMixed / stage 1 / reflective / bn.mixed.12
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week that leans one way.
2026-02-20  [pulse]  rebalance / stage 1 / plain / rebalance.s1.19
  rule:  pulse.rebalance.s1
  fired: the area had been still five to thirteen days before it moved, the area has at least 1 events in the window, so naming it cannot be a phantom claim
  facts: ageDays=7 [area.areaDormancyDays:health], areaName=Health
  > Health did not move for seven days. Yesterday it did.
  > Back for good, or a one off?
  ? Something changed | Nothing did

2026-02-21  [momentum]  steadyStretch / stage 1 / reflective / mo.steady.09
  rule:  momentum.steadyStretch
  fired: active on nine or more of the last fourteen days, there are fourteen days to have been active across, the app has existed for the 14 days being described
  > Steady, across fourteen days.
2026-02-21  [banner]  weekMixed / stage 1 / reflective / bn.mixed.24
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > The weight of the week sits in one place.
2026-02-21  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-02-22  [momentum]  steadyStretch / stage 1 / reflective / mo.steady.07
  rule:  momentum.steadyStretch
  fired: active on nine or more of the last fourteen days, there are fourteen days to have been active across, the app has existed for the 14 days being described
  > A consistent stretch.
2026-02-22  [banner]  weekStarting / stage 1 / reflective / bn.start.10
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > The start of a week.
2026-02-22  [pulse]  SILENT (NO_RULE_QUALIFIED)
2026-02-22  [report headline]  comeback / stage 1 / plain / hd.back.44
  rule:  report.headline.comeback
  fired: this area returned after a long dormancy, the area has at least 1 events in the window, so naming it cannot be a phantom claim
  > An area found its way back.
2026-02-22  [report observation]  areaRevival / stage 1 / observational / ob.rev.l29
  rule:  report.observation.areaRevival
  fired: this area returned after a dormancy, the area has at least 1 events in the window, so naming it cannot be a phantom claim
  facts: ageDays=7 [area.areaDormancyDays:health], areaName=Health
  > Nothing moved in Health for 7 days. The week that just ended had something in it.
2026-02-22  [report observation]  timeOfDay / stage 1 / observational / ob.tod.l08
  rule:  report.observation.timeOfDay
  fired: one part of the day holds most of the week, the week was not confined to a single part of one day
  > The week had a busy stretch.
2026-02-22  [report pattern]  SILENT (NO_RULE_QUALIFIED)

2026-02-23  [momentum]  steadyStretch / stage 1 / reflective / mo.steady.31
  rule:  momentum.steadyStretch
  fired: active on nine or more of the last fourteen days, there are fourteen days to have been active across, the app has existed for the 14 days being described
  > A fortnight with more movement than stillness.
2026-02-23  [banner]  weekStarting / stage 1 / reflective / bn.start.09
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > Early yet.
2026-02-23  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-02-24  [momentum]  steadyStretch / stage 1 / reflective / mo.steady.27
  rule:  momentum.steadyStretch
  fired: active on nine or more of the last fourteen days, there are fourteen days to have been active across, the app has existed for the 14 days being described
  > Something on most of the days.
2026-02-24  [banner]  weekMixed / stage 1 / reflective / bn.mixed.23
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week that did not spread evenly.
2026-02-24  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-02-25  [momentum]  steadyStretch / stage 1 / reflective / mo.steady.56
  rule:  momentum.steadyStretch
  fired: active on nine or more of the last fourteen days, there are fourteen days to have been active across, the app has existed for the 14 days being described
  > A fortnight of small steady days.
2026-02-25  [banner]  weekMixed / stage 1 / reflective / bn.mixed.19
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week gathered mostly in one place.
2026-02-25  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-02-26  [momentum]  comeback / stage 1 / reflective / mo.come.26
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > Quiet, then not quiet.
2026-02-26  [banner]  weekMixed / stage 1 / reflective / bn.mixed.10
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Lopsided, for now.
2026-02-26  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-02-27  [momentum]  comeback / stage 1 / reflective / mo.come.30
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > A pause with something after it.
2026-02-27  [banner]  weekMixed / stage 1 / reflective / bn.mixed.16
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week with a heavier side.
2026-02-27  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-02-28  [momentum]  comeback / stage 1 / reflective / mo.come.19
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > A gap closed.
2026-02-28  [banner]  weekMixed / stage 1 / reflective / bn.mixed.17
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Spread unevenly, so far.
2026-02-28  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
```

### March 2026

13 days on screen, 3 Pulses spoken, 10 Pulse days silent, 26 sentences in all.

```text
2026-03-01  [momentum]  comeback / stage 1 / reflective / mo.come.13
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > A restart.
2026-03-01  [banner]  weekStarting / stage 1 / reflective / bn.start.23
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > Nothing here has had time to become a shape.
2026-03-01  [pulse]  SILENT (NO_RULE_QUALIFIED)
2026-03-01  [report headline]  comeback / stage 1 / plain / hd.back.43
  rule:  report.headline.comeback
  fired: this area returned after a long dormancy, the area has at least 1 events in the window, so naming it cannot be a phantom claim
  facts: areaName=Health
  > Health was quiet until now.
2026-03-01  [report observation]  areaRevival / stage 1 / observational / ob.rev.l11
  rule:  report.observation.areaRevival
  fired: this area returned after a dormancy, the area has at least 1 events in the window, so naming it cannot be a phantom claim
  facts: ageDays=8 [area.areaDormancyDays:health], areaName=Health
  > Health has something in it again after 8 days.
2026-03-01  [report observation]  timeOfDay / stage 1 / observational / ob.tod.l15
  rule:  report.observation.timeOfDay
  fired: one part of the day holds most of the week, the week was not confined to a single part of one day
  > The week clustered.
2026-03-01  [report pattern]  decliningActivity / stage 1 / plain / pt.dec.07
  rule:  report.pattern.decliningActivity
  fired: total activity has fallen three weeks running, there are at least three weeks of snapshots, without which no pattern may fire
  > The trend has been downward for three weeks.

2026-03-02  [momentum]  comeback / stage 1 / reflective / mo.come.15
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > A gap, then movement.
2026-03-02  [banner]  weekStarting / stage 1 / reflective / bn.start.19
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > Little to read this early.
2026-03-02  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-03-03  [momentum]  comeback / stage 1 / reflective / mo.come.29
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > Something on the far side of a gap.
2026-03-03  [banner]  weekBuilding / stage 1 / reflective / bn.build.32
  rule:  banner.weekBuilding
  fired: three to five days into the week, completions are accumulating
  > Something behind it, more of it ahead.
2026-03-03  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-03-04  [momentum]  comeback / stage 1 / reflective / mo.come.14
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > The quiet ended.
2026-03-04  [banner]  weekMixed / stage 1 / reflective / bn.mixed.16
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week with a heavier side.
2026-03-04  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-03-13  [momentum]  comeback / stage 1 / reflective / mo.come.15
  rule:  momentum.comeback
  fired: this area resumed after a gap of five days or more, the area has at least 1 events in the window, so naming it cannot be a phantom claim, the app has existed for the 14 days being described
  > A gap, then movement.
2026-03-13  [banner]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-03-13  [pulse]  quietDay / stage 3 / plain / quietday.s3.15
  rule:  pulse.quietDay.s3
  fired: four or more days running with nothing in them at all, there is at least a day of history, so this is a quiet day and not an empty install
  > A run of still days.
  > Anything moving off the app?
  ? Time off | Time elsewhere | No time at all

2026-03-14  [momentum]  quietStretch / stage 1 / reflective / mo.quiet.30
  rule:  momentum.quietStretch
  fired: active on four or fewer of the last fourteen days, active on at least one, so no day count renders as zero, the app has existed for the 14 days being described, there are fourteen days to have been quiet across
  > A fortnight of small movements.
2026-03-14  [banner]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-03-14  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-03-17  [momentum]  quietStretch / stage 1 / reflective / mo.quiet.27
  rule:  momentum.quietStretch
  fired: active on four or fewer of the last fourteen days, active on at least one, so no day count renders as zero, the app has existed for the 14 days being described, there are fourteen days to have been quiet across
  > A fortnight that moved a little.
2026-03-17  [banner]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-03-17  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-03-21  [momentum]  quietStretch / stage 1 / reflective / mo.quiet.24
  rule:  momentum.quietStretch
  fired: active on four or fewer of the last fourteen days, active on at least one, so no day count renders as zero, the app has existed for the 14 days being described, there are fourteen days to have been quiet across
  > A quiet stretch of days.
2026-03-21  [banner]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-03-21  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-03-23  [momentum]  quietStretch / stage 1 / reflective / mo.quiet.19
  rule:  momentum.quietStretch
  fired: active on four or fewer of the last fourteen days, active on at least one, so no day count renders as zero, the app has existed for the 14 days being described, there are fourteen days to have been quiet across
  > Two weeks that mostly stayed as they were.
2026-03-23  [banner]  weekStarting / stage 1 / reflective / bn.start.21
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > Early, before anything settles.
2026-03-23  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-03-24  [momentum]  quietStretch / stage 1 / reflective / mo.quiet.24
  rule:  momentum.quietStretch
  fired: active on four or fewer of the last fourteen days, active on at least one, so no day count renders as zero, the app has existed for the 14 days being described, there are fourteen days to have been quiet across
  > A quiet stretch of days.
2026-03-24  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-03-24  [pulse]  persistence / stage 2 / plain / persistence.s2.32
  rule:  pulse.persistence.s2
  fired: the active item is six to thirteen days old, the area holding the item has at least 1 events in the window
  facts: areaName=Work, itemTitle=Set up the new laptop
  > Work starts with Set up the new laptop.
  > Steady, or stalled?
  ? Making progress | Not really

2026-03-25  [momentum]  singleAreaWeek / stage 1 / reflective / mo.single.32
  rule:  momentum.singleAreaWeek
  fired: this area holds seventy percent of the window or more, the area has at least 4 events in the window, so its share is describing something real, the window holds at least 4 events, so a share is describing something real, the app has existed for the 14 days being described
  > One thing at a time, for two weeks.
2026-03-25  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-03-25  [pulse]  quietDay / stage 1 / plain / quietday.s1.23
  rule:  pulse.quietDay.s1
  fired: fewer than two events in the window, per the family trigger, there is at least a day of history, so this is a quiet day and not an empty install
  > An uneventful day.
  > Off the app, or off entirely?
  ? On purpose | Other things came up | Too much on

2026-03-27  [momentum]  singleAreaWeek / stage 1 / reflective / mo.single.04
  rule:  momentum.singleAreaWeek
  fired: this area holds seventy percent of the window or more, the area has at least 4 events in the window, so its share is describing something real, the window holds at least 4 events, so a share is describing something real, the app has existed for the 14 days being described
  > A narrow fortnight.
2026-03-27  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-03-27  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-03-30  [momentum]  singleAreaWeek / stage 1 / reflective / mo.single.34
  rule:  momentum.singleAreaWeek
  fired: this area holds seventy percent of the window or more, the area has at least 4 events in the window, so its share is describing something real, the window holds at least 4 events, so a share is describing something real, the app has existed for the 14 days being described
  > Two weeks that kept mostly to a single area.
2026-03-30  [banner]  weekStarting / stage 1 / reflective / bn.start.26
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > A week still near its own beginning.
2026-03-30  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
```

### April 2026

12 days on screen, 2 Pulses spoken, 10 Pulse days silent, 35 sentences in all.

```text
2026-04-05  [momentum]  quietStretch / stage 1 / reflective / mo.quiet.22
  rule:  momentum.quietStretch
  fired: active on four or fewer of the last fourteen days, active on at least one, so no day count renders as zero, the app has existed for the 14 days being described, there are fourteen days to have been quiet across
  > A stretch with little in it.
2026-04-05  [banner]  weekStarting / stage 1 / reflective / bn.start.11
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > Barely begun.
2026-04-05  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-04-05  [report headline]  SILENT (NO_RULE_QUALIFIED)
2026-04-05  [report observation]  neglectedArea / stage 1 / neutral_agent / ob.neg.s1.l08
  rule:  report.observation.neglectedArea.s1
  fired: the area has been silent seven to thirteen days, the area has real history, so this is a silence and not a new area
  facts: ageDays=10 [area.areaDaysSinceLastEvent:work], areaName=Work
  > 10 days passed in Work with nothing in them.
2026-04-05  [report pattern]  areaGoneQuiet / stage 1 / plain / pt.gone.06
  rule:  report.pattern.areaGoneQuiet
  fired: the area has had nothing in it for three weeks, it used to move, so this is a stop rather than an empty heading, there are at least three weeks of snapshots, without which no pattern may fire
  facts: areaName=Health, sinceRef=2026-03-05
  > Health still exists. It has not done anything since March.

2026-04-08  [momentum]  cleanSlate / stage 1 / reflective / mo.clean.07
  rule:  momentum.cleanSlate
  fired: no events at all in the window, there are areas, so there is an app to have a clean slate in
  > Nothing here yet.
2026-04-08  [banner]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-04-08  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-04-09  [momentum]  cleanSlate / stage 1 / reflective / mo.clean.03
  rule:  momentum.cleanSlate
  fired: no events at all in the window, there are areas, so there is an app to have a clean slate in
  > Empty, for now.
2026-04-09  [banner]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-04-09  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-04-12  [momentum]  cleanSlate / stage 1 / reflective / mo.clean.16
  rule:  momentum.cleanSlate
  fired: no events at all in the window, there are areas, so there is an app to have a clean slate in
  > Two weeks with the page unchanged.
2026-04-12  [banner]  weekStarting / stage 1 / reflective / bn.start.10
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > The start of a week.
2026-04-12  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-04-12  [report headline]  SILENT (NO_RULE_QUALIFIED)
2026-04-12  [report observation]  neglectedArea / stage 2 / neutral_agent / ob.neg.s2.l09
  rule:  report.observation.neglectedArea.s2
  fired: the area has been silent fourteen days or more, the area has real history, so this is a silence and not a new area
  facts: areaName=Health, sinceRef=2026-03-05
  > Health has been still since March.
2026-04-12  [report pattern]  areaGoneQuiet / stage 1 / plain / pt.gone.10
  rule:  report.pattern.areaGoneQuiet
  fired: the area has had nothing in it for three weeks, it used to move, so this is a stop rather than an empty heading, there are at least three weeks of snapshots, without which no pattern may fire
  facts: areaName=Side project, sinceRef=2026-03-03
  > Whatever Side project was for, it has not been for it since March.

2026-04-14  [momentum]  quietStretch / stage 1 / reflective / mo.quiet.17
  rule:  momentum.quietStretch
  fired: active on four or fewer of the last fourteen days, active on at least one, so no day count renders as zero, the app has existed for the 14 days being described, there are fourteen days to have been quiet across
  > Still, rather than busy.
2026-04-14  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-04-14  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-04-15  [momentum]  quietStretch / stage 1 / reflective / mo.quiet.15
  rule:  momentum.quietStretch
  fired: active on four or fewer of the last fourteen days, active on at least one, so no day count renders as zero, the app has existed for the 14 days being described, there are fourteen days to have been quiet across
  > A fortnight with a few days in it.
2026-04-15  [banner]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-04-15  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-04-19  [momentum]  quietStretch / stage 1 / reflective / mo.quiet.23
  rule:  momentum.quietStretch
  fired: active on four or fewer of the last fourteen days, active on at least one, so no day count renders as zero, the app has existed for the 14 days being described, there are fourteen days to have been quiet across
  > A fortnight without much in it.
2026-04-19  [banner]  weekStarting / stage 1 / reflective / bn.start.10
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > The start of a week.
2026-04-19  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-04-19  [report headline]  comeback / stage 1 / plain / hd.back.21
  rule:  report.headline.comeback
  fired: this area returned after a long dormancy, the area has at least 1 events in the window, so naming it cannot be a phantom claim
  facts: areaName=Work
  > Work picked back up.
2026-04-19  [report observation]  selfReportVsData / stage 1 / editorial / ob.srvd.l44
  rule:  report.observation.selfReportVsData
  fired: the item the user answered about is still active, the area holding the item has at least 1 events in the window, there is a stored answer to quote
  > What you said is a fact about you and the days are a fact about the item.
2026-04-19  [report observation]  areaRevival / stage 1 / observational / ob.rev.l02
  rule:  report.observation.areaRevival
  fired: this area returned after a dormancy, the area has at least 1 events in the window, so naming it cannot be a phantom claim
  facts: areaName=Work
  > Work came back this week.
2026-04-19  [report observation]  queuePressure / stage 1 / neutral_agent / ob.qp.l28
  rule:  report.observation.queuePressure
  fired: at least one area's queue grew, the queues hold three or more things to count
  facts: m=4 [rollup.queueTotalAtStart], n=5 [rollup.queueTotal]
  > 5 things are in the queues now, 4 were there when the week opened, and at least one queue grew inside it.
2026-04-19  [report observation]  quietWeek / stage 1 / neutral_agent / ob.quiet.l11
  rule:  report.observation.quietWeek
  fired: the week holds fewer events than it has days, something happened, so the week is quiet rather than absent
  facts: n=3 [window.totalEvents]
  > Seven days, 3 things.
2026-04-19  [report pattern]  areaGoneQuiet / stage 1 / plain / pt.gone.03
  rule:  report.pattern.areaGoneQuiet
  fired: the area has had nothing in it for three weeks, it used to move, so this is a stop rather than an empty heading, there are at least three weeks of snapshots, without which no pattern may fire
  facts: areaName=Health, sinceRef=2026-03-05
  > Health has been silent since March.

2026-04-20  [momentum]  quietStretch / stage 1 / reflective / mo.quiet.24
  rule:  momentum.quietStretch
  fired: active on four or fewer of the last fourteen days, active on at least one, so no day count renders as zero, the app has existed for the 14 days being described, there are fourteen days to have been quiet across
  > A quiet stretch of days.
2026-04-20  [banner]  weekStarting / stage 1 / reflective / bn.start.24
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > The shape of this week is still ahead.
2026-04-20  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-04-21  [momentum]  quietStretch / stage 1 / reflective / mo.quiet.21
  rule:  momentum.quietStretch
  fired: active on four or fewer of the last fourteen days, active on at least one, so no day count renders as zero, the app has existed for the 14 days being described, there are fourteen days to have been quiet across
  > The fortnight is much as it started.
2026-04-21  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-04-21  [pulse]  persistence / stage 4 / plain / persistence.s4.22
  rule:  pulse.persistence.s4
  fired: the active item is thirty days old or more, the area holding the item has at least 1 events in the window, this item genuinely holds the longest ever active record, without which stage 4's historical language would be false
  facts: ageDays=37 [item.itemAgeDays:itm-105], areaName=Work, itemTitle=Set up the new laptop
  > Set up the new laptop has been the active item in Work for one month.
  > Blocked, or just big?
  ? Needs breaking up | Fine as it is

2026-04-22  [momentum]  quietStretch / stage 1 / reflective / mo.quiet.23
  rule:  momentum.quietStretch
  fired: active on four or fewer of the last fourteen days, active on at least one, so no day count renders as zero, the app has existed for the 14 days being described, there are fourteen days to have been quiet across
  > A fortnight without much in it.
2026-04-22  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-04-22  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-04-26  [momentum]  quietStretch / stage 1 / reflective / mo.quiet.31
  rule:  momentum.quietStretch
  fired: active on four or fewer of the last fourteen days, active on at least one, so no day count renders as zero, the app has existed for the 14 days being described, there are fourteen days to have been quiet across
  > Two weeks with a little in them.
2026-04-26  [banner]  weekStarting / stage 1 / reflective / bn.start.25
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > A week at the point where nothing is decided.
2026-04-26  [pulse]  quietDay / stage 3 / plain / quietday.s3.35
  rule:  pulse.quietDay.s3
  fired: four or more days running with nothing in them at all, there is at least a day of history, so this is a quiet day and not an empty install
  > Every area has been where it is for days.
  > How has the stretch been?
  ? A season | A break | A stall
2026-04-26  [report headline]  comeback / stage 1 / plain / hd.back.12
  rule:  report.headline.comeback
  fired: this area returned after a long dormancy, the area has at least 1 events in the window, so naming it cannot be a phantom claim
  facts: areaName=Work
  > Work has a week again.
2026-04-26  [report observation]  areaRevival / stage 1 / observational / ob.rev.l16
  rule:  report.observation.areaRevival
  fired: this area returned after a dormancy, the area has at least 1 events in the window, so naming it cannot be a phantom claim
  facts: areaName=Work
  > Work is on the week's list again.
2026-04-26  [report observation]  dayShape / stage 1 / observational / ob.day.l03
  rule:  report.observation.dayShape
  fired: one day of the week stands out and can be named, that day holds a third of the week or more
  facts: n=2 [window.activeDays]
  > 2 of seven days had activity.
2026-04-26  [report observation]  neglectedArea / stage 2 / neutral_agent / ob.neg.s2.l08
  rule:  report.observation.neglectedArea.s2
  fired: the area has been silent fourteen days or more, the area has real history, so this is a silence and not a new area
  facts: ageDays=52 [area.areaDaysSinceLastEvent:health], areaName=Health
  > 1 month have passed without anything moving in Health.
2026-04-26  [report observation]  queuePressure / stage 1 / neutral_agent / ob.qp.l12
  rule:  report.observation.queuePressure
  fired: at least one area's queue grew, the queues hold three or more things to count
  facts: areaCount=1 [rollup.areasWithQueue]
  > Something waits in 1 area.
2026-04-26  [report pattern]  areaGoneQuiet / stage 1 / plain / pt.gone.01
  rule:  report.pattern.areaGoneQuiet
  fired: the area has had nothing in it for three weeks, it used to move, so this is a stop rather than an empty heading, there are at least three weeks of snapshots, without which no pattern may fire
  facts: ageDays=54 [area.areaDaysSinceLastEvent:side], areaName=Side project
  > Side project went quiet 1 month ago. It has not moved since.

2026-04-28  [momentum]  quietStretch / stage 1 / reflective / mo.quiet.30
  rule:  momentum.quietStretch
  fired: active on four or fewer of the last fourteen days, active on at least one, so no day count renders as zero, the app has existed for the 14 days being described, there are fourteen days to have been quiet across
  > A fortnight of small movements.
2026-04-28  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-04-28  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
```

### May 2026

10 days on screen, 2 Pulses spoken, 8 Pulse days silent, 20 sentences in all.

```text
2026-05-05  [momentum]  quietStretch / stage 1 / reflective / mo.quiet.33
  rule:  momentum.quietStretch
  fired: active on four or fewer of the last fourteen days, active on at least one, so no day count renders as zero, the app has existed for the 14 days being described, there are fourteen days to have been quiet across
  > Two weeks of small things.
2026-05-05  [banner]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-05-05  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-05-08  [momentum]  quietStretch / stage 1 / reflective / mo.quiet.25
  rule:  momentum.quietStretch
  fired: active on four or fewer of the last fourteen days, active on at least one, so no day count renders as zero, the app has existed for the 14 days being described, there are fourteen days to have been quiet across
  > Little movement, across a fortnight.
2026-05-08  [banner]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-05-08  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-05-10  [momentum]  cleanSlate / stage 1 / reflective / mo.clean.13
  rule:  momentum.cleanSlate
  fired: no events at all in the window, there are areas, so there is an app to have a clean slate in
  > A page waiting for something to put on it.
2026-05-10  [banner]  weekStarting / stage 1 / reflective / bn.start.23
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > Nothing here has had time to become a shape.
2026-05-10  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-05-10  [report headline]  decliningActivity / stage 1 / plain / hd.decline.10
  rule:  report.headline.decliningActivity
  fired: total activity has fallen three weeks running, there are at least three weeks of snapshots, without which no pattern may fire
  > The line is going down.
2026-05-10  [report observation]  neglectedArea / stage 1 / neutral_agent / ob.neg.s1.l09
  rule:  report.observation.neglectedArea.s1
  fired: the area has been silent seven to thirteen days, the area has real history, so this is a silence and not a new area
  facts: ageDays=13 [area.areaDaysSinceLastEvent:work], areaName=Work
  > 13 days without an event in Work.
2026-05-10  [report pattern]  areaGoneQuiet / stage 1 / plain / pt.gone.07
  rule:  report.pattern.areaGoneQuiet
  fired: the area has had nothing in it for three weeks, it used to move, so this is a stop rather than an empty heading, there are at least three weeks of snapshots, without which no pattern may fire
  facts: areaName=Health
  > Three weeks of nothing in Health.

2026-05-11  [momentum]  quietStretch / stage 1 / reflective / mo.quiet.29
  rule:  momentum.quietStretch
  fired: active on four or fewer of the last fourteen days, active on at least one, so no day count renders as zero, the app has existed for the 14 days being described, there are fourteen days to have been quiet across
  > Fourteen days, little changed.
2026-05-11  [banner]  weekStarting / stage 1 / reflective / bn.start.14
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > Too early for much of a reading.
2026-05-11  [pulse]  persistence / stage 4 / plain / persistence.s4.21
  rule:  pulse.persistence.s4
  fired: the active item is thirty days old or more, the area holding the item has at least 1 events in the window, this item genuinely holds the longest ever active record, without which stage 4's historical language would be false
  facts: ageDays=57 [item.itemAgeDays:itm-105]
  > One item, one month.
  > Is it one thing, or many?
  ? Right place | Wrong place

2026-05-12  [momentum]  quietStretch / stage 1 / reflective / mo.quiet.31
  rule:  momentum.quietStretch
  fired: active on four or fewer of the last fourteen days, active on at least one, so no day count renders as zero, the app has existed for the 14 days being described, there are fourteen days to have been quiet across
  > Two weeks with a little in them.
2026-05-12  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-05-12  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-05-14  [momentum]  quietStretch / stage 1 / reflective / mo.quiet.20
  rule:  momentum.quietStretch
  fired: active on four or fewer of the last fourteen days, active on at least one, so no day count renders as zero, the app has existed for the 14 days being described, there are fourteen days to have been quiet across
  > A stretch that stayed where it was.
2026-05-14  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-05-14  [pulse]  quietDay / stage 2 / plain / quietday.s2.22
  rule:  pulse.quietDay.s2
  fired: two or three days running with nothing in them at all, there is at least a day of history, so this is a quiet day and not an empty install
  > Still where it all was.
  > A break you took, or one you got?
  ? Needed the days | Out doing things | Under it

2026-05-16  [momentum]  quietStretch / stage 1 / reflective / mo.quiet.20
  rule:  momentum.quietStretch
  fired: active on four or fewer of the last fourteen days, active on at least one, so no day count renders as zero, the app has existed for the 14 days being described, there are fourteen days to have been quiet across
  > A stretch that stayed where it was.
2026-05-16  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-05-16  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-05-24  [momentum]  quietStretch / stage 1 / reflective / mo.quiet.21
  rule:  momentum.quietStretch
  fired: active on four or fewer of the last fourteen days, active on at least one, so no day count renders as zero, the app has existed for the 14 days being described, there are fourteen days to have been quiet across
  > The fortnight is much as it started.
2026-05-24  [banner]  weekStarting / stage 1 / reflective / bn.start.19
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > Little to read this early.
2026-05-24  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-05-24  [report headline]  SILENT (NO_RULE_QUALIFIED)
2026-05-24  [report observation]  neglectedArea / stage 1 / neutral_agent / ob.neg.s1.l08
  rule:  report.observation.neglectedArea.s1
  fired: the area has been silent seven to thirteen days, the area has real history, so this is a silence and not a new area
  facts: ageDays=12 [area.areaDaysSinceLastEvent:work], areaName=Work
  > 12 days passed in Work with nothing in them.
2026-05-24  [report pattern]  areaGoneQuiet / stage 1 / plain / pt.gone.05
  rule:  report.pattern.areaGoneQuiet
  fired: the area has had nothing in it for three weeks, it used to move, so this is a stop rather than an empty heading, there are at least three weeks of snapshots, without which no pattern may fire
  facts: areaName=Health
  > Health used to move every week. It has not moved in three.

2026-05-26  [momentum]  quietStretch / stage 1 / reflective / mo.quiet.08
  rule:  momentum.quietStretch
  fired: active on four or fewer of the last fourteen days, active on at least one, so no day count renders as zero, the app has existed for the 14 days being described, there are fourteen days to have been quiet across
  > Quiet, across two weeks.
2026-05-26  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-05-26  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-05-27  [momentum]  quietStretch / stage 1 / reflective / mo.quiet.26
  rule:  momentum.quietStretch
  fired: active on four or fewer of the last fourteen days, active on at least one, so no day count renders as zero, the app has existed for the 14 days being described, there are fourteen days to have been quiet across
  > A fortnight that kept to itself.
2026-05-27  [banner]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-05-27  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
```

### June 2026

6 days on screen, 2 Pulses spoken, 4 Pulse days silent, 8 sentences in all.

```text
2026-06-02  [momentum]  quietStretch / stage 1 / reflective / mo.quiet.33
  rule:  momentum.quietStretch
  fired: active on four or fewer of the last fourteen days, active on at least one, so no day count renders as zero, the app has existed for the 14 days being described, there are fourteen days to have been quiet across
  > Two weeks of small things.
2026-06-02  [banner]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-06-02  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-06-03  [momentum]  quietStretch / stage 1 / reflective / mo.quiet.19
  rule:  momentum.quietStretch
  fired: active on four or fewer of the last fourteen days, active on at least one, so no day count renders as zero, the app has existed for the 14 days being described, there are fourteen days to have been quiet across
  > Two weeks that mostly stayed as they were.
2026-06-03  [banner]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-06-03  [pulse]  persistence / stage 4 / plain / persistence.s4.51
  rule:  pulse.persistence.s4
  fired: the active item is thirty days old or more, the area holding the item has at least 1 events in the window, this item genuinely holds the longest ever active record, without which stage 4's historical language would be false
  facts: areaName=Work
  > Work has one name on it.
  > Would you add it today?
  ? Still the work | Something else now

2026-06-05  [momentum]  quietStretch / stage 1 / reflective / mo.quiet.08
  rule:  momentum.quietStretch
  fired: active on four or fewer of the last fourteen days, active on at least one, so no day count renders as zero, the app has existed for the 14 days being described, there are fourteen days to have been quiet across
  > Quiet, across two weeks.
2026-06-05  [banner]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-06-05  [pulse]  quietDay / stage 1 / plain / quietday.s1.20
  rule:  pulse.quietDay.s1
  fired: fewer than two events in the window, per the family trigger, there is at least a day of history, so this is a quiet day and not an empty install
  > A day with nothing in it.
  > How was it, actually?
  ? Fine | Busy | Struggling

2026-06-11  [momentum]  quietStretch / stage 1 / reflective / mo.quiet.22
  rule:  momentum.quietStretch
  fired: active on four or fewer of the last fourteen days, active on at least one, so no day count renders as zero, the app has existed for the 14 days being described, there are fourteen days to have been quiet across
  > A stretch with little in it.
2026-06-11  [banner]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-06-11  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-06-16  [momentum]  quietStretch / stage 1 / reflective / mo.quiet.11
  rule:  momentum.quietStretch
  fired: active on four or fewer of the last fourteen days, active on at least one, so no day count renders as zero, the app has existed for the 14 days being described, there are fourteen days to have been quiet across
  > A still fortnight.
2026-06-16  [banner]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-06-16  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-06-27  [momentum]  cleanSlate / stage 1 / reflective / mo.clean.26
  rule:  momentum.cleanSlate
  fired: no events at all in the window, there are areas, so there is an app to have a clean slate in
  > Two weeks that came and went.
2026-06-27  [banner]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-06-27  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
```

### July 2026

11 days on screen, 0 Pulses spoken, 11 Pulse days silent, 16 sentences in all.

```text
2026-07-04  [momentum]  cleanSlate / stage 1 / reflective / mo.clean.11
  rule:  momentum.cleanSlate
  fired: no events at all in the window, there are areas, so there is an app to have a clean slate in
  > Fourteen days with nothing in them.
2026-07-04  [banner]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-07-04  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-07-10  [momentum]  cleanSlate / stage 1 / reflective / mo.clean.10
  rule:  momentum.cleanSlate
  fired: no events at all in the window, there are areas, so there is an app to have a clean slate in
  > A page with nothing on it.
2026-07-10  [banner]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-07-10  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-07-13  [momentum]  cleanSlate / stage 1 / reflective / mo.clean.26
  rule:  momentum.cleanSlate
  fired: no events at all in the window, there are areas, so there is an app to have a clean slate in
  > Two weeks that came and went.
2026-07-13  [banner]  weekStarting / stage 1 / reflective / bn.start.09
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > Early yet.
2026-07-13  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-07-15  [momentum]  cleanSlate / stage 1 / reflective / mo.clean.10
  rule:  momentum.cleanSlate
  fired: no events at all in the window, there are areas, so there is an app to have a clean slate in
  > A page with nothing on it.
2026-07-15  [banner]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-07-15  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-07-20  [momentum]  quietStretch / stage 1 / reflective / mo.quiet.33
  rule:  momentum.quietStretch
  fired: active on four or fewer of the last fourteen days, active on at least one, so no day count renders as zero, the app has existed for the 14 days being described, there are fourteen days to have been quiet across
  > Two weeks of small things.
2026-07-20  [banner]  weekStarting / stage 1 / reflective / bn.start.21
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > Early, before anything settles.
2026-07-20  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-07-22  [momentum]  quietStretch / stage 1 / reflective / mo.quiet.18
  rule:  momentum.quietStretch
  fired: active on four or fewer of the last fourteen days, active on at least one, so no day count renders as zero, the app has existed for the 14 days being described, there are fourteen days to have been quiet across
  > The last two weeks, mostly unchanged.
2026-07-22  [banner]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-07-22  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-07-23  [momentum]  quietStretch / stage 1 / reflective / mo.quiet.17
  rule:  momentum.quietStretch
  fired: active on four or fewer of the last fourteen days, active on at least one, so no day count renders as zero, the app has existed for the 14 days being described, there are fourteen days to have been quiet across
  > Still, rather than busy.
2026-07-23  [banner]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-07-23  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-07-26  [momentum]  quietStretch / stage 1 / reflective / mo.quiet.20
  rule:  momentum.quietStretch
  fired: active on four or fewer of the last fourteen days, active on at least one, so no day count renders as zero, the app has existed for the 14 days being described, there are fourteen days to have been quiet across
  > A stretch that stayed where it was.
2026-07-26  [banner]  weekStarting / stage 1 / reflective / bn.start.21
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > Early, before anything settles.
2026-07-26  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-07-26  [report headline]  SILENT (NO_RULE_QUALIFIED)
2026-07-26  [report observation]  neglectedArea / stage 1 / neutral_agent / ob.neg.s1.l09
  rule:  report.observation.neglectedArea.s1
  fired: the area has been silent seven to thirteen days, the area has real history, so this is a silence and not a new area
  facts: ageDays=10 [area.areaDaysSinceLastEvent:work], areaName=Work
  > 10 days without an event in Work.
2026-07-26  [report pattern]  queueEquilibrium / stage 1 / plain / pt.eq.02
  rule:  report.pattern.queueEquilibrium
  fired: the queues have held the same length for four weeks, there are four weeks to see it across, there is a queue to be in balance
  > What goes in and what comes out have matched for a month.

2026-07-28  [momentum]  quietStretch / stage 1 / reflective / mo.quiet.19
  rule:  momentum.quietStretch
  fired: active on four or fewer of the last fourteen days, active on at least one, so no day count renders as zero, the app has existed for the 14 days being described, there are fourteen days to have been quiet across
  > Two weeks that mostly stayed as they were.
2026-07-28  [banner]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-07-28  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-07-29  [momentum]  cleanSlate / stage 1 / reflective / mo.clean.12
  rule:  momentum.cleanSlate
  fired: no events at all in the window, there are areas, so there is an app to have a clean slate in
  > A fortnight with nothing in it.
2026-07-29  [banner]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-07-29  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-07-30  [momentum]  cleanSlate / stage 1 / reflective / mo.clean.30
  rule:  momentum.cleanSlate
  fired: no events at all in the window, there are areas, so there is an app to have a clean slate in
  > Nothing yet, across the whole fortnight.
2026-07-30  [banner]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-07-30  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
```

### August 2026

10 days on screen, 0 Pulses spoken, 10 Pulse days silent, 14 sentences in all.

```text
2026-08-01  [momentum]  cleanSlate / stage 1 / reflective / mo.clean.18
  rule:  momentum.cleanSlate
  fired: no events at all in the window, there are areas, so there is an app to have a clean slate in
  > A page that has nothing to say yet.
2026-08-01  [banner]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-08-01  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-08-07  [momentum]  cleanSlate / stage 1 / reflective / mo.clean.11
  rule:  momentum.cleanSlate
  fired: no events at all in the window, there are areas, so there is an app to have a clean slate in
  > Fourteen days with nothing in them.
2026-08-07  [banner]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-08-07  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-08-09  [momentum]  cleanSlate / stage 1 / reflective / mo.clean.16
  rule:  momentum.cleanSlate
  fired: no events at all in the window, there are areas, so there is an app to have a clean slate in
  > Two weeks with the page unchanged.
2026-08-09  [banner]  weekStarting / stage 1 / reflective / bn.start.21
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > Early, before anything settles.
2026-08-09  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-08-09  [report headline]  SILENT (NO_RULE_QUALIFIED)
2026-08-09  [report observation]  neglectedArea / stage 2 / neutral_agent / ob.neg.s2.l09
  rule:  report.observation.neglectedArea.s2
  fired: the area has been silent fourteen days or more, the area has real history, so this is a silence and not a new area
  facts: areaName=Work, sinceRef=2026-07-16
  > Work has been still since July.
2026-08-09  [report pattern]  areaGoneQuiet / stage 1 / plain / pt.gone.01
  rule:  report.pattern.areaGoneQuiet
  fired: the area has had nothing in it for three weeks, it used to move, so this is a stop rather than an empty heading, there are at least three weeks of snapshots, without which no pattern may fire
  facts: ageDays=24 [area.areaDaysSinceLastEvent:work], areaName=Work
  > Work went quiet 3 weeks ago. It has not moved since.

2026-08-10  [momentum]  cleanSlate / stage 1 / reflective / mo.clean.11
  rule:  momentum.cleanSlate
  fired: no events at all in the window, there are areas, so there is an app to have a clean slate in
  > Fourteen days with nothing in them.
2026-08-10  [banner]  weekStarting / stage 1 / reflective / bn.start.09
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > Early yet.
2026-08-10  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-08-13  [momentum]  cleanSlate / stage 1 / reflective / mo.clean.21
  rule:  momentum.cleanSlate
  fired: no events at all in the window, there are areas, so there is an app to have a clean slate in
  > Nothing across the last fourteen days.
2026-08-13  [banner]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-08-13  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-08-14  [momentum]  cleanSlate / stage 1 / reflective / mo.clean.28
  rule:  momentum.cleanSlate
  fired: no events at all in the window, there are areas, so there is an app to have a clean slate in
  > Empty across two weeks.
2026-08-14  [banner]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-08-14  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-08-15  [momentum]  cleanSlate / stage 1 / reflective / mo.clean.15
  rule:  momentum.cleanSlate
  fired: no events at all in the window, there are areas, so there is an app to have a clean slate in
  > The last fourteen days are blank.
2026-08-15  [banner]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-08-15  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-08-20  [momentum]  quietStretch / stage 1 / reflective / mo.quiet.11
  rule:  momentum.quietStretch
  fired: active on four or fewer of the last fourteen days, active on at least one, so no day count renders as zero, the app has existed for the 14 days being described, there are fourteen days to have been quiet across
  > A still fortnight.
2026-08-20  [banner]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-08-20  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-08-26  [momentum]  quietStretch / stage 1 / reflective / mo.quiet.24
  rule:  momentum.quietStretch
  fired: active on four or fewer of the last fourteen days, active on at least one, so no day count renders as zero, the app has existed for the 14 days being described, there are fourteen days to have been quiet across
  > A quiet stretch of days.
2026-08-26  [banner]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-08-26  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-08-29  [momentum]  cleanSlate / stage 1 / reflective / mo.clean.13
  rule:  momentum.cleanSlate
  fired: no events at all in the window, there are areas, so there is an app to have a clean slate in
  > A page waiting for something to put on it.
2026-08-29  [banner]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-08-29  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
```

### September 2026

7 days on screen, 0 Pulses spoken, 7 Pulse days silent, 8 sentences in all.

```text
2026-09-03  [momentum]  cleanSlate / stage 1 / reflective / mo.clean.17
  rule:  momentum.cleanSlate
  fired: no events at all in the window, there are areas, so there is an app to have a clean slate in
  > An empty two weeks, waiting.
2026-09-03  [banner]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-09-03  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-09-11  [momentum]  cleanSlate / stage 1 / reflective / mo.clean.14
  rule:  momentum.cleanSlate
  fired: no events at all in the window, there are areas, so there is an app to have a clean slate in
  > Nothing has happened here in two weeks.
2026-09-11  [banner]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-09-11  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-09-16  [momentum]  cleanSlate / stage 1 / reflective / mo.clean.07
  rule:  momentum.cleanSlate
  fired: no events at all in the window, there are areas, so there is an app to have a clean slate in
  > Nothing here yet.
2026-09-16  [banner]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-09-16  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-09-17  [momentum]  cleanSlate / stage 1 / reflective / mo.clean.30
  rule:  momentum.cleanSlate
  fired: no events at all in the window, there are areas, so there is an app to have a clean slate in
  > Nothing yet, across the whole fortnight.
2026-09-17  [banner]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-09-17  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-09-19  [momentum]  cleanSlate / stage 1 / reflective / mo.clean.19
  rule:  momentum.cleanSlate
  fired: no events at all in the window, there are areas, so there is an app to have a clean slate in
  > Two weeks with no marks on them.
2026-09-19  [banner]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-09-19  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-09-21  [momentum]  cleanSlate / stage 1 / reflective / mo.clean.27
  rule:  momentum.cleanSlate
  fired: no events at all in the window, there are areas, so there is an app to have a clean slate in
  > A page with the last fortnight still blank.
2026-09-21  [banner]  weekStarting / stage 1 / reflective / bn.start.15
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > Nothing settled yet.
2026-09-21  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-09-24  [momentum]  cleanSlate / stage 1 / reflective / mo.clean.10
  rule:  momentum.cleanSlate
  fired: no events at all in the window, there are areas, so there is an app to have a clean slate in
  > A page with nothing on it.
2026-09-24  [banner]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-09-24  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
```

### October 2026

12 days on screen, 2 Pulses spoken, 10 Pulse days silent, 31 sentences in all.

```text
2026-10-01  [momentum]  cleanSlate / stage 1 / reflective / mo.clean.19
  rule:  momentum.cleanSlate
  fired: no events at all in the window, there are areas, so there is an app to have a clean slate in
  > Two weeks with no marks on them.
2026-10-01  [banner]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-10-01  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-10-02  [momentum]  cleanSlate / stage 1 / reflective / mo.clean.18
  rule:  momentum.cleanSlate
  fired: no events at all in the window, there are areas, so there is an app to have a clean slate in
  > A page that has nothing to say yet.
2026-10-02  [banner]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-10-02  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-10-06  [momentum]  cleanSlate / stage 1 / reflective / mo.clean.17
  rule:  momentum.cleanSlate
  fired: no events at all in the window, there are areas, so there is an app to have a clean slate in
  > An empty two weeks, waiting.
2026-10-06  [banner]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-10-06  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-10-07  [momentum]  cleanSlate / stage 1 / reflective / mo.clean.23
  rule:  momentum.cleanSlate
  fired: no events at all in the window, there are areas, so there is an app to have a clean slate in
  > An empty stretch of fourteen days.
2026-10-07  [banner]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-10-07  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-10-11  [momentum]  cleanSlate / stage 1 / reflective / mo.clean.16
  rule:  momentum.cleanSlate
  fired: no events at all in the window, there are areas, so there is an app to have a clean slate in
  > Two weeks with the page unchanged.
2026-10-11  [banner]  weekStarting / stage 1 / reflective / bn.start.16
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > A week still mostly ahead.
2026-10-11  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-10-11  [report headline]  SILENT (NO_RULE_QUALIFIED)
2026-10-11  [report observation]  neglectedArea / stage 2 / neutral_agent / ob.neg.s2.l08
  rule:  report.observation.neglectedArea.s2
  fired: the area has been silent fourteen days or more, the area has real history, so this is a silence and not a new area
  facts: ageDays=56 [area.areaDaysSinceLastEvent:work], areaName=Work
  > 1 month have passed without anything moving in Work.
2026-10-11  [report pattern]  areaGoneQuiet / stage 1 / plain / pt.gone.03
  rule:  report.pattern.areaGoneQuiet
  fired: the area has had nothing in it for three weeks, it used to move, so this is a stop rather than an empty heading, there are at least three weeks of snapshots, without which no pattern may fire
  facts: areaName=Work, sinceRef=2026-08-16
  > Work has been silent since August.

2026-10-12  [momentum]  quietStretch / stage 1 / reflective / mo.quiet.08
  rule:  momentum.quietStretch
  fired: active on four or fewer of the last fourteen days, active on at least one, so no day count renders as zero, the app has existed for the 14 days being described, there are fourteen days to have been quiet across
  > Quiet, across two weeks.
2026-10-12  [banner]  weekStarting / stage 1 / reflective / bn.start.21
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > Early, before anything settles.
2026-10-12  [pulse]  rebalance / stage 2 / plain / rebalance.s2.25
  rule:  pulse.rebalance.s2
  fired: the area had been still fourteen days or more before it moved, the area has at least 1 events in the window, so naming it cannot be a phantom claim
  facts: ageDays=57 [area.areaDormancyDays:work], areaName=Work
  > Activity in Work, after one month.
  > Is this one of those that goes in cycles?
  ? A return | A last try

2026-10-18  [momentum]  quietStretch / stage 1 / reflective / mo.quiet.06
  rule:  momentum.quietStretch
  fired: active on four or fewer of the last fourteen days, active on at least one, so no day count renders as zero, the app has existed for the 14 days being described, there are fourteen days to have been quiet across
  > A slow stretch.
2026-10-18  [banner]  weekStarting / stage 1 / reflective / bn.start.18
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > A beginning.
2026-10-18  [pulse]  quietDay / stage 3 / plain / quietday.s3.26
  rule:  pulse.quietDay.s3
  fired: four or more days running with nothing in them at all, there is at least a day of history, so this is a quiet day and not an empty install
  > No change for days.
  > Busy somewhere else?
  ? Recharging | Busy elsewhere | Overwhelmed
2026-10-18  [report headline]  comeback / stage 1 / plain / hd.back.09
  rule:  report.headline.comeback
  fired: this area returned after a long dormancy, the area has at least 1 events in the window, so naming it cannot be a phantom claim
  facts: areaName=Work
  > Work is moving again.
2026-10-18  [report observation]  areaRevival / stage 1 / observational / ob.rev.l20
  rule:  report.observation.areaRevival
  fired: this area returned after a dormancy, the area has at least 1 events in the window, so naming it cannot be a phantom claim
  facts: ageDays=57 [area.areaDormancyDays:work], areaName=Work
  > Work started moving again after 1 month.
2026-10-18  [report observation]  queuePressure / stage 1 / neutral_agent / ob.qp.l26
  rule:  report.observation.queuePressure
  fired: at least one area's queue grew, the queues hold three or more things to count
  facts: n=17 [rollup.queueTotal]
  > 17 things wait behind the items that are moving.
2026-10-18  [report observation]  quietWeek / stage 1 / neutral_agent / ob.quiet.l11
  rule:  report.observation.quietWeek
  fired: the week holds fewer events than it has days, something happened, so the week is quiet rather than absent
  facts: n=2 [window.totalEvents]
  > Seven days, 2 things.
2026-10-18  [report pattern]  queueEquilibrium / stage 1 / plain / pt.eq.01
  rule:  report.pattern.queueEquilibrium
  fired: the queues have held the same length for four weeks, there are four weeks to see it across, there is a queue to be in balance
  > Your queues have held the same length for four weeks.

2026-10-20  [momentum]  quietStretch / stage 1 / reflective / mo.quiet.08
  rule:  momentum.quietStretch
  fired: active on four or fewer of the last fourteen days, active on at least one, so no day count renders as zero, the app has existed for the 14 days being described, there are fourteen days to have been quiet across
  > Quiet, across two weeks.
2026-10-20  [banner]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-10-20  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-10-21  [momentum]  quietStretch / stage 1 / reflective / mo.quiet.06
  rule:  momentum.quietStretch
  fired: active on four or fewer of the last fourteen days, active on at least one, so no day count renders as zero, the app has existed for the 14 days being described, there are fourteen days to have been quiet across
  > A slow stretch.
2026-10-21  [banner]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-10-21  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-10-23  [momentum]  quietStretch / stage 1 / reflective / mo.quiet.30
  rule:  momentum.quietStretch
  fired: active on four or fewer of the last fourteen days, active on at least one, so no day count renders as zero, the app has existed for the 14 days being described, there are fourteen days to have been quiet across
  > A fortnight of small movements.
2026-10-23  [banner]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-10-23  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-10-24  [momentum]  quietStretch / stage 1 / reflective / mo.quiet.23
  rule:  momentum.quietStretch
  fired: active on four or fewer of the last fourteen days, active on at least one, so no day count renders as zero, the app has existed for the 14 days being described, there are fourteen days to have been quiet across
  > A fortnight without much in it.
2026-10-24  [banner]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-10-24  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-10-25  [momentum]  quietStretch / stage 1 / reflective / mo.quiet.23
  rule:  momentum.quietStretch
  fired: active on four or fewer of the last fourteen days, active on at least one, so no day count renders as zero, the app has existed for the 14 days being described, there are fourteen days to have been quiet across
  > A fortnight without much in it.
2026-10-25  [banner]  weekStarting / stage 1 / reflective / bn.start.22
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > There is more week ahead than behind.
2026-10-25  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-10-25  [report headline]  comeback / stage 1 / plain / hd.back.33
  rule:  report.headline.comeback
  fired: this area returned after a long dormancy, the area has at least 1 events in the window, so naming it cannot be a phantom claim
  > The quiet broke.
*** hardStretch, the difficulty register of 6.4. Read it as a friend saying it. ***
2026-10-25  [report observation]  hardStretch / stage 1 / neutral_agent / ob.hard.l07
  rule:  report.observation.hardStretch.quiet
  fired: three or more weeks running below one event a day, and the queues grew across them, there are at least three weeks of snapshots, without which no pattern may fire
  > A period like this is not a failure of the system you set up.
2026-10-25  [report observation]  areaRevival / stage 1 / observational / ob.rev.l15
  rule:  report.observation.areaRevival
  fired: this area returned after a dormancy, the area has at least 1 events in the window, so naming it cannot be a phantom claim
  facts: ageDays=13 [area.areaDormancyDays:work]
  > A gap of 13 days, now closed.
2026-10-25  [report observation]  queuePressure / stage 1 / neutral_agent / ob.qp.l24
  rule:  report.observation.queuePressure
  fired: at least one area's queue grew, the queues hold three or more things to count
  facts: n=18 [rollup.queueTotal]
  > The queues came out of the week holding 18 things.
2026-10-25  [report observation]  quietWeek / stage 1 / neutral_agent / ob.quiet.l10
  rule:  report.observation.quietWeek
  fired: the week holds fewer events than it has days, something happened, so the week is quiet rather than absent
  > The queues held.
2026-10-25  [report pattern]  reportedVsActual / stage 1 / plain / pt.rva.41
  rule:  report.pattern.reportedVsActual
  fired: there are several stored answers to compare against, one family has been answered about more than twice, there are at least three weeks of snapshots, without which no pattern may fire
  > A question the app has asked you more than twice.
```

### November 2026

10 days on screen, 0 Pulses spoken, 10 Pulse days silent, 25 sentences in all.

```text
2026-11-01  [momentum]  quietStretch / stage 1 / reflective / mo.quiet.23
  rule:  momentum.quietStretch
  fired: active on four or fewer of the last fourteen days, active on at least one, so no day count renders as zero, the app has existed for the 14 days being described, there are fourteen days to have been quiet across
  > A fortnight without much in it.
2026-11-01  [banner]  weekStarting / stage 1 / reflective / bn.start.24
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > The shape of this week is still ahead.
2026-11-01  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-11-01  [report headline]  SILENT (NO_RULE_QUALIFIED)
2026-11-01  [report observation]  neglectedArea / stage 1 / neutral_agent / ob.neg.s1.l09
  rule:  report.observation.neglectedArea.s1
  fired: the area has been silent seven to thirteen days, the area has real history, so this is a silence and not a new area
  facts: ageDays=7 [area.areaDaysSinceLastEvent:work], areaName=Work
  > 7 days without an event in Work.
2026-11-01  [report pattern]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-11-06  [momentum]  quietStretch / stage 1 / reflective / mo.quiet.11
  rule:  momentum.quietStretch
  fired: active on four or fewer of the last fourteen days, active on at least one, so no day count renders as zero, the app has existed for the 14 days being described, there are fourteen days to have been quiet across
  > A still fortnight.
2026-11-06  [banner]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-11-06  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-11-07  [momentum]  cleanSlate / stage 1 / reflective / mo.clean.27
  rule:  momentum.cleanSlate
  fired: no events at all in the window, there are areas, so there is an app to have a clean slate in
  > A page with the last fortnight still blank.
2026-11-07  [banner]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-11-07  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-11-08  [momentum]  cleanSlate / stage 1 / reflective / mo.clean.18
  rule:  momentum.cleanSlate
  fired: no events at all in the window, there are areas, so there is an app to have a clean slate in
  > A page that has nothing to say yet.
2026-11-08  [banner]  weekStarting / stage 1 / reflective / bn.start.21
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > Early, before anything settles.
2026-11-08  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-11-08  [report headline]  SILENT (NO_RULE_QUALIFIED)
2026-11-08  [report observation]  neglectedArea / stage 2 / neutral_agent / ob.neg.s2.l09
  rule:  report.observation.neglectedArea.s2
  fired: the area has been silent fourteen days or more, the area has real history, so this is a silence and not a new area
  facts: areaName=Work, sinceRef=2026-10-25
  > Work has been still since October.
2026-11-08  [report pattern]  queueEquilibrium / stage 1 / plain / pt.eq.05
  rule:  report.pattern.queueEquilibrium
  fired: the queues have held the same length for four weeks, there are four weeks to see it across, there is a queue to be in balance
  > The queues have been stable for a month.

2026-11-16  [momentum]  cleanSlate / stage 1 / reflective / mo.clean.24
  rule:  momentum.cleanSlate
  fired: no events at all in the window, there are areas, so there is an app to have a clean slate in
  > A stretch of days with nothing on it.
2026-11-16  [banner]  weekStarting / stage 1 / reflective / bn.start.22
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > There is more week ahead than behind.
2026-11-16  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-11-20  [momentum]  cleanSlate / stage 1 / reflective / mo.clean.18
  rule:  momentum.cleanSlate
  fired: no events at all in the window, there are areas, so there is an app to have a clean slate in
  > A page that has nothing to say yet.
2026-11-20  [banner]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-11-20  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-11-21  [momentum]  cleanSlate / stage 1 / reflective / mo.clean.09
  rule:  momentum.cleanSlate
  fired: no events at all in the window, there are areas, so there is an app to have a clean slate in
  > Still blank, for now.
2026-11-21  [banner]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-11-21  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-11-22  [momentum]  cleanSlate / stage 1 / reflective / mo.clean.15
  rule:  momentum.cleanSlate
  fired: no events at all in the window, there are areas, so there is an app to have a clean slate in
  > The last fourteen days are blank.
2026-11-22  [banner]  weekStarting / stage 1 / reflective / bn.start.19
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > Little to read this early.
2026-11-22  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-11-22  [report headline]  SILENT (NO_RULE_QUALIFIED)
2026-11-22  [report observation]  neglectedArea / stage 2 / neutral_agent / ob.neg.s2.l10
  rule:  report.observation.neglectedArea.s2
  fired: the area has been silent fourteen days or more, the area has real history, so this is a silence and not a new area
  facts: areaName=Work, n=18 [area.areaQueue:work]
  > 18 things sit in Work, untouched.
2026-11-22  [report pattern]  areaGoneQuiet / stage 1 / plain / pt.gone.01
  rule:  report.pattern.areaGoneQuiet
  fired: the area has had nothing in it for three weeks, it used to move, so this is a stop rather than an empty heading, there are at least three weeks of snapshots, without which no pattern may fire
  facts: ageDays=28 [area.areaDaysSinceLastEvent:work], areaName=Work
  > Work went quiet 4 weeks ago. It has not moved since.

2026-11-27  [momentum]  quietStretch / stage 1 / reflective / mo.quiet.06
  rule:  momentum.quietStretch
  fired: active on four or fewer of the last fourteen days, active on at least one, so no day count renders as zero, the app has existed for the 14 days being described, there are fourteen days to have been quiet across
  > A slow stretch.
2026-11-27  [banner]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-11-27  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-11-29  [momentum]  quietStretch / stage 1 / reflective / mo.quiet.15
  rule:  momentum.quietStretch
  fired: active on four or fewer of the last fourteen days, active on at least one, so no day count renders as zero, the app has existed for the 14 days being described, there are fourteen days to have been quiet across
  > A fortnight with a few days in it.
2026-11-29  [banner]  weekStarting / stage 1 / reflective / bn.start.10
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > The start of a week.
2026-11-29  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-11-29  [report headline]  comeback / stage 1 / plain / hd.back.01
  rule:  report.headline.comeback
  fired: this area returned after a long dormancy, the area has at least 1 events in the window, so naming it cannot be a phantom claim
  facts: areaName=Work
  > Work came back.
2026-11-29  [report observation]  areaRevival / stage 1 / observational / ob.rev.l17
  rule:  report.observation.areaRevival
  fired: this area returned after a dormancy, the area has at least 1 events in the window, so naming it cannot be a phantom claim
  facts: ageDays=29 [area.areaDormancyDays:work], areaName=Work
  > Nothing had happened in Work for 4 weeks. This week did.
2026-11-29  [report observation]  queuePressure / stage 1 / neutral_agent / ob.qp.l10
  rule:  report.observation.queuePressure
  fired: at least one area's queue grew, the queues hold three or more things to count
  > The queues are longer.
2026-11-29  [report observation]  quietWeek / stage 1 / neutral_agent / ob.quiet.l10
  rule:  report.observation.quietWeek
  fired: the week holds fewer events than it has days, something happened, so the week is quiet rather than absent
  > The queues held.
2026-11-29  [report pattern]  queueEquilibrium / stage 1 / plain / pt.eq.04
  rule:  report.pattern.queueEquilibrium
  fired: the queues have held the same length for four weeks, there are four weeks to see it across, there is a queue to be in balance
  > Four weeks of balance between intake and output.
```

### December 2026

6 days on screen, 0 Pulses spoken, 6 Pulse days silent, 11 sentences in all.

```text
2026-12-06  [momentum]  cleanSlate / stage 1 / reflective / mo.clean.09
  rule:  momentum.cleanSlate
  fired: no events at all in the window, there are areas, so there is an app to have a clean slate in
  > Still blank, for now.
2026-12-06  [banner]  weekStarting / stage 1 / reflective / bn.start.14
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > Too early for much of a reading.
2026-12-06  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-12-06  [report headline]  SILENT (NO_RULE_QUALIFIED)
2026-12-06  [report observation]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-12-06  [report pattern]  reportedVsActual / stage 1 / plain / pt.rva.51
  rule:  report.pattern.reportedVsActual
  fired: there are several stored answers to compare against, one family has been answered about more than twice, there are at least three weeks of snapshots, without which no pattern may fire
  > Three answers about one kind of moment is not a verdict on anything, and it is a record that the moment kept happening.

2026-12-09  [momentum]  cleanSlate / stage 1 / reflective / mo.clean.26
  rule:  momentum.cleanSlate
  fired: no events at all in the window, there are areas, so there is an app to have a clean slate in
  > Two weeks that came and went.
2026-12-09  [banner]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-12-09  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-12-11  [momentum]  cleanSlate / stage 1 / reflective / mo.clean.03
  rule:  momentum.cleanSlate
  fired: no events at all in the window, there are areas, so there is an app to have a clean slate in
  > Empty, for now.
2026-12-11  [banner]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-12-11  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-12-22  [momentum]  quietStretch / stage 1 / reflective / mo.quiet.21
  rule:  momentum.quietStretch
  fired: active on four or fewer of the last fourteen days, active on at least one, so no day count renders as zero, the app has existed for the 14 days being described, there are fourteen days to have been quiet across
  > The fortnight is much as it started.
2026-12-22  [banner]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-12-22  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-12-23  [momentum]  quietStretch / stage 1 / reflective / mo.quiet.24
  rule:  momentum.quietStretch
  fired: active on four or fewer of the last fourteen days, active on at least one, so no day count renders as zero, the app has existed for the 14 days being described, there are fourteen days to have been quiet across
  > A quiet stretch of days.
2026-12-23  [banner]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-12-23  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-12-27  [momentum]  cleanSlate / stage 1 / reflective / mo.clean.05
  rule:  momentum.cleanSlate
  fired: no events at all in the window, there are areas, so there is an app to have a clean slate in
  > A blank fortnight, waiting.
2026-12-27  [banner]  weekStarting / stage 1 / reflective / bn.start.15
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > Nothing settled yet.
2026-12-27  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-12-27  [report headline]  SILENT (NO_RULE_QUALIFIED)
2026-12-27  [report observation]  neglectedArea / stage 2 / neutral_agent / ob.neg.s2.l08
  rule:  report.observation.neglectedArea.s2
  fired: the area has been silent fourteen days or more, the area has real history, so this is a silence and not a new area
  facts: ageDays=15 [area.areaDaysSinceLastEvent:work], areaName=Work
  > 2 weeks have passed without anything moving in Work.
2026-12-27  [report pattern]  queueEquilibrium / stage 1 / plain / pt.eq.02
  rule:  report.pattern.queueEquilibrium
  fired: the queues have held the same length for four weeks, there are four weeks to see it across, there is a queue to be in balance
  > What goes in and what comes out have matched for a month.
```

### The year in counts

```text
-- counts ------------------------------------------------------
pulse: 153 days, 18 spoken, 135 silent (88 percent silent)
silence reasons: ALL_QUALIFIED_RULES_FILTERED 98, NO_RULE_QUALIFIED 36, INSUFFICIENT_DATA 1
pulse families: quietDay 6, persistence 4, rebalance 4, spread 3, freshStart 1
pulse stages:   persistence.s4 3, quietDay.s3 3, rebalance.s1 3, spread.s1 3, quietDay.s1 2, freshStart.s1 1, persistence.s2 1, quietDay.s2 1, rebalance.s2 1
momentum: 153 invocations, 152 spoken
  families: quietStretch 48, cleanSlate 41, comeback 29, steadyStretch 18, firstDays 13, singleAreaWeek 3
banner: 153 invocations, 83 spoken
  families: weekStarting 44, weekMixed 31, weekBuilding 8
report headline: 25 invocations, 14 spoken
  families: comeback 10, datedFallback 1, decliningActivity 1, firstWeek 1, personalBest 1
report observation: 47 invocations, 46 spoken
  families: neglectedArea 13, areaRevival 10, timeOfDay 8, queuePressure 5, quietWeek 4, areaBalance 1, dayShape 1, firstMilestone 1, hardStretch 1, personalBest 1, selfReportVsData 1
report pattern: 22 invocations, 18 spoken
  families: areaGoneQuiet 9, queueEquilibrium 5, decliningActivity 2, reportedVsActual 2
layer 5 vetoes: 0
distinct variants used: 207
```

## 3. Balanced across four, the silent year

Four areas, none dominant, moving steadily all year. This is a person using the app
exactly the way it was designed to be used, and **the Pulse speaks to them nine times
in three hundred and sixty five days.** Everything phase 9 found about silence is
visible in this one life, and so is the finding depth cannot reach: the Momentum
headline says `balancedWeek` on 351 of 365 openings and the report is headlined
`personalBest` in 50 weeks out of 52. Fifty different sentences, one claim.

```text
persona: balancedAcrossFour, Balanced across four
why:     Four areas, none dominant. Feeds balanced, spread and areaBalance.
span:    365 simulated days, 365 opens, 1901 events, 1404 engine invocations
```

### January 2026

28 days on screen, 9 Pulses spoken, 19 Pulse days silent, 71 sentences in all.

```text
2026-01-04  [momentum]  SILENT (INSUFFICIENT_DATA)
2026-01-04  [banner]  weekStarting / stage 1 / reflective / bn.start.11
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > Barely begun.
2026-01-04  [pulse]  SILENT (INSUFFICIENT_DATA)

2026-01-05  [momentum]  firstDays / stage 1 / reflective / mo.first.22
  rule:  momentum.firstDays
  fired: fewer than fourteen days since install, at least one event, so this is a beginning rather than an empty app
  > There are not many days here to look at.
2026-01-05  [banner]  weekStarting / stage 1 / reflective / bn.start.15
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > Nothing settled yet.
2026-01-05  [pulse]  spread / stage 2 / plain / spread.s2.10
  rule:  pulse.spread.s2
  fired: four or more areas had events, no area is above half the window, the window holds at least 5 events, so a share is describing something real
  facts: areaCount=4 [rollup.areasWithEvents], n=9 [window.totalEvents]
  > nine moves across four areas.
  > Covering ground, or spinning?
  ? Covering ground | Spinning

2026-01-06  [momentum]  firstDays / stage 1 / reflective / mo.first.30
  rule:  momentum.firstDays
  fired: fewer than fourteen days since install, at least one event, so this is a beginning rather than an empty app
  > Early, with the rest of it ahead.
2026-01-06  [banner]  weekBuilding / stage 1 / reflective / bn.build.32
  rule:  banner.weekBuilding
  fired: three to five days into the week, completions are accumulating
  > Something behind it, more of it ahead.
2026-01-06  [pulse]  freshStart / stage 1 / plain / freshstart.s1.02
  rule:  pulse.freshStart.s1
  fired: the area is one this window began or reopened, the area has at least 1 events in the window, so naming it cannot be a phantom claim
  facts: areaName=Health
  > You added Health.
  > Is this a big one?
  ? A big one | A small one

2026-01-07  [momentum]  firstDays / stage 1 / reflective / mo.first.16
  rule:  momentum.firstDays
  fired: fewer than fourteen days since install, at least one event, so this is a beginning rather than an empty app
  > A start, nothing more.
2026-01-07  [banner]  weekBuilding / stage 1 / reflective / bn.build.31
  rule:  banner.weekBuilding
  fired: three to five days into the week, completions are accumulating
  > Part of a week, with something in it.
2026-01-07  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-01-08  [momentum]  firstDays / stage 1 / reflective / mo.first.24
  rule:  momentum.firstDays
  fired: fewer than fourteen days since install, at least one event, so this is a beginning rather than an empty app
  > Only a few days of this so far.
2026-01-08  [banner]  weekBuilding / stage 1 / reflective / bn.build.23
  rule:  banner.weekBuilding
  fired: three to five days into the week, completions are accumulating
  > Something already done, the week still open.
2026-01-08  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-01-09  [momentum]  firstDays / stage 1 / reflective / mo.first.19
  rule:  momentum.firstDays
  fired: fewer than fourteen days since install, at least one event, so this is a beginning rather than an empty app
  > A short history so far.
2026-01-09  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-01-09  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-01-10  [momentum]  firstDays / stage 1 / reflective / mo.first.21
  rule:  momentum.firstDays
  fired: fewer than fourteen days since install, at least one event, so this is a beginning rather than an empty app
  > Early, before much of it has happened.
2026-01-10  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-01-10  [pulse]  persistence / stage 1 / plain / persistence.s1.24
  rule:  pulse.persistence.s1
  fired: the active item is three to five days old, the area holding the item has at least 1 events in the window
  facts: areaName=Health
  > One item has held Health.
  > Would you put it there again today?
  ? Deep work | Stuck

2026-01-11  [momentum]  firstDays / stage 1 / reflective / mo.first.07
  rule:  momentum.firstDays
  fired: fewer than fourteen days since install, at least one event, so this is a beginning rather than an empty app
  > The beginning of a picture.
2026-01-11  [banner]  weekStarting / stage 1 / reflective / bn.start.17
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > The first days of it.
2026-01-11  [pulse]  freshStart / stage 1 / plain / freshstart.s1.03
  rule:  pulse.freshStart.s1
  fired: the area is one this window began or reopened, the area has at least 1 events in the window, so naming it cannot be a phantom claim
  facts: areaName=Personal, itemTitle=Tidy the reading list
  > Tidy the reading list is the first thing in Personal.
  > A commitment, or a trial?
  ? Long overdue | Brand new
2026-01-11  [report headline]  balanced / stage 1 / plain / hd.bal.25
  rule:  report.headline.balanced
  fired: three or more areas had activity, no area dominates, the window holds at least 5 events, so a share is describing something real
  > Many, not one.
2026-01-11  [report observation]  areaBalance / stage 1 / editorial / ob.bal.l08
  rule:  report.observation.areaBalance
  fired: three or more areas had activity, no area holds half the week, the window holds at least 6 events, so a share is describing something real
  > A week can be wide or it can be deep.
2026-01-11  [report observation]  firstMilestone / stage 1 / observational / ob.first.l05
  rule:  report.observation.firstMilestone
  fired: something happened this window for the first time ever, the window has activity behind the milestone
  > Every area had something active at once, for the first time.
2026-01-11  [report observation]  completionSplit / stage 1 / observational / ob.split.l31
  rule:  report.observation.completionSplit
  fired: three or more pulses were answered in the window, there are completions for the answers to be about
  facts: n=3 [pulse.answeredInWindow], priorCount=1 [pulse.labelCountInWindow:A big one], priorLabel=A big one
  > 3 answers this week, 1 of them A big one, and the week had completions in it.
2026-01-11  [report observation]  intakeVsOutput / stage 2 / neutral_agent / ob.flow.s2.l16
  rule:  report.observation.intakeVsOutput.s2
  fired: intake exceeds output by five or more, there are additions to count
  facts: k=4 [rollup.queueGrowth]
  > 4 net, into the queues.

2026-01-12  [momentum]  firstDays / stage 1 / reflective / mo.first.25
  rule:  momentum.firstDays
  fired: fewer than fourteen days since install, at least one event, so this is a beginning rather than an empty app
  > The opening days.
2026-01-12  [banner]  weekMixed / stage 1 / reflective / bn.mixed.20
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Concentrated, with the rest thinner.
2026-01-12  [pulse]  persistence / stage 1 / plain / persistence.s1.18
  rule:  pulse.persistence.s1
  fired: the active item is three to five days old, the area holding the item has at least 1 events in the window
  facts: ageDays=5 [item.itemAgeDays:itm-6]
  > five days at the front.
  > Part done, or not begun?
  ? On purpose | Just there

2026-01-13  [momentum]  firstDays / stage 1 / reflective / mo.first.20
  rule:  momentum.firstDays
  fired: fewer than fourteen days since install, at least one event, so this is a beginning rather than an empty app
  > The part of it that has happened so far.
2026-01-13  [banner]  weekBuilding / stage 1 / reflective / bn.build.10
  rule:  banner.weekBuilding
  fired: three to five days into the week, completions are accumulating
  > Midweek, with something in it.
2026-01-13  [pulse]  freshStart / stage 1 / plain / freshstart.s1.01
  rule:  pulse.freshStart.s1
  fired: the area is one this window began or reopened, the area has at least 1 events in the window, so naming it cannot be a phantom claim
  facts: areaName=Home
  > Home has its first item.
  > A commitment, or a trial?
  ? Expanding | Exploring

2026-01-14  [momentum]  firstDays / stage 1 / reflective / mo.first.23
  rule:  momentum.firstDays
  fired: fewer than fourteen days since install, at least one event, so this is a beginning rather than an empty app
  > A beginning with a few days behind it.
2026-01-14  [banner]  weekBuilding / stage 1 / reflective / bn.build.12
  rule:  banner.weekBuilding
  fired: three to five days into the week, completions are accumulating
  > Partway through.
2026-01-14  [pulse]  persistence / stage 1 / plain / persistence.s1.25
  rule:  pulse.persistence.s1
  fired: the active item is three to five days old, the area holding the item has at least 1 events in the window
  facts: itemTitle=Finish the tax folder
  > Finish the tax folder was here yesterday too.
  > Chosen, or defaulted to?
  ? Moving | Parked

2026-01-15  [momentum]  firstDays / stage 1 / reflective / mo.first.04
  rule:  momentum.firstDays
  fired: fewer than fourteen days since install, at least one event, so this is a beginning rather than an empty app
  > Just getting going.
2026-01-15  [banner]  weekBuilding / stage 1 / reflective / bn.build.16
  rule:  banner.weekBuilding
  fired: three to five days into the week, completions are accumulating
  > A week no longer new.
2026-01-15  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-01-16  [momentum]  firstDays / stage 1 / reflective / mo.first.22
  rule:  momentum.firstDays
  fired: fewer than fourteen days since install, at least one event, so this is a beginning rather than an empty app
  > There are not many days here to look at.
2026-01-16  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-01-16  [pulse]  freshStart / stage 1 / plain / freshstart.s1.09
  rule:  pulse.freshStart.s1
  fired: the area is one this window began or reopened, the area has at least 1 events in the window, so naming it cannot be a phantom claim
  facts: areaName=Work
  > Work went from empty to active.
  > Expanding, or exploring?
  ? Long overdue | Brand new

2026-01-17  [momentum]  firstDays / stage 1 / reflective / mo.first.20
  rule:  momentum.firstDays
  fired: fewer than fourteen days since install, at least one event, so this is a beginning rather than an empty app
  > The part of it that has happened so far.
2026-01-17  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-01-17  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-01-18  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.26
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Several places, none of them all of it.
2026-01-18  [banner]  weekStarting / stage 1 / reflective / bn.start.21
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > Early, before anything settles.
2026-01-18  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-01-18  [report headline]  balanced / stage 1 / plain / hd.bal.02
  rule:  report.headline.balanced
  fired: three or more areas had activity, no area dominates, the window holds at least 5 events, so a share is describing something real
  > A wide week.
2026-01-18  [report observation]  areaBalance / stage 1 / editorial / ob.bal.l17
  rule:  report.observation.areaBalance
  fired: three or more areas had activity, no area holds half the week, the window holds at least 6 events, so a share is describing something real
  facts: areaCount=4 [rollup.areasWithEvents]
  > A week that touches 4 areas and gives none of them half has no single subject.
2026-01-18  [report observation]  completionSplit / stage 1 / observational / ob.split.l12
  rule:  report.observation.completionSplit
  fired: three or more pulses were answered in the window, there are completions for the answers to be about
  facts: m=2 [pulse.positiveInWindow]
  > 2 of your answers were positive.
2026-01-18  [report observation]  intakeVsOutput / stage 2 / neutral_agent / ob.flow.s2.l25
  rule:  report.observation.intakeVsOutput.s2
  fired: intake exceeds output by five or more, there are additions to count
  facts: k=6 [rollup.queueGrowth]
  > 6 more waiting than at the start.
2026-01-18  [report observation]  queuePressure / stage 1 / neutral_agent / ob.qp.l07
  rule:  report.observation.queuePressure
  fired: at least one area's queue grew, the queues hold three or more things to count
  facts: n=10 [rollup.queueTotal]
  > 10 things are in the queues.

2026-01-19  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.20
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > No single thing has held it.
2026-01-19  [banner]  weekMixed / stage 1 / reflective / bn.mixed.09
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week with a tilt to it.
2026-01-19  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-01-20  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.18
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Level, more or less.
2026-01-20  [banner]  weekMixed / stage 1 / reflective / bn.mixed.09
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week with a tilt to it.
2026-01-20  [pulse]  persistence / stage 1 / plain / persistence.s1.20
  rule:  pulse.persistence.s1
  fired: the active item is three to five days old, the area holding the item has at least 1 events in the window
  facts: itemTitle=Cancel the old subscription
  > Cancel the old subscription sits where it sat.
  > Would you put it there again today?
  ? Deep work | Stuck

2026-01-21  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.22
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Two weeks that went several ways.
2026-01-21  [banner]  weekBuilding / stage 1 / reflective / bn.build.15
  rule:  banner.weekBuilding
  fired: three to five days into the week, completions are accumulating
  > A week past its start.
2026-01-21  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-01-22  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.16
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > No one place took it all.
2026-01-22  [banner]  weekBuilding / stage 1 / reflective / bn.build.33
  rule:  banner.weekBuilding
  fired: three to five days into the week, completions are accumulating
  > A week with days still in front of it.
2026-01-22  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-01-23  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.16
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > No one place took it all.
2026-01-23  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-01-23  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-01-24  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.17
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Room for more than one thing.
2026-01-24  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-01-24  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-01-25  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.26
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Several places, none of them all of it.
2026-01-25  [banner]  weekStarting / stage 1 / reflective / bn.start.24
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > The shape of this week is still ahead.
2026-01-25  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-01-25  [report headline]  personalBest / stage 1 / plain / hd.best.08
  rule:  report.headline.personalBest
  fired: no earlier week strictly beats this one, this week reaches the personal best, there is more than one week to be best of, there are completions, so no count renders as zero
  > Your best week here.
2026-01-25  [report observation]  personalBest / stage 1 / editorial / ob.best.l04
  rule:  report.observation.personalBest
  fired: no earlier week strictly beats this one, this week reaches the personal best, there is more than one week to be best of, there are completions, so no count renders as zero
  > Nothing in eleven weeks of history looks like this one.
2026-01-25  [report observation]  areaBalance / stage 1 / editorial / ob.bal.l11
  rule:  report.observation.areaBalance
  fired: three or more areas had activity, no area holds half the week, the window holds at least 6 events, so a share is describing something real
  > A wide week and a thin week look the same from a count, and nothing here tells them apart.
2026-01-25  [report observation]  intakeVsOutput / stage 2 / neutral_agent / ob.flow.s2.l26
  rule:  report.observation.intakeVsOutput.s2
  fired: intake exceeds output by five or more, there are additions to count
  > The queues took on more than they let go.
2026-01-25  [report observation]  persistentItem / stage 1 / observational / ob.pers.l43
  rule:  report.observation.persistentItem.low
  fired: the item has been active seven to thirteen days, the area holding the item has at least 1 events in the window
  facts: ageDays=8 [item.itemAgeDays:itm-26], areaName=Home, itemTitle=Label the storage boxes
  > Label the storage boxes has led Home for 8 days, which is longer than the week this report covers.

2026-01-26  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.23
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Nothing has held more than half.
2026-01-26  [banner]  weekMixed / stage 1 / reflective / bn.mixed.12
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week that leans one way.
2026-01-26  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-01-27  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.24
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight that kept moving around.
2026-01-27  [banner]  weekMixed / stage 1 / reflective / bn.mixed.21
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week with more of it in one area.
2026-01-27  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-01-28  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.17
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Room for more than one thing.
2026-01-28  [banner]  weekMixed / stage 1 / reflective / bn.mixed.16
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week with a heavier side.
2026-01-28  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-01-29  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.29
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Several things at once, for two weeks.
2026-01-29  [banner]  weekBuilding / stage 1 / reflective / bn.build.19
  rule:  banner.weekBuilding
  fired: three to five days into the week, completions are accumulating
  > A week with a few days behind it.
2026-01-29  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-01-30  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.25
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Two weeks across several areas.
2026-01-30  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-01-30  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-01-31  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.27
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight that went in several directions.
2026-01-31  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-01-31  [pulse]  SILENT (NO_RULE_QUALIFIED)
```

### February 2026

28 days on screen, 0 Pulses spoken, 28 Pulse days silent, 72 sentences in all.

```text
2026-02-01  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.21
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Across, rather than down.
2026-02-01  [banner]  weekStarting / stage 1 / reflective / bn.start.17
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > The first days of it.
2026-02-01  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-02-01  [report headline]  personalBest / stage 1 / plain / hd.best.33
  rule:  report.headline.personalBest
  fired: no earlier week strictly beats this one, this week reaches the personal best, there is more than one week to be best of, there are completions, so no count renders as zero
  facts: n=7 [window.completions]
  > 7 finished. A ceiling.
2026-02-01  [report observation]  personalBest / stage 1 / editorial / ob.best.l18
  rule:  report.observation.personalBest
  fired: no earlier week strictly beats this one, this week reaches the personal best, there is more than one week to be best of, there are completions, so no count renders as zero
  facts: n=7 [window.completions]
  > 7 completions is the largest number this record has held, and a number is all it is.
2026-02-01  [report observation]  persistentItem / stage 1 / neutral_agent / ob.pers.l20
  rule:  report.observation.persistentItem.high
  fired: the item has been active fourteen days or more, the area holding the item has at least 1 events in the window, there is a median completion time to compare against
  facts: ageDays=15 [item.itemAgeDays:itm-26]
  > 2 weeks with the same thing in front.
2026-02-01  [report observation]  areaBalance / stage 1 / editorial / ob.bal.l07
  rule:  report.observation.areaBalance
  fired: three or more areas had activity, no area holds half the week, the window holds at least 6 events, so a share is describing something real
  > The week had no owner.
2026-02-01  [report observation]  intakeVsOutput / stage 2 / neutral_agent / ob.flow.s2.l18
  rule:  report.observation.intakeVsOutput.s2
  fired: intake exceeds output by five or more, there are additions to count
  facts: k=7 [rollup.queueGrowth], m=7 [window.completions], n=14 [window.additions]
  > 14 in, 7 out, and 7 added to the queues.
2026-02-01  [report pattern]  reportedVsActual / stage 1 / plain / pt.rva.51
  rule:  report.pattern.reportedVsActual
  fired: there are several stored answers to compare against, one family has been answered about more than twice, there are at least three weeks of snapshots, without which no pattern may fire
  > Three answers about one kind of moment is not a verdict on anything, and it is a record that the moment kept happening.

2026-02-02  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.28
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight that did not settle in one place.
2026-02-02  [banner]  weekMixed / stage 1 / reflective / bn.mixed.20
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Concentrated, with the rest thinner.
2026-02-02  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-02-03  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.26
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Several places, none of them all of it.
2026-02-03  [banner]  weekBuilding / stage 1 / reflective / bn.build.22
  rule:  banner.weekBuilding
  fired: three to five days into the week, completions are accumulating
  > A week that has started to add up.
2026-02-03  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-02-04  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.23
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Nothing has held more than half.
2026-02-04  [banner]  weekMixed / stage 1 / reflective / bn.mixed.15
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Tilted, so far.
2026-02-04  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-02-05  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.25
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Two weeks across several areas.
2026-02-05  [banner]  weekBuilding / stage 1 / reflective / bn.build.14
  rule:  banner.weekBuilding
  fired: three to five days into the week, completions are accumulating
  > Days behind it, days ahead.
2026-02-05  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-02-06  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.30
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Spread, without any one place holding it.
2026-02-06  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-02-06  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-02-07  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.17
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Room for more than one thing.
2026-02-07  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-02-07  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-02-08  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.21
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Across, rather than down.
2026-02-08  [banner]  weekStarting / stage 1 / reflective / bn.start.26
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > A week still near its own beginning.
2026-02-08  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-02-08  [report headline]  personalBest / stage 1 / plain / hd.best.11
  rule:  report.headline.personalBest
  fired: no earlier week strictly beats this one, this week reaches the personal best, there is more than one week to be best of, there are completions, so no count renders as zero
  > Nothing earlier beat it.
2026-02-08  [report observation]  personalBest / stage 1 / editorial / ob.best.l10
  rule:  report.observation.personalBest
  fired: no earlier week strictly beats this one, this week reaches the personal best, there is more than one week to be best of, there are completions, so no count renders as zero
  > Nothing here needs to happen again.
2026-02-08  [report observation]  areaBalance / stage 1 / editorial / ob.bal.l14
  rule:  report.observation.areaBalance
  fired: three or more areas had activity, no area holds half the week, the window holds at least 6 events, so a share is describing something real
  facts: pct=36 [area.areaShare:health]
  > 36 percent is the largest share anything took. It is not a majority.
2026-02-08  [report observation]  intakeVsOutput / stage 2 / neutral_agent / ob.flow.s2.l24
  rule:  report.observation.intakeVsOutput.s2
  fired: intake exceeds output by five or more, there are additions to count
  > Intake ran ahead of output.
2026-02-08  [report observation]  persistentItem / stage 1 / observational / ob.pers.l36
  rule:  report.observation.persistentItem.low
  fired: the item has been active seven to thirteen days, the area holding the item has at least 1 events in the window
  facts: areaName=Personal, itemTitle=Order new running shoes
  > Nothing in the Personal queue has come past Order new running shoes.
2026-02-08  [report pattern]  growingQueues / stage 1 / plain / pt.grow.36
  rule:  report.pattern.growingQueues
  fired: the queues have grown three weeks running, there are at least three weeks of snapshots, without which no pattern may fire
  facts: m=24 [history.weekQueueSizeAgo:1], n=31 [history.weekQueueSizeAgo:0]
  > 31 now, up from 24.

2026-02-09  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.12
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Nothing has dominated.
2026-02-09  [banner]  weekMixed / stage 1 / reflective / bn.mixed.17
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Spread unevenly, so far.
2026-02-09  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-02-10  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.32
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight with its weight spread out.
2026-02-10  [banner]  weekMixed / stage 1 / reflective / bn.mixed.25
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Uneven going.
2026-02-10  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-02-11  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.26
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Several places, none of them all of it.
2026-02-11  [banner]  weekMixed / stage 1 / reflective / bn.mixed.14
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A shape rather than a spread.
2026-02-11  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-02-12  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.07
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Evenly spread, across two weeks.
2026-02-12  [banner]  weekBuilding / stage 1 / reflective / bn.build.22
  rule:  banner.weekBuilding
  fired: three to five days into the week, completions are accumulating
  > A week that has started to add up.
2026-02-12  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-02-13  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.13
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Wide, across two weeks.
2026-02-13  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-02-13  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-02-14  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.31
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > More than one thing at a time.
2026-02-14  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-02-14  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-02-15  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.33
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight that went around rather than down.
2026-02-15  [banner]  weekStarting / stage 1 / reflective / bn.start.12
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > Early, with most of it ahead.
2026-02-15  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-02-15  [report headline]  personalBest / stage 1 / plain / hd.best.23
  rule:  report.headline.personalBest
  fired: no earlier week strictly beats this one, this week reaches the personal best, there is more than one week to be best of, there are completions, so no count renders as zero
  > This one is the highest.
2026-02-15  [report observation]  personalBest / stage 1 / editorial / ob.best.l12
  rule:  report.observation.personalBest
  fired: no earlier week strictly beats this one, this week reaches the personal best, there is more than one week to be best of, there are completions, so no count renders as zero
  > The record moved. That is all the record does.
2026-02-15  [report observation]  persistentItem / stage 1 / neutral_agent / ob.pers.l12
  rule:  report.observation.persistentItem.high
  fired: the item has been active fourteen days or more, the area holding the item has at least 1 events in the window, there is a median completion time to compare against
  facts: ageDays=14 [item.itemAgeDays:itm-46], areaName=Personal, itemTitle=Order new running shoes
  > Nothing else has reached the front of Personal in 2 weeks, and Order new running shoes is still there.
2026-02-15  [report observation]  areaBalance / stage 1 / editorial / ob.bal.l12
  rule:  report.observation.areaBalance
  fired: three or more areas had activity, no area holds half the week, the window holds at least 6 events, so a share is describing something real
  > The week had a shape without a subject.
2026-02-15  [report observation]  intakeVsOutput / stage 2 / neutral_agent / ob.flow.s2.l31
  rule:  report.observation.intakeVsOutput.s2
  fired: intake exceeds output by five or more, there are additions to count
  facts: k=7 [rollup.queueGrowth], n=14 [window.additions]
  > 14 things came in over seven days and the lists are 7 longer for it.
2026-02-15  [report pattern]  consistentRhythm / stage 1 / plain / pt.rhy.22
  rule:  report.pattern.consistentRhythm
  fired: four weeks inside a narrow band, there are four weeks to see it across, the band is a rhythm rather than a run of empty weeks
  > The totals have sat close together for a month.

2026-02-16  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.28
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight that did not settle in one place.
2026-02-16  [banner]  weekMixed / stage 1 / reflective / bn.mixed.16
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week with a heavier side.
2026-02-16  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-02-17  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.07
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Evenly spread, across two weeks.
2026-02-17  [banner]  weekBuilding / stage 1 / reflective / bn.build.18
  rule:  banner.weekBuilding
  fired: three to five days into the week, completions are accumulating
  > Some of the week has happened.
2026-02-17  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-02-18  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.04
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A wide fortnight.
2026-02-18  [banner]  weekBuilding / stage 1 / reflective / bn.build.24
  rule:  banner.weekBuilding
  fired: three to five days into the week, completions are accumulating
  > The beginning has passed.
2026-02-18  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-02-19  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.19
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Spread out, over fourteen days.
2026-02-19  [banner]  weekBuilding / stage 1 / reflective / bn.build.16
  rule:  banner.weekBuilding
  fired: three to five days into the week, completions are accumulating
  > A week no longer new.
2026-02-19  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-02-20  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.23
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Nothing has held more than half.
2026-02-20  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-02-20  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-02-21  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.23
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Nothing has held more than half.
2026-02-21  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-02-21  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-02-22  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.18
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Level, more or less.
2026-02-22  [banner]  weekStarting / stage 1 / reflective / bn.start.10
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > The start of a week.
2026-02-22  [pulse]  SILENT (NO_RULE_QUALIFIED)
2026-02-22  [report headline]  personalBest / stage 1 / plain / hd.best.10
  rule:  report.headline.personalBest
  fired: no earlier week strictly beats this one, this week reaches the personal best, there is more than one week to be best of, there are completions, so no count renders as zero
  > A week that stands out.
2026-02-22  [report observation]  personalBest / stage 1 / editorial / ob.best.l16
  rule:  report.observation.personalBest
  fired: no earlier week strictly beats this one, this week reaches the personal best, there is more than one week to be best of, there are completions, so no count renders as zero
  facts: m=7 [history.personalBestCompletions]
  > The previous best was 7. This week is not less than that.
2026-02-22  [report observation]  persistentItem / stage 1 / neutral_agent / ob.pers.l11
  rule:  report.observation.persistentItem.high
  fired: the item has been active fourteen days or more, the area holding the item has at least 1 events in the window, there is a median completion time to compare against
  facts: ageDays=21 [item.itemAgeDays:itm-46], areaName=Personal, itemTitle=Order new running shoes
  > The front of Personal has been Order new running shoes for 3 weeks and nothing in the queue has come past it.
2026-02-22  [report observation]  areaBalance / stage 1 / editorial / ob.bal.l10
  rule:  report.observation.areaBalance
  fired: three or more areas had activity, no area holds half the week, the window holds at least 6 events, so a share is describing something real
  > Nothing here was the main thing.
2026-02-22  [report observation]  intakeVsOutput / stage 2 / neutral_agent / ob.flow.s2.l14
  rule:  report.observation.intakeVsOutput.s2
  fired: intake exceeds output by five or more, there are additions to count
  > Nothing added this week has left yet.
2026-02-22  [report pattern]  reportedVsActual / stage 1 / plain / pt.rva.53
  rule:  report.pattern.reportedVsActual
  fired: there are several stored answers to compare against, one family has been answered about more than twice, there are at least three weeks of snapshots, without which no pattern may fire
  > One subject has produced three answers, and the record holds at least five answers in total.

2026-02-23  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.13
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Wide, across two weeks.
2026-02-23  [banner]  weekMixed / stage 1 / reflective / bn.mixed.17
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Spread unevenly, so far.
2026-02-23  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-02-24  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.32
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight with its weight spread out.
2026-02-24  [banner]  weekBuilding / stage 1 / reflective / bn.build.31
  rule:  banner.weekBuilding
  fired: three to five days into the week, completions are accumulating
  > Part of a week, with something in it.
2026-02-24  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-02-25  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.12
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Nothing has dominated.
2026-02-25  [banner]  weekMixed / stage 1 / reflective / bn.mixed.19
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week gathered mostly in one place.
2026-02-25  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-02-26  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.31
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > More than one thing at a time.
2026-02-26  [banner]  weekBuilding / stage 1 / reflective / bn.build.16
  rule:  banner.weekBuilding
  fired: three to five days into the week, completions are accumulating
  > A week no longer new.
2026-02-26  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-02-27  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.12
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Nothing has dominated.
2026-02-27  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-02-27  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-02-28  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.27
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight that went in several directions.
2026-02-28  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-02-28  [pulse]  SILENT (NO_RULE_QUALIFIED)
```

### March 2026

31 days on screen, 0 Pulses spoken, 31 Pulse days silent, 84 sentences in all.

```text
2026-03-01  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.17
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Room for more than one thing.
2026-03-01  [banner]  weekStarting / stage 1 / reflective / bn.start.23
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > Nothing here has had time to become a shape.
2026-03-01  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-03-01  [report headline]  personalBest / stage 1 / plain / hd.best.13
  rule:  report.headline.personalBest
  fired: no earlier week strictly beats this one, this week reaches the personal best, there is more than one week to be best of, there are completions, so no count renders as zero
  > No week has topped this.
2026-03-01  [report observation]  personalBest / stage 1 / editorial / ob.best.l11
  rule:  report.observation.personalBest
  fired: no earlier week strictly beats this one, this week reaches the personal best, there is more than one week to be best of, there are completions, so no count renders as zero
  facts: n=7 [window.completions]
  > The number is 7. The reading of it is not the app's to make.
2026-03-01  [report observation]  areaBalance / stage 1 / editorial / ob.bal.l04
  rule:  report.observation.areaBalance
  fired: three or more areas had activity, no area holds half the week, the window holds at least 6 events, so a share is describing something real
  > No single area owned this week.
2026-03-01  [report observation]  intakeVsOutput / stage 2 / neutral_agent / ob.flow.s2.l17
  rule:  report.observation.intakeVsOutput.s2
  fired: intake exceeds output by five or more, there are additions to count
  > More came in than went out, by five or more.
2026-03-01  [report observation]  queuePressure / stage 1 / neutral_agent / ob.qp.l11
  rule:  report.observation.queuePressure
  fired: at least one area's queue grew, the queues hold three or more things to count
  facts: n=52 [rollup.queueTotal]
  > 52 things are waiting.
2026-03-01  [report pattern]  consistentRhythm / stage 1 / plain / pt.rhy.44
  rule:  report.pattern.consistentRhythm
  fired: four weeks inside a narrow band, there are four weeks to see it across, the band is a rhythm rather than a run of empty weeks
  > Four weeks, all within reach of each other.

2026-03-02  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.27
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight that went in several directions.
2026-03-02  [banner]  weekMixed / stage 1 / reflective / bn.mixed.25
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Uneven going.
2026-03-02  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-03-03  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.16
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > No one place took it all.
2026-03-03  [banner]  weekBuilding / stage 1 / reflective / bn.build.32
  rule:  banner.weekBuilding
  fired: three to five days into the week, completions are accumulating
  > Something behind it, more of it ahead.
2026-03-03  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-03-04  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.27
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight that went in several directions.
2026-03-04  [banner]  weekBuilding / stage 1 / reflective / bn.build.18
  rule:  banner.weekBuilding
  fired: three to five days into the week, completions are accumulating
  > Some of the week has happened.
2026-03-04  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-03-05  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.26
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Several places, none of them all of it.
2026-03-05  [banner]  weekBuilding / stage 1 / reflective / bn.build.17
  rule:  banner.weekBuilding
  fired: three to five days into the week, completions are accumulating
  > A week that has begun to fill.
2026-03-05  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-03-06  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.26
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Several places, none of them all of it.
2026-03-06  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-03-06  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-03-07  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.23
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Nothing has held more than half.
2026-03-07  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-03-07  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-03-08  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.27
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight that went in several directions.
2026-03-08  [banner]  weekStarting / stage 1 / reflective / bn.start.24
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > The shape of this week is still ahead.
2026-03-08  [pulse]  SILENT (NO_RULE_QUALIFIED)
2026-03-08  [report headline]  personalBest / stage 1 / plain / hd.best.49
  rule:  report.headline.personalBest
  fired: no earlier week strictly beats this one, this week reaches the personal best, there is more than one week to be best of, there are completions, so no count renders as zero
  facts: n=7 [window.completions]
  > 7 completions at the top.
2026-03-08  [report observation]  personalBest / stage 1 / editorial / ob.best.l14
  rule:  report.observation.personalBest
  fired: no earlier week strictly beats this one, this week reaches the personal best, there is more than one week to be best of, there are completions, so no count renders as zero
  facts: n=7 [window.completions]
  > 7 is where the record stands now.
2026-03-08  [report observation]  areaBalance / stage 1 / editorial / ob.bal.l07
  rule:  report.observation.areaBalance
  fired: three or more areas had activity, no area holds half the week, the window holds at least 6 events, so a share is describing something real
  > The week had no owner.
2026-03-08  [report observation]  intakeVsOutput / stage 2 / neutral_agent / ob.flow.s2.l12
  rule:  report.observation.intakeVsOutput.s2
  fired: intake exceeds output by five or more, there are additions to count
  facts: k=7 [rollup.queueGrowth]
  > 7 more things are waiting than were on Sunday.
2026-03-08  [report observation]  persistentItem / stage 1 / observational / ob.pers.l28
  rule:  report.observation.persistentItem.low
  fired: the item has been active seven to thirteen days, the area holding the item has at least 1 events in the window
  facts: ageDays=13 [item.itemAgeDays:itm-66], itemTitle=Finish the tax folder
  > Finish the tax folder has stayed in place 13 days.
2026-03-08  [report pattern]  reportedVsActual / stage 1 / plain / pt.rva.60
  rule:  report.pattern.reportedVsActual
  fired: there are several stored answers to compare against, one family has been answered about more than twice, there are at least three weeks of snapshots, without which no pattern may fire
  > The questions this app asks come from what the days looked like, and one kind of day has come around three times.

2026-03-09  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.22
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Two weeks that went several ways.
2026-03-09  [banner]  weekMixed / stage 1 / reflective / bn.mixed.25
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Uneven going.
2026-03-09  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-03-10  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.22
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Two weeks that went several ways.
2026-03-10  [banner]  weekMixed / stage 1 / reflective / bn.mixed.11
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Weighted rather than spread.
2026-03-10  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-03-11  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.26
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Several places, none of them all of it.
2026-03-11  [banner]  weekBuilding / stage 1 / reflective / bn.build.09
  rule:  banner.weekBuilding
  fired: three to five days into the week, completions are accumulating
  > A week under way.
2026-03-11  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-03-12  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.31
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > More than one thing at a time.
2026-03-12  [banner]  weekBuilding / stage 1 / reflective / bn.build.27
  rule:  banner.weekBuilding
  fired: three to five days into the week, completions are accumulating
  > A week with a few days already spent.
2026-03-12  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-03-13  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.24
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight that kept moving around.
2026-03-13  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-03-13  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-03-14  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.15
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Breadth, more than depth.
2026-03-14  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-03-14  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-03-15  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.23
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Nothing has held more than half.
2026-03-15  [banner]  weekStarting / stage 1 / reflective / bn.start.24
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > The shape of this week is still ahead.
2026-03-15  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-03-15  [report headline]  personalBest / stage 1 / plain / hd.best.04
  rule:  report.headline.personalBest
  fired: no earlier week strictly beats this one, this week reaches the personal best, there is more than one week to be best of, there are completions, so no count renders as zero
  > The most you have finished.
2026-03-15  [report observation]  personalBest / stage 1 / editorial / ob.best.l09
  rule:  report.observation.personalBest
  fired: no earlier week strictly beats this one, this week reaches the personal best, there is more than one week to be best of, there are completions, so no count renders as zero
  > A record is a fact about the past. Nothing here turns it into a target for the week ahead.
2026-03-15  [report observation]  persistentItem / stage 1 / neutral_agent / ob.pers.l23
  rule:  report.observation.persistentItem.high
  fired: the item has been active fourteen days or more, the area holding the item has at least 1 events in the window, there is a median completion time to compare against
  facts: areaName=Home, itemTitle=Finish the tax folder, n=19 [item.itemQueueBehind:itm-66]
  > Finish the tax folder has 19 things waiting behind it while Home moved.
2026-03-15  [report observation]  areaBalance / stage 1 / editorial / ob.bal.l17
  rule:  report.observation.areaBalance
  fired: three or more areas had activity, no area holds half the week, the window holds at least 6 events, so a share is describing something real
  facts: areaCount=4 [rollup.areasWithEvents]
  > A week that touches 4 areas and gives none of them half has no single subject.
2026-03-15  [report observation]  intakeVsOutput / stage 2 / neutral_agent / ob.flow.s2.l10
  rule:  report.observation.intakeVsOutput.s2
  fired: intake exceeds output by five or more, there are additions to count
  facts: m=7 [window.completions], n=14 [window.additions]
  > 14 things arrived. 7 left.
2026-03-15  [report pattern]  consistentRhythm / stage 1 / plain / pt.rhy.42
  rule:  report.pattern.consistentRhythm
  fired: four weeks inside a narrow band, there are four weeks to see it across, the band is a rhythm rather than a run of empty weeks
  > The four weeks are the same week, near enough.

2026-03-16  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.19
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Spread out, over fourteen days.
2026-03-16  [banner]  weekMixed / stage 1 / reflective / bn.mixed.21
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week with more of it in one area.
2026-03-16  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-03-17  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.31
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > More than one thing at a time.
2026-03-17  [banner]  weekBuilding / stage 1 / reflective / bn.build.27
  rule:  banner.weekBuilding
  fired: three to five days into the week, completions are accumulating
  > A week with a few days already spent.
2026-03-17  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-03-18  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.10
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Broad rather than deep.
2026-03-18  [banner]  weekBuilding / stage 1 / reflective / bn.build.21
  rule:  banner.weekBuilding
  fired: three to five days into the week, completions are accumulating
  > Between the start of a week and the end of it.
2026-03-18  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-03-19  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.33
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight that went around rather than down.
2026-03-19  [banner]  weekBuilding / stage 1 / reflective / bn.build.29
  rule:  banner.weekBuilding
  fired: three to five days into the week, completions are accumulating
  > Some days gone, some things done, the week still open.
2026-03-19  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-03-20  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.15
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Breadth, more than depth.
2026-03-20  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-03-20  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-03-21  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.33
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight that went around rather than down.
2026-03-21  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-03-21  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-03-22  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.15
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Breadth, more than depth.
2026-03-22  [banner]  weekStarting / stage 1 / reflective / bn.start.15
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > Nothing settled yet.
2026-03-22  [pulse]  SILENT (NO_RULE_QUALIFIED)
2026-03-22  [report headline]  personalBest / stage 1 / plain / hd.best.02
  rule:  report.headline.personalBest
  fired: no earlier week strictly beats this one, this week reaches the personal best, there is more than one week to be best of, there are completions, so no count renders as zero
  > A record.
2026-03-22  [report observation]  personalBest / stage 1 / editorial / ob.best.l19
  rule:  report.observation.personalBest
  fired: no earlier week strictly beats this one, this week reaches the personal best, there is more than one week to be best of, there are completions, so no count renders as zero
  > A week can be the best one and still be an ordinary week from the inside.
2026-03-22  [report observation]  areaBalance / stage 1 / editorial / ob.bal.l15
  rule:  report.observation.areaBalance
  fired: three or more areas had activity, no area holds half the week, the window holds at least 6 events, so a share is describing something real
  facts: areaCount=4 [rollup.areasWithEvents]
  > Attention went to 4 places and settled in none of them.
2026-03-22  [report observation]  intakeVsOutput / stage 2 / neutral_agent / ob.flow.s2.l33
  rule:  report.observation.intakeVsOutput.s2
  fired: intake exceeds output by five or more, there are additions to count
  facts: m=7 [window.completions], n=14 [window.additions]
  > The week took in 14 things and released 7, and the difference between those two runs to five or more.
2026-03-22  [report observation]  persistentItem / stage 1 / observational / ob.pers.l41
  rule:  report.observation.persistentItem.low
  fired: the item has been active seven to thirteen days, the area holding the item has at least 1 events in the window
  facts: areaName=Personal, itemTitle=Plan the trip route
  > The rest of Personal moved this week and Plan the trip route stayed still.
2026-03-22  [report pattern]  reportedVsActual / stage 1 / plain / pt.rva.20
  rule:  report.pattern.reportedVsActual
  fired: there are several stored answers to compare against, one family has been answered about more than twice, there are at least three weeks of snapshots, without which no pattern may fire
  > The app kept asking.

2026-03-23  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.22
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Two weeks that went several ways.
2026-03-23  [banner]  weekMixed / stage 1 / reflective / bn.mixed.22
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Some places busier than others.
2026-03-23  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-03-24  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.20
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > No single thing has held it.
2026-03-24  [banner]  weekMixed / stage 1 / reflective / bn.mixed.20
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Concentrated, with the rest thinner.
2026-03-24  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-03-25  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.14
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight in several places.
2026-03-25  [banner]  weekBuilding / stage 1 / reflective / bn.build.21
  rule:  banner.weekBuilding
  fired: three to five days into the week, completions are accumulating
  > Between the start of a week and the end of it.
2026-03-25  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-03-26  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.17
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Room for more than one thing.
2026-03-26  [banner]  weekBuilding / stage 1 / reflective / bn.build.22
  rule:  banner.weekBuilding
  fired: three to five days into the week, completions are accumulating
  > A week that has started to add up.
2026-03-26  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-03-27  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.27
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight that went in several directions.
2026-03-27  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-03-27  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-03-28  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.22
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Two weeks that went several ways.
2026-03-28  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-03-28  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-03-29  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.19
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Spread out, over fourteen days.
2026-03-29  [banner]  weekStarting / stage 1 / reflective / bn.start.24
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > The shape of this week is still ahead.
2026-03-29  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-03-29  [report headline]  personalBest / stage 1 / plain / hd.best.25
  rule:  report.headline.personalBest
  fired: no earlier week strictly beats this one, this week reaches the personal best, there is more than one week to be best of, there are completions, so no count renders as zero
  facts: n=7 [window.completions]
  > 7 completions, unmatched.
2026-03-29  [report observation]  personalBest / stage 1 / editorial / ob.best.l13
  rule:  report.observation.personalBest
  fired: no earlier week strictly beats this one, this week reaches the personal best, there is more than one week to be best of, there are completions, so no count renders as zero
  > A high week is a fact.
2026-03-29  [report observation]  persistentItem / stage 1 / neutral_agent / ob.pers.l26
  rule:  report.observation.persistentItem.high
  fired: the item has been active fourteen days or more, the area holding the item has at least 1 events in the window, there is a median completion time to compare against
  facts: ageDays=16 [item.itemAgeDays:itm-86], itemTitle=Plan the trip route, n=21 [item.itemQueueBehind:itm-86]
  > Plan the trip route has 21 things in the queue behind it. The item itself has been in place 2 weeks.
2026-03-29  [report observation]  areaBalance / stage 1 / editorial / ob.bal.l16
  rule:  report.observation.areaBalance
  fired: three or more areas had activity, no area holds half the week, the window holds at least 6 events, so a share is describing something real
  > Spread is not the same as balance. This week was spread.
2026-03-29  [report observation]  intakeVsOutput / stage 2 / neutral_agent / ob.flow.s2.l32
  rule:  report.observation.intakeVsOutput.s2
  fired: intake exceeds output by five or more, there are additions to count
  facts: k=7 [rollup.queueGrowth], m=7 [window.completions], n=14 [window.additions]
  > 14 things came in this week, 7 went out, and the queues ended 7 longer than they opened.
2026-03-29  [report pattern]  consistentRhythm / stage 1 / plain / pt.rhy.05
  rule:  report.pattern.consistentRhythm
  fired: four weeks inside a narrow band, there are four weeks to see it across, the band is a rhythm rather than a run of empty weeks
  > A steady month.

2026-03-30  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.32
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight with its weight spread out.
2026-03-30  [banner]  weekMixed / stage 1 / reflective / bn.mixed.17
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Spread unevenly, so far.
2026-03-30  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-03-31  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.27
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight that went in several directions.
2026-03-31  [banner]  weekMixed / stage 1 / reflective / bn.mixed.24
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > The weight of the week sits in one place.
2026-03-31  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
```

### April 2026

30 days on screen, 0 Pulses spoken, 30 Pulse days silent, 78 sentences in all.

```text
2026-04-01  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.18
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Level, more or less.
2026-04-01  [banner]  weekMixed / stage 1 / reflective / bn.mixed.20
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Concentrated, with the rest thinner.
2026-04-01  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-04-02  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.15
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Breadth, more than depth.
2026-04-02  [banner]  weekBuilding / stage 1 / reflective / bn.build.24
  rule:  banner.weekBuilding
  fired: three to five days into the week, completions are accumulating
  > The beginning has passed.
2026-04-02  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-04-03  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.10
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Broad rather than deep.
2026-04-03  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-04-03  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-04-04  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.33
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight that went around rather than down.
2026-04-04  [banner]  weekMixed / stage 1 / reflective / bn.mixed.16
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week with a heavier side.
2026-04-04  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-04-05  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.16
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > No one place took it all.
2026-04-05  [banner]  weekStarting / stage 1 / reflective / bn.start.11
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > Barely begun.
2026-04-05  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-04-05  [report headline]  personalBest / stage 1 / plain / hd.best.36
  rule:  report.headline.personalBest
  fired: no earlier week strictly beats this one, this week reaches the personal best, there is more than one week to be best of, there are completions, so no count renders as zero
  > The week that set the mark.
2026-04-05  [report observation]  personalBest / stage 1 / editorial / ob.best.l21
  rule:  report.observation.personalBest
  fired: no earlier week strictly beats this one, this week reaches the personal best, there is more than one week to be best of, there are completions, so no count renders as zero
  facts: m=7 [history.personalBestCompletions], sinceRef=2026-03-22
  > The best earlier week was 7, in March, and this one is not below it.
2026-04-05  [report observation]  areaBalance / stage 1 / editorial / ob.bal.l09
  rule:  report.observation.areaBalance
  fired: three or more areas had activity, no area holds half the week, the window holds at least 6 events, so a share is describing something real
  facts: areaCount=4 [rollup.areasWithEvents]
  > 4 areas moved and none of them ran the week.
2026-04-05  [report observation]  intakeVsOutput / stage 2 / neutral_agent / ob.flow.s2.l34
  rule:  report.observation.intakeVsOutput.s2
  fired: intake exceeds output by five or more, there are additions to count
  facts: k=7 [rollup.queueGrowth]
  > The queues hold 7 more things than they held on the first day of the week.
2026-04-05  [report observation]  queuePressure / stage 1 / neutral_agent / ob.qp.l23
  rule:  report.observation.queuePressure
  fired: at least one area's queue grew, the queues hold three or more things to count
  > One queue at least is longer than it was.
2026-04-05  [report pattern]  reportedVsActual / stage 1 / plain / pt.rva.58
  rule:  report.pattern.reportedVsActual
  fired: there are several stored answers to compare against, one family has been answered about more than twice, there are at least three weeks of snapshots, without which no pattern may fire
  > Nothing here compares what you said against what happened. It records that you said something, three times.

2026-04-06  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.12
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Nothing has dominated.
2026-04-06  [banner]  weekMixed / stage 1 / reflective / bn.mixed.14
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A shape rather than a spread.
2026-04-06  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-04-07  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.15
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Breadth, more than depth.
2026-04-07  [banner]  weekMixed / stage 1 / reflective / bn.mixed.12
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week that leans one way.
2026-04-07  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-04-08  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.20
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > No single thing has held it.
2026-04-08  [banner]  weekBuilding / stage 1 / reflective / bn.build.13
  rule:  banner.weekBuilding
  fired: three to five days into the week, completions are accumulating
  > A few days in, a few things done.
2026-04-08  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-04-09  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.29
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Several things at once, for two weeks.
2026-04-09  [banner]  weekBuilding / stage 1 / reflective / bn.build.19
  rule:  banner.weekBuilding
  fired: three to five days into the week, completions are accumulating
  > A week with a few days behind it.
2026-04-09  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-04-10  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.21
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Across, rather than down.
2026-04-10  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-04-10  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-04-11  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.30
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Spread, without any one place holding it.
2026-04-11  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-04-11  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-04-12  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.16
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > No one place took it all.
2026-04-12  [banner]  weekStarting / stage 1 / reflective / bn.start.10
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > The start of a week.
2026-04-12  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-04-12  [report headline]  personalBest / stage 1 / plain / hd.best.30
  rule:  report.headline.personalBest
  fired: no earlier week strictly beats this one, this week reaches the personal best, there is more than one week to be best of, there are completions, so no count renders as zero
  > Your ceiling, this week.
2026-04-12  [report observation]  personalBest / stage 1 / editorial / ob.best.l20
  rule:  report.observation.personalBest
  fired: no earlier week strictly beats this one, this week reaches the personal best, there is more than one week to be best of, there are completions, so no count renders as zero
  facts: n=7 [window.completions], sinceRef=2026-03-29
  > Nothing in the history reads higher than 7, and the history goes back to before March.
2026-04-12  [report observation]  areaBalance / stage 1 / editorial / ob.bal.l13
  rule:  report.observation.areaBalance
  fired: three or more areas had activity, no area holds half the week, the window holds at least 6 events, so a share is describing something real
  > Every area got some of the week and none got most of it.
2026-04-12  [report observation]  intakeVsOutput / stage 2 / neutral_agent / ob.flow.s2.l30
  rule:  report.observation.intakeVsOutput.s2
  fired: intake exceeds output by five or more, there are additions to count
  > More in than out.
2026-04-12  [report observation]  persistentItem / stage 1 / observational / ob.pers.l39
  rule:  report.observation.persistentItem.low
  fired: the item has been active seven to thirteen days, the area holding the item has at least 1 events in the window
  facts: ageDays=8 [item.itemAgeDays:itm-106], areaName=Home
  > Your active item in Home has not changed in 8 days.
2026-04-12  [report pattern]  consistentRhythm / stage 1 / plain / pt.rhy.23
  rule:  report.pattern.consistentRhythm
  fired: four weeks inside a narrow band, there are four weeks to see it across, the band is a rhythm rather than a run of empty weeks
  > A month without a big week or a small one.

2026-04-13  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.19
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Spread out, over fourteen days.
2026-04-13  [banner]  weekMixed / stage 1 / reflective / bn.mixed.18
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Unequal, with the week still going.
2026-04-13  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-04-14  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.28
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight that did not settle in one place.
2026-04-14  [banner]  weekMixed / stage 1 / reflective / bn.mixed.14
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A shape rather than a spread.
2026-04-14  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-04-15  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.04
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A wide fortnight.
2026-04-15  [banner]  weekMixed / stage 1 / reflective / bn.mixed.13
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week with a center of gravity in it.
2026-04-15  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-04-16  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.23
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Nothing has held more than half.
2026-04-16  [banner]  weekBuilding / stage 1 / reflective / bn.build.18
  rule:  banner.weekBuilding
  fired: three to five days into the week, completions are accumulating
  > Some of the week has happened.
2026-04-16  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-04-17  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.24
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight that kept moving around.
2026-04-17  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-04-17  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-04-18  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.17
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Room for more than one thing.
2026-04-18  [banner]  weekMixed / stage 1 / reflective / bn.mixed.16
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week with a heavier side.
2026-04-18  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-04-19  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.23
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Nothing has held more than half.
2026-04-19  [banner]  weekStarting / stage 1 / reflective / bn.start.10
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > The start of a week.
2026-04-19  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-04-19  [report headline]  personalBest / stage 1 / plain / hd.best.14
  rule:  report.headline.personalBest
  fired: no earlier week strictly beats this one, this week reaches the personal best, there is more than one week to be best of, there are completions, so no count renders as zero
  > The top of the whole record.
2026-04-19  [report observation]  personalBest / stage 1 / editorial / ob.best.l17
  rule:  report.observation.personalBest
  fired: no earlier week strictly beats this one, this week reaches the personal best, there is more than one week to be best of, there are completions, so no count renders as zero
  > A best week says what happened and nothing about what comes next.
2026-04-19  [report observation]  persistentItem / stage 1 / neutral_agent / ob.pers.l09
  rule:  report.observation.persistentItem.high
  fired: the item has been active fourteen days or more, the area holding the item has at least 1 events in the window, there is a median completion time to compare against
  facts: itemTitle=Read the design chapter
  > Read the design chapter is where it was.
2026-04-19  [report observation]  areaBalance / stage 1 / editorial / ob.bal.l11
  rule:  report.observation.areaBalance
  fired: three or more areas had activity, no area holds half the week, the window holds at least 6 events, so a share is describing something real
  > A wide week and a thin week look the same from a count, and nothing here tells them apart.
2026-04-19  [report observation]  intakeVsOutput / stage 2 / neutral_agent / ob.flow.s2.l27
  rule:  report.observation.intakeVsOutput.s2
  fired: intake exceeds output by five or more, there are additions to count
  > What arrived outnumbered what left.
2026-04-19  [report pattern]  reportedVsActual / stage 1 / plain / pt.rva.08
  rule:  report.pattern.reportedVsActual
  fired: there are several stored answers to compare against, one family has been answered about more than twice, there are at least three weeks of snapshots, without which no pattern may fire
  > The same question, three times.

2026-04-20  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.24
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight that kept moving around.
2026-04-20  [banner]  weekMixed / stage 1 / reflective / bn.mixed.25
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Uneven going.
2026-04-20  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-04-21  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.17
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Room for more than one thing.
2026-04-21  [banner]  weekMixed / stage 1 / reflective / bn.mixed.25
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Uneven going.
2026-04-21  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-04-22  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.19
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Spread out, over fourteen days.
2026-04-22  [banner]  weekMixed / stage 1 / reflective / bn.mixed.16
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week with a heavier side.
2026-04-22  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-04-23  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.32
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight with its weight spread out.
2026-04-23  [banner]  weekBuilding / stage 1 / reflective / bn.build.18
  rule:  banner.weekBuilding
  fired: three to five days into the week, completions are accumulating
  > Some of the week has happened.
2026-04-23  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-04-24  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.19
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Spread out, over fourteen days.
2026-04-24  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-04-24  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-04-25  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.32
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight with its weight spread out.
2026-04-25  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-04-25  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-04-26  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.30
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Spread, without any one place holding it.
2026-04-26  [banner]  weekStarting / stage 1 / reflective / bn.start.25
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > A week at the point where nothing is decided.
2026-04-26  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-04-26  [report headline]  personalBest / stage 1 / plain / hd.best.28
  rule:  report.headline.personalBest
  fired: no earlier week strictly beats this one, this week reaches the personal best, there is more than one week to be best of, there are completions, so no count renders as zero
  facts: n=7 [window.completions]
  > 7 things finished, a record.
2026-04-26  [report observation]  personalBest / stage 1 / editorial / ob.best.l15
  rule:  report.observation.personalBest
  fired: no earlier week strictly beats this one, this week reaches the personal best, there is more than one week to be best of, there are completions, so no count renders as zero
  > Something in this week worked. The record can say that it did without saying what it was.
2026-04-26  [report observation]  persistentItem / stage 1 / neutral_agent / ob.pers.l18
  rule:  report.observation.persistentItem.high
  fired: the item has been active fourteen days or more, the area holding the item has at least 1 events in the window, there is a median completion time to compare against
  facts: ageDays=22 [item.itemAgeDays:itm-106], areaName=Home
  > The front of Home has not changed in 3 weeks.
2026-04-26  [report observation]  areaBalance / stage 1 / editorial / ob.bal.l07
  rule:  report.observation.areaBalance
  fired: three or more areas had activity, no area holds half the week, the window holds at least 6 events, so a share is describing something real
  > The week had no owner.
2026-04-26  [report observation]  intakeVsOutput / stage 2 / neutral_agent / ob.flow.s2.l19
  rule:  report.observation.intakeVsOutput.s2
  fired: intake exceeds output by five or more, there are additions to count
  facts: k=7 [rollup.queueGrowth]
  > The queues are carrying 7 more than they were.
2026-04-26  [report pattern]  consistentRhythm / stage 1 / plain / pt.rhy.48
  rule:  report.pattern.consistentRhythm
  fired: four weeks inside a narrow band, there are four weeks to see it across, the band is a rhythm rather than a run of empty weeks
  > What the last four weeks have in common is their size.

2026-04-27  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.30
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Spread, without any one place holding it.
2026-04-27  [banner]  weekMixed / stage 1 / reflective / bn.mixed.16
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week with a heavier side.
2026-04-27  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-04-28  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.25
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Two weeks across several areas.
2026-04-28  [banner]  weekMixed / stage 1 / reflective / bn.mixed.24
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > The weight of the week sits in one place.
2026-04-28  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-04-29  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.04
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A wide fortnight.
2026-04-29  [banner]  weekMixed / stage 1 / reflective / bn.mixed.25
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Uneven going.
2026-04-29  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-04-30  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.22
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Two weeks that went several ways.
2026-04-30  [banner]  weekBuilding / stage 1 / reflective / bn.build.12
  rule:  banner.weekBuilding
  fired: three to five days into the week, completions are accumulating
  > Partway through.
2026-04-30  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
```

### May 2026

31 days on screen, 0 Pulses spoken, 31 Pulse days silent, 83 sentences in all.

```text
2026-05-01  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.18
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Level, more or less.
2026-05-01  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-05-01  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-05-02  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.21
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Across, rather than down.
2026-05-02  [banner]  weekMixed / stage 1 / reflective / bn.mixed.14
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A shape rather than a spread.
2026-05-02  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-05-03  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.15
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Breadth, more than depth.
2026-05-03  [banner]  weekStarting / stage 1 / reflective / bn.start.20
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > Still near the beginning.
2026-05-03  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-05-03  [report headline]  personalBest / stage 1 / plain / hd.best.33
  rule:  report.headline.personalBest
  fired: no earlier week strictly beats this one, this week reaches the personal best, there is more than one week to be best of, there are completions, so no count renders as zero
  facts: n=7 [window.completions]
  > 7 finished. A ceiling.
2026-05-03  [report observation]  personalBest / stage 1 / editorial / ob.best.l10
  rule:  report.observation.personalBest
  fired: no earlier week strictly beats this one, this week reaches the personal best, there is more than one week to be best of, there are completions, so no count renders as zero
  > Nothing here needs to happen again.
2026-05-03  [report observation]  areaBalance / stage 1 / editorial / ob.bal.l08
  rule:  report.observation.areaBalance
  fired: three or more areas had activity, no area holds half the week, the window holds at least 6 events, so a share is describing something real
  > A week can be wide or it can be deep.
2026-05-03  [report observation]  intakeVsOutput / stage 2 / neutral_agent / ob.flow.s2.l29
  rule:  report.observation.intakeVsOutput.s2
  fired: intake exceeds output by five or more, there are additions to count
  > A collecting week.
2026-05-03  [report observation]  persistentItem / stage 1 / observational / ob.pers.l36
  rule:  report.observation.persistentItem.low
  fired: the item has been active seven to thirteen days, the area holding the item has at least 1 events in the window
  facts: areaName=Personal, itemTitle=Fix the leaking tap
  > Nothing in the Personal queue has come past Fix the leaking tap.
2026-05-03  [report pattern]  reportedVsActual / stage 1 / plain / pt.rva.54
  rule:  report.pattern.reportedVsActual
  fired: there are several stored answers to compare against, one family has been answered about more than twice, there are at least three weeks of snapshots, without which no pattern may fire
  > An answer is a reading taken at a moment, and this app now holds five of yours or more.

2026-05-04  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.20
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > No single thing has held it.
2026-05-04  [banner]  weekMixed / stage 1 / reflective / bn.mixed.11
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Weighted rather than spread.
2026-05-04  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-05-05  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.26
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Several places, none of them all of it.
2026-05-05  [banner]  weekMixed / stage 1 / reflective / bn.mixed.19
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week gathered mostly in one place.
2026-05-05  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-05-06  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.20
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > No single thing has held it.
2026-05-06  [banner]  weekMixed / stage 1 / reflective / bn.mixed.25
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Uneven going.
2026-05-06  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-05-07  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.30
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Spread, without any one place holding it.
2026-05-07  [banner]  weekBuilding / stage 1 / reflective / bn.build.16
  rule:  banner.weekBuilding
  fired: three to five days into the week, completions are accumulating
  > A week no longer new.
2026-05-07  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-05-08  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.19
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Spread out, over fourteen days.
2026-05-08  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-05-08  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-05-09  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.25
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Two weeks across several areas.
2026-05-09  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-05-09  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-05-10  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.07
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Evenly spread, across two weeks.
2026-05-10  [banner]  weekStarting / stage 1 / reflective / bn.start.23
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > Nothing here has had time to become a shape.
2026-05-10  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-05-10  [report headline]  personalBest / stage 1 / plain / hd.best.59
  rule:  report.headline.personalBest
  fired: no earlier week strictly beats this one, this week reaches the personal best, there is more than one week to be best of, there are completions, so no count renders as zero
  facts: n=7 [window.completions]
  > 7 completions, none higher.
2026-05-10  [report observation]  personalBest / stage 1 / editorial / ob.best.l12
  rule:  report.observation.personalBest
  fired: no earlier week strictly beats this one, this week reaches the personal best, there is more than one week to be best of, there are completions, so no count renders as zero
  > The record moved. That is all the record does.
2026-05-10  [report observation]  persistentItem / stage 1 / neutral_agent / ob.pers.l08
  rule:  report.observation.persistentItem.high
  fired: the item has been active fourteen days or more, the area holding the item has at least 1 events in the window, there is a median completion time to compare against
  facts: ageDays=18 [item.itemAgeDays:itm-126], areaName=Personal
  > 2 weeks at the front of Personal.
2026-05-10  [report observation]  areaBalance / stage 1 / editorial / ob.bal.l12
  rule:  report.observation.areaBalance
  fired: three or more areas had activity, no area holds half the week, the window holds at least 6 events, so a share is describing something real
  > The week had a shape without a subject.
2026-05-10  [report observation]  intakeVsOutput / stage 2 / neutral_agent / ob.flow.s2.l22
  rule:  report.observation.intakeVsOutput.s2
  fired: intake exceeds output by five or more, there are additions to count
  > Five apart, at least.
2026-05-10  [report pattern]  consistentRhythm / stage 1 / plain / pt.rhy.06
  rule:  report.pattern.consistentRhythm
  fired: four weeks inside a narrow band, there are four weeks to see it across, the band is a rhythm rather than a run of empty weeks
  > Four weeks of roughly the same shape.

2026-05-11  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.07
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Evenly spread, across two weeks.
2026-05-11  [banner]  weekMixed / stage 1 / reflective / bn.mixed.10
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Lopsided, for now.
2026-05-11  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-05-12  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.23
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Nothing has held more than half.
2026-05-12  [banner]  weekBuilding / stage 1 / reflective / bn.build.18
  rule:  banner.weekBuilding
  fired: three to five days into the week, completions are accumulating
  > Some of the week has happened.
2026-05-12  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-05-13  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.18
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Level, more or less.
2026-05-13  [banner]  weekBuilding / stage 1 / reflective / bn.build.16
  rule:  banner.weekBuilding
  fired: three to five days into the week, completions are accumulating
  > A week no longer new.
2026-05-13  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-05-14  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.16
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > No one place took it all.
2026-05-14  [banner]  weekBuilding / stage 1 / reflective / bn.build.28
  rule:  banner.weekBuilding
  fired: three to five days into the week, completions are accumulating
  > Some days behind it, more of it to come.
2026-05-14  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-05-15  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.19
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Spread out, over fourteen days.
2026-05-15  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-05-15  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-05-16  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.17
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Room for more than one thing.
2026-05-16  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-05-16  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-05-17  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.12
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Nothing has dominated.
2026-05-17  [banner]  weekStarting / stage 1 / reflective / bn.start.26
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > A week still near its own beginning.
2026-05-17  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-05-17  [report headline]  personalBest / stage 1 / plain / hd.best.01
  rule:  report.headline.personalBest
  fired: no earlier week strictly beats this one, this week reaches the personal best, there is more than one week to be best of, there are completions, so no count renders as zero
  > Your most productive week.
2026-05-17  [report observation]  personalBest / stage 1 / editorial / ob.best.l04
  rule:  report.observation.personalBest
  fired: no earlier week strictly beats this one, this week reaches the personal best, there is more than one week to be best of, there are completions, so no count renders as zero
  > Nothing in eleven weeks of history looks like this one.
2026-05-17  [report observation]  areaBalance / stage 1 / editorial / ob.bal.l10
  rule:  report.observation.areaBalance
  fired: three or more areas had activity, no area holds half the week, the window holds at least 6 events, so a share is describing something real
  > Nothing here was the main thing.
2026-05-17  [report observation]  intakeVsOutput / stage 2 / neutral_agent / ob.flow.s2.l18
  rule:  report.observation.intakeVsOutput.s2
  fired: intake exceeds output by five or more, there are additions to count
  facts: k=7 [rollup.queueGrowth], m=7 [window.completions], n=14 [window.additions]
  > 14 in, 7 out, and 7 added to the queues.
2026-05-17  [report observation]  queuePressure / stage 1 / neutral_agent / ob.qp.l10
  rule:  report.observation.queuePressure
  fired: at least one area's queue grew, the queues hold three or more things to count
  > The queues are longer.
2026-05-17  [report pattern]  reportedVsActual / stage 1 / plain / pt.rva.29
  rule:  report.pattern.reportedVsActual
  fired: there are several stored answers to compare against, one family has been answered about more than twice, there are at least three weeks of snapshots, without which no pattern may fire
  > You have answered at least five pulses since you started.

2026-05-18  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.26
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Several places, none of them all of it.
2026-05-18  [banner]  weekMixed / stage 1 / reflective / bn.mixed.16
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week with a heavier side.
2026-05-18  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-05-19  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.29
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Several things at once, for two weeks.
2026-05-19  [banner]  weekMixed / stage 1 / reflective / bn.mixed.19
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week gathered mostly in one place.
2026-05-19  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-05-20  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.04
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A wide fortnight.
2026-05-20  [banner]  weekMixed / stage 1 / reflective / bn.mixed.23
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week that did not spread evenly.
2026-05-20  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-05-21  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.13
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Wide, across two weeks.
2026-05-21  [banner]  weekBuilding / stage 1 / reflective / bn.build.29
  rule:  banner.weekBuilding
  fired: three to five days into the week, completions are accumulating
  > Some days gone, some things done, the week still open.
2026-05-21  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-05-22  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.18
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Level, more or less.
2026-05-22  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-05-22  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-05-23  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.19
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Spread out, over fourteen days.
2026-05-23  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-05-23  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-05-24  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.16
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > No one place took it all.
2026-05-24  [banner]  weekStarting / stage 1 / reflective / bn.start.19
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > Little to read this early.
2026-05-24  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-05-24  [report headline]  personalBest / stage 1 / plain / hd.best.12
  rule:  report.headline.personalBest
  fired: no earlier week strictly beats this one, this week reaches the personal best, there is more than one week to be best of, there are completions, so no count renders as zero
  facts: n=7 [window.completions]
  > 7 this week, a first.
2026-05-24  [report observation]  personalBest / stage 1 / editorial / ob.best.l18
  rule:  report.observation.personalBest
  fired: no earlier week strictly beats this one, this week reaches the personal best, there is more than one week to be best of, there are completions, so no count renders as zero
  facts: n=7 [window.completions]
  > 7 completions is the largest number this record has held, and a number is all it is.
2026-05-24  [report observation]  areaBalance / stage 1 / editorial / ob.bal.l04
  rule:  report.observation.areaBalance
  fired: three or more areas had activity, no area holds half the week, the window holds at least 6 events, so a share is describing something real
  > No single area owned this week.
2026-05-24  [report observation]  intakeVsOutput / stage 2 / neutral_agent / ob.flow.s2.l14
  rule:  report.observation.intakeVsOutput.s2
  fired: intake exceeds output by five or more, there are additions to count
  > Nothing added this week has left yet.
2026-05-24  [report observation]  persistentItem / stage 1 / observational / ob.pers.l44
  rule:  report.observation.persistentItem.low
  fired: the item has been active seven to thirteen days, the area holding the item has at least 1 events in the window
  facts: ageDays=10 [item.itemAgeDays:itm-146], areaName=Home, itemTitle=Clear the garage shelf
  > For 10 days the first thing in Home has been Clear the garage shelf. It is still the first thing.
2026-05-24  [report pattern]  consistentRhythm / stage 1 / plain / pt.rhy.59
  rule:  report.pattern.consistentRhythm
  fired: four weeks inside a narrow band, there are four weeks to see it across, the band is a rhythm rather than a run of empty weeks
  > Four weeks alike.

2026-05-25  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.26
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Several places, none of them all of it.
2026-05-25  [banner]  weekMixed / stage 1 / reflective / bn.mixed.15
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Tilted, so far.
2026-05-25  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-05-26  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.25
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Two weeks across several areas.
2026-05-26  [banner]  weekBuilding / stage 1 / reflective / bn.build.13
  rule:  banner.weekBuilding
  fired: three to five days into the week, completions are accumulating
  > A few days in, a few things done.
2026-05-26  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-05-27  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.16
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > No one place took it all.
2026-05-27  [banner]  weekBuilding / stage 1 / reflective / bn.build.10
  rule:  banner.weekBuilding
  fired: three to five days into the week, completions are accumulating
  > Midweek, with something in it.
2026-05-27  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-05-28  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.33
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight that went around rather than down.
2026-05-28  [banner]  weekBuilding / stage 1 / reflective / bn.build.23
  rule:  banner.weekBuilding
  fired: three to five days into the week, completions are accumulating
  > Something already done, the week still open.
2026-05-28  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-05-29  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.23
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Nothing has held more than half.
2026-05-29  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-05-29  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-05-30  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.17
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Room for more than one thing.
2026-05-30  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-05-30  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-05-31  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.32
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight with its weight spread out.
2026-05-31  [banner]  weekStarting / stage 1 / reflective / bn.start.26
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > A week still near its own beginning.
2026-05-31  [pulse]  SILENT (NO_RULE_QUALIFIED)
2026-05-31  [report headline]  personalBest / stage 1 / plain / hd.best.50
  rule:  report.headline.personalBest
  fired: no earlier week strictly beats this one, this week reaches the personal best, there is more than one week to be best of, there are completions, so no count renders as zero
  > The top week so far.
2026-05-31  [report observation]  personalBest / stage 1 / editorial / ob.best.l11
  rule:  report.observation.personalBest
  fired: no earlier week strictly beats this one, this week reaches the personal best, there is more than one week to be best of, there are completions, so no count renders as zero
  facts: n=7 [window.completions]
  > The number is 7. The reading of it is not the app's to make.
2026-05-31  [report observation]  persistentItem / stage 1 / neutral_agent / ob.pers.l11
  rule:  report.observation.persistentItem.high
  fired: the item has been active fourteen days or more, the area holding the item has at least 1 events in the window, there is a median completion time to compare against
  facts: ageDays=17 [item.itemAgeDays:itm-146], areaName=Home, itemTitle=Clear the garage shelf
  > The front of Home has been Clear the garage shelf for 2 weeks and nothing in the queue has come past it.
2026-05-31  [report observation]  areaBalance / stage 1 / editorial / ob.bal.l14
  rule:  report.observation.areaBalance
  fired: three or more areas had activity, no area holds half the week, the window holds at least 6 events, so a share is describing something real
  facts: pct=32 [area.areaShare:personal]
  > 32 percent is the largest share anything took. It is not a majority.
2026-05-31  [report observation]  intakeVsOutput / stage 2 / neutral_agent / ob.flow.s2.l35
  rule:  report.observation.intakeVsOutput.s2
  fired: intake exceeds output by five or more, there are additions to count
  > Five or more things separate what arrived this week from what left it, and seven days is where both numbers came from.
2026-05-31  [report pattern]  reportedVsActual / stage 1 / plain / pt.rva.34
  rule:  report.pattern.reportedVsActual
  fired: there are several stored answers to compare against, one family has been answered about more than twice, there are at least three weeks of snapshots, without which no pattern may fire
  > Five answers or more sit in the record now.
```

### June 2026

30 days on screen, 0 Pulses spoken, 30 Pulse days silent, 76 sentences in all.

```text
2026-06-01  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.10
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Broad rather than deep.
2026-06-01  [banner]  weekMixed / stage 1 / reflective / bn.mixed.21
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week with more of it in one area.
2026-06-01  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-06-02  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.15
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Breadth, more than depth.
2026-06-02  [banner]  weekMixed / stage 1 / reflective / bn.mixed.25
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Uneven going.
2026-06-02  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-06-03  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.18
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Level, more or less.
2026-06-03  [banner]  weekBuilding / stage 1 / reflective / bn.build.16
  rule:  banner.weekBuilding
  fired: three to five days into the week, completions are accumulating
  > A week no longer new.
2026-06-03  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-06-04  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.14
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight in several places.
2026-06-04  [banner]  weekBuilding / stage 1 / reflective / bn.build.24
  rule:  banner.weekBuilding
  fired: three to five days into the week, completions are accumulating
  > The beginning has passed.
2026-06-04  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-06-05  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.15
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Breadth, more than depth.
2026-06-05  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-06-05  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-06-06  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.28
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight that did not settle in one place.
2026-06-06  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-06-06  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-06-07  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.23
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Nothing has held more than half.
2026-06-07  [banner]  weekStarting / stage 1 / reflective / bn.start.24
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > The shape of this week is still ahead.
2026-06-07  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-06-07  [report headline]  personalBest / stage 1 / plain / hd.best.04
  rule:  report.headline.personalBest
  fired: no earlier week strictly beats this one, this week reaches the personal best, there is more than one week to be best of, there are completions, so no count renders as zero
  > The most you have finished.
2026-06-07  [report observation]  personalBest / stage 1 / editorial / ob.best.l16
  rule:  report.observation.personalBest
  fired: no earlier week strictly beats this one, this week reaches the personal best, there is more than one week to be best of, there are completions, so no count renders as zero
  facts: m=7 [history.personalBestCompletions]
  > The previous best was 7. This week is not less than that.
2026-06-07  [report observation]  areaBalance / stage 1 / editorial / ob.bal.l17
  rule:  report.observation.areaBalance
  fired: three or more areas had activity, no area holds half the week, the window holds at least 6 events, so a share is describing something real
  facts: areaCount=4 [rollup.areasWithEvents]
  > A week that touches 4 areas and gives none of them half has no single subject.
2026-06-07  [report observation]  intakeVsOutput / stage 2 / neutral_agent / ob.flow.s2.l26
  rule:  report.observation.intakeVsOutput.s2
  fired: intake exceeds output by five or more, there are additions to count
  > The queues took on more than they let go.
2026-06-07  [report observation]  queuePressure / stage 1 / neutral_agent / ob.qp.l12
  rule:  report.observation.queuePressure
  fired: at least one area's queue grew, the queues hold three or more things to count
  facts: areaCount=4 [rollup.areasWithQueue]
  > Something waits in 4 areas.
2026-06-07  [report pattern]  consistentRhythm / stage 1 / plain / pt.rhy.01
  rule:  report.pattern.consistentRhythm
  fired: four weeks inside a narrow band, there are four weeks to see it across, the band is a rhythm rather than a run of empty weeks
  > Your activity has stayed within a narrow band for four weeks.

2026-06-08  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.26
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Several places, none of them all of it.
2026-06-08  [banner]  weekMixed / stage 1 / reflective / bn.mixed.13
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week with a center of gravity in it.
2026-06-08  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-06-09  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.23
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Nothing has held more than half.
2026-06-09  [banner]  weekBuilding / stage 1 / reflective / bn.build.13
  rule:  banner.weekBuilding
  fired: three to five days into the week, completions are accumulating
  > A few days in, a few things done.
2026-06-09  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-06-10  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.15
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Breadth, more than depth.
2026-06-10  [banner]  weekBuilding / stage 1 / reflective / bn.build.20
  rule:  banner.weekBuilding
  fired: three to five days into the week, completions are accumulating
  > The middle days.
2026-06-10  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-06-11  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.25
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Two weeks across several areas.
2026-06-11  [banner]  weekBuilding / stage 1 / reflective / bn.build.24
  rule:  banner.weekBuilding
  fired: three to five days into the week, completions are accumulating
  > The beginning has passed.
2026-06-11  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-06-12  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.30
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Spread, without any one place holding it.
2026-06-12  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-06-12  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-06-13  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.16
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > No one place took it all.
2026-06-13  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-06-13  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-06-14  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.29
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Several things at once, for two weeks.
2026-06-14  [banner]  weekStarting / stage 1 / reflective / bn.start.14
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > Too early for much of a reading.
2026-06-14  [pulse]  SILENT (NO_RULE_QUALIFIED)
2026-06-14  [report headline]  personalBest / stage 1 / plain / hd.best.32
  rule:  report.headline.personalBest
  fired: no earlier week strictly beats this one, this week reaches the personal best, there is more than one week to be best of, there are completions, so no count renders as zero
  > The most in the record.
2026-06-14  [report observation]  personalBest / stage 1 / editorial / ob.best.l09
  rule:  report.observation.personalBest
  fired: no earlier week strictly beats this one, this week reaches the personal best, there is more than one week to be best of, there are completions, so no count renders as zero
  > A record is a fact about the past. Nothing here turns it into a target for the week ahead.
2026-06-14  [report observation]  areaBalance / stage 1 / editorial / ob.bal.l15
  rule:  report.observation.areaBalance
  fired: three or more areas had activity, no area holds half the week, the window holds at least 6 events, so a share is describing something real
  facts: areaCount=4 [rollup.areasWithEvents]
  > Attention went to 4 places and settled in none of them.
2026-06-14  [report observation]  intakeVsOutput / stage 2 / neutral_agent / ob.flow.s2.l24
  rule:  report.observation.intakeVsOutput.s2
  fired: intake exceeds output by five or more, there are additions to count
  > Intake ran ahead of output.
2026-06-14  [report observation]  persistentItem / stage 1 / observational / ob.pers.l02
  rule:  report.observation.persistentItem.low
  fired: the item has been active seven to thirteen days, the area holding the item has at least 1 events in the window
  facts: ageDays=13 [item.itemAgeDays:itm-166], itemTitle=Call the bank
  > Nothing has moved past Call the bank in 13 days.
2026-06-14  [report pattern]  reportedVsActual / stage 1 / plain / pt.rva.47
  rule:  report.pattern.reportedVsActual
  fired: there are several stored answers to compare against, one family has been answered about more than twice, there are at least three weeks of snapshots, without which no pattern may fire
  > You have answered at least five pulses, and three of them were about the same kind of moment.

2026-06-15  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.14
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight in several places.
2026-06-15  [banner]  weekMixed / stage 1 / reflective / bn.mixed.09
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week with a tilt to it.
2026-06-15  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-06-16  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.20
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > No single thing has held it.
2026-06-16  [banner]  weekMixed / stage 1 / reflective / bn.mixed.14
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A shape rather than a spread.
2026-06-16  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-06-17  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.14
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight in several places.
2026-06-17  [banner]  weekBuilding / stage 1 / reflective / bn.build.20
  rule:  banner.weekBuilding
  fired: three to five days into the week, completions are accumulating
  > The middle days.
2026-06-17  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-06-18  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.12
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Nothing has dominated.
2026-06-18  [banner]  weekBuilding / stage 1 / reflective / bn.build.18
  rule:  banner.weekBuilding
  fired: three to five days into the week, completions are accumulating
  > Some of the week has happened.
2026-06-18  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-06-19  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.29
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Several things at once, for two weeks.
2026-06-19  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-06-19  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-06-20  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.30
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Spread, without any one place holding it.
2026-06-20  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-06-20  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-06-21  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.31
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > More than one thing at a time.
2026-06-21  [banner]  weekStarting / stage 1 / reflective / bn.start.09
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > Early yet.
2026-06-21  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-06-21  [report headline]  personalBest / stage 1 / plain / hd.best.55
  rule:  report.headline.personalBest
  fired: no earlier week strictly beats this one, this week reaches the personal best, there is more than one week to be best of, there are completions, so no count renders as zero
  facts: n=7 [window.completions]
  > 7 finished, nothing above.
2026-06-21  [report observation]  personalBest / stage 1 / editorial / ob.best.l13
  rule:  report.observation.personalBest
  fired: no earlier week strictly beats this one, this week reaches the personal best, there is more than one week to be best of, there are completions, so no count renders as zero
  > A high week is a fact.
2026-06-21  [report observation]  persistentItem / stage 1 / neutral_agent / ob.pers.l27
  rule:  report.observation.persistentItem.high
  fired: the item has been active fourteen days or more, the area holding the item has at least 1 events in the window, there is a median completion time to compare against
  facts: areaName=Personal, itemTitle=Call the bank
  > Personal had a week with things in it. Call the bank was at the front for all of them.
2026-06-21  [report observation]  areaBalance / stage 1 / editorial / ob.bal.l16
  rule:  report.observation.areaBalance
  fired: three or more areas had activity, no area holds half the week, the window holds at least 6 events, so a share is describing something real
  > Spread is not the same as balance. This week was spread.
2026-06-21  [report observation]  intakeVsOutput / stage 2 / neutral_agent / ob.flow.s2.l31
  rule:  report.observation.intakeVsOutput.s2
  fired: intake exceeds output by five or more, there are additions to count
  facts: k=7 [rollup.queueGrowth], n=14 [window.additions]
  > 14 things came in over seven days and the lists are 7 longer for it.
2026-06-21  [report pattern]  consistentRhythm / stage 1 / plain / pt.rhy.54
  rule:  report.pattern.consistentRhythm
  fired: four weeks inside a narrow band, there are four weeks to see it across, the band is a rhythm rather than a run of empty weeks
  > A month can be steady without being still, and this one had a total in every week of it.

2026-06-22  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.17
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Room for more than one thing.
2026-06-22  [banner]  weekMixed / stage 1 / reflective / bn.mixed.26
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Most of it in one area, so far.
2026-06-22  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-06-23  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.27
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight that went in several directions.
2026-06-23  [banner]  weekMixed / stage 1 / reflective / bn.mixed.11
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Weighted rather than spread.
2026-06-23  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-06-24  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.20
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > No single thing has held it.
2026-06-24  [banner]  weekMixed / stage 1 / reflective / bn.mixed.25
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Uneven going.
2026-06-24  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-06-25  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.24
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight that kept moving around.
2026-06-25  [banner]  weekBuilding / stage 1 / reflective / bn.build.14
  rule:  banner.weekBuilding
  fired: three to five days into the week, completions are accumulating
  > Days behind it, days ahead.
2026-06-25  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-06-26  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.04
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A wide fortnight.
2026-06-26  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-06-26  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-06-27  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.14
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight in several places.
2026-06-27  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-06-27  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-06-28  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.10
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Broad rather than deep.
2026-06-28  [banner]  weekStarting / stage 1 / reflective / bn.start.26
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > A week still near its own beginning.
2026-06-28  [pulse]  SILENT (NO_RULE_QUALIFIED)
2026-06-28  [report headline]  personalBest / stage 1 / plain / hd.best.46
  rule:  report.headline.personalBest
  fired: no earlier week strictly beats this one, this week reaches the personal best, there is more than one week to be best of, there are completions, so no count renders as zero
  facts: n=7 [window.completions]
  > 7 finished, the most yet.
2026-06-28  [report observation]  personalBest / stage 1 / editorial / ob.best.l21
  rule:  report.observation.personalBest
  fired: no earlier week strictly beats this one, this week reaches the personal best, there is more than one week to be best of, there are completions, so no count renders as zero
  facts: m=7 [history.personalBestCompletions], sinceRef=2026-06-14
  > The best earlier week was 7, in June, and this one is not below it.
2026-06-28  [report observation]  areaBalance / stage 1 / editorial / ob.bal.l09
  rule:  report.observation.areaBalance
  fired: three or more areas had activity, no area holds half the week, the window holds at least 6 events, so a share is describing something real
  facts: areaCount=4 [rollup.areasWithEvents]
  > 4 areas moved and none of them ran the week.
2026-06-28  [report observation]  intakeVsOutput / stage 2 / neutral_agent / ob.flow.s2.l16
  rule:  report.observation.intakeVsOutput.s2
  fired: intake exceeds output by five or more, there are additions to count
  facts: k=7 [rollup.queueGrowth]
  > 7 net, into the queues.
2026-06-28  [report observation]  queuePressure / stage 1 / neutral_agent / ob.qp.l30
  rule:  report.observation.queuePressure
  fired: at least one area's queue grew, the queues hold three or more things to count
  facts: areaCount=4 [rollup.areasWithQueue], n=171 [rollup.queueTotal]
  > The queues hold 171 things now, which is what 4 areas have between them.
2026-06-28  [report pattern]  reportedVsActual / stage 1 / plain / pt.rva.41
  rule:  report.pattern.reportedVsActual
  fired: there are several stored answers to compare against, one family has been answered about more than twice, there are at least three weeks of snapshots, without which no pattern may fire
  > A question the app has asked you more than twice.

2026-06-29  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.15
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Breadth, more than depth.
2026-06-29  [banner]  weekMixed / stage 1 / reflective / bn.mixed.14
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A shape rather than a spread.
2026-06-29  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-06-30  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.10
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Broad rather than deep.
2026-06-30  [banner]  weekMixed / stage 1 / reflective / bn.mixed.18
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Unequal, with the week still going.
2026-06-30  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
```

### July 2026

31 days on screen, 0 Pulses spoken, 31 Pulse days silent, 78 sentences in all.

```text
2026-07-01  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.29
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Several things at once, for two weeks.
2026-07-01  [banner]  weekBuilding / stage 1 / reflective / bn.build.17
  rule:  banner.weekBuilding
  fired: three to five days into the week, completions are accumulating
  > A week that has begun to fill.
2026-07-01  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-07-02  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.18
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Level, more or less.
2026-07-02  [banner]  weekBuilding / stage 1 / reflective / bn.build.29
  rule:  banner.weekBuilding
  fired: three to five days into the week, completions are accumulating
  > Some days gone, some things done, the week still open.
2026-07-02  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-07-03  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.12
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Nothing has dominated.
2026-07-03  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-07-03  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-07-04  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.16
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > No one place took it all.
2026-07-04  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-07-04  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-07-05  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.30
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Spread, without any one place holding it.
2026-07-05  [banner]  weekStarting / stage 1 / reflective / bn.start.26
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > A week still near its own beginning.
2026-07-05  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-07-05  [report headline]  personalBest / stage 1 / plain / hd.best.41
  rule:  report.headline.personalBest
  fired: no earlier week strictly beats this one, this week reaches the personal best, there is more than one week to be best of, there are completions, so no count renders as zero
  facts: n=7 [window.completions]
  > 7, and nothing over it.
2026-07-05  [report observation]  personalBest / stage 1 / editorial / ob.best.l14
  rule:  report.observation.personalBest
  fired: no earlier week strictly beats this one, this week reaches the personal best, there is more than one week to be best of, there are completions, so no count renders as zero
  facts: n=7 [window.completions]
  > 7 is where the record stands now.
2026-07-05  [report observation]  areaBalance / stage 1 / editorial / ob.bal.l10
  rule:  report.observation.areaBalance
  fired: three or more areas had activity, no area holds half the week, the window holds at least 6 events, so a share is describing something real
  > Nothing here was the main thing.
2026-07-05  [report observation]  intakeVsOutput / stage 2 / neutral_agent / ob.flow.s2.l32
  rule:  report.observation.intakeVsOutput.s2
  fired: intake exceeds output by five or more, there are additions to count
  facts: k=7 [rollup.queueGrowth], m=7 [window.completions], n=14 [window.additions]
  > 14 things came in this week, 7 went out, and the queues ended 7 longer than they opened.
2026-07-05  [report observation]  persistentItem / stage 1 / observational / ob.pers.l34
  rule:  report.observation.persistentItem.low
  fired: the item has been active seven to thirteen days, the area holding the item has at least 1 events in the window
  facts: ageDays=12 [item.itemAgeDays:itm-186], areaName=Home, itemTitle=Cancel the old subscription
  > Home has had a week and Cancel the old subscription has had 12 days.
2026-07-05  [report pattern]  consistentRhythm / stage 1 / plain / pt.rhy.46
  rule:  report.pattern.consistentRhythm
  fired: four weeks inside a narrow band, there are four weeks to see it across, the band is a rhythm rather than a run of empty weeks
  > Four weeks of the same weight.

2026-07-06  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.16
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > No one place took it all.
2026-07-06  [banner]  weekMixed / stage 1 / reflective / bn.mixed.15
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Tilted, so far.
2026-07-06  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-07-07  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.07
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Evenly spread, across two weeks.
2026-07-07  [banner]  weekMixed / stage 1 / reflective / bn.mixed.11
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Weighted rather than spread.
2026-07-07  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-07-08  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.17
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Room for more than one thing.
2026-07-08  [banner]  weekMixed / stage 1 / reflective / bn.mixed.26
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Most of it in one area, so far.
2026-07-08  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-07-09  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.07
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Evenly spread, across two weeks.
2026-07-09  [banner]  weekBuilding / stage 1 / reflective / bn.build.11
  rule:  banner.weekBuilding
  fired: three to five days into the week, completions are accumulating
  > The middle of a week.
2026-07-09  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-07-10  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.27
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight that went in several directions.
2026-07-10  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-07-10  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-07-11  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.07
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Evenly spread, across two weeks.
2026-07-11  [banner]  weekMixed / stage 1 / reflective / bn.mixed.14
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A shape rather than a spread.
2026-07-11  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-07-12  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.20
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > No single thing has held it.
2026-07-12  [banner]  weekStarting / stage 1 / reflective / bn.start.13
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > The opening of a week.
2026-07-12  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-07-12  [report headline]  personalBest / stage 1 / plain / hd.best.11
  rule:  report.headline.personalBest
  fired: no earlier week strictly beats this one, this week reaches the personal best, there is more than one week to be best of, there are completions, so no count renders as zero
  > Nothing earlier beat it.
2026-07-12  [report observation]  personalBest / stage 1 / editorial / ob.best.l17
  rule:  report.observation.personalBest
  fired: no earlier week strictly beats this one, this week reaches the personal best, there is more than one week to be best of, there are completions, so no count renders as zero
  > A best week says what happened and nothing about what comes next.
2026-07-12  [report observation]  persistentItem / stage 1 / neutral_agent / ob.pers.l24
  rule:  report.observation.persistentItem.high
  fired: the item has been active fourteen days or more, the area holding the item has at least 1 events in the window, there is a median completion time to compare against
  facts: ageDays=19 [item.itemAgeDays:itm-186], areaName=Home, itemTitle=Cancel the old subscription, n=48 [item.itemQueueBehind:itm-186]
  > Cancel the old subscription has held the front of Home for 2 weeks, with 48 things in the queue behind it.
2026-07-12  [report observation]  areaBalance / stage 1 / editorial / ob.bal.l13
  rule:  report.observation.areaBalance
  fired: three or more areas had activity, no area holds half the week, the window holds at least 6 events, so a share is describing something real
  > Every area got some of the week and none got most of it.
2026-07-12  [report observation]  intakeVsOutput / stage 2 / neutral_agent / ob.flow.s2.l21
  rule:  report.observation.intakeVsOutput.s2
  fired: intake exceeds output by five or more, there are additions to count
  facts: n=14 [window.additions]
  > 14 arrived.
2026-07-12  [report pattern]  reportedVsActual / stage 1 / plain / pt.rva.18
  rule:  report.pattern.reportedVsActual
  fired: there are several stored answers to compare against, one family has been answered about more than twice, there are at least three weeks of snapshots, without which no pattern may fire
  > Answers, and then more answers.

2026-07-13  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.07
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Evenly spread, across two weeks.
2026-07-13  [banner]  weekMixed / stage 1 / reflective / bn.mixed.11
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Weighted rather than spread.
2026-07-13  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-07-14  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.07
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Evenly spread, across two weeks.
2026-07-14  [banner]  weekMixed / stage 1 / reflective / bn.mixed.22
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Some places busier than others.
2026-07-14  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-07-15  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.18
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Level, more or less.
2026-07-15  [banner]  weekMixed / stage 1 / reflective / bn.mixed.14
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A shape rather than a spread.
2026-07-15  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-07-16  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.10
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Broad rather than deep.
2026-07-16  [banner]  weekBuilding / stage 1 / reflective / bn.build.30
  rule:  banner.weekBuilding
  fired: three to five days into the week, completions are accumulating
  > Far enough in that something has happened.
2026-07-16  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-07-17  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.24
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight that kept moving around.
2026-07-17  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-07-17  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-07-18  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.24
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight that kept moving around.
2026-07-18  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-07-18  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-07-19  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.22
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Two weeks that went several ways.
2026-07-19  [banner]  weekStarting / stage 1 / reflective / bn.start.16
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > A week still mostly ahead.
2026-07-19  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-07-19  [report headline]  personalBest / stage 1 / plain / hd.best.17
  rule:  report.headline.personalBest
  fired: no earlier week strictly beats this one, this week reaches the personal best, there is more than one week to be best of, there are completions, so no count renders as zero
  > Your highest week here.
2026-07-19  [report observation]  personalBest / stage 1 / editorial / ob.best.l20
  rule:  report.observation.personalBest
  fired: no earlier week strictly beats this one, this week reaches the personal best, there is more than one week to be best of, there are completions, so no count renders as zero
  facts: n=7 [window.completions], sinceRef=2026-07-05
  > Nothing in the history reads higher than 7, and the history goes back to before July.
2026-07-19  [report observation]  areaBalance / stage 1 / editorial / ob.bal.l07
  rule:  report.observation.areaBalance
  fired: three or more areas had activity, no area holds half the week, the window holds at least 6 events, so a share is describing something real
  > The week had no owner.
2026-07-19  [report observation]  intakeVsOutput / stage 2 / neutral_agent / ob.flow.s2.l12
  rule:  report.observation.intakeVsOutput.s2
  fired: intake exceeds output by five or more, there are additions to count
  facts: k=7 [rollup.queueGrowth]
  > 7 more things are waiting than were on Sunday.
2026-07-19  [report observation]  persistentItem / stage 1 / observational / ob.pers.l29
  rule:  report.observation.persistentItem.low
  fired: the item has been active seven to thirteen days, the area holding the item has at least 1 events in the window
  facts: areaName=Personal
  > The front of Personal is unchanged.
2026-07-19  [report pattern]  consistentRhythm / stage 1 / plain / pt.rhy.17
  rule:  report.pattern.consistentRhythm
  fired: four weeks inside a narrow band, there are four weeks to see it across, the band is a rhythm rather than a run of empty weeks
  > Nothing has moved much in a month.

2026-07-20  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.33
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight that went around rather than down.
2026-07-20  [banner]  weekMixed / stage 1 / reflective / bn.mixed.11
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Weighted rather than spread.
2026-07-20  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-07-21  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.22
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Two weeks that went several ways.
2026-07-21  [banner]  weekMixed / stage 1 / reflective / bn.mixed.24
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > The weight of the week sits in one place.
2026-07-21  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-07-22  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.13
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Wide, across two weeks.
2026-07-22  [banner]  weekMixed / stage 1 / reflective / bn.mixed.17
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Spread unevenly, so far.
2026-07-22  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-07-23  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.30
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Spread, without any one place holding it.
2026-07-23  [banner]  weekBuilding / stage 1 / reflective / bn.build.31
  rule:  banner.weekBuilding
  fired: three to five days into the week, completions are accumulating
  > Part of a week, with something in it.
2026-07-23  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-07-24  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.20
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > No single thing has held it.
2026-07-24  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-07-24  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-07-25  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.22
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Two weeks that went several ways.
2026-07-25  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-07-25  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-07-26  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.12
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Nothing has dominated.
2026-07-26  [banner]  weekStarting / stage 1 / reflective / bn.start.21
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > Early, before anything settles.
2026-07-26  [pulse]  SILENT (NO_RULE_QUALIFIED)
2026-07-26  [report headline]  personalBest / stage 1 / plain / hd.best.30
  rule:  report.headline.personalBest
  fired: no earlier week strictly beats this one, this week reaches the personal best, there is more than one week to be best of, there are completions, so no count renders as zero
  > Your ceiling, this week.
2026-07-26  [report observation]  personalBest / stage 1 / editorial / ob.best.l10
  rule:  report.observation.personalBest
  fired: no earlier week strictly beats this one, this week reaches the personal best, there is more than one week to be best of, there are completions, so no count renders as zero
  > Nothing here needs to happen again.
2026-07-26  [report observation]  areaBalance / stage 1 / editorial / ob.bal.l08
  rule:  report.observation.areaBalance
  fired: three or more areas had activity, no area holds half the week, the window holds at least 6 events, so a share is describing something real
  > A week can be wide or it can be deep.
2026-07-26  [report observation]  intakeVsOutput / stage 2 / neutral_agent / ob.flow.s2.l29
  rule:  report.observation.intakeVsOutput.s2
  fired: intake exceeds output by five or more, there are additions to count
  > A collecting week.
2026-07-26  [report observation]  queuePressure / stage 1 / neutral_agent / ob.qp.l28
  rule:  report.observation.queuePressure
  fired: at least one area's queue grew, the queues hold three or more things to count
  facts: m=192 [rollup.queueTotalAtStart], n=199 [rollup.queueTotal]
  > 199 things are in the queues now, 192 were there when the week opened, and at least one queue grew inside it.
2026-07-26  [report pattern]  reportedVsActual / stage 1 / plain / pt.rva.09
  rule:  report.pattern.reportedVsActual
  fired: there are several stored answers to compare against, one family has been answered about more than twice, there are at least three weeks of snapshots, without which no pattern may fire
  > One subject, answered more than twice.

2026-07-27  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.21
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Across, rather than down.
2026-07-27  [banner]  weekMixed / stage 1 / reflective / bn.mixed.26
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Most of it in one area, so far.
2026-07-27  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-07-28  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.22
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Two weeks that went several ways.
2026-07-28  [banner]  weekMixed / stage 1 / reflective / bn.mixed.25
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Uneven going.
2026-07-28  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-07-29  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.28
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight that did not settle in one place.
2026-07-29  [banner]  weekBuilding / stage 1 / reflective / bn.build.34
  rule:  banner.weekBuilding
  fired: three to five days into the week, completions are accumulating
  > Some of the week spent, some of it left.
2026-07-29  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-07-30  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.22
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Two weeks that went several ways.
2026-07-30  [banner]  weekBuilding / stage 1 / reflective / bn.build.09
  rule:  banner.weekBuilding
  fired: three to five days into the week, completions are accumulating
  > A week under way.
2026-07-30  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-07-31  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.18
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Level, more or less.
2026-07-31  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-07-31  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
```

### August 2026

31 days on screen, 0 Pulses spoken, 31 Pulse days silent, 84 sentences in all.

```text
2026-08-01  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.30
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Spread, without any one place holding it.
2026-08-01  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-08-01  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-08-02  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.27
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight that went in several directions.
2026-08-02  [banner]  weekStarting / stage 1 / reflective / bn.start.16
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > A week still mostly ahead.
2026-08-02  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-08-02  [report headline]  personalBest / stage 1 / plain / hd.best.18
  rule:  report.headline.personalBest
  fired: no earlier week strictly beats this one, this week reaches the personal best, there is more than one week to be best of, there are completions, so no count renders as zero
  facts: n=7 [window.completions]
  > The first time at 7.
2026-08-02  [report observation]  personalBest / stage 1 / editorial / ob.best.l19
  rule:  report.observation.personalBest
  fired: no earlier week strictly beats this one, this week reaches the personal best, there is more than one week to be best of, there are completions, so no count renders as zero
  > A week can be the best one and still be an ordinary week from the inside.
2026-08-02  [report observation]  areaBalance / stage 1 / editorial / ob.bal.l12
  rule:  report.observation.areaBalance
  fired: three or more areas had activity, no area holds half the week, the window holds at least 6 events, so a share is describing something real
  > The week had a shape without a subject.
2026-08-02  [report observation]  intakeVsOutput / stage 2 / neutral_agent / ob.flow.s2.l13
  rule:  report.observation.intakeVsOutput.s2
  fired: intake exceeds output by five or more, there are additions to count
  facts: k=7 [window.intakeGap]
  > Arrivals outpaced departures by 7.
2026-08-02  [report observation]  persistentItem / stage 1 / observational / ob.pers.l37
  rule:  report.observation.persistentItem.low
  fired: the item has been active seven to thirteen days, the area holding the item has at least 1 events in the window
  facts: ageDays=12 [item.itemAgeDays:itm-202], areaName=Home, itemTitle=Book the dentist
  > Book the dentist has been the answer to what is next in Home for 12 days.
2026-08-02  [report pattern]  consistentRhythm / stage 1 / plain / pt.rhy.31
  rule:  report.pattern.consistentRhythm
  fired: four weeks inside a narrow band, there are four weeks to see it across, the band is a rhythm rather than a run of empty weeks
  > No week in the month broke away.

2026-08-03  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.13
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Wide, across two weeks.
2026-08-03  [banner]  weekMixed / stage 1 / reflective / bn.mixed.26
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Most of it in one area, so far.
2026-08-03  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-08-04  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.10
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Broad rather than deep.
2026-08-04  [banner]  weekBuilding / stage 1 / reflective / bn.build.16
  rule:  banner.weekBuilding
  fired: three to five days into the week, completions are accumulating
  > A week no longer new.
2026-08-04  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-08-05  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.20
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > No single thing has held it.
2026-08-05  [banner]  weekBuilding / stage 1 / reflective / bn.build.27
  rule:  banner.weekBuilding
  fired: three to five days into the week, completions are accumulating
  > A week with a few days already spent.
2026-08-05  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-08-06  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.12
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Nothing has dominated.
2026-08-06  [banner]  weekBuilding / stage 1 / reflective / bn.build.33
  rule:  banner.weekBuilding
  fired: three to five days into the week, completions are accumulating
  > A week with days still in front of it.
2026-08-06  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-08-07  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.16
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > No one place took it all.
2026-08-07  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-08-07  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-08-08  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.18
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Level, more or less.
2026-08-08  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-08-08  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-08-09  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.27
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight that went in several directions.
2026-08-09  [banner]  weekStarting / stage 1 / reflective / bn.start.21
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > Early, before anything settles.
2026-08-09  [pulse]  SILENT (NO_RULE_QUALIFIED)
2026-08-09  [report headline]  personalBest / stage 1 / plain / hd.best.47
  rule:  report.headline.personalBest
  fired: no earlier week strictly beats this one, this week reaches the personal best, there is more than one week to be best of, there are completions, so no count renders as zero
  > Your record, matched or beaten.
2026-08-09  [report observation]  personalBest / stage 1 / editorial / ob.best.l15
  rule:  report.observation.personalBest
  fired: no earlier week strictly beats this one, this week reaches the personal best, there is more than one week to be best of, there are completions, so no count renders as zero
  > Something in this week worked. The record can say that it did without saying what it was.
2026-08-09  [report observation]  areaBalance / stage 1 / editorial / ob.bal.l16
  rule:  report.observation.areaBalance
  fired: three or more areas had activity, no area holds half the week, the window holds at least 6 events, so a share is describing something real
  > Spread is not the same as balance. This week was spread.
2026-08-09  [report observation]  intakeVsOutput / stage 2 / neutral_agent / ob.flow.s2.l34
  rule:  report.observation.intakeVsOutput.s2
  fired: intake exceeds output by five or more, there are additions to count
  facts: k=7 [rollup.queueGrowth]
  > The queues hold 7 more things than they held on the first day of the week.
2026-08-09  [report observation]  persistentItem / stage 1 / observational / ob.pers.l41
  rule:  report.observation.persistentItem.low
  fired: the item has been active seven to thirteen days, the area holding the item has at least 1 events in the window
  facts: areaName=Personal, itemTitle=Draft the quarterly summary
  > The rest of Personal moved this week and Draft the quarterly summary stayed still.
2026-08-09  [report pattern]  reportedVsActual / stage 1 / plain / pt.rva.57
  rule:  report.pattern.reportedVsActual
  fired: there are several stored answers to compare against, one family has been answered about more than twice, there are at least three weeks of snapshots, without which no pattern may fire
  > The app asked, you answered, and then the app asked again, and that has happened three times about one subject.

2026-08-10  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.17
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Room for more than one thing.
2026-08-10  [banner]  weekMixed / stage 1 / reflective / bn.mixed.10
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Lopsided, for now.
2026-08-10  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-08-11  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.30
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Spread, without any one place holding it.
2026-08-11  [banner]  weekMixed / stage 1 / reflective / bn.mixed.11
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Weighted rather than spread.
2026-08-11  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-08-12  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.27
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight that went in several directions.
2026-08-12  [banner]  weekBuilding / stage 1 / reflective / bn.build.25
  rule:  banner.weekBuilding
  fired: three to five days into the week, completions are accumulating
  > In motion, midweek.
2026-08-12  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-08-13  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.14
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight in several places.
2026-08-13  [banner]  weekBuilding / stage 1 / reflective / bn.build.33
  rule:  banner.weekBuilding
  fired: three to five days into the week, completions are accumulating
  > A week with days still in front of it.
2026-08-13  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-08-14  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.28
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight that did not settle in one place.
2026-08-14  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-08-14  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-08-15  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.33
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight that went around rather than down.
2026-08-15  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-08-15  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-08-16  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.12
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Nothing has dominated.
2026-08-16  [banner]  weekStarting / stage 1 / reflective / bn.start.23
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > Nothing here has had time to become a shape.
2026-08-16  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-08-16  [report headline]  personalBest / stage 1 / plain / hd.best.45
  rule:  report.headline.personalBest
  fired: no earlier week strictly beats this one, this week reaches the personal best, there is more than one week to be best of, there are completions, so no count renders as zero
  > No week looks better.
2026-08-16  [report observation]  personalBest / stage 1 / editorial / ob.best.l04
  rule:  report.observation.personalBest
  fired: no earlier week strictly beats this one, this week reaches the personal best, there is more than one week to be best of, there are completions, so no count renders as zero
  > Nothing in eleven weeks of history looks like this one.
2026-08-16  [report observation]  persistentItem / stage 1 / neutral_agent / ob.pers.l12
  rule:  report.observation.persistentItem.high
  fired: the item has been active fourteen days or more, the area holding the item has at least 1 events in the window, there is a median completion time to compare against
  facts: ageDays=16 [item.itemAgeDays:itm-222], areaName=Personal, itemTitle=Draft the quarterly summary
  > Nothing else has reached the front of Personal in 2 weeks, and Draft the quarterly summary is still there.
2026-08-16  [report observation]  areaBalance / stage 1 / editorial / ob.bal.l04
  rule:  report.observation.areaBalance
  fired: three or more areas had activity, no area holds half the week, the window holds at least 6 events, so a share is describing something real
  > No single area owned this week.
2026-08-16  [report observation]  intakeVsOutput / stage 2 / neutral_agent / ob.flow.s2.l14
  rule:  report.observation.intakeVsOutput.s2
  fired: intake exceeds output by five or more, there are additions to count
  > Nothing added this week has left yet.
2026-08-16  [report pattern]  consistentRhythm / stage 1 / plain / pt.rhy.30
  rule:  report.pattern.consistentRhythm
  fired: four weeks inside a narrow band, there are four weeks to see it across, the band is a rhythm rather than a run of empty weeks
  > A month of similar weeks.

2026-08-17  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.19
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Spread out, over fourteen days.
2026-08-17  [banner]  weekMixed / stage 1 / reflective / bn.mixed.25
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Uneven going.
2026-08-17  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-08-18  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.15
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Breadth, more than depth.
2026-08-18  [banner]  weekMixed / stage 1 / reflective / bn.mixed.17
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Spread unevenly, so far.
2026-08-18  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-08-19  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.04
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A wide fortnight.
2026-08-19  [banner]  weekMixed / stage 1 / reflective / bn.mixed.19
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week gathered mostly in one place.
2026-08-19  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-08-20  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.26
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Several places, none of them all of it.
2026-08-20  [banner]  weekBuilding / stage 1 / reflective / bn.build.25
  rule:  banner.weekBuilding
  fired: three to five days into the week, completions are accumulating
  > In motion, midweek.
2026-08-20  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-08-21  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.29
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Several things at once, for two weeks.
2026-08-21  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-08-21  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-08-22  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.18
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Level, more or less.
2026-08-22  [banner]  weekMixed / stage 1 / reflective / bn.mixed.12
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week that leans one way.
2026-08-22  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-08-23  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.07
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Evenly spread, across two weeks.
2026-08-23  [banner]  weekStarting / stage 1 / reflective / bn.start.13
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > The opening of a week.
2026-08-23  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-08-23  [report headline]  personalBest / stage 1 / plain / hd.best.24
  rule:  report.headline.personalBest
  fired: no earlier week strictly beats this one, this week reaches the personal best, there is more than one week to be best of, there are completions, so no count renders as zero
  > Nothing since you started beats it.
2026-08-23  [report observation]  personalBest / stage 1 / editorial / ob.best.l11
  rule:  report.observation.personalBest
  fired: no earlier week strictly beats this one, this week reaches the personal best, there is more than one week to be best of, there are completions, so no count renders as zero
  facts: n=7 [window.completions]
  > The number is 7. The reading of it is not the app's to make.
2026-08-23  [report observation]  areaBalance / stage 1 / editorial / ob.bal.l11
  rule:  report.observation.areaBalance
  fired: three or more areas had activity, no area holds half the week, the window holds at least 6 events, so a share is describing something real
  > A wide week and a thin week look the same from a count, and nothing here tells them apart.
2026-08-23  [report observation]  intakeVsOutput / stage 2 / neutral_agent / ob.flow.s2.l22
  rule:  report.observation.intakeVsOutput.s2
  fired: intake exceeds output by five or more, there are additions to count
  > Five apart, at least.
2026-08-23  [report observation]  queuePressure / stage 1 / neutral_agent / ob.qp.l21
  rule:  report.observation.queuePressure
  fired: at least one area's queue grew, the queues hold three or more things to count
  facts: areaCount=4 [rollup.areasWithQueue], areaName=Personal
  > Personal holds the longest queue of the 4.
2026-08-23  [report pattern]  reportedVsActual / stage 1 / plain / pt.rva.52
  rule:  report.pattern.reportedVsActual
  fired: there are several stored answers to compare against, one family has been answered about more than twice, there are at least three weeks of snapshots, without which no pattern may fire
  > What you said at the time is stored exactly as you said it, and there are five of those at least.

2026-08-24  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.24
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight that kept moving around.
2026-08-24  [banner]  weekMixed / stage 1 / reflective / bn.mixed.24
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > The weight of the week sits in one place.
2026-08-24  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-08-25  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.20
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > No single thing has held it.
2026-08-25  [banner]  weekMixed / stage 1 / reflective / bn.mixed.13
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week with a center of gravity in it.
2026-08-25  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-08-26  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.12
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Nothing has dominated.
2026-08-26  [banner]  weekBuilding / stage 1 / reflective / bn.build.15
  rule:  banner.weekBuilding
  fired: three to five days into the week, completions are accumulating
  > A week past its start.
2026-08-26  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-08-27  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.04
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A wide fortnight.
2026-08-27  [banner]  weekBuilding / stage 1 / reflective / bn.build.24
  rule:  banner.weekBuilding
  fired: three to five days into the week, completions are accumulating
  > The beginning has passed.
2026-08-27  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-08-28  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.31
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > More than one thing at a time.
2026-08-28  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-08-28  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-08-29  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.26
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Several places, none of them all of it.
2026-08-29  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-08-29  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-08-30  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.13
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Wide, across two weeks.
2026-08-30  [banner]  weekStarting / stage 1 / reflective / bn.start.13
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > The opening of a week.
2026-08-30  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-08-30  [report headline]  personalBest / stage 1 / plain / hd.best.23
  rule:  report.headline.personalBest
  fired: no earlier week strictly beats this one, this week reaches the personal best, there is more than one week to be best of, there are completions, so no count renders as zero
  > This one is the highest.
2026-08-30  [report observation]  personalBest / stage 1 / editorial / ob.best.l12
  rule:  report.observation.personalBest
  fired: no earlier week strictly beats this one, this week reaches the personal best, there is more than one week to be best of, there are completions, so no count renders as zero
  > The record moved. That is all the record does.
2026-08-30  [report observation]  areaBalance / stage 1 / editorial / ob.bal.l17
  rule:  report.observation.areaBalance
  fired: three or more areas had activity, no area holds half the week, the window holds at least 6 events, so a share is describing something real
  facts: areaCount=4 [rollup.areasWithEvents]
  > A week that touches 4 areas and gives none of them half has no single subject.
2026-08-30  [report observation]  intakeVsOutput / stage 2 / neutral_agent / ob.flow.s2.l11
  rule:  report.observation.intakeVsOutput.s2
  fired: intake exceeds output by five or more, there are additions to count
  facts: m=7 [window.completions], n=14 [window.additions]
  > The queues took on 14 and released 7.
2026-08-30  [report observation]  persistentItem / stage 1 / observational / ob.pers.l28
  rule:  report.observation.persistentItem.low
  fired: the item has been active seven to thirteen days, the area holding the item has at least 1 events in the window
  facts: ageDays=8 [item.itemAgeDays:itm-242], itemTitle=Update the resume
  > Update the resume has stayed in place 8 days.
2026-08-30  [report pattern]  consistentRhythm / stage 1 / plain / pt.rhy.15
  rule:  report.pattern.consistentRhythm
  fired: four weeks inside a narrow band, there are four weeks to see it across, the band is a rhythm rather than a run of empty weeks
  > Four weeks of the same size.

2026-08-31  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.17
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Room for more than one thing.
2026-08-31  [banner]  weekMixed / stage 1 / reflective / bn.mixed.15
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Tilted, so far.
2026-08-31  [pulse]  SILENT (NO_RULE_QUALIFIED)
```

### September 2026

30 days on screen, 0 Pulses spoken, 30 Pulse days silent, 78 sentences in all.

```text
2026-09-01  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.12
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Nothing has dominated.
2026-09-01  [banner]  weekMixed / stage 1 / reflective / bn.mixed.73
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week that piled up in one place.
2026-09-01  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-09-02  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.14
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight in several places.
2026-09-02  [banner]  weekMixed / stage 1 / reflective / bn.mixed.23
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week that did not spread evenly.
2026-09-02  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-09-03  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.13
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Wide, across two weeks.
2026-09-03  [banner]  weekBuilding / stage 1 / reflective / bn.build.29
  rule:  banner.weekBuilding
  fired: three to five days into the week, completions are accumulating
  > Some days gone, some things done, the week still open.
2026-09-03  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-09-04  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.19
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Spread out, over fourteen days.
2026-09-04  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-09-04  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-09-05  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.29
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Several things at once, for two weeks.
2026-09-05  [banner]  weekMixed / stage 1 / reflective / bn.mixed.14
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A shape rather than a spread.
2026-09-05  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-09-06  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.07
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Evenly spread, across two weeks.
2026-09-06  [banner]  weekStarting / stage 1 / reflective / bn.start.14
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > Too early for much of a reading.
2026-09-06  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-09-06  [report headline]  personalBest / stage 1 / plain / hd.best.57
  rule:  report.headline.personalBest
  fired: no earlier week strictly beats this one, this week reaches the personal best, there is more than one week to be best of, there are completions, so no count renders as zero
  > A peak.
2026-09-06  [report observation]  personalBest / stage 1 / editorial / ob.best.l18
  rule:  report.observation.personalBest
  fired: no earlier week strictly beats this one, this week reaches the personal best, there is more than one week to be best of, there are completions, so no count renders as zero
  facts: n=7 [window.completions]
  > 7 completions is the largest number this record has held, and a number is all it is.
2026-09-06  [report observation]  persistentItem / stage 1 / neutral_agent / ob.pers.l13
  rule:  report.observation.persistentItem.high
  fired: the item has been active fourteen days or more, the area holding the item has at least 1 events in the window, there is a median completion time to compare against
  facts: itemTitle=Update the resume
  > Update the resume is still at the front.
2026-09-06  [report observation]  areaBalance / stage 1 / editorial / ob.bal.l14
  rule:  report.observation.areaBalance
  fired: three or more areas had activity, no area holds half the week, the window holds at least 6 events, so a share is describing something real
  facts: pct=43 [area.areaShare:personal]
  > 43 percent is the largest share anything took. It is not a majority.
2026-09-06  [report observation]  intakeVsOutput / stage 2 / neutral_agent / ob.flow.s2.l33
  rule:  report.observation.intakeVsOutput.s2
  fired: intake exceeds output by five or more, there are additions to count
  facts: m=7 [window.completions], n=14 [window.additions]
  > The week took in 14 things and released 7, and the difference between those two runs to five or more.
2026-09-06  [report pattern]  reportedVsActual / stage 1 / plain / pt.rva.12
  rule:  report.pattern.reportedVsActual
  fired: there are several stored answers to compare against, one family has been answered about more than twice, there are at least three weeks of snapshots, without which no pattern may fire
  > A repeated question.

2026-09-07  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.26
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Several places, none of them all of it.
2026-09-07  [banner]  weekMixed / stage 1 / reflective / bn.mixed.18
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Unequal, with the week still going.
2026-09-07  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-09-08  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.27
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight that went in several directions.
2026-09-08  [banner]  weekMixed / stage 1 / reflective / bn.mixed.09
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week with a tilt to it.
2026-09-08  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-09-09  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.14
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight in several places.
2026-09-09  [banner]  weekMixed / stage 1 / reflective / bn.mixed.13
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week with a center of gravity in it.
2026-09-09  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-09-10  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.24
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight that kept moving around.
2026-09-10  [banner]  weekBuilding / stage 1 / reflective / bn.build.29
  rule:  banner.weekBuilding
  fired: three to five days into the week, completions are accumulating
  > Some days gone, some things done, the week still open.
2026-09-10  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-09-11  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.31
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > More than one thing at a time.
2026-09-11  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-09-11  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-09-12  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.23
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Nothing has held more than half.
2026-09-12  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-09-12  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-09-13  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.17
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Room for more than one thing.
2026-09-13  [banner]  weekStarting / stage 1 / reflective / bn.start.15
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > Nothing settled yet.
2026-09-13  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-09-13  [report headline]  personalBest / stage 1 / plain / hd.best.07
  rule:  report.headline.personalBest
  fired: no earlier week strictly beats this one, this week reaches the personal best, there is more than one week to be best of, there are completions, so no count renders as zero
  facts: n=7 [window.completions]
  > 7 completions. A first.
2026-09-13  [report observation]  personalBest / stage 1 / editorial / ob.best.l13
  rule:  report.observation.personalBest
  fired: no earlier week strictly beats this one, this week reaches the personal best, there is more than one week to be best of, there are completions, so no count renders as zero
  > A high week is a fact.
2026-09-13  [report observation]  persistentItem / stage 1 / neutral_agent / ob.pers.l23
  rule:  report.observation.persistentItem.high
  fired: the item has been active fourteen days or more, the area holding the item has at least 1 events in the window, there is a median completion time to compare against
  facts: areaName=Home, itemTitle=Update the resume, n=65 [item.itemQueueBehind:itm-242]
  > Update the resume has 65 things waiting behind it while Home moved.
2026-09-13  [report observation]  areaBalance / stage 1 / editorial / ob.bal.l07
  rule:  report.observation.areaBalance
  fired: three or more areas had activity, no area holds half the week, the window holds at least 6 events, so a share is describing something real
  > The week had no owner.
2026-09-13  [report observation]  intakeVsOutput / stage 2 / neutral_agent / ob.flow.s2.l19
  rule:  report.observation.intakeVsOutput.s2
  fired: intake exceeds output by five or more, there are additions to count
  facts: k=7 [rollup.queueGrowth]
  > The queues are carrying 7 more than they were.
2026-09-13  [report pattern]  consistentRhythm / stage 1 / plain / pt.rhy.51
  rule:  report.pattern.consistentRhythm
  fired: four weeks inside a narrow band, there are four weeks to see it across, the band is a rhythm rather than a run of empty weeks
  > There is no week in the last four that the other three do not resemble.

2026-09-14  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.14
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight in several places.
2026-09-14  [banner]  weekMixed / stage 1 / reflective / bn.mixed.19
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week gathered mostly in one place.
2026-09-14  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-09-15  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.29
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Several things at once, for two weeks.
2026-09-15  [banner]  weekMixed / stage 1 / reflective / bn.mixed.24
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > The weight of the week sits in one place.
2026-09-15  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-09-16  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.13
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Wide, across two weeks.
2026-09-16  [banner]  weekMixed / stage 1 / reflective / bn.mixed.25
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Uneven going.
2026-09-16  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-09-17  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.25
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Two weeks across several areas.
2026-09-17  [banner]  weekBuilding / stage 1 / reflective / bn.build.26
  rule:  banner.weekBuilding
  fired: three to five days into the week, completions are accumulating
  > A week with something already in it.
2026-09-17  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-09-18  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.24
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight that kept moving around.
2026-09-18  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-09-18  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-09-19  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.04
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A wide fortnight.
2026-09-19  [banner]  weekMixed / stage 1 / reflective / bn.mixed.15
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Tilted, so far.
2026-09-19  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-09-20  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.32
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight with its weight spread out.
2026-09-20  [banner]  weekStarting / stage 1 / reflective / bn.start.13
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > The opening of a week.
2026-09-20  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-09-20  [report headline]  personalBest / stage 1 / plain / hd.best.15
  rule:  report.headline.personalBest
  fired: no earlier week strictly beats this one, this week reaches the personal best, there is more than one week to be best of, there are completions, so no count renders as zero
  facts: n=7 [window.completions]
  > 7 done, a high.
2026-09-20  [report observation]  personalBest / stage 1 / editorial / ob.best.l21
  rule:  report.observation.personalBest
  fired: no earlier week strictly beats this one, this week reaches the personal best, there is more than one week to be best of, there are completions, so no count renders as zero
  facts: m=7 [history.personalBestCompletions], sinceRef=2026-09-06
  > The best earlier week was 7, in September, and this one is not below it.
2026-09-20  [report observation]  areaBalance / stage 1 / editorial / ob.bal.l15
  rule:  report.observation.areaBalance
  fired: three or more areas had activity, no area holds half the week, the window holds at least 6 events, so a share is describing something real
  facts: areaCount=4 [rollup.areasWithEvents]
  > Attention went to 4 places and settled in none of them.
2026-09-20  [report observation]  intakeVsOutput / stage 2 / neutral_agent / ob.flow.s2.l24
  rule:  report.observation.intakeVsOutput.s2
  fired: intake exceeds output by five or more, there are additions to count
  > Intake ran ahead of output.
2026-09-20  [report observation]  persistentItem / stage 1 / observational / ob.pers.l02
  rule:  report.observation.persistentItem.low
  fired: the item has been active seven to thirteen days, the area holding the item has at least 1 events in the window
  facts: ageDays=11 [item.itemAgeDays:itm-262], itemTitle=Schedule the eye test
  > Nothing has moved past Schedule the eye test in 11 days.
2026-09-20  [report pattern]  reportedVsActual / stage 1 / plain / pt.rva.43
  rule:  report.pattern.reportedVsActual
  fired: there are several stored answers to compare against, one family has been answered about more than twice, there are at least three weeks of snapshots, without which no pattern may fire
  > One kind of moment has come around three times.

2026-09-21  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.07
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Evenly spread, across two weeks.
2026-09-21  [banner]  weekMixed / stage 1 / reflective / bn.mixed.12
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week that leans one way.
2026-09-21  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-09-22  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.17
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Room for more than one thing.
2026-09-22  [banner]  weekMixed / stage 1 / reflective / bn.mixed.09
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week with a tilt to it.
2026-09-22  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-09-23  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.21
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Across, rather than down.
2026-09-23  [banner]  weekMixed / stage 1 / reflective / bn.mixed.14
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A shape rather than a spread.
2026-09-23  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-09-24  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.22
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Two weeks that went several ways.
2026-09-24  [banner]  weekBuilding / stage 1 / reflective / bn.build.17
  rule:  banner.weekBuilding
  fired: three to five days into the week, completions are accumulating
  > A week that has begun to fill.
2026-09-24  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-09-25  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.25
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Two weeks across several areas.
2026-09-25  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-09-25  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-09-26  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.17
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Room for more than one thing.
2026-09-26  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-09-26  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-09-27  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.10
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Broad rather than deep.
2026-09-27  [banner]  weekStarting / stage 1 / reflective / bn.start.11
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > Barely begun.
2026-09-27  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-09-27  [report headline]  personalBest / stage 1 / plain / hd.best.25
  rule:  report.headline.personalBest
  fired: no earlier week strictly beats this one, this week reaches the personal best, there is more than one week to be best of, there are completions, so no count renders as zero
  facts: n=7 [window.completions]
  > 7 completions, unmatched.
2026-09-27  [report observation]  personalBest / stage 1 / editorial / ob.best.l09
  rule:  report.observation.personalBest
  fired: no earlier week strictly beats this one, this week reaches the personal best, there is more than one week to be best of, there are completions, so no count renders as zero
  > A record is a fact about the past. Nothing here turns it into a target for the week ahead.
2026-09-27  [report observation]  persistentItem / stage 1 / neutral_agent / ob.pers.l09
  rule:  report.observation.persistentItem.high
  fired: the item has been active fourteen days or more, the area holding the item has at least 1 events in the window, there is a median completion time to compare against
  facts: itemTitle=Schedule the eye test
  > Schedule the eye test is where it was.
2026-09-27  [report observation]  areaBalance / stage 1 / editorial / ob.bal.l09
  rule:  report.observation.areaBalance
  fired: three or more areas had activity, no area holds half the week, the window holds at least 6 events, so a share is describing something real
  facts: areaCount=4 [rollup.areasWithEvents]
  > 4 areas moved and none of them ran the week.
2026-09-27  [report observation]  intakeVsOutput / stage 2 / neutral_agent / ob.flow.s2.l32
  rule:  report.observation.intakeVsOutput.s2
  fired: intake exceeds output by five or more, there are additions to count
  facts: k=7 [rollup.queueGrowth], m=7 [window.completions], n=14 [window.additions]
  > 14 things came in this week, 7 went out, and the queues ended 7 longer than they opened.
2026-09-27  [report pattern]  consistentRhythm / stage 1 / plain / pt.rhy.27
  rule:  report.pattern.consistentRhythm
  fired: four weeks inside a narrow band, there are four weeks to see it across, the band is a rhythm rather than a run of empty weeks
  > A month that reads as one thing.

2026-09-28  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.14
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight in several places.
2026-09-28  [banner]  weekMixed / stage 1 / reflective / bn.mixed.22
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Some places busier than others.
2026-09-28  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-09-29  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.33
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight that went around rather than down.
2026-09-29  [banner]  weekBuilding / stage 1 / reflective / bn.build.26
  rule:  banner.weekBuilding
  fired: three to five days into the week, completions are accumulating
  > A week with something already in it.
2026-09-29  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-09-30  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.25
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Two weeks across several areas.
2026-09-30  [banner]  weekBuilding / stage 1 / reflective / bn.build.17
  rule:  banner.weekBuilding
  fired: three to five days into the week, completions are accumulating
  > A week that has begun to fill.
2026-09-30  [pulse]  SILENT (NO_RULE_QUALIFIED)
```

### October 2026

31 days on screen, 0 Pulses spoken, 31 Pulse days silent, 76 sentences in all.

```text
2026-10-01  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.17
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Room for more than one thing.
2026-10-01  [banner]  weekBuilding / stage 1 / reflective / bn.build.26
  rule:  banner.weekBuilding
  fired: three to five days into the week, completions are accumulating
  > A week with something already in it.
2026-10-01  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-10-02  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.14
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight in several places.
2026-10-02  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-10-02  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-10-03  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.16
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > No one place took it all.
2026-10-03  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-10-03  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-10-04  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.23
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Nothing has held more than half.
2026-10-04  [banner]  weekStarting / stage 1 / reflective / bn.start.17
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > The first days of it.
2026-10-04  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-10-04  [report headline]  personalBest / stage 1 / plain / hd.best.20
  rule:  report.headline.personalBest
  fired: no earlier week strictly beats this one, this week reaches the personal best, there is more than one week to be best of, there are completions, so no count renders as zero
  > No earlier week did more.
2026-10-04  [report observation]  personalBest / stage 1 / editorial / ob.best.l16
  rule:  report.observation.personalBest
  fired: no earlier week strictly beats this one, this week reaches the personal best, there is more than one week to be best of, there are completions, so no count renders as zero
  facts: m=7 [history.personalBestCompletions]
  > The previous best was 7. This week is not less than that.
2026-10-04  [report observation]  areaBalance / stage 1 / editorial / ob.bal.l10
  rule:  report.observation.areaBalance
  fired: three or more areas had activity, no area holds half the week, the window holds at least 6 events, so a share is describing something real
  > Nothing here was the main thing.
2026-10-04  [report observation]  intakeVsOutput / stage 2 / neutral_agent / ob.flow.s2.l28
  rule:  report.observation.intakeVsOutput.s2
  fired: intake exceeds output by five or more, there are additions to count
  > The list is longer than it was.
2026-10-04  [report observation]  queuePressure / stage 1 / neutral_agent / ob.qp.l13
  rule:  report.observation.queuePressure
  fired: at least one area's queue grew, the queues hold three or more things to count
  facts: areaCount=4 [rollup.areasWithQueue], n=269 [rollup.queueTotal]
  > 269 waiting, across 4 areas.
2026-10-04  [report pattern]  reportedVsActual / stage 1 / plain / pt.rva.22
  rule:  report.pattern.reportedVsActual
  fired: there are several stored answers to compare against, one family has been answered about more than twice, there are at least three weeks of snapshots, without which no pattern may fire
  > Three answers, one subject.

2026-10-05  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.07
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Evenly spread, across two weeks.
2026-10-05  [banner]  weekMixed / stage 1 / reflective / bn.mixed.19
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week gathered mostly in one place.
2026-10-05  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-10-06  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.21
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Across, rather than down.
2026-10-06  [banner]  weekMixed / stage 1 / reflective / bn.mixed.20
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Concentrated, with the rest thinner.
2026-10-06  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-10-07  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.14
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight in several places.
2026-10-07  [banner]  weekMixed / stage 1 / reflective / bn.mixed.22
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Some places busier than others.
2026-10-07  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-10-08  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.07
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Evenly spread, across two weeks.
2026-10-08  [banner]  weekBuilding / stage 1 / reflective / bn.build.21
  rule:  banner.weekBuilding
  fired: three to five days into the week, completions are accumulating
  > Between the start of a week and the end of it.
2026-10-08  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-10-09  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.24
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight that kept moving around.
2026-10-09  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-10-09  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-10-10  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.15
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Breadth, more than depth.
2026-10-10  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-10-10  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-10-11  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.30
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Spread, without any one place holding it.
2026-10-11  [banner]  weekStarting / stage 1 / reflective / bn.start.16
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > A week still mostly ahead.
2026-10-11  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-10-11  [report headline]  personalBest / stage 1 / plain / hd.best.31
  rule:  report.headline.personalBest
  fired: no earlier week strictly beats this one, this week reaches the personal best, there is more than one week to be best of, there are completions, so no count renders as zero
  > A high point.
2026-10-11  [report observation]  personalBest / stage 1 / editorial / ob.best.l14
  rule:  report.observation.personalBest
  fired: no earlier week strictly beats this one, this week reaches the personal best, there is more than one week to be best of, there are completions, so no count renders as zero
  facts: n=7 [window.completions]
  > 7 is where the record stands now.
2026-10-11  [report observation]  areaBalance / stage 1 / editorial / ob.bal.l04
  rule:  report.observation.areaBalance
  fired: three or more areas had activity, no area holds half the week, the window holds at least 6 events, so a share is describing something real
  > No single area owned this week.
2026-10-11  [report observation]  intakeVsOutput / stage 2 / neutral_agent / ob.flow.s2.l35
  rule:  report.observation.intakeVsOutput.s2
  fired: intake exceeds output by five or more, there are additions to count
  > Five or more things separate what arrived this week from what left it, and seven days is where both numbers came from.
2026-10-11  [report observation]  persistentItem / stage 1 / observational / ob.pers.l29
  rule:  report.observation.persistentItem.low
  fired: the item has been active seven to thirteen days, the area holding the item has at least 1 events in the window
  facts: areaName=Home
  > The front of Home is unchanged.
2026-10-11  [report pattern]  consistentRhythm / stage 1 / plain / pt.rhy.14
  rule:  report.pattern.consistentRhythm
  fired: four weeks inside a narrow band, there are four weeks to see it across, the band is a rhythm rather than a run of empty weeks
  > A month at one level.

2026-10-12  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.33
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight that went around rather than down.
2026-10-12  [banner]  weekMixed / stage 1 / reflective / bn.mixed.15
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Tilted, so far.
2026-10-12  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-10-13  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.30
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Spread, without any one place holding it.
2026-10-13  [banner]  weekBuilding / stage 1 / reflective / bn.build.30
  rule:  banner.weekBuilding
  fired: three to five days into the week, completions are accumulating
  > Far enough in that something has happened.
2026-10-13  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-10-14  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.26
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Several places, none of them all of it.
2026-10-14  [banner]  weekBuilding / stage 1 / reflective / bn.build.15
  rule:  banner.weekBuilding
  fired: three to five days into the week, completions are accumulating
  > A week past its start.
2026-10-14  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-10-15  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.25
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Two weeks across several areas.
2026-10-15  [banner]  weekBuilding / stage 1 / reflective / bn.build.09
  rule:  banner.weekBuilding
  fired: three to five days into the week, completions are accumulating
  > A week under way.
2026-10-15  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-10-16  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.33
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight that went around rather than down.
2026-10-16  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-10-16  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-10-17  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.30
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Spread, without any one place holding it.
2026-10-17  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-10-17  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-10-18  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.13
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Wide, across two weeks.
2026-10-18  [banner]  weekStarting / stage 1 / reflective / bn.start.18
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > A beginning.
2026-10-18  [pulse]  SILENT (NO_RULE_QUALIFIED)
2026-10-18  [report headline]  personalBest / stage 1 / plain / hd.best.22
  rule:  report.headline.personalBest
  fired: no earlier week strictly beats this one, this week reaches the personal best, there is more than one week to be best of, there are completions, so no count renders as zero
  > The high mark.
2026-10-18  [report observation]  personalBest / stage 1 / editorial / ob.best.l20
  rule:  report.observation.personalBest
  fired: no earlier week strictly beats this one, this week reaches the personal best, there is more than one week to be best of, there are completions, so no count renders as zero
  facts: n=7 [window.completions], sinceRef=2026-10-04
  > Nothing in the history reads higher than 7, and the history goes back to before October.
2026-10-18  [report observation]  persistentItem / stage 1 / neutral_agent / ob.pers.l22
  rule:  report.observation.persistentItem.high
  fired: the item has been active fourteen days or more, the area holding the item has at least 1 events in the window, there is a median completion time to compare against
  facts: ageDays=17 [item.itemAgeDays:itm-282], areaName=Home
  > The same item has been first in Home for 2 weeks.
2026-10-18  [report observation]  areaBalance / stage 1 / editorial / ob.bal.l07
  rule:  report.observation.areaBalance
  fired: three or more areas had activity, no area holds half the week, the window holds at least 6 events, so a share is describing something real
  > The week had no owner.
2026-10-18  [report observation]  intakeVsOutput / stage 2 / neutral_agent / ob.flow.s2.l17
  rule:  report.observation.intakeVsOutput.s2
  fired: intake exceeds output by five or more, there are additions to count
  > More came in than went out, by five or more.
2026-10-18  [report pattern]  reportedVsActual / stage 1 / plain / pt.rva.54
  rule:  report.pattern.reportedVsActual
  fired: there are several stored answers to compare against, one family has been answered about more than twice, there are at least three weeks of snapshots, without which no pattern may fire
  > An answer is a reading taken at a moment, and this app now holds five of yours or more.

2026-10-19  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.27
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight that went in several directions.
2026-10-19  [banner]  weekMixed / stage 1 / reflective / bn.mixed.14
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A shape rather than a spread.
2026-10-19  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-10-20  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.10
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Broad rather than deep.
2026-10-20  [banner]  weekMixed / stage 1 / reflective / bn.mixed.26
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Most of it in one area, so far.
2026-10-20  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-10-21  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.23
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Nothing has held more than half.
2026-10-21  [banner]  weekBuilding / stage 1 / reflective / bn.build.09
  rule:  banner.weekBuilding
  fired: three to five days into the week, completions are accumulating
  > A week under way.
2026-10-21  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-10-22  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.27
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight that went in several directions.
2026-10-22  [banner]  weekBuilding / stage 1 / reflective / bn.build.34
  rule:  banner.weekBuilding
  fired: three to five days into the week, completions are accumulating
  > Some of the week spent, some of it left.
2026-10-22  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-10-23  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.10
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Broad rather than deep.
2026-10-23  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-10-23  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-10-24  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.20
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > No single thing has held it.
2026-10-24  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-10-24  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-10-25  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.23
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Nothing has held more than half.
2026-10-25  [banner]  weekStarting / stage 1 / reflective / bn.start.22
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > There is more week ahead than behind.
2026-10-25  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-10-25  [report headline]  personalBest / stage 1 / plain / hd.best.59
  rule:  report.headline.personalBest
  fired: no earlier week strictly beats this one, this week reaches the personal best, there is more than one week to be best of, there are completions, so no count renders as zero
  facts: n=7 [window.completions]
  > 7 completions, none higher.
2026-10-25  [report observation]  personalBest / stage 1 / editorial / ob.best.l19
  rule:  report.observation.personalBest
  fired: no earlier week strictly beats this one, this week reaches the personal best, there is more than one week to be best of, there are completions, so no count renders as zero
  > A week can be the best one and still be an ordinary week from the inside.
2026-10-25  [report observation]  areaBalance / stage 1 / editorial / ob.bal.l12
  rule:  report.observation.areaBalance
  fired: three or more areas had activity, no area holds half the week, the window holds at least 6 events, so a share is describing something real
  > The week had a shape without a subject.
2026-10-25  [report observation]  intakeVsOutput / stage 2 / neutral_agent / ob.flow.s2.l13
  rule:  report.observation.intakeVsOutput.s2
  fired: intake exceeds output by five or more, there are additions to count
  facts: k=7 [window.intakeGap]
  > Arrivals outpaced departures by 7.
2026-10-25  [report observation]  queuePressure / stage 1 / neutral_agent / ob.qp.l24
  rule:  report.observation.queuePressure
  fired: at least one area's queue grew, the queues hold three or more things to count
  facts: n=290 [rollup.queueTotal]
  > The queues came out of the week holding 290 things.
2026-10-25  [report pattern]  consistentRhythm / stage 1 / plain / pt.rhy.35
  rule:  report.pattern.consistentRhythm
  fired: four weeks inside a narrow band, there are four weeks to see it across, the band is a rhythm rather than a run of empty weeks
  > The last four weeks came in level.

2026-10-26  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.19
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Spread out, over fourteen days.
2026-10-26  [banner]  weekMixed / stage 1 / reflective / bn.mixed.12
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week that leans one way.
2026-10-26  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-10-27  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.21
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Across, rather than down.
2026-10-27  [banner]  weekBuilding / stage 1 / reflective / bn.build.09
  rule:  banner.weekBuilding
  fired: three to five days into the week, completions are accumulating
  > A week under way.
2026-10-27  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-10-28  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.30
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Spread, without any one place holding it.
2026-10-28  [banner]  weekBuilding / stage 1 / reflective / bn.build.26
  rule:  banner.weekBuilding
  fired: three to five days into the week, completions are accumulating
  > A week with something already in it.
2026-10-28  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-10-29  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.23
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Nothing has held more than half.
2026-10-29  [banner]  weekBuilding / stage 1 / reflective / bn.build.09
  rule:  banner.weekBuilding
  fired: three to five days into the week, completions are accumulating
  > A week under way.
2026-10-29  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-10-30  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.16
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > No one place took it all.
2026-10-30  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-10-30  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-10-31  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.10
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Broad rather than deep.
2026-10-31  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-10-31  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
```

### November 2026

30 days on screen, 0 Pulses spoken, 30 Pulse days silent, 83 sentences in all.

```text
2026-11-01  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.18
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Level, more or less.
2026-11-01  [banner]  weekStarting / stage 1 / reflective / bn.start.24
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > The shape of this week is still ahead.
2026-11-01  [pulse]  SILENT (NO_RULE_QUALIFIED)
2026-11-01  [report headline]  personalBest / stage 1 / plain / hd.best.13
  rule:  report.headline.personalBest
  fired: no earlier week strictly beats this one, this week reaches the personal best, there is more than one week to be best of, there are completions, so no count renders as zero
  > No week has topped this.
2026-11-01  [report observation]  personalBest / stage 1 / editorial / ob.best.l17
  rule:  report.observation.personalBest
  fired: no earlier week strictly beats this one, this week reaches the personal best, there is more than one week to be best of, there are completions, so no count renders as zero
  > A best week says what happened and nothing about what comes next.
2026-11-01  [report observation]  areaBalance / stage 1 / editorial / ob.bal.l11
  rule:  report.observation.areaBalance
  fired: three or more areas had activity, no area holds half the week, the window holds at least 6 events, so a share is describing something real
  > A wide week and a thin week look the same from a count, and nothing here tells them apart.
2026-11-01  [report observation]  intakeVsOutput / stage 2 / neutral_agent / ob.flow.s2.l10
  rule:  report.observation.intakeVsOutput.s2
  fired: intake exceeds output by five or more, there are additions to count
  facts: m=7 [window.completions], n=14 [window.additions]
  > 14 things arrived. 7 left.
2026-11-01  [report observation]  persistentItem / stage 1 / observational / ob.pers.l34
  rule:  report.observation.persistentItem.low
  fired: the item has been active seven to thirteen days, the area holding the item has at least 1 events in the window
  facts: ageDays=13 [item.itemAgeDays:itm-302], areaName=Personal, itemTitle=Reply to the landlord
  > Personal has had a week and Reply to the landlord has had 13 days.
2026-11-01  [report pattern]  reportedVsActual / stage 1 / plain / pt.rva.04
  rule:  report.pattern.reportedVsActual
  fired: there are several stored answers to compare against, one family has been answered about more than twice, there are at least three weeks of snapshots, without which no pattern may fire
  > Your answers have been consistent. So has the pattern behind them.

2026-11-02  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.30
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Spread, without any one place holding it.
2026-11-02  [banner]  weekMixed / stage 1 / reflective / bn.mixed.15
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Tilted, so far.
2026-11-02  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-11-03  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.25
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Two weeks across several areas.
2026-11-03  [banner]  weekMixed / stage 1 / reflective / bn.mixed.25
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Uneven going.
2026-11-03  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-11-04  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.10
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Broad rather than deep.
2026-11-04  [banner]  weekBuilding / stage 1 / reflective / bn.build.10
  rule:  banner.weekBuilding
  fired: three to five days into the week, completions are accumulating
  > Midweek, with something in it.
2026-11-04  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-11-05  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.12
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Nothing has dominated.
2026-11-05  [banner]  weekBuilding / stage 1 / reflective / bn.build.26
  rule:  banner.weekBuilding
  fired: three to five days into the week, completions are accumulating
  > A week with something already in it.
2026-11-05  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-11-06  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.15
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Breadth, more than depth.
2026-11-06  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-11-06  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-11-07  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.25
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Two weeks across several areas.
2026-11-07  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-11-07  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-11-08  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.32
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight with its weight spread out.
2026-11-08  [banner]  weekStarting / stage 1 / reflective / bn.start.21
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > Early, before anything settles.
2026-11-08  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-11-08  [report headline]  personalBest / stage 1 / plain / hd.best.06
  rule:  report.headline.personalBest
  fired: no earlier week strictly beats this one, this week reaches the personal best, there is more than one week to be best of, there are completions, so no count renders as zero
  > Nothing has come close.
2026-11-08  [report observation]  personalBest / stage 1 / editorial / ob.best.l15
  rule:  report.observation.personalBest
  fired: no earlier week strictly beats this one, this week reaches the personal best, there is more than one week to be best of, there are completions, so no count renders as zero
  > Something in this week worked. The record can say that it did without saying what it was.
2026-11-08  [report observation]  persistentItem / stage 1 / neutral_agent / ob.pers.l17
  rule:  report.observation.persistentItem.high
  fired: the item has been active fourteen days or more, the area holding the item has at least 1 events in the window, there is a median completion time to compare against
  facts: n=79 [item.itemQueueBehind:itm-302]
  > 79 things wait behind it.
2026-11-08  [report observation]  areaBalance / stage 1 / editorial / ob.bal.l13
  rule:  report.observation.areaBalance
  fired: three or more areas had activity, no area holds half the week, the window holds at least 6 events, so a share is describing something real
  > Every area got some of the week and none got most of it.
2026-11-08  [report observation]  intakeVsOutput / stage 2 / neutral_agent / ob.flow.s2.l27
  rule:  report.observation.intakeVsOutput.s2
  fired: intake exceeds output by five or more, there are additions to count
  > What arrived outnumbered what left.
2026-11-08  [report pattern]  consistentRhythm / stage 1 / plain / pt.rhy.08
  rule:  report.pattern.consistentRhythm
  fired: four weeks inside a narrow band, there are four weeks to see it across, the band is a rhythm rather than a run of empty weeks
  > Same rhythm, four weeks running.

2026-11-09  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.24
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight that kept moving around.
2026-11-09  [banner]  weekMixed / stage 1 / reflective / bn.mixed.13
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week with a center of gravity in it.
2026-11-09  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-11-10  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.33
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight that went around rather than down.
2026-11-10  [banner]  weekMixed / stage 1 / reflective / bn.mixed.20
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Concentrated, with the rest thinner.
2026-11-10  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-11-11  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.23
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Nothing has held more than half.
2026-11-11  [banner]  weekMixed / stage 1 / reflective / bn.mixed.16
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week with a heavier side.
2026-11-11  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-11-12  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.16
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > No one place took it all.
2026-11-12  [banner]  weekBuilding / stage 1 / reflective / bn.build.34
  rule:  banner.weekBuilding
  fired: three to five days into the week, completions are accumulating
  > Some of the week spent, some of it left.
2026-11-12  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-11-13  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.07
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Evenly spread, across two weeks.
2026-11-13  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-11-13  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-11-14  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.31
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > More than one thing at a time.
2026-11-14  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-11-14  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-11-15  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.26
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Several places, none of them all of it.
2026-11-15  [banner]  weekStarting / stage 1 / reflective / bn.start.18
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > A beginning.
2026-11-15  [pulse]  SILENT (NO_RULE_QUALIFIED)
2026-11-15  [report headline]  personalBest / stage 1 / plain / hd.best.28
  rule:  report.headline.personalBest
  fired: no earlier week strictly beats this one, this week reaches the personal best, there is more than one week to be best of, there are completions, so no count renders as zero
  facts: n=7 [window.completions]
  > 7 things finished, a record.
2026-11-15  [report observation]  personalBest / stage 1 / editorial / ob.best.l10
  rule:  report.observation.personalBest
  fired: no earlier week strictly beats this one, this week reaches the personal best, there is more than one week to be best of, there are completions, so no count renders as zero
  > Nothing here needs to happen again.
2026-11-15  [report observation]  areaBalance / stage 1 / editorial / ob.bal.l16
  rule:  report.observation.areaBalance
  fired: three or more areas had activity, no area holds half the week, the window holds at least 6 events, so a share is describing something real
  > Spread is not the same as balance. This week was spread.
2026-11-15  [report observation]  intakeVsOutput / stage 2 / neutral_agent / ob.flow.s2.l16
  rule:  report.observation.intakeVsOutput.s2
  fired: intake exceeds output by five or more, there are additions to count
  facts: k=7 [rollup.queueGrowth]
  > 7 net, into the queues.
2026-11-15  [report observation]  queuePressure / stage 1 / neutral_agent / ob.qp.l28
  rule:  report.observation.queuePressure
  fired: at least one area's queue grew, the queues hold three or more things to count
  facts: m=304 [rollup.queueTotalAtStart], n=311 [rollup.queueTotal]
  > 311 things are in the queues now, 304 were there when the week opened, and at least one queue grew inside it.
2026-11-15  [report pattern]  reportedVsActual / stage 1 / plain / pt.rva.10
  rule:  report.pattern.reportedVsActual
  fired: there are several stored answers to compare against, one family has been answered about more than twice, there are at least three weeks of snapshots, without which no pattern may fire
  > The question came back.

2026-11-16  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.27
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight that went in several directions.
2026-11-16  [banner]  weekMixed / stage 1 / reflective / bn.mixed.24
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > The weight of the week sits in one place.
2026-11-16  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-11-17  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.26
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Several places, none of them all of it.
2026-11-17  [banner]  weekMixed / stage 1 / reflective / bn.mixed.09
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week with a tilt to it.
2026-11-17  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-11-18  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.18
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Level, more or less.
2026-11-18  [banner]  weekBuilding / stage 1 / reflective / bn.build.24
  rule:  banner.weekBuilding
  fired: three to five days into the week, completions are accumulating
  > The beginning has passed.
2026-11-18  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-11-19  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.13
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Wide, across two weeks.
2026-11-19  [banner]  weekBuilding / stage 1 / reflective / bn.build.18
  rule:  banner.weekBuilding
  fired: three to five days into the week, completions are accumulating
  > Some of the week has happened.
2026-11-19  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-11-20  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.31
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > More than one thing at a time.
2026-11-20  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-11-20  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-11-21  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.13
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Wide, across two weeks.
2026-11-21  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-11-21  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-11-22  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.16
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > No one place took it all.
2026-11-22  [banner]  weekStarting / stage 1 / reflective / bn.start.19
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > Little to read this early.
2026-11-22  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-11-22  [report headline]  personalBest / stage 1 / plain / hd.best.05
  rule:  report.headline.personalBest
  fired: no earlier week strictly beats this one, this week reaches the personal best, there is more than one week to be best of, there are completions, so no count renders as zero
  > A new high.
2026-11-22  [report observation]  personalBest / stage 1 / editorial / ob.best.l12
  rule:  report.observation.personalBest
  fired: no earlier week strictly beats this one, this week reaches the personal best, there is more than one week to be best of, there are completions, so no count renders as zero
  > The record moved. That is all the record does.
2026-11-22  [report observation]  areaBalance / stage 1 / editorial / ob.bal.l17
  rule:  report.observation.areaBalance
  fired: three or more areas had activity, no area holds half the week, the window holds at least 6 events, so a share is describing something real
  facts: areaCount=4 [rollup.areasWithEvents]
  > A week that touches 4 areas and gives none of them half has no single subject.
2026-11-22  [report observation]  intakeVsOutput / stage 2 / neutral_agent / ob.flow.s2.l18
  rule:  report.observation.intakeVsOutput.s2
  fired: intake exceeds output by five or more, there are additions to count
  facts: k=7 [rollup.queueGrowth], m=7 [window.completions], n=14 [window.additions]
  > 14 in, 7 out, and 7 added to the queues.
2026-11-22  [report observation]  persistentItem / stage 1 / observational / ob.pers.l31
  rule:  report.observation.persistentItem.low
  fired: the item has been active seven to thirteen days, the area holding the item has at least 1 events in the window
  facts: areaName=Home, itemTitle=Repot the balcony plants
  > Home kept working around Repot the balcony plants.
2026-11-22  [report pattern]  consistentRhythm / stage 1 / plain / pt.rhy.54
  rule:  report.pattern.consistentRhythm
  fired: four weeks inside a narrow band, there are four weeks to see it across, the band is a rhythm rather than a run of empty weeks
  > A month can be steady without being still, and this one had a total in every week of it.

2026-11-23  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.07
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Evenly spread, across two weeks.
2026-11-23  [banner]  weekMixed / stage 1 / reflective / bn.mixed.24
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > The weight of the week sits in one place.
2026-11-23  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-11-24  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.13
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Wide, across two weeks.
2026-11-24  [banner]  weekMixed / stage 1 / reflective / bn.mixed.22
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Some places busier than others.
2026-11-24  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-11-25  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.12
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Nothing has dominated.
2026-11-25  [banner]  weekMixed / stage 1 / reflective / bn.mixed.14
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A shape rather than a spread.
2026-11-25  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-11-26  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.18
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Level, more or less.
2026-11-26  [banner]  weekBuilding / stage 1 / reflective / bn.build.14
  rule:  banner.weekBuilding
  fired: three to five days into the week, completions are accumulating
  > Days behind it, days ahead.
2026-11-26  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-11-27  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.28
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight that did not settle in one place.
2026-11-27  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-11-27  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-11-28  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.13
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Wide, across two weeks.
2026-11-28  [banner]  weekMixed / stage 1 / reflective / bn.mixed.25
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Uneven going.
2026-11-28  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-11-29  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.04
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A wide fortnight.
2026-11-29  [banner]  weekStarting / stage 1 / reflective / bn.start.10
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > The start of a week.
2026-11-29  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-11-29  [report headline]  personalBest / stage 1 / plain / hd.best.57
  rule:  report.headline.personalBest
  fired: no earlier week strictly beats this one, this week reaches the personal best, there is more than one week to be best of, there are completions, so no count renders as zero
  > A peak.
2026-11-29  [report observation]  personalBest / stage 1 / editorial / ob.best.l04
  rule:  report.observation.personalBest
  fired: no earlier week strictly beats this one, this week reaches the personal best, there is more than one week to be best of, there are completions, so no count renders as zero
  > Nothing in eleven weeks of history looks like this one.
2026-11-29  [report observation]  persistentItem / stage 1 / neutral_agent / ob.pers.l08
  rule:  report.observation.persistentItem.high
  fired: the item has been active fourteen days or more, the area holding the item has at least 1 events in the window, there is a median completion time to compare against
  facts: ageDays=19 [item.itemAgeDays:itm-322], areaName=Home
  > 2 weeks at the front of Home.
2026-11-29  [report observation]  areaBalance / stage 1 / editorial / ob.bal.l08
  rule:  report.observation.areaBalance
  fired: three or more areas had activity, no area holds half the week, the window holds at least 6 events, so a share is describing something real
  > A week can be wide or it can be deep.
2026-11-29  [report observation]  intakeVsOutput / stage 2 / neutral_agent / ob.flow.s2.l29
  rule:  report.observation.intakeVsOutput.s2
  fired: intake exceeds output by five or more, there are additions to count
  > A collecting week.
2026-11-29  [report pattern]  reportedVsActual / stage 1 / plain / pt.rva.55
  rule:  report.pattern.reportedVsActual
  fired: there are several stored answers to compare against, one family has been answered about more than twice, there are at least three weeks of snapshots, without which no pattern may fire
  > The same kind of question has come around three times, which says more about the situation than about the answers.

2026-11-30  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.19
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Spread out, over fourteen days.
2026-11-30  [banner]  weekMixed / stage 1 / reflective / bn.mixed.12
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week that leans one way.
2026-11-30  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
```

### December 2026

31 days on screen, 0 Pulses spoken, 31 Pulse days silent, 80 sentences in all.

```text
2026-12-01  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.27
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight that went in several directions.
2026-12-01  [banner]  weekMixed / stage 1 / reflective / bn.mixed.25
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Uneven going.
2026-12-01  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-12-02  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.10
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Broad rather than deep.
2026-12-02  [banner]  weekMixed / stage 1 / reflective / bn.mixed.11
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Weighted rather than spread.
2026-12-02  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-12-03  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.33
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight that went around rather than down.
2026-12-03  [banner]  weekBuilding / stage 1 / reflective / bn.build.24
  rule:  banner.weekBuilding
  fired: three to five days into the week, completions are accumulating
  > The beginning has passed.
2026-12-03  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-12-04  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.13
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Wide, across two weeks.
2026-12-04  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-12-04  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-12-05  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.23
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Nothing has held more than half.
2026-12-05  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-12-05  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-12-06  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.27
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight that went in several directions.
2026-12-06  [banner]  weekStarting / stage 1 / reflective / bn.start.14
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > Too early for much of a reading.
2026-12-06  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-12-06  [report headline]  personalBest / stage 1 / plain / hd.best.10
  rule:  report.headline.personalBest
  fired: no earlier week strictly beats this one, this week reaches the personal best, there is more than one week to be best of, there are completions, so no count renders as zero
  > A week that stands out.
2026-12-06  [report observation]  personalBest / stage 1 / editorial / ob.best.l18
  rule:  report.observation.personalBest
  fired: no earlier week strictly beats this one, this week reaches the personal best, there is more than one week to be best of, there are completions, so no count renders as zero
  facts: n=7 [window.completions]
  > 7 completions is the largest number this record has held, and a number is all it is.
2026-12-06  [report observation]  areaBalance / stage 1 / editorial / ob.bal.l14
  rule:  report.observation.areaBalance
  fired: three or more areas had activity, no area holds half the week, the window holds at least 6 events, so a share is describing something real
  facts: pct=36 [area.areaShare:health]
  > 36 percent is the largest share anything took. It is not a majority.
2026-12-06  [report observation]  intakeVsOutput / stage 2 / neutral_agent / ob.flow.s2.l31
  rule:  report.observation.intakeVsOutput.s2
  fired: intake exceeds output by five or more, there are additions to count
  facts: k=7 [rollup.queueGrowth], n=14 [window.additions]
  > 14 things came in over seven days and the lists are 7 longer for it.
2026-12-06  [report observation]  persistentItem / stage 1 / observational / ob.pers.l33
  rule:  report.observation.persistentItem.low
  fired: the item has been active seven to thirteen days, the area holding the item has at least 1 events in the window
  facts: itemTitle=Repot the balcony plants
  > Repot the balcony plants outlasted the week.
2026-12-06  [report pattern]  consistentRhythm / stage 1 / plain / pt.rhy.06
  rule:  report.pattern.consistentRhythm
  fired: four weeks inside a narrow band, there are four weeks to see it across, the band is a rhythm rather than a run of empty weeks
  > Four weeks of roughly the same shape.

2026-12-07  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.04
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A wide fortnight.
2026-12-07  [banner]  weekMixed / stage 1 / reflective / bn.mixed.25
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Uneven going.
2026-12-07  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-12-08  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.10
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Broad rather than deep.
2026-12-08  [banner]  weekMixed / stage 1 / reflective / bn.mixed.16
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week with a heavier side.
2026-12-08  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-12-09  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.22
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Two weeks that went several ways.
2026-12-09  [banner]  weekMixed / stage 1 / reflective / bn.mixed.16
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week with a heavier side.
2026-12-09  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-12-10  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.23
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Nothing has held more than half.
2026-12-10  [banner]  weekBuilding / stage 1 / reflective / bn.build.33
  rule:  banner.weekBuilding
  fired: three to five days into the week, completions are accumulating
  > A week with days still in front of it.
2026-12-10  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-12-11  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.10
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Broad rather than deep.
2026-12-11  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-12-11  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-12-12  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.15
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Breadth, more than depth.
2026-12-12  [banner]  weekMixed / stage 1 / reflective / bn.mixed.73
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week that piled up in one place.
2026-12-12  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-12-13  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.29
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Several things at once, for two weeks.
2026-12-13  [banner]  weekStarting / stage 1 / reflective / bn.start.23
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > Nothing here has had time to become a shape.
2026-12-13  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-12-13  [report headline]  personalBest / stage 1 / plain / hd.best.18
  rule:  report.headline.personalBest
  fired: no earlier week strictly beats this one, this week reaches the personal best, there is more than one week to be best of, there are completions, so no count renders as zero
  facts: n=7 [window.completions]
  > The first time at 7.
2026-12-13  [report observation]  personalBest / stage 1 / editorial / ob.best.l13
  rule:  report.observation.personalBest
  fired: no earlier week strictly beats this one, this week reaches the personal best, there is more than one week to be best of, there are completions, so no count renders as zero
  > A high week is a fact.
2026-12-13  [report observation]  persistentItem / stage 1 / neutral_agent / ob.pers.l21
  rule:  report.observation.persistentItem.high
  fired: the item has been active fourteen days or more, the area holding the item has at least 1 events in the window, there is a median completion time to compare against
  facts: areaName=Personal, itemTitle=Repot the balcony plants
  > Personal had events this week. Repot the balcony plants stayed at its front.
2026-12-13  [report observation]  areaBalance / stage 1 / editorial / ob.bal.l04
  rule:  report.observation.areaBalance
  fired: three or more areas had activity, no area holds half the week, the window holds at least 6 events, so a share is describing something real
  > No single area owned this week.
2026-12-13  [report observation]  intakeVsOutput / stage 2 / neutral_agent / ob.flow.s2.l26
  rule:  report.observation.intakeVsOutput.s2
  fired: intake exceeds output by five or more, there are additions to count
  > The queues took on more than they let go.
2026-12-13  [report pattern]  reportedVsActual / stage 1 / plain / pt.rva.15
  rule:  report.pattern.reportedVsActual
  fired: there are several stored answers to compare against, one family has been answered about more than twice, there are at least three weeks of snapshots, without which no pattern may fire
  > Three weeks of answers.

2026-12-14  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.25
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Two weeks across several areas.
2026-12-14  [banner]  weekMixed / stage 1 / reflective / bn.mixed.24
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > The weight of the week sits in one place.
2026-12-14  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-12-15  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.15
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Breadth, more than depth.
2026-12-15  [banner]  weekMixed / stage 1 / reflective / bn.mixed.21
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week with more of it in one area.
2026-12-15  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-12-16  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.07
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Evenly spread, across two weeks.
2026-12-16  [banner]  weekMixed / stage 1 / reflective / bn.mixed.23
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week that did not spread evenly.
2026-12-16  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-12-17  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.12
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Nothing has dominated.
2026-12-17  [banner]  weekBuilding / stage 1 / reflective / bn.build.30
  rule:  banner.weekBuilding
  fired: three to five days into the week, completions are accumulating
  > Far enough in that something has happened.
2026-12-17  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-12-18  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.25
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Two weeks across several areas.
2026-12-18  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-12-18  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-12-19  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.12
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Nothing has dominated.
2026-12-19  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-12-19  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-12-20  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.04
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A wide fortnight.
2026-12-20  [banner]  weekStarting / stage 1 / reflective / bn.start.13
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > The opening of a week.
2026-12-20  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-12-20  [report headline]  personalBest / stage 1 / plain / hd.best.27
  rule:  report.headline.personalBest
  fired: no earlier week strictly beats this one, this week reaches the personal best, there is more than one week to be best of, there are completions, so no count renders as zero
  > The highest count so far.
2026-12-20  [report observation]  personalBest / stage 1 / editorial / ob.best.l21
  rule:  report.observation.personalBest
  fired: no earlier week strictly beats this one, this week reaches the personal best, there is more than one week to be best of, there are completions, so no count renders as zero
  facts: m=7 [history.personalBestCompletions], sinceRef=2026-12-06
  > The best earlier week was 7, in December, and this one is not below it.
2026-12-20  [report observation]  persistentItem / stage 1 / neutral_agent / ob.pers.l15
  rule:  report.observation.persistentItem.high
  fired: the item has been active fourteen days or more, the area holding the item has at least 1 events in the window, there is a median completion time to compare against
  facts: ageDays=22 [item.itemAgeDays:itm-342], itemTitle=Repot the balcony plants
  > Repot the balcony plants has held its place for 3 weeks.
2026-12-20  [report observation]  areaBalance / stage 1 / editorial / ob.bal.l10
  rule:  report.observation.areaBalance
  fired: three or more areas had activity, no area holds half the week, the window holds at least 6 events, so a share is describing something real
  > Nothing here was the main thing.
2026-12-20  [report observation]  intakeVsOutput / stage 2 / neutral_agent / ob.flow.s2.l11
  rule:  report.observation.intakeVsOutput.s2
  fired: intake exceeds output by five or more, there are additions to count
  facts: m=7 [window.completions], n=14 [window.additions]
  > The queues took on 14 and released 7.
2026-12-20  [report pattern]  consistentRhythm / stage 1 / plain / pt.rhy.40
  rule:  report.pattern.consistentRhythm
  fired: four weeks inside a narrow band, there are four weeks to see it across, the band is a rhythm rather than a run of empty weeks
  > Four weeks that hold the same line.

2026-12-21  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.27
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight that went in several directions.
2026-12-21  [banner]  weekMixed / stage 1 / reflective / bn.mixed.11
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > Weighted rather than spread.
2026-12-21  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-12-22  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.17
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Room for more than one thing.
2026-12-22  [banner]  weekMixed / stage 1 / reflective / bn.mixed.24
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > The weight of the week sits in one place.
2026-12-22  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-12-23  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.25
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Two weeks across several areas.
2026-12-23  [banner]  weekMixed / stage 1 / reflective / bn.mixed.19
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week gathered mostly in one place.
2026-12-23  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-12-24  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.30
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Spread, without any one place holding it.
2026-12-24  [banner]  weekBuilding / stage 1 / reflective / bn.build.29
  rule:  banner.weekBuilding
  fired: three to five days into the week, completions are accumulating
  > Some days gone, some things done, the week still open.
2026-12-24  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-12-25  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.16
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > No one place took it all.
2026-12-25  [banner]  SILENT (NO_RULE_QUALIFIED)
2026-12-25  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-12-26  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.28
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight that did not settle in one place.
2026-12-26  [banner]  weekMixed / stage 1 / reflective / bn.mixed.12
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week that leans one way.
2026-12-26  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-12-27  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.17
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Room for more than one thing.
2026-12-27  [banner]  weekStarting / stage 1 / reflective / bn.start.15
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > Nothing settled yet.
2026-12-27  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2026-12-27  [report headline]  personalBest / stage 1 / plain / hd.best.02
  rule:  report.headline.personalBest
  fired: no earlier week strictly beats this one, this week reaches the personal best, there is more than one week to be best of, there are completions, so no count renders as zero
  > A record.
2026-12-27  [report observation]  personalBest / stage 1 / editorial / ob.best.l11
  rule:  report.observation.personalBest
  fired: no earlier week strictly beats this one, this week reaches the personal best, there is more than one week to be best of, there are completions, so no count renders as zero
  facts: n=7 [window.completions]
  > The number is 7. The reading of it is not the app's to make.
2026-12-27  [report observation]  areaBalance / stage 1 / editorial / ob.bal.l04
  rule:  report.observation.areaBalance
  fired: three or more areas had activity, no area holds half the week, the window holds at least 6 events, so a share is describing something real
  > No single area owned this week.
2026-12-27  [report observation]  intakeVsOutput / stage 2 / neutral_agent / ob.flow.s2.l33
  rule:  report.observation.intakeVsOutput.s2
  fired: intake exceeds output by five or more, there are additions to count
  facts: m=7 [window.completions], n=14 [window.additions]
  > The week took in 14 things and released 7, and the difference between those two runs to five or more.
2026-12-27  [report observation]  persistentItem / stage 1 / observational / ob.pers.l02
  rule:  report.observation.persistentItem.low
  fired: the item has been active seven to thirteen days, the area holding the item has at least 1 events in the window
  facts: ageDays=7 [item.itemAgeDays:itm-362], itemTitle=Call the bank
  > Nothing has moved past Call the bank in 7 days.
2026-12-27  [report pattern]  reportedVsActual / stage 1 / plain / pt.rva.59
  rule:  report.pattern.reportedVsActual
  fired: there are several stored answers to compare against, one family has been answered about more than twice, there are at least three weeks of snapshots, without which no pattern may fire
  > Five answers is enough for the record to have a shape, and three of them point at the same kind of moment.

2026-12-28  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.32
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight with its weight spread out.
2026-12-28  [banner]  weekMixed / stage 1 / reflective / bn.mixed.73
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week that piled up in one place.
2026-12-28  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-12-29  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.28
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight that did not settle in one place.
2026-12-29  [banner]  weekMixed / stage 1 / reflective / bn.mixed.19
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week gathered mostly in one place.
2026-12-29  [pulse]  SILENT (NO_RULE_QUALIFIED)

2026-12-30  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.22
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Two weeks that went several ways.
2026-12-30  [banner]  weekMixed / stage 1 / reflective / bn.mixed.21
  rule:  banner.weekMixed
  fired: two or more areas were active, one of them holds half the week or more, the window holds at least 4 events, so a share is describing something real
  > A week with more of it in one area.
2026-12-30  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2026-12-31  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.12
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Nothing has dominated.
2026-12-31  [banner]  weekBuilding / stage 1 / reflective / bn.build.15
  rule:  banner.weekBuilding
  fired: three to five days into the week, completions are accumulating
  > A week past its start.
2026-12-31  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
```

### January 2027

3 days on screen, 0 Pulses spoken, 3 Pulse days silent, 10 sentences in all.

```text
2027-01-01  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.22
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > Two weeks that went several ways.
2027-01-01  [banner]  SILENT (NO_RULE_QUALIFIED)
2027-01-01  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)

2027-01-02  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.27
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > A fortnight that went in several directions.
2027-01-02  [banner]  SILENT (NO_RULE_QUALIFIED)
2027-01-02  [pulse]  SILENT (NO_RULE_QUALIFIED)

2027-01-03  [momentum]  balancedWeek / stage 1 / reflective / mo.bal.16
  rule:  momentum.balancedWeek
  fired: three or more areas were active, none above half the window, the window holds at least 5 events, so a share is describing something real, the app has existed for the 14 days being described
  > No one place took it all.
2027-01-03  [banner]  weekStarting / stage 1 / reflective / bn.start.14
  rule:  banner.weekStarting
  fired: one or two days into the week, fewer than three completions so far
  > Too early for much of a reading.
2027-01-03  [pulse]  SILENT (ALL_QUALIFIED_RULES_FILTERED)
2027-01-03  [report headline]  personalBest / stage 1 / plain / hd.best.35
  rule:  report.headline.personalBest
  fired: no earlier week strictly beats this one, this week reaches the personal best, there is more than one week to be best of, there are completions, so no count renders as zero
  > Top of the list.
2027-01-03  [report observation]  personalBest / stage 1 / editorial / ob.best.l16
  rule:  report.observation.personalBest
  fired: no earlier week strictly beats this one, this week reaches the personal best, there is more than one week to be best of, there are completions, so no count renders as zero
  facts: m=7 [history.personalBestCompletions]
  > The previous best was 7. This week is not less than that.
2027-01-03  [report observation]  persistentItem / stage 1 / neutral_agent / ob.pers.l12
  rule:  report.observation.persistentItem.high
  fired: the item has been active fourteen days or more, the area holding the item has at least 1 events in the window, there is a median completion time to compare against
  facts: ageDays=14 [item.itemAgeDays:itm-362], areaName=Home, itemTitle=Call the bank
  > Nothing else has reached the front of Home in 2 weeks, and Call the bank is still there.
2027-01-03  [report observation]  areaBalance / stage 1 / editorial / ob.bal.l15
  rule:  report.observation.areaBalance
  fired: three or more areas had activity, no area holds half the week, the window holds at least 6 events, so a share is describing something real
  facts: areaCount=4 [rollup.areasWithEvents]
  > Attention went to 4 places and settled in none of them.
2027-01-03  [report observation]  intakeVsOutput / stage 2 / neutral_agent / ob.flow.s2.l32
  rule:  report.observation.intakeVsOutput.s2
  fired: intake exceeds output by five or more, there are additions to count
  facts: k=7 [rollup.queueGrowth], m=7 [window.completions], n=14 [window.additions]
  > 14 things came in this week, 7 went out, and the queues ended 7 longer than they opened.
2027-01-03  [report pattern]  consistentRhythm / stage 1 / plain / pt.rhy.59
  rule:  report.pattern.consistentRhythm
  fired: four weeks inside a narrow band, there are four weeks to see it across, the band is a rhythm rather than a run of empty weeks
  > Four weeks alike.
```

### The year in counts

```text
-- counts ------------------------------------------------------
pulse: 365 days, 9 spoken, 356 silent (97 percent silent)
silence reasons: ALL_QUALIFIED_RULES_FILTERED 276, NO_RULE_QUALIFIED 79, INSUFFICIENT_DATA 1
pulse families: freshStart 4, persistence 4, spread 1
pulse stages:   freshStart.s1 4, persistence.s1 4, spread.s2 1
momentum: 365 invocations, 364 spoken
  families: balancedWeek 351, firstDays 13
banner: 365 invocations, 271 spoken
  families: weekMixed 126, weekBuilding 91, weekStarting 54
report headline: 52 invocations, 52 spoken
  families: personalBest 50, balanced 2
report observation: 208 invocations, 208 spoken
  families: areaBalance 52, intakeVsOutput 52, personalBest 50, persistentItem 40, queuePressure 11, completionSplit 2, firstMilestone 1
report pattern: 49 invocations, 49 spoken
  families: consistentRhythm 24, reportedVsActual 24, growingQueues 1
layer 5 vetoes: 0
distinct variants used: 281
```

---

## Appendix A: every `hardStretch` line in the run

`hardStretch` fired **five times across eleven persona years**, which is what 6.4 intends:
it waits six weeks between firings and only three or more quiet weeks with growing queues,
or a four week decline, reach it at all. One falls inside the three years above and is
banner flagged in place. All five are here so the family can be judged whole, because five
lines is the entire year's evidence for a family whose stated consequence is removal.

| persona | date | key | the line |
|---|---|---|---|
| Abandoning | 2026-10-25 | `ob.hard.l07` | A period like this is not a failure of the system you set up. |
| Accepts every plan | 2026-04-26 | `ob.hard.l09` | Three weeks now. |
| Accepts every plan | 2026-09-06 | `ob.hard.l15` | The app has a shape for the last three weeks and no reading of it. |
| Low focus | 2026-08-30 | `ob.hard.l02` | Stretches like this are common and they are not usually about the app. |
| Fast completer | 2026-09-27 | `ob.hard.l06` | Nothing here has moved in a month. That happens. |

**The three lines to rule on are all approved lines, and none of them is phase 9's.**
`ob.hard.l01`, `l02` and `l03` say that a stretch like this is *common*, that it *usually
means something outside the app*, and that it *generally has a reason that is not visible
here*. Every one of those is a claim about the world rather than a reading of the record,
and the app has no other person's data to know it from. `ob.hard.l07` denies a failure,
which is reassurance wearing the grammar of an observation. All four predate phase 9 and
all four are in the bench the eight lines above were grown from.

**The eight lines phase 9 added to this bench hold 6.4 more tightly than the eight it
inherited.** `Three weeks now.` and `The app has a shape for the last three weeks and no
reading of it.` state the pattern and stop; the second one declines to interpret out loud,
which is the whole register in one sentence. That is the shape of the ruling if the owner
wants one: the newer half of this bench is the standard, and the older half is what 6.4
warned about when it said the family is removed rather than rewritten.

---

## Appendix B: where the repeats are now, and who owns them

Phase 9 took variant repeats inside ninety days from **7,370 to 3,898**, and the
tightest gap is still one day. **95 percent of what is left is the Momentum headline and the
areas banner**, which are the two surfaces that render on every app open rather than once a
day: 2,108 of the repeats are Momentum and 1,586 are the banner, against 46 in the Pulse,
176 in the report and 10 in the pattern section.

The benches under the most pressure, measured as firings across the run divided by the
number of lines the chooser could actually reach.

| surface | family | firings | lines reached | firings per line | the most said line |
|---|---|---|---|---|---|
| banner | `weekMixed` | 1,308 | 19 | 68.8 | `bn.mixed.25`, 101 times |
| banner | `weekStarting` | 804 | 18 | 44.7 | `bn.start.21`, 72 times |
| report observation | `focusInvestment` s3 | 35 | **1** | 35.0 | `ob.focus.s3.l02`, 35 times |
| Momentum | `singleAreaWeek` | 1,054 | 31 | 34.0 | `mo.single.40`, 55 times |
| Momentum | `balancedWeek` | 831 | 25 | 33.2 | `mo.bal.23`, 50 times |
| Momentum | `comeback` | 604 | 25 | 24.2 | `mo.come.14`, 37 times |
| Pulse | `quietDay` s1 | 212 | 21 | 10.1 | `quietday.s1.20`, 15 times |

**Two different problems sit in that table and only one of them is about writing.**

The banner and Momentum rows are a **sizing** problem that 11.1 does not have a tier for. Its
table stops at "40 firings a year or more" and asks for 60 to 100 lines. `weekMixed` fires
1,308 times across eleven persona years because it is recomputed on every open, and no bench
of any size holds a ninety day exclusion against that. This is a cooldown or a throttle
decision, not a bench.

The `focusInvestment` row is a **binding** problem, and it is the sharpest single line in this
run. That bench has six leads. The register the realizer reaches is observational, which
holds two of them, and one of those two, `ob.focus.s3.l04`, is one of the 82 lines the engine
cannot fill. So the reachable bench is **one sentence**, and `highFocus` hears it thirty five
times in a year: *That is more focused time than any week before this one.* The rule behind it
requires eight finished focus sessions and at least one minute, and **neither criterion
establishes a record**, so the line also claims something the rule does not fix, thirty five
times, to the same person. `focusInvestment` is warm at 36 firings, so phase 9 was correctly
told not to touch it. One binding or one line closes it and no authoring pass would have
found it, because it is only visible in a year read in order.

---

## Appendix C: the seventh measurement, in brief

The full table, with all six earlier columns beside it, is in
`CLARITY_LOGIC_ENGINE.md` 12. The short version, over the same eleven personas and the same
simulated year as every measurement before it.

| reading | sixth, before phase 9 | seventh, after |
|---|---|---|
| authored corpus lines | 2,942 | **4,733** |
| variant repeats inside ninety days | 7,370 | **3,898** |
| two consecutive report leads sharing a length band | 719 at the fifth, the last recorded | **277** |
| three or more parallel numeric clauses in a row | 41 at the fifth, the last recorded | **121** |
| Pulse silence, all personas | 65.7 percent | **65.7 percent** |
| silent days: qualified and filtered / nothing qualified / too little data | 1,161 / 895 / 11 | **1,161 / 895 / 11** |
| layer 5 vetoes | 0 | **0** |
| distinct variants a whole run reached | not measured | **1,162 of 4,733** |
| registers a surface reaches | not measured | **one, on five of the six surfaces** |

One reading moved the wrong way. **Three or more parallel numeric clauses in a row went from
41 runs to 121**, while two consecutive leads sharing a length band went from 719 to 277.
Both are properties of the order a report is composed in rather than of any line, and the
deeper benches gave the composer more numeric leads to put next to each other at the same
time as they gave it more length bands to alternate. The band rule is honored as a preference
inside `Realizer.choose` and the numeric rule is honored by nothing at all, which is the
whole of the difference.

**Authoring moved every reading a bench can move, and moved silence by nothing, because no
silent day in this run was caused by a bench.** `VariantChoice.choose` reuses a line rather
than falling silent when a bench is exhausted, so depth cannot produce silence at all; a
bench can only fail to speak when no line in it can be *filled*, which is a question about
slot bindings and not about how many lines there are. The rest of the filtered column comes
from availability, callbacks, the horizon, the Pulse repeat filter and the cooldowns, none
of which reads the corpus. That is why 3,230 new lines left the silent day count identical
to the day.
