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

Two documents sit beside this set rather than inside it. `docs/addenda/ADDENDUM_01_EXECUTIVE_FUNCTION.md` is provenance: it records what was asked for on one date and carries no authority of its own. DECISIONS.md at the repository root is the dated decision register, and it is where a conflict between an addendum and this document is resolved and reasoned. Where an addendum and this document disagree on behavior or data, this document wins, and DECISIONS.md says why. Addendum 01 also names four documents by names this repository does not use: MASTER_SPEC.md is this file, DESIGN.md is `design-v3.md`, DECISIONS.md was created at the root for it, and tools/audit.py is `audit.py` at the root.

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

### 3.2 Platform first, and when custom is right

A standing architectural rule, applied to every decision from here on. `design-v3.md` carries the visual half of it. **Check for a platform solution first. Use it when it fits. Build custom when it does not.** In order:

1. The official Material 3 Expressive component, unmodified
2. The official component, themed with our tokens
3. The official component, extended
4. **Custom**

The platform comes first for three reasons that have nothing to do with taste. Platform components get correct accessibility, right to left, dynamic type, predictive back and motion physics for free. They are hardware accelerated and tested at a scale this project cannot match. And they keep the codebase small enough for one person to maintain, which is the constraint every other constraint here answers to.

**Go custom, without hesitation, for any of four reasons.**

- **No platform equivalent exists.** Several things this app needs have none: the depleting focus ring, the week ribbon in the Report, the 14 day rhythm dot row, the area color wash, the two stage color picker, the tutorial spotlight. Do not contort a platform component into one of these shapes
- **The platform component carries meaning the app rejects.** Anything implying achievement, scoring, celebration or progress toward a target is wrong here regardless of how well it is built
- **The platform component fights a design rule.** If using it would put two separation devices on one element, a colored edge treatment, or an all caps label on a screen, the rule wins
- **The platform component is worse for this audience.** Motion or saturation that cannot be tamed through theming is a reason to build something calmer

**Reaching step 4 is the rule working, not a conflict.** The one reason that does not count is preferring the look of something you would draw yourself; that instinct belongs in the polish pass, which works through theming rather than replacement.

**Custom components inherit the platform's obligations.** Anything built by hand handles accessibility, right to left, dynamic type, reduce motion and calm mode to the standard the component it replaced would have met. That work is the real cost of going custom, and it is why the platform comes first when it fits.

**Record the decision either way**, one line in DECISIONS.md naming what was checked and which of the four reasons applied, so a later session does not redo the analysis.

The platform sources this app uses:

| for | use |
|---|---|
| components and motion physics | `androidx.compose.material3`, Material 3 Expressive, including its shape morphing and spring physics rather than hand rolled equivalents |
| iconography | Material Symbols wherever a symbol carries the right meaning. The SVGs in the visual references are illustrative and are not shipping assets. The mapping, including anything drawn by hand, is recorded in `design-v3.md` |
| widgets | `androidx.glance`, per 13.3 |
| the Live Update | `Notification.ProgressStyle`, per 14b.6 |
| app shortcuts | the androidx.core shortcuts APIs, per 13.5 |
| the quick settings tile | the platform `TileService`, per 13.5 |
| fonts | Google Fonts, downloaded and committed as files per 3.1. Never the Downloadable Fonts API |

### 3.3 Verify every version at build time

**Trust no library version, API level or platform requirement named in this document, in `design-v3.md`, or in any addendum.** They were written on one date and this project builds on another. Before integrating anything, check the current stable release and the current recommended integration path, and record what was chosen and why. This applies equally to a key derivation function's recommended parameters (14b.7) and to a Play Console policy (16.11), both of which move on their own schedule and neither of which announces that it has moved.

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

Payloads carry **full before and after values**, so replay reconstructs state without reading any other table. This is the trap in event sourcing: a log that reads nicely is not the same as a log that replays correctly.

**Display snapshots are carried where they are cheap, not everywhere.** Eight of the twenty eight types carry enough of one to name both the subject and the area of a Trail row with no lookup: the five area events that carry a name, and `ITEM_ADDED`, `ITEM_PROMOTED` and `ITEM_COMPLETED`. The other twenty resolve their display values by folding the log to the instant of the event, which is what `domain.query.TrailQueries` does. Neither path reads a live entity table, so a rename never rewrites an older Trail entry.

| type | payload |
|---|---|
| `AREA_CREATED` | areaId, name, colorHex, orderKey |
| `AREA_RENAMED` | areaId, previousName, newName |
| `AREA_RECOLORED` | areaId, previousHex, newHex |
| `AREA_REORDERED` | areaId, previousOrderKey, newOrderKey |
| `AREA_ARCHIVED` | areaId, nameSnapshot |
| `AREA_UNARCHIVED` | areaId, nameSnapshot |
| `AREA_DELETED` | areaId, nameSnapshot (tombstone, never a row removal) |
| `ITEM_ADDED` | itemId, areaId (nullable), title, note, orderKey, areaNameSnapshot, estimateMinutes (nullable), firstStep (nullable) |
| `ITEM_FILED` | itemId, areaId, orderKey, areaNameSnapshot |
| `ITEM_EDITED` | itemId, previousTitle, newTitle, previousNote, newNote |
| `ITEM_ESTIMATED` | itemId, previousEstimateMinutes, newEstimateMinutes |
| `ITEM_QUEUED` | itemId, areaId, orderKey, previousStatus |
| `ITEM_PROMOTED` | itemId, areaId, previousStatus, demotedItemId (nullable), demotedToOrderKey (nullable), titleSnapshot, areaNameSnapshot |
| `ITEM_COMPLETED` | itemId, areaId, titleSnapshot, areaNameSnapshot, activeDurationDays |
| `ITEM_REOPENED` | itemId, areaId, targetOrderKey |
| `ITEM_REORDERED` | itemId, areaId, previousOrderKey, newOrderKey |
| `ITEM_DELETED` | itemId, areaId (nullable), titleSnapshot (tombstone) |
| `FOCUS_STARTED` | sessionId, areaId, itemId, plannedSeconds |
| `FOCUS_COMPLETED` | sessionId, actualSeconds |
| `FOCUS_ENDED_EARLY` | sessionId, actualSeconds |
| `FOCUS_EXTENDED` | sessionId, addedSeconds, newPlannedSeconds |
| `PULSE_GENERATED` | pulseId, dateKey, family, escalationStage, register, variantKey, renderedObservation, renderedQuestion, factSnapshot, reflectionPeriod |
| `PULSE_ANSWERED` | pulseId, responseKey, responseLabel, responseIsPositive |
| `REPORT_GENERATED` | reportId, weekStartKey, headlineKey, renderedSections, factSnapshot |
| `PLAN_OFFERED` | planId, weekStartKey, frameKey, cueKey, actionKey, familyKey, subjectId, offeredLine, committedLine, resolutionFactRef |
| `PLAN_ACCEPTED` | planId |
| `SETTING_CHANGED` | key, previousValue, newValue (only for settings affecting behavior history, such as afterCompleting) |
| `APP_OPENED` | dateKey |

