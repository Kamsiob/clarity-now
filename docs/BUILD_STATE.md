# Build state

Where the build actually is. Updated at the end of every phase.

**Last updated:** August 27, 2026, end of phase 6.
**Phases 4, 5 and 6 are all built and all awaiting the check that closes them.**
Everything this file says about focus sessions, about the engine and about the Pulse is
true of the source and is not yet true of anything installed.
**Version:** 0.7.0, versionCode 700.
**Installed and verified on:** Pixel 8 (`shiba`), over USB, last at the end of phase 3c.

**The device check for phase 6 has not run, and it did not fail: it was not attempted.**
The Pixel was in use by another session for the whole of this phase, and no `adb` command
was issued from it. The orchestrator runs the closing build, install and device pass, so
every claim in the phase 6 section below is a claim about source and unit tests. **Three
integration gaps are listed there that the closing build will find on its own**, one of
which stops the app compiling, and they are named in advance so nobody spends the build
diagnosing them.

**A note on the format of the lines above**, kept because it keeps being needed. Two
successive edits once left this block stitched from two states, with each paragraph's
first line replaced and its continuation lines orphaned, and the file then claimed a phase
was awaiting a check it had already passed. **A status line stitched from two states is
worse than either of them**, and the way to edit these is to replace the whole block.

---

## Phases

| phase | state | issue |
|---|---|---|
| 1. Foundations | done | closed |
| 2. Core mechanics | done | closed |
| 3. Trail | done | closed |
| 3b. Executive function retrofit | done | closed |
| 3c. Design foundations, the polish pass | done | closed |
| 4. Focus sessions | built, awaiting the device check | #2 |
| 5. Engine skeleton and simulator | built, awaiting the closing build and install | #3 |
| 6. Pulse | built, awaiting the device check and three integration gaps | #4 |
| 7. Momentum | not started | #5 |
| 8. Snapshots and the Report | not started | #6 |
| 9. Corpus | not started | #7 |
| 9b. Guidance, layer six | not started | #8 |
| 10. First run | not started | #9 |
| 11. Settings, About, data | not started | #10 |
| 12. Widgets and notifications | not started | #11 |
| 12b. Design surfaces, the polish pass | not started | #54 |
| 13. Ship | not started | #12 |

---

## What the device check found in phase 3b

Run on the Pixel 8 at 0.4.0, versionCode 400, with `adb logcat` checked after every
step. **Zero fatal exceptions.**

Verified by hand, not only in tests:

- The Room 2 to 3 migration against the **real** database. The existing area and its
  whole history survived, which is the check a migration test cannot make
- Capture with no area chosen: the add sheet offers a title, a note, a first step and
  an estimate, says `This goes to your inbox`, and commits with no area decision
- The `Inbox 1` chip appears on Areas only when the inbox is not empty. Plain text,
  no badge, no dot
- Filing: the chooser states `Becomes the active item` before committing, and filing
  into an idle area promotes in the same transaction
- The inbox empty state reads `There is no hurry to file it`
- Calm mode, reached through the OS reduce motion setting: the wash desaturates while
  the 7dp identity dot, the area label and `actionBlue` keep full saturation, which is
  16.2's exclusion list holding

Four defects were found and fixed before the phase closed, all of them in the seam
between two agents rather than in either one's work: a `Modifier.padding` overload
that does not exist, a promotion cue that snapped instead of crossfading under calm
mode (so the card never said which item had completed), a float precision assertion,
and an enumeration test that had not seen a new call site. A fifth was a genuine test
defect: three invariant tests built their attempt event before their fixture, so the
attempt sorted first and was applied to an empty log, and one of them was passing for
the wrong reason.

---

## What the device check found in phase 4

Run on the Pixel 8 at 0.5.0. **Zero fatal exceptions** across the whole session
lifecycle. The two criteria the specification insists are verified by gesture rather
than by reasoning both pass.

- **Back mid session leaves the session running.** The Areas card showed the
  intensified wash and `In focus, 25 minutes left`, with no bar, and the ongoing
  notification stayed posted. This is called out twice in the specification because
  ending the session on navigation is the obvious implementation and it is wrong
- **A force stop mid session restores it.** Killed at 24:38 and relaunched straight
  into the focus screen at 23:46, with the time that passed while the process was
  dead correctly accounted for and the arc depleted to match
- `POST_NOTIFICATIONS` was requested on the first session start, never at launch
- **Add 10 minutes did not restart the session and did not return the arc to full.**
  23:46 became 33:18 and the elapsed gap stayed where it was, against the new longer
  total
- Ending past 60 seconds showed `End this session?` with `Keep going` before `End`
- **A session ended early after two minutes reads `Session complete`.** Not
  abandoned, not ended early, nothing implying failure. Addendum 01 4e, and it is the
  most important sentence in the phase
- The Trail rendered all three focus rows, and each resolved its item title. That is
  the dangling preposition defect adversarial review found in phase 3 and which was
  fixed before any focus event existed to expose it

---

## What the device check found in phase 6

Run on the Pixel 8 at 0.7.0, logcat clean.

**The engine spoke, and what it said was true.** On the first foreground of the day it
generated from the `concentration` family and wrote `PULSE_GENERATED`. The sheet read:

> Everything yesterday was Work by.
> On purpose, or it just happened?

Yesterday's activity really was all in that one area, which is the only area, so the
observation is correct rather than merely plausible. That sentence traces the whole
path the project was built around: `TrailQueries` to the fact extractor, the catalog,
the selector, the realizer, the validator, then the log, then the screen. Every number
from a query and every word from a corpus.

- Two response options, not three, because this is not `quietDay`
- The chip carried the amber dot **and** the changed label `Today's Pulse`, which is
  design-v3.md 13's rule that color is never the only signal
- After answering, the chip reads `Pulse` with no dot, so the ready state cleared on
  both signals rather than one
- Ambient mode showed the 14 day rhythm row with today filled amber and the earlier
  days faint, the retained observation, `You answered A push`, and the History entry

**One blocker was found and fixed before it could ship.** The three corpus files were
never packaged into the APK, so the catalog would have parsed nothing and the Pulse
would have been silent forever on a real device while every unit test passed. They are
now copied into the assets by a build step rather than duplicated into the source tree,
because a second copy is two corpora that drift and the shipped one would not be the
one anybody reviewed. It goes through AGP's Variant API, since the source set API
refuses a provider and a plain `srcDir` would not carry the task dependency.

---

## Addendum 01, executive function support

**Arrived August 27, 2026.** A directive from the owner, out of research and user
panel work on serving people with executive function challenges. It is recorded in
full, and phase 3b built the first six of its items. The rest waits for its phase.

**Where it is recorded, because a cold start needs to find it:**

| what | where |
|---|---|
| the source document | `docs/addenda/ADDENDUM_01_EXECUTIVE_FUNCTION.md`, provenance only |
| behavior and data | `MASTER_BUILD_PROMPT.md` 14b, plus 3.2, 13.3, 13.5, 16.11, 18, 19 |
| everything visual | `design-v3.md` 16 calm mode, 17, and edits through 7 to 15 |
| why, and the nine conflicts | `DECISIONS.md`, the August 27 entries |
| what remains | issues #22 to #51, all labeled `addendum:01` |

**None of it is a rebuild.** The core mechanic is unchanged. These are additions.

### What actually changed in the code

First the schema alone, because the addendum marks Step 2 urgent: a payload change is
nearly free before user data exists and painful afterward. Then phase 3b, which is
written up below.

The catalog went from 24 event types to 28, `FOCUS_ABANDONED` was renamed to
`FOCUS_ENDED_EARLY`, `ITEM_ADDED` gained a nullable area and two optional fields, and
the issue #19 payload fields landed in the same window. Room went to schema 3, and the
migration was verified against the real database on the phone rather than only in a
test: the existing area and its whole history survived.

### One shipped defect found while doing it

Teaching the replay harness to file items out of the inbox exposed a bug class that
shipped in 0.2.0: an order key was chosen against the entities currently in view
rather than against everything that can occupy the ordering space. Six paths had it.
The plainest instance is that **the second item added to any fresh area took the same
order key as the first**, because the queue is empty at that moment and the active
item was not being looked at.

Nothing looks wrong when it happens. It surfaces much later as a crash on a drag.
`DECISIONS.md` has the full write up and `OrderKeySpaceTest` is the property test that
now holds the line.

### The nine conflicts

Eight were resolved by the recording session and one, the event rename, was put to the
owner and decided the same day. All nine are written up in `DECISIONS.md`. The two
worth knowing without opening that file:

- **`APP_OPENED` must never count as activity.** Phase 3 shipped `isUserActivity`, and
  `activeDays` counts only user activity. Had the new event counted, opening the app
  and doing nothing would have marked a day active, `mo.steady` would have fired for
  someone who did nothing for a fortnight, and `quietDay` would have been nearly
  unreachable. It is a presence marker for gap detection and nothing else.
