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
| app shortcuts | the androidx.core shortcuts APIs, per 13.5. Built as the `res/xml/shortcuts.xml` resource those APIs parse, because no androidx entry point publishes a shortcut without making it dynamic and 13.5 rules dynamic out |
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

**DataStore holds only per-device values, never synced:** `theme`, `focusDurationMinutes` (default 25), `focusHighlightEnabled` (default true), `afterCompleting` (default AUTO_PROMOTE), `pulseRemindersEnabled` (default true), `pulseReminderHour` (default 20), `hasCompletedOnboarding`, `hasSeenTutorial`, `originId`, `lamportCounter`, `lastExportAt`, and `calmMode`, which joined the list in phase 3b. `calmMode` is **absent** until the user sets it, and while it is absent calm mode follows the OS reduce motion setting live, per 14b.12. `transitionWarningEnabled`, default false, joined in phase 4; its Settings row is
phase 11. **Phase 4 added two more, `focusSessionId` and `focusSessionEndsAt`**, which
together name the running session this device is the one running and the instant its
planned time ends. They are a cache rather than engine state, which is the distinction
rule 6 in `CLAUDE.md` is really applying: the log holds the start and the folded
extensions, so any device computes the same end instant with no help from these, and
the log wins whenever the two disagree. What they add is the one fact in a session that
is about a phone rather than about a person, since a merged log can legitimately carry
one running session per device. See 10 and 14b.5.

**`textSize` joined in the accessibility pass for Addendum 01 8f, issue #51.** It stores
one of five named steps and defaults to `DEFAULT`, which is a real default rather than a
placeholder: the setting **multiplies** the OS font scale rather than replacing it, so
`DEFAULT` already means "whatever this phone asks for" and there is no absent third state
to resolve the way `calmMode` has one. It is a fact about a screen and not about a person,
so no corpus line, no observation and no engine layer reads it, and two devices holding
the same log compute the same sentence while rendering it at different sizes. `design-v3.md`
13.2 owns the behavior, the 200 percent cap on the combined scale and the spacing rule that
comes with it.

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

**Built, phase 8.** Issue #6.

`ClarityWeekSnapshot` doubles as a replay checkpoint: a serialized `ClarityState` plus the `lamport` it was taken at, written when a week closes. Cold start loads the newest checkpoint and replays only events after it. A full rebuild from event zero is available in the debug menu and runs in the export path as a correctness check.

**Resuming is safe only while the log still begins with the prefix the checkpoint was folded from, and that is a count rather than a comparison of the two ends.** A checkpoint is the fold of a prefix of the total order. Importing or merging a foreign log can insert events *before* the checkpoint's position, at which point both ends of the log still look right, the checkpoint's own event is still there, and every inserted event is silently dropped as already folded in when it never was. Nothing looks wrong afterwards. The numbers are just smaller, forever. So the rule is stated once, in `ClarityReplay.canResume`, over two numbers a caller can get from a list or from SQL: the log holds exactly as many events at or before the checkpoint's position as the checkpoint state was folded from, and the event it was taken at is still there. Anything else is a full rebuild, which is always correct.

**Both merge paths throw every checkpoint away regardless**, so forgetting costs a slow cold start rather than a wrong one, and `ClarityRepository.ingestForeignLog` is the only door foreign events come through in either mode.

**Every checkpoint this app stores is a full rebuild from event zero, checked against the state the app is running on**, and a disagreement writes nothing and clears what is stored. A checkpoint taken from the live projection would be one line shorter and would carry any error the projection had picked up, and carry it forever, because the next checkpoint resumes from this one and no later check finds it. **Exactly one checkpoint row survives.** A row per week accumulating forever is the obvious answer and it is the wrong one: nothing reads any checkpoint but the newest, each row is a serialized copy of everything the person owns, and anything that read an older one to say what a past week held would be engine state living outside the log. Past reports are what remain forever, in a different table, for a different reason.

**The cold start path never reads the head of the log to decide whether it needs to read the head of the log.** Three bounded queries answer the rule: one aggregate count, one range scan forward from the checkpoint on the `lamport` index, and one primary key seek. A row this build cannot decode makes the two numbers disagree and sends the load down the rebuild path, which is the conservative direction.

**The export path landed in phase 11 and the debug menu action did not.** `ClarityRepository.exportSnapshot` takes the log and the rebuild under one lock, so the pair inside a backup file is consistent with itself, and it hands `RebuildCheck.matched` out to the export path. `rebuildCacheFromLog` still has no caller. The method both share answers what it found rather than only doing the work. `RebuildCheck.matched` is the correctness check itself: false means the state every screen has been reading disagrees with the log it was supposedly folded from, which is the one defect in this app that cannot be seen by looking.

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

**Built, phase 4, and awaiting the device check that closes it.** Four changes from Addendum 01 landed with it rather than after it: a session ended early is presented as a completed short session rather than as a stopped one, ten minutes can be added to a running session, an optional transition warning fires five minutes out, and the session is visible outside the app as a Live Update. See 14b.5 and 14b.6.

**One thing in the list above is not built, and it is the tone.** A natural completion fires the `focusEnd` haptic and shows the completion state, and it makes no sound at all while the person is looking at the ring. The gentle notification posted when the app is somewhere else does sound, because the Focus channel in 13.4 carries the phone's own notification sound, so the audible half of a completion exists on that path and only on that path. It is the smaller of the two signals this phase owes and did not deliver; the other is the `transitionWarn` haptic in `design-v3.md` 9. Both are recorded on issue #2 rather than left to be noticed.

**Two of the mechanics above can only be confirmed on the phone**, and the phase does not close until they are: killing the app mid session with `adb shell am force-stop` and relaunching restores the ring at the right remaining time, and the back gesture during a session leaves the session running with the ongoing notification and the card countdown both still correct.

---

## 11. HOW TO USE THE LOGIC ENGINE AND THE CORPORA

**This section exists because the engine and the corpora are the easiest part of this project to misuse, and misuse is not detectable by looking at the screen.**

**Layers 1 to 5 are built, as of phase 5, and the Pulse is their first caller, as of phase 6.** What follows describes an engine that exists and that you call, not one you are about to design. Layer 6 is phase 9b and is the last thing built. `CLARITY_LOGIC_ENGINE.md` remains the authority on every layer; 11.7 below is the short version of where the code is.

**The Pulse half of 11.3 is built and every rule in 11.4 held.** Read `domain/pulse/PulseGenerator.kt` before writing the Report's caller: it is the sequence below with its steps numbered in the code, it has no way to write, and the instant arrives as a parameter so that a test can stand on either side of 17:00 and on either side of a daylight saving boundary. What it added to the sequence is one step, numbered 2b rather than hidden, for the re-entry suppression in 14b.4, which postdates the eight steps below.

### 11.1 The one rule everything follows from

**No sentence reaches a screen except by passing through the engine layers in order, and every sentence displayed comes from a corpus file.**

There is no second path. Not for empty states, not for errors, not for edge cases, not for "just this one string." If a screen needs a sentence about the user's data, it asks the engine.

### 11.2 What each corpus is for, and when it is read

**All three files are read once, by `ClarityCatalog.build`, which parses them into families, stages, variants and response pairs.** Nothing else opens a corpus file, and nothing copies a corpus line into Kotlin. What differs per surface is the `Purpose` the engine is asked for, and which part of which file that purpose draws on.

| corpus | reached through | when | produces |
|---|---|---|---|
| `CORPUS_1_PULSE.md` | `Purpose.PULSE` | once on the first app foreground of each local calendar day | one observation, one question, one response set, or nothing |
| `CORPUS_2_REPORT.md` 1, 2, 3 | `REPORT_HEADLINE`, `REPORT_OBSERVATION`, `REPORT_PATTERN` | first open of the Report tab in a new week, and on manual regenerate | one headline, 2 to 4 observations, at most one pattern |
| `CORPUS_2_REPORT.md` 4 | `GuidanceComposer`, layer 6, phase 9b | after the report body exists, once | one plan, one non-plan closing, or nothing |
| `CORPUS_2_REPORT.md` 5, 6 | the catalog's auxiliary benches | same | footer and edge states |
| `CORPUS_3_MOMENTUM.md` | `MOMENTUM_HEADLINE` and `AREAS_BANNER` | on Momentum entry; banner at most once per hour of app use | one headline, or one banner sentence plus one caption |

**Nothing else reads a corpus. Ever.** Widgets read the widget snapshot, which contains sentences the engine already produced. Notifications use fixed strings from `strings.xml`. The tutorial and onboarding use fixed strings.

### 11.3 The invocation sequence, exactly

**Pulse, once per calendar day on first foreground. Built, phase 6**, in `domain/pulse/PulseGenerator.decide`, which carries these eight numbers as comments so that a later edit has to notice which step it is changing:

