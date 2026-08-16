# Clarity Now by Kamsiob: Master Build Specification

Read this document, `design-v3.md` and `CLARITY_LOGIC_ENGINE.md` before writing any code.

**This document is self-contained.** Nothing here refers to an earlier version. Everything from both review panels is folded in.

---

## 0. How to work on this

You are building a complete, shipping Android application for a builder who does not write code and will not read your source to check it. That places the burden of correctness on you.

- Every generated file must be complete and compilable. Never emit `// rest of implementation here` or `// TODO`
- When a change touches multiple files, update every file in the same pass. When you create a file referencing another, create that file too
- Build in the phases in section 16. Each phase ends with its slice of the verification checklist green
- `design-v3.md` wins on anything visual or interactive. This document wins on behavior and data. `CLARITY_LOGIC_ENGINE.md` wins inside `domain.engine` and `domain.guidance`. If a genuine contradiction survives, stop and ask
- **Choose the version number yourself** and announce it at each release, using semantic versioning, with one line of reasoning. Bug fixes bump the third number, backward-compatible features the second, breaking changes the first. The builder does not track version numbers
- **Flag achievability.** If any part of this specification cannot realistically be built this way, say so before starting it rather than producing something that compiles but does not work

### 0.1 The document set

| file | authority over |
|---|---|
| this file | behavior, data, build order, and how to operate the engine |
| `design-v3.md` | everything visual and interactive |
| `CLARITY_LOGIC_ENGINE.md` | all six engine layers, including guidance |
| `CORPUS_1_PULSE.md` | Pulse language |
| `CORPUS_2_REPORT.md` | Report language and the guidance corpus |
| `CORPUS_3_MOMENTUM.md` | Momentum headline and Areas banner language |
| `clarity-now-visual-reference-v3.html` | the mark, swipe, both pickers, About |
| `clarity-now-visual-reference-v2.html` | every other screen |

The two HTML files are static mocks. **Never copy them into the project.** Read them for structure, proportion and color relationships.

**`design-v3.md` states every dimension in dp and is the only source for a number. The mocks are for proportion, structure and color relationships, never for arithmetic.** Their px values are not uniformly scaled: layout dimensions sit at roughly 0.82 of dp, but corner radii, icon sizes and touch targets were drawn at their true dp value. Converting a mock px figure to dp will produce wrong results. Read the number from the design document.

Where a mock disagrees with `design-v3.md` on anything at all, the design document wins.

`rationale/` holds the review panels and superseded drafts. Not needed to build.

---

## 1. Product Definition

A queue based personal productivity app. Your life has a small number of Areas of Focus, each area has exactly one active item, everything else waits in a queue. Complete the active item and the next is promoted.

Around that sit three reflective features powered by one deterministic engine: **Clarity Pulse**, a daily behavioral observation with a one tap answer; **Momentum**, a calm progress mirror; **Clarity Report**, a weekly narrative. **Trail**, a chronological event log, is the source of truth from which all state and all reflection derives.

Pulse captures data in the moment. Momentum shows what is going on now. The Report aggregates everything to provide insight.

---

## 2. Non-negotiables

Violating any of these is a build failure.

1. **No monetization.** No subscription, IAP, billing library, entitlements, gates, paywall, locked features, premium badges, upgrade prompts or trial. Every feature available to every user immediately. The only money-related element is one support row linking to Buy Me a Coffee
2. **No accounts.** No sign in, user ID, profile, cloud or server
3. **No data collection.** No analytics, telemetry, crash reporting, advertising, attribution or third party SDKs
4. **No `INTERNET` permission.** Load bearing, stated to users as a checkable fact. If a dependency pulls it in transitively, remove the dependency or strip it with `tools:node="remove"` and verify the merged manifest
5. **No AI, model, inference, ML Kit, AICore or Gemini Nano.** All intelligence is deterministic code over real counts
6. **AGPL-3.0.** `LICENSE` at the repo root, license line in About
7. **No em dashes or en dashes in any string.** Not in UI copy, the corpora, comments, the README, or commit messages
8. **No emojis, no non-ASCII outside standard punctuation**
9. **American English throughout.** color, license, behavior, prioritize, ads, organize

**Dependencies:** Google, JetBrains and AndroidX artifacts only. Banned: Firebase, any analytics SDK, any crash reporting SDK, any ads SDK, RevenueCat, Realm, image loading libraries, anything requiring a network permission.

---

## 3. Platform and Stack

- **Kotlin**, latest stable, coroutines and Flow throughout
- **Jetpack Compose** with Material 3 heavily customized. Material You dynamic color explicitly disabled
- **Room** for all app data. **DataStore** for per-device settings and flags only
- **WorkManager** for the Pulse reminder and widget refresh. No other background work
- **Jetpack Glance** for widgets
- **Compose Navigation**, single activity
- **kotlinx.serialization** for the event log, export and import
- **Hand written singleton container** (`ClarityGraph`) for dependency injection. Not Hilt; for a single module app it adds annotation processing and failure modes that are hard to diagnose without reading code
- **Fonts: Newsreader** (variable, optical size axis intact) and **Hanken Grotesk** (variable). Both SIL OFL, both bundled and subset. Verify tabular figure support in Hanken Grotesk at build time
- Min SDK 31, target SDK latest stable
- Package `com.kamsiob.claritynow`
- **App label** `Clarity Now` on release, `Clarity Now debug` on the debug variant, set through a manifest placeholder so both can sit on the launcher at once
- **Repository name** `clarity-now`, public, at github.com/kamsiob

### 3.1 Fonts, and the trap in them

Both faces are on Google Fonts under the SIL OFL. **Download the actual font files and commit them to `res/font/`.**

**Android's Downloadable Fonts API is unusable in this project and must not be reached for.** It fetches over the network through Play Services, and this app has no `INTERNET` permission. It is the default modern path for Google Fonts on Android, so it is exactly what an agent reaches for by reflex, and it will either fail at runtime or pull in a dependency that reintroduces the permission. Neither failure is obvious in a screenshot.

- **Newsreader**: variable, with the `opsz` axis intact. The design system uses one family at two optical sizes, so a static instance is not sufficient
- **Hanken Grotesk**: variable, weights 300 to 800
- Source both from `fonts.google.com` or the `google/fonts` GitHub repository
- Declare them as `res/font` XML font families, referenced from the Compose type system
- Subset if the tooling is available, and commit the `OFL.txt` for each alongside the files
- **Verify tabular figures (`tnum`) in Hanken Grotesk before relying on them.** If absent, use a fixed-width numeral treatment rather than switching families

---

## 4. Project Structure

