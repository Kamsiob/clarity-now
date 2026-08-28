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

**Headline totals: 158 lines across 17 families.**

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
```

**Extensions**
```
ob.flow.s1.e01  [O]  Most of the new items went to {areaName}.
ob.flow.s1.e02  [O]  Last week the balance went the other way.
ob.flow.s1.e03  [O]  {k} of what you added is still untouched.
ob.flow.s1.e04  [E]  A small gap one week is noise. Three weeks is a pattern.
ob.flow.s1.e05  [O]  Nothing added on {dayName} has moved yet.
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
```

**Extensions**
```
ob.flow.s2.e01  [O]  That is the widest gap in {sinceRef}.
ob.flow.s2.e02  [O]  The queues have grown three weeks running.
ob.flow.s2.e03  [O]  {k} of what you added is in {areaName}, which already held {n}.
ob.flow.s2.e04  [E]  Capture is useful right up until the point the queue stops being read.
ob.flow.s2.e05  [O]  Two areas are now holding more than they were a month ago.
ob.flow.s2.e06  [E]  Adding is easier than finishing, and it feels similar at the time.
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
```

**Extensions**
```
ob.flow.s3.e01  [O]  That is the strongest net week since {sinceRef}.
ob.flow.s3.e02  [O]  Output has beaten intake three weeks running.
ob.flow.s3.e03  [O]  {areaName} is now empty.
ob.flow.s3.e04  [E]  Weeks like this are either a clear out or the end of a backlog.
ob.flow.s3.e05  [O]  {m} of the completions had been waiting more than a fortnight.
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
```

**Extensions**
```
ob.split.e01  [O]  The numbers agree with that.
ob.split.e02  [O]  The numbers read a little differently.
ob.split.e03  [O]  Completions were up, which fits.
ob.split.e04  [E]  What a week feels like and what it counts as are not always the same.
ob.split.e05  [O]  Last week the split went the other way.
ob.split.e06  [E]  That is worth holding next to the numbers above.
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
```

**Extensions**
```
ob.srvd.e01  [E]  Both things can be true.
ob.srvd.e02  [O]  That was the only pulse you answered about it.
ob.srvd.e03  [E]  Worth revisiting the answer, or the item.
ob.srvd.e04  [O]  You have said the same thing about it twice now.
ob.srvd.e05  [E]  The first read may still be the right one.
ob.srvd.e06  [O]  It has been active {ageDays} in total.
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
```

**Extensions**
```
ob.qp.e01  [O]  {m} of those have been waiting more than a fortnight.
ob.qp.e02  [O]  The queues have grown three weeks running.
ob.qp.e03  [O]  The oldest is {itemTitle}, queued {ageDays} ago.
ob.qp.e04  [E]  Length is not the problem. Length that never shortens is.
ob.qp.e05  [O]  Nothing in {areaName}'s queue has moved since {sinceRef}.
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
```

**Extensions**
```
ob.rev.e01  [O]  {n} of its {m} queued items went.
ob.rev.e02  [E]  Whether it holds is next week's question.
ob.rev.e03  [O]  It has come back before, in {sinceRef}, and stayed for {n} weeks.
ob.rev.e04  [O]  Its queue is now empty.
ob.rev.e05  [E]  Returns like this are usually a decision rather than an accident.
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
```

**Extensions**
```
ob.pers.e01  [O]  You called it deep work when it was {n} days old.
ob.pers.e02  [O]  It is the longest anything has been active in {areaName}.
ob.pers.e03  [E]  Long is not wrong. Long and unexamined is a different thing.
ob.pers.e04  [O]  {n} things are waiting behind it.
ob.pers.e05  [O]  It has survived {n} focus sessions without finishing.
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
```

**Extensions**
```
ob.best.e01  [O]  {n} of them were in {areaName}.
ob.best.e02  [O]  It came alongside {sessions} focus sessions.
ob.best.e03  [E]  Peaks are worth noticing. They are not worth defending.
ob.best.e04  [O]  Your queues still grew, by {m}.
ob.best.e05  [E]  What made the difference is worth knowing, if you can name it.
```

## 2.12 mostActiveSince

**Leads**
```
ob.since.l01  [P]  Your busiest week since {sinceRef}.
ob.since.l02  [O]  {n} events, the most since {sinceRef}.
ob.since.l03  [O]  Nothing since {sinceRef} has looked like this.
ob.since.l04  [E]  The last week with this much in it was {sinceRef}.
ob.since.l05  [P]  {n} completions, the highest since {sinceRef}.
```

**Extensions**
```
ob.since.e01  [O]  That week was mostly {areaName} too.
ob.since.e02  [O]  It has been {n} weeks.
ob.since.e03  [E]  Whether that is a return or a spike shows up next week.
ob.since.e04  [O]  The weeks between averaged {m}.
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
```

**Extensions**
```
ob.tod.e01  [O]  That has been consistent for three weeks.
ob.tod.e02  [O]  Last week it was the other way around.
ob.tod.e03  [E]  Knowing when you finish things is more useful than knowing how many.
ob.tod.e04  [O]  Your longest focus sessions were the early ones.
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
```

**Extensions**
```
ob.stead.e01  [O]  The distribution across areas was similar too.
ob.stead.e02  [E]  Consistency is harder to notice than a spike and usually worth more.
ob.stead.e03  [O]  Your queues have stayed the same length throughout.
ob.stead.e04  [O]  {areaName} has led every one of those weeks.
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
```

**Extensions**
```
ob.bal.e01  [O]  That is more even than any week this month.
ob.bal.e02  [O]  Completions were less even than events: {n} of {m} were in {areaName}.
ob.bal.e03  [E]  Broad weeks and deep weeks measure different things.
ob.bal.e04  [O]  Last week one area held {otherPct}.
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
```

**Extensions**
```
ob.hard.e01  [N]  The queues are where you left them.
ob.hard.e02  [N]  Nothing has been lost. It is all still here.
ob.hard.e03  [N]  Whatever picks back up will start from exactly this point.
```

**Observation totals: 213 leads and 134 extensions across 21 families.** With combination, roughly **1,300 distinct observation surfaces.**

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
pt.gone.01  {areaName} went quiet {ageDays} ago. It is not on pause. It is stopped.
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
```

