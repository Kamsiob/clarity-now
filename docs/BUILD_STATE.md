# Build state

Where the build actually is. Updated at the end of every phase.

**Last updated:** August 27, 2026, end of phase 12 and of the rules pass, which ran in
the same tree at the same time.
**Phases 4, 5, 6, 7, 8, 10, 11 and 12, the facts phase, the slot bindings phase and the
rules pass are all built and all awaiting the check that closes them.** Everything this
file says about focus sessions, about the engine, about the Pulse, about Momentum, about
the Report, about first run, about Settings, about the six widgets, the three app
shortcuts and the quick settings tile, about the twelve facts and fourteen rules, about
the nine families that gained a slot binding and about the pattern cooldown, the check 1
narrowing and the fourth simulator reading is true of the source and is not yet true of
anything installed.
**Version:** the orchestrator chooses it at the release. `MASTER_BUILD_PROMPT.md` 16.7
makes the number a deliberate choice and the release is the orchestrator's closing step.
The recommendation is still **0.11.0, versionCode 1100**, one minor bump for all three
rather than three: onboarding, the tutorial, Settings, About and the backup path all arrive
as screens on contracts that already existed, the facts phase and the slot bindings phase
between them change what the engine can say without adding a screen, a permission or an
event type, the event catalog is untouched, and nothing in the log format moved. A major is not warranted before the ship phase.
**Phase 12 argues for one more minor bump on top of that, 0.12.0, versionCode 1200.** It
adds three surfaces outside the app that did not exist before, six widgets, three
shortcuts and a tile, which is something a person can do that they could not do the day
before, and that is what a minor has meant every other time in this file.
**Installed and verified on:** Pixel 8 (`shiba`), over USB, last at the end of phase 3c.

**No device check has run since phase 3c, and none of them failed: none was attempted.**
No `adb` command was issued from phases 6, 7, 8, 10 or 11, from the facts phase, from the
slot bindings phase or from the rules pass, and none of those three ran a Gradle task
either, because another workflow was building in the tree. The orchestrator runs the
closing build, install and device pass, so every claim in the phase sections below is a
claim about source and unit tests, and the readings from the facts phase, the slot bindings
phase and the rules pass are a claim about source alone. **The working tree also holds
uncommitted phase 12 widget work from that other workflow**, which is not part of the rules
pass and is not measured by anything in the rules pass section: the simulator compiles
`domain`, `data.event` and `devtools` and reaches none of it. **Two of the three phase 6 integration gaps are
closed**: `ClarityShell` hosts every tab, and phases 7 and 8 hung Momentum and the Report
on the two branches that read `UnderConstruction`. The corpus is packaged by a Gradle task
into `assets/corpus/` and all three engine surfaces read the same packaged files, so a
session must not add a second copy. **The third is still open: `MainActivity` does not
route `PulseIntents.opensPulse`**, so tapping the Pulse reminder opens the app at whatever
tab it was on.

**One thing phase 10 built is not reachable, and it is one line at four call sites.** The
tutorial's five spotlight targets need `Modifier.tutorialTarget` at each of them; only the
tab bar has it. Until the FAB, the first area card, the Focus chip and the Pulse chip wear
it, the tutorial does not start and `hasSeenTutorial` is never written. That is by design
rather than by accident and it fails safe, but it means **the phase 10 device check cannot
pass as things stand.**

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
| 4. Focus sessions | done, device checked | closed |
| 5. Engine skeleton and simulator | done | closed |
| 6. Pulse | done, device checked. The reminder route is closed | closed |
| 7. Momentum | done, device checked | closed |
| 8. Snapshots and the Report | done, device checked | closed |
| Facts and rules, the facts phase | done | no issue, owner approved |
| Slot bindings, and the third simulator reading | done | no issue, owner approved |
| The rules pass, and the fourth simulator reading | done | no issue, owner approved |
| The persona repair, and the fifth simulator reading | done | no issue, owner approved |
| The executive function engine gaps, 14b.3, 14b.4, 14b.8, 14b.9 | in progress | #27, #42, #43, #45 |
| 9. Corpus | not started. The fifth reading settles which job it is: **authoring to fix silence**, and says in advance that authoring alone cannot reach the band | #7, #44 |
| 9b. Guidance, layer six | not started | #8 |
| 10. First run | built, all five tutorial targets landed, awaiting the device check | #9 |
| 11. Settings, About, data | built, awaiting the device check | #10 |
| 12. Widgets and notifications | built, all six routes landed, awaiting the device check and the preview captures | #11 |
| 12b. Design surfaces, the polish pass | built, awaiting the device check | #54 |
| 12c. The re-entry screen | not started. 14b.4 left it without a phase; this run assigned it one | #56 |
| 13. Ship | in progress. Store listing, the About disclaimer and the baseline profile generator are in | #12 |

---

## What the device check found at 0.9.0

Run on the Pixel 8 (`shiba`) at versionName 0.9.0, versionCode 900, with `adb logcat`
checked after every step. **Zero fatal exceptions across the whole pass.** This is the
check that covered phases 4, 10, 12b and the accessibility pass at once, because they had
all landed in one tree.

### The two phase 4 checks that could not be met by reasoning both pass

**The session survives a process kill.** A 25 minute session was started, the ring read
`24:57`, `adb shell am force-stop` killed the process, and the app was relaunched. **It
came back straight to the running session** rather than to the tab it was on, and the
ring read `24:32`: exactly the twenty five seconds of wall clock that had passed. The arc
redrew at the right position, which is the property that proves it is computed from the
persisted end instant rather than animated from a counter.

**Back leaves the session running.** Back went to Areas, the notification stayed posted,
and the area card carried `In focus, 25 minutes left` with the play glyph in the area's
color.

**The notification was read out of `dumpsys` rather than judged from a screenshot**, and
it carries what 14b.6 asks for: `channel=ongoing`, `category=progress`, flags
`ONGOING_EVENT|ONLY_ALERT_ONCE|LOCAL_ONLY|PROMOTED_ONGOING`, and two actions, `Add 10 min`
and `End`, both broadcasts rather than activity launches. **`PROMOTED_ONGOING` was granted
by the platform**, so the Live Update treatment is live rather than degraded to an
ordinary ongoing notification.

**The completion screen is a success state.** `Session complete`, the item title,
`2 minutes in Today`, then `Mark item complete` and `Done`. The word this project renamed
away from reaches nothing a person can see.

### The tutorial runs, which it could not before

All five spotlights were walked and each aligned on its element: the FAB, the first area
card, the Focus chip, the Pulse chip and the tab bar. Phase 10 had left four of the five
targets without a `tutorialTarget`, so the mechanism waited rather than running wrong,
which was the designed failure and was the right one.

### The text size control, at the largest step

Set to `Largest` and every screen re-laid out: type grew, spacing grew with it, the
segmented control wrapped `Choose from queue` to two lines rather than truncating, the
item title wrapped to two lines on the area card, and **the floating tab bar held**,
which was the tightest element by a wide margin in the static analysis. Nothing clipped
anywhere the pass looked.

### One defect found, and it was invisible

**The Momentum area balance percentages did not sum and nothing said why.** Two areas
read 64 and 21 percent under a sidehead called `Area balance`, with fifteen points
unaccounted for and every automated gate green. Both numbers were correct: the shares are
shares of every user activity event in the fortnight, and answering a Pulse or changing a
setting belongs to no area. It is fixed by stating the denominator rather than by changing
it, and the reasoning is in its own commit and in a test on the fact.

**That is the argument for this pass in one item.** It was not a rendering bug, a crash or
a contrast failure. It was two true numbers that a person would add.

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

## What the device check found in phases 7 and 8

Run on the Pixel 8 at 0.8.0, logcat clean. All four tabs now render and all three
Contemplative worlds exist.

**One defect, and it was the first screen that ever spoke to a person.** On a two day
old install Momentum read `A narrow fortnight.`, and before that `A still fortnight.`
Both describe twelve days that had not happened.

The cause is that a fact window is always its full width, so `window.dayCount` cannot
tell a fortnight of data from a fortnight of window, and every fortnight family
qualified on day two. Every fixture in the suite had history behind it, so nothing
caught it: it took a fresh install probe. The fix is one criterion,
`fortnightOfHistory()`, applied to every fortnight horizon headline except `firstDays`
and `cleanSlate`, which exist precisely for the young and the empty case. The headline
now reads `Early, but it is starting.`

That is Addendum 01 7d's requirement, that the first weeks are honest about what they
do not have yet, enforced by a guard rather than by language.

**The Report renders as 11.1 specifies**, including the parts easiest to get wrong: the
week ribbon with no axes, no gridlines, no values on the marks and no card, with the
caption beneath carrying the numbers so the ribbon is never the sole carrier of a
claim; two observations rather than a padded four; and no Pattern section at all,
correctly, because `weeksOfData` is under three. It opened with `Your first week. There
is not much to compare against yet.`

**Recorded rather than fixed:** three corpus catalogs are built per process, one per
surface, where MASTER_BUILD_PROMPT 11.7 asks for one. No sentence is wrong because of
it and all three parse the same assets. Issue #55 has the worked out fix.

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
- Calm mode: less color in every wash, no list entrance, a shimmer that holds still, and
  a bottom sheet that still travels. It followed the OS reduce motion setting through
  phases 3b to 8 because there was no Settings screen; **phase 11 built its row**, and it
  still follows the system until somebody touches that row
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

**Outside the app**, added in phase 12 and true of the source rather than of anything
installed.

- **Six widgets**, all reading one snapshot the app rewrites whenever the content
  changes and a job refreshes every six hours. They draw the right thing today; where a
  tap lands is the routing gap in the open questions below
- **Three shortcuts** on a long press of the icon: `Quick capture`, `Start focus`,
  `Today's Pulse`. The middle one lands on the Focus surface today and the other two open
  the app, for the same reason
- **One quick settings tile**, which says `Start focus` or `End focus` depending on the
  session the log holds, opens the chooser from the first and ends the session from the
  second, and asks a locked phone for its passcode before either

## What is deliberately not there yet

Each of these is a phase, not an oversight. See the linked issue for why.

- The **Pulse chip** in the Areas header, which arrives with Pulse in phase 6 rather
  than sitting inert. The Focus chip beside it landed in phase 4 and is permanent.
- The **archived areas view**, issue #15. Archiving works and removes the area from
  the list, but there is still no way back. **Phase 11 was supposed to carry it and did
  not**, so this bullet has outlived the phase it was assigned to. The archive glyph is
  in the Areas header and `AreasRoute` passes it an empty lambda. Unarchiving, the typed
  permanent deletion and the `AREA_DELETED` tombstone go with the screen. Worth knowing
  before archiving anything you care about.
- **Momentum and the Report** are built, phases 7 and 8, and neither renders the honest
  placeholder line any more. Issue #16 is answered by the two sections below.
- The **weekly banner** on Areas is built, phase 7. It had been recorded here as absent
  since phase 2 because its sentence comes from the engine.
- The **re-entry surface**, the phase 6 half of issue #27. Detection is built and
  nothing renders it. The surface has to be able to say nothing, and saying nothing is
  an engine decision; the engine can now say nothing, and the screen is phase 6.
- **Everything the engine says**, which is all of it. Phase 5 built layers 1 to 5 and no
  caller, so no sentence the engine produces has reached a device. Phase 6 is the first
  screen that asks it anything.
- The **closing line on the Report**, with its accept pill and its decline. The block is
  built and always empty, because a closing line is layer six and layer six is phase 9b.
  Accept settles the pill and writes nothing; decline records nothing at all and never
  will, because there is no `PLAN_DECLINED` event and nothing that could count one.
- **Past reports**, which are built and empty, because nothing writes `REPORT_GENERATED`.
  See the known defect below.
- The **debug menu action** that rebuilds the cache from event zero. Phase 11 built the
  export half of this, so every export now runs a full rebuild from event zero as its
  correctness check, and `ClarityRepository.rebuildCacheFromLog` itself still has no
  caller.
- The **medical disclaimer sentence in About**, phase 13. `MASTER_BUILD_PROMPT.md` 14.4
  and 16.11 want it verbatim in the app and in the store listing at the same time, and
  the listing is phase 13, so phase 11 left the place for it marked rather than shipping
  it early.

## Known defects and open questions

- **The WorkManager permission question is answered, phase 12, and the answer is that
  the rule was wrong rather than the build.** Open since phase 2.
  `MASTER_BUILD_PROMPT.md` section 18 said no permission beyond notifications was in
  scope for v1, section 3 requires WorkManager, and 12.1 and 13.3 both schedule work
  with it. **The merged manifest was read for both variants rather than reasoned
  about**, at `app/build/intermediates/`, on the debug main manifest, the debug
  packaged manifest and the release main manifest. All three carry the same list, and
  it is seven entries and not the six this file used to claim:
  `POST_NOTIFICATIONS`, `POST_PROMOTED_NOTIFICATIONS` and `VIBRATE` from this app,
  `WAKE_LOCK`, `RECEIVE_BOOT_COMPLETED` and `FOREGROUND_SERVICE` from work-runtime,
  and `<applicationId>.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION` from androidx.core,
  which is a signature permission the app both declares and uses and which nothing
  else on the phone can hold. **`ACCESS_NETWORK_STATE` is in neither**, removed with
  `tools:node="remove"`, and no network permission appears in either variant.
  **Plainly: the app as built does not match what section 18 promised.** Four of the
  seven are beyond notifications. Section 18 now states the list above and why each
  one stays, because dropping WorkManager would cost the Pulse reminder and the widget
  refresh, both required by name, and `VIBRATE` is required by name in `design-v3.md`
  9. None of the four is a runtime permission and none appears in the permission list
  a person is shown. **What is unchanged is the network half**: phase 6 checked the
  `ACCESS_NETWORK_STATE` removal against work-runtime 2.11.2 rather than assuming it,
  a request with no constraints never reaches the network tracker, and **adding any
  constraint to any work request in this app means putting that permission back**,
  which changes what the privacy policy invites people to verify and stays the owner's
  call. `DECISIONS.md` still carries the entry as open and it can now be closed with
  this; phase 12 could not edit that file, which another workflow held.
  **One number in the phase 6 and phase 11 records is wrong because of this**: they say
  six permissions, and the seventh, the androidx.core signature one, was never counted.
