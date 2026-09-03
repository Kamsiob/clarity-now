# Changelog

Every released version, newest first. Dates are the day the version was tagged.

The format is one section per release: what a person can now do that they could not
before, what changed about what they already had, and what was wrong and is not any more.

## 0.17.0, 2026-09-03

A test user was handed the last build and said: "It looks nothing like a to-do app. It's
not designed around what it's supposed to do, it's not intuitive, and you don't know what
you're looking at." They were right, and almost none of it was taste. `DECISIONS.md` has
the whole argument and the sources.

### New

- **Every area card has a circle you tap to finish what is on it.** Until now completing
  was reachable only by a right swipe, a long press or a sheet, and all three are
  invisible. The words `task`, `to-do` and `done` appeared **zero times** in any string a
  person could read. A tap now writes immediately, the ring fills, and the next item in
  that area rises into the slot: the app's whole idea, in one gesture, where it can be
  seen.
- **Momentum says its own name.** The word appeared nowhere in the running app except the
  tab chip, and tab labels drop one text size above default, so an enlarged-type user
  navigated to an untitled page of numbers through an unnamed glyph. It also says what it
  counts, permanently rather than only while empty.

### Changed

- **`3 waiting` is now `3 more`.** In this product category `waiting` is a term of art
  meaning **blocked on somebody else**. It comes from Getting Things Done, Things ships a
  support article defining it that way, and Todoist uses `@waiting` as its canonical
  example. Here it meant the opposite: work available right now that simply is not first.
  That is the worst kind of wording error, because it produces a confidently wrong reader
  rather than a confused one, and it would never have surfaced in testing.
- **The swipe face reads `Delete area`.** It read `Delete`, on a card whose other action
  completes an item. All three usability personas swiped what they read as a task, tapped
  it, and were asked to destroy an area.

### Fixed

- **Completing an item crashed the app.** The promotion animation raised the incoming title
  with negative padding whenever its spring overshot, which is what that spring is for.
  Nothing had found it because completing was hard to reach.
- **The tutorial teaches a gesture that does not work.** Step two spotlights the first area
  card and says "swipe right to complete it", and on a first run every card is empty, so
  the right swipe is clamped to zero travel and the card does not move a pixel. The card
  now has a control that does work, and the step points at it.
- **The one sentence explaining what a Pulse is had never been drawn.** Its guard could
  only be satisfied after an answer had already been given.
- **`Replay the tour` did nothing inside its own session.**
- **`Skip setup` threw away a first item you had typed**, under a comment asserting that
  nothing was typed on that path.
- **The completion control's first touch target was 34dp**, because a 48dp control inside a
  34dp gutter is silently made smaller. The first tap fell through to the card underneath.

## 0.16.0, 2026-09-03

The plate, the swipe and the manage room. Six things the owner reported on the phone,
plus an audit that ran across the whole app while they were being fixed. The reasoning
for every decision is in `DECISIONS.md`.

### New

- **Manage areas**, behind a new glyph in the header between the archive and the gear.
  Two labeled arrows on every row move an area one place up or one place down, which is
  the single pointer alternative WCAG 2.2 SC 2.5.7 wants for the drag that was the only
  way to reorder. Tapping a row opens the same menu the long press opens on the Areas
  list, so nothing about an area has a second copy of itself here.
- **The last area can be deleted.** It was the one card in the app that refused, which
  meant the first card a new person tries a left swipe on revealed nothing at all.
- **The Report can say how many weeks it is based on.** Three of the six basis lines in
  the corpus read `and {m} weeks of data`, and `{m}` was bound to a measure that nothing
  declared, so those three dropped out of the bench on every report ever written.
- **Rebuild the cache from the log**, in a debug build. `MASTER_BUILD_PROMPT.md` 5.4 asks
  for it, this repository said in three places that it existed, and nothing called it.

### Changed

- **The Areas screen has a top.** The wordmark row, the weekly banner, two elevated white
  pills, a chip and a hairline rule are replaced by one full bleed parchment plate with a
  hard top and a hard bottom, carrying the date, three bare doors and the engine's
  sentence. Its one separation device is the ground change, which is what lets the rule
  go: that line existed only because the two regions it divided were made of the same
  material.
- **The app's name is off its own home screen.** It was the largest type in the product,
  above a tab bar whose selected item already says `Areas`. The date is there instead.
- **A waiting Pulse changes its label as well as growing a dot.** The string for it has
  existed since phase 6 and was referenced by nothing, so the only signal was color.
- **The area card has a right edge.** The queue count moved from the bottom left, where it
  was the fourth stacked caption in the same size and color as the two above it, to the
  trailing end of the identity row.
- **The first step is set at 15 rather than 12.5**, the size of a timestamp, for the line
  that is supposed to make an item startable.
- **The Areas empty state is left aligned** on the same measure as everything else, and
  the plus button is not drawn while it is showing, because both named the same action.
- **The re-entry screen's title** is an invitation rather than an observation.
- **Onboarding no longer promises the Pulse can be turned off.** Only its reminder can,
  which is what the Settings copy has always said correctly.