- **Six of the addendum's items were assigned to phases 1 and 2, which are shipped.**
  Hence phase 3b, inserted before phase 4, which depends on part of it.

### Two open questions that are not the addendum's

#20 and #21 predate it and still need the owner. #19 was folded into the schema window
rather than left waiting.

---

## What works today

Verified by hand on the device, not only in tests.

- Create an area, name it, pick its color through the two stage picker with the
  live preview card
- Add an item. It becomes active immediately in an idle area, or joins the queue,
  and the add sheet says which before you commit
- Complete the active item by swiping right, from the area detail sheet, or through
  the accessibility action. The queue head is promoted with the hero animation
- Swap, through a full left swipe or the chooser, naming the item being demoted
- Delete an item with a five second undo, during which nothing is written to the log
- Delete an area behind a typed confirmation
- Archive an area from the detail sheet
- Edit a queued item, including move to front
- Reorder areas by long press and drag, one event written on release
- The floating tab bar, with the current destination expanded to icon plus label
- The Trail: filter chips, day grouping with an inline count, ten minute timestamp
  clustering, the mint completed row, and pagination by fourteen local day windows

**Also working, added in phase 3b and checked on the device when that phase closed.**

- Add an item without choosing an area. It goes to the inbox, the Areas header carries
  a plain `Inbox 3` chip while anything is in there, and filing it into an area is one
  tap from that sheet, which says where the item will land before you commit
- Give an item a first step and an estimate, on the add sheet or the edit sheet. Both
  are optional, both are the last fields on the sheet, and nothing anywhere asks for
  either
- Calm mode, which today follows the OS reduce motion setting because there is no
  Settings screen yet: less color in every wash, no list entrance, a shimmer that
  holds still, and a bottom sheet that still travels
- The list entrance fires once per tab per app session rather than on every tab
  switch. Switching tabs twenty times should show it twice, once for Areas and once
  for the Trail

**Built in phase 4 and not yet checked by hand on the device.** They are in the code
and under test, and the closing step in `MASTER_BUILD_PROMPT.md` 16.8 is what moves
them into the list above.

- The **Focus chip** in the Areas header, which opens a chooser listing every area,
  with the ones that have no active item dimmed, inert and reading `Add an item first`
- A **focus session**: the indigo room, the 240dp ring depleting clockwise from the
  top, the timer numeral inside it, `remaining` beneath the numeral, `End session` and
  `Add 10 minutes`. Six elements and nothing else
- The session **survives the app being killed**. The end instant is stored per device
  and the remaining time is recomputed from the log, so a relaunch lands back on the
  ring at the right number
- **Back leaves the session running.** It does not end it, prompt or warn. The ongoing
  notification is the way back in and the area card carries the countdown meanwhile
- The **ongoing notification**, silent and low importance, with a countdown
  chronometer, `Add 10 min` and `End` that both work without opening the app, and a
  body that reopens the session
- The **Live Update** on Android 16 and later, one depleting track in the area color,
  degrading to the chronometer above with no word to the person about it
- **Ending early is a completed short session** in the same words a full one uses.
  Under a minute it is discarded silently; past a minute it asks once, `End this
  session?`, with `End` and `Keep going` at equal weight
- The **area card** shows the intensified wash and a live countdown while a session
  runs on it, with no bar anywhere
- Calm mode reaches all of it: the gradient keeps its geometry and loses its chroma,
  the glow holds still, the bloom becomes a check appearing, and **the arc still
  depletes**, because it is information

## What is deliberately not there yet

Each of these is a phase, not an oversight. See the linked issue for why.

- The **Pulse chip** in the Areas header, which arrives with Pulse in phase 6 rather
  than sitting inert. The Focus chip beside it landed in phase 4 and is permanent.
- The **`Session length` selector** and the **`Five minute warning`** row, both under
  Focus in Settings, phase 11, issue #10. The preferences behind them are built and
  honored, so every session today is the stored default of 25 minutes and the
  transition warning is off and reachable only by writing the key by hand.
- The **settings glyph** in the Areas header, which arrives with phase 11.
- The **archived areas view**, issue #15. Archiving works and removes the area from
  the list, but there is currently no way back. Worth knowing before archiving
  anything you care about.
- **Momentum and Report** render one honest line rather than a skeleton, issue #16.
  The Trail no longer does.
- The **weekly banner** on Areas, because its sentence comes from the engine. The
  engine now exists and produces that sentence; the surface that shows it is phase 7.
- The **re-entry surface**, the phase 6 half of issue #27. Detection is built and
  nothing renders it. The surface has to be able to say nothing, and saying nothing is
  an engine decision; the engine can now say nothing, and the screen is phase 6.
- **Everything the engine says**, which is all of it. Phase 5 built layers 1 to 5 and no
  caller, so no sentence the engine produces has reached a device. Phase 6 is the first
  screen that asks it anything.
- The **`Calm mode` Settings row**, phase 11, issue #10. The setting behind it is built
  and honored everywhere in the app. Until the row exists the key has no stored value,
  which means calm mode follows the OS reduce motion setting, which is its specified
  default.

## Known defects and open questions

- **WorkManager pulls three permissions into the merged manifest**:
  `FOREGROUND_SERVICE`, `WAKE_LOCK`, `RECEIVE_BOOT_COMPLETED`. None is a network
  permission and the no internet gate passes on both variants. But
  `MASTER_BUILD_PROMPT` section 18 says no permission beyond notifications is in
  scope for v1, and WorkManager is required by section 3 for the Pulse reminder and
  the widget refresh. **Open since phase 2, and phase 6 is the first thing that
  really uses it.** The merged manifest, debug and release, today declares six
  permissions in total: `POST_NOTIFICATIONS`, `POST_PROMOTED_NOTIFICATIONS`,
  `VIBRATE`, `FOREGROUND_SERVICE`, `WAKE_LOCK` and `RECEIVE_BOOT_COMPLETED`.
  `ACCESS_NETWORK_STATE`, which work-runtime also declares, is removed with
  `tools:node="remove"` and appears in neither. Phase 6 checked that removal against
  work-runtime 2.11.2 rather than assuming it: a request with no constraints never
  reaches the network tracker, so **adding any constraint to any work request in this
  app means putting that permission back**, which changes what the privacy policy
  invites people to verify. **Still the owner's call, and it is now due at phase 11**,
  where the privacy sheet and the permission card have to show this list to a person.
  `DECISIONS.md` carries it as open with the recommendation stated and not taken.
- **The re-entry surface is not built and has no phase.** `MASTER_BUILD_PROMPT.md`
  14b.4 and `design-v3.md` 11.2 assigned it to phase 6, phase 6 built the two engine
  side rules that follow it and did not build the screen. Both documents now say so at
  the point a session would otherwise read them as shipped. A person returning after a
  fortnight today sees the ordinary Areas screen and a Pulse that stays quiet for two
  days: nothing measures their absence, and nothing greets them. **Assigning it is the
  owner's call.** The two candidates are a phase of its own, since it is one screen and
  it is finished when it works, or phase 10, which already owns the first thing a person
  sees.
- **Three integration gaps from phase 6, one of which stops the build.** `ClarityShell`
  does not pass the `onOpenPulse` callback that `AreasRoute` now requires, `MainActivity`
  does not route `PulseIntents.opensPulse`, and the three corpus files are not packaged
  into `assets/corpus/`, which would make every day come back `Unavailable` on the
  device. All three are described in the phase 6 section below, with where each call
  goes.
- `VIBRATE` is declared, and has to be, because `design-v3.md` section 9 specifies
  sixteen haptic events. It is a normal permission with no prompt.
- Material's `MotionScheme` is internal in material3 1.5.0-alpha26, so Clarity's
  springs cannot be injected into Material components yet. Issue #13.
- **The queue promotion snaps when motion is reduced, and therefore in calm mode.**
  `AreaCard` sets both titles to their end state at once, so the struck through
  outgoing title is never seen and the card stops answering "which one did I just
  finish". Calm mode removes motion, not information, and this removes information.
  It shipped in phase 2 and calm mode inherited it rather than causing it.
  `design-v3.md` 16.6 item 1 specifies the crossfade that keeps the answer and marks
  that row as the one line in the table not yet true in the code.
- **`inkSecondary` measures 4.29 to one on an in session card** in light mode, under
  `design-v3.md` 13's 4.5 floor. Nothing draws it there today, and raising the token
  would be a change to every screen in the app rather than a calm mode change, so it
  is pinned in `CalmModeContrastTest` instead: the day a caption lands on an in session
  card the build fails, rather than the screen quietly missing the floor.
