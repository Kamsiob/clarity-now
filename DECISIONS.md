# Decisions

Why this project is the way it is.

Every other document in this repository says what the app does. This one says why it
does that and not the obvious alternative, so that a session arriving with no context
can tell a settled question from an open one, and can see the argument rather than
only the instruction. An unrecorded decision gets re-proposed every few months, and a
session with no record of the losing option will pick it, reasonably, and be wrong.

## What belongs here

- A choice between two defensible options, with the losing option named
- A conflict between two documents, and which one won
- A rule adopted that constrains later work
- A thing deliberately not built. These are the entries that earn their keep. A no
  with no reason attached does not survive contact with the next session
- A component built by hand rather than taken from the platform, per the platform
  first rule in Addendum 01 Step 3. Those go in the standing register at the foot of
  this file, one line each, rather than in a dated entry
- A question the owner has to answer, recorded as open, with the recommendation
  stated and not taken

## What does not belong here

- What a thing is, or how it behaves. That is MASTER_BUILD_PROMPT.md and design-v3.md
- The exact wording of any sentence the app says about a person's own data. That is a
  corpus file, and there is no second path
- Progress. What is built lives in `docs/BUILD_STATE.md`, what remains lives on the
  issue board with acceptance criteria
- A decision that was never in doubt. Writing those down buries the ones that were

## Where this sits in the authority order

Nowhere. This file records reasoning; it has no authority over anything. The order in
CLAUDE.md is unchanged: design-v3.md wins on anything visual or interactive,
MASTER_BUILD_PROMPT.md wins on behavior and data, CLARITY_LOGIC_ENGINE.md wins inside
`domain.engine` and `domain.guidance`, and the corpora win on the exact wording of any
sentence.

If an entry here disagrees with one of those documents, the document is right and the
entry is stale. Fix the entry. If the entry is what the owner actually decided and the
document never caught up, change the document and write a new entry saying so. An
entry is a record of a decision on a date, not a standing instruction, and a session
must never build from this file alone.

## How an entry is written

**Newest first.** The top of the file is the current state of the argument. The one
exception is the standing register at the foot, which is append only and ordered by
the date each row was added, because a dozen one line records spread through a dozen
dated entries would bury everything else in the file.

Each entry carries:

| field | what goes in it |
|---|---|
| heading | the date written out, then a short name for the decision |
| **Decided** | what is now true, in the fewest sentences that leave no ambiguity |
| **Why** | the reasoning and the evidence behind it, long enough to be checkable |
| **Considered and rejected** | the options that lost, each with its reason |
| **Revisit if** | what would have to change for the decision to be reopened |

The last field is not optional and is not decoration. A decision with no revisit
condition is a rule, and a rule belongs in a specification document rather than here.
Where an entry settles several things at once, each section carries its own revisit
line.

**Nothing is deleted.** A reversed decision gets a new entry that names the old one.
The old entry stays where it is, wrong, dated, and useful.

---

## August 27, 2026: the twelve open choices in the Pulse

Phase 6, issue #4. `MASTER_BUILD_PROMPT.md` 11.3, 11.6, 12.1, 13.4 and 14b.4,
`design-v3.md` 3.3, 8.2, 8.4, 10.1, 11 and 13, `CLARITY_LOGIC_ENGINE.md` 6.1 and 6.2,
and `CORPUS_1_PULSE.md`. Twelve things those documents leave to the builder, settled
here under `design-v3.md` 15, and one that is not a builder's to settle and is recorded
as open.

**This is the first surface in the app that shows a sentence the engine wrote about a
person's own life.** Everything before it was a fixed label or a readout of a number the
app had just counted. That changes what a wrong decision costs: a screen that assembles
one string for one edge case is not a visible defect, it is a false claim about
somebody's week with nothing on the screen pointing back at the cause. Several entries
below are therefore about a shape that makes the wrong thing unreachable rather than
about a check somebody has to remember to write.

### The response pills are stacked, and never set side by side

**Decided.** The two options, or three for `quietDay`, sit in a vertical stack, identical
in width and in treatment, with one composable called once per option and no parameter
that could make one of them louder. Implemented in `ui/pulse/PulseSurface.kt` and
`ui/pulse/PulseResponsePill.kt`, and stated in `design-v3.md` 11.

**Why.** `design-v3.md` 11 says "response pills" and gives no arrangement, so section 15
applies. Side by side is the statistically common answer and it costs two things.
It puts one option on the left, and in a left to right reading order the left position
reads as the recommendation, which `CLARITY_LOGIC_ENGINE.md` 6.1 forbids the interface
from making: both responses must feel equally valid read out of context. And it does not
survive `quietDay`, the one family with three options, so the surface would rearrange
itself between families for a reason the person cannot see. One stacked layout answers
both cases in one shape.

**Considered and rejected: side by side for two and stacked for three**, which is the
arrangement that looks best in each case taken alone and is the worst of the three,
because it makes the layout a signal about which family fired. **Also rejected: a primary
and a secondary treatment**, which is the same prohibition stated in color instead of in
position.

**Revisit if** a three option stack turns out to push the observation off the top of the
room at a large font scale. The first move then is the room, not the arrangement.

### The room is a fixed band rather than a sheet that wraps its content

**Decided.** The Pulse opens at 520dp, collapsing to no less than 320dp, and both phases
scroll inside whatever height they are given. Implemented in `ui/pulse/PulseRoute.kt`.

**Why.** `design-v3.md` gives the Pulse no height. A sheet that wraps its content is the
obvious answer and would be about 300dp for the question and about 250dp for ambient
mode, which makes the amber night a panel rather than a room, and makes the surface
resize under the settle in 8.2 item 11. A little over half a phone is enough for a serif
observation to sit in space with its question beneath it, and short of the full screen
that would make a drag down feel like leaving the app rather than closing a sheet. The
band rather than a single value is what carries landscape, where a fixed 520dp is taller
than the device, and a 200 percent font scale, where the content scrolls inside the room
instead of growing it.

**Considered and rejected: full screen**, which is what the Focus surface is and is right
there, because Focus is a place a person stays for twenty five minutes and this is a
question that takes fifteen seconds.

**Revisit if** the device check shows the ambient phase looking empty in the room at
520dp. The answer then is what ambient mode holds, not the height.

### The amber tint reaches under half the height and stops

**Decided.** The dawn and evening tints are a vertical gradient at 55 percent from one
edge, reaching 45 percent of the height and going to nothing. In calm mode the tint is
not transformed, it is not drawn at all. Implemented in `ui/pulse/PulseBackdrop.kt` and
consistent with `design-v3.md` 3.3 and 16.7.

**Why.** 3.3 says the shift "must be felt rather than noticed", which is the whole
specification of its strength. The obvious reading of blending a whisper into one edge is
a gradient across the whole surface, which is what a hero background looks like in 2026
and which would make the time of day the loudest thing on a screen whose entire content
is one sentence. A tint that stops before the midpoint is a room lit from one side rather
than a colored screen.

**Considered and rejected: a tint that animates as the hour moves.** Nothing here counts
down to the next boundary. A background that redrew itself while somebody was reading is
the exact opposite of felt rather than noticed.

**Revisit if** the two tints are indistinguishable from midday on the device at low
brightness, which is the failure mode of choosing quiet.

### The chip's second signal is the destination's name, not a status word

**Decided.** The Pulse chip reads `Pulse` at rest and `Today's Pulse` when a Pulse is
ready and unanswered, alongside the 6dp `warnAmber` dot that `design-v3.md` 10.1
specifies. Implemented in `ui/areas/PulseChip.kt`, with both strings in `strings.xml`.

**Why.** `design-v3.md` 13 requires a second signal because color is never the only one,
and leaves the wording open. The obvious answers are a status word appended to the label,
`Pulse ready`, or a count, `Pulse 1`, which is the shape the inbox chip already uses.
Both report on the person rather than name the destination, and a count of one is a
number nobody needs. `MASTER_BUILD_PROMPT.md` 13.5 already calls this surface
`Today's Pulse` for the app shortcut that opens it, so the two entry points into one
surface say the same words, and the label reads as an invitation rather than as a notice
that something is outstanding.

**Considered and rejected: a content description on the dot.** Any wording for it would
be a sentence about an unanswered question, and 11.6 says not answering is never chased,
never counted and never mentioned. The changed label is the whole of what is said.

**Revisit if** user testing shows people do not read the chip label at all, in which case
the second signal has to be a shape rather than a word.

### The history list carries absolute dates, including today's

**Decided.** Every row in the Pulse history is a date. There is no `Today` and no
`Yesterday`. Implemented in `ui/pulse/PulseHistoryPage.kt`.

**Why.** The Trail does relative labels for the two newest days, and inheriting that here
was the default. The two lists are read for different things: the Trail is read for
recency, and this is read for pattern, down one column. A list whose first two rows are
labeled differently from the rest breaks the column a person is scanning.

**Considered and rejected: relative labels everywhere**, which would have been consistent
with the Trail and would have made the page a list of phrases instead of a list of dates.

**Revisit if** the Trail and the Pulse history ever appear on one screen, where two date
conventions side by side would be worse than either.

### The acknowledgment is held for 1,100ms, and the hold does not shorten under reduce motion

**Decided.** After an answer the pill fills over 220ms, the acknowledgment fades in over
400ms after a 250ms hold per `design-v3.md` 8.2 item 10, it is held for 1,100ms, and the
room then settles into ambient mode. Under reduce motion the two fades shorten to 150ms
and the 1,100ms hold is unchanged. Implemented in `ui/pulse/PulseSurface.kt`.

**Why.** 8.2 gives every number in that sequence except how long the acknowledgment
stands. The corpus calls these lines "shown briefly" and they are short: a second and a
bit is one unhurried reading of a five word sentence and is short of the point where a
person starts wondering whether the screen has stuck. The hold survives reduce motion for
the reason `design-v3.md` 8.4 keeps the empty state's delay: **a hold is not motion**, and
shortening it would remove reading time from the person most likely to need it rather
than removing an animation.

**Considered and rejected: dismissing on the next tap instead of on a timer**, which
would hand the person a control whose only function is to skip a sentence the app just
wrote for them.

**Revisit if** the corpus grows acknowledgment lines materially longer than the ones
authored today, in which case the hold has to be a function of length rather than a
constant.

### The reminder is a chain of one hop at a time, not a periodic request

**Decided.** The daily reminder is a `OneTimeWorkRequest` under a unique name, and each
run arms the next one by recomputing the target hour against the clock and its zone.
Implemented in `work/PulseReminderScheduler.kt` and `work/PulseReminderSchedule.kt`.

**Why.** A `PeriodicWorkRequest` with a twenty four hour period is the obvious
implementation and it loses on the one thing this feature is, which is a time. A period
is a fixed duration measured from the previous run and not a wall clock hour: the morning
the clocks change it fires an hour early or an hour late and stays there until something
reschedules it, and WorkManager's batching accumulates over months. Recomputing on every
hop makes daylight saving a calendar question the scheduling code never sees, and absorbs
a timezone change on the next hop. For the audience `MASTER_BUILD_PROMPT.md` 14b
describes, a reminder that arrives at the wrong hour teaches them the app's timing cannot
be trusted, and they switch it off.

**Considered and rejected: an exact alarm**, which needs `SCHEDULE_EXACT_ALARM` and is
out of scope under section 18. WorkManager may run this late and Doze can hold it on a
sleeping phone. A reminder that arrives a little late is the honest version of what this
app can promise.

**Revisit if** the device check shows the reminder arriving materially late in ordinary
use, which is the one thing the unit tests cannot see.

### The permission prompt fires on a transition, and never on the first composition

**Decided.** `NotificationPermissionOnReminderEnabled` remembers the value the switch
first showed and asks for `POST_NOTIFICATIONS` only when that value goes from false to
true. Implemented in `notifications/NotificationPermission.kt`.

**Why.** `pulseRemindersEnabled` defaults to true, per `MASTER_BUILD_PROMPT.md` 14b.12,
so the obvious `LaunchedEffect` keyed on the value would prompt every person who has
never touched the switch, the moment they first opened Settings. That is not quite launch
and it is close enough to be the same defect: a permission prompt for something they did
not just do. It is also derived from the setting changing rather than announced by
whoever changed it, so there is no `onCheckedChange` anybody has to remember to wire, and
the moment cannot drift into the wrong place in a later phase.

**Considered and rejected: prompting the first time the reminder would actually post**,
which happens with the app closed and cannot show a dialog at all.

**Revisit if** the default for `pulseRemindersEnabled` ever moves to false, at which
point the first composition and the transition are the same event and this can be
simplified.

### The reminder cannot be posted without a token read from the day's entry

**Decided.** `PulseReminderPoster.post` takes a `PulseReminderDue`, whose constructor is
private and whose only factory returns null unless the day's entry exists and is
unanswered. Implemented in `notifications/PulseReminderPoster.kt`.

**Why.** `MASTER_BUILD_PROMPT.md` 12.1 and issue #4 both say the reminder posts only if
that day's entry exists and is unanswered, never when IDLE. Written as an `if` at the
call site that is one line, it looks right without it, and it is exactly the kind of
check a later refactor drops. A notification on a day the engine chose to stay silent
turns designed silence into a broken promise, which is the most expensive defect this
phase could ship. Made a type, the rule is not something anybody can forget: there is no
way to reach the poster without having read the entry out of the log. It is the same
shape `NotificationMoment` uses to make "never ask at launch" structural.

**Considered and rejected: generating a Pulse inside the worker** so that there would
always be something to post, which would be the app speaking to somebody who did not open
it, forbidden by name in 13.4.

**Revisit if** a second thing ever needs to post on this channel, which is the point at
which the token stops being free.

### The rhythm row has three marks and today carries no ring

**Decided.** Filled amber for answered, a hollow ring for generated but unanswered, and a
smaller half strength mark for a silent day. No fourth state, no ring on today, no
caption, and a spoken description that names the element rather than tallying it.
Implemented in `ui/pulse/PulseRhythmRow.kt`.

**Why.** `MASTER_BUILD_PROMPT.md` 12.1 names the three and stops. Today's ring is
Momentum's treatment, 12.2, and importing it here would add a fourth mark to a row whose
whole safety property is that it has three: the mark takes a `PulseDayState`, so a fourth
kind of mark would need a fourth day state and the log cannot produce one. The row is
also the single most likely place in the app to reintroduce a streak by accident, so each
mark is drawn from its own day and knows nothing about the day beside it, and a gap is a
fainter mark and nothing else. The silent mark is drawn smaller as well as fainter, so
the three differ in form and not only in opacity, and it is held at 3.0 to one against
`deepBlack` per `design-v3.md` 16.7, because a mark below that stops being quiet and
starts being absent, and a fortnight of silence would then look like a broken row rather
than a calm one.

**Considered and rejected: a count beneath the row.** `design-v3.md` 14 forbids a
consecutive count and section 11 gives the row no caption, so a spoken figure would hand
a screen reader user a claim the sighted reader cannot see.

**Revisit if** the three marks turn out to be hard to tell apart on the device at arm's
length, in which case the silent mark's size moves before its opacity does.

### The corpus ships as an asset rather than as a Kotlin constant

**Decided.** The three committed corpus files are read at runtime through a `CorpusSource`
seam, which on the phone reads `assets/corpus/` and in a test reads the committed file off
disk. Implemented in `domain/pulse/PulseCoordinator.kt` and `ClarityApp.kt`.

**Why.** CLAUDE.md's authority order gives the corpora the last word on the wording of
every sentence. A copy of a corpus embedded in Kotlin is a second corpus, and a second
corpus drifts silently: the file an author edits stops being the file the app reads, and
nothing fails when they disagree. The seam rather than a direct call is what keeps
`ClarityCatalog.build` taking text, which is what lets phase 9 judge a corpus edit against
the real rules from a string.