`ITEM_PROMOTED` carrying `demotedItemId` is what makes swap replay correctly, and is exactly the detail a descriptive log would omit.

**The four new types and the change to `ITEM_ADDED` come from Addendum 01**, and they are in the catalog now rather than in the phase that uses them because an event payload is nearly free to change before user data exists and expensive afterward. `docs/EVENT_FORMAT.md`, the golden fixture and the replay harness carry the same change, including replay coverage for an unfiled item, the filing transition and a session extension. The schema version those events are written at, and whether the event `schemaVersion` moves off 1, belong to that one schema commit, which also carries the payload questions already open as issue #19. A log written before the change must still replay: an absent optional field reads as null.

**`APP_OPENED` is a presence marker and is never counted as activity.** It records a date key, no time and no count, written at most once per calendar day on the first foreground, and it exists only so a gap can be detected without any tracking. It is excluded from `ClarityEventType.isUserActivity`, joining `PULSE_GENERATED`, `REPORT_GENERATED` and `PLAN_OFFERED`, and the predicate's complement is therefore no longer only the three events the engine writes: it is every event the app writes without a user act. This is not a nicety. `activeDays` and `totalEvents` count user activity, so an `APP_OPENED` that counted would make `mo.steady`, active on 9 or more of the last 14 days, fire for someone who did nothing for a fortnight, would turn `ob.day.l03`, `{n} of seven days had activity`, into a count of app opens, and would put `quietDay` nearly out of reach. For the same reason `APP_OPENED` renders no Trail row and is excluded from the day header event count in section 9: a log of when someone was present is a measurement of absence turned inside out, which is exactly what 14b.4 forbids.

**`ITEM_DELETED` carries a nullable area for the same reason.** Deleting is the one operation an inbox must always support, and an unfiled item has no area to name. Requiring one would have meant a person could put something in the inbox and get it out again only by filing it first, which is the opposite of what a capture surface is for.

**`ITEM_FILED` exists because `ITEM_ADDED` can now carry a null area.** An item with no area is real, is queryable, and is outside every area scoped invariant in 6.2 and outside every engine fact. It cannot be `ACTIVE` or `COMPLETED` until it is filed, and filing is the only transition into an area. `ITEM_FILED` carries `areaNameSnapshot` so a Trail row can name the destination without a lookup, and resolves the item title by folding, which is what every type without a full snapshot does. Room migrates to schema 3 for the nullable column.

**`FOCUS_ABANDONED` was renamed to `FOCUS_ENDED_EARLY`.** Addendum 01 4e requires that the word `abandoned` appear nowhere a person can see. No user visible string contained it, because the Trail already reads `Stopped after N minutes`, so the exposure was the type name itself: in the export file of 14b.7, which the addendum requires be readable when no password is set, and more importantly in `docs/EVENT_FORMAT.md`, which is the contract the future Linux desktop app is built against in a separate session. A name in that document is an instruction to the next implementer about what the concept means, and `abandoned` teaches the wrong one. Renamed in the schema commit while the window was open. DECISIONS.md C6.

### 5.3 Order keys

`orderKey` is a **fractional index**, stored as a String using a base 62 lexicographic scheme. **Never an Int.** Inserting between two items produces a key strictly between them without touching any other row. Integer positions break under concurrent reorder and cannot be retrofitted once user data exists.

Provide `OrderKey.between(a: String?, b: String?): String`, `OrderKey.first()`, `OrderKey.last(after: String)`, and a rebalance routine for the rare case where keys grow long.

### 5.4 The materialized cache

Room tables `clarity_area`, `clarity_item`, `clarity_focus_session`, `clarity_pulse_entry`, `clarity_report`, `clarity_week_snapshot`, `clarity_plan`, `clarity_conflict`. All derived. Each carries `deletedAt: Long?` and `lastEventLamport: Long`.

They exist purely for query speed. Any can be dropped and rebuilt from the log with no data loss, and a debug menu action does exactly that as a proof.

**DataStore holds only per-device values, never synced:** `theme`, `focusDurationMinutes` (default 25), `focusHighlightEnabled` (default true), `afterCompleting` (default AUTO_PROMOTE), `pulseRemindersEnabled` (default true), `pulseReminderHour` (default 20), `hasCompletedOnboarding`, `hasSeenTutorial`, `originId`, `lamportCounter`, `lastExportAt`, and `calmMode`, which joined the list in phase 3b. `calmMode` is **absent** until the user sets it, and while it is absent calm mode follows the OS reduce motion setting live, per 14b.12. **Pending from Addendum 01:** `transitionWarningEnabled`, default false, in phase 4.

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
- **Built in phase 3b.** An item with no area sits outside every invariant above. It is never `ACTIVE`, never `COMPLETED`, and never counted in any area's queue. `ITEM_FILED` naming an area that is unknown, deleted or archived is skipped like any other event referencing an unknown entity, which leaves the item unfiled rather than losing it

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

**Built in phase 3b.** Capture no longer requires an area at all. The FAB opens the add sheet with **no area chosen** and the item is written unfiled, where it sits in the inbox until it is filed; adding straight into a known area is reached from that area's detail sheet instead. The same sheet carries two optional fields, the first physical step and a time estimate. See 14b.1 to 14b.3.

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
- Early end: under 60 seconds discards silently as `FOCUS_ENDED_EARLY`. Past 60 seconds, a small confirm reading `End this session?` with `End` and `Keep going`
- **Abandonment is treated neutrally everywhere.** Pulse and Report language never blames
- While running, the Areas card for that area shows the intensified wash and live countdown when `focusHighlightEnabled` is on. **There is no bar**

**Pending, phase 4.** Four changes from Addendum 01 land in this flow: a session ended early is presented as a completed short session rather than as a stopped one, ten minutes can be added to a running session, an optional transition warning fires five minutes out, and the session becomes visible outside the app as a Live Update. See 14b.5 and 14b.6.

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

**Pending, phase 6.** Pulse generates nothing for the first two days after a return from a long absence, which makes those days IDLE and posts no reminder. See 14b.4.

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

**Pending, phases 6 through 9.** Four changes from Addendum 01 reach this screen. For a full week after a return from a long absence, every decline, neglect and gap family is unavailable to selection (14b.4). A dip that has a precedent in the user's own history is a rhythm and not a decline, and the two speak differently (14b.9). Estimate observations may state a ratio or a tendency and may never state a delta (14b.8). And every section that needs history says plainly what it needs and roughly when it becomes useful, rather than showing a zero (14b.10).

**Controls.** History (past reports by week), regenerate (spinner on the headline block, near instant), copy (plain text to the clipboard). The copy control is the app's only integration surface with anything else.

---

## 13. Onboarding, Tutorial, Widgets, Notifications, Shortcuts

### 13.1 Onboarding

