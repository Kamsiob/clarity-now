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
  data.widget                the one snapshot every widget reads, and nothing else

  domain                     ClarityClock, the single source of time
  domain.replay              reducer, invariants, conflict resolution, checkpoints
  domain.engine              layer one's facts, the engine loop, StableHash
  domain.engine.catalog      layer two: the corpus parser and the rule catalog
  domain.engine.select       layer three
  domain.engine.realize      layer four
  domain.engine.validate     layer five
  domain.guidance            layer six, the plan composer
  domain.query               TrailQueries, the only path from data to a number
  domain.corpus              the corpus seam, and the one catalog for the process

  ui.theme                   colors, type, shape, motion tokens, haptics
  ui.components              shared composables
  ui.areas ui.focus ui.pulse ui.momentum ui.report ui.trail
  ui.settings ui.about ui.onboarding ui.tutorial
  ui.reentry                 one screen, shown at most once per absence
  ui.nav                     the shell, the tab bar, and what covers them

  widget notifications work  the surfaces outside the app, and the jobs behind them
  shortcuts tile             the launcher surfaces: three static shortcuts, one tile
  devtools                   the simulator: personas, the dump, the checks. Debug only
  di                         ClarityGraph, the hand written container
```

**Two modules, not one.** `:baselineprofile` is a `com.android.test` module that drives
the real app on a real device to generate `app/src/main/baseline-prof.txt`. It ships no
code into the APK and nothing in it runs in `verifyClarity`. A macrobenchmark has to cold
start the app under test as a separate process, which it cannot do from inside that app's
own instrumentation, so the separate module is the only shape that measures a real cold
start rather than a warm one.

**Files worth knowing about by name**, because their names do not say what they are:

| file | what it holds |
|---|---|
| `ui/nav/PushedScreens.kt` | a depth count in a composition local. A pushed screen declares itself and the shell stops composing the tab bar |
| `ui/nav/ExternalRequest.kt` | one pure table from an intent action to a destination. Every widget, shortcut and notification route is in it |
| `ui/nav/PointerBlocking.kt` | one modifier, so a surface drawn over another one does not let taps through to it |
| `ui/components/ScrollEdge.kt` | the fade at the top and bottom of every scroller, which erases rather than paints |
| `ui/theme/ClarityTextSize.kt` | the app's own text size, applied to `LocalDensity` in exactly one place |
| `data/repo/AreaRestore.kt` | where a restored area's order key comes from, which is usually nowhere |
| `data/repo/ReEntryChoice.kt` | a pure function returning `List<ItemQueued>`, so clearing cannot compile into a delete |

## Layering rules

These are enforced, not aspirational.

- **ViewModels never touch a DAO.** Only repositories.
- **Composables never touch a repository.** Only ViewModels, plus a thin theme and
  haptics layer.
- **`domain.engine`, `domain.guidance` and `domain.replay` are pure Kotlin with no
  Android imports.** Tests assert this.
- **Every displayed number comes from a query.** There is no second path.
- **The realizer receives only the `FactSet`.** No live entity table reaches layer 4, and
  there is no parameter through which one could. See the engine section below.
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

## The engine

Five of the six layers exist. Each is a pure function, each is independently testable,
and each is separated from the next by what it is allowed to see rather than only by
what it does.

```
Event log
    |
[1] FactExtractor      (TrailQueries, TrailWindow)        -> FactSet          built
[2] ClarityCatalog     the three corpus files, parsed     -> rules, phrasing  built
[3] Selector           (FactSet, FiringHistory, catalog)  -> ranked, or why not
[4] Realizer           (Selection, FactSet, catalog)      -> Candidate        built
[5] ClarityValidator   (Candidate, FactSet)               -> pass, or a veto  built
[6] GuidanceComposer   (Validated[], FactSet, PlanHistory) -> a plan, or not  phase 9b
    |