## 3.16 insufficientData

The single faint line shown when there are fewer than three weeks of snapshots.

```
pt.none.01  Patterns will appear after a few weeks of use.
pt.none.02  Three weeks of history is where patterns start.
pt.none.03  Not enough weeks yet to see a shape.
pt.none.04  This section fills in after a few more weeks.
```

**Pattern totals: 111 lines across 16 families.**

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
```

## 6.2 First week

```
ed.first.01  Your first week. There is not much to compare against yet.
ed.first.02  Week one. The report gets more useful as history builds.
ed.first.03  This is the first of these. It will say more in a month.
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
| Headlines | 158 | 158 |
| Observation leads | 213 | ~1,300 combined with extensions |
| Observation extensions | 134 | |
| Patterns | 111 | 111 |
| Guidance frames | 7 | ~4,500 plan surfaces |
| Guidance cues | 21 | |
| Guidance actions | 54 | |
| Commitment forms | 3 | |
| Non-plan closings | 24 | 24 |
| Basis and edge states | 12 | 12 |
| **Total** | **737** | **~6,100** |

Plan surfaces are frames times compatible cues times actions. Not every cue pairs with every action, so the figure assumes roughly twelve of the twenty one cues are valid for a given action.

A report contains one headline, two to four observations, at most one pattern and one closing. At 52 reports a year a user sees roughly 400 report sentences annually. With these benches and the 90 day exclusion, the same headline would not recur for several years and a full report would never repeat.

**Combined with volume 1: 1,357 authored lines, roughly 16,700 distinct surfaces.**

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