Four beats, runs once, replayable from Settings. Entirely Contemplative. A persistent nav overlay: back chevron (hidden on beat 1) at 35 percent white, an 80dp progress line filling by beat, and `Jump in` at 30 percent white, always visible. Tap or swipe left advances, swipe right goes back.

**Beat 1, See It Work.** About 9 seconds, auto advances. Four colored demo cards enter with staggered three-part entrances. The top card's item strikes through and completes; the next queued title slides up and takes its place. One sentence: `One thing at a time. The next one is ready when you are.` **This beat must land the whole model in five seconds.**

**Beat 2, Your Areas.** The user picks two to four starter areas. Suggestion chips (Work, Personal, Health, Family, Learning, Side Project) plus a custom field. Each selection shows a mini card and opens the mood color rows. **Selections are transient in-memory structs; nothing is written until beat 3.**

**Beat 3, The Reveal.** Selected areas are written as real events, then an iris-open transition uncovers the user's actual Areas screen rendered live behind the overlay, with the closing line fading in and out: `Your clarity starts here.`

**Beat 4, The Depth.** About 20 to 25 seconds, four auto-paced moments, tap to advance. Moment 1, philosophy on black: `You just organized your focus.` then `Now let the app learn how you work.` Moment 2, Pulse: amber glow, a sample observation card with two response pills, caption `One question a day. One tap.` Moment 3, Momentum: blue glow, the 14 dot row fills day by day, caption `Your rhythm, without the guilt.` Moment 4, Report: gold glow, a miniature report headline, caption `Every week, an honest mirror. Written on your device.`

Then it opens the app with the tutorial queued. **There is no paywall beat and no sheet at the end.**

**Pending, phase 10.** Beat 2 gains a `Just start` path of equal standing, and beat 4 gains one line announcing Pulse before it ever appears. See 14b.11.

### 13.2 Tutorial

Five spotlight steps on first arrival at Areas. A full screen overlay above everything including the tab bar: 56 percent black radial dim, a feathered cutout, a slowly pulsing 2dp white ring at 38 percent, a floating tooltip in surfaceRaised with a step indicator. Skip always visible top right. Tap anywhere advances.

**Implementation:** targets report bounds via `onGloballyPositioned` keyed by stable test tags. **One uniform mechanism for every step, no per-step special cases.** This was a hard-won lesson in the iOS build; do not mix strategies.

**Steps:** 1 the FAB, 2 an area card, 3 the Focus chip, 4 the Pulse chip, 5 the tab bar.

### 13.3 Widgets

**Widgets matter more than notifications for this audience, and the reason is specific.** A widget is persistent and cannot be dismissed, so it works with out of sight, out of mind rather than against it. A notification is a one time event that is swiped away and forgotten. A widget is still there tomorrow. Eight are specified below and **six are required in v1**. This section was rewritten by Addendum 01; DECISIONS.md C4 records what it replaced and why.

The goal for every one of them is **zero taps to see**, and where an action exists, **one tap to act**.

**Where the data comes from.** The widget snapshot written to DataStore on every meaningful change, plus a WorkManager refresh every 6 hours. **Widgets never read a corpus and never run the engine.** Any sentence a widget shows was produced by the engine, written into the snapshot, and is repeated verbatim, per 11.2. A widget that composed its own sentence would be a second path to the screen, which 11.1 does not allow, and it would be the one path with no validator on it.

**Rules every widget follows.** Built with `androidx.glance`. Deep links open the right surface, except while a focus session is running, when any widget tap goes to the focus screen. If a configured area was deleted or archived, show a reconfigure prompt. Each one renders correctly in dark mode, honors calm mode, scales text without clipping at its smallest grid size, and shows a sensible state when it has nothing to show. Each one is usable with TalkBack and carries real content descriptions rather than a repeated label. **Every preview image in the widget picker is generated from the real widget and never from a mockup**, which is the same rule 16.6 applies to the README screenshots and for the same reason. The shared visual DNA is in `design-v3.md` 12; this section is the behavior.

**Required in v1. All pending, phase 12.**

| widget | size | what it shows | tap |
|---|---|---|---|
| **Next Up** | 2x2 | one active item: area dot, area name, title, and a count of what waits behind it. Configurable to a pinned area, or automatic, which shows the least recently touched active area and rotates daily | opens that area |
| **First Step** | 2x2 | the active item's **first step** rather than its title. With no first step set, the title and a quiet prompt to add one | starts a focus session on that item |
| **Quick Capture** | 2x2 or 1x1 | one large target, with the inbox count beneath it as plain text | opens capture straight into the unfiled inbox, keyboard up, no area to choose |
| **Focus Countdown** | 2x2 | live during a session: a depleting arc as the primary carrier, digits secondary. Otherwise a `Start focus` target | opens the focus screen |
| **All Areas** | 4x2 | every non-archived area as a row: dot, name, active item or `Idle`. Configurable to all areas or a chosen subset | opens that area |
| **Rhythm** | 4x2 | the 14 day dot row exactly as Momentum renders it, and one plain line beneath: `Active 11 of the last 14 days.` | opens Momentum |

**Optional, built if phase 12 has room.** **This Week** (2x2): three numbers from Momentum, completed, focus minutes and reflections, typographic, with no chart, no gauge and no ring toward a target, because there is no target. **One Thing** (4x2): the plan the user accepted from the most recent Report, in its first person committed form, or the Report headline when there is none. It is the only place guidance appears outside the Report, and it appears only because the user chose it. **Never an unaccepted plan. Never a declined one.**

**Four rules inside that table are easy to lose.**

- **First Step exists because the hardest moment is starting**, and the title of a task is often the intimidating part of it. `Rewrite the proposal intro` is a wall. `Open the doc and read what is there` is not. The smallest possible action on the home screen removes the activation barrier at the moment it bites
- **Rhythm must never become a streak.** No consecutive count, no chain, no language about breaking one. A gap is a lighter dot and nothing else, exactly as in 12.2
- **Quick Capture carries the whole capture principle.** Every decision between the thought and the record is a place the thought is lost, which is why it opens into the inbox and asks for nothing. The inbox count is plain text, **never a badge and never a dot**
- **Focus Countdown reads as a shape before it reads as a number**, per `design-v3.md`. Glance updates are throttled by the system, so its refresh cadence is chosen deliberately and the reasoning is recorded rather than tuned until it looks right

### 13.4 Notifications

Channels: **Focus** (session complete, default importance, gentle sound), **Reminders** (Pulse reminder), **Ongoing** (silent, the running session chronometer).

Request `POST_NOTIFICATIONS` contextually, the first time the user starts a focus session or enables a reminder. **Never at launch.** Every notification deep links correctly. **No marketing notifications, ever. No re-engagement notifications, ever. Nothing that exists to pull the user back.**

**Pending, phase 4.** One promoted notification joins these, the focus session Live Update, on the Ongoing channel and under the same rules: silent, dismissed when the session ends, never re-engaging. It needs `POST_PROMOTED_NOTIFICATIONS`, which is a notifications permission and therefore inside what section 18 allows. See 14b.6.

