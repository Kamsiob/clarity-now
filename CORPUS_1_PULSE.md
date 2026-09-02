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
persistence.s1.16  [P]  Still {itemTitle}.
persistence.s1.17  [P]  {itemTitle}, unmoved.
persistence.s1.18  [P]  {ageDays} at the front.
persistence.s1.19  [P]  {itemTitle} holds. {ageDays} in.
persistence.s1.20  [P]  {itemTitle} sits where it sat.
persistence.s1.21  [P]  The same item, {ageDays} later.
persistence.s1.22  [P]  Same front, {ageDays} on.
persistence.s1.23  [P]  {areaName} has not moved off {itemTitle}.
persistence.s1.24  [P]  One item has held {areaName}.
persistence.s1.25  [P]  {itemTitle} was here yesterday too.
persistence.s1.26  [P]  {itemTitle}, day after day.
persistence.s1.27  [P]  {ageDays} of {areaName} on one item.
persistence.s1.28  [P]  {ageDays}, and counting.
persistence.s1.29  [P]  {itemTitle} is still the active item in {areaName}.
persistence.s1.30  [P]  {itemTitle} has held the front of {areaName} for {ageDays}.
persistence.s1.31  [P]  {itemTitle} went to the front of {areaName} {ageDays} ago.
persistence.s1.32  [P]  {areaName} has pointed at {itemTitle} for {ageDays}.
persistence.s1.33  [P]  {itemTitle} became active {ageDays} ago and is active still.
persistence.s1.34  [P]  {itemTitle} has been the first thing in {areaName} for {ageDays}.
persistence.s1.35  [O]  No change in {areaName}.
persistence.s1.36  [O]  Nothing new at the front.
persistence.s1.37  [O]  The queue has not turned.
persistence.s1.38  [O]  No swap in {areaName} yet.
persistence.s1.39  [O]  The order in {areaName} held.
persistence.s1.40  [O]  Nothing has passed {itemTitle}.
persistence.s1.41  [O]  The front of {areaName} looks the same.
persistence.s1.42  [O]  Nothing has replaced {itemTitle}.
persistence.s1.43  [O]  {ageDays} without a swap.
persistence.s1.44  [O]  The same name at the front.
persistence.s1.45  [O]  Nothing in {areaName} has changed since {itemTitle} became active.
persistence.s1.46  [O]  The queue in {areaName} sits where it sat {ageDays} ago.
persistence.s1.47  [O]  Items have arrived in other areas. {itemTitle} is still here.
persistence.s1.48  [O]  What was at the front {ageDays} ago is at the front now.
persistence.s1.49  [O]  Elsewhere things started and ended. {itemTitle} did neither.
persistence.s1.50  [O]  {areaName} looks the same as it did {ageDays} ago.
persistence.s1.51  [O]  Other items in {areaName} have waited {ageDays}.
persistence.s1.52  [O]  Days have passed and {itemTitle} has not.
persistence.s1.53  [O]  Nothing has come past {itemTitle} in {areaName}.
persistence.s1.54  [O]  {itemTitle} has kept its place for {ageDays}.
persistence.s1.55  [O]  Nothing behind {itemTitle} in {areaName} has moved yet.
persistence.s1.56  [O]  A few days have gone by with {itemTitle} at the front.
persistence.s1.57  [O]  {areaName} has looked like this since {itemTitle} arrived.
persistence.s1.58  [R]  {ageDays} on one thing.
persistence.s1.59  [R]  {ageDays}, one subject.
persistence.s1.60  [R]  {itemTitle} is the constant here.
persistence.s1.61  [R]  {ageDays} is not long, and it is long enough to notice.
persistence.s1.62  [R]  {itemTitle} has become the shape of {areaName}.
persistence.s1.63  [R]  {ageDays} of one thing is a start or a stall.
persistence.s1.64  [R]  Some items take {ageDays}. This is one of them.
persistence.s1.65  [R]  {ageDays} of one item is either a choice or a habit.
persistence.s1.66  [R]  An item can be slow and still be moving.
persistence.s1.67  [R]  {ageDays} is how long, not how far.
persistence.s1.68  [R]  {areaName} has been one question for {ageDays}.
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
persistence.s1.q09  Moving, or parked?
persistence.s1.q10  Going somewhere, or sitting?
persistence.s1.q11  Still first, or not any more?
persistence.s1.q12  The time it needs, or more than that?
persistence.s1.q13  Waiting on you, or on something else?
persistence.s1.q14  One task, or several?
persistence.s1.q15  Would you put it there again today?
persistence.s1.q16  What is it waiting for?
persistence.s1.q17  Part done, or not begun?
persistence.s1.q18  Close, or nowhere near?
persistence.s1.q19  Underway, or untouched?
persistence.s1.q20  Is this the item, or the week?
persistence.s1.q21  Held on purpose, or just held?
persistence.s1.q22  Still the one?
```

### Response pairs

```
persistence.s1.r01  Deep work / Stuck
persistence.s1.r02  Still going / Stuck on it
persistence.s1.r03  By choice / By default
persistence.s1.r04  It needs the time / It needs a nudge
persistence.s1.r05  In progress / Not started
persistence.s1.r06  Right thing / Wrong thing
persistence.s1.r07  Moving / Parked
persistence.s1.r08  Underway / Untouched
persistence.s1.r09  Worth the front / Time to swap
persistence.s1.r10  Taking time / Taking space
persistence.s1.r11  Going somewhere / Going nowhere
persistence.s1.r12  Close / Nowhere near
persistence.s1.r13  One task / Several
persistence.s1.r14  On purpose / Just there
persistence.s1.r15  Yes, still / Not any more
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
persistence.s2.19  [P]  {ageDays} and holding.
persistence.s2.20  [P]  Still {itemTitle} at the front.
persistence.s2.21  [P]  {ageDays} on the same item.
persistence.s2.22  [P]  {itemTitle} has been here a while.
persistence.s2.23  [P]  {itemTitle} again, {ageDays} on.
persistence.s2.24  [P]  {ageDays} of {itemTitle} in {areaName}.
persistence.s2.25  [P]  {itemTitle} is the standing item.
persistence.s2.26  [P]  {ageDays} without a finish.
persistence.s2.27  [P]  {areaName} has not changed hands.
persistence.s2.28  [P]  {itemTitle} has stayed put.
persistence.s2.29  [P]  {ageDays} and the same front.
persistence.s2.30  [P]  {itemTitle} keeps the front.
persistence.s2.31  [P]  {ageDays} and one item.
persistence.s2.32  [P]  {areaName} starts with {itemTitle}.
persistence.s2.33  [P]  {itemTitle} has not finished.
persistence.s2.34  [P]  {ageDays} have passed and {itemTitle} is where it started.
persistence.s2.35  [P]  {itemTitle} became the active item {ageDays} ago and still is.
persistence.s2.36  [P]  The front of {areaName} has not changed in {ageDays}.
persistence.s2.37  [P]  {areaName} has been about {itemTitle} for {ageDays} now.
persistence.s2.38  [P]  {ageDays} of {areaName} have belonged to one item.
persistence.s2.39  [P]  {itemTitle} arrived, and {ageDays} later it is still the front.
persistence.s2.40  [P]  {areaName} has spent {ageDays} on one item.
persistence.s2.41  [O]  The queue has waited {ageDays}.
persistence.s2.42  [O]  Nothing has taken its turn.
persistence.s2.43  [O]  The rest of {areaName} waits.
persistence.s2.44  [O]  Other items are still queued.
persistence.s2.45  [O]  No swap in {ageDays}.
persistence.s2.46  [O]  The order has held.
persistence.s2.47  [O]  {areaName} has one thing going.
persistence.s2.48  [O]  The active item is unchanged.
persistence.s2.49  [O]  {ageDays}, and {areaName} has not moved.
persistence.s2.50  [O]  Every other item in {areaName} has waited {ageDays}.
persistence.s2.51  [O]  Things have finished in other areas while {itemTitle} stayed.
persistence.s2.52  [O]  The queue behind {itemTitle} is the queue that was there {ageDays} ago.
persistence.s2.53  [O]  Nothing has arrived at the front of {areaName} in {ageDays}.
persistence.s2.54  [O]  Whole days have gone by with {itemTitle} unchanged.
persistence.s2.55  [O]  {areaName} has held its shape for {ageDays}.
persistence.s2.56  [O]  Work has happened elsewhere. {itemTitle} has not moved.
persistence.s2.57  [O]  {areaName} has been waiting on {itemTitle} for {ageDays}.
persistence.s2.58  [O]  The queue in {areaName} has not shortened in {ageDays}.
persistence.s2.59  [O]  {itemTitle} has outlasted the week it started in.
persistence.s2.60  [O]  {ageDays} in, and nothing behind {itemTitle} has moved.
persistence.s2.61  [R]  {itemTitle} has become expected.
persistence.s2.62  [R]  {ageDays} of the same answer.
persistence.s2.63  [R]  {itemTitle} has seen a few days.
persistence.s2.64  [R]  {itemTitle} has gone from new to normal.
persistence.s2.65  [R]  {ageDays} of one item is either depth or a stall.
persistence.s2.66  [R]  {areaName} has had one subject for {ageDays}, and it is {itemTitle}.
persistence.s2.67  [R]  {itemTitle} has been what {areaName} shows for {ageDays}.
persistence.s2.68  [R]  An item can be steady or it can be stuck.
persistence.s2.69  [R]  {ageDays} of one item says nothing on its own.
persistence.s2.70  [R]  {areaName} has been one thing for {ageDays}.
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
persistence.s2.q11  Moving, or parked?
persistence.s2.q12  What is it waiting for?
persistence.s2.q13  One task, or several?
persistence.s2.q14  Waiting on you, or on something else?
persistence.s2.q15  Still worth the front?
persistence.s2.q16  Is it close?
persistence.s2.q17  Underway, or untouched?
persistence.s2.q18  Steady, or stalled?
persistence.s2.q19  Is it the size, or the start?
persistence.s2.q20  Wanted, or just kept?
persistence.s2.q21  Moving inside, or not at all?
persistence.s2.q22  Would you swap it now?
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
persistence.s2.r08  Moving / Parked
persistence.s2.r09  Underway / Untouched
persistence.s2.r10  Worth the front / Worth a swap
persistence.s2.r11  Close / Not close
persistence.s2.r12  Waiting on me / Waiting on something else
persistence.s2.r13  One task / Several
persistence.s2.r14  Wanted / Just kept
persistence.s2.r15  Too big / Just slow
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
persistence.s3.17  [P]  {ageDays}, still {itemTitle}.
persistence.s3.18  [P]  {itemTitle}, {ageDays} on.
persistence.s3.19  [P]  Weeks now, on {itemTitle}.
persistence.s3.20  [P]  {itemTitle} is still the item.
persistence.s3.21  [P]  Still {itemTitle}, weeks in.
persistence.s3.22  [P]  {areaName} has not moved on.
persistence.s3.23  [P]  {ageDays} of the same front.
persistence.s3.24  [P]  {itemTitle} has held for weeks.
persistence.s3.25  [P]  The front is unchanged.
persistence.s3.26  [P]  {ageDays}, one item.
persistence.s3.27  [P]  {itemTitle} is where it was weeks ago.
persistence.s3.28  [P]  The same front for weeks.
persistence.s3.29  [P]  {itemTitle} has not moved in weeks.
persistence.s3.30  [P]  Weeks, and the same item.
persistence.s3.31  [P]  {itemTitle} is still first in {areaName}.
persistence.s3.32  [P]  {areaName} has stayed on one item.
persistence.s3.33  [P]  {ageDays} have passed with {itemTitle} at the front.
persistence.s3.34  [P]  {itemTitle} has led {areaName} for {ageDays}.
persistence.s3.35  [P]  Nothing has replaced {itemTitle} in {ageDays}.
persistence.s3.36  [P]  The front of {areaName} has not turned in {ageDays}.
persistence.s3.37  [P]  {itemTitle} has been the active item through {ageDays}.
persistence.s3.38  [P]  {ageDays} of {areaName} has been one name.
persistence.s3.39  [O]  The queue has waited weeks.
persistence.s3.40  [O]  Other items waited {ageDays}.
persistence.s3.41  [O]  No swap in weeks.
persistence.s3.42  [O]  The order has not changed.
persistence.s3.43  [O]  The queue behind {itemTitle} has waited {ageDays} for its turn.
persistence.s3.44  [O]  Other areas have started and finished things in that time.
persistence.s3.45  [O]  {ageDays} of other items waiting in {areaName}.
persistence.s3.46  [O]  Nothing in {areaName} has reached the front since {itemTitle} did.
persistence.s3.47  [O]  {itemTitle} has been active across more than one week.
persistence.s3.48  [O]  The app has shown you {itemTitle} for {ageDays}.
persistence.s3.49  [O]  Everything queued in {areaName} is {ageDays} older.
persistence.s3.50  [O]  Everything else in {areaName} has waited {ageDays} for its turn.
persistence.s3.51  [O]  {itemTitle} has been active while other items came and went.
persistence.s3.52  [O]  {areaName} has looked the same for {ageDays} running.
persistence.s3.53  [R]  An item can be large, or it can be waiting on something else.
persistence.s3.54  [R]  {ageDays} of one item is either a project or a pause.
persistence.s3.55  [R]  {itemTitle} has stopped being a new thing to do.
persistence.s3.56  [R]  {ageDays} makes this a different question.
persistence.s3.57  [R]  An item at the front for {ageDays} is a different kind of item.
persistence.s3.58  [R]  {itemTitle} has been here long enough to stop looking new.
persistence.s3.59  [R]  An item can hold a front for weeks and still be moving.
persistence.s3.60  [R]  {areaName} has had the same subject for weeks.
persistence.s3.61  [R]  {itemTitle} has become what {areaName} is for.
persistence.s3.62  [R]  {itemTitle} is no longer the newest thing in {areaName}.
persistence.s3.63  [R]  Weeks at the front is a different fact from days at the front.
persistence.s3.64  [P]  {itemTitle} is still active.
persistence.s3.65  [O]  Nothing new in {areaName}.
persistence.s3.66  [P]  {ageDays} and no change.
persistence.s3.67  [P]  Weeks with one item.
persistence.s3.68  [O]  {areaName} has not turned over.
persistence.s3.69  [O]  {itemTitle} has outlasted weeks.
persistence.s3.70  [O]  The queue is where it was.
persistence.s3.71  [O]  {ageDays} in {areaName}, no swap.
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
persistence.s3.q09  Still one item, or several?
persistence.s3.q10  What has moved inside it?
persistence.s3.q11  Is it close, or is it early?
persistence.s3.q12  Does it still want the front?
persistence.s3.q13  Paused, or in progress?
persistence.s3.q14  Would you start it again today?
persistence.s3.q15  The right size for one item?
persistence.s3.q16  Held on purpose?
persistence.s3.q17  Waiting on a person, or on a day?
persistence.s3.q18  Bigger than it looked?
persistence.s3.q19  Still yours to do?
persistence.s3.q20  What would move it?
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
persistence.s3.r08  One item / Several
persistence.s3.r09  Paused / In progress
persistence.s3.r10  Close / Early
persistence.s3.r11  On purpose / Not really
persistence.s3.r12  Waiting on a person / Waiting on a day
persistence.s3.r13  Bigger than it looked / About right
persistence.s3.r14  Still mine / Someone else's
persistence.s3.r15  Still wants the front / Ready to move down
```

## Stage 4, thirty days and beyond

Rules reaching this stage carry a criterion asserting the record claim is genuinely held.

### Statements

```
persistence.s4.01  [P]  {itemTitle} has been active for {ageDays}.
persistence.s4.02  [O]  Nothing in {areaName} has moved past {itemTitle} in {ageDays}.
persistence.s4.03  [O]  {areaName} has meant one thing for {ageDays}.
persistence.s4.04  [O]  A month of {areaName} has been {itemTitle}.
persistence.s4.05  [O]  Other items have come and gone from {areaName} across {ageDays}.
persistence.s4.06  [O]  {itemTitle} has been active across {m} different weeks.
persistence.s4.07  [R]  {ageDays}. At some point an item stops being a task and becomes furniture.
persistence.s4.08  [R]  {itemTitle} has been there long enough to stop being noticed.
persistence.s4.09  [R]  A month is long enough to ask a different question about this.
persistence.s4.10  [R]  {areaName} has had the same answer for a month.
persistence.s4.11  [P]  {itemTitle} became active {ageDays} ago. It is still active.
persistence.s4.12  [P]  {ageDays} at the front of {areaName}.
persistence.s4.13  [P]  {itemTitle}, {ageDays} in.
persistence.s4.14  [P]  Months now.
persistence.s4.15  [P]  Still {itemTitle}, months on.
persistence.s4.16  [P]  {areaName} has not changed.
persistence.s4.17  [P]  {itemTitle} has held for months.
persistence.s4.18  [P]  {ageDays} and no finish.
persistence.s4.19  [P]  The same item, months later.
persistence.s4.20  [P]  {areaName} still means {itemTitle}.
persistence.s4.21  [P]  One item, {ageDays}.
persistence.s4.22  [P]  {itemTitle} has been the active item in {areaName} for {ageDays}.
persistence.s4.23  [P]  {ageDays} of {areaName} have had the same first item.
persistence.s4.24  [P]  {areaName} has carried {itemTitle} for {ageDays}.
persistence.s4.25  [O]  The queue has waited months.
persistence.s4.26  [O]  Other items are still waiting.
persistence.s4.27  [O]  Months of other items waiting.
persistence.s4.28  [O]  The order has not moved.
persistence.s4.29  [O]  The queue has not turned in months.
persistence.s4.30  [O]  Nothing in {areaName} has passed it in {ageDays}.
persistence.s4.31  [O]  Whole months have gone by with {itemTitle} at the front.
persistence.s4.32  [O]  Months of {areaName} have belonged to one item.
persistence.s4.33  [O]  Items have arrived and left {areaName} while {itemTitle} stayed.
persistence.s4.34  [O]  The queue behind {itemTitle} has been queued for {ageDays}.
persistence.s4.35  [O]  {areaName} has spent {ageDays} pointing at one thing.
persistence.s4.36  [O]  The same queue has been behind it for {ageDays}.
persistence.s4.37  [O]  The active item has not changed in {ageDays}.
persistence.s4.38  [R]  {ageDays} is a long stay.
persistence.s4.39  [R]  Months change what an item is.
persistence.s4.40  [R]  {areaName} has had one subject for months.
persistence.s4.41  [R]  {itemTitle} has become part of what {areaName} is.
persistence.s4.42  [R]  {ageDays} makes this a fact about {areaName}.
persistence.s4.43  [R]  An item can be months old and still be the right item.
persistence.s4.44  [R]  {itemTitle} has been the answer in {areaName} for months.
persistence.s4.45  [R]  {ageDays} of one item is either a long job or a long wait.
persistence.s4.46  [P]  {itemTitle} is still what {areaName} shows.
persistence.s4.47  [P]  {ageDays}, and the same name.
persistence.s4.48  [R]  {areaName} reads the same today as it did months ago.
persistence.s4.49  [P]  {itemTitle} is where it started.
persistence.s4.50  [O]  Nothing has taken over.
persistence.s4.51  [P]  {areaName} has one name on it.
persistence.s4.52  [P]  {ageDays} without a change.
persistence.s4.53  [P]  Still the same front.
persistence.s4.54  [O]  The front has not turned.
persistence.s4.55  [O]  {itemTitle} outlasted the month it began in.
persistence.s4.56  [P]  Months, and one item.
persistence.s4.57  [P]  {itemTitle} has not left the front.
persistence.s4.58  [P]  {itemTitle} has stayed for {ageDays}.
persistence.s4.59  [O]  {itemTitle} has been in front of everything in {areaName} for {ageDays}.
persistence.s4.60  [P]  The same item has held {areaName} through {ageDays}.
persistence.s4.61  [O]  Nothing behind {itemTitle} has reached the front in {ageDays}.
persistence.s4.62  [O]  Everything else in {areaName} is {ageDays} into its wait.
persistence.s4.63  [P]  {itemTitle} has been active every day for {ageDays}.
persistence.s4.64  [R]  An item can be old and still be the work.
persistence.s4.65  [R]  A month or many, it is still the front.
persistence.s4.66  [R]  Months at the front is its own kind of answer.
persistence.s4.67  [P]  Months, one item, one area.
```

### Questions

```
persistence.s4.q01  Still the work, or is it something else now?
persistence.s4.q02  Is this a project rather than an item?
persistence.s4.q03  Worth continuing, or worth reconsidering?
persistence.s4.q04  Waiting on you, or waiting on someone else?
persistence.s4.q05  Does it need breaking up?
persistence.s4.q06  Would you add it today?
persistence.s4.q07  Is it one thing, or many?
persistence.s4.q08  What is the smallest part of it?
persistence.s4.q09  Still the right place for it?
persistence.s4.q10  Has it changed since you wrote it?
persistence.s4.q11  Live, or just listed?
persistence.s4.q12  Is it waiting on a decision?
persistence.s4.q13  Does it still describe the work?
persistence.s4.q14  Blocked, or just big?
persistence.s4.q15  Is the title still right?
persistence.s4.q16  Has anything about it moved?
persistence.s4.q17  Yours, or someone else's now?
persistence.s4.q18  One item, or a heading?
```

### Response pairs

```
persistence.s4.r01  Still the work / Something else now
persistence.s4.r02  It is a project / It is one thing
persistence.s4.r03  Keep going / Rethink it
persistence.s4.r04  Waiting on me / Waiting on someone else
persistence.s4.r05  Needs breaking up / Fine as it is
persistence.s4.r06  Yes, still / No, not today
persistence.s4.r07  One thing / Many
persistence.s4.r08  Right place / Wrong place
persistence.s4.r09  Changed / The same
persistence.s4.r10  Live / Just listed
persistence.s4.r11  Waiting on a decision / Waiting on time
persistence.s4.r12  Blocked / Just big
persistence.s4.r13  Title still right / Needs renaming
persistence.s4.r14  Something moved / Nothing moved
```

**Persistence surface count:** stage 1, 68 x 22 x 15 = 22,440. Stage 2, 70 x 22 x 15 = 23,100. Stage 3, 71 x 20 x 15 = 21,300. Stage 4, 67 x 18 x 14 = 16,884. **Family total 83,724 surfaces from 417 authored lines.**

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
concentration.s1.15  [P]  {areaName} had {n} of yesterday's {m} moves.
concentration.s1.16  [P]  Yesterday held {m} moves, {n} of them in {areaName}.
concentration.s1.17  [P]  You spent yesterday mostly in {areaName}.
concentration.s1.18  [P]  {areaName} carried {n} of the {m} things that moved.
concentration.s1.19  [P]  Most of yesterday's moves were in one area.
concentration.s1.20  [P]  {areaName} was the busiest area yesterday.
concentration.s1.21  [P]  {areaName} had the bulk of yesterday.
concentration.s1.22  [P]  Your day happened mostly in one area.
concentration.s1.23  [P]  {areaName} held the larger part of yesterday.
concentration.s1.24  [P]  Yesterday ran mostly through {areaName}.
concentration.s1.25  [P]  No other area came close to {areaName} yesterday.
concentration.s1.26  [P]  {areaName} had more moves yesterday than everything else together.
concentration.s1.27  [P]  {pct} of yesterday in one area, the rest elsewhere.
concentration.s1.28  [P]  Yesterday's {m} moves were not evenly spread.
concentration.s1.29  [P]  {areaName} was the only area with much happening.
concentration.s1.30  [P]  Most of what happened yesterday was one area.
concentration.s1.31  [P]  The day's {m} moves were mostly {areaName}.
concentration.s1.32  [O]  {otherArea} waited while {areaName} moved.
concentration.s1.33  [O]  The day settled into {areaName}.
concentration.s1.34  [O]  Very little of yesterday was elsewhere.
concentration.s1.35  [O]  {areaName} kept the day's attention.
concentration.s1.36  [O]  The other areas had a light day.
concentration.s1.37  [O]  Little happened outside {areaName}.
concentration.s1.38  [O]  Everything else moved a little. {areaName} moved a lot.
concentration.s1.39  [O]  {areaName} took up most of the room yesterday.
concentration.s1.40  [O]  Attention pooled in {areaName}.
concentration.s1.41  [O]  One name covers most of yesterday.
concentration.s1.42  [O]  Most of what moved yesterday moved in {areaName}.
concentration.s1.43  [O]  Yesterday was not spread around much.
concentration.s1.44  [O]  {areaName} kept most of yesterday to itself.
concentration.s1.45  [O]  {areaName} had company yesterday, but not much of it.
concentration.s1.46  [O]  Most of the day had one name on it.
concentration.s1.47  [O]  {otherArea} had a quieter day.
concentration.s1.48  [R]  Yesterday knew where it was going.
concentration.s1.49  [R]  Yesterday had one subject.
concentration.s1.50  [R]  {areaName} ran through the whole day.
concentration.s1.51  [R]  Some days have one thing in them.
concentration.s1.52  [R]  One area, and the day around it.
concentration.s1.53  [R]  Some days do not spread out.
concentration.s1.54  [R]  {areaName} was what yesterday was about.
concentration.s1.55  [R]  One area, most of a day.
concentration.s1.56  [R]  Yesterday did not divide itself evenly.
concentration.s1.57  [R]  The day had a direction.
concentration.s1.58  [R]  Yesterday took the shape of {areaName}.
concentration.s1.59  [R]  The day kept one thing in view.
concentration.s1.60  [R]  A day spent mostly in one area is still a whole day.
concentration.s1.61  [R]  The day rested on one area.
concentration.s1.62  [R]  Most of yesterday belonged to {areaName}.
concentration.s1.63  [R]  The day did not go far from {areaName}.
concentration.s1.64  [R]  A lot of yesterday was one thing.
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
concentration.s1.q08  Depth, or default?
concentration.s1.q09  Was one area enough yesterday?
concentration.s1.q10  Did the day choose, or did you?
concentration.s1.q11  Concentrated, or narrow?
concentration.s1.q12  Was anything else waiting?
concentration.s1.q13  One thing at a time, or one thing only?
concentration.s1.q14  Did that feel like focus?
concentration.s1.q15  Was that the shape you wanted?
concentration.s1.q16  Pulled there, or headed there?
concentration.s1.q17  Would tomorrow look the same?
concentration.s1.q18  A day for one thing, or a day that became one?
concentration.s1.q19  Was there room for anything else?
concentration.s1.q20  Was that where things were happening?
```

