# Clarity Phrasing Corpus, Volume 1: Pulse

The authored language library for Clarity Pulse. This is the data that `domain.engine` reads. `CLARITY_LOGIC_ENGINE.md` governs how it is selected, rendered and validated; this file governs what is actually said.

**All language panel amendments applied.** American spelling throughout. The Pulse response format is settled at two options, except `quietDay` which needs three; see `CLARITY_LOGIC_ENGINE.md` 6.2 for why a universal third option was rejected.

**Volume 1 of 3.** Volume 2 is the Report and guidance. Volume 3 is Momentum and the Areas banner. Pulse comes first because it is the daily surface and therefore the one where repetition is detected first.

---

## How this file is structured

Every family owns benches per escalation stage. Three benches, combined at realization:

- **Statements**, the observation. Each tagged with a register: `[P]` plain, `[O]` observational, `[R]` reflective.
- **Questions**, the single question that follows.
- **Response pairs**, the tappable answers. The first option in each pair is the positive one.

**Combination rule.** A statement combines only with a question and a response pair from its own family and its own stage. There is no global pool. Every question in a stage must read correctly after every statement in that stage, and this was checked line by line during authoring. Any new line added later must be checked the same way.

**Surface count.** A stage with 15 statements, 8 questions and 5 response pairs yields 600 distinct surfaces. The tables at the end of each family give real numbers.

**Slots.** `{itemTitle}` `{areaName}` `{otherArea}` `{ageDays}` `{n}` `{m}` `{pct}` `{areaCount}` `{dayCount}` `{sinceRef}` `{priorLabel}`

Numbers render per `CLARITY_LOGIC_ENGINE.md` 7.2. In Pulse, two through nine are words. `{ageDays}` renders as `yesterday`, `three days`, `nine days`, `three weeks`, `two months` at the appropriate magnitude, so statements must be written to read correctly with any of those.

**Variant keys** are `family.stage.index`, stable forever, never reused.

**Key prefixes are the family name in lowercase**, so `quietday` is the family `quietDay` and `freshstart` is `freshStart`. The engine's `FamilyKey` values are the camelCase forms listed in `CLARITY_LOGIC_ENGINE.md` 6.1. Within a stage, a bare number marks a statement, `q` marks a question and `r` marks a response pair. Deleting a line retires its key.

---

# 1. PERSISTENCE

An active item has stayed active. The most frequently firing family in the app and the one where escalation matters most.

Subject: the item. Escalation fact: `activeItemAgeDays`.

## Stage 1, three to five days

### Statements

```
persistence.s1.01  [P]  {itemTitle} has been active in {areaName} for {ageDays}.
persistence.s1.02  [P]  {ageDays} on {itemTitle}.
persistence.s1.03  [P]  {itemTitle} is still what {areaName} is on.
persistence.s1.04  [O]  {areaName} has been holding {itemTitle} since the start of the week.
persistence.s1.05  [O]  Nothing has moved past {itemTitle} yet.
persistence.s1.06  [O]  {itemTitle} has had {areaName} to itself for {ageDays}.
persistence.s1.07  [O]  The queue behind {itemTitle} has not moved in {ageDays}.
persistence.s1.08  [O]  {itemTitle} has stayed put while other things happened elsewhere.
persistence.s1.09  [R]  {ageDays}, and {itemTitle} is still the one.
persistence.s1.10  [R]  {itemTitle} has been sitting at the front of {areaName}.
persistence.s1.11  [R]  {areaName} has asked the same thing of you for {ageDays}.
persistence.s1.12  [R]  Same item, {ageDays} running, in {areaName}.
persistence.s1.13  [P]  {itemTitle} arrived at the front {ageDays} ago and has stayed.
persistence.s1.14  [O]  Whatever else moved this week, {itemTitle} did not.
persistence.s1.15  [R]  {itemTitle} has outlasted a few days now.
```

### Questions

```
persistence.s1.q01  Deep work, or stuck?
persistence.s1.q02  Still the right thing to be on?
persistence.s1.q03  Working through it, or working around it?
persistence.s1.q04  Is it big, or is it just sitting there?
persistence.s1.q05  Chosen, or defaulted to?
persistence.s1.q06  Taking time, or taking up space?
persistence.s1.q07  Where is it actually at?
persistence.s1.q08  Moving slowly, or not moving?
```

### Response pairs

```
persistence.s1.r01  Deep work / Stuck
persistence.s1.r02  Still going / Stuck on it
persistence.s1.r03  By choice / By default
persistence.s1.r04  It needs the time / It needs a nudge
persistence.s1.r05  In progress / Not started
persistence.s1.r06  Right thing / Wrong thing
```

## Stage 2, six to thirteen days

### Statements

```
persistence.s2.01  [P]  Still {itemTitle}. {ageDays} now.
persistence.s2.02  [P]  {itemTitle} has held {areaName} for {ageDays}.
persistence.s2.03  [P]  {ageDays} on {itemTitle}, and counting.
persistence.s2.04  [P]  {itemTitle} is going into its second week in {areaName}.
persistence.s2.05  [O]  Nothing has moved past {itemTitle} in {ageDays}.
persistence.s2.06  [O]  {areaName} has been waiting on {itemTitle} since last week.
persistence.s2.07  [O]  You have opened this app several times with {itemTitle} still there.
persistence.s2.08  [O]  The rest of {areaName} has been on hold for {ageDays}.
persistence.s2.09  [O]  {itemTitle} has outlasted everything else you started this week.
persistence.s2.10  [O]  Other areas have moved. {areaName} has not.
persistence.s2.11  [R]  {ageDays}, and {itemTitle} is still what {areaName} means.
persistence.s2.12  [R]  {itemTitle} has been the answer to that area for {ageDays}.
persistence.s2.13  [R]  Same item at the front of {areaName}, {ageDays} running.
persistence.s2.14  [R]  {itemTitle} has quietly become a fixture.
persistence.s2.15  [R]  {ageDays} is long enough that it is worth naming.
persistence.s2.16  [P]  {itemTitle} became active {ageDays} ago and has not moved since.
persistence.s2.17  [O]  Whatever else changed this week, {itemTitle} did not.
persistence.s2.18  [R]  {areaName} has had one answer for {ageDays}.
```

### Questions

