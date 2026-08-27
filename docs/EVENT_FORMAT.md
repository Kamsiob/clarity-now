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
item for item events, the session, pulse, report or plan for theirs, the setting key
for `SETTING_CHANGED`, and the date key for `APP_OPENED`. It is derived rather than
authored, so it can always be recomputed if it is ever wrong. The last two are ids only
in the sense that they are the one identifier those events have, and neither names a
row in any table.

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

### The version number

`schemaVersion` is still `1` after the change described in section 4, which added four
types, renamed one, and added fields to three payloads. That is deliberate rather than
an oversight. A version number earns its keep when a reader has to tell two shapes
apart and accept both, and no reader here does: every field that change added is
optional and defaults to null, an absent key reads as that default, and unknown keys
are ignored, so a log written before it replays to the same state after it. The one
type that was renamed is written by a phase that has not shipped, so no log anywhere
contains the old name. Moving the number with nothing to distinguish would spend the
signal on the occasion that did not need it.

**A reader must therefore accept an absent optional field as null rather than refusing
the event.** This implementation always writes nulls explicitly, so reading a log it
wrote never exercises that path, and reading one written by an older or smaller
implementation does. Writing them explicitly and reading their absence tolerantly are
both required and they are not the same rule.

Room's database version is a different number that tracks the cache tables described in
section 1. It moves for storage reasons that have nothing to do with this contract, and
a second implementation neither sees it nor needs it.

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

Twenty eight types. Every payload carries full before and after values, so a replay
reconstructs state without reading any other table.

A field marked nullable below may be null and must be read as such. Everything
unmarked is required, is never written as null, and an event missing one is malformed
rather than partial.

Display snapshots are a separate question and they are not universal. Eight of the
twenty eight carry enough of one to name both the subject and the area of a Trail row
with no lookup at all: the five area events that carry a name, and `ITEM_ADDED`,
`ITEM_PROMOTED` and `ITEM_COMPLETED`, which carry a title and an area name together.
The other twenty carry a partial snapshot or none. Those resolve their display
values by folding the log to the instant of the event, which is what
`domain.query.TrailQueries` does. Neither path reads a live entity table, so a Trail
entry from a year ago still renders the name an area had at the time.

**Four of the twenty eight arrived in one change, along with one rename and new fields
on three payloads.** `ITEM_FILED`, `ITEM_ESTIMATED`, `FOCUS_EXTENDED` and `APP_OPENED`
are written by phases of this app that have not shipped yet, and they are in the
catalog anyway, because an event payload is nearly free to change before a real log
exists and expensive afterward. If you are working from an older copy of this document,
section 4 is the part that moved. Nothing already in the catalog changed shape except
`ITEM_ADDED`, and nothing a log could already contain changed meaning.

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
| `ITEM_ADDED` | `itemId`, `areaId` (nullable), `title`, `note` (nullable), `orderKey`, `areaNameSnapshot` (nullable), `estimateMinutes` (nullable), `firstStep` (nullable) |
| `ITEM_FILED` | `itemId`, `areaId`, `orderKey`, `areaNameSnapshot` |
| `ITEM_EDITED` | `itemId`, `previousTitle`, `newTitle`, `previousNote` (nullable), `newNote` (nullable) |
| `ITEM_ESTIMATED` | `itemId`, `previousEstimateMinutes` (nullable), `newEstimateMinutes` (nullable) |
| `ITEM_QUEUED` | `itemId`, `areaId`, `orderKey`, `previousStatus` |
| `ITEM_PROMOTED` | `itemId`, `areaId`, `previousStatus`, `demotedItemId` (nullable), `demotedToOrderKey` (nullable), `titleSnapshot`, `areaNameSnapshot` |
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
immediately, which is written as `ITEM_ADDED` followed by `ITEM_PROMOTED`. An item
added with no area is created `QUEUED` as well and is never promoted, for the reason
below.

Only an active item can be completed. A completion aimed at a queued item is recorded
as a diagnostic and has no effect.

**A null `areaId` on `ITEM_ADDED` means the item is unfiled.** It is a real item: it
exists, it can be edited, estimated and deleted, and it holds an order key of its own.
What it does not have is an area, and every rule in this format that is scoped to an
area passes it by.

- An unfiled item is never `ACTIVE` and never `COMPLETED`. Those two states are what it
  means to be the one thing happening in an area, and there is no area
