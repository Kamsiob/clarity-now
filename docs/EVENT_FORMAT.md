# The Clarity Now event format

This document and the two files in `testdata/` are the contract between the Android
app and any other implementation, including the Linux desktop companion that will be
written later. A second implementation that reads this document, replays
`testdata/golden-log.json`, and produces `testdata/golden-state.json` agrees with
this one about everything that matters.

If this document and the code ever disagree, the code in `data.event` and
`domain.replay` is the truth and this document is a bug.

---

## 1. The idea

The event log is the truth. Areas, items, queue positions, completion states,
reflections and reports are all computed by replaying the log in order. Every other
table in the database is a cache that can be deleted and rebuilt with no data loss,
and a debug action does exactly that as a proof.

Nothing in version 1 of the app performs networking. The format, the reducer and the
merge rules exist now so that exchanging log files later requires no change to the
data model, the reducer, or any screen.

---

## 2. The event

One append only table, `clarity_event`. Rows are never updated and never deleted.

| field | type | notes |
|---|---|---|
| `id` | string | UUIDv4 in the app. Any stable unique string is valid |
| `schemaVersion` | integer | currently `1`, present on every event ever written |
| `type` | string | one of the names in section 4 |
| `wallClock` | integer | epoch milliseconds. Display only. **Never used for ordering** |
| `lamport` | integer | logical clock. See section 3 |
| `originId` | string | a UUID generated once per install. Identifies the device, never the person |
| `entityId` | string or null | the primary entity the event concerns, indexed |
| `payload` | object | shaped by `type`, see section 4 |

`entityId` is always the primary id inside the payload: the area for area events, the
item for item events, the session, pulse, report or plan for theirs, and the setting
key for `SETTING_CHANGED`. It is derived rather than authored, so it can always be
recomputed if it is ever wrong.

### The JSON form

A log file is a JSON array of event objects. The payload is a nested object, not an
escaped string, so a log can be read by a person and parsed once by a program.

```json
[
  {
    "id": "evt-001",
    "schemaVersion": 1,
    "type": "AREA_CREATED",
    "wallClock": 1767517200000,
    "lamport": 1,
    "originId": "01947b3f-0000-4000-8000-000000000001",
    "entityId": "area-work",
    "payload": {
      "areaId": "area-work",
      "name": "Work",
      "colorHex": "#2D7FF9",
      "orderKey": "a0mr"
    }
  }
]
```

Nulls are written explicitly rather than omitted, so a second implementation sees the
field exists. Unknown payload fields are ignored on read, and an unknown `type` is
skipped with a diagnostic rather than refusing the file. Both rules exist so that a
newer build and an older build can share a log without either losing data.

The Room column form is the same payload object serialized on its own. It is the only
place the payload appears as a string.

---

## 3. Ordering

**The total order is `(lamport, originId, id)` ascending.**

- `lamport` is a logical clock. A device advances it to `max(local, seen) + 1` when it
  merges a foreign log, so causality is preserved without a shared clock
- `originId` breaks a tie between two devices that acted at the same logical time. It
  is arbitrary but stable, which is exactly what a tiebreak needs to be
- `id` is a final tiebreak. A same device, same lamport collision should be
  impossible, and if one ever happens the answer is still one answer rather than an
  arbitrary one

**`wallClock` is never part of the order.** Two devices will disagree about the time,
and a log sorted by wall clock reorders itself when a phone's clock is corrected.

### Idempotency

Delivering the same event twice must not change the resulting state. This is handled
before the reducer runs: the log is sorted into total order and duplicate `id`s are
dropped. The storage layer also ignores an insert whose `id` already exists.

---

## 4. The event catalog

Twenty four types. Every payload carries full before and after values, so a replay
reconstructs state without reading any other table.

Display snapshots are a separate question and they are not universal. Eight of the
twenty four carry enough of one to name both the subject and the area of a Trail row
with no lookup at all: the five area events that carry a name, and `ITEM_ADDED`,
`ITEM_PROMOTED` and `ITEM_COMPLETED`, which carry a title and an area name together.
The other sixteen carry a partial snapshot or none. Those resolve their display
values by folding the log to the instant of the event, which is what
`domain.query.TrailQueries` does. Neither path reads a live entity table, so a Trail
entry from a year ago still renders the name an area had at the time.

### Areas

| type | payload |
|---|---|
| `AREA_CREATED` | `areaId`, `name`, `colorHex`, `orderKey` |
| `AREA_RENAMED` | `areaId`, `previousName`, `newName` |
| `AREA_RECOLORED` | `areaId`, `previousHex`, `newHex` |
| `AREA_REORDERED` | `areaId`, `previousOrderKey`, `newOrderKey` |
| `AREA_ARCHIVED` | `areaId`, `nameSnapshot` |
| `AREA_UNARCHIVED` | `areaId`, `nameSnapshot` |
| `AREA_DELETED` | `areaId`, `nameSnapshot` |