```
persistence.s2.q01  Deep work, or stuck?
persistence.s2.q02  Still the right thing?
persistence.s2.q03  Working through it, or working around it?
persistence.s2.q04  Is it hard, or is it just sitting there?
persistence.s2.q05  Chosen, or defaulted to?
persistence.s2.q06  Does it still belong at the front?
persistence.s2.q07  Big job, or blocked job?
persistence.s2.q08  Slow going, or not going?
persistence.s2.q09  Is this the item, or is it the timing?
persistence.s2.q10  Would you pick it again today?
```

### Response pairs

```
persistence.s2.r01  Deep work / Stuck
persistence.s2.r02  Still going / Stuck on it
persistence.s2.r03  By choice / By default
persistence.s2.r04  It is big / It is blocked
persistence.s2.r05  Yes, still right / Time to swap
persistence.s2.r06  Making progress / Not really
persistence.s2.r07  Worth the time / Worth a rethink
```

## Stage 3, fourteen to twenty nine days

### Statements

```
persistence.s3.01  [P]  {itemTitle} has been active for {ageDays}.
persistence.s3.02  [P]  {ageDays} on {itemTitle}. Nothing behind it has moved.
persistence.s3.03  [P]  {itemTitle} has been at the front of {areaName} for {ageDays}.
persistence.s3.04  [O]  {itemTitle} has been active longer than most things you finish.
persistence.s3.05  [O]  {areaName} has been about one thing for {ageDays}.
persistence.s3.06  [O]  Everything queued behind {itemTitle} has waited {ageDays}.
persistence.s3.07  [O]  You have finished {n} other things since {itemTitle} became active.
persistence.s3.08  [O]  {itemTitle} has survived two weeks of everything else changing.
persistence.s3.09  [O]  Most things you complete take {m}. This one is at {ageDays}.
persistence.s3.10  [R]  {ageDays}. {itemTitle} is no longer a task, it is a state.
persistence.s3.11  [R]  {areaName} has meant one thing for {ageDays} now.
persistence.s3.12  [R]  {itemTitle} has stopped being new and has not become done.
persistence.s3.13  [R]  Three weeks is long enough for something to stop being a decision.
persistence.s3.14  [R]  {itemTitle} has held its place through everything.
persistence.s3.15  [P]  {itemTitle} has not moved in {ageDays}, and neither has the queue behind it.
persistence.s3.16  [O]  This is the longest anything has stayed active in {areaName}.
```

### Questions

```
persistence.s3.q01  Deep work, or stuck?
persistence.s3.q02  Is this still a task, or has it become something else?
persistence.s3.q03  Still the right thing to be on?
persistence.s3.q04  Big job, or blocked job?
persistence.s3.q05  Would you pick it again today?
persistence.s3.q06  Does it need time, or does it need breaking up?
persistence.s3.q07  Is it waiting on you, or on something else?
persistence.s3.q08  Worth continuing, or worth reconsidering?
```

### Response pairs

```
persistence.s3.r01  Deep work / Stuck
persistence.s3.r02  Still going / Stuck on it
persistence.s3.r03  It is big / It is blocked
persistence.s3.r04  Yes, still right / Time to swap
persistence.s3.r05  Needs more time / Needs breaking up
persistence.s3.r06  Waiting on me / Waiting on something else
persistence.s3.r07  Keep going / Rethink it
```

## Stage 4, thirty days and beyond

Rules reaching this stage carry a criterion asserting the record claim is genuinely held.

### Statements

```
persistence.s4.01  [P]  {itemTitle} has been active for {ageDays}.
persistence.s4.02  [O]  This has been active longer than anything you have ever kept active.
persistence.s4.03  [O]  {itemTitle} is now the longest running item in the app's history.
persistence.s4.04  [O]  A month of {areaName} has been {itemTitle}.
persistence.s4.05  [O]  You have completed {n} things elsewhere while {itemTitle} stayed put.
persistence.s4.06  [O]  {itemTitle} has been active across {m} different weeks.
persistence.s4.07  [R]  {ageDays}. At some point an item stops being a task and becomes furniture.
persistence.s4.08  [R]  {itemTitle} has been there long enough to stop being noticed.
persistence.s4.09  [R]  A month is long enough to ask a different question about this.
persistence.s4.10  [R]  {areaName} has had the same answer for a month.
persistence.s4.11  [P]  {itemTitle} became active {ageDays} ago. It is still active.
persistence.s4.12  [O]  Nothing else in the app has ever lasted this long at the front.
```

### Questions

```
persistence.s4.q01  Still the work, or is it something else now?
persistence.s4.q02  Is this a project rather than an item?
persistence.s4.q03  Worth continuing, or worth reconsidering?
persistence.s4.q04  Waiting on you, or waiting on someone else?
persistence.s4.q05  Does it need breaking up?
persistence.s4.q06  Would you add it today?
```

### Response pairs

```
persistence.s4.r01  Still the work / Something else now
persistence.s4.r02  It is a project / It is one thing
persistence.s4.r03  Keep going / Rethink it
persistence.s4.r04  Waiting on me / Waiting on someone else
persistence.s4.r05  Needs breaking up / Fine as it is
persistence.s4.r06  Yes, still / No, not today
```

**Persistence surface count:** stage 1, 15 x 8 x 6 = 720. Stage 2, 18 x 10 x 7 = 1,260. Stage 3, 16 x 8 x 7 = 896. Stage 4, 12 x 6 x 6 = 432. **Family total 3,308 surfaces from 141 authored lines.**

---

# 2. CONCENTRATION

One area holds 70 percent or more of the window's events, minimum four events.

Subject: the area. Escalation fact: `shareOfEvents` combined with `eventsInWindow`.

## Stage 1, seventy to eighty four percent

### Statements

```
concentration.s1.01  [P]  Most of yesterday happened in {areaName}.
concentration.s1.02  [P]  {areaName} took {pct} of what you did.
concentration.s1.03  [P]  {n} of your {m} moves were in {areaName}.
concentration.s1.04  [O]  {areaName} did most of the talking yesterday.
concentration.s1.05  [O]  Attention leaned toward {areaName}.
concentration.s1.06  [O]  Your other areas were quiet while {areaName} moved.
concentration.s1.07  [O]  {areaName} carried the day.
concentration.s1.08  [O]  Nearly everything landed in one place.
concentration.s1.09  [R]  Yesterday had a center of gravity, and it was {areaName}.
concentration.s1.10  [R]  {areaName} was where the day went.
concentration.s1.11  [R]  One area held most of yesterday.
concentration.s1.12  [R]  The day pointed mostly at {areaName}.
concentration.s1.13  [P]  {pct} of yesterday was {areaName}.
concentration.s1.14  [O]  {areaName} was busy. The rest was not.
```