### Response pairs

```
concentration.s1.r01  On purpose / It just happened
concentration.s1.r02  That was the plan / That was the day
concentration.s1.r03  Chosen / Fell that way
concentration.s1.r04  Focus / Gravity
concentration.s1.r05  Where it needed to be / Not quite
concentration.s1.r06  Deliberate / Just how it went
concentration.s1.r07  Depth / Default
concentration.s1.r08  It was enough / Not quite enough
concentration.s1.r09  I chose it / The day chose
concentration.s1.r10  Concentrated / Narrow
concentration.s1.r11  Nothing else was waiting / Other things were waiting
concentration.s1.r12  Felt like focus / Felt like drift
concentration.s1.r13  Headed there / Pulled there
concentration.s1.r14  Same again tomorrow / Different tomorrow
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
concentration.s2.15  [P]  {areaName} held {pct} of yesterday.
concentration.s2.16  [P]  Yesterday was {areaName} and very little else.
concentration.s2.17  [P]  The day added up to {pct} in {areaName}.
concentration.s2.18  [P]  {areaName} took {n} of the {m} things that happened.
concentration.s2.19  [P]  Nearly all of yesterday happened in {areaName}.
concentration.s2.20  [P]  Almost the entire day, one area.
concentration.s2.21  [P]  Outside {areaName}, yesterday was quiet.
concentration.s2.22  [P]  {areaName} was very nearly the whole of yesterday.
concentration.s2.23  [P]  {areaName} left little for anywhere else.
concentration.s2.24  [P]  {areaName} had all but a few of yesterday's moves.
concentration.s2.25  [P]  The day was {areaName} with a small remainder.
concentration.s2.26  [P]  {areaName} was where almost all of it happened.
concentration.s2.27  [P]  Yesterday went almost entirely to {areaName}.
concentration.s2.28  [P]  Yesterday was one area, near enough.
concentration.s2.29  [P]  {areaName} covered {pct} of the day.
concentration.s2.30  [P]  {areaName} had {n} moves yesterday. The day had {m}.
concentration.s2.31  [P]  {pct} in {areaName}, and the remainder somewhere else.
concentration.s2.32  [O]  {otherArea} was barely there yesterday.
concentration.s2.33  [O]  Everything but a fraction of yesterday was {areaName}.
concentration.s2.34  [O]  {areaName} was doing nearly all the moving.
concentration.s2.35  [O]  Yesterday kept coming back to {areaName}.
concentration.s2.36  [O]  {areaName} was the day, more or less.
concentration.s2.37  [O]  The day did not stray far.
concentration.s2.38  [O]  {areaName} had yesterday almost to itself.
concentration.s2.39  [O]  The other areas were nearly still.
concentration.s2.40  [O]  {areaName} was where nearly every move landed.
concentration.s2.41  [O]  The day's exceptions were few.
concentration.s2.42  [O]  {areaName} did nearly everything, and {otherArea} did the rest.
concentration.s2.43  [O]  Yesterday came very close to being one area.
concentration.s2.44  [O]  The rest of your areas hardly moved.
concentration.s2.45  [O]  {areaName} was the day and the others were the margin.
concentration.s2.46  [O]  {otherArea} put in an appearance. {areaName} did the rest.
concentration.s2.47  [O]  Yesterday hardly went anywhere but {areaName}.
concentration.s2.48  [O]  One area, and the thinnest edge of another.
concentration.s2.49  [O]  {otherArea} came and went. {areaName} stayed.
concentration.s2.50  [R]  Yesterday had one thing on its mind.
concentration.s2.51  [R]  This is what a narrow day looks like.
concentration.s2.52  [R]  Yesterday did not go looking for anything else.
concentration.s2.53  [R]  Close to a single thing.
concentration.s2.54  [R]  The day gave itself to {areaName}.
concentration.s2.55  [R]  That is a lot of one area.
concentration.s2.56  [R]  {areaName} was nearly the whole story.
concentration.s2.57  [R]  One area is enough for some days.
concentration.s2.58  [R]  Yesterday spent itself in one place.
concentration.s2.59  [R]  A day can be almost one thing and still be a full day.
concentration.s2.60  [R]  Barely more than one area.
concentration.s2.61  [R]  Some days only have one subject.
concentration.s2.62  [R]  A day that almost has one name.
concentration.s2.63  [R]  Yesterday did not spread itself out.
concentration.s2.64  [R]  Nearly all of one day, in one place.
concentration.s2.65  [R]  Yesterday came down to one thing.
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
concentration.s2.q08  Was there anywhere else to be?
concentration.s2.q09  Deep in it, or lost in it?
concentration.s2.q10  A day worth repeating?
concentration.s2.q11  Was that one thing worth the whole day?
concentration.s2.q12  Necessary, or convenient?
concentration.s2.q13  One area by choice, or by circumstance?
concentration.s2.q14  Did anything else get a chance?
concentration.s2.q15  Would you call that focus?
concentration.s2.q16  A day like that again, or a wider one?
concentration.s2.q17  Is that where the important things are?
concentration.s2.q18  Did you notice at the time?
concentration.s2.q19  Committed, or cornered?
concentration.s2.q20  Right area, right day?
```