Rendered output, or nothing
```

`ClarityEngine.observe(facts, history, purpose)` runs 3, 4 and 5 in a loop and answers
`Spoke` or `Silent`. Layer six runs **only** for the Report, only after layers 1 to 5
have produced the body, and produces at most one output. Pulse, Momentum and the banner
never reach it.

### What each layer may see, and what it may never see

| layer | sees | never sees |
|---|---|---|
| 1 `FactExtractor` | `TrailQueries` and one window | the catalog, any sentence, any rule |
| 2 `ClarityCatalog` | the three corpus files as text | any fact, any person's data |
| 3 `Selector` | facts, firing history, rules | any sentence. Rules carry no strings |
| 4 `Realizer` | **the `FactSet` and the corpus, and nothing else** | any live entity table |
| 5 `ClarityValidator` | the candidate and the same `FactSet` | the log, the clock, the catalog's rules |
| 6 guidance | only the observations that were **validated** | anything vetoed, anything not shown |

**The rule that matters most is layer 4's.** The realizer receives the `FactSet` and a
corpus and there is no parameter through which a live entity table could be passed. Every
name it can reach is a snapshot field that layer one resolved by folding the log to the
window being described, so a sentence realized today about a week in March still says
what that area was called in March. `CLARITY_LOGIC_ENGINE.md` 8 check 5 is enforced by
that shape rather than by a check: the stale name failure has nowhere to come from.

The same trick is used three more times, and each one turns a rule somebody has to
remember into a shape nobody can violate.

- **Archived and tombstoned areas are absent from `FactSet.areas` entirely.** A subject
  selector reads that map, so there is no subject to qualify, so prohibition 3 of 1.1
  holds without any rule checking anything
- **`Validated` can only be constructed by layer 5.** Layer six takes a list of them, so
  it cannot advise on a sentence that was vetoed or never appeared
- **`ClarityEngine` holds its validator as a seam with no default.** There is no
  constructor that omits layer 5, so the bypass `MASTER_BUILD_PROMPT.md` 11.4 forbids
  cannot be written by accident

### Where the engine's types live

```
domain.engine              FactSet and every fact class, FiringHistory, FactDates,
                           FactExtractor, StableHash, FactRef, Validated,
                           ClarityEngine, EngineResult, SilenceReason, RenderedOutput
domain.engine.catalog      Purpose, Register, LengthBand, the corpus parser, the rule
                           catalog, the phrasing catalog, integrity checks
domain.engine.select       Selector, Selection, the incompatibility matrix
domain.engine.realize      Realizer, Slot, SlotBindings, Measures, VariantChoice,
                           RegisterChoice, Candidate