**Considered and rejected: parsing at build time into a generated Kotlin source**, which
is faster on the phone and puts a generated artifact between the author and the app,
which is the drift this decision exists to prevent.

**Revisit if** catalog build time is measurable on the first foreground of the day on the
device. The answer then is when it is built, not where it is read from.

### The Pulse generates after the presence marker, not before it

**Decided.** On the first foreground of a process, `ClarityApp` writes `APP_OPENED` and
then runs the generation sequence, in that order, in one call. Implemented in
`ClarityApp.kt`.

**Why.** 14b.4 suppresses the Pulse for the first two days after a return, and a return
is detected by comparing today's `APP_OPENED` against the newest one before it. Generating
first would find no return on the one day it matters, and a person coming back after a
fortnight would be met by exactly the observation about their absence that the whole of
14b.4 exists to prevent. The ordering is not a preference, it is the difference between
the rule holding and the rule reading correctly while doing nothing.

**Considered and rejected: running the two in parallel**, which is faster by a few
milliseconds on a path nobody is waiting on and reintroduces the race the ordering
removes.

**Revisit if** the presence marker ever moves out of the foreground callback, at which
point this ordering has to move with it.

### Open, and the owner's rather than a builder's: what WorkManager adds to the merged manifest

**Not decided.** This phase is the first thing in the app that really uses WorkManager,
and the question has been open since phase 2. It is recorded here rather than settled.

**What is actually true today.** The merged manifest, debug and release, declares six
permissions: `POST_NOTIFICATIONS`, `POST_PROMOTED_NOTIFICATIONS`, `VIBRATE`,
`FOREGROUND_SERVICE`, `WAKE_LOCK` and `RECEIVE_BOOT_COMPLETED`. The last three arrive from
work-runtime and none of them is a network permission, so `verifyNoInternetPermission`
passes on both variants and the privacy claim a person can check in Android settings is
intact. `ACCESS_NETWORK_STATE`, which work-runtime also declares, is removed with
`tools:node="remove"` and does not appear in either merged manifest.

**Why it is still open.** `MASTER_BUILD_PROMPT.md` 18 puts every permission beyond
notifications out of scope for v1, and three of the six are beyond notifications. Section
3 and 12.1 both require WorkManager for this reminder, and 12 requires it again for the
widget refresh, so the only ways to close the gap are to accept the three permissions in
writing or to build the reminder on something else. This phase uses WorkManager because
the specification tells it to, and reports what the merged manifest gains.

**The removal was checked against the library rather than assumed.** In work-runtime
2.11.2, `WorkConstraintsTracker.track` filters its controllers by
`ConstraintController.hasConstraint` for the spec it is given, so a request with no
constraints never reaches the network tracker and never starts it, and everything that
does touch `ConnectivityManager` already catches `SecurityException`. A constraint free
daily reminder is therefore safe without the permission. **Adding any constraint to any
work request in this app means putting `ACCESS_NETWORK_STATE` back**, which changes what
the privacy policy invites people to verify, so it is the owner's call and not a build
fix. The manifest says so at the point of the removal.

**The recommendation, stated and not taken.** Amend section 18 to name the three
WorkManager permissions explicitly rather than leave a document saying no permission
beyond notifications while the app ships three, since a specification that is quietly
untrue about permissions is worse than one that says which three and why.

**Revisit at** phase 11, where the privacy sheet and the permission card are built and
this list has to be shown to a person, and again at phase 12, where the widget refresh
makes WorkManager unavoidable a second time.

---

## August 27, 2026: the fourteen open choices in the engine skeleton and the simulator

Phase 5, issue #3. `CLARITY_LOGIC_ENGINE.md` 2 to 8, 11.1, 12 and 14,
`MASTER_BUILD_PROMPT.md` 9 and 11, and CLAUDE.md's authority order. Thirteen things those
documents leave to the builder, settled here under `design-v3.md` 15, and one reading that
is a finding rather than a choice and is recorded as open.

**Every one of these is invisible.** Nothing in this entry changes a pixel. A wrong
decision here becomes a false sentence about somebody's life some months later, with
nothing on the screen pointing back at the cause, which is why the entry is long and why
the losing option is named in every case.

### A family with a missing fact gets no rule at all

**Decided.** Nine families and three single stages have authored corpus language and no
rule, because the fact their trigger names is not declared in `CLARITY_LOGIC_ENGINE.md`
3.1. Each is recorded in code as a gap carrying the missing fact and the corpus line that
needs it, and a catalog test asserts that every family in the corpus either has a rule or
is on that list. Stated at the point of ambiguity in 6.1 and in `MASTER_BUILD_PROMPT.md`
11.7.

**Why.** Every one of them had an approximation available that is nearly the right shape.
Window active days in place of a run of consecutive quiet days. The window's swap count in
place of an area's. Days since an area's last event in place of the dormancy it returned
from. A per week area count that nothing keeps. Each would fire the family on a shape it
does not describe, and the sentence that came out would be arithmetic nobody could fault
and a claim about a person's week that was not true. That is the prime directive in
section 1, and this is the phase where obeying it costs something, which is the only kind
of obedience that counts.

**Considered and rejected: the near enough criterion**, which is what a builder under
schedule pressure writes and what nobody would ever catch, because the output is fluent
and the number is real. **Also rejected: deleting the corpus language**, which throws away
authored, reviewed lines to make a test pass and loses the record of what the family was
for. **Also rejected: a comment.** A comment is not read by a test, and the failure it
guards against is a later session wiring the family up with the approximation.

**Revisit if** phase 9 or a later phase adds the fact. The fix is a fact in 3.1 and a
query behind it, never a criterion built out of the facts that are already there.

### The escalation ladder drops the pair rather than raising the stage

**Decided.** When `FiringHistory` says a `(family, subject)` pair last spoke at a higher
stage than the one the magnitude now supports, the pair is dropped and the family says
nothing until the magnitude catches up. It is never raised to the stage last shown.
Recorded in 7.3.

**Why.** 7.3 requires that a pair never show a lower stage while the condition stayed
continuously true, and the obvious implementation of that is to render the stage last
shown. It is wrong because a stage is authored around a magnitude: stage 2 of
`persistence` is written for six to thirteen days, so showing it for an item whose age
just reset to three says `going into its second week` about a three day old item. The rule
exists to prevent whiplash, and the obvious implementation produces a false sentence
instead. **A missing sentence costs a day of silence. A false one costs the credibility of
every sentence after it.**

**Considered and rejected: rendering the higher stage anyway**, above. **Also rejected:
tracking the condition's continuity explicitly**, with an event or a field recording when
a ladder lapsed. That is engine state, it would have to live somewhere, and the only place
it could live and still merge is the log, which means a new event type carrying a fact
nothing else needs. The bound used instead is the family's own cooldown plus the window it
describes: inside that the family was either speaking or forbidden from speaking, and
beyond it the engine has no evidence and the ladder starts again.

**Revisit if** the simulator dump shows a family going quiet for long stretches after a
single high stage firing. The reading to look at is the family's own share of a persona's
year, not the count of dropped pairs.

### Editorial notability is specificity at three or more

**Decided.** The editorial register is offered only to a rule with three or more criteria,
and only for the Report, and only while the two lead budget in 7.4 is unspent.

**Why.** 7.4 caps the report at two editorial leads and `CORPUS_2_REPORT.md` says the
register is reserved for leads that have earned it with a genuinely notable fact. That is
a condition on the lead, and nothing in 3.1 or section 4 carries a notability flag, so
either something invents one or the realizer uses a measure the engine already computes.
Specificity is that measure: a rule requiring four things at once describes a narrower
situation than one requiring two, which is the entire mechanism of section 5 and the same
thing an editor means by a fact worth writing up. Three is the threshold because two is
the ordinary shape of a rule in this catalog, a condition and the floor that keeps it
honest, so three is where a rule stops describing a number and starts describing a
situation.

**Considered and rejected: an authored `notable` flag on the rule.** It is the obvious
answer, and it would be a second judgment about the same thing, drifting away from
specificity the first time somebody added a criterion. `CLARITY_LOGIC_ENGINE.md` says
specificity is never authored, for exactly that reason, and a notability flag is
specificity authored under another name. **Also rejected: letting the corpus tag decide.**
The register tag says which voice a line is in, not whether the fact behind it earned that
voice, and the fact is not knowable when the line is written.

**Revisit if** phase 9 finds editorial leads landing on facts that do not carry them. The
first move is the threshold, not a new flag.

### Two families 7.4 qualifies by a stage they do not have become two rules each

**Decided.** `persistentItem` and `switchingBehavior` are single stage families in
`CORPUS_2_REPORT.md`, and 7.4 marks them unflattering at stages the corpus does not have.
Each gets two rules pointing at the same stage, split at the magnitude the corpus already
states for the matching Pulse ladder: fourteen days for `persistentItem`, two swaps for
`switchingBehavior`. Only the higher rule is unflattering.

**Why.** 7.4 wrote the qualification deliberately, which rules out marking the whole
family; and three neutral agent lines are authored for the switching family, which rules
out marking none of it, because they would be unreachable. The qualification is really
about magnitude rather than about a stage number, and a rule is the object that carries a
magnitude in this design. The split points are not invented: both are already written down
in `CORPUS_1_PULSE.md` as the start of the corresponding Pulse stage.

**Considered and rejected: adding stages to the two corpus families** so the stage numbers
in 7.4 resolve. That is a corpus edit, phase 9 owns the corpus, and it would mean
authoring lines to satisfy a cross reference rather than because a person needed them.

**Revisit if** phase 9 gives either family real stages. The two rules then collapse back
into stage rules and this entry becomes history.

### A tie for the busiest day resolves to the earliest, and the family carries a floor

**Decided.** `WindowFacts.busiestDayKey` resolves a tie to the earliest day. Separately,
the family that names the day requires the day's count to be a real share of the window's
events. Recorded in 3.1.

**Why.** 3.1 makes `dominantAreaId` null on a tie and says nothing about this field, so the
tie had to go somewhere, and the earliest day is the day the peak was first reached. That
alone is not enough, and the second half is the part that matters: `Tuesday carried the
week` is false on a three way tie whichever day wins, so the honest guard is not the tie
break but the floor, which is the same guard every share based rule in the catalog
carries.

**Considered and rejected: null on a tie**, matching `dominantAreaId`. It reads as
consistent and it throws away a usable fact: a two way tie in a week of forty events is
still a real peak, and the floor is what makes it safe to name. **Also rejected: the
latest day**, which has no argument for it beyond recency.

**Revisit if** a corpus line ever wants to name the busiest day without a share claim
attached. It would need a different fact, not a different tie break.

### Response pairs live on the stage, and are not flattened onto the family

**Decided.** `PhrasingFamily` carries no flat response list. Each stage carries its
authored pairs, and the realizer chooses one pair and never splits it across stages.
Recorded in 7.1.

**Why.** 7.1 declares responses as a flat `List<ResponseOption>` on the family.
`CORPUS_1_PULSE.md` authors them as pairs, six or seven per stage, and the equal validity
test in 11.3 is a test on a pair: read both aloud with no context, and if one sounds like
the answer a good person gives, rewrite both. Flattening loses the pairing, which is the
only thing that makes that test mean anything, and a runtime that could combine the first
half of one pair with the second half of another would produce a question nobody reviewed.
CLAUDE.md's authority order gives the corpus the last word on the shape of a sentence, and
a response pair is a sentence shape.

**Considered and rejected: flattening and re-pairing by index**, which works until a stage
has an odd number of options and then silently pairs the wrong two.

**Revisit if** 7.1 is amended. This is a case where the corpus won over the engine
document, which is what the authority order says should happen, and the engine document
should probably be corrected rather than this reversed.

### The corpus violations that exist today are a recorded list, not a disabled check

**Decided.** 7.7 forbids a fragment appearing in two families and a rhetorical construction
appearing in more than two. The corpus breaks both, in six places and two shapes. The
checks are on, and the exact violations are enumerated in code with the keys and the
reason each is tolerated. A **new** violation fails the build.

**Why.** There were three options and only one leaves the build honest. Deleting the checks
loses them permanently, because nobody writes a check for a rule they have already decided
to break. Ignoring them until phase 9 means a check that is off, which is a check that
finds nothing on the day somebody adds a seventh violation. Writing down exactly what is
wrong keeps the check running, hands phase 9 a list rather than a rediscovery, and makes
every entry a debt with a name on it.

**Considered and rejected: fixing the corpus now.** Phase 9 owns the corpus, authors in
batches of forty with anchor lines, and presents them for approval. Editing six lines
outside that process is exactly the drift the process exists to prevent.

**Revisit if** the list stops shrinking. Each phase 9 batch that touches one of those
families should leave it shorter, and a list that is the same size at the end of phase 9
means nobody read it.

### Which families count as hot is measured, not authored

**Decided.** The simulator's stage coverage check calls a family hot when it fired forty or
more times across the run, which is 11.1's own definition of the tier, and reads its stages
from the rules that actually fired.

**Why.** 11.1 defines the tiers by expected firing frequency, so the simulator already
holds the only honest answer to which families are hot. A hand written list would be a
second place where a judgment about frequency lives, it would drift the first time a rule
changed, and it would let a family be called hot without ever having fired, which is the
one state the check exists to catch.

**Considered and rejected: the fifteen family list 11.1 estimates.** It is an estimate in a
document, and the simulator is the instrument that replaces estimates in this project. The
run produced twenty nine families at forty or more firings, which is not fifteen, and the
gap is itself a reading phase 9 should look at.

**Revisit if** the persona set changes enough that firing counts stop meaning anything
about a real year. The threshold is 11.1's, not the simulator's.

### The validator masks the person's own words before it reads a sentence

**Decided.** Checks 7, 8 and 10 read the rendered sentence with area names, item titles and
quoted response labels masked out. Check 9, the length check, reads the sentence as it will
appear. Recorded in section 8.

**Why.** An area is named by the person who made it. Somebody may spell a word the way they
were taught, put an exclamation mark in an item title, or write one in a language the
banned vocabulary list cannot spell. Vetoing on their string silences the engine over
somebody's own vocabulary, and the app already shows that exact string on every other
screen, so the veto would not even hide it. **The words the app chose are the words the app
is answerable for.** Length is the exception because a long name really does make a long
headline, and the reader sees the whole line.

**Considered and rejected: reading the whole rendered string.** It is the obvious
implementation and it fails on the first person who names an area in a language with
accents, which the non-ASCII rule would then read as a validator failure rather than as a
name. **Also rejected: sanitizing names on input**, which is the app editing what somebody
wrote about their own life.

**Revisit if** a masked check is ever found to have let something through that a reader
would fault the app for. The answer then is a narrower mask, not the whole string.

### A deferred simulator check runs, measures and reports, and only its verdict is deferred

**Decided.** Six of the ten checks in section 12 cannot pass in phase 5. Each carries a
deferral naming the date and the issue that lifts it, runs on every simulation, prints the
number it measured and prints its failures as loudly as an enforced check. What deferral
changes is only whether the build goes red.

**Why.** The obvious way to handle a check that cannot pass yet is to skip it, and a skipped
check produces no number. The whole reason the simulator is built before the corpus is so
that the growing can be aimed, and a gate that reports only on failure tells an author
nothing about how close the corpus is to the target it is being grown toward. A deferral
with a date and an issue is also the difference between a decision and a thing nobody comes
back to.

**Considered and rejected: an assumption or a commented out assertion**, which is a skip
with an apology attached. **Also rejected: relaxing the thresholds to what phase 5 can
meet**, which would leave the build green and the target lost, and is the failure mode that
turns a specification into whatever was convenient.

**Revisit if** phase 9 lifts a deferral. The deferral is deleted, not the check.

### Debug only is verified against the resolved source sets, not assumed from the layout

**Decided.** A Gradle task reads the source directories Gradle resolved for each variant and
fails unless the devtools package is present under a debug directory, absent from every
release one, and named by no file a release build compiles. It runs inside `verifyClarity`
and blocks `assembleRelease`.