### Response pairs

```
concentration.s2.r01  On purpose / It just happened
concentration.s2.r02  Where it needed to be / Not quite
concentration.s2.r03  A push / A pull
concentration.s2.r04  They waited / They got forgotten
concentration.s2.r05  Yes, again / Differently next time
concentration.s2.r06  Focus / Gravity
concentration.s2.r07  Nowhere else to be / Somewhere else to be
concentration.s2.r08  Deep in it / Lost in it
concentration.s2.r09  Worth repeating / Once was enough
concentration.s2.r10  Necessary / Convenient
concentration.s2.r11  By choice / By circumstance
concentration.s2.r12  I would call it focus / I would not
concentration.s2.r13  Noticed at the time / Only noticing now
concentration.s2.r14  Committed / Cornered
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
concentration.s3.13  [P]  {pct} of yesterday was {areaName}.
concentration.s3.14  [P]  Your whole day was in {areaName}.
concentration.s3.15  [P]  Nothing much happened outside {areaName}.
concentration.s3.16  [P]  Yesterday did not leave {areaName}.
concentration.s3.17  [P]  All of yesterday was in one area.
concentration.s3.18  [P]  Yesterday was {areaName}, start to finish.
concentration.s3.19  [P]  Every move yesterday was in {areaName}.
concentration.s3.20  [P]  The day was {areaName} and only {areaName}.
concentration.s3.21  [P]  From the first move to the last, {areaName}.
concentration.s3.22  [P]  You were in {areaName} and nowhere else.
concentration.s3.23  [P]  {pct} in {areaName}, and nothing anywhere else.
concentration.s3.24  [P]  Yesterday's record has one area in it.
concentration.s3.25  [P]  {areaName} took the whole of yesterday.
concentration.s3.26  [P]  No other area moved yesterday.
concentration.s3.27  [P]  Yesterday's moves all had one area on them.
concentration.s3.28  [P]  {areaName} and nothing else, all day.
concentration.s3.29  [P]  Your other areas did nothing yesterday.
concentration.s3.30  [P]  One area covered yesterday.
concentration.s3.31  [P]  {areaName} was where yesterday happened, all of it.
concentration.s3.32  [P]  There is one name on yesterday.
concentration.s3.33  [O]  Nothing else got a look yesterday.
concentration.s3.34  [O]  The other areas sat still yesterday.
concentration.s3.35  [O]  The rest of the app was quiet.
concentration.s3.36  [O]  There was nowhere else yesterday.
concentration.s3.37  [O]  Nothing else came up.
concentration.s3.38  [O]  {areaName} absorbed the day.
concentration.s3.39  [O]  The day never left one area.
concentration.s3.40  [O]  Nothing outside {areaName} moved yesterday.
concentration.s3.41  [O]  One area, and nowhere else.
concentration.s3.42  [O]  Yesterday stayed inside {areaName}.
concentration.s3.43  [O]  Yesterday belonged entirely to {areaName}.
concentration.s3.44  [O]  Nothing else asked for anything yesterday.
concentration.s3.45  [O]  Everything that moved yesterday moved in {areaName}.
concentration.s3.46  [O]  The other areas were not part of yesterday.
concentration.s3.47  [O]  {areaName} was the only one moving.
concentration.s3.48  [O]  The other queues stayed as they were.
concentration.s3.49  [O]  The day did not look past {areaName}.
concentration.s3.50  [O]  Every bit of yesterday was {areaName}.
concentration.s3.51  [O]  The rest of your areas were untouched.
concentration.s3.52  [O]  Yesterday went nowhere near your other areas.
concentration.s3.53  [R]  Yesterday was as narrow as a day gets.
concentration.s3.54  [R]  Most days have more than one area in them.
concentration.s3.55  [R]  That is a complete day in one area.
concentration.s3.56  [R]  Yesterday did not divide at all.
concentration.s3.57  [R]  Nothing shared yesterday with {areaName}.
concentration.s3.58  [R]  Yesterday was one thing all the way through.
concentration.s3.59  [R]  A day can be one thing on purpose.
concentration.s3.60  [R]  That is the whole day in one word.
concentration.s3.61  [R]  There was no second thing yesterday.
concentration.s3.62  [R]  Some days do not divide.
concentration.s3.63  [R]  Yesterday did not need a second area.
concentration.s3.64  [R]  A day with one thing in it is still a day.
concentration.s3.65  [R]  One direction, all day.
concentration.s3.66  [R]  A whole day of one thing.
concentration.s3.67  [R]  Nothing else was in the frame.
concentration.s3.68  [R]  Yesterday was one thing, and it stayed one thing.
concentration.s3.69  [R]  A single area, a whole day.
concentration.s3.70  [R]  One area held the whole of yesterday.
```