- It is in no area's queue, so it shares an order key space with nothing and is counted
  by no per area aggregate
- It is outside every fact computed about an area
- `ITEM_FILED` is the only way it gets an area. There is no unfile and no move
- Tombstones apply to it exactly as they apply to anything else

It is a null rather than a reserved inbox area, and the difference is worth stating
because the reserved area is the obvious answer. A synthetic area satisfies every
existing invariant with no nullable field and no migration, and then leaks into every
place areas are enumerated: the area list, the widgets, the per area counts, the color
palette, archiving, deleting, and every area scoped rule in the reflective layer. Each
of those becomes a special case a later implementer has to know about, one of them
eventually gets missed, and the failure is a fabricated area name printed inside a
sentence about a person's own life. A null is one thing to remember, and a type system
raises it at every call site rather than waiting for a rendered line to print it.

`ITEM_FILED` sets the area and the order key and leaves the item `QUEUED`. **It never
promotes.** Filing is bookkeeping and promotion is a choice about what to do next, and
folding the two together would let the safest possible act, writing something down for
later, displace whatever the person is currently working on. It carries
`areaNameSnapshot` so a history row can name the destination with no lookup, and
deliberately does not carry the title, which is resolved by folding the log the way
every other partial snapshot is. There is no `previousAreaId`, because that value is
known without storing it: it is always null.

A filing aimed at an item that already has an area is recorded and has no effect, and
so is one naming an area that is unknown, deleted or archived. The refusal leaves the
item unfiled rather than losing it, which is the better of the two failures. An item
that stays in the inbox is visible and can be filed again; one filed into an archived
area is neither.

**`estimateMinutes` and `firstStep` are optional and stay optional.**
`estimateMinutes` is a guess in whole minutes, made at capture or not at all.
`firstStep` is one line naming the first physical action, which is a different sentence
from the title: "Rewrite the proposal intro" is a wall, and "Open the doc and read what
is there" is not. Neither field is ever required and neither is ever prompted for.

`ITEM_ESTIMATED` records a revision made after capture, so that changing a guess never
rewrites what the person originally wrote down and the sequence of guesses stays
readable in the log. Both of its values are nullable in both directions: null to a
number sets an estimate, a number to null clears one, and a number to a different
number revises one. The new value is what a projection takes, and the previous value is
the before half of the pair this format carries everywhere and is never consulted, in
the same way `ITEM_EDITED` never consults `previousTitle`.

**An estimate is never compared to an actual in anything a person reads.** The log
holds both, because the moment an item became active and the moment it completed are
already in it, so the arithmetic is available to anyone who replays this format. The
constraint is on what may be said with it: a tendency across many items can be stated,
and a difference on one item cannot. That rule belongs to the language layer rather
than to this document, and it is written here because this is where a second
implementation learns that both numbers exist.

### Focus

| type | payload |
|---|---|
| `FOCUS_STARTED` | `sessionId`, `areaId`, `itemId`, `plannedSeconds` |
| `FOCUS_COMPLETED` | `sessionId`, `actualSeconds` |
| `FOCUS_ENDED_EARLY` | `sessionId`, `actualSeconds` |
| `FOCUS_EXTENDED` | `sessionId`, `addedSeconds`, `newPlannedSeconds` |

A session has exactly one `FOCUS_STARTED`, any number of `FOCUS_EXTENDED`, and at most
one terminal event. **At most one, not exactly one.** A process killed mid session
leaves a started session with no ending at all, so started does not equal completed
plus ended early, and that is a legal state rather than a defect. Nothing may infer an
outcome by subtracting one count from another.

Ending a session early is neutral. A session ended early is a completed short session,
and fourteen minutes is fourteen minutes. Nothing treats it as a failure and no
sentence shown to a person names it as one.

**`FOCUS_ENDED_EARLY` was called `FOCUS_ABANDONED` until the Addendum 01 schema
change.** If you are reading an older copy of this document, they are one type: the
payload is unchanged, and no log anywhere contains the old name, because the phase that
writes it has not shipped. The rename happened while the schema window was open, and
this document is the reason it happened at all. A type name here is an instruction to
whoever builds the second implementation about what the concept means, and the old one
taught the wrong thing. It invites a screen that says the word, a rule that counts
abandonments, and an outcome inferred by subtraction, which is the one inference the
paragraph above forbids. `DECISIONS.md` C6 records the decision and the argument on
both sides.