- **The re-entry surface is not built and has no phase.** `MASTER_BUILD_PROMPT.md`
  14b.4 and `design-v3.md` 11.2 assigned it to phase 6, phase 6 built the two engine
  side rules that follow it and did not build the screen. Both documents now say so at
  the point a session would otherwise read them as shipped. A person returning after a
  fortnight today sees the ordinary Areas screen and a Pulse that stays quiet for two
  days: nothing measures their absence, and nothing greets them. **Assigning it is the
  owner's call.** The two candidates are a phase of its own, since it is one screen and
  it is finished when it works, or phase 10, which already owns the first thing a person
  sees.
- **`MainActivity` routes one intent, and phase 12 made that gap six surfaces wide.**
  It calls `FocusIntents.opensFocusSession` and nothing else. So the Pulse reminder
  still opens the app at whatever tab it was on, which is the phase 6 gap, and now the
  taps that open an area, open capture into the inbox, open Momentum or start a session
  on one item do the same, along with two of the three app shortcuts. **Every predicate
  they need already exists**, in `WidgetIntents` and `PulseIntents`, written as
  contracts by the phases that could not reach that Activity, and each is one call in
  `onCreate` and one in `onNewIntent` beside the call that is there. The two surfaces
  that do work are the `Start focus` shortcut and the quick settings tile, because both
  send the one action that is routed. **This is the single highest value hour left in
  the tree**: it is the difference between six widgets and three launcher surfaces that
  deep link and the same ten that merely launch. The other two phase 6 gaps are closed:
  `ClarityShell` hosts every tab, and the corpus is packaged into `assets/corpus/` by a
  Gradle task that all three engine surfaces read from.
- **Nothing writes `REPORT_GENERATED`, and three consequences follow that are not design
  decisions.** `MASTER_BUILD_PROMPT.md` 11.3 step 9 belongs to `ClarityRepository`, which is
  the only writer in the app and has no method for it. So 12.3's cadence question is asked
  correctly and always answers due, which means the report composes on every open rather
  than once a week; `FiringHistory` never learns what the Report said, so the ninety day
  variant exclusion and the fourteen day family cooldowns cannot vary it week to week; and
  the History page is empty, because it reads the projection and the projection is fed by
  the log. The report is deterministic either way, so a person sees the same page rather
  than a changing one. **It is one method and it is the whole of what stands between phase 8
  and shipped.**
- **A past report has no headline to draw.** `ReportGenerated` carries `headlineKey` and
  `headlineVariantKey` and not the headline's rendered text, while `renderedSections`
  carries the text of every observation, so `design-v3.md` section 5 gives past report
  headlines a display treatment for a string the payload cannot supply. Re-realizing the
  variant would be a second path to a sentence, which `CLAUDE.md` rule 8 closes, and the
  facts of that week are gone in any case. **The fix is one more string on the payload,
  which is a change to the committed format in `docs/EVENT_FORMAT.md`**, so it is the
  owner's call. `DECISIONS.md` carries it as open with the recommendation stated and not
  taken.
- **Three Addendum 01 items assigned to phase 8 did not land, and they are unassigned.**
  The week long Report suppression after a return (14b.4), capacity aware decline detection
  and its cyclical persona test (14b.9), and the estimate calibration facts, their floor and
  the delta veto (14b.8). All three are fact extraction and rule criteria in `domain.engine`
  rather than screen work, and none has a fact behind it in `domain/engine/facts/` today.
  **Two of the three are refusals**, so nothing fails while they are missing: a person
  returning after a fortnight can be told about the gap by the first report they open, and a
  person whose activity is cyclical can be told they are declining when they are not. Three
  lines in `MASTER_BUILD_PROMPT.md` 17 now say so at the point a session would read them as
  met. **The recommendation is phase 9 and it is the owner's call**, recorded in
  `DECISIONS.md`.
- **The length band rule is applied as a preference and the phase 5 gate is not lifted.**
  `CLARITY_LOGIC_ENGINE.md` 7.5 forbids two consecutive leads from the same band; the
  composer prefers an alternative and takes the highest ranked line anyway where a section
  has none, because dropping a true observation for cadence is the trade 11.4 forbids in
  the other direction. `ReportInvariants` therefore does not assert it, so neither the ten
  thousand week run nor the persona year measures it. The 715 collisions across 451 reports
  from phase 5 remain the baseline and phase 9 is what moves them.
- **There are now three engine catalogs in the process** where 11.7 asks for one, one per
  coordinator. Phase 6 recorded the second, phase 8 added the third, and the cause is the
  same each time: the one lazy binding that would serve all three belongs in `ClarityGraph`,
  which has been outside every surface phase's file list. The cost is two extra parses of
  three markdown files, on a background dispatcher, the first time each tab is opened. The
  fix is one binding and a constructor parameter, at which point `MomentumGraph`,
  `ReportGraph` and `AssetCorpus` all go away together.
- **`ReportCoordinator` is in `ui.report` and belongs in `domain.report`**, beside
  `PulseCoordinator` and `MomentumCoordinator`. Nothing in it depends on Android. It is a
  package line and an import.
- **The banner's caption binding table is in `domain/momentum/BannerCaptions.kt` and belongs
  in `domain/engine/realize/SlotBindings.kt`**, which is where layer 4 authors every other
  slot binding. The caption bench has slots and is not a family, so `bindingsFor` has nowhere
  to look it up and phase 5 left it unbound. `BannerCaptionsTest` fails the build if a
  caption line in the real corpus has no binding, so the table cannot silently rot while it
  waits to move.
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
- **The corpus totals were corrected by the facts phase and are no longer a defect.** Four
  stated totals were wrong, all four are now the counted figure, no corpus line was added
  or removed to reach it, and `CorpusTotalsAuditTest` recounts every stated total in the
  three files and in 11.1 on every run. This bullet is kept rather than deleted because the
  phase 5 entry below still records the finding as open, and the two would otherwise
  disagree.
- **Eight of the twelve simulator checks in `CLARITY_LOGIC_ENGINE.md` 12 fail**, by design
  and with a date and an issue on each. Ten were built in phase 5 and the facts phase added
  the family coverage reading and rewrote the stage one. They are listed here so nobody has
  to rediscover which are expected. The current numbers are in the rules pass section below
  and the reasoning is in `DECISIONS.md`.
- **Eight families and two hot family stages have rules that never qualified once**, across
  eleven simulated years and four measurements. In the Pulse, `throughput`, `burst` and
  `queueDrain`, unchanged since phase 5, and `accumulation` stage 2. In the Report,
  `clearing`, `fragmented`, `netOutflow`, `queueDrained`, `weekendShift` and
  `intakeVsOutput` stage 3. **The rules pass diagnosed every one of them at the rule that
  carries it and deliberately moved no threshold**, because every stage threshold in a
  ladder is a corpus stage header that `StageRangeTest` asserts against, so lowering one
  would make the engine say a sentence about a day that did not happen. Three of them,
  `queueDrain`, `queueDrained` and `clearing`, are a real anchoring error and are waiting on
  a fact 3.1 does not declare: the queue an area held immediately before the promotion that
  took it to zero, rather than the queue read at the window boundary. The other five are the
  persona set rather than the rule, and the rules pass entry in `DECISIONS.md` says which is
  which. **`insufficientData` has left this list**: it was unreachable by construction, and
  it is now rendered by the Report itself rather than by a rule.
- **The check 1 conflict is closed, and this bullet is kept rather than deleted because the
  slot bindings section below still records it as open.** `neglectedArea` and
  `areaGoneQuiet` were the only families vetoed anywhere in the run, 107 vetoes, every one
  of them check 1, because their rules require an area to have been silent for one to three
  weeks and check 1 forbade naming an area with no events in the window. The owner ruled
  that **the check was right and the writing was wrong**, and it is narrowed rather than
  widened: `ClarityRule.absenceSubject` marks a rule whose subject is the silence, and
  `AbsenceSubject` grants the exception only to an area with a real lifetime, not new, and
  a measured `daysSinceLastEvent`. A brand new empty area is still refused by every rule.
  The fourth measurement records **0 vetoes across the whole run and 92 absences named on
  purpose.**
- **The tutorial cannot start**, phase 10. Four of its five spotlight targets do not wear
  `Modifier.tutorialTarget`: only the tab bar does, and `TutorialTargets.ready` requires
  all five. The overlay therefore waits and `hasSeenTutorial` is never written, which is
  the mechanism failing safe rather than failing quietly. The fix is one line each at the
  FAB, the first area card, the Focus chip and the Pulse chip in `ui/areas/`. **The phase
  10 device check cannot pass before it lands.**
- **Settings does not cover the floating tab bar**, phase 11. `design-v3.md` 10.15 makes
  it a pushed screen; `ClarityShell` draws the bar as a sibling above the tab content and
  the shell was outside phase 11's file list, so the surface is hosted from inside the
  Areas tab and reserves room at the foot instead. Everything is reachable and readable.
  The remedy is one branch in the shell beside the Focus surface.
- **`SETTING_CHANGED` is never written**, phase 11. `After completing` is the one setting
  the specification says writes an event, and it does not. The event type, the reducer
  fold, the Trail row and `TrailFacts` all handle it already; what is missing is a method
  on `ClarityRepository`, shaped like `recordSettingChanged(key, previousValue, newValue)`
  beside `recordAppOpened`. `commit` is private, so the event is absent rather than written
  down a second path, which is the right way round but is still a gap.
- **The first run gate latches for the process**, phase 10, so `Replay the welcome`,
  `Replay the tour` and the return to onboarding after an erase all take effect on the next
  cold start rather than immediately. The erase itself is immediate. This is a consequence
  of the rule that makes beat 3's write survive the reveal, not an oversight, but it is a
  behavior a device check will notice and should not report as a defect.

---

## The rules pass delivered

**Not on the issue board.** The owner ordered it after reading the third measurement,
which found that Pulse silence at 68 percent was **mostly trigger windows and two
specification contradictions rather than corpus depth**, and that emptying the filtered
column entirely would still leave silence at 31 percent with six of eleven personas
outside the band. The pass was to fix the rules. Phase 9 writes the sentences and comes
after it.

- **The pattern section waits three weeks.** `Selector.PATTERN_COOLDOWN_DAYS` is 21,
  applied as a floor on a `REPORT_PATTERN` selection rather than on a family's
  declaration, because `decliningActivity` is a headline family and a pattern family at
  once and the two share one `(family, subjectId)` cooldown key. The flat Report 14 could
  not hold anybody out of anything: a report is recorded against its week start and
  selected against its week end, so two consecutive reports are 14 days apart on the only
  clock `FiringHistory` keeps.
- **Check 1 is narrowed, and the specification conflict the last phase refused to settle
  is closed.** `ClarityRule.absenceSubject` marks a rule whose subject is an area's
  silence, and `AbsenceSubject` in `domain.engine.validate` holds the one exception that
  both implementations of check 1 read. Three rules carry the flag: `neglectedArea` at
  both stages, `areaGoneQuiet` and `areaRevival`. Phantom protection is untouched, because
  the exception requires a real lifetime, a non new area and a measured
  `daysSinceLastEvent` rather than the never sentinel.
- **A queue that emptied has to have been finished rather than deleted.**
  `drainedByFinishing()` is one criterion shared by `pulse.queueDrain` at both stages and
  by `report.observation.queueDrained`, and `drained.hadAQueue` is gone as padding that
  bought a free point of specificity.
- **`insufficientData` left the engine.** Its rule was unreachable by construction, the
  composer asking for a pattern only at three weeks of data and the rule requiring fewer.
  `ReportRules.RENDERED_DIRECTLY` names it, `ReportComposer` renders it through
  `ReportLanguage.insufficientData`, and `CatalogIntegrity` reads the new register so a
  family cannot leave the engine without somebody deciding it should. **Layer 5 is not
  bypassed**: the line still goes through `VariantChoice`, `SlotRenderer` and
  `ClarityValidator`.
- **Not one threshold was moved.** Every stage threshold in a ladder is a corpus stage
  header that `StageRangeTest` asserts against, so moving one to make a family fire in a
  measurement run would make the engine say a sentence about a day that did not happen.
  The eight families that never qualify are diagnosed instead, at the rule that carries
  each of them.
- **Not one corpus line was written, added or edited.** That is phase 9.
- **The simulator now reports how each report section shared its slots out.**
  `SimulationAggregate.sectionSlots` measures concentration rather than fill, because a
  section can be 99 percent full while three families take everything, which is exactly
  what the pattern section was doing and what nothing in the run reported until somebody
  read a dump by hand.

### What the simulator measured, beside the three earlier baselines

The same eleven personas, a full simulated year each. 3,148 opens, 15,281 events, 451
reports, 419 pattern slots, 11,901 engine invocations.

| gate | target | phase 5 | facts | bindings | after the rules pass |
|---|---|---|---|---|---|
| Pulse silence, all personas | 8 to 25 percent of opened days | 76 percent | 73 percent | 68 percent | **68 percent, unchanged** |
| Pulse silence, per persona | the same band | 43 to 98 | 42 to 98 | 40 to 97 | **40 to 97, unchanged** |
| Pulse families that ever fired | 11 of 11 | 6 of 11 | 7 of 11 | 8 of 11 | **8 of 11, unchanged** |
| every family the corpus declares fires | 78 of 78 | not measured | 58 of 78 | 60 of 78 | **65 of 78** |
| every stage of every hot family fires | all | 29 hot, one gap | 31 hot, two gaps | 33 hot, two gaps | **35 hot, the same two gaps** |
| no variant repeats inside ninety days | none | 7,384 | 7,430 | 7,445 | **7,376, tightest after 1 day** |
| no family over a fifth of a year's Pulses | 20 percent | 27 to 60 | 25 to 57 | 25 to 51 | **25 to 51, unchanged** |
| no two consecutive leads share a band | none | 715 | 725 | 716 | **712 across 451 reports** |
| no three parallel numeric clauses | none | 27 runs | 37 runs | 36 runs | **41 runs** |
| layer 6 silence | at least 15 percent | not measurable | not measurable | not measurable | **not measurable** |

Three readings the earlier tables could not carry:

| reading | before | after |
|---|---|---|
| layer 5 vetoes across the run | 107, every one check 1 | **0** |
| pattern slots | 416 of 419 filled, 8 families ever held one, top three took 402 | **401 of 419 filled, 12 families held one, top three took 296** |
| absences named on purpose | 0, all 107 vetoed | **92** |

The four enforced checks still pass. Family coverage in full: PULSE 8 of 11,
REPORT_HEADLINE 13 of 17, REPORT_OBSERVATION 20 of 21, REPORT_PATTERN 12 of 16,
MOMENTUM_HEADLINE 8 of 8, AREAS_BANNER 4 of 5. The five families that gained a voice are
all on the Report: `neglectedArea` 71, `growingQueues` 55, `areaGoneQuiet` 21,
`focusHabitForming` 19 and `broadeningFocus` 1. **One of the thirteen still counted quiet
left the engine on purpose**, `insufficientData`, and `SimulationChecks` now says so on
its own failure line.

**The pattern section, before and after**, which is the reading the cooldown was set by:
`reportedVsActual` 233 to 170, `comebackPattern` 98 to 71, `consistentRhythm` 71 to 41,
and `growingQueues` 0 to 55, `areaGoneQuiet` 0 to 21, `focusHabitForming` 0 to 19,
`decliningActivity` 2 to 13, `queueEquilibrium` 2 to 4, `broadeningFocus` 0 to 1. Empty
slots rose from 3 to 18, which the cooldown was documented in advance as costing.

### The reading, and it names which job phase 9 is

**Pulse silence is 68 percent and it did not move by a single day.** 2,167 silent days
out of 3,148 opens, splitting into **1,185 where a rule qualified and every candidate was
filtered, 971 where nothing qualified at all, and 11 with too little data**, which are the
same three numbers the third measurement produced.

**The cause is that the pass moved the Report and the Pulse was never in its reach.** The
pattern cooldown applies to `REPORT_PATTERN` selections; the check 1 narrowing is read by
`ClarityValidator` and `ReportIntegrity` for three families that only exist on the Report.
The one instrument that touches a Pulse rule, `drainedByFinishing`, touches `queueDrain`,
which fired zero times before the pass and zero times after. A pass with no Pulse
instrument in it cannot move a Pulse number.

**The owner's determination was stated in advance and the number decides it.**

> If silence lands near band, phase 9 is authoring to fix repeats. If it does not, phase 9
> is authoring to fix silence.

68 against a ceiling of 25 is not near band. **Phase 9 is authoring to fix silence.**

**What authoring alone could reach, measured rather than estimated.** Emptying the
filtered column completely, which is the most a deeper bench can ever do, leaves 982
silent days out of 3,148, which is **31 percent**, six points above the ceiling. Per
persona at that floor: `queueHoarder` 8, `sporadic` 9, `brandNew` 14, `highFocus` 16,
`balancedAcrossFour` 21, `abandoning` 26, `lowFocus` 42, `heavySingleArea` 44,
`acceptsEveryPlan` 45, `fastCompleter` 47, `longDormantRevival` 50. **Five of eleven
personas reach the band and six do not**, and the six are not the worst six today:
`balancedAcrossFour` is the worst persona in the run at 97 percent and lands in band at
the floor, while `lowFocus` sits at 63 today and would still be at 42.

**The owner's standing instruction applies and this phase stops here.**

> If after the rules pass and the hot family growth silence is still outside the band, do
> not keep grinding. Report the number, state the cause, and move on. An app that ships
> with 30 percent silence is better than one that does not ship.

The number is 68 percent, the floor authoring can reach is 31 percent, the cause is above,
and **no further rules pass is proposed.**

### Two deferrals the owner authorized

- **Warm and long tail families stay at their current depth for v1.** 11.1 grows hot
  families from four to eight lines per stage to sixty to a hundred; the other two tiers
  are not grown with them. A family that fires five times a year cannot repeat itself
  inside ninety days however thin its bench is, so lines added there buy variety nobody
  encounters.
- **Variant repeats at roughly 7,400 are the honest baseline, not a defect.** 7,376 here,
  against 7,445, 7,430 and 7,384 before it. 7.6 excludes a variant for ninety days and the
  benches are the size phase 5 found them. **The number is what phase 9 moves, and its
  movement is how phase 9 is measured.** A session that finds it high has found the
  baseline rather than a regression.

### What is deliberately not in this pass

- **Not one corpus line.** Unchanged across four phases and still the owner's condition.
- **No threshold moved.** See above. The eight families that never qualify are diagnosed
  in place and three of them are waiting on a fact rather than on a number.
- **The eight week pattern cooldown that would rotate every starved family.** Three weeks
  rotates two pairs at the head and reached four more families. Rotating all seven would
  take eight weeks and cost more empty slots, and that number is the owner's to set.
- **Splitting the `weekendShift` bench.** Three of its five lines claim absolute silence
  and two claim a share, and one rule cannot encode both without letting the looser branch
  select `Nothing has happened on a weekend in four weeks` for a month that had one. That
  is authoring work.
- **Layer 6**, still phase 9b, still not measurable.

### What the closing check still has to find

**No Gradle task and no `adb` command was run in this pass either.** The readings were
produced the way the two before them were: by compiling `domain`, `data.event` and
`devtools` with a driver out of tree against the committed corpus files and running the
year. The devtools sources compile clean with warnings as errors. **The unit suite has not
been run on this work.** The closing build settles whether `verifyClarity` is green and
whether `SimulatorTest`, `PatternCooldownTest`, `DarkFamilyRulesTest`, `SelectorTest`,
`CatalogIntegrityTest`, `ValidatorVetoTest`, `ReportIntegrityVetoTest`,
`ReportLanguageTest` and `ReportCompositionTest` all pass.

---

## The slot bindings phase delivered

**Not on the issue board.** The facts phase left the bindings out because
`domain/engine/realize/` was outside its file list, and `SlotBindingsTest` recorded
the cost in a sentence: **a family with a rule, markers in its lines and no binding
can only ever speak through the lines that happen to have no marker in them.** Nine
families were in that state. This phase bound them and measured the year again.

- **Nine families gained a binding they did not have**, and three more gained a slot
  inside one they already had. The nine: Pulse `switching` and `rebalance`, and the
  Report patterns `narrowingFocus`, `broadeningFocus`, `focusHabitForming`,
  `focusHabitFading`, `weekendShift`, `abandonmentPattern` and `comebackPattern`. The
  three: Momentum `comeback`, the Report headline `comeback` and the Report
  observation `areaRevival`, each of which can now render the gap an area came back
  from.
- **`SlotBindings.KNOWN_UNBOUND` still holds five entries and none of them is a
  regression.** `PULSE quietDay` is now a refusal rather than a gap: the fact its
  stages 2 and 3 wanted has arrived, and `{dayCount}` and `{sinceRef}` are exactly the
  markers a capped run of absence may not fill.
- **Thirteen more lines are held out in `EXCLUDED`**, taking it to 42, each with the
  reason written where it is excluded. Four of the thirteen are a shape that did not
  exist before: a line whose markers are bound correctly and whose **words** state a
  count no rule fixes, `Three weeks of stillness` under a stage that begins at
  fourteen days.
- **Not one corpus line was written, added or edited.** The owner's condition is
  unchanged and neither half of it is met yet.

### What the simulator measured, beside the two earlier baselines

The same eleven personas, a full simulated year each. 3,148 opens, 451 reports, 419
pattern slots, 11,833 engine invocations.

| gate | target | phase 5 | facts | after the bindings |
|---|---|---|---|---|
| Pulse silence, all personas | 8 to 25 percent of opened days | 76 percent | 73 percent | **68 percent** |
| Pulse silence, per persona | the same band | 43 to 98 | 42 to 98 | **40 to 97** |
| Pulse families that ever fired | 11 of 11 | 6 of 11 | 7 of 11 | **8 of 11** |
| every family the corpus declares fires | 78 of 78 | not measured | 58 of 78 | **60 of 78** |
| every stage of every hot family fires | all | 29 hot, one gap | 31 hot, two gaps | **33 hot, the same two gaps** |
| no variant repeats inside ninety days | none | 7,384 | 7,430 | **7,445, tightest after 1 day** |
| no family over a fifth of a year's Pulses | 20 percent | 27 to 60 | 25 to 57 | **25 to 51** |
| no two consecutive leads share a band | none | 715 across 451 reports | 725 | **716** |
| no three parallel numeric clauses | none | 27 runs | 37 runs | **36 runs** |
| layer 6 silence | at least 15 percent | not measurable | not measurable | not measurable |

The four enforced checks still pass. Family coverage in full: PULSE 8 of 11,
REPORT_HEADLINE 13 of 17, REPORT_OBSERVATION 19 of 21, REPORT_PATTERN 8 of 16,
MOMENTUM_HEADLINE 8 of 8, AREAS_BANNER 4 of 5. Per persona silence, worst last:
`queueHoarder` 40, `brandNew` 42, `heavySingleArea` 61, `highFocus` 61, `sporadic`
62, `lowFocus` 63, `acceptsEveryPlan` 63, `abandoning` 75, `longDormantRevival` 78,
`fastCompleter` 92, `balancedAcrossFour` 97.

**Pulse firing counts across the run**, which is the number that replaces a sentence
somebody remembered: `persistence` 268, `quietDay` 243, `concentration` 170,
`accumulation` 143, `rebalance` 111, `freshStart` 25, `switching` 16, `spread` 5, and
`throughput`, `burst` and `queueDrain` at zero. `switching` fired nothing before this
phase and `rebalance` fired 20 times at stage 2 only; it now fires 111 across both its
stages. That is the binding doing exactly what the facts phase predicted it would.

### The reading, and it is not the one a bigger corpus answers

**Improved, and still nowhere near the band.** Eight points below phase 5, five below
the facts phase, 68 against a ceiling of 25.

**The split is arithmetic rather than judgment.** The 2,167 silent Pulse days are
1,185 where a rule qualified and every candidate was filtered, 971 where nothing
qualified at all, and 11 with too little data, against 1,348, 944 and 11 after the
facts phase. Bench depth is the only fix for the first column. **If phase 9 emptied
that column completely, silence would be 31 percent and still above the ceiling**,
and six of the eleven personas would still be outside the band. Authoring is
necessary and provably not sufficient, and that one sentence is what this measurement
was for.

**The eighteen dark families divide three ways**, and only the third is authoring.
Nine never qualified once across eleven simulated years, so a threshold or a window is
wrong rather than a bench being thin: `throughput` needs completions to exceed
additions inside the one day Pulse window and that never happened at all, `burst`
needs three completions in one area in one day, `queueDrain` needs a queue of three to
reach zero inside a day, and `clearing`, `netOutflow`, `queueDrained`, `fragmented`
and `weekendShift` are the same shape at the Report grain. `insufficientData` is worse
than a threshold: 6.3 admits a pattern only with three weeks of data and that family's
rule fires only with fewer, so it is unreachable by construction. Two more,
`neglectedArea` and `areaGoneQuiet`, are **the only families vetoed anywhere in the
run**, 107 times, every one of them check 1, because their rules require an area to
have been silent for one to three weeks and check 1 forbids naming an area with no
events in the window. The remaining seven qualify and lose a slot that exists once:
419 pattern slots existed, 416 were filled, and three families took 402 of them.

The full tables, the per criterion evidence and the open question that comes out of it
are in `DECISIONS.md` under the entry of the same name.

### What is deliberately not in this phase

- **Not one corpus line.** Unchanged and deliberate.
- **No threshold was touched.** Nine families never qualify and the fix is a rule
  change, which is phase 9's to make with the personas open beside it. Changing a
  threshold to make a family fire in a measurement run is the shape of tuning a
  number until a gate goes green.
- **The check 1 conflict is recorded, not resolved.** It is a conflict between section
  8 and two families in `CORPUS_2_REPORT.md`, and CLAUDE.md says to stop and ask.

### What the closing check still has to find

**No Gradle task and no `adb` command was run in this phase either.** The readings
were produced the way the facts phase produced its own: by compiling the domain, the
devtools and a driver out of tree against the committed corpus files and running the
year. **The unit suite has not been run on this work.** The closing build settles
whether `verifyClarity` is green and whether `SimulatorTest` and `SlotBindingsTest`
still pass.

---

## The facts phase delivered

**Not on the issue board.** The owner approved it after reading the phase 5 readings and
it ran before phase 9, for one reason: twelve families and stages had authored, approved
language and **no rule at all**, because the fact their trigger names was not declared in
`CLARITY_LOGIC_ENGINE.md` 3.1. Writing them more sentences would have changed nothing, and
phase 9 is mostly writing sentences.

- **Twelve facts, all of them declared in 3.1's terms and none of them approximated.**
  `AreaFacts.swapsInWindow` and `AreaFacts.dormantDaysBeforeReturn`, the two that were
  cheap and already specified. `AreaFacts.weekEventsSeries`, so a second return can be
  counted. Five weekly series on `HistoryFacts`: `weekAreaCountSeries`,
  `weekFocusStartedSeries`, `weekFocusCompletedSeries`, `weekFocusEndedEarlySeries` and
  `weekWeekendEventsSeries`. And `currentQuietRunDays`, `currentSingleAreaRunDays` and
  `currentSingleAreaRunAreaId`, which are the scoped exception to the streak ban.
- **Started, finished and ended early are three series and not two.** The register asked
  for one `weekFocusSessionSeries` and it was deliberately not built under that name: the
  two families citing it read the started count, `abandonmentPattern` compares ended early
  against completed, and a killed process leaves a session with no terminal event, so
  subtracting one from another would attribute it to an ending the person did not choose.
- **`weekendShift` reads its own series rather than `CueFacts`**, which the register left
  open as a choice. 3.7 restricts `CueFacts` to layer 6, and its `weekdayOnly` is a twelve
  week share under a ceiling: it cannot substantiate a claim that nothing happened, and it
  answers over twelve weeks a family that speaks about four.
- **One new function on `TrailQueries`**, `eventsPerAreaByDay`. Everything else came from
  functions already on the facade, and no number is computed in the engine.
- **Fourteen new rules, taking the catalog from 92 to 106** across the same 78 families.
  Seven Pulse and seven Report patterns. `RulesAwaitingFacts.GAPS` is now empty, and the
  empty list is load bearing: `CatalogIntegrity.everyFamilyHasARule` reads it, so a family
  cannot go quiet again without somebody writing the entry that says so.