### Questions

```
concentration.s3.q01  A season, or a slide?
concentration.s3.q02  On purpose, or it just happened?
concentration.s3.q03  Is this a push, or has something else stalled?
concentration.s3.q04  Deliberate for now, or overdue for a look?
concentration.s3.q05  Are the others on hold, or dropped?
concentration.s3.q06  Working as intended?
concentration.s3.q07  One area, on purpose?
concentration.s3.q08  Was there anything else to do?
concentration.s3.q09  Is this where it all is right now?
concentration.s3.q10  A day like this, or a run of them?
concentration.s3.q11  Would you want another one like it?
concentration.s3.q12  Is anything else waiting on you?
concentration.s3.q13  Did the rest of it cross your mind?
concentration.s3.q14  Right now, is there anything else?
concentration.s3.q15  Does everything else still matter?
concentration.s3.q16  Is one area the plan for now?
concentration.s3.q17  Was there ever a decision in this?
concentration.s3.q18  Is this the season for it?
concentration.s3.q19  Is tomorrow going to look like this?
concentration.s3.q20  Is this where you want to be?
```

### Response pairs

```
concentration.s3.r01  A season / A slide
concentration.s3.r02  On purpose / It just happened
concentration.s3.r03  A push / Something stalled
concentration.s3.r04  Deliberate for now / Overdue for a look
concentration.s3.r05  On hold / Dropped
concentration.s3.r06  As intended / Not really
concentration.s3.r07  Nothing else to do / Other things to do
concentration.s3.r08  This is where it all is / Not really
concentration.s3.r09  Another like it / A different one next
concentration.s3.r10  Nothing is waiting / Something is waiting
concentration.s3.r11  Everything else still matters / Some of it does not
concentration.s3.r12  The plan for now / Not the plan
concentration.s3.r13  Where I want to be / Not where I want to be
concentration.s3.r14  The whole picture / Part of it
```