- **Nothing can set the stored `calmMode` key yet.** `MainActivity` reads it and
  passes it to the theme, which phase 3c wired, but until the phase 11 Settings row
  exists nothing writes it, so it is always absent and calm mode follows the OS reduce
  motion setting, which is its specified default. `CalmModeTest` asserts the
  resolution rule in the meantime. This entry previously said `MainActivity` did not
  pass the value, which stopped being true in phase 3c.
- **The soft tone at a natural completion is not built.**
  `MASTER_BUILD_PROMPT.md` 10 asks for a soft tone and the `focusEnd` haptic. The
  haptic fires. Nothing makes a sound while the person is looking at the ring, and the
  only audible completion is the notification posted when the app is somewhere else,
  which carries the phone's own notification sound through the Focus channel. Recorded
  on issue #2.
- **The `transitionWarn` haptic is not built**, which is the one row in
  `design-v3.md` 9's table of seventeen with no implementation. `ClarityHaptics` does
  not carry the event, and the in app signal the notifications layer publishes for
  that moment has nothing collecting it. Everything visible about the five minute mark
  is built. Recorded on issue #30.
- **`transitionWarningStep` has no test**, and it is the piece of the transition
  warning that is worth one: it is a pure function of three values, its own KDoc says
  so, and issue #30 asks by name for a test that a five minute session fires nothing
  and a session extended twice fires exactly twice. The mark's own arithmetic is
  covered in `FocusSessionTest`; the arming rule is not.
- **A session whose planned time runs out while the process is dead posts nothing.**
  There is nothing alive to post it. The ongoing notification stays in the shade with
  its chronometer at zero, because the platform draws that without help, and the
  session resolves at the next resume, which is the other half of what
  `MASTER_BUILD_PROMPT.md` 10 allows. The fix would be an alarm: an exact one needs a
  permission section 18 puts out of scope, and an inexact one can be held by Doze for
  a quarter of an hour and would announce the end of a session long after it ended.
  **Telling someone with time blindness the wrong time is worse than telling them
  nothing.** Recorded here rather than hidden in a comment.
- **`FOCUS_ENDED_EARLY` from the notification action has no confirm**, where the same
  act on the focus screen asks once past sixty seconds. Addendum 01 5c requires the
  action to work without opening the app, and a confirm there could only be a second
  notification or a screen, so the two requirements cannot both be met on that
  surface. A notification action is already a deliberate tap on a labeled control
  rather than a gesture that can complete by accident.
- **The platform bottom sheet does not honor calm mode.** Its entrance and dismiss are
  Material's own, they honor the system animator scale and therefore reduce motion, and
  they expose no hook an app preference can reach. Recorded as a decision in
  `DECISIONS.md` and in `design-v3.md` 16.8 rather than left to be rediscovered.
- **Three corpus totals disagree with the files beneath them**, found by counting at
  catalog load rather than by reading. `CLARITY_LOGIC_ENGINE.md` 11.1 states 162 Momentum
  and banner lines and 1,519 authored lines in total; the files carry 146 and 1,503.
  `CORPUS_3_MOMENTUM.md` claims 112 Momentum headlines in two places and carries 96, and
  inside `CORPUS_2_REPORT.md` section 1's prose says 176 headlines against its own table's
  158 and section 3's says 128 patterns against 111. **Left as written**, because whether a
  file grows to its total or a total is corrected to its file is a phase 9 question and a
  builder quietly editing a specification to match what the code counted is the move the
  authority order exists to prevent. The count is recorded at the point of the claim in
  11.1 and the recommendation is in `DECISIONS.md`.
- **Six of the ten simulator checks in `CLARITY_LOGIC_ENGINE.md` 12 fail**, by design and
  with a date and an issue on each. They are not defects of phase 5 and they are listed
  here so nobody has to rediscover which ones are expected. The numbers they measured are
  in the phase 5 section below. **`accumulation` never reaching stage 2 across a year of
  eleven personas is the one worth a second look**, because the other six failures are bench
  depth and that one is a rule that may never fire.
- **Three Pulse families have rules that never fired**, `throughput`, `burst` and
  `queueDrain`, across eleven simulated years. Either the personas do not produce the
  shape, or the criteria are tighter than the corpus stage headers they were built from.
  Phase 9 has to know which before it grows those benches.

---

## Phase 6 delivered

The Pulse, issue #4. `MASTER_BUILD_PROMPT.md` 11.3, 11.6 and 12.1, `design-v3.md` 11 and
10.1, `CLARITY_LOGIC_ENGINE.md` 6.1 and 6.2, and `CORPUS_1_PULSE.md`.

**This is the first screen in the app that renders a sentence the engine wrote about a
person's own life.** Everything shipped before it was a fixed label or a readout of a
number the app had just counted. Nothing in this phase composes a sentence, shortens one,
reformats one or supplies one when the engine had nothing to say: the observation, the
question, the response labels and the acknowledgment are all corpus lines, selected,
realized and validated by the engine and stored on the event, and a silent day shows two
fixed lines that describe how the Pulse works and say nothing whatever about the person's
week.

- **The generation lifecycle**, `domain/pulse/PulseGenerator.kt`. The eight steps of 11.3
  with their numbers as comments, plus one added step numbered 2b rather than hidden, for
  the re-entry suppression in 14b.4, which postdates the sequence. **It has no clock and
  no way to write**: the instant arrives as a parameter and it returns a decision the
  caller commits, so there is no path by which a silent day, a suppressed day or an
  already answered day can leave a row behind.
- **The plumbing around it**, `domain/pulse/PulseCoordinator.kt`. The catalog is built once
  and held, per 11.7, and the firing history is rebuilt inside the generator on every
  invocation and never cached, because it derives from a log that merges. The corpus is
  read through a `CorpusSource` seam, from packaged assets on the phone and from the
  committed files in tests, so the file an author edits is the file the app reads.
- **Two writes and one read on the repository.** `recordPulseGenerated` takes the check
  and the append under one lock, so a second entry for one date key cannot be produced by
  two foregrounds racing at launch. `answerPulse` stores `responseLabel` **verbatim** off
  the whole `ResponseOption`, so a label reworded in a later release cannot rewrite what an
  old answer said.
- **The sheet**, `ui/pulse/`. The amber night behind it, the observation in `readSerif`
  centered, the question in `body` at `textDim`, stacked response pills that fill with
  amber from the tap point over 220ms while the unselected pill fades to 30 percent and
  drops 4dp, the acknowledgment after a 250ms hold, then the settle into ambient mode: the
  14 day rhythm row, today's answered card, and a History entry.
- **The chip**, `ui/areas/PulseChip.kt`. The 6dp `warnAmber` dot from `design-v3.md` 10.1,
  and the changed label beside it, because 13 says color is never the only signal. It reads
  `Pulse` at rest and `Today's Pulse` when one is waiting.
- **The reminder**, `work/` and `notifications/PulseReminderPoster.kt`. A chain of one time
  requests, each arming the next against the clock and its zone, rather than a periodic
  request whose period is a duration and not a wall clock hour. **It is silent, and it
  cannot post on a silent day**: the poster takes a token whose constructor is private and
  whose only factory returns null unless the day's entry exists and is unanswered.
- **40 tests across five new test classes**, plus a shared fixture that runs a month of
  ordinary use through the real engine and the real corpus one foreground a day, because a
  test that hand wrote a `PulseGenerated` would only prove the code can read a literal.

### The two risks issue #4 named, and what holds each

**Generating twice in a day, or not at all, across a timezone change.** One clock, one
explicit zone, and the date key and the reflection period fall out of the same reading.
`PulseScheduleTest` stands on both sides of a spring forward and a fall back and asserts
one date key per calendar day and one reflection period across each, that the morning after
a spring forward reflects on a twenty three hour yesterday, that the morning after a fall
back reflects on a twenty five hour one, and that the hour which happens twice is one day.
`PulseGenerationTest` asserts at most one entry per local day and that an entry already in
the log stops the sequence before it reads anything.

**Posting a reminder on a silent day.** Made unreachable rather than checked.
`PulseReminderDue` has a private constructor and one factory that returns null for an IDLE
day and for an answered one, and `PulseReminderPoster.post` takes nothing else.
`PulseReminderDueTest` walks all three states. A second collector cancels the reminder when
the day is answered some other way, and it can only cancel: there is exactly one thing in
this app that can post it.

### Three integration gaps the closing build will find

**None of these is a design question and all three are small.** They are recorded here
because the phase was built in slices and the shell was not one of them, and because a
session that hits the first one at the compiler will not know the other two are waiting.

- **`ClarityShell` does not pass `onOpenPulse`, so the app does not compile.**
  `AreasRoute` now requires it and deliberately has no default, because a default of `{}`
  would compile everywhere and ship a chip that opens nothing. The shell has to host
  `PulseRoute` the way it already hosts the Focus surface, and hand the chip the callback
  that opens it.
- **`MainActivity` does not route the reminder's intent.** `PulseIntents.opensPulse` is
  written as the contract and nothing calls it, so tapping the notification opens the app
  at whatever tab it was on. The call goes in `onCreate` and in `onNewIntent`, beside the
  `FocusIntents.opensFocusSession` call that is already there.