**Why.** Putting a file in `src/debug` is the mechanism, not the verification, and the
failure mode is silent: the day somebody moves one simulator class into `src/main` so a
screen can reach it, nothing breaks and the release build quietly grows eleven synthetic
personas, a year of generated logs and a copy of the check suite. The first of the three
conditions is the one usually missing. A check that only looked for the package where it
must not be would pass on a repository where the simulator had been deleted.

**Considered and rejected: trusting the source set layout**, which is what everybody does
and what nobody notices failing. **Also rejected: a ProGuard rule**, which would strip the
code from the release binary and leave the source set wrong, so the next person would
inherit the mistake with the evidence removed.

**Revisit if** the simulator is ever wanted in a release build for a debug menu. That is a
product decision, and it would start with what a person is shown rather than with this task.

### The engine takes a zone at construction and keeps the signature 2.2 specifies

**Decided.** `ClarityEngine` and `ClarityValidator` take a `ZoneId` as a constructor
parameter. `observe(facts, history, purpose)` is unchanged.

**Why.** Three things in the engine need to know which local day it is speaking on: 7.6
hashes the date key, 5.1 buckets it, and every exclusion window measures against it. The
fact set carries the window as two instants and no zone. The ambient default is banned, and
`DomainPurityTest` fails the build on it, because `ZoneId.systemDefault()` is the documented
cause of two Pulses in one day or none at all. So the zone the extractor counted with is
handed in once and the date key is derived from the window itself.

**Considered and rejected: a fourth parameter on `observe`.** 2.2 states that signature and
every caller would then carry a value that never varies between calls. **Also rejected:
putting the zone on the fact set**, which would make it a fact, and it is not a fact about
the person's week; it is a property of the device asking.

**Revisit if** a person can ever change the zone the app counts in. Today the extractor's
zone is the app's, and both sides of this read the same one.

### The validator is a seam the engine cannot skip

**Decided.** `ClarityEngine` holds a `CandidateValidator` with **no default value**. There is
no constructor that omits layer 5. A permissive implementation exists for testing layers 3
and 4 alone and its name says what it does.

**Why.** `MASTER_BUILD_PROMPT.md` 11.4 says never bypass the validator, not for a simple
sentence, not for an empty state, not to fix a bug. A rule that can only be obeyed is
better than a rule that has to be remembered, and a seam with no default is the only kind
that cannot be forgotten. The failure it prevents is silent in the worst way: everything
would still render, and nothing would be checked.

**Considered and rejected: a default validator on the constructor**, which is what
convenience asks for and what a future session would reach for at four in the afternoon.

**Revisit if** the wiring in phase 6 makes construction awkward. The answer then is a
factory that supplies the real validator, not a default.

### Open, and a finding rather than a builder's choice: three corpus totals have drifted

**Not decided, and deliberately not fixed.** `CLARITY_LOGIC_ENGINE.md` 11.1 states 162
Momentum and banner lines and 1,519 authored lines in total. Counted at catalog load, the
Momentum volume carries 146 and the combined figure is 1,503. `CORPUS_3_MOMENTUM.md` itself
claims 112 Momentum headlines in two places and carries 96. Inside `CORPUS_2_REPORT.md`,
section 1's prose says 176 headlines against its own totals table's 158, and section 3's
says 128 patterns against 111; there the tables are right and the prose is stale. Pulse
agrees at 620 and the Report volume's table agrees at 737.

**Why it is left alone.** Whether a file grows to match its total or a total is corrected to
match its file is a question about the corpus, and phase 9 is where the corpus is grown. A
builder quietly editing a number in a specification to match what the code counted is the
exact move CLAUDE.md's authority order exists to prevent. The catalog counts the keyed lines
at load and holds the readings, so the drift is visible rather than remembered, and the
count is recorded at the point of the claim in 11.1.

**The recommendation, stated and not taken:** correct the two stale prose figures in
`CORPUS_2_REPORT.md`, which are wrong against that file's own tables and cost nothing to
fix, and leave the Momentum shortfall to phase 9, which will close it by authoring rather
than by editing a number.

**Revisit if** phase 9 closes the gap, or if the owner decides the totals should be
generated rather than written.

### The readings the deferred gates produced, which is what phase 9 is judged against

Eleven personas, a full simulated year each, against the corpus and the rules as phase 5
left them: 92 rules across 78 families, 3,148 simulated opens, 451 reports, and 1,388
invocations for the persona who accepts every plan.

| gate, `CLARITY_LOGIC_ENGINE.md` 12 | target | phase 5 measured |
|---|---|---|
| no variant repeats inside ninety days | none | 7,384 repeats, the tightest after 1 day |
| Pulse silence | 8 to 25 percent of opened days | 43 to 98 percent per persona, 76 percent overall |
| layer 6 silence | at least 15 percent of reports | not measurable, layer 6 is phase 9b |
| no family over a fifth of a year's Pulses | 20 percent | 27 to 60 percent per persona |
| every stage of every hot family fires | all | 29 hot families, one gap: `accumulation` never reached stage 2 |
| no two consecutive report leads share a band | none | 715 collisions across 451 reports |
| no three consecutive parallel numeric clauses | none | 27 runs of three or more |

The four enforced checks pass: no banned word, dash, emoji or non-ASCII character in any of
the year's sentences; no sentence naming an area with no events in its window; no visible
slot marker; and nothing in the plan-accepting persona's year referencing a plan, a
commitment, an intention or a failure to act.

**Two of those numbers say something the phase did not expect, and phase 9 should read them
before authoring anything.** The silence figure is three to twelve times the target band,
and the cause is not only bench depth: of the eleven Pulse families in 6.1, **six ever
fired**. Two have no rule at all, and `throughput`, `burst` and `queueDrain` have rules that
no persona's year ever satisfied. Of the 2,383 silent days, 1,238 were days where something
qualified and every candidate was filtered by a cooldown or by yesterday's family, and
1,134 were days where nothing qualified at all. **A bigger corpus moves the first number and
not the second.** Growing benches will fix the repetition figure and the length band
collisions; it will not fix silence, and the honest reading is that silence needs more rules
and, behind three of them, more facts in 3.1.

**Revisit if** any of these is measured again after phase 9. The table above is a baseline
with a date on it, not a target, and the targets are the ones section 12 states.

---

## August 27, 2026: the eleven open choices in the focus surface

Phase 4, issues #2, #28, #29, #30, #32 and #49. Addendum 01 4e, 4f, 4g, Step 5 and 8d,
`MASTER_BUILD_PROMPT.md` 10, 13.4, 14b.5 and 14b.6, `design-v3.md` 3.3, 8.2, 9, 10.18,
11, 11.3, 11.4 and 16. Ten things those documents leave to the builder, settled here
under `design-v3.md` 15, and one that is not a builder's to settle and is recorded as
open.

**Two platform first checks, recorded so nobody redoes them.** The Live Update is
the platform's own promoted notification progress style, taken at step 1 of
`design-v3.md` 17.1, and the end of session confirm is the platform `AlertDialog`
themed, taken at step 2. Neither earns a register row, because the register at the foot
of this file records components built by hand. What the availability check turned out
to be is worth writing down, since `MASTER_BUILD_PROMPT.md` 3.3 asks for the platform
to be verified rather than trusted: on androidx core 1.19.0,
`NotificationManagerCompat.canPostPromotedNotifications` answers both halves of the
question in one call, returning false below API 36 without touching the platform and
asking the platform above it. The register's `Depleting focus ring` row moves to built
in the same commit as this entry.

### Calm mode takes the collapse and the expanding circle, and leaves the check

**Decided.** In calm mode and under reduce motion the completion bloom is the check
appearing over 150ms. The ring does not collapse, the soft circle does not expand, and
the check does not scale from 0.6. Implemented in `ui/focus/FocusRing.kt` and stated in
`design-v3.md` 16.6 item 9, which this entry did not have to change because the row was
already written.

**Why.** The row in 16.6 says crossfade to the completed state and the check still
appears, and the question this phase actually had to answer is what "still appears"
means when the thing it appears out of is gone. A check that faded in where a bloom
would have been is the answer, because the check is the information and the bloom is
the atmosphere. Calm mode removes motion, not information, 16.4, and this surface has
exactly one piece of information at that moment: the session finished.

**Considered and rejected: keeping a shortened bloom.** Compressing 700ms to 150ms
produces a fast flare, which is more startling than the slow one it replaces and is the
opposite of what a person switched calm mode on for. **Also rejected: holding the ring
in place at zero.** It reads as a session that has not finished yet, which is a false
statement rather than a quieter one.

**Revisit if** anyone reports that the completion screen arrives with no sense that
anything happened. The honest fix then is the tone `MASTER_BUILD_PROMPT.md` 10 already
asks for and phase 4 did not build, not a return of the motion.

### The Live Update track is one segment and at most one point

**Decided.** One undivided segment carrying the area color, with progress set to the
seconds **remaining** rather than the seconds spent, so the track depletes. One point,
only when the transition warning is on, at the five minute position, taking no color of
its own. Implemented in `notifications/FocusNotificationPoster.kt` and stated in
`design-v3.md` 11.4.

**Why.** `MASTER_BUILD_PROMPT.md` 14b.6 says a single track is the likely right answer
and asks for segments or points to be used only if they genuinely add clarity, and the
two halves come out differently. Dividing the track would invent a structure inside a
session that the session does not have: nothing happens at the boundary between one
segment and the next, so a person would be reading a shape that means nothing. The
single point is the opposite case, because 11.4 makes the track reaching that point the
state change the transition warning asks for. The point is the mechanism rather than
decoration, and with the warning off there is no point and no state change at all.

**Considered and rejected: a segment per five minutes**, which is the demonstration
every platform release note leads with. It would put four boundaries on a twenty five
minute session, none of which is an event, for an audience that reads a shape rather
than a number precisely because arithmetic is expensive. **Also rejected: coloring the
point**, which 11.4 forbids in the same sentence that allows the track one color.

**Revisit if** a later session finds a real interval inside a session worth marking. A
platform feature is not one.

### The completion screen has one wording, and is not told which ending it is drawing

**Decided.** The value the completion screen is handed carries the session id, the area
name, the item, the real duration in minutes and whether the item can still be
completed. **It carries no field recording whether the planned time ran out.**
Implemented in `ui/focus/FocusViewModel.kt` and stated in `design-v3.md` 11.

**Why.** Addendum 01 4e requires a session ended early to reach the same screen in the
same words with the same actions, and the obvious implementation passes a boolean and
branches on nothing today. The problem with that is not this phase, it is the next one:
a fact a screen holds is a fact a later edit can render, and the edit that renders it
will look reasonable at the time. Not handing the screen the fact is the only version
of the rule that cannot rot. The duration line is the real duration, rounded to nearest
the way `domain/query/TrailRow.kt` rounds it so the same session does not read as
fourteen on one surface and fifteen on the other, and it is never a comparison against
what was planned.

The one asymmetry that survives is the sixty second threshold in
`MASTER_BUILD_PROMPT.md` 10, and it is deliberately about the interface rather than
about the log: both endings write `FOCUS_ENDED_EARLY`, and what changes under a minute
is that there is no confirm before it and no completion screen after it, because forty
seconds is a mis-tap rather than a short session somebody meant to have.

**Considered and rejected: a second, gentler completion screen for a short session.**
It is what a well meaning designer would build and it is the exact failure 4e names.
Fourteen minutes is fourteen minutes, and a quieter acknowledgment is a verdict
delivered in a whisper.

**Revisit if** the owner decides a person should be able to see, on the screen, that a
session ran its full planned length. That is a change to 4e and an owner's call, and it
would start with the corpus rather than with this model.

### The ring is thin and the weight is spent on the tip

**Decided.** A 6dp stroke on the 240dp ring, a 10dp filled tip, and a 15dp radial
falloff behind the tip at 38 percent. Implemented in `ui/focus/FocusRing.kt` and stated
in `design-v3.md` 11.

**Why.** v3 fixed the diameter and said nothing about the stroke, and the statistically
common answer is a heavy ring at twelve to sixteen dp, which is what an activity ring
looks like and what `design-v3.md` 15.1 names under a ring closing toward a daily
target. A heavy ring reads as a target filling even when it is depleting, because the
weight is what makes it look like a container being filled. A fine line with a bright
point traveling it reads as time passing, and it is the treatment that belongs in a
room lit by a radial glow and eight specks.

**Considered and rejected: a heavy ring with a lighter track**, which would have read
as the thing 15.1 warns about with an extra step. **Also rejected: no tip at all.** The
tip is what makes the direction of travel legible at a glance, which is the whole claim
in `design-v3.md` 11.3.

**Revisit if** the arc turns out to be hard to read at arm's length on the device. The
first move then is the tip, not the stroke.

### Both actions on the end confirm carry the same weight

**Decided.** `End` and `Keep going` are two text buttons in the same treatment.
Neither is filled, neither is styled as the recommendation, and neither carries the
`warn` haptic. Implemented in `ui/focus/FocusSessionScreen.kt`.

**Why.** The obvious answer is a filled confirm and a quiet dismiss, and it fails in
both directions at once. Filling `Keep going` recommends carrying on, which is the app
having an opinion about how long somebody should work. Filling `End` and dressing it as
destructive says ending is a loss, which is what Addendum 01 4e exists to deny. The
`warn` haptic is scoped by `design-v3.md` 9 to a destructive confirmation arming, and
this confirmation guards against a mis-tap rather than against a loss.

**Considered and rejected: no confirm at all past sixty seconds**, which
`MASTER_BUILD_PROMPT.md` 10 rules out by naming the dialog and its two words.

**Revisit if** the two equal actions test as hard to tell apart. The answer then is the
order and the spacing, not the weight.

### The Contemplative primary uses one of 10.7's two forms everywhere

**Decided.** Every filled control on the Focus surface is the surface accent at 14
percent with a bright label. The translucent white pill at 9 percent, which
`design-v3.md` 10.7 offers beside it, is not used anywhere in this app. Implemented in
`ui/focus/FocusControls.kt`.

**Why.** 10.7 offers two forms and does not say when each applies, and section 11
settles it by specifying `Mark item complete` on the completion screen as being in the
accent. A control that changed its treatment between two screens of the same surface
would read as two different controls, and the End session pill and the Mark item
complete pill are one component in this app with one appearance. One separation device,
6.1: a background lightness shift, no border, and no shadow, because the Contemplative
world has none.

**Considered and rejected: the white pill for End and the accent for Mark item
complete**, on the argument that ending is a different kind of act from completing. It
is not: both are ordinary, both are what the screen is for, and giving one of them a
different container would say otherwise.

**Revisit if** a later Contemplative surface needs two filled controls on one screen at
different ranks. Pulse in phase 6 is the first test of that.

### The Focus chip is permanent, and never becomes a countdown

**Decided.** The Focus chip is present in the Areas header at all times, including when
no area has an active item, and it keeps its label while a session runs rather than
becoming a live timer. Implemented in `ui/areas/AreasScreen.kt`.

**Why.** `design-v3.md` 10.1 calls the chip permanent, and the chooser has an empty
state of its own reading `Nothing to focus on yet`, which is a sentence that explains
the situation where a missing chip would leave a person wondering where the feature
went. A disabled control is a question somebody has to answer; a permanent one that
leads to an explanation is an answer. As for the countdown, it would be one line of
code, and the area card two thumbs below already carries the live countdown beside the
name of the item the session is on. **Two surfaces reporting the same number in one
screenful is how a person learns to read neither**, and the chip's job is to be the way
back in, which it does under the same label either way.

**Considered and rejected: hiding or dimming the chip with no active item anywhere.**
`design-v3.md` 10.16 settles the same question for the inbox chip in the opposite
direction, and the difference is real: the inbox chip is a count, so with nothing in
the inbox it has nothing to say, while the Focus chip is a door.