```
1. Compute dateKey from ClarityClock with an explicit zone
2. If a ClarityPulseEntry exists for dateKey, stop. Display it. It is immutable
3. reflectionPeriod: before 17:00 use yesterday, at or after 17:00 use today so far
4. FactExtractor(queries).extract(window) -> FactSet
5. FiringHistory.from the log: PULSE_GENERATED, REPORT_GENERATED, PLAN_OFFERED. Never DataStore
6. ClarityEngine(catalog, ClarityValidator(zone), zone).observe(facts, history, Purpose.PULSE)
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

**Both are one call.** `observe` runs selection, realization and validation itself, walks down the ranked list when layer 5 vetoes something, and answers `Spoke` or `Silent`. The Report's observation pass is `observeObservations`, which is the same loop with the family exclusion, the incompatibility matrix, the editorial budget and the length band alternation applied across the set.

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

**The simulator in `devtools` exists and runs, as of phase 5.** Eleven synthetic personas, a full simulated year each, every Pulse, Momentum, banner and Report invocation dumped to plain text annotated with the rule that fired, the stage, the register, the variant key and the facts used. Without it you are authoring blind.

Run it through `SimulatorTest`, which builds the catalog from the three committed corpus files, runs every persona and prints the ten checks in `CLARITY_LOGIC_ENGINE.md` 12 with the number each one measured. Four of those checks are enforced and fail the build. Six measure a property that a corpus this size cannot have, and each carries a date and the issue that lifts it. **Judge a batch of authored lines against that dump, never on the page.**

### 11.6 The Pulse response format, settled

**Two options, always, except `quietDay` which needs three.** Do not add a universal third. **Built, phase 6**, and held by a test that walks every stage of every family in `CORPUS_1_PULSE.md` and asserts two, except `quietDay` which asserts three. The pills are stacked rather than set side by side, so the three option case is the same layout as the two option case and neither position reads as a recommendation; `DECISIONS.md` holds the argument.

A third path already exists: **not answering**. Dismissing the sheet is a fully supported state with its own representation, the hollow amber ring in the ambient rhythm row. Never chased, never counted against the user, never mentioned.

`Neither` would produce a response with no signal, which is worse than no response, because it enters the aggregation and dilutes it.

### 11.7 What exists, and how to reach it

Phase 5 built layers 1 to 5 and the simulator. This is where they are.

| you want | you call | in |
|---|---|---|
| the facts for a window | `FactExtractor(queries).extract(window)` | `domain.engine` |
| the catalog | `ClarityCatalog.build(pulseText, reportText, momentumText)` | `domain.engine.catalog` |
| what has been said before | `FiringHistory.from(queries, asOfMillis)`, rebuilt every time | `domain.engine` |
| one sentence for a surface | `ClarityEngine.observe(facts, history, purpose)` | `domain.engine` |
| the Report's 2 to 4 observations | `ClarityEngine.observeObservations(...)` | `domain.engine` |
| a year of output to read | `ClaritySimulator(catalog).runAll()`, then `SimulationDump` | `devtools`, debug only |

**Build the catalog once and hold it.** It parses three markdown files, computes every length band from the realized word count and runs its integrity checks; it is not a per invocation cost. **Build `FiringHistory` every invocation.** It is derived from the log, it merges because the log merges, and caching it is how two devices drift apart silently.

**Four things phase 5 deliberately did not build**, so that nobody looks for them:

- **Layer 6.** `GuidanceComposer`, plans, cues in use, the nominal offer frame. Phase 9b, and `CLARITY_LOGIC_ENGINE.md` 10 calls it the last thing built. `CueFacts` is extracted and confidence gated already, and nothing reads it
- **The Pulse, Momentum and Report screens**, and the generation lifecycle in 11.3. Phases 6, 7 and 8. Phase 5 built the engine those screens call, and no caller. **The Pulse third of this landed in phase 6**, so the lifecycle now has one worked example; Momentum and the Report are still unwritten
- **The corpus at its target size.** Phase 9. The benches are the size they were authored at, which is why six of the ten simulator checks are deferred rather than passing
- **Rules for every family the corpus has language for.** Nine families and three single stages have authored lines and no rule, because the fact their trigger names is not declared in `CLARITY_LOGIC_ENGINE.md` 3.1. Each one is listed in code with the fact it needs and the corpus line that needs it, and a catalog test fails if a family goes quiet without being listed. **Do not approximate one of them.** A near enough criterion fires a family on a shape it does not describe, and the sentence that comes out is arithmetic nobody can fault and a claim that is not true

---

## 12. Pulse, Momentum, Report

### 12.1 Pulse

**Built, phase 6, and awaiting the device check that closes it.** Issue #4.

Once per day, one behavioral observation, one question, two or three answers. Data capture, not advice.

**States** IDLE, READY, PRESENTED, ANSWERED. At most one per calendar day, keyed by `dateKey`. Generated per 11.3, immutable once written. **`PulseDayState.of` is the one definition of those states in the app**, read by the chip's dot, by the rhythm row and by the reminder, so a notification and a dot have no way to disagree about what READY means.

**The sheet.** The observation in readSerif centered, the question in body at textDim, then response pills. After answering, a neutral acknowledgment fades in, then ambient mode: a 14 day rhythm row, today's answered card, and a History entry. Filled amber means answered, a hollow ring means generated but unanswered, faint means a silent day. **Built, phase 6.** Every one of those strings came out of a corpus through the engine and off the event; the response label is stored verbatim on `PULSE_ANSWERED` so that a later callback quotes what the person actually saw rather than what the label says by then.

**Reminders.** When enabled, WorkManager schedules a daily notification at the chosen hour, **posted only if that day's entry exists and is unanswered.** Never posted when IDLE. **Built, phase 6**, as a chain of one time requests that each arm the next rather than as a periodic request, because a period is a duration and this is a wall clock hour. The rule about IDLE is a type rather than a check: the poster takes a token that can only be produced from an entry that exists and is unanswered, so there is no code path that posts on a silent day. **The switch and the hour picker that turn it on landed in phase 11**, under Daily routine, with the contextual `POST_NOTIFICATIONS` request beside the switch and fired on the transition rather than on the value, so a person who never turns the reminder on is never asked. The hour picker is a list of the twenty four hours rather than a clock dial, because the preference stores an hour and the reminder is WorkManager work rather than an exact alarm, so a dial would promise a precision the scheduler is explicit about not having.

**Pending, phase 6.** Pulse generates nothing for the first two days after a return from a long absence, which makes those days IDLE and posts no reminder. See 14b.4. **Built, phase 6**, as step 2b of the sequence in 11.3, ahead of fact extraction so that the facts of an absence are never read at all.

### 12.2 Momentum

**Built, phase 7, and awaiting the device check that closes it.** Issue #5.

The calm daily mirror. **It observes and never interprets.** It must never say because, suggests, or means; that vocabulary belongs to the Report.

1. **Headline** in readSerif, from the engine, under twelve words
2. **Activity row:** `Active X of last 14 days` with the 14 dot grid, today ringed. A rolling window by design. **There is no streak, and a missed day never resets anything**
3. **Area tiles:** one per non-archived area, area color at 60 percent when it has an active item, faint outline when idle
4. **This Week:** three typographic stats, Monday to now, no cards. Unused features render dimmed with a soft discovery line
5. **Insights**, each only when it has data: Area Balance, Completion Pace (8 week sparkline), Focus Patterns (7 day heat strip), Idle Areas (only at 7 or more days inactive, gentle, no red)

**Empty state:** welcoming sentence, empty dots, outlined tiles, dimmed stats. No guilt. **Built, phase 7**, as the same screen with nothing in it rather than a layout of its own, so there is no second arrangement to keep in step and the welcome is whatever the engine says, which for a person with areas and no events is the `cleanSlate` family.

**Which three stats, which this section does not name.** Completions, minutes focused and items added, in that order. **Built, phase 7**, and the order is the choice rather than the obvious one: the lifecycle order opens the screen on a number that goes up every time somebody has an idea, and a fourth axis, areas active, would repeat what the tiles above already say. Recorded in `DECISIONS.md`.

**The banner is throttled in the ViewModel and never in the engine**, per 11.2, and the surface it sits on is the Areas screen rather than this one, `design-v3.md` 10.2. **Built, phase 7**, as an hour measured in app use by a ViewModel resolved against the Activity's store, so a tab switch does not reset it and a recomposition does not recompute a sentence about the shape of a week.

**There is no streak here and the absence is structural rather than remembered.** `FactSet` declares no streak fact, the dot row is handed fourteen independent days, and nothing on the way out of the composer can answer whether two active days were adjacent. `MomentumComposerTest` asserts the count is the size of a set and that a missed day resets nothing, and `MomentumLanguageTest` asserts no line in `CORPUS_3_MOMENTUM.md` reaching this screen or the banner contains a causal construction, per `CLARITY_LOGIC_ENGINE.md` 6.5.

**Pending, phase 9.** The first weeks half of 14b.10. An insight with too little behind it is absent rather than drawn empty, which is that item's `never an empty chart` and is built. The other half, a line saying plainly what the module needs and roughly when it becomes useful, is a corpus line by 11.1 and the edge state benches do not carry one yet. Phase 7 wrote none, because phase 9 grows the corpus and it goes to the owner.

### 12.3 Report

**Built, phase 8, and awaiting the device check and one write that closes it.** Issue #6. The write is named under Cadence below and it is the whole of what stands between this section and shipped.

**Prime directive: data integrity.** Every claim traces to a specific query with a non-zero result.

**Hard integrity rules, built as code and not conventions.** Before any area is named it must have at least one event in the window. Before any number is stated it must come from an actual count query. New areas with no activity, archived areas and deleted areas are never mentioned. The validation layer vetoes any sentence failing these, and **the veto path must be reachable in unit tests.**

**Built, phase 8, as a second validator at the scale of a page.** `ClarityValidator` checks one sentence against the facts it claims, which is the whole defense the Pulse needs and half the one this screen needs: a report says eight to ten things at once, and two true sentences can still contradict each other, and one fact can still render two different numbers. `ReportIntegrity` holds the nine report scope checks of `CLARITY_LOGIC_ENGINE.md` 9, including 9.2's map of every rendered numeric slot in the whole report against the `FactRef` behind it. **The whole report is vetoed rather than the offending line**, because two numbers disagreeing about one fact means the fact was computed twice and nothing on the page can say which computation was the good one. `ReportIntegrityVetoTest` has a test per check that builds a report violating exactly that check and asserts the veto, and a first test asserting the fixture passes everything, so a veto below it means something.

**Window and cadence.** Trailing 7 days ending today, recalculated on every generation. Generated automatically on first open in a new week (Sunday start), regenerable at any time. Past weeks remain forever.

**The window is the seven completed days before today, and the week and the window are two different questions.** **Built, phase 8.** `ending today` is read as `[startOfDay(today - 7), startOfDay(today))`, because 11.1 draws the week as seven marks scaled against the busiest and a day three hours old drawn at full width beside six whole ones is a claim about a day that is not over. The cadence is a calendar question and the window is not, so a report is due when no `REPORT_GENERATED` event has been written since local midnight on the Sunday that begins this week, asked of the log rather than of a stored flag. Keying the cadence on the window's first day instead would give a person who opened the app on Wednesday and again on Friday two reports in one week.

**Owed, and it is one method.** 11.3 step 9 writes `REPORT_GENERATED`, and `ClarityRepository`, which is the only writer in the app, has no method for it. Until it does, three things are true and none of them is a design decision: the cadence question is asked correctly and always answers yes, so the report composes on every open; `FiringHistory` never learns what the Report said, so the ninety day variant exclusion and the fourteen day family cooldowns cannot vary it week to week; and past reports are empty, because the History page reads the projection and the projection is fed by the log. The report is deterministic either way, so a person sees the same page rather than a changing one.

**Structure** per `design-v3.md` 11.1: controls, eyebrow, headline, the week ribbon, gold rule, sections under sentence-case sideheads, the pattern section as the single grid break, the closing line with accept and decline, footer. **Built, phase 8.** The closing line's block is built and always empty, because a closing line is layer six and layer six is phase 9b.

**Edge cases.** A brand new user gets `Your first week` with whatever is honest. All areas empty and no activity shows the styled empty state with no generated observations. Intent-qualified insights require 3 or more answered pulses in the window; below that the report is trail data only. **All three built, phase 8**, out of `CORPUS_2_REPORT.md` 6.1 and 6.2 through the same selection, rendering and validation every other sentence takes. **A fourth state was added and it is not an edge case of the third:** a report the integrity layer refused shows neither the report nor the empty state, because `Nothing to report yet` is a true sentence about a week in which nothing happened and a false one about a week the app could not prove its arithmetic for.

**Pending, phase 9.** One of the four changes from Addendum 01 that reach this screen: every section that needs history says plainly what it needs and roughly when it becomes useful, rather than showing a zero (14b.10). The section is omitted rather than drawn empty today, which is that item's `never an empty chart`; the sentence that replaces it is a corpus line by 11.1 and the edge state benches do not carry one yet.

**Carried, after phase 8 did not. Three Addendum 01 items build order 19 gave phase 8**, all three of them engine work rather than screen work. For a full week after a return from a long absence, every decline, neglect and gap family is unavailable to selection (14b.4). A dip that has a precedent in the user's own history is a rhythm and not a decline, and the two speak differently (14b.9). Estimate observations may state a ratio or a tendency and may never state a delta (14b.8). **All three now have a fact behind them in `domain.engine.facts`, a gate or a veto reading it, and a test**, and what remains of all three is language: the estimate observation family and the `familiarDip` bench are phase 9's.

**Controls.** History (past reports by week), regenerate (spinner on the headline block, near instant), copy (plain text to the clipboard). The copy control is the app's only integration surface with anything else. **Built, phase 8**, and **the spinner is a shimmer**, because `design-v3.md` 8.2 item 22 says placeholder shimmer and never a spinner and the authority order gives the look to that document. It is on the headline block and nothing else, and the rest of the page does not move.

---

## 13. Onboarding, Tutorial, Widgets, Notifications, Shortcuts

### 13.1 Onboarding

Four beats, runs once, replayable from Settings. Entirely Contemplative. A persistent nav overlay: back chevron (hidden on beat 1) at 35 percent white, an 80dp progress line filling by beat, and `Jump in` in `textDim`, always visible. **It was 30 percent white**, which the phase 13 contrast audit measured at 2.643 to one against `design-v3.md` 13's floor of 4.5, and `design-v3.md` wins on anything visual. The back chevron stays at 35 percent, where it measures 3.133 against the 3.0 a graphic carries. Tap or swipe left advances, swipe right goes back.

**Beat 1, See It Work.** About 9 seconds, auto advances. Four colored demo cards enter with staggered three-part entrances. The top card's item strikes through and completes; the next queued title slides up and takes its place. One sentence: `One thing at a time. The next one is ready when you are.` **This beat must land the whole model in five seconds.**

**Beat 2, Your Areas.** The user picks two to four starter areas. Suggestion chips (Work, Personal, Health, Family, Learning, Side Project) plus a custom field. Each selection shows a mini card and opens the mood color rows. **Selections are transient in-memory structs; nothing is written until beat 3.**

**Beat 3, The Reveal.** Selected areas are written as real events, then an iris-open transition uncovers the user's actual Areas screen rendered live behind the overlay, with the closing line fading in and out: `Your clarity starts here.`

**Beat 4, The Depth.** About 20 to 25 seconds, four auto-paced moments, tap to advance. Moment 1, philosophy on black: `You just organized your focus.` then `Now let the app learn how you work.` Moment 2, Pulse: amber glow, a sample observation card with two response pills, caption `One question a day. One tap.` Moment 3, Momentum: blue glow, the 14 dot row fills day by day, caption `Your rhythm, without the guilt.` Moment 4, Report: gold glow, a miniature report headline, caption `Every week, an honest mirror. Written on your device.`

Then it opens the app with the tutorial queued. **There is no paywall beat and no sheet at the end.**

**Built, phase 10.** All four beats, the nav overlay, the swipes and the back behavior, in `ui/onboarding/`. Beat 2 gained the `Just start` path of equal standing and beat 4 gained the line announcing Pulse before it ever appears, both per 14b.11.

**Three things this phase settled that 13.1 left open.** Beat 2 opens as a **fork rather than as a form**: two stacked panels of identical width, surface and type, with `Just start` on top, because equal standing is a property of the arrangement and vertical position is the only weight left once the treatment is identical. Beat 4's fourth moment **does not auto advance**, because a line that announces a behavior and is then replaced on a timer is the unannounced behavior 14b.11 objects to, one level up. And beat 3 writes `hasCompletedOnboarding` **at the reveal rather than at the end of beat 4**, which is what makes the force quit rule below true; beat 4 is depth, and depth is not setup. All three are recorded in the files that make them and in `docs/BUILD_STATE.md`.

**The routing is one composable, `ui/onboarding/FirstRunGate.kt`,** which reads the two flags in the order 10.15 states, latches the answer for the process and composes the app underneath onboarding from the moment the reveal begins. Latching is what lets beat 3 write its flag without the route recomputing itself out from under the iris. It has one consequence a session should know rather than rediscover: `Replay the welcome`, `Replay the tour` and the return to onboarding after an erase all take effect on the **next cold start**, because the decision this process took is not revisited.

### 13.2 Tutorial

Five spotlight steps on first arrival at Areas. A full screen overlay above everything including the tab bar: 56 percent black radial dim, a feathered cutout, a slowly pulsing 2dp white ring at 38 percent, a floating tooltip in surfaceRaised with a step indicator. Skip always visible top right. Tap anywhere advances.

**Implementation:** targets report bounds via `onGloballyPositioned` keyed by stable test tags. **One uniform mechanism for every step, no per-step special cases.** This was a hard-won lesson in the iOS build; do not mix strategies.

**Steps:** 1 the FAB, 2 an area card, 3 the Focus chip, 4 the Pulse chip, 5 the tab bar.

**Built, phase 10, and not yet reachable.** `ui/tutorial/` holds the whole mechanism and it is the uniform one this section asks for: `TutorialStep` is the only place the order and the copy live, `Modifier.tutorialTarget` applies the stable test tag and reports `boundsInRoot`, `TutorialTargets` holds the rectangles, and `TutorialOverlay` derives the dim, the cutout, the ring and the tooltip side from the rectangle alone with **no branch on which step is drawing**. `ClarityShell` provides the registry and composes the overlay last, after the tab bar and the Focus surface, which is the whole of the "above everything" guarantee.

**Four of the five call sites are missing and the tutorial therefore never starts.** Only the tab bar wears `tutorialTarget`; the FAB, the first area card, the Focus chip and the Pulse chip do not, and `TutorialTargets.ready` requires all five. That is the designed failure rather than a silent one: with a target missing the tutorial does not run, `hasSeenTutorial` is not written, and it runs correctly on the first launch after the modifier is added. The remedy is one modifier in each of the four chains in `ui/areas/`. Recorded in `docs/BUILD_STATE.md` and on issue #9.

### 13.3 Widgets

**Widgets matter more than notifications for this audience, and the reason is specific.** A widget is persistent and cannot be dismissed, so it works with out of sight, out of mind rather than against it. A notification is a one time event that is swiped away and forgotten. A widget is still there tomorrow. Eight are specified below and **six are required in v1**. This section was rewritten by Addendum 01; DECISIONS.md C4 records what it replaced and why.

The goal for every one of them is **zero taps to see**, and where an action exists, **one tap to act**.

**Where the data comes from.** The widget snapshot written to DataStore on every meaningful change, plus a WorkManager refresh every 6 hours. **Widgets never read a corpus and never run the engine.** Any sentence a widget shows was produced by the engine, written into the snapshot, and is repeated verbatim, per 11.2. A widget that composed its own sentence would be a second path to the screen, which 11.1 does not allow, and it would be the one path with no validator on it.

**Rules every widget follows.** Built with `androidx.glance`. Deep links open the right surface, except while a focus session is running, when any widget tap goes to the focus screen. If a configured area was deleted or archived, show a reconfigure prompt. Each one renders correctly in dark mode, honors calm mode, scales text without clipping at its smallest grid size, and shows a sensible state when it has nothing to show. Each one is usable with TalkBack and carries real content descriptions rather than a repeated label. **Every preview image in the widget picker is generated from the real widget and never from a mockup**, which is the same rule 16.6 applies to the README screenshots and for the same reason. The shared visual DNA is in `design-v3.md` 12; this section is the behavior.

**Required in v1. All six built, phase 12**, and described as built at the foot of this section.

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

**Built, phase 12.** All six, in `widget/`, on `androidx.glance`, over one snapshot in
`data/widget/`. The snapshot is written by a collector `ClarityApp` installs, which
follows the projection and rewrites only when the content changed, plus
`WidgetRefreshWorker` every six hours. `Next Up`, `First Step` and `All Areas` each name
an optional configuration screen, so a widget is placed and drawn on the automatic area
immediately and pinned to one afterward if the person wants that. An area archived or
deleted under a widget is told apart from the other by the snapshot and named, rather
than left stale or blank.

**The boundary this phase was most likely to lose is intact.** `ClarityWidgetSnapshot`
carries facts and finished sentences and nothing that could be realized on the far side:
no key, no family, no stage, no fact reference. **No widget reads a corpus and no widget
calls the engine**, which is 11.1's one path, and the accepted plan line the deferred One
Thing widget will need is carried whole rather than as the four keys it was built from.

**One thing it owes.** **No widget has a preview image**, so the picker shows the
loading layout: `design-v3.md` 12.1 requires one generated from the real widget, a hand
written preview layout is exactly the mockup that rule forbids, and each resource file
says so at the place the attribute would go. It is a device capture and it is in
`HANDOFF.md`.

**The routing gap it also owed is closed.** All six actions land on their surface:
`ui/nav/ExternalRequest.kt` holds one pure table from an action string to a destination,
`MainActivity` notes a request from both `onCreate` and `onNewIntent`, and the shell
dispatches it in a `LaunchedEffect` that also fires on first composition, so a cold start
and a warm start behave identically. That was the failure mode worth designing against:
half of these arrive at one entry point and half at the other, and a route that works
from one is the classic version of this bug.

Two of the six needed real plumbing rather than a branch. Opening one area's sheet by id
waits on the projection, because a sheet opened against a log that has not finished
loading dismisses itself. Starting a session on one item joins the log load and the
session restore before it writes, because without the join it works warm and is silently
refused cold. The serial that makes a repeated tap a new request is compared for
difference rather than for order, since the shell's counter restarts at zero after
process death while the saved mark comes back at three, and a greater-than would swallow
the first request of every new process.

**The gap phase 6 left behind for the Pulse reminder is closed with them.** A test scans
all three intent source files for every `ACTION_` constant and requires each to be routed
or listed as broadcast only, so a seventh action added later fails on the commit that
adds it rather than in a picker six months on.

### 13.4 Notifications

Channels: **Focus** (session complete, default importance, gentle sound), **Reminders** (Pulse reminder), **Ongoing** (silent, the running session chronometer).

Request `POST_NOTIFICATIONS` contextually, the first time the user starts a focus session or enables a reminder. **Never at launch.** Every notification deep links correctly. **No marketing notifications, ever. No re-engagement notifications, ever. Nothing that exists to pull the user back.**

**Built, phase 4.** One promoted notification joined these, the focus session Live Update, on the Ongoing channel and under the same rules: silent, dismissed when the session ends, never re-engaging. It needs `POST_PROMOTED_NOTIFICATIONS`, which is a notifications permission and therefore inside what section 18 allows, and which the no internet gate does not match. See 14b.6.

**All three channels are created at process start, in one place**, and creating them posts nothing, alerts nobody and needs no permission. A channel's importance and sound are fixed for an install the moment it is created and cannot be raised afterwards, so a channel created late by whichever code path happened to need it is a channel whose settings were chosen by that code path. Reminders therefore exists from phase 4 with nothing behind it until phase 6, which is the price of the rule and is the smaller cost. If a later phase needs different settings on a channel it changes the id, deletes the old one and creates the new one, which is the platform's only escape from that immutability.

### 13.5 App shortcuts and the quick settings tile

**Both are built, phase 12,** both are new in Addendum 01, and both supersede the line in section 18 that put home screen shortcuts out of scope for v1. The reasoning is the widgets' reasoning: fewer steps between an intention and an action.

**Three static shortcuts** on a long press of the app icon, built with the androidx.core shortcuts APIs: `Quick capture`, `Start focus`, `Today's Pulse`. Each opens the destination its matching widget opens, so quick capture lands in the unfiled inbox and today's Pulse opens the Pulse surface in whatever state it is in, including its ambient state on a day the engine stayed silent. **Static, not dynamic.** A shortcut list that reordered itself around what the user did most would be a measurement of the user, and it would be one they never asked for.