### 13.5 App shortcuts and the quick settings tile

**Both are pending, phase 12,** both are new in Addendum 01, and both supersede the line in section 18 that put home screen shortcuts out of scope for v1. The reasoning is the widgets' reasoning: fewer steps between an intention and an action.

**Three static shortcuts** on a long press of the app icon, built with the androidx.core shortcuts APIs: `Quick capture`, `Start focus`, `Today's Pulse`. Each opens the destination its matching widget opens, so quick capture lands in the unfiled inbox and today's Pulse opens the Pulse surface in whatever state it is in, including its ambient state on a day the engine stayed silent. **Static, not dynamic.** A shortcut list that reordered itself around what the user did most would be a measurement of the user, and it would be one they never asked for.

**One quick settings tile** that starts or ends a focus session from the notification shade, on the platform `TileService`, reflecting live session state. Tapping it with no active item anywhere opens the chooser rather than failing, which is the same degradation the Focus chip already performs in section 10.

---

## 14. Settings and About

### 14.1 Settings

A Daylight screen, rows on canvas under sentence-case sideheads, **no card containers.**

**Daily routine.** `Daily reflection` explainer row opening a short sheet describing Pulse. `Pulse reminder` toggle. `Remind me at` time picker, shown only when the toggle is on.

**Focus.** `Highlight the active session` toggle. `Session length` selector.

**After completing.** Segmented choice: `Promote next` versus `Choose from queue`, with an explanatory line reflecting the current selection.

**Appearance.** Three real-miniature tiles per `design-v3.md` 10.10. System is the default. A line below noting that Focus, Pulse and Report are always dark by design.

**Your data.** `Export everything`, one JSON file via the Storage Access Framework, showing the last export date. `Import from a file`, validating schema version and integrity before a transactional full replace with a typed confirmation. `Erase all data`, per 14.2.

**Pending from Addendum 01.** Two rows join this screen and one grows. Under **Focus**, `Five minute warning`, off by default, in phase 4 (14b.5). Under **Appearance**, `Calm mode`, following the OS reduce motion setting by default. **The setting was built in phase 3b and is honored everywhere in the app; the row is pending here, in phase 11, because this screen does not exist yet** (14b.12). Under **Your data**, export gains an optional password and a plain statement of what an unencrypted file is, import gains full pre validation and a choice of replace or merge, and one quiet line appears when the last export is older than 30 days and real data exists, in phase 11 (14b.7).

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

**Pending, phase 13.** One sentence sits under that paragraph, required verbatim by 16.11 and in the store listing at the same time: `Clarity Now is a productivity tool. It does not provide medical advice, diagnosis or treatment.`

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

## 14b. Executive function support

**Part of this section is now built.** Phase 3b landed 14b.1, 14b.2, the capture half of 14b.3, the detection half of 14b.4, and calm mode itself out of 14b.12. Everything else here is pending, and each part below states its own state. It comes from `docs/addenda/ADDENDUM_01_EXECUTIVE_FUNCTION.md`, a directive dated August 2026 produced from research on serving people with executive function challenges: ADHD, autism, brain fog from long COVID or ME/CFS, cognitive changes in perimenopause, TBI recovery, depression and anxiety, and burnout. It is written here as behavior and data because that is what this document is the authority on. The visual, motion and language halves are in `design-v3.md`. The reasoning, the market evidence, and the four things deliberately ruled out are in DECISIONS.md.

**It is numbered 14b rather than 15** because sections 15 through 19 are cited by number from the source, the tests, `CLAUDE.md` and the other documents in the set, and renumbering them would break those citations silently. The project already uses this form for work inserted after a plan was set, as phase 9b and phase 3b do in section 19.

**None of this is a rebuild.** The core mechanic is unchanged: one active item per area, everything else waits, complete the active item and the next is promoted. What follows does two things. It removes decisions from the path between an intention and a record, and it removes measurement from the path back after an absence.

### 14b.1 Capture without a decision, the unfiled inbox

**Built in phase 3b.** From Addendum 01 4a.

**Capture must never require a decision.** Adding an item does not require choosing an area. `ITEM_ADDED` is written with a null `areaId`, the item exists, and the thought is out of the person's head, which is the entire job of the capture path. Filing is a separate, later, optional act.

**What an unfiled item may do.** It can be edited (`ITEM_EDITED`), deleted (`ITEM_DELETED`, with the same 5 second undo as anywhere else), and filed (`ITEM_FILED`).

**What it may not do.** It cannot be `ACTIVE` and it cannot be `COMPLETED`. Completing an unfiled item is not offered anywhere, in the same way and for the same reason that completing a queued item is not offered in 8.2: the rule that only one thing is active at a time is the philosophical work this app does, and an inbox that could be worked straight through would be a second, unlimited queue with no area attached to it.

**Filing.** `ITEM_FILED` carries the target area, an `orderKey` and an `areaNameSnapshot`. If the target area has no active item, filing promotes it in the same transaction, `ITEM_FILED` then `ITEM_PROMOTED`, matching what 8.2 does on add. The filing sheet states where the item will land before the user commits, exactly as the add sheet does.

**The inbox is reachable from the Areas screen** and its count is shown quietly. **Never a badge, never a red dot, never a color that reads as an alert.** An inbox that nags is a worse place to put a thought than a notes app, and a person who has learned that capture produces a scolding number stops capturing.

**An unfiled item is invisible to the engine.** It is outside every area scoped invariant in 6.2, outside every fact the engine extracts, never counted in a queue length, and never named in an observation. It is in the Trail, because `ITEM_ADDED` and `ITEM_FILED` are user activity and the Trail is the record of what happened.

**What the FAB means, with at least one area.** It opens the add sheet with **no area chosen**, and the item is written with a null `areaId`. The sheet carries no area control at all, because a picker with N options is the decision this whole path exists to remove, and an unselected row of areas reads as an unanswered question. Adding straight into a known area is unchanged and is one tap away, on that area's detail sheet, where the area is context rather than a choice. Before phase 3b the FAB added into whichever area sorted first, which was a decision the app made on the person's behalf and got right only by accident. DECISIONS.md.

**One question this leaves open, and it is not settled here.** 8.4 says that at zero areas the FAB creates an area rather than an item, and that behavior is shipped and is in the checklist in section 17. Capture into the inbox works with no areas at all, and the Quick Capture widget in 13.3 depends on that. So at zero areas the FAB has two defensible meanings. The recommendation on the record is that capture always means capture, and that the Areas empty state carries its own create action, which removes a mode rather than adding one. **Until that is answered, 8.4 stands as written**, and phase 3b does not change it.

### 14b.2 The first step

**Built in phase 3b.** From Addendum 01 4b.

One optional line on an item: **the first physical action.** Shown on the active item card at caption weight when present, absent from the card entirely when not set. Never required, never prompted for, never inferred, and deletable at any time.