- **The corpus is not packaged.** `ClarityApp.AssetCorpus` reads `assets/corpus/`, there is
  no `app/src/main/assets` directory and no Gradle task copying the three committed
  markdown files into one, so on the device every day would come back
  `PulseOutcome.Unavailable` and the Pulse would never generate. **This one is invisible
  without logcat**, which is why the outcome is a state of its own and why `ClarityApp`
  logs it as a warning: an app producing no Pulse for a month because an asset was missing
  looks exactly like a quiet month. `adb logcat -s ClarityPulse` is the whole diagnostic.

### Twelve decisions where the obvious answer was rejected

All are written up in `DECISIONS.md` with the losing option named. In short: the response
pills are stacked rather than set side by side, because the left position reads as a
recommendation and side by side does not survive `quietDay`; the room is a fixed 520dp band
rather than a sheet that wraps its content; the amber tint reaches 45 percent of the height
and stops rather than crossing the whole surface; the chip's second signal is
`Today's Pulse` rather than `Pulse ready` or a count; history rows carry absolute dates
including today's rather than inheriting the Trail's relative labels; the acknowledgment is
held 1,100ms and the hold survives reduce motion, because a hold is not motion; the
reminder is a chain of one hop at a time rather than a periodic request; the permission
prompt fires on a false to true transition rather than on the first composition, because
the preference defaults to true; the reminder is gated by a token rather than by an `if`;
the rhythm row has three marks and today carries no ring; the corpus ships as an asset
rather than as a Kotlin constant; and generation runs after the presence marker rather than
before it, because the re-entry rule cannot otherwise see the day it is about.

### What is deliberately not in this phase

- **The re-entry surface**, 14b.4 and `design-v3.md` 11.2, which phase 6 owned and did not
  carry. This is the one item in this list that is a gap rather than a plan. The two engine
  side rules that follow that screen are built and tested; the screen is not, and it now
  has no phase. See the open questions above.
- **The reminder switch and its hour picker**, 14.1, which are phase 11. The two
  preferences exist, default to enabled at 20:00, and are honored: the scheduler follows
  them for the life of the process and needs nothing from a Settings row. The contextual
  `POST_NOTIFICATIONS` request is written as
  `NotificationPermissionOnReminderEnabled` and is waiting for that row to place it, so
  **nothing asks for the permission today**, which is correct rather than a gap.
- **`PulseCoordinator` living on `ClarityGraph`.** It is a process scoped singleton built
  from the graph's own repository and clock, which is exactly what the graph holds, and it
  is on `ClarityApp` instead because the slice that built it could not edit that file. The
  move is one lazy binding and `AssetCorpus` going with it, and `ClarityViewModelFactory`
  says so at the function that works around it.
- **Momentum and the Report**, phases 7 and 8, which call the same engine through the same
  shape. The Pulse is the worked example.

### What the device check still has to find

**It has not run.** The Pixel was in use by another session for the whole of this phase and
no `adb` command was issued. What the phone can prove that a unit test cannot:

- **That the app launches at all**, once the three gaps above are closed. That is the first
  question and it is not rhetorical: this phase adds a coroutine to the first foreground
  path that reads the whole log, builds a catalog out of three markdown files and appends
  an event.
- **`adb logcat -s ClarityPulse` after the first launch**, which prints one line saying what
  the Pulse did. Three of its four outcomes are silence, and silence is indistinguishable
  from breakage from the outside. A line reading `no Pulse:` is the packaging gap above.
- **Whether the amber night reads as a room**, and whether the dawn and evening tints are
  distinguishable from midday at low brightness, which is the failure mode of choosing quiet.
- **Whether the three marks in the rhythm row are distinguishable at arm's length**, which
  is the one thing the 3.0 to one floor cannot settle on its own.
- **The answer animation end to end**: the fill from the tap point, the unselected pill
  dropping, the acknowledgment, the settle. Four timed steps in a row is where a jank shows.
- **Whether the reminder arrives at the hour it was armed for**, which needs a device, a
  night, and the reminder hour moved to something reachable. WorkManager may run it late and
  Doze can hold it, and `DECISIONS.md` records that as the honest limit of what this app can
  promise without an exact alarm.
- **TalkBack over the sheet and the rhythm row.** The acknowledgment is a polite live region
  hidden until it is visible, the row is one node that names itself and tallies nothing, and
  neither has been heard.
- **Font scale 200 percent on the sheet**, where a long observation has to scroll inside the
  room rather than grow it.

---

## Phase 5 delivered

The engine, issue #3. Layers 1 to 5 of the six in `CLARITY_LOGIC_ENGINE.md` 2, plus the
simulator, which section 12 requires **before a single corpus sentence is written**. It is
the largest phase in the project so far and the only one with no screen in it: nothing here
is visible, and every defect it could contain is a false sentence about somebody's life
some months from now with nothing on screen pointing back at the cause.

**Nothing here is a rebuild.** `FactRef`, `StableHash`, the event log at 28 types and
`TrailQueries` all existed and were built for this. What phase 5 added is everything above
the facade.

- **Layer 1, the facts.** `domain/engine/`, with the fact classes in a `facts/` directory
  and in the `domain.engine` package, because they are the vocabulary every layer imports.
  `FactExtractor` runs once per invocation and returns a fully populated, immutable
  `FactSet`. **Nothing in it is lazy**, because a fact computed at validation time was
  computed against a different log than the fact beside it. Archived and tombstoned areas
  are absent from the map entirely, which turns prohibition 3 of 1.1 into a shape rather
  than a rule somebody has to remember. `CueFacts` is extracted and gated on all three
  thresholds in 3.7, and nothing reads it until phase 9b.
- **17 new functions on `TrailQueries`**, taking the facade to 59. Every one of them exists
  because a fact the engine needed could not be answered from what was there, and
  `MASTER_BUILD_PROMPT.md` 9 leaves exactly one option: add it to the facade, never compute
  it in the engine. The per area focus attribution, the per day and per hour buckets, the
  live item set behind validator check 2, and the three readers that make `FiringHistory`
  derivable from the log are all in that group.
- **Layer 2, the catalog.** `domain/engine/catalog/`. `ClarityCatalog.build` parses all
  three corpus files into families, stages, variants and response pairs. **Nothing is
  authored in Kotlin except the rules**, so a corpus edit cannot silently disagree with a
  copy of the corpus embedded in code. Stage thresholds are parsed out of the corpus stage
  headers per 7.3, a compound header becomes two rules pointing at one stage, a qualitative
  header gets no range at all, and `lengthBand` is computed from the realized word count at
  load time and never read from a tag.
- **Layer 3, selection.** `domain/engine/select/`. The seven steps in section 5 in that
  order, the three term ranking with the final key sort present, the incompatibility matrix
  from section 9, and 5.1's deliberate silence. Four of the five silence reasons are
  produced here and every one is a described state rather than an error.
- **Layer 4, realization.** `domain/engine/realize/`. The ladder, the register order, the
  bench, the line. **It receives only the `FactSet` and a corpus**, and there is no
  parameter through which a live entity table could be passed, which is validator check 5
  enforced by shape. Slots render centrally: percent as a word, counts of two to nine as
  words in Pulse and Momentum and digits in the Report, both plural forms carried with no
  default, and zero never reaching a template.
- **Layer 5, validation.** `domain/engine/validate/`. All ten checks in section 8, in that
  order, as a list a test walks rather than as control flow. A veto sends the engine to the
  next ranked selection, and an exhausted list is silence.
- **The engine loop.** `ClarityEngine.observe(facts, history, purpose)` answers `Spoke` or
  `Silent`. It holds its validator as a seam **with no default**, so the bypass
  `MASTER_BUILD_PROMPT.md` 11.4 forbids cannot be written by accident.
- **The simulator**, `app/src/debug/java/com/kamsiob/claritynow/devtools/`. Eleven personas
  including the one who accepts every plan and completes none, a full simulated year each,
  the engine day by day for the Pulse and week by week for the Report with Momentum and the
  banner on every simulated open, and a plain text dump annotated with the rule, the stage,
  the register, the variant key and the facts used. It writes its own output back into its
  log, because `FiringHistory` derives from exactly those events.
- **`verifyDevtoolsAreDebugOnly`**, a Gradle task that reads the source directories Gradle
  resolved rather than trusting the layout on disk, and fails if the package is missing from
  the debug source set, present in a release one, or named by any file a release build
  compiles. It runs inside `verifyClarity` and blocks `assembleRelease`.
- **246 unit tests** across 27 new classes, and every one of them holds something that is
  invisible on a screen. The twenty veto tests are the ones the specification asks for by
  name: a validator whose failure branch never executes is a validator nobody has verified.

### What the simulator measured, and why six checks fail on purpose