domain.engine.validate     ClarityValidator, the ten checks, the banned vocabulary
```

The fact classes sit in `domain.engine` and in a `facts/` directory, which is the layout
`CLARITY_LOGIC_ENGINE.md` 2.1 asks for: the facts are the engine's shared vocabulary and
every layer imports them, so they are not in a subpackage of their own. `Purpose`,
`Register` and `LengthBand` are in `catalog` rather than in `domain.engine` where 2.1
places them, which is a phase 5 build artifact recorded in `DECISIONS.md` rather than a
decision.

### Two properties that outrank the rest

- **The engine may say nothing.** Silence is supported, expected and designed, and it
  carries one of five reasons so that a quiet week and a rule that can never fire are
  told apart in the simulator dump and in a debug log. The reason is never shown to a
  person
- **`FiringHistory` is rebuilt from the log on every invocation.** It derives entirely
  from `PULSE_GENERATED`, `REPORT_GENERATED` and `PLAN_OFFERED`, is never persisted and
  never read from DataStore, because DataStore does not merge and two devices holding the
  same log must compute the same next sentence

### The catalog is parsed, not authored in Kotlin

`ClarityCatalog.build` reads `CORPUS_1_PULSE.md`, `CORPUS_2_REPORT.md` and
`CORPUS_3_MOMENTUM.md` as text and produces every family, stage, variant and response
pair. Nothing is copied into code, so a corpus edit cannot silently disagree with a copy
of the corpus embedded somewhere else. Two consequences follow and both are load bearing:

- **Escalation thresholds live in the corpus stage headers** and are parsed into ranges.
  A compound header becomes two rules pointing at the same stage, never a disjunctive
  range, and a qualitative header gets no range at all rather than a guessed one
- **`lengthBand` is computed at load** from the realized word count, never read from a
  tag, because a hand applied tag drifts the moment a line is edited

Rules are the other half and they are authored in Kotlin, in `PulseRules`, `ReportRules`
and `MomentumRules`. **Rules contain no strings.** A rule names a family, a stage, a
subject selector and a list of criteria, and the criteria are predicates over facts.

### The simulator

`devtools`, debug builds only, and the Gradle task `verifyDevtoolsAreDebugOnly` reads the
source directories Gradle resolved rather than trusting the layout: it fails if the
package is missing from the debug source set, present in a release one, or named by any
file a release build compiles. Being in `src/debug` is the mechanism; the task is the
verification.

It runs eleven synthetic personas through a full simulated year, the engine day by day
for the Pulse, week by week for the Report and Momentum on every simulated open, and
dumps plain text annotated with the rule, the stage, the register, the variant key and
the facts used. It writes the engine's own output back into its log, because
`FiringHistory` is derived from exactly those events and a simulator that dropped what it
said would show every family at stage one forever.

Ten automated checks run over the dump. Four are enforced now; six measure properties a
corpus of this size cannot have and are deferred, with a date and an issue, to phase 9
and 9b. `docs/BUILD_STATE.md` carries the numbers they measured.

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
| `DomainPurityTest` | `domain.engine`, `domain.guidance`, `domain.replay`, `domain.query` and `domain.corpus` import no Android, no clock, no random and no `String.hashCode` |
| `SharedCatalogTest`, `CatalogSharingTest` | the corpus is read and parsed once however many surfaces ask and however many ask at once, a failure is reported with its reason and retried rather than held, and exactly one place in the app builds a catalog |
| `CorpusParseTest`, `StageRangeTest`, `LengthBandTest` | the three corpus files parse, stage ranges are contiguous per family, and every computed length band matches the words in the line |
| `RuleCatalogTest`, `CatalogIntegrityTest`, `FamilyPolicyTest` | every rule points at a family that exists, every family has a rule or a recorded reason for having none, no duplicate keys, and every share based rule carries an event floor |
| `FactExtractorTest`, `FactSetIntegrityTest` | the facts are what the queries say, and an archived or tombstoned area is absent from the fact set rather than filtered later |
| `FiringHistoryTest` | two histories rebuilt independently from the same merged log select the same thing for the same day |
| `SelectorTest` | the seven steps run in the order section 5 states, and the ranking's final key sort is present |
| `RealizerTest`, `SlotRenderingTest`, `VariantChoiceTest`, `RegisterChoiceTest` | the ladder never goes backwards, numbers render by register, and the same day picks the same line on two devices |
| `ValidatorChecksTest`, `ValidatorVetoTest` | all ten checks, and a deliberately violating candidate for each of the first four so the veto branch is executed rather than assumed |
| `EngineDeterminismTest` | ten thousand generated cases through two independently built engines, with the history's maps rebuilt in reverse |
| `SimulatorTest` | a full simulated year for eleven personas dumps without a crash, annotated, with the ten checks reporting their numbers |

`testdata/golden-log.json` and `testdata/golden-state.json` are the contract with the
future Linux desktop app. Regenerate deliberately, never as a side effect:

```
./gradlew :app:testDebugUnitTest -PregenerateGolden=true
```

If the golden test fails after a state shape change, that is the fixture doing its
job. Read the diff before regenerating.