**Revisit if** the quick settings tile in phase 12 makes a second countdown surface
unavoidable anyway.

### Nothing stores whether the notification permission has been asked for

**Decided.** The request is made at one moment, the first tick of a session a person
just started, and no flag records that it happened. Implemented in
`notifications/NotificationPermission.kt`.

**Why.** The obvious implementation keeps a "we have asked" key so the prompt appears
exactly once, and it does not survive the check `design-v3.md` 15 asks for. From
Android 13 the platform already caps the prompt at two appearances for the life of the
install and denies silently afterwards, so the key would buy one fewer dialog in
exchange for a value in DataStore that has to be erased, exported and reasoned about.
It would also be wrong in the one case where asking again is right, which is a person
who granted the permission and later revoked it.

The moment is derived rather than announced, which is the part worth keeping: a session
whose elapsed time is under five seconds was started here, and a session restored after
a process death was not. Written as "the session id went from null to something" it
would fire at launch, because on a cold start the restore is asynchronous and the id
does go from null to something. `MASTER_BUILD_PROMPT.md` 13.4 forbids asking at launch
in the same sentence that asks for a contextual request.

**Considered and rejected: asking on the chooser rather than on the session.** It is
earlier and therefore feels safer, and it asks people who opened the chooser and
changed their mind.

**Revisit if** the platform changes its own cap, which is the only thing this reasoning
rests on.

### The completion notification takes the phone's own sound

**Decided.** The Focus channel is created at default importance with no call to
`setSound`, so a completion arriving while the app is elsewhere makes the sound the
person already chose for notifications. No tone is bundled. Implemented in
`notifications/ClarityNotificationChannels.kt`.

**Why.** `MASTER_BUILD_PROMPT.md` 13.4 asks for a gentle sound and the obvious answer
is to ship a soft chime. It loses for a specific reason rather than a stylistic one: a
person who has already set their phone to a quiet notification sound has told the
device what gentle means to them, and an app supplied tone overrides that decision on
the one notification this app posts that makes any sound at all. All three channels are
created together at process start, because a channel's importance and sound are frozen
for an install the moment it is created, and a channel created late by whichever code
path needed it is a channel whose settings were chosen by that code path.

**Considered and rejected: a bundled chime**, and **low importance for the completion
channel**, which would have made it silent and would have made the one moment this app
is allowed to interrupt for arrive as nothing.

**Revisit if** a person reports the completion sound is jarring, at which point the
answer is their own system setting and this decision is what makes that true.

### The chooser names an area in `textDim`, not in the area color

**Decided.** On the Focus chooser the area name is `textDim` and the 7dp dot beside it
carries the identity at full saturation. Implemented in `ui/focus/FocusChooserScreen.kt`.

**Why.** `design-v3.md` 3.4 permits an area label in the accent as one of four forms an
area color may take, and the helper that computes that variant was measured against a
Daylight card. On the indigo gradient it would be a fifth application point on a ground
nothing has measured, which is exactly the defect the phase 3c contrast audit found on
a surface that had been reasoned about rather than measured. The dot is excluded from
calm mode's transform by name, 16.2, because it is how an area is recognized, and it is
enough on its own.

**Considered and rejected: measuring the accent variant against the indigo ground and
using it.** It is the better looking answer and it is a contrast audit this phase did
not run. It can be done later with a number in hand.

**Revisit if** the phase 12b surface pass measures the Contemplative ground and finds
headroom.

### Open, and not a builder's to settle: a session a person ends fires no haptic

**Not decided. This is the owner's.** At a natural completion, with the person watching
the ring, the completion screen fires `focusEnd`, which is `design-v3.md` 9's row for
that moment. At a session the person ends themself, the End control fires the ordinary
`tap` and the completion screen that follows fires nothing. A session resolved on the
next resume fires nothing either, and that part is settled: section 9 forbids a haptic
on screen entry.

**Why it is open.** Issue #28 asks for the completion haptic and tone for an early end
to be the same as for a natural completion **or deliberately gentler**, and says
plainly that they are not to be absent, because absence reads as the app withholding
acknowledgment. What was built is absent. There is a real argument for it, which is
that section 9 fires nothing more than once per user action and the tap on End is that
action, so firing `focusEnd` a beat later would be a second event for one gesture. That
argument was never written down as a decision, and the requirement it collides with is
explicit, so recording it as settled would be recording a choice nobody made.

**The recommendation, stated and not taken:** fire `focusEnd` on the completion screen
for both endings, and let the End control fire nothing, so the acknowledgment lands
where the completion is rather than where the tap was. That is one event per gesture,
it satisfies #28 without a new haptic row, and it makes the two endings identical on
the one surface Addendum 01 4e cares most about. The alternative, a deliberately
gentler event, needs a row in section 9 that does not exist and would make the two
endings distinguishable by feel, which is the thing 4e is against.

**Revisit if:** it is open, so it does not need one. It should be closed before phase
13's accessibility and haptics pass, which is where a missing acknowledgment is found
by hand.

---

## August 27, 2026: the polish pass, and the design foundations under it

Phase 3c, issue #53, and the split that puts the rest of it in phase 12b, issue #54.
Addendum 01 3c, `MASTER_BUILD_PROMPT.md` 19, and `design-v3.md` 1, 3.1, 3.4, 5.3, 10.3,
11, 13, 14 and 15. Eight things settled, seven of which required changing the document
that is the authority on anything visual, which is the owner's call and not a builder's.

**The starting fact, recorded plainly because it is the least comfortable one.**
Addendum 01 3c asks for a polish pass and no phase ever carried it. When the addendum
was recorded, earlier the same day, 3c was written into `design-v3.md` 17.4 and
`MASTER_BUILD_PROMPT.md` 3.2 as a rule governing platform versus custom, and the pass
that the same section describes was never turned into a phase. That is a recording
error rather than a change of plan. **The owner found it by looking at the app rather
than at the plan**, on the build at 0.4.0:

> "I don't know if we've gotten to the design, polish, and premium feeling layer that's
> added on top of the Material 3 Expressive. But I can tell you so far what I've seen is
> not that great."

An audit was commissioned to separate three causes that have completely different
fixes: implementation drift, where the code does not match a number the document
already states; missing content, where a screen is running at partial content because
its phase has not arrived; and a genuine gap, where the code matches the document and
the result is still flat. **It found the third dominates**, and about twenty drift
findings of which five were visible on a screen the owner could open.

**The finding that made every decision below the owner's.** `design-v3.md` removed
every conventional decoration device on purpose, which is a strong position and the
right one, and it moves the entire burden onto four things: type, space, value and
motion. Measured on the two built screens, three of the four were not carrying. Value:
card against canvas at 1.126 to one, `raise` declared and drawn nowhere, and two colors
covering 91.6 percent of the Areas screen. Type: tracking set on two of the nine sans
roles, `body` and `bodyStrong` both at 16sp, and the signature serif appearing at five
call sites in the whole app of which four were empty states, which had quietly made
Newsreader mean "there is nothing here". Motion: nothing moving at rest. Space was
carrying, alone, and what it was expressing was emptiness. **None of that is reachable
by conforming harder**, which is the whole reason the document moved instead.

### The light world buys its depth downward, and pure white loses

**Decided.** `canvas` goes from `#F1F1F6` to `#E6E6EC`, L\* 95.3 to 91.4. `card` goes
from `#FFFFFF` to `#FCFBF9`, L\* 100 to 98.6, and is warm. `raise` goes from `#FAFAFC`
to `#F4F3F0`, L\* 98.3 to 95.8. Card against canvas moves from **1.126 to one** to
**1.202 to one**, and the light world's span from 4.73 L\* to 7.19. In dark, where
`design-v3.md` 6.1 allows no shadows at all and the lightness ladder is therefore the
only separation device there is, `card` goes `#191921` to `#1D1D25` and `raise`
`#15151C` to `#18181F`, while `canvas` is deliberately held at `#0E0E13`. Stated in
`design-v3.md` 3.1 and 3.2 and held by `SurfaceLadderTest`.

**This resolves a contradiction that had survived two phases, and it is recorded as
resolved rather than quietly fixed.** `design-v3.md` 1 says "backgrounds are never pure
white or pure black". Section 14 says "no pure white or pure black backgrounds". 3.1
said `card` is `#FFFFFF`, and 10.3 repeated the hex rather than naming the token. **Two
statements against one, and the build had followed the one**, faithfully, because 3.1
is the token table. The two win. 10.3 now names the token, because a component section
repeating a color out of the token table is how the contradiction lasted as long as it
did.

**Why the ground moved rather than the card.** There was no headroom. `card` was
already pure white, so no amount of lifting it could produce a step, and the only
direction with any room in it was down. That is also the answer to the instinct the
audit named and `design-v3.md` 15.3 now refuses: when an element does not separate,
measure whether the ground under it has any room left before reaching for the element.
The new canvas keeps the cool lean section 1 asks for, at 6 points of blue over red,
and the new card is warm at 3 points of red over blue, which is the "warmth in the
cards" the same sentence promises and which nothing in the app delivered. `cardWash` is
the user's own area color and four of the eight moods are cool, so the default area put
a cool wash on a pure white card.

**Considered and rejected: taking the dark canvas toward pure black**, which is the
statistically common 2026 answer for an OLED panel and would have bought dark its step
the cheap way. Section 14 bans pure black outright, and the Contemplative world's
`deepBlack` is only 0.97 L\* below the Daylight canvas, so a darker Daylight ground
would collapse the two worlds' floors and take the room dimming out of entering Focus.
Dark bought its depth upward instead, which is the exact mirror of the light world's
problem. **Considered and rejected: leaving dark alone** on the theory that the light
world was what the owner was looking at. Dark measured 1.102 to one card against
canvas, worse than the light world that also had a paired shadow to help it, and a
device capture was 86.6 percent one color.

**One half of the contradiction is deliberately left open.** Section 1 also says "dark
surfaces are warm blacks" and every dark token in 3.2 leans cool by 5 to 8 points of
blue over red. That is a separate decision about four tokens across two worlds and it
is left to be taken as one rather than folded into this entry.

**Revisit if** the parchment weekly banner arrives in phase 5 and still fails to
separate. It measured 1.036 to one against the old canvas, which is to say it would
have arrived **less** visible than the card was; against the new canvas it is a
different number and it was not re-derived here, because nothing draws it yet.

### `inkSecondary` moves to 0.64, and it was forced rather than chosen

**Decided.** The light `inkSecondary` goes from ink at 0.60 to ink at **0.64**.
Stated in `design-v3.md` 3.1, held by `TrailContrastTest`.

**Why this is in its own entry.** It is not a design improvement and nobody proposed
it. It is a second order effect of the canvas change, and it is the kind of effect that
gets missed. At 0.60 the light canvas was already the tightest surface in the app at
**4.5046 to one**, four ten-thousandths above `design-v3.md` 13's floor of 4.5, and a
test written in phase 3 pinned it there with a comment predicting that "a later
darkening of `canvas` would take the whole Daylight world under the floor without
touching this screen". Darkening the canvas did exactly that, to **4.33**. So the
canvas change is not free: it costs four points of alpha on an ink token, everywhere,
and had the prediction not been written down the first symptom would have been an
accessibility failure on a screen nobody had edited.

At 0.64 the tightest ground in the app is the light canvas at **4.88 to one**. The
raise also retires a defect phase 3b had to pin as unfixable rather than fix: at 0.60
this token measured 4.48 to one on a resting area card and **4.27 on an in-session
one**, and 16.7 recorded it as measured and held in a test because raising an ink token
for every screen in the app was out of scope for a calm mode audit. It is in scope for
a token pass. Both now clear, at 5.05 and 4.75.

**Considered and rejected: leaving the ink alone and taking less canvas.** A canvas
light enough to keep 0.60 over the floor is a canvas that does not buy the card a step,
which was the entire point. **Considered and rejected: raising the alpha only where a
measurement failed**, which would have made `inkSecondary` mean two different values
depending on the ground and defeated the purpose of a token.

**Revisit if** any future change to `canvas`, in either world, moves the tightest
measured pair. The rule this entry establishes: **the ink tokens are downstream of the
ground tokens and are re-measured whenever a ground moves.**

### `raise` gets a job

**Decided.** The three neutral surfaces are a **rank ladder** and each rank has named
occupants. `canvas` is the page. `raise` is chrome: the floating tab bar and an
unselected chip. `card` is content: area cards, sheets and the undo snackbar. Content
is the top plane and chrome recedes from it, and the statement is the same in both
worlds, which is why the steps are matched across them rather than each world being
tuned alone. Stated in `design-v3.md` 3.1, held by `SurfaceLadderTest`.

**Why.** 3.1 defined `raise` as "the 3 percent lightness step used instead of a border"
and never said where it goes, so it went nowhere: it was handed to Material's
`surfaceVariant` and drawn by nothing. The card, the tab bar, the sheets, the chips and
the snackbar were therefore one value doing four semantically different jobs, and a
screen of cards read as a screen of chrome. It would not have helped even if something
had drawn it, at 1.68 L\* under the card. Giving it a rank roughly triples the depth
budget of the app using no decoration whatsoever, which is the only kind of depth this
design system permits.

**`raise` plus a shadow is one separation device, not two, and that needs saying
because it looks like a violation.** 6.1's prohibition is specifically "never a hairline
and a shadow on the same element", and 3.1 defines `raise` as the step used **instead
of** a border. A card has always been lighter than the canvas and has always carried a
shadow. A value is what a surface is; only a deliberate step standing in for a hairline
is the device. Putting a hairline on anything in the ladder is still forbidden, and
`design-v3.md` 15.3 refuses it by name.

**Considered and rejected: putting sheets on `raise` as well**, which is the tidier
rule, one value for everything that floats. A sheet is where a person reads and types
and is content, not chrome. **Considered and rejected: putting the undo snackbar on
`raise`** with the rest of the floating furniture. For five seconds it is the top plane
on the screen and what it holds is the only way back from a deletion; chrome recedes
and that cannot afford to.

**Revisit if** a surface has to be drawn **inside** a sheet, which is the one job
`raise` was defined for and does not yet have. The color picker preview in 10.9 is the
likeliest first.

### Tracking runs the whole sans scale, and `body` and `bodyStrong` stop being the same size

**Decided.** Every one of the nine sans roles in `design-v3.md` 5.3 carries a tracking
value, and a role without one is now a defect rather than a default. The ramp opens at
the small sizes and closes at the large ones: +0.032, +0.022, +0.016, +0.004, -0.006,
-0.014, -0.022, -0.030em, from `swipeLabel` at 10.5sp to `timerNumeral` at 64sp.
`sidehead` sits deliberately off the ramp at +0.024em, above `label` at the same 13sp.
Separately, `body` drops from 16sp to **15** and `bodyStrong` rises from 16sp to **17**.

**Why tracking.** Hanken Grotesk carries one set of sidebearings, drawn to fit at
reading sizes. Set large it leaves proportionally too much room, because the space
between letters does not need to grow at the same rate the letters do; set small it
leaves too little, for the same reason in reverse. The correction is the oldest one in
typesetting. Two of the nine roles carried a value, so the seven that make up almost
every string on both built screens ran at whatever the font file happened to ship. This
is the cheapest change in the whole pass and it touches every label, sidehead, caption
and chip in the app, built and unbuilt.

**Why `sidehead` is off the ramp.** A section label has to read as a marker rather than
as a sentence, and the conventional way to buy that is capitals, which 5.3 and section
14 and 15.1 all forbid. Tracking says the same thing at a fraction of the volume and
leaves the sentence case intact. Recorded under section 15 as an open choice taken
against the obvious answer.