### Questions

```
concentration.s1.q01  On purpose, or it just happened?
concentration.s1.q02  Was that the plan?
concentration.s1.q03  Chosen, or where the day went?
concentration.s1.q04  Focus, or gravity?
concentration.s1.q05  Deliberate, or the path of least resistance?
concentration.s1.q06  Did the other areas wait, or did you not need them?
concentration.s1.q07  Was that where it needed to be?
```

### Response pairs

```
concentration.s1.r01  On purpose / It just happened
concentration.s1.r02  That was the plan / That was the day
concentration.s1.r03  Chosen / Fell that way
concentration.s1.r04  Focus / Gravity
concentration.s1.r05  Where it needed to be / Not quite
concentration.s1.r06  Deliberate / Just how it went
```

## Stage 2, eighty five to ninety four percent

### Statements

```
concentration.s2.01  [P]  Almost everything yesterday was {areaName}.
concentration.s2.02  [P]  {pct} of yesterday happened in one area.
concentration.s2.03  [P]  {n} of {m} moves, all in {areaName}.
concentration.s2.04  [O]  {areaName} was close to the whole day.
concentration.s2.05  [O]  Your other areas barely registered.
concentration.s2.06  [O]  Everything else waited while {areaName} moved.
concentration.s2.07  [O]  Yesterday was {areaName} with a few exceptions.
concentration.s2.08  [O]  One area did almost all of it.
concentration.s2.09  [R]  Yesterday was not a balanced day, and it may not have needed to be.
concentration.s2.10  [R]  {areaName} took nearly all of it.
concentration.s2.11  [R]  The day belonged to one area.
concentration.s2.12  [R]  Everything pointed at {areaName} yesterday.
concentration.s2.13  [P]  Only {m} things happened outside {areaName}.
concentration.s2.14  [O]  {areaName} moved. Nothing much else did.
```

### Questions

```
concentration.s2.q01  On purpose, or it just happened?
concentration.s2.q02  Was that where it needed to be?
concentration.s2.q03  Focus, or gravity?
concentration.s2.q04  A push, or a pull?
concentration.s2.q05  Did the other areas wait, or get forgotten?
concentration.s2.q06  Chosen, or where the day went?
concentration.s2.q07  Would you do it that way again?
```

### Response pairs

```
concentration.s2.r01  On purpose / It just happened
concentration.s2.r02  Where it needed to be / Not quite
concentration.s2.r03  A push / A pull
concentration.s2.r04  They waited / They got forgotten
concentration.s2.r05  Yes, again / Differently next time
concentration.s2.r06  Focus / Gravity
```

## Stage 3, ninety five percent and above, or four or more consecutive days

### Statements

```
concentration.s3.01  [P]  Everything yesterday was {areaName}.
concentration.s3.02  [P]  {areaName} has held everything for {dayCount} running.
concentration.s3.03  [O]  Nothing outside {areaName} has moved in {dayCount}.
concentration.s3.04  [O]  Your other areas have been still for {dayCount}.
concentration.s3.05  [O]  {areaName} has been the whole app for several days.
concentration.s3.06  [O]  For {dayCount}, this has been a one area app.
concentration.s3.07  [R]  {dayCount} of one thing. That is either a season or a slide.
concentration.s3.08  [R]  {areaName} has been everything for a while now.
concentration.s3.09  [R]  Several days in a row have all been the same area.
concentration.s3.10  [P]  Nothing has happened outside {areaName} since {sinceRef}.
concentration.s3.11  [O]  {otherArea} has not moved while {areaName} took everything.
concentration.s3.12  [R]  This has stopped being a busy day and become a pattern.
```

### Questions

```
concentration.s3.q01  A season, or a slide?
concentration.s3.q02  On purpose, or it just happened?
concentration.s3.q03  Is this a push, or has something else stalled?
concentration.s3.q04  Deliberate for now, or overdue for a look?
concentration.s3.q05  Are the others on hold, or dropped?
concentration.s3.q06  Working as intended?
```

### Response pairs

```
concentration.s3.r01  A season / A slide
concentration.s3.r02  On purpose / It just happened
concentration.s3.r03  A push / Something stalled
concentration.s3.r04  Deliberate for now / Overdue for a look
concentration.s3.r05  On hold / Dropped
concentration.s3.r06  As intended / Not really
```

**Concentration surface count:** 14 x 7 x 6 plus 14 x 7 x 6 plus 12 x 6 x 6 = **1,608 surfaces from 111 authored lines.**

---

# 3. ACCUMULATION

Additions exceed completions by two or more.

Subject: none. Escalation fact: the gap.

## Stage 1, gap of two to three

### Statements

```
accumulation.s1.01  [P]  You added {n} things and finished {m}.
accumulation.s1.02  [P]  More went in than came out yesterday.
accumulation.s1.03  [P]  {n} added, {m} finished.
accumulation.s1.04  [O]  The queues grew a little yesterday.
accumulation.s1.05  [O]  Intake ran ahead of output.
accumulation.s1.06  [O]  Yesterday put more on the list than it took off.
accumulation.s1.07  [O]  Your queues are slightly longer than they were.
accumulation.s1.08  [O]  Capture outpaced completion.
accumulation.s1.09  [R]  Yesterday was better at noticing than finishing.
accumulation.s1.10  [R]  The list grew.
accumulation.s1.11  [R]  More arrived than left.
accumulation.s1.12  [P]  Your queues gained {n} yesterday.
accumulation.s1.13  [O]  Things got written down. Fewer got done.
```

### Questions

```
accumulation.s1.q01  Building up, or avoiding?
accumulation.s1.q02  Capturing, or collecting?
accumulation.s1.q03  Getting it out of your head, or putting it off?
accumulation.s1.q04  A planning day, or a slow one?
accumulation.s1.q05  Was there time to finish things?
accumulation.s1.q06  Intentional, or just how it went?
```

### Response pairs

```
accumulation.s1.r01  Building up / Avoiding
accumulation.s1.r02  Capturing / Collecting
accumulation.s1.r03  Out of my head / Putting it off
accumulation.s1.r04  Planning day / Slow day
accumulation.s1.r05  No time / Had time
accumulation.s1.r06  Intentional / Just happened
```