**One quick settings tile** that starts or ends a focus session from the notification shade, on the platform `TileService`, reflecting live session state. Tapping it with no active item anywhere opens the chooser rather than failing, which is the same degradation the Focus chip already performs in section 10.

**Built, phase 12.** The shortcuts are `res/xml/shortcuts.xml` and one `meta-data` line
on the launcher activity, with nothing behind them at runtime: they are published by the
system from the manifest, so no code in this app creates them, ranks them or reports that
one was used. `ShortcutManagerCompat.reportShortcutUsed` is never called and a test
asserts it never will be, because that call exists to feed a launcher's ranking and a
usage report is a usage record. Each shortcut sends the action the matching widget or
notification already sends, `ShortcutContractTest` asserts the three strings in the
resource still equal the three constants they were copied from, and the package inside
each intent comes from a generated string resource because `${applicationId}` does not
reach a resource file and a literal would be three dead shortcuts on every debug install.

The tile is `tile/FocusTileService.kt`. It reads
`ClarityRepository.runningFocusSession`, which is the persisted handle resolved against
the log and the same flow the Focus surface and the ongoing notification read, so the
three cannot disagree. Ending goes to the receiver the Live Update's `End` action already
reaches, which is one more caller of one path rather than a second way to end a session,
and an early end stays a completed short session per 14b.5. Both halves run behind
`unlockAndRun`, so a locked phone asks for the passcode first and neither half has a
different rule from the other.

**Two open choices, decided here rather than left implicit.** `Start focus`, on the
shortcut and on the tile, **opens the Focus surface rather than starting a session**,
even when exactly one area has an active item and the choice would be unambiguous. A
session is a row in a log that only ever gains rows, the person has not seen a screen
yet, and the First Step widget already answers the same question the same way, so all
three launcher surfaces say one thing. With a session already running the same action
opens that session, so neither surface can start a second one. And **the tile carries no
subtitle and no countdown**: the shade is readable over a lock screen and what somebody
is working on is not for a passer by, while a number that only moves when the shade opens
is worse than no number at all.

**What the shortcuts still wait on is `MainActivity`.** `Start focus` works today because
that Activity routes `ACTION_OPEN_FOCUS`. `Quick capture` and `Today's Pulse` send
actions nothing routes yet, so both open the app at whatever tab it was left on. It is
the same missing call the six widgets wait on, 13.3 names it, and it is two predicates
that already exist.

---

## 14. Settings and About

### 14.1 Settings

A Daylight screen, rows on canvas under sentence-case sideheads, **no card containers.**

**Daily routine.** `Daily reflection` explainer row opening a short sheet describing Pulse. `Pulse reminder` toggle. `Remind me at` time picker, shown only when the toggle is on.

**Focus.** `Highlight the active session` toggle. `Session length` selector.

**After completing.** Segmented choice: `Promote next` versus `Choose from queue`, with an explanatory line reflecting the current selection.

**Appearance.** Three real-miniature tiles per `design-v3.md` 10.10. System is the default. A line below noting that Focus, Pulse and Report are always dark by design.

**Your data.** `Export everything`, one JSON file via the Storage Access Framework, showing the last export date. `Import from a file`, validating schema version and integrity before a transactional full replace with a typed confirmation. `Erase all data`, per 14.2.

**Built, phase 11**, in `ui/settings/`. Every group above in the order above, and with it the four rows that were waiting on this screen to exist. Under **Focus**, `Session length` with the eight options in section 10 and `Five minute warning` off by default, whose preferences phase 4 already read and honored (14b.5). Under **Appearance**, `Calm mode`, still following the OS reduce motion setting until it is touched, whose transform phase 3b already applied everywhere (14b.12). Under **Daily routine**, the reminder switch and its hour picker, whose preferences phase 6 already read, with the contextual `POST_NOTIFICATIONS` request beside the switch and fired on the transition rather than on the value, so a person who leaves the default alone is never asked (13.4). Under **Your data**, everything 14b.7 requires: the optional password, the plain statement about a readable file, full pre validation, replace or merge, and the quiet line when the last export is older than 30 days and real data exists.