```
com.kamsiob.claritynow
  data.event      ClarityEvent, payloads, the event DAO, the log writer
  data.model      Room entities for the materialized cache
  data.db         database, DAOs, converters, migrations
  data.repo       repositories, the only thing that writes
  data.prefs      DataStore
  data.export     backup, export, import, the sync file format
  domain.replay   reducer, invariants, conflict resolution, checkpoints
  domain.engine   facts, rules, catalog, selection, realization, validation
  domain.guidance layer six, the plan composer
  domain.query    TrailQueries, the only path from data to a displayed number
  ui.theme        colors, type, shape, motion tokens, haptics
  ui.components   shared composables
  ui.areas | ui.focus | ui.pulse | ui.momentum | ui.report | ui.trail
  ui.settings | ui.about | ui.onboarding | ui.tutorial
  widget | notifications
  devtools        simulator and corpus dump harness, debug only
```

**Rules.** ViewModels never touch DAOs, only repositories. Composables never touch repositories, only ViewModels plus a thin theme and haptics layer. `domain.engine`, `domain.guidance` and `domain.replay` are pure Kotlin with no Android imports. Every displayed number comes from a query. All Compose state is immutable, stable, collected as StateFlow. Lazy lists always use stable keys. One `ClarityClock` injected everywhere time is read.

---

## 5. Data Model: Event Sourced

**The event log is the truth. Everything else is a cache that can be deleted and rebuilt.**

Every state change is an immutable, append-only event. Areas, items, queue positions and completion states are computed by replaying the log. This exists so a Linux desktop companion can later sync by exchanging log files with no rework.

Nothing in v1 performs networking. The format, reducer and merge semantics are built now; the transport is added later as an additive feature.

### 5.1 ClarityEvent

One Room table, `clarity_event`, append only. Rows never updated, never deleted.

| column | type | notes |
|---|---|---|
| `id` | String | UUIDv4 |
| `schemaVersion` | Int | starts at 1, present from the first event ever written |
| `type` | String | see 5.2 |
| `wallClock` | Long | epoch millis, display only, never ordering |
| `lamport` | Long | logical clock, advanced to `max(local, seen) + 1` on merge |
| `originId` | String | a UUID generated once at install, stored in DataStore. Identifies the device, never the person |
| `payload` | String | JSON, one sealed payload class per type |
| `entityId` | String? | the primary entity concerned, indexed |

Indices on `lamport`, `type`, `entityId`, `wallClock`.

**Total ordering is `(lamport, originId)` ascending**, with `originId` as a deterministic tiebreak. **Never order by `wallClock`;** two devices will disagree.

### 5.2 The event catalog

Payloads carry **full before and after values plus display snapshots**, so replay reconstructs state without reading any other table. This is the trap in event sourcing: a log that reads nicely is not the same as a log that replays correctly.

| type | payload |
|---|---|
| `AREA_CREATED` | areaId, name, colorHex, orderKey |
| `AREA_RENAMED` | areaId, previousName, newName |
| `AREA_RECOLORED` | areaId, previousHex, newHex |
| `AREA_REORDERED` | areaId, previousOrderKey, newOrderKey |
| `AREA_ARCHIVED` | areaId, nameSnapshot |
| `AREA_UNARCHIVED` | areaId, nameSnapshot |
| `AREA_DELETED` | areaId, nameSnapshot (tombstone, never a row removal) |
| `ITEM_ADDED` | itemId, areaId, title, note, orderKey, areaNameSnapshot |
| `ITEM_EDITED` | itemId, previousTitle, newTitle, previousNote, newNote |
| `ITEM_QUEUED` | itemId, areaId, orderKey, previousStatus |
| `ITEM_PROMOTED` | itemId, areaId, previousStatus, demotedItemId (nullable), demotedToOrderKey (nullable), titleSnapshot, areaNameSnapshot |
| `ITEM_COMPLETED` | itemId, areaId, titleSnapshot, areaNameSnapshot, activeDurationDays |
| `ITEM_REOPENED` | itemId, areaId, targetOrderKey |
| `ITEM_REORDERED` | itemId, areaId, previousOrderKey, newOrderKey |
| `ITEM_DELETED` | itemId, areaId, titleSnapshot (tombstone) |
| `FOCUS_STARTED` | sessionId, areaId, itemId, plannedSeconds |
| `FOCUS_COMPLETED` | sessionId, actualSeconds |
| `FOCUS_ABANDONED` | sessionId, actualSeconds |
| `PULSE_GENERATED` | pulseId, dateKey, family, escalationStage, register, variantKey, renderedObservation, renderedQuestion, factSnapshot, reflectionPeriod |
| `PULSE_ANSWERED` | pulseId, responseKey, responseLabel, responseIsPositive |
| `REPORT_GENERATED` | reportId, weekStartKey, headlineKey, renderedSections, factSnapshot |
| `PLAN_OFFERED` | planId, weekStartKey, frameKey, cueKey, actionKey, familyKey, subjectId, offeredLine, committedLine, resolutionFactRef |
| `PLAN_ACCEPTED` | planId |
| `SETTING_CHANGED` | key, previousValue, newValue (only for settings affecting behavior history, such as afterCompleting) |

`ITEM_PROMOTED` carrying `demotedItemId` is what makes swap replay correctly, and is exactly the detail a descriptive log would omit.

### 5.3 Order keys

`orderKey` is a **fractional index**, stored as a String using a base 62 lexicographic scheme. **Never an Int.** Inserting between two items produces a key strictly between them without touching any other row. Integer positions break under concurrent reorder and cannot be retrofitted once user data exists.

Provide `OrderKey.between(a: String?, b: String?): String`, `OrderKey.first()`, `OrderKey.last(after: String)`, and a rebalance routine for the rare case where keys grow long.

### 5.4 The materialized cache

Room tables `clarity_area`, `clarity_item`, `clarity_focus_session`, `clarity_pulse_entry`, `clarity_report`, `clarity_week_snapshot`, `clarity_plan`, `clarity_conflict`. All derived. Each carries `deletedAt: Long?` and `lastEventLamport: Long`.

They exist purely for query speed. Any can be dropped and rebuilt from the log with no data loss, and a debug menu action does exactly that as a proof.

**DataStore holds only per-device values, never synced:** `theme`, `focusDurationMinutes` (default 25), `focusHighlightEnabled` (default true), `afterCompleting` (default AUTO_PROMOTE), `pulseRemindersEnabled` (default true), `pulseReminderHour` (default 20), `hasCompletedOnboarding`, `hasSeenTutorial`, `originId`, `lamportCounter`, `lastExportAt`.

**Nothing the Logic Engine reads may live in DataStore.** Variation history, escalation state, personal records, first-ever flags and plan history all derive from the log so two devices compute the same answer. This is a hard rule and it will not fail loudly if you get it wrong.

### 5.5 Write path

Exactly one path exists:

1. The repository builds the event, assigns `lamport` and `originId`
2. Inside a single Room transaction: append the event, then apply the reducer's effect to the cache
3. Emit updated Flows

