# Changelog

Every released version, newest first. Dates are the day the version was tagged.

The format is one section per release: what a person can now do that they could not
before, what changed about what they already had, and what was wrong and is not any more.

## 0.15.0, 2026-09-03

The appeal pass. Four research agents and two usability walkthroughs went over the built
app against the owner's report that it is at six out of ten and not fun to use. The
reasoning for every decision is in `DECISIONS.md`.

### New

- **You can make a second area.** Both doors to the new area sheet were gated on having
  none, so a person who finished setup with three areas had three forever. There is an
  `Add an area` row at the foot of the list, `A new area` in the filing chooser, and a
  chip in the capture sheet.
- **The app has an opening.** The launch window is near white and onboarding is near
  black, so the first frame anybody saw slammed from one to the other. The room dims from
  the launch color now while the mark assembles one card at a time, which is the mechanic
  it depicts: one card in front, two waiting behind it.
- **Every verb an area has, in one menu.** A long press on a card opens it. `design-v3.md`
  10.3.1 called this mandatory and the handler opened the detail sheet instead.
- **Reordering without dragging.** `Move to the top` in that menu, which is the single
  pointer alternative WCAG 2.2 SC 2.5.7 requires and which reordering did not have.
- **The Pulse explains itself** where a person meets it, including the sentence nothing
  anywhere said: neither answer is the right one.

### Changed

- **The tab bar.** 68dp, four equal slots, a 26dp glyph in its own indicator, and all four
  destinations named again. Nothing moves but the indicator; the old bar pushed the other
  three sideways on every tap.
- **Capture fits above the keyboard.** The sheet was four stacked fields, so the line
  saying where a thought was going and the button that puts it there were both under the
  IME while you typed. The optional fields are behind one disclosure, and every
  destination is a chip, so picking an area is one tap.
- **Beat 1 says what it is**: `An example: four areas.` The sentence under the demo used
  two coined nouns against shapes nothing had named.
- **A rule between the anchors and the areas**, at one device pixel, fading out at both
  margins rather than stopping.
- **The Daylight world carries area color again.** The card wash goes to 7 percent, the
  top of the band the spec already gives it, and shadows stop being pure black.
- **Entrances fire once per session again**, per 8.4. Returning to Areas had been
  re-assembling the whole screen over 748ms, every time.
- **`Manage this area`** rather than `Area` over the archive and delete rows, with a
  labeled `Edit this area` beside them.

### Removed

- **The permission card in Settings.** The claim is right and the placement was wrong. The
  build gate, the privacy sheet one row above and `PRIVACY.md` all still carry it.
- **The privacy policy's `Children` clause.** This app collects nothing from anyone, which
  the paragraph above it already says.
- `PulseChip.kt`, 108 lines nothing called.

### Fixed

- **The tutorial's spotlight lit the whole bottom right corner** instead of the plus
  button, because the modifier sat before the padding rather than after it.
- **A cleared area asked for its first item**, directly above `Last active 2 days ago`.
- **The promotion had no haptic.** One was defined and written and never called.
- **`Nothing is hidden anywhere else.`** False about the inbox, the archive, the area
  editor and reordering, and the last thing the tutorial said.
- **`Names and colors can come later.`** Read as covering more areas, which was the one
  thing that could not.
- **The onboarding Pulse sample looked live.** Two testers believed they had answered a
  question. The sample answers are no longer shaped like buttons.

## 0.14.0, 2026-09-02

The deferred issue pass. The polish pass ended with eight issues logged as needing a
decision rather than a detail fix. This closes all eight, and two older confirmations that
were waiting for something real to measure against.

### New

- **Past reports.** Nothing wrote `REPORT_GENERATED`, so the history page told a person
  every report the app writes is kept there and could never contain one. One report per
  calendar week is filed now, with the line it led with, and the page names the boundary
  where the record starts rather than appearing to have lost the weeks before it.
- **The home screen says how much is waiting.** With eleven things behind Work the card
  was identical to the day there was one, while the All Areas widget printed the count on
  the home screen. It is plain text in the card's existing line: no badge, no dot, no
  color.
- **A focus session starts from the item you are looking at.** The area sheet offers it,
  and the session length is on the Focus surface rather than four screens away in
  Settings. Arranging five minutes took eight interactions and takes three.
- **The first step appears at the two moments it was written for**, choosing a session and
  running one, per `ADDENDUM_01` 4b. It was on the card, in the area sheet and on a widget,
  and on neither Focus screen.
- **Back previews where it goes** on the Focus surface, Settings, the archive and both
  history pages. Three sites are deliberately left without a preview and say why at their
  own declaration.

### Changed

- **Onboarding shows the queue instead of asserting it.** The demo cards carry the same
  waiting line the real card carries, and it falls by one as the next item takes its place.
- **The Report's onboarding caption says what the app will not do**: `It notices what
  happened and says it plainly. It does not make anything up.` That is one of the five
  things 13.1 says onboarding must convey, and it had no carrier anywhere in the sequence.
- **Accepting a plan leaves a record you can return to.** The Trail row said `Accepted one
  thing` with no subject; it names the line now. Nothing anywhere gains any notion of
  whether it was acted on, and a test holds that.
- **A color swatch answers a thumb.** The app's 6 percent ink press is invisible when the
  thing being pressed is itself a color. Swatches draw a ring in the ink measured against
  that exact color, and the mood pills thicken the ring they already own.

### Fixed

- **`Nothing here can break.` stayed on the screen through the whole of beat 4**, drawn
  over the Pulse sample's second answer, for anybody who tapped through beat 3 rather than
  waiting. Found by walking the sequence on the phone rather than in a diff.
- **A closing line could come back a week later.** A closing with no plan in it was
  recorded nowhere, so the ninety day exclusion could never see one. Eleven persona years
  now produce no closing repeat inside ninety days at all.
- **A Report named two different weeks in two places.** The Trail row named the calendar
  week and the page named the seven days described. Both name the described week now.

### Confirmed, after waiting for something real to measure

- **Tabular figures at weight 250**, on the focus countdown, measured across six
  consecutive frames on the device rather than judged by eye.
- **Hanging punctuation** was judged against the real corpus and declined. Nothing the
  engine can compose begins with a punctuation mark, and there is not one quotation mark
  in 2,357 authored report lines.

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
