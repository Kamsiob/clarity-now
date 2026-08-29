# Component and Layout Specification

**What this is.** One buildable specification for form, proportion, composition,
spacing, alignment, scale and variation, covering every control, every container and
every page in Clarity Now. It resolves six independent design reports into a single
set of numbers. Where those reports disagreed, this document picks one and gives the
reason on the same line.

**What it is not.** It is not about material, depth, motion, type family, color or the
anti-slop line, except where a form decision cannot be stated without touching one. It
adds no data, no screen and no capability. Every fact drawn on a page below already
exists in a view model that ships today.

**Authority.** This file is a **proposal against `design-v3.md`**, not a replacement
for it. Section 12 lists every stated number and rule it supersedes, quoted, with the
reason. Until the owner accepts those, `design-v3.md` still wins on anything visual and
`MASTER_BUILD_PROMPT.md` still wins on behavior and data. Nothing here changes behavior
or data. Nothing here was written into `design-v3.md`.

---

## 0. How to read the numbers

**The reference device is the Pixel 8:** 1080 by 2400 physical pixels at density 2.625,
so the canvas is **411.4 by 914.3dp** and 1dp is 2.625px. The narrowest supported phone
is 360dp wide and every width below is checked against it.

**The content measure is the screen width less two 20dp margins.** On the reference
device that is **371.4dp**. On a 360dp phone it is **320dp**. Three of the six reports
were written at one of those two widths and two of them disagreed by 51dp, which is 16
percent. **No width in this document is written as a constant.** Every one is either a
number of grid columns or the measure itself.

**Vertical positions in a wireframe are measured from the top of the safe drawing area**,
which is the bottom of the status bar inset, and never from the physical top edge. Two
reports derived that inset independently and got 30dp and 50dp. The code already reads
it from `WindowInsets.statusBars`, which is the correct answer and makes the question
moot, so it is never a number here.

**Every dimension is dp, every type size is sp.** A value marked **fixed** does not
scale with the text size. A value marked **scaled** goes through `ClaritySpacing.scaled`.
The distinction is `ClaritySpacing`'s own doctrine and it is untouched: a gap between two
pieces of type scales, a fingertip and a grip and a horizontal inset do not.

**The verdict this answers.** The owner said the app is ugly, boring and lifeless, and
then corrected a first pass that had read that as polish: the charge is the way the
buttons are, the way the blocks are, the way the whole thing fits together, and the
spacing, alignment, size, variation and layout of every element on virtually every page.
So nothing below is a finish. It is the forms.

---

# PART A. THE SCALE

Everything else in this document is expressed in these four scales, so they come first.

## A.1 The unit, and the rule that relates the three ladders

> **The unit is 4dp. Every line height is a multiple of 4sp, every spacing token is a
> multiple of 4dp, and every control height is a multiple of 4dp. Therefore every
> baseline in the app sits on one ladder and every gap is a whole number of line slots.**

| ladder | unit | ratio | steps | range |
|---|---|---|---|---|
| type | a 4sp line box | **1.200** | 11 roles with 3 deliberate holes | 5.1 to 1 |
| spacing | 4dp | **1.50 to 1.67** | 6 tokens | 13 to 1 |
| grid | 4dp at the reference width | 6 columns | 5 spans | 6 to 1 |

The three ratios differ on purpose. Type is read foveally and small differences register,
so it takes the tightest ratio. Spacing is read peripherally and needs a larger delta,
so it takes a wider one. A column span is read as structure, so it takes the widest.
A spacing system built on the same ratio as its type system produces distinctions nobody
can see, which is most of what is wrong with the app today.

**Corner radii are deliberately not on the 4dp unit** and this is the one place the rule
above stops. A radius is a property of an outline rather than a distance between two
things, so it never accumulates down a page and never pushes anything off a baseline.
The radius scale is a closed set of five values chosen by family, in A.4.

**Why the app is flat today, measured.** Nine of the fourteen shipping type roles live
between 10.5 and 21sp, so 64 percent of the scale spends itself inside a band spanning a
factor of two. Of 646 `dp` literals in `ui/`, 342 are not multiples of 4, and only 4 of
the 14 line heights are. There are four content left edges and no columns. That is the
mechanism behind "everything feels slightly off without anything looking broken."

## A.2 The type scale

**Ratio 1.200. Base 15sp. Two families on one ladder. Eleven roles, down from fourteen.**

The shipping sizes were already almost a 1.2 geometric scale (12, 15, 21, 26, 30, 64)
and were then filled in with in-between steps at 13, 17, 19 and 24 that flattened it.
The scale is not being replaced. It is being un-filled, and the holes are the hierarchy.

### Sans track, Hanken Grotesk, the interface voice

| step | role | size | weight | tracking | line height | replaces |
|---|---|---|---|---|---|---|
| s-1 | `meta` | **12.5sp** | 400 / 600 / 700 | +0.022 / +0.016 / +0.024em | **16** | caption + label + sidehead + swipeLabel |
| s0 | `read` | **15sp** | 400 | +0.004em | **24** | body |
| s1 | `readStrong` | **18sp** | 600 / 700 | -0.008em | **24** | bodyStrong + title |
| s2 | `lead` | **21.5sp** | 650 | -0.022em | **28** | itemTitle |
| s4 | `leadStrong` | **31sp** | 600 | -0.026em | **40** | new: the Trail day header |
| s8 | `numeral` | **64sp** | 250, tabular | -0.030em | **68** | timerNumeral |

### Serif track, Newsreader, the reading voice

| step | role | size | opsz | weight | tracking | line height | replaces |
|---|---|---|---|---|---|---|---|
| s1 | `prose` | **18sp** | 18 | 400 | 0, opsz | **28** | bodySerif 17 |
| s2 | `voice` | **21.5sp** | 26 | 400 | 0, opsz | **28** | new: the Areas band sentence |
| s3 | `speak` | **26sp** | 34 | 400 | 0, opsz | **36** | readSerif + closingLine 24 |
| s4 | `announce` | **31sp** | 48 | 400 | -0.006em | **40** | displayTitle 30 |
| s6* | `hero` | **40sp** | 68 | 400 | -0.012em | **44** | displayHero |

**Every line height is a multiple of 4.** All eleven. Four of fourteen were, before.
`read` 15/24 and `readStrong` 18/24 share a 24sp line, so a mixed run of the two keeps
its baseline rhythm, which the current 22.5 and 23 cannot do.

**The three merges, and why each is a gain.**

- **caption 12 + label 13 + sidehead 13 into `meta` 12.5.** A 1.083 step is invisible.
  The three were always separated by weight and tracking, which are the distinctions that
  actually read, and those are kept exactly. `swipeLabel` 10.5 joins them and is deleted,
  because 10.5sp was the app's smallest type sitting on its highest consequence control.
- **bodyStrong 17 + title 19 into `readStrong` 18.** A 1.118 step. 5.3 already worried in
  writing that "one step is as far as either can move without starting to shout." At
  18/600 a sheet title and a button label are the same object, and they should be.
- **closingLine 24 + readSerif 26 into `speak` 26.** A 1.083 step between two roles that
  never appear on the same screen.

**The holes.** s3 is empty in the sans and s5 and s7 are empty everywhere. `lead` 21.5 to
`leadStrong` 31 is 1.44x with nothing between, and `announce` 31 to `hero` 40 is 1.29x
with nothing between. A scale with every step occupied is a ramp. A scale with skipped
steps has a top and a bottom.

**`hero` at 40sp is the one deliberate departure from the ratio**, sitting between s5 and
s6. The scale report proposed 45. It is refused: 40 against a 12.5 modal is already
3.2 to 1 with a sentence as the dominant, which is the only element in the app that
passes every clause of A.3 today, and 45 costs a fourth line on the longest headline at a
371.4dp measure. Changing a working number for ladder tidiness is the kind of change this
repository does not make without a reason.

**The Focus item title moves from 26sp sans to `lead` 21.5.** That was the last off-ladder
sans size in the app. It also strengthens 10.3's "this never shrinks" into "this never
changes": the item title is now one size in all four places a person meets it, on the
card, in the detail sheet, in a focus session and on the completion screen.

### A.3 The dominance budget

A ratio alone does not fix "no dominant element." This does. Three clauses, all
mechanically checkable by a test that walks a screen's call sites.

1. **Exactly one element on a screenful sits at a dominant step** (`leadStrong` 31,
   `announce` 31, `hero` 40, `numeral` 64). One. Never two, never zero.
2. **The dominant is at least three steps, 1.73x, above the modal size on that screen**,
   and where the screen asks a person to decide something, the dominant is content and
   not chrome.
3. **No single step carries more than half the strings on a screen.**

Audited against the shipping build, and this is the whole diagnosis in one table:

| screen | modal | loudest content | ratio | clause 2 | clause 3 |
|---|---|---|---|---|---|
| Areas | 15 | 21 item title | 1.40 | **fail** | pass |
| Momentum | 12 | 26 headline | 2.17 | pass | **fail, 69 percent at 12sp** |
| Trail | 15 | 17 day header | **1.13** | **fail** | pass |
| Settings | 12 | 19 sheet title | 1.58 | **fail** | pass |
| Report | 12 | 40 hero | 3.33 | pass | pass |
| Focus | 17 | 64 numeral | 3.76 | pass | pass |

Five of the six surfaces sit at exactly 2.50 to 1 of total type range, to two decimal
places. The one that does not is the Focus session at 5.33 to 1, and it is the one
surface the owner did not name. That is not a coincidence and it is the reference the
other five are being brought to.

## A.4 The corner scale

Today `ClarityShapes` names **thirteen radii across a 20dp band**: 18, 12, 28, 12, pill,
8, 11, 8, 16, 8, 12, 14, 16, 12. Nobody can see 11 from 12 or 14 from 12, so the scatter
is cost with no benefit and it is a large part of why the app reads as assembled rather
than made.

**Six values. Nothing may use a value off this list.**

```
0     8     12     18     28     full
|     |      |      |      |      |
|     |      |      |      |      +-- pills: chips, tabs, the FAB, anchors, inline actions
|     |      |      |      +--------- sheets, and only sheets
|     |      |      +---------------- cards: the area card, the archive row, the tutorial
|     |      |                        card, onboarding panels, swatches, the mark badge,
|     |      |                        the swipe clip
|     |      +----------------------- rows, committing buttons, field wells, segment
|     |                               tracks, the snackbar, appearance tiles, momentum
|     |                               marks, segments' own corner is 8
|     +------------------------------ anything under 40dp: mood pills, widget internals,
|                                     check badges, segments
+------------------------------------ Bands and Zones: they bleed, so they have no corner
```

**Four token values move, each by 1 to 2dp, and one is deleted.**

| token | today | after | why |
|---|---|---|---|
| `momentumTile` | 11dp | **12dp** | one call site, 1dp off `row`. It is a Cell |
| `weeklyBanner` | 14dp | **deleted** | the banner becomes a Band, which has no corner |
| `swatch` | 16dp | **18dp** | one call site, 2dp off `card` |
| `markBadge` | 16dp | **18dp** | one call site, 2dp off `card` |
| `settingsBadge` | 8dp | **deleted** | the badge itself is deleted, D.9 |

**No single one of these is visible in a screenshot. Together they take the app from
thirteen radii to six, and that is the change that makes it look made.** It is exactly
the kind of thing that survives thirteen phases because nobody can point at it.

**The card stays at 18dp.** The scale report proposed 16dp so that a radius would sit on
the 4dp unit and so that a first line would clear the corner. Refused: 2dp is invisible
and would spend credibility on a change nobody can see, and the card's own 12dp vertical
padding already clears the 7.2dp of corner intrusion the correction is about.

**Where shape carries meaning, two laws.**

> **The permanence law. A full pill is reversible, or a place you can leave. A 12dp
> rectangle writes an event to the log.**

Pills: chips, tab items, the FAB, header glyphs, inline actions, the support button, the
re-entry default, every Contemplative anchor. Rectangles at 12dp: Add, Save, Complete,
Archive, Delete, Erase, Accept.

> **The one-turn law. A Contemplative surface carries exactly one filled anchor, and
> everything else on it is text with no container.**

One exception, named, and it does not extend by analogy: the Pulse has two anchors,
because `CLARITY_LOGIC_ENGINE.md` 6.1 requires both answers to be equally valid and the
whole content of the screen is that choice.

**Shape changes on state in exactly two places.** The FAB morphs from a full pill to a
20dp squircle on press, using `MorphShape` and `morphingPressShape`, which are already
built and already wired there. And a filter chip morphs from a full pill to 12dp when it
is selected, which is the deliberate choice under section 15: selection today is an ink
fill, which is a color signal, which is why `semantics { selected }` had to be bolted on
to satisfy section 13. A shape change works in grayscale, in a screenshot and for any
color vision, and it reuses the permanence law, because a set filter persists.

**Buttons do not morph on press.** 8.2 item 2 gives them a 0.97 scale, and a corner morph
on top would be two press treatments on one element, which is 6.1's error in a different
register.

## A.5 The spacing scale

Today: 27 distinct spacing values in `ui/`, of which 19 are off the 4dp grid that
section 6 claims. `10`, `14`, `6`, `18`, `26`, `22`, `7`, `5`, `9`, `13`, `17`, `11`,
`15`, `30`, `34`. That is not a grid with exceptions. That is a habit of picking a number.

**Six tokens. A Fibonacci sequence snapped to the 4dp unit.**

| token | dp | units | ratio | job |
|---|---|---|---|---|
| `hair` | **4** | 1 | | inside one unit: a glyph to its label, a dot to its name, a numeral to its caption |
| `tight` | **8** | 2 | 2.00 | lines of a single thought: a title to its caption, marks to their readout |
| `snug` | **12** | 3 | 1.50 | peers in a group: card to card, row to row, a sidehead to its first row |
| `step` | **20** | 5 | 1.67 | the screen inset, and a block to a sibling block |
| `rest` | **32** | 8 | 1.60 | section to section |
| `break` | **52** | 13 | 1.63 | movement to movement, and the bottom reservation |

Three properties an 8pt ladder does not have, which is why the 8pt ladder is refused.

1. **It is additive.** `snug + step = rest` and `step + rest = break`. Nesting can never
   drift off the ladder, because the sum of two adjacent steps is the next step.
2. **The ratio holds.** 1.50 to 1.67, converging on phi. An 8pt ladder is arithmetic
   above its first step and its ratio collapses from 2.00 to 1.25 as it climbs, so its
   top steps are indistinguishable, which is precisely why teams reach for a number
   between the ones the ladder gives them. That is a description of this codebase.
3. **It is closed under the 200 percent multiply.** Doubled it is 8, 16, 24, 40, 64, 104,
   every one still a multiple of 4.

**Vertical rhythm is a function of the type above it, so nobody picks a number.**

| relationship | gap | derivation |
|---|---|---|
| line to line inside one thought | `tight` 8 | the block's own leading, `lh - size`, snapped |
| block to sibling block | one line slot of the block's own role, snapped down | `read` 24 gives `step` 20; `leadStrong` 40 gives `rest` 32 |
| block to a new section | two line slots of the largest role above, snapped | `readStrong` 2 x 24 = 48 gives `break` 52 |
| a sidehead to its content | `snug` 12, fixed | a sidehead is a marker, not a line of the block |

**Named tokens that move.** `cardPaddingVertical` 17 to **12**. `cardGap` 11 to
**`snug` 12**. `sectionGap` 28 to **`rest` 32**. `sheetContentTop` 18 to **`step` 20**.
`cardPaddingHorizontal` stays **18dp fixed**. `screenPadding` stays **20dp fixed** and is
now named `step`. `swipeActionWidth` stops scaling entirely, B.6.

## A.6 The grid, and the optical corrections a grid needs

**Six columns. 12dp gutter, which is `snug`. 20dp margin, which is `step`.**

| device | measure | column | 2 col | 3 col | 4 col | 5 col |
|---|---|---|---|---|---|---|
| reference, 411.4dp | 371.4 | **51.9** | 115.8 | 179.7 | **243.6** | 307.5 |
| narrowest, 360dp | 320 | **43.3** | 98.7 | 154.0 | **209.3** | 264.7 |

The app's only multi-column layout, Momentum's three-up tile row, already measures
115.8dp cells with a 12.2dp gutter. It is sitting on a clean six-column grid and nothing
else in the app knows about it.

**Why six and not four.** The statistically common phone grid is four columns, which is
Material's compact window class. Four cannot express thirds, and the one multi-column
layout this app has in production is a three-up row, so a four-column grid would make the
app's own existing grid ungriddable. Six divides by two and by three, which gives halves,
thirds and sixths, exactly the three splits this app needs. It has no quarters and needs
none: a four-up row at 411.4dp gives 84dp cells, below a comfortable target with a label.

**Three left edges, not four.** Today there are four content left edges: 20.2 for titles
and sideheads, 38.2 inside a card, 56.4 on a Trail row and 61.0 on a settings row. The
last two differ by 4.6dp for no reason, which is invisible on either screen alone and is
exactly what makes the app feel unresolved when you move between them.

| edge | at | what sits there |
|---|---|---|
| **bleed** | 0 | Bands and Zones. Their content still sits at the measure |
| **measure** | 20 | every screen title, sidehead, sentence, card edge, row, button and paragraph |
| **indent** | 20 + `rest` 32 = **52** | text after a leading glyph: every Trail row |
| **card measure** | 20 + 18 = **38** | text inside a card. A container coordinate, never compared with the page's |
| **pattern indent** | **30** | the Report's pattern break and closing band only, existing, 11.1 |

The settings row's 61.0dp edge disappears with the badge. The Trail's 56.4 becomes 52.

**Six optical corrections. Five of them the app makes nowhere today.**

1. **Round and pointed forms overhang a flat margin by 3 percent of their width.** The
   Trail's 18dp glyphs need **0.5dp left**. `opticalGlyphNudge` already exists and is
   used in exactly one place. This is what it is for. The 7dp area dot needs 0.2dp,
   which is below the threshold of visibility, so it is left alone.
2. **A leading glyph aligns to the cap height of the first line, never to the center of
   the block.** `glyphTop = (firstLineHeight - glyphSize) / 2 + (firstLineHeight -
   capHeight) / 2`. For a 20dp glyph beside `readStrong` 18/24 that is
   `(24-20)/2 + (24-13)/2 = 7.5dp` from the block's top. Use `Alignment.Top` plus that
   offset. **Never `CenterVertically`.** The measured failure is on the shipping
   settings screen, where the badge on a two-line row sits 9.5dp below the title's cap
   center and reads as belonging to the caption.
3. **A sidehead's rule starts `tight` 8 past the label's ink, not past its advance.**
   Hanken's right sidebearing at 12.5sp is about 0.8dp. Measured today the Momentum rule
   starts 13.3dp past the ink and the Settings convention differs from it.
4. **The right margin belongs to ink, not to layout boxes.** The settings chevron's ink
   stops at 384.4dp while the hairline above it ends at 391.2. Trailing glyphs are offset
   so the ink lands on the margin. Chevron: **+6dp**.
5. **Serif display carries a left correction of about -0.06em.** `announce` 31sp needs
   **-1.9dp**, `hero` 40sp needs **-2.4dp**, `speak` 26sp needs -1.6dp, so a serif title
   set flush at 20dp puts its stem on 20 rather than on 21.9. This is why the current
   Areas title looks a hair inset relative to everything under it, measured at 23.6
   against 20.95.
6. **Radius steals from the top corner.** An 18dp radius needs `topInset >= radius x 0.4`
   = 7.2dp of vertical clearance beyond the nominal padding at the corner. The card's
   12dp vertical padding clears it. Any new container at 18dp must check the same.

---

# PART B. EVERY CONTROL

## B.0 The charge, measured

There are **35 distinct interactive constructions** in this app. Seven of them are
buttons, built seven different ways, in five different shapes, at three different
heights. **Twenty six of the thirty five are between 42dp and 60dp tall.** The app has
one size. Three have no `Role.Button` semantics at all. Four of the five `ClarityButton`
roles have no disabled appearance, so a disabled Primary is pixel-identical to an enabled
one. No control in the app has a loading state. Two live controls are under the 48dp
floor.

**There is no control system. There are 35 individually reasoned objects that happen to
be the same height.** Each has an excellent doc comment explaining itself and none
explains itself in terms of the others. That is what produces a screen that is competent
and lifeless: every element is defensible and no two are related.

The fix is a ladder, a formula, the shape law from A.4, an emphasis law and a state
matrix, and then all 35 re-expressed through them.

## B.1 The rung ladder

| rung | drawn height | touch target | ratio to previous | job |
|---|---|---|---|---|
| **Inline** | **36dp** | 48dp | | an action inside a row, a header, a list or a filter row |
| **Standard** | **48dp** | 48dp | 1.33x | the app's ordinary control: chips, fields, rows, tab items, segments, text actions |
| **Commit** | **60dp** | 60dp | 1.25x | the one action that closes a sheet or writes an event |
| **Anchor** | **76dp** | 76dp | 1.27x | the single most important control on a Contemplative surface |

Every step is at least 12dp, which on the reference device is 31px, roughly 2.2mm. That
is an order of magnitude above the vernier threshold: a person sees these apart without
comparing them.

**Material 3 Expressive's own ladder is 32 / 40 / 56 / 96 / 136, and it is refused.**
15.3 names "adopting a Material 3 Expressive default because it is the default" as a
refusal by itself, and two of those rungs have no use here: 96 and 136 are for a control
that is alone on a screen and this app never has one. What is taken is the shape of the
argument, which is that rungs must be far enough apart to be seen and each rung must have
a job.

**36 at the bottom rather than 32**, because a 36dp pill centered in a 48dp target leaves
6dp of margin, which is what `ClarityChip` already does at 38dp, so the Inline rung is a
2dp adjustment to a component that exists rather than a new one. 32dp with a 12.5sp label
leaves 8dp of leading and clips at 130 percent font scale.

**76 at the top rather than 96**, measured against the Focus ring: 76dp with an 18sp
label leaves 26dp of air above and below, and 36dp of air turns a two-word label into a
slab.

**The names are roles, not t-shirt sizes.** A builder handed `size = Medium` picks by
eye. A builder handed `rung = Commit` has to answer "does this commit something", and
the answer is in the code they are already writing.

## B.2 The anatomy formula

One formula, and it generates the table. **Side padding is `height x 0.42` snapped to
2dp. The icon gap is `height x 0.17` snapped to 2dp.**

| rung | height | side padding | icon | icon to label | label |
|---|---|---|---|---|---|
| Inline | 36 | **16** | **18** | **6** | `meta` 12.5 / 600 / +0.016em |
| Standard | 48 | **20** | **20** | **8** | `readStrong` 18 / 600 / -0.008em |
| Commit | 60 | **26** | **22** | **10** | `readStrong` 18 / 600 |
| Anchor | 76 | **32** | **24** | **12** | `readStrong` 18 / 600 |

**Two label sizes, not four.** The controls report gave Commit 17sp and Anchor 19sp. The
type scale merges 17 and 19 into `readStrong` 18, so those two rungs have no separate
size available and none is invented. **The rung is carried by the box**, which is the
honest carrier: at 48, 60 and 76dp the internal air runs 12, 18 and 26dp per side around
one 24sp line, and that is a difference a person sees without reading the label.

Icon sizes are 18 / 20 / 22 / 24 rather than Material's 20 / 24 / 32 / 40. Section 7
fixes Material Symbols Rounded at weight 500, and a 32dp symbol at weight 500 reads thin
and hollow. 24dp is already the app's largest glyph anywhere, so nothing new has to be
drawn or re-hinted.

**Chips and rows keep their own type regardless of rung.** A chip is always `meta`
12.5/600 and a row is always a `read` 15/600 title over a `meta` 12.5 caption, because a
chip that grew to 18sp would stop being a chip.

## B.3 The emphasis law

Emphasis is the product of three independent axes, not a role enum.

| axis | values, loudest first |
|---|---|
| **ground** | filled (`actionBlue`, `inkPrimary`, mint, gold) > tinted (4 to 14 percent) > bare |
| **rung** | Anchor > Commit > Standard > Inline |
| **width** | Fill > Wide > Hug |

And one rule that stops it becoming a matrix nobody can hold:

> **One filled control per surface.** A sheet has exactly one. A screen has exactly one.
> If a design needs two, one of them is wrong.