No UI, ViewModel, or engine ever writes. There is no second way to change state.

---

## 6. Replay, Invariants and Conflict Resolution

### 6.1 The reducer

`ClarityReducer.apply(state: ClarityState, event: ClarityEvent): ClarityState`

A pure function. No Android imports, no clock, no randomness. Given the same ordered event list it must produce byte identical state every time, on any device, in any process.

### 6.2 Invariants enforced during replay

- At most one `ACTIVE` item per non-deleted, non-archived area
- A `COMPLETED` item is never `ACTIVE`
- Every queued item in an area has a distinct `orderKey`
- A deleted or archived entity never appears in a live projection
- An event referencing an unknown entity is skipped, logged to a replay diagnostics list, and never crashes the app

### 6.3 Conflict resolution

Implemented and tested now, because it is what the reducer does at a divergence point.

**Two ACTIVE items in one area.** The event with the higher `(lamport, originId)` wins. The loser goes to the head of the queue with a fresh `orderKey`. A `ClarityConflict` record is written with both ids and the resolution.

**Edit versus delete.** Delete wins. The edit stays in the log but has no effect.

**Concurrent reorder.** Fractional keys mean both survive with a deterministic order.

**Duplicate date-keyed rows.** Higher `(lamport, originId)` wins; the loser drops from projection but stays in the log.

**Surfacing.** When a replay produces conflicts, the Areas screen shows one dismissible card in the app's voice:

> While you were away, two things became active in Work. Rewrite the proposal intro is active. Call the printer is back at the top of the queue.

Never silent. Never a technical dialog. Never data loss.

### 6.4 Checkpoints

`ClarityWeekSnapshot` doubles as a replay checkpoint: a serialized `ClarityState` plus the `lamport` it was taken at, written when a week closes. Cold start loads the newest checkpoint and replays only events after it. A full rebuild from event zero is available in the debug menu and runs in the export path as a correctness check.

### 6.5 The replay test harness

Build this in phase 1, not at the end. It is what delivers the guarantee that sync plugs in later without rework.

- **Determinism.** Generate a random valid event stream, replay twice, assert identical state
- **Divergence.** From a common ancestor, generate two device streams with overlapping targets, merge by `(lamport, originId)`, replay on both sides, assert byte identical results. Across thousands of scenarios including both promoting in the same area, one deleting what the other edits, clock skew up to 48 hours, out of order arrival, duplicate delivery
- **Idempotency.** Delivering the same event twice must not change state
- **Checkpoint.** State from a checkpoint plus tail replay equals state from a full replay
- **Reset virginity.** After `Erase all data`, a fresh replay produces a virgin state: no personal records, no first-ever flags consumed, no variation history, no plan history, and the `your first week` paths correctly re-armed. **Assert this explicitly;** it is the only promise the Report cannot survive breaking

### 6.6 The golden fixture

Commit `testdata/golden-log.json` (a canonical event stream) and `testdata/golden-state.json` (the exact state it must produce), both plain JSON, both human readable. Also commit `docs/EVENT_FORMAT.md` describing every event type, payload field, the ordering rule, the order key scheme and the conflict rules in prose.

These are the contract between this app and the future Linux desktop app, which will be built in a separate session. Without them the two implementations will drift, and drifting means data loss.

---

## 7. Sync Readiness

Nothing in v1 opens a socket, requests a permission, or shows a sync setting. What v1 must have: the event log format versioned from event one, tombstones everywhere, `originId` and `lamport` on every event, fractional order keys, a `data.export` package with a `SyncTarget` interface declared but exactly one implementation (`LocalFileTarget`, used only by manual export and import), and the full harness above.

When sync ships later, the work is: implement a second `SyncTarget`, add a settings screen, add credential storage, add the permission. No changes to the data model, the reducer, or any screen.

---

## 8. Core Mechanics

### 8.1 Areas

**Create.** Name required, 1 to 40 characters trimmed. Color defaulted by walking mood groups so the first four areas are distinct. Appends at the end. Writes `AREA_CREATED`.

There is no limit on the number of areas. The philosophy is carried by copy and layout, not a cap. Onboarding says `Pick two to four` and the Areas screen breathes at four to five cards.

**Edit.** Rename and recolor through the two stage color picker. Reorder by long press and drag.

**Archive, not delete.** Archiving hides the area from Areas, Momentum tiles and widgets. History remains and the Report stops mentioning it. An Archived view from the header icon allows unarchiving or permanent deletion with a typed confirmation, writing `AREA_DELETED` as a tombstone. Trail events survive because they carry snapshots.

### 8.2 Items and the queue

**Add.** From the FAB or an area detail sheet. Title required, optional note, target area. If the area has no active item it becomes active immediately (`ITEM_ADDED` then `ITEM_PROMOTED`). Otherwise it appends to the queue. The add sheet states where the item will land before the user commits.

**Complete.** From swipe right, area detail, or the focus completion flow. Writes `ITEM_COMPLETED`. Then per `afterCompleting`: `AUTO_PROMOTE` takes the queue head and promotes it with the hero animation, or `CHOOSE_FROM_QUEUE` opens a chooser; dismissing leaves the area idle.

**Only the active item can be completed.** Completing from the queue is not allowed, and this is the rule doing the philosophical work.

**Swap.** Sends the active item to the head of the queue and promotes a chosen item, as one `ITEM_PROMOTED` carrying `demotedItemId`. The chooser names the item being demoted so nothing disappears silently. **Swaps are normal behavior. No warning tone anywhere, in copy or color.**

**Queue management.** Drag to reorder. Edit titles and notes. Delete with a 5 second undo before the event commits. Completed items live in a collapsed section, newest first, and can be reopened to the queue head.

### 8.3 Swipe gestures

Per `design-v3.md` 10.3.1. Summarized because it is easy to get wrong.

| gesture | behavior |
|---|---|
| swipe right past 25 percent | reveals **Complete**, positiveGreen at 18 percent, 66dp action width |
| swipe right past 55 percent | commits Complete. One haptic tick at the threshold |
| swipe left past 25 percent | reveals **Swap** then **Delete**, side by side, 66dp each |
| swipe left past 55 percent | commits **Swap** only |
| tap Delete on the revealed row | commits delete, with the 5 second undo snackbar |

**Delete is never reachable by a full swipe.** Destructive actions must not be committed by momentum.

**State gating.** An idle area offers neither Complete nor Swap; a left swipe reveals Delete only, and that deletes the **area**, with a typed confirmation rather than an undo. An area with an empty queue offers Complete but not Swap. During an active focus session both Complete and Swap are available; completing ends the session naturally.