It is the deterministic answer to the thing an AI task breakdown would be reached for, which is why DECISIONS.md rules that out and this in. The user writes the small action; the app stores it and shows it at the moment it is needed, which is the moment before starting, on the card and on the First Step widget in 13.3.

Carried on `ITEM_ADDED` as `firstStep`, and edited through `ITEM_EDITED` like the title and the note.

### 14b.3 The time estimate

**Capture built in phase 3b. The observation half is pending, phase 8.** From Addendum 01 4c.

An optional estimate in minutes on an item, carried on `ITEM_ADDED` as `estimateMinutes` and changed afterward with `ITEM_ESTIMATED`, which records the previous and the new value so a changed estimate does not edit history.

**The actual comes free.** The Trail already records when an item became active and when it completed, so nothing new is measured and nothing new is asked for.

**The input is a free number field, digits only and four of them at most, rather than a set of preset durations.** The obvious answer is a row of chips, and it loses twice: it is a decision with five options placed in the capture path, which is what this work exists to remove, and the buckets somebody else chose would silently become the shape of the data 14b.8 reads as a ratio. DECISIONS.md.

**Never a required field. Never a countdown against the item.** An estimate that turns into a visible timer on the card is a deadline the person set for themselves in a hopeful moment and then has to watch expire. What the estimate is for is 14b.8, and what it may never say is also 14b.8.

### 14b.4 Re-entry after an absence

**Detection built in phase 3b. The surface is pending, phase 6.** From Addendum 01 4d.

**This is the highest stakes screen in the app.** It is also the one screen nobody building or testing the app daily will ever see, which is exactly why it has to be specified rather than discovered.

This audience leaves and comes back. A fortnight of nothing is not a failure of the user. It is what a fluctuating condition, a bad month, a hospital stay or an ordinary overwhelming stretch looks like from inside the data. The app has one chance to be the thing that did not keep score while they were gone, and one screen in which to spend it.

**Detection.** `APP_OPENED` carries a date key and nothing else, written at most once per calendar day on the first foreground. The gap is the number of calendar days between the newest `APP_OPENED` before today's and today, read from the log through `TrailQueries` and never from a DataStore timestamp, so a restored backup or a second device reaches the same answer. **A gap of 14 or more days puts the app into the re-entry state**, on the foreground that writes today's `APP_OPENED` and only then, so it appears at most once per calendar day and in practice once per gap. It does not apply before onboarding is complete or when no earlier `APP_OPENED` exists.

**What detection landed as.** `ClarityApp` counts started activities and writes the marker when that count crosses zero, rather than writing it in `Application.onCreate`. The difference is not academic: from phase 12 this process is created by a widget update and by a scheduled refresh as well as by a person, and a process kept alive across midnight would never write the second day's marker at all, which would report an absence nobody had. `TrailQueries.reEntryOn(dateKey)` answers the question the surface asks on the day it appears; `TrailQueries.lastReEntryOnOrBefore(dateKey)` answers the one the two suppression windows ask for days afterward. Both run the same arithmetic over calendar dates, so the day the screen appears and the day the Report starts withholding can never be two different days. **The value they return carries the date of the return and not the length of the absence.** There is no field holding the number and no function handing it out, because a prohibition that rests on somebody remembering it is a prohibition with a shelf life. The onboarding gate above belongs to the surface rather than to the query: the query has no way to know.

**What the re-entry state does.**

- It offers to **keep everything exactly as it was**, and that is the default and the larger of the two choices
- It offers, second and quieter, to clear the active items and start fresh. **Clearing demotes**: each active item returns to the head of its own queue with `ITEM_QUEUED`. Nothing is deleted, nothing is completed, and the wording says so. The obvious implementation of a fresh start is a delete, and a delete of a person's own work on the day they came back is the single most expensive thing this app could do
- It is dismissed by either choice and is not shown again for that gap

**What it must never do.**

- **It does not state the length of the gap.** Not in days, not in weeks, not as a date, not as `since March`
- **It does not count anything.** Not what waits, not what was completed before, not how many areas went idle
- **It does not ask where the user has been**, in any wording, including a warm one
- **Pulse generates nothing for the first two days back.** The engine returns Silent for those two date keys and writes no `PULSE_GENERATED`, so the days are IDLE, the chip shows no dot and no reminder is posted. These suppressed days sit outside the Pulse silence floor in section 17, in both the numerator and the denominator, because the floor measures how often the engine chose not to speak and this is not a choice it made
- **The Report suppresses every decline, neglect and gap observation for a full week back.** For seven days from the re-entry date every rule in those families is unavailable to selection and the next ranked candidate is taken instead. The report is shorter when nothing else qualifies, because 11.4 forbids padding a section to reach a minimum. The same suppression applies to the Momentum headline and the Areas banner, which read from the same catalog
- **It never appears alongside the tutorial, a conflict card, or anything else that wants the first moment.** It is the first screen and it is alone. A conflict card from 6.3 waits behind it rather than being dropped, because a conflict is never silent

**A returning user must never be greeted by a measurement of their absence.** If a sentence, a number, a dot row or an empty chart on the first screen back can be read as a report on how long they were gone, it is wrong, whatever else is true about it.

### 14b.5 Focus sessions: ending early, adding time, the transition warning

**Pending, phase 4.** From Addendum 01 4e, 4f and 4g. Section 10 is otherwise unchanged.

**Ending early is a success state.** A session ended early is a **completed short session**, and the completion screen says so in the same shape a natural completion uses, with the same actions. Fourteen minutes is fourteen minutes. The rule in 10 that discards a session under 60 seconds silently stands, because that is a mis-tap rather than a short session.

**The word `abandoned` appears nowhere a person can see it**, including the Trail, every accessibility label, and the export file in 14b.7. The Trail already reads `Stopped after N minutes`. The event type itself was renamed from `FOCUS_ABANDONED` to `FOCUS_ENDED_EARLY`, because a raw type name is visible in a readable export and, more importantly, in `docs/EVENT_FORMAT.md`, which a second implementation is built from. DECISIONS.md C6.

**Adding time.** An `Add 10 minutes` control extends the running session without resetting it and without starting a new one. It writes `FOCUS_EXTENDED` with the added seconds and the new planned total, and the persisted end timestamp is recomputed so the session still survives process death per section 10. It is repeatable and uncapped. The reducer folds extensions, so a session's planned duration is the newest `newPlannedSeconds` rather than the value in `FOCUS_STARTED`, and every later reader, the completion path, the Trail, the engine and the widget, reads the folded value. **Ending a timer should not have to break flow.** It is reachable from the focus screen, from the Live Update and from the ongoing notification.

**The transition warning is optional and off by default.** A quiet five minutes left signal before a session ends, controlled in Settings. **Never a full notification unless the app is backgrounded.** It does not fire when fewer than five minutes remain at the moment the session starts or the moment it is switched on, which would make it fire immediately and teach the person to distrust it. Switching from one task to another is the expensive act for this audience, and a warning is the difference between a transition and an interruption. It is off by default because an unannounced signal is also an interruption.