Issue #3 says in advance that the statistical checks in section 12 cannot pass in this
phase, because the corpus is not grown until phase 9 and layer 6 does not exist until 9b.
**They are built, they run, they measure and they report, and only their verdict is
deferred.** Each carries a date and the issue that lifts it. A skipped check produces no
number, and the whole reason the simulator comes before the corpus is so the growing can be
aimed.

Eleven personas, a year each: 92 rules across 78 families, 3,148 simulated opens, 451
reports, 118 layer 5 vetoes across the whole run.

| gate | target | measured |
|---|---|---|
| no variant repeats inside ninety days | none | 7,384 repeats, the tightest after 1 day |
| Pulse silence | 8 to 25 percent of opened days | 43 to 98 percent per persona, 76 overall |
| layer 6 silence | at least 15 percent of reports | not measurable, layer 6 is phase 9b |
| no family over a fifth of a year's Pulses | 20 percent | 27 to 60 percent per persona |
| every stage of every hot family fires | all | 29 hot families, one gap: `accumulation` never reached stage 2 |
| no two consecutive leads share a band | none | 715 collisions across 451 reports |
| no three parallel numeric clauses | none | 27 runs of three or more |

**The four enforced checks pass.** No banned word, dash, emoji or non-ASCII character in
any sentence of any persona's year; no sentence naming an area with no events in its
window; no visible slot marker; and nothing in the plan-accepting persona's 1,388
invocations referencing a plan, a commitment, an intention or a failure to act.

### The reading phase 9 should not miss

**Silence is three to twelve times the target band, and a bigger corpus will not fix it.**
Of the eleven Pulse families in 6.1, six ever fired. Two have no rule at all. Three more,
`throughput`, `burst` and `queueDrain`, have rules that no persona's year ever satisfied.
Of 2,383 silent days, 1,238 were days where something qualified and every candidate was
filtered by a cooldown or by yesterday's family, and 1,134 were days where nothing
qualified at all.

Growing benches moves the repetition figure and the length band collisions. It does not
move the second number. **Silence needs more rules, and behind three of them, more facts in
`CLARITY_LOGIC_ENGINE.md` 3.1.**

### Fourteen decisions where the obvious answer was rejected

All are written up in `DECISIONS.md` with the losing option named. In short: a family whose
escalation fact 3.1 does not declare gets no rule rather than an approximate criterion; the
escalation ladder drops the pair rather than raising the stage; editorial notability is
specificity at three rather than an authored flag; the two families 7.4 qualifies by a
stage they do not have become two rules each; a tie for the busiest day resolves to the
earliest and the family carries a floor; response pairs live on the stage rather than being
flattened onto the family; the corpus violations that exist today are a recorded list
rather than a disabled check; which families count as hot is measured rather than authored;
the validator masks the person's own words before three of its ten checks; a deferred check
runs and reports and only its verdict is deferred; debug only is verified against the
resolved source sets; the engine takes a zone at construction and keeps the signature 2.2
specifies; and the validator is a seam with no default.

**The fourteenth is recorded as open rather than settled**, because it is not a builder's
to settle: three corpus totals have drifted. `CLARITY_LOGIC_ENGINE.md` 11.1 states 162
Momentum lines and 1,519 in total; the files carry 146 and 1,503. `CORPUS_3_MOMENTUM.md`
claims 112 Momentum headlines and carries 96, and two prose figures inside
`CORPUS_2_REPORT.md` disagree with that file's own totals tables. The numbers are left as
written and the count is recorded at the point of the claim. The recommendation is stated in
`DECISIONS.md` and not taken.

### What is deliberately not in this phase

- **Layer 6, guidance.** Phase 9b, issue #8, and `CLARITY_LOGIC_ENGINE.md` 10 calls it the
  last thing built and the first thing removed if it reads as supervision. `CueFacts` is
  extracted and unread.
- **Every screen that calls the engine**, and the generation lifecycle in
  `MASTER_BUILD_PROMPT.md` 11.3. Phases 6, 7 and 8. Phase 5 built the engine and no caller,
  so nothing the engine produces has reached a device.
- **The corpus at its target size.** Phase 9, issue #7.
- **Rules for nine families and three single stages** whose escalation fact 3.1 does not
  declare. Listed in code with the missing fact and the corpus line that needs it, and a
  catalog test fails if a family goes quiet without being on that list.

### What the closing check still has to find

It has not run. **This is the phase where a screenshot is least likely to catch anything**,
because there is nothing to photograph: no engine output reaches a surface until phase 6.
What the closing build and install can still prove is that the app that was already working
still works with 8,000 lines of new pure Kotlin in it and a new Gradle verification in the
chain, and that the release variant builds at all, which is what
`verifyDevtoolsAreDebugOnly` is attached to.

Worth pointing the phone at anyway, and `adb logcat` after every step per `CLAUDE.md`:
Areas, the Trail, a focus session, and calm mode, all unchanged. The one thing that would be
new information is a release build, `assembleRelease`, which no phase has needed until now
and which is where a devtools reference would surface.

## Phase 4 delivered

Focus sessions and the first Contemplative surface in the app, issue #2, with five
Addendum 01 issues landing inside it rather than after it because retrofitting them
into a finished focus surface would have cost more than building them in: **#28**
early ending as a success state, **#29** add ten minutes, **#30** the transition
warning, **#32** the Live Update, and **#49** the arc reading before the digits.

**Nothing here is a rebuild of anything.** The event types, the reducer's focus fold,
`FocusSessionState`, `FocusOutcome.ENDED_EARLY`, the Trail's focus rows and
`AreaCardModel.focusMinutesRemaining` all existed before this phase and were built for
it. What phase 4 added is the surface, the persistence, the notifications and the one
ticker underneath all three.

- **The Focus surface**, `ui/focus/`. One entry point rather than three destinations,
  because the chooser, a running session and a finished one are the same room and
  which is showing is a fact about the log rather than about navigation.
  `FocusRoute.kt` enters `ContemplativeTheme`, which is a theme and not a branch
  inside the Daylight theme, so the theme setting can never invert it.
  `FocusBackdrop.kt` draws the indigo night in one Canvas that never recomposes,
  `FocusRing.kt` holds the ring, the numeral and the completion bloom,
  `FocusChooserScreen.kt`, `FocusSessionScreen.kt` and `FocusCompleteScreen.kt` are
  the three faces, and `FocusControls.kt` is the two Contemplative controls.
- **The session's arithmetic**, `data/repo/FocusSession.kt`. A file with no `android.`
  and no `androidx.` import, holding the countdown, the restore decision, which
  running session belongs to this device, whether a session may start, whether an
  ending is shown or discarded, and the one ticker. It sits in `data` rather than in
  `domain` because it answers what one device should show and write right now, which
  `domain.replay` and `domain.query` do not do. It is a separate file so that a unit
  test can reach every one of those rules without Room or DataStore.
- **The write paths**, in `ClarityRepository`. `startFocus`, `completeFocus`,
  `endFocusEarly` and `extendFocus`, plus `restoreFocus`, which is what a cold start
  or a resume asks. **One running session at a time is enforced in the repository and
  nowhere else**, because a chooser is not the only door in: a notification action,
  and later a shortcut and a tile, all reach the same object, and a rule enforced in a
  screen holds until the next screen.
- **One ticker in the process.** `focusTicks` is shared and private, and everything
  reads `focusCountdown`, so the ring, the ongoing notification, the Live Update and
  the area card cannot derive remaining time four slightly different ways. The ticker
  is attached only while a session is running and re-aligns to the second boundary on
  every emission, so it cannot drift over an hour the way a fixed one second loop
  does.
- **Persistence across process death.** `focusSessionId` and `focusSessionEndsAt` join
  DataStore as a per device handle. **They are a cache, not engine state**: the log
  holds the start and the folded extensions, so any device computes the same end
  instant without them, and `restoreFocus` falls back to the log and repairs the
  handle whenever the two disagree. What they add is the one fact in a session that is
  about a phone rather than about a person, which is which of the running sessions in
  a merged log this device is the one running.
- **The notifications**, `notifications/`. Three channels created at process start,
  the ongoing notification with its countdown chronometer, the Live Update on
  `Notification.ProgressStyle` where the platform allows it, one completion
  notification and one transition warning, and a receiver that performs `Add 10 min`
  and `End` without opening the app. All of it collects the same countdown and writes
  through the repository, so it is not a second clock and not a second write path.
- **The shell's half**, `ui/nav/FocusEntry.kt` and `ClarityShell.kt`. The surface is
  not a tab; it covers the tabs and the tab bar while it shows, gets a ViewModel store
  of its own that is cleared when it goes, and the system bars flip to light content
  over the indigo night whatever the theme setting says.
