# Architecture

How the code is laid out and why. `MASTER_BUILD_PROMPT.md` section 4 is the
authority; this file is the working map.

---

## The one sentence version

The event log is the truth, a pure reducer folds it into `ClarityState`, the
repository is the only writer, and every sentence about a person's own data comes
out of a six layer engine that never touches Android.

---

## Package map

```
com.kamsiob.claritynow
  ClarityApp                 Application, installs the graph
  MainActivity               single activity, sets the theme and the shell

  data.event                 the log: ClarityEvent, payloads, the DAO, the codec
  data.model                 Room entities for the materialized cache
  data.db                    database, DAOs, converters, migrations
  data.repo                  repositories, the only thing that writes
  data.prefs                 DataStore, per device settings only
  data.export                backup, export, import, the sync file format

  domain                     ClarityClock, the single source of time
  domain.replay              reducer, invariants, conflict resolution, checkpoints
  domain.engine              facts, rules, catalog, selection, realization, validation
  domain.guidance            layer six, the plan composer
  domain.query               TrailQueries, the only path from data to a number

  ui.theme                   colors, type, shape, motion tokens, haptics
  ui.components              shared composables
  ui.areas ui.focus ui.pulse ui.momentum ui.report ui.trail
  ui.settings ui.about ui.onboarding ui.tutorial
  ui.nav                     the shell and the tab bar

  widget notifications
  devtools                   simulator and corpus dump harness, debug only
  di                         ClarityGraph, the hand written container
```

## Layering rules

These are enforced, not aspirational.

- **ViewModels never touch a DAO.** Only repositories.
- **Composables never touch a repository.** Only ViewModels, plus a thin theme and
  haptics layer.
- **`domain.engine`, `domain.guidance` and `domain.replay` are pure Kotlin with no
  Android imports.** Tests assert this.
- **Every displayed number comes from a query.** There is no second path.
- All Compose state is immutable, stable, and collected as `StateFlow`.
- Lazy lists always use stable keys.
- One `ClarityClock`, injected everywhere time is read.

---

## The three things that surprise people

### 1. `ClarityEvent` and `ClarityEventRow` are different types

`domain.replay` must not import `androidx.room`, so the log record cannot be the
Room entity.

- `data.event.ClarityEvent` is plain Kotlin, serializable, and is what the reducer,
  the export path and the golden fixture use
- `data.db.ClarityEventRow` is the `@Entity` for the `clarity_event` table
- `toRow()` and `toEvent()` map between them

`toEvent()` returns null for an event type this build does not know, which is what
lets a newer desktop build and an older phone build share a file without data loss.

### 2. The in memory state is the projection, not the tables

`ClarityRepository` holds a `MutableStateFlow<ClarityState>` produced by folding the
log. That is what the UI reads.

The Room cache tables mirror it so a cold start does not replay a year of events.
They hold current state and no history, so they cannot serve the Trail: the Trail
pages the event log itself, one wall clock window at a time, through
`ClarityRepository.trailPage()`. **Every one of those tables can be dropped and
rebuilt with no data loss**, and `ClarityRepository.rebuildCacheFromLog()` does
exactly that as a proof.

### 3. The write path is one function

```
UI calls a repository method
  -> repository builds one or more payloads
  -> commit(vararg payloads)
       reserves consecutive lamports
       builds ClarityEvent for each
       folds them through ClarityReducer
       inside one Room transaction: append the rows, upsert the changed cache rows
       emits the new state
```

All payloads passed in one `commit` call belong to one user action and get
consecutive lamport values, so a completion and the promotion it caused can never
be separated by an event from another device.

There is no other way to change state anywhere in the app.

---

## Ordering, merging and time

- Total order is `(lamport, originId, id)` ascending. **Never `wallClock`**, because
  two devices will disagree.
- `wallClock` is display only.
- `lamport` advances to `max(local, seen) + 1`.
- `originId` is a UUID generated once at install. It identifies the device, never
  the person, and is never sent anywhere because there is nowhere to send it.
- `inTotalOrder()` sorts and removes duplicate deliveries by event id. Idempotency
  lives there rather than in the reducer, so the reducer stays a plain fold.
- Order keys are **fractional indices** stored as base 62 strings, never integers.
  `OrderKey.between(a, b, jitter)` produces a key strictly between two others
  without touching any other row. The jitter comes from the device id, so two
  people inserting at the same point offline do not collide.

## Conflict resolution

Handled inside the reducer, deterministically, at the divergence point.