**Two things on this screen are not what they should be, and both are recorded rather than worked around.** Settings is hosted from inside the Areas tab rather than from `ClarityShell`, so it does not cover the floating tab bar the way 10.15 makes a pushed screen cover it; it reserves room at the foot instead, and the remedy is one branch in the shell beside the Focus surface. And **changing `After completing` does not write `SETTING_CHANGED`**: the event type, the reducer fold and the Trail row all exist, but `ClarityRepository.commit` is private and no method on the only writer in the app takes an arbitrary setting change, so the event is missing rather than written down a second path. Both are on issue #10 and in `docs/BUILD_STATE.md`.

**Privacy.** `Privacy policy` opening the in-app sheet with the text in 14.3. `Open source licenses` listing AGPL-3.0 for the app, SIL OFL for Newsreader and Hanken Grotesk, Apache 2.0 for Material Symbols and AndroidX. Then the permission card:

> **This app has no internet permission.**
> Not a promise, a fact your phone enforces. Clarity Now cannot open a network connection even if it wanted to. Check it in Android settings.

**Help.** `Replay the tour`. `Replay the welcome`. `About Clarity Now`.

**Then the support block** per 14.5, then the version line: `Clarity Now by Kamsiob · [version]` and `AGPL-3.0 · free and open source`.

### 14.2 Erase all data

A sheet, not a dialog. Serif heading `Erase everything?`, two paragraphs stating exactly what is removed and that it cannot be undone, a nudge to export first, then a field requiring the user to type `ERASE`. The destructive button stays inert grey until the text matches. `Keep my data` sits below it.

On confirmation: wipe the event log, every cache table, every checkpoint, and all DataStore keys except a freshly regenerated `originId`. Return to onboarding. The reset virginity test in 6.5 covers this.

**Built, phase 11.** The sheet is `ui/settings/SettingsSheets.kt`, the wipe is `ClarityRepository.eraseEverything` and `ClarityPreferences.eraseEverything`, and the typed word lives in a plain `remember` so that leaving the sheet discards it with nothing to clear by hand. The button is the `DESTRUCTIVE` treatment in `design-v3.md` 10.7, inert grey until the text matches and **never red**.

**Two things worth knowing before reading this as closed.** The typed word is matched **case insensitively**, because every field in this app sets sentence capitalization and an exact match would make this the one control that cannot be operated without finding caps lock; the gate is that five specific letters were typed on purpose, and they were either way. And the return to onboarding happens on the **next cold start**, because the first run gate latches its decision for the process, per 13.1. The erase itself is immediate.

**The virginity promise is proved in two halves, because it spans two stores that both need a device.** `ReplayHarnessTest` folds an emptied log and asserts a virgin state, which is the half a unit test can reach. `EraseContractTest` reads the source of both erase methods and asserts that preferences are cleared with `clear()` rather than with a list of `remove` calls, which is the failure this guards against: a list is a list somebody adds a key without.

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

**Shipped verbatim, phase 11.** The heading, the seven leads and the seven bodies are in `strings.xml` as `privacy_heading` and `privacy_lead_2` through `privacy_body_7`, checked word for word against the text above. The sheet renders them and composes nothing. **The builder still has to publish the identical text at a URL under kamsiob.com**, which is a phase 13 task and is not made done by the app carrying it.

**Play Console Data Safety declarations for the builder to enter:** no data collected, no data shared, data is not encrypted in transit because there is no transit, users can delete their data in app, no data types apply. No account, so the account deletion URL requirement does not apply. Contains no ads.

### 14.4 About

App mark at 62dp on `#141A2E`, name in displayTitle, `Version [x] · by Kamsiob`, then one paragraph in bodySerif:

> One active item per area. Everything else waits its turn. No account, no cloud, no subscription, no ads, and nothing collected. Your history lives in one file on this phone until you delete it.

**Pending, phase 13, and deliberately absent from the shipped screen.** One sentence sits under that paragraph, required verbatim by 16.11 and in the store listing at the same time: `Clarity Now is a productivity tool. It does not provide medical advice, diagnosis or treatment.` Phase 11 built the screen without it rather than early, because shipping it now would put the sentence in the app before the listing it has to match. `ui/about/AboutScreen.kt` says so at the place it goes.

**Then a quiet link list under an `Elsewhere` sidehead.** Each row: an outlined icon at 50 percent opacity, the label, and the destination trailing in caption inkTertiary.

| label | trailing | destination |
|---|---|---|
| YouTube | `@kamsiob` | https://youtube.com/@kamsiob |
| Source code | `github.com/kamsiob` | https://github.com/kamsiob/clarity-now |
| Website | `kamsiob.com` | https://kamsiob.com |
| Kamsiob Lab | `telegram` | https://t.me/+g5LKm9rUnNcxMjk5 |
| Feedback | `hello@kamsiob.com` | mailto:hello@kamsiob.com |

Links are findable but visually subordinate. Then the support block, then license lines.

**Built, phase 11**, in `ui/about/`. All five rows with the labels, trailing values and destinations in the table above. The destinations are Kotlin constants rather than string resources, which matters for exactly one of them: 14.5 forbids any coffee or caffeine reference in the support block's label or body, the URL it opens contains one, and a constant is somewhere a `stringResource` call cannot reach it. The `Elsewhere` rows carry no accent and no chevron, because a chevron in this design means a screen inside the app and every one of these leaves it.

### 14.5 The support block

Appears at the bottom of **Settings and About only.** A rounded card with a warm parchment gradient and no border.

- Heading `Support this work` with a small outlined heart icon in `#B45309`
- Body: *Built and carried by one person. If software made this way matters to you, there is a place to stand behind it. Either way, it is yours.*
- Button: filled `#B45309`, label exactly `Support this work`, opening https://buymeacoffee.com/kamsiob

**Copy rules, absolute.** No coffee or caffeine references anywhere, in the label or the body. No framing anchoring support to a small amount. No begging, no urgency, no counter, no goal bar, no `if you enjoy`, no exclamation marks. Never a dialog, never an interstitial, never after completing a task, never more than these two placements.

**Built, phase 11**, as `ui/about/SupportBlock.kt`. **There are exactly two call sites**, the foot of `SettingsScreen` and the foot of `AboutScreen`, and that count is the whole of how the placement rule stays true: a third call site is a grep away from being visible, and nothing else in the app imports this composable.

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

**Detection built in phase 3b. The two engine side consequences built in phase 6. The surface itself is not built, and has no phase.** From Addendum 01 4d.

**Read that again before building anything near this section.** Phase 6 was the phase that owned the surface and phase 6 did not carry it. What phase 6 did build is the pair of rules that follow the screen: the Pulse writes nothing for the first two days back, as step 2b of the sequence in 11.3, and `TrailQueries.lastReEntryOnOrBefore` is now called in anger for the first time. What does not exist is the screen described below: the two choices, the demotion by `ITEM_QUEUED`, the once per gap dismissal, the Daylight treatment in `design-v3.md` 11.2. **A person who comes back after a fortnight today sees the ordinary Areas screen and a quiet Pulse**, which is not wrong and is not what this section asks for.

The consequence worth stating plainly: the suppression rules exist without the screen they were written to protect, so nothing today can greet a returning person with a measurement of their absence, and nothing greets them at all. **Assigning it a phase is the owner's call** and `docs/BUILD_STATE.md` records it as open. **Phase 10 has now closed without it**, which removes one of the two candidates: it built the cold start gate in `ui/onboarding/FirstRunGate.kt` and left the place for this route marked, after both flags, exactly where 10.15 puts it, but it did not build the screen. What remains is a phase of its own, because it is one screen and it is finished when it works.

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
- **The Report suppresses every decline, neglect and gap observation for a full week back.** For seven days from the re-entry date every rule in those families is unavailable to selection and the next ranked candidate is taken instead. **Built.** `HistoryFacts.isJustBackFromAbsence` is true on the day of the return and for the six days after it, asked of the last day the window describes, and it is a boolean precisely so that it carries neither the date nor the length. `FamilyAvailability.WITHHELD_ON_RE_ENTRY` is the set of families and the reason for each, applied at step 1b of selection, on the Report, the Momentum headline and the Areas banner, which read from the same catalog. The report is shorter when nothing else qualifies, because 11.4 forbids padding a section to reach a minimum, and `FamilyAvailabilityTest` asserts a week where the one qualifying observation was a reading of the absence and the section came out empty. **Measured over the simulated year, sixth measurement.** `longDormantRevival` returns on day 251 after 195 days away and receives 22 sentences in the seven days that follow, none of them a decline, a neglect or a gap. The same seven days with this withholding disabled produce seven: `A still fortnight.` on the Momentum headline on four separate days, and a report headlined `Work moved again.` carrying `Work had been the quietest area. It was not this week.` and `6 events. All week.` **The set is not only the decline and neglect families**, and the half a reader will not expect is the gap families: `mo.come.01` is `Back after {ageDays}` and `ob.rev.l01` is `{areaName} moved again after {ageDays} of nothing`, both warm, both true, and both stating the length of the absence in days on the first screen back. **It is not applied to the Pulse**, which has the older and stronger rule two bullets up: it declines to run the engine at all for two days rather than withholding some of its families
- **It never appears alongside the tutorial, a conflict card, or anything else that wants the first moment.** It is the first screen and it is alone. A conflict card from 6.3 waits behind it rather than being dropped, because a conflict is never silent

**A returning user must never be greeted by a measurement of their absence.** If a sentence, a number, a dot row or an empty chart on the first screen back can be read as a report on how long they were gone, it is wrong, whatever else is true about it.

### 14b.5 Focus sessions: ending early, adding time, the transition warning

**Built, phase 4, except where this section says otherwise.** From Addendum 01 4e, 4f and 4g. Section 10 is otherwise unchanged.

**Ending early is a success state.** A session ended early is a **completed short session**, and the completion screen says so in the same shape a natural completion uses, with the same actions. Fourteen minutes is fourteen minutes. The rule in 10 that discards a session under 60 seconds silently stands, because that is a mis-tap rather than a short session.

**The word `abandoned` appears nowhere a person can see it**, including the Trail, every accessibility label, and the export file in 14b.7. The Trail already reads `Stopped after N minutes`. The event type itself was renamed from `FOCUS_ABANDONED` to `FOCUS_ENDED_EARLY`, because a raw type name is visible in a readable export and, more importantly, in `docs/EVENT_FORMAT.md`, which a second implementation is built from. DECISIONS.md C6.

**Adding time.** An `Add 10 minutes` control extends the running session without resetting it and without starting a new one. It writes `FOCUS_EXTENDED` with the added seconds and the new planned total, and the persisted end timestamp is recomputed so the session still survives process death per section 10. It is repeatable and uncapped. The reducer folds extensions, so a session's planned duration is the newest `newPlannedSeconds` rather than the value in `FOCUS_STARTED`, and every later reader, the completion path, the Trail, the engine and the widget, reads the folded value. **Ending a timer should not have to break flow.** It is reachable from the focus screen, from the Live Update and from the ongoing notification.

**The transition warning is optional and off by default.** A quiet five minutes left signal before a session ends, controlled in Settings. **Never a full notification unless the app is backgrounded.** It does not fire when fewer than five minutes remain at the moment the session starts or the moment it is switched on, which would make it fire immediately and teach the person to distrust it. Switching from one task to another is the expensive act for this audience, and a warning is the difference between a transition and an interruption. It is off by default because an unannounced signal is also an interruption.

**What of it is built, stated plainly because the halves shipped apart.** The mark on the ring track, the tick brightening when the arc reaches it, the word beneath the numeral changing to `5 minutes left` and staying, the point on the Live Update track, the silent notification when the app is elsewhere with no Live Update, and the arming rule that makes an extension re-arm the signal exactly once are all built, phase 4. **The haptic is not.** `design-v3.md` 9 gives the moment one `transitionWarn` event, `ClarityHaptics` does not carry it yet, and nothing collects the in app signal the notifications layer publishes. The setting is stored and defaults to false; its Settings row landed in phase 11, under Focus, per 14.1. Recorded on issue #30.

### 14b.6 The Live Update

**Built, phase 4, extended in phase 12.** From Addendum 01 Step 5. The surface itself is specified in `design-v3.md`.

