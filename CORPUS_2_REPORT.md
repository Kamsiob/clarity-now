# Clarity Phrasing Corpus, Volume 2: The Clarity Report

The authored language library for the weekly Clarity Report. This is the deepest and most consequential corpus in the app.

**Volume 2 of 3.** Volume 1 is Pulse. Volume 3 is Momentum and the Areas banner.

**All language panel amendments are applied.** Section 4 is now the guidance corpus in nominal form. Section labels are sentence case. A neutral-agent register, a difficulty family, and a short length band have been added. Reused constructions are capped and the line-level deletions are done. American spelling throughout.

---

## Why this volume is different

Pulse says one thing and asks one question. The Report writes an eight to ten sentence essay about a person's week, and it does three things Pulse never does:

1. **It compares.** Against last week, against the last three weeks, against every week since install.
2. **It calls back.** It quotes what the user said about their own behavior and sets it against what actually happened. This is the single most powerful thing the engine does.
3. **It advises.** Exactly once, in the `ONE THING THIS WEEK` line. That is the only sanctioned advice in the entire application.

It also has a structural problem Pulse does not. Because it emits many sentences at once, those sentences can contradict each other. Section 7 handles that.

---

## Structure of every family here

Report families use two benches rather than three:

- **Leads**, which state the fact. Every lead is a complete sentence and can stand alone.
- **Extensions**, which contextualise, compare, or draw the line to a second fact. Optional. A lead with no extension is a valid observation.

A lead combines only with an extension from its own family and stage. Extension use is governed by the family's `extensionRate` and by whether the extension's required facts are available. Never force one.

Registers are tagged `[P]` plain, `[O]` observational, `[E]` editorial, `[N]` neutral-agent. **`[N]` neutral-agent** is used where the content is unflattering. The fact becomes the grammatical subject instead of the person: *Nine things arrived. Six left.* This is **not passive voice**; *nine things were added by you* is banned. Selection is automatic, driven by the rule's `unflattering` flag, per `CLARITY_LOGIC_ENGINE.md` 7.4.

**Length bands.** `SHORT` under 7 words, `MEDIUM` 7 to 14, `LONG` 15 to 24. The composer may not select two consecutive leads from the same band.

**The band is computed at catalog load from the realized word count, never authored.** A handful of `[S]` markers appear below as authoring hints where a short line was written deliberately. They are advisory; the computed value always wins. See `CLARITY_LOGIC_ENGINE.md` 7.5.

Editorial is a register Pulse does not have. It is where the Report is allowed to sound like a piece of writing rather than a readout, and it is reserved for leads that have earned it with a genuinely notable fact.

**Slots.** `{areaName}` `{otherArea}` `{thirdArea}` `{itemTitle}` `{n}` `{m}` `{k}` `{pct}` `{otherPct}` `{minutes}` `{sessions}` `{dayName}` `{sinceRef}` `{weekRef}` `{ageDays}` `{priorLabel}` `{priorCount}` `{areaCount}` `{medianDays}`

**Cue slots**, used only in section 4 and filled from `CueFacts`: `{strongestWeekday}` `{quietestWeekday}` `{focusTypicalWeekday}`. Each may only render when its confidence threshold in `CLARITY_LOGIC_ENGINE.md` 3.7 is met.

In the Report, counts of two through nine render as **digits**, not words. This is the register difference from Pulse and it is enforced by the renderer. Percentages render as `78 percent`.

**Variant keys** are `report.section.family.stage.index`, stable forever.


## Key prefixes

Variant keys abbreviate their family name. **This table is the only place the mapping is written down, and the engine's `FamilyKey` values are the camelCase names in the right column.**

| key prefix | family |
|---|---|
| `hd.quiet` | quietWeek |
| `hd.out` | netOutflow |
| `hd.in` | netInflow |
| `hd.single` | singleFocus |
| `hd.bal` | balanced |
| `hd.focus` | focusProtected |
| `hd.best` | personalBest |
| `hd.since` | mostActiveSince |
| `hd.decline` | decliningActivity |
| `hd.rise` | risingActivity |
| `hd.back` | comeback |
| `hd.queue` | queuePressure |
| `hd.clear` | clearing |
| `hd.steady` | steadyPace |
| `hd.frag` | fragmented |
| `hd.first` | firstWeek |
| `hd.fall` | datedFallback |
| `ob.single` | singleFocus |
| `ob.flow` | intakeVsOutput |
| `ob.focus` | focusInvestment |
| `ob.neg` | neglectedArea |
| `ob.split` | completionSplit |
| `ob.srvd` | selfReportVsData |
| `ob.quiet` | quietWeek |
| `ob.qp` | queuePressure |
| `ob.rev` | areaRevival |
| `ob.pers` | persistentItem |
| `ob.best` | personalBest |
| `ob.since` | mostActiveSince |
| `ob.day` | dayShape |
| `ob.tod` | timeOfDay |
| `ob.swi` | switchingBehavior |
| `ob.aban` | focusAbandonment |
| `ob.drain` | queueDrained |
| `ob.stead` | steadyPace |
| `ob.first` | firstMilestone |
| `ob.bal` | areaBalance |
| `ob.hard` | hardStretch |
| `ob.fam` | familiarDip |
| `ob.est` | estimateCalibration |
| `pt.shift` | shiftingFocus |
| `pt.grow` | growingQueues |
| `pt.imp` | improvingThroughput |
| `pt.dec` | decliningActivity |
| `pt.gone` | areaGoneQuiet |
| `pt.rhy` | consistentRhythm |
| `pt.narrow` | narrowingFocus |
| `pt.broad` | broadeningFocus |
| `pt.hab` | focusHabitForming |
| `pt.fade` | focusHabitFading |
| `pt.rva` | reportedVsActual |
| `pt.eq` | queueEquilibrium |
| `pt.wknd` | weekendShift |
| `pt.ab` | abandonmentPattern |
| `pt.come` | comebackPattern |
| `pt.none` | insufficientData |
| `cls.trust` | trustThePace |
| `cls.let` | letItBe |
| `cls.new` | noRhythmYet |
| `cls.rev` | review |

Within a key, `l` marks a lead and `e` marks an extension. `s1`, `s2`, `s3` mark escalation stages where a family has them.

---

# SECTION 1: HEADLINES

One per report. Under 8 words. Serif, centered, the largest text in the app. It is a pull quote, not a summary, and it never motivates.

## 1.1 quietWeek

Trigger: total events below the quiet threshold.

```
hd.quiet.01  A still week.
hd.quiet.02  Not much moved.
hd.quiet.03  A quiet one.
hd.quiet.04  The week passed.
hd.quiet.05  Little happened here.
hd.quiet.06  A week of waiting.
hd.quiet.07  Almost nothing.
hd.quiet.08  Stillness.
hd.quiet.09  A pause.
hd.quiet.10  Quiet, start to finish.
hd.quiet.11  Nothing moved.
hd.quiet.12  A week held in place.
```

## 1.2 netOutflow

Trigger: completions clearly exceed additions.

```
hd.out.01  More out than in.
hd.out.02  The queues got shorter.
hd.out.03  A clearing week.
hd.out.04  Things left.
hd.out.05  Output won.
hd.out.06  You finished more than you started.
hd.out.07  The list shrank.
hd.out.08  Emptying, not filling.
hd.out.09  Closed more than opened.
hd.out.10  A week of endings.
hd.out.11  Down, not up.
hd.out.12  Lighter than it was.
```

## 1.3 netInflow

Trigger: additions clearly exceed completions.

```
hd.in.01  More went in than came out.
hd.in.02  The queues grew.
hd.in.03  A gathering week.
hd.in.04  Things arrived.
hd.in.05  Intake won.
hd.in.06  You started more than you finished.
hd.in.07  The list got longer.
hd.in.08  Filling, not emptying.
hd.in.09  Opened more than closed.
hd.in.10  A week of beginnings.
hd.in.11  Up, not down.
hd.in.12  Heavier than it was.
```

## 1.4 singleFocus

Trigger: one area at 80 percent or more.

```
hd.single.01  Deep in {areaName}.
hd.single.02  {areaName}, mostly.
hd.single.03  It was a {areaName} week.
hd.single.04  One area held it.
hd.single.05  All {areaName}.
hd.single.06  {areaName} took the week.
hd.single.07  Everything pointed one way.
hd.single.08  A week with one center.
hd.single.09  {areaName} and little else.
hd.single.10  Narrow and deep.
hd.single.11  One thing at a time, all week.
hd.single.12  {areaName} carried it.
```

## 1.5 balanced

Trigger: activity across three or more areas, none dominant.

```
hd.bal.01  Attention everywhere.
hd.bal.02  A wide week.
hd.bal.03  Everything got some.
hd.bal.04  No center.
hd.bal.05  Spread across the board.
hd.bal.06  Every area moved.
hd.bal.07  A week in all directions.
hd.bal.08  Nothing dominated.
hd.bal.09  Broad, not deep.
hd.bal.10  All of it, a little.
hd.bal.11  Evenly distributed.
hd.bal.12  A week without a subject.
hd.bal.13  Three areas at least.
hd.bal.14  No area took half.
hd.bal.15  Nothing took the week.
hd.bal.16  The week went several ways.
hd.bal.17  Several areas, none in charge.
hd.bal.18  A week with several centers.
hd.bal.19  Work in more than one place.
hd.bal.20  Three or more areas moved.
hd.bal.21  Attention divided.
hd.bal.22  Nothing held a majority.
hd.bal.23  A week split several ways.
hd.bal.24  Wide, not narrow.
hd.bal.25  Many, not one.
hd.bal.26  No single center of gravity.
hd.bal.27  Every area got something.
hd.bal.28  A week of several subjects.
hd.bal.29  Nothing above half.
hd.bal.30  A week across the areas.
hd.bal.31  Nothing carried the week alone.
hd.bal.32  Some in each.
hd.bal.33  No area over half.
hd.bal.34  Three ways at once.
hd.bal.35  A spread week.
hd.bal.36  Nothing concentrated.
hd.bal.37  No one area held it.
hd.bal.38  Several places, some in each.
hd.bal.39  It went everywhere.
hd.bal.40  A week with breadth.
hd.bal.41  Attention in several places.
hd.bal.42  Nothing over half of it.
hd.bal.43  Each area saw something.
hd.bal.44  A week of many small parts.
hd.bal.45  The week never narrowed.
hd.bal.46  No area claimed it.
hd.bal.47  A wide spread.
hd.bal.48  Three fronts at least.
hd.bal.49  Several things at once.
hd.bal.50  Attention shared out.
hd.bal.51  Several areas had a share.
hd.bal.52  No majority anywhere.
hd.bal.53  Every one of them moved.
hd.bal.54  A week without a lead.
hd.bal.55  Nothing took more than half.
hd.bal.56  More places than one.
hd.bal.57  Three areas or more had something.
hd.bal.58  Several areas took part.
hd.bal.59  Nothing rose above the rest.
hd.bal.60  No area was the week.
```

## 1.6 focusProtected

Trigger: five or more focus sessions.

```
hd.focus.01  Time was protected.
hd.focus.02  You made room.
hd.focus.03  A week with focus in it.
hd.focus.04  {sessions} sessions.
hd.focus.05  Deliberate hours.
hd.focus.06  Focus held.
hd.focus.07  You sat down {sessions} times.
hd.focus.08  Time was set aside.
hd.focus.09  A week of guarded hours.
hd.focus.10  Attention was defended.
```

## 1.7 personalBest

Trigger: completions exceed every previous week.

```
hd.best.01  Your most productive week.
hd.best.02  A record.
hd.best.03  More than any week before.
hd.best.04  The most you have finished.
hd.best.05  A new high.
hd.best.06  Nothing has come close.
hd.best.07  {n} completions. A first.
hd.best.08  Your best week here.
hd.best.09  Above everything before it.
hd.best.10  A week that stands out.
hd.best.11  Nothing earlier beat it.
hd.best.12  {n} this week, a first.
hd.best.13  No week has topped this.
hd.best.14  The top of the whole record.
hd.best.15  {n} done, a high.
hd.best.16  Nothing above it yet.
hd.best.17  Your highest week here.
hd.best.18  The first time at {n}.
hd.best.19  The record now reads {n}.
hd.best.20  No earlier week did more.
hd.best.21  {n}, and nothing higher.
hd.best.22  The high mark.
hd.best.23  This one is the highest.
hd.best.24  Nothing since you started beats it.
hd.best.25  {n} completions, unmatched.
hd.best.26  Level with the best, or above.
hd.best.27  The highest count so far.
hd.best.28  {n} things finished, a record.
hd.best.29  Nothing earlier reads higher.
hd.best.30  Your ceiling, this week.
hd.best.31  A high point.
hd.best.32  The most in the record.
hd.best.33  {n} finished. A ceiling.
hd.best.34  No week has matched it.
hd.best.35  Top of the list.
hd.best.36  The week that set the mark.
hd.best.37  {n} completions and no better week.
hd.best.38  Nothing before it did more.
hd.best.39  Your best count here.
hd.best.40  The week that sits on top.
hd.best.41  {n}, and nothing over it.
hd.best.42  Nothing has gone higher.
hd.best.43  Higher than every week before it.
hd.best.44  A new mark, {n}.
hd.best.45  No week looks better.
hd.best.46  {n} finished, the most yet.
hd.best.47  Your record, matched or beaten.
hd.best.48  The best week in the record.
hd.best.49  {n} completions at the top.
hd.best.50  The top week so far.
hd.best.51  Nothing higher in the history.
hd.best.52  {n}, unbeaten.
hd.best.53  The most you have done here.
hd.best.54  A week nothing outranks.
hd.best.55  {n} finished, nothing above.
hd.best.56  Nothing better in the record.
hd.best.57  A peak.
hd.best.58  The high week.
hd.best.59  {n} completions, none higher.
hd.best.60  The history has nothing above this.
```

## 1.8 mostActiveSince

Trigger: this week beats every week back to a named point.

```
hd.since.01  Your busiest week since {sinceRef}.
hd.since.02  Nothing like this since {sinceRef}.
hd.since.03  The most since {sinceRef}.
hd.since.04  A return to {sinceRef} form.
hd.since.05  Not since {sinceRef}.
hd.since.06  Back to where you were in {sinceRef}.
hd.since.07  {sinceRef} was the last week like this.
hd.since.08  The strongest since {sinceRef}.
hd.since.09  Nothing since {sinceRef} finished more.
hd.since.10  More than every week since {sinceRef}.
hd.since.11  Higher than anything since {sinceRef}.
hd.since.12  {sinceRef} was the last week above this.
hd.since.13  The last week over this was {sinceRef}.
hd.since.14  Nothing has topped it since {sinceRef}.
hd.since.15  Since {sinceRef}, nothing higher.
hd.since.16  {sinceRef} still holds the bigger week.
hd.since.17  A peak, though not the peak.
hd.since.18  Up on last week.
hd.since.19  The last time was {sinceRef}.
hd.since.20  Your highest week since {sinceRef}.
hd.since.21  The weeks after {sinceRef} were smaller.
hd.since.22  First to reach this since {sinceRef}.
hd.since.23  Above every week since {sinceRef}.
hd.since.24  {sinceRef} finished more. Nothing after it did.
hd.since.25  The recent weeks were smaller.
hd.since.26  A high, with {sinceRef} still higher.
hd.since.27  {sinceRef} holds the nearest week above.
hd.since.28  The last bigger week was in {sinceRef}.
hd.since.29  Nothing has passed this since {sinceRef}.
hd.since.30  Not since {sinceRef} has one been larger.
hd.since.31  A week that reached back to {sinceRef}.
hd.since.32  More finished than any week since {sinceRef}.
hd.since.33  Nothing after {sinceRef} came this high.
hd.since.34  {sinceRef} was the last one like it.
hd.since.35  Larger than every week that followed {sinceRef}.
hd.since.36  The weeks since {sinceRef} finished less.
hd.since.37  The count is up from last week.
hd.since.38  Last week was smaller.
hd.since.39  Last week finished fewer.
hd.since.40  Above last week and the ones before.
hd.since.41  A rise. An old mark still stands.
hd.since.42  The {sinceRef} mark still stands.
hd.since.43  {sinceRef} was the last one bigger.
hd.since.44  Since {sinceRef}, this is the top.
hd.since.45  The weeks in between all finished less.
hd.since.46  Nothing in the weeks since went higher.
hd.since.47  Not one week since {sinceRef} went higher.
hd.since.48  {sinceRef} is where the higher week sits.
hd.since.49  Last beaten in {sinceRef}.
hd.since.50  Above everything after {sinceRef}.
hd.since.51  Last week did less.
hd.since.52  Ahead of every week since {sinceRef}.
hd.since.53  Nothing since has been larger.
hd.since.54  The nearest bigger week is in {sinceRef}.
hd.since.55  The weeks after it were all under.
hd.since.56  This one finished more than last.
hd.since.57  The most in the weeks since {sinceRef}.
hd.since.58  Above all the weeks after {sinceRef}.
hd.since.59  The weeks between were all under it.
hd.since.60  Nothing since {sinceRef} has come out higher.
```

## 1.9 decliningActivity

Trigger: falling activity across three or more weeks. This is the hardest headline to get right and every line here was checked against the mirror test.

```
hd.decline.01  Momentum is fading.
hd.decline.02  Quieter than the last two weeks.
hd.decline.03  Slowing.
hd.decline.04  Less, three weeks running.
hd.decline.05  The pace has been dropping.
hd.decline.06  Third quieter week.
hd.decline.07  A downward stretch.
hd.decline.08  Softening.
hd.decline.09  Less each week.
hd.decline.10  The line is going down.
```

## 1.10 risingActivity

```
hd.rise.01  Picking up.
hd.rise.02  More, three weeks running.
hd.rise.03  Building.
hd.rise.04  The pace is climbing.
hd.rise.05  Third busier week.
hd.rise.06  An upward stretch.
hd.rise.07  More each week.
hd.rise.08  The line is going up.
hd.rise.09  Gathering speed.
hd.rise.10  Steadily more.
```

## 1.11 comeback

Trigger: an area returned after a long dormancy.

```
hd.back.01  {areaName} came back.
hd.back.02  {areaName} moved again.
hd.back.03  Something returned.
hd.back.04  {areaName}, after {ageDays}.
hd.back.05  A revival.
hd.back.06  {areaName} is back.
hd.back.07  The quiet one moved.
hd.back.08  {areaName} woke up.
hd.back.09  {areaName} is moving again.
hd.back.10  {areaName} restarted.
hd.back.11  {ageDays} on, {areaName} moved.
hd.back.12  {areaName} has a week again.
hd.back.13  Motion again, after {ageDays}.
hd.back.14  {areaName} rejoined the week.
hd.back.15  The gap ended.
hd.back.16  {areaName}, once more.
hd.back.17  Still, then moving.
hd.back.18  Something started again.
hd.back.19  {areaName} broke {ageDays} of quiet.
hd.back.20  A quiet area moved.
hd.back.21  {areaName} picked back up.
hd.back.22  The still one moved.
hd.back.23  {areaName} after a gap.
hd.back.24  {ageDays} of nothing, then {areaName}.
hd.back.25  The return of {areaName}.
hd.back.26  {areaName} has activity again.
hd.back.27  Movement in {areaName}.
hd.back.28  {areaName} started over.
hd.back.29  The gap ran {ageDays}.
hd.back.30  {areaName} showed up.
hd.back.31  An area returned.
hd.back.32  {areaName} is not quiet now.
hd.back.33  The quiet broke.
hd.back.34  {areaName} moved, {ageDays} later.
hd.back.35  Nothing, then this.
hd.back.36  {areaName} is in this week.
hd.back.37  A dormant area woke.
hd.back.38  {areaName} resumed.
hd.back.39  What stopped has started.
hd.back.40  {ageDays} ended.
hd.back.41  {areaName} is doing things again.
hd.back.42  Back on the list.
hd.back.43  {areaName} was quiet until now.
hd.back.44  An area found its way back.
hd.back.45  {areaName} reappeared.
hd.back.46  Back in the record: {areaName}.
hd.back.47  The first move in {ageDays}.
hd.back.48  {areaName} closed a {ageDays} gap.
hd.back.49  Something old moved.
hd.back.50  {areaName} counts again.
hd.back.51  The week includes {areaName}.
hd.back.52  After the quiet, {areaName}.
hd.back.53  {areaName} did something.
hd.back.54  A stopped area started.
hd.back.55  {areaName} broke its silence.
hd.back.56  Dormant, then not.
hd.back.57  {ageDays} gone, {areaName} back.
hd.back.58  {areaName} put something in the week.
hd.back.59  A still stretch, over.
hd.back.60  A long quiet, ended.
```

## 1.12 queuePressure

Trigger: queues grew substantially or for consecutive weeks.

```
hd.queue.01  The queues are filling.
hd.queue.02  More waiting than before.
hd.queue.03  Building up.
hd.queue.04  {n} things waiting.
hd.queue.05  The backlog grew.
hd.queue.06  Longer lines.
hd.queue.07  Things are accumulating.
hd.queue.08  The queues took the week.
```

## 1.13 clearing

Trigger: one or more areas fully drained.

```
hd.clear.01  {areaName} is empty.
hd.clear.02  Cleared out.
hd.clear.03  Nothing left waiting.
hd.clear.04  {areaName} finished everything.
hd.clear.05  A clean sweep.
hd.clear.06  Emptied.
hd.clear.07  The queue ran out.
hd.clear.08  All the way through.
```

## 1.14 steadyPace

Trigger: activity within a narrow band of the previous weeks.

```
hd.steady.01  A steady week.
hd.steady.02  Much like last week.
hd.steady.03  Holding pace.
hd.steady.04  Consistent.
hd.steady.05  The same rhythm.
hd.steady.06  Level.
hd.steady.07  No change in tempo.
hd.steady.08  Even, again.
```