## Stage 2, gap of four to seven

### Statements

```
accumulation.s2.01  [P]  You added {n} things and finished {m}.
accumulation.s2.02  [P]  Your queues grew by {n} yesterday.
accumulation.s2.03  [O]  A lot more went in than came out.
accumulation.s2.04  [O]  Intake ran well ahead of output.
accumulation.s2.05  [O]  The queues are meaningfully longer than they were.
accumulation.s2.06  [O]  Yesterday was mostly about noticing things.
accumulation.s2.07  [O]  {areaName} took most of the new items.
accumulation.s2.08  [O]  Everything you added is still waiting.
accumulation.s2.09  [R]  Yesterday added weight without taking any off.
accumulation.s2.10  [R]  The list is heavier than it was.
accumulation.s2.11  [R]  A lot arrived. Not much left.
accumulation.s2.12  [P]  {n} in, {m} out.
accumulation.s2.13  [O]  Nothing you added yesterday has moved yet.
```

### Questions

```
accumulation.s2.q01  Building up, or avoiding?
accumulation.s2.q02  Capturing, or collecting?
accumulation.s2.q03  Was that a planning day?
accumulation.s2.q04  Clearing your head, or filling the queue?
accumulation.s2.q05  Do these all belong here?
accumulation.s2.q06  Necessary, or just easier than finishing?
accumulation.s2.q07  Intentional, or just how it went?
```

### Response pairs

```
accumulation.s2.r01  Building up / Avoiding
accumulation.s2.r02  Capturing / Collecting
accumulation.s2.r03  Planning day / Avoidance day
accumulation.s2.r04  Clearing my head / Filling the queue
accumulation.s2.r05  They all belong / Some do not
accumulation.s2.r06  Necessary / Easier than finishing
```

## Stage 3, gap of eight or more, or queues growing three weeks running

### Statements

```
accumulation.s3.01  [P]  You added {n} things and finished {m}.
accumulation.s3.02  [O]  Your queues have grown every week for three weeks.
accumulation.s3.03  [O]  The gap between what goes in and what comes out is widening.
accumulation.s3.04  [O]  Your queues hold {n} things now.
accumulation.s3.05  [O]  Nothing you added this week has been finished.
accumulation.s3.06  [R]  The list has been growing for a while now.
accumulation.s3.07  [R]  At some point a queue stops being a plan.
accumulation.s3.08  [R]  Things keep arriving faster than they leave.
accumulation.s3.09  [P]  {n} in, {m} out, three weeks running.
accumulation.s3.10  [O]  {areaName} is holding {n} things that have not moved.
accumulation.s3.11  [R]  The queues have been getting longer for three weeks.
```

### Questions

```
accumulation.s3.q01  Building up, or avoiding?
accumulation.s3.q02  Is the queue still a plan?
accumulation.s3.q03  Do these all still belong?
accumulation.s3.q04  Growing on purpose, or growing by default?
accumulation.s3.q05  Worth a clear out?
accumulation.s3.q06  Too much coming in, or not enough going out?
```

### Response pairs

```
accumulation.s3.r01  Building up / Avoiding
accumulation.s3.r02  Still a plan / A pile
accumulation.s3.r03  They belong / Time to cut some
accumulation.s3.r04  On purpose / By default
accumulation.s3.r05  Too much coming in / Not enough going out
accumulation.s3.r06  Fine for now / Worth a clear out
```

**Accumulation surface count:** 13 x 6 x 6 plus 13 x 7 x 6 plus 11 x 6 x 6 = **1,410 surfaces from 96 authored lines.**

---

# 4. THROUGHPUT

Completions exceed additions, minimum two completions.

Subject: none. Escalation fact: net flow.

## Stage 1, net of one to two

### Statements

```
throughput.s1.01  [P]  You finished {n} things and added {m}.
throughput.s1.02  [P]  More came out than went in yesterday.
throughput.s1.03  [P]  {n} finished, {m} added.
throughput.s1.04  [O]  The queues got a little shorter.
throughput.s1.05  [O]  Output ran ahead of intake.
throughput.s1.06  [O]  Yesterday took more off the list than it put on.
throughput.s1.07  [O]  Things moved out faster than they arrived.
throughput.s1.08  [R]  Yesterday was better at finishing than collecting.
throughput.s1.09  [R]  The list got shorter.
throughput.s1.10  [R]  More left than arrived.
throughput.s1.11  [P]  Your queues lost {n} yesterday.
throughput.s1.12  [O]  {areaName} moved a couple of things through.
```

### Questions

```
throughput.s1.q01  Clearing the deck, or running low?
throughput.s1.q02  Making room, or running out of things?
throughput.s1.q03  A good day, or a quiet one?
throughput.s1.q04  Was that momentum, or tidying?
throughput.s1.q05  Deliberate, or just how it fell?
```

### Response pairs

```
throughput.s1.r01  Clearing the deck / Running low
throughput.s1.r02  Making room / Running out
throughput.s1.r03  Momentum / Tidying
throughput.s1.r04  Good day / Quiet day
throughput.s1.r05  Deliberate / Just fell that way
```

## Stage 2, net of three to five

### Statements

```
throughput.s2.01  [P]  You finished {n} things and added {m}.
throughput.s2.02  [P]  Your queues shrank by {n} yesterday.
throughput.s2.03  [O]  Considerably more came out than went in.
throughput.s2.04  [O]  Output ran well ahead of intake.
throughput.s2.05  [O]  The queues are noticeably shorter than they were.
throughput.s2.06  [O]  Yesterday was mostly about finishing.
throughput.s2.07  [O]  {areaName} moved {n} things through.
throughput.s2.08  [O]  Several things left the queues and few arrived.
throughput.s2.09  [R]  Yesterday took weight off without adding any.
throughput.s2.10  [R]  The list is lighter than it was.
throughput.s2.11  [R]  A clearing kind of day.
throughput.s2.12  [P]  {n} out, {m} in.
throughput.s2.13  [O]  You closed more than you opened.
```

### Questions

```
throughput.s2.q01  Clearing the deck, or running low?
throughput.s2.q02  Momentum, or a backlog day?
throughput.s2.q03  Making room, or running out of things?
throughput.s2.q04  Was that the plan?
throughput.s2.q05  A good day, or a catch up day?
throughput.s2.q06  Real progress, or small items?
```

### Response pairs