A focus session is exactly the user initiated, start to end, time bound task that Android's Live Updates exist for. On a Pixel it surfaces as a status bar chip that expands, and on Samsung devices in the Now Bar. **For an audience with time blindness, the session being visible outside the app is not a nicety, it is the point.**

**Use the platform API, not a custom notification dressed to look like one.** `Notification.ProgressStyle`, introduced in Android 16, API 36. Declare `POST_PROMOTED_NOTIFICATIONS`. Check `NotificationManager.canPostPromotedNotifications()` before posting and degrade silently when it is false. Verify the current API details before implementing, per 3.3.

**What it shows.** The area name, the item title, and the remaining time as a depleting track. A single track is the likely right answer; use segments or points only if they genuinely add clarity. When the transition warning is enabled, the track changes state at the five minute mark.

**Actions, two at most.** `Add 10 min` and `End`. Both work without opening the app. Tapping the body opens the focus screen.

**Degradation is required, not optional.** Below API 36, or where promoted notifications are unavailable or denied, fall back to the ongoing notification with a chronometer that section 10 already specifies. The app is fully usable with no Live Update at all. **Never gate a feature behind it and never tell the user their device is missing out.**

**How the built version answers the availability question**, since 3.3 asks for the platform to be verified rather than trusted. `NotificationManagerCompat.canPostPromotedNotifications` on androidx core 1.19.0 answers both cases in one call: it returns false below API 36 without touching the platform and asks the platform above it. It is checked before every post rather than once, because promotion can be revoked in system settings while a session is running. Both renderings carry one notification id, so a revocation mid session changes the notification a person is already looking at rather than adding a second one. The device this project builds for runs API 36 or later; every device below it takes the chronometer path, which is why that path is the base and the promoted half is the branch.

**This is the only Live Update the app will ever post.** Not for Pulse, not for the Report, not for a reminder, not for anything the user did not just start. It is silent, it is dismissed when the session ends, it never re-engages, and it is not a marketing surface.

### 14b.7 Backup, export and import, as a safety feature

**Built, phase 11.** From Addendum 01 4h. This replaces the two sentence description of export and import in 14.1 with the requirements they have to meet.

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

**Both are built and both run on a plain JVM**, `BackupRoundTripTest` and `BackupRefusalTest` in `data/export/`. The refusal suite carries all six required cases and eight more. What makes them honest is `ClarityBackupStore`, a three method interface that `ClarityRepository` implements and `data.export` is written against: a test that claims the database was untouched has to be able to watch for the write, Room needs a device, and three methods a fake can implement is what puts that assertion on a unit test rather than in a comment. Nothing in `data.export` can reach the database except through those three, which is the other half of "validate before touching anything": the validating half does not hold one.

**How the phase built it, and the four choices inside that.** `BackupCodec` reads and writes and cannot write to the database at all, and `BackupService.apply` takes a type only a passing validation can produce, so there is no path into the database that skipped a check. The **file is two parts**, one header line and then the body, rather than one nested JSON document, because a checksum over exact bytes is reproducible by a second implementation while a checksum over a nested object is a checksum over somebody's canonical serialization. The **KDF is scrypt written out in the repository** against the RFC 7914 vectors, because 3.3's check on August 27, 2026 put Argon2id first and scrypt second and Android's providers offer neither, and because scrypt is in the standard library of every runtime the Linux companion in `docs/EVENT_FORMAT.md` could be written in. Its **parameters are OWASP's third equivalent row, N=2^15, r=8, p=3**, rather than the first: the first wants 128 MiB in one allocation, and a backup that cannot be opened on the phone holding the only copy is worse than one of five settings the same reference calls equivalent. **Every parameter travels in the file**, so a later build can raise them without making one earlier backup unreadable. And a **recorded projection that disagrees with this build's own fold is reported rather than refused**, because the log is the truth and refusing somebody their own history over a derived number would be the failure this feature exists to prevent.

**The reminder, and its limits.** Settings shows the date of the last export, from `lastExportAt`. If more than 30 days have passed **and** real data exists, one quiet line appears **in Settings only**. Never a notification, never a nag, never a badge, never a card on Areas.

**Built as `data/export/ExportReminder.kt`, one pure function with one caller**, which is what keeps the placement rule checkable: one place to read is one place a second surface would have to be added, and it would be visible. Real data means **at least one item the person has not deleted**, not an area, because an area is a name and a color and an item is a thought they had once. When nothing has ever been exported the rule measures from **the oldest item they still have**, because most people never export anything and a rule anchored only on the last export would be silent forever for exactly the people it exists for.

### 14b.8 Estimates are calibration, never error

**The facts, the veto and the floor are built and the year is clean. The observation family and its language are phase 9.** From Addendum 01 7a. **Measured, sixth measurement:** zero estimate deltas in 13,576 rendered strings across twelve persona years, and zero sentences mentioning an estimate at all.

**What the facts phase landed, and the shape it chose.** `HistoryFacts` carries `estimatedCompletions`, `activeToEstimateRatio` and `estimateTendency`, defined in `CLARITY_LOGIC_ENGINE.md` 3.1 with the window, the sample and the four decisions the definition rests on. The ratio is a **multiple and never a percentage**, because a ratio of 2.4 rendered as 240 percent is one literal hundred away from the second forbidden line below. **No quantity of minutes exists anywhere in the fact set**: `TrailQueries.estimateOutcomes` divides the two magnitudes inside its own body and returns the quotient, so the delta this section bans is unformable above that line rather than caught after the fact. The veto below is still required and is still worth having, as the backstop for a number arriving some other way.

**Hard rule, enforced in the validator: no rendered sentence may state a delta between an estimate and an actual.**

| | |
|---|---|
| permitted | `Things you estimate at an hour tend to take about three.` |
| forbidden | `You underestimated by two hours.` |
| forbidden | `You were off by 140 percent.` |

Only ratios and tendencies. The difference is not politeness and it is not tone. A ratio is a description of how this person's estimates map onto their days, which is useful and which they can do something with. A delta is a score against a target they set themselves, and time blindness is the reason the estimate was wrong in the first place, so the delta measures the symptom and reports it as a mistake.

**Floor.** No estimate observation may fire until **at least five completed items carry an estimate inside the window the sentence describes**, and the count travels as a `FactRef` so the validator re-reads it, per 11.4.

**What the veto phase landed.** The veto is **check 11** in `ClarityValidator`, appended rather than inserted so the ten checks section 8 numbers keep the numbers three documents cite them by. It has two rules: a delta form anywhere in the sentence, whether or not the sentence says estimate, because the second forbidden line above never does; and a `Percent` slot in a sentence that is about an estimate, because the reading is a multiple and never a percentage. `EstimateDeltaVetoTest` builds both forbidden lines word for word and the permitted one beside them, so a veto in that file means something. The floor is `RuleBuilders.estimateFloor`, enforced the way the share floor is enforced, by `CatalogIntegrity.estimateRulesCarryAFloor` over any rule whose criteria read an estimate fact, and the count reaches the validator through the `estimatedCompletions` measure. **Both estimate measures are counts and neither is a percent**, and there is deliberately no measure for a quantity of minutes.

**A new observation family** is authored in phase 9 with the rest of the corpus. Nothing reads the estimate facts today, so the floor check and the veto both have no subjects yet, which is what a backstop should look like.

### 14b.9 Capacity aware decline detection

**The fact, the gate, both branches' rules and the persona test are built, and the acceptance criterion passes. The second branch's language is phase 9.** From Addendum 01 7b. **Measured, sixth measurement:** `cyclicalDips` receives zero decline, neglect or fading observations across fifty two weekly reports, against 34 in the same year composed with every precedent forced to `NONE`, and all 34 sit on a fall whose precedent reads `PRESENT`.

**What the facts phase landed.** `AreaFacts.dipPrecedent`, `HistoryFacts.activityDipPrecedent` and `HistoryFacts.focusDipPrecedent`, defined in `CLARITY_LOGIC_ENGINE.md` 3.1 along with what makes a fall comparable in depth and in duration. `Precedent` has four values and the two that matter to a gate are not opposites: `NONE` is the permission and `PRESENT` is the veto, and `INSUFFICIENT` is neither, so **both branches test for their own value** and a person with too short a history gets neither sentence.

**This is a correctness fix, not politeness.**

A fluctuating condition looks identical to decline in the data. Both are a fall in completions, a rise in idle days, an area going quiet. Without this check, the app will tell someone with a cyclical or relapsing condition that they are deteriorating, **on a fixed schedule, forever, and it will be technically accurate every time**. Every individual report passes its integrity rules. The claim the sequence makes is still false, because the shape it is reading is a cycle and it has read only half of one.

**The rule.** Before any decline, neglect or fading family may fire, the engine asks whether this shape has occurred before in this user's history for this subject. If a comparable dip has happened before, **it is a rhythm, not a decline**, and a different family fires with different language.

This needs a new fact, rules for both branches, and tests for both. The fact's definition, what makes a dip comparable in depth and in duration, belongs in `CLARITY_LOGIC_ENGINE.md` with the other fact definitions; what this document requires is that the fact exist, that it gate those families rather than merely re-word them, and that the gate be reachable in a test.

**What the gate landed as.** `FamilyAvailability.PRECEDENT_GATED` at step 1b of selection, which removes the family from the ranking rather than re-realizing it, so the next ranked candidate is taken and nothing is re-worded. It is not a criterion, because specificity is `criteria.size` and a criterion would make the gated families outrank rules that genuinely require more. **A family is gated only where a precedent fact measures the same quantity its claim is about**, which is the mapping the facts phase declared: the activity precedent answers for `decliningActivity`, `quietWeek` and `hardStretch`, the focus precedent for `focusHabitFading`, and the area precedent for `neglectedArea` and `areaGoneQuiet`. `narrowingFocus` is the family that tests the discipline and is deliberately left out: it is a decline by any reading and its claim is about how many areas moved, which no precedent fact measures.

**The gate closes on `PRESENT` alone, and that is one reading of the sentence above rather than a transcription of it.** Read strictly, `NONE` being the permission asks a decline family to require `NONE`, which would also close the gate on `NOT_IN_A_DIP`. That value means only that the newest closed week is not under three quarters of the subject's normal, which is not a question any decline family asks, so requiring `NONE` would silence a true observation every time the two definitions came apart. `CLARITY_LOGIC_ENGINE.md` 3.1 carries the full argument and `FamilyAvailability.CLOSES_THE_GATE` is the one line that changes it.

**The second branch is `familiarDip`**, declared with its three rules in `FamiliesAwaitingLanguage` and held out of the catalog until phase 9 authors its bench, because a family declared with no lines fails the corpus parser and a family with a rule and no lines would qualify, say nothing, and look like a family that never fired. **It is withheld for the week after a return as well**, per 14b.4, because a sentence about a familiar stretch of low weeks said on the first report back is the absence measured in a kinder vocabulary.

**What the persona landed as, and the qualification an earlier version of it needed.** `SimulationPersona.CYCLICAL` is a life and deliberately not a waveform: twelve episodes across the year, one to three weeks long, arriving after gaps of two to six good weeks, bottoming out anywhere from thirteen events in a week down to one, with recoveries of different heights and the working days inside each week moving. A clean period would have passed the test and proved nothing, because the precedent fact would have been matching on a shape no person produces. **The assertion is the sentence below with nothing added to it: no decline, neglect or fading observation in any of the fifty two weekly reports.** The first version of this persona could not hold that and section 17 was amended to scope the claim to the second half of the year; the amendment is withdrawn, because the reason it could not be held was the persona rather than the fact. `Precedent`'s low is a week under three quarters of normal, which is a much wider bar than any decline family's, so a person can be squarely inside a fall and reach nothing that can be said out loud. This person's first eleven weeks are made of exactly those weeks, including a three week stretch, and by the twelfth, which is where a precedent first becomes answerable, every length and depth the rest of the year holds has already been seen once. `CapacityGatePersonaTest` asserts the two halves separately: that no gated family speaks, and that every gated observation the control run produced sits on a fall whose precedent is `PRESENT`, so no week of the silence came from a ranking, a cooldown or a family that simply did not qualify.

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

**Built, phase 10.** From Addendum 01 8a and 8b. Section 13.1 is otherwise unchanged.

**The zero decision path.** Onboarding beat 2 asks for two to four area names plus a color each, which is up to twelve decisions demanded from people whose central difficulty is deciding. Beat 2 gains a **`Just start`** option offered as a genuine equal alternative, **not buried, not a text link under the real button**. It creates one area named `Today` with the color the mood walk in 8.1 yields first, writes it as a real `AREA_CREATED` exactly as beat 3 would, and drops the user straight into adding their first item. Areas, names and colors become things discovered later, which is the order most people would have chosen anyway.