**Why the two body roles split, and why the step is uneven.** They were both 16sp,
which made a size hierarchy impossible inside a run of text. `design-v3.md` 11 gives a
Trail day header to `bodyStrong` and the rows under it to `body`, so the header could
outrank its rows by weight and by nothing else, and a device capture of four events
read as a wall of one size at 12 and 16sp. Forty rows would read worse. Most of the
step is taken below rather than above, because `bodyStrong` is also the button label in
10.7 and the undo action in 10.14, and one step is as far as either can move without
starting to shout. 16sp is in any case Material's `bodyLarge` default, so moving off it
is the choice section 15 asks for, and 10.11 already set a settings row title at 15sp,
which makes 15 an established row size in this document rather than a new one.

**Considered and rejected: a new role for the day header** and leaving the body pair at
16sp. It adds a fifteenth name to a scale of fourteen to avoid admitting that two of
the existing fourteen were the same size, and it would have left every other run of
text in the app with no step available. **Considered and rejected: moving `bodyStrong`
to 18sp** for a cleaner 15 and 18. It is one step from `title` at 19sp, and a button
label at 18sp is a loud button.

**Revisit if** a screen built in a later phase needs a size between 15 and 17. The sans
ladder is 13, 15, 17, 19, 21, one even step per rung, with 12 and 10.5 beneath it, and
inserting a rung is a change to the ladder rather than an addition to it.

### The Trail gets a serif title, which reverses a decision phase 3 took deliberately

**Decided.** The Trail opens with `Trail` in `displayTitle`, left aligned on the same
measure as the day headers, no glyph beside it, the same treatment the Areas title
takes in `design-v3.md` 10.1. Stated in `design-v3.md` 11 and built in
`ui/trail/TrailScreen.kt`.

**This reverses a decision, and the old one was not careless.** Section 11 gave the
Trail day headers and rows and stopped, phase 3 read that silence as intentional, and
it reasoned the reversal out: the tab bar already says Trail, so a heading reading Trail
repeats a word the user can see at the other end of the same screen. That is a real
argument and it is still true.

**What changed is not the argument, it is what the argument was weighed against.**
Phase 3 priced the repetition and did not price the absence, because with one screen
built there was nothing to price it against. Section 11 opens every other surface in
this app with a headline treatment: Areas takes a `displayTitle`, the Report an eyebrow
and a `displayHero`, Momentum a `readSerif` headline, About a `displayTitle`. The Trail
was the one screen in the document that began with its content. The audit measured what
that cost. The built Trail contained **no serif glyph at all**, and across the whole app
Newsreader had five call sites, of which four were empty states, so the signature
typeface of this design system meant "there is nothing here" four times in five. The
same absence had a second effect the audit named separately: with no title, the loudest
element on the Trail was the selected `All` filter chip, so **a filter outranked
everything it filtered**. One serif line fixes both, and it changes the app-wide ratio
as well as this screen.

**Considered and rejected: a different treatment for this title**, something with more
character than the Areas title, on the argument that the Trail is a different kind of
screen. Two screens that open the same way are one app; a Trail title styled to be
interesting would be a second design language for the screen that reads history.
**Considered and rejected: reusing `tab_trail` for the string.** The two say the same
word today and are free to stop, and a tab label and a serif screen title are not the
same kind of copy.

**Revisit if** a later screen has something that genuinely belongs beside its title, as
the archive and settings glyphs do on Areas. The Trail has nothing, which is why it has
no glyph and not because a title never takes one.

### The idle card title comes off `inkTertiary`, because section 13 beats 10.3

**Decided.** `Add your first item` and the `Last active` status line under it read at
`inkSecondary` rather than `inkTertiary`. The weight drop from 650 to 500 is unchanged
and is what carries the idle state. Stated in `design-v3.md` 10.3.

**Why, and it is a contradiction inside one document rather than a preference.** 10.3
said the idle title reads "at inkTertiary weight 500" and the same section calls the
active title "the most important string on the screen". `inkTertiary` measures **2.40
to one** on the card in light and 3.22 in dark, against section 13's floor of 4.5.
**13 wins, because a floor is a floor.** `inkSecondary` is the one step down from
`inkPrimary` that clears, at 5.29 to one in light and 6.36 in dark on the phase 3c card.
The idle state loses nothing: the weight is what says "waiting to be filled", and it
says it whether or not the color is also below the threshold at which a person can read
the invitation they are being given. The status line was the worse of the two, being
12sp rather than 21, and 10.3 named no color for it at all, so there was no
contradiction there to resolve, only section 13 to obey.

**Two inert weight calls were retired in the same change and they are worth recording,
because nothing failed.** `AreaCard.kt` asked for weight 500 on the idle title and 600
on the in-session status line, and both rendered at the wrong weight in phases 2 and 3b.
The sans family was built one weight per `FontFamily` with `FontVariation.weight` baked
into the typeface at load, so the font matcher had exactly one face to return and
`TextStyle.copy(fontWeight = ...)` resolved to the pinned instance. **The requested
weight was discarded with no error and no visible symptom.** The family now registers
every weight the design system asks for, so the matcher has something to choose between.

**Considered and rejected: keeping `inkTertiary` and lifting the alpha until it
clears.** `inkTertiary` at an alpha that clears 4.5 to one is `inkSecondary` with a
second name.

**Revisit if** a variable weight axis is ever needed at a weight not on the registered
list. A request off the list resolves to the nearest one that is, which makes a missing
entry a slightly wrong rendering rather than a silent no-op, and that is the contract
this change restored.

### The default area walk starts at Berry and never hands out a function color

**Decided.** Two changes, and the second is the load bearing one. The walk starts at
**Berry** rather than Ocean, and it never assigns `#2D7FF9`, `#4DA3FF`, `#22C55E` or
`#F59E0B`, each of which is byte identical to a function token in `design-v3.md` 3.1 or
3.2. **All four remain choosable**: 3.4 opens with "all 48 available to everyone" and
that is untouched. What changed is only what the app hands out on its own.

**Why.** The shipped walk gave the first area `#2D7FF9`, which is `actionBlue`. On the
first run screen, where the only two colored elements in the entire app are one area and
one FAB, they were the same color, so identity and function collapsed on the first
screen a new user ever sees. **An identity indistinguishable from a status is not an
identity.** It is also not a single collision: `#22C55E` is the first color of Meadow,
so the walk also handed out `positiveGreen`, the completion color, at area five, and it
reaches both inside the first eight areas from **every** starting point. Moving where
the walk starts cannot fix that. Naming what it may not hand out can, which is why the
reservation is the real change and the starting point is the smaller one.

**Why Berry.** 3.4 asks the walk for one thing, that the first four be distinct without
the user choosing, and the shipped start did not deliver it: `#2D7FF9` at hue 216 next
to `#6366F1` at 239 is a 23 degree step and two blues in a row. Berry's first four are
292, 0, 142 and 41, whose narrowest step is 68 degrees, the widest that any of the eight
possible starts produces, measured across all eight rather than assumed.

**Considered and rejected: Twilight, which is the obvious move**, one step along the
list from Ocean. `#6366F1` is 23 degrees from `actionBlue`, an indigo that reads as a
shade of the button rather than as an identity, and it walks toward the family 15.1
names twice. It is the worst available start on both counts. **Considered and rejected:
Earth and Stone**, both of which sit further from `actionBlue` than Berry does. Starting
at Earth puts `#CA8A04` and `#A68B6B` eight degrees apart in the first four, two muted
yellows a person has to compare rather than recognize; starting at Stone takes the
narrowest step to 22. Each buys distance from one button by giving up the distinctness
the walk exists for. **Considered and rejected: stepping over a reserved color at
assignment time** rather than filtering it out of the list. Stepping maps two different
area counts onto the same color, so area three and area nineteen would both come out
`#18BFFF`; filtering keeps the walk a bijection over each mood's remaining colors, which
is the property that makes "distinct without anyone choosing" true past the first eight.

**Revisit if** a function color is ever added to or removed from 3.1 or 3.2. The
reserved list is derived from that table and is not independent of it.

### The pass is split: foundations now, surfaces at phase 12b

**Decided.** Phase 3c, tokens and type, sits between 3b and 4. Phase 12b, surfaces,
sits between 12 and 13. `MASTER_BUILD_PROMPT.md` 19 carries both, issue #54 carries
12b's acceptance criteria.

**Why, and it is not a compromise between doing it now and doing it later.** The two
halves have genuinely different properties. **A token or a type role corrected now is
inherited free by every screen that does not exist yet.** Focus, Pulse, Momentum, the
Report, onboarding, Settings, About and six widgets will all be built on the corrected
values without anyone touching them again. Corrected at phase 13 instead, all eight of
those would have been composed against a value structure the owner has already said is
not good enough, and every one would have to be re-examined, which is exactly the
re-polishing cost the scheduling tension in #53 names. The two highest impact changes
in the entire audit are both token changes.

**Surface changes have the opposite property and get better information by waiting.**
Fading the scroll edge on Areas does nothing for Momentum. Deciding what a text field
looks like is worth doing once, when there is more than one sheet to judge it against.
And the Contemplative world is unbuilt: the indigo Focus night, the amber Pulse night
and the gold Report carry half this app's character and all of its atmosphere, and
polishing the Daylight surface with no Contemplative surface to balance it against is
judging half a design against itself.

**Phase 12b and not a bullet inside phase 13**, which is the part of this decision most
likely to be undone by someone being sensible. Phase 13 already carries the Baseline
Profile, R8, the accessibility pass, the full checklist, real screenshots, the README,
the store listing and the release. **A polish pass buried inside a ship phase is the
first thing cut when a date moves.**

**One expectation set honestly, because it is easier to set now than to explain later.**
Phase 3c moves the app from flat to solid. It does not move it from flat to distinctive,
and it was not designed to. Distinctive is 12b, and it is mostly five open questions
none of which can be answered well with two screens on the table: whether anything in
this app moves at rest, what the Trail's event circle carries, whether an inactive tab
keeps its label, what a text field looks like, and what a sheet full of choices looks
like. 3c should be judged as "the foundation is now right".

**Considered and rejected: doing all of it now**, which polishes four screens and
re-polishes as eight more arrive. **Considered and rejected: doing all of it at phase
13**, which lets eight more screens be built against a look nobody is happy with and
turns the pass into a rewrite. **Considered and rejected: waiting for the whole pass
before shipping anything**, which the audit argued against directly: the largest single
visual return in it was thirty minutes of work, an area card whose shadow was being
clipped away in full by the row that made it swipeable.

**Revisit if** phase 12b's five questions turn out to be answerable earlier than phase
12, which would mean the Contemplative surfaces built in phases 4, 6 and 8 settled them
on arrival. That is a reason to pull 12b forward, never a reason to fold it into 13.

### Where the audit's refusal list went, and why

**Decided.** The audit's "what not to do" list is `design-v3.md` **15.3**, a section of
its own, rather than additional entries on 15.1.

**Why.** 15.1 is a dated record of what the industry currently produces, 15.2 requires
it to be re-swept before every release, and it is expected to churn. The refusals are
not industry observations and they do not expire: each is a specific temptation in this
specific app, paired with the sentence in this document that already forbids it, and
re-deriving them at every release is the work the section exists to prevent. Merging
them would also blunt 15.1, which is read as a list of tells to check a design against,
while half the refusals are not tells at all but fixes that break a rule. The two lists
fail in opposite directions, which is the last reason they are two: a stale entry on
15.1 costs a false alarm about a treatment nobody makes any more, and a missing entry in
15.3 costs the treatment.

**Considered and rejected: `DECISIONS.md`**, meaning this file, which is where the
reasoning would naturally go. This file has no authority over anything and says so in
its own header. A refusal that has to hold against a session reaching for the nearest
premium looking thing has to live in the document that wins on anything visual.

**Revisit if** 15.3 grows past the point where a person will read it before starting.
It is fifteen entries and it is meant to be read once per pass.

## August 27, 2026: six open choices in the calm mode and entrance retrofit

Phase 3b, issues #48, #50 and #27. Addendum 01 8c, 8e and 4d,
`MASTER_BUILD_PROMPT.md` 14b.4 and 14b.12, `design-v3.md` 8.4 and 16. Six things
those documents leave to the builder, settled here under `design-v3.md` 15.

**The capture path's four choices are the entry below this one** and are not repeated
here: the FAB captures into the inbox, deleting an area does not orphan its items, the
first step truncates on the card and wraps in the sheet, and the estimate is a free
number field.

### An app session is the process lifetime

**Decided.** An entrance fires on the first open of its tab per app session, and a
session begins at process start and ends when the process ends. A rotation or a theme
switch does not re-arm it. A process death does. Implemented in
`ui/theme/ClarityEntrance.kt` and stated in `design-v3.md` 8.4.

**Why.** The flag is stored as **which session spent it** rather than as a boolean,
and that is the whole trick. `design-v3.md` 8.4 wants a rotation to leave the entrance
spent and a process death to re-arm it, and those pull in opposite directions: a
`remember` loses a rotation and would re-fire on every one, a `rememberSaveable`
survives a system initiated process death and would therefore never re-arm. Holding a
token read once per process settles both, because a bundle restored into a new process
carries a token that matches nothing. The token is `SystemClock.elapsedRealtimeNanos`,
read once when the holder initializes: two launches cannot read the same nanosecond,
and a reboot resets the clock but also discards every saved bundle.

It is keyed to the tab rather than to the screen, in the per tab saveable state the
shell has held since phase 3, so a sheet opening over Areas is not a first open.

**Considered and rejected: re-arming after some period in the background**, which is
what a person would guess if asked, and which several apps do. It invents a threshold
nobody asked for, it makes one screen behave two ways for a reason the user cannot
see, and predictable interface behavior is worth more to this audience than a second
chance to show an animation.

**Revisit if** phase 8's Report reveal needs its content key, which `design-v3.md` 8.4
already carves out as the one entrance that re-arms on a content change. That is a key
alongside the session token, not a change to what a session means.

### Calm mode does not go back to following the system

**Decided.** Calm mode follows the OS reduce motion setting, live, until the user sets
the switch. The first explicit choice gives the switch a value of its own and it stops
following. There is no path back to following, short of erasing all data. Implemented
in `data/prefs/ClarityPreferences.kt` and `ui/theme/CalmMode.kt`, and stated in
`design-v3.md` 16.1.

**Why.** A control that silently changes state because something outside the app
changed is a control this audience cannot rely on. Predictability is worth more here
than saving somebody one tap. The storage carries three states, absent or true or
false, and the interface shows two, which is what makes both halves true at once: the
default is not off, it is whatever the system asks for, and a `Boolean` defaulting to
false would have meant off for every person who has the system setting on and never
opens Settings. That is precisely the person the feature exists for.

**Considered and rejected: keeping the switch tracking the system whenever the two
happen to agree.** It is the tidier model and it is unpredictable in the one way that
matters: the app would look different tomorrow because of something the person changed
in a different app last week. A three-state control, On, Off, Follow system, was
rejected separately in `design-v3.md` 16.1, because a third state is a third decision
on a settings screen already full of them.

**Revisit if** anyone asks for a way back to following the system. `Erase all data`
clears the key with everything else, which is a real answer but a heavy one.

### The entrance starts at the screen title, not at the first card

**Decided.** The Areas header takes entrance index 0 and the cards follow at 1 and
after, so the screen arrives as one sequence. Implemented in `ui/areas/AreasScreen.kt`.
The conflict cards deliberately take no entrance index at all.

**Why.** The obvious answer, and the one most list screens use, is to stagger the rows
and leave the header fixed. A fixed title with rows pouring in underneath reads as
content loading into a frame. What this entrance is for is the app arriving. Starting
at the title costs one stagger step, 50ms, and makes the screen one thing rather than
two. A conflict card is an interruption that arrives because a merge produced one,
carrying its own reveal, rather than part of the screen's resting content, so it is
outside the sequence.

**Considered and rejected: a fixed header.** Cheaper by 50ms and it reads as a loading
state, which is the one thing an entrance must not look like.

**Revisit if** a header ever carries something a person needs before the list, at
which point the thing that needs to be immediate is the argument, not the header.