### 14b.6 The Live Update

**Pending, phase 4, extended in phase 12.** From Addendum 01 Step 5. The surface itself is specified in `design-v3.md`.

A focus session is exactly the user initiated, start to end, time bound task that Android's Live Updates exist for. On a Pixel it surfaces as a status bar chip that expands, and on Samsung devices in the Now Bar. **For an audience with time blindness, the session being visible outside the app is not a nicety, it is the point.**

**Use the platform API, not a custom notification dressed to look like one.** `Notification.ProgressStyle`, introduced in Android 16, API 36. Declare `POST_PROMOTED_NOTIFICATIONS`. Check `NotificationManager.canPostPromotedNotifications()` before posting and degrade silently when it is false. Verify the current API details before implementing, per 3.3.

**What it shows.** The area name, the item title, and the remaining time as a depleting track. A single track is the likely right answer; use segments or points only if they genuinely add clarity. When the transition warning is enabled, the track changes state at the five minute mark.

**Actions, two at most.** `Add 10 min` and `End`. Both work without opening the app. Tapping the body opens the focus screen.

**Degradation is required, not optional.** Below API 36, or where promoted notifications are unavailable or denied, fall back to the ongoing notification with a chronometer that section 10 already specifies. The app is fully usable with no Live Update at all. **Never gate a feature behind it and never tell the user their device is missing out.**

**This is the only Live Update the app will ever post.** Not for Pulse, not for the Report, not for a reminder, not for anything the user did not just start. It is silent, it is dismissed when the session ends, it never re-engages, and it is not a marketing surface.

### 14b.7 Backup, export and import, as a safety feature

**Pending, phase 11.** From Addendum 01 4h. This replaces the two sentence description of export and import in 14.1 with the requirements they have to meet.

Because everything is local, **the user's data has exactly one copy unless they make another**. Export is a safety feature, not a convenience feature, and it is treated with that seriousness. Everything else in this app is recoverable by rebuilding from the log. This is the one path where a mistake is permanent.

**Export.**

- Writes the **entire** database, every event and all derived state, to one portable file through the Storage Access Framework
- Offers an **optional password**. When set, the file is encrypted with a key derived from the password by a modern KDF at its current recommended parameters, verified at build time per 3.3, never at the parameters written in a document. When not set, the file is readable, **and the export screen says so plainly** rather than implying a safety it does not provide
- The file carries a schema version, a creation date, an item count, an event count, and a checksum
- The export path runs a full rebuild from event zero as a correctness check, which 6.4 already requires of it

**Import.**

- Validates schema version, checksum and the internal integrity of the event log **before touching anything**. Nothing is written until validation passes
- A corrupt, truncated or foreign file is refused with **one plain sentence saying what was wrong**. That sentence is about a file rather than about the person's own data, so it is a fixed string in `strings.xml` and not a corpus line. It is one of the few places rule 11.1 does not reach, and the reason it does not is worth stating rather than leaving to be rediscovered
- Offers **replace or merge**. Replace is one transaction. Merge is the deterministic event union the sync design already specifies in 6.3 and section 7: union by event id, order by `(lamport, originId)`, advance the local lamport to `max(local, seen) + 1`
- **A wrong password fails clearly and destroys nothing**

**Tests, both required.** One performs a full round trip, encrypted and unencrypted, and asserts byte identical state. One feeds it deliberately corrupted files, at minimum a truncation, a flipped bit inside the payload, a wrong checksum, an unknown schema version, a foreign JSON document and a wrong password, and asserts a clean refusal with the database unchanged.

**The reminder, and its limits.** Settings shows the date of the last export, from `lastExportAt`. If more than 30 days have passed **and** real data exists, one quiet line appears **in Settings only**. Never a notification, never a nag, never a badge, never a card on Areas.

### 14b.8 Estimates are calibration, never error

**Pending. The facts and the veto in phase 8, the language in phase 9.** From Addendum 01 7a.

**Hard rule, enforced in the validator: no rendered sentence may state a delta between an estimate and an actual.**

| | |
|---|---|
| permitted | `Things you estimate at an hour tend to take about three.` |
| forbidden | `You underestimated by two hours.` |
| forbidden | `You were off by 140 percent.` |

Only ratios and tendencies. The difference is not politeness and it is not tone. A ratio is a description of how this person's estimates map onto their days, which is useful and which they can do something with. A delta is a score against a target they set themselves, and time blindness is the reason the estimate was wrong in the first place, so the delta measures the symptom and reports it as a mistake.

**Floor.** No estimate observation may fire until **at least five completed items carry an estimate inside the window the sentence describes**, and the count travels as a `FactRef` so the validator re-reads it, per 11.4.

**A new observation family** is authored in phase 9 with the rest of the corpus, and **a veto test constructs the forbidden form and proves it cannot render**, which is the same shape as the Report integrity vetoes in 12.3 and is listed in section 17.

### 14b.9 Capacity aware decline detection

**Pending, phase 8.** From Addendum 01 7b.

**This is a correctness fix, not politeness.**

A fluctuating condition looks identical to decline in the data. Both are a fall in completions, a rise in idle days, an area going quiet. Without this check, the app will tell someone with a cyclical or relapsing condition that they are deteriorating, **on a fixed schedule, forever, and it will be technically accurate every time**. Every individual report passes its integrity rules. The claim the sequence makes is still false, because the shape it is reading is a cycle and it has read only half of one.

**The rule.** Before any decline, neglect or fading family may fire, the engine asks whether this shape has occurred before in this user's history for this subject. If a comparable dip has happened before, **it is a rhythm, not a decline**, and a different family fires with different language.

This needs a new fact, rules for both branches, and tests for both. The fact's definition, what makes a dip comparable in depth and in duration, belongs in `CLARITY_LOGIC_ENGINE.md` with the other fact definitions; what this document requires is that the fact exist, that it gate those families rather than merely re-word them, and that the gate be reachable in a test.

**The test that proves it** is a persona whose activity is cyclical across a simulated year, who must receive no decline, neglect or fading observation at all, because every dip they have has a precedent. It is listed in section 17 beside the non-compliance test in 9b, which it resembles: both assert that a whole year of output contains no sentence of a given kind.

### 14b.10 Tone, and the first weeks

**Pending. The tone pass in phase 9, the honest empty states in phases 6, 7 and 8.** From Addendum 01 7c and 7d.

**Rejection sensitivity is common in this population, and an observation read as a verdict is how someone deletes the app.** The corpus is where this is fixed, so the work is a corpus batch presented for approval like any other, per 11.4. Four changes:

- **Widen the `unflattering` flag** to cover every rule concerning a decline, a gap, a neglect, an imbalance or an unmet expectation. The enumeration this is checked against lives in `CLARITY_LOGIC_ENGINE.md` 7.4 and is amended with it, because section 17 asserts the two match
- **Author the missing `NEUTRAL_AGENT` variants.** Any family that can now fire unflattering at a stage where it has no neutral agent variant needs them written
- **Soften `pt.gone`.** `Personal is not on pause. It is stopped.` is the strongest line in the corpus for a general reader and the worst line in it for this one. The flagship becomes a factual form. The pointed version survives only where the user has previously indicated that the area is deliberately paused, which means it needs a fact with a real source behind it, and a Pulse response is the only source the app has. **If no response can supply that fact, the pointed variant does not ship**, because inferring that someone meant to stop is the inference this whole section exists to prevent
- **Ship the `hardStretch` family** with every constraint already specified for it. Nothing in the corpus can currently acknowledge that a hard month was hard

**The first weeks are honest.** The reflective layer needs history, so it is emptiest exactly when a person is deciding whether to keep the app. Every reflective surface states plainly what it needs and roughly when it becomes useful, in the shape of `Patterns show up after about three weeks.` or `This fills in as the days do.` **Never an empty chart. Never a zero with no explanation.** These sentences are about the person's own data, or the absence of it, so they are corpus lines from the edge state sections of `CORPUS_2_REPORT.md`, reached through the engine like everything else. They are not `strings.xml` copy, and rule 11.1 has no exception here.

### 14b.11 First run without a decision

**Pending, phase 10.** From Addendum 01 8a and 8b. Section 13.1 is otherwise unchanged.

**The zero decision path.** Onboarding beat 2 asks for two to four area names plus a color each, which is up to twelve decisions demanded from people whose central difficulty is deciding. Beat 2 gains a **`Just start`** option offered as a genuine equal alternative, **not buried, not a text link under the real button**. It creates one area named `Today` with the color the mood walk in 8.1 yields first, writes it as a real `AREA_CREATED` exactly as beat 3 would, and drops the user straight into adding their first item. Areas, names and colors become things discovered later, which is the order most people would have chosen anyway.

**Announce Pulse before it appears.** One line at the end of onboarding: once a day, one question, one tap, and it can be turned off in Settings. **Predictability matters enormously to autistic users, and interface behavior that arrives unannounced is a real cost**, not a delightful surprise. This line is fixed copy about how the app works rather than an observation about the person, so it lives in `strings.xml`, per 11.2. This is the exact kind of sentence a session will be tempted to route through the engine, and it must not be.

### 14b.12 What this adds to Settings and to DataStore

**Calm mode and the entrance rule are built, phase 3b. Both Settings rows are pending, phases 4 and 11.**

Two per-device keys join the list in 5.4, and neither is engine state, so neither violates the rule that nothing the engine reads may live in DataStore.

| key | default | phase | row |
|---|---|---|---|
| `calmMode` | follow the OS reduce motion setting | key 3b, row 11 | Appearance |
| `transitionWarningEnabled` | false | 4 | Focus |

**Calm mode** is a Settings toggle in addition to and independent of the OS reduce motion setting, which `design-v3.md` 8.3 already honors. It reduces motion to crossfades, reduces the saturation of washes and accents, disables the staggered list entrance and the breathing glow, and **applies to the widgets and the Live Update as well as to the app**. `design-v3.md` owns every value it changes. It is what makes Material 3 Expressive safe for this audience rather than overwhelming: **ship the expressive direction and the exit.**

**What was built, and the one piece that was not.** The switch, the color transform, the entrance rule and the three audits in `design-v3.md` 16.6 to 16.8 are in the code, and calm mode joins the one global motion flag with an `or` rather than adding a motion level beside it, so **reduce motion always wins on motion**. The `Calm mode` row itself is pending, phase 11, because there is no Settings screen to put it on, and the two carry-forwards named above are pending with their own surfaces: the Live Update honors it in phase 4 and the widgets in phase 12, both by reading it out of the widget snapshot rather than out of DataStore, which is not multi process safe. Until then the key has no stored value, which means calm mode follows the OS reduce motion setting live, which is its specified default. **The stored value is nullable rather than a boolean, and that is load bearing:** a `Boolean` defaulting to false would mean off for every person who has the system setting on and never opens Settings, which is precisely the person the feature exists for. Absence is a state the storage carries and the interface never shows.

Phase 3b also built one motion change from Addendum 01 8e that has no setting behind it: **an entrance fires on the first open of its tab per app session, not on every return to a tab.** An entrance animation on a screen opened twenty times a day stops being an entrance and becomes noise. A session is the process lifetime, so a rotation does not re-arm it and a process death does. The rule is stated once in `design-v3.md` 8.4 and governs every entrance in 8.2 rather than being repeated beside each one. Calm mode removes those entrances entirely rather than reducing them.

**Export's rows** grow as 14b.7 describes, in phase 11.

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

### 16.11 Positioning and the store listing

**Pending, phase 13.** From Addendum 01 Step 10, recorded now because the words chosen here constrain copy written long before the listing is, and because this is the one mistake in the project that gets the app removed rather than reviewed.

**Use the words people actually search for. Do not make medical claims.** These are different things, and the distinction is exactly what Google Play's health policy turns on. **A claim triggers it. A keyword does not.**

**Permitted and encouraged**, in the long description and the keywords:

> ADHD, autism, executive function, executive dysfunction, task paralysis, time blindness, brain fog, neurodivergent, overwhelm, procrastination, focus, one thing at a time

Phrasing of this shape: `Built for people who find long lists paralyzing. Designed with ADHD, autism and brain fog in mind.`

**Forbidden**, anywhere in the listing, in app copy, or in anything published about the app:

> treats, manages, cures, therapy, therapeutic, clinically proven, medically, symptoms, diagnosis, disorder used as a claim, and any statement that the app improves or reduces anything clinical

**Required, verbatim, in the listing and in About:**

> Clarity Now is a productivity tool. It does not provide medical advice, diagnosis or treatment.

**Complete Google Play's Health Apps Declaration.** With no data collection, no account, no health data access and no network permission, this app should certify cleanly, and the Data Safety answers in 14.3 already say so. **Verify the current requirements in Play Console rather than trusting this note.** The policy moved through 2025 and added medical device labeling in January 2026. 3.3 applies to a policy exactly as it applies to a library version.

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

**Executive function support, per 14b. Each becomes live when its phase lands**
- **No rendered sentence states a delta between an estimate and an actual.** A veto test constructs the forbidden form and asserts it cannot render
- No estimate observation fires below five completed items carrying an estimate inside the window the sentence describes
- **A persona whose activity is cyclical across a simulated year receives no decline, neglect or fading observation**, because every dip they have has a precedent
- **A re-entry persona receives no Pulse for two days and no decline, neglect or gap observation for a week**, and no surface states the length of the gap, counts anything, or asks where they were
- **The word `abandoned` reaches nothing a person can see**, including the Trail and every accessibility label. Whether it also has to be absent from the export file follows the open naming decision in 5.2
- Export and import round trip, encrypted and unencrypted, to byte identical state, and a corruption suite refuses cleanly and leaves the database unchanged
- An unfiled item is never `ACTIVE`, never `COMPLETED`, never counted in an area's queue, and never named by the engine
- **`APP_OPENED` is excluded from `isUserActivity`**, from the Trail, and from every day header count
- **No widget reads a corpus or runs the engine.** Automated over the imports of the `widget` package
- Calm mode is honored in the app, in every widget and in the Live Update, and the staggered entrance fires once per session per screen

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