**Edge cases, all required and all specified in `design-v3.md` 10.3.1.** One row open at a time, with opening a second closing the first. A tap while a row is open closes it and is consumed rather than passing through. Swipe disabled during a drag reorder and while any sheet is open. A fling above 1,200dp per second commits below the distance threshold. On delete the row slides off then collapses its height, and undo expands it back. A gesture whose initial direction is predominantly vertical is a scroll and never becomes a swipe.

**Accessibility, mandatory.** All three actions must also be reachable from a long press context menu on the card **and** from the area detail sheet. Swipe is invisible to TalkBack and is an accelerator, never the only path.

### 8.4 Navigation and back

Implement `design-v3.md` 10.15 exactly. The points most likely to be missed:

- **Predictive back on every destination.** Declare a handler so the system preview shows the real destination. Its absence is visible on Android 14 and later
- **Back during a focus session navigates away and leaves the session running.** It does not end it, prompt, or warn. Ending is a deliberate button press, never a side effect of navigating
- **Back on a non-root tab returns to Areas. Back on Areas exits.** No double-tap-to-exit prompt
- **Back in the tutorial skips it entirely**, identically to Skip
- **Zero areas is a real state.** Archiving or deleting everything is permitted, and while it holds **the FAB creates an area rather than an item**. All four tabs show their own empty states
- **A queued item is tappable** and opens an edit sheet with title, note, Delete, and Move to front. Without it the queue is read-only, which no user expects
- **First launch reads `hasCompletedOnboarding`, then `hasSeenTutorial`,** in that order. Onboarding beat 3 writes real events, so a force-quit after beat 3 lands on a populated Areas screen rather than restarting the flow

---

## 9. Trail

Every meaningful action writes its events at the moment it happens, inside the same transaction as the cache change, through the repository, never from the UI layer.

All engine reads go through `TrailQueries`, a pure facade with functions such as `completionsBetween(start, end)`, `eventsPerArea(window)`, `activeDays(window)`, `focusMinutes(window)`, `queueSizeAt(instant)`, `daysActiveForItem(itemId)`, `lastEventForArea(areaId)`. **Every Report and Pulse claim traces to one of these. There is no other path to a displayed number.**

**Retention: forever.** Events are never auto deleted. Full history back to install date is what makes callbacks possible.

**The Trail screen.** Filter chip row (All, then one chip per non-archived area with its color dot, horizontally scrollable). Events grouped by day, newest first, day header with an event count. Within a day, newest first, timestamps on the first event of each 10 minute cluster. Pagination loads 14 days per page and never queries the whole table.

---

## 10. Focus Sessions

**Flow.** The Focus chip opens a chooser listing areas with an active item. Areas without one are dimmed and non-selectable with the hint `Add an item first`. Selecting starts a session at the settings duration (default 25 minutes; options 5, 10, 15, 20, 25, 30, 45, 60).

**The session screen** is the indigo Contemplative surface. Screen stays on during a session.

**Mechanics.**
- Starting writes `FOCUS_STARTED`. The computed end timestamp persists so the session survives process death; on relaunch during a running session the focus screen is restored
- An ongoing notification, low importance and silent, shows the item title and a countdown chronometer. Tapping it reopens the session
- Natural completion: soft tone and the `focusEnd` haptic, `FOCUS_COMPLETED` written, then the completion state with `Mark item complete` and `Done`
- If backgrounded at completion, post a gentle notification and resolve on next resume or from the notification
- Early end: under 60 seconds discards silently as `FOCUS_ABANDONED`. Past 60 seconds, a small confirm reading `End this session?` with `End` and `Keep going`
- **Abandonment is treated neutrally everywhere.** Pulse and Report language never blames
- While running, the Areas card for that area shows the intensified wash and live countdown when `focusHighlightEnabled` is on. **There is no bar**

---

## 11. HOW TO USE THE LOGIC ENGINE AND THE CORPORA

**This section exists because the engine and the corpora are the easiest part of this project to misuse, and misuse is not detectable by looking at the screen.**

### 11.1 The one rule everything follows from

**No sentence reaches a screen except by passing through the engine layers in order, and every sentence displayed comes from a corpus file.**

There is no second path. Not for empty states, not for errors, not for edge cases, not for "just this one string." If a screen needs a sentence about the user's data, it asks the engine.

### 11.2 What each corpus is for, and when it is read

| corpus | read by | when | produces |
|---|---|---|---|
| `CORPUS_1_PULSE.md` | `PulseEngine` | once on the first app foreground of each local calendar day | one observation, one question, one response set, or nothing |
| `CORPUS_2_REPORT.md` 1, 2, 3 | `ReportEngine` | first open of the Report tab in a new week, and on manual regenerate | one headline, 2 to 4 observations, at most one pattern |
| `CORPUS_2_REPORT.md` 4 | `GuidanceComposer`, layer 6 | after the report body exists, once | one plan, one non-plan closing, or nothing |
| `CORPUS_2_REPORT.md` 5, 6 | `ReportEngine` | same | footer and edge states |
| `CORPUS_3_MOMENTUM.md` | `MomentumEngine` | on Momentum entry; banner at most once per hour of app use | one headline, or one banner sentence plus one caption |

**Nothing else reads a corpus. Ever.** Widgets read the widget snapshot, which contains sentences the engine already produced. Notifications use fixed strings from `strings.xml`. The tutorial and onboarding use fixed strings.

### 11.3 The invocation sequence, exactly

**Pulse, once per calendar day on first foreground:**

```
1. Compute dateKey from ClarityClock with an explicit zone
2. If a ClarityPulseEntry exists for dateKey, stop. Display it. It is immutable
3. reflectionPeriod: before 17:00 use yesterday, at or after 17:00 use today so far
4. FactExtractor over that window -> FactSet
5. FiringHistory rebuilt from PULSE_GENERATED and PULSE_ANSWERED events. Never DataStore
6. ClarityEngine.observe(facts, history, Purpose.PULSE)
7. If Silent, write nothing. The day is IDLE. The chip shows no dot
8. If Spoke, write PULSE_GENERATED with family, stage, register, variantKey,
   rendered strings and the fact snapshot
```

**The Report, on first open in a new week or on regenerate:**

```
1. Window is the trailing 7 days ending today
2. FactExtractor -> FactSet, including CueFacts over 12 weeks
3. Select the headline FIRST. It constrains everything after it
4. Select 2 to 4 observations, applying the incompatibility matrix against the
   headline and against each other, plus the length band and parallel clause rules
5. Select at most one pattern, only if weeksOfData >= 3
6. Realize and validate each. Vetoed candidates fall through to the next ranked selection
7. Pass ONLY the observations that ACTUALLY APPEARED into GuidanceComposer
8. GuidanceComposer returns a plan, a non-plan closing, or nothing
9. Write REPORT_GENERATED, and PLAN_OFFERED if a plan was produced
```