### The color picker's swatches keep their true color in calm mode

**Decided.** The 48 swatches and the selection ring are not transformed. The live
preview card beside the grid is. Implemented in `ui/areas/ColorPicker.kt`, where the
preview goes through `Modifier.areaWash` and the swatches call `parseAreaColor`
directly, and recorded in `design-v3.md` 16.7.

**Why.** `design-v3.md` 16.2 sorts every use of an area accent into atmosphere, which
takes the transform, and identity, which does not. A swatch is neither. It is a
**choice**, and a person picking a color has to see the color they are picking. A
desaturated grid would have somebody choose one accent and receive a different one the
moment they turned the switch off. The preview card takes the transform for the
opposite reason: it is a miniature of the real card and its job is showing what the
card will look like, which in calm mode is calm.

**Considered and rejected: transforming the grid too, for consistency.** Consistency
is the argument, and it is the wrong one: it would make the picker consistent with the
card at the cost of making it inconsistent with the thing it is picking.

**Revisit if** the swatch grid is ever shown without the live preview beside it, which
is the arrangement that makes the distinction legible.

### The platform bottom sheet cannot honor calm mode, and it ships anyway

**Decided.** `ClaritySheet` stays on Material's `ModalBottomSheet`. With calm mode on
and the system reduce motion setting off, the sheet's own entrance and dismiss still
travel. The 42 percent scrim and everything drawn inside the sheet do honor calm mode.
Recorded in `design-v3.md` 16.8.

**Why.** The sheet's animation is the platform's, it honors the system animator scale
and therefore honors reduce motion, and it exposes no specification an app preference
can reach. Two standing rules point in opposite directions here: `design-v3.md` 17.2's
fourth reason says a component that cannot honor calm mode is a component that cannot
ship, and 17.4 says a polish pass never reimplements a working platform component.
Both are true, which makes this a decision rather than a defect to fix quietly. The
person calm mode exists for almost certainly has the system setting on as well, which
is the case where the gap does not arise; the gap is real only for somebody who wants
the app calm and the rest of their phone lively.

**Considered and rejected: hand-building the sheet.** It would be the most used
component in the app rebuilt to change one curve, and it would put predictive back,
the insets behavior, the drag to dismiss and the accessibility handling on this
project's own maintenance. The standing register at the foot of this file exists for
components with no platform equivalent, and a bottom sheet is not one of them.

**Revisit if** the platform grows a hook for its own sheet animation, or if the sheet
turns out to be the one thing a calm mode user says bothers them.

### Re-entry detection hands out the date of the return, never the length of the absence

**Decided.** `ReEntry` carries `returnedOn` and nothing else. There is no field, no
function and no overload anywhere in `domain.query` that yields the number of days a
person was gone. `daysSince(dateKey)` exists, counts forward from the return, and
every caller of it is a suppression window. Implemented in
`domain/query/TrailFacts.kt` and `domain/query/TrailQueries.kt`.

**Why.** `MASTER_BUILD_PROMPT.md` 14b.4 forbids stating the length of the gap "not in
days, not in weeks, not as a date, not as `since March`". The statistically common
shape for this is `fun gapDays(): Int?`, and it is the shape that makes the forbidden
screen a one line mistake four phases from now, when somebody who has not read 14b.4
is building the surface and finds a number sitting there that looks like it wants to
be rendered. A prohibition that rests on somebody remembering it has a shelf life. This
one rests on the number not existing.

**Considered and rejected: carrying the gap and marking it forbidden in KDoc.** It is
the version that survives review and fails in six months. The comparison against the
14 day threshold happens inside the query and the number does not survive the return.

**Revisit if** 14b.4 itself changes, because nothing short of that could make the
length of somebody's absence a thing this app is allowed to say.

---

## August 27, 2026: the four open choices in the capture path

Phase 3b, issues #24, #25 and #26. Addendum 01 4a, 4b and 4c, `MASTER_BUILD_PROMPT.md`
14b.1 to 14b.3, `design-v3.md` 10.16 and 10.17. Four things those documents leave to
the builder, settled here under `design-v3.md` 15: identify the statistically common
answer, and take something else unless the common one is genuinely best.

### The FAB captures into the inbox, not into the first area

**Decided.** With at least one area, the FAB opens the add sheet with **no area
chosen**, and the item is written with a null `areaId`. The add sheet carries no area
control at all. Adding straight into a known area stays where it already is, on that
area's detail sheet, where the area is context rather than a choice. At zero areas the
FAB still creates an area, which `MASTER_BUILD_PROMPT.md` 8.4 states and 14b.1 leaves
standing until its own open question is answered.

**Why.** The obvious answer is an area picker on the add sheet with the inbox as one of
its options, and it is the wrong one twice. It puts a decision with N options between
the thought and the record, which is the exact thing 4a exists to remove, and it makes
the inbox look like a place you file into rather than the absence of filing. What the
FAB did before this was worse than either: it added into whichever area sorted first,
which is a decision the app made on the person's behalf and got right only by
accident. Nothing is lost by removing it, because there was never a picker to lose.
The FAB now means one thing everywhere, and it means capture.

**Considered and rejected: a row of area chips at the foot of the add sheet**,
deselectable, nothing selected by default. It is defensible and it was close. It loses
on the same ground the picker does: a control that is visible is a decision that has
been offered, and an audience whose difficulty is deciding will read an unselected row
of areas as an unanswered question. It also grows the sheet at 200 percent font past
what one screen holds.

**Revisit if** 14b.1's own open question is answered the other way, or if the Quick
Capture widget in phase 12 makes the FAB's meaning ambiguous again.

### Deleting an area does not orphan its items into the inbox

**Decided.** `AREA_DELETED` tombstones the area's items with it, which is what
`ClarityReducer.areaDeleted` already does. Nothing moves to the inbox. Asserted in
`ClarityInvariantsTest` so that reversing it is a deliberate act rather than a side
effect of an edit somewhere else.

**Why.** The obvious answer is to orphan, on the reasoning that nothing is lost that
way. It loses anyway. Deleting an area is already the most deliberate act in the app:
it takes a typed confirmation, it has no undo, and the sheet says in plain words that
the area and everything in it goes and that the history stays in the Trail. Thirty
queued items reappearing in the inbox would contradict a sentence the person read and
typed DELETE against, which is worse than either behavior chosen honestly. And it would
turn one deliberate act into a pile of work nobody captured, at the top of the one
surface `design-v3.md` 10.16 built specifically so that a pile never sits above the
thing a person opened the app to see.

**Considered and rejected: orphan, and say so in the delete copy.** Honest, but it
makes delete a bulk unfile, which means there are then two ways to empty an area and
one of them is spelled `delete`. Archive already exists as the non destructive path and
its copy already says what it does.

**Revisit if** anyone ever reports losing work to this. The undo window does not cover
an area delete, so the evidence would arrive as a person saying so rather than as a
metric, and there are no metrics.

### The first step truncates on the card and wraps in the sheet

**Decided.** One line, ellipsized, at `caption` on the area card and on an inbox row.
In full, wrapping, in the area detail sheet. The field itself has no length limit.

**Why.** `design-v3.md` 10.3 caps the card at four lines and names this the row that
truncates first when a status line is also present, because the status line is about
now and the first step is about next, and now wins on a card that has to pass a three
second test. The sheet has no such job and is where reading happens, so it wraps. The
field is unbounded because `MAX_ITEM_TITLE` exists to protect the line every surface
prints, and refusing a capture over a long second line would lose the thought this
whole path exists to keep.

**Considered and rejected: a character limit on the field**, which is the common
answer and would make both surfaces easy. It moves the cost to the wrong place: the
person typing, at the moment of capture, rather than the layout.

**Revisit if** a surface ever needs the first step in a fixed height slot. A widget
might, and phase 12 owns that.

### The estimate is a free number field, not a set of durations

**Decided.** Optional minutes, typed as digits, four at most. No chips, no stepper, no
presets. `design-v3.md` 10.17 says `entered as a number` and this is that.

**Why.** The statistically common answer is a row of duration chips: 15, 30, 60, and
so on. It loses twice. It is a decision with five options placed in the capture path,
which is what this phase exists to remove, and it silently makes the buckets somebody
else chose into the shape of the data. `MASTER_BUILD_PROMPT.md` 14b.8 has phase 8 read
these numbers as ratios and tendencies, and a ratio computed over five preset values is
a fact about the presets. Typing `20` is one gesture; picking the nearest chip to 20 is
a comparison.

**Also decided, and it is the harder half:** no surface counts down against the number,
anywhere. The card does not show it at all. The detail sheet shows it once, as plain
text, and no surface renders it beside an actual. An estimate that becomes a deadline
is a worse instrument than no estimate, and it is a deadline the person set for
themselves in a hopeful moment and then has to watch expire.

**Revisit if** phase 8's calibration facts turn out to need a coarser input to be
stable at low counts. That would be an argument for rounding what is read, not for
bucketing what is typed.

---

## August 27, 2026: a key is chosen against the whole space, not the visible part

**Decided.** Any order key is computed against **every entity that can occupy that
ordering space**, including the ones a filtered list leaves out, and excluding only
tombstoned ones that can never return. In the repository this is `tightenedBetween`,
and every path that mints a key goes through it or through `tailOrderKey`.

**Why.** A class of defect shipped in 0.2.0 and was found here by the replay harness
after the harness was taught to file items out of the inbox. Four separate paths had
it, and every one had the same shape: bounds were taken from the entities currently in
view rather than from the space.

The two spaces, and what each hides:

- **An area's items.** The active item holds a key in the same space as the queue but
  is not a member of the queue. `queueIn` therefore leaves it out, and at either end
  of the queue one bound is null and encloses everything. The plainest instance: the
  second item added to a fresh area took `OrderKey.first`, which is deterministic for
  a given jitter, and the first item was holding exactly that key already. It also
  cannot be assumed the active item sits below the whole queue, because promotion
  from the head leaves it there but a swap promotes whichever item the person chose.
- **Areas.** An archived area keeps its key. Archiving is reversible, so the key is
  not free, and unarchiving returns the area to the live list still holding it.

**What made it worth the hunt is the distance between cause and symptom.** A duplicate
key does nothing at the moment it is made. Both lists still render, the tie broken by
id, and no screen looks wrong. It surfaces much later as an `IllegalArgumentException`
out of `OrderKey.between`, on the first drag that asks for a key between the two of
them, in a session that did nothing unusual. Nothing about that crash points back at
the add, the swap or the unarchive that caused it.

The affected paths were `addItem`, `fileItem`, `reopenItem`, `moveItem`, `createArea`
and `moveArea`, plus the matching four in the test generator.

**Considered and rejected.**

- **Detecting the collision when a key is used and repairing it then.** Rejected: it
  puts the fix at the symptom, where the information about intended position has been
  lost, and a repair that guesses a position is a silent reordering of a person's
  queue.
- **Making `OrderKey.between` tolerant of equal bounds** by returning something
  plausible. Rejected for the same reason, and worse: it would have buried this defect
  permanently instead of surfacing it. The refusal is the feature.
- **Fixing only the paths the harness caught.** Rejected. Two of the six were found
  only after the first two were fixed, which is the usual sign that the class matters
  more than the instances.

**Revisit if** a third ordering space appears, or if an entity type gains a state that
holds a key while being filtered out of the list a key is computed from. Both are the
precondition for this defect, and neither is visible in the code that does the
computing. `OrderKeySpaceTest` is a property test over generated streams and is the
thing that would catch it; it asserts the property rather than the six paths.

---

## August 27, 2026: the language gate is widened, and what "user facing" means

**Decided.** Two things, both narrowing what the build will accept.

1. `verifyLanguageHygiene`'s British spelling list grows from 15 patterns to roughly
   130, built from three families: a curated `-ise` stem list, a `-yse` stem list, and
   a table of irregulars. It is curated rather than a blanket rule.
2. Em dashes and en dashes remain banned across every `.kt`, `.kts`, `.xml`, `.md` and
   `.pro` file in the repository. The owner's instruction was narrower, that they must
   never appear in anything user facing. The wider rule is kept, and the reason is
   recorded below so nobody relaxes it to match the letter of the instruction.

**Why.** The old list had a hole that a real document fell through.

A note on how this entry is written, because it is the first cost of the wider rule:
**the gate scans this file too, so the banned forms cannot be quoted here.** They are
named by description instead. A rule that applies to every file in the repository
cannot be documented inside that repository without this workaround, and that is worth
knowing before anyone tries to write the next entry about it.

The hole was this. The list matched the British form of "analyze" with a pattern
anchored by a word boundary, and that boundary cannot match inside the British form of
"paralyze", because the stem sits in the middle of the word rather than at its start.
So that word reached a committed document unnoticed and was caught by hand rather than
by the gate.

Widening the list immediately found two more British spellings that were already in
the repository and had passed every previous build: the past tense of "travel" in a
comment in `ui/components/Reorderable.kt`, and the gerund of "label" in the
transcribed addendum. The first had been sitting in shipped code since phase 2.

Three misses is enough evidence that a hand-picked list of fifteen was not a gate, it
was a sample.

On the second point: the reason to keep the dash ban wider than user facing text is
that nothing in this repository stays on one side of that line. A Trail row sentence
lives in `strings.xml`, but the sentence explaining why it is worded that way lives in
a KDoc, the rule behind it lives in a specification document, and the corpus lines the
engine will render live in a `.md` file that is not user facing today and is the
source of user facing text tomorrow. A rule that applies to some files is a rule
somebody has to think about before typing, and this project already asks a session to
hold a great deal in its head. A rule that applies to every file is one nobody has to
think about at all.

**Considered and rejected.**

- **A blanket `is(e|ed|ing)` rule** instead of a curated stem list. Rejected because a
  great many `-ise` words are correct in American English: advertise, exercise,
  surprise, compromise, franchise, improvise, supervise, televise, revise, devise,
  disguise, enterprise. A gate that fails the build on correct copy is a gate people
  argue with, and a gate people argue with gets switched off.
- **Narrowing the dash ban to user facing files only**, which is the literal reading
  of the instruction. Rejected for the reason above. The instruction is satisfied by a
  stricter rule, and where the two differ the stricter one costs nothing.
- **Adding `grey` to the list.** Still absent, still deliberately. The design system
  uses it, MASTER_BUILD_PROMPT 2.9 does not name it, and both spellings are current in
  American English.
- **Removing the `app/src/main/res/raw/**` exclusion**, which holds the two OFL license
  texts. Both are currently pure ASCII, so the exclusion hides nothing today. It stays
  because a third party license text is not ours to edit, and many of them legitimately
  contain a copyright sign or an accented author name. Failing the build on a file we
  are not permitted to change would be a gate with no correct response.

**Revisit if** a third party text lands in `res/raw` that is displayed to a person and
contains a character the rest of the app would be failed for. At that point the
licenses screen is showing something the gate would reject, and the honest answer is
to record that exception in this file rather than to widen the exclusion quietly.

---

## August 27, 2026: Addendum 01, executive function support

**Source:** `docs/addenda/ADDENDUM_01_EXECUTIVE_FUNCTION.md`, a directive from the
owner. That copy is provenance, not authority; it records what was asked for on one
date. What the project will build lives in MASTER_BUILD_PROMPT.md, design-v3.md, this
entry and the issue board.

**Decided.** Adopt the addendum in full. Its Step 2 schema changes land now, while no
user data exists. Its Step 3 platform first rule becomes standing and applies from
this date forward. Everything in Steps 4 through 10 is assigned to a phase and waits
for that phase. Nine conflicts with the existing specification were found. Eight were
resolved by the recording session; the ninth, C6, was put to the owner and decided by
them the same day, so all nine are settled below.

Nothing here is built yet. At the time of writing the project is at the end of phase
3, version 0.3.0, and every item in this entry is pending.

### Why this direction