### How it looks

The owner's verdict on the build before this one was that it was uglier than ever, dead
and boring. Four research passes went over open source design work to find out why, and
the answer was measurable rather than a matter of taste.

- **The four light surfaces were on four different hues.** Measured in OKLCH: the canvas
  at 286 degrees, the card at 85, the raised chrome at 95, the parchment at 103. **202
  degrees of spread**, where every design system checked holds its neutral ramp inside
  about twenty. Surfaces on different hues are not one material, which is the mechanical
  reason the cards read as blocks dropped onto a page rather than as raised parts of one.
  The whole light world is Flexoki now, by Steph Ango, MIT licensed and credited under
  Settings, Open source licenses. The spread is 3 degrees.
- **The area colors were Tailwind's defaults**, on a canvas that was Tailwind `zinc` to a
  decimal place of hue. That pairing is the most replicated palette in template interfaces
  since 2021. All forty eight are Flexoki accents now, and by the numbers the new set
  desaturated in calm mode is more separable than the old set at full strength.
- **The date is the home screen's masthead**, set in the app's own serif at its headline
  rank. The version before this one removed the wordmark, correctly, and put a small grey
  sans dateline in its place, which took the only serif off the screen. Both surfaces this
  app does well, Momentum and the Report, open with a large serif line on a bare ground.
- **The type scale had four size collisions**, and its weight axis could not separate
  anything: `bodyStrong` and `title` were both 18sp, and 600 against 700 in this face is
  0.126sp of ink. Fourteen named roles rendered as about seven. Worse, Android's Bold text
  setting adds 300 and clamps, so with it on **six roles became one weight**. Three weights
  now, no size collisions, and the hierarchy survives that setting.
- **The serif was drawn 11 percent smaller than the sans at nominal parity**, because
  Newsreader's x-height varies with its optical axis and Hanken's does not. The serif roles
  are sized for what they measure, not for what they are called.
- **The icons went from weight 500 to weight 400.** At 24dp beside 15sp text a 2.275dp stem
  put every icon at the optical weight of the words next to it.
- **The ground has a center.** Two and a half percent of lightness in one radial pair
  behind the Areas list. Emptiness on a flat field reads as vacancy; the same emptiness on
  a field with a center reads as room.

### How it moves

- **Three screens stopped appearing between two frames.** The archive, manage areas and
  About had no transition at all.
- **A press took 293 milliseconds to answer.** The press scale ran on the travel spring,
  which reaches its value at 193ms, on top of the 100ms the platform withholds so it can
  tell a tap from a scroll. The band a press should answer in is 100 to 160.
- **`springSnappy` goes back to what `design-v3.md` specifies.** It had been changed to a
  bouncier spring than the design asks for, on the smallest and most frequent controls in
  the app.
- **Reduced motion stops deleting the reading order.** It zeroed the entrance stagger, and
  a stagger carries no displacement, so there was nothing about it to reduce. The guidance
  from MDN, WebKit and Apple is unanimous that the instruction is about magnitude, not
  existence.

### Fixed

- **Settings crashed on open** after the palette changed, because a top level value looked
  up two moods by name that no longer existed and threw inside a static initializer. A test
  now holds every mood name the app writes down against the palette that has to contain it.
- **Dark area labels were solved against a ground a shade lighter than the deepest one they
  are drawn on.** One percentage point, invisible until a color landed inside it.
- **Onboarding ran on every launch.** `Skip setup` fired an asynchronous write and then
  tore down the composition that owned it, so the write was canceled every time.
- **`Replay the tour` and `Replay the welcome` did nothing.** The first run gate decides
  once per process and latches, which it has to; it now also listens for somebody asking
  for the tour again.
- **The swipe opened a hole in the page.** Each face was a fixed 66dp block, so any drag
  past that showed bare canvas between the face and the card, and the commit threshold is
  three times that far. The revealed strip follows the card now.
- **The app said `Item deleted` while the item was still on the screen.** Nothing is
  written until the five second window closes, deliberately, so the sentence is
  `Deleting item` and it is true.
- **Importing a file could take the app down.** The one button that can replace an entire
  history had no error handling at all; a failure now says so, and says that nothing on
  the phone changed, which is true because the whole ingest is one transaction.
- **The tab bar was unnamed at large text sizes**, where its labels are not drawn.
- **A corpus observation was pasted into `strings.xml`**, so the app could have told
  somebody the same sentence twice while believing it had said it once. A test now holds
  every interface string against all three corpora.
- Five dead or misplaced explanations, a dead glyph table, an unused shape token and a
  wash 6 percent over its own ceiling in dark.
- **`verifyClarity` could pass over a `strings.xml` it had never read**, because nineteen
  tests open that file at runtime and Gradle could not see it. The same gap was found and
  fixed for the corpus files in phase 9 and not for the rest.
- **`audit.py` reported CLEAN over ten documents it never opened.** It read the twelve
  root markdown files and none of `docs/`.

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