**Momentum, on screen entry:** extract, select one `MOMENTUM_HEADLINE`, realize, validate. The banner is the same with `AREAS_BANNER` and a one hour throttle held in the ViewModel, not the engine.

### 11.4 What you must never do

These are the failures that will not be visible in a screenshot.

- **Never write a sentence in a composable.** If a screen shows a sentence about user data that is not in a corpus file, the implementation is wrong even if the sentence is good
- **Never build a sentence by string concatenation at runtime.** Templates with typed slots only. Fragments belong to one family and one register and may not be assembled across families
- **Never interpolate a live entity name.** Names come from snapshot fields on the `FactSet`. The realizer must not have access to live entity tables at all
- **Never bypass the validator.** Not for a simple sentence, not for an empty state, not to fix a bug. If the validator vetoes something it should not, the rule is wrong, not the validator
- **Never let a number reach a template without a `FactRef`.** The validator re-reads that fact and compares
- **Never read engine state from DataStore.** Variation history, escalation stages, personal records, first-ever flags, plan history
- **Never use `String.hashCode()`** for variant selection. Use the specified FNV-1a 64 bit
- **Never pad a section to reach a minimum.** One qualifying observation means one observation. No pattern trend means the section is omitted entirely
- **Never make the engine speak every day.** Pulse silence between 8 and 25 percent of days. Layer 6 silence at least 15 percent of reports
- **Never let layer 6 inject a sentence about a plan.** Follow-through is a priority boost on a family that must qualify independently
- **Never show a plan as an imperative.** Nominal when offered, first person when accepted
- **Never offer a plan without an explicit decline.** One button is not a choice
- **Never generate corpus lines in bulk.** Batches of forty, one family and stage at a time, anchored with ten approved lines, presented for approval, judged against simulator output

### 11.5 Before a single corpus line

The simulator in `devtools` exists and runs. Synthetic personas, a full simulated year, every Pulse and Report dumped to plain text annotated with the rule that fired, the stage, the register, the variant key and the facts used. Without it you are authoring blind.

### 11.6 The Pulse response format, settled

**Two options, always, except `quietDay` which needs three.** Do not add a universal third.

A third path already exists: **not answering**. Dismissing the sheet is a fully supported state with its own representation, the hollow amber ring in the ambient rhythm row. Never chased, never counted against the user, never mentioned.

`Neither` would produce a response with no signal, which is worse than no response, because it enters the aggregation and dilutes it.

---

## 12. Pulse, Momentum, Report

### 12.1 Pulse

Once per day, one behavioral observation, one question, two or three answers. Data capture, not advice.

**States** IDLE, READY, PRESENTED, ANSWERED. At most one per calendar day, keyed by `dateKey`. Generated per 11.3, immutable once written.

**The sheet.** The observation in readSerif centered, the question in body at textDim, then response pills. After answering, a neutral acknowledgment fades in, then ambient mode: a 14 day rhythm row, today's answered card, and a History entry. Filled amber means answered, a hollow ring means generated but unanswered, faint means a silent day.

**Reminders.** When enabled, WorkManager schedules a daily notification at the chosen hour, **posted only if that day's entry exists and is unanswered.** Never posted when IDLE.

### 12.2 Momentum

The calm daily mirror. **It observes and never interprets.** It must never say because, suggests, or means; that vocabulary belongs to the Report.

1. **Headline** in readSerif, from the engine, under twelve words
2. **Activity row:** `Active X of last 14 days` with the 14 dot grid, today ringed. A rolling window by design. **There is no streak, and a missed day never resets anything**
3. **Area tiles:** one per non-archived area, area color at 60 percent when it has an active item, faint outline when idle
4. **This Week:** three typographic stats, Monday to now, no cards. Unused features render dimmed with a soft discovery line
5. **Insights**, each only when it has data: Area Balance, Completion Pace (8 week sparkline), Focus Patterns (7 day heat strip), Idle Areas (only at 7 or more days inactive, gentle, no red)

**Empty state:** welcoming sentence, empty dots, outlined tiles, dimmed stats. No guilt.

### 12.3 Report

**Prime directive: data integrity.** Every claim traces to a specific query with a non-zero result.

**Hard integrity rules, built as code and not conventions.** Before any area is named it must have at least one event in the window. Before any number is stated it must come from an actual count query. New areas with no activity, archived areas and deleted areas are never mentioned. The validation layer vetoes any sentence failing these, and **the veto path must be reachable in unit tests.**

**Window and cadence.** Trailing 7 days ending today, recalculated on every generation. Generated automatically on first open in a new week (Sunday start), regenerable at any time. Past weeks remain forever.

**Structure** per `design-v3.md` 11.1: controls, eyebrow, headline, the week ribbon, gold rule, sections under sentence-case sideheads, the pattern section as the single grid break, the closing line with accept and decline, footer.

**Edge cases.** A brand new user gets `Your first week` with whatever is honest. All areas empty and no activity shows the styled empty state with no generated observations. Intent-qualified insights require 3 or more answered pulses in the window; below that the report is trail data only.

**Controls.** History (past reports by week), regenerate (spinner on the headline block, near instant), copy (plain text to the clipboard). The copy control is the app's only integration surface with anything else.

---

## 13. Onboarding, Tutorial, Widgets, Notifications

### 13.1 Onboarding

Four beats, runs once, replayable from Settings. Entirely Contemplative. A persistent nav overlay: back chevron (hidden on beat 1) at 35 percent white, an 80dp progress line filling by beat, and `Jump in` at 30 percent white, always visible. Tap or swipe left advances, swipe right goes back.

**Beat 1, See It Work.** About 9 seconds, auto advances. Four colored demo cards enter with staggered three-part entrances. The top card's item strikes through and completes; the next queued title slides up and takes its place. One sentence: `One thing at a time. The next one is ready when you are.` **This beat must land the whole model in five seconds.**

**Beat 2, Your Areas.** The user picks two to four starter areas. Suggestion chips (Work, Personal, Health, Family, Learning, Side Project) plus a custom field. Each selection shows a mini card and opens the mood color rows. **Selections are transient in-memory structs; nothing is written until beat 3.**

**Beat 3, The Reveal.** Selected areas are written as real events, then an iris-open transition uncovers the user's actual Areas screen rendered live behind the overlay, with the closing line fading in and out: `Your clarity starts here.`

**Beat 4, The Depth.** About 20 to 25 seconds, four auto-paced moments, tap to advance. Moment 1, philosophy on black: `You just organized your focus.` then `Now let the app learn how you work.` Moment 2, Pulse: amber glow, a sample observation card with two response pills, caption `One question a day. One tap.` Moment 3, Momentum: blue glow, the 14 dot row fills day by day, caption `Your rhythm, without the guilt.` Moment 4, Report: gold glow, a miniature report headline, caption `Every week, an honest mirror. Written on your device.`