**Built as a fork rather than as a form.** Beat 2 opens on two stacked panels of identical width, surface and type, one composable called twice with no role parameter that could make either louder, and the area picker is the second screen of one path rather than the beat with an escape hatch attached. `Just start` sits on top, which is the deliberate order rather than the obvious one: some order has to exist, vertical position is the only weight left once the treatment is identical, and the person who most needs a zero decision start is the least likely to read past the first option. It costs the picker's user one tap, which is the right trade in a section whose whole argument is that decisions are expensive and taps are not.

**Announce Pulse before it appears.** One line at the end of onboarding: once a day, one question, one tap, and it can be turned off in Settings. **Predictability matters enormously to autistic users, and interface behavior that arrives unannounced is a real cost**, not a delightful surprise. This line is fixed copy about how the app works rather than an observation about the person, so it lives in `strings.xml`, per 11.2. This is the exact kind of sentence a session will be tempted to route through the engine, and it must not be.

**Built as `onboarding_pulse_announcement` in `strings.xml`**, and it did not go through the engine. It carries a second consequence the phase drew out of the same argument: **beat 4's last moment holds rather than timing out into the app**, because a line that announces a behavior and is then replaced on a timer is the unannounced behavior this paragraph objects to, one level up.

### 14b.12 What this adds to Settings and to DataStore

**Calm mode and the entrance rule are built, phase 3b. The transition warning key is built, phase 4. Both Settings rows are built, phase 11**, `Calm mode` under Appearance and `Five minute warning` under Focus.

Three per-device keys join the list in 5.4, and none of them is engine state, so none of them violates the rule that nothing the engine reads may live in DataStore.

| key | default | phase | row |
|---|---|---|---|
| `calmMode` | follow the OS reduce motion setting | key 3b, row 11 | Appearance |
| `transitionWarningEnabled` | false | key 4, row 11 | Focus |
| `textSize` | `DEFAULT`, which multiplies the OS font scale by 1.0 | issue #51 | Appearance |

**Text size** is the third key and the one that needed a decision rather than a default.
It multiplies the OS font scale rather than overriding it, the combined result is capped
at 200 percent, and the spacing grid opens with the text and never closes below section
6's numbers. `design-v3.md` 13.2 carries all three with the reasoning, including what the
cap costs a person whose phone is already at 200 percent and what the app says to them
instead of moving a selection that changes nothing.

**Calm mode** is a Settings toggle in addition to and independent of the OS reduce motion setting, which `design-v3.md` 8.3 already honors. It reduces motion to crossfades, reduces the saturation of washes and accents, disables the staggered list entrance and the breathing glow, and **applies to the widgets and the Live Update as well as to the app**. `design-v3.md` owns every value it changes. It is what makes Material 3 Expressive safe for this audience rather than overwhelming: **ship the expressive direction and the exit.**

**What was built, and what closed it.** The switch, the color transform, the entrance rule and the three audits in `design-v3.md` 16.6 to 16.8 landed in phase 3b, and calm mode joins the one global motion flag with an `or` rather than adding a motion level beside it, so **reduce motion always wins on motion**. The `Calm mode` **row** landed in phase 11 with the screen it was waiting for, under Appearance, labeled and captioned as `design-v3.md` 16.1 states. The carry-forward closed in phase 12: every widget honors it, and it reaches them in the widget snapshot rather than out of DataStore, which is not multi process safe. A unit test asserts calm mode travels that way rather than being read on the far side. The Live Update's half was phase 4's.

**The stored value stayed nullable through the row landing, and that is load bearing:** a `Boolean` defaulting to false would mean off for every person who has the system setting on and never opens Settings, which is precisely the person the feature exists for. Absence is a state the storage carries and the interface never shows, so `SettingsUiState` deliberately carries **no** calm mode field: the resolved two state value reaches the screen through `LocalCalmMode`, which is computed from the stored value and the live system setting, and a copy in the state would be a second answer that does not follow the system.

Phase 3b also built one motion change from Addendum 01 8e that has no setting behind it: **an entrance fires on the first open of its tab per app session, not on every return to a tab.** An entrance animation on a screen opened twenty times a day stops being an entrance and becomes noise. A session is the process lifetime, so a rotation does not re-arm it and a process death does. The rule is stated once in `design-v3.md` 8.4 and governs every entrance in 8.2 rather than being repeated beside each one. Calm mode removes those entrances entirely rather than reducing them.

**Export's rows** grew as 14b.7 describes, in phase 11.

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
- Reducer determinism, divergence merge, idempotency, checkpoint equivalence, reset virginity. **Phase 8 closed the checkpoint half of this line. Phase 11 closed the other half of reset virginity, which spans two stores and needed a second test to reach.** `ReplayHarnessTest` folds an emptied log and asserts a virgin state; `EraseContractTest` reads the source of both erase methods and asserts preferences are cleared with `clear()` and a fresh `originId` minted, rather than with a list of `remove` calls that a later key would quietly fall off. `CheckpointResumeTest` asserts a checkpoint taken anywhere in a log plus its tail equals a full replay, that a log which grew before the checkpoint cannot be resumed, that a merged log resumed over a checkpoint would silently drop events and is therefore refused, that an emptied log never resurrects the state a checkpoint held, and that the count form of the rule and the list form agree. The other four are phase 1's and are tested there
- One active item per area under concurrent completes; promotion order; order key insertion and rebalance
- Golden fixture replays to the exact committed state
- Pulse selection: no-repeat rule, silence case, 17:00 boundary, escalation monotonicity, DST boundaries. **Phase 6 closed the lifecycle half of this line.** `PulseScheduleTest` walks the 17:00 boundary minute by minute and both daylight saving transitions in each direction, asserting one date key per calendar day and one reflection period across each; `PulseGenerationTest` holds at most one entry per local day, an existing entry stopping the sequence, and the two day suppression after a return. The selection half, the no-repeat rule and escalation monotonicity, is phase 5's and is tested there
- Report integrity vetoes, with a test per validator check constructing a violating candidate. **Closed, phase 8.** `ReportIntegrityVetoTest` builds a violating report for each of the nine checks in `ReportIntegrity` and asserts the veto, plus a first test asserting the fixture report passes all nine so that a veto below it means something, plus a test asserting the declared check list is complete and in the order the layer runs them. Layer 5's own ten checks are phase 5's and are tested there
- **Banned vocabulary test matches the evaluative sense of `behind` only**, per `CLARITY_LOGIC_ENGINE.md` 11.3. The spatial sense, a queue behind an item, is correct and appears in thirteen approved lines
- **Every `lengthBand` is computed at catalog load, never read from a corpus tag.** A test asserts the computed band for every lead and that no two consecutive leads in a generated report share one. **Phase 5 closed the computation half. Phase 8 applied the rule a second time, over the reading order, and did not close the second half.** Regrouping the observations under their sideheads can put two leads of one band together, so the composer re-applies the rule over the order the page is actually read in, as the preference the realizer already treats it as: the next line is the highest ranked one in its section whose band differs, and where every remaining line in the section shares the band, the highest ranked one is taken anyway. **Dropping a true observation to improve the cadence is the trade 11.4 forbids in the other direction, and rhythm is worth a line rather than a paragraph.** The measured collision count is one of the six phase 5 gates that phase 9 lifts, and the baseline it is judged against is in `docs/BUILD_STATE.md`
- **Escalation stage ranges are parsed from the corpus stage headers**, are contiguous and non-overlapping per family, and a compound header becomes two rules rather than a disjunctive range
- **Every family declares a `cooldownDays`** matching the table in `CLARITY_LOGIC_ENGINE.md` 7.3, and no `(family, subjectId)` pair fires inside its window
- **Every rule's `unflattering` flag matches the enumeration** in `CLARITY_LOGIC_ENGINE.md` 7.4, and no neutral or positive family uses the `NEUTRAL_AGENT` register
- Catalog integrity: rules point at existing families, required slots producible, no duplicate keys, **no fragment in two families**, no construction in more than two families, every share-based rule carries an event floor
- Purity: no Android imports, no `System.currentTimeMillis`, no `Random`, no `String.hashCode()` in `domain.engine` or `domain.guidance`
- Composition: no report violates the incompatibility matrix, the length band rule, or the parallel clause cap. **The matrix and the cap are closed, phase 8**, across ten thousand generated weeks and eleven personas playing a simulated year each, asserted against `ReportInvariants`, which restates section 9 by hand from the document rather than calling the code that enforces it. The same runs also hold the area mention cap, the editorial budget, the two to four observation range, the pattern gate, one fact to one number, that no area is named without events in the window, and that no sidehead is drawn twice. **The length band rule is the preference described above and is not asserted there**
- Cue substantiation: no plan renders with a cue below threshold
- **The non-compliance test:** a persona accepting every plan and completing none produces a simulated year in which no sentence references a plan, a commitment, an intention, or a failure to act
- Silence floors: Pulse 8 to 25 percent of days, guidance at least 15 percent of reports
- Simulator: a full year dumps without a crash and without a repeated variant inside 90 days

**Executive function support, per 14b. Each becomes live when its phase lands**
- **No rendered sentence states a delta between an estimate and an actual.** A veto test constructs the forbidden form and asserts it cannot render. **Closed.** It is check 11 in `ClarityValidator`, and `EstimateDeltaVetoTest` builds both of 14b.8's forbidden lines word for word, the permitted line beside them so a veto means something, four other ways of saying the same difference, and a count measure funneled into a percentage slot. **It is a backstop rather than the prohibition**: no quantity of minutes exists anywhere in the fact set and no measure produces one, so the subtraction is unformable above layer 5. No corpus line states an estimate yet, so the check currently refuses only what a test hands it, which is the honest version of a backstop
- No estimate observation fires below five completed items carrying an estimate inside the window the sentence describes. **Closed as a mechanism and awaiting its first subject.** `RuleBuilders.estimateFloor` is the criterion, `CatalogIntegrity.estimateRulesCarryAFloor` fails the build on any rule that reads an estimate fact without it, and the count reaches the validator through the `estimatedCompletions` measure so 11.4's re-read holds. The first rule to read the facts is phase 9's
- **A persona whose activity is cyclical across a simulated year receives no decline, neglect or fading observation**, because every dip they have has a precedent. **Closed, on the unqualified sentence.** `CapacityGatePersonaTest` runs a twelfth persona through a simulated year of weekly reports and composes each week twice: once as the app now speaks and once with every precedent forced to `NONE`, which is the year this person would have had before 14b.9. The control run is the finding, and the assertion covers every week. A second assertion is what makes the first one mean anything: **every gated observation the control run produced sits on a fall whose precedent is `PRESENT`**, so the silence is the gate's doing and never a ranking, a cooldown or a family that did not qualify. **An earlier version of this persona could not hold the claim before its twelfth week and this line was amended to scope it to the second half of the year; the amendment is withdrawn.** The cause was the persona and not the fact: `Precedent`'s low is a week under three quarters of normal and no decline family asks that question, so a first season can be full of real falls and empty of anything sayable, which is what this one is. The twelfth persona is deliberately outside `SimulationPersona.ALL`, because that list is section 12's eleven and every measurement this project has recorded is quoted against it
- **A re-entry persona receives no Pulse for two days and no decline, neglect or gap observation for a week**, and no surface states the length of the gap, counts anything, or asks where they were. **The first clause is true as of phase 6** and `PulseGenerationTest` holds it in both directions, the two days silent and the third day speaking again. **The second clause is now true too.** `FamilyAvailability.WITHHELD_ON_RE_ENTRY` removes thirteen families from selection on the Report, the Momentum headline and the Areas banner for seven days from the return, and `FamilyAvailabilityTest` asserts that none of them survives, that what replaced them already qualified, and that a week with nothing left to say produces a shorter report rather than a padded one. The set includes the families that name the gap a return came back from, `comeback` and `areaRevival` among them, which is where `Back after two weeks` would otherwise have appeared on the first screen back. **The third clause is vacuously true and must not be read as met**: there is no re-entry surface to state anything, per 14b.4, and this line becomes a real check on the day that screen is built
- **The word `abandoned` reaches nothing a person can see**, including the Trail and every accessibility label. The naming decision in 5.2 is settled and the type is `FOCUS_ENDED_EARLY`, so it is absent from the export file too. Phase 4 built the Focus surface and every string on it, and `EndedEarlyRenameTest` holds the line
- Export and import round trip, encrypted and unencrypted, to byte identical state, and a corruption suite refuses cleanly and leaves the database unchanged. **Closed, phase 11.** `BackupRoundTripTest` round trips both file kinds, asserts the header carries the schema version, the date, both counts and a checksum, asserts the export rebuilt from event zero before it wrote, and asserts that a write which fails records no export date. `BackupRefusalTest` carries the six required corruption cases and eight more, including an edited count proved to be covered by the checksum, a duplicated record, a record with no identity, a later event schema version and a wrong password, and every one of them asserts the store was never touched. Both run on a plain JVM against `ClarityBackupStore`, which is the interface that lets a unit test watch for a write that must not happen
- **The scrypt implementation matches the vectors published for it.** `ScryptVectorsTest` asserts `salsa20Core8`, `blockMix`, `roMix`, both PBKDF2-HMAC-SHA256 vectors and the full scrypt vector from RFC 7914 sections 8 through 12, and asserts that the parameters this build actually ships are one of the verified rows. A KDF whose intermediate steps cannot be checked against published vectors is a KDF nobody can check, which is why those three functions are public. **New in phase 11**, and it exists because 14b.7's KDF was written out here rather than taken from a dependency
- An unfiled item is never `ACTIVE`, never `COMPLETED`, never counted in an area's queue, and never named by the engine
- **`APP_OPENED` is excluded from `isUserActivity`**, from the Trail, and from every day header count
- **No widget reads a corpus or runs the engine.** Automated over the imports of the `widget` package
- Calm mode is honored in the app, in every widget and in the Live Update, and the staggered entrance fires once per session per screen. The app is phase 3b, the Live Update and the whole Contemplative surface are phase 4, the widgets are phase 12