`AREA_DELETED` is a tombstone. The row is never removed. Deleting an area also
tombstones every item inside it, computed by the reducer rather than written as
separate events, because a cascade computed identically on every device cannot fall
out of step with its parent.

### Items

| type | payload |
|---|---|
| `ITEM_ADDED` | `itemId`, `areaId`, `title`, `note`, `orderKey`, `areaNameSnapshot` |
| `ITEM_EDITED` | `itemId`, `previousTitle`, `newTitle`, `previousNote`, `newNote` |
| `ITEM_QUEUED` | `itemId`, `areaId`, `orderKey`, `previousStatus` |
| `ITEM_PROMOTED` | `itemId`, `areaId`, `previousStatus`, `demotedItemId`, `demotedToOrderKey`, `titleSnapshot`, `areaNameSnapshot` |
| `ITEM_COMPLETED` | `itemId`, `areaId`, `titleSnapshot`, `areaNameSnapshot`, `activeDurationDays` |
| `ITEM_REOPENED` | `itemId`, `areaId`, `targetOrderKey` |
| `ITEM_REORDERED` | `itemId`, `areaId`, `previousOrderKey`, `newOrderKey` |
| `ITEM_DELETED` | `itemId`, `areaId`, `titleSnapshot` |

`previousStatus` is one of `ACTIVE`, `QUEUED`, `COMPLETED`, `DELETED`.

**`ITEM_PROMOTED` carrying `demotedItemId` is the detail that makes a swap replay
correctly, and it is exactly what a descriptive log would omit.** A swap is one event,
not two. Without the demoted id, a second device replaying the promotion would leave
two active items in one area and have no record of which one moved.

An item is created `QUEUED`. An area with no active item promotes its new item
immediately, which is written as `ITEM_ADDED` followed by `ITEM_PROMOTED`.

Only an active item can be completed. A completion aimed at a queued item is recorded
as a diagnostic and has no effect.

### Focus

| type | payload |
|---|---|
| `FOCUS_STARTED` | `sessionId`, `areaId`, `itemId`, `plannedSeconds` |
| `FOCUS_COMPLETED` | `sessionId`, `actualSeconds` |
| `FOCUS_ABANDONED` | `sessionId`, `actualSeconds` |

Abandonment is neutral. Nothing anywhere in the app treats it as a failure.

### Pulse

| type | payload |
|---|---|
| `PULSE_GENERATED` | `pulseId`, `dateKey`, `family`, `escalationStage`, `register`, `variantKey`, `renderedObservation`, `renderedQuestion`, `factSnapshot`, `reflectionPeriod` |
| `PULSE_ANSWERED` | `pulseId`, `responseKey`, `responseLabel`, `responseIsPositive` |

`dateKey` is `yyyy-MM-dd` in the device's local zone. `reflectionPeriod` is
`YESTERDAY` or `TODAY_SO_FAR`. `factSnapshot` is a flat map of strings recording what
the engine saw when it spoke.

`responseLabel` is stored verbatim, so a later callback can quote what the person
actually saw rather than a label reworded in a newer version of the app.

The generation events are also where the engine's own history lives. Variation
history, escalation stages and the last family shown are all rebuilt from
`PULSE_GENERATED`, never read from device preferences, because preferences do not
merge and two devices would silently disagree about what to say next.

### Report and guidance

| type | payload |
|---|---|
| `REPORT_GENERATED` | `reportId`, `weekStartKey`, `headlineKey`, `renderedSections`, `factSnapshot` |
| `PLAN_OFFERED` | `planId`, `weekStartKey`, `frameKey`, `cueKey`, `actionKey`, `familyKey`, `subjectId`, `offeredLine`, `committedLine`, `resolutionFactRef` |
| `PLAN_ACCEPTED` | `planId` |

`renderedSections` is a list of `{ sectionKey, sidehead, text }`. `weekStartKey` is the
`yyyy-MM-dd` of the Sunday that starts the week.

`resolutionFactRef` is `{ category, path }`, naming the fact a follow up would read.

**There is no `PLAN_DECLINED`, deliberately.** Declining a plan writes nothing, costs
nothing, and is never counted. An unaccepted plan is simply one that was never
accepted, and the format has no way to express the difference between declining and
ignoring.

`offeredLine` is nominal and `committedLine` is first person. The imperative form of a
plan does not exist anywhere in the app, so it is not stored here either.

### Settings

| type | payload |
|---|---|
| `SETTING_CHANGED` | `key`, `previousValue`, `newValue` |

Only for settings that affect behavior history, such as `afterCompleting`. Ordinary
per device preferences, including the theme and the reminder hour, live in DataStore
and are never events, because they are not shared and not part of history.