```
throughput.s2.r01  Clearing the deck / Running low
throughput.s2.r02  Momentum / Backlog day
throughput.s2.r03  Making room / Running out
throughput.s2.r04  That was the plan / That was the day
throughput.s2.r05  Real progress / Small things
```

## Stage 3, net of six or more, or three weeks of rising completions

### Statements

```
throughput.s3.01  [P]  You finished {n} things and added {m}.
throughput.s3.02  [O]  Your completions have risen three weeks running.
throughput.s3.03  [O]  The queues are the shortest they have been in {sinceRef}.
throughput.s3.04  [O]  {n} things left the queues yesterday.
throughput.s3.05  [O]  Output has outpaced intake for a while now.
throughput.s3.06  [R]  Something has been moving for three weeks.
throughput.s3.07  [R]  The list has been getting shorter, steadily.
throughput.s3.08  [P]  {n} out, {m} in, and the pattern is holding.
throughput.s3.09  [O]  {areaName} has cleared most of what it was holding.
throughput.s3.10  [R]  A stretch like this is worth noticing.
```

### Questions

```
throughput.s3.q01  Clearing the deck, or running low?
throughput.s3.q02  Momentum, or the end of a backlog?
throughput.s3.q03  Is there more behind this, or is it done?
throughput.s3.q04  Sustainable, or a sprint?
throughput.s3.q05  Was this the plan all along?
```

### Response pairs

```
throughput.s3.r01  Clearing the deck / Running low
throughput.s3.r02  Momentum / End of a backlog
throughput.s3.r03  More behind it / That was it
throughput.s3.r04  Sustainable / A sprint
throughput.s3.r05  The plan / A surprise
```

**Throughput surface count:** 12 x 5 x 5 plus 13 x 6 x 5 plus 10 x 5 x 5 = **940 surfaces from 76 authored lines.**

---

# 5. QUIET DAY

Fewer than two events in the window.

Subject: none. Escalation fact: consecutive quiet days.

## Stage 1, one quiet day

### Statements

```
quietday.s1.01  [P]  Yesterday was quiet here.
quietday.s1.02  [P]  Nothing moved yesterday.
quietday.s1.03  [P]  One thing happened yesterday.
quietday.s1.04  [O]  The app did not see much of you yesterday.
quietday.s1.05  [O]  Your areas stayed where they were.
quietday.s1.06  [O]  Yesterday came and went without much here.
quietday.s1.07  [O]  Everything is where you left it.
quietday.s1.08  [R]  A still day.
quietday.s1.09  [R]  Yesterday did not leave a mark here.
quietday.s1.10  [R]  Nothing to report from yesterday, which is its own report.
quietday.s1.11  [P]  No completions yesterday.
quietday.s1.12  [O]  The queues did not move.
```

### Questions

```
quietday.s1.q01  What kind of quiet was it?
quietday.s1.q02  Where did the day go?
quietday.s1.q03  Rest, or overload?
quietday.s1.q04  Off, or elsewhere?
quietday.s1.q05  How was it, actually?
```

### Response pairs

Three options here rather than two. Recharging and Busy elsewhere are positive, the third is flagged.

```
quietday.s1.r01  Recharging / Busy elsewhere / Overwhelmed
quietday.s1.r02  Resting / Elsewhere / Stuck
quietday.s1.r03  Deliberate / Life happened / Too much
quietday.s1.r04  A day off / Off the app / Underwater
quietday.s1.r05  Needed it / Doing other things / Not coping
```

## Stage 2, two to three consecutive quiet days

### Statements

```
quietday.s2.01  [P]  Nothing has moved in {dayCount}.
quietday.s2.02  [P]  {dayCount} without a completion.
quietday.s2.03  [O]  Your areas have been still for a few days.
quietday.s2.04  [O]  Everything is exactly where you left it {dayCount} ago.
quietday.s2.05  [O]  The queues have not changed since {sinceRef}.
quietday.s2.06  [O]  A few quiet days in a row now.
quietday.s2.07  [R]  A still stretch.
quietday.s2.08  [R]  {dayCount} of nothing here. That can mean several things.
quietday.s2.09  [R]  The app has been waiting.
quietday.s2.10  [P]  {itemTitle} has been active and untouched for {dayCount}.
quietday.s2.11  [O]  Nothing has moved, in any area.
```

### Questions

```
quietday.s2.q01  What kind of quiet is this?
quietday.s2.q02  Rest, or overload?
quietday.s2.q03  Off, or elsewhere?
quietday.s2.q04  Is this a break, or a stall?
quietday.s2.q05  How has it actually been?
```

### Response pairs

```
quietday.s2.r01  Recharging / Busy elsewhere / Overwhelmed
quietday.s2.r02  A break / Elsewhere / A stall
quietday.s2.r03  Deliberate / Life happened / Too much
quietday.s2.r04  Resting / Occupied / Underwater
```

## Stage 3, four or more consecutive quiet days

### Statements

```
quietday.s3.01  [P]  Nothing has moved in {dayCount}.
quietday.s3.02  [O]  Your areas have been still since {sinceRef}.
quietday.s3.03  [O]  {dayCount} without anything changing here.
quietday.s3.04  [O]  Everything is where it was almost a week ago.
quietday.s3.05  [R]  A long still stretch.
quietday.s3.06  [R]  {dayCount} is long enough to be worth naming.
quietday.s3.07  [R]  The app has been waiting a while.
quietday.s3.08  [O]  {itemTitle} has been active and untouched for {dayCount}.
quietday.s3.09  [P]  No completions since {sinceRef}.
quietday.s3.10  [R]  Sometimes a quiet week is the right week.
```

### Questions

```
quietday.s3.q01  What kind of quiet is this?
quietday.s3.q02  A break, or a stall?
quietday.s3.q03  Rest, or overload?
quietday.s3.q04  Is the app still the right shape for you?
quietday.s3.q05  Off, or elsewhere?
```

### Response pairs

```
quietday.s3.r01  Recharging / Busy elsewhere / Overwhelmed
quietday.s3.r02  A break / Elsewhere / A stall
quietday.s3.r03  Needed it / Doing other things / Not coping
quietday.s3.r04  Deliberate / Life happened / Too much
```

**Quiet day surface count:** 12 x 5 x 5 plus 11 x 5 x 4 plus 10 x 5 x 4 = **1,220 surfaces from 79 authored lines.**

---

# 6. SPREAD

Three or more areas with events, none above 50 percent, minimum five events.