- **The area card in session**, `ui/areas/AreasFocus.kt`. Two values and two pure
  functions, in a file with no Android import, deciding which card carries the
  intensified wash and how many whole minutes it reads. **The card is handed minutes
  and no denominator**, so the fraction a progress bar would need is not on that side
  of the boundary at all.
- **Forty five unit tests** across `FocusSessionTest` (20), `FocusFoldTest` (10),
  `FocusEntryTest` (9) and `AreasFocusTest` (6), and every one of them exists because
  the thing it holds is invisible in a screenshot.

### The five things the specification said would go wrong

Each of these is called out in `MASTER_BUILD_PROMPT.md` or `design-v3.md` because it
is the obvious implementation and it is wrong. What stops each is structural rather
than remembered.

1. **Back ending the session.** There is no method on `FocusViewModel` a back handler
   could call that ends anything, and `FocusEntry` is a value with no Compose, no
   Android and no coroutine in it whose only job is remembering that somebody walked
   away. The subtler version of the same failure is a shell that re-opens the surface
   on the next frame because a session is running, which is a back button that does
   nothing; `FocusEntry.leftSessionId` is what stops it, and `FocusEntryTest` holds
   both.
2. **A per frame countdown.** The tick is read inside the dial and inside two click
   handlers and nowhere above them, so a value arriving once a second reaches the
   numeral and the arc and nothing else. The session's unchanging facts live in a
   separate value the tick cannot touch, and the notification layer throttles to a
   render key so the shade is written once a minute rather than sixty times.
3. **A conditional Contemplative theme.** `ContemplativeTheme` is entered by the route
   and `LocalContemplativeColors` is what every composable on the surface reads. No
   file in `ui/focus/` reads `LocalClarityColors`.
4. **The word this app does not use.** No string in `strings.xml`, no content
   description and nothing in the notifications package contains it, the type is
   `FOCUS_ENDED_EARLY`, and the completion model carries no field saying which kind of
   ending it was, so there is nothing for a later edit to render.
5. **The digits outranking the shape.** The arc is 240dp and the numeral is 64sp,
   capped at 1.3x the font scale while the ring does not grow, and
   `design-v3.md` 11.3 now states that as a ratio a later session can check with a
   ruler.

### Eleven decisions where the obvious answer was rejected

All eleven are written up in `DECISIONS.md` with the losing option named. In short:
calm mode takes the collapse and the expanding circle and leaves the check; the Live
Update is one segment and at most one point; the completion screen has one wording and
is not told which ending it is drawing; the ring is thin and the weight goes to the
tip; both actions on the end confirm carry the same weight; the Contemplative primary
uses one of `design-v3.md` 10.7's two forms everywhere; the Focus chip is permanent
and never becomes a countdown; nothing stores whether the notification permission has
been asked for; the completion notification takes the phone's own sound; and the
chooser names an area in `textDim` rather than in the area color.

**The eleventh is recorded as open rather than settled**, because it is not a
builder's to settle: a session a person ends themself fires the ordinary tap on the
End control and nothing on the completion screen after it, while a natural completion
fires `focusEnd`. Issue #28 asks for the two to be the same or deliberately gentler
and says plainly that they are not to be absent. The recommendation is stated in
`DECISIONS.md` and not taken.

### What is deliberately not in this phase

- **The `Session length` selector and the `Five minute warning` row**, both phase 11,
  because there is no Settings screen. The preferences behind them are built and
  honored, so every session today runs at the stored default of 25 minutes and the
  transition warning is off.
- **The Focus Countdown widget**, phase 12, which is the third of the three surfaces
  `design-v3.md` 11.3 requires to read a session as a shape.
- **The Pulse chip** beside the Focus chip, phase 6.
- **The soft tone and the `transitionWarn` haptic**, which are not deferrals but
  omissions, and are in the defects list above rather than here.

### What the device check still has to find

It has not run. Phase 2 found seven defects on the device that the build had passed,
phase 3 found five more after the build was green, and phase 3b found four in the seam
between two agents. **This phase is the one where a screenshot is least likely to
catch anything**, because most of what it added is timing, process lifetime and the
notification shade.

Two of the checks are named in the specification and close the phase:

- **`adb shell am force-stop` mid session, then relaunch.** The ring must come back at
  the right remaining time. Repeat it after an `Add 10 minutes`, per issue #29, since
  the stored instant has to have moved with the extension.
- **The back gesture during a session.** The surface goes, the session keeps running,
  the ongoing notification stays in the shade and the area card keeps counting down.
  Then the notification body brings it back.

The rest of what is worth pointing a phone at: the indigo room against the Daylight
one it fades out of, the status bar glyphs over both, the numeral not jittering as the
seconds change, the ongoing notification's chronometer agreeing with the ring, the
Live Update chip on a device that can promote one and the chronometer on one that
cannot, `Add 10 min` and `End` from the shade with the app closed, a completion
arriving while the app is in the background, TalkBack reading the ring as one node in
whole minutes, and calm mode over all of it. `adb logcat` after every step, per
`CLAUDE.md`: several defects in this project were silent app exits that a screenshot
passed.

## Phase 3c delivered

The foundations half of the polish pass, issue #53. **It exists because Addendum 01 3c
calls for a polish pass and `MASTER_BUILD_PROMPT.md` 19 never carried one.** That is a
recording error rather than a change of plan, and the owner found it by looking at the
app rather than at the plan. The surfaces half is phase 12b, issue #54, and the split is
argued in `DECISIONS.md`.

**An audit ran first**, against `design-v3.md`, the two device capture sets and the
source, to separate three causes with completely different fixes: implementation drift,
missing content, and a genuine gap in the design. It found the third dominates. The
document removed every conventional decoration device on purpose, which put the whole
burden on type, space, value and motion, and three of those four were not carrying.

Three drift fixes landed first and in their own commit, because none of them needed a
design decision and the first is the largest single visual return in the whole audit.

- **The area card's shadow was 100 percent clipped away.** `SwipeableRow` put
  `.clip(shape)` on the Box whose only measured child is the card, and the clip rect and
  the card bounds are the same 18dp rounded rectangle. The primary content layer on the
  home screen was the only element on it with no separation device at all while the tab
  bar, the FAB and the chips all rendered theirs, which **inverted the depth order**.
  Measured on a device capture, the card's bottom edge stepped from `#FFFFFF` to the
  canvas in one pixel, against fourteen pixels of decay under the tab bar. The clip is
  genuinely required by `design-v3.md` 10.3.1 and now sits on the action layer
- **The largest tap target in the app produced no feedback.** 8.2 item 2 gives a card the
  same 0.97 press as a button and section 9 gives it the tap haptic. `clarityClickable`
  sets `indication = null` deliberately and nothing had been put in its place. The press
  scale is now a shared modifier rather than a block copied per component, which is how
  it came to be written twice in `Buttons.kt` and not at all on the card
- **The FAB drew its colored glow in dark mode.** 6.1 is explicit that dark and
  Contemplative elevation is lightness only, and every other `clarityShadow` call site
  already guarded on it

Then the pass itself, **tokens and type only**. Every item below is a `design-v3.md`
change as well as a code change, recorded in the section that states it.

- **The surface ladder**, 3.1 and 3.2. `canvas` `#F1F1F6` to `#E6E6EC`, `card` `#FFFFFF`
  to `#FCFBF9`, `raise` `#FAFAFC` to `#F4F3F0`. Card against canvas 1.126 to one becomes
  **1.202 to one**, and the light world's span 4.73 L\* becomes 7.19. In dark, where 6.1
  allows no shadows at all and the lightness ladder is the only device there is, `card`
  `#191921` to `#1D1D25` and `raise` `#15151C` to `#18181F`, with `canvas` deliberately
  held. `raise` now has named occupants for the first time: chrome, meaning the floating
  tab bar and an unselected chip, sits one rank under content. `SurfaceLadderTest` holds
  the order, the size of each step and the fact that `raise` is drawn somewhere
- **`inkSecondary` to 0.64** from 0.60, in light, and it was **forced by the canvas
  change rather than chosen**. See below
- **Tracking on all nine sans roles**, 5.3, where two of nine carried a value. The ramp
  opens small and closes large, +0.032 to -0.030em, with `sidehead` deliberately off it
  at +0.024em because the conventional way to make a section label read as a marker is
  capitals and three separate rules forbid those
- **`body` to 15sp and `bodyStrong` to 17sp**, 5.3. They were both 16, so `design-v3.md`
  11's instruction to set a Trail day header in one and its rows in the other bought a
  weight change and nothing else. The Trail now reads 30, 17, 15, 12 rather than 16 and
  12
- **A serif screen title on the Trail**, 11, which **reverses a decision phase 3 took
  deliberately**. Newsreader had five call sites in the whole app and four were empty
  states, so the signature typeface meant "there is nothing here" four times in five
- **The idle card title off `inkTertiary`**, 10.3. `inkTertiary` measures 2.40 to one on
  the card against section 13's floor of 4.5, on the string 10.3 itself calls the most
  important one on the screen. A contradiction inside one document, resolved for 13