---

## 5. Order keys

`orderKey` is a fractional index: a string, never an integer.

Integer positions break under concurrent reorder and cannot be retrofitted once
someone has data, so this is settled from the first event.

**Alphabet.** Base 62, in ASCII order:

```
0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz
```

Ordinary string comparison is therefore the sort.

**Shape.** A short integer part followed by an optional fraction. The head character
of the integer part encodes that part's length: `A` through `Z` count down from 27
characters to 2, and `a` through `z` count up from 2 to 27. An empty list starts at
`a0`, with room on both sides for more insertions than anyone will make.

The integer part keeps repeated appends short. Five hundred appends stay within four
characters. The fraction appears only when something is inserted between two adjacent
keys.

**Jitter.** Generated keys carry two extra characters derived from the writing
device's `originId`. Fractional indexing is deterministic, which is a virtue
everywhere except here: two people inserting at the same point in the same queue,
offline, would otherwise compute byte identical keys and the queue would hold two rows
claiming one position. The jitter is appended only when the result still sorts below
the upper bound, because a generated key is sometimes a prefix of that bound.

**Rules.**

- A key is never empty
- A key's fraction never ends in `0`
- `A` followed by twenty six `0` characters is a reserved lower bound and is never a
  valid key
- Every queued item in one area has a distinct key

**Rebalance.** Repeated insertion at the same point lengthens keys. When any key in a
list passes twenty characters the list can be rewritten with a fresh evenly spaced
sequence. A rebalance must write one `ITEM_REORDERED` or `AREA_REORDERED` per row, so
that it replays on every device like any other change.

---

## 6. Replay

`ClarityReducer.apply(state, event)` is a pure function. No clock, no randomness, no
identifier generation, no platform calls. Given the same ordered list of events it
produces byte identical state every time, on any machine, in any process.

It never throws. Anything it cannot apply becomes a diagnostic record naming the event
and the reason.

### Invariants

Checked after replay in tests, and available from the debug menu against real data.

1. At most one `ACTIVE` item per live area
2. A `COMPLETED` item is never active
3. Every queued item in an area has a distinct order key
4. A deleted or archived entity never appears in a live projection
5. An event referencing an unknown entity is skipped and recorded, never fatal

### Checkpoints

A checkpoint is a serialized state plus the position it was taken at, where a position
is the `(lamport, originId, id)` of the last event folded in. Cold start loads the
newest checkpoint and replays only what came after it.

**A checkpoint is only valid while the log grows at the end.** Importing or merging a
foreign log can introduce events that sort before the checkpoint, so both paths throw
every checkpoint away and rebuild from event zero. A full rebuild is always correct
and is the only thing the export path uses.

---

## 7. Conflict resolution

These are the rules a second implementation must match exactly, or the two will drift.

### Two active items in one area

The event with the higher `(lamport, originId)` wins. Because replay runs in ascending
order, that is simply the later event. The sitting item goes to the head of the queue
with a fresh order key, and a conflict record is written naming both items, the area,
and the snapshots needed to explain it.

A swap whose `demotedItemId` matches the item currently active is an ordinary swap and
is never recorded as a conflict.

### Edit versus delete

Delete wins. An edit aimed at a tombstoned entity stays in the log, has no effect, and
is recorded as a diagnostic. This holds regardless of which event has the higher
lamport, because a tombstone is permanent.

### Concurrent reorder

Both survive. Fractional keys plus per device jitter mean two devices reordering the
same queue produce different keys and a deterministic merged order.

### Duplicate date keyed rows

For a `PULSE_GENERATED` sharing a `dateKey`, or a `REPORT_GENERATED` sharing a
`weekStartKey`, the higher `(lamport, originId)` wins. The loser stays in the log and
drops out of the projection. An answer attached to a losing pulse has no effect.

### Surfacing

A conflict is never silent, never a technical dialog, and never data loss. The Areas
screen shows one dismissible card in the app's voice:

> While you were away, two things became active in Work. Rewrite the proposal intro is
> active. Call the printer is back at the top of the queue.

Everything that sentence needs is carried on the conflict record itself, so it can be
written without reading a live entity.

---

## 8. The golden fixture

`testdata/golden-log.json` is a canonical stream of 48 events: three areas, a
fortnight of ordinary use, a week closed with a report and an accepted plan, and two
deliberate divergences from a second device. It exercises all twenty four event types
and both conflict kinds.

`testdata/golden-state.json` is the exact state that log must produce, with every map
and list in a fixed order.

Both files are regenerated deliberately and never casually:

```
./gradlew :app:testDebugUnitTest -PregenerateGolden=true
```

A change to either file is a change to this contract. It belongs in a diff someone
argues for, not in a quiet commit.
