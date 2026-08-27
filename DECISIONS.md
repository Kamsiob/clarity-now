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
| August 27, 2026 | Depleting focus ring | 1, no platform equivalent exists | pending, phase 4 |
| August 27, 2026 | Week ribbon, in the Report | 1, no platform equivalent exists | pending, phase 8 |
| August 27, 2026 | Fourteen day rhythm dot row | 1, no platform equivalent exists | pending, phase 7 |
| August 27, 2026 | Area color wash | 1, no platform equivalent exists | built, phase 2 |
| August 27, 2026 | Two stage color picker | 1, no platform equivalent exists | built, phase 2 |
| August 27, 2026 | Tutorial spotlight | 1, no platform equivalent exists | pending, phase 10 |

All six are named in Addendum 01 Step 3a as components with no platform equivalent,
with the instruction not to contort a platform component into their shape. Two of them
were already built by hand in phase 2, before the rule existed, and are recorded here
so the register is complete rather than only forward looking.