**Manual**
- Promotion animation clean with no double text
- Focus session survives process kill. **This is one of the two checks that close phase 4** and it cannot be met by reasoning: `adb shell am force-stop` mid session, then relaunch, then read the remaining time off the ring
- **Swipe right completes; full swipe left swaps; delete requires a tap and offers undo; all three reachable without swiping**
- Widgets update after a completion
- Tutorial spotlights align on the smallest and largest screens. **Now checkable.** Phase 10 built the mechanism and left four of the five targets without a `tutorialTarget`, so the tutorial could not start at all. All five now wear one: the FAB, the first area card, the focus chip and the Pulse chip in `ui/areas/AreasScreen.kt`, and the fifth where phase 10 put it. The remaining half is a gesture on a device at both extremes of screen size
- Font scale 200 percent; dark mode across all Daylight screens; Contemplative screens identical in both system themes
- TalkBack pass, reduce motion pass, haptics correct and never repeated
- **Predictive back shows the correct destination from every screen**, verified by gesture on the device
- **Back during a focus session leaves the session running**, with the ongoing notification and the live countdown on the card both still correct. The other of the two checks that close phase 4, and it is a gesture on the device rather than a unit test, though `FocusEntryTest` holds the decision underneath it
- **Every screen can be left without the tab bar**, verified by walking each destination and pressing back
- **Zero areas is reachable and usable**, with the FAB creating an area
- **A queued item opens an edit sheet**
- **Swipe: only one row opens at a time, a fling commits, a vertical gesture scrolls, delete collapses the row and undo restores it**
- **The build installs and launches on the physical Pixel**, not only on an emulator. Haptics, the focus session surviving a real process kill, and widget behavior cannot be trusted from an emulator

---

## 18. Out of Scope for v1

Sync of any kind, the data model is ready but the transport is not built. Any networking. Any permission beyond notifications, which includes `POST_PROMOTED_NOTIFICATIONS` and therefore permits the Live Update in 14b.6. Any account. Wear OS. Tablet layouts, though the phone layout must not break. Locales beyond English. Macrobenchmark harnesses. Analytics of any kind, permanently.

**The permission line above is not true of the app as built, and phase 12 settled it rather than restating it.** `docs/BUILD_STATE.md` has carried the question since phase 2: section 3 requires WorkManager, 12.1 and 13.3 both schedule work with it, and its manifest contributes three permissions to the merge. The merged manifest was read for both variants rather than reasoned about, and it declares exactly this, on debug and on release:

| permission | where it comes from | what it is |
|---|---|---|
| `POST_NOTIFICATIONS` | this app, 13.4 | runtime, requested contextually |
| `POST_PROMOTED_NOTIFICATIONS` | this app, 14b.6 | normal, granted at install |
| `VIBRATE` | this app, `design-v3.md` 9 | normal, granted at install |
| `WAKE_LOCK` | work-runtime | normal, granted at install |
| `RECEIVE_BOOT_COMPLETED` | work-runtime | normal, granted at install |
| `FOREGROUND_SERVICE` | work-runtime | normal, granted at install |
| `<applicationId>.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION` | androidx.core | signature, declared and used by this app, held by nothing else |

`ACCESS_NETWORK_STATE`, which work-runtime also declares, is removed with `tools:node="remove"` and appears in neither variant. **No network permission is in either merged manifest**, which is the claim 14.3 invites people to check and the one `verifyNoInternetPermission` fails the build over.

**The decision is to ship that list and to say so here, rather than to keep a rule the build has never satisfied.** Four of the seven are beyond notifications, so the sentence above describes the app that was planned and not the app that exists. Dropping WorkManager instead would cost the Pulse reminder and the widget refresh, both of which are required by name; VIBRATE is required by name in `design-v3.md` 9; and the androidx.core entry is a signature permission this app both declares and uses, which nothing else on the phone can hold. None of the four is a runtime permission, none appears in the permission list a person is shown, and none of them is network. **What stays absolute is the network half**: adding a constraint to any work request in this app puts `ACCESS_NETWORK_STATE` back, which changes what the privacy policy invites people to verify, and that is a decision for the owner rather than a build fix.

**Permanently out of scope, and now for a second reason.** Real time social presence of any kind, including body doubling, because it needs networking and would cost the no internet permission guarantee, which is this app's strongest claim. AI task breakdown, per non-negotiable 5; the first step field in 14b.2 is the deterministic version of the same idea. Streaks, badges, XP, levels, confetti and celebration, which were already forbidden by the design and are now also a documented abandonment trigger for this audience. DECISIONS.md holds the full reasoning for all four.

---

## 19. Build Order

**This is the single place a session reads to know what is next**, so every item Addendum 01 adds is written into the phase that owns it rather than left in the addendum. A phase marked done here is shipped and installed. `docs/BUILD_STATE.md` is the live record of what is half done, what is known broken and what the last session left behind; this list is the plan.

**Phase 1. Foundations. Done.** Scaffold. Theme: every token, type scale, shape, `ClarityMotion`, `ClarityHaptics`, reduce motion local. Event log schema, payload serialization, order keys. Reducer and all invariants. Replay test harness and golden fixture. `docs/EVENT_FORMAT.md`. **Git configured with the identity in 16.1, repo created public, everything committed.**

**Phase 2. Core mechanics. Done.** Areas, items, queue. Repository write path, cache projection. Detail sheet, add item, edit, the two stage color picker, archive, tombstoned delete, drag reorder, **swipe gestures with state gating and non-swipe fallbacks**, undo snackbar. The Areas screen with the promotion animation.

**Phase 3. Trail. Done.** Queries facade, screen, filters, day grouping, clustering, pagination.

**Phase 3b. Executive function retrofit. Done.** **It exists because Addendum 01 assigned six of its items to phases 1 and 2, and both are closed and shipped**, and because phase 4 and phase 6 depend on parts of it. It carried: capture with no area and the unfiled inbox (14b.1), the first step field (14b.2), the estimate on capture (14b.3), re-entry detection, meaning `APP_OPENED`, the gap query and the `isUserActivity` exclusion (14b.4), calm mode with its color transform, its one motion flag and the three audits in `design-v3.md` 16.6 to 16.8 (14b.12), and the staggered entrance fired once per tab per app session. Trail rows for the new event types, and none for `APP_OPENED`. It assumed the Addendum 01 event schema in 5.2 was already in the log, and it was, landed in the schema commit that also settled issue #19.

**Two pieces named in this phase are deliberately not in it, and neither is an oversight.** The re-entry **surface** is phase 6, because a screen that has to be able to say nothing is an engine decision and the engine does not exist until phase 5. The `Calm mode` **Settings row** was phase 11, because there was no Settings screen to put it on; the setting behind it was built and honored everywhere, and it followed the OS reduce motion setting until the row landed with that screen.

**Phase 3c. Design foundations, the first half of the polish pass. Done.** Issue #53. **It exists because Addendum 01 3c calls for a polish pass and no phase ever carried it.** When the addendum was recorded on August 27, 2026, 3c was written into 3.2 of this document and into `design-v3.md` 17.4 as a rule about platform versus custom, and the pass the same section describes was never turned into a phase. That is a recording error rather than a change of plan, and the owner found it by looking at the app rather than at this list.

It carries **tokens and type only**, and every item in it is a `design-v3.md` change as well as a code change, recorded in the section that states it rather than in a changelog. The surface ladder in 3.1 and 3.2: three distinct values for `canvas`, `card` and `raise`, and a named job for `raise`, which had none and was drawn nowhere. `card` off pure white, which resolves the contradiction between sections 1 and 14 and the old 3.1. `inkSecondary` raised to hold `design-v3.md` 13's 4.5:1 floor over the new ground, which the canvas change forced rather than invited. A tracking value on every sans role in 5.3, where two of nine had one, and a size step between `body` and `bodyStrong`, which were both 16sp. A serif screen title on the Trail, in section 11. The idle card title off `inkTertiary` in 10.3, where 10.3 and 13 disagreed and 13 wins. And the default area palette walk in 3.4 moved off `actionBlue`.

**It sits between 3b and 4 rather than at the end, and the reason is inheritance.** A token or a type role corrected here is inherited, at no cost and with nobody touching them again, by all eight screens that do not yet exist. Corrected at phase 13 instead, those eight would each have been composed against a value structure the owner has already said is not good enough, and every one of them would then have to be re-examined. Phase 4 also introduces the first Contemplative surface in the app, and a surface should be composed on tokens that have stopped moving.

**What it deliberately leaves alone is phase 12b**, below, and the split is argued for there.

**Phase 4. Focus. Built, and awaiting the device check that closes it.** Issue #2. Sessions, process death persistence, the ongoing notification, the completion flow, the indigo surface and its motion. **Plus Addendum 01:** early ending as a completed short session (#28), `Add 10 minutes` writing `FOCUS_EXTENDED` (#29), the transition warning off by default (14b.5, #30), the Live Update on `Notification.ProgressStyle` with its required silent fallback (14b.6, #32), and the arc reading before the digits on every surface that shows a session (11.3, #49).

**Two things it owes and did not deliver**, both named where they belong rather than dropped: the soft tone at a natural completion, section 10, and the `transitionWarn` haptic, `design-v3.md` 9. Both are signals rather than structure, both are one call each once `ClarityHaptics` carries the event, and neither gates anything. **Two things belonged to phase 11 rather than to this phase**, and both landed there on preferences this phase already read and honored: the `Session length` selector with section 10's eight options, and the `Five minute warning` row, 14.1.

**The two checks that close it are on the phone and nowhere else.** A force stop mid session followed by a relaunch, and the back gesture during a session. Both are in the manual list in section 17, both are the specification's own words, and neither can be met by reading the code.

**Phase 5. Engine skeleton and simulator. Built, and awaiting the closing build and install that finishes it.** Issue #3. Fact extraction, the rule catalog, selection, realization, validation, and **the simulator in `devtools` before any corpus work**. Layers 1 to 5 exist and are described as they are, not as planned, in section 11 above.

It ended up larger than the 40 rules the phase asked for and smaller than the corpus it will eventually drive: **92 rules across 78 families**, every sentence parsed out of the three committed corpus files rather than authored in Kotlin, and 17 new functions on `TrailQueries` because a fact the facade could not answer was added to the facade rather than computed in the engine. Eleven personas run a full simulated year each.

**Six of the ten checks in `CLARITY_LOGIC_ENGINE.md` 12 fail, and that is this phase working rather than failing.** Issue #3 says so in advance: the corpus is not grown until phase 9 and layer 6 does not exist until 9b, so every check whose failure is a bench too small carries a date and the issue that lifts it, runs on every simulation, and prints the number it measured. Those numbers are the baseline phase 9 is judged against and they are in `docs/BUILD_STATE.md` and `DECISIONS.md`.