Subject: none. Escalation fact: area count.

## Stage 1, three areas

### Statements

```
spread.s1.01  [P]  Yesterday touched {areaCount} areas.
spread.s1.02  [P]  Attention went across {areaCount} areas yesterday.
spread.s1.03  [O]  No single area took over yesterday.
spread.s1.04  [O]  Yesterday was spread fairly evenly.
spread.s1.05  [O]  Three areas moved, none of them dominant.
spread.s1.06  [O]  You touched a bit of everything.
spread.s1.07  [R]  A wide day rather than a deep one.
spread.s1.08  [R]  Yesterday went in several directions.
spread.s1.09  [R]  Nothing owned yesterday.
spread.s1.10  [P]  {areaCount} areas, {n} moves, no clear center.
spread.s1.11  [O]  Everything got a little attention.
```

### Questions

```
spread.s1.q01  Felt manageable, or felt stretched?
spread.s1.q02  Balanced, or scattered?
spread.s1.q03  Was that a good shape for the day?
spread.s1.q04  Juggling, or covering ground?
spread.s1.q05  By design, or by demand?
```

### Response pairs

```
spread.s1.r01  Felt manageable / Felt stretched
spread.s1.r02  Balanced / Scattered
spread.s1.r03  Covering ground / Juggling
spread.s1.r04  By design / By demand
spread.s1.r05  Good shape / Too thin
```

## Stage 2, four or more areas

### Statements

```
spread.s2.01  [P]  Yesterday touched {areaCount} areas.
spread.s2.02  [P]  Every area moved yesterday.
spread.s2.03  [O]  You were in {areaCount} places yesterday, none of them for long.
spread.s2.04  [O]  Nothing got a concentrated run.
spread.s2.05  [O]  Attention went everywhere and settled nowhere.
spread.s2.06  [O]  {areaCount} areas moved, {n} things total.
spread.s2.07  [R]  A wide day. Very wide.
spread.s2.08  [R]  Yesterday went in every direction at once.
spread.s2.09  [R]  Everything got some attention. Nothing got much.
spread.s2.10  [P]  {n} moves across {areaCount} areas.
spread.s2.11  [O]  No area held you for long yesterday.
```

### Questions

```
spread.s2.q01  Felt manageable, or felt stretched?
spread.s2.q02  Balanced, or scattered?
spread.s2.q03  Covering ground, or spinning?
spread.s2.q04  Was anything actually finished?
spread.s2.q05  By design, or by demand?
spread.s2.q06  Would a narrower day have been better?
```

### Response pairs

```
spread.s2.r01  Felt manageable / Felt stretched
spread.s2.r02  Balanced / Scattered
spread.s2.r03  Covering ground / Spinning
spread.s2.r04  By design / By demand
spread.s2.r05  It worked / Too thin
spread.s2.r06  Wide was right / Narrower would be better
```

**Spread surface count:** 11 x 5 x 5 plus 11 x 6 x 6 = **671 surfaces from 55 authored lines.**

---

# 7. SWITCHING

One or more swaps in the window.

Subject: the area. Escalation fact: swap count.

## Stage 1, one swap

### Statements

```
switching.s1.01  [P]  You changed what is active in {areaName}.
switching.s1.02  [P]  {itemTitle} took the front of {areaName} yesterday.
switching.s1.03  [O]  Something moved aside in {areaName} to let something else through.
switching.s1.04  [O]  {areaName} has a different priority than it did yesterday.
switching.s1.05  [O]  The front of {areaName} changed hands.
switching.s1.06  [R]  {areaName} changed its mind.
switching.s1.07  [R]  What mattered most in {areaName} shifted.
switching.s1.08  [P]  {areaName} swapped its active item.
switching.s1.09  [O]  What was active in {areaName} went back to the queue.
```

### Questions

```
switching.s1.q01  Reprioritizing, or restless?
switching.s1.q02  New information, or a change of mood?
switching.s1.q03  Was the first one wrong, or is this one more urgent?
switching.s1.q04  A decision, or a drift?
switching.s1.q05  Which one was right?
```

### Response pairs

```
switching.s1.r01  Reprioritizing / Restless
switching.s1.r02  New information / Change of mood
switching.s1.r03  The first was wrong / This one is urgent
switching.s1.r04  A decision / A drift
switching.s1.r05  This one is right / Still not sure
```

## Stage 2, two or more swaps

### Statements

```
switching.s2.01  [P]  You changed what is active in {areaName} {n} times.
switching.s2.02  [O]  {areaName} has had {n} different priorities recently.
switching.s2.03  [O]  The front of {areaName} keeps changing.
switching.s2.04  [O]  Three items have taken turns being active in {areaName}.
switching.s2.05  [O]  Nothing in {areaName} has stayed at the front long.
switching.s2.06  [R]  {areaName} has been hard to settle.
switching.s2.07  [R]  {areaName} keeps changing its mind.
switching.s2.08  [P]  {n} swaps in {areaName}.
switching.s2.09  [O]  Items have been moving in and out of the front of {areaName}.
```

### Questions

```
switching.s2.q01  Reprioritizing, or restless?
switching.s2.q02  Is the right one in there at all?
switching.s2.q03  Changing conditions, or changing mood?
switching.s2.q04  Searching, or spinning?
switching.s2.q05  Is the queue right, or is the area?
```

### Response pairs

```
switching.s2.r01  Reprioritizing / Restless
switching.s2.r02  Right one is in there / Not sure it is
switching.s2.r03  Conditions changed / Mood changed
switching.s2.r04  Searching / Spinning
switching.s2.r05  Queue is fine / Queue needs work
```

**Switching surface count:** 9 x 5 x 5 plus 9 x 5 x 5 = **450 surfaces from 46 authored lines.**

---

# 8. BURST

Three or more completions in one area in one day.

Subject: the area. Escalation fact: completion count.

## Stage 1, three to four completions

### Statements

```
burst.s1.01  [P]  You finished {n} things in {areaName} yesterday.
burst.s1.02  [P]  {n} completions in {areaName}, all in one day.
burst.s1.03  [O]  {areaName} moved a lot yesterday.
burst.s1.04  [O]  The queue in {areaName} got noticeably shorter.
burst.s1.05  [O]  {areaName} had a run yesterday.
burst.s1.06  [R]  Something opened up in {areaName} yesterday.
burst.s1.07  [R]  {areaName} had one of those days.
burst.s1.08  [P]  {n} things left {areaName} yesterday.
burst.s1.09  [O]  Yesterday was a good day for {areaName}.
```