The addendum came out of research and user panel work on serving people with
executive function challenges: ADHD, autism, brain fog from long COVID or ME/CFS,
cognitive changes in perimenopause, TBI recovery, depression and anxiety, and
burnout.

It states that provenance and does not cite sources or name a market size, so this
entry does not pretend to either. What it does give, and what is worth recording
because it is the checkable part, is a specific mechanism for each change and a reason
that mechanism bites for this audience. Those reasons are the argument, and they are
reproduced here in the addendum's own terms:

- **Widgets matter more than notifications, for a specific reason.** A widget is
  persistent and cannot be dismissed, so it works with out of sight, out of mind
  rather than against it. A notification is a one time event that gets swiped away and
  forgotten. A widget is still there tomorrow
- **Time blindness makes the session's visibility the point, not a nicety.** A focus
  session that exists only inside the app does not exist for someone who cannot feel
  the hour passing
- **The hardest moment is starting, and the title of a task is often the intimidating
  part of it.** Hence the first step field and the widget that shows it instead of the
  title
- **Every decision between the thought and the record is a place the thought is
  lost.** Hence capture that never requires choosing an area
- **Onboarding currently demands up to twelve decisions from people whose central
  difficulty is deciding.** Hence a genuine zero decision path
- **Predictability matters enormously to autistic users, and surprise interface
  behavior is a real cost.** Hence announcing Pulse before it first appears
- **Rejection sensitivity is common in this population, and an observation read as a
  verdict is how someone deletes the app.** Hence the tone pass across every rule that
  concerns a decline, a gap or a neglect
- **A fluctuating condition looks identical to decline in the data.** This one is a
  correctness fix and not politeness. Without a capacity check, the app will tell
  someone with a cyclical or relapsing condition that they are deteriorating, on a
  fixed schedule, forever, and it will be technically accurate every time
- **This audience leaves and returns, and that is normal use rather than failure.**
  Hence the re-entry state, and the rule that a returning user is never greeted by a
  measurement of their absence

Two of the addendum's conclusions are about where the evidence is thin rather than
where it is strong, and they are the reason to trust the rest: it declines body
doubling because the evidence is still thin despite the idea being popular, and it
declines a dyslexia friendly typeface on the same ground, putting the accessibility
budget into text size, spacing and contrast instead. A document that only ever
argued for adding things would be worth less.

### None of this is a rebuild

This is the part that matters most for a later session's judgment, and it is the
addendum's own first claim:

> **None of this is a rebuild.** The core mechanic is unchanged and is already well
> suited to this audience. These are additions and refinements.

A directive of this size arriving after three shipped phases reads like a pivot, and a
session that treats it as one will start rewriting things that are already correct.
It is not a pivot. Nothing in the addendum touches one active item per area,
everything else queued, promotion on completion, the event log as the single truth,
the six engine layers, the corpora, the visual system, or any of the four
non-negotiables in MASTER_BUILD_PROMPT.md 2.

What it changes is narrow and additive:

| what changes | what it was before |
|---|---|
| Capture no longer requires choosing an area | Capture required an area |
| Items gain two optional fields, an estimate and a first step | Title and note only |
| Presence gets a date only marker so a long gap can be detected | No marker existed |
| A focus session gains an extension and an honest early end | Neither existed |
| The reflective layer learns to tell a rhythm from a decline | Decline only |
| Six widgets in v1 rather than two | MASTER_BUILD_PROMPT.md 13.3, two |

Each of those sits at the edge of a mechanic that already does the thing this audience
needs, which is to show one item instead of a list of forty. The addendum closes with
its own guard on this point: **do not let this document pull the build out of order.**

**Revisit if:** never, as a framing. If a later session finds itself proposing a
change to the queue mechanic in the name of executive function support, the proposal
is outside this entry and needs its own argument.

### The four things deliberately ruled out

Addendum Step 9. Each of these is the kind of feature that any session reading the
audience description above will propose, which is exactly why the reason is recorded
and not just the no.

**9a. No body doubling, and no real time social presence of any kind.** It requires
networking, and the no `INTERNET` permission guarantee is the strongest claim this app
makes. That claim is checkable by a person in Android settings, it is checked by
`verifyNoInternetPermission` against the merged manifest on every build, and the whole
privacy section of the README rests on it. Body doubling is popular in the community
and the evidence for it is still thin. Trading a verifiable guarantee for a thin
evidence base is a bad trade at any price.
**Revisit if:** nothing short of abandoning the no network position, which would be a
different app rather than a feature.

**9b. No AI task breakdown.** It breaks two commitments at once: a hosted model needs
the socket the app does not have, and an on device model is still a model, which
MASTER_BUILD_PROMPT.md 2 rules out as a category rather than as a dependency. The
first step field is the deterministic version of the same idea, and it is arguably the
better one, because the user's own words for the first physical action are more use to
them than a generated decomposition of a task the app cannot see.
**Revisit if:** the owner drops the no AI commitment. That is a positioning decision
and not an engineering one, and it would invalidate several pages of the README.

**9c. No streaks, badges, XP, levels, confetti or celebration.** This was already the
rule. design-v3.md 14 says no streak breaking or shame mechanics, no confetti, no
premium badges. MASTER_BUILD_PROMPT.md 12.2 says of the Momentum activity row that
there is no streak and a missed day never resets anything. What the addendum adds is a
second and stronger reason: **streak loss is a documented abandonment trigger for this
audience.** The design position was about tone; this one is about whether the person
still has the app installed next month.

The consequence is already written into the widget family: the Rhythm widget renders
the fourteen day dot row and must never become a streak. No consecutive count, no fire
icon, no invitation not to break a chain. A gap is just a lighter dot.
**Revisit if:** never. This is now held up by two independent arguments, and losing one
would not move it.

**9d. No Live Update for anything but a focus session.** A focus session is a user
initiated, start to end, time bound task, which is the exact case Android's Live
Updates were designed for. Pulse, the Report, a reminder, and anything else the user
did not just start are none of those things. The addendum asks that this constraint be
written into design-v3.md rather than left implicit, so that a later session wanting to
extend the surface has to argue against a written rule instead of filling a silence.
It also sits directly on top of an existing rule in MASTER_BUILD_PROMPT.md 13.4: no
re-engagement notifications, ever, and nothing that exists to pull the user back.
**Revisit if:** never for the constraint. If the platform later offers a promoted
surface for something that is genuinely a user initiated ongoing task, that is a new
case and gets its own entry.

### The nine conflicts, and how each was resolved

The addendum's closing instruction:

> If any item here conflicts with something already in MASTER_SPEC.md, DESIGN.md or the
> corpora, do not silently pick a winner. Flag the conflict, propose a resolution, log
> it in DECISIONS.md, and continue.

Nine were found. The tiebreak used throughout, stated once so it is not re-argued nine
times: **the addendum is the owner's directive and post-dates every document it
contradicts, so where it plainly overrides an earlier scope line by the same author,
the later instruction wins and the earlier line is rewritten rather than left to rot.**
Where it instead collides with a shipped mechanism or with a number the engine reports
about a person, the shipped behavior is examined first, because a document can be
edited quietly and a wrong sentence shown to a person cannot be un-shown.

| id | subject | resolution | decided by |
|---|---|---|---|
| C1 | Document names | Read as aliases, rename nothing | recording session |
| C2 | The addendum fails the language gate | Normalized at transcription | transcription |
| C3 | Six items assigned to closed phases | New phase 3b, runs next | recording session |
| C4 | Two widgets in v1 versus six | Addendum supersedes, 13.3 and 18 rewritten | owner |
| C5 | App shortcuts out of scope | Addendum supersedes, struck from 18 | owner |
| C6 | The `FOCUS_ABANDONED` event name | Renamed to `FOCUS_ENDED_EARLY` | owner |
| C7 | `APP_OPENED` versus `activeDays` | Never counts as user activity | recording session |
| C8 | Nullable `areaId` versus the invariants | Unfiled sits outside area scope | recording session |
| C9 | Overlap with open issue #19 | One schema commit, not two | recording session |

#### C1. The documents the addendum names do not exist under those names

The addendum refers throughout to MASTER_SPEC.md, DESIGN.md, DECISIONS.md and
`tools/audit.py`. This repository has MASTER_BUILD_PROMPT.md, design-v3.md, no
decision log at all, and `audit.py` at the root.

**Resolution:** read them as aliases. MASTER_SPEC.md means MASTER_BUILD_PROMPT.md,
DESIGN.md means design-v3.md, `tools/audit.py` means `audit.py`, and DECISIONS.md is
this file, created by this entry. Nothing is renamed to match the addendum, because
the real names are cited from inside the source comments, the issue board and every
other document.

The alias predates the addendum, which is why it is worth a line rather than a
correction. Until August 27, 2026, `audit.py` carried DESIGN.md in a short whitelist of
known absent references inside its dangling file check, so something in this project's
history had been calling design-v3.md by that name for a while. That whitelist was
removed the same day in favor of asking whether a file exists anywhere in the
repository, which is the question the check was always trying to ask. The consequence
for this file: the alias may be written about, but never in the backticked form the
check reads as a reference to a real document.

This conflict is bookkeeping rather than substance, and is recorded only so that the
next session to read the addendum does not go looking for files that were never there.

**Revisit if:** a document is renamed. The alias line moves with it.

#### C2. The addendum's own text fails this project's language gate

The source document used em dashes throughout, and British spellings, including the
British form of color nine times and the British forms of behavior and paralyzing.
`verifyLanguageHygiene` fails the build on an em dash, an en dash, any character above
U+007F, or a British spelling, across every `.kt`, `.kts`, `.xml`, `.md` and `.pro`
file in the repository. Committing the document unedited would have broken the build
for every later session until someone found it.

**Resolution:** the stored copy was normalized at transcription. Em dashes were
replaced with commas, four spellings were Americanized, nothing else was changed and
no sentence was reworded. The file's own header records exactly that, names itself as
provenance rather than authority, and says the original is held outside the
repository.

The wider point, since this will happen again: **any document arriving from outside
this repository is subject to the gate the moment it is committed.** Normalize it at
the door, record what was changed in the file's header, and keep the original
elsewhere. Do not weaken the gate for an incoming file.

*Note at the time of writing:* the stored copy still contains the British form of
honored twice, in Step 8c. `audit.py` does not catch it because its British form list
is the shorter of the two; the Gradle gate does, because `BRITISH_FORMS` in
`build.gradle.kts` matches on that word. It is a live build breaker and belongs to
whoever owns that file. Strike this note once it is fixed.

**Revisit if:** never. The gate is not negotiable, per CLAUDE.md.

#### C3. Six items are assigned to phases that have already shipped

The addendum assigns 4a the unfiled inbox, 4b the first step field, 4c estimate
capture and 4d re-entry detection to phase 2, 8c calm mode to phase 1, and 8e the once
per session staggered entrance to phase 2. `docs/BUILD_STATE.md` records phases 1, 2
and 3 as done, at version 0.3.0, installed and verified on the device. Phase 4 is
next.

**Resolution:** insert **phase 3b, executive function retrofit**, running next, before
phase 4. It carries all six items plus the Step 2 schema changes, which the addendum
wants in the current phase regardless of where the matching interface lands.

Not reopening phases 1 and 2. A phase in this project is a closed issue with
acceptance criteria that were checked, a version number, and a build installed on a
physical device. Reopening one makes the record of what shipped and when unreadable,
and `docs/BUILD_STATE.md` is the document a cold start trusts most.

Running 3b before phase 4 is a dependency order rather than a preference. Phase 4 needs
`FOCUS_EXTENDED` for 4f, uses the `FOCUS_ENDED_EARLY` name C6 settled for 4e, is
governed by 8d on analog time for its ring, and cannot honor calm mode in its motion
if calm mode does not exist yet.

**Decided by:** the recording session. Pending. The phase 3b issue and the
MASTER_BUILD_PROMPT.md 19 build order edit are not part of this entry.

**Revisit if:** the owner would rather fold these six into phase 4 and accept one
larger phase. The dependency order survives either way; only the accounting changes.

#### C4. The widget count: two in v1, or six

MASTER_BUILD_PROMPT.md 13.3 is explicit: **v1 ships two**, Next Up and All Areas, with
four deferred and listed only so the snapshot format accommodates them, Area Focus,
Action Board, Weekly Momentum and Weekly Insight. It gives its reason plainly: "Glance
is finicky and six widgets is a lot of grinding for early payoff." Section 18 then puts
"The four deferred widgets" out of scope for v1.

Addendum Step 6 specifies eight and requires six in v1: Next Up, First Step, Quick
Capture, Focus Countdown, All Areas and Rhythm, with This Week and One Thing optional
if phase 12 has room.

**Resolution:** the addendum supersedes. 13.3 and 18 are rewritten. The four old
deferred names are superseded by the addendum's naming rather than kept alongside it,
because nothing in the new set maps cleanly onto Area Focus or Action Board, and
carrying two vocabularies for one surface is how a snapshot format ends up with dead
fields nobody dares remove.

The reason this is a supersession and not a coin toss: 13.3's argument was cost against
payoff, and **early payoff** was its operative phrase. The addendum's argument is that
for this audience the payoff is not early, it is central, and it says why, which 13.3
never had to: a widget is persistent and cannot be dismissed. That is a different claim
from "widgets are nice to have", and it beats a cost estimate.

The cost is real and is not waved away. Glance is still finicky, six widgets is still
grinding, and phase 12 additionally carries the Live Update, three app shortcuts and a
quick settings tile. Two of the eight are marked optional for exactly that reason, and
dropping them is the expected release valve rather than a failure.

**Decided by:** the owner, by issuing the addendum. The mechanics of the rewrite are
the recording session's, and are pending.

**Revisit if:** phase 12 runs long. The two optional widgets absorb it. If six in v1
still proves undeliverable after that, the shortfall is a conversation with the owner
and a new entry here, not a quiet reversion to 13.3.

#### C5. App shortcuts were out of scope

MASTER_BUILD_PROMPT.md 18 lists "Home screen shortcuts" among the things out of scope
for v1. Addendum 6i requires three static shortcuts in phase 12, quick capture, start
focus and today's Pulse, built on the androidx.core shortcuts APIs.

**Resolution:** the addendum supersedes and the line is struck from 18. The reasoning
is the same as the widgets, fewer steps between intention and action, and three static
shortcuts are a small piece of work next to the widget family, which is presumably why
18 could afford to drop them and can now afford to keep them.

One ambiguity worth naming rather than resolving silently: "Home screen shortcuts" in
18 may have meant pinned shortcuts placed on the launcher, which is a different feature
from the static long press menu the addendum asks for. The narrow reading is the one
struck. Pinned and dynamic shortcuts stay out of scope, and nothing currently asks for
them.

**Decided by:** the owner, by issuing the addendum. The edit to 18 is pending.

**Revisit if:** nothing. This is settled.

#### C6. The FOCUS_ABANDONED event name. DECIDED: rename to FOCUS_ENDED_EARLY

Addendum 4e: a focus session ended early is a completed short session, the completion
screen says so, and

> The word "abandoned" appears nowhere the user can see it, including in Trail. Rename
> the event if necessary.

**What is already true.** No user visible string in the app contains the word. The
Trail row for that event reads `Stopped after N minutes`. On a plain reading the
requirement is already met and no rename is needed. The word lives only in the event
type name `FOCUS_ABANDONED`, in `FocusCounts.abandoned` in
`domain/query/TrailFacts.kt`, in the payload, in the golden fixture and in the event
format document.

**Why that reading does not settle it.** Phase 11 export, which the addendum itself
specifies at 4h, writes the entire database, every event and all derived state, to one
portable file. It offers an optional password, and when no password is set the file is
readable, and the addendum requires the export screen to say so plainly rather than
imply safety. So the addendum creates a route by which the raw event type names are
user visible, and export is a safety feature people are meant to actually use. Anyone
who opens their own export file reads the word.