**Concentration surface count:** 64 x 20 x 14 plus 65 x 20 x 14 plus 70 x 20 x 14 = **55,720 surfaces from 301 authored lines.**

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
accumulation.s1.14  [P]  Yesterday saw {n} things arrive and {m} leave.
accumulation.s1.15  [P]  A few more things came in than went out.
accumulation.s1.16  [P]  The lists took in {n} things.
accumulation.s1.17  [P]  Yesterday put {n} things in and took {m} out.
accumulation.s1.18  [P]  The queues took on {n} things yesterday.
accumulation.s1.19  [P]  The queues are heavier than they were.
accumulation.s1.20  [P]  You wrote down {n} things and finished {m}.
accumulation.s1.21  [P]  Yesterday brought in more than it sent out.
accumulation.s1.22  [P]  The lists are longer than they were yesterday.
accumulation.s1.23  [P]  The queues gained a little.
accumulation.s1.24  [P]  Yesterday added {n} and closed {m}.
accumulation.s1.25  [P]  Yesterday's intake was {n} things.
accumulation.s1.26  [P]  You captured {n} things yesterday.
accumulation.s1.27  [P]  The day put {n} things on the lists.
accumulation.s1.28  [P]  Yesterday's additions came to {n}.
accumulation.s1.29  [P]  Yesterday left the queues a little fuller.
accumulation.s1.30  [O]  More was noticed than finished.
accumulation.s1.31  [O]  Yesterday was a day for writing things down.
accumulation.s1.32  [O]  Things went on the list faster than they came off.
accumulation.s1.33  [O]  The queues are carrying a little more than they were.
accumulation.s1.34  [O]  The queues are holding a little more.
accumulation.s1.35  [O]  Intake edged ahead yesterday.
accumulation.s1.36  [O]  What arrived outnumbered what left.
accumulation.s1.37  [O]  Yesterday collected more than it closed.
accumulation.s1.38  [O]  The lists took on weight.
accumulation.s1.39  [O]  More was written down than crossed off.
accumulation.s1.40  [O]  Your queues ended the day slightly heavier.
accumulation.s1.41  [O]  Yesterday was more about starting than finishing.
accumulation.s1.42  [O]  The day left a little more on the lists than it found.
accumulation.s1.43  [O]  The queues are a fraction longer.
accumulation.s1.44  [O]  Intake was the busier side yesterday.
accumulation.s1.45  [O]  Capture led yesterday.
accumulation.s1.46  [R]  The lists got longer, which is what lists do.
accumulation.s1.47  [R]  A day of noticing.
accumulation.s1.48  [R]  More came to the lists than left them.
accumulation.s1.49  [R]  Some days are for gathering.
accumulation.s1.50  [R]  The list is where things wait.
accumulation.s1.51  [R]  A little more is waiting than was.
accumulation.s1.52  [R]  Yesterday started more than it ended.
accumulation.s1.53  [R]  Yesterday was mostly arrivals.
accumulation.s1.54  [R]  Some days add and some days subtract.
accumulation.s1.55  [R]  Noticing and finishing are not the same day.
accumulation.s1.56  [R]  Yesterday was one of the finding days.
accumulation.s1.57  [R]  The day tipped one way.
accumulation.s1.58  [R]  A day of arrivals more than departures.
accumulation.s1.59  [R]  Yesterday tipped toward the lists.
accumulation.s1.60  [R]  The lists ended yesterday heavier than they started.
accumulation.s1.61  [R]  A small tilt toward taking things on.
accumulation.s1.62  [R]  Slightly more in than out.
accumulation.s1.63  [R]  A day that added more than it took away.
```

### Questions

```
accumulation.s1.q01  Building up, or avoiding?
accumulation.s1.q02  Capturing, or collecting?
accumulation.s1.q03  Getting it out of your head, or putting it off?
accumulation.s1.q04  A planning day, or a slow one?
accumulation.s1.q05  Was there time to finish things?
accumulation.s1.q06  Intentional, or just how it went?
accumulation.s1.q07  Was there anything to finish?
accumulation.s1.q08  Noticing, or postponing?
accumulation.s1.q09  Did the new things need to go on the list?
accumulation.s1.q10  A day for input?
accumulation.s1.q11  Will these get done, or will they wait?
accumulation.s1.q12  Worth writing down, all of it?
accumulation.s1.q13  Is the list keeping up with you, or ahead of you?
accumulation.s1.q14  Groundwork, or backlog?
accumulation.s1.q15  Was yesterday a day for finishing?
accumulation.s1.q16  Room for these, or already full?
accumulation.s1.q17  A good day to capture?
accumulation.s1.q18  Are these for soon, or for someday?
accumulation.s1.q19  Did these arrive, or did you go looking?
accumulation.s1.q20  Is that the right place for them?
```

### Response pairs

```
accumulation.s1.r01  Building up / Avoiding
accumulation.s1.r02  Capturing / Collecting
accumulation.s1.r03  Out of my head / Putting it off
accumulation.s1.r04  Planning day / Slow day
accumulation.s1.r05  No time / Had time
accumulation.s1.r06  Intentional / Just happened
accumulation.s1.r07  Nothing to finish / Things to finish
accumulation.s1.r08  Noticing / Postponing
accumulation.s1.r09  They will get done / They will wait
accumulation.s1.r10  All worth writing down / Some were not
accumulation.s1.r11  Groundwork / Backlog
accumulation.s1.r12  For finishing / For noticing
accumulation.s1.r13  There is room / Already full
accumulation.s1.r14  For soon / For someday
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
accumulation.s2.14  [P]  Yesterday took in {n} things and finished {m}.
accumulation.s2.15  [P]  Several more in than out.
accumulation.s2.16  [P]  The intake side was busier.
accumulation.s2.17  [P]  Several things are waiting now.
accumulation.s2.18  [P]  You captured {n} things and closed {m}.
accumulation.s2.19  [P]  A lot more arrived than left yesterday.
accumulation.s2.20  [P]  The lists picked up {n} things.
accumulation.s2.21  [P]  Yesterday's intake was well ahead.
accumulation.s2.22  [P]  The lists took on {n} things and let {m} go.
accumulation.s2.23  [P]  Your queues are several items longer.
accumulation.s2.24  [P]  Yesterday brought in {n} and sent out {m}.
accumulation.s2.25  [P]  The queues gained {n} arrivals yesterday.
accumulation.s2.26  [P]  The day put {n} things on and took {m} off.
accumulation.s2.27  [P]  Yesterday's additions outnumbered its completions by several.
accumulation.s2.28  [P]  The queues ended noticeably longer.
accumulation.s2.29  [P]  You added {n} things yesterday.
accumulation.s2.30  [P]  Quite a few things arrived.
accumulation.s2.31  [P]  Yesterday's intake ran to {n} things.
accumulation.s2.32  [O]  The queues took on a lot.
accumulation.s2.33  [O]  Yesterday was a collecting day more than a closing one.
accumulation.s2.34  [O]  What came in yesterday was well ahead of what went out.
accumulation.s2.35  [O]  The lists are carrying several more than they were.
accumulation.s2.36  [O]  A good deal more was written down than was closed.
accumulation.s2.37  [O]  The queues have grown since yesterday morning.
accumulation.s2.38  [O]  Yesterday put a lot on the lists.
accumulation.s2.39  [O]  The day was weighted toward taking things on.
accumulation.s2.40  [O]  Intake was the larger half.
accumulation.s2.41  [O]  Yesterday was heavier on arrivals than departures.
accumulation.s2.42  [O]  The lists grew by several.
accumulation.s2.43  [O]  What went on the lists yesterday outweighed what came off.
accumulation.s2.44  [O]  Yesterday brought in a good deal more than it let go.
accumulation.s2.45  [O]  The queues are noticeably fuller.
accumulation.s2.46  [O]  Arrivals ran ahead of departures yesterday.
accumulation.s2.47  [O]  Several things joined the lists.
accumulation.s2.48  [O]  More captured than closed.
accumulation.s2.49  [R]  A collecting day.
accumulation.s2.50  [R]  Yesterday found more than it finished.
accumulation.s2.51  [R]  The list took on some weight.
accumulation.s2.52  [R]  Several arrived. Fewer left.
accumulation.s2.53  [R]  Some days fill the list and some empty it.
accumulation.s2.54  [R]  Yesterday was a day of intake.
accumulation.s2.55  [R]  Yesterday brought a lot in.
accumulation.s2.56  [R]  A day that mostly took things on.
accumulation.s2.57  [R]  More waiting than there was.
accumulation.s2.58  [R]  Yesterday was a day for writing, not for closing.
accumulation.s2.59  [R]  Yesterday was mostly about what is next.
accumulation.s2.60  [R]  The lists are several things heavier than yesterday morning.
accumulation.s2.61  [R]  A list grows before it shrinks, sometimes.
accumulation.s2.62  [R]  Yesterday was more beginning than ending.
accumulation.s2.63  [R]  A day that filled the lists.
accumulation.s2.64  [R]  Several things are waiting that were not waiting before.
accumulation.s2.65  [R]  There is a good deal more on the lists than there was.
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
accumulation.s2.q08  Was that a good haul, or a heavy one?
accumulation.s2.q09  Do these need doing, or just recording?
accumulation.s2.q10  Did they all arrive at once?
accumulation.s2.q11  Will these keep?
accumulation.s2.q12  Was there time for any of it?
accumulation.s2.q13  Too many to do, or too many to keep?
accumulation.s2.q14  Did you mean to take all of that on?
accumulation.s2.q15  Is the queue growing on purpose?
accumulation.s2.q16  Will these still matter next week?
accumulation.s2.q17  A backlog, or a plan?
accumulation.s2.q18  Was any of it ready to finish?
accumulation.s2.q19  More than usual, or the usual?
accumulation.s2.q20  Is this a day you would repeat?
```

### Response pairs

```
accumulation.s2.r01  Building up / Avoiding
accumulation.s2.r02  Capturing / Collecting
accumulation.s2.r03  Planning day / Avoidance day
accumulation.s2.r04  Clearing my head / Filling the queue
accumulation.s2.r05  They all belong / Some do not
accumulation.s2.r06  Necessary / Easier than finishing
accumulation.s2.r07  A good haul / A heavy one
accumulation.s2.r08  They need doing / Just recording
accumulation.s2.r09  They will keep / They will not
accumulation.s2.r10  There was time / There was no time
accumulation.s2.r11  Too many to do / Too many to keep
accumulation.s2.r12  They will still matter / Some will not
accumulation.s2.r13  A plan / A backlog
accumulation.s2.r14  More than usual / The usual
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
accumulation.s3.12  [P]  Three weeks of growth.
accumulation.s3.13  [P]  Longer every week for three weeks.
accumulation.s3.14  [P]  The queues are longer again.
accumulation.s3.15  [P]  The lists ended the week longer.
accumulation.s3.16  [P]  The queues rose again.
accumulation.s3.17  [P]  The queues grew again yesterday.
accumulation.s3.18  [P]  More is on the lists now.
accumulation.s3.19  [P]  Three weeks, all of them longer.
accumulation.s3.20  [P]  More waiting than yesterday.
accumulation.s3.21  [P]  The queues rose three weeks running.
accumulation.s3.22  [P]  The queues have been growing for three weeks.
accumulation.s3.23  [P]  Your lists have grown in each of the last three weeks.
accumulation.s3.24  [P]  The queues were shorter three weeks ago.
accumulation.s3.25  [P]  Each of the last three weeks left the queues longer.
accumulation.s3.26  [P]  The lists are longer than they were three weeks ago.
accumulation.s3.27  [P]  Week on week, the queues have grown.
accumulation.s3.28  [P]  Yesterday put another {n} things on the lists.
accumulation.s3.29  [P]  The lists are longer this morning than yesterday morning.
accumulation.s3.30  [P]  Yesterday left the queues longer than it found them.
accumulation.s3.31  [P]  The lists took on more than they gave up yesterday.
accumulation.s3.32  [O]  The lists have been climbing.
accumulation.s3.33  [O]  The queues keep growing.
accumulation.s3.34  [O]  The queues have only gone up.
accumulation.s3.35  [O]  The queues have not shortened lately.
accumulation.s3.36  [O]  Output has not caught up.
accumulation.s3.37  [O]  Intake is still ahead.
accumulation.s3.38  [O]  More on than off.
accumulation.s3.39  [O]  The lists have not stopped growing.
accumulation.s3.40  [O]  The lists are still filling.
accumulation.s3.41  [O]  Nothing has taken the queues back down.
accumulation.s3.42  [O]  More goes on than comes off, week after week.
accumulation.s3.43  [O]  Three weeks of intake ahead of output.
accumulation.s3.44  [O]  Arrivals have outnumbered departures for a while.
accumulation.s3.45  [O]  What comes in has been staying in.
accumulation.s3.46  [O]  The lists keep taking on more than they give up.
accumulation.s3.47  [O]  Yesterday added to a list that was already growing.
accumulation.s3.48  [O]  Intake has been the larger side for weeks.
accumulation.s3.49  [O]  The queues have been filling faster than they empty.
accumulation.s3.50  [O]  What is on the lists has been accumulating.
accumulation.s3.51  [R]  Lists grow quietly.
accumulation.s3.52  [R]  Weeks in the making.
accumulation.s3.53  [R]  The shape is older than yesterday.
accumulation.s3.54  [R]  A long slow build.
accumulation.s3.55  [R]  Older than one day.
accumulation.s3.56  [R]  Not a one day thing.
accumulation.s3.57  [R]  A pattern, not a day.
accumulation.s3.58  [R]  A slow accumulation.
accumulation.s3.59  [R]  A list can grow for a long time before anyone notices.
accumulation.s3.60  [R]  The last three weeks all went one way.
accumulation.s3.61  [R]  A list gets long one day at a time.
accumulation.s3.62  [R]  This has been the direction for a while.
accumulation.s3.63  [R]  Three weeks in one direction is a pattern.
accumulation.s3.64  [R]  What is waiting has been waiting a while.
accumulation.s3.65  [R]  A backlog is just a list that kept going.
accumulation.s3.66  [R]  This has been building for weeks.
accumulation.s3.67  [R]  There is more here than a day made.
accumulation.s3.68  [R]  Nothing about this happened yesterday.
accumulation.s3.69  [R]  More has been arriving than leaving, steadily.
accumulation.s3.70  [R]  A list this long took time to make.
accumulation.s3.71  [R]  What arrives is outpacing what leaves.
```

### Questions

```
accumulation.s3.q01  Building up, or avoiding?
accumulation.s3.q02  Is the queue still a plan?
accumulation.s3.q03  Do these all still belong?
accumulation.s3.q04  Growing on purpose, or growing by default?
accumulation.s3.q05  Worth a clear out?
accumulation.s3.q06  Too much coming in, or not enough going out?
accumulation.s3.q07  Does the list still describe the work?
accumulation.s3.q08  A season, or how it is now?
accumulation.s3.q09  Is anything on there done already?
accumulation.s3.q10  Has this been useful, or just growing?
accumulation.s3.q11  Do you look at all of it?
accumulation.s3.q12  Is there a plan for the older ones?
accumulation.s3.q13  Would you write all of these again?
accumulation.s3.q14  Room for more, or full?
accumulation.s3.q15  Would you miss any of them?
accumulation.s3.q16  Is this collecting, or is this storing?
accumulation.s3.q17  Is this what you want it to be?
accumulation.s3.q18  Growing, or gathering?
accumulation.s3.q19  Keep it all, or cut some?
accumulation.s3.q20  Does the size of it bother you?
```

### Response pairs

```
accumulation.s3.r01  Building up / Avoiding
accumulation.s3.r02  Still a plan / A pile
accumulation.s3.r03  They belong / Time to cut some
accumulation.s3.r04  On purpose / By default
accumulation.s3.r05  Too much coming in / Not enough going out
accumulation.s3.r06  Fine for now / Worth a clear out
accumulation.s3.r07  It describes the work / It has drifted
accumulation.s3.r08  A season / How it is now
accumulation.s3.r09  Useful / Just growing
accumulation.s3.r10  I look at all of it / I do not
accumulation.s3.r11  There is room / It is full
accumulation.s3.r12  Collecting / Storing
accumulation.s3.r13  Keep it all / Cut some
accumulation.s3.r14  It does not bother me / It does
```

**Accumulation surface count:** 63 x 20 x 14 plus 65 x 20 x 14 plus 71 x 20 x 14 = **55,720 surfaces from 301 authored lines.**

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

**Throughput surface count:** 12 x 5 x 5 plus 13 x 6 x 5 plus 10 x 5 x 5 = **940 surfaces from 66 authored lines.**

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
quietday.s1.13  [P]  Yesterday was still.
quietday.s1.14  [P]  Nothing finished yesterday.
quietday.s1.15  [P]  Nothing arrived yesterday.
quietday.s1.16  [P]  One quiet day.
quietday.s1.17  [P]  Yesterday passed quietly.
quietday.s1.18  [P]  No swaps yesterday.
quietday.s1.19  [P]  Nothing left the queues.
quietday.s1.20  [P]  A day with nothing in it.
quietday.s1.21  [P]  Yesterday left things as they were.
quietday.s1.22  [P]  A day of almost no events.
quietday.s1.23  [P]  An uneventful day.
quietday.s1.24  [P]  Yesterday came and went with nothing logged.
quietday.s1.25  [P]  No items moved in any area yesterday.
quietday.s1.26  [P]  Nothing completed, nothing added, nothing swapped.
quietday.s1.27  [P]  Yesterday has little to describe.
quietday.s1.28  [P]  The day went by without touching this app.
quietday.s1.29  [P]  Nothing in any of your areas moved yesterday.
quietday.s1.30  [O]  The areas did not move.
quietday.s1.31  [O]  Nothing came or went.
quietday.s1.32  [O]  The app saw nothing.
quietday.s1.33  [O]  Everything sat where it was.
quietday.s1.34  [O]  No item changed place.
quietday.s1.35  [O]  The fronts stayed put.
quietday.s1.36  [O]  Everything stayed.
quietday.s1.37  [O]  No area moved.
quietday.s1.38  [O]  Every area looks the way it looked the day before.
quietday.s1.39  [O]  The queues are the same length they were.
quietday.s1.40  [O]  Nothing arrived and nothing left yesterday.
quietday.s1.41  [O]  Your areas kept their shape through yesterday.
quietday.s1.42  [O]  The front of every area is unchanged.
quietday.s1.43  [O]  Yesterday went past without moving anything here.
quietday.s1.44  [O]  Whatever happened yesterday happened somewhere else.
quietday.s1.45  [O]  One day passed and the lists stayed the same.
quietday.s1.46  [O]  No area added anything and none finished anything.
quietday.s1.47  [O]  The same items are at the same fronts.
quietday.s1.48  [O]  The lists are the lists from the day before.
quietday.s1.49  [O]  A day went by and the app has the same picture.
quietday.s1.50  [O]  Every area finished yesterday the way it started it.
quietday.s1.51  [O]  The same items are waiting in the same order.
quietday.s1.52  [O]  Yesterday made no difference to any queue.
quietday.s1.53  [O]  A quiet day leaves the same numbers as the day before.
quietday.s1.54  [R]  A day without a mark.
quietday.s1.55  [R]  A small day.
quietday.s1.56  [R]  Some days are like that.
quietday.s1.57  [R]  A near blank day.
quietday.s1.58  [R]  Yesterday was its own thing.
quietday.s1.59  [R]  A day that stayed put.
quietday.s1.60  [R]  Still, everywhere.
quietday.s1.61  [R]  A day can pass without a record.
quietday.s1.62  [R]  A quiet day is still a day that happened.
quietday.s1.63  [R]  Some days go by without leaving anything behind.
quietday.s1.64  [R]  Yesterday asked nothing of this app.
quietday.s1.65  [R]  A quiet day here can be a full day elsewhere.
quietday.s1.66  [R]  Rest and overload look the same from in here.
quietday.s1.67  [R]  Some weeks have a day like this in them.
```