- **The default area walk**, 3.4. It starts at Berry and never hands out `#2D7FF9`,
  `#4DA3FF`, `#22C55E` or `#F59E0B`, each byte identical to a function token. All four
  stay choosable; only what the app assigns on its own changed
- **`design-v3.md` 15.3**, the refusal list. Fifteen fixes somebody would reasonably
  propose for a problem this app actually has, each paired with the sentence that
  already forbids it

### The contradiction it resolved, and it had survived two phases

`design-v3.md` 1 says "backgrounds are never pure white or pure black". Section 14 says
"no pure white or pure black backgrounds". 3.1 said `card` is `#FFFFFF` and 10.3
repeated the hex rather than naming the token. **Two statements against one, and the
build had followed the one**, faithfully, because 3.1 is the token table. The two win.
10.3 now names the token, because a component section repeating a color out of the token
table is how the contradiction lasted as long as it did.

The card also had no warm pixel in it, and neither did anything else. `cardWash` is the
user's own area color and four of the eight moods are cool, so the default area put a
cool wash on a pure white card. `#FCFBF9` is warm at 3 points of red over blue, which is
the "warmth in the cards" section 1 promises.

### What the token change cost downstream, and it was not free

**The canvas change forced an ink token to move, and that is the kind of second order
effect that gets missed.** At 0.60 the light canvas was already the tightest surface in
the app at **4.5046 to one**, four ten-thousandths over section 13's floor, and a test
written in phase 3 pinned it with a comment predicting that a later darkening of
`canvas` would take the whole Daylight world under the floor without touching that
screen. It did, to **4.33**. `inkSecondary` moved to 0.64 in consequence, and the
tightest ground in the app is now the light canvas at 4.88 to one.

The same raise retired a defect phase 3b had recorded as unfixable rather than fixed. At
0.60 `inkSecondary` measured 4.48 to one on a resting area card and **4.27 on an
in-session one**, and 16.7 held it in a test on the grounds that raising an ink token
for every screen was out of scope for a calm mode audit. It is in scope for a token
pass. Both now clear, at 5.05 and 4.75.

**The 48 area label variants were re-verified against the new ground**, in both worlds,
ordinary and in calm mode, which `design-v3.md` 3.4 and 16.7 require whenever the card
moves. Eight of the 96 variants changed, three in light and five in dark, and the
function found them on its own because every ground is recomputed at runtime from the
token set rather than written down. The worst case is 4.538 to one in light and 4.655 in
dark.

**A second calm mode defect surfaced while doing it.** Phase 3b assumed that a variant
clearing the ordinary ground clears the calm one, because 16.2 pins the calm wash
shallower and desaturates the accent under it. In light that holds. **In dark it does
not**: a desaturated accent at 15 percent over the card is not uniformly lighter than
the true accent at 16, and fourteen of the 48 dark labels measure slightly worse in calm
mode than out of it. It survived phase 3b on 0.04 of margin. Lifting the dark card spent
it, and three variants went under. Labels are now verified against **both** grounds and
are the same color on both, which is what 16.2 requires of them.

### Eight decisions where the obvious answer was rejected

Recorded per `design-v3.md` 15, in `DECISIONS.md` under the August 27 polish pass entry.
Depth is bought downward rather than by lifting the card, because there was no headroom
above pure white. The dark canvas is held rather than taken toward pure black for OLED.
Sheets and the undo snackbar stay at `card` while the tab bar and unselected chips move
to `raise`. `sidehead` sits off the tracking ramp rather than on it. The body pair
splits unevenly, 15 and 17 rather than 16 and 18. The Trail title takes the same
treatment as the Areas title rather than one with more character. The area walk starts
at Berry rather than at Twilight, the next mood along. And the audit's refusal list
became `design-v3.md` 15.3 rather than more entries on the dated tell list at 15.1.

### What is deliberately not in this phase

All of it is phase 12b, issue #54, and the reason is that surface decisions get better
information once there is more than one sheet and more than one world to judge them
against.

- **Scroll edge treatment.** Content still passes hard edged under the status bar and
  behind the floating tab bar, and a grep for `verticalGradient`, `fadingEdge`, `blur(`
  or `overscroll` across `ui/` returns zero hits
- **The sheet shadow.** `ClarityElevation.sheet` is declared in 6.1 and still has zero
  call sites, so every sheet in the app butts flat against its scrim
- **Whether anything in this app moves at rest.** Section 14 closes with "An app that
  never moves is an app that feels broken" and that sentence is false today. Every
  conventional fix for it is on 15.1 or next to it and 15.3 refuses four of them by
  name. It is an owner decision rather than a patch
- **What the Trail's event circle carries** that the sentence beside it does not, and
  **whether an inactive tab keeps its label**, and **what a text field looks like**,
  which section 10 never says at all
- **The mint completion block's margin.** It sits at 8dp while every other element on the
  Trail sits at 20dp, because the wash is applied to the row rather than inside the row's
  own padding. Small, and it belongs with the other surface work

### What the device check still has to find

It has not run. Phase 2 found seven defects on the device that the build had passed and
phase 3 found five more after the build was green. This phase changes six color tokens
and nine type roles, so every screen in the app is affected and nothing is isolated to a
new surface. The two worlds and calm mode all have to be looked at, `adb logcat` checked
after the install per `CLAUDE.md`, and the things worth pointing a phone at are the card
against its new ground with its shadow restored, the tab bar at `raise` against a card at
`card`, the Trail's new title and its 30, 17, 15, 12 ladder, an area card at 200 percent
font scale now that `body` is 15sp, and a freshly created area coming out `#D946EF`
rather than the color of the FAB.

## Phase 3b delivered

The executive function retrofit. Six items from Addendum 01 that had been assigned to
phases 1 and 2, both of them closed and shipped, collected into one phase and run
before phase 4, which depends on parts of it.

- **The unfiled inbox**, issue #24. The FAB opens the add sheet with no area chosen and
  the item is written with a null `areaId`. `ui/areas/InboxSheet.kt` holds the sheet,
  its rows, the empty state and the area chooser that files an item, including the case
  where there are no areas to file into yet. The count is a plain chip in the Areas
  header, present only while the inbox holds something. No badge, no dot, no color that
  reads as an alert
- **The first step**, issue #25. One optional line on an item. One ellipsized line at
  `caption` on the area card, in full in the detail sheet, absent entirely when there
  is none, and read by TalkBack after the title rather than before it
- **The estimate**, issue #26, the capture half. Optional minutes as a free number
  field, digits only, four at most. It appears on exactly one surface, the area detail
  sheet, as plain text. No surface counts down against it and none renders it beside an
  actual
- **Re-entry detection**, issue #27, and only detection. `ClarityApp` writes
  `APP_OPENED` on the first foreground of each calendar day by counting started
  activities across zero rather than by running in `Application.onCreate`.
  `TrailQueries.reEntryOn` and `lastReEntryOnOrBefore` find a return after 14 or more
  calendar days, folded from date keys with no wall clock anywhere in the arithmetic
- **Calm mode**, issue #48. `ui/theme/CalmMode.kt`: an OKLab chroma transform at 0.6
  holding lightness, applied at one point for every wash in the app, plus the two
  pinned wash opacities on the token set. It joins the one global motion flag with an
  `or` rather than adding a motion level beside it, so reduce motion always wins on
  motion
- **The entrance rule**, issue #50. `ui/theme/ClarityEntrance.kt`: an entrance fires on
  the first open of its tab per app session and never again. The flag lives in the per
  tab saveable state the shell has held since phase 3, and it stores which session
  spent it rather than a boolean, so a rotation leaves it spent and a process death
  re-arms it

### The audit was most of the work

Calm mode is a retrofit, not a feature, and the tempting version of it is a boolean and
a few branches in new code. `design-v3.md` gained three tables instead.

- **16.6**, every animation in 8.2 by number, all twenty eight, including the ones that
  are already crossfades and the ones calm mode leaves alone
- **16.7**, every color token in section 3, with a number against each rather than the
  words "less saturated". `chroma x 0.6` or `0`
- **16.8**, every component in `ui/components/` shipped in phases 1, 2 and 3, named,
  with what calm mode does to it. `unchanged` is the most common answer, because most
  of them are built from ink and from the four tokens 16.2 excludes by name

The contrast half is computed rather than judged. `CalmModeContrastTest` walks all 48
area colors on every ground they can sit on, in both worlds, ordinary and calm. The
transform moves WCAG relative luminance by at most 0.0185, which is what holding
lightness buys and is why no ratio verified in `design-v3.md` 13 can be broken by it.

### One shipped defect the measurement found, and it was not calm mode's

