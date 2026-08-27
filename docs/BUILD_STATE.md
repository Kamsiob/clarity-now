# Build state

Where the build actually is. Updated at the end of every phase.

**Last updated:** August 27, 2026, after Addendum 01 was recorded and its schema
window closed.
**Version:** 0.3.0, versionCode 300. The addendum work is not yet a release.
**Installed and verified on:** Pixel 8 (`shiba`), over USB.

---

## Phases

| phase | state | issue |
|---|---|---|
| 1. Foundations | done | closed |
| 2. Core mechanics | done | closed |
| 3. Trail | done | closed |
| 3b. Executive function retrofit | not started | #22 |
| 4. Focus sessions | not started | #2 |
| 5. Engine skeleton and simulator | not started | #3 |
| 6. Pulse | not started | #4 |
| 7. Momentum | not started | #5 |
| 8. Snapshots and the Report | not started | #6 |
| 9. Corpus | not started | #7 |
| 9b. Guidance, layer six | not started | #8 |
| 10. First run | not started | #9 |
| 11. Settings, About, data | not started | #10 |
| 12. Widgets and notifications | not started | #11 |
| 13. Ship | not started | #12 |

---

## Addendum 01, executive function support

**Arrived August 27, 2026.** A directive from the owner, out of research and user
panel work on serving people with executive function challenges. It is recorded in
full and almost none of it is built.

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

Only the schema, because the addendum marks Step 2 urgent: a payload change is nearly
free before user data exists and painful afterward. Everything else waits for its
phase.

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

## What is deliberately not there yet

Each of these is a phase, not an oversight. See the linked issue for why.

- The **Focus chip** and **Pulse chip** in the Areas header. They arrive with their
  features in phases 4 and 6 rather than sitting inert. `AreaCardModel` already
  carries `focusMinutesRemaining`, so the in session card state is plumbed and
  waiting.
- The **settings glyph** in the Areas header, which arrives with phase 11.
- The **archived areas view**, issue #15. Archiving works and removes the area from
  the list, but there is currently no way back. Worth knowing before archiving
  anything you care about.
- **Momentum and Report** render one honest line rather than a skeleton, issue #16.
  The Trail no longer does.
- The **weekly banner** on Areas, because its sentence comes from the engine, which
  does not exist until phase 5.

## Known defects and open questions

- **WorkManager pulls three permissions into the merged manifest**:
  `FOREGROUND_SERVICE`, `WAKE_LOCK`, `RECEIVE_BOOT_COMPLETED`. None is a network
  permission and the no internet gate passes on both variants. But
  `MASTER_BUILD_PROMPT` section 18 says no permission beyond notifications is in
  scope for v1, and WorkManager is required by section 3 for the Pulse reminder and
  the widget refresh. **This is the builder's call at phase 12.**
- `VIBRATE` is declared, and has to be, because `design-v3.md` section 9 specifies
  sixteen haptic events. It is a normal permission with no prompt.
- Material's `MotionScheme` is internal in material3 1.5.0-alpha26, so Clarity's
  springs cannot be injected into Material components yet. Issue #13.

---

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