| conflict | resolution |
|---|---|
| Two `ACTIVE` items in one area | higher `(lamport, originId)` wins, loser goes to the head of the queue with a fresh order key, a `ClarityConflict` is recorded |
| Edit versus delete | delete wins; the edit stays in the log and has no effect |
| Concurrent reorder | fractional keys mean both survive in a deterministic order |
| Duplicate date keyed rows | higher `(lamport, originId)` wins, loser drops from projection but stays in the log |

Conflicts are surfaced on the Areas screen as one dismissible card in the app's
voice, assembled from the snapshots carried in the events themselves. Never silent,
never a technical dialog, never data loss.

---

## The engine, in one diagram

```
Event log
    |
[1] FactExtractor      (EventLog, Window, Clock) -> FactSet
[2] RuleCatalog        static data, no strings
[3] Selector           (FactSet, FiringHistory, RuleCatalog) -> Selection?
[4] Realizer           (Selection, FactSet, PhrasingCatalog) -> Candidate
[5] Validator          (Candidate, FactSet) -> Validated | Vetoed
[6] GuidanceComposer   (Validated[], FactSet, PlanHistory) -> GuidanceResult
    |
Rendered output, or nothing
```

Layer six runs **only** for the Report, only after layers 1 to 5 have produced the
body, and produces at most one output. Pulse, Momentum and the banner never reach it.

Two properties matter more than the rest:

- **The engine may say nothing.** Silence is supported, expected and designed.
- **`FiringHistory` is rebuilt from the log on every invocation**, never persisted
  and never read from DataStore, because DataStore does not merge and two devices
  holding the same log must compute the same next sentence.

---

## Theme

Two worlds, scoped separately so one can never be accidentally inverted into the
other.

- **`ClarityTheme`** is the Daylight world: Areas, Momentum, Trail, Settings, About.
  Follows the light, dark or system setting.
- **`ContemplativeTheme`** is Focus, Pulse, Report and Onboarding. Always dark,
  regardless of the setting.

Material You dynamic color is explicitly not used. The Material color scheme exists
only so Material components do not fall back to the default purple; every color the
app actually draws comes from `LocalClarityColors`.

Motion is spring first, which is both what `design-v3.md` section 8 specifies and
the Material 3 Expressive model. `clarityMotion()` returns the reduced variant when
the animator duration scale is zero **or calm mode is on**, so reduce motion is one
global check rather than twenty six individual ones and calm mode joins that check
with an `or` rather than adding a level beside it. The animator scale is observed
rather than read once, because calm mode's default is to follow it live.

Calm mode's color half is `ui/theme/CalmMode.kt`, an OKLab chroma transform applied
at one point, `Modifier.areaWash`, plus the two other atmospheric uses of an accent
that call the transform deliberately: the Swap swipe face and the Trail's event
circle. The 7dp area dot and the area label text are excluded by
`design-v3.md` 16.2 and never pass through it. `ui/theme/ClarityEntrance.kt` holds
8.4's rule that an entrance fires once per tab per app session, which calm mode
removes entirely rather than shortening.

---

## Testing

| suite | what it proves |
|---|---|
| `ReplayHarnessTest` | determinism, divergence merge, idempotency, checkpoint equivalence, reset virginity |
| `GoldenFixtureTest` | the committed fixture replays to the exact committed state |
| `ConflictResolutionTest` | each conflict rule resolves as specified |
| `OrderKeyTest` | insertion, ordering, rebalance |
| `EventFormatTest` | every payload round trips through JSON |
| `StableHashTest` | FNV-1a matches known vectors |
| `ClarityInvariantsTest` | an unfiled item is never active or completed, and an area delete does not orphan |
| `OptionalFieldsReplayTest` | the first step and the estimate survive every arrival order |
| `ReEntryGapTest` | one presence marker per calendar day, and the gap across both daylight saving boundaries |
| `Migration2To3Test` | the golden log replays across the Room 2 to 3 migration |
| `CalmModeTest` | the default resolution, the transform, and that every area color reaching the screen was routed deliberately |
| `CalmModeContrastTest` | 4.5:1 on all 48 area colors, both worlds, ordinary and calm, computed rather than judged |
| `DomainPurityTest` | `domain.engine`, `domain.guidance`, `domain.replay` and `domain.query` import no Android |

`testdata/golden-log.json` and `testdata/golden-state.json` are the contract with the
future Linux desktop app. Regenerate deliberately, never as a side effect:

```
./gradlew :app:testDebugUnitTest -PregenerateGolden=true
```

If the golden test fails after a state shape change, that is the fixture doing its
job. Read the diff before regenerating.