Then it opens the app with the tutorial queued. **There is no paywall beat and no sheet at the end.**

### 13.2 Tutorial

Five spotlight steps on first arrival at Areas. A full screen overlay above everything including the tab bar: 56 percent black radial dim, a feathered cutout, a slowly pulsing 2dp white ring at 38 percent, a floating tooltip in surfaceRaised with a step indicator. Skip always visible top right. Tap anywhere advances.

**Implementation:** targets report bounds via `onGloballyPositioned` keyed by stable test tags. **One uniform mechanism for every step, no per-step special cases.** This was a hard-won lesson in the iOS build; do not mix strategies.

**Steps:** 1 the FAB, 2 an area card, 3 the Focus chip, 4 the Pulse chip, 5 the tab bar.

### 13.3 Widgets

Data comes from a widget snapshot written to DataStore on every meaningful change plus a WorkManager refresh every 6 hours. Deep links open the right surface, except while a focus session is running, when any tap goes to the focus screen. If a configured area was deleted or archived, show a reconfigure prompt.

**v1 ships two.** **Next Up** (small): one active item, configurable to a pinned area or automatic. **All Areas** (medium): each area as a row with dot, name, active item or `Idle`.

Deferred, listed so the snapshot format accommodates them: Area Focus, Action Board, Weekly Momentum, Weekly Insight. Glance is finicky and six widgets is a lot of grinding for early payoff.

### 13.4 Notifications

Channels: **Focus** (session complete, default importance, gentle sound), **Reminders** (Pulse reminder), **Ongoing** (silent, the running session chronometer).

Request `POST_NOTIFICATIONS` contextually, the first time the user starts a focus session or enables a reminder. **Never at launch.** Every notification deep links correctly. **No marketing notifications, ever. No re-engagement notifications, ever. Nothing that exists to pull the user back.**

---

## 14. Settings and About

### 14.1 Settings

A Daylight screen, rows on canvas under sentence-case sideheads, **no card containers.**

**Daily routine.** `Daily reflection` explainer row opening a short sheet describing Pulse. `Pulse reminder` toggle. `Remind me at` time picker, shown only when the toggle is on.

**Focus.** `Highlight the active session` toggle. `Session length` selector.

**After completing.** Segmented choice: `Promote next` versus `Choose from queue`, with an explanatory line reflecting the current selection.

**Appearance.** Three real-miniature tiles per `design-v3.md` 10.10. System is the default. A line below noting that Focus, Pulse and Report are always dark by design.

**Your data.** `Export everything`, one JSON file via the Storage Access Framework, showing the last export date. `Import from a file`, validating schema version and integrity before a transactional full replace with a typed confirmation. `Erase all data`, per 14.2.

**Privacy.** `Privacy policy` opening the in-app sheet with the text in 14.3. `Open source licenses` listing AGPL-3.0 for the app, SIL OFL for Newsreader and Hanken Grotesk, Apache 2.0 for Material Symbols and AndroidX. Then the permission card:

> **This app has no internet permission.**
> Not a promise, a fact your phone enforces. Clarity Now cannot open a network connection even if it wanted to. Check it in Android settings.

**Help.** `Replay the tour`. `Replay the welcome`. `About Clarity Now`.

**Then the support block** per 14.5, then the version line: `Clarity Now by Kamsiob · [version]` and `AGPL-3.0 · free and open source`.

### 14.2 Erase all data

A sheet, not a dialog. Serif heading `Erase everything?`, two paragraphs stating exactly what is removed and that it cannot be undone, a nudge to export first, then a field requiring the user to type `ERASE`. The destructive button stays inert grey until the text matches. `Keep my data` sits below it.

On confirmation: wipe the event log, every cache table, every checkpoint, and all DataStore keys except a freshly regenerated `originId`. Return to onboarding. The reset virginity test in 6.5 covers this.

### 14.3 Privacy policy text

Ship this exact text in app. The builder publishes the identical text at a URL under kamsiob.com for the Play Console listing, which requires a hosted policy regardless of how little is collected.

> **Nothing leaves this device.**
>
> Clarity Now collects no personal data, has no analytics, no crash reporting, no advertising, no third party services, no accounts and no servers. The developer cannot see anything you write, because there is nowhere for it to go.
>
> **What is stored, and where.** Your areas, items, history, reflections and reports are written to a database inside this app's private storage on this phone. Android prevents other apps from reading it. It is never uploaded.
>
> **Network access.** This app does not declare the internet permission. Android will refuse any attempt it makes to open a connection. You can verify this yourself in the app info screen.
>
> **Deleting your data.** Erase all data in Settings removes everything permanently. Uninstalling the app does the same. There is no copy anywhere else, and no request to make of anyone.
>
> **Children.** Clarity Now is not directed at children and collects no data from anyone, of any age.
>
> **Changes.** If this policy ever changes, the updated version ships with the app and is dated. Previous versions remain in the public repository history.
>
> **Contact.** hello@kamsiob.com. The full source is public, so every claim on this page can be checked rather than trusted.

**Play Console Data Safety declarations for the builder to enter:** no data collected, no data shared, data is not encrypted in transit because there is no transit, users can delete their data in app, no data types apply. No account, so the account deletion URL requirement does not apply. Contains no ads.

### 14.4 About

App mark at 62dp on `#141A2E`, name in displayTitle, `Version [x] · by Kamsiob`, then one paragraph in bodySerif:

> One active item per area. Everything else waits its turn. No account, no cloud, no subscription, no ads, and nothing collected. Your history lives in one file on this phone until you delete it.

**Then a quiet link list under an `Elsewhere` sidehead.** Each row: an outlined icon at 50 percent opacity, the label, and the destination trailing in caption inkTertiary.

| label | trailing | destination |
|---|---|---|
| YouTube | `@kamsiob` | https://youtube.com/@kamsiob |
| Source code | `github.com/kamsiob` | https://github.com/kamsiob/clarity-now |
| Website | `kamsiob.com` | https://kamsiob.com |
| Kamsiob Lab | `telegram` | https://t.me/+g5LKm9rUnNcxMjk5 |
| Feedback | `hello@kamsiob.com` | mailto:hello@kamsiob.com |

Links are findable but visually subordinate. Then the support block, then license lines.

### 14.5 The support block

Appears at the bottom of **Settings and About only.** A rounded card with a warm parchment gradient and no border.

- Heading `Support this work` with a small outlined heart icon in `#B45309`
- Body: *Built and carried by one person. If software made this way matters to you, there is a place to stand behind it. Either way, it is yours.*
- Button: filled `#B45309`, label exactly `Support this work`, opening https://buymeacoffee.com/kamsiob