### Questions

```
burst.s1.q01  Momentum, or clearing out?
burst.s1.q02  Big things, or small ones?
burst.s1.q03  A good run, or a backlog?
burst.s1.q04  Did it feel like that at the time?
burst.s1.q05  Planned, or did it just flow?
```

### Response pairs

Both options positive. Milestone moments get responses that are both good.

```
burst.s1.r01  Momentum / Clearing out
burst.s1.r02  Big things / Small things
burst.s1.r03  A good run / A backlog day
burst.s1.r04  Felt like it / Did not notice
burst.s1.r05  Planned / It just flowed
```

## Stage 2, five or more completions

### Statements

```
burst.s2.01  [P]  You finished {n} things in {areaName} yesterday.
burst.s2.02  [O]  That is the most {areaName} has moved in one day.
burst.s2.03  [O]  {areaName} cleared {n} things in a single day.
burst.s2.04  [O]  Most of {areaName}'s queue went yesterday.
burst.s2.05  [R]  {areaName} had a real day yesterday.
burst.s2.06  [R]  Something broke open in {areaName}.
burst.s2.07  [P]  {n} completions, one area, one day.
burst.s2.08  [O]  {areaName} has not had a day like that before.
```

### Questions

```
burst.s2.q01  Momentum, or clearing out?
burst.s2.q02  A breakthrough, or a backlog?
burst.s2.q03  Big things, or small ones?
burst.s2.q04  Can you do that again, or was it a one off?
burst.s2.q05  Did it feel like that at the time?
```

### Response pairs

```
burst.s2.r01  Momentum / Clearing out
burst.s2.r02  A breakthrough / A backlog
burst.s2.r03  Big things / Small things
burst.s2.r04  Repeatable / A one off
burst.s2.r05  Felt like it / Did not notice
```

**Burst surface count:** 9 x 5 x 5 plus 8 x 5 x 5 = **425 surfaces from 44 authored lines.**

---

# 9. REBALANCE

Activity returned to an area dormant five or more days.

Subject: the area. Escalation fact: dormancy length.

## Stage 1, five to thirteen days dormant

### Statements

```
rebalance.s1.01  [P]  {areaName} moved again after {ageDays}.
rebalance.s1.02  [P]  Something happened in {areaName} for the first time in {ageDays}.
rebalance.s1.03  [O]  {areaName} came back yesterday.
rebalance.s1.04  [O]  After {ageDays} of stillness, {areaName} moved.
rebalance.s1.05  [O]  {areaName} had been quiet since {sinceRef}.
rebalance.s1.06  [R]  {areaName} woke up.
rebalance.s1.07  [R]  Something returned to {areaName} after a gap.
rebalance.s1.08  [P]  {ageDays} of quiet in {areaName}, then yesterday.
```

### Questions

```
rebalance.s1.q01  Planned, or it just happened?
rebalance.s1.q02  Back for good, or a one off?
rebalance.s1.q03  Did you notice the gap?
rebalance.s1.q04  A return, or a reminder?
rebalance.s1.q05  Was the pause deliberate?
```

### Response pairs

```
rebalance.s1.r01  Planned / It just happened
rebalance.s1.r02  Back for good / A one off
rebalance.s1.r03  Noticed the gap / Did not notice
rebalance.s1.r04  A return / A reminder
rebalance.s1.r05  The pause was deliberate / It got away from me
```

## Stage 2, fourteen or more days dormant

### Statements

```
rebalance.s2.01  [P]  {areaName} moved again after {ageDays}.
rebalance.s2.02  [O]  {areaName} had not moved since {sinceRef}.
rebalance.s2.03  [O]  Three weeks of stillness in {areaName}, then yesterday.
rebalance.s2.04  [O]  {areaName} came back after the longest gap it has had.
rebalance.s2.05  [R]  {areaName} has been away a while.
rebalance.s2.06  [R]  Something came back that had been gone a long time.
rebalance.s2.07  [P]  First activity in {areaName} in {ageDays}.
rebalance.s2.08  [O]  {areaName} was almost forgotten. Yesterday it was not.
```

### Questions

```
rebalance.s2.q01  Planned, or it just happened?
rebalance.s2.q02  Back for good, or passing through?
rebalance.s2.q03  Did you miss it, or had you let it go?
rebalance.s2.q04  Does it still belong here?
rebalance.s2.q05  A return, or a last try?
```

### Response pairs

```
rebalance.s2.r01  Planned / It just happened
rebalance.s2.r02  Back for good / Passing through
rebalance.s2.r03  Missed it / Had let it go
rebalance.s2.r04  It belongs / Not sure it does
rebalance.s2.r05  A return / A last try
```

**Rebalance surface count:** 8 x 5 x 5 plus 8 x 5 x 5 = **400 surfaces from 42 authored lines.**

---

# 10. QUEUE DRAIN

An area's queue went from three or more to zero.

Subject: the area. Escalation fact: starting queue size.

## Stage 1, queue of three to four drained

### Statements

```
queuedrain.s1.01  [P]  {areaName} is empty. It held {n} things.
queuedrain.s1.02  [P]  You cleared everything in {areaName}.
queuedrain.s1.03  [O]  {areaName}'s queue went from {n} to nothing.
queuedrain.s1.04  [O]  There is nothing waiting in {areaName}.
queuedrain.s1.05  [O]  {areaName} finished everything it was holding.
queuedrain.s1.06  [R]  {areaName} is clear.
queuedrain.s1.07  [R]  Nothing left in {areaName}.
queuedrain.s1.08  [P]  {n} things left {areaName}, and nothing replaced them.
```

### Questions

```
queuedrain.s1.q01  Finished strong, or running empty?
queuedrain.s1.q02  Is there more to add, or is that genuinely it?
queuedrain.s1.q03  A clean finish, or an empty area?
queuedrain.s1.q04  Does {areaName} need something new?
queuedrain.s1.q05  Done, or paused?
```

### Response pairs

```
queuedrain.s1.r01  Finished strong / Running empty
queuedrain.s1.r02  More to add / That is genuinely it
queuedrain.s1.r03  A clean finish / An empty area
queuedrain.s1.r04  Needs something new / Fine as it is
queuedrain.s1.r05  Done / Paused
```

## Stage 2, queue of five or more drained

### Statements