## 1.15 fragmented

Trigger: many events, few completions, high switching.

```
hd.frag.01  Many small moves.
hd.frag.02  Busy, not finished.
hd.frag.03  A lot of motion.
hd.frag.04  Movement without endings.
hd.frag.05  Plenty happened. Little closed.
hd.frag.06  Motion, not output.
hd.frag.07  A scattered week.
hd.frag.08  Lots of starts.
```

> **Note.** `hd.frag.06` previously contained a non-Latin character. It has been corrected. The ASCII-plus-standard-punctuation build test exists to catch any recurrence.

## 1.16 firstWeek

```
hd.first.01  Your first week.
hd.first.02  Week one.
hd.first.03  The beginning.
hd.first.04  A start.
hd.first.05  Day one to day seven.
hd.first.06  Just getting going.
```

## 1.17 datedFallback

Never absent. Used when nothing else qualifies.

```
hd.fall.01  Week of {weekRef}.
hd.fall.02  {weekRef}.
hd.fall.03  Seven days from {weekRef}.
hd.fall.04  The week beginning {weekRef}.
```

**Headline totals: 360 lines across 17 families.**

---

# SECTION 2: Your week, honestly

Two to four observations. Each triggered, each traceable. This is the analytical core of the app.

## 2.1 singleFocus

One area holds 80 percent or more of the week's events.

### Stage 1, eighty to eighty nine percent

**Leads**
```
ob.single.s1.l01  [P]  {areaName} held {pct} of everything you did.
ob.single.s1.l02  [P]  {n} of your {m} moves this week were in {areaName}.
ob.single.s1.l03  [O]  Nearly everything this week happened in one area.
ob.single.s1.l04  [O]  {areaName} took most of the week.
ob.single.s1.l05  [O]  Your other areas moved {m} times between them.
ob.single.s1.l06  [O]  The week had one clear center, and it was {areaName}.
ob.single.s1.l07  [E]  This was a {areaName} week, whatever else was on the list.
ob.single.s1.l08  [E]  {areaName} was not one of the things you did. It was the thing.
ob.single.s1.l09  [P]  {pct} of the week was a single area.
ob.single.s1.l10  [O]  {otherArea} and {thirdArea} were largely still while {areaName} moved.
ob.single.s1.l11  [O]  {areaName} held four fifths of the week or more.
ob.single.s1.l12  [O]  Most of what happened was in {areaName}.
ob.single.s1.l13  [O]  {n} moves of the week's {m} belonged to {areaName}.
ob.single.s1.l14  [O]  The week had one area in it, mostly.
ob.single.s1.l15  [O]  {pct} of the week was {areaName}.
ob.single.s1.l16  [O]  {areaName} was where the week went.
ob.single.s1.l17  [O]  Four moves in five or more were in {areaName}.
ob.single.s1.l18  [O]  The week leaned one way.
ob.single.s1.l19  [O]  {n} of the week's moves were {areaName}'s.
ob.single.s1.l20  [O]  One area took the majority of the week.
ob.single.s1.l21  [O]  {areaName} was the week's main subject.
ob.single.s1.l22  [O]  Four in five moves landed in {areaName}.
ob.single.s1.l23  [O]  {areaName} carried {pct} of it.
ob.single.s1.l24  [O]  The week concentrated in {areaName}.
ob.single.s1.l25  [O]  Not everything was {areaName}, but most of it was.
ob.single.s1.l26  [O]  {areaName} has {n} moves against the week's {m}.
ob.single.s1.l27  [O]  Most of seven days pointed at {areaName}.
ob.single.s1.l28  [O]  {pct} of the moves this week were in one area.
ob.single.s1.l29  [O]  {areaName} was busier than everything else together.
ob.single.s1.l30  [O]  What did not happen in {areaName} was a small part of the week.
ob.single.s1.l31  [O]  {areaName} took {n} of the {m} moves.
ob.single.s1.l32  [O]  {areaName} was most of the record for these seven days.
ob.single.s1.l33  [O]  {areaName} held {sessions} focus sessions.
ob.single.s1.l34  [P]  {n} moves in {areaName}.
ob.single.s1.l35  [P]  One area, {pct} of the week.
ob.single.s1.l36  [P]  {m} moves. {n} of them in {areaName}.
ob.single.s1.l37  [P]  {n} in {areaName}, {m} in the week.
ob.single.s1.l38  [P]  Four fifths in one area.
ob.single.s1.l39  [P]  The bulk was {areaName}.
ob.single.s1.l40  [P]  Most moves: {areaName}.
ob.single.s1.l41  [P]  One area, most of the moves.
ob.single.s1.l42  [P]  {sessions} focus sessions in {areaName}.
ob.single.s1.l43  [P]  {n} moves, one area.
ob.single.s1.l44  [P]  {areaName} took four in five.
ob.single.s1.l45  [E]  A week with a main subject is not the same as a week with one subject.
ob.single.s1.l46  [E]  {areaName} did not take the whole week. It took most of it.
ob.single.s1.l47  [E]  A week can be about one thing without being only that thing.
ob.single.s1.l48  [O]  {areaName} holds {pct} of the week, which is most of it and is not all of it.
ob.single.s1.l49  [O]  The week put {n} of its {m} moves in one area, and the areas around it took what was left.
ob.single.s1.l50  [O]  Four moves in five or more happened in {areaName}, and the week still had something outside it.
ob.single.s1.l51  [E]  A week can have a subject without being about nothing else, and this one had {areaName} and a margin.
ob.single.s1.l52  [E]  {pct} in one area is a week with a direction rather than a week with a single occupant.
ob.single.s1.l53  [O]  The other areas did move this week, and what they did came to less than a fifth of it.
ob.single.s1.l54  [E]  Most of a week in one area is a shape before it is an amount, and the shape is what this says.
ob.single.s1.l55  [P]  Most of it in one place.
ob.single.s1.l56  [O]  One area, mostly.
ob.single.s1.l57  [P]  {areaName} took {pct}.
ob.single.s1.l58  [P]  {areaName} and a little else.
ob.single.s1.l59  [P]  {sessions} sessions there.
ob.single.s1.l60  [O]  Nearly one area.
```

**Extensions**
```
ob.single.s1.e01  [O]  That is more concentrated than your average week.
ob.single.s1.e02  [O]  Last week it was {otherPct}.
ob.single.s1.e03  [O]  {n} of those were completions.
ob.single.s1.e04  [O]  Three of your four focus sessions were there too.
ob.single.s1.e05  [E]  Weeks like that are usually deliberate, or they are drift.
ob.single.s1.e06  [O]  {otherArea} has now been quiet for {ageDays}.
ob.single.s1.e07  [E]  Whether that is depth or narrowness depends on what the other areas needed.
ob.single.s1.e08  [O]  The queues elsewhere did not change.
ob.single.s1.e09  [P]  {pct} of the week, in all.
ob.single.s1.e10  [O]  The other areas moved, and less than a fifth of the week was theirs.
ob.single.s1.e11  [E]  Whether that was chosen or arrived at is not in the record.
ob.single.s1.e12  [O]  {sessions} focus sessions there too.
ob.single.s1.e13  [P]  {n} of {m} moves.
ob.single.s1.e14  [O]  The rest of the week was somewhere else.
ob.single.s1.e15  [E]  A week with a center is not a week with only one thing in it, and this one had a margin outside {areaName}.
ob.single.s1.e16  [O]  The week outside {areaName} was smaller than the week inside it.
ob.single.s1.e17  [P]  {m} moves in total.
ob.single.s1.e18  [O]  The areas that are not {areaName} were still there and still moved this week.
ob.single.s1.e19  [O]  {areaName} was not the only area with something in it.
ob.single.s1.e20  [O]  How much of that was one long piece of work and how much was many small ones is not something the counts separate.
```

### Stage 2, ninety percent and above

**Leads**
```
ob.single.s2.l01  [P]  {pct} of this week was {areaName}.
ob.single.s2.l02  [P]  {m} things happened outside {areaName}. All week.
ob.single.s2.l03  [O]  This week was almost entirely one area.
ob.single.s2.l04  [O]  Everything except {m} moves was {areaName}.
ob.single.s2.l05  [E]  For seven days, the app had one subject.
ob.single.s2.l06  [E]  {areaName} did not dominate the week. It was the week.
ob.single.s2.l07  [O]  Your other areas registered almost nothing.
ob.single.s2.l08  [P]  {n} of {m} moves, one area.
ob.single.s2.l09  [E]  A week this narrow is either a sprint or a blind spot.
ob.single.s2.l10  [O]  Nothing outside {areaName} was completed.
ob.single.s2.l11  [N]  {pct} of the week landed in one area.
ob.single.s2.l12  [N]  {m} things happened anywhere else.
ob.single.s2.l13  [N]  Everything except {m} moves belonged to {areaName}.
ob.single.s2.l14  [P]  {pct}, one area.                                  [S]
ob.single.s2.l15  [N]  One area. All week.                               [S]
ob.single.s2.l16  [N]  Nine moves in ten or more were {areaName}.
ob.single.s2.l17  [N]  One area holds {pct} of the record for this week.
ob.single.s2.l18  [N]  {n} of {m} moves were in {areaName}.
ob.single.s2.l19  [N]  The week has one area in it.
ob.single.s2.l20  [N]  {areaName} is almost the whole week.
ob.single.s2.l21  [N]  Almost nothing happened outside {areaName}.
ob.single.s2.l22  [N]  The week and {areaName} are nearly the same thing.
ob.single.s2.l23  [N]  {pct} in one place.
ob.single.s2.l24  [N]  Nine tenths of the week or more sits in {areaName}.
ob.single.s2.l25  [N]  {areaName} covers all but a tenth of the week.
ob.single.s2.l26  [N]  The record for this week is {areaName}. What is not {areaName} would fit inside a tenth of it.
ob.single.s2.l27  [N]  {n} moves in {areaName}, out of {m} altogether.
ob.single.s2.l28  [N]  Almost the entire week is one area.
ob.single.s2.l29  [N]  The week has a single occupant.
ob.single.s2.l30  [N]  The rest is a tenth at most.
ob.single.s2.l31  [N]  Everything but a fraction was {areaName}.
ob.single.s2.l32  [N]  The week reads as one area.
ob.single.s2.l33  [N]  {areaName} is {pct} of what happened.
ob.single.s2.l34  [N]  Outside {areaName} the week is nearly empty.
ob.single.s2.l35  [N]  One area, {pct}, seven days.
ob.single.s2.l36  [N]  The week did not spread.
ob.single.s2.l37  [N]  {areaName} took nine tenths of it or more.
ob.single.s2.l38  [N]  Nothing much sits outside {areaName}.
ob.single.s2.l39  [N]  {m} moves in the week. {n} of them in {areaName}.
ob.single.s2.l40  [O]  Almost everything this week was in one area.
ob.single.s2.l41  [O]  The week has one area and a remainder.
ob.single.s2.l42  [O]  Nine tenths of the week or more went to {areaName}.
ob.single.s2.l43  [O]  {areaName} is nearly all of what the week holds.
ob.single.s2.l44  [O]  Outside {areaName} there is barely a week at all.
ob.single.s2.l45  [O]  One area took all but a tenth of the moves.
ob.single.s2.l46  [O]  This week is {areaName} and a margin.
ob.single.s2.l47  [O]  The record has one busy area.
ob.single.s2.l48  [O]  {pct} of seven days was a single area.
ob.single.s2.l49  [O]  Everything else together comes to under a tenth.
ob.single.s2.l50  [O]  The week has one area and almost no second one.
ob.single.s2.l51  [O]  {areaName} holds {pct} of it, which leaves very little anywhere else.
ob.single.s2.l52  [P]  {areaName}, almost all of it.
ob.single.s2.l53  [P]  {n} moves there, {m} in the week.
ob.single.s2.l54  [E]  {pct} is what the days came to rather than what was chosen.
ob.single.s2.l55  [E]  A week can be this narrow because one thing needed it or because nothing else was asked for.
ob.single.s2.l56  [E]  There is only one area in this report because there was only one area in the week.
ob.single.s2.l57  [E]  What a week this concentrated does not show is what the other areas were waiting for.
ob.single.s2.l58  [E]  {areaName} at {pct} describes this week and says nothing about the year.
ob.single.s2.l59  [P]  One area, all but a tenth.
ob.single.s2.l60  [N]  What is not {areaName} is a tenth of the week or less.
```

**Extensions**
```
ob.single.s2.e01  [O]  That is the most concentrated week you have had.
ob.single.s2.e02  [O]  The previous high was {otherPct}, in {sinceRef}.
ob.single.s2.e03  [O]  {otherArea} has not moved in {ageDays}.
ob.single.s2.e04  [E]  Sprints end. Blind spots do not, until something forces them to.
ob.single.s2.e05  [O]  Two other areas still hold active items that did not move.
ob.single.s2.e06  [O]  All {sessions} focus sessions were in {areaName}.
ob.single.s2.e07  [E]  It is worth knowing whether you chose this or arrived at it.
ob.single.s2.e08  [N]  {pct} of it in one place.
ob.single.s2.e09  [P]  {n} of {m} moves.
ob.single.s2.e10  [O]  The other areas are still there, whatever the share says.
ob.single.s2.e11  [N]  What happened outside {areaName} rounds to very little.
ob.single.s2.e12  [E]  A number this high is a description of seven days and nothing longer.
ob.single.s2.e13  [P]  {m} moves in seven days, {n} of them in {areaName}.
ob.single.s2.e14  [N]  The remainder is small.
ob.single.s2.e15  [O]  Whether the other areas needed anything is not measured here.
ob.single.s2.e16  [N]  The week has one area with almost everything in it and the rest of the areas hold what is left over.
ob.single.s2.e17  [E]  A week can be narrow because one thing was worth it or because nothing else came up, and this does not say which.
ob.single.s2.e18  [P]  {sessions} sessions, one area.
ob.single.s2.e19  [O]  Almost nothing elsewhere.
ob.single.s2.e20  [N]  Under a tenth elsewhere.
```

## 2.2 intakeVsOutput

The gap between what was added and what was finished.

### Stage 1, mild imbalance, gap of two to four

**Leads**
```
ob.flow.s1.l01  [P]  You added {n} things and finished {m}.
ob.flow.s1.l02  [P]  {n} in, {m} out.
ob.flow.s1.l03  [O]  A little more arrived than left.
ob.flow.s1.l04  [O]  The queues are {k} longer than they were on Sunday.
ob.flow.s1.l05  [O]  Intake edged ahead of output.
ob.flow.s1.l06  [E]  The week was slightly better at noticing than finishing.
ob.flow.s1.l07  [P]  Your queues gained {k} this week.
ob.flow.s1.l08  [O]  {n} additions against {m} completions.
ob.flow.s1.l09  [N]  {n} arrived and {m} left, a gap of {k}.
ob.flow.s1.l10  [N]  The queues ended the week {k} longer than they started it.
ob.flow.s1.l11  [E]  The gap is small enough that another week could close it or widen it.
ob.flow.s1.l12  [N]  {n} came in. {m} went out.
ob.flow.s1.l13  [N]  Two counts, a few apart.
ob.flow.s1.l14  [N]  {n} against {m}.
ob.flow.s1.l15  [N]  The queues ended {k} longer.
ob.flow.s1.l16  [N]  {n} one way, {m} the other.
ob.flow.s1.l17  [N]  Two numbers, four apart at most.
ob.flow.s1.l18  [N]  The lists took on {k} more.
ob.flow.s1.l19  [N]  Both directions had something in them.
ob.flow.s1.l20  [N]  Intake and output finished within four things of each other.
ob.flow.s1.l21  [N]  The week ends with a difference of a few things in it.
ob.flow.s1.l22  [N]  Neither side of the week ran away with the other.
ob.flow.s1.l23  [N]  The counts are close enough that one day would move them.
ob.flow.s1.l24  [N]  {n} things came in and {m} went out over seven days.
ob.flow.s1.l25  [N]  The queues carry {k} more than they did on the first day.
ob.flow.s1.l26  [N]  The week moved things both ways and ended a little uneven.
ob.flow.s1.l27  [N]  {n} things came in over seven days and {m} went out over the same seven, which leaves a few things between them.
ob.flow.s1.l28  [N]  The queues hold {k} more than they held when the week opened, which is what a gap of this size looks like.
ob.flow.s1.l29  [N]  {n} things arrived across the week and {m} left it, and the queues ended {k} longer than they started.
ob.flow.s1.l30  [N]  Seven days, {n} things in, {m} things out, and a difference small enough to sit inside one of them.
ob.flow.s1.l31  [N]  The queues closed the week {k} longer than they opened it, over seven days that held {n} additions.
ob.flow.s1.l32  [O]  The week nearly evened out.
ob.flow.s1.l33  [O]  Both columns have a number in them.
ob.flow.s1.l34  [O]  Something went out this week too.
ob.flow.s1.l35  [O]  A small difference, in one direction.
ob.flow.s1.l36  [O]  Two numbers, one small gap.
ob.flow.s1.l37  [O]  Intake and output stayed near each other this week.
ob.flow.s1.l38  [O]  Your queues are {k} longer than on day one.
ob.flow.s1.l39  [O]  The two sides of the week ended a few things apart.
ob.flow.s1.l40  [O]  {n} things joined the lists and {m} came off them.
ob.flow.s1.l41  [O]  The list is a little longer than it was, by {k}.
ob.flow.s1.l42  [O]  Whichever way the week leaned, it did not lean far.
ob.flow.s1.l43  [O]  The week added to the lists and took from them.
ob.flow.s1.l44  [O]  There is a gap between what arrived and what left, and it runs to a few things.
ob.flow.s1.l45  [O]  {n} things went onto the lists this week and {m} came off them over the same seven days.
ob.flow.s1.l46  [P]  {m} finished. {n} arrived.
ob.flow.s1.l47  [P]  A gap of a few things.
ob.flow.s1.l48  [P]  {k} more waiting than on Sunday.
ob.flow.s1.l49  [P]  Seven days, {n} and {m}.
ob.flow.s1.l50  [P]  {n} things added, {m} things gone.
ob.flow.s1.l51  [P]  The week in two numbers.
ob.flow.s1.l52  [P]  The list is {k} longer than it started.
ob.flow.s1.l53  [P]  Across the week, {n} things in and {m} out.
ob.flow.s1.l54  [P]  The two counts for the week are {n} and {m}.
ob.flow.s1.l55  [P]  The additions came to {n} and the completions to {m}, and the two are within four things of each other.
ob.flow.s1.l56  [P]  The queues gained {k} things over the week, and the week itself added {n} things and finished {m}.
ob.flow.s1.l57  [E]  A gap this size is one afternoon.
ob.flow.s1.l58  [E]  The week leaned, and a lean is not a slide.
ob.flow.s1.l59  [E]  A gap of a few things is the kind a single quiet day makes and a single busy one removes.
ob.flow.s1.l60  [E]  Adding and finishing are different acts, and this week the two of them came out within a few things of each other.
```

**Extensions**
```
ob.flow.s1.e01  [O]  Most of the new items went to {areaName}.
ob.flow.s1.e02  [O]  Last week the balance went the other way.
ob.flow.s1.e03  [O]  {k} of what you added is still untouched.
ob.flow.s1.e04  [E]  A small gap one week is noise. Three weeks is a pattern.
ob.flow.s1.e05  [O]  Nothing added on {dayName} has moved yet.
ob.flow.s1.e06  [P]  {areaName} was the busiest area of the week, which says where the moves were and not which way they went.
ob.flow.s1.e07  [P]  Seven days produced both numbers.
ob.flow.s1.e08  [P]  Both numbers came out of the same seven days.
ob.flow.s1.e09  [O]  {areaName} moved more than anywhere else this week.
ob.flow.s1.e10  [O]  A matter of a few things.
ob.flow.s1.e11  [O]  One more week would tell.
ob.flow.s1.e12  [O]  That is a difference of two, three or four things.
ob.flow.s1.e13  [O]  Seven days made it.
ob.flow.s1.e14  [E]  A gap is a fact about two counts and about the seven days that produced them, and about nothing else.
ob.flow.s1.e15  [E]  Weeks rarely land level, and a week that misses by three is not a different kind of week from one that lands.
ob.flow.s1.e16  [E]  Either count could have been the larger.
ob.flow.s1.e17  [E]  A difference this small says more about seven days than about a habit.
ob.flow.s1.e18  [O]  Four things is the widest this goes.
ob.flow.s1.e19  [P]  The week finished {m}.
ob.flow.s1.e20  [P]  The week added {n}.
```

### Stage 2, clear imbalance toward intake