**Copy rules, absolute.** No coffee or caffeine references anywhere, in the label or the body. No framing anchoring support to a small amount. No begging, no urgency, no counter, no goal bar, no `if you enjoy`, no exclamation marks. Never a dialog, never an interstitial, never after completing a task, never more than these two placements.

---

## 15. Motion, Haptics, Performance, Accessibility

Implement `design-v3.md` sections 8, 9 and 13 exactly. One `ClarityMotion` object, one `ClarityHaptics` abstraction, one global `LocalReduceMotion`. All 26 animations and all 16 haptic events required; **the queue promotion is the one to get perfect.**

Cold start under 800ms. Baseline Profile and R8 full mode required; macrobenchmark harnesses not required for v1. No frame over 16ms on scroll, promotion, sheets or the focus ring. Under 15MB installed. Contrast 4.5:1 verified per area color in both modes. Font scale to 200 percent. TalkBack pass on every screen, with the Report reading a spoken summary of the week ribbon, and **every swipe action duplicated in a long press menu and the detail sheet.**

---

## 16. Environment, Build, Device and Release

### 16.1 The build machine

The builder runs **Bazzite**, an immutable Fedora Atomic system with KDE Plasma. This matters and is the single most likely cause of a wasted first hour.

**`/usr` is read only.** Any installer that writes to `/usr` or calls `useradd` will fail. **`/etc` and `$HOME` are writable.**

Keep the entire Android toolchain inside `$HOME`, where none of this applies:

| tool | where | note |
|---|---|---|
| JDK 17 or later | bundled JetBrains Runtime from Android Studio, or SDKMAN in `$HOME` | do not attempt a system package |
| Android SDK | `~/Android/Sdk` | writable, no layering needed |
| Gradle | the Gradle wrapper in the repo | never a system Gradle |
| `adb` and `fastboot` | `brew install android-platform-tools` | Bazzite ships Homebrew, which installs to its own prefix and never touches `/usr` |

**Verify the toolchain before writing code**, and if anything is missing say so rather than attempting an `rpm-ostree` layer, which would require a reboot the builder did not ask for.

`local.properties` holds the SDK path and is **machine specific. Never commit it.**

### 16.2 Git identity

Configure **locally in the repository**, not globally, before the first commit:

```
git config user.name "Kamsiob"
git config user.email "306265999+Kamsiob@users.noreply.github.com"
```

### 16.3 What must never be committed

Write `.gitignore` before the first commit, covering at minimum:

```
*.iml
.gradle/
/local.properties
/.idea/
/build/
/app/build/
/captures/
.externalNativeBuild/
.cxx/
*.apk
*.aab
*.keystore
*.jks
keystore.properties
/devtools-output/
```

**Never commit a keystore, a signing password, a `keystore.properties`, or any Play service account JSON.** If a release keystore is ever generated, it lives outside the repository and its location is told to the builder, not stored.

The debug keystore Android generates automatically is fine and is already excluded by the pattern above.

### 16.4 First build

- **Initialize git** if not already done
- **Create the GitHub repository as PUBLIC**, matching the settings, topics and structure pattern of the other repositories at github.com/kamsiob
- **Commit ALL project files**, including every design document, both corpora, the engine specification, the visual references, `docs/EVENT_FORMAT.md` and the golden fixtures. **Not just source code**
- `LICENSE` at the root, AGPL-3.0
- A `README.md` at the root describing what the app is, the privacy stance, the license, and how to build
- `.gitignore` per 16.3

### 16.5 Installing on the phone

**There is no desktop binary for this project.** It is an Android app, so the deliverable is an installed app on the builder's device, not an exported file.

**Target device: a Pixel 10 Pro XL.** Min SDK 31, so it is well within range.

**Prefer wireless debugging over USB.** It avoids udev rules entirely, which is one less thing to fight on an immutable OS.

1. On the phone: Settings, System, Developer options, Wireless debugging, on
2. Tap **Pair device with pairing code** to get an IP, port and code
3. `adb pair <ip>:<pairing-port>` then enter the code
4. `adb connect <ip>:<connect-port>`
5. Confirm with `adb devices`

Pairing persists across reconnections on the same network, so steps 2 and 3 are a one time setup.

If USB is used instead, `android-udev` rules go in `/etc/udev/rules.d`, which **is** writable on Fedora Atomic.

**Give the debug build its own application id** so a debug install can never collide with a Play install later:

```kotlin
buildTypes {
    debug { applicationIdSuffix = ".debug"; versionNameSuffix = "-debug" }
}
```

**Install with `./gradlew installDebug`**, or `adb install -r` on the built APK. Report the outcome plainly: what was installed, which version, and on which device.

### 16.6 Screenshots for the README

Taken from the running app on the real device, **never mockups and never from the HTML references**:

```
adb exec-out screencap -p > docs/screenshots/areas.png
```

Navigate the app, capture each screen as **actual in-app screenshots**, and commit them under `docs/screenshots/`. If the device is unavailable, say so and leave the README screenshot section as a placeholder rather than substituting anything.

### 16.7 Versioning

Two separate numbers, and confusing them is a common and painful mistake.

- **`versionName`** is the semantic version shown to humans, for example `1.2.0`. **Choose it yourself** using semantic versioning and state it with one line of reasoning. Bug fixes bump the third number, backward-compatible features the second, breaking changes the first
- **`versionCode`** is an integer Play requires to increase monotonically forever. It never decreases and is never reused, even across a failed upload. Derive it deterministically, for example `major * 10000 + minor * 100 + patch`, and state the value

### 16.8 Every subsequent update

Closing step on every change, without being asked:

1. **Commit and push all changes to GitHub**
2. **Build and install the updated app on the phone**, replacing the previous install
3. **Report the version name, version code, and that the install succeeded**

There is nothing to export to the desktop. GitHub stays in sync with the source, and the phone stays in sync with the build.

### 16.9 Throughout

- **Keep exactly ONE copy of the project on the machine.** One clone, one build output tree. Do not create parallel copies, dated folders, or `-v2` directories across sessions. Delete stale build outputs rather than accumulating them
- **One installed build on the phone at a time.** The debug suffix in 16.5 permits a Play install alongside it later, but never two debug variants
- Commit messages follow the same rules as everything else: no em dashes, no emojis, American spelling
- Run `./gradlew --stop` if a daemon is left holding memory after a long session

### 16.10 Play Console

Not required for v1 and not part of any build phase. Recorded so it is available when the builder asks.

The app must be created manually in Play Console and one build uploaded through the web UI before the Publisher API can manage it. The IARC content rating questionnaire has no API and must be completed manually. Ads declaration and app access instructions are likely manual-only.

Developer name is **Kamsiob** under the **B7 Collective** organization account. Automation runs through the service account `kamsiob@kamsiob-503213.iam.gserviceaccount.com` in Google Cloud project `kamsiob-503213`, with the Android Publisher API enabled. A release build requires an upload keystore, which must be generated and stored outside the repository per 16.3.