**The area label's contrast was being verified against the wrong ground.**
`design-v3.md` 3.4 says to verify 4.5:1 against the card, and `areaLabelColor` measured
against the bare `card` token, where the worst of the 48 clears at 4.58 to one and
looks fine. The card a label actually sits on carries that area's own wash at up to 13
percent in light and 16 in dark. Measured against the card as drawn, the same label on
an in session area is `#E11D48` at **3.83 to one**, and 3.95 at the peak of a
promotion.

It is the same class of mistake phase 3 found in the Trail's mint completed row, where
a wash was composited over the wrong ground and cost 0.1 of a ratio. Here it cost 0.75.
It surfaced only because calm mode's transform has to be measured on the ground it
lands on, and measuring it exposed that nothing else ever had been. Calm mode neither
caused it nor fixes it: with the transform applied the same label reads 4.41, better
and still failing.

Corrected with the remedy 3.4 already names, adjusting the label variant and never the
dot or the wash, applied against the correct ground. The worst case is now 4.55 in
light and 4.56 in dark on every one of the 48, in both modes, on every wash opacity the
design permits. Twenty three of the 48 light label variants moved and five of the dark
ones.

### Ten decisions where the obvious answer was rejected

Recorded per `design-v3.md` 15, in `DECISIONS.md` under the two August 27 entries. Four
in the capture path: the FAB captures into the inbox rather than into the first area,
deleting an area does not orphan its items, the first step truncates on the card and
wraps in the sheet, and the estimate is a free number field rather than a set of
duration chips. Six in the retrofit: an app session is the process lifetime, calm mode
does not go back to following the system once set, the entrance starts at the screen
title rather than at the first card, the color picker's swatches keep their true color,
the platform bottom sheet ships without honoring calm mode, and re-entry detection
hands out the date of the return and never the length of the absence.

The last of those is a shape rather than a rule. `ReEntry` carries `returnedOn` and
nothing else, and no function in `domain.query` yields the number of days a person was
gone, because `MASTER_BUILD_PROMPT.md` 14b.4's central prohibition should not rest on
the phase 6 builder remembering it.

### What is deliberately not in this phase

- **The re-entry surface.** Phase 6, issue #27's second half. It has to be able to say
  nothing, and saying nothing is an engine decision
- **The `Calm mode` Settings row.** Phase 11, issue #10, because there is no Settings
  screen. `strings.xml` carries the label and caption it will use, fixed here alongside
  the setting they name
- **The estimate as an observation.** Phase 8 for the fact and the delta veto, phase 9
  for the language. `MASTER_BUILD_PROMPT.md` 14b.8

### What the device check still has to find

It has not run. Phase 2 found seven defects on the device that the build had passed and
phase 3 found five more after the build was green, and this phase adds three surfaces
worth pointing a phone at: the inbox sheet and the filing chooser, the two new fields
at 200 percent font scale on a sheet that now scrolls, and calm mode on the Areas
screen, the Trail and the shell. `adb logcat` after the install, per `CLAUDE.md`,
because several defects in this project were silent app exits a screenshot would have
passed.

## Phase 1 delivered

Scaffold, theme, and the data foundation.

- Every color, type, shape, motion and haptic token from `design-v3.md`
- Newsreader and Hanken Grotesk committed as variable font files in `res/font`
  with their OFL texts. Downloadable Fonts is never used, because this app has no
  network permission
- The event log: 24 event types, payload serialization, fractional order keys
- The reducer, the invariants, conflict resolution, and checkpoints
- The replay test harness: determinism, divergence merge, idempotency, checkpoint
  equivalence, reset virginity
- `testdata/golden-log.json` and `testdata/golden-state.json`, the contract with the
  future Linux desktop app, plus `docs/EVENT_FORMAT.md`
- The public repository, the AGPL-3.0 license, and the automated language and
  permission gates

**Two font traps were checked and one was real.** Hanken Grotesk ships no `tnum`
feature. Its digits happen to share one advance width at the default instance, but
the font carries HVAR so that is not guaranteed at other weights. Every updating
numeric display goes through the fixed width treatment in
`ui/components/ClarityText.kt` rather than trusting the font. Newsreader does carry
`tnum`.

## Phase 3 delivered

The Trail, and the query facade every later phase reads a number through.

- `domain.query.TrailQueries`, a pure fold over the event log. Thirty six functions,
  every one taking its time bounds as parameters and reading no clock. Bounds are
  half open, `[start, end)`, stated once and held everywhere
- `DomainPurityTest`, the first purity test in the repository. `docs/ARCHITECTURE.md`
  had claimed one existed since phase 1 and it did not
- Five DAO queries, each bounded by reading, and six repository methods. Nothing on
  the Trail path selects the whole table
- The Trail screen, and `ClarityChip`'s first call site, which turned up three
  deviations from `design-v3.md` in a component nothing had used yet
- 126 unit tests, up from 91

### What the specification could not answer

Three contradictions survived the authority order and are open as issues rather than
guesses: #19, #20 and #21. None of them blocks phase 3, because `TrailQueries`
computes no week of its own and takes every bound as a parameter. **#19 is the urgent
one:** it is a change to the event format contract, and it is cheapest to settle
before a real log exists.

### Four decisions where the obvious answer was rejected

Recorded per `design-v3.md` 15.

1. **The event circle's color.** The obvious answer is a semantic palette, green for
   completions and red for deletes. `design-v3.md` 3.1 scopes three of those tokens
   to one use each, and 3.4 forbids color as a filled block. The circle takes the
   area's color as of that event, at 12 percent
2. **Pagination anchors on real events**, not on calendar arithmetic from today. A
   person returning after a year would otherwise face twenty six empty page loads
3. **Day headers do not stick.** A sticky header needs an opaque backing, which is a
   second separation device on an element already carrying a hairline
4. **No Trail row is tappable.** `design-v3.md` 10.15's destination table has no
   Trail entry, and nine of the twenty four event types name nothing that could open

### Five defects the review found after the build was green

The build passed and the screen looked right before any of these were known.

1. **The completed row's mint was composited over `canvas`**, which darkens the
   ground to `#E0EDEA` where the timestamp measures 4.40:1, under `design-v3.md` 13's
   4.5:1 floor. `design-v3.md` 11 calls it a mint wash **card**, and over `card` the
   ground is `#EDFAF2` at 4.57:1. A measurement caught this, not an eye
2. **A focus row could never name its item.** A focus event is keyed by its session,
   so the page's entity ids never contain the item id, and a session on an item added
   more than a fortnight earlier would have rendered as `Finished 25 minutes of focus
   on` with nothing after the preposition. Latent until phase 4, invisible to every
   test, because every fixture hands the facade a complete log. The rule is now
   `itemIdsNeededBy`, pure and tested
3. **The row sentence was capped at two lines with an ellipsis**, which clips at 200
   percent font scale on a row that cannot be tapped to read the rest
4. **The day header's label had no width cap**, so a long date at 200 percent font
   scale took the hairline to zero width and the header lost its one separation device
5. **Tab switches discarded scroll position.** The Trail keeps its loaded pages in a
   ViewModel that outlives the switch, so a person who paged a month back returned to
   the top of a list that still held the month. The shell now holds saveable state
   per tab, which fixes Areas too

## Phase 2 delivered

Areas, items, the queue, and the app shell.

- `ClarityPreferences`, the per device settings store
- `ClarityRepository`, the only writer in the app
- `AreaState.lastEventAt`, stamped in one place in the reducer for every event type,
  so the card's `Last active` line comes from the projection rather than a query
- Room schema 2, with a migration for that column
- The Areas screen, the swipe gestures with state gating, drag reorder, the undo
  snackbar, the conflict card, and all the sheets
- The floating tab bar
- 41 Material Symbols Rounded icons at weight 500, as committed vector drawables

### Seven defects the device found

Recorded because they are the class of thing that passes a build and fails a person.

1. **No `VIBRATE` permission.** The first haptic ever fired took the app down.
   Haptics now also fail soft, because feedback must never break a screen.
2. Status bar and tab bar insets on the Areas screen.
3. A spring undershoot drove a tab label to a negative width, which is a hard crash.
4. **A zero width border still draws a hairline.** This had put a permanent outline
   on every swatch, mood pill and focusable element in the app.
5. Sheets did not lift for the keyboard, leaving the primary action unreachable.
6. The revealed swipe action did not match the card's height or corner radius.
7. Missing autofocus on the new area name field.

### Design work

`docs/DESIGN_RESEARCH.md` records the August 2026 review of current machine
generated design tells and premium interface practice. Eight entries were added to
the dated tell list in `design-v3.md` 15.1, which that document instructs be updated
before each release.

Material 3 Expressive was adopted as the motion model, springs rather than duration
and easing, with `design-v3.md`'s named curves kept authoritative. Four craft fixes
came out of the research and are in the code: cap height trimmed labels inside
contained controls, a designed keyboard focus state, optical nudge on asymmetric
glyphs, and trailing glyph padding compensation.