**Leads**
```
ob.flow.s2.l01  [P]  You added {n} things and finished {m}.
ob.flow.s2.l02  [O]  Considerably more arrived than left this week.
ob.flow.s2.l03  [O]  The queues are {k} longer than they were on Sunday.
ob.flow.s2.l04  [E]  This was a week of collecting rather than closing.
ob.flow.s2.l05  [P]  {n} added, {m} completed, {k} net.
ob.flow.s2.l06  [O]  Nothing you added this week has been finished.
ob.flow.s2.l07  [O]  {areaName} took {k} of the new items.
ob.flow.s2.l08  [E]  The list grew faster than you could work it.
ob.flow.s2.l09  [O]  Your queues now hold {n} things.
ob.flow.s2.l10  [N]  {n} things arrived. {m} left.
ob.flow.s2.l11  [N]  The queues took on {n} and released {m}.
ob.flow.s2.l12  [N]  {k} more things are waiting than were on Sunday.
ob.flow.s2.l13  [N]  Arrivals outpaced departures by {k}.
ob.flow.s2.l14  [N]  Nothing added this week has left yet.
ob.flow.s2.l15  [P]  {n} in, {m} out.                                    [S]
ob.flow.s2.l16  [N]  {k} net, into the queues.                           [S]
ob.flow.s2.l17  [N]  More came in than went out, by five or more.
ob.flow.s2.l18  [N]  {n} in, {m} out, and {k} added to the queues.
ob.flow.s2.l19  [N]  The queues are carrying {k} more than they were.
ob.flow.s2.l20  [N]  The lists grew.
ob.flow.s2.l21  [N]  {n} arrived.
ob.flow.s2.l22  [N]  Five apart, at least.
ob.flow.s2.l23  [N]  The week collected more than it closed.
ob.flow.s2.l24  [N]  Intake ran ahead of output.
ob.flow.s2.l25  [N]  {k} more waiting than at the start.
ob.flow.s2.l26  [N]  The queues took on more than they let go.
ob.flow.s2.l27  [N]  What arrived outnumbered what left.
ob.flow.s2.l28  [N]  The list is longer than it was.
ob.flow.s2.l29  [N]  A collecting week.
ob.flow.s2.l30  [N]  More in than out.
ob.flow.s2.l31  [N]  {n} things came in over seven days and the lists are {k} longer for it.
ob.flow.s2.l32  [N]  {n} things came in this week, {m} went out, and the queues ended {k} longer than they opened.
ob.flow.s2.l33  [N]  The week took in {n} things and released {m}, and the difference between those two runs to five or more.
ob.flow.s2.l34  [N]  The queues hold {k} more things than they held on the first day of the week.
ob.flow.s2.l35  [N]  Five or more things separate what arrived this week from what left it, and seven days is where both numbers came from.
ob.flow.s2.l36  [O]  Your queues took on {k} things this week.
ob.flow.s2.l37  [O]  More arrived than left this week by a clear margin.
ob.flow.s2.l38  [O]  Collecting ran ahead of finishing.
ob.flow.s2.l39  [O]  You noticed more than you closed.
ob.flow.s2.l40  [O]  {n} things went onto the lists and {m} came off.
ob.flow.s2.l41  [O]  The lists are longer than they were on Sunday.
ob.flow.s2.l42  [O]  This was a week of adding.
ob.flow.s2.l43  [O]  The week ran one way.
ob.flow.s2.l44  [O]  {n} things arrived over seven days, which is more than left in the same seven.
ob.flow.s2.l45  [O]  The queues are {k} longer than they were when the week opened.
ob.flow.s2.l46  [O]  Five things or more sit between the two counts, and seven days is all it took to put them there.
ob.flow.s2.l47  [O]  The week put things onto the lists faster than it took them off, and the lists are longer for it.
ob.flow.s2.l48  [O]  What the week collected outweighed what it finished, and the distance between the two is five things or more.
ob.flow.s2.l49  [P]  {n} in, {m} out, {k} waiting.
ob.flow.s2.l50  [P]  {n} things added this week.
ob.flow.s2.l51  [P]  The queues gained {k} things.
ob.flow.s2.l52  [P]  {n} additions. {m} completions.
ob.flow.s2.l53  [P]  Five things or more, one direction.
ob.flow.s2.l54  [P]  {n} added over seven days, {m} finished.
ob.flow.s2.l55  [P]  The queues opened the week shorter than they closed it, by {k} things.
ob.flow.s2.l56  [P]  {n} things came in, {m} went out, and the queues ended the week {k} longer than they began it.
ob.flow.s2.l57  [P]  Seven days, {n} things in and {m} out, and {k} more waiting than there was.
ob.flow.s2.l58  [E]  A week that noticed more than it finished.
ob.flow.s2.l59  [E]  A list is where a thing goes when it has been noticed and not yet done, and {n} things went there this week.
ob.flow.s2.l60  [E]  Five things is a small number and a wide gap, and it is both at once.
```

**Extensions**
```
ob.flow.s2.e01  [O]  That is the widest gap in {sinceRef}.
ob.flow.s2.e02  [O]  The queues have grown three weeks running.
ob.flow.s2.e03  [O]  {k} of what you added is in {areaName}, which already held {n}.
ob.flow.s2.e04  [E]  Capture is useful right up until the point the queue stops being read.
ob.flow.s2.e05  [O]  Two areas are now holding more than they were a month ago.
ob.flow.s2.e06  [E]  Adding is easier than finishing, and it feels similar at the time.
ob.flow.s2.e07  [P]  {areaName} led the week.
ob.flow.s2.e08  [P]  The queues hold {k} more.
ob.flow.s2.e09  [P]  Seven days, {n} additions.
ob.flow.s2.e10  [P]  The gap is five things or more.
ob.flow.s2.e11  [O]  The lists are longer for it.
ob.flow.s2.e12  [O]  {areaName} moved more than any other area this week.
ob.flow.s2.e13  [O]  It did not close by Sunday.
ob.flow.s2.e14  [O]  Nothing here says how long any of it will wait, or whether any of it needs to be waited on at all.
ob.flow.s2.e15  [O]  Five is the point at which this reads as a direction rather than a wobble.
ob.flow.s2.e16  [E]  A week is long enough to add things to a list and short enough not to finish them, and this one did both.
ob.flow.s2.e17  [E]  A queue is a decision kept.
ob.flow.s2.e18  [E]  Collecting and closing use different hours.
ob.flow.s2.e19  [O]  The gap between the two counts is at least five things, and it is the only direction the week ran in.
ob.flow.s2.e20  [E]  A list that grows is a list being used, and a list that only grows is a different thing.
```

### Stage 3, clear imbalance toward output

**Leads**
```
ob.flow.s3.l01  [P]  You finished {n} things and added {m}.
ob.flow.s3.l02  [O]  Considerably more left than arrived this week.
ob.flow.s3.l03  [O]  The queues are {k} shorter than they were on Sunday.
ob.flow.s3.l04  [E]  This was a week of closing rather than collecting.
ob.flow.s3.l05  [P]  {n} completed against {m} added.
ob.flow.s3.l06  [O]  {areaName} cleared {k} of them.
ob.flow.s3.l07  [E]  The list got lighter, and it stayed lighter.
ob.flow.s3.l08  [O]  Your queues now hold {n} things.
ob.flow.s3.l09  [P]  {n} out, {m} in.                                    [S]
ob.flow.s3.l10  [P]  The list got shorter.                               [S]
ob.flow.s3.l11  [O]  More things left than came in.
ob.flow.s3.l12  [O]  The lists got shorter.
ob.flow.s3.l13  [O]  Output ran ahead of intake.
ob.flow.s3.l14  [O]  Five things or more, the other way.
ob.flow.s3.l15  [O]  {n} things came off the lists and {m} went on.
ob.flow.s3.l16  [O]  The week ended with less waiting than it started with.
ob.flow.s3.l17  [O]  Your queues gave up {k} things this week.
ob.flow.s3.l18  [O]  The week finished more than it took in.
ob.flow.s3.l19  [O]  Closing ran ahead of collecting.
ob.flow.s3.l20  [O]  {n} finished against {m} added over seven days.
ob.flow.s3.l21  [O]  The lists are shorter at the end of the week than at the start.
ob.flow.s3.l22  [O]  Five things or more went the way of finishing.
ob.flow.s3.l23  [O]  The week spent itself on what was already there.
ob.flow.s3.l24  [O]  {n} things left the lists over seven days.
ob.flow.s3.l25  [O]  A finishing week.
ob.flow.s3.l26  [O]  More went out than came in.
ob.flow.s3.l27  [O]  The queues carry {k} fewer things than they did.
ob.flow.s3.l28  [O]  What left outnumbered what arrived.
ob.flow.s3.l29  [O]  The week closed more than it opened.
ob.flow.s3.l30  [O]  The week ran one way, and it was out.
ob.flow.s3.l31  [O]  Seven days of taking things off lists rather than putting them on.
ob.flow.s3.l32  [O]  {n} things went, {m} came, and the queues are {k} shorter for it.
ob.flow.s3.l33  [O]  The queues ended the week {k} shorter than they began it, over seven days that added {m} things.
ob.flow.s3.l34  [O]  More came off the lists this week than went onto them, and the difference runs to five things or more.
ob.flow.s3.l35  [O]  {n} things finished over seven days and {m} arrived in the same seven, which leaves the lists shorter than they were.
ob.flow.s3.l36  [O]  Five things or more separate what left this week from what arrived, and every one of them came off a list.
ob.flow.s3.l37  [P]  {n} gone, {m} arrived.
ob.flow.s3.l38  [P]  The lists are {k} shorter.
ob.flow.s3.l39  [P]  {n} completions this week.
ob.flow.s3.l40  [P]  {k} fewer waiting than on Sunday.
ob.flow.s3.l41  [P]  The queues gave up {k}.
ob.flow.s3.l42  [P]  {n} things left. {m} came in.
ob.flow.s3.l43  [P]  The week put {n} things away and took {m} on.
ob.flow.s3.l44  [P]  Across seven days, {n} out and {m} in.
ob.flow.s3.l45  [P]  The queues are {k} shorter than when the week opened.
ob.flow.s3.l46  [P]  {n} completions against {m} additions.
ob.flow.s3.l47  [P]  The list ends the week {k} shorter.
ob.flow.s3.l48  [P]  {n} things came off the lists and {m} went on over seven days.
ob.flow.s3.l49  [P]  Five things or more separate the two counts.
ob.flow.s3.l50  [P]  The week's two numbers are {n} and {m}.
ob.flow.s3.l51  [P]  {n} out and {m} in over the same seven days.
ob.flow.s3.l52  [P]  Less is waiting.
ob.flow.s3.l53  [P]  {k} things fewer than at the start of the week.
ob.flow.s3.l54  [P]  {n} things finished over seven days, {m} arrived, and the queues ended {k} shorter than they started.
ob.flow.s3.l55  [P]  Seven days, {n} things finished and {m} added, and the lists {k} shorter for it.
ob.flow.s3.l56  [P]  The queues held more on the first day of the week than on the last, by {k} things.
ob.flow.s3.l57  [E]  The week emptied rather than filled.
ob.flow.s3.l58  [E]  Finishing is slower than adding, and this week did more of the slower thing.
ob.flow.s3.l59  [E]  A list that shortens is a week's worth of decisions already made.
ob.flow.s3.l60  [E]  A shorter list at the end of a week is the visible half of a lot of small decisions.
```

**Extensions**
```
ob.flow.s3.e01  [O]  That is the strongest net week since {sinceRef}.
ob.flow.s3.e02  [O]  Output has beaten intake three weeks running.
ob.flow.s3.e03  [O]  {areaName} is now empty.
ob.flow.s3.e04  [E]  Weeks like this are either a clear out or the end of a backlog.
ob.flow.s3.e05  [O]  {m} of the completions had been waiting more than a fortnight.
ob.flow.s3.e06  [P]  {n} came off the lists.
ob.flow.s3.e07  [P]  {areaName} was the busiest of them.
ob.flow.s3.e08  [P]  The queues are {k} shorter.
ob.flow.s3.e09  [P]  Five things or more of a difference.
ob.flow.s3.e10  [O]  The lists are shorter for it.
ob.flow.s3.e11  [O]  That is the direction the week ran in.
ob.flow.s3.e12  [O]  Fewer things went on than came off.
ob.flow.s3.e13  [O]  Seven days did that.
ob.flow.s3.e14  [O]  The difference is at least five things, and it went one way.
ob.flow.s3.e15  [O]  {areaName} did more of it than any other area.
ob.flow.s3.e16  [E]  A shorter list is the only thing finishing leaves behind.
ob.flow.s3.e17  [E]  Finishing leaves less evidence behind than adding does, and a shorter list is the whole of what it leaves.
ob.flow.s3.e18  [E]  An empty space on a list is a thing that used to be a decision.
ob.flow.s3.e19  [O]  The queues are shorter than they were at the start of the week, and seven days is what did it.
ob.flow.s3.e20  [P]  Seven days, {n} finished and {m} added, {k} off the queues.
```

## 2.3 focusInvestment

Focus sessions and minutes.

### Stage 1, one to three sessions

**Leads**
```
ob.focus.s1.l01  [P]  {sessions} focus sessions, {minutes} minutes.
ob.focus.s1.l02  [P]  You sat down for focused time {sessions} times.
ob.focus.s1.l03  [O]  {minutes} minutes were set aside deliberately.
ob.focus.s1.l04  [O]  Most of that time went to {areaName}.
ob.focus.s1.l05  [E]  A few hours were protected rather than absorbed.
ob.focus.s1.l06  [P]  {minutes} minutes of focus, all in {areaName}.
```

**Extensions**
```
ob.focus.s1.e01  [O]  All of it on {dayName}.
ob.focus.s1.e02  [O]  More than last week, which had {m}.
ob.focus.s1.e03  [O]  Two of those ended early.
ob.focus.s1.e04  [E]  Small amounts of protected time still count as protected.
ob.focus.s1.e05  [O]  {areaName} completed {n} things in the same window.
```

### Stage 2, four to seven sessions

**Leads**
```
ob.focus.s2.l01  [P]  {sessions} focus sessions this week, {minutes} minutes.
ob.focus.s2.l02  [O]  You protected {minutes} minutes across {sessions} sittings.
ob.focus.s2.l03  [O]  Focus time went mostly to {areaName}.
ob.focus.s2.l04  [E]  This was a week where time was defended rather than spent.
ob.focus.s2.l05  [P]  {minutes} minutes, {sessions} sessions, mostly {areaName}.
ob.focus.s2.l06  [O]  Focused time appeared on {n} different days.
ob.focus.s2.l07  [E]  Nearly every day had a window in it.
```

**Extensions**
```
ob.focus.s2.e01  [O]  That is your highest since {sinceRef}.
ob.focus.s2.e02  [O]  {n} of them were in the morning.
ob.focus.s2.e03  [O]  Every one of them finished.
ob.focus.s2.e04  [O]  {m} ended early, which is normal.
ob.focus.s2.e05  [E]  A habit shows up as a shape on the week, and this week has one.
ob.focus.s2.e06  [O]  {areaName} completed {n} things across those sessions.
```

### Stage 3, eight or more sessions

**Leads**
```
ob.focus.s3.l01  [P]  {sessions} focus sessions, {minutes} minutes.
ob.focus.s3.l02  [O]  That is more focused time than any week before this one.
ob.focus.s3.l03  [E]  Focus stopped being an event this week and became a routine.
ob.focus.s3.l04  [O]  You protected time on {n} of the seven days.
ob.focus.s3.l05  [P]  {minutes} minutes across {sessions} sittings.
ob.focus.s3.l06  [E]  Whatever else the week was, it was deliberate.
```

**Extensions**
```
ob.focus.s3.e01  [O]  The previous high was {m} minutes, in {sinceRef}.
ob.focus.s3.e02  [O]  {n} sessions were in {areaName} alone.
ob.focus.s3.e03  [E]  Weeks this consistent are usually built, not stumbled into.
ob.focus.s3.e04  [O]  Every session finished.
ob.focus.s3.e05  [O]  Completions rose alongside it, from {m} to {n}.
```

## 2.4 neglectedArea

An area with real history that has gone silent. Never fires on a new area.

### Stage 1, seven to thirteen days

**Leads**
```
ob.neg.s1.l01  [P]  {areaName} had no activity this week.
ob.neg.s1.l02  [P]  Nothing happened in {areaName} for {ageDays}.
ob.neg.s1.l03  [O]  {areaName} still holds an active item that did not move.
ob.neg.s1.l04  [O]  {areaName} sat out the week.
ob.neg.s1.l05  [E]  {areaName} is still on the list. It just was not part of the week.
ob.neg.s1.l06  [O]  {itemTitle} has been active in {areaName} and untouched for {ageDays}.
ob.neg.s1.l07  [P]  {areaName}: no events in {ageDays}.
ob.neg.s1.l08  [N]  {ageDays} passed in {areaName} with nothing in them.
ob.neg.s1.l09  [N]  {ageDays} without an event in {areaName}.               [S]
ob.neg.s1.l10  [E]  One quiet week is a week. It is not yet a shape.
```

**Extensions**
```
ob.neg.s1.e01  [O]  It has {n} things waiting behind that.
ob.neg.s1.e02  [O]  Before this, it had moved every week since {sinceRef}.
ob.neg.s1.e03  [E]  A quiet week in one area is normal. Two starts to mean something.
ob.neg.s1.e04  [O]  {otherArea} took most of the attention instead.
ob.neg.s1.e05  [O]  Its last completion was {ageDays} ago.
```

### Stage 2, fourteen or more days

**Leads**
```
ob.neg.s2.l01  [P]  {areaName} has had no activity in {ageDays}.
ob.neg.s2.l02  [O]  {areaName} has been still since {sinceRef}.
ob.neg.s2.l03  [E]  {areaName} is not on pause. It has stopped.
ob.neg.s2.l04  [O]  {itemTitle} has been active in {areaName} and untouched for {ageDays}.
ob.neg.s2.l05  [O]  {areaName} holds {n} things and has not moved in {ageDays}.
ob.neg.s2.l06  [E]  There is an area here that exists mainly as a heading.
ob.neg.s2.l07  [P]  Nothing in {areaName} since {sinceRef}.
ob.neg.s2.l08  [N]  {ageDays} have passed without anything moving in {areaName}.
ob.neg.s2.l09  [N]  {areaName} has been still since {sinceRef}.
ob.neg.s2.l10  [N]  {n} things sit in {areaName}, untouched.
ob.neg.s2.l11  [P]  {areaName}: nothing, {ageDays}.                      [S]
```

**Extensions**
```
ob.neg.s2.e01  [O]  It was your second busiest area in {sinceRef}.
ob.neg.s2.e02  [O]  {n} things are waiting in it.
ob.neg.s2.e03  [E]  An area with no activity is either finished, paused, or over.
ob.neg.s2.e04  [O]  The rest of your areas all moved this week.
ob.neg.s2.e05  [E]  Archiving it would be a decision. Leaving it is also a decision.
```

## 2.5 completionSplit

**Callback family.** Requires three or more answered pulses in the window. This is where the Report demonstrates that it listened.

**Leads**
```
ob.split.l01  [P]  You called {n} of this week's completions momentum, and {m} clearing out.
ob.split.l02  [O]  Of the {n} pulses you answered, {m} pointed at momentum.
ob.split.l03  [O]  Your own read on the week was mostly {priorLabel}.
ob.split.l04  [E]  When asked, you described the week as {priorLabel} more often than not.
ob.split.l05  [P]  {n} answers this week: {m} positive, {k} not.
ob.split.l06  [O]  You said {priorLabel} {priorCount} times.
ob.split.l07  [E]  The week felt like {priorLabel} to you at the time.
ob.split.l08  [O]  You answered {n} pulses this week.
ob.split.l09  [O]  {n} answers came back this week.
ob.split.l10  [O]  {priorLabel} came up {priorCount} times.
ob.split.l11  [O]  Of {n} answers, {m} were the positive one.
ob.split.l12  [O]  {m} of your answers were positive.
ob.split.l13  [O]  The week produced {n} answers.
ob.split.l14  [O]  {priorLabel} was the answer you gave most.
ob.split.l15  [O]  {n} readings, given as you went.
ob.split.l16  [O]  The app asked and you answered, {n} times.
ob.split.l17  [O]  {n} answers, something finished.
ob.split.l18  [O]  Your own read on the week is in the record {n} times.
ob.split.l19  [O]  {m} positive answers and {k} of the other kind.
ob.split.l20  [O]  {priorLabel} is what you said most often this week.
ob.split.l21  [O]  The record holds {n} answers.
ob.split.l22  [O]  {priorCount} of your {n} answers were {priorLabel}.
ob.split.l23  [O]  Some things finished this week and you had {n} things to say about it.
ob.split.l24  [O]  Your answers this week came to {n}, of which {m} were the positive option.
ob.split.l25  [O]  What you said at the time is on record: {n} answers this week.
ob.split.l26  [O]  You answered {n} pulses and something got finished in the same seven days.
ob.split.l27  [O]  You said {priorLabel} {priorCount} times this week, out of {n} answers in total.
ob.split.l28  [O]  The app asked about your days as they went, and you answered {n} times over the seven of them.
ob.split.l29  [O]  You answered {n} times this week, said {priorLabel} {priorCount} of them, and finished at least one thing.
ob.split.l30  [O]  Across the week you gave {n} answers. {m} of them were positive and {k} were not.
ob.split.l31  [O]  {n} answers this week, {priorCount} of them {priorLabel}, and the week had completions in it.
ob.split.l32  [O]  Of the {n} answers you gave this week, {m} took the positive option and {k} took the other.
ob.split.l33  [P]  {n} responses.
ob.split.l34  [P]  {m} positive, {k} not.
ob.split.l35  [P]  {priorLabel} {priorCount} times over.
ob.split.l36  [P]  {n} this week.
ob.split.l37  [P]  {priorCount} of them {priorLabel}.
ob.split.l38  [P]  {m} of one kind, {k} of another.
ob.split.l39  [P]  {n} answers on record.
ob.split.l40  [P]  {m} of them positive.
ob.split.l41  [P]  You answered {n} times.
ob.split.l42  [P]  The split was {m} and {k}.
ob.split.l43  [P]  {n}, {m} positive, {k} flagged.
ob.split.l44  [P]  {n} answers this week, {priorCount} of them the same.
ob.split.l45  [P]  You answered {n} of the app's questions this week.
ob.split.l46  [P]  {m} of the {n} answers were the positive option.
ob.split.l47  [P]  The most given answer was {priorLabel}, {priorCount} times.
ob.split.l48  [P]  Your answers this week: {n} in total, {priorCount} of them {priorLabel}.
ob.split.l49  [P]  {n} answers and at least one completion this week.
ob.split.l50  [P]  Out of {n} answers this week, {m} were positive.
ob.split.l51  [P]  {priorLabel} came up {priorCount} times among the {n} answers you gave this week.
ob.split.l52  [P]  You gave {n} answers over the seven days. {priorLabel} was the one you gave most.
ob.split.l53  [P]  {n} answers this week: {m} of them took the positive option and {k} took the other one.
ob.split.l54  [P]  The week holds {n} answers, one of which you gave {priorCount} times, and it was {priorLabel}.
ob.split.l55  [E]  An answer is a reading, not a verdict.
ob.split.l56  [E]  You said something about this week while it was still this week.
ob.split.l57  [E]  The app has {n} sentences from you about this week and the counts that came with them.
ob.split.l58  [E]  Three answers is a count and not a conclusion.
ob.split.l59  [E]  Answering does not change the week. It changes what the record of the week holds.
ob.split.l60  [E]  The only thing this app does with an answer is store it and count it.
```

