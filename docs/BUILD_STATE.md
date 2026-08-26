# Build state

Where the build actually is. Updated at the end of every phase.

**Last updated:** August 26, 2026, end of phase 2.
**Version:** 0.2.0, versionCode 200.
**Installed and verified on:** Pixel 8 (`shiba`), over USB.

---

## Phases

| phase | state | issue |
|---|---|---|
| 1. Foundations | done | closed |
| 2. Core mechanics | done | closed |
| 3. Trail | not started | #1 |
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
- **Momentum, Report and Trail** render one honest line rather than a skeleton,
  issue #16.
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