### Questions

```
quietday.s1.q01  What kind of quiet was it?
quietday.s1.q02  Where did the day go?
quietday.s1.q03  Rest, or overload?
quietday.s1.q04  Off, or elsewhere?
quietday.s1.q05  How was it, actually?
quietday.s1.q06  Quiet by choice?
quietday.s1.q07  Busy elsewhere?
quietday.s1.q08  What was yesterday for?
quietday.s1.q09  Needed, or not?
quietday.s1.q10  A pause, or a stop?
quietday.s1.q11  Full day, or empty one?
quietday.s1.q12  What took the day?
quietday.s1.q13  Rested, or run down?
quietday.s1.q14  Away on purpose?
quietday.s1.q15  How did it feel?
quietday.s1.q16  One of those days?
quietday.s1.q17  Did you need it?
quietday.s1.q18  Anything happening away from here?
quietday.s1.q19  Choice, or circumstance?
quietday.s1.q20  Off the app, or off entirely?
```

### Response pairs

Three options here rather than two. Recharging and Busy elsewhere are positive, the third
is the honest one.

**Every label names the day or the load. None of them names the person, and the third one
still has to read as the harder answer.**

Positivity here is assigned by POSITION and nothing else: `CorpusParser` marks every option
but the last as positive, so a third label that reads neutral gets a person's tap written to
the log as a hard day they did not report. The first attempt at this rewrite traded four
pathologizing labels for four that read as fine, which is the same defect with the sign
turned round. A third label has to be plainly the harder of the three AND still be about the
day. That is 6.4's
own rule, applied at the one point in this product where a person taps a word about
themselves: the tap is written to the log, rendered in the Trail forever, carried into the
plaintext export, and quotable back weeks later as `{priorLabel}`. `Overwhelmed`, `Not
coping`, `Underwater`, `Running on empty` and `Struggling` were all available after ONE
day with fewer than two events, which is an ordinary Tuesday, and each of them is a
diagnosis in the first person that the app then keeps. `Too much at once`, `A hard one`,
`Too much on` and `Nothing left in the tank` describe the same day and make no claim about
who lived it.

```
quietday.s1.r01  Recharging / Busy elsewhere / Too much at once
quietday.s1.r02  Resting / Elsewhere / Stuck
quietday.s1.r03  Deliberate / Life happened / Too much on
quietday.s1.r04  A day off / Off the app / No room for anything
quietday.s1.r05  Needed it / Doing other things / A hard one
quietday.s1.r06  Rested / Busy elsewhere / Nothing left in the tank
quietday.s1.r07  On purpose / Other things came up / A day that got away
quietday.s1.r08  A pause / Away / Stalled
quietday.s1.r09  Needed the day / Out doing things / Never got started
quietday.s1.r10  Chosen / Circumstance / Too full for it
quietday.s1.r11  Time off / Time elsewhere / No time at all
quietday.s1.r12  Fine / Busy / A hard day
quietday.s1.r13  Recovered / Occupied / A tiring one
quietday.s1.r14  Slow on purpose / Slow by circumstance / Ran out of day
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
quietday.s2.12  [P]  Still quiet.
quietday.s2.13  [P]  Quiet again.
quietday.s2.14  [P]  A few quiet days.
quietday.s2.15  [P]  The queues are unchanged.
quietday.s2.16  [P]  A run of quiet days.
quietday.s2.17  [P]  Days without events.
quietday.s2.18  [P]  Quiet before this too.
quietday.s2.19  [P]  No finishes in a few days.
quietday.s2.20  [P]  The queues sit where they sat.
quietday.s2.21  [P]  Nothing new in any area.
quietday.s2.22  [P]  Still where it all was.
quietday.s2.23  [P]  A few days of stillness.
quietday.s2.24  [P]  No movement anywhere.
quietday.s2.25  [P]  The same fronts as before.
quietday.s2.26  [P]  Nothing moved anywhere for a few days.
quietday.s2.27  [P]  No area has changed in the last few days.
quietday.s2.28  [P]  Nothing has finished in any area for a few days.
quietday.s2.29  [P]  The same items have been active for a few days.
quietday.s2.30  [P]  Several days have passed without an event here.
quietday.s2.31  [P]  Nothing has arrived and nothing has left for a few days.
quietday.s2.32  [P]  A few days have gone without a completion.
quietday.s2.33  [P]  The last few days hold no events.
quietday.s2.34  [P]  Every area has been where it is for a few days.
quietday.s2.35  [O]  The areas have not moved.
quietday.s2.36  [O]  Nothing has come or gone.
quietday.s2.37  [O]  Every front is the same.
quietday.s2.38  [O]  No swaps, no finishes.
quietday.s2.39  [O]  No item took the front.
quietday.s2.40  [O]  Nothing has left an area.
quietday.s2.41  [O]  Every list is unchanged.
quietday.s2.42  [O]  No item has moved.
quietday.s2.43  [O]  The app has been still too.
quietday.s2.44  [O]  Every queue is the length it was a few days ago.
quietday.s2.45  [O]  The fronts of your areas have not changed in days.
quietday.s2.46  [O]  A few days have passed and the lists look the same.
quietday.s2.47  [O]  Whatever has been happening has been happening elsewhere.
quietday.s2.48  [O]  The app has the same picture it had a few days ago.
quietday.s2.49  [O]  Your areas have kept their shape for a few days.
quietday.s2.50  [O]  Days have gone by with the same items at the front.
quietday.s2.51  [O]  The same items have been at the same fronts for days.
quietday.s2.52  [O]  No queue has grown or shrunk in a few days.
quietday.s2.53  [O]  The picture here has not changed in a few days.
quietday.s2.54  [O]  The last few days have left nothing here.
quietday.s2.55  [R]  A still run of days.
quietday.s2.56  [R]  More than one still day.
quietday.s2.57  [R]  A few days is not a pattern.
quietday.s2.58  [R]  Nothing here knows why the days were quiet.
quietday.s2.59  [R]  A few quiet days can be a full week elsewhere.
quietday.s2.60  [R]  A short quiet.
quietday.s2.61  [R]  A small run of quiet.
quietday.s2.62  [R]  A few days is enough to notice and not enough to name.
quietday.s2.63  [R]  Nothing here can tell a rest from a hard week.
quietday.s2.64  [R]  A few days of quiet is a run and runs end.
quietday.s2.65  [R]  Some quiet runs are chosen and some are not.
```

### Questions

```
quietday.s2.q01  What kind of quiet is this?
quietday.s2.q02  Rest, or overload?
quietday.s2.q03  Off, or elsewhere?
quietday.s2.q04  Is this a break, or a stall?
quietday.s2.q05  How has it actually been?
quietday.s2.q06  Chosen, or not?
quietday.s2.q07  Busy elsewhere?
quietday.s2.q08  What has been taking the time?
quietday.s2.q09  A pause, or a stop?
quietday.s2.q10  Needed, or stuck?
quietday.s2.q11  Away, or under it?
quietday.s2.q12  Still resting?
quietday.s2.q13  Anything happening off the app?
quietday.s2.q14  Quiet here, busy there?
quietday.s2.q15  Full days, or empty ones?
quietday.s2.q16  Where have the days gone?
quietday.s2.q17  Rested, or run down?
quietday.s2.q18  Coming back to it?
quietday.s2.q19  A break you took, or one you got?
quietday.s2.q20  How is it going, away from here?
```

### Response pairs