**Extensions**
```
ob.split.e01  [O]  The numbers agree with that.
ob.split.e02  [O]  The numbers read a little differently.
ob.split.e03  [O]  Completions were up, which fits.
ob.split.e04  [E]  What a week feels like and what it counts as are not always the same.
ob.split.e05  [O]  Last week the split went the other way.
ob.split.e06  [E]  That is worth holding next to the numbers above.
ob.split.e07  [P]  {n} answers in total.
ob.split.e08  [P]  {m} of them positive.
ob.split.e09  [P]  {priorLabel} came up most.
ob.split.e10  [P]  {k} went the other way.
ob.split.e11  [O]  That is {priorCount} of {n}.
ob.split.e12  [O]  The rest went the other way.
ob.split.e13  [O]  Something finished in the same week.
ob.split.e14  [O]  You answered while the week was still open.
ob.split.e15  [O]  {priorCount} of those answers used the same word.
ob.split.e16  [E]  An answer given on the day is a different thing from one given later.
ob.split.e17  [E]  The app keeps what you said and does nothing else with it.
ob.split.e18  [O]  {m} of the {n} answers took the positive option and {k} took the other.
ob.split.e19  [E]  Counting answers is not the same as understanding them, and this is the counting.
ob.split.e20  [O]  Across the week you answered {n} times. {priorCount} of those answers were the same one.
```

**Guard.** `ob.split.e01` and `ob.split.e02` are mutually exclusive and are selected by a criterion comparing the answer polarity to the actual net flow. They must never be chosen at random.

## 2.6 selfReportVsData

**Callback family, and the flagship of the whole engine.** Sets what the user said against what subsequently happened. Requires a specific prior pulse and a fact that resolves it.

**Leads**
```
ob.srvd.l01  [O]  You called {itemTitle} deep work {ageDays} ago. It is still active.
ob.srvd.l02  [O]  When {itemTitle} was {n} days old you said deep work. It is now {m}.
ob.srvd.l03  [E]  You said {priorLabel}. Since then, {itemTitle} has not moved.
ob.srvd.l04  [O]  You said {priorLabel} about {areaName}. It has since finished {n} things.
ob.srvd.l05  [E]  You described that as {priorLabel}. The week went on to prove you right.
ob.srvd.l06  [O]  You called the concentration in {areaName} deliberate. It held all week.
ob.srvd.l07  [E]  You said the queue was building up on purpose. It has grown by {n} since.
ob.srvd.l08  [O]  You marked that as {priorLabel}. {n} days later, it is unchanged.
ob.srvd.l09  [E]  Last time this area went quiet you called it planned. This time it lasted longer.
ob.srvd.l10  [O]  You said {priorLabel} on {dayName}. The rest of the week followed.
ob.srvd.l11  [O]  You answered a pulse about {itemTitle}. It is still the active one.
ob.srvd.l12  [O]  You said {priorLabel} about {itemTitle}. It has been active {ageDays}.
ob.srvd.l13  [O]  The answer on record for {itemTitle} is {priorLabel}.
ob.srvd.l14  [O]  {priorLabel} was your reading of {itemTitle}. It is still there.
ob.srvd.l15  [O]  {itemTitle} has been active {ageDays} and it has an answer against it.
ob.srvd.l16  [O]  You said {priorLabel}. {itemTitle} is still the active item in {areaName}.
ob.srvd.l17  [O]  There were events in {areaName}. {itemTitle} stayed where it was.
ob.srvd.l18  [O]  The answer you gave about {itemTitle} was {priorLabel}.
ob.srvd.l19  [O]  {areaName} has had {itemTitle} in front for {ageDays}.
ob.srvd.l20  [O]  {itemTitle} carries one answer from you and {ageDays} of being active.
ob.srvd.l21  [O]  You were asked about {itemTitle} and you said {priorLabel}.
ob.srvd.l22  [O]  The record holds {priorLabel} against {itemTitle}.
ob.srvd.l23  [O]  {areaName} had a week. {itemTitle} is still its active item.
ob.srvd.l24  [O]  You said {priorLabel} within the last month. {itemTitle} is still active.
ob.srvd.l25  [O]  {itemTitle} is the item you answered about. It has not been replaced.
ob.srvd.l26  [O]  Your answer about {itemTitle} was {priorLabel}. That was inside the last month.
ob.srvd.l27  [O]  There is something you said about {itemTitle}. It was {priorLabel}.
ob.srvd.l28  [O]  What you said about {itemTitle} is on the record, word for word.
ob.srvd.l29  [O]  {areaName} moved. {itemTitle} did not leave.
ob.srvd.l30  [O]  The pulse asked about {itemTitle} and you answered {priorLabel}.
ob.srvd.l31  [O]  Since you answered, {itemTitle} has stayed at the front.
ob.srvd.l32  [E]  You said {priorLabel}. The app has kept it exactly as you said it.
ob.srvd.l33  [E]  {priorLabel} was a reading taken at a moment. {itemTitle} is still active.
ob.srvd.l34  [E]  You answered {priorLabel} about {itemTitle}. Nothing here says whether that was right.
ob.srvd.l35  [E]  What you said about {itemTitle} may still be true. This is only a note that you said it.
ob.srvd.l36  [E]  An answer given inside the last month, about an item still at the front.
ob.srvd.l37  [E]  You called it {priorLabel}. It has not been swapped out since.
ob.srvd.l38  [E]  The record holds your reading of {itemTitle} and the fact that it is still active. It has no third thing.
ob.srvd.l39  [E]  {ageDays} of being active, with a sentence from you about it.
ob.srvd.l40  [E]  {priorLabel} was true when you said it. Whether it still is, only you can say.
ob.srvd.l41  [E]  An item can outlast the answer given about it without either being wrong.
ob.srvd.l42  [E]  The app has your words about {itemTitle} and its own count of days. It does not have a reading.
ob.srvd.l43  [E]  Some answers age and some do not. Nothing here says which this is.
ob.srvd.l44  [E]  What you said is a fact about you and the days are a fact about the item.
ob.srvd.l45  [E]  The one thing the app knows about this item that it did not measure is {priorLabel}.
ob.srvd.l46  [E]  {itemTitle} has been active {ageDays}. The answer you gave is the only reading of it anyone has.
ob.srvd.l47  [P]  {priorLabel}, about {itemTitle}.
ob.srvd.l48  [P]  {itemTitle} is still active.
ob.srvd.l49  [P]  You said {priorLabel} about it.
ob.srvd.l50  [P]  {itemTitle}, in {areaName}, {ageDays} old.
ob.srvd.l51  [P]  {itemTitle} has been active {ageDays}.
ob.srvd.l52  [P]  The answer was {priorLabel}.
ob.srvd.l53  [P]  {areaName} holds it still.
ob.srvd.l54  [P]  {priorLabel}, on the record.
ob.srvd.l55  [O]  The answer stands.
ob.srvd.l56  [E]  The app is not comparing your answer to anything here. What it does is put the answer and the item side by side.
ob.srvd.l57  [O]  The item outlasted the answer.
ob.srvd.l58  [O]  Your reading is on file.
ob.srvd.l59  [P]  {areaName} still has it in front.
ob.srvd.l60  [O]  Nothing has replaced it.
```

**Extensions**
```
ob.srvd.e01  [E]  Both things can be true.
ob.srvd.e02  [O]  That was the only pulse you answered about it.
ob.srvd.e03  [E]  Worth revisiting the answer, or the item.
ob.srvd.e04  [O]  You have said the same thing about it twice now.
ob.srvd.e05  [E]  The first read may still be the right one.
ob.srvd.e06  [O]  It has been active {ageDays} in total.
ob.srvd.e07  [P]  The answer is stored exactly.
ob.srvd.e08  [O]  Nothing since has changed which item is active.
ob.srvd.e09  [P]  {ageDays} active, so far.
ob.srvd.e10  [O]  The area around it had a week of its own.
ob.srvd.e11  [E]  An answer is a reading of a moment. The moment it read has passed.
ob.srvd.e12  [P]  {priorLabel}. Nothing more.
ob.srvd.e13  [E]  Nothing here is a correction.
ob.srvd.e14  [P]  Still active.
ob.srvd.e15  [O]  The days since are in the record too.
ob.srvd.e16  [E]  What you said and what the days did are two different kinds of fact, and the app only measured one of them.
ob.srvd.e17  [O]  Its area was one of the ones that moved.
ob.srvd.e18  [E]  You may still be right.
ob.srvd.e19  [O]  The app can hold your answer next to the age of the item and go no further than that.
ob.srvd.e20  [O]  Whatever you meant by it is not recorded.
```

**Guard.** Every lead here requires check 6 of the validator, comparing the quoted label against the stored `responseLabel` on the referenced `PULSE_ANSWERED` event. A fabricated callback is the single most damaging output the app can produce.

## 2.7 quietWeek

Replaces the section body entirely when activity is near zero.

**Leads**
```
ob.quiet.l01  [P]  A quiet week. {n} things happened.
ob.quiet.l02  [P]  Almost nothing moved this week.
ob.quiet.l03  [O]  Your queues hold {n} things and none of them changed.
ob.quiet.l04  [O]  No completions this week.
ob.quiet.l05  [E]  The week passed without leaving much here.
ob.quiet.l06  [O]  {n} events across seven days.
ob.quiet.l07  [E]  Some weeks are for other things.
ob.quiet.l08  [P]  Nothing was completed and nothing was added.
ob.quiet.l09  [N]  {n} events. All week.                                 [S]
ob.quiet.l10  [N]  The queues held.                                      [S]
ob.quiet.l11  [N]  Seven days, {n} things.                               [S]
```

**Extensions**
```
ob.quiet.e01  [O]  Everything is where you left it on Sunday.
ob.quiet.e02  [O]  Last week had {n}.
ob.quiet.e03  [E]  A quiet week is data too, it is just quieter data.
ob.quiet.e04  [O]  {itemTitle} has now been active {ageDays}.
ob.quiet.e05  [E]  Nothing here needs explaining. It is just what the week was.
```

## 2.8 queuePressure

**Leads**
```
ob.qp.l01  [P]  Your queues hold {n} things, up from {m}.
ob.qp.l02  [O]  {areaName} is holding {n} items behind its active one.
ob.qp.l03  [O]  Two areas grew their queues this week.
ob.qp.l04  [E]  There is more waiting now than at any point this month.
ob.qp.l05  [P]  {n} items are queued across {areaCount} areas.
ob.qp.l06  [O]  The longest queue is in {areaName}, at {n}.
ob.qp.l07  [N]  {n} things are in the queues.                             [S]
ob.qp.l08  [N]  {n} waiting, {m} a week ago.                              [S]
ob.qp.l09  [E]  A queue is a record of decisions not yet made, and this one got longer.
ob.qp.l10  [N]  The queues are longer.                                    [S]
ob.qp.l11  [N]  {n} things are waiting.
ob.qp.l12  [N]  Something waits in {areaCount} areas.
ob.qp.l13  [N]  {n} waiting, across {areaCount} areas.
ob.qp.l14  [N]  The longest queue is {areaName}'s.
ob.qp.l15  [N]  At least one queue got longer.
ob.qp.l16  [N]  {n} things sit behind active items.
ob.qp.l17  [N]  {areaCount} areas hold something.
ob.qp.l18  [N]  A queue grew this week.
ob.qp.l19  [N]  {n} things are waiting across the areas.
ob.qp.l20  [N]  The queues hold {n} things and one of them got longer.
ob.qp.l21  [N]  {areaName} holds the longest queue of the {areaCount}.
ob.qp.l22  [N]  {n} things are waiting. {m} were waiting on Sunday.
ob.qp.l23  [N]  One queue at least is longer than it was.
ob.qp.l24  [N]  The queues came out of the week holding {n} things.
ob.qp.l25  [N]  {areaCount} areas have something behind the item in front.
ob.qp.l26  [N]  {n} things wait behind the items that are moving.
ob.qp.l27  [N]  The week ended with {n} things in the queues.
ob.qp.l28  [N]  {n} things are in the queues now, {m} were there when the week opened, and at least one queue grew inside it.
ob.qp.l29  [N]  {areaCount} areas are holding something behind their active item, and the longest of those queues is in {areaName}.
ob.qp.l30  [N]  The queues hold {n} things now, which is what {areaCount} areas have between them.
ob.qp.l31  [N]  At least one queue is longer than it was seven days ago, and {n} things are waiting in total.
ob.qp.l32  [O]  Your queues hold {n} things.
ob.qp.l33  [O]  {areaName} is holding the most.
ob.qp.l34  [O]  One of your queues grew this week.
ob.qp.l35  [O]  Queues in {areaCount} areas.
ob.qp.l36  [O]  {n} things are queued across your areas.
ob.qp.l37  [O]  The queue in {areaName} is the longest one you have.
ob.qp.l38  [O]  {m} things were waiting when the week opened. {n} are waiting now.
ob.qp.l39  [O]  Your areas are holding {n} things between them.
ob.qp.l40  [O]  {areaName} has more waiting than any other area.
ob.qp.l41  [O]  The queues took on length somewhere this week.
ob.qp.l42  [O]  The thing at the front of {areaName} has the most behind it.
ob.qp.l43  [O]  Across your areas, {n} things are waiting for something to happen to them.
ob.qp.l44  [O]  Something is waiting in every one of {areaCount} areas, and the longest of those queues is the one in {areaName}.
ob.qp.l45  [O]  Waiting is what {n} things are doing right now, in {areaCount} areas, behind whatever is at the front of each.
ob.qp.l46  [O]  Your queues held {m} things when the week opened and {n} at the end of it, and one of them grew somewhere in between.
ob.qp.l47  [P]  {n} queued.
ob.qp.l48  [P]  Something behind the front in {areaCount} places.
ob.qp.l49  [P]  {n} now, {m} on Sunday.
ob.qp.l50  [P]  The front of {areaName} has a line behind it.
ob.qp.l51  [P]  {n} things across {areaCount} areas.
ob.qp.l52  [P]  {n} in all.
ob.qp.l53  [P]  {n} things waiting now, {m} seven days ago.
ob.qp.l54  [P]  {n} things spread over {areaCount} areas.
ob.qp.l55  [P]  The longest queue in the app is {areaName}'s. {n} things are waiting overall.
ob.qp.l56  [P]  {n} things are waiting at the end of this week, spread over {areaCount} areas, one of which grew.
ob.qp.l57  [E]  Waiting is a state, not a verdict.
ob.qp.l58  [E]  {n} things waiting is a number before it is anything else.
ob.qp.l59  [E]  A queue is a list of things that have already been decided on once.
ob.qp.l60  [E]  There is nothing in a queue that was not put there on purpose, and {n} things are in one now.
```

**Extensions**
```
ob.qp.e01  [O]  {m} of those have been waiting more than a fortnight.
ob.qp.e02  [O]  The queues have grown three weeks running.
ob.qp.e03  [O]  The oldest is {itemTitle}, queued {ageDays} ago.
ob.qp.e04  [E]  Length is not the problem. Length that never shortens is.
ob.qp.e05  [O]  Nothing in {areaName}'s queue has moved since {sinceRef}.
ob.qp.e06  [P]  {areaName} holds the longest of them.
ob.qp.e07  [P]  {n} things in total.
ob.qp.e08  [P]  {m} were waiting a week ago.
ob.qp.e09  [P]  {areaCount} areas have one.
ob.qp.e10  [O]  One of them got longer this week.
ob.qp.e11  [O]  How long any of it has been waiting is not measured here.
ob.qp.e12  [O]  {n} across every area.
ob.qp.e13  [O]  The queue in {areaName} is the longest.
ob.qp.e14  [O]  One is longer than it was.
ob.qp.e15  [O]  That is {n} things and {areaCount} areas.
ob.qp.e16  [E]  Some of it will still be there next week and some will not.
ob.qp.e17  [E]  A number on its own says nothing.
ob.qp.e18  [E]  The queues are a record of what was kept rather than of what was done, and they are {n} things long.
ob.qp.e19  [O]  {n} things are waiting across {areaCount} areas. At least one of those queues is longer than it was.
ob.qp.e20  [E]  What is waiting is not the same as what is late, and nothing here measures the second thing.
```

## 2.9 areaRevival

**Leads**
```
ob.rev.l01  [P]  {areaName} moved again after {ageDays} of nothing.
ob.rev.l02  [O]  {areaName} came back this week.
ob.rev.l03  [O]  After {ageDays} still, {areaName} completed {n} things.
ob.rev.l04  [E]  Something you had stopped touching started moving again.
ob.rev.l05  [P]  First activity in {areaName} since {sinceRef}.
ob.rev.l06  [O]  {areaName} had been the quietest area. It was not this week.
ob.rev.l07  [O]  {areaName} moved this week.
ob.rev.l08  [O]  {areaName} had been quiet for {ageDays}.
ob.rev.l09  [O]  Something in {areaName} started again.
ob.rev.l10  [O]  {areaName} was still for {ageDays}. This week it was not.
ob.rev.l11  [O]  {areaName} has something in it again after {ageDays}.
ob.rev.l12  [O]  The quiet in {areaName} ended this week.
ob.rev.l13  [O]  {areaName} has events again.
ob.rev.l14  [O]  {ageDays} of stillness, then a week with something in it.
ob.rev.l15  [O]  A gap of {ageDays}, now closed.
ob.rev.l16  [O]  {areaName} is on the week's list again.
ob.rev.l17  [O]  Nothing had happened in {areaName} for {ageDays}. This week did.
ob.rev.l18  [O]  {areaName} came off the quiet list.
ob.rev.l19  [O]  The silence in {areaName} ran {ageDays}.
ob.rev.l20  [O]  {areaName} started moving again after {ageDays}.
ob.rev.l21  [O]  Whatever had stopped in {areaName} started again this week.
ob.rev.l22  [O]  {areaName} spent {ageDays} out of the report and came back into it.
ob.rev.l23  [O]  An area that had been quiet for {ageDays} was part of this week.
ob.rev.l24  [O]  {areaName} has been in this report before. It is in this one too.
ob.rev.l25  [O]  The week has {areaName} in it, which the last {ageDays} did not.
ob.rev.l26  [O]  {areaName} went {ageDays} without an event and then had one.
ob.rev.l27  [O]  There was a gap of {ageDays} in {areaName}. The week that ended today closed it.
ob.rev.l28  [O]  {areaName} had gone {ageDays} without a single event, until this week put one in it and ended the gap.
ob.rev.l29  [O]  Nothing moved in {areaName} for {ageDays}. The week that just ended had something in it.
ob.rev.l30  [O]  {areaName} was the area nothing was happening in, for {ageDays}, and it is not that this week.
ob.rev.l31  [O]  An area can be quiet for {ageDays} and then not be, and {areaName} is the one that did that here.
ob.rev.l32  [P]  {areaName} moved again.
ob.rev.l33  [P]  {ageDays} of nothing, then this week.
ob.rev.l34  [P]  First event in {areaName} in {ageDays}.
ob.rev.l35  [P]  {areaName} is active again.
ob.rev.l36  [P]  Activity in {areaName}, after {ageDays}.
ob.rev.l37  [P]  {areaName} returned.
ob.rev.l38  [P]  {areaName} had {ageDays} with nothing in them.
ob.rev.l39  [P]  {ageDays} still, then a week.
ob.rev.l40  [P]  {areaName} moved this week for the first time in {ageDays}.
ob.rev.l41  [P]  Something happened in {areaName} this week.
ob.rev.l42  [P]  {areaName}: {ageDays} quiet, then a week that was not.
ob.rev.l43  [P]  The last {ageDays} held nothing in {areaName}. This week did.
ob.rev.l44  [P]  {areaName} had been quiet for {ageDays} before this week, and the report had nothing to say about it in that time.
ob.rev.l45  [P]  The first thing to happen in {areaName} in {ageDays}.
ob.rev.l46  [P]  {areaName} is not quiet now.
ob.rev.l47  [P]  {ageDays} with nothing in {areaName}. Then one.
ob.rev.l48  [P]  {areaName} closed a gap of {ageDays} this week.
ob.rev.l49  [P]  This week holds the first event {areaName} has had in {ageDays}, and the gap before it was the longer part.
ob.rev.l50  [P]  {areaName} spent {ageDays} with nothing in it and then had a week with something in it.
ob.rev.l51  [P]  The report has not named {areaName} for {ageDays}. It names it now.
ob.rev.l52  [E]  An area came back.
ob.rev.l53  [E]  {areaName} was not gone. It was still.
ob.rev.l54  [E]  Areas do not announce that they have stopped, and they do not announce that they have started.
ob.rev.l55  [E]  Whether {areaName} keeps moving is a different question from whether it moved.
ob.rev.l56  [E]  {ageDays} of quiet can be a decision or an accident, and the record does not say which.
ob.rev.l57  [E]  A return says what happened. It does not say why.
ob.rev.l58  [E]  A stop and a start are two events with {ageDays} between them.
ob.rev.l59  [E]  {areaName} is back in the record, which is all the record can tell you.
ob.rev.l60  [E]  An area coming back is only visible after it has happened.
```