`FOCUS_EXTENDED` adds time to a running session without ending it and without starting
a new one. `newPlannedSeconds` is the absolute value after the extension and is what a
replay applies. Applying `addedSeconds` as a delta instead would make the merged result
depend on the order two devices extended in, and the absolute figure is the one the
person was actually shown. `addedSeconds` records the size of the gesture, and it is
what a history row reads, so a line reading "Added ten minutes" comes out of the log
rather than out of a subtraction between two events that may not both be loaded. An
extension aimed at a session that has already ended is recorded and has no effect.

### Pulse

| type | payload |
|---|---|
| `PULSE_GENERATED` | `pulseId`, `dateKey`, `family`, `escalationStage`, `register`, `variantKey`, `renderedObservation`, `renderedQuestion` (nullable), `factSnapshot`, `reflectionPeriod`, `subjectId` (nullable), `subjectKind` (nullable) |
| `PULSE_ANSWERED` | `pulseId`, `responseKey`, `responseLabel`, `responseIsPositive`, `subjectId` (nullable), `subjectKind` (nullable) |

`dateKey` is `yyyy-MM-dd` in the device's local zone. `reflectionPeriod` is
`YESTERDAY` or `TODAY_SO_FAR`. `factSnapshot` is a flat map of strings recording what
the engine saw when it spoke.

`responseLabel` is stored verbatim, so a later callback can quote what the person
actually saw rather than a label reworded in a newer version of the app.

The generation events are also where the engine's own history lives. Variation
history, escalation stages and the last family shown are all rebuilt from
`PULSE_GENERATED`, never read from device preferences, because preferences do not
merge and two devices would silently disagree about what to say next.

`subjectId` names the area or item an observation was about, and `subjectKind` says
which of the two it is: `AREA` or `ITEM`. Both are null for a family that has no
subject. **The kind is stored rather than inferred.** An id on its own cannot be
resolved back to what it names, and testing it against the known area ids is a guess
that works right up until the area it names has been deleted.

The pair is on `PULSE_ANSWERED` as well as on `PULSE_GENERATED`, which duplicates it,
and that is deliberate. It could be joined through `pulseId`, and a join is exactly the
shape that has already produced one defect in this app: a history row resolved its item
through the event that started the session, and rendered blank whenever that event fell
outside the loaded page. The observation family that compares what a person said about
an area against what they did in it is the most important thing the reflective layer
says, and it must not rest on a lookup that can miss. One duplicated string is the
cheaper side of that trade.

### Report and guidance

| type | payload |
|---|---|
| `REPORT_GENERATED` | `reportId`, `weekStartKey`, `headlineKey`, `renderedSections`, `factSnapshot`, `headlineVariantKey` (nullable) |
| `PLAN_OFFERED` | `planId`, `weekStartKey`, `frameKey`, `cueKey`, `actionKey`, `familyKey`, `subjectId` (nullable), `offeredLine`, `committedLine`, `resolutionFactRef` |
| `PLAN_ACCEPTED` | `planId` |

`renderedSections` is a list of `{ sectionKey, sidehead, text, familyKey, variantKey,
escalationStage, register, subjectId, subjectKind }`, where the last two are nullable
and the rest are not. `weekStartKey` is the `yyyy-MM-dd` of the Sunday that starts the
week.

**The keys beside the rendered text are what make the selector's own history
derivable, and the sentence alone is not enough.** Choosing what to say next excludes
any variant used in the last ninety days, applies a flat cooldown per family, and caps
the family that acknowledges a hard stretch at six weeks. None of those three can be
computed from rendered prose: two variants of one family read as two unrelated
sentences, and a strongly worded line and a mild one of the same family read as
unrelated as well. The keys are what the rules are actually stated in.
`headlineVariantKey` exists for the same reason. `headlineKey` names the family and the
exclusion needs the variant, and without it the headline is the one rendered line in
the app that can repeat itself word for word, in the largest type on the screen.

The subject and variant fields described here and in the Pulse section above are a
proposed shape rather than a settled one. They answer a question this project tracks as
issue 19, the owner may still adjust them, and anything that moves there moves this
contract with it.

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

### Presence

| type | payload |
|---|---|
| `APP_OPENED` | `dateKey` |