**Phase 6. Pulse. Built, and awaiting the closing build, install and device check.** Issue #4. Generation lifecycle per 11.3, the sheet, ambient mode, history, reminders. **Plus Addendum 01:** the two day Pulse silence after a return (14b.4), and an empty state that says what it needs and roughly when it becomes useful (14b.10), which landed as two fixed lines describing how the Pulse works and saying nothing whatever about the person's week.

**It is the first phase whose output is a sentence about a person's own life**, and the checks that matter in it are the ones a screenshot cannot make: one entry per local day across both daylight saving transitions, a reflection period that switches once at 17:00, a firing history rebuilt from the log on every invocation and never cached, and a reminder with no code path that can post on a silent day. All four are unit tested. What the device adds is whether the amber night reads as a room, whether the three marks in the rhythm row are distinguishable at arm's length, and whether the reminder actually arrives at the hour it was armed for.

**One thing this phase owned and did not carry: the re-entry surface**, 14b.4 and `design-v3.md` 11.2. The two engine side rules that follow that screen are built and tested; the screen is not, and it has no phase. 14b.4 now says so at the point where a session would otherwise read it as shipped, and `docs/BUILD_STATE.md` carries it as an open question for the owner. **Two things belonged to phase 11 rather than to this phase**, and both landed there: the reminder switch and its hour picker, 14.1, on preferences this phase already read and honored, and the contextual `POST_NOTIFICATIONS` request beside the switch.

**Phase 7. Momentum. Built, and awaiting the closing build, install and device check.** Issue #5. All five blocks plus the empty state, and the Areas banner, which is 12.2's headline machinery pointed at a second purpose and which had been recorded as deliberately absent since phase 2 because its sentence comes from the engine. **Plus Addendum 01:** the same honest first weeks treatment on every block that needs history, and no empty chart anywhere (14b.10).

**It carried half of that addendum item and could not carry the other half.** No module is ever drawn empty: each has a floor under it, an area balance needs two areas with something in them, a sparkline needs three points with two of them carrying something, and idle areas appear at seven days and not a day sooner. The line that says what a module needs and roughly when it becomes useful is a corpus line by 11.1, the edge state benches do not carry one, and **phase 9 grows the corpus and it goes to the owner**, so phase 7 wrote none.

**Its risk was an accidental streak and the guard is structural.** `FactSet` declares no streak fact, the dot row is handed fourteen independent days, and there is no field on the way out of the composer that could answer whether two of them were adjacent. The second risk, a banner recomputing on every recomposition, is answered by a ViewModel of its own resolved against the Activity's store, so the hour is measured in app use and survives a tab switch.

**Phase 8. Snapshots and Report. Built, and awaiting the closing build, install, device check and one write.** Issue #6. Week snapshots doubling as checkpoints, the integrity layer with tests written first, the screen with all four treatments including the week ribbon and the pattern grid break, history, regenerate, copy.

**The integrity tests really were written first and they are the reason the phase is defensible.** `ReportIntegrity` holds nine report scope checks, `ReportIntegrityVetoTest` constructs a violating report for every one of them, and `ReportInvariants` restates section 9 by hand from the document rather than calling the code that enforces it, so a report that satisfies the code and violates the list is a defect in one of the two. That list is asserted over ten thousand generated weeks and over eleven personas playing a simulated year each, with every report written back into the log so the following week's cooldowns and exclusions are real.

**Three things it owes**, all named where they belong rather than dropped. `REPORT_GENERATED` is not written, which is 11.3 step 9 and one method on `ClarityRepository`; until it lands the cadence in 12.3 always answers due, the firing history never learns what the Report said, and the History page is empty. The past report headline is not on the payload, so a row on that page leads with its week and its ribbon; re-realizing the variant would be a second path to a sentence. And of the debug menu and export path that call `rebuildCacheFromLog`, **phase 11 built the export path and did not build the menu**, so the correctness check now runs on every export and the method itself still has no caller.

**The three Addendum 01 items this entry assigned it did not land with phase 8 and landed afterward, in two passes of their own**, and all three are engine work rather than screen work: capacity aware decline detection and its cyclical persona test (14b.9), the estimate calibration facts, their floor and the delta veto (14b.8), and the week long suppression after a return (14b.4). The first pass built the three facts in `domain.engine.facts`; the second built the criteria, the gate, the veto and the tests that read them. **They were recorded as open in `docs/BUILD_STATE.md` and on issue #6 rather than quietly moved to phase 9**, because two of the three are refusals rather than additions and a refusal that nobody is holding is a refusal that ships as its opposite. What is left of them is language, and that is phase 9's.

**Phase 9. Corpus.** Grow toward the sizing targets in batches of forty, one family at a time, judged against simulator output, presented for approval. **Plus Addendum 01:** the tone pass, meaning the widened `unflattering` enumeration, the missing `NEUTRAL_AGENT` variants, the softened `pt.gone` flagship and the `hardStretch` family, plus the estimate observation family in ratio and tendency form only (14b.8, 14b.10). **Two benches are owed to families whose rules already exist**: the estimate family, whose rules are phase 9's to write against the floor and the two count measures already declared, and `familiarDip`, whose three rules are written and waiting in `FamiliesAwaitingLanguage` with the five steps that land it and the constraints its lines are written under (14b.9).

**Phase 9b. Guidance.** Cue fact extraction with confidence thresholds. Layer 6 and its composition rules. Plan events, the nominal offer frame, first-person storage on accept, the explicit decline. Non-evaluative follow-through by priority boost. **The non-compliance test written before the follow-through code, not after.**

**Phase 10. First run. Built, and one modifier short of reachable.** Issue #9. All four beats in `ui/onboarding/`, the persistent nav overlay, the swipes, the iris and the tutorial mechanism in `ui/tutorial/`, plus the first run gate that decides what a cold start does. **Plus Addendum 01:** the `Just start` path at equal standing on beat 2, built as a fork of two identically treated stacked panels rather than as a form with an escape hatch, and the line that announces Pulse before it ever appears, which is fixed copy in `strings.xml` and did not go near the engine (14b.11).

**The tutorial is complete and does not run.** Its mechanism is the uniform one 13.2 demands, with no branch on which step is drawing anywhere in the overlay, and `ClarityShell` composes it last so it really is above the tab bar. Only the tab bar wears `tutorialTarget`; the FAB, the first area card, the Focus chip and the Pulse chip do not, and readiness requires all five. **That is the designed failure rather than a silent one**, because a missing target means the tutorial waits and `hasSeenTutorial` is never written, so it runs correctly on the first launch after the four modifiers land. It is four one line additions in `ui/areas/`, and it is the one thing standing between this phase and its device check.

**One consequence of the gate worth carrying forward.** The two flags are read once and latched for the process, which is what lets beat 3 write `hasCompletedOnboarding` without the route recomputing itself in the middle of the reveal. It also means `Replay the welcome`, `Replay the tour` and the return to onboarding after an erase all take effect on the next cold start. The alternative is a route that observes the flows, and that route swaps onboarding out for the app at the emotional peak of the flow.

**What it deliberately did not build:** design-v3.md 10.15's third cold start check, the re-entry route at fourteen days. 11.2 is still unowned and the gate leaves the place for it marked.

**Phase 11. Settings, About, data. Built, and awaiting the device check.** Issue #10. Every group in 14.1's order, the appearance miniatures drawn from live tokens, the privacy sheet verbatim, the licenses, the permission card, export, import, erase with both halves of the reset test, the `Elsewhere` links and the support block at its two and only two call sites. It also landed the four rows that earlier phases wrote settings for and had no screen to put controls on: `Session length` and `Five minute warning` from phase 4, `Calm mode` from phase 3b, and the reminder switch and hour from phase 6, with the contextual `POST_NOTIFICATIONS` request beside the switch. **Plus Addendum 01:** export as a safety feature in `data/export/`, meaning the optional password over scrypt written out against RFC 7914, the checksum over exact bytes, full pre validation in a half of the code that cannot write to the database at all, replace or merge, both required test suites and the quiet last export line (14b.7).

**Four things it owes**, all named where they belong rather than dropped.

- **Settings is hosted from inside the Areas tab**, so it does not cover the floating tab bar the way `design-v3.md` 10.15 makes a pushed screen cover it. It reserves room at the foot instead. The remedy is one branch in `ClarityShell` beside the Focus surface, and `PushedScreen` and `SettingsSurface` both carry the note
- **`SETTING_CHANGED` is not written when `After completing` changes.** The type, the reducer fold and the Trail row all exist; what is missing is a method on the only writer in the app, shaped like `recordSettingChanged` beside `recordAppOpened`. The event is absent rather than written down a second path, which is the right way round
- **The archived areas view did not land**, so issue #15 does not close with this phase and the archive glyph in the Areas header still opens nothing. Unarchive, the typed permanent deletion and the `AREA_DELETED` tombstone go with it
- **The debug action that calls `rebuildCacheFromLog` still has no caller.** Phase 8 assigned the debug menu and the export path to this phase; the export path landed and shares one locked rebuild with the debug method, and the menu did not

**The disclaimer sentence in About is deliberately absent**, not forgotten. 14.4 and 16.11 require it verbatim in the app and in the store listing at the same time, phase 13 writes the listing, and shipping it early puts it in the app before the thing it has to match.

**Phase 12. Widgets and notifications. Built, and awaiting the device check.** Issue #11. Six required widgets per 13.3, the snapshot they all read, the six hourly refresh job, the optional configuration screen behind three of them. **Plus Addendum 01:** three static app shortcuts and the quick settings tile (13.5).

**Its notification half was already built and this phase confirmed rather than added.** The three channels and the contextual `POST_NOTIFICATIONS` request landed in phases 4, 6 and 11, and the Live Update was built whole in phase 4, promoted style, both actions, silent fallback and all. The extension this list expected of 14b.6 turned out to be nothing: what phase 12 added is a third caller of the same `End` path, the tile, rather than anything new on the notification itself.

**Two things it owes**, both named where they belong rather than dropped. **No widget has a preview image**, because `design-v3.md` 12.1 wants one generated from the real widget and a hand written preview layout is the mockup that rule forbids; it is a device capture and `HANDOFF.md` carries it. And **the two optional widgets, This Week and One Thing, were not built**, which 13.3 permits: the snapshot carries a slot for each so that building them later is a composer change and not a schema change. **The routing gap it also owed is closed**, in the follow up 13.3 now describes: all six actions land on their surface, cold and warm alike, and a test fails the build if a seventh is ever added without one.

**What it settled that had been open since phase 2.** Section 18's permission rule was checked against the merged manifest for both variants rather than reasoned about, found to be describing an app that was never built, and rewritten to state the seven permissions that are actually there and why each one stays. The network half of that promise is unchanged and still gated on every build.

**Phase 12b. Design surfaces, the second half of the polish pass.** Issue #54. Everything phase 3c deferred because it needs more than two screens on the table to judge: scroll edge treatment at the top of every scroller and above the floating tab bar, the sheet shadow `design-v3.md` 6.1 declares and nothing draws, the question of whether anything in this app moves at rest, what the Trail's event circle carries that the sentence beside it does not, whether an inactive tab keeps its label, and what a text field looks like, which section 10 never says. Three of those are open choices under `design-v3.md` 15 and get the section 15 treatment and a `DECISIONS.md` entry each.

**Why it waits.** Token and type changes are inherited free by screens that do not exist yet, which is why 3c is early. Surface changes have the opposite property and get better information by waiting: fading the scroll edge on Areas does nothing for Momentum, and deciding what a text field looks like is worth doing once, when there is more than one sheet to judge it against. The Contemplative world is also unbuilt until phases 4, 6 and 8, and it carries half this app's character and all of its atmosphere, so polishing the Daylight surface with no Contemplative surface to balance it against is judging half a design against itself.

**Its own number, and not a bullet inside phase 13, which is the part that matters.** Phase 13 already carries the Baseline Profile, R8, the accessibility pass, the full checklist, real screenshots, the README, the store listing and the release. **A polish pass buried inside a ship phase is the first thing cut when a date moves.** Giving it a number of its own means it has to be closed on its own terms, and phase 13's checklist can then check that it was.

**Phase 13. Ship.** Baseline Profile, R8, accessibility pass, the full checklist, real screenshots, README, release. **Plus Addendum 01:** the store listing and its keywords, the forbidden claim words, the required disclaimer and the Play Health Apps Declaration, all per 16.11.

**The follow-through in phase 9b is the last thing built and the first thing removed** if it reads as supervision when tested. That reservation was formally registered by the review panel and it stands.

**At the end of every phase**, apply 16.8: commit, push, build, and install the updated app on the phone.