```
quietday.s2.r01  Recharging / Busy elsewhere / Too much at once
quietday.s2.r02  A break / Elsewhere / A stall
quietday.s2.r03  Deliberate / Life happened / Too much on
quietday.s2.r04  Resting / Occupied / A hard one
quietday.s2.r05  On purpose / Other things came up / No room for it
quietday.s2.r06  A pause / Away / Stuck in it
quietday.s2.r07  Resting / Working elsewhere / Cannot get started
quietday.s2.r08  Chosen / Circumstance / Too full for it
quietday.s2.r09  Time off / Time elsewhere / No time at all
quietday.s2.r10  Fine / Busy / A hard day
quietday.s2.r11  Recovered / Occupied / A tiring one
quietday.s2.r12  Coming back / Still away / Not ready
quietday.s2.r13  Needed the days / Out doing things / Under it
quietday.s2.r14  Slow on purpose / Slow by circumstance / Stopped
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
quietday.s3.11  [P]  Quiet, still.
quietday.s3.12  [P]  Days with nothing in them.
quietday.s3.13  [P]  No events for a while.
quietday.s3.14  [P]  Still nothing here.
quietday.s3.15  [P]  A run of still days.
quietday.s3.16  [P]  No area has moved.
quietday.s3.17  [P]  Quiet for days now.
quietday.s3.18  [P]  A long quiet.
quietday.s3.19  [P]  Nothing for several days.
quietday.s3.20  [P]  Quiet for a while now.
quietday.s3.21  [P]  No finishes for days.
quietday.s3.22  [P]  Nothing added in days.
quietday.s3.23  [P]  Days without an event.
quietday.s3.24  [P]  The same fronts, days on.
quietday.s3.25  [P]  Still, days later.
quietday.s3.26  [P]  No change for days.
quietday.s3.27  [P]  Several days have gone by with nothing moving.
quietday.s3.28  [P]  Nothing has finished in any area for several days.
quietday.s3.29  [P]  The same items have been active for days now.
quietday.s3.30  [P]  Days have passed without an event in any area.
quietday.s3.31  [P]  Nothing has arrived and nothing has left for days.
quietday.s3.32  [P]  Every area is exactly as it was several days ago.
quietday.s3.33  [P]  Several days have gone without a completion.
quietday.s3.34  [P]  The last several days hold no events.
quietday.s3.35  [P]  Every area has been where it is for days.
quietday.s3.36  [O]  Every area is untouched.
quietday.s3.37  [O]  The fronts have not turned.
quietday.s3.38  [O]  The lists are as they were.
quietday.s3.39  [O]  No swaps for days.
quietday.s3.40  [O]  Everything has held its place.
quietday.s3.41  [O]  Every queue is unchanged.
quietday.s3.42  [O]  Nothing has taken a turn.
quietday.s3.43  [O]  The areas have sat still.
quietday.s3.44  [O]  No item has moved in days.
quietday.s3.45  [O]  The queues have been the same length for days.
quietday.s3.46  [O]  The fronts of your areas have not changed in a while.
quietday.s3.47  [O]  Whatever has been happening has not been happening here.
quietday.s3.48  [O]  Days have gone by with the same items at the front.
quietday.s3.49  [O]  The app has held the same picture for days.
quietday.s3.50  [O]  Nothing has moved in or out of any area for days.
quietday.s3.51  [O]  Your areas have looked like this for a while now.
quietday.s3.52  [O]  The same items have been waiting in the same order for days.
quietday.s3.53  [O]  No queue has grown or shrunk in days.
quietday.s3.54  [O]  The picture here has not changed in days.
quietday.s3.55  [O]  The last several days have left nothing here.
quietday.s3.56  [R]  A quiet run, still going.
quietday.s3.57  [R]  Some stretches are like this.
quietday.s3.58  [R]  A quiet stretch can be rest or it can be a stall.
quietday.s3.59  [R]  Nothing here knows why the quiet has lasted.
quietday.s3.60  [R]  Days of quiet here can be days of work elsewhere.
quietday.s3.61  [R]  Nothing about a quiet run says which kind it is.
quietday.s3.62  [R]  A quiet spell.
quietday.s3.63  [R]  Days of the same.
quietday.s3.64  [R]  Some runs are long.
quietday.s3.65  [R]  A run this long is a fact and the reason is not in here.
quietday.s3.66  [R]  Nothing here can tell a break from a stop.
```

### Questions

```
quietday.s3.q01  What kind of quiet is this?
quietday.s3.q02  A break, or a stall?
quietday.s3.q03  Rest, or overload?
quietday.s3.q04  Is the app still the right shape for you?
quietday.s3.q05  Off, or elsewhere?
quietday.s3.q06  Still resting?
quietday.s3.q07  Busy somewhere else?
quietday.s3.q08  What has the time been going to?
quietday.s3.q09  A season, or a stall?
quietday.s3.q10  Coming back, or done for now?
quietday.s3.q11  Chosen, or not?
quietday.s3.q12  Is this a hard stretch?
quietday.s3.q13  A stretch you chose?
quietday.s3.q14  Away, or under it?
quietday.s3.q15  How has the stretch been?
quietday.s3.q16  Anything moving off the app?
quietday.s3.q17  Rested, or run down?
quietday.s3.q18  Missing it, or not really?
quietday.s3.q19  A break you took, or one that took you?
quietday.s3.q20  How long has it felt?
```

### Response pairs

```
quietday.s3.r01  Recharging / Busy elsewhere / Too much at once
quietday.s3.r02  A break / Elsewhere / A stall
quietday.s3.r03  Needed it / Doing other things / A hard one
quietday.s3.r04  Deliberate / Life happened / Too much on
quietday.s3.r05  Resting / Elsewhere / A tiring one
quietday.s3.r06  A season / A break / No room for it
quietday.s3.r07  On purpose / Other things came up / A slow one
quietday.s3.r08  Coming back / Still away / Not ready
quietday.s3.r09  Fine / Busy / A hard day
quietday.s3.r10  Needed the time / Occupied / Under it
quietday.s3.r11  Chosen / Circumstance / Too full for it
quietday.s3.r12  Still fits / Needs a rethink / Too much on it
quietday.s3.r13  Rested / Away / Run down
quietday.s3.r14  Time off / Time elsewhere / No time at all
```

**Quiet day surface count:** 67 x 20 x 14 plus 65 x 20 x 14 plus 66 x 20 x 14 = **55,440 surfaces from 300 authored lines.**

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

**Spread surface count:** 11 x 5 x 5 plus 11 x 6 x 6 = **671 surfaces from 44 authored lines.**

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

**Switching surface count:** 9 x 5 x 5 plus 9 x 5 x 5 = **450 surfaces from 38 authored lines.**

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