**`APP_OPENED` is a presence marker for gap detection, and it is nothing else.** It
records the local `yyyy-MM-dd` and no other value: no time, no duration, no count, no
session length. It is written at most once per local calendar day, on the first
foreground of that day. It exists so that a long absence can be noticed without any
tracking at all, because someone returning after a fortnight away should be met
differently from someone who was here yesterday, and a date key per day is the smallest
record that can answer that question.

Three rules come with it, and each one is load bearing.

**It is never counted as activity, by anything.** Every count of what a person did
excludes it, alongside the three events the engine writes for itself. An implementation
that counted it would report someone who opened the app each morning for a fortnight
and touched nothing as active on all fourteen of those days; it would turn a line
reading `{n} of seven days had activity` into a count of app opens presented as a count
of activity; and it would put the observations about a quiet day nearly out of reach.
All three of those numbers would look entirely plausible on screen, which is why the
rule is stated here rather than left to be rediscovered. The event exists to detect an
absence and must never become the mechanism that measures a presence.

**It renders no row in any history view**, and it is excluded from any per day event
count such a view shows. A daily "opened the app" line would be noise in a
chronological log, and a visible tally of presence besides.

**It projects no state.** A replay applies it as a no operation, so folding a log that
contains these events and the same log with them removed produces identical state. It
is read from the log directly, as the set of distinct date keys, which is also why two
devices writing the same date key needs no conflict rule: a set does not care how many
times a member was added.

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
6. An item with no area is never `ACTIVE` and never `COMPLETED`

Rules 1 and 3 are scoped to an area, which is not the tautology it sounds like. An
unfiled item has no area, so it sits in no area's queue and is the one active thing
nowhere, and neither rule has anything to say about it. Rule 6 is what stands in their
place, and it is written down rather than left implied, because an invariant that is
silently conditional is an invariant nobody can check.

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

### Two devices file the same item

The first filing in total order wins. The second stays in the log, has no effect, and
is recorded as a diagnostic. This is not the higher `(lamport, originId)` rule the
cases above use, and the difference is deliberate: there is no unfile and no move, so a
second filing is either a duplicate delivery or a mistake, and under both readings the
right answer is the one both devices already agree on. Moving an item between areas is
a different act that this format does not express, and if it is ever added it will be
its own event rather than a second `ITEM_FILED`.

### Duplicate date keyed rows

For a `PULSE_GENERATED` sharing a `dateKey`, or a `REPORT_GENERATED` sharing a
`weekStartKey`, the higher `(lamport, originId)` wins. The loser stays in the log and
drops out of the projection. An answer attached to a losing pulse has no effect.

`APP_OPENED` shares a `dateKey` across devices routinely and is deliberately outside
this rule. It projects nothing, so there is no row for a winner to occupy, and it is
read as a set of dates in which a repeat is not an event.

### Surfacing

A conflict is never silent, never a technical dialog, and never data loss. The Areas
screen shows one dismissible card in the app's voice:

> While you were away, two things became active in Work. Rewrite the proposal intro is
> active. Call the printer is back at the top of the queue.

Everything that sentence needs is carried on the conflict record itself, so it can be
written without reading a live entity.

---

## 8. The golden fixture

`testdata/golden-log.json` is a canonical hand written stream: three areas, a fortnight
of ordinary use, a week closed with a report and an accepted plan, and two deliberate
divergences from a second device. It exercises all twenty eight event types and both
conflict kinds, and it covers the transitions nothing else in it would reach, which are
an item captured with no area, that item filed later, an estimate revised after
capture, a running session extended, and a day marked as opened.

`testdata/golden-state.json` is the exact state that log must produce, with every map
and list in a fixed order.

Both files are regenerated deliberately and never casually:

```
./gradlew :app:testDebugUnitTest -PregenerateGolden=true
```

A change to either file is a change to this contract. It belongs in a diff someone
argues for, not in a quiet commit.

**The regeneration that carried the Addendum 01 schema change is that diff, and this is
the argument for it.** The four new types, the fields added to `ITEM_ADDED`, and the
rename in the Focus section all come from Addendum 01 Step 2, which asks for them ahead
of the phases that use them on the grounds that an event payload is nearly free to
change before real user data exists and painful afterward. `DECISIONS.md` C6 records
the owner's decision on the rename, and why a type name in this document is not an
internal detail. `DECISIONS.md` C9 is why all of it landed in one regeneration rather
than two: the subject and variant fields on the Pulse and Report payloads were an open
question of their own, they touch the same fixture, and one argued diff gets more
scrutiny than the second of two.