**Extensions**
```
ob.rev.e01  [O]  {n} of its {m} queued items went.
ob.rev.e02  [E]  Whether it holds is next week's question.
ob.rev.e03  [O]  It has come back before, in {sinceRef}, and stayed for {n} weeks.
ob.rev.e04  [O]  Its queue is now empty.
ob.rev.e05  [E]  Returns like this are usually a decision rather than an accident.
ob.rev.e06  [P]  The gap before it was {ageDays}.
ob.rev.e07  [P]  That area is {areaName}.
ob.rev.e08  [P]  {ageDays}, and then this week.
ob.rev.e09  [P]  It had been {ageDays} since the last one.
ob.rev.e10  [O]  The quiet ran to {ageDays}.
ob.rev.e11  [O]  Nothing here says why it stopped.
ob.rev.e12  [O]  Nothing here says why it started again either.
ob.rev.e13  [O]  The report had not named it in {ageDays}.
ob.rev.e14  [O]  It is one week against {ageDays}.
ob.rev.e15  [O]  Whether the next week has it too is not known yet.
ob.rev.e16  [E]  A return is easier to see than a stop.
ob.rev.e17  [E]  Stopping and starting are the same act seen from two ends, and the record shows the second one more clearly.
ob.rev.e18  [E]  One week and {ageDays} are two different lengths of time.
ob.rev.e19  [O]  The last {ageDays} in {areaName} held nothing at all. This week is the first thing in the record since.
ob.rev.e20  [E]  An area that has been quiet for {ageDays} and then moves says nothing at all about the week after this one.
```

## 2.10 persistentItem

**Leads**
```
ob.pers.l01  [P]  {itemTitle} has been active in {areaName} for {ageDays}.
ob.pers.l02  [O]  Nothing has moved past {itemTitle} in {ageDays}.
ob.pers.l03  [O]  {itemTitle} has outlasted {n} other items you completed.
ob.pers.l04  [E]  {itemTitle} has stopped being this week's work and become a standing condition.
ob.pers.l05  [P]  {ageDays} on {itemTitle}, with {n} things queued behind it.
ob.pers.l06  [O]  Most things you finish take {medianDays}. This one is at {ageDays}.
ob.pers.l07  [E]  There is one item here that has been true for longer than anything else.
ob.pers.l08  [N]  {ageDays} at the front of {areaName}.                    [S]
ob.pers.l09  [N]  {itemTitle} is where it was.                             [S]
ob.pers.l10  [N]  {itemTitle} has {n} things behind it.                    [S]
ob.pers.l11  [N]  The front of {areaName} has been {itemTitle} for {ageDays} and nothing in the queue has come past it.
ob.pers.l12  [N]  Nothing else has reached the front of {areaName} in {ageDays}, and {itemTitle} is still there.
ob.pers.l13  [N]  {itemTitle} is still at the front.
ob.pers.l14  [N]  Front of the queue, {ageDays}.
ob.pers.l15  [N]  {itemTitle} has held its place for {ageDays}.
ob.pers.l16  [N]  Nothing has replaced {itemTitle}.
ob.pers.l17  [N]  {n} things wait behind it.
ob.pers.l18  [N]  The front of {areaName} has not changed in {ageDays}.
ob.pers.l19  [N]  {itemTitle} has been the active item for {ageDays}.
ob.pers.l20  [N]  {ageDays} with the same thing in front.
ob.pers.l21  [N]  {areaName} had events this week. {itemTitle} stayed at its front.
ob.pers.l22  [N]  The same item has been first in {areaName} for {ageDays}.
ob.pers.l23  [N]  {itemTitle} has {n} things waiting behind it while {areaName} moved.
ob.pers.l24  [N]  {itemTitle} has held the front of {areaName} for {ageDays}, with {n} things in the queue behind it.
ob.pers.l25  [N]  {ageDays} is how long {itemTitle} has led {areaName}.
ob.pers.l26  [N]  {itemTitle} has {n} things in the queue behind it. The item itself has been in place {ageDays}.
ob.pers.l27  [N]  {areaName} had a week with things in it. {itemTitle} was at the front for all of them.
ob.pers.l28  [O]  {itemTitle} has stayed in place {ageDays}.
ob.pers.l29  [O]  The front of {areaName} is unchanged.
ob.pers.l30  [O]  {itemTitle} still leads {areaName}.
ob.pers.l31  [O]  {areaName} kept working around {itemTitle}.
ob.pers.l32  [O]  {n} things have queued up behind the front of {areaName}.
ob.pers.l33  [O]  {itemTitle} outlasted the week.
ob.pers.l34  [O]  {areaName} has had a week and {itemTitle} has had {ageDays}.
ob.pers.l35  [O]  {itemTitle} has been the front item in {areaName} for {ageDays}.
ob.pers.l36  [O]  Nothing in the {areaName} queue has come past {itemTitle}.
ob.pers.l37  [O]  {itemTitle} has been the answer to what is next in {areaName} for {ageDays}.
ob.pers.l38  [O]  {n} things sit behind the active item in {areaName}.
ob.pers.l39  [O]  Your active item in {areaName} has not changed in {ageDays}.
ob.pers.l40  [O]  {itemTitle} has been at the front while other things moved.
ob.pers.l41  [O]  The rest of {areaName} moved this week and {itemTitle} stayed still.
ob.pers.l42  [O]  {itemTitle} has had {n} things sitting behind it while {areaName} moved around them.
ob.pers.l43  [O]  {itemTitle} has led {areaName} for {ageDays}, which is longer than the week this report covers.
ob.pers.l44  [O]  For {ageDays} the first thing in {areaName} has been {itemTitle}. It is still the first thing.
ob.pers.l45  [P]  {ageDays} on {itemTitle}.
ob.pers.l46  [P]  Same item, {ageDays} on.
ob.pers.l47  [P]  {n} queued, one active.
ob.pers.l48  [P]  {ageDays}, one item, no change.
ob.pers.l49  [P]  {itemTitle} holds.
ob.pers.l50  [P]  {areaName}'s active item is {itemTitle}, {ageDays} old.
ob.pers.l51  [P]  {itemTitle} has a queue of {n} behind it.
ob.pers.l52  [P]  {medianDays} is the usual. {ageDays} is this one.
ob.pers.l53  [P]  The queue in {areaName} holds {n} things, all of them behind its active item.
ob.pers.l54  [P]  {ageDays} at the front of {areaName}, with {n} things behind.
ob.pers.l55  [P]  {itemTitle} has been the active item in {areaName} for {ageDays}, with {n} things queued behind it.
ob.pers.l56  [P]  The active item in {areaName} has been {itemTitle} for {ageDays}. The usual time to finish something is {medianDays}.
ob.pers.l57  [E]  {ageDays} is long enough for a queue to form behind something.
ob.pers.l58  [E]  The front of a list is a decision that keeps being made.
ob.pers.l59  [E]  Whatever changed in {areaName}, the front of it did not.
ob.pers.l60  [E]  An item can sit at the front because it is hard or because it is large, and nothing here tells the two apart.
```

**Extensions**
```
ob.pers.e01  [O]  You called it deep work when it was {n} days old.
ob.pers.e02  [O]  It is the longest anything has been active in {areaName}.
ob.pers.e03  [E]  Long is not wrong. Long and unexamined is a different thing.
ob.pers.e04  [O]  {n} things are waiting behind it.
ob.pers.e05  [O]  It has survived {n} focus sessions without finishing.
ob.pers.e06  [P]  {n} things still wait.
ob.pers.e07  [P]  {ageDays} so far.
ob.pers.e08  [P]  The area around it moved.
ob.pers.e09  [P]  {medianDays} is the usual.
ob.pers.e10  [O]  It has been first the whole time.
ob.pers.e11  [O]  {areaName} had a week around it.
ob.pers.e12  [O]  Nothing came past it.
ob.pers.e13  [O]  The queue behind it has {n} things in it.
ob.pers.e14  [O]  It has been at the front since before this week began.
ob.pers.e15  [E]  Being first for {ageDays} is a fact about the list rather than about the item.
ob.pers.e16  [E]  Some things take {medianDays} and some take longer.
ob.pers.e17  [E]  An item that stays at the front is either the right one or the one nothing displaced.
ob.pers.e18  [O]  The queue is {n} long.
ob.pers.e19  [E]  The front of a queue is the part of a list that is always visible, and this one has held {ageDays}.
ob.pers.e20  [O]  The record shows {ageDays} of {itemTitle} at the front of {areaName} and does not show why.
```

## 2.11 personalBest

**Leads**
```
ob.best.l01  [P]  {n} completions, more than any week before.
ob.best.l02  [O]  This was your most productive week since you started.
ob.best.l03  [O]  The previous best was {m}, in {sinceRef}.
ob.best.l04  [E]  Nothing in eleven weeks of history looks like this one.
ob.best.l05  [P]  A new high: {n} completions.
ob.best.l06  [O]  You finished more this week than in any two earlier weeks combined.
ob.best.l07  [P]  A new high.                                            [S]
ob.best.l08  [P]  {n} finished. A record.                                [S]
ob.best.l09  [E]  A record is a fact about the past. Nothing here turns it into a target for the week ahead.
ob.best.l10  [E]  Nothing here needs to happen again.
ob.best.l11  [E]  The number is {n}. The reading of it is not the app's to make.
ob.best.l12  [E]  The record moved. That is all the record does.
ob.best.l13  [E]  A high week is a fact.
ob.best.l14  [E]  {n} is where the record stands now.
ob.best.l15  [E]  Something in this week worked. The record can say that it did without saying what it was.
ob.best.l16  [E]  The previous best was {m}. This week is not less than that.
ob.best.l17  [E]  A best week says what happened and nothing about what comes next.
ob.best.l18  [E]  {n} completions is the largest number this record has held, and a number is all it is.
ob.best.l19  [E]  A week can be the best one and still be an ordinary week from the inside.
ob.best.l20  [E]  Nothing in the history reads higher than {n}, and the history goes back to before {sinceRef}.
ob.best.l21  [E]  The best earlier week was {m}, in {sinceRef}, and this one is not below it.
ob.best.l22  [O]  {n} completions, with no earlier week higher.
ob.best.l23  [O]  Nothing recorded is above this.
ob.best.l24  [O]  The previous high was {m}.
ob.best.l25  [O]  {n} is the count.
ob.best.l26  [O]  {areaName} carried more of it than anywhere else.
ob.best.l27  [O]  It came with {sessions} focus sessions.
ob.best.l28  [O]  The record was {m} until this week.
ob.best.l29  [O]  {sinceRef} was the one before this.
ob.best.l30  [O]  Nothing since {sinceRef} has finished more.
ob.best.l31  [O]  No week since you started has read higher than this one, and there are at least two of them.
ob.best.l32  [O]  Your best week was {sinceRef}. It is this one now.
ob.best.l33  [O]  {n} against a previous best of {m}.
ob.best.l34  [O]  This week sits at the top of the record.
ob.best.l35  [O]  A week like this has not happened since {sinceRef}.
ob.best.l36  [O]  {areaName} holds the largest share of it.
ob.best.l37  [O]  The record has stood since {sinceRef}. This week reaches it.
ob.best.l38  [O]  The previous best was {m}. This week is {n}.
ob.best.l39  [O]  {n} completions this week, against {m} in the best week before it, and nothing between the two came higher.
ob.best.l40  [O]  {n} things finished this week, {sessions} focus sessions alongside them, and no earlier week higher.
ob.best.l41  [O]  Your record held {m} until this week. {sinceRef} was the week that held it.
ob.best.l42  [O]  Nothing in the record beats {n}. {areaName} is where more of it happened than anywhere else.
ob.best.l43  [P]  {n} completions.
ob.best.l44  [P]  {n}, the highest yet.
ob.best.l45  [P]  Previous best: {m}.
ob.best.l46  [P]  {n} this week, {m} before.
ob.best.l47  [P]  {sessions} focus sessions alongside.
ob.best.l48  [P]  {areaName} was the busiest.
ob.best.l49  [P]  {m} in {sinceRef}, {n} now.
ob.best.l50  [P]  {n} completions, {sessions} focus sessions.
ob.best.l51  [P]  {n} set against {m}.
ob.best.l52  [P]  {m} was the best before, in {sinceRef}.
ob.best.l53  [P]  Your record is {n} completions, set this week.
ob.best.l54  [P]  {n} completions and {sessions} focus sessions in seven days.
ob.best.l55  [P]  The best week before this one was {sinceRef}, at {m}.
ob.best.l56  [P]  {n} completions, more of them in {areaName} than anywhere else.
ob.best.l57  [P]  {n} completions this week, {m} in {sinceRef}, and nothing higher between them.
ob.best.l58  [P]  The record now reads {n} completions, set in the week that just ended.
ob.best.l59  [P]  {n} completions this week against {m} in {sinceRef}, which was the highest week until this one arrived.
ob.best.l60  [P]  Seven days, {n} completions, {sessions} focus sessions, and no earlier week above it in the record.
```

**Extensions**
```
ob.best.e01  [O]  {n} of them were in {areaName}.
ob.best.e02  [O]  It came alongside {sessions} focus sessions.
ob.best.e03  [E]  Peaks are worth noticing. They are not worth defending.
ob.best.e04  [O]  Your queues still grew, by {m}.
ob.best.e05  [E]  What made the difference is worth knowing, if you can name it.
ob.best.e06  [P]  {n} completions in all.
ob.best.e07  [P]  {m} was the previous number.
ob.best.e08  [P]  {sessions} focus sessions in the same week.
ob.best.e09  [P]  {sinceRef} held it before.
ob.best.e10  [O]  Nothing earlier comes to {n}.
ob.best.e11  [O]  The old number was {m}.
ob.best.e12  [O]  {areaName} holds more of it than anywhere else.
ob.best.e13  [O]  It has not been this high before.
ob.best.e14  [O]  The record has two weeks of history behind it at least.
ob.best.e15  [E]  A record here is a record of two weeks or twenty.
ob.best.e16  [E]  Whether it holds is a question for another week.
ob.best.e17  [E]  Nothing about a peak says how it was reached.
ob.best.e18  [O]  {n} completions, against {m} in {sinceRef}, with nothing higher in between.
ob.best.e19  [E]  A best week is the one the record noticed. There may have been better weeks it could not see.
ob.best.e20  [O]  The week that just ended is the highest in the record, and {areaName} holds more of it than anywhere else.
```

## 2.12 mostActiveSince

**Leads**
```
ob.since.l01  [P]  Your busiest week since {sinceRef}.
ob.since.l02  [O]  {n} events, the most since {sinceRef}.
ob.since.l03  [O]  Nothing since {sinceRef} has looked like this.
ob.since.l04  [E]  The last week with this much in it was {sinceRef}.
ob.since.l05  [P]  {n} completions, the highest since {sinceRef}.
ob.since.l06  [O]  The last week to finish more than this one was in {sinceRef}.
ob.since.l07  [O]  This week finished more than last week did.
ob.since.l08  [O]  There is a bigger week in the record. It is in {sinceRef}.
ob.since.l09  [O]  {sinceRef} holds the last week that finished more.
ob.since.l10  [O]  Every week since {sinceRef} finished less than this one.
ob.since.l11  [O]  The weeks between {sinceRef} and now all came in lower.
ob.since.l12  [O]  This week sits above every week after {sinceRef}.
ob.since.l13  [O]  {n} moves this week. The last better week was in {sinceRef}.
ob.since.l14  [O]  Last week finished fewer than this one.
ob.since.l15  [O]  The record has one week above this, in {sinceRef}.
ob.since.l16  [O]  Nothing between {sinceRef} and this week finished more.
ob.since.l17  [O]  {n} moves, in the week that finished the most since {sinceRef}.
ob.since.l18  [O]  Nothing after {sinceRef} has come in above this.
ob.since.l19  [O]  The weeks since {sinceRef} have all been smaller in what they finished.
ob.since.l20  [O]  {sinceRef} is the last month with a bigger week in it.
ob.since.l21  [O]  More was finished this week than in any week after {sinceRef}.
ob.since.l22  [O]  This week went above last week and above everything since {sinceRef}.
ob.since.l23  [O]  {sinceRef} still holds a bigger week than this one.
ob.since.l24  [P]  {n} moves this week.
ob.since.l25  [P]  The most finished since {sinceRef}.
ob.since.l26  [P]  {n} moves. No week since {sinceRef} finished more.
ob.since.l27  [P]  Above last week.
ob.since.l28  [P]  {sinceRef} was bigger.
ob.since.l29  [P]  Nothing since {sinceRef} was bigger.
ob.since.l30  [P]  {n} moves in seven days.
ob.since.l31  [P]  Last week was smaller.
ob.since.l32  [P]  The last bigger week: {sinceRef}.
ob.since.l33  [P]  {n} moves. More finished than last week.
ob.since.l34  [P]  Higher than last week, lower than {sinceRef}.
ob.since.l35  [P]  {n} moves, {sinceRef} the last week above.
ob.since.l36  [P]  Up from last week.
ob.since.l37  [P]  The peak since {sinceRef}.
ob.since.l38  [E]  {sinceRef} is far enough back that most of what happened since has been smaller.
ob.since.l39  [E]  A week that clears everything after it and still has {sinceRef} above it.
ob.since.l40  [E]  This is the high point of a run rather than of the whole record.
ob.since.l41  [E]  A week can be the biggest since {sinceRef} and still not be the biggest.
ob.since.l42  [E]  What this week did, no week since {sinceRef} has done.
ob.since.l43  [E]  {sinceRef} is the last time the record went higher than this.
ob.since.l44  [E]  The comparison this week invites is with {sinceRef} and with nothing in between.
ob.since.l45  [E]  Two facts sit side by side. This week beat last week. {sinceRef} still beats this week.
ob.since.l46  [E]  The week is not a record and it is the nearest thing to one since {sinceRef}.
ob.since.l47  [E]  Somewhere in {sinceRef} there is a week this one did not reach.
ob.since.l48  [P]  {n} moves in all.
ob.since.l49  [P]  A high since {sinceRef}.
ob.since.l50  [O]  Bigger than last week.
ob.since.l51  [P]  {sinceRef}, then nothing higher.
ob.since.l52  [O]  The reach goes back to {sinceRef}.
ob.since.l53  [O]  Nothing nearer than {sinceRef}.
ob.since.l54  [P]  Up on the week before.
ob.since.l55  [O]  {sinceRef} is still ahead.
ob.since.l56  [E]  The record still holds a week this one did not reach, and everything after it came in lower.
ob.since.l57  [O]  This week finished more than the week before it and more than every week back as far as {sinceRef}.
ob.since.l58  [E]  A week is worth naming when the reach back is long, and this reach goes to {sinceRef} without finding anything higher.
ob.since.l59  [O]  {n} moves went through the app this week. No week since {sinceRef} finished more than this one did.
ob.since.l60  [E]  There is a week in {sinceRef} this one did not reach, and nothing between the two got higher than this.
```

**Extensions**
```
ob.since.e01  [O]  That week was mostly {areaName} too.
ob.since.e02  [O]  It has been {n} weeks.
ob.since.e03  [E]  Whether that is a return or a spike shows up next week.
ob.since.e04  [O]  The weeks between averaged {m}.
ob.since.e05  [O]  Every week in between finished less than this one.
ob.since.e06  [P]  {n} moves across the week.
ob.since.e07  [O]  The week before this one came in lower.
ob.since.e08  [O]  {sinceRef} is still above it.
ob.since.e09  [P]  {n} moves, against {sinceRef} for the last higher week.
ob.since.e10  [E]  Whether the weeks after this one hold it is a question for later.
ob.since.e11  [O]  Nothing in the weeks since finished more.
ob.since.e12  [P]  Nothing since has been higher.
ob.since.e13  [O]  The weeks between {sinceRef} and this one all came in under it, which is what makes the reach worth naming.
ob.since.e14  [E]  A week that is second to something is still a week that everything since has come in under.
ob.since.e15  [O]  The record has not been higher since {sinceRef}.
ob.since.e16  [O]  That makes this the highest finishing week since {sinceRef}.
ob.since.e17  [E]  The comparison that holds is with {sinceRef}, because it is the last week this one did not pass.
ob.since.e18  [O]  Everything between then and now came in lower, and the week that did not is in {sinceRef}.
ob.since.e19  [P]  Above last week too.
ob.since.e20  [O]  The last week to go higher is in {sinceRef}.
```

## 2.13 dayShape

Which days carried the week.

**Leads**
```
ob.day.l01  [P]  {dayName} carried the week, with {n} of the {m} events.
ob.day.l02  [O]  Most of the week happened on two days.
ob.day.l03  [O]  {n} of seven days had activity.
ob.day.l04  [E]  The week was not spread evenly. It was mostly {dayName}.
ob.day.l05  [P]  Nothing happened on {n} of the seven days.
ob.day.l06  [O]  Your busiest day was {dayName}, at {n} events.
ob.day.l07  [E]  This week had a shape: quiet, then a burst, then quiet again.
```

**Extensions**
```
ob.day.e01  [O]  {dayName} has been your busiest day three weeks running.
ob.day.e02  [O]  The weekend was silent.
ob.day.e03  [E]  Weeks with a shape like this usually have a reason behind them.
ob.day.e04  [O]  Last week the activity was more even.
ob.day.e05  [O]  All {sessions} focus sessions were on the same two days.
```

## 2.14 timeOfDay