- **`StreakExceptionAudit`**, four checks that keep the run facts scoped, run over the
  shipped catalog and over deliberately wrong rules so that every failure branch has
  executed. `DECISIONS.md` carries the exception, the owner's reasoning, and an explicit
  warning against widening it.
- **`CorpusTotalsAuditTest`**, which recounts every stated total in the three corpus files
  and in 11.1 against the keyed lines beneath them. Four stated totals were wrong and are
  corrected in the files; no corpus line was added or removed.
- **The simulator reports two things it did not.** Pulse silence as a share of opened days
  **per persona and for every persona together**, and a per family firing count against the
  families the catalog declares, so "six of eleven families ever fired" is a number the
  build prints rather than a sentence somebody remembered. Both are in
  `SimulationAggregate`, both are checks in `SimulationChecks`, and both are deferred with
  a date and the issue that lifts them.
- **43 new unit tests**, 38 in four new classes and five added to the catalog and fact set
  tests that were already there.

### What the simulator measured after it, beside the phase 5 baseline

The same eleven personas, a full simulated year each. 3,148 opens, 451 reports, 11,843
engine invocations.

| gate | target | phase 5 | after |
|---|---|---|---|
| Pulse silence, all personas | 8 to 25 percent of opened days | 76 percent | **73 percent** |
| Pulse silence, per persona | the same band | 43 to 98 | **42 to 98** |
| Pulse families that ever fired | 11 of 11 | **6 of 11** | **7 of 11** |
| every family the corpus declares fires | 78 of 78 | not measured | **58 of 78** |
| every stage of every hot family fires | all | 29 hot, one gap | **31 hot, two gaps** |
| no variant repeats inside ninety days | none | 7,384, tightest after 1 day | 7,430, tightest after 1 day |
| no family over a fifth of a year's Pulses | 20 percent | 27 to 60 | 25 to 57 |
| no two consecutive leads share a band | none | 715 across 451 reports | 725 across 451 reports |
| no three parallel numeric clauses | none | 27 runs | 37 runs |
| layer 6 silence | at least 15 percent | not measurable | not measurable |

The four enforced checks still pass. The family coverage reading in full: PULSE 7 of 11,
REPORT_HEADLINE 13 of 17, REPORT_OBSERVATION 19 of 21, REPORT_PATTERN 7 of 16,
MOMENTUM_HEADLINE 8 of 8, AREAS_BANNER 4 of 5. **Not one of the twenty quiet families
lacks a rule**, which is the empty register seen from the other end.

**The stage row changed what it measures and the two columns are not comparable.** Phase 5
read a family's ladder off the stages that fired, so the highest stage anybody reached was
the highest stage the check knew about. The denominator is now the stages that have a rule,
which is askable only because the register is empty. `accumulation` never reaching stage 2
is the phase 5 gap, still open. `intakeVsOutput` never reaching stage 3 was always true and
is newly visible.

### What this did not fix, and phase 9 should read it before authoring

**Silence moved three points against a band of 8 to 25**, 2,383 silent days out of 3,148
opens becoming 2,303. Seven of the fourteen new rules
ever fired, for 87 firings, and Pulse silence fell by 80 days, so the phase delivered what
its rules could deliver and the shortfall is elsewhere. The silent days now split **1,348
where something qualified and every candidate was filtered, 944 where nothing qualified,
and 11 with too little data**, against 1,238, 1,134 and 11 at phase 5.

Three causes, and they are three different pieces of work:

1. **A bench whose slots have no binding.** `switching` has two rules and fired zero times.
   All eighteen of its statements name an area and `SlotBindings` has no entry for the
   family, so the realizer drops every line for an unfilled marker. The fact and the rule
   are two thirds of a working family; the third is one map entry. `rebalance` is the same
   shape and fired only at stage 2, where some lines carry no marker.
2. **A rule no simulated life satisfies.** `throughput`, `burst` and `queueDrain` have ten
   rules between them and fired nothing across eleven simulated years, exactly as at
   phase 5.
3. **A selector that declines to speak on a qualifying day**, 1,348 times. This is the one
   a bigger bench genuinely helps, because 7.6 excludes a variant for ninety days and a
   family with four lines per stage runs out.

### What is deliberately not in this phase

- **Not one new corpus line.** The owner was explicit: none is written until silence is
  inside the band and every family fires. This phase added facts and rules and corrected
  four counts, and correcting a count is not authoring.
- **The `switching` and `rebalance` slot bindings**, which are the difference between two
  families qualifying and two families speaking. They are a corpus and realization question
  rather than a fact question, and they belong with the work that reads the benches.
- **Layer 6**, still phase 9b. `CueFacts` is still extracted and unread, and the new
  weekend series exists precisely so that a Report pattern does not reach into it.

### What the closing check still has to find

**No Gradle task and no `adb` command was run in this phase.** Another workflow was
building in the tree, so the orchestrator runs the build, the unit suite and the device
pass. Everything above is a claim about source, and the simulator readings were produced
by compiling the domain, the devtools and a driver out of tree and running the year
against the committed corpus files. **The unit suite has not been run on this work**, so
the first thing the closing build settles is whether `verifyClarity` is green, and the
second is whether `SimulatorTest` still passes with two checks added to the report and one
of them measuring against a new denominator.

---

## The executive function engine gaps delivered

The four items `MASTER_BUILD_PROMPT.md` 14b assigned to phase 8 and phase 8 did not
carry, plus the sixth measurement. Issues #27, #42, #43 and #45.

Section 17 said of three of these that the checklist line was **vacuously true and must
not be read as met**, which is the most useful sentence anyone wrote in that document:
there was no estimate fact, so nothing for a veto to refuse; no precedent fact, so no
gate; and no re-entry criterion, so a person coming back after a fortnight could be told
about the gap by the first report they opened.

All three are now real checks and all three pass.

### The estimate delta is unformable rather than forbidden, 14b.8

**The rule was going to be a validator check. It is also a shape.** `TrailQueries`
divides the two magnitudes inside its own body and returns a ratio. **No quantity of
minutes, estimated or actual, exists anywhere above that line or anywhere in the fact
set**, so `actual - estimate` is not a subtraction a future rule author can write in one
step or in any number of them. Two reflection tests hold that shape rather than trusting
it: one walks every fact class and asserts the only estimate named fields are a count, a
multiple and a band, and the other asserts nothing public on the facade hands out an
estimate.

The ratio is **a multiple and never a percentage**, because a ratio of 2.4 rendered as
240 percent is one literal hundred away from `You were off by 140 percent`.

**Check 11 in the validator** is the belt to that suspenders, appended rather than
inserted so the ten checks three documents cite by number keep their numbers.

Eight choices are recorded at the fact. The two worth repeating here: **an estimate
changed after the item became active is ignored**, because a revision made mid work is a
progress report informed by the very thing being measured and honoring it moves every
ratio toward one and flatters the person; and the statistic is a **median rather than a
mean**, because one item left running over a holiday moves a mean to a number no week of
anybody's life resembles, and "tend to" is a median word.

**A naming warning for phase 9, recorded at the fact: it is a stay, not an effort.**
Nothing in this app measures time spent working. A thing estimated at an hour that
occupies a day and a half is a true reading of how estimates map onto days and a false
one of how long the work took.

**Measured: zero estimate deltas in 13,576 rendered strings across twelve persona
years**, and zero sentences mentioning an estimate at all, because the family that will
say one is phase 9's.

### A dip with a precedent is a rhythm, 14b.9

This is the correctness fix, not politeness. Without it the app tells somebody with a
cyclical or relapsing condition that they are deteriorating, on a fixed schedule,
forever, and it is technically accurate every time while the claim the sequence makes is
false.

**A subject's normal is the median of the weeks it moved in, not of all its weeks.** The
all weeks median is zero for anybody quiet more than half the time, which would have
passed the gate for exactly the most cyclical people. Depth is four bands; the current
fall is the unbroken stretch of low weeks ending at the newest closed bucket; a
precedent is any strictly earlier stretch at least as long and at least as deep.

**The fact distinguishes no precedent from not enough history to say**, and the gate
closes on `PRESENT` alone. That overrode a sentence in two specification documents, and
the argument is in `DECISIONS.md`: the fact's notion of low is a week under three
quarters of normal, and **no decline family asks that question**, so requiring the
absence of a precedent would have silenced a true observation every time the two
definitions came apart, invisibly.

**A gate rather than a rewording**, which 14b.9 requires: it is a filter at step 1b of
selection and not a criterion, because specificity is the criterion count and a
criterion would make the gated families **outrank** rules that require more.

**The persona is a life rather than a waveform.** The first attempt was three weeks on,
three weeks off, all year, in which every fall is a precedent for every other by
construction and the test proves nothing. It is now fifty three weeks of capacity
written out one at a time: twelve episodes, one to three weeks long, arriving after gaps
of two to six good weeks, bottoming anywhere from thirteen events in a week down to one,
recoveries of different heights, and the working days inside each week moving by hash so
no episode starts on the same day of a month.

**Measured: zero decline, neglect and fading observations across 52 weekly reports.**
The same year with the precedents forced to `NONE` produces **34**, and all 34 sit on a
fall whose precedent reads `PRESENT`, which is the assertion that stops the first one
being vacuous.

### A returning person is not told about the gap, 14b.4

A boolean, true on the return day and the six days after, asked of the last day the
window describes. **A boolean carries neither the length of the absence nor its date, so
14b.4's prohibition holds by shape** rather than by anybody remembering it.

**Measured**: the revival persona returns on day 251 after 195 days away and receives 22
sentences in the seven days after, none of them a decline, a neglect or a gap. **With
the suppression disabled it receives seven**: `A still fortnight.` on the Momentum
headline on four separate days, and a report headlined `Work moved again.` carrying
`Work had been the quietest area. It was not this week.` and `6 events. All week.`

That is what this item was for, and it is worth reading twice. Every one of those seven
is true, every one passes the integrity layer, and every one of them is a measurement of
an absence read back to somebody on the day they came back.

### The instrument, again: the app wrote events on days it was never opened

`ClaritySimulator` called each persona's act unconditionally, including on days the
persona did not open the app, **so two personas wrote item events on days with no
`APP_OPENED`, which the real app cannot produce.** Measured rather than estimated: 52 of
`sporadic`'s 116 closed days and 44 of `abandoning`'s 212 did, ninety six impossible days
in a single run of two lives.

**The install day is inside the gate, and that is the half that is easy to miss.** Setup
writes `AREA_CREATED`, which is a screen gesture like any other, so an install day the
persona happened not to open would put the same impossible event one line earlier.

**A clearing session that lands on a closed day is skipped and never deferred.**
Deferring makes a day depend on the days before it, which breaks the hash of the day
property the whole persona file rests on and which is why a seeded generator was refused
in phase 5.

### Two tests that had never agreed with the code, one of them red at HEAD

**`ReportInvariants` had been failing since the check 1 narrowing landed, and it was
committed red.** It restates section 9 by hand, deliberately, and it was never given the
one narrowing the absence subject flag carries, so it refused every `neglectedArea` and
`areaGoneQuiet` sentence while both the integrity layer and the validator passed them:
358 violations over ten thousand generated weeks and 112 across the persona years. The
four conditions are now restated there **with their literals rather than by reading the
extractor's constants**, because a second encoding that reads the first one's numbers is
not a second encoding.

**A gate cannot only remove, at the level of a page.** One headline and four observation
slots out of everything that qualified means removing a candidate frees a place, and
sixteen lines took those places. The claim belongs to the selector, where it is exactly
true: step 1b is a filter, so every gated ranking is a subsequence of the ungated one,
across 260 rankings compared. That version fails if either gate is ever made a criterion,
which is the mistake the gate argues against at length.

### The sixth measurement

| reading | fifth | sixth |
|---|---|---|
| Pulse silence, all personas | 63 percent | **65.7 percent** |
| per persona | 37 to 97, none in band | **37 to 97, none in band** |
| every family the corpus declares | 71 of 78 | **69 of 78** |
| variant repeats inside ninety days | 7,418 | **7,370** |
| layer 5 vetoes | 0, 85 absences on purpose | **0, 38 absences on purpose** |
| pattern slots | 399 of 419, 13 families | **397 of 419, 12 families** |

2,067 silent days of 3,424 opened, split **1,161 filtered, 895 nothing qualified, 11 too
little data**. The twelfth persona is kept out of every aggregate so the six recorded
columns stay comparable.

**Which half is the instrument, measured rather than argued.** A control ran the same
year with the gates disabled entirely, and **its silence table is identical to the sixth
measurement in every cell**. So the two point rise is the presence fix and the gates cost
no Pulse sentence at all. That was predictable from the tables, because no Pulse family
is in either gate, and a prediction is not evidence. What the gates did cost is visible
elsewhere: `quietWeek` goes dark on the headline, and absences named on purpose fall from
101 to 38.

### The reading, and it is the same reading as last time

**A bench deep enough to empty the filtered column entirely leaves silence at 28.7
percent against a ceiling of 25**, against 29.0 at the fifth measurement: the same
finding to within a rounding, through a repaired instrument. At that floor five of eleven
personas are in band, five above, and one below.

**Authoring cannot reach the band. Bench depth is necessary and provably not
sufficient**, and phase 9 is still authoring to fix silence rather than repeats. Per the
owner's standing instruction this is reported and not ground at: an app that ships at 30
percent silence is better than one that does not ship.

### What is deliberately not in this pass

- **No corpus line was written.** The estimate family and the rhythm family are declared
  with rules and no language, held in a register that is the mirror of the one holding
  rules that await facts. Phase 9 writes them, and the register carries the five step
  handoff
- **The re-entry screen itself**, which is phase 12c and issue #56

---

## The accessibility pass delivered

Issue #51, Addendum 01 item 8f, and `design-v3.md` 13. Two halves: an adjustable text
size control, and turning contrast from something judged into something computed.

**8f is a spending decision as much as a feature.** Adjustable size and real contrast are
the two things with strong evidence behind them, they help every condition Addendum 01
was written for, and they help people with none of them. **No dyslexia friendly typeface
was added**, per 8f, and the two bundled families stay Newsreader and Hanken Grotesk.