---

## 17. Verification Checklist

**Build and hygiene**
- Zero warnings, treated as errors
- **The merged manifest contains no `INTERNET` permission.** Automated
- No em dash or en dash in any string. Automated
- No British spelling in any user-facing string. Automated
- All strings ASCII plus standard punctuation. Automated
- **No element declares two separation devices.** Automated over the component set
- **No colored stripe, bar or edge treatment exists.** Automated grep for edge-anchored colored views
- No all-caps user-facing string outside the day initials on the week ribbon
- **Anti-slop pass** against the dated list in `design-v3.md` 15.1, with the list reviewed and updated before each release

**Engine and data**
- Reducer determinism, divergence merge, idempotency, checkpoint equivalence, reset virginity
- One active item per area under concurrent completes; promotion order; order key insertion and rebalance
- Golden fixture replays to the exact committed state
- Pulse selection: no-repeat rule, silence case, 17:00 boundary, escalation monotonicity, DST boundaries
- Report integrity vetoes, with a test per validator check constructing a violating candidate
- **Banned vocabulary test matches the evaluative sense of `behind` only**, per `CLARITY_LOGIC_ENGINE.md` 11.3. The spatial sense, a queue behind an item, is correct and appears in thirteen approved lines
- **Every `lengthBand` is computed at catalog load, never read from a corpus tag.** A test asserts the computed band for every lead and that no two consecutive leads in a generated report share one
- **Escalation stage ranges are parsed from the corpus stage headers**, are contiguous and non-overlapping per family, and a compound header becomes two rules rather than a disjunctive range
- **Every family declares a `cooldownDays`** matching the table in `CLARITY_LOGIC_ENGINE.md` 7.3, and no `(family, subjectId)` pair fires inside its window
- **Every rule's `unflattering` flag matches the enumeration** in `CLARITY_LOGIC_ENGINE.md` 7.4, and no neutral or positive family uses the `NEUTRAL_AGENT` register
- Catalog integrity: rules point at existing families, required slots producible, no duplicate keys, **no fragment in two families**, no construction in more than two families, every share-based rule carries an event floor
- Purity: no Android imports, no `System.currentTimeMillis`, no `Random`, no `String.hashCode()` in `domain.engine` or `domain.guidance`
- Composition: no report violates the incompatibility matrix, the length band rule, or the parallel clause cap
- Cue substantiation: no plan renders with a cue below threshold
- **The non-compliance test:** a persona accepting every plan and completing none produces a simulated year in which no sentence references a plan, a commitment, an intention, or a failure to act
- Silence floors: Pulse 8 to 25 percent of days, guidance at least 15 percent of reports
- Simulator: a full year dumps without a crash and without a repeated variant inside 90 days

**Manual**
- Promotion animation clean with no double text
- Focus session survives process kill
- **Swipe right completes; full swipe left swaps; delete requires a tap and offers undo; all three reachable without swiping**
- Widgets update after a completion
- Tutorial spotlights align on the smallest and largest screens
- Font scale 200 percent; dark mode across all Daylight screens; Contemplative screens identical in both system themes
- TalkBack pass, reduce motion pass, haptics correct and never repeated
- **Predictive back shows the correct destination from every screen**, verified by gesture on the device
- **Back during a focus session leaves the session running**, with the ongoing notification and the live countdown on the card both still correct
- **Every screen can be left without the tab bar**, verified by walking each destination and pressing back
- **Zero areas is reachable and usable**, with the FAB creating an area
- **A queued item opens an edit sheet**
- **Swipe: only one row opens at a time, a fling commits, a vertical gesture scrolls, delete collapses the row and undo restores it**
- **The build installs and launches on the physical Pixel**, not only on an emulator. Haptics, the focus session surviving a real process kill, and widget behavior cannot be trusted from an emulator

---

## 18. Out of Scope for v1

Sync of any kind, the data model is ready but the transport is not built. Any networking. Any permission beyond notifications. Any account. Wear OS. Tablet layouts, though the phone layout must not break. Locales beyond English. The four deferred widgets. Home screen shortcuts. Macrobenchmark harnesses. Analytics of any kind, permanently.

---

## 19. Build Order

**Phase 1. Foundations.** Scaffold. Theme: every token, type scale, shape, `ClarityMotion`, `ClarityHaptics`, reduce motion local. Event log schema, payload serialization, order keys. Reducer and all invariants. Replay test harness and golden fixture. `docs/EVENT_FORMAT.md`. **Git configured with the identity in 16.1, repo created public, everything committed.**

**Phase 2. Core mechanics.** Areas, items, queue. Repository write path, cache projection. Detail sheet, add item, edit, the two stage color picker, archive, tombstoned delete, drag reorder, **swipe gestures with state gating and non-swipe fallbacks**, undo snackbar. The Areas screen with the promotion animation.

**Phase 3. Trail.** Queries facade, screen, filters, day grouping, clustering, pagination.

**Phase 4. Focus.** Sessions, process death persistence, ongoing notification, completion flow, the indigo surface and its motion.

**Phase 5. Engine skeleton and simulator.** Fact extraction, rule catalog structure, selection, realization, validation. **The simulator in `devtools` before any corpus work.** Roughly 40 rules and 150 sentences to prove the shape end to end.

**Phase 6. Pulse.** Generation lifecycle per 11.3, the sheet, ambient mode, history, reminders.

**Phase 7. Momentum.** All five blocks plus the empty state.

**Phase 8. Snapshots and Report.** Week snapshots doubling as checkpoints, the integrity layer with tests written first, the screen with all four treatments including the week ribbon and the pattern grid break, history, regenerate, copy.

**Phase 9. Corpus.** Grow toward the sizing targets in batches of forty, one family at a time, judged against simulator output, presented for approval.

**Phase 9b. Guidance.** Cue fact extraction with confidence thresholds. Layer 6 and its composition rules. Plan events, the nominal offer frame, first-person storage on accept, the explicit decline. Non-evaluative follow-through by priority boost. **The non-compliance test written before the follow-through code, not after.**

**Phase 10. First run.** Onboarding four beats and the tutorial.

**Phase 11. Settings, About, data.** Every group, the appearance miniatures, the privacy sheet, licenses, the permission card, export, import, erase with the reset test, the links, the support block.

**Phase 12. Widgets and notifications.** Next Up and All Areas, the snapshot writer, the refresh job, channels and contextual permission.

**Phase 13. Ship.** Baseline Profile, R8, accessibility pass, the full checklist, real screenshots, README, release.

**The follow-through in phase 9b is the last thing built and the first thing removed** if it reads as supervision when tested. That reservation was formally registered by the review panel and it stands.

**At the end of every phase**, apply 16.8: commit, push, build, and install the updated app on the phone.