**Leads**
```
ob.tod.l01  [P]  Most of this week happened in the morning.
ob.tod.l02  [O]  {n} of your {m} completions were before midday.
ob.tod.l03  [O]  Evenings were where most things got added.
ob.tod.l04  [E]  You finish in the morning and collect at night.
ob.tod.l05  [P]  {pct} of your activity was after 5pm.
ob.tod.l06  [O]  Your focus sessions all started before 11am.
ob.tod.l07  [O]  Half of this week landed in one part of the day.
ob.tod.l08  [O]  The week had a busy stretch.
ob.tod.l09  [O]  More than one part of the day had something in it.
ob.tod.l10  [O]  The week was not level across the hours.
ob.tod.l11  [O]  One stretch of the day took half of everything.
ob.tod.l12  [O]  Your week has a preferred time.
ob.tod.l13  [O]  The day divides into parts and one of them holds this week.
ob.tod.l14  [O]  Half the week or more happened in the same stretch of hours.
ob.tod.l15  [O]  The week clustered.
ob.tod.l16  [O]  One part of the day did most of the work.
ob.tod.l17  [O]  The hours of the week were not equal.
ob.tod.l18  [O]  The week has a time of day in it.
ob.tod.l19  [O]  When you work showed up this week.
ob.tod.l20  [O]  Half of everything happened in one window of the day.
ob.tod.l21  [O]  The week leaned toward one part of the day.
ob.tod.l22  [O]  Your week is not evenly distributed across the day.
ob.tod.l23  [O]  The week gathered.
ob.tod.l24  [O]  A time of day held it.
ob.tod.l25  [O]  One part of the day did half of it.
ob.tod.l26  [O]  The clock had a preference.
ob.tod.l27  [O]  More than one time of day had something in it, and one of them had half.
ob.tod.l28  [O]  Half of the week's activity sat inside one part of the day, and the rest was spread over the others.
ob.tod.l29  [O]  The week came out of the same part of the day more often than not, and at least one other part had something too.
ob.tod.l30  [O]  There is a part of the day that holds half of this week, and there are other parts that hold the rest.
ob.tod.l31  [O]  When something happened this week, it was in one part of the day at least as often as in the other three put together.
ob.tod.l32  [P]  Half the week, one stretch.
ob.tod.l33  [P]  Half the week in the same hours.
ob.tod.l34  [P]  The week had a peak hour.
ob.tod.l35  [P]  Two parts of the day at least.
ob.tod.l36  [P]  Half in one stretch.
ob.tod.l37  [P]  One time of day, half.
ob.tod.l38  [P]  The week concentrated in time.
ob.tod.l39  [P]  A busy part of the day.
ob.tod.l40  [P]  The week is not flat.
ob.tod.l41  [P]  One window did most of it.
ob.tod.l42  [P]  Most of this week sat in one part of the day.
ob.tod.l43  [P]  The week fell mostly inside one part of the day.
ob.tod.l44  [P]  One part of the day carried half of the week.
ob.tod.l45  [P]  Two or more parts of the day had activity in them.
ob.tod.l46  [P]  The week's events were not spread evenly across the day.
ob.tod.l47  [P]  At least half of the week happened in one part of the day.
ob.tod.l48  [P]  There is a part of the day this week belongs to more than to any other.
ob.tod.l49  [P]  Half of this week or more sat inside one part of the day, and the rest sat outside it.
ob.tod.l50  [P]  The week put half of itself into one part of the day and the remainder into the others.
ob.tod.l51  [P]  Across seven days, one part of the day took at least half of everything that happened.
ob.tod.l52  [P]  The week did not use the day evenly. At least two parts of it had something in them.
ob.tod.l53  [P]  One part of the day holds half of this week's record, and the other parts hold what is left.
ob.tod.l54  [E]  Time of day is a fact about a week, not a verdict on it.
ob.tod.l55  [E]  Days have shapes.
ob.tod.l56  [E]  A week keeps its own hours.
ob.tod.l57  [E]  Where in the day something happens is a smaller fact than that it happened.
ob.tod.l58  [E]  The week kept returning to the same part of the day.
ob.tod.l59  [E]  Half of a week in one part of the day is either a schedule or an accident.
ob.tod.l60  [E]  Nothing here says whether the hours were chosen or available.
```

**Extensions**
```
ob.tod.e01  [O]  That has been consistent for three weeks.
ob.tod.e02  [O]  Last week it was the other way around.
ob.tod.e03  [E]  Knowing when you finish things is more useful than knowing how many.
ob.tod.e04  [O]  Your longest focus sessions were the early ones.
ob.tod.e05  [P]  Two parts of the day at least.
ob.tod.e06  [P]  Half of it in one stretch.
ob.tod.e07  [P]  That is half the week.
ob.tod.e08  [P]  One part of the day, more than the rest.
ob.tod.e09  [O]  The other parts had less.
ob.tod.e10  [O]  The rest of the day held what was left.
ob.tod.e11  [O]  The week was not flat across the day.
ob.tod.e12  [O]  More than one part was used.
ob.tod.e13  [O]  The concentration is in when, not in what.
ob.tod.e14  [O]  Seven days, one part busier.
ob.tod.e15  [E]  When is a smaller question than whether.
ob.tod.e16  [E]  A week has hours in it.
ob.tod.e17  [E]  A day has a shape and a week has a different one.
ob.tod.e18  [O]  Half of the week sat in one part of the day, and the other parts divided the rest between them.
ob.tod.e19  [E]  Whether those are the hours you have or the hours you pick is not something a count can answer.
ob.tod.e20  [O]  At least two parts of the day had something in them, and one of those had at least half.
```

## 2.15 switchingBehavior

**Leads**
```
ob.swi.l01  [P]  You swapped what was active {n} times this week.
ob.swi.l02  [O]  {areaName} changed its active item {n} times.
ob.swi.l03  [O]  {n} different items took turns at the front of {areaName}.
ob.swi.l04  [E]  {areaName} was hard to settle this week.
ob.swi.l05  [P]  {n} swaps across {areaCount} areas.
ob.swi.l06  [O]  Nothing stayed at the front of {areaName} for more than {ageDays}.
ob.swi.l07  [N]  The front of {areaName} changed {n} times.
ob.swi.l08  [N]  {n} different items held {areaName}'s active slot.
ob.swi.l09  [N]  {n} swaps, one area.                                    [S]
```

**Extensions**
```
ob.swi.e01  [O]  {n} of those swaps went back and forth between the same two items.
ob.swi.e02  [O]  None of the swapped items has been completed.
ob.swi.e03  [E]  Swapping is reprioritizing until it becomes circling.
ob.swi.e04  [O]  You called that reprioritizing when asked.
ob.swi.e05  [O]  That is more than the previous three weeks combined.
```

## 2.16 focusAbandonment

Handled without judgment. This family exists specifically so abandonment is visible without being scolded.

**Leads**
```
ob.aban.l01  [P]  {n} of your {m} focus sessions ended early.
ob.aban.l02  [O]  {n} sessions were started and not finished.
ob.aban.l03  [O]  The sessions that finished averaged {minutes} minutes.
ob.aban.l04  [E]  Some sessions ran and some did not, which is what usually happens.
ob.aban.l05  [P]  {sessions} started, {n} completed.
ob.aban.l06  [N]  {n} sessions stopped early.                              [S]
ob.aban.l07  [N]  {n} of the {m} sessions this week stopped before the time set aside for them ran out.
ob.aban.l08  [E]  The sessions that ended early are in the record beside the ones that did not, and both are sessions.
```

**Extensions**
```
ob.aban.e01  [O]  The ones that ended early were all in the evening.
ob.aban.e02  [O]  All of them were in {areaName}.
ob.aban.e03  [E]  A session that ends early still counted as a decision to start.
ob.aban.e04  [O]  Last week every session ran to the end.
```

## 2.17 queueDrained

**Leads**
```
ob.drain.l01  [P]  {areaName} is empty. It held {n} things on Sunday.
ob.drain.l02  [O]  {areaName} cleared its entire queue this week.
ob.drain.l03  [O]  Two areas ended the week with nothing waiting.
ob.drain.l04  [E]  There is an area with nothing in it for the first time.
ob.drain.l05  [P]  {areaName} went from {n} queued to zero.
```

**Extensions**
```
ob.drain.e01  [O]  It still has an active item.
ob.drain.e02  [O]  It has nothing active either.
ob.drain.e03  [E]  Empty is either finished or waiting for a decision.
ob.drain.e04  [O]  The last item had been queued since {sinceRef}.
```

## 2.18 steadyPace

**Leads**
```
ob.stead.l01  [P]  {n} completions, close to your average of {m}.
ob.stead.l02  [O]  This week looked much like the three before it.
ob.stead.l03  [O]  Activity has stayed within a narrow band for a month.
ob.stead.l04  [E]  Nothing about this week stands out, which is its own kind of result.
ob.stead.l05  [P]  {n} events, {m} last week, {k} the week before.
ob.stead.l06  [P]  The week finished {n} things.
ob.stead.l07  [P]  {n} finished, against an average of {m}.
ob.stead.l08  [P]  Three weeks within two of each other.
ob.stead.l09  [P]  {n} this week, {m} in an average one.
ob.stead.l10  [P]  {m} is your average. This week it was {n}.
ob.stead.l11  [P]  {n} completions, {m} the usual.
ob.stead.l12  [P]  Three weeks, much the same.
ob.stead.l13  [P]  Your average week finishes {m}.
ob.stead.l14  [P]  A week inside the usual range.
ob.stead.l15  [P]  The last three weeks are level.
ob.stead.l16  [P]  {n} finished.
ob.stead.l17  [P]  Nothing unusual in the totals.
ob.stead.l18  [P]  Three weeks at one size.
ob.stead.l19  [P]  Steady, three weeks running.
ob.stead.l20  [P]  This week and the two before it are close.
ob.stead.l21  [O]  The last three weeks came in within two events of each other.
ob.stead.l22  [O]  This week did what the two before it did.
ob.stead.l23  [O]  Nothing in the last three weeks moved far from the others.
ob.stead.l24  [O]  The three weeks sit close enough together to read as one.
ob.stead.l25  [O]  This week did not break the pattern of the two before it.
ob.stead.l26  [O]  The totals for three weeks running are within two of each other.
ob.stead.l27  [O]  There was something in each of the last three weeks.
ob.stead.l28  [O]  {n} completions, where the earlier weeks average {m}.
ob.stead.l29  [O]  The week came in where the last two came in.
ob.stead.l30  [O]  Nothing separated this week from the two behind it.
ob.stead.l31  [O]  Three weeks have now come in at about the same size.
ob.stead.l32  [O]  The record shows three weeks that are hard to distinguish.
ob.stead.l33  [O]  The three most recent weeks are within a couple of events.
ob.stead.l34  [O]  The week landed where the last two landed.
ob.stead.l35  [O]  There is a band and all three weeks are inside it.
ob.stead.l36  [O]  {n} things finished. An ordinary week of yours holds {m}.
ob.stead.l37  [O]  The two weeks before this one were the same size as it.
ob.stead.l38  [O]  Three weeks running, the total has stayed put.
ob.stead.l39  [E]  A week that does not stand out is still a week that happened.
ob.stead.l40  [E]  Three weeks the same size is a pace rather than a coincidence.
ob.stead.l41  [E]  Nothing here needs explaining, which is a finding rather than an absence.
ob.stead.l42  [E]  Steadiness shows up as an absence of anything to say about it.
ob.stead.l43  [E]  There is no story in three weeks the same size, and that is the whole of the observation.
ob.stead.l44  [E]  The weeks that look identical from here were not identical from the inside.
ob.stead.l45  [E]  A pace is what three weeks in a row look like from a distance.
ob.stead.l46  [E]  What the app can see is that the size held. What that was like is not in the record.
ob.stead.l47  [E]  A band is a range rather than a line. Three weeks have stayed inside this one.
ob.stead.l48  [E]  Nothing moved far. That is the observation and there is no second half to it.
ob.stead.l49  [P]  {m} in a normal week.
ob.stead.l50  [O]  No week outgrew the others.
ob.stead.l51  [P]  A week like the last two.
ob.stead.l52  [O]  The pace did not change.
ob.stead.l53  [O]  No week broke away.
ob.stead.l54  [P]  A flat run of three.
ob.stead.l55  [O]  The week held.
ob.stead.l56  [E]  A week that could be swapped with either of the two before it without the record looking any different.
ob.stead.l57  [O]  {n} things were finished this week and {m} is the number an average week of yours has finished before now.
ob.stead.l58  [E]  Three weeks inside a band of two events is a pace, and a pace is the one thing a single week cannot show.
ob.stead.l59  [O]  Each of the last three weeks held something, and none of them held more than two events more than another.
ob.stead.l60  [O]  The two weeks behind this one match it.
```

**Extensions**
```
ob.stead.e01  [O]  The distribution across areas was similar too.
ob.stead.e02  [E]  Consistency is harder to notice than a spike and usually worth more.
ob.stead.e03  [O]  Your queues have stayed the same length throughout.
ob.stead.e04  [O]  {areaName} has led every one of those weeks.
ob.stead.e05  [P]  {m} is the average.
ob.stead.e06  [O]  The two before it matched.
ob.stead.e07  [P]  {n} finished this time.
ob.stead.e08  [E]  There is nothing else in this to read.
ob.stead.e09  [O]  The band across the three weeks is two events wide.
ob.stead.e10  [P]  Three weeks, no change.
ob.stead.e11  [O]  None of the three weeks was empty.
ob.stead.e12  [E]  A run of three is short enough to be chance and long enough to be worth noticing.
ob.stead.e13  [P]  {n} beside the usual {m}.
ob.stead.e14  [O]  Nothing in the three weeks reached far from the middle of them.
ob.stead.e15  [E]  What holds for three weeks may not hold for four.
ob.stead.e16  [O]  The size did not move.
ob.stead.e17  [O]  The last three weeks are within two events of one another, which is the whole basis for calling this a pace.
ob.stead.e18  [E]  Steady is a description and not a verdict.
ob.stead.e19  [O]  Whether the three weeks felt the same from the inside is not something a total can answer.
ob.stead.e20  [O]  All three had something in them.
```

## 2.19 firstMilestone

Fires once per `FirstEver` flag, ever.

**Leads**
```
ob.first.l01  [P]  Your first completion.
ob.first.l02  [P]  Your first focus session, {minutes} minutes in {areaName}.
ob.first.l03  [O]  You swapped an active item for the first time.
ob.first.l04  [O]  An area emptied its queue for the first time.
ob.first.l05  [O]  Every area had something active at once, for the first time.
ob.first.l06  [E]  This week contained something that had not happened before.
```

**Extensions**
```
ob.first.e01  [O]  It took {ageDays} from when you added it.
ob.first.e02  [E]  Firsts are only interesting once, so this is the mention.
ob.first.e03  [O]  There were {n} more after it.
```

## 2.20 areaBalance

**Leads**
```
ob.bal.l01  [P]  {areaName} at {pct}, {otherArea} at {otherPct}.
ob.bal.l02  [O]  Attention split across {areaCount} areas, none above {pct}.
ob.bal.l03  [O]  Every area moved this week.
ob.bal.l04  [E]  No single area owned this week.
ob.bal.l05  [P]  {areaCount} areas active, {n} events between them.
ob.bal.l06  [O]  The gap between your busiest and quietest area was {n} events.
ob.bal.l07  [E]  The week had no owner.
ob.bal.l08  [E]  A week can be wide or it can be deep.
ob.bal.l09  [E]  {areaCount} areas moved and none of them ran the week.
ob.bal.l10  [E]  Nothing here was the main thing.
ob.bal.l11  [E]  A wide week and a thin week look the same from a count, and nothing here tells them apart.
ob.bal.l12  [E]  The week had a shape without a subject.
ob.bal.l13  [E]  Every area got some of the week and none got most of it.
ob.bal.l14  [E]  {pct} is the largest share anything took. It is not a majority.
ob.bal.l15  [E]  Attention went to {areaCount} places and settled in none of them.
ob.bal.l16  [E]  Spread is not the same as balance. This week was spread.
ob.bal.l17  [E]  A week that touches {areaCount} areas and gives none of them half has no single subject.
ob.bal.l18  [O]  {areaCount} areas moved this week.
ob.bal.l19  [O]  Nothing took half the week.
ob.bal.l20  [O]  The largest share was {pct}.
ob.bal.l21  [O]  {areaName} led at {pct}.
ob.bal.l22  [O]  Attention went to {areaCount} areas.
ob.bal.l23  [O]  {areaName} and {otherArea} were the two busiest.
ob.bal.l24  [O]  The week's biggest share was {pct}. It was {areaName}'s.
ob.bal.l25  [O]  {areaCount} areas had something in them this week.
ob.bal.l26  [O]  {areaName} at {pct} was the most any area held.
ob.bal.l27  [O]  {n} events went to {areaCount} areas.
ob.bal.l28  [O]  {otherArea} came second, at {otherPct}.
ob.bal.l29  [O]  Every one of {areaCount} areas has something in this week's record.
ob.bal.l30  [O]  The week did not settle on one area.
ob.bal.l31  [O]  {areaName} was ahead and it was not far ahead.
ob.bal.l32  [O]  Your attention did not pool anywhere.
ob.bal.l33  [O]  The gap between {areaName} and {otherArea} was {pct} against {otherPct}.
ob.bal.l34  [O]  {n} events, with no area taking half of them.
ob.bal.l35  [O]  The week went to {areaCount} areas rather than to one.
ob.bal.l36  [O]  {areaName} took {pct} of the week and the rest went elsewhere.
ob.bal.l37  [O]  {areaCount} areas moved, {areaName} moved most, and the most was {pct}.
ob.bal.l38  [O]  The week spread itself over {areaCount} areas. The biggest of those shares came to {pct}.
ob.bal.l39  [O]  {areaName} at {pct} and {otherArea} at {otherPct} were the top two of {areaCount} areas that moved.
ob.bal.l40  [O]  Something happened in {areaCount} different areas this week, and the largest share of it was {pct}.
ob.bal.l41  [P]  {n} moves, {areaCount} areas.
ob.bal.l42  [P]  {n} events across {areaCount} areas.
ob.bal.l43  [P]  {areaName} first, {otherArea} second.
ob.bal.l44  [P]  {pct} and {otherPct}, the top two.
ob.bal.l45  [P]  {areaCount} in motion.
ob.bal.l46  [P]  The top share was {areaName}'s.
ob.bal.l47  [P]  {areaName} ahead, {otherArea} next.
ob.bal.l48  [P]  Everything under half.
ob.bal.l49  [P]  {n} events in {areaCount} places.
ob.bal.l50  [P]  {areaCount} areas were part of the week.
ob.bal.l51  [P]  The week's {n} events went to {areaCount} areas.
ob.bal.l52  [P]  {areaName} held {pct} and {otherArea} held {otherPct}.
ob.bal.l53  [P]  The largest area share this week was {pct}.
ob.bal.l54  [P]  {areaCount} areas moved and none of them took half.
ob.bal.l55  [P]  The two biggest were {areaName} and {otherArea}.
ob.bal.l56  [P]  {n} events, spread across {areaCount} areas, with {pct} the largest single share.
ob.bal.l57  [P]  The week put {n} events into {areaCount} areas. The largest share of them was {pct}.
ob.bal.l58  [P]  {areaName} took {pct} of the week, {otherArea} took {otherPct}, and {areaCount} areas moved in total.
ob.bal.l59  [P]  Across {areaCount} areas and {n} events, the biggest single share came to {pct} and no more.
ob.bal.l60  [P]  {areaCount} areas, {n} events between them, and no single area holding as much as half.
```

**Extensions**
```
ob.bal.e01  [O]  That is more even than any week this month.
ob.bal.e02  [O]  Completions were less even than events: {n} of {m} were in {areaName}.
ob.bal.e03  [E]  Broad weeks and deep weeks measure different things.
ob.bal.e04  [O]  Last week one area held {otherPct}.
ob.bal.e05  [P]  {areaCount} areas were involved.
ob.bal.e06  [P]  {areaName} was the largest of them, at {pct}.
ob.bal.e07  [P]  {n} events in all.
ob.bal.e08  [P]  {otherArea} was next, at {otherPct}.
ob.bal.e09  [O]  None of them held half.
ob.bal.e10  [O]  The spread is what the week looks like.
ob.bal.e11  [O]  Every one of them moved at least once.
ob.bal.e12  [O]  {pct} was the ceiling this week.
ob.bal.e13  [O]  The rest of the week went to the other areas.
ob.bal.e14  [E]  Wide weeks and narrow weeks are both weeks.
ob.bal.e15  [E]  A week with no center is harder to name than one with a center, and it is not a worse week.
ob.bal.e16  [E]  Nothing about a spread says whether it was chosen.
ob.bal.e17  [O]  {areaName} and {otherArea} came to {pct} and {otherPct} between them.
ob.bal.e18  [O]  Across {areaCount} areas, no share reached half of the week's {n} events.
ob.bal.e19  [E]  An even week gives a report less to point at, which is not the same as less happening.
ob.bal.e20  [O]  The week reached {areaCount} areas and stopped short of half in every one of them.
```

## 2.21 hardStretch

**The difficulty register.** Fires on three or more consecutive quiet weeks combined with growing queues, or a sustained decline across four weeks. Every constraint in `CLARITY_LOGIC_ENGINE.md` 6.4 applies and all are enforced in code: the subject is always the pattern and never the person, it never names or infers an emotional state, never asks a question, never offers help or advice, fires at most once every six weeks, and cannot appear alongside a plan or alongside `selfReportVsData`.

**If any line here reads as consolation rather than observation, the family is removed rather than rewritten.**