**Burst surface count:** 9 x 5 x 5 plus 8 x 5 x 5 = **425 surfaces from 37 authored lines.**

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
rebalance.s1.09  [P]  {areaName} went {ageDays} without an event.
rebalance.s1.10  [P]  The quiet in {areaName} ended yesterday.
rebalance.s1.11  [P]  Nothing happened in {areaName} for {ageDays}. Yesterday something did.
rebalance.s1.12  [P]  {areaName} had its first activity in {ageDays}.
rebalance.s1.13  [P]  Before yesterday, {areaName} had been unchanged for {ageDays}.
rebalance.s1.14  [P]  {areaName} is active again.
rebalance.s1.15  [P]  The gap in {areaName} was {ageDays} long.
rebalance.s1.16  [P]  Yesterday was the first day in {ageDays} that {areaName} moved.
rebalance.s1.17  [P]  Something moved in {areaName} yesterday, after {ageDays}.
rebalance.s1.18  [P]  The last event in {areaName} before yesterday was {ageDays} ago.
rebalance.s1.19  [P]  {areaName} did not move for {ageDays}. Yesterday it did.
rebalance.s1.20  [P]  Activity returned to {areaName} yesterday.
rebalance.s1.21  [P]  The quiet in {areaName} lasted {ageDays}.
rebalance.s1.22  [P]  {areaName} was still until yesterday.
rebalance.s1.23  [P]  Something in {areaName} yesterday.
rebalance.s1.24  [P]  Yesterday, {areaName} moved.
rebalance.s1.25  [O]  The last two things to happen in {areaName} are {ageDays} apart.
rebalance.s1.26  [P]  Nothing in {areaName} moved for {ageDays}.
rebalance.s1.27  [P]  {areaName} came out of {ageDays} of quiet yesterday.
rebalance.s1.28  [P]  {areaName} did nothing for {ageDays} until yesterday.
rebalance.s1.29  [O]  The last {ageDays} in {areaName} were empty. Yesterday was not.
rebalance.s1.30  [O]  The stillness in {areaName} was {ageDays} old when it ended.
rebalance.s1.31  [O]  The app has {ageDays} of nothing for {areaName}, then yesterday.
rebalance.s1.32  [O]  Nothing came in or out of {areaName} for {ageDays}.
rebalance.s1.33  [O]  {areaName} moved yesterday and had not for {ageDays} before that.
rebalance.s1.34  [O]  Yesterday, {areaName} stopped being quiet.
rebalance.s1.35  [O]  For {ageDays} {areaName} was still. Yesterday it moved.
rebalance.s1.36  [O]  The gap in {areaName} closed yesterday.
rebalance.s1.37  [O]  Nothing was recorded in {areaName} for {ageDays}.
rebalance.s1.38  [O]  Something in {areaName} broke {ageDays} of quiet.
rebalance.s1.39  [O]  The quiet in {areaName} was {ageDays} and yesterday was not part of it.
rebalance.s1.40  [O]  The days in {areaName} before yesterday were all the same.
rebalance.s1.41  [O]  Yesterday {areaName} did what it had not done for {ageDays}.
rebalance.s1.42  [O]  {areaName} was still for {ageDays}. That ended yesterday.
rebalance.s1.43  [O]  {areaName} shows nothing for {ageDays} and something for yesterday.
rebalance.s1.44  [O]  Something was different about {areaName} yesterday.
rebalance.s1.45  [O]  Yesterday is the newest thing in {areaName} by {ageDays}.
rebalance.s1.46  [O]  {areaName} is not where it was {ageDays} ago.
rebalance.s1.47  [P]  Nothing for {ageDays}. Then yesterday.
rebalance.s1.48  [O]  {areaName} has been the same for {ageDays} and is not now.
rebalance.s1.49  [R]  A gap, then a day.
rebalance.s1.50  [R]  Something came back.
rebalance.s1.51  [R]  A pause in {areaName}, now ended.
rebalance.s1.52  [R]  Areas do not run at one speed.
rebalance.s1.53  [R]  Some things run in bursts with quiet in between.
rebalance.s1.54  [R]  Things come back.
rebalance.s1.55  [R]  Every area has quiet stretches.
rebalance.s1.56  [R]  Some areas move in weeks rather than days.
rebalance.s1.57  [R]  Whatever the gap was, it is over.
rebalance.s1.58  [R]  That is either a restart or a visit.
rebalance.s1.59  [R]  Quiet and gone are different things.
rebalance.s1.60  [R]  Some areas wait longer than others.
rebalance.s1.61  [R]  An area can be quiet without being closed.
rebalance.s1.62  [R]  {areaName} stirred.
rebalance.s1.63  [R]  An area does not move every week.
rebalance.s1.64  [R]  A gap of {ageDays} has two ends.
rebalance.s1.65  [R]  Areas keep their own time.
rebalance.s1.66  [R]  Two ordinary days, {ageDays} apart.
rebalance.s1.67  [R]  Areas go quiet and areas come back.
rebalance.s1.68  [R]  The quiet had a last day and yesterday was after it.
```

### Questions

```
rebalance.s1.q01  Planned, or it just happened?
rebalance.s1.q02  Back for good, or a one off?
rebalance.s1.q03  Did you notice the gap?
rebalance.s1.q04  A return, or a reminder?
rebalance.s1.q05  Was the pause deliberate?
rebalance.s1.q06  Back on purpose, or by accident?
rebalance.s1.q07  Is this still an area you use?
rebalance.s1.q08  Do you know why the gap happened?
rebalance.s1.q09  Picking it up again, or just checking in?
rebalance.s1.q10  Was the gap a break, or a drift?
rebalance.s1.q11  Is the reason for the gap still true?
rebalance.s1.q12  Was it out of mind, or out of time?
rebalance.s1.q13  Does this feel like a return?
rebalance.s1.q14  Was that a decision?
rebalance.s1.q15  Did anything change to make it possible?
rebalance.s1.q16  Is this area on a slower clock than the rest?
rebalance.s1.q17  Was there a moment it came back to mind?
rebalance.s1.q18  Is the gap the odd part, or the day?
rebalance.s1.q19  Did the gap feel long?
rebalance.s1.q20  Something changed, or nothing did?
```

### Response pairs

```
rebalance.s1.r01  Planned / It just happened
rebalance.s1.r02  Back for good / A one off
rebalance.s1.r03  Noticed the gap / Did not notice
rebalance.s1.r04  A return / A reminder
rebalance.s1.r05  The pause was deliberate / It got away from me
rebalance.s1.r06  On purpose / By accident
rebalance.s1.r07  Picking it up / Just checking in
rebalance.s1.r08  A break / A drift
rebalance.s1.r09  Still mine / Not sure any more
rebalance.s1.r10  Out of mind / Out of time
rebalance.s1.r11  Something changed / Nothing did
rebalance.s1.r12  Slower by nature / Same as the rest
rebalance.s1.r13  Felt long / Went quickly
rebalance.s1.r14  Back to it / Just a look
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
rebalance.s2.09  [P]  {areaName} had been quiet for {ageDays}.
rebalance.s2.10  [P]  {areaName} moved yesterday for the first time in {ageDays}.
rebalance.s2.11  [P]  {areaName} came back after {ageDays} away.
rebalance.s2.12  [P]  {areaName} sat unchanged for {ageDays}.
rebalance.s2.13  [P]  No events in {areaName} for {ageDays}.
rebalance.s2.14  [P]  Something happened in {areaName} yesterday. Before that, {ageDays}.
rebalance.s2.15  [P]  {areaName} was last active {ageDays} back.
rebalance.s2.16  [P]  {areaName} went {ageDays} between one event and the next.
rebalance.s2.17  [P]  Yesterday, {areaName} did something. It had not in {ageDays}.
rebalance.s2.18  [P]  A gap of {ageDays} in {areaName}.
rebalance.s2.19  [P]  Yesterday broke {ageDays} of quiet.
rebalance.s2.20  [P]  {areaName} showed nothing for {ageDays}.
rebalance.s2.21  [P]  Yesterday is {ageDays} after the last time {areaName} moved.
rebalance.s2.22  [P]  {areaName} moved after a long quiet.
rebalance.s2.23  [P]  Nothing had happened in {areaName} since {ageDays} back.
rebalance.s2.24  [P]  {areaName} was still, then yesterday.
rebalance.s2.25  [P]  Activity in {areaName}, after {ageDays}.
rebalance.s2.26  [P]  {areaName} has {ageDays} of nothing in it.
rebalance.s2.27  [P]  {areaName} went quiet {ageDays} ago.
rebalance.s2.28  [P]  Something in {areaName} yesterday, after {ageDays}.
rebalance.s2.29  [O]  Something came back to {areaName}.
rebalance.s2.30  [O]  Whatever kept {areaName} quiet for {ageDays} did not keep it quiet yesterday.
rebalance.s2.31  [O]  The app had {ageDays} of nothing to say about {areaName}.
rebalance.s2.32  [O]  {areaName} went a long time without a day.
rebalance.s2.33  [O]  {areaName} moved yesterday, which had not happened in {ageDays}.
rebalance.s2.34  [O]  Yesterday is the first day {areaName} has had in {ageDays}.
rebalance.s2.35  [O]  Nothing about {areaName} was different for {ageDays}. Yesterday was.
rebalance.s2.36  [O]  The record shows {ageDays} of nothing in {areaName}, then yesterday.
rebalance.s2.37  [O]  {areaName} came back from {ageDays} of nothing.
rebalance.s2.38  [O]  Something moved in {areaName} for the first time in a long while.
rebalance.s2.39  [O]  The stillness in {areaName} ran {ageDays}.
rebalance.s2.40  [O]  {areaName} was where it was {ageDays} ago until yesterday.
rebalance.s2.41  [O]  {areaName} had no day of its own for {ageDays}.
rebalance.s2.42  [O]  The quiet in {areaName} was old by the time it ended.
rebalance.s2.43  [O]  Nothing here saw {areaName} for {ageDays}.
rebalance.s2.44  [O]  Something is different in {areaName}.
rebalance.s2.45  [O]  Something ended the quiet in {areaName}.
rebalance.s2.46  [O]  {areaName} is different from {ageDays} ago.
rebalance.s2.47  [O]  The quiet in {areaName} is done.
rebalance.s2.48  [O]  Nothing in {areaName} until yesterday.
rebalance.s2.49  [R]  A long gap, then a day.
rebalance.s2.50  [R]  Some areas go away and come back.
rebalance.s2.51  [R]  Long gaps end the same way short ones do.
rebalance.s2.52  [R]  {areaName} came back from further away.
rebalance.s2.53  [R]  Some areas are seasonal.
rebalance.s2.54  [R]  Some things come back and some do not.
rebalance.s2.55  [R]  {areaName} reappeared.
rebalance.s2.56  [R]  The distance between two days can be {ageDays}.
rebalance.s2.57  [R]  Something that was gone is not gone.
rebalance.s2.58  [R]  Some parts of a life move by the month.
rebalance.s2.59  [R]  {areaName} has a yesterday again.
rebalance.s2.60  [R]  Some returns take {ageDays}.
rebalance.s2.61  [R]  An area can be quiet for a season.
rebalance.s2.62  [R]  The long quiet is over.
rebalance.s2.63  [R]  A gap can be a decision or an accident.
rebalance.s2.64  [R]  A day can end a long quiet.
rebalance.s2.65  [R]  A gap of {ageDays} is a fact with two dates in it.
rebalance.s2.66  [R]  Long quiets end.
rebalance.s2.67  [R]  Areas can go a long time between days.
rebalance.s2.68  [R]  {areaName} is back in the record.
```

### Questions

```
rebalance.s2.q01  Planned, or it just happened?
rebalance.s2.q02  Back for good, or passing through?
rebalance.s2.q03  Did you miss it, or had you let it go?
rebalance.s2.q04  Does it still belong here?
rebalance.s2.q05  A return, or a last try?
rebalance.s2.q06  Has it been on your mind at all?
rebalance.s2.q07  Did you decide to leave it, or did it just go?
rebalance.s2.q08  Did anything change in that time?
rebalance.s2.q09  Is it the same thing it was?
rebalance.s2.q10  Was it out of reach, or out of mind?
rebalance.s2.q11  Does it still fit the week you have?
rebalance.s2.q12  Did you notice how long it had been?
rebalance.s2.q13  Would you have guessed how long it was?
rebalance.s2.q14  Is it the area, or the timing?
rebalance.s2.q15  Coming back, or closing it out?
rebalance.s2.q16  Did something make room for it?
rebalance.s2.q17  Is this one of those that goes in cycles?
rebalance.s2.q18  Did it feel gone?
rebalance.s2.q19  Was it ever really away?
rebalance.s2.q20  What brought it back?
```

### Response pairs

```
rebalance.s2.r01  Planned / It just happened
rebalance.s2.r02  Back for good / Passing through
rebalance.s2.r03  Missed it / Had let it go
rebalance.s2.r04  It belongs / Not sure it does
rebalance.s2.r05  A return / A last try
rebalance.s2.r06  On my mind / Not on my mind
rebalance.s2.r07  I left it / It got left
rebalance.s2.r08  Things changed / Nothing changed
rebalance.s2.r09  Same as it was / Different now
rebalance.s2.r10  Out of reach / Out of mind
rebalance.s2.r11  Still fits / Does not fit
rebalance.s2.r12  Knew how long / Longer than I thought
rebalance.s2.r13  Coming back / Closing it out
rebalance.s2.r14  Room for it now / No room yet
```

**Rebalance surface count:** 68 x 20 x 14 plus 68 x 20 x 14 = **38,080 surfaces from 204 authored lines.**

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

**Queue drain surface count:** 8 x 5 x 5 plus 7 x 5 x 5 = **375 surfaces from 35 authored lines.**

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
| Persistence | 417 | 83,724 |
| Concentration | 301 | 55,720 |
| Accumulation | 301 | 55,720 |
| Throughput | 66 | 940 |
| Quiet day | 300 | 55,440 |
| Spread | 44 | 671 |
| Switching | 38 | 450 |
| Burst | 37 | 425 |
| Rebalance | 204 | 38,080 |
| Queue drain | 35 | 375 |
| Fresh start | 20 | 250 |
| Acknowledgments | 12 | 12 |
| **Total** | **1,775** | **291,807** |

At roughly 365 pulses a year, with the 90 day variant exclusion and per family cooldowns applied on top, a user would need to run the app for many years before a statement repeats, and would never see the same statement, question and response combination twice.

Section 11.1 of `CLARITY_LOGIC_ENGINE.md` gives the sizing targets, and phase 9 has brought every hot family in this volume inside them. persistence, quietDay, concentration, accumulation and rebalance each carry 63 to 71 statements per stage against a target band of 60 to 100. Five of the six families that were below the hot line are the size 11.1 asks of them and are deliberately not grown: throughput, spread, switching, burst and queueDrain together fire 70 times a year across eleven simulated lives, where rebalance alone fires 100. **The sixth was freshStart and it is no longer below the line.** The ninth measurement reads it at 42 firings against 26 at the eighth, because bounding the Pulse repeat filter to yesterday released a family that fires on a first week and on a return, which are the two moments a stale last generated family used to sit on. Its bench is ten statements against 11.1's sixty, and that debt is recorded in `HotFamilies` and in `docs/CORPUS_ANCHORS.md` rather than paid here, because authoring is not that pass's to do.

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