### The size control multiplies the phone's setting, and stops at 2.0 combined

Five steps, Android's own font size ladder, applied to `LocalDensity` in exactly one
place. `ContemplativeTheme` deliberately does not apply it, because it is always nested
inside `ClarityTheme` and a second application would square the factor. A test pins that.

**Multiplying rather than overriding**, because overriding fails the person the feature
exists for on their first screen: somebody at OS 200 percent has already said they cannot
read 100 percent, and an overriding app opens at 100 percent silently. The default step is
exactly 1.0, so a person who never opens the row gets their phone's figure untouched.

**The 2.0 cap is a measurement rather than a round number.** Every clipping analysis in
this project is written against the 200 percent condition: section 13 states it, 5.3 caps
the timer numeral at 1.3x of it, and the tab bar's own note measures the floating pill at
roughly 290dp of the 314dp available. A control able to exceed 2.0 would invalidate all
three at once on a device nobody has run. The cost is stated rather than hidden: a phone
already at 200 percent has no headroom, so the steps above default change nothing, they
stay tappable because 10.16 forbids the disabled control question, and one line says the
phone's own setting is what is deciding.

**The control is a list and not a slider**, which is the section 15 choice. A slider is
continuous for a quantity that is discrete on this platform and a ladder in 5.3; a preview
paragraph is a specimen in a fixed box, which is the one thing that cannot show the half
that matters, that spacing moved. Settings is already real content, so the screen is the
preview: sideheads, row titles, captions, a paragraph, switches and a card all re-lay out
on the tap.

**And every option label is set at the current size, never at its own.** The tempting
version shows the ladder at a glance, and it renders the two smallest rows below the size
a person has already told the app they cannot read, which makes the affordance for a size
control fail the need the size control serves.

### Spacing scales with the text, across 261 call sites

A size control that grows type inside fixed boxes makes an interface less legible rather
than more, which is the risk issue #51 names. Three rules govern it. **Never below 1.0**,
because section 13 calls section 6's numbers minimums, which means every dimension is
identical to before this change on a default phone and is what made a sweep this wide
safe. **Linear above 1.0**, so the rhythm is preserved rather than tightened. **No
horizontal counterpart**, because width is the axis a phone runs out of and a margin that
grew would take room from the measure it serves.

Four things deliberately do not scale, each with the reason on the token: the 48dp touch
minimum, which is a fingertip; the 7dp area dot, which 3.4 makes identity; the sheet
handle, which is a grip; and the tab bar's height and inset.

**Four genuine clipping sites were found and fixed**, all fixed `height()` boxes around
unbounded `bodyStrong` labels that would have cut a wrapped second line with nothing to
see it happen: the button, the support block, the focus pill and the tab item.

**The timer numeral's cap was quietly wrong and is now right.** It capped the phone's
font scale and knew nothing about the app's, so it would have let 2.0 times the largest
app step through. It also constructed a plain `Density`, which discards Android 14's non
linear scaling curve at exactly the largest size in the app; it now preserves it. That is
the same reason the theme wraps `LocalDensity` rather than replacing it, and without it
5.3's 40sp Report headline would render at 80sp where the platform intended about 64.

### Contrast is now a computed number over 10,107 pairs

Phase 3 shipped a 4.40:1 failure on a screen that looked completely correct, and it was
found by computing the number rather than by looking. The audit enumerates rather than
samples: four worlds, all 48 area colors, every wash depth 3.1, 3.2, 3.4 and 12.1 permit
at every whole percent, calm mode on and off.

**It is total by construction, in two ways that matter more than the count.** Tokens come
from reflection over the declared fields of the theme objects, so adding a color to the
theme fails a test until somebody says what it is for; that mechanism immediately turned
up five tokens nobody had classified. And grounds come from the range the design states
rather than from the value the app currently ships, so a later nudge inside a range the
design already permits cannot introduce a failure the audit has not already seen.

**It found 236 pairs under floor in 33 distinct shapes, and all 236 are fixed.** They are
recorded here because the sizes matter and because this is what the app looked like right
up until the audit existed, without anything looking wrong. The Swap swipe label drew the
raw area accent as 10.5sp type and measured **1.03:1** in the dark world, failing on 28 of
48 colors and on 43 of 48 in light. `actionBlue` could not carry text in the light world
at any rank, at five call sites including the undo snackbar action. `positiveGreen` failed
as both label and completion check. The dark FAB put white on blue at 2.63.

### What the fix turned out to be, four times over

**`actionBlue` was one color carrying two jobs it could not both hold.** As a fill it has
to be dark enough for a label; as text it has to be dark enough for the ladder. In the
light world both wants point the same way, so one value serves both and it moved to
`#004BAE`. In the dark world no value does either job: text needs a lightness above one
threshold and white needs it below a lower one. So **the word white leaves the design and
a filled action inverts its label to `card`**, which is the inversion 10.8 already used
for the destructive button and the selected chip. The binding constraint on the light
value turned out to be the Swap face, because a token has to be legible on its own tint.

**`positiveGreen` split into a fill and an ink.** The fill has to stay a light mint,
because section 11 calls the Trail's ground a mint and 10.7 puts a label on a 13 percent
button, and the foreground has to be dark. One value cannot do both, and that was checked
rather than assumed. `positiveInk` is new, it retires a hardcoded green, and
`positiveGreen` now carries no foreground anywhere, which a source gate proves rather than
a floor.

**The Swap face was the wrong shape, and the evidence is a measurement.** 3.4's remedy for
this exact problem elsewhere is to derive a readable variant of the area accent. Applied
to this ground it would move **44 of 48 colors, a median of 34 percent toward black and
five of them past half**. A color blended past half no longer identifies anything, so the
mechanism does not belong here. The face takes the token its own ground is tinted from,
matching Complete and Delete, and identity stays where 3.4 puts it: on the card being
swiped, 8dp away.

**The wash contradiction was closed by narrowing rather than by re-solving.** 3.1 and 3.2
permitted a wash one point deeper than 3.4 solves the area label against, which was a
trapdoor under a token that looked safe to nudge rather than a live defect. Narrowing the
stated range moves no shipped pixel; re-solving would have moved every area label in the
app. The two functions that computed the two ranges are now one, so the trapdoor is
structurally closed rather than numerically avoided, and the test asserts that one point
deeper still fails, so the narrowing stays load bearing.

**And it found two things a floor could not have.** `inkTertiary` and Contemplative's
`textFaint` clear no floor this design states, and 3.1 already says `inkTertiary` carries
no text anywhere in this app. A contrast test cannot notice a screen that starts drawing
one, which is exactly how they spread: the phase 3 test proved that no **Trail** text took
`inkTertiary`, and proved only that. A source scan found **30 `inkTertiary` foregrounds in
11 files and 15 `textFaint` foregrounds in 12**, with four inactive control uses excused by
name under WCAG 1.4.3 and matched on the conditional rather than a line number, so the
exemption survives the file moving.

**The WCAG large text exemption was declined**, which is the section 15 choice on this
one. Taking 3:1 for large text is the common answer and it would put the 21sp item title,
the 40sp display hero and the 64sp timer numeral below the audit's reach for free. It is
refused for a reason specific to this app rather than for taste: this pass adds an in app
size control on top of the OS font scale, so every size moves by a factor the audit cannot
know, and a floor that depends on a size is a floor that changes when somebody drags a
slider. One floor, 4.5, for all text, and 3.0 for a shape rather than for a large word.

### The tightest margins, which are what a future token change breaks first

**Text.** 4.535, 4.538 and 4.546, all the area label on its own in session card. Margins
of thirty five to forty six thousandths. That variant solves to just above floor by
construction, so any move to the light `card` token, to the wash depth, or to the 48 area
colors lands here first.

**Graphic.** 3.133, the onboarding back chevron, with the Report's 50 percent gold mark
behind it at 3.162. The Pulse quiet day mark used to hold this place at 3.003, three
thousandths of margin, under a source comment saying it measured 3.0 to one against
`deepBlack`. That comment was correct and the mark still failed, because the dawn ground
is lighter than `deepBlack` and the same mark measured 2.969 there. **That one number is
the whole thesis of issue #51**: a contrast figure is only true against the ground it was
measured on, and an app has more grounds than anybody enumerates by hand. It is now 55
percent rather than 50, and measures 3.277 on the worst of its three grounds.

---

## Phase 12b delivered

The surfaces half of the polish pass, issue #54. `design-v3.md` 6.1, 10.19, 11, 14 and 15.

Phase 3c did the foundations, deliberately scoped to tokens and type because those are
inherited free by screens that did not exist yet. **Everything it deferred needed more
than two screens on the table to judge**, including all three Contemplative worlds, which
is what the deferral was waiting for. Those exist now, so this is the pass that could
finally be run.

- **The scroll edge fade**, `ui/components/ScrollEdge.kt`, on all four tab screens and on
  every pushed screen. Before it, content passed hard edged under the clock and behind the
  floating pill, and a grep for `verticalGradient`, `fadingEdge`, `blur(` or `overscroll`
  across `ui/` returned nothing at all
- **The Trail's event circle removed**, the glyph grown 14dp to 18dp inside the same 23dp
  slot so no text moves
- **A rule for the tab bar's labels**, with a measurement behind it instead of an
  inherited default
- **What a text field looks like**, `design-v3.md` 10.19, which section 10 never said
- **Section 14's motion sentence narrowed** rather than satisfied
- **`design-v3.md` 3.1 and 3.2 corrected**, which was four phases overdue and unasked

### The fade erases rather than paints, and that is the whole design

The obvious implementation is a ground colored rectangle faded to transparent. It is
wrong on three surfaces here: the Report's ground is two centers of gold light, the
Pulse's shifts with the time of day, and Focus is a radial gradient, so a flat scrim
would cut a dark band across a gradient at exactly the edge the treatment exists to
soften. Instead the content goes into an offscreen layer and the fade is drawn with
`BlendMode.DstOut`, removing the content's own alpha and revealing what is genuinely
behind it. That also removes the ground color parameter from every call site.

**The blur is refused, and the refusal is a test rather than a sentence.** 15.1 lists
"glassmorphism used as decoration rather than to solve a layering problem", and content
passing under a floating bar is a real layering problem that deserves a real answer; the
fade is the honest one. `ScrollEdgeTest` now fails the build on `.blur(`, `BlurEffect`,
`RenderEffect` or `BlurMaskFilter` anywhere in `ui/`.

### The sheet shadow is refused with evidence, and it costs nothing

`ClarityElevation.sheet` is declared in 6.1 and has zero call sites, which read as an
oversight. It is not reachable through theming, and the phase proved that rather than
asserting it: `ModalBottomSheet` in the shipped `material3-android` takes no shadow
parameter of any kind, and the caller's `modifier` attaches to a node that stays at the
top of the window while the content is placed at an offset inside it, so a shadow drawn
there would land at the top of the screen.

**And 6.1 says stop at the first device that reads.** A sheet sits on a 42 percent scrim,
which is a roughly 42 point lightness step under a surface at L\* 98.6. It reads at
device two, so the shadow was always the third device on an element that already had one.
The token is kept with the analysis attached in case a later Material release grows a
hook.

### The Trail's circle: a container that could hold no information

It was tinted by area, so on a one area app every circle was the same disc and the icon
column carried nothing the sentence did not. Section 11 said "the event color" and
defined it nowhere, which made it an open choice under 15.

**A per event palette is unavailable by construction**, and that is a proof rather than a
preference: 3.1 scopes `positiveGreen`, `warnAmber` and `deleteMuted` to one job each, and
3.4 permits an area accent in four forms of which the only one a 23dp shape may take is a
5 to 14 percent wash. Eight moods at 12 percent over the canvas are not distinguishable at
a glance, so the disc could not carry identity either.

So the container goes. **It also resolves a contradiction**: a glyph in a tonal circle
beside one sentence is a stock Material list row, and section 14 forbids those on primary
screens. Area identity stays where 3.4 puts it, on the 7dp dot on the filter chips, and
the mint completion wash is now the only colored surface in the list rather than one
colored thing in a column of faint discs.

### The tab labels: a measurement, not a default

The bar is the only element in the app with a width it cannot grow out of. Icons do not
scale with `fontScale` and labels do, and a 360dp phone leaves the bar 314dp. At 200
percent, one label plus four icons is roughly 290dp of that and four labels roughly 570dp.
Section 13 requires 200 percent without clipping, so labels-always either clips a
destination name, which is the failure the rule would exist to prevent, or grows the one
piece of chrome on every screen and takes four screens' content padding with it.

**So the shipped behavior stays and stops being an inherited default.** 10.15's "no hidden
navigation" is a different claim and is satisfied: all four are always on screen, in fixed
order, and each announces its name to TalkBack. The new rule is a constraint on section 7:
**a destination whose glyph cannot be recognized on its own does not get an unlabeled
state**, and `arrow_outward` fails it, which is recorded in the mapping table in 7.2.

**A defect fell out of it.** The tab item was `.height(46.dp)`, under section 13's 48dp
minimum, and is now `ClaritySpacing.minTouchTarget`.

### The text field is a well, and two hairlines left the app

10.19 is new. The obvious answers are what shipped, an underlined field with a colored
focus rule, or Material's outlined and filled fields. Walking 6.1 in the order 6.1 gives
reaches neither: whitespace genuinely fails, because an empty field with no target is not
a target, and device two does not fail. So the field steps **down** a rank to `raise` on a
`card` sheet, and the hairline goes, because 6.1 puts a hairline fourth and only if all
three above have genuinely failed. Those were the third and fourth hairlines in the whole
app.

Focus takes the well one rank further down to `canvas`, which is the same device speaking
louder rather than a second device arriving, and the caret is the second signal. Two
pre-existing misses were fixed on the way: a placeholder at `inkTertiary` measures 2.40:1
against section 13's floor, and the field was 28dp tall against a 48dp minimum.

### Nothing moves at rest, and section 14 is narrowed rather than satisfied

Section 14 ended "nothing that is still. An app that never moves is an app that feels
broken", and today nothing moves at rest, so that sentence was false. **Every conventional
fix is on the tell list or beside it**, and 15.3 refuses the pulse, breathe, glow loop and
the ambient shimmer by name. None of them says anything about the person's data either.