Audited against what is built, the rule is already almost obeyed: the area sheet has
Positive plus Secondary, the erase sheet has Destructive plus Secondary, the focus
complete screen has one pill and one text action. Stating it costs nothing and prevents
the next regression. It is **broken today on exactly one surface**, the re-entry screen,
which has a filled primary and a transparent twin of the same width, B.7 item 1.

**Width stops being a boolean.** `fillWidth: Boolean = true` is why nine of fourteen
`ClarityButton` call sites get a full-width button they never asked for.

| width | rule | permitted where |
|---|---|---|
| `Hug` | intrinsic + 2 x side padding | **the default.** Every Inline and Standard control |
| `Wide` | **4 grid columns**, 243.6dp on the reference device and 209.3dp at 360dp | every Commit and every Anchor |
| `Fill` | the content measure | **only where the control sits directly under a full-width field in the same form.** Three sites: add item, edit item, area editor |

`Wide` is four columns rather than a constant so it ports, and on the reference device it
lands within 4dp of the Focus ring's 240dp diameter, which is where the one exception is:
**the Focus session anchor is exactly 240dp, the ring's own diameter**, because a
relationship a person cannot name and can see beats a number that relates to nothing.

`Fill` survives only for forms, where a hugging button under full-width fields breaks the
form's left edge. Everywhere else the air around a control is the emphasis, and a
full-width control has no shape.

**On the accessibility trade.** Complete on the area sheet goes from 371.4 x 44dp to
243.6 x 60dp. That is 11 percent less area and **16dp more in the axis a thumb actually
misses on**, and 10.3.1 already requires Complete to be reachable by swipe and by
long-press menu as well.

## B.4 The state matrix

| state | today | after |
|---|---|---|
| **enabled** | yes | yes |
| **pressed** | `scale 0.97`, FAB `0.94`, `springStandard` | keep, and **add a ground**: 6 percent toward `inkPrimary` on a light ground, 8 percent toward white on a dark one, on `effectsFast` 150ms. A bare control **creates** a ground at 6 percent in its target's pill shape |
| **focused** | 2dp `actionBlue` ring on `effectsFast` | keep, and **add it to the six controls that have none**: `SheetActionRow`, `CompletedRow`, `HeaderGlyph`, `SettingsRow`, `SettingsSegment`, the onboarding mini card's remove |
| **disabled** | Destructive and the onboarding primary only | all rungs: ground to `inkPrimary` 6 percent when filled or 4 percent when tinted, **label to `inkSecondary`**, no press scale, no haptic, `semantics { disabled() }` |
| **selected** | an ink fill on the chip and the segment | ink fill **plus the corner morph, pill to 12dp** |
| **loading** | **does not exist** | a 2dp `actionBlue` line sweeping the inner bottom edge at 1.2s per pass, and the control stops taking taps. **Never a spinner** |
| **error** | n/a | n/a. No action in this app fails in a way a button reports |

**A disabled label is `inkSecondary`, not `inkTertiary`.** The controls report specified
`inkTertiary`. It is refused, and by this document's own precedent: 10.3, 10.9, 10.11 and
10.19 each reached the same resolution against `inkTertiary` in four separate phases, and
section 13 holds one floor of 4.5 to 1 which `inkTertiary` fails at 2.34 on the canvas.
A floor is a floor. What says a control is off is the ground dropping and the press and
the haptic being absent, not a word nobody can read.

**The loading state does not dim its label**, which the controls report proposed at 45
percent. Same reason. The 2dp line is the whole signal, which is one device, and it is
the same 2dp line `UndoSnackbar` already draws for its five second window, so the app
gains a loading state without inventing a motion language.

**Why press needs a ground.** A 3 percent scale on a 371dp button is 11dp of width change
and reads. On a 36dp inline chip it is 1dp and does not. **Today every small control in
the app has no visible press feedback at all.**

**A press ground is not a second separation device**, and the precedent is in this repo:
`Interactions.kt:120` already argues that a focus ring "is not a separation device and
does not fall under the one device rule in 6.1. It is transient, it belongs to a state
rather than to a boundary." A press ground is the same class of thing on the same terms.

**Prefer absence to disablement.** 10.16 already says this for the unfiled row: "a
disabled control is a question the user then has to answer." Generalized, a control is
disabled **only** when the person can enable it by something they are doing on the same
surface, such as typing into a field. Otherwise it is absent. That reduces disablement to
about three sites and makes it honest.

## B.5 The master control table

Daylight unless marked (C) for Contemplative. All heights are the drawn box; Inline
controls sit in a 48dp target.

| control | rung | width | corner | side pad | type | icon | ground at rest | pressed | selected | disabled |
|---|---|---|---|---|---|---|---|---|---|---|
| **Button, Primary** | Commit 60 | Wide 4col, Fill in a form | 12 | 26 | readStrong 18/600 | 22 | `actionBlue` | +6% ink | | ink 6% / inkSecondary |
| **Button, Positive** | Commit 60 | Wide 4col | 12 | 26 | readStrong 18/600 | 22 | `positiveGreen` 13% | +6% ink | | ink 4% / inkSecondary |
| **Button, Secondary** | Standard 48 | Hug | 12 | 20 | readStrong 18/600 | 20 | ink 5% | +6% ink | | ink 4% / inkSecondary |
| **Button, Tertiary** | Standard 48 | **Hug** | full | 20 | readStrong 18/600 | 20 | none | `actionBlue` 6% pill | | inkSecondary |
| **Button, Destructive** | Commit 60 | Wide 4col | 12 | 26 | readStrong 18/600 | 22 | `inkPrimary` | +8% white | | ink 10% / inkSecondary |
| **Button, Inline** | Inline 36 | Hug | full | 16 | meta 12.5/600 | 18 | `actionBlue` 6% | 12% | | ink 4% / inkSecondary |
| **FAB** | **64 x 56** | fixed | full, morphs to 20 pressed | | | **26** | `actionBlue`, glyph `card` | scale 0.94 + morph | | **never disabled** |
| **Chip, navigation** | **40 drawn** | Hug | full | 16 | meta 12.5/600 | 18 | `raise` **plus card elevation** | +6% ink | never | |
| **Chip, filter** | Inline 36 | Hug | full, **morphs to 12 selected** | 16 | meta 12.5/600 | 18 | `raise`, **flat** | +6% ink | `inkPrimary` fill, `card` label, 12dp corner | |
| **Chip, suggestion (C)** | Standard 48 | Hug | full | 18 | meta 12.5/600 | | white 7% | white 12% | white 16%, `textBright`, 7dp dot | |
| **Text field** | Standard 48 min | Fill | 12 | 14 h / 12 v | read 15/400 | | `raise` well | | focus: `canvas` | ink 4% / inkSecondary |
| **Text field (C)** | Standard 48 min | Fill | 12 | 14 h / 12 v | read 15/400 | | **white 6%** (was a hairline) | | focus: white 12% | white 3% |
| **Tab item** | Standard 48 | Hug | full | 14, +4 selected | meta 12.5/600 | 24 | none | | `actionBlue` 10% light, **16% dark** | |
| **Segment** | **Standard 48** (was 42) | **Hug** (was `weight(1f)`) | 8 | 14 | meta 12.5/600 | | none | +6% ink | ink fill, `card` label | |
| **Segment track** | wraps | Hug, max = measure | 12 | **4** (was 3) | | | `raise` | | | |
| **Switch** | platform, in a 56 row | | | | | | track `raise`, thumb `inkTertiary` | | track **`inkPrimary`**, thumb `card` | 40% |
| **Row, compact** | Standard 48 min | Fill | 0 | 20 | read 15/600 + meta 12.5 | 20 | none | ink 5% | | inkSecondary |
| **Row, setting** | **56 min** | Fill | 0 | 20 | readStrong 18/600 + meta 12.5 | none | none | ink 5% | check 18 + inkPrimary | inkSecondary |
| **Row, choice** | Standard 48 min | Fill | 0 | 20 | read 15/600 | | none | ink 5% | check 18 + inkPrimary | |
| **Row, destructive** | Standard 48 min | Fill | 0 | 20 | read 15/600 in `deleteMuted` | 20 | **none, never a container** | `deleteMuted` 6% | | |
| **Swipe face, Complete** | card height | **72 fixed** | card clip 18 | | **meta 12.5/700** | **24** | `positiveGreen` 18%, +40% past commit | | | absent when gated |
| **Swipe face, Swap** | card height | **72 fixed** | card clip | | meta 12.5/700 | 24 | `actionBlue` 12% | | | absent when gated |
| **Swipe face, Delete** | card height | **56 fixed** | card clip | | meta 12.5/700 | 24 | `deleteMuted` 13% | | | **tap only, never a swipe commit** |
| **Snackbar action** | Inline 36 | Hug | full | 16 | meta 12.5/600 | | none | `actionBlue` 6% | | |
| **Sheet handle** | 34 x 4, **12dp fixed padding** | | 2 | | | | ink 18% | | | |
| **Header glyph** | Inline 36 | | full | | | 22 | none | ink 6% pill | | |
| **Anchor (C)** | **Anchor 76** | Wide 4col; **240 on Focus** | full | 32 | readStrong 18/600 | | accent 14% | accent 22% | | accent 6% / textDim |
| **Text action (C)** | Standard 48 | Hug | full | 20 | readStrong 18/600 | | none | **accent 8% pill** | | textDim |
| **Icon action (C)** | Standard 48 | | full | | | 20 | none | white 8% circle | | 40% |
| **Choice panel (C)** | wraps, equal | Fill | 18 | 20 h / 20 v | readStrong 18 + read 15 | | **white 6%** | +8% white | | |
| **Tutorial advance** | Inline 36 | Hug | full | 16 | meta 12.5/600 | | none | white 8% | | |
| **Tutorial skip** | Inline 36 | Hug | full | 16 | meta 12.5/600 | | **`deepBlack` opaque** | white 14% | | |
| **External link mark** | | | | | | **14** | none, trailing | | | |

**Seven button implementations collapse to four components.**

| was | becomes |
|---|---|
| `ClarityButton` at five roles with an implied size | `ClarityButton(rung, ground, width)` |
| `FocusPill` | `ClarityAnchor` (C) |
| `PulseResponsePill` | `ClarityAnchor` (C) at Commit, with its radial fill unchanged |
| `ReportAcceptPill` | `ClarityAnchor` (C) with its settle unchanged |
| `OnboardingPrimaryButton` | `ClarityAnchor` (C) |
| `SheetActionRow`, `QueueRow`, `CompletedRow`, `SettingsRow`, the inbox rows | **`ClarityRow`**, moved into `ui/components` |
| `FocusTextAction` | `ClarityTextAction` (C) |

**The Contemplative twin file stays a twin and is not merged**, for the reason
`OnboardingControls.kt:57` already gives: every component in `ui/components` resolves
`LocalClarityColors`, and a Daylight button on `deepBlack` is not a theming bug that
shows up in a screenshot. The twin is a **color** twin. It was never a **form** twin, and
that is the drift being corrected: after this, `ClarityAnchor` and `ClarityTextAction`
are the Daylight rungs at Daylight geometry with a Contemplative palette resolved.

## B.6 The three chip populations

One component is doing navigation and filtering today, so two chips that look identical
do different things, and the amber Pulse dot then reads as a third unnamed state on the
same object. Three populations, three separation devices, no new element and no new
color.