```
queuedrain.s2.01  [P]  {areaName} is empty. It held {n} things.
queuedrain.s2.02  [O]  {areaName} cleared its whole queue, {n} items.
queuedrain.s2.03  [O]  That is the biggest queue you have cleared out.
queuedrain.s2.04  [O]  {areaName} went from {n} waiting to nothing.
queuedrain.s2.05  [R]  {areaName} is completely clear for the first time.
queuedrain.s2.06  [R]  A whole area, emptied.
queuedrain.s2.07  [P]  {n} things left {areaName} and nothing took their place.
```

### Questions

```
queuedrain.s2.q01  Finished strong, or running empty?
queuedrain.s2.q02  Is there more, or is that the whole thing?
queuedrain.s2.q03  A finish, or a stopping point?
queuedrain.s2.q04  Does {areaName} still belong here?
queuedrain.s2.q05  What goes there next?
```

### Response pairs

```
queuedrain.s2.r01  Finished strong / Running empty
queuedrain.s2.r02  There is more / That is the whole thing
queuedrain.s2.r03  A finish / A stopping point
queuedrain.s2.r04  It still belongs / Time to archive it
queuedrain.s2.r05  Something new / Nothing for now
```

**Queue drain surface count:** 8 x 5 x 5 plus 7 x 5 x 5 = **375 surfaces from 40 authored lines.**

---

# 11. FRESH START

A new area was created, or a first item was added to an empty area.

Subject: the area. Single stage.

### Statements

```
freshstart.s1.01  [P]  {areaName} has its first item.
freshstart.s1.02  [P]  You added {areaName}.
freshstart.s1.03  [P]  {itemTitle} is the first thing in {areaName}.
freshstart.s1.04  [O]  There is somewhere new to put things now.
freshstart.s1.05  [O]  {areaName} started yesterday.
freshstart.s1.06  [O]  A new area, with one thing in it.
freshstart.s1.07  [R]  {areaName} exists now.
freshstart.s1.08  [R]  Something new got a place of its own.
freshstart.s1.09  [P]  {areaName} went from empty to active.
freshstart.s1.10  [O]  {areaName} has one thing and nothing behind it.
```

### Questions

```
freshstart.s1.q01  Expanding, or exploring?
freshstart.s1.q02  A commitment, or a trial?
freshstart.s1.q03  Been meaning to, or just occurred to you?
freshstart.s1.q04  Is this a big one?
freshstart.s1.q05  Where did this come from?
```

### Response pairs

Both options positive. A new beginning never gets a bad answer.

```
freshstart.s1.r01  Expanding / Exploring
freshstart.s1.r02  A commitment / A trial
freshstart.s1.r03  Been meaning to / Just occurred to me
freshstart.s1.r04  A big one / A small one
freshstart.s1.r05  Long overdue / Brand new
```

**Fresh start surface count:** 10 x 5 x 5 = **250 surfaces from 20 authored lines.**

---

# Corpus totals, volume 1

Counted from the keyed lines in this file, not estimated. Surfaces are statements times questions times response pairs, summed per stage.

| family | authored lines | distinct surfaces |
|---|---|---|
| Persistence | 119 | 3,308 |
| Concentration | 78 | 1,608 |
| Accumulation | 74 | 1,410 |
| Throughput | 66 | 940 |
| Quiet day | 61 | 720 |
| Spread | 44 | 671 |
| Switching | 38 | 450 |
| Burst | 37 | 425 |
| Rebalance | 36 | 400 |
| Queue drain | 35 | 375 |
| Fresh start | 20 | 250 |
| Acknowledgments | 12 | 12 |
| **Total** | **620** | **10,569** |

At roughly 365 pulses a year, with the 90 day variant exclusion and per family cooldowns applied on top, a user would need to run the app for many years before a statement repeats, and would never see the same statement, question and response combination twice.

These counts are the **current** state, not the target. Section 11.1 of `CLARITY_LOGIC_ENGINE.md` gives the sizing targets, and phase 9 of the build grows the hot families toward them.

# Acknowledgment lines

Shown briefly after an answer, before the sheet settles to ambient. Neutral, never approving, never assessing. One bench, shared across families, because it responds to the act of answering rather than to the content.

```
ack.01  Noted.
ack.02  Good to name that.
ack.03  The report will see this.
ack.04  Logged.
ack.05  That is worth knowing.
ack.06  Filed.
ack.07  Recorded.
ack.08  Understood.
ack.09  That helps.
ack.10  Kept.
ack.11  Noted for the week.
ack.12  Added to the picture.
```

Selection is by the same deterministic hash used for variants, over `dateKey` plus `ack` key.

---

# Authoring rules for anyone extending this file

1. **Read the whole family before adding to it.** Voice drift within a bench is more damaging than a smaller bench.
2. **Every new question must read correctly after every existing statement in its stage.** Write it out against all of them. If it only works after some, it belongs to a different stage or a different family.
3. **Every response pair passes the equal validity test.** Read both options aloud with no context. If one sounds like the answer a good person gives, rewrite both.
4. **Every statement passes the mirror test.** Read it as though a friend said it about your week. If it would make you defensive, it is wrong.
5. **Never reuse a variant key.** Retired lines keep their keys forever so firing history stays coherent.
6. **Statements must read correctly with every magnitude their slot can render.** `{ageDays}` can be `three days` or `three weeks` or `two months`. A statement that only reads well at one magnitude belongs to a narrower stage.
7. **No fragment may appear in two families.** A test enforces this. Shared phrasing is how the seams become visible.
8. **No em dashes, no en dashes, no emojis, no exclamation marks.**
**On the word `behind`.** The ban targets the **evaluative** sense only: falling behind, behind schedule, you are behind. The **spatial** sense is correct and common in this app, because a queue literally has things behind the active item. `The queue behind Rewrite the proposal intro has not moved` is fine. `You are behind on Work` is not.

The build test must therefore match `behind` only in these constructions, not the bare word:
`\b(?:fall(?:ing|s|en)?|get(?:ting)?|slip(?:ping)?|running|are|is|am|were|was)\s+behind\b` and `\bbehind\s+(?:schedule|target|plan|where|the\s+curve)\b`.

9. **Banned vocabulary:** should, failed, behind, streak, hurry, lazy, don't forget, you haven't, make sure, try to, remember to, keep it up, well done, great job.
10. **Batches of forty, one family and stage at a time, anchored with ten approved lines, judged against simulator output.**