**But the sentence was not simply false. Four things already move at rest and all four are
time**: the focus arc and numeral, an in session card's countdown, the focus glow's
breath, and the Pulse's ground shifting through the day. Section 14 now says that, and
adds the amendment: **a screen with no time on it does not invent motion to prove it is
alive.** A recorded absence is better than a tell.

### The correction nobody asked for, and it had been wrong for four phases

`design-v3.md` 3.1 and 3.2 are the authority for every color in the app. Phase 3c moved
six tokens in the code and recorded the reasoning in `ClarityColors.kt`, in its commit
message and in two tests, **and never updated the two tables in the document that is the
authority for them.** The file that wins on every visual question said `card` was
`#FFFFFF` while the app drew `#FCFBF9`, and `canvas`, `raise`, dark `card`, dark `raise`
and `inkSecondary` were all wrong too. Every phase after 3c read a table that lied.

### What is deliberately not in this phase

- **The Pulse room takes no fade.** It is a bounded sheet with its own padding rather than
  a surface passing under the status bar, and its ground is the time of day gradient
- **10.6's filing chooser is untouched.** It was outside this pass and its implementation
  is in a file another agent was editing, and writing a specification rule for work in
  somebody else's file is how document and code drift starts
- **15.1 was not re-swept and deliberately not re-dated.** A tell list is a record of what
  the industry currently produces and cannot be re-derived from inside this repository.
  Every entry of 15.1 and 15.3 was checked one at a time against everything this phase
  built, and nothing built appears on the list or is refused by it, which is a narrower
  claim than a sweep and is the one that can honestly be made. `HANDOFF.md` carries it

---

## Phase 12 delivered

Widgets, the launcher surfaces and the permission question, issue #11.
`MASTER_BUILD_PROMPT.md` 13.3 and 13.5, `design-v3.md` section 12, issues #33 to #41.

**Built by three slices working in one tree at the same time**, which is why this section
is written by one of them rather than by each: the widget slices owned code and this one
owned the record. Everything below about the widgets is a claim about source that was read
rather than written here, and it is marked where it matters.

- **Six widgets**, in `widget/`, on `androidx.glance`. `Next Up`, `First Step`,
  `All Areas`, `Quick Capture`, `Focus Countdown` and `Rhythm`. Three of them name an
  optional configuration screen, `WidgetConfigurationActivity`, so a widget draws on the
  automatic area the moment it is placed and is pinned to one afterward if the person
  wants that
- **One snapshot they all read**, `data/widget/ClarityWidgetSnapshot.kt`, one JSON
  document in one DataStore file, written by a collector `ClarityApp` installs that
  follows the projection and rewrites only when the content changed, plus
  `WidgetRefreshWorker` every six hours
- **Three static app shortcuts**, `res/xml/shortcuts.xml` and one `meta-data` line on the
  launcher activity: `Quick capture`, `Start focus`, `Today's Pulse`
- **One quick settings tile**, `tile/FocusTileService.kt`, which starts or ends a focus
  session from the shade and reflects the running session live
- **Two contract tests**, `ShortcutContractTest` and `FocusTileContractTest`, which read
  the resource and the manifest, because neither a shortcut nor a tile can be reached from
  a unit test and both fail silently when they are declared wrong
- **The permission question, answered**, in the open questions above and in section 18

### The rule this phase was most likely to break

**Widgets never read a corpus and never run the engine**, 13.3 and 11.1. The boundary is
the snapshot type and it is enforced by what that type is allowed to contain: facts, and
finished sentences the engine rendered before they were stored. No key, no family, no
stage, no fact reference, so there is nothing on the far side that could be realized even
if somebody tried. The accepted plan line the deferred `One Thing` widget will need is
carried whole rather than as the four keys it was built from, for exactly this reason.

The tile is outside that rule and deliberately so. It shows no sentence at all: two fixed
labels, `Start focus` and `End focus`, which are interface labels in `strings_launcher.xml`
like every other control in the app.

### Seven decisions where the obvious answer was rejected

1. **`Start focus` opens the chooser rather than starting a session**, on the shortcut and
   on the tile, even when exactly one area has an active item and the choice would be
   unambiguous. Issue #40 leaves this to the builder. A session is a row in a log that only
   ever gains rows, the person has not seen a screen yet, and the `First Step` widget had
   already answered the same question the same way, so all three launcher surfaces now say
   one thing instead of two
2. **The tile keeps one icon in both states.** `timer`, with the label and the tile's own
   active state carrying the change. Swapping the icon too would be a second separation
   device on one element, and the third signal is the one that eventually disagrees with
   the other two
3. **The tile carries no subtitle and no item title.** The shade is readable over a lock
   screen. What somebody is working on is not for whoever is holding the phone
4. **Both halves of the tile run behind `unlockAndRun`.** Ending could be done without it,
   since it opens nothing, but two halves of one tile with two different rules about a
   locked phone is a tile nobody can predict, and ending writes to the log
5. **The shortcuts' short label and long label are the same three names.** A launcher shows
   the long one where it fits, so a more descriptive long label would have been the string
   most people actually saw, quietly replacing the three names the design chose
6. **The shortcut icons carry their own disc.** A launcher does not tint a shortcut icon
   and draws it on a popup that follows the phone's theme, so a bare ink glyph is invisible
   in one of the two themes. The disc is the app icon's own #141A2E with the mark's #F3F1EC
   on it
7. **The package name inside each shortcut intent is a generated string resource.**
   `${applicationId}` reaches the manifest and not a resource file, and the debug variant
   carries `.debug`, so a literal would have been three dead shortcuts on every build a
   device check runs on. `app/build.gradle.kts` generates `clarity_application_id` per
   build type from the same constant that sets `applicationId`

### What is deliberately not in this phase

- **The two optional widgets**, `This Week` and `One Thing`. 13.3 makes them optional and
  the snapshot carries a slot for each, so building them later is a composer change and a
  widget rather than a change to the shape the other six read
- **Preview images.** `design-v3.md` 12.1 requires one generated from the real widget and
  never from a mockup, and a hand written preview layout is that mockup with a different
  file extension. The six ship with none, the picker shows the loading layout, and each
  resource file says so at the line the attribute would sit on. It needs a device
- **Dynamic and pinned shortcuts**, and `reportShortcutUsed` with them. 13.5 rules out a
  list that reorders itself around what somebody did most, because that is a measurement of
  the person. `ShortcutContractTest` asserts no file in `src/main/java` calls any of the
  four APIs that would publish one
- **A confirm on the tile's `End`.** The same reasoning `FocusActionReceiver` gives for the
  Live Update's `End` action: a confirm on this surface could only be a second notification
  or a screen, and a labeled control in the shade is already a deliberate tap
- **`SETTING_CHANGED` for the tile**, because nothing on the tile is a setting

### The one thing that stopped this phase closing, and it is now built

**`MainActivity` routed one intent.** The Pulse reminder, four of the six widget taps and
two of the three shortcuts sent actions nothing routed, so they opened the app rather than
a surface. The predicates all existed, in `WidgetIntents` and `PulseIntents`, written as
contracts by the phases that could not reach that Activity.

**A follow up closed all six**, and it was more than the call each in `onCreate` and
`onNewIntent` this section predicted.

`ui/nav/ExternalRequest.kt` holds one value, a serial plus a destination, one sealed
destination with six cases, and one pure table from an action string to a destination.
`MainActivity` notes a request from both entry points and the shell dispatches it in a
`LaunchedEffect`, which fires on first composition too, so a cold start and a warm start
behave identically. That was the risk worth designing for: the Activity is `singleTask`,
so half of these arrive at each entry point, and a route that works from one is the
classic version of this bug.

**Two of the six needed real plumbing rather than a branch.** `AreasRoute` takes a request
and waits on the projection before opening a sheet, because a sheet opened against a log
that has not finished loading dismisses itself. `FocusViewModel.startOnItem` joins the log
load and the session restore before it writes, because without the join it works warm and
is silently refused cold, which is the failure that would have shipped.

**Three decisions worth reading.** The table is keyed on the action string rather than on
the `Intent` predicates: there is no Robolectric here and `isReturnDefaultValues` makes a
built `Intent` answer null, so the predicate form is untestable, while the table names the
same constants and a rename is still a red build. The predicates are kept and documented
as deliberately uncalled. The running session override is not re-implemented in the shell,
because `WidgetIntents.tap` applies it against the snapshot before the `PendingIntent`
exists and a shell copy would need a projection that is empty on a cold start, so it would
answer differently cold than warm. And the serial is compared for difference rather than
for order, because the shell's counter restarts at zero after process death while the
saved mark comes back at three, and a greater-than would swallow the first request of
every new process.

**The test is the part that lasts.** It scans all three intent source files for every
`ACTION_` constant and requires each to be routed or listed as broadcast only. A seventh
action added later fails on the commit that adds it, rather than in a widget picker six
months on, which is how this gap happened the first time.

### What the device check still has to find

**No Gradle task and no `adb` command was run in this phase.** Another workflow was
building in the tree, so the orchestrator runs the build, the unit suite and the device
pass, and everything above is a claim about source.

- Whether the three shortcuts appear on a long press of the icon, on the debug install,
  with the labels unclipped on a Pixel 8, and whether each lands where it should once the
  routing above exists
- Whether the tile appears in the shade's tile picker, and whether adding it, starting a
  session, extending it from the Live Update and ending it from the tile leaves the tile,
  the notification and the app agreeing
- Whether the tile behaves as stated on a locked phone: a passcode prompt first, then the
  tap
- Whether every widget draws in both themes, at the smallest grid size, and whether a
  completion updates them
- `adb logcat` after each, per CLAUDE.md. Two of the three surfaces here can only fail
  silently

---

## Phase 11 delivered

Settings, About and the data controls, issue #10. `MASTER_BUILD_PROMPT.md` 14.1 to 14.5
and 14b.7, `design-v3.md` 10.10, 10.11, 10.15, section 11 and 16.1.

- **The screen**, `ui/settings/`. All seven groups in 14.1's order, rows directly on
  canvas under sentence case sideheads with no card containers, the support block, and
  the version line. The one card on it is the permission card, which 14.1 asks for by
  name. It also landed the four rows that earlier phases wrote preferences for and had no
  screen to put controls on: `Session length` and `Five minute warning` from phase 4,
  `Calm mode` from phase 3b, and the reminder switch and hour from phase 6.
- **The appearance picker**, `ui/settings/AppearancePicker.kt`. Three real miniatures of
  the Areas screen at 84dp, 12dp radius, 9dp gaps, with the System tile clipped at 103
  degrees and drawing both halves including the rows. It reads `ClarityLightColors` and
  `ClarityDarkColors` rather than the four hex values in 10.10, three of which moved in
  phase 3c; a miniature drawn from those numbers would be a picture of a screen this app
  no longer has.
- **The sheets**, `ui/settings/SettingsSheets.kt`. The Pulse explainer, session length,
  the reminder hour, erase, export, the import password prompt, the import choice, the
  privacy policy and the licenses. Each is a bottom sheet, which is a window of its own
  and therefore genuinely above the floating tab bar.
- **The backup path**, `data/export/`. `BackupCodec` reads and writes and cannot touch
  the database; `BackupService.apply` takes a type only a passing validation can produce;
  `BackupCrypto` and `Scrypt` are the optional password; `ImportRefusal` is six reasons
  and no sentences; `ExportReminder` is the quiet 30 day line; `SyncTarget` is section 7's
  seam with its one Storage Access Framework implementation.
- **About**, `ui/about/`. The mark at 62dp on `#141A2E`, the paragraph in `bodySerif`, all
  five `Elsewhere` rows, the support block and the license lines.
- **Five new test classes.** `BackupRoundTripTest` and `BackupRefusalTest` are the two
  14b.7 requires; `ScryptVectorsTest` asserts every intermediate step against RFC 7914;
  `ExportReminderTest` holds both halves of the reminder rule; `EraseContractTest` reads
  the source of both erase methods.

### The privacy text is verbatim and has to stay that way

The heading, the seven leads and the seven bodies in `strings.xml` were checked word for
word against 14.3. **The builder still owes the identical text at a URL under
kamsiob.com**, which the Play Console listing requires and which the app carrying it does
not satisfy. That is phase 13's, and it is the one place in this project where a
paraphrase would be visible to a reviewer rather than only to a reader.

### The backup file, and the four choices inside it

- **Two parts, one header line and then the body**, rather than one nested JSON document.
  A checksum over a nested object is a checksum over somebody's canonical serialization,
  and two writers that disagree about key order produce two checksums for the same data.
  Split, the checksum is over exact bytes, which a second implementation can reproduce
  with no rules to agree about. The unencrypted body is pretty printed JSON starting on
  line 2, so the file still opens in a text editor and reads as what it is.
- **scrypt, written out in the repository against RFC 7914**, rather than a dependency or
  PBKDF2. `MASTER_BUILD_PROMPT.md` 3.3's check on August 27, 2026 put Argon2id first and
  scrypt second on the OWASP list, and Android's providers offer neither. The other half
  of the reason is `docs/EVENT_FORMAT.md`: scrypt is in the standard library of every
  runtime the Linux companion could be written in and Argon2 is in almost none of them.
- **N=2^15, r=8, p=3, OWASP's third equivalent row rather than its first.** The first row
  wants 128 MiB in a single allocation, and a backup that cannot be opened on the phone
  holding the only copy of a person's history is worse than one of five settings the same
  reference calls equivalent. Every parameter travels in the file, so a later build can
  raise them without making one earlier backup unreadable.
- **A recorded projection that disagrees with this build's fold is reported, not
  refused.** The log is the truth and the projection is a cache, so a later build whose
  reducer improved would count differently from the build that wrote the file. Refusing
  somebody their own history over a derived number would be the failure the whole feature
  exists to prevent.

### Ten decisions where the obvious answer was rejected

`design-v3.md` 15, and each is recorded in the file that makes it.

1. **The settings badge glyph is `inkSecondary`, not the group color.** The common
   settings screen of 2026 puts a saturated glyph on a tinted square of the same hue,
   which turns a column of rows into a column of badges on a screen whose whole content
   is words.
2. **The per group colors come from the mood groups in 3.4, never from the four function
   colors.** A settings badge in `actionBlue` is the second meaning that scoping exists to
   prevent.