**Leads**
```
ob.hard.l01  [N]  Four quiet weeks in a row usually means something outside the app.
ob.hard.l02  [N]  Stretches like this are common and they are not usually about the app.
ob.hard.l03  [N]  A month like this generally has a reason that is not visible here.
ob.hard.l04  [N]  Some months hold more than they can move.
ob.hard.l05  [N]  This has been a long stretch.                          [S]
ob.hard.l06  [N]  Nothing here has moved in a month. That happens.
ob.hard.l07  [N]  A period like this is not a failure of the system you set up.
ob.hard.l08  [N]  The app can only see what it can see, and it has not seen much.
ob.hard.l09  [N]  Three weeks now.                                        [S]
ob.hard.l10  [N]  The stretch is three weeks old.                         [S]
ob.hard.l11  [N]  Nothing here has changed direction.                     [S]
ob.hard.l12  [N]  Three weeks is long enough to be a stretch rather than a week.
ob.hard.l13  [N]  Whatever this stretch is about is not in the record.
ob.hard.l14  [N]  The last three weeks read as one thing rather than three.
ob.hard.l15  [N]  The app has a shape for the last three weeks and no reading of it.
ob.hard.l16  [N]  The report reads three weeks at a time here, and one week of it would say nothing.
```

**Extensions**
```
ob.hard.e01  [N]  The queues are where you left them.
ob.hard.e02  [N]  Nothing has been lost. It is all still here.
ob.hard.e03  [N]  Whatever picks back up will start from exactly this point.
ob.hard.e04  [N]  That is the whole reading.                              [S]
ob.hard.e05  [N]  The rest is not measured here.                          [S]
ob.hard.e06  [N]  What the weeks were like is a different question from what the record has in it.
```

## 2.22 familiarDip

**The second branch of the capacity gate.** `MASTER_BUILD_PROMPT.md` 14b.9. When a fall has a precedent in this person's own record, the decline, neglect and fading families are removed from the ranking and this one speaks instead. Its three rules read the three precedent facts, one for the person's weeks, one for their focus, and one for a single area.

**This is the most careful bench in the volume, and five constraints are held by every line in it.** It never states the depth, the duration or the date of any fall, because `Precedent` carries the verdict and nothing else and no measure can reach one. It never claims the person meant it. It never predicts a return. It may not say `this week`, because the precedent is read over twelve weekly buckets and the newest part week is skipped. And it reads as an observation and not as reassurance, which is the same test 6.4 puts on `hardStretch`.

**It never names what the shape is a precedent for.** The record holds a shape, the shape has occurred before, and everything past that is the person's to know and not the app's to say.

**Leads**
```
ob.fam.l01  [P]  This has happened before.                                [S]
ob.fam.l02  [P]  This is not the first one.                               [S]
ob.fam.l03  [P]  This shape has been in the record before.
ob.fam.l04  [O]  Whatever this stretch turns out to be, the record has one like it further back already.
ob.fam.l05  [O]  A stretch like this one has come around before.
ob.fam.l06  [O]  The record is long enough to hold two of these.
ob.fam.l07  [O]  What the record shows now, it has shown before.
ob.fam.l08  [E]  A first time and a second time read differently.
ob.fam.l09  [E]  The same shape twice is a shape rather than a direction.
ob.fam.l10  [P]  {areaName} has had quiet stretches before.               [S]
ob.fam.l11  [O]  {areaName} has had a stretch like this before this one.
ob.fam.l12  [O]  {areaName} has looked like this before.                  [S]
```

**Extensions**
```
ob.fam.e01  [O]  The record goes back further than this stretch does.
ob.fam.e02  [P]  There is an earlier one.                                 [S]
ob.fam.e03  [E]  A shape that repeats is read differently from a shape that arrives.
ob.fam.e04  [P]  The record is older than this.                           [S]
ob.fam.e05  [O]  Whether the stretch after this one looks the same is not something the record can say yet.
ob.fam.e06  [E]  Once is an event. Twice is a shape.
```

## 2.23 estimateCalibration

**Calibration, never error.** `MASTER_BUILD_PROMPT.md` 14b.8. The reading is a **multiple** and never a percentage, and no line here may state a difference between an estimate and an actual. Validator check 11 refuses both forms whether or not the sentence says estimate.

**Two things every line holds, and both are easy to lose.** The statistic is read over **twelve weeks**, so no line may say `this week`. And it is a **stay and not an effort**: the actual behind the ratio is the elapsed time from the moment an item became active to the moment it was completed, which is the only actual the log holds. Nothing in this app measures time spent working, so a thing estimated at an hour that stays active for a day and a half is a true reading of how estimates map onto days and a false one of how long the work took. `stay active` is the verb this bench uses. `take`, `spend` and `work on` are not.

The bench fires only where the tendency runs long, which is the direction 14b.8's permitted example names and the only one the multiple can render: a stay under half its estimate rounds to nought and the measure answers null, and a stay close to its estimate rounds to one, which is a sentence with nothing in it.

**Leads**
```
ob.est.l01  [N]  Things with an estimate on them tend to stay active about {n} times that estimate.
ob.est.l02  [N]  A thing estimated at an hour tends to be active for about {n} hours.
ob.est.l03  [P]  Things you estimate at an hour tend to stay active about {n} times that long.
ob.est.l04  [P]  About {n} times the estimate, across {m} finished things.
ob.est.l05  [O]  The median estimated thing stays active about {n} times its estimate.
ob.est.l06  [O]  Over twelve weeks the reading has been about {n} times.
ob.est.l07  [E]  An estimate measures the work. A stay measures the days it sat in. They are different numbers.
ob.est.l08  [E]  Nothing here counts hours worked. This is the shape of a plan against a calendar.
```

**Extensions**
```
ob.est.e01  [P]  {m} finished things carried an estimate.                  [S]
ob.est.e02  [P]  The window is twelve weeks.                              [S]
ob.est.e03  [O]  That is a median rather than an average.
ob.est.e04  [O]  The reading covers the last {m} finished things with estimates.
ob.est.e05  [E]  The stay is what is measured here. The effort is not something this app has ever counted.
ob.est.e06  [E]  A ratio is a description of how estimates and days line up. It is not a score.
```

**Observation totals: 1,022 leads and 370 extensions across 23 families.** With combination, roughly **19,700 distinct observation surfaces.**

---

# SECTION 3: PATTERN

Rendered under the sidehead `Pattern`.

At most one per report. Requires three or more weeks of snapshots. This is the only place the Report reaches back across weeks, and it is where the app most feels like it has been watching.

## 3.1 shiftingFocus

```
pt.shift.01  A different area has led each of the last three weeks.
pt.shift.02  Three weeks, three different centers of gravity.
pt.shift.03  {areaName}, then {otherArea}, then {thirdArea}.
pt.shift.04  Your attention has moved every week for three weeks.
pt.shift.05  No area has led twice in a row this month.
pt.shift.06  The focus keeps relocating.
pt.shift.07  Each week has belonged to something different.
pt.shift.08  Three weeks in, nothing has held the lead.
pt.shift.09  The center of your week keeps moving.
pt.shift.10  {areaName} led, then handed over, twice.
```

## 3.2 growingQueues

```
pt.grow.01  Your queues have grown every week for three weeks.
pt.grow.02  Total waiting: {k}, then {m}, then {n}.
pt.grow.03  Nothing has come back down in three weeks.
pt.grow.04  The backlog has risen each week since {sinceRef}.
pt.grow.05  More arrives than leaves, and has for three weeks.
pt.grow.06  The queues have not been shorter since {sinceRef}.
pt.grow.07  Three consecutive weeks of net growth.
pt.grow.08  What is waiting has doubled since {sinceRef}.
pt.grow.09  The line keeps going up.
pt.grow.10  Each week ends with more waiting than it started with.
pt.grow.11  {n} waiting now, {m} last week, {k} the week before.
pt.grow.12  The total is higher than last week, and last week was higher than the one before it.
pt.grow.13  Two weeks of increase, back to back.
pt.grow.14  The queues went up, then up again.
pt.grow.15  More is waiting than a week ago.
pt.grow.16  More is waiting than two weeks ago.
pt.grow.17  The number waiting has not fallen in two weeks.
pt.grow.18  Up, and up again.
pt.grow.19  From {k} to {m} to {n}.
pt.grow.20  Nothing has shortened.
pt.grow.21  The queues are longer than they were.
pt.grow.22  Each of the last two weeks ended higher than the one before.
pt.grow.23  {n} things are waiting now. Last week it was {m}.
pt.grow.24  What is waiting has gone up twice running.
pt.grow.25  The queues took on more than they gave back.
pt.grow.26  Two increases in a row.
pt.grow.27  The direction has been one way for two weeks.
pt.grow.28  The end of each week has been higher than the one before it.
pt.grow.29  {m} at the end of last week, {n} at the end of this one.
pt.grow.30  Longer than last week. Longer than the week before that.
pt.grow.31  Twice in a row the queues finished the week higher.
pt.grow.32  {k} two weeks back, {n} now.
pt.grow.33  A rise, then another rise.
pt.grow.34  Two weeks, two increases.
pt.grow.35  The queues ended higher than they started, twice over.
pt.grow.36  {n} now, up from {m}.
pt.grow.37  {n} now, up from {k} two weeks ago.
pt.grow.38  The queues are carrying more than they were.
pt.grow.39  Growth in the queues, two weeks running.
pt.grow.40  The last two weeks both ended with more.
pt.grow.41  The queues have not given anything back.
pt.grow.42  More waiting at the end of this week than at the end of last.
pt.grow.43  The number has climbed twice.
pt.grow.44  {m} became {n}.
pt.grow.45  Two consecutive weekly increases sit in the record.
pt.grow.46  The queues are at {n}, and they were at {k} two weeks ago.
pt.grow.47  Nothing here says why the queues are longer.
pt.grow.48  The record has two weekly increases in a row and nothing between them going the other way.
pt.grow.49  Whether the queues are longer because more arrived or because less left is not something this reading separates.
pt.grow.50  The queues held {k} two weeks ago, {m} last week and {n} now, which is up twice.
pt.grow.51  What is waiting has been going in one direction for two weeks, and the reading does not say for how much longer.
pt.grow.52  The queues are longer than they were a week ago and longer than they were two weeks ago.
pt.grow.53  Up twice.
pt.grow.54  {n} waiting.
pt.grow.55  Higher again.
pt.grow.56  The queues grew.
pt.grow.57  Two weeks up.
pt.grow.58  Still rising.
pt.grow.59  The queues held {k} at the end of one week, {m} at the end of the next and {n} at the end of this.
pt.grow.60  Two weekly readings in a row came in above the one before them, which is the whole of what this section is saying.
```

## 3.3 improvingThroughput

```
pt.imp.01  Completions have risen three weeks running.
pt.imp.02  {k}, then {m}, then {n}.
pt.imp.03  You have finished more each week for three weeks.
pt.imp.04  The pace has climbed every week since {sinceRef}.
pt.imp.05  Three consecutive weeks of more.
pt.imp.06  Output has increased every week this month.
pt.imp.07  Something has been building since {sinceRef}.
pt.imp.08  The trend has been upward for three weeks.
```

## 3.4 decliningActivity

Written with particular care. It states a fact and stops.

```
pt.dec.01  Total activity has fallen three weeks running.
pt.dec.02  {k}, then {m}, then {n}.
pt.dec.03  Each of the last three weeks was quieter than the one before.
pt.dec.04  The pace has dropped every week since {sinceRef}.
pt.dec.05  Three consecutive weeks of less.
pt.dec.06  Activity has been declining since {sinceRef}.
pt.dec.07  The trend has been downward for three weeks.
pt.dec.08  Less has happened here each week for three weeks.
```

## 3.5 areaGoneQuiet

The flagship pattern line, and the one that most demonstrates memory.

```
pt.gone.01  {areaName} went quiet {ageDays} ago. It has not moved since.
pt.gone.02  {areaName} has had no activity in three weeks.
pt.gone.03  {areaName} has been silent since {sinceRef}.
pt.gone.04  There has been nothing in {areaName} for {ageDays}.
pt.gone.05  {areaName} used to move every week. It has not moved in three.
pt.gone.06  {areaName} still exists. It has not done anything since {sinceRef}.
pt.gone.07  Three weeks of nothing in {areaName}.
pt.gone.08  {areaName} holds {n} things and has not touched any of them in {ageDays}.
pt.gone.10  Whatever {areaName} was for, it has not been for it since {sinceRef}.
```

## 3.6 consistentRhythm

```
pt.rhy.01  Your activity has stayed within a narrow band for four weeks.
pt.rhy.02  Four weeks, all within {n} events of each other.
pt.rhy.03  The pace has not changed meaningfully in a month.
pt.rhy.04  Nothing has spiked or dropped since {sinceRef}.
pt.rhy.05  A steady month.
pt.rhy.06  Four weeks of roughly the same shape.
pt.rhy.07  Consistency, more than a month of it.
pt.rhy.08  Same rhythm, four weeks running.
pt.rhy.09  Four weeks with nearly the same total in each.
pt.rhy.10  The four weekly totals are within {n} of each other.
pt.rhy.11  Nothing in the last month moved far from the middle.
pt.rhy.12  A month without a spike or a dip.
pt.rhy.13  The last four weeks look like each other.
pt.rhy.14  A month at one level.
pt.rhy.15  Four weeks of the same size.
pt.rhy.16  The highest week and the lowest are {n} apart.
pt.rhy.17  Nothing has moved much in a month.
pt.rhy.18  Four weeks in a row within a band of {n}.
pt.rhy.19  A month of weeks that resemble each other.
pt.rhy.20  No week in the last four stood out.
pt.rhy.21  Four weeks, one level.
pt.rhy.22  The totals have sat close together for a month.
pt.rhy.23  A month without a big week or a small one.
pt.rhy.24  The spread across four weeks is {n} events.
pt.rhy.25  Four flat weeks.
pt.rhy.26  The pace has held.
pt.rhy.27  A month that reads as one thing.
pt.rhy.28  Four weeks, all close.
pt.rhy.29  The last month has one shape.
pt.rhy.30  A month of similar weeks.
pt.rhy.31  No week in the month broke away.
pt.rhy.32  The gap between the busiest and the quietest week is {n}.
pt.rhy.33  A steady four weeks.
pt.rhy.34  A month with no week larger than the rest by much.
pt.rhy.35  The last four weeks came in level.
pt.rhy.36  The month kept one pace.
pt.rhy.37  Four weeks without a change worth naming.
pt.rhy.38  The record for the last month is level.
pt.rhy.39  No week rose above the others.
pt.rhy.40  Four weeks that hold the same line.
pt.rhy.41  The totals did not move by more than {n}.
pt.rhy.42  The four weeks are the same week, near enough.
pt.rhy.43  A month of one size.
pt.rhy.44  Four weeks, all within reach of each other.
pt.rhy.45  The last four weeks are within {n} events top to bottom.
pt.rhy.46  Four weeks of the same weight.
pt.rhy.47  The month is flat. Flat is a shape too.
pt.rhy.48  What the last four weeks have in common is their size.
pt.rhy.49  The busiest week of the last four and the quietest are {n} events apart.
pt.rhy.50  Nothing in four weeks has been far from the others, which is what a rhythm looks like from the inside.
pt.rhy.51  There is no week in the last four that the other three do not resemble.
pt.rhy.52  A month of weeks within {n} events of each other is a month with a pace rather than a set of separate weeks.
pt.rhy.53  The four weeks behind this one came in within {n} events of each other, top to bottom.
pt.rhy.54  A month can be steady without being still, and this one had a total in every week of it.
pt.rhy.55  Nothing in the last four weeks was far enough from the others to change what the month looks like.
pt.rhy.56  The difference between the fullest of the last four weeks and the emptiest of them is {n} events.
pt.rhy.57  Four weeks that sit within {n} events of one another is a pace, and a pace is a different fact from a total.
pt.rhy.58  Four weeks, one pace.
pt.rhy.59  Four weeks alike.
pt.rhy.60  A month without news.
```

## 3.7 narrowingFocus

```
pt.narrow.01  Your attention has concentrated further each week for three weeks.
pt.narrow.02  {areaName}'s share has gone {k}, {m}, {pct}.
pt.narrow.03  Fewer areas have moved each week.
pt.narrow.04  The week keeps getting narrower.
pt.narrow.05  Three weeks ago you touched {n} areas. This week, {m}.
pt.narrow.06  Focus has been tightening since {sinceRef}.
pt.narrow.07  Each week has been more about one thing than the last.
pt.narrow.08  The spread keeps shrinking.
```

## 3.8 broadeningFocus

```
pt.broad.01  Your attention has spread wider each week for three weeks.
pt.broad.02  Three weeks ago you touched {n} areas. This week, {m}.
pt.broad.03  No area has held a majority for three weeks.
pt.broad.04  The week keeps getting wider.
pt.broad.05  {areaName}'s share has fallen each week since {sinceRef}.
pt.broad.06  More areas have moved each week.
pt.broad.07  The spread keeps growing.
```

## 3.9 focusHabitForming

```
pt.hab.01  Focus sessions have appeared every week since {sinceRef}.
pt.hab.02  {k}, then {m}, then {n} sessions.
pt.hab.03  Protected time has increased every week for three weeks.
pt.hab.04  Four consecutive weeks with focus time in them.
pt.hab.05  This has stopped being occasional.
pt.hab.06  Focus sessions have been part of every week for a month.
pt.hab.07  Something that started as a one off has held for four weeks.
```

## 3.10 focusHabitFading

```
pt.fade.01  Focus sessions have fallen every week for three weeks.
pt.fade.02  {k}, then {m}, then {n} sessions.
pt.fade.03  Protected time has been dropping since {sinceRef}.
pt.fade.04  There has been no focus time in two weeks.
pt.fade.05  The last focus session was {ageDays} ago.
pt.fade.06  Focus was weekly in {sinceRef}. It has not been since.
```

## 3.11 reportedVsActual

**Callback pattern.** Compares what the user has been saying across weeks against what the data shows.

```
pt.rva.01  You have answered deep work {n} times about {itemTitle}. It is still active.
pt.rva.02  You have called the queue growth deliberate {n} weeks running.
pt.rva.03  Three times you have said {priorLabel} about {areaName}.
pt.rva.04  Your answers have been consistent. So has the pattern behind them.
pt.rva.05  You have described this as {priorLabel} in each of the last three weeks.
pt.rva.06  The same answer, three weeks in a row, about the same thing.
pt.rva.07  You said {priorLabel} in {sinceRef}, and again this week.
pt.rva.08  The same question, three times.
pt.rva.09  One subject, answered more than twice.
pt.rva.10  The question came back.
pt.rva.11  You answered. Then again.
pt.rva.12  A repeated question.
pt.rva.13  The record holds five answers.
pt.rva.14  More than two answers, one subject.
pt.rva.15  Three weeks of answers.
pt.rva.16  The same prompt, again.
pt.rva.17  You have been asked repeatedly.
pt.rva.18  Answers, and then more answers.
pt.rva.19  One thing, three readings.
pt.rva.20  The app kept asking.
pt.rva.21  Asked three times, answered three times.
pt.rva.22  Three answers, one subject.
pt.rva.23  The same ground, more than twice.
pt.rva.24  A subject that keeps returning.
pt.rva.25  Three moments, all about one thing.
pt.rva.26  Answers accumulate.
pt.rva.27  This was not the first asking.
pt.rva.28  One thing here has been asked about more than twice.
pt.rva.29  You have answered at least five pulses since you started.
pt.rva.30  The same kind of question has come back to you three times.
pt.rva.31  There are three weeks of answers behind this reading.
pt.rva.32  One subject has produced three answers of its own.
pt.rva.33  The app has asked about one thing more than once, and more than twice.
pt.rva.34  Five answers or more sit in the record now.
pt.rva.35  Something here has been worth asking about repeatedly.
pt.rva.36  Three separate moments, and the same subject under all three.
pt.rva.37  You have given this app more than a handful of answers.
pt.rva.38  The questions kept landing on the same subject.
pt.rva.39  Three answers about one family of question, over three weeks or more.
pt.rva.40  What you said is in the record, three times over.
pt.rva.41  A question the app has asked you more than twice.
pt.rva.42  The record is three weeks deep and holds five answers at least.
pt.rva.43  One kind of moment has come around three times.
pt.rva.44  Your answers to one kind of question now number three.
pt.rva.45  There is a subject here that the app has raised more than twice.
pt.rva.46  The same situation has come up enough times to have answers stacked against it.
pt.rva.47  You have answered at least five pulses, and three of them were about the same kind of moment.
pt.rva.48  The app has asked about one thing three times, and you answered each time it did.
pt.rva.49  There are three weeks of history here and five answers or more inside them, three of them about one subject.
pt.rva.50  A question that comes back three times is a question about something that did not resolve between the askings.
pt.rva.51  Three answers about one kind of moment is not a verdict on anything, and it is a record that the moment kept happening.
pt.rva.52  What you said at the time is stored exactly as you said it, and there are five of those at least.
pt.rva.53  One subject has produced three answers, and the record holds at least five answers in total.
pt.rva.54  An answer is a reading taken at a moment, and this app now holds five of yours or more.
pt.rva.55  The same kind of question has come around three times, which says more about the situation than about the answers.
pt.rva.56  Three answers about one kind of moment, given across three weeks or more, sit in the record behind this page.
pt.rva.57  The app asked, you answered, and then the app asked again, and that has happened three times about one subject.
pt.rva.58  Nothing here compares what you said against what happened. It records that you said something, three times.
pt.rva.59  Five answers is enough for the record to have a shape, and three of them point at the same kind of moment.
pt.rva.60  The questions this app asks come from what the days looked like, and one kind of day has come around three times.
```

## 3.12 queueEquilibrium

```
pt.eq.01  Your queues have held the same length for four weeks.
pt.eq.02  What goes in and what comes out have matched for a month.
pt.eq.03  The backlog has neither grown nor shrunk since {sinceRef}.
pt.eq.04  Four weeks of balance between intake and output.
pt.eq.05  The queues have been stable for a month.
```

## 3.13 weekendShift