**The recommendation, stated and not taken:** rename to `FOCUS_ENDED_EARLY` now, while
the schema window is open. Step 2 exists precisely because a payload change is nearly
free before user data exists and painful afterward, and a type name is worse than a
payload field, because it appears in the log, the reducer, the query facade, the golden
fixture and the format document at once. Doing it now costs one commit that also
regenerates the golden fixture, which C9 says is happening regardless. Doing it later
costs a schema version and a reader that accepts both spellings, forever.

**The argument on the other side, recorded so the owner can weigh both:** internal
identifiers are not copy, and treating them as copy is how a codebase acquires a
euphemism layer. That argument holds right up until the export is readable, which is
the whole difficulty here.

**One point that cuts the same way as the recommendation:** the comment on
`FocusCounts` in `TrailFacts.kt` already argues that the log does not reliably know
that a session was abandoned. A killed process leaves a started session with no
terminal event, so `started != completed + abandoned` is a legal state rather than a
bug, and nothing anywhere is allowed to infer abandonment by subtraction.
`FOCUS_ENDED_EARLY` is the more accurate name on those grounds alone, independent of
what the user can see.

**Decided by the owner on August 27, 2026: rename it to `FOCUS_ENDED_EARLY`**, in the
same schema commit as the Step 2 changes and the issue #19 payload fields, while the
window is open.

**The owner's reason went past the export path**, which had been the whole of the
argument above, and it is the more durable one:

> `docs/EVENT_FORMAT.md` is a public contract that the future Linux desktop app will
> be built against in a separate session. The word teaches the wrong framing to the
> next implementer.

That reframes the question. The export file is a route by which one user might read
the word once. The format document is the specification a second implementation gets
built from, by someone with no other context, and every name in it is an instruction
about what the concept means. A type called `FOCUS_ABANDONED` invites the next
implementer to write a screen that says "abandoned", to add a rule that counts
abandonments, or to infer abandonment by subtraction, which the facade explicitly
forbids. The name is not internal in the way the counter-argument assumed, because it
crosses a process boundary into another codebase.

Scope of the rename: the event type, the payload class, `FocusOutcome.ABANDONED`,
`FocusCounts.abandoned`, the golden fixture, `docs/EVENT_FORMAT.md`,
`MASTER_BUILD_PROMPT.md` 5.2 and 10, and `design-v3.md`. No user visible string
changes, because none contained the word.

#### C7. APP_OPENED versus activeDays

This is the sharp one, and the only conflict in the nine that would have silently
corrupted a number the app states to a person.

**What the addendum adds.** Step 2d: a new `APP_OPENED` event recording the date key
only, no time and no count, written at most once per calendar day. It is the input to
4d, the re-entry state that appears when the app is opened after a gap of fourteen or
more days. The addendum's phrasing is that this is what makes gap detection possible
without any tracking, and that is right: a date key per day is the smallest possible
record that can answer the question.

**What the project already had.** Phase 3 shipped `ClarityEventType.isUserActivity` in
`domain/query/TrailFacts.kt`, and `TrailQueries.activeDays`, `totalEvents` and
`eventsPerArea` all count only events for which it is true. The predicate's own comment
states the reason it exists:

> PULSE_GENERATED, REPORT_GENERATED and PLAN_OFFERED arrive with no user gesture. If
> they counted, someone who opened the app daily and touched nothing would read as
> active on every day of the fortnight, and CLARITY_LOGIC_ENGINE.md 6.1's `quietDay`
> and `quietWeek` families could never fire at all.

**The defect that was one line away from shipping.** `isUserActivity` is written as a
negation: it is true unless the type is one of the three the engine authors itself. A
new event type is therefore user activity **by default**. Add `APP_OPENED` to the enum
without a second thought and merely opening the app marks the day active, at which
point the sentence in that comment stops being the hypothetical it was written to guard
against and becomes a literal description of the app's behavior.

Three numbers would then be wrong, and each of them would still look entirely
plausible:

- **`mo.steady` in CORPUS_3_MOMENTUM.md** fires on active on 9 or more of the last 14
  days and renders lines including `Active {dayCount} of the last fourteen days.`
  Someone who opened the app each morning and did nothing else for a fortnight would be
  told they had been steady
- **`ob.day.l03` in CORPUS_2_REPORT.md 2.13**, `{n} of seven days had activity.`, would
  become a count of app opens presented as a count of activity. The sentence would be
  false about the only subject it names
- **`quietDay` in CLARITY_LOGIC_ENGINE.md 6.1** fires on fewer than two events in the
  window. An `APP_OPENED` plus any single gesture clears that bar, so the family would
  be close to unreachable, and `quietWeek` with it

There is a second order harm that makes it worse. `mo.steady` and the day shape
observations are exactly the surfaces the addendum's own 4d exists to protect a
returning user from. An event added for the sole purpose of detecting an absence would
have become the mechanism that told a returning user they had been present.

**Resolution: `APP_OPENED` is excluded from `isUserActivity`.** It is a presence marker
for gap detection and is never counted as activity, anywhere, by anything. It joins
`PULSE_GENERATED`, `REPORT_GENERATED` and `PLAN_OFFERED` in being excluded.

One precision for whoever implements it. The current spelling of the predicate is
`isUserActivity = !isEngineAuthored`, and that equivalence stops being true here.
`APP_OPENED` is not engine authored; it is written by the app shell on launch. Folding
it into `isEngineAuthored` to get the right answer would record a false reason in the
name, and the comment on that predicate is the thing a later session actually reads.
The shape is the builder's call. The constraint is that the exclusion is exhaustive and
the reason is written down accurately.

**The standing rule that comes out of this, which is the more valuable half:** every
new event type is classified against `isUserActivity` deliberately, in the same commit
that adds it, with the classification argued in the comment. A default-inclusive
predicate is safe only while somebody is looking at it.

**Raised but not decided:** whether `APP_OPENED` renders a Trail row at all. A daily
"opened the app" line would be noise in a chronological log, and it would also be a
visible tally of presence, which 4d says never to show. That is a question for the
phase 3b issue, not a decision made here.

**Decided by:** the recording session, on the strength of the argument the shipped
comment in `TrailFacts.kt` had already made about the three engine authored types.
Pending implementation in phase 3b.

**Revisit if:** never, for the counting question. If some later feature genuinely needs
a count of app opens, it reads `APP_OPENED` directly, which is what the event is for.

#### C8. A nullable areaId versus the replay invariants

Addendum 2c gives items an unfiled state: `ITEM_ADDED`'s `areaId` becomes nullable, and
a new `ITEM_FILED` event moves an unfiled item into an area. 4a gives the reason,
capture must never require a decision, and sets the limit, an unfiled item can be
filed, edited or deleted but cannot be active or completed until filed.

MASTER_BUILD_PROMPT.md 6.2's invariants are written as though every item has an area:
at most one `ACTIVE` item per non-deleted, non-archived area, and every queued item in
an area has a distinct `orderKey`. `ClarityState.ItemState.areaId` and the Room
`ItemRow.areaId` are both non-null. The engine's facts are area scoped throughout, and
the Trail's `eventsPerArea` already drops an event whose area cannot be resolved rather
than bucketing it under a placeholder, because a placeholder would eventually be
printed and CLARITY_LOGIC_ENGINE.md 1 is blunt about what one fabricated area name
costs.

**Resolution:** an unfiled item sits outside every area scoped invariant and outside
every engine fact. It cannot be `ACTIVE` and cannot be `COMPLETED` until `ITEM_FILED`
gives it an area, which is the addendum's own rule rather than an addition to it. Room
migrates to schema 3. MASTER_BUILD_PROMPT.md 6.2 gains the qualifier in writing rather
than by implication, because an invariant that is silently conditional is an invariant
nobody can check.

**Considered and rejected: a synthetic inbox area.** A reserved area id holding every
unfiled item satisfies all four existing invariants with no nullable column, no
migration and no new state. It is the obvious answer, and design-v3.md 15 says the
obvious answer is not the answer unless it is genuinely best. It is not. A synthetic
area leaks into every place areas are enumerated: the Areas screen, the All Areas
widget, `eventsPerArea`, area count facts, the color palette, archive, delete, and the
area scoped engine families. Each of those becomes a special case that a later session
has to know about and one of them will eventually be missed, and the failure mode is a
fabricated area name in a rendered sentence. A null is one thing to remember, and the
type system raises it at every call site rather than waiting for a corpus line to
print it.

**Decided by:** the recording session. Pending. The schema half lands in the Step 2
commit and the interface half in phase 3b.

**Revisit if:** a second unfiled-like state ever appears. Two nullable states would
argue for a proper item location type rather than a null, and that would be a new
entry.

#### C9. Overlap with open issue #19

Issue #19, open and labeled `engine`, was raised during phase 3: the Pulse and Report
payloads cannot produce the `FiringHistory` the engine requires. `PulseGenerated`
carries no `subjectId`, `PulseAnswered` has the same gap, which disables
`selfReportVsData`, and `ReportSectionSnapshot` carries no family key, variant key,
stage, register or subject, so the variant exclusion, the family cooldown and
`hardStretch`'s six week limit are all underivable. Its stated urgency is word for word
the reason the addendum gives for Step 2: it is a change to the event format contract
and it is cheapest to settle before a real log exists.

**Resolution:** they land in one schema commit. Two commits mean two golden fixture
regenerations, and the event format document is explicit that a change to either
fixture belongs in a diff someone argues for rather than in a quiet commit. One argued
diff is better than two, and the second one always gets less scrutiny than the first.

Both open questions gate that commit and should be answered together: #19 still needs
the owner's decision on the exact fields, `subjectId` and probably `subjectKind` on the
Pulse events, and either the five fields on `ReportSectionSnapshot` or a parallel
rendered variant record list on `ReportGenerated`. C6 above was decided by the owner
on the event name. Both touch the same commit and the same fixture.

**Decided by:** the recording session, for the pairing only. The field shapes remain
the owner's call, per the issue.

**Revisit if:** #19 is answered in a way that raises `schemaVersion` and requires a
reader that accepts both shapes. At that point the ordering stops mattering and the two
can separate.

### Platform first, and where the per component records go

Addendum Step 3 makes Google's components and assets the default and custom the
fallback, in four steps: the official Material 3 Expressive component unmodified, then
themed with our tokens, then extended, then custom. Custom is named as a legitimate and
expected outcome rather than a failure. The reasons the platform comes first are stated
as accessibility, RTL, dynamic type, predictive back and motion physics arriving
correct for free, hardware acceleration and testing at a scale one person cannot match,
and a codebase small enough for one person to maintain.

**The rule requires a record either way**, which is the part that concerns this file.
When a component goes custom, note what was checked and which of four reasons applied:

| # | reason to go custom |
|---|---|
| 1 | No platform equivalent exists |
| 2 | The platform component carries meaning the app rejects: achievement, scoring, celebration, or progress toward a target |
| 3 | The platform component fights a design rule: two separation devices on one element, a colored edge treatment, an all caps label |
| 4 | The platform component is worse for this audience: motion or saturation that theming cannot tame |

And what is explicitly **not** a reason: preferring the look of something you would
draw yourself. That instinct belongs in the polish pass at 3c, which works through
theming rather than replacement.

**Decided: those records go in the standing register at the foot of this file, one row
each, append only.** Not as dated entries, because they accumulate a line at a time
across a dozen phases and would bury every substantive entry between them. Not in
design-v3.md, because that document says what a component is and what its dimensions
are, not what was checked before someone built it. The register is the file's one
exception to reverse chronology and is named as such in the header above.

Two things the register is not. It is not a discouragement: the addendum says plainly
that reaching the custom conclusion for a given component is the rule working, not a
conflict, and the register exists so a later session does not redo an analysis someone
already did. And it is not free: a custom component inherits the platform's
obligations, accessibility, RTL, dynamic type, reduce motion and calm mode, to the
same standard the platform component would have met. That inheritance is the real cost
of going custom and it is why the platform comes first when it fits.

The companion rule at 3d, verify current library versions at build time rather than
trusting any version named in any document, is a build practice rather than a decision.
It belongs with the phase that integrates the library.

**Revisit if:** a Material release ships a genuine equivalent for something in the
register. Every row is dated so that this can be checked rather than assumed.

### Positioning, and why the wording has legal weight

Addendum Step 10, recorded now, applied in phase 13.

**The distinction the whole section turns on: Google Play's health policy triggers on
claims, not on keywords.** Using the words people actually search for is permitted.
Stating that the app does something clinical is not. These are two different acts and
the policy treats them differently, which is what makes it possible to reach this
audience honestly without making a single medical claim.

**Permitted and encouraged** in the long description and the keywords: ADHD, autism,
executive function, executive dysfunction, task paralysis, time blindness, brain fog,
neurodivergent, overwhelm, procrastination, focus, one thing at a time. The addendum
gives the register it wants: "Built for people who find long lists paralyzing. Designed
with ADHD, autism and brain fog in mind."

**Forbidden**, verbatim from the addendum, anywhere in the listing, the app copy or any
marketing: treats, manages, cures, therapy, therapeutic, clinically proven, medically,
symptoms, diagnosis, disorder used as a claim, and any statement that the app improves
or reduces anything clinical.

**Required**, in the store listing and in About, per MASTER_BUILD_PROMPT.md 14.4:

> Clarity Now is a productivity tool. It does not provide medical advice, diagnosis or
> treatment.

Google Play's Health Apps Declaration must be completed. With zero data collection, no
accounts, no health data access and no network permission, this app should certify
cleanly, and the source being public means every one of those claims is checkable
rather than asserted.

**The part of this that ages, and the reason it is recorded as a decision rather than
as a fact:** the current Play Console requirements must be read at the time of listing
rather than trusted from any document, including this one. The policy changed through
2025 and adds medical device labeling in January 2026. An entry dated August 2026 is
not evidence about a form submitted in 2027.

**Revisit if:** the Play health policy changes again, which this entry assumes it will.
The permitted and forbidden lists above are the addendum's, and a change to either is
an owner decision and a new entry.

### Revisit if, for the direction as a whole

There is no measurement that could reopen this, and that is deliberate rather than an
oversight: the app collects nothing, reports nothing and will never have telemetry, so
there is no number that could arrive later and argue against the direction. What could
reopen it is the owner deciding the audience framing is wrong, or a specific addition
proving genuinely unbuildable in this stack. The addendum asks that the second be said
plainly before starting rather than discovered halfway through a half-built feature,
and that instruction is adopted here: an item that cannot be built is raised as an
issue and an entry, not quietly dropped.

Each conflict above carries its own revisit condition. C6 is not a revisit condition,
it is an open question, and it is the owner's.

---

## Standing register: platform first records

Append only, oldest first, one row per component built by hand rather than taken from
the platform. The four reasons are numbered in the platform first section of the
August 27, 2026 entry above. A row is added in the same commit that builds the
component, and its state is updated when that component ships.

This register exists so that a later session can see the platform was considered and
does not have to redo the analysis. It is not a list of exceptions to apologize for.

| date | component | reason | state |
|---|---|---|---|
| August 27, 2026 | Depleting focus ring | 1, no platform equivalent exists | built, phase 4 |
| August 27, 2026 | Week ribbon, in the Report | 1, no platform equivalent exists | pending, phase 8 |
| August 27, 2026 | Fourteen day rhythm dot row | 1, no platform equivalent exists | built, phase 6, in the Pulse; Momentum's own row is phase 7 |
| August 27, 2026 | Area color wash | 1, no platform equivalent exists | built, phase 2 |
| August 27, 2026 | Two stage color picker | 1, no platform equivalent exists | built, phase 2 |
| August 27, 2026 | Tutorial spotlight | 1, no platform equivalent exists | pending, phase 10 |

All six are named in Addendum 01 Step 3a as components with no platform equivalent,
with the instruction not to contort a platform component into their shape. Two of them
were already built by hand in phase 2, before the rule existed, and are recorded here
so the register is complete rather than only forward looking.