3. **`After completing` reuses `ClarityChip`'s ink fill rather than a sliding thumb.** A
   thumb is a movement 8.2 has no token for, and it is a second way of saying chosen.
4. **The erase confirmation is case insensitive.** Every field in this app sets sentence
   capitalization, so an exact match would make this the one control that cannot be
   operated without finding caps lock. The gate is that five specific letters were typed
   on purpose, and they were either way.
5. **The export password is visible while typed.** A shoulder surfer is not the threat
   model for a backup of your own task list; a mistyped password is, because the file it
   makes cannot be opened by anyone including its maker, and they find out on the day they
   need it.
6. **The reminder hour is a list of 24 hours, not a clock dial.** The preference stores an
   hour and the reminder is WorkManager work rather than an exact alarm, so a dial would
   promise a precision the scheduler is explicit about not having.
7. **The appearance tiles read live tokens rather than 10.10's hex values.**
8. **The import picker accepts every type rather than the backup type.** A file written by
   this app and carried through a cloud drive or a chat app very often comes back as
   `application/octet-stream`, and a filter that hid a person's own backup from them would
   look correct and lose their data. Nothing is trusted from the type either way.
9. **Import refusals collapse to six reasons rather than twelve.** Every distinction that
   does not change what a person can do about it collapses: a truncation, a flipped bit
   and an edited checksum are all `DAMAGED`, because the answer to all three is find
   another copy.
10. **The export reminder anchors on the oldest surviving item when nothing was ever
    exported.** Most people never export anything, so a rule measured only from the last
    export would be silent forever for exactly the people it exists for.

### What is deliberately not in this phase

- **The archived areas view, issue #15**, which this phase's issue said it closes. It did
  not land. The glyph is in the header and `AreasRoute` hands it an empty lambda.
- **`SETTING_CHANGED` on the `After completing` change.** The event type, the reducer fold
  and the Trail row all exist. What is missing is a method on the only writer in the app,
  shaped like `recordSettingChanged(key, previousValue, newValue)` beside
  `recordAppOpened`. `ClarityRepository.commit` is private and the repository was outside
  this phase's file list, so the event is absent rather than written down a second path.
- **The medical disclaimer in About**, phase 13, per 14.4.
- **The debug menu action** that rebuilds the cache from event zero. The export path calls
  the same locked rebuild body; the menu has no caller.

### What the device check still has to find

- **Settings does not cover the floating tab bar.** It is hosted from inside the Areas tab
  because `ClarityShell` draws the bar as a sibling above the tab content and the shell was
  outside this phase's file list. The surface reserves room at the foot instead. Everything
  is reachable and readable; it is visibly not what `design-v3.md` 10.15 describes, and the
  remedy is one branch in the shell beside the Focus surface.
- **The erase returns to onboarding on the next cold start, not immediately**, because the
  first run gate latches its decision for the process. The erase itself is immediate. The
  same is true of `Replay the welcome` and `Replay the tour`.
- Whether the three appearance miniatures read as miniatures of this app's Areas screen at
  84dp, which is the only claim in 10.10 that a ruler cannot settle.
- Whether the export and import flows survive a real Storage Access Framework round trip
  through a provider, which the unit tests deliberately do not touch.
- Whether the support block reads as an offer rather than a request, in both placements.

---

## Phase 10 delivered

First run, issue #9. `MASTER_BUILD_PROMPT.md` 13.1, 13.2 and 14b.11, `design-v3.md`
10.15, section 11 and 8.2 items 18 and 19.

- **Onboarding**, `ui/onboarding/`. Four beats, Contemplative from the first frame to the
  last, one persistent nav overlay across all of them, tap or swipe left to advance and
  swipe right to go back. Beat 1's four demo cards and the promotion inside nine seconds
  with the model landed in five; beat 2's fork and picker; beat 3's write and iris; beat
  4's four moments and the line announcing the Pulse.
- **The tutorial**, `ui/tutorial/`. `TutorialStep` holds the order and the copy,
  `Modifier.tutorialTarget` applies the stable test tag and reports `boundsInRoot`,
  `TutorialTargets` holds the rectangles, `TutorialOverlay` derives everything it draws
  from the rectangle. **There is no branch on which step is drawing anywhere in that
  file**, which is what 13.2 means by one uniform mechanism.
- **The first run gate**, `ui/onboarding/FirstRunGate.kt`. The two flags, in order, read
  once and latched for the process.
- **`ClarityShell` and `AreasScreen` changes.** The shell provides the target registry and
  composes the overlay last, after the tab bar and the Focus surface. The Areas header
  gained the settings glyph beside the archive glyph, both at `inkSecondary`, both on a
  48dp target with a 22dp glyph.

### The one thing that stops this phase closing

**Four of the tutorial's five targets do not wear `tutorialTarget`.** Only the tab bar
does. `TutorialTargets.ready` requires all five, so the tutorial never starts and
`hasSeenTutorial` is never written.

**It fails safe rather than silently, and that is the design.** A missing target means the
overlay waits; it does not point at the wrong place and it does not mark itself seen. The
same rule covers the real case it was written for, an install with no areas at all, where
there is no card to point at. So the fix is four one line additions in `ui/areas/`, at the
FAB, the first area card, the Focus chip and the Pulse chip, each exactly
`.tutorialTarget(TutorialStep.FAB)` and its three counterparts. **The phase 10 device
check cannot pass before they land.**

### Beat 3 writes the flag, and that is required rather than early

`design-v3.md` 10.15: a person who force quits after beat 3 and relaunches lands on a
populated Areas screen rather than starting over. The flag is what decides that on the
next cold start, and the areas are already real events by then, so onboarding is genuinely
complete at that moment. Beat 4 is depth, and depth is not setup.

**The gate latches its answer for the process for the same reason.** A route that
recomputed itself from the flows would swap onboarding out for the app in the middle of
the reveal, because beat 3 writes the flag while onboarding is still showing. The cost is
that `Replay the welcome`, `Replay the tour` and the return to onboarding after an erase
all take effect on the **next cold start**, which is the trade this phase took knowingly.

**The app is composed underneath onboarding from the start of beat 3, not a few frames
before the iris.** Composing it late lets the staggered card entrance play into the
opening hole, which is prettier, and it also means the iris opening onto a screen still
loading the log on any launch where that takes longer than the head start. A half drawn
screen at the emotional peak of the flow is the worse outcome. Beat 3 gives it the whole
of the closing line's hold, about 1.8 seconds.

### Eight decisions where the obvious answer was rejected

`design-v3.md` 15, and each is recorded in the file that makes it.

1. **Beat 2 opens as a fork rather than as a form**, two stacked panels of identical width,
   surface and type, one composable called twice with no role parameter. The area picker is
   the second screen of one path rather than the beat with an escape hatch attached.
2. **`Just start` sits on top.** The obvious order is the fuller path first, because it is
   the original and the addendum is the addition. Vertical position is the only weight left
   once the treatment is identical, and the tie goes to the path that costs nothing.
3. **Beat 4's last moment holds rather than timing out.** A line announcing a behavior,
   replaced on a timer, is the unannounced behavior 14b.11 objects to, one level up.
4. **The tutorial starts on readiness rather than after a delay.** A delay guesses, and it
   guesses differently on a cold start, on a slow device and on a phone with a long area
   list. Readiness is the fact the delay stands in for.
5. **The mood rows are a single row of six rather than 10.9's three by two grid.** Inside a
   beat that also carries chips, a field and a list of selections, a second block of rows
   pushes Continue off the bottom of a small phone.
6. **Onboarding's controls are written in `ui/onboarding/` rather than reused from
   `ui/components/`**, because every control in that package resolves Daylight colors and a
   Daylight button on `deepBlack` looks plausible and is the wrong world.
7. **The backdrop glow does not breathe.** 8.2 item 8 puts a breathing glow on the Focus
   surface and nowhere else; a second one here is an ambient animation on a screen a person
   reads once.
8. **Beat 1's demo colors are the real first four from the mood walk in 3.4.** A set of
   colors invented for a demo would be a promise the app breaks on the next screen.

### What is deliberately not in this phase

- **`design-v3.md` 10.15's third cold start check**, the re-entry route at fourteen days.
  11.2 is still unowned. The gate leaves the place for it marked, after both branches,
  never inside them.
- **A paywall beat and a closing sheet**, both forbidden by 13.1 and both absent.

### What the device check still has to find

- Whether beat 1 really lands the model in five seconds, which is the only number in that
  beat 13.1 calls non-negotiable and the only one a stopwatch can settle.
- Whether the iris opens onto a settled Areas screen rather than a half drawn one, on a
  cold start with a real log behind it.
- **The force quit after beat 3.** `adb shell am force-stop` between the write and the end
  of beat 4, then relaunch, and the app must land on a populated Areas screen.
- Whether the tutorial spotlights align on the smallest and largest screens, which cannot
  be checked until the four modifiers land.
- Whether back is genuinely hidden on beat 1 and returns to the previous beat everywhere
  else, and whether back inside the tutorial skips the whole thing.

---

## Phase 8 delivered

Snapshots and the Report, issue #6. `MASTER_BUILD_PROMPT.md` 6.4, 11.3 and 12.3,
`design-v3.md` 11.1, `CLARITY_LOGIC_ENGINE.md` 9, and `CORPUS_2_REPORT.md`.

**The integrity tests were written first, which is what issue #6 asked for and is the
reason the phase is defensible.** 12.3 calls data integrity the prime directive and says
why it does not degrade gracefully: one fabricated area name or one off by one number
permanently destroys the credibility of everything else the app says, and the person has no
way to verify anything afterwards. The screen came after the layer that can refuse it.

- **The report scope validator**, `domain/engine/validate/ReportIntegrity.kt`. Nine checks
  over a whole assembled report rather than over one sentence, ordered so that a report
  breaking several is reported against the most fundamental thing wrong with it.
  It exists because two failure classes are invisible to a per sentence validator: two true
  sentences that contradict each other, and one fact rendering two different numbers, where
  each number re-reads correctly against the fact it was given and the report is still
  wrong. **The whole report is vetoed rather than the offending line**, because two numbers
  disagreeing means the fact was computed twice and nothing on the page can say which
  computation was the good one.
- **9.2's consistency map is held on the finished report**, not thrown away with the
  composer, because the screen prints numbers of its own in the caption beneath the ribbon
  and there is no second path to a displayed number. The caption reads the map rather than
  counting, so when a corpus line above has already stated the same fact the caption repeats
  that number instead of minting a second one.
- **The composer**, `domain/report/ReportComposer.kt`. 11.3 steps 3 to 6 with the numbers as
  comments, plus the four rules that only exist at the scale of a page: reading order, the
  area mention cap, the parallel clause cap and 12.3's intent gate. **Nothing is padded and
  nothing is backfilled.** It asks for four observations and a report where two were dropped
  is a report of two; `ClarityReport.dropped` records every one, because a report of two is
  otherwise indistinguishable from a quiet week.
- **The four benches that are not families**, `domain/report/ReportLanguage.kt`. The
  generated line, the basis line and the two edge states, resolved out of
  `CORPUS_2_REPORT.md` 5 and 6 through `VariantChoice`, `SlotRenderer` and
  `ClarityValidator`, which are the same three functions the engine loop uses. Nothing here
  composes, concatenates or writes a word.
- **The window and the cadence**, `domain/report/ReportSchedule.kt`. Days are calendar days
  built from `LocalDate` and never 86,400,000 milliseconds, so the week the clocks change is
  167 hours long and the report still covers seven days. Every function takes its zone as a
  parameter and there is no overload without one.
- **Checkpoints**, `domain/replay/ClarityCheckpointCodec.kt`, `ClarityReplay.canResume` and
  `data/repo/ClarityRepository.kt`. A week snapshot doubles as a replay checkpoint, cold
  start replays only the tail, and `ingestForeignLog` throws every checkpoint away and folds
  from event zero in both modes. **The resume rule is a count**, not a comparison of the two
  ends of the log, and the reason is in the next section.
- **The screen**, `ui/report/`. All nine items of `design-v3.md` 11.1, the gold editorial
  ground with two fixed lights and no specks, the week ribbon with its caption, the pattern
  break as the one grid break, the History page, regenerate, copy, and the reveal inside its
  1.4 second ceiling.
- **Eight new test classes and three shared fixtures.** `ReportIntegrityVetoTest` has a test per
  check; `ReportCompositionTest` drives the assembly with candidates built by hand;
  `ReportPropertyTest` runs ten thousand generated weeks; `ReportPersonaTest` runs eleven
  personas through a simulated year and writes each report back into the log so the next
  week's cooldowns and exclusions are real; `CheckpointResumeTest` is the phase 1 harness
  line that had never had a checkpoint to resume from.

### The one defect the checkpoint work found, and it was latent rather than new

**`canResume` compared the two ends of the log and passed on a merged one.** The old rule
asked whether the newest event sorted at or after the checkpoint's position and whether the
position was still in the log. A checkpoint is the fold of a **prefix** of the total order,
so an import that inserted three events *before* the position satisfied both halves, and
`replayFrom` then dropped all three as already folded in when they never were.

**Nothing looks wrong afterwards. The numbers are just smaller, forever.** There is no
screen that shows it, no exception, and no later check that finds it. The fix is to count:
the log holds exactly as many events at or before the position as the checkpoint state was
folded from. `CheckpointResumeTest` has the case, named
`resuming a merged log over a checkpoint would silently drop events`.

It was latent because nothing had ever written a checkpoint. The read path existed from
phase 1 and `newestCheckpoint` always answered null, so the wrong rule had never been asked
a question it could get wrong. **Both merge paths clear every checkpoint anyway**, so
forgetting the count would cost a slow cold start rather than a wrong one, and the rule is
stated once, over two numbers, so the SQL caller and the list caller cannot drift.

### What the composer refuses, and it refuses in both directions

- **A third observation naming one area is dropped**, and the one dropped is the lower
  ranked one, which is the direction the incompatibility matrix already resolves in
- **A third editorial lead is dropped.** The backstop is unreachable through `compose`,
  because the engine spends the budget while it realizes, and it is there anyway: editorial
  voice on an ordinary fact is the clearest tell of generated writing
