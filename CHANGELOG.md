# Changelog

Every released version, newest first. Dates are the day the version was tagged.

The format is one section per release: what a person can now do that they could not
before, what changed about what they already had, and what was wrong and is not any more.

## 0.13.0, 2026-09-02

The polish and detail pass. Eight specialists reviewed the built app, six personas ran ten
tasks each against it, and two focus groups went over the results. 108 specialist findings
and 96 usability findings, merged, ranked and worked down.

### New

- **Undo on completion.** A right swipe commits on a flick, and completing was the only
  frequent act in the app with no way back. Five of six people in usability testing
  completed something by accident. Undo reopens the item into the active slot it left.
- **Undo on archiving.** Archiving takes a whole area and its queue off the home screen in
  one tap and had no way back short of finding the archive screen.
- **The keyboard can finish a capture.** The action key now commits from the title field.
- **Content edge case fixtures.** `-PwriteFixtures=true` writes importable backup files for
  one area, twelve areas, a forty item queue, very long strings and an idle area, so a
  device test of any of those is reproducible.
- **Android Lint runs**, with `warningsAsErrors` and a 68 issue baseline.

### Changed

- **Onboarding.** Three tone variants written and argued; the one that sounds least like
  marketing ships, with one line grafted from another. Beat 4 no longer advances itself
  every 5.5 seconds. Every beat gains a 64dp optical lift. The area picker stops
  re-centering the moment it starts to grow.
- **The import confirm button names the act**: `Merge these events` or `Replace everything
  on this phone`. It used to read `Import from a file`, on a sheet titled `Import from a
  file`, reached from a row titled `Import from a file`.
- **The Trail sets what varies in the strong half.** Four rows in one day print the same
  subject, so the bold column repeated and the action, which is what differed, was the
  faintest text on the screen.
- **A short focus session is described the same way as a long one.** Both terminal events
  render with one verb and the real duration. `Session complete` becomes `Session ended`.
- **The tab bar changes world on the Report** instead of standing on it as the brightest
  object on the page.
- **Every tappable thing responds to a press.** 54 call sites, 15 of which had any
  response.
- **21 Pulse response labels name the day or the load rather than the person.**
  `Not coping`, `Overwhelmed` and `Underwater` were one-tap answers after a single quiet
  day, and the tap is written to the log, rendered in the Trail forever and quotable back
  weeks later.

### Fixed in the second and third consultation rounds

The pass reviewed its own work twice, and both rounds found it had broken things. These
were introduced by this release and fixed inside it.

- Tapping Undo left the snackbar on screen permanently.
- Naming a text field for a screen reader hid the typed text, then the second attempt
  stopped it being a field at all. The working arrangement is now verified on a device and
  recorded in `AccessibilityShapeTest`.
- Appending a custom action inside a semantics receiver compiles and throws when an
  accessibility service reads the tree, which would have crashed the Areas screen for
  screen reader users only.
- The Pulse caption promised the app never asks how you feel; seven Pulse questions do.
- The Trail briefly separated its two halves by color alone.
- Momentum's figures briefly moved out of the serif.
- Six vertical gaps briefly stopped responding to the Text size setting.
- The Report tab bar briefly went from the loudest object on the page to no object.
- Four rewritten Pulse labels read as neutral in a slot that is flagged by position.

### Fixed

- A capture over 200 characters was destroyed on save with no message.
- A half typed capture did not survive process death.
- The keyboard action key was dead at all twelve field call sites.
- An area holding a full queue said `Add your first item`.
- A brand new area said `Last active today`, and three weeks later `Last active 20 days
  ago`.
- The Areas banner spoke from event one: four minutes after onboarding the home screen
  read `A week at the point where nothing is decided` over two empty cards.
- Momentum rendered `Active 0 of last 14 days` under fourteen faded marks on the first
  screen back from a gap.
- `cleanSlate` fired on re-entry, putting `An empty stretch of fourteen days.` in the
  largest serif on the page directly after the screen that deliberately says nothing about
  a gap.
- The erase confirmation said everything on the phone had been erased.
- Typing `delete` failed the delete confirmation, because that sheet pinned case
  sensitivity while the two in Settings did not.
- The area card reached TalkBack as five loose nodes; the code that fixes it was written in
  phase 2 and never called.
- The three swipe actions sat on a node TalkBack never stopped on, so Complete, Swap and
  Delete were unreachable without the gesture.
- Every text field was anonymous to a screen reader.
- Undo was a 24dp unlabeled word on a five second timer with no announcement.
- The tab bar never said which tab was selected.
- Two header glyphs measured 48 by 44, because a fixed height clamps its children.
- The tab crossfade ran one spec on both halves, holding combined opacity at exactly 1.0.
- The entrance faded underneath it and staggered without a ceiling.
- A sentence opening on a spelled number opened lowercase, on 177 corpus lines.
- The tutorial's `Skip` was drawn on top of the settings gear.
- The FAB computed a shape morph on every frame and drew a circle.
- Three type call sites sat below the scale's own floor.
- Four persistence lines announced a personal worst or counted what had been finished
  elsewhere.
- Sheets closed by their own control vanished in one frame after entering on a 300ms lift.

## 0.12.0, 2026-08-29

The visual refresh, plus two acts that were not possible before.

### New

- **Make active**, a one tap button on every queue row. Whatever is active is demoted to
  the head of the queue.
- **Reopen from the Trail.** Tap a completed row and choose to put it back in the queue or
  make it the active one.

### Changed

- The surface ladder widened: card against canvas went 1.202:1 to 1.400:1.
- Eleven type roles on a 1.200 ratio, six spacing values on a Fibonacci ladder, six corner
  radii.
- The area wash became a flat tint. A radial gradient on a card cost it 4 to 6 L\* and put
  it within two points of the surrounding chrome.
- Momentum's two 60 percent area tiles were deleted. They were the loudest objects in the
  app and each carried one bit.
- Settings lost the per-row badge, which set a second left edge, and its sixteen hairlines
  became air.
- Motion became four entrance roles on the Material 3 Expressive spatial springs, replayed
  on every screen entry.

## 0.11.1 and earlier

Thirteen build phases plus 3b, 3c, 12b and 12c. See `docs/BUILD_STATE.md` and the closed
issues for what each delivered.