```
pt.wknd.01  Nothing has happened on a weekend in four weeks.
pt.wknd.02  Weekends have been silent since {sinceRef}.
pt.wknd.03  {pct} of your activity has been on weekdays for a month.
pt.wknd.04  Your weeks end on Friday, consistently.
pt.wknd.05  Saturdays and Sundays have not registered in four weeks.
```

## 3.14 abandonmentPattern

```
pt.ab.01  More focus sessions have ended early than finished, three weeks running.
pt.ab.02  Sessions have been getting shorter each week.
pt.ab.03  {n} of your last {m} sessions ended before the timer.
pt.ab.04  Started sessions have outnumbered finished ones since {sinceRef}.
```

## 3.15 comebackPattern

```
pt.come.01  {areaName} has gone quiet and returned twice since {sinceRef}.
pt.come.02  This is the second time {areaName} has come back from a long gap.
pt.come.03  {areaName} moves in bursts, with weeks of nothing between them.
pt.come.04  {areaName} has never been active two weeks in a row.
pt.come.05  Every time {areaName} returns, it lasts about {ageDays}.
pt.come.06  {areaName} has stopped and started more than once.
pt.come.07  This is not the first time {areaName} has come back.
pt.come.08  {areaName} has been quiet more than once and is not quiet now.
pt.come.09  There is more than one silence in {areaName}'s record.
pt.come.10  {areaName} keeps stopping and starting.
pt.come.11  {areaName} has come back from quiet at least twice.
pt.come.12  {areaName} is active now and has been quiet before.
pt.come.13  {areaName} has a stop and start shape.
pt.come.14  Nothing here explains why {areaName} comes and goes.
pt.come.15  {areaName} has returned from silence more than once.
pt.come.16  This is at least the second return for {areaName}.
pt.come.17  The pattern in {areaName} is off and on.
pt.come.18  Two silences at least, both over, in {areaName}.
pt.come.19  {areaName} stops. {areaName} starts.
pt.come.20  Whatever stops {areaName} has stopped it before.
pt.come.21  {areaName} has more than one quiet week behind it.
pt.come.22  The quiet weeks in {areaName} have not been the end of it.
pt.come.23  Silence in {areaName} has ended more than once.
pt.come.24  {areaName} has a history of going quiet and coming back.
pt.come.25  The gaps in {areaName} are part of its record.
pt.come.26  {areaName} has restarted at least twice.
pt.come.27  {areaName} runs in spells.
pt.come.28  {areaName} has a rhythm the app can see and cannot read.
pt.come.29  {areaName} does not go for good.
pt.come.30  Every quiet spell in {areaName} so far has ended.
pt.come.31  This has happened to {areaName} before.
pt.come.32  Nothing in {areaName} has been continuous.
pt.come.33  {areaName} has emptied out and filled again, more than once.
pt.come.34  There is a pattern in {areaName} and it is not a straight line.
pt.come.35  {areaName} moves, stops, and moves again.
pt.come.36  The silences in {areaName} have all been temporary so far.
pt.come.37  {areaName} has come back at least twice. This week it is here.
pt.come.38  Nothing says whether the gaps in {areaName} were chosen.
pt.come.39  {areaName} did not stay quiet.
pt.come.40  {areaName} is back, again.
pt.come.41  Two quiet stretches at least. {areaName} is out of both.
pt.come.42  What {areaName} does is stop and start.
pt.come.43  {areaName} is moving. It has not always been.
pt.come.44  {areaName} comes and goes.
pt.come.45  {areaName} restarts.
pt.come.46  {areaName} has done this before.
pt.come.47  A second silence, also over.
pt.come.48  {areaName} started again.
pt.come.49  Stop, start, stop, start.
pt.come.50  {areaName} paused more than once.
pt.come.51  The gaps ended.
pt.come.52  {areaName} stopped twice at least.
pt.come.53  The record shows {areaName} going quiet and then moving again, and it shows that happening more than once.
pt.come.54  A gap in {areaName} has ended at least twice, which is what makes this a shape rather than a single quiet spell.
pt.come.55  Whether the quiet weeks in {areaName} were a decision or an interruption is not something the app has any way to know.
pt.come.56  {areaName} has gone quiet and started again more than once, and the weeks in between held nothing at all.
pt.come.57  Two things are true of {areaName}: it has been quiet for whole weeks, and it is not quiet now.
pt.come.58  This is a second return at least, so the quiet weeks in {areaName} are a pattern rather than one lapse.
pt.come.59  The app can count the times {areaName} stopped and the times it started, and that is the whole of what it knows here.
pt.come.60  Nothing in the record says how long the quiet weeks in {areaName} lasted, only that there were more than one of them.
```

## 3.16 insufficientData

The single faint line shown when there are fewer than three weeks of snapshots.

```
pt.none.01  Patterns will appear after a few weeks of use.
pt.none.02  Three weeks of history is where patterns start.
pt.none.03  Not enough weeks yet to see a shape.
pt.none.04  This section fills in after a few more weeks.
```

**Pattern totals: 321 lines across 16 families.**

---

# SECTION 4: THE CLOSING LINE

Rendered under the sidehead `One thing`. **Exactly one line per report**, produced by layer 6 of the engine.

This is the only place the application ever offers guidance, and it is therefore the most dangerous section in the corpus. Every line here was checked against four tests: it is a single concrete act, it is completable inside a week, it implies no failure, and it survives being read preceded by *You should have*.

## 4.0 How a plan is built

Three benches combine. **Frame, cue, action.**

The offered form is **nominal, never imperative**. On acceptance the plan is re-rendered in **first person** and that is the only form shown afterwards. The imperative never exists anywhere in the app.

> Offered: *One option for Wednesday morning: ten minutes in Personal before you open Work.*
> Stored: *If it's Wednesday morning, I'll spend ten minutes in Personal before opening Work.*

Rendering: `{frame}` takes `{cue}` and `{action}` as slots. Cue lines carry no terminal punctuation. Action lines are gerund phrases with no leading capital.

## 4.1 Frames

```
frm.01  One option for {cue}: {action}.
frm.02  If you want one thing to aim at {cue}: {action}.
frm.03  {cue} might be the moment for {action}.
frm.04  Something to consider {cue}: {action}.
frm.05  A small one {cue}: {action}.
frm.06  There is room {cue} for {action}.
frm.07  {cue} would suit {action}.
```

## 4.2 Cue bank

Each cue names the `CueFacts` field it requires. A cue that cannot be substantiated is unavailable. Confidence thresholds in `CLARITY_LOGIC_ENGINE.md` 3.7 are mandatory.

### Weekday, requires `strongestWeekday`
```
cue.day.01   {strongestWeekday}
cue.day.02   {strongestWeekday}, your busiest day
cue.day.03   when {strongestWeekday} comes around
cue.day.04   before {strongestWeekday} is over
cue.day.05   the day you usually get most done
```

### Part of day, requires `productiveBand`
```
cue.band.01  {strongestWeekday} morning
cue.band.02  the morning, when you finish most things
cue.band.03  before midday, where most of your completions land
cue.band.04  your first hour
cue.band.05  early on {strongestWeekday}
```

### Behavioral, anchored to an existing habit
```
cue.hab.01   before you open {areaName}
cue.hab.02   after your next focus session
cue.hab.03   before you add anything new
cue.hab.04   the next time you finish something
cue.hab.05   before you start on {areaName} again
cue.hab.06   the next time you open the app
```

### Boundary
```
cue.bound.01 before the week ends
cue.bound.02 before {quietestWeekday}
cue.bound.03 at the start of next week
cue.bound.04 before Friday
cue.bound.05 your next quiet evening
```

## 4.3 Action bank

Nominal, never imperative. An action pairs with a cue only when its motivating family appeared in the report.

### From `intakeVsOutput` or `queuePressure`
```
act.fin.01   finishing one thing already waiting in {areaName}
act.fin.02   closing the oldest item in {areaName}
act.fin.03   picking the smallest thing in a queue and finishing it
act.fin.04   clearing one item before adding another
act.fin.05   taking {itemTitle} off the queue for good
act.fin.06   finishing something before writing anything down
act.fin.07   moving one queued item all the way through
act.fin.08   letting one thing leave before the next arrives
act.fin.09   the easiest item in {areaName}
act.fin.10   fifteen minutes on whatever is nearest to finished
```

### From `neglectedArea`
```
act.neg.01   ten minutes in {areaName}
act.neg.02   opening {areaName} and reading what is in it
act.neg.03   the smallest thing {areaName} is holding
act.neg.04   one item and one hour in {areaName}
act.neg.05   deciding whether {areaName} stays or goes to the archive
act.neg.06   looking at {areaName} before anything else
act.neg.07   putting one new thing at the front of {areaName}
act.neg.08   finding out whether {areaName} still matters to you
act.neg.09   moving anything at all in {areaName}
act.neg.10   reading {areaName}'s queue and cutting what is dead
```

### From `singleFocus`
```
act.oth.01   opening {otherArea} before {areaName}
act.oth.02   one thing outside {areaName}
act.oth.03   fifteen minutes in {otherArea}
act.oth.04   checking what the other areas have been holding
act.oth.05   starting somewhere other than {areaName}
act.oth.06   letting {areaName} wait and doing something else first
act.oth.07   the first half hour anywhere but {areaName}
```

### From `persistentItem`
```
act.brk.01   writing down what {itemTitle} is actually waiting on
act.brk.02   replacing {itemTitle} with the first step of it
act.brk.03   twenty minutes on {itemTitle} and nothing else
act.brk.04   deciding whether {itemTitle} is one thing or several
act.brk.05   adding a note to {itemTitle} saying what is in the way
act.brk.06   putting the smaller version of {itemTitle} at the front instead
act.brk.07   one uninterrupted hour on {itemTitle}
act.brk.08   naming the obstacle, even in three words
```

### From `focusInvestment` or `focusHabitFading`
```
act.foc.01   one focus session
act.foc.02   whatever you did last {focusTypicalWeekday}
act.foc.03   twenty five protected minutes for {areaName}
act.foc.04   a session before anything else
act.foc.05   one session in before the week fills up
act.foc.06   the same length of time that worked before
```

### From `queueDrained` or an idle area
```
act.pick.01  giving {areaName} something to hold
act.pick.02  one thing at the front of {areaName}
act.pick.03  deciding what {areaName} is about this month
act.pick.04  writing down the next thing for {areaName}
act.pick.05  one item for {areaName}, or the archive
```

### From `switchingBehavior`
```
act.set.01   picking one item in {areaName} and leaving it there
act.set.02   finishing what is at the front of {areaName} before changing it again
act.set.03   deciding what {areaName} is actually for this week
act.set.04   one thing in {areaName}, until it is done
```

### From `dayShape` or `timeOfDay`
```
act.rep.01   whatever you did last {strongestWeekday}
act.rep.02   the same hours that worked
act.rep.03   starting at the time you usually finish things
act.rep.04   the hardest item in your best hour
```

## 4.4 Commitment rendering

On accept, the plan re-renders in first person. Cue and action map to a fixed grammar:

```
com.01  If it's {cue}, I'll {actionVerb}.
com.02  {cue}, I'm {actionGerund}.
com.03  My one thing {cue}: {actionNoun}.
```

Each action carries a verb form alongside its gerund so `com.01` can render. `act.neg.01` gerund is `ten minutes in Personal`; verb form is `spend ten minutes in Personal`.

## 4.5 The accept and decline labels

Fixed, never varied. Varying them would make the choice feel like a game.

```
Accept:  I'll do that
Decline: Not this week
```

Declining writes nothing, costs nothing, is never counted, never referenced. Ignoring both is identical to declining.

## 4.6 Non-plan closings

Complete lines, not frame plus cue plus action. Used when layer 6 cannot or should not produce a plan.

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

### `noRhythmYet`, when cues have not stabilized
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

## 4.7 Worked plans

Real combinations, showing what the user actually sees.

> **One option for Wednesday morning: ten minutes in Personal before you open Work.**
> `frm.01` + `cue.band.01` + `act.neg.01`, motivated by `neglectedArea`.

> **If you want one thing to aim at before you add anything new: closing the oldest item in Work.**
> `frm.02` + `cue.hab.03` + `act.fin.02`, motivated by `intakeVsOutput`.

> **After your next focus session might be the moment for writing down what Rewrite the proposal intro is actually waiting on.**
> `frm.03` + `cue.hab.02` + `act.brk.01`, motivated by `persistentItem`.

> **Something to consider before Friday: deciding whether Reading stays or goes to the archive.**
> `frm.04` + `cue.bound.04` + `act.neg.05`, motivated by `neglectedArea` at stage 2.

## 4.8 Totals

| bank | lines |
|---|---|
| Frames | 7 |
| Cues | 21 |
| Actions | 54 |
| Commitment forms | 3 |
| Non-plan closings | 24 |
| **Total** | **109** |

Family compatibility constrains pairing, yielding roughly **4,500 valid plan surfaces** plus 24 non-plan closings. Fewer raw lines than the imperative version and considerably more surfaces, because every line now does two jobs.

## 4.9 Authoring rules for guidance

1. **Every action is one concrete act completable in a week.** If it cannot be finished in a sitting it is a project, not an action
2. **Every action names a thing.** `ten minutes in Personal`, not `some attention on an area`. A slot that cannot be filled means the line cannot fire
3. **The permission test.** Read each action preceded by *You should have*. If it still parses naturally, rewrite it
4. **Cues are things the app has observed, never things it assumes.** No `before bed`, no `at the weekend`, no `when you have a moment`. If the data cannot substantiate it, it does not exist
5. **Every cue must read grammatically inside every frame and before every action in its compatible families.** Write it out against all of them
6. **No imperatives, no exclamation marks, no `try to`.** Actions are gerund phrases

---

# SECTION 5: FOOTER AND BASIS

## 5.1 Generated line

Always present. Fixed, not varied, because it is a factual claim and varying it would weaken it.

```
Generated on your device
```

## 5.2 Basis line

Clauses are omitted when their value is zero. The whole line is omitted when everything is zero.

```
bs.01  Based on {n} Pulse responses, and {m} weeks of data.
bs.02  Based on {n} Pulse response, and {m} weeks of data.
bs.03  Based on {m} weeks of data.
bs.04  Based on {n} Pulse responses.
bs.05  Based on {n} Pulse response.
bs.06  Based on your first week.
```

Singular and plural forms are separate lines rather than a runtime substitution, so no `1 responses` can ever occur.

---

# SECTION 6: EDGE STATES

## 6.1 Nothing to report

Replaces the entire body when there is genuinely no activity and no history.

```
ed.none.01  Nothing to report yet. Add items to your areas and check back.
ed.none.02  There is nothing here yet. That changes as soon as something moves.
ed.none.03  No activity to write about. Come back after a few days.
ed.none.04  This page fills in as the days do.
ed.none.05  There is nothing to read yet. That takes a few days of use.
ed.none.06  Patterns show up after about three weeks. The rest arrives sooner.
```

## 6.2 First week

```
ed.first.01  Your first week. There is not much to compare against yet.
ed.first.02  Week one. The report gets more useful as history builds.
ed.first.03  This is the first of these. It will say more in a month.
ed.first.04  Comparisons need a second week. Patterns need about three.
ed.first.05  The sections that need more history are not here yet. They fill in week by week.
ed.first.06  Week one has no earlier week to set against it.
```

---

# SECTION 7: COMPOSITION RULES

The Report emits many sentences at once, which creates a class of failure Pulse cannot have: two true sentences that contradict each other in tone or implication. These rules are enforced by code, not by authoring care.

## 7.1 The incompatibility matrix

No report may contain both members of any of these pairs:

| A | B | why |
|---|---|---|
| `singleFocus` | `areaBalance` | one says narrow, the other says broad |
| `quietWeek` | `personalBest` | contradictory |
| `quietWeek` | `focusInvestment` | a quiet week that also protected eight hours reads as broken |
| `intakeVsOutput` stage 2 | `intakeVsOutput` stage 3 | opposite directions |
| `neglectedArea` | `areaRevival` **on the same area** | the same area cannot be both |
| `steadyPace` | `personalBest` | a record is not steady |
| `steadyPace` | `mostActiveSince` | same |
| `selfReportVsData` | a declining headline | pile-on |
| `selfReportVsData` | `neglectedArea` | pile-on |
| `selfReportVsData` | itself, same subject, ever | rarity is what gives it force |
| `hardStretch` | any plan | a hard stretch does not also get homework |
| `hardStretch` | `selfReportVsData` | pile-on |
| headline `hd.quiet` | any observation family other than `quietWeek`, `neglectedArea`, `persistentItem` | the headline set the frame |
| headline `hd.single` | observation `areaBalance` | frame conflict |
| headline `hd.bal` | observation `singleFocus` | frame conflict |

The selector applies this matrix after ranking and before taking the second, third and fourth observations. When a conflict arises, the lower ranked family is dropped and the next non conflicting one is considered.

## 7.2 Headline agreement

The headline is selected first and constrains everything after it. An observation whose family conflicts with the chosen headline is excluded from consideration entirely, not merely deprioritized.

## 7.3 One area, one mention

No single area may be named in more than two of the four observations. Beyond that, the report reads as being about one area rather than about the week, and the `PATTERN` and `One thing` sections lose their weight.

## 7.4 Length variation

No two consecutive leads may come from the same length band. `S` under 7 words, `M` 7 to 14, `L` 15 to 24. This forces rhythm without anyone writing to a word count.

## 7.4b Parallel clause cap

No more than two parallel numeric clauses may appear consecutively. Where a third would follow, the composer drops it or re-realizes it at a different length. The three-part list is a rhetorical reflex and once a reader sees it they cannot stop seeing it.

## 7.4c Construction cap

No rhetorical construction may appear in more than two families. Two specific moves are each retained in exactly one place:

- **Negate then restate**, *X did not dominate the week. It was the week.* Retained in `ob.single.s2.l06` only.
- **"At some point X stops being Y"**, retained in `accumulation.s3.07` only.

A catalog test enforces this.

## 7.4d Opener variation

`That is` may open at most two extensions per report. The composer tracks openers and re-realizes beyond that.

## 7.5 Register balance

At most two `[E]` editorial leads per report. Editorial voice is powerful in small quantities and exhausting in large ones. If ranking produces three or more, the lowest ranked editorial lead is re-realized in `[O]`.

## 7.5 The ONE THING must follow

The `One thing` line is derived from the highest priority observation that actually appeared. It may never advise on something the report did not mention. A report that never mentioned queues cannot end with advice about queues.

## 7.6 Number consistency

If two observations reference the same underlying count, they must state the same number. The composer holds a map of every rendered numeric slot and its `FactRef` across the whole report and vetoes the whole report if the same `FactRef` renders two different values, which would indicate a fact recomputation bug rather than a copy problem.

---

# CORPUS TOTALS, VOLUME 2

Counted from the keyed lines in this file, not estimated.

| section | authored lines | distinct surfaces |
|---|---|---|
| Headlines | 360 | 360 |
| Observation leads | 1,022 | ~19,700 combined with extensions |
| Observation extensions | 370 | |
| Patterns | 321 | 321 |
| Guidance frames | 7 | ~4,500 plan surfaces |
| Guidance cues | 21 | |
| Guidance actions | 54 | |
| Commitment forms | 3 | |
| Non-plan closings | 24 | 24 |
| Basis and edge states | 18 | 18 |
| **Total** | **2,200** | **~24,900** |

Plan surfaces are frames times compatible cues times actions. Not every cue pairs with every action, so the figure assumes roughly twelve of the twenty one cues are valid for a given action.

A report contains one headline, two to four observations, at most one pattern and one closing. At 52 reports a year a user sees roughly 400 report sentences annually. With these benches and the 90 day exclusion, the same headline would not recur for several years and a full report would never repeat.

**Combined with volume 1: 3,975 authored lines, roughly 317,000 distinct surfaces.** The surface figure is a product of benches rather than a count of lines, so it is the only number here that is approximate on purpose; every counted figure above is recounted by `CorpusTotalsAuditTest` on every run. This is the reading taken when volume 2 closed, so it is the settled one rather than a figure still owed: volume 1 closed first at 1,775 lines and volume 3 before it at 758, and nothing in either moves again for this phase. The figure it replaces was 3,649, which assumed 1,874 Report lines against the 1,617 the file carried at the time; the audit had been naming that disagreement in three files before this pass corrected it.

These counts are the **current** state, not the target. Section 11.1 of `CLARITY_LOGIC_ENGINE.md` gives the sizing targets, and phase 9 of the build grows the hot families toward them.

---

# AUTHORING RULES SPECIFIC TO THE REPORT

Everything in volume 1's authoring rules applies, plus:

1. **Every extension must read correctly after every lead in its family and stage.** Write it out against all of them.
2. **An extension may never introduce a fact the lead did not license.** If the lead is about focus minutes, the extension may compare focus minutes; it may not pivot to queue length.
3. **Editorial register must earn itself.** An `[E]` line is only appropriate when the underlying fact is genuinely notable. Editorial voice attached to an ordinary fact is the clearest tell of generated writing.
4. **The ONE THING line must pass three tests:** it is a single concrete action, it is small enough to do this week, and it implies no failure. Any line that reads as a correction rather than a suggestion is rewritten.
5. **`letItBe` and `trustThePace` must be reachable often enough to matter.** A report that always has advice is a report that is inventing problems. The simulator asserts that layer 6 returns a non-plan closing or nothing on at least 15 percent of reports across the persona set. The review panel raised this from 12 when the guidance system was added.
6. **Callback families require the strictest review.** A fabricated quote is the one output that destroys the app. Every lead in `selfReportVsData` and `completionSplit` must be traceable to a stored `responseLabel`.
7. **Decline and neglect families get the mirror test twice.** Read as a friend speaking, then read on the worst week you have had. If it stings on the second reading, rewrite it.