- **A callback observation is dropped below three answered pulses.** `completionSplit`
  already carried the floor; `selfReportVsData` did not, and its rule requires one stored
  answer ever, which is the right condition for the quote to be real and the wrong one for
  the claim to be representative
- **A third consecutive parallel numeric lead is dropped**, where a parallel numeric lead
  is one rendering two or more numbers. Counting any lead with a number in it would have
  silently shortened almost every report with nothing failing
- **Nothing is reached for to replace what was dropped.** The obvious implementation asks
  the engine for eight and keeps the first four that survive, which is padding with extra
  steps

### Where the length band rule stands, and it is not closed

`CLARITY_LOGIC_ENGINE.md` 7.5 forbids two consecutive leads from the same band. The composer
applies it a second time, over the reading order, because grouping the observations under
their sideheads can put two leads of one band together after the realizer had spread them.
**It is applied as a preference and not as a veto**: where every remaining line in a section
shares the band, the highest ranked one is taken anyway.

That is deliberate and it is recorded in `DECISIONS.md` rather than assumed. 11.4 forbids
padding a section to reach a minimum, and dropping a true observation to improve the cadence
is the same trade in the other direction. **It also means the phase 5 gate is not lifted by
this phase.** The 715 collisions across 451 reports measured in phase 5 are a bench depth
reading and phase 9 is what moves them. `ReportInvariants` deliberately does not assert the
band rule, so the ten thousand week run and the persona year say nothing about it either
way.

### Fifteen decisions where the obvious answer was rejected

All are in `DECISIONS.md` with the losing option named. In short: the window is the seven
completed days before today rather than today so far; the cadence is asked of the log about
the calendar week rather than keyed on the window; the caption states events, completions
and additions rather than completions, focus minutes and a percentage; there are three
sideheads with a mapping made at composition rather than one section holding everything; a
quiet day keeps a mark at a floor and the scale is linear rather than curved; the pattern
break changes the optical size and leaves the point size alone; the reveal's stagger stops
growing after five blocks so the 1.4 second ceiling holds; there are no specks on this
surface and the two lights are fixed to the room; exactly one checkpoint row survives rather
than one per week; every checkpoint is a full rebuild from event zero checked against the
running state; resuming is decided by a count rather than by the log's two ends; a parallel
numeric clause is a lead rendering two numbers rather than any lead with a number;
observations are grouped by section with the band rule re-applied as a preference; the
regenerate wait is a shimmer rather than the spinner 12.3 asks for, because `design-v3.md`
8.2 item 22 owns the look; and a report the integrity layer refused is its own state rather
than the empty state.

### What is deliberately not in this phase

- **`REPORT_GENERATED` is never written.** 11.3 step 9 belongs to `ClarityRepository`, which
  is the only writer in the app and has no method for it. Three things follow and **none is
  a design decision**: `isDue` asks the log the right question and therefore always answers
  yes, so the report composes on every open; `FiringHistory` never learns what the Report
  said, so the ninety day variant exclusion and the fourteen day family cooldowns cannot
  vary it week to week; and the History page is empty, because it reads the projection and
  the projection is fed by the log. The report is deterministic either way, so a person sees
  the same page rather than a changing one.
- **The closing line is always absent**, and the composable that draws it is built. A
  closing line is layer six and layer six is phase 9b. `ReportPage.Composed` is where it
  arrives, `accept` settles the pill and writes nothing because `PLAN_OFFERED` is phase 9b's,
  and **declining records nothing at all**, deliberately: there is no `PLAN_DECLINED` event
  and no surface anywhere that could count one.
- **A past report has no headline on the page.** `ReportGenerated` carries `headlineKey` and
  `headlineVariantKey` and not the rendered text, while `renderedSections` carries the text
  of every observation. Re-realizing the variant would be a second path to a sentence.
  Recorded in `DECISIONS.md` as an open question, because adding the field is a change to
  the committed event format in `docs/EVENT_FORMAT.md`.
- **The debug menu action and the export path** that 6.4 asks to call
  `rebuildCacheFromLog`. The method is built and answers what it found; both callers are
  phase 11.
- **`ReportCoordinator` lives in `ui.report` and belongs in `domain.report`**, beside
  `PulseCoordinator` and `MomentumCoordinator`. Nothing in it depends on Android. It is a
  package line and an import, and `domain/report/` was outside this slice's file list.
- **The third catalog in the process.** 11.7 wants one built and held; there are now three,
  one per coordinator, for the reason phase 6 recorded about the second. The fix is one lazy
  binding in `ClarityGraph` and a constructor parameter on each, at which point
  `MomentumGraph`, `ReportGraph` and `AssetCorpus` all go away together.
- **The three Addendum 01 items build order 19 gave this phase.** They are in the open
  questions above and on issue #6.

### What the device check still has to find

**It has not run.** The Pixel was in use by another session and no `adb` command was issued.
What the phone can prove that a unit test cannot:

- **Whether the gold night reads as a room with no specks in it**, which is the one place
  this surface departs from every other Contemplative screen in the app.
- **Whether the pattern break reads as a break at `opsz` 28 and 17sp.** That is the number
  `design-v3.md` 11.1 left reading two ways and it is worth an owner's glance.
- **The ribbon draw**, which 8.2 calls the most satisfying single animation after the
  promotion, and the whole reveal against its 1.4 second ceiling on a real frame budget.
- **Whether the quiet day's mark reads as quiet rather than as absent** at the 3.0 to one
  floor, at arm's length, at low brightness. The same question the Pulse rhythm row asked.
- **TalkBack over the page**: eyebrow, headline, the spoken ribbon summary, then sections,
  with the controls reached last despite being drawn over the top.
- **Copy**, which is the app's only integration surface with anything else, pasted into
  something else and read.
- **Font scale 200 percent**, where the headline is `displayHero` and the pattern band has a
  measure 20dp narrower than the prose above it.
- **That the background gradient really does reach the very top edge** under the status bar,
  which is 11.1's last line and is the kind of thing an emulator flatters.

---

## Phase 7 delivered

Momentum and the Areas banner, issue #5. `MASTER_BUILD_PROMPT.md` 11.2, 11.3 and 12.2,
`design-v3.md` 8.2, 10.2 and 11, `CLARITY_LOGIC_ENGINE.md` 6.5, and `CORPUS_3_MOMENTUM.md`.

**Momentum observes and never interprets, and that rule governs the screen as well as the
corpus.** Nothing on it says why. The dot row does not explain the gaps, the tiles do not
rank the areas, the stats carry no comparison against last week, and the insight modules
state a shape and stop. One sentence comes from the engine and every other thing on the
screen is a number the log was asked for through a `TrailQueries` function, drawn as a dot,
a tile, a figure or a mark, with its label out of `strings.xml`.

- **The composer**, `domain/momentum/MomentumComposer.kt`. Pure Kotlin, no Android, no
  database. It decides the fourteen days, the tiles, the three figures and the four insight
  modules, and every one of them is a query result rather than a computation. Testable
  against the real corpus with a fake clock.
- **The plumbing**, `domain/momentum/MomentumCoordinator.kt`. The clock, the log and the
  corpus text. **It writes nothing and there is nothing for it to write**: the event catalog
  has no `MOMENTUM_GENERATED`, so unlike the Pulse there is no append, no immutability rule
  and no per day key, which is what makes the banner throttle a rate limit on work rather
  than a correctness rule.
- **The banner**, `ui/momentum/AreasBanner.kt`, `BannerThrottle.kt` and
  `domain/momentum/BannerCaptions.kt`. `design-v3.md` 10.2, and the one element on the Areas
  screen whose sentence comes from the engine. It has been recorded here as deliberately
  absent since phase 2 for exactly that reason, and this is its arrival. **The throttle is
  in the ViewModel and not in the engine**, per 11.2, as a value type with no Android in it
  so the boundary can be walked in a test.
- **The screen**, `ui/momentum/MomentumScreen.kt` and `MomentumInsights.kt`. The headline in
  `readSerif`, the fourteen dot row with today ringed, the three column tile mosaic, three
  figures Monday to now as pure typography, and the four insight modules under sideheads,
  each absent when it has no data.
- **The two animations `design-v3.md` 8.2 gives this screen**, items 13 and 14, in
  `MomentumMotion.kt`. The dot cascade at a 35ms stagger with the today ring drawing last,
  and the number roll counting up from zero over 600ms. Both are entrances, so 8.4 governs
  them: once per app session, and never again on a return to the tab.
- **Four new test classes.** `MomentumComposerTest` walks every number and every floor,
  `MomentumLanguageTest` reads the real corpus, `BannerCaptionsTest` holds the caption bench,
  and `BannerThrottleTest` walks the hour.

### The risk issue #5 named first, and why it cannot happen here

**An accidental streak.** The guard is structural rather than a rule somebody has to
remember. `FactSet` declares no streak fact, `MomentumWindows` hands the composer fourteen
independent calendar days, and there is no field on the way out that could answer whether
two active days were adjacent. `MomentumComposerTest` states it twice, as
`the count is the size of a set and a missed day resets nothing` and as
`opening the app is not activity`, which is the phase 3b decision that keeps `APP_OPENED`
out of `isUserActivity` and stops a fortnight of opening and closing drawing a full row.

**On the screen the same rule is kept the same way.** An inactive day is drawn smaller and
lighter, it has no content description of its own, and the row is one node to a screen
reader that names itself and tallies nothing. `design-v3.md` 14 says a gap is rendered as a
lighter dot with nothing said about it anywhere, and a spoken count of the gaps would be
saying something about them.

**The second risk was the banner recomputing on every recomposition.** It has its own
ViewModel resolved against the Activity's store, it does not collect the projection, and the
hour is measured in app use rather than reset by a tab switch. Calling it more often is free:
it is a lock, a subtraction and a return.

### Every module has a floor under it, and none of them is a taste

11.4 forbids padding a section to reach a minimum, and a module drawn from too little is not
a smaller version of itself, it is a shape a person can read something into that is not
there.

- **Area balance needs two areas with something in them.** One area holding a hundred percent
  of a fortnight is a balance in the arithmetic sense and says nothing anybody could act on,
  and `AreaFacts.shareOfEvents` calls itself the most misused fact in the system for this
  reason
- **The pace sparkline needs three points, two of them carrying something.** Two is a
  comparison and one is a dot, and a line drawn across a single spike is a trend nobody has
- **The focus strip is absent until there is focus in it**
- **Idle areas appear at seven days and not a day sooner**

### The half of 14b.10 this phase could not carry

**No empty chart, anywhere.** That half is built: every module is absent rather than drawn
empty, and a figure whose feature has never been used is dimmed with a discovery line rather
than hidden.

**The other half is a corpus line and the bench does not carry one.** 14b.10 asks every
reflective surface to state plainly what it needs and roughly when it becomes useful, in the
shape of `Patterns show up after about three weeks.`, and says in as many words that those
sentences are about the person's own data and are corpus lines rather than `strings.xml`
copy. `CORPUS_3_MOMENTUM.md` has no such bench. **Phase 7 wrote none**, because phase 9 grows
the corpus and every batch goes to the owner. The discovery lines that do exist are in
`strings.xml` and are correct there: every one of them describes how a feature works and none
says anything about the person.

### Nine decisions where the obvious answer was rejected

All are in `DECISIONS.md` with the losing option named. In short: the tiles are a three
column grid rather than a scrolling row or four columns; the three figures are completions,
minutes focused and items added rather than the lifecycle order; an unused feature is a
lifetime question rather than a weekly one; the activity readout sits under the dots rather
than over them; the figures are set in the serif with sans labels on a common left edge
rather than as a centered dashboard row; the area name sits under the tile and the idle
outline is the `hairline` token rather than the area's own color; the idle module's sidehead
reads `Quiet areas` rather than 12.2's own `Idle Areas`, because 12.2's word for what the
label has to be is gentle; the share rows are typography with no bar behind the numbers; and
the banner has a ViewModel of its own rather than a field on `AreasViewModel`.

### What is deliberately not in this phase

- **The first weeks lines from 14b.10**, above. Phase 9.
- **The banner's caption binding table lives in `domain/momentum/BannerCaptions.kt` and
  belongs in `domain/engine/realize/SlotBindings.kt`**, which is where layer 4 authors every
  other slot binding. The caption bench has slots and is not a family, so `bindingsFor` has
  nowhere to look it up and phase 5 left it unbound. The table is in the same shape with the
  corpus line quoted beside every entry, and `BannerCaptionsTest` fails the build if a
  caption line in the real corpus has no binding. **When the table moves, this file goes with
  it and nothing else changes.**
- **The second catalog in the process**, for the reason phase 6 recorded about the first. One
  lazy binding in `ClarityGraph` replaces it.
- **Any change to `AreasViewModel`.** The banner reaches its own ViewModel from inside
  `AreasHeader` rather than taking a value through `AreasUiState`, which is a deviation from
  the shape every other element on that screen follows and is recorded in a comment at the
  call site rather than left quiet.

### What the device check still has to find

**It has not run.** The Pixel was in use by another session and no `adb` command was issued.
What the phone can prove that a unit test cannot:

- **Whether this really is the calmest screen in the Daylight world**, which is the only
  acceptance criterion in issue #5 that no test can express.
- **Whether the serif figures read at a glance.** They are the deliberate choice over a sans
  dashboard row and they are the one that could be wrong on a real screen.
- **The dot cascade and the number roll**, and specifically that the today ring draws last,
  which is the half of 8.2 item 13 that is easy to lose.
- **That the entrances fire once per app session**, verified by switching tabs twenty times
  and seeing them twice, once for Momentum and once for whichever other tab is entered.
- **Whether a 60 percent tile reads as the person's own color** against the name beneath it,
  at both themes and in calm mode, where the tile desaturates and the identity dot does not.
- **The Areas banner appearing at all**, and then not reappearing for an hour, and the header
  keeping its height on a week no family describes.
- **TalkBack over the dot row and the tiles**, where the row is one node and each tile names
  its area and its state rather than its color.
- **Font scale 200 percent** on the three figure block, which is three columns of type with
  no container to give way.

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
  catalog test fails if a family goes quiet without being on that list. **Closed by the
  facts phase, above**: all twelve facts are declared, all twelve entries are gone, and
  `RulesAwaitingFacts.GAPS` is empty.

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