| | drawn height | ground | device under 6.1 | shape at rest | when selected |
|---|---|---|---|---|---|
| **navigation** (Focus, Today's Pulse, Inbox 4) | **40dp** | `raise` | **elevation** | full pill | never selected |
| **filter** (All, an area) | **36dp** | `raise` | **a lightness step, flat** | full pill | **12dp corner**, ink fill, `card` label |
| **suggestion (C)** | 40dp in a 48 target | white 7% | a lightness step | full pill | full pill, white 16%, 7dp dot |

A navigation chip is an object you pick up and go somewhere with, so it carries
elevation. A filter chip is a setting on the list beneath it, so it lies flat against the
page. One visible difference, no new color, and it is a reason rather than a decoration.
Four dp of height is at the edge of perception in isolation and obvious when the two rows
are compared, which is the only moment it matters.

**Only a filter chip changes shape**, so a shape change means "you set this", everywhere,
full stop.

**The Pulse dot moves inside the pill, at the leading edge, at 6dp.** Today it floats 8dp
outside the arc at the top right, which is the construction 15.1 lists as "a red numeric
badge as the primary signal that something is waiting" in a different color and without
the number. Inside, at the leading edge, it is the same grammar as an area dot on a
filter chip: a dot at the leading edge of a chip identifies what the chip is about.
Nothing is lost, because 10.1 already requires the **label** to be the signal and the
label already changes to `Today's Pulse`.

**Swipe faces stop scaling with the text size.** `swipeActionWidth` is `scaled(66.dp)`,
so at 200 percent two left faces are 264dp while the card travels 55 percent of 371.4 =
204dp, and Delete goes off the screen. A face is a box for a fingertip and a glyph, not a
box for text. **Complete 72, Swap 72, Delete 56, all fixed.** Delete is deliberately
narrower than the other two: it is the one face that must never be hit by momentum. The
label draws at `meta` 12.5/700 only while it fits on one line inside the face, and above
that the glyph stands alone, which costs nothing because 10.3.1 already makes the
long-press menu and the detail sheet mandatory paths.

**Nothing else in the swipe model moves.** Every threshold, the 1,200dp/s fling, the
one-open-row coordinator, the 180ms slide and 200ms collapse, and rule 12's law that a
full left swipe commits Swap while delete needs a deliberate tap and an undo window, are
all untouched.

## B.7 The defect ledger: broken, not ugly

Ranked by consequence. Every one is fixed by something above.

| # | defect | evidence |
|---|---|---|
| 1 | **The re-entry screen's second option is a full-measure, 50dp, fully transparent rectangle 8dp beneath a live filled blue button of the identical width.** It writes events to the log and that screen has no undo | `reentry/ReEntryScreen.kt:163-176` passes `role = TERTIARY` with `fillWidth` at its default `true`; `Buttons.kt:90` gives TERTIARY `Color.Transparent` inside `heightIn(min = 50.dp)` |
| 2 | **The Light theme tile has no visible container.** It renders the light `canvas` token on the light `canvas` | `settings/AppearancePicker.kt`; a full-column scan of `settings3.png` finds the three miniature rows and no tile edge |
| 3 | **The tutorial's advance is a full-screen invisible `Box` and `Tap to continue` is a `Text`.** Keyboard and switch users can reach Skip and nothing else | `tutorial/TutorialOverlay.kt:250` |
| 4 | **`SheetActionRow` has no `Role.Button`, no focus ring and no press feedback**, so Archive and Delete on the area sheet are announced to TalkBack as plain text | `areas/AreaSheets.kt:667-689` |
| 5 | **`CompletedRow`'s reopen is a bare `Text` with no role, no click label and no touch target** | `AreaSheets.kt:660` |
| 6 | **Skip is drawn on top of the Areas settings gear** | visible in `shot6.png` |
| 7 | **Settings segments are 42dp**, under section 13's 48dp floor | `SettingsComponents.kt:482` |
| 8 | **`QueueRow` is about 46.5dp**, under the floor: 12 + 12 around a 15sp line with no `defaultMinSize` | `AreaSheets.kt:607` |
| 9 | **Four of five button roles have no disabled appearance**; only DESTRUCTIVE branches on `enabled` | `Buttons.kt:86-100` |
| 10 | **The settings badge sits 9.5dp below the title's cap center on every two-line row**, so it aligns with the caption | measured 560.4 against 550.9 in `settings.png` |
| 11 | **The chevron's ink stops 6.5dp short of the margin** while the hairline and the switch track reach it | measured 384.4 against 390.9 |
| 12 | **The FAB's inset is `screenPadding` 20dp while the tab bar 14dp beneath it uses `tabBarInset` 17dp** | `areas/AreasScreen.kt:307`. That 3dp is visible in `v011_areas.png` |
| 13 | **The swipe faces overflow at 200 percent**, 264dp of face against 204dp of travel | `ClaritySpacing.kt:106` |
| 14 | **The sheet handle's padding scales with the text size**, against `ClaritySpacing`'s own doctrine | `ClaritySheet.kt:100` |
| 15 | **The active tab pill is near invisible in dark**, `actionBlue` at 10 percent over `raise` `#18181F` | `ClarityTabBar.kt:170`, visible in `22-3c-dark.png` |
| 16 | **`OnboardingField` is a hairline underline**, the construction 10.19 rebuilt the Daylight field to remove and 15.3 refuses by name | `OnboardingControls.kt:384-391` |
| 17 | **`OnboardingPrimaryButton` is white at 9 percent on `#0B0B10` and reads disabled when enabled.** 10.7 states 14 | `OnboardingControls.kt:395` |
| 18 | **The onboarding fork panels are a 1.2 L\* step over their own ground**, because the fill is an opaque token and the glow behind it lifts the ground it sits on | measured at y=1150 in `shot_areas.png`: panel 19.6, ground beside it 17.9, ground at the screen foot 14.0 |
| 19 | **The fork panels differ in height by 26 percent**, 111.2 against 88.4, on a pair Addendum 01 8a requires to be a genuine equal alternative | measured |
| 20 | **The onboarding nav row is 5.7dp out of balance**: the back glyph's ink is 6.5dp inside the measure and the Jump in label's ink is 0.8dp inside it | measured |
| 21 | **About draws two 31sp serif titles 100dp apart**, `About` and `Clarity Now` | `about/AboutScreen.kt` plus `PushedScreen`'s own title |
| 22 | **The Trail's timestamps draw suppressed rows in `Color.Transparent`**, so the eye reads missing data rather than clustering | `trail/TrailScreen.kt` |
| 23 | **Four of sixteen settings glyphs are duplicates and one is the brand mark**, which 4.3 says is not an affordance | `SettingsScreen.kt` |
| 24 | **Theme tile labels are 9.5sp**, below the app's smallest role, on the one control about text size | `AppearancePicker.kt:209` |
| 25 | **The Momentum stat row overflows at the default text size**, because `Minutes focused` is more than twice the length of `Completed`, and cannot lay out at 200 percent | `momentum2.png` |
| 26 | **The onboarding mini card's remove control has no press feedback of any kind** | `OnboardingControls.kt:289` |
| 27 | **No loading state anywhere.** Export, Import, Erase and Rebuild cache can hang silently | `settings/SettingsSheets.kt`. The only item here that needs a ViewModel flag |
| 28 | **`about_version` renders U+00B7 through `&#183;`**, so `verifyLanguageHygiene` passes on the source while the rendered string carries a non-ASCII character | `strings.xml`. A question for the owner rather than a layout defect |

---

# PART C. EVERY CONTAINER

## C.0 The charge, measured

The two most important objects on the flagship screen, measured off `v011_areas.png` at
the reference width:

| object | width | height | aspect | radius | ground |
|---|---|---|---|---|---|
| weekly banner | 371.4 | 71.7 | **5.2 : 1** | 14 | parchment |
| area card | 371.4 | 86.1 | **4.3 : 1** | 18 | card plus wash |

**The app's most important object and a box holding a sentence differ by 14.4dp of height
and 4dp of radius, and nothing else.** That repeats everywhere: the settings row at
5.7:1, the Trail row at 7.4:1, the Pulse response pill at 4.8:1, the Complete button at
8.4:1. **Fourteen of the sixteen containers in this app are a full-measure rounded
rectangle between 3:1 and 8:1, at 20dp of side padding, separated by 11 or 12dp.**

The app does not have one container idea repeated at three sizes. It has one container
idea repeated at one size, and the three sizes are 18 / 14 / 12 / 11 / 8dp of radius,
which is a difference nobody can see from arm's length.

## C.1 The five kinds

Told apart by three properties a person reads before they read anything: does it stop
before the screen does, how square is it, and does it carry your color.

| kind | edge | aspect | your color | ground | radius | what it is for |
|---|---|---|---|---|---|---|
| **Object** | stops at the measure | **2:1 to 4:1** | yes | `card` plus wash, elevation | 18 | a thing in your life you can act on |
| **Band** | **bleeds both edges** | any | no | a tint: parchment, gold | 0 | the app talking about the whole page |
| **Zone** | **bleeds both edges** | any | no | `raise` | 0 | a region of an Object or of a sheet |
| **Field** | **nothing is drawn** | n/a | no | the page | n/a | a group of rows under a sidehead |
| **Cell** | stops | **1:1 to 2:1** | sometimes | varies | 12 | one of a set, in a grid or a strip |

Plus the ground itself, which is not a container: **the Room**, on Focus, Pulse and the
Report. Full bleed, Contemplative, no container inside it at all.

At a glance: an **Object** is lifted, squarer than a row, and has your color in it. A
**Band** touches both edges of the screen and is warm. A **Zone** touches both edges and
is one shade of grey away from what it sits on. A **Field** is a small label with a rule,
and air. A **Cell** is small, nearly square, and there is more than one of it.

**The Field is the kind the app was missing entirely**, and it is why the settings screen
reached for sixteen hairlines, why the area sheet has three different section gaps and
why the Trail needs a timestamp column. A group of rows made of a sidehead and air is a
real container that draws nothing.

**Aspect is the rule that makes this checkable with a ruler: an Object is squarer than a
row.** Before, fourteen of sixteen containers sat between 3:1 and 8:1. After, Objects
cluster at 2.0 to 3.9:1, rows and controls at 5.4 to 9.7:1, Cells at about 1.2:1, and
Bands and Zones have no aspect at all because they bleed.

## C.2 The area card, the app's one Object

The card carries **two of the five facts it owes**, and every one of the missing three is
already in `AreaCardModel`. This is entirely presentation: no new query, no new data.

| the card must say | today | the field it is already handed |
|---|---|---|
| which area | yes | `name`, `colorHex` |
| what is active | yes | `activeItemTitle`, `activeItemFirstStep` |
| how long it has been active | only when idle | `daysSinceLastEvent` |
| whether a session is running | as a fourth stacked line | `focusMinutesRemaining` |
| how much is waiting | **nowhere on screen**, semantics only | `queueLength` |

And every card is exactly the same height, so a column of them has no rhythm. The owner
named variation by name.

**The move: the card gains a body and a foot.** The body carries identity and subject at
the card measure. The foot is a 28dp `raise` strip at the bottom, inside the card's own
18dp clip, carrying state on the left and the queue mark on the right.

```
BEFORE   measure x 86.1dp   4.3 : 1        AFTER   measure x 104dp   3.6 : 1  with a foot
                                                   measure x  76dp   4.9 : 1  without

+----------------------------------+         +----------------------------------+  r18
|                          17dp    |         |                          12dp    |  card
|  o Work by      13sp/600 accent  |         |  o Work by     meta 12.5/600     |  body
|                           7dp    |         |                           8dp    |
|  Look           21sp/650         |         |  Look          lead 21.5/650     |
|                          17dp    |         |                          12dp    |
+----------------------------------+         +----------------------------------+  raise
                                             | In focus, 7 minutes left     v   |  foot 28
   one column, one ground,                   +----------------------------------+
   fixed height, two of five facts
                                                two grounds, two alignments,
                                                variable height, five of five facts
```

**Body.**

| element | value |
|---|---|
| horizontal padding | **18dp fixed**, a physical inset per `ClaritySpacing`'s doctrine |
| vertical padding | **12dp scaled**, was 17 |
| eyebrow | 7dp dot + `tight` 8 + the area name at `meta` 12.5/600 in the area label color. Row 16dp |
| eyebrow to title | `tight` **8**, was 7 |
| title | `lead` 21.5/650/-0.022em, lh 28, `inkPrimary`, max 2 lines. **This never shrinks** |
| title to first step | `tight` **8**, was 5 |
| first step | `read` 15/400, lh 24, `inkSecondary`, one line, ellipsized |

Body height **76dp** typical, 108dp with a first step, 104dp with a two-line title.

**Foot.**

| element | value |
|---|---|
| height | **28dp** at the default scale; grows with its own text |
| ground | `raise`, under the wash, inside the card's own 18dp clip |
| horizontal padding | **16dp fixed** |
| left slot | `meta` 12.5, one of two cases below, max 2 lines |
| right slot | `ClarityIcons.expand` at **16dp** in `inkSecondary`, top aligned to the left slot's first line |

**The foot exists only when it has something to say, and that is where the variation comes
from.**

| the area's state | left slot | right slot |
|---|---|---|
| a session is running | a 13dp play glyph + `In focus, 7 minutes left` at `meta`/600 in the accent | the queue mark when `queueLength > 0` |
| idle | `Last active 21 days ago` at `meta` `inkSecondary` | the queue mark when `queueLength > 0` |
| active and quiet, empty queue | **no foot at all.** The card is 76dp | |

**How much is waiting is never a number.** One `expand_more` glyph at 16dp in
`inkSecondary` at the foot's trailing edge, present when the queue is not empty and
absent otherwise. No number, no dot, no bar, no fifth accent form. The card says
**whether**, never **how much**. Section 14 as amended by Addendum 01 forbids a numeric
badge on any surface, 10.16 already settled that a count is the label or it is nothing,
and this app's audience is specifically harmed by counters of unfinished work. The
chevron doubles as an honest affordance: there is more here and tapping opens it, which
is true. The rejected forms were a row of pips, which is a count and silhouettes as a
dashed stripe, and a stacked card edge peeking below, which is a card inside a card in
silhouette.

**The status line is never extended to an ordinary active area.** The containers report
proposed `Since Tuesday` on a quiet active card, as a landmark rather than a duration.
It is refused. 10.3 shows the status line "only when it carries information" and scopes
it to idle and in-session areas, a date on an ordinary active item carries none, and the
moment a clock appears on an unfinished task the app is one inference away from a
duration, which is what 10.17's one hard rule and 13.1 exist to prevent. **The foot
therefore carries only what 10.3 already permits, which means it needs no spec amendment
and no owner's word.**

**This is not a card inside a card.** The foot has no radius of its own, no shadow, and
bleeds to the card's own edges. It is the construction 10.19 already ships, where a
field's well steps down to `raise` inside a `card` sheet. The card carries one separation
device, elevation. The foot carries one, a lightness step. Section 14 and 6.1 both hold,
and the swipe reveal is unchanged because the foot sits inside the same 18dp clip.

**Does five still fit?** 10.3 and 11 both claim five areas fit comfortably. On the
reference device, with the weekly banner gone from the stack because it becomes the
header Band:

- five bare cards: 5 x 76 + 4 x 12 = **428dp** against 452dp of field. Fits, with 24dp over.
- a realistic mix, two with feet and three without: 2 x 104 + 3 x 76 + 4 x 12 = **484dp**. Scrolls
  by 44dp, so the fifth card is fully visible and the page has somewhere to go.
- five cards all with feet: 5 x 104 + 4 x 12 = 568dp. The fifth is two thirds visible and it scrolls,
  which is correct: a person with five areas that all carry sessions and queues has more
  on their screen and it should say so.

## C.3 The Band, and the Zone

**A Band bleeds to both screen edges, carries a tint, has no radius, no border and no
shadow, and its content sits on the 20dp measure.** It is the app talking about the whole
page rather than about one thing on it. There are exactly four in the app after this
document, and no fifth may be added without a reason written down: the Areas header, the
Report's pattern break, the Report's closing block, and the settings appearance Zone's
sibling (a Zone, not a Band, because its ground is `raise` rather than a tint).

**A Band's bottom edge is a fade, never a line.** A 32 to 40dp linear fade that removes
the tint's own alpha rather than painting the canvas over it, which is the identical
construction 6.1 already specifies for scroll edges, so it is correct over the
Contemplative gradients as well as over a flat canvas and nothing is invented. **Hard top,
faded bottom, deliberately asymmetric:** two hard edges make it a box, which is the thing
being deleted, and two fades make it a glow, which is 15.1 adjacent.

**A Zone bleeds the same way and takes `raise` instead of a tint.** It is a region of
something, not a statement about the page. Three exist: the area sheet's head, the area
sheet's Active zone, and the settings appearance picker.

**Is a Band a tell?** It is not on 15.1. It is not glassmorphism, because there is no
blur and no translucency over content. It is not a stripe, because it has no edge, it
fades. It is not a gradient behind a headline for impact, because it is a flat tint
carrying a whole header zone rather than sitting behind one line. And the precedent is
already in the app: 11.1's pattern break "bleeds to full screen width, sits on gold at
4.5 percent." The Band kind is not invented here. It exists on one screen and is now the
system.

## C.4 The Field

A sidehead, then air, then rows on the page's own ground. **Nothing is drawn.** One gap
value for each relationship, everywhere in the app:

| relationship | gap |
|---|---|
| a sidehead to its first row | `snug` 12 |
| a row to a row inside one field | `tight` 8 on a settings screen, `snug` 12 in a sheet |
| the last row to the next sidehead | `rest` 32 |
| a Zone or a Band to the next field | `rest` 32 |

**Grouping without a line, checked on the worst case.** A settings row's title-to-caption
gap is `hair` 4 between a 24sp line box and a 16sp one; its caption to the next title is
`tight` 8 plus the two half-leadings, which draws as about 12dp. That is 3 to 1, and
optically wider than 3 to 1 because the trimmed line boxes put more air into the larger
gap. A two-line row cannot be read as two rows. Group to group at `rest` 32 against row
to row at 12 is 2.7 to 1, with a sidehead and its rule sitting in the gap.

6.1 puts a hairline **fourth**, "only if all three above have genuinely failed." Between
two 56dp rows with 12dp of air, whitespace has not failed. **Sixteen drawn lines on the
settings screen go to zero**, and the seven that remain are the sideheads' own rules,
which are each that sidehead's single device.

## C.5 The Cell

Small, nearly square, one of a set, 12dp radius. Four exist: the appearance tile at
100 x 84, the Momentum area mark at 44 x 44, the color picker swatch (18dp, an Object
by radius because it is the accent at full strength), and the mood pill at 46 x 26 and
8dp. A Cell's label sits **centered beneath it**, which is the one permitted exception to
a page having a single axis.

## C.6 The full container inventory, before and after

| container | kind | before | aspect | after | aspect | verdict |
|---|---|---|---|---|---|---|
| area card, bare | Object | measure x 86, r18 | 4.3:1 | measure x 76, r18 | 4.9:1 | **rebuilt**, body and foot |
| area card, with a foot | Object | | | measure x 104, r18 | 3.6:1 | **new state**, and the source of the variation |
| weekly banner | Band | measure x 72, r14 | 5.2:1 | **bleeds, r0, 32dp fade** | | **rebuilt** as the page's head |
| sheet head | Zone | none | | bleeds, 76dp | | **new** |
| sheet Active zone | Zone | none | | bleeds, about 176dp | | **new** |
| sheet Complete control | | measure x 44 | 8.4:1 | 243.6 x 60 | 4.1:1 | **reframed**, Commit rung |
| sheet Archive / Delete | | two rows | | two Standard buttons, Hug | | **reformed** to 10.20's settled pair |
| Momentum area tile | Cell | 115.8 x 52, r11 | 2.2:1 | **44 x 44, r12**, leading a row | 1:1 | **relocated**, carries the 3.4 grant |
| Momentum balance row | | measure x 36 of text | 10:1 | 44dp row with a Cell | | **rebuilt**, merged with the tiles |
| Momentum This week | | a three-up stat row | | three rows, 144dp | | **rebuilt**, 15.1 "stat banners" |
| Momentum focus strip | | 7 x (48.6 x 41), r11 | 1.2:1 | 7 circles, a 34dp block | | **rebuilt** to the one mark language |
| Trail day header | Field | label + count + rule | | **31sp heading, count on the margin, no rule** | | **rebuilt**, the reserve deleted |
| Trail cluster | Field | not drawn | | a time head plus its rows | | **new**, from data already computed |
| Trail row | | measure x 41.7 | 8.9:1 | measure x 32 | 11.6:1 | **rebuilt**, 23 percent denser |
| settings row | | measure x 56 + a hairline | 6.6:1 | measure x 56, no line, no badge | 6.6:1 | **rebuilt** |
| settings icon badge | | 26dp, r8, seven hues | 1:1 | **deleted** | | |
| appearance Zone | Zone | none | | bleeds, 148dp | | **new** |
| appearance tile | Cell | 100 x 84, r12 | 1.2:1 | unchanged, on `raise` | 1.2:1 | ground fixed |
| Report pattern break | Band | bleeds plus two rules | | bleeds, **no rules** | | **one device restored** |
| Report closing block | Band | no container at all | | bleeds, gold 3 percent, plus its glow | | **new** |
| Pulse response pill | | measure x 66 | 5.6:1 | 240 x 60 | 4:1 | tightened into a group |
| Pulse History control | | measure wide | | about 92dp, centered | | **rebuilt**, stops being a row |
| tutorial card | Object | measure x 150, r18 | 2.5:1 | 5 columns x 140, r18 | 2.2:1 | **reframed**, marks instead of a numeral |
| archive row | Object | measure x 130, r18 | 2.9:1 | measure x 144, r18, with a foot | 2.6:1 | adopts the card's foot |
| onboarding panel | Object | measure x 111 and x 88, r18 | 3.3 and 4.2:1 | **both** measure x 100, r18 | 3.7:1 | **equalized** |
| permission card | Object | measure, r18 | | unchanged | | survives |
| support block | Object | measure, r18 | | unchanged, its button rebuilt | | survives |

---

# PART D. EVERY PAGE

In the order a person meets them. Every wireframe is drawn at the reference width,
371.4dp of measure, and every vertical figure is from the top of the safe drawing area.
The tutorial is drawn last for reference even though a person meets it third, because it
is an overlay over the page in D.2.

**Three rules that apply to every page and are stated once.**

1. **One axis per page.** Every element takes the alignment of the block it belongs to.
   The single permitted exception is a label centered under the Cell it names.
2. **A serif screen title means you navigated here and nothing else names this place.**
   Every pushed screen: `announce` 31, Newsreader, with the -1.9dp optical start offset.
   **A sans title means the tab bar already named it**: Areas and the Trail take `lead`
   21.5 Hanken. Momentum and the Report take no title at all, because their headline is
   the title and a second one would be the page announcing itself twice.
3. **One bottom reservation everywhere:** `navigationBar + tabBarHeight 61 + tabBarInset
   17 + break 52`. Today Areas reserves `+76` and Momentum, the Trail and the Report
   reserve `+24`, which is why the Trail capture shows a day header rendering under the
   floating bar.

## D.1 Onboarding, four beats, Contemplative

### D.1.1 The persistent nav overlay

```
BEFORE                                        AFTER

+-------------------------------------+      +-------------------------------------+
| <        ---|---          Jump in   | 56   | <        o o o o          Jump in   | 56
+-------------------------------------+      +-------------------------------------+
  ^27.1     ^165.7  245.7       390.1 ^        ^20.0     ^ 4 marks, 38dp     391.4 ^

 back ink 6.5dp inside the measure,           both controls' INK sits on the measure.
 Jump in ink 0.8dp inside it:                 The chevron adopts PushedScreen's own
 the row is 5.7dp out of balance.             offset(x = -(48 - 22)/2), which measures
                                              correct to 0.4dp on Settings.
 an 80 x 2dp bar, white 12 percent
 track, fill = beat / 4                       four marks on an 11dp pitch: the current
                                              one 5dp at textBright, the rest 3dp at
 Jump in: a bare label, no ground              white 22 percent. Inline 36 in a 48
                                              target, press ground white 8 percent.
```

**Why the bar becomes marks, and it is systemic rather than taste.** The app has two
idioms for "where am I in a sequence": an 80dp filling bar in onboarding and the string
`1 of 5` in the tutorial, and a person crosses from one to the other within three seconds
of the iris. One of them has to go. `1 of 5` is the exact shape of 15.1's "numbered
1-2-3 step sequences". A filling bar is one word away from section 14's "no progress ring
or bar pointed at a target", and although that rule is about a person's own data and
onboarding genuinely has an end, when the design has already ruled a form suspect the
other legal answer is the safer one. **Marks are already the app's own idiom**, on the
Pulse rhythm row and on Momentum's fortnight row, and they carry two signals, size and
opacity, which is what section 13 asks for.

### D.1.2 Beat 2, the fork

```
BEFORE                                    AFTER

 106  nav bottom                           106  nav bottom

        280dp above                               213dp above

 366  How do you want to start?           319  How do you want to start?
      readSerif 26, ink at 23.24               speak 26 / 36, ink on 20.0
        42dp                                     rest 32
 424  +--------------------------+ 111.2  387  +--------------------------+ 100
      | Just start   title 19/700 |            | Just start  readStrong 18|
      | One area called Today.    |            | One area called Today.   |
      | Names and colors can      |            | Names and colors can     |
      | come later.      body 15  |            | come later.     read 15  |
      +--------------------------+            +--------------------------+
        14.1dp                                   snug 12
 550  +--------------------------+  88.4  499  +--------------------------+ 100
      | Pick a few areas          |            | Pick a few areas         |
      | Two to four parts of your |            | Two to four parts of     |
      | life to keep track of.    |            | your life to keep track  |
      +--------------------------+            | of.                      |
 638                                           +--------------------------+ 599

        252dp below                                    291dp below
 890                                       890

 FILL: surfaceRaised, an opaque token.     FILL: white at 6 percent, composited.
 Measured 1.2 L* over its own ground at    4.2 L* at the glow's center, 5.0 L* at
 the glow's center, because the glow       the edges. Both inside 6.1's 3 to 5
 lifts the ground and an opaque token      percent band, everywhere on the screen.
 cannot follow it.
                                           HEIGHTS: both 100dp, set by the taller of
 HEIGHTS: 111.2 and 88.4. A 26 percent     the two through IntrinsicSize.Max.
 difference read as weight, on a pair
 Addendum 01 8a requires to be equal.      PLACEMENT: the block's optical center at
                                           45 percent of the reading field. 213:291.
 PLACEMENT: dead centered, 280 : 252,      A 55dp lift, the mat-board proportion.
 so the block sits BELOW the optical
 center, which is backwards.
```

**The fill is translucent white, not the `surfaceRaised` token.** That is a departure
from every other panel in the app and it is measured: the panel sits where the beat's
radial glow is brightest, so the ground rises to meet it exactly where it most needs to
separate. A fill expressed as white over whatever is beneath it holds a constant step
everywhere on the screen; an opaque hex cannot.

**Equal heights, not weighted fills.** The containers report proposed 7 percent and 4
percent to make `Just start` the recommended door. Refused: Addendum 01 8a requires
`Just start` to be "a genuine equal alternative", and section 15's open-choice rule only
operates where the design leaves a choice open. This one is shut. The existing rule, one
composable called twice with no parameter that could make either louder, was satisfied
and the panels were still unequal, because the heights came from how much text each
happened to carry. Fixing them to the taller is the mechanism that rule always wanted.

### D.1.3 Beat 2, Just start

```
BEFORE                                    AFTER

 What is on your mind?   readSerif 26      What is on your mind?  speak 26 / 36
        10dp                                      tight 8
 One thing is enough, and you can          One thing is enough, and you can
 leave it empty and add something          leave it empty and add something
 later.                    body 15         later.                read 15 / 24
        30dp                                      rest 32
 First item           sidehead 13/700      First item            meta 12.5/700
        8dp                                       tight 8
 Rewrite the proposal introduction|        +-----------------------------------+
 ___________________________________       | Rewrite the proposal introduction |48
 ^ a 1dp rule at white 16 percent,         +-----------------------------------+
   brightening to 42 percent on focus,       white 6%, r12, 14 h / 12 v padding
   running the whole measure.                focus deepens the SAME well to 12%
        34dp                                      rest 32
              +-----------+                (          Continue          )   76
              | Continue  | 50, 125.7dp     ^ Anchor rung, Wide 4 columns,
              +-----------+                   white 14 percent, readStrong 18/600,
   white 9 percent on deepBlack:               LEFT ALIGNED on the measure
   it reads disabled when enabled.
   And it is CENTERED on a beat whose
   every other element is left aligned.
```

Three fixes, each with a precedent already in the repo. **The field becomes a well**,
because 10.19 rebuilt the Daylight field as a well in phase 12b to remove exactly this
construction and 15.3 refuses "an accent rule, an accent border or a two dp underline on
a focused text field" by name; the Contemplative twin is the same rule in the other
world, one device going one rank deeper rather than a second device arriving. **Nine
percent becomes fourteen**, which is 10.7's own stated Contemplative fill. **The anchor
takes the axis of the block it belongs to**, which is left here and centered on the Pulse
and the Focus screen, because those whole surfaces are centered and this beat is not.

### D.1.4 Beats 1, 3 and 4

- **Beat 1's closing sentence goes left**, at `speak` 26, on the measure. Today it is
  centered over a stack of four left-aligned demo cards; the cards are the subject and
  the sentence is their caption, so it takes their axis. The gap before it becomes `rest`
  32.
- **Suggestion chips survive**, two to a row at equal widths, for the reason
  `OnboardingBeatTwo.kt` already gives: no chip may read as recommended because its label
  is longer. The row gap becomes `snug` 12 and the label becomes `meta` 12.5/600.
- **`OnboardingMiniCard` stays** at `row` 12dp and 48dp and gains the press ground it has
  never had.
- **Beat 3 is the reveal and draws nothing of its own.** Untouched.
- **Beat 4 is genuinely centered throughout and survives.** One correction: its sample
  Pulse pill is a picture of the real Pulse pill, so its geometry tracks D.7's. If the
  real pill moves and this one does not, the picture stops being true.

## D.2 Areas, the flagship

### D.2.1 The charge, measured

```
title bottom  ->  chips     16.1dp        four gaps, four values,
chips bottom  ->  banner    19.1dp        NONE of them on the 4dp grid
banner bottom ->  card 1    17.1dp        section 6 claims, and three of
card 1        ->  card 2    11.0dp        them within 6.5dp of each other
```

Three different relationships, chrome to control, control to content and content to
content, are drawn as the same relationship, and the one that genuinely differs is the
only one that looks different. Below that:

- the **void is 355.4dp**, 46 percent of the live canvas and **4.13 times the tallest
  element**, with **one content edge and no bottom edge at all**, because it runs to a
  floating bar
- the content block's center sits at 258.9dp and the canvas's optical center is at 388dp,
  so **the page's weight is 129dp above where the eye goes looking for it**
- the FAB is a lone 48dp circle **289dp below the nearest content**, the only ink in the
  bottom half of the screen, and 3.2dp out of line with the bar beneath it
- the chips measure **38.1dp**, which is **9.9dp under the 48dp floor**, on the two
  permanent controls of the flagship screen

### D.2.2 After

```
0                    1        2        3        4        5        6         411.4dp
|<-20->|<-51.9->|<12>|<-51.9->|<12>|... 6 columns / 12dp gutter / 20dp margin
+=========================================================================+  y=0
|                                              status bar over parchment  |
|  Areas                                     [ archive 36 ] [ gear 36 ]   |  38..86
|  ^ lead 21.5 SANS w650, on 20.0 (the tab bar names this place)          |
|            step 20                                                      |
|  ( Focus )   ( Today's Pulse )                                          | 106..154
|  ^ 40dp drawn in a 48 target, raise + card ELEVATION, 6dp amber dot     |
|    INSIDE the pill at the leading edge                                  |
|            step 20                                                      |
|  One area doing most of the week.        voice 21.5 NEWSREADER / 28     | 174..202
|            tight 8                                                      |
|  one completed this week                 meta 12.5/400 inkSecondary     | 210..226
|            step 20                                                      |
+=========================================================================+ 246
| ~~~~~~~~~~~~~~~~ 32dp linear fade to canvas ~~~~~~~~~~~~~~~~~~~~~~~~~~~ | 278
+=========================================================================+
                                                       BAND: parchment,
                     100dp                             full bleed, r0, no
                     UPPER MARGIN                      edge, no shadow.
                                                       Hard top, faded bottom.
+-------------------------------------------------------------------------+ 378
|  o Work by                              meta 12.5/600 in the accent      |  card
|  Look                                   lead 21.5/650                    |  r18
+-------------------------------------------------------------------------+  raise
|  In focus, 7 minutes left                                          v     |  foot 28
+-------------------------------------------------------------------------+ 482
            snug 12
+-------------------------------------------------------------------------+ 494
|  o Today                                                                 |  76dp
|  Rewrite the proposal introduction                                       |
+-------------------------------------------------------------------------+ 570

                     160dp
                     LOWER MARGIN

                                                          ( + )             742..798
                                            64 x 56 pill, 26dp glyph in card,
                                            right edge on tabBarInset 394.4,
                                            morphs to a 20dp squircle on press
   +---------------------------------------------------------------+        812
   |  [ Areas ]      ^          =          o                       |  61dp
   +---------------------------------------------------------------+        873

 TYPE   21.5 serif / 21.5 sans / 15 / 12.5    dominant is the SENTENCE
 SPACE  step 20 | step 20 | fade | snug 12    four gaps, three tokens, all on 4
 GRID   header 6/6, chips hug and terminate ragged, cards 6/6
 VOID   100 above + 160 below, 1 : 1.6, both bounded. Largest void / tallest
        element = 160 / 192 = 0.83, was 4.13.
```

### D.2.3 The two refusals this composition rests on

**No lead card.** The scale report puts the first card's title at `leadStrong` 31 to give
the screen a dominant. Refused on the app's own terms: section 11 states the three second
test as "what is active **everywhere**, at a glance", and a dominant first card answers
"what is active here" instead. It also asserts a ranking the data does not have, because
every area holds exactly one active item and they are peers by construction, and the list
is ordered by hand, so making position one loudest turns a low-stakes drag into a
decision about which part of your life gets to be big. It does not survive eight areas
either, where the screen becomes a hero plus a list, which is a different product.

**No terminal mark.** The scale report bounds the void with a 24dp mark at 12 percent
ink. Refused on measurement: a device that carries meaning is held to 3.0 to 1 by 16.7,
and `inkTertiary` measures 2.34 on the canvas. Raise it until it clears and it is no
longer quiet; leave it below and it carries no meaning, at which point it is decoration,
which section 1 forbids outright. The Mark itself is refused twice over, because 4.2
gives it six applications and none is inside a screen, and a logo at the foot of a page
is a watermark.

**The dominant is the engine's sentence**, which is the one thing on this screen that was
written rather than labeled and is currently dressed as chrome. It gets the app's reading
voice, its own ground, the full width of the screen and the top of the page.

**Why the sentence is `voice` 21.5 serif and not 26.** 10.3 says the active item title is
"the most important string on the screen" and "this never shrinks." A 26sp banner would
outrank it and put the document in contradiction with itself. At 21.5 the sentence sits
at exactly the item title's step, and what separates them is **family, ground, position
and width**, four signals, none of them size. That is 5.1 and 5.2 doing the job they were
written for: the serif is the app speaking, the sans is your things. The sentence still
gains 26 percent in size, the reading voice, the full width of the screen and the top of
the page.

### D.2.4 The anchoring formula, and every case it has to survive

The card field runs from the fade's end to the FAB's top less `snug` 12, which is
**278 to 730dp, a field of 452dp**.

> **upper margin = clamp(air / 2.6, 32, 156). lower margin = air - upper.**

One formula, continuous and monotonic, with no discontinuity as areas are added. The 2.6
divisor produces **1 : 1.6**, which is the mat-board proportion a framer cuts to and is
the oldest answer in the trade to "how do you make emptiness look deliberate". It is
derivable, so nobody guesses.

| areas | stack | air | upper | lower | reading |
|---|---|---|---|---|---|
| 1, bare | 76 | 376 | **145** | 231 | one card floating between two margins. It reads as a plate on a page |
| 1, idle | 104 | 348 | 134 | 214 | the idle card is taller than the filled one, because the invitation carries a foot |
| 2 | 192 | 260 | **100** | 160 | the case in the capture |
| 4 | 340 | 112 | **43** | 69 | the margins have collapsed toward the minimum, and there are still two of them |
| 5, bare | 428 | 24 | 32 clamped | scrolls 8dp | five bare cards effectively fit. 10.3 and 11 are preserved by measurement, not by luck |
| 5, mixed | 496 | | 32 | scrolls 44dp | honest: five areas with sessions and queues is more, and the screen says so |
| 8 | 692 | | 32 | scrolls | top anchored, four and a half cards above the FAB line |

**The offset does not animate on arrival.** A stack that slides into position on every
open is motion that says nothing about the person's data, and section 14 as narrowed in
phase 12b permits only time to move on its own. The composition is in place at frame one.
**It does animate on `springGentle` when an area is added or removed**, because that is a
response to a touch, which is the other half of the same rule.

### D.2.5 The zero areas state

The **Band does not draw**, because it exists only when the engine has a sentence and
with zero areas it has none. A tinted header carrying only chrome is a hero band for its
own sake. Title and chips sit on `canvas`.

```
   38   Areas                                     [archive]  [gear]     canvas
  106   ( Focus )  ( Pulse )               Focus is permanent, 10.1

                     149dp  upper margin

  303   No areas yet.                             speak 26 serif
  339   An area is a part of your life you        read 15 / 24, 2 lines
        want to keep track of.
  431   +-------------------------+               Commit 60, Wide 4 columns
        |     Create an area      |  60dp         = 243.6dp
  491   +-------------------------+

                     239dp  lower margin          NO FAB
```

**Left aligned at the measure, not centered.** Every other sentence on this screen is
left aligned; a centered block in a left-aligned app is a different page arriving. The
statistically common empty state is centered text with an illustration and a button. The
illustration is already forbidden by 10.13, and centering is the half nobody questions,
which is the half section 15 asks to question.

**The FAB is suppressed while this state holds.** Two controls that do the same thing,
20dp and 700dp apart, is an ambiguity this audience pays for, and the FAB's whole
justification is that it is the persistent add for a list that exists. 10.15 specifies
what the FAB does in this state, not that it must be drawn. Flagged in section 12.

### D.2.6 What the eye does

It lands on the parchment, because that is the only ground change above the fold and it
runs the full width of the screen. Inside it, it lands on the serif line, because the
serif is the only shape on the page that is not a rounded rectangle or a sans label and
because it has two `step` gaps of air above it and a caption a third its size beneath it.
It reads one sentence. Then the parchment fades out from under it and 100dp of nothing
says the app has stopped talking. It picks up on the first card, now the only lifted
object on the page, reads the area name at 12.5 and drops to the item title at 21.5,
which is the same size that title takes everywhere else in the app. It runs down the
stack in one column at one rhythm, and where a card has a foot it gets a second, quieter
line on a second ground with a chevron at the trailing edge saying there is more inside.
Then it stops, because below is margin, the one control that adds, and the bar.

### D.2.7 The hard question, answered directly

**With two areas the screen is more than half empty, permanently. What occupies the lower
half, and why is that not filler?**

**It is the page's bottom margin and it contains nothing.** Five things make it a margin
rather than the current hole.

1. **It is one of two margins.** 100dp above the stack and 160dp below it, in a stated
   1 : 1.6 ratio. Content with space on both sides is placed. Content with space on one
   side has fallen.
2. **It is bounded at both ends**, above by the last card's drawn edge and below by the
   FAB at a fixed position and the tab bar. A margin between two edges is a margin. A gap
   with one edge is a hole.
3. **It is smaller than the content it frames**: 160dp against a 192dp block, a ratio of
   0.83 where it was 4.13. Whatever is largest on a screen is what the screen is about,
   and this screen stops being about an absence.
4. **It has a shape**, because the measure changes as the page descends: the Band bleeds
   to the full screen, the chip row hugs and terminates ragged at about 234dp, the cards
   fill the measure, and below them there is nothing. The void tapers rather than sitting
   square.
5. **Its top edge is a ground change, not a card edge**, so the eye reads a transition
   rather than a gap in a list.

And the sentence that has to be said plainly, because every conventional fix violates it:
**with two areas, the correct amount of content on this screen is two areas' worth.** The
statistically common answers, contextual onboarding in the empty space, a suggestion card,
a personalized block, a stat row, all put the app's voice where the app has nothing to
say. This app's entire claim is that everything except the one thing per area waits out
of sight, and its audience is people for whom counters of unfinished work are harmful and
long lists are paralyzing. **A screen that fills its own margin to look busy is the app
breaking its promise in order to look competent.**

## D.3 The area detail sheet

### D.3.1 The charge

The sheet is 496dp of an 800dp screen and **it has no top**: the title row sits at the
same left margin, the same ground and the same rhythm as every row beneath it, and the
handle floats over content belonging to nothing. It has **three section gaps**, 26, 22 and
18dp, against row gaps of 10 and 14dp; grouping at 1.8 to 1 does not read. Its most
important control is a **full-measure by 44dp mint band at 8.4 to 1**, which is the same
object class as the Trail's completed row. And `Add an item`, `Archive` and `Delete` are
**one row form doing create, hide and destroy**.

### D.3.2 After

```
BEFORE  496 of 800                        AFTER  heightIn(min 420, max 620)

+==================================+      +==================================+
|             ====                 |      |             ====                 |  raise
|                                  |      |  o Today                   [ = ] |  HEAD 76
|  o Today                   [ = ] |      +==================================+
|                                  |      |                                  |  raise
|  Active ----------------------   | 26   |  Active ----------------------   |  ZONE
|                                  |      |                                  |
|  Rewrite the proposal            |      |  Rewrite the proposal            |  lead 21.5
|  introduction         21sp/650   |      |  introduction                    |
|                                  |      |  Open the doc and read what      |  read 15
| +------------------------------+ | 44   |  is there.                       |
| |          Complete            | | 8.4:1|                                  |
| +------------------------------+ |      | +--------------------+           |  Commit 60
|                                  |      | |      Complete      |  60dp     |  4 columns
|  Queue -----------------------   | 22   | +--------------------+  243.6    |  = 243.6
|                                  |      |                                  |
|  Nothing waiting.                |      +==================================+  20dp pad
|                                  |      |                                  |  card
|  +  Add an item              48  |      |  Queue ----------------------    |  FIELD
|                                  |      |  Nothing waiting.       read 15  |
|  Area ------------------------   | 18   |  ( + Add an item )               |  Inline 36
|                                  |      |   ^ actionBlue 6%, hug ~128dp    |
|  [ ]  Archive                48  |      |                                  |
|                                  |      |            rest 32               |
|  [T]  Delete                 48  |      | +-----------+  +----------+      |  Standard
|                                  |      | |  Archive  |  |  Delete  |      |  48 each
+==================================+      | +-----------+  +----------+      |  10.20's pair
                                          +==================================+
 no top, 3 section gaps, an 8.4:1
 primary, and three identical rows         a head, one raised zone, plain
 doing add / archive / destroy             fields, one section gap, and three
                                           forms for three consequences
```

### D.3.3 Measured

**The head. 76dp, ground `raise`, full bleed.** 12 handle padding + 4 handle + 12 + 40
title row + 8. Content on the 20dp measure: a 9dp dot + 10dp + the area name at
`readStrong` 18/700. A trailing tune glyph at Inline 36 in a 48dp target with its ink on
the right margin. The head is where a sheet says which sheet it is, and it makes the
handle sit **on** something. **The handle's padding stops scaling with the text size**,
because `ClaritySpacing`'s own doctrine fixes a grip against text scaling and its width
and height are already fixed; the padding around it was missed.

**The Active zone. Ground `raise`, full bleed, 20dp of padding all round.**

| element | spec |
|---|---|
| `Active` sidehead | `meta` 12.5/700/+0.024em `inkSecondary` plus a hairline to the trailing edge |
| sidehead to title | `snug` 12 |
| item title | **`lead` 21.5/650**, the same rank as the card, so the string does not change size when you tap the thing it is on |
| first step | `tight` 8, then `read` 15 `inkPrimary`, in full, 10.17 |
| note | `tight` 8, then `read` 15 `inkSecondary` |
| estimate | `tight` 8, then `meta` 12.5 `inkSecondary`, plain text, once, never against an actual |
| to Complete | `rest` 32 |
| **Complete** | **Commit 60**, Wide 4 columns = 243.6dp, r12, `positiveGreen` 13 percent, `positiveInk` label at `readStrong` 18/600, **left aligned on the zone's 20dp measure** |
| **Swap**, when offered | `snug` 12 to its right, Standard 48, Hug, Secondary |

**Left aligned rather than centered**, which is a departure from the containers report's
78 percent centered measure. Centered gives the control a frame. Left aligned gives it a
**spine**: the item title, the first step and the button all begin at the same x, so the
control reads as the next step after the sentence rather than as a slab centered in a
panel. It also survives the Complete plus Swap pair unchanged, where a centered pair of
unequal buttons reads as accidental. The right-hand 108dp of `raise` is the frame, and a
ragged right edge under a paragraph is what a paragraph has.

**Everything else is a Field: no ground, no edge, a sidehead and air.** One gap value
each, where there were three: sidehead to first row `snug` 12, row to row `snug` 12,
field to field `rest` 32.

**Queue rows are Standard 48 minimum**, which fixes a live floor violation. Title `read`
15/600, note `meta` 12.5, a chevron at 18dp with its ink on the right margin, the whole
row a `Role.Button` with a focus ring and a 5 percent ink press ground, none of which it
has today.

**The empty queue.** `Nothing waiting.` at `read` 15 `inkSecondary` on the measure, then
`snug` 12, then `Add an item`. The two are one gesture: nothing is here, and here is the
way to put something here. The empty line is deliberately not at row rank, so an empty
queue can never be mistaken for a row.

**`Add an item` drops to the Inline rung**: 36dp drawn in a 48dp target, full pill,
`actionBlue` at 6 percent, 16dp side padding, an 18dp `add` glyph + `hair` 6 + a `meta`
12.5/600 label in `actionBlue`, hugging to about 128dp, left aligned on the measure. It
stops being a peer of Archive and Delete, and it gains a container at the smallest rung
because it is the one additive act on the sheet.

**Archive and Delete become 10.20's settled pair, imported wholesale.** Archive is a
Secondary at Standard 48, hugging to about 118dp, ink at 5 percent, r12, `readStrong`
18/600 `inkPrimary`. Delete is that button's geometry with a **transparent ground** and
the `deleteMuted` label. Never a container, per the destructive rule in B.5.

**The `Area` sidehead is deleted** and replaced by `rest` 32 of air. Three sideheads on a
sheet with three things in it is labeling for its own sake: `Active` and `Queue` name
content, and `Area` named a pair of buttons. 6.1 ranks whitespace first and says stop as
soon as it reads; 32dp against the 12dp inside the field is 2.7 to 1 and it reads.

**Three acts, three forms, matched to consequence**, and a person now meets the
Restore-and-Delete pair twice in this app, here and on the archive row, as one
construction at one geometry.

**`heightIn(min = 420dp, max = 620dp)`.** The minimum is new: a sparse sheet needs a body
for the head to head.

### D.3.4 What the eye does

It lands on the handle, which now sits on a raised head rather than floating, and reads
one line: the dot and the area name, which is the answer to "what did I just open". The
head ends in a hard ground change and the same `raise` continues into a taller block
under it, so the eye reads the head and the Active zone as one raised region with a seam
in it, which is what they are. Inside that region it goes to the item title, the largest
sans on the sheet, then down the left spine through the first step to Complete, which
starts at the same x and is the only filled thing on the surface. Then the ground drops
back to `card` and 32dp of air says the raised part is over. Below it are two flat fields
at one rhythm, and the three actions in them are three visibly different objects: a small
blue pill, a filled grey button and a bare word in the delete ink.

## D.4 The Trail

### D.4.1 The charge

```
title ink            73.9 - 96.0    displayTitle 30 serif
chips               121.9 - 160.0   38.1dp, ending at 260.9, 130dp of empty row
day header ink      182.8 - 198.1   bodyStrong 17, a bare numeral, a rule from 90.3
row pitch                  41.7dp   twelve identical lines
```

1. **Twelve identical rows**, so twelve events read as a log dump.
2. **The timestamps are ragged with holes.** MBP section 9 puts a timestamp on the first
   event of each ten minute cluster and the suppressed ones draw in `Color.Transparent`.
   On screen that is `3:39 AM`, three rows of nothing, `4:37 PM`. **The eye reads missing
   data, not clustering.**
3. **The clustering is already computed and thrown away.** The UI keeps only the
   suppression.
4. **The screen's loudest content is a 17sp day header over 15sp rows: 1.13 to 1.** There
   is no dominant element anywhere on it, which is worse than Momentum.
5. The bottom reserve is 52dp short of Areas', which is why the capture shows a day
   header rendering under the floating bar.

### D.4.2 After

```
BEFORE                                    AFTER

 Trail                     30 serif        Trail                    lead 21.5 SANS
                                                                    in a 48dp row
 ( All ) ( o Work by ) ( o Today )         [ All ] ( o Work by ) ( o Today )
   ink     raise+elev    raise+elev         ^12dp   ^ 36dp drawn, FLAT, no elevation
   38dp    38dp          38dp                corner
                                             morph
 Today  4 --------------------------
                                           Today                              4
  ^  Made Rewrite the proposal 3:39 AM     ^ leadStrong 31 / 40        ^ meta 12.5
     introduction active                     no hairline, no 96dp reserve, ink on
                                             the right margin, first-baseline aligned
  +  Added Rewrite the proposal
                                           3:39 AM                    meta 12.5/600
  o  Created Today                           ^  Made Rewrite the proposal
                                                  introduction active
  |  A Pulse was shown                       +  Added Rewrite the proposal
                                             o  Created Today
 Yesterday  8 ---------------------          |  A Pulse was shown

  |  Answered a Pulse with     4:37 PM      Yesterday                          8
     A push
                                           4:37 PM
  |  A Pulse was shown                       |  Answered a Pulse with A push
                                             |  A Pulse was shown
  t  Stopped after 2 minutes  11:40 AM
     on Look                               11:40 AM
                                             t  Stopped after 2 minutes on Look
 41.7dp pitch, a ragged right edge           t  Added 10 minutes to a focus session
 with holes in it, twelve equal                    on Look
 lines, no dominant anywhere                 t  Started a focus session on Look

                                           32dp pitch, no right column at all, four
                                           clusters instead of twelve lines, and the
                                           day is the dominant at 2.07 : 1
```

### D.4.3 Measured

| y | element | spec |
|---|---|---|
| 38..86 | title row 48dp | `Trail` at `lead` 21.5 Hanken w650 on the measure |
| 106..154 | filter row, 48dp targets | **36dp drawn pills, `raise`, FLAT, no elevation**, 16dp side padding, `meta` 12.5/600 |
| 186..226 | **day header** | `leadStrong` **31sp/600/-0.026em, lh 40** `inkPrimary` on the measure. The count at `meta` 12.5/400 `inkSecondary`, right aligned on the margin, first-baseline aligned. **No hairline** |
| 238..254 | cluster head | the time at `meta` 12.5/600/+0.016em `inkSecondary` on the measure, 16dp tall |
| 258.. | rows | a 20dp glyph slot on the measure (glyph 18dp `inkSecondary`, nudged 0.5dp left), `snug` 12, then the sentence at `read` 15/400 lh 24 on the **indent 52dp**, no maxLines, no ellipsis, `hair` 4 of vertical padding each side. **A one-line row is 32dp** |
| | cluster to cluster | `step` **20** |
| | day to day | `break` **52** |

**The timestamp leaves the row and becomes the cluster's head.** That deletes the ragged
right edge, deletes the `Color.Transparent` branch and returns about 55dp of measure to
the sentence, which matters because a Trail sentence has no maxLines and must not clip at
200 percent. Every cluster gets a head, so there are no holes: the eye reads *four things
happened today*, not *twelve lines occurred*.

**The day header is the dominant and the hairline goes with the size.** At 31sp with
`break` 52 above it the header is already a section marker, and a rule on top of it would
be a second separation device on an element that has one, which 6.1 forbids by
construction. Losing the rule also deletes `DAY_HEADER_RULE_RESERVE`, 96dp reserved for a
24dp glyph, and the `widthIn` cap that existed only to stop a long date starving the rule
at 200 percent. **The count stays**, because MBP section 9 requires it and MBP wins on
behavior, and it moves to the right margin where it gives the Trail back the second
column it lost when the timestamps left.

Contrast on the new hierarchy: 31 against a 12.5 modal is **2.48 to 1** and against the
rows' 15 is **2.07 to 1**, where today it is 1.13.

**Honest arithmetic, because two reports claimed a saving that does not exist.** The
Yesterday block in the capture, eight rows in three clusters, measures 362dp today and
**408dp after**. It is **13 percent taller, not shorter**, because the day header grew.
That is the right trade on a screen whose retention is forever and whose only real
problem is finding your way: rows get 23 percent denser, 41.7 to 32dp, and the 46dp goes
into landmarks a person can flick to and land on. **Scanability, not density, is what a
long log is for.**

**The completed row keeps its mint**, the one moment of color on this screen, at `row`
12dp bled to the measure, sitting inside its cluster like any other row, with vertical
padding `tight` 8 against an ordinary row's `hair` 4 so it reads as a moment with
breathing room rather than as a row that is mysteriously 4dp taller.

**The footer** (`That is everything.` or the loader) goes to `meta` 12.5 on the measure,
left aligned, for the same reason the Areas empty state is.

### D.4.4 What the eye does

It lands on a big word. `Today` at 31sp is the only thing on the screen above 15sp and it
is the only thing a log needs to be navigated by. It sees a small numeral on the right
margin telling it how big this day is, decides, and drops into the block. Inside, it
reads a time, then a short stack of sentences at one rhythm at one indent with no
right-hand column and no ragged holes. It reads another time, another stack. When it
flicks it flicks past 31sp words and lands on one, which is the whole interaction and is
the thing the current screen cannot do. The filter chips are flat and small and stay out
of the way until they are wanted, and when one is set its corner squares off, so a person
can tell at a glance that the list beneath is a subset without reading a word.

## D.5 Momentum

### D.5.1 The charge, and it is the worst screen in the app

1. **The largest thing on the screen is 30sp, there are three of them, and they are the
   digits 1, 2 and 2.** The sentence the screen exists to deliver sits beneath them at
   26sp, which is 0.87 times the size of the digit "1". All three clauses of A.3 fail.
2. **Ten of nineteen strings are 12sp and thirteen are 12 or 13sp.** Sixty eight percent
   of the screen is whisper. A screen that is mostly whisper has one headline and a fog.
3. **Fourteen vertical gaps, fourteen distinct values**, from 15 to 69dp, not one pair
   repeating. `SECTION_SPACING` is the only named one.
4. **The two saturated blocks are 115.8 x 52dp and carry one bit each**, `hasActiveItem`,
   which is the entire content of the previous tab where it is stated with the item's own
   name on a card. The row terminates at 263.3dp, leaving **128dp of dead measure** on a
   screen whose focus strip 400dp below runs the full measure. Two right edges on one
   screen with nothing stating which is correct.
5. **The same two areas are drawn twice**, once as tiles with no data and 300dp lower as
   `Area balance` rows with real numbers.
6. **It runs through five time windows in the order 14 days, right now, this week, 14
   days, eight weeks, seven days, seven or more days ago.** A reader has to re-anchor
   their sense of *when* five times, in an order with no direction. For an audience that
   includes time blindness that is not a taste problem. It is the specific cognitive
   failure the app exists to help with, committed by the screen that claims to be a
   mirror of time.

### D.5.2 The spine: one continuous zoom

| | block | window | form |
|---|---|---|---|
| header | dateline, claim, the fortnight's shape | **14 days** | text and 14 circles |
| 1 | This week | **7 days** | three figure rows |
| 2 | Focus, seven days | **7 days** | seven circles |
| 3 | Your areas | **14 days** | rows led by a 44dp Cell |
| 4 | Eight weeks | **56 days** | one line |

Body windows run 7, 7, 14, 56. **Monotonic**, with the header declaring the page's own
window once so nothing below repeats it. No block can move: block 2 cannot precede block
1 because they share a window and the shorter-lens pair must be adjacent, block 3 widens
the lens, block 4 is the widest. **Order is load-bearing.** Adjacent forms are
text-and-circles, figure rows, circles, marked rows, a line: no two neighbors alike, and
circles repeating at the header and at block 2 is a rhyme rather than a repetition,
because they are not adjacent.

### D.5.3 After

```
+----------------------------------------------------------------------+
|                              status bar                              |
|            rest 32                                                   |
|  August 15 to August 28                    meta 12.5/600 inkSecondary|  62..78
|            tight 8                          <- THE DATELINE          |
|  The beginning of a                        announce 31 serif / 40    |  86..166
|  picture.                                  <- THE ONLY DOMINANT      |
|                                               31 : 12.5 = 2.48 : 1   |
|            break 52                                                  |
|  .  .  .  .  .  .  .  .  .  .  .  o  o  (O)   16dp cells, kept       | 218..234
|            tight 8                          9dp active / 5dp idle    |
|  Active 3 of last 14 days                  meta 12.5 inkSecondary    | 242..258
|                                                                      |
|            break 52   <- the header's own air                        |
|  This week ------------------------------------- meta 12.5 + rule    | 310..326
|            snug 12                                                   |
|   1    Completed          21.5 SERIF tabular, right in a 40dp gutter | 338..366
|            snug 12       then snug 12, then read 15 sans             |
|   2    Minutes focused    LAST BASELINE ALIGNED, never centered      | 378..406
|            snug 12                                                   |
|   2    Added                                                         | 418..446
|                                                                      |
|            rest 32                                                   |
|  Focus, seven days ------------------------------                    | 478..494
|            snug 12                                                   |
|   o   .   .   .   o   O   .    7 circles, 5 to 9dp by minutes,       | 506..518
|            tight 8             3dp inkTertiary on a day with none    |
|  2 minutes in the last seven days               meta 12.5            | 526..542
|                                                                      |
|            rest 32                                                   |
|  Your areas ------------------------------------- 14 events          | 574..590
|            snug 12                  ^ TRAILING READOUT ON THE RULE   |
|  +----+                                                              |
|  |####| Work by                        64 percent    44dp Cell, r12, | 602..646
|  +----+ ^78dp                          ^391.4       accent at 60%    |
|            snug 12                     name read 15 | pct meta 12.5  |
|  +----+                                                              |
|  |    | Today                          21 percent   hairline outline | 658..702
|  +----+                                             when idle        |
|                                                                      |
|            rest 32                                                   |
|  Eight weeks ------------------------------------                    | 734..750
|            snug 12                                                   |
|   \    /\    /\__              a 1.5dp line, 44dp tall, full measure, | 762..806
|    \__/  \__/    \_            one dot on the newest week            |
|            tight 8                                                   |
|  Busiest week, 3 completed                      meta 12.5            | 814..830
+----------------------------------------------------------------------+

 TYPE     31 / 21.5 / 15 / 12.5      dominant : modal = 2.48 : 1, and it is a SENTENCE
 SPACE    8 / 12 / 32 / 52           four values, was fourteen
 EDGES    20 for all text, 78 for a name under a 44dp Cell. Two, both earned.
 WINDOWS  14 declared once, then 7, 7, 14, 56.  Monotonic.
```

Two areas and a one-line headline end the page at 830dp against a tab bar at 812, so
**the two-area case fills the screen exactly and the five-area case scrolls.** Today the
two-area case ends at 661dp and leaves 151dp of unbounded void.

### D.5.4 The decisions inside Momentum

**The dateline earns its line.** `August 15 to August 28`, `meta` 12.5/600, on the
measure, from `ActivityDay.dateKey` first and last, which the view model already hands
over. It gives the page a top, which the page does not have. It makes Momentum and the
Report open the same way, which is the argument section 11 already used to give the Trail
a title. And it declares the window once, which is what lets `Your areas` carry a bare
`14 events` on its rule instead of `Of 14 events in the fortnight.` as an orphaned
footnote. The obvious answer was `The last fourteen days`, refused because it repeats
what the dot row's own readout says 160dp lower and because for a person with time
blindness a relative window is the useless half. **A date is a place. A duration is an
arithmetic problem.**

**The headline goes 26 to `announce` 31, and it is the only thing that moves up.** Scale
was never Momentum's headline problem: its cap top sits 43dp below the status bar with a
dot row 69dp beneath it, so **it has no room**. The Report's headline has 115dp of page
above its eyebrow and 36dp of stated air on each side. So the size moves one step and
**the air moves three**: `break` 52 below it against the 28dp that used to follow.
52 : 32 is 1.63, which is a different order from a section gap. 26 against 28 was 0.93,
which is not.

**The three stats become three rows in the serif at `lead` 21.5.** Right-aligned numeral
in a 40dp gutter through `TabularNumber`, then `snug` 12, then the label at `read` 15,
aligned on the last baseline, never centered vertically. Row 28dp, pitch 40dp. Three
columns become three rows because 15.1 lists stat banners as a tell and a three-up KPI
row is one, and because `Minutes focused` is more than twice the length of `Completed`,
so the row already overflows at the default text size and cannot lay out at all at 200
percent. Take the gutter from `IntrinsicSize.Max` over all three figures so the labels
never sit on three different left edges.

**The figures stay in Newsreader.** The scale report proposed 21.5 sans tabular. Refused
on a fact in the repo: 5.2 records that Hanken Grotesk ships no `tnum`, which is why
`TabularNumber` exists and lays digits in slots of the widest digit. Slotting is
invisible on a narrow serif and conspicuously loose on a sans. The size comes down from
30 to 21.5 because A.3 permits one dominant and 31 against 30 is a tie the digits were
winning.

**The tiles are not deleted. They become the rows.** The 60 percent tile stops being a
block of its own and becomes the **leading 44 x 44dp mark of the area's row**, at 12dp
radius, filled at the accent's 60 percent when the area has an active item and carrying a
1dp `hairline` outline when it does not, exactly as today. Fixed size, never scaled,
**top aligned to the name's first line**. Row anatomy: the Cell on the measure, `snug` 12
plus 2, the name at x = 78 in `read` 15 `inkPrimary` with `weight(1f)` and an ellipsis,
and the trailing value right aligned on the margin at `meta` 12.5. Row 44dp, or 56dp with
a quiet subline. Pitch = height + `snug` 12.

| the area's state in the fortnight | trailing | subline |
|---|---|---|
| has events | `64 percent` | none |
| no events, idle 7 days or more | none | `Last active 21 days ago` |
| no events, idle under 7 days | none | none |

**The presence argument, under section 15.** The obvious answer, taken in phase 7 and
logged, is to make the tile as large as three columns allow so that 3.4's "one place
where an area color gets real presence" is true. The result is on the screen: two blocks
that are the loudest objects in the app and say one bit each. **Presence was measured in
area when it should have been measured in relationship.** A 44dp square of a person's own
color at the head of the row that says what that area did with the fortnight has more
presence than a 115dp block that says nothing, because presence is what an element means
and not how much of the screen it covers. The tile's area falls 68 percent and its
meaning goes from one bit to three facts. Section 11's grid was itself an open choice
logged under section 15, and 3.4 states the 60 percent tile without stating its size, so
the size is open and section 15 requires the non-obvious answer.

**The containers report's alternative is refused because it is not buildable.** It puts
fourteen per-area marks on each row. `AreaShare` carries `events` and `percent` and no
per-day series, so that row needs a new query, and the brief forbids new data.

**Row order is the person's own, not busiest first.** `areaBalanceOf` sorts by descending
events. The merged rows take the `tiles` order, which is the order the Areas screen
shows. The statistically common answer is to rank, because every analytics list on every
phone sorts by magnitude. Refused: **a ranked list of a person's own life areas, busiest
first, is a leaderboard**, and for this audience that is a scoreboard of neglect with the
sign flipped. Keeping their own order makes the block a mirror of the previous tab rather
than a chart of it, which is what the screen claims to be.

**One gate moves.** `MIN_AREAS_FOR_BALANCE` nulls the whole module below two areas with
events, on the stated grounds that one area holding a hundred percent of a fortnight says
nothing a person could act on. **That argument is about a comparison and the merged block
is not one.** So the gate moves from the block to the numbers: under two active areas no
row shows a percent and the sidehead carries no trailing readout, and the rows still show
the Cells, the names and any quiet lines. Every claim the composer's own note defends is
preserved, and what is no longer lost is the areas themselves.

**`Quiet areas` disappears as a module.** An area idle seven days or more carries `Last
active 21 days ago` as a subline in the one list where all of a person's areas already
are, from the same `R.plurals.area_last_active_days` the area card uses. 12.2 asks the
idle module to be gentle: **a heading over a list of the areas you have neglected is less
gentle than a line at the bottom of the list of all your areas**, because a heading makes
neglect a category and a subline makes it a fact about one row. The wording does not
change by a character. Only the container does.

**The focus strip becomes seven circles.** Today it is seven 48.6 x 41dp rounded
rectangles, six of them at 10 percent ink, which read unmistakably as unloaded
skeletons. The floor exists for a good reason and a 48dp field of 10 percent ink is not
quiet, it is a placeholder. After: a day with focus is a filled circle running **5dp to
9dp linearly** with that day's minutes against the busiest day, at `inkSecondary`; a day
with none is a **3dp** circle at `inkTertiary`. A 12dp row, `SpaceBetween` across the
measure, with the caption unchanged beneath. **Not the Report's week ribbon**, which
11.1 says "repeats at 60 percent scale in the past reports list and nowhere else", and
better: the ribbon encodes magnitude as height and Momentum's hero row already encodes it
as size and opacity, so seven marks in the fortnight row's language rhyme with the row
300dp above them. **No day initials**, because the ribbon has them only where the caption
makes no per-day claim, and seven more whisper strings is the last thing this screen
needs.

**The trailing readout on a rule.** `Your areas --------- 14 events`, with the label at
the leading edge, the hairline through the middle and the value right aligned on the
margin. This is a variant of 10.12 with **two call sites**, here and the Trail's day
header having refused it, so it is a variant rather than an exception: a sidehead whose
block has a denominator carries it on the rule. **The two graphic captions do not move to
their rules.** `Active 3 of last 14 days` and `2 minutes in the last seven days` stay
beneath their marks, because 11's argument is explicit and correct: a label above a
graphic makes the graphic an illustration of the label.

**One mark language.** Every graphic on Momentum is now a circle on a row at one of two
sizes: 9dp active and 5dp quiet on the whole-app hero row, and 5dp active and 3dp quiet
on the subordinate focus row. Today the screen draws 9dp circles, 5dp circles, 115.8 x 52
rounded rectangles at 11dp and 48.6 x 41 rounded squares at 11dp. **Two graphic idioms
are deleted and nothing is added.**

### D.5.5 What the eye does

**Before**, it lands on the three serif digits, because they are the largest ink on the
page and sit dead center vertically. It reads "1", finds "Completed" beneath it at 12sp,
and has learned nothing. It scans up, because the serif sentence is the only other thing
with weight, and finds it *smaller* than the digit it just left. From there it has no
route: the two colored blocks pull hard, deliver one bit and hand it back to a column of
near-identical 12sp lines at one gap value. That pass takes about a second and a half,
which is why the screen reads as something generated rather than something written.

**After**, it lands on a 31sp serif sentence with 52dp of air under it, because it is the
only thing at its size and the first ink below a small grey date. It reads the sentence.
It drops to the fourteen marks, which are directly beneath the claim and are its evidence,
and reads the texture before it reads the caption, which is the arrangement section 11
asked for and never gave the row the position to earn. Then a 52dp gap, larger than any
other on the page, says a new movement has started, and it descends through four blocks
32dp apart whose forms alternate: figures, circles, marked rows, a line. The lens widens
as it goes and nothing asks it to go back. **The eye finishes at the bottom of the page
rather than at the bottom of the screen.**

## D.6 The Report

### D.6.1 Why it works, because the rest of the app needs these seven reasons

The Report is the best-composed screen in the app and everything below is a correction
rather than a rebuild.

1. **Its order is load-bearing.** Cut the page into blocks and shuffle them and every one
   is lost: the dateline cannot follow the headline, the ribbon cannot precede the claim
   it is evidence for, a digression needs something to digress from, and an address comes
   after the thing it is about. **No other screen in this codebase survives that test.**
2. **Eight blocks, eight forms, no two neighbors alike.**
3. **Exactly one turn.** The pattern break is the only element in the app that leaves the
   20dp measure, and 11.1 says out loud why there is only one: "two would be a layout."
4. **It has a top and a bottom.** 115dp of page above the eyebrow, and a footer at 12sp
   that is unmistakably a signature. No other screen has either, and both are the
   cheapest things on the page.
5. **Three axes and no accidents**: centered display, the 20dp measure for prose, and the
   30dp indent for the aside. Every other screen has one axis and four accidental
   indents.
6. **Type contrast 3.33 to 1 with a dominant that is a sentence.**
7. **It describes, then turns and speaks.** That turn is what makes it a letter rather
   than a summary, and it is the property Momentum is structurally forbidden from copying
   and must substitute for with position and scale.

**And the caution.** The Report is read 52 times a year, which is what buys its
generosity. The lesson is not "use the Report's numbers." It is **give each surface an
order that is load-bearing, at the pacing its frequency earns.**

### D.6.2 Five findings, and the first is a lighting error

**1. Both centers of light are below the elements they exist to light.**
`ReportBackdrop` places two radial gold glows at `HEADLINE_Y = 0.30` and
`CLOSING_Y = 0.86` of the surface height, which on this canvas is 274.3dp and 786.3dp.
The headline's block runs 151 to 237dp, center **194dp**, so its light is **80dp below
it**. The closing line reads at 646 to 681dp, center **663dp**, so its light is **123dp
below it**. And 786dp is not merely low: the bottom scroll-edge fade begins at 796.3dp
and the floating tab bar's top edge is at 812.3dp, so **the second center of light's
brightest point sits 10dp inside the fade band and 26dp under a floating near-white
pill.** The comment in the file explains 0.86 as "for a report short enough not to
scroll", and **that case does not exist**, because `contentInsets()` reserves 118dp at the
bottom. This one defect is most of why the Report reads as flat black with type on it.

```
BEFORE, to scale on the 914.3dp canvas          AFTER

  0 -------------------------------------      0 -------------------------------
115   eyebrow                                115   eyebrow
194   HEADLINE CENTER                        194   HEADLINE CENTER
274   ( ) glow 1     <- 80dp BELOW it        194   ( ) glow 1    HEADLINE_Y 0.21
327   ribbon                                 327   ribbon
663   CLOSING LINE CENTER                    640   ( ) glow 2    CLOSING_Y  0.70
786   ( ) glow 2     <- 123dp below,         663   CLOSING LINE CENTER
796   ===== fade band =====     10dp                            CLOSING_REACH 0.38
812   [====== TAB BAR ======]   INSIDE it    796   ===== fade band =====
914 -------------------------------------    812   [====== TAB BAR ======]
```

`HEADLINE_Y` **0.30 to 0.21**, `CLOSING_Y` **0.86 to 0.70**, `CLOSING_REACH` **0.42 to
0.38** so the second light's falloff ends above the fade band. `HEADLINE_REACH` 0.62 is
unchanged, so the asymmetry 11.1 asks for, the second light both fainter and smaller, is
preserved and sharpened. Neither number is invented: **0.21 is the measured center of the
headline block and 0.70 is where the closing band sits when a reader has stopped to read
it.** 3.3's rule that the light belongs to the room rather than to the scroll is
untouched. What changes is that the room's lights are where the room's objects are.

**2. Six rules in three widths, and one of them has no label.** `GoldRule` draws once at
the body measure under the ribbon, three times inside sideheads and twice at full bleed
around the pattern break. Five belong to a label. **One floats in a 28dp gap with nothing
on either side of it**, and it marks the wrong boundary: reading down the capture, the
page's axis changes between the first-week note and the first sidehead, and the rule sits
42dp above that.

**Delete three rules.** The standalone one under the ribbon becomes `break` 52, which
reads and which lands on the axis change. The two around the pattern break go because
**the tint is already the device**: a rule plus a tint is two devices on one element,
which is 6.1's rule and 15.1's named tell in gold. The capture settles whether the tint
carries alone: gold at 4.5 percent over `deepBlack` composites to roughly L\* 7.5 against
a ground at L\* 4, a 3.5 L\* step, and the band's edges are plainly visible with the rules
present. Removing them removes a line, not an edge. **The law left behind: on the Report,
a rule always follows a label. There is no such thing as a rule alone.** `GoldRule`
becomes an internal of `ReportSidehead`, and the page goes from six rules in three widths
to three in one.

**3. The body has one gap used six times.** `sectionGap` 28 sits between the ribbon and
the rule, the rule and the note, the note and the first section, and every pair of
sections. `HEADLINE_SPACE` 36 against 28 is 1.29, so the headline's air is not
distinguishable from a section boundary, which contradicts 11.1's own word for it,
"generous."

| relationship | before | after |
|---|---|---|
| a sidehead to its prose | 12 | `snug` **12** |
| paragraph to paragraph | 14 | `snug` **12** |
| section to section | 28 | `rest` **32** |
| around the headline | 36 | `break` **52** |
| around the pattern break | 28 plus a rule | `break` **52**, no rule |
| before the closing block | 34 | `break` **52** |
| before the footer | 34 | `break` **52** |

12 : 32 : 52 is 1 : 2.67 : 4.33. Three levels a reader can feel, replacing a metronome.

**4. The closing block has no container while the aside has one.** The page's single most
important sentence, the only thing on it a person can answer, is centered serif on the
bare ground at the bottom of a long scroll, while the **aside** has a full-bleed tinted
band, a 30dp inset and two rules. **The closing block becomes the page's second Band**,
at gold **3 percent**, full bleed, r0, content inset **30dp** to match the aside exactly,
vertical padding **40dp** because it is the end of the page rather than a step aside.

Two Bands and two turns, and they are different turns, which is why this does not break
the one-turn law: the pattern break turns **sideways** into a narrower measure, and the
closing band turns **toward the reader**. And 3.3 asked for two centers of light all
along, so after this change **the two bands and the two lights are the same two places by
construction**, which they have never been. The page's silhouette becomes header, column,
band, column, band, footer. Today it is header, column, band, column.

**5. The accept pill is at the accessibility floor on the app's most designed screen.**
`PILL_MIN_HEIGHT = 48.dp`, which is exactly `minTouchTarget`. It goes to **Anchor 76 x
Wide 4 columns**, gold at 14 percent, side padding 32, label `readStrong` 18/600. The
settle, the fill from the center to the settled value, the label crossfade and the
refusal to brighten, survives exactly as built.

**Gaps above and below the pill become 20 and 20.** 11.1 specifies 24 and 12 and argues
at length that both draw as about 30dp, because the decline's 48dp target carries about
18dp of its own air above its text while the closing line leaves about 6dp of descent
below its own. **That optical argument was made against a 48dp pill and does not survive
a 76dp one**: at 76dp the pill carries 29dp of internal air below its own text, so 12dp
of layout would draw as about 41dp against 30 above and the asymmetry would invert.
Equalizing at 20 and 20 restores 11.1's stated intent under the new geometry rather than
overruling it.

### D.6.3 After

```
+----------------------------------------------------------------------+
|                                       (o)  (o)  (o)   controls, textDim|  75
|                                       + a 6 percent white press ground |
|               Clarity Report . Week of August 21   meta 12.5, centered | 115
|        break 52  (was HEADLINE_SPACE 36)                              |
|              Day one to day                hero 40 serif / 44         | 151
|                  seven.                    textBright, centered        |
|                                  ( ) glow 1 recentered HERE, 0.21     | 194
|        break 52                                                       | 237
|                    | |                     7 marks, 5dp, r2           | 285
|                  | | |                     height and opacity         |
|          . . . . | | |                     UNCHANGED. The best        |
|          F S S M T W T                      graphic in the app.        | 341
|          11 events, 1 completed, 1 added   meta 12.5, centered        | 372
|        break 52  <- was 28 + A RULE + 28. The rule is DELETED and     |
|                     this gap now falls on the axis change.            |
|        This is the first of these. It will  prose 18 serif, centered  | 450
|        say more in a month.                 textDim                    |
|        rest 32                                                        |
| Your week, honestly ---------------------  sidehead + rule, x = 20    | 502
|        snug 12                                                        |
| Your busiest day was Thursday, at 7 events. prose 18 / 28, LEFT       | 526
|        snug 12                                                        |
| Your first completion.                                                | 566
|        snug 12                                                        |
| Work by and a little else.                                            | 606
|        break 52  <- the turn gets movement air, not a section gap     |
+======================================================================+ 690
|      Pattern                     BAND 1, THE ASIDE. gold 4.5 percent, |
|      snug 12                     full bleed, content inset 30dp,      |
|      Three weeks of history is   vertical padding rest 32.            |
|      where patterns start.       Newsreader opsz 28. NO RULES.        |
+======================================================================+ 806
|        break 52                                                       |
+======================================================================+ 858
|                    One thing         BAND 2, THE ADDRESS.             |
|                                      gold 3 percent, full bleed,      |
|         There is not a repeating     inset 30dp, padding 40dp,        |
|              hour in the record yet. glow 2 centered on it            |
|                +----------------------+                              |
|                |     I'll do that     |  ANCHOR 76 x 4 columns        |
|                +----------------------+  gold 14%, readStrong 18/600  |
|                        20dp              (was 48dp, THE FLOOR)        |
|                     Not this week        read 15, textDim, 48 target  |
|                        20dp              (was 24 above / 12 below)    |
+======================================================================+
|        break 52                                                       |
|                Generated on your device        meta 12.5, textDim     |
|                Based on your first week.                              |
+----------------------------------------------------------------------+
```

**The headline stays at 40sp.** A.2 argues it. Its line height goes 43.2 to **44**, which
is the scale report's real contribution here and costs nothing.

### D.6.4 What the eye does

**Before**, it lands on the headline, falls to the ribbon, reads the shape of the week,
takes the caption as confirmation, then hits a rule with nothing on either side of it and
pauses on a boundary that is not one. From there it descends through six identical 28dp
gaps. The prose is good enough that it keeps going, but nothing tells it where it is, and
then the page's most important sentence appears with no ground under it at the bottom of
the scroll with a floating white pill on top of it. **One moment, then a long even walk.**

**After**, it lands on the headline and the ground is brightest exactly there, so the
headline is lit rather than merely large. It falls to the ribbon and then into 52dp of
nothing, which reads as a paragraph break rather than as a missing element, and arrives at
the first sidehead with its axis already changed. It reads three sections 32dp apart at a
pace it can feel is steady. Then 52dp, visibly more than a section gap, and the ground
goes warm and the measure narrows: the page has stepped aside. Then 52dp again, and the
ground goes warm a second time more faintly with the second glow under it, and the page is
speaking to the reader. It finishes on a 76dp gold pill with 20dp of air on both sides and
a way out beneath it. **Two moments, one on each half of the page, each with a light
behind it and a band under it.**

## D.7 The Pulse

### D.7.1 The charge, both states

**The question state.** The sheet is 568dp of which 544 is above the gesture bar. The
handle sits at 355dp and **the next ink is at 495dp**: the top quarter of the room
contains a 34 x 4dp grip and nothing else. The observation sits at 511 and the pills end
at 712, leaving 149dp of black above the content and 178dp below it on a surface whose
column is `Alignment.Center`. The asymmetry has an invisible cause: the acknowledgment is
composed from the first frame at zero opacity so its space is reserved and nothing moves
when it arrives, which is correct reasoning and means the centered column is 48dp taller
than what you can see, **all of it below the pills**. The two pills are **300 x 50dp,
10dp apart**, which is 94 percent of the 320dp observation measure, so the answers are as
wide as the question.

**The answered state, and it is the worse one.** Five strings, **three of them at 17sp**:
the observation, the answer, and the word **`History`**. Type range **17 to 12, or 1.42
to 1**, the flattest Contemplative surface in the app, and its loudest element is a
three-way tie in which the navigation label is one of the three. And **the sentence
shrinks**: `PulseQuestion` sets the observation in `readSerif` 26 and `PulseAmbient` sets
the same string in `bodySerif` 17, so **answering the Pulse reduces the observation by 35
percent**. No section asks for that. The screen currently tells a person that once they
have answered, what was said about them matters less. The History row spans the whole
measure with the word at one end and the chevron at the other: **a 320dp bar with 300dp
of nothing in the middle of it**, and it is the widest control on the surface.

### D.7.2 The move: the room gets a ceiling that never moves

**The 14-mark rhythm row sits at the top of the room in both states and does not move
when the Pulse is answered.** `state.rhythm` is on `PulseUiState` and is available to both
phases; today only `PulseAmbient` draws it. Drawing it in both costs one composable call
and no new data, and it buys three things. **The room gets a top**, so something is in the
upper quarter of a 544dp sheet other than a grip. **The two states become one
composition**, so the crossfade in 8.2 item 11 lands on a page that has a fixed ceiling
and changing contents, which is what the word "settle" means; today they are two different
screens that share a ground. And **the claim gets a frame**: beneath a fortnight of marks
it reads as today's entry in a run of days, which is what it is.

The containers report proposed moving the row **below** the answer for narrative order.
Refused: that order is right for a page and wrong for a room, it leaves the top empty in
both states, and it means the row moves 400dp during the settle, which would be the
largest single displacement in the app.

Safety is unchanged: fourteen independent marks, three states, no run length, no
consecutive count, no per-mark content description, and the row records whether a day was
answered and never what was answered.

### D.7.3 After

```
BEFORE, question                          AFTER, question

+============================+ 346        +============================+ 346
|         ====  34x4         | 355        |         ====  34x4         | 358
|                            |            | . . . . . . . . . . o o(O) | 394
|      149dp of black        |            |    14 marks, 320dp,        |
|      with a grip in it     |            |    THE ROOM'S CEILING      |
|                            |            |    it does not move when   |
|                            |            |    the Pulse is answered   |
|  You added Work by.        | 511        |  You added Work by.        | 462
|      readSerif 26 / 35.36  |            |      speak 26 / 36         |
|  Been meaning to, or just  | 559        |  Been meaning to, or just  | 510
|  occurred to you?          |            |  occurred to you?          |
|      body 15 textDim       |            |      read 15 / 24 textDim  |
|                            |            |                            |
|                            |            |    THE PAUSE. All slack    |
|                            |            |    collects here, between  |
|                            |            |    being asked and         |
|                            |            |    answering.              |
| +------------------------+ | 612        |    +------------------+    | 676
| |      Expanding         | |            |    |    Expanding     |    |
| +------------------------+ | 662        |    +------------------+    | 736
|            10dp            |            |          snug 12           |
| +------------------------+ | 672        |    +------------------+    | 748
| |      Exploring         | |            |    |    Exploring     |    |
| +------------------------+ | 712        |    +------------------+    | 808
|  300 x 50, 6:1, 94% of the |            |   240 x 60, 4:1, 75% of    |
|  320dp measure             |            |   the measure              |
|                            |            |                            |
|  178dp, of which 48 is an  |            |  ack slot, 32 + 24 = 56dp, | 840
|  INVISIBLE ack slot        |            |  bottom anchored           |
+============================+ 890        +============================+ 890


BEFORE, answered                          AFTER, answered

|      172dp of black        |            | . . . . . . . . . . o o(O) | 394
| . . . . . . . . . . o o(O) | 518        |    THE SAME CEILING,       |
|   the SMALLEST element     |            |    IN THE SAME PLACE       |
|   is the FIRST element     |            |                            |
|  You added Work by.        | 565        |  You added Work by.        | 462
|      bodySerif 17          |            |      speak 26 / 36         |
|      SHRUNK 35% ON ANSWER  |            |      NO LONGER 17sp        |
|  Been meaning to, or just  | 597        |  Been meaning to, or just  | 510
|  occurred to you?          |            |  occurred to you?          |
|                            |            |        rest 32             |
|  You answered   caption 12 | 637        |  You answered   meta 12.5  | 566
|  Expanding  bodyStrong 17  | 661        |        tight 8             |
|                            |            |  Expanding  readStrong 18  | 590
|  History                (>)| 725        |                            |
|  ^49.4          ^350.6     |            |         History (>)        | 816
|  320dp WIDE, 300dp EMPTY   |            |    ~86dp, CENTERED,        |
|  TIED WITH THE OBSERVATION |            |    read 15 textDim,        |
+============================+            |    chevron 16, in a 48     |
                                          |    target                  |
 TYPE  17 / 15 / 12 = 1.42 : 1            +============================+
       three-way tie at the top            TYPE  26 / 18 / 15 / 12.5 = 2.08 : 1
```

**Where those offsets come from**, so nobody guesses when the room resizes. The room is
346 to 890, which is 544dp, with `ROOM_PADDING` 26 at each end. **The ceiling is
top-anchored**: 12dp of handle padding, the 4dp handle, then `rest` 32, so the marks sit
at 394. **The claim hangs from the ceiling**: `break` 52 to the observation at 462, then
`snug` 12 to the question. **The control group is bottom-anchored**: the room's bottom
padding, then the 56dp acknowledgment slot, then the group, so two 60dp pills with a
`snug` 12 gap run 676 to 808 and the acknowledgment line runs 840 to 864. **Everything
between the question and the first pill is the pause**, and it is the only part of the
room that changes when the room does.

### D.7.4 The five Pulse decisions

**The pills take the Commit rung at 60dp, 240dp wide, `snug` 12 apart.** The controls
report puts both at Anchor 76 as the app's one named two-anchor exception. Refused, and
the argument is theirs turned around: **the exception should be paid for by the anchors,
not granted to them.** Two 76dp slabs plus a gap is 164dp of control in a 544dp room, 30
percent of the surface in buttons, on a screen whose content is two sentences. And 60dp
is the Commit rung by that report's own definition, because answering a Pulse writes
`PULSE_ANSWERED` to the log and Commit is the rung for "the one action that closes a sheet
or writes an event." The rung is not a downgrade. It is the correct reading of what the
control does.

**240dp is 75 percent of the 320dp measure**, which is the same proportion that stops the
area sheet's Complete control from touching its own edges. **A control narrower than the
text above it makes the text the subject and the control the answer.** At 300 of 320 the
pills are the subject.

Everything good about them survives untouched: identical width and treatment, stacked and
never side by side per 11 and the three-option `quietDay` case, the radial fill from the
tap point at 220ms, and the unselected drop to 30 percent and 4dp.

**The observation holds at `speak` 26 in both states**, the one change that costs nothing
and fixes the flattest surface in the app.

**The History control stops being a row.** `History >`, `read` 15 in `textDim`, a 16dp
chevron, `hair` 6 between them, about 86dp total, inside a 48dp target, **centered on the
room's axis.** The containers report put it at the left margin: refused, because every
other line on this surface is centered and a left-aligned control at the foot of a
five-line centered column introduces a second axis for one word. The Report earns three
axes with a body of prose; the Pulse has five centered lines and cannot pay for two. It
drops from `bodyStrong` `textBright` to `read` `textDim` because it is the quietest thing
in the room and today it is tied for the loudest.

**The acknowledgment's reserve becomes visible.** The pill group bottom-anchors 56dp
above the room's bottom padding, which is the reserved slot's real height, 32dp of gap
plus a 24dp line. Today the reserve is invisible and reads as an unexplained 29dp lean.
Anchored, it is the room's floor, and the acknowledgment fades into a space the eye has
already accepted as empty.

**The slack collects in the middle, in both states.** Ceiling anchored at the top, control
group anchored at the bottom, claim anchored under the ceiling. Whatever height the room
is given, the extra black falls into the gap between the question and the answers, which
is the one place on this surface where empty black is an asset, because it is the pause
between being asked something and answering it.

## D.8 The focus session

### D.8.1 What it already gets right, and every one is a lesson

**One dominant, and it is enormous**: 64sp against a 13sp floor, 4.92 to 1 on the session
screen, where nothing else in the app comes within 1.5x. **One axis.** **It tapers**:
title measure 320, ring 240, pill at least 160, text action about 130, and two of those
are in a 4:3 relation whether or not anyone noticed. **It is balanced top to bottom**,
222dp of indigo above the area label and 210 below the text action, which makes it the
only screen in the app with both a top and a bottom. **Its ground is doing work and it
breathes.** **Six elements, a stated ceiling, and a documented reason the count went from
five to six.** **The ring is deliberately thin**, 6dp of stroke with the weight spent on
a 10dp tip and a 15dp falloff.

### D.8.2 Three findings

**1. The pool of light is 80dp above the ring it is the room for.**
`GRADIENT_CENTER_Y = 0.42f` and 3.3 justifies it in one sentence: "above the middle,
because that is where the ring is and the pool of light is the room the ring sits in."
**That sentence is false and it is falsifiable with arithmetic the codebase contains.**
0.42 x 914.3 = **384.0dp**. Measured off `focus2.png` the ring runs 346 to 589.7, center
**467.8dp**, diameter 243.7 against a specified 240. The light is **72 to 84dp above the
object it exists to light**, and in the capture the ring's upper arc sits in noticeably
brighter indigo than its lower arc, so it reads as a lighting mistake rather than as a
room.

**`GRADIENT_CENTER_Y` 0.42 to 0.48.** With the revised column below, the ring's center
lands at **440dp, which is 0.481**, and swings **0.466 to 0.497** as the item title runs
one to three lines, so 0.48 is the middle of that band and lands exactly on the typical
two-line case. 3.3's two justifications, "above the optical
middle" and "where the ring is", point in opposite directions and 3.3 asserts they
coincide. **"Where the ring is" wins**, because 3.3's own sentence makes the light a
property of the object.

**2. The primary and the tertiary are 2dp apart.** `PILL_HEIGHT = 50` and
`FocusTextAction` sits in a 48dp target, and the only thing separating them is that one
has a 14 percent ground. **Anchor 76** for `End session` and **Standard 48** for `Add 10
minutes`, with real air between them. **The anchor is 240dp, the ring's own diameter**,
which is the one place `Wide` is not four columns, because a relationship a person cannot
name and can see beats a number that relates to nothing. And `FocusTextAction`'s press
scale, which today is applied to the **`Text`** so that pressing it shrinks 17sp of type
by 0.5dp, moves to the accent-at-8-percent pill ground in B.4.

**3. The column's gaps are 14 / 36 / 36 / 6 and none is on a ladder.** Two 36s that are
not the same relationship, a 14 that is a within-group gap, and a 6 that draws as about
30 optically because of a touch target's internal padding.

| relationship | before | after |
|---|---|---|
| the dot to the area name | 9 | `tight` **8** |
| the area label to the item title | 14 | `snug` **12** |
| the item title to the ring | 36 | `break` **52** |
| the numeral to `remaining` | 2 | `hair` **4** |
| the ring to the anchor | 36 | `break` **52** |
| the anchor to the text action | 6 | `step` **20** |

Two `break`s bracket the ring, which is correct: the ring is the movement and everything
else on the screen is either its label or its controls.

### D.8.3 After

```
+----------------------------------------------------------------------+
|                                                        status bar     |
|                                                                       |
|              134dp, and the column is centered in the safe area       |
|                                                                       |
|                    o Today          areaDot 7 + tight 8 + meta 12.5   | 184..200
|                    snug 12                                            |
|              Rewrite the proposal   lead 21.5 / 28, textBright,       | 212..268
|                 introduction        max 3 lines, measure 320dp        |
|                                     (was 26sp, the last off-ladder    |
|                                      sans size in the app)            |
|                    break 52  (was 36)                                 |
|                 .---------------.                                     | 320
|               /                   \    ring 240dp, stroke 6dp,        |
|              |       24:57         |   track white 16%, progress      |
|              |    numeral 64       |   #8BA4FF, tip 10dp #B9C8FF,     |
|              |       hair 4        |   15dp falloff. UNCHANGED.       |
|              |     remaining       |   meta 12.5 textDim              |
|               \                   /  ( ) GRADIENT CENTER MOVES HERE   | 440
|                 '---------------'      0.42 -> 0.48. 440 / 914.3 =    | 560
|                                        0.481, and it swings 0.466 to  |
|                    break 52  (was 36)  0.497 as the title runs one to |
|                                        three lines, so 0.48 is the    |
|         +---------------------------------+  middle of that band.     | 612..688
|         |          End session            |  ANCHOR 76 x 240dp        |
|         +---------------------------------+  = THE RING'S DIAMETER    |
|                    step 20  (was 6)          accent 14%, readStrong   |
|                                              18/600                   |
|                  Add 10 minutes              STANDARD 48dp target,    | 708..756
|                                              readStrong 18/600 in the |
|                                              accent, press: accent 8% |
|                                              pill (was a 3% scale on  |
|                                               the TYPE itself)        |
|                                                                       |
|              134dp                                                    |
+----------------------------------------------------------------------+

 COLUMN   16 + 12 + 56 + 52 + 240 + 52 + 76 + 20 + 48 = 572dp, centered
          in a safe area of about 840dp, so 134dp above and 134dp below
 TYPE     64 / 21.5 / 18 / 12.5      dominant : modal = 5.12 : 1
 WIDTHS   320 title : 240 ring = 240 anchor       4:3, two values
 AXIS     one, centered
 LIGHT    at the ring's center for the first time
```

**And one thing refused, recorded.** The screen never says how long the session was set
for, and for an audience that includes time blindness `of 25 minutes` under the numeral is
the obvious kindness. It is refused: **the arc is the answer.** The ring's remaining sweep
is a continuous statement of the fraction left and it is the only element that carries
that fact. A seventh element would break the count that is this screen's best property.
Section 15: the obvious answer here is genuinely available and genuinely wrong.

**The Focus complete screen takes the same rungs**: `Mark item complete` at Anchor 76 x
240, `Done` at Standard 48 as a text action, `break` 52 between the bloom and the anchor,
`step` 20 between the two controls. Nothing else on it changes.

## D.9 Settings

### D.9.1 The charge, measured

Four content left edges: **20.2** for sideheads, paragraphs and tiles, **23.6** for the
serif title, **61.0** for every row title, and centered for tile labels and the colophon.
Three right edges by ink: **391.2** for the hairline and the switch track, **385.1** for a
check, **384.4** for a chevron. **Sixteen drawn hairlines**, nine row dividers and seven
sidehead rules, on a screen 6.1 says should have needed none. Segments at **42dp**, under
the floor. The badge floating 9.5dp below the title on every two-line row. **The Light
theme tile has no container at all.** The Appearance group's one row starts 41dp right of
the three controls above it, so one group has three left edges of its own.

And the one nobody has said out loud: **the row pitch is 57.1dp and the group gap is 28dp,
which is 1 : 0.49.** A group boundary is drawn *weaker* than a row boundary. The seven
groups exist in the code and are not legible on the screen.

### D.9.2 The badge goes entirely

10.11 already refused the saturated glyph, and its own words are that "here it is not a
signal at all." Three further facts settle it. **Four of the sixteen glyphs are
duplicates**: `reminders` on both `Pulse reminder` and `Five minute warning`, `regenerate`
on both `Replay the tour` and `Replay the welcome`. **One of them is the mark**, on
`About`, and 4.3 says the mark is not an affordance, so the brand is being used as a list
bullet. And **it costs 41dp of measure on every row**, 11 percent of the column, on the
screen where the longest strings in the app live.

The containers report kept a 20dp ink glyph without the square. This goes one step further
and cuts both, and it owes a replacement for the one thing a glyph column genuinely buys,
which is scanning. **The row's identity moves from a leading badge to a trailing state.**
The trailing edge already carries five different silhouettes, a chevron, a switch, a value
plus a chevron, a value alone, and nothing, crushed into the last 30dp while 41dp of the
leading edge is spent on a mark that carries no meaning. After, the trailing state is a
real column with its ink on the margin, and it is the half of the row a person came to
read. **And the index for sixteen rows is not sixteen icons. It is seven sideheads.**

`SettingsGroupColors` becomes dead code, and with it seven hues, the 12.5 percent tint,
`BADGE_SIZE`, `BADGE_ICON_SIZE` and `BADGE_GAP`.

### D.9.3 After

```
+----------------------------------------------------------------------+
|  <-                              48dp target, glyph ink on 20.0       | 58..106
|            snug 12                                                    |
|  Settings                        announce 31 serif / 40, start        | 118..158
|                                  offset -1.9dp so the stem lands on   |
|                                  20.0 with the sideheads below it     |
|            rest 32                                                    |
|  Daily routine -------------------------------------------------------| 190..206
|  ^20.0  meta 12.5/700 +0.024em; rule from ink + tight 8 to 391.4      |
|            tight 8                                                    |
|  Daily reflection                                              >      | 214..270
|  ^20.0  readStrong 18/600 lh 24        chevron nudged +6dp: ink 391.4 |  56dp
|            snug 12                                                    |
|  Pulse reminder                                        ( =====@ )     | 282..338
|                                  ink track, card thumb, ink on 391.4  |
|            snug 12                                                    |
|  Remind me at                                       8:00 PM    >      | 350..406
|                                              read 15/400 inkSecondary |
|            rest 32                                                    |
|  Focus ---------------------------------------------------------------| 438..454
|            tight 8                                                    |
|  Highlight the active session                          ( =====@ )     | 462..518
|            snug 12                                                    |
|  Session length                                    25 minutes  >      | 530..586
|            snug 12                                                    |
|  Five minute warning                                   ( @===== )     | 598..674
|  A mark on the ring, five minutes before the end   meta 12.5/400      |  76dp
|  ^ hair 4 title to caption against snug 12 row to row = 3 : 1         |
|            rest 32                                                    |
|  After completing -----------------------------------------------------| 706..722
|            tight 8                                                    |
|  [ Promote next ][ Choose from queue ]                                | 730..778
|   ^ HUG. The track wraps to 268dp of 371.4, so 103dp of canvas on its |
|     trailing side IS the framing. 48dp segments, r8, 4dp track pad    |
|     inside a 12dp track: 12 - 4 = 8, exactly concentric.              |
|            snug 12                                                    |
|  Completing an item promotes the next one in that      read 15 / 24   | 790..838
|  area's queue straight away.                                          |
|            rest 32                                                    |
|  Appearance -----------------------------------------------------------| 870..886
|            tight 8                                                    |
+=======================================================================+  ZONE
|            step 20                                     raise, bleeds, |  148dp
|  +---------+  +---------+  +---------+                 r0             |
|  |  Light  |  |  Dark   |  | System  |   100 x 84, r12, 9dp gaps      |
|  +---------+  +---------+  +---------+   2dp actionBlue ring, 14dp    |
|     Light        Dark        System      check badge, ALL KEPT        |
|     ^ meta 12.5/600, actionBlue at 700 when selected (was 9.5sp)      |
|            step 20                                                    |
+=======================================================================+
|            snug 12                                                    |
|  Focus, Pulse and Report are always dark by design.    meta 12.5      |
|            rest 32                                                    |
|  Text size                                        readStrong 18/600   |
|   ^ a heading, not a sixth row: rest 32 above it, tight 8 below,      |
|     and no rule                                                       |
|            tight 8                                                    |
|  Small                                                                |
|  Default                                                    v         |  48dp
|  Large                        choice rows, tight 8 apart, on the      |  choice
|  Larger                       20dp measure                            |  rows
|  Largest                                                              |
|            tight 8                                                    |
|  This builds on your phone's own text size setting.    meta 12.5      |
|            rest 32                                                    |
|  Calm mode                                             ( @===== )     |
|  Less motion, softer color                                            |
|   ^ 20.0, the same edge as everything above it (was 61.0)             |
+----------------------------------------------------------------------+

 ONE left edge, 20.0, for the title, every sidehead, every row and every
   paragraph.  Was four.
 ONE right edge by INK, 391.4: the chevron nudged +6dp, the switch track
   and the check already there.  Was three.
 ZERO drawn lines except the seven sidehead rules, each of which is that
   sidehead's own single device.  Was sixteen.
 RHYTHM  4 : 8 : 12 : 32.  Was 2 : 6 : 0 : 28.
```

The screen is about 76dp longer over the first four groups, roughly 10 percent. That is
the whole cost.

### D.9.4 The settings row, measured

| element | before | after |
|---|---|---|
| leading | a 26dp badge, r8, one of seven hues at 12.5 percent, a 15dp glyph | **deleted** |
| badge to title | 14dp | n/a |
| title | `body` 15/600 at x = 61.0 | **`readStrong` 18/600, lh 24, at x = 20.0** |
| title to caption | 2dp | **`hair` 4** |
| caption | `caption` 12 | **`meta` 12.5/400/+0.022em, lh 16** |
| one-line row | 56dp min, 10dp padding | **56dp**: a 24dp line plus 16dp each side |
| two-line row | 78dp | **76dp**: 16 + 24 + 4 + 16 + 16 |
| trailing value | `caption` 12 | **`read` 15/400**, right aligned, ink on the margin |
| chevron | 18dp, box on the margin, **ink 6.5dp short** | 18dp, **box offset +6dp so the ink lands on the margin** |
| row to row | a 1dp hairline, zero air | **`snug` 12 of air, nothing drawn** |
| sidehead to its first row | 6dp | **`tight` 8** |
| group to group | 28dp | **`rest` 32** |

**The switch track goes from `actionBlue` to `inkPrimary`, with a `card` thumb.** 3.1
scopes `actionBlue` to a control you press and a switch track is a state readout. In the
capture the two saturated `#004BAE` blobs are the loudest objects on a screen whose entire
content is words. **Ink is quieter and more emphatic at once**: on a page of grey and near
white the darkest object is where the eye goes, and it does not shout to get there. It is
also the same ink fill the selected segment speaks 200dp below.

### D.9.5 The segmented control, and the one Zone

```
BEFORE                                     AFTER
[ Promote next ][ Choose from queue ]      [ Promote next ][ Choose from queue ]
 42dp segments, weight(1f), so unequal      48dp segments, HUG, so each is its
 labels get equal boxes                     own label plus 2 x 14dp
 3dp pad in a 12dp track: 12 - 3 = 9,       4dp pad in a 12dp track: 12 - 4 = 8,
 one dp off concentric                      exactly concentric
 the track fills the measure                the track wraps to 268dp of 371.4
 UNDER THE 48dp FLOOR                       103dp of canvas on its trailing side
```

**The Hug is the framing, so this control gets no Zone.** The containers report wrapped it
in one to stop it reading as "a row that got taller." It does not need one: once the
segments hug their labels the track has stopped touching both edges, which was the whole
complaint, and a second Zone on this screen would make the Zone a rhythm instead of an
exception.

**At large text the control becomes a choice list, triggered by a measurement rather than
a threshold.** When the track's intrinsic width exceeds the content column, render the two
options as choice rows instead, the same component the text size list uses 300dp below. A
measured trigger survives translation where a font-scale threshold would not. At the 200
percent cap the track wants 462dp against 371.4 available, which is what breaks it today.

**The appearance picker gets the app's only settings Zone**, and its reason cannot
generalize: it is the one control whose content is three miniature renderings of the app's
own canvas colors, **one of which is the color of the page it is drawn on**. `raise`
`#F4F3F0` at L\* 95.8 against `canvas` `#E6E6EC` at L\* 91.4 is a 4.4 L\* step, so the
Light tile's `canvas` miniature becomes a recess in its surround and reads for the first
time. In dark the same relationship holds with `raise` `#18181F` over `canvas` `#0E0E13`,
which is the world where lightness is the only device available anyway. One device on the
Zone, one on each tile, no border.

**Two row heights on this screen, and that is variation with a reason.** A setting row is
56dp and a choice row is 48dp: a setting is a thing with a state, a choice is one option
inside a setting.

### D.9.6 The permission card, the support block and the colophon

- **The permission card** keeps its 18dp radius, its `card` ground and its elevation. Only
  its air changes: `step` 20 above and `rest` 32 below, so it reads as belonging to the
  group above it. Today it is 14 above and 28 below, which says nothing.
- **The support button becomes a Commit 60 in a full pill, hugging to about 248dp, left
  aligned on the card's own 18dp padding.** Today it is a 50dp 12dp-radius button filling
  the card's 335dp column, so the padding is its only frame. 248 of 335 is a 74 percent
  measure. **The pill is the permanence law, not a preference**: it opens a browser and
  writes nothing, so the one control in the app that asks for money stops looking like the
  control that completes an item.
- **The colophon goes left**, at `meta` 12.5 on the measure, `break` 52 above. The
  statistically common version footer is centered and tiny, and it is the one place on
  this page where the axis breaks, for a string a person is asked to quote in a bug report.

**One filled control on the surface, checked.** The support button. Switch tracks are
state readouts, the selected segment and the check badge are selections, the permission
card is a container.

### D.9.7 Settings is not the app in a plainer register. It is a fourth vocabulary.

A leading icon column, a 61dp text indent, hairlines between rows, seven per-group hues
and 9.5sp labels. **Not one of those five things appears anywhere else in the product.**
That is why it reads as the most generic screen in the app: it is not underdesigned, it is
designed to a different system. After this it is built out of the app's own kinds and
nothing else, and the proof is a test anyone can run by eye: **take a row off the
redesigned settings screen and drop it into an area sheet, and it fits.** Today it does
not, and that is the whole of the charge.

## D.10 About

No capture exists for this screen, so it is derived from `about/AboutScreen.kt` and
`strings.xml`. It contains the worst single composition defect in the app, and it is
invisible in code review because the two halves are sixty lines apart.

```
BEFORE                                    AFTER

  <-                                        <-      glyph ink on 20.0
                                                    rest 32
  About            displayTitle 30 serif    +--------+                        138..200
  ^ TITLE ONE                               | [mark] |  62dp on #141A2E, r18
                                            +--------+
  +--------+                                          snug 12
  | [mark] |  62dp, r16                     Clarity Now       announce 31 serif  212..252
  +--------+                                          hair 4
                                            Version 0.11.0 by Kamsiob  meta 12.5 256..272
  Clarity Now      displayTitle 30 serif     ^ the mark, the name and the version
  ^ TITLE TWO, one hundred dp below            are ONE block, and they ARE the
    the first one. The screen                  title of this screen.
    announces itself twice.                            rest 32
  Version 0.11.0 (dot) by Kamsiob           One active item per area. Everything    304..416
                                            else waits its turn. No account, no
  One active item per area...  bodySerif 17 cloud, no subscription, no ads, and
                                            nothing collected. Your history lives
  Clarity Now is a productivity tool...     in one file on this phone until you
                               caption 12   delete it.          prose 18 serif / 28
                                                      step 20
  Elsewhere ----------------------          Clarity Now is a productivity tool.     436..468
                                            It does not provide medical advice,
  [^] YouTube            @kamsiob   48      diagnosis or treatment.   meta 12.5
  [^] Source code   github.com/...  48                break 52
  [^] Website        kamsiob.com    48      Elsewhere --------------------------    520..536
  [^] Kamsiob Lab      telegram     48                tight 8
  [M] Feedback   hello@kamsiob.com  48      YouTube                @kamsiob  [^]    544..592
   ^ FOUR IDENTICAL leading glyphs,          ^ read 15/400   meta 12.5 ^   14dp,
     and five rows abutting with               inkPrimary    inkSecondary  ink on
     zero air between them                                                 391.4
                                                      tight 8 between rows
                                            Source code      github.com/kamsiob [^]
                                            Website              kamsiob.com    [^]
                                            Kamsiob Lab             telegram    [^]
                                            Feedback       hello@kamsiob.com    [^]
                                                      break 52
                                            [ the support block, identical to
                                              the one on Settings ]
                                                      break 52
                                            Clarity Now is free software under...
                                            Newsreader and Hanken Grotesk are...
                                            Material Symbols, AndroidX and...
                                             ^ meta 12.5, tight 8 apart
```

**About drops its `PushedScreen` title.** It is the only pushed screen in the app whose
first content element is the app's own name at display size, so `title = null` is a
variant used exactly once with a stated reason rather than a general escape. The back
glyph stays, so 10.15's "every screen has an obvious way out" is unaffected.

**The leading arrow moves to the trailing edge and shrinks to 14dp.** What that glyph says
is "this leaves the app." Said four times in a column at the leading edge it is
decoration; said once per row at the trailing edge, after the destination, it is the
affordance. And it is deliberately **not** a chevron, for exactly the reason
`ExternalLinks.kt` already gives for having no chevron: a chevron in this design means a
screen inside the app. So the trailing mark is the correct form and the leading one was
the wrong one.

**Rows gain `tight` 8 of air.** Five 48dp rows abutting is a 240dp wall.

**One honest near-miss, recorded rather than contorted around.** Counting each row's label
and its destination as separate strings, `meta` 12.5 carries 8 of the 14 strings on the
first screenful, which is 57 percent and over A.3 clause 3's half. Counting a row as one
object it is 36 percent. **A list of ten cells is one object with ten parts**, and the
page is not reshaped to make a counting rule come out even.

## D.11 The archive

### D.11.1 What is right, first

**The archive row is the best-composed container in the app and it is the model the area
card should have followed.** Four ranks: identity at 45 percent, subject at item-title
rank, a quantity, and two actions. Two actions at deliberately different weights, which is
10.20's most careful argument. And an aspect of 2.9:1, squarely in the Object band, unlike
every other container that shipped. **Nothing in 10.20's reasoning is disturbed below.**

Two things are wrong and both are inherited rather than local. The two buttons are
`weight(1f)` each, so they split the card and read as a segmented control rather than as a
weighted pair, and `weight(1f)` is the one width that makes a deliberately weighted pair
equal. And they float in the card's single ground, so the card is a stack of four lines
like every other container in the app rather than a thing with an anatomy.

### D.11.2 After

```
BEFORE                                    AFTER

 <-  48dp target                           <-  48dp target on the measure

 Archive           displayTitle 30 serif   Archive          announce 31 SERIF
                                                            (a pushed screen keeps
                                                             the serif)
                                                   rest 32  (was 22)
 +-------------------------------------+  +-------------------------------------+ card
 |  o Recipes              21/650      |  |  o Recipes             lead 21.5    | r18
 |  4 items                12/400      |  |  4 items               meta 12.5    | 72dp
 |                                     |  +-------------------------------------+ raise
 | +------------+ +------------------+ |  | +-----------+  +----------+         | FOOT
 | |  Restore   | |     Delete       | |  | |  Restore  |  |  Delete  |         | 72dp
 | +------------+ +------------------+ |  | +-----------+  +----------+         |
 +-------------------------------------+  +-------------------------------------+ 144dp
   weight(1f) each: two equal halves,       Hug, left aligned, air to the right:
   which is a segmented control              a weighted pair, and the same pair
   130dp, one ground                         the detail sheet now shows
```

| element | spec |
|---|---|
| body padding | 18dp horizontal fixed, `snug` 12 vertical |
| identity row | a 7dp dot at **45 percent** + `tight` 8 + the name at `lead` 21.5/650 `inkPrimary`. Row 28dp |
| to the count | `hair` 4 |
| count | `meta` 12.5 `inkSecondary` at the dot's indent, `4 items` or `Nothing inside` |
| **foot** | **72dp**, ground `raise`, 16dp horizontal fixed, inside the card's own 18dp clip |
| **Restore** | Secondary, Standard 48, Hug to about 112dp, ink 5 percent, r12, `readStrong` 18/600 |
| gap | `snug` 12 |
| **Delete** | identical geometry, transparent ground, `deleteMuted` label |
| row to row | `snug` 12 |

Body 72 plus foot 72 is **144dp at 2.6:1**, in the Object band, and it now shares one
anatomy with the area card: **the body says what it is, the foot says what you can do or
what state it is in.** That is the container system doing its job across two screens
rather than being asserted once. It also means a person meets Restore-and-Delete twice in
this app, here and at the foot of the detail sheet, as one construction at one geometry.

**The empty state is unchanged in substance**, left aligned on the measure at `speak` 26
plus `read` 15, `rest` 32 below the title, no button, the back glyph as the way out. The
two-state distinction between *empty* and *not loaded yet* survives untouched, because it
is the most carefully reasoned thing on the screen.

## D.12 The re-entry screen

Fourteen days away. Four strings, two controls, and **nobody building or testing this app
daily will ever see it**, which is why every rule on it is checked by a test rather than
by looking.

### D.12.1 The charge

```
BEFORE
  Everything is where you left it.       readSerif 26, two lines            70..142
        10dp
  Keep it that way, or start fresh by putting the active                   152..220
  items back in their queues. Nothing is deleted and       body 15
  nothing is marked complete.
        28dp
  +-------------------------------------------------------------+          248..298
  |                 Keep everything as it is                    | 50dp
  +-------------------------------------------------------------+
        8dp
  ...............................................................          306..356
  .        Put active items back in their queues                . 50dp
  ...............................................................

  A FULL-MEASURE, 50dp, FULLY TRANSPARENT rectangle, 8dp beneath a live
  filled blue button of the identical width, at the identical label size,
  in the identical hue. It writes events. There is no undo here.

              556dp of unbounded canvas
              = 66 percent of the live canvas
              = 11.1 x the tallest element on the screen
              ONE content edge, at the top. No edge below.

  DOMINANCE: modal 15sp carries 3 of the 4 strings. Clause 3 fails by a
             wide margin. The screen is one size.
```

### D.12.2 The shape law settles it, and the answer is already in the source

`ReEntryRoute.kt:93` carries the fact that decides this screen's geometry:

```
/** The default, and the one that costs nothing. Nothing is written to the log. */
suspend fun keepEverything() { preferences.setReEntrySettledOn(returnedOn) }
```

against `putItemsBack()`, which calls `repository.putActiveItemsBackInTheirQueues()`.

**So under the permanence law in A.4, the default is a full pill because it writes
nothing, and the option that writes has no container at all and shows a 12dp rectangle
only under a finger.** The safe control and the writing control end up maximally
distinguishable, and not one line of that came from taste.

### D.12.3 After

```
+----------------------------------------------------------------------+
|             TOP_OF_THE_TITLE 20, unchanged: this line sits where the  |
|             Areas screen's own title sits.  11.2, untouched.          |
|  Everything is where you left it.        speak 26 serif / 36          | 70..142
|             tight 8                                                   |
|  Keep it that way, or start fresh by putting the active               | 150..222
|  items back in their queues. Nothing is deleted and   read 15 / 24    |
|  nothing is marked complete.                                          |
|                                                                       |
|              237dp        UPPER INTERVAL                              |
|                                                                       |
|  (          Keep everything as it is          )                       | 459..519
|   ^ Commit 60. HUG to about 258dp. FULL PILL. actionBlue ground,      |
|     card label at readStrong 18/600. A pill because it writes NOTHING.|
|             step 20                                                   |
|  Put active items back in their queues                                | 539..587
|   ^ Standard 48. Full measure. NO GROUND AT ALL. actionBlue label at  |
|     read 15/600. Press ground: actionBlue 6 percent at 12dp, the      |
|     shape of a control that writes, shown only under a finger.        |
|                                                                       |
|              303dp        LOWER INTERVAL                              |
+----------------------------------------------------------------------+

 The options group centers on ITS OWN field's optical center at 45 percent,
 the same rule the Areas card stack takes.  237 : 303 = 1 : 1.28.

 FOUR differences between the two controls, where there was one:
   height   60dp   /  48dp
   width   258dp   / full measure
   ground  filled  /  none
   label   18/600  /  15/600
 A person aiming at "the button" now has exactly one rectangle to aim at.

 DOMINANCE: 26 / 15 / 18 / 15.  Modal 15 at 2 of 4 = 50 percent.
            Dominant 26 / 15 = 1.73x = three ladder steps. Both clauses pass.
```

**The serif line does not move off the top.** 11.2 chose top alignment with a reason worth
keeping: it makes the screen after this one look like the same screen with the sentences
taken out. So this screen mirrors its sibling exactly. **The serif line sits where the
Areas title sits and the two options sit where the card stack sits**, at the same 45
percent optical center, and the controls land at 459 to 587dp, which is inside the thumb
arc, instead of at 248 to 356 where nothing reaches.

**Reading the remaining void honestly.** It is still about 540dp of 840, roughly 64
percent, and the largest interval is 5.0x the tallest element. **That number does not come
down**, because the screen has four strings on it and 11.2 forbids adding anything. What
changed is that the emptiness has two edges instead of one and the block between them is
framed at the mat proportion, which is the difference between a page that is quiet and a
page that is unfinished.

**Above 72 percent occupancy the block top-anchors and scrolls**, which the existing
`verticalScroll` already supports. At the 200 percent cap the content is roughly 700dp
against a 668dp field, so it crosses.

## D.13 The tutorial

The mechanism underneath this overlay is the best-argued code in the app and none of it
changes.

```
BEFORE                                     AFTER

 Areas            [archive]   Skip         Areas       [archive] [gear]  ( Skip )
                              [gear]                                      ^ Inline 36
                              ^^^^^^                                        in a 48
 THE WORD "Skip" IS DRAWN ON TOP OF THE      deepBlack pill in a 48dp        target
 SETTINGS GEAR, visible in the capture,      target, meta 12.5/600 in
 at bodyStrong 17, the same rank as the      textDim, label ink on the
 tutorial card's own title.                  margin, at statusBar + snug 12.
                                             An OPAQUE ground, so it can
 +--------------------------------------+    never sit transparently on
 |                                      |    the app again.
 |  Add anything          bodyStrong 17 |
 |            6dp                       |  +-----------------------------------+
 |  Write it down without choosing      |  |            18dp                   |
 |  where it goes. It waits in your     |  |  Add anything    readStrong 18/600|
 |  inbox until you file it.   body 15  |  |            tight 8                |
 |            14dp                      |  |  Write it down without choosing   |
 |  1 of 5              Tap to continue |  |  where it goes. It waits in your  |
 |  ^ caption 12         ^ NOT A CONTROL|  |  inbox until you file it. read 15 |
 +--------------------------------------+  |            step 20                |
  full measure, 331dp between the two      |  o o o o o          ( Tap to      |
  footer items, r16                        |  ^ 5 marks, 11dp       continue ) |
                                           |    pitch, current      ^ Inline 36|
 "Tap to continue" is a Text. The control  |    5dp textBright,       in a 48  |
 is a full-screen invisible Box. Keyboard  |    rest 3dp white 22%    target,  |
 and switch users can reach Skip and       |            18dp          press:   |
 nothing else.                             +--------------------------white 8%-+
                                            5 of 6 columns = 307.5dp, r18,
                                            leading aligned, 63.7dp of dimmed
                                            page on its trailing side
```

**The card is five of six columns, not a constant.** That is 307.5dp on the reference
device and 264.7 at 360dp, and it scales without anybody redoing the arithmetic. The
63.7dp of dimmed page on its trailing side is what makes it read as a note pointing at
something rather than as a page section.

**It stays leading aligned at every step, deliberately.** A rule putting the card on the
side away from its target would be a function of the target rect rather than a branch on
`step`, so the file's own doctrine would permit it. It is refused anyway, because **that
file's single best property is that there is no branch anywhere in it**, and a 307.5dp
card overlaps its target's column in almost every case regardless. One rule, zero
branches, preserved.

**`Tap to continue` becomes a real control**: an Inline 36 pill in a 48dp target carrying
`onAdvance`, focusable, labeled, with a press ground. The full-screen tap layer stays,
because 13.2 requires tap-anywhere, so the gesture is unchanged and the keyboard path
exists for the first time.

**Skip gets an opaque ground rather than a new position.** Moving it would cost
predictability, which is this audience's stated need. A `deepBlack` pill is honest about
what it is: the Contemplative overlay saying this control belongs to it and not to the app
underneath. `textDim` on `deepBlack` measures 5.63 per the file's own note.

**What survives untouched, and it is most of the file**: the radial dim running 44 percent
at the cutout to 56 percent at the corners with its center traveling with the spotlight,
the sixteen-stroke feather, the `DstOut` knockout, four springs given one spec,
`tooltipTop` as a pure function of eight numbers, and the no-branch rule.

---

# PART E. TWO WORLDS, ONE APP

The systemic question, and it has a specific answer.

> Onboarding is dark and ceremonial, the app is light and plain, and Settings is a list.
> A person's first ten minutes crosses all three. Does it feel like one thing?

**No, and the reason is not the two worlds.** The two worlds are settled, correct and
load-bearing. The reason is that **five idioms are each drawn twice, differently, on
either side of the seam**, and a person crosses all five inside ten minutes.

| time | surface | what is learned | what contradicts it |
|---|---|---|---|
| 0:00 | beat 1 | a card is 18dp with a 7dp dot, an area label and an item title | nothing. **This crossing works** |
| 1:00 | beat 2 field | **a field is a hairline rule** | forty seconds later, in the app, a field is a well |
| 1:00 | Continue | **the thing that continues is a faint centered pill** | in the app, the thing that commits is a blue left-anchored rectangle |
| 0:00 to 1:30 | nav | **where you are is a filling bar** | thirty seconds later the tutorial says `1 of 5` |
| 1:30 | the iris | the real Areas screen | nothing. **This is the app's best moment** |
| 4:00 | **Settings** | a leading badge column, a 61dp indent, sixteen hairlines, seven hues | **none of those five things exists anywhere else in the app** |
| 5:00 | About | two serif titles, four identical arrows | |

**The app is one thing when the component set is world-independent and only the palette
resolves per world.** Today three components are world-dependent by construction, and two
of the three differ in **form** rather than only in color. `OnboardingControls.kt:57`
gives the correct reason for the twin file, which is that everything in `ui/components`
resolves `LocalClarityColors`. **That reason justifies a color twin. It never justified a
form twin, and that is the drift.**

**Six invariants that hold in both worlds.**

| # | invariant | Daylight | Contemplative | broken today by |
|---|---|---|---|---|
| 1 | **One measure, one axis.** A page has one left edge and one alignment | 20dp | 20dp | the settings 61.0 indent, the centered colophon, beat 2's centered Continue, beat 1's centered sentence |
| 2 | **The four rungs.** A control's height says what it does, not which world it is in | 36 / 48 / 60 / 76 | the same | onboarding's 50dp anchor, the 42dp segment, the tutorial's non-control |
| 3 | **A field is a well.** Never a rule, never a border, one rank deeper on focus | `raise`, focus to `canvas` | white 6 percent, focus to 12 | `OnboardingField`'s hairline |
| 4 | **A position in a set is a row of marks.** Never a bar, never a numeral | the Pulse row, Momentum | the onboarding nav, the tutorial | the 80dp bar and `1 of 5` |
| 5 | **One filled control per surface** | one filled button | one filled anchor | the re-entry screen |
| 6 | **Shape says permanence.** A full pill is reversible or a place you can leave; a 12dp rectangle writes an event | the same | the same | the support button and the re-entry default, both rectangles that write nothing |

**What still separates the worlds after all six hold**, because flattening is the risk:
the **ground** (`canvas` against `deepBlack`), **elevation** (paired shadows in the light
world, lightness only in the other two, 6.1), **the center of light** (a radial glow, the
specks, the Focus gradient, the Report's two glows, and Daylight has none and never gets
one), **the axis of attention** (a Daylight page is a set of objects you act on, a
Contemplative room holds one thing you attend to), and **the motion budget** (the iris,
the nine second beat, the crossfades, the twenty two second depth). **Five differences,
all of them atmosphere, none of them form.** That is what "two worlds, one app" means, and
it is a stronger claim than the app currently earns.

---

# PART F. THE DIFF AGAINST design-v3.md

**These are proposals. `design-v3.md` is not edited by this document.** Each row quotes
the sentence it supersedes so the decision is visible rather than inferred. Nothing here
changes behavior or data, and nothing here contradicts `MASTER_BUILD_PROMPT.md`.

## F.1 Section 5.3, typography

| quoted today | proposed | reason |
|---|---|---|
| "The sans sizes are a ladder: 13, 15, 17, 19, 21, one even step per rung" | **12.5, 15, 18, 21.5, 31, 64 on a 1.200 ratio** | an arithmetic ladder's ratio shrinks as it climbs, 15 percent from 13 to 15 and 10.5 percent from 19 to 21, which is backwards. Nine of fourteen roles were inside one octave |
| "bodyStrong \| Hanken Grotesk 17, w600, lh 1.35" and "title \| Hanken Grotesk 19, w700, lh 1.26" | **one role, `readStrong` 18sp, lh 24** | 1.118 apart. 5.3 already worried in writing that "one step is as far as either can move" |
| "label \| Hanken Grotesk 13, w600", "sidehead \| Hanken Grotesk 13, w700", "caption \| Hanken Grotesk 12, w400" | **one role, `meta` 12.5sp, at 600 / 700 / 400 with the three tracking values kept exactly** | 12 to 13 is 1.083 and invisible. The weight and tracking distinctions, which are what read, are untouched |
| "swipeLabel \| Hanken Grotesk 10.5, w700" | **deleted; the swipe label takes `meta` 12.5/700** | 10.5sp was the app's smallest type on its highest consequence control |
| "closingLine \| Newsreader 24, opsz 34, w400, lh 1.42" | **merged into `speak` 26** | 1.083 apart, and the two never appear on the same screen |
| "bodySerif \| Newsreader 17, opsz 17, w400, lh 1.62" | **`prose` 18sp, opsz 18, lh 28** | joins the ladder at s1 and lands on the 4sp line grid |
| "displayTitle \| Newsreader 30, opsz 48, w400, lh 1.2" | **`announce` 31sp, lh 40** | one step on the ladder, and a line height on the grid |
| lh 1.08, 1.36, 1.42, 1.62, 1.26, 1.5, 1.35 and the rest | **every line height a multiple of 4sp** | four of fourteen were. Every block under the other ten pushes what follows it off the grid by a fraction of a dp that accumulates down the page |
| (no role) | **`voice`, Newsreader 21.5sp, opsz 26, lh 28** | the Areas band sentence, D.2.3 |

**Not changed:** `body` at 15sp, argued in 5.3 and used by 10.11, is the base of the new
ladder. `displayHero` stays at **40sp** and only its line height moves to 44.

## F.2 Section 6, shape and spacing

| quoted today | proposed | reason |
|---|---|---|
| "Momentum tiles 11dp" | **12dp**, folded into `row` | one call site, 1dp off its neighbor. It is a Cell |
| "Color picker swatches 16dp" | **18dp**, folded into `card` | one call site, 2dp off its neighbor |
| "Settings icon badges 8dp" | **deleted with the badge**, D.9.2 | 10.11's own argument for neutering the badge's color, applied one step earlier |
| `markBadge` 16dp (code, not the document) | **18dp** | the same collapse |
| "Screen padding 20dp" | unchanged, now named `step` | it was already the right number. Its problem was that four other indents existed beside it |
| "Card padding 18dp horizontal, 17dp vertical" | **18 horizontal, 12 vertical** | 17 is off the unit and the card's own leading already carries the air |
| "Vertical rhythm between cards 11dp" | **`snug` 12** | on the unit |
| "Section spacing 28dp" | **`rest` 32** | on the unit, and it needs a `break` 52 above it to be a rhythm rather than a metronome |
| "Sheet content top padding after the handle 18dp" | **`step` 20** | on the unit |
| "4dp base grid" | kept, and **made true**: 53 percent of the app's spacing literals are off it today | |

## F.3 Section 6.1, elevation

| quoted today | proposed | reason |
|---|---|---|
| "FAB: `y 5dp blur 16dp actionBlue 40%`" | **`y 1 b3 black 6%` plus `y 4 b14 black 10%`** | a colored glow under a saturated element is the most dated construction in the capture. It is still one device, in the same paired form 6.1 already defines for a card. Dark and Contemplative keep no shadow |

## F.4 Section 10, components

| section | quoted today | proposed | reason |
|---|---|---|---|
| 10.1 | "Serif title at displayTitle, left aligned" | **`lead` 21.5 sans on the four root tabs; serif retained on every pushed screen** | the tab bar names this place 700dp below with a filled glyph and the word. A serif title on a tab is the only place the app spends its reading voice on a label rather than a sentence |
| 10.1 | "a 6dp warnAmber dot at its top right" | **inside the pill at the leading edge, 6dp** | outside the arc it is the badge construction on 15.1's tell list. Inside, it is the same grammar as an area dot on a filter chip. 10.1 already requires the label to be the signal |
| 10.2 | "Full width, parchment, 14dp radius, no border" | **a full-bleed Band at 0dp radius carrying the whole header, with a 32dp fade** | 10.2 applies 6.1's step-2 device to the one element on the screen that needs no separation, so it carries two. Removing the box deletes the app's second loudest rectangle and gives the flagship a top |
| 10.2 | "A bodyStrong sentence and a caption line" | **`voice` 21.5 Newsreader and `meta` 12.5** | the sentence is the app's whole personality and it is styled as interface chrome |
| 10.3 | "18 by 17dp padding, 11dp gaps between cards" | **18 by 12, gaps `snug` 12** | F.2 |
| 10.3 | the four-row content list | **a body and a 28dp `raise` foot**, with the status line moving into the foot and a 16dp chevron joining it when the queue is not empty | the card carries two of the five facts it owes and every missing one is already in `AreaCardModel`. The foot is what makes cards vary in height |
| 10.3.1 | "66dp action width" and a 10.5sp label | **Complete 72, Swap 72, Delete 56, all fixed, label `meta` 12.5/700** | `scaled(66.dp)` overflows at 200 percent: 264dp of face against 204dp of travel. A face is a box for a fingertip, not for text |
| 10.4 | "an inner pill with actionBlue at 10 percent" | **10 percent light, 16 percent dark**, and `SpaceEvenly` becomes `SpaceBetween` | at 10 percent over `raise` `#18181F` the active tab is near invisible in dark, and `SpaceEvenly` makes the rhythm shift as the inner pill grows |
| 10.5 | "48dp circle in actionBlue with the add glyph in `card`" | **64 x 56dp pill, 26dp glyph, right edge on `tabBarInset`, morphs to a 20dp squircle on press** | 48 is the accessibility floor, spent on the most important control of the flagship screen. And `screenPadding` 20 against the bar's `tabBarInset` 17 is a live 3dp misalignment |
| 10.7 | "**Primary:** actionBlue fill, `card` bodyStrong, 50dp, 12dp radius" | **four rungs, three grounds, three widths**, Part B | the roles were an enum where the app needed three axes, and `fillWidth = true` gives nine of fourteen call sites a full-width button they never asked for |
| 10.7 | "a translucent white pill at 9 percent" | **14 percent**, the other form 10.7 itself offers | at 9 percent on `#0B0B10` the onboarding primary reads disabled when it is enabled |
| 10.7 | (no disabled, no loading) | **the state matrix in B.4** | four of five roles have no disabled appearance and nothing in the app has a loading state |
| 10.8 | one chip description | **three populations with three separation devices** | one component is doing navigation and filtering, so two identical pills do different things |
| 10.10 | "Labels sit beneath at 9.5sp" | **`meta` 12.5/600** | 9.5sp is below the app's smallest role, on the one control whose subject is how large your text is |
| 10.10 | (no ground stated) | **the tiles sit in a full-bleed `raise` Zone** | the Light tile renders the light `canvas` on the light `canvas` and has no visible container at all |
| 10.11 | "a 26dp rounded-square icon badge tinted at 11 to 14 percent of a per-group color" | **deleted, glyph and square** | four of sixteen glyphs are duplicates, one is the brand mark, and the column costs 41dp of measure on every row |
| 10.11 | "Rows sit directly on the canvas separated by hairlines" | **separated by `snug` 12 of air, nothing drawn** | 6.1 puts a hairline fourth, "only if all three above have genuinely failed", and between two 56dp rows whitespace has not failed |
| 10.11 | "a title at 15sp semibold" | **`readStrong` 18/600 on the 20dp measure** | the fourth left edge disappears with the badge |
| 10.11 | (switch not specified) | **track `inkPrimary`, thumb `card`** | 3.1 scopes `actionBlue` to a control you press. A switch track is a state readout |
| 10.12 | "a hairline running to the trailing edge" | kept, plus **a named variant: a sidehead whose block has a denominator carries it right aligned at the end of the rule** | two call sites, Momentum's `Your areas` and any future one. The rule starts `tight` 8 past the label's **ink** rather than its advance |
| 10.15 | (the FAB in the zero-areas state) | **suppressed while `Create an area` is on screen** | two controls doing the same thing 700dp apart is an ambiguity this audience pays for |
| 10.18 | the End session pill at 50dp | **Anchor 76 x 240dp, the ring's own diameter**, with `Add 10 minutes` at Standard 48 | 50 and 48 is a 2dp gap between a primary and a tertiary, and the tertiary's press scale is applied to its `Text`, so it shrinks 17sp of type by 0.5dp |
| 10.19 | the field spec, Daylight only | **the same rule stated for the Contemplative world** | `OnboardingField` is the last hairline field in the app, and 10.19's own logic removes it |
| 10.20 | "Restore is a secondary button" at `weight(1f)` | **Hug, left aligned, in a 72dp `raise` foot** | `weight(1f)` halves are the one width that makes a deliberately weighted pair equal |

## F.5 Section 11, surface art direction

| quoted today | proposed | reason |
|---|---|---|
| Focus: "item title in bold sans at 26sp" | **`lead` 21.5** | the last off-ladder sans size, and it makes the item title one size in all four places a person meets it |
| Focus: 3.3's "The gradient's center sits at 0.5 across and **0.42 down**, above the middle, because that is where the ring is" | **0.48 down** | 0.42 x 914.3 = 384dp; the ring's measured center is 467.8dp. **The light is 80dp above the object it is the room for.** 3.3's two justifications point in opposite directions and its own sentence makes the light a property of the object, so "where the ring is" wins. One constant, three screens, and 3.3's rule that the light does not move with content is untouched |
| Momentum: "area tiles" as a three column grid | **the tile becomes the leading 44 x 44dp Cell of the area's row** | the tiles carry one bit each and the same two areas are drawn again 300dp lower with real numbers. 3.4's 60 percent grant is preserved and spent on something that carries a reading |
| Momentum: "three stats as pure typography" in a row | **three rows** | 15.1 lists stat banners, and `Minutes focused` already overflows the middle column at the default text size |
| Momentum: the module order in `MASTER_BUILD_PROMPT.md` 12.2, area balance / completion pace / focus pattern / idle areas | **This week, Focus, Your areas, Eight weeks, with idle merged in as a subline** | the screen runs through five time windows in an order with no direction, for an audience that includes time blindness. Cheap to refuse: keep 12.2's order and the screen still gains its top, its type ranks and its four gap values |
| Momentum: `Quiet areas` as its own module | **a subline in the one list where all the areas already are**, wording unchanged | 12.2 asks the idle module to be gentle. A heading makes neglect a category; a subline makes it a fact about one row |
| Trail: "A screen title reading `Trail` in displayTitle" | **`lead` 21.5 sans** | the reasoning that gave it a serif was that Newsreader had five call sites of which four were empty states, so the signature face meant "there is nothing here". After this document the serif carries the Areas band sentence, both Momentum and Report headlines, every pushed screen title and every onboarding beat, so that absence is gone and the Trail's 31sp day header is a larger typographic event than the title was |
| Trail: "Day headers as bodyStrong with an inline count and a hairline to the trailing edge" | **`leadStrong` 31 with the count right aligned on the margin and no hairline** | the day header is the dominant, and a rule on top of a 31sp heading with `break` 52 above it is a second separation device on an element that has one |
| Pulse: the ambient observation at `bodySerif` | **holds at `speak` 26 in both states** | answering the Pulse currently shrinks the observation by 35 percent. No section asks for that |
| Pulse: the rhythm row in ambient only | **drawn in both states, as the room's fixed ceiling** | the top quarter of a 544dp sheet contains a grip and nothing else, and the two states are currently two different screens that share a ground |
| Pulse: response pills at 50dp | **Commit 60 x 240dp, `snug` 12 apart** | 300 of 320dp makes the answers as wide as the question. 240 is 75 percent of the measure, which makes the text the subject |
| 11.1 item 5: "**A gold rule**, full width, fading at both ends" | **deleted; `break` 52 replaces it** | it is the one rule on the page with no label on either side of it, and it marks a boundary 42dp above the one that happens |
| 11.1 item 7: "bounded top and bottom by full-bleed gold rules" | **no rules; the tint is the device** | a rule plus a tint is two devices on one element, 6.1, and 15.1's "a hairline border and a diffuse shadow on the same element" in gold |
| 11.1 item 6: "Each a sidehead followed by bodySerif prose, 28dp apart" | **`rest` 32**, with `break` 52 around the headline, the pattern break and the closing block | one gap used six times is a metronome. 12 : 32 : 52 is three levels a reader can feel |
| 11.1 item 8: "34dp of space above. Centered." on the bare ground | **a full-bleed Band at gold 3 percent, inset 30dp, 40dp of vertical padding** | the aside has a container and the page's most important sentence does not. And 3.3's "two centers of light" then becomes two bands and two lights in the same two places, by construction |
| 11.1: "the air under the pill is the air above it, 12dp of layout against 24dp" | **20 and 20** | that optical argument was made against a 48dp pill. At 76dp the pill carries 29dp of internal air below its own text, so 12dp of layout would draw as 41 against 30 and the asymmetry would invert. This restores 11.1's stated intent under the new geometry |
| 3.3: the Report's glows | **`HEADLINE_Y` 0.30 to 0.21, `CLOSING_Y` 0.86 to 0.70, `CLOSING_REACH` 0.42 to 0.38** | both lights are below the elements they exist to light, by 80 and 123dp, and the second one's brightest point sits 10dp inside the bottom fade band and 26dp under the floating tab bar |
| 11.2: the re-entry block | the serif line stays where it is; **the two options move to their own field's 45 percent optical center**, and the second one loses its transparent button box | the second option is a full-measure transparent rectangle 8dp under a live filled button of identical width, on the app's highest-stakes screen |
| 14.4: About through `PushedScreen` | **About draws no pushed title** | it is the only pushed screen whose first content element is the app's own name at display size, so today it announces itself twice, 100dp apart |
| 13.1 / 13.2: an 80dp progress bar in onboarding and `1 of 5` in the tutorial | **four marks and five marks, one idiom** | two idioms for "where am I in a sequence", crossed inside three seconds, and `1 of 5` is 15.1's numbered step sequence |

---

# PART G. WHAT SURVIVES, AND WHY

The brief says nothing is assumed fine, so every element left alone owes a sentence.
**The honest headline is that most of the reasoning survives and almost none of the
geometry does.** What is right about this app is its arguments; what is wrong is that
they were each made once, locally, and never against each other.

**Untouched, and load-bearing.**

1. **The whole swipe gesture model.** Every threshold, the 1,200dp/s fling, the
   one-open-row coordinator, the 180ms slide and 200ms collapse, and rule 12's law that a
   full left swipe commits Swap while delete needs a deliberate tap and an undo window.
   Only face widths and label size move.
2. **The Report's week ribbon, entire.** Seven marks at 5dp and 2dp radius, an 18dp fixed
   gap, height and opacity linear from a floor, initials at 8sp, one caption. It is the
   best graphic in the app, its floor argument is correct, its fixed gap is what makes it
   scalable, and 11.1's refusal to curve the scaling is the sharpest sentence in the
   document.
3. **The Report's three axes**, its reveal timings, its refusal of specks, and its
   controls floating over the page rather than inside it.
4. **The accept pill's settle.** The front leaving the center is the reduced prominence
   arriving, one brush so nothing ever gets brighter, the ground halves and the label does
   not, and a settled pill stops describing itself as a control. Best-argued animation in
   the app. Only its box grows.
5. **The Focus ring, entire.** 240dp, 6dp stroke, 10dp tip, a 15dp falloff drawn as a
   radial rather than a mask filter. The thin-ring-with-a-bright-tip argument is correct
   and visible.
6. **The Focus numeral's 1.3x font-scale cap**, taken against the combined scale through
   `cappedFontScale` so the platform's non-linear sp curve survives.
7. **The breathing glow and the specks**, including that the glow is disabled rather than
   slowed under reduce motion so no infinite animation holds the frame loop open for a
   constant, and that specks are placed by `StableHash` per key with a different seed per
   surface.
8. **The six-element rule on the Focus session**, including the refusal to add
   `of 25 minutes`.
9. **The Pulse pills staying identical and stacked**, and the fact that it is enforced by
   there being one composable with no parameter that could make either louder. Only their
   height, width and gap move.
10. **The Pulse's radial fill from the tap point**, and its three rhythm-row states with
    the floor, which already answer section 13 with form as well as opacity and are the
    model Momentum's marks now copy.
11. **The Pulse's invisible acknowledgment reserve** in principle: composing it at zero
    opacity so nothing moves when it arrives is correct. Only its anchoring changes.
12. **The floating tab bar's 61dp height, 17dp inset and one-label rule**, and the entire
    314dp measurement argument behind them. It is the one element in the app with a width
    it cannot grow out of. Two values move and nothing else.
13. **`ClarityTextField`.** The best-reasoned component here, and the Contemplative field
    is being brought up to match it rather than the reverse.
14. **`clarityClickable` with `indication = null`.** Refusing the Material ripple in favor
    of the scale press is what makes this feel like one product rather than a themed
    Material app.
15. **`clarityFocusRing`**, correctly argued as belonging to state rather than to
    separation, and extended to the six controls that lack it.
16. **The sheet handle's 34 x 4dp at 18 percent ink.** Platform affordance vocabulary;
    redesigning it would cost recognition and buy nothing. Only its position and its
    padding's scaling behavior change.
17. **`UndoSnackbar`'s 2dp depleting line**, which becomes the app's one progress
    language and is reused by the new loading state so nothing is invented.
18. **`AppearancePicker`'s real miniatures** drawn from the live tokens rather than from
    four hex values, and the 103 degree System split. The most genuinely distinctive
    element in the app. Only the ground under it moves.
19. **`TextSizePicker`'s argument that the screen is its own preview**, and that every
    option label renders at the current size rather than at its own. The single best piece
    of accessibility reasoning in the repo.
20. **The archive's two written-out actions**, its refusal of a swipe, a glyph and a
    detail sheet, its restore-to-position rule, and its two-state distinction between
    *empty* and *not loaded yet*.
21. **The 7dp area dot and the four permitted forms of area color**, and the wash pooled
    to the hashed corner, which is what makes two cards distinguishable at a glance
    without a stripe. The wash now runs over the card's foot rather than being cut at the
    seam.
22. **The Trail's icon column carrying a glyph and nothing else**, and its no-maxLines,
    no-ellipsis rule on the sentence. 15.3 refuses a semantic palette for event glyphs by
    name.
23. **`SettingsToggleRow`'s whole-row target with a null-callback switch.**
24. **The choice row's three-signal selection**: a check that is a shape, a label that
    moves from `inkSecondary` to `inkPrimary`, and `selected` in semantics.
25. **`ElsewhereRow`'s refusal of a chevron**, which is what tells this document where the
    external mark belongs.
26. **`ReEntryScreen`'s two-callback signature**, so that "no number appears here" is a
    fact about the type rather than a rule somebody has to keep, and its absent
    `BackHandler`.
27. **The tutorial's entire mechanism**, and its no-branch rule.
28. **Beat 1's demo cards using the real `AreaPalette` colors**, and its nine second
    timeline.
29. **`ClaritySpacing`'s fixed-against-scaled doctrine.** Every new dimension in this
    document is classified by it.
30. **The sidehead as a sentence-case label with a rule**, 10.12, and 15.3's refusal of
    all-caps. Only the rule's start point moves.
31. **The permission card**, its 18dp radius, its `card` ground and its elevation.
32. **The support block's copy rules** and its measurement that the accent belongs on the
    18dp glyph rather than on the heading.

**And one thing that survives by refusal rather than by inertia:** the FAB does not move
with content. A control that changes position based on how much is on screen is
unpredictable, and predictability outranks composition for an audience with executive
dysfunction. It costs nothing anyway: once the stack is anchored, the FAB sits 66.9dp
below the last card instead of 289dp below it, so the orphan problem dissolves without
moving the object.

---

# PART H. WHAT MUST BE MEASURED BEFORE ANY OF THIS SHIPS

Every color proposed here goes into the total audit at a 4.5:1 floor for text and 3.0:1
for a graphic that carries meaning. **None of the figures below is a measurement.** They
are estimates from the token hex values in `ClarityColors.kt`, stated so a builder knows
what to expect and knows the difference.

| pair | where | estimate | floor | named fallback if it fails |
|---|---|---|---|---|
| `inkPrimary` and `inkSecondary` on the light parchment Band `#EFEEE2` | the Areas header | L\* 93.6, lighter than `canvas` at 91.4, so both should improve on their canvas readings | 4.5 | the Band takes `raise` instead of parchment and becomes a Zone |
| the dark parchment Band at 55 percent over `#0E0E13`, carrying both inks | the Areas header, dark | lands near L\* 8.5, between `canvas` and `raise` | 4.5 | as above |
| **the area accent as text on `raise` under the wash** | the card's in-session foot line | never measured. 3.4 verified the area label against exactly two grounds and this is a **third**: 48 colors x 2 worlds = **96 new pairs** | 4.5 | the in-session line stays in the card body as row four, today's behavior, and the foot carries only the queue chevron |
| `inkSecondary` on the card's `raise` foot, both worlds | the idle foot line | should track its `card` reading closely | 4.5 | as above |
| the area accent at **60 percent on a 44dp Cell** | Momentum's area rows | 48 colors x 2 worlds = **96 pairs**, at the graphic floor. Today's 115.8dp tile is the same pair at a different size and may already be in the audit | 3.0 | the Cell takes the `hairline` outline in both states and the fill is dropped |
| `card` thumb on an `inkPrimary` switch track, both worlds | Settings | about 17:1 light, 14.5:1 dark | 3.0, a graphic | the thumb takes `raise` |
| `actionBlue` at 6 percent as a ground carrying an `actionBlue` label | the inline `Add an item` pill, both worlds | about 10.5:1 light, 6.5:1 dark | 4.5 | the label takes `inkPrimary` and the ground stays |
| the dark tab pill at **16 percent** | the active tab, dark | about 5.27:1, down from 6.07 at 10 percent | 4.5 | 14 percent |
| **white at 6 percent** as the onboarding panel fill and the Contemplative field well, carrying `textBright` and `textDim` | onboarding | about 11:1 and 5.6:1 on a ground near L\* 11 | 4.5 | 8 percent, still inside 6.1's 3 to 5 percent band at the glow's center |
| **white at 14 percent** on `deepBlack` carrying `textBright` | every Contemplative anchor | about 11:1 | 4.5 | |
| gold at **3 percent** over `deepBlack`, carrying `closingLine` `#EDE9DF` | the Report's closing band | about L\* 6.5 and about 15:1 | 3.0 for the band, 4.5 for the text | 4.5 percent, matching the aside |
| gold at 14 percent carrying `readStrong` 18/600 in `textBright` | the accept pill at 76dp | the existing pill measures this pair at 17sp/600 and the change is one size step, so it should hold. **Confirm rather than assume** | 4.5 | |
| `read` 15 `textDim` on `deepBlack` under the evening tint's composited edge, about `#1D1610` | the Pulse History label | `textDim` measures 5.70 on bare `deepBlack`; the tinted edge has never been measured for text | 4.5 | `textBright` |
| `deleteMuted` as a bare label on `card` at `readStrong` 18 | the sheet's Delete button | it is already a `read` 15 label on the same ground | 4.5 | |
| `meta` 12.5 `inkSecondary` on `canvas` | the Momentum dateline and every trailing readout | `inkSecondary` at 64 percent measures 4.88:1 on `canvas`, which clears. 12.5sp is a new size for the role and the audit is keyed on pairs rather than sizes, so this is a note rather than a measurement | 4.5 | |

**Two hundred of those pairs are the two 96-pair blocks**, and both have the same named
fallback, which is that the area accent stays where 3.4 has already measured it. Neither
blocks anything else in this document.

---

# PART I. IMPLEMENTATION ORDER

Ordered by value per unit of risk, cheapest first, so the owner sees change early. Each
wave ships on its own and each one is independently revertible.

## Wave 1: four numbers and two one-liners, no layout change at all

The largest visible improvement in this document per line changed.

| # | change | files | fixes |
|---|---|---|---|
| 1 | `HEADLINE_Y` 0.30 to **0.21**, `CLOSING_Y` 0.86 to **0.70**, `CLOSING_REACH` 0.42 to **0.38** | `report/ReportBackdrop.kt` | both Report lights are 80 and 123dp below what they light, and the second is inside the fade band |
| 2 | `GRADIENT_CENTER_Y` 0.42 to **0.48** | `focus/FocusBackdrop.kt` | the Focus pool of light is 80dp above the ring it is the room for. One constant, three screens |
| 3 | draw `PulseRhythmRow` in `PulseQuestion`; set the ambient observation to the 26sp serif | `pulse/PulseSurface.kt` | the flattest surface in the app, and its missing top |
| 4 | four radius values: `momentumTile` 11 to 12, `swatch` 16 to 18, `markBadge` 16 to 18, delete `weeklyBanner` | `theme/ClarityShapes.kt` | thirteen radii become six. Invisible individually, the largest cumulative effect here |

## Wave 2: the accessibility defects, which are broken rather than ugly

| # | change | fixes |
|---|---|---|
| 5 | **`ClarityRow` in `ui/components`**, one primitive replacing `SheetActionRow`, `QueueRow`, `CompletedRow`, `SettingsRow` and the inbox rows, with `Role.Button`, a focus ring, a press ground and a 48dp floor | defects 4, 5 and 8, and half of 9 |
| 6 | **the state matrix on `ClarityButton`**, plus `Hug` as the width default | defects 1 and 9. **Fixes the re-entry ghost button**, which is the highest-consequence defect found |
| 7 | segments to 48dp and Hug; the swipe faces stop scaling; the sheet handle's padding stops scaling; the dark tab pill to 16 percent; the FAB to `tabBarInset` | defects 7, 12, 13, 14, 15 |
| 8 | **the tutorial's `Tap to continue` becomes a real control** and Skip gets an opaque ground | defects 3 and 6 |

## Wave 3: the scales, which touch every call site and change no logic

| # | change |
|---|---|
| 9 | **the type scale**: eleven roles, every line height a multiple of 4. Mechanical, wide, and the single highest-value change in the document |
| 10 | **the spacing ladder**: six tokens, and `ClaritySpacing`'s four named values moved |
| 11 | **the rung ladder and the anatomy formula** applied at every call site |
| 12 | **the three left edges**, which falls out of 9 and 10 on most screens |

## Wave 4: the components with new geometry

| # | change |
|---|---|
| 13 | **the FAB**: 64 x 56, 26dp glyph, the paired black shadow, the press morph |
| 14 | **the three chip populations**, with the selected corner morph and the Pulse dot moving inside the pill |
| 15 | **the Contemplative twins**: `ClarityAnchor` and `ClarityTextAction`, the onboarding well, 9 to 14 percent, the fork's equal heights and translucent fill, the four nav marks |
| 16 | **the settings rebuild**: the badge, the hairlines and the seven hues deleted, the appearance Zone added, the switch track to ink |

## Wave 5: the page compositions

| # | change |
|---|---|
| 17 | **Areas**: the Band, the card's foot, the anchoring formula, the zero-areas state |
| 18 | **the area sheet**: the head, the Active zone, the fields, 10.20's pair |
| 19 | **the Trail**: clusters from data already computed, the 31sp day header, the timestamp column deleted |
| 20 | **Momentum**: the header, the four gap values, three stat rows, the area row merge, seven circles |
| 21 | **the Report**: three rules deleted, three gap levels, the closing band, the Anchor rung |
| 22 | **About, the archive, the re-entry screen, the tutorial card** |
| 23 | **the loading state**, the only item here that needs a ViewModel flag. Cut it first if scope is tight |

**If only one wave ships, ship wave 1.** Six numbers, no layout risk, and both Contemplative
rooms stop being lit in the wrong place.

---

# PART J. OPEN CHOICES, RECORDED UNDER SECTION 15

Where the design left a choice open, the obvious answer is named and something else is
chosen, or the obvious answer is taken and the reason is recorded.

| choice | the statistically common answer | taken | why |
|---|---|---|---|
| the type ratio | the Major Third, 1.250, which every type-scale generator defaults to | **1.200** | 1.25 from 12.5 puts five near-equal steps between 15 and 30, which reproduces the exact defect being fixed. Contrast comes from skipped steps, which 1.200 can afford and 1.250 cannot |
| the spacing ladder | the 8pt grid, 4 / 8 / 16 / 24 / 32 / 48 | **Fibonacci on 4: 4 / 8 / 12 / 20 / 32 / 52** | an 8pt ladder's ratio collapses from 2.00 to 1.25 as it climbs, so its top steps are indistinguishable, which is why forty seven literals exist. This one is additive and holds 1.5 to 1.67 |
| the column count | 4, Material's compact window class | **6** | 4 cannot express thirds, and the app's only production multi-column layout is a three-up row already sitting on a six-column grid |
| the control ladder | M3 Expressive's 32 / 40 / 56 / 96 / 136 | **36 / 48 / 60 / 76** | 15.3 refuses adopting an Expressive default because it is the default, and two of those rungs are for a control alone on a screen, which this app never has |
| a sparse home screen | fill it: contextual onboarding, a suggestion block, a personalized card, a stat row | **compose it: anchor to a 1 : 1.6 margin pair, taper the measure, bound both ends** | filling contradicts the app's thesis and puts the app's voice where it has nothing to say. The void was never too large. It was all on one side |
| the engine's sentence | a card or a tinted box with a rounded radius | **a full-bleed Band with a fade and no edge** | 10.2's box applies 6.1's step-2 device to the one element that needs no separation, so it carries two |
| its size | make it the biggest thing on the screen | **level with the item title, in the serif** | 10.3 says the item title is the most important string. Family, ground, position and width carry the distinction instead of size |
| a screen title | a large display serif, the 2026 editorial default | **sans on the four tabs, serif on pushed screens** | a serif title on a tab is the one place the app spends its reading voice on a label, and the tab bar already says the word |
| the end of a finished list | a sentence, a logo, or a rule | **nothing, and the composition bounds it** | a sentence about their data needs a corpus. A mark that carries meaning must clear 3.0:1 and stops being quiet; one that does not is decoration. A logo is a watermark |
| the card's queue signal | a count, a badge, a dot, or a row of pips | **one 16dp chevron, present or absent** | section 14 forbids a badge and a count, and a row of pips silhouettes as a dashed stripe |
| Momentum's tiles | make them bigger so the accent has presence, which is what phase 7 did | **44dp, leading a row that carries a reading** | presence was measured in area when it should have been measured in relationship |
| Momentum's area order | rank by magnitude, which every analytics list does | **the person's own order** | a ranked list of your own life areas is a leaderboard, which for this audience is a scoreboard of neglect with the sign flipped |
| Momentum's figures | a heavy sans figure over a caps label | **Newsreader, because Hanken ships no `tnum`** | the second half of 11's existing argument, unchanged |
| the Trail's density | group and cluster to reduce clutter | **taken, and recorded as taken** | this is the one place the obvious answer is genuinely best: the clusters are already computed and thrown away, so drawing them costs no query. Section 15 permits taking the obvious answer where it is right, on the record |
| the Trail's day header | a sidehead with a rule, per 10.12 | **a 31sp heading with no rule** | a log is navigated by its landmarks. The size is the device and a rule would be the second |
| the archive's two actions | equal halves, an icon, or a swipe | **Hug, weighted, left aligned, in a foot** | 10.20 already refused the icon and the swipe. `weight(1f)` is the one width that makes a weighted pair equal |
| the empty state's alignment | centered, with an illustration and a button | **left on the measure, no illustration, one button, no FAB** | the illustration is already forbidden. Centering is the half nobody questions |
| the onboarding fork | weight one panel as the recommended door | **equal fills, equal heights** | Addendum 01 8a requires a genuine equal alternative, so this choice is shut rather than open |
| the segmented control's frame | wrap it in a raised zone | **hug it, and let 103dp of canvas be the frame** | a second Zone on that screen would make the Zone a rhythm instead of an exception |
| a disabled label | `inkTertiary`, the conventional grey | **`inkSecondary`** | 10.3, 10.9, 10.11 and 10.19 each reached that resolution in four separate phases. A floor is a floor |
| a loading state | a spinner, or a dimmed label | **a 2dp line, and nothing else changes** | section 14 refuses the spinner, a dimmed label fails the floor, and `UndoSnackbar` already draws exactly this line |
| the Focus session's remaining time | state the planned length under the numeral | **refused; the arc is the answer** | a seventh element breaks the count that is that screen's best property |
| the Pulse's two anchors | grant both the top rung, since the design names them an exception | **Commit 60, not Anchor 76** | the exception should be paid for by the anchors, not granted to them. And answering writes an event, which is Commit's own definition |
| the Focus anchor's width | the `Wide` constant | **240dp, the ring's diameter** | a relationship a person cannot name and can see beats a number that relates to nothing |
| the Report headline | raise it to the ladder's next step, 45 | **hold at 40** | it already passes every dominance clause, and 45 costs a fourth line on the longest headlines |

---

# THE ONE PARAGRAPH READING

The app is not lifeless because its elements are badly made. Every one of them is
individually well argued, and that is precisely the problem: **there are 35 controls, 16
containers and 6 screens, each reasoned on its own, and no two of them reasoned against
each other.** The measurable consequences are that 64 percent of the type roles live
inside one octave, 53 percent of the spacing literals are off the grid the document
claims, there are four content left edges and no columns, thirteen corner radii sit inside
a 20dp band, twenty six of thirty five controls are the same height, and on the flagship
screen the largest single element is 355dp of nothing with one edge. The fix is one ratio,
one 4dp unit, six columns, four control rungs, five container kinds, and about a dozen
elements removed. **Almost nothing here is added.** The area card gains a foot, the Areas
header gains a ground, the Report gains a band, and everything else in this document is a
subtraction, a re-proportioning, or a number moved onto a ladder that already existed.