Sync of any kind, the data model is ready but the transport is not built. Any networking. Any permission beyond notifications, which includes `POST_PROMOTED_NOTIFICATIONS` and therefore permits the Live Update in 14b.6. Any account. Wear OS. Tablet layouts, though the phone layout must not break. Locales beyond English. Macrobenchmark harnesses. Analytics of any kind, permanently.

**Permanently out of scope, and now for a second reason.** Real time social presence of any kind, including body doubling, because it needs networking and would cost the no internet permission guarantee, which is this app's strongest claim. AI task breakdown, per non-negotiable 5; the first step field in 14b.2 is the deterministic version of the same idea. Streaks, badges, XP, levels, confetti and celebration, which were already forbidden by the design and are now also a documented abandonment trigger for this audience. DECISIONS.md holds the full reasoning for all four.

---

## 19. Build Order

**This is the single place a session reads to know what is next**, so every item Addendum 01 adds is written into the phase that owns it rather than left in the addendum. A phase marked done here is shipped and installed. `docs/BUILD_STATE.md` is the live record of what is half done, what is known broken and what the last session left behind; this list is the plan.

**Phase 1. Foundations. Done.** Scaffold. Theme: every token, type scale, shape, `ClarityMotion`, `ClarityHaptics`, reduce motion local. Event log schema, payload serialization, order keys. Reducer and all invariants. Replay test harness and golden fixture. `docs/EVENT_FORMAT.md`. **Git configured with the identity in 16.1, repo created public, everything committed.**

**Phase 2. Core mechanics. Done.** Areas, items, queue. Repository write path, cache projection. Detail sheet, add item, edit, the two stage color picker, archive, tombstoned delete, drag reorder, **swipe gestures with state gating and non-swipe fallbacks**, undo snackbar. The Areas screen with the promotion animation.

**Phase 3. Trail. Done.** Queries facade, screen, filters, day grouping, clustering, pagination.

**Phase 3b. Executive function retrofit. Built, and awaiting the device check that closes it.** **It exists because Addendum 01 assigned six of its items to phases 1 and 2, and both are closed and shipped**, and because phase 4 and phase 6 depend on parts of it. It carried: capture with no area and the unfiled inbox (14b.1), the first step field (14b.2), the estimate on capture (14b.3), re-entry detection, meaning `APP_OPENED`, the gap query and the `isUserActivity` exclusion (14b.4), calm mode with its color transform, its one motion flag and the three audits in `design-v3.md` 16.6 to 16.8 (14b.12), and the staggered entrance fired once per tab per app session. Trail rows for the new event types, and none for `APP_OPENED`. It assumed the Addendum 01 event schema in 5.2 was already in the log, and it was, landed in the schema commit that also settled issue #19.

**Two pieces named in this phase are deliberately not in it, and neither is an oversight.** The re-entry **surface** is phase 6, because a screen that has to be able to say nothing is an engine decision and the engine does not exist until phase 5. The `Calm mode` **Settings row** is phase 11, because there is no Settings screen to put it on; the setting behind it is built and honored everywhere, and until the row exists it follows the OS reduce motion setting, which is its specified default.

**Phase 4. Focus.** Sessions, process death persistence, ongoing notification, completion flow, the indigo surface and its motion. **Plus Addendum 01:** early ending as a completed short session, `Add 10 minutes` writing `FOCUS_EXTENDED`, the transition warning off by default (14b.5), and the Live Update on `Notification.ProgressStyle` with its required silent fallback (14b.6).

**Phase 5. Engine skeleton and simulator.** Fact extraction, rule catalog structure, selection, realization, validation. **The simulator in `devtools` before any corpus work.** Roughly 40 rules and 150 sentences to prove the shape end to end.

**Phase 6. Pulse.** Generation lifecycle per 11.3, the sheet, ambient mode, history, reminders. **Plus Addendum 01:** the re-entry surface and the two day Pulse silence after a return (14b.4), and an empty state that says what it needs and roughly when it becomes useful (14b.10).

**Phase 7. Momentum.** All five blocks plus the empty state. **Plus Addendum 01:** the same honest first weeks treatment on every block that needs history, and no empty chart anywhere (14b.10).

**Phase 8. Snapshots and Report.** Week snapshots doubling as checkpoints, the integrity layer with tests written first, the screen with all four treatments including the week ribbon and the pattern grid break, history, regenerate, copy. **Plus Addendum 01:** capacity aware decline detection and its cyclical persona test (14b.9), the estimate calibration facts, their floor and the delta veto (14b.8), and the week long suppression after a return (14b.4).

**Phase 9. Corpus.** Grow toward the sizing targets in batches of forty, one family at a time, judged against simulator output, presented for approval. **Plus Addendum 01:** the tone pass, meaning the widened `unflattering` enumeration, the missing `NEUTRAL_AGENT` variants, the softened `pt.gone` flagship and the `hardStretch` family, plus the estimate observation family in ratio and tendency form only (14b.8, 14b.10).

**Phase 9b. Guidance.** Cue fact extraction with confidence thresholds. Layer 6 and its composition rules. Plan events, the nominal offer frame, first-person storage on accept, the explicit decline. Non-evaluative follow-through by priority boost. **The non-compliance test written before the follow-through code, not after.**

**Phase 10. First run.** Onboarding four beats and the tutorial. **Plus Addendum 01:** the `Just start` path at equal standing on beat 2, and the line that announces Pulse before it ever appears (14b.11).

**Phase 11. Settings, About, data.** Every group, the appearance miniatures, the privacy sheet, licenses, the permission card, export, import, erase with the reset test, the links, the support block. **Plus Addendum 01:** export as a safety feature, meaning the optional password and its KDF, the checksum and full pre validation, replace or merge, the round trip and corruption tests and the quiet last export line (14b.7), and the disclaimer sentence in About that 16.11 requires.

**Phase 12. Widgets and notifications.** Six required widgets and two optional ones per 13.3, the snapshot writer, the refresh job, real preview images, channels and contextual permission. **Plus Addendum 01:** three static app shortcuts and the quick settings tile (13.5), and the phase 12 extension of the Live Update (14b.6).

**Phase 13. Ship.** Baseline Profile, R8, accessibility pass, the full checklist, real screenshots, README, release. **Plus Addendum 01:** the store listing and its keywords, the forbidden claim words, the required disclaimer and the Play Health Apps Declaration, all per 16.11.

**The follow-through in phase 9b is the last thing built and the first thing removed** if it reads as supervision when tested. That reservation was formally registered by the review panel and it stands.

**At the end of every phase**, apply 16.8: commit, push, build, and install the updated app on the phone.
